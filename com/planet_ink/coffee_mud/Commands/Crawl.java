package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Crawl extends Go
{
	public Crawl(){}

	private String[] access={"CRAWL","CR"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		int direction=Directions.getGoodDirectionCode(Util.combine(commands,1));
		if(direction>=0)
		{
			FullMsg msg=new FullMsg(mob,null,null,CMMsg.MSG_SIT,null);
			if(Sense.isSitting(mob)||(mob.location().okMessage(mob,msg)))
			{
				if(!Sense.isSitting(mob))
					mob.location().send(mob,msg);
				move(mob,direction,false,false,false);
			}
		}
		else
		{
			mob.tell(getScr("Movement","crawlerr1"));
			return false;
		}
		return false;
	}
	public int ticksToExecute(){return 2;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
