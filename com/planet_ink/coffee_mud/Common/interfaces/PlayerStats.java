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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.ExpertiseAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
/**
 * Accessible from any PC mob object, PlayerStats are
 * the repository for all manner of player specific
 * mob values.
 *
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#playerStats()
 */
public interface PlayerStats extends CMCommon, Modifiable, AccountStats, Contingent
{
	/**
	 * The time, in milis since 1970, that the player was last saved.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setLastUpdated(long)
	 *
	 * @return the time, in milis since 1970, that the player was last saved.
	 */
	@Override
	public long getLastUpdated();

	/**
	 * Sets the time, in milis since 1970, that the player was last saved.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getLastUpdated()
	 *
	 * @param time the time, in milis since 1970, that the player was last saved.
	 */
	@Override
	public void setLastUpdated(long time);

	/**
	 * The time, in milis since 1970, that the player gained the given level
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setLeveledDateTime(int, long, Room)
	 *
	 * @param level the level to check for
	 * @return the time, in milis since 1970, that the player gained the given level
	 */
	public long leveledDateTime(int level);

	/**
	 * The roomID that the player gained the given level
	 * 
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setLeveledDateTime(int, long, Room)
	 *
	 * @param level  the level to check for
	 * @return roomID that the player gained the given level
	 */
	public String leveledRoomID(int level);

	/**
	 * The number of minutes played when the player gained the given level
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setLeveledDateTime(int, long, Room)
	 *
	 * @param level the level to check for
	 * @return the minutes played before the player gained the given level
	 */
	public long leveledMinutesPlayed(int level);

	/**
	 * Notifies the player records that, at the moment this method was called,
	 * the player gained the given level.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#leveledDateTime(int)
	 *
	 * @param level the level to set up
	 * @param ageHours the hours played at this point
	 * @param R the room in which the level was gained
	 */
	public void setLeveledDateTime(int level, long ageHours, Room R);

	/**
	 * Returns a bitmask of channels turned on/off. (32 channels supported)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setChannelMask(int)
	 *
	 * @return a bitmask of channels turned on/off. (1=off)
	 */
	public int getChannelMask();

	/**
	 * Sets the bitmask of channels turned on/off. (32 channels supported)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setChannelMask(int)
	 *
	 * @param newMask the bitmask of channels turned on/off. (1=off)
	 */
	public void setChannelMask(int newMask);

	/**
	 * Returns a custom coded string detailing the changes to the official
	 * color code that apply to this player only.  The format is the Color
	 * Code Letter (the one after the ^ character) followed by the ansi color
	 * from the basic set, followed by a # character, repeated.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setColorStr(String)
	 *
	 * @return a custom coded color string
	 */
	public String getColorStr();

	/**
	 * Sets a custom coded string detailing the changes to the official
	 * color code that apply to this player only.  The format is the Color
	 * Code Letter (the one after the ^ character) followed by the ansi color
	 * from the basic set, followed by a # character, repeated.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getColorStr()
	 *
	 * @param color a custom coded color string
	 */
	public void setColorStr(String color);

	/**
	 * Gets the saved pose string for players.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setSavedPose(String)
	 *
	 * @return a saved pose string
	 */
	public String getSavedPose();

	/**
	 * Sets the saved pose string for players.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getSavedPose()
	 *
	 * @param msg a saved pose string
	 */
	public void setSavedPose(String msg);

	/**
	 * Returns the word wrap column number for this player, or 0.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setWrap(int)
	 *
	 * @return the word wrap column number for this player, or 0.
	 */
	public int getWrap();

	/**
	 * Sets the word wrap column number for this player, or 0.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getWrap()
	 *
	 * @param newWrap the word wrap column number for this player, or 0.
	 */
	public void setWrap(int newWrap);

	/**
	 * Returns the page break row for this player, or 0.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPageBreak(int)
	 *
	 * @return the page break row for this player, or 0.
	 */
	public int getPageBreak();

	/**
	 * Sets the page break row for this player, or 0.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getPageBreak()
	 *
	 * @param newBreak the page break row for this player, or 0.
	 */
	public void setPageBreak(int newBreak);

