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
public class Spell_KnowAlignment extends Spell
{
	public String ID() { return "Spell_KnowAlignment"; }
	public String name(){return "Know Alignment";}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^SYou draw out <T-NAME>s disposition.^?",affectType(auto),auto?"":"^S<S-NAME> draw(s) out your disposition.^?",affectType(auto),auto?"":"^S<S-NAME> draws out <T-NAME>s disposition.^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
				mob.tell(mob,target,null,"<T-NAME> seem(s) like <T-HE-SHE> is "+CommonStrings.alignmentStr(target.getAlignment())+".");
			else
			{
				MOB newMOB=(MOB)CMClass.getMOB("StdMOB");
				newMOB.setAlignment(Dice.rollPercentage()*10);
				mob.tell(mob,target,null,"<T-NAME> seem(s) like <T-HE-SHE> is "+CommonStrings.alignmentStr(newMOB.getAlignment())+".");
			}
		}


		// return whether it worked
		return success;
	}
}
