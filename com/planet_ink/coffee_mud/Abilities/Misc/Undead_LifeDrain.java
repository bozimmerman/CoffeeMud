package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Undead_LifeDrain extends StdAbility
{
	public String ID() { return "Undead_LifeDrain"; }
	public String name(){ return "Drain Life";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"DRAINLIFE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}

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

		boolean success=profficiencyCheck(mob,0,auto);


		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			int much=mob.envStats().level();
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_UNDEAD|(auto?CMMsg.MASK_GENERAL:0),auto?"":"^S<S-NAME> clutch(es) <T-NAMESELF>, and drain(s) <T-HIS-HER> life!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.curState().adjMana(-much,mob.maxState());
				if(msg.value()>0)
					much = (int)Math.round(Util.div(much,2.0));
				MUDFight.postDamage(mob,target,this,much,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_GASSING,"The drain <DAMAGE> <T-NAME>!");
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to drain life from <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
