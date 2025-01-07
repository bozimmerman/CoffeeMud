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
public class Fighter_StaffSpin extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_StaffSpin";
	}

	private final static String localizedName = CMLib.lang().L("Staff Spin");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(dazzled by a staff)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[] triggerStrings =I(new String[] {"STAFFSPIN"});
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
		return Ability.CAN_MOBS;
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

	int armorHit = -1;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(affected instanceof MOB)
		{
			if(armorHit < 0)
			{
				if(invoker()==null)
					armorHit=10;
				else
				{
					final Item I=invoker().fetchWieldedItem();
					if(I==null)
						armorHit=10;
					else
						armorHit = (int)Math.round(CMath.mul(I.phyStats().damage(),1+CMath.div(super.getXLEVELLevel(invoker()),10.0)));
				}
			}
			if(CMLib.flags().canBeSeenBy(invoker(), (MOB)affected))
				affectableStats.setArmor(affectableStats.armor()+armorHit);
		}
		super.affectPhyStats(affected,affectableStats);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!mob.isInCombat()||(mob.getVictim()!=target))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
			final Item I = getStaff(mob);
			if(I == null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You must be in combat to do a staff spin!"));
			return false;
		}
		final MOB target=mob.getVictim();
		if(target==null)
			return false;

		final Item w=getStaff(mob);
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell(L("You need to be wielding a staff to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),
					L(auto?"":"^F<S-NAME> elaborately spin(s) @x1 for <T-NAME>!^?",w.name(mob)));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final int duration = 2+(super.getXLEVELLevel(mob)/3);
					final Fighter_StaffSpin spinA = (Fighter_StaffSpin)maliciousAffect(mob,target,asLevel,duration,-1);
					spinA.armorHit = (int)Math.round(CMath.mul(w.phyStats().damage(),1+CMath.div(super.getXLEVELLevel(mob),10.0)));
					success=(spinA!=null);
					target.recoverPhyStats();
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to spin @x1 for <T-NAMESELF>, but flub(s) it."));

		// return whether it worked
		return success;
	}
}
