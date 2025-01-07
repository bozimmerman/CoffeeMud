package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class DBConnection
{
	/** Connection object being used */
	private Connection			myConnection		= null;

	/** (new) resultset being used currently */
	private volatile ResultSet	myResultSet			= null;

	/** (new) statement object being used currently */
	private volatile Statement	myStatement			= null;

	/** (new) statement object being used currently */
	private volatile PreparedStatement	myPreparedStatement	= null;

	/** Whether this dbconnection is being used */
	protected volatile boolean	inUse;

	/** if any SQL errors occur, they are here. **/
	protected String			lastError			= null;

	/** last time the connection was queried/executed. **/
	private volatile long		lastQueryTime		= System.currentTimeMillis();

	/** when this connection was put into use **/
	private volatile long		lastPutInUseTime	= System.currentTimeMillis();

	/** number of failures in a row */
	protected volatile int		failuresInARow		= 0;

	protected volatile boolean	sqlserver			= false;

	protected boolean			isReusable			= false;

	/** parent container of this connection **/
	private DBConnections		myParent			= null;

	/** for tracking the last sql statement made */
	protected volatile String	lastSQL				= "";

	/** for remembering whether this is a fakeDB connection */
	private Boolean				isFakeDB			= null;
	
	/** Track the thread that owns the connection */
	private volatile Thread		lastThread			= null;

	public static enum FetchType
	{
		EMPTY,
		STATEMENT,
		PREPAREDSTATEMENT,
		TESTSTATEMENT
	}

	/**
	 * construction
	 *
	 * Usage: DBConnection("","","");
	 * @param parent 	the parent connections object
	 * @param dbClass    JDBC Class
	 * @param dbService    ODBC SERVICE
	 * @param dbUser	ODBC LOGIN USERNAME
	 * @param dbPass	ODBC LOGIN PASSWORD
	 * @param dbParms	JDBC extra arguments
	 * @param useTransactions true to group statements into a transaction
	 * @param dbReuse   Whether the connection can be reused.
	 * @throws SQLException a sql error
	 */
	public DBConnection(final DBConnections parent,
						String dbClass,
						final String dbService,
						final String dbUser,
						final String dbPass,
						final Map<String, String> dbParms,
						final boolean useTransactions,
						final boolean dbReuse)
		throws SQLException
	{
		myParent=parent;
		if((dbClass==null)||(dbClass.length()==0))
			dbClass="sun.jdbc.odbc.JdbcOdbcDriver";
		try
		{
			Class.forName(dbClass);
		}
		catch(final ClassNotFoundException ce)
		{
			ce.printStackTrace();
		}
		sqlserver=true;
		isReusable=dbReuse;
		final java.util.Properties p = new java.util.Properties();
		if((dbUser!=null)
		&&(dbUser.length()>0))
		{
			p.put("user",dbUser);
			p.put("password",dbPass);
		}
		for(final String key : dbParms.keySet())
			p.put(key, dbParms.get(key));
		p.put("SetBigStringTryClob", "true");
		myConnection=DriverManager.getConnection(dbService,p);
		if(useTransactions)
			myConnection.setAutoCommit(false);
		else
			myConnection.setAutoCommit(true);
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
			Log.debugOut("New connection made to :"+dbService+" using "+dbClass);
		sqlserver=false;
		inUse=false;
	}

	public String catalog()
	{
		try
		{
			return myConnection.getCatalog();
		}
		catch (final Exception e)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
			{
				Log.errOut("DBConnection",e,"catalog");
			}
		}
		return "";
	}

	public boolean isFakeDB()
	{
		if(isFakeDB==null)
		{
			final String catalog = catalog();
			if(catalog==null)
				isFakeDB=Boolean.FALSE;
			else
				isFakeDB = Boolean.valueOf(catalog.equalsIgnoreCase("FAKEDB"));
		}
		return isFakeDB.booleanValue();
	}

	/**
	 * shut down this connection totally
	 *
	 * Usage: close()
	 */
	public void close()
	{
		try
		{
			if(myStatement!=null)
				myStatement.close();
		}
		catch (final SQLException e)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				Log.errOut("DBConnection",e,"sclose");
		}
		try
		{
			if(myPreparedStatement!=null)
				myPreparedStatement.close();
		}
		catch (final SQLException e)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				Log.errOut("DBConnection",e,"pclose");
		}
		try
		{
			if(myConnection!=null)
				myConnection.close();
		}
		catch (final SQLException e)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				Log.errOut("DBConnection",e,"cclose");
		}
		myConnection=null;
		myStatement=null;
		myPreparedStatement=null;
		myParent=null;
	}

	/**
	 * set up this connection for use
	 *
	 * Usage: use("begin transaction")
	 * @param openerSQL    Any SQL string you'd like to send
	 * @return boolean    The connection being used
	 */
	public synchronized boolean use(final String openerSQL)
	{
		if((!inUse)&&(ready())&&(!isProbablyDead()))
		{
			this.lastThread = Thread.currentThread();
			lastError=null;
			try
			{
				myPreparedStatement=null;
				sqlserver=true;
				myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			}
			catch(final SQLException e)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
					Log.errOut("DBConnection",e,"use1");
				myConnection=null;
				failuresInARow++;
				sqlserver=false;
				return false;
			}

			sqlserver=false;
			try
			{
				if(!openerSQL.equals(""))
				{
					lastSQL=openerSQL;
					lastQueryTime=System.currentTimeMillis();
					myStatement.executeUpdate(openerSQL);
				}
			}
			catch(final SQLException e)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				{
					Log.errOut("Error use: "+openerSQL);
					Log.errOut("DBConnection",e);
				}
				return false;
				// not a real error?!
			}

			lastPutInUseTime=System.currentTimeMillis();
			inUse=true;
			return true;
		}
		return false;
	}

	/**
	 * set up this connection for use
	 *
	 * Usage: useEmpty()
	 * @return boolean    The connection being used
	 */
	public synchronized boolean useEmpty()
	{
		if((!inUse)&&(ready())&&(!isProbablyDead()))
		{
			this.lastThread = Thread.currentThread();
			lastError=null;
			myPreparedStatement=null;
			sqlserver=true;
			myStatement=null;
			sqlserver=false;
			lastPutInUseTime=System.currentTimeMillis();
			inUse=true;
			return true;
		}
		return false;
	}

	/**
	 * set up this connection for use as a prepared statement
	 *
	 * Usage: usePrepared("SQL String")
	 * @param SQL    Any SQL string you'd like to use
	 * @return boolean    The connection being used
	 */
	public synchronized boolean usePrepared(final String SQL)
	{
		if((!inUse)&&(ready()))
		{
			this.lastThread = Thread.currentThread();
			lastError=null;
			try
			{
				myStatement=null;
				sqlserver=true;
				lastSQL=SQL;
				myPreparedStatement=myConnection.prepareStatement(SQL);
				sqlserver=false;
			}
			catch(final SQLException e)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				{
					Log.errOut("Error prepare: "+SQL);
					Log.errOut("DBConnection",e);
				}
				sqlserver=false;
				myConnection=null;
				failuresInARow++;
				return false;
			}

			sqlserver=false;
			lastPutInUseTime=System.currentTimeMillis();
			failuresInARow=0;
			inUse=true;
			return true;
		}
		return false;
	}

	/**
	 * set up this connection for use as a prepared statement
	 * Requires an already in use connection.
	 *
	 * Usage: rePrepare("SQL String")
	 * @param SQL    Any SQL string you'd like to use
	 * @return boolean    The connection being used
	 */
	public synchronized boolean rePrepare(final String SQL)
	{
		if(inUse)
		{
			closeStatements("");
			lastError=null;
			try
			{
				myStatement=null;
				sqlserver=true;
				lastSQL=SQL;
				myPreparedStatement=myConnection.prepareStatement(SQL);
				sqlserver=false;
			}
			catch(final SQLException e)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				{
					Log.errOut("Error reprepare: "+SQL);
					Log.errOut("DBConnection",e);
				}
				else
				if((""+e).equals("null"))
					Log.errOut("Re-prepare error: null");
				else
					Log.errOut("Re-prepare error: "+e.getMessage());
				sqlserver=false;
				myConnection=null;
				failuresInARow++;
				return false;
			}

			sqlserver=false;
			lastPutInUseTime=System.currentTimeMillis();
			failuresInARow=0;
			inUse=true;
			return true;
		}
		return false;
	}

	protected void closeStatements(final String Closer)
	{
		try
		{
			if(!Closer.equals(""))
			{
				if(myStatement!=null)
				{
					lastSQL=Closer;
					lastQueryTime=System.currentTimeMillis();
					myStatement.executeUpdate(Closer);
				}
			}
			if(myResultSet!=null)
			{
				myResultSet.close();
				myResultSet=null;
			}
			if(myConnection!=null)
			{
				if(!myConnection.getAutoCommit())
					myConnection.commit();
			}
			if(myPreparedStatement!=null)
			{
				myPreparedStatement.close();
				myPreparedStatement=null;
			}
			if(myStatement!=null)
			{
				myStatement.close();
				myStatement=null;
			}
		}
		catch(final SQLException e)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
			{
				Log.errOut("DBConnection",e,"closestat");
			}
			// not a real error?
		}
	}

	/**
	 * report this connection as being free
	 *
	 * Usage: doneUsing("roll back");
	 * @param Closer	Any SQL string you'd like to send
	 */
	protected void doneUsing(final String Closer)
	{
		closeStatements(Closer);
		if(!isReusable)
			close();
		this.lastThread = null;
		inUse=false;
	}

	/**
	 * Return the time, in millis, when this connection
	 * was last returned.
	 * @return the last time a query was made on this conn
	 */
	public long getLastQueryTime()
	{
		return lastQueryTime;
	}

	/**
	 * execute a query, returning the resultset
	 *
	 * Usage: R=query("SELECT STATEMENT");
	 * @param queryString    SQL query-style string
	 * @return ResultSet	The results of the query
	 * @throws SQLException a sql error
	 */
	public ResultSet query(final String queryString)
		throws SQLException
	{
		lastSQL=queryString;
		ResultSet R=null;
		if((inUse)&&(ready()))
		{
			try
			{
				sqlserver=true;
				lastQueryTime=System.currentTimeMillis();
				if(myPreparedStatement!=null)
					R=myPreparedStatement.executeQuery();
				else
				if(myStatement!=null)
					R=myStatement.executeQuery(queryString);
				else
					lastError="DBConnection Statement not open.";
				sqlserver=false;
			}
			catch(final SQLException sqle)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				{
					Log.errOut("Error query: "+queryString);
					Log.errOut(""+sqle);
				}
				else
				if((""+sqle).equals("null"))
					Log.errOut("Query error: null");
				else
					Log.errOut("Query error: "+sqle.getMessage()+": "+queryString);
				sqlserver=false;
				failuresInARow++;
				lastError=""+sqle;
				if(isProbablyDead())
				{
					if(myParent!=null)
						myParent.resetConnections();
				}
				throw sqle;
			}
			sqlserver=false;
		}
		else
		{
			lastError="DBConnection not ready.";
		}
		sqlserver=false;
		failuresInARow=0;
		lastPutInUseTime=System.currentTimeMillis();
		if(myParent!=null)
			myParent.clearErrors();
		myResultSet=R;
		return R;
	}

	/**
	 * Sets all the clobs in the prepared statement to the given strings
	 * @param vals the strings, in order
	 * @throws SQLException a sql error
	 */
	public void setPreparedClobs(final String[] vals) throws SQLException
	{
		if(getPreparedStatement()==null)
		{
			return;
		}
		for(int t=0;t<vals.length;t++)
		{
			if(vals[t]==null)
				getPreparedStatement().setNull(t+1, java.sql.Types.CLOB);
			else
				getPreparedStatement().setString(t+1, vals[t]);
		}
	}

	/**
	 * execute an sql update, returning the status
	 *
	 * Usage: update("UPDATE STATEMENT");
	 * @param updateString    SQL update-style string
	 * @param retryNum    a retry number
	 * @return int    The status of the update
	 * @throws SQLException a sql error
	 */
	public int update(final String updateString, final int retryNum)
		throws SQLException
	{
		lastSQL=updateString;
		int responseCode=-1;
		if((inUse)&&(ready()))
		{
			try
			{
				sqlserver=true;
				lastQueryTime=System.currentTimeMillis();
				if(myStatement!=null)
					responseCode=myStatement.executeUpdate(updateString);
				else
				if(myPreparedStatement!=null)
					responseCode=myPreparedStatement.executeUpdate();
				else
				{
					myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
					responseCode=myStatement.executeUpdate(updateString);
				}
				sqlserver=false;
			}
			catch(final SQLException sqle)
			{
				sqlserver=false;
				if((sqle.getMessage()==null)
				||(sqle.getMessage().toUpperCase().indexOf("PRIMARY KEY")<0))
					failuresInARow++;
				lastError=""+sqle;
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
				{
					Log.errOut("Error update: "+updateString);
					Log.errOut("DBConnection",sqle);
				}
				else
					Log.errOut(updateString+": "+sqle);
				if((myParent!=null) && (myStatement != null))
					myParent.enQueueError(updateString,""+sqle,""+(retryNum+1));
				if(isProbablyDead())
				{
					if(myParent!=null)
						myParent.resetConnections();
				}
				throw sqle;
			}
		}

		sqlserver=false;
		lastPutInUseTime=System.currentTimeMillis();
		failuresInARow=0;
		if(myParent!=null)
			myParent.clearErrors();
		return responseCode;
	}

	/**
	 * returns whether this connection is ready for use
	 *
	 * Usage: ready();
	 * @return boolean    Whether this connection is ready
	 */
	public boolean ready()
	{
		return (myConnection!=null);
	}

	/**
	 * returns whether this connection is in use
	 *
	 * Usage: inUse();
	 * @return boolean    Whether this connection is in use
	 */
	public boolean inUse()
	{
		return inUse;
	}

	/**
	 * Returns the number of records in the given result set.
	 * @param R the result set
	 * @return the number of records
	 */
	public int getRecordCount(final ResultSet R)
	{
		int recordCount=0;
		try
		{
			R.last();
			recordCount=R.getRow();
			R.beforeFirst();
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
		return recordCount;
	}

	/**
	 * known errors should not be a reason to report a dead state
	 *
	 * Usage: clearFailures();
	 */
	public void clearFailures()
	{
		failuresInARow=0;
	}

	public boolean inSQLServerCommunication()
	{
		return sqlserver;
	}

	/**
	 * If the connection is in use, returns the status of the thread
	 * that has claimed it.
	 * 
	 * @return true if the thread is alive, false if no claimed thread, or its not alive
	 */
	public boolean isThreadAlive()
	{
		final Thread t = this.lastThread;
		if(t == null)
			return false;
		final java.lang.StackTraceElement[] s=t.getStackTrace();
		boolean isAlive=t.isAlive();
		if(isAlive)
		{
			for (final StackTraceElement element : s)
			{
				if(element.getMethodName().equalsIgnoreCase("sleep")
				&&(element.getClassName().equalsIgnoreCase("java.lang.Thread")))
					isAlive=false;
				else
				if(element.getMethodName().equalsIgnoreCase("park")
				&&(element.getClassName().equalsIgnoreCase("sun.misc.Unsafe")))
					isAlive=false;
				else
				if(element.getMethodName().equalsIgnoreCase("wait")
				&&(element.getClassName().equalsIgnoreCase("java.lang.Object")))
					isAlive=false;
				break;
			}
		}
		return isAlive;
	}
	
	/**
	 * Returns an empty stack trace, or a full one if the connection has
	 * an owner thread and its not dead.
	 *  
	 * @return a stack trace, always
	 */
	public java.lang.StackTraceElement[] getStackTrace()
	{
		final Thread t = this.lastThread;
		if(!isThreadAlive())
			return new java.lang.StackTraceElement[0];
		return t.getStackTrace();
	}

	/**
	 * returns whether this connection is *probably* dead
	 *
	 * Usage: isProbablyDead();
	 * @return boolean    Whether this connection is probably dead
	 */
	public boolean isProbablyDead()
	{
		try
		{
			if((myConnection==null)||(myConnection.isClosed())||(failuresInARow>2))
				return true;
			return false;
		}
		catch(final SQLException e)
		{
			return true;
		}
	}

	/**
	 * returns whether this connection is *probably* locked up
	 *
	 * Usage: isProbablyLockedUp();
	 * @return boolean    Whether this connection is locked up
	 */
	public boolean isProbablyLockedUp()
	{
		final long twominsAgo=System.currentTimeMillis()-(2*60*1000);
		if((lastPutInUseTime<twominsAgo)&&inUse)
			return true;
		return false;
	}

	/**
	 * returns an error if there was one
	 *
	 * Usage: getLastError();
	 * @return String    The last error SQL string, if any
	 */
	public String getLastError()
	{
		if(lastError==null)
			return "";
		return lastError;
	}

	/**
	 * returns the prepared statement, if creates
	 *
	 * Usage: getPreparedStatement();
	 * @return PreparedStatement	the prepared statement
	 */
	public PreparedStatement getPreparedStatement()
	{
		return myPreparedStatement;
	}
}
