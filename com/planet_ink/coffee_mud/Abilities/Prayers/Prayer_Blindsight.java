package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Blindsight extends Prayer
{
	public Prayer_Blindsight()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Blindsight";
		displayText="(Blindsight)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		quality=Ability.BENEFICIAL_SELF;
		baseEnvStats().setLevel(13);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Blindsight();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("Your natural vision returns.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
		if(!Sense.canSee(affected))
			affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_SEE);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You already have the blindsight.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> attain(s) blindsight.":"^S<S-NAME> pray(s) for the blindsight.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for blindsight, but go(es) unanswered.");


		// return whether it worked
		return success;
	}
}
