package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SeeAura extends Spell
{
	public Spell_SeeAura()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="See Aura";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(18);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_SeeAura();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_DIVINATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"^SYou draw out <T-NAME>s aura, seeing <T-HIM-HER> from the inside out...^?",affectType,auto?"":"^S<S-NAME> draw(s) out your aura.^?",affectType,auto?"":"^S<S-NAME> draws out <T-NAME>s aura.^?");
		if(success)
		{
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				StringBuffer str=ExternalPlay.getScore(target);
				if(!mob.isMonster())
					mob.session().unfilteredPrintln(str.toString());
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> examine(s) <T-NAME>, encanting, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}