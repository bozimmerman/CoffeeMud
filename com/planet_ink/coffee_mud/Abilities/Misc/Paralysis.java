package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paralysis extends StdAbility
{
	public Paralysis()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Paralyze";
		displayText="(Paralyzed)";
		quality=Ability.MALICIOUS;
		baseEnvStats().setLevel(10);

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Paralysis();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_MOVE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("The paralysis eases out of your muscles.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_PARALYZE|(auto?Affect.ACT_GENERAL:0),auto?"":"<S-NAME> paralyze(s) <T-NAMESELF>.");
			if(target.location().okAffect(msg))
			{
				target.location().send(target,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,-1);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> can't move!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to paralyze <T-NAMESELF>, but fail(s)!");


		// return whether it worked
		return success;
	}
}
