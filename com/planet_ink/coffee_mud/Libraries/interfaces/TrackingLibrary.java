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
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg.CheckedMsgResponse;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.RFilter;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.RFilters;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2024 Bo Zimmerman

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
 * The Tracking and NPC movement library.
 * Contains functionality to map out regions of rooms for finding trails
 * from one room to another, or for searching regions of rooms.  This
 * also contains methods for helping NPCs get around in a RP-friendly
 * way.
 *
 * Another really important aspect of this library is the ability to
 * filter rooms by criteria, in order to achieve trails that will work
 * for players for npcs, and provide efficiency by skipping impassable
 * terrain.
 *
 * @author Bo Zimmerman
 *
 */
public interface TrackingLibrary extends CMLibrary
{
	/**
	 * Returns a trail of rooms to move through in order to go from the given location
	 * to the given destination room.  The trail will have the destination room first,
	 * and the location last.
	 *
	 * @see TrackingLibrary#trackNextDirectionFromHere(List, Room, boolean)
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param location the starting room for the trail
	 * @param destRoom the target room for the trail
	 * @param flags any Radiant flags -- not used in the trail calculation
	 * @param maxRadius maximum radius for the Radiant rooms
	 * @return the trail, or null if a failure
	 */
	public List<Room> findTrailToRoom(Room location, Room destRoom, TrackingFlags flags, int maxRadius);

	/**
	 * Returns a trail of rooms to move through in order to go from the given location
	 * to the given destination room.  The trail will have the destination room first,
	 * and the location last.  Providing radiant rooms is optional, but they should
	 * have the location first and destination last.
	 *
	 * @see TrackingLibrary#trackNextDirectionFromHere(List, Room, boolean)
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param location the starting room for the trail
	 * @param destRoom the target room for the trail
	 * @param flags any Radiant flags -- not used in the trail calculation
	 * @param maxRadius maximum radius for the Radiant rooms
	 * @param radiant optional radiant rooms list
	 * @return the trail, or null if a failure
	 */
	public List<Room> findTrailToRoom(Room location, Room destRoom, TrackingFlags flags, int maxRadius, List<Room> radiant);

	/**
	 * Returns a trail of rooms to move through in order to go from the given location
	 * to one of the given destination rooms.  It will prefer the closest, and prefer
	 * one in the same area as the start room.  It does every search to make sure.
	 * The trail will have the destination room first, and the location last.
	 *
	 * @see TrackingLibrary#trackNextDirectionFromHere(List, Room, boolean)
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param location the starting room for the trail
	 * @param destRooms a list of rooms, any of which may be the destination
	 * @param flags any Radiant flags -- not used in the trail calculation
	 * @param maxRadius maximum radius for the Radiant rooms
	 * @return the trail, or null if a failure
	 */
	public List<Room> findTrailToAnyRoom(Room location, List<Room> destRooms, TrackingFlags flags, int maxRadius);

	/**
	 * Returns a trail of rooms to move through in order to go from the given location
	 * to a room that is not filtered out of the destFilter.  It will prefer the closest, and prefer
	 * one in the same area as the start room.  It does every search to make sure.
	 * The trail will have the destination room first, and the location last.
	 *
	 * @see TrackingLibrary#trackNextDirectionFromHere(List, Room, boolean)
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param location the starting room for the trail
	 * @param destFilter a filter to identify the destination, by filtering OUT
	 * @param flags any Radiant flags -- not used in the trail calculation
	 * @param maxRadius maximum radius for the Radiant rooms
	 * @return the trail, or null if a failure
	 */
	public List<Room> findTrailToAnyRoom(Room location, RFilter destFilter, TrackingFlags flags, int maxRadius);

	/**
	 * Returns every trail found from the starting to the ending room, given a radiantTrail that includes
	 * both.
	 * The trails will have the direction from starting room first.
	 * Providing radiant rooms is required, and they should have the location
	 * first and destination last.
	 *
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param from the starting room
	 * @param to the target room
	 * @param radiantTrail the radiant rooms from the starting room
	 * @return the set of all valid trails, as directions
	 */
	public List<List<Integer>> findAllTrails(Room from, Room to, List<Room> radiantTrail);

