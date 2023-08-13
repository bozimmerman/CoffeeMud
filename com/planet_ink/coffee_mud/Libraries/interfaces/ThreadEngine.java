package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.TickableGroup.LocalType;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2004-2023 Bo Zimmerman

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
 * Low level task and thread management system, which contains its own
 * internal respect for the "thread group id" system used by CoffeeMud.
 * This is where all repeating things are scheduled, and new tasks
 * launched.
 *
 * A Tick is a global unit of time, usually 4 seconds.
 * A Ticker or TickClient is an object that receives cpu time,
 * usually every Tick, and gives that time to a Tickable.
 * A Tickable is a normal common CoffeeMud object, such
 * as a mob or item, that needs cpu time.
 * A Tick Group is a collection of Tickers that, for
 * efficiency, tick in a sequence, but at the
 * same rate (usually once per Tick).
 *
 * @author Bo Zimmerman
 *
 */
public interface ThreadEngine extends CMLibrary, Runnable
{
	/**
	 * Starts a standard repeating "ticker", but with
	 * a custom Tick time.
	 *
	 * @param E The Ticking object
	 * @param tickID the tick id denoting the type of event
	 * @param tickTimeMs the time between ticks, usually global
	 * @param numTicks the number of given ticks between Tickable ticks
	 * @return the TickClient inside which the Tickable is placed
	 */
	public TickClient startTickDown(Tickable E, int tickID, long tickTimeMs, int numTicks);
	/**
	 * Starts a standard repeating "ticker", on standard Tick time.
	 *
	 * @param E The Ticking object
	 * @param tickID the tick id denoting the type of event
	 * @param numTicks the number of given ticks between Tickable ticks
	 * @return the TickClient inside which the Tickable is placed
	 */
	public TickClient startTickDown(Tickable E, int tickID, int numTicks);

	/**
	 * Searches the TickGroups for the given Tickable, returning true
	 * if it was found still registered to a Tick Client.  Call
	 * sparingly!
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ANY
	 * @return true if the Tickable was found somewhere
	 */
	public boolean isTicking(Tickable E, int tickID);

	/**
	 * Searches the TickGroups for the given Tickable, and returns
	 * the number of miliseconds (roughly) before it will tick
	 * again.
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ANY
	 * @return the time in ms until next tick, or -1
	 */
	public long getTimeMsToNextTick(Tickable E, int tickID);

	/**
	 * Returns an iterator over all the existing tick groups.
	 *
	 * @return an iterator over all the existing tick groups.
	 */
	public Iterator<TickableGroup> tickGroups();

	/**
	 * Searches the TickGroups for the given Tickable, and returns
	 * the number of milliseconds that its tick group is using as
	 * a "tick".
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ANY
	 * @return the time in ms of its tick group, or -1
	 */
	public long getTickGroupPeriod(final Tickable E, final int tickID);

	/**
	 * Searches the TickGroups for the given Tickable, on the
	 * given tickID, and deletes it.
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ALL
	 * @return true if it was found and killed.
	 */
	public boolean deleteTick(Tickable E, int tickID);

	/**
	 * A special method that deletes all known ticks of
	 * all tick ids from the given object, but also deletes
	 * all ticks of any objects dependent on it, such as
	 * item on mobs, effects on things, in rooms, etc.
	 *
	 * @param E the object to go completely quiet
	 * @return true if something was done
	 */
	public boolean unTickAll(Tickable E);

	/**
	 * If the given Tickable is waiting multiple ticks
	 * before they get time, this will set them to
	 * pending, meaning they will wake up on the very
	 * next opportunity.
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ALL
	 * @return true if the object was found, false otherwise
	 */
	public boolean setTickPending(Tickable E, int tickID);

	/**
	 * Suspends any actual thread time for this tick,
	 * without removing it from its tick group.
	 *
	 * @see ThreadEngine#resumeTicking(Tickable, int)
	 * @see ThreadEngine#suspendResumeRecurse(CMObject, boolean, boolean)
	 * @see ThreadEngine#isSuspended(Tickable, int)
	 * @see ThreadEngine#suspendAll(CMRunnable[])
	 * @see ThreadEngine#resumeAll()
	 * @see ThreadEngine#isAllSuspended()
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ALL
	 */
	public void suspendTicking(Tickable E, int tickID);

	/**
	 * Resumes the thread time for this tick,
	 * if it was previously suspended.
	 *
	 * @see ThreadEngine#suspendTicking(Tickable, int)
	 * @see ThreadEngine#suspendResumeRecurse(CMObject, boolean, boolean)
	 * @see ThreadEngine#isSuspended(Tickable, int)
	 * @see ThreadEngine#suspendAll(CMRunnable[])
	 * @see ThreadEngine#resumeAll()
	 * @see ThreadEngine#isAllSuspended()
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ALL
	 */
	public void resumeTicking(Tickable E, int tickID);

