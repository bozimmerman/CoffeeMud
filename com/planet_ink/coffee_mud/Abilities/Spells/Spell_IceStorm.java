package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_IceStorm extends Spell
{
	public String ID() { return "Spell_IceStorm"; }
	public String name(){return "Ice Storm";}
	public int maxRange(){return 5;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_IceStorm();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth storming.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.location().show(mob,null,affectType(auto),auto?"A ferocious ice storm appears!":"^S<S-NAME> evoke(s) a ferocious ice storm!^?");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_COLD|(auto?Affect.MASK_GENERAL:0),null);
				if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					invoker=mob;

					int numDice = adjustedLevel(mob);
					int damage = Dice.roll(numDice, 15, 10);
					if((!msg.wasModified())||(msg2.wasModified()))
						damage = (int)Math.round(Util.div(damage,2.0));
					damage = (int)Math.round(Util.div(damage,2.0));
					ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_FROSTING,"The freezing blast <DAMAGE> <T-NAME>!");
					ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_FROSTING,"The lumps of hail <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to evoke an ice storm, but the spell fizzles.");

		return success;
	}
}
