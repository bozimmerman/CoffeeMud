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
public class Prayer_MassBlindness extends Prayer
{
	public String ID() { return "Prayer_MassBlindness"; }
	public String name(){ return "Mass Blindness";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Blindness)";}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(Sense.canSee(mob)))
			mob.tell("Your vision returns.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null) return false;

		boolean success=profficiencyCheck(mob,0,auto);
		boolean nothingDone=true;
		if(success)
		{
			for(Iterator e=h.iterator();e.hasNext();)
			{
				MOB target=(MOB)e.next();
				if(auto||(target.charStats().getBodyPart(Race.BODY_EYE)>0))
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> "+prayForWord(mob)+" an unholy blindness upon <T-NAMESELF>.^?");
					if((target!=mob)&&(mob.location().okMessage(mob,msg)))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
						{
							success=maliciousAffect(mob,target,0,-1);
							mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> go(es) blind!");
						}
						nothingDone=false;
					}
				}
			}
		}

		if(nothingDone)
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to blind everyone, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
