package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Serve extends StdCommand
{
	public Serve(){}

	private String[] access={"SERVE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Serve whom?");
			return false;
		}
		commands.removeElementAt(0);
		MOB recipient=mob.location().fetchInhabitant(Util.combine(commands,0));
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell("I don't see "+Util.combine(commands,0)+" here.");
			return false;
		}
		FullMsg msg=new FullMsg(mob,recipient,null,CMMsg.MSG_SERVE,"<S-NAME> swear(s) fealty to <T-NAMESELF>.");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
