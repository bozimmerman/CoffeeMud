package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Paladin_HolyStrike extends StdAbility
{
	@Override
	public String ID()
	{
		return "Paladin_HolyStrike";
	}

	private final static String	localizedName	= CMLib.lang().L("Holy Strike");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "HOLYSTRIKE" });

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String displayText()
	{
		return "";
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
		return Ability.ACODE_SKILL | Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setArmor(affectableStats.armor()-200);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);
		final MOB mob=(MOB)affected;
		if(msg.amISource(invoker)
		&&(msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==invoker.fetchWieldedItem())
		&&(msg.value()>0))
		{
			if(mob.location()!=null)
				mob.location().show(msg.source(),msg.target(),CMMsg.MSG_OK_VISUAL, L("A blinding holy light from <O-NAME> comes down upon <T-NAME>!"));
			msg.setValue((msg.value()*2)+super.getXLEVELLevel(invoker));
			unInvoke();
		}
		return super.okMessage(myHost,msg);
	}

	protected boolean prereqs(MOB mob, boolean quiet)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			if(!quiet)
			mob.tell(L("You are too far away to do a holy strike!"));
			return false;
		}
		if(!(CMLib.flags().isGood(mob)))
		{
			mob.tell(L("Your alignment has alienated you from your god."));
			return false;
		}

		final Item w=mob.fetchWieldedItem();
		if((w==null)||(!(w instanceof Weapon))||(((Weapon)w).weaponClassification()!=Weapon.CLASS_SWORD))
		{
			if(!quiet)
				mob.tell(L("You need a sword to perform a holy strike!"));
			return false;
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!prereqs(mob,true))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isEvil(target))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!prereqs(mob,false))
			return false;

		MOB target=super.getTarget(mob, commands, givenTarget);
		if(target == null)
			return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("@x1 already has a holy strike charged against @x2.",target.name(mob),target.charStats().himher()));
			return false;
		}
		if(!CMLib.flags().isEvil(target))
		{
			mob.tell(L("But @x1 is not evil!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,L("^F^<FIGHT^><S-NAME> call(s) down a holy strike against <T-NAME>!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Ability A=maliciousAffect(mob,target,asLevel,2,-1);
				target.recoverPhyStats();
				CMLib.combat().postAttack(mob, target, mob.fetchWieldedItem());
				if(A!=null)
					A.unInvoke();
				target.recoverPhyStats();
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> call(s) a holy strike against , but fail(s) <S-HIS-HER> attack."));

		// return whether it worked
		return success;
	}
}
