package com.planet_ink.coffee_mud.Commands;
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
public class Throw extends StdCommand
{
	public Throw(){}

	private String[] access={"THROW","TOSS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()==2)&&(mob.isInCombat()))
			commands.addElement(mob.getVictim().name()+"$");
		if(commands.size()<3)
		{
			mob.tell("Throw what, where or at whom?");
			return false;
		}
		commands.removeElementAt(0);
		String str=(String)commands.lastElement();
		commands.removeElement(str);
		String what=Util.combine(commands,0);
		Item item=mob.fetchWornItem(what);
		if(item==null) item=mob.fetchInventory(what);
		if((item==null)||(!Sense.canBeSeenBy(item,mob)))
		{
			mob.tell("You don't seem to have a '"+what+"'!");
			return false;
		}
		if((!item.amWearingAt(Item.HELD))&&(!item.amWearingAt(Item.WIELD)))
		{
			mob.tell("You aren't holding or wielding "+item.name()+"!");
			return false;
		}

		int dir=Directions.getGoodDirectionCode(str);
		Environmental target=null;
		if(dir<0)
			target=mob.location().fetchInhabitant(str);
		else
		{
			target=mob.location().getRoomInDir(dir);
			if((target==null)
			||(mob.location().getExitInDir(dir)==null)
			||(!mob.location().getExitInDir(dir).isOpen()))
			{
				mob.tell("You can't throw anything that way!");
				return false;
			}
			boolean amOutside=((mob.location().domainType()&Room.INDOORS)==0);
			boolean isOutside=((((Room)target).domainType()&Room.INDOORS)==0);
			boolean isUp=(mob.location().getRoomInDir(Directions.UP)==target);
			boolean isDown=(mob.location().getRoomInDir(Directions.DOWN)==target);

			if(amOutside&&isOutside&&(!isUp)&&(!isDown)
			&&((((Room)target).domainType()&Room.DOMAIN_OUTDOORS_AIR)==0))
			{
				mob.tell("That's too far to throw "+item.name()+".");
				return false;
			}
		}
		if((dir<0)&&((target==null)||((target!=mob.getVictim())&&(!Sense.canBeSeenBy(target,mob)))))
		{
			mob.tell("You can't target "+item.name()+" at '"+str+"'!");
			return false;
		}

		if(!(target instanceof Room))
		{
			FullMsg newMsg=new FullMsg(mob,item,null,CMMsg.MSG_REMOVE,null);
			if(mob.location().okMessage(mob,newMsg))
			{
				mob.location().send(mob,newMsg);
				FullMsg msg=new FullMsg(mob,target,item,CMMsg.MSG_THROW,CMMsg.MSG_WEAPONATTACK,CMMsg.MSG_THROW,"<S-NAME> throw(s) <O-NAME> at <T-NAMESELF>.");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,item,CMMsg.MSG_THROW,"<S-NAME> throw(s) <T-NAME> "+Directions.getInDirectionName(dir).toLowerCase()+".");
			FullMsg msg2=new FullMsg(mob,target,item,CMMsg.MSG_THROW,"<T-NAME> fl(ys) in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()+".");
			if(mob.location().okMessage(mob,msg)&&((Room)target).okMessage(mob,msg2))
			{
				mob.location().send(mob,msg);
				((Room)target).sendOthers(mob,msg2);
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
