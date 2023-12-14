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
/**
 * Utility library for global map searches.
 *
 * @author Bo Zimmerman
 *
 */
public interface WorldHuntLibrary extends CMLibrary
{
	/**
	 * Searches the whole world's rooms liberally and returns all rooms with a match.
	 * Liberal codes are: wE)ak areas, strict A)areas, R)rooms, P)layers, room I)tems,
	 * M)obs, inV)entory,  shop stocK)s.
	 *
	 * @param mob the mob whose room access to respect
	 * @param cmd the search string
	 * @param srchWhatAERIPMVK the librarl codes for what to search
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @param maxMillis maximum amount of time to search before aborting
	 * @return the matching rooms
	 */
	public List<Room> findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);

	/**
	 * Searches the whole world's rooms liberally and returns the first room with a match.
	 * Liberal codes are: wE)ak areas, strict A)areas, R)rooms, P)layers, room I)tems,
	 * M)obs, inV)entory,  shop stocK)s.
	 *
	 * @param mob the mob whose room access to respect
	 * @param cmd the search string
	 * @param srchWhatAERIPMVK the librarl codes for what to search
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @param maxMillis maximum amount of time to search before aborting
	 * @return the first matching room
	 */
	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);

	/**
	 * Given an area, searches the rooms liberally and returns all rooms with a match.
	 * Liberal codes are: wE)ak areas, strict A)areas, R)rooms, P)layers, room I)tems,
	 * M)obs, inV)entory,  shop stocK)s.
	 *
	 * @param mob the mob whose room access to respect
	 * @param A the area to search
	 * @param cmd the search string
	 * @param srchWhatAERIPMVK the librarl codes for what to search
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the first matching rooms
	 */
	public List<Room> findAreaRoomsLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);

	/**
	 * Given an area, searches the rooms liberally and returns the first room with a match.
	 * Liberal codes are: wE)ak areas, strict A)areas, R)rooms, P)layers, room I)tems,
	 * M)obs, inV)entory,  shop stocK)s.
	 *
	 * @param mob the mob whose room access to respect
	 * @param A the area to search
	 * @param cmd the search string
	 * @param srchWhatAERIPMVK the librarl codes for what to search
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the first matching room
	 */
	public Room findAreaRoomLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);

	/**
	 * Finds all the matched rooms in the rooms in the given room
	 * enumerator.
	 *
	 * @param rooms the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param displayOnly true to search only display, and skip description
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the found room rooms
	 */
	public List<Room> findRooms(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);

	/**
	 * Finds the first matched room in the rooms of the given room
	 * enumerator.
	 *
	 * @param rooms the rooms to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param displayOnly true to search only display, and skip description
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return null, or the first room
	 */
	public Room findFirstRoom(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);

	/**
	 * Finds the first matched mob in the rooms of the given room
	 * enumerator.
	 *
	 * @param rooms the rooms to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return null, or the first room mob
	 */
	public MOB findFirstInhabitant(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Finds the matched mobs in the rooms in the given room
	 * enumerator.  If any exact matches are found, those are
	 * returned.  Otherwise, the slightly looser matches are
	 * all returned.
	 *
	 * @param rooms the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param returnFirst true to return only 1 match, false for all
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the found room mobs
	 */
	public List<MOB> findInhabitantsFavorExact(Enumeration<Room> rooms, MOB mob, String srchStr, boolean returnFirst, int timePct);

	/**
	 * Finds all the matched mobs in the rooms in the given room
	 * enumerator.
	 *
	 * @param rooms the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the found room mobs
	 */
	public List<MOB> findInhabitants(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Finds all the matched items in the rooms in the given room
	 * enumerator.
	 *
	 * @param rooms the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param anyItems true to include container searches, false for non-contained only
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the found room items
	 */
	public List<Item> findRoomItems(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);

	/**
	 * Finds the first matched item in the rooms of the given room
	 * enumerator.
	 *
	 * @param rooms the rooms to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param anyItems true to include container searches, false for non-contained only
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return null, or the first room item
	 */
	public Item findFirstRoomItem(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);

	/**
	 * Finds all the matched stock item in the shops of shopkeepeers in the given room
	 * enumerator.
	 *
	 * @param rooms the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the found stock items
	 */
	public List<Environmental> findShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Finds the first matched stock item in the shops of shopkeepeers in the given room
	 * enumerator.
	 *
	 * @param rooms the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return null, or the first stock item
	 */
	public Environmental findFirstShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Finds all shopkeepers from the given room enumerator and given mob room accessor who
	 * has a shop stock item matching the given hunt search string.
	 *
	 * @param rooms the rooms to iterate through
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the list of shopkeepers
	 */
	public List<Environmental> findShopStockers(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Finds the first shopkeeper from the given room enumerator and given mob room accessor who
	 * has a shop stock item matching the given hunt search string.
	 *
	 * @param rooms the rooms to iterate through
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return the first shopkeeper
	 */
	public Environmental findFirstShopStocker(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Finds all matched items in the inventory of mobs in the given room
	 * enumerator, or in the inventory of players if null rooms are given.
	 *
	 * @param rooms null for players, or the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return null, or the found items
	 */
	public List<Item> findInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Finds the first matched item in the inventory of mobs in the given room
	 * enumerator, or in the inventory of players if null rooms are given.
	 *
	 * @param rooms null for players, or the rooms with mobs to search
	 * @param mob the mob whose room access to confirm
	 * @param srchStr the search string, using full world hunt rules
	 * @param timePct % of a second to keep searching between rooms, 100% is full time
	 * @return null, or the first found item
	 */
	public Item findFirstInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);

	/**
	 * Returns whether the given object is in the given area.
	 *
	 * @see WorldHuntLibrary#isHere(CMObject, Room)
	 *
	 * @param E2 the object to look for
	 * @param here the room to see if its in
	 * @return true if the object is in the area
	 */
	public boolean isHere(CMObject E2, Area here);

	/**
	 * Returns whether the given object is in the given room.
	 *
	 * @see WorldHuntLibrary#isHere(CMObject, Area)
	 *
	 * @param E2 the object to look for
	 * @param here the room to see if its in
	 * @return true if the object is in the room
	 */
	public boolean isHere(CMObject E2, Room here);

	/**
	 * Returns whether an authorized room or mob editor
	 * is in the room, optionally with sysmsgs turned on.
	 *
	 * @param R the room to check
	 * @param sysMsgsOnly true to return true only if sysmsgs are on
	 * @return true if admin mode is in effect for the room
	 */
	public boolean isAnAdminHere(Room R, boolean sysMsgsOnly);

	/**
	 * Gets the entire party -- group members, ridden things,
	 * followed and things riding them, etc, etc.
	 *
	 * @param P the starting point for the group
	 * @param hereOnlyR null, or the room they must all ne present in
	 * @return the entire happy family
	 */
	public Set<Physical> getAllGroupRiders(final Physical P, final Room hereOnlyR);
}
