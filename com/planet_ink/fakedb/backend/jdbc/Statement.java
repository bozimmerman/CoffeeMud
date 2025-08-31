package com.planet_ink.fakedb.backend.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.planet_ink.fakedb.backend.Connection;
import com.planet_ink.fakedb.backend.Backend.ConnectorType;
import com.planet_ink.fakedb.backend.statements.ImplAlterStatement;
import com.planet_ink.fakedb.backend.statements.ImplCreateStatement;
import com.planet_ink.fakedb.backend.statements.ImplDeleteStatement;
import com.planet_ink.fakedb.backend.statements.ImplDropStatement;
import com.planet_ink.fakedb.backend.statements.ImplInsertStatement;
import com.planet_ink.fakedb.backend.statements.ImplSelectStatement;
import com.planet_ink.fakedb.backend.statements.ImplUpdateStatement;
import com.planet_ink.fakedb.backend.structure.FakeColumn;
import com.planet_ink.fakedb.backend.structure.FakeCondition;
import com.planet_ink.fakedb.backend.structure.FakeTable;

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
public class Statement implements java.sql.Statement
{
	protected ResultSet		myResultSet						= null;
	protected boolean		closeStatementOnResultSetClose	= false;
	protected Connection	connection;
	public String			lastSQL							= "null";

	static protected void log(final String x)
	{
		System.err.println("Statement: " + x);
	}

	public Statement(final Connection c)
	{
		connection = c;
	}

	@Override
	public java.sql.Connection getConnection()
	{
		return connection;
	}

	public Connection getFakeConnection()
	{
		return connection;
	}

	protected String split(String sql, final String[] token)
	{
		while (true)
		{
			if (sql.length() == 0)
			{
				token[0] = "";
				return "";
			}
			if (sql.charAt(0) == ' ')
			{
				sql = sql.substring(1);
				continue;
			}
			if (sql.charAt(0) == ';')
			{
				sql = sql.substring(1);
				continue;
			}
			int index;
			for (index = 0; index < sql.length(); index++)
			{
				char c = sql.charAt(index);
				if (c == ' ')
				{
					break;
				}
				else
				if (c == ';')
				{
					break;
				}
				else
				if (c == '\'')
				{
					for (++index; index < sql.length(); index++)
					{
						c = sql.charAt(index);
						if (c == '\\')
							index++;
						else
						if (c == '\'')
							break;
					}
				}
			}
			if (index >= sql.length())
			{
				token[0] = sql;
				return "";
			}
			token[0] = sql.substring(0, index);
			return sql.substring(index + 1);
		}
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		return connection.isClosed();
	}

	@Override
	public void setPoolable(final boolean isPoolable)
	{
	}

	@Override
	public boolean isPoolable() throws SQLException
	{
		return false;
	}

	@Override
	public boolean isWrapperFor(final Class<?> arg0) throws SQLException
	{
		return false;
	}

	@Override
	public <T> T unwrap(final Class<T> arg0) throws SQLException
	{
		return null;
	}

