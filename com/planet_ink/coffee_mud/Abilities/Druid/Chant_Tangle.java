package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_Tangle extends Chant
{
	public String ID() { return "Chant_Tangle"; }
	public String name(){ return "Tangle";}
	public String displayText(){return "(Tangled)";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 2;}
	public Item thePlants=null;
	public int amountRemaining=0;
	public long flags(){return Ability.FLAG_BINDING;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if((thePlants==null)||(thePlants.owner()==null)||(!(thePlants.owner() instanceof Room)))
		{
			unInvoke();
			return super.okMessage(myHost,msg);
		}

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				mob.location().show(mob,null,thePlants,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against <O-NAME>.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)*4);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
				mob.location().show(mob,null,thePlants,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of <O-NAME>.");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		thePlants=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if(thePlants==null)
		{
			mob.tell("There doesn't appear to be any plants here you can control!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> begin(s) to chant.^?"))
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						amountRemaining=200;
						if(target.location()==mob.location())
						{
							success=maliciousAffect(mob,target,asLevel,(adjustedLevel(mob,asLevel)*10),-1);
							target.location().show(target,null,thePlants,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) stuck in <O-NAME> as they grow and twist around <S-HIM-HER>!");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fades.");


		// return whether it worked
		return success;
	}
}
