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

public class Prayer_HolyAura extends Prayer
{
	public String ID() { return "Prayer_HolyAura"; }
	public String name(){ return "Holy Aura";}
	public String displayText(){ return "(Holy Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_BLESSING;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setArmor(affectableStats.armor()-20);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+10);
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
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> holy aura fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"<T-NAME> become(s) clothed in holiness.":"^S<S-NAME> "+prayForWord(mob)+" to clothe <T-NAMESELF> in holiness.^?")+CommonStrings.msp("bless.wav",10));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=Prayer_Bless.getSomething(target,true);
				while(I!=null)
				{
					FullMsg msg2=new FullMsg(target,I,null,CMMsg.MASK_GENERAL|CMMsg.MSG_DROP,"<S-NAME> release(s) <T-NAME>.");
					target.location().send(target,msg2);
					Prayer_Bless.endLowerCurses(I,CMAble.lowestQualifyingLevel(ID()));
					I.recoverEnvStats();
					I=Prayer_Bless.getSomething(target,true);
				}
				Prayer_Bless.endLowerBlessings(target,CMAble.lowestQualifyingLevel(ID()));
				Prayer_Bless.endLowerCurses(target,CMAble.lowestQualifyingLevel(ID()));
				beneficialAffect(mob,target,asLevel,0);
				target.recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for holiness, but nothing happens.");


		// return whether it worked
		return success;
	}
}
