package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdArea implements Area
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="the area";
	protected String description="";
	protected String miscText="";
	protected int climateID=Area.CLIMASK_NORMAL;
	protected int currentWeather=Area.WEATHER_CLEAR;
	protected int nextWeather=Area.WEATHER_CLEAR;
	protected Vector myRooms=null;

	protected static int year=1;
	protected static int month=1;
	protected static int day=1;
	protected static int time=0;
	protected static int timeCode=Area.TIME_DAWN;
	
	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector subOps=new Vector();
	
	protected boolean stopTicking=false;
	private static final int WEATHER_TICK_DOWN=75; // 75 = 5 minutes * 60 seconds / 4
	protected int weatherTicker=WEATHER_TICK_DOWN;
	protected static int windDirection=Directions.NORTH;

	public StdArea()
	{
	}

	public String ID()
	{
		return myID;
	}
	public String name(){ return name;}
	public void setName(String newName){name=newName;}
	public EnvStats envStats()
	{
		return envStats;
	}
	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public int weatherType(Room room)
	{
		if((room.domainType()&Room.INDOORS)==(Room.INDOORS))
			return Area.WEATHER_CLEAR;
		return currentWeather;
	}
	public String weatherDescription(Room room)
	{
		if((room.domainType()&Room.INDOORS)==(Room.INDOORS))
			return "You can't tell much about the weather from here.";
		return getWeatherDescription();
	}
	public String timeDescription(MOB mob, Room room)
	{
		StringBuffer timeDesc=new StringBuffer("");
		
		if(Sense.canSee(mob))
		switch(timeCode)
		{
		case Area.TIME_DAWN: timeDesc.append("It is dawn "); break;
		case Area.TIME_DAY: timeDesc.append("It is daytime "); break;
		case Area.TIME_DUSK: timeDesc.append("It is dusk "); break;
		case Area.TIME_NIGHT: timeDesc.append("It is nighttime "); break;
		}
		timeDesc.append("(Hour: "+getTimeOfDay()+"/"+(Area.A_FULL_DAY-1)+")");
		
		if(Sense.canSee(mob))
		if((timeCode==Area.TIME_NIGHT)&&((room.domainType()&Room.INDOORS)==0))
		{
			switch(weatherType(room))
			{
			case Area.WEATHER_BLIZZARD:
			case Area.WEATHER_HAIL:
			case Area.WEATHER_SLEET:
			case Area.WEATHER_SNOW:
			case Area.WEATHER_RAIN:
			case Area.WEATHER_THUNDERSTORM:
				timeDesc.append("\n\r"+weatherDescription(room)+" You can't see the moon."); break;
			case Area.WEATHER_CLOUDY:
				timeDesc.append("\n\rThe clouds obscure the moon."); break;
			case Area.WEATHER_DUSTSTORM:
				timeDesc.append("\n\rThe dust obscures the moon."); break;
			default:
				switch(getMoonPhase())
				{
				case 0: timeDesc.append("\n\rThere is a new moon in the sky."); break;
				case 1: timeDesc.append("\n\rThe moon is in the waxing crescent phase."); break;
				case 2: timeDesc.append("\n\rThe moon is in its first quarter."); break;
				case 3: timeDesc.append("\n\rThe moon is in the waxing gibbous phase (almost full)."); break;
				case 4: timeDesc.append("\n\rThere is a full moon in the sky."); break;
				case 5: timeDesc.append("\n\rThe moon is in the waning gibbous phase (no longer full)."); break;
				case 6: timeDesc.append("\n\rThe moon is in its last quarter."); break;
				case 7: timeDesc.append("\n\rThe moon is in the waning crescent phase."); break;
				default: timeDesc.append("\n\rThere is a BLUE MOON! Oh my GOD! Run away!!!!!"); break;
				}
			}
		}
		return timeDesc.toString();
	}
	public int climateType(){return climateID;}
	public void setClimateType(int newClimateType)
	{
		climateID=newClimateType;
	}
	public int getYear(){return year;}
	public void setYear(int y){year=y;}

	public int getMonth(){return month;}
	public void setMonth(int m){month=m;}
	public int getMoonPhase(){return (int)Math.round(Util.mul(Util.div(getDayOfMonth(),Area.DAYS_IN_MONTH),8));}
	
	public int getDayOfMonth(){return day;}
	public void setDayOfMonth(int d){day=d;}
	public int getTimeOfDay(){return time;}
	public int getTODCode(){return timeCode;}
	public boolean setTimeOfDay(int t)
	{
		boolean raiseLowerTheSun=false;
		switch(t)
		{
		case 0:timeCode=Area.TIME_DAWN; raiseLowerTheSun=true; break;
		case 1:timeCode=Area.TIME_DAY; raiseLowerTheSun=true; break;
		case 12:timeCode=Area.TIME_DUSK; raiseLowerTheSun=true; break;
		case 13:timeCode=Area.TIME_NIGHT; raiseLowerTheSun=true; break;
		case 14:
		case 15:timeCode=Area.TIME_NIGHT; break;
		default:timeCode=Area.TIME_DAY; break;
		}
		time=t;
		return raiseLowerTheSun;
	}
	
	
	public boolean amISubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				return true;
		}
		return false;
	}
	public String getSubOpList()
	{
		StringBuffer list=new StringBuffer("");
		for(int s=subOps.size()-1;s>=0;s--)
		{
			String str=((String)subOps.elementAt(s));
			list.append(str);
			list.append(";");
		}
		return list.toString();
	}
	public void setSubOpList(String list)
	{
		subOps=new Vector();
		int x=list.indexOf(";");
		while(x>0)
		{
			subOps.addElement(list.substring(0,x).trim());
			list=list.substring(x+1).trim();
			x=list.indexOf(";");
		}
		if((list.trim().length()>0)&&(!list.trim().equalsIgnoreCase(";")))
			subOps.addElement(list.trim());
	}
	public void addSubOp(String username){subOps.addElement(username);}
	public void delSubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				subOps.removeElementAt(s);
		}
	}
	
	public Environmental newInstance()
	{
		return new StdArea();
	}
	public boolean isGeneric(){return false;}
	private void cloneFix(Area E)
	{
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();

		affects=new Vector();
		behaviors=new Vector();
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				behaviors.addElement(B);
		}
		for(int a=0;a<E.numAffects();a++)
		{
			Ability A=E.fetchAffect(a);
			if(A!=null)
				affects.addElement(A);
		}
		setSubOpList(E.getSubOpList());
	}
	public Environmental copyOf()
	{
		try
		{
			StdArea E=(StdArea)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public String displayText(){return "";}
	public void setDisplayText(String newDisplayText){}
	
	public String text()
	{
		return Generic.getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			Generic.setPropertiesStr(this,newMiscText,true);
	}
	
	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}

	public boolean okAffect(Affect affect)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okAffect(this,affect)))
				return false;
		}
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(!A.okAffect(affect)))
				return false;
		}
		return true;
	}

	public void affect(Affect affect)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.affect(this,affect);
		}
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if(A!=null)
				A.affect(affect);
		}
	}

	public void tickControl(boolean start)
	{
		if(start)
		{
			stopTicking=false;
			ExternalPlay.startTickDown(this,Host.AREA_TICK,1);
		}
		else
			stopTicking=true;
		
	}
	
	public void weatherTick()
	{
		if((--weatherTicker)<=0)
		{
				
			weatherTicker=WEATHER_TICK_DOWN;
			int[] changeChance=new int[Area.NUM_WEATHER];
			for(int c=0;c<changeChance.length;c++) changeChance[c]=50;
			changeChance[Area.WEATHER_BLIZZARD]=85;
			changeChance[Area.WEATHER_CLEAR]=15;
			changeChance[Area.WEATHER_CLOUDY]=25;
			changeChance[Area.WEATHER_DROUGHT]=15;
			changeChance[Area.WEATHER_DUSTSTORM]=85;
			changeChance[Area.WEATHER_HAIL]=90;
			changeChance[Area.WEATHER_HEAT_WAVE]=50;
			changeChance[Area.WEATHER_RAIN]=50;
			changeChance[Area.WEATHER_SLEET]=90;
			changeChance[Area.WEATHER_SNOW]=65;
			changeChance[Area.WEATHER_THUNDERSTORM]=65;
			changeChance[Area.WEATHER_WINDY]=25;
			changeChance[Area.WEATHER_WINTER_COLD]=15;
			
			String say=null;
			int oldWeather=currentWeather;
			if(Dice.rollPercentage()<changeChance[currentWeather])
			{
				String stopWord="";
				int[] chanceToDo=new int[Area.NUM_WEATHER];
				int chanceToRain=0;
				int chanceToSnow=0;
				int chanceToHail=0;
				int chanceForClouds=10;
				int chanceForStorm=0;
				int chanceForWind=5;
				if((climateType()&Area.CLIMATE_WINDY)>0)
					chanceForWind=15;
				if((climateType()&Area.CLIMASK_WET)>0)
					chanceForClouds=35;
				else
				if((climateType()&Area.CLIMASK_DRY)>0)
					chanceForClouds=5;
				switch(oldWeather)
				{
				case Area.WEATHER_HAIL:
					stopWord="The hailstorm stops.";
					break;
				case Area.WEATHER_CLOUDY:
					stopWord="The clouds dissipate.";
					chanceForClouds=0;
					if((climateType()&Area.CLIMASK_WET)>0)
					{
						if((climateType()&Area.CLIMASK_COLD)>0)
						{
							chanceToSnow=20;
							chanceToHail=20;
							chanceToRain=10;
						}
						else
						if((climateType()&Area.CLIMASK_HOT)>0)
						{
							chanceForStorm=20;
							chanceToRain=35;
							chanceToSnow=2;
						}
						else
						{
							chanceForStorm=10;
							chanceToRain=25;
							chanceToSnow=5;
							chanceToHail=5;
						}
					}
					else
					if((climateType()&Area.CLIMASK_DRY)>0)
					{
						if((climateType()&Area.CLIMASK_COLD)>0)
						{
							chanceToSnow=5;
							chanceToHail=5;
							chanceToRain=2;
						}
						else
						if((climateType()&Area.CLIMASK_HOT)>0)
						{
							chanceForStorm=5;
							chanceToRain=9;
							chanceToSnow=1;
						}
						else
						{
							chanceForStorm=2;
							chanceToRain=7;
							chanceToSnow=1;
							chanceToHail=0;
						}
					}
					else
					{
						if((climateType()&Area.CLIMASK_COLD)>0)
						{
							chanceToSnow=10;
							chanceToHail=10;
							chanceToRain=5;
						}
						else
						if((climateType()&Area.CLIMASK_HOT)>0)
						{
							chanceForStorm=10;
							chanceToRain=18;
							chanceToSnow=1;
						}
						else
						{
							chanceForStorm=5;
							chanceToRain=10;
							chanceToSnow=2;
							chanceToHail=2;
						}
					}
					break;
				case Area.WEATHER_THUNDERSTORM:
					stopWord="The thunderstorm stops.";
					break;
				case Area.WEATHER_CLEAR:
					break;
				case Area.WEATHER_RAIN:
					stopWord="It stops raining.";
					break;
				case Area.WEATHER_SNOW:
					stopWord="It stops snowing.";
					break;
				case Area.WEATHER_WINDY:
					chanceForWind=0;
					stopWord="The wind gusts stop.";
					if((climateType()&Area.CLIMASK_WET)>0)
					{
						if((climateType()&Area.CLIMASK_COLD)>0)
							chanceForClouds=45;
						else
						if((climateType()&Area.CLIMASK_HOT)>0)
							chanceForClouds=55;
					}
					else
					if((climateType()&Area.CLIMASK_DRY)>0)
						chanceForClouds=0;
					else
					if((climateType()&Area.CLIMASK_COLD)>0)
						chanceForClouds=5;
					else
					if((climateType()&Area.CLIMASK_HOT)>0)
						chanceForClouds=3;
					break;
				}
				int newWeather=nextWeather;
				if(Dice.rollPercentage()<chanceForClouds)
					nextWeather=Area.WEATHER_CLOUDY;
				else
				if(Dice.rollPercentage()<chanceForWind)
					nextWeather=Area.WEATHER_WINDY;
				else
				if(Dice.rollPercentage()<chanceToSnow)
					nextWeather=Area.WEATHER_SNOW;
				else
				if(Dice.rollPercentage()<chanceToHail)
					nextWeather=Area.WEATHER_HAIL;
				else
				if(Dice.rollPercentage()<chanceToRain)
					nextWeather=Area.WEATHER_RAIN;
				else
				if(Dice.rollPercentage()<chanceForStorm)
					nextWeather=Area.WEATHER_THUNDERSTORM;
					
				currentWeather=newWeather;
				switch(oldWeather)
				{
				case Area.WEATHER_CLEAR:
					switch(newWeather)
					{
					case Area.WEATHER_CLEAR:
						break;
					default:
						say=getWeatherDescription();
						break;
					}
					break;
				case Area.WEATHER_CLOUDY:
					switch(newWeather)
					{
					case Area.WEATHER_CLEAR:
						say=stopWord;
						break;
					case Area.WEATHER_CLOUDY:
						break;
					case Area.WEATHER_HAIL:
					case Area.WEATHER_RAIN:
					case Area.WEATHER_SNOW:
					case Area.WEATHER_THUNDERSTORM:
						say=getWeatherDescription();
						break;
					case Area.WEATHER_WINDY:
						say=stopWord+" "+getWeatherDescription();
						break;
					}
					break;
				case Area.WEATHER_HAIL:
					switch(newWeather)
					{
					case Area.WEATHER_CLEAR:
					case Area.WEATHER_CLOUDY:
						say=stopWord;
						break;
					case Area.WEATHER_HAIL:
						break;
					case Area.WEATHER_RAIN:
					case Area.WEATHER_SNOW:
					case Area.WEATHER_THUNDERSTORM:
					case Area.WEATHER_WINDY:
						say=stopWord+" "+getWeatherDescription();
						break;
					}
					break;
				case Area.WEATHER_RAIN:
					switch(newWeather)
					{
					case Area.WEATHER_CLEAR:
					case Area.WEATHER_CLOUDY:
					case Area.WEATHER_WINDY:
						say=stopWord;
						break;
					case Area.WEATHER_HAIL:
					case Area.WEATHER_SNOW:
					case Area.WEATHER_THUNDERSTORM:
						say=stopWord+" "+getWeatherDescription();
						break;
					case Area.WEATHER_RAIN:
						break;
					}
					break;
				case Area.WEATHER_SNOW:
					switch(newWeather)
					{
					case Area.WEATHER_CLEAR:
					case Area.WEATHER_CLOUDY:
						say=stopWord;
						break;
					case Area.WEATHER_HAIL:
					case Area.WEATHER_THUNDERSTORM:
					case Area.WEATHER_RAIN:
					case Area.WEATHER_WINDY:
						say=stopWord+" "+getWeatherDescription();
						break;
					case Area.WEATHER_SNOW:
						break;
					}
					break;
				case Area.WEATHER_THUNDERSTORM:
					switch(newWeather)
					{
					case Area.WEATHER_CLEAR:
					case Area.WEATHER_CLOUDY:
					case Area.WEATHER_WINDY:
						say=stopWord;
						break;
					case Area.WEATHER_HAIL:
					case Area.WEATHER_SNOW:
					case Area.WEATHER_RAIN:
						say=stopWord+" "+getWeatherDescription();
						break;
					case Area.WEATHER_THUNDERSTORM:
						break;
					}
					break;
				case Area.WEATHER_WINDY:
					switch(newWeather)
					{
					case Area.WEATHER_CLEAR:
						say=stopWord;
						break;
					case Area.WEATHER_CLOUDY:
					case Area.WEATHER_HAIL:
					case Area.WEATHER_SNOW:
					case Area.WEATHER_THUNDERSTORM:
					case Area.WEATHER_RAIN:
						say=getWeatherDescription();
						break;
					case Area.WEATHER_WINDY:
						break;
					}
					break;
				}
			}
			else
			if(currentWeather==Area.WEATHER_THUNDERSTORM)
				say="A bolt of lightning streaks across the sky.";
			if(say!=null)
			{
				Vector myMap=this.getMyMap();
				for(int r=0;r<myMap.size();r++)
				{
					Room R=(Room)myMap.elementAt(r);
					if((R.domainType()&Room.INDOORS)==0)
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB mob=R.fetchInhabitant(i);
						if((mob!=null)
						   &&(!mob.isMonster())
						   &&(Sense.canSee(mob)||(currentWeather!=oldWeather)))
							mob.tell(say);
					}
				}
			}
		}
	}
	
	public boolean tick(int tickID)
	{
		if(stopTicking) return false;
		if(tickID==Host.AREA_TICK)
		{
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(this,tickID);
			}

			int a=0;
			while(a<numAffects())
			{
				Ability A=fetchAffect(a);
				if(A!=null)
				{
					int s=affects.size();
					if(!A.tick(tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
			weatherTick();
		}
		return true;
	}
	
	protected String theWeatherDescription(int weather)
	{
		StringBuffer desc=new StringBuffer("");
		switch(weather)
		{
		case Area.WEATHER_HAIL:
			if((climateType()&Area.CLIMASK_COLD)>0)
				desc.append("Golfball sized clumps of ice ");
			else
				desc.append("Light streams of sleet and hail ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirl down from above.");
			else
				desc.append("fall from the sky.");
			break;
		case Area.WEATHER_CLOUDY:
			if((climateType()&Area.CLIMASK_COLD)>0)
				desc.append("Grey and gloomy clouds ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("Dark and looming stormclouds ");
			else
			if((climateType()&Area.CLIMASK_HOT)>0)
				desc.append("Light whisps of cloud ");
			else
				desc.append("Fluffy cloudbanks ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("move across the sky.");
			else
				desc.append("obscure the sky.");
			break;
		case Area.WEATHER_THUNDERSTORM:
			desc.append("A heavy and thunderous rainstorm ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("pours down from above.");
			break;
		case Area.WEATHER_CLEAR:
			if((climateType()&Area.CLIMASK_COLD)>0)
				desc.append("The weather is cool and clear.");
			else
			if((climateType()&Area.CLIMASK_HOT)>0)
			{
				if((climateType()&Area.CLIMASK_WET)>0)
					desc.append("The weather is warm and humid, but clear.");
				else
					desc.append("The weather is warm and clear");
			}
			else
				desc.append("The weather is clear.");
			break;
		case Area.WEATHER_RAIN:
			if((climateType()&Area.CLIMASK_COLD)>0)
				desc.append("A cold light rain ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("A cool soaking rain ");
			else
			if((climateType()&Area.CLIMASK_HOT)>0)
				desc.append("A warm steaming rain ");
			else
				desc.append("A light rain ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Area.WEATHER_SNOW:
			if((climateType()&Area.CLIMASK_COLD)>0)
				desc.append("A light snow ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("A slushy snow ");
			else
			if((climateType()&Area.CLIMASK_HOT)>0)
				desc.append("A freakish snow ");
			else
				desc.append("An unseasonable snow ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Area.WEATHER_WINDY:
			if((climateType()&Area.CLIMASK_COLD)>0)
				desc.append("A cold "+(((climateType()&Area.CLIMASK_HOT)>0)?"dry ":"")+"wind ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("A forboding gust of wind ");
			else
			if((climateType()&Area.CLIMASK_HOT)>0)
				desc.append("A hot "+(((climateType()&Area.CLIMASK_HOT)>0)?"dry ":"")+"wind ");
			else
				desc.append("A light "+(((climateType()&Area.CLIMASK_HOT)>0)?"dry ":"")+"wind ");
			desc.append("blows from the "+Directions.getFromDirectionName(this.windDirection));
			break;
		}
		return desc.toString();
	}
	
	public String getWeatherDescription()
	{
		return theWeatherDescription(currentWeather);
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((affected instanceof Room)&&((((Room)affected).domainType()&Room.INDOORS)==0))
		{
			if((weatherType((Room)affected)==Area.WEATHER_BLIZZARD)
			   ||(weatherType((Room)affected)==Area.WEATHER_DUSTSTORM)
			   ||(timeCode==Area.TIME_NIGHT))
					affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_DARK);
		}
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public void addNonUninvokableAffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addAffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delAffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numAffects()
	{
		return affects.size();
	}
	public Ability fetchAffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchAffect(String ID)
	{
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}
	
	public StringBuffer getAreaStats()
	{
		StringBuffer s=new StringBuffer("");
		s.append(description()+"\n\r");
		Vector myMap=this.getMyMap();
		s.append("Number of rooms: "+myMap.size()+"\n\r");
		
		Vector mobRanges=new Vector();
		int totalMOBs=0;
		int lowestLevel=Integer.MAX_VALUE;
		int highestLevel=Integer.MIN_VALUE;
		int totalLevels=0;
		int averageLevel=0;
		int medianLevel=0;
		long totalAlignments=0;
		int averageAlignment=0;
		for(int r=0;r<myMap.size();r++)
		{
			Room R=(Room)myMap.elementAt(r);
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB mob=R.fetchInhabitant(i);
				if((mob!=null)&&(mob.isMonster()))
				{
					int lvl=mob.baseEnvStats().level();
					mobRanges.addElement(new Integer(lvl));
					totalAlignments+=mob.getAlignment();
					totalMOBs++;
					totalLevels+=lvl;
					if(lvl<lowestLevel)
						lowestLevel=lvl;
					if(lvl>highestLevel)
						highestLevel=lvl;
				}
			}
		}
		if(mobRanges.size()>0)
		{
			Collections.sort((List)mobRanges);
			medianLevel=((Integer)mobRanges.elementAt((int)Math.round(Math.floor(Util.div(mobRanges.size(),2.0))))).intValue();
			averageLevel=(int)Math.round(Util.div(totalLevels,totalMOBs));
			averageAlignment=(int)Math.round(new Long(totalAlignments).doubleValue()/new Integer(totalMOBs).doubleValue());
			s.append("Level range    : "+lowestLevel+" to "+highestLevel+"\n\r");
			s.append("Average level  : "+averageLevel+"\n\r");
			s.append("Median level   : "+medianLevel+"\n\r");
			s.append("Alignment avg. : "+averageAlignment+" ("+ExternalPlay.alignmentStr(averageAlignment)+")\n\r");
		}
		return s;
	}
	
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	
	public void clearMap(){myRooms=null;}
	
	public synchronized Vector getMyMap()
	{
		if(myRooms!=null) return myRooms;
		Vector myMap=new Vector();
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room R=CMMap.getRoom(m);
			if(R.getArea()==this)
				myMap.addElement(R);
		}
		myRooms=myMap;
		return myMap;
	}
	
	public int nextWeatherType(Room room)
	{
		if((room.domainType()&Room.INDOORS)==(Room.INDOORS))
			return Area.WEATHER_CLEAR;
		return nextWeather;
	}
	public String nextWeatherDescription(Room room)
	{
		if((room.domainType()&Room.INDOORS)==(Room.INDOORS))
			return "You can't tell much about the weather from here.";
		return getNextWeatherDescription();
	}
	public String getNextWeatherDescription()
	{
		return theWeatherDescription(nextWeather);
	}
	
}
