package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

/**
* DBConnections.java 
*****************************************************
* Copyright 2000 WebIQ, Inc.
* Ramzi Sheikh
* 02-11-2000
*****************************************************
* Manages the numerous database connections, and their usage
* <br>
* Security Considerations:  NA
* <br>
* Performance Considerations:  NA
*/
public class DBConnections
{
	private String DBClass="";
	/** the odbc service*/
	private String DBService="";
	/** the odbc login user */
	private String DBUser="";
	/** the odbc password */
	private String DBPass="";
	/** number of connections to make*/
	private int numConnections=0;
	/** the disconnected flag */
	private boolean disconnected=false;
	/** the im in trouble flag*/
	private boolean lockedUp=false;
	/** the number of times the system has failed to get a db*/
	private int consecutiveFailures=0;
	/** the number of times the system has failed a request */
	private int consecutiveErrors=0;
	/** Object to synchronize around on error handling*/
	private Boolean fileSemaphore=new Boolean(true);
	/** Object to synchronize around on error handling*/
	private boolean errorQueingEnabled=false;
	/** the database connnections */
	private Vector Connections;
	/** last time queued errors were tried */
	private IQCalendar lastTriedQueued=IQCalendar.getIQInstance();
	/** set this to true once, cuz it makes it all go away. **/
	private boolean YOU_ARE_DONE=false;
	
	/** 
	 * Initialize this class.  Must be called at first,
	 * and after any killConnections() calls.
	 * 
	 * <br><br><b>Usage:</b> Initialize("ODBCSERVICE","USER","PASSWORD",10);
	 * @param NEWDBService	the odbc service
	 * @param NEWDBUser	the odbc user login
	 * @param NEWDBPass	the odbc user password
	 * @param NEWnumConnections	Connections to maintain
	 * @return NA
	 */
	public DBConnections(String NEWDBClass,
						 String NEWDBService, 
						 String NEWDBUser, 
						 String NEWDBPass, 
						 int NEWnumConnections,
						 boolean DoErrorQueueing)
	{
		DBClass=NEWDBClass;
		DBService=NEWDBService;
		DBUser=NEWDBUser;
		DBPass=NEWDBPass;
		numConnections=NEWnumConnections;
		Connections = new Vector();
		errorQueingEnabled=DoErrorQueueing;
		fixConnections(DBClass,DBService,DBUser,DBPass,numConnections);
	}

