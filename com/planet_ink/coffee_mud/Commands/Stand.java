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
public class Stand extends StdCommand
{
	public Stand(){}

	private String[] access={"STAND","ST","STA","STAN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean ifnecessary=((commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("IFNECESSARY")));

		if((!Sense.isSitting(mob))&&(!Sense.isSleeping(mob)))
		{
			if(!ifnecessary)
				mob.tell(getScr("Movement","standerr1"));
		}
		else
		if(mob.location()!=null)
		{
			FullMsg msg=new FullMsg(mob,null,null,CMMsg.MSG_STAND,mob.amDead()?null:getScr("Movement","standup"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
