package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Whomp extends StdAbility
{
	public Fighter_Whomp()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Whomp";
		displayText="(knocked out)";
		miscText="";

		triggerStrings.addElement("WHOMP");

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Whomp();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amISource(mob))&&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL)))
		{
			if((Util.bset(affect.sourceMajor(),Affect.ACT_EYES))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOUTH))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_EARS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOVE)))
			{
				if(affect.sourceMessage()!=null)
					mob.tell(mob,null,"You are way too drowsy.");
				return false;
			}
		}
		return super.okAffect(affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SLEEPING);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(!mob.amDead())
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> seem(s) less drowsy.");
			else
				mob.tell("You feel less drowsy.");
			ExternalPlay.standIfNecessary(mob);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!auto)&&(mob.charStats().getStrength()<18))
		{
			mob.tell("You need at least an 18 strength to do that.");
			return false;
		}

		if((!auto)&&(mob.envStats().weight()<(target.envStats().weight()-100)))
		{
			mob.tell(target.name()+" is way to big to knock out!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>0) 
			levelDiff=levelDiff*10;
		else 
			levelDiff=0;
		// now see if it worked
		boolean success=profficiencyCheck((-levelDiff)+(-((target.charStats().getStrength()-mob.charStats().getStrength()))),auto)&&(auto||((target!=null)&&(ExternalPlay.isHit(mob,target))));
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),auto?"<T-NAME> hit(s) the floor!":"<S-NAME> knock(s) <T-NAMESELF> to the floor!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,2,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to knock <T-NAMESELF> out, but fail(s).");

		// return whether it worked
		return success;
	}
}
