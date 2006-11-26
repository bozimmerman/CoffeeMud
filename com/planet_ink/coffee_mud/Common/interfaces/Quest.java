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

    public void stepQuest();
    
    public void setCopy(boolean truefalse);
    public boolean isCopy();
    public void setSpawnable(boolean truefalse);
    public boolean isSpawnable();
    
    
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
	public Object getQuestObject(String named);
	
    // these methods should only be used WHILE a quest script is running
    // they are called when you want the quest engine to be aware of a
    // a quest-specific object thats being added to the map, so that it
    // can be cleaned up later.  Ditto for abilities, affects, and behaviors.
    public void runtimeRegisterAbility(MOB mob, String abilityID, String parms, boolean give);
    public void runtimeRegisterObject(Environmental object);
    public void runtimeRegisterEffect(Environmental affected, String abilityID, String parms, boolean give);
    public void runtimeRegisterBehavior(Environmental behaving, String behaviorID, String parms, boolean give);
    
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
	
	public boolean isStat(String code);
	public String getStat(String code);
	public void setStat(String code, String val);
	
	public final static String[] QCODES={"CLASS", "NAME", "DURATION", "WAIT", "MINPLAYERS", "PLAYERMASK",
										 "RUNLEVEL", "DATE", "MUDDAY", "INTERVAL","SPAWNABLE"};
	public static final String[] SPECIAL_QCODES={"AREA","MOBTYPE","MOBGROUP","ITEMTYPE","LOCALE",
												 "ROOM","MOB","ITEM","ITEMGROUP","ROOMGROUP","LOCALEGROUP",
												 "ROOMGROUPAROUND","LOCALEGROUPAROUND","PRESERVE"};
	public final static String[] QOBJS={"LOADEDMOBS", "LOADEDITEMS", "AREA", "ROOM", "MOBGROUP", "ITEMGROUP", "ROOMGROUP",
		 								"ITEM", "ENVOBJ", "STUFF", "MOB"};
	
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
}
