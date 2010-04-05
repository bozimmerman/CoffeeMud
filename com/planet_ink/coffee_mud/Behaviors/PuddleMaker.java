package com.planet_ink.coffee_mud.Behaviors;
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
public class PuddleMaker extends StdBehavior
{
	public String ID(){return "PuddleMaker";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
    protected int lastWeather=-1;

	public boolean coldWetWeather(int weather)
	{
		switch(weather)
		{
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_SLEET:
		case Climate.WEATHER_SNOW:
		case Climate.WEATHER_HAIL:
			return true;
		}
		return false;
	}
    public boolean coldWeather(int weather)
    {
        switch(weather)
        {
        case Climate.WEATHER_BLIZZARD:
        case Climate.WEATHER_SLEET:
        case Climate.WEATHER_SNOW:
        case Climate.WEATHER_HAIL:
        case Climate.WEATHER_WINTER_COLD:
            return true;
        }
        return false;
    }
	public boolean dryWeather(int weather)
	{
		switch(weather)
		{
		case Climate.WEATHER_DROUGHT:
		case Climate.WEATHER_DUSTSTORM:
		case Climate.WEATHER_HEAT_WAVE:
			return true;
		}
		return false;
	}
	public boolean justWetWeather(int weather)
	{
		switch(weather)
		{
		case Climate.WEATHER_RAIN:
		case Climate.WEATHER_THUNDERSTORM:
			return true;
		}
		return false;
	}
	public boolean anyWetWeather(int weather)
	{
		return coldWetWeather(weather)||justWetWeather(weather);
	}
	public int pct()
	{
		int pct=50;
		if(getParms().length()>0)
			pct=CMath.s_int(getParms());
		return pct;
	}

	public void makePuddle(Room R, int oldWeather, int newWeather)
	{
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if((I instanceof Drink)
			   &&(!CMLib.flags().isGettable(I))
			   &&((I.name().toLowerCase().indexOf("puddle")>=0)
				  ||(I.name().toLowerCase().indexOf("snow")>=0)))
					return;
		}
		Item I=CMClass.getItem("GenLiquidResource");
		CMLib.flags().setGettable(I,false);
		((Drink)I).setLiquidHeld(100);
		((Drink)I).setLiquidRemaining(100);
		((Drink)I).setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
		I.setMaterial(RawMaterial.RESOURCE_FRESHWATER);
        I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_UNSAVABLE);
		CMLib.materials().addEffectsToResource(I);
        I.recoverEnvStats();
		if(coldWetWeather(oldWeather))
		{
			I.setName("some snow");
			I.setDisplayText("some snow rests on the ground here.");
			I.setDescription("the snow is white and still quite cold!");
		}
		else
		{
			I.setName("a puddle of water");
			I.setDisplayText("a puddle of water has formed here.");
			I.setDescription("It looks drinkable.");
		}
		R.addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
		R.recoverRoomStats();
	}



	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(anyWetWeather(lastWeather))
		{
			if(ticking instanceof Room)
			{
				Room R=(Room)ticking;
				Area A=R.getArea();
				if((!anyWetWeather(A.getClimateObj().weatherType(R)))
				&&(!dryWeather(A.getClimateObj().weatherType(R)))
				&&(CMLib.dice().rollPercentage()<pct()))
					makePuddle(R,lastWeather,A.getClimateObj().weatherType(R));
			}
			else
			if(ticking instanceof Area)
			{
				Area A=(Area)ticking;
				if((!anyWetWeather(A.getClimateObj().weatherType(null)))
				&&(!dryWeather(A.getClimateObj().weatherType(null))))
					for(Enumeration e=A.getProperMap();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						if(((R.domainType()&Room.INDOORS)==0)
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(CMLib.dice().rollPercentage()<pct()))
							makePuddle(R,lastWeather,A.getClimateObj().weatherType(null));
					}
			}
		}

		if(ticking instanceof Room)
			lastWeather=((Room)ticking).getArea().getClimateObj().weatherType((Room)ticking);
		else
		if(ticking instanceof Area)
			lastWeather=((Area)ticking).getClimateObj().weatherType(null);
		return true;
	}
}
