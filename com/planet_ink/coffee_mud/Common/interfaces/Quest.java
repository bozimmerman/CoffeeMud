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
/**
 * A quest object manages the details and text for a single
 * descriptive script that is scheduled and, when directed,
 * spawns, creates, watches, shuts down, and cleans up the various
 * objects, subsidiary quests, and existing objects modifications
 * related to this Quest.
 * 
 * To the user, a quest is a task the user must complete for 
 * reward.  To the Archon, a quest is something that adds 
 * content to an area at particular times, or under particular
 * circumstances.
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.QuestManager
 */
public interface Quest extends Tickable, CMCommon, CMModifiable
{
    /**
     * Returns the unique name of the quest
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setName(String)
     * @return the unique name of the quest
     */
	public String name();
	
	/**
	 * Sets the unique name of the quest
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#name()
	 * @param newName the unique name of the quest
	 */
	public void setName(String newName);
	
	/**
	 * Returns the friendly display name of the quest
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setDisplayName(String)
	 * @return the friendly display name of the quest
	 */
    public String displayName();
    
    /**
     * Sets the friendly display name of the quest
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#displayName()
     * @param newName the friendly display name of the quest
     */
    public void setDisplayName(String newName);
    
    /**
     * Returns the unique start date of the quest.  The format
     * is either MONTH-DAY for real life dates, or 
     * MUDDAY MONTH-DAY for mudday based dates. 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setStartDate(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setStartMudDate(String)
     * @return the unique formatted start date of the quest
     */
    public String startDate();
    
    /**
     * Sets the real-life start date of this quest. The format
     * is MONTH-DAY.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startDate()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setStartMudDate(String)
     * @param newName the real-life start date of this quest
     */
    public void setStartDate(String newName);
    
    /**
     * Sets the in-game mud start date of this quest. The format
     * is MONTH-DAY.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startDate()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setStartDate(String)
     * @param newName the in-game mud start date of this quest
     */
    public void setStartMudDate(String newName);
    
	/**
	 * Returns the duration, in ticks of this quest. A value of
	 * 0 means the quest runs indefinitely.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setDuration(int)
	 * @return the duration, in ticks, of this quest
	 */
	public int duration();
	
	/**
     * Sets the duration, in ticks of this quest. A value of
     * 0 means the quest runs indefinitely.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#duration()
	 * @param newTicks the duration, in ticks, of this quest
	 */
	public void setDuration(int newTicks);
	
	/**
	 * Returns whether this quest object is suspended.  A
	 * suspended quest is always in a stopped state.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setSuspended(boolean)
	 * @return true if this quest object is suspended
	 */
	public boolean suspended();
	
	/**
     * Sets whether this quest object is suspended.  A
     * suspended quest should always in a stopped state.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#suspended()
	 * @param truefalse true if this quest object is suspended
	 */
	public void setSuspended(boolean truefalse);
	
	/**
	 * Sets the quest script.  This may be semicolon-separated 
	 * instructions, or a LOAD command followed by the quest 
	 * script path.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#script()
	 * @param parm the actual quest script
	 */
	public void setScript(String parm);
	
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param script
	 * @param startAtLine
	 */
    public void setVars(Vector script, int startAtLine);
    
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setScript(String)
     * @return
     */
	public String script();
	
	/**
	 * this will execute the quest script.  If the quest is running, it
	 * will call stopQuest first to shut it down.  
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return whether the quest was successfully started
	 */
	public boolean startQuest();
    
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
    public boolean startQuestOnTime();
	
	/**
     * this will stop executing of the quest script.  It will clean up 
     * any objects or mobs which may have been loaded, restoring map 
     * mobs to their previous state.  If the quest is autorandom, it 
     * will restart the waiting process
	 */
	public void stopQuest();

	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 */
    public void internalQuestDelete();
    
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @return
     */
    public boolean stepQuest();

    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @return
     */
    public boolean enterDormantState();
    
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param truefalse
     */
    public void setCopy(boolean truefalse);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @return
     */
    public boolean isCopy();
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param spawnFlag
     */
    public void setSpawn(int spawnFlag);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @return
     */
    public int getSpawn();
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param named
     * @return
     */
    public StringBuffer getResourceFileData(String named);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param name
     * @return
     */
    public int wasObjectInUse(String name);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param E
     * @return
     */
	public boolean isObjectInUse(Environmental E);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param named
	 * @return
	 */
    public Object getDesignatedObject(String named);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param name
     * @return
     */
    public int wasQuestMob(String name);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param i
     * @return
     */
    public MOB getQuestMob(int i);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param i
     * @return
     */
	public String getQuestMobName(int i);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param name
	 * @return
	 */
    public int wasQuestItem(String name);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param i
     * @return
     */
    public Item getQuestItem(int i);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param i
     * @return
     */
	public String getQuestItemName(int i);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param roomID
	 * @return
	 */
    public int wasQuestRoom(String roomID);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param i
     * @return
     */
    public Room getQuestRoom(int i);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param i
     * @return
     */
    public String getQuestRoomID(int i);
	
