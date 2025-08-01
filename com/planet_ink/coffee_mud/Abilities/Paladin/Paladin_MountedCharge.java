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
   Copyright 2004-2025 Bo Zimmerman

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
public class Paladin_MountedCharge extends StdAbility
{
	@Override
	public String ID()
	{
		return "Paladin_MountedCharge";
	}

	private final static String localizedName = CMLib.lang().L("Mounted Charge");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"MOUNTEDCHARGE","MCHARGE"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
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
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int minRange()
	{
		return 1;
	}

	@Override
	public int maxRange()
	{
		return 99;
	}

	public boolean done=false;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK))
			done=true;
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(done)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		final int xlvl=adjustedLevel((MOB)affected,0);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(4*xlvl));
		affectableStats.setArmor(affectableStats.armor()+(4*xlvl));
		affectableStats.setDamage(affectableStats.damage()+xlvl);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if((mob.isInCombat())&&(mob.rangeToTarget()<=0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.riding()==null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final boolean notInCombat=!mob.isInCombat();
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((mob.isInCombat())
		&&(mob.rangeToTarget()<=0))
		{
			mob.tell(L("You can not charge while in melee!"));
			return false;
		}

		//if(!PaladinSkill.paladinAlignmentCheck(this, mob, false))
		//	return false;

		final Rideable mount=mob.riding();
		if(!CMLib.flags().isMobileMounted(mob))
		{
			mob.tell(L("You must be riding a mount to use this skill."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_ADVANCE,
					L("<S-NAME> @x1 hard at <T-NAMESELF>!",mount.rideString(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(mob.getVictim()==target)
				{
					msg.setSourceMessage(null);
					msg.setTargetMessage(null);
					msg.setOthersMessage(null);
					msg.setTool(null);
					for(int i=mob.rangeToTarget()-1;i>=0;i--)
					{
						if(mob.location().okMessage(mob, msg))
							mob.location().send(mob, msg);
					}
					if(mob.rangeToTarget()==0)
					{
						target.setRangeToTarget(0);
						beneficialAffect(mob,mob,asLevel,2);
						mob.recoverPhyStats();
						if(notInCombat)
						{
							done=true;
							CMLib.combat().postAttack(mob,target,mob.fetchWieldedItem());
						}
						else
							done=false;
						if(mob.getVictim()==null)
							mob.setVictim(null); // correct range
						if(target.getVictim()==null)
							target.setVictim(null); // correct range
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> @x1 at <T-NAMESELF>, but miss(es).",mount.rideString(mob)));

		// return whether it worked
		return success;
	}
}
