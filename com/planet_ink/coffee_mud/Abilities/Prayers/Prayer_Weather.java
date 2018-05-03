package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Prayer_Weather extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Weather";
	}

	private final static String localizedName = CMLib.lang().L("Change Weather");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int size=mob.location().getArea().numberOfProperIDedRooms();
		size=size-((mob.phyStats().level()+(2*getXLEVELLevel(mob)))*20);
		if(size<0)
			size=0;
		final boolean success=proficiencyCheck(mob,-size,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?L("The sky changes color!"):L("^S<S-NAME> @x1 for a change in weather!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				final int switcher=CMLib.dice().roll(1,3,0);
				mob.location().send(mob,msg);
				switch(mob.location().getArea().getClimateObj().weatherType(mob.location()))
				{
				case Climate.WEATHER_BLIZZARD:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_BLIZZARD);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_BLIZZARD);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SNOW);
					break;
				case Climate.WEATHER_CLEAR:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_CLOUDY:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_DROUGHT:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_HEAT_WAVE);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_DUSTSTORM:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_HAIL:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_HAIL);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SLEET);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_HEAT_WAVE:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_RAIN:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_SLEET:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SLEET);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SLEET);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_SNOW:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_BLIZZARD);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SLEET);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_THUNDERSTORM:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_WINDY:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_WINTER_COLD:
					if(switcher==1)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SNOW);
					else
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				default:
					break;
				}
				mob.location().getArea().getClimateObj().forceWeatherTick(mob.location().getArea());
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> @x1, but nothing happens.",prayWord(mob)));

		return success;
	}
}
