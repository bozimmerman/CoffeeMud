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

public class Fighter_WeaponBreak extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_WeaponBreak";
	}

	private final static String localizedName = CMLib.lang().L("Weapon Break");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"BREAK"});
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
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
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

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			final MOB victim=mob.getVictim();
			if((!mob.isInCombat())||(victim==null))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.fetchWieldedItem()==null)
				return Ability.QUALITY_INDIFFERENT;
			final Item item=victim.fetchWieldedItem();
			if((item==null)
			||(!(item instanceof Weapon))
			||(((Weapon)item).weaponClassification()==Weapon.CLASS_NATURAL))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB victim=mob.getVictim();
		if((!mob.isInCombat())||(victim==null))
		{
			mob.tell(L("You must be in combat to do this!"));
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You are too far away to try that!"));
			return false;
		}
		if((!auto)&&(mob.fetchWieldedItem()==null))
		{
			mob.tell(L("You need a weapon to break someone elses!"));
			return false;
		}
		final Item item=victim.fetchWieldedItem();
		if((item==null)
		||(!(item instanceof Weapon))
		||(((Weapon)item).weaponClassification()==Weapon.CLASS_NATURAL))
		{
			mob.tell(L("@x1 is not wielding a weapon!",victim.charStats().HeShe()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=victim.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		final Item hisWeapon=victim.fetchWieldedItem();
		final int chance=(-levelDiff)+(-(victim.charStats().getStat(CharStats.STAT_DEXTERITY)*2));
		final boolean hit=(auto)||CMLib.combat().rollToHit(mob,victim);
		final boolean success=proficiencyCheck(mob,chance,auto)&&(hit);
		if((success)
		   &&(hisWeapon!=null)
		   &&(hisWeapon.phyStats().ability()==0)
		   &&(!CMLib.flags().isABonusItems(hisWeapon))
		&&((hisWeapon.rawProperLocationBitmap()==Wearable.WORN_WIELD)
		   ||(hisWeapon.rawProperLocationBitmap()==Wearable.WORN_WIELD+Wearable.WORN_HELD)))
		{
			final String str=auto?L("@x1 break(s) in <T-HIS-HER> hands!",hisWeapon.name()):L("<S-NAME> attack(s) <T-NAMESELF> and destroy(s) @x1!",hisWeapon.name());
			hisWeapon.unWear();
			final CMMsg msg=CMClass.getMsg(mob,victim,this,CMMsg.MSG_NOISYMOVEMENT,str);
			final CMMsg msg2=CMClass.getMsg(mob,hisWeapon,this,CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS|CMMsg.TYP_CAST_SPELL,null);
			if(mob.location().okMessage(mob,msg)&&mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if(msg2.value()<=0)
					hisWeapon.destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
		if(hisWeapon != null)
			return maliciousFizzle(mob,victim,L("<S-NAME> attempt(s) to destroy @x1 and fail(s)!",hisWeapon.name()));
		else
			return maliciousFizzle(mob,victim,L("<S-NAME> attempt(s) to destroy <T-YOUPOSS> non-existant weapon and fail(s)!"));
		return success;
	}

}
