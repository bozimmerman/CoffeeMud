package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_MassHeal extends Prayer
{
	public Prayer_MassHeal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Heal";
		baseEnvStats().setLevel(22);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_MassHeal();
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
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAME>.");
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					int healing=Dice.roll(mob.envStats().level(),5,mob.envStats().level()*5);
					target.curState().adjHitPoints(healing,target.maxState());
					target.tell("You feel tons better!");
				}
			}
			else
			{
				// it didn't work, but tell everyone you tried.
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAME>, but <S-HIS-HER> god does not heed.");
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
		}

		// return whether it worked
		return success;
	}
}
