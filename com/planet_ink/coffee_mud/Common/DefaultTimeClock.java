package com.planet_ink.coffee_mud.Common;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class DefaultTimeClock implements TimeClock
{
	@Override
	public String ID()
	{
		return "DefaultTimeClock";
	}

	@Override
	public String name()
	{
		return loadName;
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultTimeClock();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	protected int	tickStatus	= Tickable.STATUS_NOT;

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	protected boolean		loaded		= false;
	protected String		loadName	= null;
	protected volatile long	lastTicked	= 0;

	@Override
	public void setLoadName(final String name)
	{
		loadName = name;
	}

	protected int		year			= 1000;
	protected int		month			= 1;
	protected int		day				= 1;
	protected int		time			= 0;
	protected int		hoursInDay		= 6;
	protected Season	season			= Season.FALL;

	protected int		weekOfMonth		= 0; // these are derived
	protected int		weekOfYear		= 0; // these are derived
	protected int		dayOfYear		= 1; // these are derived
	protected int		daysInYear		= 8 * 20; // these are derived
	protected int		monthsInSeason	= 3; // these are derived

	protected String[] monthsInYear={
		 "the 1st month","the 2nd month","the 3rd month","the 4th month",
		 "the 5th month","the 6th month","the 7th month","the 8th month"
	};
	protected int		daysInMonth		= 20;
	protected int[]		dawnToDusk		= { 0, 1, 4, 6 };
	protected String[]	weekNames		= {};
	protected String[]	yearNames		= { "year #" };

	protected long		clockCode		= -1;

	@Override
	public int getHoursInDay()
	{
		return hoursInDay;
	}

	@Override
	public void setHoursInDay(final int h)
	{
		hoursInDay = h;
	}

	@Override
	public int getDaysInMonth()
	{
		return daysInMonth;
	}

	@Override
	public void setDaysInMonth(final int d)
	{
		daysInMonth = d;
		if((d>0)&&(this.monthsInYear.length>0))
			daysInYear = this.monthsInYear.length * d;
	}

	@Override
	public int getMonthsInYear()
	{
		return monthsInYear.length;
	}

	@Override
	public String[] getMonthNames()
	{
		return monthsInYear;
	}

	@Override
	public void setMonthsInYear(final String[] months)
	{
		if(months != null)
		{
			monthsInYear = months;
			if((getDaysInMonth()>0)&&(months.length>0))
				daysInYear = months.length * getDaysInMonth();
			monthsInSeason = (int)Math.round(Math.floor(CMath.div(getMonthsInYear(),4.0)));
		}
	}

	@Override
	public int getWeekOfMonth()
	{
		return weekOfMonth;
	}

	@Override
	public int getWeekOfYear()
	{
		return weekOfYear;
	}

	@Override
	public int getDayOfYear()
	{
		return dayOfYear;
	}

	@Override
	public int getDaysInYear()
	{
		return daysInYear;
	}

	@Override
	public int[] getDawnToDusk()
	{
		return dawnToDusk;
	}

	@Override
	public String[] getYearNames()
	{
		return yearNames;
	}

	@Override
	public void setYearNames(final String[] years)
	{
		yearNames = years;
	}

	@Override
	public void setDawnToDusk(final int dawn, final int day, final int dusk, final int night)
	{
		dawnToDusk[TimeOfDay.DAWN.ordinal()]=dawn;
		dawnToDusk[TimeOfDay.DAY.ordinal()]=day;
		dawnToDusk[TimeOfDay.DUSK.ordinal()]=dusk;
		dawnToDusk[TimeOfDay.NIGHT.ordinal()]=night;
	}

	@Override
	public String[] getWeekNames()
	{
		return weekNames;
	}

	@Override
	public int getDaysInWeek()
	{
		return weekNames.length;
	}

	@Override
	public void setWeekNames(final String[] days)
	{
		weekNames = days;
		setDayOfMonth(getDayOfMonth()); // causes derived fields to be recalculated
	}

	@Override
	public String getShortestTimeDescription()
	{
		final StringBuffer timeDesc=new StringBuffer("");
		timeDesc.append(getYear());
		timeDesc.append("/"+getMonth());
		timeDesc.append("/"+getDayOfMonth());
		timeDesc.append(" HR:"+getHourOfDay());
		return timeDesc.toString();
	}

	@Override
	public String getShortTimeDescription()
	{
		final StringBuffer timeDesc=new StringBuffer("");
		timeDesc.append("hour "+getHourOfDay()+" on ");
		if(getDaysInWeek()>0)
		{
			long x=((long)getYear())*((long)getMonthsInYear())*getDaysInMonth();
			x=x+((long)(getMonth()-1))*((long)getDaysInMonth());
			x=x+getDayOfMonth();
			final String[] weekNames = getWeekNames();
			final int weekDayIndex = (int)(x%getDaysInWeek());
			if((weekDayIndex<0) || (weekDayIndex >= weekNames.length))
			{
				if(weekDayIndex < 0)
					Log.errOut("Negative weekday configuration: "+getYear()+", "+getMonthsInYear()+", "+getDaysInMonth()+", "+getMonth());
				else
				if(getDaysInWeek() > weekNames.length)
					Log.errOut("Bad weekday configuration: "+getDaysInWeek()+"/"+weekDayIndex);
				timeDesc.append(L("Unknown")+", ");
			}
			else
				timeDesc.append(weekNames[weekDayIndex]+", ");
		}
		timeDesc.append("the "+getDayOfMonth()+CMath.numAppendage(getDayOfMonth()));
		final int month = getMonth();
		if((month>=0)&&(month<getMonthsInYear()))
			timeDesc.append(" day of "+getMonthNames()[month]);
		else
			timeDesc.append(" day of month "+month);
		if(getYearNames().length>0)
			timeDesc.append(", "+CMStrings.replaceAll(getYearNames()[getYear()%getYearNames().length],"#",""+getYear()));
		return timeDesc.toString();
	}

	@Override
	public void initializeINIClock(final CMProps page)
	{
		if(CMath.s_int(page.getStr("HOURSINDAY"))>0)
			setHoursInDay(CMath.s_int(page.getStr("HOURSINDAY")));

		if(CMath.s_int(page.getStr("DAYSINMONTH"))>0)
			setDaysInMonth(CMath.s_int(page.getStr("DAYSINMONTH")));

		final String monthsInYear=page.getStr("MONTHSINYEAR");
		if(monthsInYear.trim().length()>0)
			setMonthsInYear(CMParms.toStringArray(CMParms.parseCommas(monthsInYear,true)));

		setWeekNames(CMParms.toStringArray(CMParms.parseCommas(page.getStr("DAYSINWEEK"),true)));

		if(page.containsKey("YEARDESC"))
			setYearNames(CMParms.toStringArray(CMParms.parseCommas(page.getStr("YEARDESC"),true)));

		if(page.containsKey("DAWNHR")&&page.containsKey("DAYHR")
				&&page.containsKey("DUSKHR")&&page.containsKey("NIGHTHR"))
		setDawnToDusk(  CMath.s_int(page.getStr("DAWNHR")),
						CMath.s_int(page.getStr("DAYHR")),
						CMath.s_int(page.getStr("DUSKHR")),
						CMath.s_int(page.getStr("NIGHTHR")));

		CMProps.setIntVar(CMProps.Int.TICKSPERMUDDAY,""+((CMProps.getMillisPerMudHour()*CMLib.time().globalClock().getHoursInDay()/CMProps.getTickMillis())));
		CMProps.setIntVar(CMProps.Int.TICKSPERMUDMONTH,""+((CMProps.getMillisPerMudHour()*CMLib.time().globalClock().getHoursInDay()*CMLib.time().globalClock().getDaysInMonth()/CMProps.getTickMillis())));
	}

	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public String timeDescription(final MOB mob, final Room room)
	{
		final StringBuffer timeDesc=new StringBuffer("");

		if(CMLib.flags().canSee(mob))
			timeDesc.append(getTODCode().getDesc());
		timeDesc.append("(Hour: "+getHourOfDay()+"/"+(getHoursInDay()-1)+")");
		timeDesc.append("\n\rIt is ");
		if(getDaysInWeek()>0)
		{
			long x=((long)getYear())*((long)getMonthsInYear())*getDaysInMonth();
			x=x+((long)(getMonth()-1))*((long)getDaysInMonth());
			x=x+getDayOfMonth();
			timeDesc.append(getWeekNames()[(int)(x%getDaysInWeek())]+", ");
		}
		timeDesc.append("the "+getDayOfMonth()+CMath.numAppendage(getDayOfMonth()));
		timeDesc.append(" day of "+getMonthNames()[getMonth()-1]);
		if(getYearNames().length>0)
			timeDesc.append(", "+CMStrings.replaceAll(getYearNames()[getYear()%getYearNames().length],"#",""+getYear()));
		timeDesc.append(L(".\n\rIt is "+getSeasonCode().toString().toLowerCase()+"."));
		if((CMLib.flags().canSee(mob))
		&&(getTODCode()==TimeClock.TimeOfDay.NIGHT)
		&&(CMLib.map().hasASky(room)))
		{
			switch(room.getArea().getClimateObj().weatherType(room))
			{
			case Climate.WEATHER_BLIZZARD:
			case Climate.WEATHER_HAIL:
			case Climate.WEATHER_SLEET:
			case Climate.WEATHER_SNOW:
			case Climate.WEATHER_RAIN:
			case Climate.WEATHER_THUNDERSTORM:
				timeDesc.append("\n\r"+room.getArea().getClimateObj().weatherDescription(room)+L(" You can't see the moon.")); break;
			case Climate.WEATHER_CLOUDY:
				timeDesc.append(L("\n\rThe clouds obscure the moon."));
				break;
			case Climate.WEATHER_FOG:
				timeDesc.append(L("\n\rIt is very foggy."));
				break;
			case Climate.WEATHER_DUSTSTORM:
				timeDesc.append(L("\n\rThe dust obscures the moon."));
				break;
			default:
				timeDesc.append(L("\n\r"+getMoonPhase(room).getDesc()));
				break;
			}
		}
		return timeDesc.toString();
	}

	@Override
	public int getYear()
	{
		return year;
	}

	@Override
	public void setYear(final int y)
	{
		year=y;
	}

	@Override
	public int getMonthsInSeason()
	{
		return (int)Math.round(Math.floor(CMath.div(getMonthsInYear(),4.0)));

	}

	@Override
	public Season getSeasonCode()
	{
		return season;
	}

	@Override
	public int getMonth()
	{
		return month;
	}

	@Override
	public void setMonth(final int m)
	{
		month=m;
		final int div=getMonthsInSeason();
		if(month<div)
			season=TimeClock.Season.WINTER;
		else
		if(month<(div*2))
			season=TimeClock.Season.SPRING;
		else
		if(month<(div*3))
			season=TimeClock.Season.SUMMER;
		else
			season=TimeClock.Season.FALL;
	}

	@Override
	public MoonPhase getMoonPhase(final Room room)
	{
		int moonDex = (int)Math.round(Math.floor(CMath.mul(CMath.div(getDayOfMonth(),getDaysInMonth()+1),8.0)));
		if(room != null)
		{
			final Area area = room.getArea();
			if((room.numEffects()>0) || ((area != null)&&(area.numEffects()>0)))
			{
				final List<Ability> moonEffects = new ArrayList<Ability>(1);
				if(room.numEffects()>0)
					moonEffects.addAll(CMLib.flags().domainAffects(room, Ability.DOMAIN_MOONALTERING));
				if((area != null)&&(area.numEffects()>0))
					moonEffects.addAll(CMLib.flags().domainAffects(area, Ability.DOMAIN_MOONALTERING));
				for(final Ability A : moonEffects)
					moonDex = (moonDex + A.abilityCode()) % 8;
			}
		}
		return TimeClock.MoonPhase.values()[moonDex];
	}

	@Override
	public TidePhase getTidePhase(final Room room)
	{
		final MoonPhase moonPhase = getMoonPhase(room);
		TidePhase tidePhase;
		if(getHourOfDay() == dawnToDusk[1])
			tidePhase = moonPhase.getLowTide();
		else
		if(getHourOfDay() == dawnToDusk[2])
			tidePhase = moonPhase.getHighTide();
		else
			tidePhase = TidePhase.NO_TIDE;
		if(room != null)
		{
			final Area area = room.getArea();
			if((room.numEffects()>0) || ((area != null)&&(area.numEffects()>0)))
			{
				final List<Ability> moonEffects = new ArrayList<Ability>(1);
				if(room.numEffects()>0)
					moonEffects.addAll(CMLib.flags().flaggedAffects(room, Ability.FLAG_TIDEALTERING));
				if((area != null)&&(area.numEffects()>0))
					moonEffects.addAll(CMLib.flags().flaggedAffects(area, Ability.FLAG_TIDEALTERING));
				if(moonEffects.size()>0)
				{
					int tideDex = CMParms.indexOf(TidePhase.values(), tidePhase);
					for(final Ability A : moonEffects)
						tideDex = (tideDex + A.abilityCode()) % TidePhase.values().length;
					tidePhase = TidePhase.values()[tideDex];
				}
			}
		}
		return tidePhase;
	}

	@Override
	public int getDayOfMonth()
	{
		return day;
	}

	@Override
	public void setDayOfMonth(final int d)
	{
		day = d;
		if(getMonth()>0)
		{
			dayOfYear = ((getMonth()-1) * getDaysInMonth()) + d;
			if(getDaysInWeek()>0)
			{
				weekOfMonth = (int)Math.round(CMath.floor(CMath.div(day,getDaysInWeek())));
				weekOfYear = (int)Math.round(CMath.floor(CMath.div(dayOfYear,getDaysInWeek())));
			}
		}
	}

	@Override
	public int getHourOfDay()
	{
		return time;
	}

	@Override
	public TimeOfDay getTODCode()
	{
		if((time>=getDawnToDusk()[TimeClock.TimeOfDay.NIGHT.ordinal()])&&(getDawnToDusk()[TimeClock.TimeOfDay.NIGHT.ordinal()]>=0))
			return TimeClock.TimeOfDay.NIGHT;
		if((time>=getDawnToDusk()[TimeClock.TimeOfDay.DUSK.ordinal()])&&(getDawnToDusk()[TimeClock.TimeOfDay.DUSK.ordinal()]>=0))
			return TimeClock.TimeOfDay.DUSK;
		if((time>=getDawnToDusk()[TimeClock.TimeOfDay.DAY.ordinal()])&&(getDawnToDusk()[TimeClock.TimeOfDay.DAY.ordinal()]>=0))
			return TimeClock.TimeOfDay.DAY;
		if((time>=getDawnToDusk()[TimeClock.TimeOfDay.DAWN.ordinal()])&&(getDawnToDusk()[TimeClock.TimeOfDay.DAWN.ordinal()]>=0))
			return TimeClock.TimeOfDay.DAWN;
		// it's before night, dusk, day, and dawn... before dawn is still night.
		if(getDawnToDusk()[TimeClock.TimeOfDay.NIGHT.ordinal()]>=0)
			return TimeClock.TimeOfDay.NIGHT;
		return TimeClock.TimeOfDay.DAY;
	}

	@Override
	public boolean setHourOfDay(final int t)
	{
		final TimeOfDay oldCode=getTODCode();
		time=t;
		return getTODCode()!=oldCode;
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final TimeClock C=(TimeClock)this.clone();
			return C;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultTimeClock();
		}
	}

	@Override
	public TimeClock deriveClock(final long millis)
	{
		try
		{
			final TimeClock C=(TimeClock)this.clone();
			final long diff=(millis - System.currentTimeMillis())/CMProps.getMillisPerMudHour();
			C.tickTock((int)diff);
			return C;
		}
		catch(final CloneNotSupportedException e)
		{
		}
		return CMLib.time().globalClock();
	}

	@Override
	public long toTimestamp(final TimeClock now)
	{
		if(now.compareTo(this) < 0)
		{
			final long diff = this.deriveMillisAfter(now);
			return System.currentTimeMillis() + diff;
		}
		else
		{
			final long diff = now.deriveMillisAfter(this);
			return System.currentTimeMillis() - diff;
		}
	}

	@Override
	public String deriveEllapsedTimeString(final long millis)
	{
		int hours=(int)(millis/CMProps.getMillisPerMudHour());
		int days=0;
		int months=0;
		int years=0;
		if(hours>getHoursInDay())
		{
			days=(int)Math.round(Math.floor(CMath.div(hours,getHoursInDay())));
			hours=hours-(days*getHoursInDay());
		}
		if(days>getDaysInMonth())
		{
			months=(int)Math.round(Math.floor(CMath.div(days,getDaysInMonth())));
			days=days-(months*getDaysInMonth());
		}
		if(months>=getMonthsInYear())
		{
			years=(int)Math.round(Math.floor(CMath.div(months,getMonthsInYear())));
			months=months-(years*getMonthsInYear());
		}
		final StringBuffer buf=new StringBuffer("");
		if(years>0)
			buf.append(years+L((years==1)?" year":" years"));
		if(months>0)
		{
			if(buf.length()>0)
				buf.append(", ");
			buf.append(months+L((months==1)?" month":" months"));
		}
		if(days>0)
		{
			if(buf.length()>0)
				buf.append(", ");
			buf.append(days+L((days==1)?" day":" days"));
		}
		if(hours>0)
		{
			if(buf.length()>0)
				buf.append(", ");
			buf.append(hours+L((hours==1)?" hour":" hours"));
		}
		if(buf.length()==0)
			return L("under an hour");
		return buf.toString();
	}

	@Override
	public long deriveMillisAfter(final TimeClock C)
	{
		return deriveMudHoursAfter(C) *CMProps.getMillisPerMudHour();
	}

	@Override
	public long getPeriodMillis(final TimePeriod P)
	{
		switch(P)
		{
		case DAY:
			return CMProps.getMillisPerMudHour() * getHoursInDay();
		case HOUR:
			return CMProps.getMillisPerMudHour();
		case MONTH:
			return CMProps.getMillisPerMudHour() * getHoursInDay() * getDaysInMonth();
		case SEASON:
			return CMProps.getMillisPerMudHour() * getMonthsInSeason() * getHoursInDay() * getDaysInMonth();
		case WEEK:
			return CMProps.getMillisPerMudHour() * getDaysInWeek() * getHoursInDay();
		case YEAR:
			return CMProps.getMillisPerMudHour() * getMonthsInYear() * getHoursInDay() * getDaysInMonth();
		case ALLTIME:
		default:
			return 0;
		}
	}

	@Override
	public boolean isAfter(final TimeClock C)
	{
		if(getYear()!=C.getYear())
			return getYear()>C.getYear();
		if(getMonth()!=C.getMonth())
			return getMonth()>C.getMonth();
		if(getDayOfMonth()!=C.getDayOfMonth())
			return getDayOfMonth()>C.getDayOfMonth();
		if(getHourOfDay()!=C.getHourOfDay())
			return getHourOfDay()>C.getHourOfDay();
		return false;
	}

	@Override
	public boolean isBefore(final TimeClock C)
	{
		if(getYear()!=C.getYear())
			return getYear()<C.getYear();
		if(getMonth()!=C.getMonth())
			return getMonth()<C.getMonth();
		if(getDayOfMonth()!=C.getDayOfMonth())
			return getDayOfMonth()<C.getDayOfMonth();
		if(getHourOfDay()!=C.getHourOfDay())
			return getHourOfDay()<C.getHourOfDay();
		return false;
	}

	@Override
	public boolean isEqual(final TimeClock C)
	{
		if(getYear()!=C.getYear())
			return false;
		if(getMonth()!=C.getMonth())
			return false;
		if(getDayOfMonth()!=C.getDayOfMonth())
			return false;
		if(getHourOfDay()!=C.getHourOfDay())
			return false;
		return true;
	}

	@Override
	public long deriveMudHoursAfter(final TimeClock C)
	{
		if(isEqual(C))
			return 0;
		if(isBefore(C))
			return -1;
		final long me = (long)((getDayOfYear()-1)*getHoursInDay()) + getHourOfDay();
		final long ot = (long)((C.getDayOfYear()-1)*C.getHoursInDay()) + C.getHourOfDay();
		final long hoursInYear = (getHoursInDay()*getDaysInMonth()*getMonthsInYear());
		final long yearDiff = getYear()-C.getYear();
		long numMudHours=0;
		if(me>ot)
		{
			numMudHours = me-ot;
			numMudHours+=hoursInYear*yearDiff;
		}
		else
		if(me==ot)
			numMudHours=hoursInYear*yearDiff;
		else
		{
			numMudHours+=(hoursInYear-ot)+me;
			numMudHours+=hoursInYear*(yearDiff-1);
		}
		return numMudHours;
	}

	@Override
	public void handleTimeChange()
	{
		try
		{
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if(A.getTimeObj()==this)
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=null)&&((R.numInhabitants()>0)||(R.numItems()>0)))
					{
						R.recoverPhyStats();
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB mob=R.fetchInhabitant(m);
							if((mob!=null)
							&&(!mob.isMonster()))
							{
								if(CMLib.map().hasASky(R)
								&&(!CMLib.flags().isSleeping(mob))
								&&(CMLib.flags().canSee(mob)))
								{
									final String message = CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.TOD_CHANGE_OUTSIDE, getTODCode().ordinal());
									if(message.trim().length()>0)
										mob.tell(message);
								}
								else
								{
									final String message = CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.TOD_CHANGE_INSIDE, getTODCode().ordinal());
									if(message.trim().length()>0)
										mob.tell(message);
								}
							}
						}
					}
					if(R!=null)
						R.recoverRoomStats();
				}
			}
		}
		catch (final java.util.NoSuchElementException x)
		{
		}
	}

	protected void tickTock(final int howManyHours, final boolean moveTheSky)
	{
		final TimeOfDay todCode=getTODCode();
		if(howManyHours!=0)
		{
			lastTicked=System.currentTimeMillis();
			setHourOfDay(getHourOfDay()+howManyHours);
			if(getHourOfDay()>=getHoursInDay())
			{
				bumpDays(1);
				final int extraHours = getHourOfDay() - getHoursInDay();
				final int newHour = extraHours % getHoursInDay();
				setHourOfDay(newHour);
				if(extraHours>=getHoursInDay())
					bumpDays((int)Math.round(Math.floor(extraHours / getHoursInDay())));
			}
			else
			if(getHourOfDay()<0)
			{
				bumpDays(-1);
				final int extraHours = Math.abs(getHourOfDay());
				setHourOfDay(getHoursInDay() - (extraHours % getHoursInDay()));
				if(extraHours>=getHoursInDay())
					bumpDays(-(int)Math.round(Math.floor(extraHours / getHoursInDay())));
			}
		}
		if((moveTheSky)&&(getTODCode()!=todCode))
			handleTimeChange();
	}

	@Override
	public void tickTock(final int howManyHours)
	{
		tickTock(howManyHours,true);
	}

	@Override
	public void bumpHours(final int num)
	{
		tickTock(num,false);
	}

	@Override
	public void bumpDays(final int num)
	{
		setDayOfMonth(getDayOfMonth()+num);
		if(getDayOfMonth()>getDaysInMonth())
		{
			bumpMonths(1);
			final int extraDays = getDayOfMonth() - getDaysInMonth();
			final int newDay = (extraDays % getDaysInMonth());
			setDayOfMonth(newDay+1);
			if(extraDays>getDaysInMonth())
				bumpMonths((int)Math.round(Math.floor(extraDays / getDaysInMonth())));
		}
		else
		if(getDayOfMonth()<=0)
		{
			bumpMonths(-1);
			final int extraDays = Math.abs(getDayOfMonth());
			final int newDay = getDaysInMonth() - (extraDays % getDaysInMonth());
			setDayOfMonth(newDay);
			if(extraDays>getDaysInMonth())
				bumpMonths(-(int)Math.round(Math.floor(extraDays / getDaysInMonth())));
		}
	}

	@Override
	public void bumpWeeks(final int num)
	{
		bumpDays(getDaysInWeek() * num);
	}

	@Override
	public void bumpMonths(final int num)
	{
		setMonth(getMonth()+num);
		if(getMonth()>=getMonthsInYear())
		{
			bumpYears(1);
			final int extraMonths = getMonth() - getMonthsInYear();
			final int newMonth = extraMonths % getMonthsInYear();
			setMonth(newMonth);
			if(extraMonths>=getMonthsInYear())
				bumpYears((int)Math.round(Math.floor(extraMonths / getMonthsInYear()))); // this should be an even #
		}
		if(getMonth()<0)
		{
			bumpYears(-1);
			final int extraMonths = Math.abs(getMonth());
			final int newMonth = getMonthsInYear() - (extraMonths % getMonthsInYear());
			setMonth(newMonth);
			if(extraMonths>=getMonthsInYear())
				bumpYears(-(int)Math.round(Math.floor(extraMonths / getMonthsInYear())));
		}
	}

	@Override
	public void bumpYears(final int num)
	{
		setYear(getYear()+num);
	}

	@Override
	public void bump(final TimePeriod P, final int num)
	{
		switch(P)
		{
		case DAY:
			bumpDays(num);
			break;
		case HOUR:
			bumpHours(num);
			break;
		case MONTH:
			bumpMonths(num);
			break;
		case SEASON:
			bumpMonths(getMonthsInSeason() * num);
			break;
		case WEEK:
			bumpWeeks(num);
			break;
		case YEAR:
			bumpYears(num);
			break;
		case ALLTIME:
			break;
		}
	}

	@Override
	public long toHoursSinceEpoc()
	{
		final long hoursInDay = this.getHoursInDay();
		final long hoursInMonth = this.getDaysInMonth() * hoursInDay;
		final long hoursInYear = this.getMonthsInYear() * hoursInMonth;
		final long total = this.getHourOfDay() +
				((day-1) * hoursInDay) +
				((month-1) * hoursInMonth) +
				(year * hoursInYear);
		return total;
	}

	@Override
	public void setFromHoursSinceEpoc(long num)
	{
		final long hoursInDay = this.getHoursInDay();
		final long hoursInMonth = this.getDaysInMonth() * hoursInDay;
		final long hoursInYear = this.getMonthsInYear() * hoursInMonth;
		year=0;
		while(num > hoursInYear)
		{
			year++;
			num -= hoursInYear;
		}
		month = 1;
		while(num > hoursInMonth)
		{
			month++;
			num -= hoursInMonth;
		}
		day = 1;
		while(num > hoursInDay)
		{
			day++;
			num -= hoursInDay;
		}
		time = (int)num;
		setYear(year); // for any derived fields
		setMonth(month); // for any derived fields
		setDayOfMonth(day); // for any derived fields
	}

	@Override
	public void setDateTime(final TimeClock fromC)
	{
		loaded = true;
		if(fromC instanceof DefaultTimeClock)
		{
			final DefaultTimeClock fromDC = (DefaultTimeClock)fromC;
			year = fromDC.year;
			month = fromDC.month;
			day = fromDC.day;
			time = fromDC.time;
			dayOfYear = fromDC.dayOfYear;
			weekOfMonth = fromDC.weekOfMonth;
			weekOfYear = fromDC.weekOfYear;
			season = fromDC.season;
		}
		else
		{
			this.setYear(fromC.getYear());
			this.setMonth(fromC.getMonth());
			this.setDayOfMonth(fromC.getDayOfMonth());
			this.setHourOfDay(fromC.getHourOfDay());
		}
	}

	@Override
	public int getHoursPer(final TimePeriod period)
	{
		switch(period)
		{
		case DAY:
			return this.getHoursInDay();
		case MONTH:
			return this.getHoursInDay() * this.getDaysInMonth();
		case SEASON:
			return this.getHoursInDay() * this.getDaysInMonth() * this.getMonthsInSeason();
		case WEEK:
			return this.getHoursInDay() * this.getDaysInWeek();
		case YEAR:
			return this.getHoursInDay() * this.getDaysInYear();
		default:
		case ALLTIME:
		case HOUR:
			return 1;
		}
	}

	@Override
	public void save()
	{
		if((loaded)&&(loadName!=null))
		{
			CMLib.database().DBReCreatePlayerData(loadName,"TIMECLOCK","TIMECLOCK/"+loadName,
			"<HOUR>"+getHourOfDay()+"</HOUR>"+
			"<DAY>"+getDayOfMonth()+"</DAY><MONTH>"+getMonth()+"</MONTH><YEAR>"+getYear()+"</YEAR>"
			+"<HOURS>"+getHoursInDay()+"</HOURS><DAYS>"+getDaysInMonth()+"</DAYS>"
			+"<MONTHS>"+CMParms.toListString(getMonthNames())+"</MONTHS>"
			+"<DAWNHR>"+getDawnToDusk()[TimeOfDay.DAWN.ordinal()]+"</DAWNHR>"
			+"<DAYHR>"+getDawnToDusk()[TimeOfDay.DAY.ordinal()]+"</DAYHR>"
			+"<DUSKHR>"+getDawnToDusk()[TimeOfDay.DUSK.ordinal()]+"</DUSKHR>"
			+"<NIGHTHR>"+getDawnToDusk()[TimeOfDay.NIGHT.ordinal()]+"</NIGHTHR>"
			+"<WEEK>"+CMParms.toListString(getWeekNames())+"</WEEK>"
			+"<YEARS>"+CMParms.toListString(getYearNames())+"</YEARS>"
			);
		}
	}

	@Override
	public String toTimePeriodCodeString()
	{
		return getYear()+"/"+getMonth()+"/"+getDayOfMonth()+"/"+getHourOfDay()+" "+loadName;
	}

	@Override
	public String toTimeString()
	{
		return getMonth()+"/"+getDayOfMonth()+"/"+getYear()+" "+getHourOfDay()+"h ";
	}

	@Override
	public TimeClock fromTimePeriodCodeString(String period)
	{
		TimeClock C = null;
		period = period.trim();
		final int x = period.indexOf(' ');
		String[] date;
		if(x<0)
			date = period.split("/");
		else
		{
			date = period.substring(0,x).trim().split("/");
			final String clockName =  period.substring(x+1).trim();
			if((clockName.length()>1)
			&&(date.length==3)
			&&(clockName.endsWith("h"))
			&&(CMath.isInteger(clockName.substring(0,clockName.length()-1))))
			{
				date = Arrays.copyOf(date, 4);
				date[3]=clockName.substring(0,clockName.length()-1);
			}
			else
			{
				final TimeClock foundClock = CMLib.map().getClockCache().get(clockName);
				if(foundClock != null)
					C = (TimeClock)foundClock.copyOf();
			}
		}
		if(C == null)
			C = (TimeClock)this.copyOf();
		C.setYear(CMath.s_int(date[0]));
		C.setMonth(CMath.s_int(date[1]));
		C.setDayOfMonth(CMath.s_int(date[2]));
		C.setHourOfDay(CMath.s_int(date[3]));
		return C;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus=Tickable.STATUS_NOT;
		if(((loadName==null)||(loaded))
		&&(((System.currentTimeMillis()-lastTicked)<=CMProps.getMillisPerMudHour())))
			return true;
		boolean process = false;
		boolean timeToTick=false;
		synchronized(this)
		{
			timeToTick = ((System.currentTimeMillis()-lastTicked)>CMProps.getMillisPerMudHour());
			lastTicked=System.currentTimeMillis();
			if((loadName!=null)&&(!loaded))
			{
				process=true;
			}
		}
		if(process)
		{
			loaded=true;
			final List<PlayerData> bitV=CMLib.database().DBReadPlayerData(loadName,"TIMECLOCK");
			String timeRsc=null;
			if((bitV==null)||(bitV.size()==0))
				timeRsc="<TIME>-1</TIME><DAY>1</DAY><MONTH>1</MONTH><YEAR>1</YEAR>";
			else
				timeRsc=bitV.get(0).xml();
			final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(timeRsc);
			setHourOfDay(CMLib.xml().getIntFromPieces(V,"TIME"));
			setDayOfMonth(CMLib.xml().getIntFromPieces(V,"DAY"));
			setMonth(CMLib.xml().getIntFromPieces(V,"MONTH"));
			setYear(CMLib.xml().getIntFromPieces(V,"YEAR"));
			final String hr = CMLib.xml().getValFromPieces(V,"HOUR");
			if(hr.length()>0)
				setHourOfDay(CMath.s_int(hr));
			final TimeClock globalClock=CMLib.time().globalClock();
			if(this!=globalClock)
			{
				if((CMLib.xml().getValFromPieces(V,"HOURS").length()==0)
				||(CMLib.xml().getValFromPieces(V,"DAYS").length()==0)
				||(CMLib.xml().getValFromPieces(V,"MONTHS").length()==0))
				{
					setHoursInDay(globalClock.getHoursInDay());
					setDaysInMonth(globalClock.getDaysInMonth());
					setMonthsInYear(globalClock.getMonthNames());
					setDawnToDusk(globalClock.getDawnToDusk()[TimeOfDay.DAWN.ordinal()],
								  globalClock.getDawnToDusk()[TimeOfDay.DAY.ordinal()],
								  globalClock.getDawnToDusk()[TimeOfDay.DUSK.ordinal()],
								  globalClock.getDawnToDusk()[TimeOfDay.NIGHT.ordinal()]);
					setWeekNames(globalClock.getWeekNames());
					setYearNames(globalClock.getYearNames());
				}
				else
				{
					setHoursInDay(CMLib.xml().getIntFromPieces(V,"HOURS"));
					setDaysInMonth(CMLib.xml().getIntFromPieces(V,"DAYS"));
					setMonthsInYear(CMParms.toStringArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(V,"MONTHS"),true)));
					setDawnToDusk(CMLib.xml().getIntFromPieces(V,"DAWNHR"),
								  CMLib.xml().getIntFromPieces(V,"DAYHR"),
								  CMLib.xml().getIntFromPieces(V,"DUSKHR"),
								  CMLib.xml().getIntFromPieces(V,"NIGHTHR"));
					setWeekNames(CMParms.toStringArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(V,"WEEK"),true)));
					setYearNames(CMParms.toStringArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(V,"YEARS"),true)));
				}
			}
			CMLib.map().getClockCache().put(loadName, this);
		}
		if(timeToTick)
			tickTock(1);
		return true;
	}

	@Override
	public int get(final TimePeriod period)
	{
		switch(period)
		{
		case ALLTIME:
			return 0;
		case DAY:
			return this.getDayOfMonth();
		case HOUR:
			return this.getHourOfDay();
		case MONTH:
			return this.getMonth();
		case SEASON:
			return this.getSeasonCode().ordinal();
		case WEEK:
			return this.getWeekOfYear();
		case YEAR:
			return this.getYear();
		}
		return -1;
	}

	@Override
	public int getMax(final TimePeriod period)
	{
		switch(period)
		{
		case ALLTIME:
			return -1;
		case DAY:
			return this.getDaysInMonth();
		case HOUR:
			return this.getHoursInDay()-1;
		case MONTH:
			return this.getMonthsInYear()-1;
		case SEASON:
			return 3;
		case WEEK:
			return this.getDaysInYear() / this.getDaysInWeek();
		case YEAR:
			return Integer.MAX_VALUE/2;
		}
		return -1;
	}


	@Override
	public int getMin(final TimePeriod period)
	{
		switch(period)
		{
		case ALLTIME:
			return -1;
		case DAY:
			return 1;
		case HOUR:
			return 0;
		case MONTH:
			return 0;
		case SEASON:
			return 0;
		case WEEK:
			return 0;
		case YEAR:
			return 0;
		}
		return -1;
	}

	@Override
	public void set(final TimePeriod period, final int value)
	{
		switch(period)
		{
		case ALLTIME:
			return;
		case DAY:
			setDayOfMonth(value);
			break;
		case HOUR:
			setHourOfDay(value);
			break;
		case MONTH:
			setMonth(value);
			break;
		case SEASON:
			if(value != getSeasonCode().ordinal())
				setNext(period, value);
			break;
		case WEEK:
			if(value != getWeekOfYear())
				setNext(period, value);
			break;
		case YEAR:
			setYear(value);
			break;
		}
	}

	@Override
	public void setNext(final TimePeriod period, final int value)
	{
		switch(period)
		{
		case ALLTIME:
			return;
		case DAY:
			if(value <= getDayOfMonth())
				bump(TimePeriod.MONTH, 1);
			setDayOfMonth(value);
			break;
		case HOUR:
			if(value <= getHourOfDay())
				bump(TimePeriod.DAY, 1);
			setHourOfDay(value);
			break;
		case MONTH:
			if(value <= getMonth())
				bump(TimePeriod.YEAR, 1);
			setMonth(value);
			break;
		case SEASON:
			if(this.getSeasonCode().ordinal()==value)
				bump(TimePeriod.YEAR, 1);
			else
			if((value>=0)&&(value<Season.values().length))
			{
				for(int i=0;i<getMonthsInYear();i++)
				{
					if(this.getSeasonCode().ordinal()!=value)
						bump(TimePeriod.MONTH, 1);
					else
						break;
				}
			}
			break;
		case WEEK:
			if(getWeekOfYear()==value)
				bump(TimePeriod.YEAR, 1);
			else
			{
				final int numWeeksInYear = (getDaysInYear() / getWeekNames().length);
				if((value>=0)&&(value<=numWeeksInYear))
				{
					for(int i=0;i<=numWeeksInYear;i++)
					{
						if(getWeekOfYear() != value)
							bump(TimePeriod.WEEK, 1);
						else
							break;
					}
				}
			}
			break;
		case YEAR:
			setYear(getYear());
			break;
		}
	}

	@Override
	public int compareTo(final CMObject o)
	{
		if(o instanceof TimeClock)
		{
			final TimeClock c2=(TimeClock)o;
			final long myHrs = (getHourOfDay()
						+ (getDayOfMonth() * getHoursInDay())
						+ (getMonth() * getDaysInMonth() * getHoursInDay())
						+ (getYear() * getMonthsInYear() * getDaysInMonth() * getHoursInDay()));
			final long hisHrs = (c2.getHourOfDay()
					+ (c2.getDayOfMonth() * c2.getHoursInDay())
					+ (c2.getMonth() * c2.getDaysInMonth() * c2.getHoursInDay())
					+ (c2.getYear() * c2.getMonthsInYear() * c2.getDaysInMonth() * c2.getHoursInDay()));
			if (myHrs == hisHrs)
				return 0;
			if (myHrs > hisHrs)
				return 1;
			return -1;
		}
		else
			return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}
}
