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
   Copyright 2003-2025 Bo Zimmerman

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
public class ClanDetails extends StdCommand
{
	public ClanDetails()
	{
	}

	private final String[]	access	= I(new String[] { "CLANDETAILS", "CLAN" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
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
				for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
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
				msg.append(foundClan.getDetail(mob));
				final Pair<Clan,Integer> p=mob.getClanRole(foundClan.clanID());
				if((p!=null)&&(mob.clans().iterator().next().first!=p.first))
				{
					mob.setClan(foundClan.clanID(), mob.getClanRole(foundClan.clanID()).second.intValue());
					msg.append(L("\n\rYour default clan is now @x1.",p.first.getName()));
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
		return false;
	}

}
