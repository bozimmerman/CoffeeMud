package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class SysMsgs extends StdCommand
{
	public SysMsgs(){}

	private String[] access={"SYSMSGS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to do that.");
			return false;
		}
		if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
		else
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
		mob.tell("Extended messages are now : "+((Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))?"ON":"OFF"));
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
