package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtectHealth extends Prayer
{
	public Prayer_ProtectHealth()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Protect Health";
		displayText="(Protection of Mind and Body)";

		quality=Ability.BENEFICIAL_SELF;
		baseEnvStats().setLevel(13);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_ProtectHealth();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your bodies natural defenses take over.");
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+50);
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if(affect.target()==invoker)
		{
			if((affect.tool()!=null)
			   &&(Dice.rollPercentage()>50)
			   &&((affect.tool().ID().equals("Prayer_Plague"))
				||(affect.tool().name().toUpperCase().indexOf("DISEASE")>=0)
				||(affect.tool().name().toUpperCase().indexOf("PLAGUE")>=0)
				||(affect.tool().name().toUpperCase().indexOf("VIRUS")>=0)))
			{
				affect.source().location().show(invoker,null,Affect.MSG_OK_VISUAL,"An unhealthy assault against <S-NAME> is magically repelled.");
				return false;
			}

		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> attain(s) a healthy mind and body.":"<S-NAME> pray(s) for a healthy mind and body.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for a healthy body and mind, but nothing happens.");


		// return whether it worked
		return success;
	}
}
