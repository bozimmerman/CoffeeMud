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

public class Prayer_Disenchant extends Prayer
{
	public String ID() { return "Prayer_Disenchant"; }
	public String name(){ return "Neutralize Item";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,5-((mob.envStats().level()-target.envStats().level())*5),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> appear(s) neutralized!":"^S<S-NAME> "+prayForWord(mob)+" to neutralize <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				target.baseEnvStats().setAbility(0);
				Vector affects=new Vector();
				for(int a=target.numEffects()-1;a>=0;a--)
				{
					Ability A=target.fetchEffect(a);
					if(A!=null)
						affects.addElement(A);
				}
				for(int a=0;a<affects.size();a++)
				{
					Ability A=(Ability)affects.elementAt(a);
					A.unInvoke();
					target.delEffect(A);
				}
				if(target instanceof Wand)
				{
					((Wand)target).setSpell(null);
					((Wand)target).setUsesRemaining(0);
				}
				else
				if(target instanceof SpellHolder)
					((SpellHolder)target).setSpellList("");
				target.recoverEnvStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to neutralize <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}