package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_DetectTraps extends ThiefSkill
{
	public String ID() { return "Thief_DetectTraps"; }
	public String name(){ return "Detect Traps";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CHECK"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_DetectTraps();	}
	private Environmental lastChecked=null;

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatTounlock=Util.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(unlockThis==null) return false;

		int oldProfficiency=profficiency();
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()
											 -unlockThis.envStats().level())*3),auto);
		Trap theTrap=CMClass.fetchMyTrap(unlockThis);
		if(unlockThis instanceof Exit)
		{
			dirCode=ExternalPlay.getMyDirCode((Exit)unlockThis,mob.location(),dirCode);
			if(dirCode>=0)
			{
				Exit exit=mob.location().getReverseExit(dirCode);
				Trap opTrap=null;
				if(exit!=null) opTrap=CMClass.fetchMyTrap(exit);
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
		FullMsg msg=new FullMsg(mob,unlockThis,this,auto?Affect.MSG_OK_ACTION:Affect.MSG_DELICATE_HANDS_ACT,auto?"":"<S-NAME> look(s) "+unlockThis.name()+" over very carefully.");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			if((unlockThis==lastChecked)&&((theTrap==null)||(theTrap.sprung())))
				setProfficiency(oldProfficiency);
			if((!success)||(theTrap==null))
				mob.tell("You don't find any traps on "+unlockThis.name()+".");
			else
			{
				if(theTrap.sprung())
					mob.tell(unlockThis.name()+" is trapped, but the trap looks safely sprung.");
				else
					mob.tell(unlockThis.name()+" definitely looks trapped.");
			}
			lastChecked=unlockThis;
		}

		return success;
	}
}