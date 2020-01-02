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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class ClanMOTD extends StdCommand
{
	public ClanMOTD()
	{
	}

	private final String[]	access	= I(new String[] { "CLANMOTD" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final String clanName=(commands.size()>1)?CMParms.combine(commands,1,commands.size()):"";

		Clan chkC=null;
		final boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks)
			chkC=mob.getClanRole(mob.Name()).first;

		if(chkC==null)
		{
			for(final Pair<Clan,Integer> c : mob.clans())
			{
				if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
				&&(c.first.getAuthority(c.second.intValue(), Clan.Function.CLAN_MOTD)!=Authority.CAN_NOT_DO))
				{
					chkC = c.first;
					break;
				}
			}
		}

		commands.set(0,getAccessWords()[0]);

		final Clan C=chkC;
		if(C==null)
		{
			mob.tell(L("You aren't allowed to set the message for @x1.",((clanName.length()==0)?"anything":clanName)));
			return false;
		}

		if((!skipChecks)&&(!CMLib.clans().goForward(mob,C,commands,Clan.Function.PREMISE,false)))
		{
			mob.tell(L("You aren't in the right position to set the message for your @x1.",C.getGovernmentName()));
			return false;
		}
		if((skipChecks)&&(commands.size()>1))
		{
			addClanMOTD(mob,C,CMParms.combine(commands,1));
			return false;
		}
		final Session session=mob.session();
		if(session==null)
		{
			return false;
		}
		session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
		{
			@Override
			public void showPrompt()
			{
				session.promptPrint(L("Enter the message (or DELETE)\n\r: "));
			}

			@Override
			public void timedOut()
			{
			}

			@Override
			public void callBack()
			{
				final String premise=this.input;
				if(premise.length()==0)
				{
					return;
				}
				if(premise.equalsIgnoreCase("DELETE")||premise.equalsIgnoreCase("CLEAR"))
				{
					CMLib.clans().clanAnnounce(mob,L("Clan MOTD has been cleared. "));
					CMLib.database().DBDeleteJournal("CLAN_MOTD_"+C.clanID(), null);
					return;
				}
				final Vector<String> cmds=new Vector<String>();
				cmds.add(getAccessWords()[0]);
				cmds.add(premise);
				if(skipChecks||CMLib.clans().goForward(mob,C,cmds,Clan.Function.CLAN_MOTD,true))
				{
					addClanMOTD(mob,C,premise);
				}
			}
		});
		return false;
	}

	public void addClanMOTD(final MOB mob, final Clan C, final String text)
	{
		final String subj = L("@x1 @x2 Message of the Day",C.getGovernmentName(), C.clanID());
		CMLib.database().DBDeleteJournal("CLAN_MOTD_"+C.clanID(), null);
		CMLib.database().DBWriteJournal("CLAN_MOTD_"+C.clanID(), mob.Name(), C.clanID(), subj, text);
		CMLib.clans().clanAnnounce(mob,L("New MOTD: ")+text);
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
