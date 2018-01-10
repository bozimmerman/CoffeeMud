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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class ClanKills extends StdCommand
{
	public ClanKills()
	{
	}

	private final String[]	access	= I(new String[] {"CLANKILLS"});

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String clanName=(commands.size()>1)?CMParms.combine(commands,1,commands.size()):"";
		if((clanName.length()==0)&&(mob.clans().iterator().hasNext()))
			clanName=mob.clans().iterator().next().first.clanID();
		final StringBuffer msg=new StringBuffer("");
		if(clanName.length()>0)
		{
			Clan foundClan=CMLib.clans().getClan(clanName);
			if(foundClan == null)
			{
				for(final Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
				{
					final Clan C=(Clan)e.nextElement();
					if(CMLib.english().containsString(C.getName(), clanName))
					{
						foundClan=C;
						break;
					}
				}
			}
			if(foundClan==null)
				msg.append(L("No clan was found by the name of '@x1'.\n\r",clanName));
			else
			{
				if(mob.getClanRole(foundClan.clanID())==null)
				{
					msg.append(L("You are not a member of @x1.\n\r",foundClan.name()));
				}
				else
				{
					final List<Pair<String,Integer>> topKillers = new ArrayList<Pair<String,Integer>>();
					for(final Clan.MemberRecord M : foundClan.getMemberList())
					{
						if((M.mobpvps+M.playerpvps) > 0)
							topKillers.add(new Pair<String,Integer>(M.name,new Integer(M.mobpvps+M.playerpvps)));
					}
					@SuppressWarnings("unchecked")
					final Pair<String,Integer>[] killerArray = topKillers.toArray(new Pair[0]);
					Arrays.sort(killerArray,new Comparator<Pair<String,Integer>>()
					{
						@Override
						public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
						{
							return o2.second.compareTo(o1.second);
						}
					});
					if(topKillers.size()==0)
						msg.append(L("There have not been any rival clan kills...\n\r"));
					else
					{
						msg.append(L("^XTop ranked rival clan killers of @x1^?^.\n\r\n\r",foundClan.name()));
						topKillers.clear();
						final List<String> reverseList = new ArrayList<String>();
						for(int x=0;x<killerArray.length;x++)
						{
							final Pair<String,Integer> p=killerArray[x];
							reverseList.add((x+1)+". "+p.first+" ("+p.second.intValue()+")");
						}
						msg.append(CMLib.lister().threeColumns(mob, reverseList));
					}
				}
			}
		}
		else
		{
			msg.append(L("You need to specify which clan you would like details on. Try 'CLANLIST'.\n\r"));
		}
		mob.tell(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}