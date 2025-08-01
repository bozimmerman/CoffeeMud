package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
 * This interface represents more than a "Time Zone", but
 * a complete calendar, a complete lunar cycle, and
 * schedule for days and nights.  Oh, and it also manages
 * the current date and time.
 *
 *  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#setTimeObj(TimeClock)
 *  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTimeObj()
 */
public interface TimeClock extends Tickable, CMCommon
{
	/**
	 * Returns a complete description of the date, time, and lunar
	 * orientation, and day of the week in a full sentence word
	 * format.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getShortTimeDescription()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getShortestTimeDescription()
	 *
	 * @param mob the mob who wants to know the time
	 * @param room the room the room where the mob is wanting the time
	 *
	 * @return the string representing the date and time
	 */
	public String timeDescription(MOB mob, Room room);

	/**
	 * Gets a shorter time description, showing the date and time in
	 * full sentence format, but skipping lunar orientation.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#timeDescription(MOB, Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getShortestTimeDescription()
	 *
	 * @return the short time description
	 */
	public String getShortTimeDescription();

	/**
	 * Gets the shortest time description, showing only the date and
	 * time in brief numeric format.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#timeDescription(MOB, Room)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getShortTimeDescription()
	 *
	 * @return the shortest time description
	 */
	public String getShortestTimeDescription();

	/**
	 * Gets the current year.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setYear(int)
	 *
	 * @return the current year
	 */
	public int getYear();

	/**
	 * Sets the current year.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getYear()
	 *
	 * @param y the new year
	 */
	public void setYear(int y);

	/**
	 * Gets the current month.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setMonth(int)
	 *
	 * @return the current month
	 */
	public int getMonth();

	/**
	 * Sets the current month.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getMonth()
	 *
	 * @param m the new month
	 */
	public void setMonth(int m);

	/**
	 * Gets the current day of month.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setDayOfMonth(int)
	 *
	 * @return the day of month
	 */
	public int getDayOfMonth();

	/**
	 * Sets the current day of month.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDayOfMonth()
	 *
	 * @param d the new day of month
	 */
	public void setDayOfMonth(int d);

	/**
	 * Gets the current time of day (the hour).
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setHourOfDay(int)
	 *
	 * @return the time of day (hour)
	 */
	public int getHourOfDay();

	/**
	 * Sets the time of day (the hour).
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getHourOfDay()
	 *
	 * @param t the time of day (the hour)
	 *
	 * @return true, if the new time denotes a change of sun-orientation, false otherwise
	 */
	public boolean setHourOfDay(int t);

	/**
	 * Gets the TOD code, which is the sun-orientation (morning, evening, etc)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setHourOfDay(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setDawnToDusk(int, int, int, int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimeOfDay
	 *
	 * @return the TOD code
	 */
	public TimeOfDay getTODCode();

	/**
	 * Gets the moon phase an an enumeration.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.MoonPhase
	 *
	 * @param room the room to check the moon phase for
	 * @return the moon phase as an enumeration
	 */
	public MoonPhase getMoonPhase(Room room);

	/**
	 * Gets the phase of the tides as an enumeration.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TidePhase
	 *
	 * @param room the room to check the tide phase for
	 * @return the tide phase as an enumeration
	 */
	public TidePhase getTidePhase(Room room);

	/**
	 * Gets the season code.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.Season
	 *
	 * @return the season code
	 */
	public Season getSeasonCode();

	/**
	 * Returns the number of months in a season.
	 *
	 * @return  the number of months in a season.
	 */
	public int getMonthsInSeason();

	/**
	 * Alters the time/day by the given number of hours (forward
	 * or backward)
	 *
	 * @param howManyHours the how many hours to alter the time by
	 */
	public void tickTock(int howManyHours);

	/**
	 * Saves the current time/date information where ever its supposed
	 * to be saved.  Requires that setLoadName be called before.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setLoadName(String)
	 */
	public void save();

	/**
	 * Sets the name of this time object for the purposes of database loading
	 * and saving.  This is required for the save method to do anything at all.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#save()
	 *
	 * @param name the new load name
	 */
	public void setLoadName(String name);

