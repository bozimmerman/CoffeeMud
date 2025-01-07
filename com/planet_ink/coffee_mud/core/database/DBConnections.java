package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnection.FetchType;
import com.planet_ink.coffee_mud.core.database.DBConnector.DBPreparedBatchEntry;
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
public class DBConnections
{
	protected String			dbClass				= "";
	/** the odbc service */
	protected String			dbService			= "";
	/** the odbc login user */
	protected String			dbUser				= "";
	/** the odbc password */
	protected String			dbPass				= "";
	/** the jdbc args */
	protected Map<String,String>dbParms				= new Hashtable<String,String>();
	/** number of connections to make */
	protected int				maxConnections		= 0;
	/** the disconnected flag */
	protected boolean			disconnected		= false;
	/** the im in trouble flag */
	protected boolean			lockedUp			= false;
	/** the number of times the system has failed to get a db */
	protected int				consecutiveFailures	= 0;
	/** the number of times the system has failed a request */
	protected int				consecutiveErrors	= 0;
	/** Object to synchronize around on error handling */
	protected boolean			errorQueingEnabled	= false;
	/** the database connnections */
	protected List<DBConnection>connections;
	/** set this to true once, cuz it makes it all go away. **/
	protected boolean			shutdown			= false;
	/** whether to reuse connections */
	protected boolean			reuse				= false;
	/** whether to reuse connections */
	protected boolean			transact			= false;
	/** last time resetconnections called (or resetconnections) */
	private long				lastReset			= 0;
	/** check for whether these connections are fakedb */
	private Boolean				isFakeDB			= null;

	/**
	 * Initialize this class.  Must be called at first,
	 * and after any killConnections() calls.
	 *
	 * Usage: Initialize("ODBCSERVICE","USER","PASSWORD",10);
	 * @param dbClass	the odbc service
	 * @param dbService    the odbc service
	 * @param dbUser	the odbc user login
	 * @param dbPass	the odbc user password
	 * @param dbParms	extra jdbc parameters
	 * @param numConnections	Connections to maintain
	 * @param reuse    Whether to reuse connections
	 * @param transact true to group statements into a transaction
	 * @param doErrorQueueing    whether to save errors to a file
	 */
	public DBConnections(final String dbClass,
						 final String dbService,
						 final String dbUser,
						 final String dbPass,
						 final Map<String,String> dbParms,
						 final int numConnections,
						 final boolean reuse,
						 final boolean transact,
						 final boolean doErrorQueueing)
	{
		this.dbClass=dbClass;
		this.dbService=dbService;
		this.dbUser=dbUser;
		this.dbPass=dbPass;
		this.dbParms=dbParms;
		this.reuse=reuse;
		this.transact=transact;
		this.maxConnections=numConnections;
		this.connections = new SVector<DBConnection>();
		this.errorQueingEnabled=doErrorQueueing;
	}

	/**
	 * Usage: update("UPDATE...");
	 * @param updateStrings    the update SQL commands
	 * @return int    the responseCode, or -1
	 */
	public int update(final String[] updateStrings)
	{
		DBConnection dbConnection=null;
		int result=-1;
		String updateString=null;
		try
		{
			dbConnection=DBFetch();
			for (final String updateString2 : updateStrings)
			{
				updateString=updateString2;
				try
				{
					result=dbConnection.update(updateString,0);
				}
				catch(final Exception sqle)
				{
					if(sqle instanceof java.io.EOFException)
					{
						Log.errOut(""+sqle);
						DBDone(dbConnection);
						return -1;
					}
					if(sqle instanceof SQLException)
					{
						// queued by the connection for retry
					}
				}
				if(result<0)
				{
					Log.errOut(""+dbConnection.getLastError()+"/"+updateString);
				}
			}
		}
		catch(final Exception e)
		{
			enQueueError(updateString,""+e,""+0);
			reportError();
			Log.errOut(""+e);
		}
		finally
		{
			if(dbConnection!=null)
				DBDone(dbConnection);
		}
		return result;
	}

