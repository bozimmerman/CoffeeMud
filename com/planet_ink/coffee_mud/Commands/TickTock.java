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
public class TickTock extends StdCommand
{
	public TickTock()
	{
	}

	private final String[] access = I(new String[] { "TICKTOCK" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final String s=CMParms.combine(commands,1).toLowerCase();
		try
		{
			if(CMath.isInteger(s))
			{
				int h=CMath.s_int(s);
				if(h==0)
					h=1;
				mob.tell(L("..tick..tock.."));
				mob.location().getArea().getTimeObj().tickTock(h);
				mob.location().getArea().getTimeObj().save();
			}
			else
			{
				int numTimes=1;
				if(CMath.isInteger(commands.get(commands.size()-1)))
					numTimes=CMath.s_int(commands.get(commands.size()-1));
				if(s.startsWith("clantick"))
				{
					for(int n=0;n<numTimes;n++)
						CMLib.clans().tickAllClans();
					mob.tell(L("Clans ticked!"));
				}
				else
				if(s.startsWith("smtp"))
				{
					for(int n=0;n<numTimes;n++)
						mob.tell(L(CMLib.host().executeCommand("TICK SMTP")));
				}
				else
				{
					for(final Enumeration<CMLibrary> e=CMLib.libraries();e.hasMoreElements();)
					{
						final CMLibrary lib=e.nextElement();
						if((lib.getServiceClient()!=null)&&(s.equalsIgnoreCase(lib.getServiceClient().getName())))
						{
							for(int n=0;n<numTimes;n++)
							{
								if(lib instanceof Runnable)
									((Runnable)lib).run();
								else
									lib.getServiceClient().tickTicker(true);
							}
							mob.tell(L("Done."));
							return false;
						}
					}
					List<TickClient> tickables = CMLib.threads().findTickClient(s, true);
					if((tickables == null) || (tickables.size()==0))
						tickables = CMLib.threads().findTickClient(s, false);
					if((tickables != null) && (tickables.size()>0))
					{
						for(final TickClient T : tickables)
						{
							T.tickTicker(false);
							mob.tell(L("Ticked "+T.getName()));
						}
						return false;
					}
					mob.tell(L("Ticktock what?  Enter a number of mud-hours, or clanticks, or thread id."));
				}
			}
		}
		catch(final Exception e)
		{
			mob.tell(L("Ticktock failed: @x1",e.getMessage()));
		}

		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK);
	}

}
