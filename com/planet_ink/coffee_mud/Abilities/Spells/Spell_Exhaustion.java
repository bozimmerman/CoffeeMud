package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Exhaustion extends Spell
{
	public String ID() { return "Spell_Exhaustion"; }
	public String name(){return "Exhaustion";}
	public int maxRange(){return 5;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_Exhaustion();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and shout(s)!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				if(msg.value()>0)
				{
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<T-NAME> become(s) exhausted!");
					target.curState().setMovement(0);
					target.curState().setFatigue(target.curState().getFatigue()+CharState.FATIGUED_MILLIS);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) and shout(s) at <T-NAMESELF>, but nothing more happens.");

		return success;
	}
}
