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
 * A CoffeeTableRow object represents a period of sustained statistics gathering.
 * The CoffeeMud engine keeps counters on all manner of different game events,
 * such as logins, new players, levels, deaths, and others.  CoffeeTableRows
 * are managed by the CoffeeTables library.
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.StatisticsLibrary
 */
public interface CoffeeTableRow extends CMCommon
{
	/**
	 * The start time, in millis since 1970, for this row of data
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#setStartTime(long)
	 * @return the start time in millis
	 */
	public long startTime();

	/**
	 * The end time, in millis since 1970, for this row of data
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#setEndTime(long)
	 * @return the end time in millis
	 */
	public long endTime();

	/**
	 * Sets the start time, in millis since 1970, for this row of data
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#startTime()
	 * @param time the start time in millis
	 */
	public void setStartTime(long time);

	/**
	 * Sets the end time, in millis since 1970, for this row of data
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#endTime()
	 * @param time the end time in millis
	 */
	public void setEndTime(long time);

	/**
	 * Returns the highest number of players online during this period.
	 * @return the highest number of players online
	 */
	public long highestOnline();

	/**
	 * Returns the cumulative number online during this period per poll.
	 * Used to calulate the avg online for the period.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#numberOnlineCounter()
	 * @return the cumulative number online during this period per poll
	 */
	public long numberOnlineTotal();

	/**
	 * Returns the number of times the number of players online has been
	 * polled during this period.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#numberOnlineTotal()
	 * @return number of times the number of players online has been polled
	 */
	public long numberOnlineCounter();

	/**
	 * Returns an XML document representing all the information in this object.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#populate(long, long, String)
	 * @return an xml document
	 */
	public String data();

	/**
	 * Populates this object from an xml document containing relevant statistics.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#data()
	 * @param start the start time, in millis, for this row of data
	 * @param end the end time, in millis, for this row of data
	 * @param data the statistics and data for this row, as xml
	 */
	public void populate(long start, long end, String data);

	/**
	 * Finds a named statistic of the given name, and increments the value
	 * of that long statistic by 1.  Requires the event being recorded.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#STAT_LOGINS
	 * @param s the named statistic to record for the given event
	 * @param type the type of event to tabulate
	 */
	public void bumpVal(String s, int type);

	/**
	 * Gathers relevant information about the given Environmental object
	 * (usually MOB or Ability) and adds to the relevant statistics.
	 * Requires the Environmental object to query, and the event being
	 * recorded.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#STAT_LOGINS
	 * @param E the mob or ability
	 * @param type the type of event to tabulate
	 */
	public void bumpVal(CMObject E, int type);

	/**
	 * Simple method that replaces a strings spaces with _
	 * characters, and makes the string uppercase.
	 * @param s the string to change
	 * @return the changed string
	 */
	public String tagFix(String s);

	/**
	 * Loops through adding all the event stats for the given code string
	 * together
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#STAT_TOTAL
	 * @param code the code string to use, or *
	 * @param tot the running total of all events stats
	 */
	public void totalUp(String code, long[] tot);

	/** a constant index into statistics for login events */
	public final int STAT_LOGINS=0;
	/** a constant index into statistics for a tick event*/
	public final int STAT_TICKSONLINE=1;
	/** a constant index into statistics for a new player event*/
	public final int STAT_NEWPLAYERS=2;
	/** a constant index into statistics for a level gain event */
	public final int STAT_LEVELSGAINED=3;
	/** a constant index into statistics for a death event*/
	public final int STAT_DEATHS=4;
	/** a constant index into statistics for pk death event*/
	public final int STAT_PKDEATHS=5;
	/** a constant index into statistics for a marriage event*/
	public final int STAT_MARRIAGES=6;
	/** a constant index into statistics for a birth event */
	public final int STAT_BIRTHS=7;
	/** a constant index into statistics for a divorce event*/
	public final int STAT_DIVORCES=8;
	/** a constant index into statistics for a class change event */
	public final int STAT_CLASSCHANGE=9;
	/** a constant index into statistics for a purge event */
	public final int STAT_PURGES=10;
	/** a constant index into statistics for a skill use event*/
	public final int STAT_SKILLUSE=11;
	/** a constant index of the total number of enumerated statistical events*/
	public final int STAT_TOTAL=12;

	/** a constant index into statistics for a quest failed start*/
	public final int STAT_QUESTFAILEDSTART=1;
	/** a constant index into statistics for a times start*/
	public final int STAT_QUESTTIMESTART=2;
	/** a constant index into statistics for a quest timeout stop*/
	public final int STAT_QUESTTIMESTOP=3;
	/** a constant index into statistics for a quest manual stop*/
	public final int STAT_QUESTSTOP=4;
	/** a constant index into statistics for a quest accepted*/
	public final int STAT_QUESTACCEPTED=5;
	/** a constant index into statistics for a quest failed*/
	public final int STAT_QUESTFAILED=6;
	/** a constant index into statistics for a quest success*/
	public final int STAT_QUESTSUCCESS=7;
	/** a constant index into statistics for a quest dropped*/
	public final int STAT_QUESTDROPPED=8;
	/** a constant index into statistics for a manual start*/
	public final int STAT_QUESTSTARTATTEMPT=9;

	/** a constant index into statistics for a num players online poll event*/
	public final int STAT_SPECIAL_NUMONLINE=1000;
}
