package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
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
import java.io.IOException;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class Pause extends StdCommand
{
	public Pause()
	{
	}

	private final String[]	access	= I(new String[] { "PAUSE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell(L("You are not allowed to do that here."));
		return false;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{

		final String cmd=CMParms.combine(commands,1);
		if(commands.size()<2)
		{
			if(!CMLib.threads().isAllSuspended())
			{
				if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.PAUSE))
					mob.tell(L("You are not allowed to pause all objects."));
				else
				{
					CMLib.threads().suspendAll(new CMRunnable[]{mob.session()});
					mob.tell(L("All objects have been suspended. Enter PAUSE again to resume."));
				}
			}
			else
			{
				CMLib.threads().resumeAll();
				mob.tell(L("All objects have been resumed."));
			}
		}
		else
		if(cmd.equalsIgnoreCase("RESUME"))
		{
			if(!CMLib.threads().isAllSuspended())
				mob.tell(L("Objects are not currently suspended."));
			else
			{
				CMLib.threads().resumeAll();
				mob.tell(L("All objects have been resumed."));
			}
		}
		else
		{
			Environmental E=null;
			if(cmd.equalsIgnoreCase("AREA"))
				E=mob.location().getArea();
			else
			if(cmd.equalsIgnoreCase("ROOM"))
				E=mob.location();
			else
				E=mob.location().fetchFromRoomFavorMOBs(null,cmd);
			if(E==null)
				mob.tell(L("'@x1' is an unknown object here.",cmd));
			else
			if(!CMLib.threads().isTicking(E,-1))
				mob.tell(L("'@x1' has no thread support.",cmd));
			else
			if(!CMLib.threads().isSuspended(E,-1))
			{
				CMLib.threads().suspendTicking(E,-1);
				mob.tell(L("Object '@x1' ticks have been suspended. Enter PAUSE @x2 again to resume.",E.name(),cmd.toUpperCase()));
			}
			else
			{
				CMLib.threads().resumeTicking(E,-1);
				mob.tell(L("Object '@x1' ticks have been resumed.",E.name()));
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
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.PAUSE);
	}

}
