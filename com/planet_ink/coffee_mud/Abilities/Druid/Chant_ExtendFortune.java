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

import java.util.*;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class Chant_ExtendFortune extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_ExtendFortune";
	}

	private final static String localizedName = CMLib.lang().L("Extend Fortune");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Extended Fortune)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			mob.tell(L("Your extended fortune fades."));
			final Ability A = mob.fetchEffect("AutoAwards");
			if(A != null)
			{
				A.setStat("HOLDER", ""); // autoawards has a backup to this, so no worries
				A.setStat("FORCETICK", "true");
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!auto)
		&&(!mob.getGroupMembers(new XTreeSet<MOB>()).contains(target))
		&&(!mob.mayIFight(target))
		&&(mob!=target))
		{
			mob.tell(mob,target,null,L("<T-HE-SHE> <T-IS-ARE> not a valid target."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) extensively to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> feel(s) more fortunate!"));
					final int ticks=super.getBeneficialTickdownTime(mob, target, 0, asLevel);
					final Ability meA = beneficialAffect(mob,target,asLevel,ticks*10);
					if(meA != null)
					{
						if(!target.isPlayer())
							CMLib.awards().giveAutoProperties(target, false);
						final Ability A = target.fetchEffect("AutoAwards");
						if(A != null)
							A.setStat("HOLDER", meA.ID());
						target.recoverPhyStats();
						target.recoverCharStats();
						target.recoverMaxState();
					}
				}
			}
		}
		else
		if((mob!=target)&&(!mob.getGroupMembers(new XTreeSet<MOB>()).contains(target))&&(mob.mayIFight(target)))
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));
		// return whether it worked
		return success;
	}
}
