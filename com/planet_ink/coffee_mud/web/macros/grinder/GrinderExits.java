package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderExits
{
	public static String editExit(Room R, int dir)
	{
		return "";
	}
	public static String delExit(Room R, int dir)
	{
		R.rawDoors()[dir]=null;
		R.rawExits()[dir]=null;
		ExternalPlay.DBUpdateExits(R);
		if(R instanceof GridLocale)
			((GridLocale)R).buildGrid();
		return "";
	}
	
	public static String linkRooms(Room R, Room R2, int dir, int dir2)
	{
		if(R.rawDoors()[dir]==null) R.rawDoors()[dir]=R2;
		if(R2.rawDoors()[dir2]==null) R2.rawDoors()[dir2]=R;
			
		if(R.rawExits()[dir]==null)
			R.rawExits()[dir]=CMClass.getExit("StdOpenDoorway");
		if(R2.rawExits()[dir2]==null)
			R2.rawExits()[dir2]=CMClass.getExit("StdOpenDoorway");
			
		ExternalPlay.DBUpdateExits(R);
		ExternalPlay.DBUpdateExits(R2);
			
		if(R instanceof GridLocale)
			((GridLocale)R).buildGrid();
		if(R2 instanceof GridLocale)
			((GridLocale)R2).buildGrid();
		return "";
	}
}
