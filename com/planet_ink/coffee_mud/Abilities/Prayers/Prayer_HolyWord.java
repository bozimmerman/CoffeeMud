package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_HolyWord extends Prayer
{
	public Prayer_HolyWord()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Holy Word";
		displayText="(Holy Word)";

		holyQuality=Prayer.HOLY_GOOD;
		baseEnvStats().setLevel(23);

		addQualifyingClass("Cleric",baseEnvStats().level());
		addQualifyingClass("Paladin",baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_HolyWord();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		if(mob==invoker) return;
		if(mob.getAlignment()>650)
		{
			affectableStats.setArmor(affectableStats.armor()-30);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20);
		}
		else
		if(mob.getAlignment()<350)
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

		mob.tell("Your blinding holy aura fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=auto?"The holy word is spoken.":"<S-NAME> speak(s) the holy word of <S-HIS-HER> god to <T-NAMESELF>.";
		String missStr="<S-NAME> speak(s) the holy word of <S-HIS-HER> god, but nothing happens.";
		Room room=mob.location();
		if(room!=null)
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB target=room.fetchInhabitant(i);
			if(target==null) break;
			
			affectType=Affect.MSG_CAST_VERBAL_SPELL;
			if(auto) affectType=affectType|Affect.ACT_GENERAL;
			if(target.getAlignment()<350)
				affectType=affectType|Affect.MASK_MALICIOUS;

			if(success)
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,str);
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						if(Sense.canBeHeardBy(mob,target))
						{
							str=null;
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
									if(A instanceof Prayer_HolyAura)
										A.unInvoke();
									else
									if(A instanceof Prayer_GreatCurse)
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
							target.recoverEnvStats();
						}
						else
						if(Util.bset(affectType,Affect.MASK_MALICIOUS))
							maliciousFizzle(mob,target,"<T-NAME> did not hear the word!");
						else
							beneficialWordsFizzle(mob,target,"<T-NAME> did not hear the word!");

					}
				}
			}
			else
			{
				if(Util.bset(affectType,Affect.MASK_MALICIOUS))
					maliciousFizzle(mob,target,missStr);
				else
					beneficialWordsFizzle(mob,target,missStr);
				missStr=null;
				return false;
			}
		}


		// return whether it worked
		return success;
	}
}