	public String parseWhereClause(final String tableName, final String sql, List<FakeCondition> conditions) throws java.sql.SQLException
	{
		int s = 0;
		final String eow1 = " \t!=><";
		final java.util.Stack<List<FakeCondition>> parenStack = new java.util.Stack<List<FakeCondition>>();
		while (s < sql.length())
		{
			while ((s < sql.length()) && (sql.charAt(s) == ' ' || sql.charAt(s) == '\t'))
				s++;
			if (s >= sql.length())
				break;
			FakeCondition condition = null;
			if (sql.charAt(s) == '(')
			{
				condition = connection.getBackend().buildFakeCondition(tableName, null, null, null, false);
				conditions.add(condition);
				parenStack.push(conditions);
				condition.contains = new ArrayList<FakeCondition>();
				conditions = condition.contains;
				s++;
				continue;
			}
			else
			if (sql.charAt(s) == ')')
			{
				if (parenStack.size() == 0)
					throw new java.sql.SQLException("Unexpected end parenthesis " + sql);
				conditions = parenStack.pop();
				condition = conditions.get(conditions.size() - 1);
				s++;
			}
			else
			{
				int e = s;
				boolean unPrepared = false;
				while ((e < sql.length()) && (eow1.indexOf(sql.charAt(e)) < 0))
					e++;
				final String columnName = sql.substring(s, e);
				if (e >= sql.length())
					throw new java.sql.SQLException("Unexpected end of where clause in " + sql);
				s = e;
				while ((s < sql.length()) && (sql.charAt(s) == ' ' || sql.charAt(s) == '\t'))
					s++;
				e = s;
				String comparitor;
				if ((e < sql.length() - 5)
				&& (Character.toLowerCase(sql.charAt(e)) == 'l')
				&& (Character.toLowerCase(sql.charAt(e + 1)) == 'i')
				&& (Character.toLowerCase(sql.charAt(e + 2)) == 'k')
				&& (Character.toLowerCase(sql.charAt(e + 3)) == 'e')
				&& (Character.toLowerCase(sql.charAt(e + 4)) == ' '))
				{
					comparitor = "like";
					e += 5;
				}
				else
				if ((e < sql.length()) && (eow1.indexOf(sql.charAt(e)) > 0))
				{
					while ((e < sql.length()) && (eow1.indexOf(sql.charAt(e)) > 0))
						e++;
					comparitor = sql.substring(s, e).trim();
				}
				else
					throw new java.sql.SQLException("Illegal comparator " + sql);
				if (e >= sql.length() || comparitor.length() == 0)
					throw new java.sql.SQLException("Unexpected end of where clause in " + sql);
				s = e;
				while ((sql.charAt(s) == ' ' || sql.charAt(s) == '\t') && (s < sql.length()))
					s++;
				if (s >= sql.length())
					throw new java.sql.SQLException("Unexpected end of where clause in " + sql);
				String value;
				e = s;
				if (sql.charAt(s) == '\'')
				{
					e++;
					final StringBuilder str = new StringBuilder("");
					while ((e < sql.length()) && (sql.charAt(e) != '\''))
					{
						if (sql.charAt(e) == '\\')
							e++;
						if (e < sql.length())
							str.append(sql.charAt(e));
						e++;
					}
					if (e >= sql.length())
						throw new java.sql.SQLException("Unexpected end of where clause in " + sql);
					e++;
					value = str.toString();
				}
				else
				{
					while ((e < sql.length())
					&& (sql.charAt(e) != ' ')
					&& (sql.charAt(e) != '\t')
					&& (sql.charAt(e) != ';'||(s==e)))
						e++;
					value = sql.substring(s, e);
					if (value.equalsIgnoreCase("?"))
						unPrepared = true;
				}
				s = e;
				condition = connection.getBackend().buildFakeCondition(tableName, columnName, comparitor, value, unPrepared);
				conditions.add(condition);
			}
			while ((s < sql.length()) && (sql.charAt(s) == ' ' || sql.charAt(s) == '\t'))
				s++;
			if (s >= sql.length())
				break;
			if(sql.charAt(s)==';')
				break;
			int e = s;
			while ((e < sql.length()) && (sql.charAt(e) != ' ') && (sql.charAt(e) != '\t'))
				e++;
			if (condition == null)
				continue;
			final String peeker = sql.substring(s, e);
			if (peeker.equalsIgnoreCase(")"))
			{

			}
			else
			if (peeker.equalsIgnoreCase("AND"))
			{
				s = e;
				condition.connector = ConnectorType.AND;
			}
			else
			if (peeker.equalsIgnoreCase("OR"))
			{
				s = e;
				condition.connector = ConnectorType.OR;
			}
			else
				break;
		}
		if (parenStack.size() > 0)
			throw new java.sql.SQLException("Unended parenthesis " + sql);
		if (s >= sql.length())
			return "";
		return sql.substring(s);
	}

	@Override
	public java.sql.ResultSet executeQuery(String sql) throws java.sql.SQLException
	{
		lastSQL = sql;
		try
		{
			final String[] token = new String[1];
			sql = split(sql, token);
			if (!token[0].equalsIgnoreCase("select"))
				throw new java.sql.SQLException("first query token not select");
			final ImplSelectStatement stmt = ImplSelectStatement.parse(this,sql, token);
			return connection.getBackend().constructScan(stmt);
		}
		catch (final java.sql.SQLException e)
		{
			log("unsupported SQL in executeQuery: " + sql);
			throw e;
		}
	}

	@Override
	public int executeUpdate(String sql) throws java.sql.SQLException
	{
		lastSQL = sql;
		// log("executeUpdate"+sql);

		// insert into x (a,b,c) values (a,b,c)
		// update x set a=A,b=B where x=y
		// delete from x where x=y

		final String originalSql = sql;
		try
		{
			final String[] token = new String[1];
			sql = split(sql, token);
			if (token[0].equalsIgnoreCase("insert"))
			{
				final ImplInsertStatement stmt = ImplInsertStatement.parse(sql, token);
				connection.getBackend().dupKeyCheck(stmt.tableName, stmt.columns, stmt.sqlValues);
				connection.getBackend().insertValues(stmt);
			}
			else
			if (token[0].equalsIgnoreCase("update"))
			{
				final ImplUpdateStatement stmt = ImplUpdateStatement.parse(this,sql, token);
				connection.getBackend().updateRecord(stmt);
			}
			else
			if (token[0].equalsIgnoreCase("delete"))
			{
				final ImplDeleteStatement stmt = ImplDeleteStatement.parse(this,sql, token);
				connection.getBackend().deleteRecord(stmt);
			}
			else
			if (token[0].equalsIgnoreCase("create"))
			{
				final ImplCreateStatement stmt = ImplCreateStatement.parse(sql, token);
				connection.getBackend().createTable(stmt);
			}
			else
			if (token[0].equalsIgnoreCase("drop"))
			{
				final ImplDropStatement stmt = ImplDropStatement.parse(sql, token);
				connection.getBackend().dropTable(stmt);
			}
			else
			if (token[0].equalsIgnoreCase("alter"))
			{
				final ImplAlterStatement stmt = ImplAlterStatement.parse(sql, token);
				connection.getBackend().alterTable(stmt);
			}
			else
				throw new java.sql.SQLException("unimplemented command: " + token[0]);
			return 1;
		}
		catch (final java.sql.SQLException e)
		{
			e.printStackTrace();
			log("unsupported SQL in executeUpdate: " + originalSql);
			throw e;
		}
	}

