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
/**
 * Accessible from any PC mob object, PlayerStats are
 * the repository for all manner of player specific
 * mob values.
 * 
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#playerStats()
 */
@SuppressWarnings("unchecked")
public interface PlayerStats extends CMCommon, CMModifiable, AccountStats
{
    /**
     * The time, in milis since 1970, that the player was last saved.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setLastUpdated(long)
     * 
     * @return the time, in milis since 1970, that the player was last saved.
     */
	public long lastUpdated();
	
	/**
     * Sets the time, in milis since 1970, that the player was last saved.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#lastUpdated()
	 * 
	 * @param time the time, in milis since 1970, that the player was last saved.
	 */
    public void setLastUpdated(long time);

    /**
     * The time, in milis since 1970, that the player gained the given level
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setLeveledDateTime(int, Room)
     * 
     * @param level the level to check for
     * @return the time, in milis since 1970, that the player gained the given level
     */
    public long leveledDateTime(int level);

    /**
     * Notifies the player records that, at the moment this method was called,
     * the player gained the given level.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#leveledDateTime(int)
     * 
     * @param level the level to set up
     * @param R the room in which the level was gained
     */
    public void setLeveledDateTime(int level, Room R);

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
	 * Returns a Vector of modifiable title definitions.  These are things
	 * like *, the bunny slayer and such.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getActiveTitle()
     * 
	 * @return  a Vector of modifiable title definitions
	 */
    public Vector getTitles();
    
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
     * Returns a Vector of the last few string messages sent and received to and
     * from this player.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addTellStack(String)
     * 
     * @return a Vector of strings, the last few tell messages
     */
	public Vector getTellStack();
	
	/**
	 * Adds a new string message to the tell stack.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getTellStack()
     * 
	 * @param msg the new message for the tell stack.
	 */
    public void addTellStack(String msg);
    
    /**
     * Returns a Vector of the last few string messages sent and received to and
     * from this players group.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#addGTellStack(String)
     * 
     * @return a Vector of strings, the last few gtell messages
     */
    public Vector getGTellStack();

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
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#poofOut()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
     * 
	 * @return the poof-in GOTO message
	 */
	public String poofIn();
	
    /**
     * For player with the GOTO command, this is the message seen by all when
     * the player leaves using GOTO.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#poofIn()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
     * 
     * @return the poof-out GOTO message
     */
	public String poofOut();

	/**
     * For player with the TRANSFER command, this is the message seen by all when
     * the player arrives from using TRANSFER.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#tranPoofOut()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
     * 
     * @return the poof-in TRANSFER message
	 */
	public String tranPoofIn();
	
    /**
     * For player with the TRANSFER command, this is the message seen by all when
     * the player leaves using TRANSFER.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#tranPoofIn()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setPoofs(String, String, String, String)
     * 
     * @return the poof-out TRANSFER message
     */
	public String tranPoofOut();

	/**
	 * For players with either the GOTO or TRANSFER command, this will set the 
	 * various messages seen when coming and going using either of those commands.
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#tranPoofIn()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#tranPoofOut()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#poofIn()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#poofOut()
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
    public String announceMessage();

    /**
     * For players with the ANNOUNCE command, this sets the message used to
     * prefix the announcements proclaimed.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#announceMessage()
     * 
     * @param msg prefix to announcements
     */
    public void setAnnounceMessage(String msg);
	
	/**
	 * Returns the last MOB player who sent this player a private instant message.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setReplyTo(MOB, int)
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyType()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyTime()
	 * 
	 * @return the last MOB player who sent this player a private instant message.
	 */
    public MOB replyTo();

    /**
     * Sets the last MOB player who sent this player a private instant message,
     * and some information about that tell (a defined constant).
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyTo()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyType()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyTime()
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
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyTo()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyTime()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_SAY
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_TELL
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#REPLY_YELL
	 * 
	 * @return the type of private message last sent to this player
	 */
	public int replyType();
	
	/**
	 * Returns the last time, in millis since 1970, that a player last
	 * sent this playe a private message.
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#setReplyTo(MOB, int)
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyTo()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#replyType()
	 * 
	 * @return the last time this player got a private message
	 */
	public long replyTime();
	
	/**
	 * Returns a modifiable Vector of security flags and groups 
	 * strings to which this player is privileged.
	 * 
	 * @return  a Vector of security flags and groups strings 
	 */
	public Vector getSecurityGroups();

	/**
	 * When a player is first created, this method is used to 
	 * either initialize their birthday, or derive their 
	 * birthday based on the number of hours they've played.
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getBirthday()
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#getAgeHours()
     * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * 
	 * @param ageHours the number of hours the player played
	 * @param R the players Race
	 * @return the players new age, in mud-years
	 */
    public int initializeBirthday(int ageHours, Race R);

    /**
     * Returns a 2-dimensional integer array with the players birth
     * day and month (in mud calendar)
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#initializeBirthday(int, Race)
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
     * 
     * @param R the room to credit the player with
     */
    public void addRoomVisit(Room R);

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
     * Whether this object instance is functionally identical to the object passed in.  Works by repeatedly
     * calling getStat on both objects and comparing the values.
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getStatCodes()
     * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats#getStat(String)
     * @param E the object to compare this one to
     * @return whether this object is the same as the one passed in
     */
	public boolean sameAs(PlayerStats E);

	/** Constant for private messenging, means the last private msg was a SAYTO */
    public static final int REPLY_SAY=0;
    /** Constant for private messenging, means the last private msg was a YELL */
    public static final int REPLY_YELL=1;
    /** Constant for private messenging, means the last private msg was a TELL */
    public static final int REPLY_TELL=2;
    
    /** Constant for hygeine system, denotes ceiling of stinkiness before emoting */
    public final static long HYGIENE_DELIMIT=5000;
    /** Constant for hygeine system, denotes amount of cleaning water does per tick */
    public final static long HYGIENE_WATERCLEAN=-1000;
    /** Constant for hygeine system, denotes amount of dirtiness from using common skills */
    public final static long HYGIENE_COMMONDIRTY=2;
    /** Constant for hygeine system, denotes amount of dirtiness from fighting */
    public final static long HYGIENE_FIGHTDIRTY=1;
}
