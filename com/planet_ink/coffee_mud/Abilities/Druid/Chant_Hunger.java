package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Hunger extends Chant
{
	public Chant_Hunger()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hunger";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Hunger)";

		canAffectCode=0;
		canTargetCode=Ability.CAN_MOBS;

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_Hunger();
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
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> chant(s) to <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					target.curState().adjHunger(-(150+(mob.envStats().level()) * 5),target.maxState());
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> feel(s) incredibly hungry!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
