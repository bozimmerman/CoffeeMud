package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Gain extends StdCommand
{
	public Gain(){}
	
	private String[] access={"GAIN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()==1)
		{
			mob.tell(getScr("AbilityEvoker","gainerr"));
			return false;
		}
		commands.insertElementAt(getScr("CommandSet","say"),0);
		mob.doCommand(commands);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
