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

public class Chant_ControlWeather extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_ControlWeather";
	}

	private final static String localizedName = CMLib.lang().L("Control Weather");

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
		return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;
	}

	public int controlCode=0;

	@Override
	public int abilityCode()
	{
		return controlCode;
	}

	@Override
	public void setAbilityCode(int code)
	{
		super.setAbilityCode(code);
		controlCode=code;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.tool() instanceof Ability)
		&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_WEATHERAFFECTING)))
		{
			if(abilityCode()==1)
			{
				final Ability A=(Ability)msg.tool();
				if((!msg.amISource(invoker()))
				||(((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_MOONALTERING)
				   &&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_MOONSUMMONING)))
				{
					msg.source().tell(L("The weather is finely balanced here, and will not heed your call."));
					return false;
				}
			}
			else
			if(!msg.amISource(invoker()))
			{
				msg.source().tell(L("The sky here does not heed to your call."));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Chant_ControlWeather A=(Chant_ControlWeather)mob.location().getArea().fetchEffect(ID());
		if((A!=null)&&(A.abilityCode()==1))
		{
			final long remaining=A.tickDown*CMProps.getTickMillis();
			mob.tell(L("This area is under an enchantment of climactic balance, which can not be controlled for @x1.",mob.location().getArea().getTimeObj().deriveEllapsedTimeString(remaining)));
			return false;
		}
		int size=mob.location().getArea().numberOfProperIDedRooms();
		size=size/(mob.phyStats().level()+(super.getXLEVELLevel(mob)));
		if(size<0)
			size=0;
		if(A!=null)
			size=size-((A.invoker().phyStats().level()-(mob.phyStats().level()+(super.getXLEVELLevel(mob))))*10);
		final boolean success=proficiencyCheck(mob,-size,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.location().getArea(),this,verbalCastCode(mob,mob.location().getArea(),auto),auto?L("The sky changes color as the weather comes under control!"):L("^S<S-NAME> chant(s) into the sky for control of the weather!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((A!=null)&&(A.invoker()!=mob))
					mob.tell(L("You successfully wrest control of the weather from @x1.",A.invoker().name()));
				if(A!=null)
					A.unInvoke();
				beneficialAffect(mob,mob.location().getArea(),asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) into the sky for control, but the magic fizzles."));

		return success;
	}
}
