package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtDisease extends Prayer
{
	public Prayer_ProtDisease()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Protection Disease";
		displayText="(Protection from Disease)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		quality=Ability.BENEFICIAL_SELF;
		baseEnvStats().setLevel(13);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_ProtDisease();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("Your natural defenses against disease take over.");
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_DISEASE,affectedStats.getStat(CharStats.SAVE_DISEASE)+100);
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
			   &&((affect.targetMinor()==Affect.TYP_DISEASE)))
			{
				affect.source().location().show(invoker,null,Affect.MSG_OK_VISUAL,"<S-NAME> magically repells the disease.");
				return false;
			}

		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You already have protection from disease.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> attain(s) disease protection.":"^S<S-NAME> pray(s) for protection from diseases.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for protection from diseases, but go(es) unanswered.");


		// return whether it worked
		return success;
	}
}
