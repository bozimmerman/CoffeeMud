package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class DefaultTimeClock implements TimeClock, Cloneable
{
	public String ID(){return "DefaultTimeClock";}
	public String name(){return "Time Object";}
	public static final TimeClock globalClock=new DefaultTimeClock();
	
	protected long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	private boolean loaded=false;
	private String loadName=null;
	public void setLoadName(String name){loadName=name;}
	private int year=1;
	private int month=1;
	private int day=1;
	private int time=0;
	private int hoursInDay=6;
	private String[] monthsInYear={
			 "the 1st month","the 2nd month","the 3rd month","the 4th month",
			 "the 5th month","the 6th month","the 7th month","the 8th month"
	};
	private int daysInMonth=20;
	private int[] dawnToDusk={0,1,4,6};
	private String[] weekNames={};
	private String[] yearNames={"year #"};
	
	public int getHoursInDay(){return hoursInDay;}
	public void setHoursInDay(int h){hoursInDay=h;}
	public int getDaysInMonth(){return daysInMonth;}
	public void setDaysInMonth(int d){daysInMonth=d;}
	public int getMonthsInYear(){return monthsInYear.length;}
	public String[] getMonthNames(){return monthsInYear;}
	public void setMonthsInYear(String[] months){monthsInYear=months;}
	public int[] getDawnToDusk(){return dawnToDusk;}
	public String[] getYearNames(){return yearNames;}
	public void setYearNames(String[] years){yearNames=years;}
	public void setDawnToDusk(int dawn, int day, int dusk, int night)
	{ 
		dawnToDusk[TIME_DAWN]=dawn;
		dawnToDusk[TIME_DAY]=day;
		dawnToDusk[TIME_DUSK]=dusk;
		dawnToDusk[TIME_NIGHT]=night;
	}
	public String[] getWeekNames(){return weekNames;}
	public int getDaysInWeek(){return weekNames.length;}
	public void setDaysInWeek(String[] days){weekNames=days;}
	
    public String getShortestTimeDescription()
    {
        StringBuffer timeDesc=new StringBuffer("");
        timeDesc.append(getYear());
        timeDesc.append("/"+getMonth());
        timeDesc.append("/"+getDayOfMonth());
        timeDesc.append(" HR:"+getTimeOfDay());
        return timeDesc.toString();
    }
    public String getShortTimeDescription()
    {
        StringBuffer timeDesc=new StringBuffer("");
        timeDesc.append("hour "+getTimeOfDay()+" on ");
        if(getDaysInWeek()>0)
        {
            long x=((long)getYear())*((long)getMonthsInYear())*getDaysInMonth();
            x=x+((long)(getMonth()-1))*((long)getDaysInMonth());
            x=x+getDayOfMonth();
            timeDesc.append(getWeekNames()[(int)(x%getDaysInWeek())]+", ");
        }
        timeDesc.append("the "+getDayOfMonth()+numAppendage(getDayOfMonth()));
        timeDesc.append(" day of "+getMonthNames()[getMonth()-1]);
        if(getYearNames().length>0)
            timeDesc.append(", "+Util.replaceAll(getYearNames()[getYear()%getYearNames().length],"#",""+getYear()));
        return timeDesc.toString();
    }
    
    public static int determineSeason(String str)
    {
        str=str.toUpperCase().trim();
        if(str.length()==0) return -1;
        for(int i=0;i<TimeClock.SEASON_DESCS.length;i++)
            if(TimeClock.SEASON_DESCS[i].startsWith(str))
                return i;
        return -1;
    }
    
	public String timeDescription(MOB mob, Room room)
	{
		StringBuffer timeDesc=new StringBuffer("");

		if((Sense.canSee(mob))&&(getTODCode()>=0))
			timeDesc.append(TOD_DESC[getTODCode()]);
		timeDesc.append("(Hour: "+getTimeOfDay()+"/"+(getHoursInDay()-1)+")");
		timeDesc.append("\n\rIt is ");
		if(getDaysInWeek()>0)
		{
			long x=((long)getYear())*((long)getMonthsInYear())*getDaysInMonth();
			x=x+((long)(getMonth()-1))*((long)getDaysInMonth());
			x=x+getDayOfMonth();
			timeDesc.append(getWeekNames()[(int)(x%getDaysInWeek())]+", ");
		}
		timeDesc.append("the "+getDayOfMonth()+numAppendage(getDayOfMonth()));
		timeDesc.append(" day of "+getMonthNames()[getMonth()-1]);
		if(getYearNames().length>0)
			timeDesc.append(", "+Util.replaceAll(getYearNames()[getYear()%getYearNames().length],"#",""+getYear()));
		timeDesc.append(".\n\rIt is "+(TimeClock.SEASON_DESCS[getSeasonCode()]).toLowerCase()+".");
		if((Sense.canSee(mob))
		&&(getTODCode()==TimeClock.TIME_NIGHT)
		&&(CoffeeUtensils.hasASky(room)))
		{
			switch(room.getArea().getClimateObj().weatherType(room))
			{
			case Climate.WEATHER_BLIZZARD:
			case Climate.WEATHER_HAIL:
			case Climate.WEATHER_SLEET:
			case Climate.WEATHER_SNOW:
			case Climate.WEATHER_RAIN:
			case Climate.WEATHER_THUNDERSTORM:
				timeDesc.append("\n\r"+room.getArea().getClimateObj().weatherDescription(room)+" You can't see the moon."); break;
			case Climate.WEATHER_CLOUDY:
				timeDesc.append("\n\rThe clouds obscure the moon."); break;
			case Climate.WEATHER_DUSTSTORM:
				timeDesc.append("\n\rThe dust obscures the moon."); break;
			default:
				if(getMoonPhase()>=0)
					timeDesc.append("\n\r"+MOON_PHASES[getMoonPhase()]);
				break;
			}
		}
		return timeDesc.toString();
	}

	private String numAppendage(int num)
	{
	    String strn=""+num;
        if((num<11)||(num>13))
		switch(Util.s_int(""+(strn).charAt(strn.length()-1)))
		{
		case 1: return "st";
		case 2: return "nd";
		case 3: return "rd";
		}
		return "th";
	}

	public int getYear(){return year;}
	public void setYear(int y){year=y;}

	public int getSeasonCode(){
	    int div=(int)Math.round(Math.floor(Util.div(getMonthsInYear(),4.0)));
	    if(month<div) return TimeClock.SEASON_WINTER;
	    if(month<(div*2)) return TimeClock.SEASON_SPRING;
	    if(month<(div*3)) return TimeClock.SEASON_SUMMER;
	    return TimeClock.SEASON_FALL;
	}
	public int getMonth(){return month;}
	public void setMonth(int m){month=m;}
	public int getMoonPhase(){return (int)Math.round(Math.floor(Util.mul(Util.div(getDayOfMonth(),getDaysInMonth()),8.0)));}

	public int getDayOfMonth(){return day;}
	public void setDayOfMonth(int d){day=d;}
	public int getTimeOfDay(){return time;}
	public int getTODCode()
	{
		if((time>=getDawnToDusk()[TimeClock.TIME_NIGHT])&&(getDawnToDusk()[TimeClock.TIME_NIGHT]>=0))
			return TimeClock.TIME_NIGHT;
		if((time>=getDawnToDusk()[TimeClock.TIME_DUSK])&&(getDawnToDusk()[TimeClock.TIME_DUSK]>=0))
			return TimeClock.TIME_DUSK;
		if((time>=getDawnToDusk()[TimeClock.TIME_DAY])&&(getDawnToDusk()[TimeClock.TIME_DAY]>=0))
			return TimeClock.TIME_DAY;
		if((time>=getDawnToDusk()[TimeClock.TIME_DAWN])&&(getDawnToDusk()[TimeClock.TIME_DAWN]>=0))
			return TimeClock.TIME_DAWN;
		return TimeClock.TIME_DAY;
	}
	public boolean setTimeOfDay(int t)
	{
		int oldCode=getTODCode();
		time=t;
		return getTODCode()!=oldCode;
	}
    
    public TimeClock deriveClock(long millis)
    {
        try
        {
            TimeClock C=(TimeClock)this.clone();
            long diff=(System.currentTimeMillis()-millis)/MudHost.TIME_UTILTHREAD_SLEEP;
            C.tickTock((int)diff);
            return C;
        }
        catch(CloneNotSupportedException e)
        {
            
        }
        return globalClock;
    }

	public void raiseLowerTheSunEverywhere()
	{
	    try
	    {
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R!=null)
				&&(R.getArea()!=null)
				&&(R.getArea().getTimeObj()==this)
				&&((R.numInhabitants()>0)||(R.numItems()>0)))
				{
					R.recoverEnvStats();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB mob=R.fetchInhabitant(m);
						if((mob!=null)
						&&(!mob.isMonster()))
						{
							if(CoffeeUtensils.hasASky(R)
							&&(!Sense.isSleeping(mob))
							&&(Sense.canSee(mob)))
							{
								switch(getTODCode())
								{
								case TimeClock.TIME_DAWN:
									mob.tell("The sun begins to rise in the west.");
									break;
								case TimeClock.TIME_DAY:
									break;
									//mob.tell("The sun is now shining brightly."); break;
								case TimeClock.TIME_DUSK:
									mob.tell("The sun begins to set in the east."); break;
								case TimeClock.TIME_NIGHT:
									mob.tell("The sun has set and darkness again covers the world."); break;
								}
							}
							else
							{
								switch(getTODCode())
								{
								case TimeClock.TIME_DAWN:
									mob.tell("It is now daytime."); break;
								case TimeClock.TIME_DAY: break;
									//mob.tell("The sun is now shining brightly."); break;
								case TimeClock.TIME_DUSK: break;
									//mob.tell("It is almost nighttime."); break;
								case TimeClock.TIME_NIGHT:
									mob.tell("It is nighttime."); break;
								}
							}
						}
					}
				}
				R.recoverRoomStats();
			}
	    }catch(java.util.NoSuchElementException x){}
	}

	public void tickTock(int howManyHours)
	{
	    int todCode=getTODCode();
		if(howManyHours!=0)
		{
			setTimeOfDay(getTimeOfDay()+howManyHours);
			lastTicked=System.currentTimeMillis();
			while(getTimeOfDay()>=getHoursInDay())
			{
				setTimeOfDay(getTimeOfDay()-getHoursInDay());
				setDayOfMonth(getDayOfMonth()+1);
				if(getDayOfMonth()>getDaysInMonth())
				{
					setDayOfMonth(1);
					setMonth(getMonth()+1);
					if(getMonth()>getMonthsInYear())
					{
						setMonth(1);
						setYear(getYear()+1);
					}
				}
			}
			while(getTimeOfDay()<0)
			{
				setTimeOfDay(getHoursInDay()+getTimeOfDay());
				setDayOfMonth(getDayOfMonth()-1);
				if(getDayOfMonth()<1)
				{
					setDayOfMonth(getDaysInMonth());
					setMonth(getMonth()-1);
					if(getMonth()<1)
					{
						setMonth(getMonthsInYear());
						setYear(getYear()-1);
					}
				}
			}
		}
		if(getTODCode()!=todCode) raiseLowerTheSunEverywhere();
	}
	public void save()
	{
		if((loaded)&&(loadName!=null))
		{
			CMClass.DBEngine().DBDeleteData(loadName,"TIMECLOCK");
			CMClass.DBEngine().DBCreateData(loadName,"TIMECLOCK","TIMECLOCK/"+loadName,
			"<DAY>"+getDayOfMonth()+"</DAY><MONTH>"+getMonth()+"</MONTH><YEAR>"+getYear()+"</YEAR>"
			+"<HOURS>"+getHoursInDay()+"</HOURS><DAYS>"+getDaysInMonth()+"</DAYS>"
			+"<MONTHS>"+Util.toStringList(getMonthNames())+"</MONTHS>"
			+"<DAWNHR>"+getDawnToDusk()[TIME_DAWN]+"</DAWNHR>"
			+"<DAYHR>"+getDawnToDusk()[TIME_DAY]+"</DAYHR>"
			+"<DUSKHR>"+getDawnToDusk()[TIME_DUSK]+"</DUSKHR>"
			+"<NIGHTHR>"+getDawnToDusk()[TIME_NIGHT]+"</NIGHTHR>"
			+"<WEEK>"+Util.toStringList(getWeekNames())+"</WEEK>"
			+"<YEARS>"+Util.toStringList(getYearNames())+"</YEARS>"
			);
		}
	}

	public long lastTicked=0;
	public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_NOT;
		synchronized(this)
		{
			if((loadName!=null)&&(!loaded))
			{
				loaded=true;
				Vector V=CMClass.DBEngine().DBReadData(loadName,"TIMECLOCK");
				String timeRsc=null;
				if((V==null)||(V.size()==0)||(!(V.elementAt(0) instanceof Vector)))
					timeRsc="<TIME>-1</TIME><DAY>1</DAY><MONTH>1</MONTH><YEAR>1</YEAR>";
				else
					timeRsc=(String)((Vector)V.elementAt(0)).elementAt(3);
				V=XMLManager.parseAllXML(timeRsc);
				setTimeOfDay(XMLManager.getIntFromPieces(V,"TIME"));
				setDayOfMonth(XMLManager.getIntFromPieces(V,"DAY"));
				setMonth(XMLManager.getIntFromPieces(V,"MONTH"));
				setYear(XMLManager.getIntFromPieces(V,"YEAR"));
				if(this!=globalClock)
				{
					if((XMLManager.getValFromPieces(V,"HOURS").length()==0)
					||(XMLManager.getValFromPieces(V,"DAYS").length()==0)
					||(XMLManager.getValFromPieces(V,"MONTHS").length()==0))
					{
						setHoursInDay(globalClock.getHoursInDay());
						setDaysInMonth(globalClock.getDaysInMonth());
						setMonthsInYear(globalClock.getMonthNames());
						setDawnToDusk(globalClock.getDawnToDusk()[TIME_DAWN],
									  globalClock.getDawnToDusk()[TIME_DAY],
									  globalClock.getDawnToDusk()[TIME_DUSK],
									  globalClock.getDawnToDusk()[TIME_NIGHT]);
						setDaysInWeek(globalClock.getWeekNames());
						setYearNames(globalClock.getYearNames());
					}
					else
					{
						setHoursInDay(XMLManager.getIntFromPieces(V,"HOURS"));
						setDaysInMonth(XMLManager.getIntFromPieces(V,"DAYS"));
						setMonthsInYear(Util.toStringArray(Util.parseCommas(XMLManager.getValFromPieces(V,"MONTHS"),true)));
						setDawnToDusk(XMLManager.getIntFromPieces(V,"DAWNHR"),
									  XMLManager.getIntFromPieces(V,"DAYHR"),
									  XMLManager.getIntFromPieces(V,"DUSKHR"),
									  XMLManager.getIntFromPieces(V,"NIGHTHR"));
						setDaysInWeek(Util.toStringArray(Util.parseCommas(XMLManager.getValFromPieces(V,"WEEK"),true)));
						setYearNames(Util.toStringArray(Util.parseCommas(XMLManager.getValFromPieces(V,"YEARS"),true)));
					}
				}
			}
			if((System.currentTimeMillis()-lastTicked)>MudHost.TIME_MILIS_PER_MUDHOUR)
				tickTock(1);
		}
		return true;
	}
}
