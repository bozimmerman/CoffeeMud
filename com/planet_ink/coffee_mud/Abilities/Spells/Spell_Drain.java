package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_Drain extends Spell
	implements EvocationDevotion, InvocationDevotion
{
	public Spell_Drain()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Drain";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Drain)";


		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(17);

		addQualifyingClass("Mage",17);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Drain();
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_UNDEAD,null);
			FullMsg msg2=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> reach(es) at <T-NAMESELF>!");
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg2);
				mob.location().send(mob,msg);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					int damage = 0;
					int maxDie =  (int)Math.round(Util.div(mob.envStats().level(),4.0));
					if (maxDie > 5)
						maxDie = 5;
					damage += Dice.roll(maxDie,5,1);

					mob.location().show(mob,target,Affect.MSG_OK_ACTION,auto?"<T-NAME> shudder(s) in a draining magical wake.":"The draining grasp "+ExternalPlay.hitWord(-1,damage)+" <T-NAME>.");
					ExternalPlay.postDamage(mob,target,this,damage);
					mob.curState().adjHitPoints(damage,mob.maxState());
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> reach(es) for <T-NAMESELF>, but the spell fades.");


		// return whether it worked
		return success;
	}
}