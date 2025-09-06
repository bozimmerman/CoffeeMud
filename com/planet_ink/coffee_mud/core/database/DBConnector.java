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
import java.util.Map;
import java.util.Hashtable;
/*
   Copyright 2001-2025 Bo Zimmerman

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
/**
 * A DBConnector is a pool of DBConnection objects, all connected to the
 * same database.  It manages the connections, and provides
 * methods for querying and updating the database.
 * It also provides some static convenience methods for
 * cleaning strings for SQL statements, and for
 * getting values from a ResultSet.
 */
public class DBConnector
{
	private DBConnections		dbConnections		= null;
	private String				dbClass				= "";
	private String				dbService			= "";
	private String				dbUser				= "";
	private String				dbPass				= "";
	private Map<String, String>	dbParms				= new Hashtable<String, String>();
	private boolean				dbReuse				= false;
	private boolean				dbTransact			= false;
	private int					numConnections		= 0;
	private int					dbPingIntMins		= 0;
	private boolean				doErrorQueueing		= false;
	private boolean				newErrorQueueing	= false;

	/**
	 * A single prepared batch entry, consisting of a SQL string
	 * and a 2D array of CLOB strings.  Each row of the 2D array
	 * is a separate execution of the SQL string, and each column
	 * of the 2D array is a separate CLOB parameter to be
	 * set on the prepared statement.
	 */
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

	/**
	 * Default constructor. All fields must be set manually, and then
	 * reconnect() must be called before use.
	 */
	public DBConnector()
	{
		super();
	}

	/**
	 * Full constructor.
	 * @param dbClass the class name of the JDBC driver
	 * @param dbService the database URL
	 * @param dbUser the database user name
	 * @param dbPass the database user password
	 * @param dbParms additional connection parameters
	 * @param numConnections the number of connections to maintain in the pool
	 * @param dbPingIntMins the number of minutes between connection pings
	 * @param reuse if true, connections are reused
	 * @param transact if true, transactions are used for updates
	 * @param doErrorQueueing if true, failed updates are queued for later retry
	 * @param retryErrorQueue if true, queued updates are retried on reconnect
	 */
	public DBConnector (final String dbClass,
						final String dbService,
						final String dbUser,
						final String dbPass,
						final Map<String,String> dbParms,
						final int numConnections,
						final int dbPingIntMins,
						final boolean reuse,
						final boolean transact,
						final boolean doErrorQueueing,
						final boolean retryErrorQueue)
	{
		super();
		this.dbClass=dbClass;
		this.dbService=dbService;
		this.dbUser=dbUser;
		this.dbPass=dbPass;
		this.dbParms=dbParms;
		this.numConnections=numConnections;
		this.doErrorQueueing=doErrorQueueing;
		this.newErrorQueueing=retryErrorQueue;
		this.dbReuse=reuse;
		this.dbTransact=transact;
		this.dbPingIntMins=dbPingIntMins;
		if(this.dbPingIntMins<=0)
			this.dbPingIntMins=Integer.MAX_VALUE;
	}

	/**
	 * Reconnect to the database using the current settings.
	 */
	public void reconnect()
	{
		if (dbConnections != null)
		{
			dbConnections.deregisterDriver();
			dbConnections.killConnections();
		}
		dbConnections=new DBConnections(dbClass,dbService,dbUser,dbPass,dbParms,numConnections,dbReuse,dbTransact,doErrorQueueing);
		if(dbConnections.amIOk()&&newErrorQueueing)
			dbConnections.retryQueuedErrors();
	}

	/**
	 * Get the database class name.
	 * @return String the database class name
	 */
	public String service()
	{
		return dbService;
	}

	/**
	 *  Should bulk inserts be used?
	 * @return boolean true if bulk inserts should be used
	 */
	public boolean useBulkInserts()
	{
		return this.dbTransact && ((dbConnections==null)||(!dbConnections.getDBType().contains("oracle")));
	}

	/**
	 * Get the number of records in a ResultSet.
	 * @param D the DBConnection used to create the ResultSet, or null
	 * @param R the ResultSet to count records in
	 * @return int the number of records in the ResultSet
	 */
	public int getRecordCount(final DBConnection D, final ResultSet R)
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

	/**
	 * Deregister the JDBC driver.
	 * This should be called before application shutdown
	 * @return boolean true if successful, false otherwise
	 */
	public boolean deregisterDriver()
	{
		if(dbConnections!=null)
			return dbConnections.deregisterDriver();
		return false;
	}

	/**
	 * Check to see if this is a fake DB connection.
	 * @return boolean true if this is a fake DB connection
	 */
	public boolean isFakeDB()
	{
		return (dbConnections!=null)?dbConnections.isFakeDB():false;
	}

