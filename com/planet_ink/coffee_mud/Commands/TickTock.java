package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class TickTock extends StdCommand
{
	public TickTock(){}

	private String[] access={"TICKTOCK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(null)) return false;
		int h=Util.s_int(Util.combine(commands,1));
		if(h==0) h=1;
		mob.tell("..tick..tock..");
		mob.location().getArea().getTimeObj().tickTock(h);
		mob.location().getArea().getTimeObj().save();
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
