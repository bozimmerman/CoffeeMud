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

public class Fighter_AtemiStrike extends StdAbility
{
	public String ID() { return "Fighter_AtemiStrike"; }
	public String name(){ return "Atemi Strike";}
	public String displayText(){return "(Atemi Strike)";}
	private static final String[] triggerStrings = {"ATEMI"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0; }
	protected int overrideMana(){return 100; }
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){ return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

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
				MUDFight.postDeath(invoker,mob,null);
		}
	}

	public boolean anyWeapons(MOB mob)
	{
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			   &&((I.amWearingAt(Item.WIELD))
			      ||(I.amWearingAt(Item.HELD))))
				return true;
		}
		return false;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away from your target to strike!");
			return false;
		}

		if((!auto)&&(mob.baseWeight()<(mob.baseWeight()/2)))
		{
			mob.tell(target.name()+" is too big to strike!");
			return false;
		}

		if((!auto)&&(mob.envStats().level()<(target.envStats().level()-5)))
		{
			mob.tell(target.name()+" is too powerful to strike!");
			return false;
		}
		if((!auto)&&(anyWeapons(mob)))
		{
			mob.tell("You must be unarmed to perform the strike.");
			return false;
		}

		if(Sense.isGolem(target))
		{
			mob.tell(target,null,null,"You can't hurt <T-NAMESELF> with Atemi Strike.");
			return false;
		}

		if(mob.charStats().getBodyPart(Race.BODY_HAND)<=0)
		{
			mob.tell("You need hands to do this.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-adjustedLevel(mob,asLevel);
		if(levelDiff>0)
			levelDiff=levelDiff*20;
		else
			levelDiff=0;
		// now see if it worked
		boolean hit=(auto)||(Dice.normalizeAndRollLess(mob.adjustedAttackBonus(target)+target.adjustedArmor()));
		boolean success=profficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STRENGTH)-mob.charStats().getStat(CharStats.STRENGTH)))),auto)&&(hit);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),auto?"<T-NAME> hit(s) the floor!":"^F<S-NAME> deliver(s) a deadly Atemi strike to <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> do(es) not look well.");
				success=maliciousAffect(mob,target,asLevel,mob.envStats().level()/3,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) the deadly Atemi strike on <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
