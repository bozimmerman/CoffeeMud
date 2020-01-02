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
   Copyright 2004-2020 Bo Zimmerman

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
public class ANSI extends StdCommand
{
	public ANSI()
	{
	}

	private final String[] access=I(new String[]{"ANSI","COLOR","COLOUR"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			final String firstCmd = ((commands!=null)&&(commands.size()>1))?commands.get(1):"";
			final String lastCmd = ((commands!=null)&&(commands.size()>1))?commands.get(commands.size()-1):"";
			final boolean lastOff = lastCmd.equalsIgnoreCase("off");
			if(lastOff)
			{
				final Command C=CMClass.getCommand("NOANSI");
				if(C!=null)
				{
					return C.execute(mob, commands, metaFlags);
				}
			}
			if(firstCmd.equals("16"))
			{
				if(lastOff)
				{
					if(mob.isAttributeSet(MOB.Attrib.ANSI))
					{
						if(mob.isAttributeSet(MOB.Attrib.ANSI16))
						{
							mob.setAttribute(MOB.Attrib.ANSI16,true);
							mob.tell(L("^!ANSI^N ^H256 colour^N re-enabled.\n\r"));
						}
						else
						{
							mob.tell(L("^!ANSI^N 256 color is ^Halready^N enabled.\n\r"));
						}
					}
					else
					{
						final Command C=CMClass.getCommand("NOANSI");
						if(C!=null)
						{
							return C.execute(mob, commands, metaFlags);
						}
					}
				}
				else
				if(mob.isAttributeSet(MOB.Attrib.ANSI))
				{
					mob.setAttribute(MOB.Attrib.ANSI16,!mob.isAttributeSet(MOB.Attrib.ANSI16));
					if(mob.isAttributeSet(MOB.Attrib.ANSI16))
						mob.tell(L("^!ANSI^N ^H16 colour^N enabled.\n\r"));
					else
						mob.tell(L("^!ANSI^N ^H256 colour^N enabled.\n\r"));
				}
				else
				{
					mob.setAttribute(MOB.Attrib.ANSI,true);
					mob.setAttribute(MOB.Attrib.ANSI16,true);
					mob.tell(L("^!ANSI^N 16 ^Hcolour^N enabled.\n\r"));
				}
			}
			else
			if(firstCmd.equals("256"))
			{
				if(lastOff)
				{
					if(mob.isAttributeSet(MOB.Attrib.ANSI))
					{
						if(!mob.isAttributeSet(MOB.Attrib.ANSI16))
						{
							mob.setAttribute(MOB.Attrib.ANSI16,true);
							mob.tell(L("^!ANSI^N ^H256 colour^N re-enabled.\n\r"));
						}
						else
						{
							mob.tell(L("^!ANSI^N 256 color is ^Halready^N enabled.\n\r"));
						}
					}
					else
					{
						final Command C=CMClass.getCommand("NOANSI");
						if(C!=null)
						{
							return C.execute(mob, commands, metaFlags);
						}
					}
				}
				else
				if(mob.isAttributeSet(MOB.Attrib.ANSI))
				{
					mob.setAttribute(MOB.Attrib.ANSI16,!mob.isAttributeSet(MOB.Attrib.ANSI16));
					if(mob.isAttributeSet(MOB.Attrib.ANSI16))
						mob.tell(L("^!ANSI^N ^H16 colour^N enabled.\n\r"));
					else
						mob.tell(L("^!ANSI^N ^H256 colour^N enabled.\n\r"));
				}
				else
				{
					mob.setAttribute(MOB.Attrib.ANSI,true);
					mob.setAttribute(MOB.Attrib.ANSI16,false);
					mob.tell(L("^!ANSI^N 256 ^Hcolour^N enabled.\n\r"));
				}
			}
			else
			{
				if(!mob.isAttributeSet(MOB.Attrib.ANSI))
				{
					mob.setAttribute(MOB.Attrib.ANSI,true);
					if(mob.isAttributeSet(MOB.Attrib.ANSI16))
						mob.tell(L("^!ANSI^N ^H16 colour^N enabled.\n\r"));
					else
						mob.tell(L("^!ANSI^N ^H256 colour^N enabled.\n\r"));
				}
				else
				{
					mob.tell(L("^!ANSI^N is ^Halready^N enabled.\n\r"));
				}
			}

			PlayerAccount acct = null;
			if(mob.playerStats()!=null)
				acct = mob.playerStats().getAccount();
			if(acct != null)
			{
				acct.setFlag(PlayerAccount.AccountFlag.ANSI, mob.isAttributeSet(MOB.Attrib.ANSI));
				acct.setFlag(PlayerAccount.AccountFlag.ANSI16, mob.isAttributeSet(MOB.Attrib.ANSI16));
			}
			mob.session().setClientTelnetMode(Session.TELNET_ANSI,mob.isAttributeSet(MOB.Attrib.ANSI));
			mob.session().setServerTelnetMode(Session.TELNET_ANSI,mob.isAttributeSet(MOB.Attrib.ANSI));
			mob.session().setClientTelnetMode(Session.TELNET_ANSI16,mob.isAttributeSet(MOB.Attrib.ANSI16));
			mob.session().setServerTelnetMode(Session.TELNET_ANSI16,mob.isAttributeSet(MOB.Attrib.ANSI16));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
