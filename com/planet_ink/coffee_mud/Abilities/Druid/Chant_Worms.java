package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_Worms extends Chant implements DiseaseAffect
{
	public String ID() { return "Chant_Worms"; }
	public String name(){ return "Worms";}
	public String displayText(){return "(Worms)";}
	public int quality(){return Ability.MALICIOUS;}
	public int abilityCode(){return 0;}
	public int difficultyLevel(){return 1;}
	int plagueDown=5;
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		MOB mob=(MOB)affected;
		mob.curState().adjHunger(-100,mob.maxState().maxHunger(mob.baseWeight()));
		if((--plagueDown)<=0)
		{
			plagueDown=5;
			if(invoker==null) invoker=mob;
			int dmg=(mob.envStats().level()/4)+1;
			MUDFight.postDamage(invoker,mob,this,dmg,CMMsg.TYP_DISEASE,-1,"<T-NAME> feel(s) <T-HIS-HER> innards being consumed by worms!");
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-1);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-1);
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
			{
				Ability A=mob.fetchEffect("TemporaryImmunity");
				if(A==null)
				{
					A=CMClass.getAbility("TemporaryImmunity");
					A.setBorrowed(mob,true);
					A.makeLongLasting();
					mob.addEffect(A);
					A.makeLongLasting();
				}
				A.setMiscText("+"+ID());
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> worms disease is cured.");
			}
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>!^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_DISEASE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					invoker=mob;
					maliciousAffect(mob,target,asLevel,688,-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) very sick in the stomach!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
