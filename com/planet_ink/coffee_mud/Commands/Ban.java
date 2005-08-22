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
public class Ban extends StdCommand
{
	public Ban(){}

	private String[] access={getScr("Ban","bancmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		String banMe=Util.combine(commands,0);
		if(banMe.length()==0)
		{
			mob.tell(getScr("Ban","banerr"));
			return false;
		}
		banMe=banMe.toUpperCase().trim();
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String B=(String)banned.elementAt(b);
			if(B.equals(banMe))
			{
				mob.tell(getScr("Ban","albanned")+(b+1)+".");
				return false;
			}
		}
		mob.tell(getScr("Ban","banned",banMe));
		StringBuffer str=Resources.getFileResource("banned.ini",false);
		if(banMe.trim().length()>0) str.append(banMe+"\n");
		Resources.updateResource("banned.ini",str);
		Resources.saveFileResource("banned.ini");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"BAN");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
