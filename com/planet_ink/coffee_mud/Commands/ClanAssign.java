package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.List;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class ClanAssign extends StdCommand
{
	public ClanAssign(){}

	private String[] access={"CLANASSIGN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt(getAccessWords()[0],0);
		if(commands.size()<3)
		{
			mob.tell("You must specify the members name, and a new role.");
			return false;
		}
		String qual=((String)commands.elementAt(1)).toUpperCase();
		String pos=CMParms.combine(commands,2).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append("You aren't even a member of a clan.");
			}
			else
			{
				C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.FUNC_CLANASSIGN,false))
				{
					List<MemberRecord> members=C.getMemberList();
					if(members.size()<1)
					{
						mob.tell("There are no members in your "+C.typeName()+"");
						return false;
					}
					int newPos=CMLib.clans().getRoleFromName(C.getGovernment(),pos);
					if(newPos<0)
					{
						mob.tell("'"+pos+"' is not a valid role.");
						return false;
					}
					qual=CMStrings.capitalizeAndLower(qual);
					for(MemberRecord member : members)
					{
						if(member.name.equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMLib.players().getLoadPlayer(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not change "+C.typeName()+" role.");
							return false;
						}
						if(!C.canBeAssigned(M, newPos))
						{
							mob.tell(M.name()+" may not be assigned to "+CMLib.clans().getRoleName(C.getGovernment(),newPos,true,false)+".");
							return false;
						}
						if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.FUNC_CLANASSIGN,true))
						{
						    int oldPos=M.getClanRole();
							int maxInNewPos=Clan.ROL_MAX[C.getGovernment()][CMLib.clans().getIntFromRole(newPos)];
							Vector currentMembersInNewPosV=new Vector();
							for(MemberRecord member : members)
								if(member.role==newPos)
									currentMembersInNewPosV.addElement(member.name);
							if(CMLib.clans().getRoleOrder(oldPos)==Clan.POSORDER.length-1)
							{ // If you WERE already the highest order.. you must be being demoted.
								// so we check to see if there will be any other high officers left
							    int numMembers=0;
								for(MemberRecord member : members)
								    if(!M.Name().equalsIgnoreCase(member.name))
										if(member.role==oldPos)
											numMembers++;
								if(numMembers==0)
								{
								    mob.tell(M.Name()+" is the last "+CMLib.clans().getRoleName(C.getGovernment(),oldPos,true,false)+" and must be replaced before being reassigned.");
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
									CMLib.clans().clanAnnounce(mob," "+s+" of the "+C.typeName()+" "+C.clanID()+" is now a "+CMLib.clans().getRoleName(C.getGovernment(),Clan.POS_MEMBER,true,false)+".");
									MOB M2=CMLib.players().getPlayer(s);
									if(M2!=null) M2.setClanRole(Clan.POS_MEMBER);
									CMLib.database().DBUpdateClanMembership(s, C.clanID(), Clan.POS_MEMBER);
									C.updateClanPrivileges(M2);
								}
							}
							// finally, promote
							CMLib.clans().clanAnnounce(mob,M.name()+" of the "+C.typeName()+" "+C.clanID()+" changed from "+CMLib.clans().getRoleName(C.getGovernment(),M.getClanRole(),true,false)+" to "+CMLib.clans().getRoleName(C.getGovernment(),newPos,true,false)+".");
                            C.addMember(M,newPos);
							mob.tell(M.Name()+" of the "+C.typeName()+" "+C.clanID()+" has been assigned to be "+CMLib.english().startWithAorAn(CMLib.clans().getRoleName(C.getGovernment(),newPos,false,false))+". ");
							M.tell("You have been assigned to be "+CMLib.english().startWithAorAn(CMLib.clans().getRoleName(C.getGovernment(),newPos,false,false))+" of "+C.typeName()+" "+C.clanID()+".");
							return false;
						}
					}
					else
					{
						msg.append(qual+" isn't a member of your "+C.typeName()+".");
					}
				}
				else
				{
					msg.append("You aren't in the right position to assign anyone in your "+C.typeName()+".");
				}
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
