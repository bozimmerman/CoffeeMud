package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
						if(!C.delClanHomeSpell(mob))
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
