package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_ArrestingSap extends StdAbility
{
	public String ID() { return "Skill_ArrestingSap"; }
	public String name(){ return "Arresting Sap";}
	public String displayText(){ return "(Knocked out)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"SAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Skill_ArrestingSap();}
	private int enhancement=0;
	public int abilityCode(){return enhancement;}
	public void setAbilityCode(int newCode){enhancement=newCode;}

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
					mob.tell("You are way too drowsy.");
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
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
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
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> regain(s) consciousness.");
				ExternalPlay.standIfNecessary(mob);
			}
			else
				mob.tell("You regain consciousness.");
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-adjustedLevel(mob);
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		levelDiff-=(abilityCode()*mob.charStats().getStat(CharStats.STRENGTH));
		
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_NOISYMOVEMENT|Affect.MASK_MALICIOUS|Affect.MSK_MALICIOUS_MOVE|(auto?Affect.MASK_GENERAL:0),auto?"<T-NAME> hit(s) the floor!":"^F<S-NAME> rear(s) back and sap(s) <T-NAMESELF>, and knocking <T-HIM-HER> out!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,3,-1);
				if(mob.getVictim()==target) mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> rear(s) back and attempt(s) to knock <T-NAMESELF> out, but fail(s).");

		// return whether it worked
		return success;
	}
}
