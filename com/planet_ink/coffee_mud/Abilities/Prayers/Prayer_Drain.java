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

public class Prayer_Drain extends Prayer
{
	public String ID() { return "Prayer_Drain"; }
	public String name(){ return "Drain";}
	public String displayText(){ return "(Drain)";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_UNDEAD|(auto?CMMsg.MASK_GENERAL:0),null);
			FullMsg msg2=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> reach(es) at <T-NAMESELF>, "+prayingWord(mob)+"!^?");
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg2);
				mob.location().send(mob,msg);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					int damage = 0;
					int maxDie =  adjustedLevel(mob,asLevel);
					damage += Dice.roll(maxDie,6,20);
					MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,auto?"<T-NAME> shudder(s) in a draining magical wake.":"The draining grasp <DAMAGE> <T-NAME>.");
					if(mob!=target)
						MUDFight.postHealing(mob,mob,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,damage,null);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> reach(es) for <T-NAMESELF>, "+prayingWord(mob)+", but the spell fades.");


		// return whether it worked
		return success;
	}
}
