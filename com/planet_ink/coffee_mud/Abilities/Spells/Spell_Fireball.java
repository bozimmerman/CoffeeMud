package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Fireball extends Spell
{
	public String ID() { return "Spell_Fireball"; }
	public String name(){return "Fireball";}
	public int maxRange(){return 5;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_Fireball();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A huge fireball appears and blazes towards <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, shooting forth a blazing fireball!^?");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_FIRE|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				invoker=mob;
                int numDice = (int)Math.round(Util.div(adjustedLevel(mob),2.0));
				int damage = Dice.roll(numDice, 10, 10);
				if((!msg.wasModified())||(msg2.wasModified()))
					damage = (int)Math.round(Util.div(damage,2.0));
				ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"The flaming blast <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) and shout(s) at <T-NAMESELF>, but nothing more happens.");

		return success;
	}
}
