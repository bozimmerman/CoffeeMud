package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CureSerious extends Prayer
{
	public Prayer_CureSerious()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Cure Serious Wounds";
		baseEnvStats().setLevel(6);
		quality=Ability.BENEFICIAL_OTHERS;
		holyQuality=Prayer.HOLY_GOOD;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_CureSerious();
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"A soft white glow surrounds <T-NAME>.":"<S-NAME> pray(s) over <T-NAMESELF>, delivering a serious healing touch.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				int healing=Dice.roll(3,mob.envStats().level(),5);
				target.curState().adjHitPoints(healing,target.maxState());
				target.tell("You feel better!");
			}
		}
		else
		{
			// it didn't work, but tell everyone you tried.
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> pray(s) over <T-NAMESELF>, but <S-HIS-HER> god does not heed.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}


		// return whether it worked
		return success;
	}
}
