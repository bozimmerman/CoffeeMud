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

public class Prayer_Absorption extends Prayer
{
	public String ID() { return "Prayer_Absorption"; }
	public String name(){ return "Absorption";}
	public String displayText(){ return "(Absorption)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected Ability absorbed=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB M=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(absorbed!=null)&&(M!=null))
		{
			M.delAbility(absorbed);
			M.tell("You forget all about "+absorbed.name()+".");
			absorbed=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("Umm.. ok. Done.");
			return false;
		}
		Prayer_Absorption old=(Prayer_Absorption)mob.fetchEffect(ID());
		if(old!=null)
		{
			if(old.absorbed!=null)
				mob.tell("You have already absorbed "+old.absorbed.name()+" from someone.");
			else
				mob.tell("You have already absorbed a skill from someone.");
			return false;
		}

		absorbed=null;
		int tries=0;
		while((absorbed==null)&&((++tries)<100))
		{
			absorbed=target.fetchAbility(Dice.roll(1,target.numAbilities(),-1));
			if(mob.fetchAbility(absorbed.ID())!=null)
				absorbed=null;
			else
			if(absorbed.isAutoInvoked())
				absorbed=null;
			else
			if(CMAble.qualifyingLevel(mob,absorbed)>0)
				absorbed=null;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if((success)&&(absorbed!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for some of <T-YOUPOSS> knowledge!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(absorbed.ID());
				absorbed=(Ability)absorbed.copyOf();
				absorbed.setBorrowed(mob,true);
				mob.addAbility(absorbed);
				mob.tell("You have absorbed "+absorbed.name()+"!");
				beneficialAffect(mob,mob,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for some of <T-YOUPOSS> knowledge, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
