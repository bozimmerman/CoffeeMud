package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanPremise extends BaseClanner
{
	public ClanPremise(){}

	private String[] access={"CLANPREMISE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt("clanpremise",0);

		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(Clans.getClan(mob.getClanID())==null))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		{
			Clan C=Clans.getClan(mob.getClanID());
			if((!skipChecks)&&(!goForward(mob,C,commands,Clan.FUNC_CLANPREMISE,false)))
			{
				msg.append("You aren't in the right position to set the premise to your "+C.typeName()+".");
			}
			else
			{
				try
				{
					String premise="";
					if((skipChecks)&&(commands.size()>1))
						premise=Util.combine(commands,1);
					else
					if(mob.session()!=null)
						premise=mob.session().prompt("Describe your "+C.typeName()+"'s Premise\n\r: ","");
					if(premise.length()>0)
					{
						commands.addElement(premise);
						if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANPREMISE,true))
						{
							C.setPremise(premise);
							C.update();
							clanAnnounce(mob,"Your clans premise has been changed.");
							return false;
						}
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
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
