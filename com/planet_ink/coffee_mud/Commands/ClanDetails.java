package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanDetails extends BaseClanner
{
	public ClanDetails(){}

	private String[] access={"CLANDETAILS","CLAN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String qual=Util.combine(commands,1).toUpperCase();
		if(qual.length()==0) qual=mob.getClanID();
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			boolean found=false;
			for(Enumeration e=Clans.clans();e.hasMoreElements();)
			{
				Clan C=(Clans)e.nextElement();
				if(EnglishParser.containsString(C.ID(), qual))
				{
					msg.append(C.getDetail(mob));
					found=true;
				}
			}
			if(!found)
			{
				msg.append("No clan was found by the name of '"+qual+"'.\n\r");
			}
		}
		else
		{
			msg.append("You need to specify which clan you would like details on. Try 'CLANLIST'.\n\r");
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
