package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AutoGold extends StdCommand
{
	public AutoGold(){}

	private String[] access={"AUTOGOLD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOGOLD))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOGOLD));
			mob.tell("Autogold has been turned off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOGOLD));
			mob.tell("Autogold has been turned on.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