	/**
	 * Returns every trail found from the starting to each ending room, given a radiantTrail that includes
	 * all.
	 * The trails will have the direction from starting room first.
	 * Providing radiant rooms is required, and they should have the location
	 * first and destination last.
	 *
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param from the starting room
	 * @param tos the target rooms
	 * @param radiantTrail the radiant rooms from the starting room
	 * @return the set of all valid trails, as directions
	 */
	public List<List<Integer>> findAllTrails(Room from, List<Room> tos, List<Room> radiantTrail);

	/**
	 * Does nothing interesting.  Just returns the shortest list of integers given a list
	 * of lists.
	 *
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param finalSets the shorted list &gt; 0
	 * @return the shortest list.
	 */
	public List<Integer> getShortestTrail(final List<List<Integer>> finalSets);

	/**
	 * Searches for a room fitting to the given search string, within the given
	 * radiant rooms, with a trail fitting the given trail flags, optionally ignoring
	 * any ignore rooms.  Returns a descriptions of the turns and movements from the
	 * start room to the target, or null. Can also have a time limit on finding the
	 * way.
	 *
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#canValidTrail(Room, List, String, int, Set, int)
	 *
	 * @param startR the starting room
	 * @param radiantV the complete radiant rooms
	 * @param where the target to search for
	 * @param trailFlags the flags for rooms to travel through
	 * @param radius the maximum radius to travel
	 * @param ignoreRooms optional set of rooms to ignore in the trail
	 * @param maxSecs maximum seconds to keep looking for trail
	 * @return the description of the way to get there
	 */
	public String getTrailToDescription(Room startR, List<Room> radiantV, String where, Set<TrailFlag> trailFlags, int radius, Set<Room> ignoreRooms, int maxSecs);


	/**
	 * Searches for a room fitting to the given search string, within the given
	 * radiant rooms, with a trail fitting the given trail flags, optionally ignoring
	 * any ignore rooms.  Returns whether there is a trail from the
	 * start room to the target, or not. Can also have a time limit on finding the
	 * way.
	 *
	 * @see TrackingLibrary#findAllTrails(Room, List, List)
	 * @see TrackingLibrary#findAllTrails(Room, Room, List)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 * @see TrackingLibrary#getShortestTrail(List)
	 * @see TrackingLibrary#getTrailToDescription(Room, List, String, Set, int, Set, int)
	 *
	 * @param startR the starting room
	 * @param radiantV the complete radiant rooms
	 * @param where the target to search for
	 * @param radius the maximum radius to travel
	 * @param ignoreRooms optional set of rooms to ignore in the trail
	 * @param maxSecs maximum seconds to keep looking for trail
	 * @return true if you can get from here to there
	 */
	public boolean canValidTrail(Room startR, List<Room> radiantV, String where, int radius, Set<Room> ignoreRooms, int maxSecs);

	/**
	 * Given a room trail, with the destination room first, and the location last, this
	 * will return the next direction to travel from the given location, optionally stopping
	 * at doors.
	 *
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, List, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToAnyRoom(Room, RFilter, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int)
	 * @see TrackingLibrary#findTrailToRoom(Room, Room, TrackingFlags, int, List)
	 *
	 * @param theTrail the room trail
	 * @param location the current room on the trail
	 * @param openOnly true to skip doors
	 * @return the next direction, or -1, or 999 if you have arrived
	 */
	public int trackNextDirectionFromHere(List<Room> theTrail, Room location, boolean openOnly);

	/**
	 * Uninvokes and deletes any Tracking-related effects on the given mob.
	 *
	 * @see TrackingLibrary#autoTrack(MOB, Room)
	 *
	 * @param mob the mob to untrack
	 */
	public void stopTracking(MOB mob);

	/**
	 * Starts the given mob tracking from their current location to the given room.
	 *
	 * @see TrackingLibrary#stopTracking(MOB)
	 *
	 * @param mob the mob to start tracking
	 * @param destR the target room
	 * @return true if tracking was successfully started, false otherwise
	 */
	public boolean autoTrack(MOB mob, Room destR);

	/**
	 * Causes the given mob or item to 'fall' from one room to another,
	 * usually down, if it can.
	 *
	 * @param P the thing to fall
	 * @param room the things location
	 * @param reverseFall true to fall UP instead of down
	 * @return true if falling was started, false otherwise
	 */
	public boolean makeFall(Physical P, Room room, boolean reverseFall);

