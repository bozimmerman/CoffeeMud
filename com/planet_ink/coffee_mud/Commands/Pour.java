package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Pour extends StdCommand
{
	public Pour(){}
	
	private String[] access={"POUR"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Pour what, into what?");
			return false;
		}
		commands.removeElementAt(0);
		Environmental fillFromThis=null;
		String thingToFillFrom=(String)commands.elementAt(0);
		fillFromThis=mob.fetchCarried(null,thingToFillFrom);
		if((fillFromThis==null)||((fillFromThis!=null)&&(!Sense.canBeSeenBy(fillFromThis,mob))))
		{
			mob.tell("You don't seem to have '"+thingToFillFrom+"'.");
			return false;
		}
		commands.removeElementAt(0);

		if((commands.size()>1)&&(((String)commands.firstElement())).equalsIgnoreCase("into"))
			commands.removeElementAt(0);

		if(commands.size()<1)
		{
			mob.tell("Into what should I pour the "+thingToFillFrom+"?");
			return false;
		}

		String thingToFill=Util.combine(commands,0);
		Environmental fillThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFill,Item.WORN_REQ_ANY);
		Item out=null;
		if((fillThis==null)&&(thingToFill.equalsIgnoreCase("out")))
		{
			out=CMClass.getItem("StdDrink");
			((Drink)out).setLiquidHeld(999999);
			((Drink)out).setLiquidRemaining(0);
			out.setDisplayText("");
			out.setName("out");
			mob.location().addItemRefuse(out,Item.REFUSE_RESOURCE);
			fillThis=out;
		}
		if((fillThis==null)
		||(!Sense.canBeSeenBy(fillThis,mob)))
			mob.tell("I don't see '"+thingToFill+"' here.");
		else
		{
			FullMsg fillMsg=new FullMsg(mob,fillThis,fillFromThis,CMMsg.MSG_FILL,(out==null)?"<S-NAME> pour(s) <O-NAME> into <T-NAME>.":"<S-NAME> pour(s) <O-NAME> <T-NAME>.");
			if(mob.location().okMessage(mob,fillMsg))
				mob.location().send(mob,fillMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
