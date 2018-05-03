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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
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
@SuppressWarnings({})
public class ClanAccept extends StdCommand
{
	public ClanAccept()
	{
	}

	private final String[]	access	= I(new String[] { "CLANACCEPT" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String memberStr=(commands.size()>1)?(String)commands.get(commands.size()-1):"";
		final String clanName=(commands.size()>2)?CMParms.combine(commands,1,commands.size()-1):"";

		Clan C=null;
		final boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks)
			C=mob.getClanRole(mob.Name()).first;

		if(C==null)
		{
			for(final Pair<Clan,Integer> c : mob.clans())
			{
				if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
				&&(c.first.getAuthority(c.second.intValue(), Clan.Function.ACCEPT)!=Authority.CAN_NOT_DO))
				{
					C = c.first;
					break;
				}
			}
		}

		commands.clear();
		commands.add(getAccessWords()[0]);
		commands.add(memberStr);
		final StringBuffer msg=new StringBuffer("");
		boolean found=false;
		if(memberStr.length()>0)
		{
			if(C==null)
			{
				mob.tell(L("You aren't allowed to accept anyone into @x1.",((clanName.length()==0)?"anything":clanName)));
				return false;
			}
			if(C.getGovernment().getAutoRole() == C.getGovernment().getAcceptPos())
			{
				mob.tell(L("Everyone is already accepted."));
				return false;
			}
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.ACCEPT,false))
			{
				final List<MemberRecord> apps=C.getMemberList(C.getGovernment().getAutoRole());
				if(apps.size()<1)
				{
					mob.tell(L("There are no applicants to your @x1.",C.getGovernmentName()));
					return false;
				}
				memberStr=CMStrings.capitalizeAndLower(memberStr);
				for(final MemberRecord member : apps)
				{
					if(member.name.equalsIgnoreCase(memberStr))
					{
						found=true;
					}
				}
				if(found)
				{
					final MOB M=CMLib.players().getLoadPlayer(memberStr);
					if(M==null)
					{
						mob.tell(L("@x1 was not found.  Could not add to @x2.",memberStr,C.getGovernmentName()));
						return false;
					}
					if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.ACCEPT,true))
					{
						C.addMember(M,C.getGovernment().getAcceptPos());
						if(C.getGovernment().getEntryScript().trim().length()>0)
						{
							final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
							S.setSavable(false);
							S.setVarScope("*");
							S.setScript(C.getGovernment().getEntryScript());
							final CMMsg msg2=CMClass.getMsg(M,M,null,CMMsg.MSG_OK_VISUAL,null,null,L("CLANENTRY"));
							S.executeMsg(M, msg2);
							S.dequeResponses();
							S.tick(M,Tickable.TICKID_MOB);
						}
						CMLib.clans().clanAnnounce(mob,L("@x1 is now a new member of @x2 @x3.",M.Name(),C.getGovernmentName(),C.name()));
						mob.tell(L("@x1 has been accepted into @x2 '@x3'.",M.Name(),C.getGovernmentName(),C.clanID()));
						if((M.session()!=null)&&(M.session().mob()==M))
							M.tell(L("@x1 has accepted you as a member of @x2 '@x3'.",mob.Name(),C.getGovernmentName(),C.clanID()));
						return false;
					}
				}
				else
				{
					msg.append(L("@x1 isn't an applicant of your @x2.",memberStr,C.getGovernmentName()));
				}
			}
			else
			{
				msg.append(L("You aren't in the right position to accept members into your @x1.",C.getGovernmentName()));
			}
		}
		else
		{
			msg.append(L("You haven't specified which applicant you are accepting."));
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
