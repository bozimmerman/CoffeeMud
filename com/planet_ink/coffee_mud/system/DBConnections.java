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
	private String DBClass="sun.jdbc.odbc.JdbcOdbcDriver";
	/** the odbc service*/
	private String DBService="";
	/** the odbc login user */
	private String DBUser="";
	/** the odbc password */
	private String DBPass="";
	/** number of connections to make*/
	private int numConnections=0;
	/** the im in trouble flag*/
	private boolean inTrouble=false;
	/** the number of times the system has failed to get a db*/
	private int consecutiveFailures=0;
	/** Object to synchronize around on error handling*/
	private Boolean fileSemaphore=new Boolean(true);
	/** Object to synchronize around on error handling*/
	private boolean errorQueingEnabled=false;
	/** the database connnections */
	private Vector Connections;
	
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
		if(NEWDBClass.length()>0)
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
				Result=DBToUse.update(updateString);
			}
			catch(SQLException sqle)
			{}
			if(Result<0)
			{
				Log.errOut("DBConnections",""+DBToUse.getLastError());
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
	
	/** 
	 * 
	 * <br><br><b>Usage: updateGroup(V);</b> 
	 * @param Vector of strings to update with
	 * @return int	the responseCode, or -1
	 */
	public int updateGroup(Vector V)
	{
		DBConnection DBToUse=null;
		int Result=-1;
		try
		{
			DBToUse=DBFetch();
			for(int v=0;v<V.size();v++)
			{
				String updateString=(String)V.elementAt(v);
				try
				{
					Result=DBToUse.update(updateString);
				}
				catch(SQLException sqle)
				{}
				if(Result<0)
				{
					Log.errOut("DBConnections",""+DBToUse.getLastError());
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
		try
		{
			synchronized(Connections)
			{
				while(Connections.size()>0)
				{
					((DBConnection)Connections.elementAt(0)).close();
					Connections.removeElementAt(0);
				}
				Connections.clear();
				for(int c=0;c<numConnections;c++)
				{
					DBConnection DBConnect = new DBConnection(this,DBClass,DBService,DBUser,DBPass);
					Connections.addElement(DBConnect);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",sqle);
			inTrouble=true;
		}
	}

	public boolean deregisterDriver()
	{
		try
		{
			return true;
		}
		catch(Exception ce)
		{
		}
		return false;
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
		DBConnection ThisDB=null;
		while(ThisDB==null)
		{
			synchronized(Connections)
			{
				for(int i=0;i<Connections.size();i++)
				{
					ThisDB=(DBConnection)Connections.elementAt(i);
					if(ThisDB.use(""))
						break;
					ThisDB=null;
				}
			}
			
			if(ThisDB==null)
			{
				if((consecutiveFailures++)>=100)
				{
					if(consecutiveFailures>100)
					{
						inTrouble=true;
						consecutiveFailures=0;
					}
					else
						fixConnections(DBClass,DBService,DBUser,DBPass,numConnections);
				}
				else
				{
					try
					{
						Thread.sleep(Math.round(Math.random()*1000));
					}
					catch(InterruptedException i)
					{
					}
				}
			}
		}
		
		if(ThisDB!=null)
			consecutiveFailures=0;
		return ThisDB;
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
	public DBConnection DBFetchPrepared(String SQL)	
	{
		DBConnection ThisDB=null;
		while(ThisDB==null)
		{
			synchronized(Connections)
			{
				for(int i=0;i<Connections.size();i++)
				{
					ThisDB=(DBConnection)Connections.elementAt(i);
					if(ThisDB.usePrepared(SQL))
						break;
				}
			}
			
			if(ThisDB==null)
			{
				if((consecutiveFailures++)>=100)
				{
					if(consecutiveFailures>100)
					{
						inTrouble=true;
						consecutiveFailures=0;
					}
					else
						fixConnections(DBClass,DBService,DBUser,DBPass,numConnections);
				}
				else
				{
					try
					{
						Thread.sleep(Math.round(Math.random()*1000));
					}
					catch(InterruptedException i)
					{
					}
				}
			}
		}
		
		if(ThisDB!=null)
			consecutiveFailures=0;
		return ThisDB;
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
			if((sqle!=null)&&(sqle.getMessage()!=null))
				Log.errOut("DBConnections",sqle);
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
		String Val=null;
		try
		{
			Val=Results.getString(Field);
			if((Val!=null)&&(Val.trim().length()>0))
				return Long.parseLong(Val.trim());
			else
				return 0;

		}
		catch(SQLException sqle)
		{
			Log.errOut("DBConnections",sqle);
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
			Log.errOut("DBConnections",sqle);
			return "";
		}
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
				Log.errOut("DBConnections",sqle);
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
		return !inTrouble;
	}
	
	/** 
	 * Queue up a failed write/update for later processing.
	 * 
	 * <br><br><b>Usage:</b> enQueueError("UPDATE SQL","error string");
	 * @param SQLString	UPDATE style SQL statement
	 * @param SQLError	The error message being reported
	 * @return NA
	 */
	public void enQueueError(String SQLString, String SQLError)
		throws IOException
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
				Log.errOut("DBConnections",IQCalendar.d2String(System.currentTimeMillis())+"\t"+SQLError+"\t"+SQLString);
			}
					
			if(out!=null)
			{
				out.println(SQLString+"\t!|!\t"+SQLError+"\t!|!\t"+IQCalendar.d2String(System.currentTimeMillis()));
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
		
		if(inTrouble)
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
			Log.sysOut("DBConnections","DB Error queue was not processed.  Good.");
		}
		else
		{
			while(Queue.size()>0)
			{
				String queueLine=(String)Queue.elementAt(0);
				Queue.removeElementAt(0);
				
				int firstTab=queueLine.indexOf("\t!|!\t");
				int secondTab=queueLine.indexOf("\t!|!\t",firstTab+1);
				if((firstTab>0)&&(secondTab>firstTab))
				{
					DBConnection DB=null;
					try
					{
						DB=DBFetch();
						String retrySQL=queueLine.substring(0,firstTab);
						try
						{
							DB.update(retrySQL);
							Log.sysOut("DBConnections","Successfully retried: "+queueLine);
						}
						catch(SQLException sqle)
						{
							Log.errOut("DBConnections","Unsuccessfully retried line: "+queueLine);
						}
					}
					catch(Exception e)
					{
							Log.errOut("DBConnections",e+", Unsuccessfully retried line: "+queueLine);
					}
					if(DB!=null)
						DBDone(DB);
				}
				else
					Log.errOut("DBConnections","Could not retry line: "+queueLine+"/"+firstTab+"/"+secondTab);
			}
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
			catch(SQLException sqle)
			{
				Log.errOut("DBConnections",sqle);
			}
		}
		catch(Exception e)
		{
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
			Log.errOut("DBConnections",sqle);
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
		if(inTrouble)
			out.println("** Database is reporting a down status! **");
		
		for(int p=0;p<Connections.size();p++)
		{
			DBConnection DB=(DBConnection)Connections.elementAt(p);
			out.println(Integer.toString(p+1)
						+". Connected="+DB.ready()
						+", In use="+DB.inUse()
						+", Status="+(DB.isProbablyLockedUp()?"Locked up.":"OK")
						);
		}
		out.println("\n");
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
		
		if(inTrouble)
			status.append("#100 DBCONNECTIONS REPORTING A LOCKED STATE\n");
		for(int p=0;p<Connections.size();p++)
		{
			DBConnection DB=(DBConnection)Connections.elementAt(p);
			if(DB.isProbablyLockedUp())
				status.append("#101-"+(p+1)+" DBCONNECTION IS REPORTING A LOCKED STATE");
		}
		return status;
	}
}
