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
public class Prayer_UnholyWord extends Prayer
{
	public String ID() { return "Prayer_UnholyWord"; }
	public String name(){ return "Unholy Word";}
	public String displayText(){ return "(Unholy Word)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_CURSE;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		if(mob==invoker) return;

		if(mob.getAlignment()<350)
		{
			affectableStats.setArmor(affectableStats.armor()-30);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20);
		}
		else
		if(mob.getAlignment()>650)
		{
			affectableStats.setArmor(affectableStats.armor()+30);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-20);
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The unholy word has been spoken.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		String str=auto?"The unholy word is spoken.":"^S<S-NAME> speak(s) the unholy word"+ofDiety(mob)+" to <T-NAMESELF>.^?";
		String missStr="<S-NAME> speak(s) the unholy word of"+ofDiety(mob)+", but nothing happens.";

		Room room=mob.location();
		if(room!=null)
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB target=room.fetchInhabitant(i);
			if(target==null) break;
			int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
			if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
			if(target.getAlignment()>650)
				affectType=affectType|CMMsg.MASK_MALICIOUS;

			if(success)
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,str);
				if(room.okMessage(mob,msg))
				{
					room.send(mob,msg);
					if(msg.value()<=0)
					{
						if(Sense.canBeHeardBy(mob,target))
						{
							Item I=Prayer_Curse.getSomething(mob,true);
							if(I!=null)
							{
								Prayer_Curse.endLowerBlessings(I,CMAble.lowestQualifyingLevel(ID()));
								I.recoverEnvStats();
							}
							Prayer_Curse.endLowerBlessings(target,CMAble.lowestQualifyingLevel(ID()));
							beneficialAffect(mob,target,asLevel,0);
							target.recoverEnvStats();
						}
						else
						if(Util.bset(affectType,CMMsg.MASK_MALICIOUS))
							maliciousFizzle(mob,target,"<T-NAME> did not hear the unholy word!");
						else
							beneficialWordsFizzle(mob,target,"<T-NAME> did not hear the unholy word!");
					}
				}
			}
			else
			{
				if(Util.bset(affectType,CMMsg.MASK_MALICIOUS))
					maliciousFizzle(mob,target,"<S-NAME> attempt(s) to speak the unholy word to <T-NAMESELF>, but flub(s) it.");
				else
					beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to speak the unholy word to <T-NAMESELF>, but flub(s) it.");
				missStr=null;
				return false;
			}
		}


		// return whether it worked
		return success;
	}
}
