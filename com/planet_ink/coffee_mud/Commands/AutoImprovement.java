package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AutoImprovement extends StdCommand
{
	public AutoImprovement(){}

	private String[] access={"AUTOIMPROVEMENT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOIMPROVE))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOIMPROVE));
			mob.tell("Skill improvement notifications are now off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOIMPROVE));
			mob.tell("Skill improvement notifications are now on.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
