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
public class Prayer_RemovePoison extends Prayer
{
	public String ID() { return "Prayer_RemovePoison"; }
	public String name(){ return "Remove Poison";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public static Vector returnOffensiveAffects(Environmental fromMe)
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> feel(s) purified of <T-HIS-HER> poisons.":"^S<S-NAME> "+prayWord(mob)+" that <T-NAME> be purified of <T-HIS-HER> poisons.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
				if((target instanceof Drink)&&(((Drink)target).liquidHeld()==EnvResource.RESOURCE_POISON))
				{
					((Drink)target).setLiquidHeld(EnvResource.RESOURCE_FRESHWATER);
					target.baseEnvStats().setAbility(0);
				}
				if(!Sense.stillAffectedBy(target,offensiveAffects,false))
				{
					if(target instanceof MOB)
					{
						((MOB)target).tell("You feel much better!");
						((MOB)target).recoverCharStats();
						((MOB)target).recoverMaxState();
					}
				}
				target.recoverEnvStats();
			}
		}
		else
		if(!auto)
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" that <T-NAME> be purified of <T-HIS-HER> poisons, but there is no answer.");


		// return whether it worked
		return success;
	}
}