	/**
	 * Usage: updateWithClobs("UPDATE...");
	 * @param entries    the update SQL commands
	 * @return int    the responseCode, or -1
	 */
	public int updateWithClobs(final List<DBPreparedBatchEntry> entries)
	{
		DBConnection dbConnection=null;
		int result=-1;
		try
		{
			dbConnection=DBFetchEmpty();
			for(final DBPreparedBatchEntry entry : entries)
			{
				try
				{
					if((entry.clobs==null)||(entry.clobs.length==0))
					{
						dbConnection.closeStatements("");
						result=dbConnection.update(entry.sql,0);
					}
					else
					{
						dbConnection.rePrepare(entry.sql);
						for (final String[] clob : entry.clobs)
						{
							dbConnection.setPreparedClobs(clob);
							result=dbConnection.update("",0);
						}
					}
				}
				catch(final Exception sqle)
				{
					if(sqle instanceof java.io.EOFException)
					{
						Log.errOut(""+sqle);
						DBDone(dbConnection);
						return -1;
					}
					if(sqle instanceof SQLException)
					{
						// queued by the connection for retry
					}
				}
				if(result<0)
				{
					Log.errOut(""+dbConnection.getLastError()+"/"+entry.sql);
				}
			}
		}
		catch(final Exception e)
		{
			reportError();
			Log.errOut(""+e);
		}
		finally
		{
			if(dbConnection!=null)
				DBDone(dbConnection);
		}
		return result;
	}

	/**
	 * Usage: updateWithClobs("UPDATE...");
	 * @param updateStrings    the update SQL commands
	 * @param values	the update SQL command values
	 * @return int    the responseCode, or -1
	 */
	public int updateWithClobs(final String[] updateStrings, final String[][][] values)
	{
		final LinkedList<DBPreparedBatchEntry> entries = new LinkedList<DBPreparedBatchEntry>();
		for(int i=0;i<updateStrings.length;i++)
			entries.add(new DBPreparedBatchEntry(updateStrings[i],values[i]));
		return updateWithClobs(entries);
	}

	/**
	 * Usage: updateWithClobs("UPDATE...");
	 * @param updateString    the update SQL commands
	 * @param values	the update SQL values
	 * @return int    the responseCode, or -1
	 */
	public int updateWithClobs(final String updateString, final String[][] values)
	{
		return updateWithClobs(Arrays.asList(new DBPreparedBatchEntry(updateString,values)));
	}

	/**
	 * Return the number of connections made.
	 *
	 * Usage: n=numConnectionsMade();
	 * @return numConnectionsMade    The number of connections
	 */
	public int numConnectionsMade()
	{
		return connections.size();
	}

	public int numInUse()
	{
		int num=0;
		for(final DBConnection conn : connections)
		{
			if(conn.inUse())
				num++;
		}
		return num;
	}

	public int numAvailable()
	{
		int num=0;
		for(final DBConnection conn : connections)
		{
			if(!conn.inUse())
				num++;
		}
		return num;
	}

