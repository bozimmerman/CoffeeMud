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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Trophy;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2020 Bo Zimmerman

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
public class ClanList extends StdCommand
{
	public ClanList()
	{
	}

	private final String[]	access	= I(new String[] { "CLANLIST", "CLANS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final boolean trophySystemActive=CMLib.clans().trophySystemActive();
		final StringBuffer head=new StringBuffer("");
		head.append("^x[");
		head.append(CMStrings.padRight(L("Clan Name"),30)+"| ");
		head.append(CMStrings.padRight(L("Type"),10)+"| ");
		head.append(CMStrings.padRight(L("#"),4)+"| ");
		head.append(CMStrings.padRight(L("Status"),14));
		if(trophySystemActive)
			head.append(" | "+CMStrings.padRight(L("Trophies"),8));
		head.append("]^.^? \n\r");
		final StringBuffer msg=new StringBuffer("");
		final List<Clan> clans=new ArrayList<Clan>(CMLib.clans().numClans());
		for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
			clans.add(e.nextElement());
		Collections.sort(clans, new Comparator<Clan>()
		{
			@Override
			public int compare(final Clan o1, final Clan o2)
			{
				if(o1.getStatus() > o2.getStatus())
					return 1;
				if(o1.getStatus() < o2.getStatus())
					return -1;
				if(o2.getSize()==o1.getSize())
					return 0;
				if(o2.getSize()<o1.getSize())
					return -1;
				return 1;
			}
		});

		for(final Clan thisClan : clans)
		{
			if(!thisClan.isPubliclyListedFor(mob))
				continue;

			final StringBuffer trophySet = new StringBuffer("");
			if(trophySystemActive)
			{
				for(final Trophy t : Trophy.values())
				{
					if(CMath.bset(thisClan.getTrophies(),t.flagNum()))
					{
						if(trophySet.length()>6)
						{
							trophySet.append('+');
							break;
						}
						else
						trophySet.append(t.shortChar);
					}
				}
			}

			msg.append(" ");
			msg.append("^<CLAN^>"+CMStrings.padRight(CMStrings.removeColors(thisClan.clanID()),30)+"^</CLAN^>  ");
			msg.append(CMStrings.padRight(thisClan.getGovernmentName(),10)+"  ");
			boolean war=false;
			for(final Enumeration<Clan> e2=CMLib.clans().clans();e2.hasMoreElements();)
			{
				final Clan C=e2.nextElement();
				if((C!=thisClan)
				&&(thisClan.getClanRelations(C.clanID())==Clan.REL_WAR))
				{
					if(C.getClanRelations(thisClan.clanID())==Clan.REL_WAR)
					{
						war=true;
						break;
					}
					else
					{
					}
				}
			}
			String status = war?"^r":"^g";
			switch(thisClan.getStatus())
			{
			case Clan.CLANSTATUS_FADING:
			case Clan.CLANSTATUS_STAGNANT:
				status+=L("Inactive");
				break;
			case Clan.CLANSTATUS_PENDING:
				status+=L("Pending");
				break;
			default:
				status+=L("Active");
			}
			if(war)
				status += " (War)";
			status += "^N";
			msg.append(CMStrings.padRight(Integer.toString(thisClan.getSize()),4)+"  ");
			msg.append(CMStrings.padRight(status,14)+"  ");
			msg.append(trophySet);
			msg.append("\n\r");
		}
		mob.tell(head.toString()+msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
