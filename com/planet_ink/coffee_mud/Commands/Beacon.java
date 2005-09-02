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
public class Beacon extends StdCommand
{
	public Beacon(){}

	private String[] access={getScr("Beacon","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			if(mob.getStartRoom()==mob.location())
				mob.tell(getScr("Beacon","albeacon"));
			else
			{
				mob.setStartRoom(mob.location());
				mob.tell(getScr("Beacon","modbeacon"));
			}
		}
		else
		{
			String name=Util.combine(commands,0);
			MOB M=null;
			for(int s=0;s<Sessions.size();s++)
			{
				Session S=Sessions.elementAt(s);
				if((S.mob()!=null)&&(EnglishParser.containsString(S.mob().name(),name)))
				{ M=S.mob(); break;}
			}
			if(M==null)
			{
				mob.tell(getScr("Beacon","noonlinecld",name));
				return false;
			}
			if(M.getStartRoom()==M.location())
			{
				mob.tell(getScr("Beacon","altheirbeacon",M.name()));
				return false;
			}
			if(!CMSecurity.isAllowed(mob,M.location(),"BEACON"))
			{
				mob.tell(getScr("Beacon","nobeaconthere",M.name()));
				return false;
			}
			M.setStartRoom(M.location());
			mob.tell(getScr("Beacon","modtheirbeacon",M.name()));
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"BEACON");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
