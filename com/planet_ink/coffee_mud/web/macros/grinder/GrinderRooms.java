package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class GrinderRooms
{
	public static String editRoom(Room R)
	{
		return "";
	}
	public static String delRoom(Room R)
	{
		for(int r=0;r<CMMap.numRooms();r++)
		{
			Room R2=CMMap.getRoom(r);
			if(R2!=R)
			for(int d=0;d<R2.rawDoors().length;d++)
			{
				if(R2.rawDoors()[d]==R)
				{
					R2.rawDoors()[d]=null;
					R2.rawExits()[d]=null;
					ExternalPlay.DBUpdateExits(R2);
				}
			}
		}
		CMMap.delRoom(R);
		ExternalPlay.DBDeleteRoom(R);
		return "";
	}
	public static String createRoom(Room R, int dir)
	{
		Room newRoom=CMClass.getLocale("StdRoom");
		if(R.rawDoors()[dir]!=null)
			return "Room already there!";
		R.rawDoors()[dir]=newRoom;
		if(R.rawExits()[dir]==null)
			R.rawExits()[dir]=CMClass.getExit("StdOpenDoorway");
		newRoom.setID(ExternalPlay.getOpenRoomID(R.getArea().name()));
		newRoom.setDisplayText("Title of "+newRoom.ID());
		newRoom.setDescription("Description of "+newRoom.ID());
		newRoom.rawDoors()[Directions.getOpDirectionCode(dir)]=R;
		newRoom.rawExits()[Directions.getOpDirectionCode(dir)]=CMClass.getExit("StdOpenDoorway");
		newRoom.setArea(R.getArea());
		ExternalPlay.DBCreateRoom(newRoom,"StdRoom");
		CMMap.addRoom(newRoom);
		ExternalPlay.DBUpdateExits(R);
		return "";
	}
}