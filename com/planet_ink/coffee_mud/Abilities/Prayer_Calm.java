package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_Calm extends Prayer
{
	public Prayer_Calm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Calm";

		baseEnvStats().setLevel(16);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Calm();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		boolean someoneIsFighting=false;
		for(int i=0;i<mob.location().numInhabitants();i++)
			if(mob.location().fetchInhabitant(i).isInCombat())
				someoneIsFighting=true;

		if((success)&&(someoneIsFighting))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> pray(s) for calmness.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
					if(mob.location().fetchInhabitant(i).isInCombat())
					{
						mob.location().fetchInhabitant(i).tell("You feel at peace.");
						mob.location().fetchInhabitant(i).makePeace();
					}
			}
		}
		else
			beneficialFizzle(mob,null,"<S-NAME> pray(s) for calmness, but nothing happens.");


		// return whether it worked
		return success;
	}
}
