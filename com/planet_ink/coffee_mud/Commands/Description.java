package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Description extends StdCommand
{
	public Description(){}
	
	private String[] access={"DESCRIPTION"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Change your description to what?");
			return false;
		}
		String s=Util.combine(commands,1);
		if(s.length()>255)
			mob.tell("Your description exceeds 255 characters in length.  Please re-enter a shorter one.");
		else
		{
			mob.setDescription(s);
			mob.tell("Your description has been changed.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
