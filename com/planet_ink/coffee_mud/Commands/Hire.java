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
public class Hire extends StdCommand
{
	public Hire(){}

	private String[] access={"HIRE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String rest=Util.combine(commands,1);
		Environmental target=mob.location().fetchFromRoomFavorMOBs(null,rest,Item.WORN_REQ_ANY);
		if((target!=null)&&(!target.name().equalsIgnoreCase(rest))&&(rest.length()<4))
		   target=null;
		if((target!=null)&&(!Sense.canBeSeenBy(target,mob)))
			target=null;
		FullMsg msg=null;
		if(target==null)
			msg=new FullMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'I'm looking to hire some help.'^?");
		else
			msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) to <T-NAMESELF> 'Are you for hire?'^?");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
