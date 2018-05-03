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
   Copyright 2014-2018 Bo Zimmerman

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
	
	public BackLogLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	
	protected int getCounter(String channelName, boolean bump)
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
					Log.errOut("Journal",sqle);
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
					Log.errOut("Journal",sqle);
				}
				finally
				{
					DB.DBDone(D);
				}
			}
			return counter[0];
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
			D.update("",0);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}
	
	public List<Pair<String,Long>> getBackLogEntries(String channelName, final int newestToSkip, final int numToReturn)
	{
		final List<Pair<String,Long>> list=new Vector<Pair<String,Long>>();
		if(channelName == null)
			return list;
		channelName = channelName.toUpperCase().trim();
		final int counter = getCounter(channelName, false);
		DBConnection D=null;
		try
		{
			final int number = numToReturn + newestToSkip;
			final int oldest = number >= counter ? 1 : (counter - number);
			final int newest = newestToSkip >= counter ? counter : (counter - newestToSkip);
			D=DB.DBFetch();
			StringBuilder sql=new StringBuilder("SELECT CMDATA,CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"'");
			sql.append(" AND CMINDX >="+oldest);
			sql.append(" AND CMINDX <="+newest);
			sql.append(" ORDER BY CMINDX");
			final ResultSet R = D.query(sql.toString());
			while(R.next())
				list.add(new Pair<String,Long>(DB.getRes(R, "CMDATA"),Long.valueOf(DB.getLongRes(R, "CMDATE"))));
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("Journal",sqle);
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
				Log.errOut("Journal",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
		}
	}
}
