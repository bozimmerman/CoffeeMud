package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Distract extends ThiefSkill
{
	public String ID() { return "Thief_Distract"; }
	public String name(){ return "Distract";}
	public String displayText(){ return "(Distracted)";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"DISTRACT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor()+(affectableStats.armor()/2)+abilityCode());
		affectableStats.setAttackAdjustment((affectableStats.attackAdjustment()-(affectableStats.attackAdjustment()/2))-abilityCode());
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return true;

		MOB mob=(MOB)affected;
		if(invoker.location()!=mob.location())
			unInvoke();
		else
		{
			// preventing distracting player from doin anything else
			if(msg.amISource(invoker)
			&&(Dice.rollPercentage()>(mob.charStats().getStat(CharStats.WISDOM)*2))
			&&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK))
			{
				invoker.location().show(invoker,mob,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> distract(s) <T-NAME>.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if((invoker!=null)&&(invoker.location()==mob.location())&&(!invoker.amDead()))
					invoker.tell("You are no longer distracting "+mob.name()+".");
				if((mob.location()!=null)&&(!mob.amDead()))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer so distracted.");
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((Sense.isSitting(mob)||Sense.isSleeping(mob)))
		{
			mob.tell("You are on the floor!");
			return false;
		}

		if((!Sense.aliveAwakeMobile(mob,true)||(Sense.isSitting(mob))))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to distract "+mob.getVictim().name()+"!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		boolean success=profficiencyCheck(mob,-levelDiff,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,auto?"<T-NAME> seem(s) distracted!":"<S-NAME> distract(s) <T-NAMESELF>!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,4);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to distract <T-NAMESELF>, but flub(s) it.");
		return success;
	}
}
