package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Friends extends StdCommand
{
	public Friends(){}

	private String[] access={"FRIENDS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		Hashtable h=pstats.getFriends();

		if((commands.size()<2)||(((String)commands.elementAt(1)).equalsIgnoreCase("list")))
		{
			if(h.size()==0)
				mob.tell("You have no friends listed.  Use FRIENDS ADD to add more.");
			else
			{
				StringBuffer str=new StringBuffer("Your listed friends are: ");
				for(Enumeration e=h.elements();e.hasMoreElements();)
					str.append(((String)e.nextElement())+" ");
				mob.tell(str.toString());
			}
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("ADD"))
		{
			String name=Util.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Add whom?");
				return false;
			}
			MOB M=CMClass.getMOB("StdMOB");
			if(name.equalsIgnoreCase("all"))
				M.setName("All");
			else
			if(!CMClass.DBEngine().DBUserSearch(M,name))
			{
				mob.tell("No player by that name was found.");
				return false;
			}
			if(h.get(M.Name())!=null)
			{
				mob.tell("That name is already on your list.");
				return false;
			}
			h.put(M.Name(),M.Name());
			mob.tell("The Player '"+M.Name()+"' has been added to your friends list.");
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("REMOVE"))
		{
			String name=Util.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Remove whom?");
				return false;
			}
			if(h.get(name)==null)
			{
				mob.tell("That name '"+name+"' does not appear on your list.  Watch your casing!");
				return false;
			}
			h.remove(name);
			mob.tell("The Player '"+name+"' has been removed from your friends list.");
		}
		else
		{
			mob.tell("Parameter '"+((String)commands.elementAt(1))+"' is not recognized.  Try LIST, ADD, or REMOVE.");
			return false;
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
