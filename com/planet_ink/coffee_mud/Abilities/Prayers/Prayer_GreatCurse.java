package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_GreatCurse extends Prayer
{
	public Prayer_GreatCurse()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Great Curse";
		displayText="(Greater Curse)";
		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(15);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_GreatCurse();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		
		affectableStats.setArmor(affectableStats.armor()+20);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-10);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("The great curse is lifted.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"<T-NAME> is horribly cursed!":"<S-NAME> curse(s) <T-NAMESELF> horribly.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,-1);
					int a=0;
					while(a<target.numAffects())
					{
						Ability A=target.fetchAffect(a);
						if(A!=null)
						{
							int b=target.numAffects();
							if(A instanceof Prayer_Bless)
								A.unInvoke();
							else
							if(A instanceof Prayer_Sanctuary)
								A.unInvoke();
							else
							if(A instanceof Prayer_HolyAura)
								A.unInvoke();
							else
							if(A instanceof Prayer_Curse)
								A.unInvoke();
							else
							if(A instanceof Prayer_UnholyWord)
								A.unInvoke();
							if(b==target.numAffects())
								a++;
						}
						else
							a++;
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to horribly curse <T-NAMESELF> , but nothing happens.");


		// return whether it worked
		return success;
	}
}
