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

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector subOps=new Vector();
	
	protected boolean stopTicking=false;
	private static final int WEATHER_TICK_DOWN=450; // 450 = 30 minutes * 60 seconds / 4
	protected int weatherTicker=WEATHER_TICK_DOWN;
	protected int windDirection=Directions.NORTH;

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
	public int climateType(){return climateID;}
	public void setClimateType(int newClimateType)
	{
		climateID=newClimateType;
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
			if((--weatherTicker)<=0)
			{
				weatherTicker=WEATHER_TICK_DOWN;
				int changeChance=50;
				switch(currentWeather)
				{
				case Area.WEATHER_HAIL:
					changeChance=90; 
					break;
				case Area.WEATHER_CLOUDY:
					changeChance=25;
					break;
				case Area.WEATHER_THUNDERSTORM:
					changeChance=65;
					break;
				case Area.WEATHER_CLEAR:
					changeChance=15; 
					break;
				case Area.WEATHER_RAIN:
					changeChance=50; 
					break;
				case Area.WEATHER_SNOW:
					changeChance=65; 
					break;
				case Area.WEATHER_WINDY:
					changeChance=25;
					break;
				}
				String say=null;
				int oldWeather=currentWeather;
				if(Dice.rollPercentage()<changeChance)
				{
					String stopWord="";
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
					switch(currentWeather)
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
					int newWeather=0;
					if(Dice.rollPercentage()<chanceToRain)
						newWeather=Area.WEATHER_RAIN;
					else
					if(Dice.rollPercentage()<chanceToSnow)
						newWeather=Area.WEATHER_SNOW;
					else
					if(Dice.rollPercentage()<chanceToHail)
						newWeather=Area.WEATHER_HAIL;
					else
					if(Dice.rollPercentage()<chanceForClouds)
						newWeather=Area.WEATHER_CLOUDY;
					else
					if(Dice.rollPercentage()<chanceForWind)
						newWeather=Area.WEATHER_WINDY;
					else
					if(Dice.rollPercentage()<chanceForStorm)
						newWeather=Area.WEATHER_THUNDERSTORM;
					
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
					say="A bolt of lightening streaks across the sky.";
				if(say!=null)
				{
					Vector myMap=this.getMyMap();
					for(int r=0;r<myMap.size();r++)
					{
						Room R=(Room)myMap.elementAt(r);
						if((R.domainType()&Room.INDOORS)==(Room.INDOORS))
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
		return true;
	}
	
	public String getWeatherDescription()
	{
		StringBuffer desc=new StringBuffer("");
		switch(currentWeather)
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
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{}
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
	
	public Vector getMyMap()
	{
		Vector myMap=new Vector();
		for(int m=0;m<CMMap.map.size();m++)
		{
			Room R=(Room)CMMap.map.elementAt(m);
			if(R.getArea()==this)
				myMap.addElement(R);
		}
		return myMap;
	}
}
