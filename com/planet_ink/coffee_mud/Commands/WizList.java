package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class WizList extends StdCommand
{
	public WizList(){}

	private String[] access={"WIZLIST"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer head=new StringBuffer("");
		boolean isArchonLooker=CMSecurity.isASysOp(mob);
		head.append("^x[");
		head.append(CMStrings.padRight("Race",8)+" ");
		head.append(CMStrings.padRight("Lvl",4)+" ");
		if(isArchonLooker)
			head.append(CMStrings.padRight("Last",18)+" ");
		head.append("] Archon Character Name^.^?\n\r");
		mob.tell("^x["+CMStrings.centerPreserve("The Archons of "+CMProps.getVar(CMProps.SYSTEM_MUDNAME),head.length()-10)+"]^.^?");
		java.util.List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
        CharClass C=CMClass.getCharClass("Archon");
		for(PlayerLibrary.ThinPlayer U : allUsers)
		{
			if(U.charClass.equals("Archon"))
			{
				head.append("[");
				head.append(CMStrings.padRight(U.race,8)+" ");
                if((C==null)||(!C.leveless()))
    				head.append(CMStrings.padRight(""+U.level,4)+" ");
                else
                    head.append(CMStrings.padRight("    ",4)+" ");
                if(isArchonLooker)
					head.append(CMStrings.padRight(CMLib.time().date2String(U.last),18)+" ");
				head.append("] "+CMStrings.padRight(U.name,25));
				head.append("\n\r");
			}
		}
		mob.tell(head.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
