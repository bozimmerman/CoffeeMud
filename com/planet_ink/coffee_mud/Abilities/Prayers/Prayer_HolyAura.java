package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_HolyAura extends Prayer
{
	public Prayer_HolyAura()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Holy Aura";
		displayText="(Holy Aura)";

		baseEnvStats().setLevel(15);
		quality=Ability.BENEFICIAL_OTHERS;
		holyQuality=Prayer.HOLY_GOOD;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_HolyAura();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setArmor(affectableStats.armor()-20);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+10);
	}



	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your holy aura fades.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> become(s) clothed in holyness.":"<S-NAME> call(s) on <S-HIS-HER> god for <T-NAME> to be clothed in holyness.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				int a=0;
				while(a<target.numAffects())
				{
					Ability A=target.fetchAffect(a);
					if(A!=null)
					{
						int b=target.numAffects();
						if(A instanceof Prayer_Curse)
							A.unInvoke();
						else
						if(A instanceof Prayer_Bless)
							A.unInvoke();
						else
						if(A instanceof Prayer_GreatCurse)
							A.unInvoke();
						else
						if(A instanceof Prayer_HolyWord)
							A.unInvoke();
						if(b==target.numAffects())
							a++;
					}
					else
						a++;
				}
				target.recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> call(s) on <S-HIS-HER> god for holy blessing, but nothing happens.");


		// return whether it worked
		return success;
	}
}
