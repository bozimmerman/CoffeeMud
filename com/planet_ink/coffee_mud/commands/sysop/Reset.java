package com.planet_ink.coffee_mud.commands.sysop;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
public class Reset
{
	public void resetSomething(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			mob.tell("Reset this Room, or the whole Area?");
			return;
		}
		String s=(String)commands.elementAt(0);
		if(s.equalsIgnoreCase("room"))
			room(mob);
	}
	public void room(MOB mob)
	{
		new Rooms().clearTheRoom(mob.location());
		ExternalPlay.DBReadContent(mob.location());
		mob.tell("Done.");
	}
}
