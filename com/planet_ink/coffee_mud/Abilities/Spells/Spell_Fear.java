package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_Fear extends Spell
{
	public String ID() { return "Spell_Fear"; }
	public String name(){return "Fear";}
	public String displayText(){return "(Afraid)";}
	public int quality(){return MALICIOUS;};
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			if(!auto)
				mob.tell("There doesn't appear to be anyone here worth scaring.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> scare(s) <T-NAMESELF>.^?");
				FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
				if(((text().toUpperCase().indexOf("WEAK")<0)||((mob.envStats().level()/2)>target.envStats().level()))
				&&((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2)))))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						mob.location().send(mob,msg2);
						if(msg2.value()<=0)
						{
							invoker=mob;
							CommonMsgs.flee(target,"");
						}
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) a frightening spell, but completely flub(s) it.");


		// return whether it worked
		return success;
	}
}
