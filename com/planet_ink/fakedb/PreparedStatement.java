package com.planet_ink.fakedb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

/*
   Copyright 2009-2018 Bo Zimmerman

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
public class PreparedStatement extends Statement implements java.sql.PreparedStatement
{
	private Backend.ImplAbstractStatement	stmt	= null;

	PreparedStatement(Connection c)
	{
		super(c);
	}

	public void prepare(String sql) throws java.sql.SQLException
	{
		lastSQL = sql;
		final String originalSql = sql;
		try
		{
			final String[] token = new String[1];
			sql = split(sql, token);
			if (token[0].equalsIgnoreCase("insert"))
			{
				stmt = parseInsert(sql, token);
			}
			else 
			if (token[0].equalsIgnoreCase("update"))
			{
				stmt = parseUpdate(sql, token);
			}
			else 
			if (token[0].equalsIgnoreCase("delete"))
			{
				stmt = parseDelete(sql, token);
			}
			else 
			if (token[0].equalsIgnoreCase("select"))
			{
				stmt = parseSelect(sql, token);
			}
			else
				throw new java.sql.SQLException("unimplemented command: " + token[0]);
		}
		catch (final java.sql.SQLException e)
		{
			e.printStackTrace();
			log("unsupported SQL in preparedStatement: " + originalSql);
			throw e;
		}
	}

	@Override
	public void addBatch() throws SQLException
	{
		log("unsupported method: addBatch");
	}

	@Override
	public void clearParameters() throws SQLException
	{
		log("unsupported method: clearParameters");
	}

	@Override
	public boolean execute() throws SQLException
	{
		switch (stmt.getStatementType())
		{
		case SELECT:
			myResultSet = (ResultSet) connection.getBackend().constructScan((Backend.ImplSelectStatement) stmt);
			return true;
		case UPDATE:
			connection.getBackend().updateRecord((Backend.ImplUpdateStatement) stmt);
			return true;
		case DELETE:
			connection.getBackend().deleteRecord((Backend.ImplDeleteStatement) stmt);
			return true;
		case INSERT:
		{
			final Backend.ImplInsertStatement istmt = (Backend.ImplInsertStatement) stmt;
			connection.getBackend().dupKeyCheck(istmt.tableName, istmt.columns, istmt.sqlValues);
			connection.getBackend().insertValues(istmt);
			return true;
		}
		}
		return false;
	}

	@Override
	public java.sql.ResultSet executeQuery() throws SQLException
	{
		if (stmt.getStatementType() != Backend.StatementType.SELECT)
			throw new SQLException("Not a query.");
		return connection.getBackend().constructScan((Backend.ImplSelectStatement) stmt);
	}

	@Override
	public int executeUpdate() throws SQLException
	{
		switch (stmt.getStatementType())
		{
		case SELECT:
			throw new SQLException("Not a update.");
		case UPDATE:
			connection.getBackend().updateRecord((Backend.ImplUpdateStatement) stmt);
			return 0;
		case DELETE:
			connection.getBackend().deleteRecord((Backend.ImplDeleteStatement) stmt);
			return 0;
		case INSERT:
		{
			final Backend.ImplInsertStatement istmt = (Backend.ImplInsertStatement) stmt;
			connection.getBackend().dupKeyCheck(istmt.tableName, istmt.columns, istmt.sqlValues);
			connection.getBackend().insertValues(istmt);
			return 0;
		}
		}
		return -1;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException
	{
		log("unsupported method: getMetaData");
		return null;
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException
	{
		log("unsupported method: getParameterMetaData");
		return null;
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
	{
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int c = 0;
		while (c >= 0)
		{
			try
			{
				c = x.read();
				if (c >= 0)
				{
					bout.write(c);
				}
			}
			catch (final IOException ioe)
			{
				throw new SQLException(ioe.getMessage());
			}
		}
		final byte[] b = bout.toByteArray();
		final char[] cs = new char[b.length];
		for (int i = 0; i < b.length; i++)
			cs[i] = (char) b[i];
		setObject(parameterIndex, new String(cs));
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
	{
		final byte[] b = new byte[length];
		int len = length;
		try
		{
			len = x.read(b, 0, length);
		}
		catch (final Exception e)
		{
			log(e.getMessage());
		}
		final char[] cs = new char[len];
		for (int i = 0; i < len; i++)
			cs[i] = (char) b[i];
		setObject(parameterIndex, new String(cs));
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
	{
		setAsciiStream(parameterIndex, x, (int) length);
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException
	{
		setObject(parameterIndex, Integer.valueOf(x ? 0 : 1));
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
	{
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int c = 0;
		while (c >= 0)
		{
			try
			{
				c = reader.read();
				if (c >= 0)
				{
					bout.write(c);
				}
			}
			catch (final IOException ioe)
			{
				throw new SQLException(ioe.getMessage());
			}
		}
		setObject(parameterIndex, new String(bout.toByteArray()));
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
	{
		final char[] b = new char[length];
		int len = length;
		try
		{
			len = reader.read(b, 0, length);
		}
		catch (final Exception e)
		{
			log(e.getMessage());
		}
		setObject(parameterIndex, new String(b, 0, len));
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
	{
		setCharacterStream(parameterIndex, reader, (int) length);
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException
	{
		if (x == null)
			setObject(parameterIndex, null);
		else
			setObject(parameterIndex, x.getSubString(0, (int) x.length()));
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException
	{
		setCharacterStream(parameterIndex, reader);
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
	{
		setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException
	{
		if (x == null)
			setObject(parameterIndex, null);
		else
			setObject(parameterIndex, Long.valueOf(x.getTime()));
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
	{
		if (x == null)
			setObject(parameterIndex, null);
		else
			setObject(parameterIndex, Long.valueOf(x.getTime()));
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException
	{
		setObject(parameterIndex, Integer.valueOf(x));
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException
	{
		setObject(parameterIndex, Long.valueOf(x));
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
	{
		setCharacterStream(parameterIndex, value);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
	{
		setCharacterStream(parameterIndex, value, length);
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException
	{
		setClob(parameterIndex, value);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException
	{
		setClob(parameterIndex, reader);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
	{
		setClob(parameterIndex, reader, length);
	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException
	{
		setString(parameterIndex, value);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException
	{
		setObject(parameterIndex, null);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
	{
		setObject(parameterIndex, null);
	}

	@SuppressWarnings("rawtypes")
	private boolean setRecursiveObject(final int parameterIndex, final Object x, final List<Backend.FakeCondition> conds, final int[] atIndex)
	{
		if ((conds != null) && (conds.size() > 0))
		{
			for (final Backend.FakeCondition cond : conds)
			{
				if (cond.unPrepared)
				{
					if (atIndex[0] == parameterIndex)
					{
						if (x == null)
							cond.conditionValue = new Backend.ComparableValue(null);
						else 
						if (x instanceof Comparable)
							cond.conditionValue = new Backend.ComparableValue((Comparable) x);
						else
							cond.conditionValue = new Backend.ComparableValue(x.toString());
						return true;
					}
					atIndex[0]++;
				}
				if (setRecursiveObject(parameterIndex, x, cond.contains, atIndex))
					return true;
			}
		}
		return false;
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException
	{
		int atIndex = 1;
		for (int v = 0; v < stmt.unPreparedValuesFlags().length; v++)
		{
			if (stmt.unPreparedValuesFlags()[v].booleanValue())
			{
				if (atIndex == parameterIndex)
				{
					if (x == null)
						stmt.values()[v] = null;
					else
						stmt.values()[v] = x.toString();
					return;
				}
				atIndex++;
			}
		}
		final int[] atIndexA = new int[] { atIndex };
		if (setRecursiveObject(parameterIndex, x, stmt.conditions(), atIndexA))
			return;
		throw new SQLException("Invalid index: > " + atIndexA[0]);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
	{
		setObject(parameterIndex, x);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException
	{
		setObject(parameterIndex, x);
	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException
	{
		setInt(parameterIndex, x);
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException
	{
		setObject(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException
	{
		if (x == null)
			setObject(parameterIndex, null);
		else
			setObject(parameterIndex, Long.valueOf(x.getTime()));
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
	{
		if (x == null)
			setObject(parameterIndex, null);
		else
			setObject(parameterIndex, Long.valueOf(x.getTime()));
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
	{
		if (x == null)
			setObject(parameterIndex, null);
		else
			setObject(parameterIndex, Long.valueOf(x.getTime()));
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
	{
		if (x == null)
			setObject(parameterIndex, null);
		else
			setObject(parameterIndex, Long.valueOf(x.getTime()));
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
	{
		// TODO Auto-generated method stub

	}

}
