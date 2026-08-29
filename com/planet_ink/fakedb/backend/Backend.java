package com.planet_ink.fakedb.backend;

/*
   Copyright 2001 Thomas Neumann
   Copyright 2002-2026 Bo Zimmerman

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

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import com.planet_ink.fakedb.backend.jdbc.ResultSet;
import com.planet_ink.fakedb.backend.jdbc.Statement;
import com.planet_ink.fakedb.backend.statements.ImplAbstractStatement.StatementType;
import com.planet_ink.fakedb.backend.statements.ImplAlterStatement;
import com.planet_ink.fakedb.backend.statements.ImplCreateStatement;
import com.planet_ink.fakedb.backend.statements.ImplDeleteStatement;
import com.planet_ink.fakedb.backend.statements.ImplDropStatement;
import com.planet_ink.fakedb.backend.statements.ImplInsertStatement;
import com.planet_ink.fakedb.backend.statements.ImplSelectStatement;
import com.planet_ink.fakedb.backend.statements.ImplUpdateStatement;
import com.planet_ink.fakedb.backend.structure.ComparableValue;
import com.planet_ink.fakedb.backend.structure.FakeColumn;
import com.planet_ink.fakedb.backend.structure.FakeCondition;
import com.planet_ink.fakedb.backend.structure.FakeTable;
import com.planet_ink.fakedb.backend.structure.FakeTable2;
import com.planet_ink.fakedb.backend.structure.RecordInfo;
import com.planet_ink.fakedb.backend.structure.FakeColumn.FakeColType;

public class Backend
{
	File							basePath;
	private Map<String, FakeTable>	fakeTables	= new HashMap<String, FakeTable>();
	private int						defaultTableVersion	= 1;


	public Backend(final File basePath)
	{
		this.basePath = basePath;
	}
	/**
	*
	*/
	public void clearFakeTables()
	{
		basePath = null;
		if (fakeTables != null)
			for (final FakeTable R : fakeTables.values())
				R.close();
		fakeTables = new HashMap<String, FakeTable>();
	}

	/**
	 *
	 * @return
	 */
	public Map<String, FakeTable> getFakeTables()
	{
		return fakeTables;
	}

	/**
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum ConnectorType
	{
		AND, OR
	}

	/**
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public interface FakeConditionResponder
	{
		public void callBack(ComparableValue[] values, RecordInfo info) throws Exception;
	}

	/**
	 * Check for duplicate keys
	 * @param fakeTable the table
	 * @param columns the columns
	 * @param sqlValues the values
	 * @throws java.sql.SQLException
	 */
	public void dupKeyCheck(final String tableName, final String[] doCols, final String[] sqlValues) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table for dup check " + tableName);
		final List<FakeCondition> conditions = new ArrayList<FakeCondition>(2);
		for (int i = 0; i < doCols.length; i++)
		{
			final int id = fakeTable.findColumn(doCols[i]);
			if (id < 0)
				continue;
			final FakeColumn col = fakeTable.columns[id];
			if (col.keyNumber >= 0)
			{
				final FakeCondition condition = buildFakeCondition(fakeTable.name, col.name, "=", sqlValues[i], false);
				condition.connector = Backend.ConnectorType.AND;
				conditions.add(condition);
			}
		}
		if (conditions.size() == 0)
			return;
		final FakeConditionResponder responder = new FakeConditionResponder()
		{
			@Override
			public void callBack(final ComparableValue[] values, final RecordInfo info) throws Exception
			{
				throw new java.sql.SQLException("duplicate key error");
			}
		};
		try
		{
			fakeTable.recordIterator(conditions, responder);
		}
		catch (final SQLException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw new java.sql.SQLException(e.getMessage(),e);
		}
	}

	/**
	 * Read the schema from a file
	 * @param basePath The base path
	 * @throws IOException if it fails
	 */
	public void readSchema(final File basePath) throws SQLException
	{
		final List<List<String>> schema = readRawSchema();
		defaultTableVersion = readSchemaVersionFlag(schema);
		for(final List<String> group : schema)
		{
			if((group.size()==0)||(group.get(0).startsWith("#")))
				continue;
			final String s = group.remove(0).toUpperCase().trim();
			final int vx = s.lastIndexOf(' ');
			int version = 1;
			String tableName = s;
			if(vx > 0)
			{
				final String v = s.substring(vx+1);
				if((v.length()==2) && (v.startsWith("V"))&&(Character.isDigit(v.charAt(1))))
				{
					tableName = s.substring(0,vx).trim();
					version = Integer.valueOf(v.substring(1)).intValue();
				}
			}
			if (fakeTables.get(tableName) != null)
				throw new SQLException("Can not read schema: tableName is duplicate: " + tableName);

			final FakeTable fakeTable = new FakeTable2(tableName, new File(basePath, "fakedb.data." + tableName));
			fakeTable.version = version;
			fakeTable.initializeColumns(group);
			try
			{
				fakeTable.open();
			}
			catch(IOException x)
			{
				throw new SQLException("Unable to open table "+tableName, x);
			}
			fakeTables.put(tableName, fakeTable);
		}
	}

	/**
	 * Scan the raw schema groups for a "#VERSION n" directive line and return the
	 * requested default table version, or 1 if no directive is present.
	 * @param schema the raw schema groups
	 * @return the default table version (1 unless overridden)
	 */
	private int readSchemaVersionFlag(final List<List<String>> schema)
	{
		for (final List<String> group : schema)
		{
			for (final String line : group)
			{
				final String s = line.trim();
				if (s.toUpperCase().startsWith("#VERSION"))
				{
					final String[] parts = s.split("\\s+");
					if (parts.length > 1)
					{
						try
						{
							final int v = Integer.parseInt(parts[1]);
							if (v > 0)
								return v;
						}
						catch (final NumberFormatException e)
						{
						}
					}
				}
			}
		}
		return 1;
	}

	/**
	 * Persist the default table version into the schema file as a "#VERSION n"
	 * directive, and apply it to the in-memory backend.
	 * @param version the default table version to use for new tables
	 * @throws SQLException if the schema file cannot be read or written
	 */
	public synchronized void setDefaultTableVersion(final int version) throws SQLException
	{
		defaultTableVersion = version;
		final File schemaFile = new File(basePath, "fakedb.schema");
		final List<List<String>> schema;
		if (schemaFile.exists())
			schema = readRawSchema();
		else
			schema = new ArrayList<List<String>>();
		for (final List<String> group : schema)
		{
			final Iterator<String> it = group.iterator();
			while (it.hasNext())
			{
				if (it.next().trim().toUpperCase().startsWith("#VERSION"))
					it.remove();
			}
		}
		for (final Iterator<List<String>> it = schema.iterator(); it.hasNext();)
			if (it.next().isEmpty())
				it.remove();
		final List<String> flagGroup = new ArrayList<String>();
		flagGroup.add("#VERSION " + version);
		schema.add(0, flagGroup);
		rewriteRawSchema(schema);
	}

	/**
	 * Open the backend
	 * @return true if it worked
	 */
	public boolean open() throws IOException
	{
		for(final FakeTable tab : fakeTables.values())
			tab.close();
		fakeTables.clear();
		try
		{
			readSchema(basePath);
		}
		catch(SQLException e)
		{
			if(e.getCause() instanceof IOException)
				throw (IOException)e.getCause();
			throw new IOException(e.getMessage(),e);
		}
		return true;
	}

	/**
	 * Construct a scan
	 * @param stmt The select statement
	 * @return The result set
	 * @throws java.sql.SQLException
	 */
	public java.sql.ResultSet constructScan(final ImplSelectStatement stmt) throws java.sql.SQLException
	{
		final Statement s = stmt.s;
		final String tableName = stmt.tableName;
		final List<String> cols = stmt.cols;
		final List<FakeCondition> conditions = stmt.conditions;
		final String[] orderVars = stmt.orderVars;
		final String[] orderModifiers = stmt.orderModifiers;
		final FakeTable table = fakeTables.get(tableName);
		if (table == null)
			throw new java.sql.SQLException("unknown table for scan " + tableName);
		int[] showCols;
		boolean hasCountColumn = false;
		if ((cols.size() == 0) || (cols.contains("*")))
		{
			showCols = new int[table.numColumns()];
			for (int i = 0; i < showCols.length; i++)
				showCols[i] = i;
		}
		else
		{
			int index = 0;
			showCols = new int[cols.size()];
			for (final String col : cols)
			{
				if (col.toLowerCase().startsWith("count("))
				{
					showCols[index] = FakeColumn.INDEX_COUNT;
					hasCountColumn = true;
				}
				else
				{
					showCols[index] = table.findColumn(col);
					if (showCols[index] < 0)
					{
						try
						{
							Integer.parseInt(col);
							showCols[index] = FakeColumn.INDEX_COUNT;
							hasCountColumn = true;
						}
						catch (final Exception e)
						{
							throw new java.sql.SQLException("unknown column " + tableName + "." + col);
						}
					}
				}
				index++;
			}
		}

		int[] orderDexIndexes = null;
		if (orderVars != null)
		{
			orderDexIndexes = new int[orderVars.length];
			int d = 0;
			for (final String var : orderVars)
			{
				final int index = table.findColumn(var);
				int indexDex = -1;
				if (index < 0)
					throw new java.sql.SQLException("unknown column " + var);
				for (final int i : table.columnIndexesOfIndexed)
					if (i == index)
						indexDex = i;
				if (indexDex < 0)
					throw new java.sql.SQLException("unable to order by non-indexed " + var);
				orderDexIndexes[d] = indexDex;
				d++;
			}
		}

		java.util.Iterator<RecordInfo> lookupIter = null;
		if ((orderDexIndexes == null) && (!hasCountColumn) && (conditions.size() == 1))
		{
			final FakeCondition c0 = conditions.get(0);
			if ((c0.contains == null) && (c0.eq) && (!c0.not) && (!c0.like) && (!c0.lt) && (!c0.gt)
			&& (table.columns[c0.conditionIndex].indexNumber >= 0))
				lookupIter = table.indexLookupIterator(c0.conditionIndex, c0.conditionValue);
		}
		return new ResultSet(s, table, showCols, conditions, orderDexIndexes, orderModifiers, lookupIter);
	}

	/**
	 *
	 * @param stmt
	 * @throws java.sql.SQLException
	 */
	public void insertValues(final ImplInsertStatement stmt) throws java.sql.SQLException
	{
		final String tableName = stmt.tableName;
		final String[] columns = stmt.columns;
		final String[] sqlValues = stmt.sqlValues;

		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table for insert " + tableName);

		final ComparableValue[] values = new ComparableValue[fakeTable.columns.length];
		for (int index = 0; index < sqlValues.length; index++)
		{
			final int id;
			final String colName;
			if(index < columns.length)
			{
				colName = columns[index];
				id = fakeTable.findColumn(colName);
			}
			else
			if(index < fakeTable.columnNames.length)
			{
				colName = fakeTable.columnNames[index];
				id = fakeTable.findColumn(colName);
			}
			else
				throw new java.sql.SQLException("missing column for insert: " + (index+1));
			if (id < 0)
				throw new java.sql.SQLException("unknown column for insert " + colName);
			final FakeColumn col = fakeTable.columns[id];
			try
			{
				if ((sqlValues[index] == null) || (sqlValues[index].equals("null")))
					values[id] = new ComparableValue(null);
				else
				{
					switch (col.type)
					{
					case INTEGER:
						values[id] = new ComparableValue(Integer.valueOf(sqlValues[index]));
						break;
					case LONG:
					case DATETIME:
						values[id] = new ComparableValue(Long.valueOf(sqlValues[index]));
						break;
					default:
						values[id] = new ComparableValue(sqlValues[index]);
						break;
					}
				}
			}
			catch (final Exception e)
			{
				throw new java.sql.SQLException("illegal value '" + sqlValues[index] + "' for column " + col.name);
			}
		}
		final FakeTable2 table2 = (fakeTable instanceof FakeTable2) ? (FakeTable2) fakeTable : null;
		for (int id = 0; id < fakeTable.columns.length; id++)
		{
			if ((table2 != null) && fakeTable.columns[id].isBlobColumn())
			{
				final ComparableValue val = values[id];
				final Object o = (val == null) ? null : val.getValue();
				if (o != null)
					values[id] = new ComparableValue(table2.storeBlob(o.toString()));
			}
		}
		final ComparableValue[] keys = new ComparableValue[fakeTable.columnIndexesOfIndexed.length];
		for (int index = 0; index < fakeTable.columnIndexesOfIndexed.length; index++)
		{
			final int id = fakeTable.columnIndexesOfIndexed[index];
			if (values[id] == null)
				keys[index] = new ComparableValue(null);
			else
				keys[index] = new ComparableValue(values[id]);
		}
		final String[] strVals = new String[values.length];
		for (int x = 0; x < values.length; x++)
		{
			if (values[x] == null)
				strVals[x] = null;
			else
			{
				@SuppressWarnings("rawtypes")
				final Comparable val = values[x].getValue();
				strVals[x] = (val == null) ? null : val.toString();
			}
		}
		dupKeyCheck(tableName, fakeTable.columnNames, strVals);
		if (!fakeTable.insertRecord(null, keys, values))
			throw new java.sql.SQLException("unable to insert record");
	}


	/**
	 * Delete records in a table
	 * @param stmt The delete statement
	 * @throws java.sql.SQLException
	 */
	public int deleteRecord(final ImplDeleteStatement stmt) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(stmt.tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + stmt.tableName);

		final FakeTable2 table2 = (fakeTable instanceof FakeTable2) ? (FakeTable2) fakeTable : null;
		boolean hasBlob = false;
		if (table2 != null)
		{
			for (final FakeColumn col : fakeTable.columns)
				if (col.isBlobColumn())
				{
					hasBlob = true;
					break;
				}
		}
		if (hasBlob)
		{
			final List<String> blobRefs = new ArrayList<String>();
			try
			{
				final FakeConditionResponder responder = new FakeConditionResponder()
				{
					@Override
					public void callBack(final ComparableValue[] values, final RecordInfo info) throws Exception
					{
						for (int i = 0; i < fakeTable.columns.length; i++)
						{
							if (fakeTable.columns[i].isBlobColumn())
							{
								final ComparableValue val = values[i];
								final Object o = (val == null) ? null : val.getValue();
								if (o != null)
									blobRefs.add(o.toString());
							}
						}
					}
				};
				fakeTable.recordIterator(stmt.conditions, responder);
			}
			catch (final SQLException e)
			{
				throw e;
			}
			catch (final Exception e)
			{
				throw new java.sql.SQLException(e.getMessage(), e);
			}
			final int deleted = fakeTable.deleteRecord(stmt.conditions);
			for (final String ref : blobRefs)
				table2.freeBlob(ref);
			return deleted;
		}
		else
			return fakeTable.deleteRecord(stmt.conditions);
	}

	/**
	 * Update records in a table
	 * @param stmt The update statement
	 * @throws java.sql.SQLException
	 */
	public int updateRecord(final ImplUpdateStatement stmt) throws java.sql.SQLException
	{
		final String tableName = stmt.tableName;
		final List<FakeCondition> conditions = stmt.conditions;
		final String[] varNames = stmt.columns;
		final String[] sqlValues = stmt.sqlValues;

		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table for update" + tableName);

		final int[] vars = new int[varNames.length];
		for (int index = 0; index < vars.length; index++)
			if ((vars[index] = fakeTable.findColumn(varNames[index])) < 0)
				throw new java.sql.SQLException("unknown column " + varNames[index]);

		final ComparableValue[] values = new ComparableValue[fakeTable.columns.length];
		boolean doDupCheck = false;
		for (int index = 0; index < sqlValues.length; index++)
		{
			final FakeColumn col = fakeTable.columns[vars[index]];
			try
			{
				final ComparableValue newVal;
				if ((sqlValues[index] == null) || (sqlValues[index].equals("null")))
					newVal = new ComparableValue(null);
				else
				{
					switch (col.type)
					{
					case INTEGER:
						newVal = new ComparableValue(Integer.valueOf(sqlValues[index]));
						break;
					case LONG:
					case DATETIME:
						newVal = new ComparableValue(Long.valueOf(sqlValues[index]));
						break;
					default:
						newVal = new ComparableValue(sqlValues[index]);
						break;
					}
				}
				if(col.keyNumber>=0)
					doDupCheck = true;
				values[index] = newVal;
			}
			catch (final Exception e)
			{
				throw new java.sql.SQLException("illegal value '" + sqlValues[index] + "' for column " + col.name);
			}
		}
		return fakeTable.updateRecord(conditions, vars, values,this,doDupCheck?this.fakeTables.get(stmt.tableName):null);
	}

	/**
	 * Build a fake condition
	 * @param tableName Table name
	 * @param columnName Column name
	 * @param comparitor Comparitor string
	 * @param value Value string
	 * @return The fake condition
	 * @throws java.sql.SQLException
	 */
	public FakeCondition buildFakeCondition(final String tableName, final String columnName, final String comparitor, final String value, final boolean unPrepared) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table for faking " + tableName);
		final FakeCondition fake = new FakeCondition();
		fake.unPrepared = unPrepared;
		if (columnName == null)
		{
			fake.conditionIndex = 0;
			fake.conditionValue = new ComparableValue(null);
			return fake;
		}
		if ((fake.conditionIndex = fakeTable.findColumn(columnName)) < 0)
			throw new java.sql.SQLException("unknown column " + tableName + "." + columnName);
		final FakeColumn col = fakeTable.columns[fake.conditionIndex];
		if (col == null)
			throw new java.sql.SQLException("bad column " + tableName + "." + columnName);
		fake.colType = col.type;
		final boolean isLike = comparitor.equalsIgnoreCase("like");
		if (isLike && (col.type != FakeColType.STRING))
			throw new java.sql.SQLException("can't do like comparison on " + tableName + "." + columnName);
		if ((value == null) || value.equals("null") || unPrepared)
			fake.conditionValue = new ComparableValue(null);
		else
		{
			switch (col.type)
			{
			case INTEGER:
			{
				try
				{
					fake.conditionValue = new ComparableValue(Integer.valueOf(value));
				}
				catch (final Exception e)
				{
					throw new java.sql.SQLException("can't compare " + value + " to " + tableName + "." + columnName);
				}
				break;
			}
			case DATETIME:
			case LONG:
			case UNKNOWN:
			{
				try
				{
					fake.conditionValue = new ComparableValue(Long.valueOf(value));
				}
				catch (final Exception e)
				{
					throw new java.sql.SQLException("can't compare " + value + " to " + tableName + "." + columnName);
				}
				break;
			}
			default:
				fake.conditionValue = new ComparableValue(value);
				break;
			}
		}
		if (isLike)
			fake.like = true;
		else
		{
			for (final char c : comparitor.toCharArray())
			{
				switch (c)
				{
				case '!':
					fake.not = true;
					break;
				case '=':
					fake.eq = true;
					break;
				case '<':
					fake.lt = true;
					break;
				case '>':
					fake.gt = true;
					break;
				}
			}
		}
		if (fake.lt && fake.gt && (!fake.eq))
		{
			fake.lt = false;
			fake.gt = false;
			fake.not = !fake.not;
			fake.eq = true;
		}
		return fake;
	}

	public List<List<String>> readRawSchema() throws SQLException
	{
		final File schema = new File(basePath, "fakedb.schema");
		final List<List<String>> groups = new ArrayList<List<String>>();
		try(final BufferedReader in = new BufferedReader(new FileReader(schema)))
		{
			List<String> group = new ArrayList<String>();
			groups.add(group);
			String s = in.readLine();
			while (s != null)
			{
				s=s.trim();
				if(s.length()==0)
				{
					if(group.size()>0)
					{
						group = new ArrayList<String>();
						groups.add(group);
					}
				}
				else
				if(s.startsWith("#"))
				{
					if((group.size()>0)&&(!group.get(0).startsWith("#")))
					{
						group = new ArrayList<String>();
						groups.add(group);
					}
					group.add(s);
				}
				else
				{
					if((group.size()>0)&&(group.get(0).startsWith("#")))
					{
						group = new ArrayList<String>();
						groups.add(group);
					}
					group.add(s);
				}
				s = in.readLine();
			}
			if(group.size()==0)
				groups.remove(group);
		}
		catch(IOException e)
		{
			throw new SQLException("Error reading schema file.", e);
		}
		return groups;
	}

	public synchronized void rewriteRawSchema(final List<List<String>> groups) throws SQLException
	{
		if(!basePath.exists())
			basePath.mkdirs();
		final File schema = new File(basePath, "fakedb.schema");

		final StringBuilder str = new StringBuilder("");
		for (final List<String> group : groups)
		{
			for (final String s : group)
				str.append(s + "\n");
			str.append("\n");
		}
		try (final PrintWriter out = new PrintWriter(new FileWriter(schema)))
		{
			out.println(str.toString());
			out.flush();
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to write schema file");
		}
	}

	private List<String> findTableDef(final String tableName, final List<List<String>> schema) throws SQLException
	{
		for (final List<String> group : schema)
		{
			if ((group.size() > 0) && (!group.get(0).startsWith("#")))
			{
				final String s = group.get(0).toUpperCase().trim();
				final int vx = s.lastIndexOf(' ');
				String tName = s;
				if (vx > 0)
				{
					final String v = s.substring(vx + 1);
					if ((v.length() == 2) && (v.startsWith("V")) && (Character.isDigit(v.charAt(1))))
						tName = s.substring(0, vx).trim();
				}
				if (tName.equalsIgnoreCase(tableName))
					return group;
			}
		}
		throw new java.sql.SQLException("unknown table  for deffing" + tableName);
	}

	public String findColumnDef(final String columnName, final List<String> tableDef) throws SQLException
	{
		for (int i = 1; i < tableDef.size(); i++)
		{
			final String s = tableDef.get(i).toUpperCase().trim();
			final int vx = s.indexOf(' ');
			String cName = s;
			if (vx > 0)
				cName = s.substring(0, vx).trim();
			if (cName.equalsIgnoreCase(columnName))
				return tableDef.get(i);
		}
		return null;
	}

	/**
	 * Drop a table
	 *
	 * @param stmt The drop statement
	 */
	public void dropTable(final ImplDropStatement stmt) throws SQLException
	{
		final String tableName = stmt.tableName;
		if (fakeTables.get(tableName) == null)
			throw new java.sql.SQLException("table " + tableName + " doesn't exist");
		final FakeTable fakeTable = fakeTables.remove(tableName);
		final List<List<String>> schema = readRawSchema();
		final List<String> tableDef = findTableDef(tableName,schema);
		schema.remove(tableDef);
		this.rewriteRawSchema(schema);
		fakeTable.eraseDataFile();
		try
		{
			this.open();
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to re-open database after table drop: " + e.getMessage());
		}

	}

	/**
	 * Build a schema column-definition line for a column, version-aware:
	 * v2 tables append a bare numeric size for sized column types.
	 * @param col the column
	 * @param version the table storage version
	 * @return the schema line (no trailing newline)
	 */
	private static String columnDefLine(final FakeColumn col, final int version)
	{
		final StringBuilder colDef = new StringBuilder(col.name + " " + col.type.name().toLowerCase());
		if (col.keyNumber >= 0)
			colDef.append(" KEY");
		else
		if (col.canNull)
			colDef.append(" NULL");
		if ((version >= 2)
		&& ((col.type == FakeColType.STRING) || (col.type == FakeColType.BLOB) || (col.type == FakeColType.CLOB))
		&& (col.size != Integer.MAX_VALUE))
			colDef.append(" ").append(col.size);
		return colDef.toString();
	}

	/**
	 * Return the trailing numeric size token of a schema column-definition line,
	 * e.g. " 50" for "SVAL STRING NULL 50", or "" if the column has no size.
	 * @param colDef the column-definition line
	 * @return the size token (with leading space), or empty string
	 */
	private static String columnSizeToken(final String colDef)
	{
		final String[] parts = colDef.trim().split("\\s+");
		if (parts.length > 2)
		{
			final String last = parts[parts.length - 1];
			if ((last.length() > 0) && Character.isDigit(last.charAt(0)))
				return " " + last;
		}
		return "";
	}

	/**
	 * Alter a table
	 * @param stmt The alter statement
	 * @throws SQLException if it fails
	 */
	public synchronized void alterTable(final ImplAlterStatement stmt) throws SQLException
	{
		final StatementType action = stmt.getSubStatementType();
		final String objType = stmt.objType;
		final FakeColumn col = (FakeColumn)stmt.extValues()[1];
		final String tableName = stmt.tableName;
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table for altering " + tableName);
		final boolean v2 = fakeTable.version >= 2;
		final List<List<String>> schema = readRawSchema();
		final List<String> tableDef = findTableDef(tableName,schema);
		if(action == StatementType.CREATE)
		{
			if(objType.equals("COLUMN"))
			{
				if (findColumnDef(col.name, tableDef) != null)
					throw new java.sql.SQLException("column " + col.name + " already exists");
				if (v2 && (col.type == FakeColType.STRING) && (col.size == Integer.MAX_VALUE))
					throw new java.sql.SQLException("V2 column '" + col.name + "' requires a size, e.g. " + col.name + " STRING (50)");
				tableDef.add(columnDefLine(col, fakeTable.version));
				if (!v2)
					fakeTable.addColumn();
			}
			else
			if(objType.equals("PRIMARY"))
			{
				final String[] colNames = stmt.changes;
				for(final String colName : colNames)
				{
					String colDef = findColumnDef(colName, tableDef);
					if (colDef == null)
						throw new java.sql.SQLException("column " + colName + " does not exist");
					final int index = tableDef.indexOf(colDef);
					if (colDef.toUpperCase().indexOf(" KEY") > 0)
						throw new java.sql.SQLException("column " + colName + " is already a key");
					final int secondSpaceIndex = colDef.indexOf(' ', colDef.indexOf(' ') + 1);
					if (secondSpaceIndex < 0)
						colDef = colDef + " KEY" + columnSizeToken(colDef);
					else
						colDef = colDef.substring(0, secondSpaceIndex) + " KEY" + columnSizeToken(colDef);
					tableDef.set(index, colDef);
				}
			}
			else
			if(objType.equals("INDEX"))
			{
				final String[] colNames = stmt.changes;
				for(final String colName : colNames)
				{
					String colDef = findColumnDef(colName, tableDef);
					if (colDef == null)
						throw new java.sql.SQLException("column " + colName + " does not exist");
					final int index = tableDef.indexOf(colDef);
					if (colDef.toUpperCase().indexOf(" KEY") > 0)
						throw new java.sql.SQLException("column " + colName + " is already a key");
					if (colDef.toUpperCase().indexOf(" INDEX") > 0)
						throw new java.sql.SQLException("column " + colName + " is already a index");
					final int secondSpaceIndex = colDef.indexOf(' ', colDef.indexOf(' ') + 1);
					if (secondSpaceIndex < 0)
						colDef = colDef + " INDEX";
					else
						colDef = colDef.substring(0, secondSpaceIndex) + " INDEX" + colDef.substring(secondSpaceIndex);
					tableDef.set(index, colDef);
				}
			}
		}
		else
		if (action == StatementType.DROP)
		{
			if (objType.equals("COLUMN"))
			{
				for(final String name : stmt.changes)
				{
					final String colDef = findColumnDef(name, tableDef);
					if (colDef == null)
						throw new java.sql.SQLException("column " + name + " does not exist");
					final int index = tableDef.indexOf(colDef);
					tableDef.remove(index);
					if (!v2)
						fakeTable.removeColumn(index-1);
				}
			}
			else
			if (objType.equals("KEY"))
			{
				final String keyName = stmt.changes[0];
				String colDef = findColumnDef(keyName, tableDef);
				if (colDef == null)
					throw new java.sql.SQLException("column " + keyName + " does not exist");
				final int index = tableDef.indexOf(colDef);
				if (colDef.toUpperCase().indexOf(" KEY") < 0)
					throw new java.sql.SQLException("column " + keyName + " is not a key");
				colDef = colDef.replaceAll("(?i) KEY", "");
				tableDef.set(index, colDef);
			}
			else
			if (objType.equals("INDEX"))
			{
				final String idxName = stmt.changes[0];
				String colDef = findColumnDef(idxName, tableDef);
				if (colDef == null)
					throw new java.sql.SQLException("column " + idxName + " does not exist");
				final int index = tableDef.indexOf(colDef);
				if (colDef.toUpperCase().indexOf(" INDEX") < 0)
					throw new java.sql.SQLException("column " + idxName + " is not an index");
				colDef = colDef.replaceAll("(?i) INDEX", "");
				tableDef.set(index, colDef);
			}
		}
		else
		if(action == StatementType.ALTER)
		{
			if (!objType.equals("COLUMN"))
				throw new java.sql.SQLException("can only alter columns");
			final String colDef = findColumnDef(col.name, tableDef);
			if (colDef == null)
				throw new java.sql.SQLException("column " + col.name + " does not exist");
			final int index = tableDef.indexOf(colDef);
			final StringBuilder newColDef = new StringBuilder(col.name + " " + col.type.name().toLowerCase());
			if (col.canNull)
				newColDef.append(" NULL");
			else
				newColDef.append(" NOT NULL");
			if (col.keyNumber >= 0)
				newColDef.append(" KEY");
			if(v2)
			{
				int newSize = col.size;
				if (newSize == Integer.MAX_VALUE)
				{
					final String[] parts = colDef.trim().split(" ");
					for (int i = parts.length - 1; i >= 0; i--)
					{
						if ((parts[i].length() > 0) && Character.isDigit(parts[i].charAt(0)))
						{
							try
							{
								newSize = Integer.parseInt(parts[i]);
								break;
							}
							catch (final NumberFormatException e)
							{
							}
						}
					}
				}
				if ((col.type == FakeColType.STRING) 
				&& (newSize == Integer.MAX_VALUE))
					throw new java.sql.SQLException("V2 column '" + col.name + "' requires a size, e.g. " + col.name + " STRING (50)");
				if (((col.type == FakeColType.STRING) 
					|| (col.type == FakeColType.BLOB) 
					|| (col.type == FakeColType.CLOB))
				&& (newSize != Integer.MAX_VALUE))
					newColDef.append(" ").append(newSize);
			}
			tableDef.set(index, newColDef.toString());
		}
		if (v2)
			((FakeTable2)fakeTable).rebuildDataFile(tableDef);
		fakeTable.rewriteDataFileHash(tableDef);
		this.rewriteRawSchema(schema);
		try
		{
			// Rebuild all in-memory table state (columns/columnHash/metadata)
			this.open();
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to re-open database after table alter: "+e.getMessage());
		}
	}


	/**
	 * Create a table
	 *
	 * @param stmt The create statement
	 */
	public void createTable(final ImplCreateStatement stmt) throws SQLException
	{
		final String tableName = stmt.tableName;
		final int version = stmt.versionSpecified ? stmt.version : defaultTableVersion;
		final FakeColumn[] columns = (FakeColumn[])stmt.extValues();
		if (fakeTables.get(tableName) != null)
			throw new java.sql.SQLException("table " + tableName + " already exists");
		if (version >= 2)
		{
			for (final FakeColumn col : columns)
				if ((col.type == FakeColType.STRING) && (col.size == Integer.MAX_VALUE))
					throw new java.sql.SQLException("V2 column '" + col.name + "' requires a size, e.g. " + col.name + " STRING (50)");
		}
		final List<List<String>> schema = readRawSchema();
		int insert=-1;
		if((schema.size()>1)
		&&(schema.get(schema.size()-1).size()>0)
		&&(schema.get(schema.size()-1).get(0).startsWith("#")))
		{
			for(int g=schema.size()-1;g>=0;g--)
			{
				if ((schema.get(g).size() > 0) && (!schema.get(g).get(0).startsWith("#")))
				{
					insert = g;
					break;
				}
			}
		}
		final List<String> newTable = new ArrayList<String>();
		newTable.add(tableName+" V"+version);
		for (final FakeColumn col : columns)
		{
			col.tableName = tableName;
			final StringBuilder colDef = new StringBuilder(col.name + " " + col.type.name().toLowerCase());
			if (col.keyNumber >= 0)
				colDef.append(" KEY");
			else
			if (col.canNull)
				colDef.append(" NULL");
			if ((version >= 2)
			&& ((col.type == FakeColType.STRING) || (col.type == FakeColType.BLOB) || (col.type == FakeColType.CLOB))
			&& (col.size != Integer.MAX_VALUE))
				colDef.append(" ").append(col.size);
			newTable.add(colDef.toString());
		}
		if(insert<0)
			schema.add(newTable);
		else
			schema.add(insert+1,newTable);
		this.rewriteRawSchema(schema);
		try
		{
			this.open();
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to re-open database after table create: "+e.getMessage());
		}
	}

}
