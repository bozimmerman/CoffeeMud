package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_BurningHands extends Spell
{
	public String ID() { return "Spell_BurningHands"; }
	public String name(){return "Burning Hands";}
	public String displayText(){return "(Burning Hands spell)";}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_BurningHands();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),((auto?"":"^S<S-NAME> incant(s) and reach(es) for <T-NAMESELF>.  ")+"A fan of flames erupts!^?")+CommonStrings.msp("fireball.wav",40));
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_FIRE|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(mob,msg))&&(mob.location().okAffect(mob,msg2)))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				mob.location().send(mob,msg2);
				int damage = 0;
				int maxDie =  adjustedLevel(mob);
				damage += Dice.roll(1,maxDie,15);
				if((msg2.wasModified())||(msg.wasModified()))
					damage = (int)Math.round(Util.div(damage,2.0));
				if(target.location()==mob.location())
					ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"The flaming hands <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) and reach(es) for <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}
