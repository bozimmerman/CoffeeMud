package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Sit extends StdCommand
{
	public Sit(){}

	private String[] access={"SIT","REST","R"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(Sense.isSitting(mob))
		{
			mob.tell(getScr("Movement","siterr1"));
			return false;
		}
		if(commands.size()<=1)
		{
			FullMsg msg=new FullMsg(mob,null,null,CMMsg.MSG_SIT,getScr("Movement","sitdown"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=null;
		if(possibleRideable.length()>0)
		{
			E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
			if((E==null)||(!Sense.canBeSeenBy(E,mob)))
			{
				mob.tell(getScr("Movement","youdontsee",possibleRideable));
				return false;
			}
			if(E instanceof MOB)
			{
				Command C=CMClass.getCommand("Mount");
				if(C!=null) return C.execute(mob,commands);
			}
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=getScr("Movement","sitmounton",((Rideable)E).mountString(CMMsg.TYP_SIT,mob));
		else
			mountStr=getScr("Movement","sitson");
		FullMsg msg=new FullMsg(mob,E,null,CMMsg.MSG_SIT,mountStr);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
