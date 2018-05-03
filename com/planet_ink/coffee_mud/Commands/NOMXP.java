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

public class NOMXP extends StdCommand
{
	public NOMXP(){}

	private final String[] access=I(new String[]{"NOMXP"});
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
			boolean quiet=false;
			if((commands!=null)
			&&(commands.size()>1)
			&&(commands.get(1).toUpperCase().equals("QUIET")))
			{
				quiet=true;
			}
			if((mob.isAttributeSet(MOB.Attrib.MXP))
			||(mob.session().getClientTelnetMode(Session.TELNET_MXP)))
			{
				if(mob.session().getClientTelnetMode(Session.TELNET_MXP))
					mob.session().rawOut("\033[3z \033[7z");
				mob.setAttribute(MOB.Attrib.MXP,false);
				mob.session().changeTelnetMode(Session.TELNET_MXP,false);
				mob.session().setClientTelnetMode(Session.TELNET_MXP,false);
				if(!quiet)
					mob.tell(L("MXP codes are disabled.\n\r"));
			}
			else
				mob.tell(L("MXP codes are already disabled.\n\r"));
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
		return super.securityCheck(mob)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP));
	}
}
