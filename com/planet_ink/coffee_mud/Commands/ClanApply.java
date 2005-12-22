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
public class ClanApply extends BaseClanner
{
	public ClanApply(){}

	private String[] access={getScr("ClanApply","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.setElementAt("clanapply",0);
		String qual=CMParms.combine(commands,1).toUpperCase();
		if(mob.isMonster()) return false;
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				Clan C=CMLib.clans().findClan(qual);
				if(C!=null)
				{
					if(CMLib.masking().maskCheck(C.getAcceptanceSettings(),mob))
					{
                        if(CMLib.masking().maskCheck("-<"+CMProps.getIntVar(CMProps.SYSTEMI_MINCLANLEVEL),mob))
                        {
    						CMLib.database().DBUpdateClanMembership(mob.Name(), C.clanID(), Clan.POS_APPLICANT);
    						mob.setClanID(C.clanID());
    						mob.setClanRole(Clan.POS_APPLICANT);
    						clanAnnounce(mob,getScr("ClanApply","new",C.typeName(),C.clanID(),mob.Name()));
    						mob.tell(getScr("ClanApply","membapplied",C.clanID()));
                        }
                        else
                        {
                            msg.append(getScr("ClanApply","leastlev",CMProps.getIntVar(CMProps.SYSTEMI_MINCLANLEVEL)+""));
                        }
					}
					else
					{
						msg.append(getScr("ClanApply","nrq",C.clanID()));
					}
				}
				else
				{
					msg.append(getScr("ClanApply","noclan",qual));
				}
			}
			else
			{
				msg.append(getScr("ClanApply","almember",mob.getClanID()));
			}
		}
		else
		{
			msg.append(getScr("ClanApply","spec"));
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
