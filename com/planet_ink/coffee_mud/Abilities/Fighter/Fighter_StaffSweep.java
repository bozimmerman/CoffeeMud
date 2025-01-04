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
   Copyright 2024-2025 Bo Zimmerman

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
public class Fighter_StaffSweep extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_StaffSweep";
	}

	private final static String localizedName = CMLib.lang().L("Staff Sweep");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"SSWEEP", "STAFFSWEEP"});
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
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	private volatile int weaponRange = 0;

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(weaponRange);
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	protected static Weapon getSweeper(final MOB mob)
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
			final Item I = getSweeper(mob);
			if(I == null)
				return Ability.QUALITY_INDIFFERENT;
			final Set<MOB> h=properTargets(mob,target,false);
			if((h==null)||(h.size()<2))
				return Ability.QUALITY_INDIFFERENT;
			for(final MOB M : h)
			{
				if((M.rangeToTarget()<0)||(M.rangeToTarget()>I.maxRange()))
					h.remove(M);
			}
			if(h.size()<2)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You must be in combat to do a staff sweep!"));
			return false;
		}
		final Item w=getSweeper(mob);
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell(L("You need to be wielding a staff to do that."));
			return false;
		}
		final Weapon wp=(Weapon)w;
		weaponRange=Math.max(0,wp.maxRange());

		Set<MOB> h=properTargets(mob,givenTarget,false);
		if(h==null)
			h=new HashSet<MOB>();
		for(final MOB M : h)
		{
			if((M.rangeToTarget()<0)||(M.rangeToTarget()>weaponRange))
				h.remove(M);
		}

		if(h.size()==0)
		{
			mob.tell(L("There aren't enough enough targets in range!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,L("^F^<FIGHT^><S-NAME> sweep(s) low with @x1!^</FIGHT^>^?",w.name()));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				final Ability A = CMClass.getAbility("Skill_Trip");
				A.setProficiency(100);
				for (final Object element : h)
				{
					final MOB target=(MOB)element;
					if(CMLib.combat().postAttack(mob, target, w))
						A.invoke(mob, target, true, adjustedLevel(mob, asLevel));
				}
				mob.recoverPhyStats();
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> fail(s) to sweep with @x1.",w.name()));
		return success;
	}
}
