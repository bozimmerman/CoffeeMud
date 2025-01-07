package com.planet_ink.coffee_mud.Abilities.Fighter;
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
public class Fighter_StaffThrust extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_StaffThrust";
	}

	private final static String localizedName = CMLib.lang().L("Staff Thrust");

	@Override
	public String name()
	{
		return localizedName;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings =I(new String[] {"STAFFTHRUST","STHRUST"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected int damageType = Weapon.TYPE_BASHING;
	protected Weapon staffI = null;
	protected int maxAttacks = 0;

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(staffI != null)
		&&(msg.tool()==staffI))
		{
			if((--maxAttacks<=0)&&(canBeUninvoked()))
				unInvoke();
		}
	}

	@Override
	public void unInvoke()
	{
		if((staffI != null)&&(canBeUninvoked()))
			staffI.setWeaponDamageType(damageType);
		super.unInvoke();
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if(P instanceof MOB)
		{
			staffI = getStaff((MOB)P);
			if(staffI != null)
			{
				damageType = staffI.weaponDamageType();
				staffI.setWeaponDamageType(Weapon.TYPE_PIERCING);
			}
			maxAttacks = super.getXLEVELLevel((MOB)P);
			if(maxAttacks<=0)
				maxAttacks=1;
		}
	}

	protected static Weapon getStaff(final MOB mob)
	{
		final Item I = mob.fetchWieldedItem();
		if((I instanceof Weapon)
		&&(((Weapon)I).weaponClassification()==Weapon.CLASS_STAFF))
			return (Weapon)I;
		return null;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			final Item staffI=getStaff(mob);
			if(staffI==null)
				return Ability.QUALITY_INDIFFERENT;
			if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Weapon staffI=getStaff(mob);
		if(staffI==null)
		{
			mob.tell(L("You must wielding a staff to thrust with it."));
			return false;
		}
		final MOB target = mob.getVictim();
		if((!mob.isInCombat())||(target==null))
		{
			mob.tell(L("You must be in combat to do a staff thrust."));
			return false;
		}
		if(mob.fetchEffect(ID())!=null)
		{
			mob.tell(L("You are already staff thrusting!"));
			return false;
		}

		if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
		{
			mob.tell(L("@x1 must stand up first!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
			CMLib.combat().postWeaponAttackResult(mob, target, staffI, 0, false);
		else
		{
			final int damageType = staffI.weaponDamageType();
			try
			{
				staffI.setWeaponDamageType(Weapon.TYPE_PIERCING);
				success = CMLib.combat().postAttack(mob, target, staffI) || success;
			}
			finally
			{
				staffI.setWeaponDamageType(damageType);
				if(super.getXLEVELLevel(mob)>0)
					beneficialAffect(mob,mob,asLevel,2);
			}
		}
		return success;
	}

}
