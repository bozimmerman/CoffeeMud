package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Poison extends ThiefSkill
{
	// **
	// This is a deprecated skill provided only
	// for backwards compatibility.  It has been
	// replaced with Thief_UsePoison
	// **
	public String ID() { return "Thief_Poison"; }
	public String name(){ return "Deprecated Poison";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"DOPOISON"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Poison();}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			str=auto?"":"^F<S-NAME> attempt(s) to poison <T-NAMESELF>!^?";
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,str,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.MSG_THIEF_ACT|(auto?CMMsg.MASK_GENERAL:0),str,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					Ability A=CMClass.getAbility("Poison");
					A.invoke(mob,target,true);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).");

		return success;
	}
}
