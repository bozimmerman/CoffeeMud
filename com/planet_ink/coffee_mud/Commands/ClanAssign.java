package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class ClanAssign extends BaseClanner
{
	public ClanAssign(){}

	private String[] access={"CLANASSIGN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt("clanassign",0);
		if(commands.size()<3)
		{
			mob.tell("You must specify the members name, and a new role.");
			return false;
		}
		String qual=((String)commands.elementAt(1)).toUpperCase();
		String pos=Util.combine(commands,2).toUpperCase();
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
				C=Clans.getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANASSIGN,false))
				{
					DVector apps=C.getMemberList();
					if(apps.size()<1)
					{
						mob.tell("There are no members in your "+C.typeName()+"");
						return false;
					}
					int newPos=getRoleFromName(C.getGovernment(),pos);
					if(newPos<0)
					{
						mob.tell("'"+pos+"' is not a valid role.");
						return false;
					}
					qual=Util.capitalize(qual);
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q,1)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMMap.getLoadPlayer(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not change "+C.typeName()+" role.");
							return false;
						}
						else
						{
							if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANASSIGN,true))
							{
								int max=Clan.ROL_MAX[C.getGovernment()][getIntFromRole(newPos)];
								Vector olds=new Vector();
								for(int i=0;i<apps.size();i++)
									if(((Integer)apps.elementAt(i,2)).intValue()==newPos)
										olds.addElement(apps.elementAt(i,1));
								if((olds.size()>0)&&(max<Integer.MAX_VALUE))
								{
									if(max==1)
									{
										for(int i=0;i<olds.size();i++)
										{
											String s=(String)olds.elementAt(i);
											clanAnnounce(mob,s+" is now a "+Clans.getRoleName(C.getGovernment(),Clan.POS_MEMBER,true,false));
											MOB M2=CMMap.getPlayer(s);
											if(M2!=null) M2.setClanRole(Clan.POS_MEMBER);
											CMClass.DBEngine().DBUpdateClanMembership(s, C.ID(), Clan.POS_MEMBER);
										}
									}
									else
									{
										if(olds.size()>3)
										{
											max=olds.size()/max;
											while((olds.size()>max)&&(olds.size()>3))
											{
												String s=(String)olds.elementAt(0);
												apps.removeElementAt(0);
												clanAnnounce(mob,s+" is now a "+Clans.getRoleName(C.getGovernment(),Clan.POS_MEMBER,true,false));
												MOB M2=CMMap.getPlayer(s);
												if(M2!=null) M2.setClanRole(Clan.POS_MEMBER);
												CMClass.DBEngine().DBUpdateClanMembership(s, C.ID(), Clan.POS_MEMBER);
											}
										}
									}
								}
								clanAnnounce(mob,M.Name()+" changed from "+Clans.getRoleName(C.getGovernment(),M.getClanRole(),true,false)+" to "+Clans.getRoleName(C.getGovernment(),newPos,true,false));
								M.setClanRole(newPos);
								CMClass.DBEngine().DBUpdateClanMembership(M.Name(), C.ID(), newPos);
								mob.tell(M.Name()+" has been assigned to be "+Util.startWithAorAn(Clans.getRoleName(C.getGovernment(),newPos,false,false))+" of "+C.typeName()+" '"+C.ID()+"'.");
								M.tell("You have been assigned to be "+Util.startWithAorAn(Clans.getRoleName(C.getGovernment(),newPos,false,false))+" of "+C.typeName()+" '"+C.ID()+"'.");
								return false;
							}
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
