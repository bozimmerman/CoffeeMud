package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
public class CMMap
{
	protected static Vector AREAS=new Vector();
	protected static Vector map=new Vector();
	
	public static Hashtable MOBs=new Hashtable();

	private static Room startRoom=null;
	

	public static Room startRoom(){return startRoom;}
	public static void setStartRoom(Room newRoom)
	{ startRoom=newRoom;}

	public static int numRooms(){return map.size();}
	public static void addRoom(Room newOne){map.addElement(newOne);}
	public static void setRoomAt(Room oldOne, int place){map.setElementAt(oldOne,place);}
	public static void delRoom(Room oneToDel){try{map.removeElement(oneToDel);}catch(Exception e){}}
	public static Room getRoom(int x){try{return (Room)map.elementAt(x);}catch(Exception e){};return null;}
	public static Vector getRoomVector(){return map;}
	
	
	public static int numAreas(){return AREAS.size();}
	public static void addArea(Area newOne){AREAS.addElement(newOne);}
	public static void delArea(Area oneToDel){try{AREAS.removeElement(oneToDel);}catch(Exception e){}}
	public static Area getArea(int x){try{return (Area)AREAS.elementAt(x);}catch(Exception e){};return null;}
	public static Vector getAreaVector(){return AREAS;}
	
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
