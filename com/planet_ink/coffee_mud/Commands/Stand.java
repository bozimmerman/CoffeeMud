package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Stand extends StdCommand
{
	public Stand(){}

	private String[] access={"STAND","ST"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean ifnecessary=((commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("IFNECESSARY")));

		if((!Sense.isSitting(mob))&&(!Sense.isSleeping(mob)))
		{
			if(!ifnecessary)
				mob.tell(getScr("Movement","standerr1"));
		}
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,CMMsg.MSG_STAND,getScr("Movement","standup"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
