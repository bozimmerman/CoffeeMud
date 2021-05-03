package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;

/*
   Copyright 2014-2021 Bo Zimmerman

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
public class BackLogLoader
{
	protected DBConnector DB=null;
	protected Map<String,int[]> counters = new Hashtable<String,int[]>();

	public BackLogLoader(final DBConnector newDB)
	{
		DB=newDB;
	}

	protected int getCounter(final String channelName, final boolean bump)
	{
		synchronized(("BACKLOG_"+channelName).intern())
		{
			int[] counter = counters.get(channelName);
			if(counter == null)
			{
				DBConnection D=null;
				try
				{
					D=DB.DBFetch();
					final ResultSet R=D.query("SELECT CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX = 0");
					if(R.next())
					{
						counters.put(channelName, new int[] { (int)DBConnections.getLongRes(R, "CMDATE") });
						R.close();
					}
					else
					{
						R.close();
						D.update("INSERT INTO CMBKLG (CMNAME,  CMINDX, CMDATE) VALUES ('"+channelName+"', 0, 0)", 0);
						counters.put(channelName, new int[] {0});
					}
				}
				catch(final Exception sqle)
				{
					Log.errOut("BackLog",sqle);
				}
				finally
				{
					DB.DBDone(D);
				}
				counter = counters.get(channelName);
			}
			if(bump)
			{
				DBConnection D=null;
				try
				{
					D=DB.DBFetch();
					synchronized(counter)
					{
						counter[0]++;
						D.update("UPDATE CMBKLG SET CMDATE="+counter[0]+" WHERE CMNAME='"+channelName+"' AND CMINDX = 0", 0);
					}
				}
				catch(final Exception sqle)
				{
					Log.errOut("BackLog",sqle);
				}
				finally
				{
					DB.DBDone(D);
				}
			}
			return counter[0];
		}
	}

	public Integer checkSetBacklogTableVersion(final Integer setVersion)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			Integer existingVersion = null;
			final ResultSet R=D.query("SELECT CMDATE FROM CMBKLG WHERE CMNAME='TABLE_VERSION' AND CMINDX = 0");
			if(R.next())
			{
				final Integer I= Integer.valueOf((int)R.getLong("CMDATE"));
				existingVersion=I;
			}
			R.close();
			if(setVersion == null)
				return existingVersion == null ? Integer.valueOf(0) : existingVersion;
			if(existingVersion == null)
				D.update("INSERT INTO CMBKLG (CMNAME,  CMINDX, CMDATE) VALUES ('TABLE_VERSION', 0, "+setVersion.intValue()+")", 0);
			else
				D.update("UPDATE CMBKLG SET CMDATE="+setVersion.intValue()+" WHERE CMNAME='TABLE_VERSION' AND CMINDX=0;", 0);
			return setVersion;
		}
		catch(final SQLException sqle)
		{
			Log.errOut(sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return setVersion;
	}

	public void updateBackLogEntry(String channelName, final int index, final long date, final String entry)
	{
		if((entry == null) || (channelName == null) || (entry.length()==0))
			return;
		channelName = channelName.toUpperCase().trim();
		DBConnection D=null;
		try
		{
			D=DB.DBFetchPrepared("UPDATE CMBKLG SET CMDATE="+date+", CMDATA=? WHERE CMNAME='"+channelName+"' AND CMINDX="+index);
			D.setPreparedClobs(new String[]{entry});
			try
			{
				D.update("",0);
			}
			catch(final Exception sqle)
			{
				Log.errOut("Fail: "+sqle.getMessage());
				DB.DBDone(D);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void addBackLogEntry(String channelName, final String entry)
	{
		if((entry == null) || (channelName == null) || (entry.length()==0))
			return;
		channelName = channelName.toUpperCase().trim();
		final int counter = getCounter(channelName, true);
		DBConnection D=null;
		try
		{
			D=DB.DBFetchPrepared("INSERT INTO CMBKLG (CMNAME,  CMINDX, CMDATE, CMDATA) VALUES ('"+channelName+"', "+counter+", "+System.currentTimeMillis()+", ?)");
			D.setPreparedClobs(new String[]{entry});
			try
			{
				D.update("",0);
			}
			catch(final Exception sqle)
			{
				// retry for duplicate entries, but how could that even happen?!
				Log.errOut("Retry: "+sqle.getMessage());
				DB.DBDone(D);
				final int counter2 = getCounter(channelName, true);
				D=DB.DBFetchPrepared("INSERT INTO CMBKLG (CMNAME,  CMINDX, CMDATE, CMDATA) VALUES ('"+channelName+"', "+counter2+", "+System.currentTimeMillis()+", ?)");
				D.setPreparedClobs(new String[]{entry});
				D.update("",0);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void delBackLogEntry(String channelName, final long timeStamp)
	{
		if(channelName == null)
			return;
		channelName = channelName.toUpperCase().trim();
		DBConnection D=DB.DBFetch();
		String[] updates = new String[0];
		try
		{
			try
			{
				D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMDATE="+timeStamp,0);
			}
			catch(final Exception sqle)
			{
				// retry for duplicate entries, but how could that even happen?!
				Log.errOut("Retry: "+sqle.getMessage());
				DB.DBDone(D);
				D=DB.DBFetch();
				D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMDATE="+timeStamp,0);
			}
			{
				final List<String> updateV = new ArrayList<String>();
				ResultSet R = D.query("SELECT CMDATE, CMINDX FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMDATE > "+timeStamp+" AND CMINDX>0");
				if(R!=null)
				{
					while(R.next())
					{
						final long ts = R.getLong("CMDATE");
						final int index = R.getInt("CMINDX");
						updateV.add("UPDATE CMBKLG SET CMINDX="+(index-1)+" WHERE CMNAME='"+channelName+"' AND CMINDX="+index+" AND CMDATE="+ts+";");
					}
					R.close();
				}
				R = D.query("SELECT CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX=0");
				if(R!=null)
				{
					if(R.next())
					{
						final long ts = R.getLong("CMDATE");
						updateV.add("UPDATE CMBKLG SET CMDATE="+(ts-1)+" WHERE CMNAME='"+channelName+"' AND CMINDX=0;");
					}
					R.close();
				}
				updates = updateV.toArray(updates);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if((updates != null) && (updates.length>0))
		{
			try
			{
				DB.update(updates);
				synchronized(("BACKLOG_"+channelName).intern())
				{
					counters.remove(channelName);
				}
			}
			catch(final Exception sqle)
			{
				Log.errOut("BackLog",sqle);
			}
		}
	}

	public List<Triad<String,Integer,Long>> enumBackLogEntries(String channelName, final int firstIndex, final int numToReturn)
	{
		final List<Triad<String,Integer,Long>> list=new Vector<Triad<String,Integer,Long>>();
		if(channelName == null)
			return list;
		channelName = channelName.toUpperCase().trim();
		final StringBuilder sql=new StringBuilder("SELECT CMDATA,CMINDX,CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"'");
		sql.append(" AND CMINDX >="+firstIndex);
		sql.append(" ORDER BY CMINDX");
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R = D.query(sql.toString());
			while((R.next())&&(list.size()<numToReturn))
				list.add(new Triad<String,Integer,Long>(DB.getRes(R, "CMDATA"),Integer.valueOf((int)DB.getLongRes(R,"CMINDX")),Long.valueOf(DB.getLongRes(R, "CMDATE"))));
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return list;

	}

	public List<Triad<String,Integer,Long>> getBackLogEntries(String channelName, final Set<String> extraData, final int newestToSkip, final int numToReturn)
	{
		final List<Triad<String,Integer,Long>> list=new Vector<Triad<String,Integer,Long>>();
		if(channelName == null)
			return list;
		channelName = channelName.toUpperCase().trim();
		final int counter = getCounter(channelName, false);
		final List<String> extraDataFixed=new LinkedList<String>();
		final String[][] extraDataFixes = new String[][] {
			{ "\\", "\\\\" },
			{ "'", "\\'"},
			{ "%","\\%"}
		};
		for(final String str : extraData)
			extraDataFixed.add("%<D>" + CMStrings.replaceAlls(str,extraDataFixes)+"</D>%");
		DBConnection D=null;
		try
		{
			final int number = numToReturn + newestToSkip;
			final int oldest = number >= counter ? 1 : (counter - number + 1);
			final int newest = newestToSkip >= counter ? counter : (counter - newestToSkip);
			D=DB.DBFetch();
			final StringBuilder sql=new StringBuilder("SELECT CMDATA,CMINDX,CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"'");
			sql.append(" AND CMINDX >="+oldest);
			sql.append(" AND CMINDX <="+newest);
			/*
			if(extraDataFixed.size()==1)
				sql.append(" AND CMDATA LIKE '"+extraDataFixed.iterator().next()+"'");
			else
			if(extraDataFixed.size() > 1)
			{
				sql.append(" AND (");
				boolean first=false;
				for(final String str : extraDataFixed)
					sql.append((first?"":" OR ")+"CMDATA LIKE '"+str+"'");
				sql.append(")");
			}
			*/
			sql.append(" ORDER BY CMINDX");
			final ResultSet R = D.query(sql.toString());
			while((R.next())&&(list.size()<numToReturn))
				list.add(new Triad<String,Integer,Long>(DB.getRes(R, "CMDATA"),Integer.valueOf((int)DB.getLongRes(R,"CMINDX")),Long.valueOf(DB.getLongRes(R, "CMDATE"))));
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return list;
	}

	public void trimBackLogEntries(final String[] channels, final int maxMessages, final long oldestTime)
	{
		for(final String channelName : channels)
		{
			final int counter = getCounter(channelName, false);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				if((maxMessages == 0) && (D != null))
				{
					D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"'",0);
				}
				else
				if((maxMessages < counter) && (D != null))
				{
					final int oldestCounter = counter - maxMessages;
					D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX != 0 AND CMINDX < "+oldestCounter,0);
				}
				if((oldestTime > 0) && (D != null))
				{
					D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX != 0 AND CMDATE < "+oldestTime,0);
				}
			}
			catch(final Exception sqle)
			{
				Log.errOut("BackLog",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
		}
	}
}
