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
   Copyright 2001-2018 Bo Zimmerman

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
public class Skill_Revoke extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Revoke";
	}

	private final static String	localizedName	= CMLib.lang().L("Revoke");

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

	private static final String[]	triggerStrings	= I(new String[] { "REVOKE" });

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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final String whatToRevoke=CMParms.combine(commands,0);

		Physical target=null;
		if((whatToRevoke.length()==0)
		&&(mob.location().numEffects()>0))
			target=mob.location();
		else
		if(whatToRevoke.equalsIgnoreCase("room"))
			target=mob.location();
		else
		if(whatToRevoke.equalsIgnoreCase("self"))
			target=mob;
		else
		{
			final int dir=CMLib.directions().getGoodDirectionCode(whatToRevoke);
			if(dir>=0)
				target=mob.location().getExitInDir(dir);
			else
			{
				target=mob.location().fetchFromRoomFavorMOBs(null,whatToRevoke);
				if(target==null)
					target=mob.findItem(null,whatToRevoke);
			}
		}

		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("Revoke from what?  You don't see '@x1' here.",whatToRevoke));
			return false;
		}

		Ability revokeThis=null;
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
				revokeThis=A;
		}

		if(revokeThis==null)
		{
			if(target instanceof Room)
				mob.tell(L("Revoke your magic from what?"));
			else
				mob.tell(mob,target,null,L("<T-NAME> do(es) not appear to be affected by anything you can revoke."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_HANDS,L("<S-NAME> revoke(s) @x1 from @x2",revokeThis.name(),target.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to revoke @x1 from @x2, but flub(s) it.",revokeThis.name(),target.name()));
		return success;
	}
}