	/**
	 * Gets the hours in a day.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setHoursInDay(int)
	 *
	 * @return the hours in a day
	 */
	public int getHoursInDay();

	/**
	 * Sets the hours in a day.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getHoursInDay()
	 *
	 * @param h the new hours in a day
	 */
	public void setHoursInDay(int h);

	/**
	 * Gets the days in a month.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setDaysInMonth(int)
	 *
	 * @return the days in a month
	 */
	public int getDaysInMonth();

	/**
	 * Sets the days in a month.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDaysInMonth()
	 *
	 * @param d the new days in a month
	 */
	public void setDaysInMonth(int d);

	/**
	 * Gets the months in a year.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setMonthsInYear(String[])
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getMonthNames()
	 *
	 * @return the months in a year
	 */
	public int getMonthsInYear();

	/**
	 * Gets the month names, in order, indexed by 0.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setMonthsInYear(String[])
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getMonthNames()
	 *
	 * @return the month names
	 */
	public String[] getMonthNames();

	/**
	 * Sets the months in year as a string array of names, arranged in order.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getMonthNames()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getMonthsInYear()
	 *
	 * @param months the new month names in a year
	 */
	public void setMonthsInYear(String[] months);

	/**
	 * Gets the dawn to dusk values, indexed by the TOD constants.
	 * Each entry is an hour when the TOD starts. The order of the
	 * values is DAWN, DAY, DUSK, NIGHT.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getTODCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setDawnToDusk(int, int, int, int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimeOfDay
	 *
	 * @return the dawn to dusk array
	 */
	public int[] getDawnToDusk();

	/**
	 * Sets the dawn to dusk values as absolute hours when that particular
	 * time of day begins.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDawnToDusk()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getTODCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimeOfDay
	 *
	 * @param dawn the dawn hour
	 * @param day the day hour
	 * @param dusk the dusk hour
	 * @param night the night hour
	 */
	public void setDawnToDusk(int dawn, int day, int dusk, int night);

	/**
	 * Gets the week names, which is the names of the days of each week,
	 * a string array indexed by the day of the week - 1.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setWeekNames(String[])
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDaysInWeek()
	 *
	 * @return the week names as an array of strings
	 */
	public String[] getWeekNames();

	/**
	 * Gets the number of days in each week
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setWeekNames(String[])
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getWeekNames()
	 *
	 * @return the days in each week
	 */
	public int getDaysInWeek();

	/**
	 * Gets the current week of the month.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDaysInWeek()
	 *
	 * @return the week of month, 0-x
	 */
	public int getWeekOfMonth();

	/**
	 * Gets the current week of the year.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDaysInWeek()
	 *
	 * @return the week of year, 0-x
	 */
	public int getWeekOfYear();

	/**
	 * Gets the current days of the year.
	 *
	 * @return the day of year, 1-x
	 */
	public int getDayOfYear();

	/**
	 * Gets the current days of the year.
	 *
	 * @return the day of year, 1-x
	 */
	public int getDaysInYear();

	/**
	 * Sets the number of days in each week by naming each of them in a string array
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDaysInWeek()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getWeekNames()
	 *
	 * @param days the new days in each week string array
	 */
	public void setWeekNames(String[] days);

	/**
	 * Gets the names of the year, an arbitrary sized list that is rotated
	 * through from year to year. (think: "year of the donkey", etc..)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setYearNames(String[])
	 *
	 * @return the year names, if any.
	 */
	public String[] getYearNames();

	/**
	 * Sets the year names, which is an arbitrary sized list that is rotated
	 * through from year to year. (think: "year of the donkey", etc..)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getYearNames()
	 *
	 * @param years the new years names
	 */
	public void setYearNames(String[] years);

	/**
	 * Using the current time and date as a yardstick, and assuming constant
	 * running and perfect timing, this method will derive a mud date and time
	 * from the given real life date/time in milliseconds.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveMillisAfter(TimeClock)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveEllapsedTimeString(long)
	 *
	 * @param millis the milliseconds since 1970
	 *
	 * @return the time clock object representing that real life time
	 */
	public TimeClock deriveClock(long millis);

