package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CureSerious extends Prayer
{
	public String ID() { return "Prayer_CureSerious"; }
	public String name(){ return "Cure Serious Wounds";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALING;}
	public Environmental newInstance(){	return new Prayer_CureSerious();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		boolean undead=target.charStats().getMyRace().racialCategory().equals("Undead");

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,(!undead?0:Affect.MASK_MALICIOUS)|affectType(auto),auto?"A soft white glow surrounds <T-NAME>.":"^S<S-NAME> "+prayWord(mob)+", delivering a serious healing touch to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				int healing=Dice.roll(3,adjustedLevel(mob),5);
				ExternalPlay.postHealing(mob,target,this,Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,healing,null);
				target.tell("You feel better!");
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
