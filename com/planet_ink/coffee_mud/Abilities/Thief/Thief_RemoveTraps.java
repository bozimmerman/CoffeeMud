package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_RemoveTraps extends ThiefSkill
{

	public Thief_RemoveTraps()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Remove Traps";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("DETRAP");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(9);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_DetectTraps();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatTounlock=Util.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().exits()[dirCode];
		if(unlockThis==null)
			unlockThis=getTarget(mob,mob.location(),givenTarget,commands);
		if(unlockThis==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()-unlockThis.envStats().level()-envStats().level())*3),auto);
		Trap theTrap=new Trap_Trap().fetchMyTrap(unlockThis);
		Trap opTrap=null;
		if(unlockThis instanceof Exit)
		{
			dirCode=ExternalPlay.getMyDirCode((Exit)unlockThis,mob.location(),dirCode);
			if(dirCode>=0)
			{
				Exit exit=mob.location().getReverseExit(dirCode);
				opTrap=new Trap_Trap().fetchMyTrap(exit);
			}
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,auto?Affect.MSG_OK_ACTION:Affect.MSG_DELICATE_HANDS_ACT,Affect.MSG_DELICATE_HANDS_ACT,Affect.MSG_OK_ACTION,auto?unlockThis.name()+" begins to glow.":"<S-NAME> attempt(s) to safely deactivate a trap on "+unlockThis.name()+".");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				if(theTrap!=null)
					theTrap.setSprung(true);
				if(opTrap!=null)
					opTrap.setSprung(true);
			}


			if(!auto)
				mob.tell("You have completed your attempt.");
		}

		return success;
	}
}