package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SenseAlignment extends Prayer
{
	public String ID() { return "Prayer_SenseAlignment"; }
	public String name(){ return "Sense Alignment";}
	//public String displayText(){ return "(Sense Alignment)";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_SenseAlignment();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> peer(s) into the eyes of <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell(mob,target,null,"<T-NAME> seem(s) like <T-HE-SHE> is "+CommonStrings.alignmentStr(target.getAlignment())+".");
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> peer(s) into the eyes of <T-NAMESELF>, but then blink(s).");


		// return whether it worked
		return success;
	}
}
