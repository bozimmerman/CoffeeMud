package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

/**
* DBConnection.java 
*****************************************************
* Copyright 2000 WebIQ, Inc.
* Ramzi Sheikh
* 02-11-2000
*****************************************************
* Manages a single database connections, and its usage
* <br>
* Security Considerations:  NA
* <br>
* Performance Considerations:  NA
*/
public class DBConnection
{
	/** Connection object being used*/
	private Connection myConnection=null;
	
	/** (new) statement object being used currently */
	private Statement myStatement=null;
	
	/** (new) statement object being used currently */
	private PreparedStatement myPreparedStatement=null;
	
	/** Whether this dbconnection is being used */
	private boolean inUse;
	
	/** if any SQL errors occur, they are here.**/
	private String lastError=null;
	
	/** when this connection was put into use**/
	private long useTime=System.currentTimeMillis();
	
	/** parent container of this connection **/
	private DBConnections myParent=null;
	private String myDBClass="";
	private String myDBService=""; 
	private String myDBUser="";
	private String myDBPass="";

	private boolean communicationLinkFailure=false;
	
	/** 
	 * construction
	 * 
	 * <br><br><b>Usage:</b> DBConnection("","","");
	 * @param parentObject	the DBConnections object
	 * @param DBService	ODBC SERVICE
	 * @param DBUser	ODBC LOGIN USERNAME
	 * @param DBPass	ODBC LOGIN PASSWORD
	 * @return NA
	 */
	public DBConnection(DBConnections parentObject,
						String DBClass,
						String DBService, 
						String DBUser, 
						String DBPass)
		throws SQLException
	{
		myParent=parentObject;
		myDBClass=DBClass;
		myDBService=DBService;
		myDBUser=DBUser;
		myDBPass=DBPass;
		open();
		inUse=false;
	}
	
	/** 
	 * shut down this connection totally
	 * 
	 * <br><br><b>Usage:</b> close()
	 * @param NA
	 * @return NA
	 */
	public synchronized void close()
		throws SQLException
	{
		if(myConnection!=null)
			myConnection.close();
		myConnection=null;
		myStatement=null;
		myPreparedStatement=null;
		inUse=false;
	}
	
	public void open()
		throws SQLException
	{
		if(myParent==null)
			return;
		
		try
		{
			Class.forName(myDBClass);
		}
		catch(ClassNotFoundException ce)
		{
			System.out.println(ce);
		}
		myConnection=DriverManager.getConnection(myDBService,myDBUser,myDBPass);
	}
	
	/** 
	 * set up this connection for use
	 * 
	 * <br><br><b>Usage:</b> use("begin transaction")
	 * @param Opener	Any SQL string you'd like to send
	 * @return boolean	The connection being used
	 */
	public synchronized boolean use(String Opener)
	{
		if((!inUse)&&(ready()))
		{
			lastError=null;
		
			try
			{
				myPreparedStatement=null;
				myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			}
			catch(SQLException e)
			{
				myConnection=null;
				return false;
			}
		
			try
			{
				if(!Opener.equals(""))
					myStatement.executeUpdate(Opener);
			}
			catch(SQLException e)
			{
				return false;
				// not a real error?!
			}
		
			useTime=System.currentTimeMillis();
			inUse=true;
			return true;
		}
		else
			return false;
	}
	
	
	/** 
	 * set up this connection for use as a prepared statement
	 * 
	 * <br><br><b>Usage:</b> usePrepared("SQL String")
	 * @param SQL	Any SQL string you'd like to use
	 * @return boolean	The connection being used
	 */
	public synchronized boolean usePrepared(String SQL)
	{
		if((!inUse)&&(ready()))
		{
		
			lastError=null;
		
			try
			{
				myStatement=null;
				myPreparedStatement=myConnection.prepareStatement(SQL);
			}
			catch(SQLException e)
			{
				myConnection=null;
				return false;
			}
		
			useTime=System.currentTimeMillis();
			inUse=true;
			return true;
		}
		else
			return false;
	}
	
	/** 
	 * report this connection as being free
	 * 
	 * <br><br><b>Usage:</b> doneUsing("roll back");
	 * @param Closer	Any SQL string you'd like to send
	 * @return NA
	 */
	public synchronized void doneUsing(String Closer)
	{
		try
		{
			if(!Closer.equals(""))
				if(myStatement!=null)
					myStatement.executeUpdate(Closer);
		}
		catch(SQLException e)
		{
			// not a real error?
		}
		
		try
		{
			if(myStatement!=null)
				myStatement.close();
			if(myPreparedStatement!=null)
				myPreparedStatement.close();
		}
		catch(SQLException e)
		{
			// recoverable error
		}
		
		myStatement=null;
		myPreparedStatement=null;
		inUse=false;
	}
	
