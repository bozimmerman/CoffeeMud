package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Typo extends StdCommand
{
	public Typo(){}

	private String[] access={"TYPO"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((Util.combine(commands,1).length()>0)&&(mob.location()!=null))
		{
			CMClass.DBEngine().DBWriteJournal("SYSTEM_TYPOS",mob.Name(),"ALL","TYPOS","("+mob.location().roomID()+") "+Util.combine(commands,1),-1);
			mob.tell("Thank you for your assistance!");
		}
		else
			mob.tell("What's the typo?");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
