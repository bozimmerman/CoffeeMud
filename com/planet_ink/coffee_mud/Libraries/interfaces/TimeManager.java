package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.*;

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
public interface TimeManager extends CMLibrary
{
	/**
	 * Returns the numeric representation of the month
	 *
	 * Usage: month2MM("January");
	 * @param monthName The month name
	 * @return String The number of the month as a string
	 */
	public String month2MM(String monthName);
	/**
	 * Return the name of the month, given a number
	 *
	 * Usage: String Mnth=getMonthName(m,GiveShort).charStats();
	 * @param monthNum Month number to convert
	 * @param giveShort Give abbreviation if true
	 * @return String Month name
	 */
	public String getMonthName(int monthNum, boolean giveShort);

	/**
	 * Returns whether the given string would parse to a valid
	 * date.  If true is returned, the change of getting a valid
	 * date from string2Date is much higher.
	 * @param dateTimeStr the strong to parse
	 * @return true if its possibly valid, false definitely not
	 */
	public boolean isValidDateString(String dateTimeStr);

	/**
	 * Converts a string of some form into a Calendar object.
	 *
	 * Usage: string2Millis(GetRes(Results,"StartDateTime"));
	 * @param dateTimeStr The string to retrieve from
	 * @return Calendar Calendar object
	 */
	public long string2Millis(String dateTimeStr);

	/**
	 * Converts a string of some form into a Calendar object.
	 *
	 * Usage: Calendar.string2Date(GetRes(Results,"StartDateTime"));
	 * @param dateTimeStr The string to retrieve from
	 * @return Calendar Calendar object
	 */
	public Calendar string2Date(String dateTimeStr);

	/**
	 * Converts a number to two digits.
	 * @param num the number
	 * @return the number as two digits
	 */
	public String twoDigits(long num);

	/**
	 * Returns the regular Hours given the hours in the
	 * international format (military time)
	 *
	 * Usage: ConvertHour(GetIn(req, "ENDHR"))
	 * @param hours24 Hours in military format
	 * @return String Hours in regular format
	 **/
	public String convertHour(String hours24);

	/**
	 * Returns the AMPM stamp given the international Hours portion the Time
	 *
	 * Usage: getAMPM(GetIn(req, "ENDHR"))
	 * @param TheHour Hours in military format
	 * @return String AM or PM stamp
	 **/
	public String getAMPM(String TheHour);

	/**
	 * Get the zone id given the timezone string
	 *
	 * Usage: GetTheZoneID(MeetTZ.getRawOffset())+"\n";
	 * @param theRawOffset The time zone's raw offset to convert
	 * @return String The time zone ID
	 */
	public String getTheIntZoneID(int theRawOffset);

	/**
	 *  Returns the time zone of the given ID
	 *
	 * Usage: MEETZN = GetTheTimeZone(ID);
	 * @param theID The ID of the abbreviated time zone.
	 * @return String The time zone name
	 */
	public String getTheTimeZone(String theID);

	/**
	 * Returns the month for a given date
	 *
	 * Usage: String ENDMM=date2MonthString(time,true);
	 * @param time The time in miliseconds
	 * @param shortName true to use shortened months
	 * @return String The month name
	 **/
	public String date2MonthString(long time, boolean shortName);

	/**
	 * Returns time in HH:MMap format, unless the day is other
	 * than today, in which case it prepends yy/m/d.
	 *
	 * Usage: String ENDMM=date2BriefString(time,true);
	 * @param time The time in miliseconds
	 * @return String The date string
	 **/
	public String date2BriefString(long time);

	/**
	 * Returns the month/day for a given date
	 *
	 * Usage: String ENDMM=date2MonthDateString();
	 * @param time The time in miliseconds
	 * @param shortName true to use shortened months
	 * @return String the month/day name
	 **/
	public String date2MonthDateString(long time, boolean shortName);

	/**
	 * Return the time in HH:MM AP format.
	 * @param time the time in millis
	 * @return the time in format
	 */
	public String date2APTimeString(long time);

	/**
	 * Returns the DD portion of a given date
	 *
	 * Usage: String ENDDD=date2DayOfMonthString();
	 * @param time The time in miliseconds
	 * @return String The day
	 **/
	public String date2DayOfMonthString(long time);

	/**
	* Returns the YYYY portion of a given date
	* Returns the DD portion of a given date
	*
	* Usage: String ENDYYYY=date2YYYYString();
	 * @param time The time in miliseconds
	* @return String The year
	**/
	public String date2YYYYString(long time);

	/**
	* Returns the Hours portion of a given Time
	*
	* Usage: String ENDHR=date2HRString();
	* @param time time used
	* @return String The hour
	**/
	public String date2HRString(long time);

	/**
	* Returns the Minutes portion of a given Time
	*
	* Usage: String ENDMIN=date2MINString();
	* @param time The time in miliseconds
	* @return String The minutes
	**/
	public String date2MINString(long time);

