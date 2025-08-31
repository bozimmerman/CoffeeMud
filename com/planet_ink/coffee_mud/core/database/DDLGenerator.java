package com.planet_ink.coffee_mud.core.database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
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
 * A utility class to generate basic DDL SQL statements (CREATE TABLE, ALTER TABLE, etc.)
 */
public class DDLGenerator
{
	private final DatabaseMetaData	metaData;
	private final String			productName;
	private final Map<String, String> typeMappings = new HashMap<>();

	public DDLGenerator(final DatabaseMetaData metaData) throws SQLException
	{
		this.metaData=metaData;
		this.productName=metaData.getDatabaseProductName().toLowerCase();
		final Map<String,Integer> dbTypes = new HashMap<>();
		dbTypes.put("INT", Integer.valueOf(java.sql.Types.INTEGER));
		dbTypes.put("LONG", Integer.valueOf(java.sql.Types.BIGINT));
		dbTypes.put("VARCHAR", Integer.valueOf(java.sql.Types.VARCHAR));
		dbTypes.put("TEXT", Integer.valueOf(java.sql.Types.CLOB));
		dbTypes.put("TIMESTAMP", Integer.valueOf(java.sql.Types.TIMESTAMP));
		dbTypes.put("DATETIME", Integer.valueOf(java.sql.Types.DATE));
		final ResultSet R = this.metaData.getTypeInfo();
		while (R.next())
		{
			final String typeName = R.getString("TYPE_NAME").toUpperCase();
			final int dataType = R.getInt("DATA_TYPE");
			for(final String key:dbTypes.keySet())
			{
				if(dbTypes.get(key).intValue()==dataType)
				{
					if(!typeMappings.containsKey(key))
						typeMappings.put(key, typeName);
				}
			}
		}
	}

