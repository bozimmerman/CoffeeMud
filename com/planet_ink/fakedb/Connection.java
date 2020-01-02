package com.planet_ink.fakedb;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.lang.ref.WeakReference;

/*
   Copyright 2001 Thomas Neumann
   Copyright 2004-2020 Bo Zimmerman

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
public class Connection implements java.sql.Connection
{
	static private java.util.Map<String, WeakReference<Backend>>	databases	= new java.util.HashMap<String, WeakReference<Backend>>();
	static private java.util.Map<String, Integer>					references	= new java.util.HashMap<String, Integer>();

	private Backend	backend;
	private boolean	closed	= false;
	private String	oldPath	= "";

	static private void log(final String x)
	{
		System.err.println("Connection: " + x);
	}

	Backend getBackend()
	{
		return backend;
	}

	public Connection(final String path) throws java.sql.SQLException
	{
		connect(path);
	}

	public String getOldPath()
	{
		return oldPath;
	}

	private void connect(String path) throws java.sql.SQLException
	{
		try
		{
			path = (new java.io.File(path)).getCanonicalPath();
		}
		catch (final java.io.IOException e)
		{
		}

		oldPath = path;
		if (!closed)
		{
			synchronized (references)
			{
				Integer conCount = references.get(path);
				if (conCount == null)
					conCount = Integer.valueOf(0);
				references.remove(path);
				references.put(path, Integer.valueOf(conCount.intValue() + 1));
			}
		}

		synchronized (databases)
		{
			final WeakReference<Backend> ref = databases.get(path);
			Backend backend = null;
			if (ref != null)
				backend = ref.get();
			if (backend == null)
			{
				backend = new Backend();
				if (!backend.open(new java.io.File(path)))
					throw new java.sql.SQLException("unable to open database");
				databases.put(path, new WeakReference<Backend>(backend));
			}
			this.backend = backend;
		}
	}

	@Override
	public java.sql.Statement createStatement() throws java.sql.SQLException
	{
		return new Statement(this);
	}

	@Override
	public java.sql.Statement createStatement(final int a, final int b) throws java.sql.SQLException
	{
		return createStatement();
	}

	@Override
	public java.sql.Statement createStatement(final int a, final int b, final int c) throws java.sql.SQLException
	{
		return createStatement();
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(final String sql) throws java.sql.SQLException
	{
		final PreparedStatement p = new PreparedStatement(this);
		p.prepare(sql);
		return p;
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(final String sql, final int a) throws java.sql.SQLException
	{
		return prepareStatement(sql);
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(final String sql, final int[] a) throws java.sql.SQLException
	{
		return prepareStatement(sql);
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(final String sql, final String[] a) throws java.sql.SQLException
	{
		return prepareStatement(sql);
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(final String sql, final int a, final int b) throws java.sql.SQLException
	{
		return prepareStatement(sql);
	}

	public java.sql.PreparedStatement prepareStatement(final String sql, final int a, final int[] b) throws java.sql.SQLException
	{
		return prepareStatement(sql);
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(final String sql, final int a, final int b, final int c) throws java.sql.SQLException
	{
		return prepareStatement(sql);
	}

	@Override
	public java.sql.CallableStatement prepareCall(final String sql) throws java.sql.SQLException
	{
		log("prepareCall");
		throw new java.sql.SQLException("Callable statments not suppoted.", "S1C00");
	}

	@Override
	public java.sql.CallableStatement prepareCall(final String sql, final int a, final int b) throws java.sql.SQLException
	{
		return prepareCall(sql);
	}

	@Override
	public java.sql.CallableStatement prepareCall(final String sql, final int a, final int b, final int c) throws java.sql.SQLException
	{
		return prepareCall(sql);
	}

	@Override
	public int getHoldability()
	{
		return java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
	}

	@Override
	public java.sql.Savepoint setSavepoint() throws java.sql.SQLException
	{
		throw new java.sql.SQLException("Savepoints not supported");
	}

	@Override
	public java.sql.Savepoint setSavepoint(final String S) throws java.sql.SQLException
	{
		throw new java.sql.SQLException("Savepoints not supported");
	}

	@Override
	public void rollback(final java.sql.Savepoint saved) throws java.sql.SQLException
	{
		throw new java.sql.SQLException("Savepoints not supported");
	}

	@Override
	public void releaseSavepoint(final java.sql.Savepoint saved) throws java.sql.SQLException
	{
		throw new java.sql.SQLException("Savepoints not supported");
	}

	@Override
	public String nativeSQL(final String sql) throws java.sql.SQLException
	{
		return sql;
	}

	@Override
	public void setAutoCommit(final boolean autoCommit) throws java.sql.SQLException
	{
		log("setAutoCommit");
		if (!autoCommit)
			throw new java.sql.SQLException("Cannot disable AUTO_COMMIT", "08003");
		return;
	}

	@Override
	public boolean getAutoCommit() throws java.sql.SQLException
	{
		return true;
	}

	@Override
	public void commit() throws java.sql.SQLException
	{
		// log("commit");
	}

	@Override
	public void rollback() throws java.sql.SQLException
	{
		// log("rollback");
	}

	@Override
	public void close() throws java.sql.SQLException
	{
		if (!closed)
		{
			closed = true;
			synchronized (references)
			{
				final Integer conCount = references.get(oldPath);
				if (conCount != null)
				{
					if (conCount.intValue() == 1)
					{
						if (backend != null)
							backend.clearFakeTables();
						backend = null;
						references.remove(oldPath);
						databases.remove(oldPath);
					}
					else
					{
						references.remove(oldPath);
						references.put(oldPath, Integer.valueOf(conCount.intValue() - 1));
					}
				}
			}
		}
	}

	@Override
	public boolean isClosed() throws java.sql.SQLException
	{
		return closed;
	}

	@Override
	public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException
	{
		log("getMetaData");
		return null;
	}

	@Override
	public void setReadOnly(final boolean readOnly) throws java.sql.SQLException
	{
		log("setReadOnly");
	}

	@Override
	public boolean isReadOnly() throws java.sql.SQLException
	{
		return false;
	}

	@Override
	public void setCatalog(final String Catalog) throws java.sql.SQLException
	{
		log("setCatalog");
	}

	@Override
	public String getCatalog() throws java.sql.SQLException
	{
		return "FAKEDB";
	}

	@Override
	public void setTransactionIsolation(final int level) throws java.sql.SQLException
	{
		log("setTransactionIsolation");
		throw new java.sql.SQLException("Transaction Isolation Levels are not supported.", "S1C00");
	}

	@Override
	public int getTransactionIsolation() throws java.sql.SQLException
	{
		return java.sql.Connection.TRANSACTION_NONE;
	}

	@Override
	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
	{
		log("getWarnings");
		return null;
	}

	@Override
	public void clearWarnings() throws java.sql.SQLException
	{
		log("clearWarnings");
	}

	@Override
	public void setHoldability(final int holdability) throws java.sql.SQLException
	{
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public java.util.Map getTypeMap() throws java.sql.SQLException
	{
		return new java.util.HashMap();
	}

	@Override
	public Array createArrayOf(final String arg0, final Object[] arg1) throws SQLException
	{
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException
	{
		return null;
	}

	@Override
	public Clob createClob() throws SQLException
	{
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException
	{
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException
	{
		return null;
	}

	@Override
	public Struct createStruct(final String arg0, final Object[] arg1) throws SQLException
	{
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException
	{
		return null;
	}

	@Override
	public String getClientInfo(final String arg0) throws SQLException
	{
		return null;
	}

	@Override
	public boolean isValid(final int arg0) throws SQLException
	{
		return false;
	}

	@Override
	public void setClientInfo(final Properties arg0) throws SQLClientInfoException
	{
	}

	@Override
	public void setClientInfo(final String arg0, final String arg1) throws SQLClientInfoException
	{
	}

	// public void setTypeMap(Map arg0) throws SQLException { }
	@Override
	public void setTypeMap(final Map<String, Class<?>> arg0) throws SQLException
	{
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException
	{
		return false;
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException
	{
		return null;
	}

	public void setSchema(final String schema) throws SQLException
	{
		connect(schema);
	}

	public String getSchema() throws SQLException
	{
		return oldPath;
	}

	public void abort(final Executor executor) throws SQLException
	{
		this.close();
	}

	public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException
	{
	}

	public int getNetworkTimeout() throws SQLException
	{
		return 0;
	}

	// JDK 1.4 stuff
	/*
	 * public int getHoldability() throws java.sql.SQLException { return
	 * java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT; } public void
	 * releaseSavepoint(java.sql.Savepoint savepoint) throws
	 * java.sql.SQLException {} public java.sql.Savepoint setSavepoint() throws
	 * java.sql.SQLException { return null; } public java.sql.Savepoint
	 * setSavepoint(String name) throws java.sql.SQLException { return null; }
	 * public void rollback(java.sql.Savepoint p) throws java.sql.SQLException {
	 * rollback(); }
	 */
}
