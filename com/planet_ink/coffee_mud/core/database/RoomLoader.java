package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector.DBPreparedBatchEntry;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.CMCatalog.CataDataImpl;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
/* 
   Copyright 2000-2013 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class RoomLoader
{
	protected DBConnector DB=null;
	public RoomLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	private int recordCount=1;
	private int currentRecordPos=1;
	private int updateBreak=1;
	private final static String zeroes="000000000000";
	
	protected static class StuffClass
	{
		public Hashtable<String,Hashtable<String,PhysicalAgent>> itemNums=new Hashtable<String,Hashtable<String,PhysicalAgent>>();
		public Hashtable<String,Hashtable<String,String>> cataData=new Hashtable<String,Hashtable<String,String>>();
		public Hashtable<String,Hashtable<Item,String>> itemLocs=new Hashtable<String,Hashtable<Item,String>>();
		public Hashtable<String,Hashtable<MOB, String>> mobRides=new Hashtable<String,Hashtable<MOB, String>>();
	}

	public Area DBReadArea(Area A)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMAREA WHERE CMAREA='"+A.Name()+"'");
			while(R.next())
			{
				String areaName=DBConnections.getRes(R,"CMAREA");
				String areaType=DBConnections.getRes(R,"CMTYPE");
				if(A==null) A=CMClass.getAreaType(areaType);
				if(A==null) A=CMClass.getAreaType("StdArea");
				if(A==null)
				{
					Log.errOut("Could not read area: "+areaName);
					return null;
				}
				A.setName(areaName);
				A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
				A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
				A.setDescription(DBConnections.getRes(R,"CMDESC"));
				A.setMiscText(DBConnections.getRes(R,"CMROTX"));
				A.setTheme((int)DBConnections.getLongRes(R,"CMTECH"));
				return A;
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}
	
	protected void DBReadAllAreas()
	{
		DBConnection D=null;
		while(CMLib.map().numAreas()>0)
			CMLib.map().delArea(CMLib.map().getFirstArea());
		try
		{
			D=DB.DBFetch();
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Areas");
			ResultSet R=D.query("SELECT * FROM CMAREA");
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			LinkedList<Pair<Area,String>> areasLoaded=new LinkedList<Pair<Area,String>>();
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String areaName=DBConnections.getRes(R,"CMAREA");
				String areaType=DBConnections.getRes(R,"CMTYPE");
				Area A=CMClass.getAreaType(areaType);
				if(A==null) A=CMClass.getAreaType("StdArea");
				if(A==null)
				{
					Log.errOut("Could not read area: "+areaName);
					continue;
				}
				A.setName(areaName);
				A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
				A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
				A.setDescription(DBConnections.getRes(R,"CMDESC"));
				String miscData=DBConnections.getRes(R,"CMROTX");
				A.setTheme((int)DBConnections.getLongRes(R,"CMTECH"));
				A.setAreaState(Area.State.ACTIVE);
				if((currentRecordPos%updateBreak)==0)
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");
				
				CMLib.map().addArea(A);
				areasLoaded.add(new Pair<Area,String>(A,miscData));
			}
			for(Pair<Area,String> a : areasLoaded)
			{
				a.first.setMiscText(a.second);
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		catch(Exception sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}
	
	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus)
	{
		RoomnumberSet roomSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Fetching roomnums for "+areaName);
			ResultSet R=D.query("SELECT * FROM CMROOM"+((areaName==null)?"":" WHERE CMAREA='"+areaName+"'"));
			while(R.next())
				roomSet.add(DBConnections.getRes(R,"CMROID"));
		}
		catch(SQLException sqle)
		{
			Log.errOut("RoomSet",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
		return roomSet;
	}
	
	public Map<String, Room> DBReadRoomData(String singleRoomIDtoLoad, boolean reportStatus)
	{ 
		return DBReadRoomData(singleRoomIDtoLoad,null,reportStatus,null,null);
	}
	
	public Room[] DBReadRoomObjects(String areaName, boolean reportStatus)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Rooms");
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMAREA='"+areaName+"'");
			List<Room> rooms=buildRoomObjects(D,R,reportStatus);
			return rooms.toArray(new Room[0]);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return new Room[0];
	}
	
	private void buildExistingRoomObject(ResultSet R, Room newRoom) throws SQLException
	{
		newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
		if(CMProps.getBoolVar(CMProps.Bool.ROOMDNOCACHE))
			newRoom.setDescription("");
		else
			newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
		newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
	}

	private List<Room> buildRoomObjects(DBConnection D, ResultSet R, boolean reportStatus) throws SQLException
	{
		recordCount=DB.getRecordCount(D,R);
		updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
		String roomID=null;
		List<Room> rooms=new Vector<Room>();
		while(R.next())
		{
			currentRecordPos=R.getRow();
			roomID=DBConnections.getRes(R,"CMROID");
			String localeID=DBConnections.getRes(R,"CMLOID");
			//String areaName=DBConnections.getRes(R,"CMAREA");
			Room newRoom=CMClass.getLocale(localeID);
			if(newRoom==null)
				Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
			else
			{
				newRoom.setRoomID(roomID);
				buildExistingRoomObject(R,newRoom);
				rooms.add(newRoom);
			}
			if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
		}
		return rooms;
	}
	
	public boolean DBReReadRoomObject(Room room)
	{
		if((room==null)||(room.roomID()==null))
			return false;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
			if(R.next())
				buildExistingRoomObject(R, room);
			else
				return false;
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			return false;
		}
		finally
		{
			DB.DBDone(D);
		}
		return true;
	}
	
	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Rooms");
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomIDtoLoad+"'");
			List<Room> rooms=buildRoomObjects(D,R,reportStatus);
			if(rooms.size()>0)
				return rooms.get(0);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}
	
	public Map<String,Room> DBReadRoomData(String singleRoomIDtoLoad,
										   RoomnumberSet roomsToLoad,
										   boolean reportStatus, 
										   List<String> unknownAreas, 
										   RoomnumberSet unloadedRooms)
	{
		STreeMap<String, Room> roomSet = new STreeMap<String, Room>(new Comparator<String>()
		{
			public int compare(String o1, String o2) {
				if(o1==o2) return 0;
				if(o1==null) return -1;
				if(o2==null) return 1;
				return o1.compareTo(o2);
			}
		});
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Rooms");
			ResultSet R=D.query("SELECT * FROM CMROOM"+((singleRoomIDtoLoad==null)?"":" WHERE CMROID='"+singleRoomIDtoLoad+"'"));
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			String roomID=null;
			while(R.next())
			{
				currentRecordPos=R.getRow();
				roomID=DBConnections.getRes(R,"CMROID");
				if((roomsToLoad!=null)&&(!roomsToLoad.contains(roomID)))
					continue;
				String localeID=DBConnections.getRes(R,"CMLOID");
				String areaName=DBConnections.getRes(R,"CMAREA");
				Area myArea=CMLib.map().getArea(areaName);
				if(myArea==null)
				{
					myArea=(Area)CMClass.getAreaType("StdArea").copyOf();
					myArea.setName(areaName);
					if((unknownAreas!=null)
					&&(!unknownAreas.contains(areaName)))
						unknownAreas.add(areaName);
				}
				myArea.addProperRoomnumber(roomID);
				if(CMath.bset(myArea.flags(),Area.FLAG_THIN))
				{
					if(unloadedRooms!=null)
					{
						if(!unloadedRooms.contains(roomID))
							unloadedRooms.add(roomID);
						continue;
					}
				}
				Room newRoom=CMClass.getLocale(localeID);
				if(newRoom==null)
					Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
				else
				{
					newRoom.setRoomID(roomID);
					newRoom.setArea(myArea);
					CMLib.map().registerWorldObjectLoaded(myArea, newRoom, newRoom);
					newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
					if(CMProps.getBoolVar(CMProps.Bool.ROOMDNOCACHE))
						newRoom.setDescription("");
					else
						newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
					newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
					roomSet.put(roomID,newRoom);
				}
				if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
		return roomSet;
	}

	public void DBReadRoomExits(String roomID, Room room, boolean reportStatus)
	{
		Map<String,Room> map=new TreeMap<String,Room>();
		map.put(roomID, room);
		DBReadRoomExits(roomID,map,reportStatus,null);
	}
	
	public void DBReadRoomExits(String roomID, Map<String, Room> allRooms, boolean reportStatus, RoomnumberSet unloadedRooms)
	{
		DBConnection D=null;
		// now grab the exits
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Exits");
			ResultSet R=D.query("SELECT * FROM CMROEX"+((roomID==null)?"":" WHERE CMROID='"+roomID+"'"));
			Room thisRoom=null;
			Room newRoom=null;
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				roomID=DBConnections.getRes(R,"CMROID");
				int direction=(int)DBConnections.getLongRes(R,"CMDIRE");
				thisRoom=allRooms.get(roomID);
				if(thisRoom==null)
				{
					if((unloadedRooms!=null)&&(!unloadedRooms.contains(roomID)))
						Log.errOut("Room","Couldn't set "+direction+" exit for unknown room '"+roomID+"'");
				}
				else
				{
					String exitID=DBConnections.getRes(R,"CMEXID");
					String exitMiscText=DBConnections.getResQuietly(R,"CMEXTX");
					String nextRoomID=DBConnections.getRes(R,"CMNRID");
					newRoom=allRooms.get(nextRoomID);
					Exit newExit=CMClass.getExit(exitID);
					if(nextRoomID.length()==0)
					{ /* this is likely a room link (rebuilt by import at a later time) */}
					else
					if(newRoom==null)
					{
						int x=nextRoomID.indexOf('#');
						Area otherA=null;
						if((unloadedRooms!=null)&&(unloadedRooms.contains(nextRoomID)))
							otherA=thisRoom.getArea();
						else
						if(x>0)
						{
							otherA=CMLib.map().getArea(nextRoomID.substring(0,x));
							if((otherA!=null)&&(otherA!=thisRoom.getArea()))
								newRoom=otherA.getRoom(nextRoomID);
						}
						if(newRoom!=null)
						{ /* its all worked out now */}
						else
						if(otherA==null)
							Log.errOut("RoomLoader","Unknown area for unlinked room #"+nextRoomID+" in "+roomID);
						else
						if(((unloadedRooms!=null)&&(unloadedRooms.contains(nextRoomID)))
						||(CMath.bset(otherA.flags(),Area.FLAG_THIN)))
						{
							newRoom=CMClass.getLocale("ThinRoom");
							newRoom.setRoomID(nextRoomID);
							newRoom.setArea(otherA);
						}
						else
						if(!nextRoomID.startsWith("#"))
							Log.errOut("RoomLoader","Unknown unlinked room #"+nextRoomID);
						else
						if(newExit!=null)
							newExit.setTemporaryDoorLink(nextRoomID);
					}
							
					if((newExit==null)&&(newRoom==null))
						Log.errOut("Room",roomID+":no room&exit to '"+nextRoomID+"', exit type '"+exitID+"', direction: "+direction);
					else
					if((direction>255)&&(!(thisRoom instanceof GridLocale)))
						Log.errOut("Room","Not GridLocale, tried "+direction+" exit for room '"+roomID+"'");
					else
					if((direction>255)&&(newRoom!=null))
					{
						Vector<String> CEs=CMParms.parseSemicolons(exitMiscText.trim(),true);
						for(int ces=0;ces<CEs.size();ces++)
						{
							Vector<String> SCE=CMParms.parse(CEs.elementAt(ces).trim());
							WorldMap.CrossExit CE=new WorldMap.CrossExit();
							if(SCE.size()<3) continue;
							CE.x=CMath.s_int(SCE.elementAt(0));
							CE.y=CMath.s_int(SCE.elementAt(1));
							int codeddir=CMath.s_int(SCE.elementAt(2));
							if(SCE.size()>=4)
								CE.destRoomID=newRoom.roomID()+SCE.elementAt(3);
							else
								CE.destRoomID=newRoom.roomID();
							CE.out=(codeddir&256)==256;
							CE.dir=codeddir&255;
							((GridLocale)thisRoom).addOuterExit(CE);
							if((!CE.out)&&(!(newRoom instanceof GridLocale)))
							{
								newRoom.rawDoors()[CE.dir]=thisRoom;
								newRoom.setRawExit(CE.dir,CMClass.getExit("Open"));
							}
						}
					}
					else
					{
						if(newExit!=null)
						{
							newExit.setTemporaryDoorLink("{{#"+roomID+"#}}");
							newExit.setMiscText(exitMiscText);
						}
						if(direction>=Directions.NUM_DIRECTIONS())
							Log.errOut("RoomLoader",CMLib.map().getExtendedRoomID(thisRoom)+" has an invalid direction #"+direction);
						else
						{
							thisRoom.rawDoors()[direction]=newRoom;
							thisRoom.setRawExit(direction,newExit);
							CMLib.map().registerWorldObjectLoaded(thisRoom.getArea(), newRoom, newExit);
						}
					}
				}
				if(reportStatus&&((currentRecordPos%updateBreak)==0))
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Exits ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}
	
	public void DBReadAllRooms(RoomnumberSet set)
	{
		List<String> newAreasToCreate=new Vector<String>();
		if(set==null)
			DBReadAllAreas();
		if(CMLib.map().numAreas()==0)
			return;

		RoomnumberSet unloadedRooms=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		Map<String,Room> rooms=DBReadRoomData(null,set,set==null,newAreasToCreate,unloadedRooms);
		
		// handle stray areas
		for(String areaName : newAreasToCreate)
		{
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			Area A=CMClass.getAreaType("StdArea");
			A.setName(areaName);
			DBCreate(A);
			CMLib.map().addArea(A);
			for(Map.Entry<String,Room> entry : rooms.entrySet())
			{
				Room R=entry.getValue();
				if(R.getArea().Name().equals(areaName))
					R.setArea(A);
			}
		}
		
		DBReadRoomExits(null,rooms,set==null,unloadedRooms);

		DBReadContent(null,null,rooms,unloadedRooms,set==null,true);

		if(set==null)
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Finalizing room data)");

		for(Map.Entry<String,Room> entry : rooms.entrySet())
		{
			Room thisRoom=entry.getValue();
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
		}

		if(set==null)
			for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				a.nextElement().getAreaStats();
	}
	
	public String DBReadRoomDesc(String roomID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomID+"'");
			if(R.next())
			{
				String txt=DBConnections.getRes(R,"CMDESC2");
				return txt;
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}
	
	public String DBReadRoomMOBData(String roomID, String mobID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+roomID+"'");
			while(R.next())
			{
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				if(NUMID.equalsIgnoreCase(mobID))
				{
					String txt=DBConnections.getRes(R,"CMCHTX");
					return txt;
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	private void fixItemKeys(Hashtable<Item,String> itemLocs, Hashtable<String, PhysicalAgent> itemNums)
	{
		for(Enumeration<Item> e=itemLocs.keys();e.hasMoreElements();)
		{
			Item keyItem=e.nextElement();
			String location=itemLocs.get(keyItem);
			Environmental container=itemNums.get(location);
			if((container instanceof Container)&&(((Container)container).capacity()>0))
				keyItem.setContainer((Container)container);
			else
			if(container instanceof Rideable)
				keyItem.setRiding((Rideable)container);
			else
			if(container instanceof Container)
				keyItem.setContainer((Container)container);
		}
	}

	private void fixMOBRides(Map<MOB, String> mobRides, Map<String,PhysicalAgent> itemNums)
	{
		for(MOB M : mobRides.keySet())
		{
			String ride=mobRides.get(M);
			if(ride!=null)
			{
				PhysicalAgent P=itemNums.get(ride);
				if(P!=null)
				{
					if(P instanceof Rideable)
						M.setRiding((Rideable)P);
					else
					if(P instanceof MOB)
						M.setFollowing((MOB)P);
				}
			}
		}
	}

	private void fixContentContainers(Hashtable<String,PhysicalAgent> content, StuffClass stuff, String roomID, Room room, boolean debug, boolean makeLive)
	{
		String lastName=null;
		Hashtable<Item,String> itemLocs=null;
		Hashtable<MOB, String> mobRides=null;
		if(room != null)
			for(Enumeration<PhysicalAgent> i=content.elements();i.hasMoreElements();)
			{
				PhysicalAgent P=i.nextElement();
				if((debug)&&((lastName==null)||(!lastName.equals(P.Name()))))
				{lastName=P.Name(); Log.debugOut("RoomLoader","Loading object(s): "+P.Name());}
				if(P instanceof Item)
				{
					room.addItem((Item)P);
					if(makeLive)
						CMLib.map().registerWorldObjectLoaded(room.getArea(), room, P);
				}
				else
				{
					((MOB)P).setStartRoom(room);
					if(makeLive)
						((MOB)P).bringToLife(room,true);
					else
					{
						room.addInhabitant((MOB)P);
						((MOB)P).recoverPhyStats();
						((MOB)P).recoverMaxState();
						((MOB)P).recoverCharStats();
						((MOB)P).resetToMaxState();
					}
				}
			}
		itemLocs=stuff.itemLocs.get("LOCSFOR"+roomID.toUpperCase());
		mobRides=stuff.mobRides.get("RIDESFOR"+roomID.toUpperCase());
		if(itemLocs!=null)
		{
			fixItemKeys(itemLocs,content);
			if(room!=null)
			{
				room.recoverRoomStats();
				room.recoverRoomStats();
			}
		}
		if(mobRides!=null)
			fixMOBRides(mobRides,content);
	}
	
	public void DBReadCatalogs()
	{
		DBReadContent("CATALOG_MOBS",null,null,null,true,false);
		DBReadContent("CATALOG_ITEMS",null,null,null,true,false);
	}
	
	public void DBReadContent(String thisRoomID, Room thisRoom, Map<String, Room> rooms, RoomnumberSet unloadedRooms, boolean setStatus, boolean makeLive)
	{
		boolean debug=Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMPOP));
		if(debug||(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS))))
			Log.debugOut("RoomLoader","Reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));
		
		StuffClass stuff=new StuffClass();
		Hashtable<String,PhysicalAgent> itemNums=null;
		Hashtable<String,String> cataData=null;
		Hashtable<Item,String> itemLocs=null;
		Hashtable<MOB, String> mobRides=null;
		
		boolean catalog=((thisRoomID!=null)&&(thisRoomID.startsWith("CATALOG_")));

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			if(setStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Items");
			ResultSet R=D.query("SELECT * FROM CMROIT"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus) recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				if((unloadedRooms!=null)&&(unloadedRooms.contains(roomID)))
					continue;
				if((!catalog)&&(roomID.startsWith("CATALOG_")))
					continue;
				itemNums=stuff.itemNums.get("NUMSFOR"+roomID.toUpperCase());
				if(itemNums==null)
				{
					itemNums=new Hashtable<String,PhysicalAgent>();
					stuff.itemNums.put("NUMSFOR"+roomID.toUpperCase(),itemNums);
				}
				itemLocs=stuff.itemLocs.get("LOCSFOR"+roomID.toUpperCase());
				if(itemLocs==null)
				{
					itemLocs=new Hashtable<Item,String>();
					stuff.itemLocs.put("LOCSFOR"+roomID.toUpperCase(),itemLocs);
				}
				String itemNum=DBConnections.getRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"' for room "+roomID);
				else
				{
					newItem.setDatabaseID(itemNum);
					itemNums.put(itemNum,newItem);
					Room room=(rooms!=null)?rooms.get(roomID):thisRoom;
					newItem.setOwner(room); // temporary measure to make sure item behavior thread group is properly assigned
					String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						PhysicalAgent container=itemNums.get(loc);
						if(container instanceof Container)
							newItem.setContainer((Container)container);
						else
							itemLocs.put(newItem,loc);
					}
					try {
						if(catalog)
						{
							String text=DBConnections.getResQuietly(R,"CMITTX");
							int x=text.lastIndexOf("<CATALOGDATA");
							if((x>0)&&(text.indexOf("</CATALOGDATA>",x)>0))
							{
								cataData=stuff.cataData.get("CATADATAFOR"+roomID.toUpperCase());
								if(cataData==null)
								{
									cataData=new Hashtable<String,String>();
									stuff.cataData.put("CATADATAFOR"+roomID.toUpperCase(),cataData);
								}
								cataData.put(itemNum,text.substring(x));
								text=text.substring(0,x);
							}
							newItem.setMiscText(text);
						}
						else
							newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
						newItem.basePhyStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
						newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
						newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
						newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
						newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
						newItem.recoverPhyStats();
					} catch(Exception e) { Log.errOut("RoomLoader",e); itemNums.remove(itemNum);}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Items ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}

		// now grab the inhabitants
		try
		{
			D=DB.DBFetch();
			if(setStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting MOBS");
			ResultSet R=D.query("SELECT * FROM CMROCH"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus) recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				if((unloadedRooms!=null)&&(unloadedRooms.contains(roomID)))
					continue;
				if((!catalog)&&(roomID.startsWith("CATALOG_")))
					continue;
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				String MOBID=DBConnections.getRes(R,"CMCHID");

				itemNums=stuff.itemNums.get("NUMSFOR"+roomID.toUpperCase());
				if(itemNums==null)
				{
					itemNums=new Hashtable<String,PhysicalAgent>();
					stuff.itemNums.put("NUMSFOR"+roomID.toUpperCase(),itemNums);
				}
				mobRides=stuff.mobRides.get("RIDESFOR"+roomID.toUpperCase());
				if(mobRides==null)
				{
					mobRides=new Hashtable<MOB, String>();
					stuff.mobRides.put("RIDESFOR"+roomID.toUpperCase(),mobRides);
				}

				MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"'");
				else
				{
					Room room=(rooms!=null)?rooms.get(roomID):thisRoom;
					newMOB.setLocation(room); // temporary measure to make sure thread group is properly assigned
					newMOB.setDatabaseID(NUMID);
					itemNums.put(NUMID,newMOB);
					if(thisRoom!=null)
					{
						newMOB.setStartRoom(thisRoom);
						newMOB.setLocation(thisRoom);
					}
					try {
						if((CMProps.getBoolVar(CMProps.Bool.MOBNOCACHE))
						&&(!catalog)
						&&(NUMID.indexOf(MOBID+"@")>=0))
							newMOB.setMiscText("%DBID>"+roomID+NUMID.substring(NUMID.indexOf('@')));
						else
						{
							String text=DBConnections.getResQuietly(R,"CMCHTX");
							if(catalog)
							{
								int x=text.lastIndexOf("<CATALOGDATA");
								if((x>0)&&(text.indexOf("</CATALOGDATA>",x)>0))
								{
									cataData=stuff.cataData.get("CATADATAFOR"+roomID.toUpperCase());
									if(cataData==null)
									{
										cataData=new Hashtable<String,String>();
										stuff.cataData.put("CATADATAFOR"+roomID.toUpperCase(),cataData);
									}
									cataData.put(NUMID,text.substring(x));
									text=text.substring(0,x);
								}
							}
							newMOB.setMiscText(text);
						}
						newMOB.basePhyStats().setLevel(((int)DBConnections.getLongRes(R,"CMCHLV")));
						newMOB.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMCHAB"));
						newMOB.basePhyStats().setRejuv((int)DBConnections.getLongRes(R,"CMCHRE"));
						String ride=DBConnections.getRes(R,"CMCHRI");
						if((ride!=null)&&(ride.length()>0))
							mobRides.put(newMOB,ride);
						newMOB.recoverCharStats();
						newMOB.recoverPhyStats();
						newMOB.recoverMaxState();
						newMOB.resetToMaxState();
					} catch(Exception e) { Log.errOut("RoomLoader",e); itemNums.remove(NUMID);}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading MOBs ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if(thisRoom!=null)
		{
			rooms=new STreeMap<String,Room>();
			rooms.put(thisRoom.roomID(),thisRoom);
		}
		if(rooms!=null) recordCount=rooms.size();
		updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
		currentRecordPos=0;

		itemNums=stuff.itemNums.get("NUMSFORCATALOG_ITEMS");
		cataData=stuff.cataData.get("CATADATAFORCATALOG_ITEMS");
		if((itemNums!=null)&&(thisRoomID!=null)&&(thisRoomID.equals("CATALOG_ITEMS")))
		{
			String itemNum;
			Item I;
			String data;
			fixContentContainers(itemNums,stuff,"CATALOG_ITEMS",null,debug,false);
			for(Enumeration<String> e=itemNums.keys();e.hasMoreElements();)
			{
				itemNum=e.nextElement();
				I=(Item)itemNums.get(itemNum);
				data=(cataData!=null)?cataData.get(itemNum):null;
				Item oldI=CMLib.catalog().getCatalogItem(I.Name());
				if((oldI!=null)&&(I.databaseID().length()>0)&&(!oldI.databaseID().equals(I.databaseID())))
					DBDeleteRoomItem("CATALOG_ITEMS", I);
				else	
				{
					CMLib.catalog().submitToCatalog(I);
					if((data!=null)&&(data.length()>0))
					{
						CatalogLibrary.CataData dataI=CMLib.catalog().getCatalogItemData(I.Name());
						if(dataI!=null)
							dataI.build(data);
					}
				}
			}
		}

		// load mob catalog
		itemNums=stuff.itemNums.get("NUMSFORCATALOG_MOBS");
		cataData=stuff.cataData.get("CATADATAFORCATALOG_MOBS");
		if((itemNums!=null)&&(thisRoomID!=null)&&(thisRoomID.equals("CATALOG_MOBS")))
		{
			String itemNum;
			MOB M;
			String data;
			fixContentContainers(itemNums,stuff,"CATALOG_MOBS",null,debug,false);
			for(Enumeration<String> e=itemNums.keys();e.hasMoreElements();)
			{
				itemNum=e.nextElement();
				M=(MOB)itemNums.get(itemNum);
				data=(cataData!=null)?cataData.get(itemNum):null;
				MOB oldM=CMLib.catalog().getCatalogMob(M.Name());
				if((oldM!=null)&&(M.databaseID().length()>0)&&(!oldM.databaseID().equals(M.databaseID())))
					DBDeleteRoomMOB("CATALOG_MOBS", M);
				else	
				{
					CMLib.catalog().submitToCatalog(M);
					if((data!=null)&&(data.length()>0))
					{
						CatalogLibrary.CataData dataM=CMLib.catalog().getCatalogMobData(M.Name());
						if(dataM!=null)
							dataM.build(data);
					}
				}
			}
		}
		
		// now load the rooms
		if(rooms!=null)
		for(Map.Entry<String,Room> entry : rooms.entrySet())
		{
			if((((++currentRecordPos)%updateBreak)==0)&&(setStatus))
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Populating Rooms ("+(currentRecordPos)+" of "+recordCount+")");
			Room room=entry.getValue();
			if(debug) Log.debugOut("RoomLoader","Populating room: "+room.roomID());
			itemNums=stuff.itemNums.get("NUMSFOR"+room.roomID().toUpperCase());
			if(itemNums!=null)
				fixContentContainers(itemNums,stuff,room.roomID(),room,debug,makeLive);
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));
	}

	
	private List<Item> DBGetContents(Room room)
	{
		if((!room.isSavable())||(room.amDestroyed())) return new Vector<Item>();
		List<Item> contents=new Vector<Item>();
		for(int i=0;i<room.numItems();i++)
		{
			Item thisItem=room.getItem(i);
			if((thisItem!=null)&&(!contents.contains(thisItem))&&thisItem.isSavable())
				contents.add(thisItem);
		}
		return contents;
	}
	
	protected DBPreparedBatchEntry getDBCreateItemString(String roomID, Item thisItem)
	{
		boolean catalog=((roomID!=null)&&(roomID.startsWith("CATALOG_")));
		thisItem.setExpirationDate(0); // saved items won't clear!
		Environmental container=thisItem.container();
		if((container==null)
		&&(thisItem.riding()!=null)
		&&(thisItem.riding().isSavable()))
		{
			Room room=CMLib.map().roomLocation(thisItem);
			if(((room!=null)&&(room.isHere(thisItem.riding())))
			||(CMLib.catalog().isCatalogObj(thisItem.riding())))
				container=thisItem.riding();
		}
		String itemID=""+thisItem;
		thisItem.setDatabaseID(itemID);
		String text=thisItem.text();
		if(catalog)
		{
			CatalogLibrary.CataData dataI=CMLib.catalog().getCatalogItemData(thisItem.Name());
			if(dataI!=null)
				text+=dataI.data();
		}
		return new DBPreparedBatchEntry(
		"INSERT INTO CMROIT ("
		+"CMROID, "
		+"CMITNM, "
		+"CMITID, "
		+"CMITLO, "
		+"CMITTX, "
		+"CMITRE, "
		+"CMITUR, "
		+"CMITLV, "
		+"CMITAB, "
		+"CMHEIT"
		+") values ("
		+"'"+roomID+"',"
		+"'"+itemID+"',"
		+"'"+thisItem.ID()+"',"
		+"'"+((container!=null)?(""+container):"")+"',"
		+"?,"
		+thisItem.basePhyStats().rejuv()+","
		+thisItem.usesRemaining()+","
		+thisItem.basePhyStats().level()+","
		+thisItem.basePhyStats().ability()+","
		+thisItem.basePhyStats().height()+")",
		text+" ");
	}
	public void DBCreateThisItem(String roomID, Item thisItem)
	{
		DB.updateWithClobs(getDBCreateItemString(roomID,thisItem));
	}
	
	public void DBUpdateTheseItems(Room room, List<Item> items)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Start item update for room "+room.roomID());
		List<DBPreparedBatchEntry> statements=new Vector<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'"));
		for(int i=0;i<items.size();i++)
		{
			Item thisItem=items.get(i);
			CMLib.map().registerWorldObjectLoaded(room.getArea(), room, thisItem);
			statements.add(getDBCreateItemString(room.roomID(),thisItem));
		}
		DB.updateWithClobs(statements);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Finished items update for room "+room.roomID());
	}
	
	public void DBUpdateItems(Room room)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		DBUpdateTheseItems(room,DBGetContents(room));
	}

	public void DBUpdateExits(Room room)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROEX)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Starting exit update for room "+room.roomID());
		List<DBPreparedBatchEntry> statements=new Vector<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMROEX WHERE CMROID='"+room.roomID()+"'"));
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit thisExit=room.getRawExit(d);
			Room thisRoom=room.rawDoors()[d];
			
			if((thisExit!=null)&&(!thisExit.isSavable()))
				thisExit=null;
			if((thisRoom!=null)&&(!thisRoom.isSavable()))
				thisRoom=null;
			if((thisRoom!=null)||(thisExit!=null))
			{
				CMLib.map().registerWorldObjectLoaded(room.getArea(), room, thisExit);
				statements.add(new DBPreparedBatchEntry(
				"INSERT INTO CMROEX ("
				+"CMROID, "
				+"CMDIRE, "
				+"CMEXID, "
				+"CMEXTX, "
				+"CMNRID"
				+") values ("
				+"'"+room.roomID()+"',"
				+d+","
				+"'"+((thisExit==null)?" ":thisExit.ID())+"',"
				+"?,"
				+"'"+((thisRoom==null)?" ":thisRoom.roomID())+"')",
				((thisExit==null)?" ":thisExit.text())));
			}
		}
		if(room instanceof GridLocale)
		{
			HashSet<String> done=new HashSet<String>();
			int ordinal=0;
			for(Iterator<WorldMap.CrossExit> i=((GridLocale)room).outerExits();i.hasNext();)
			{
				WorldMap.CrossExit CE=i.next();
				Room R=CMLib.map().getRoom(CE.destRoomID);
				if(R==null) continue;
				if(R.getGridParent()!=null) R=R.getGridParent();
				if((R.isSavable())&&(!done.contains(R.roomID())))
				{
					done.add(R.roomID());
					HashSet<String> oldStrs=new HashSet<String>();
					for(Iterator<WorldMap.CrossExit> i2=((GridLocale)room).outerExits();i2.hasNext();)
					{
						WorldMap.CrossExit CE2=i2.next();
						if((CE2.destRoomID.equals(R.roomID())
						||(CE2.destRoomID.startsWith(R.roomID()+"#("))))
						{
							String str=CE2.x+" "+CE2.y+" "+((CE2.out?256:512)|CE2.dir)+" "+CE2.destRoomID.substring(R.roomID().length())+";";
							if(!oldStrs.contains(str))
								oldStrs.add(str);
						}
					}
					StringBuffer exitStr=new StringBuffer("");
					for(Iterator<String> a=oldStrs.iterator();a.hasNext();)
						exitStr.append(a.next());
					statements.add(new DBPreparedBatchEntry(
					"INSERT INTO CMROEX ("
					+"CMROID, "
					+"CMDIRE, "
					+"CMEXID, "
					+"CMEXTX, "
					+"CMNRID"
					+") values ("
					+"'"+room.roomID()+"',"
					+(256+(++ordinal))+","
					+"'Open',"
					+"?,"
					+"'"+R.roomID()+"')",
					exitStr.toString()));
				}
			}
		}
		DB.updateWithClobs(statements);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROEX)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Finished exit update for room "+room.roomID());
	}

	public void DBCreateThisMOB(String roomID, MOB thisMOB)
	{
		DB.updateWithClobs(getDBCreateMOBString(roomID,thisMOB));
	}
	
	public DBPreparedBatchEntry getDBCreateMOBString(String roomID, MOB thisMOB)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Creating mob "+thisMOB.name()+" for room "+roomID);
		boolean catalog=((roomID!=null)&&(roomID.startsWith("CATALOG_")));
		
		String ride=null;
		if(thisMOB.riding()!=null)
			ride=""+thisMOB.riding();
		else
		if(thisMOB.amFollowing()!=null)
			ride=""+thisMOB.amFollowing();
		else
			ride="";
		String mobID=""+thisMOB;
		thisMOB.setDatabaseID(mobID);
		
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Created mob "+thisMOB.name()+" for room "+roomID);
		
		if((CMProps.getBoolVar(CMProps.Bool.MOBNOCACHE))&&(!catalog))
		   thisMOB.setMiscText("%DBID>"+roomID+mobID.substring(mobID.indexOf('@')));
		
		String text=thisMOB.text();
		if(catalog)
		{
			CatalogLibrary.CataData dataM=CMLib.catalog().getCatalogMobData(thisMOB.Name());
			if(dataM!=null)
				text+=dataM.data();
		}
		return new DBPreparedBatchEntry(
		"INSERT INTO CMROCH ("
		+"CMROID, "
		+"CMCHNM, "
		+"CMCHID, "
		+"CMCHTX, "
		+"CMCHLV, "
		+"CMCHAB, "
		+"CMCHRE, "
		+"CMCHRI "
		+") values ("
		+"'"+roomID+"',"
		+"'"+mobID+"',"
		+"'"+CMClass.classID(thisMOB)+"',"
		+"?,"
		+thisMOB.basePhyStats().level()+","
		+thisMOB.basePhyStats().ability()+","
		+thisMOB.basePhyStats().rejuv()+","
		+"'"+ride+"'"
		+")",
		text+" ");
	}
	
	public void DBUpdateTheseMOBs(Room room, List<MOB> mobs)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Updating mobs for room "+room.roomID());
		if(mobs==null) mobs=new Vector<MOB>();
		List<DBPreparedBatchEntry> statements=new Vector<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'"));
		for(int m=0;m<mobs.size();m++)
		{
			MOB thisMOB=mobs.get(m);
			CMLib.map().registerWorldObjectLoaded(room.getArea(), room, thisMOB);
			statements.add(getDBCreateMOBString(room.roomID(),thisMOB));
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating mobs for room "+room.roomID());
		DB.updateWithClobs(statements);
	}

	public void DBUpdateMOBs(Room room)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		Vector<MOB> mobs=new Vector<MOB>();
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB thisMOB=room.fetchInhabitant(m);
			if((thisMOB!=null)&&(thisMOB.isSavable()))
				mobs.addElement(thisMOB);
		}
		DBUpdateTheseMOBs(room,mobs);
	}


	public void DBUpdateAll(Room room)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		DBUpdateRoom(room);
		DBUpdateMOBs(room);
		DBUpdateExits(room);
		DBUpdateItems(room);
	}

	public void DBUpdateRoom(Room room)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROOM)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Start updating room "+room.roomID());
		CMLib.map().registerWorldObjectLoaded(room.getArea(), room, room);
		DB.updateWithClobs(
		"UPDATE CMROOM SET "
		+"CMLOID='"+CMClass.classID(room)+"',"
		+"CMAREA='"+room.getArea().Name()+"',"
		+"CMDESC1='"+room.displayText().replace('\'', '`')+" ',"
		+"CMDESC2=?,"
		+"CMROTX=? "
		+"WHERE CMROID='"+room.roomID()+"'",
		new String[]{room.description()+" ",room.text()+" "});
		if(CMProps.getBoolVar(CMProps.Bool.ROOMDNOCACHE))
			room.setDescription("");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROOM)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating room "+room.roomID());
	}


	public void DBReCreate(Room room, String oldID)
	{
		if((!room.isSavable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROOM)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Recreating room "+room.roomID());
		
		DB.update(
		"UPDATE CMROOM SET "
		+"CMROID='"+room.roomID()+"', "
		+"CMAREA='"+room.getArea().Name()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		if(CMProps.getBoolVar(CMProps.Bool.MOBNOCACHE))
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB M=room.fetchInhabitant(m);
				if((M!=null)&&(M.isSavable()))
					M.setMiscText(M.text());
			}
		
		DB.update(
		"UPDATE CMROCH SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");

		DB.update(
		"UPDATE CMROEX SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		DB.update(
		"UPDATE CMROEX SET "
		+"CMNRID='"+room.roomID()+"' "
		+"WHERE CMNRID='"+oldID+"'");
		
		DB.update(
		"UPDATE CMROIT SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		DB.update(
		"UPDATE CMCHAR SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROOM)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done recreating room "+room.roomID());
	}

	public void DBCreate(Area A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Creating area "+A.name());
		if((A==null)||(A.name().length()==0)) {
			Log.errOut("RoomLoader","Unable to create area "+((A!=null)?A.name():"null"));
			return;
		}

		CMLib.map().registerWorldObjectLoaded(A, null, A);
		
		DB.updateWithClobs(
		"INSERT INTO CMAREA ("
		+"CMAREA,"
		+"CMTYPE,"
		+"CMCLIM,"
		+"CMSUBS,"
		+"CMDESC,"
		+"CMROTX,"
		+"CMTECH"
		+") values ("
		+"'"+A.Name()+"',"
		+"'"+A.ID()+"',"
		+""+A.getClimateTypeCode()+","
		+"'"+A.getSubOpList()+"',"
		+"?,"
		+"?,"
		+A.getThemeCode()+")",
		new String[][]{{A.description()+" ",A.text()+" "}});
		A.setAreaState(Area.State.ACTIVE);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done creating area "+A.name());
	}

	public void DBUpdate(String keyName,Area A)
	{
		if((A==null)||(!A.isSavable())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Updating area "+A.name());
		boolean ignoreType=CMSecurity.isDisabled(CMSecurity.DisFlag.FATAREAS)||CMSecurity.isDisabled(CMSecurity.DisFlag.THINAREAS);
		CMLib.map().registerWorldObjectLoaded(A, null, A);
		DB.updateWithClobs(
		"UPDATE CMAREA SET "
		+"CMAREA='"+A.Name()+"',"
		+(ignoreType?"":"CMTYPE='"+A.ID()+"',")
		+"CMCLIM="+A.getClimateTypeCode()+","
		+"CMSUBS='"+A.getSubOpList()+"',"
		+"CMDESC=?,"
		+"CMROTX=?,"
		+"CMTECH="+A.getThemeCode()+" "
		+"WHERE CMAREA='"+keyName+"'",
		new String[]{A.description()+" ",A.text()+" "});
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating area "+A.name());
	}

	public void DBDeleteRoomItem(String roomID, Item item)
	{
		String keyName=item.databaseID();
		if(keyName.length()==0) keyName=""+item;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating item "+item.name()+" in room "+roomID);
		DB.update(
		"DELETE FROM CMROIT "
		+"WHERE CMROID='"+roomID+"' "
		+"AND CMITNM='"+keyName+"'");
	}
	
	public void DBUpdateRoomItem(String roomID, Item item)
	{
		if((roomID==null)||(!item.isSavable())||(item.amDestroyed())) return;
		synchronized(roomID.toUpperCase().intern())
		{
			DBDeleteRoomItem(roomID,item);
			if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
				Log.debugOut("RoomLoader","Continue updating item "+item.name()+" in room "+roomID);
			DBCreateThisItem(roomID,item);
			if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
				Log.debugOut("RoomLoader","Done updating item "+item.name()+" in room "+roomID);
		}
	}
	
	public void DBDeleteRoomMOB(String roomID, MOB mob)
	{
		String keyName=mob.databaseID();
		if(keyName.length()==0) keyName=""+mob;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+roomID);
		DB.update(
		"DELETE FROM CMROCH "
		+"WHERE CMROID='"+roomID+"' "
		+"AND CMCHNM='"+keyName+"'");
	}
	
	public void DBUpdateRoomMOB(String roomID, MOB mob)
	{
		if((roomID==null)||(!mob.isSavable())||(mob.amDestroyed())) return;
		DBDeleteRoomMOB(roomID, mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Continue updating mob "+mob.name()+" in room "+roomID);
		DBCreateThisMOB(roomID,mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+roomID);
	}

	public void DBDelete(Area A)
	{
		if(A==null) return;
		if(!A.isSavable()) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Destroying area "+A.name());
		A.setAreaState(Area.State.STOPPED);
		DB.update("DELETE FROM CMAREA WHERE CMAREA='"+A.Name()+"'");
		DB.update("DELETE FROM CMPDAT WHERE CMPLID='"+A.Name()+"' AND CMSECT='TIMECLOCK'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done destroying area "+A.name()+".");
	}


	public void DBCreate(Room room)
	{
		if(!room.isSavable()) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROOM)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Creating new room "+room.roomID());
		CMLib.map().registerWorldObjectLoaded(room.getArea(), room, room);
		DB.updateWithClobs(
		"INSERT INTO CMROOM ("
		+"CMROID,"
		+"CMLOID,"
		+"CMAREA,"
		+"CMDESC1,"
		+"CMDESC2,"
		+"CMROTX"
		+") values ("
		+"'"+room.roomID()+"',"
		+"'"+CMClass.classID(room)+"',"
		+"'"+room.getArea().Name()+"',"
		+"'"+room.displayText()+" ',"
		+"?,"
		+"?)",
		new String[][]{{room.description()+" ",room.text()+" "}});
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROOM)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done creating new room "+room.roomID());
	}

	public void DBDelete(Room room)
	{
		if(!room.isSavable()) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Destroying room "+room.roomID());
		room.destroy();
		DB.update("DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'");
		DB.update("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'");
		DB.update("DELETE FROM CMROEX WHERE CMROID='"+room.roomID()+"'");
		DB.update("DELETE FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
		room.destroy();
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done gestroying room "+room.roomID());
	}
}
