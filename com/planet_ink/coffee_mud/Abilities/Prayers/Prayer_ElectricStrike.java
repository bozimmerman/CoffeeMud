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

public class Prayer_ElectricStrike extends Prayer
{
	public String ID() { return "Prayer_ElectricStrike"; }
	public String name(){ return "Electric Strike";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			Prayer_ElectricStrike newOne=(Prayer_ElectricStrike)this.copyOf();
			FullMsg msg=new FullMsg(mob,target,newOne,affectType(auto),(auto?"<T-NAME> is filled with a holy charge!":"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+"!^?")+CommonStrings.msp("lightning.wav",40));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					int harming=Dice.roll(3,adjustedLevel(mob,asLevel),5);
					MUDFight.postDamage(mob,target,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_STRIKING,"^SThe ELECTRIC STRIKE <DAMAGE> <T-NAME>!^?");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