	/**
	 * If the given mob is riding anything, this will schedule a check
	 * to see if it is STILL riding.  This must be done on the mobs tick
	 * because boats dont tick.
	 *
	 * @param mob the mob to check
	 * @return n/a
	 */
	public boolean doFallenOffCheck(final MOB mob);

	/**
	 * Causes the given mob or item to 'sink' from one room to another,
	 * usually down, if it can.
	 *
	 * @param P the thing to fall
	 * @param room the things location
	 * @param reverseSink true to sink 'up'
	 */
	public void makeSink(Physical P, Room room, boolean reverseSink);

	/**
	 * Utility method for handling movement into a water surface type
	 * room.  It receives the room being moved into or out of, and an event
	 * message to preview.  It returns a Tri-State response:  Either
	 * to approve immediately, fail immediately, or continue with any other checks.
	 *
	 * @param room the watery room an event occurred in
	 * @param msg the event happening in the
	 * @return the state that the caller should respect.
	 */
	public CheckedMsgResponse isOkWaterSurfaceAffect(final Room room, final CMMsg msg);

	/**
	 * Generates an ordered list of rooms radiating from the given room, with max
	 * depth, and optional stopping room, ignore rooms, and flags.
	 * The radiant rooms start with the origin room and get increasingly outward as
	 * you go down the list.
	 *
	 * @see TrackingLibrary#getRadiantAreas(Area, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, RFilters, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, TrackingFlags, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsEnum(Room, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsToTarget(Room, List, TrackingFlags, RFilter, int)
	 * @see TrackingLibrary#getRadiantRoomTarget(Room, RFilters, RFilter)
	 *
	 * @param room the starting room
	 * @param rooms the radiant rooms output
	 * @param flags optional flags to limit the radiant paths
	 * @param radiateTo optional room to stop at, which would be last on the list
	 * @param maxDepth the maximum depth of the radiation
	 * @param ignoreRooms optional rooms to ignore in radiation
	 */
	public void getRadiantRooms(Room room, List<Room> rooms, TrackingFlags flags, Room radiateTo, int maxDepth, Set<Room> ignoreRooms);

	/**
	 * Generates an ordered list of rooms radiating from the given room, with max
	 * depth and optional room radiating filters.
	 * The radiant rooms start with the origin room and get increasingly outward as
	 * you go down the list.
	 *
	 * @see TrackingLibrary#getRadiantAreas(Area, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, TrackingFlags, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, TrackingFlags, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsEnum(Room, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsToTarget(Room, List, TrackingFlags, RFilter, int)
	 * @see TrackingLibrary#getRadiantRoomTarget(Room, RFilters, RFilter)
	 *
	 * @param room the starting room
	 * @param filters one or more filters that blocks various radiation paths
	 * @param maxDepth the maximum depth of the radiation
	 * @return the radiant rooms list
	 */
	public List<Room> getRadiantRooms(final Room room, final RFilters filters, final int maxDepth);

	/**
	 * Generates an ordered list of rooms radiating from the given room, with max
	 * depth, and optional ignore rooms, and radiating room filters.
	 * The radiant rooms start with the origin room and get increasingly outward as
	 * you go down the list.
	 *
	 * @see TrackingLibrary#getRadiantAreas(Area, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, RFilters, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, TrackingFlags, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, TrackingFlags, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsEnum(Room, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsToTarget(Room, List, TrackingFlags, RFilter, int)
	 * @see TrackingLibrary#getRadiantRoomTarget(Room, RFilters, RFilter)
	 *
	 * @param room the starting room
	 * @param rooms the radiant rooms output
	 * @param filters one or more filters that blocks various radiation paths
	 * @param radiateTo optional room to stop at, which would be last on the list
	 * @param maxDepth the maximum depth of the radiation
	 * @param ignoreRooms optional rooms to ignore in radiation
	 */
	public void getRadiantRooms(final Room room, List<Room> rooms, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms);

	/**
	 * Generates an ordered list of rooms radiating from the given room, to a
	 * given target room filter in, with max depth, and optional ignore rooms, and
	 * radiating room filters/flags.
	 * The radiant rooms start with the origin room and get increasingly outward as
	 * you go down the list.
	 *
	 * @see TrackingLibrary#getRadiantAreas(Area, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, RFilters, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, TrackingFlags, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, TrackingFlags, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsEnum(Room, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomTarget(Room, RFilters, RFilter)
	 *
	 * @param room the starting room
	 * @param rooms the radiant rooms output
	 * @param flags one or more filters that blocks various radiation paths
	 * @param radiateTo filter for the room to stop at, which would be last on the list
	 * @param maxDepth the maximum depth of the radiation
	 * @return true if the radiation was successful, false otherwise
	 */
	public boolean getRadiantRoomsToTarget(final Room room, final List<Room> rooms, TrackingFlags flags, final RFilter radiateTo, final int maxDepth);

