package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Heal extends Prayer
{
	public String ID() { return "Prayer_Heal"; }
	public String name(){ return "Heal";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public int holyQuality(){ return HOLY_GOOD;}
	public Environmental newInstance(){	return new Prayer_Heal();}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) surrounded by a white light.":"^S<S-NAME> pray(s) over <T-NAMESELF> for tremendous healing power.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				int healing=Dice.roll(5,adjustedLevel(mob),10);
				target.curState().adjHitPoints(healing,target.maxState());
				target.tell("You feel tons better!");
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) over <T-NAMESELF>, but <S-HIS-HER> god does not heed.");


		// return whether it worked
		return success;
	}
}
