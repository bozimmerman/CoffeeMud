package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Poison extends Chant
{
	public String ID() { return "Chant_Poison"; }
	public String name(){ return "Venomous Bite";}
	public String displayText(){return "";}
	public int canAffectCode(){return 0;}
	public int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Chant_Poison();}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF> and bite(s) <T-HIM-HER>!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					invoker=mob;
					Ability A=CMClass.getAbility("Poison_Venom");
					if(A!=null)
						A.invoke(mob,target,true);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}