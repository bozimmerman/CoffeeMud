package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Poison extends Prayer
{
	public String ID() { return "Prayer_Poison"; }
	public String name(){ return "Unholy Poison";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_Poison();}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> inflict(s) an unholy poison upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					Ability A=CMClass.getAbility("Poison");
					A.invoke(mob,target,true);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to inflict an unholy poison upon <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
