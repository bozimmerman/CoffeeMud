package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DefaultTimeClock implements TimeClock
{
	public String ID(){return "DefaultTimeClock";}
	public String name(){return "Time Object";}
	protected long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	private boolean loaded=false;
	private String loadName=null;
	public void setLoadName(String name){loadName=name;}
	private int year=1;
	private int month=1;
	private int day=1;
	private int time=0;
	public String timeDescription(MOB mob, Room room)
	{
		StringBuffer timeDesc=new StringBuffer("");

		if((Sense.canSee(mob))&&(getTODCode()>=0))
			timeDesc.append(TOD_DESC[getTODCode()]);
		timeDesc.append("(Hour: "+getTimeOfDay()+"/"+(TimeClock.A_FULL_DAY-1)+")");
		timeDesc.append("\n\rIt is the "+getDayOfMonth()+numAppendage(getDayOfMonth()));
		timeDesc.append(" day of the "+getMonth()+numAppendage(getMonth()));
		timeDesc.append(" month.  It is "+(TimeClock.SEASON_DESCS[getSeasonCode()]).toLowerCase()+".");
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
		switch(num)
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
		switch(month)
		{
		case 1: return TimeClock.SEASON_WINTER;
		case 2: return TimeClock.SEASON_WINTER; 
		case 3: return TimeClock.SEASON_SPRING; 
		case 4: return TimeClock.SEASON_SPRING; 
		case 5: return TimeClock.SEASON_SPRING; 
		case 6: return TimeClock.SEASON_SUMMER; 
		case 7: return TimeClock.SEASON_SUMMER; 
		case 8: return TimeClock.SEASON_SUMMER; 
		case 9: return TimeClock.SEASON_FALL; 
		case 10:return TimeClock.SEASON_FALL; 
		case 11:return TimeClock.SEASON_FALL; 
		case 12:return TimeClock.SEASON_WINTER; 
		}
		return TimeClock.SEASON_WINTER;
	}
	public int getMonth(){return month;}
	public void setMonth(int m){month=m;}
	public int getMoonPhase(){return (int)Math.round(Math.floor(Util.mul(Util.div(getDayOfMonth(),TimeClock.DAYS_IN_MONTH),8.0)));}

	public int getDayOfMonth(){return day;}
	public void setDayOfMonth(int d){day=d;}
	public int getTimeOfDay(){return time;}
	public int getTODCode()
	{
		switch(time)
		{
		case 0:return TimeClock.TIME_DAWN;
		case 12:return TimeClock.TIME_DUSK;
		case 13:return TimeClock.TIME_NIGHT;
		case 14:return TimeClock.TIME_NIGHT;
		case 15:return TimeClock.TIME_NIGHT;
		default:return TimeClock.TIME_DAY;
		}
	}
	public boolean setTimeOfDay(int t)
	{
		boolean raiseLowerTheSun=false;
		switch(t)
		{
		case 0:raiseLowerTheSun=true; break;
		case 1:raiseLowerTheSun=true; break;
		case 12:raiseLowerTheSun=true; break;
		case 13:raiseLowerTheSun=true; break;
		case 14:break;
		case 15:break;
		default:break;
		}
		time=t;
		return raiseLowerTheSun;
	}

	public void raiseLowerTheSunEverywhere()
	{
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R!=null)&&((R.numInhabitants()>0)||(R.numItems()>0)))
			{
				R.recoverEnvStats();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB mob=R.fetchInhabitant(m);
					if(!mob.isMonster())
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
	}

	public void tickTock(int howManyHours)
	{
		if(howManyHours!=0)
		{
			boolean raiseLowerTheSun=setTimeOfDay(getTimeOfDay()+howManyHours);
			lastTicked=System.currentTimeMillis();
			if(getTimeOfDay()>=TimeClock.A_FULL_DAY)
			{
				raiseLowerTheSun=setTimeOfDay(getTimeOfDay()-TimeClock.A_FULL_DAY);
				setDayOfMonth(getDayOfMonth()+1);
				if(getDayOfMonth()>TimeClock.DAYS_IN_MONTH)
				{
					setDayOfMonth(1);
					setMonth(getMonth()+1);
					if(getMonth()>TimeClock.MONTHS_IN_YEAR)
					{
						setMonth(1);
						setYear(getYear()+1);
					}
				}
			}
			else
			if(getTimeOfDay()<0)
			{
				raiseLowerTheSun=setTimeOfDay(TimeClock.A_FULL_DAY+getTimeOfDay());
				setDayOfMonth(getDayOfMonth()-1);
				if(getDayOfMonth()<1)
				{
					setDayOfMonth(TimeClock.DAYS_IN_MONTH);
					setMonth(getMonth()-1);
					if(getMonth()<1)
					{
						setMonth(TimeClock.MONTHS_IN_YEAR);
						setYear(getYear()-1);
					}
				}
			}
			if(raiseLowerTheSun) raiseLowerTheSunEverywhere();
		}
	}
	public void save()
	{
		if((loaded)&&(loadName!=null))
		{
			CMClass.DBEngine().DBDeleteData(loadName,"TIMECLOCK");
			CMClass.DBEngine().DBCreateData(loadName,"TIMECLOCK","TIMECLOCK/"+loadName,
			"<DAY>"+getDayOfMonth()+"</DAY><MONTH>"+getMonth()+"</MONTH><YEAR>"+getYear()+"</YEAR>");
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
			}
			if((System.currentTimeMillis()-lastTicked)>MudHost.TIME_TICK_DELAY)
				tickTock(1);
		}
		return true;
	}
}
