package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_Anger extends Prayer
{
	public Prayer_Anger()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Anger";

		malicious=true;

		baseEnvStats().setLevel(16);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Anger();
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

		if((success)&&(!someoneIsFighting)&&(mob.location().numInhabitants()>1))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> rage(s) for anger.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
					if((!mob.location().fetchInhabitant(i).isInCombat())
					&&(mob.location().fetchInhabitant(i)!=mob))
					{
						int tries=0;
						MOB target=null;
						while((tries<100)&&(target==null))
						{
							target=mob.location().fetchInhabitant((int)Math.round(Math.random()*mob.location().numInhabitants()));
							if(target==mob) target=null;
							tries++;
						}
						if(target!=null)
						{
							mob.location().fetchInhabitant(i).tell("You feel angry.");
							mob.setVictim(target);
						}
					}
			}
		}
		else
			maliciousFizzle(mob,null,"<S-NAME> pray(s) for rage, but nothing happens.");


		// return whether it worked
		return success;
	}
}