	/**
	 * Suspends or Resumes all embedded mobs, items, etc inside
	 * the given area, room, mob, etc.  It can even do the inside
	 * of ships, unless told otherwise.
	 *
	 * @see ThreadEngine#resumeTicking(Tickable, int)
	 * @see ThreadEngine#suspendTicking(Tickable, int)
	 * @see ThreadEngine#isSuspended(Tickable, int)
	 * @see ThreadEngine#suspendAll(CMRunnable[])
	 * @see ThreadEngine#resumeAll()
	 * @see ThreadEngine#isAllSuspended()
	 *
	 * @param O the object to suspend ticks on and well inside
	 * @param skipEmbeddedAreas true to skip ships and the like
	 * @param suspend true to suspend, false to resume
	 */
	public void suspendResumeRecurse(CMObject O, boolean skipEmbeddedAreas, boolean suspend);

	/**
	 * Returns whether the given Tickable is currently
	 * suspended from receiving tick time, despite being
	 * assigned a tick group.
	 *
	 * @see ThreadEngine#resumeTicking(Tickable, int)
	 * @see ThreadEngine#suspendTicking(Tickable, int)
	 * @see ThreadEngine#suspendResumeRecurse(CMObject, boolean, boolean)
	 * @see ThreadEngine#suspendAll(CMRunnable[])
	 * @see ThreadEngine#resumeAll()
	 * @see ThreadEngine#isAllSuspended()
	 *
	 * @param E the Tickable to search for
	 * @param tickID the assigned tick id, or -1 for ALL
	 * @return true if its suspended, false otherwise
	 */
	public boolean isSuspended(Tickable E, int tickID);

	/**
	 * Suspend all threads, preventing anything from
	 * getting official tick time, except for the given
	 * runnable, typically a Session.
	 *
	 * @see ThreadEngine#resumeTicking(Tickable, int)
	 * @see ThreadEngine#suspendTicking(Tickable, int)
	 * @see ThreadEngine#suspendResumeRecurse(CMObject, boolean, boolean)
	 * @see ThreadEngine#isSuspended(Tickable, int)
	 * @see ThreadEngine#resumeAll()
	 * @see ThreadEngine#isAllSuspended()
	 *
	 * @param exceptRs null, or the runnables to NOT suspend
	 */
	public void suspendAll(CMRunnable[] exceptRs);

	/**
	 * Resumes all thread ticking, assuming they were
	 * all previously suspended.
	 *
	 * @see ThreadEngine#resumeTicking(Tickable, int)
	 * @see ThreadEngine#suspendTicking(Tickable, int)
	 * @see ThreadEngine#suspendResumeRecurse(CMObject, boolean, boolean)
	 * @see ThreadEngine#isSuspended(Tickable, int)
	 * @see ThreadEngine#suspendAll(CMRunnable[])
	 * @see ThreadEngine#isAllSuspended()
	 */
	public void resumeAll();

	/**
	 * Returns whether all threads are currently in
	 * the global suspended thread.
	 *
	 * @see ThreadEngine#resumeTicking(Tickable, int)
	 * @see ThreadEngine#suspendTicking(Tickable, int)
	 * @see ThreadEngine#suspendResumeRecurse(CMObject, boolean, boolean)
	 * @see ThreadEngine#isSuspended(Tickable, int)
	 * @see ThreadEngine#suspendAll(CMRunnable[])
	 * @see ThreadEngine#resumeAll()
	 *
	 * @return true if all are suspended, false otherwise
	 */
	public boolean isAllSuspended();

	/**
	 * Schedule a runnable to be executed in the future.
	 *
	 * @see ThreadEngine#scheduleRunnable(Runnable, long)
	 * @see ThreadEngine#executeRunnable(Runnable)
	 * @see ThreadEngine#executeRunnable(char, Runnable)
	 * @see ThreadEngine#executeRunnable(String, Runnable)
	 *
	 * @param R the runnable to execute
	 * @param ellapsedMs the number of milliseconds before executing
	 */
	public void scheduleRunnable(Runnable R, long ellapsedMs);

	/**
	 * Execute a runnable in an available thread, right now,
	 * or as soon as humanly possible.
	 *
	 * @see ThreadEngine#scheduleRunnable(Runnable, long)
	 * @see ThreadEngine#executeRunnable(Runnable)
	 * @see ThreadEngine#executeRunnable(char, Runnable)
	 * @see ThreadEngine#executeRunnable(String, Runnable)
	 *
	 * @param R the runnable to execute
	 */
	public void executeRunnable(Runnable R);

	/**
	 * Execute a runnable in an available thread, right now,
	 * or as soon as humanly possible.  Ensure that it is
	 * from the given thread group, given its name.
	 *
	 * @see ThreadEngine#scheduleRunnable(Runnable, long)
	 * @see ThreadEngine#executeRunnable(Runnable)
	 * @see ThreadEngine#executeRunnable(char, Runnable)
	 * @see ThreadEngine#executeRunnable(String, Runnable)
	 *
	 * @param threadGroupName the thread group to execute it in
	 * @param R the runnable to execute
	 */
	public void executeRunnable(String threadGroupName, Runnable R);

