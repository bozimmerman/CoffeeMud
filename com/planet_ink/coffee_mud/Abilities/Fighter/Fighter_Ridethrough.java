package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2023-2025 Bo Zimmerman

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
public class Fighter_Ridethrough extends StdAbility
{
	@Override
	public String ID()
	{
		return "Fighter_Ridethrough";
	}

	private final static String localizedName = CMLib.lang().L("Ridethrough");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"RIDETHROUGH"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
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
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	@Override
	public int maxRange()
	{
		return 99;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You must be in combat to ridethrough!"));
			return false;
		}
		final MOB target=mob.getVictim();
		final Rideable mount = mob.riding();
		if(!CMLib.flags().isMobileMounted(mob))
		{
			mob.tell(L("You must be riding a mount to use this skill."));
			return false;
		}

		if((!auto)
		&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You must be in melee range of your target to ride through them."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final int oldRange = mob.rangeToTarget();
		final boolean success=proficiencyCheck(mob,0,auto) && (auto||(!CMLib.flags().isMobileMounted(target)));
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.getVictim(),this,CMMsg.MSG_RETREAT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int totMoves = 3 + super.getXLEVELLevel(mob) /2;
				if(totMoves + mob.rangeToTarget() > mob.location().maxRange())
					totMoves = mob.location().maxRange() - mob.rangeToTarget();
				for(int i=1;i<totMoves;i++)
				{
					if(mob.location().okMessage(mob, msg))
						mob.location().send(mob, msg);
				}
			}
		}
		if((success) && (mob.rangeToTarget() > oldRange))
			return mob.location().show(mob, target, this, CMMsg.MSG_NOISYMOVEMENT,
					L("^F^<FIGHT^><S-NAME> @x2 through <T-NAMESELF> to a range of @x1!^?^</FIGHT^>",""+mob.rangeToTarget(),mount.rideString(mob)));
		else
			return beneficialVisualFizzle(mob,mob.getVictim(),L("<S-NAME> attempt(s) to @x1 through <T-NAMESELF>, but <S-IS-ARE> disrupted.",mount.rideString(mob)));
	}
}