	/** 
	 * 
	 * <br><br><b>Usage: update("UPDATE...");</b> 
	 * @param updateString	the update SQL command
	 * @return int	the responseCode, or -1
	 */
	public int update(String updateString)
	{
		DBConnection DBToUse=null;
		int Result=-1;
		try
		{
			DBToUse=DBFetch();
			try
			{
				Result=DBToUse.update(updateString,0);
			}
			catch(Exception sqle)
			{
				// queued by the connection for retry
			}
			if(Result<0)
			{
				Log.errOut("DBConnections",""+DBToUse.getLastError());
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
	 * Should close all Connections, repopulate Connections
	 * with fresh new database connections.
	 * 
	 * <br><br><b>Usage:</b> fixConnections("SVC","USR","PASS",10);
	 * @param DBService	the odbc service
	 * @param DBUser	the odbc user login
	 * @param DBPass	the odbc user password
	 * @param numConnections	Connections to maintain
	 * @return NA
	 */
	private void fixConnections(String DBClass,
								String DBService, 
								String DBUser, 
								String DBPass, 
								int numConnections)
	{
		lockedUp=true;
		try
		{
			DBConnection DBConnect = new DBConnection(this,DBClass,DBService,DBUser,DBPass);
			killConnections();
			Connections=new Vector();
			synchronized(Connections)
			{
				for(int c=0;c<numConnections;c++)
				{
					if(DBConnect.ready())
						Connections.addElement(DBConnect);
					if(c<numConnections-1)
						DBConnect = new DBConnection(this,DBClass,DBService,DBUser,DBPass);
				}
			}
			lockedUp=false;
		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",""+sqle);
			lockedUp=true;
		}
	}
	
	private void repairConnections(String DBClass,
								   String DBService,
								   String DBUser,
								   String DBPass)
	{
		Log.errOut("DBConnections","Repairing connections...");	
		try
		{
			synchronized(Connections)
			{
				lockedUp=true;
				for(int c=0;c<Connections.size();c++)
				{
					DBConnection DBConnect=(DBConnection)Connections.elementAt(c);
					if((!DBConnect.ready())||(DBConnect.isProbablyDead())||(DBConnect.isProbablyLockedUp()))
					{
						DBConnection DBConnect2 = new DBConnection(this,DBClass,DBService,DBUser,DBPass);
						Connections.setElementAt(DBConnect2,c);
						Log.sysOut("DBConnections","Repaired one connection.");
					}
				}
				lockedUp=false;
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",""+sqle);
			lockedUp=true;
		}
		Log.errOut("DBConnections","Done repairing connections.");		
	}

	/** 
	 * Return the number of connections made. 
	 * 
	 * <br><br><b>Usage: n=numConnectionsMade();</b> 
	 * @param NA
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
	 * @param NA
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
			boolean connectionFailure=false;
			ThisDB=null;
			for(int i=0;i<Connections.size();i++)
			{
				try
				{
					DBConnection ADB=(DBConnection)Connections.elementAt(i);
					if(((!prepared)&&ADB.use(SQL))
					||(prepared&&ADB.usePrepared(SQL)))
					{
						Connections.remove(ADB);
						Connections.addElement(ADB);
						ThisDB=ADB;
						break;
					}
					else
					if((!ADB.ready())||(ADB.isProbablyDead())||(ADB.isProbablyLockedUp()))
						connectionFailure=true;
				}
				catch(java.lang.IndexOutOfBoundsException x){}
			}
			
			if(ThisDB==null)
			{
				if((consecutiveFailures++)>=50)
				{
					if(consecutiveFailures>50)
					{
						lockedUp=true;
						consecutiveFailures=0;
					}
					else
					{
						fixConnections(DBClass,DBService,DBUser,DBPass,numConnections);
					}
				}
				else
				{
					if(connectionFailure)
						repairConnections(DBClass,DBService,DBUser,DBPass);
					int inuse=0;
					for(int i=0;i<Connections.size();i++)
						if(((DBConnection)Connections.elementAt(i)).inUse())
							inuse++;
					if(consecutiveFailures==30)
						Log.errOut("DBConnections","Serious failure obtaining DBConnection ("+inuse+"/"+Connections.size()+" in use).");
					else
					if(consecutiveFailures==15)
						Log.errOut("DBConnections","Moderate failure obtaining DBConnection ("+inuse+"/"+Connections.size()+" in use).");
					else
					if(consecutiveFailures==5)
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
		}
		
		if(ThisDB!=null)
		{
			consecutiveFailures=0;
			lockedUp=false;
		}
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
	 * @return NA
	 */
	public void DBDone(DBConnection D)
	{
		D.doneUsing("");
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
	throws SQLException
	{
		try
		{
			String TVal=Results.getString(Field);
			if(TVal==null) 
				return "";
			else
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
				else
					return Long.parseLong(Val);
			}
			else
				return 0;

		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",""+sqle);
			return 0;
		}
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
			else
				return TVal.trim();
		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",""+sqle);
			return "";
		}
	}
	
	public static String getResQuietly(ResultSet Results, String Field)
	throws SQLException
	{
		try
		{
			String TVal=Results.getString(Field);
			if(TVal==null) 
				return "";
			else
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
	
	/** 
	 * Destroy all database connections, effectively
	 * shutting down this class.
	 * 
	 * <br><br><b>Usage:</b> killConnections();
	 * @param NA
	 * @return NA
	 */
	public void killConnections()
	{
		synchronized(Connections)
		{
			while(Connections.size()>0)
			{
				DBConnection DB=(DBConnection)Connections.elementAt(0);
				Connections.removeElement(DB);
				try
				{
					DB.close();
				}
				catch(SQLException sqle)
				{
					Log.errOut("DBConnections",""+sqle);
				}
			}
		}
	}
	
	/** 
	 * Return the happiness level of the connections
	 * <br><br><b>Usage:</b> amIOk()
	 * @param NA
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
	 * @return NA
	 */
	public void enQueueError(String SQLString, String SQLError, String tries)
	{
		if(!errorQueingEnabled)
		{
			Log.errOut("DBConnections","Error Queueing not enabled.");
			return;
		}
		
		synchronized(fileSemaphore)
		{
			PrintWriter out=null;
			try
			{
				out=new PrintWriter(new FileOutputStream("DBServer.que",true),true);
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
	 * @param NA
	 * @return NA
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
		
		synchronized(fileSemaphore)
		{
			File myFile=new File("DBServer.que");
			if(myFile!=null)
			{
				if(myFile.canRead())
				{
					// open a reader for the file
					BufferedReader in=null;
					try
					{
						in = new BufferedReader(new FileReader(myFile));
					}
					catch(FileNotFoundException f)
					{
						in=null;
					}
					
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
			}
			myFile.delete();
		}
		
		// did we actually READ anything?
		if(Queue.size()==0)
		{
			Log.sysOut("DBConnections","DB Retry Queue is empty.  Good.");
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
					int oldAttempts=Util.s_int(queueLine.substring(secondTab+5));
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
	
	/** write a buffer of data to numerous SQL records by
	 * breaking the buffer into small chunks
	 * 
	 * <br><br><b>Usage:</b> writeCheeseWheelBuffer(SQL, buf);
	 * @param SQL	The prepared statement string
	 * @param buf	the buffer to send
	 * @return int	the response code, or -1 for an error
	 */
	public int writeCheeseWheelBuffer(String SQL, byte[] buf)
	{
		DBConnection DB=null;
		try
		{
			DB=DBFetchPrepared(SQL);
			PreparedStatement preState=DB.getPreparedStatement();
			try
			{
				// ===== set the buffer index
				preState.setInt(1, 1);
							
				// ===== set the blob
				preState.setBytes(2,buf);
				//preState.setBinaryStream(2,(InputStream)bis,iNextBlockSize);
					
				// ===== execute the prepared statement
				if (preState.executeUpdate() < 0)
				{
					// ===== error out
					Log.errOut("DBConnections","Attachment update failed.");
					return -1;
				}
			}
			catch(Exception sqle)
			{
				Log.errOut("DBConnections",""+sqle);
			}
		}
		catch(Exception e)
		{
			enQueueError(SQL,""+e,""+0);
			reportError();
			Log.errOut("DBConnections",""+e);
		}
		if(DB!=null)
			DBDone(DB);
		return 0;
	}
	
	/** read a buffer of data to numerous SQL records by
	 * breaking the buffer into small chunks
	 * 
	 * <br><br><b>Usage:</b> buf=readCheeseWheelBuffer(SQL);
	 * @param SQL	The query string
	 * @param buf	the buffer to send
	 * @return byte[]	the buffer of data
	 */
	public byte[] readCheeseWheelBuffer(String SQL)
	{
		ByteArrayOutputStream buf=new ByteArrayOutputStream();
		DBConnection DB=null;
		try
		{
			DB=DBFetch();
			ResultSet allBufs=DB.query(SQL);
			if(allBufs!=null)
			{
				try
				{
					while(allBufs.next())
						buf.write(allBufs.getBytes("BlobData"));
				}
				catch(IOException e)
				{
					Log.errOut("DBConnections","Error writing bytes to array.");
				}
			}
			else
			{
				Log.errOut("DBConnections","Could not execute query: "+SQL);
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",""+sqle);
		}
		catch(Exception e)
		{
			Log.errOut("DBConnections",""+e);
		}
		if(DB!=null)
			DBDone(DB);
		return buf.toByteArray();
	}
	
	
	/** list the connections 
	 * 
	 * <br><br><b>Usage:</b> listConnections(out);
	 * @param PrintStream	place to send the list out to
	 * @return NA
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
		double size=new Integer(Connections.size()).doubleValue();
		double down=new Integer(consecutiveErrors).doubleValue();
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
	 * @param NA
	 * @return StringBuffer	complete error status
	 */
	public StringBuffer errorStatus()
	{
		StringBuffer status=new StringBuffer("");
		if(lockedUp)
			status.append("#100 DBCONNECTIONS REPORTING A LOCKED STATE\n");
		if(disconnected)
			status.append("#101 DBCONNECTIONS REPORTING A DISCONNECTED STATE\n");
		for(int c=0;c<Connections.size();c++)
		{
			DBConnection DBConnect=(DBConnection)Connections.elementAt(c);
			if((!DBConnect.ready())||(DBConnect.isProbablyDead())||(DBConnect.isProbablyLockedUp()))
			{
				repairConnections(DBClass,DBService,DBUser,DBPass);
				break;
			}
		}
		double size=new Integer(Connections.size()).doubleValue();
		double down=0.0;
		for(int p=0;p<Connections.size();p++)
		{
			DBConnection DB=(DBConnection)Connections.elementAt(p);
			if((!DB.ready())||(DB.isProbablyDead()))
			{
				status.append("#102-"+(p+1)+" DBCONNECTION IS REPORTING A DEAD STATE"+(DB.inSQLServerCommunication()?"(SERVER COMM)":""));
				down+=1.0;
			}
			else
			if(DB.isProbablyLockedUp())
			{
				status.append("#102-"+(p+1)+" DBCONNECTION IS REPORTING A LOCKED STATE "+(DB.inSQLServerCommunication()?"(SERVER COMM)":""));
				down+=1.0;
			}
		}
		if(((down/size)>0.25)||(lockedUp)||(disconnected))
			fixConnections(DBClass,DBService,DBUser,DBPass,numConnections);
		return status;
	}
}
