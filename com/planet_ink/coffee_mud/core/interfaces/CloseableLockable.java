package com.planet_ink.coffee_mud.core.interfaces;

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

/*
   Copyright 2014-2018 Bo Zimmerman

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
 * An interface for all mechanisms that has a lockable door or lid, such
 * as containers and exits
 * @see com.planet_ink.coffee_mud.Exits.interfaces.Exit
 * @see com.planet_ink.coffee_mud.Items.interfaces.Container
 */
public interface CloseableLockable extends Physical
{
	/**
	 * Returns whether this is OPEN and may be accessed or travelled through
	 * @return whether this is OPEN and may be accessed or travelled through
	 */
	public boolean isOpen();

	/**
	 * Returns whether this is LOCKED, and must be unlocked before
	 * being used.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#hasADoor()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#hasALock()
	 * @return true if locked, false otherwise
	 */
	public boolean isLocked();

	/**
	 * Returns whether this has a door, and must be opened before
	 * being used.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#isOpen()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#isLocked()
	 * @return true if a door is present, false otherwise.
	 */
	public boolean hasADoor();

	/**
	 * Returns whether this has a lock on it, and, if locked,
	 * must be unlocked before being used.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#isOpen()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#isLocked()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#hasADoor()
	 * @return whether a lock is present
	 */
	public boolean hasALock();

	/**
	 * For exits with a door and lock, this returns whether this
	 * defaults in a closed and locked state.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#hasADoor()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#defaultsClosed()
	 * @return true if defaults closed and locked, false otherwise
	 */
	public boolean defaultsLocked();

	/**
	 * For exits with a door, this returns whether this
	 * defaults in a closed state.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#hasADoor()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#defaultsLocked()
	 * @return true if defaults closed, false otherwise
	 */
	public boolean defaultsClosed();

	/**
	 * Modifies the various door/lock settings for this thing.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#isOpen()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#isLocked()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#hasADoor()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#hasALock()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#defaultsClosed()
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#defaultsLocked()
	 * @param hasADoor whether this has a door
	 * @param isOpen whether this is open for use
	 * @param defaultsClosed whether this defaults closed
	 * @param hasALock whether this has a lock
	 * @param isLocked whether this is presently locked
	 * @param defaultsLocked whether this defaults closed and locked.
	 */
	public void setDoorsNLocks(boolean hasADoor,
							   boolean isOpen,
							   boolean defaultsClosed,
							   boolean hasALock,
							   boolean isLocked,
							   boolean defaultsLocked);

	/**
	 * For things with doors and locks, this returns the unique string
	 * representing the key code required to unlock/lock it.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#setKeyName(String)
	 * @return the key code
	 */
	public String keyName();

	/**
	 * For things with doors and locks, this sets the unique string
	 * representing the key code required to unlock/lock it.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#keyName()
	 * @param keyName the new key code
	 */
	public void setKeyName(String keyName);
	
	/**
	 * Returns the number of ticks that this remains open when a mob
	 * or player changes it from a closed to an open state, and this thing
	 * defaults in a closed state.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#setOpenDelayTicks(int)
	 * @see com.planet_ink.coffee_mud.core.interfaces.Tickable#TICKID_EXIT_REOPEN
	 * @return the number of ticks this remains open
	 */
	public int openDelayTicks();

	/**
	 * Sets the number of ticks that this will remain open when a mob
	 * or player changes it from a closed to an open state, and this thing
	 * defaults in a closed state.
	 * @see com.planet_ink.coffee_mud.core.interfaces.CloseableLockable#openDelayTicks()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Tickable#TICKID_EXIT_REOPEN
	 * @param numTicks the number of ticks this will remain open
	 */
	public void setOpenDelayTicks(int numTicks);
}
