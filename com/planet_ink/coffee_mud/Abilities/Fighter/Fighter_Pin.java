package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_Pin extends StdAbility
{
	public String ID() { return "Fighter_Pin"; }
	public String name(){ return "Pin";}
	public String displayText()
	{
		if(affected==invoker)
			return "(Pinning)";
		else
			return "(Pinned)";
	}
	private static final String[] triggerStrings = {"PIN"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){ return Ability.SKILL;}
	public long flags(){return Ability.FLAG_BINDING;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL)))
		{
			if((Util.bset(msg.sourceMajor(),CMMsg.MASK_EYES))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE)))
			{
				if(msg.sourceMessage()!=null)
					mob.tell("You are pinned!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
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
			if((!mob.amDead())&&(Sense.isInTheGame(mob)))
			{
				if(mob==invoker)
				{
					if(mob.location()!=null)
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> release(s) <S-HIS-HER> pin.");
					else
						mob.tell("You release your pin.");
				}
				else
				{
					if(mob.location()!=null)
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> <S-IS-ARE> released from the pin");
					else
						mob.tell("You are released from the pin.");
				}
				CommonMsgs.stand(mob,true);
			}
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away from your target to pin them!");
			return false;
		}

		if((!auto)&&(mob.baseWeight()<(target.baseWeight()-200)))
		{
			mob.tell(target.name()+" is too big to pin!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-adjustedLevel(mob);
		if(levelDiff>0)
			levelDiff=levelDiff*10;
		else
			levelDiff=0;
		// now see if it worked
		boolean hit=(auto)||(Dice.normalizeAndRollLess(mob.adjustedAttackBonus(target)+target.adjustedArmor()));
		boolean success=profficiencyCheck(mob,(-levelDiff)+(-(((target.charStats().getStat(CharStats.STRENGTH)-mob.charStats().getStat(CharStats.STRENGTH))*5))),auto)&&(hit);
		success=success&&(target.charStats().getBodyPart(Race.BODY_LEG)>0);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),auto?"<T-NAME> get(s) pinned!":"^F<S-NAME> pin(s) <T-NAMESELF> to the floor!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,5,-1);
				success=maliciousAffect(mob,mob,5,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to pin <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
