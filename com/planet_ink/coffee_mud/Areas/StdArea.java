package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdArea implements Area
{
	public String ID(){	return "StdArea";}
	protected String name="the area";
	protected String description="";
	protected String miscText="";
	protected int climateID=Area.CLIMASK_NORMAL;
	protected int currentWeather=Area.WEATHER_CLEAR;
	protected int nextWeather=Area.WEATHER_CLEAR;
	protected int techLevel=0;
	protected Vector myRooms=null;
	protected boolean mobility=true;

	protected static int year=1;
	protected static int month=1;
	protected static int day=1;
	protected static int time=0;
	protected static int timeCode=Area.TIME_DAWN;
	protected static int seasonCode=Area.SEASON_SPRING;
	protected static boolean reReadTime=true;

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector subOps=new Vector();

	protected boolean stopTicking=false;
	private static final int WEATHER_TICK_DOWN=300; // 300 = 20 minutes * 60 seconds / 4
	protected int weatherTicker=WEATHER_TICK_DOWN;
	protected static int windDirection=Directions.NORTH;

	private final static
	int[] seasonalWeather={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  40, 20, 10, 14,  5,  1,  0,  5,  0,  0,  0,  0,  5,
		/*SUMMER*/  31, 20, 5,  10, 12,  0,  0, 20,  0,  0,  1,  1,  0,
		/*FALL*/    37, 10, 15, 15, 10,  5,  2,  5,  2,  1,  0,  0, 10,
		/*WINTER*/  32, 15, 11,  4,  2,  7,  3,  0,  3,  3,  0,  0, 20,
	};

	private final static
	int[] cold={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  -5, -5,  5,-10,  0,  5,  0, -5,  5,  0,  0,  0,  10,
		/*SUMMER*/   5,  1,  5,  0,  0,  1,  1,-20,  1,  1,  0,  0,  5,
		/*FALL*/     0,  0,  1, -5,  0,  1,  1, -5,  1,  1,  0,  0,  5,
		/*WINTER*/ -15,  0,  0, -4, -2,  5,  2,  0,  2,  2,  0,  0,  10,
	};
	private final static
	int[] hot={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/   5,  5, -5, 10,  0, -5,  0,  5, -5,  0,  0,  0, -10,
		/*SUMMER*/  -5, -1, -5,  0,  0, -1, -1, 20, -1, -1,  0,  0, -5,
		/*FALL*/     0,  0, -1,  5,  0, -1, -1,  5, -1, -1,  0,  0, -5,
		/*WINTER*/  15,  0,  0,  4,  2, -5, -2,  0, -2, -2,  0,  0, -10,
	};
	private final static
	int[] dry={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
		/*SUMMER*/  10,-22,  0,  0,  0,  0,  0,  0,  0,  0,  6,  6,   0,
		/*FALL*/    10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
		/*WINTER*/  10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
	};
	private final static
	int[] wet={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/ -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,  -2,
		/*SUMMER*/ -10, 22,  0,  0,  0,  0,  0,  0,  0,  0, -6, -6,   0,
		/*FALL*/   -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,  -2,
		/*WINTER*/ -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,   2,
	};
	private final static
	int[] windy={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/ -10,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   0,
		/*SUMMER*/ -10,  0, 11,  0,  0,  0,  0, -2,  0,  0,  0,  1,   0,
		/*FALL*/   -10,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   0,
		/*WINTER*/ -10, -2, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   2,
	};
	private final static
	int[] changeMap=		{
	/*				     -    CL   WD   RA   TH   SN   HA   HE   SL   BL   DU   DR   WC*/
	/*CLEAR*/			85,    0,   0,-100,-100,-100,-100,   0,-100,-100,   0, -20,   0,
	/*CLOUDY*/			 0,   75,   0,   0,   0,   0,   0,   0,   0,   0,-100,-100,   0,
	/*WINDY*/			 0,    0,  25,-100,-100,-100,-100,-100,-100,-100,   1,   0,   0,
	/*RAIN*/			-5,    5,   0,  50,   5, -20,   0,-100, -20,-100,-100,-100,   0,
	/*THUNDERSTORM*/	-5,   10,   5,   5,  35,-100,   0,   0,   0,-100,-100,-100,   0,
	/*SNOW*/			-5,    5,   0,-100,-100,  35,-100,-100,-100,   5,-100,-100,   5,
	/*HAIL*/			-5,    5,   0,  -8,  -8,-100,  10,-100,   0,-100,-100,-100,   5,
	/*HEAT*/			 0,    0,   0,  -8,  -8,-100,-100,  50,-100,-100,   0,   1,-100,
	/*SLEET*/			-5,    5,   0,  -8,  -8,   0,   0,   0,  10,   0,-100,   0,   5,
	/*BLIZZ*/			-5,    5,   0,-100,-100,   5,-100,-100,-100,  15,-100,   0,  10,
	/*DUST*/			-5,  -10,  20,-100,-100,-100,-100,   0,-100,-100,  15,   0,   0,
	/*DROUGHT*/		   -15,  -15,   0,-100,-100,-100,-100,   0,-100,-100,   1,  85,   0,
	/*WINTER*/			 0,    0,   0,   0,-100,-100,-100,-100,-100,-100,-100,  -5,  85,
	};

	public StdArea()
	{
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
	public void setNextWeatherType(int weatherCode){nextWeather=weatherCode;}
	public void setCurrentWeatherType(int weatherCode){currentWeather=weatherCode;}
	public int getTechLevel(){return techLevel;}
	public void setTechLevel(int level){techLevel=level;}
	public int weatherType(Room room)
	{
		if((room!=null)&&(room.domainType()&Room.INDOORS)==(Room.INDOORS))
			return Area.WEATHER_CLEAR;
		return currentWeather;
	}
	public String weatherDescription(Room room)
	{
		if((room!=null)&&(room.domainType()&Room.INDOORS)==(Room.INDOORS))
			return "You can't tell much about the weather from here.";
		return getWeatherDescription();
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
	public String timeDescription(MOB mob, Room room)
	{
		StringBuffer timeDesc=new StringBuffer("");

		if((Sense.canSee(mob))&&(timeCode>=0))
			timeDesc.append(TOD_DESC[timeCode]);
		timeDesc.append("(Hour: "+getTimeOfDay()+"/"+(Area.A_FULL_DAY-1)+")");
		timeDesc.append("\n\rIt is the "+getDayOfMonth()+numAppendage(getDayOfMonth()));
		timeDesc.append(" day of the "+getMonth()+numAppendage(getMonth()));
		timeDesc.append(" month.  It is "+(Area.SEASON_DESCS[getSeasonCode()]).toLowerCase()+".");
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
				if(getMoonPhase()>=0)
					timeDesc.append("\n\r"+MOON_PHASES[getMoonPhase()]);
				break;
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

	public int getSeasonCode(){ return seasonCode;}
	public int getMonth(){return month;}
	public void setMonth(int m)
	{
		switch(m)
		{
		case 1: seasonCode=Area.SEASON_WINTER; break;
		case 2: seasonCode=Area.SEASON_WINTER; break;
		case 3: seasonCode=Area.SEASON_SPRING; break;
		case 4: seasonCode=Area.SEASON_SPRING; break;
		case 5: seasonCode=Area.SEASON_SPRING; break;
		case 6: seasonCode=Area.SEASON_SUMMER; break;
		case 7: seasonCode=Area.SEASON_SUMMER; break;
		case 8: seasonCode=Area.SEASON_SUMMER; break;
		case 9: seasonCode=Area.SEASON_FALL; break;
		case 10:seasonCode=Area.SEASON_FALL; break;
		case 11:seasonCode=Area.SEASON_FALL; break;
		case 12:seasonCode=Area.SEASON_WINTER; break;
		}
		month=m;
	}
	public int getMoonPhase(){return (int)Math.round(Math.floor(Util.mul(Util.div(getDayOfMonth(),Area.DAYS_IN_MONTH),8.0)));}

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


	public boolean getMobility(){return mobility;}
	public void toggleMobility(boolean onoff){mobility=onoff;}
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

	public boolean okAffect(Environmental myHost, Affect affect)
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
			if((A!=null)&&(!A.okAffect(this,affect)))
				return false;
		}
		if((!mobility)||(!Sense.canMove(this)))
		{
			if((affect.sourceMinor()==Affect.TYP_ENTER)
			||(affect.sourceMinor()==Affect.TYP_LEAVE)
			||(affect.sourceMinor()==Affect.TYP_FLEE))
				return false;
		}
		if(getTechLevel()==Area.TECH_HIGH)
		{
			if((Util.bset(affect.sourceCode(),affect.MASK_MAGIC))
			||(Util.bset(affect.targetCode(),affect.MASK_MAGIC))
			||(Util.bset(affect.othersCode(),affect.MASK_MAGIC)))
			{
				Room room=null;
				if((affect.target()!=null)
				&&(affect.target() instanceof MOB)
				&&(((MOB)affect.target()).location()!=null))
					room=((MOB)affect.target()).location();
				else
				if((affect.source()!=null)
				&&(affect.source().location()!=null))
					room=affect.source().location();
				if(room!=null)
					room.showHappens(Affect.MSG_OK_VISUAL,"Magic doesn't seem to work here.");
				return false;
			}
		}
		else
		if(getTechLevel()==Area.TECH_LOW)
		{
			if((affect.tool()!=null)
			&&(affect.tool() instanceof Electronics))
			{
				switch(affect.sourceMinor())
				{
				case Affect.TYP_BUY:
				case Affect.TYP_CLOSE:
				case Affect.TYP_DEPOSIT:
				case Affect.TYP_DROP:
				case Affect.TYP_EXAMINESOMETHING:
				case Affect.TYP_GET:
				case Affect.TYP_GIVE:
				case Affect.TYP_OPEN:
				case Affect.TYP_PUT:
				case Affect.TYP_SELL:
				case Affect.TYP_VALUE:
				case Affect.TYP_VIEW:
				case Affect.TYP_WITHDRAW:
					break;
				default:
					{
						Room room=null;
						if((affect.target()!=null)
						&&(affect.target() instanceof MOB)
						&&(((MOB)affect.target()).location()!=null))
							room=((MOB)affect.target()).location();
						else
						if((affect.source()!=null)
						&&(affect.source().location()!=null))
							room=affect.source().location();
						if(room!=null)
							room.showHappens(Affect.MSG_OK_VISUAL,"Technology doesn't seem to work here.");
						return false;
					}
				}
			}
		}
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
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
				A.affect(this,affect);
		}
		if((affect.sourceMinor()==Affect.TYP_RETIRE)
		&&(amISubOp(affect.source().name())))
			delSubOp(affect.source().name());
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

	private String getWeatherStop(int weatherCode)
	{
		switch(weatherCode)
		{
		case Area.WEATHER_HAIL: return "The hailstorm stops.";
		case Area.WEATHER_CLOUDY: return "The clouds dissipate.";
		case Area.WEATHER_THUNDERSTORM: return "The thunderstorm stops.";
		case Area.WEATHER_RAIN: return "It stops raining.";
		case Area.WEATHER_SNOW: return "It stops snowing.";
		case Area.WEATHER_WINDY: return "The wind gusts stop.";
		case Area.WEATHER_WINTER_COLD: return "The cold snap is over.";
		case Area.WEATHER_HEAT_WAVE: return "The heat wave eases.";
		case Area.WEATHER_SLEET: return "The sleet stops pouring down.";
		case Area.WEATHER_DUSTSTORM: return "The dust storm ends.";
		case Area.WEATHER_DROUGHT: return "The drought is finally over.";
		}
		return "";
	}


	public int[] addMaskAndReturn(int[] one, int[] two)
	{
		if(one.length!=two.length)
			return one;
		int[] returnable=new int[one.length];
		for(int o=0;o<one.length;o++)
			returnable[o]=one[o]+two[o];
		return returnable;
	}

	public void forceWeatherTick()
	{
		weatherTicker=1;
		weatherTick();
	}

	public void weatherTick()
	{
		if((--weatherTicker)<=0)
		{
			// create a seasonal CHANCE graph
			int[] seasonal=new int[seasonalWeather.length];
			seasonal=addMaskAndReturn(seasonalWeather,seasonal);

			if((climateType()&Area.CLIMASK_COLD)>0)
				seasonal=addMaskAndReturn(seasonal,cold);

			if((climateType()&Area.CLIMASK_HOT)>0)
				seasonal=addMaskAndReturn(seasonal,hot);

			if((climateType()&Area.CLIMASK_DRY)>0)
				seasonal=addMaskAndReturn(seasonal,dry);

			if((climateType()&Area.CLIMASK_WET)>0)
				seasonal=addMaskAndReturn(seasonal,wet);

			if((climateType()&Area.CLIMATE_WINDY)>0)
				seasonal=addMaskAndReturn(seasonal,windy);

			// reset the weather ticker!
			weatherTicker=WEATHER_TICK_DOWN;


			String say=null;
			int goodWeatherTotal=0;
			int possibleNextWeather=nextWeather;
			for(int g=0;g<Area.NUM_WEATHER;g++)
			{
				// take the base chance for a seasonal weather occurrence (rain in winter, etc)
				int seasonalNum=seasonal[(getSeasonCode()*Area.NUM_WEATHER)+g];
				// find the chance of changing from what it will be, to some new condition.
				int changeNum=changeMap[(nextWeather*Area.NUM_WEATHER)+g];
				// add them together to find the chance of a particular change in a particular season
				// to a particular condition.
				int chance=seasonalNum+changeNum;
				// total all the change chances, negative means NO chance of this change
				if(chance>0) goodWeatherTotal+=chance;
			}

			// some sort of debugging commentary
			/*StringBuffer buf=new StringBuffer(name()+"/"+(Area.SEASON_DESCS[getSeasonCode()])+"/"+Area.WEATHER_DESCS[nextWeather]+"->");
			for(int g=0;g<Area.NUM_WEATHER;g++)
			{
				int seasonalNum=seasonal[(getSeasonCode()*Area.NUM_WEATHER)+g];
				int changeNum=changeMap[(nextWeather*Area.NUM_WEATHER)+g];
				int chance=seasonalNum+changeNum;
				//if(chance>0) buf.append(Area.WEATHER_DESCS[g]+"="+chance+"("+seasonalNum+"+"+changeNum+"), ");
			}*/

			// roll a number from this to that.  Like the lottery, whosever number gets rolled wins!
			int newGoodWeatherNum=Dice.roll(1,goodWeatherTotal,-1);

			// now, determine the winner!
			int tempWeatherTotal=0;
			for(int g=0;g<Area.NUM_WEATHER;g++)
			{
				// take the base chance for a seasonal weather occurrence (rain in winter, etc)
				int seasonalNum=seasonal[(getSeasonCode()*Area.NUM_WEATHER)+g];
				// find the chance of changing from what it will be, to some new condition.
				int changeNum=changeMap[(nextWeather*Area.NUM_WEATHER)+g];
				// add them together to find the chance of a particular change in a particular season
				// to a particular condition.
				int chance=seasonalNum+changeNum;
				if(chance>0)
				{
					tempWeatherTotal+=chance;
					if(newGoodWeatherNum<tempWeatherTotal)
					{
						possibleNextWeather=g;
						break;
					}
				}
			}

			// remember your olde weather
			int oldWeather=currentWeather;
			currentWeather=nextWeather;
			nextWeather=possibleNextWeather;
			if(oldWeather!=currentWeather)
			{
				switch(Dice.rollPercentage())
				{
				case 1: windDirection=Directions.NORTH; break;
				case 2: windDirection=Directions.SOUTH; break;
				case 3: windDirection=Directions.WEST; break;
				case 4: windDirection=Directions.EAST; break;
				}

				// 0=say nothing;
				// 1=say weatherdescription only
				// 2=say stop word only
				// 3=say stop word, then weatherdescription
				/*				     -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
				int[] sayMap=		{
				/*CLEAR*/			 0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
				/*CLOUDY*/			 2,  0,  3,  1,  1,  1,  1,  3,  1,  1,  3,  3,  3,
				/*WINDY*/			 2,  1,  0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
				/*RAIN*/			 2,  2,  2,  0,  1,  1,  1,  3,  1,  1,  3,  3,  3,
				/*THUNDERSTORM*/	 2,  2,  2,  3,  0,  3,  3,  3,  3,  3,  3,  3,  3,
				/*SNOW*/			 2,  2,  3,  3,  3,  0,  3,  3,  3,  1,  3,  3,  2,
				/*HAIL*/			 2,  2,  3,  3,  3,  3,  0,  3,  3,  1,  3,  3,  2,
				/*HEAT*/			 2,  3,  3,  3,  3,  3,  3,  0,  3,  3,  1,  1,  3,
				/*SLEET*/			 2,  2,  3,  3,  3,  3,  3,  3,  0,  3,  3,  3,  2,
				/*BLIZZ*/			 2,  2,  3,  3,  3,  3,  3,  3,  3,  0,  3,  3,  2,
				/*DUST*/			 2,  3,  2,  3,  3,  3,  3,  3,  3,  3,  0,  3,  3,
				/*DROUGHT*/  		 2,  3,  3,  3,  3,  3,  3,  2,  3,  3,  1,  0,  3,
				/*WINTER*/			 2,  3,  3,  3,  3,  1,  1,  3,  1,  1,  1,  1,  0,
									};
				String stopWord=getWeatherStop(oldWeather);
				switch(sayMap[(oldWeather*Area.NUM_WEATHER)+currentWeather])
				{
				case 0: say=null; break;
				case 1: say=getWeatherDescription(); break;
				case 2: say=stopWord; break;
				case 3: say=stopWord+" "+getWeatherDescription(); break;
				}
			}
			else
			if(currentWeather==Area.WEATHER_THUNDERSTORM)
				say="A bolt of lightning streaks across the sky.";

			if(say!=null)
			{
				for(Enumeration r=getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
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

	public boolean tick(Tickable ticking, int tickID)
	{
		if(stopTicking) return false;
		if(tickID==Host.AREA_TICK)
		{
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}

			int a=0;
			while(a<numAffects())
			{
				Ability A=fetchAffect(a);
				if(A!=null)
				{
					int s=affects.size();
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
			weatherTick();
			if(weatherTicker==1)
			{
				int coldChance=0;
				int fluChance=0;
				int rustChance=0;
				switch(weatherType(null))
				{
				case Area.WEATHER_BLIZZARD:
				case Area.WEATHER_SLEET:
				case Area.WEATHER_SNOW:
					coldChance=99;
					fluChance=25;
					rustChance=5;
					break;
				case Area.WEATHER_HAIL:
					coldChance=50;
					rustChance=5;
					break;
				case Area.WEATHER_THUNDERSTORM:
				case Area.WEATHER_RAIN:
					coldChance=25;
					rustChance=5;
					break;
				case Area.WEATHER_WINTER_COLD:
					coldChance=75;
					fluChance=10;
					break;
				}
				if((coldChance>0)||(rustChance>0))
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(S.mob().location().getArea()==this)
					&&(!S.mob().isMonster())
					&&(weatherType(S.mob().location())!=Area.WEATHER_CLEAR))
					{
						MOB M=S.mob();
						if(Dice.rollPercentage()<coldChance)
						{
							long coveredPlaces=0;
							for(int i=0;i<M.inventorySize();i++)
							{
								Item I=M.fetchInventory(i);
								if(I==null)	continue;
								if(I.amWearingAt(Item.ON_FEET))
									coveredPlaces=coveredPlaces|Item.ON_FEET;
								else
								if(I.amWearingAt(Item.ABOUT_BODY))
									coveredPlaces=coveredPlaces|Item.ON_TORSO|Item.ON_LEGS;
								else
								if(I.amWearingAt(Item.ON_TORSO))
									coveredPlaces=coveredPlaces|Item.ON_TORSO;
								else
								if(I.amWearingAt(Item.ON_LEGS))
									coveredPlaces=coveredPlaces|Item.ON_LEGS;
							}
							if(coveredPlaces!=(Item.ON_FEET|Item.ON_TORSO|Item.ON_LEGS))
							{
								Ability COLD=CMClass.getAbility("Disease_Cold");
								if(Dice.rollPercentage()<(fluChance+(((M.location().domainConditions()&Room.CONDITION_WET)>0)?10:0)))
									COLD=CMClass.getAbility("Disease_Flu");
								if(COLD!=null) COLD.invoke(M,M,true);
							}
						}
						else
						if(Dice.rollPercentage()<rustChance)
						{
							String weatherDesc=Area.WEATHER_DESCS[weatherType(null)].toLowerCase();
							Vector rustThese=new Vector();
							for(int i=0;i<M.inventorySize();i++)
							{
								Item I=M.fetchInventory(i);
								if(I==null)	continue;
								if((!I.amWearingAt(Item.INVENTORY))
								&&(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL))
								&&(I.subjectToWearAndTear())
								&&((Dice.rollPercentage()>I.envStats().ability()*25)))
									rustThese.addElement(I);
								else
								if(I.amWearingAt(Item.ABOUT_BODY)
								&&(((I.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_METAL)))
								{	rustThese.clear();	break;	}
							}
							for(int i=0;i<rustThese.size();i++)
							{
								Item I=(Item)rustThese.elementAt(i);
								M.tell("Your "+I.name()+" rusts in the "+weatherDesc+".");
								I.setUsesRemaining(I.usesRemaining()-1);
								if(I.usesRemaining()<=0)
								{
									if(M.location()!=null)
									{
										FullMsg msg=new FullMsg(M,null,null,Affect.MSG_OK_VISUAL,I.name()+" is destroyed!",null,I.name()+" owned by "+M.name()+" is destroyed!");
										if(M.location().okAffect(M,msg))
											M.location().send(M,msg);
									}
									I.destroyThis();
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	protected String theWeatherDescription(int weather)
	{
		StringBuffer desc=new StringBuffer("");
		switch(weather)
		{
		case Area.WEATHER_HAIL:
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("Golfball sized clumps of ice ");
			else
				desc.append("Light streams of hail ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirl down from above.");
			else
				desc.append("fall from the sky.");
			break;
		case Area.WEATHER_HEAT_WAVE:
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("It is rather warm.");
			else
				desc.append("It is very hot. ");
			break;
		case Area.WEATHER_WINTER_COLD:
			if(((climateType()&Area.CLIMASK_HOT)>0)||(getSeasonCode()==Area.SEASON_SUMMER))
				desc.append("It is rather cold.");
			else
				desc.append("It is very cold. ");
			break;
		case Area.WEATHER_DROUGHT:
			desc.append("There are horrible drought conditions.");
			break;
		case Area.WEATHER_CLOUDY:
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("Grey and gloomy clouds ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("Dark and looming stormclouds ");
			else
			if(((climateType()&Area.CLIMASK_HOT)>0)||(getSeasonCode()==Area.SEASON_SUMMER))
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
		case Area.WEATHER_DUSTSTORM:
			desc.append("An eye-stinging dust storm ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("blows in from "+Directions.getFromDirectionName(this.windDirection));
			break;
		case Area.WEATHER_BLIZZARD:
			desc.append("A thunderous blizzard ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("pours down from above.");
			break;
		case Area.WEATHER_CLEAR:
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("The weather is cool and clear.");
			else
			if(((climateType()&Area.CLIMASK_HOT)>0)||(getSeasonCode()==Area.SEASON_SUMMER))
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
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("A cold light rain ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("A cool soaking rain ");
			else
			if(((climateType()&Area.CLIMASK_HOT)>0)||(getSeasonCode()==Area.SEASON_SUMMER))
				desc.append("A warm rain ");
			else
				desc.append("A light rain ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Area.WEATHER_SNOW:
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("A light snow ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("A slushy snow ");
			else
			if(((climateType()&Area.CLIMASK_HOT)>0)||(getSeasonCode()==Area.SEASON_SUMMER))
				desc.append("A freakish snow ");
			else
				desc.append("An unseasonable snow ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Area.WEATHER_SLEET:
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("A sleet storm ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("A slushy snow ");
			else
			if(((climateType()&Area.CLIMASK_HOT)>0)||(getSeasonCode()==Area.SEASON_SUMMER))
				desc.append("A freakish sleet storm ");
			else
				desc.append("An unseasonable sleet storm ");
			if((climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Area.WEATHER_WINDY:
			if(((climateType()&Area.CLIMASK_COLD)>0)||(getSeasonCode()==Area.SEASON_WINTER))
				desc.append("A cold "+(((climateType()&Area.CLIMASK_DRY)>0)?"dry ":"")+"wind ");
			else
			if((climateType()&Area.CLIMASK_WET)>0)
				desc.append("A forboding gust of wind ");
			else
			if(((climateType()&Area.CLIMASK_HOT)>0)||(getSeasonCode()==Area.SEASON_SUMMER))
				desc.append("A hot "+(((climateType()&Area.CLIMASK_DRY)>0)?"dry ":"")+"wind ");
			else
				desc.append("A light "+(((climateType()&Area.CLIMASK_DRY)>0)?"dry ":"")+"wind ");
			desc.append("blows from "+Directions.getFromDirectionName(this.windDirection));
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
		if(envStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|envStats().sensesMask());
		int disposition=envStats().disposition()
			&((Integer.MAX_VALUE-(EnvStats.IS_SLEEPING|EnvStats.IS_HIDDEN)));
		if((affected instanceof Room)&&((((Room)affected).domainType()&Room.INDOORS)==0))
		{
			if((weatherType((Room)affected)==Area.WEATHER_BLIZZARD)
			   ||(weatherType((Room)affected)==Area.WEATHER_DUSTSTORM)
			   ||(timeCode==Area.TIME_NIGHT))
				disposition=disposition|EnvStats.IS_DARK;
		}
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public void raiseLowerTheSunEverywhere()
	{
		Area A=this;
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
						if(((R.domainType()&Room.INDOORS)==0)
						&&(!Sense.isSleeping(mob))
						&&(Sense.canSee(mob)))
						{
							switch(A.getTODCode())
							{
							case Area.TIME_DAWN:
								mob.tell("The sun begins to rise in the west.");
								break;
							case Area.TIME_DAY:
								break;
								//mob.tell("The sun is now shining brightly."); break;
							case Area.TIME_DUSK:
								mob.tell("The sun begins to set in the east."); break;
							case Area.TIME_NIGHT:
								mob.tell("The sun has set and darkness again covers the world."); break;
							}
						}
						else
						{
							switch(A.getTODCode())
							{
							case Area.TIME_DAWN:
								mob.tell("It is now daytime."); break;
							case Area.TIME_DAY: break;
								//mob.tell("The sun is now shining brightly."); break;
							case Area.TIME_DUSK: break;
								//mob.tell("It is almost nighttime."); break;
							case Area.TIME_NIGHT:
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
		Area A=this;
		if(reReadTime)
		{
			reReadTime=false;
			StringBuffer timeRsc=Resources.getFileResource("time.txt");
			if(timeRsc.length()<30)
			{
				timeRsc=new StringBuffer("<TIME>-1</TIME><DAY>1</DAY><MONTH>1</MONTH><YEAR>1</YEAR>");
				Resources.updateResource("time.txt",timeRsc);
				Resources.saveFileResource("time.txt");
			}
			Vector V=XMLManager.parseAllXML(timeRsc.toString());
			A.setTimeOfDay(XMLManager.getIntFromPieces(V,"TIME"));
			A.setDayOfMonth(XMLManager.getIntFromPieces(V,"DAY"));
			A.setMonth(XMLManager.getIntFromPieces(V,"MONTH"));
			A.setYear(XMLManager.getIntFromPieces(V,"YEAR"));
		}
		boolean raiseLowerTheSun=A.setTimeOfDay(A.getTimeOfDay()+howManyHours);
		if(A.getTimeOfDay()>=Area.A_FULL_DAY)
		{
			raiseLowerTheSun=A.setTimeOfDay(A.getTimeOfDay()-Area.A_FULL_DAY);
			A.setDayOfMonth(A.getDayOfMonth()+1);
			if(A.getDayOfMonth()>Area.DAYS_IN_MONTH)
			{
				A.setDayOfMonth(1);
				A.setMonth(A.getMonth()+1);
				if(A.getMonth()>Area.MONTHS_IN_YEAR)
				{
					A.setMonth(1);
					A.setYear(A.getYear()+1);
				}
			}
		}
		else
		if(A.getTimeOfDay()<0)
		{
			raiseLowerTheSun=A.setTimeOfDay(Area.A_FULL_DAY+A.getTimeOfDay());
			A.setDayOfMonth(A.getDayOfMonth()-1);
			if(A.getDayOfMonth()<1)
			{
				A.setDayOfMonth(Area.DAYS_IN_MONTH);
				A.setMonth(A.getMonth()-1);
				if(A.getMonth()<1)
				{
					A.setMonth(Area.MONTHS_IN_YEAR);
					A.setYear(A.getYear()-1);
				}
			}
		}
		if(raiseLowerTheSun) raiseLowerTheSunEverywhere();

		StringBuffer timeRsc=new StringBuffer("<DAY>"+A.getDayOfMonth()+"</DAY><MONTH>"+A.getMonth()+"</MONTH><YEAR>"+A.getYear()+"</YEAR>");
		Resources.updateResource("time.txt",timeRsc);
		Resources.saveFileResource("time.txt");
	}

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

	public int adjustWaterConsumption(int base, MOB mob, Room room)
	{
		if((room.domainType()&Room.INDOORS)>0)
			return base;
		else
		switch(currentWeather)
		{
		case Area.WEATHER_DROUGHT:
			return base*4;
		case Area.WEATHER_DUSTSTORM:
			return base*3;
		case Area.WEATHER_HEAT_WAVE:
			return base*2;
		case Area.WEATHER_RAIN:
		case Area.WEATHER_THUNDERSTORM:
			return (int)Math.round(Math.floor(Util.div(base,2)));
		case Area.WEATHER_BLIZZARD:
		case Area.WEATHER_CLEAR:
		case Area.WEATHER_CLOUDY:
		case Area.WEATHER_HAIL:
		case Area.WEATHER_WINDY:
		case Area.WEATHER_WINTER_COLD:
			break;
		}
		return base;
	}

	public void fillInAreaRooms()
	{
		for(Enumeration r=getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.clearSky();
			if(R.ID().length()>0)
			{
				if(R instanceof GridLocale)
					((GridLocale)R).buildGrid();
			}
		}
		myRooms=null;
		for(Enumeration r=getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.clearSky();
			R.giveASky(0);
		}
		myRooms=null;
	}

	public void fillInAreaRoom(Room R)
	{
		R.clearSky();
		if(R.ID().length()>0)
		{
			if(R instanceof GridLocale)
				((GridLocale)R).buildGrid();
		}
		R.giveASky(0);
	}

	public int adjustMovement(int base, MOB mob, Room room)
	{
		if((room.domainType()&Room.INDOORS)>0)
			return base;
		else
		switch(currentWeather)
		{
		case Area.WEATHER_THUNDERSTORM:
			return base*2;
		case Area.WEATHER_HAIL:
			return base*2;
		case Area.WEATHER_DUSTSTORM:
			return base*3;
		case Area.WEATHER_BLIZZARD:
			return base*4;
		}
		return base;
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
		getMap();
		s.append("Number of rooms: "+myRooms.size()+"\n\r");

		Vector mobRanges=new Vector();
		int totalMOBs=0;
		int lowestLevel=Integer.MAX_VALUE;
		int highestLevel=Integer.MIN_VALUE;
		int totalLevels=0;
		int averageLevel=0;
		int medianLevel=0;
		long totalAlignments=0;
		int averageAlignment=0;
		for(Enumeration r=getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
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
			s.append("Alignment avg. : "+averageAlignment+" ("+CommonStrings.alignmentStr(averageAlignment)+")\n\r");
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
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}

	public void clearMap(){myRooms=null;}

	public int mapSize()
	{
		getMap();
		return myRooms.size();
	}
	public Room getRandomRoom()
	{
		if(mapSize()==0) return null;
		return (Room)myRooms.elementAt(Dice.roll(1,mapSize(),-1));
	}
	public synchronized Enumeration getMap()
	{
		if(myRooms!=null) return myRooms.elements();
		Vector myMap=new Vector();
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R.getArea()==this)
				myMap.addElement(R);
		}
		myRooms=myMap;
		return myMap.elements();
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

	public Vector getSubOpVectorList()
	{
		return subOps;
	}

	private static final String[] CODES={"CLASS","CLIMATE","DESCRIPTION","TEXT","TECHLEVEL"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+climateType();
		case 2: return description();
		case 3: return text();
		case 4: return ""+getTechLevel();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(Util.s_int(val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTechLevel(Util.s_int(val)); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdArea)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
