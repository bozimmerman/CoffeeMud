package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DefaultClimate implements Climate
{
	public String ID(){return "DefaultClimate";}
	public String name(){return "Climate Object";}
	protected long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	protected int currentWeather=WEATHER_CLEAR;
	protected int nextWeather=WEATHER_CLEAR;
	protected int botherDown=BOTHER_WEATHER_TICKS;
	private static final int WEATHER_TICK_DOWN=300; // 300 = 20 minutes * 60 seconds / 4
	protected int weatherTicker=WEATHER_TICK_DOWN;
	protected static int windDirection=Directions.NORTH;

	public int nextWeatherType(Room room)
	{
		if(!CoffeeUtensils.hasASky(room)) return Climate.WEATHER_CLEAR;
		return nextWeather;
	}
	public String nextWeatherDescription(Room room)
	{
		if(!CoffeeUtensils.hasASky(room)) return "You can't tell much about the weather from here.";
		return getNextWeatherDescription(room.getArea());
	}
	public String getNextWeatherDescription(Area A)
	{
		return theWeatherDescription(A,nextWeather);
	}

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

	public void setNextWeatherType(int weatherCode){nextWeather=weatherCode;}
	public void setCurrentWeatherType(int weatherCode){currentWeather=weatherCode;}
	public int weatherType(Room room)
	{
		if(!CoffeeUtensils.hasASky(room)) return Climate.WEATHER_CLEAR;
		return currentWeather;
	}
	public String weatherDescription(Room room)
	{
		if(!CoffeeUtensils.hasASky(room))
			return "You can't tell much about the weather from here.";
		return getWeatherDescription(room.getArea());
	}
	public boolean canSeeTheMoon(Room room)
	{
		if(((room.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_NIGHT)&&(room.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_DUSK))
		||(!CoffeeUtensils.hasASky(room)))
			return false;
		switch(weatherType(room))
		{
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_HAIL:
		case Climate.WEATHER_SLEET:
		case Climate.WEATHER_SNOW:
		case Climate.WEATHER_RAIN:
		case Climate.WEATHER_THUNDERSTORM:
		case Climate.WEATHER_CLOUDY:
		case Climate.WEATHER_DUSTSTORM:
			return false;
		default:
			return true;
		}
	}

	public boolean canSeeTheSun(Room room)
	{
		if(((room.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_DAY)&&(room.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_DAWN))
		||(!CoffeeUtensils.hasASky(room)))
			return false;

		switch(weatherType(room))
		{
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_HAIL:
		case Climate.WEATHER_SLEET:
		case Climate.WEATHER_SNOW:
		case Climate.WEATHER_RAIN:
		case Climate.WEATHER_THUNDERSTORM:
		case Climate.WEATHER_CLOUDY:
		case Climate.WEATHER_DUSTSTORM:
			return false;
		default:
			return true;
		}

	}
	private String getWeatherStop(int weatherCode)
	{
		switch(weatherCode)
		{
		case Climate.WEATHER_HAIL: return "The hailstorm stops.";
		case Climate.WEATHER_CLOUDY: return "The clouds dissipate.";
		case Climate.WEATHER_THUNDERSTORM: return "The thunderstorm stops.";
		case Climate.WEATHER_RAIN: return "It stops raining.";
		case Climate.WEATHER_SNOW: return "It stops snowing.";
		case Climate.WEATHER_WINDY: return "The wind gusts stop.";
		case Climate.WEATHER_WINTER_COLD: return "The cold snap is over.";
		case Climate.WEATHER_HEAT_WAVE: return "The heat wave eases.";
		case Climate.WEATHER_SLEET: return "The sleet stops pouring down.";
		case Climate.WEATHER_DUSTSTORM: return "The dust storm ends.";
		case Climate.WEATHER_DROUGHT: return "The drought is finally over.";
		}
		return "";
	}

	public void forceWeatherTick(Area A)
	{
		weatherTicker=1;
		weatherTick(A);
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

	public void weatherTick(Area A)
	{
		if((--weatherTicker)<=0)
		{
			// create a seasonal CHANCE graph
			int[] seasonal=new int[seasonalWeather.length];
			seasonal=addMaskAndReturn(seasonalWeather,seasonal);

			if((A.climateType()&Area.CLIMASK_COLD)>0)
				seasonal=addMaskAndReturn(seasonal,cold);

			if((A.climateType()&Area.CLIMASK_HOT)>0)
				seasonal=addMaskAndReturn(seasonal,hot);

			if((A.climateType()&Area.CLIMASK_DRY)>0)
				seasonal=addMaskAndReturn(seasonal,dry);

			if((A.climateType()&Area.CLIMASK_WET)>0)
				seasonal=addMaskAndReturn(seasonal,wet);

			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				seasonal=addMaskAndReturn(seasonal,windy);

			// reset the weather ticker!
			weatherTicker=WEATHER_TICK_DOWN;


			String say=null;
			int goodWeatherTotal=0;
			int possibleNextWeather=nextWeather;
			for(int g=0;g<Climate.NUM_WEATHER;g++)
			{
				// take the base chance for a seasonal weather occurrence (rain in winter, etc)
				int seasonalNum=seasonal[(A.getTimeObj().getSeasonCode()*Climate.NUM_WEATHER)+g];
				// find the chance of changing from what it will be, to some new condition.
				int changeNum=changeMap[(nextWeather*Climate.NUM_WEATHER)+g];
				// add them together to find the chance of a particular change in a particular season
				// to a particular condition.
				int chance=seasonalNum+changeNum;
				// total all the change chances, negative means NO chance of this change
				if(chance>0) goodWeatherTotal+=chance;
			}

			// some sort of debugging commentary
			/*StringBuffer buf=new StringBuffer(name()+"/"+(TimeClock.SEASON_DESCS[A.getTimeObj().getSeasonCode()])+"/"+Climate.WEATHER_DESCS[nextWeather]+"->");
			for(int g=0;g<Climate.NUM_WEATHER;g++)
			{
				int seasonalNum=seasonal[(A.getTimeObj().getSeasonCode()*Climate.NUM_WEATHER)+g];
				int changeNum=changeMap[(nextWeather*Climate.NUM_WEATHER)+g];
				int chance=seasonalNum+changeNum;
				//if(chance>0) buf.append(Climate.WEATHER_DESCS[g]+"="+chance+"("+seasonalNum+"+"+changeNum+"), ");
			}*/

			// roll a number from this to that.  Like the lottery, whosever number gets rolled wins!
			int newGoodWeatherNum=Dice.roll(1,goodWeatherTotal,-1);

			// now, determine the winner!
			int tempWeatherTotal=0;
			for(int g=0;g<Climate.NUM_WEATHER;g++)
			{
				// take the base chance for a seasonal weather occurrence (rain in winter, etc)
				int seasonalNum=seasonal[(A.getTimeObj().getSeasonCode()*Climate.NUM_WEATHER)+g];
				// find the chance of changing from what it will be, to some new condition.
				int changeNum=changeMap[(nextWeather*Climate.NUM_WEATHER)+g];
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
				switch(sayMap[(oldWeather*Climate.NUM_WEATHER)+currentWeather])
				{
				case 0: say=null; break;
				case 1: say=getWeatherDescription(A); break;
				case 2: say=stopWord; break;
				case 3: say=stopWord+" "+getWeatherDescription(A); break;
				}
			}
			else
			if(currentWeather==Climate.WEATHER_THUNDERSTORM)
				say="A bolt of lightning streaks across the sky.";

			if(say!=null)
			{
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(CoffeeUtensils.hasASky(R))
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
		if(ticking instanceof Area)
		{
			Area A=(Area)ticking;
			tickStatus=Tickable.STATUS_WEATHER;
			if((--botherDown)<=0)
			{
				botherDown=Climate.BOTHER_WEATHER_TICKS;
				switch(weatherType(null))
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_SLEET:
				case Climate.WEATHER_SNOW:
				case Climate.WEATHER_HAIL:
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_RAIN:
					for(Enumeration r=A.getMap();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(CoffeeUtensils.hasASky(R))
							for(int i=0;i<R.numInhabitants();i++)
							{
								MOB mob=R.fetchInhabitant(i);
								if((mob!=null)
								&&(!mob.isMonster())
								&&(Sense.aliveAwakeMobile(mob,true)))
									mob.tell(getWeatherDescription(A));
							}
					}
					break;
				}
			}
			weatherTick(A);
			if(weatherTicker==1)
			{
				int coldChance=0;
				int fluChance=0;
				int rustChance=0;
				switch(weatherType(null))
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_SLEET:
				case Climate.WEATHER_SNOW:
					coldChance=99;
					fluChance=25;
					rustChance=5;
					break;
				case Climate.WEATHER_HAIL:
					coldChance=50;
					rustChance=5;
					break;
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_RAIN:
					coldChance=25;
					rustChance=5;
					break;
				case Climate.WEATHER_WINTER_COLD:
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
						&&((weatherType(S.mob().location())!=Climate.WEATHER_CLEAR)))
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
						String weatherDesc=Climate.WEATHER_DESCS[weatherType].toLowerCase();
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
	
	protected String theWeatherDescription(Area A, int weather)
	{
		StringBuffer desc=new StringBuffer("");
		switch(weather)
		{
		case Climate.WEATHER_HAIL:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("Golfball sized clumps of ice ");
			else
				desc.append("Light streams of hail ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirl down from above.");
			else
				desc.append("fall from the sky.");
			break;
		case Climate.WEATHER_HEAT_WAVE:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("It is rather warm.");
			else
				desc.append("It is very hot. ");
			break;
		case Climate.WEATHER_WINTER_COLD:
			if(((A.climateType()&Area.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_SUMMER))
				desc.append("It is rather cold.");
			else
				desc.append("It is very cold. ");
			break;
		case Climate.WEATHER_DROUGHT:
			desc.append("There are horrible drought conditions.");
			break;
		case Climate.WEATHER_CLOUDY:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("Grey and gloomy clouds ");
			else
			if((A.climateType()&Area.CLIMASK_WET)>0)
				desc.append("Dark and looming stormclouds ");
			else
			if(((A.climateType()&Area.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_SUMMER))
				desc.append("Light whisps of cloud ");
			else
				desc.append("Fluffy cloudbanks ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("move across the sky.");
			else
				desc.append("obscure the sky.");
			break;
		case Climate.WEATHER_THUNDERSTORM:
			desc.append("A heavy and thunderous rainstorm ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("pours down from above.");
			break;
		case Climate.WEATHER_DUSTSTORM:
			desc.append("An eye-stinging dust storm ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("blows in from "+Directions.getFromDirectionName(this.windDirection));
			break;
		case Climate.WEATHER_BLIZZARD:
			desc.append("A thunderous blizzard ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("pours down from above.");
			break;
		case Climate.WEATHER_CLEAR:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("The weather is cool and clear.");
			else
			if(((A.climateType()&Area.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_SUMMER))
			{
				if((A.climateType()&Area.CLIMASK_WET)>0)
					desc.append("The weather is warm and humid, but clear.");
				else
					desc.append("The weather is warm and clear");
			}
			else
				desc.append("The weather is clear.");
			break;
		case Climate.WEATHER_RAIN:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("A cold light rain ");
			else
			if((A.climateType()&Area.CLIMASK_WET)>0)
				desc.append("A cool soaking rain ");
			else
			if(((A.climateType()&Area.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_SUMMER))
				desc.append("A warm rain ");
			else
				desc.append("A light rain ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Climate.WEATHER_SNOW:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("A light snow ");
			else
			if((A.climateType()&Area.CLIMASK_WET)>0)
				desc.append("A slushy snow ");
			else
			if(((A.climateType()&Area.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_SUMMER))
				desc.append("A freakish snow ");
			else
				desc.append("An unseasonable snow ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Climate.WEATHER_SLEET:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("A sleet storm ");
			else
			if((A.climateType()&Area.CLIMASK_WET)>0)
				desc.append("A slushy snow ");
			else
			if(((A.climateType()&Area.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_SUMMER))
				desc.append("A freakish sleet storm ");
			else
				desc.append("An unseasonable sleet storm ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls down from the sky.");
			else
				desc.append("falls from the sky.");
			break;
		case Climate.WEATHER_WINDY:
			if(((A.climateType()&Area.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER))
				desc.append("A cold "+(((A.climateType()&Area.CLIMASK_DRY)>0)?"dry ":"")+"wind ");
			else
			if((A.climateType()&Area.CLIMASK_WET)>0)
				desc.append("A forboding gust of wind ");
			else
			if(((A.climateType()&Area.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_SUMMER))
				desc.append("A hot "+(((A.climateType()&Area.CLIMASK_DRY)>0)?"dry ":"")+"wind ");
			else
				desc.append("A light "+(((A.climateType()&Area.CLIMASK_DRY)>0)?"dry ":"")+"wind ");
			desc.append("blows from "+Directions.getFromDirectionName(this.windDirection));
			break;
		}
		return desc.toString();
	}

	public String getWeatherDescription(Area A)
	{
		return theWeatherDescription(A,currentWeather);
	}

	public int adjustWaterConsumption(int base, MOB mob, Room room)
	{
		if(((room!=null)&&(room.domainType()&Room.INDOORS)==(Room.INDOORS)))
			return base;
		else
		switch(currentWeather)
		{
		case Climate.WEATHER_DROUGHT:
			return base*4;
		case Climate.WEATHER_DUSTSTORM:
			return base*3;
		case Climate.WEATHER_HEAT_WAVE:
			return base*2;
		case Climate.WEATHER_RAIN:
		case Climate.WEATHER_THUNDERSTORM:
			return (int)Math.round(Math.floor(Util.div(base,2)));
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_CLEAR:
		case Climate.WEATHER_CLOUDY:
		case Climate.WEATHER_HAIL:
		case Climate.WEATHER_WINDY:
		case Climate.WEATHER_WINTER_COLD:
			break;
		}
		return base;
	}

	public int adjustMovement(int base, MOB mob, Room room)
	{
		if(((room!=null)&&(room.domainType()&Room.INDOORS)==(Room.INDOORS)))
			return base;
		else
		switch(currentWeather)
		{
		case Climate.WEATHER_THUNDERSTORM:
			return base*2;
		case Climate.WEATHER_HAIL:
			return base*2;
		case Climate.WEATHER_DUSTSTORM:
			return base*3;
		case Climate.WEATHER_BLIZZARD:
			return base*4;
		}
		return base;
	}

}
