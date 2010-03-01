package com.planet_ink.coffee_mud.Exits.interfaces;
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
 * An interface for all mechanisms or pathways through which a mob may
 * travel when trying to get from one Room to another.
 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
 */
public interface Exit extends Environmental
{
    /**
     * Returns whether this exit is OPEN and may be travelled through
     * @return whether this exit is OPEN and may be travelled through
     */
	public boolean isOpen();
	
	/**
	 * Returns whether this exit is LOCKED, and must be unlocked before
	 * being used.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasALock()
	 * @return true if locked, false otherwise
	 */
	public boolean isLocked();
	
	/**
	 * Returns whether this exit has a door, and must be opened before
	 * being used.
	 * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isOpen()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isLocked()
	 * @return true if a door is present, false otherwise.
	 */
	public boolean hasADoor();
	
	/**
	 * Returns whether this exit has a lock on its door, and, if locked, 
	 * must be unlocked before being used.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isOpen()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isLocked()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
	 * @return whether a lock is present
	 */
	public boolean hasALock();
	
	/**
	 * For exits with a door and lock, this returns whether the door
	 * defaults in a closed and locked state. 
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#defaultsClosed()
	 * @return true if defaults closed and locked, false otherwise
	 */
	public boolean defaultsLocked();
	
	
    /**
     * For exits with a door, this returns whether the door
     * defaults in a closed state. 
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#defaultsLocked()
     * @return true if defaults closed, false otherwise
     */
	public boolean defaultsClosed();
	
	/**
	 * Modifies the various door/lock settings for this exit.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isOpen()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isLocked()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasALock()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#defaultsClosed()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#defaultsLocked()
	 * @param hasADoor whether this exit has a door
	 * @param isOpen whether this exit is open for travel
	 * @param defaultsClosed whether this exit defaults closed
	 * @param hasALock whether this exit has a door lock
	 * @param isLocked whether this exit is presently locked
	 * @param defaultsLocked whether this exit defaults closed and locked.
	 */
	public void setDoorsNLocks(boolean hasADoor,
							   boolean isOpen,
							   boolean defaultsClosed,
							   boolean hasALock,
							   boolean isLocked,
							   boolean defaultsLocked);
	
	/**
	 * For Exits with doors and locks, this returns the unique string
	 * representing the key code required to unlock/lock the door.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setKeyName(String)
	 * @return the key code
	 */
	public String keyName();
	
	/**
     * For Exits with doors and locks, this sets the unique string
     * representing the key code required to unlock/lock the door.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#keyName()
	 * @param keyName the new key code
	 */
	public void setKeyName(String keyName);
	
	/**
	 * For Exits that are readable, this returns the readable string
	 * for this exit.  That is to say, what the player sees when they
	 * read the door.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isReadable()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setReadable(boolean)
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setReadableText(String)
	 * @return the readable string
	 */
	public String readableText();
	
	/**
	 * Returns whether this exit is readable when the player uses the READ command
	 * and targets it.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#readableText()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setReadable(boolean)
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setReadableText(String)
	 * @return true if the exit is readable.
	 */
	public boolean isReadable();
	
	/**
     * Returns whether this exit is readable when the player uses the READ command
     * and targets it.  Readable text should also be set or unset.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#readableText()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isReadable()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setReadableText(String)
	 * @param isTrue true if the exit is readable, and false otherwise
	 */
	public void setReadable(boolean isTrue);
	
	/**
     * For Exits that are readable, this set the readable string
     * for this exit.  That is to say, what the player sees when they
     * read the door.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#isReadable()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setReadable(boolean)
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#readableText()
	 * @param text the readable text
	 */
	public void setReadableText(String text);
	
	/**
	 * Both reads and optionally modifies an internal reference counter for this
	 * exit.  Not currently functional.
	 * @param change 0 to make no change, or a positive or negative number
	 * @return the value of the usage counter after the change is applied
	 */
	public short exitUsage(short change);
	
	/**
	 * Returns the modified and qualified player-viewed description of this exit. 
	 * @param mob the mob doing the viewing
	 * @param myRoom the room from which the mob sees the exit
	 * @return the description of this exit from the given player pov
	 */
	public StringBuilder viewableText(MOB mob, Room myRoom);
	
	/**
	 * Returns the short name of this door, e.g. gate, door, portal, etc..
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setExitParams(String, String, String, String)
	 * @return the short name of this door.
	 */
	public String doorName();
	
    /**
     * Returns the verb used for describing closing this exit, e.g. close,
     * lower, drop, etc..
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#openWord()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#defaultsClosed()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setExitParams(String, String, String, String)
     * @return the short name of a verb used when closing this door.
     */
	public String closeWord();
    
    /**
     * Returns the verb used for describing opening this exit, e.g. open,
     * raise, lift, etc..
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#closeWord()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#defaultsClosed()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setExitParams(String, String, String, String)
     * @return the short name of a verb used when opening this door.
     */
	public String openWord();
	
	/**
	 * Returns a text description of what this exit looks like when closed. 
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#hasADoor()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#defaultsClosed()
	 * @return A description of this exit when closed.
	 */
	public String closedText();
	
	/**
	 * Sets various word descriptions used when doing some dynamic manipulations
	 * of this exit.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#closeWord()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#openWord()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#doorName()
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#closedText()
	 * @param newDoorName short name of this door, e.g. door, gate
	 * @param newCloseWord verb used to close this door, e.g. close, lower
	 * @param newOpenWord verb used to open this door, e.g. open, raise
	 * @param newClosedText description of this exit when in a closed state
	 */
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText);

	/**
	 * Returns the number of ticks that this exit remains open when a mob
	 * or player changes it from a closed to an open state, and the door
	 * defaults in a closed state.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setOpenDelayTicks(int)
	 * @see com.planet_ink.coffee_mud.core.interfaces.Tickable#TICKID_EXIT_REOPEN
	 * @return the number of ticks this exit remains open
	 */
	public int openDelayTicks();
	
	/**
     * Sets the number of ticks that this exit will remain open when a mob
     * or player changes it from a closed to an open state, and the door
     * defaults in a closed state.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#openDelayTicks()
     * @see com.planet_ink.coffee_mud.core.interfaces.Tickable#TICKID_EXIT_REOPEN
	 * @param numTicks the number of ticks this exit will remain open
	 */
	public void setOpenDelayTicks(int numTicks);
	
	/**
	 * If this exit represents a route to a room not yet created, but whose room id
	 * is already known, this will return that designated room id.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#setTemporaryDoorLink(String)
	 * @return the room id of the room that will be linked in later
	 */
	public String temporaryDoorLink();
	
    /**
     * If this exit represents a route to a room not yet created, but whose room id
     * is already known, this will set that designated room id.
     * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit#temporaryDoorLink()
     * @param link the room id of the room that will be linked in later
     */
	public void setTemporaryDoorLink(String link);
}
