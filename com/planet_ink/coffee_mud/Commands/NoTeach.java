package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class NoTeach extends StdCommand
{
	public NoTeach(){}

	private String[] access={"NOTEACH"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_NOTEACH))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_NOTEACH));
			mob.tell("You may now teach, train, or learn.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_NOTEACH));
			mob.tell("You are no longer teaching, training, or learning.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
