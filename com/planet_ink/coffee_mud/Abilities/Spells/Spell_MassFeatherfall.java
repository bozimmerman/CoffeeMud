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
public class Spell_MassFeatherfall extends Spell
{
	public String ID() { return "Spell_MassFeatherfall"; }
	public String name(){return "Mass FeatherFall";}
	public String displayText(){return "";}
	public int quality(){ return OK_OTHERS;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,false);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth floating.");
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
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) lightly.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					Spell_FeatherFall fall=new Spell_FeatherFall();
					fall.setProfficiency(profficiency());
					fall.beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) lightly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
