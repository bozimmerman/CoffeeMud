package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
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

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_HolyAura();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		affectableStats.setArmor(affectableStats.armor()-(mob.envStats().level()*2));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(mob.envStats().level()*2));
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

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> call(s) on <S-HIS-HER> god for <T-NAME> to be clothed in holyness.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				int a=0;
				while(a<target.numAffects())
				{
					Ability A=target.fetchAffect(a);
					int b=target.numAffects();
					if(A instanceof Prayer_Curse)
						A.unInvoke();
					else
					if(A instanceof Prayer_Bless)
						A.unInvoke();
					else
					if(A instanceof Prayer_GreatCurse)
						A.unInvoke();
					if(b==target.numAffects())
						a++;
				}
				target.recoverEnvStats();
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> call(s) on <S-HIS-HER> god for holy blessing, but nothing happens.");


		// return whether it worked
		return success;
	}
}
