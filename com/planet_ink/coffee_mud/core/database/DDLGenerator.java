package com.planet_ink.coffee_mud.core.database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private final DatabaseMetaData		metaData;
	private final String				productName;
	private final String				schema;
	private final Map<String, String>	typeMappings		= new HashMap<>();
	private final Map<Integer, String>	sqlTypeToPortable	= new HashMap<>();

	/**
	 * Constructor
	 *
	 * @param metaData the database metadata object
	 * @param schema the database schema name, or null
	 * @throws SQLException if an error occurs
	 */
	public DDLGenerator(final DatabaseMetaData metaData, final String schema) throws SQLException
	{
		this.metaData=metaData;
		this.schema=schema;
		this.productName=metaData.getDatabaseProductName().toLowerCase();
		if (this.productName.contains("oracle"))
			typeMappings.put("TEXT", "CLOB");
		final Map<String,Integer> dbTypes = new HashMap<>();
		dbTypes.put("INT", Integer.valueOf(java.sql.Types.INTEGER));
		dbTypes.put("LONG", Integer.valueOf(java.sql.Types.BIGINT));
		dbTypes.put("VARCHAR", Integer.valueOf(java.sql.Types.VARCHAR));
		dbTypes.put("TEXT", Integer.valueOf(java.sql.Types.CLOB));
		dbTypes.put("TIMESTAMP", Integer.valueOf(java.sql.Types.TIMESTAMP));
		dbTypes.put("DATETIME", Integer.valueOf(java.sql.Types.DATE));
		for (final Map.Entry<String, Integer> entry : dbTypes.entrySet())
			if(!sqlTypeToPortable.containsKey(entry.getValue()))
				sqlTypeToPortable.put(entry.getValue(), entry.getKey());
		final ResultSet R = this.metaData.getTypeInfo();
		while (R.next())
		{
			final String typeName = R.getString("TYPE_NAME").toUpperCase();
			final int dataType = R.getInt("DATA_TYPE");
			final int precision = R.getInt("PRECISION");
			final String portableTypeName = DDLValidator.getPortableType(dataType, typeName, precision);
			if(portableTypeName != null)
			{
				if(!typeMappings.containsKey(portableTypeName) || typeName.equalsIgnoreCase(portableTypeName))
					typeMappings.put(portableTypeName, typeName);
				if (!sqlTypeToPortable.containsKey(Integer.valueOf(dataType)))
					sqlTypeToPortable.put(Integer.valueOf(dataType), portableTypeName);
			}
			else
			{
				for (final String key : dbTypes.keySet())
				{
					if (dbTypes.get(key).intValue() == dataType)
					{
						if (!typeMappings.containsKey(key))
							typeMappings.put(key, typeName);
					}
				}
			}
		}
		R.close();
		if (this.productName.contains("db2"))
		{
			typeMappings.put("TEXT", "LONG VARCHAR");
		}
		if (this.productName.contains("oracle"))
		{
			typeMappings.put("INT", "INTEGER");
			typeMappings.put("LONG", "NUMBER(20,0)");
			typeMappings.put("VARCHAR", "VARCHAR2");
			typeMappings.put("TEXT", "CLOB");
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
		final StringBuilder sql = new StringBuilder("DROP INDEX ").append(quote).append(indexName).append(quote);
		if((!productName.contains("derby"))
		&&(!productName.contains("hsql"))
		&&(!productName.contains("oracle"))
		&&(!productName.contains("db2")))
			 sql.append(" ON ").append(quote).append(tableName).append(quote);
		return sql.toString();
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
		final ResultSet pkRs = metaData.getPrimaryKeys(null, null, tableName);
		String pkName = null;
		while (pkRs.next())
		{
			pkName = pkRs.getString("PK_NAME");
			if(pkName != null)
				break;
		}
		pkRs.close();
		final String quote = metaData.getIdentifierQuoteString();
		if(pkName != null)
		{
			return "ALTER TABLE " + quote + tableName + quote + " DROP CONSTRAINT " + quote + pkName + quote + "";
		}
		else
		{
			return "ALTER TABLE " + quote + tableName + quote + " DROP PRIMARY KEY";
		}
	}

	/**
	 * Generates CREATE TABLE SQL with support for column definitions including size, nullability, and primary keys
	 * @param tableName The name of the table
	 * @param columnDefs A list of Maps defining each column. Each map should contain:
	 *   - "name", "type", "size", "nullable", "isPrimaryKey"
	 * @return The generated SQL string.
	 * @throws SQLException if an error occurs
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
			else
			if(productName.equals("fakedb") && (!col.containsKey("nullable")))
				sql.append(" NULL");
			if(i<(columnDefs.size()-1))
				sql.append(", ");
		}
		sql.append(")");
		return sql.toString();
	}

	/**
	 * Generates ADD COLUMN SQL with size and nullability.
	 * @param tableName The table name.
	 * @param colDef A map defining the column: "name" (String), "type" (String), "size" (Integer optional), "nullable" (Boolean default true)
	 * @return The generated SQL string.
	 * @throws SQLException if an error occurs or if ADD COLUMN is not supported.
	 */
	public String getAddColumnSQL(final String tableName, final Map<String, Object> colDef) throws SQLException
	{
		if(!metaData.supportsAlterTableWithAddColumn())
			throw new SQLException("Add column not supported");
		final String quote=metaData.getIdentifierQuoteString();
		final String addKeyword =(requiresColumnKeywordInAdd())?" COLUMN":"";
		final String name=(String)colDef.get("name");
		final String type=(String)colDef.get("type");
		final Integer size=(Integer)colDef.get("size");
		final boolean nullable=colDef.containsKey("nullable")?((Boolean)colDef.get("nullable")).booleanValue():true;
		final StringBuilder sql=new StringBuilder("ALTER TABLE ").append(quote).append(tableName).append(quote)
								.append(" ADD").append(addKeyword).append(" ").append(quote).append(name).append(quote).append(" ")
								.append(getDataTypeSQL(type, size));
		if(!nullable)
		{
			sql.append(" NOT NULL");
			if(productName.contains("db2"))
			{
				String defaultVal = " DEFAULT ''"; // for VARCHAR/TEXT
				if (type.equalsIgnoreCase("INT") || type.equalsIgnoreCase("LONG"))
					defaultVal = " DEFAULT 0";
				else
				if (type.equalsIgnoreCase("DOUBLE") || type.equalsIgnoreCase("FLOAT"))
					defaultVal = " DEFAULT 0.0";
				else
				if (type.equalsIgnoreCase("TIMESTAMP") || type.equalsIgnoreCase("DATETIME"))
					defaultVal = " DEFAULT CURRENT TIMESTAMP";
				sql.append(defaultVal);
			}
		}
		sql.append("");
		return sql.toString();
	}

	private boolean requiresColumnKeywordInAdd()
	{
		return productName.contains("postgre")
			|| productName.contains("mysql")
			|| productName.contains("derby")
			|| productName.contains("fakedb")
			|| productName.contains("hsql")
			|| productName.contains("access");
	}

	/**
	 * Returns the Postgres-specific SQL for a MODIFY COLUMN operation.
	 *
	 * @param actualTable the table name
	 * @param columnName the column name
	 * @param newType the new type (or null to keep existing)
	 * @param newSize the new size (or null to keep existing)
	 * @param newNullable the new nullability (or null to keep existing)
	 * @return the list of SQL statements to run
	 * @throws SQLException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public List<String> getModifyColumnSQLPostgreSQL(final String actualTable, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final List<String> sqls = new ArrayList<String>();
		final String quote = metaData.getIdentifierQuoteString();
		final String tempColumnName = columnName + "_TEMP";

		String oldType = "";
		String oldPortableType = "";
		int oldSize = -1;
		boolean oldNullable = true;
		final ResultSet colRs = metaData.getColumns(null, null, actualTable, columnName);
		if(colRs.next())
		{
			oldType = colRs.getString("TYPE_NAME").toUpperCase();
			oldSize = colRs.getInt("COLUMN_SIZE");
			oldNullable = "YES".equals(colRs.getString("IS_NULLABLE"));
			final int oldDataType = colRs.getInt("DATA_TYPE");
			oldPortableType = sqlTypeToPortable.getOrDefault(Integer.valueOf(oldDataType), oldType);
		}
		colRs.close();

		final String effectivePortableType = (newType != null) ? newType : oldPortableType;
		final String effectiveNewType = (newType != null) ? mapPortableToLocalType(newType) : oldType;
		final Integer effectiveNewSizeInt = (newSize != null) ? newSize : Integer.valueOf(oldSize);
		final int effectiveNewSize = effectiveNewSizeInt.intValue();
		final Boolean effectiveNewNullableBool = (newNullable != null) ? newNullable : Boolean.valueOf(oldNullable);
		final boolean effectiveNewNullable = effectiveNewNullableBool.booleanValue();
		final List<String> pkCols = new ArrayList<String>();
		String pkName = null;
		final ResultSet pkRs = metaData.getPrimaryKeys(null, null, actualTable);
		while (pkRs.next())
		{
			pkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
			if(pkName == null) pkName = pkRs.getString("PK_NAME");
		}
		pkRs.close();
		final boolean isPkColumn = pkCols.contains(columnName.toUpperCase());
		final Map<String, Map<String, Object>> indexes = new LinkedHashMap<String, Map<String, Object>>();
		final ResultSet idxRs = metaData.getIndexInfo(null, null, actualTable, false, false);
		while (idxRs.next())
		{
			final short type = idxRs.getShort("TYPE");
			if(type == DatabaseMetaData.tableIndexStatistic)
				continue;
			final String indexName = idxRs.getString("INDEX_NAME");
			if((indexName == null)
			||((pkName != null)
				&&(indexName.equals(pkName))||(indexName.startsWith("SYS_IDX_SYS_PK_"))))
					continue;
			final boolean nonUnique = idxRs.getBoolean("NON_UNIQUE");
			final String colName = idxRs.getString("COLUMN_NAME");
			if(colName == null)
				continue;
			final short ordinal = idxRs.getShort("ORDINAL_POSITION");
			final Map<String, Object> index = indexes.computeIfAbsent(indexName, k ->
			{
				final Map<String, Object> m = new HashMap<String, Object>();
				m.put("unique", Boolean.valueOf(!nonUnique));
				m.put("columns", new ArrayList<Map<String, Object>>());
				return m;
			});
			final Map<String, Object> colMap = new HashMap<String, Object>();
			colMap.put("pos", Short.valueOf(ordinal));
			colMap.put("col", colName.toUpperCase());
			((List<Map<String, Object>>) index.get("columns")).add(colMap);
		}
		idxRs.close();
		final List<Map<String, Object>> depIndexes = new ArrayList<Map<String, Object>>();
		for (final Map.Entry<String, Map<String, Object>> entry : indexes.entrySet())
		{
			final String indexName = entry.getKey();
			final Map<String, Object> index = entry.getValue();
			final List<Map<String, Object>> cols = (List<Map<String, Object>>) index.get("columns");
			boolean depends = false;
			for (final Map<String, Object> c : cols)
			{
				if(((String) c.get("col")).equals(columnName.toUpperCase()))
				{
					depends = true;
					break;
				}
			}
			if(depends)
			{
				final Map<String, Object> dep = new HashMap<String, Object>();
				dep.put("name", indexName);
				dep.put("unique", index.get("unique"));
				cols.sort((a, b) -> ((Short) a.get("pos")).compareTo((Short) b.get("pos")));
				final List<String> colList = new ArrayList<String>();
				for (final Map<String, Object> c : cols)
					colList.add((String) c.get("col"));
				dep.put("columns", colList);
				depIndexes.add(dep);
			}
		}
		if(isPkColumn)
		{
			String dropSql;
			if(pkName != null)
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP CONSTRAINT " + quote + pkName + quote;
			else
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP PRIMARY KEY";
			sqls.add(dropSql);
		}
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			sqls.add(getDropIndexSQL(indexName, actualTable));
		}
		final String addTempSql = "ALTER TABLE " + quote + actualTable + quote + " ADD COLUMN " + quote + tempColumnName + quote + " "
								  + getDataTypeSQL(effectivePortableType, effectiveNewSizeInt);
		sqls.add(addTempSql);
		String copyExpr = quote + columnName + quote;
		if((effectiveNewSize < oldSize) && "VARCHAR".equalsIgnoreCase(effectiveNewType))
			copyExpr = "SUBSTRING(" + copyExpr + ", 1, " + effectiveNewSize + ")";
		final String copySql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = " + copyExpr;
		sqls.add(copySql);
		if(!effectiveNewNullable)
		{
			final String replaceNullsSql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = '' WHERE " + quote + tempColumnName + quote + " IS NULL";
			sqls.add(replaceNullsSql);
			final String setNotNullSql = "ALTER TABLE " + quote + actualTable + quote + " ALTER COLUMN " + quote + tempColumnName + quote + " SET NOT NULL";
			sqls.add(setNotNullSql);
		}
		sqls.add("ALTER TABLE " + quote + actualTable + quote + " DROP COLUMN " + quote + columnName + quote);
		final String renameSql = "ALTER TABLE " + quote + actualTable + quote + " RENAME COLUMN " + quote + tempColumnName + quote + " TO " + quote + columnName + quote;
		sqls.add(renameSql);
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			final List<String> cols = (List<String>) idx.get("columns");
			final boolean unique = ((Boolean) idx.get("unique")).booleanValue();
			sqls.add(getCreateIndexSQL(indexName, actualTable, cols, unique));
		}
		if(isPkColumn)
		{
			boolean allNotNull = true;
			for (final String pkCol : pkCols)
			{
				String isNullableStr;
				if(pkCol.equals(columnName.toUpperCase()))
					isNullableStr = effectiveNewNullable ? "YES" : "NO";
				else
				{
					final ResultSet pkColRs = metaData.getColumns(null, null, actualTable, pkCol);
					isNullableStr = pkColRs.next() ? pkColRs.getString("IS_NULLABLE") : "YES";
					pkColRs.close();
				}
				if("YES".equals(isNullableStr))
				{
					allNotNull = false;
					break;
				}
			}
			Collections.sort(pkCols);
			if(allNotNull)
				sqls.add(getAddPrimaryKeySQL(actualTable, pkCols, null));
			else
				sqls.add(getCreateIndexSQL("UNQ_" + actualTable.replaceAll("[^A-Z0-9]","_"), actualTable, pkCols, true));
		}
		return sqls;
	}


	/**
	 * Returns the HSQL-specific SQL for a MODIFY COLUMN operation.
	 *
	 * @param actualTable the table name
	 * @param columnName the column name
	 * @param newType the new type (or null to keep existing)
	 * @param newSize the new size (or null to keep existing)
	 * @param newNullable the new nullability (or null to keep existing)
	 * @return the list of SQL statements to run
	 * @throws SQLException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public List<String> getModifyColumnSQLHSQLDB(final String actualTable, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final List<String> sqls = new ArrayList<String>();
		final String quote = metaData.getIdentifierQuoteString();
		final String tempColumnName = columnName + "_TEMP";

		String oldPortableType = "";
		String oldType = "";
		int oldSize = -1;
		boolean oldNullable = true;
		final ResultSet colRs = metaData.getColumns(null, null, actualTable, columnName);
		if(colRs.next())
		{
			oldType = colRs.getString("TYPE_NAME").toUpperCase();
			oldSize = colRs.getInt("COLUMN_SIZE");
			oldNullable = "YES".equals(colRs.getString("IS_NULLABLE"));
			final int oldDataType = colRs.getInt("DATA_TYPE");
			oldPortableType = sqlTypeToPortable.getOrDefault(Integer.valueOf(oldDataType), oldType);
		}
		colRs.close();

		final String effectivePortableType = (newType != null) ? newType : oldPortableType;
		final String effectiveNewType = (newType != null) ? mapPortableToLocalType(newType) : oldType;
		final Integer effectiveNewSizeInt = (newSize != null) ? newSize : Integer.valueOf(oldSize);
		final int effectiveNewSize = effectiveNewSizeInt.intValue();
		final Boolean effectiveNewNullableBool = (newNullable != null) ? newNullable : Boolean.valueOf(oldNullable);
		final boolean effectiveNewNullable = effectiveNewNullableBool.booleanValue();
		final List<String> pkCols = new ArrayList<String>();
		String pkName = null;
		final ResultSet pkRs = metaData.getPrimaryKeys(null, null, actualTable);
		while (pkRs.next())
		{
			pkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
			if(pkName == null) pkName = pkRs.getString("PK_NAME");
		}
		pkRs.close();
		final boolean isPkColumn = pkCols.contains(columnName.toUpperCase());
		final Map<String, Map<String, Object>> indexes = new LinkedHashMap<String, Map<String, Object>>();
		final ResultSet idxRs = metaData.getIndexInfo(null, null, actualTable, false, false);
		while (idxRs.next())
		{
			final short type = idxRs.getShort("TYPE");
			if(type == DatabaseMetaData.tableIndexStatistic)
				continue;
			final String indexName = idxRs.getString("INDEX_NAME");
			if((indexName == null)
			||((pkName != null)
				&&(indexName.equals(pkName))||(indexName.startsWith("SYS_IDX_SYS_PK_"))))
					continue;
			final boolean nonUnique = idxRs.getBoolean("NON_UNIQUE");
			final String colName = idxRs.getString("COLUMN_NAME");
			if(colName == null)
				continue;
			final short ordinal = idxRs.getShort("ORDINAL_POSITION");
			final Map<String, Object> index = indexes.computeIfAbsent(indexName, k ->
			{
				final Map<String, Object> m = new HashMap<String, Object>();
				m.put("unique", Boolean.valueOf(!nonUnique));
				m.put("columns", new ArrayList<Map<String, Object>>());
				return m;
			});
			final Map<String, Object> colMap = new HashMap<String, Object>();
			colMap.put("pos", Short.valueOf(ordinal));
			colMap.put("col", colName.toUpperCase());
			((List<Map<String, Object>>) index.get("columns")).add(colMap);
		}
		idxRs.close();
		final List<Map<String, Object>> depIndexes = new ArrayList<Map<String, Object>>();
		for (final Map.Entry<String, Map<String, Object>> entry : indexes.entrySet())
		{
			final String indexName = entry.getKey();
			final Map<String, Object> index = entry.getValue();
			final List<Map<String, Object>> cols = (List<Map<String, Object>>) index.get("columns");
			boolean depends = false;
			for (final Map<String, Object> c : cols)
			{
				if(((String) c.get("col")).equals(columnName.toUpperCase()))
				{
					depends = true;
					break;
				}
			}
			if(depends)
			{
				final Map<String, Object> dep = new HashMap<String, Object>();
				dep.put("name", indexName);
				dep.put("unique", index.get("unique"));
				cols.sort((a, b) -> ((Short) a.get("pos")).compareTo((Short) b.get("pos")));
				final List<String> colList = new ArrayList<String>();
				for (final Map<String, Object> c : cols)
					colList.add((String) c.get("col"));
				dep.put("columns", colList);
				depIndexes.add(dep);
			}
		}
		if(isPkColumn)
		{
			String dropSql;
			if(pkName != null)
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP CONSTRAINT " + quote + pkName + quote;
			else
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP PRIMARY KEY";
			sqls.add(dropSql);
		}
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			sqls.add(getDropIndexSQL(indexName, actualTable));
		}
		final String addTempSql = "ALTER TABLE " + quote + actualTable + quote + " ADD COLUMN " + quote + tempColumnName + quote + " "
								  + getDataTypeSQL(effectivePortableType, effectiveNewSizeInt);
		sqls.add(addTempSql);
		String copyExpr = quote + columnName + quote;
		if((effectiveNewSize < oldSize) && "VARCHAR".equalsIgnoreCase(effectiveNewType))
			copyExpr = "SUBSTRING(" + copyExpr + ", 1, " + effectiveNewSize + ")";
		final String copySql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = " + copyExpr;
		sqls.add(copySql);
		if(!effectiveNewNullable)
		{
			final String replaceNullsSql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = '' WHERE " + quote + tempColumnName + quote + " IS NULL";
			sqls.add(replaceNullsSql);
			final String setNotNullSql = "ALTER TABLE " + quote + actualTable + quote + " ALTER COLUMN " + quote + tempColumnName + quote + " SET NOT NULL";
			sqls.add(setNotNullSql);
		}
		sqls.add("ALTER TABLE " + quote + actualTable + quote + " DROP COLUMN " + quote + columnName + quote);
		final String renameSql = "ALTER TABLE " + quote + actualTable + quote + " ALTER COLUMN " + quote + tempColumnName + quote + " RENAME TO " + quote + columnName + quote;
		sqls.add(renameSql);
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			final List<String> cols = (List<String>) idx.get("columns");
			final boolean unique = ((Boolean) idx.get("unique")).booleanValue();
			sqls.add(getCreateIndexSQL(indexName, actualTable, cols, unique));
		}
		if(isPkColumn)
		{
			boolean allNotNull = true;
			for (final String pkCol : pkCols)
			{
				String isNullableStr;
				if(pkCol.equals(columnName.toUpperCase()))
					isNullableStr = effectiveNewNullable ? "YES" : "NO";
				else
				{
					final ResultSet pkColRs = metaData.getColumns(null, null, actualTable, pkCol);
					isNullableStr = pkColRs.next() ? pkColRs.getString("IS_NULLABLE") : "YES";
					pkColRs.close();
				}
				if("YES".equals(isNullableStr))
				{
					allNotNull = false;
					break;
				}
			}
			Collections.sort(pkCols);
			if(allNotNull)
				sqls.add(getAddPrimaryKeySQL(actualTable, pkCols, null));
			else
				sqls.add(getCreateIndexSQL("UNQ_" + actualTable.replaceAll("[^A-Z0-9]","_"), actualTable, pkCols, true));
		}
		return sqls;
	}


	/**
	 * Returns the DB2-specific SQL for a MODIFY COLUMN operation.
	 *
	 * @param actualTable the table name
	 * @param columnName the column name
	 * @param newType the new type (or null to keep existing)
	 * @param newSize the new size (or null to keep existing)
	 * @param newNullable the new nullability (or null to keep existing)
	 * @return the list of SQL statements to run
	 * @throws SQLException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public List<String> getModifyColumnSQLDB2(final String actualTable, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final List<String> sqls = new ArrayList<String>();
		final String quote = metaData.getIdentifierQuoteString();
		final String tempColumnName = columnName + "_TEMP";

		String oldType = "";
		int oldSize = -1;
		String oldPortableType = "";
		boolean oldNullable = true;
		final ResultSet colRs = metaData.getColumns(null, null, actualTable, columnName);
		if(colRs.next())
		{
			oldType = colRs.getString("TYPE_NAME").toUpperCase();
			oldSize = colRs.getInt("COLUMN_SIZE");
			oldNullable = "YES".equals(colRs.getString("IS_NULLABLE"));
			final int oldDataType = colRs.getInt("DATA_TYPE");
			oldPortableType = sqlTypeToPortable.getOrDefault(Integer.valueOf(oldDataType), oldType);
		}
		colRs.close();

		final String effectivePortableType = (newType != null) ? newType : oldPortableType;
		final Integer effectiveNewSizeInt = (newSize != null) ? newSize : Integer.valueOf(oldSize);
		final int effectiveNewSize = effectiveNewSizeInt.intValue();
		final Boolean effectiveNewNullableBool = (newNullable != null) ? newNullable : Boolean.valueOf(oldNullable);
		final boolean effectiveNewNullable = effectiveNewNullableBool.booleanValue();
		final List<String> pkCols = new ArrayList<String>();
		String pkName = null;
		final ResultSet pkRs = metaData.getPrimaryKeys(null, null, actualTable);
		while (pkRs.next())
		{
			pkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
			if(pkName == null) pkName = pkRs.getString("PK_NAME");
		}
		pkRs.close();
		final boolean isPkColumn = pkCols.contains(columnName.toUpperCase());
		final Map<String, Map<String, Object>> indexes = new LinkedHashMap<String, Map<String, Object>>();
		final ResultSet idxRs = metaData.getIndexInfo(null, null, actualTable, false, false);
		while (idxRs.next())
		{
			final short type = idxRs.getShort("TYPE");
			if(type == DatabaseMetaData.tableIndexStatistic)
				continue;
			final String indexName = idxRs.getString("INDEX_NAME");
			if((indexName == null)
			||((pkName != null)
				&&(indexName.equals(pkName))||(indexName.startsWith("SQL"))||(indexName.startsWith("SYS_IDX_SYS_PK_"))))
					continue;
			final boolean nonUnique = idxRs.getBoolean("NON_UNIQUE");
			final String colName = idxRs.getString("COLUMN_NAME");
			if(colName == null)
				continue;
			final short ordinal = idxRs.getShort("ORDINAL_POSITION");
			final Map<String, Object> index = indexes.computeIfAbsent(indexName, k ->
			{
				final Map<String, Object> m = new HashMap<String, Object>();
				m.put("unique", Boolean.valueOf(!nonUnique));
				m.put("columns", new ArrayList<Map<String, Object>>());
				return m;
			});
			final Map<String, Object> colMap = new HashMap<String, Object>();
			colMap.put("pos", Short.valueOf(ordinal));
			colMap.put("col", colName.toUpperCase());
			((List<Map<String, Object>>) index.get("columns")).add(colMap);
		}
		idxRs.close();
		final List<Map<String, Object>> depIndexes = new ArrayList<Map<String, Object>>();
		for (final Map.Entry<String, Map<String, Object>> entry : indexes.entrySet())
		{
			final String indexName = entry.getKey();
			final Map<String, Object> index = entry.getValue();
			final List<Map<String, Object>> cols = (List<Map<String, Object>>) index.get("columns");
			boolean depends = false;
			for (final Map<String, Object> c : cols)
			{
				if(((String) c.get("col")).equals(columnName.toUpperCase()))
				{
					depends = true;
					break;
				}
			}
			if(depends)
			{
				final Map<String, Object> dep = new HashMap<String, Object>();
				dep.put("name", indexName);
				dep.put("unique", index.get("unique"));
				cols.sort((a, b) -> ((Short) a.get("pos")).compareTo((Short) b.get("pos")));
				final List<String> colList = new ArrayList<String>();
				for (final Map<String, Object> c : cols)
					colList.add((String) c.get("col"));
				dep.put("columns", colList);
				depIndexes.add(dep);
			}
		}
		if(isPkColumn)
		{
			String dropSql;
			if(pkName != null)
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP CONSTRAINT " + quote + pkName + quote;
			else
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP PRIMARY KEY";
			sqls.add(dropSql);
		}
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			sqls.add(getDropIndexSQL(indexName, actualTable));
		}
		final String addTempSql = "ALTER TABLE " + quote + actualTable + quote + " ADD COLUMN " + quote + tempColumnName + quote + " "
								  + getDataTypeSQL(effectivePortableType, effectiveNewSizeInt);
		sqls.add(addTempSql);
		String copyExpr = quote + columnName + quote;
		if((effectiveNewSize < oldSize) && "VARCHAR".equalsIgnoreCase(effectivePortableType))
			copyExpr = "SUBSTR(" + copyExpr + ", 1, " + effectiveNewSize + ")";
		final String copySql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = " + copyExpr;
		sqls.add(copySql);
		if(!effectiveNewNullable)
		{
			final String replaceNullsSql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = '' WHERE " + quote + tempColumnName + quote + " IS NULL";
			sqls.add(replaceNullsSql);
			final String setNotNullSql = "ALTER TABLE " + quote + actualTable + quote + " ALTER COLUMN " + quote + tempColumnName + quote + " SET NOT NULL";
			sqls.add(setNotNullSql);
		}
		sqls.add("ALTER TABLE " + quote + actualTable + quote + " DROP COLUMN " + quote + columnName + quote);
		final String renameSql = "ALTER TABLE " + quote + actualTable + quote + " RENAME COLUMN " + quote + tempColumnName + quote + " TO " + quote + columnName + quote;
		sqls.add(renameSql);
		sqls.add("CALL SYSPROC.ADMIN_CMD('REORG TABLE " + actualTable + "')");
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			final List<String> cols = (List<String>) idx.get("columns");
			final boolean unique = ((Boolean) idx.get("unique")).booleanValue();
			sqls.add(getCreateIndexSQL(indexName, actualTable, cols, unique));
		}
		if(isPkColumn)
		{
			boolean allNotNull = true;
			for (final String pkCol : pkCols)
			{
				String isNullableStr;
				if(pkCol.equals(columnName.toUpperCase()))
					isNullableStr = effectiveNewNullable ? "YES" : "NO";
				else
				{
					final ResultSet pkColRs = metaData.getColumns(null, null, actualTable, pkCol);
					isNullableStr = pkColRs.next() ? pkColRs.getString("IS_NULLABLE") : "YES";
					pkColRs.close();
				}
				if("YES".equals(isNullableStr))
				{
					allNotNull = false;
					break;
				}
			}
			Collections.sort(pkCols);
			if(allNotNull)
				sqls.add(getAddPrimaryKeySQL(actualTable, pkCols, null));
			else
				sqls.add(getCreateIndexSQL("UNQ_" + actualTable.replaceAll("[^A-Z0-9]","_"), actualTable, pkCols, true));
		}
		sqls.add("CALL SYSPROC.ADMIN_CMD('REORG TABLE " + actualTable + "')");
		return sqls;
	}

	/**
	 * Generates MODIFY COLUMN SQL for Derby by dropping/re-adding via a temp column,
	 * handling data copy with truncation if needed, and accounting for keys/indexes.
	 * Adds temp as nullable to avoid NOT NULL add errors, copies data, replaces NULLs if needed,
	 * then sets NOT NULL.
	 *
	 * @param actualTable the table name
	 * @param columnName the column name
	 * @param newType the new type (or null to keep existing)
	 * @param newSize the new size (or null to keep existing)
	 * @param newNullable the new nullability (or null to keep existing)
	 * @return the list of SQL statements to run
	 * @throws SQLException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public List<String> getModifyColumnSQLDerby(final String actualTable, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final List<String> sqls = new ArrayList<String>();
		final String quote = metaData.getIdentifierQuoteString();
		final String tempColumnName = columnName + "_TEMP"; // Temporary column name
		String oldType = "";
		String oldPortableType = "";
		int oldSize = -1;
		boolean oldNullable = Boolean.TRUE.booleanValue();
		final ResultSet colRs = metaData.getColumns(null, null, actualTable, columnName);
		if(colRs.next())
		{
			oldType = colRs.getString("TYPE_NAME").toUpperCase();
			oldSize = colRs.getInt("COLUMN_SIZE");
			oldNullable = "YES".equals(colRs.getString("IS_NULLABLE"));
			final int oldDataType = colRs.getInt("DATA_TYPE");
			oldPortableType = sqlTypeToPortable.getOrDefault(Integer.valueOf(oldDataType), oldType);
		}
		colRs.close();
		final String effectivePortableType = (newType != null) ? newType : oldPortableType;
		final String effectiveNewType = (newType != null) ? mapPortableToLocalType(newType) : oldType;
		final Integer effectiveNewSizeInt = (newSize != null) ? newSize : Integer.valueOf(oldSize);
		final int effectiveNewSize = effectiveNewSizeInt.intValue();
		final Boolean effectiveNewNullableBool = (newNullable != null) ? newNullable : Boolean.valueOf(oldNullable);
		final boolean effectiveNewNullable = effectiveNewNullableBool.booleanValue();
		final List<String> pkCols = new ArrayList<String>();
		String pkName = null;
		final ResultSet pkRs = metaData.getPrimaryKeys(null, null, actualTable);
		while (pkRs.next())
		{
			pkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
			if(pkName == null)
				pkName = pkRs.getString("PK_NAME");
		}
		pkRs.close();
		final boolean isPkColumn = pkCols.contains(columnName.toUpperCase());
		final Map<String, Map<String, Object>> indexes = new LinkedHashMap<String, Map<String, Object>>();
		final ResultSet idxRs = metaData.getIndexInfo(null, null, actualTable, Boolean.FALSE.booleanValue(), Boolean.FALSE.booleanValue());
		while (idxRs.next())
		{
			final short type = idxRs.getShort("TYPE");
			if(Short.valueOf(type).equals(Short.valueOf(DatabaseMetaData.tableIndexStatistic)))
				continue;
			final String indexName = idxRs.getString("INDEX_NAME");
			if(indexName == null)
				continue;
			if((pkName != null) && indexName.equals(pkName))
				continue; // Skip PK index
			final boolean nonUnique = idxRs.getBoolean("NON_UNIQUE");
			final String colName = idxRs.getString("COLUMN_NAME");
			if(colName == null)
				continue;
			final short ordinal = idxRs.getShort("ORDINAL_POSITION");
			final Map<String, Object> index = indexes.computeIfAbsent(indexName, k ->
			{
				final Map<String, Object> m = new HashMap<String, Object>();
				m.put("unique", Boolean.valueOf(!nonUnique));
				m.put("columns", new ArrayList<Map<String, Object>>());
				return m;
			});
			final Map<String, Object> colMap = new HashMap<String, Object>();
			colMap.put("pos", Short.valueOf(ordinal));
			colMap.put("col", colName.toUpperCase());
			((List<Map<String, Object>>) index.get("columns")).add(colMap);
		}
		idxRs.close();
		final List<Map<String, Object>> depIndexes = new ArrayList<Map<String, Object>>();
		for (final Map.Entry<String, Map<String, Object>> entry : indexes.entrySet())
		{
			final String indexName = entry.getKey();
			final Map<String, Object> index = entry.getValue();
			final List<Map<String, Object>> cols = (List<Map<String, Object>>) index.get("columns");
			boolean depends = Boolean.FALSE.booleanValue();
			for (final Map<String, Object> c : cols)
			{
				if(((String) c.get("col")).equals(columnName.toUpperCase()))
				{
					depends = Boolean.TRUE.booleanValue();
					break;
				}
			}
			if(depends)
			{
				final Map<String, Object> dep = new HashMap<String, Object>();
				dep.put("name", indexName);
				dep.put("unique", index.get("unique"));
				cols.sort((a, b) -> ((Short) a.get("pos")).compareTo((Short) b.get("pos")));
				final List<String> colList = new ArrayList<String>();
				for (final Map<String, Object> c : cols)
					colList.add((String) c.get("col"));
				dep.put("columns", colList);
				depIndexes.add(dep);
			}
		}
		if(isPkColumn)
		{
			String dropSql;
			if(pkName != null)
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP CONSTRAINT " + quote + pkName + quote;
			else
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP PRIMARY KEY";
			sqls.add(dropSql);
		}
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			sqls.add(getDropIndexSQL(indexName, actualTable));
		}
		final String addTempSql = "ALTER TABLE " + quote + actualTable + quote + " ADD COLUMN " + quote + tempColumnName + quote + " "
			+ getDataTypeSQL(effectivePortableType, effectiveNewSizeInt);
		sqls.add(addTempSql);
		String copyExpr = quote + columnName + quote;
		if((effectiveNewSize < oldSize) && "VARCHAR".equalsIgnoreCase(effectiveNewType))
			copyExpr = "SUBSTR(" + copyExpr + ", 1, " + effectiveNewSize + ")";
		final String copySql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = " + copyExpr;
		sqls.add(copySql);
		if(!effectiveNewNullable)
		{
			final String replaceNullsSql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = '' WHERE " + quote + tempColumnName + quote + " IS NULL";
			sqls.add(replaceNullsSql);
			final String setNotNullSql = "ALTER TABLE " + quote + actualTable + quote + " ALTER COLUMN " + quote + tempColumnName + quote + " NOT NULL";
			sqls.add(setNotNullSql);
		}
		sqls.add("ALTER TABLE " + quote + actualTable + quote + " DROP COLUMN " + quote + columnName + quote);
		final String renameSql = "RENAME COLUMN " + quote + actualTable + quote + "." + quote + tempColumnName + quote + " TO " + quote + columnName + quote;
		sqls.add(renameSql);
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			final List<String> cols = (List<String>) idx.get("columns");
			final boolean unique = ((Boolean) idx.get("unique")).booleanValue();
			sqls.add(getCreateIndexSQL(indexName, actualTable, cols, unique));
		}
		if(isPkColumn)
		{
			boolean allNotNull = Boolean.TRUE.booleanValue();
			for (final String pkCol : pkCols)
			{
				String isNullableStr;
				if(pkCol.equals(columnName.toUpperCase()))
					isNullableStr = effectiveNewNullable ? "YES" : "NO";
				else
				{
					final ResultSet pkColRs = metaData.getColumns(null, null, actualTable, pkCol);
					isNullableStr = pkColRs.next() ? pkColRs.getString("IS_NULLABLE") : "YES";
					pkColRs.close();
				}
				if("YES".equals(isNullableStr))
				{
					allNotNull = Boolean.FALSE.booleanValue();
					break;
				}
			}
			Collections.sort(pkCols);
			if(allNotNull)
				sqls.add(getAddPrimaryKeySQL(actualTable, pkCols, null));
			else
				sqls.add(getCreateIndexSQL("UNQ_" + actualTable.replaceAll("[^A-Z0-9]","_"), actualTable, pkCols, true));
		}
		return sqls;
	}

	/**
	 * Generates MODIFY COLUMN SQL with new size and nullability, for SQL Server specifics
	 * Note: Modifying nullability or size may have restrictions in some DBs (e.g., can't reduce size if data exists)
	 *
	 * @param actualTable the table name
	 * @param columnName the column name
	 * @param newType the new type (or null to keep existing)
	 * @param newSize the new size (or null to keep existing)
	 * @param newNullable the new nullability (or null to keep existing)
	 * @return the list of SQL statements to run
	 * @throws SQLException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public List<String> getModifyColumnSQLServer(final String actualTable, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final List<String> sqls = new ArrayList<String>();
		final String quote = metaData.getIdentifierQuoteString();
		final List<String> pkCols = new ArrayList<>();
		String pkName = null;
		final ResultSet pkRs = metaData.getPrimaryKeys(null, null, actualTable);
		while (pkRs.next())
		{
			pkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
			if(pkName == null)
				pkName = pkRs.getString("PK_NAME");
		}
		pkRs.close();
		final boolean isPkColumn = pkCols.contains(columnName.toUpperCase());
		final Map<String, Map<String, Object>> indexes = new LinkedHashMap<>();
		final ResultSet idxRs = metaData.getIndexInfo(null, null, actualTable, false, false);
		while (idxRs.next())
		{
			final short type = idxRs.getShort("TYPE");
			if(type == DatabaseMetaData.tableIndexStatistic)
				continue;
			final String indexName = idxRs.getString("INDEX_NAME");
			if(indexName == null)
				continue;
			if((pkName != null) && indexName.equals(pkName))
				continue; // Skip PK index
			final boolean nonUnique = idxRs.getBoolean("NON_UNIQUE");
			final String colName = idxRs.getString("COLUMN_NAME");
			if(colName == null)
				continue;
			final short ordinal = idxRs.getShort("ORDINAL_POSITION");
			final Map<String, Object> index = indexes.computeIfAbsent(indexName, k ->
			{
				final Map<String, Object> m = new HashMap<>();
				m.put("unique", Boolean.valueOf(!nonUnique));
				m.put("columns", new ArrayList<Map<String, Object>>());
				return m;
			});
			final Map<String, Object> colMap = new HashMap<>();
			colMap.put("pos", Short.valueOf(ordinal));
			colMap.put("col", colName.toUpperCase());
			((List<Map<String, Object>>) index.get("columns")).add(colMap);
		}
		idxRs.close();
		final List<Map<String, Object>> depIndexes = new ArrayList<>();
		for (final Map.Entry<String, Map<String, Object>> entry : indexes.entrySet())
		{
			final String indexName = entry.getKey();
			final Map<String, Object> index = entry.getValue();
			final List<Map<String, Object>> cols = (List<Map<String, Object>>) index.get("columns");
			boolean depends = false;
			for (final Map<String, Object> c : cols)
			{
				if(((String) c.get("col")).equals(columnName.toUpperCase()))
				{
					depends = true;
					break;
				}
			}
			if(depends)
			{
				final Map<String, Object> dep = new HashMap<>();
				dep.put("name", indexName);
				dep.put("unique", index.get("unique"));
				// Sort columns by position
				cols.sort((a, b) -> ((Short) a.get("pos")).compareTo((Short) b.get("pos")));
				final List<String> colList = new ArrayList<>();
				for (final Map<String, Object> c : cols)
					colList.add((String) c.get("col"));
				dep.put("columns", colList);
				depIndexes.add(dep);
			}
		}
		if(isPkColumn)
		{
			String dropSql;
			if(pkName != null)
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP CONSTRAINT " + quote + pkName + quote + "";
			else
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP PRIMARY KEY";
			sqls.add(dropSql);
		}
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			sqls.add(getDropIndexSQL(indexName, actualTable));
		}
		Boolean effectiveNewNullable = newNullable;
		if(effectiveNewNullable == null)
		{
			final ResultSet colRs = metaData.getColumns(null, null, actualTable, columnName);
			final String isNullableStr = colRs.next() ? colRs.getString("IS_NULLABLE") : "YES";
			colRs.close();
			effectiveNewNullable = Boolean.valueOf("YES".equals(isNullableStr));  // true if nullable, false if NOT NULL
		}
		sqls.add(getModifyColumnSQL(actualTable, columnName, newType, newSize, effectiveNewNullable));

		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			final List<String> cols = (List<String>) idx.get("columns");
			final boolean unique = ((Boolean) idx.get("unique")).booleanValue();
			sqls.add(getCreateIndexSQL(indexName, actualTable, cols, unique));
		}
		if(isPkColumn)
		{
			boolean allNotNull = true;
			for (final String pkCol : pkCols)
			{
				String isNullableStr;
				if(pkCol.equals(columnName.toUpperCase()))
				{
					if(newNullable != null)
						isNullableStr = newNullable.booleanValue() ? "YES" : "NO";
					else
					{
						final ResultSet colRs = metaData.getColumns(null, null, actualTable, pkCol);
						isNullableStr = colRs.next() ? colRs.getString("IS_NULLABLE") : "YES";
						colRs.close();
					}
				}
				else
				{
					final ResultSet colRs = metaData.getColumns(null, null, actualTable, pkCol);
					isNullableStr = colRs.next() ? colRs.getString("IS_NULLABLE") : "YES";
					colRs.close();
				}
				if("YES".equals(isNullableStr))
				{
					allNotNull = false;
					break;
				}
			}
			Collections.sort(pkCols);
			if(allNotNull)
			{
				//sqls.add(getCreateIndexSQL("UNQ_" + actualTable.replaceAll("[^A-Z0-9]","_"), actualTable, pkCols, true));
				sqls.add(getAddPrimaryKeySQL(actualTable, pkCols, null));
			}
			else
				sqls.add(getCreateIndexSQL("UNQ_" + actualTable.replaceAll("[^A-Z0-9]","_"), actualTable, pkCols, true));
		}
		return sqls;
	}

	/**
	 * Generates MODIFY COLUMN SQL with new size and nullability, for Oracle specifics
	 * Note: Modifying nullability or size may have restrictions in some DBs (e.g., can't reduce size if data exists)
	 *
	 * @param actualTable the table name
	 * @param columnName the column name
	 * @param newType the new type (or null to keep existing)
	 * @param newSize the new size (or null to keep existing)
	 * @param newNullable the new nullability (or null to keep existing)
	 * @return the list of SQL statements to run
	 * @throws SQLException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public List<String> getModifyColumnSQLOracle(final String actualTable, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final List<String> sqls = new ArrayList<String>();
		final String quote = metaData.getIdentifierQuoteString();
		final String tempColumnName = columnName + "_TEMP";

		String oldType = "";
		String oldPortableType = "";
		int oldSize = -1;
		boolean oldNullable = true;
		final ResultSet colRs = metaData.getColumns(null, null, actualTable, columnName);
		if(colRs.next())
		{
			oldType = colRs.getString("TYPE_NAME").toUpperCase();
			oldSize = colRs.getInt("COLUMN_SIZE");
			oldNullable = "YES".equals(colRs.getString("IS_NULLABLE"));
			final int oldDataType = colRs.getInt("DATA_TYPE");
			oldPortableType = sqlTypeToPortable.getOrDefault(Integer.valueOf(oldDataType), oldType);
		}
		colRs.close();

		final String effectivePortableType = (newType != null) ? newType : oldPortableType;
		final String effectiveNewType = (newType != null) ? mapPortableToLocalType(newType) : oldType;
		final Integer effectiveNewSizeInt = (newSize != null) ? newSize : Integer.valueOf(oldSize);
		final int effectiveNewSize = effectiveNewSizeInt.intValue();
		final Boolean effectiveNewNullableBool = (newNullable != null) ? newNullable : Boolean.valueOf(oldNullable);
		final boolean effectiveNewNullable = effectiveNewNullableBool.booleanValue();
		final List<String> pkCols = new ArrayList<String>();
		String pkName = null;
		final ResultSet pkRs = metaData.getPrimaryKeys(null, null, actualTable);
		while (pkRs.next())
		{
			pkCols.add(pkRs.getString("COLUMN_NAME").toUpperCase());
			if(pkName == null) pkName = pkRs.getString("PK_NAME");
		}
		pkRs.close();
		final boolean isPkColumn = pkCols.contains(columnName.toUpperCase());
		final Map<String, Map<String, Object>> indexes = new LinkedHashMap<String, Map<String, Object>>();
		final ResultSet idxRs = metaData.getIndexInfo(null, null, actualTable, false, false);
		while (idxRs.next())
		{
			final short type = idxRs.getShort("TYPE");
			if(type == DatabaseMetaData.tableIndexStatistic)
				continue;
			final String indexName = idxRs.getString("INDEX_NAME");
			if((indexName == null)
			||((pkName != null)
				&&(indexName.equals(pkName) || indexName.startsWith("SYS_IDX_"))))
					continue;
			final boolean nonUnique = idxRs.getBoolean("NON_UNIQUE");
			final String colName = idxRs.getString("COLUMN_NAME");
			if(colName == null)
				continue;
			final short ordinal = idxRs.getShort("ORDINAL_POSITION");
			final Map<String, Object> index = indexes.computeIfAbsent(indexName, k ->
			{
				final Map<String, Object> m = new HashMap<String, Object>();
				m.put("unique", Boolean.valueOf(!nonUnique));
				m.put("columns", new ArrayList<Map<String, Object>>());
				return m;
			});
			final Map<String, Object> colMap = new HashMap<String, Object>();
			colMap.put("pos", Short.valueOf(ordinal));
			colMap.put("col", colName.toUpperCase());
			((List<Map<String, Object>>) index.get("columns")).add(colMap);
		}
		idxRs.close();
		final List<Map<String, Object>> depIndexes = new ArrayList<Map<String, Object>>();
		for (final Map.Entry<String, Map<String, Object>> entry : indexes.entrySet())
		{
			final String indexName = entry.getKey();
			final Map<String, Object> index = entry.getValue();
			final List<Map<String, Object>> cols = (List<Map<String, Object>>) index.get("columns");
			boolean depends = false;
			for (final Map<String, Object> c : cols)
			{
				if(((String) c.get("col")).equals(columnName.toUpperCase()))
				{
					depends = true;
					break;
				}
			}
			if(depends)
			{
				final Map<String, Object> dep = new HashMap<String, Object>();
				dep.put("name", indexName);
				dep.put("unique", index.get("unique"));
				cols.sort((a, b) -> ((Short) a.get("pos")).compareTo((Short) b.get("pos")));
				final List<String> colList = new ArrayList<String>();
				for (final Map<String, Object> c : cols)
					colList.add((String) c.get("col"));
				dep.put("columns", colList);
				depIndexes.add(dep);
			}
		}
		if(isPkColumn)
		{
			String dropSql;
			if(pkName != null)
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP CONSTRAINT " + quote + pkName + quote;
			else
				dropSql = "ALTER TABLE " + quote + actualTable + quote + " DROP PRIMARY KEY";
			sqls.add(dropSql);
		}
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			sqls.add(getDropIndexSQL(indexName, actualTable));
		}
		final String addTempSql = "ALTER TABLE " + quote + actualTable + quote + " ADD " + quote + tempColumnName + quote + " "
								  + getDataTypeSQL(effectivePortableType, effectiveNewSizeInt);
		sqls.add(addTempSql);
		String copyExpr = quote + columnName + quote;
		if((effectiveNewSize < oldSize) && effectivePortableType.contains("VARCHAR"))
			copyExpr = "SUBSTR(" + copyExpr + ", 1, " + effectiveNewSize + ")";
		else if(effectiveNewType.equals("CLOB") && oldType.contains("CHAR"))
			copyExpr = "TO_CLOB(" + copyExpr + ")";
		final String copySql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = " + copyExpr;
		sqls.add(copySql);
		if(!effectiveNewNullable)
		{
			final String replaceNullsSql = "UPDATE " + quote + actualTable + quote + " SET " + quote + tempColumnName + quote + " = '' WHERE " + quote + tempColumnName + quote + " IS NULL";
			sqls.add(replaceNullsSql);
			final String setNotNullSql = "ALTER TABLE " + quote + actualTable + quote + " MODIFY " + quote + tempColumnName + quote + " NOT NULL";
			sqls.add(setNotNullSql);
		}
		sqls.add("ALTER TABLE " + quote + actualTable + quote + " DROP COLUMN " + quote + columnName + quote);
		final String renameSql = "ALTER TABLE " + quote + actualTable + quote + " RENAME COLUMN " + quote + tempColumnName + quote + " TO " + quote + columnName + quote;
		sqls.add(renameSql);
		for (final Map<String, Object> idx : depIndexes)
		{
			final String indexName = (String) idx.get("name");
			final List<String> cols = (List<String>) idx.get("columns");
			final boolean unique = ((Boolean) idx.get("unique")).booleanValue();
			sqls.add(getCreateIndexSQL(indexName, actualTable, cols, unique));
		}
		if(isPkColumn)
		{
			boolean allNotNull = true;
			for (final String pkCol : pkCols)
			{
				String isNullableStr;
				if(pkCol.equals(columnName.toUpperCase()))
					isNullableStr = effectiveNewNullable ? "YES" : "NO";
				else
				{
					final ResultSet pkColRs = metaData.getColumns(null, null, actualTable, pkCol);
					isNullableStr = pkColRs.next() ? pkColRs.getString("IS_NULLABLE") : "YES";
					pkColRs.close();
				}
				if("YES".equals(isNullableStr))
				{
					allNotNull = false;
					break;
				}
			}
			Collections.sort(pkCols);
			if(allNotNull)
				sqls.add(getAddPrimaryKeySQL(actualTable, pkCols, null));
			else
				sqls.add(getCreateIndexSQL("UNQ_" + actualTable.replaceAll("[^A-Z0-9]","_"), actualTable, pkCols, true));
		}
		return sqls;
	}


	/**
	 * Generates MODIFY COLUMN SQL with new size and nullability, for SQL Server specifics
	 * Note: Modifying nullability or size may have restrictions in some DBs (e.g., can't reduce size if data exists)
	 *
	 * @param tableName the table name
	 * @param columnName the column name
	 * @param newType the new type (or null to keep existing)
	 * @param newSize the new size (or null to keep existing)
	 * @param newNullable the new nullability (or null to keep existing)
	 * @return the SQL statements to run
	 * @throws SQLException if an error occurs
	 */
	public String getModifyColumnSQL(final String tableName, final String columnName, final String newType, final Integer newSize, final Boolean newNullable) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();
		String modifyClause;
		if(productName.contains("mysql"))
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
		return sql.toString();
	}

	/**
	 * Generates ALTER TABLE ADD PRIMARY KEY SQL
	 * @param tableName The table name
	 * @param columnNames List of column names for the primary key (composite if multiple)
	 * @param nullable If true, creates a unique index instead of a primary key
	 * @return The generated SQL string
	 * @throws SQLException if an error occurs
	 */
	public String getAddPrimaryKeySQL(final String tableName, final List<String> columnNames, final Boolean nullable) throws SQLException
	{
		if((nullable != null) && (nullable.booleanValue()))
			return this.getCreateIndexSQL("UNQ_" + tableName.replaceAll("[^A-Z0-9]","_"), tableName, columnNames, true);
		final String quote=metaData.getIdentifierQuoteString();
		final StringBuilder sql=new StringBuilder("ALTER TABLE ").append(quote)
				.append(tableName).append(quote).append(" ADD");
		if(productName.contains("oracle") || productName.contains("db2")|| productName.contains("sqlserver"))
			sql.append(" CONSTRAINT PK_"+tableName);
		sql.append(" PRIMARY KEY (");
		for(int i=0;i<columnNames.size();i++)
		{
			sql.append(quote).append(columnNames.get(i)).append(quote);
			if(i<(columnNames.size()-1))
				sql.append(", ");
		}
		sql.append(")");
		return sql.toString();
	}

	/**
	 * Generates CREATE INDEX SQL.
	 * @param indexName The name of the index
	 * @param tableName The table name
	 * @param columnNames List of columns to index
	 * @param isUnique Whether the index is unique
	 * @return The generated SQL string
	 * @throws SQLException if an error occurs
	 */
	public String getCreateIndexSQL(final String indexName, final String tableName, final List<String> columnNames, final boolean isUnique) throws SQLException
	{
		final String quote=metaData.getIdentifierQuoteString();

		final StringBuilder sql=new StringBuilder();
		if(productName.contains("fakedb"))
			sql.append("ALTER TABLE ").append(quote).append(tableName).append(quote).append(" ADD ");
		else
			sql.append("CREATE ");
		if(isUnique)
			sql.append("UNIQUE ");
		sql.append("INDEX ");
		if(productName.contains("fakedb"))
			sql.append("(");
		else
		{
			sql.append(quote).append(indexName).append(quote).append(" ON ")
				.append(quote).append(tableName).append(quote).append(" (");
		}
		for(int i=0;i<columnNames.size();i++)
		{
			sql.append(quote).append(columnNames.get(i)).append(quote);
			if(i<(columnNames.size()-1))
				sql.append(", ");
		}
		sql.append(")");
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
		final String newType=mapPortableToLocalType(type);
		if((productName.contains("oracle"))
		&&(newType.contains("CHAR"))
		&&(size == null))
			throw new SQLException("Where's the size?!");
		if(type.equalsIgnoreCase("VARCHAR"))
		{
			if(size!=null)
				return newType+"("+size+")";
			throw new SQLException("Where's the size?!");
		}
		return newType;
	}

	/**
	 * Maps a portable type to a database-specific type. Extend this method to
	 * handle more types and databases as needed.
	 *
	 * @param portableType The portable type string
	 * @return The mapped database-specific type string
	 */
	private String mapPortableToLocalType(final String portableType)
	{
		if(!typeMappings.containsKey(portableType))
			System.out.println("STOP!");
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
		if(!metaData.supportsAlterTableWithDropColumn() && !productName.contains("oracle") && !productName.contains("db2"))
			throw new SQLException("Drop column not supported");
		final String quote=metaData.getIdentifierQuoteString();
		final StringBuilder sql = new StringBuilder("ALTER TABLE ").append(quote).append(tableName).append(quote)
				.append(" DROP COLUMN ").append(quote).append(columnName).append(quote);
		if(productName.contains("db2"))
			sql.append(" RESTRICT");
		return sql.toString();
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
		return "DROP TABLE "+quote+tableName+quote+"";
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
		final Map<String, String> tableCaseMap = new HashMap<>();
		final ResultSet tablesRs = metaData.getTables(null, schema, "%", new String[]{"TABLE"});
		while(tablesRs.next())
		{
			final String actualTableName = tablesRs.getString("TABLE_NAME");
			tableCaseMap.put(actualTableName.toUpperCase(), actualTableName);
		}
		tablesRs.close();
		final Map<String, List<String>> pkAddsByTable = new HashMap<String, List<String>>();
		final String quote = metaData.getIdentifierQuoteString();
		final Map<String,Boolean> nulledColumns = new HashMap<String,Boolean>();
		for(final JSONObject change : changes)
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
							colDef.put("nullable", c.containsKey("nullable") ? c.getCheckedBoolean("nullable") : Boolean.TRUE);
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
				{
					boolean allNotNull = true;
					for(final String pk : pkCols)
					{
						boolean found = false;
						for(final Map<String, Object> col : columnDefs)
						{
							if(((String)col.get("name")).equals(pk))
							{
								found = true;
								final boolean nul = ((Boolean)col.get("nullable")).booleanValue();
								if(nul)
									allNotNull = false;
								break;
							}
						}
						if(!found)
							allNotNull = false;
						if(!allNotNull)
							break;
					}
					if(allNotNull)
					{
						sqls.add(getAddPrimaryKeySQL(tableName, pkCols, null));
						//sqls.add(getCreateIndexSQL("UNQ_" + tableName.replaceAll("[^A-Z0-9]","_"), tableName, pkCols, true));
					}
					else
						sqls.add(getCreateIndexSQL("UNQ_" + tableName.replaceAll("[^A-Z0-9]","_"), tableName, pkCols, true));
				}
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
					final String transferSql = "INSERT INTO "+quote+tableName+quote+" ("+insertCols+") SELECT "+selectCols+" FROM "+quote+fromTable+quote+"";
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
				final String colName = change.getCheckedString("name").toUpperCase();
				final String desType = change.getCheckedString("type").toUpperCase();
				final Integer desSize = change.containsKey("size") ? Integer.valueOf(change.getCheckedLong("size").intValue()) : null;
				final Boolean desNullable = change.containsKey("nullable") ? change.getCheckedBoolean("nullable") : Boolean.TRUE;
				final String actualTable = tableCaseMap.getOrDefault(table, table);
				final ResultSet colRs = metaData.getColumns(null, null, actualTable, colName);
				if(!colRs.next())
				{
					final Map<String, Object> colDef = new HashMap<>();
					colDef.put("name", colName);
					colDef.put("type", desType);
					if(desSize != null)
						colDef.put("size", desSize);
					colDef.put("nullable", desNullable);
					sqls.add(getAddColumnSQL(table, colDef));
				}
				else
				{
					final int currDataType = colRs.getInt("DATA_TYPE");
					final String currType = sqlTypeToPortable.get(Integer.valueOf(currDataType));
					final int currSize = colRs.getInt("COLUMN_SIZE");
					final boolean currNullable = "YES".equals(colRs.getString("IS_NULLABLE"));
					boolean mismatch = false;
					if((currType == null)||(!desType.equals(currType)))
						mismatch = true;
					else
					if((desSize != null)&&(desSize.intValue() != currSize))
						mismatch = true;
					else
					if(!desNullable.booleanValue() == (currNullable))
						mismatch = true;
					if(mismatch)
					{
						if(productName.contains("sql server"))
							sqls.addAll(getModifyColumnSQLServer(actualTable, colName, desType, desSize, desNullable));
						else
						if(productName.contains("derby"))
							sqls.addAll(getModifyColumnSQLDerby(actualTable, colName, desType, desSize, desNullable));
						else
						if(productName.contains("hsql"))
							sqls.addAll(getModifyColumnSQLHSQLDB(actualTable, colName, desType, desSize, desNullable));
						else
						if(productName.contains("postgres"))
							sqls.addAll(getModifyColumnSQLPostgreSQL(actualTable, colName, desType, desSize, desNullable));
						else
						if(productName.contains("oracle"))
							sqls.addAll(getModifyColumnSQLOracle(actualTable, colName, desType, desSize, desNullable));
						else
						if(productName.contains("db2"))
							sqls.addAll(getModifyColumnSQLDB2(actualTable, colName, desType, desSize, desNullable));
						else
							sqls.add(getModifyColumnSQL(table, colName, desType, desSize, desNullable));
					}
				}
				colRs.close();
			}
			else
			if(action.equals("MODIFY") && target.equals("COLUMN"))
			{
				final String col = change.getCheckedString("name").toUpperCase();
				final String type = change.getCheckedString("type").toUpperCase();
				final Integer size = change.containsKey("size")?Integer.valueOf(change.getCheckedLong("size").intValue()):null;
				final Boolean nullable = change.containsKey("nullable")?Boolean.valueOf(change.getCheckedBoolean("nullable").booleanValue()):null;
				final String actualTable = tableCaseMap.getOrDefault(table, table);
				if(nullable != null)
					nulledColumns.put(table+"."+col,nullable);
				if(productName.contains("sql server"))
					sqls.addAll(getModifyColumnSQLServer(actualTable, col, type, size, nullable));
				else
				if(productName.contains("derby"))
					sqls.addAll(getModifyColumnSQLDerby(actualTable, col, type, size, nullable));
				else
				if(productName.contains("hsql"))
					sqls.addAll(getModifyColumnSQLHSQLDB(actualTable, col, type, size, nullable));
				else
				if(productName.contains("postgres"))
					sqls.addAll(getModifyColumnSQLPostgreSQL(actualTable, col, type, size, nullable));
				else
				if(productName.contains("oracle"))
					sqls.addAll(getModifyColumnSQLOracle(actualTable, col, type, size, nullable));
				else
				if(productName.contains("db2"))
					sqls.addAll(getModifyColumnSQLDB2(actualTable, col, type, size, nullable));
				else
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
				final String transferSql = "INSERT INTO "+quote+toTable+quote+" ("+insertCols+") SELECT "+selectCols+" FROM "+quote+fromTable+quote+"";
				sqls.add(transferSql);
				sqls.add(getDropColumnSQL(fromTable, col));
			}
		}
		for(final String table : pkAddsByTable.keySet())
		{
			final List<String> cols = pkAddsByTable.get(table);
			if(cols.isEmpty())
				continue;
			final String actualTable = tableCaseMap.getOrDefault(table, table);
			boolean allNotNull = true;
			for(final String col : cols)
			{
				final ResultSet colRs = metaData.getColumns(null, null, actualTable, col);
				if(colRs.next())
				{
					if("YES".equals(colRs.getString("IS_NULLABLE")))
						allNotNull = false;
				}
				else
					allNotNull = false;
				colRs.close();
				if(!allNotNull)
					break;
			}
			if(allNotNull)
			{
				//final String indexName = "UNQ_" + table.replaceAll("[^A-Z0-9]","_");
				//sqls.add(getCreateIndexSQL(indexName, table, cols, true));
				sqls.add(getAddPrimaryKeySQL(table, cols,null));
			}
			else
			{
				final String indexName = "UNQ_" + table.replaceAll("[^A-Z0-9]","_");
				sqls.add(getCreateIndexSQL(indexName, table, cols, true));
			}
		}
		return sqls;
	}
}
