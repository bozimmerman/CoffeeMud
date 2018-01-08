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
public class CoffeeTime extends StdLibrary implements TimeManager
{
	@Override
	public String ID()
	{
		return "CoffeeTime";
	}

	protected TimeClock	globalClock	= null;

	@Override
	public String month2MM(String monthName)
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

	@Override
	public long string2Millis(String dateTimeStr)
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
				final DateFormat fmt=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
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
				final DateFormat fmt=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
				fmt.parse(dateTimeStr);
			}
			catch(final ParseException e)
			{
				return false;
			}
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
		catch(final Exception e)
		{
		}
	}

	@Override
	public String convertHour(String hours24)
	{
		int IntHour =  CMath.s_int(hours24);
		if (IntHour > 12)
		{
			IntHour = IntHour-12;
		}
		else
		if (IntHour == 0)
			IntHour = 12;

		hours24 = Integer.toString(IntHour);
		return hours24;
	}

	@Override
	public String getAMPM(String TheHour)
	{
		String Stamp;

		final int IntHour =  CMath.s_int(TheHour);
		if (IntHour >= 12)
			Stamp = "PM";
		else
			Stamp = "AM";
		return Stamp;
	}

	@Override
	public String getTheIntZoneID(int theRawOffset)
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

	@Override
	public String date2MonthString(long time, boolean shortName)
	{
		final Calendar C=makeCalendar(time);
		return getMonthName(C.get(Calendar.MONTH)+1,shortName);
	}

	@Override
	public String date2MonthDateString(long time, boolean shortName)
	{
		final Calendar C=makeCalendar(time);
		return getMonthName(C.get(Calendar.MONTH)+1,shortName) + " " + C.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public String date2DayOfMonthString(long time)
	{
		final Calendar C=makeCalendar(time);
		String Day=Integer.toString(C.get(Calendar.DAY_OF_MONTH)).trim();
		if (Day.length()==1)
			Day = "0" + Day;
		return Day;
	}

	@Override
	public String twoDigits(long num)
	{
		final String s=Long.toString(num);
		if(s.length()==1)
			return "0"+s;
		return s;
	}

	@Override
	public String date2YYYYString(long time)
	{
		final Calendar C=makeCalendar(time);
		String Year=Integer.toString(C.get(Calendar.YEAR)).trim();
		if (Year.length()==2)
			Year = "20" + Year;
		return Year;
	}

	@Override
	public String date2HRString(long time)
	{
		return date2HRString(makeCalendar(time));
	}

	@Override
	public String date2MINString(long time)
	{
		return date2MINString(makeCalendar(time));
	}

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

	public String date2MINString(Calendar C)
	{
		int IntMin = C.get(Calendar.MINUTE);
		final int remainder = IntMin % 5;
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

	@Override
	public String date2ZoneString(long time)
	{
		final Calendar C=makeCalendar(time);
		TimeZone CurrentZone;
		CurrentZone = C.getTimeZone();
		String theID = CurrentZone.getID();
		theID = getTheIntZoneID(CurrentZone.getRawOffset());

		return  theID;
	}

	@Override
	public String date2AMPMString(long time)
	{
		return date2AMPMString(makeCalendar(time));
	}

	public String date2AMPMString(Calendar C)
	{
		if (C.get(Calendar.AM_PM)==Calendar.PM)
			return "PM";
		else
			return "AM";
	}

	@Override
	public String date2APTimeString(long time)
	{
		final Calendar C=makeCalendar(time);
		return date2HRString(C)+":"+date2MINString(C)+" "+date2AMPMString(C);
	}

	@Override
	public String date2BriefString(long time)
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

	private Calendar makeCalendar(long time)
	{
		final Calendar C=Calendar.getInstance();
		C.setTimeInMillis(time);
		return C;
	}

	@Override
	public String date2String(Calendar C)
	{
		String MINUTE=Integer.toString(C.get(Calendar.MINUTE)).trim();
		if(MINUTE.length()==1)
			MINUTE="0"+MINUTE;
		String AMPM="AM";
		if(C.get(Calendar.AM_PM)==Calendar.PM)
			AMPM="PM";
		int Hour=C.get(Calendar.HOUR);
		if(Hour==0)
			Hour=12;
		String Year=Integer.toString(C.get(Calendar.YEAR));
		if(Year.length()<4)
		{
			if(Year.length()<2)
				Year=("0"+Year);
			if(Year.length()<2)
				Year=("0"+Year);
			final int Yr=CMath.s_int(Year);
			if(Yr<50)
				Year="20"+Year;
			else Year="19"+Year;
		}
		return (C.get(Calendar.MONTH)+1)+"/"+C.get(Calendar.DATE)+"/"+Year+" "+Hour+":"+MINUTE+" "+AMPM;
	}

	@Override
	public String date2String(long time)
	{
		final Calendar C=makeCalendar(time);
		return date2String(C);
	}

	@Override
	public String date2EllapsedTime(long time, TimeUnit minUnit, boolean shortest)
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
	public String date2SmartEllapsedTime(long time, boolean shortest)
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
	public String date2SecondsString(long time)
	{
		final Calendar C=makeCalendar(time);
		final String StrDate=date2String(C);
		if(StrDate.length()<3)
			return StrDate;
		return (StrDate.substring(0,StrDate.length()-3)+":"+C.get(Calendar.SECOND)+" "+StrDate.substring(StrDate.length()-2));
	}

	@Override
	public String date2DateString(long time)
	{
		String T=date2String(time);
		if(T.indexOf(' ')>0)
			T=T.substring(0,T.indexOf(' '));
		return T.trim();
	}

	@Override
	public String date2Date2String(long time)
	{
		String T=date2DateString(time);
		final int x=T.lastIndexOf('/');
		T=T.substring(0,x+1)+T.substring(x+3);
		return T.trim();
	}

	@Override
	public String smtpDateFormat(long time)
	{
		final Calendar senddate=makeCalendar(time);
		String formatted = "hold";

		final String Day[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
		final String Month[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul","Aug", "Sep", "Oct", "Nov", "Dec"};
		final int dow=senddate.get(Calendar.DAY_OF_WEEK)-1;
		final int date=senddate.get(Calendar.DAY_OF_MONTH);
		final int m=senddate.get(Calendar.MONTH);
		final int y=senddate.get(Calendar.YEAR);
		final int h=senddate.get(Calendar.HOUR_OF_DAY);
		final int min=senddate.get(Calendar.MINUTE);
		final int s=senddate.get(Calendar.SECOND);
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

	@Override
	public TimeClock globalClock()
	{
		if(globalClock==null)
		{
			globalClock=(TimeClock)CMClass.getCommon("DefaultTimeClock");
			if(globalClock!=null)
				globalClock.setLoadName("GLOBAL");
		}
		return globalClock;
	}

	private double getTickExpressionMultiPlier(String lastWord)
	{
		lastWord=lastWord.toUpperCase().trim();
		if(lastWord.startsWith("MINUTE")||lastWord.equals("MINS")||lastWord.equals("MIN"))
			return CMath.div(TimeManager.MILI_MINUTE,CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("SECOND")||lastWord.equals("SECS")||lastWord.equals("SEC"))
			return CMath.div(TimeManager.MILI_SECOND,CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("HOUR"))
			return CMath.div(TimeManager.MILI_HOUR,CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("DAY")||lastWord.equals("DAYS"))
			return CMath.div(TimeManager.MILI_DAY,CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("TICK"))
			return 1.0;
		else
		if(lastWord.startsWith("MUDHOUR"))
			return CMath.div(CMProps.getMillisPerMudHour(),CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("MUDDAY"))
			return CMath.div(CMProps.getMillisPerMudHour()
					*globalClock().getHoursInDay()
					,CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("MUDWEEK"))
			return CMath.div(CMProps.getMillisPerMudHour()
					*globalClock().getHoursInDay()
					*globalClock().getDaysInWeek()
					,CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("MUDMONTH"))
			return CMath.div(CMProps.getMillisPerMudHour()
					*globalClock().getHoursInDay()
					*globalClock().getDaysInMonth()
					,CMProps.getTickMillisD());
		else
		if(lastWord.startsWith("MUDYEAR"))
			return CMath.div(CMProps.getMillisPerMudHour()
					*globalClock().getHoursInDay()
					*globalClock().getDaysInMonth()
					*globalClock().getMonthsInYear()
					,CMProps.getTickMillisD());
		return 0.0;
	}

	@Override
	public boolean isTickExpression(String val)
	{
		val=val.trim();
		if(CMath.isMathExpression(val))
			return true;
		final int x=val.lastIndexOf(' ');
		if(x<0)
			return CMath.isMathExpression(val);
		final double multiPlier=getTickExpressionMultiPlier(val.substring(x+1));
		if(multiPlier==0.0)
			return CMath.isMathExpression(val);
		return CMath.isMathExpression(val.substring(0,x).trim());
	}

	@Override
	public int parseTickExpression(String val)
	{
		val=val.trim();
		if(CMath.isMathExpression(val))
			return CMath.s_parseIntExpression(val);
		final int x=val.lastIndexOf(' ');
		if(x<0)
			return CMath.s_parseIntExpression(val);
		final double multiPlier=getTickExpressionMultiPlier(val.substring(x+1));
		if(multiPlier==0.0)
			return CMath.s_parseIntExpression(val);
		return (int)Math.round(CMath.mul(multiPlier,CMath.s_parseIntExpression(val.substring(0,x).trim())));
	}

	@Override
	public TimeClock localClock(Physical P)
	{
		if(P instanceof Area)
			return ((Area)P).getTimeObj();
		if(P instanceof Room)
			return localClock(((Room)P).getArea());
		if(P instanceof Item)
			return localClock(((Item)P).owner());
		if(P instanceof MOB)
			return localClock(((MOB)P).location());
		return globalClock();
	}
}
