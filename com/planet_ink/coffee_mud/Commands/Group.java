package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Group extends BaseWho
{
	public Group(){}
	
	private String[] access={"GROUP","GR"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		mob.tell(mob.name()+"'s group:\n\r");
		Hashtable group=mob.getGroupMembers(new Hashtable());
		StringBuffer msg=new StringBuffer("");
		for(Enumeration e=group.elements();e.hasMoreElements();)
		{
			MOB follower=(MOB)e.nextElement();
			msg.append(showWhoLong(follower));
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
