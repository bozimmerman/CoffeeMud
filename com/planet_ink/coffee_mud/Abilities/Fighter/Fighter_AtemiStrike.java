package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_AtemiStrike extends StdAbility
{
	public String ID() { return "Fighter_AtemiStrike"; }
	public String name(){ return "Atemi Strike";}
	public String displayText(){return "(Atemi Strike)";}
	private static final String[] triggerStrings = {"ATEMI"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Fighter_AtemiStrike();	}
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
				ExternalPlay.postDeath(invoker,mob,null);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
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

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-adjustedLevel(mob);
		if(levelDiff>0)
			levelDiff=levelDiff*20;
		else
			levelDiff=0;
		// now see if it worked
		boolean hit=(auto)||(CoffeeUtensils.normalizeAndRollLess(mob.adjustedAttackBonus()+target.adjustedArmor()));
		boolean success=profficiencyCheck((-levelDiff)+(-((target.charStats().getStat(CharStats.STRENGTH)-mob.charStats().getStat(CharStats.STRENGTH)))),auto)&&(hit);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),auto?"<T-NAME> hit(s) the floor!":"^F<S-NAME> deliver(s) a deadly Atemi strike to <T-NAMESELF>!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> do(es) not look well.");
				success=maliciousAffect(mob,target,mob.envStats().level()/3,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) the deadly Atemi strike on <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
