package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Quiet extends StdCommand
{
	public Quiet(){}

	private String[] access={"QUIET"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!Util.bset(mob.getBitmap(),MOB.ATT_QUIET))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_QUIET));
			mob.tell("Quiet mode is now on.  You will no longer receive channel messages or tells.");
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_QUIET));
			mob.tell("Quiet mode is now off.  You may now receive channel messages and tells.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
