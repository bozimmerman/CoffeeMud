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
public class Sleep extends StdCommand
{
	public Sleep(){}

	private String[] access={"SLEEP","SL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(Sense.isSleeping(mob))
		{
			mob.tell(getScr("Movement","sleeperr1"));
			return false;
		}
		if(commands.size()<=1)
		{
			FullMsg msg=new FullMsg(mob,null,null,CMMsg.MSG_SLEEP,getScr("Movement","sleep"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",possibleRideable));
			return false;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=getScr("Movement","sleepmounton",((Rideable)E).mountString(CMMsg.TYP_SLEEP,mob));
		else
			mountStr=getScr("Movement","sleepson");
		String sourceMountStr=null;
		if(!Sense.canBeSeenBy(E,mob))
			sourceMountStr=mountStr;
		else
		{
			sourceMountStr=Util.replaceAll(mountStr,"<T-NAME>",E.name());
			sourceMountStr=Util.replaceAll(sourceMountStr,"<T-NAMESELF>",E.name());
		}
		FullMsg msg=new FullMsg(mob,E,null,CMMsg.MSG_SLEEP,sourceMountStr,mountStr,mountStr);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
