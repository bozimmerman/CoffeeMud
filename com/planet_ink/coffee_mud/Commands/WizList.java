package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WizList extends StdCommand
{
	public WizList(){}

	private String[] access={"WIZLIST"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer head=new StringBuffer("");
		head.append("^x[");
		head.append(Util.padRight("Race",8)+" ");
		head.append(Util.padRight("Lvl",4)+" ");
		head.append(Util.padRight("Last",18)+" ");
		head.append("] Archon Character Name^.^?\n\r");
		mob.tell("^x["+Util.centerPreserve("The Archons of "+CommonStrings.getVar(CommonStrings.SYSTEM_MUDNAME),head.length()-10)+"]^.^?");
		Vector allUsers=CMClass.DBEngine().getUserList();
		for(int u=0;u<allUsers.size();u++)
		{
			Vector U=(Vector)allUsers.elementAt(u);
			if(((String)U.elementAt(1)).equals("Archon"))
			{
				head.append("[");
				head.append(Util.padRight((String)U.elementAt(2),8)+" ");
				head.append(Util.padRight((String)U.elementAt(3),4)+" ");
				head.append(Util.padRight(IQCalendar.d2String(Util.s_long((String)U.elementAt(5))),18)+" ");
				head.append("] "+Util.padRight((String)U.elementAt(0),25));
				head.append("\n\r");
			}
		}
		mob.tell(head.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
