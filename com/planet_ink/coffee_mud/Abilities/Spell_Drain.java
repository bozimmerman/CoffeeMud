package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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


		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(17);

		addQualifyingClass(new Mage().ID(),17);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Drain();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;


		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> reach(es) at <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int damage = 0;
					int maxDie =  (int)Math.round(Util.div(mob.envStats().level(),2.0));
					if (maxDie > 10)
						maxDie = 10;
					damage += Dice.roll(maxDie,5,1);

					mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The draining grasp of <S-NAME> "+TheFight.hitWord(-1,damage)+" <T-NAME>.");
					TheFight.doDamage(target, damage);
					mob.curState().adjHitPoints(damage,mob.maxState());
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> reach(es) for <T-NAME>, but the spell fades.");


		// return whether it worked
		return success;
	}
}