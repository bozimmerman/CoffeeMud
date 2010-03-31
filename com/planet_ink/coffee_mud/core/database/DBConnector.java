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
import java.sql.ResultSet;

import java.io.PrintStream;
import java.sql.SQLException;
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
public class DBConnector
{
	private DBConnections DBs=null;
	private String DBClass="";
	private String DBService="";
	private String DBUser="";
	private String DBPass="";
	private boolean DBReuse=false;
	private int numConnections=0;
	private boolean DoErrorQueueing=false;
	private boolean NewErrorQueueing=false;
	
	public DBConnector (){super();}
	
	public DBConnector (String NEWDBClass,
					    String NEWDBService, 
						String NEWDBUser, 
						String NEWDBPass, 
						int NEWnumConnections,
						boolean NEWReuse,
						boolean NEWDoErrorQueueing,
						boolean NEWRetryErrorQueue)
	{
		super();
		DBClass=NEWDBClass;
		DBService=NEWDBService;
		DBUser=NEWDBUser;
		DBPass=NEWDBPass;
		numConnections=NEWnumConnections;
		DoErrorQueueing=NEWDoErrorQueueing;
		NewErrorQueueing=NEWRetryErrorQueue;
		DBReuse=NEWReuse;
	}
	public void reconnect()
	{
		if(DBs!=null){ DBs.deregisterDriver(); DBs.killConnections();}
		DBs=new DBConnections(DBClass,DBService,DBUser,DBPass,numConnections,DBReuse,DoErrorQueueing);
		if(DBs.amIOk()&&NewErrorQueueing) DBs.retryQueuedErrors();
	}
	
	public String service(){ return DBService;}
	
	public int getRecordCount(DBConnection D, ResultSet R)
	{
		int recordCount=0;
		try
		{
			R.last(); 
			recordCount=R.getRow(); 
			R.beforeFirst();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return recordCount;
	}
	
	public boolean deregisterDriver()
    { 
		if(DBs!=null) return DBs.deregisterDriver();
		return false;
	}
	
	public int update(String[] updateStrings){ return (DBs!=null)?DBs.update(updateStrings):0;}
	public int update(String updateString){ return (DBs!=null)?DBs.update(new String[]{updateString}):0;}
	
	public int queryRows(String queryString){ return (DBs!=null)?DBs.queryRows(queryString):0;}

	/** 
	 * Fetch a single, not in use DBConnection object. 
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 * 
	 * <br><br><b>Usage: DB=DBFetch();</b> 
	 * @return DBConnection	The DBConnection to use
	 */
	public DBConnection DBFetch(){return (DBs!=null)?DBs.DBFetch():null;}
	
    public int numConnectionsMade(){return (DBs!=null)?DBs.numConnectionsMade():0;}
	public int numDBConnectionsInUse(){ return (DBs!=null)?DBs.numInUse():0;}
	
	/** 
	 * Fetch a single, not in use DBConnection object. 
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 * 
	 * <br><br><b>Usage: DB=DBFetchPrepared();</b> 
	 * @param SQL	The prepared statement SQL
	 * @return DBConnection	The DBConnection to use
	 */
	public DBConnection DBFetchPrepared(String SQL){ return (DBs!=null)?DBs.DBFetchPrepared(SQL):null;}
	/** 
	 * Return a DBConnection object fetched with DBFetch()
	 * 
	 * <br><br><b>Usage:</b> 
	 * @param D	The Database connection to return to the pool
	 */
	public void DBDone(DBConnection D){ if(DBs!=null) DBs.DBDone(D);}

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
	public String getRes(ResultSet Results, String Field)
	{ return DBConnections.getRes(Results,Field);}

	public String getResQuietly(ResultSet Results, String Field)
	{ return DBConnections.getResQuietly(Results, Field);}

	public String injectionClean(String s)
	{
		if(s==null) return null;
		return s.replace('\'', '`');
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
	public long getLongRes(ResultSet Results, String Field)
	{ return DBConnections.getLongRes(Results,Field);}
	
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
	public String getRes(ResultSet Results, int One)
	{ return DBConnections.getRes(Results,One);}
	
	/** 
	 * Destroy all database connections, effectively
	 * shutting down this class.
	 * 
	 * <br><br><b>Usage:</b> killConnections();
	 */
	public void killConnections(){ if(DBs!=null) DBs.killConnections();}
	
	/** 
	 * Return the happiness level of the connections
	 * <br><br><b>Usage:</b> amIOk()
	 * @return boolean	true if ok, false if not ok
	 */
	public boolean amIOk(){ return (DBs!=null)?DBs.amIOk():false;}
	
	/** 
	 * Queue up a failed write/update for later processing.
	 * 
	 * <br><br><b>Usage:</b> enQueueError("UPDATE SQL","error string");
	 * @param SQLString	UPDATE style SQL statement
	 * @param SQLError	The error message being reported
	 * @param count	The number of tries so far
	 */
	public void enQueueError(String SQLString, String SQLError, String count)
	{ if(DBs!=null)DBs.enQueueError(SQLString, SQLError,count);}
	
	
	/** 
	 * Queue up a failed write/update for later processing.
	 * 
	 * <br><br><b>Usage:</b> RetryQueuedErrors();
	 */
	public void retryQueuedErrors()
	{ if(DBs!=null)DBs.retryQueuedErrors();}
	
	/** list the connections 
	 * 
	 * <br><br><b>Usage:</b> listConnections(out);
	 * @param out	place to send the list out to
	 */
	public void listConnections(PrintStream out)
	{ if(DBs!=null)DBs.listConnections(out);}
	
	/** return a status string, or "" if everything is ok.
	 * 
	 * <br><br><b>Usage:</b> errorStatus();
	 * @return StringBuffer	complete error status
	 */
	public StringBuffer errorStatus()
	{ 
        if(DBs==null) return new StringBuffer("Not connected.");
		StringBuffer status=DBs.errorStatus();
		if(status.length()==0)
			return new StringBuffer("OK! Connections in use="+DBs.numInUse()+"/"+DBs.numConnectionsMade());
		return new StringBuffer("<BR>"+status.toString().replaceAll("\n","<BR>")+"Connections in use="+DBs.numInUse()+"/"+DBs.numConnectionsMade());
	}
}
