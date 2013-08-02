package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
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
public interface WorldMap extends CMLibrary
{
	public final static long ROOM_EXPIRATION_MILLIS=2500000;
	
	/************************************************************************/
	/**							 AREAS										*/
	/************************************************************************/
	public int numAreas();
	public void addArea(Area newOne);
	public void delArea(Area oneToDel);
	public Area getArea(String calledThis);
	public Area findAreaStartsWith(String calledThis);
	public Area findArea(String calledThis);
	public Area getDefaultParentArea();
	public Enumeration<Area> areas();
	public Enumeration<Area> sortedAreas();
	public Area getFirstArea();
	public Area getRandomArea();
	public void obliterateArea(Area theOne);
	
	/************************************************************************/
	/**							 ROOMS										*/
	/************************************************************************/
	public int numRooms();
	public Enumeration<String> roomIDs();
	public String getExtendedRoomID(final Room R);
	public String getExtendedTwinRoomIDs(final Room R1,final Room R2);
	public Room getRoom(Room room);
	public Room getRoom(String calledThis);
	public Room getRoom(Enumeration<Room> roomSet, String calledThis);
	public Enumeration<Room> rooms();
	public Enumeration<Room> roomsFilled();
	public Room getRandomRoom();
	public void renameRooms(Area A, String oldName, List<Room> allMyDamnRooms);
	public void obliterateRoom(Room deadRoom);
	public Room findConnectingRoom(Room room);
	public int getRoomDir(Room from, Room to);
	public int getExitDir(Room from, Exit to);
	
	/************************************************************************/
	/**								SEARCH TOOLS 							*/
	/************************************************************************/
	public List<Room> findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);
	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);
	public List<Room> findAreaRoomsLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public Room findAreaRoomLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public List<Room> findRooms(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public Room findFirstRoom(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public MOB findFirstInhabitant(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<MOB> findInhabitants(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Item> findRoomItems(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);
	public Item findFirstRoomItem(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);
	public List<Environmental> findShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Environmental findFirstShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Environmental> findShopStockers(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Environmental findFirstShopStocker(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Item> findInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Item findFirstInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	
	/************************************************************************/
	/**							 ROOM-AREA-UTILITIES						*/
	/************************************************************************/
	public void resetArea(Area area);
	public void resetRoom(Room room);
	public void resetRoom(Room room, boolean rebuildGrids);
	public Room getStartRoom(Environmental E);
	public Area getStartArea(Environmental E);
	public Room roomLocation(Environmental E);
	public void emptyRoom(Room room, Room bringBackHere);
	public void emptyArea(Area A);
	public boolean hasASky(Room room);
	public boolean isClearableRoom(Room room);
	public String createNewExit(Room from, Room room, int direction);
	public Area areaLocation(CMObject E);
	public boolean explored(Room R);
	public CMFile.CMVFSDir getMapRoot(final CMFile.CMVFSDir root);
	
	/************************************************************************/
	/**						WORLD OBJECT INDEXES							*/
	/************************************************************************/
	public void registerWorldObjectLoaded(Area area, Room room, CMObject o);
	public void registerWorldObjectDestroyed(Area area, Room room, CMObject o);
	public Enumeration<LocatedPair> scriptHosts(Area area);
	public Deity getDeity(String calledThis);
	public Enumeration<Deity> deities();
	public PostOffice getPostOffice(String chain, String areaNameOrBranch);
	public Enumeration<PostOffice> postOffices();
	public Banker getBank(String chain, String areaNameOrBranch);
	public Enumeration<Banker> banks();
	public Iterator<String> bankChains(Area AreaOrNull);
	public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch);
	public Enumeration<Auctioneer> auctionHouses();
	
	/************************************************************************/
	/**							 SPACE METHODS 								*/
	/************************************************************************/
	public long getRelativeSpeed(SpaceObject O1, SpaceObject O2);
	public boolean isObjectInSpace(SpaceObject O);
	public void delObjectInSpace(SpaceObject O);
	public void addObjectToSpace(SpaceObject O, long[] coords);
	public long getDistanceFrom(SpaceObject O1, SpaceObject O2);
	public double[] getDirection(SpaceObject FROM, SpaceObject TO);
	public long[] getLocation(long[] oldLocation, double[] direction, long distance);
	public void moveSpaceObject(SpaceObject O);
	public void moveSpaceObject(SpaceObject O, long[] coords);
	public SpaceObject getSpaceObject(CMObject o, boolean ignoreMobs);
	public Enumeration<SpaceObject> getSpaceObjects();
	public List<SpaceObject> getSpaceObjectsWithin(SpaceObject ofObj, long minDistance, long maxDistance);
	
	/************************************************************************/
	/**							 MESSAGES	 								*/
	/************************************************************************/
	public void addGlobalHandler(MsgListener E, int category);
	public void delGlobalHandler(MsgListener E, int category);
	public MOB deity();
	public MOB getFactoryMOBInAnyRoom();
	public MOB getFactoryMOB(Room R);
	public boolean sendGlobalMessage(MOB host, int category, CMMsg msg);
	
	/************************************************************************/
	/**							 HELPER CLASSES								*/
	/************************************************************************/
	public static class LocatedPair
	{
		private final WeakReference<Room> roomW;
		private final WeakReference<PhysicalAgent> objW;
		public Room room(){return roomW.get();}
		public PhysicalAgent obj(){return objW.get();}
		public LocatedPair(final Room room, final PhysicalAgent obj)
		{ this.roomW=new WeakReference<Room>(room); this.objW=new WeakReference<PhysicalAgent>(obj);}
	}
	
	public static class CrossExit
	{
		public int x;
		public int y;
		public int dir;
		public String destRoomID="";
		public boolean out=false;
		public static CrossExit make(int xx, int xy, int xdir, String xdestRoomID, boolean xout)
		{   CrossExit EX=new CrossExit();
			EX.x=xx;EX.y=xy;EX.dir=xdir;EX.destRoomID=xdestRoomID;EX.out=xout;
			return EX;
		}
	}
	
	public class CompleteRoomIDEnumerator implements Enumeration<String>
	{
		Enumeration<String> roomIDEnumerator=null;
		Enumeration<Area> areaEnumerator=null;
		public CompleteRoomIDEnumerator(WorldMap map){areaEnumerator=map.areas();}
		public boolean hasMoreElements()
		{
			if((roomIDEnumerator==null)||(!roomIDEnumerator.hasMoreElements()))
				while(areaEnumerator.hasMoreElements())
				{
					Area A=areaEnumerator.nextElement();
					roomIDEnumerator=A.getProperRoomnumbers().getRoomIDs();
					if(roomIDEnumerator.hasMoreElements()) return true;
				}
			return ((roomIDEnumerator!=null)&&(roomIDEnumerator.hasMoreElements()));
		}
		public String nextElement(){ return hasMoreElements()?(String)roomIDEnumerator.nextElement():null;}
	}

	public static class MapCacheEntry implements CMObject
	{
		public final List<Room> rooms;
		public final String ID;
		public volatile long lastAccessed=System.currentTimeMillis();
		public MapCacheEntry(final String ID, final List<Room> rooms) {this.ID=ID; this.rooms=rooms;}
		public String ID() { return ID;}
		public String name() { return ID();}
		public CMObject copyOf() { return this;}
		public void initializeClass() {}
		public CMObject newInstance() { return this;}
		public int compareTo(CMObject o) { return ID.compareTo(o.ID()); }
	}
}
