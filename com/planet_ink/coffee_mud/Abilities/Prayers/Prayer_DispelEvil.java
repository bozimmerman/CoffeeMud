package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DispelEvil extends Prayer
{
	public String ID() { return "Prayer_DispelEvil"; }
	public String name(){ return "Dispel Evil";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_DispelEvil();	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if((success)&&(target.getAlignment()<350))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"The evil inside <T-NAME> exorcise(s)!":"^S<S-NAME> exorcise(s) the evil inside <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int harming=Dice.roll(3,adjustedLevel(mob)+8,10);
				if(msg.value()>0)
					harming=(int)Math.round(Util.div(harming,2.0));
				if(target.getAlignment()<350)
					MUDFight.postDamage(mob,target,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"The wicked spell <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> exorcise(s) <T-NAMESELF>, but nothing emerges.");


		// return whether it worked
		return success;
	}
}
