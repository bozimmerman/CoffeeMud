package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Sounds extends StdCommand
{
	public Sounds(){}

	private final String[] access=I(new String[]{"SOUNDS","MSP"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			if((commands!=null)
			&&(commands.size()>1)
			&&(commands.get(1).toUpperCase().equals("OFF")))
			{
				final Command C=CMClass.getCommand("NoSounds");
				if(C!=null)
				{
					return C.execute(mob, commands, metaFlags);
				}
			}
			
			boolean force=false;
			if(commands != null)
			{
				for(final Object o : commands)
				{
					if(o.toString().equalsIgnoreCase("force"))
						force=true;
				}
			}
			final Session session=mob.session();
			if((!mob.isAttributeSet(MOB.Attrib.SOUND))
			||(!session.getClientTelnetMode(Session.TELNET_MSP)))
			{
				session.changeTelnetMode(Session.TELNET_MSP,true);
				for(int i=0;((i<5)&&(!session.getClientTelnetMode(Session.TELNET_MSP)));i++)
				{
					try
					{
						mob.session().prompt("",500);
					}
					catch(final Exception e)
					{
					}
				}
				if(session.getClientTelnetMode(Session.TELNET_MSP))
				{
					mob.setAttribute(MOB.Attrib.SOUND,true);
					mob.tell(L("MSP Sound/Music enabled.\n\r"));
				}
				else
				if(force)
				{
					session.setClientTelnetMode(Session.TELNET_MSP, true);
					session.setServerTelnetMode(Session.TELNET_MSP, true);
					mob.setAttribute(MOB.Attrib.SOUND,true);
					mob.tell(L("MSP Sound/Music has been forceably enabled.\n\r"));
				}
				else
					mob.tell(L("Your client does not appear to support MSP."));
			}
			else
			{
				mob.tell(L("MSP Sound/Music is already enabled.\n\r"));
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return super.securityCheck(mob)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP));
	}
}
