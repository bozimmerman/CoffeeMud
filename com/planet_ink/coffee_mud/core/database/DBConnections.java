package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class DBConnections
{
	protected String DBClass="";
	/** the odbc service*/
	protected String DBService="";
	/** the odbc login user */
	protected String DBUser="";
	/** the odbc password */
	protected String DBPass="";
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
	protected Vector Connections;
	/** set this to true once, cuz it makes it all go away. **/
	protected boolean YOU_ARE_DONE=false;
	/** whether to reuse connections */
	protected boolean reuse=false;
	/** last time resetconnections called (or resetconnections) */
	private long lastReset=0;


	/**
	 * Initialize this class.  Must be called at first,
	 * and after any killConnections() calls.
	 *
	 * <br><br><b>Usage:</b> Initialize("ODBCSERVICE","USER","PASSWORD",10);
	 * @param NEWDBClass	the odbc service
	 * @param NEWDBService	the odbc service
	 * @param NEWDBUser	the odbc user login
	 * @param NEWDBPass	the odbc user password
	 * @param NEWnumConnections	Connections to maintain
	 * @param NEWreuse	Whether to reuse connections
	 * @param DoErrorQueueing	whether to save errors to a file
	 */
	public DBConnections(String NEWDBClass,
						 String NEWDBService,
						 String NEWDBUser,
						 String NEWDBPass,
						 int NEWnumConnections,
						 boolean NEWreuse,
						 boolean DoErrorQueueing)
	{
		DBClass=NEWDBClass;
		DBService=NEWDBService;
		DBUser=NEWDBUser;
		DBPass=NEWDBPass;
		reuse=NEWreuse;
		maxConnections=NEWnumConnections;
		Connections = new Vector();
		errorQueingEnabled=DoErrorQueueing;
	}

	/**
	 *
	 * <br><br><b>Usage: update("UPDATE...");</b>
	 * @param updateString	the update SQL command
	 * @return int	the responseCode, or -1
	 */
	public int update(String[] updateStrings)
	{
		DBConnection DBToUse=null;
		int Result=-1;
		String updateString=null;
		try
		{
			DBToUse=DBFetch();
			for(int i=0;i<updateStrings.length;i++)
			{
				updateString=updateStrings[i];
				try
				{
					Result=DBToUse.update(updateString,0);
				}
				catch(Exception sqle)
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
		catch(Exception e)
		{
			enQueueError(updateString,""+e,""+0);
			reportError();
			Log.errOut("DBConnections",""+e);
		}
		if(DBToUse!=null)
			DBDone(DBToUse);
		return Result;
	}

	/**
	 * Return the number of connections made.
	 *
	 * <br><br><b>Usage: n=numConnectionsMade();</b>
	 * @return numConnectionsMade	The number of connections
	 */
	public int numConnectionsMade()
	{
		return Connections.size();
	}

	public int numInUse()
	{
		int num=0;
		for(int i=0;i<Connections.size();i++)
			if(((DBConnection)Connections.elementAt(i)).inUse())
				num++;
		return num;
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * <br><br><b>Usage: DB=DBFetch();</b>
	 * @return DBConnection	The DBConnection to use
	 */
	public DBConnection DBFetch()
	{
		return DBFetchAny("",false);
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * <br><br><b>Usage: DB=DBFetchPrepared();</b>
	 * @param SQL	The prepared statement SQL
	 * @param prepared	whether the statement should be a prepared one
	 * @return DBConnection	The DBConnection to use
	 */
	public DBConnection DBFetchAny(String SQL, boolean prepared)
	{
		DBConnection ThisDB=null;
		while(ThisDB==null)
		{
			if(YOU_ARE_DONE)
			{
				// can't throw without declaring, so this is the only way.
				int x=1;
				int y=x-1;
				System.out.println(x/y);
				// this should create a division by zero error.
			}

			if(Connections.size()<maxConnections)
				try{
					ThisDB=new DBConnection(this,DBClass,DBService,DBUser,DBPass,reuse);
					Connections.addElement(ThisDB);
				}catch(Exception e){
					if((e.getMessage()==null)||(e.getMessage().indexOf("java.io.EOFException")<0))
						Log.errOut("DBConnections",e);
					ThisDB=null;
				}
			if((ThisDB==null)&&(reuse))
			{
				try{
					for(int i=0;i<Connections.size();i++)
						if(((DBConnection)Connections.elementAt(i)).use(""))
						{
							ThisDB=((DBConnection)Connections.elementAt(i));
							break;
						}
				}catch(Exception e){}
			}
			if((ThisDB!=null)&&(ThisDB.isProbablyDead()||ThisDB.isProbablyLockedUp()||(!ThisDB.ready())))
			{
				Log.errOut("DBConnections","Failed to connect to database.");
				try{ThisDB.close();}catch(Exception e){}
				ThisDB=null;
			}
			if(ThisDB==null)
			{
				if((consecutiveFailures++)>=50)
				{
					if(consecutiveFailures>50)
					{
						if(Connections.size()==0)
							disconnected=true;
						else
							lockedUp=true;
						consecutiveFailures=0;
					}
				}
				if(Connections.size()>=maxConnections)
				{
					int inuse=0;
					for(int i=0;i<Connections.size();i++)
						if(((DBConnection)Connections.elementAt(i)).inUse())
							inuse++;
					if(consecutiveFailures==180)
					{
						Log.errOut("DBConnections","Serious failure obtaining DBConnection ("+inuse+"/"+Connections.size()+" in use).");
						if(inuse==0)
							resetConnections();
					}
					else
					if(consecutiveFailures==90)
						Log.errOut("DBConnections","Moderate failure obtaining DBConnection ("+inuse+"/"+Connections.size()+" in use).");
					else
					if(consecutiveFailures==30)
						Log.errOut("DBConnections","Minor failure obtaining DBConnection("+inuse+"/"+Connections.size()+" in use).");
					try
					{
						Thread.sleep(Math.round(Math.random()*500));
					}
					catch(InterruptedException i)
					{
					}
				}
			}
			else
			{
				ThisDB.use(SQL);
			}
		}


		consecutiveFailures=0;
		disconnected=false;
		lockedUp=false;

		return ThisDB;
	}

	public DBConnection DBFetchPrepared(String SQL)
	{
		return DBFetchAny(SQL,true);
	}

	/**
	 * Return a DBConnection object fetched with DBFetch()
	 *
	 * <br><br><b>Usage:</b>
	 * @param D	The Database connection to return to the pool
	 */
	public void DBDone(DBConnection D)
	{
		if(D==null) return;
		D.doneUsing("");
		if(!D.ready())
		{
			synchronized(Connections)
			{
				Connections.remove(D);
			}
		}
	}


	/**
	 * When reading a database table, this routine will read in
	 * the given Field NAME, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 *
	 * <br><br><b>Usage:</b> str=getRes(R,"FIELD");
	 * @param Results	The ResultSet object to use
	 * @param Field		Field name to return
	 * @return String	The value of the field being returned
	 */
	public static String getRes(ResultSet Results, String Field)
	{
		try
		{
			String TVal=Results.getString(Field);
			if(TVal==null)
				return "";
			return TVal.trim();
		}
		catch(SQLException sqle)
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
	 * @param Results	The ResultSet object to use
	 * @param Field		Field name to return
	 * @return String	The value of the field being returned
	 */
	public static long getLongRes(ResultSet Results, String Field)
	{
		try
		{
			String Val=Results.getString(Field);
			if(Val!=null)
			{
				if(Val.indexOf(".")>=0)
					return Math.round(Float.parseFloat(Val));
				return Long.parseLong(Val);
			}
			return 0;
		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",""+sqle);
			return 0;
		}
		catch(java.lang.NumberFormatException nfe){ return 0;}
	}

	/**
	 * When reading a database table, this routine will read in
	 * the given One index number, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 *
	 * <br><br><b>Usage:</b> str=getRes(R,1);
	 * @param Results	The ResultSet object to use
	 * @param One		Field number to return
	 * @return String	The value of the field being returned
	 */
	public static String getRes(ResultSet Results, int One)
	{
		try
		{
			String TVal=Results.getString(One);
			if(TVal==null)
				return "";
			return TVal.trim();
		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",""+sqle);
			return "";
		}
	}

	public static String getResQuietly(ResultSet Results, String Field)
	{
		try
		{
			String TVal=Results.getString(Field);
			if(TVal==null)
				return "";
			return TVal.trim();
		}
		catch(SQLException sqle)
		{
			return "";
		}
	}

	public boolean deregisterDriver()
	{
		try
		{
			YOU_ARE_DONE=true;
			return true;
		}
		catch(Exception ce)
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
		synchronized(Connections)
		{
			while(Connections.size()>0)
			{
				DBConnection DB=(DBConnection)Connections.elementAt(0);
				Connections.removeElement(DB);
				DB.close();
			}
		}
		try
		{
	        java.util.Properties p = new java.util.Properties();
	        p.put("user",DBUser);
	        p.put("password",DBPass);
	        p.put("shutdown", "true");
	        //DriverManager.getConnection(DBService,p);
		}
		catch(Exception e){}

	}

	/**
	 * Return the happiness level of the connections
	 * <br><br><b>Usage:</b> amIOk()
	 * @return boolean	true if ok, false if not ok
	 */
	public boolean amIOk()
	{
		return (!lockedUp)&&(!disconnected);
	}

	/**
	 * Queue up a failed write/update for later processing.
	 *
	 * <br><br><b>Usage:</b> enQueueError("UPDATE SQL","error string");
	 * @param SQLString	UPDATE style SQL statement
	 * @param SQLError	The error message being reported
	 * @param tries	The number of tries to redo it so far
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
			catch(FileNotFoundException fnfe)
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
		Vector Queue=new Vector();

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
			File myFile=new File("SQLErrors.que");
			if(myFile.canRead())
			{
				// open a reader for the file
				BufferedReader in=null;
				try
				{
					in = new BufferedReader(new FileReader(myFile));
				}
				catch(FileNotFoundException f){}

				if(in!=null)
				{
					// read in the queue
					try
					{
						while(in.ready())
						{
							String queueLine=in.readLine();
							if(queueLine==null)
								break;
							Queue.addElement(queueLine);
						}
					}
					catch(IOException e){}

					// close the channel.. done?
					try
					{
						  in.close();
					}
					catch(IOException e){}
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
				String queueLine=(String)Queue.elementAt(0);
				Queue.removeElementAt(0);

				int firstTab=queueLine.indexOf("\t!|!\t");
				int secondTab=-1;
				if(firstTab>0)
					secondTab=queueLine.indexOf("\t!|!\t",firstTab+5);
				if((firstTab>0)&&(secondTab>firstTab))
				{
					DBConnection DB=null;
					String retrySQL=queueLine.substring(0,firstTab);
					int oldAttempts=CMath.s_int(queueLine.substring(secondTab+5));
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
							catch(SQLException sqle)
							{
								unsuccesses++;
								//Log.errOut("DBConnections","Unsuccessfull retry: "+queueLine);
								//DO NOT DO THIS AGAIN -- the UPDATE WILL GENERATE the ENQUE!!!
								//enQueueError(retrySQL,""+sqle,oldDate);
							}
						}
						catch(Exception e)
						{
							unsuccesses++;
							enQueueError(retrySQL,e.getMessage(),""+(oldAttempts+1));
						}
						if(DB!=null)
						{
							DB.clearFailures();
							DBDone(DB);
							try{Thread.sleep(1000);}catch(Exception e){}
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
	 * @param queryString	the update SQL command
	 * @return int	the responseCode, or -1
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
				ResultSet R=DBToUse.query(queryString);
				if(R==null)
					Result=0;
				else
				while(R.next())
					Result++;
			}
			catch(Exception sqle)
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
		catch(Exception e)
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
	 * @param out	place to send the list out to
	 */
	public void listConnections(PrintStream out)
	{
		out.println("\nDatabase connections:");
		if((lockedUp)||(disconnected))
			out.println("** Database is reporting a down status! **");

		for(int p=0;p<Connections.size();p++)
		{
			DBConnection DB=(DBConnection)Connections.elementAt(p);
			String OKString="OK";
			if((DB.isProbablyDead())&&(DB.isProbablyLockedUp()))
				OKString="Completely dead"+(DB.inSQLServerCommunication()?" (SERVER COMM)":"")+".";
			else
			if(DB.isProbablyDead())
				OKString="Dead"+(DB.inSQLServerCommunication()?" (SERVER COMM)":"")+".";
			else
			if(DB.isProbablyLockedUp())
				OKString="Locked up"+(DB.inSQLServerCommunication()?" (SERVER COMM)":"")+".";
			out.println(Integer.toString(p+1)
						+". Connected="+DB.ready()
						+", In use="+DB.inUse()
						+", Status="+OKString
						);
		}
		out.println("\n");
	}

	public void reportError()
	{
		consecutiveErrors++;
		double size=(double)Connections.size();
		double down=(double)consecutiveErrors;
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
	 * @return StringBuffer	complete error status
	 */
	public StringBuffer errorStatus()
	{
		StringBuffer status=new StringBuffer("");
		if(lockedUp)
			status.append("#100 DBCONNECTIONS REPORTING A LOCKED STATE\n");
		if(disconnected)
			status.append("#101 DBCONNECTIONS REPORTING A DISCONNECTED STATE\n");
		if((lockedUp)||(disconnected))
		for(int c=0;c<Connections.size();c++)
		{
			DBConnection DBConnect=(DBConnection)Connections.elementAt(c);
			DBDone(DBConnect);
		}
		return status;
	}
}
