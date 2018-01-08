package com.planet_ink.coffee_mud.Common.interfaces;
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

import java.util.List;
import java.util.Map;

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
public interface Quest extends Tickable, CMCommon, Modifiable
{
	/**
	 * Returns the unique name of the quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setName(String)
	 * @return the unique name of the quest
	 */
	@Override
	public String name();

	/**
	 * Sets the unique name of the quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#name()
	 * @param newName the unique name of the quest
	 */
	public void setName(String newName);

	/**
	 * Returns the author of the quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setAuthor(String)
	 * @return the author of the quest
	 */
	public String author();

	/**
	 * Sets the author of the quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#author()
	 * @param newName the author of the quest
	 */
	public void setAuthor(String newName);

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
	 * @param showErrors true to report file errors, false otherwise
	 * @return true
	 */
	public boolean setScript(String parm, boolean showErrors);

	/**
	 * Accepts a pre-parsed quest script and extracts certain
	 * non-iterative variables, such as the quest name and
	 * similar variables.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param script the parsed quest script
	 * @param startAtLine which line of the script to start at
	 */
	public void setVars(List<?> script, int startAtLine);

	/**
	 * Returns the unparsed quest script as a single happy string.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setScript(String, boolean)
	 * @return the unparsed quest script as a single happy string.
	 */
	public String script();

	/**
	 * This will execute the quest script.  If the quest is running, it
	 * will call stopQuest first to shut it down.  It will spawn its
	 * subquests and subsections if necessary.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#resetQuest(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stepQuest()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stopQuest()
	 * @return whether the quest was successfully started
	 */
	public boolean startQuest();

	/**
	 * this will stop executing of the quest script.  It will clean up
	 * any objects or mobs which may have been loaded, restoring map
	 * mobs to their previous state.  If the quest is autorandom, it
	 * will restart the waiting process
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startQuest()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stepQuest()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#resetQuest(int)
	 */
	public void stopQuest();

	/**
	 * this will stop executing of the quest script.  It will clean up
	 * any objects or mobs which may have been loaded, restoring map
	 * mobs to their previous state.  It will then enter a stopped-paused
	 * state for the given ticks.  Any start failures after that
	 * will cause the pause time to be doubled before the next try.
	 * @param firstPauseTicks ticks to remain in stopped state before restarting
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startQuest()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stepQuest()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stopQuest()
	 */
	public void resetQuest(int firstPauseTicks);

	/**
	 * If any files are embedded and cached inside this quest
	 * script, this method will clear them from resources and
	 * memory.
	 */
	public void internalQuestDelete();

	/**
	 * This method is called when a quest is done with a
	 * particular step in a multi-step quest.  This method
	 * will clean up any objects from the current step or
	 * previous steps and attempt to start up the next
	 * step in the quest. If there are no more steps, or
	 * the quest is only 1 step, stopQuest() will be called.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startQuest()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stopQuest()
	 * @return true if another step was started, false otherwise
	 */
	public boolean stepQuest();

	/**
	 * A dormant state is the state where a quest is no longer running, but
	 * is not, or has not yet, been scheduled to wait for another run time.
	 * This may result in a quest being deleted if it was a spawned temporary
	 * quest.
	 * @return true if it is in a dormant state, or false if quest was deleted
	 */
	public boolean enterDormantState();

	/**
	 * Sets whether this quest object is a spawned copy
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#isCopy()
	 * @param truefalse true if this quest object is a spawned copy
	 */
	public void setCopy(boolean truefalse);

	/**
	 * Returns whether this quest object is a spawned copy
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setCopy(boolean)
	 * @return whether this quest object is a spawned copy
	 */
	public boolean isCopy();

	/**
	 * Sets the flag denoting whether this quest spawns new ones
	 * from its several steps and if so, by what method.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_ANY
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_FIRST
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_NO
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_DESCS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getSpawn()
	 * @param spawnFlag the quest spawn flag info
	 */
	public void setSpawn(int spawnFlag);

	/**
	 * Returns the flag denoting whether this quest spawns new ones
	 * from its several steps and if so, by what method.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_ANY
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_FIRST
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_NO
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#SPAWN_DESCS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setSpawn(int)
	 * @return the quest spawn flag info
	 */
	public int getSpawn();

	/**
	 * Quest scripts can have files of various sorts embedded
	 * in them.  This method will return the text of any such
	 * files of the given name, if they were embedded, or if
	 * not, it will attempt to open the file in the filesystem
	 * and return that one instead.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param named the name of the resource path file to return
	 * @param showErrors true to report file errors, false otherwise
	 * @return the text of the file, if found.
	 */
	public StringBuffer getResourceFileData(String named, boolean showErrors);

