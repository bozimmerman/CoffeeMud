package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_MassHeal extends Prayer
{
	public Prayer_MassHeal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Heal";
		baseEnvStats().setLevel(22);
		quality=Ability.BENEFICIAL_OTHERS;
		holyQuality=Prayer.HOLY_GOOD;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_MassHeal();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null) return false;
		for(Enumeration e=h.elements();e.hasMoreElements();)
		{
			MOB target=(MOB)e.nextElement();
			if(success)
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> become(s) surrounded by a white light.":"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>.");
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					int healing=Dice.roll(mob.envStats().level(),5,mob.envStats().level());
					target.curState().adjHitPoints(healing,target.maxState());
					target.tell("You feel tons better!");
				}
			}
			else
			{
				// it didn't work, but tell everyone you tried.
				FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but <S-HIS-HER> god does not heed.");
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
		}

		// return whether it worked
		return success;
	}
}
