package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtectElements extends Prayer
{
	public Prayer_ProtectElements()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Protect Elements";
		displayText="(Protection from Elements)";

		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(18);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_ProtectElements();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your elemental protection fades.");
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
			if((affect.targetMinor()==Affect.TYP_WATER)
			||(affect.targetMinor()==Affect.TYP_COLD)
			||(affect.targetMinor()==Affect.TYP_FIRE)
			||(affect.targetMinor()==Affect.TYP_GAS))
			{
				affect.source().location().show(invoker,null,Affect.MSG_OK_VISUAL,"The holy field around <S-NAME> protects <S-HIS-HER> body from elemental assaults.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> attain(s) elemental protection.":"<S-NAME> pray(s) for elemental protection.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for elemental protection, but nothing happens.");


		// return whether it worked
		return success;
	}
}
