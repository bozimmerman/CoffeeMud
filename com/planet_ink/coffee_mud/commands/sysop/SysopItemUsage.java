package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class SysopItemUsage
{
	public void take(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Take what from whom?");
			return;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("From whom should I take the "+(String)commands.elementAt(0));
			return;
		}

		MOB victim=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));

		if((victim==null)||((victim!=null)&&(!Sense.canBeSeenBy(victim,mob))))
		{
			mob.tell("I don't see "+(String)commands.elementAt(commands.size()-1)+" here.");
			return;
		}
		commands.removeElementAt(commands.size()-1);


		commands.insertElementAt("give",0);
		commands.insertElementAt(mob.name(),commands.size());

		new SocialProcessor().give(victim,commands,true);
	}
}
