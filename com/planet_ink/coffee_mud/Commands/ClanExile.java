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
public class ClanExile extends StdCommand
{
	public ClanExile()
	{
	}

	private final String[]	access	= I(new String[] { "CLANEXILE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final String memberStr=(commands.size()>1)?(String)commands.get(commands.size()-1):"";
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
				&&(c.first.getAuthority(c.second.intValue(), Clan.Function.EXILE)!=Authority.CAN_NOT_DO))
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
				mob.tell(L("You aren't allowed to exile anyone from @x1.",((clanName.length()==0)?"anything":clanName)));
				return false;
			}
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.EXILE,false))
			{
				final List<MemberRecord> apps=C.getMemberList();
				if(apps.size()<1)
				{
					mob.tell(L("There are no members in your @x1.",C.getGovernmentName()));
					return false;
				}
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
						mob.tell(L("@x1 was not found.  Could not exile from @x2.",memberStr,C.getGovernmentName()));
						return false;
					}
					if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.EXILE,true))
					{
						if(C.getGovernment().getExitScript().trim().length()>0)
						{
							final Pair<Clan,Integer> curClanRole=M.getClanRole(C.clanID());
							if(curClanRole!=null)
								M.setClan(C.clanID(), curClanRole.second.intValue());
							final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
							S.setSavable(false);
							S.setVarScope("*");
							S.setScript(C.getGovernment().getExitScript());
							final CMMsg msg2=CMClass.getMsg(M,M,null,CMMsg.MSG_OK_VISUAL,null,null,L("CLANEXIT"));
							S.executeMsg(M, msg2);
							S.dequeResponses();
							S.tick(M,Tickable.TICKID_MOB);
						}
						CMLib.clans().clanAnnounce(mob,L("Member exiled from @x1 @x2: @x3",C.getGovernmentName(),C.name(),M.Name()));
						mob.tell(L("@x1 has been exiled from @x2 '@x3'.",M.Name(),C.getGovernmentName(),C.clanID()));
						if((M.session()!=null)&&(M.session().mob()==M))
							M.tell(L("You have been exiled from @x1 '@x2'.",C.getGovernmentName(),C.clanID()));
						C.delMember(M);
						return false;
					}
				}
				else
				{
					msg.append(L("@x1 isn't a member of your @x2.",memberStr,C.getGovernmentName()));
				}
			}
			else
			{
				msg.append(L("You aren't in the right position to exile anyone from your @x1.",C.getGovernmentName()));
			}
		}
		else
		{
			msg.append(L("You haven't specified which member you are exiling."));
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
