package com.planet_ink.coffee_mud.Abilities.Paladin;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Paladin_HealingHands extends StdAbility
{
	@Override
	public String ID()
	{
		return "Paladin_HealingHands";
	}

	private final static String localizedName = CMLib.lang().L("Healing Hands");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HANDS"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
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
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_HEALING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HEALINGMAGIC;
	}

	@Override
	protected long minCastWaitTime()
	{
		return CMProps.getTickMillis();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;

		if((!auto)&&(!(CMLib.flags().isGood(mob))))
		{
			mob.tell(L("Your alignment has alienated your god from you."));
			return false;
		}

		final int healing=1+((int)Math.round(CMath.div(adjustedLevel(mob,asLevel),4.0)));
		if(mob.curState().getMana()<healing)
		{
			mob.tell(L("You don't have enough mana to do that."));
			return false;
		}

		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		helpProficiency(mob, 0);

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_CAST_SOMANTIC_SPELL,auto?L("A pair of celestial hands surround <T-NAME>"):L("^S<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int manaLost=healing;
				if(manaLost>0)
					manaLost=manaLost*-1;
				mob.curState().adjMana(manaLost,mob.maxState());
				CMLib.combat().postHealing(mob,target,this,healing,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,null);
				target.tell(L("You feel a little better!"));
				lastCastHelp=System.currentTimeMillis();
			}
		}
		else
			return beneficialVisualFizzle(mob,mob,L("<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>, but <S-HIS-HER> god does not heed."));

		// return whether it worked
		return success;
	}

}
