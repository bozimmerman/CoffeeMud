package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
public class CMMap
{
	public static Vector AREAS=new Vector();
	public static Vector map=new Vector();
	public static Hashtable MOBs=new Hashtable();

	private static Room startRoom=null;

	public static Room startRoom(){return startRoom;}
	public static void setStartRoom(Room newRoom)
	{ startRoom=newRoom;}

	public static void setStartRoom(String preferred)
	{
		startRoom=getRoom(preferred);
		if(startRoom==null)
			startRoom=getRoom("START");
		if(startRoom==null)
			startRoom=getRoom("Start");
		if((startRoom==null)&&(map.size()>0))
			startRoom=(Room)map.elementAt(0);
	}

	public static Room getRoom(String calledThis)
	{
		for(int i=0;i<map.size();i++)
		{
			Room R=(Room)map.elementAt(i);
			if(R.ID().equalsIgnoreCase(calledThis))
				return R;
		}
		return null;
	}
	
	public static void unLoad()
	{
		map=new Vector();
		AREAS=new Vector();
		MOBs=new Hashtable();
		startRoom=null;
	}
	
	public static Area getArea(String areaName)
	{
		for(int a=0;a<AREAS.size();a++)
		{
			Area A=(Area)AREAS.elementAt(a);
			if(A.name().equalsIgnoreCase(areaName))
				return A;
		}
		return null;
	}
}
