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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
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

public class Commands extends StdCommand
{
	public Commands(){}

	private final String[] access=I(new String[]{"COMMANDS"});
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
			if ((commands!=null) 
			&& (commands.size()>1) 
			&& ("CLEAR".startsWith(commands.get(1).toString().toUpperCase())))
			{
				mob.clearCommandQueue();
				mob.tell(L("Command queue cleared."));
				return false;
			}
			final StringBuffer commandList=new StringBuffer("");
			final Vector<String> commandSet=new Vector<String>();
			int col=0;
			final HashSet<String> done=new HashSet<String>();
			for(final Enumeration<Command> e=CMClass.commands();e.hasMoreElements();)
			{
				final Command C=e.nextElement();
				final String[] access=C.getAccessWords();
				if((access!=null)
				&&(access.length>0)
				&&(access[0].length()>0)
				&&(!done.contains(access[0]))
				&&(C.securityCheck(mob)))
				{
					done.add(access[0]);
					commandSet.add(access[0]);
				}
			}
			for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(A.triggerStrings()!=null)&&(A.triggerStrings().length>0)&&(!done.contains(A.triggerStrings()[0])))
				{
					done.add(A.triggerStrings()[0]);
					commandSet.add(A.triggerStrings()[0]);
				}
			}
			Collections.sort(commandSet);
			final int COL_LEN=CMLib.lister().fixColWidth(19.0,mob);
			for(final Iterator<String> i=commandSet.iterator();i.hasNext();)
			{
				final String s=i.next();
				if(++col>3)
				{
					commandList.append("\n\r");
					col=0;
				}
				commandList.append(CMStrings.padRight("^<HELP^>"+s+"^</HELP^>",COL_LEN));
			}
			commandList.append("\n\r\n\rEnter HELP 'COMMAND' for more information on these commands.\n\r");
			mob.session().colorOnlyPrintln(L("^HComplete commands list:^?\n\r@x1",commandList.toString()),false);
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