	/**
	 * Returns the index of a room, mob, or item of the given name
	 * in use by this quest.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#isObjectInUse(Environmental)
	 * @param name the given name
	 * @return the index of a room, mob, or item of the given name
	 */
	public int getObjectInUseIndex(String name);

	/**
	 * Returns whether the exact given object is in use by this quest.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getObjectInUseIndex(String)
	 * @param E the object to check
	 * @return true if its in use, false otherwise
	 */
	public boolean isObjectInUse(Environmental E);

	/**
	 * From the given official quest variable name, it derives
	 * either an object or a vector of objects that reflect it.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#QOBJS
	 * @param named the code to return a string, object, or vector for
	 * @return a string, mob, item, room, vector, etc..
	 */
	public Object getDesignatedObject(String named);

	/**
	 * Returns the index of a mob of the given name in use by this quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestMobName(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestMob(int)
	 * @param name the given name
	 * @return the index of a mob of the given name in use by this quest
	 */
	public int getQuestMobIndex(String name);

	/**
	 * Returns the mob in use by this quest at the given index
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestMobName(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestMobIndex(String)
	 * @param i the index
	 * @return the mob in use by this quest at the given index
	 */
	public MOB getQuestMob(int i);

	/**
	 * Returns the name of the mob in use by this quest at the given index
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestMob(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestMobIndex(String)
	 * @param i the index
	 * @return the name of the mob in use by this quest at the given index
	 */
	public String getQuestMobName(int i);

	/**
	 * Returns the index of a item of the given name in use by this quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestItem(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestItemName(int)
	 * @param name the given name
	 * @return the index of a item of the given name in use by this quest
	 */
	public int getQuestItemIndex(String name);

	/**
	 * Returns the item in use by this quest at the given index
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestItemIndex(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestItemName(int)
	 * @param i the index
	 * @return the item in use by this quest at the given index
	 */
	public Item getQuestItem(int i);

	/**
	 * Returns the name of the item in use by this quest at the given index
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestItem(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestItemIndex(String)
	 * @param i the index
	 * @return the name of the item in use by this quest at the given index
	 */
	public String getQuestItemName(int i);

	/**
	 * Returns the index of a room of the given id in use by this quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestRoom(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestRoomID(int)
	 * @param roomID the given room id
	 * @return the index of a room of the given id in use by this quest
	 */
	public int getQuestRoomIndex(String roomID);

	/**
	 * Returns the room in use by this quest at the given index
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestRoomIndex(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestRoomID(int)
	 * @param i the index
	 * @return the room in use by this quest at the given index
	 */
	public Room getQuestRoom(int i);

	/**
	 * Returns the id of the room in use by this quest at the given index
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestRoom(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getQuestRoomIndex(String)
	 * @param i the index
	 * @return the id of the room in use by this quest at the given index
	 */
	public String getQuestRoomID(int i);

	/**
	 * they are called when you want the quest engine to be aware of a
	 * a quest-specific object thats being added to the map, so that it
	 * can be cleaned up later.  Ditto for abilities, affects, and behaviors.
	 * this method should only be used WHILE a quest script is being interpreted
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterBehavior(PhysicalAgent, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterEffect(PhysicalAgent, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterObject(PhysicalAgent)
	 * @param mob the mob receiving the ability
	 * @param abilityID the id of the ability
	 * @param parms any ability parameters
	 * @param give false to remove this ability, true to replace an existing one
	 */
	public void runtimeRegisterAbility(MOB mob, String abilityID, String parms, boolean give);

	/**
	 * Called when you want the quest engine to be aware of a quest specific object
	 * that is being added to the map, so that it can be cleaned up later.
	 * this method should only be used WHILE a quest script is being interpreted
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterAbility(MOB, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterBehavior(PhysicalAgent, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterEffect(PhysicalAgent, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest
	 * @param P the object added to the map
	 */
	public void runtimeRegisterObject(PhysicalAgent P);

	/**
	 * Called when you want the quest engine to be aware of a quest specific object
	 * that is being added to the map, so that it can be cleaned up later.  This is
	 * called to add an effect to the given object.
	 * this method should only be used WHILE a quest script is being interpreted
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterAbility(MOB, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterBehavior(PhysicalAgent, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterObject(PhysicalAgent)
	 * @param affected the object receiving the effect
	 * @param abilityID the id of the effect
	 * @param parms any effect parameters
	 * @param give false to remove this effect, true to replace an existing one
	 */
	public void runtimeRegisterEffect(PhysicalAgent affected, String abilityID, String parms, boolean give);

