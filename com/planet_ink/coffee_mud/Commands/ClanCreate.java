package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanCreate extends BaseClanner
{
	public ClanCreate(){}

	private String[] access={"CLANCREATE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			if(!mob.isMonster())
			{
				int cost=CommonStrings.getIntVar(CommonStrings.SYSTEMI_CLANCOST);
				if(cost>0)
				{
					if(MoneyUtils.totalMoney(mob)<cost)
					{
						mob.tell("It costs "+cost+" gold to create a clan.  You don't have it.");
						return false;
					}
					MoneyUtils.subtractMoney(null,mob,cost);
				}
				try
				{
					String check=mob.session().prompt("Are you sure you want to found a new clan (y/N)?","N");
					if(check.equalsIgnoreCase("Y"))
					{
						String doubleCheck=mob.session().prompt("Enter the name of your new clan exactly how you want it:","");
						if(doubleCheck.length()<1)
							return false;
						Clan C=Clans.getClan(doubleCheck);
						if((CMClass.DBEngine().DBUserSearch(null,doubleCheck))
						||(doubleCheck.equalsIgnoreCase("All")))
							msg.append("That name is not available for clans.");
						else
						if(C!=null)
							msg.append("Clan "+C.ID()+" exists already. Type 'CLANLIST' and I'll show you what clans are available.  You may 'CLANAPPLY' to join them.");
						else
						{
							if(mob.session().confirm("Is '"+doubleCheck+"' correct (y/N)?", "N"))
							{
								int govtType=-1;
								while(govtType==-1)
								{
									String govt=mob.session().prompt(
									"Now enter a political style for this clan. Choices are:\n\r"
									+"CLAN - Ruled by a boss who assigns underlings.\n\r"
									+"GUILD - Ruled by a numerous bosses who assign underlings.\n\r"
									+"UNION - Ruled by an elected set of leaders and staff.\n\r"
									+"FELLOWSHIP - All decisions and staff are set through the vote.\n\r"
									+": ","");
									if(govt.length()==0){ mob.tell("Aborted."); return false;}
									for(int i=0;i<Clan.GVT_DESCS.length;i++)
										if(govt.equalsIgnoreCase(Clan.GVT_DESCS[i]))
											govtType=i;
								}
								Clan newClan=Clans.getClanType(Clan.TYPE_CLAN);
								newClan.setName(doubleCheck);
								newClan.setGovernment(govtType);
								newClan.setStatus(Clan.CLANSTATUS_PENDING);
								newClan.create();
								CMClass.DBEngine().DBUpdateClanMembership(mob.Name(),newClan.getName(),newClan.getTopRank());
								addClanHomeSpell(mob);
								clanAnnounce(mob, "Your new "+newClan.typeName()+" is online and can now accept applicants.");
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
