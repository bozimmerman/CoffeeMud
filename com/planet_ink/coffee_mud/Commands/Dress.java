package com.planet_ink.coffee_mud.Commands;
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
public class Dress extends StdCommand
{
	public Dress(){}

	private String[] access={"DRESS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Dress whom in what?");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are in combat!");
			return false;
		}
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		commands.removeElement(what);
		String whom=Util.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see "+whom+" here.");
			return false;
		}
		if((!target.isMonster())&&(!CMSecurity.isAllowedEverywhere(mob,"ORDER")))
		{
			mob.tell(target.Name()+" is a player!");
			return false;
		}
		if((target.willFollowOrdersOf(mob))||(Sense.isBoundOrHeld(target)))
		{
			Item item=mob.fetchInventory(null,what);
			if((item==null)||(!Sense.canBeSeenBy(item,mob)))
			{
				mob.tell("I don't see "+what+" here.");
				return false;
			}
			if(CMSecurity.isAllowed(mob,mob.location(),"ORDER")
			||(CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")&&(target.isMonster()))
			||(CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")&&(target.isMonster())))
			{
				mob.location().show(mob,target,item,CMMsg.MASK_GENERAL|CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> mystically put(s) <O-NAME> on <T-NAMESELF>.");
				item.unWear();
				target.giveItem(item);
				item.wearIfPossible(target);
				if((item.rawProperLocationBitmap()!=0)&&(item.amWearingAt(Item.INVENTORY))&&(target.isMonster()))
				{
					if(item.rawLogicalAnd())
						item.wearAt(item.rawProperLocationBitmap());
					else
					{
						for(int i=0;i<20;i++)
						{
							long wornCode=1<<i;
							if(item.fitsOn(wornCode)&&(wornCode!=Item.HELD))
							{ item.wearAt(wornCode); break;}
						}
						if(item.amWearingAt(Item.INVENTORY))
							item.wearAt(Item.HELD);
					}
				}
				target.location().recoverRoomStats();
			}
			else
			{
				if(!item.amWearingAt(Item.INVENTORY))
				{
					mob.tell("You might want to remove that first.");
					return false;
				}
				if(item instanceof Coins)
				{
				    mob.tell("I don't think you want to dress someone in "+item.name()+".");
				    return false;
				}
				if(target.isInCombat())
				{
					mob.tell("Not while "+target.name()+" is in combat!");
					return false;
				}
				FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_QUIETMOVEMENT,null);
				if(mob.location().okMessage(mob,msg))
				{
					if(CommonMsgs.drop(mob,item,true,false))
					{
						msg=new FullMsg(target,item,null,CMMsg.MASK_GENERAL|CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_GET,null);
						if(mob.location().okMessage(mob,msg))
						{
							mob.location().send(mob,msg);
							msg=new FullMsg(target,item,null,CMMsg.MASK_GENERAL|CMMsg.MSG_WEAR,CMMsg.MSG_WEAR,CMMsg.MSG_WEAR,null);
							if(mob.location().okMessage(mob,msg))
							{
								mob.location().send(mob,msg);
								mob.location().show(mob,target,item,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> put(s) <O-NAME> on <T-NAMESELF>.");
							}
							else
								mob.tell("You cannot seem to get "+item.name()+" on "+target.name()+".");
						}
						else
							mob.tell("You cannot seem to get "+item.name()+" to "+target.name()+".");
					}
				}
			}
		}
		else
			mob.tell(target.name()+" won't let you.");
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