	/** 
	 * execute a query, returning the resultset
	 * 
	 * <br><br><b>Usage:</b> R=query("SELECT STATEMENT");
	 * @param QueryString	SQL query-style string
	 * @return ResultSet	The results of the query
	 */
	public synchronized ResultSet query(String queryString)
		throws SQLException
	{
		ResultSet R=null;
		if((inUse)&&(ready())) 
		{
			try
			{
				if(myStatement!=null)
					R=myStatement.executeQuery(queryString);
				else
					lastError="DBConnection Statement not open.";
			}
			catch(SQLException sqle)
			{
				if((!communicationLinkFailure)&&(sqle.getMessage().toUpperCase().indexOf("COMMUNICATION LINK FAILURE")>=0))
				{
					try
					{
						communicationLinkFailure=true;
						close();
						open();
						use("");
						ResultSet resSet=query(queryString);
						communicationLinkFailure=false;
						return resSet;
					}
					catch(SQLException sqle2)
					{
						lastError=""+sqle2;
						throw sqle2;
					}
					
				}
				else
				{
					lastError=""+sqle;
					throw sqle;
				}
			}
		}
		else
		{
			lastError="DBConnection not ready.";
		}
		
		useTime=System.currentTimeMillis();
		return R;
	}
	
	/** 
	 * execute an sql update, returning the status
	 * 
	 * <br><br><b>Usage:</b> update("UPDATE STATEMENT");
	 * @param UpdateString	SQL update-style string
	 * @return int	The status of the update
	 */
	public synchronized int update(String updateString)
		throws SQLException
	{
		int responseCode=-1;
		if((inUse)&&(ready()))
		{
			try
			{
				if(myStatement!=null)
					responseCode=myStatement.executeUpdate(updateString);
				else
					lastError="DBConnection Statement not open.";
			}
			catch(SQLException sqle)
			{
				if((!communicationLinkFailure)&&(sqle.getMessage().toUpperCase().indexOf("COMMUNICATION LINK FAILURE")>=0))
				{
					try
					{
						communicationLinkFailure=true;
						close();
						open();
						use("");
						update(updateString);
						communicationLinkFailure=false;
					}
					catch(SQLException sqle2)
					{
						try
						{
							lastError=""+sqle2;
							if(myParent!=null)
								myParent.enQueueError(updateString,""+sqle2);
							throw sqle2;
						}
						catch(IOException e)
						{
							throw sqle2;
						}
					}
					
				}
				else
				{
					try
					{
						lastError=""+sqle;
						if(myParent!=null)
							myParent.enQueueError(updateString,""+sqle);
						throw sqle;
					}
					catch(IOException e)
					{
						throw sqle;
					}
				}
			}
		}
		
		useTime=System.currentTimeMillis();
		return responseCode;
	}
	
	/** 
	 * returns whether this connection is ready for use
	 * 
	 * <br><br><b>Usage:</b> ready();
	 * @param NA
	 * @return boolean	Whether this connection is ready
	 */
	public boolean ready()
	{
		return (myConnection!=null);
	}
	
	/** 
	 * returns whether this connection is in use
	 * 
	 * <br><br><b>Usage:</b> inUse();
	 * @param NA
	 * @return boolean	Whether this connection is in use
	 */
	public boolean inUse()
	{
		return inUse;
	}
	
	
	/** 
	 * returns whether this connection is *probably* locked up
	 * 
	 * <br><br><b>Usage:</b> isProbablyLockedUp();
	 * @param NA
	 * @return boolean	Whether this connection is locked up
	 */
	public boolean isProbablyLockedUp()
	{
		if(communicationLinkFailure)
			return true;
		
		if((useTime<(System.currentTimeMillis()-60000))&&inUse) 
			return true;
		else
			return false;
	}
	
	/** 
	 * returns an error if there was one
	 * 
	 * <br><br><b>Usage:</b> getLastError();
	 * @param NA
	 * @return String	The last error SQL string, if any
	 */
	public String getLastError()
	{
		if(lastError==null)
			return "";
		else
			return lastError;
	}
	
	
	/** 
	 * returns the prepared statement, if creates
	 * 
	 * <br><br><b>Usage:</b> getPreparedStatement();
	 * @param NA
	 * @return PreparedStatement	the prepared statement
	 */
	public PreparedStatement getPreparedStatement()
	{
		return myPreparedStatement;
	}
}
