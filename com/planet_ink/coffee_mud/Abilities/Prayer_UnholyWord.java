package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_UnholyWord extends Prayer
{
	public Prayer_UnholyWord()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Unholy Word";
		displayText="(Unholy Word)";
		malicious=true;
		baseEnvStats().setLevel(23);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_UnholyWord();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		if(mob==invoker) return;

		if(mob.getAlignment()<350)
		{
			affectableStats.setArmor(affectableStats.armor()-(mob.envStats().level()*5));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(mob.envStats().level()*5));
		}
		else
		if(mob.getAlignment()>650)
		{
			affectableStats.setArmor(affectableStats.armor()+(mob.envStats().level()*5));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(mob.envStats().level()*5));
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


	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		String str="<S-NAME> speak(s) the unholy word of <S-HIS-HER> god to <T-NAME>.";
		String missStr="<S-NAME> speak(s) the unholy word of <S-HIS-HER> god, but nothing happens.";

		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB target=mob.location().fetchInhabitant(i);
			int targetType=Affect.SOUND_MAGIC;
			if(target.getAlignment()>650)
				targetType=Affect.STRIKE_MAGIC;

			if(success)
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,targetType,Affect.SOUND_MAGIC,str);
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						beneficialAffect(mob,target,0);
						int a=0;
						while(a<target.numAffects())
						{
							Ability A=target.fetchAffect(a);
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
							if(A instanceof Prayer_HolyWord)
								A.unInvoke();
							if(b==target.numAffects())
								a++;
						}
						target.recoverEnvStats();
					}
				}
			}
			else
			{
				if(targetType==Affect.STRIKE_MAGIC)
					maliciousFizzle(mob,target,"<S-NAME> attempt(s) to speak the unholy word to <T-NAME>, but flub(s) it.");
				else
					beneficialFizzle(mob,target,"<S-NAME> attempt(s) to speak the unholy word to <T-NAME>, but flub(s) it.");
				missStr=null;
				return false;
			}
		}


		// return whether it worked
		return success;
	}
}
