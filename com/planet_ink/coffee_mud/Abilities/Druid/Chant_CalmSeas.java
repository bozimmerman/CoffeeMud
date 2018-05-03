package com.planet_ink.coffee_mud.Abilities.Druid;
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

import java.util.List;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Chant_CalmSeas extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_CalmSeas";
	}

	private final static String	localizedName	= CMLib.lang().L("Calm Seas");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_AREAS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public int abilityCode()
	{
		return eachDispelCost;
	}

	@Override
	public void setAbilityCode(int code)
	{
		super.setAbilityCode(code);
		eachDispelCost = code;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	public int eachDispelCost=10;
	
	private void exactCost()
	{
		if(this.canBeUninvoked)
		{
			super.tickDown -= eachDispelCost;
			if(super.tickDown < 1)
				super.tickDown = 1;
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
		{
			if(ticking instanceof Area)
			{
				final Area A=(Area)ticking;
				final Climate C=A.getClimateObj();
				if(C!=null)
				{
					switch(C.weatherType(null))
					{
					case Climate.WEATHER_WINDY:
						C.setNextWeatherType(Climate.WEATHER_CLEAR);
						exactCost();
						C.forceWeatherTick(A);
						break;
					case Climate.WEATHER_THUNDERSTORM:
						C.setNextWeatherType(Climate.WEATHER_RAIN);
						exactCost();
						C.forceWeatherTick(A);
						break;
					case Climate.WEATHER_BLIZZARD:
						C.setNextWeatherType(Climate.WEATHER_SNOW);
						exactCost();
						C.forceWeatherTick(A);
						break;
					case Climate.WEATHER_DUSTSTORM:
						C.setNextWeatherType(Climate.WEATHER_CLEAR);
						exactCost();
						C.forceWeatherTick(A);
						break;
					case Climate.WEATHER_HAIL:
						C.setNextWeatherType(Climate.WEATHER_CLOUDY);
						exactCost();
						C.forceWeatherTick(A);
						break;
					case Climate.WEATHER_SLEET:
						C.setNextWeatherType(Climate.WEATHER_CLOUDY);
						exactCost();
						C.forceWeatherTick(A);
						break;
					case Climate.WEATHER_SNOW:
						C.setNextWeatherType(Climate.WEATHER_CLOUDY);
						exactCost();
						C.forceWeatherTick(A);
						break;
					case Climate.WEATHER_RAIN:
						C.setNextWeatherType(Climate.WEATHER_CLOUDY);
						exactCost();
						C.forceWeatherTick(A);
						break;
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.tool() instanceof Ability)
		{
			final Ability A=(Ability)msg.tool();
			if((!msg.amISource(invoker()))&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_WATERCONTROL))
			{
				msg.source().tell(L("The water is magically calm here, and will not heed your call."));
				exactCost();
				if((A.affecting() != null) && (A.canBeUninvoked()))
					A.unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((((mob.location().domainType()&Room.INDOORS)>0)||(!CMLib.flags().isWateryRoom(mob.location())))
		&&(!auto))
		{
			mob.tell(L("You must be on the sea for this chant to work."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Chant_CalmSeas A=(Chant_CalmSeas)mob.location().getArea().fetchEffect(ID());
		if(A!=null)
		{
			final long remaining=A.tickDown*CMProps.getTickMillis();
			mob.tell(L("This area is under an enchantment of climactic balance, which can not be calmed for @x1.",mob.location().getArea().getTimeObj().deriveEllapsedTimeString(remaining)));
			return false;
		}
		int size=mob.location().getArea().numberOfProperIDedRooms();
		size=size/(mob.phyStats().level()+(super.getXLEVELLevel(mob)));
		if(size<0)
			size=0;
		final boolean success=proficiencyCheck(mob,-size,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.location().getArea(),this,verbalCastCode(mob,mob.location().getArea(),auto),auto?L("The sea changes color as it comes under control!"):L("^S<S-NAME> chant(s) into the sea for calm!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location().getArea(),asLevel,0);
				A=(Chant_CalmSeas)mob.location().getArea().fetchEffect(ID());
				if(A!=null)
				{
					int tickDown = A.tickDown;
					int numControls = adjustedLevel(mob,asLevel) / 6;
					if(numControls < 1)
						numControls = 1;
					A.setAbilityCode(tickDown / numControls);
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) into the sea for calm, but the magic fizzles."));

		return success;
	}
}
