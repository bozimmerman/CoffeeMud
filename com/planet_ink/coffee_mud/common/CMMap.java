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
	protected static Hashtable startRooms=new Hashtable();
	protected static Hashtable deathRooms=new Hashtable();

	private static void theWorldChanged()
	{
		for (Enumeration a=areas(); a.hasMoreElements();)
			((Area)a.nextElement()).clearMap();
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
		for(Enumeration a=areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(A.Name().equalsIgnoreCase(calledThis))
				return A;
		}
		return null;
	}
	public static Enumeration areas(){
		return areasList.elements();
	}
	public static Area getFirstArea()
	{
		if (areas().hasMoreElements())
			return (Area) areas().nextElement();
		return null;
	}
	public static Area getRandomArea()
	{
		Area A=null;
		while((numAreas()>0)&&(A==null))
		{
			try{
				A=(Area)areasList.elementAt(Dice.roll(1,numAreas(),-1));
			}catch(Exception e){}
		}
		return A;
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
	
	public static String getExtendedRoomID(Room R)
	{
		if(R.roomID().length()>0) return R.roomID();
		Area A=R.getArea();
		if(A==null) return "";
		for(Enumeration e=A.getMap();e.hasMoreElements();)
		{
			Room anyR=(Room)e.nextElement();
			if((anyR instanceof GridLocale)
			&&(((GridLocale)anyR).isMyChild(R)))
				return ((GridLocale)anyR).getChildCode(R);
		}
		return R.roomID();
	}
	
	public static Room getRoom(String calledThis)
	{
		Room R = null;
		if(calledThis==null) return null;
		if(calledThis.endsWith(")"))
		{
			int child=calledThis.lastIndexOf("#(");
			if(child>1)
			{
				R=getRoom(calledThis.substring(0,child));
				if((R!=null)&&(R instanceof GridLocale))
					R=((GridLocale)R).getChild(calledThis);
				else
					R=null;
			}
		}
		if(R!=null) return R;
		for (Enumeration i=rooms(); i.hasMoreElements();)
		{
			R = (Room)i.nextElement();
			if (R.roomID().equalsIgnoreCase(calledThis))
				return R;
		}
		return null;
	}
	public static Enumeration rooms() {
		return roomsList.elements();
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
		if (rooms().hasMoreElements())
			return (Room) rooms().nextElement();
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
		for (Enumeration i=deities(); i.hasMoreElements();)
		{
			D = (Deity)i.nextElement();
			if (D.Name().equalsIgnoreCase(calledThis))
				return D;
		}
		return null;
	}
	public static Enumeration deities() { return deitiesList.elements(); }

	public static int numPlayers() { return playersList.size(); }
	public static void addPlayer(MOB newOne) { playersList.add(newOne); }
	public static void delPlayer(MOB oneToDel) { playersList.remove(oneToDel); }
	public static MOB getPlayer(String calledThis)
	{
		MOB M = null;

		for (Enumeration p=players(); p.hasMoreElements();)
		{
			M = (MOB)p.nextElement();
			if (M.Name().equalsIgnoreCase(calledThis))
				return M;
		}
		return null;
	}
	public static Enumeration players() { return playersList.elements(); }

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
		for(Enumeration r=CMClass.races();r.hasMoreElements();)
		{
			Race R=(Race)r.nextElement();
			String cat=R.racialCategory().toUpperCase();
			cat.replace(' ','_');
			String thisOne=page.getProperty(start+"_"+R.racialCategory().toUpperCase());
			if((thisOne!=null)&&(thisOne.length()>0))
				table.put(cat,thisOne);
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
