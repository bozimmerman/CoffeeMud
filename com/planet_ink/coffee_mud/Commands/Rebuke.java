package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Rebuke extends StdCommand
{
	public Rebuke(){}

	private String[] access={"REBUKE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Rebuke whom?");
			return false;
		}
		MOB target=mob.location().fetchInhabitant(Util.combine(commands,1));
		if(target==null)
		{
			if(mob.getWorshipCharID().length()>0)
				target=(MOB)CMMap.getDeity(Util.combine(commands,1));
			if((target==null)
			&&(!Util.combine(commands,1).equalsIgnoreCase(mob.getLeigeID()))
			&&(!Util.combine(commands,1).equalsIgnoreCase(mob.getWorshipCharID())))
			{
				mob.tell("There's nobody here called '"+Util.combine(commands,1)+"' and you aren't serving '"+Util.combine(commands,1)+"'.");
				return false;
			}
		}

		FullMsg msg=null;
		if(target!=null)
			msg=new FullMsg(mob,target,null,CMMsg.MSG_REBUKE,"<S-NAME> rebuke(s) "+target.Name()+".");
		else
			msg=new FullMsg(mob,target,null,CMMsg.MSG_REBUKE,"<S-NAME> rebuke(s) "+mob.getLeigeID()+".");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
