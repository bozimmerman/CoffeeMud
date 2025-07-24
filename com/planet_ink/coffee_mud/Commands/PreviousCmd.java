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
public class PreviousCmd extends StdCommand
{
	public PreviousCmd()
	{
	}

	private final String[] access=I(new String[]{"!"});
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

			final LinkedList<List<String>> cmdHistory = mob.session().getHistory();
			if(cmdHistory.size()>0)
			{
				List<String> lastCmd = new XVector<String>(cmdHistory.getLast());
				final String parms=CMParms.combineQuoted(commands,0).substring(1).trim().toLowerCase();
				if(parms.length()>0)
				{
					lastCmd = null;
					for(final Iterator<List<String>> h=cmdHistory.descendingIterator();h.hasNext();)
					{
						final List<String> cmd = h.next();
						final String combined  = CMParms.combineQuoted(cmd,0).trim().toLowerCase();
						if(combined.startsWith(parms))
						{
							lastCmd = cmd;
							break;
						}
					}
					if(lastCmd == null)
					{
						for(final Iterator<List<String>> h=cmdHistory.descendingIterator();h.hasNext();)
						{
							final List<String> cmd = h.next();
							final String combined  = CMParms.combineQuoted(cmd,0).trim().toLowerCase();
							if(combined.endsWith(parms))
							{
								lastCmd = cmd;
								break;
							}
						}
					}
					if(lastCmd == null)
					{
						for(final Iterator<List<String>> h=cmdHistory.descendingIterator();h.hasNext();)
						{
							final List<String> cmd = h.next();
							final String combined  = CMParms.combineQuoted(cmd,0).trim().toLowerCase();
							if(combined.indexOf(parms)>=0)
							{
								lastCmd = cmd;
								break;
							}
						}
					}
					if(lastCmd == null)
					{
						mob.tell(L("'@x1' does not match any commands in your history.  Try the HISTORY command.",parms));
						return false;
					}
				}
				final Vector<String> cmds = new Vector<String>(lastCmd.size());
				for(final Object o : lastCmd)
					cmds.add(o.toString());
				mob.enqueCommand(cmds,metaFlags,0);
			}
			else
				mob.tell(L("No previous history!"));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
