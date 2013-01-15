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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2012 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class ClanExile extends StdCommand
{
	public ClanExile(){}

	private final String[] access={"CLANEXILE"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String memberStr=(commands.size()>1)?(String)commands.get(commands.size()-1):"";
		String clanName=(commands.size()>2)?CMParms.combine(commands,1,commands.size()-1):"";
		
		Clan C=null;
		boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks) C=mob.getClanRole(mob.Name()).first;
			
		if(C==null)
		for(Pair<Clan,Integer> c : mob.clans())
			if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
			&&(c.first.getAuthority(c.second.intValue(), Clan.Function.EXILE)!=Authority.CAN_NOT_DO))
			{	C=c.first; break; }

		commands.clear();
		commands.addElement(getAccessWords()[0]);
		commands.addElement(memberStr);
		
		StringBuffer msg=new StringBuffer("");
		boolean found=false;
		if(memberStr.length()>0)
		{
			if(C==null)
			{
				mob.tell("You aren't allowed to exile anyone from "+((clanName.length()==0)?"anything":clanName)+".");
				return false;
			}
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.EXILE,false))
			{
				List<MemberRecord> apps=C.getMemberList();
				if(apps.size()<1)
				{
					mob.tell("There are no members in your "+C.getGovernmentName()+".");
					return false;
				}
				for(MemberRecord member : apps)
				{
					if(member.name.equalsIgnoreCase(memberStr))
					{
						found=true;
					}
				}
				if(found)
				{
					MOB M=CMLib.players().getLoadPlayer(memberStr);
					if(M==null)
					{
						mob.tell(memberStr+" was not found.  Could not exile from "+C.getGovernmentName()+".");
						return false;
					}
					if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.EXILE,true))
					{
						CMLib.clans().clanAnnounce(mob,"Member exiled from "+C.getGovernmentName()+" "+C.name()+": "+M.Name());
						mob.tell(M.Name()+" has been exiled from "+C.getGovernmentName()+" '"+C.clanID()+"'.");
						if((M.session()!=null)&&(M.session().mob()==M))
							M.tell("You have been exiled from "+C.getGovernmentName()+" '"+C.clanID()+"'.");
						C.delMember(M);
						return false;
					}
				}
				else
				{
					msg.append(memberStr+" isn't a member of your "+C.getGovernmentName()+".");
				}
			}
			else
			{
				msg.append("You aren't in the right position to exile anyone from your "+C.getGovernmentName()+".");
			}
		}
		else
		{
			msg.append("You haven't specified which member you are exiling.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
