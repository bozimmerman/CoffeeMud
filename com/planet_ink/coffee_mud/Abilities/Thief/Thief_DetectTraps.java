package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_DetectTraps extends ThiefSkill
{

	public Thief_DetectTraps()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Detect Traps";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("CHECK");
		quality=Ability.OK_SELF;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(6);

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
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=getTarget(mob,mob.location(),givenTarget,commands);
		if(unlockThis==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()-unlockThis.envStats().level()-envStats().level())*3),auto);
		Trap theTrap=new Trap_Trap().fetchMyTrap(unlockThis);
		if(unlockThis instanceof Exit)
		{
			dirCode=ExternalPlay.getMyDirCode((Exit)unlockThis,mob.location(),dirCode);
			if(dirCode>=0)
			{
				Exit exit=mob.location().getReverseExit(dirCode);
				Trap opTrap=new Trap_Trap().fetchMyTrap(exit);
				if((theTrap!=null)&&(opTrap!=null))
				{
					if((theTrap.sprung())&&(!opTrap.sprung()))
						theTrap=opTrap;
				}
				else
				if((opTrap!=null)&&(theTrap==null))
					theTrap=opTrap;
			}
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,auto?Affect.MSG_OK_ACTION:Affect.MSG_DELICATE_HANDS_ACT,auto?"":"<S-NAME> look(s) "+unlockThis.name()+" over very carefully.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if((!success)||(theTrap==null))
				mob.tell("You don't find any traps on "+unlockThis.name()+".");
			else
			{
				if(theTrap.sprung())
					mob.tell(unlockThis.name()+" is trapped, but the trap looks safely sprung.");
				else
					mob.tell(unlockThis.name()+" definitely looks trapped.");
			}
		}

		return success;
	}
}