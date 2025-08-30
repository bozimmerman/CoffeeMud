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
import com.planet_ink.fakedb.backend.statements.ImplDeleteStatement;
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
	 *
	 * @param fakeTable
	 * @param columns
	 * @param sqlValues
	 * @return
	 * @throws java.sql.SQLException
	 */
	public void dupKeyCheck(final String tableName, final String[] doCols, final String[] sqlValues) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + tableName);
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
	 *
	 * @param basePath
	 * @param schema
	 * @throws IOException
	 */
	public void readSchema(final File basePath, final File schema) throws IOException
	{
		final BufferedReader in = new BufferedReader(new FileReader(schema));

		try
		{
			int version = 1;
			while (true)
			{
				final String fakeTableName = in.readLine();
				if (fakeTableName == null)
					break;
				final int vx = fakeTableName.lastIndexOf(' ');
				if(vx > 0)
				{
					final String v = fakeTableName.substring(vx+1);
					if((v.length()==2) && (v.startsWith("V"))&&(Character.isDigit(v.charAt(1))))
						version = Integer.valueOf(v.substring(1)).intValue();
				}
				if (fakeTableName.length() == 0)
					throw new IOException("Can not read schema: tableName is null");
				if (fakeTableName.startsWith("#")) // comment
					continue;
				if (fakeTables.get(fakeTableName) != null)
					throw new IOException("Can not read schema: tableName is missing: " + fakeTableName);

				final FakeTable fakeTable = new FakeTable(fakeTableName, new File(basePath, "fakedb.data." + fakeTableName));
				fakeTable.version = version;
				fakeTable.initializeColumns(in);
				if(fakeTable.version > version)
					version = fakeTable.version;
				fakeTable.open();
				fakeTables.put(fakeTableName, fakeTable);
			}
			for(final FakeTable tab : fakeTables.values())
			{
				tab.version = version;
				for(final FakeColumn col : tab.columns)
					col.version = version;
			}
		}
		finally
		{
			in.close();
		}
	}

	/**
	 *
	 * @param basePath
	 * @return
	 */
	public boolean open(final File basePath) throws IOException
	{
		readSchema(basePath, new File(basePath, "fakedb.schema"));
		return true;
	}

	/**
	 *
	 * @param s
	 * @param tableName
	 * @param cols
	 * @param conditions
	 * @param orderVars
	 * @param orderModifiers
	 * @return
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
			throw new java.sql.SQLException("unknown table " + tableName);
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
	 * @param tableName
	 * @param columns
	 * @param dataValues
	 * @throws java.sql.SQLException
	 */
	public void insertValues(final ImplInsertStatement stmt) throws java.sql.SQLException
	{
		final String tableName = stmt.tableName;
		final String[] columns = stmt.columns;
		final String[] sqlValues = stmt.sqlValues;

		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + tableName);

		final ComparableValue[] values = new ComparableValue[fakeTable.columns.length];
		for (int index = 0; index < columns.length; index++)
		{
			final int id = fakeTable.findColumn(columns[index]);
			if (id < 0)
				throw new java.sql.SQLException("unknown column " + columns[index]);
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
	 *
	 * @param tableName
	 * @param conditionVar
	 * @param conditionValue
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
	 *
	 * @param tableName
	 * @param conditionVar
	 * @param conditionValue
	 * @param varNames
	 * @param values
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
			throw new java.sql.SQLException("unknown table " + tableName);

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
	 *
	 * @param tableName
	 * @param columnName
	 * @param comparitor
	 * @param value
	 * @return
	 * @throws java.sql.SQLException
	 */
	public FakeCondition buildFakeCondition(final String tableName, final String columnName, final String comparitor, final String value, final boolean unPrepared) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + tableName);
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

}
