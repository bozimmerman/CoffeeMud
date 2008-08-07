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

// TODO: Auto-generated Javadoc
/* 
   Copyright 2000-2008 Bo Zimmerman

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
 * The Interface TimeClock.
 */
public interface TimeClock extends Tickable, CMCommon
{
	
	/**
	 * Time description.
	 * 
	 * @param mob the mob
	 * @param room the room
	 * 
	 * @return the string
	 */
	public String timeDescription(MOB mob, Room room);
    
    /**
     * Gets the short time description.
     * 
     * @return the short time description
     */
    public String getShortTimeDescription();
    
    /**
     * Gets the shortest time description.
     * 
     * @return the shortest time description
     */
    public String getShortestTimeDescription();
	
	/**
	 * Gets the year.
	 * 
	 * @return the year
	 */
	public int getYear();
	
	/**
	 * Sets the year.
	 * 
	 * @param y the new year
	 */
	public void setYear(int y);
	
	/**
	 * Gets the month.
	 * 
	 * @return the month
	 */
	public int getMonth();
	
	/**
	 * Sets the month.
	 * 
	 * @param m the new month
	 */
	public void setMonth(int m);
	
	/**
	 * Gets the moon phase.
	 * 
	 * @return the moon phase
	 */
	public int getMoonPhase();
	
	/**
	 * Gets the day of month.
	 * 
	 * @return the day of month
	 */
	public int getDayOfMonth();
	
	/**
	 * Sets the day of month.
	 * 
	 * @param d the new day of month
	 */
	public void setDayOfMonth(int d);
	
	/**
	 * Gets the time of day.
	 * 
	 * @return the time of day
	 */
	public int getTimeOfDay();
	
	/**
	 * Sets the time of day.
	 * 
	 * @param t the t
	 * 
	 * @return true, if successful
	 */
	public boolean setTimeOfDay(int t);
	
	/**
	 * Gets the tOD code.
	 * 
	 * @return the tOD code
	 */
	public int getTODCode();
	
	/**
	 * Tick tock.
	 * 
	 * @param howManyHours the how many hours
	 */
	public void tickTock(int howManyHours);
	
	/**
	 * Save.
	 */
	public void save();
	
	/**
	 * Gets the season code.
	 * 
	 * @return the season code
	 */
	public int getSeasonCode();
	
	/**
	 * Sets the load name.
	 * 
	 * @param name the new load name
	 */
	public void setLoadName(String name);
	
	/**
	 * Gets the hours in day.
	 * 
	 * @return the hours in day
	 */
	public int getHoursInDay();
	
	/**
	 * Sets the hours in day.
	 * 
	 * @param h the new hours in day
	 */
	public void setHoursInDay(int h);
	
	/**
	 * Gets the days in month.
	 * 
	 * @return the days in month
	 */
	public int getDaysInMonth();
	
	/**
	 * Sets the days in month.
	 * 
	 * @param d the new days in month
	 */
	public void setDaysInMonth(int d);
	
	/**
	 * Gets the months in year.
	 * 
	 * @return the months in year
	 */
	public int getMonthsInYear();
	
	/**
	 * Gets the month names.
	 * 
	 * @return the month names
	 */
	public String[] getMonthNames();
	
	/**
	 * Sets the months in year.
	 * 
	 * @param months the new months in year
	 */
	public void setMonthsInYear(String[] months);
	
	/**
	 * Gets the dawn to dusk.
	 * 
	 * @return the dawn to dusk
	 */
	public int[] getDawnToDusk();
	
	/**
	 * Sets the dawn to dusk.
	 * 
	 * @param dawn the dawn
	 * @param day the day
	 * @param dusk the dusk
	 * @param night the night
	 */
	public void setDawnToDusk(int dawn, int day, int dusk, int night);
	
	/**
	 * Gets the week names.
	 * 
	 * @return the week names
	 */
	public String[] getWeekNames();
	
	/**
	 * Gets the days in week.
	 * 
	 * @return the days in week
	 */
	public int getDaysInWeek();
	
	/**
	 * Sets the days in week.
	 * 
	 * @param days the new days in week
	 */
	public void setDaysInWeek(String[] days);
	
	/**
	 * Gets the year names.
	 * 
	 * @return the year names
	 */
	public String[] getYearNames();
	
	/**
	 * Sets the year names.
	 * 
	 * @param years the new year names
	 */
	public void setYearNames(String[] years);
    
    /**
     * Derive clock.
     * 
     * @param millis the millis
     * 
     * @return the time clock
     */
    public TimeClock deriveClock(long millis);
    
    /**
     * Derive millis after.
     * 
     * @param C the c
     * 
     * @return the long
     */
    public long deriveMillisAfter(TimeClock C);
    
    /**
     * Derive ellapsed time string.
     * 
     * @param millis the millis
     * 
     * @return the string
     */
    public String deriveEllapsedTimeString(long millis);
    
    /**
     * Determine season.
     * 
     * @param str the str
     * 
     * @return the int
     */
    public int determineSeason(String str);
    
    /**
     * Initialize ini clock.
     * 
     * @param page the page
     */
    public void initializeINIClock(CMProps page);
	
	/** The Constant MOON_PHASES. */
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
	
	/** The Constant PHASE_NEW. */
	public final static int PHASE_NEW=0;
	
	/** The Constant PHASE_WAXCRESCENT. */
	public final static int PHASE_WAXCRESCENT=1;
	
	/** The Constant PHASE_WAXQUARTER. */
	public final static int PHASE_WAXQUARTER=2;
	
	/** The Constant PHASE_WAXGIBBOUS. */
	public final static int PHASE_WAXGIBBOUS=3;
	
	/** The Constant PHASE_FULL. */
	public final static int PHASE_FULL=4;
	
	/** The Constant PHASE_WANEGIBBOUS. */
	public final static int PHASE_WANEGIBBOUS=5;
	
	/** The Constant PHASE_WANDEQUARTER. */
	public final static int PHASE_WANDEQUARTER=6;
	
	/** The Constant PHASE_WANECRESCENT. */
	public final static int PHASE_WANECRESCENT=7;
	
	/** The Constant PHASE_BLUE. */
	public final static int PHASE_BLUE=8;
	
	/** The Constant PHASE_DESC. */
	public final static String[] PHASE_DESC={"NEW","WAXCRESCENT","WAXQUARTER","WAXGIBBOUS","FULL","WANEGIBBOUS","WANEQUARTER","WANECRESCENT","BLUE"};
	
	/** The Constant TOD_DESC. */
	public final static String[] TOD_DESC={
		"It is dawn ","It is daytime ","It is dusk ","It is nighttime "
	};
										   
	/** The Constant TIME_DAWN. */
	public final static int TIME_DAWN=0;
	
	/** The Constant TIME_DAY. */
	public final static int TIME_DAY=1;
	
	/** The Constant TIME_DUSK. */
	public final static int TIME_DUSK=2;
	
	/** The Constant TIME_NIGHT. */
	public final static int TIME_NIGHT=3;
	
	/** The Constant SEASON_SPRING. */
	public final static int SEASON_SPRING=0;
	
	/** The Constant SEASON_SUMMER. */
	public final static int SEASON_SUMMER=1;
	
	/** The Constant SEASON_FALL. */
	public final static int SEASON_FALL=2;
	
	/** The Constant SEASON_WINTER. */
	public final static int SEASON_WINTER=3;
	
	/** The Constant SEASON_DESCS. */
	public final static String[] SEASON_DESCS={"SPRING","SUMMER","FALL","WINTER"};
	
}
