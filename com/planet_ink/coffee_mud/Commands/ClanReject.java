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
public class ClanReject extends BaseClanner
{
	public ClanReject(){}

	private String[] access={"CLANREJECT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt("clanreject",0);

		String qual=CMParms.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
				msg.append("You aren't even a member of a clan.");
			else
			{
				C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANREJECT,false))
				{
					DVector apps=C.getMemberList(Clan.POS_APPLICANT);
					if(apps.size()<1)
					{
						mob.tell("There are no applicants to your "+C.typeName()+".");
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
							mob.tell(qual+" was not found.  Could not reject from "+C.typeName()+".");
							return false;
						}
						if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANREJECT,true))
						{
							M.setClanID("");
							M.setClanRole(0);
							CMLib.database().DBUpdateClanMembership(M.Name(), "", 0);
							mob.tell(M.Name()+" has been denied acceptance to "+C.typeName()+" '"+C.clanID()+"'.");
							M.tell("You have been rejected as a member of "+C.typeName()+" '"+C.clanID()+"'.");
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
				  msg.append("You aren't in the right position to reject applicants to your "+C.typeName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which applicant you are rejecting.");
		}
		mob.tell(msg.toString());
		return false;
	}
	public int actionsCost(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
