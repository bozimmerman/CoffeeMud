package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Enter extends Go
{
	public Enter(){}
	
	private String[] access={"ENTER","EN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<=1)
		{
			mob.tell(getScr("Movement","entererr1"));
			return false;
		}
		String enterWhat=Util.combine(commands,1).toUpperCase();
		int dir=MUDTracker.findExitDir(mob,mob.location(),enterWhat);
		if(dir<0)
		{
			Environmental getThis=mob.location().fetchFromRoomFavorItems(null,enterWhat,Item.WORN_REQ_UNWORNONLY);
			if((getThis!=null)&&(getThis instanceof Rideable))
			{
				Command C=CMClass.getCommand("Sit");
				if(C!=null) return C.execute(mob,commands);
			}
			mob.tell(getScr("Movement","youdontsee",enterWhat.toLowerCase()));
			return false;
		}
		move(mob,dir,false,false,false);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
