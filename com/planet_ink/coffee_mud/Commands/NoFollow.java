package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class NoFollow extends Follow
{
	public NoFollow(){}

	private String[] access={"NOFOLLOW","NOFOL"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()>1)&&(commands.elementAt(1) instanceof String))
		{
			if(((String)commands.elementAt(1)).equalsIgnoreCase("UNFOLLOW"))
			{
				unfollow(mob,((commands.size()>2)&&(commands.elementAt(2) instanceof String)&&(((String)commands.elementAt(2)).equalsIgnoreCase("QUIETLY"))));
				return false;
			}
		}
		if(!Util.bset(mob.getBitmap(),MOB.ATT_NOFOLLOW))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_NOFOLLOW));
			unfollow(mob,false);
			mob.tell("You are no longer accepting followers.");
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_NOFOLLOW));
			mob.tell("You are now accepting followers.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
