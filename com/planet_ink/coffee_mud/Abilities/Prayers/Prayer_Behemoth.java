package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_Behemoth extends Prayer
{
	public String ID() { return "Prayer_Behemoth"; }
	public String name(){ return "Behemoth";}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){return "(Behemoth)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your behemoth size has subsided.");
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.STRENGTH,affectedStats.getStat(CharStats.STRENGTH)+5);
		affectedStats.setStat(CharStats.DEXTERITY,affectedStats.getStat(CharStats.DEXTERITY)-5);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		affectedStats.setWeight(affectedStats.weight()*4);
		affectedStats.setHeight(affectedStats.height()*3);
		affectedStats.setName("A BEHEMOTH "+affected.name().toUpperCase());
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already BEHEMOTH in size.");
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
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> grow(s) to BEHEMOTH size!");
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
