package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_Forceback extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Forceback";
	}

	private final static String localizedName = CMLib.lang().L("Forceback");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
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
	protected long minCastWaitTime()
	{
		return 60000L;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target = mob.getVictim();
		if((target == null)
		||((target.location()!=mob.location())))
		{
			mob.tell(L("You must be in combat to do that."));
			return false;
		}
		final Item I = mob.fetchWieldedItem();
		if(!(I instanceof Weapon))
		{
			mob.tell(L("You can not do this with @x1.",I.name(mob)));
			return false;
		}
		final Weapon W = (Weapon)I;
		if((!W.rawLogicalAnd())
		|| (W.maxRange()<1)
		|| (!W.amBeingWornProperly())
		|| (W.weaponClassification()!=Weapon.CLASS_POLEARM))
		{
			mob.tell(L("You can not do that with @x1.  Forceback requires a long two-handed polearm-class weapon."));
			return false;
		}

		if(mob.rangeToTarget() > W.maxRange()+1)
		{
			mob.tell(L("You are already out of range."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,
											L("^F^<FIGHT^><S-NAME> force(s) <T-NAME> back with @x1!^</FIGHT^>^?",W.name(mob)));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final CMMsg msg2=CMClass.getMsg(target,null,this,CMMsg.MSG_RETREAT,null);
				if(mob.location().okMessage(mob,msg2))
					mob.location().send(mob,msg2);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to force <T-NAME> back, but fail(s)."));

		// return whether it worked
		return success;
	}
}
