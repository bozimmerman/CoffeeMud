package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

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
public class HelpList extends StdCommand
{
	public HelpList(){}

	private String[] access={"HELPLIST","HLIST"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String helpStr=Util.combine(commands,1);
		if(MUDHelp.getHelpFile().size()==0)
		{
			mob.tell("No help is available.");
			return false;
		}
		if(helpStr.length()==0)
		{
		    mob.tell("You must enter a search pattern.  Use 'TOPICS' or 'COMMANDS' for an unfiltered list.");
		    return false;
		}
		StringBuffer thisTag=
		    		MUDHelp.getHelpList(
			        helpStr,
			        MUDHelp.getHelpFile(),
			        CMSecurity.isAllowed(mob,mob.location(),"AHELP")?MUDHelp.getArcHelpFile():null,
			        mob);
		if((thisTag==null)||(thisTag.length()==0))
		{
			mob.tell("No help entries match '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			Log.errOut("Help",mob.Name()+" wanted help list match on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln("^xHelp File Matches:^.^?\n\r^N"+thisTag.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}

