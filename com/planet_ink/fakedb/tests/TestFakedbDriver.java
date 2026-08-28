package com.planet_ink.fakedb.tests;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/*
   Copyright 2026-2026 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
public class TestFakedbDriver
{
	private static int						passed		= 0;
	private static int						failed		= 0;
	private static PrintStream				realErr;
	private static int						tableCounter= 0;

	private static final List<java.sql.Connection>	allConnections	= new ArrayList<java.sql.Connection>();
	private static final List<File>					allTempDirs		= new ArrayList<File>();

	private interface SQLAction
	{
		void run() throws Exception;
	}

	private static void check(final String name, final boolean cond, final String failMsg)
	{
		if (cond)
			passed++;
		else
		{
			System.out.println("[FAIL] " + name + ": " + failMsg);
			failed++;
		}
	}

	private static void checkEq(final String name, final Object expected, final Object actual)
	{
		final boolean eq = (expected == null) ? (actual == null) : expected.equals(actual);
		check(name, eq, "expected [" + expected + "] but got [" + actual + "]");
	}

	private static void expectEx(final String name, final SQLAction action, final String expectFragment)
	{
		try
		{
			action.run();
			System.out.println("[FAIL] " + name + ": expected exception but none was thrown");
			failed++;
		}
		catch (final SQLException e)
		{
			if ((expectFragment == null) || (e.getMessage() != null && e.getMessage().toLowerCase().contains(expectFragment.toLowerCase())))
				passed++;
			else
			{
				System.out.println("[FAIL] " + name + ": expected message containing '" + expectFragment + "' but got '" + e.getMessage() + "'");
				failed++;
			}
		}
		catch (final Exception e)
		{
			if (expectFragment == null)
				passed++;
			else
			{
				System.out.println("[FAIL] " + name + ": expected SQLException but got " + e.getClass().getSimpleName() + ": " + e.getMessage());
				failed++;
			}
		}
	}

	private static void runPhase(final String name, final SQLAction phase)
	{
		try
		{
			phase.run();
		}
		catch (final Throwable t)
		{
			System.out.println("[FAIL] " + name + ": " + t.getClass().getSimpleName() + ": " + t.getMessage());
			failed++;
		}
	}

	private static File createTempDB() throws IOException
	{
		final File dir = new File(System.getProperty("java.io.tmpdir"),
				"fakedbtest-" + System.nanoTime() + "-" + (int) (Math.random() * 100000));
		if (!dir.mkdirs())
			throw new IOException("Could not create temp dir " + dir);
		if (!new File(dir, "fakedb.schema").createNewFile())
			throw new IOException("Could not create schema file");
		allTempDirs.add(dir);
		return dir;
	}

	private static java.sql.Connection connect(final File dir) throws SQLException
	{
		final java.sql.Connection c = DriverManager.getConnection("jdbc:fakedb:" + dir.getAbsolutePath());
		allConnections.add(c);
		return c;
	}

	private static String nextTableName()
	{
		return "T" + (++tableCounter);
	}

	private static int countRows(final java.sql.Connection c, final String table) throws SQLException
	{
		final java.sql.Statement st = c.createStatement();
		final java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
		rs.next();
		final int n = rs.getInt(1);
		rs.close();
		st.close();
		return n;
	}

	private static int countResults(final java.sql.Connection c, final String sql) throws SQLException
	{
		final java.sql.Statement st = c.createStatement();
		final java.sql.ResultSet rs = st.executeQuery(sql);
		int n = 0;
		while (rs.next())
			n++;
		rs.close();
		st.close();
		return n;
	}

	private static String querySingle(final java.sql.Connection c, final String sql, final int colIndex) throws SQLException
	{
		final java.sql.Statement st = c.createStatement();
		final java.sql.ResultSet rs = st.executeQuery(sql);
		String result = null;
		if (rs.next())
			result = rs.getString(colIndex);
		rs.close();
		st.close();
		return result;
	}

	private static void deleteRecursively(final File f)
	{
		if (f == null || !f.exists())
			return;
		if (f.isDirectory())
		{
			final File[] children = f.listFiles();
			if (children != null)
				for (final File child : children)
					deleteRecursively(child);
		}
		f.delete();
	}

	private static void cleanup()
	{
		for (final java.sql.Connection c : allConnections)
		{
			try
			{
				if (!c.isClosed())
					c.close();
			}
			catch (final SQLException e)
			{
			}
		}
		allConnections.clear();
		for (final File dir : allTempDirs)
			deleteRecursively(dir);
		allTempDirs.clear();
	}

	private static void testDriver() throws SQLException, IOException
	{
		final java.sql.Driver driver = DriverManager.getDriver("jdbc:fakedb:/tmp");

		boolean found = false;
		for (final java.sql.Driver d : Collections.list(DriverManager.getDrivers()))
		{
			if (d instanceof com.planet_ink.fakedb.Driver)
				found = true;
		}
		check("driver-registered", found, "Driver not registered with DriverManager");

		check("acceptsURL-valid", driver.acceptsURL("jdbc:fakedb:/tmp"),
				"acceptsURL returned false for jdbc:fakedb URL");
		check("acceptsURL-invalid", !driver.acceptsURL("jdbc:mysql://localhost"),
				"acceptsURL returned true for non-fakedb URL");
		check("acceptsURL-exact-prefix", !driver.acceptsURL("jdbc:fakedbxyz"),
				"acceptsURL returned true for jdbc:fakedbxyz (should require jdbc:fakedb:)");

		File tmpDir = null;
		java.sql.Connection c = null;
		try
		{
			tmpDir = createTempDB();
			c = driver.connect("jdbc:fakedb:" + tmpDir.getAbsolutePath(), null);
			check("connect-valid", c != null, "connect returned null for valid URL");

			final java.sql.Connection c2 = driver.connect("jdbc:mysql://localhost", null);
			check("connect-invalid-null", c2 == null, "connect returned non-null for non-fakedb URL");
		}
		finally
		{
			if (c != null) c.close();
		}

		check("majorVersion-3", driver.getMajorVersion() == 3,
				"getMajorVersion returned " + driver.getMajorVersion() + " expected 3");
		check("minorVersion-0", driver.getMinorVersion() == 0,
				"getMinorVersion returned " + driver.getMinorVersion() + " expected 0");
		check("jdbcCompliant-false", !driver.jdbcCompliant(),
				"jdbcCompliant returned true");

		final DriverPropertyInfo[] pi = driver.getPropertyInfo("jdbc:fakedb:/tmp", null);
		check("propertyInfo-empty", pi != null && pi.length == 0,
				"getPropertyInfo returned " + (pi == null ? "null" : pi.length + " elements"));

		expectEx("getParentLogger-throws", () -> driver.getParentLogger(), null);
	}

	private static void testConnection() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final java.sql.Connection c = connect(dir);

		check("createStatement-notnull", c.createStatement() != null, "createStatement returned null");

		expectEx("prepareCall-throws", () -> c.prepareCall("SELECT 1"), "Callable");

		try
		{
			check("getAutoCommit-true", c.getAutoCommit(), "getAutoCommit returned false");
			c.setAutoCommit(true);
			check("setAutoCommit-true-ok", true, "setAutoCommit(true) should not throw");
		}
		catch (final SQLException e)
		{
			check("setAutoCommit-true-ok", false, "setAutoCommit(true) threw: " + e.getMessage());
		}

		expectEx("setAutoCommit-false-throws", () -> c.setAutoCommit(false), "AUTO_COMMIT");

		try
		{
			c.commit();
			check("commit-noop", true, "commit should not throw");
		}
		catch (final SQLException e)
		{
			check("commit-noop", false, "commit threw: " + e.getMessage());
		}

		try
		{
			c.rollback();
			check("rollback-noop", true, "rollback should not throw");
		}
		catch (final SQLException e)
		{
			check("rollback-noop", false, "rollback threw: " + e.getMessage());
		}

		check("getCatalog-FAKEDB", "FAKEDB".equals(c.getCatalog()),
				"getCatalog returned " + c.getCatalog() + " expected FAKEDB");

		check("getTransactionIsolation-NONE",
				c.getTransactionIsolation() == java.sql.Connection.TRANSACTION_NONE,
				"getTransactionIsolation returned " + c.getTransactionIsolation());

		expectEx("setTransactionIsolation-throws", () -> c.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED), "Transaction Isolation");

		expectEx("setSavepoint-throws", () -> c.setSavepoint(), "Savepoint");

		check("getMetaData-notnull", c.getMetaData() != null, "getMetaData returned null");
		check("isReadOnly-false", !c.isReadOnly(), "isReadOnly returned true");
		check("getHoldability-default",
				c.getHoldability() == java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT,
				"getHoldability returned " + c.getHoldability());

		final String schema = c.getSchema();
		check("getSchema-notnull", schema != null && schema.length() > 0, "getSchema returned null or empty");

		check("getWarnings-null", c.getWarnings() == null, "getWarnings returned non-null");
		try
		{
			c.clearWarnings();
			check("clearWarnings-noop", true, "clearWarnings should not throw");
		}
		catch (final SQLException e)
		{
			check("clearWarnings-noop", false, "clearWarnings threw: " + e.getMessage());
		}

		c.close();
		check("isClosed-true", c.isClosed(), "isClosed returned false after close()");

		final java.sql.Connection c2 = connect(dir);
		check("reopen-after-close", !c2.isClosed(), "reopened connection should not be closed");
		c2.close();
	}

	private static void testCrud() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final java.sql.Connection c = connect(dir);
		final java.sql.Statement st = c.createStatement();
		final String tbl = nextTableName();

		st.executeUpdate("CREATE TABLE " + tbl + " (USERID STRING KEY, NAME STRING NULL, AGE INTEGER NULL, TS LONG NULL, BIO STRING NULL)");
		check("create-success", true, "CREATE TABLE should not throw");

		expectEx("create-duplicate", () -> st.executeUpdate("CREATE TABLE " + tbl + " (X INTEGER)"), "already exists");

		expectEx("create-bad-type", () -> st.executeUpdate("CREATE TABLE BADTBL (X FOOBAR)"), "Illegal column type");

		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('u1','Alice',30,1000,'hello world')");
		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('u2','Bob',25,2000,'test')");
		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('u3','Charlie',35,3000,null)");
		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('u4','Alicia',28,4000,'hi')");
		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('u5','Zoe',40,5000,'goodbye')");
		check("insert-5-rows", countRows(c, tbl) == 5, "expected 5 rows, got " + countRows(c, tbl));

		expectEx("insert-partial-columns", () ->
				st.executeUpdate("INSERT INTO " + tbl + " (USERID, NAME) VALUES ('u6','Fail')"), null);

		expectEx("insert-dup-key", () ->
				st.executeUpdate("INSERT INTO " + tbl + " VALUES ('u1','Dup',99,99,'dup')"), "duplicate key");

		expectEx("insert-unknown-table", () ->
				st.executeUpdate("INSERT INTO NOTABLE VALUES ('x','y',1,1,'z')"), "unknown table");

		expectEx("insert-unknown-column", () ->
				st.executeUpdate("INSERT INTO " + tbl + " (NOCOL) VALUES ('x')"), "unknown column");

		expectEx("insert-bad-int", () ->
				st.executeUpdate("INSERT INTO " + tbl + " VALUES ('u7','Bad','notint',7,'x')"), "illegal value");

		expectEx("insert-col-val-mismatch", () ->
				st.executeUpdate("INSERT INTO " + tbl + " (USERID, NAME, AGE) VALUES ('u8','Only')"), "mismatch");

		java.sql.ResultSet rs = st.executeQuery("SELECT * FROM " + tbl);
		int rowCount = 0;
		while (rs.next())
			rowCount++;
		check("select-all-5", rowCount == 5, "SELECT * returned " + rowCount + " rows, expected 5");
		rs.close();

		rs = st.executeQuery("SELECT USERID, NAME FROM " + tbl);
		rowCount = 0;
		while (rs.next())
		{
			rowCount++;
			final String id = rs.getString(1);
			final String nm = rs.getString(2);
			check("select-cols-row" + rowCount, id != null && nm != null,
					"row " + rowCount + " got id=" + id + " name=" + nm);
		}
		check("select-cols-5", rowCount == 5, "SELECT USERID,NAME returned " + rowCount + " rows");
		rs.close();

		rs = st.executeQuery("SELECT USERID, NAME FROM " + tbl);
		if (rs.next())
		{
			final String idByName = rs.getString("USERID");
			final String idByIndex = rs.getString(1);
			check("select-by-name", idByName != null && idByName.equals(idByIndex),
					"getString by name [" + idByName + "] != by index [" + idByIndex + "]");
		}
		else
			check("select-by-name", false, "no rows returned");
		rs.close();

		check("count-5", countRows(c, tbl) == 5, "COUNT(*) returned " + countRows(c, tbl) + " expected 5");

		final int countWithWhere = countRows(c, tbl);
		final int filteredCount = countResults(c, "SELECT * FROM " + tbl + " WHERE AGE > 30");
		check("count-with-where-limitation",
				countWithWhere == 5 && filteredCount == 2,
				"COUNT(*)=" + countWithWhere + " (expected 5), filtered=" + filteredCount + " (expected 2)");

		check("where-eq", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE = 30") == 1,
				"WHERE AGE = 30 should return 1 row");
		check("where-ne", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE != 30") == 4,
				"WHERE AGE != 30 should return 4 rows");
		check("where-lt", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE < 30") == 2,
				"WHERE AGE < 30 should return 2 rows");
		check("where-gt", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE > 30") == 2,
				"WHERE AGE > 30 should return 2 rows");
		check("where-le", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE <= 30") == 3,
				"WHERE AGE <= 30 should return 3 rows");
		check("where-ge", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE >= 30") == 3,
				"WHERE AGE >= 30 should return 3 rows");

		check("where-and", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE > 20 AND AGE < 35") == 3,
				"WHERE AGE > 20 AND AGE < 35 should return 3 rows");
		check("where-or", countResults(c, "SELECT * FROM " + tbl + " WHERE AGE < 26 OR AGE > 35") == 2,
				"WHERE AGE < 26 OR AGE > 35 should return 2 rows");

		check("where-parens", countResults(c,
				"SELECT * FROM " + tbl + " WHERE (AGE > 30 AND AGE < 45) OR NAME = 'Bob'") == 3,
				"parenthesized WHERE should return 3 rows");

		final int likePrefixResult = countResults(c, "SELECT * FROM " + tbl + " WHERE NAME LIKE 'A%'");
		check("where-like-prefix", likePrefixResult == 2,
				"WHERE NAME LIKE 'A%' should return 2 rows (Alice, Alicia), got " + likePrefixResult
				+ " (fakedb LIKE prefix/suffix logic appears inverted)");
		final int likeSuffixResult = countResults(c, "SELECT * FROM " + tbl + " WHERE NAME LIKE '%e'");
		check("where-like-suffix", likeSuffixResult == 3,
				"WHERE NAME LIKE '%e' should return 3 rows (Alice, Charlie, Zoe), got " + likeSuffixResult
				+ " (fakedb LIKE prefix/suffix logic appears inverted)");
		check("where-like-contains", countResults(c,
				"SELECT * FROM " + tbl + " WHERE NAME LIKE '%li%'") == 3,
				"WHERE NAME LIKE '%li%' should return 3 rows (Alice, Alicia, Charlie)");
		check("where-like-exact", countResults(c,
				"SELECT * FROM " + tbl + " WHERE NAME LIKE 'Bob'") == 1,
				"WHERE NAME LIKE 'Bob' should return 1 row");

		final int eqNullResult = countResults(c, "SELECT * FROM " + tbl + " WHERE BIO = null");
		check("where-eq-null", eqNullResult == 1,
				"WHERE BIO = null should return 1 row (u3 has null BIO), got " + eqNullResult
				+ " (fakedb fileBuffer corruption after multiple queries corrupts null values)");
		final int neNullResult = countResults(c, "SELECT * FROM " + tbl + " WHERE BIO != null");
		check("where-ne-null", neNullResult == 4,
				"WHERE BIO != null should return 4 rows (non-null BIOs), got " + neNullResult
				+ " (fakedb fileBuffer corruption after multiple queries corrupts null values)");

		rs = st.executeQuery("SELECT * FROM " + tbl + " ORDER BY USERID");
		final List<String> orderedIds = new ArrayList<String>();
		while (rs.next())
			orderedIds.add(rs.getString(1));
		rs.close();
		final List<String> sortedIds = new ArrayList<String>(orderedIds);
		Collections.sort(sortedIds);
		check("orderby-asc", orderedIds.equals(sortedIds),
				"ORDER BY USERID ASC not sorted: " + orderedIds);

		rs = st.executeQuery("SELECT * FROM " + tbl + " ORDER BY USERID DESC");
		final List<String> descIds = new ArrayList<String>();
		while (rs.next())
			descIds.add(rs.getString(1));
		rs.close();
		final List<String> reverseSorted = new ArrayList<String>(sortedIds);
		Collections.reverse(reverseSorted);
		check("orderby-desc", descIds.equals(reverseSorted),
				"ORDER BY USERID DESC not reverse sorted: " + descIds);

		expectEx("orderby-non-indexed", () ->
				st.executeQuery("SELECT * FROM " + tbl + " ORDER BY AGE"), "non-indexed");

		expectEx("select-unknown-table", () ->
				st.executeQuery("SELECT * FROM NOTABLE"), "unknown table");
		expectEx("select-unknown-column", () ->
				st.executeQuery("SELECT NOCOL FROM " + tbl), "unknown column");

		rs = st.executeQuery("SELECT USERID, NAME, AGE FROM " + tbl);
		final ResultSetMetaData md = rs.getMetaData();
		check("metadata-colcount-3", md.getColumnCount() == 3,
				"getColumnCount returned " + md.getColumnCount() + " expected 3");
		check("metadata-colname-1", "USERID".equals(md.getColumnName(1)),
				"getColumnName(1) returned " + md.getColumnName(1));
		check("metadata-colname-2", "NAME".equals(md.getColumnName(2)),
				"getColumnName(2) returned " + md.getColumnName(2));
		check("metadata-coltype-1", md.getColumnType(1) == Types.VARCHAR,
				"getColumnType(1) returned " + md.getColumnType(1) + " expected VARCHAR");
		check("metadata-coltype-3", md.getColumnType(3) == Types.INTEGER,
				"getColumnType(3) returned " + md.getColumnType(3) + " expected INTEGER");
		if (rs.next())
		{
			check("metadata-nullable-userid", md.isNullable(1) == ResultSetMetaData.columnNoNulls,
					"USERID should be NOT NULL (KEY)");
			check("metadata-nullable-name", md.isNullable(2) == ResultSetMetaData.columnNullable,
					"NAME should be nullable");
		}
		rs.close();

		rs = st.executeQuery("SELECT * FROM " + tbl);
		check("rs-isBeforeFirst", rs.isBeforeFirst(), "isBeforeFirst should be true before next()");
		rs.next();
		check("rs-getRow-1", rs.getRow() == 1, "getRow returned " + rs.getRow() + " expected 1");
		check("rs-isFirst", rs.isFirst(), "isFirst should be true after first next() (fakedb always returns false — bug)");
		rs.last();
		check("rs-isAfterLast", rs.isAfterLast(), "isAfterLast should be true after last()");
		rs.close();

		rs = st.executeQuery("SELECT * FROM " + tbl + " WHERE USERID = 'u1'");
		if (rs.next())
		{
			check("getstring-1", "u1".equals(rs.getString(1)), "getString(1) returned " + rs.getString(1));
			check("getstring-2", "Alice".equals(rs.getString(2)), "getString(2) returned " + rs.getString(2));
			check("getint-3", rs.getInt(3) == 30, "getInt(3) returned " + rs.getInt(3) + " expected 30");
			check("getlong-4", rs.getLong(4) == 1000L, "getLong(4) returned " + rs.getLong(4) + " expected 1000");
			check("getboolean-age30", !rs.getBoolean(3),
					"getBoolean(3) for 30 should be false ('3' is not T/Y/1 per driver rules)");
			check("getobject-1", rs.getObject(1) != null, "getObject(1) should not be null");
			check("getbigdecimal-3", rs.getBigDecimal(3).intValue() == 30,
					"getBigDecimal(3) returned " + rs.getBigDecimal(3));
		}
		else
			check("getstring-1", false, "no row returned for USERID='u1'");
		rs.close();

		rs = st.executeQuery("SELECT * FROM " + tbl + " WHERE USERID = 'u3'");
		if (rs.next())
		{
			rs.getString(5);
			check("wasnull-bio", rs.wasNull(),
					"wasNull should be true for null BIO column (fakedb fileBuffer corruption after multiple queries)");
		}
		rs.close();

		st.executeUpdate("UPDATE " + tbl + " SET AGE = 31 WHERE USERID = 'u1'");
		final String ageAfterUpdate = querySingle(c, "SELECT AGE FROM " + tbl + " WHERE USERID = 'u1'", 1);
		checkEq("update-where", "31", ageAfterUpdate);

		st.executeUpdate("UPDATE " + tbl + " SET BIO = 'updated'");
		check("update-all-rows", countResults(c, "SELECT * FROM " + tbl + " WHERE BIO = 'updated'") == 5,
				"UPDATE all rows should set BIO='updated' for all 5 rows");

		st.executeUpdate("UPDATE " + tbl + " SET USERID = 'u1new' WHERE USERID = 'u1'");
		check("update-key-new", countResults(c, "SELECT * FROM " + tbl + " WHERE USERID = 'u1new'") == 1,
				"UPDATE key to new value should work");

		expectEx("update-key-dup", () ->
				st.executeUpdate("UPDATE " + tbl + " SET USERID = 'u2' WHERE USERID = 'u3'"), "duplicate key");

		expectEx("update-unknown-table", () ->
				st.executeUpdate("UPDATE NOTABLE SET X = 1"), "unknown table");

		expectEx("update-unknown-column", () ->
				st.executeUpdate("UPDATE " + tbl + " SET NOCOL = 1"), "unknown column");

		expectEx("update-bad-int", () ->
				st.executeUpdate("UPDATE " + tbl + " SET AGE = 'notint'"), "illegal value");

		st.executeUpdate("DELETE FROM " + tbl + " WHERE USERID = 'u2'");
		check("delete-1-row", countRows(c, tbl) == 4, "after deleting u2, expected 4 rows, got " + countRows(c, tbl));

		st.executeUpdate("DELETE FROM " + tbl);
		check("delete-all", countRows(c, tbl) == 0, "after DELETE all, expected 0 rows, got " + countRows(c, tbl));

		expectEx("delete-unknown-table", () ->
				st.executeUpdate("DELETE FROM NOTABLE"), "unknown table");

		final boolean isQuery = st.execute("SELECT * FROM " + tbl);
		check("execute-select-returns-true", isQuery, "execute(SELECT) should return true");
		final java.sql.ResultSet rsExec = st.getResultSet();
		check("execute-getresultset", rsExec != null, "getResultSet should not be null after SELECT");
		if (rsExec != null) rsExec.close();

		final boolean isUpdate = st.execute("INSERT INTO " + tbl + " VALUES ('x','y',1,1,'z')");
		check("execute-insert-returns-false", !isUpdate, "execute(INSERT) should return false");
		check("execute-insert-worked", countRows(c, tbl) == 1, "execute INSERT should add 1 row");

		st.close();
	}

	private static void testPrepared() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final java.sql.Connection c = connect(dir);
		final java.sql.Statement st = c.createStatement();
		final String tbl = nextTableName();

		st.executeUpdate("CREATE TABLE " + tbl + " (USERID STRING KEY, NAME STRING NULL, AGE INTEGER NULL, TS LONG NULL)");
		st.close();

		final java.sql.PreparedStatement insStmt = c.prepareStatement("INSERT INTO " + tbl + " VALUES (?, ?, ?, ?)");
		insStmt.setString(1, "p1");
		insStmt.setString(2, "Prepared1");
		insStmt.setInt(3, 20);
		insStmt.setLong(4, 100);
		insStmt.execute();
		check("prepared-insert", countRows(c, tbl) == 1, "prepared INSERT should add 1 row");

		insStmt.setString(1, "p2");
		insStmt.setString(2, "Prepared2");
		insStmt.setInt(3, 30);
		insStmt.setLong(4, 200);
		insStmt.executeUpdate();
		check("prepared-insert-2", countRows(c, tbl) == 2, "prepared INSERT 2 should add second row");

		insStmt.setString(1, "p3");
		insStmt.setNull(2, Types.VARCHAR);
		insStmt.setInt(3, 40);
		insStmt.setObject(4, null);
		insStmt.execute();
		check("prepared-insert-null", countRows(c, tbl) == 3, "prepared INSERT with nulls should add 1 row");

		final String nullName = querySingle(c, "SELECT NAME FROM " + tbl + " WHERE USERID = 'p3'", 1);
		check("prepared-null-value", nullName == null, "NAME should be null for p3, got " + nullName);

		final java.sql.PreparedStatement selStmt = c.prepareStatement("SELECT * FROM " + tbl + " WHERE USERID = ?");
		selStmt.setString(1, "p1");
		final java.sql.ResultSet rs = selStmt.executeQuery();
		if (rs.next())
		{
			check("prepared-select-name", "Prepared1".equals(rs.getString(2)),
					"prepared SELECT got name=" + rs.getString(2));
			check("prepared-select-age", rs.getInt(3) == 20,
					"prepared SELECT got age=" + rs.getInt(3) + " expected 20");
		}
		else
			check("prepared-select-name", false, "prepared SELECT returned no rows");
		rs.close();

		final java.sql.PreparedStatement updStmt = c.prepareStatement("UPDATE " + tbl + " SET AGE = ? WHERE USERID = ?");
		updStmt.setInt(1, 99);
		updStmt.setString(2, "p1");
		updStmt.execute();
		final String updatedAge = querySingle(c, "SELECT AGE FROM " + tbl + " WHERE USERID = 'p1'", 1);
		checkEq("prepared-update", "99", updatedAge);

		final java.sql.PreparedStatement delStmt = c.prepareStatement("DELETE FROM " + tbl + " WHERE USERID = ?");
		delStmt.setString(1, "p2");
		delStmt.execute();
		check("prepared-delete", countRows(c, tbl) == 2, "after prepared DELETE, expected 2 rows");

		final java.sql.PreparedStatement boolIns = c.prepareStatement("INSERT INTO " + tbl + " VALUES (?, ?, ?, ?)");
		boolIns.setString(1, "bool1");
		boolIns.setString(2, "booltest");
		boolIns.setBoolean(3, true);
		boolIns.setLong(4, 0);
		boolIns.execute();
		boolIns.setString(1, "bool0");
		boolIns.setString(2, "booltest");
		boolIns.setBoolean(3, false);
		boolIns.setLong(4, 0);
		boolIns.execute();
		final java.sql.PreparedStatement boolSel = c.prepareStatement("SELECT AGE FROM " + tbl + " WHERE USERID = ?");
		boolSel.setString(1, "bool1");
		final java.sql.ResultSet brs = boolSel.executeQuery();
		if (brs.next())
		{
			final boolean retrieved = brs.getBoolean(1);
			check("setBoolean-true-roundtrip", retrieved == true,
					"setBoolean(true) → getBoolean() returned " + retrieved + " — round-trip inconsistent (set stores 0, get only treats T/Y/1 as true)");
		}
		else
			check("setBoolean-true-roundtrip", false, "no row returned for bool1");
		brs.close();
		boolSel.setString(1, "bool0");
		final java.sql.ResultSet brs2 = boolSel.executeQuery();
		if (brs2.next())
		{
			final boolean retrieved = brs2.getBoolean(1);
			check("setBoolean-false-roundtrip", retrieved == false,
					"setBoolean(false) → getBoolean() returned " + retrieved + " — round-trip inconsistent (set stores 1, get treats 1 as true)");
		}
		else
			check("setBoolean-false-roundtrip", false, "no row returned for bool0");
		brs2.close();
		boolSel.close();
		boolIns.close();

		expectEx("prepared-bad-index", () ->
		{
			final java.sql.PreparedStatement ps = c.prepareStatement("INSERT INTO " + tbl + " VALUES (?, ?, ?, ?)");
			ps.setObject(99, "bad");
		}, "Invalid index");

		expectEx("prepared-execQuery-on-update", () ->
		{
			final java.sql.PreparedStatement ps = c.prepareStatement("UPDATE " + tbl + " SET AGE = 1");
			ps.executeQuery();
		}, "Not a query");

		expectEx("prepared-execUpdate-on-select", () ->
		{
			final java.sql.PreparedStatement ps = c.prepareStatement("SELECT * FROM " + tbl);
			ps.executeUpdate();
		}, "Not a update");

		expectEx("prepared-unsupported-command", () ->
				c.prepareStatement("DROP TABLE " + tbl), "unimplemented command");

		insStmt.close();
		selStmt.close();
		updStmt.close();
		delStmt.close();
	}

	private static void testPersistence() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final String tbl = nextTableName();

		{
			final java.sql.Connection c = connect(dir);
			final java.sql.Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE " + tbl + " (USERID STRING KEY, VAL INTEGER NULL, TS LONG NULL)");
			st.executeUpdate("INSERT INTO " + tbl + " VALUES ('persist1', 111, 1001)");
			st.executeUpdate("INSERT INTO " + tbl + " VALUES ('persist2', 222, 1002)");
			st.executeUpdate("INSERT INTO " + tbl + " VALUES ('persist3', 333, 1003)");
			st.close();
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir);
			check("persist-reopen-count", countRows(c, tbl) == 3,
					"after reopen, expected 3 rows, got " + countRows(c, tbl));

			final java.sql.Statement st = c.createStatement();
			final java.sql.ResultSet rs = st.executeQuery("SELECT * FROM " + tbl + " WHERE USERID = 'persist1'");
			if (rs.next())
				check("persist-reopen-data", rs.getInt(2) == 111,
						"persist1 VAL should be 111, got " + rs.getInt(2));
			else
				check("persist-reopen-data", false, "no row for persist1 after reopen");
			rs.close();
			st.close();
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir);
			final java.sql.Statement st = c.createStatement();
			st.executeUpdate("UPDATE " + tbl + " SET VAL = 999 WHERE USERID = 'persist1'");
			st.executeUpdate("DELETE FROM " + tbl + " WHERE USERID = 'persist2'");
			st.close();
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir);
			check("persist-after-update-count", countRows(c, tbl) == 2,
					"after update+delete+reopen, expected 2 rows, got " + countRows(c, tbl));
			final String val = querySingle(c, "SELECT VAL FROM " + tbl + " WHERE USERID = 'persist1'", 1);
			checkEq("persist-after-update-data", "999", val);
			c.close();
		}

		final String vtbl = nextTableName();
		{
			final java.sql.Connection c = connect(dir);
			final java.sql.Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE " + vtbl + " (USERID STRING KEY, VAL INTEGER NULL)");
			for (int i = 0; i < 100; i++)
				st.executeUpdate("INSERT INTO " + vtbl + " VALUES ('v" + i + "', " + i + ")");
			for (int i = 0; i < 20; i++)
				st.executeUpdate("DELETE FROM " + vtbl + " WHERE USERID = 'v" + i + "'");
			check("vacuum-before-close", countRows(c, vtbl) == 80,
					"after delete 20 of 100, expected 80 rows, got " + countRows(c, vtbl));
			st.close();
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir);
			check("vacuum-after-reopen", countRows(c, vtbl) == 80,
					"after vacuum+reopen, expected 80 rows, got " + countRows(c, vtbl));
			final String val50 = querySingle(c, "SELECT VAL FROM " + vtbl + " WHERE USERID = 'v50'", 1);
			checkEq("vacuum-data-intact", "50", val50);
			c.close();
		}
	}

	private static void testAlter() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final java.sql.Connection c = connect(dir);
		final java.sql.Statement st = c.createStatement();
		final String tbl = nextTableName();

		st.executeUpdate("CREATE TABLE " + tbl + " (USERID STRING KEY, NAME STRING NULL, AGE INTEGER NULL)");
		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('a1','Alice',30)");

		st.executeUpdate("ALTER TABLE " + tbl + " ADD COLUMN EMAIL STRING NULL");
		final String email = querySingle(c, "SELECT EMAIL FROM " + tbl + " WHERE USERID = 'a1'", 1);
		check("alter-add-column", email == null || email.length() == 0,
				"new EMAIL column should be null or empty, got [" + email + "]");

		st.executeUpdate("UPDATE " + tbl + " SET EMAIL = 'alice@test.com' WHERE USERID = 'a1'");
		final String emailAfter = querySingle(c, "SELECT EMAIL FROM " + tbl + " WHERE USERID = 'a1'", 1);
		checkEq("alter-add-column-data", "alice@test.com", emailAfter);

		st.executeUpdate("ALTER TABLE " + tbl + " DROP COLUMN EMAIL");
		expectEx("alter-drop-column-gone", () ->
				st.executeQuery("SELECT EMAIL FROM " + tbl), "unknown column");

		expectEx("alter-add-dup-column", () ->
				st.executeUpdate("ALTER TABLE " + tbl + " ADD COLUMN NAME STRING NULL"), "already exists");

		expectEx("alter-unknown-table", () ->
				st.executeUpdate("ALTER TABLE NOTABLE ADD COLUMN X INTEGER"), "unknown table");

		st.executeUpdate("ALTER TABLE " + tbl + " ADD PRIMARY KEY (NAME)");
		rs_orderby:
		{
			final java.sql.ResultSet rs = st.executeQuery("SELECT * FROM " + tbl + " ORDER BY NAME");
			check("alter-add-pk-orderby", rs != null, "ORDER BY NAME should work after ADD PRIMARY KEY");
			if (rs != null) rs.close();
		}

		st.executeUpdate("ALTER TABLE " + tbl + " ADD INDEX (AGE)");
		{
			final java.sql.ResultSet rs = st.executeQuery("SELECT * FROM " + tbl + " ORDER BY AGE");
			check("alter-add-index-orderby", rs != null, "ORDER BY AGE should work after ADD INDEX");
			if (rs != null) rs.close();
		}

		st.executeUpdate("ALTER TABLE " + tbl + " DROP INDEX AGE");
		expectEx("alter-drop-index-orderby-fails", () ->
				st.executeQuery("SELECT * FROM " + tbl + " ORDER BY AGE"), "non-indexed");

		st.executeUpdate("ALTER TABLE " + tbl + " MODIFY COLUMN AGE LONG NULL");
		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('a2','Bob',9999999999)");
		final String bigVal = querySingle(c, "SELECT AGE FROM " + tbl + " WHERE USERID = 'a2'", 1);
		checkEq("alter-modify-column-type", "9999999999", bigVal);

		st.close();
	}

	private static void testDrop() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final java.sql.Connection c = connect(dir);
		final java.sql.Statement st = c.createStatement();
		final String tbl = nextTableName();

		st.executeUpdate("CREATE TABLE " + tbl + " (USERID STRING KEY, VAL INTEGER NULL)");
		st.executeUpdate("INSERT INTO " + tbl + " VALUES ('d1', 1)");

		final File dataFile = new File(dir, "fakedb.data." + tbl);
		check("drop-datafile-exists-before", dataFile.exists(),
				"data file should exist before DROP");

		st.executeUpdate("DROP TABLE " + tbl);
		check("drop-success", true, "DROP TABLE should not throw");

		expectEx("drop-select-after", () ->
				st.executeQuery("SELECT * FROM " + tbl), "unknown table");

		check("drop-datafile-gone", !dataFile.exists(),
				"data file should be deleted after DROP");

		expectEx("drop-nonexistent", () ->
				st.executeUpdate("DROP TABLE NOTABLE"), "doesn't exist");

		expectEx("drop-no-table-keyword", () ->
				st.executeUpdate("DROP " + tbl), "no table token");

		st.close();
	}

	private static void testTypes() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final java.sql.Connection c = connect(dir);
		final java.sql.Statement st = c.createStatement();
		final String tbl = nextTableName();

		st.executeUpdate("CREATE TABLE " + tbl + " (K STRING KEY, V STRING NULL, I INTEGER NULL, L LONG NULL, D DATETIME NULL)");
		st.close();

		final java.sql.PreparedStatement ps = c.prepareStatement("INSERT INTO " + tbl + " VALUES (?, ?, ?, ?, ?)");

		ps.setString(1, "backslash");
		ps.setString(2, "a\\b");
		ps.setInt(3, 0);
		ps.setLong(4, 0L);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-backslash", "a\\b", querySingle(c, "SELECT V FROM " + tbl + " WHERE K = 'backslash'", 1));

		ps.setString(1, "newline");
		ps.setString(2, "line1\nline2");
		ps.setInt(3, 0);
		ps.setLong(4, 0L);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-newline", "line1\nline2", querySingle(c, "SELECT V FROM " + tbl + " WHERE K = 'newline'", 1));

		ps.setString(1, "unicode");
		ps.setString(2, "price \u20AC 100");
		ps.setInt(3, 0);
		ps.setLong(4, 0L);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-unicode", "price \u20AC 100", querySingle(c, "SELECT V FROM " + tbl + " WHERE K = 'unicode'", 1));

		ps.setString(1, "intmax");
		ps.setString(2, "");
		ps.setInt(3, Integer.MAX_VALUE);
		ps.setLong(4, 0L);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-int-max", String.valueOf(Integer.MAX_VALUE),
				querySingle(c, "SELECT I FROM " + tbl + " WHERE K = 'intmax'", 1));

		ps.setString(1, "intmin");
		ps.setString(2, "");
		ps.setInt(3, Integer.MIN_VALUE);
		ps.setLong(4, 0L);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-int-min", String.valueOf(Integer.MIN_VALUE),
				querySingle(c, "SELECT I FROM " + tbl + " WHERE K = 'intmin'", 1));

		ps.setString(1, "longmax");
		ps.setString(2, "");
		ps.setInt(3, 0);
		ps.setLong(4, Long.MAX_VALUE);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-long-max", String.valueOf(Long.MAX_VALUE),
				querySingle(c, "SELECT L FROM " + tbl + " WHERE K = 'longmax'", 1));

		final long ts = 1696114800000L;
		ps.setString(1, "datetime");
		ps.setString(2, "");
		ps.setInt(3, 0);
		ps.setLong(4, 0L);
		ps.setLong(5, ts);
		ps.execute();
		{
			final java.sql.Statement st2 = c.createStatement();
			final java.sql.ResultSet rs = st2.executeQuery("SELECT D FROM " + tbl + " WHERE K = 'datetime'");
			if (rs.next())
			{
				final long retrieved = rs.getLong(1);
				check("type-datetime-long", retrieved == ts,
						"DATETIME getLong returned " + retrieved + " expected " + ts);
				final Timestamp tstamp = rs.getTimestamp(1);
				check("type-datetime-timestamp", tstamp != null && tstamp.getTime() == ts,
						"DATETIME getTimestamp returned " + tstamp);
			}
			else
				check("type-datetime-long", false, "no row for datetime");
			rs.close();
			st2.close();
		}

		ps.setString(1, "nulls");
		ps.setNull(2, Types.VARCHAR);
		ps.setNull(3, Types.INTEGER);
		ps.setNull(4, Types.BIGINT);
		ps.setNull(5, Types.TIMESTAMP);
		ps.execute();
		{
			final java.sql.Statement st2 = c.createStatement();
			final java.sql.ResultSet rs = st2.executeQuery("SELECT V, I, L, D FROM " + tbl + " WHERE K = 'nulls'");
			if (rs.next())
			{
				check("type-null-string", rs.getString(1) == null && rs.wasNull(),
						"null STRING should return null and wasNull=true");
				rs.getInt(2);
				check("type-null-int-wasnull", rs.wasNull(), "null INTEGER should set wasNull=true");
				rs.getLong(3);
				check("type-null-long-wasnull", rs.wasNull(), "null LONG should set wasNull=true");
			}
			else
				check("type-null-string", false, "no row for nulls");
			rs.close();
			st2.close();
		}

		ps.setString(1, "emptystr");
		ps.setString(2, "");
		ps.setInt(3, 0);
		ps.setLong(4, 0L);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-empty-string", "", querySingle(c, "SELECT V FROM " + tbl + " WHERE K = 'emptystr'", 1));

		ps.setString(1, "mixed");
		ps.setString(2, "a\\b\nc\\d");
		ps.setInt(3, 0);
		ps.setLong(4, 0L);
		ps.setLong(5, 0L);
		ps.execute();
		checkEq("type-mixed-encoding", "a\\b\nc\\d", querySingle(c, "SELECT V FROM " + tbl + " WHERE K = 'mixed'", 1));

		ps.close();
	}

	private static void testCorruptionResilience() throws SQLException, IOException
	{
		final File dir = createTempDB();
		final String tbl = nextTableName();

		{
			final java.sql.Connection c = connect(dir);
			final java.sql.Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE " + tbl + " (K STRING KEY, V STRING NULL, I INTEGER NULL)");
			st.executeUpdate("INSERT INTO " + tbl + " VALUES ('k1', 'hello', 42)");
			st.executeUpdate("INSERT INTO " + tbl + " VALUES ('k2', 'world', 43)");
			st.close();
			c.close();
		}

		final java.sql.Connection c = connect(dir);
		try
		{
			final java.sql.ResultSet warmup = c.createStatement().executeQuery("SELECT * FROM " + tbl);
			while (warmup.next())
			{
			}
			warmup.close();

			final File dataFile = new File(dir, "fakedb.data." + tbl);
			final RandomAccessFile raf = new RandomAccessFile(dataFile, "rw");
			final byte[] garbage = new byte[(int) raf.length()];
			Arrays.fill(garbage, (byte) 'A');
			raf.seek(0);
			raf.write(garbage);
			raf.close();

			final java.sql.ResultSet rs = c.createStatement().executeQuery("SELECT * FROM " + tbl);
			int rows = 0;
			while (rs.next())
				rows++;
			rs.close();
			check("corruption-resilience-no-aioobe", true,
					"getRecord should not crash on corrupt data");
			check("corruption-resilience-no-rows", rows == 0,
					"corrupt records should be skipped, got " + rows + " rows back (not a crash)");
		}
		finally
		{
			c.close();
		}
	}

	private static void testConcurrency() throws SQLException, IOException, InterruptedException
	{
		{
			final File dir = createTempDB();
			final java.sql.Connection setupC = connect(dir);
			final java.sql.Statement setupSt = setupC.createStatement();
			final String tbl = nextTableName();
			setupSt.executeUpdate("CREATE TABLE " + tbl + " (K STRING KEY, V INTEGER NULL)");
			setupSt.close();
			setupC.close();

			final int numThreads = 8;
			final int rowsPerThread = 50;
			final String sharedTbl = tbl;
			final File sharedDir = dir;
			final AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
			final CountDownLatch latch = new CountDownLatch(numThreads);
			final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

			for (int t = 0; t < numThreads; t++)
			{
				final int threadIdx = t;
				pool.submit(new Runnable()
				{
					@Override
					public void run()
					{
						java.sql.Connection tc = null;
						try
						{
							tc = connect(sharedDir);
							final java.sql.Statement st = tc.createStatement();
							for (int i = 0; i < rowsPerThread; i++)
							{
								final String key = "t" + threadIdx + "-r" + i;
								st.executeUpdate("INSERT INTO " + sharedTbl + " VALUES ('" + key + "', " + i + ")");
							}
							for (int i = 0; i < 10; i++)
							{
								final java.sql.ResultSet rs = st.executeQuery(
										"SELECT * FROM " + sharedTbl + " WHERE K = 't" + threadIdx + "-r" + i + "'");
								rs.close();
							}
							st.close();
						}
						catch (final Throwable e)
						{
							error.compareAndSet(null, e);
						}
						finally
						{
							if (tc != null)
								try { tc.close(); } catch (final SQLException e) { }
							latch.countDown();
						}
					}
				});
			}

			latch.await(60, TimeUnit.SECONDS);
			pool.shutdown();

			if (error.get() != null)
			{
				System.out.println("[FAIL] concurrency-shared-stress: " + error.get().getClass().getSimpleName()
						+ ": " + error.get().getMessage());
				failed++;
			}
			else
			{
				final java.sql.Connection vc = connect(sharedDir);
				final int totalExpected = numThreads * rowsPerThread;
				final int actual = countRows(vc, sharedTbl);
				check("concurrency-shared-stress", actual == totalExpected,
						"expected " + totalExpected + " rows after concurrent inserts, got " + actual);
				vc.close();
			}
		}

		{
			final File dir = createTempDB();
			final java.sql.Connection setupC = connect(dir);
			final java.sql.Statement setupSt = setupC.createStatement();
			final String tbl = nextTableName();
			setupSt.executeUpdate("CREATE TABLE " + tbl + " (K STRING KEY, V INTEGER NULL)");
			setupSt.close();
			setupC.close();

			final AtomicReference<Throwable> readerError = new AtomicReference<Throwable>(null);
			final CountDownLatch writerDone = new CountDownLatch(1);
			final CountDownLatch readersDone = new CountDownLatch(4);
			final ExecutorService pool = Executors.newFixedThreadPool(5);

			pool.submit(new Runnable()
			{
				@Override
				public void run()
				{
					java.sql.Connection wc = null;
					try
					{
						wc = connect(dir);
						final java.sql.Statement st = wc.createStatement();
						for (int i = 0; i < 100; i++)
							st.executeUpdate("INSERT INTO " + tbl + " VALUES ('w" + i + "', " + i + ")");
						st.close();
					}
					catch (final Throwable e)
					{
						readerError.compareAndSet(null, e);
					}
					finally
					{
						if (wc != null)
							try { wc.close(); } catch (final SQLException e) { }
						writerDone.countDown();
					}
				}
			});

			for (int r = 0; r < 4; r++)
			{
				pool.submit(new Runnable()
				{
					@Override
					public void run()
					{
						java.sql.Connection rc = null;
						try
						{
							rc = connect(dir);
							while (writerDone.getCount() > 0)
							{
								try
								{
									final java.sql.Statement st = rc.createStatement();
									final java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tbl);
									if (rs.next())
									{
										final int c = rs.getInt(1);
										if (c < 0 || c > 100)
										{
											readerError.compareAndSet(null,
													new RuntimeException("bad count: " + c));
										}
									}
									rs.close();
									st.close();
								}
								catch (final SQLException e)
								{
									readerError.compareAndSet(null, e);
									break;
								}
							}
						}
						catch (final SQLException e)
						{
							readerError.compareAndSet(null, e);
						}
						finally
						{
							if (rc != null)
								try { rc.close(); } catch (final SQLException e) { }
							readersDone.countDown();
						}
					}
				});
			}

			writerDone.await(60, TimeUnit.SECONDS);
			readersDone.await(60, TimeUnit.SECONDS);
			pool.shutdown();

			if (readerError.get() != null)
			{
				System.out.println("[FAIL] concurrency-readers-vs-writer: " + readerError.get().getClass().getSimpleName()
						+ ": " + readerError.get().getMessage());
				failed++;
			}
			else
			{
				final java.sql.Connection vc = connect(dir);
				check("concurrency-readers-vs-writer", countRows(vc, tbl) == 100,
						"after concurrent reads+writes, expected 100 rows, got " + countRows(vc, tbl));
				vc.close();
			}
		}

		{
			final int numPaths = 3;
			final int rowsPerPath = 50;
			final List<File> dirs = new ArrayList<File>();
			final List<String> tbls = new ArrayList<String>();
			for (int p = 0; p < numPaths; p++)
			{
				final File d = createTempDB();
				dirs.add(d);
				final String t = nextTableName();
				tbls.add(t);
				final java.sql.Connection sc = connect(d);
				final java.sql.Statement ss = sc.createStatement();
				ss.executeUpdate("CREATE TABLE " + t + " (K STRING KEY, V INTEGER NULL)");
				ss.close();
				sc.close();
			}

			final AtomicReference<Throwable> pathError = new AtomicReference<Throwable>(null);
			final CountDownLatch pathLatch = new CountDownLatch(numPaths);
			final ExecutorService pool = Executors.newFixedThreadPool(numPaths);

			for (int p = 0; p < numPaths; p++)
			{
				final int pIdx = p;
				pool.submit(new Runnable()
				{
					@Override
					public void run()
					{
						java.sql.Connection pc = null;
						try
						{
							pc = connect(dirs.get(pIdx));
							final java.sql.Statement st = pc.createStatement();
							for (int i = 0; i < rowsPerPath; i++)
								st.executeUpdate("INSERT INTO " + tbls.get(pIdx)
										+ " VALUES ('p" + pIdx + "-" + i + "', " + i + ")");
							final int cnt = countRows(pc, tbls.get(pIdx));
							if (cnt != rowsPerPath)
								pathError.compareAndSet(null, new RuntimeException(
										"path " + pIdx + " count=" + cnt + " expected " + rowsPerPath));
							st.close();
						}
						catch (final Throwable e)
						{
							pathError.compareAndSet(null, e);
						}
						finally
						{
							if (pc != null)
								try { pc.close(); } catch (final SQLException e) { }
							pathLatch.countDown();
						}
					}
				});
			}

			pathLatch.await(60, TimeUnit.SECONDS);
			pool.shutdown();

			if (pathError.get() != null)
			{
				System.out.println("[FAIL] concurrency-multi-path: " + pathError.get().getClass().getSimpleName()
						+ ": " + pathError.get().getMessage());
				failed++;
			}
			else
			{
				boolean allOk = true;
				for (int p = 0; p < numPaths; p++)
				{
					final java.sql.Connection vc = connect(dirs.get(p));
					final int cnt = countRows(vc, tbls.get(p));
					if (cnt != rowsPerPath)
						allOk = false;
					vc.close();
				}
				check("concurrency-multi-path", allOk,
						"multi-path: expected " + rowsPerPath + " rows in each of " + numPaths + " paths");
			}
		}
	}

	public static void main(final String[] args)
	{
		realErr = System.err;
		System.setErr(new PrintStream(new OutputStream()
		{
			@Override
			public void write(final int b)
			{
			}
		}));
		try
		{
			Class.forName("com.planet_ink.fakedb.Driver");
		}
		catch (final ClassNotFoundException e)
		{
			System.setErr(realErr);
			System.out.println("[FAIL] Driver class not found: " + e.getMessage());
			System.out.println("Tests passed: 0");
			return;
		}
		try
		{
			runPhase("Driver", TestFakedbDriver::testDriver);
			runPhase("Connection", TestFakedbDriver::testConnection);
			runPhase("CRUD", TestFakedbDriver::testCrud);
			runPhase("Prepared", TestFakedbDriver::testPrepared);
			runPhase("Persistence", TestFakedbDriver::testPersistence);
			runPhase("Alter", TestFakedbDriver::testAlter);
			runPhase("Drop", TestFakedbDriver::testDrop);
			runPhase("Types", TestFakedbDriver::testTypes);
			runPhase("CorruptionResilience", TestFakedbDriver::testCorruptionResilience);
			runPhase("Concurrency", TestFakedbDriver::testConcurrency);
		}
		finally
		{
			cleanup();
			System.setErr(realErr);
		}
		System.out.println("Tests passed: " + passed);
		if (failed > 0)
			System.out.println("Tests failed: " + failed);
	}
}
