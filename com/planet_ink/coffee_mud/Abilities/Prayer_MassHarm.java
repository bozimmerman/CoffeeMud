package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_MassHarm extends Prayer
{
	public Prayer_MassHarm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Harm";

		malicious=true;
		baseEnvStats().setLevel(22);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_MassHarm();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB target=mob.location().fetchInhabitant(i);

			if(success)
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAME>.");
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					int harming=Dice.roll(mob.envStats().level(),5,mob.envStats().level()*5);
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The spell "+TheFight.hitWord(-1,harming)+" <T-NAME>!");
					TheFight.doDamage(target,harming);
				}
			}
			else
				maliciousFizzle(mob,target,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAME>, but <S-HIS-HER> god does not heed.");
		}


		// return whether it worked
		return success;
	}
}
