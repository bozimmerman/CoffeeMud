package com.planet_ink.coffee_mud.Commands.base.sysop;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class Reset
{
	public String getOpenRoomID(String AreaID)
	{
		int highest=0;
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room thisRoom=CMMap.getRoom(m);
			if((thisRoom.getArea().name().equals(AreaID))
			&&(thisRoom.ID().startsWith(AreaID+"#")))
			{
				int newnum=Util.s_int(thisRoom.ID().substring(AreaID.length()+1));
				if(newnum>=highest)
					highest=newnum+1;
			}
		}
		return AreaID+"#"+highest;
	}

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
		{
			room(mob.location());
			mob.tell("Done.");
		}
		else
		if(s.equalsIgnoreCase("area"))
		{
			area(mob.location().getArea());
			mob.tell("Done.");
		}
	}
	public void room(Room room)
	{
		if(room==null) return;
		new Rooms().clearTheRoom(room);
		ExternalPlay.DBReadContent(room);
	}
	public void area(Area area)
	{
		Vector allRooms=area.getMyMap();
		for(int r=0;r<allRooms.size();r++)
		{
			Room room=(Room)allRooms.elementAt(r);
			room(room);
		}
	}
}
