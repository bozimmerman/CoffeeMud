package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Distract extends ThiefSkill
{
	public Thief_Distract()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Distract";
		displayText="(Distracted)";
		miscText="";

		triggerStrings.addElement("DISTRACT");

		canTargetCode=Ability.CAN_MOBS;
		canAffectCode=Ability.CAN_MOBS;
		
		quality=Ability.OK_OTHERS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Distract();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor()+(affectableStats.armor()/2));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(affectableStats.attackAdjustment()/2));
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return true;

		MOB mob=(MOB)affected;
		if(invoker.location()!=mob.location())
			unInvoke();
		else
		{
			// preventing distracting player from doin anything else
			if(affect.amISource(invoker)
			&&(Dice.rollPercentage()>(mob.charStats().getStat(CharStats.WISDOM)*2))
			&&(affect.sourceMinor()==Affect.TYP_WEAPONATTACK))
			{
				invoker.location().show(invoker,mob,Affect.MSG_NOISYMOVEMENT,"<S-NAME> distract(s) <T-NAME>.");
				return false;
			}
		}
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
		{
			if(!mob.amDead())
			{
				if((invoker!=null)&&(invoker.location()==mob.location())&&(!invoker.amDead()))
					invoker.tell("You are no longer distracting "+mob.name()+".");
				mob.tell("You are no longer distracted.");
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((Sense.isSitting(target)||Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" is on the floor!");
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

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>0) 
			levelDiff=levelDiff*5;
		else 
			levelDiff=0;
		boolean success=profficiencyCheck(-levelDiff,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MASK_MALICIOUS|Affect.MSG_THIEF_ACT,auto?"<T-NAME> seem(s) distracted!":"<S-NAME> distract(s) <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
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
