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
public class ClanCreate extends StdCommand
{
	public ClanCreate(){}

	private String[] access={"CLANCREATE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			if(!mob.isMonster())
			{
				int cost=CMProps.getIntVar(CMProps.SYSTEMI_CLANCOST);
				if(cost>0)
				{
					if(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)<((double)cost))
					{
						mob.tell("It costs "+CMLib.beanCounter().nameCurrencyShort(mob,cost)+" to create a clan.  You don't have it.");
						return false;
					}
				}
				try
				{
					String check=mob.session().prompt("Are you sure you want to found a new clan (y/N)?","N");
					if(check.equalsIgnoreCase("Y"))
					{
						String doubleCheck=mob.session().prompt("Enter the name of your new clan (30 chars max), exactly how you want it\n\r:","");
						if(doubleCheck.length()<1)
							return false;
                        if(doubleCheck.length()>30) // Robert checking length
                        {
                            mob.tell("That name is too long, please use a shorter one.");
                            return false;
                        }
						Clan C=CMLib.clans().findClan(doubleCheck);
						if(CMLib.players().playerExists(doubleCheck)
						||(doubleCheck.equalsIgnoreCase("All")))
							msg.append("That name can not be used.");
						else
						if(C!=null)
							msg.append("Clan "+C.clanID()+"  exists already. Type 'CLANLIST' and I'll show you what clans are available.  You may 'CLANAPPLY' to join them.");
						else
						{
							if(mob.session().confirm("Is '"+doubleCheck+"' correct (y/N)?", "N"))
							{
								int govtType=-1;
								while(govtType==-1)
								{
									String govt=mob.session().prompt(
									"Now enter a political style for this clan. Choices are:\n\r"
									+"GANG  - Ruled by a boss who assigns underlings.\n\r"
									+"GUILD - Ruled by a numerous bosses who assign underlings.\n\r"
									+"UNION - Ruled by an elected set of leaders and staff.\n\r"
									+"FELLOWSHIP - All decisions and staff are set through the vote.\n\r"
									+"THEOCRACY - Clerics rule, can conquer through conversion.\n\r"
                                    +"FAMILY - Patron or Matron rules an unlisted group only relations join.\n\r"
									+": ","");
									if(govt.length()==0){ mob.tell("Aborted."); return false;}
									for(int i=0;i<Clan.GVT_DESCS.length;i++)
										if(govt.equalsIgnoreCase(Clan.GVT_DESCS[i]))
											govtType=i;
								}

								if(cost>0)
									CMLib.beanCounter().subtractMoney(mob,cost);

								Clan newClan=CMLib.clans().getClanType(Clan.TYPE_CLAN);
								newClan.setName(doubleCheck);
								newClan.setGovernment(govtType);
								newClan.setStatus(Clan.CLANSTATUS_PENDING);
								newClan.create();
								CMLib.database().DBUpdateClanMembership(mob.Name(),newClan.getName(),newClan.getTopRank(mob));
								newClan.updateClanPrivileges(mob);
								CMLib.clans().clanAnnounce(mob, "The "+newClan.typeName()+" "+newClan.clanID()+" is online and can now accept applicants.");
							}
						}
					}
				}
				catch(java.io.IOException e)
				{
				}
			}
		}
		else
		{
			msg.append("You are already a member of "+mob.getClanID()+". You need to resign from your before you can create one.");
		}
		mob.tell(msg.toString());
		return false;
	}

	public boolean canBeOrdered(){return false;}


}
