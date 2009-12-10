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
public class IMC2 extends StdCommand
{
	public IMC2(){}

	private String[] access={"IMC2"};
	public String[] getAccessWords(){return access;}

	public void IMC2Error(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),"IMC2"))
			mob.tell("Try IMC2 LIST, IMC2 INFO [MUD], IMC2 LOCATE, or IMC2 CHANNELS.");
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(!(CMLib.intermud().imc2online()))
		{
			mob.tell("IMC2 is unavailable.");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			IMC2Error(mob);
			return false;
		}
		String str=(String)commands.firstElement();
		if(!(CMLib.intermud().imc2online()))
			mob.tell("IMC2 is unavailable.");
		else
		if(str.equalsIgnoreCase("list"))
			CMLib.intermud().giveIMC2MudList(mob);
		else
		if(str.equalsIgnoreCase("locate"))
			CMLib.intermud().i3locate(mob,CMParms.combine(commands,1));
		else
		if(str.equalsIgnoreCase("channels"))
			CMLib.intermud().giveIMC2ChannelsList(mob);
		else
		if(str.equalsIgnoreCase("info"))
			CMLib.intermud().imc2mudInfo(mob,CMParms.combine(commands,1));
		else
			IMC2Error(mob);

		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
