package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanList extends BaseClanner
{
	public ClanList(){}

	private String[] access={"CLANLIST","CLANS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer head=new StringBuffer("");
		head.append("^x[");
		head.append(Util.padRight("Clan Name",24)+" | ");
		head.append(Util.padRight("Type",13)+" | ");
		head.append(Util.padRight("Status",8)+" | ");
		head.append(Util.padRight("Clan Members",12));
		head.append("]^.^? \n\r");
		StringBuffer msg=new StringBuffer("");
		for(Enumeration e=Clans.clans();e.hasMoreElements();)
		{
			Clan thisClan=(Clans)e.nextElement();
			msg.append(" ");
			msg.append(Util.padRight(thisClan.ID(),24)+"   ");
			msg.append(Util.padRight(thisClan.typeName(),13)+"   ");
			boolean war=false;
			for(Enumeration e2=Clans.clans();e2.hasMoreElements();)
			{
				Clan C=(Clan)e2.nextElement();
				if((C!=thisClan)
				&&((thisClan.getClanRelations(C.ID())==Clan.REL_WAR)
					||(C.getClanRelations(thisClan.ID())==Clan.REL_WAR)))
				{ war=true; break;}
			}
			String status=(war)?"At War":"Active";
			switch(thisClan.getStatus())
			{
			case Clan.CLANSTATUS_FADING:
				status="Inactive";
				break;
			case Clan.CLANSTATUS_PENDING:
				status="Pending";
				break;
			}
			msg.append(Util.padRight(status,8)+"   ");
			msg.append(Util.padRight(new Integer(thisClan.getSize()).toString(),12));
			msg.append("\n\r");
		}
		mob.tell(head.toString()+msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
