package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Weather extends StdCommand
{
	public Weather(){}

	private String[] access={"WEATHER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Room room=mob.location();
		if(room==null) return false;
		if((commands.size()>1)&&((room.domainType()&Room.INDOORS)==0)&&(((String)commands.elementAt(1)).equalsIgnoreCase("WORLD")))
		{
			StringBuffer tellMe=new StringBuffer("");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(Sense.canAccess(mob,A))
					tellMe.append(Util.padRight(A.name(),20)+": "+A.weatherDescription(room)+"\n\r");
			}
			mob.tell(tellMe.toString());
			return false;
		}
		mob.tell(room.getArea().weatherDescription(room));
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
