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
public class Wake extends StdCommand
{
	public Wake(){}

	private String[] access={"WAKE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands!=null)
			commands.removeElementAt(0);
		if((commands==null)||(commands.size()==0))
		{
			if(!Sense.isSleeping(mob))
				mob.tell(getScr("Movement","wakeerr1"));
			else
			{
				FullMsg msg=new FullMsg(mob,null,null,CMMsg.MSG_STAND,getScr("Movement","wakeup"));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
		}
		else
		{
			String whom=Util.combine(commands,0);
			MOB M=mob.location().fetchInhabitant(whom);
			if((M==null)||(!Sense.canBeSeenBy(M,mob)))
			{
				mob.tell(getScr("Movement","youdontsee",whom));
				return false;
			}
			if(!Sense.isSleeping(M))
			{
				mob.tell(getScr("Movement","wakeerr2",M.name()));
				return false;
			}
			FullMsg msg=new FullMsg(mob,M,null,CMMsg.MSG_NOISYMOVEMENT,getScr("Movement","wakeother"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				execute(M,null);
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