	/**
	 * format the date for an smtp message
	 *
	 * Usage:  smtpDateFormat(98374987234)
	* @param time The time in miliseconds
	* @return String The minutes
	 */
	 public String smtpDateFormat(long time);

	/**
	 *  Returns the time zone of the server
	 *
	 * Usage: MEETZN = T2ZoneString();
	 * @param time The time in miliseconds
	 * @return String The time zone
	 */
	public String date2ZoneString(long time);
	/**
	 * Returns the Minutes portion of a given Time
	 *
	 * Usage: String ST_AMPM=date2AMPMString(time);
	 * @param time The time in miliseconds
	 * @return String AM or PM stamp
	 **/
	public String date2AMPMString(long time);
	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY HH:MM AP
	 *
	 * Usage: date2String()
	 * @param C The time in calendar
	 * @return String Formatted date/time
	 */
	public String date2String(Calendar C);
	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY HH:MM AP
	 *
	 * Usage: date2String(time)
	 * @param time The time in miliseconds
	 * @return String Formatted date/time
	 */
	public String date2String(long time);
	/**
	 * Converts a given number of milliseconds,
	 * into a number of rl years, months, days,
	 * hours, minutes, and seconds.  If in short form,
	 * returns y, m, d, h, m, and s.
	 *
	 * Usage: date2EllapsedTime(time)
	 * @param time The time in miliseconds
	 * @param minUnit The smallest unit to round down to
	 * @param shortest true for short form, false otherwise
	 * @return String Formatted ellapsed time
	 */
	public String date2EllapsedTime(long time, TimeUnit minUnit, boolean shortest);

	/**
	 * Picks the single best representation of the given ellapsed time, 
	 * from seconds to minutes to hours to days, and returns the correct
	 * amount along with the single letter suffix
	 * @param t time in milliseconds
	 * @return ellapsed time approximation.
	 */
	public String date2BestShortEllapsedTime(long t);
	/**
	 * Converts a given number of milliseconds,
	 * into a number of rl years, months, days,
	 * hours, minutes, and seconds.  If in short form,
	 * returns y, m, d, h, m, and s.  This method
	 * will automatically determine the smallest reasonable
	 * unit of time to show.
	 *
	 * Usage: date2SmartEllapsedTime(time)
	 * @param time The time in miliseconds
	 * @param shortest true for short form, false otherwise
	 * @return String Formatted ellapsed time
	 */
	public String date2SmartEllapsedTime(long time, boolean shortest);

	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY HH:MM AP
	 *
	 * Usage: date2SecondsString(time)
	 * @param time The time in miliseconds
	 * @return String Formatted date/time
	 */
	public String date2SecondsString(long time);
	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY
	 *
	 * Usage: date2DateString(time)
	 * @param time The time in miliseconds
	 * @return String Formatted date
	 */
	public String date2DateString(long time);
	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YY
	 *
	 * Usage: date2Date2String(time)
	 * @param time The time in miliseconds
	 * @return String Formatted date
	 */
	public String date2Date2String(long time);

	/**
	 * Parses either a tick expression, or an
	 * expression ending with the word minutes,
	 * hours, seconds, days, mudhours, muddays,
	 * mudweeks, mudmonths, or mudyears
	 * @param val the expression
	 * @return the number of ticks represented by the string
	 */
	public int parseTickExpression(String val);

	/**
	 * Parses whether a tick expression, or an
	 * expression ending with the word minutes,
	 * hours, seconds, days, mudhours, muddays,
	 * mudweeks, mudmonths, or mudyears
	 * @param val the expression
	 * @return whether a number of ticks is represented by the string
	 */
	public boolean isTickExpression(String val);

	/**
	 * Returns the most local clock for the given object.
	 * @param P an item, room, mob, whatever.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock
	 * @return the local clock
	 */
	public TimeClock localClock(Physical P);

	/**
	 * Returns the mud wide global time clock
	 * object as defined by the coffeemud.ini
	 * file.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock
	 * @return the global clock
	 */
	public TimeClock globalClock();

	/** constant for the names of the months in a rl year */
	public final static String[] MONTHS={
		"January","February","March","April","May","June","July","August","September","October","November","December"
	};

	/** constant for the short names of the months in a rl year */
	public final static String[] SHORTMONTHS={
		"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
	};

	/** constant for the number of milliseconds in a second */
	public final static long MILI_SECOND=1000;
	/** constant for the number of milliseconds in a minute */
	public final static long MILI_MINUTE=MILI_SECOND*60;
	/** constant for the number of milliseconds in a rl hour */
	public final static long MILI_HOUR=MILI_MINUTE*60;
	/** constant for the number of milliseconds in a rl day */
	public final static long MILI_DAY=MILI_HOUR*24;
	/** constant for the number of milliseconds in a rl week */
	public final static long MILI_WEEK=MILI_DAY*7;
	/** constant for the number of milliseconds in a rl month */
	public final static long MILI_MONTH=MILI_DAY*30;
	/** constant for the number of milliseconds in a rl year */
	public final static long MILI_YEAR=MILI_DAY*365;
}
