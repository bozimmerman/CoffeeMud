package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Thief_RemoveTraps extends ThiefSkill
{
	public String ID() { return "Thief_RemoveTraps"; }
	public String name(){ return "Remove Traps";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"DETRAP","UNTRAP","REMOVETRAPS"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental lastChecked=null;
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String whatTounlock=Util.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		Room nextRoom=null;
		if(dirCode>=0)
		{
			nextRoom=mob.location().getRoomInDir(dirCode);
			unlockThis=mob.location().getExitInDir(dirCode);
		}
		if((unlockThis==null)&&(whatTounlock.equalsIgnoreCase("room")||whatTounlock.equalsIgnoreCase("here")))
			unlockThis=mob.location();
		if(unlockThis==null)
			unlockThis=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(unlockThis==null) return false;
		int oldProfficiency=profficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,+((mob.envStats().level()
											 -unlockThis.envStats().level())*3),auto);
		Trap theTrap=CoffeeUtensils.fetchMyTrap(unlockThis);
		Trap opTrap=null;
		if(unlockThis instanceof Exit)
		{
			if(dirCode<0)
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(mob.location().getExitInDir(d)==unlockThis){ dirCode=d; break;}
			if(dirCode>=0)
			{
				Exit exit=mob.location().getReverseExit(dirCode);
				if(exit!=null)
					opTrap=CoffeeUtensils.fetchMyTrap(exit);
				Trap roomTrap=null;
				if(nextRoom!=null) roomTrap=CoffeeUtensils.fetchMyTrap(nextRoom);
				if((theTrap!=null)&&(theTrap.disabled())&&(roomTrap!=null))
				{
					opTrap=null;
					unlockThis=nextRoom;
					theTrap=roomTrap;
				}
			}
		}
		FullMsg msg=new FullMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_ACTION,auto?unlockThis.name()+" begins to glow.":"<S-NAME> attempt(s) to safely deactivate a trap on "+unlockThis.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if((unlockThis==lastChecked)&&((theTrap==null)||(theTrap.disabled())))
				setProfficiency(oldProfficiency);
			if(success)
			{
				if(theTrap!=null)
					theTrap.disable();
				if(opTrap!=null)
					opTrap.disable();
			}
			if(!auto)
				mob.tell("You have completed your attempt.");
			lastChecked=unlockThis;
		}

		return success;
	}
}
