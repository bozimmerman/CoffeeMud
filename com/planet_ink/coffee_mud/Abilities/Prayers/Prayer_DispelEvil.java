package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DispelEvil extends Prayer
{
	public Prayer_DispelEvil()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dispel Evil";

		holyQuality=Prayer.HOLY_GOOD;
		quality=Ability.MALICIOUS;
		baseEnvStats().setLevel(9);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_DispelEvil();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if((success)&&(target.getAlignment()<350))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"The evil inside <T-NAME> exercise(s)!":"<S-NAME> exercise(s) the evil inside <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				int harming=Dice.roll(1,15,10);
				if(msg.wasModified())
					harming=(int)Math.round(Util.div(harming,2.0));
				if(target.getAlignment()<350)
				{
					target.setAlignment(mob.getAlignment()+100);
					target.location().show(target,null,Affect.MSG_OK_VISUAL,"The wicked spell "+ExternalPlay.standardHitWord(-1,harming)+" <S-NAME>!");
					ExternalPlay.postDamage(mob,target,this,harming);
					
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> exercise(s) <T-NAMESELF>, but nothing emerges.");


		// return whether it worked
		return success;
	}
}