	/**
	 * This method will discover the difference in mud hours between this time clock
	 * and the given EARLIER clock, assuming they use the same scale of measurement.
	 * It will then return the number of actual milliseconds would have elapsed,
	 * assuming constant running and perfect timing.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveClock(long)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveEllapsedTimeString(long)
	 *
	 * @param C the earlier time clock object
	 *
	 * @return the number of milliseconds elapsed since the given clock
	 */
	public long deriveMillisAfter(TimeClock C);

	/**
	 * This method will discover the difference in mud hours between this time clock
	 * and the given EARLIER clock, assuming they use the same scale of measurement.
	 * It will then return the number of actual mud hours would have elapsed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveClock(long)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveEllapsedTimeString(long)
	 *
	 * @param C the earlier time clock object
	 *
	 * @return the number of mud hours elapsed since the given clock
	 */
	public long deriveMudHoursAfter(TimeClock C);

	/**
	 * Given a standard time period, returns this local clocks
	 * elapsed millis for each period.
	 *
	 * @param P the TimePeriod to get a duration for
	 * @return the millis for the TimePeriod
	 */
	public long getPeriodMillis(final TimePeriod P);

	/**
	 * Returns whether this time clock represents the
	 * exact same hour as the given one, timezones
	 * notwithstanding.
	 *
	 * @see TimeClock#isBefore(TimeClock)
	 * @see TimeClock#isEqual(TimeClock)
	 * @see TimeClock#isAfter(TimeClock)
	 *
	 * @param C the clock to compare to
	 * @return true if they are the same
	 */
	public boolean isEqual(final TimeClock C);

	/**
	 * Returns whether this time clock represents an
	 * earlier hour than the given one, timezones
	 * notwithstanding.
	 *
	 * @param C the clock to compare to
	 * @return true if this is before
	 */
	public boolean isBefore(final TimeClock C);

	/**
	 * Returns whether this time clock represents an
	 * earlier later than the given one, timezones
	 * notwithstanding.
	 *
	 * @param C the clock to compare to
	 * @return true if this is later
	 */
	public boolean isAfter(final TimeClock C);

	/**
	 * Increase this clocks time by the given number of hours.
	 * Does NOT move the sky.  Use tickTock for that.
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpDays(int)
	 * @see TimeClock#bumpWeeks(int)
	 * @see TimeClock#bumpMonths(int)
	 * @see TimeClock#bumpYears(int)
	 * @see TimeClock#bump(TimePeriod, int)
	 * @param num the number to bump
	 */
	public void bumpHours(int num);

	/**
	 * Increase this clocks time by the given number of days.
	 * Does NOT move the sky.  Use tickTock for that.
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpHours(int)
	 * @see TimeClock#bumpWeeks(int)
	 * @see TimeClock#bumpMonths(int)
	 * @see TimeClock#bumpYears(int)
	 * @see TimeClock#bump(TimePeriod, int)
	 * @param num the number to bump
	 */
	public void bumpDays(int num);

	/**
	 * Increase this clocks time by the given number of weeks.
	 * Does NOT move the sky.  Use tickTock for that.
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpHours(int)
	 * @see TimeClock#bumpDays(int)
	 * @see TimeClock#bumpMonths(int)
	 * @see TimeClock#bumpYears(int)
	 * @see TimeClock#bump(TimePeriod, int)
	 * @param num the number to bump
	 */
	public void bumpWeeks(int num);

	/**
	 * Increase this clocks time by the given number of months.
	 * Does NOT move the sky.  Use tickTock for that.
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpHours(int)
	 * @see TimeClock#bumpDays(int)
	 * @see TimeClock#bumpWeeks(int)
	 * @see TimeClock#bumpYears(int)
	 * @see TimeClock#bump(TimePeriod, int)
	 * @param num the number to bump
	 */
	public void bumpMonths(int num);

	/**
	 * Increase this clocks time by the given number of years.
	 * Does NOT move the sky.  Use tickTock for that.
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpHours(int)
	 * @see TimeClock#bumpDays(int)
	 * @see TimeClock#bumpWeeks(int)
	 * @see TimeClock#bumpMonths(int)
	 * @see TimeClock#bump(TimePeriod, int)
	 * @param num the number to bump
	 */
	public void bumpYears(int num);

