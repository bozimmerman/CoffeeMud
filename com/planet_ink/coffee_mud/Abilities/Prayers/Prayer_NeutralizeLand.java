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
public class Prayer_NeutralizeLand extends Prayer
{
	public String ID() { return "Prayer_NeutralizeLand"; }
	public String name(){ return "Neutralize Land";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		Room target=mob.location();
		if((target!=null)&&(success))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) neutralized.":"^S<S-NAME> "+prayWord(mob)+", sweeping <S-HIS-HER> hands over <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability revokeThis=null;
				boolean foundSomethingAtLeast=false;
				for(int a=0;a<target.numEffects();a++)
				{
					Ability A=(Ability)target.fetchEffect(a);
					if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())
					&&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
					   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
					   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
					   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)))
					{
						foundSomethingAtLeast=true;
						if((A.invoker()!=null)&&((A.invoker().envStats().level()<=mob.envStats().level())))
							revokeThis=A;
					}
				}

				if(revokeThis==null)
				{
					if(foundSomethingAtLeast)
						mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"The magic on <T-NAME> appears too powerful to be nullified.");
					else
					if(auto)
						mob.tell(mob,target,null,"Nothing seems to be happening to <T-NAME>.");
				}
				else
				{
					revokeThis.unInvoke();
					target.delEffect(revokeThis);
					if(!revokeThis.canBeUninvoked())
						CMClass.DBEngine().DBUpdateRoom(mob.location());
				}
			}
			else
				beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but "+hisHerDiety(mob)+" does not heed.");
		}

		// return whether it worked
		return success;
	}
}
