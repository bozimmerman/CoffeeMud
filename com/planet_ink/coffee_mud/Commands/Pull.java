package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Pull extends StdCommand
{
	public Pull(){}

	private String[] access={"PULL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatToOpen=Util.combine(commands,1);
		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen,Item.WORN_REQ_ANY);

		if((openThis==null)||(!Sense.canBeSeenBy(openThis,mob)))
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return false;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,CMMsg.MSG_PULL,"<S-NAME> pull(s) <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}