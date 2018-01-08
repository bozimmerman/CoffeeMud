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
public class ClanDeclare extends StdCommand
{
	public ClanDeclare()
	{
	}

	private final String[]	access	= I(new String[] { "CLANDECLARE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You must specify the clans name, and a new relationship."));
			return false;
		}
		commands.set(0,getAccessWords()[0]);
		final String rel=commands.get(commands.size()-1).toUpperCase();
		Clan C=null;
		Clan C2=null;
		String clanName="";

		final boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks)
			C=mob.getClanRole(mob.Name()).first;

		String clan2Name=CMParms.combine(commands,1,commands.size()-1);
		C2=CMLib.clans().findClan(clan2Name);
		if((C2==null)&&(C==null)&&(commands.size()>3))
		{
			clanName=commands.get(1);
			clan2Name=CMParms.combine(commands,2,commands.size()-1);
		}

		if(C==null)
		{
			for(final Pair<Clan,Integer> c : mob.clans())
			{
				if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
				&&(c.first.getAuthority(c.second.intValue(), Clan.Function.DECLARE)!=Authority.CAN_NOT_DO))
				{
					C = c.first;
					break;
				}
			}
		}
		if(C2==null)
		{
			for(final Pair<Clan,Integer> c : mob.clans())
			{
				if(CMLib.english().containsString(c.first.getName(), clan2Name))
				{
					C2 = c.first;
					break;
				}
			}
		}

		if(C2==null)
		{
			mob.tell(L("@x1 is an unknown clan.",clan2Name));
			return false;
		}

		if(C==null)
		{
			mob.tell(L("You aren't allowed to declare @x1 on behalf of @x2.",rel.toLowerCase(),((clanName.length()==0)?"anything":clanName)));
			return false;
		}

		if((!C2.isRivalrous())||(!C.isRivalrous()))
		{
			mob.tell(L("Relations between @x1 and @x2 are impossible.",C.getName(),C2.getName()));
			return false;
		}

		final StringBuffer msg=new StringBuffer("");
		if(rel.length()>0)
		{
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.DECLARE,false))
			{
				int newRole=-1;
				for(int i=0;i<Clan.REL_DESCS.length;i++)
				{
					if(rel.equalsIgnoreCase(Clan.REL_DESCS[i]))
						newRole=i;
				}
				if(newRole<0)
				{
					mob.tell(L("'@x1' is not a valid relationship. Try WAR, HOSTILE, NEUTRAL, FRIENDLY, or ALLY.",rel));
					return false;
				}
				if(C2==C)
				{
					mob.tell(L("You can't do that."));
					return false;
				}
				if(C.getClanRelations(C2.clanID())==newRole)
				{
					mob.tell(L("@x1 is already in that state with @x2.",C.getName(),C2.getName()));
					return false;

				}
				long last=C.getLastRelationChange(C2.clanID());
				if(last>(CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)*CMProps.getTickMillis()))
				{
					last=last+(CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)*CMProps.getTickMillis());
					if(System.currentTimeMillis()<last)
					{
						mob.tell(L("You must wait at least 1 mud month between relation changes."));
						return false;
					}
				}
				commands.clear();
				commands.add(getAccessWords()[0]);
				commands.add(clan2Name);
				commands.add(rel);
				if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.DECLARE,true))
				{
					CMLib.clans().clanAnnounce(mob,L("The @x1 @x2 has declared @x3 @x4.",C.getGovernmentName(),C.clanID(),CMStrings.capitalizeAndLower(Clan.REL_STATES[newRole].toLowerCase()),C2.name()));
					C.setClanRelations(C2.clanID(),newRole,System.currentTimeMillis());
					C.update();
					return false;
				}
			}
			else
			{
				msg.append(L("You aren't in the right position to declare relationships with your @x1.",C.getGovernmentName()));
			}
		}
		else
		{
			mob.tell(L("You must specify the clans name, and a new relationship."));
			return false;
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
