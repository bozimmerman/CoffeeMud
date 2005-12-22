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
   Copyright 2000-2006 Bo Zimmerman

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
public class ClanDeclare extends BaseClanner
{
	public ClanDeclare(){}

	private String[] access={getScr("ClanDeclare","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());

		commands.setElementAt("clandeclare",0);
		if(commands.size()<3)
		{
			mob.tell(getScr("ClanDeclare","specname"));
			return false;
		}
		String rel=((String)commands.lastElement()).toUpperCase();
		commands.removeElementAt(commands.size()-1);
		String clan=CMParms.combine(commands,1).toUpperCase();
		commands.addElement(rel);
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		if((clan.length()>0)&&(rel.length()>0))
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append(getScr("ClanDeclare","evenmember"));
			}
			else
			{
				C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell(getScr("ClanDeclare","nolonger",mob.getClanID()));
					return false;
				}
				if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANDECLARE,false))
				{
					int newRole=-1;
					for(int i=0;i<Clan.REL_DESCS.length;i++)
						if(rel.equalsIgnoreCase(Clan.REL_DESCS[i]))
							newRole=i;
					if(newRole<0)
					{
						mob.tell(getScr("ClanDeclare","notvrel",rel));
						return false;
					}
					Clan C2=CMLib.clans().findClan(clan);
					if(C2==null)
					{
						mob.tell(getScr("ClanDeclare","notvclan",clan));
						return false;
					}
					if(C2==C)
					{
						mob.tell(getScr("ClanDeclare","cantdothat"));
						return false;
					}
					if(C.getClanRelations(C2.clanID())==newRole)
					{
						mob.tell(getScr("ClanDeclare","alreadystate",C2.clanID()));
						return false;

					}
					long last=C.getLastRelationChange(C2.clanID());
					if(last>(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)*MudHost.TICK_TIME))
					{
						last=last+(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)*MudHost.TICK_TIME);
						if(System.currentTimeMillis()<last)
						{
							mob.tell(getScr("ClanDeclare","waitforrel"));
							return false;
						}
					}
					if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANDECLARE,true))
					{
						clanAnnounce(mob,getScr("ClanDeclare","declared",C.typeName(),C.clanID(),CMStrings.capitalizeAndLower(Clan.REL_STATES[newRole].toLowerCase()),C2.name()));
						C.setClanRelations(C2.clanID(),newRole,System.currentTimeMillis());
						C.update();
						return false;
					}
				}
				else
				{
					msg.append(getScr("ClanDeclare","notright",C.typeName()));
				}
			}
		}
		else
		{
			mob.tell(getScr("ClanDeclare","specnameandrel"));
			return false;
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
