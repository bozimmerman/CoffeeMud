package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Sacrifice extends Prayer
{
	public Prayer_Sacrifice()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sacrifice";

		baseEnvStats().setLevel(3);
		holyQuality=Prayer.HOLY_GOOD;

		addQualifyingClass("Cleric",baseEnvStats().level());
		addQualifyingClass("Paladin",baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Sacrifice();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands);
		if(target==null) return false;

		if(!(target instanceof DeadBody))
		{
			mob.tell("You may only sacrifice the dead.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> sacrifice(s) <T-HIM-HER>self.":"<S-NAME> sacrifice(s) <T-NAMESELF> to <S-HIS-HER> god.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				target.destroyThis();
				if(mob.getAlignment()>=500)
					mob.charStats().getMyClass().gainExperience(mob,null,5);
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			// it didn't work, but tell everyone you tried.
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> attempt(s) to sacrifice <T-NAMESELF>, but fail(s).");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}


		// return whether it worked
		return success;
	}
}
