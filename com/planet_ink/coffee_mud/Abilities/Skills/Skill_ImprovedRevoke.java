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
   Copyright 2024-2024 Bo Zimmerman

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
public class Skill_ImprovedRevoke extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ImprovedRevoke";
	}

	private final static String	localizedName	= CMLib.lang().L("Improved Revoke");

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
		return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_ROOMS | Ability.CAN_EXITS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "IMPROVEDREVOKE", "IREVOKE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_NEUTRALIZATION;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
	}

	@Override
	public boolean ignoreCompounding()
	{
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String what="ANY";
		String whomToRevoke="";
		boolean quietly = false;
		if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("quietly")))
		{
			commands.remove(commands.size()-1);
			quietly = true;
		}

		Physical target=null;
		if(givenTarget != null)
			target = givenTarget;
		else
		if(commands.size()>1)
		{
			what=commands.get(0);
			whomToRevoke=CMParms.combine(commands,1);
			if((whomToRevoke.length()==0)
			&&(mob.location().numEffects()>0))
				target=mob.location();
			else
			if(whomToRevoke.equalsIgnoreCase("room"))
				target=mob.location();
			else
			if(whomToRevoke.equalsIgnoreCase("area"))
				target=mob.location().getArea();
			else
			if(whomToRevoke.equalsIgnoreCase("self"))
				target=mob;
			else
			{
				final int dir=CMLib.directions().getGoodDirectionCode(whomToRevoke);
				if(dir>=0)
					target=mob.location().getExitInDir(dir);
				else
				{
					target=mob.location().fetchFromRoomFavorMOBs(null,whomToRevoke);
					if(target==null)
						target=mob.findItem(null,whomToRevoke);
				}
			}
		}
		else
			what=CMParms.combine(commands,0);

		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("Revoke what from whom?  You don't see '@x1' here.",whomToRevoke));
			return false;
		}
		final List<Ability> revokables = new ArrayList<Ability>();
		for(int a=0;a<target.numEffects();a++)
		{
			final Ability A=target.fetchEffect(a);
			if((A!=null)
			&&(A.invoker()==mob)
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT))
			&&(A.canBeUninvoked()))
				revokables.add(A);
		}
		final List<Environmental> revokeThese = new ArrayList<Environmental>();
		if(what.length()>0)
		{
			if(what.equalsIgnoreCase("any")&&(revokables.size()>0))
				revokeThese.add(revokables.get(CMLib.dice().roll(1, revokables.size(), -1)));
			if(revokeThese.size()==0)
				revokeThese.addAll(CMLib.english().fetchEnvironmentals(revokables, what, true));
			if(revokeThese.size()==0)
				revokeThese.addAll(CMLib.english().fetchEnvironmentals(revokables, what, false));
		}
		else
		if(revokables.size()>0)
			revokeThese.add(revokables.get(CMLib.dice().roll(1, revokables.size(), -1)));

		if(revokeThese.size()==0)
		{
			if(target instanceof Room)
				mob.tell(L("Revoke which magic from @x1?",target.name(mob)));
			else
				mob.tell(mob,target,null,L("<T-NAME> do(es) not appear to be affected by anything called '@x1'.",what));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,10 + (revokeThese.size()*-10),auto);
		final String abilityName;
		if(revokeThese.size()>1)
			abilityName=L("multiple effects");
		else
			abilityName=revokeThese.get(0).name();
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_HANDS,quietly?"":L("<S-NAME> revoke(s) @x1 from @x2",abilityName,target.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(final Environmental E : revokeThese)
					((Ability)E).unInvoke();
			}
		}
		else
			beneficialVisualFizzle(mob,target,quietly?"":
				L("<S-NAME> attempt(s) to revoke @x1 from @x2, but flub(s) it.",abilityName,target.name()));
		return success;
	}
}
