package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_MassSleep extends Spell
{
	public String ID() { return "Spell_MassSleep"; }
	public String name(){return "Mass Sleep";}
	public String displayText(){return "";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth putting to sleep.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,-20,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) and wave(s) <S-HIS-HER> arms.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// if they can't hear the sleep spell, it
				// won't happen
				if(Sense.canBeHeardBy(mob,target))
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					MOB oldVictim=mob.getVictim();
					FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
					if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
						{
							Spell_Sleep spell=new Spell_Sleep();
							spell.setProfficiency(profficiency());
							success=spell.maliciousAffect(mob,target,asLevel,2,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0));
							if(success)
								target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s) asleep!!");
						}
					}
					if(oldVictim==null) mob.setVictim(null);
				}
				else
					maliciousFizzle(mob,target,"<T-NAME> seem(s) unaffected by the Sleep spell from <S-NAME>.");
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> whisper(s) a sleeping spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
