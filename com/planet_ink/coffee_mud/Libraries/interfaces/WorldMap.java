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
import java.util.Map.Entry;
/*
   Copyright 2005-2018 Bo Zimmerman

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

	/* ***********************************************************************/
	/* *							 AREAS										*/
	/* ***********************************************************************/
	public int numAreas();
	public void addArea(Area newOne);
	public void delArea(Area oneToDel);
	public Area getArea(String calledThis);
	public Area findAreaStartsWith(String calledThis);
	public Area findArea(String calledThis);
	public Area getDefaultParentArea();
	public Enumeration<Area> areas();
	public Enumeration<Area> mundaneAreas();
	public Enumeration<Area> spaceAreas();
	public Area getFirstArea();
	public Area getModelArea(Area A);
	public Area getRandomArea();
	public void obliterateArea(Area theOne);
	public void renamedArea(Area theA);

	/* ***********************************************************************/
	/* *							 ROOMS										*/
	/* ***********************************************************************/
	public int numRooms();
	public Enumeration<String> roomIDs();
	public String getExtendedRoomID(final Room R);
	public String getDescriptiveExtendedRoomID(final Room room);
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
	public Area getTargetArea(Room from, Exit to);
	public Room getTargetRoom(Room from, Exit to);

	/* ***********************************************************************/
	/* *								SEARCH TOOLS 							*/
	/* ***********************************************************************/
	public List<Room> findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);
	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);
	public List<Room> findAreaRoomsLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public Room findAreaRoomLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public List<Room> findRooms(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public Room findFirstRoom(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public MOB findFirstInhabitant(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<MOB> findInhabitantsFavorExact(Enumeration<Room> rooms, MOB mob, String srchStr, boolean returnFirst, int timePct);
	public List<MOB> findInhabitants(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Item> findRoomItems(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);
	public Item findFirstRoomItem(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);
	public List<Environmental> findShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Environmental findFirstShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Environmental> findShopStockers(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Environmental findFirstShopStocker(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Item> findInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Item findFirstInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public boolean isHere(CMObject E2, Area here);
	public boolean isHere(CMObject E2, Room here);

	/* ***********************************************************************/
	/* *							 ROOM-AREA-UTILITIES						*/
	/* ***********************************************************************/
	public void resetArea(Area area);
	public void resetRoom(Room room);
	public void resetRoom(Room room, boolean rebuildGrids);
	public Room getStartRoom(Environmental E);
	public Area getStartArea(Environmental E);
	public Room roomLocation(Environmental E);
	public void emptyRoom(Room room, Room toRoom, boolean clearPlayers);
	public void emptyAreaAndDestroyRooms(Area A);
	public boolean hasASky(Room room);
	public boolean isClearableRoom(Room room);
	public String createNewExit(Room from, Room room, int direction);
	public Area areaLocation(CMObject E);
	public ThreadGroup getOwnedThreadGroup(CMObject E);
	public boolean explored(Room R);
	public CMFile.CMVFSDir getMapRoot(final CMFile.CMVFSDir root);

	/* ***********************************************************************/
	/* *						WORLD OBJECT INDEXES							*/
	/* ***********************************************************************/
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
	public int numLibraries();
	public Librarian getLibrary(String chain, String areaNameOrBranch);
	public Enumeration<Librarian> libraries();
	public Iterator<String> libraryChains(Area AreaOrNull);
	public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch);
	public Enumeration<Auctioneer> auctionHouses();
	public BoardableShip getShip(String calledThis);
	public Enumeration<BoardableShip> ships();
	public int numShips();
	public Room getSafeRoomToMovePropertyTo(Room room, PrivateProperty I);

	/* ***********************************************************************/
	/* *							 SPACE METHODS 								*/
	/* ***********************************************************************/
	public Enumeration<Area> areasPlusShips();
	public long getRelativeSpeed(SpaceObject O1, SpaceObject O2);
	public int numSpaceObjects();
	public boolean isObjectInSpace(SpaceObject O);
	public void delObjectInSpace(SpaceObject O);
	public void addObjectToSpace(SpaceObject O, long[] coords);
	public long getDistanceFrom(SpaceObject O1, SpaceObject O2);
	public long getDistanceFrom(final long[] coord1, final long[] coord2);
	public double getAngleDelta(final double[] fromAngle, final double[] toAngle);
	public double getMinDistanceFrom(SpaceObject FROM, long prevDistance, SpaceObject TO);
	public double[] getDirection(SpaceObject FROM, SpaceObject TO);
	public TechComponent.ShipDir getDirectionFromDir(double[] facing, double roll, double[] direction);
	public long[] getLocation(long[] oldLocation, double[] direction, long distance);
	public void moveSpaceObject(SpaceObject O);
	public void moveSpaceObject(SpaceObject O, long[] coords);
	public void moveSpaceObject(final SpaceObject O, final double[] accelDirection, final long newAccelleration);
	public double moveSpaceObject(final double[] curDirection, final double curSpeed, final double[] accelDirection, final long newAccelleration);
	public long[] moveSpaceObject(final long[] coordinates, final double[] direction, long speed);
	public SpaceObject getSpaceObject(CMObject o, boolean ignoreMobs);
	public Enumeration<SpaceObject> getSpaceObjects();
	public Enumeration<Entry<SpaceObject, List<WeakReference<TrackingVector<SpaceObject>>>>>  getSpaceObjectEntries();
	public List<SpaceObject> getSpaceObjectsWithin(SpaceObject ofObj, long minDistance, long maxDistance);
	public List<SpaceObject> getSpaceObjectsByCenterpointWithin(final long[] centerCoordinates, long minDistance, long maxDistance);
	public SpaceObject findSpaceObject(String s, boolean exactOnly);
	public String getSectorName(long[] coordinates);
	public long[] getInSectorCoords(long[] coordinates);

	/* ***********************************************************************/
	/* *							 MESSAGES	 								*/
	/* ***********************************************************************/
	public void addGlobalHandler(MsgListener E, int category);
	public void delGlobalHandler(MsgListener E, int category);
	public MOB deity();
	public MOB getFactoryMOBInAnyRoom();
	public MOB getFactoryMOB(Room R);
	public boolean sendGlobalMessage(MOB host, int category, CMMsg msg);

	/* ***********************************************************************/
	/* *							 HELPER CLASSES								*/
	/* ***********************************************************************/
	public static interface LocatedPair
	{
		public Room room();
		public PhysicalAgent obj();
	}
}
