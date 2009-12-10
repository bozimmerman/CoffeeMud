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
public class Shutdown extends StdCommand
{
	public Shutdown(){}

	private String[] access={"SHUTDOWN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.isMonster()) return false;
		boolean keepItDown=true;
		boolean noPrompt=false;
		String externalCommand=null;
		for(int i=commands.size()-1;i>=1;i--)
		{
			String s=(String)commands.elementAt(i);
			if(s.equalsIgnoreCase("RESTART"))
			{ keepItDown=false; commands.removeElementAt(i);}
			else
			if(s.equalsIgnoreCase("NOPROMPT"))
			{ noPrompt=true; commands.removeElementAt(i); }
		}
		if((!keepItDown)&&(commands.size()>1))
			externalCommand=CMParms.combine(commands,1);

		if((!noPrompt)
		&&(!mob.session().confirm("Are you fully aware of the consequences of this act (y/N)?","N")))
			return false;
		
		for(int s=0;s<CMLib.sessions().size();s++)
			CMLib.sessions().elementAt(s).colorOnlyPrintln("\n\r\n\r^x"+CMProps.getVar(CMProps.SYSTEM_MUDNAME)+" is now shutting down!^.^?\n\r");

		if(keepItDown)
			Log.errOut("CommandProcessor",mob.Name()+" starts system shutdown...");
		else
		if(externalCommand!=null)
			Log.errOut("CommandProcessor",mob.Name()+" starts system restarting '"+externalCommand+"'...");
		else
			Log.errOut("CommandProcessor",mob.Name()+" starts system restart...");
		mob.tell("Starting shutdown...");

		com.planet_ink.coffee_mud.application.MUD.globalShutdown(mob.session(),keepItDown,externalCommand);
		return false;
	}
	
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"SHUTDOWN");}

	
}
