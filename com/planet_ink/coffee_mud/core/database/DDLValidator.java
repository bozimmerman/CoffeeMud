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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.CMParms;
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
	private final DBConnector DB;

	/**
	 * Constructs a DDLValidator with the specified DBConnector.
	 *
	 * @param DB the database connector
	 */
	public DDLValidator(final DBConnector DB)
	{
		this.DB=DB;
	}

	/**
	 * Current database version derived from the changelist.
	 *
	 * @param changesJson the changelist JSON object
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
			final ResultSet rs=meta.getTables(null, null, "%", new String[]{"TABLE"});
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
					String typeName=null;
					switch (type)
					{
					case java.sql.Types.INTEGER:
					case java.sql.Types.SMALLINT:
					case java.sql.Types.TINYINT:
						typeName="INT";
						break;
					case java.sql.Types.BIGINT:
						typeName="LONG";
						break;
					case java.sql.Types.VARCHAR:
					case java.sql.Types.CHAR:
						typeName="VARCHAR";
						break;
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
					final int size=colsRs.getInt("COLUMN_SIZE");
					final boolean nullable=colsRs.getString("IS_NULLABLE").equals("YES");
					if(typeName==null)
						throw new Exception("Unknown DB type "+typeName+" for column "+cname+" in table "+upperT);
					final JSONObject column=new JSONObject();
					column.put("name", cname);
					column.put("type", typeName);
					column.put("size", Long.valueOf(size));
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
				final Map<String, List<String>> indexGroups=new HashMap<>();
				while(idxRs.next())
				{
					final String idxName=idxRs.getString("INDEX_NAME");
					if(idxName==null||idxName.equals("PRIMARY"))
						continue;
					final String col=idxRs.getString("COLUMN_NAME").toUpperCase();
					indexGroups.computeIfAbsent(idxName, k -> new ArrayList<>()).add(col);
				}
				idxRs.close();
				final Set<String> dbIndexSets=new HashSet<>();
				for(final List<String> group : indexGroups.values())
				{
					Collections.sort(group);
					dbIndexSets.add(String.join(",", group));
				}
				dbIndexSetsByTable.put(upperT, dbIndexSets);
			}

			int startVer=DBInterface.currentVersion;
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
				if ((oneChgIdx!=null)&&(oneChgIdx.intValue() >= 0)&&(oneChgIdx.intValue()<list.size()))
				{
					if (validateChange(list.get(oneChgIdx.intValue()), dbTables, dbColumnsByTable, dbPrimaryKeysByTable, dbIndexSetsByTable, stringsAreBlobs))
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
				if(CMParms.contains(fieldGroups[g], dc.getCheckedString("type").toUpperCase()))
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
			if(dc.containsKey("size"))
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
						if(CMParms.contains(fieldGroups[g], dc.getCheckedString("type").toUpperCase()))
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
					if(dc.containsKey("size"))
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
				final String name=c.getCheckedString("name");
				final List<String> group=Arrays.asList(name.split(","));
				final List<String> g=new ArrayList<>();
				for(final String s : group)
					g.add(s.trim().toUpperCase());
				Collections.sort(g);
				final String idxStr=String.join(",", g);
				final boolean has=dbIdxs.contains(idxStr);
				if(action.equals("ADD")||action.equals("MODIFY"))
					return has;
				if(action.equals("DELETE"))
					return !has;
			}
		}
		return false;
	}

	/**
	 * Returns the changelists from the changelist JSON file.
	 *
	 * @param quiet true to suppress error messages
	 * @return the changelists, or null if unable to read/parse
	 */
	private List<JSONObject>[] getChangeLists(final boolean quiet)
	{
		final File f=new File("guides"+File.separatorChar+"database"+File.separatorChar+"changelist.json");
		if(!f.exists())
		{
			if(!quiet)
				Log.errOut("Unable to find database changelist for validation.");
			return null;
		}
		try
		{
			final String json=new String(Files.readAllBytes(f.toPath()));
			final MiniJSON.JSONObject obj=(MiniJSON.JSONObject)new MiniJSON().parse(json);
			@SuppressWarnings("unchecked")
			final List<JSONObject>[] allChanges=new List[DBInterface.currentVersion+1];
			for(int i=1;i<=DBInterface.currentVersion;i++)
			{
				if (!obj.containsKey(""+i))
				{
					allChanges[i]=new ArrayList<JSONObject>();
					continue;
				}
				final Object[] arr=obj.getCheckedArray(""+i);
				final List<JSONObject> changeSet=new ArrayList<JSONObject>();
				for (final Object oc : arr)
					changeSet.add((JSONObject)oc);
				allChanges[i]=changeSet;
			}
			return allChanges;
		}
		catch (final MJSONException e)
		{
			if(!quiet)
				Log.errOut("Unable to parse database changelist for validation.");
			return null;
		}
		catch (final IOException e)
		{
			if(!quiet)
				Log.errOut("Unable to read database changelist for validation.");
			return null;
		}
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
		final List<JSONObject>[] changes=this.getChangeLists(quiet);
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
				if(lastAppliedVersion<DBInterface.currentVersion)
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
	 * Validates the current database version against the changelist.
	 * @return null if valid, otherwise an error string
	 */
	public String validateDatabaseVersion()
	{
		final int lastAppliedVersion=getDatabaseVersionCode(false);
		if(lastAppliedVersion==Integer.MIN_VALUE)
			return "Unable to validate database version.  Startup aborted.";
		if(lastAppliedVersion>0)
		{
			if(lastAppliedVersion<DBInterface.currentVersion)
				return "Database is at version "+lastAppliedVersion+", but version "+DBInterface.currentVersion+" is required.";
			else
			if(lastAppliedVersion==0)
				return "Database was empty, and needs to be initialized to version "+DBInterface.currentVersion+".";
		}
		else
			return "Your database is at approximately version "+(-lastAppliedVersion)+", but version "+DBInterface.currentVersion+" is required.";
		return null;
	}

	/**
	 * Creates the database from scratch, using the changelist.
	 *
	 * @return null if successful, otherwise an error string
	 */
	private String createDatabase()
	{
		final List<JSONObject>[] changes=getChangeLists(true);
		if(changes==null)
			return "Unable to upgrade database: no change lists found.";
		DBConnection conn=null;
		try
		{
			conn=DB.DBFetch();
			final DatabaseMetaData meta=conn.getMetaData();
			final DDLGenerator ddlGen=new DDLGenerator(meta);
			final List<String> sql=ddlGen.getSchemaSQL(changes);
			for(final String s : sql)
			{
				try
				{
					conn.update(s, 0);
				}
				catch (final SQLException e)
				{
					Log.errOut(e);
					return "Unable to create to database: "+e.getMessage()+".  Please do so manually.";
				}
			}
		}
		catch (final Exception e)
		{
			Log.errOut(e);
			return "Unable to create database.  Please do so manually.";
		}
		finally
		{
			if (conn!=null)
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
		final List<JSONObject>[] changes=getChangeLists(true);
		if(changes==null)
			return "Unable to upgrade database: no change lists found.";
		int version=getDatabaseVersionCode(true);
		if(version==0)
			return createDatabase();
		if(version<0)
		{
			final int oldVersion=-version;
			final List<JSONObject> baseChanges=changes[-version];
			final List<JSONObject> applicableChanges=new LinkedList<JSONObject>();
			for (int c=0;c<baseChanges.size();c++)
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
			if (applicableChanges.size() >0)
			{
				DBConnection conn=null;
				try
				{
					conn=DB.DBFetch();
					final DatabaseMetaData meta=conn.getMetaData();
					final DDLGenerator ddlGen=new DDLGenerator(meta);
					final List<String> sql=ddlGen.generateSQLForChanges(applicableChanges);
					for(final String s : sql)
					{
						try
						{
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
					if (conn!=null)
						DB.DBDone(conn);
				}
				Log.sysOut("Database upgraded to version "+oldVersion);
			}
			version=getDatabaseVersionCode(true);
			if((version==-oldVersion)||(version<oldVersion))
				return "Unable to upgrade database version "+oldVersion+".  Please do so manually.";
		}
		int oldVersion=version;
		for(++version;version<=DBInterface.currentVersion;version++)
		{
			if (changes[version].size() >0)
			{
				DBConnection conn=null;
				try
				{
					conn=DB.DBFetch();
					final DatabaseMetaData meta=conn.getMetaData();
					final DDLGenerator ddlGen=new DDLGenerator(meta);
					final List<String> sql=ddlGen.generateSQLForChanges(changes[version]);
					for(final String s : sql)
					{
						try
						{
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
					if(Math.abs(newVersion)==oldVersion)
						return "Failed to upgrade database version "+version+".  Please do so manually.";
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
					if (conn!=null)
						DB.DBDone(conn);
				}
				oldVersion=version;
			}
		}
		return null;
	}

}
