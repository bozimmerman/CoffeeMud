package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2003-2020 Bo Zimmerman

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
public class Spell_Grow extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Grow";
	}

	private final static String localizedName = CMLib.lang().L("Grow");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Grow)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	protected int getOldWeight()
	{
		if(!CMath.isInteger(text()))
		{
			if(affected!=null)
				super.setMiscText(Integer.toString(affected.basePhyStats().weight()));
			return 0;
		}
		return CMath.s_int(text());
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final MOB invoker=invoker() == null ? (MOB)affected : invoker();
			final double aff=1.0 + CMath.mul(0.1,(invoker.phyStats().level()+(2*getXLEVELLevel(invoker))));
			affectableStats.setHeight((int)Math.round(CMath.mul(affectableStats.height(),aff)));
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		final MOB invoker=invoker() == null ? (MOB)affected : invoker();
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)/2);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+((invoker.phyStats().level()+(2*getXLEVELLevel(invoker)))/5));
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(super.canBeUninvoked()))
		{
			final MOB mob=(MOB)affected;
			if(getOldWeight()<1)
				mob.baseCharStats().getMyRace().setHeightWeight(mob.basePhyStats(),(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER));
			else
				mob.basePhyStats().setWeight(getOldWeight());
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> shrink(s) back down to size."));
			CMLib.utensils().confirmWearability(mob);
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("@x1 is already HUGE!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final boolean maliciousFlag =
				target.isMonster()
				&& (!mob.getGroupMembers(new HashSet<MOB>()).contains(target))
				&& (target.fetchEffect("Spell_Shrink")==null)
				&& (target.fetchWornItems(Long.MIN_VALUE, (short)-2048, (short)0).size()>0);
		if(success)
		{
			final int malicious = maliciousFlag ? CMMsg.MASK_MALICIOUS : 0;
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto)|malicious,
					auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect("Spell_Shrink");
				if((A!=null)&&(A.canBeUninvoked()))
					A.unInvoke();
				else
				{
					double aff=1.0 + CMath.mul(0.1,(target.phyStats().level()));
					aff=aff*aff;
					beneficialAffect(mob,target,asLevel,0);

					A=target.fetchEffect(ID());
					if(A!=null)
					{
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> grow(s) to an enormous size!"));
						setMiscText(Integer.toString(target.basePhyStats().weight()));
						A.setMiscText(Integer.toString(target.basePhyStats().weight()));
						long newWeight=Math.round(CMath.mul(target.basePhyStats().weight(),aff));
						if(newWeight>Short.MAX_VALUE)
							newWeight=Short.MAX_VALUE;
						target.basePhyStats().setWeight((int)newWeight);
						CMLib.utensils().confirmWearability(target);
					}
				}
			}
		}
		else
		if(maliciousFlag)
			maliciousFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting but nothing happens."));
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting but nothing happens."));

		// return whether it worked
		return success;
	}
}
