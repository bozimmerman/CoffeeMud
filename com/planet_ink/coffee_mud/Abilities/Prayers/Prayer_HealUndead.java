package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Prayer_HealUndead extends Prayer
{
	public String ID() { return "Prayer_HealUndead"; }
	public String name(){ return "Heal Undead";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_HEALING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		boolean undead=target.charStats().getMyRace().racialCategory().equals("Undead");
		if((!undead)&&(!auto))
		{
			mob.tell("Only the undead are affected by this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,(undead?0:CMMsg.MASK_MALICIOUS)|affectType(auto),auto?"<T-NAME> become(s) surrounded by a white light.":"^S<S-NAME> "+prayWord(mob)+" for negative healing power into <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int healing=Dice.roll(5,adjustedLevel(mob,asLevel),10);
				if(undead)
				{
					target.curState().adjHitPoints(healing,target.maxState());
					target.tell("You feel tons better!");
				}
				else
					MUDFight.postDamage(mob,target,this,healing,
											CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,
											Weapon.TYPE_BURNING,
											"The unholy spell <DAMAGE> <T-NAME>!");

			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