	/**
	 * Called when you want the quest engine to be aware of a quest specific object
	 * that is being added to the map, so that it can be cleaned up later.  This is
	 * called to add a behavior to the given object.
	 * this method should only be used WHILE a quest script is being interpreted
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterAbility(MOB, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterEffect(PhysicalAgent, String, String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runtimeRegisterObject(PhysicalAgent)
	 * @param behaving the object receiving the behavior
	 * @param behaviorID the id of the behavior
	 * @param parms any behavior parameters
	 * @param give false to remove this behavior, true to replace an existing one
	 */
	public void runtimeRegisterBehavior(PhysicalAgent behaving, String behaviorID, String parms, boolean give);

	/**
	 * Registers the given player name as having won this quest.  The name
	 * may be prefixed with a "-" to undeclare the winner (for player deletes).
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinners()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinnerStr()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#wasWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWinners(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#whenLastWon(String)
	 * @param mobName the player name
	 */
	public void declareWinner(String mobName);

	/**
	 * Returns the names of all the winners of this quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#declareWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinnerStr()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#wasWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWinners(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#whenLastWon(String)
	 * @return the names of all the winners of this quest and last time won
	 */
	public Map<String, Long> getWinners();

	/**
	 * Returns a semicolon delimited string of all the winners of this quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#declareWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinners()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#wasWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWinners(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#whenLastWon(String)
	 * @return a semicolon delimited string of all the winners of this quest
	 */
	public String getWinnerStr();

	/**
	 * Returns whether a player of the given name has won this quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#declareWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinners()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinnerStr()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWinners(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#whenLastWon(String)
	 * @param name the player name
	 * @return true if a player of the given name has won this quest
	 */
	public boolean wasWinner(String name);

	/**
	 * Returns when a player of the given name last won this quest or null
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#declareWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinners()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinnerStr()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWinners(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#wasWinner(String)
	 * @param name the player name
	 * @return true if a player of the given name has won this quest
	 */
	public Long whenLastWon(String name);
	
	/**
	 * Sets the list of player names that have won this quest
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#declareWinner(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinners()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#getWinnerStr()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#wasWinner(String)
	 * @param list a semicolon delimtied list of player names
	 */
	public void setWinners(String list);

	/**
	 * The minimum number of players matching player criteria required before
	 * this quest will start
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setMinPlayers(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#playerMask()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setPlayerMask(String)
	 * @return minimum number of players matching player criteria required
	 */
	public int minPlayers();

	/**
	 * Sets minimum number of players matching player criteria required before
	 * this quest will start
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#minPlayers()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#playerMask()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setPlayerMask(String)
	 * @param players minimum number of players matching player criteria required
	 */
	public void setMinPlayers(int players);

	/**
	 * Returns the run level. -1 means runs always, otherwise,
	 * this quest will always defer to running quests of equal
	 * or lower run level.  Higher, therefore, is weaker.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setRunLevel(int)
	 * @return the run level. -1 means runs always
	 */
	public int runLevel();

	/**
	 * Sets the run level. -1 means runs always, otherwise,
	 * this quest will always defer to running quests of equal
	 * or lower run level.  Higher, therefore, is weaker.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#runLevel()
	 * @param level the run level. -1 means runs always
	 */
	public void setRunLevel(int level);

	/**
	 * Returns the zappermask that determines who counts as an
	 * elligible player for the purposes of the minPlayer setting.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setMinPlayers(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#minPlayers()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setPlayerMask(String)
	 * @return the zappermask that determines who counts as a player
	 */
	public String playerMask();

	/**
	 * Sets the zappermask that determines who counts as an
	 * elligible player for the purposes of the minPlayer setting.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setMinPlayers(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#minPlayers()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#playerMask()
	 * @param mask the zappermask that determines who counts as a player
	 */
	public void setPlayerMask(String mask);

	/**
	 * Returns the minimum number of ticks between attempts to run this quest.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setMinWait(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#waitInterval()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWaitInterval(int)
	 * @return the minimum number of ticks between attempts to run this quest.
	 */
	public int minWait();

	/**
	 * Sets the minimum number of ticks between attempts to run this quest.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#minWait()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#waitInterval()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWaitInterval(int)
	 * @param wait the minimum number of ticks between attempts to run this quest.
	 */
	public void setMinWait(int wait);

	/**
	 * Returns the maximum ticks, above the minimum wait, that must go by
	 * before an attempt to run a quest.  This is therefore, the random part.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setMinWait(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#minWait()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setWaitInterval(int)
	 * @return the maximum ticks, above the minimum wait, that must go by
	 */
	public int waitInterval();

