package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2005 Bo Zimmerman

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

	private String[] access={getScr("ClanAssign","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt("clanassign",0);
		if(commands.size()<3)
		{
			mob.tell(getScr("ClanAssign","specname"));
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
				msg.append(getScr("ClanAssign","clanassign"));
			}
			else
			{
				C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell(getScr("ClanAssign","nolonger",mob.getClanID()));
					return false;
				}
				if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANASSIGN,false))
				{
					DVector apps=C.getMemberList();
					if(apps.size()<1)
					{
						mob.tell(getScr("ClanAssign","nomembers",C.typeName(),""));
						return false;
					}
					int newPos=getRoleFromName(C.getGovernment(),pos);
					if(newPos<0)
					{
						mob.tell(getScr("ClanAssign","notvrole",pos));
						return false;
					}
					qual=CMStrings.capitalizeAndLower(qual);
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q,1)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMLib.map().getLoadPlayer(qual);
						if(M==null)
						{
							mob.tell(getScr("ClanAssign","notfound",qual,C.typeName()));
							return false;
						}
						if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANASSIGN,true))
						{
						    int oldPos=M.getClanRole();
							int max=Clan.ROL_MAX[C.getGovernment()][getIntFromRole(newPos)];
							Vector olds=new Vector();
							for(int i=0;i<apps.size();i++)
								if(((Integer)apps.elementAt(i,2)).intValue()==newPos)
									olds.addElement(apps.elementAt(i,1));
							if(CMLib.clans().getRoleOrder(oldPos)==Clan.POSORDER.length-1)
							{
							    int numOlds=0;
								for(int i=0;i<apps.size();i++)
								    if(!M.Name().equalsIgnoreCase((String)apps.elementAt(i,1)))
										if(((Integer)apps.elementAt(i,2)).intValue()==oldPos)
											numOlds++;
								if(numOlds==0)
								{
								    mob.tell(getScr("ClanAssign","replaced",M.Name(),CMLib.clans().getRoleName(C.getGovernment(),oldPos,true,false)));
								    return false;
								}
							}
							if((olds.size()>0)&&(max<Integer.MAX_VALUE))
							{
								if(max==1)
								{
									for(int i=0;i<olds.size();i++)
									{
										String s=(String)olds.elementAt(i);
										clanAnnounce(mob,getScr("ClanAssign","isnowa",C.typeName(),C.clanID(),s,CMLib.clans().getRoleName(C.getGovernment(),Clan.POS_MEMBER,true,false)));
										MOB M2=CMLib.map().getPlayer(s);
										if(M2!=null) M2.setClanRole(Clan.POS_MEMBER);
										CMLib.database().DBUpdateClanMembership(s, C.clanID(), Clan.POS_MEMBER);
										C.updateClanPrivileges(M2);
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
											clanAnnounce(mob,getScr("ClanAssign","isnowa",C.typeName(),C.clanID(),s,CMLib.clans().getRoleName(C.getGovernment(),Clan.POS_MEMBER,true,false)));
											MOB M2=CMLib.map().getPlayer(s);
											if(M2!=null) M2.setClanRole(Clan.POS_MEMBER);
											CMLib.database().DBUpdateClanMembership(s, C.clanID(), Clan.POS_MEMBER);
											C.updateClanPrivileges(M2);
										}
									}
								}
							}
							clanAnnounce(mob,getScr("ClanAssign","changedfrom",M.name(),C.typeName(),C.clanID(),CMLib.clans().getRoleName(C.getGovernment(),M.getClanRole(),true,false),CMLib.clans().getRoleName(C.getGovernment(),newPos,true,false)));
							M.setClanRole(newPos);
							C.updateClanPrivileges(M);
							CMLib.database().DBUpdateClanMembership(M.Name(), C.clanID(), newPos);
							mob.tell(getScr("ClanAssign","assigned",M.Name(),C.typeName(),C.clanID(),CMStrings.startWithAorAn(CMLib.clans().getRoleName(C.getGovernment(),newPos,false,false))));
							M.tell(getScr("ClanAssign","youassigned",CMStrings.startWithAorAn(CMLib.clans().getRoleName(C.getGovernment(),newPos,false,false)),C.typeName(),C.clanID()));
							return false;
						}
					}
					else
					{
						msg.append(getScr("ClanAssign","nome",qual,C.typeName()));
					}
				}
				else
				{
					msg.append(getScr("ClanAssign","youarent",C.typeName()));
				}
			}
		}
		else
		{
			msg.append(getScr("ClanAssign","specmem"));
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
