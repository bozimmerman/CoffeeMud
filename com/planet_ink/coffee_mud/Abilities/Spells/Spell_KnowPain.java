package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_KnowPain extends Spell
{
	public String ID() { return "Spell_KnowPain"; }
	public String name(){return "Know Pain";}
	public String displayText(){return "(Spell_KnowPain)";}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){ return new Spell_KnowPain();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A painful memory erupts!":"^S<S-NAME> invoke(s) <T-YOUPOSS> most painful memories."));
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				invoker=mob;

				int damage = 0;
				int maxDie = (target.envStats().level()+adjustedLevel(mob))/2;
				damage += Dice.roll(maxDie,6,target.charStats().getStat(CharStats.INTELLIGENCE));
				mob.location().send(mob,msg2);
				if((msg2.wasModified())||(msg.wasModified()))
					damage = (int)Math.round(Util.div(damage,2.0));

				if(target.location()==mob.location())
					ExternalPlay.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_COLD,Weapon.TYPE_BURSTING,"The horrible memory <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}