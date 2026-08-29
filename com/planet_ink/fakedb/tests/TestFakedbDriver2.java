package com.planet_ink.fakedb.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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

public class TestFakedbDriver2 extends TestFakedbDriver
{
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

	private static File writeEmptySchema() throws IOException
	{
		final File dir = new File(System.getProperty("java.io.tmpdir"),
				"fakedb2test-" + System.nanoTime() + "-" + (int) (Math.random() * 100000));
		if (!dir.mkdirs())
			throw new IOException("Could not create temp dir " + dir);
		allTempDirs.add(dir);
		if (!new File(dir, "fakedb.schema").createNewFile())
			throw new IOException("Could not create schema file");
		return dir;
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

	private static int readByte(final File dir, final String table, final long off) throws IOException
	{
		final RandomAccessFile raf = new RandomAccessFile(new File(dir, "fakedb.data." + table), "r");
		try
		{
			raf.seek(off);
			return raf.read();
		}
		finally
		{
			raf.close();
		}
	}

	private static String orderedIds(final java.sql.Connection c, final String sql) throws SQLException
	{
		final Statement st = c.createStatement();
		final ResultSet rs = st.executeQuery(sql);
		final StringBuilder sb = new StringBuilder();
		while (rs.next())
		{
			if (sb.length() > 0)
				sb.append(',');
			sb.append(rs.getString(1));
		}
		rs.close();
		st.close();
		return sb.toString();
	}

	private static boolean rowRecordsEmpty(final java.sql.Connection c, final String table)
	{
		try
		{
			final com.planet_ink.fakedb.backend.Backend backend = ((com.planet_ink.fakedb.backend.Connection) c).getBackend();
			final com.planet_ink.fakedb.backend.structure.FakeTable t = backend.getFakeTables().get(table);
			final java.lang.reflect.Field f = com.planet_ink.fakedb.backend.structure.FakeTable.class.getDeclaredField("rowRecords");
			f.setAccessible(true);
			final com.planet_ink.fakedb.backend.structure.IndexedRowMap map = (com.planet_ink.fakedb.backend.structure.IndexedRowMap) f.get(t);
			return !map.iterator(-1, false).hasNext();
		}
		catch (final Throwable t)
		{
			return false;
		}
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

	// Rewrites a v1-style column list into a v2 one by giving every STRING/CLOB/BLOB
	// column a default size, so the inherited v1 phases can run against v2 tables.
	private static String toV2ColumnDefs(final String columnDefs)
	{
		final String body = columnDefs.trim();
		final String inner = (body.startsWith("(") && body.endsWith(")"))
				? body.substring(1, body.length() - 1).trim() : body;
		final String[] cols = inner.split(",");
		final StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < cols.length; i++)
		{
			if (i > 0)
				sb.append(", ");
			final String[] parts = cols[i].trim().split("\\s+");
			if (parts.length < 2)
			{
				sb.append(cols[i].trim());
				continue;
			}
			sb.append(parts[0]).append(' ').append(parts[1]);
			final String typeUpper = parts[1].toUpperCase();
			if (typeUpper.equals("STRING") || typeUpper.equals("CLOB") || typeUpper.equals("BLOB"))
				sb.append(" (50)");
			for (int j = 2; j < parts.length; j++)
				sb.append(' ').append(parts[j]);
		}
		sb.append(')');
		return sb.toString();
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

	// Phase G: updateRecord — in-place update, key change + dup check, persistence.
	private static void testUpdateRecord() throws Exception
	{
		final String table = "T5";
		final File dir = writeV2SchemaOnly(table, COL_LINES);

		{
			final java.sql.Connection c = connect(dir);
			final Statement st = c.createStatement();
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,null)");
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25,2000,null)");
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Charlie',35,3000,null)");

			st.executeUpdate("UPDATE " + table + " SET NAME='Alicia' WHERE USERID='u1'");
			checkEq("upd-name", "Alicia", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
			check("upd-marker-inplace", readMarker(dir, table, HEADER_SIZE) == '-', "updated row must remain '-' on disk");
			check("upd-count", countRows(c, table) == 3, "expected 3 rows after update, got " + countRows(c, table));

			st.executeUpdate("UPDATE " + table + " SET USERID='u1x' WHERE USERID='u1'");
			checkEq("upd-key-new", "Alicia", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1x'", 1));
			check("upd-key-old-gone", countResults(c, "SELECT * FROM " + table + " WHERE USERID='u1'") == 0,
					"old key must be gone after key update");

			boolean dupThrew = false;
			try
			{
				st.executeUpdate("UPDATE " + table + " SET USERID='u2' WHERE USERID='u3'");
			}
			catch (final SQLException e)
			{
				dupThrew = (e.getMessage() != null) && (e.getMessage().indexOf("dup") >= 0);
			}
			check("upd-key-dup", dupThrew, "duplicate key update must throw");
			st.close();
			c.close();
		}

		{
			final java.sql.Connection c = connect(dir);
			checkEq("upd-reopen-name", "Alicia", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1x'", 1));
			checkEq("upd-reopen-u2", "Bob", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u2'", 1));
			check("upd-reopen-count", countRows(c, table) == 3, "expected 3 rows after reopen, got " + countRows(c, table));
			c.close();
		}
	}

	// Phase H: updateRecord on a CLOB column — new content round-trips and the old
	// FlatFileFS entry is freed, leaving exactly one entry behind.
	private static void testUpdateBlob() throws Exception
	{
		final String table = "T6";
		final StringBuilder bio = new StringBuilder("");
		for (int i = 0; i < 20; i++)
			bio.append("ABCDEFGHIJ");
		final String bio1 = bio.toString();
		final String bio2 = bio.toString().replace('A', 'Z');

		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,'" + bio1 + "')");

		st.executeUpdate("UPDATE " + table + " SET BIO='" + bio2 + "' WHERE USERID='u1'");
		final String readBack = querySingle(c, "SELECT BIO FROM " + table + " WHERE USERID='u1'", 1);
		checkEq("updblob-roundtrip", bio2, readBack);
		check("updblob-not-truncated", (readBack != null) && (readBack.length() == 200),
				"updated blob content was truncated");
		st.close();
		c.close();

		try (final FlatFileFS fs = new FlatFileFS(new File(dir, table + ".flatfs").getAbsolutePath()))
		{
			check("updblob-one-entry", fs.listAllFiles().size() == 1, "blob store must hold exactly 1 entry after update");
		}
	}

	// Phase H2: a no-op blob update must not re-store (reference unchanged); ALTER
	// DROP COLUMN on a blob column must free the blob.
	private static void testBlobNoopAndDropV2() throws Exception
	{
		{
			final String table = "T41";
			final File dir = writeV2SchemaOnly(table, COL_LINES);
			final java.sql.Connection c = connect(dir);
			final Statement st = c.createStatement();
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,'same-content')");
			final String refBefore;
			try (final FlatFileFS fs = new FlatFileFS(new File(dir, table + ".flatfs").getAbsolutePath()))
			{
				refBefore = fs.listAllFiles().get(0).getKey();
			}
			st.executeUpdate("UPDATE " + table + " SET BIO='same-content' WHERE USERID='u1'");
			final String refAfter;
			try (final FlatFileFS fs = new FlatFileFS(new File(dir, table + ".flatfs").getAbsolutePath()))
			{
				refAfter = fs.listAllFiles().get(0).getKey();
			}
			checkEq("blobnoop-content", "same-content", querySingle(c, "SELECT BIO FROM " + table + " WHERE USERID='u1'", 1));
			check("blobnoop-ref-unchanged", refBefore.equals(refAfter),
					"no-op blob update should not re-store (ref changed " + refBefore + " -> " + refAfter + ")");
			st.close();
			c.close();
		}

		{
			final String table = "T42";
			final File dir = writeEmptySchema();
			final java.sql.Connection c = connect(dir);
			final Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE " + table + " V2 (USERID STRING KEY (50), NAME STRING NULL (50), BIO CLOB NULL (200))");
			st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice','dropped-blob')");
			st.executeUpdate("ALTER TABLE " + table + " DROP COLUMN BIO");
			check("dropblob-count", countRows(c, table) == 1, "row count should be 1 after drop");
			st.close();
			c.close();
			try (final FlatFileFS fs = new FlatFileFS(new File(dir, table + ".flatfs").getAbsolutePath()))
			{
				check("dropblob-freed", fs.listAllFiles().size() == 0, "blob store should be empty after dropping blob column");
			}
		}
	}

	// Phase I: CREATE TABLE ... V2 — SQL DDL creates a v2 table (with sizes); CRUD works.
	private static void testCreateTableV2() throws Exception
	{
		final String table = "T7";
		final File dir = writeEmptySchema();
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("CREATE TABLE " + table + " V2 (USERID STRING KEY (50), NAME STRING NULL (50), AGE INTEGER NULL, TS LONG NULL, BIO CLOB NULL (200))");

		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,'hello-blob')");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25,2000,null)");

		check("createv2-count", countRows(c, table) == 2, "expected 2 rows, got " + countRows(c, table));
		checkEq("createv2-name", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
		checkEq("createv2-blob", "hello-blob", querySingle(c, "SELECT BIO FROM " + table + " WHERE USERID='u1'", 1));

		st.executeUpdate("UPDATE " + table + " SET AGE=31 WHERE USERID='u1'");
		checkEq("createv2-update", "31", querySingle(c, "SELECT AGE FROM " + table + " WHERE USERID='u1'", 1));
		st.close();
		c.close();

		{
			final java.sql.Connection c2 = connect(dir);
			checkEq("createv2-reopen", "Alice", querySingle(c2, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
			c2.close();
		}
	}

	// Phase J: ALTER TABLE on a v2 table — ADD/DROP COLUMN, ADD/DROP INDEX, and
	// MODIFY COLUMN rewrite the fixed-width data file with the new layout.
	private static void testAlterTableV2() throws Exception
	{
		final String table = "T8";
		final File dir = writeEmptySchema();
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("CREATE TABLE " + table + " V2 (USERID STRING KEY (50), NAME STRING NULL (50), AGE INTEGER NULL)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25)");

		st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN EMAIL STRING NULL (100)");
		check("alter2-count-after-add", countRows(c, table) == 2, "expected 2 rows after ADD COLUMN, got " + countRows(c, table));
		final String email = querySingle(c, "SELECT EMAIL FROM " + table + " WHERE USERID='u1'", 1);
		check("alter2-add-null", (email == null) || (email.length() == 0), "new EMAIL column should be null, got [" + email + "]");
		checkEq("alter2-add-preserved", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));

		st.executeUpdate("UPDATE " + table + " SET EMAIL='alice@test.com' WHERE USERID='u1'");
		checkEq("alter2-add-update", "alice@test.com", querySingle(c, "SELECT EMAIL FROM " + table + " WHERE USERID='u1'", 1));

		st.executeUpdate("ALTER TABLE " + table + " DROP COLUMN EMAIL");
		checkEq("alter2-drop-preserved", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
		check("alter2-drop-count", countRows(c, table) == 2, "expected 2 rows after DROP COLUMN, got " + countRows(c, table));

		st.executeUpdate("ALTER TABLE " + table + " ADD INDEX (AGE)");
		{
			final ResultSet rs = st.executeQuery("SELECT * FROM " + table + " ORDER BY AGE");
			check("alter2-add-index", rs != null, "ORDER BY AGE should work after ADD INDEX");
			if (rs != null)
				rs.close();
		}

		st.executeUpdate("ALTER TABLE " + table + " DROP INDEX AGE");
		boolean orderByThrew = false;
		try
		{
			st.executeQuery("SELECT * FROM " + table + " ORDER BY AGE");
		}
		catch (final SQLException e)
		{
			orderByThrew = true;
		}
		check("alter2-drop-index", orderByThrew, "ORDER BY AGE should fail after DROP INDEX");

		st.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN AGE LONG NULL");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Charlie',9999999999)");
		checkEq("alter2-modify-type", "9999999999", querySingle(c, "SELECT AGE FROM " + table + " WHERE USERID='u3'", 1));
		checkEq("alter2-modify-old", "30", querySingle(c, "SELECT AGE FROM " + table + " WHERE USERID='u1'", 1));

		st.close();
		c.close();

		{
			final java.sql.Connection c2 = connect(dir);
			checkEq("alter2-reopen-name", "Alice", querySingle(c2, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
			checkEq("alter2-reopen-age", "9999999999", querySingle(c2, "SELECT AGE FROM " + table + " WHERE USERID='u3'", 1));
			c2.close();
		}
	}

	// Phase K: MODIFY COLUMN that changes a STRING column's size — the full-file
	// rewrite must widen/shrink the fixed-width rows while preserving values.
	private static void testAlterStringResize() throws Exception
	{
		final String table = "T9";
		final File dir = writeEmptySchema();
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("CREATE TABLE " + table + " V2 (USERID STRING KEY (50), NAME STRING NULL (50), AGE INTEGER NULL)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30)");

		st.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN NAME STRING NULL (100)");
		checkEq("resize-widen-preserved", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));

		final StringBuilder longName = new StringBuilder("");
		for (int i = 0; i < 80; i++)
			longName.append('X');
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','" + longName + "',40)");
		checkEq("resize-widen-long", longName.toString(), querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u2'", 1));

		st.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN NAME STRING NULL (20)");
		checkEq("resize-shrink-preserved", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
		final String shrunkLong = querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u2'", 1);
		check("resize-shrink-truncated", (shrunkLong != null) && (shrunkLong.length() <= 20),
				"long name should truncate to 20, got length " + ((shrunkLong == null) ? "null" : Integer.valueOf(shrunkLong.length())));

		st.close();
		c.close();

		{
			final java.sql.Connection c2 = connect(dir);
			checkEq("resize-reopen", "Alice", querySingle(c2, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
			c2.close();
		}
	}

	// Phase L: DROP TABLE must delete the blob store (.flatfs) along with the
	// data file, so no blob data leaks after the table is gone.
	private static void testDropTableV2() throws Exception
	{
		final String table = "T10";
		final File dir = writeEmptySchema();
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("CREATE TABLE " + table + " V2 (USERID STRING KEY (50), NAME STRING NULL (50), BIO CLOB NULL (200))");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice','some-blob-content')");

		final File blobFile = new File(dir, table + ".flatfs");
		check("drop2-blob-exists-before", blobFile.exists(), "blob store should exist before DROP TABLE");

		st.executeUpdate("DROP TABLE " + table);
		check("drop2-blob-gone", !blobFile.exists(), "blob store (.flatfs) must be deleted on DROP TABLE");
		check("drop2-data-gone", !new File(dir, "fakedb.data." + table).exists(),
				"data file must be deleted on DROP TABLE");

		st.close();
		c.close();
	}

	// Phase M: open() builds the on-disk BST and flags the header 'I'; ORDER BY
	// walks the tree; no per-row in-memory map is populated.
	private static void testOnDiskIndex() throws Exception
	{
		final String table = "T11";
		final int rw = rowWidth();
		final File dir = writeV2Database(table, COL_LINES, COLS, rw, new String[][] {
				{ "u3", "Charlie", "35", "3000", null },
				{ "u1", "Alice", "30", "1000", null },
				{ "u2", "Bob", "25", "2000", null } });

		final java.sql.Connection c = connect(dir);
		check("idx-count-3", countRows(c, table) == 3, "expected 3 rows, got " + countRows(c, table));
		checkEq("idx-flag", Integer.valueOf((int) 'I'), Integer.valueOf(readByte(dir, table, 11)));
		final long u3off = HEADER_SIZE;
		final long u1off = HEADER_SIZE + rw;
		final long u2off = HEADER_SIZE + 2L * rw;
		// balanced build links the median key (u2) as root, u1 left, u3 right
		checkEq("idx-root", Long.valueOf(u2off), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE * 2)));
		checkEq("idx-u2-left", Long.valueOf(u1off), Long.valueOf(readPaddedLong(dir, table, u2off + 1)));
		checkEq("idx-u2-right", Long.valueOf(u3off), Long.valueOf(readPaddedLong(dir, table, u2off + 1 + LONG_SIZE)));
		check("idx-memory-free", rowRecordsEmpty(c, table), "rowRecords must be empty for disk-backed v2");

		checkEq("idx-orderby", "u1,u2,u3", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY USERID"));
		checkEq("idx-orderby-desc", "u3,u2,u1", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY USERID DESC"));
		c.close();
	}

	// Phase N: equality WHERE on the key column uses the on-disk index (point lookup);
	// single- and multi-condition WHERE both stay correct.
	private static void testPointLookupV2() throws Exception
	{
		final String table = "T12";
		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25,2000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Charlie',35,3000,null)");

		checkEq("lookup-name", "Bob", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u2'", 1));
		check("lookup-missing", countResults(c, "SELECT * FROM " + table + " WHERE USERID='u9'") == 0,
				"missing key must return 0 rows");
		checkEq("lookup-multi", "Bob", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u2' AND AGE=25", 1));
		st.close();
		c.close();
	}

	// Phase O: deleting an internal BST node re-links the tree; ORDER BY + lookups stay correct.
	private static void testDeleteRelinkV2() throws Exception
	{
		final String table = "T13";
		final int rw = rowWidth();
		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25,2000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Charlie',35,3000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u4','Dana',42,4000,null)");

		st.executeUpdate("DELETE FROM " + table + " WHERE USERID='u2'"); // internal node in a right chain
		checkEq("delrelink-orderby", "u1,u3,u4", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY USERID"));
		check("delrelink-gone", countResults(c, "SELECT * FROM " + table + " WHERE USERID='u2'") == 0, "u2 must be gone");
		checkEq("delrelink-u3", "Charlie", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u3'", 1));

		final long u1off = HEADER_SIZE;
		final long u3off = HEADER_SIZE + 2L * rw;
		checkEq("delrelink-root", Long.valueOf(u1off), Long.valueOf(readPaddedLong(dir, table, LONG_SIZE * 2)));
		checkEq("delrelink-u1-right", Long.valueOf(u3off), Long.valueOf(readPaddedLong(dir, table, u1off + 1 + LONG_SIZE)));
		st.close();
		c.close();
	}

	// Phase P: updating an indexed (key) value unlinks and re-links the node.
	private static void testUpdateRelinkV2() throws Exception
	{
		final String table = "T14";
		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25,2000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Charlie',35,3000,null)");

		st.executeUpdate("UPDATE " + table + " SET USERID='u1x' WHERE USERID='u1'");
		checkEq("updrelink-new", "Alice", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u1x'", 1));
		check("updrelink-old-gone", countResults(c, "SELECT * FROM " + table + " WHERE USERID='u1'") == 0, "old key must be gone");
		checkEq("updrelink-orderby", "u1x,u2,u3", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY USERID"));
		st.close();
		c.close();
	}

	// Phase Q: a non-key INDEX column supports duplicates; ORDER BY and equality lookups
	// must return all matching rows.
	private static void testNonUniqueIndexV2() throws Exception
	{
		final String table = "T15";
		final List<String> colLines = Arrays.asList("ID STRING KEY 50", "GRP STRING INDEX 50");
		final File dir = writeV2SchemaOnly(table, colLines);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('a','g2')");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('b','g1')");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('c','g2')");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('d','g1')");

		checkEq("nu-orderby-grp", "b,d,a,c", orderedIds(c, "SELECT ID FROM " + table + " ORDER BY GRP"));
		checkEq("nu-lookup-g2", "a,c", orderedIds(c, "SELECT ID FROM " + table + " WHERE GRP='g2'"));
		checkEq("nu-lookup-g1", "b,d", orderedIds(c, "SELECT ID FROM " + table + " WHERE GRP='g1'"));
		st.close();
		c.close();
	}

	// Phase R: a freed slot (reused with a lower key) is correctly re-inserted into the BST.
	// Phase R4: index-less v2 tables must reclaim deleted row slots via the free
	// list (previously gated on the presence of an indexed column).
	private static void testNonIndexedFreeListV2() throws Exception
	{
		final String table = "T39";
		final File dir = writeEmptySchema();
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("CREATE TABLE " + table + " V2 (VAL INTEGER NULL, NAME STRING NULL (50))");
		st.executeUpdate("INSERT INTO " + table + " VALUES (1,'one')");
		st.executeUpdate("INSERT INTO " + table + " VALUES (2,'two')");
		st.executeUpdate("INSERT INTO " + table + " VALUES (3,'three')");
		final File dataFile = new File(dir, "fakedb.data." + table);
		final long sizeAfterInsert = dataFile.length();

		st.executeUpdate("DELETE FROM " + table + " WHERE VAL=1");
		st.executeUpdate("DELETE FROM " + table + " WHERE VAL=2");
		st.executeUpdate("DELETE FROM " + table + " WHERE VAL=3");

		st.executeUpdate("INSERT INTO " + table + " VALUES (4,'four')");
		st.executeUpdate("INSERT INTO " + table + " VALUES (5,'five')");
		st.executeUpdate("INSERT INTO " + table + " VALUES (6,'six')");
		check("nonidx-reuse-size", dataFile.length() == sizeAfterInsert,
				"data file should not grow after delete+reinsert, was " + sizeAfterInsert + " now " + dataFile.length());
		check("nonidx-reuse-count", countRows(c, table) == 3, "expected 3 rows after reinsert");
		checkEq("nonidx-reuse-read", "four", querySingle(c, "SELECT NAME FROM " + table + " WHERE VAL=4", 1));
		st.close();
		c.close();
	}

	private static void testFreeListReuseIndexV2() throws Exception
	{
		final String table = "T16";
		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30,1000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Bob',25,2000,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Charlie',35,3000,null)");
		st.executeUpdate("DELETE FROM " + table + " WHERE USERID='u2'");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u0','Zero',1,0,null)");

		checkEq("reuse-orderby", "u0,u1,u3", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY USERID"));
		checkEq("reuse-lookup", "Zero", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='u0'", 1));
		st.close();
		c.close();
	}

	// Phase S: deleting a node with two children exercises successor replacement in
	// the on-disk BST delete.
	private static void testDeleteTwoChildrenV2() throws Exception
	{
		final String table = "T17";
		final File dir = writeV2SchemaOnly(table, COL_LINES);
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("INSERT INTO " + table + " VALUES ('b','Bee',2,200,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('a','Aye',1,100,null)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('c','Sea',3,300,null)");

		checkEq("twochild-pre-order", "a,b,c", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY USERID"));
		st.executeUpdate("DELETE FROM " + table + " WHERE USERID='b'"); // root with two children
		checkEq("twochild-post-order", "a,c", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY USERID"));
		checkEq("twochild-a", "Aye", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='a'", 1));
		checkEq("twochild-c", "Sea", querySingle(c, "SELECT NAME FROM " + table + " WHERE USERID='c'", 1));
		check("twochild-b-gone", countResults(c, "SELECT * FROM " + table + " WHERE USERID='b'") == 0, "b must be gone");
		st.close();
		c.close();
	}

	// Phase R2: ALTER TABLE ADD INDEX on a STRING column must preserve the column's
	// size (regression for the "Unable to compute new layout" bug that dropped the
	// size token from the schema line).
	private static void testAddIndexStringV2() throws Exception
	{
		final String table = "T20";
		final File dir = writeEmptySchema();
		final java.sql.Connection c = connect(dir);
		final Statement st = c.createStatement();
		st.executeUpdate("CREATE TABLE " + table + " V2 (USERID STRING KEY (50), NAME STRING NULL (50), AGE INTEGER NULL)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u1','Alice',30)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u2','Charlie',35)");
		st.executeUpdate("INSERT INTO " + table + " VALUES ('u3','Bob',25)");

		st.executeUpdate("ALTER TABLE " + table + " ADD INDEX (NAME)");
		check("addidxstr-count", countRows(c, table) == 3, "expected 3 rows after ADD INDEX, got " + countRows(c, table));
		checkEq("addidxstr-orderby", "u1,u3,u2", orderedIds(c, "SELECT USERID FROM " + table + " ORDER BY NAME"));
		checkEq("addidxstr-lookup", "Charlie", querySingle(c, "SELECT NAME FROM " + table + " WHERE NAME='Charlie'", 1));

		st.executeUpdate("ALTER TABLE " + table + " DROP INDEX NAME");
		boolean threw = false;
		try
		{
			st.executeQuery("SELECT * FROM " + table + " ORDER BY NAME");
		}
		catch (final SQLException e)
		{
			threw = true;
		}
		check("addidxstr-drop", threw, "ORDER BY NAME should fail after DROP INDEX");

		st.close();
		c.close();

		{
			final java.sql.Connection c2 = connect(dir);
			checkEq("addidxstr-reopen", "Alice", querySingle(c2, "SELECT NAME FROM " + table + " WHERE USERID='u1'", 1));
			c2.close();
		}
	}

	// Phase R3: a "#VERSION 2" directive in fakedb.schema (settable via the JDBC
	// "version" property or URL query) makes CREATE TABLE default to v2, with no
	// non-standard "V2" keyword needed in the SQL.
	private static String readSchemaFile(final File dir) throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		final java.io.BufferedReader in = new java.io.BufferedReader(new java.io.FileReader(new File(dir, "fakedb.schema")));
		try
		{
			String line;
			while ((line = in.readLine()) != null)
				sb.append(line).append('\n');
		}
		finally
		{
			in.close();
		}
		return sb.toString();
	}

	private static void testSchemaVersionFlag() throws Exception
	{
		{
			final File dir = createTempDB();
			final java.util.Properties props = new java.util.Properties();
			props.setProperty("version", "2");
			final java.sql.Connection c = DriverManager.getConnection("jdbc:fakedb:" + dir.getAbsolutePath(), props);
			final Statement st = c.createStatement();

			final String schema1 = readSchemaFile(dir);
			check("flag-written", schema1.contains("#VERSION 2"), "schema should contain #VERSION 2, got:\n" + schema1);

			st.executeUpdate("CREATE TABLE T30 (USERID STRING KEY (50), NAME STRING NULL (50), AGE INTEGER NULL)");
			st.executeUpdate("INSERT INTO T30 VALUES ('u1','Alice',30)");
			checkEq("flag-read", "Alice", querySingle(c, "SELECT NAME FROM T30 WHERE USERID='u1'", 1));

			final String schema2 = readSchemaFile(dir);
			check("flag-table-v2", schema2.contains("T30 V2"), "T30 should be V2 in schema, got:\n" + schema2);

			boolean sizeThrew = false;
			try
			{
				st.executeUpdate("CREATE TABLE T30BAD (NAME STRING NULL)");
			}
			catch (final SQLException e)
			{
				sizeThrew = true;
			}
			check("flag-v2-requires-size", sizeThrew, "un-sized STRING should fail under v2 default");

			st.close();
			c.close();
		}

		{
			// URL query form: jdbc:fakedb:<path>?version=2
			final File dir = createTempDB();
			final java.sql.Connection c = DriverManager.getConnection("jdbc:fakedb:" + dir.getAbsolutePath() + "?version=2");
			final Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE T31 (USERID STRING KEY (50), VAL INTEGER NULL)");
			st.executeUpdate("INSERT INTO T31 VALUES ('k1', 7)");
			checkEq("flag-url-read", "7", querySingle(c, "SELECT VAL FROM T31 WHERE USERID='k1'", 1));
			check("flag-url-v2", readSchemaFile(dir).contains("T31 V2"), "T31 should be V2 via URL ?version=2");
			st.close();
			c.close();
		}

		{
			// No flag: default stays v1 and un-sized STRING is allowed.
			final File dir = createTempDB();
			final java.sql.Connection c = connect(dir);
			final Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE T32 (NAME STRING NULL)");
			check("flag-absent-v1", readSchemaFile(dir).contains("T32 V1"), "T32 should default to V1 without a flag");
			st.close();
			c.close();
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

	// Phase S: a larger dataset (500 rows) survives close/reopen with the on-disk
	// index intact, and lexicographic key ordering is consistent (numeric strings,
	// prefix keys, and case all sort by string order).
	private static void testBigIndexV2() throws Exception
	{
		final String table = "T18";
		final File dir = writeEmptySchema();
		{
			final java.sql.Connection c = connect(dir);
			final Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE " + table + " V2 (USERID STRING KEY (50), VAL INTEGER NULL)");
			for (int i = 0; i < 500; i++)
				st.executeUpdate("INSERT INTO " + table + " VALUES ('u" + String.format("%03d", Integer.valueOf(i)) + "', " + i + ")");
			check("big-count", countRows(c, table) == 500, "expected 500 rows, got " + countRows(c, table));
			st.close();
			c.close();
		}
		{
			final java.sql.Connection c = connect(dir);
			check("big-reopen-count", countRows(c, table) == 500,
					"reopen expected 500 rows, got " + countRows(c, table));
			checkEq("big-reopen-lookup", "250", querySingle(c, "SELECT VAL FROM " + table + " WHERE USERID='u250'", 1));
			checkEq("big-reopen-first", "u000", querySingle(c, "SELECT USERID FROM " + table + " ORDER BY USERID", 1));
			checkEq("big-reopen-last", "u499", querySingle(c, "SELECT USERID FROM " + table + " ORDER BY USERID DESC", 1));
			check("big-reopen-order-count", countResults(c, "SELECT * FROM " + table + " ORDER BY USERID") == 500,
					"ORDER BY should return all 500 rows");
			c.close();
		}

		final String t2 = "T19";
		final File dir2 = writeEmptySchema();
		{
			final java.sql.Connection c = connect(dir2);
			final Statement st = c.createStatement();
			st.executeUpdate("CREATE TABLE " + t2 + " V2 (USERID STRING KEY (50), VAL INTEGER NULL)");
			for (final String k : new String[] { "10", "9", "2", "1", "a", "ab", "abc", "Alice", "alice", "ALICE" })
				st.executeUpdate("INSERT INTO " + t2 + " VALUES ('" + k + "', 0)");
			checkEq("order-lex", "1,10,2,9,ALICE,Alice,a,ab,abc,alice",
					orderedIds(c, "SELECT USERID FROM " + t2 + " ORDER BY USERID"));
			st.close();
			c.close();
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
			// The inherited v1 phases create tables through createTable; redirect
			// their DDL to v2 (with sizes) so they exercise the on-disk backend.
			createTable = (st, tbl, columnDefs) ->
				st.executeUpdate("CREATE TABLE " + tbl + " V2 " + toV2ColumnDefs(columnDefs));

			runPhase("ScanAndDelete", TestFakedbDriver2::testScanAndDelete);
			runPhase("ReopenAndNonIndexedDelete", TestFakedbDriver2::testReopenAndNonIndexedDelete);
			runPhase("FreshInitHeader", TestFakedbDriver2::testFreshInitHeader);
			runPhase("InsertRecord", TestFakedbDriver2::testInsertRecord);
			runPhase("Blobs", TestFakedbDriver2::testBlobs);
			runPhase("UpdateRecord", TestFakedbDriver2::testUpdateRecord);
			runPhase("UpdateBlob", TestFakedbDriver2::testUpdateBlob);
			runPhase("BlobNoopAndDropV2", TestFakedbDriver2::testBlobNoopAndDropV2);
			runPhase("CreateTableV2", TestFakedbDriver2::testCreateTableV2);
			runPhase("AlterTableV2", TestFakedbDriver2::testAlterTableV2);
			runPhase("AlterStringResize", TestFakedbDriver2::testAlterStringResize);
			runPhase("DropTableV2", TestFakedbDriver2::testDropTableV2);
			runPhase("OnDiskIndex", TestFakedbDriver2::testOnDiskIndex);
			runPhase("PointLookupV2", TestFakedbDriver2::testPointLookupV2);
			runPhase("DeleteRelinkV2", TestFakedbDriver2::testDeleteRelinkV2);
			runPhase("UpdateRelinkV2", TestFakedbDriver2::testUpdateRelinkV2);
			runPhase("NonUniqueIndexV2", TestFakedbDriver2::testNonUniqueIndexV2);
			runPhase("FreeListReuseIndexV2", TestFakedbDriver2::testFreeListReuseIndexV2);
			runPhase("NonIndexedFreeListV2", TestFakedbDriver2::testNonIndexedFreeListV2);
			runPhase("DeleteTwoChildrenV2", TestFakedbDriver2::testDeleteTwoChildrenV2);
			runPhase("AddIndexStringV2", TestFakedbDriver2::testAddIndexStringV2);
			runPhase("SchemaVersionFlag", TestFakedbDriver2::testSchemaVersionFlag);
			runPhase("StoreValueWidths", TestFakedbDriver2::testStoreValueWidths);
			runPhase("BigIndexV2", TestFakedbDriver2::testBigIndexV2);

			// v1 phases that also exercise the (shared) v2 SQL/DDL paths.
			runPhase("MetaDataV2", TestFakedbDriver2::testMetaData);
			runPhase("ConnectionLifecycleV2", TestFakedbDriver2::testConnectionLifecycle);
			runPhase("CrudV2", TestFakedbDriver2::testCrud);
			runPhase("PartialInsertV2", TestFakedbDriver2::testPartialInsert);
			runPhase("UpdateCountsV2", TestFakedbDriver2::testUpdateCounts);
			runPhase("LikeV2", TestFakedbDriver2::testLike);
			runPhase("JdbcEdgeCasesV2", TestFakedbDriver2::testJdbcEdgeCases);
			runPhase("StatementTypesV2", TestFakedbDriver2::testStatementTypes);
			runPhase("PreparedV2", TestFakedbDriver2::testPrepared);
			runPhase("PersistenceV2", TestFakedbDriver2::testPersistence);
			runPhase("TypesV2", TestFakedbDriver2::testTypes);
			runPhase("ConcurrencyV2", TestFakedbDriver2::testConcurrency);
			runPhase("PerformanceV2", TestFakedbDriver2::testPerformance);
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
