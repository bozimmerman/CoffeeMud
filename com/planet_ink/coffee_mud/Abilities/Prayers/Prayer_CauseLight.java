package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CauseLight extends Prayer
{
	public Prayer_CauseLight()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Cause Light Wounds";

		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_CauseLight();
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
			FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"A light painful burst assaults <T-NAME>.":"<S-NAME> pray(s) at <T-NAMESELF> for a light burst of pain!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int harming=Dice.roll(1,mob.envStats().level(),3);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"The spell "+ExternalPlay.standardHitWord(-1,harming)+" <S-NAME>!");
					ExternalPlay.postDamage(mob,target,this,harming);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> pray(s) at <T-NAMESELF>, but <S-HIS-HER> god does not heed.");

		// return whether it worked
		return success;
	}
}