	/**
	 * Generates DROP INDEX SQL statement
	 * @param indexName name of the index
	 * @param tableName name of the table
	 * @return The generated SQL string.
	 * @throws SQLException if an error occurs
	 */
	public String getDropIndexSQL(final String indexName, final String tableName) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();
		return "DROP INDEX "+quote+indexName+quote+" ON "+quote+tableName+quote+";";
	}


	/**
	 * Generates DROP PRIMARY KEY SQL statement
	 *
	 * @param tableName name of the table
	 * @return The generated SQL string
	 * @throws SQLException if an error occurs
	 */
	public String getDropPrimaryKeySQL(final String tableName) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();
		return "ALTER TABLE "+quote+tableName+quote+" DROP PRIMARY KEY;";
	}

	/**
	 * Generates CREATE TABLE SQL with support for column definitions including size, nullability, and primary keys
	 * @param tableName The name of the table
	 * @param columnDefs A list of Maps defining each column. Each map should contain:
	 *   - "name", "type", "size", "nullable", "isPrimaryKey"
	 * @return The generated SQL string.
	 */
	public String getCreateTableSQL(final String tableName, final List<Map<String, Object>> columnDefs) throws SQLException
	{
		final StringBuilder sql=new StringBuilder("CREATE TABLE ");
		final String quote=metaData.getIdentifierQuoteString();
		sql.append(quote).append(tableName).append(quote).append(" (");

		for(int i=0;i<columnDefs.size();i++)
		{
			final Map<String, Object> col=columnDefs.get(i);
			final String name=(String) col.get("name");
			final String type=(String) col.get("type");
			final Integer size=(Integer) col.get("size");
			final boolean nullable=col.containsKey("nullable")?((Boolean) col.get("nullable")).booleanValue():true;
			sql.append(quote).append(name).append(quote).append(" ");
			sql.append(getDataTypeSQL(type, size));
			if(!nullable)
				sql.append(" NOT NULL");
			if(i<(columnDefs.size()-1))
				sql.append(", ");
		}
		sql.append(");");
		return sql.toString();
	}

	/**
	 * Generates ADD COLUMN SQL with size and nullability.
	 * @param tableName The table name.
	 * @param colDef A map defining the column: "name" (String), "type" (String), "size" (Integer optional), "nullable" (Boolean default true)
	 */
	public String getAddColumnSQL(final String tableName, final Map<String, Object> colDef) throws SQLException
	{
		if(!metaData.supportsAlterTableWithAddColumn())
			throw new SQLException("Add column not supported");
		final String quote=metaData.getIdentifierQuoteString();
		final String addKeyword =(productName.contains("access"))?" COLUMN":"";
		final String name=(String)colDef.get("name");
		final String type=(String)colDef.get("type");
		final Integer size=(Integer)colDef.get("size");
		final boolean nullable=colDef.containsKey("nullable")?((Boolean)colDef.get("nullable")).booleanValue():true;
		final StringBuilder sql=new StringBuilder("ALTER TABLE ").append(quote).append(tableName).append(quote)
								.append(" ADD").append(addKeyword).append(" ").append(quote).append(name).append(quote).append(" ")
								.append(getDataTypeSQL(type, size));
		if(!nullable)
			sql.append(" NOT NULL");
		sql.append(";");
		return sql.toString();
	}

	/**
	 * Generates MODIFY COLUMN SQL with new size and nullability
	 * Note: Modifying nullability or size may have restrictions in some DBs (e.g., can't reduce size if data exists)
	 */
	public String getModifyColumnSQL(final String tableName, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();
		String modifyClause;
		if(productName.contains("mysql") || productName.contains("oracle"))
			modifyClause=" MODIFY COLUMN ";
		else
			modifyClause=" ALTER COLUMN ";
		final StringBuilder sql=new StringBuilder("ALTER TABLE ").append(quote).append(tableName).append(quote)
							.append(modifyClause).append(quote).append(columnName).append(quote).append(" ")
							.append(getDataTypeSQL(newType, newSize));
		if(newNullable!=null)
		{
			if(!newNullable.booleanValue())
				sql.append(" NOT NULL");
			else
			if(supportsNullabilityChange())
				sql.append(" NULL");
		}
		sql.append(";");
		return sql.toString();
	}

	/**
	 * Generates ALTER TABLE ADD PRIMARY KEY SQL
	 * @param tableName The table name
	 * @param columnNames List of column names for the primary key (composite if multiple)
	 */
	public String getAddPrimaryKeySQL(final String tableName, final List<String> columnNames) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();
		final StringBuilder sql=new StringBuilder("ALTER TABLE ").append(quote)
				.append(tableName).append(quote).append(" ADD PRIMARY KEY (");
		for(int i=0;i<columnNames.size();i++)
		{
			sql.append(quote).append(columnNames.get(i)).append(quote);
			if(i<(columnNames.size()-1))
				sql.append(", ");
		}
		sql.append(");");
		if(productName.contains("oracle") || productName.contains("db2"))
			sql.insert(21+tableName.length(), " CONSTRAINT PK_"+tableName);
		return sql.toString();
	}

	/**
	 * Generates CREATE INDEX SQL.
	 * @param indexName The name of the index
	 * @param tableName The table name
	 * @param columnNames List of columns to index
	 * @param isUnique Whether the index is unique
	 */
	public String getCreateIndexSQL(final String indexName, final String tableName, final List<String> columnNames, final boolean isUnique) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();
		final StringBuilder sql=new StringBuilder("CREATE ");
		if(isUnique)
			sql.append("UNIQUE ");
		sql.append("INDEX ").append(quote).append(indexName).append(quote).append(" ON ")
			.append(quote).append(tableName).append(quote).append(" (");
		for(int i=0;i<columnNames.size();i++)
		{
			sql.append(quote).append(columnNames.get(i)).append(quote);
			if(i<(columnNames.size()-1))
				sql.append(", ");
		}
		sql.append(");");
		return sql.toString();
	}

	/**
	 * Maps a portable type to a database-specific type, adding size if
	 * applicable.
	 *
	 * @param type The portable type (e.g., "VARCHAR", "INT")
	 * @param size The size for types that require it (e.g., VARCHAR(255))
	 * @return The database-specific type string
	 */
	private String getDataTypeSQL(final String type, final Integer size) throws SQLException
	{
		final String baseType=mapPortableType(type);
		if((baseType.equalsIgnoreCase("CHAR") || baseType.equalsIgnoreCase("VARCHAR")) && (size!=null))
			return baseType+"("+size+")";
		return baseType;
	}

	/**
	 * Maps a portable type to a database-specific type. Extend this method to
	 * handle more types and databases as needed.
	 *
	 * @param portableType The portable type string
	 * @return The mapped database-specific type string
	 */
	private String mapPortableType(final String portableType)
	{
		return this.typeMappings.getOrDefault(portableType.toUpperCase(), portableType);
	}

	/**
	 * Determines if the database supports changing column nullability.
	 *
	 * @return true if nullability changes are supported, false otherwise
	 */
	private boolean supportsNullabilityChange()
	{
		return true;
	}

	/**
	 * Generates DROP COLUMN SQL statement
	 *
	 * @param tableName The table name
	 * @param columnName The column name to drop
	 * @return The generated SQL string.
	 * @throws SQLException if an error occurs or if dropping columns is not
	 *			 supported
	 */
	public String getDropColumnSQL(final String tableName, final String columnName) throws SQLException
	{
		if(!metaData.supportsAlterTableWithDropColumn())
			throw new SQLException("Drop column not supported");
		final String quote=metaData.getIdentifierQuoteString();
		return "ALTER TABLE "+quote+tableName+quote+" DROP COLUMN "+quote+columnName+quote+";";
	}

	/**
	 * Generates DROP TABLE SQL statement
	 *
	 * @param tableName The table name to drop
	 * @return The generated SQL string.
	 * @throws SQLException if an error occurs
	 */
	public String getDropTableSQL(final String tableName) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();
		return "DROP TABLE "+quote+tableName+quote+";";
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
			this.name = name;
		}
	}

	/**
	 * Given an array of schema versions (each a list of actions), computes the
	 * final schema and generates the SQL statements to create it from scratch.
	 *
	 * @param versions Array of schema versions, each a list of action maps
	 * @return List of SQL statements to create the final schema
	 * @throws Exception if an error occurs during processing
	 */
	public List<String> getSchemaSQL(final List<JSONObject>[] versions) throws Exception
	{
		final List<JSONObject> finalSchema = getFinalSchema(versions);
		return generateSQLForChanges(finalSchema);
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
		final Map<String, DDLTable> schema = new HashMap<>();
		final List<String> tableOrder = new ArrayList<>();

		for(final List<JSONObject> version:versions)
		{
			for(final JSONObject action:version)
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
						final JSONObject props = new JSONObject();
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
						final Map<String, Object> props = schema.get(table).columns.get(col);
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
						JSONObject props = schema.get(from).columns.remove(col);
						if(props==null)
							props = new JSONObject();
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

		final List<JSONObject> result = new ArrayList<>();
		for(final String t : tableOrder)
		{
			final DDLTable tab = schema.get(t);
			if(tab==null)
				continue;
			for(final Map.Entry<String, JSONObject> entry:tab.columns.entrySet())
			{
				final String col = entry.getKey();
				final JSONObject props = entry.getValue();
				final JSONObject addCol = new JSONObject();
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
			final JSONObject addTable = new JSONObject();
			addTable.put("action", "ADD");
			addTable.put("target", "TABLE");
			addTable.put("name", tab.name);
			result.add(addTable);
		}
		return result;
	}


	/**
	 * Generates SQL statements for a list of schema changes. Each change is
	 * represented as a JSONObject with keys: - action: "ADD", "MODIFY",
	 * "DELETE", "MOVE" - target: "TABLE", "COLUMN", "KEY", "INDEX" - Additional
	 * keys depending on action and target
	 *
	 * @param changes List of JSONObjects representing schema changes
	 * @return List of generated SQL statements
	 * @throws Exception if an error occurs during SQL generation
	 */
	public List<String> generateSQLForChanges(final List<JSONObject> changes) throws Exception
	{
		final List<String> sqls = new ArrayList<>();
		final Set<JSONObject> processed = new HashSet<>();
		final Map<String, List<String>> pkAddsByTable = new HashMap<>();
		final String quote = metaData.getIdentifierQuoteString();
		for(final JSONObject change:changes)
		{
			if(processed.contains(change))
				continue;
			final String action = change.getCheckedString("action").toUpperCase();
			final String target = change.getCheckedString("target").toUpperCase();
			if(action.equals("ADD") && target.equals("TABLE"))
			{
				final String tableName = change.getCheckedString("name").toUpperCase();
				final List<Map<String, Object>> columnDefs = new ArrayList<>();
				final List<JSONObject> moveChanges = new ArrayList<>();
				final List<String> pkCols = new ArrayList<>();
				final List<JSONObject> indexChanges = new ArrayList<>();
				for(final JSONObject c:changes)
				{
					if(processed.contains(c) || (c==change))
						continue;
					final String ca = c.getCheckedString("action").toUpperCase();
					final String ct = c.getCheckedString("target").toUpperCase();
					String ctable = c.containsKey("table")?c.getCheckedString("table").toUpperCase():"";
					if(ca.equals("MOVE"))
						ctable = c.getCheckedString("to_table").toUpperCase();
					if(ctable.equals(tableName))
					{
						processed.add(c);
						if(ct.equals("COLUMN"))
						{
							final Map<String, Object> colDef = new HashMap<>();
							colDef.put("name", c.getCheckedString("name").toUpperCase());
							colDef.put("type", c.getCheckedString("type").toUpperCase());
							if(c.containsKey("size"))
								colDef.put("size", Integer.valueOf(c.getCheckedLong("size").intValue()));
							colDef.put("nullable", Boolean.TRUE); // assume
							columnDefs.add(colDef);
							if(ca.equals("MOVE"))
								moveChanges.add(c);
						}
						else
						if(ct.equals("KEY") && ca.equals("ADD"))
						{
							pkCols.add(c.getCheckedString("name").toUpperCase());
						}
						else
						if(ct.equals("INDEX") && ca.equals("ADD"))
						{
							indexChanges.add(c);
						}
					}
				}
				sqls.add(getCreateTableSQL(tableName, columnDefs));
				if(!pkCols.isEmpty())
					sqls.add(getAddPrimaryKeySQL(tableName, pkCols));
				// handle data moves if any
				if(!moveChanges.isEmpty())
				{
					final JSONObject firstMove = moveChanges.get(0);
					final String fromTable = firstMove.getCheckedString("from_table").toUpperCase();
					for(final JSONObject m:moveChanges)
					{
						if(!m.getCheckedString("from_table").toUpperCase().equals(fromTable))
							throw new Exception("Multiple from_tables for moves to new table "+tableName);
					}
					final ResultSet pkRs = metaData.getPrimaryKeys(null, null, fromTable);
					final List<String> fromPkCols = new ArrayList<>();
					while(pkRs.next())
					{
						fromPkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
					}
					pkRs.close();
					if(fromPkCols.isEmpty())
					{
						final ResultSet idxs = metaData.getIndexInfo(null, null, fromTable, true, true);
						while(idxs.next())
						{
							fromPkCols.add(idxs.getString("COLUMN_NAME").toUpperCase());
						}
						idxs.close();
						if(fromPkCols.isEmpty())
							throw new Exception("No primary key found for from_table "+fromTable);
					}
					final StringBuilder insertCols = new StringBuilder();
					final StringBuilder selectCols = new StringBuilder();
					for(final String pk:fromPkCols)
					{
						insertCols.append(quote).append(pk).append(quote).append(",");
						selectCols.append(quote).append(pk).append(quote).append(",");
					}
					for(final JSONObject m:moveChanges)
					{
						final String col = m.getCheckedString("name").toUpperCase();
						insertCols.append(quote).append(col).append(quote).append(",");
						selectCols.append(quote).append(col).append(quote).append(",");
					}
					if(insertCols.length()>0)
						insertCols.setLength(insertCols.length()-1);
					if(selectCols.length()>0)
						selectCols.setLength(selectCols.length()-1);
					final String transferSql = "INSERT INTO "+quote+tableName+quote+" ("+insertCols+") SELECT "+selectCols+" FROM "+quote+fromTable+quote+";";
					sqls.add(transferSql);
					for(final JSONObject m:moveChanges)
					{
						sqls.add(getDropColumnSQL(fromTable, m.getCheckedString("name").toUpperCase()));
					}
				}
				// add indexes
				for(final JSONObject idx:indexChanges)
				{
					final String name = idx.getCheckedString("name");
					final String idxName = tableName+"_IDX_"+name.replaceAll("[^A-Z0-9]","_");
					final List<String> group = Arrays.asList(name.split(","));
					final List<String> cols = new ArrayList<>();
					for(final String s:group)
						cols.add(s.trim().toUpperCase());
					sqls.add(getCreateIndexSQL(idxName, tableName, cols, false));
				}
				continue;
			}
			String table = change.containsKey("table")?change.getCheckedString("table").toUpperCase():null;
			if((table==null) && target.equals("TABLE"))
				table = change.getCheckedString("name").toUpperCase();
			if(action.equals("ADD") && target.equals("COLUMN"))
			{
				final Map<String, Object> colDef = new HashMap<>();
				colDef.put("name", change.getCheckedString("name").toUpperCase());
				colDef.put("type", change.getCheckedString("type").toUpperCase());
				if(change.containsKey("size"))
					colDef.put("size", Integer.valueOf(change.getCheckedLong("size").intValue()));
				colDef.put("nullable", Boolean.TRUE);
				sqls.add(getAddColumnSQL(table, colDef));
			}
			else
			if(action.equals("MODIFY") && target.equals("COLUMN"))
			{
				final String col = change.getCheckedString("name").toUpperCase();
				final String type = change.getCheckedString("type").toUpperCase();
				final Integer size = change.containsKey("size")?Integer.valueOf(change.getCheckedLong("size").intValue()):null;
				final Boolean nullable = change.containsKey("nullable")?Boolean.valueOf(change.getCheckedBoolean("nullable").booleanValue()):null;
				sqls.add(getModifyColumnSQL(table, col, type, size, nullable));
			}
			else
			if(action.equals("DELETE") && target.equals("COLUMN"))
			{
				final String col = change.getCheckedString("name").toUpperCase();
				sqls.add(getDropColumnSQL(table, col));
			}
			else
			if(action.equals("DELETE") && target.equals("TABLE"))
			{
				sqls.add(getDropTableSQL(table));
			}
			else
			if(action.equals("ADD") && target.equals("KEY"))
			{
				final String col = change.getCheckedString("name").toUpperCase();
				pkAddsByTable.computeIfAbsent(table, k -> new ArrayList<>()).add(col);
			}
			else
			if(action.equals("DELETE") && target.equals("KEY"))
			{
				sqls.add(getDropPrimaryKeySQL(table));
			}
			else
			if(action.equals("ADD") && target.equals("INDEX"))
			{
				final String name = change.getCheckedString("name");
				final String idxName = table+"_IDX_"+name.replaceAll("[^A-Z0-9]","_");
				final List<String> group = Arrays.asList(name.split(","));
				final List<String> cols = new ArrayList<>();
				for(final String s:group)
					cols.add(s.trim().toUpperCase());
				sqls.add(getCreateIndexSQL(idxName, table, cols, false));
			}
			else
			if(action.equals("DELETE") && target.equals("INDEX"))
			{
				final String name = change.getCheckedString("name");
				final String idxName = table+"_IDX_"+name.replaceAll("[^A-Z0-9]","_");
				sqls.add(getDropIndexSQL(idxName, table));
			}
			else
			if(action.equals("MOVE") && target.equals("COLUMN"))
			{
				final String toTable = change.getCheckedString("to_table").toUpperCase();
				final String fromTable = change.getCheckedString("from_table").toUpperCase();
				final String col = change.getCheckedString("name").toUpperCase();
				final String type = change.getCheckedString("type").toUpperCase();
				final Integer size = change.containsKey("size")?Integer.valueOf(change.getCheckedLong("size").intValue()):null;
				final Map<String, Object> colDef = new HashMap<>();
				colDef.put("name", col);
				colDef.put("type", type);
				if(size!=null)
					colDef.put("size", size);
				colDef.put("nullable", Boolean.TRUE);
				sqls.add(getAddColumnSQL(toTable, colDef));
				final ResultSet pkRs = metaData.getPrimaryKeys(null, null, fromTable);
				final List<String> fromPkCols = new ArrayList<>();
				while(pkRs.next())
					fromPkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
				pkRs.close();
				if(fromPkCols.isEmpty())
				{
					final ResultSet idxs = metaData.getIndexInfo(null, null, fromTable, true, true);
					while(idxs.next())
					{
						fromPkCols.add(idxs.getString("COLUMN_NAME").toUpperCase());
					}
					idxs.close();
					if(fromPkCols.isEmpty())
					{
						throw new Exception("No primary key found for from_table "+fromTable);
					}
				}
				final StringBuilder insertCols = new StringBuilder();
				final StringBuilder selectCols = new StringBuilder();
				for(final String pk:fromPkCols)
				{
					insertCols.append(quote).append(pk).append(quote).append(",");
					selectCols.append(quote).append(pk).append(quote).append(",");
				}
				insertCols.append(quote).append(col).append(quote);
				selectCols.append(quote).append(col).append(quote);
				final String transferSql = "INSERT INTO "+quote+toTable+quote+" ("+insertCols+") SELECT "+selectCols+" FROM "+quote+fromTable+quote+";";
				sqls.add(transferSql);
				sqls.add(getDropColumnSQL(fromTable, col));
			}
		}
		for(final String table : pkAddsByTable.keySet())
		{
			final List<String> cols = pkAddsByTable.get(table);
			if(!cols.isEmpty())
				sqls.add(getAddPrimaryKeySQL(table, cols));
		}
		return sqls;
	}
}
