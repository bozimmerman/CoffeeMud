package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Paladin_Breakup extends StdAbility
{
	public String ID() { return "Paladin_Breakup"; }
	public String name(){ return "Breakup Fight";}
	private static final String[] triggerStrings = {"BREAKUP"};
	public int quality(){return Ability.OK_OTHERS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("You must end combat before trying to break up someone elses fight.");
			return false;
		}
		if((!auto)&&(mob.getAlignment()<650))
		{
			mob.tell("You don't feel worthy of a such a good act.");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(!target.isInCombat())
		{
			mob.tell(target.name()+" is not fighting anyone!");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"<T-NAME> exude(s) a peaceful aura.":"<S-NAME> break(s) up the fight between <T-NAME> and "+target.getVictim().name()+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.makePeace();
				if((target.getVictim()!=null)
				   &&(target.getVictim().getVictim()==target))
						target.getVictim().makePeace();
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to break up <T-NAME>'s fight, but fail(s).");


		// return whether it worked
		return success;
	}
}
