package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_Frost extends Spell
	implements ElementalDevotion
{
	public Spell_Frost()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Frost";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Frost)";

		quality=Ability.MALICIOUS;

		uses=Integer.MAX_VALUE;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(4);
		maxRange=1;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Frost();
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

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,(auto?"A ":"<S-NAME> incant(s) and point(s) at <T-NAMESELF>. A ")+"blast of frost errupts!");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_COLD,null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					invoker=mob;

					int damage = 0;
					int maxDie =  mob.envStats().level();
					if (maxDie > 15)
						maxDie = 15;
					damage += Dice.roll(maxDie,3,3);
					mob.location().send(mob,msg2);
					if(!msg2.wasModified())
						damage = (int)Math.round(Util.div(damage,2.0));

					if(target.location()==mob.location())
					{
						target.location().show(target,null,Affect.MSG_OK_ACTION,"The frost "+ExternalPlay.standardHitWord(-1,damage)+" <S-NAME>!");
						ExternalPlay.postDamage(mob,target,this,damage);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) and point(s) at <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}