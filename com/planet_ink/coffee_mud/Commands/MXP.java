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
public class MXP extends StdCommand
{
	public MXP(){}

	private String[] access={"MXP"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			if(!Util.bset(mob.getBitmap(),MOB.ATT_MXP))
			{
			    mob.session().rawPrint("\033[5z \033[1z<VERSION>\n\r");
			    String s=mob.session().prompt("",1000).trim().toUpperCase();
			    if((s.indexOf("<VERSION ")>=0)&&(s.indexOf("MXP=")>=0))
			    {
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_MXP));
					mob.tell("MXP codes enabled.\n\r");
			    }
			    else
			        mob.tell("Your client does not appear to support MXP. Sorry.");
			}
			else
			{
				mob.tell("MXP codes are already enabled.\n\r");
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}

