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
public class Emote extends StdCommand
{
	public Emote(){}

	private String[] access={getScr("Emote","emotecmd"),",",";",":"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(getScr("Emote","emoteerr"));
			return false;
		}
		String combinedCommands=Util.combine(commands,1);
		combinedCommands=CommonStrings.applyFilter(combinedCommands,CommonStrings.SYSTEM_EMOTEFILTER);
        if(!combinedCommands.startsWith("'"))
            combinedCommands=" "+combinedCommands.trim();
		String emote="^E<S-NAME> "+combinedCommands+" ^?";
		FullMsg msg=new FullMsg(mob,null,null,CMMsg.MSG_EMOTE,"^E"+mob.name()+combinedCommands+" ^?",emote,emote);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
