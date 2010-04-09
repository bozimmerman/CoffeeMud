package com.planet_ink.coffee_mud.Libraries;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;


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
public class CoffeeTime extends StdLibrary implements TimeManager
{
    public String ID(){return "CoffeeTime";}
    protected TimeClock globalClock=null;
    /**
     * Returns the numeric representation of the month
     *
     * <br><br><b>Usage:</b> Month2MM("January");
     * @param Month The month name
     * @return String The number of the month as a string
     */
    public String month2MM(String Month)
    {
    	for(int m=0;m<MONTHS.length;m++)
    		if(Month.equals(MONTHS[m]))
    			if(m<9)
	    			return "0"+(m+1);
    			else
    				return String.valueOf(m+1);
        return "01";
    }

    /**
     * Return the name of the month, given a number
     *
     * <br><br><b>Usage:</b> String Mnth=ReturnMonthName(m,GiveShort).charStats();
     * @param Number Month number to convert
     * @param GiveShort Give abbreviation if true
     * @return String Month name
     */
    public String getMonthName(int number, boolean giveShort)
    {
    	if(number<=0)
    		number=1;
    	else
        if(number>12)
        	number=(number%12)+1;

        if(!giveShort)
        	return MONTHS[number-1];
        else
        	return SHORTMONTHS[number-1];
    }


    /**
     * Converts a string of some form into a Calendar object.
     *
     * <br><br><b>Usage:</b> Calendar.S2Date(GetRes(Results,"StartDateTime"));
     * @param TheDate The string to retrieve from
     * @return Calendar Calendar object
     */
    public long string2Millis(String TheDate)
    {
        Calendar C=string2Date(TheDate);
        if(C!=null) return C.getTimeInMillis();
        return 0;
    }

    /**
     * Converts a string of some form into a Calendar object.
     *
     * <br><br><b>Usage:</b> Calendar.string2Date(GetRes(Results,"StartDateTime"));
     * @param TheDate The string to retrieve from
     * @return Calendar Calendar object
     */
    public Calendar string2Date(String TheDate)
    {
        Calendar D=Calendar.getInstance();

        if(TheDate==null)
            return D;
        if(TheDate.trim().length()==0)
            return D;
        // for those stupid SQLServer date formats, clean them up!
        if((TheDate.indexOf(".")==19)
        ||((TheDate.indexOf("-")==4)&&(TheDate.indexOf(":")==13)))
        {
            //String TheOldDate=TheDate;
            int HH=CMath.s_int(TheDate.substring(11,13));
            int MM=CMath.s_int(TheDate.substring(14,16));
            int AP=Calendar.AM;
            if(TheDate.trim().endsWith("PM"))
                AP=Calendar.PM;
            else
            if(TheDate.trim().endsWith("AM"))
                AP=Calendar.AM;
            else
            if(HH==0)
            {
                HH=12;
                AP=Calendar.AM;
            }
            else
            if(HH>12)
            {
                HH=HH-12;
                AP=Calendar.PM;
            }
            else
            if(TheDate.toUpperCase().substring(10).indexOf("P")>=0)
                AP=Calendar.PM;
            else
            if(TheDate.toUpperCase().substring(10).indexOf("A")>=0)
                AP=Calendar.AM;
            else
            if(HH==12) // as 12 always means 12 noon in international date/time -- 0 = 12am
                AP=Calendar.PM;

            if((AP==Calendar.PM)&&(HH==12))
                D.set(Calendar.HOUR,0);
            else
            if((AP==Calendar.AM)&&(HH==12))
                D.set(Calendar.HOUR,0);
            else
                D.set(Calendar.HOUR,HH);

            D.set(Calendar.AM_PM,AP);
            D.set(Calendar.MINUTE,MM);
            D.set(Calendar.SECOND,0);
            D.set(Calendar.MILLISECOND,0);

            int YY=CMath.s_int(TheDate.substring(0,4));
            D.set(Calendar.YEAR,YY);
            int MN=CMath.s_int(TheDate.substring(5,7));
            D.set(Calendar.MONTH,MN-1);
            int DA=CMath.s_int(TheDate.substring(8,10));
            D.set(Calendar.DATE,DA);
            D.set(Calendar.AM_PM,AP);
        }
        else
        {
            // If it has no time, give it one!
            if((TheDate.indexOf(":")<0)
            &&(TheDate.indexOf("AM")<0)
            &&(TheDate.indexOf("PM")<0))
                TheDate=TheDate+" 5:00 PM";

            try
            {
                DateFormat fmt=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
                fmt.parse(TheDate);
                D=fmt.getCalendar();
                D.set(Calendar.SECOND,0);
                D.set(Calendar.MILLISECOND,0);
            }
            catch(ParseException e)
            { }
        }
        confirmDateAMPM(TheDate,D);
        return D;
    }

