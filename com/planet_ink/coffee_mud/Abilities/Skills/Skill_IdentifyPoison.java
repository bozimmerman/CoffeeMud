package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_IdentifyPoison extends StdAbility
{
	public String ID() { return "Skill_IdentifyPoison"; }
	public String name(){ return "Identify Poison";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"IDPOISON","IDENTIFYPOISON"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A.classificationCode()==Ability.POISON))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Apothecary")==null)
		{
			teacher.tell(student.name()+" has not yet learned to be an apothecary.");
			student.tell("You need to learn apothecary before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&((offensiveAffects.size()>0)
					   ||((target instanceof Drink)&&(((Drink)target).liquidHeld()==EnvResource.RESOURCE_POISON))))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|(auto?CMMsg.MASK_GENERAL:0),auto?"":"^S<S-NAME> carefully sniff(s) and taste(s) <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer buf=new StringBuffer(target.name()+" contains: ");
				if(offensiveAffects.size()==0)
					buf.append("weak impurities, ");
				else
				for(int i=0;i<offensiveAffects.size();i++)
					buf.append(((Ability)offensiveAffects.elementAt(i)).name()+", ");
				mob.tell(buf.toString().substring(0,buf.length()-2));
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> sniff(s) and taste(s) <T-NAME>, but receives no insight.");


		// return whether it worked
		return success;
	}
}
