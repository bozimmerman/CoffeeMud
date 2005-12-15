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

import java.sql.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public static void DBRead(MudHost myHost)
	{
		CMLib.quests().shutdown();
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMQUESTS");
			while(R.next())
			{
				String questName=DBConnections.getRes(R,"CMQUESID");
				String questScript=DBConnections.getRes(R,"CMQSCRPT");
				String questWinners=DBConnections.getRes(R,"CMQWINNS");
				Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				Q.setScript(questScript);
				Q.setWinners(questWinners);
				if(Q.name().length()==0)
					Q.setName(questName);
				if(Q.name().length()==0)
                    Log.sysOut("QuestLoader","Unable to load Quest '"+questName+"' due to blank name.");
                else
                if(Q.duration()<=0)
                    Log.sysOut("QuestLoader","Unable to load Quest '"+questName+"' due to duration "+Q.duration()+".");
                else
                if(CMLib.quests().fetchQuest(Q.name())!=null)
                    Log.sysOut("QuestLoader","Unable to load Quest '"+questName+"' due to it already being loaded.");
                else
					CMLib.quests().addQuest(Q);
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Quest",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}
	
	
	public static void DBUpdateQuest(Quest Q)
	{
		if(Q==null) return;
		DBConnector.update("DELETE FROM CMQUESTS WHERE CMQUESID='"+Q.name()+"'");
		DBConnector.update(
		"INSERT INTO CMQUESTS ("
		+"CMQUESID, "
		+"CMQUTYPE, "
		+"CMQSCRPT, "
		+"CMQWINNS "
		+") values ("
		+"'"+Q.name()+"',"
		+"'"+CMClass.className(Q)+"',"
		+"'"+Q.script()+" ',"
		+"'"+Q.getWinnerStr()+" '"
		+")");
	}
	public static void DBUpdateQuests(Vector quests)
	{
		if(quests==null) quests=new Vector();
		DBConnector.update("DELETE FROM CMQUESTS WHERE CMQUTYPE='Quests'");
		for(int m=0;m<quests.size();m++)
		{
			Quest Q=(Quest)quests.elementAt(m);
			DBConnector.update(
			"INSERT INTO CMQUESTS ("
			+"CMQUESID, "
			+"CMQUTYPE, "
			+"CMQSCRPT, "
			+"CMQWINNS "
			+") values ("
			+"'"+Q.name()+"',"
			+"'"+CMClass.className(Q)+"',"
			+"'"+Q.script()+" ',"
			+"'"+Q.getWinnerStr()+" '"
			+")");
		}
	}

}
