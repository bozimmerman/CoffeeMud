package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BardMap extends GenMap
{
	public String ID(){	return "BardMap";}
	public BardMap()
	{
		super();
		setName("a map");
		baseEnvStats.setWeight(0);
		setDisplayText("a map is rolled up here.");
		setDescription("");
		baseGoldValue=5;
		baseEnvStats().setLevel(3);
		setMaterial(EnvResource.RESOURCE_PAPER);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new BardMap();
	}
	public void doMapArea()
	{
		myMap=null;
	}

	public StringBuffer[][] getMyMappedRoom()
	{
		if(myMap!=null)	return myMap;
		myMap=finishMapMaking();
		return myMap;
	}

	public Hashtable makeMapRooms()
	{
		String newText=getMapArea();
		Vector mapAreas=Util.parseSemicolons(newText,true);
		Hashtable mapRooms=new Hashtable();
		for(int a=0;a<mapAreas.size();a++)
		{
			String area=(String)mapAreas.elementAt(a);
			Room room=CMMap.getRoom(area);
			if(room!=null)
			{
				MapRoom mr=new MapRoom();
				mr.r=room;
				mapRooms.put(room,mr);
			}
		}
		super.clearTheSkys(mapRooms);
		return mapRooms;
	}
}
