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
import java.sql.ResultSet;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
/*
   Copyright 2001-2018 Bo Zimmerman

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
	private DBConnections	dbConnections		= null;
	private String			dbClass				= "";
	private String			dbService			= "";
	private String			dbUser				= "";
	private String			dbPass				= "";
	private boolean			dbReuse				= false;
	private int				numConnections		= 0;
	private int				dbPingIntMins		= 0;
	private boolean			doErrorQueueing		= false;
	private boolean			newErrorQueueing	= false;

	public static final class DBPreparedBatchEntry
	{
		public DBPreparedBatchEntry(final String sql)
		{
			this.sql = sql;
			this.clobs = new String[][] { {} };
		}

		public DBPreparedBatchEntry(final String sql, final String[] clobs)
		{
			this.sql = sql;
			this.clobs = new String[][] { clobs };
		}

		public DBPreparedBatchEntry(final String sql, final String clobs)
		{
			this.sql = sql;
			this.clobs = new String[][] { { clobs } };
		}

		public DBPreparedBatchEntry(final String sql, final String[][] clobs)
		{
			this.sql = sql;
			this.clobs = clobs;
		}

		public final String		sql;
		public final String[][]	clobs;
	}

	public DBConnector()
	{
		super();
	}

	public DBConnector (String dbClass,
						String dbService,
						String dbUser,
						String dbPass,
						int numConnections,
						int dbPingIntMins,
						boolean reuse,
						boolean doErrorQueueing,
						boolean retryErrorQueue)
	{
		super();
		this.dbClass=dbClass;
		this.dbService=dbService;
		this.dbUser=dbUser;
		this.dbPass=dbPass;
		this.numConnections=numConnections;
		this.doErrorQueueing=doErrorQueueing;
		this.newErrorQueueing=retryErrorQueue;
		this.dbReuse=reuse;
		this.dbPingIntMins=dbPingIntMins;
		if(this.dbPingIntMins<=0)
			this.dbPingIntMins=Integer.MAX_VALUE;
	}

	public void reconnect()
	{
		if (dbConnections != null)
		{
			dbConnections.deregisterDriver();
			dbConnections.killConnections();
		}
		dbConnections=new DBConnections(dbClass,dbService,dbUser,dbPass,numConnections,dbReuse,doErrorQueueing);
		if(dbConnections.amIOk()&&newErrorQueueing)
			dbConnections.retryQueuedErrors();
	}

	public String service()
	{
		return dbService;
	}

	public int getRecordCount(DBConnection D, ResultSet R)
	{
		if(D!=null)
			return D.getRecordCount(R);
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

	public boolean deregisterDriver()
	{
		if(dbConnections!=null)
			return dbConnections.deregisterDriver();
		return false;
	}

	public boolean isFakeDB()
	{
		return (dbConnections!=null)?dbConnections.isFakeDB():false;
	}

	public int update(final String[] updateStrings)
	{
		return (dbConnections != null) ? dbConnections.update(updateStrings) : 0;
	}

	public int update(final String updateString)
	{
		return (dbConnections != null) ? dbConnections.update(new String[] { updateString }) : 0;
	}

	public int updateWithClobs(String[] updateStrings, String[][][] values)
	{
		return (dbConnections != null) ? dbConnections.updateWithClobs(updateStrings, values) : 0;
	}

	public int updateWithClobs(final List<DBPreparedBatchEntry> entries)
	{
		return (dbConnections != null) ? dbConnections.updateWithClobs(entries) : 0;
	}

	public int updateWithClobs(final DBPreparedBatchEntry entry)
	{
		return updateWithClobs(entry.sql, entry.clobs);
	}

	public int updateWithClobs(final String updateString, final String... values)
	{
		return updateWithClobs(updateString, new String[][] { values });
	}

	public int updateWithClobs(final String updateString, final String[][] values)
	{
		return (dbConnections != null) ? dbConnections.updateWithClobs(updateString, values) : 0;
	}

	public int queryRows(String queryString)
	{
		return (dbConnections != null) ? dbConnections.queryRows(queryString) : 0;
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
		return (dbConnections != null) ? dbConnections.DBFetch() : null;
	}

	/**
	 * Fetch a single, not in use DBConnection object for testing only.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.  This is
	 * different than DBFetch because it ensures limited connection attempts.
	 *
	 * Usage: DB=DBFetchTest();
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchTest()
	{
		return (dbConnections != null) ? dbConnections.DBFetchTest() : null;
	}
	
	/**
	 * Fetch a single, not in use DBConnection object.  Must be rePrepared afterwards
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * Usage: DB=DBFetchEmpty();
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchEmpty()
	{
		return (dbConnections != null) ? dbConnections.DBFetchEmpty() : null;
	}

	public int numConnectionsMade()
	{
		return (dbConnections != null) ? dbConnections.numConnectionsMade() : 0;
	}

	public int numDBConnectionsInUse()
	{
		return (dbConnections != null) ? dbConnections.numInUse() : 0;
	}

	/**
	 * Fetch a single, not in use DBConnection object.
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 *
	 * Usage: DB=DBFetchPrepared();
	 * @param SQL    The prepared statement SQL
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchPrepared(String SQL)
	{
		return (dbConnections != null) ? dbConnections.DBFetchPrepared(SQL) : null;
	}

	/**
	 * Return a DBConnection object fetched with DBFetch()
	 *
	 * Usage:
	 * @param D    The Database connection to return to the pool
	 */
	public void DBDone(DBConnection D)
	{
		if (dbConnections != null)
			dbConnections.DBDone(D);
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
	public String getRes(ResultSet Results, String Field)
	{
		return DBConnections.getRes(Results, Field);
	}

	public String getResQuietly(ResultSet Results, String Field)
	{
		return DBConnections.getResQuietly(Results, Field);
	}

	public String injectionClean(String s)
	{
		if(s==null)
			return null;
		return s.replace('\'', '`');
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
	public long getLongRes(ResultSet Results, String Field)
	{
		return DBConnections.getLongRes(Results, Field);
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
	public String getRes(ResultSet Results, int One)
	{
		return DBConnections.getRes(Results, One);
	}

	/**
	 * Destroy all database connections, effectively
	 * shutting down this class.
	 *
	 * Usage: killConnections();
	 */
	public void killConnections()
	{
		if (dbConnections != null)
			dbConnections.killConnections();
	}

	/**
	 * Return the happiness level of the connections
	 * Usage: amIOk()
	 * @return boolean    true if ok, false if not ok
	 */
	public boolean amIOk()
	{
		return (dbConnections != null) ? dbConnections.amIOk() : false;
	}

	/**
	 * Pings all connections
	 * @param querySql the query to ping with
	 * @return the number of pings done
	 */
	public int pingAllConnections(final String querySql)
	{
		return (dbConnections!=null) ? dbConnections.pingAllConnections(querySql, dbPingIntMins * (60 * 1000)) : 0;
	}

	/**
	 * Pings all connections
	 * @param querySql the query to ping with
	 * @param overridePingIntMillis the age of a connection before a ping is necessary
	 * @return the number of pings done
	 */
	public int pingAllConnections(final String querySql, final long overridePingIntMillis)
	{
		return (dbConnections!=null) ? dbConnections.pingAllConnections(querySql, overridePingIntMillis) : 0;
	}

	/**
	 * Queue up a failed write/update for later processing.
	 *
	 * Usage: enQueueError("UPDATE SQL","error string");
	 * @param SQLString    UPDATE style SQL statement
	 * @param SQLError    The error message being reported
	 * @param count    The number of tries so far
	 */
	public void enQueueError(String SQLString, String SQLError, String count)
	{
		if (dbConnections != null)
			dbConnections.enQueueError(SQLString, SQLError, count);
	}

	/**
	 * Queue up a failed write/update for later processing.
	 *
	 * Usage: RetryQueuedErrors();
	 */
	public void retryQueuedErrors()
	{
		if (dbConnections != null)
			dbConnections.retryQueuedErrors();
	}

	/** list the connections
	 *
	 * Usage: listConnections(out);
	 * @param out    place to send the list out to
	 */
	public void listConnections(PrintStream out)
	{
		if (dbConnections != null)
			dbConnections.listConnections(out);
	}

	/** return a status string, or "" if everything is ok.
	 *
	 * Usage: errorStatus();
	 * @return StringBuffer    complete error status
	 */
	public StringBuffer errorStatus()
	{
		if(dbConnections==null)
			return new StringBuffer("Not connected.");
		final StringBuffer status=dbConnections.errorStatus();
		if(status.length()==0)
			return new StringBuffer("OK! Connections in use="+dbConnections.numInUse()+"/"+dbConnections.numConnectionsMade());
		return new StringBuffer("<BR>"+status.toString().replaceAll("\n","<BR>")+"Connections in use="+dbConnections.numInUse()+"/"+dbConnections.numConnectionsMade());
	}
}
