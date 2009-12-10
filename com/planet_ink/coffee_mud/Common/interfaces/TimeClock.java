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
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setTimeOfDay(int)
     * 
	 * @return the time of day (hour)
	 */
	public int getTimeOfDay();
	
	/**
	 * Sets the time of day (the hour).
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#getTimeOfDay()
     * 
	 * @param t the time of day (the hour)
	 * 
	 * @return true, if the new time denotes a change of sun-orientation, false otherwise
	 */
	public boolean setTimeOfDay(int t);
	
	/**
	 * Gets the TOD code, which is the sun-orientation (morning, evening, etc)
	 * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setTimeOfDay(int)
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#setDawnToDusk(int, int, int, int)
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DAWN
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DUSK
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DAY
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_NIGHT
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TOD_DESC
     * 
	 * @return the TOD code
	 */
	public int getTODCode();
	
    /**
     * Gets the moon phase an an enumeration.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#MOON_PHASES
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#PHASE_DESC
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#PHASE_FULL
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#PHASE_NEW
     * 
     * @return the moon phase as an enumeration
     */
    public int getMoonPhase();
    
    /**
     * Gets the season code.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#SEASON_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#SEASON_FALL
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#SEASON_SPRING
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#SEASON_SUMMER
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#SEASON_WINTER
     * 
     * @return the season code
     */
    public int getSeasonCode();
    
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
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DAWN
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DUSK
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DAY
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_NIGHT
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TOD_DESC
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
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DAWN
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DUSK
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_DAY
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TIME_NIGHT
     * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock#TOD_DESC
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
	
	/** The Moon Phase Constant MOON_PHASES, with long sentence descriptions of the various phases. */
	public final static String[] MOON_PHASES={
		"There is a new moon in the sky.",
		"The moon is in the waxing crescent phase.",
		"The moon is in its first quarter.",
		"The moon is in the waxing gibbous phase (almost full).",
		"There is a full moon in the sky.",
		"The moon is in the waning gibbous phase (no longer full).",
		"The moon is in its last quarter.",
		"The moon is in the waning crescent phase.",
		"There is a BLUE MOON! Oh my GOD! Run away!!!!!"
	};
	
	/** The Moon Phase Constant PHASE_NEW. */
	public final static int PHASE_NEW=0;
	
	/** The Moon Phase Constant PHASE_WAXCRESCENT. */
	public final static int PHASE_WAXCRESCENT=1;
	
	/** The Moon Phase Constant PHASE_WAXQUARTER. */
	public final static int PHASE_WAXQUARTER=2;
	
	/** The Moon Phase Constant PHASE_WAXGIBBOUS. */
	public final static int PHASE_WAXGIBBOUS=3;
	
	/** The Moon Phase Constant PHASE_FULL. */
	public final static int PHASE_FULL=4;
	
	/** The Moon Phase Constant PHASE_WANEGIBBOUS. */
	public final static int PHASE_WANEGIBBOUS=5;
	
	/** The Moon Phase Constant PHASE_WANDEQUARTER. */
	public final static int PHASE_WANDEQUARTER=6;
	
	/** The Moon Phase Constant PHASE_WANECRESCENT. */
	public final static int PHASE_WANECRESCENT=7;
	
	/** The Moon Phase Constant PHASE_BLUE. */
	public final static int PHASE_BLUE=8;
	
	/** The Moon Phase Constant PHASE_DESC, holding the coded short values of the moon phases. */
	public final static String[] PHASE_DESC={"NEW","WAXCRESCENT","WAXQUARTER","WAXGIBBOUS","FULL","WANEGIBBOUS","WANEQUARTER","WANECRESCENT","BLUE"};
	
	/** The Constant TOD_DESC, with a sentence description of each sun-orientation (time of day). */
	public final static String[] TOD_DESC={
		"It is dawn ","It is daytime ","It is dusk ","It is nighttime "
	};
										   
	/** The Time Of Day (TOD) Constant TIME_DAWN. */
	public final static int TIME_DAWN=0;
	
	/** The Time Of Day (TOD) Constant TIME_DAY. */
	public final static int TIME_DAY=1;
	
	/** The Time Of Day (TOD) Constant TIME_DUSK. */
	public final static int TIME_DUSK=2;
	
	/** The Time Of Day (TOD) Constant TIME_NIGHT. */
	public final static int TIME_NIGHT=3;
	
	/** The Season Constant SEASON_SPRING. */
	public final static int SEASON_SPRING=0;
	
	/** The Season Constant SEASON_SUMMER. */
	public final static int SEASON_SUMMER=1;
	
	/** The Season Constant SEASON_FALL. */
	public final static int SEASON_FALL=2;
	
	/** The Season Constant SEASON_WINTER. */
	public final static int SEASON_WINTER=3;
	
	/** The Season Constant SEASON_DESCS, with the string word name of each season. */
	public final static String[] SEASON_DESCS={"SPRING","SUMMER","FALL","WINTER"};
	
}
