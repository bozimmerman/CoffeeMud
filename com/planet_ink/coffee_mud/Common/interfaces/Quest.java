package com.planet_ink.coffee_mud.Common.interfaces;
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

import java.util.Vector;

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
public interface Quest extends Tickable
{
	// the unique name of the quest
	public String name();
	public void setName(String newName);
	
    // the unique start date of the quest
    public String startDate();
    public void setStartDate(String newName);
    
	// the duration, in ticks
	public int duration();
	public void setDuration(int newTicks);
	
	// the rest of the script.  This may be semicolon-separated instructions, 
	// or a LOAD command followed by the quest script path.
	public void setScript(String parm);
	public String script();
	
	// this will execute the quest script.  If the quest is running, it 
	// will call stopQuest first to shut it down.
	public void startQuest();
	
	// this will stop executing of the quest script.  It will clean up 
	// any objects or mobs which may have been loaded, restoring map 
	// mobs to their previous state.  If the quest is autorandom, it 
	// will restart the waiting process
	public void stopQuest();
	
	// these refer the objects designated during the quest
	public int wasQuestMob(String name);
	public int wasQuestItem(String name);
	public int wasQuestObject(String name);
	public boolean isQuestObject(String name, int i);
	public boolean isQuestObject(Environmental E);
	public String getQuestObjectName(int i);
	public String getQuestMobName(int i);
	public String getQuestItemName(int i);
	public Environmental getQuestObject(int i);
	public MOB getQuestMob(int i);
	public Item getQuestItem(int i);
	
    // these methods should only be used WHILE a quest script is running
    // they are called when you want the quest engine to be aware of a
    // a quest-specific object thats being added to the map, so that it
    // can be cleaned up later.  Ditto for abilities, affects, and behaviors.
    public void runtimeRegisterAbility(MOB mob, String abilityID, String parms);
    public void runtimeRegisterObject(Environmental object);
    public void runtimeRegisterEffect(Environmental affected, String abilityID, String parms);
    public void runtimeRegisterBehavior(Environmental behaving, String behaviorID, String parms);
    
	// if the quest has a winner, this is him.
	public void declareWinner(String mobName);
	// retreive the list of previous winners
	public Vector getWinners();
	// retreive the list of previous winners as a string
	public String getWinnerStr();
	// was a previous winner
	public boolean wasWinner(String name);
	// set winners list from a ; delimited string
	public void setWinners(String list);
	public int minPlayers();
	public void setMinPlayers(int players);
	public int runLevel();
	public void setRunLevel(int level);
	public String playerMask();
	public void setPlayerMask(String mask);
	
	// for waiting...
	public int minWait();
	public void setMinWait(int wait);
	public int waitInterval();
	public void setWaitInterval(int wait);
	public void autostartup();
	
	// informational
	public boolean running();
	public boolean stopping();
	public boolean waiting();
	public int ticksRemaining();
	public int minsRemaining();
	public int waitRemaining();
	
	public final static String[] QCODES={"CLASS","NAME","DURATION","WAIT","MINPLAYERS","PLAYERMASK",
										 "RUNLEVEL","DATE","MUDDAY","INTERVAL"};
	public static final String[] SPECIAL_QCODES={"AREA","MOBTYPE","MOBGROUP","ITEMTYPE","LOCALE",
												 "ROOM","MOB","ITEM","ITEMGROUP","ROOMGROUP","LOCALEGROUP"};
	//TODO: add the following:
	// EVIDENCE, ALIBY sets need to figure in, and be able to load from lists dependent on ACTION and/or FACTION
	// some way to add behaviors/props to implement random alibies... thats the last thing I think. -- yes, it is!
	public static final String[] MYSTERY_QCODES={"FACTION","FACTIONGROUP",
												 "AGENT","AGENTGROUP",
												 "ACTION","ACTIONGROUP",
												 "TARGET","TARGETGROUP",
												 "MOTIVE","MOTIVEGROUP",
												 "WHEREHAPPENED","WHEREHAPPENEDGROUP",
												 "WHEREAT","WHEREATGROUP",
												 "WHENHAPPENED","WHENHAPPENEDGROUP",
												 "WHENAT","WHENATGROUP",
												 "TOOL","TOOLGROUP"};
    public static class MysteryData
    {
    	public Vector factionGroup;
    	public Faction faction;
    	public MOB agent;
    	public Vector agentGroup;
    	public Environmental target;
    	public Vector targetGroup;
    	public Environmental tool;
    	public Vector toolGroup;
    	public Room whereHappened;
    	public Vector whereHappenedGroup;
    	public Room whereAt;
    	public Vector whereAtGroup;
    	public String action;
    	public Vector actionGroup;
    	public String motive;
    	public Vector motiveGroup;
    	public TimeClock whenHappened;
    	public Vector whenHappenedGroup;
    	public TimeClock whenAt;
    	public Vector whenAtGroup;
    	public Object getStat(String statName)
    	{
    		int code=-1;
    		for(int i=0;i<MYSTERY_QCODES.length;i++)
    			if(statName.equalsIgnoreCase(MYSTERY_QCODES[i]))
    			{ code=i; break;}
    		switch(code){
	    		case 0: return faction;  case 1: return factionGroup; 
	    		case 2: return agent;  case 3: return agentGroup; 
	    		case 4: return action;  case 5: return actionGroup; 
	    		case 6: return target;  case 7: return targetGroup; 
	    		case 8: return motive;  case 9: return motiveGroup; 
	    		case 10: return whereHappened;  case 11: return whereHappenedGroup; 
	    		case 12: return whenHappened;  case 13: return whenHappenedGroup; 
	    		case 14: return whenAt;  case 15: return whenAtGroup; 
	    		case 16: return tool;  case 17: return toolGroup; 
    		}
    		return null;
    	}
    }
}
