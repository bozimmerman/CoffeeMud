package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Blindness extends Spell
{
	public Spell_Blindness()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Blindness";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Blindness)";

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(9);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Blindness();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("Your vision returns.");
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
			invoker=mob;
			String autoStr="A flashing light blazes at <T-NAME>!";
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?autoStr:"You invoke a flashing light into <T-NAME>s eyes.",affectType,auto?autoStr:"<S-NAME> invoke(s) a flashing light into your eyes.",Affect.MSG_CAST_ATTACK_VERBAL_SPELL,auto?autoStr:"<S-NAME> invokes a flashing light into <T-NAME>s eyes.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> go(es) blind!");
					success=maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}