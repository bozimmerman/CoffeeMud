package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Ambush extends ThiefSkill
{
	public String ID() { return "Thief_Ambush"; }
	public String name(){ return "Ambush";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"AMBUSH"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Ambush();}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchEffect("Thief_Hide")!=null)
		{
			mob.tell("You are already hiding.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Hashtable H=mob.getGroupMembers(new Hashtable());
		if(!H.contains(mob)) H.put(mob,mob);
		int numBesidesMe=0;
		for(Enumeration e=H.elements();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if((M!=mob)&&(mob.location().isInhabitant(M)))
				numBesidesMe++;
		}
		if(numBesidesMe==0)
		{
			mob.tell("You need a group to set up an ambush!");
			return false;
		}
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&(M!=mob)&&(!H.contains(M))&&(Sense.canSee(M)))
			{
				mob.tell(M,null,null,"<S-NAME> is watching you too closely.");
				return false;
			}
		}
		boolean success=profficiencyCheck(0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to set up an ambush, but fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),"<S-NAME> set(s) up an ambush, directing everyone to hiding places.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Ability hide=CMClass.getAbility("Thief_Hide");
				for(Enumeration e=H.elements();e.hasMoreElements();)
				{
					MOB M=(MOB)e.nextElement();
					hide.invoke(M,M,true);
				}
			}
			else
				success=false;
		}
		return success;
	}
}