    public boolean isValidDateString(String TheDate)
    {
        if(TheDate==null)
            return false;
        if(TheDate.trim().length()==0)
            return false;
        // for those stupid SQLServer date formats, clean them up!
        if((TheDate.indexOf(".")==19)
        ||((TheDate.indexOf("-")==4)&&(TheDate.indexOf(":")==13)))
        {
            //String TheOldDate=TheDate;
        	if(!CMath.isInteger(TheDate.substring(11,13)))
        		return false;
        	if(!CMath.isInteger(TheDate.substring(14,16)))
        		return false;
        	if(!CMath.isInteger(TheDate.substring(0,4)))
        		return false;
        	if(!CMath.isInteger(TheDate.substring(5,7)))
        		return false;
        	if(!CMath.isInteger(TheDate.substring(8,10)))
        		return false;
        }
        else
        {
            // If it has no time, give it one!
            if((TheDate.indexOf(":")<0)
            &&(TheDate.indexOf("AM")<0)
            &&(TheDate.indexOf("PM")<0))
                TheDate=TheDate+" 5:00 PM";
            try
            {
                DateFormat fmt=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
                fmt.parse(TheDate);
            }
            catch(ParseException e)
            { return false; }
        }
        return true;
    }
    
    private void confirmDateAMPM(String TheDate, Calendar D)
    {
        try
        {
            if(TheDate.trim().endsWith("PM"))
            {
                if(D.get(Calendar.AM_PM)==Calendar.AM)
                    D.set(Calendar.AM_PM,Calendar.PM);
                if(D.get(Calendar.AM_PM)==Calendar.AM)
                    D.add(Calendar.HOUR,12);
                if(D.get(Calendar.AM_PM)==Calendar.AM)
                    D.setTimeInMillis(D.getTimeInMillis()+(12*60*60*1000));
            }
            else
            if(TheDate.trim().endsWith("AM"))
            {
                if(D.get(Calendar.AM_PM)==Calendar.PM)
                    D.set(Calendar.AM_PM,Calendar.AM);
                if(D.get(Calendar.AM_PM)==Calendar.PM)
                    D.add(Calendar.HOUR,-12);
                if(D.get(Calendar.AM_PM)==Calendar.PM)
                    D.setTimeInMillis(D.getTimeInMillis()-(12*60*60*1000));
            }
        }
        catch(Exception e)
        { }
    }


    /**
     * Returns the regular Hours given the hours in the
     * international format (military time)
     *
     * <br><br><b>Usage:</b> ConvertHour(GetIn(req, "ENDHR"))
     * @param TheHour Hours in military format
     * @return String Hours in regular format
     **/
    public String convertHour(String TheHour)
    {
        int IntHour =  CMath.s_int(TheHour);
        if (IntHour > 12)
        {
            IntHour = IntHour-12;
        }
        else
            if (IntHour == 0)
                IntHour = 12;

        TheHour = Integer.toString(IntHour);
        return TheHour;
    }

    /**
     * Returns the AMPM stamp given the international Hours portion the Time
     *
     * <br><br><b>Usage:</b> getAMPM(GetIn(req, "ENDHR"))
     * @param TheHour Hours in military format
     * @return String AM or PM stamp
     **/
    public String getAMPM(String TheHour)
    {
        String Stamp;

        int IntHour =  CMath.s_int(TheHour);
        if (IntHour >= 12)
            Stamp = "PM";
        else
            Stamp = "AM";
        return Stamp;
    }

