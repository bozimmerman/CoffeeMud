package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AutoDraw extends StdCommand
{
	public AutoDraw(){}

	private String[] access={"AUTODRAW"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!Util.bset(mob.getBitmap(),MOB.ATT_AUTODRAW))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTODRAW));
			mob.tell("Auto weapon drawing has been turned on.  You will now draw a weapon when one is handy, and sheath one a few seconds after combat.");
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTODRAW));
			mob.tell("Auto weapon drawing has been turned off.  You will no longer draw or sheath your weapon automatically.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
