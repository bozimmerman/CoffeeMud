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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class ClanAssign extends StdCommand
{
	public ClanAssign(){}

	private final String[] access={"CLANASSIGN"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You must specify the members name, and a new role.");
			return false;
		}
		String memberStr=(commands.size()>2)?(String)commands.get(commands.size()-2):"";
		String pos=(commands.size()>1)?(String)commands.get(commands.size()-1):"";
		String clanName=(commands.size()>3)?CMParms.combine(commands,1,commands.size()-2):"";
		
		Clan C=null;
		boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks) C=mob.getClanRole(mob.Name()).first;
			
		if(C==null)
		for(Pair<Clan,Integer> c : mob.clans())
			if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
			&&(c.first.getAuthority(c.second.intValue(), Clan.Function.ASSIGN)!=Authority.CAN_NOT_DO))
			{	C=c.first; break; }
		
		commands.clear();
		commands.add(getAccessWords()[0]);
		commands.add(memberStr);
		commands.add(pos);
		StringBuffer msg=new StringBuffer("");
		boolean found=false;
		if(memberStr.length()>0)
		{
			if(C==null)
			{
				mob.tell("You aren't allowed to assign anyone from "+((clanName.length()==0)?"anything":clanName)+".");
				return false;
			}
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.ASSIGN,false))
			{
				List<MemberRecord> members=C.getMemberList();
				if(members.size()<1)
				{
					mob.tell("There are no members in your "+C.getGovernmentName()+"");
					return false;
				}
				int newPos=C.getRoleFromName(pos);
				if(newPos<0)
				{
					mob.tell("'"+pos+"' is not a valid role.");
					return false;
				}
				memberStr=CMStrings.capitalizeAndLower(memberStr);
				for(MemberRecord member : members)
				{
					if(member.name.equalsIgnoreCase(memberStr))
					{
						found=true;
					}
				}
				if(found)
				{
					MOB M=CMLib.players().getLoadPlayer(memberStr);
					Pair<Clan,Integer> oldRole=(M!=null)?M.getClanRole(C.clanID()):null;
					if((M==null)||(oldRole==null))
					{
						mob.tell(memberStr+" was not found.  Could not change "+C.getGovernmentName()+" role.");
						return false;
					}
					if(!C.canBeAssigned(M, newPos))
					{
						mob.tell(M.name(mob)+" may not be assigned to "+C.getRoleName(newPos,true,false)+".");
						return false;
					}
					if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.ASSIGN,true))
					{
						int oldPos=oldRole.second.intValue();
						int maxInNewPos=C.getMostInRole(newPos);
						Vector currentMembersInNewPosV=new Vector();
						for(MemberRecord member : members)
							if(member.role==newPos)
								currentMembersInNewPosV.addElement(member.name);
						List<Integer> topRoleIDs=C.getTopRankedRoles(Function.ASSIGN);
						if(topRoleIDs.contains(Integer.valueOf(oldPos)))
						{ // If you WERE already the highest order.. you must be being demoted.
							// so we check to see if there will be any other high officers left
							int numMembers=0;
							for(MemberRecord member : members)
								if(!M.Name().equalsIgnoreCase(member.name))
									if(topRoleIDs.contains(Integer.valueOf(member.role)))
										numMembers++;
							if(numMembers==0)
							{
								mob.tell(M.Name()+" is the last "+C.getRoleName(oldPos,true,false)+" and must be replaced before being reassigned.");
								return false;
							}
						}
						if((currentMembersInNewPosV.size()>0)&&(maxInNewPos<Integer.MAX_VALUE))
						{
							// if there are too many in the new position, demote some of them.
							while(currentMembersInNewPosV.size()>=maxInNewPos)
							{
								String s=(String)currentMembersInNewPosV.elementAt(0);
								currentMembersInNewPosV.removeElementAt(0);
								CMLib.clans().clanAnnounce(mob," "+s+" of the "+C.getGovernmentName()+" "+C.clanID()+" is now a "+C.getRoleName(C.getGovernment().getAcceptPos(),true,false)+".");
								MOB M2=CMLib.players().getPlayer(s);
								if(M2!=null) M2.setClan(C.clanID(),C.getGovernment().getAcceptPos());
								CMLib.database().DBUpdateClanMembership(s, C.clanID(), C.getGovernment().getAcceptPos());
								C.updateClanPrivileges(M2);
							}
						}
						// finally, promote
						CMLib.clans().clanAnnounce(mob,M.name()+" of the "+C.getGovernmentName()+" "+C.clanID()+" changed from "+C.getRoleName(oldRole.second.intValue(),true,false)
								+" to "+C.getRoleName(newPos,true,false)+".");
						C.addMember(M,newPos);
						mob.tell(M.Name()+" of the "+C.getGovernmentName()+" "+C.clanID()+" has been assigned to be "+CMLib.english().startWithAorAn(C.getRoleName(newPos,false,false))+". ");
						if((M.session()!=null)&&(M.session().mob()==M))
							M.tell("You have been assigned to be "+CMLib.english().startWithAorAn(C.getRoleName(newPos,false,false))+" of "+C.getGovernmentName()+" "+C.clanID()+".");
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
				msg.append("You aren't in the right position to assign anyone in your "+C.getGovernmentName()+".");
			}
		}
		else
		{
			msg.append("You haven't specified which member you are assigning a new role to.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