    /**
     * Get the zone id given the timezone string
     *
     * <br><br><b>Usage:</b> GetTheZoneID(MeetTZ.getRawOffset())+"\n";
     * @param theRawOffset The time zone's raw offset to convert
     * @return String The time zone ID
     */
    public String getTheIntZoneID(int theRawOffset)
    {
        if (theRawOffset == 0)          // GMT 0
            return "GMT";
        if (theRawOffset == 3600000)    // GMT 1
            return "CET";
        if (theRawOffset == 7200000)    // GMT 2
            return "CAT";
        if (theRawOffset == 10800000)   // GMT 3
            return "EAT";
        if (theRawOffset == 12600000)   // GMT 3.5
            return "MET";
        if (theRawOffset == 14400000)   // GMT 4
            return "NET";
        if (theRawOffset == 18000000)   // GMT 5
            return "PLT";
        if (theRawOffset == 19800000)   // GMT 5.5
            return "IST";
        if (theRawOffset == 21600000)   // GMT 6
            return "BST";
        if (theRawOffset == 25200000)   // GMT 7
            return "VST";
        if (theRawOffset == 28800000)   // GMT 8
            return "CTT";
        if (theRawOffset == 32400000)   // GMT 9
            return "JST";
        if (theRawOffset == 34200000)   // GMT 9.5
            return "ACT";
        if (theRawOffset == 36000000)   // GMT 10
            return "AET";
        if (theRawOffset == 39600000)   // GMT 11
            return "SST";
        if (theRawOffset == 43200000)   // GMT 12
            return "NST";
        if (theRawOffset == -39600000)  // GMT -11
            return "MIT";
        if (theRawOffset == -36000000)  // GMT -10
            return "HST";
        if (theRawOffset == -32400000)  // GMT -9
            return "AST";
        if (theRawOffset == -28800000)  // GMT -8
            return "PST";
        if (theRawOffset == -25200000)  // GMT -7
            return "MST";
        if (theRawOffset == -21600000)  // GMT -6
            return "CST";
        if (theRawOffset == -18000000)  // GMT -5
            return "EST";
        if (theRawOffset == -14400000)  // GMT -4
            return "ADT";
        if (theRawOffset == -12600000)  // GMT -3.5
            return "CNT";
        if (theRawOffset == -10800000)  // GMT -3
            return "AGT";
        if (theRawOffset == -7200000)   // GMT -2
            return "BET";
        if (theRawOffset == -3600000)   // GMT -1
            return "EET";

        return "GMT";

    }

    /**
     *  Returns the time zone of the given ID
     *
     * <br><br><b>Usage:</b> MEETZN = GetTheTimeZone(ID);
     * @param theID The ID of the abbreviated time zone.
     * @return String The time zone name
     */
    public String getTheTimeZone(String theID)
    {
        if (theID.equalsIgnoreCase("CET"))
            return "Europe/Paris";
        if (theID.equalsIgnoreCase("ADT"))
            return "America/Halifax";
        if (theID.equalsIgnoreCase("BET"))
            return "Atlantic/South_Georgia";
        if (theID.equalsIgnoreCase("EET"))
            return "Atlantic/Azores";
        if (theID.equalsIgnoreCase("CAT"))
            return "Europe/Athens";
        if (theID.equalsIgnoreCase("EAT"))
            return "Asia/Riyadh";

        return theID;
    }

    /**
     * Returns the month name for a given date
     *
     * <br><br><b>Usage:</b> String ENDMM=d2MMString();
     * @param time The time in miliseconds
     * @return String The month name
     **/
    public String date2MonthString(long time, boolean shortName)
    {
        Calendar C=makeCalendar(time);
        return getMonthName(C.get(Calendar.MONTH)+1,shortName);
    }

