package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Spook extends Spell
{
	public Spell_Spook()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Spook";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Spooked)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(5);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Spook();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}


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
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> scare(s) <T-NAMESELF>.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND,null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().send(mob,msg2);
					if(!msg2.wasModified())
					{
						if(target.location()==mob.location())
						{
							target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> shake(s) in fear!");
							invoker=mob;
							ExternalPlay.flee(target,"");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to scare <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}