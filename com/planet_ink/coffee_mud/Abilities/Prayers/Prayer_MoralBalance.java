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
public class Prayer_MoralBalance extends Prayer
{
	public String ID() { return "Prayer_MoralBalance"; }
	public String name(){ return "Moral Balance";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY | Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		FullMsg msg2=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,"<T-NAME> does not seem to like <S-NAME> messing with <T-HIS-HER> head.");

		if((success)&&(Factions.getFaction(Factions.AlignID())!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"<T-NAME> feel(s) completely different about the world.":"^S<S-NAME> "+prayWord(mob)+" to bring balance to <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					target.tell("Your views on the world suddenly change.");
                    Faction F=Factions.getFaction(Factions.AlignID());
                    if(F!=null)
	                    target.addFaction(Factions.AlignID(),(int)Math.round(Util.div((F.maximum-F.minimum)-target.fetchFaction(Factions.AlignID()),2)));

					if(!target.isInCombat() && target.isMonster())
					{
					   if(mob.location().okMessage(mob,msg2))
					   {
					      mob.location().send(mob,msg2);
					   }
					}
				}
			}
		}
		else
		{
			if(!target.isInCombat() && target.isMonster())
			{
			   if(mob.location().okMessage(mob,msg2))
			   {
			      mob.location().send(mob,msg2);
			   }
			}
			return beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");
		}


		// return whether it worked
		return success;
	}
}
