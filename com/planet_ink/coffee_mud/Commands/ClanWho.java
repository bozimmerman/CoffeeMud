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
public class ClanWho extends Who
{
	public ClanWho()
	{
	}

	private final String[]	access	= I(new String[] { "CLANWHO", "CLWH" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final String clanName=CMParms.combine(commands,1).toUpperCase();
		final StringBuffer msg=new StringBuffer("");
		final List<String> clanList=new XVector<String>();
		MOB whoM = null;
		if(clanName.trim().length()>0)
		{
			final Clan C=CMLib.clans().findClan(clanName);
			if(C==null)
			{
				if(CMLib.players().playerExists(clanName))
				{
					for(final Pair<Clan,Integer> c : mob.clans())
					{
						if(c.first.getAuthority(c.second.intValue(), Clan.Function.CLAN_BENEFITS) != Clan.Authority.CAN_NOT_DO)
						{
							final MOB M = CMLib.players().getLoadPlayer(clanName);
							if(M!=null)
							{
								if(M.getClanRole(c.first.clanID())!=null)
								{
									whoM=M;
									clanList.add(c.first.clanID());
								}
							}
						}
					}
					if(whoM == null)
						mob.tell(L("You can't clanwho any such clan or player as '@x1'.",clanName));
				}
				else
					mob.tell(L("There's no such clan or player as '@x1'.",clanName));
			}
			else
				clanList.add(C.clanID());
		}
		else
		{
			for(final Pair<Clan,Integer> c : mob.clans())
				clanList.add(c.first.clanID());
			if(clanList.size()==0)
			{
				mob.tell(L("You need to specify a clan."));
				return false;
			}
		}
		final Set<MOB> alreadyDone=new HashSet<MOB>();
		final int[] colWidths=getShortColWidths(mob);
		for(final String clanID : clanList)
		{
			final Clan C=CMLib.clans().getClan(clanID);
			if(C!=null)
			{
				msg.append("\n\r^x").append(C.getGovernmentName()).append(" ").append(C.getName()).append("^.^N\n\r");
				if(whoM != null)
				{
					final Clan.MemberRecord M=C.getMember(whoM.Name());
					if(M != null)
					{
						msg.append(CMStrings.padRight("^HName", 10)).append(": ^N").append(whoM.name(mob)).append("\n\r");
						msg.append(CMStrings.padRight("^HLevel", 10)).append(": ^N").append(whoM.phyStats().level()).append("\n\r");
						msg.append(CMStrings.padRight("^HRace", 10)).append(": ^N")
							.append(whoM.charStats().raceName()).append(" (").append(whoM.baseCharStats().genderName()).append(") ")
							.append("\n\r");
						for(final CharClass lC : whoM.charStats().getCharClasses())
						{
							msg.append(CMStrings.padRight("^HClass", 10)).append(": ^N")
								.append(lC.name(whoM.charStats().getClassLevel(lC)))
								.append(" (").append(whoM.charStats().getClassLevel(lC)).append(")")
								.append("\n\r");
						}
						msg.append(CMStrings.padRight("^HRank", 10)).append(": ^N").append(C.getRoleName(M.role, true, false)).append("\n\r");
						msg.append(CMStrings.padRight("^HJoined", 10)).append(": ^N").append(CMLib.time().date2String(M.joinDate)).append("\n\r");
						msg.append(CMStrings.padRight("^HPC Kills", 10)).append(": ^N").append(M.playerpvps).append("\n\r");
						msg.append(CMStrings.padRight("^HNPC Kills", 10)).append(": ^N").append(M.mobpvps).append("\n\r");
						msg.append(CMStrings.padRight("^H$ Donated", 10)).append(": ^N")
							.append(CMLib.beanCounter().abbreviatedPrice(whoM, M.donatedGold))
							.append("\n\r");
						msg.append(CMStrings.padRight("^HXP Donated", 10)).append(": ^N").append(M.donatedXP).append("\n\r");
					}
				}
				else
				{
					msg.append(getHead(colWidths));
					for(final Session S : CMLib.sessions().localOnlineIterable())
					{
						MOB mob2=S.mob();
						if((mob2!=null)&&(mob2.soulMate()!=null))
							mob2=mob2.soulMate();

						if((mob2!=null)
						&&(!S.isStopped())
						&&((((mob2.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
							||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))&&(mob.phyStats().level()>=mob2.phyStats().level()))))
						&&(mob2.getClanRole(C.clanID())!=null)
						&&(!alreadyDone.contains(mob2))
						&&(CMLib.flags().isInTheGame(mob2,true))
						&&(mob2.phyStats().level()>0))
						{
							msg.append(showWhoShort(mob2,mob,colWidths));
							alreadyDone.add(mob2);
						}
					}
				}
			}
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