    /**
     * Returns the month/day string for a given date
     *
     * <br><br><b>Usage:</b> String ENDMM=d2MMString();
     * @param time The time in miliseconds
     * @return String The month/day name
     **/
    public String date2MonthDateString(long time, boolean shortName)
    {
        Calendar C=makeCalendar(time);
        return getMonthName(C.get(Calendar.MONTH)+1,shortName) + " " + C.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Returns the DD portion of a given date
     *
     * <br><br><b>Usage:</b> String ENDDD=d2DDString();
     * @param time The time in miliseconds
     * @return String The day
     **/
    public String date2DayOfMonthString(long time)
    {
        Calendar C=makeCalendar(time);
        String Day=Integer.toString(C.get(Calendar.DAY_OF_MONTH)).trim();
        if (Day.length()==1)
            Day = "0" + Day;
        return Day;
    }

    /**
     * Converts a number to two digits.
     * @param num the number
     * @return the number as two digits
     */
    public String twoDigits(long num)
    {
       String s=Long.toString(num);
       if(s.length()==1) return "0"+s;
       return s;
    }

    /**
    * Returns the YYYY portion of a given date
    * Returns the DD portion of a given date
    *
    * <br><br><b>Usage:</b> String ENDYYYY=d2YYYYString();
    * @param time The time in miliseconds
    * @return String The year
    **/
    public String date2YYYYString(long time)
    {
        Calendar C=makeCalendar(time);
        String Year=Integer.toString(C.get(Calendar.YEAR)).trim();
        if (Year.length()==2)
            Year = "20" + Year;
        return Year;
    }

    /**
    * Returns the Hours portion of a given Time
    *
    * <br><br><b>Usage:</b> String ENDHR=T2HRString();
    * @param time The time in miliseconds
    * @return String The hour
    **/
    public String date2HRString(long time)
    {
    	return date2HRString(makeCalendar(time));
    }


    /**
    * Returns the Minutes portion of a given Time
    *
    * <br><br><b>Usage:</b> String ENDMIN=T2MINString();
    * @param time The time in miliseconds
    * @return String The minutes
    **/
    public String date2MINString(long time)
    {
    	return date2MINString(makeCalendar(time));
    }


    /**
    * Returns the Hours portion of a given Time
    *
    * <br><br><b>Usage:</b> String ENDHR=T2HRString();
    * @param time The time in miliseconds
    * @return String The hour
    **/
    public String date2HRString(Calendar C)
    {
        int IntHour = C.get(Calendar.HOUR);
        if (IntHour==0)
            IntHour=12;

        String StrHour=Integer.toString(IntHour);
        if (StrHour.length()==1)
            StrHour = "0" + StrHour;
        return StrHour;
    }


    /**
    * Returns the Minutes portion of a given Time
    *
    * <br><br><b>Usage:</b> String ENDMIN=T2MINString();
    * @param time The time in miliseconds
    * @return String The minutes
    **/
    public String date2MINString(Calendar C)
    {
        int IntMin = C.get(Calendar.MINUTE);
        int remainder = IntMin % 5;
        if (remainder != 0)
        {
            if (remainder >= 3)
            {
                IntMin = IntMin + (5 - remainder);
                if (IntMin == 60)
                    IntMin = 55;
            }
            else
                IntMin = IntMin - remainder;
        }
        String StrMin=Integer.toString(IntMin);
        if (StrMin.length()==1)
            StrMin = "0" + StrMin;
        return StrMin;
    }
    
    /**
     *  Returns the time zone of the server
     *
     * <br><br><b>Usage:</b> MEETZN = T2ZoneString();
     * @param time The time in miliseconds
     * @return String The time zone
     */
    public String date2ZoneString(long time)
    {
        Calendar C=makeCalendar(time);
        TimeZone CurrentZone;
        CurrentZone = C.getTimeZone();
        String theID = CurrentZone.getID();
        theID = getTheIntZoneID(CurrentZone.getRawOffset());

        return  theID;
    }

    /**
     * Returns the Minutes portion of a given Time
     *
     * <br><br><b>Usage:</b> String ST_AMPM=date2AMPMString(time);
     * @param time The time in miliseconds
     * @return String AM or PM stamp
     **/
    public String date2AMPMString(long time)
    {
    	return date2AMPMString(makeCalendar(time));
    }

    /**
     * Returns the Minutes portion of a given Time
     *
     * <br><br><b>Usage:</b> String ST_AMPM=date2AMPMString(time);
     * @param time The time in miliseconds
     * @return String AM or PM stamp
     **/
    public String date2AMPMString(Calendar C)
    {
        if (C.get(Calendar.AM_PM)==Calendar.PM)
            return "PM";
        else
        	return "AM";
    }

    /**
     * Returns the time portion of a given Time
     *
     * <br><br><b>Usage:</b> String ST_AMPM=date2APTimeString(time);
     * @param time The time in ampm format
     * @return String AM or PM stamp
     **/
    public String date2APTimeString(long time)
    {
        Calendar C=makeCalendar(time);
        return date2HRString(C)+":"+date2MINString(C)+" "+date2AMPMString(C);
    }
    
    private Calendar makeCalendar(long time)
    {
        Calendar C=Calendar.getInstance();
        C.setTimeInMillis(time);
        return C;
    }
    public String date2String(Calendar C)
    {
        String MINUTE=Integer.toString(C.get(Calendar.MINUTE)).trim();
        if(MINUTE.length()==1)
            MINUTE="0"+MINUTE;
        String AMPM="AM";
        if(C.get(Calendar.AM_PM)==Calendar.PM)
            AMPM="PM";
        int Hour=C.get(Calendar.HOUR);
        if(Hour==0) Hour=12;
        String Year=Integer.toString(C.get(Calendar.YEAR));
        if(Year.length()<4)
        {
            if(Year.length()<2)
                Year=("0"+Year);
            if(Year.length()<2)
                Year=("0"+Year);
            int Yr=CMath.s_int(Year);
            if(Yr<50)Year="20"+Year;
            else Year="19"+Year;
        }
        return (C.get(Calendar.MONTH)+1)+"/"+C.get(Calendar.DATE)+"/"+Year+" "+Hour+":"+MINUTE+" "+AMPM;
    }
    /**
     * Converts a given date into a string of form:
     * MM/DD/YYYY HH:MM AP
     *
     * <br><br><b>Usage:</b> d2String()
     * @param time The time in miliseconds
     * @return String Formatted date/time
     */
    public String date2String(long time)
    {
        Calendar C=makeCalendar(time);
        return date2String(C);
    }


    /**
     * Converts a given date into a string of form:
     * MM/DD/YYYY HH:MM AP
     *
     * <br><br><b>Usage:</b> d2SString()
     * @param time The time in miliseconds
     * @return String Formatted date/time
     */
    public String date2SecondsString(long time)
    {
        Calendar C=makeCalendar(time);
        String StrDate=date2String(C);
        if(StrDate.length()<3) return StrDate;
        return (StrDate.substring(0,StrDate.length()-3)+":"+C.get(Calendar.SECOND)+" "+StrDate.substring(StrDate.length()-2));
    }

    /**
     * Converts a given date into a string of form:
     * MM/DD/YYYY
     *
     * <br><br><b>Usage:</b> d2DString()
     * @param time The time in miliseconds
     * @return String Formatted date
     */
    public String date2DateString(long time)
    {
        String T=date2String(time);
        if(T.indexOf(" ")>0) T=T.substring(0,T.indexOf(" "));
        return T.trim();
    }

    /**
     * Converts a given date into a string of form:
     * MM/DD/YY
     *
     * <br><br><b>Usage:</b> date2Date2String()
     * @param time The time in miliseconds
     * @return String Formatted date
     */
    public String date2Date2String(long time)
    {
        String T=date2DateString(time);
        int x=T.lastIndexOf("/");
        T=T.substring(0,x+1)+T.substring(x+3);
        return T.trim();
    }

    /**
    * format the date
    *
    * <br><br><b>Usage:</b>  msgDateFormat(98374987234)
    * @param time The time in miliseconds
    * @return String The date
    */
    public String smtpDateFormat(long time)
    {
        Calendar senddate=makeCalendar(time);
        String formatted = "hold";

        String Day[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String Month[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul","Aug", "Sep", "Oct", "Nov", "Dec"};
        int dow=senddate.get(Calendar.DAY_OF_WEEK)-1;
        int date=senddate.get(Calendar.DAY_OF_MONTH);
        int m=senddate.get(Calendar.MONTH);
        int y=senddate.get(Calendar.YEAR);
        int h=senddate.get(Calendar.HOUR_OF_DAY);
        int min=senddate.get(Calendar.MINUTE);
        int s=senddate.get(Calendar.SECOND);
        int zof=senddate.get(Calendar.ZONE_OFFSET);
        int dof=senddate.get(Calendar.DST_OFFSET);

        formatted = Day[dow] + ", ";
        formatted = formatted + String.valueOf(date) + " ";
        formatted = formatted + Month[m] + " ";
        formatted = formatted + String.valueOf(y) + " ";
        if (h < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(h) + ":";
        if (min < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(min) + ":";
        if (s < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(s) + " ";
        if ((zof + dof) < 0)
            formatted = formatted + "-";
        else
            formatted = formatted + "+";

        zof=zof/1000; // now in seconds
        zof=zof/60; // now in minutes

        dof=dof/1000; // now in seconds
        dof=dof/60; // now in minutes

        if ((Math.abs(zof + dof)/60) < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(Math.abs(zof + dof)/60);
        if ((Math.abs(zof + dof)%60) < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(Math.abs(zof + dof)%60);

        return formatted;
    }
    
    public TimeClock globalClock()
    {
        if(globalClock==null)
        {
            globalClock=(TimeClock)CMClass.getCommon("DefaultTimeClock");
            if(globalClock!=null) globalClock.setLoadName("GLOBAL");
        }
        return globalClock;
    }
    
    private double getTickExpressionMultiPlier(String lastWord) {
        lastWord=lastWord.toUpperCase().trim();
        if(lastWord.startsWith("MINUTE")||lastWord.equals("MINS")||lastWord.equals("MIN"))
            return CMath.div(TimeManager.MILI_MINUTE,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("SECOND")||lastWord.equals("SECS")||lastWord.equals("SEC"))
            return CMath.div(TimeManager.MILI_SECOND,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("HOUR"))
            return CMath.div(TimeManager.MILI_HOUR,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("DAY")||lastWord.equals("DAYS"))
            return CMath.div(TimeManager.MILI_DAY,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("TICK"))
            return 1.0;
        else
        if(lastWord.startsWith("MUDHOUR"))
            return CMath.div(TimeClock.TIME_MILIS_PER_MUDHOUR,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("MUDDAY"))
            return CMath.div(TimeClock.TIME_MILIS_PER_MUDHOUR
                    *globalClock().getHoursInDay()
                    ,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("MUDWEEK"))
            return CMath.div(TimeClock.TIME_MILIS_PER_MUDHOUR
                    *globalClock().getHoursInDay()
                    *globalClock().getDaysInWeek()
                    ,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("MUDMONTH"))
            return CMath.div(TimeClock.TIME_MILIS_PER_MUDHOUR
                    *globalClock().getHoursInDay()
                    *globalClock().getDaysInMonth()
                    ,Tickable.TIME_TICK_DOUBLE);
        else
        if(lastWord.startsWith("MUDYEAR"))
            return CMath.div(TimeClock.TIME_MILIS_PER_MUDHOUR
                    *globalClock().getHoursInDay()
                    *globalClock().getDaysInMonth()
                    *globalClock().getMonthsInYear()
                    ,Tickable.TIME_TICK_DOUBLE);
        return 0.0;
    }

    public boolean isTickExpression(String val) {
        val=val.trim();
        if(CMath.isMathExpression(val)) return true;
        int x=val.lastIndexOf(' ');
        if(x<0) return CMath.isMathExpression(val);
        double multiPlier=getTickExpressionMultiPlier(val.substring(x+1));
        if(multiPlier==0.0) return CMath.isMathExpression(val);
        return CMath.isMathExpression(val.substring(0,x).trim());
    }

    
    public int parseTickExpression(String val) {
        val=val.trim();
        if(CMath.isMathExpression(val))
            return CMath.s_parseIntExpression(val);
        int x=val.lastIndexOf(' ');
        if(x<0) return CMath.s_parseIntExpression(val);
        double multiPlier=getTickExpressionMultiPlier(val.substring(x+1));
        if(multiPlier==0.0) return CMath.s_parseIntExpression(val);
        return (int)Math.round(CMath.mul(multiPlier,CMath.s_parseIntExpression(val.substring(0,x).trim())));
    }
}
