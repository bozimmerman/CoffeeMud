package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ver extends StdCommand
{
	public Ver(){}

	private String[] access={"VERSION","VER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		//*TODO FIx Ver
		//mob.tell(myHost.getVer());
		mob.tell("(C) 2000-2004 Bo Zimmerman");
		mob.tell("bo@zimmers.net");
		mob.tell("http://www.zimmers.net/home/mud.html");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
