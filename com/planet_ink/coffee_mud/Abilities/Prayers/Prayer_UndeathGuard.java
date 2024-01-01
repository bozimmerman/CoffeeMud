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
   Copyright 2023-2024 Bo Zimmerman

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
public class Prayer_UndeathGuard extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_UndeathGuard";
	}

	private final static String	localizedName	= CMLib.lang().L("Undeath Guard");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Undeath Guard)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_HEALING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int modifyCastCode(final int castCode, final MOB mob, final Physical target, final boolean auto)
	{
		if((target instanceof MOB)
		&&(!CMLib.flags().isUndead((MOB)target)))
			return castCode|CMMsg.MASK_MALICIOUS;
		return castCode;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!CMLib.flags().isUndead((MOB)target))
					return Ability.QUALITY_MALICIOUS;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((affected instanceof MOB)&&(invoker()!=null))
		{
			final MOB mob=(MOB)affected;
			final MOB clericM=invoker();
			if((clericM!=null)
			&&(!clericM.amDead())
			&&(mob!=null)
			&&(!mob.amDead())
			&&((mob.curState().getHitPoints() < (int)Math.round(CMath.mul(mob.getWimpHitPoint(),1.30)))
				||(CMath.div(mob.curState().getHitPoints(), mob.maxState().getHitPoints())<=0.30))
			&&(mob.location()!=null)
			&&(mob.location().isHere(clericM)))
			{
				Ability A=clericM.fetchAbility("Prayer_Harm");
				if(A==null)
					A=clericM.fetchAbility("Prayer_CauseCritical");
				if(A==null)
					A=clericM.fetchAbility("Prayer_CauseSerious");
				if(A==null)
					A=clericM.fetchAbility("Prayer_CauseLight");
				if(A!=null)
				{
					final int[][] abilityUsageCache = mob.getAbilityUsageCache(A.ID());
					final int[] cache = abilityUsageCache[Ability.CACHEINDEX_LASTTIME];
					final int oldCount = (cache == null)? 0 : cache[USAGEINDEX_COUNT];
					A.invoke(clericM, new XVector<String>(mob.Name()), mob, false, 0);
					if(cache == null)
						abilityUsageCache[Ability.CACHEINDEX_LASTTIME]=null;
					else
						cache[USAGEINDEX_COUNT]=oldCount;
				}
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your undeath guard fades."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> become(s) protected by an undeath guard!"):L("^S<S-NAME> @x1 for a death guard over <T-NAMESELF>!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for an undeath guard over <T-NAMESELF>, but there is no answer.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
