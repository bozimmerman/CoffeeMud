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
public interface TrackingLibrary extends CMLibrary
{
	public List<Room> findTrailToRoom(Room location, Room destRoom, TrackingFlags flags, int maxRadius);
	public List<Room> findTrailToRoom(Room location, Room destRoom, TrackingFlags flags, int maxRadius, List<Room> radiant);
	public List<Room> findTrailToAnyRoom(Room location, List<Room> destRooms, TrackingFlags flags, int maxRadius);
	public List<Room> findTrailToAnyRoom(Room location, RFilter destFilter, TrackingFlags flags, int maxRadius);
	public List<Integer> getShortestTrail(final List<List<Integer>> finalSets);
	public List<List<Integer>> findAllTrails(Room from, Room to, List<Room> radiantTrail);
	public List<List<Integer>> findAllTrails(Room from, List<Room> tos, List<Room> radiantTrail);
	public String getTrailToDescription(Room R1, List<Room> set, String where, Set<TrailFlag> trailFlags, int radius, Set<Room> ignoreRooms, int maxMins);

	public int trackNextDirectionFromHere(List<Room> theTrail, Room location, boolean openOnly);
	public void stopTracking(MOB mob);
	public boolean autoTrack(MOB mob, Room destR);

	public boolean makeFall(Physical P, Room room, boolean reverseFall);
	public void makeSink(Physical P, Room room, boolean reverseSink);
	public CheckedMsgResponse isOkWaterSurfaceAffect(final Room room, final CMMsg msg);

	public int radiatesFromDir(Room room, List<Room> rooms);
	public boolean areNearEachOther(final MOB whichM, final MOB nearM);

	public void getRadiantRooms(Room room, List<Room> rooms, TrackingFlags flags, Room radiateTo, int maxDepth, Set<Room> ignoreRooms);
	public List<Room> getRadiantRooms(final Room room, final RFilters filters, final int maxDepth);
	public void getRadiantRooms(final Room room, List<Room> rooms, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms);
	public boolean getRadiantRoomsToTarget(final Room room, final List<Room> rooms, TrackingFlags flags, final RFilter radiateTo, final int maxDepth);
	public List<Room> getRadiantRooms(Room room, TrackingFlags flags, int maxDepth);
	public List<Area> getRadiantAreas(Area area, int maxDepth);
	public Enumeration<Room> getRadiantRoomsEnum(final Room room, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms);
	public Room getRadiantRoomTarget(final Room room, final RFilters filters, final RFilter radiateTo);

	public boolean wanderCheckedAway(MOB M, boolean mindPCs, boolean andGoHome);
	public boolean wanderCheckedFromTo(MOB M, Room toHere, boolean mindPCs);
	public void wanderAway(MOB M, boolean mindPCs, boolean andGoHome);
	public void wanderFromTo(MOB M, Room toHere, boolean mindPCs);
	public void wanderIn(MOB M, Room toHere);

	public boolean beMobile(MOB mob, boolean dooropen, boolean wander, boolean roomprefer, boolean roomobject, int[] status, Set<Room> rooms);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);
	public void walkForced(MOB M, Room fromHere, Room toHere, boolean andFollowers, boolean forceLook, String msg);
	public boolean walk(Item I, int directionCode);
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);

	public void forceRecall(final MOB mob, boolean includeFollowers);
	public void forceEntry(MOB M, Room toHere, boolean andFollowers, boolean forceLook, String msg);
	public boolean doFallenOffCheck(final MOB mob);
	public int findExitDir(MOB mob, Room R, String desc);
	public int findRoomDir(MOB mob, Room R);
	public Room getNearestValidIDRoom(final Room R);
	public boolean isAnAdminHere(Room R, boolean sysMsgsOnly);
	public Set<Physical> getAllGroupRiders(final Physical P, final Room hereOnlyR);
	public void markToWanderHomeLater(MOB M);
	public boolean canValidTrail(Room R1, List<Room> set, String where, int radius, Set<Room> ignoreRooms, int maxMins);

	public Rideable findALadder(MOB mob, Room room);
	public void postMountLadder(MOB mob, Rideable ladder);

	public TrackingFlags newFlags();
	public Room getCalculatedAdjacentRoom(PairVector<Room,int[]> rooms, Room R, int dir);
	public PairVector<Room,int[]> buildGridList(Room room, String ownerName, int maxDepth);

	public static interface RFilter
	{
		public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir);
	}

	public static interface RFilters
	{
		public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir);

		public RFilters plus(RFilter filter);

		public RFilters minus(RFilter filter);

		public RFilters copyOf();
	}

	public static interface TrackingFlags extends Set<TrackingFlag>
	{
		public TrackingFlags plus(TrackingFlag flag);

		public TrackingFlags plus(TrackingFlags flags);

		public TrackingFlags minus(TrackingFlag flag);

		public TrackingFlags copyOf();
	}

	public static enum TrailFlag
	{
		CONFIRM,
		AREANAMES
	}

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
		INDOORONLY
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
