package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ANSI extends StdCommand
{
	public ANSI(){}

	private String[] access={"ANSI","COLOR","COLOUR"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			if(!Util.bset(mob.getBitmap(),MOB.ATT_ANSI))
			{
				mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_ANSI));
				mob.tell("^!ANSI^N ^Hcolour^N enabled.\n\r");
			}
			else
			{
				mob.tell("^!ANSI^N is ^Halready^N enabled.\n\r");
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
