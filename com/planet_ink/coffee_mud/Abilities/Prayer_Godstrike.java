package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_Godstrike extends Prayer
{
	public Prayer_Godstrike()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Godstrike";

		isNeutral=true;

		baseEnvStats().setLevel(19);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Godstrike();
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
			Prayer_Godstrike newOne=(Prayer_Godstrike)this.copyOf();
			newOne.malicious=true;
			newOne.isNeutral=false;
			FullMsg msg=new FullMsg(mob,target,newOne,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) the mighty power of <S-HIS-HER> god against the evil inside <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int harming=Dice.roll(mob.envStats().level(),10,50);
					if(mob.getAlignment()<350)
					{
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The unholy spell "+TheFight.hitWord(-1,harming)+" <T-NAME>!");
						TheFight.doDamage(target,harming);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> invoke(s) <S-HIS-HER> god, but nothing happens.");


		// return whether it worked
		return success;
	}
}
