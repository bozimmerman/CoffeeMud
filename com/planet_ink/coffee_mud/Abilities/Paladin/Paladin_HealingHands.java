package com.planet_ink.coffee_mud.Abilities.Paladin;
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

public class Paladin_HealingHands extends StdAbility
{
	public String ID() { return "Paladin_HealingHands"; }
	public String name(){ return "Healing Hands";}
	private static final String[] triggerStrings = {"HANDS"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	protected long lastDone=0;
	public long flags(){return Ability.FLAG_HEALING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		if((!auto)&&(mob.getAlignment()<650))
		{
			mob.tell("Your alignment has alienated your god from you.");
			return false;
		}

		if(mob.curState().getMana()==0)
		{
			mob.tell("You don't have enough mana to do that.");
			return false;
		}

		long now=System.currentTimeMillis();
		if((now-lastDone)<1000)
		{
			mob.tell("You need a second to regather your strength.");
			return false;
		}
		lastDone=now;

		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		helpProfficiency(mob);

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_CAST_SOMANTIC_SPELL,auto?"A pair of celestial hands surround <T-NAME>":"^S<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.curState().adjMana(-(1+(int)Math.round(Util.div(adjustedLevel(mob),5.0))),mob.maxState());
				int healing=1+(int)Math.round(Util.div(adjustedLevel(mob),5.0));
				MUDFight.postHealing(mob,target,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,healing,null);
				target.tell("You feel a little better!");
			}
		}
		else
			return beneficialVisualFizzle(mob,mob,"<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>, but <S-HIS-HER> god does not heed.");


		// return whether it worked
		return success;
	}

}
