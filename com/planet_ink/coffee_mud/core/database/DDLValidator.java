package com.planet_ink.coffee_mud.core.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.MiniJSON;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
/*
	Copyright 2025-2025 Bo Zimmerman

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

/**
 * A class to validate and upgrade a database schema based on a JSON changelist.
 */
public class DDLValidator
{
	private final DBConnector	DB;
	private List<JSONObject>[]	changes;

	/**
	 * Constructs a DDLValidator with the specified DBConnector.
	 *
	 * @param DB the database connector
	 * @param changeList the JSON object representing the changelist
	 */
	public DDLValidator(final DBConnector DB, final JSONObject changeList)
	{
		this.DB=DB;
		if(changeList != null)
		{
			@SuppressWarnings("unchecked")
			List<JSONObject>[] allChanges=new List[1];
			for(final String key : changeList.keySet())
			{
				final int i = Integer.parseInt(key);
				if(changeList.get(key) instanceof Object[])
				{
					final Object[] arr=(Object[])changeList.get(key);
					final List<JSONObject> changeSet=new ArrayList<JSONObject>();
					for(final Object oc : arr)
						changeSet.add((JSONObject)oc);
					if(i>=allChanges.length)
						allChanges=Arrays.copyOf(allChanges, i+1);
					allChanges[i]=changeSet;
				}
			}
			for (int i=0;i<allChanges.length;i++)
			{
				if (allChanges[i] == null)
					allChanges[i] = new ArrayList<JSONObject>();
			}
			changes = allChanges;
		}
		else
			changes = null;
	}

	/**
	 * A helper class to represent a table schema during processing
	 */
	private static class DDLTable
	{
		String name;

		LinkedHashMap<String, JSONObject>	columns			= new LinkedHashMap<>();
		List<JSONObject>					keyActions		= new ArrayList<>();
		List<JSONObject>					indexActions	= new ArrayList<>();

		public DDLTable(final String name)
		{
			this.name=name;
		}
	}

	public static String getPortableType(final int type, final String actualName, final int size)
	{
		String typeName=null;
		switch (type)
		{
		case java.sql.Types.NUMERIC:
		case java.sql.Types.DECIMAL:
			if(size == 38)
				typeName = "INT";
			else
			if(size == 20)
				typeName = "LONG";
			else
				typeName = (size > 9) ? "LONG" : "INT";
			break;
		case java.sql.Types.INTEGER:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
			typeName="INT";
			break;
		case java.sql.Types.BIGINT:
			typeName="LONG";
			break;
		case java.sql.Types.VARCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.CHAR:
			if((actualName != null)&&(actualName.equalsIgnoreCase("text")))
				typeName = "TEXT";
			else
				typeName="VARCHAR";
			break;
		case -16:
		case java.sql.Types.NCLOB:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.CLOB:
			typeName="TEXT";
			break;
		case java.sql.Types.TIMESTAMP:
			typeName="TIMESTAMP";
			break;
		case java.sql.Types.DATE:
		case java.sql.Types.TIME:
			typeName="DATETIME";
			break;
		}
		return typeName;
	}

