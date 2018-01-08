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
public class ClanMorgueSet extends StdCommand
{
	public ClanMorgueSet()
	{
	}

	private final String[]	access	= I(new String[] { "CLANMORGUESET" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final String clanName=(commands.size()>1)?CMParms.combine(commands,1,commands.size()):"";

		Clan C=null;
		final boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks)
			C=mob.getClanRole(mob.Name()).first;

		if(C==null)
		{
			for(final Pair<Clan,Integer> c : mob.clans())
			{
				if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
				&&(c.first.getAuthority(c.second.intValue(), Clan.Function.MORGUE)!=Authority.CAN_NOT_DO))
				{
					C = c.first;
					break;
				}
			}
		}

		Room R=mob.location();
		if(skipChecks)
		{
			commands.set(0,getAccessWords()[0]);
			R=CMLib.map().getRoom(CMParms.combine(commands,1));
		}
		else
		{
			commands.clear();
			commands.add(getAccessWords()[0]);
			commands.add(CMLib.map().getExtendedRoomID(R));
		}

		if((C==null)||(R==null))
		{
			mob.tell(L("You aren't allowed to set a morgue room for @x1.",((clanName.length()==0)?"anything":clanName)));
			return false;
		}

		if(C.getStatus()>Clan.CLANSTATUS_ACTIVE)
		{
			mob.tell(L("You cannot set a morgue.  Your @x1 does not have enough members to be considered active.",C.getGovernmentName()));
			return false;
		}
		if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.SET_HOME,false))
		{
			if(!CMLib.law().doesOwnThisProperty(C.clanID(),R))
			{
				mob.tell(L("Your @x1 does not own this room.",C.getGovernmentName()));
				return false;
			}
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.SET_HOME,true))
			{
				C.setMorgue(CMLib.map().getExtendedRoomID(R));
				C.update();
				mob.tell(L("Your @x1 morgue is now set to @x2.",C.getGovernmentName(),R.displayText(mob)));
				CMLib.clans().clanAnnounce(mob, L("The morgue of @x1 @x2 is now set to @x3.",C.getGovernmentName(),C.clanID(),R.displayText(mob)));
				return true;
			}
		}
		else
		{
			mob.tell(L("You aren't in the right position to set your @x1's morgue.",C.getGovernmentName()));
			return false;
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
