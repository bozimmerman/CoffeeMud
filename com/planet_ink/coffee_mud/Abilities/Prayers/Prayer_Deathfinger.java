package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Deathfinger extends Prayer
{
	public String ID() { return "Prayer_Deathfinger"; }
	public String name(){ return "Deathfinger";}
	public int quality(){ return MALICIOUS;}
	public int holyQuality(){ return HOLY_EVIL;}
	public Environmental newInstance(){	return new Prayer_Deathfinger();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if((auto)||(mob.curState().getMana()<mob.maxState().getMana()))
		{
			mob.tell("You must be at full mana to invoke this power.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"A finger of death rages at <T-NAME>.":"^S<S-NAME> point(s) in rage at <T-NAMESELF> and "+prayWord(mob)+"!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int harming=(int)Math.round(Util.div(target.curState().getHitPoints(),2.0));
					ExternalPlay.postDamage(mob,target,this,harming,Affect.MASK_GENERAL|Affect.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The finger of DEATH <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) in rage at <T-NAMESELF> and "+prayWord(mob)+", but "+hisHerDiety(mob)+" does nothing.");


		// return whether it worked
		return success;
	}
}