	/**
	 * Pings all connections not currently in use.
	 * @param querySql the query to use as a ping
	 * @param usageTimeoutMillis the idle time to use to decide which connections to ping.
	 * @return the number of connections actually pinged
	 */
	public int pingAllConnections(final String querySql, final long usageTimeoutMillis)
	{
		final LinkedList<DBConnection> fetched = new LinkedList<DBConnection>();
		int numPinged=0;
		while(numAvailable()>0)
		{
			final DBConnection dbConnection=DBFetch();
			if(dbConnection!=null)
			{
				fetched.add(dbConnection);
				if((System.currentTimeMillis()-dbConnection.getLastQueryTime())> usageTimeoutMillis)
				{
					try
					{
						dbConnection.query(querySql);
						numPinged++;
					}
					catch (final SQLException e)
					{
						Log.errOut(e.getMessage());
					}
				}
			}
		}
		for(final DBConnection dbConnection : fetched)
			DBDone(dbConnection);
		return numPinged;
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * Usage: DB=DBFetch();
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetch()
	{
		return DBFetchAny("",DBConnection.FetchType.STATEMENT);
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.  This is
	 * different than DBFetch because it ensures limited connection attempts.
	 *
	 * Usage: DB=DBFetchTest();
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchTest()
	{
		return DBFetchAny("",DBConnection.FetchType.TESTSTATEMENT);
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * Usage: DB=DBFetchPrepared();
	 * @param SQL    The prepared statement SQL
	 * @param type    the type of fetching to do
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchAny(final String SQL, final DBConnection.FetchType type)
	{
		DBConnection newConn=null;
		while(newConn==null)
		{
			if(shutdown)
			{
				throw new java.lang.IllegalArgumentException();
			}

			if(connections.size()<maxConnections)
			{
				try
				{
					newConn=new DBConnection(this,dbClass,dbService,dbUser,dbPass,dbParms,transact,reuse);
					connections.add(newConn);
				}
				catch(final Exception e)
				{
					final String errMsg = e.getMessage();
					if(errMsg!=null)
					{
						if(errMsg.indexOf("java.io.EOFException")<0)
							Log.errOut("DBConnections",e);
					}
					else
					if(e.getMessage()==null)
						Log.errOut("DBConnections",e);
					newConn=null;
					if(connections.size()==0)
					{
						disconnected=true;
						return null;
					}
					if(type==FetchType.TESTSTATEMENT)
						return null;
				}
			}
			if((newConn==null)&&(reuse))
			{
				try
				{
					for(final DBConnection conn : connections)
					{
						switch(type)
						{
						case PREPAREDSTATEMENT:
							if( conn.usePrepared(SQL))
							{
								newConn=conn;
							}
							break;
						case TESTSTATEMENT:
						case STATEMENT:
							if(conn.use(""))
							{
								newConn=conn;
							}
							break;
						case EMPTY:
							if(conn.useEmpty())
							{
								newConn=conn;
							}
							break;
						}
						if(newConn!=null)
							break;
					}
				}
				catch (final Exception e)
				{
				}
			}
			if((newConn!=null)&&(newConn.isProbablyDead()||newConn.isProbablyLockedUp()||(!newConn.ready())))
			{
				Log.errOut("Failed to connect to database.");
				try
				{
					newConn.close();
				}
				catch (final Exception e)
				{
				}
				newConn=null;
			}
			if(newConn==null)
			{
				if((consecutiveFailures++)>=50)
				{
					if(consecutiveFailures>50)
					{
						if(connections.size()==0)
							disconnected=true;
						else
							lockedUp=true;
						consecutiveFailures=0;
					}
				}
				if(connections.size()>=maxConnections)
				{
					int inuse=0;
					for(final DBConnection conn : connections)
					{
						if(conn.inUse())
							inuse++;
					}
					if(consecutiveFailures==180)
					{
						Log.errOut("Serious failure obtaining DBConnection ("+inuse+"/"+connections.size()+" in use).");
						for(final DBConnection conn : connections)
						{
							if(conn.inUse())
								Log.errOut("Last SQL was: "+conn.lastSQL);
						}
						if(inuse==0)
							resetConnections();
					}
					else
					if(consecutiveFailures==90)
						Log.errOut("Moderate failure obtaining DBConnection ("+inuse+"/"+connections.size()+" in use).");
					else
					if(consecutiveFailures==30)
						Log.errOut("Minor failure obtaining DBConnection("+inuse+"/"+connections.size()+" in use).");
					try
					{
						Thread.sleep(Math.round(Math.random()*500));
					}
					catch(final InterruptedException i)
					{
					}
				}
			}
			else
			{
				switch(type)
				{
				case PREPAREDSTATEMENT:
					newConn.usePrepared(SQL);
					break;
				case TESTSTATEMENT:
				case STATEMENT:
					newConn.use(SQL);
					break;
				case EMPTY:
					newConn.useEmpty();
					break;
				}
			}
		}

		consecutiveFailures=0;
		disconnected=false;
		lockedUp=false;
		if((isFakeDB==null)&&(newConn!=null))
		{
			isFakeDB=Boolean.valueOf(newConn.isFakeDB());
		}
		return newConn;
	}

	public boolean isFakeDB()
	{
		if(isFakeDB==null)
		{
			final DBConnection c = DBFetchAny("",DBConnection.FetchType.EMPTY);
			if(c!=null)
			{
				if(isFakeDB==null)
				{
					isFakeDB=Boolean.valueOf(c.isFakeDB());
				}
				c.doneUsing("");
			}
		}
		if(isFakeDB!=null)
		{
			return isFakeDB.booleanValue();
		}
		return false;
	}

	public DBConnection DBFetchPrepared(final String SQL)
	{
		return DBFetchAny(SQL,DBConnection.FetchType.PREPAREDSTATEMENT);
	}

	public DBConnection DBFetchEmpty()
	{
		return DBFetchAny("",DBConnection.FetchType.EMPTY);
	}

	/**
	 * Return a DBConnection object fetched with DBFetch()
	 *
	 * Usage:
	 * @param D    The Database connection to return to the pool
	 */
	public void DBDone(final DBConnection D)
	{
		if(D==null)
			return;
		D.doneUsing("");
		if(!D.ready())
			connections.remove(D);
	}

	/**
	 * When reading a database table, this routine will read in
	 * the given Field NAME, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 *
	 * Usage: str=getRes(R,"FIELD");
	 * @param Results    The ResultSet object to use
	 * @param Field 	   Field name to return
	 * @return String    The value of the field being returned
	 */
	public static String getRes(final ResultSet Results, final String Field)
	{
		try
		{
			final String TVal=Results.getString(Field);
			if(TVal==null)
				return "";
			return TVal.trim();
		}
		catch(final SQLException sqle)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
			{

				Log.errOut("getString: "+Field);
				Log.errOut(sqle);
			}
			else
				Log.errOut(""+sqle);
			return "";
		}
	}

	/**
	  * When reading a database table, this routine will read in
	 * the given Field NAME, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 *
	 * Usage: str=getLongRes(R,"FIELD");
	 * @param Results    The ResultSet object to use
	 * @param Field 	   Field name to return
	 * @return String    The value of the field being returned
	 */
	public static long getLongRes(final ResultSet Results, final String Field)
	{
		try
		{
			final String Val=Results.getString(Field);
			if(Val!=null)
			{
				if(Val.indexOf('.')>=0)
					return Math.round(Float.parseFloat(Val));
				return Long.parseLong(Val);
			}
			return 0;
		}
		catch(final SQLException sqle)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
			{

				Log.errOut("getLong: "+Field);
				Log.errOut(sqle);
			}
			else
				Log.errOut(""+sqle);
			return 0;
		}
		catch (final java.lang.NumberFormatException nfe)
		{
			return 0;
		}
	}

	/**
	 * When reading a database table, this routine will read in
	 * the given One index number, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 *
	 * Usage: str=getRes(R,1);
	 * @param Results    The ResultSet object to use
	 * @param One   	 Field number to return
	 * @return String    The value of the field being returned
	 */
	public static String getRes(final ResultSet Results, final int One)
	{
		try
		{
			final String TVal=Results.getString(One);
			if(TVal==null)
				return "";
			return TVal.trim();
		}
		catch(final SQLException sqle)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
			{

				Log.errOut("getRes: "+One);
				Log.errOut(sqle);
			}
			else
				Log.errOut(""+sqle);
			return "";
		}
	}

