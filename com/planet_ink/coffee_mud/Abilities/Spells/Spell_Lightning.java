package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Lightning extends Spell
{
	public String ID() { return "Spell_Lightning"; }
	public String name(){return "Lightning Bolt";}
	public String displayText(){return "(Lightning Bolt spell)";}
	public int maxRange(){return 2;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_Lightning();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

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


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A lightning bolt streaks through the air!":"^S<S-NAME> point(s) incanting at <T-NAMESELF>, shooting forth a lightning bolt!^?");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_ELECTRIC|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				int maxDie =  (int)Math.round(new Integer(adjustedLevel(mob)).doubleValue()/3.0);
				int damage = Dice.roll(maxDie,10,maxDie);
				if((!msg.wasModified())&&(!msg2.wasModified()))
					damage = (int)Math.round(Util.div(damage,2.0));

				if(target.location()==mob.location())
					ExternalPlay.postDamage(mob,target,this,damage,Affect.ACT_GENERAL|Affect.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"The bolt <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> invoke(s) at <T-NAMESELF>, but the spell fades.");


		// return whether it worked
		return success;
	}
}
