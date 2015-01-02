package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2001-2015 Bo Zimmerman

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
	protected String dbClass="";
	/** the odbc service*/
	protected String dbService="";
	/** the odbc login user */
	protected String dbUser="";
	/** the odbc password */
	protected String dbPass="";
	/** number of connections to make*/
	protected int maxConnections=0;
	/** the disconnected flag */
	protected boolean disconnected=false;
	/** the im in trouble flag*/
	protected boolean lockedUp=false;
	/** the number of times the system has failed to get a db*/
	protected int consecutiveFailures=0;
	/** the number of times the system has failed a request */
	protected int consecutiveErrors=0;
	/** Object to synchronize around on error handling*/
	protected boolean errorQueingEnabled=false;
	/** the database connnections */
	protected SVector<DBConnection> connections;
	/** set this to true once, cuz it makes it all go away. **/
	protected boolean shutdown=false;
	/** whether to reuse connections */
	protected boolean reuse=false;
	/** last time resetconnections called (or resetconnections) */
	private long lastReset=0;
	/** check for whether these connections are fakedb */
	private Boolean isFakeDB = null;

	/**
	 * Initialize this class.  Must be called at first,
	 * and after any killConnections() calls.
	 *
	 * <br><br><b>Usage:</b> Initialize("ODBCSERVICE","USER","PASSWORD",10);
	 * @param NEWDBClass	the odbc service
	 * @param NEWDBService    the odbc service
	 * @param NEWDBUser    the odbc user login
	 * @param NEWDBPass    the odbc user password
	 * @param NEWnumConnections    Connections to maintain
	 * @param NEWreuse    Whether to reuse connections
	 * @param DoErrorQueueing    whether to save errors to a file
	 */
	public DBConnections(String NEWDBClass,
						 String NEWDBService,
						 String NEWDBUser,
						 String NEWDBPass,
						 int NEWnumConnections,
						 boolean NEWreuse,
						 boolean DoErrorQueueing)
	{
		dbClass=NEWDBClass;
		dbService=NEWDBService;
		dbUser=NEWDBUser;
		dbPass=NEWDBPass;
		reuse=NEWreuse;
		maxConnections=NEWnumConnections;
		connections = new SVector<DBConnection>();
		errorQueingEnabled=DoErrorQueueing;
	}

	/**
	 * <br><br><b>Usage: update("UPDATE...");</b>
	 * @param updateStrings    the update SQL commands
	 * @return int    the responseCode, or -1
	 */
	public int update(String[] updateStrings)
	{
		DBConnection DBToUse=null;
		int Result=-1;
		String updateString=null;
		try
		{
			DBToUse=DBFetch();
			for (final String updateString2 : updateStrings)
			{
				updateString=updateString2;
				try
				{
					Result=DBToUse.update(updateString,0);
				}
				catch(final Exception sqle)
				{
					if(sqle instanceof java.io.EOFException)
					{
						Log.errOut("DBConnections",""+sqle);
						DBDone(DBToUse);
						return -1;
					}
					if(sqle instanceof SQLException)
					{
						// queued by the connection for retry
					}
				}
				if(Result<0)
				{
					Log.errOut("DBConnections",""+DBToUse.getLastError()+"/"+updateString);
				}
			}
		}
		catch(final Exception e)
		{
			enQueueError(updateString,""+e,""+0);
			reportError();
			Log.errOut("DBConnections",""+e);
		}
		finally
		{
			if(DBToUse!=null)
				DBDone(DBToUse);
		}
		return Result;
	}

	/**
	 * <br><br><b>Usage: updateWithClobs("UPDATE...");</b>
	 * @param entries    the update SQL commands
	 * @return int    the responseCode, or -1
	 */
	public int updateWithClobs(final List<DBPreparedBatchEntry> entries)
	{
		DBConnection DBToUse=null;
		int Result=-1;
		try
		{
			DBToUse=DBFetchEmpty();
			for(final DBPreparedBatchEntry entry : entries)
			{
				try
				{
					if((entry.clobs==null)||(entry.clobs.length==0))
					{
						DBToUse.closeStatements("");
						Result=DBToUse.update(entry.sql,0);
					}
					else
					{
						DBToUse.rePrepare(entry.sql);
						for (final String[] clob : entry.clobs)
						{
							DBToUse.setPreparedClobs(clob);
							Result=DBToUse.update("",0);
						}
					}
				}
				catch(final Exception sqle)
				{
					if(sqle instanceof java.io.EOFException)
					{
						Log.errOut("DBConnections",""+sqle);
						DBDone(DBToUse);
						return -1;
					}
					if(sqle instanceof SQLException)
					{
						// queued by the connection for retry
					}
				}
				if(Result<0)
				{
					Log.errOut("DBConnections",""+DBToUse.getLastError()+"/"+entry.sql);
				}
			}
		}
		catch(final Exception e)
		{
			reportError();
			Log.errOut("DBConnections",""+e);
		}
		finally
		{
			if(DBToUse!=null)
				DBDone(DBToUse);
		}
		return Result;
	}

	/**
	 * <br><br><b>Usage: updateWithClobs("UPDATE...");</b>
	 * @param updateStrings    the update SQL commands
	 * @param values	the update SQL command values
	 * @return int    the responseCode, or -1
	 */
	public int updateWithClobs(String[] updateStrings, String[][][] values)
	{
		final LinkedList<DBPreparedBatchEntry> entries = new LinkedList<DBPreparedBatchEntry>();
		for(int i=0;i<updateStrings.length;i++)
			entries.add(new DBPreparedBatchEntry(updateStrings[i],values[i]));
		return updateWithClobs(entries);
	}

	/**
	 * <br><br><b>Usage: updateWithClobs("UPDATE...");</b>
	 * @param updateString    the update SQL commands
	 * @param values	the update SQL values
	 * @return int    the responseCode, or -1
	 */
	public int updateWithClobs(String updateString, String[][] values)
	{
		return updateWithClobs(Arrays.asList(new DBPreparedBatchEntry(updateString,values)));
	}

	/**
	 * Return the number of connections made.
	 *
	 * <br><br><b>Usage: n=numConnectionsMade();</b>
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
			if(conn.inUse())
				num++;
		return num;
	}

	public int numAvailable()
	{
		int num=0;
		for(final DBConnection conn : connections)
			if(!conn.inUse())
				num++;
		return num;
	}

	/**
	 * Pings all connections not currently in use.
	 * @param querySql the query to use as a ping
	 */
	public int pingAllConnections(final String querySql, final long usageTimeoutMillis)
	{
		final LinkedList<DBConnection> fetched = new LinkedList<DBConnection>();
		int numPinged=0;
		while(numAvailable()>0)
		{
			final DBConnection DB=DBFetch();
			if(DB!=null)
			{
				fetched.add(DB);
				if((System.currentTimeMillis()-DB.getLastQueryTime())> usageTimeoutMillis)
				{
					try
					{
						DB.query(querySql);
						numPinged++;
					}
					catch (final SQLException e)
					{
						Log.errOut("DBConnections",e.getMessage());
					}
				}
			}
		}
		for(final DBConnection DB : fetched)
			DBDone(DB);
		return numPinged;
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * <br><br><b>Usage: DB=DBFetch();</b>
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetch()
	{
		return DBFetchAny("",DBConnection.FetchType.STATEMENT);
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * <br><br><b>Usage: DB=DBFetchPrepared();</b>
	 * @param SQL    The prepared statement SQL
	 * @param type    the type of fetching to do
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchAny(final String SQL, final DBConnection.FetchType type)
	{
		DBConnection ThisDB=null;
		while(ThisDB==null)
		{
			if(shutdown)
			{
				// can't throw without declaring, so this is the only way.
				final int x=1;
				final int y=x-1;
				System.out.println(x/y);
				// this should create a division by zero error.
			}

			if(connections.size()<maxConnections)
				try
				{
					ThisDB=new DBConnection(this,dbClass,dbService,dbUser,dbPass,reuse);
					connections.addElement(ThisDB);
				}catch(final Exception e)
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
					ThisDB=null;
					if(connections.size()==0)
					{
						disconnected=true;
						return null;
					}
				}
			if((ThisDB==null)&&(reuse))
			{
				try
				{
					for(final DBConnection conn : connections)
					{
						if(type==DBConnection.FetchType.PREPAREDSTATEMENT)
						{
							if( conn.usePrepared(SQL))
							{
								ThisDB=conn;
								break;
							}
						}
						else
						if(type==DBConnection.FetchType.STATEMENT)
						{
							if(conn.use(""))
							{
								ThisDB=conn;
								break;
							}
						}
						else
						{
							if(conn.useEmpty())
							{
								ThisDB=conn;
								break;
							}
						}
					}
				}catch(final Exception e){}
			}
			if((ThisDB!=null)&&(ThisDB.isProbablyDead()||ThisDB.isProbablyLockedUp()||(!ThisDB.ready())))
			{
				Log.errOut("DBConnections","Failed to connect to database.");
				try{ThisDB.close();}catch(final Exception e){}
				ThisDB=null;
			}
			if(ThisDB==null)
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
						if(conn.inUse())
							inuse++;
					if(consecutiveFailures==180)
					{
						Log.errOut("DBConnections","Serious failure obtaining DBConnection ("+inuse+"/"+connections.size()+" in use).");
						for(final DBConnection conn : connections)
							if(conn.inUse())
								Log.errOut("DBConnections","Last SQL was: "+conn.lastSQL);
						if(inuse==0)
							resetConnections();
					}
					else
					if(consecutiveFailures==90)
						Log.errOut("DBConnections","Moderate failure obtaining DBConnection ("+inuse+"/"+connections.size()+" in use).");
					else
					if(consecutiveFailures==30)
						Log.errOut("DBConnections","Minor failure obtaining DBConnection("+inuse+"/"+connections.size()+" in use).");
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
				if(type==DBConnection.FetchType.PREPAREDSTATEMENT)
					ThisDB.usePrepared(SQL);
				else
				if(type==DBConnection.FetchType.STATEMENT)
					ThisDB.use(SQL);
				else
					ThisDB.useEmpty();
			}
		}


		consecutiveFailures=0;
		disconnected=false;
		lockedUp=false;
		if(isFakeDB==null)
		{
			isFakeDB=Boolean.valueOf(ThisDB.isFakeDB());
		}
		return ThisDB;
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

	public DBConnection DBFetchPrepared(String SQL)
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
	 * <br><br><b>Usage:</b>
	 * @param D    The Database connection to return to the pool
	 */
	public void DBDone(DBConnection D)
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
	 * <br><br><b>Usage:</b> str=getRes(R,"FIELD");
	 * @param Results    The ResultSet object to use
	 * @param Field 	   Field name to return
	 * @return String    The value of the field being returned
	 */
	public static String getRes(ResultSet Results, String Field)
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
			Log.errOut("DBConnections",""+sqle);
			return "";
		}
	}

	/**
	  * When reading a database table, this routine will read in
	 * the given Field NAME, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 *
	 * <br><br><b>Usage:</b> str=getLongRes(R,"FIELD");
	 * @param Results    The ResultSet object to use
	 * @param Field 	   Field name to return
	 * @return String    The value of the field being returned
	 */
	public static long getLongRes(ResultSet Results, String Field)
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
			Log.errOut("DBConnections",""+sqle);
			return 0;
		}
		catch(final java.lang.NumberFormatException nfe){ return 0;}
	}

	/**
	 * When reading a database table, this routine will read in
	 * the given One index number, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 *
	 * <br><br><b>Usage:</b> str=getRes(R,1);
	 * @param Results    The ResultSet object to use
	 * @param One   	 Field number to return
	 * @return String    The value of the field being returned
	 */
	public static String getRes(ResultSet Results, int One)
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
			Log.errOut("DBConnections",""+sqle);
			return "";
		}
	}

	public static String getResQuietly(ResultSet Results, String Field)
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
	 * <br><br><b>Usage:</b> killConnections();
	 */
	public void killConnections()
	{
		synchronized(connections)
		{
			for(final DBConnection conn : connections)
				conn.close();
			connections.removeAllElements();
		}
		try
		{
			final java.util.Properties p = new java.util.Properties();
			p.put("user",dbUser);
			p.put("password",dbPass);
			p.put("shutdown", "true");
			//DriverManager.getConnection(DBService,p);
		}
		catch(final Exception e){}

	}

	/**
	 * Return the happiness level of the connections
	 * <br><br><b>Usage:</b> amIOk()
	 * @return boolean    true if ok, false if not ok
	 */
	public boolean amIOk()
	{
		return (!lockedUp)&&(!disconnected);
	}

	/**
	 * Queue up a failed write/update for later processing.
	 *
	 * <br><br><b>Usage:</b> enQueueError("UPDATE SQL","error string");
	 * @param SQLString    UPDATE style SQL statement
	 * @param SQLError    The error message being reported
	 * @param tries    The number of tries to redo it so far
	 */
	public void enQueueError(String SQLString, String SQLError, String tries)
	{
		if(!errorQueingEnabled)
		{
			Log.errOut("DBConnections","Error Queueing not enabled.");
			return;
		}

		synchronized("SQLErrors.que")
		{
			PrintWriter out=null;
			try
			{
				out=new PrintWriter(new FileOutputStream("SQLErrors.que",true),true);
			}
			catch(final FileNotFoundException fnfe)
			{
				Log.errOut("DBConnections","Could not open queue?!?!");
				Log.errOut("DBConnections",SQLString+"\t"+SQLError);
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
	 * <br><br><b>Usage:</b> RetryQueuedErrors();
	 */
	public void retryQueuedErrors()
	{
		final Vector<String> Queue=new Vector<String>();

		if((lockedUp)||(disconnected))
		{
			Log.sysOut("DBConnections","Database is in trouble.  Retry skipped.");
			return;
		}

		if(!errorQueingEnabled)
		{
			Log.errOut("DBConnections","Error Queueing not enabled.");
			return;
		}

		synchronized("SQLErrors.que")
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
				catch(final FileNotFoundException f){}

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
					catch(final IOException e){}

					// close the channel.. done?
					try
					{
						  in.close();
					}
					catch(final IOException e){}
				}
			}
			myFile.delete();
		}

		// did we actually READ anything?
		if(Queue.size()==0)
		{
			//Log.sysOut("DBConnections","DB Retry Queue is empty.  Good.");
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
					DBConnection DB=null;
					final String retrySQL=queueLine.substring(0,firstTab);
					final int oldAttempts=CMath.s_int(queueLine.substring(secondTab+5));
					if(oldAttempts>20)
					{
						Log.errOut("DBConnections","Giving up on :"+retrySQL);
						Log.errOut("DBConnections","Giving up because: "+queueLine.substring(firstTab+5,secondTab));
					}
					else
					{
						try
						{
							DB=DBFetch();
							try
							{
								DB.update(retrySQL,oldAttempts);
								successes++;
								//Log.sysOut("DBConnections","Successful retry: "+queueLine);
							}
							catch(final SQLException sqle)
							{
								unsuccesses++;
								//Log.errOut("DBConnections","Unsuccessfull retry: "+queueLine);
								//DO NOT DO THIS AGAIN -- the UPDATE WILL GENERATE the ENQUE!!!
								//enQueueError(retrySQL,""+sqle,oldDate);
							}
						}
						catch(final Exception e)
						{
							unsuccesses++;
							enQueueError(retrySQL,e.getMessage(),""+(oldAttempts+1));
						}
						if(DB!=null)
						{
							DB.clearFailures();
							DBDone(DB);
							try{Thread.sleep(1000);}catch(final Exception e){}
						}
					}
				}
				else
					Log.errOut("DBConnections","Could not retry line: "+queueLine+"/"+firstTab+"/"+secondTab);
			}
			clearErrors();
			if(unsuccesses>successes)
				Log.errOut("DBConnections","Finished running retry Que. Successes: "+successes+"/Failures: "+unsuccesses);
			else
				Log.sysOut("DBConnections","Finished running retry Que. Successes: "+successes+"/Failures: "+unsuccesses);
		}
	}

	/**
	 *
	 * <br><br><b>Usage: update("UPDATE...");</b>
	 * @param queryString    the update SQL command
	 * @return int    the responseCode, or -1
	 */
	public int queryRows(String queryString)
	{
		DBConnection DBToUse=null;
		int Result=0;
		try
		{
			DBToUse=DBFetch();
			try
			{
				final ResultSet R=DBToUse.query(queryString);
				if(R==null)
					Result=0;
				else
				while(R.next())
					Result++;
			}
			catch(final Exception sqle)
			{
				if(sqle instanceof java.io.EOFException)
				{
					Log.errOut("DBConnections",""+sqle);
					DBDone(DBToUse);
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
			Log.errOut("DBConnections",""+e);
		}
		if(DBToUse!=null)
			DBDone(DBToUse);
		return Result;
	}


	/** list the connections
	 *
	 * <br><br><b>Usage:</b> listConnections(out);
	 * @param out    place to send the list out to
	 */
	public void listConnections(PrintStream out)
	{
		out.println("\nDatabase connections:");
		if((lockedUp)||(disconnected))
			out.println("** Database is reporting a down status! **");

		int p=1;
		for(final DBConnection conn : connections)
		{
			String OKString="OK";
			if((conn.isProbablyDead())&&(conn.isProbablyLockedUp()))
				OKString="Completely dead"+(conn.inSQLServerCommunication()?" (SERVER COMM)":"")+".";
			else
			if(conn.isProbablyDead())
				OKString="Dead"+(conn.inSQLServerCommunication()?" (SERVER COMM)":"")+".";
			else
			if(conn.isProbablyLockedUp())
				OKString="Locked up"+(conn.inSQLServerCommunication()?" (SERVER COMM)":"")+".";
			out.println(Integer.toString(p)
						+". Connected="+conn.ready()
						+", In use="+conn.inUse()
						+", Status="+OKString
						);
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
	 * <br><br><b>Usage:</b> errorStatus();
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
			for(final DBConnection conn : connections)
				DBDone(conn);
		return status;
	}
}
