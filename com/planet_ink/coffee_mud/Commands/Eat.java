package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Eat extends StdCommand
{
	public Eat(){}
	
	private String[] access={"EAT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Eat what?");
			return false;
		}
		commands.removeElementAt(0);

		Environmental thisThang=null;
		thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0),Item.WORN_REQ_ANY);
		if((thisThang==null)
		||((thisThang!=null)
		   &&(!mob.isMine(thisThang))
		   &&(!Sense.canBeSeenBy(thisThang,mob))))
		{
			mob.tell("You don't see '"+Util.combine(commands,0)+"' here.");
			return false;
		}
		FullMsg newMsg=new FullMsg(mob,thisThang,null,CMMsg.MSG_EAT,"<S-NAME> eat(s) <T-NAMESELF>."+CommonStrings.msp("gulp.wav",10));
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
