package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class ClanAccept extends BaseClanner
{
	public ClanAccept(){}

	private String[] access={"CLANACCEPT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());

		commands.setElementAt("clanaccept",0);
		String qual=Util.combine(commands,1).toUpperCase();
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
				if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANACCEPT,false))
				{
					DVector apps=C.getMemberList(Clan.POS_APPLICANT);
					if(apps.size()<1)
					{
						mob.tell("There are no applicants to your "+C.typeName()+".");
						return false;
					}
					qual=Util.capitalizeAndLower(qual);
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
							mob.tell(qual+" was not found.  Could not add to "+C.typeName()+".");
							return false;
						}
						if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANACCEPT,true))
						{
							clanAnnounce(mob,"New Member of "+C.name()+": "+M.Name());
							M.setClanID(mob.getClanID());
							M.setClanRole(Clan.POS_MEMBER);
							CMClass.DBEngine().DBUpdateClanMembership(qual, mob.getClanID(), Clan.POS_MEMBER);
							mob.tell(M.Name()+" has been accepted into "+C.typeName()+" '"+C.ID()+"'.");
							M.tell(mob.Name()+" has accepted you as a member of "+C.typeName()+" '"+C.ID()+"'.");
							C.updateClanPrivileges(M);
							return false;
						}
					}
					else
					{
						msg.append(qual+" isn't an applicant of your "+C.typeName()+".");
					}
				}
				else
				{
					msg.append("You aren't in the right position to accept members into your "+C.typeName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which applicant you are accepting.");
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
