package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Desecrate extends Prayer
{
	public String ID() { return "Prayer_Desecrate"; }
	public String name(){ return "Desecrate";}
	public int quality(){ return INDIFFERENT;}
	public int holyQuality(){ return HOLY_EVIL;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_Desecrate();}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> feel(s) desecrated!":"^S<S-NAME> desecrate(s) <T-NAMESELF> before <S-HIS-HER> god.^?");
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
