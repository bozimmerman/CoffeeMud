package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_SenseAge extends Chant
{
	public String ID() { return "Chant_SenseAge"; }
	public String name(){ return "Sense Age";}
	protected int canAffectCode(){return 0;}
	public int quality(){return Ability.OK_OTHERS;}
	protected int manaOverride(){return 5;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) over <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect("Age");
				if((!(target instanceof MOB))&&(A==null))
				{
					mob.tell("You have no way to determining the age of "+target.name()+".");
					success=false;
				}
				else
				if((A==null)||(A.displayText().length()==0))
				{
					MOB M=(MOB)target;
					if(M.baseCharStats().getStat(CharStats.AGE)<=0)
						mob.tell("You can't determine how old "+target.name()+" is with this magic.");
					else
						mob.tell(target.name()+" is "+Util.startWithAorAn(M.baseCharStats().ageName().toLowerCase())+" "+M.baseCharStats().raceName()+", aged "+M.baseCharStats().getStat(CharStats.AGE)+" years.");
				}
				else
				{
					String s=A.displayText();
					if(s.startsWith("(")) s=s.substring(1);
					if(s.endsWith(")")) s=s.substring(0,s.length()-1);
					mob.tell(target.name()+" is "+s+".");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) over <T-NAMESELF>, but the magic fades.");

		// return whether it worked
		return success;
	}
}
