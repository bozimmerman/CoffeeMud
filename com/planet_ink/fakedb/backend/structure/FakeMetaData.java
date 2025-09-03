package com.planet_ink.fakedb.backend.structure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.planet_ink.fakedb.backend.Backend;

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
/**
*
* @author Bo Zimmerman
*
*/
public class FakeMetaData implements DatabaseMetaData
{
	private final Backend backend;

	public FakeMetaData(final Backend backend)
	{
		super();
		this.backend = backend;
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException
	{
		return null;
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException
	{
		return false;
	}

	@Override
	public boolean allProceduresAreCallable() throws SQLException
	{
		return false;
	}

	@Override
	public boolean allTablesAreSelectable() throws SQLException
	{
		return true;
	}

	@Override
	public String getURL() throws SQLException
	{
		return "jdbc:fakedb";
	}

	@Override
	public String getUserName() throws SQLException
	{
		return "";
	}

	@Override
	public boolean isReadOnly() throws SQLException
	{
		return false;
	}

	@Override
	public boolean nullsAreSortedHigh() throws SQLException
	{
		return false;
	}

	@Override
	public boolean nullsAreSortedLow() throws SQLException
	{
		return true;
	}

	@Override
	public boolean nullsAreSortedAtStart() throws SQLException
	{
		return false;
	}

	@Override
	public boolean nullsAreSortedAtEnd() throws SQLException
	{
		return false;
	}

	@Override
	public String getDatabaseProductName() throws SQLException
	{
		return "fakedb";
	}

	@Override
	public String getDatabaseProductVersion() throws SQLException
	{
		return "1.0";
	}

	@Override
	public String getDriverName() throws SQLException
	{
		return "FakeDB JDBC Driver";
	}

	@Override
	public String getDriverVersion() throws SQLException
	{
		return "1.0";
	}

	@Override
	public int getDriverMajorVersion()
	{
		return 1;
	}

	@Override
	public int getDriverMinorVersion()
	{
		return 0;
	}

	@Override
	public boolean usesLocalFiles() throws SQLException
	{
		return true;
	}

	@Override
	public boolean usesLocalFilePerTable() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsMixedCaseIdentifiers() throws SQLException
	{
		return true;
	}

	@Override
	public boolean storesUpperCaseIdentifiers() throws SQLException
	{
		return true;
	}

	@Override
	public boolean storesLowerCaseIdentifiers() throws SQLException
	{
		return false;
	}

	@Override
	public boolean storesMixedCaseIdentifiers() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
	{
		return false;
	}

	@Override
	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
	{
		return false;
	}

	@Override
	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
	{
		return false;
	}

	@Override
	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
	{
		return false;
	}

	@Override
	public String getIdentifierQuoteString() throws SQLException
	{
		return "'";
	}

	@Override
	public String getSQLKeywords() throws SQLException
	{
		return "";
	}

	@Override
	public String getNumericFunctions() throws SQLException
	{
		return "";
	}

	@Override
	public String getStringFunctions() throws SQLException
	{
		return "";
	}

	@Override
	public String getSystemFunctions() throws SQLException
	{
		return "";
	}

	@Override
	public String getTimeDateFunctions() throws SQLException
	{
		return "";
	}

	@Override
	public String getSearchStringEscape() throws SQLException
	{
		return "\\";
	}

	@Override
	public String getExtraNameCharacters() throws SQLException
	{
		return "";
	}

	@Override
	public boolean supportsAlterTableWithAddColumn() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsAlterTableWithDropColumn() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsColumnAliasing() throws SQLException
	{
		return false;
	}

	@Override
	public boolean nullPlusNonNullIsNull() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsConvert() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsConvert(final int fromType, final int toType) throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsTableCorrelationNames() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsDifferentTableCorrelationNames() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsExpressionsInOrderBy() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsOrderByUnrelated() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsGroupBy() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsGroupByUnrelated() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsGroupByBeyondSelect() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsLikeEscapeClause() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsMultipleResultSets() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsMultipleTransactions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsNonNullableColumns() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsMinimumSQLGrammar() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsCoreSQLGrammar() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsExtendedSQLGrammar() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsANSI92EntryLevelSQL() throws SQLException
	{
		return true;
	}

	@Override
	public boolean supportsANSI92IntermediateSQL() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsANSI92FullSQL() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsIntegrityEnhancementFacility() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsOuterJoins() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsFullOuterJoins() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsLimitedOuterJoins() throws SQLException
	{
		return false;
	}

	@Override
	public String getSchemaTerm() throws SQLException
	{
		return null;
	}

	@Override
	public String getProcedureTerm() throws SQLException
	{
		return null;
	}

	@Override
	public String getCatalogTerm() throws SQLException
	{
		return null;
	}

	@Override
	public boolean isCatalogAtStart() throws SQLException
	{
		return false;
	}

	@Override
	public String getCatalogSeparator() throws SQLException
	{
		return "";
	}

	@Override
	public boolean supportsSchemasInDataManipulation() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSchemasInProcedureCalls() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSchemasInTableDefinitions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSchemasInIndexDefinitions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsCatalogsInDataManipulation() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsCatalogsInProcedureCalls() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsCatalogsInTableDefinitions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsCatalogsInIndexDefinitions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsPositionedDelete() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsPositionedUpdate() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSelectForUpdate() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsStoredProcedures() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSubqueriesInComparisons() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSubqueriesInExists() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSubqueriesInIns() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsSubqueriesInQuantifieds() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsCorrelatedSubqueries() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsUnion() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsUnionAll() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsOpenCursorsAcrossCommit() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsOpenCursorsAcrossRollback() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsOpenStatementsAcrossCommit() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsOpenStatementsAcrossRollback() throws SQLException
	{
		return false;
	}

	@Override
	public int getMaxBinaryLiteralLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxCharLiteralLength() throws SQLException
	{
		return 255;
	}

	@Override
	public int getMaxColumnNameLength() throws SQLException
	{
		return 255;
	}

	@Override
	public int getMaxColumnsInGroupBy() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxColumnsInIndex() throws SQLException
	{
		return 1;
	}

	@Override
	public int getMaxColumnsInOrderBy() throws SQLException
	{
		return 1;
	}

	@Override
	public int getMaxColumnsInSelect() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxColumnsInTable() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxConnections() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxCursorNameLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxIndexLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxSchemaNameLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxProcedureNameLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxCatalogNameLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxRowSize() throws SQLException
	{
		return 0;
	}

	@Override
	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
	{
		return false;
	}

	@Override
	public int getMaxStatementLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxStatements() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxTableNameLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxTablesInSelect() throws SQLException
	{
		return 0;
	}

	@Override
	public int getMaxUserNameLength() throws SQLException
	{
		return 0;
	}

	@Override
	public int getDefaultTransactionIsolation() throws SQLException
	{
		return 0;
	}

	@Override
	public boolean supportsTransactions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsTransactionIsolationLevel(final int level) throws SQLException
	{
		return level == Connection.TRANSACTION_NONE;
	}

	@Override
	public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsDataManipulationTransactionsOnly() throws SQLException
	{
		return false;
	}

	@Override
	public boolean dataDefinitionCausesTransactionCommit() throws SQLException
	{
		return false;
	}

	@Override
	public boolean dataDefinitionIgnoredInTransactions() throws SQLException
	{
		return true;
	}

	@Override
	public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	private boolean patternMatch(final String key, final String pattern)
	{
		if((pattern==null)
		||(pattern.equals("%"))
		||(pattern.equalsIgnoreCase(key))
		||(pattern.equalsIgnoreCase("'"+key+"'"))
		||(pattern.equalsIgnoreCase("\""+key+"\""))
		||(pattern.equalsIgnoreCase("`"+key+"`"))
		||(pattern.startsWith("%")
				&&(pattern.endsWith("%"))
				&&(key.toUpperCase().indexOf(pattern.substring(1,pattern.length()-1).toUpperCase())>=0))
		||(pattern.startsWith("%")
				&&(key.toUpperCase().endsWith(pattern.substring(1).toUpperCase())))
		||(pattern.endsWith("%")
				&& (key.toUpperCase().startsWith(pattern.substring(0, pattern.length() - 1).toUpperCase()))))
			return true;
		return false;
	}

	@Override
	public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException
	{
		final List<Map<String,Object>> rows = new ArrayList<>();
		final Map<String,FakeTable> tables = this.backend.getFakeTables();
		for(final String key : tables.keySet())
		{
			final Map<String,Object> row = new HashMap<>();
			if(patternMatch(key,tableNamePattern))
			{
				row.put("TABLE_NAME", key);
				row.put("TABLE_TYPE", "TABLE");
				rows.add(row);
			}
		}
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createSimpleResultSet(rows);
	}

	@Override
	public ResultSet getSchemas() throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getCatalogs() throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getTableTypes() throws SQLException
	{
		final Map<String,Object> row = new HashMap<>();
		row.put("TABLE_TYPE", "TABLE");
		final List<Map<String,Object>> rows = new ArrayList<>();
		rows.add(row);
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createSimpleResultSet(rows);
	}

	@Override
	public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException
	{
		final List<Map<String,Object>> rows = new ArrayList<>();
		final Map<String,FakeTable> tables = this.backend.getFakeTables();
		FakeTable table = null;
		for(final String key : tables.keySet())
		{
			if(patternMatch(key,tableNamePattern))
			{
				table=tables.get(key);
				break;
			}
		}
		if(table!=null)
		{
			for(final FakeColumn col : table.columns)
			{
				final String colName=col.name;
				if((colName!=null)
				&&(patternMatch(colName,columnNamePattern)))
				{
					final Map<String,Object> row = new HashMap<>();
					row.put("TABLE_NAME", table.name);
					row.put("COLUMN_NAME", colName);
					row.put("DATA_TYPE", Integer.valueOf(col.type.getSQLType()));
					row.put("TYPE_NAME", col.type.name());
					row.put("COLUMN_SIZE", Integer.valueOf(col.size==Integer.MAX_VALUE?-1:col.size));
					row.put("BUFFER_LENGTH", null);
					row.put("DECIMAL_DIGITS", Integer.valueOf(col.size==Integer.MAX_VALUE?-1:col.size));
					row.put("NUM_PREC_RADIX", Integer.valueOf(10));
					row.put("NULLABLE", Integer.valueOf(col.canNull?1:0));
					row.put("REMARKS", "");
					row.put("COLUMN_DEF", col.canNull?null:"");
					row.put("SQL_DATA_TYPE", null);
					row.put("SQL_DATETIME_SUB", null);
					row.put("CHAR_OCTET_LENGTH", Integer.valueOf(col.size==Integer.MAX_VALUE?-1:col.size));
					row.put("ORDINAL_POSITION", Integer.valueOf(col.indexNumber+1));
					row.put("IS_NULLABLE", col.canNull?"YES":"NO");
					row.put("SCOPE_CATALOG", null);
					row.put("SCOPE_SCHEMA", null);
					row.put("SCOPE_TABLE", null);
					row.put("SOURCE_DATA_TYPE", null);
					row.put("IS_AUTOINCREMENT", "NO");
					row.put("IS_GENERATEDCOLUMN", "NO");
					rows.add(row);
				}
			}
		}
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createSimpleResultSet(rows);
	}

	@Override
	public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getPrimaryKeys(final String catalog, final String schema, final String tablePattern) throws SQLException
	{
		final List<Map<String,Object>> rows = new ArrayList<>();
		final Map<String,FakeTable> tables = this.backend.getFakeTables();
		FakeTable table = null;
		for(final String key : tables.keySet())
		{
			if(patternMatch(key,tablePattern))
			{
				table=tables.get(key);
				break;
			}
		}
		if(table!=null)
		{
			for (final FakeColumn col : table.columns)
			{
				if (col.keyNumber >= 0)
				{
					final Map<String, Object> row = new HashMap<>();
					row.put("TABLE_NAME", table.name);
					row.put("COLUMN_NAME", col.name);
					row.put("KEY_SEQ", Integer.valueOf(col.keyNumber + 1));
					row.put("PK_NAME", "PRIMARY");
					rows.add(row);
				}
			}
		}
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createSimpleResultSet(rows);
	}

	@Override
	public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getCrossReference(final String parentCatalog, final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getTypeInfo() throws SQLException
	{
		final List<Map<String,Object>> rows = new ArrayList<>();
		for (final FakeColumn.FakeColType t : FakeColumn.FakeColType.values())
		{
			if (t != FakeColumn.FakeColType.UNKNOWN)
			{
				final Map<String, Object> row = new HashMap<>();
				row.put("TYPE_NAME", t.name());
				row.put("DATA_TYPE", Integer.valueOf(t.getSQLType()));
				row.put("PRECISION", Integer.valueOf(0));
				row.put("LITERAL_PREFIX", null);
				row.put("LITERAL_SUFFIX", null);
				row.put("CREATE_PARAMS", null);
				row.put("NULLABLE", Integer.valueOf(1));
				row.put("CASE_SENSITIVE", Boolean.FALSE);
				row.put("SEARCHABLE", Integer.valueOf(3));
				row.put("UNSIGNED_ATTRIBUTE", null);
				row.put("FIXED_PREC_SCALE", null);
				row.put("AUTO_INCREMENT", "NO");
				row.put("LOCAL_TYPE_NAME", t.name());
				row.put("MINIMUM_SCALE", Integer.valueOf(0));
				row.put("MAXIMUM_SCALE", Integer.valueOf(0));
				row.put("SQL_DATA_TYPE", null);
				row.put("SQL_DATETIME_SUB", null);
				row.put("NUM_PREC_RADIX", null);
				rows.add(row);
			}
		}
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createSimpleResultSet(rows);
	}

	@Override
	public ResultSet getIndexInfo(final String catalog, final String schema, final String tablePattern, final boolean unique, final boolean approximate) throws SQLException
	{
		final List<Map<String,Object>> rows = new ArrayList<>();
		final Map<String,FakeTable> tables = this.backend.getFakeTables();
		FakeTable table = null;
		for(final String key : tables.keySet())
		{
			if(patternMatch(key,tablePattern))
			{
				table=tables.get(key);
				break;
			}
		}
		if(table!=null)
		{
			for (final FakeColumn col : table.columns)
			{
				if(col.indexNumber >= 0)
				{
					final Map<String, Object> row = new HashMap<>();
					row.put("TABLE_NAME", table.name);
					row.put("NON_UNIQUE", Boolean.valueOf(col.keyNumber < 0));
					row.put("INDEX_QUALIFIER", null);
					row.put("INDEX_NAME", "INDEX" + Integer.toString(col.indexNumber));
					row.put("TYPE", Integer.valueOf(3));
					row.put("ORDINAL_POSITION", Integer.valueOf(col.indexNumber + 1));
					row.put("COLUMN_NAME", col.name);
					row.put("ASC_OR_DESC", "A");
					row.put("CARDINALITY", Integer.valueOf(-1));
					row.put("PAGES", Integer.valueOf(-1));
					row.put("FILTER_CONDITION", null);
					rows.add(row);
				}
			}
		}

		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createSimpleResultSet(rows);
	}

	@Override
	public boolean supportsResultSetType(final int type) throws SQLException
	{
		return type == ResultSet.TYPE_FORWARD_ONLY;
	}

	@Override
	public boolean supportsResultSetConcurrency(final int type, final int concurrency) throws SQLException
	{
		return type == ResultSet.TYPE_FORWARD_ONLY && concurrency == ResultSet.CONCUR_READ_ONLY;
	}

	@Override
	public boolean ownUpdatesAreVisible(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean ownDeletesAreVisible(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean ownInsertsAreVisible(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean othersUpdatesAreVisible(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean othersDeletesAreVisible(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean othersInsertsAreVisible(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean updatesAreDetected(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean deletesAreDetected(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean insertsAreDetected(final int type) throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsBatchUpdates() throws SQLException
	{
		return true;
	}

	@Override
	public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return null;
	}

	@Override
	public boolean supportsSavepoints() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsNamedParameters() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsMultipleOpenResults() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsGetGeneratedKeys() throws SQLException
	{
		return false;
	}

	@Override
	public ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public boolean supportsResultSetHoldability(final int holdability) throws SQLException
	{
		return holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
	}

	@Override
	public int getResultSetHoldability() throws SQLException
	{
		return ResultSet.HOLD_CURSORS_OVER_COMMIT;
	}

	@Override
	public int getDatabaseMajorVersion() throws SQLException
	{
		return 1;
	}

	@Override
	public int getDatabaseMinorVersion() throws SQLException
	{
		return 0;
	}

	@Override
	public int getJDBCMajorVersion() throws SQLException
	{
		return 4;
	}

	@Override
	public int getJDBCMinorVersion() throws SQLException
	{
		return 0;
	}

	@Override
	public int getSQLStateType() throws SQLException
	{
		return DatabaseMetaData.sqlStateSQL;
	}

	@Override
	public boolean locatorsUpdateCopy() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsStatementPooling() throws SQLException
	{
		return false;
	}

	@Override
	public RowIdLifetime getRowIdLifetime() throws SQLException
	{
		return RowIdLifetime.ROWID_UNSUPPORTED;
	}

	@Override
	public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
	{
		return false;
	}

	@Override
	public boolean autoCommitFailureClosesAllResultSets() throws SQLException
	{
		return false;
	}

	@Override
	public ResultSet getClientInfoProperties() throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException
	{
		return com.planet_ink.fakedb.backend.jdbc.ResultSet.createEmptyResultSet();
	}

	@Override
	public boolean generatedKeyAlwaysReturned() throws SQLException
	{
		return false;
	}

}
