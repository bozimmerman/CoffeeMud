package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Godstrike extends Prayer
{
	public Prayer_Godstrike()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Godstrike";

		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_GOOD;
		baseEnvStats().setLevel(19);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Godstrike();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if((success)&&(target.getAlignment()<350))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			Prayer_Godstrike newOne=(Prayer_Godstrike)this.copyOf();
			FullMsg msg=new FullMsg(mob,target,newOne,affectType|Affect.MASK_MALICIOUS,auto?"<T-NAME> is filled with holy fury!":"<S-NAME> invoke(s) the mighty power of <S-HIS-HER> god against the evil inside <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int harming=Dice.roll(3,mob.envStats().level(),15);
					if(target.getAlignment()<350)
					{
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"The holy spell "+ExternalPlay.standardHitWord(-1,harming)+" <S-NAME>!");
						ExternalPlay.postDamage(mob,target,this,harming);
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
