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
public class Serve extends StdCommand
{
	public Serve(){}

	private String[] access={"SERVE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Serve whom?");
			return false;
		}
		commands.removeElementAt(0);
		MOB recipient=mob.location().fetchInhabitant(Util.combine(commands,0));
		if((recipient!=null)&&(recipient.isMonster())&&(!(recipient instanceof Deity)))
		{
			mob.tell("You may not serve "+recipient.name()+".");
			return false;
		}
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell("I don't see "+Util.combine(commands,0)+" here.");
			return false;
		}
		FullMsg msg=new FullMsg(mob,recipient,null,CMMsg.MSG_SERVE,"<S-NAME> swear(s) fealty to <T-NAMESELF>.");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
