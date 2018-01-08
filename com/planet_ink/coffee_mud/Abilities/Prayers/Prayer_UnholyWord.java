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

public class Prayer_UnholyWord extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_UnholyWord";
	}

	private final static String localizedName = CMLib.lang().L("Unholy Word");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Unholy Word)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if(mob==invoker)
			return;

		final int xlvl=super.getXLEVELLevel(invoker());
		if(CMLib.flags().isEvil(mob))
		{
			affectableStats.setArmor(affectableStats.armor()-15-(6*xlvl));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20+(4*xlvl));
		}
		else
		if(CMLib.flags().isGood(mob))
		{
			affectableStats.setArmor(affectableStats.armor()+15+(6*xlvl));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-20-(4*xlvl));
		}
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
			mob.tell(L("The unholy word has been spoken."));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final String str=auto?L("The unholy word is spoken."):L("^S<S-NAME> speak(s) the unholy word@x1 to <T-NAMESELF>.^?",ofDiety(mob));

		final Room room=mob.location();
		if(room!=null)
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB target=room.fetchInhabitant(i);
			if(target==null)
				break;
			int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
			if(auto)
				affectType=affectType|CMMsg.MASK_ALWAYS;
			if(CMLib.flags().isGood(target))
				affectType=affectType|CMMsg.MASK_MALICIOUS;

			if(success)
			{
				final CMMsg msg=CMClass.getMsg(mob,target,this,affectType,str);
				if(room.okMessage(mob,msg))
				{
					room.send(mob,msg);
					if(msg.value()<=0)
					{
						if(CMLib.flags().canBeHeardSpeakingBy(mob,target))
						{
							final Item I=Prayer_Curse.getSomething(mob,true);
							if(I!=null)
							{
								Prayer_Curse.endLowerBlessings(I,CMLib.ableMapper().lowestQualifyingLevel(ID()));
								I.recoverPhyStats();
							}
							Prayer_Curse.endLowerBlessings(target,CMLib.ableMapper().lowestQualifyingLevel(ID()));
							beneficialAffect(mob,target,asLevel,0);
							target.recoverPhyStats();
						}
						else
						if(CMath.bset(affectType,CMMsg.MASK_MALICIOUS))
							maliciousFizzle(mob,target,L("<T-NAME> did not hear the unholy word!"));
						else
							beneficialWordsFizzle(mob,target,L("<T-NAME> did not hear the unholy word!"));
					}
				}
			}
			else
			{
				if(CMath.bset(affectType,CMMsg.MASK_MALICIOUS))
					maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to speak the unholy word to <T-NAMESELF>, but flub(s) it."));
				else
					beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to speak the unholy word to <T-NAMESELF>, but flub(s) it."));
				return false;
			}
		}

		// return whether it worked
		return success;
	}
}
