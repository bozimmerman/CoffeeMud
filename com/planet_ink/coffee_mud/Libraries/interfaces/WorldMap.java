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
	public Room getRoom(Enumeration<Room> roomSet, String calledThis);
	public Room getCachedRoom(final String calledThis);
	public Room getRoomAllHosts(final String calledThis);
	public Enumeration<Room> rooms();
	public Enumeration<Room> roomsFilled();
	public Enumeration<MOB> worldMobs();
	public Enumeration<Item> worldRoomItems();
	public Enumeration<Item> worldEveryItems();
	public Room getRandomRoom();
	public void renameRooms(Area A, String oldName, List<Room> allMyDamnRooms);
	public void obliterateMapRoom(final Room deadRoom);
	public void destroyRoomObject(final Room deadRoom);
	public Room findConnectingRoom(Room room);
	public int getRoomDir(Room from, Room to);
	public int getExitDir(Room from, Exit to);
	public Area getTargetArea(Room from, Exit to);
	public Room getTargetRoom(Room from, Exit to);

	/* ***********************************************************************/
	/* *							 ROOM-AREA-UTILITIES					 */
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
	/* *						WORLD OBJECT INDEXES						 */
	/* ***********************************************************************/
	public void registerWorldObjectLoaded(Area area, Room room, CMObject o);
	public void registerWorldObjectDestroyed(Area area, Room room, CMObject o);

	public Boardable getShip(String calledThis);
	public Boardable findShip(final String s, final boolean exactOnly);
	public Enumeration<Boardable> ships();
	public Enumeration<Room> shipsRoomEnumerator(final Area inA);
	public int numShips();

	public Room getSafeRoomToMovePropertyTo(Room room, PrivateProperty I);

	public Enumeration<LocatedPair> scriptHosts(Area area);

	public MOB deity();
	public Deity getDeity(String calledThis);
	public Enumeration<Deity> deities();

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
