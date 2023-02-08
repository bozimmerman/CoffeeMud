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

import java.util.*;

/*
   Copyright 2002-2023 Bo Zimmerman

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
public class Chant_SummonSquall extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonSquall";
	}

	private final static String localizedName = CMLib.lang().L("Summon Squall");

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
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_WEATHERAFFECTING;
	}

	private int oldClimate = -1;

	@Override
	public void unInvoke()
	{
		final Physical P = affected;
		if((P instanceof Area)
		&&(oldClimate >= 0)
		&&(super.canBeUninvoked()))
		{
			final Climate C = ((Area)P).getClimateObj();
			C.setNextWeatherType(oldClimate);
			C.forceWeatherTick((Area)P);
		}
		super.unInvoke();
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if(CMath.bset(weatherQue(R),WEATHERQUE_RAIN))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;
		if(((R.domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if((A.fetchEffect(ID())!=null)
		||(A.getClimateObj().weatherType(R)==Climate.WEATHER_THUNDERSTORM))
		{
			mob.tell(L("It is already storming!"));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int size=A.numberOfProperIDedRooms();
		size=size/(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(size<0)
			size=0;
		boolean success=proficiencyCheck(mob,-size,auto);
		if(success)
		{
			final Climate C=A.getClimateObj();
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?L("^JThe sky changes color!^?"):L("^S<S-NAME> chant(s) into the sky for a squall!^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Chant_SummonSquall cA;
				cA = (Chant_SummonSquall)super.beneficialAffect(mob, givenTarget, asLevel, 4);
				success = cA != null;
				if(success)
				{
					cA.oldClimate = C.nextWeatherType(R);
					C.setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					C.forceWeatherTick(A);
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) into the sky for water, but the magic fizzles."));

		return success;
	}
}
