package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class UnLink extends StdCommand
{
	public UnLink(){}

	private String[] access={"UNLINK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to do that.");
			return false;
		}
		commands.setElementAt("DESTROY",0);
		commands.insertElementAt("ROOM",1);
		Command C=CMClass.getCommand("Destroy");
		C.execute(mob,commands);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