	/**
	 * Current database version derived from the changelist.
	 *
	 * @param changes the changelist JSON object
	 * @param oneVer if non-null, only check this version
	 * @param oneChgIdx if non-null, only check this change index
	 * @return the current database version, or -database version if corrupted
	 * @throws Exception if an error occurs
	 */
	public int getLastAppliedVersion(final List<JSONObject>[] changes, final Integer oneVer, final Integer oneChgIdx) throws Exception
	{
		DBConnection conn=null;
		try
		{
			conn=DB.DBFetch();
			final DatabaseMetaData meta=conn.getMetaData();
			if(meta==null)
				return Integer.MIN_VALUE;
			final String productName=meta.getDatabaseProductName().toLowerCase();
			final String schema = (productName.contains("oracle") || productName.contains("db2")) ? conn.getSchema() : null;
			final ResultSet rs=meta.getTables(null, schema, "%", new String[]{"TABLE"});
			final Set<String> dbTables=new HashSet<>();
			final Map<String, String> tableCaseMap=new HashMap<>();
			while(rs.next())
			{
				final String actualTableName=rs.getString("TABLE_NAME");
				final String upperTableName=actualTableName.toUpperCase();
				dbTables.add(upperTableName);
				tableCaseMap.put(upperTableName, actualTableName);
			}
			rs.close();
			final boolean stringsAreBlobs=meta.getDatabaseProductName().equalsIgnoreCase("fakedb");

			final Map<String, Map<String, JSONObject>> dbColumnsByTable=new HashMap<>();
			final Map<String, List<String>> dbPrimaryKeysByTable=new HashMap<>();
			final Map<String, Set<String>> dbIndexSetsByTable=new HashMap<>();

			for(final String upperT : dbTables)
			{
				final String actualT=tableCaseMap.getOrDefault(upperT, upperT);
				final ResultSet colsRs=meta.getColumns(null, null, actualT, "%");
				final Map<String, JSONObject> dbCols=new HashMap<>();
				while(colsRs.next())
				{
					final String cname=colsRs.getString("COLUMN_NAME").toUpperCase();
					final int type=colsRs.getInt("DATA_TYPE");
					final String actualTypeName = colsRs.getString("TYPE_NAME");
					final int precision = colsRs.getInt("COLUMN_SIZE");
					final String typeName=getPortableType(type, actualTypeName, precision);
					final boolean nullable=colsRs.getString("IS_NULLABLE").equals("YES");
					if(typeName==null)
						throw new Exception("Unknown DB type "+typeName+" for column "+cname+" in table "+upperT);
					final JSONObject column=new JSONObject();
					column.put("name", cname);
					column.put("type", typeName);
					column.put("size", Long.valueOf(precision));
					column.put("nullable", Boolean.valueOf(nullable));
					dbCols.put(cname, column);
				}
				colsRs.close();
				dbColumnsByTable.put(upperT, dbCols);

				// primary keys
				final ResultSet pkRs=meta.getPrimaryKeys(null, null, actualT);
				final List<String> dbKeys=new ArrayList<>();
				while(pkRs.next())
				{
					final String col=pkRs.getString("COLUMN_NAME").toUpperCase();
					dbKeys.add(col);
				}
				pkRs.close();
				Collections.sort(dbKeys);
				dbPrimaryKeysByTable.put(upperT, dbKeys);

				// indexes
				final ResultSet idxRs=meta.getIndexInfo(null, null, actualT, false, false);
				final Set<String> dbIndexSets=new HashSet<>();
				while(idxRs.next())
				{
					final String idxName=idxRs.getString("INDEX_NAME");
					if(idxName==null||idxName.equals("PRIMARY"))
						continue;
					final String col=idxRs.getString("COLUMN_NAME").toUpperCase();
					dbIndexSets.add(col);
				}
				idxRs.close();
				dbIndexSetsByTable.put(upperT, dbIndexSets);
			}

			int startVer=changes.length-1;
			int lastVer=1;
			if(oneVer!=null)
			{
				startVer=oneVer.intValue();
				lastVer=oneVer.intValue();
			}
			for(int ver=startVer;ver>=lastVer;ver--)
			{
				if((changes[ver]==null)||(changes[ver].size()==0))
					continue;
				final List<JSONObject> list=changes[ver];
				int validCount=0;
				if((oneChgIdx!=null)&&(oneChgIdx.intValue() >= 0)&&(oneChgIdx.intValue()<list.size()))
				{
					if(validateChange(list.get(oneChgIdx.intValue()), dbTables, dbColumnsByTable, dbPrimaryKeysByTable, dbIndexSetsByTable, stringsAreBlobs))
						return ver;
					else
						return 0;
				}
				else
				for(final JSONObject c : list)
				{
					if(validateChange(c, dbTables, dbColumnsByTable, dbPrimaryKeysByTable, dbIndexSetsByTable, stringsAreBlobs))
						validCount++;
				}
				if(validCount==list.size())
					return ver;
				else
				if(validCount>0)
					return -ver;
			}
			return 0;
		}
		finally
		{
			if(conn!=null)
				DB.DBDone(conn);
		}
	}