	/**
	 * Returns the custom prompt, an encoded string, for this player.  "" means
	 * default is used.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPrompt(String)
	 *
	 * @return the custom prompt, an encoded string, or ""
	 */
	public String getPrompt();

	/**
	 * Sets the custom prompt, an encoded string, for this player.  "" means
	 * default is used.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getPrompt()
	 *
	 * @param prompt the custom prompt, an encoded string, or ""
	 */
	public void setPrompt(String prompt);

	/**
	 * Returns a list of modifiable title definitions.  These are things
	 * like *, the bunny slayer and such.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getActiveTitle()
	 *
	 * @return  a list of modifiable title definitions
	 */
	public List<String> getTitles();

	/**
	 * Returns which of the player available titles is currently being used by
	 * this player.  Its a string like *, the bunny slayer
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTitles()
	 *
	 * @return a title being used by this player
	 */
	public String getActiveTitle();

	/**
	 * Returns a List of the last few string messages sent and received to and
	 * from this player.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addTellStack(String)
	 *
	 * @return a List of strings, the last few tell messages
	 */
	public List<String> getTellStack();

	/**
	 * Adds a new string message to the tell stack.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTellStack()
	 *
	 * @param msg the new message for the tell stack.
	 */
	public void addTellStack(String msg);

	/**
	 * Returns a List of the last few string messages sent and received to and
	 * from this players group.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addGTellStack(String)
	 *
	 * @return a List of strings, the last few gtell messages
	 */
	public List<String> getGTellStack();

	/**
	 * Adds a new string message to the gtell stack.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getGTellStack()
	 *
	 * @param msg the new message for the gtell stack.
	 */
	public void addGTellStack(String msg);

	/**
	 * For player with the GOTO command, this is the message seen by all when
	 * the player arrives from using GOTO.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getPoofOut()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
	 *
	 * @return the poof-in GOTO message
	 */
	public String getPoofIn();

	/**
	 * For player with the GOTO command, this is the message seen by all when
	 * the player leaves using GOTO.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getPoofIn()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
	 *
	 * @return the poof-out GOTO message
	 */
	public String getPoofOut();

	/**
	 * For player with the TRANSFER command, this is the message seen by all when
	 * the player arrives from using TRANSFER.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTranPoofOut()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
	 *
	 * @return the poof-in TRANSFER message
	 */
	public String getTranPoofIn();

	/**
	 * For player with the TRANSFER command, this is the message seen by all when
	 * the player leaves using TRANSFER.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTranPoofIn()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
	 *
	 * @return the poof-out TRANSFER message
	 */
	public String getTranPoofOut();

	/**
	 * For players with either the GOTO or TRANSFER command, this will set the
	 * various messages seen when coming and going using either of those commands.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTranPoofIn()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTranPoofOut()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getPoofIn()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getPoofOut()
	 *
	 * @param poofIn the msg seen when entering a room using GOTO
	 * @param poofOut the msg seen when leaving a room using TRANSFER
	 * @param tranPoofIn the msg seen when entering a room using GOTO
	 * @param tranPoofOut the msg seen when leaving a room using TRANSFER
	 */
	public void setPoofs(String poofIn, String poofOut, String tranPoofIn, String tranPoofOut);

	/**
	 * For players with the ANNOUNCE command, this is the message used to
	 * prefix the announcements proclaimed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setAnnounceMessage(String)
	 *
	 * @return prefix to announcements
	 */
	public String getAnnounceMessage();

	/**
	 * For players with the ANNOUNCE command, this sets the message used to
	 * prefix the announcements proclaimed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAnnounceMessage()
	 *
	 * @param msg prefix to announcements
	 */
	public void setAnnounceMessage(String msg);

	/**
	 * Returns the last MOB player who sent this player a private instant message.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setReplyTo(MOB, int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyType()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyToTime()
	 *
	 * @return the last MOB player who sent this player a private instant message.
	 */
	public MOB getReplyToMOB();

