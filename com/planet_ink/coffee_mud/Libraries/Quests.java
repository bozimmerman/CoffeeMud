package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import java.util.*;

import org.mozilla.javascript.*;


/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class Quests extends StdLibrary implements QuestManager
{
	public String ID(){return "Quests";}
	protected Vector quests=new Vector();
    
	public Quest objectInUse(Environmental E)
	{
		if(E==null) return null;
		for(int q=0;q<numQuests();q++)
		{
			Quest Q=fetchQuest(q);
			if(Q.isQuestObject(E)) return Q;
		}
		return null;
	}

	public int numQuests(){return quests.size();}
	public Quest fetchQuest(int i){
		try{
			return (Quest)quests.elementAt(i);
		}catch(Exception e){}
		return null;
	}
	public Quest fetchQuest(String qname)
	{
		for(int i=0;i<numQuests();i++)
		{
			Quest Q=fetchQuest(i);
			if(Q.name().equalsIgnoreCase(qname))
				return Q;
		}
		return null;
	}
	public void addQuest(Quest Q)
	{
		if((fetchQuest(Q.name())==null)
		&&(!quests.contains(Q)))
		{
			quests.addElement(Q);
			Q.autostartup();
		}
	}
	public void shutdown()
	{
		for(int i=numQuests();i>=0;i--)
		{
			Quest Q=fetchQuest(i);
			delQuest(Q);
		}
		quests.clear();
	}
	public void delQuest(Quest Q)
	{
		if(quests.contains(Q))
		{
			Q.stopQuest();
			CMLib.threads().deleteTick(Q,Tickable.TICKID_QUEST);
			quests.removeElement(Q);
		}
	}
	public void save()
	{
		CMLib.database().DBUpdateQuests(quests);
	}
    
}
