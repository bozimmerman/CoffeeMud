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
public class Prayer_Maladiction extends Prayer
{
	public String ID() { return "Prayer_Maladiction"; }
	public String name(){ return "Maladiction";}
	public String displayText(){ return "(Maladiction)";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_CURSE;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your maladiction fades.");
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.source()==affected))
		{
			if(msg.value()<0)
				msg.setValue(msg.value()*2);
			else
				msg.setValue(msg.value()/2);
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) filled with maladiction!":"^S<S-NAME> "+prayWord(mob)+" for a maladiction over <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					maliciousAffect(mob,target,asLevel,(int)CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY),-1);
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a maladiction over <T-YOUPOSS>, but there is no answer.");


		// return whether it worked
		return success;
	}
}