	/**
	 * Increase this clocks time by the given number of
	 * time periods.
	 *
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpHours(int)
	 * @see TimeClock#bumpDays(int)
	 * @see TimeClock#bumpWeeks(int)
	 * @see TimeClock#bumpMonths(int)
	 * @see TimeClock#bumpYears(int)
	 * @param P the time period
	 * @param num the number of periods to bump by
	 */
	public void bump(final TimePeriod P, final int num);

	/**
	 * Increase this clocks time by the given number of
	 * time periods, setting the remainder of the clock
	 * to the very first moment of that time period.
	 *
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpHours(int)
	 * @see TimeClock#bumpDays(int)
	 * @see TimeClock#bumpWeeks(int)
	 * @see TimeClock#bumpMonths(int)
	 * @see TimeClock#bumpYears(int)
	 * @param period the time period
	 * @param times the number of periods to bump by
	 */
	public void bumpToNext(final TimePeriod period, final int times);

	/**
	 * Returns the total hours since epoc
	 * @see TimeClock#setFromHoursSinceEpoc(long)
	 * @return total hours since epoc
	 */
	public long toHoursSinceEpoc();

	/**
	 * Sets this clock to the given number
	 * of hours since epoc. Does NOT move the
	 * sky, you need to call tickTock for that.
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#toHoursSinceEpoc()
	 * @param num the new time, in hours since epoc.
	 */
	public void setFromHoursSinceEpoc(long num);

	/**
	 * Using the given number of milliseconds, this method will return a string
	 * describing the number of mud days, hours, etc that is represented by
	 * that amount of real time, assuming constant running and perfect timing.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveMillisAfter(TimeClock)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#deriveClock(long)
	 *
	 * @param millis the milliseconds of elapsed time represented
	 *
	 * @return the string representing the elapsed mud time
	 */
	public String deriveEllapsedTimeString(long millis);

	/**
	 * Initialize ini clock by reading calendar values from the given
	 * properties page.  Does not load current values from anywhere.
	 *
	 * @param page the properties page
	 */
	public void initializeINIClock(CMProps page);

	/**
	 * Causes the world to visibly react to a change in time
	 */
	public void handleTimeChange();

	/**
	 * Converts this TimeClock to an approximate real-life
	 * millis since epoc.
	 * @param now the timeclock to convert
	 *
	 * @return the real time of this clock.
	 */
	public long toTimestamp(TimeClock now);

	/**
	 * Returns this clocks date/time, plus a brief
	 * definition of the scale of the various time periods.
	 *
	 * @return the coded time
	 */
	public String toTimePeriodCodeString();

	/**
	 * Copies this clock and sets it accord to
	 * the coded date/time.
	 *
	 * @param period the coded time
	 * @return the TimeClock object copy
	 */
	public TimeClock fromTimePeriodCodeString(final String period);

	/**
	 * Returns this timeclock in m/d/y h format.
	 * @return this timeclock in m/d/y h format.
	 */
	public String toTimeString();

	/**
	 * Updates the date/time info from one clock into this one.
	 * The calendar structure should be the same before doing this.
	 *
	 * @param fromC the current clock to copy from
	 */
	public void setDateTime(final TimeClock fromC);

	/**
	 * Given a time period, returns the number of mud hours
	 * contained in each, based on this clock.
	 *
	 * @param period the period you are curious about
	 * @return the number of mud hours.
	 */
	public int getHoursPer(final TimePeriod period);

	/**
	 * Returns the value of the given period for this
	 * time clock object.
	 *
	 * @param period the hours, months, etc.
	 * @return the value for this calendar
	 */
	public int get(final TimePeriod period);

	/**
	 * Returns the maximum value of the given period for this
	 * time clock object.
	 *
	 * @param period hours, months, days, etc
	 * @return the last hour, months, etc.
	 */
	public int getMax(final TimePeriod period);

	/**
	 * Returns the first value of the given period for this
	 * time clock object.
	 *
	 * @param period hours, months, days, etc
	 * @return the first hour, months, etc.
	 */
	public int getMin(final TimePeriod period);

	/**
	 * Sets the value of the given period for this
	 * time clock object.
	 *
	 * @param period the hours, months, etc.
	 * @param value the value for this calendar
	 */
	public void set(final TimePeriod period, int value);

