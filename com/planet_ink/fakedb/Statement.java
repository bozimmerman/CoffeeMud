package com.planet_ink.fakedb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.planet_ink.fakedb.Backend.ComparableValue;
import com.planet_ink.fakedb.Backend.FakeTable;

/*
   Copyright 2001 Thomas Neumann
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Statement implements java.sql.Statement
{
	protected ResultSet	myResultSet						= null;
	protected boolean	closeStatementOnResultSetClose	= false;

	static protected void log(String x)
	{
		System.err.println("Statement: " + x);
	}

	protected Connection	connection;
	public String			lastSQL	= "null";

	Statement(Connection c)
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

	protected String split(String sql, String[] token)
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
			int index;
			for (index = 0; index < sql.length(); index++)
			{
				char c = sql.charAt(index);
				if (c == ' ')
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

	protected String splitColumns(String sql, List<String> cols)
	{
		int s = 0;
		while ((sql.length() > 0) && (s < sql.length()))
		{
			if ((s < sql.length()) && ((sql.charAt(s) == ' ') || (sql.charAt(s) == '\t')))
				s++;
			if (s >= sql.length())
				return "";
			int e = s;
			while ((e < sql.length()) && (sql.charAt(e) != ' ') && (sql.charAt(e) != '\t') && (sql.charAt(e) != ','))
				e++;
			if (e >= sql.length()) // was whatever it was the last word.. done
				return sql.substring(s);
			final String word = sql.substring(s, e);
			cols.add(word);
			if (sql.charAt(e) != ',')
			{
				while ((e < sql.length()) && ((sql.charAt(e) == ' ') || (sql.charAt(e) == '\t')))
					e++;
			}
			if ((e >= sql.length()) || (sql.charAt(e) != ','))
				return sql.substring(e);
			while (sql.charAt(e) == ',')
				e++;
			s = e;
		}
		return "";
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		return connection.isClosed();
	}

	@Override
	public void setPoolable(boolean isPoolable)
	{
	}

	@Override
	public boolean isPoolable() throws SQLException
	{
		return false;
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException
	{
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException
	{
		return null;
	}

	public String parseWhereClause(String tableName, String sql, List<Backend.FakeCondition> conditions) throws java.sql.SQLException
	{
		int s = 0;
		final String eow1 = " \t!=><";
		final java.util.Stack<List<Backend.FakeCondition>> parenStack = new java.util.Stack<List<Backend.FakeCondition>>();
		while (s < sql.length())
		{
			while ((s < sql.length()) && (sql.charAt(s) == ' ' || sql.charAt(s) == '\t'))
				s++;
			if (s >= sql.length())
				break;
			Backend.FakeCondition condition = null;
			if (sql.charAt(s) == '(')
			{
				condition = connection.getBackend().buildFakeCondition(tableName, null, null, null, false);
				conditions.add(condition);
				parenStack.push(conditions);
				condition.contains = new ArrayList<Backend.FakeCondition>();
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
					while ((e < sql.length()) && (sql.charAt(e) != ' ') && (sql.charAt(e) != '\t'))
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
				condition.connector = Backend.ConnectorType.AND;
			}
			else 
			if (peeker.equalsIgnoreCase("OR"))
			{
				s = e;
				condition.connector = Backend.ConnectorType.OR;
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

	public Backend.ImplSelectStatement parseSelect(String sql, String[] token) throws java.sql.SQLException
	{
		final List<String> cols = new ArrayList<String>();
		sql = splitColumns(sql, cols);
		if (cols.size() == 0)
			throw new java.sql.SQLException("no columns given");
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("from"))
			throw new java.sql.SQLException("no from clause");
		sql = split(sql, token);
		final String tableName = token[0];
		final List<Backend.FakeCondition> conditions = new ArrayList<Backend.FakeCondition>();
		String[] orderVars = null;
		String[] orderConditions = null;
		if (sql.length() > 0)
		{
			sql = split(sql, token);
			if (token[0].equalsIgnoreCase("where"))
			{
				sql = parseWhereClause(tableName, sql, conditions);
				if (conditions.size() == 0)
					throw new java.sql.SQLException("no more where clause!");
				sql = split(sql, token);
			}
			if ((token[0] != null) && (token[0].equalsIgnoreCase("order")))
			{
				sql = split(sql, token);
				if (!token[0].equalsIgnoreCase("by"))
					throw new java.sql.SQLException("no by token");
				sql = split(sql, token);
				orderVars = new String[] { token[0] };
				orderConditions = new String[1];
				if (sql.length() > 0)
				{
					split(sql, token);
					if (token[0].equalsIgnoreCase("ASC") || token[0].equalsIgnoreCase("DESC"))
					{
						orderConditions = new String[] { token[0].toUpperCase().trim() };
						sql = split(sql, token);
					}
				}
			}
			if (sql.length() > 0)
				throw new java.sql.SQLException("extra garbage: " + sql);
		}
		return new Backend.ImplSelectStatement(this, tableName, cols, conditions, orderVars, orderConditions);
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
			final Backend.ImplSelectStatement stmt = parseSelect(sql, token);
			return connection.getBackend().constructScan(stmt);
		}
		catch (final java.sql.SQLException e)
		{
			log("unsupported SQL in executeQuery: " + sql);
			throw e;
		}
	}

	protected static String skipWS(String sql)
	{
		int index;
		for (index = 0; index < sql.length(); index++)
		{
			final char c = sql.charAt(index);
			if ((c != ' ') && (c != '\t') && (c != '\r') && (c != '\n'))
				break;
		}
		if (index == 0)
			return sql;
		return sql.substring(index);
	}

	protected static String[] parseVal(String sql)
	{
		final String[] result = new String[3];
		sql = skipWS(sql);
		if (sql.length() == 0)
		{
			result[0] = result[1] = "";
			result[2] = null;
		}
		else 
		if (sql.charAt(0) == '\'')
		{
			final StringBuffer buffer = new StringBuffer();
			int index;
			for (index = 1; index < sql.length(); ++index)
			{
				char c = sql.charAt(index);
				if (c == '\'')
					break;
				if (c == '\\')
					c = sql.charAt(++index);
				buffer.append(c);
			}
			if (index >= sql.length())
				index = sql.length() - 1;
			result[0] = sql.substring(index + 1);
			result[1] = buffer.toString();
			result[2] = null;
		}
		else
		{
			final StringBuffer buffer = new StringBuffer();
			int index;
			for (index = 0; index < sql.length(); ++index)
			{
				final char c = sql.charAt(index);
				if ((c == ' ') || (c == ',') || (c == ')'))
					break;
				buffer.append(c);
			}
			result[0] = sql.substring(index);
			result[1] = buffer.toString();
			result[2] = buffer.toString().equals("?") ? "" : null;
		}
		return result;
	}

	protected Backend.ImplInsertStatement parseInsert(String sql, String[] token) throws java.sql.SQLException
	{
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("into"))
			throw new java.sql.SQLException("no into token");
		sql = split(sql, token);
		final String tableName = token[0];
		sql = skipWS(sql);
		if ((sql.length() < 0) || (sql.charAt(0) != '('))
			throw new java.sql.SQLException("no open paren");
		sql = sql.substring(1);

		final java.util.List<String> columnList = new java.util.LinkedList();
		while (true)
		{
			sql = skipWS(sql);
			int index = sql.indexOf(',');
			final int index2 = sql.indexOf(')');
			if ((index < 0) || (index2 < index))
				index = index2;
			if (index < 0)
				throw new java.sql.SQLException("no comma");
			columnList.add(sql.substring(0, index).trim());
			final char c = sql.charAt(index);
			sql = skipWS(sql.substring(index + 1));
			if (c == ')')
				break;
		}

		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("values"))
			throw new java.sql.SQLException("no values");
		sql = skipWS(sql);
		if ((sql.length() < 0) || (sql.charAt(0) != '('))
			throw new java.sql.SQLException("no value open paren");
		sql = sql.substring(1);

		final java.util.List<String> valuesList = new java.util.LinkedList<String>();
		final java.util.List<Boolean> unPreparedValueList = new java.util.LinkedList();
		while (true)
		{
			sql = skipWS(sql);
			final String[] r = parseVal(sql);
			final String val = r[1];
			sql = skipWS(r[0]);
			valuesList.add(val);
			unPreparedValueList.add(Boolean.valueOf(r[2] != null));
			if (sql.length() == 0)
				throw new java.sql.SQLException("no sql again");
			final char c = sql.charAt(0);
			sql = skipWS(sql.substring(1));
			if (c == ')')
				break;
			if (c != ',')
				throw new java.sql.SQLException("no comma before last paren");
		}
		if ((sql.length() > 0) && (sql.charAt(0) == ';'))
			sql = skipWS(sql.substring(1));
		if ((sql.length() > 0) || (columnList.size() != valuesList.size()))
		{
			throw new java.sql.SQLException("something very bad");
		}
		return new Backend.ImplInsertStatement(tableName, columnList.toArray(new String[0]), valuesList.toArray(new String[0]), unPreparedValueList.toArray(new Boolean[0]));
	}

	protected Backend.ImplUpdateStatement parseUpdate(String sql, String[] token) throws java.sql.SQLException
	{
		sql = split(sql, token);
		final String tableName = token[0];
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("set"))
			throw new java.sql.SQLException("no set");

		final java.util.List<String> columnList = new java.util.LinkedList();
		final java.util.List<String> valueList = new java.util.LinkedList();
		final java.util.List<Boolean> unPreparedValueList = new java.util.LinkedList();
		final StringBuffer buffer = new StringBuffer();
		while (true)
		{
			sql = skipWS(sql);
			buffer.setLength(0);
			while (sql.length() > 0)
			{
				final char c = sql.charAt(0);
				if ((c == '=') || (c == ' '))
					break;
				buffer.append(c);
				sql = sql.substring(1);
			}
			sql = skipWS(sql);
			final String attr = buffer.toString();
			if (sql.length() == 0)
				throw new java.sql.SQLException("no more sql");
			if (sql.charAt(0) != '=')
			{
				if (!attr.equalsIgnoreCase("where"))
					throw new java.sql.SQLException("no where");
				break;
			}
			sql = skipWS(sql.substring(1));
			if (sql.length() == 0)
				throw new java.sql.SQLException("no no sql no mo");
			buffer.setLength(0);
			if (sql.charAt(0) == '\'')
			{
				int sub = 1;
				for (; sub < sql.length(); sub++)
				{
					char c = sql.charAt(sub);
					if (c == '\'')
						break;
					if (c == '\\')
						c = sql.charAt(++sub);
					buffer.append(c);
				}
				columnList.add(attr);
				valueList.add(buffer.toString());
				sql = sql.substring(sub + 1);
				unPreparedValueList.add(Boolean.valueOf(false));
			}
			else
			{
				final String[] r = parseVal(sql);
				sql = r[0];
				columnList.add(attr);
				valueList.add(r[1]);
				unPreparedValueList.add(Boolean.valueOf(r[2] != null));
			}
			sql = skipWS(sql);
			if ((sql.length() > 0) && (sql.charAt(0) == ','))
				sql = skipWS(sql.substring(1));
		}
		final List<Backend.FakeCondition> conditions = new ArrayList<Backend.FakeCondition>();
		sql = parseWhereClause(tableName, sql, conditions);
		if (conditions.size() == 0)
			throw new java.sql.SQLException("no more where clause!");
		return new Backend.ImplUpdateStatement(tableName, conditions, columnList.toArray(new String[0]), valueList.toArray(new String[0]), unPreparedValueList.toArray(new Boolean[0]));

	}

	protected Backend.ImplDeleteStatement parseDelete(String sql, String[] token) throws java.sql.SQLException
	{
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("from"))
			throw new java.sql.SQLException("no from clause");
		sql = split(sql, token);
		final String tableName = token[0];
		sql = split(sql, token);
		List<Backend.FakeCondition> conditions;
		if (token[0].equalsIgnoreCase("where"))
		{
			sql = skipWS(sql);
			conditions = new ArrayList<Backend.FakeCondition>();
			sql = parseWhereClause(tableName, sql, conditions);
			if (conditions.size() == 0)
				throw new java.sql.SQLException("no more where clause!");
		}
		else 
		if (token.length > 0)
		{
			conditions = new ArrayList<Backend.FakeCondition>();
		}
		else
			throw new java.sql.SQLException("no other where clause");
		return new Backend.ImplDeleteStatement(tableName, conditions);
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
				final Backend.ImplInsertStatement stmt = parseInsert(sql, token);
				connection.getBackend().dupKeyCheck(stmt.tableName, stmt.columns, stmt.sqlValues);
				connection.getBackend().insertValues(stmt);
			}
			else 
			if (token[0].equalsIgnoreCase("update"))
			{
				final Backend.ImplUpdateStatement stmt = parseUpdate(sql, token);
				connection.getBackend().updateRecord(stmt);
			}
			else 
			if (token[0].equalsIgnoreCase("delete"))
			{
				final Backend.ImplDeleteStatement stmt = parseDelete(sql, token);
				connection.getBackend().deleteRecord(stmt);
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
	public int executeUpdate(String sql, int a) throws java.sql.SQLException
	{
		return executeUpdate(sql);
	}

	@Override
	public int executeUpdate(String sql, int[] a) throws java.sql.SQLException
	{
		return executeUpdate(sql);
	}

	@Override
	public int executeUpdate(String sql, String[] a) throws java.sql.SQLException
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
	public void setMaxFieldSize(int max) throws java.sql.SQLException
	{
	}

	@Override
	public int getMaxRows() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public void setMaxRows(int max) throws java.sql.SQLException
	{
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws java.sql.SQLException
	{
	}

	@Override
	public int getQueryTimeout() throws java.sql.SQLException
	{
		return 60;
	}

	@Override
	public void setQueryTimeout(int seconds) throws java.sql.SQLException
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
	public void setCursorName(String Name) throws java.sql.SQLException
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
			final Backend.ImplSelectStatement stmt = parseSelect(sql, token);
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
	public boolean execute(String sql, int a) throws java.sql.SQLException
	{
		return execute(sql, 0);
	}

	@Override
	public boolean execute(String sql, int[] a) throws java.sql.SQLException
	{
		return execute(sql, 0);
	}

	@Override
	public boolean execute(String sql, String[] a) throws java.sql.SQLException
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
	public boolean getMoreResults(int a) throws java.sql.SQLException
	{
		return false;
	}

	@Override
	public int getResultSetHoldability() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public void setFetchDirection(int i) throws java.sql.SQLException
	{
	}

	@Override
	public int getFetchDirection() throws java.sql.SQLException
	{
		return 0;
	}

	@Override
	public void addBatch(String a) throws java.sql.SQLException
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
	public void setFetchSize(int i) throws java.sql.SQLException
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

	public void closeOnCompletion() throws SQLException
	{
		closeStatementOnResultSetClose = true;
	}

	public boolean isCloseOnCompletion() throws SQLException
	{
		return closeStatementOnResultSetClose;
	}
}
