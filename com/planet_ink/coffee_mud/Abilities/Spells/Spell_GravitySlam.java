package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_GravitySlam extends Spell
{
	public String ID() { return "Spell_GravitySlam"; }
	public String name(){return "Gravity Slam";}
	public int maxRange(){return 5;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){ return new Spell_GravitySlam();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	public long flags(){return Ability.FLAG_MOVING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"":"^S<S-NAME> incant(s) and point(s) at <T-NAMESELF>!^?"));
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;

				int damage = 0;
				int maxDie =  adjustedLevel(mob);
				if(!Sense.isFlying(target))
					maxDie=maxDie/2;
				damage += Dice.roll(maxDie,adjustedLevel(mob)/2,adjustedLevel(mob)/2);
				if(msg.wasModified())
					damage = (int)Math.round(Util.div(damage,2.0));
				if(!Sense.isFlying(target))
					mob.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> is hurled up into the air and **SLAMMED** back down!");
				else
					mob.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> is hurled even higher into the air and **SLAMMED** back down!");

				if(target.location()==mob.location())
					ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.TYP_JUSTICE,Weapon.TYPE_BASHING,"The fall <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) and point(s) at <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}