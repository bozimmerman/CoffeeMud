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
import com.planet_ink.coffee_mud.commands.sysop.Rooms;
import java.io.*;
import java.util.*;


public class Archon_ResetRoom extends ArchonSkill
{
	public Archon_ResetRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Reset Room";

		triggerStrings.addElement("RESETROOM");

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Archon_ResetRoom();
	}
	
	public boolean invoke(MOB mob, Vector commands)
	{

		if(mob.isMonster()) return false;
		Room room=mob.location();
		Rooms.clearTheRoom(mob.location());
		RoomLoader.DBReadContent(mob.location());
		mob.tell("Done.");
		return true;
	}
}