	/**
	 * Validates a single change against the current database state.
	 *
	 * @param c the change JSON object
	 * @param dbTables the set of current database tables
	 * @param dbColumnsByTable columns by table
	 * @param dbPrimaryKeysByTable primary keys by table
	 * @param dbIndexSetsByTable indexes by table
	 * @param stringsAreBlobs if true, VARCHAR and TEXT are interchangeable
	 * @return true if the change is valid, false otherwise
	 * @throws MiniJSON.MJSONException if a JSON error occurs
	 */
	public boolean validateChange(final JSONObject c, final Set<String> dbTables,
								  final Map<String, Map<String, JSONObject>> dbColumnsByTable,
								  final Map<String, List<String>> dbPrimaryKeysByTable,
								  final Map<String, Set<String>> dbIndexSetsByTable,
								  final boolean stringsAreBlobs)
		throws MiniJSON.MJSONException
	{
		if((!c.containsKey("name"))||(c.get("name")==null))
			return false;
		final String nameUpper=c.getCheckedString("name").toUpperCase();
		final String action=c.getCheckedString("action").toUpperCase();
		final String target=c.getCheckedString("target").toUpperCase();

		if(action.equals("MOVE"))
		{
			if(!target.equals("COLUMN"))
				return false;
			if((!c.containsKey("from_table"))||(c.get("from_table")==null)||(!c.containsKey("to_table"))||(c.get("to_table")==null))
				return false;
			final String from_table=c.getCheckedString("from_table").toUpperCase();
			final String to_table=c.getCheckedString("to_table").toUpperCase();
			final boolean fromTableExists=dbTables.contains(from_table);
			if(fromTableExists)
			{
				final Map<String, JSONObject> dbCols=dbColumnsByTable.get(from_table);
				final boolean exists=dbCols.containsKey(nameUpper);
				if(exists)
					return false;
			}
			final boolean toTableExists=dbTables.contains(to_table);
			if(!toTableExists)
				return false;
			final Map<String, JSONObject> dbCols=dbColumnsByTable.get(to_table);
			final JSONObject dc=dbCols.get(nameUpper);
			final boolean exists=(dc!=null);
			if(!exists)
				return false;
			final String[][] fieldGroups=new String[][] {
				{"INT","INTEGER"},
				{"LONG","BIGINT"},
				{"VARCHAR", "CHAR", "STRING"},
				{"TEXT","LONGTEXT"},
				{"DATETIME"},
				{"TIMESTAMP"},
			};
			int ctype=-1;
			int dctype=-1;
			for(int g=0;g<fieldGroups.length;g++)
			{
				if(CMParms.contains(fieldGroups[g], c.getCheckedString("type").toUpperCase()))
					ctype=g;
				if((dc!=null) && CMParms.contains(fieldGroups[g], dc.getCheckedString("type").toUpperCase()))
					dctype=g;
			}

			if((ctype>=0)&&(dctype!=ctype))
			{
				if(!stringsAreBlobs)
					return false;
				if((ctype==2)&&(dctype!=3))
					return false;
				if((ctype==3)&&(dctype!=2))
					return false;
			}
			final int csize;
			if(c.containsKey("size"))
				csize=c.getCheckedLong("size").intValue();
			else
				csize=-1;
			final int dcsize;
			if((dc!=null) && dc.containsKey("size"))
				dcsize=dc.getCheckedLong("size").intValue();
			else
				dcsize=-1;
			if(csize >= 0&&dcsize >= 0&&dcsize!=csize)
				return false;
			return true;
		}


		if(target.equals("TABLE"))
		{
			final boolean exists=dbTables.contains(nameUpper);
			if(action.equals("ADD")||action.equals("MODIFY"))
				return exists;
			if(action.equals("DELETE"))
				return !exists;
			return false;
		}
		else
		{
			if((!c.containsKey("table"))||(c.get("table")==null))
				return false;
			final String tname=c.getCheckedString("table").toUpperCase();
			final boolean tableExists=dbTables.contains(tname);
			if(!tableExists)
			{
				if(action.equals("ADD")||action.equals("MODIFY"))
					return false;
				if(action.equals("DELETE"))
					return false;
				return false;
			}
			if(target.equals("COLUMN"))
			{
				final Map<String, JSONObject> dbCols=dbColumnsByTable.get(tname);
				final JSONObject dc=dbCols.get(nameUpper);
				final boolean exists=(dc!=null);
				if(action.equals("ADD")||action.equals("MODIFY"))
				{
					if(!exists)
						return false;
					final String[][] fieldGroups=new String[][] {
						{"INT","INTEGER"},
						{"LONG","BIGINT"},
						{"VARCHAR", "CHAR", "STRING"},
						{"TEXT","LONGTEXT"},
						{"DATETIME"},
						{"TIMESTAMP"},
					};
					int ctype=-1;
					int dctype=-1;
					for(int g=0;g<fieldGroups.length;g++)
					{
						if(CMParms.contains(fieldGroups[g], c.getCheckedString("type").toUpperCase()))
							ctype=g;
						if((dc!=null) && CMParms.contains(fieldGroups[g], dc.getCheckedString("type").toUpperCase()))
							dctype=g;
					}

					if((ctype>=0)&&(dctype!=ctype))
					{
						if(!stringsAreBlobs)
							return false;
						if((ctype==2)&&(dctype!=3))
							return false;
						if((ctype==3)&&(dctype!=2))
							return false;
					}
					final int csize;
					if(c.containsKey("size"))
						csize=c.getCheckedLong("size").intValue();
					else
						csize=-1;
					final int dcsize;
					if((dc!=null) && dc.containsKey("size"))
						dcsize=dc.getCheckedLong("size").intValue();
					else
						dcsize=-1;
					if((csize>=0)&&(dcsize>=0)&&(dcsize!=csize))
						return false;
					return true;
				}
				else
				if(action.equals("DELETE"))
				{
					return !exists;
				}
			}
			else
			if(target.equals("KEY"))
			{
				final List<String> dbPks=dbPrimaryKeysByTable.get(tname);
				final Set<String> dbIdxs=dbIndexSetsByTable.get(tname);
				final boolean inPk=dbPks.contains(nameUpper);
				final boolean inIdx=dbIdxs.contains(nameUpper);
				final boolean has=inPk||inIdx;
				if(action.equals("ADD")||action.equals("MODIFY"))
					return has;
				if(action.equals("DELETE"))
					return !has;
			}
			else
			if(target.equals("INDEX"))
			{
				final Set<String> dbIdxs=dbIndexSetsByTable.get(tname);
				final boolean has=dbIdxs.contains(nameUpper);
				if(action.equals("ADD")||action.equals("MODIFY"))
					return has;
				if(action.equals("DELETE"))
					return !has;
			}
		}
		return false;
	}