	/**
	 * Sets the last MOB player who sent this player a private instant message,
	 * and some information about that tell (a defined constant).
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyToMOB()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyType()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyToTime()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_SAY
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_TELL
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_YELL
	 *
	 * @param mob the last MOB player who sent this player a private instant message.
	 * @param replyType the type of
	 */
	public void setReplyTo(MOB mob, int replyType);

	/**
	 * Returns the type of private message last sent to this player, an
	 * encoded constant.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setReplyTo(MOB, int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyToMOB()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyToTime()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_SAY
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_TELL
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_YELL
	 *
	 * @return the type of private message last sent to this player
	 */
	public int getReplyType();

	/**
	 * Returns the last time, in millis since 1970, that a player last
	 * sent this playe a private message.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setReplyTo(MOB, int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyToMOB()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getReplyType()
	 *
	 * @return the last time this player got a private message
	 */
	public long getReplyToTime();

	/**
	 * Returns a read-only Set of security flags that apply to this
	 * player.  All groups are accounted for.
	 *
	 * @return  a group of security flags
	 */
	public CMSecurity.SecGroup getSecurityFlags();

	/**
	 * Returns and/or sets the security flags and groups
	 * strings to which this player is privileged.  The
	 * sets should be semicolon delimited.
	 * @param newFlags null, or a semicolon list of flags and groups
	 * @return the official parsed list of flags and groups
	 */
	public String getSetSecurityFlags(String newFlags);

	/**
	 * When a player is first created, this method is used to
	 * either initialize their birthday, or derive their
	 * birthday based on the number of hours they've played.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getBirthday()
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#getAgeMinutes()
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 *
	 * @param clock the local clock to use for month/day calculations
	 * @param ageHours the number of hours the player played
	 * @param R the players Race
	 * @return the players new age, in mud-years
	 */
	public int initializeBirthday(TimeClock clock, int ageHours, Race R);

	/**
	 * Returns a 2-dimensional integer array with the players birth
	 * day and month (in mud calendar)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#initializeBirthday(TimeClock, int, Race)
	 *
	 * @return a 2-dimensional integer array (day/month)
	 */
	public int[] getBirthday();

	/**
	 * Returns a long value of how stinky this player is.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#adjHygiene(long)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setHygiene(long)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#HYGIENE_DELIMIT
	 *
	 * @return how stinky this player is
	 */
	public long getHygiene();

	/**
	 * Modifies, up or down, how stinky this player is.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getHygiene()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setHygiene(long)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#HYGIENE_COMMONDIRTY
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#HYGIENE_FIGHTDIRTY
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#HYGIENE_WATERCLEAN
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#HYGIENE_DELIMIT
	 *
	 * @param byThisMuch an amount to adjust the stinkiness by.
	 *
	 * @return true of the amount goes past the HYGIENE_DELIMIT constant
	 */
	public boolean adjHygiene(long byThisMuch);

	/**
	 * Sets the number meaning how stinky this player is.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getHygiene()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#adjHygiene(long)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#HYGIENE_DELIMIT
	 *
	 * @param newVal how stinky this player is.
	 */
	public void setHygiene(long newVal);

	/**
	 * Returns whether this player has visited the given room.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#percentVisited(MOB, Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addRoomVisit(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Area)
	 *
	 * @param R the room to check and see whether the player has been there.
	 *
	 * @return true if the player has been there, false otherwise.
	 */
	public boolean hasVisited(Room R);

	/**
	 * Returns whether this player has visited the given area.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#percentVisited(MOB, Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addRoomVisit(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Area)
	 *
	 * @param A the area to check
	 * @return true if the player has been there, false otherwise
	 */
	public boolean hasVisited(Area A);

	/**
	 * Returns the percentage (0-100) of the given area that the
	 * given player has explored.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addRoomVisit(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Area)
	 *
	 * @param mob the player of these stats
	 * @param A the Area to check
	 * @return the percent of the area the player has explored
	 */
	public int percentVisited(MOB mob, Area A);

	/**
	 * Records the fact that this player has been to the given room.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#percentVisited(MOB, Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Area)
	 *
	 * @param R the room to credit the player with
	 * @return true if this was first visit, false otherwise
	 */
	public boolean addRoomVisit(Room R);

