package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Skill_Disarm extends StdAbility
{
	public String ID() { return "Skill_Disarm"; }
	public String name(){ return "Disarm";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"DISARM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB victim=mob.getVictim();
		if(victim==null)
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
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
		if(hisWeapon==null) hisWeapon=victim.fetchFirstWornItem(Item.HELD);
		if((hisWeapon==null)
		||(!(hisWeapon instanceof Weapon))
		||((((Weapon)hisWeapon).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			mob.tell(victim.charStats().HeShe()+" is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=victim.envStats().level()-mob.envStats().level();
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		boolean hit=(auto)||(Dice.normalizeAndRollLess(mob.adjustedAttackBonus(victim)+victim.adjustedArmor()));
		boolean success=profficiencyCheck(mob,-levelDiff,auto)&&(hit);
		if((success)&&(hisWeapon!=null)
		   &&((hisWeapon.fitsOn(Item.WIELD))
			  ||hisWeapon.fitsOn(Item.WIELD|Item.HELD)))
		{
			if(mob.location().show(mob,victim,this,CMMsg.MSG_NOISYMOVEMENT,null))
			{
				FullMsg msg=new FullMsg(victim,hisWeapon,null,CMMsg.MSG_DROP,null);
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
