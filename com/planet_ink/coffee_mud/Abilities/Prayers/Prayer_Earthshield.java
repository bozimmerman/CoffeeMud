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

public class Prayer_Earthshield extends Prayer
{
	public String ID() { return "Prayer_Earthshield"; }
	public String name(){ return "Earthshield";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(In Earthshield)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		affectableStats.setArmor(affectableStats.armor()-(2*CMAble.qualifyingClassLevel((MOB)affected,this)));
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		affectableState.setMovement(affectableState.getMovement()/2);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> earth shield vanishes.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=beneficialAffect(mob,target,asLevel,0);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is covered by an Earth Shield!");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but flub(s) it.");


		// return whether it worked
		return success;
	}
}
