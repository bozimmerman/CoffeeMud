package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Sounds extends StdCommand
{
	public Sounds(){}

	private String[] access={"SOUNDS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			if(!Util.bset(mob.getBitmap(),MOB.ATT_SOUND))
			{
				mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_SOUND));
				mob.tell("MSP Sound/Music enabled.\n\r");
			}
			else
			{
				mob.tell("MSP Sound/Music is already enabled.\n\r");
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
