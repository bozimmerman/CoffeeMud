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
public class Activate extends BaseItemParser
{
	public Activate(){}

	private String[] access={"ACTIVATE","ACT","A",">"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Activate what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,what,Item.WORN_REQ_ANY);
		Item item=null;
		if(mob.riding() instanceof Electronics)
		{
		    if((E==null)||(cmd.equalsIgnoreCase("$")))
		        item=(Item)mob.riding();
		}
		else
		    commands.removeElementAt(commands.size()-1);
		if((item==null)&&(E instanceof Electronics))
		    item=(Item)E;
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
			mob.tell("You don't see anything called '"+what+"' here that you can activate.");
		else
		if(item==null)
			mob.tell("You can't activate '"+E.name()+"'.");
		
		String rest=Util.combine(commands,0);
		FullMsg newMsg=new FullMsg(mob,item,null,CMMsg.MSG_ACTIVATE,null,CMMsg.MSG_ACTIVATE,rest,CMMsg.MSG_ACTIVATE,null);
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
