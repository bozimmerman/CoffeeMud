package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanTax extends BaseClanner
{
	public ClanTax(){}

	private String[] access={"CLANTAX"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt("clantax",0);

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
			if((!skipChecks)&&(!goForward(mob,C,commands,Clan.FUNC_CLANTAX,false)))
			{
				msg.append("You aren't in the right position to set the experience tax rate for your "+C.typeName()+".");
			}
			else
			{
				try
				{
					double newRate=0.0;
					if((skipChecks)&&(commands.size()>1))
						newRate=Util.div(Util.s_int(Util.combine(commands,1)),100);
					else
					if(mob.session()!=null)
					{
						String t=null;
						if((commands.size()<=1)||(!Util.isNumber(Util.combine(commands,1))))
							t=mob.session().prompt("Enter your "+C.typeName()+"'s new tax rate (0-100)\n\r: ","");
						else
							t=Util.combine(commands,1);
						if(t.length()==0) return false;
						int intt=Util.s_int(t);
						if((intt<0)||(intt>100)) return false;
						commands.clear();
						commands.addElement("clantax");
						commands.addElement(t);
						newRate=Util.div(Util.s_int(t),100);
					}
					if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANTAX,true))
					{
						C.setTaxes(newRate);
						C.update();
						clanAnnounce(mob,"Your "+C.typeName()+"'s experience tax rate has been changed to "+((int)Math.round(C.getTaxes()*100.0)+"%."));
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
