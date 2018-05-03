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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class DefaultClimate implements Climate
{
	@Override
	public String ID()
	{
		return "DefaultClimate";
	}

	@Override
	public String name()
	{
		return "Climate Object";
	}

	protected int	tickStatus	= Tickable.STATUS_NOT;

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	protected int	currentWeather	= WEATHER_CLEAR;
	protected int	nextWeather		= WEATHER_CLEAR;
	protected int	weatherTicker	= WEATHER_TICK_DOWN;

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultClimate();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O=this.clone();
			return (CMObject)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultClimate();
		}
	}

	@Override
	public int nextWeatherType(Room room)
	{
		if(room==null)
			return nextWeather;
		if(!CMLib.map().hasASky(room))
			return Climate.WEATHER_CLEAR;
		return nextWeather;
	}

	@Override
	public String nextWeatherDescription(Room room)
	{
		if(!CMLib.map().hasASky(room))
			return CMLib.lang().fullSessionTranslation("You can't tell much about the weather from here.");
		return getNextWeatherDescription(room.getArea());
	}

	@Override
	public String getNextWeatherDescription(Area A)
	{
		return theWeatherDescription(A,nextWeather);
	}

	protected final static
	int[] seasonalWeather={
		/*  		-   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  40, 20, 10, 14,  5,  1,  0,  5,  0,  0,  0,  0,  5,
		/*SUMMER*/  31, 20, 5,  10, 12,  0,  0, 20,  0,  0,  1,  1,  0,
		/*FALL*/	37, 10, 15, 15, 10,  5,  2,  5,  2,  1,  0,  0, 10,
		/*WINTER*/  32, 15, 11,  4,  2,  7,  3,  0,  3,  3,  0,  0, 20,
	};

	protected final static
	int[] cold={
		/*  		-   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  -5, -5,  5,-10,  0,  5,  0, -5,  5,  0,  0,  0,  10,
		/*SUMMER*/   5,  1,  5,  0,  0,  1,  1,-20,  1,  1,  0,  0,  5,
		/*FALL*/	 0,  0,  1, -5,  0,  1,  1, -5,  1,  1,  0,  0,  5,
		/*WINTER*/ -15,  0,  0, -4, -2,  5,  2,  0,  2,  2,  0,  0,  10,
	};
	protected final static
	int[] hot={
		/*  		-   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/   5,  5, -5, 10,  0, -5,  0,  5, -5,  0,  0,  0, -10,
		/*SUMMER*/  -5, -1, -5,  0,  0, -1, -1, 20, -1, -1,  0,  0, -5,
		/*FALL*/	 0,  0, -1,  5,  0, -1, -1,  5, -1, -1,  0,  0, -5,
		/*WINTER*/  15,  0,  0,  4,  2, -5, -2,  0, -2, -2,  0,  0, -10,
	};
	protected final static
	int[] dry={
		/*  		-   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/  10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
		/*SUMMER*/  10,-22,  0,  0,  0,  0,  0,  0,  0,  0,  6,  6,   0,
		/*FALL*/	10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
		/*WINTER*/  10,-15,  0,  0,  0,  0,  0,  2,  0,  0,  0,  3,   0,
	};
	protected final static
	int[] wet={
		/*  		-   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/ -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,  -2,
		/*SUMMER*/ -10, 22,  0,  0,  0,  0,  0,  0,  0,  0, -6, -6,   0,
		/*FALL*/   -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,  -2,
		/*WINTER*/ -10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3,   2,
	};
	protected final static
	int[] windy={
		/*  		-   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
		/*SPRING*/ -10,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   0,
		/*SUMMER*/ -10,  0, 11,  0,  0,  0,  0, -2,  0,  0,  0,  1,   0,
		/*FALL*/   -10,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   0,
		/*WINTER*/ -10, -2, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,   2,
	};
	protected final static
	int[] changeMap=		{
	/*					 -    CL   WD   RA   TH   SN   HA   HE   SL   BL   DU   DR   WC*/
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

	@Override
	public void setNextWeatherType(int weatherCode)
	{
		nextWeather = weatherCode;
	}

	@Override
	public void setCurrentWeatherType(int weatherCode)
	{
		currentWeather = weatherCode;
	}

	@Override
	public int weatherType(Room room)
	{
		if(room==null)
			return currentWeather;
		if(!CMLib.map().hasASky(room))
			return Climate.WEATHER_CLEAR;
		return currentWeather;
	}

	@Override
	public String weatherDescription(Room room)
	{
		if(!CMLib.map().hasASky(room))
			return CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.WEATHER_NONE, 0);
		return getWeatherDescription(room.getArea());
	}

	@Override
	public boolean canSeeTheMoon(Room room, Ability butNotA)
	{
		if(canSeeTheStars(room))
			return true;
		final List<Ability> V=CMLib.flags().domainAffects(room,Ability.DOMAIN_MOONSUMMONING);
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v)!=butNotA)
				return true;
		}
		return false;
	}

	@Override
	public boolean canSeeTheStars(Room room)
	{
		if(((room.getArea().getTimeObj().getTODCode()!=TimeClock.TimeOfDay.NIGHT)
				&&(room.getArea().getTimeObj().getTODCode()!=TimeClock.TimeOfDay.DUSK))
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

	@Override
	public boolean canSeeTheSun(Room room)
	{
		if((room==null)
		||((room.getArea().getTimeObj().getTODCode()!=TimeClock.TimeOfDay.DAY)
			&&(room.getArea().getTimeObj().getTODCode()!=TimeClock.TimeOfDay.DAWN))
		||(!CMLib.map().hasASky(room))
		||(CMLib.flags().isInDark(room)))
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
		if((weatherCode>=0)&&(weatherCode<Climate.NUM_WEATHER))
			return CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.WEATHER_ENDS, weatherCode);
		return "";
	}

	@Override
	public void forceWeatherTick(Area A)
	{
		weatherTicker=1;
		weatherTick(A);
	}

	public int[] addMaskAndReturn(int[] one, int[] two)
	{
		if(one.length!=two.length)
			return one;
		final int[] returnable=new int[one.length];
		for(int o=0;o<one.length;o++)
			returnable[o]=one[o]+two[o];
		return returnable;
	}

	protected void sayToEveryoneInArea(Area A, int oldWeather, String say)
	{
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(CMLib.map().hasASky(R))
			{
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB mob=R.fetchInhabitant(i);
					if((mob!=null)
					&&(!mob.isMonster())
					&&(CMLib.flags().canSee(mob)||(currentWeather!=oldWeather)))
						mob.tell(say);
				}
			}
		}
		for(Enumeration<BoardableShip> s =CMLib.map().ships();s.hasMoreElements();)
		{
			final BoardableShip ship = s.nextElement();
			if((ship != null) && (A == CMLib.map().areaLocation(ship.getShipItem())))
			{
				Area inA=ship.getShipArea();
				if(inA!=null)
					sayToEveryoneInArea(inA,oldWeather,say);
			}
		}
	}
	
	public void weatherTick(Area A)
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.WEATHER))
		{
			currentWeather = Climate.WEATHER_CLEAR;
			return;
		}
		if((--weatherTicker)<=0)
		{
			// create a seasonal CHANCE graph
			int[] seasonal=new int[seasonalWeather.length];
			seasonal=addMaskAndReturn(seasonalWeather,seasonal);
			final int derivedClimate=A.getClimateType();
			if((derivedClimate&Places.CLIMASK_COLD)>0)
				seasonal=addMaskAndReturn(seasonal,cold);

			if((derivedClimate&Places.CLIMASK_HOT)>0)
				seasonal=addMaskAndReturn(seasonal,hot);

			if((derivedClimate&Places.CLIMASK_DRY)>0)
				seasonal=addMaskAndReturn(seasonal,dry);

			if((derivedClimate&Places.CLIMASK_WET)>0)
				seasonal=addMaskAndReturn(seasonal,wet);

			if((derivedClimate&Places.CLIMASK_WINDY)>0)
				seasonal=addMaskAndReturn(seasonal,windy);

			// reset the weather ticker!
			weatherTicker=WEATHER_TICK_DOWN;

			String say=null;
			int goodWeatherTotal=0;
			int possibleNextWeather=nextWeather;
			for(int g=0;g<Climate.NUM_WEATHER;g++)
			{
				// take the base chance for a seasonal weather occurrence (rain in winter, etc)
				final int seasonalNum=seasonal[(A.getTimeObj().getSeasonCode().ordinal()*Climate.NUM_WEATHER)+g];
				// find the chance of changing from what it will be, to some new condition.
				final int changeNum=changeMap[(nextWeather*Climate.NUM_WEATHER)+g];
				// add them together to find the chance of a particular change in a particular season
				// to a particular condition.
				final int chance=seasonalNum+changeNum;
				// total all the change chances, negative means NO chance of this change
				if(chance>0)
					goodWeatherTotal+=chance;
			}

			// some sort of debugging commentary
			/*StringBuffer buf=new StringBuffer(name()+"/"+(A.getTimeObj().getSeasonCode().toString())+"/"+Climate.WEATHER_DESCS[nextWeather]+"->");
			for(int g=0;g<Climate.NUM_WEATHER;g++)
			{
				int seasonalNum=seasonal[(A.getTimeObj().getSeasonCode()*Climate.NUM_WEATHER)+g];
				int changeNum=changeMap[(nextWeather*Climate.NUM_WEATHER)+g];
				int chance=seasonalNum+changeNum;
				//if(chance>0) buf.append(Climate.WEATHER_DESCS[g]+"="+chance+"("+seasonalNum+"+"+changeNum+"), ");
			}*/

			// roll a number from this to that.  Like the lottery, whosever number gets rolled wins!
			final int newGoodWeatherNum=CMLib.dice().roll(1,goodWeatherTotal,-1);

			// now, determine the winner!
			int tempWeatherTotal=0;
			for(int g=0;g<Climate.NUM_WEATHER;g++)
			{
				// take the base chance for a seasonal weather occurrence (rain in winter, etc)
				final int seasonalNum=seasonal[(A.getTimeObj().getSeasonCode().ordinal()*Climate.NUM_WEATHER)+g];
				// find the chance of changing from what it will be, to some new condition.
				final int changeNum=changeMap[(nextWeather*Climate.NUM_WEATHER)+g];
				// add them together to find the chance of a particular change in a particular season
				// to a particular condition.
				final int chance=seasonalNum+changeNum;
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
			final int oldWeather=currentWeather;
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.WEATHERCHANGES))
			{
				currentWeather=nextWeather;
				nextWeather=possibleNextWeather;
			}
			if(oldWeather!=currentWeather)
			{
				// 0=say nothing;
				// 1=say weatherdescription only
				// 2=say stop word only
				// 3=say stop word, then weatherdescription
				/*					 -   CL  WD  RA  TH  SN  HA  HE  SL  BL  DU  DR  WC*/
				final int[] sayMap=		{
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
				final String stopWord=getWeatherStop(oldWeather);
				switch(sayMap[(oldWeather*Climate.NUM_WEATHER)+currentWeather])
				{
				case 0:
					break; // say=null break;
				case 1:
					say = getWeatherDescription(A);
					break;
				case 2:
					say = stopWord;
					break;
				case 3:
					say = stopWord + " " + getWeatherDescription(A);
					break;
				}
			}

			if((say!=null)&&!CMSecurity.isDisabled(CMSecurity.DisFlag.WEATHERNOTIFIES))
			{
				sayToEveryoneInArea(A,oldWeather,say);
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(ticking instanceof Area)
		{
			final Area A=(Area)ticking;
			tickStatus=Tickable.STATUS_WEATHER;
			weatherTick(A);
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	protected String theWeatherDescription(Area A, int weather)
	{
		final StringBuffer desc=new StringBuffer("");
		if((weather<0)||(weather>=Climate.NUM_WEATHER))
			return "";
		final int listFileOrd = CMProps.ListFile.WEATHER_CLEAR.ordinal() + weather;
		final CMProps.ListFile listFileEnum = CMProps.ListFile.values()[listFileOrd];
		final String prefix;
		//#    NORMAL, WET, COLD (WINTER), HOT (SUMMER), DRY
		final int derivedClimate=A.getClimateType();
		if(((derivedClimate&Places.CLIMASK_COLD)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.Season.WINTER))
			prefix=CMProps.getListFileChoiceFromIndexedList(listFileEnum, 2);
		else
		if(((derivedClimate&Places.CLIMASK_HOT)>0)||(A.getTimeObj().getSeasonCode()==TimeClock.Season.SUMMER))
			prefix=CMProps.getListFileChoiceFromIndexedList(listFileEnum, 3);
		else
		if((derivedClimate&Places.CLIMASK_WET)>0)
			prefix=CMProps.getListFileChoiceFromIndexedList(listFileEnum, 1);
		else
		if((derivedClimate&Places.CLIMASK_DRY)>0)
			prefix=CMProps.getListFileChoiceFromIndexedList(listFileEnum, 4);
		else
			prefix=CMProps.getListFileChoiceFromIndexedList(listFileEnum, 0);
		final String suffix;
		if((derivedClimate&Places.CLIMASK_WINDY)>0)
			suffix=CMProps.getListFileChoiceFromIndexedList(listFileEnum, 5);
		else
			suffix=CMProps.getListFileChoiceFromIndexedList(listFileEnum, 6);
		desc.append((suffix.trim().length()>0) ? prefix + " " + suffix : prefix);
		switch(weather)
		{
		case Climate.WEATHER_HAIL:
			desc.append(CMLib.protocol().msp("hail.wav", 10));
			break;
		case Climate.WEATHER_HEAT_WAVE:
			break;
		case Climate.WEATHER_WINTER_COLD:
			break;
		case Climate.WEATHER_DROUGHT:
			break;
		case Climate.WEATHER_CLOUDY:
			break;
		case Climate.WEATHER_THUNDERSTORM:
			break;
		case Climate.WEATHER_DUSTSTORM:
			desc.append(CMLib.protocol().msp("windy.wav", 10));
			break;
		case Climate.WEATHER_BLIZZARD:
			desc.append(CMLib.protocol().msp("blizzard.wav", 10));
			break;
		case Climate.WEATHER_CLEAR:
			break;
		case Climate.WEATHER_RAIN:
			desc.append(CMLib.protocol().msp("rainlong.wav", 10));
			break;
		case Climate.WEATHER_SNOW:
			break;
		case Climate.WEATHER_SLEET:
			desc.append(CMLib.protocol().msp("rain.wav", 10));
			break;
		case Climate.WEATHER_WINDY:
			desc.append(CMLib.protocol().msp("wind.wav", 10));
			break;
		}
		return "^J"+desc.toString()+"^?";
	}

	@Override
	public String getWeatherDescription(Area A)
	{
		return theWeatherDescription(A,currentWeather);
	}

	@Override
	public int adjustWaterConsumption(int base, Room room)
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

	@Override
	public int adjustMovement(int base, Room room)
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

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}
}
