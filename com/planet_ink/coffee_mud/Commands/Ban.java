package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ban extends StdCommand
{
	public Ban(){}

	private String[] access={"BAN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(null))
		{
			mob.tell("Only the Archons can do that.");
			return false;
		}
		commands.removeElementAt(0);
		String banMe=Util.combine(commands,0);
		if(banMe.length()==0)
		{
			mob.tell("Ban what?  Enter an IP address or name mask.");
			return false;
		}
		banMe=banMe.toUpperCase().trim();
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String B=(String)banned.elementAt(b);
			if(B.equals(banMe))
			{
				mob.tell("That is already banned.  Do LIST BANNED and check out #"+(b+1)+".");
				return false;
			}
		}
		mob.tell("Logins and IPs matching '"+banMe+"' are now banned.");
		StringBuffer str=Resources.getFileResource("banned.ini",false);
		str.append(banMe+"\n\r");
		Resources.updateResource("banned.ini",str);
		Resources.saveFileResource("banned.ini");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
