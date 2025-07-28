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
   Copyright 2025-2025 Bo Zimmerman

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
public class Fighter_Pistolwhip extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Pistolwhip";
	}

	private final static String	localizedName	= CMLib.lang().L("Pistolwhip");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PISTOLWHIP" });

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
		return Ability.ACODE_SKILL | Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	public static final boolean isPistolWeapon(final Environmental E)
	{
		if(!(E instanceof AmmunitionWeapon))
			return false;
		final AmmunitionWeapon W = (AmmunitionWeapon)E;
		if((W.weaponClassification()!=Weapon.CLASS_RANGED)
		||(!W.requiresAmmunition())
		||(W.ammunitionCapacity()==0)
		||((!W.ammunitionType().toLowerCase().startsWith("bullet"))
			&&(!(W.ammunitionType().toLowerCase().startsWith("bolt")))
			&&(!(W instanceof Technical))))
				return false;
		return true;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			final Item weapon = mob.fetchWieldedItem();
			if(!isPistolWeapon(weapon))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0)&&(!auto))
		{
			mob.tell(L("You are too far away to bowwhip!"));
			return false;
		}
		final Item weapon = mob.fetchWieldedItem();
		if(!isPistolWeapon(weapon))
		{
			mob.tell(L("Pistolwhip requires a crossbow or similar weapon."));
			return false;
		}
		final AmmunitionWeapon W = (AmmunitionWeapon)weapon;
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			str=auto?L("<T-NAME> is pistolwhipped!"):L("^F^<FIGHT^><S-NAME> attempt(s) to bash <T-NAMESELF> with the butt of @x1!^</FIGHT^>^?",W.name());
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),str);
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()>0)
					return maliciousFizzle(mob,target,L("<T-NAME> fight(s) off <S-YOUPOSS> pistolwhip."));
				final Weapon w=CMClass.getWeapon("ShieldWeapon");
				if(w!=null)
				{
					w.setName(L("the butt of @x1",W.name()));
					w.setDisplayText(W.displayText());
					w.setDescription(W.description());
					w.basePhyStats().setAbility(W.basePhyStats().ability());
					w.basePhyStats().setDamage(W.basePhyStats().damage()+getXLEVELLevel(mob));
					w.recoverPhyStats();
					CMLib.combat().postAttack(mob,target,w);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to pistolwhip <T-NAMESELF>, but end(s) up looking silly."));

		// return whether it worked
		return success;
	}
}
