package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.utils.*;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;


public class Archon_GOTO extends ArchonSkill
{
	public Archon_GOTO()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Goto";


		baseEnvStats().setLevel(1);
		addQualifyingClass("Archon",1);
		triggerStrings.addElement("GOTO");
		quality=Ability.OK_SELF;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Archon_GOTO();
	}
	
	private static Room findRoom(String roomID)
	{	
		for(int m=0;m<CMMap.map.size();m++)
		{
			Room thisRoom=(Room)CMMap.map.elementAt(m);
			if(thisRoom.ID().equalsIgnoreCase(roomID))
			   return thisRoom;
		}
		return null;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		Room room=null;
		if((givenTarget!=null)&&(givenTarget instanceof Room))
			room=(Room)givenTarget;
		else
		if(commands.size()<1)
		{
			mob.tell("Go where?  You need the Room ID or a player name!");
			return false;
		}
		
		Room curRoom=mob.location();
		StringBuffer cmd = new StringBuffer(Util.combine(commands,0));

		room = findRoom(cmd.toString());
		if(room==null)
		{
			if((cmd.charAt(0)=='#')&&(curRoom!=null))
			{
				cmd.insert(0,curRoom.getAreaID());
				room = findRoom(cmd.toString());
			}
			else
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=(Session)Sessions.elementAt(s);
					if((thisSession.mob()!=null) && (!thisSession.killFlag()) 
					&&(thisSession.mob().location()!=null)
					&&(thisSession.mob().name().equalsIgnoreCase(cmd.toString())))
					{
						room = thisSession.mob().location();
						break;
					}
				}
				if(room==null)
					for(int s=0;s<Sessions.size();s++)
					{
						Session thisSession=(Session)Sessions.elementAt(s);
						if((thisSession.mob()!=null)&&(!thisSession.killFlag()) 
						&&(thisSession.mob().location()!=null)
						&&(CoffeeUtensils.containsString(thisSession.mob().name(),cmd.toString())))
						{
							room = thisSession.mob().location();
							break;
						}
					}
			}
		}
		if(room==null)
		{
			mob.tell("Go where?  You need the Room ID or a player name!");
			return false;
		}
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