	/**
	 * Generates an ordered list of rooms radiating from the given room, with max
	 * depth and optional room radiating filters.
	 * The radiant rooms start with the origin room and get increasingly outward as
	 * you go down the list.
	 *
	 * @see TrackingLibrary#getRadiantAreas(Area, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, RFilters, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, TrackingFlags, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsEnum(Room, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsToTarget(Room, List, TrackingFlags, RFilter, int)
	 * @see TrackingLibrary#getRadiantRoomTarget(Room, RFilters, RFilter)
	 *
	 * @param room the starting room
	 * @param flags one or more filters that blocks various radiation paths
	 * @param maxDepth the maximum depth of the radiation
	 * @return the radiant rooms output
	 */
	public List<Room> getRadiantRooms(Room room, TrackingFlags flags, int maxDepth);

	/**
	 * Generates an ordered list of areas radiating from the given area, with max
	 * depth.
	 * The radiant areas start with the origin area and get increasingly outward as
	 * you go down the list.
	 *
	 * @see TrackingLibrary#getRadiantRooms(Room, RFilters, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, TrackingFlags, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, TrackingFlags, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsEnum(Room, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsToTarget(Room, List, TrackingFlags, RFilter, int)
	 * @see TrackingLibrary#getRadiantRoomTarget(Room, RFilters, RFilter)
	 *
	 * @param area the starting area
	 * @param maxDepth the maximum depth of the radiation
	 * @return the radiant areas output
	 */
	public List<Area> getRadiantAreas(Area area, int maxDepth);

	/**
	 * Generates a enumerator for an ordered list of rooms radiating from
	 * the given room, with max depth, and optional ignore rooms, and
	 * radiating room filters.
	 * The radiant rooms start with the origin room and get increasingly outward as
	 * you go down the list.
	 *
	 * @see TrackingLibrary#getRadiantAreas(Area, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, RFilters, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, TrackingFlags, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, TrackingFlags, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsToTarget(Room, List, TrackingFlags, RFilter, int)
	 * @see TrackingLibrary#getRadiantRoomTarget(Room, RFilters, RFilter)
	 *
	 * @param room the starting room
	 * @param filters one or more filters that blocks various radiation paths
	 * @param radiateTo optional room to stop at, which would be last on the list
	 * @param maxDepth the maximum depth of the radiation
	 * @param ignoreRooms optional rooms to ignore in radiation
	 * @return the enumerator of radiating rooms
	 */
	public Enumeration<Room> getRadiantRoomsEnum(final Room room, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms);

	/**
	 * Returns a room that is not filtered out of the rooms radiating from the given room.
	 * May also include optional filters for the radiating room path.  No max depth, so
	 * this will just continue until it runs through the entire connected map!
	 *
	 * @see TrackingLibrary#getRadiantAreas(Area, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, RFilters, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, TrackingFlags, int)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRooms(Room, List, TrackingFlags, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsEnum(Room, RFilters, Room, int, Set)
	 * @see TrackingLibrary#getRadiantRoomsToTarget(Room, List, TrackingFlags, RFilter, int)
	 *
	 * @param room the starting room
	 * @param filters one or more filters that blocks various radiation paths
	 * @param radiateTo filter that the returned room is NOT filtered out of
	 * @return null, or the found room
	 */
	public Room getRadiantRoomTarget(final Room room, final RFilters filters, final RFilter radiateTo);

	/**
	 * Builts a radiating room list that is assumed to be in grid formation, and
	 * owned as private property.  The list of rooms is returned with their
	 * relative grid position to the starting room.  Only rooms owned by the
	 * given owner, if given, are returned.
	 *
	 * @see TrackingLibrary#getCalculatedAdjacentGridRoom(PairVector, Room, int)
	 *
	 * @param room the starting room
	 * @param ownerName "", or the owner of the rooms to return
	 * @param maxDepth the max depth to radiate
	 * @return the list of rooms and grid information
	 */
	public PairVector<Room,int[]> buildGridList(Room room, String ownerName, int maxDepth);

