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
public class Spell_FaerieFog extends Spell
{
	public String ID() { return "Spell_FaerieFog"; }
	public String name(){return "Faerie Fog";}
	public String displayText(){return "(Faerie Fog)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode() {	return Ability.SPELL|Ability.DOMAIN_ILLUSION;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if(canBeUninvoked())
		{
			Room room=(Room)affected;
			room.showHappens(CMMsg.MSG_OK_VISUAL, "The faerie fog starts to clear out.");
		}
		super.unInvoke();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask() |  EnvStats.CAN_SEE_INVISIBLE);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Environmental target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
		    mob.tell(mob,null,null,"A faerie fog is already here.");
			return false;
		}


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto),(auto?"A ":"^S<S-NAME> speak(s) and gesture(s) and a ")+"sparkling fog envelopes the area.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> mutter(s) about a faerie fog, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
