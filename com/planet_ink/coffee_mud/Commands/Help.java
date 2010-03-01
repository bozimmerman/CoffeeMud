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
public class Help extends StdCommand
{
	public Help(){}

	private String[] access={"HELP"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String helpStr=CMParms.combine(commands,1);
		if(CMLib.help().getHelpFile().size()==0)
		{
			mob.tell("No help is available.");
			return false;
		}
		StringBuilder thisTag=null;
		if(helpStr.length()==0)
			thisTag=new StringBuilder(Resources.getFileResource("help/help.txt",true));
		else
			thisTag=CMLib.help().getHelpText(helpStr,CMLib.help().getHelpFile(),mob);
		if((thisTag==null)&&(CMSecurity.isAllowed(mob,mob.location(),"AHELP")))
			thisTag=CMLib.help().getHelpText(helpStr,CMLib.help().getArcHelpFile(),mob);
		if(thisTag==null)
		{
			StringBuilder thisList=
	    		CMLib.help().getHelpList(
		        helpStr,
		        CMLib.help().getHelpFile(),
		        CMSecurity.isAllowed(mob,mob.location(),"AHELP")?CMLib.help().getArcHelpFile():null,
		        mob);
			if((thisList!=null)&&(thisList.length()>0))
				mob.tell("No help is available on '"+helpStr+"'.\n\rHowever, here are some search matches:\n\r^N"+thisList.toString().replace('_',' '));
			else
				mob.tell("No help is available on '"+helpStr+"'.\n\rEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list, or 'HELPLIST' to search.");
			Log.helpOut("Help",mob.Name()+" wanted help on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln(thisTag.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
