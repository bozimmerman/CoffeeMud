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

import java.sql.*;
import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class QuestLoader
{
	protected DBConnector DB=null;
	public QuestLoader(DBConnector newDB)
	{
		DB=newDB;
	}

	public List<Quest> DBRead()
	{
		List<Quest> quests=new LinkedList<Quest>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMQUESTS");
			while(R.next())
			{
				final String questName=DBConnections.getRes(R,"CMQUESID");
				final String questScript=DBConnections.getRes(R,"CMQSCRPT");
				final String questWinners=DBConnections.getRes(R,"CMQWINNS");
				final long flags=DBConnections.getLongRes(R, "CMQFLAGS");
				final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				Q.setFlags(flags);
				final boolean loaded=Q.setScript(questScript,!Q.suspended());
				Q.setFlags(flags);
				Q.setWinners(questWinners);
				if(Q.name().length()==0)
					Q.setName(questName);
				if(!loaded)
				{
					if(!Q.suspended())
					{
						Log.sysOut("QuestLoader","Unable to load Quest '"+questName+"'.  Suspending.");
						Q.setSuspended(true);
					}
					boolean dup=false;
					for(Quest Q2 : quests)
					{
						if(Q2.name().equalsIgnoreCase(Q.name()))
							dup=true;
					}
					if(!dup)
						quests.add(Q);
					continue;
				}
				if(Q.name().length()==0)
					Log.sysOut("QuestLoader","Unable to load Quest '"+questName+"' due to blank name.");
				else
				if(Q.duration()<0)
					Log.sysOut("QuestLoader","Unable to load Quest '"+questName+"' due to duration "+Q.duration()+".");
				else
				if(CMLib.quests().fetchQuest(Q.name())!=null)
					Log.sysOut("QuestLoader","Unable to load Quest '"+questName+"' due to it already being loaded.");
				else
					quests.add(Q);
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Quest",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return quests;
	}

	public void DBUpdateQuest(Quest Q)
	{
		if(Q==null)
			return;
		DB.update("DELETE FROM CMQUESTS WHERE CMQUESID='"+Q.name()+"'");
		DB.updateWithClobs(
		"INSERT INTO CMQUESTS ("
		+"CMQUESID, "
		+"CMQUTYPE, "
		+"CMQFLAGS, "
		+"CMQSCRPT, "
		+"CMQWINNS "
		+") values ("
		+"'"+Q.name()+"',"
		+"'"+CMClass.classID(Q)+"',"
		+Q.getFlags()+","
		+"?,"
		+"?"
		+")", new String[][]{{Q.script()+" ",Q.getWinnerStr()+" "}});
	}

	public void DBUpdateQuests(List<Quest> quests)
	{
		if(quests==null)
			quests=new Vector<Quest>();
		DBConnection D=null;
		final List<Quest> addThese=new LinkedList<Quest>();
		final Set<String> types=new HashSet<String>();
		for(Quest Q : quests)
		{
			if(!Q.isCopy())
			{
				addThese.add(Q);
				types.add(CMClass.classID(Q));
			}
		}
		final List<Quest> updateThese=new ArrayList<Quest>();
		final List<String[]> deleteThese=new LinkedList<String[]>();
		try
		{
			D=DB.DBFetch();
			try
			{
				final ResultSet R=D.query("SELECT CMQUESID,CMQUTYPE FROM CMQUESTS");
				while(R.next())
				{
					final String questName=DBConnections.getRes(R,"CMQUESID");
					final String questType=DBConnections.getRes(R,"CMQUTYPE");
					boolean found=false;
					for(Iterator<Quest> i=addThese.iterator();i.hasNext();)
					{
						Quest Q=i.next();
						if((Q.name().equals(questName))
						&&(questType.equals(CMClass.classID(Q))))
						{
							i.remove();
							updateThese.add(Q);
							found=true;
						}
					}
					if((!found)&&(types.contains(questType)))
						deleteThese.add(new String[]{questName, questType});
				}
				R.close();
			}
			catch(final SQLException sqle)
			{
				Log.errOut("Quest",sqle);
				return;
			}
			for(final String[] delQ : deleteThese)
			{
				try
				{
					D.update("DELETE FROM CMQUESTS WHERE CMQUESID='"+delQ[0]+"' AND CMQUTYPE='"+delQ[1]+"'",0);
				}
				catch(final SQLException sqle)
				{
					Log.errOut("Quest",sqle);
					return;
				}
			}
			for(final Quest Q : updateThese)
			{
				try
				{
					D.rePrepare(
					"UPDATE CMQUESTS SET "
					+"CMQFLAGS="+Q.getFlags()+", "
					+"CMQSCRPT=?, "
					+"CMQWINNS=? "
					+"WHERE CMQUESID='"+Q.name()+"' AND CMQUTYPE='"+CMClass.classID(Q)+"'");
					D.setPreparedClobs(new String[]{Q.script()+" ",Q.getWinnerStr()+" "});
					D.update("",0);
				}
				catch(final java.sql.SQLException sqle)
				{
					Log.errOut("Quest",sqle);
				}
			}
			for(final Quest Q : addThese)
			{
				try
				{
					D.rePrepare(
					"INSERT INTO CMQUESTS ("
					+"CMQUESID, "
					+"CMQUTYPE, "
					+"CMQFLAGS, "
					+"CMQSCRPT, "
					+"CMQWINNS "
					+") values ("
					+"'"+Q.name()+"',"
					+"'"+CMClass.classID(Q)+"',"
					+Q.getFlags()+","
					+"?,"
					+"?"
					+")");
					D.setPreparedClobs(new String[]{Q.script()+" ",Q.getWinnerStr()+" "});
					D.update("",0);
				}
				catch(final java.sql.SQLException sqle)
				{
					Log.errOut("Quest",sqle);
				}
			}
		}
		finally
		{
			DB.DBDone(D);
		}
	}

}
