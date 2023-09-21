package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2022-2023 Bo Zimmerman

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
public class Thief_SetTimer extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SetTimer";
	}

	private final static String	localizedName	= CMLib.lang().L("Set Timer");

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
		return Ability.CAN_ITEMS | Ability.CAN_EXITS | Ability.CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SETTIMER" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_TRAPPING;
	}

	public int				code		= 0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code = newCode;
	}

	@Override
	protected boolean ignoreCompounding()
	{
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		int timer=30;
		if(!auto)
		{
			if(commands.size()<2)
			{
				mob.tell(L("Set how much time on what?"));
				return false;
			}
			timer=CMath.s_int(commands.remove(0));
			if(timer<0)
			{
				mob.tell(L("@x1 is not a valid amount of time.",commands.get(0)));
				return false;
			}
		}
		final Physical checkP=super.getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_ANY);
		if(checkP==null)
			return false;

		final Trap T=CMLib.utensils().fetchMyTrap(checkP);
		if(T==null)
		{
			mob.tell(L("@x1 doesn't appear to be trapped.",checkP.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int adjustment=((mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)))-checkP.phyStats().level())*5;
		if(adjustment>0)
			adjustment=0;
		final boolean success=proficiencyCheck(mob,adjustment,auto);

		if(!success)
			beneficialVisualFizzle(mob,checkP,L("<S-NAME> attempt(s) to set the timer on <T-NAME> and fail(s)."));
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,checkP,this,auto?CMMsg.MSG_OK_VISUAL:(CMMsg.MSG_DELICATE_SMALL_HANDS_ACT),
					CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,L("<S-NAME> set(s) the timer on <T-NAME>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob, msg);
				int finalTime = (int)Math.round(Math.ceil(CMath.div(timer,4.0)));
				if((timer>0)&&(finalTime<=0))
					finalTime = 1;
				T.setReset(timer);
			}
		}

		return success;
	}
}
