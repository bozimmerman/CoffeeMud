package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_SummonRain extends Chant
{
	public String ID() { return "Chant_SummonRain"; }
	public String name(){ return "Summon Rain";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;}
	public long flags(){return Ability.FLAG_WEATHERAFFECTING;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            Room R=mob.location();
            if(R!=null)
            {
                if(CMath.bset(weatherQue(R),WEATHERQUE_RAIN))
                    return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int size=mob.location().getArea().numberOfProperIDedRooms();
		size=size/(mob.envStats().level()+(2*super.getXLEVELLevel(mob)));
		if(size<0) size=0;
		boolean success=proficiencyCheck(mob,-size,auto);
		if(success)
		{
            Climate C=mob.location().getArea().getClimateObj();
            Climate oldC=(Climate)C.copyOf();
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"^JThe sky changes color!^?":"^S<S-NAME> chant(s) into the sky for water!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				switch(C.weatherType(mob.location()))
				{
				case Climate.WEATHER_BLIZZARD:
					C.setNextWeatherType(Climate.WEATHER_BLIZZARD);
					break;
				case Climate.WEATHER_CLEAR:
					C.setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_CLOUDY:
					C.setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_DROUGHT:
					C.setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_DUSTSTORM:
					C.setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_HAIL:
					C.setNextWeatherType(Climate.WEATHER_SLEET);
					break;
				case Climate.WEATHER_HEAT_WAVE:
					C.setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_RAIN:
					C.setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					break;
				case Climate.WEATHER_SLEET:
					C.setNextWeatherType(Climate.WEATHER_SLEET);
					break;
				case Climate.WEATHER_SNOW:
					C.setNextWeatherType(Climate.WEATHER_SLEET);
					break;
				case Climate.WEATHER_THUNDERSTORM:
					C.setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					break;
				case Climate.WEATHER_WINDY:
					C.setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					break;
				case Climate.WEATHER_WINTER_COLD:
					C.setNextWeatherType(Climate.WEATHER_SNOW);
					break;
				default:
					break;
				}
				C.forceWeatherTick(mob.location().getArea());
                Chant_CalmWeather.xpWorthyChange(mob,mob.location().getArea(),oldC,C);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky for water, but the magic fizzles.");

		return success;
	}
}
