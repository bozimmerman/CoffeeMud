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

public class I3Cmd extends StdCommand
{
	public I3Cmd(){}

	private final String[] access=I(new String[]{"I3"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public void i3Error(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			mob.tell(L("Try I3 LIST, I3 CHANNELS, I3 ADD [CHANNEL], I3 DELETE [CHANNEL], I3 LISTEN [CHANNEL], I3 SILENCE [CHANNEL], I3 PING [MUD], I3 LOCATE [NAME], I3 RESTART, or I3 INFO [MUD]."));
		else
			mob.tell(L("Try I3 LIST, I3 LOCATE [NAME], or I3 INFO [MUD-NAME]."));
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		if(commands.size()<1)
		{
			if(!CMLib.intermud().i3online())
			{
				mob.tell(L("I3 is unavailable."));
				return false;
			}
			i3Error(mob);
			return false;
		}
		final String str=commands.get(0);
		if((!CMLib.intermud().i3online())&&(!str.equalsIgnoreCase("restart")))
			mob.tell(L("I3 is unavailable."));
		else
		if(str.equalsIgnoreCase("list"))
			CMLib.intermud().giveI3MudList(mob);
		else
		if(str.equalsIgnoreCase("add"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			if(commands.size()<2)
			{
				mob.tell(L("You did not specify a channel name!"));
				return false;
			}
			CMLib.intermud().i3channelAdd(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("channels"))
			CMLib.intermud().giveI3ChannelsList(mob);
		else
		if(str.equalsIgnoreCase("delete"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			if(commands.size()<2)
			{
				mob.tell(L("You did not specify a channel name!"));
				return false;
			}
			CMLib.intermud().i3channelRemove(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("listen"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			if(commands.size()<2)
			{
				mob.tell(L("You did not specify a channel name!"));
				return false;
			}
			CMLib.intermud().i3channelListen(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("ping"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			CMLib.intermud().i3pingRouter(mob);
		}
		else
		if(str.equalsIgnoreCase("restart"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			try
			{
				mob.tell(CMLib.hosts().get(0).executeCommand("START I3"));
			}
			catch(final Exception e){ Log.errOut("I3Cmd",e);}
		}
		else
		if(str.equalsIgnoreCase("locate"))
		{
			if(commands.size()<2)
			{
				mob.tell(L("You did not specify a name!"));
				return false;
			}
			CMLib.intermud().i3locate(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("silence"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			if(commands.size()<2)
			{
				mob.tell(L("You did not specify a channel name!"));
				return false;
			}
			CMLib.intermud().i3channelSilence(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("info"))
			CMLib.intermud().i3mudInfo(mob,CMParms.combine(commands,1));
		else
			i3Error(mob);

		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