	/**
	 * Sets the value of the given period for this
	 * time clock object by bumping the clock until
	 * it is the next of the given period.
	 *
	 * @param period the hours, months, etc.
	 * @param value the value for this calendar
	 */
	public void setNext(final TimePeriod period, int value);

	/**
	 * Different time periods for player stats.
	 * @author Bo Zimmerman
	 */
	public enum TimePeriod
	{
		HOUR(TimeManager.MILI_HOUR),
		DAY(TimeManager.MILI_DAY),
		WEEK(TimeManager.MILI_WEEK),
		MONTH(TimeManager.MILI_MONTH),
		SEASON(TimeManager.MILI_YEAR / 4L),
		YEAR(TimeManager.MILI_YEAR),
		ALLTIME(0)
		;
		private final long increment;
		private static TimePeriod[] reversed = null;

		private TimePeriod(final long increment)
		{
			this.increment=increment;
		}

		public long getIncrement()
		{
			return increment;
		}

		public static TimePeriod get(final String s)
		{
			if(s==null)
				return null;
			try
			{
				return valueOf(s.toUpperCase().trim());
			}
			catch(final Exception e)
			{}
			if(s.endsWith("s")||s.endsWith("S"))
				return get(s.substring(0,s.length()-1));
			return null;
		}

		public static TimePeriod[] reversed()
		{
			if(reversed == null)
			{
				reversed = Arrays.copyOf(values(),values().length);
				Collections.reverse(Arrays.asList(reversed));
			}
			return reversed;
		}

		public long nextPeriod()
		{
			final Calendar calendar=Calendar.getInstance();
			calendar.add(Calendar.MILLISECOND, -calendar.get(Calendar.MILLISECOND));
			calendar.add(Calendar.SECOND, -calendar.get(Calendar.SECOND));
			calendar.add(Calendar.MINUTE, -calendar.get(Calendar.MINUTE));
			switch(this)
			{
			case HOUR:
				calendar.add(Calendar.HOUR_OF_DAY,1);
				break;
			case DAY:
				calendar.add(Calendar.HOUR_OF_DAY,-calendar.get(Calendar.HOUR_OF_DAY));
				calendar.add(Calendar.DATE,1);
				break;
			case WEEK:
				calendar.add(Calendar.DAY_OF_WEEK,-(calendar.get(Calendar.DAY_OF_WEEK)-1));
				calendar.add(Calendar.HOUR_OF_DAY,-calendar.get(Calendar.HOUR_OF_DAY));
				calendar.add(Calendar.WEEK_OF_YEAR,1);
				break;
			case MONTH:
				calendar.add(Calendar.DAY_OF_MONTH,-(calendar.get(Calendar.DAY_OF_MONTH)-1));
				calendar.add(Calendar.HOUR_OF_DAY,-calendar.get(Calendar.HOUR_OF_DAY));
				calendar.add(Calendar.MONTH,1);
				break;
			case SEASON:
				calendar.add(Calendar.DAY_OF_YEAR, -(calendar.get(Calendar.DAY_OF_YEAR) % (365/4)));
				calendar.add(Calendar.HOUR_OF_DAY,-calendar.get(Calendar.HOUR_OF_DAY));
				calendar.add(Calendar.DAY_OF_YEAR,(365/4));
				break;
			case YEAR:
				calendar.add(Calendar.DAY_OF_YEAR,-(calendar.get(Calendar.DAY_OF_YEAR)-1));
				calendar.add(Calendar.HOUR_OF_DAY,-calendar.get(Calendar.HOUR_OF_DAY));
				calendar.add(Calendar.YEAR,1);
				break;
			case ALLTIME:
				return Long.MAX_VALUE;
			}
			return calendar.getTimeInMillis();
		}
	}

