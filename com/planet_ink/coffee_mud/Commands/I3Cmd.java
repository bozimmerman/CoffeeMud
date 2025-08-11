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
import com.planet_ink.coffee_mud.Libraries.interfaces.IntermudInterface.InterProto;
import com.planet_ink.coffee_mud.Libraries.interfaces.IntermudInterface.RemoteIMud;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
	public I3Cmd()
	{
	}

	private final String[] access=I(new String[]{"I3"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public void i3Error(final MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			mob.tell(L("Try I3 LIST, I3 CHANNELS, I3 ADD [CHANNEL], I3 DELETE [CHANNEL], I3 LISTEN [CHANNEL], "
					+ "I3 SILENCE [CHANNEL], I3 PING [MUD], I3 LOCATE [NAME], I3 RESTART, I3 STOP, or I3 INFO [MUD]."));
		else
			mob.tell(L("Try I3 LIST, I3 LOCATE [NAME], or I3 INFO [MUD-NAME]."));
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		if(commands.size()<1)
		{
			if(!CMLib.intermud().isOnline(InterProto.I3))
			{
				mob.tell(L("I3 is unavailable."));
				return false;
			}
			i3Error(mob);
			return false;
		}
		final String str=commands.get(0);
		if((!CMLib.intermud().isOnline(InterProto.I3))&&(!str.equalsIgnoreCase("restart")))
			mob.tell(L("I3 is unavailable."));
		else
		if(str.equalsIgnoreCase("list"))
		{
			final boolean coffeemudOnly=((commands.size()>1)&&(commands.get(1).toLowerCase().startsWith("coffeemud")));
			final List<RemoteIMud> muds = CMLib.intermud().getMudInfo(InterProto.I3, coffeemudOnly);
			final StringBuffer buf=new StringBuffer("\n\rI3 Mud List:\n\r");
			final int col1Width=CMLib.lister().fixColWidth(25, mob);
			final int col2Width=CMLib.lister().fixColWidth(25, mob);
			for(final RemoteIMud m : muds)
			{
				if((m!=null)&&(m.mudLib!=null))
				{
					final String mudlib = m.mudLib.startsWith("CoffeeMud") ? "^H"+m.mudLib+"^?" : m.mudLib;
					buf.append("["+CMStrings.padRight(m.name,col1Width)+"]["+CMStrings.padRight(mudlib,col2Width)+"] "+m.hostPort+"\n\r");
				}
			}
			mob.session().wraplessPrintln(buf.toString());
		}
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
			CMLib.intermud().channelAdd(InterProto.I3,mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("channels"))
			CMLib.intermud().getChannelsList(mob,InterProto.I3);
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
			CMLib.intermud().channelRemove(InterProto.I3,mob,CMParms.combine(commands,1));
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
			CMLib.intermud().channelListen(InterProto.I3,mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("ping"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			CMLib.intermud().pingRouter(InterProto.I3,mob);
		}
		else
		if(str.equalsIgnoreCase("STOP"))
		{
			CMLib.intermud().stopIntermud(InterProto.I3);
			mob.tell(L("Done."));
		}
		else
		if(str.equalsIgnoreCase("restart"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			{
				i3Error(mob);
				return false;
			}
			if(CMLib.intermud().startIntermud(InterProto.I3,true))
				mob.tell(L("Done."));
			else
				mob.tell(L("Failure."));
		}
		else
		if(str.equalsIgnoreCase("locate"))
		{
			if(commands.size()<2)
			{
				mob.tell(L("You did not specify a name!"));
				return false;
			}
			CMLib.intermud().imudLocate(mob,CMParms.combine(commands,1));
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
			CMLib.intermud().channelSilence(InterProto.I3,mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("info"))
			CMLib.intermud().mudInfo(InterProto.I3, mob,CMParms.combine(commands,1));
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