	/**
	 * Sets the maximum ticks, above the minimum wait, that must go by
	 * before an attempt to run a quest.  This is therefore, the random part.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#setMinWait(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#minWait()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#waitInterval()
	 * @param wait the maximum ticks, above the minimum wait, that must go by
	 */
	public void setWaitInterval(int wait);

	/**
	 * After a quest is added to the list of quests, this method is
	 * called to put the quest into its initial wait state, and get
	 * it thread time.
	 */
	public void autostartup();

	/**
	 * Returns whether this quest is in a running state
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#suspended()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#waiting()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startQuest()
	 * @return true if the quest is running, false if stopped
	 */
	public boolean running();

	/**
	 * Returns whether this quest is in a midway stopping state
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#suspended()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#waiting()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#running()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stopQuest()
	 * @return true if the quest is in the processess of stopping
	 */
	public boolean stopping();

	/**
	 * Returns whether this quest is in a wait state between runs
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#suspended()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#waiting()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#running()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#stopQuest()
	 * @return true if this quest is in a wait state between runs
	 */
	public boolean waiting();

	/**
	 * Returns the number of ticks before this quest will go from
	 * a running state to a stopped state.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#minsRemaining()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startQuest()
	 * @return the numer of ticks the quest will keep running
	 */
	public int ticksRemaining();

	/**
	 * Returns the number of minutes before this quest will go from
	 * a running state to a stopped state.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#ticksRemaining()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#startQuest()
	 * @return the numer of minutes the quest will keep running
	 */
	public int minsRemaining();

	/**
	 * Returns the number of ticks before this quest will attempt to start.
	 * A number greater than or equal to 0 means the quest is currently in a stopped state.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#resetWaitRemaining(long)
	 * @return the number of ticks before this quest will attempt to start.
	 */
	public int waitRemaining();

	/**
	 * Sets the number of ticks before this quest will attempt to start.
	 * A number greater than or equal to 0 means the quest is currently in a stopped state.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#waitRemaining()
	 * @param minusEllapsed the number of miliseconds already ellapsed before wait began
	 * @return true if the quest is successfully put into a non-running wait state
	 */
	public boolean resetWaitRemaining(long minusEllapsed);

	/**
	 * Returns flag bitmap
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#FLAG_SUSPENDED
	 * @return the flag bitmap
	 */
	public long getFlags();

	/**
	 * Sets the flag bitmap
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Quest#FLAG_SUSPENDED
	 * @param flags the flag bitmap
	 */
	public void setFlags(long flags);

	/** A quest spawn flag denoting that this quest does not spawn its steps */
	public final static int SPAWN_NO=0;
	/** A quest spawn flag denoting that this quest spawns only its first step */
	public final static int SPAWN_FIRST=1;
	/** A quest spawn flag denoting that this quest attempts to spawn every step at once */
	public final static int SPAWN_ANY=2;
	/** Descriptions of the several quest step spawn flags */
	public final static String[] SPAWN_DESCS={"FALSE","TRUE","ALL"};

	/** A quest flag @see {@link Quest#getFlags()} */
	public final static int FLAG_SUSPENDED=1;

	/** The list of BASIC non-iterative variable codes that pertain to a quest object */
	public final static String[] QCODES={"CLASS", "NAME", "DURATION", "WAIT", "MINPLAYERS", "PLAYERMASK",
										 "RUNLEVEL", "DATE", "MUDDAY", "INTERVAL","SPAWNABLE", "DISPLAY",
										 "INSTRUCTIONS", "PERSISTANCE", "AUTHOR"};
	/** The list of basic quest objects defined in an iterative fashion during quest script execution */
	public final static String[] QOBJS={"LOADEDMOBS", "LOADEDITEMS", "AREA", "ROOM", "MOBGROUP", "ITEMGROUP", "ROOMGROUP",
		 								"ITEM", "ENVOBJ", "STUFF", "MOB"};
	/** The list of basic mystery quest objects defined in an iterative fashion during quest script execution */
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
	/** the list of room-related mystery quest objects defined in an iterative fashion during quest script execution */
	public static final String[] ROOM_REFERENCE_QCODES={"WHEREHAPPENED","WHEREHAPPENEDGROUP",
														"WHEREAT","WHEREATGROUP",
														"ROOM","ROOMGROUP"
	};
	/**
	 * A Quest script contains strings, and options, which
	 * are mini-quest scripts of strings.
	 * @author Bo Zimmermanimmerman
	 */
	public static class QuestScript extends SVector<String>
	{
		private static final long serialVersionUID = 6627484220915485083L;
		public List<QuestScript> options = new SVector<QuestScript>();
	}
}
