package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2000-2006 Bo Zimmerman

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
	public String ID() { return "Fighter_WeaponBreak"; }
	public String name(){ return "Weapon Break";}
	private static final String[] triggerStrings = {"BREAK"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int maxRange(){return 1;}
	public int classificationCode(){ return Ability.ACODE_SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to try that!");
			return false;
		}
		if((!auto)&&(mob.fetchWieldedItem()==null))
		{
			mob.tell("You need a weapon to break someone elses!");
			return false;
		}
		if((mob.getVictim().fetchWieldedItem()==null)
		||(!(mob.getVictim().fetchWieldedItem() instanceof Weapon))
		||(((Weapon)mob.getVictim().fetchWieldedItem()).weaponClassification()==Weapon.CLASS_NATURAL))
		{
			mob.tell(mob.getVictim().charStats().HeShe()+" is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-adjustedLevel(mob,asLevel);
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		Item hisWeapon=mob.getVictim().fetchWieldedItem();
		int chance=(-levelDiff)+(-(mob.getVictim().charStats().getStat(CharStats.STAT_DEXTERITY)*2));
		boolean hit=(auto)||CMLib.combat().rollToHit(mob,mob.getVictim());
		boolean success=profficiencyCheck(mob,chance,auto)&&(hit);
		if((success)
		   &&(hisWeapon!=null)
		   &&(hisWeapon.envStats().ability()==0)
		   &&(!CMLib.flags().isABonusItems(hisWeapon))
		&&((hisWeapon.rawProperLocationBitmap()==Item.WORN_WIELD)
		   ||(hisWeapon.rawProperLocationBitmap()==Item.WORN_WIELD+Item.WORN_HELD)))
		{
			String str=auto?hisWeapon.name()+" break(s) in <T-HIS-HER> hands!":"<S-NAME> disarm(s) <T-NAMESELF> and destroy(s) "+hisWeapon.name()+"!";
			hisWeapon.unWear();
			CMMsg msg=CMClass.getMsg(mob,mob.getVictim(),this,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
			{
				hisWeapon.destroy();
				mob.location().send(mob,msg);
				mob.location().recoverRoomStats();
			}
		}
		else
			return maliciousFizzle(mob,mob.getVictim(),"<S-NAME> attempt(s) to destroy "+hisWeapon.name()+" and fail(s)!");
		return success;
	}

}
