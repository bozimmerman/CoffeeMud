package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AutoMelee extends StdCommand
{
	public AutoMelee(){}

	private String[] access={"AUTOMELEE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!Util.bset(mob.getBitmap(),MOB.ATT_AUTOMELEE))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOMELEE));
			mob.tell("Automelee has been turned off.  You will no longer charge into melee combat from a ranged position.");
			if(mob.isMonster())
				CommonMsgs.say(mob,null,"I will no longer charge into melee.",false,false);
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOMELEE));
			mob.tell("Automelee has been turned back on.  You will now enter melee combat normally.");
			if(mob.isMonster())
				CommonMsgs.say(mob,null,"I will now enter melee combat normally.",false,false);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
