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
public class Spell_FaerieFire extends Spell
{
	public String ID() { return "Spell_FaerieFire"; }
	public String name(){return "Faerie Fire";}
	public String displayText(){return "(Faerie Fire)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, "The faerie fire around <S-NAME> fades.");
		super.unInvoke();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);

		if((affectableStats.disposition()&EnvStats.IS_INVISIBLE)==EnvStats.IS_INVISIBLE)
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GLOWING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
		affectableStats.setArmor(affectableStats.armor()+10);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		MOB target = getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto),(auto?"A ":"^S<S-NAME> speak(s) and gesture(s) and a ")+"twinkling fire envelopes <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> mutter(s) about a faerie fire, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
