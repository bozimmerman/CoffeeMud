package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class GrinderRooms
{
	public static String editRoom(ExternalHTTPRequests httpReq, Hashtable parms, Room R)
	{
		if(R==null) return "Old Room not defined!";
		boolean redoAllMyDamnRooms=false;
		Room oldR=R;
		
		// class!
		String className=(String)httpReq.getRequestParameters().get("CLASS");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this room.";
		
		ExternalPlay.resetRoom(R);
		
		if(!className.equalsIgnoreCase(CMClass.className(R)))
		{
			R=CMClass.getLocale(className);
			if(R==null)
				return "The class you chose does not exist.  Choose another.";
			CMMap.delRoom(oldR);
			CMMap.addRoom(R);
			R.setArea(oldR.getArea());
			R.setID(oldR.ID());
			for(int d=0;d<R.rawDoors().length;d++)
				R.rawDoors()[d]=oldR.rawDoors()[d];
			for(int d=0;d<R.rawExits().length;d++)
				R.rawExits()[d]=oldR.rawExits()[d];
			for(int i=0;i<oldR.numInhabitants();i++)
				R.addInhabitant(oldR.fetchInhabitant(i));
			for(int i=0;i<oldR.numItems();i++)
				R.addItem(oldR.fetchItem(i));
			redoAllMyDamnRooms=true;
		}
		
		// name
		String name=(String)httpReq.getRequestParameters().get("NAME");
		if((name==null)||(name.length()==0))
			return "Please enter a name for this room.";
		R.setDisplayText(name);
		
		
		// description
		String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
		if(desc==null)desc="";
		R.setDescription(desc);
		
		String err=GrinderAreas.doAffectsNBehavs(R,httpReq,parms);
		if(err.length()>0) return err;
		
		if(redoAllMyDamnRooms)
		{
			for(int r=0;r<CMMap.numRooms();r++)
			{
				Room R2=CMMap.getRoom(r);
				for(int d=0;d<R2.rawDoors().length;d++)
					if(R2.rawDoors()[d]==oldR)
					{
						R2.rawDoors()[d]=R;
						if(R2 instanceof GridLocale)
							((GridLocale)R2).buildGrid();
					}
			}
			R.getArea().clearMap();
		}
		ExternalPlay.DBUpdateRoom(R);
		return "";
	}
	public static String delRoom(Room R)
	{
		for(int r=0;r<CMMap.numRooms();r++)
		{
			Room R2=CMMap.getRoom(r);
			for(int d=0;d<R2.rawDoors().length;d++)
			{
				if(R2.rawDoors()[d]==R)
				{
					R2.rawDoors()[d]=null;
					R2.rawExits()[d]=null;
					if(R2 instanceof GridLocale)
						((GridLocale)R2).buildGrid();
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
		if(R instanceof GridLocale)
			((GridLocale)R).buildGrid();
		return "";
	}
}