	/**
	 * Execute a runnable in an available thread, right now,
	 * or as soon as humanly possible.  Ensure that it is
	 * from the given thread group, given its id.
	 *
	 * @see ThreadEngine#scheduleRunnable(Runnable, long)
	 * @see ThreadEngine#executeRunnable(Runnable)
	 * @see ThreadEngine#executeRunnable(char, Runnable)
	 * @see ThreadEngine#executeRunnable(String, Runnable)
	 *
	 * @param threadGroupId the thread group to execute it in
	 * @param R the runnable to execute
	 */
	public void executeRunnable(final char threadGroupId, final Runnable R);

	/**
	 * Given a map room, this will find and force-tick every one of those
	 * objects, on the current tick group.  This is normally for
	 * speeding things up.
	 *
	 * @param here the room to tick objects in
	 */
	public void tickAllTickers(Room here);

	/**
	 * Given a map room, this will find all the rejuvable items and
	 * mobs associated with the room and force them to respawn.
	 *
	 * @param here the room to force rejuv items in
	 * @param tickID -1 for items and mobs, or TICKID_MOB or TICKID_ROOM_ITEM_REJUV
	 */
	public void rejuv(Room here, int tickID);

	/**
	 * Deletes the ticks of all the objects in the
	 * room of the given type.
	 *
	 * @see TickableGroup.LocalType
	 *
	 * @param room the mob to delete objects from
	 * @param typeCode the type of objects
	 */
	public void clearDebri(Room room, LocalType typeCode);

	/**
	 * Retreives special internal information from
	 * the thread engine of the callers thread
	 * group, mostly for the web server.
	 * Variables:
	 *  tickgroupsize
	 *  tickerssizeX
	 *  tickernameX-Y
	 * 	tickeridX-Y
	 * 	tickerstatusstrX-Y
	 * 	tickerstatusX-Y
	 * 	tickercodewordX-Y
	 * 	tickertickdownX-Y
	 * 	tickerretickdownX-Y
	 * 	tickermillitotalX-Y
	 * 	tickermilliavgX-Y
	 * 	tickerlaststartmillisX-Y
	 * 	tickerlaststopmillisX-Y
	 * 	tickerlaststartdateX-Y
	 * 	tickerlaststopdateX-Y
	 * 	tickerlastdurationX-Y
	 * 	tickersuspendedX-Y
	 *
	 *  X is group num, Y is client num
	 *
	 * @param which the special encoded variable to return
	 * @return "", or the value
	 */
	public String getTickInfoReport(String which);

	/**
	 * Retreives special thread related information about
	 * map objects.
	 * Variables:
	 * totalMOBMillis
	 * totalMOBMillisTime
	 * totalMOBMillisTimePlusAverage
	 * totalMOBTicks
	 * topMOBMillis
	 * topMOBMillisTime
	 * topMOBMillisTimePlusAverage
	 * topMOBTicks
	 * topMOBClient
	 * tickerproblems-X  (X is the number to return)
	 * tickerprob2-X  (X is the number to return)
	 * freeMemory
	 * totalMemory
	 * totalTime
	 * startTime
	 * currentTime
	 * totalTickers
	 * totalMillis
	 * totalMillisTime
	 * totalMillisTimePlusAverage
	 * totalTicks
	 * tickgroupsize
	 * numthreads
	 * numactivethreads
	 * topGroupNumber
	 * topGroupMillis
	 * topGroupMillisTime
	 * topGroupMillisTimePlusAverage
	 * topGroupTicks
	 * topObjectMillis
	 * topObjectMillisTime
	 * topObjectMillisTimePlusAverage
	 * topObjectTicks
	 * topObjectGroup
	 * activeMiliTotalX
	 * milliTotalX
	 * statusX
	 * nameX
	 * MilliTotalTimeX
	 * MiliTotalTimeX
	 * MilliTotalTimePlusAverageX
	 * MiliTotalTimePlusAverageX
	 * TickTotalX
	 * X = thread number
	 *
	 * @param itemCode the special variable to get value of
	 * @return "". or the value
	 */
	public String getSystemReport(String itemCode);

	/**
	 * A bit out of date, this returns the status of the
	 * given ticking object as a descriptive string.
	 *
	 * @param obj the object that is maybe ticking
	 * @return a description, or ""
	 */
	public String getTickStatusSummary(Tickable obj);

	/**
	 * Does an In-String search of all ticking objects
	 * and returns them.
	 * @param name the partial name
	 * @return the list of named objects
	 */
	public List<Tickable> getNamedTickingObjects(String name);

	/**
	 * Finds all the TickClients with tickers matching
	 * the given name.  This is a english parser format
	 * search.
	 *
	 * @param name the string name to match
	 * @param exactOnly true for exact only, false for partial
	 * @return the list of tickclients with matching hits
	 */
	public List<TickClient> findTickClient(final String name, final boolean exactOnly);

	/**
	 * This is used for determining the runner on the given thread,
	 * which involves scanning thread pools for some reason?
	 *
	 * @param thread the thread to get the runnable for
	 * @return the runnable, or null, presumably
	 */
	public Runnable findRunnableByThread(final Thread thread);

	/**
	 * Dumps a stack trace, logging with the given ID
	 *
	 * @param ID the log id, a string of your choice
	 * @param theThread the thread to dump
	 */
	public void dumpDebugStack(final String ID, Thread theThread);
}
