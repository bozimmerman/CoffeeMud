package com.planet_ink.coffee_mud.Abilities.Prayers;

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
public class Prayer_MassHarm extends Prayer
{
	public String ID() { return "Prayer_MassHarm"; }
	public String name(){ return "Mass Harm";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null) return false;

		boolean success=profficiencyCheck(mob,0,auto);
		int numEnemies=h.size();
		for(Iterator e=h.iterator();e.hasNext();)
		{
			MOB target=(MOB)e.next();
			if(target!=mob)
			{
				if(success)
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"<T-NAME> become(s) surrounded by a dark cloud.":"^S<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, "+prayingWord(mob)+".^?");
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						int harming=Dice.roll(4,adjustedLevel(mob,asLevel)/numEnemies,numEnemies);
						MUDFight.postDamage(mob,target,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The unholy spell <DAMAGE> <T-NAME>!");
					}
				}
				else
					maliciousFizzle(mob,target,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, "+prayingWord(mob)+", but "+hisHerDiety(mob)+" does not heed.");
			}
		}


		// return whether it worked
		return success;
	}
}
