package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_DispelEvil extends Prayer
{
	public Prayer_DispelEvil()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dispel Evil";

		isNeutral=true;

		baseEnvStats().setLevel(9);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_DispelEvil();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if((success)&&(target.getAlignment()<350))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			Prayer_DispelEvil newOne=(Prayer_DispelEvil)this.copyOf();
			newOne.malicious=true;
			newOne.isNeutral=false;
			FullMsg msg=new FullMsg(mob,target,newOne,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> exercise(s) the evil inside <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int harming=Dice.rollPercentage()+1;
					if(mob.getAlignment()<350)
					{
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The wicked spell "+TheFight.hitWord(-1,harming)+" <T-NAME>!");
						TheFight.doDamage(target,harming);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> exercise(s) <T-NAME>, but nothing emerges.");


		// return whether it worked
		return success;
	}
}