	/**
	 * Returns the current database version code, or Integer.MIN_VALUE if unable
	 * to determine. Positive version means valid, negative means corrupted.
	 *
	 * @param quiet true to suppress error messages
	 * @return the current database version code
	 */
	public int getDatabaseVersionCode(final boolean quiet)
	{
		if(changes==null)
			return Integer.MIN_VALUE;
		try
		{
			final int lastAppliedVersion=getLastAppliedVersion(changes, null, null);
			if(lastAppliedVersion==Integer.MIN_VALUE)
			{
				if(!quiet)
					Log.errOut("Unable to create a connection");
				return Integer.MIN_VALUE;
			}
			if(lastAppliedVersion>0)
			{
				if(lastAppliedVersion<changes.length-1)
					return lastAppliedVersion;
				else
				if(lastAppliedVersion==0)
					return 0;
				return lastAppliedVersion; // positive
			}
			else
				return lastAppliedVersion; // negative
		}
		catch (final Exception e)
		{
			if(!quiet)
			{
				Log.errOut("Unable to validate database.");
				Log.errOut(e);
			}
			return Integer.MIN_VALUE;
		}
	}

	/**
	 * Validates a list of schema changes against the current database state,
	 * returning a list of error messages for any changes that cannot be applied.
	 * For changes that add a new table and include modifications to that table,
	 * only errors for the table addition are reported to avoid redundant messages.
	 *
	 * @param changes the list of schema changes to validate
	 * @return a list of error messages, empty if all changes are valid
	 * @throws Exception if an error occurs accessing the database
	 */
	public List<String> validateChanges(final List<JSONObject> changes) throws Exception
	{
		final List<String> errors=new ArrayList<>();
		DBConnection conn=null;
		try
		{
			conn=DB.DBFetch();
			final DatabaseMetaData meta=conn.getMetaData();
			final String productName=meta.getDatabaseProductName().toLowerCase();
			final String schema = (productName.contains("oracle"))?conn.getSchema():null;
			final ResultSet rs=meta.getTables(null, schema, "%", new String[] { "TABLE" });
			final Set<String> dbTables=new HashSet<>();
			final Map<String, String> tableCaseMap=new HashMap<>();
			while(rs.next())
			{
				final String actualTableName=rs.getString("TABLE_NAME");
				final String upperTableName=actualTableName.toUpperCase();
				dbTables.add(upperTableName);
				tableCaseMap.put(upperTableName, actualTableName);
			}
			rs.close();

			final Map<String, Map<String, JSONObject>> dbColumnsByTable=new HashMap<>();
			final Map<String, List<String>> dbPrimaryKeysByTable=new HashMap<>();
			final Map<String, Set<String>> dbIndexSetsByTable=new HashMap<>();

			for(final String upperT : dbTables)
			{
				final String actualT=tableCaseMap.getOrDefault(upperT, upperT);
				final ResultSet colsRs=meta.getColumns(null, null, actualT, "%");
				final Map<String, JSONObject> dbCols=new HashMap<>();
				while(colsRs.next())
				{
					final String cname=colsRs.getString("COLUMN_NAME").toUpperCase();
					final int type=colsRs.getInt("DATA_TYPE");
					final String actualTypeName = colsRs.getString("TYPE_NAME");
					final int precision = colsRs.getInt("COLUMN_SIZE");
					final String typeName = DDLValidator.getPortableType(type, actualTypeName, precision);
					if(typeName == null)
						continue;
					final boolean nullable=colsRs.getString("IS_NULLABLE").equals("YES");
					final JSONObject column=new JSONObject();
					column.put("name", cname);
					column.put("type", typeName);
					column.put("size", Long.valueOf(precision));
					column.put("nullable", Boolean.valueOf(nullable));
					dbCols.put(cname, column);
				}
				colsRs.close();
				dbColumnsByTable.put(upperT, dbCols);

				final ResultSet pkRs=meta.getPrimaryKeys(null, null, actualT);
				final List<String> dbKeys=new ArrayList<>();
				while(pkRs.next())
				{
					final String col=pkRs.getString("COLUMN_NAME").toUpperCase();
					dbKeys.add(col);
				}
				pkRs.close();
				Collections.sort(dbKeys);
				dbPrimaryKeysByTable.put(upperT, dbKeys);
				final ResultSet idxRs=meta.getIndexInfo(null, null, actualT, false, false);
				final Set<String> dbIndexSets=new HashSet<>();
				while(idxRs.next())
				{
					final String idxName=idxRs.getString("INDEX_NAME");
					if((idxName == null)||(idxName.equals("PRIMARY")))
						continue;
					final String col=idxRs.getString("COLUMN_NAME").toUpperCase();
					dbIndexSets.add(col);
				}
				idxRs.close();
				dbIndexSetsByTable.put(upperT, dbIndexSets);
			}
			final Set<String> newTables=new HashSet<>();
			for(final JSONObject c : changes)
			{
				try
				{
					final String act=c.getCheckedString("action").toUpperCase();
					final String tgt=c.getCheckedString("target").toUpperCase();
					if("ADD".equals(act) && "TABLE".equals(tgt))
					{
						final String name=c.getCheckedString("name").toUpperCase();
						newTables.add(name);
					}
				}
				catch (final MJSONException e)
				{
				}
			}
			for(final JSONObject c : changes)
			{
				try
				{
					final String act=c.getCheckedString("action").toUpperCase();
					final String tgt=c.getCheckedString("target").toUpperCase();
					final String name=c.getCheckedString("name");
					final String nameUpper=name.toUpperCase();
					final String table=c.containsKey("table") ? c.getCheckedString("table").toUpperCase() : null;
					if(table != null && newTables.contains(table))
						continue;

					if("TABLE".equals(tgt))
					{
						final boolean exists=dbTables.contains(nameUpper);
						if("ADD".equals(act))
						{
							if(exists)
								errors.add("Cannot add table '"+name+"': already exists.");
						}
						else
						if("DELETE".equals(act))
						{
							if(!exists)
								errors.add("Cannot delete table '"+name+"': does not exist.");
						}
						else
							errors.add("Unsupported action '"+act+"' for table: "+c.toString());
					}
					else
					if("COLUMN".equals(tgt))
					{
						if(table == null)
						{
							errors.add("Column change missing 'table': "+c.toString());
							continue;
						}
						final boolean tableExists=dbTables.contains(table);
						if(!tableExists)
						{
							errors.add("Table '"+table+"' does not exist for column change: "+c.toString());
							continue;
						}
						final Map<String, JSONObject> dbCols=dbColumnsByTable.get(table);
						final boolean colExists=dbCols.containsKey(nameUpper);
						if("ADD".equals(act))
						{
							if(colExists)
								errors.add("Cannot add column '"+name+"' to table '"+table+"': already exists.");
						}
						else
						if("MODIFY".equals(act))
						{
							if(!colExists)
								errors.add("Cannot modify column '"+name+"' in table '"+table+"': does not exist.");
						}
						else
						if("DELETE".equals(act))
						{
							if(!colExists)
								errors.add("Cannot delete column '"+name+"' from table '"+table+"': does not exist.");
						}
						else
							errors.add("Unsupported action '"+act+"' for column: "+c.toString());
					}
					else
					if("KEY".equals(tgt))
					{
						if(table == null)
						{
							errors.add("Key change missing 'table': "+c.toString());
							continue;
						}
						final boolean tableExists=dbTables.contains(table);
						if(!tableExists)
						{
							errors.add("Table '"+table+"' does not exist for key change: "+c.toString());
							continue;
						}
						final Map<String, JSONObject> dbCols=dbColumnsByTable.get(table);
						if(!dbCols.containsKey(nameUpper))
						{
							errors.add("Column '"+name+"' does not exist in table '"+table+"' for key change.");
							continue;
						}
						final List<String> dbPks=dbPrimaryKeysByTable.get(table);
						final boolean isPk=dbPks.contains(nameUpper);
						if("ADD".equals(act))
						{
							if(isPk)
								errors.add("Cannot add key '"+name+"' to table '"+table+"': already a primary key.");
						}
						else
						if("DELETE".equals(act))
						{
							if(!isPk)
								errors.add("Cannot delete key '"+name+"' from table '"+table+"': not a primary key.");
						}
						else
							errors.add("Unsupported action '"+act+"' for key: "+c.toString());
					}
					else
					if("INDEX".equals(tgt))
					{
						if(table == null)
						{
							errors.add("Index change missing 'table': "+c.toString());
							continue;
						}
						final boolean tableExists=dbTables.contains(table);
						if(!tableExists)
						{
							errors.add("Table '"+table+"' does not exist for index change: "+c.toString());
							continue;
						}
						final String idxName=c.getCheckedString("name");
						final List<String> group=Arrays.asList(idxName.split(","));
						final List<String> cols=new ArrayList<>();
						for(final String s : group)
							cols.add(s.trim().toUpperCase());
						Collections.sort(cols);
						final String idxStr=String.join(",", cols);
						final Set<String> dbIdxs=dbIndexSetsByTable.get(table);
						final boolean idxExists=dbIdxs.contains(idxStr);
						if("ADD".equals(act))
						{
							if(idxExists)
								errors.add("Cannot add index '"+idxName+"' to table '"+table+"': already exists.");
							final Map<String, JSONObject> dbCols=dbColumnsByTable.get(table);
							for(final String col : cols)
							{
								if(!dbCols.containsKey(col))
									errors.add("Column '"+col+"' does not exist in table '"+table+"' for index add.");
							}
						}
						else
						if("DELETE".equals(act))
						{
							if(!idxExists)
								errors.add("Cannot delete index '"+idxName+"' from table '"+table+"': does not exist.");
						}
						else
							errors.add("Unsupported action '"+act+"' for index: "+c.toString());
					}
					else
					if("MOVE".equals(act))
					{
						if(!"COLUMN".equals(tgt))
						{
							errors.add("Unsupported move target: "+c.toString());
							continue;
						}
						final String fromTable=c.getCheckedString("from_table").toUpperCase();
						final String toTable=c.getCheckedString("to_table").toUpperCase();
						if(fromTable == null || toTable == null)
						{
							errors.add("Move column missing 'from_table' or 'to_table': "+c.toString());
							continue;
						}
						final boolean fromExists=dbTables.contains(fromTable);
						final boolean toExists=dbTables.contains(toTable);
						if(!fromExists)
							errors.add("From table '"+fromTable+"' does not exist for move column '"+name+"'.");
						if(!toExists)
							errors.add("To table '"+toTable+"' does not exist for move column '"+name+"'.");
						if(fromExists)
						{
							final Map<String, JSONObject> dbColsFrom=dbColumnsByTable.get(fromTable);
							if(!dbColsFrom.containsKey(nameUpper))
								errors.add("Column '"+name+"' does not exist in from table '"+fromTable+"' for move.");
						}
						if(toExists)
						{
							final Map<String, JSONObject> dbColsTo=dbColumnsByTable.get(toTable);
							if(dbColsTo.containsKey(nameUpper))
								errors.add("Column '"+name+"' already exists in to table '"+toTable+"' for move.");
						}
					}
					else
						errors.add("Unsupported change: "+c.toString());
				}
				catch (final MJSONException e)
				{
					errors.add("Invalid change JSON: "+e.getMessage()+" for "+c.toString());
				}
			}
			return errors;
		}
		finally
		{
			if(conn != null)
				DB.DBDone(conn);
		}
	}

