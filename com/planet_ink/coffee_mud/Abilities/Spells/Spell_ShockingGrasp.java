package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ShockingGrasp extends Spell
{
	public Spell_ShockingGrasp()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Shocking Grasp";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Shocking Grasp spell)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(6);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ShockingGrasp();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_EVOCATION;
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> grab(s) at <T-NAMESELF>.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_ELECTRIC|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().send(mob,msg2);
					if(!msg2.wasModified())
					{
						invoker=mob;
						int damage = Dice.roll(1,8,mob.envStats().level()*2);
						ExternalPlay.postDamage(mob,target,this,damage,Affect.ACT_GENERAL|Affect.TYP_ELECTRIC,Weapon.TYPE_BURNING,auto?"<T-NAME> gasp(s) in shock and pain!":"The shocking grasp <DAMAGE> <T-NAME>!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> grab(s) at <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
