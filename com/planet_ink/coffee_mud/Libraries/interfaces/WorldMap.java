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
   Copyright 2005-2023 Bo Zimmerman

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
	/* *							 AREAS									 */
	/* ***********************************************************************/
	public int numAreas();
	public void addArea(Area newOne);
	public void delArea(Area oneToDel);
	public Area getArea(String calledThis);
	public Area findAreaStartsWith(String calledThis);
	public Area findArea(String calledThis);
	public Area getDefaultParentArea();
	public Area findRoomIDArea(final String roomID);
	public Enumeration<Area> areas();
	public Enumeration<Area> mundaneAreas();
	public Enumeration<Area> topAreas();
	public Enumeration<Area> areasPlusShips();
	public Area getFirstArea();
	public Area getModelArea(Area A);
	public Area getRandomArea();
	public void obliterateMapArea(Area theOne);
	public void destroyAreaObject(Area theOne);
	public void renamedArea(Area theA);

	/* ***********************************************************************/
	/* *							 ROOMS									 */
	/* ***********************************************************************/
	public int numRooms();
	public Enumeration<String> roomIDs();
	public String getExtendedRoomID(final Room R);
	public String getDescriptiveExtendedRoomID(final Room room);
	public String getExtendedTwinRoomIDs(final Room R1,final Room R2);
	public String getApproximateExtendedRoomID(final Room room);
	public Room getRoom(Room room);
	public Room getRoom(String calledThis);

	//TODO:
	/**
	 *
	 * @see WorldMap#getRoom(Room)
	 * @see WorldMap#getRoom(String)
	 * @see WorldMap#getRoom(Enumeration, String)
	 * @see WorldMap#getCachedRoom(String)
	 * @see WorldMap#getRoomAllHosts(String)
	 *
	 * @param roomSet
	 * @param calledThis the room ID to get
	 * @return null, or the room from the set
	 */
	public Room getRoom(Enumeration<Room> roomSet, String calledThis);

	/**
	 * Given a room ID, this will return the room on the map that matches, it.
	 * It will only match cached rooms, esp from thin areas.
	 *
	 * @see WorldMap#getRoom(Room)
	 * @see WorldMap#getRoom(String)
	 * @see WorldMap#getRoom(Enumeration, String)
	 * @see WorldMap#getCachedRoom(String)
	 * @see WorldMap#getRoomAllHosts(String)
	 *
	 * @param calledThis the room ID to get
	 * @return null, or the room from the map
	 */
	public Room getCachedRoom(final String calledThis);

	/**
	 * Given a room ID, this will return the room on the map that matches, it.
	 * This will include ALL maps on every host in this mud.
	 * This will de-cache any rooms from thin areas.
	 *
	 * @see WorldMap#getRoom(Room)
	 * @see WorldMap#getRoom(String)
	 * @see WorldMap#getRoom(Enumeration, String)
	 * @see WorldMap#getCachedRoom(String)
	 *
	 * @param calledThis the room ID to get
	 * @return null, or the room from the map
	 */
	public Room getRoomAllHosts(final String calledThis);

	/**
	 * Returns an enumeration of every CACHED proper room in every area, including
	 * 'filled' proper rooms, which also includes ships and similar areas.
	 *
	 * @see WorldMap#roomsFilled()
	 *
	 * @return the enumeration of all the cached proper rooms
	 */
	public Enumeration<Room> rooms();

	/**
	 * Returns an enumeration of every CACHED room in every area, including
	 * 'filled' rooms, which includes skys and underwater rooms, which
	 * also includes ships and similar areas.
	 *
	 * @see WorldMap#rooms()
	 *
	 * @return the enumeration of all the cached rooms
	 */
	public Enumeration<Room> roomsFilled();

	/**
	 * Returns a JIT enumeration of every CACHED mob in every room
	 * on the map, as it presently exists at the time the Next
	 * method is called.
	 *
	 * @see WorldMap#worldEveryItems()
	 * @see WorldMap#worldRoomItems()
	 *
	 * @return the enumeration of all the mobs
	 */
	public Enumeration<MOB> worldMobs();

	/**
	 * Returns a JIT enumeration of every CACHED item in every room
	 * on the map, as it presently exists at the time the Next
	 * method is called.
	 *
	 * @see WorldMap#worldEveryItems()
	 * @see WorldMap#worldMobs()
	 *
	 * @return the enumeration of all the items
	 */
	public Enumeration<Item> worldRoomItems();

	/**
	 * Returns a JIT enumeration of every CACHED item in every room
	 * on the map, well as every mob in every room on the map,
	 * as it presently exists at the time the Next method is
	 * called.
	 *
	 * @see WorldMap#worldEveryItems()
	 * @see WorldMap#worldMobs()
	 *
	 * @return the enumeration of all the items
	 */
	public Enumeration<Item> worldEveryItems();

	/**
	 * Returns a random room from a random area on the map.
	 * Just for fun -- typically for temporary mobs.
	 *
	 * @return a random cached room from the map
	 */
	public Room getRandomRoom();

	/**
	 * This will take an area that was recently renamed, and an optional list of
	 * all its existing rooms, and rename the rooms to reflect the new name,
	 * updating the database along the way.
	 *
	 * @param A the area that WAS renamed
	 * @param oldName the previous name of the given area
	 * @param allMyDamnRooms null, or the list of existing rooms.
	 */
	public void renameRooms(Area A, String oldName, List<Room> allMyDamnRooms);

	/**
	 * Removes the given room from the map, clears it, and deletes
	 * any incoming exits.  Then deletes it, and all internally saved
	 * objects from the DB.
	 *
	 * @see WorldMap#destroyRoomObject(Room)
	 * @see WorldMap#obliterateMapArea(Area)
	 *
	 * @param deadRoom the room to remove from the map
	 */
	public void obliterateMapRoom(final Room deadRoom);

	/**
	 * Removes the given room from the map, clears it, and deletes
	 * any incoming exits.  Does not affect the DB.
	 *
	 * @see WorldMap#obliterateMapRoom(Room)
	 * @see WorldMap#destroyAreaObject(Area)
	 *
	 * @param deadRoom the room to remove from the map
	 */
	public void destroyRoomObject(final Room deadRoom);

	/**
	 * Returns a room that is connected to, and preferably links
	 * back, to the given room.  This can include skys, and
	 * might prefer them.
	 *
	 * @param room the room to look at exits from
	 * @return a connecting room
	 */
	public Room findConnectingRoom(Room room);

	/**
	 * Given a room and a target room, this will return which
	 * direction code is being used.
	 *
	 * @see com.planet_ink.coffee_mud.core.Directions
	 * @see WorldMap#getTargetArea(Room, Exit)
	 * @see WorldMap#getTargetRoom(Room, Exit)
	 * @see WorldMap#getExitDir(Room, Exit)
	 *
	 * @param from the initial room
	 * @param to the room being target
	 * @return the direction code from the from room to the target
	 */
	public int getRoomDir(Room from, Room to);

	/**
	 * Given a room and an exit, this will return which
	 * direction code is being used.
	 *
	 * @see com.planet_ink.coffee_mud.core.Directions
	 * @see WorldMap#getTargetArea(Room, Exit)
	 * @see WorldMap#getTargetRoom(Room, Exit)
	 * @see WorldMap#getRoomDir(Room, Room)
	 *
	 * @param from the initial room
	 * @param to the exit being used
	 * @return the direction code
	 */
	public int getExitDir(Room from, Exit to);

	/**
	 * Given a room and an exit, this will return which area
	 * it will lead to.
	 *
	 * @see WorldMap#getTargetRoom(Room, Exit)
	 * @see WorldMap#getRoomDir(Room, Room)
	 * @see WorldMap#getExitDir(Room, Exit)
	 *
	 * @param from the initial room
	 * @param to the exit being used
	 * @return the area it leads to, or null
	 */
	public Area getTargetArea(Room from, Exit to);

	/**
	 * Given a room and an exit, this will return which room
	 * it will lead to.
	 *
	 * @see WorldMap#getTargetArea(Room, Exit)
	 * @see WorldMap#getRoomDir(Room, Room)
	 * @see WorldMap#getExitDir(Room, Exit)
	 *
	 * @param from the initial room
	 * @param to the exit being used
	 * @return the room it leads to, or null
	 */
	public Room getTargetRoom(Room from, Exit to);

	/* ***********************************************************************/
	/* *							 ROOM-AREA-UTILITIES					 */
	/* ***********************************************************************/
	/**
	 * Resets the given area by resetting all the cached rooms in it.
	 *
	 * @param area the area to reset.
	 */
	public void resetArea(Area area);

	/**
	 * Resets the contents of the given room to stock, without clearing grid
	 * rooms.
	 *
	 * @param room the room whose content to reset
	 */
	public void resetRoom(Room room);

	/**
	 * Resets the contents of the given room to stock, clearing any grids
	 * if necessary.
	 *
	 * @param room the room whose content to reset
	 * @param rebuildGrids true to also clear and rebuild grids, or false otherwise
	 */
	public void resetRoom(Room room, boolean rebuildGrids);

	/**
	 * Attempts to return the start/orig room for the given mob or item
	 * or room or whatever.
	 *
	 * @param E the object to get a start room for
	 * @return the start room, or the area its in
	 */
	public Room getStartRoom(Environmental E);

	/**
	 * Attempts to return the start/orig area for the given mob or item
	 * or room or whatever.
	 *
	 * @param E the object to get a start area for
	 * @return the start area, or the area its in
	 */
	public Area getStartArea(Environmental E);

	/**
	 * Returns the Room in which the given object exists in, or is
	 * attached to.  Handles almost any kind of object in the game,
	 * so long as it can be traced back to a room.
	 *
	 * @param E the game object whose room location you are curious about
	 * @return null, or the room that the object is in
	 */
	public Room roomLocation(Environmental E);

	/**
	 * Empties the given room of mobs and items, optionally moving stuff to
	 * another room.  If no target junk room is given, you can optionally
	 * move players somewhere safe anyway.
	 *
	 * @param room the room to clear
	 * @param toRoom null, or the room to move all mobs/items to
	 * @param clearPlayers true to move players anyway, false otherwise
	 */
	public void emptyRoom(Room room, Room toRoom, boolean clearPlayers);

	/**
	 * This method removes any area effects, and then empties and
	 * destroys every room in the area.  Perfect for instances,
	 * and other non-permanent areas.  Does not actually destroy
	 * the area object per-se.
	 *
	 * @param A the area to empty
	 */
	public void emptyAreaAndDestroyRooms(Area A);

	/**
	 * Returns whether the given room might have a sky, due to
	 * being outdoors but not being underwater.
	 *
	 * @param room the room to check
	 * @return true if a sky would be appropriate, false otherwise
	 */
	public boolean hasASky(Room room);

	/**
	 * Sends any mobs or players in the given room to their start room,
	 * and returns whether any players, private property, or player
	 * corpses remain.  Also returns false if the room is under a temporary
	 * effect.
	 *
	 * @param room the room to clear/check.
	 * @return true if the room is ready to be cleared, false otherwise
	 */
	public boolean isClearableRoom(Room room);

	/**
	 * Attempts to create a pair of Open exits from the from room to the
	 * room room.  If any errors are found in the inputs, then a descriptive
	 * error is returned.  The exits changes ARE SAVED TO THE DB!
	 *
	 * @param from the from room
	 * @param room the to room
	 * @param direction the direction from the from to the room room
	 * @return "", or an error message if something went wrong.
	 */
	public String createNewExit(Room from, Room room, int direction);

	/**
	 * Returns the Area in which the given object exists in, or is
	 * attached to.  Handles almost any kind of object in the game,
	 * so long as it can be traced back to a room or area.
	 *
	 * @param E the game object whose area location you are curious about
	 * @return null, or the area that the object is in
	 */
	public Area areaLocation(CMObject E);

	/**
	 * Generates a fake VFS file tree for the database game world map.
	 * The nodes are generated dynamically during browsing, so this call
	 * is very efficient.
	 *
	 * @param root the root directory to add the map directory to
	 * @return the fake VFS map directory
	 */
	public CMFile.CMVFSDir getMapRoot(final CMFile.CMVFSDir root);

	/* ***********************************************************************/
	/* *						WORLD OBJECT INDEXES						 */
	/* ***********************************************************************/

	/**
	 * When a map object is permanently added to the map, such as during map
	 * load, this method is called to give the world map manager a chance
	 * to keep track of it.
	 *
	 * @see WorldMap#registerWorldObjectDestroyed(Area, Room, CMObject)
	 *
	 * @param area the area that the object was loaded into
	 * @param room the room that the object was loaded into
	 * @param o the object to register as loaded
	 */
	public void registerWorldObjectLoaded(Area area, Room room, CMObject o);

	/**
	 * When a map object is permanently destroyed, this method is called
	 * to give the world map manager a chance to remove tracking for it.
	 *
	 * @see WorldMap#registerWorldObjectLoaded(Area, Room, CMObject)
	 *
	 * @param area the area that the object was originally loaded into
	 * @param room the room that the object was originally loaded into
	 * @param o the object to de-register
	 */
	public void registerWorldObjectDestroyed(Area area, Room room, CMObject o);

	/**
	 * Checks the list of existing registered boardables, such as sailing ships,
	 * space ships, castles, and caravans.  It returns the exact matching named
	 * boardable.
	 *
	 * @see WorldMap#findShip(String, boolean)
	 * @see WorldMap#ships()
	 * @see WorldMap#shipsRoomEnumerator(Area)
	 * @see WorldMap#numShips()
	 *
	 * @param calledThis the name of the boardable
	 * @return the boardable found, or null
	 */
	public Boardable getShip(String calledThis);

	/**
	 * Searches the list of existing registered boardables, such as sailing ships,
	 * space ships, castles, and caravans.  It returns the first one found.
	 *
	 * @see WorldMap#getShip(String)
	 * @see WorldMap#ships()
	 * @see WorldMap#shipsRoomEnumerator(Area)
	 * @see WorldMap#numShips()
	 *
	 * @param s the name search
	 * @param exactOnly true for exact matches only, false for substring matches
	 * @return null, or the first found boardable that matches
	 */
	public Boardable findShip(final String s, final boolean exactOnly);

	/**
	 * Returns an enumeration of all registered boardables, such as sailing ships,
	 * space ships, castles, and caravans.
	 *
	 * @see WorldMap#getShip(String)
	 * @see WorldMap#findShip(String, boolean)
	 * @see WorldMap#shipsRoomEnumerator(Area)
	 * @see WorldMap#numShips()
	 *
	 * @return the enumeration of existing boardables
	 */
	public Enumeration<Boardable> ships();

	/**
	 * Returns an enumerator of all rooms contained in a registered
	 * boardable, such as a sailing ship, space ship, castle, or
	 * caravan, which is also in the given Area.
	 *
	 * @see WorldMap#getShip(String)
	 * @see WorldMap#findShip(String, boolean)
	 * @see WorldMap#ships()
	 * @see WorldMap#numShips()
	 *
	 * @param inA the area containing a ship, possibly
	 * @return the boardable rooms enumerator
	 */
	public Enumeration<Room> shipsRoomEnumerator(final Area inA);

	/**
	 * Returns the number of registered boardables, such as sailing ships,
	 * space ships, castles, and caravans.
	 *
	 * @see WorldMap#getShip(String)
	 * @see WorldMap#findShip(String, boolean)
	 * @see WorldMap#ships()
	 * @see WorldMap#shipsRoomEnumerator(Area)
	 * @see WorldMap#numShips()
	 *
	 * @return the number of registered boardables
	 */
	public int numShips();

	/**
	 * Returns the parent room of the given private property, if it is a boardable,
	 * or the given room if not. The parent of a grid, a nearby room, or at least
	 * a random one is always returned.
	 *
	 * @param room the room that needs moving FROM
	 * @param I null, or private property that needs moving
	 * @return a room thats not the given one
	 */
	public Room getSafeRoomToMovePropertyTo(Room room, PrivateProperty I);

	/**
	 * Returns an enumeration of all objects scripted upon creation
	 * in the world, or in the given area (non-metro).
	 *
	 * @see WorldMap.LocatedPair#room()
	 *
	 * @param area null, or the area to limit returns to
	 * @return an enumeration of the scripted objects
	 */
	public Enumeration<LocatedPair> scriptHosts(Area area);

	/**
	 * Returns the first available deity, creating one from
	 * scratch if necessary.
	 *
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.Deity
	 * @see WorldMap#deities()
	 * @see WorldMap#getDeity(String)
	 *
	 * @return a deity
	 */
	public MOB deity();

	/**
	 * Returns the world deity of the given name, if it exists.
	 *
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.Deity
	 * @see WorldMap#deities()
	 * @see WorldMap#deity()
	 *
	 * @param calledThis the name of the deity to get.
	 * @return null, or the deity object found
	 */
	public Deity getDeity(String calledThis);

	/**
	 * Returns an enumeration of all the Deity-derived registered objects
	 * in the game.
	 *
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.Deity
	 * @see WorldMap#deity()
	 * @see WorldMap#getDeity(String)
	 *
	 * @return all the deities
	 */
	public Enumeration<Deity> deities();

	/**
	 * Returns the world-wide clock/timezone cache.  This map is
	 * editable by the caller for submitting new clocks, or
	 * referencing existing ones.
	 *
	 * The key is typically an area name.
	 *
	 * @return the world-wide clock cache
	 */
	public Map<String,TimeClock> getClockCache();

	/* ***********************************************************************/
	/* *							 MISC		 							 */
	/* ***********************************************************************/

	/**
	 * If a mob is needed as a msg host/source, and it doesn't matter the
	 * name, level, room, or anything, then this is the method for you.
	 *
	 * @see WorldMap#getFactoryMOB(Room)
	 *
	 * @return a mob, which you should destroy after use
	 */
	public MOB getFactoryMOBInAnyRoom();

	/**
	 * If a mob is needed as a msg host/source, and it doesn't matter the
	 * name or level, then this is the method for you.  Just specify the
	 * room that the mob will be pointed at (not necc IN).
	 *
	 *   @see WorldMap#getFactoryMOBInAnyRoom()
	 *
	 * @param R the room to put the temporary mob in
	 * @return a mob, which you should destroy after use
	 */
	public MOB getFactoryMOB(Room R);

	/* ***********************************************************************/
	/* *							 MESSAGES	 							 */
	/* ***********************************************************************/

	/**
	 * Add a listener to the Global Alternative message passing system.
	 *
	 * @see WorldMap#delGlobalHandler(MsgListener, int)
	 * @see WorldMap#sendGlobalMessage(MOB, int, CMMsg)
	 *
	 * @param E the message listener for the global message
	 * @param category the CMMsg TYP code ({@link com.planet_ink.coffee_mud.Common.interfaces.CMMsg}
	 */
	public void addGlobalHandler(MsgListener E, int category);

	/**
	 * Remove a listener from the Global Alternative message passing system.
	 *
	 * @see WorldMap#addGlobalHandler(MsgListener, int)
	 * @see WorldMap#sendGlobalMessage(MOB, int, CMMsg)
	 *
	 * @param E the message listener for the global message
	 * @param category the CMMsg TYP code ({@link com.planet_ink.coffee_mud.Common.interfaces.CMMsg}
	 */
	public void delGlobalHandler(MsgListener E, int category);

	/**
	 * Send a message to all relevant listeners in the Global
	 * Alternative message passing system.
	 *
	 * @see WorldMap#addGlobalHandler(MsgListener, int)
	 * @see WorldMap#delGlobalHandler(MsgListener, int)
	 *
	 * @param host the host/sender of the message
	 * @param category the CMMsg TYP code ({@link com.planet_ink.coffee_mud.Common.interfaces.CMMsg}
	 * @param msg the actual message to send
	 * @return true if the message was successfully sent
	 */
	public boolean sendGlobalMessage(MOB host, int category, CMMsg msg);

	/* ***********************************************************************/
	/* *							 HELPER CLASSES							 */
	/* ***********************************************************************/


	/**
	 * Helper class for world searches, as it returns both the thing Found,
	 * as well as the room in which it was found.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface LocatedPair
	{
		/**
		 * The room the Thing was found in.
		 * @return the room the Thing was found in.
		 */
		public Room room();

		/**
		 * The thing that was found.
		 * @return the thing that was found.
		 */
		public PhysicalAgent obj();
	}
}
