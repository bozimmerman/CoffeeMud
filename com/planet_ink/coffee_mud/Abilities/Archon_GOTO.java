package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;


public class Archon_GOTO extends ArchonSkill
{
	public Archon_GOTO()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Goto";

		triggerStrings.addElement("GOTO");

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Archon_GOTO();
	}
	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Go where?  You need the Room ID!");
			return false;
		}
		Room room=null;
		for(int m=0;m<MUD.map.size();m++)
		{
			Room thisRoom=(Room)MUD.map.elementAt(m);
			if(thisRoom.ID().equalsIgnoreCase(CommandProcessor.combine(commands,0)))
			{
			   room=thisRoom;
			   break;
			}
		}
		if(room==null)
		{
			mob.tell("Go where?  You need the Room ID!");
			return false;
		}
		Room curRoom=mob.location();
		if(curRoom==room)
		{
			mob.tell("Done.");
			return true;
		}
		else
		{
			room.bringMobHere(mob,true);
			mob.tell("Done.");
			return true;
		}
	}
}