	@Override
	public int executeUpdate(final String sql, final int a) throws java.sql.SQLException
	{
		return executeUpdate(sql);
	}

	@Override
	public int executeUpdate(final String sql, final int[] a) throws java.sql.SQLException
	{
		return executeUpdate(sql);
	}

	@Override
	public int executeUpdate(final String sql, final String[] a) throws java.sql.SQLException
	{
		return executeUpdate(sql);
	}

	@Override
	public void close() throws java.sql.SQLException
	{

	}

	@Override
	public int getMaxFieldSize() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public void setMaxFieldSize(final int max) throws java.sql.SQLException
	{
	}

	@Override
	public int getMaxRows() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public void setMaxRows(final int max) throws java.sql.SQLException
	{
	}

	@Override
	public void setEscapeProcessing(final boolean enable) throws java.sql.SQLException
	{
	}

	@Override
	public int getQueryTimeout() throws java.sql.SQLException
	{
		return 60;
	}

	@Override
	public void setQueryTimeout(final int seconds) throws java.sql.SQLException
	{
	}

	@Override
	public void cancel() throws java.sql.SQLException
	{
	}

	@Override
	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
	{
		return null;
	}

	@Override
	public void clearWarnings() throws java.sql.SQLException
	{
	}

	@Override
	public void setCursorName(final String Name) throws java.sql.SQLException
	{
	}

	@Override
	public boolean execute(String sql) throws java.sql.SQLException
	{
		lastSQL = sql;
		try
		{
			final String[] token = new String[1];
			sql = split(sql, token);
			if (!token[0].equalsIgnoreCase("select"))
			{
				return executeUpdate(lastSQL) == 0;
			}
			final ImplSelectStatement stmt = ImplSelectStatement.parse(this,sql, token);
			myResultSet = (ResultSet) connection.getBackend().constructScan(stmt);
			return true;
		}
		catch (final java.sql.SQLException e)
		{
			log("unsupported SQL in executeQuery: " + sql);
			throw e;
		}
	}

	@Override
	public boolean execute(final String sql, final int a) throws java.sql.SQLException
	{
		return execute(sql, 0);
	}

	@Override
	public boolean execute(final String sql, final int[] a) throws java.sql.SQLException
	{
		return execute(sql, 0);
	}

	@Override
	public boolean execute(final String sql, final String[] a) throws java.sql.SQLException
	{
		return execute(sql, 0);
	}

	@Override
	public java.sql.ResultSet getResultSet() throws java.sql.SQLException
	{
		return myResultSet;
	}

	@Override
	public int getUpdateCount() throws java.sql.SQLException
	{
		log("getUpdateCount");
		return -1;
	}

	public long getLongUpdateCount()
	{
		log("getLongUpdateCount");
		return -1;
	}

	@Override
	public boolean getMoreResults() throws java.sql.SQLException
	{
		return false;
	}

	@Override
	public boolean getMoreResults(final int a) throws java.sql.SQLException
	{
		return false;
	}

	@Override
	public int getResultSetHoldability() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public void setFetchDirection(final int i) throws java.sql.SQLException
	{
	}

	@Override
	public int getFetchDirection() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public void addBatch(final String a) throws java.sql.SQLException
	{
	}

	@Override
	public void clearBatch() throws java.sql.SQLException
	{
	}

	@Override
	public int[] executeBatch() throws java.sql.SQLException
	{
		return null;
	}

	@Override
	public void setFetchSize(final int i) throws java.sql.SQLException
	{
	}

	@Override
	public int getFetchSize() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public int getResultSetConcurrency() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public int getResultSetType() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public java.sql.ResultSet getGeneratedKeys() throws java.sql.SQLException
	{
		return null;
	}

	@Override
	public void closeOnCompletion() throws SQLException
	{
		closeStatementOnResultSetClose = true;
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException
	{
		return closeStatementOnResultSetClose;
	}
}
