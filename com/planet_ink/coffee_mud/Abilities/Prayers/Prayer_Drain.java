package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Drain extends Prayer
{
	public Prayer_Drain()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Drain";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Drain)";

		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(17);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Drain();
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_UNDEAD|(auto?Affect.ACT_GENERAL:0),null);
			FullMsg msg2=new FullMsg(mob,target,this,affectType,auto?"":"^S<S-NAME> reach(es) at <T-NAMESELF>, praying!^?");
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg2);
				mob.location().send(mob,msg);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					int damage = 0;
					int maxDie =  (int)Math.round(Util.div(adjustedLevel(mob),4.0));
					if (maxDie > 5)
						maxDie = 5;
					damage += Dice.roll(maxDie,6,1);

					ExternalPlay.postDamage(mob,target,this,damage,Affect.ACT_GENERAL|Affect.TYP_UNDEAD,Weapon.TYPE_BURSTING,auto?"<T-NAME> shudder(s) in a draining magical wake.":"The draining grasp <DAMAGE> <T-NAME>.");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> reach(es) for <T-NAMESELF>, praying, but the spell fades.");


		// return whether it worked
		return success;
	}
}