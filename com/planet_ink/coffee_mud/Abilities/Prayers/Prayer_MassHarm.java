package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_MassHarm extends Prayer
{
	public Prayer_MassHarm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Harm";

		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(22);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_MassHarm();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Hashtable h=ExternalPlay.properTargets(this,mob);
		if(h==null) return false;

		boolean success=profficiencyCheck(0,auto);
		int numEnemies=h.size();
		for(Enumeration e=h.elements();e.hasMoreElements();)
		{
			MOB target=(MOB)e.nextElement();
			if(target!=mob)
			{
				if(success)
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"<T-NAME> become(s) surrounded by a dark cloud.":"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>.");
					if(mob.location().okAffect(msg))
					{
						mob.location().send(mob,msg);
						int harming=Dice.roll(4,mob.envStats().level()/numEnemies,numEnemies);
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"The spell "+ExternalPlay.hitWord(-1,harming)+" <S-NAME>!");
						ExternalPlay.postDamage(mob,target,this,harming);
					}
				}
				else
					maliciousFizzle(mob,target,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but <S-HIS-HER> god does not heed.");
			}
		}


		// return whether it worked
		return success;
	}
}
