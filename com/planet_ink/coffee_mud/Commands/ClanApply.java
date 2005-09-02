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
public class ClanApply extends BaseClanner
{
	public ClanApply(){}

	private String[] access={getScr("ClanApply","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.setElementAt("clanapply",0);
		String qual=Util.combine(commands,1).toUpperCase();
		if(mob.isMonster()) return false;
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				Clan C=Clans.findClan(qual);
				if(C!=null)
				{
					if(MUDZapper.zapperCheck(C.getAcceptanceSettings(),mob))
					{
                        if(MUDZapper.zapperCheck("-<"+CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANLEVEL),mob))
                        {
    						CMClass.DBEngine().DBUpdateClanMembership(mob.Name(), C.ID(), Clan.POS_APPLICANT);
    						mob.setClanID(C.ID());
    						mob.setClanRole(Clan.POS_APPLICANT);
    						clanAnnounce(mob,getScr("ClanApply","new")+" "+mob.Name());
    						mob.tell(getScr("ClanApply","membapplied",C.ID()));
                        }
                        else
                        {
                            msg.append(getScr("ClanApply","leastlev",CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANLEVEL)+""));
                        }
					}
					else
					{
						msg.append(getScr("ClanApply","nrq",C.ID()));
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