	/**
	 * Forces the given mob to leave the room, for aesthetic purposes.
	 *
	 * @see TrackingLibrary#wanderAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderIn(MOB, Room)
	 * @see TrackingLibrary#markToWanderHomeLater(MOB, int)
	 * @see TrackingLibrary#forceRecall(MOB, boolean)
	 *
	 * @param M the mob to leave
	 * @param mindPCs true to NOT leave if pcs are present
	 * @param andGoHome true to be transported to start room
	 * @return true if this was successful, and false otherwise
	 */
	public boolean wanderCheckedAway(MOB M, boolean mindPCs, boolean andGoHome);

	/**
	 * Aesthetically transports a mob from their location to
	 * a target room by having them leave their current room
	 * legitimately, and enter the target one.  Will optionally
	 * abort if PCs are present.
	 *
	 * @see TrackingLibrary#wanderAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderIn(MOB, Room)
	 * @see TrackingLibrary#markToWanderHomeLater(MOB, int)
	 * @see TrackingLibrary#forceRecall(MOB, boolean)
	 *
	 * @param M the mob to move
	 * @param toHere the target room
	 * @param mindPCs true to not leave if pcs are present
	 * @return true if the movement was successful
	 */
	public boolean wanderCheckedFromTo(MOB M, Room toHere, boolean mindPCs);

	/**
	 * Forces the given mob to leave the room, for aesthetic purposes.
	 * Always aborts if mob is a mount with a rider nearby.  Optionally
	 * aborts if pcs are present, and optionally does a legit re-enter
	 * of their start room.
	 *
	 * @see TrackingLibrary#wanderCheckedAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderIn(MOB, Room)
	 * @see TrackingLibrary#markToWanderHomeLater(MOB, int)
	 * @see TrackingLibrary#forceRecall(MOB, boolean)
	 *
	 * @param M the mob to move
	 * @param mindPCs true to not leave if pcs are present
	 * @param andGoHome true to also aesthetically go back to start room
	 */
	public void wanderAway(MOB M, boolean mindPCs, boolean andGoHome);

	/**
	 * Aesthetically transports a mob from their location to
	 * a target room by having them leave their current room
	 * legitimately, and enter the target one.
	 *
	 * @see TrackingLibrary#wanderAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderIn(MOB, Room)
	 * @see TrackingLibrary#markToWanderHomeLater(MOB, int)
	 * @see TrackingLibrary#forceRecall(MOB, boolean)
	 *
	 * @param M the mob to move
	 * @param toHere the destination room
	 * @param mindPCs true to not leave if pcs are present
	 */
	public void wanderFromTo(MOB M, Room toHere, boolean mindPCs);

	/**
	 * Aesthetically transports a mob from their location to
	 * a target room by having them leave their current room
	 * legitimately, and enter the target one.  It will force
	 * them to enter if necessary.
	 *
	 * @see TrackingLibrary#wanderAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#markToWanderHomeLater(MOB, int)
	 * @see TrackingLibrary#forceRecall(MOB, boolean)
	 *
	 * @param M the mob to move
	 * @param toHere the destination room
	 */
	public void wanderIn(MOB M, Room toHere);

	/**
	 * Attempts to recall the given mob, and optinally all
	 * their followers, by generating a recall message and
	 * previewing and sending it.
	 *
	 * @see TrackingLibrary#wanderAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderIn(MOB, Room)
	 * @see TrackingLibrary#markToWanderHomeLater(MOB, int)
	 *
	 * @param mob the mob to move
	 * @param includeFollowers true to also recall followers
	 */
	public void forceRecall(final MOB mob, boolean includeFollowers);

	/**
	 * Marks the mob with a temporary behavior that will force
	 * the mob to wander back to their start room as soon
	 * as possible.
	 *
	 * @see TrackingLibrary#wanderAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedAway(MOB, boolean, boolean)
	 * @see TrackingLibrary#wanderCheckedFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderFromTo(MOB, Room, boolean)
	 * @see TrackingLibrary#wanderIn(MOB, Room)
	 * @see TrackingLibrary#forceRecall(MOB, boolean)
	 *
	 * @param M the mob to move
	 * @param ticks the number of ticks to wait, or 0
	 */
	public void markToWanderHomeLater(MOB M, final int ticks);

