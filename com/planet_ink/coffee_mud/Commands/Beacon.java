package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Beacon extends StdCommand
{
	public Beacon(){}

	private String[] access={"BEACON"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			if(mob.getStartRoom()==mob.location())
				mob.tell("This is already your beacon.");
			else
			{
				mob.setStartRoom(mob.location());
				mob.tell("You have modified your beacon.");
			}
		}
		else
		{
			String name=Util.combine(commands,0);
			MOB M=null;
			for(int s=0;s<Sessions.size();s++)
			{
				Session S=Sessions.elementAt(s);
				if((S.mob()!=null)&&(EnglishParser.containsString(S.mob().name(),name)))
				{ M=S.mob(); break;}
			}
			if(M==null)
			{
				mob.tell("No one is online called '"+name+"'!");
				return false;
			}
			if(M.getStartRoom()==M.location())
			{
				mob.tell(M.name()+" is already at their beacon.");
				return false;
			}
			if(!CMSecurity.isAllowed(mob,M.location(),"BEACON"))
			{
				mob.tell("You cannot beacon "+M.name()+" there.");
				return false;
			}
			M.setStartRoom(M.location());
			mob.tell("You have modified "+M.name()+"'s beacon.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"BEACON");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
