package com.planet_ink.coffee_mud.core.interfaces;
import java.util.Iterator;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
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
   Copyright 2005-2018 Bo Zimmerman

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
 * This class represents a thread, consisting of a group of Tickable objects  receiving periodic calls
 * to their tick(Tickable,int) methods by this thread object
 * @see Tickable
 * @see Tickable#tick(Tickable, int)
 * @author Bo Zimmerman
 *
 */
public interface TickableGroup extends CMRunnable
{
	/**
	 * Returns the current or last TickClient object which this thread made a tick(Tickable,int) method
	 * call to.
	 * @see Tickable
	 * @see Tickable#tick(Tickable, int)
	 * @return the TickClient object last accessed
	 */
	public TickClient getLastTicked();

	/**
	 * Returns the name of this group
	 * @return the name
	 */
	public String getName();

	/**
	 * Returns the amount of time, in ms, between calls to this group
	 * @return the time between ticks
	 */
	public long getTickInterval();

	/**
	 * Returns the next time, in ms, when this group should tick.
	 * @return the next time, in ms, when this group should tick.
	 */
	public long getNextTickTime();

	/**
	 * Orders this group to do any necessary cleanup before going away.
	 */
	public void shutdown();

	/**
	 * Adds another ticking object to this group
	 * @see TickClient
	 * @param C the client to add
	 */
	public void addTicker(TickClient C);

	/**
	 * Removes the given ticking object from this group
	 * @return whether anything was deleted.
	 * @see TickClient
	 * @param C the client to remove
	 */
	public boolean delTicker(TickClient C);

	/**
	 * Returns an iterator of all the ticking objects
	 * @return an iterator of all the ticking objects
	 */
	public Iterator<TickClient> tickers();

	/**
	 * Returns the number of ticking objects in this group
	 * @return the number of ticking objects in this group
	 */
	public int numTickers();

	/**
	 * Returns an iterator of all clients matching the given criteria
	 * @param T the tickable object to look for
	 * @param tickID the tickid to match, or -1 for all
	 * @return an iterator of all clients matching the given criteria
	 */
	public Iterator<TickClient> getTickSet(final Tickable T, final int tickID);

	/**
	 * Returns an iterator of all the items of the given type in the given room.
	 * @param itemTypes 0=mobs or items, 1=items, 2=mobs
	 * @param R the room to look in
	 * @return an iterator of all the items of the given type in the given room.
	 */
	public Iterator<TickClient> getLocalItems(int itemTypes, Room R);

	/**
	 * Returns true if this group contains the given ticking object with
	 * the given tickID.
	 * @param T the tickable object
	 * @param tickID the tickid to match, or -1 for any
	 * @return true if its in here, false otherwise
	 */
	public boolean contains(final Tickable T, final int tickID);

	/**
	 * Returns the tickclient at the given index from 0 to numTickers
	 * @see TickableGroup#numTickers()
	 * @param i the index
	 * @return the tickclient, or null
	 */
	public TickClient fetchTickerByIndex(int i);

	/**
	 * Returns the displayable status of this ticking object
	 * @return the displayable status of this ticking object
	 */
	public String getStatus();

	/**
	 * Returns the time, in ms, that this object last ticked.
	 * @return the time, in ms, that this object last ticked.
	 */
	public long getLastStartTime();

	/**
	 * Returns the time, in ms, that this object last stopped ticking.
	 * @return the time, in ms, that this object last stopped ticking
	 */
	public long getLastStopTime();

	/**
	 * Returns the number of ms that this object has ticked in total
	 * @return the number of ms that this object has ticked in total
	 */
	public long getMilliTotal();

	/**
	 * Returns the number of times that this object has ticked in total
	 * @return the number of times that this object has ticked in total
	 */
	public long getTickTotal();

	/**
	 * Returns whether this group is only permitted a single object
	 * @return true if this group is only permitted a single object
	 */
	public boolean isSolitaryTicker();

	/**
	 * Returns true if this group currently is getting thread time
	 * @return true if this group currently is getting thread time
	 */
	public boolean isAwake();

	/**
	 * Returns the name of the thread group that this ticking group
	 * belongs to (and thus, under which it should run later)
	 * @return the name of the thread group
	 */
	public String getThreadGroupName();

	/**
	 * If this group is currently getting thread time, this will
	 * return a reference to that thread object, or null otherwise.
	 * @return the thread currently giving this group time, or null
	 */
	public Thread getCurrentThread();
}
