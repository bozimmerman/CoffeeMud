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
public class ListSessions extends StdCommand
{
	public ListSessions(){}

	private String[] access={"SESSIONS"};
	public String[] getAccessWords(){return access;}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer lines=new StringBuffer("^x");
		lines.append(Util.padRight("Status",9)+"| ");
		lines.append(Util.padRight("Valid",5)+"| ");
		lines.append(Util.padRight("Name",17)+"| ");
		lines.append(Util.padRight("IP",17)+"| ");
		lines.append(Util.padRight("Idle",17)+"^.^N\n\r");
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			lines.append((thisSession.killFlag()?"^H":"")+Util.padRight(Session.statusStr[thisSession.getStatus()],9)+(thisSession.killFlag()?"^?":"")+"| ");
			if (thisSession.mob() != null)
			{
				lines.append(Util.padRight(((thisSession.mob().session()==thisSession)?"Yes":"^HNO!^?"),5)+"| ");
				lines.append("^!"+Util.padRight(thisSession.mob().Name(),17)+"^?| ");
			}
			else
			{
				lines.append(Util.padRight("N/A",5)+"| ");
				lines.append(Util.padRight("NAMELESS",17)+"| ");
			}
			lines.append(Util.padRight(thisSession.getAddress(),17)+"| ");
			lines.append(Util.padRight((thisSession.getIdleMillis()+""),17));
			lines.append("\n\r");
		}
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(lines.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"SESSIONS");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
