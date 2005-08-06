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
public class ClanResign extends BaseClanner
{
	public ClanResign(){}

	private String[] access={"CLANRESIGN"};
	public String[] getAccessWords(){return access;}
    
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase("")))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		if(!mob.isMonster())
		{
			Clan C=Clans.getClan(mob.getClanID());
			try
			{
				String check=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
				if(check.equalsIgnoreCase("Y"))
				{
					if(C!=null)
						clanAnnounce(mob,new String("Member resigned from "+C.name()+": "+mob.Name()));
					CMClass.DBEngine().DBUpdateClanMembership(mob.Name(), "", 0);
					mob.setClanID("");
					mob.setClanRole(0);
					if(C!=null)
					{
						C.updateClanPrivileges(mob);
						CMClass.DBEngine().DBUpdateClanMembership(mob.Name(),"",0);
					}
					else
						CMClass.DBEngine().DBUpdateClanMembership(mob.Name(),"",0);
				}
				else
				{
					return false;
				}
			}
			catch(java.io.IOException e)
			{
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
