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
public class Prompt extends StdCommand
{
	public Prompt(){}

	private String[] access={"PROMPT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.session()==null) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;

		if(commands.size()==1)
			mob.session().rawPrintln("Your prompt is currently set at:\n\r"+pstats.getPrompt());
		else
		{
			String str=Util.combine(commands,1);
			if(("DEFAULT").startsWith(str.toUpperCase()))
				pstats.setPrompt("");
			else
				pstats.setPrompt(str);
			mob.session().rawPrintln("Your prompt is currently now set at:\n\r"+pstats.getPrompt());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
