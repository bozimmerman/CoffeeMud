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
public class Config extends StdCommand
{
	public Config(){}

	private String[] access={"CONFIG","AUTO"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("^HYour configuration flags:^?\n\r");
		for(int i=0;i<MOB.AUTODESC.length;i++)
		{
            if((MOB.AUTODESC[i].equalsIgnoreCase("SYSMSGS"))&&(!(CMSecurity.isAllowed(mob,mob.location(),"SYSMSGS"))))
                continue;
			
			msg.append(Util.padRight(MOB.AUTODESC[i],15)+": ");
			boolean set=Util.isSet(mob.getBitmap(),i);
			if(MOB.AUTOREV[i]) set=!set;
			msg.append(set?"ON":"OFF");
			msg.append("\n\r");
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