	/**
	 * Validates the current database version against the changelist.
	 * @return null if valid, otherwise an error string
	 */
	public String validateDatabaseVersion()
	{
		if (changes == null)
			return "Unable to find database changelist for validation.";
		final int currentVersion = changes.length-1;
		final int lastAppliedVersion=getDatabaseVersionCode(false);
		if(lastAppliedVersion==Integer.MIN_VALUE)
			return "Unable to validate database version.  Startup aborted.";
		if(lastAppliedVersion>0)
		{
			if(lastAppliedVersion<currentVersion)
				return "Database is at version "+lastAppliedVersion+", but version "+currentVersion+" is required.";
			else
			if(lastAppliedVersion==0)
				return "Database was empty, and needs to be initialized to version "+currentVersion+".";
		}
		else
		if(lastAppliedVersion == 0)
			return "Your database has an empty schema.";
		else
			return "Your database is at approximately version "+(-lastAppliedVersion)+", but version "+currentVersion+" is required.";
		return null;
	}

	/**
	 * Given a list of schema versions (each a list of actions), computes the
	 * final schema by applying all actions in order.
	 *
	 * @param versions List of schema versions, each a list of action maps
	 * @return The final schema as a list of action maps
	 */
	private List<JSONObject> getFinalSchema(final List<JSONObject>[] versions)
	{
		final Map<String, DDLTable> schema=new HashMap<>();
		final List<String> tableOrder=new ArrayList<>();

		for(final List<JSONObject> version:versions)
		{
			if(version == null)
				continue;
			for(final JSONObject action: version)
			{
				final String act=(String)action.get("action");
				final String target=(String)action.get("target");
				final String table=(String)action.get("table");
				if((table!=null)
				&& (!schema.containsKey(table)))
				{
					schema.put(table, new DDLTable(table));
					tableOrder.add(table);
				}
				if("ADD".equals(act))
				{
					if("TABLE".equals(target))
					{
						final String name=(String) action.get("name");
						if(!schema.containsKey(name))
						{
							schema.put(name, new DDLTable(name));
							tableOrder.add(name);
						}
					}
					else
					if("COLUMN".equals(target))
					{
						final String col=(String) action.get("name");
						final JSONObject props=new JSONObject();
						if(action.containsKey("type"))
							props.put("type", action.get("type"));
						if(action.containsKey("size"))
							props.put("size", action.get("size"));
						if(action.containsKey("nullable"))
							props.put("nullable", action.get("nullable"));
						schema.get(table).columns.put(col, props);
					}
					else
					if("KEY".equals(target))
						schema.get(table).keyActions.add(action);
					else
					if("INDEX".equals(target))
						schema.get(table).indexActions.add(action);
				}
				else
				if("MODIFY".equals(act))
				{
					if("COLUMN".equals(target))
					{
						final String col=(String) action.get("name");
						final Map<String, Object> props=schema.get(table).columns.get(col);
						if(props!=null) {
							if(action.containsKey("type")) props.put("type", action.get("type"));
							if(action.containsKey("size")) props.put("size", action.get("size"));
							if(action.containsKey("nullable")) props.put("nullable", action.get("nullable"));
						}
					}
				}
				else
				if("DELETE".equals(act))
				{
					if("COLUMN".equals(target))
					{
						final String col=(String) action.get("name");
						schema.get(table).columns.remove(col);
						schema.get(table).keyActions.removeIf(k -> col.equals(k.get("name")));
					}
				}
				else
				if("MOVE".equals(act))
				{
					if("COLUMN".equals(target))
					{
						final String col=(String)action.get("name");
						final String from=(String)action.get("from_table");
						final String to=(String)action.get("to_table");
						if(!schema.containsKey(to))
						{
							schema.put(to, new DDLTable(to));
							tableOrder.add(to);
						}
						JSONObject props=schema.get(from).columns.remove(col);
						if(props==null)
							props=new JSONObject();
						if(action.containsKey("type"))
							props.put("type", action.get("type"));
						if(action.containsKey("size"))
							props.put("size", action.get("size"));
						if(action.containsKey("nullable"))
							props.put("nullable", action.get("nullable"));
						schema.get(to).columns.put(col, props);
						schema.get(from).keyActions.removeIf(k -> col.equals(k.get("name")));
					}
				}
			}
		}
		final List<JSONObject> result=new ArrayList<>();
		for(final String t : tableOrder)
		{
			final DDLTable tab=schema.get(t);
			if(tab==null)
				continue;
			final JSONObject addTable=new JSONObject();
			addTable.put("action", "ADD");
			addTable.put("target", "TABLE");
			addTable.put("name", tab.name);
			result.add(addTable);
			for(final Map.Entry<String, JSONObject> entry:tab.columns.entrySet())
			{
				final String col=entry.getKey();
				final JSONObject props=entry.getValue();
				final JSONObject addCol=new JSONObject();
				addCol.put("action", "ADD");
				addCol.put("target", "COLUMN");
				addCol.put("name", col);
				addCol.put("table", t);
				if(props.containsKey("type"))
					addCol.put("type", props.get("type"));
				if(props.containsKey("size"))
					addCol.put("size", props.get("size"));
				if(props.containsKey("nullable"))
					addCol.put("nullable", props.get("nullable"));
				result.add(addCol);
			}
			for(final JSONObject key:tab.keyActions)
				result.add(key);
			for(final JSONObject index:tab.indexActions)
				result.add(index);
		}
		return result;
	}

