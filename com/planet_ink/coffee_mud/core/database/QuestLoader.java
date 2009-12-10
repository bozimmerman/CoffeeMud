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
@SuppressWarnings("unchecked")
public class QuestLoader
{
	protected DBConnector DB=null;
	public QuestLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	public void DBRead(MudHost myHost)
	{
		CMLib.quests().shutdown();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
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
                if(Q.duration()<0)
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
		if(D!=null) DB.DBDone(D);
	}
	
	
	public void DBUpdateQuest(Quest Q)
	{
		if(Q==null) return;
		DB.update("DELETE FROM CMQUESTS WHERE CMQUESID='"+Q.name()+"'");
		DB.update(
		"INSERT INTO CMQUESTS ("
		+"CMQUESID, "
		+"CMQUTYPE, "
		+"CMQSCRPT, "
		+"CMQWINNS "
		+") values ("
		+"'"+Q.name()+"',"
		+"'"+CMClass.classID(Q)+"',"
		+"'"+Q.script()+" ',"
		+"'"+Q.getWinnerStr()+" '"
		+")");
	}
	public void DBUpdateQuests(Vector quests)
	{
		if(quests==null) quests=new Vector();
		String quType="DefaultQuest";
		if(quests.size()>0) quType=CMClass.classID(quests.firstElement());
		DBConnection D=null;
		DB.update("DELETE FROM CMQUESTS WHERE CMQUTYPE='"+quType+"'");
		try{Thread.sleep((1000+(quests.size()*100)));}catch(Exception e){}
		if(DB.queryRows("SELECT * FROM CMQUESTS WHERE CMQUTYPE='"+quType+"'")>0) 
			Log.errOut("Failed to delete quest typed '"+quType+"'.");
		DB.update("DELETE FROM CMQUESTS WHERE CMQUTYPE='Quests'");
		try{Thread.sleep((1000+(quests.size()*100)));}catch(Exception e){}
		if(DB.queryRows("SELECT * FROM CMQUESTS WHERE CMQUTYPE='Quests'")>0) 
			Log.errOut("Failed to delete quest typed 'Quests'.");
		D=DB.DBFetch();
		for(int m=0;m<quests.size();m++)
		{
			Quest Q=(Quest)quests.elementAt(m);
            if(Q.isCopy()) continue;
			try{
				D.update(
				"INSERT INTO CMQUESTS ("
				+"CMQUESID, "
				+"CMQUTYPE, "
				+"CMQSCRPT, "
				+"CMQWINNS "
				+") values ("
				+"'"+Q.name()+"',"
				+"'"+CMClass.classID(Q)+"',"
				+"'"+Q.script()+" ',"
				+"'"+Q.getWinnerStr()+" '"
				+")",0);
			}
			catch(java.sql.SQLException sqle)
			{
				Log.errOut("Quest",sqle);
			}
		}
		if(D!=null) DB.DBDone(D);
	}

}
