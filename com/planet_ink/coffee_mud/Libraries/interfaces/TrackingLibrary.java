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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
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
public interface TrackingLibrary extends CMLibrary
{
	public List<Room> findTrailToRoom(Room location, Room destRoom, TrackingFlags flags, int maxRadius);
	public List<Room> findTrailToRoom(Room location, Room destRoom, TrackingFlags flags, int maxRadius, List<Room> radiant);
	public List<Room> findTrailToAnyRoom(Room location, List<Room> destRooms, TrackingFlags flags, int maxRadius);
	public List<Room> findTrailToAnyRoom(Room location, RFilter destFilter, TrackingFlags flags, int maxRadius);
	public int trackNextDirectionFromHere(List<Room> theTrail, Room location, boolean openOnly);
	public void stopTracking(MOB mob);
	public boolean makeFall(Physical P, Room room, boolean reverseFall);
	public void makeSink(Physical P, Room room, boolean reverseSink);
	public CheckedMsgResponse isOkWaterSurfaceAffect(final Room room, final CMMsg msg);
	public int radiatesFromDir(Room room, List<Room> rooms);
	public void getRadiantRooms(Room room, List<Room> rooms, TrackingFlags flags, Room radiateTo, int maxDepth, Set<Room> ignoreRooms);
	public List<Room> getRadiantRooms(final Room room, final RFilters filters, final int maxDepth);
	public void getRadiantRooms(final Room room, List<Room> rooms, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms);
	public List<Room> getRadiantRooms(Room room, TrackingFlags flags, int maxDepth);
	public Room getRadiantRoomTarget(final Room room, final RFilters filters, final RFilter radiateTo);
	public boolean beMobile(MOB mob, boolean dooropen, boolean wander, boolean roomprefer, boolean roomobject, int[] status, Set<Room> rooms);
	public boolean wanderCheckedAway(MOB M, boolean mindPCs, boolean andGoHome);
	public boolean wanderCheckedFromTo(MOB M, Room toHere, boolean mindPCs);
	public void wanderAway(MOB M, boolean mindPCs, boolean andGoHome);
	public void wanderFromTo(MOB M, Room toHere, boolean mindPCs);
	public void wanderIn(MOB M, Room toHere);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);
	public boolean walk(Item I, int directionCode);
	public void forceRecall(final MOB mob, boolean includeFollowers);
	public void forceEntry(MOB M, Room toHere, boolean andFollowers, boolean forceLook, String msg);
	public void walkForced(MOB M, Room fromHere, Room toHere, boolean andFollowers, boolean forceLook, String msg);
	public int findExitDir(MOB mob, Room R, String desc);
	public int findRoomDir(MOB mob, Room R);
	public boolean isAnAdminHere(Room R, boolean sysMsgsOnly);
	public void markToWanderHomeLater(MOB M);
	public List<Integer> getShortestTrail(final List<List<Integer>> finalSets);
	public List<List<Integer>> findAllTrails(Room from, Room to, List<Room> radiantTrail);
	public List<List<Integer>> findAllTrails(Room from, List<Room> tos, List<Room> radiantTrail);
	public boolean canValidTrail(Room R1, List<Room> set, String where, int radius, Set<Room> ignoreRooms, int maxMins);
	public String getTrailToDescription(Room R1, List<Room> set, String where, boolean areaNames, boolean confirm, int radius, Set<Room> ignoreRooms, int maxMins);
	public Rideable findALadder(MOB mob, Room room);
	public void postMountLadder(MOB mob, Rideable ladder);
	public TrackingFlags newFlags();
	public Room getCalculatedAdjacentRoom(PairVector<Room,int[]> rooms, Room R, int dir);
	public PairVector<Room,int[]> buildGridList(Room room, String ownerName, int maxDepth);
	public boolean autoTrack(MOB mob, Room destR);

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

	public static enum TrackingFlag
	{
		NOHOMES(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return CMLib.law().getLandTitle(R) != null;
			}
		}),
		OPENONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return !E.isOpen();
			}
		}),
		UNLOCKEDONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return E.hasALock();
			}
		}),
		AREAONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (R!=null)&&(hostR!=null)&&(hostR.getArea()!=R.getArea());
			}
		}),
		NOHIDDENAREAS(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return ((R!=null)&&(CMLib.flags().isHidden(R))) 
					|| ((R!=null)&&(R.getArea()!=null)&&(CMLib.flags().isHidden(R.getArea())));
			}
		}),
		NOEMPTYGRIDS(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (R.getGridParent() != null) && (R.getGridParent().roomID().length() == 0);
			}
		}),
		NOAIR(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (R.domainType() == Room.DOMAIN_INDOORS_AIR) || (R.domainType() == Room.DOMAIN_OUTDOORS_AIR);
			}
		}),
		NOWATER(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return CMLib.flags().isWateryRoom(R);
			}
		}),
		WATERSURFACEONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return !CMLib.flags().isWaterySurfaceRoom(R);
			}
		}),
		WATERSURFACEORSHOREONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				if(R==null)
					return true;
				if((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
				||(R.domainType()==Room.DOMAIN_INDOORS_AIR))
					return true;
				if(CMLib.flags().isWaterySurfaceRoom(R)
				|| (R.ID().equals("Shore"))
				|| (R.domainType() == Room.DOMAIN_OUTDOORS_SEAPORT))
					return false;
				boolean foundWater=false;
				for(int dir2 : Directions.CODES())
				{
					final Room R2=R.getRoomInDir(dir2);
					if((R2!=null)&&(CMLib.flags().isWaterySurfaceRoom(R2)))
						foundWater=true;
				}
				return (!foundWater);
			}
		}),
		SHOREONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				if(R==null)
					return true;
				if((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
				|| (R.domainType()==Room.DOMAIN_INDOORS_AIR)
				|| (CMLib.flags().isWaterySurfaceRoom(R) ))
					return true;
				if((R.ID().equals("Shore"))
				|| (R.domainType() == Room.DOMAIN_OUTDOORS_SEAPORT))
					return false;
				boolean foundWater=false;
				for(int dir2 : Directions.CODES())
				{
					final Room R2=R.getRoomInDir(dir2);
					if((R2!=null)&&(CMLib.flags().isWaterySurfaceRoom(R2)))
						foundWater=true;
				}
				return (!foundWater);
			}
		}),
		UNDERWATERONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return !CMLib.flags().isUnderWateryRoom(R);
			}
		}),
		FLOORSONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (R.getRoomInDir(Directions.DOWN)!=null);
			}
		}),
		CEILINGSSONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (R.getRoomInDir(Directions.UP)!=null);
			}
		}),
		NOCLIMB(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (CMLib.flags().isClimbing(R) || CMLib.flags().isClimbing(E));
			}
		}),
		NOCRAWL(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (CMLib.flags().isCrawlable(R) || CMLib.flags().isCrawlable(E));
			}
		}),
		OUTDOORONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (R.domainType() & Room.INDOORS) != 0;
			}
		}),
		INDOORONLY(new RFilter()
		{
			@Override
			public boolean isFilteredOut(Room hostR, final Room R, final Exit E, final int dir)
			{
				return (R.domainType() & Room.INDOORS) == 0;
			}
		});
		public RFilter myFilter;
		private TrackingFlag(RFilter filter)
		{
			this.myFilter=filter;
		}
	}
}
