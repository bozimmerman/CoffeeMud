package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bug extends StdCommand
{
	public Bug(){}
	
	private String[] access={"BUG"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(Util.combine(commands,1).length()>0)
		{
			CMClass.DBEngine().DBWriteJournal("SYSTEM_BUGS",mob.Name(),"ALL","BUG",Util.combine(commands,1),-1);
			mob.tell("Thank you for your assistance in debugging CoffeeMud!");
		}
		else
			mob.tell("What's the bug? Be Specific!");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
