package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class NoPurge extends StdCommand
{
	public NoPurge(){}

	private String[] access={"NOPURGE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		String protectMe=Util.combine(commands,0);
		if(protectMe.length()==0)
		{
			mob.tell("Protect whom?  Enter a player name to protect from autopurge.");
			return false;
		}
		if(!CMClass.DBEngine().DBUserSearch(null,protectMe))
		{
			mob.tell("Protect whom?  '"+protectMe+"' is not a known player.");
			return false;
		}
		Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		for(int b=0;b<protectedOnes.size();b++)
		{
			String B=(String)protectedOnes.elementAt(b);
			if(B.equalsIgnoreCase(protectMe))
			{
				mob.tell("That player already protected.  Do LIST NOPURGE and check out #"+(b+1)+".");
				return false;
			}
		}
		mob.tell("The player '"+protectMe+"' is now protected from autopurge.");
		StringBuffer str=Resources.getFileResource("protectedplayers.ini",false);
		if(protectMe.trim().length()>0) str.append(protectMe+"\n");
		Resources.updateResource("protectedplayers.ini",str);
		Resources.saveFileResource("protectedplayers.ini");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"NOPURGE");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
