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
public class Config extends StdCommand
{
	public Config(){}

	private String[] access={"CONFIG","AUTO"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("^HYour configuration flags:^?\n\r");
		for(int i=0;i<MOB.AUTODESC.length;i++)
		{
            if((MOB.AUTODESC[i].equalsIgnoreCase("SYSMSGS"))&&(!(CMSecurity.isAllowed(mob,mob.location(),"SYSMSGS"))))
                continue;
			
			msg.append(CMStrings.padRight(MOB.AUTODESC[i],15)+": ");
			boolean set=CMath.isSet(mob.getBitmap(),i);
			if(MOB.AUTOREV[i]) set=!set;
			msg.append(set?"ON":"OFF");
			msg.append("\n\r");
		}
	    String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
		msg.append(CMStrings.padRight("LINEWRAP",15)+": "+wrap);
		msg.append("\n\r");
	    String pageBreak=(mob.playerStats().getPageBreak()!=0)?(""+mob.playerStats().getPageBreak()):"Disabled";
		msg.append(CMStrings.padRight("PAGEBREAK",15)+": "+pageBreak);
		msg.append("\n\r");
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
