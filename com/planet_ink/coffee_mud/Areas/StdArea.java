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
	protected String archPath="";
	protected int climateID=Area.CLIMASK_NORMAL;
	protected int currentWeather=Area.WEATHER_CLEAR;
	protected int nextWeather=Area.WEATHER_CLEAR;
	protected int techLevel=0;
	protected int botherDown=Area.BOTHER_WEATHER_TICKS;
	protected Vector myRooms=null;
	protected boolean mobility=true;
	protected long tickStatus=Tickable.STATUS_NOT;
	private Boolean roomSemaphore=new Boolean(true);
	private int[] statData=null;

    protected Vector children=null;
    protected Vector parents=null;
    public Vector childrenToLoad=new Vector();
    public Vector parentsToLoad=new Vector();

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
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return name;
	}
	public void setName(String newName){name=newName;}
	public String Name(){return name;}
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
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
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

	public boolean hasASky(Room room)
	{
		if((room==null)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||((room.domainType()&Room.INDOORS)>0))
			return false;
		return true;
	}


	public int weatherType(Room room)
	{
		if(!hasASky(room)) return Area.WEATHER_CLEAR;
		return currentWeather;
	}
	public String weatherDescription(Room room)
	{
		if(!hasASky(room))
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

	public boolean canSeeTheMoon(Room room)
	{
		if(((timeCode!=Area.TIME_NIGHT)&&(timeCode!=Area.TIME_DUSK))
		||(!hasASky(room)))
			return false;
		switch(weatherType(room))
		{
		case Area.WEATHER_BLIZZARD:
		case Area.WEATHER_HAIL:
		case Area.WEATHER_SLEET:
		case Area.WEATHER_SNOW:
		case Area.WEATHER_RAIN:
		case Area.WEATHER_THUNDERSTORM:
		case Area.WEATHER_CLOUDY:
		case Area.WEATHER_DUSTSTORM:
			return false;
		default:
			return true;
		}
	}

	public boolean canSeeTheSun(Room room)
	{
		if(((timeCode!=Area.TIME_DAY)&&(timeCode!=Area.TIME_DAWN))
		||(!hasASky(room)))
			return false;

		switch(weatherType(room))
		{
		case Area.WEATHER_BLIZZARD:
		case Area.WEATHER_HAIL:
		case Area.WEATHER_SLEET:
		case Area.WEATHER_SNOW:
		case Area.WEATHER_RAIN:
		case Area.WEATHER_THUNDERSTORM:
		case Area.WEATHER_CLOUDY:
		case Area.WEATHER_DUSTSTORM:
			return false;
		default:
			return true;
		}

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
		if((Sense.canSee(mob))
		&&(timeCode==Area.TIME_NIGHT)
		&&(hasASky(room)))
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

	public String getArchivePath(){return archPath;}
	public void setArchivePath(String pathFile){archPath=pathFile;}

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
		subOps=Util.parseSemicolons(list,true);
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
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
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

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CoffeeMaker.setPropertiesStr(this,newMiscText,true);
	}

	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okMessage(this,msg)))
				return false;
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(!A.okMessage(this,msg)))
				return false;
		}
		if((!mobility)||(!Sense.canMove(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if(getTechLevel()==Area.TECH_HIGH)
		{
			if((Util.bset(msg.sourceCode(),CMMsg.MASK_MAGIC))
			||(Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
			||(Util.bset(msg.othersCode(),CMMsg.MASK_MAGIC)))
			{
				Room room=null;
				if((msg.target()!=null)
				&&(msg.target() instanceof MOB)
				&&(((MOB)msg.target()).location()!=null))
					room=((MOB)msg.target()).location();
				else
				if((msg.source()!=null)
				&&(msg.source().location()!=null))
					room=msg.source().location();
				if(room!=null)
				{
					if(room.getArea()==this)
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work here.");
					else
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work there.");
				}

				return false;
			}
		}
		else
		if(getTechLevel()==Area.TECH_LOW)
		{
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Electronics))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_DEPOSIT:
				case CMMsg.TYP_DROP:
				case CMMsg.TYP_EXAMINESOMETHING:
				case CMMsg.TYP_GET:
				case CMMsg.TYP_GIVE:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_PUT:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_VALUE:
				case CMMsg.TYP_REMOVE:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_WITHDRAW:
					break;
				default:
					{
						Room room=null;
						if((msg.target()!=null)
						&&(msg.target() instanceof MOB)
						&&(((MOB)msg.target()).location()!=null))
							room=((MOB)msg.target()).location();
						else
						if((msg.source()!=null)
						&&(msg.source().location()!=null))
							room=msg.source().location();
						if(room!=null)
							room.showHappens(CMMsg.MSG_OK_VISUAL,"Technology doesn't seem to work here.");
						return false;
					}
				}
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.executeMsg(this,msg);
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.executeMsg(this,msg);
		}
		if((msg.sourceMinor()==CMMsg.TYP_RETIRE)
		&&(amISubOp(msg.source().Name())))
			delSubOp(msg.source().Name());
	}

	public void tickControl(boolean start)
	{
		if(start)
		{
			stopTicking=false;
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_AREA,1);
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
					if(hasASky(R))
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

	public long getTickStatus(){ return tickStatus;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(stopTicking) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==MudHost.TICK_AREA)
		{
			for(int b=0;b<numBehaviors();b++)
			{
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}

			int a=0;
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					tickStatus=Tickable.STATUS_AFFECT+a;
					int s=affects.size();
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
			if((--botherDown)<=0)
			{
				botherDown=Area.BOTHER_WEATHER_TICKS;
				switch(weatherType(null))
				{
				case Area.WEATHER_BLIZZARD:
				case Area.WEATHER_SLEET:
				case Area.WEATHER_SNOW:
				case Area.WEATHER_HAIL:
				case Area.WEATHER_THUNDERSTORM:
				case Area.WEATHER_RAIN:
					for(Enumeration r=getMap();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(hasASky(R))
							for(int i=0;i<R.numInhabitants();i++)
							{
								MOB mob=R.fetchInhabitant(i);
								if((mob!=null)
								&&(!mob.isMonster())
								&&(Sense.aliveAwakeMobile(mob,true)))
									mob.tell(getWeatherDescription());
							}
					}
					break;
				}
			}
			tickStatus=Tickable.STATUS_WEATHER;
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
				long[] allspot={Item.ON_FEET,Item.ON_TORSO,Item.ON_LEGS};
				long allcode=0;
				for(int l=0;l<allspot.length;l++)
					allcode=allcode|allspot[l];

				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()==null)
					||(S.mob().location()==null)
					||(S.mob().location().getArea()!=this)
					||(S.mob().isMonster()))
						continue;

					MOB M=S.mob();
					Room R=M.location();

					if(coldChance>0)
					{
						int save=(M.charStats().getStat(CharStats.SAVE_COLD)+M.charStats().getStat(CharStats.SAVE_WATER))/2;
						if((Dice.rollPercentage()<(coldChance-save))
						&&((weatherType(S.mob().location())!=Area.WEATHER_CLEAR)))
						{
							long coveredPlaces=0;
							Item I=CMClass.getItem("GenItem");
							I.setRawLogicalAnd(false);
							for(int l=0;l<allspot.length;l++)
							{
								I.setRawProperLocationBitmap(allspot[l]);
								if(M.getWearPositions(allspot[l])==0)
									coveredPlaces=coveredPlaces|allspot[l];
							}
							for(int i=0;i<M.inventorySize();i++)
							{
								I=M.fetchInventory(i);
								if((I==null)||(I.amWearingAt(Item.INVENTORY)))
								   continue;
								if(I.amWearingAt(Item.ABOUT_BODY))
									coveredPlaces=coveredPlaces|Item.ON_TORSO|Item.ON_LEGS;
								for(int l=0;l<allspot.length;l++)
									if(I.amWearingAt(allspot[l]))
										coveredPlaces=coveredPlaces|allspot[l];
							}
							if(coveredPlaces!=allcode)
							{
								Ability COLD=CMClass.getAbility("Disease_Cold");
								if(Dice.rollPercentage()<(fluChance+(((M.location().domainConditions()&Room.CONDITION_WET)>0)?10:0)))
									COLD=CMClass.getAbility("Disease_Flu");
								if((COLD!=null)&&(M.fetchEffect(COLD.ID())==null))
									COLD.invoke(M,M,true);
							}
						}
					}

					switch(R.domainType())
					{
					case Room.DOMAIN_INDOORS_UNDERWATER:
					case Room.DOMAIN_INDOORS_WATERSURFACE:
					case Room.DOMAIN_OUTDOORS_WATERSURFACE:
					case Room.DOMAIN_OUTDOORS_UNDERWATER:
						rustChance+=5;
						break;
					default:
						break;
					}
					if(Dice.rollPercentage()<rustChance)
					{
						int weatherType=weatherType(R);
						String weatherDesc=Area.WEATHER_DESCS[weatherType].toLowerCase();
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
							if(weatherType!=0)
								M.tell("Your "+I.name()+" rusts in the "+weatherDesc+".");
							else
								M.tell("Your "+I.name()+" rusts in the water.");
							I.setUsesRemaining(I.usesRemaining()-1);
							if(I.usesRemaining()<=0)
							{
								if(M.location()!=null)
								{
									FullMsg msg=new FullMsg(M,null,null,CMMsg.MSG_OK_VISUAL,I.name()+" is destroyed!",null,I.name()+" owned by "+M.name()+" is destroyed!");
									if(M.location().okMessage(M,msg))
										M.location().send(M,msg);
								}
								I.destroy();
							}
						}
					}
				}
			}
		}
		tickStatus=Tickable.STATUS_NOT;
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
		if((affected instanceof Room)&&(hasASky((Room)affected)))
		{
			if((weatherType((Room)affected)==Area.WEATHER_BLIZZARD)
			   ||(weatherType((Room)affected)==Area.WEATHER_DUSTSTORM)
			   ||(timeCode==Area.TIME_NIGHT))
				disposition=disposition|EnvStats.IS_DARK;
		}
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+envStats().weight());
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
						if(hasASky(R)
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
			StringBuffer timeRsc=Resources.getFileResource("time.txt",false);
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

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numEffects()
	{
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}

	public int adjustWaterConsumption(int base, MOB mob, Room room)
	{
		if(((room!=null)&&(room.domainType()&Room.INDOORS)==(Room.INDOORS)))
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
			if(R.roomID().length()>0)
			{
				if(R instanceof GridLocale)
					((GridLocale)R).buildGrid();
			}
		}
		clearMap();
		for(Enumeration r=getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.clearSky();
			R.giveASky(0);
		}
		clearMap();
	}

	public void fillInAreaRoom(Room R)
	{
		if(R==null) return;
		R.clearSky();
		if(R.roomID().length()>0)
		{
			if(R instanceof GridLocale)
				((GridLocale)R).buildGrid();
		}
		R.giveASky(0);
	}

	public int adjustMovement(int base, MOB mob, Room room)
	{
		if(((room!=null)&&(room.domainType()&Room.INDOORS)==(Room.INDOORS)))
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

	public int[] getAreaIStats()
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return null;
		getAreaStats();
		return statData;
	}
	public StringBuffer getAreaStats()
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return new StringBuffer("");
		StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+Name().toUpperCase());
		if(s!=null) return s;
		s=new StringBuffer("");
		s.append(description()+"\n\r");
		s.append("Number of rooms: "+numberOfIDedRooms()+"\n\r");

		Vector levelRanges=new Vector();
		Vector alignRanges=new Vector();
		statData=new int[Area.AREASTAT_NUMBER];
		statData[Area.AREASTAT_POPULATION]=0;
		statData[Area.AREASTAT_MINLEVEL]=Integer.MAX_VALUE;
		statData[Area.AREASTAT_MAXLEVEL]=Integer.MIN_VALUE;
		statData[Area.AREASTAT_AVGLEVEL]=0;
		statData[Area.AREASTAT_MEDLEVEL]=0;
		statData[Area.AREASTAT_AVGALIGN]=0;
		statData[Area.AREASTAT_TOTLEVEL]=0;
		statData[Area.AREASTAT_INTLEVEL]=0;
		long totalAlignments=0;
		for(Enumeration r=getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB mob=R.fetchInhabitant(i);
				if((mob!=null)&&(mob.isMonster()))
				{
					int lvl=mob.baseEnvStats().level();
					levelRanges.addElement(new Integer(lvl));
					alignRanges.addElement(new Integer(mob.getAlignment()));
					totalAlignments+=mob.getAlignment();
					statData[Area.AREASTAT_POPULATION]++;
					statData[Area.AREASTAT_TOTLEVEL]+=lvl;
					if(!Sense.isAnimalIntelligence(mob))
						statData[Area.AREASTAT_INTLEVEL]+=lvl;
					if(lvl<statData[Area.AREASTAT_MINLEVEL])
						statData[Area.AREASTAT_MINLEVEL]=lvl;
					if(lvl>statData[Area.AREASTAT_MAXLEVEL])
						statData[Area.AREASTAT_MAXLEVEL]=lvl;
				}
			}
		}
		if((statData[Area.AREASTAT_POPULATION]==0)||(levelRanges.size()==0))
		{
			statData[Area.AREASTAT_MINLEVEL]=0;
			statData[Area.AREASTAT_MAXLEVEL]=0;
			s.append("Population     : 0\n\r");
		}
		else
		{
			Collections.sort((List)levelRanges);
			Collections.sort((List)alignRanges);
			statData[Area.AREASTAT_MEDLEVEL]=((Integer)levelRanges.elementAt((int)Math.round(Math.floor(Util.div(levelRanges.size(),2.0))))).intValue();
			statData[Area.AREASTAT_MEDALIGN]=((Integer)alignRanges.elementAt((int)Math.round(Math.floor(Util.div(alignRanges.size(),2.0))))).intValue();
			statData[Area.AREASTAT_AVGLEVEL]=(int)Math.round(Util.div(statData[Area.AREASTAT_TOTLEVEL],statData[Area.AREASTAT_POPULATION]));
			statData[Area.AREASTAT_AVGALIGN]=(int)Math.round(new Long(totalAlignments).doubleValue()/new Integer(statData[Area.AREASTAT_POPULATION]).doubleValue());
			s.append("Population     : "+statData[Area.AREASTAT_POPULATION]+"\n\r");
			Vector V=Sense.flaggedBehaviors(this,Behavior.FLAG_LEGALBEHAVIOR);
			if((V!=null)&&(V.size()>0)&&(V.firstElement() instanceof Behavior))
			{
				Behavior B=(Behavior)V.firstElement();
				V.clear();
				V.addElement(new Integer(Law.MOD_RULINGCLAN));
				if(B.modifyBehavior(this,CMClass.sampleMOB(),V)
				&&(V.size()>0)
				&&(V.firstElement() instanceof String))
				{
					Clan C=Clans.getClan(((String)V.firstElement()));
					if(C!=null)
						s.append("Controlled by  : "+C.typeName()+" "+C.name()+"\n\r");
				}
			}
			s.append("Level range    : "+statData[Area.AREASTAT_MINLEVEL]+" to "+statData[Area.AREASTAT_MAXLEVEL]+"\n\r");
			s.append("Average level  : "+statData[Area.AREASTAT_AVGLEVEL]+"\n\r");
			s.append("Median level   : "+statData[Area.AREASTAT_MEDLEVEL]+"\n\r");
			s.append("Avg. Alignment : "+statData[Area.AREASTAT_AVGALIGN]+" ("+CommonStrings.alignmentStr(statData[Area.AREASTAT_AVGALIGN])+")\n\r");
			s.append("Med. Alignment : "+statData[Area.AREASTAT_MEDALIGN]+" ("+CommonStrings.alignmentStr(statData[Area.AREASTAT_MEDALIGN])+")\n\r");
		}
		Resources.submitResource("HELP_"+Name().toUpperCase(),s);
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

	public void clearMap()
	{
		synchronized(roomSemaphore)
		{
			myRooms=null;
		}
	}

	public int mapSize()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms!=null)
				return myRooms.size();
			else
				makeMap();
			return myRooms.size();
		}
	}
	public int numberOfIDedRooms()
	{
		int num=0;
		for(Enumeration e=getMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				if(R instanceof GridLocale)
					num+=((GridLocale)R).getAllRooms().size();
				else
					num++;
		}
		return num;
	}
	public Room getRandomRoom()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms==null) makeMap();
			if(mapSize()==0) return null;
			return (Room)myRooms.elementAt(Dice.roll(1,mapSize(),-1));
		}
	}
	public Enumeration getMap()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms!=null) return myRooms.elements();
			makeMap();
			return myRooms.elements();
		}
	}
	private void makeMap()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms!=null) return;
			Vector myMap=new Vector();
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea()==this)
					myMap.addElement(R);
			}
			myRooms=myMap;
		}
	}

	public int nextWeatherType(Room room)
	{
		if(!hasASky(room)) return Area.WEATHER_CLEAR;
		return nextWeather;
	}
	public String nextWeatherDescription(Room room)
	{
		if(!hasASky(room)) return "You can't tell much about the weather from here.";
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

    public void addChildToLoad(String str) { childrenToLoad.addElement(str);}
    public void addParentToLoad(String str) { parentsToLoad.addElement(str);}

	// Children
	public void initChildren() {
	        if(children==null) {
	                children=new Vector();
	                for(int i=0;i<childrenToLoad.size();i++) {
	                  Area A=CMMap.getArea((String)childrenToLoad.elementAt(i));
	                  if(A==null)
	                    continue;
	                  children.addElement(A);
	                }
	        }
	}
	public Enumeration getChildren() { initChildren(); return children.elements(); }
	public String getChildrenList() {
	        initChildren();
	        StringBuffer str=new StringBuffer("");
	        for(Enumeration e=getChildren(); e.hasMoreElements();) {
	                Area A=(Area)e.nextElement();
	                if(str.length()>0) str.append(";");
	                str.append(A.name());
	        }
	        return str.toString();
	}

	public int getNumChildren() { initChildren(); return children.size(); }
	public Area getChild(int num) { initChildren(); return (Area)children.elementAt(num); }
	public Area getChild(String named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                       return A;
	        }
	        return null;
	}
	public boolean isChild(Area named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if(A.equals(named))
	                       return true;
	        }
	        return false;
	}
	public boolean isChild(String named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                        return true;
	        }
	        return false;
	}
	public void addChild(Area Adopted) {
	        initChildren();
	        // So areas can load ok, the code needs to be able to replace 'dummy' children with 'real' ones
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if(A.Name().equalsIgnoreCase(Adopted.Name())){
	                        children.setElementAt(Adopted, i);
	                        return;
	                }
	        }
	        children.addElement(Adopted);
	}
	public void removeChild(Area Disowned) { initChildren(); children.removeElement(Disowned); }
	public void removeChild(int Disowned) { initChildren(); children.removeElementAt(Disowned); }
	// child based circular reference check
	public boolean canChild(Area newChild) {
	        initParents();
	        // Someone asked this area if newChild can be a child to them,
	        // which means this is a parent to someone.  If newChild is a
	        // parent, directly or indirectly, return false.
	        if(parents.contains(newChild))
	        {
	                return false; // It is directly a parent
	        }
	        for(int i=0;i<parents.size();i++) {
	                // check with all the parents about how they feel
	                Area rent=(Area)parents.elementAt(i);
	                // as soon as any parent says false, dump that false back to them
	                if(!(rent.canChild(newChild)))
	                {
	                        return false;
	                }
	        }
	        // no parent is the same as newChild, nor is it indirectly a parent.
	        // Go for it!
	        return true;
	}

	// Parent
	public void initParents() {
	        if (parents == null) {
	                parents = new Vector();
	                for (int i = 0; i < parentsToLoad.size(); i++) {
	                        Area A = CMMap.getArea( (String) parentsToLoad.elementAt(i));
	                        if (A == null)
	                                continue;
	                        parents.addElement(A);
	                }
	        }
	}
	public Enumeration getParents() { initParents(); return parents.elements(); }
	public String getParentsList() {
	        initParents();
	        StringBuffer str=new StringBuffer("");
	        for(Enumeration e=getParents(); e.hasMoreElements();) {
	                Area A=(Area)e.nextElement();
	                if(str.length()>0) str.append(";");
	                str.append(A.name());
	        }
	        return str.toString();
	}

	public int getNumParents() { initParents(); return parents.size(); }
	public Area getParent(int num) { initParents(); return (Area)parents.elementAt(num); }
	public Area getParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                       return A;
	        }
	        return null;
	}
	public boolean isParent(Area named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.equals(named))
	                       return true;
	        }
	        return false;
	}
	public boolean isParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                        return true;
	        }
	        return false;
	}
	public void addParent(Area Adopted) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.Name().equalsIgnoreCase(Adopted.Name())){
	                        parents.setElementAt(Adopted, i);
	                        return;
	                }
	        }
	        parents.addElement(Adopted);
	}
	public void removeParent(Area Disowned) { initParents();parents.removeElement(Disowned); }
	public void removeParent(int Disowned) { initParents();parents.removeElementAt(Disowned); }
	public boolean canParent(Area newParent) {
	        initChildren();
	        // Someone asked this area if newParent can be a parent to them,
	        // which means this is a child to someone.  If newParent is a
	        // child, directly or indirectly, return false.
	        if(children.contains(newParent))
	        {
	                return false; // It is directly a child, so it can't Parent
	        }
	        for(int i=0;i<children.size();i++) {
	                // check with all the children about how they feel
	                Area child=(Area)children.elementAt(i);
	                // as soon as any child says false, dump that false back to them
	                if(!(child.canParent(newParent)))
	                {
	                        return false;
	                }
	        }
	        // no child is the same as newParent, nor is it indirectly a child.
	        // Go for it!
	        return true;
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
