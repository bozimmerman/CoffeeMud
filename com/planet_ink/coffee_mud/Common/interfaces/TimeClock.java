package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.Calendar;
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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setDaysInWeek(String[])
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDaysInWeek()
	 *
	 * @return the week names as an array of strings
	 */
	public String[] getWeekNames();

	/**
	 * Gets the number of days in each week
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setDaysInWeek(String[])
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getWeekNames()
	 *
	 * @return the days in each week
	 */
	public int getDaysInWeek();

	/**
	 * Sets the number of days in each week by naming each of them in a string array
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getDaysInWeek()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getWeekNames()
	 *
	 * @param days the new days in each week string array
	 */
	public void setDaysInWeek(String[] days);

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
	 * Increase this clocks time by the given number of hours.
	 * Does NOT move the sky.  Use tickTock for that.
	 * @see TimeClock#tickTock(int)
	 * @see TimeClock#bumpDays(int)
	 * @see TimeClock#bumpWeeks(int)
	 * @see TimeClock#bumpMonths(int)
	 * @see TimeClock#bumpYears(int)
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
	 * @param num the number to bump
	 */
	public void bumpYears(int num);

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
	 * @param the new time, in hours since epoc.
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
	 * Different time periods.
	 * @author Bo Zimmerman
	 */
	public enum TimePeriod
	{
		HOUR(60L * 60L * 1000L),
		DAY(60L * 60L * 1000L * 24L),
		WEEK(60L * 60L * 1000L * 24L * 7L),
		MONTH(60L * 60L * 1000L * 24L * 30L),
		SEASON(60L * 60L * 1000L * 24L * 365L / 4L),
		YEAR(60L * 60L * 1000L * 24L * 365L),
		ALLTIME(0)
		;
		private final long increment;
		private TimePeriod(long increment)
		{
			this.increment=increment;
		}

		public long getIncrement()
		{
			return increment;
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
		NEW("There is a new moon in the sky.",1.0,TidePhase.SPRING_HIGH,TidePhase.SPRING_LOW),
		WAXCRESCENT("The moon is in the waxing crescent phase.",0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		WAXQUARTER("The moon is in its first quarter.",0.0,TidePhase.NEAP_HIGH,TidePhase.NEAP_LOW),
		WAXGIBBOUS("The moon is in the waxing gibbous phase (almost full).",-0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		FULL("There is a full moon in the sky.",-1.0,TidePhase.SPRING_HIGH,TidePhase.SPRING_LOW),
		WANEGIBBOUS("The moon is in the waning gibbous phase (no longer full).",-0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		WANEQUARTER("The moon is in its last quarter.",0.0,TidePhase.NEAP_HIGH,TidePhase.NEAP_LOW),
		WANECRESCENT("The moon is in the waning crescent phase.",0.5,TidePhase.NORMAL_HIGH,TidePhase.NORMAL_LOW),
		BLUE("There is a BLUE MOON! Oh my GOD! Run away!!!!!",2.0,TidePhase.SPRING_HIGH,TidePhase.SPRING_LOW);

		private final String	phaseDesc;
		private final double	factor;
		private final TidePhase	highTide;
		private final TidePhase	lowTide;

		private MoonPhase(String desc, double factor, TidePhase highTide, TidePhase lowTide)
		{
			phaseDesc=desc;
			this.factor=factor;
			this.highTide=highTide;
			this.lowTide=lowTide;
		}

		public String getDesc()
		{
			return phaseDesc;
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
		SPRING_HIGH("The tide is especially high.",1.5),
		SPRING_LOW("The tide  is especially low.",-1.5),
		NORMAL_HIGH("The tide is high.",1.0),
		NORMAL_LOW("The tide is low.",-1.0),
		NEAP_HIGH("The tide is weak, but high.",0.5),
		NEAP_LOW("The tide is weak, but low.",-0.5),
		NO_TIDE("The tide is normal.", 0.0)
		;
		private final String phaseDesc;
		private final double factor;

		private TidePhase(String desc, double factor)
		{
			phaseDesc=desc;
			this.factor=factor;
		}

		public String getDesc()
		{
			return phaseDesc;
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

		private TimeOfDay(String desc)
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
