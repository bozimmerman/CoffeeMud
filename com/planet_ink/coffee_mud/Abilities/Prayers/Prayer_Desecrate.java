package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Desecrate extends Prayer
{
	public Prayer_Desecrate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Desecrate";

		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(3);

		canAffectCode=0;
		canTargetCode=Ability.CAN_ITEMS;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Desecrate();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!(target instanceof DeadBody))
		{
			mob.tell("You may only desecrate the dead.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> feel(s) desecrated!":"<S-NAME> desecrate(s) <T-NAMESELF> before <S-HIS-HER> god.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				target.destroyThis();
				if(mob.getAlignment()<=500)
					mob.charStats().getMyClass().gainExperience(mob,null,null,5);
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to desecrate <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
