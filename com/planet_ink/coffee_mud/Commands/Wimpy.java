package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Wimpy extends StdCommand
{
	public Wimpy(){}

	private String[] access={"WIMPY"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Change your wimp level to what?");
			return false;
		}
		mob.setWimpHitPoint(Util.s_int(Util.combine(commands,1)));
		mob.tell("Your wimp level has been changed to "+mob.getWimpHitPoint()+" hit points.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