    /**
     * they are called when you want the quest engine to be aware of a
     * a quest-specific object thats being added to the map, so that it
     * can be cleaned up later.  Ditto for abilities, affects, and behaviors.
     * this method should only be used WHILE a quest script is being interpreted
     * @param mob the mob receiving the ability
     * @param abilityID the id of the ability
     * @param parms any ability parameters
     * @param give false to remove this ability, true to replace an existing one
     */
    public void runtimeRegisterAbility(MOB mob, String abilityID, String parms, boolean give);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param object
     */
    public void runtimeRegisterObject(Environmental object);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param affected
     * @param abilityID
     * @param parms
     * @param give
     */
    public void runtimeRegisterEffect(Environmental affected, String abilityID, String parms, boolean give);
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param behaving
     * @param behaviorID
     * @param parms
     * @param give
     */
    public void runtimeRegisterBehavior(Environmental behaving, String behaviorID, String parms, boolean give);
    
    /**
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
     * @param mobName
     */
	public void declareWinner(String mobName);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public Vector getWinners();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public String getWinnerStr();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param name
	 * @return
	 */
	public boolean wasWinner(String name);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param list
	 */
	public void setWinners(String list);
	
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public int minPlayers();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param players
	 */
	public void setMinPlayers(int players);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public int runLevel();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param level
	 */
	public void setRunLevel(int level);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public String playerMask();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param mask
	 */
	public void setPlayerMask(String mask);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public int minWait();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param wait
	 */
	public void setMinWait(int wait);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public int waitInterval();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param wait
	 */
	public void setWaitInterval(int wait);
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 */
	public void autostartup();
	
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public boolean running();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public boolean stopping();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public boolean waiting();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public int ticksRemaining();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public int minsRemaining();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @return
	 */
	public int waitRemaining();
	/**
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param minusEllapsed
	 * @return
	 */
    public boolean resetWaitRemaining(long minusEllapsed);
	/** */
    public final static int SPAWN_NO=0;
    /** */
    public final static int SPAWN_FIRST=1;
    /** */
    public final static int SPAWN_ANY=2;
    /** */
    public final static String[] SPAWN_DESCS={"FALSE","TRUE","ALL"};
    /** */
	public boolean isStat(String code);
    /** */
	public final static String[] QCODES={"CLASS", "NAME", "DURATION", "WAIT", "MINPLAYERS", "PLAYERMASK",
										 "RUNLEVEL", "DATE", "MUDDAY", "INTERVAL","SPAWNABLE", "DISPLAY", 
                                         "INSTRUCTIONS"};
    /** */
	public static final String[] SPECIAL_QCODES={"AREA","MOBTYPE","MOBGROUP","ITEMTYPE","LOCALE",
												 "ROOM","MOB","ITEM","ITEMGROUP","ROOMGROUP","LOCALEGROUP",
												 "ROOMGROUPAROUND","LOCALEGROUPAROUND","PRESERVE",
                                                 "AREAGROUP"};
    /** */
	public final static String[] QOBJS={"LOADEDMOBS", "LOADEDITEMS", "AREA", "ROOM", "MOBGROUP", "ITEMGROUP", "ROOMGROUP",
		 								"ITEM", "ENVOBJ", "STUFF", "MOB"};
    /** */
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
    /** */
	public static final String[] ROOM_REFERENCE_QCODES={"WHEREHAPPENED","WHEREHAPPENEDGROUP",
                                                		"WHEREAT","WHEREATGROUP",
                                                		"ROOM","ROOMGROUP"
	};
}
