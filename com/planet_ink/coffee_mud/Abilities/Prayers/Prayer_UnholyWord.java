package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_UnholyWord extends Prayer
{
	public Prayer_UnholyWord()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Unholy Word";
		displayText="(Unholy Word)";
		baseEnvStats().setLevel(23);
		holyQuality=Prayer.HOLY_EVIL;

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_UnholyWord();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		if(mob==invoker) return;

		if(mob.getAlignment()<350)
		{
			affectableStats.setArmor(affectableStats.armor()-30);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20);
		}
		else
		if(mob.getAlignment()>650)
		{
			affectableStats.setArmor(affectableStats.armor()+30);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-20);
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("The unholy word has been spoken.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		String str=auto?"The unholy word is spoken.":"<S-NAME> speak(s) the unholy word of <S-HIS-HER> god to <T-NAMESELF>.";
		String missStr="<S-NAME> speak(s) the unholy word of <S-HIS-HER> god, but nothing happens.";

		Room room=mob.location();
		if(room!=null)
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB target=room.fetchInhabitant(i);
			if(target==null) break;
			affectType=Affect.MSG_CAST_VERBAL_SPELL;
			if(auto) affectType=affectType|Affect.ACT_GENERAL;
			if(target.getAlignment()>650)
				affectType=affectType|Affect.MASK_MALICIOUS;

			if(success)
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,str);
				if(room.okAffect(msg))
				{
					room.send(mob,msg);
					if(!msg.wasModified())
					{
						if(Sense.canBeHeardBy(mob,target))
						{
							Item I=Prayer_Curse.getSomething(mob,true);
							if(I!=null)
							{
								Prayer_Curse.endIt(I,2);
								I.recoverEnvStats();
							}
							Prayer_Curse.endIt(target,2);
							beneficialAffect(mob,target,0);
							target.recoverEnvStats();
						}
						else
						if(Util.bset(affectType,Affect.MASK_MALICIOUS))
							maliciousFizzle(mob,target,"<T-NAME> did not hear the unholy word!");
						else
							beneficialWordsFizzle(mob,target,"<T-NAME> did not hear the unholy word!");
					}
				}
			}
			else
			{
				if(Util.bset(affectType,Affect.MASK_MALICIOUS))
					maliciousFizzle(mob,target,"<S-NAME> attempt(s) to speak the unholy word to <T-NAMESELF>, but flub(s) it.");
				else
					beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to speak the unholy word to <T-NAMESELF>, but flub(s) it.");
				missStr=null;
				return false;
			}
		}


		// return whether it worked
		return success;
	}
}
