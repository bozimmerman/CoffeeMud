package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Bless extends Prayer
{
	public Prayer_Bless()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Bless";
		displayText="(Blessed)";

		canAffectCode=Ability.CAN_MOBS|Ability.CAN_ITEMS;
		canTargetCode=Ability.CAN_MOBS|Ability.CAN_ITEMS;
		
		baseEnvStats().setLevel(7);
		quality=Ability.BENEFICIAL_OTHERS;
		holyQuality=Prayer.HOLY_GOOD;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Bless();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOOD);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
		if(affected instanceof MOB)
			affectableStats.setArmor(affectableStats.armor()-10);
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()+1);
	}



	public void unInvoke()
	{


		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if((affected instanceof Item)&&(((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
				((MOB)((Item)affected).owner()).tell("The blessing on "+((Item)affected).name()+" fades.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		mob.tell("Your aura of blessing fades.");
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> appear(s) blessed!":"<S-NAME> invoke(s) <S-HIS-HER> god's power to bless <T-NAMESELF>.");
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
						if(A instanceof Prayer_HolyWord)
							A.unInvoke();
						else
						if(A instanceof Prayer_HolyAura)
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
			return beneficialWordsFizzle(mob,target,"<S-NAME> call(s) on <S-HIS-HER> god for blessings, but nothing happens.");
		// return whether it worked
		return success;
	}
}