	/**
	 * Deletes the visitation record for all rooms in the given area.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#percentVisited(MOB, Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Room)
	 *
	 * @param A area of rooms to remove
	 */
	public void unVisit(Area A);

	/**
	 * Deletes the visitation record for the given room
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#hasVisited(Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#percentVisited(MOB, Area)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#unVisit(Area)
	 *
	 * @param R the room to remove
	 */
	public void unVisit(Room R);

	/**
	 * Returns a personal miscellaneous object map, to be used
	 * for non-core-system attributes, such as transient
	 * class variables.
	 *  
	 * @param charClass the Character Class for the variables
	 * 
	 * @return a personal miscellaneous object map
	 */
	public Map<String,Object> getClassVariableMap(CharClass charClass);
	
	/**
	 * Returns the string array set of defined alias commands
	 * for this player.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAlias(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addAliasName(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#delAliasName(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setAlias(String, String)
	 *
	 * @return the string array set of defined alias commands.
	 */
	public String[] getAliasNames();

	/**
	 * Returns the definition of the given alias command for this player.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAliasNames()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addAliasName(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#delAliasName(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setAlias(String, String)
	 *
	 * @param named the alias command to get the definition of
	 * @return the command(s) to execute when the command is entered.
	 */
	public String getAlias(String named);

	/**
	 * Adds a new alias command for this player, undefined at first.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAliasNames()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAlias(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#delAliasName(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setAlias(String, String)
	 *
	 * @param named the name of the alias command to add
	 */
	public void addAliasName(String named);

	/**
	 * Removes an old alias command for this player.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAliasNames()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAlias(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addAliasName(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setAlias(String, String)
	 *
	 * @param named the name of the alias command to delete
	 */
	public void delAliasName(String named);

	/**
	 * Modifies the commands executed by an existing alias command.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAliasNames()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getAlias(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addAliasName(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#delAliasName(String)
	 *
	 * @param named the alias command to modify
	 * @param value the new command(s) to execute
	 */
	public void setAlias(String named, String value);

	/**
	 * If the INTRODUCTIONSYSTEM is used, this returns true if the player has
	 * been introduced to the other player of the given name.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#introduceTo(String)
	 *
	 * @param name the name of the other player
	 * @return true if this player has met that one, false otherwise
	 */
	public boolean isIntroducedTo(String name);

	/**
	 * If the INTRODUCTIONSYSTEM is used, this notifys the system that this
	 * player has met the player of the given name.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#isIntroducedTo(String)
	 *
	 * @param name the player whom this player just met.
	 */
	public void introduceTo(String name);

	/**
	 * If the ACCOUNTSYSTEM is used, this will access the account object
	 * that is shared by all players of this account.
	 * @return the account object
	 */
	public PlayerAccount getAccount();

	/**
	 * If the ACCOUNTSYSTEM is used, this will allow you to set the account
	 * object that is shared by all players of this account.
	 * @param account the account object
	 */
	public void setAccount(PlayerAccount account);

	/**
	 * Gets external items belonging to this player, which should be destroyed with the
	 * player, but can still be transient.  These are items like player corpses, buried
	 * items, perhaps artifacts, or ships, vehicles, children, etc.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.ItemCollection
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Item
	 *
	 * @return an item collection
	 */
	public ItemCollection getExtItems();

	/**
	 * Whether this object instance is functionally identical to the object passed in.  Works by repeatedly
	 * calling getStat on both objects and comparing the values.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getStatCodes()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getStat(String)
	 * @param E the object to compare this one to
	 * @return whether this object is the same as the one passed in
	 */
	public boolean sameAs(PlayerStats E);

	/**
	 * Returns the theme used to create this player.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_BIT_NAMES
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setTheme(int)
	 * @return the theme used to create this player.
	 */
	public int getTheme();

	/**
	 * Sets the theme used to create this player.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_BIT_NAMES
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTheme()
	 * @param theme the theme used to create this player.
	 */
	public void setTheme(int theme);