	/**
	 * The random mobility method, for controlling movement of a move from one room to an ajacent one.
	 * Will swim if they can swim, crawl if crawling needed, climb if possible, etc.
	 *
	 * @see TrackingLibrary#walk(Item, int)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean, boolean)
	 * @see TrackingLibrary#walkForced(MOB, Room, Room, boolean, boolean, String)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean, boolean)
	 *
	 * @param mob the mob who needs to be moved
	 * @param dooropen true attempt to open any closed doors, unless its private property
	 * @param wander true to cross area boundaries
	 * @param roomprefer only take a room on the optional list
	 * @param roomobject take any room UNLES it is on the optional list
	 * @param status optional status tracker for debugging
	 * @param rooms optional set of rooms for prefer or object
	 * @return true if the movement was successful
	 */
	public boolean beMobile(MOB mob, boolean dooropen, boolean wander, boolean roomprefer, boolean roomobject, int[] status, Set<Room> rooms);

	/**
	 * Causes the given mob to attempt to walk in the given direction.
	 *
	 * @see TrackingLibrary#beMobile(MOB, boolean, boolean, boolean, boolean, int[], Set)
	 * @see TrackingLibrary#walk(Item, int)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean, boolean)
	 * @see TrackingLibrary#walkForced(MOB, Room, Room, boolean, boolean, String)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean, boolean)
	 *
	 * @param mob the mob who needs to move
	 * @param directionCode the direction to walk
	 * @param flee true to generate a FLEE from combat message
	 * @param nolook true to avoid looking around when entering the next room
	 * @return true if the movement was successful
	 */
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook);

	/**
	 *
	 * Causes the given mob to attempt to walk in the given direction.
	 *
	 * @see TrackingLibrary#beMobile(MOB, boolean, boolean, boolean, boolean, int[], Set)
	 * @see TrackingLibrary#walk(Item, int)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean, boolean)
	 * @see TrackingLibrary#walkForced(MOB, Room, Room, boolean, boolean, String)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean, boolean)
	 *
	 * @param mob the mob who needs to move
	 * @param directionCode the direction to walk
	 * @param flee true to generate a FLEE from combat message
	 * @param nolook true to avoid looking around when entering the next room
	 * @param noriders true to prevent rideables being involved
	 * @return true if the movement was successful
	 */
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);

	/**
	 * Causes the given mob to attempt to walk in the given direction.
	 *
	 * @see TrackingLibrary#beMobile(MOB, boolean, boolean, boolean, boolean, int[], Set)
	 * @see TrackingLibrary#walk(Item, int)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#walkForced(MOB, Room, Room, boolean, boolean, String)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean, boolean)
	 *
	 * @param mob the mob who needs to move
	 * @param directionCode the direction to walk
	 * @param flee true to generate a FLEE from combat message
	 * @param nolook true to avoid looking around when entering the next room
	 * @param noriders true to prevent rideables being involved
	 * @param always true to set the always message flag, preventing preventative checks
	 * @return true if the movement was successful
	 */
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);

	/**
	 * Forces the given mob to leave the from room and enter to the room, without
	 * preview.
	 *
	 * @see TrackingLibrary#beMobile(MOB, boolean, boolean, boolean, boolean, int[], Set)
	 * @see TrackingLibrary#walk(Item, int)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean, boolean)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean, boolean)
	 *
	 * @param M the mob who needs to move
	 * @param fromHere the room left
	 * @param toHere the room entered
	 * @param andFollowers true to include followers
	 * @param forceLook true to force the mob to look around after entering the new room
	 * @param msgStr the entering message string
	 */
	public void walkForced(MOB M, Room fromHere, Room toHere, boolean andFollowers, boolean forceLook, String msgStr);

	/**
	 * Narrates the movement of an item, usually a rideable, in a given direction, from its current
	 * location.  Will move as a rideable with riders if possible.
	 *
	 * @see TrackingLibrary#beMobile(MOB, boolean, boolean, boolean, boolean, int[], Set)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean, boolean)
	 * @see TrackingLibrary#walkForced(MOB, Room, Room, boolean, boolean, String)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean, boolean)
	 *
	 * @param I the item to move
	 * @param directionCode the direction to walk
	 * @return true if the movement was successful
	 */
	public boolean walk(Item I, int directionCode);

	/**
	 * Causes the given mob to attempt to run in the given direction.
	 *
	 * @see TrackingLibrary#beMobile(MOB, boolean, boolean, boolean, boolean, int[], Set)
	 * @see TrackingLibrary#walk(Item, int)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean, boolean)
	 * @see TrackingLibrary#walkForced(MOB, Room, Room, boolean, boolean, String)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean, boolean)
	 *
	 * @param mob the mob who needs to move
	 * @param directionCode the direction to walk
	 * @param flee true to generate a FLEE from combat message
	 * @param nolook true to avoid looking around when entering the next room
	 * @param noriders true to prevent rideables being involved
	 * @return true if the movement was successful
	 */
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);

	/**
	 * Causes the given mob to attempt to run in the given direction.
	 *
	 * @see TrackingLibrary#beMobile(MOB, boolean, boolean, boolean, boolean, int[], Set)
	 * @see TrackingLibrary#walk(Item, int)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean)
	 * @see TrackingLibrary#walk(MOB, int, boolean, boolean, boolean, boolean)
	 * @see TrackingLibrary#walkForced(MOB, Room, Room, boolean, boolean, String)
	 * @see TrackingLibrary#run(MOB, int, boolean, boolean, boolean)
	 *
	 * @param mob the mob who needs to move
	 * @param directionCode the direction to walk
	 * @param flee true to generate a FLEE from combat message
	 * @param nolook true to avoid looking around when entering the next room
	 * @param noriders true to prevent rideables being involved
	 * @param always true to set the always message flag, preventing preventative checks
	 * @return true if the movement was successful
	 */
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);

	/**
	 * This method is helpful for traversing room trails.
	 * If the given room is higher in the given room list than a
	 * connecting room, it returns -1.  If a room that connects
	 * to this one is found, the direction from the given room
	 * to that one is returned.
	 *
	 * @param room the source room
	 * @param rooms the list of rooms
	 * @return the direction from the room to the lowest room in the list
	 */
	public int radiatesFromDir(Room room, List<Room> rooms);

	/**
	 * Returns true if the two give mobs are in the same room,
	 * or adjacent rooms.
	 *
	 * @param whichM the first mob
	 * @param nearM the second mob
	 * @return true if they are near each other, false otherwise
	 */
	public boolean areNearEachOther(final MOB whichM, final MOB nearM);

	/**
	 * Given a set of rooms built with grid coordinates, and a starting
	 * room inside that grid, and a direction, this will return the
	 * adjacent room based on the built grid.
	 *
	 * @see TrackingLibrary#buildGridList(Room, String, int)
	 *
	 * @param rooms the built grid array of rooms and locations
	 * @param R the room to start from
	 * @param dir the direction to inquire about
	 * @return the room in that direction, or null
	 */
	public Room getCalculatedAdjacentGridRoom(PairVector<Room,int[]> rooms, Room R, int dir);

	/**
	 * Given a mob with eyes, and their location, and a match string,
	 * this will attempt to match one of the exits and return it.
	 * It does not check direction codes, it must be an exit name-like
	 * match.
	 *
	 * @param mob the mob who needs to move
	 * @param R the room to find exits in
	 * @param desc the exit match string
	 * @return null, or the exit direction
	 */
	public int findExitDir(MOB mob, Room R, String desc);

	/**
	 * Given a mob and a room, returns the direction from
	 * the mobs direction to the target room, if possible.
	 *
	 * @param mob the mob
	 * @param R the target room
	 * @return the direction or -1
	 */
	public int findRoomDir(MOB mob, Room R);

	/**
	 * Based on radiating rooms from the given room, this
	 * will return the nearest room that has a valid room id.
	 *
	 * @param R the room that is prob not valid
	 * @return the nearest valid room
	 */
	public Room getNearestValidIDRoom(final Room R);

	/**
	 * Constructs a new set of TrackingFlags, which are filters for
	 * use in radiant and room finding methods.  These must then
	 * be filled with TrackingFlag (RFilter) objects.
	 *
	 * @return the new tracking flags set
	 */
	public TrackingFlags newFlags();

	/**
	 * If theres a ladder that can be seen in the
	 * same room as the given mob, this returns it.
	 *
	 * @see TrackingLibrary#postMountLadder(MOB, Rideable)
	 *
	 * @param mob the mob who wants a ladder
	 * @param room the room that might have a ladder in it
	 * @return null, or the ladder
	 */
	public Rideable findALadder(MOB mob, Room room);

	/**
	 * Causes the given mob to mount the given ladder.
	 * So they can ride it up, presumably.
	 *
	 * @see TrackingLibrary#findALadder(MOB, Room)
	 *
	 * @param mob the mob who wants to use a ladder
	 * @param ladder the ladder
	 */
	public void postMountLadder(MOB mob, Rideable ladder);

	/**
	 * When large sailing ships or caravans navigate from room to room,
	 * a fake temporary mob is used to represent the agency of the ship
	 * per se.  This will create that mob.  The mob MUST be destroyed
	 * after use.
	 *
	 * @param ship the navigable ship
	 * @return the mob to use, then destroy
	 */
	public MOB createNavigationMob(final NavigableItem ship);

	/**
	 * A filtering interface for rooms, or for moving from a host room
	 * to a target room.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface RFilter
	{
		/**
		 * The room in question is always the second one, "R".
		 * The hostR is a room being travelled from, and is optional.
		 * The exit and direction are the travelling means, if any.
		 *
		 * @param hostR the starting room
		 * @param R the room being filtered, and the target room
		 * @param E the exit from the host to target room
		 * @param dir the direction from host to target room
		 * @return true if the room is to be skipped, or false otherwise
		 */
		public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir);
	}

	/**
	 * A filtering interface for rooms, or for moving from a host room
	 * to a target room.  Consists of one or more smaller filters,
	 * all checked in sequence.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface RFilters
	{
		/**
		 * The room in question is always the second one, "R".
		 * The hostR is a room being travelled from, and is optional.
		 * The exit and direction are the travelling means, if any.
		 *
		 * @param hostR the starting room
		 * @param R the room being filtered, and the target room
		 * @param E the exit from the host to target room
		 * @param dir the direction from host to target room
		 * @return true if the room is to be skipped, or false otherwise
		 */
		public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir);

		/**
		 * Add a new filter to this filter set.
		 *
		 * @param filter the new filter
		 * @return the filter set
		 */
		public RFilters plus(RFilter filter);

		/**
		 * Del a filter from this filter set.
		 *
		 * @param filter the old filter
		 * @return the filter set
		 */
		public RFilters minus(RFilter filter);

		/**
		 * Make a copy of this filter.
		 *
		 * @return the filter set
		 */
		public RFilters copyOf();
	}

	/**
	 * A collection of tracking flags, which are really just RFilter
	 * objects wrappers.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface TrackingFlags extends Set<TrackingFlag>
	{
		/**
		 * Adds a new trackingflag
		 *
		 * @param flag the flag to add
		 * @return the trackingflags collection
		 */
		public TrackingFlags plus(TrackingFlag flag);

		/**
		 * Adds new trackingflags
		 *
		 * @param flags the flags to add
		 * @return the trackingflags collection
		 */
		public TrackingFlags plus(TrackingFlags flags);

		/**
		 * Removes a new trackingflag
		 *
		 * @param flag the flag to del
		 * @return the trackingflags collection
		 */
		public TrackingFlags minus(TrackingFlag flag);

		/**
		 * A copy of the tracking flags collection
		 * @return the tracking flags collection
		 */
		public TrackingFlags copyOf();
	}

	/**
	 * Trail flags when building a description of
	 * trail description.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum TrailFlag
	{
		CONFIRM,
		AREANAMES
	}

	/**
	 * A Tracking Flag is a form of RFiler that is an enum that
	 * includes various pre-configured rfilters used
	 * throughout the engine.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum TrackingFlag implements RFilter
	{
		NOHOMES,
		OPENONLY,
		UNLOCKEDONLY,
		PASSABLE,
		AREAONLY,
		NOTHINAREAS,
		NOHIDDENAREAS,
		NOEMPTYGRIDS,
		NOAIR,
		NOPRIVATEPROPERTY,
		NOWATER,
		WATERSURFACEONLY,
		WATERSURFACEORSHOREONLY,
		SHOREONLY,
		UNDERWATERONLY,
		FLOORSONLY,
		CEILINGSSONLY,
		NOCLIMB,
		NOCRAWL,
		OUTDOORONLY,
		INDOORONLY,
		DRIVEABLEONLY
		;
		public RFilter myFilter=null;

		@Override
		public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
		{
			if(myFilter == null)
				return false;
			return myFilter.isFilteredOut(hostR, R, E, dir);
		}
	}
}
