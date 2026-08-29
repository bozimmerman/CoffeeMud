package com.planet_ink.fakedb.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.planet_ink.fakedb.backend.structure.FakeColumn;
import com.planet_ink.fakedb.backend.structure.FlatFileFS;

public class TestFakedbTable2
{
	private static int								passed			= 0;
	private static int								failed			= 0;
	private static final List<java.sql.Connection>	allConnections	= new ArrayList<java.sql.Connection>();
	private static final List<File>					allTempDirs		= new ArrayList<File>();

	// ---- independent restatement of the v2 fixed-width row contract ----
	private static final int	LONG_SIZE	= 20;
	private static final int	HEADER_SIZE	= 1024;

	private static String spaces(final int n)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++)
			sb.append(' ');
		return sb.toString();
	}

	private static String padRight(final int width, final String s)
	{
		return s + spaces(width - s.length());
	}

	private static String padLong(final long v)
	{
		final String sx = Long.toString(v);
		return sx + "\n" + spaces(LONG_SIZE - sx.length() - 1);
	}

	private static int valueWidth(final String colType, final int size)
	{
		return columnOf(colType, size).getStoreValueWidth();
	}

	private static class Col
	{
		@SuppressWarnings("unused")
		final String	name;
		final String	type;
		final int		size;
		final boolean	indexed;

		Col(final String n, final String t, final int s, final boolean i)
		{
			name = n;
			type = t;
			size = s;
			indexed = i;
		}
	}

	private static FakeColumn columnOf(final String colType, final int size)
	{
		final FakeColumn col = new FakeColumn();
		col.type = FakeColumn.FakeColType.valueOf(colType);
		col.size = (size >= 0) ? size : Integer.MAX_VALUE;
		col.name = "col";
		col.tableName = "test";
		return col;
	}

	private static String schemaHash(final String table, final int version, final List<String> colLines)
	{
		final java.util.zip.CRC32 crc = new java.util.zip.CRC32();
		crc.update((table + " " + version).getBytes());
		for (final String line : colLines)
			crc.update(line.trim().getBytes());
		return String.format("%08X", Long.valueOf(crc.getValue())).toUpperCase();
	}

	private static byte[] composeRow(final List<Col> cols, final int rowWidth, final String[] values)
	{
		final byte[] row = new byte[rowWidth];
		Arrays.fill(row, (byte) ' ');
		row[0] = (byte) '-';
		int pos = 1;
		for (final Col c : cols)
		{
			if (c.indexed)
			{
				System.arraycopy(padLong(0).getBytes(StandardCharsets.US_ASCII), 0, row, pos, LONG_SIZE);
				pos += LONG_SIZE;
				System.arraycopy(padLong(0).getBytes(StandardCharsets.US_ASCII), 0, row, pos, LONG_SIZE);
				pos += LONG_SIZE;
			}
		}
		for (int i = 0; i < cols.size(); i++)
		{
			final Col c = cols.get(i);
			final int w = valueWidth(c.type, c.size);
			final String v = values[i];
			row[pos + w - 1] = (byte) '\n';
			if (v != null)
			{
				if (c.type.equals("STRING"))
				{
					final byte[] cb = v.getBytes(StandardCharsets.US_ASCII);
					System.arraycopy(String.format("%03d", Integer.valueOf(cb.length)).getBytes(StandardCharsets.US_ASCII), 0, row, pos, 3);
					System.arraycopy(cb, 0, row, pos + 3, cb.length);
				}
				else if (c.type.equals("INTEGER") || c.type.equals("LONG") || c.type.equals("DATETIME"))
				{
					final byte[] nb = v.getBytes(StandardCharsets.US_ASCII);
					System.arraycopy(nb, 0, row, pos + w - 1 - nb.length, nb.length);
				}
				else if (c.type.equals("BLOB") || c.type.equals("CLOB"))
				{
					row[pos] = (byte) c.type.charAt(0);
					final String uuid = padRight(36, String.valueOf(i));
					System.arraycopy(uuid.getBytes(StandardCharsets.US_ASCII), 0, row, pos + 1, 36);
				}
			}
			pos += w;
		}
		return row;
	}

	private static File writeV2Database(final String table, final List<String> colLines, final List<Col> cols,
			final int rowWidth, final String[][] rows) throws IOException
	{
		final File dir = new File(System.getProperty("java.io.tmpdir"),
				"fakedb2test-" + System.nanoTime() + "-" + (int) (Math.random() * 100000));
		if (!dir.mkdirs())
			throw new IOException("Could not create temp dir " + dir);
		allTempDirs.add(dir);

		final PrintWriter schemaOut = new PrintWriter(new FileWriter(new File(dir, "fakedb.schema")));
		schemaOut.println(table + " V2");
		for (final String line : colLines)
			schemaOut.println(line);
		schemaOut.println();
		schemaOut.close();

		final byte[] hdr = new byte[HEADER_SIZE];
		Arrays.fill(hdr, (byte) ' ');
		System.arraycopy(padRight(LONG_SIZE, "V2H" + schemaHash(table, 2, colLines) + "\n")
				.getBytes(StandardCharsets.US_ASCII), 0, hdr, 0, LONG_SIZE);
		System.arraycopy(padLong(0).getBytes(StandardCharsets.US_ASCII), 0, hdr, LONG_SIZE, LONG_SIZE);
		int pos = LONG_SIZE * 2;
		for (int i = 0; i < cols.size(); i++)
		{
			System.arraycopy(padLong(0).getBytes(StandardCharsets.US_ASCII), 0, hdr, pos, LONG_SIZE);
			pos += LONG_SIZE;
		}

		final File dataFile = new File(dir, "fakedb.data." + table);
		final RandomAccessFile raf = new RandomAccessFile(dataFile, "rw");
		raf.write(hdr);
		for (final String[] row : rows)
			raf.write(composeRow(cols, rowWidth, row));
		raf.getFD().sync();
		raf.close();
		return dir;
	}

	private static File writeV2SchemaOnly(final String table, final List<String> colLines) throws IOException
	{
		final File dir = new File(System.getProperty("java.io.tmpdir"),
				"fakedb2test-" + System.nanoTime() + "-" + (int) (Math.random() * 100000));
		if (!dir.mkdirs())
			throw new IOException("Could not create temp dir " + dir);
		allTempDirs.add(dir);

		final PrintWriter schemaOut = new PrintWriter(new FileWriter(new File(dir, "fakedb.schema")));
		schemaOut.println(table + " V2");
		for (final String line : colLines)
			schemaOut.println(line);
		schemaOut.println();
		schemaOut.close();
		return dir;
	}

	private static java.sql.Connection connect(final File dir) throws SQLException
	{
		final java.sql.Connection c = DriverManager.getConnection("jdbc:fakedb:" + dir.getAbsolutePath());
		allConnections.add(c);
		return c;
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

	private static void runPhase(final String name, final ThrowingRunnable phase)
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

	private interface ThrowingRunnable
	{
		void run() throws Exception;
	}

	private static int countRows(final java.sql.Connection c, final String table) throws SQLException
	{
		final Statement st = c.createStatement();
		final ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
		rs.next();
		final int n = rs.getInt(1);
		rs.close();
		st.close();
		return n;
	}

	private static int countResults(final java.sql.Connection c, final String sql) throws SQLException
	{
		final Statement st = c.createStatement();
		final ResultSet rs = st.executeQuery(sql);
		int n = 0;
		while (rs.next())
			n++;
		rs.close();
		st.close();
		return n;
	}

	private static String querySingle(final java.sql.Connection c, final String sql, final int colIndex) throws SQLException
	{
		final Statement st = c.createStatement();
		final ResultSet rs = st.executeQuery(sql);
		String result = null;
		if (rs.next())
			result = rs.getString(colIndex);
		rs.close();
		st.close();
		return result;
	}

	private static char readMarker(final File dir, final String table, final long off) throws IOException
	{
		final RandomAccessFile raf = new RandomAccessFile(new File(dir, "fakedb.data." + table), "r");
		try
		{
			raf.seek(off);
			return (char) raf.read();
		}
		finally
		{
			raf.close();
		}
	}

	private static long readPaddedLong(final File dir, final String table, final long off) throws IOException
	{
		final RandomAccessFile raf = new RandomAccessFile(new File(dir, "fakedb.data." + table), "r");
		try
		{
			raf.seek(off);
			final byte[] buf = new byte[LONG_SIZE];
			raf.readFully(buf);
			final String s = new String(buf, StandardCharsets.US_ASCII).trim();
			if (s.length() == 0)
				return 0;
			return Long.parseLong(s);
		}
		finally
		{
			raf.close();
		}
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

	private static final List<Col> COLS = Arrays.asList(new Col("USERID", "STRING", 50, true),
			new Col("NAME", "STRING", 50, false), new Col("AGE", "INTEGER", 0, false),
			new Col("TS", "LONG", 0, false), new Col("BIO", "CLOB", 200, false));
	private static final List<String> COL_LINES = Arrays.asList("USERID STRING KEY 50", "NAME STRING NULL 50",
			"AGE INTEGER NULL", "TS LONG NULL", "BIO CLOB NULL 200");

	private static int rowWidth()
	{
		return 1 + LONG_SIZE * 2 + valueWidth("STRING", 50) + valueWidth("STRING", 50)
				+ valueWidth("INTEGER", 0) + valueWidth("LONG", 0) + valueWidth("CLOB", 200);
	}

	// Phase A: open-scan populates rowRecords, indexed + on-disk delete correctness.
	private static void testScanAndDelete() throws Exception
	{
		final String table = "T1";
		final int rw = rowWidth();
		final File dir = writeV2Database(table, COL_LINES, COLS, rw, new String[][] {
				{ "u1", "Alice", "30", "1000", null },
				{ "u2", "Bob", "25", "2000", null },
				{ "u3", "Charlie", "35", "3000", null },
				{ "u4", "Alicia", "28", "4000", null } });

		final java.sql.Connection c = connect(dir);
		check("scan-count-4", countRows(c, table) == 4, "expected 4 rows after open-scan, got " + countRows(c, table));
		checkEq("scan-read-name", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
		checkEq("scan-read-ts", "1000", querySingle(c, "SELECT TS FROM " + table + " WHERE USERID='u1'", 1));

		final Statement st = c.createStatement();
		st.executeUpdate("DELETE FROM " + table + " WHERE USERID='u2'");
		check("del-indexed-count", countRows(c, table) == 3, "after DELETE u2 expected 3, got " + countRows(c, table));
		final long u2off = HEADER_SIZE + (long) rw;
		check("del-indexed-marker", readMarker(dir, table, u2off) == '*', "u2 row must be '*' on disk");
		checkEq("del-indexed-head", Long.valueOf(u2off), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE)));
		checkEq("del-indexed-next0", Long.valueOf(0L), Long.valueOf(readPaddedLong(dir, table, u2off + 1)));

		st.executeUpdate("DELETE FROM " + table + " WHERE USERID='u3'");
		check("del-indexed-count2", countRows(c, table) == 2, "after DELETE u3 expected 2, got " + countRows(c, table));
		final long u3off = HEADER_SIZE + 2L * rw;
		checkEq("del-indexed-head2", Long.valueOf(u3off), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE)));
		checkEq("del-indexed-chain", Long.valueOf(u2off), Long.valueOf(readPaddedLong(dir, table, u3off + 1)));

		st.close();
		c.close();
	}

	// Phase B: reopen self-heals the header; non-indexed WHERE via getRecord; DELETE-all.
	private static void testReopenAndNonIndexedDelete() throws Exception
	{
		final String table = "T2";
		final int rw = rowWidth();
		final File dir = writeV2Database(table, COL_LINES, COLS, rw, new String[][] {
				{ "u1", "Alice", "30", "1000", null },
				{ "u2", "Bob", "25", "2000", null },
				{ "u3", "Charlie", "35", "3000", null },
				{ "u4", "Alicia", "28", "4000", null } });

		{
			final java.sql.Connection c = connect(dir);
			final Statement st = c.createStatement();
			st.executeUpdate("DELETE FROM " + table + " WHERE USERID='u2'");
			st.close();
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir); // reopen: header self-heal under test
			check("reopen-count-3", countRows(c, table) == 3, "expected 3 rows after reopen, got " + countRows(c, table));
			check("reopen-gone", countResults(c, "SELECT * FROM " + table + " WHERE USERID='u2'") == 0,
					"deleted u2 came back after reopen");
			checkEq("reopen-name", "Charlie", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u3'", 1));

			final Statement st = c.createStatement();
			st.executeUpdate("DELETE FROM " + table + " WHERE NAME='Alice'"); // non-indexed -> getRecord
			check("del-nonindexed-count", countRows(c, table) == 2, "after NAME delete expected 2, got " + countRows(c, table));
			check("del-nonindexed-marker", readMarker(dir, table, HEADER_SIZE) == '*', "u1 row must be '*'");

			st.executeUpdate("DELETE FROM " + table);
			check("del-all-count", countRows(c, table) == 0, "after DELETE all expected 0, got " + countRows(c, table));
			final long u4off = HEADER_SIZE + 3L * rw;
			checkEq("del-all-head", Long.valueOf(u4off), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE)));
			check("del-all-markers", (readMarker(dir, table, HEADER_SIZE) == '*')
					&& (readMarker(dir, table, HEADER_SIZE + 2L * rw) == '*'), "u1/u3 rows must be '*'");
			st.close();
			c.close();
		}
	}

	// Phase C: schema-only dir exercises FakeTable2.open()'s fresh-file init branch.
	private static void testFreshInitHeader() throws Exception
	{
		final String table = "T2";
		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final String initHeader = "V2H" + schemaHash(table, 2, COL_LINES);

		{
			final java.sql.Connection c = connect(dir); // open initializes the V2 header + free-list head
			check("empty-count", countRows(c, table) == 0, "expected 0 rows in fresh table " + table + ", got " + countRows(c, table));
			checkEq("empty-head", Long.valueOf(0L), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE)));
			final File dataFile = new File(dir, "fakedb.data." + table);
			check("header-len", dataFile.length() == HEADER_SIZE,
					"fresh data file must be padded to " + HEADER_SIZE + " bytes (" + initHeader + "), got " + dataFile.length());
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir); // reopen: fresh-init header must pass hash validation
			check("reopen-count", countRows(c, table) == 0,
					"expected 0 rows after reopen validating " + initHeader + ", got " + countRows(c, table));
			c.close();
		}
	}

	// Phase E: insertRecord — fresh schema, insert via JDBC, verify rows,
	// on-disk markers, free-list reuse, and persistence across reopen.
	private static void testInsertRecord() throws Exception
	{
		final String table = "T3";
		final int rw = rowWidth();
		final File dir = writeV2SchemaOnly(table, COL_LINES);

		{
			final java.sql.Connection c = connect(dir);
			final Statement st = c.createStatement();
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,null)");
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25,2000,null)");
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Charlie',35,3000,null)");

			check("ins-count-3", countRows(c, table) == 3, "expected 3 rows after insert, got " + countRows(c, table));
			checkEq("ins-read-name", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
			checkEq("ins-read-ts", "3000", querySingle(c, "SELECT TS FROM " + table + " WHERE USERID='u3'", 1));

			check("ins-marker0", readMarker(dir, table, HEADER_SIZE) == '-', "first row must be '-' on disk");
			check("ins-marker1", readMarker(dir, table, HEADER_SIZE + rw) == '-', "second row must be '-' on disk");
			checkEq("ins-head0", Long.valueOf(0L), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE)));

			// delete u2 (2nd row), then insert u4 — the freed slot must be reused.
			st.executeUpdate("DELETE FROM " + table + " WHERE USERID='u2'");
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u4','Dana',42,4000,null)");
			check("ins-count-after-reuse", countRows(c, table) == 3, "expected 3 after delete+insert, got " + countRows(c, table));
			checkEq("ins-reuse-head", Long.valueOf(0L), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE)));
			check("ins-reuse-marker", readMarker(dir, table, HEADER_SIZE + rw) == '-', "u4 must reuse the freed 2nd-row slot");
			checkEq("ins-reuse-read", "Dana", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u4'", 1));
			st.close();
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir); // reopen: rows + free-list head persist
			check("ins-reopen-count", countRows(c, table) == 3, "expected 3 after reopen, got " + countRows(c, table));
			checkEq("ins-reopen-u1", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
			checkEq("ins-reopen-u4", "Dana", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u4'", 1));
			checkEq("ins-reopen-head", Long.valueOf(0L), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE)));
			c.close();
		}
	}

	// Phase F: BLOB/CLOB content stored in the table's own FlatFileFS and
	// referenced from the row; full-content round-trip and persistence.
	private static void testBlobs() throws Exception
	{
		final String table = "T4";
		final StringBuilder bio = new StringBuilder("");
		for (int i = 0; i < 20; i++)
			bio.append("ABCDEFGHIJ");
		final String bioContent = bio.toString(); // 200 chars, > 36

		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,'" + bioContent + "')");

		check("blob-count", countRows(c, table) == 1, "expected 1 row with blob, got " + countRows(c, table));
		final String readBack = querySingle(c, "SELECT BIO FROM " + table + " WHERE USERID='u1'", 1);
		checkEq("blob-roundtrip", bioContent, readBack);
		check("blob-not-truncated", (readBack != null) && (readBack.length() == 200),
				"blob content was truncated to " + ((readBack == null) ? "null" : Integer.valueOf(readBack.length())));

		final java.sql.ResultSet rs = st.executeQuery("SELECT BIO FROM " + table + " WHERE USERID='u1'");
		check("blob-clob-next", rs.next(), "expected a row for getClob");
		final Clob clob = rs.getClob(1);
		checkEq("blob-clob-length", Long.valueOf(200L), Long.valueOf(clob.length()));
		checkEq("blob-clob-substring", bioContent, clob.getSubString(1, 200));
		rs.close();

		st.close();
		c.close();

		{
			final java.sql.Connection c2 = connect(dir); // reopen: content persists
			checkEq("blob-reopen", bioContent, querySingle(c2, "SELECT BIO FROM " + table + " WHERE USERID='u1'", 1));
			c2.close();
		}

		{
			final java.sql.Connection c2 = connect(dir); // delete frees the FlatFileFS entry
			final Statement st2 = c2.createStatement();
			st2.executeUpdate("DELETE FROM " + table + " WHERE USERID='u1'");
			check("blob-del-count", countRows(c2, table) == 0, "expected 0 rows after delete, got " + countRows(c2, table));
			st2.close();
			c2.close();
		}
		try (final FlatFileFS fs = new FlatFileFS(new File(dir, table + ".flatfs").getAbsolutePath()))
		{
			check("blob-freed", fs.listAllFiles().size() == 0, "blob store should be empty after delete");
		}
	}

	private static void testStoreValueWidths() throws Exception
	{
		check("width-integer", columnOf("INTEGER", 0).getStoreValueWidth() == 12, "expected 12");
		check("width-long", columnOf("LONG", 0).getStoreValueWidth() == 20, "expected 20");
		check("width-datetime", columnOf("DATETIME", 0).getStoreValueWidth() == 20, "expected 20");
		check("width-string", columnOf("STRING", 50).getStoreValueWidth() == 54, "expected 54");
		check("width-unknown", columnOf("UNKNOWN", 0).getStoreValueWidth() == 20, "expected 20");
		check("width-clob", columnOf("CLOB", 200).getStoreValueWidth() == 38, "expected 38");
		check("width-blob", columnOf("BLOB", 200).getStoreValueWidth() == 38, "expected 38");
		boolean threw = false;
		try
		{
			columnOf("STRING", -1).getStoreValueWidth();
		}
		catch (final IllegalArgumentException e)
		{
			threw = true;
		}
		check("width-string-nosize-throws", threw, "expected IllegalArgumentException");
	}

	public static void main(final String[] args)
	{

		try
		{
			Class.forName("com.planet_ink.fakedb.Driver");
		}
		catch (final ClassNotFoundException e)
		{
			System.out.println("[FAIL] Driver class not found: " + e.getMessage());
			System.out.println("Tests passed: 0");
			return;
		}
		try
		{
			runPhase("ScanAndDelete", TestFakedbTable2::testScanAndDelete);
			runPhase("ReopenAndNonIndexedDelete", TestFakedbTable2::testReopenAndNonIndexedDelete);
			runPhase("FreshInitHeader", TestFakedbTable2::testFreshInitHeader);
			runPhase("InsertRecord", TestFakedbTable2::testInsertRecord);
			runPhase("Blobs", TestFakedbTable2::testBlobs);
			runPhase("StoreValueWidths", TestFakedbTable2::testStoreValueWidths);
		}
		finally
		{
			cleanup();
		}
		System.out.println("Tests passed: " + passed);
		if (failed > 0)
			System.out.println("Tests failed: " + failed);
	}
}
