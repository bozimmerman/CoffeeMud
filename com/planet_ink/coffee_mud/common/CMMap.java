package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
public class CMMap
{
	protected static Vector areasList = new Vector();
	protected static Vector roomsList = new Vector();
	protected static Vector playersList = new Vector();
	protected static Vector deitiesList = new Vector();
	private static Hashtable startRooms=new Hashtable();
	private static Hashtable deathRooms=new Hashtable();

	private static void theWorldChanged()
	{
		for (Iterator a=areas(); a.hasNext();)
			((Area)a.next()).clearMap();
	}
	// areas
	public static int numAreas() { return areasList.size(); }
	public static void addArea(Area newOne) 
	{ 
		areasList.addElement(newOne);
	}
	public static void delArea(Area oneToDel) 
	{ 
		areasList.remove(oneToDel); 
	}
	public static Area getArea(String calledThis) 
	{ 
		for(Iterator a=areas();a.hasNext();)
		{
			Area A=(Area)a.next();
			if(A.name().equalsIgnoreCase(calledThis))
				return A;
		}
		return null;
	}
	public static Iterator areas(){ 
		return ((Vector)areasList.clone()).iterator();
	}
	public static Area getFirstArea()
	{
		if (areas().hasNext()) 
			return (Area) areas().next();
		return null;
	}
	
	public static int numRooms() { return roomsList.size(); }
	public static void addRoom(Room newOne)
	{
		roomsList.addElement(newOne);
		theWorldChanged();
	}
	public static void delRoom(Room oneToDel)
	{
		if(oneToDel instanceof GridLocale)
			((GridLocale)oneToDel).clearGrid();
		roomsList.remove(oneToDel);
		theWorldChanged();
	}
	public static Room getRoom(String calledThis)
	{
		Room R = null;

		for (Iterator i=rooms(); i.hasNext();)
		{
			R = (Room)i.next();
			if (R.ID().equalsIgnoreCase(calledThis)) break;
		}

		return R;
	}
	public static Iterator rooms() { 
		return ((Vector)roomsList.clone()).iterator();
	}
	public static Vector makeRoomVector()
	{
		Vector V=new Vector();
		for(Iterator r=rooms();r.hasNext();)
			V.addElement((Room)r.next());
		return V;
	}
	public static void replaceRoom(Room newOne, Room oldOne)
	{
		if(oldOne instanceof GridLocale)
		  ((GridLocale)oldOne).clearGrid();
		roomsList.remove(oldOne);
		roomsList.addElement(newOne);
		theWorldChanged();
	}
	public static Room getFirstRoom()
	{
		if (rooms().hasNext()) 
			return (Room) rooms().next();
		return null;
	}
	public static Room getRandomRoom()
	{
		Room R=null;
		while((numRooms()>0)&&(R==null))
		{
			try{
				R=(Room)roomsList.elementAt(Dice.roll(1,numRooms(),-1));
			}catch(Exception e){}
		}
		return R;
	}
	
	public static int numDeities() { return deitiesList.size(); }
	public static void addDeity(Deity newOne) 
	{ 
		if (!deitiesList.contains(newOne)) 
			deitiesList.add(newOne); 
	}
	public static void delDeity(Deity oneToDel) 
	{ 
		deitiesList.remove(oneToDel); 
	}
	public static Deity getDeity(String calledThis)
	{
		Deity D = null;

		for (Iterator i=deities(); i.hasNext();)
		{
			D = (Deity)i.next();
			if (D.name().equalsIgnoreCase(calledThis)) break;
		}

		return D;
	}
	public static Iterator deities() { return deitiesList.iterator(); }
	
	public static int numPlayers() { return playersList.size(); }
	public static void addPlayer(MOB newOne) { playersList.add(newOne); }
	public static void delPlayer(MOB oneToDel) { playersList.remove(oneToDel); }
	public static MOB getPlayer(String calledThis)
	{
		MOB M = null;

		for (Iterator p=players(); p.hasNext();)
		{
			M = (MOB)p.next();
			if (M.name().equalsIgnoreCase(calledThis)) break;
		}

		return M;
	}
	public static Iterator players() { return ((Vector)playersList.clone()).iterator(); }
	
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
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
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
			room=mob.getStartRoom();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=mob.getStartRoom();
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
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

	public static void unLoad()
	{
		areasList.clear();
		roomsList.clear();
		deitiesList.clear();
		playersList.clear();
		startRooms=new Hashtable();
		deathRooms=new Hashtable();
	}
}
