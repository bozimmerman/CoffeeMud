package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
public class CMMap
{
	protected static Vector AREAS=new Vector();
	protected static Vector map=new Vector();
	
	public static Hashtable MOBs=new Hashtable();
	public static Vector Deities=new Vector();

	private static Hashtable startRooms=new Hashtable();
	private static Hashtable deathRooms=new Hashtable();


	public static int numRooms(){return map.size();}
	public static void addRoom(Room newOne){map.addElement(newOne);theWorldChanged();}
	public static void setRoomAt(Room newOne, int place)
	{
		try
		{
			Room olderOne=(Room)map.elementAt(place);
			if(olderOne instanceof GridLocale)
				((GridLocale)olderOne).clearGrid();
			map.setElementAt(newOne,place);
			theWorldChanged();
		}
		catch(Exception e)
		{
		}
	}
	public static void delRoom(Room oneToDel){
		try
		{
			if(oneToDel instanceof GridLocale)
				((GridLocale)oneToDel).clearGrid();
			map.removeElement(oneToDel);
			theWorldChanged();
		}
		catch(Exception e)
		{
		}
	}
	public static Room getRoom(int x){try{return (Room)map.elementAt(x);}catch(Exception e){};return null;}
	public static Vector getRoomVector(){return map;}
	
	public static int numDeities(){return Deities.size();}
	public static void addDeity(Deity newOne){if(!Deities.contains(newOne))Deities.addElement(newOne);}
	public static void delDeity(Deity oneToDel){try{Deities.removeElement(oneToDel);}catch(Exception e){}}
	public static Deity getDeity(int x){try{return (Deity)Deities.elementAt(x);}catch(Exception e){};return null;}
	public static int getDeityIndex(String named)
	{
		Deity bob=getDeity(named);
		if(bob==null) return -1;
		return Deities.indexOf(bob);
	}
	public static Deity getDeity(String named, int index)
	{
		Deity bob=getDeity(index);
		if((bob!=null)&&(bob.name().equals(named)))
			return bob;
		return null;
	}
	public static Deity getDeity(String named)
	{
		Deity bob=(Deity)CoffeeUtensils.fetchEnvironmental(Deities,named,true);
		if(bob==null) bob=(Deity)CoffeeUtensils.fetchEnvironmental(Deities,named,false);
		return bob;
	}
	
	public static int numAreas(){return AREAS.size();}
	public static void addArea(Area newOne){AREAS.addElement(newOne);}
	public static void delArea(Area oneToDel){try{AREAS.removeElement(oneToDel);}catch(Exception e){}}
	public static Area getArea(int x){try{return (Area)AREAS.elementAt(x);}catch(Exception e){};return null;}
	public static Vector getAreaVector(){return AREAS;}
	
	public static Room getStartRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String align=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase();
		String roomID=(String)startRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get(align);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get("ALL");
		
		Room room=null;
		if((roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=getRoom("START");
		if((room==null)&&(map.size()>0))
			room=(Room)map.firstElement();
		return room;
	}
	
	public static Room getDeathRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String align=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase();
		String roomID=(String)deathRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)deathRooms.get(align);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)deathRooms.get("ALL");
		
		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")));
			room=getStartRoom(mob);
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=getStartRoom(mob);
		if((room==null)&&(map.size()>0))
			room=(Room)map.firstElement();
		return room;
	}
	
	private static void pageRooms(INI page, Hashtable table, String start)
	{
		for(int r=0;r<CMClass.races.size();r++)
		{
			Race R=(Race)CMClass.races.elementAt(r);
			String cat=R.racialCategory().toUpperCase();
			cat.replace(' ','_');
			String thisOne=page.getProperty(start+"_"+R.racialCategory().toUpperCase());
			if((thisOne!=null)&&(thisOne.length()>0))
				table.put(R.racialCategory(),thisOne);
		}
		String thisOne=page.getProperty(start+"_GOOD");
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("GOOD",thisOne);
		thisOne=page.getProperty(start+"_NEUTRAL");
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("NEUTRAL",thisOne);
		thisOne=page.getProperty(start+"_EVIL");
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("EVIL",thisOne);
		
		thisOne=page.getProperty(start);
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("ALL",thisOne);
		else
		{
			thisOne=page.getProperty(start+"_ALL");
			if((thisOne!=null)&&(thisOne.length()>0))
				table.put("ALL",thisOne);
		}
	}
	
	public static void initStartRooms(INI page)
	{
		startRooms=new Hashtable();
		pageRooms(page,startRooms,"START");
	}

	public static void initDeathRooms(INI page)
	{
		deathRooms=new Hashtable();
		pageRooms(page,deathRooms,"DEATH");
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
		Deities=new Vector();
		startRooms=new Hashtable();
		deathRooms=new Hashtable();
	}
	
	private static void theWorldChanged()
	{
		for(int a=0;a<AREAS.size();a++)
		{
			Area A=(Area)AREAS.elementAt(a);
			A.clearMap();
		}
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
