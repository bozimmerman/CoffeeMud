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

public class Prayer_HolyWord extends Prayer implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Prayer_HolyWord";
	}

	private final static String localizedName = CMLib.lang().L("Holy Word");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Holy Word)");

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
		return Ability.ACODE_PRAYER|Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public boolean supportsMending(Physical item)
	{
		return (item instanceof MOB)
				&&((Prayer_Bless.getSomething((MOB)item,true)!=null)
					||(CMLib.flags().domainAffects(item,Ability.DOMAIN_CURSING).size()>0));
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
		if(CMLib.flags().isGood(mob))
		{
			affectableStats.setArmor(affectableStats.armor()-15-(6*xlvl));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20+(4*xlvl));
		}
		else
		if(CMLib.flags().isEvil(mob))
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> blinding holy aura fades."));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String str=(auto?"The holy word is spoken.":"^S<S-NAME> speak(s) the holy word"+ofDiety(mob)+" to <T-NAMESELF>.^?")+CMLib.protocol().msp("bless.wav",10);
		String missStr=L("<S-NAME> speak(s) the holy word@x1, but nothing happens.",ofDiety(mob));
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
			if(CMLib.flags().isEvil(target))
				affectType=affectType|CMMsg.MASK_MALICIOUS;

			if(success)
			{
				final CMMsg msg=CMClass.getMsg(mob,target,this,affectType,str);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						if(CMLib.flags().canBeHeardSpeakingBy(mob,target))
						{
							str=null;
							Item I=Prayer_Bless.getSomething(target,true);
							final HashSet<Item> alreadyDone=new HashSet<Item>();
							while((I!=null)&&(!alreadyDone.contains(I)))
							{
								alreadyDone.add(I);
								final CMMsg msg2=CMClass.getMsg(target,I,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,L("<S-NAME> release(s) <T-NAME>."));
								target.location().send(target,msg2);
								Prayer_Bless.endLowerCurses(I,CMLib.ableMapper().lowestQualifyingLevel(ID()));
								I.recoverPhyStats();
								I=Prayer_Bless.getSomething(target,true);
							}
							Prayer_Bless.endAllOtherBlessings(mob,target,CMLib.ableMapper().lowestQualifyingLevel(ID()));
							Prayer_Bless.endLowerCurses(target,CMLib.ableMapper().lowestQualifyingLevel(ID()));
							beneficialAffect(mob,target,asLevel,0);
							target.recoverPhyStats();
						}
						else
						if(CMath.bset(affectType,CMMsg.MASK_MALICIOUS))
							maliciousFizzle(mob,target,L("<T-NAME> did not hear the word!"));
						else
							beneficialWordsFizzle(mob,target,L("<T-NAME> did not hear the word!"));

					}
				}
			}
			else
			{
				if(CMath.bset(affectType,CMMsg.MASK_MALICIOUS))
					maliciousFizzle(mob,target,missStr);
				else
					beneficialWordsFizzle(mob,target,missStr);
				missStr=null;
				return false;
			}
		}

		// return whether it worked
		return success;
	}
}
