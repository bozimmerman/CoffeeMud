package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Thief_DetectTraps extends ThiefSkill
{
	public String ID() { return "Thief_DetectTraps"; }
	public String name(){ return "Detect Traps";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CHECK"};
	public String[] triggerStrings(){return triggerStrings;}
	private Environmental lastChecked=null;

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatTounlock=Util.combine(commands,0);
		Environmental unlockThis=null;
		Room nextRoom=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
		{
			unlockThis=mob.location().getExitInDir(dirCode);
			nextRoom=mob.location().getRoomInDir(dirCode);
		}
		if((unlockThis==null)&&(whatTounlock.equalsIgnoreCase("room")||whatTounlock.equalsIgnoreCase("here")))
			unlockThis=mob.location();
		if(unlockThis==null)
			unlockThis=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(unlockThis==null) return false;

		int oldProfficiency=profficiency();
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,+((mob.envStats().level()
											 -unlockThis.envStats().level())*3),auto);
		Trap theTrap=CoffeeUtensils.fetchMyTrap(unlockThis);
		if(unlockThis instanceof Exit)
		{
			if(dirCode<0)
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(mob.location().getExitInDir(d)==unlockThis){ dirCode=d; break;}
			if(dirCode>=0)
			{
				Exit exit=mob.location().getReverseExit(dirCode);
				Trap opTrap=null;
				Trap roomTrap=null;
				if(nextRoom!=null) roomTrap=CoffeeUtensils.fetchMyTrap(nextRoom);
				if(exit!=null) opTrap=CoffeeUtensils.fetchMyTrap(exit);
				if((theTrap!=null)&&(opTrap!=null))
				{
					if((theTrap.disabled())&&(!opTrap.disabled()))
						theTrap=opTrap;
				}
				else
				if((opTrap!=null)&&(theTrap==null))
					theTrap=opTrap;
				if((theTrap!=null)&&(theTrap.disabled())&&(roomTrap!=null))
				{
					opTrap=null;
					unlockThis=nextRoom;
					theTrap=roomTrap;
				}
			}
		}
		FullMsg msg=new FullMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_DELICATE_HANDS_ACT,auto?"":"<S-NAME> look(s) "+unlockThis.name()+" over very carefully.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if((unlockThis==lastChecked)&&((theTrap==null)||(theTrap.disabled())))
				setProfficiency(oldProfficiency);
			if((!success)||(theTrap==null))
				mob.tell("You don't find any traps on "+unlockThis.name()+".");
			else
			{
				if(theTrap.disabled())
					mob.tell(unlockThis.name()+" is trapped, but the trap looks disabled for the moment.");
				else
				if(theTrap.sprung())
					mob.tell(unlockThis.name()+" is trapped, and the trap looks sprung.");
				else
					mob.tell(unlockThis.name()+" definitely looks trapped.");
			}
			lastChecked=unlockThis;
		}

		return success;
	}
}