	/**
	 * Execute a series of SQL statements.
	 * @param updateStrings the SQL update strings to execute
	 * @return the return code of the update.
	 */
	public int update(final String[] updateStrings)
	{
		return (dbConnections != null) ? dbConnections.update(updateStrings) : 0;
	}

	/**
	 * Execute a single SQL statements.
	 *
	 * @param updateString the SQL update string to execute
	 * @return the return code of the update.
	 */
	public int update(final String updateString)
	{
		return (dbConnections != null) ? dbConnections.update(new String[] { updateString }) : 0;
	}

	/**
	 * Execute a series of SQL statements with CLOB parameters.
	 * @param updateStrings the SQL update strings to execute
	 * @param values the CLOB values to set on the prepared statements.
	 * @return the return code of the update.
	 */
	public int updateWithClobs(final String[] updateStrings, final String[][][] values)
	{
		return (dbConnections != null) ? dbConnections.updateWithClobs(updateStrings, values) : 0;
	}

	/**
	 * Execute a series of prepared SQL statements with CLOB parameters.
	 * @param entries the prepared batch entries to execute
	 * @return the return code of the update.
	 */
	public int updateWithClobs(final List<DBPreparedBatchEntry> entries)
	{
		return (dbConnections != null) ? dbConnections.updateWithClobs(entries) : 0;
	}

	/**
	 * Execute a single prepared SQL statement with CLOB parameters.
	 * @param entry the prepared batch entry to execute
	 * @return the return code of the update.
	 */
	public int updateWithClobs(final DBPreparedBatchEntry entry)
	{
		return updateWithClobs(entry.sql, entry.clobs);
	}

	/**
	 * Execute a single SQL statement with CLOB parameters.
	 * @param updateString the SQL update string to execute
	 * @param values the CLOB values to set on the prepared statement.
	 * @return the return code of the update.
	 */
	public int updateWithClobs(final String updateString, final String... values)
	{
		return updateWithClobs(updateString, new String[][] { values });
	}

	/**
	 * Execute a single SQL statement with multiple sets of CLOB parameters.
	 * @param updateString the SQL update string to execute
	 * @param values the CLOB values to set on the prepared statement.
	 * @return the return code of the update.
	 */
	public int updateWithClobs(final String updateString, final String[][] values)
	{
		return (dbConnections != null) ? dbConnections.updateWithClobs(updateString, values) : 0;
	}

	/**
	 * Execute a query, returning the ResultSet.
	 * @param queryString the SQL query string to execute
	 * @return ResultSet the results of the query
	 */
	public int queryRows(final String queryString)
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

	/**
	 * Return the number of connections made since startup
	 * @return int    number of connections made
	 */
	public int numConnectionsMade()
	{
		return (dbConnections != null) ? dbConnections.numConnectionsMade() : 0;
	}

	/**
	 * Return the number of connections currently in use
	 * @return int    number of connections in use
	 */
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
	public DBConnection DBFetchPrepared(final String SQL)
	{
		return (dbConnections != null) ? dbConnections.DBFetchPrepared(SQL) : null;
	}

	/**
	 * Return a DBConnection object fetched with DBFetch()
	 *
	 * Usage:
	 * @param D    The Database connection to return to the pool
	 */
	public void DBDone(final DBConnection D)
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
	 * @param results    The ResultSet object to use
	 * @param field 	   Field name to return
	 * @return String    The value of the field being returned
	 */
	public String getRes(final ResultSet results, final String field)
	{
		return DBConnections.getRes(results, field);
	}

	/**
	 * When reading a database table, this routine will read in
	 * the given Field NAME, returning the value.  The value
	 * will be trim()ed, and will be "" if NULL.
	 * @param Results   The ResultSet object to use
	 * @param Field        Field name to return
	 * @return String   The value of the field being returned
	 */
	public String getResQuietly(final ResultSet Results, final String Field)
	{
		return DBConnections.getResQuietly(Results, Field);
	}

	/**
	 * Cleans a string so that it is safe to use in an SQL statement.
	 * @param s   the string to be cleaned
	 * @return String   the cleaned string
	 */
	public String injectionClean(final String s)
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
	public long getLongRes(final ResultSet Results, final String Field)
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
	public String getRes(final ResultSet Results, final int One)
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
	public void enQueueError(final String SQLString, final String SQLError, final String count)
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
	 * @param stackDump also include stack traces on active connections
	 */
	public void listConnections(final PrintStream out, final boolean stackDump)
	{
		if (dbConnections != null)
			dbConnections.listConnections(out, stackDump);
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
