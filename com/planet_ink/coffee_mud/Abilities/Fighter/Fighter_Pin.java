package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
	public Environmental newInstance(){	return new Fighter_Pin();	}
	public int classificationCode(){ return Ability.SKILL;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amISource(mob))&&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL)))
		{
			if((Util.bset(affect.sourceMajor(),Affect.MASK_EYES))
			||(Util.bset(affect.sourceMajor(),Affect.MASK_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.MASK_MOUTH))
			||(Util.bset(affect.sourceMajor(),Affect.MASK_MOVE)))
			{
				if(affect.sourceMessage()!=null)
					mob.tell("You are pinned!");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
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
			if(!mob.amDead())
			{
				if(mob==invoker)
				{
					if(mob.location()!=null)
						mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> release(s) <S-HIS-HER> pin.");
					else
						mob.tell("You release your pin.");
				}
				else
				{
					if(mob.location()!=null)
						mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> <S-IS-ARE> released from the pin");
					else
						mob.tell("You are released from the pin.");
				}
				ExternalPlay.standIfNecessary(mob);
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

		if((!auto)&&(mob.envStats().weight()<(target.envStats().weight()-100)))
		{
			mob.tell(target.name()+" is way to big to pin!");
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
		boolean hit=(auto)||(CoffeeUtensils.normalizeAndRollLess(mob.adjustedAttackBonus()+target.adjustedArmor()));
		boolean success=profficiencyCheck((-levelDiff)+(-(((target.charStats().getStat(CharStats.STRENGTH)-mob.charStats().getStat(CharStats.STRENGTH))*5))),auto)&&(hit);
		success=success&&(target.charStats().getMyRace().bodyMask()[Race.BODY_LEG]>0);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),auto?"<T-NAME> get(s) pinned!":"^F<S-NAME> pin(s) <T-NAMESELF> to the floor!^?");
			if(mob.location().okAffect(mob,msg))
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
