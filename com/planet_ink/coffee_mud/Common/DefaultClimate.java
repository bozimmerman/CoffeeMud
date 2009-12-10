package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class DefaultClimate implements Climate
{
	public String ID(){return "DefaultClimate";}
	public String name(){return "Climate Object";}
	protected long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	protected int currentWeather=WEATHER_CLEAR;
	protected int nextWeather=WEATHER_CLEAR;
	protected int weatherTicker=WEATHER_TICK_DOWN;
	protected static int windDirection=Directions.NORTH;

    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultClimate();}}
    public void initializeClass(){}
    public CMObject copyOf()
    {
        try
        {
            Object O=this.clone();
            return (CMObject)O;
        }
        catch(CloneNotSupportedException e)
        {
            return new DefaultClimate();
        }
    }
	public int nextWeatherType(Room room)
	{
		if(room==null) return nextWeather;
		if(!CMLib.map().hasASky(room)) return Climate.WEATHER_CLEAR;
		return nextWeather;
	}
	public String nextWeatherDescription(Room room)
	{
		if(!CMLib.map().hasASky(room)) return "You can't tell much about the weather from here.";
		return getNextWeatherDescription(room.getArea());
	}
	public String getNextWeatherDescription(Area A)
	{
		return theWeatherDescription(A,nextWeather);
	}

    protected final static
	int[] seasonalWeather={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  40, 20, 10, 14,  5,  1,  0,  5,  0,  0,  0,  0,  5,
		/*SUMMER*/  31, 20, 5,  10, 12,  0,  0, 20,  0,  0,  1,  1,  0,
		/*FALL*/    37, 10, 15, 15, 10,  5,  2,  5,  2,  1,  0,  0, 10,
		/*WINTER*/  32, 15, 11,  4,  2,  7,  3,  0,  3,  3,  0,  0, 20,
	};

    protected final static
	int[] cold={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  -5, -5,  5,-10,  0,  5,  0, -5,  5,  0,  0,  0,  10,
		/*SUMMER*/   5,  1,  5,  0,  0,  1,  1,-20,  1,  1,  0,  0,  5,
		/*FALL*/     0,  0,  1, -5,  0,  1,  1, -5,  1,  1,  0,  0,  5,
		/*WINTER*/ -15,  0,  0, -4, -2,  5,  2,  0,  2,  2,  0,  0,  10,
	};
    protected final static
	int[] hot={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/   5,  5, -5, 10,  0, -5,  0,  5, -5,  0,  0,  0, -10,
		/*SUMMER*/  -5, -1, -5,  0,  0, -1, -1, 20, -1, -1,  0,  0, -5,
		/*FALL*/     0,  0, -1,  5,  0, -1, -1,  5, -1, -1,  0,  0, -5,
		/*WINTER*/  15,  0,  0,  4,  2, -5, -2,  0, -2, -2,  0,  0, -10,
	};
    protected final static
	int[] dry={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
		/*SUMMER*/  10,-22,  0,  0,  0,  0,  0,  0,  0,  0,  6,  6,   0,
		/*FALL*/    10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
		/*WINTER*/  10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
	};
    protected final static
	int[] wet={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/ -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,  -2,
		/*SUMMER*/ -10, 22,  0,  0,  0,  0,  0,  0,  0,  0, -6, -6,   0,
		/*FALL*/   -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,  -2,
		/*WINTER*/ -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,   2,
	};
    protected final static
	int[] windy={
		/*          -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/ -10,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   0,
		/*SUMMER*/ -10,  0, 11,  0,  0,  0,  0, -2,  0,  0,  0,  1,   0,
		/*FALL*/   -10,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   0,
		/*WINTER*/ -10, -2, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   2,
	};
    protected final static
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
		if(room==null) return currentWeather;
		if(!CMLib.map().hasASky(room)) return Climate.WEATHER_CLEAR;
		return currentWeather;
	}
	public String weatherDescription(Room room)
	{
		if(!CMLib.map().hasASky(room))
			return "^JYou can't tell much about the weather from here.^?";
		return getWeatherDescription(room.getArea());
	}
	public boolean canSeeTheMoon(Room room, Ability butNotA)
	{
		if(canSeeTheStars(room)) return true;
		Vector V=CMLib.flags().domainAffects(room,Ability.DOMAIN_MOONSUMMONING);
		for(int v=0;v<V.size();v++)
			if(V.elementAt(v)!=butNotA)
				return true;
		return false;
	}
	public boolean canSeeTheStars(Room room)
	{
		if(((room.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_NIGHT)
                &&(room.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_DUSK))
		||(!CMLib.map().hasASky(room)))
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
		||(!CMLib.map().hasASky(room)))
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
	protected String getWeatherStop(int weatherCode)
	{
		switch(weatherCode)
		{
		case Climate.WEATHER_HAIL: return "^JThe hailstorm stops.^?";
		case Climate.WEATHER_CLOUDY: return "^JThe clouds dissipate.^?";
		case Climate.WEATHER_THUNDERSTORM: return "^JThe thunderstorm stops.^?";
		case Climate.WEATHER_RAIN: return "^JIt stops raining.^?";
		case Climate.WEATHER_SNOW: return "^JIt stops snowing.^?";
		case Climate.WEATHER_WINDY: return "^JThe wind gusts stop.^?";
		case Climate.WEATHER_WINTER_COLD: return "^JThe cold snap is over.^?";
		case Climate.WEATHER_HEAT_WAVE: return "^JThe heat wave eases.^?";
		case Climate.WEATHER_SLEET: return "^JThe sleet stops pouring down.^?";
		case Climate.WEATHER_DUSTSTORM: return "^JThe dust storm ends.^?";
		case Climate.WEATHER_DROUGHT: return "^JThe drought is finally over.^?";
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
		if(CMSecurity.isDisabled("WEATHER"))
		{
			currentWeather = Climate.WEATHER_CLEAR;
			return;
		}
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
			int newGoodWeatherNum=CMLib.dice().roll(1,goodWeatherTotal,-1);

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
			if(!CMSecurity.isDisabled("WEATHERCHANGES"))
			{
				currentWeather=nextWeather;
				nextWeather=possibleNextWeather;
			}
			if(oldWeather!=currentWeather)
			{
				switch(CMLib.dice().rollPercentage())
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
				case 0: break; //say=null break;
				case 1: say=getWeatherDescription(A); break;
				case 2: say=stopWord; break;
				case 3: say=stopWord+" "+getWeatherDescription(A); break;
				}
			}

			if((say!=null)&&!CMSecurity.isDisabled("WEATHERNOTIFIES"))
			{
				for(Enumeration r=A.getProperMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(CMLib.map().hasASky(R))
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB mob=R.fetchInhabitant(i);
							if((mob!=null)
							&&(!mob.isMonster())
							&&(CMLib.flags().canSee(mob)||(currentWeather!=oldWeather)))
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
			weatherTick(A);
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
            desc.append(CMProps.msp("hail.wav",10));
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
				desc.append("Light wisps of cloud ");
			else
				desc.append("Fluffy cloudbanks ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("move across the sky.");
			else
				desc.append("obscure the sky.");
			break;
		case Climate.WEATHER_THUNDERSTORM:
			desc.append("A heavy and blusterous rainstorm ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("pours down from above.");
            desc.append(CMProps.msp("thunderandrain.wav",10));
			break;
		case Climate.WEATHER_DUSTSTORM:
			desc.append("An eye-stinging dust storm ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("blows in from "+Directions.getFromDirectionName(windDirection));
            desc.append(CMProps.msp("windy.wav",10));
			break;
		case Climate.WEATHER_BLIZZARD:
			desc.append("A thunderous blizzard ");
			if((A.climateType()&Area.CLIMATE_WINDY)>0)
				desc.append("swirls all around you.");
			else
				desc.append("pours down from above.");
            desc.append(CMProps.msp("blizzard.wav",10));
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
            desc.append(CMProps.msp("rainlong.wav",10));
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
            desc.append(CMProps.msp("rain.wav",10));
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
			desc.append("blows from "+Directions.getFromDirectionName(windDirection)+".");
            desc.append(CMProps.msp("wind.wav",10));
			break;
		}
		return "^J"+desc.toString()+"^?";
	}

	public String getWeatherDescription(Area A)
	{
		return theWeatherDescription(A,currentWeather);
	}

	public int adjustWaterConsumption(int base, MOB mob, Room room)
	{
		if(((room!=null)&&(room.domainType()&Room.INDOORS)==(Room.INDOORS)))
			return base;
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
			return (int)Math.round(Math.floor(CMath.div(base,2)));
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
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