	public static String getResQuietly(final ResultSet Results, final String Field)
	{
		try
		{
			final String TVal=Results.getString(Field);
			if(TVal==null)
				return "";
			return TVal.trim();
		}
		catch(final SQLException sqle)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
			{

				Log.errOut("getStringQ: "+Field);
				Log.errOut(sqle);
			}
			return "";
		}
	}

	public boolean deregisterDriver()
	{
		try
		{
			shutdown=true;
			return true;
		}
		catch(final Exception ce)
		{
		}
		return false;
	}

	public void resetConnections()
	{
		if((System.currentTimeMillis()-lastReset)>20000)
		{
			killConnections();
			lastReset=System.currentTimeMillis();
		}
	}
	/**
	 * Destroy all database connections, effectively
	 * shutting down this class.
	 *
	 * Usage: killConnections();
	 */
	public void killConnections()
	{
		synchronized(connections)
		{
			for(final DBConnection conn : connections)
				conn.close();
			connections.clear();
		}
		try
		{
			final java.util.Properties p = new java.util.Properties();
			p.put("user",dbUser);
			p.put("password",dbPass);
			p.put("shutdown", "true");
			//DriverManager.getConnection(DBService,p);
		}
		catch(final Exception e)
		{
		}

	}

	/**
	 * Return the happiness level of the connections
	 * Usage: amIOk()
	 * @return boolean    true if ok, false if not ok
	 */
	public boolean amIOk()
	{
		return (!lockedUp)&&(!disconnected);
	}

	/**
	 * Queue up a failed write/update for later processing.
	 *
	 * Usage: enQueueError("UPDATE SQL","error string");
	 * @param SQLString    UPDATE style SQL statement
	 * @param SQLError    The error message being reported
	 * @param tries    The number of tries to redo it so far
	 */
	public void enQueueError(final String SQLString, final String SQLError, final String tries)
	{
		if(!errorQueingEnabled)
		{
			Log.errOut("Error Queueing not enabled.");
			return;
		}

		synchronized(CMClass.getSync("SQLErrors.que"))
		{
			PrintWriter out=null;
			try
			{
				out=new PrintWriter(new FileOutputStream("SQLErrors.que",true),true);
			}
			catch(final FileNotFoundException fnfe)
			{
				Log.errOut("Could not open queue?!?!");
				Log.errOut(SQLString+"\t"+SQLError);
			}

			if(out!=null)
			{
				out.println(SQLString+"\t!|!\t"+SQLError+"\t!|!\t"+tries);
				out.close();
			}

		}
	}

	/**
	 * Queue up a failed write/update for later processing.
	 *
	 * Usage: RetryQueuedErrors();
	 */
	public void retryQueuedErrors()
	{
		final Vector<String> Queue=new Vector<String>();

		if((lockedUp)||(disconnected))
		{
			Log.sysOut("Database is in trouble.  Retry skipped.");
			return;
		}

		if(!errorQueingEnabled)
		{
			Log.errOut("Error Queueing not enabled.");
			return;
		}

		synchronized(CMClass.getSync("SQLErrors.que"))
		{
			final File myFile=new File("SQLErrors.que");
			if(myFile.canRead())
			{
				// open a reader for the file
				BufferedReader in=null;
				try
				{
					in = new BufferedReader(new FileReader(myFile));
				}
				catch (final FileNotFoundException f)
				{
				}

				if(in!=null)
				{
					// read in the queue
					try
					{
						while(in.ready())
						{
							final String queueLine=in.readLine();
							if(queueLine==null)
								break;
							Queue.addElement(queueLine);
						}
					}
					catch (final IOException e)
					{
					}

					// close the channel.. done?
					try
					{
						  in.close();
					}
					catch (final IOException e)
					{
					}
				}
			}
			myFile.delete();
		}

		// did we actually READ anything?
		if(Queue.size()==0)
		{
			//Log.sysOut("DB Retry Queue is empty.  Good.");
		}
		else
		{
			int successes=0;
			int unsuccesses=0;
			while(Queue.size()>0)
			{
				final String queueLine=Queue.elementAt(0);
				Queue.removeElementAt(0);

				final int firstTab=queueLine.indexOf("\t!|!\t");
				int secondTab=-1;
				if(firstTab>0)
					secondTab=queueLine.indexOf("\t!|!\t",firstTab+5);
				if((firstTab>0)&&(secondTab>firstTab))
				{
					DBConnection dbConnection=null;
					final String retrySQL=queueLine.substring(0,firstTab);
					final int oldAttempts=CMath.s_int(queueLine.substring(secondTab+5));
					if(oldAttempts>20)
					{
						Log.errOut("Giving up on :"+retrySQL);
						Log.errOut("Giving up because: "+queueLine.substring(firstTab+5,secondTab));
					}
					else
					{
						try
						{
							dbConnection=DBFetch();
							try
							{
								dbConnection.update(retrySQL,oldAttempts);
								successes++;
								if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
									Log.sysOut("Successful retry: "+queueLine);
							}
							catch(final SQLException sqle)
							{
								unsuccesses++;
								if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
									Log.errOut("Unsuccessfull retry: "+queueLine);
								//DO NOT DO THIS AGAIN -- the UPDATE WILL GENERATE the ENQUE!!!
								//enQueueError(retrySQL,""+sqle,oldDate);
							}
						}
						catch(final Exception e)
						{
							unsuccesses++;
							enQueueError(retrySQL,e.getMessage(),""+(oldAttempts+1));
						}
						if(dbConnection!=null)
						{
							dbConnection.clearFailures();
							DBDone(dbConnection);
							CMLib.s_sleep(1000);
						}
					}
				}
				else
					Log.errOut("Could not retry line: "+queueLine+"/"+firstTab+"/"+secondTab);
			}
			clearErrors();
			if(unsuccesses>successes)
				Log.errOut("Finished running retry Que. Successes: "+successes+"/Failures: "+unsuccesses);
			else
				Log.sysOut("Finished running retry Que. Successes: "+successes+"/Failures: "+unsuccesses);
		}
	}

	/**
	 *
	 * Usage: update("UPDATE...");
	 * @param queryString    the update SQL command
	 * @return int    the responseCode, or -1
	 */
	public int queryRows(final String queryString)
	{
		DBConnection dbConnection=null;
		int result=0;
		try
		{
			dbConnection=DBFetch();
			try
			{
				final ResultSet R=dbConnection.query(queryString);
				if(R==null)
					result=0;
				else
				while(R.next())
					result++;
			}
			catch(final Exception sqle)
			{
				if(sqle instanceof java.io.EOFException)
				{
					Log.errOut(""+sqle);
					DBDone(dbConnection);
					return -1;
				}
				if(sqle instanceof SQLException)
				{
					// queued by the connection for retry
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut(""+e);
		}
		if(dbConnection!=null)
			DBDone(dbConnection);
		return result;
	}

	/** list the connections
	 *
	 * Usage: listConnections(out);
	 * @param out    place to send the list out to
	 * @param stackDump also include stack traces on active connections
	 */
	public void listConnections(final PrintStream out, final boolean stackDump)
	{
		out.println("\nDatabase connections:");
		if((lockedUp)||(disconnected))
			out.println("** Database is reporting a down status! **");

		int p=1;
		for(final DBConnection conn : connections)
		{
			String OKString="OK";
			if((conn.isProbablyDead())&&(conn.isProbablyLockedUp()))
				OKString="Completely dead"+(conn.inSQLServerCommunication()?" (SERVER COMM)":"");
			else
			if(conn.isProbablyDead())
				OKString="Dead"+(conn.inSQLServerCommunication()?" (SERVER COMM)":"");
			else
			if(conn.isProbablyLockedUp())
				OKString="Locked up"+(conn.inSQLServerCommunication()?" (SERVER COMM)":"");
			out.println(Integer.toString(p)
						+". Connected="+conn.ready()
						+", In use="+conn.inUse()
						+", Status="+OKString+"."
						);
			if(stackDump && conn.isThreadAlive())
			{
				final java.lang.StackTraceElement[] s=conn.getStackTrace();
				for (final StackTraceElement element : s)
					out.println("\n   "+element.getClassName()+": "+element.getMethodName()+"("+element.getFileName()+": "+element.getLineNumber()+")");
			}
			p++;
		}
		out.println("\n");
	}

	public void reportError()
	{
		consecutiveErrors++;
		final double size=connections.size();
		final double down=consecutiveErrors;
		if((down/size)>.25)
		{
			disconnected=true;
			consecutiveErrors=0;
		}
	}

	public void clearErrors()
	{
		consecutiveErrors=0;
		disconnected=false;
	}

	/** return a status string, or "" if everything is ok.
	 *
	 * Usage: errorStatus();
	 * @return StringBuffer    complete error status
	 */
	public StringBuffer errorStatus()
	{
		final StringBuffer status=new StringBuffer("");
		if(lockedUp)
			status.append("#100 DBCONNECTIONS REPORTING A LOCKED STATE\n");
		if(disconnected)
			status.append("#101 DBCONNECTIONS REPORTING A DISCONNECTED STATE\n");
		if((lockedUp)||(disconnected))
		{
			for(final DBConnection conn : connections)
				DBDone(conn);
		}
		return status;
	}
}
