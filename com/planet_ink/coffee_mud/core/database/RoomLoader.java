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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.RoomContent;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;

/*
   Copyright 2001-2020 Bo Zimmerman

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
	protected DBConnector	DB					= null;
	private int				recordCount			= 1;
	private int				currentRecordPos	= 1;
	private int				updateBreak			= 1;

	public RoomLoader(final DBConnector newDB)
	{
		DB=newDB;
	}

	private final static String zeroes="000000000000";

	protected static class StuffClass
	{
		public Hashtable<String,Hashtable<String,PhysicalAgent>> itemNums=new Hashtable<String,Hashtable<String,PhysicalAgent>>();
		public Hashtable<String,Hashtable<String,String>> cataData=new Hashtable<String,Hashtable<String,String>>();
		public Hashtable<String,Hashtable<Item,String>> itemLocs=new Hashtable<String,Hashtable<Item,String>>();
		public Hashtable<String,Hashtable<MOB, String>> mobRides=new Hashtable<String,Hashtable<MOB, String>>();
	}

	protected DBPreparedBatchEntry doBulkInsert(final StringBuilder str, final List<String> clobs, final String sql, final String clob)
	{
		if(str.length()==0)
		{
			str.append(sql);
			clobs.add(clob+" ");
		}
		else
		if(str.length()<512000)
		{
			final int x=sql.indexOf(" values ");
			str.append(", ").append(sql.substring(x+7));
			clobs.add(clob+" ");
		}
		else
		{
			final DBPreparedBatchEntry entry = new DBPreparedBatchEntry(str.toString(),clobs.toArray(new String[0]));
			str.setLength(0);
			return entry;
		}
		return null;
	}

	public String DBIsAreaName(final String name)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMAREA FROM CMAREA WHERE CMAREA LIKE '"+name+"'");
			if(R.next())
			{
				final String areaName=R.getString("CMAREA");
				R.close();
				return areaName;
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public void DBReadArea(final Area A)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMAREA WHERE CMAREA='"+A.Name()+"'");
			while(R.next())
			{
				final String areaName=DBConnections.getRes(R,"CMAREA");
				A.setName(areaName);
				A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
				A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
				A.setDescription(DBConnections.getRes(R,"CMDESC"));
				A.setMiscText(DBConnections.getRes(R,"CMROTX"));
				A.setTheme((int)DBConnections.getLongRes(R,"CMTECH"));
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public boolean DBReadAreaFull(final String areaName)
	{
		if(CMLib.map().getArea(areaName)!=null)
			return false;
		final Area A=DBReadAreaObject(areaName);
		if(A==null)
			return false;
		final RoomnumberSet unloadedRooms=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		for(final Enumeration<String> ids = CMLib.map().roomIDs();ids.hasMoreElements();)
			unloadedRooms.add(ids.nextElement());

		final RoomnumberSet set = CMLib.database().DBReadAreaRoomList(areaName, false);
		CMLib.map().addArea(A);
		CMLib.map().registerWorldObjectLoaded(A, null, A);
		final Map<String,Room> rooms=DBReadRoomData(null,set,false,null,unloadedRooms);

		DBReadRoomExits(null,rooms,false,unloadedRooms);

		DBReadContent(null,null,rooms,unloadedRooms,false,true);

		for(final Map.Entry<String,Room> entry : rooms.entrySet())
		{
			final Room thisRoom=entry.getValue();
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
		}
		return true;
	}

	public Area DBReadAreaObject(String areaName)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMAREA WHERE CMAREA = '" + areaName + "'");
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				areaName=DBConnections.getRes(R,"CMAREA");
				final String areaType=DBConnections.getRes(R,"CMTYPE");
				Area A=CMClass.getAreaType(areaType);
				if(A==null)
					A=CMClass.getAreaType("StdArea");
				if(A==null)
				{
					Log.errOut("Could not read area: "+areaName);
					continue;
				}
				A.setName(areaName);
				A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
				A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
				A.setDescription(DBConnections.getRes(R,"CMDESC"));
				final String miscData=DBConnections.getRes(R,"CMROTX");
				A.setTheme((int)DBConnections.getLongRes(R,"CMTECH"));
				if((currentRecordPos%updateBreak)==0)
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");
				R.close();
				A.setMiscText(miscData);
				A.setAreaState(Area.State.ACTIVE);
				return A;
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		catch(final Exception sqle)
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
			final ResultSet R=D.query("SELECT * FROM CMAREA");
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			final LinkedList<Pair<Area,String>> areasLoaded=new LinkedList<Pair<Area,String>>();
			while(R.next())
			{
				currentRecordPos=R.getRow();
				final String areaName=DBConnections.getRes(R,"CMAREA");
				final String areaType=DBConnections.getRes(R,"CMTYPE");
				Area A=CMClass.getAreaType(areaType);
				if(A==null)
					A=CMClass.getAreaType("StdArea");
				if(A==null)
				{
					Log.errOut("Could not read area: "+areaName);
					continue;
				}
				A.setName(areaName);
				A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
				A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
				A.setDescription(DBConnections.getRes(R,"CMDESC"));
				final String miscData=DBConnections.getRes(R,"CMROTX");
				A.setTheme((int)DBConnections.getLongRes(R,"CMTECH"));
				if((currentRecordPos%updateBreak)==0)
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");

				CMLib.map().addArea(A);
				areasLoaded.add(new Pair<Area,String>(A,miscData));
			}
			for(final Pair<Area,String> a : areasLoaded)
			{
				a.first.setMiscText(a.second);
			}
			for(final Pair<Area,String> a : areasLoaded)
			{
				a.first.setAreaState(Area.State.ACTIVE);
				CMLib.map().registerWorldObjectLoaded(a.first, null, a.first);
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public RoomnumberSet DBReadAreaRoomList(final String areaName, final boolean reportStatus)
	{
		final RoomnumberSet roomSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Fetching roomnums for "+areaName);
			final ResultSet R=D.query("SELECT * FROM CMROOM"+((areaName==null)?"":" WHERE CMAREA='"+areaName+"'"));
			while(R.next())
				roomSet.add(DBConnections.getRes(R,"CMROID"));
		}
		catch(final SQLException sqle)
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

	public Room DBReadRoomData(final String singleRoomIDtoLoad, final boolean reportStatus)
	{
		final Map<String,Room> map = DBReadRoomData(singleRoomIDtoLoad,null,reportStatus,null,null);
		final Iterator<Room> i = map.values().iterator();
		if(i.hasNext())
			return i.next();
		return null;
	}

	public Room[] DBReadRoomObjects(final String areaName, final boolean reportStatus)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Rooms");
			final ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMAREA='"+areaName+"'");
			final List<Room> rooms=buildRoomObjects(D,R,reportStatus);
			return rooms.toArray(new Room[0]);
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return new Room[0];
	}

	private void populateRoomInnerFields(final ResultSet R, final Room newRoom) throws SQLException
	{
		newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
		if(CMProps.getBoolVar(CMProps.Bool.ROOMDNOCACHE))
			newRoom.setDescription("");
		else
			newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
		newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
	}

	private List<Room> buildRoomObjects(final DBConnection D, final ResultSet R, final boolean reportStatus) throws SQLException
	{
		recordCount=DB.getRecordCount(D,R);
		updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
		String roomID=null;
		final List<Room> rooms=new Vector<Room>();
		while(R.next())
		{
			currentRecordPos=R.getRow();
			roomID=DBConnections.getRes(R,"CMROID");
			final String localeID=DBConnections.getRes(R,"CMLOID");
			//String areaName=DBConnections.getRes(R,"CMAREA");
			final Room newRoom=CMClass.getLocale(localeID);
			if(newRoom==null)
				Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
			else
			{
				newRoom.setRoomID(roomID);
				populateRoomInnerFields(R,newRoom);
				rooms.add(newRoom);
			}
			if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
		}
		return rooms;
	}

	public boolean DBReReadRoomData(final Room room)
	{
		if((room==null)||(room.roomID()==null))
			return false;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
			if(R.next())
				populateRoomInnerFields(R, room);
			else
				return false;
		}
		catch(final SQLException sqle)
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

	public Room DBReadRoomObject(final String roomIDtoLoad, final boolean reportStatus)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Rooms");
			final ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomIDtoLoad+"'");
			final List<Room> rooms=buildRoomObjects(D,R,reportStatus);
			if(rooms.size()>0)
				return rooms.get(0);
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public Map<String,Room> DBReadRoomData(final String singleRoomIDtoLoad,
										   final RoomnumberSet roomsToLoad,
										   final boolean reportStatus,
										   final List<String> unknownAreas,
										   final RoomnumberSet unloadedRooms)
	{
		final STreeMap<String, Room> roomSet = new STreeMap<String, Room>(new Comparator<String>()
		{
			@Override
			public int compare(final String o1, final String o2)
			{
				if(o1==o2)
					return 0;
				if(o1==null)
					return -1;
				if(o2==null)
					return 1;
				return o1.compareTo(o2);
			}
		});
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Rooms");
			final ResultSet R=D.query("SELECT * FROM CMROOM"+((singleRoomIDtoLoad==null)?"":" WHERE CMROID='"+singleRoomIDtoLoad+"'"));
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			String roomID=null;
			while(R.next())
			{
				currentRecordPos=R.getRow();
				roomID=DBConnections.getRes(R,"CMROID");
				if((roomsToLoad!=null)&&(!roomsToLoad.contains(roomID)))
					continue;
				final String localeID=DBConnections.getRes(R,"CMLOID");
				final String areaName=DBConnections.getRes(R,"CMAREA");
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
				final Room newRoom=CMClass.getLocale(localeID);
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
		catch(final SQLException sqle)
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

	public void DBReadRoomExits(final String roomID, final Room room, final boolean reportStatus)
	{
		final Map<String,Room> map=new TreeMap<String,Room>();
		map.put(roomID, room);
		DBReadRoomExits(roomID,map,reportStatus,null);
	}

	public void DBReadRoomExits(String roomID, final Map<String, Room> allRooms, final boolean reportStatus, final RoomnumberSet unloadedRooms)
	{
		DBConnection D=null;
		// now grab the exits
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Exits");
			final ResultSet R=D.query("SELECT * FROM CMROEX"+((roomID==null)?"":" WHERE CMROID='"+roomID+"'"));
			Room thisRoom=null;
			Room newRoom=null;
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				roomID=DBConnections.getRes(R,"CMROID");
				final int direction=(int)DBConnections.getLongRes(R,"CMDIRE");
				thisRoom=allRooms.get(roomID);
				if(thisRoom==null)
				{
					if((unloadedRooms!=null)&&(!unloadedRooms.contains(roomID)))
						Log.errOut("Room","Couldn't set "+direction+" exit for unknown room '"+roomID+"'");
				}
				else
				{
					final String exitID=DBConnections.getRes(R,"CMEXID");
					final String exitMiscText=DBConnections.getResQuietly(R,"CMEXTX");
					final String nextRoomID=DBConnections.getRes(R,"CMNRID");
					newRoom=allRooms.get(nextRoomID);
					final Exit newExit=CMClass.getExit(exitID);
					if(nextRoomID.length()==0)
					{
						/* this is likely a room link (rebuilt by import at a later time) */
					}
					else
					if(newRoom==null)
					{
						final int x=nextRoomID.indexOf('#');
						Area otherA=null;
						if((unloadedRooms!=null)&&(unloadedRooms.contains(nextRoomID)))
							otherA=thisRoom.getArea();
						else
						if(x>0)
						{
							otherA=CMLib.map().getArea(nextRoomID.substring(0,x));
							if(otherA!=null)
							{
								if(otherA!=thisRoom.getArea())
									newRoom=otherA.getRoom(nextRoomID);
							}
						}
						else
						{
							for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
							{
								final Area A=a.nextElement();
								if((A!=null)
								&&(A.getProperRoomnumbers().contains(nextRoomID)))
								{
									otherA=A;
									if(!CMath.bset(A.flags(), Area.FLAG_THIN))
										newRoom=otherA.getRoom(nextRoomID);
								}
							}
						}

						if(newRoom!=null)
						{
							/* its all worked out now */
						}
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
							Log.errOut("RoomLoader","Unknown unlinked room #"+nextRoomID+" in "+roomID);
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
						final List<String> CEs=CMParms.parseSemicolons(exitMiscText.trim(),true);
						for(int ces=0;ces<CEs.size();ces++)
						{
							final Vector<String> SCE=CMParms.parse(CEs.get(ces).trim());
							final GridLocale.CrossExit CE=new GridLocale.CrossExit();
							if(SCE.size()<3)
								continue;
							CE.x=CMath.s_int(SCE.elementAt(0));
							CE.y=CMath.s_int(SCE.elementAt(1));
							final int codeddir=CMath.s_int(SCE.elementAt(2));
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
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBReadAllRooms(final RoomnumberSet set)
	{
		final List<String> newAreasToCreate=new Vector<String>();
		if(set==null)
			DBReadAllAreas();
		if(CMLib.map().numAreas()==0)
			return;

		final RoomnumberSet unloadedRooms=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		final Map<String,Room> rooms=DBReadRoomData(null,set,set==null,newAreasToCreate,unloadedRooms);

		// handle stray areas
		for(final String areaName : newAreasToCreate)
		{
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			final Area A=CMClass.getAreaType("StdArea");
			A.setName(areaName);
			DBCreate(A);
			CMLib.map().addArea(A);
			CMLib.map().registerWorldObjectLoaded(A, null, A);
			for(final Map.Entry<String,Room> entry : rooms.entrySet())
			{
				final Room R=entry.getValue();
				if(R.getArea().Name().equals(areaName))
					R.setArea(A);
			}
		}

		DBReadRoomExits(null,rooms,set==null,unloadedRooms);

		DBReadContent(null,null,rooms,unloadedRooms,set==null,true);

		if(set==null)
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Finalizing room data");

		for(final Map.Entry<String,Room> entry : rooms.entrySet())
		{
			final Room thisRoom=entry.getValue();
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
		}

		if(set==null)
		{
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				a.nextElement().getAreaStats();
		}
	}

	public String DBReadRoomDesc(final String roomID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomID+"'");
			if(R.next())
			{
				final String txt=DBConnections.getRes(R,"CMDESC2");
				return txt;
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public String DBReadRoomMOBMiscText(final String roomID, final String mobID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+roomID+"'");
			while(R.next())
			{
				final String NUMID=DBConnections.getRes(R,"CMCHNM");
				if(NUMID.equalsIgnoreCase(mobID))
				{
					final String txt=DBConnections.getRes(R,"CMCHTX");
					return txt;
				}
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public MOB DBReadRoomMOB(final String roomID, final String mobID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+roomID+"' AND CMCHNM='"+mobID+"'");
			if(R.next())
			{
				final String NUMID=DB.getRes(R, "CMCHNM");
				final String MOBID=DB.getRes(R, "CMCHID");
				final MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					return null;
				newMOB.setDatabaseID(NUMID);
				if((CMProps.getBoolVar(CMProps.Bool.MOBNOCACHE))
				&&(NUMID.indexOf(MOBID+"@")>=0))
					newMOB.setMiscText("%DBID>"+roomID+NUMID.substring(NUMID.indexOf('@')));
				else
				{
					final String text=DBConnections.getResQuietly(R,"CMCHTX");
					newMOB.setMiscText(text);
				}
				newMOB.basePhyStats().setLevel(((int)DBConnections.getLongRes(R,"CMCHLV")));
				newMOB.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMCHAB"));
				newMOB.basePhyStats().setRejuv((int)DBConnections.getLongRes(R,"CMCHRE"));
				newMOB.recoverCharStats();
				newMOB.recoverPhyStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				CMLib.threads().deleteAllTicks(newMOB);
				return newMOB;
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public Item DBReadRoomItem(final String roomID, final String itemNum)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+roomID+"' AND CMITNM='"+itemNum+"'");
			if(R.next())
			{
				final String itemID=DBConnections.getRes(R,"CMITID");
				final Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"' for room "+roomID);
				else
				{
					newItem.setDatabaseID(itemNum);
					try
					{
						newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
						newItem.basePhyStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
						newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
						newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
						newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
						newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
						newItem.recoverPhyStats();
						CMLib.threads().deleteAllTicks(newItem);
						return newItem;
					}
					catch (final Exception e)
					{
						Log.errOut("RoomLoader", e);
					}
				}
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	private void fixItemKeys(final Hashtable<Item,String> itemLocs, final Hashtable<String, PhysicalAgent> itemNums)
	{
		for(final Enumeration<Item> e=itemLocs.keys();e.hasMoreElements();)
		{
			final Item keyItem=e.nextElement();
			final String location=itemLocs.get(keyItem);
			final Environmental container=itemNums.get(location);
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

	private void fixMOBRides(final Map<MOB, String> mobRides, final Map<String,PhysicalAgent> itemNums)
	{
		for(final MOB M : mobRides.keySet())
		{
			final String ride=mobRides.get(M);
			if(ride!=null)
			{
				final PhysicalAgent P=itemNums.get(ride);
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

	private void fixContentContainers(final Hashtable<String,PhysicalAgent> content, final StuffClass stuff, final String roomID, final Room room, final boolean debug, final boolean makeLive)
	{
		String lastName=null;
		Hashtable<Item,String> itemLocs=null;
		Hashtable<MOB, String> mobRides=null;
		if(room != null)
		{
			for(final Enumeration<PhysicalAgent> i=content.elements();i.hasMoreElements();)
			{
				final PhysicalAgent P=i.nextElement();
				if((debug)&&((lastName==null)||(!lastName.equals(P.Name()))))
				{
					lastName=P.Name();
					Log.debugOut("RoomLoader","Loading object(s): "+P.Name());
				}
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

	public void DBReadSpace()
	{
		DBReadContent("SPACE",null,null,null,true,false);
	}

	public int[] DBCountRoomMobsItems(final String roomID)
	{
		DBConnection D=null;
		final int[] count = new int[] { 0, 0};
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+roomID+"'");
			count [1] = DB.getRecordCount(D,R);
			R.close();

			R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+roomID+"'");
			count [0] = DB.getRecordCount(D,R);
			R.close();
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return count;
	}

	public Item DBGetSavedRoomItemCopy(final String roomID, final String itemName)
	{
		DBConnection D=null;
		final Item I=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+roomID+"'");
			while(R.next())
			{
				final String itemID=DBConnections.getRes(R,"CMITID");
				final Item newItem=CMClass.getItem(itemID);
				newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
				newItem.basePhyStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
				newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
				newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
				newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
				newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
				newItem.recoverPhyStats();
				if(newItem.Name().equalsIgnoreCase(itemName))
					return newItem;
				newItem.destroy();
			}
			R.close();
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return I;
	}

	public boolean DBIsSavedRoomItemCopy(final String roomID, final String itemName)
	{
		final Item I=this.DBGetSavedRoomItemCopy(roomID, itemName);
		if(I!=null)
		{
			I.destroy();
			return true;
		}
		return false;
	}

	public void DBReadContent(final String thisRoomID, final Room thisRoom, Map<String, Room> rooms, final RoomnumberSet unloadedRooms, final boolean setStatus, final boolean makeLive)
	{
		final boolean debug=Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMPOP));
		if(debug||(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS))))
			Log.debugOut("RoomLoader","Reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));

		final StuffClass stuff=new StuffClass();
		Hashtable<String,PhysicalAgent> itemNums=null;
		Hashtable<String,String> cataData=null;
		Hashtable<Item,String> itemLocs=null;
		Hashtable<MOB, String> mobRides=null;

		final boolean catalog=((thisRoomID!=null)&&(thisRoomID.startsWith("CATALOG_")));
		final boolean space=((thisRoomID!=null)&&(thisRoomID.equals("SPACE")));

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			if(setStatus)
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Counting Items");
			final ResultSet R=D.query("SELECT * FROM CMROIT"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus)
				recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				final String roomID=DBConnections.getRes(R,"CMROID");
				if((unloadedRooms!=null)&&(unloadedRooms.contains(roomID)))
					continue;
				if((!catalog)&&(roomID.startsWith("CATALOG_")))
					continue;
				if((!space)&&(roomID.startsWith("SPACE")))
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
				final String itemNum=DBConnections.getRes(R,"CMITNM");
				final String itemID=DBConnections.getRes(R,"CMITID");
				final Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"' for room "+roomID);
				else
				{
					newItem.setDatabaseID(itemNum);
					itemNums.put(itemNum,newItem);
					final Room room=(rooms!=null)?rooms.get(roomID):thisRoom;
					newItem.setOwner(room); // temporary measure to make sure item behavior thread group is properly assigned
					final String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						final PhysicalAgent container=itemNums.get(loc);
						if(container instanceof Container)
							newItem.setContainer((Container)container);
						else
							itemLocs.put(newItem,loc);
					}
					try
					{
						if(catalog)
						{
							String text=DBConnections.getResQuietly(R,"CMITTX");
							final int x=text.lastIndexOf("<CATALOGDATA");
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
						if(newItem instanceof SpaceObject)
						{
							CMLib.map().addObjectToSpace((SpaceObject)newItem, ((SpaceObject) newItem).coordinates());
						}
						newItem.basePhyStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
						newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
						newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
						newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
						newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
						newItem.recoverPhyStats();
					}
					catch (final Exception e)
					{
						Log.errOut("RoomLoader", e);
						itemNums.remove(itemNum);
					}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Items ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(final SQLException sqle)
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
			final ResultSet R=D.query("SELECT * FROM CMROCH"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus)
				recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				final String roomID=DBConnections.getRes(R,"CMROID");
				if((unloadedRooms!=null)&&(unloadedRooms.contains(roomID)))
					continue;
				if((!catalog)&&(roomID.startsWith("CATALOG_")))
					continue;
				if((!space) && roomID.equals("SPACE"))
					continue;
				final String NUMID=DBConnections.getRes(R,"CMCHNM");
				final String MOBID=DBConnections.getRes(R,"CMCHID");

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

				final MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"' in room " + roomID);
				else
				{
					final Room room=(rooms!=null)?rooms.get(roomID):thisRoom;
					newMOB.setLocation(room); // temporary measure to make sure thread group is properly assigned
					newMOB.setDatabaseID(NUMID);
					itemNums.put(NUMID,newMOB);
					if(thisRoom!=null)
					{
						newMOB.setStartRoom(thisRoom);
						newMOB.setLocation(thisRoom);
					}
					try
					{
						if((CMProps.getBoolVar(CMProps.Bool.MOBNOCACHE))
						&&(!catalog)
						&&(NUMID.indexOf(MOBID+"@")>=0))
							newMOB.setMiscText("%DBID>"+roomID+NUMID.substring(NUMID.indexOf('@')));
						else
						{
							String text=DBConnections.getResQuietly(R,"CMCHTX");
							if(catalog)
							{
								final int x=text.lastIndexOf("<CATALOGDATA");
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
						final String ride=DBConnections.getRes(R,"CMCHRI");
						if((ride!=null)&&(ride.length()>0))
							mobRides.put(newMOB,ride);
						if(newMOB instanceof SpaceObject)
						{
							CMLib.map().addObjectToSpace((SpaceObject)newMOB, ((SpaceObject) newMOB).coordinates());
						}
						newMOB.recoverCharStats();
						newMOB.recoverPhyStats();
						newMOB.recoverMaxState();
						newMOB.resetToMaxState();
					}
					catch (final Exception e)
					{
						Log.errOut("RoomLoader", e);
						itemNums.remove(NUMID);
					}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading MOBs ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(final SQLException sqle)
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
		if(rooms!=null)
			recordCount=rooms.size();
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
			for(final Enumeration<String> e=itemNums.keys();e.hasMoreElements();)
			{
				itemNum=e.nextElement();
				I=(Item)itemNums.get(itemNum);
				data=(cataData!=null)?cataData.get(itemNum):null;
				final Item oldI=CMLib.catalog().getCatalogItem(I.Name());
				if((oldI!=null)
				&&(I.databaseID().length()>0)
				&&(!oldI.databaseID().equals(I.databaseID())))
					DBDeleteRoomItem("CATALOG_ITEMS", I);
				else
				{
					CMLib.catalog().submitToCatalog(I);
					if((data!=null)&&(data.length()>0))
					{
						final CatalogLibrary.CataData dataI=CMLib.catalog().getCatalogItemData(I.Name());
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
			for(final Enumeration<String> e=itemNums.keys();e.hasMoreElements();)
			{
				itemNum=e.nextElement();
				M=(MOB)itemNums.get(itemNum);
				data=(cataData!=null)?cataData.get(itemNum):null;
				final MOB oldM=CMLib.catalog().getCatalogMob(M.Name());
				if((oldM!=null)
				&&(M.databaseID().length()>0)
				&&(!oldM.databaseID().equals(M.databaseID())))
					DBDeleteRoomMOB("CATALOG_MOBS", M);
				else
				{
					CMLib.catalog().submitToCatalog(M);
					if((data!=null)&&(data.length()>0))
					{
						final CatalogLibrary.CataData dataM=CMLib.catalog().getCatalogMobData(M.Name());
						if(dataM!=null)
							dataM.build(data);
					}
				}
			}
		}

		// now load the rooms
		if(rooms!=null)
		{
			CMProps.setBoolAllVar(CMProps.Bool.POPULATIONSTARTED, true);
			for(final Map.Entry<String,Room> entry : rooms.entrySet())
			{
				if((((++currentRecordPos)%updateBreak)==0)&&(setStatus))
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Populating Rooms ("+(currentRecordPos)+" of "+recordCount+")");
				final Room room=entry.getValue();
				if(debug)
					Log.debugOut("RoomLoader","Populating room: "+room.roomID());
				itemNums=stuff.itemNums.get("NUMSFOR"+room.roomID().toUpperCase());
				if(itemNums!=null)
					fixContentContainers(itemNums,stuff,room.roomID(),room,debug,makeLive);
			}
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));
	}

	private List<Item> DBGetContents(final Room room)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return new Vector<Item>();
		final List<Item> contents=new Vector<Item>();
		for(int i=0;i<room.numItems();i++)
		{
			final Item thisItem=room.getItem(i);
			if((thisItem!=null)
			&&(!contents.contains(thisItem))
			&&thisItem.isSavable())
				contents.add(thisItem);
		}
		return contents;
	}

	protected List<String> DBReadAreaRoomIDs(final String areaName)
	{
		final List<String> lst=new ArrayList<String>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMROID FROM CMROOM WHERE CMAREA='"+DB.injectionClean(areaName)+"'");
			while(R.next())
			{
				final String roomID=R.getString("CMROID");
				lst.add(roomID);
			}
			R.close();
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return lst;
	}

	public RoomContent[] DBReadAreaMobs(final String name)
	{
		final List<RoomContent> lst=new ArrayList<RoomContent>();
		final List<String> roomIDs = DBReadAreaRoomIDs(name);
		final GenericBuilder buildLib = CMLib.coffeeMaker();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			for(final String roomID : roomIDs)
			{
				final ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+DB.injectionClean(roomID)+"'");
				while(R.next())
				{
					int buildHash = 0;
					final String classID = DB.getRes(R, "CMCHID");
					final String text = DB.getRes(R, "CMCHTX");
					final String qName = buildLib.getQuickName(classID, text);
					buildHash = classID.hashCode() ^ text.hashCode();
					buildHash ^= (int)DB.getLongRes(R, "CMCHLV");
					buildHash ^= (int)DB.getLongRes(R, "CMCHAB");
					final int finalHash = buildHash;
					final RoomContent C=new RoomContent()
					{
						final String roomId = roomID;
						final String roomKey = DB.getRes(R, "CMCHNM");
						final String classId = classID;
						final String name = qName;
						final int hashCode = finalHash;

						@Override
						public String ID()
						{
							return classId;
						}

						@Override
						public String name()
						{
							return name;
						}

						@Override
						public String roomID()
						{
							return roomId;
						}

						@Override
						public String dbKey()
						{
							return roomKey;
						}

						@Override
						public int contentHash()
						{
							return hashCode;
						}
					};
					lst.add(C);

				}
				R.close();
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return lst.toArray(new RoomContent[lst.size()]);
	}

	public RoomContent[] DBReadAreaItems(final String name)
	{
		final List<RoomContent> lst=new ArrayList<RoomContent>();
		final List<String> roomIDs = DBReadAreaRoomIDs(name);
		final GenericBuilder buildLib = CMLib.coffeeMaker();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			for(final String roomID : roomIDs)
			{
				final ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+DB.injectionClean(roomID)+"'");
				while(R.next())
				{
					int buildHash = 0;
					final String classID = DB.getRes(R, "CMITID");
					final String text = DB.getRes(R, "CMITTX");
					final String qName = buildLib.getQuickName(classID, text);
					buildHash = classID.hashCode() ^ text.hashCode();
					buildHash ^= (int)DB.getLongRes(R, "CMITLV");
					buildHash ^= (int)DB.getLongRes(R, "CMITAB");
					buildHash ^= (int)DB.getLongRes(R, "CMITUR");
					buildHash ^= (int)DB.getLongRes(R, "CMHEIT");
					final int finalHash = buildHash;
					final RoomContent C=new RoomContent()
					{
						final String roomId = roomID;
						final String roomKey = DB.getRes(R, "CMITNM");
						final String classId = classID;
						final String name = qName;
						final int hashCode = finalHash;

						@Override
						public String ID()
						{
							return classId;
						}

						@Override
						public String name()
						{
							return name;
						}

						@Override
						public String roomID()
						{
							return roomId;
						}

						@Override
						public String dbKey()
						{
							return roomKey;
						}

						@Override
						public int contentHash()
						{
							return hashCode;
						}
					};
					lst.add(C);

				}
				R.close();
			}
		}
		catch(final SQLException sqle)
		{
			Log.errOut("Area",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return lst.toArray(new RoomContent[lst.size()]);
	}

	protected String getShortID(final Environmental E)
	{
		final String classID = ""+E;
		final int x=classID.indexOf('@');
		if(x<0)
			return E.ID()+"@"+classID.hashCode()+Math.random();
		else
			return E.ID()+classID.substring(0, x).hashCode()+classID.substring(x);
	}

	protected Pair<String,String> getDBCreateItemString(final String roomID, final Item thisItem)
	{
		final boolean catalog=((roomID!=null)&&(roomID.startsWith("CATALOG_")));
		thisItem.setExpirationDate(0); // saved items won't clear!
		Environmental container=thisItem.container();
		if((container==null)
		&&(thisItem.riding()!=null)
		&&(thisItem.riding().isSavable()))
		{
			final Room room=CMLib.map().roomLocation(thisItem);
			if(((room!=null)&&(room.isHere(thisItem.riding())))
			||(CMLib.catalog().isCatalogObj(thisItem.riding())))
				container=thisItem.riding();
		}
		final String itemID=getShortID(thisItem);
		thisItem.setDatabaseID(itemID);
		String text=thisItem.text();
		if(catalog)
		{
			final CatalogLibrary.CataData dataI=CMLib.catalog().getCatalogItemData(thisItem.Name());
			if(dataI!=null)
				text+=dataI.data(null);
		}
		return new Pair<String,String>(
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
		+"'"+((container!=null)?(""+getShortID(container)):"")+"',"
		+"?,"
		+thisItem.basePhyStats().rejuv()+","
		+thisItem.usesRemaining()+","
		+thisItem.basePhyStats().level()+","
		+thisItem.basePhyStats().ability()+","
		+thisItem.basePhyStats().height()+")",
		text+" ");
	}

	public void DBCreateThisItem(final String roomID, final Item thisItem)
	{
		final Pair<String,String> sql=getDBCreateItemString(roomID,thisItem);
		final DBPreparedBatchEntry entry = new DBPreparedBatchEntry(sql.first,sql.second);
		DB.updateWithClobs(entry);
	}

	public void DBUpdateTheseItems(final Room room, final List<Item> items)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;
		final boolean useBulkInserts = DB.useBulkInserts();
		final StringBuilder bulkSQL = new StringBuilder("");
		final List<String> bulkClobs = new ArrayList<String>();
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Start item update for room "+room.roomID());
		final List<DBPreparedBatchEntry> statements=new Vector<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'"));
		for(int i=0;i<items.size();i++)
		{
			final Item thisItem=items.get(i);
			CMLib.map().registerWorldObjectLoaded(room.getArea(), room, thisItem);
			final Pair<String,String> sqlpair=getDBCreateItemString(room.roomID(),thisItem);
			if(!useBulkInserts)
				statements.add(new DBPreparedBatchEntry(sqlpair.first,sqlpair.second));
			else
			{
				final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sqlpair.first,sqlpair.second);
				if(entry != null)
					statements.add(entry);
			}
		}
		if((bulkSQL.length()>0) && useBulkInserts)
			statements.add(new DBPreparedBatchEntry(bulkSQL.toString(),bulkClobs.toArray(new String[0])));
		DB.updateWithClobs(statements);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Finished items update for room "+room.roomID());
	}

	public void DBUpdateItems(final Room room)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;
		DBUpdateTheseItems(room,DBGetContents(room));
	}

	public void DBUpdateExits(final Room room)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;

		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROEX)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Starting exit update for room "+room.roomID());
		final List<DBPreparedBatchEntry> statements=new Vector<DBPreparedBatchEntry>();
		final boolean useBulkInserts = DB.useBulkInserts();
		final StringBuilder bulkSQL = new StringBuilder("");
		final List<String> bulkClobs = new ArrayList<String>();
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
				final String fullSQL ="INSERT INTO CMROEX ("
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
						+"'"+((thisRoom==null)?" ":thisRoom.roomID())+"')";
				final String exitText = (thisExit==null)?" ":thisExit.text();
				if(!useBulkInserts)
					statements.add(new DBPreparedBatchEntry(fullSQL,exitText));
				else
				{
					final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,fullSQL,exitText);
					if(entry != null)
						statements.add(entry);
				}
			}
		}
		if(room instanceof GridLocale)
		{
			final HashSet<String> done=new HashSet<String>();
			int ordinal=0;
			for(final Iterator<GridLocale.CrossExit> i=((GridLocale)room).outerExits();i.hasNext();)
			{
				final GridLocale.CrossExit CE=i.next();
				Room R=CMLib.map().getRoom(CE.destRoomID);
				if(R==null)
					continue;
				if(R.getGridParent()!=null)
					R=R.getGridParent();
				if((R!=null)&&(R.isSavable())&&(!done.contains(R.roomID())))
				{
					done.add(R.roomID());
					final HashSet<String> oldStrs=new HashSet<String>();
					for(final Iterator<GridLocale.CrossExit> i2=((GridLocale)room).outerExits();i2.hasNext();)
					{
						final GridLocale.CrossExit CE2=i2.next();
						if((CE2.destRoomID.equals(R.roomID())
						||(CE2.destRoomID.startsWith(R.roomID()+"#("))))
						{
							final String str=CE2.x+" "+CE2.y+" "+((CE2.out?256:512)|CE2.dir)+" "+CE2.destRoomID.substring(R.roomID().length())+";";
							if(!oldStrs.contains(str))
								oldStrs.add(str);
						}
					}
					final StringBuffer exitStr=new StringBuffer("");
					for (final String string : oldStrs)
						exitStr.append(string);
					final String fullSQL =
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
					+"'"+R.roomID()+"')";
					final String exitText = exitStr.toString();
					if(!useBulkInserts)
						statements.add(new DBPreparedBatchEntry(fullSQL,exitText));
					else
					{
						final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,fullSQL,exitText);
						if(entry != null)
							statements.add(entry);
					}
				}
			}
		}
		if((bulkSQL.length()>0) && useBulkInserts)
			statements.add(new DBPreparedBatchEntry(bulkSQL.toString(),bulkClobs.toArray(new String[0])));
		DB.updateWithClobs(statements);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROEX)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Finished exit update for room "+room.roomID());
	}

	public void DBCreateThisMOB(final String roomID, final MOB thisMOB)
	{
		final Pair<String,String> sql= getDBCreateMOBString(roomID,thisMOB);
		final DBPreparedBatchEntry entry = new DBPreparedBatchEntry(sql.first,sql.second);
		DB.updateWithClobs(entry);
	}

	public Pair<String,String> getDBCreateMOBString(final String roomID, final MOB thisMOB)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Creating mob "+thisMOB.name()+" for room "+roomID);
		final boolean catalog=((roomID!=null)&&(roomID.startsWith("CATALOG_")));

		String ride=null;
		final String mobID=this.getShortID(thisMOB);
		thisMOB.setDatabaseID(mobID);
		if(thisMOB.riding()!=null)
			ride=this.getShortID(thisMOB.riding());
		else
		if(thisMOB.amFollowing()!=null)
			ride=this.getShortID(thisMOB.amFollowing());
		else
			ride="";

		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Created mob "+thisMOB.name()+" for room "+roomID);

		if((CMProps.getBoolVar(CMProps.Bool.MOBNOCACHE))&&(!catalog))
			thisMOB.setMiscText("%DBID>"+roomID+mobID.substring(mobID.indexOf('@')));

		String text=thisMOB.text();
		if(catalog)
		{
			final CatalogLibrary.CataData dataM=CMLib.catalog().getCatalogMobData(thisMOB.Name());
			if(dataM!=null)
				text+=dataM.data(null);
		}
		return new Pair<String,String>(
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

	public void DBUpdateTheseMOBs(final Room room, List<MOB> mobs)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;
		final boolean useBulkInserts = DB.useBulkInserts();
		final StringBuilder bulkSQL = new StringBuilder("");
		final List<String> bulkClobs = new ArrayList<String>();
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Updating mobs for room "+room.roomID());
		if(mobs==null)
			mobs=new ArrayList<MOB>();
		final List<DBPreparedBatchEntry> statements=new ArrayList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'"));
		for(int m=0;m<mobs.size();m++)
		{
			final MOB thisMOB=mobs.get(m);
			CMLib.map().registerWorldObjectLoaded(room.getArea(), room, thisMOB);
			final Pair<String,String> sqlpair= getDBCreateMOBString(room.roomID(),thisMOB);
			if(!useBulkInserts)
				statements.add(new DBPreparedBatchEntry(sqlpair.first,sqlpair.second));
			else
			{
				final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sqlpair.first,sqlpair.second);
				if(entry != null)
					statements.add(entry);
			}
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating mobs for room "+room.roomID());
		if((bulkSQL.length()>0) && useBulkInserts)
			statements.add(new DBPreparedBatchEntry(bulkSQL.toString(),bulkClobs.toArray(new String[0])));
		DB.updateWithClobs(statements);
	}

	public void DBUpdateMOBs(final Room room)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;
		final List<MOB> mobs=new ArrayList<MOB>();
		for(int m=0;m<room.numInhabitants();m++)
		{
			final MOB thisMOB=room.fetchInhabitant(m);
			if((thisMOB!=null)&&(thisMOB.isSavable()))
				mobs.add(thisMOB);
		}
		DBUpdateTheseMOBs(room,mobs);
	}

	public void DBUpdateAll(final Room room)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;
		DBUpdateRoom(room);
		DBUpdateMOBs(room);
		DBUpdateExits(room);
		DBUpdateItems(room);
	}

	public void DBUpdateRoom(final Room room)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;
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

	public void DBReCreate(final Room room, final String oldID)
	{
		if((!room.isSavable())||(room.amDestroyed()))
			return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROOM)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Recreating room "+room.roomID());

		DB.update(
		"UPDATE CMROOM SET "
		+"CMROID='"+room.roomID()+"', "
		+"CMAREA='"+room.getArea().Name()+"' "
		+"WHERE CMROID='"+oldID+"'");

		if(CMProps.getBoolVar(CMProps.Bool.MOBNOCACHE))
		{
			for(int m=0;m<room.numInhabitants();m++)
			{
				final MOB M=room.fetchInhabitant(m);
				if((M!=null)&&(M.isSavable()))
					M.setMiscText(M.text());
			}

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

	public void DBCreate(final Area A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Creating area "+A.name());
		if((A==null)||(A.name().length()==0))
		{
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

	public void DBUpdate(final String areaID, final Area A)
	{
		if((A==null)||(!A.isSavable()))
			return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Updating area "+A.name());
		final boolean ignoreType=CMSecurity.isDisabled(CMSecurity.DisFlag.FATAREAS)||CMSecurity.isDisabled(CMSecurity.DisFlag.THINAREAS);
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
		+"WHERE CMAREA='"+areaID+"'",
		new String[]{A.description()+" ",A.text()+" "});
		if(Log.debugChannelOn()
		&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating area "+A.name());
	}

	public void DBDeleteRoomItem(final String roomID, final Item item)
	{
		String keyName=item.databaseID();
		if(keyName.length()==0)
			keyName=this.getShortID(item);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROIT)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating item "+item.name()+" in room "+roomID);
		DB.update(
		"DELETE FROM CMROIT "
		+"WHERE CMROID='"+roomID+"' "
		+"AND CMITNM='"+keyName+"'");
	}

	public void DBUpdateRoomItem(final String roomID, final Item item)
	{
		if((roomID==null)||(!item.isSavable())||(item.amDestroyed()))
			return;
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

	public void DBDeleteRoomMOB(final String roomID, final MOB mob)
	{
		String keyName=mob.databaseID();
		if(keyName.length()==0)
			keyName=this.getShortID(mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+roomID);
		DB.update(
		"DELETE FROM CMROCH "
		+"WHERE CMROID='"+roomID+"' "
		+"AND CMCHNM='"+keyName+"'");
	}

	public void DBUpdateRoomMOB(final String roomID, final MOB mob)
	{
		if((roomID==null)||(!mob.isSavable())||(mob.amDestroyed()))
			return;
		DBDeleteRoomMOB(roomID, mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Continue updating mob "+mob.name()+" in room "+roomID);
		DBCreateThisMOB(roomID,mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+roomID);
	}

	public void DBCreate(final Room room)
	{
		if(!room.isSavable())
			return;
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

	protected List<String> getRoomDeleteStrings(final String roomID)
	{
		return new XVector<String>(
			"DELETE FROM CMROEX WHERE CMROID='"+roomID+"'",
			"DELETE FROM CMROCH WHERE CMROID='"+roomID+"'",
			"DELETE FROM CMROIT WHERE CMROID='"+roomID+"'"
		);
	}

	public void DBDelete(final Room room)
	{
		if(!room.isSavable())
			return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Destroying room "+room.roomID());
		DB.update(getRoomDeleteStrings(room.roomID()).toArray(new String[0]));
		DB.update("DELETE FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
		room.destroy();
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMROCH)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done gestroying room "+room.roomID());
	}

	protected List<String> getAreaDeleteStrings(final String areaName)
	{
		return new XVector<String>(
			"DELETE FROM CMAREA WHERE CMAREA='"+areaName+"'",
			"DELETE FROM CMPDAT WHERE CMPLID='"+areaName+"' AND CMSECT='TIMECLOCK'"
		);
	}

	public void DBDelete(final Area A)
	{
		if(A==null)
			return;
		if(!A.isSavable())
			return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Destroying area "+A.name());
		A.setAreaState(Area.State.STOPPED);
		DB.update(getAreaDeleteStrings(A.Name()).toArray(new String[0]));
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Done destroying area "+A.name()+".");
	}

	public void DBDeleteAreaAndRooms(final Area A)
	{
		if(A==null)
			return;
		if(!A.isSavable())
			return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMAREA)||CMSecurity.isDebugging(CMSecurity.DbgFlag.DBROOMS)))
			Log.debugOut("RoomLoader","Destroying area "+A.name());
		A.setAreaState(Area.State.STOPPED);
		final List<String> statements = new Vector<String>(4 + A.numberOfProperIDedRooms());
		statements.addAll(getAreaDeleteStrings(A.Name()));
		statements.add("DELETE FROM CMROOM WHERE CMAREA='"+A.Name()+"'");
		for(final Enumeration<String> ids=A.getProperRoomnumbers().getRoomIDs();ids.hasMoreElements();)
			statements.addAll(getRoomDeleteStrings(ids.nextElement()));
		statements.add("DELETE FROM CMROOM WHERE CMAREA='"+A.Name()+"'");
		final String[] statementsBlock = statements.toArray(new String[statements.size()]);
		statements.clear();
		for(int i=0;i<3;i++)
		{
			// not even mysql delete everything as commanded.
			DB.update(statementsBlock);
			try
			{
				Thread.sleep(A.numberOfProperIDedRooms());
			}
			catch(final Exception e)
			{
			}
		}
	}
}
