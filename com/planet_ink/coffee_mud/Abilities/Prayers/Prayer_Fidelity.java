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

public class Prayer_Fidelity extends Prayer
{
	public String ID() { return "Prayer_Fidelity"; }
	public String name(){ return "Fidelity";}
	public String displayText(){return "(Fidelity)";}
	public int quality(){return Ability.OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your magical fidelity subsides.");
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if(!(affected instanceof MOB)) return true;

		MOB myChar=(MOB)affected;
		if((msg.target()!=null)&&(msg.target() instanceof MOB))
		{
			MOB mate=(MOB)msg.target();
			if((msg.amISource(myChar))
			&&(!myChar.getLiegeID().equals(mate.Name()))
			&&(myChar.isMarriedToLiege())
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>")))
			{
				myChar.tell("You fidelity geas prevents you from doing that.");
				return false;
			}
		}
		return true;
	}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> gain(s) the fidelity geas!");
				beneficialAffect(mob,target,asLevel,(Integer.MAX_VALUE/2));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}