	/**
	 * Creates the database from scratch, using the changelist.
	 *
	 * @return null if successful, otherwise an error string
	 */
	private String createDatabase()
	{
		if(changes==null)
			return "Unable to upgrade database: no change lists found.";
		DBConnection conn=null;
		try
		{
			conn=DB.DBFetch();
			final DatabaseMetaData meta=conn.getMetaData();
			final String productName=meta.getDatabaseProductName().toLowerCase();
			final String schema = (productName.contains("oracle"))?conn.getSchema():null;
			final DDLGenerator ddlGen=new DDLGenerator(meta,schema);
			final List<String> sql=ddlGen.generateSQLForChanges(getFinalSchema(changes));
			for(final String s : sql)
			{
				try
				{
					if(CMSecurity.isDebugging(DbgFlag.SQLERRORS))
						Log.debugOut(s);
					conn.update(s, 0);
				}
				catch (final SQLException e)
				{
					Log.errOut(e);
					return "Unable to create to database: "+e.getMessage()+".  Please do so manually.";
				}
			}
			Log.sysOut("Database tables successfully created.");
		}
		catch (final Exception e)
		{
			Log.errOut(e);
			return "Unable to create database.  Please do so manually.";
		}
		finally
		{
			if(conn!=null)
				DB.DBDone(conn);
		}
		return null;
	}

