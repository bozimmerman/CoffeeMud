package com.planet_ink.fakedb.backend;

/*
   Copyright 2001 Thomas Neumann
   Copyright 2004-2025 Bo Zimmerman

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
		catch (final Exception e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	/**
	 * Read the schema from a file
	 * @param basePath The base path
	 * @throws IOException if it fails
	 */
	public void readSchema(final File basePath) throws IOException
	{
		final List<List<String>> schema = readRawSchema();
		int version = 1;
		for(final List<String> group : schema)
		{
			if((group.size()==0)||(group.get(0).startsWith("#")))
				continue;
			final String s = group.remove(0).toUpperCase().trim();
			final int vx = s.lastIndexOf(' ');
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
				throw new IOException("Can not read schema: tableName is duplicate: " + tableName);

			final FakeTable fakeTable = new FakeTable(tableName, new File(basePath, "fakedb.data." + tableName));
			fakeTable.version = version;
			fakeTable.initializeColumns(group);
			if(fakeTable.version > version)
				version = fakeTable.version;
			fakeTable.open();
			fakeTables.put(tableName, fakeTable);
		}
		for(final FakeTable tab : fakeTables.values())
		{
			tab.version = version;
			for(final FakeColumn col : tab.columns)
				col.version = version;
		}
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
		readSchema(basePath);
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
					showCols[index] = FakeColumn.INDEX_COUNT;
				else
				{
					showCols[index] = table.findColumn(col);
					if (showCols[index] < 0)
					{
						try
						{
							Integer.parseInt(col);
							showCols[index] = FakeColumn.INDEX_COUNT;
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
		return new ResultSet(s, table, showCols, conditions, orderDexIndexes, orderModifiers);
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
		for (int index = 0; index < columns.length; index++)
		{
			final int id = fakeTable.findColumn(columns[index]);
			if (id < 0)
				throw new java.sql.SQLException("unknown column for insert " + columns[index]);
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
		final ComparableValue[] keys = new ComparableValue[fakeTable.columnIndexesOfIndexed.length];
		for (int index = 0; index < fakeTable.columnIndexesOfIndexed.length; index++)
		{
			final int id = fakeTable.columnIndexesOfIndexed[index];
			if (values[id] == null)
				keys[index] = new ComparableValue(null);
			else
				keys[index] = new ComparableValue(values[id]);
		}
		if (!fakeTable.insertRecord(null, keys, values))
			throw new java.sql.SQLException("unable to insert record");
	}


	/**
	 * Delete records in a table
	 * @param stmt The delete statement
	 * @throws java.sql.SQLException
	 */
	public void deleteRecord(final ImplDeleteStatement stmt) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(stmt.tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + stmt.tableName);

		fakeTable.deleteRecord(stmt.conditions);
	}

	/**
	 * Update records in a table
	 * @param stmt The update statement
	 * @throws java.sql.SQLException
	 */
	public void updateRecord(final ImplUpdateStatement stmt) throws java.sql.SQLException
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
		fakeTable.updateRecord(conditions, vars, values,this,doDupCheck?this.fakeTables.get(stmt.tableName):null);
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
		if (comparitor.equalsIgnoreCase("like"))
		{
			if ((col.type != FakeColType.STRING) && (col.type != FakeColType.UNKNOWN))
				throw new java.sql.SQLException("can't do like comparison on " + tableName + "." + columnName);
			fake.like = true;
		}
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

	public List<List<String>> readRawSchema()
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
		catch(final IOException e)
		{}
		return groups;
	}

	public synchronized void rewriteRawSchema(final List<List<String>> groups) throws SQLException
	{
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
	 * Alter a table
	 * @param stmt The alter statement
	 * @throws SQLException if it fails
	 */
	public synchronized void  alterTable(final ImplAlterStatement stmt) throws SQLException
	{
		final StatementType action = stmt.getSubStatementType();
		final String objType = stmt.objType;
		final FakeColumn col = (FakeColumn)stmt.extValues()[1];
		final String tableName = stmt.tableName;
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table for altering " + tableName);
		final List<List<String>> schema = readRawSchema();
		final List<String> tableDef = findTableDef(tableName,schema);
		if(action == StatementType.CREATE)
		{
			if(objType.equals("COLUMN"))
			{
				if (findColumnDef(col.name, tableDef) != null)
					throw new java.sql.SQLException("column " + col.name + " already exists");
				tableDef.add(col.name + " " + col.type.name().toLowerCase() + " " + (col.keyNumber > 0 ? "KEY " : col.canNull ? "NULL " : ""));
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
						colDef = colDef + " KEY";
					else
						colDef = colDef.substring(0, secondSpaceIndex) + " KEY";
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
					final boolean nullable = colDef.toUpperCase().indexOf(" NULL") > 0;
					final int secondSpaceIndex = colDef.indexOf(' ', colDef.indexOf(' ') + 1);
					if (secondSpaceIndex < 0)
						colDef = colDef + " INDEX";
					else
						colDef = colDef.substring(0, secondSpaceIndex) + " INDEX" + (nullable ? " NULL" : "");
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
					fakeTable.removeColumn(index-1);
				}
			}
			else if (objType.equals("KEY"))
			{
				String colDef = findColumnDef(col.name, tableDef);
				if (colDef == null)
					throw new java.sql.SQLException("column " + col.name + " does not exist");
				final int index = tableDef.indexOf(colDef);
				if (colDef.toUpperCase().indexOf(" KEY") < 0)
					throw new java.sql.SQLException("column " + col.name + " is not a key");
				colDef = colDef.replaceAll("(?i) KEY", "");
				tableDef.set(index, colDef);
			}
			else if (objType.equals("INDEX"))
			{
				String colDef = findColumnDef(col.name, tableDef);
				if (colDef == null)
					throw new java.sql.SQLException("column " + col.name + " does not exist");
				final int index = tableDef.indexOf(colDef);
				if (colDef.toUpperCase().indexOf(" INDEX") < 0)
					throw new java.sql.SQLException("column " + col.name + " is not an index");
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
			if (col.keyNumber > 0)
				newColDef.append(" KEY");
			tableDef.set(index, newColDef.toString());
		}
		fakeTable.rewriteDataFileHash(tableDef);
		this.rewriteRawSchema(schema);
		try
		{
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
		final FakeColumn[] columns = (FakeColumn[])stmt.extValues();
		if (fakeTables.get(tableName) != null)
			throw new java.sql.SQLException("table " + tableName + " already exists");
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
		newTable.add(tableName+" V1");
		for (final FakeColumn col : columns)
		{
			col.tableName = tableName;
			newTable.add(col.name + " " + col.type.name().toLowerCase() + " " + (col.keyNumber > 0 ? "KEY " : col.canNull ? "NULL " : ""));
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
