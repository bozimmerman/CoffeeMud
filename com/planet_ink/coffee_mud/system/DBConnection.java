package com.planet_ink.coffee_mud.system;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	private IQCalendar useTime=IQCalendar.getIQInstance();
	
	/** number of failures in a row */
	private int failuresInARow=0;
	
	private boolean sqlserver=false;
	
	/** parent container of this connection **/
	private DBConnections myParent=null;	
	
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
		if((DBClass==null)||(DBClass.length()==0))
			DBClass="sun.jdbc.odbc.JdbcOdbcDriver";
		try
		{
			Class.forName(DBClass);
		}
		catch(ClassNotFoundException ce)
		{
			ce.printStackTrace();
		}
		sqlserver=true;
		myConnection=DriverManager.getConnection(DBService,DBUser,DBPass);
		sqlserver=false;
		inUse=false;
	}
	
	public String catalog()
	{
		try{
			return myConnection.getCatalog();
		}
		catch(Exception e){}
		return "";
	}
	
	/** 
	 * shut down this connection totally
	 * 
	 * <br><br><b>Usage:</b> close()
	 * @param NA
	 * @return NA
	 */
	public void close()
		throws SQLException
	{
		if(myConnection!=null)
			myConnection.close();
		myConnection=null;
		myStatement=null;
		myPreparedStatement=null;
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
		if((!inUse)&&(ready())&&(!isProbablyDead()))
		{
			lastError=null;
			try
			{
				myPreparedStatement=null;
				sqlserver=true;
				myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			}
			catch(SQLException e)
			{
				myConnection=null;
				failuresInARow++;
				sqlserver=false;
				return false;
			}
		
			sqlserver=false;
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
		
			useTime=IQCalendar.getIQInstance();
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
				sqlserver=true;
				myPreparedStatement=myConnection.prepareStatement(SQL);
				sqlserver=false;
			}
			catch(SQLException e)
			{
				sqlserver=false;
				myConnection=null;
				failuresInARow++;
				return false;
			}
		
			sqlserver=false;
			useTime=IQCalendar.getIQInstance();
			failuresInARow=0;
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
	public void doneUsing(String Closer)
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
	public ResultSet query(String queryString)
		throws SQLException
	{
		ResultSet R=null;
		if((inUse)&&(ready())) 
		{
			try
			{
				sqlserver=true;
				if(myStatement!=null)
					R=myStatement.executeQuery(queryString);
				else
					lastError="DBConnection Statement not open.";
				sqlserver=false;
			}
			catch(SQLException sqle)
			{
				sqlserver=false;
				failuresInARow++;
				if(myParent!=null) 
					myParent.reportError();
				lastError=""+sqle;
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
		useTime=IQCalendar.getIQInstance();
		if(myParent!=null) 
			myParent.clearErrors();
		return R;
	}
	
	/** 
	 * execute an sql update, returning the status
	 * 
	 * <br><br><b>Usage:</b> update("UPDATE STATEMENT");
	 * @param UpdateString	SQL update-style string
	 * @return int	The status of the update
	 */
	public int update(String updateString, int retryNum)
		throws SQLException
	{
		int responseCode=-1;
		if((inUse)&&(ready()))
		{
			try
			{
				sqlserver=true;
				if(myStatement!=null)
					responseCode=myStatement.executeUpdate(updateString);
				else
					lastError="DBConnection Statement not open.";
				sqlserver=false;
			}
			catch(SQLException sqle)
			{
				sqlserver=false;
				if((sqle.getMessage()==null)
				||(sqle.getMessage().toUpperCase().indexOf("PRIMARY KEY")<0))
					failuresInARow++;
				lastError=""+sqle;
				if(myParent!=null)
				{
					myParent.enQueueError(updateString,""+sqle,""+(retryNum+1));
					myParent.reportError();
				}
				throw sqle;
			}
		}
		
		sqlserver=false;
		useTime=IQCalendar.getIQInstance();
		failuresInARow=0;
		if(myParent!=null) 
			myParent.clearErrors();
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
	 * known errors should not be a reason to report a dead state
	 * 
	 * <br><br><b>Usage:</b> clearFailures();
	 * @param NA
	 * @return NA
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
	 * returns whether this connection is *probably* dead
	 * 
	 * <br><br><b>Usage:</b> isProbablyDead();
	 * @param NA
	 * @return boolean	Whether this connection is probably dead
	 */
	public boolean isProbablyDead()
	{
		try
		{
			if((myConnection==null)||(myConnection.isClosed())||(failuresInARow>2))
				return true;
			else
				return false;
		}
		catch(SQLException e)
		{
			return true;
		}
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
		IQCalendar C=IQCalendar.getIQInstance();
		C.add(IQCalendar.MINUTE,-2);
		if(useTime.before(C)&&inUse) 
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
