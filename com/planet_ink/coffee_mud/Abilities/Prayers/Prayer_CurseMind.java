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

public class Prayer_CurseMind extends Prayer
{
	public String ID() { return "Prayer_CurseMind"; }
	public String name(){return "Curse Mind";}
	public String displayText(){return "(Cursed Mind)";}
	public int quality(){return MALICIOUS;};
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_CURSE;}
	protected int canAffectCode(){return CAN_MOBS;}

	boolean notAgain=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			MOB newvictim=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if(newvictim!=mob) mob.setVictim(newvictim);
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your mind feels less cursed.");
		CommonMsgs.stand(mob,true);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)-50);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,-target.envStats().level(),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> feel(s) <T-HIS-HER> mind become cursed!":"^S<S-NAME> "+prayForWord(mob)+" to curse the mind of <T-NAMESELF>!^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
					success=maliciousAffect(mob,target,asLevel,15,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to curse the mind of <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
