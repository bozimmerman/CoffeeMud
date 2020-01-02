package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Skill_Lobotomizing extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Lobotomizing";
	}

	private final static String localizedName = CMLib.lang().L("Lobotomizing");

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
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"LOBOTOMIZE","LOBOTOMIZING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_TORTURING;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("Not while in combat!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(CMLib.flags().isSitting(mob))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}
		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;

		if((!auto)
		&&(CMLib.flags().isSleeping(target)))
			CMLib.commands().postStand(target, true, true);

		if((!auto)
		&&(!CMLib.flags().isBoundOrHeld(target))
		&&(!CMLib.flags().isSleeping(target))
		&&(!target.willFollowOrdersOf(mob)))
		{
			mob.tell(L("@x1 must be prone or bound or a follower to lobotomize them.",target.name(mob)));
			return false;
		}

		if((!auto)
		&&(!CMLib.flags().isSitting(target))
		&&(!CMLib.flags().isSleeping(target))
		&&((target.riding()==null)
			||(target.riding().rideBasis()!=Rideable.RIDEABLE_SLEEP)))
		{
			mob.tell(L("@x1 must be sitting down or lying on something.",target.name(mob)));
			return false;
		}

		for(int i=0;i<target.numItems();i++)
		{
			final Item I=target.getItem(i);
			if((I!=null)
			&&(I.amWearingAt(Wearable.WORN_HEAD)))
			{
				mob.tell(L("@x1 must be remove items worn on the head first.",target.name(mob)));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("^S<S-NAME> carefully lobotomize(s) <T-NAME> by sticking probes up <T-HIS-HER> nose.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final List<Ability> mindAs=CMLib.flags().flaggedAffects(target, Ability.FLAG_MINDALTERING);
				for(final Ability A : mindAs)
				{
					if(!A.isAutoInvoked())
					{
						A.unInvoke();
						target.delEffect(A);
					}
				}
				final List<Behavior> aggroBs=CMLib.flags().flaggedBehaviors(target, Behavior.FLAG_POTENTIALLYAGGRESSIVE);
				for(final Behavior B : aggroBs)
					target.delBehavior(B);
				final List<Behavior> aggroB2s=CMLib.flags().flaggedBehaviors(target, Behavior.FLAG_TROUBLEMAKING);
				for(final Behavior B : aggroB2s)
					target.delBehavior(B);
				final List<Ability> allAbles = new ArrayList<Ability>();
				for(final Enumeration<Ability> a=target.abilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if(A.proficiency() > 25)
						allAbles.add(A);
				}
				for(final Ability A : allAbles)
				{
					if(A.proficiency() > 25)
					{
						double newProf = (A.proficiency() - 25);
						newProf -= (newProf / 10.0);
						A.setProficiency((int)Math.round(newProf));
						final Ability eA=target.fetchEffect(A.ID());
						if(eA != null)
							eA.setProficiency((int)Math.round(newProf));
					}
				}
				if(allAbles.size()>0)
				{
					final Ability A=allAbles.get(CMLib.dice().roll(1, allAbles.size(), -1));
					if(A!=null)
					{
						A.setProficiency(0);
						final Ability eA=target.fetchEffect(A.ID());
						if(eA != null)
							eA.setProficiency(0);
					}
				}
				final Faction F=CMLib.factions().getFaction(CMLib.factions().getAlignmentID());
				if(F != null)
				{
					int fac = target.fetchFaction(CMLib.factions().getAlignmentID());
					if((fac > 0) && (fac < F.maximum()))
						fac += 1000;
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> attempt(s) to lobotomize <T-NAME>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