	/**
	 * The phases of the moon
	 * @author Bo Zimmerman
	 */
	public enum MoonPhase
	{
		NEW(1.0,TidePhase.SPRING_HIGH,TidePhase.SPRING_LOW),
		WAXCRESCENT(0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		WAXQUARTER(0.0,TidePhase.NEAP_HIGH,TidePhase.NEAP_LOW),
		WAXGIBBOUS(-0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		FULL(-1.0,TidePhase.SPRING_HIGH,TidePhase.SPRING_LOW),
		WANEGIBBOUS(-0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		WANEQUARTER(0.0,TidePhase.NEAP_HIGH,TidePhase.NEAP_LOW),
		WANECRESCENT(0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		BLUE(2.0,TidePhase.SPRING_HIGH,TidePhase.SPRING_LOW);

		private final double	factor;
		private final TidePhase	highTide;
		private final TidePhase	lowTide;
		private String tideDesc = null;

		private MoonPhase(final double factor, final TidePhase highTide, final TidePhase lowTide)
		{
			this.factor=factor;
			this.highTide=highTide;
			this.lowTide=lowTide;
		}

		public String getDesc()
		{
			if(tideDesc == null)
			{
				switch(this)
				{
				case NEW: tideDesc= CMLib.lang().L("There is a new moon in the sky."); break;
				case WAXCRESCENT: tideDesc= CMLib.lang().L("The moon is in the waxing crescent phase."); break;
				case WAXQUARTER: tideDesc= CMLib.lang().L("The moon is in its first quarter."); break;
				case WAXGIBBOUS: tideDesc= CMLib.lang().L("The moon is in the waxing gibbous phase (almost full)."); break;
				case FULL: tideDesc= CMLib.lang().L("There is a full moon in the sky."); break;
				case WANEGIBBOUS: tideDesc= CMLib.lang().L("The moon is in the waning gibbous phase (no longer full)."); break;
				case WANEQUARTER: tideDesc= CMLib.lang().L("The moon is in its last quarter."); break;
				case WANECRESCENT: tideDesc= CMLib.lang().L("The moon is in the waning crescent phase."); break;
				case BLUE: tideDesc= CMLib.lang().L("There is a BLUE MOON! Oh my GOD! Run away!!!!!"); break;
				}
			}
			return tideDesc;
		}

		public double getFactor()
		{
			return factor;
		}

		public TidePhase getHighTide()
		{
			return highTide;
		}

		public TidePhase getLowTide()
		{
			return lowTide;
		}
	}

	/**
	 * The phases of the tides
	 * @author Bo Zimmerman
	 */
	public enum TidePhase
	{
		SPRING_HIGH(1.5),
		SPRING_LOW(-1.5),
		NORMAL_HIGH(1.0),
		NORMAL_LOW(-1.0),
		NEAP_HIGH(0.5),
		NEAP_LOW(-0.5),
		NO_TIDE(0.0)
		;
		private final double factor;
		private String tideDesc = null;

		private TidePhase(final double factor)
		{
			this.factor=factor;
		}

		public String getDesc()
		{
			if(tideDesc == null)
			{
				switch(this)
				{
				case SPRING_HIGH: tideDesc = CMLib.lang().L("The tide is especially high."); break;
				case SPRING_LOW: tideDesc = CMLib.lang().L("The tide  is especially low."); break;
				case NORMAL_HIGH: tideDesc = CMLib.lang().L("The tide is high."); break;
				case NORMAL_LOW: tideDesc = CMLib.lang().L("The tide is low."); break;
				case NEAP_HIGH: tideDesc = CMLib.lang().L("The tide is weak, but high."); break;
				case NEAP_LOW: tideDesc = CMLib.lang().L("The tide is weak, but low."); break;
				case NO_TIDE: tideDesc = CMLib.lang().L("The tide is normal."); break;
				}
			}
			return tideDesc;
		}

		public double getFactor()
		{
			return factor;
		}
	}

	/**
	 * Time of Day enumeration
	 * @author Bo Zimmerman
	 */
	public enum TimeOfDay
	{
		DAWN("It is dawn "),
		DAY("It is daytime "),
		DUSK("It is dusk "),
		NIGHT("It is nighttime ")
		;
		private final String todDesc;

		private TimeOfDay(final String desc)
		{
			todDesc=desc;
		}

		public String getDesc()
		{
			return todDesc;
		}
	}

	/**
	 * Enumeration for the season of the year
	 * @author Bo Zimmerman
	 */
	public enum Season
	{
		SPRING,
		SUMMER,
		FALL,
		WINTER
	}

	// old codes:
}
