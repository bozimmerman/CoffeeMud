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

public class Prayer_Anger extends Prayer
{
	public String ID() { return "Prayer_Anger"; }
	public String name(){ return "Anger";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		boolean someoneIsFighting=false;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB inhab=mob.location().fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
				someoneIsFighting=true;
		}

		if((success)&&(!someoneIsFighting)&&(mob.location().numInhabitants()>3))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"A feeling of anger descends":"^S<S-NAME> rage(s) for anger.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB inhab=mob.location().fetchInhabitant(i);
					if((inhab!=null)&&(inhab!=mob)&&(!inhab.isInCombat()))
					{
						int tries=0;
						MOB target=null;
						while((tries<100)&&(target==null))
						{
							target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
							if(target!=null)
							{
								if(target==inhab) target=null;
								if(target==mob) target=null;
							}
							tries++;
						}
						FullMsg amsg=new FullMsg(mob,inhab,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
						if((target!=null)&&(mob.location().okMessage(mob,amsg)))
						{
							inhab.tell("You feel angry.");
							inhab.setVictim(target);
						}
					}
				}
			}
		}
		else
			maliciousFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for rage, but nothing happens.");


		// return whether it worked
		return success;
	}
}
