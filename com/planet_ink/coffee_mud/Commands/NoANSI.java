package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class NoANSI extends StdCommand
{
	public NoANSI(){}
	
	private String[] access={"NOANSI","NOCOLOR","NOCOLOUR"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			if(Util.bset(mob.getBitmap(),MOB.ATT_ANSI))
			{
				mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_ANSI));
				mob.tell("ANSI colour disabled.\n\r");
			}
			else
			{
				mob.tell("ANSI is already disabled.\n\r");
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
