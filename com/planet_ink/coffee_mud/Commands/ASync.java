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
   Copyright 2019-2025 Bo Zimmerman

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
public class ASync extends StdCommand
{
	public ASync()
	{
	}

	private final String[] access = I(new String[] { "ASYNC" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		if(commands.size()<1)
		{
			mob.tell(L("Asyncronously do what?"));
			return false;
		}
		if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ASYNC))||(mob.isMonster()))
		{
			mob.tell(L("You aren't powerful enough to do that."));
			return false;
		}
		CMLib.threads().executeRunnable(new Runnable()
		{
			final List<String> command=new XVector<String>(CMParms.toStringArray(commands));
			final MOB M=mob;
			@Override
			public void run()
			{
				try
				{
					M.doCommand(command,metaFlags);
				}
				catch(final Exception e)
				{
					Log.errOut(e);
					M.tell(e.getMessage());
				}
			}
			
			@Override
			public String toString()
			{
				return "ASync: "+((M==null)?"null":M.Name())+": "+CMParms.combineQuoted(commands,0);
			}
		});
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowedAnywhere(mob, CMSecurity.SecFlag.ASYNC);
	}

}
