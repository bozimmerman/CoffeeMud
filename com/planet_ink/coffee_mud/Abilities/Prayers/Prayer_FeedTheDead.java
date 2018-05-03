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
   Copyright 2004-2018 Bo Zimmerman

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

public class Prayer_FeedTheDead extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_FeedTheDead";
	}

	private final static String	localizedName	= CMLib.lang().L("Feed The Dead");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_DEATHLORE;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY | Ability.FLAG_NOORDERING;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		int amount=100;
		if(!auto)
		{
			if((commands.size()==0)||(!CMath.isNumber(commands.get(commands.size()-1))))
			{
				mob.tell(L("Feed how much experience?"));
				return false;
			}
			amount=CMath.s_int(commands.get(commands.size()-1));
			if((amount<=0)||((amount>mob.getExperience())
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless()))
			{
				mob.tell(L("You cannot feed @x1 experience.",""+amount));
				return false;
			}
			commands.remove(commands.size()-1);
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(!CMLib.flags().isUndead(target))
		{
			mob.tell(L("Only the undead may be fed in this way."));
			return false;
		}
		if(!target.isMonster())
		{
			mob.tell(L("That creature cannot be fed."));
			return false;
		}
		if(mob.isMonster() && (!auto) && (givenTarget==null))
		{
			mob.tell(L("You cannot feed the dead."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"<T-NAME> gain(s) fake life!":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to be fed.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.leveler().postExperience(mob,null,null,-amount,false);
				if((mob.phyStats().level()>target.phyStats().level())&&(target.isMonster()))
				{
					int adjLevel = adjustedLevel(mob,asLevel);
					amount+=(adjustedLevel(mob,asLevel)-target.phyStats().level())
						  *(adjLevel/3);
				}
				CMLib.leveler().postExperience(target,null,null,amount,false);
				if((CMLib.dice().rollPercentage() < amount)
				&&(target.isMonster())
				&&(target.fetchEffect("Loyalty")==null)
				&&(target.amFollowing()==mob)
				&&(mob.playerStats()!=null)
				&&(!mob.isMonster())
				&&(target.fetchEffect("Prop_ModExperience")!=null))
				{
					Ability A=CMClass.getAbility("Loyalty");
					A.setMiscText("NAME="+mob.Name());
					A.setSavable(true);
					target.addNonUninvokableEffect(A);
					mob.tell(mob,target,null,L("<T-NAME> is now loyal to you."));
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for <T-NAMESELF> to be fed, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
