package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Anger extends Prayer
{
	public Prayer_Anger()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Anger";

		holyQuality=Prayer.HOLY_EVIL;
		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Anger();
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

		if((success)&&(!someoneIsFighting)&&(mob.location().numInhabitants()>3))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"A feeling of anger descends":"<S-NAME> rage(s) for anger.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB inhab=mob.location().fetchInhabitant(i);
					if((inhab!=null)&&(inhab!=mob)&&(!inhab.isInCombat()))
					{
						int tries=0;
						MOB target=null;
						while((tries<100)&&(target==null))
						{
							target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
							if(target!=null)
							{
								if(target==inhab) target=null;
								if(target==mob) target=null;
							}
							tries++;
						}
						FullMsg amsg=new FullMsg(mob,inhab,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND,null);
						if((target!=null)&&(mob.location().okAffect(amsg)))
						{
							inhab.tell("You feel angry.");
							inhab.setVictim(target);
						}
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
