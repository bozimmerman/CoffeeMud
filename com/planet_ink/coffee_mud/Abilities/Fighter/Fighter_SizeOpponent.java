package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_SizeOpponent extends StdAbility
{
	public String ID() { return "Fighter_SizeOpponent"; }
	public String name(){ return "Opponent Knowledge";}
	private static final String[] triggerStrings = {"SIZEUP","OPPONENT"};
	public int quality(){return Ability.INDIFFERENT;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){ return Ability.SKILL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_EXAMINESOMETHING|(auto?CMMsg.MASK_GENERAL:0),"<S-NAME> size(s) up <T-NAMESELF> with <S-HIS-HER> eyes.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int adjustedAttack=target.adjustedAttackBonus(mob);
				int adjustedArmor=(-target.adjustedArmor())+50;
				StringBuffer buf=new StringBuffer(target.name()+" looks to have "+target.curState().getHitPoints()+" out of "+target.maxState().getHitPoints()+" hit points.\n\r");
				buf.append(target.charStats().HeShe()+" looks like "+target.charStats().heshe()+" is "+CommonStrings.fightingProwessStr(adjustedAttack)+" and is "+CommonStrings.armorStr(adjustedArmor)+".");
				mob.tell(buf.toString());
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> size(s) up <T-NAMESELF> with <S-HIS-HER> eyes, but look(s) confused.");

		// return whether it worked
		return success;
	}
}