	/**
	 * Upgrades the database version to the current version.
	 *
	 * @return null if successful, otherwise an error string
	 */
	public String upgradeDatabaseVersion()
	{
		if(changes==null)
			return "Unable to upgrade database: no change lists found.";
		int version=getDatabaseVersionCode(true);
		if(version==0)
			return createDatabase();
		if(version==Integer.MIN_VALUE)
			return "Unable to upgrade database: fatal error during version test.";
		if(version<0)
		{
			final int oldVersion=-version;
			final List<JSONObject> baseChanges=changes[-version];
			final List<JSONObject> applicableChanges=new LinkedList<JSONObject>();
			for(int c=0;c<baseChanges.size();c++)
			{
				try
				{
					if(getLastAppliedVersion(changes, Integer.valueOf(oldVersion), Integer.valueOf(c))==0)
						applicableChanges.add(baseChanges.get(c));
				}
				catch (final Exception e)
				{
					applicableChanges.add(baseChanges.get(c));
				}
			}
			if(applicableChanges.size() >0)
			{
				DBConnection conn=null;
				try
				{
					conn=DB.DBFetch();
					final DatabaseMetaData meta=conn.getMetaData();
					final String productName=meta.getDatabaseProductName().toLowerCase();
					final String schema = (productName.contains("oracle"))?conn.getSchema():null;
					final DDLGenerator ddlGen=new DDLGenerator(meta, schema);
					final List<String> sql=ddlGen.generateSQLForChanges(applicableChanges);
					for(final String s : sql)
					{
						try
						{
							if(CMSecurity.isDebugging(DbgFlag.SQLERRORS))
								Log.debugOut(s);
							conn.update(s, 0);
						}
						catch (final SQLException e)
						{
							Log.errOut(e);
							return "Unable to upgrade to database version "+oldVersion+": "+e.getMessage()+".  Please do so manually.";
						}
					}
				}
				catch (final Exception e)
				{
					return "Unable to upgrade to database version "+oldVersion+": "+e.getMessage()+".  Please do so manually.";
				}
				finally
				{
					if(conn!=null)
						DB.DBDone(conn);
				}
				Log.sysOut("Database upgraded to version "+oldVersion);
			}
			version=getDatabaseVersionCode(true);
			if((version==-oldVersion)||(version<Math.abs(oldVersion)))
			{
				List<String> errors;
				try
				{
					errors = this.validateChanges(applicableChanges);
				}
				catch (final Exception e)
				{
					errors = new ArrayList<String>();
					e.printStackTrace();
				}
				final StringBuilder ester = new StringBuilder("");
				for(final String s : errors)
					ester.append(s).append(";");
				return "Unable to upgrade database version "+oldVersion+": "+ester.toString()+".  Please do so manually.";
			}
		}
		int oldVersion=version;
		for(++version;version<=changes.length-1;version++)
		{
			if(changes[version]==null)
				continue;
			if(changes[version].size() >0)
			{
				DBConnection conn=null;
				try
				{
					conn=DB.DBFetch();
					final DatabaseMetaData meta=conn.getMetaData();
					final String productName=meta.getDatabaseProductName().toLowerCase();
					final String schema = (productName.contains("oracle"))?conn.getSchema():null;
					final DDLGenerator ddlGen=new DDLGenerator(meta,schema);
					final List<String> sql=ddlGen.generateSQLForChanges(changes[version]);
					for(final String s : sql)
					{
						try
						{
							if(CMSecurity.isDebugging(DbgFlag.SQLERRORS))
								Log.debugOut(s);
							conn.update(s, 0);
						}
						catch (final SQLException e)
						{
							Log.errOut(e);
							return "Unable to upgrade to database version "+version+": "+e.getMessage()+".  Please do so manually.";
						}
					}
					DB.DBDone(conn);
					conn=null;
					final int newVersion=getDatabaseVersionCode(true);
					if((Math.abs(newVersion)==oldVersion)||(newVersion<0))
					{
						final List<String> errors = this.validateChanges(changes[version]);
						final StringBuilder str = new StringBuilder("");
						for(final String e : errors)
							str.append(e).append("; ");
						return "Failed to upgrade database version "+version+": "+str.toString()+".  Please do so manually.";
					}
					Log.sysOut("Database upgraded to version "+version);
					version=newVersion;
				}
				catch (final Exception e)
				{
					Log.errOut(e);
					return "Unable to upgrade to database version "+version+".  Please do so manually.";
				}
				finally
				{
					if(conn!=null)
						DB.DBDone(conn);
				}
				oldVersion=version;
			}
		}
		return null;
	}

}
