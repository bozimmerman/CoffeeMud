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
		name="a map";
		baseEnvStats.setWeight(0);
		displayText="a map is rolled up here.";
		description="";
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

	public Vector makeMapRooms()
	{
		Vector mapAreas=new Vector();
		String newText=getMapArea();
		while(newText.length()>0)
		{
			int y=newText.indexOf(";");
			String areaName="";
			if(y>=0)
			{
				areaName=newText.substring(0,y).trim();
				newText=newText.substring(y+1);
			}
			else
			{
				areaName=newText;
				newText="";
			}
			if(areaName.length()>0)
				mapAreas.addElement(areaName);
		}

		Vector mapRooms=new Vector();
		for(int a=0;a<mapAreas.size();a++)
		{
			String area=(String)mapAreas.elementAt(a);
			Room room=CMMap.getRoom(area);
			if(room!=null)
			{
				MapRoom mr=new MapRoom();
				mr.r=room;
				mapRooms.addElement(mr);
			}
		}
		return mapRooms;
	}
}
