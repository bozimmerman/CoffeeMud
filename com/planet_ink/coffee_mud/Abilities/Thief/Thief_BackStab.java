package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_BackStab extends ThiefSkill
{
	public String ID() { return "Thief_BackStab"; }
	public String name(){ return "Back Stab";}
	public String displayText(){return "(Backstabbing)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"BACKSTAB","BS"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		int factor=(int)Math.round(Util.div(adjustedLevel((MOB)affected,0),2.0))+2;
		affectableStats.setDamage(affectableStats.damage()*factor);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+100);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Backstab whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(Sense.canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely to do that.");
			return false;
		}

		CommonMsgs.draw(mob,false,true);

		Item I=mob.fetchWieldedItem();
		Weapon weapon=null;
		if((I!=null)&&(I instanceof Weapon))
			weapon=(Weapon)I;
		if(weapon==null)
		{
			mob.tell(mob,target,null,"Backstab <T-HIM-HER> with what? You need to wield a weapon!");
			return false;
		}
		if((weapon.weaponClassification()==Weapon.CLASS_BLUNT)
		||(weapon.weaponClassification()==Weapon.CLASS_HAMMER)
		||(weapon.weaponClassification()==Weapon.CLASS_FLAILED)
		||(weapon.weaponClassification()==Weapon.CLASS_RANGED)
		||(weapon.weaponClassification()==Weapon.CLASS_THROWN)
		||(weapon.weaponClassification()==Weapon.CLASS_STAFF))
		{
			mob.tell(mob,target,weapon,"You cannot stab anyone with <O-NAME>.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,target,this,(auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT),auto?"":"<S-NAME> attempt(s) to stab <T-NAMESELF> in the back!");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if((!success)&&(Sense.canBeSeenBy(mob,target))&&(!Sense.isSleeping(target)))
				mob.location().show(target,mob,CMMsg.MSG_OK_VISUAL,auto?"":"<S-NAME> spot(s) <T-NAME>!");
			else
			{
				mob.addEffect(this);
				mob.recoverEnvStats();
			}
			MUDFight.postAttack(mob,target,weapon);
			mob.delEffect(this);
			mob.recoverEnvStats();
		}
		else
			success=false;
		return success;
	}

}
