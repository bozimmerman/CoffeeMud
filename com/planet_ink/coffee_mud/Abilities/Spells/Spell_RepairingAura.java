package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_RepairingAura extends Spell
{
	public String ID() { return "Spell_RepairingAura"; }
	public String name(){return "Repairing Aura";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	public int overrideMana(){ return 50;}
	public static final int REPAIR_MAX=30;
	public int repairDown=REPAIR_MAX;
	public int adjustedLevel=1;
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
	    if(!super.tick(ticking,tickID))
	        return false;
	    repairDown-=adjustedLevel;
	    if((repairDown<=0)&&(affected instanceof Item))
	    {
	        repairDown=REPAIR_MAX;
	        Item I=(Item)affected;
	        if((I.subjectToWearAndTear())&&(I.usesRemaining()<100))
	        {
	            if(I.owner() instanceof Room)
	                ((Room)I.owner()).showHappens(CMMsg.MSG_OK_VISUAL,I,"<S-NAME> is magically repairing itself.");
	            else
	            if(I.owner() instanceof MOB)
	                ((MOB)I.owner()).tell(I.name()+" is magically repairing itself.");
	            I.setUsesRemaining(I.usesRemaining()+1);
	        }
	    }
	    return true;
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already repairing!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> attain(s) a repairing aura.");
				beneficialAffect(mob,target,asLevel,0);
				Spell_RepairingAura A=(Spell_RepairingAura)target.fetchEffect(ID());
				if(A!=null) A.adjustedLevel=adjustedLevel(mob,asLevel);
				target.recoverEnvStats();
				mob.recoverEnvStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting, but nothing happens.");

		// return whether it worked
		return success;
	}
}
