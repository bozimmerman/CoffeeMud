package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Skill_Disarm extends StdSkill
{
	public String ID() { return "Skill_Disarm"; }
	public String name(){ return "Disarm";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"DISARM"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	public int usageType(){return USAGE_MOVEMENT;}

	public int castingQuality(MOB mob, Environmental target)
	{
		if((mob!=null)&&(target!=null))
		{
			MOB victim=mob.getVictim();
			if(victim==null)
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.fetchWieldedItem()==null)
				return Ability.QUALITY_INDIFFERENT;
			Item hisWeapon=victim.fetchWieldedItem();
			if(hisWeapon==null) hisWeapon=victim.fetchFirstWornItem(Wearable.WORN_HELD);
			if((hisWeapon==null)
			||(!(hisWeapon instanceof Weapon))
			||((((Weapon)hisWeapon).weaponClassification()==Weapon.CLASS_NATURAL)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		MOB victim=super.getTarget(mob, commands, givenTarget);
		if(victim==null) return false;
		if(((victim==mob.getVictim())&&(mob.rangeToTarget()>0))
		||((victim.getVictim()==mob)&&(victim.rangeToTarget()>0)))
		{
			mob.tell("You are too far away to disarm!");
			return false;
		}
		if(mob.fetchWieldedItem()==null)
		{
			mob.tell("You need a weapon to disarm someone!");
			return false;
		}
		Item hisWeapon=victim.fetchWieldedItem();
		if(hisWeapon==null) hisWeapon=victim.fetchFirstWornItem(Wearable.WORN_HELD);
		if((hisWeapon==null)
		||(!(hisWeapon instanceof Weapon))
		||((((Weapon)hisWeapon).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			mob.tell(victim.charStats().HeShe()+" is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=victim.envStats().level()-(mob.envStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		boolean hit=(auto)||CMLib.combat().rollToHit(mob,victim);
		boolean success=proficiencyCheck(mob,-levelDiff,auto)&&(hit);
		if((success)
		   &&((hisWeapon.fitsOn(Wearable.WORN_WIELD))
			  ||hisWeapon.fitsOn(Wearable.WORN_WIELD|Wearable.WORN_HELD)))
		{
			if(mob.location().show(mob,victim,this,CMMsg.MSG_NOISYMOVEMENT,null))
			{
				CMMsg msg=CMClass.getMsg(victim,hisWeapon,null,CMMsg.MSG_DROP,null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(victim,msg);
					mob.location().show(mob,victim,CMMsg.MSG_NOISYMOVEMENT,auto?"<T-NAME> is disarmed!":"<S-NAME> disarm(s) <T-NAMESELF>!");
				}
			}
		}
		else
			maliciousFizzle(mob,victim,"<S-NAME> attempt(s) to disarm <T-NAMESELF> and fail(s)!");
		return success;
	}

}