	/**
	 * Returns the total legacy levels for this player, all categories
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addLegacyLevel(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getLegacyLevel(String)
	 * @return the total legacy levels for this player
	 */
	public int getTotalLegacyLevels();

	/**
	 * Adds a new legacy level in the given category.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTotalLegacyLevels()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getLegacyLevel(String)
	 * @param category the category to add a legacy level for
	 */
	public void addLegacyLevel(String category);

	/**
	 * Checks whether the given ability ID is on the list of those skills
	 * whose autoInvocation should be suppressed.
	 * The abilityID is case sensitive, and you can send the fake
	 * ID "ANYTHIN" to check if ANY skills are on the list.
	 * @param abilityID the ability ID() to suppress
	 * @return true if it is on the list, false otherwise
	 */
	public boolean isOnAutoInvokeList(String abilityID);
	
	/**
	 * Adds the given ability ID is on the list of those skills
	 * whose autoInvocation should be suppressed.
	 * The abilityID is case sensitive.
	 * @param abilityID the ability ID() to add
	 */
	public void addAutoInvokeList(String abilityID);
	
	/**
	 * Removes the given ability ID is on the list of those skills
	 * whose autoInvocation should be suppressed.
	 * The abilityID is case sensitive.
	 * @param abilityID the ability ID() to remove
	 */
	public void removeAutoInvokeList(String abilityID);
	
	/**
	 * Returns the legacy levels for this player, in the given categories
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addLegacyLevel(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTotalLegacyLevels()
	 * @param category the category to add a legacy level for
	 * @return the legacy levels for this player this category
	 */
	public int getLegacyLevel(String category);

	/**
	 * Returns a mapping definition between awarded abilities and
	 * their requirements.  These are always above and beyond whatever
	 * is provided by class, race, or clan.
	 * @return a skill mapping, keyed by ability ID()
	 */
	public Map<String, AbilityMapping> getExtraQualifiedSkills();

	/**
	 * Returns a mapping definition between awarded expertise and
	 * their requirements.  These are always above and beyond whatever
	 * is provided by class, race, or clan.
	 * @return an expertise map
	 */
	public Map<String, ExpertiseDefinition> getExtraQualifiedExpertises();

	/**
	 * If the player has NoCombatSpam turned on, then these player
	 * settings are used to hold accumulated information about 
	 * combat results for summarizing later.  The map is between
	 * the name of the combatant and the amount of damage taken.
	 * @return a combat damage map
	 */
	public Map<String, int[]> getCombatSpams();
	
	/** Constant for day of birthday, as from {@link PlayerStats#getBirthday()} */
	public static final int BIRTHDEX_DAY = 0;
	/** Constant for month of birthday, as from {@link PlayerStats#getBirthday()} */
	public static final int BIRTHDEX_MONTH = 1;
	/** Constant for year of birthday, as from {@link PlayerStats#getBirthday()} */
	public static final int BIRTHDEX_YEAR = 2;
	/** Constant for year of last known birthday, as from {@link PlayerStats#getBirthday()} */
	public static final int BIRTHDEX_LASTYEARCELEBRATED = 3;

	/** Constant for private messenging, means the last private msg was a SAYTO */
	public static final int REPLY_SAY=0;
	/** Constant for private messenging, means the last private msg was a YELL */
	public static final int REPLY_YELL=1;
	/** Constant for private messenging, means the last private msg was a TELL */
	public static final int REPLY_TELL=2;

	/** Constant for hygiene system, denotes ceiling of stinkiness before emoting */
	public final static long HYGIENE_DELIMIT=5000;
	/** Constant for hygiene system, denotes amount of cleaning water does per tick */
	public final static long HYGIENE_WATERCLEAN=-1000;
	/** Constant for hygiene system, denotes amount of dirtiness from using common skills */
	public final static long HYGIENE_COMMONDIRTY=2;
	/** Constant for hygiene system, denotes amount of dirtiness from fighting */
	public final static long HYGIENE_FIGHTDIRTY=1;
	/** Constant for hygiene system, denotes amount of cleaning water does per tick */
	public final static long HYGIENE_RAINCLEAN=-100;
}
