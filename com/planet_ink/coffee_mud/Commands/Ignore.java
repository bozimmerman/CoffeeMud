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
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
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
public class Ignore extends StdCommand
{
	public Ignore()
	{
	}

	private final String[] access=I(new String[]{"IGNORE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected final static String[] OK_CATS = new String[] {
		"MAIL", "TELL", "TEACH", "RIDE"
	};

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return false;
		final Set<String> h=pstats.getIgnored();
		if((commands.size()<2)||(commands.get(1).equalsIgnoreCase("list")))
		{
			if(h.size()==0)
				mob.tell(L("You have no names on your ignore list.  Use IGNORE ADD to add more."));
			else
			{
				final StringBuffer str=new StringBuffer(L("You are ignoring: "));
				for (final String element : h)
				{
					if(element.endsWith("*"))
						str.append("(Account: "+element.substring(0,element.length()-1)+") ");
					else
						str.append(element+" ");
				}
				mob.tell(str.toString());
			}
		}
		else
		if(commands.get(1).equalsIgnoreCase("ADD"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell(L("Add whom?"));
				return false;
			}
			String cat = null;
			final int x = name.lastIndexOf('.');
			if(x>0)
			{
				if((!CMLib.players().accountExistsAllHosts(name))
				||(!CMLib.players().playerExistsAllHosts(name)))
				{
					cat = name.substring(0,x);
					name = name.substring(x+1);
					if(CMParms.indexOfIgnoreCase(OK_CATS, cat)>=0)
						cat = cat.toUpperCase().trim();
					else
					{
						CMChannel chan = CMLib.channels().getChannel(cat);
						if(chan == null)
							chan = CMLib.channels().getChannel(CMLib.channels().findChannelName(cat));
						if(chan == null)
						{
							mob.tell(L("'@x1' is not a channel name, or one of: @x2.",
									cat,CMParms.toListString(OK_CATS)));
							return false;
						}
						else
							cat = chan.name();
					}
				}
			}

			name=CMStrings.capitalizeAndLower(name);
			if(CMLib.players().accountExistsAllHosts(name))
				name=name+"*";
			else
			if((!CMLib.players().playerExistsAllHosts(name))
			&&(name.indexOf('@')<0))
			{
				mob.tell(L("No player by that name was found."));
				return false;
			}
			final String finalIgnoreName = (cat == null)?name:(cat+"."+name);
			if(h.contains(finalIgnoreName))
			{
				mob.tell(L("That name is already on your list."));
				return false;
			}
			h.add(finalIgnoreName);
			if(name.endsWith("*"))
				mob.tell(L("The Account '@x1' has been added to your ignore list.",finalIgnoreName));
			else
				mob.tell(L("The Player/Account '@x1' has been added to your ignore list.",finalIgnoreName));
		}
		else
		if(commands.get(1).equalsIgnoreCase("REMOVE"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell(L("Remove whom?"));
				return false;
			}
			if(!h.contains(name))
			{
				if(h.contains(name+"*"))
					name+="*";
				else
				{
					mob.tell(L("That name '@x1' does not appear on your list.  Watch your casing!",name));
					return false;
				}
			}
			h.remove(name);
			if(name.endsWith("*"))
				mob.tell(L("The Account '@x1' has been removed from your ignore list.",name));
			else
				mob.tell(L("The Player '@x1' has been removed from your ignore list.",name));
		}
		else
		{
			mob.tell(L("Parameter '@x1' is not recognized.  Try LIST, ADD, or REMOVE.",(commands.get(1))));
			return false;
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
