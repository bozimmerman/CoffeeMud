package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_Desecrate extends Prayer
{
	public Prayer_Desecrate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Desecrate";

		malicious=true;

		baseEnvStats().setLevel(3);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Desecrate();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		Item target=getTarget(mob,mob.location(),commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> desecrate(s) <T-NAME> before <S-HIS-HER> god.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				target.destroyThis();
				if(mob.getAlignment()<=500)
				{
					mob.tell("You receive 5 experience points for your desecration.");
					mob.charStats().getMyClass().gainExperience(mob,null,5);
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> attempt(s) to desecrate <T-NAME>, but fail(s).");

		// return whether it worked
		return success;
	}
}
