package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
import com.planet_ink.coffee_mud.common.*;

public class StdCommand implements Command
{
	Vector commandWords=new Vector();
	
	public StdCommand()
	{
		// add command words here
	}
	
	public Vector getAccessWords()
	{
		return commandWords;
	}
	public boolean execute(MOB mob, Vector commands)
	{
		// return true to continue with existing command processor after execution.
		// return false to cancel any further command processing (recommended for your commands)
		return true;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
