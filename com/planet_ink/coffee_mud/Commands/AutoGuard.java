package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AutoGuard extends StdCommand
{
	public AutoGuard(){}

	private String[] access={"AUTOGUARD","GUARD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((!Util.bset(mob.getBitmap(),MOB.ATT_AUTOGUARD))
		   ||((commands.size()>0)&&(((String)commands.firstElement()).toUpperCase().startsWith("G"))))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOGUARD));
			mob.tell("You are now on guard. You will no longer follow group leaders.");
			if(mob.isMonster())
				CommonMsgs.say(mob,null,"I am now on guard.",false,false);
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOGUARD));
			mob.tell("You are no longer on guard.  You will now follow group leaders.");
			if(mob.isMonster())
				CommonMsgs.say(mob,null,"I will now follow my group leader.",false,false);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
