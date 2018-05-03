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

public class NoANSI extends StdCommand
{
	public NoANSI(){}

	private final String[] access=I(new String[]{"NOANSI","NOCOLOR","NOCOLOUR"});
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
			PlayerAccount acct = null;
			if(mob.playerStats()!=null)
				acct = mob.playerStats().getAccount();
			if(acct != null)
				acct.setFlag(PlayerAccount.AccountFlag.ANSI, false);
			if(mob.isAttributeSet(MOB.Attrib.ANSI))
			{
				mob.setAttribute(MOB.Attrib.ANSI,false);
				mob.tell(L("ANSI colour disabled.\n\r"));
			}
			else
			{
				mob.tell(L("ANSI is already disabled.\n\r"));
			}
			mob.session().setClientTelnetMode(Session.TELNET_ANSI,false);
			mob.session().setServerTelnetMode(Session.TELNET_ANSI,false);
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
