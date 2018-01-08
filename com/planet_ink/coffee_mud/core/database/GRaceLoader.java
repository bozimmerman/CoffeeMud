package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Int;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2018 Bo Zimmerman

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
public class GRaceLoader
{
	protected DBConnector DB=null;

	public GRaceLoader(DBConnector newDB)
	{
		DB=newDB;
	}

	public void DBDeleteRace(String raceID)
	{
		DB.update("DELETE FROM CMGRAC WHERE CMRCID='"+raceID+"'");
	}

	public void DBCreateRace(String raceID, String data)
	{
		DB.updateWithClobs(
		 "INSERT INTO CMGRAC ("
		 +"CMRCID, "
		 +"CMRDAT,"
		 +"CMRCDT "
		 +") values ("
		 +"'"+raceID+"',"
		 +"?,"
		 +System.currentTimeMillis()
		 +")",
		 data+" ");
	}
	
	public void DBPruneOldRaces()
	{
		final List<String> updates = new ArrayList<String>(1);
		final long oneHour = (60L * 60L * 1000L);
		final long expireDays = CMProps.getIntVar(Int.RACEEXPIRATIONDAYS);
		final long expireMs = (oneHour * expireDays * 24L);
		final long oldestDate = System.currentTimeMillis()- expireMs;
		final long oldestHour = System.currentTimeMillis()- oneHour;
		final List<DatabaseEngine.AckStats> ackStats = DBReadRaceStats();
		for(DatabaseEngine.AckStats stat : ackStats)
		{
			if(stat.creationDate() != 0)
			{
				final Race R=CMClass.getRace(stat.ID());
				if(R.usageCount(0) == 0)
				{
					if(stat.creationDate() < oldestDate)
					{
						updates.add("DELETE FROM CMGRAC WHERE CMRCID='"+stat.ID()+"';");
						CMClass.delRace(R);
						Log.sysOut("Expiring race '"+R.ID()+": "+R.name()+": "+CMLib.time().date2String(stat.creationDate()));
					}
				}
			}
			else
			{
				final long cDate = CMLib.dice().inRange(oldestDate, oldestHour);
				updates.add("UPDATE CMGRAC SET CMRCDT="+cDate+" WHERE CMRCID='"+stat.ID()+"';");
			}
		}
		if(updates.size()>0)
		{
			try
			{
				DB.update(updates.toArray(new String[0]));
			}
			catch(final Exception sqle)
			{
				Log.errOut("GRaceLoader",sqle);
			}
		}
	}

	protected List<DatabaseEngine.AckStats> DBReadRaceStats()
	{
		DBConnection D=null;
		final List<DatabaseEngine.AckStats> rows=new Vector<DatabaseEngine.AckStats>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMGRAC");
			while(R.next())
			{
				final String rcid = DBConnections.getRes(R,"CMRCID");
				final long rfirst = DBConnections.getLongRes(R, "CMRCDT");
				final DatabaseEngine.AckStats ack=new DatabaseEngine.AckStats()
				{
					@Override
					public String ID()
					{
						return rcid;
					}

					@Override
					public long creationDate()
					{
						return rfirst;
					}
				};
				rows.add(ack);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("GRaceLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}
	
	public List<DatabaseEngine.AckRecord> DBReadRaces()
	{
		DBConnection D=null;
		final List<DatabaseEngine.AckRecord> rows=new Vector<DatabaseEngine.AckRecord>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMGRAC");
			while(R.next())
			{
				final String rcid = DBConnections.getRes(R,"CMRCID");
				final String rdat = DBConnections.getRes(R,"CMRDAT");
				final DatabaseEngine.AckRecord ack=new DatabaseEngine.AckRecord()
				{
					@Override
					public String ID()
					{
						return rcid;
					}

					@Override
					public String data()
					{
						return rdat;
					}

					@Override
					public String typeClass()
					{
						return "GenRace";
					}
				};
				rows.add(ack);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("GRaceLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}
}
