package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Yell extends StdCommand
{
	public Yell(){}

	private String[] access={"YELL","Y"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Vector newCommands=Util.parse(Util.combine(commands,0).toUpperCase());
		Command C=CMClass.getCommand("Say");
		if(C!=null) C.execute(mob,Util.parse(Util.combine(commands,0).toUpperCase()));
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
