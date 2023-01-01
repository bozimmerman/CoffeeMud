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
   Copyright 2003-2023 Bo Zimmerman

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
public class Help extends StdCommand
{
	public Help()
	{
	}

	private final String[] access=I(new String[]{"HELP", "?"});

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final String helpStr=CMParms.combine(commands,1);
		if(CMLib.help().getHelpFile().size()==0)
		{
			mob.tell(L("No help is available."));
			return false;
		}
		String helpText=null;
		if(helpStr.length()==0)
			helpText=Resources.getFileResource("help/help.txt",true).toString();
		else
		{
			final Properties helpProps =  CMLib.help().getHelpFile();
			final Properties ahelpProps =  CMLib.help().getArcHelpFile();
			Pair<String, String> match=CMLib.help().getHelpMatch(helpStr,helpProps,mob, 0);
			if(((match==null)||(match.second==null))
			&&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AHELP)))
				match=CMLib.help().getHelpMatch(helpStr,ahelpProps,mob, 0);
			if((match!=null)
			&&(match.second!=null))
			{
				helpText = match.second;
				final List<String> seeAlso = CMLib.help().getSeeAlsoHelpOn(mob, helpProps, helpStr, match.first, match.second, 5);
				if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AHELP))
				{
					for(final String s : CMLib.help().getSeeAlsoHelpOn(mob, ahelpProps, helpStr, match.first, match.second, 5))
					{
						if(!seeAlso.contains(s))
							seeAlso.add(s);
					}
				}
				if(seeAlso.size()>0)
				{
					final String alsoHelpStr = CMLib.english().toEnglishStringList(seeAlso);
					helpText += "\n\rSee also help on: "+alsoHelpStr;
				}
			}
		}
		if(helpText==null)
		{
			final Properties rHelpFile2=CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AHELP)?CMLib.help().getArcHelpFile():null;
			final List<String> thisList = CMLib.help().getHelpList( helpStr, CMLib.help().getHelpFile(), rHelpFile2, mob);
			if((thisList!=null)&&(thisList.size()>0))
			{
				final String matchText = CMLib.lister().build4ColTable(mob,thisList).toString();
				mob.tell(L("No help is available on '@x1'.\n\rHowever, here are some search matches:\n\r^N@x2",helpStr,matchText.replace('_',' ')));
			}
			else
			{
				mob.tell(L("No help is available on '@x1'.\n\rEnter 'COMMANDS' for a command list, "
						+ "or 'TOPICS' for a complete list, or 'HELPLIST' to search.",helpStr));
			}
			Log.helpOut("Help",mob.Name()+" wanted help on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln(helpText.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
