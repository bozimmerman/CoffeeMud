package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.CoffeeMudException;
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

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

/*
   Copyright 2005-2024 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CoffeeTime";
	}

	protected TimeClock	globalClock	= null;

	public static enum TimeDelta
	{
		MINUTE("MINUTES","MINS","MIN","M"),
		SECOND("SECONDS","SECS","SEC","S"),
		HOUR("HOURS","HRS","HR","H"),
		DAY("DAYS","D"),
		WEEK("WEEKS","W","WKS","WK"),
		MONTH("MONTHS"),
		YEAR("YEARS"),
		TICK("TICKS","T"),
		MUDHOUR("MUDHOURS","MHRS","MHR","MH"),
		MUDDAY("MUDDAYS","MD"),
		MUDWEEK("MUDWEEKS","MW","MWK","MWKS"),
		MUDMONTH("MUDMONTHS","MM"),
		MUDYEAR("MUDYEARS","MY","MYRS","MYR"),
		MINUTELY(),
		SECONDLY(),
		HOURLY(),
		DAYLY(),
		WEEKLY(),
		MONTHLY(),
		YEARLY()
		;
		private final static Map<String,TimeDelta> all = new Hashtable<String,TimeDelta>();
		private final String[] alts;
		private TimeDelta(final String... alts)
		{
			this.alts = alts;
		}
		public static TimeDelta get(final String str)
		{
			if(str == null)
				return null;
			if(all.size()==0)
			{
				for(final TimeDelta td : values())
				{
					all.put(td.name().toUpperCase().trim(), td);
					for(final String alt : td.alts)
						all.put(alt.toUpperCase().trim(), td);
				}
			}
			return all.get(str.toUpperCase().trim());
		}

		public long delta(TimeClock clock)
		{
			if(clock == null)
				clock = CMLib.time().globalClock();
			final Calendar C = Calendar.getInstance();
			switch(this)
			{
			case DAY:
				return TimeManager.MILI_DAY;
			case DAYLY:
				C.add(Calendar.DATE, 1);
				C.set(Calendar.HOUR_OF_DAY, 0);
				C.set(Calendar.MINUTE,0);
				C.set(Calendar.SECOND,0);
				C.set(Calendar.MILLISECOND,0);
				break;
			case HOUR:
				return TimeManager.MILI_HOUR;
			case HOURLY:
				C.add(Calendar.HOUR, 1);
				C.set(Calendar.MINUTE,0);
				C.set(Calendar.SECOND,0);
				C.set(Calendar.MILLISECOND,0);
				break;
			case MINUTE:
				return TimeManager.MILI_MINUTE;
			case MINUTELY:
				C.add(Calendar.MINUTE, 1);
				C.set(Calendar.SECOND,0);
				C.set(Calendar.MILLISECOND,0);
				break;
			case MONTH:
				return TimeManager.MILI_DAY * 30;
			case MONTHLY:
				C.add(Calendar.MONTH, 1);
				C.set(Calendar.DAY_OF_MONTH, 1);
				C.set(Calendar.HOUR_OF_DAY, 0);
				C.set(Calendar.MINUTE,0);
				C.set(Calendar.SECOND,0);
				C.set(Calendar.MILLISECOND,0);
				Log.sysOut(CMLib.time().date2String(C.getTimeInMillis()));
				break;
			case MUDDAY:
				return CMProps.getMillisPerMudHour()*clock.getHoursInDay();
			case MUDHOUR:
				return CMProps.getMillisPerMudHour();
			case MUDMONTH:
				return CMProps.getMillisPerMudHour()*clock.getHoursInDay()*clock.getDaysInMonth();
			case MUDWEEK:
				return CMProps.getMillisPerMudHour()*clock.getHoursInDay()*clock.getDaysInWeek();
			case MUDYEAR:
				return CMProps.getMillisPerMudHour()*clock.getHoursInDay()
						*clock.getDaysInMonth()*clock.getMonthsInYear();
			case SECOND:
				return TimeManager.MILI_SECOND;
			case SECONDLY:
				C.add(Calendar.SECOND, 1);
				C.set(Calendar.MILLISECOND,0);
				break;
			case TICK:
				return CMProps.getTickMillis();
			case WEEK:
				return TimeManager.MILI_DAY * 7;
			case WEEKLY:
				C.add(Calendar.WEEK_OF_YEAR, 1);
				break;
			case YEAR:
				return TimeManager.MILI_DAY * 365;
			case YEARLY:
				C.add(Calendar.YEAR, 1);
				C.set(Calendar.MONTH, 0);
				C.set(Calendar.DAY_OF_MONTH, 1);
				C.set(Calendar.HOUR_OF_DAY, 0);
				C.set(Calendar.MINUTE,0);
				C.set(Calendar.SECOND,0);
				C.set(Calendar.MILLISECOND,0);
				break;
			}
			return C.getTimeInMillis() - System.currentTimeMillis();
		}
	}

	@Override
	public String month2MM(final String monthName)
	{
		for(int m=0;m<MONTHS.length;m++)
		{
			if(monthName.equals(MONTHS[m]))
			{
				if(m<9)
					return "0"+(m+1);
				else
					return String.valueOf(m+1);
			}
		}
		return "01";
	}

	@Override
	public String getMonthName(int number, final boolean giveShort)
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

	@Override
	public long string2Millis(final String dateTimeStr)
	{
		final Calendar C=string2Date(dateTimeStr);
		if(C!=null)
			return C.getTimeInMillis();
		return 0;
	}

	@Override
	public Calendar string2Date(String dateTimeStr)
	{
		Calendar D=Calendar.getInstance();

		if(dateTimeStr==null)
			return D;
		if(dateTimeStr.trim().length()==0)
			return D;
		// for those stupid SQLServer date formats, clean them up!
		if((dateTimeStr.indexOf('.')==19)
		||((dateTimeStr.indexOf('-')==4)&&(dateTimeStr.indexOf(':')==13)))
		{
			//String TheOldDate=TheDate;
			int HH=CMath.s_int(dateTimeStr.substring(11,13));
			final int MM=CMath.s_int(dateTimeStr.substring(14,16));
			int AP=Calendar.AM;
			if(dateTimeStr.trim().endsWith("PM"))
				AP=Calendar.PM;
			else
			if(dateTimeStr.trim().endsWith("AM"))
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
			if(dateTimeStr.toUpperCase().substring(10).indexOf('P')>=0)
				AP=Calendar.PM;
			else
			if(dateTimeStr.toUpperCase().substring(10).indexOf('A')>=0)
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

			final int YY=CMath.s_int(dateTimeStr.substring(0,4));
			D.set(Calendar.YEAR,YY);
			final int MN=CMath.s_int(dateTimeStr.substring(5,7));
			D.set(Calendar.MONTH,MN-1);
			final int DA=CMath.s_int(dateTimeStr.substring(8,10));
			D.set(Calendar.DATE,DA);
			D.set(Calendar.AM_PM,AP);
		}
		else
		{
			// If it has no time, give it one!
			if((dateTimeStr.indexOf(':')<0)
			&&(dateTimeStr.indexOf("AM")<0)
			&&(dateTimeStr.indexOf("PM")<0))
				dateTimeStr=dateTimeStr+" 5:00 PM";

			try
			{
				final DateFormat fmt=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,Locale.US);
				fmt.parse(dateTimeStr);
				D=fmt.getCalendar();
				D.set(Calendar.SECOND,0);
				D.set(Calendar.MILLISECOND,0);
			}
			catch(final ParseException e)
			{
			}
		}
		confirmDateAMPM(dateTimeStr,D);
		return D;
	}


	@Override
	public Calendar string2TimeFuture(String timeStr)
	{
		if(timeStr==null)
			return null;
		if(timeStr.trim().length()==0)
			return null;
		timeStr=timeStr.toUpperCase();
		int hrNum=0;
		int hrDex=0;
		while((hrDex<timeStr.length())&&(Character.isDigit(timeStr.charAt(hrDex))))
			hrDex++;
		if(hrDex==0)
			return null;
		hrNum=CMath.s_int(timeStr.substring(0,hrDex));
		// If it has no time, give it one!
		final Calendar todayD=Calendar.getInstance();
		if(timeStr.indexOf(':')<0)
		{
			if(hrDex==timeStr.length())
				timeStr += ":00";
			else
			if(timeStr.charAt(hrDex)==' ')
				timeStr=timeStr.substring(0,hrDex)+":00"+timeStr.substring(hrDex);
			else
				timeStr=timeStr.substring(0,hrDex)+":00 "+timeStr.substring(hrDex);
		}
		if((timeStr.indexOf("AM")<0)
		&&(timeStr.indexOf("PM")<0))
		{
			int incomDex=timeStr.indexOf('A');
			if(incomDex<0)
				incomDex=timeStr.indexOf('P');
			if(incomDex>0)
				timeStr=timeStr.substring(0,incomDex+1)+"M"+timeStr.substring(incomDex+1);
			else
			if(hrNum==12)
			{
				if(todayD.get(Calendar.HOUR)!=0)
					timeStr += todayD.get(Calendar.AM_PM)==Calendar.AM?" PM":" AM";
				else
					timeStr += todayD.get(Calendar.AM_PM)==Calendar.AM?" AM":" PM";
			}
			else
			if(hrNum >= todayD.get(Calendar.HOUR)+1)
				timeStr += todayD.get(Calendar.AM_PM)==Calendar.AM?" AM":" PM";
			else
				timeStr += todayD.get(Calendar.AM_PM)==Calendar.AM?" PM":" AM";
		}
		int apDex = timeStr.indexOf("AM");
		if(apDex < 0)
			apDex = timeStr.indexOf("PM");
		if(apDex <= 0)
			return null;
		if(timeStr.charAt(apDex-1)!= ' ')
			timeStr=timeStr.substring(0,apDex)+" "+timeStr.substring(apDex);


		Calendar D=null;
		try
		{
			final DateFormat fmt=DateFormat.getTimeInstance(DateFormat.SHORT);
			fmt.parse(timeStr);
			D=fmt.getCalendar();
			D.set(Calendar.SECOND,0);
			D.set(Calendar.MILLISECOND,0);
			D.set(Calendar.YEAR, todayD.get(Calendar.YEAR));
			D.set(Calendar.MONTH, todayD.get(Calendar.MONTH));
			D.set(Calendar.DAY_OF_MONTH, todayD.get(Calendar.DAY_OF_MONTH));
			if(todayD.compareTo(D) > 0)
				D.add(Calendar.DATE, 1);
		}
		catch(final ParseException e)
		{
			return null;
		}
		confirmDateAMPM(timeStr,D);
		return D;
	}

	@Override
	public boolean isValidDateString(String dateTimeStr)
	{
		if(dateTimeStr==null)
			return false;
		if(dateTimeStr.trim().length()==0)
			return false;
		// for those stupid SQLServer date formats, clean them up!
		if((dateTimeStr.indexOf('.')==19)
		||((dateTimeStr.indexOf('-')==4)&&(dateTimeStr.indexOf(':')==13)))
		{
			//String TheOldDate=TheDate;
			if(!CMath.isInteger(dateTimeStr.substring(11,13)))
				return false;
			if(!CMath.isInteger(dateTimeStr.substring(14,16)))
				return false;
			if(!CMath.isInteger(dateTimeStr.substring(0,4)))
				return false;
			if(!CMath.isInteger(dateTimeStr.substring(5,7)))
				return false;
			if(!CMath.isInteger(dateTimeStr.substring(8,10)))
				return false;
		}
		else
		{
			// If it has no time, give it one!
			if((dateTimeStr.indexOf(':')<0)
			&&(dateTimeStr.indexOf("AM")<0)
			&&(dateTimeStr.indexOf("PM")<0))
				dateTimeStr=dateTimeStr+" 5:00 PM";
			try
			{
				final DateFormat fmt=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,Locale.US);
				fmt.parse(dateTimeStr);
			}
			catch(final ParseException e)
			{
				return false;
			}
		}
		return true;
	}

	private void confirmDateAMPM(final String TheDate, final Calendar D)
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
		catch(final Exception e)
		{
		}
	}

	@Override
	public String convertHour(String hours24)
	{
		int hour =  CMath.s_int(hours24);
		if (hour > 12)
		{
			hour = hour-12;
		}
		else
		if (hour == 0)
			hour = 12;

		hours24 = Integer.toString(hour);
		return hours24;
	}

	@Override
	public String getAMPM(final String TheHour)
	{
		String stamp;

		final int hour =  CMath.s_int(TheHour);
		if (hour >= 12)
			stamp = "PM";
		else
			stamp = "AM";
		return stamp;
	}

	@Override
	public String getTheIntZoneID(final int theRawOffset)
	{
		if (theRawOffset == 0)  		// GMT 0
			return "GMT";
		if (theRawOffset == 3600000)	// GMT 1
			return "CET";
		if (theRawOffset == 7200000)	// GMT 2
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

	@Override
	public String getTheTimeZone(final String theID)
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

	@Override
	public String date2MonthString(final long time, final boolean shortName)
	{
		final Calendar C=makeCalendar(time);
		return getMonthName(C.get(Calendar.MONTH)+1,shortName);
	}

	@Override
	public String date2MonthDateString(final long time, final boolean shortName)
	{
		final Calendar C=makeCalendar(time);
		return getMonthName(C.get(Calendar.MONTH)+1,shortName) + " " + C.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public String date2DayOfMonthString(final long time)
	{
		final Calendar C=makeCalendar(time);
		String day=Integer.toString(C.get(Calendar.DAY_OF_MONTH)).trim();
		if (day.length()==1)
			day = "0" + day;
		return day;
	}

	@Override
	public String twoDigits(final long num)
	{
		final String s=Long.toString(num);
		if(s.length()==1)
			return "0"+s;
		return s;
	}

	@Override
	public String date2YYYYString(final long time)
	{
		final Calendar C=makeCalendar(time);
		String year=Integer.toString(C.get(Calendar.YEAR)).trim();
		if (year.length()==2)
			year = "20" + year;
		return year;
	}

	@Override
	public String date2HRString(final long time)
	{
		return date2HRString(makeCalendar(time));
	}

	@Override
	public String date2MINString(final long time)
	{
		return date2MINString(makeCalendar(time));
	}

	public String date2HRString(final Calendar C)
	{
		int hour = C.get(Calendar.HOUR);
		if (hour==0)
			hour=12;

		String hourStr=Integer.toString(hour);
		if (hourStr.length()==1)
			hourStr = "0" + hourStr;
		return hourStr;
	}

	public String date2MINString(final Calendar C)
	{
		int min = C.get(Calendar.MINUTE);
		final int remainder = min % 5;
		if (remainder != 0)
		{
			if (remainder >= 3)
			{
				min = min + (5 - remainder);
				if (min == 60)
					min = 55;
			}
			else
				min = min - remainder;
		}
		String minStr=Integer.toString(min);
		if (minStr.length()==1)
			minStr = "0" + minStr;
		return minStr;
	}

	@Override
	public String date2ZoneString(final long time)
	{
		final Calendar C=makeCalendar(time);
		TimeZone curZone;
		curZone = C.getTimeZone();
		String theID = curZone.getID();
		theID = getTheIntZoneID(curZone.getRawOffset());

		return  theID;
	}

	@Override
	public String date2AMPMString(final long time)
	{
		return date2AMPMString(makeCalendar(time));
	}

	public String date2AMPMString(final Calendar C)
	{
		if (C.get(Calendar.AM_PM)==Calendar.PM)
			return "PM";
		else
			return "AM";
	}

	@Override
	public String date2APTimeString(final long time)
	{
		final Calendar C=makeCalendar(time);
		return date2HRString(C)+":"+date2MINString(C)+" "+date2AMPMString(C);
	}

	@Override
	public String date2BriefString(final long time)
	{
		final Calendar C=makeCalendar(time);
		final Calendar nowC=Calendar.getInstance();
		final StringBuilder str=new StringBuilder();
		if((nowC.get(Calendar.YEAR)!=C.get(Calendar.YEAR))
		||(nowC.get(Calendar.MONTH)!=C.get(Calendar.MONTH))
		||(nowC.get(Calendar.DATE)!=C.get(Calendar.DATE)))
			str.append(C.get(Calendar.YEAR)).append("/").append(C.get(Calendar.MONTH)+1).append("/").append(C.get(Calendar.DATE)).append(" ");
		str.append(date2HRString(C)).append(":").append(date2MINString(C)).append(date2AMPMString(C).toLowerCase());
		return str.toString();
	}

	private Calendar makeCalendar(final long time)
	{
		final Calendar C=Calendar.getInstance();
		C.setTimeInMillis(time);
		return C;
	}

	@Override
	public String date2String(final Calendar C)
	{
		String minute=Integer.toString(C.get(Calendar.MINUTE)).trim();
		if(minute.length()==1)
			minute="0"+minute;
		String ampm="AM";
		if(C.get(Calendar.AM_PM)==Calendar.PM)
			ampm="PM";
		int hour=C.get(Calendar.HOUR);
		if(hour==0)
			hour=12;
		String year=Integer.toString(C.get(Calendar.YEAR));
		if(year.length()<4)
		{
			if(year.length()<2)
				year=("0"+year);
			if(year.length()<2)
				year=("0"+year);
			final int Yr=CMath.s_int(year);
			if(Yr<50)
				year="20"+year;
			else year="19"+year;
		}
		return (C.get(Calendar.MONTH)+1)+"/"+C.get(Calendar.DATE)+"/"+year+" "+hour+":"+minute+" "+ampm;
	}

	@Override
	public String date2String(final long time)
	{
		final Calendar C=makeCalendar(time);
		return date2String(C);
	}

	@Override
	public String date2String24(final Calendar C)
	{
		String minute=Integer.toString(C.get(Calendar.MINUTE)).trim();
		if(minute.length()==1)
			minute="0"+minute;
		int hour=C.get(Calendar.HOUR);
		if(C.get(Calendar.AM_PM)==Calendar.PM)
			hour += 12;
		String year=Integer.toString(C.get(Calendar.YEAR));
		if(year.length()<4)
		{
			if(year.length()<2)
				year=("0"+year);
			if(year.length()<2)
				year=("0"+year);
			final int Yr=CMath.s_int(year);
			if(Yr<50)
				year="20"+year;
			else year="19"+year;
		}
		return year+"/"+(C.get(Calendar.MONTH)+1)+"/"+C.get(Calendar.DATE)+" "+hour+":"+minute;
	}

	@Override
	public String date2String24(final long time)
	{
		final Calendar C=makeCalendar(time);
		return date2String24(C);
	}

	@Override
	public String date2EllapsedTime(long time, final TimeUnit minUnit, final boolean shortest)
	{
		final StringBuilder str=new StringBuilder("");
		if(time > (TimeManager.MILI_YEAR))
		{
			final int num=(int)Math.round(CMath.floor(CMath.div(time,TimeManager.MILI_YEAR)));
			time = time - (num *TimeManager.MILI_YEAR);
			str.append(num+(shortest?"y":(" year"+(num!=1?"s":""))));
		}
		if(time > (TimeManager.MILI_MONTH))
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,TimeManager.MILI_MONTH)));
			time = time - (num *TimeManager.MILI_MONTH);
			str.append(num+(shortest?"M":(" month"+(num!=1?"s":""))));
		}
		if(time > (TimeManager.MILI_WEEK))
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,TimeManager.MILI_WEEK)));
			time = time - (num *TimeManager.MILI_WEEK);
			str.append(num+(shortest?"w":(" week"+(num!=1?"s":""))));
		}
		if(time > (TimeManager.MILI_DAY))
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,TimeManager.MILI_DAY)));
			time = time - (num *TimeManager.MILI_DAY);
			str.append(num+(shortest?"d":(" day"+(num!=1?"s":""))));
		}
		if(minUnit == TimeUnit.DAYS)
			return str.toString().trim();
		if(time > (TimeManager.MILI_HOUR))
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,TimeManager.MILI_HOUR)));
			time = time - (num *TimeManager.MILI_HOUR);
			str.append(num+(shortest?"h":(" hour"+(num!=1?"s":""))));
		}
		if(minUnit == TimeUnit.HOURS)
			return str.toString().trim();
		if(time > (TimeManager.MILI_MINUTE))
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,TimeManager.MILI_MINUTE)));
			time = time - (num *TimeManager.MILI_MINUTE);
			str.append(num+(shortest?"m":(" minute"+(num!=1?"s":""))));
		}
		if(minUnit == TimeUnit.MINUTES)
			return str.toString().trim();
		if(time > (TimeManager.MILI_SECOND))
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,TimeManager.MILI_SECOND)));
			time = time - (num*TimeManager.MILI_SECOND);
			str.append(num+(shortest?"s":(" second"+(num!=1?"s":""))));
		}
		if(minUnit == TimeUnit.SECONDS)
			return str.toString().trim();
		if(str.length()>0)
			str.append(shortest?" ":", ");
		return str.append(time+(shortest?"ms":(" millisecond"+(time!=1?"s":"")))).toString().trim();
	}

	@Override
	public String date2BestShortEllapsedTime(long t)
	{
		t=t/1000;
		if(t>60)
		{
			t=t/60;
			if(t>120)
			{
				t=t/60;
				if(t>48)
				{
					t=t/24;
					return t+"d";
				}
				else
					return t+"h";
			}
			else
				return t+"m";
		}
		else
			return t+"s";
	}

	@Override
	public String date2SmartEllapsedTime(final long time, final boolean shortest)
	{
		if(time > TimeManager.MILI_DAY*2)
			return date2EllapsedTime(time,TimeUnit.DAYS,shortest);
		if(time > TimeManager.MILI_HOUR*2)
			return date2EllapsedTime(time,TimeUnit.HOURS,shortest);
		if(time > TimeManager.MILI_MINUTE*2)
			return date2EllapsedTime(time,TimeUnit.MINUTES,shortest);
		if(time > TimeManager.MILI_SECOND*2)
			return date2EllapsedTime(time,TimeUnit.SECONDS,shortest);
		return date2EllapsedTime(time,null,shortest);
	}

	@Override
	public String date2EllapsedMudTime(TimeClock C, long time, final TimeDelta minUnit, final boolean shortest)
	{
		if(C == null)
			C=globalClock();
		final long millisPerHr =CMProps.getMillisPerMudHour();
		final long millisPerDay = millisPerHr * C.getHoursInDay();
		final long millisPerWeek = millisPerDay * (C.getDaysInWeek()<=2?2:C.getDaysInWeek());
		final long millisPerMonth = millisPerDay * C.getDaysInMonth();
		final long millisPerYear = millisPerDay * C.getDaysInYear();
		final StringBuilder str=new StringBuilder("");
		if(time > millisPerYear)
		{
			final int num=(int)Math.round(CMath.floor(CMath.div(time,millisPerYear)));
			time = time - (num *millisPerYear);
			str.append(num+(shortest?"y":(" "+L("year"+(num!=1?"s":"")))));
		}
		if((minUnit == TimeDelta.YEAR)||(time <= 0))
			return str.toString().trim();
		if(time > millisPerMonth)
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,millisPerMonth)));
			time = time - (num *millisPerMonth);
			str.append(num+(shortest?"M":(" "+L("month"+(num!=1?"s":"")))));
		}
		if((minUnit == TimeDelta.MONTH)||(time <= 0))
			return str.toString().trim();
		if(time > millisPerWeek)
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,millisPerWeek)));
			time = time - (num *millisPerWeek);
			str.append(num+(shortest?"w":(" "+L("week"+(num!=1?"s":"")))));
		}
		if((minUnit == TimeDelta.WEEK)||(time <= 0))
			return str.toString().trim();
		if(time > millisPerDay)
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,millisPerDay)));
			time = time - (num *millisPerDay);
			str.append(num+(shortest?"d":(" "+L("day"+(num!=1?"s":"")))));
		}
		if((minUnit == TimeDelta.DAY)||(time <= 0))
			return str.toString().trim();
		if(time > millisPerHr)
		{
			if(str.length()>0)
				str.append(shortest?" ":", ");
			final int num=(int)Math.round(CMath.floor(CMath.div(time,millisPerHr)));
			time = time - (num *millisPerHr);
			return str.append(num+(shortest?"h":(" "+L("hour"+(num!=1?"s":""))))).toString().trim();
		}
		return L("less than an hour");
	}

	@Override
	public String date2SmartEllapsedMudTime(TimeClock C, final long millis, final boolean shortest)
	{
		if(C == null)
			C = globalClock();
		final long millisPerHr =CMProps.getMillisPerMudHour();
		final long millisPerDay = millisPerHr * C.getHoursInDay();
		final long millisPerWeek = millisPerDay * (C.getDaysInWeek()<=2?2:C.getDaysInWeek());
		final long millisPerMonth = millisPerDay * C.getDaysInMonth();
		final long millisPerYear = millisPerDay * C.getDaysInYear();

		if(millis > millisPerYear*2)
			return date2EllapsedMudTime(C, millis,TimeDelta.YEAR,shortest);
		if(millis > millisPerMonth*2)
			return date2EllapsedMudTime(C, millis,TimeDelta.MONTH,shortest);
		if(millis > millisPerWeek*2)
			return date2EllapsedMudTime(C, millis,TimeDelta.WEEK,shortest);
		if(millis > millisPerDay*2)
			return date2EllapsedMudTime(C, millis,TimeDelta.DAY,shortest);
		return date2EllapsedMudTime(C, millis,TimeDelta.HOUR,shortest);
	}

	@Override
	public String date2SecondsString(final long time)
	{
		final Calendar C=makeCalendar(time);
		final String dateStr=date2String(C);
		if(dateStr.length()<3)
			return dateStr;
		return (dateStr.substring(0,dateStr.length()-3)+":"+C.get(Calendar.SECOND)+" "+dateStr.substring(dateStr.length()-2));
	}

	@Override
	public String date2DateString(final long time)
	{
		String dateStr=date2String(time);
		if(dateStr.indexOf(' ')>0)
			dateStr=dateStr.substring(0,dateStr.indexOf(' '));
		return dateStr.trim();
	}

	@Override
	public String date2Date2String(final long time)
	{
		String dateStr=date2DateString(time);
		final int x=dateStr.lastIndexOf('/');
		dateStr=dateStr.substring(0,x+1)+dateStr.substring(x+3);
		return dateStr.trim();
	}

	@Override
	public String smtpDateFormat(final long time)
	{
		final Calendar senddate=makeCalendar(time);
		String formatted = "hold";

		final String daysOfWeek[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
		final String monthsOfyear[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul","Aug", "Sep", "Oct", "Nov", "Dec"};
		final int dow=senddate.get(Calendar.DAY_OF_WEEK)-1;
		final int date=senddate.get(Calendar.DAY_OF_MONTH);
		final int m=senddate.get(Calendar.MONTH);
		final int y=senddate.get(Calendar.YEAR);
		final int h=senddate.get(Calendar.HOUR_OF_DAY);
		final int min=senddate.get(Calendar.MINUTE);
		final int s=senddate.get(Calendar.SECOND);
		int zof=senddate.get(Calendar.ZONE_OFFSET);
		int dof=senddate.get(Calendar.DST_OFFSET);

		formatted = daysOfWeek[dow] + ", ";
		formatted = formatted + String.valueOf(date) + " ";
		formatted = formatted + monthsOfyear[m] + " ";
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

	@Override
	public TimeClock globalClock()
	{
		if(globalClock==null)
		{
			globalClock=(TimeClock)CMClass.getCommon("DefaultTimeClock");
			if(globalClock!=null)
			{
				globalClock.setLoadName("GLOBAL");
				globalClock.tick(null, TICKID_AREA);
			}
		}
		return globalClock;
	}

	@Override
	public boolean isTickExpression(final String val)
	{
		try
		{
			parseTickExpression(CMLib.time().globalClock(), val);
			return true;
		}
		catch(final CMException e)
		{
			return false;
		}
	}

	@Override
	public int parseTickExpression(final TimeClock clock, String val) throws CMException
	{
		val=val.trim();
		if(CMath.isMathExpression(val))
			return CMath.s_parseIntExpression(val);
		final double[] vars = new double[10];
		int curr = 0;
		int lastDigit=0;
		for(int i=0;i<val.length();i++)
		{
			final char c = val.charAt(i);
			final int s=i;
			if(Character.isDigit(c))
			{
				while((i<val.length()) && (Character.isDigit(val.charAt(i))))
					i++;
				lastDigit = CMath.s_int(val.substring(s,i));
				i--;
			}
			else
			if(Character.isLetter(c))
			{
				while((i<val.length()) && (Character.isLetter(val.charAt(i))))
					i++;
				final String word = val.substring(s,i).toUpperCase();
				final TimeDelta delta = TimeDelta.get(word);
				if(delta == null)
					throw new CMException("Unknown word '"+word+"'");
				if(lastDigit>0)
				{
					val=val.substring(0,s)+("* @x"+(1+curr))+val.substring(i);
					i=s+4;
				}
				else
				{
					val=val.substring(0,s)+("@x"+(1+curr))+val.substring(i);
					i=s+3;
				}
				vars[curr++] = delta.delta(clock);
				lastDigit=-1;
			}
			else
			if(!Character.isWhitespace(c))
				lastDigit=-1;
		}
		if(CMath.isMathExpression(val, vars))
			return (int)Math.round(CMath.s_parseMathExpression(val, vars) / CMProps.getTickMillisD());
		throw new CMException("Unknown expression '"+val+"'");
	}

	@Override
	public TimeClock localClock(final Physical P)
	{
		if(P instanceof Area)
		{
			final TimeClock C = ((Area)P).getTimeObj();
			return (C == null)?globalClock():C;
		}
		if(P instanceof Room)
			return localClock(((Room)P).getArea());
		if(P instanceof Item)
			return localClock(((Item)P).owner());
		if(P instanceof MOB)
			return localClock(((MOB)P).location());
		return globalClock();
	}

	@Override
	public TimeClock homeClock(final Physical P)
	{
		if(P instanceof Area)
		{
			final TimeClock C = ((Area)P).getTimeObj();
			return (C == null)?globalClock():C;
		}
		if(P instanceof Room)
			return homeClock(((Room)P).getArea());
		if(P instanceof Item)
			return homeClock(((Item)P).owner());
		if(P instanceof MOB)
		{
			if(((MOB)P).getStartRoom() == null)
				return homeClock(((MOB)P).location());
			else
				return homeClock(((MOB)P).getStartRoom());
		}
		return globalClock();
	}
}
