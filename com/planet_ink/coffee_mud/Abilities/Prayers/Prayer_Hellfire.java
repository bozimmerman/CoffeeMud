package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Hellfire extends Prayer
{
	public Prayer_Hellfire()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hellfire";

		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(19);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Hellfire();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if((success)&&(target.getAlignment()>650))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			Prayer_Hellfire newOne=(Prayer_Hellfire)this.copyOf();
			FullMsg msg=new FullMsg(mob,target,newOne,affectType|Affect.MASK_MALICIOUS,auto?"":"<S-NAME> invoke(s) the rage of <S-HIS-HER> god against the good inside <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int harming=Dice.roll(3,mob.envStats().level(),15);
					if(target.getAlignment()>650)
					{
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"The unholy spell "+ExternalPlay.hitWord(-1,harming)+" <S-NAME>!");
						ExternalPlay.postDamage(mob,target,this,harming);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> invoke(s) the rage of <S-HIS-HER> god, but nothing emerges.");


		// return whether it worked
		return success;
	}
}
