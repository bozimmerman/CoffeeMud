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
   Copyright 2000-2008 Bo Zimmerman

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
public interface Quest extends Tickable, CMCommon, CMModifiable
{
	// the unique name of the quest
	public String name();
	public void setName(String newName);
	
    // the display name of the quest
    public String displayName();
    public void setDisplayName(String newName);
    
    // the unique start date of the quest
    public String startDate();
    public void setStartDate(String newName);
    public void setStartMudDate(String newName);
    
	// the duration, in ticks
	public int duration();
	public void setDuration(int newTicks);
	
	// whether this quest object is suspended
	public boolean suspended();
	public void setSuspended(boolean truefalse);
	
	// the rest of the script.  This may be semicolon-separated instructions, 
	// or a LOAD command followed by the quest script path.
	public void setScript(String parm);
    public void setVars(Vector script, int startAtLine);
	public String script();
    public static final String FILE_XML_BOUNDARY="<?xml version=\"1.0\"?>";
	
	// this will execute the quest script.  If the quest is running, it 
	// will call stopQuest first to shut it down.
	public boolean startQuest();
    
    public boolean startQuestOnTime();
	
	// this will stop executing of the quest script.  It will clean up 
	// any objects or mobs which may have been loaded, restoring map 
	// mobs to their previous state.  If the quest is autorandom, it 
	// will restart the waiting process
	public void stopQuest();

    public void internalQuestDelete();
    
    public boolean stepQuest();

    public boolean enterDormantState();
    
    public void setCopy(boolean truefalse);
    public boolean isCopy();
    
    public void setSpawn(int spawnFlag);
    public int getSpawn();
    
    public StringBuffer getResourceFileData(String named);
    
	// these refer the objects designated during the quest
    public int wasObjectInUse(String name);
	public boolean isObjectInUse(Environmental E);
    public Object getDesignatedObject(String named);
    
    public int wasQuestMob(String name);
    public MOB getQuestMob(int i);
	public String getQuestMobName(int i);
    public int wasQuestItem(String name);
    public Item getQuestItem(int i);
	public String getQuestItemName(int i);
    public int wasQuestRoom(String roomID);
    public Room getQuestRoom(int i);
    public String getQuestRoomID(int i);
	
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
    public boolean resetWaitRemaining(long minusEllapsed);
	
    public final static int SPAWN_NO=0;
    public final static int SPAWN_FIRST=1;
    public final static int SPAWN_ANY=2;
    public final static String[] SPAWN_DESCS={"FALSE","TRUE","ALL"};
    
	public boolean isStat(String code);
	
	public final static String[] QCODES={"CLASS", "NAME", "DURATION", "WAIT", "MINPLAYERS", "PLAYERMASK",
										 "RUNLEVEL", "DATE", "MUDDAY", "INTERVAL","SPAWNABLE", "DISPLAY", 
                                         "INSTRUCTIONS"};
	public static final String[] SPECIAL_QCODES={"AREA","MOBTYPE","MOBGROUP","ITEMTYPE","LOCALE",
												 "ROOM","MOB","ITEM","ITEMGROUP","ROOMGROUP","LOCALEGROUP",
												 "ROOMGROUPAROUND","LOCALEGROUPAROUND","PRESERVE",
                                                 "AREAGROUP"};
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
	public static final String[] ROOM_REFERENCE_QCODES={"WHEREHAPPENED","WHEREHAPPENEDGROUP",
                                                		"WHEREAT","WHEREATGROUP",
                                                		"ROOM","ROMGROUP"
	};
}
