package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Calm extends Prayer
{
	public Prayer_Calm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Calm";

		canAffectCode=0;
		canTargetCode=0;
		
		baseEnvStats().setLevel(16);
		quality=Ability.BENEFICIAL_OTHERS;
		holyQuality=Prayer.HOLY_GOOD;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Calm();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		boolean someoneIsFighting=false;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB inhab=mob.location().fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
				someoneIsFighting=true;
		}

		if((success)&&(someoneIsFighting))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"A feeling of calmness descends.":"^S<S-NAME> pray(s) for calmness.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB inhab=mob.location().fetchInhabitant(i);
					if((inhab!=null)&&(inhab.isInCombat()))
					{
						inhab.tell("You feel at peace.");
						inhab.makePeace();
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> pray(s) for calmness, but nothing happens.");


		// return whether it worked
		return success;
	}
}
