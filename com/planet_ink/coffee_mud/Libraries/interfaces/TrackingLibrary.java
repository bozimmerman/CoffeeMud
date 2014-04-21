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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2014 Bo Zimmerman

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
	public List<Room> findBastardTheBestWay(Room location, Room destRoom, TrackingFlags flags, int maxRadius);
	public List<Room> findBastardTheBestWay(Room location, List<Room> destRooms, TrackingFlags flags, int maxRadius);
	public int trackNextDirectionFromHere(List<Room> theTrail, Room location, boolean openOnly);
	public void stopTracking(MOB mob);
	public int radiatesFromDir(Room room, List<Room> rooms);
	public void getRadiantRooms(Room room, List<Room> rooms, TrackingFlags flags, Room radiateTo, int maxDepth, Set<Room> ignoreRooms);
	public List<Room> getRadiantRooms(final Room room, final RFilters filters, final int maxDepth);
	public void getRadiantRooms(final Room room, List<Room> rooms, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms);
	public List<Room> getRadiantRooms(Room room, TrackingFlags flags, int maxDepth);
	public boolean beMobile(MOB mob, boolean dooropen, boolean wander, boolean roomprefer, boolean roomobject, int[] status, List<Room> rooms);
	public void wanderAway(MOB M, boolean mindPCs, boolean andGoHome);
	public void wanderFromTo(MOB M, Room toHere, boolean mindPCs);
	public void wanderIn(MOB M, Room toHere);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
	public boolean walk(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);
	public boolean run(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders, boolean always);
	public int findExitDir(MOB mob, Room R, String desc);
	public int findRoomDir(MOB mob, Room R);
	public void markToWanderHomeLater(MOB M);
	public List<Integer> getShortestTrail(final List<List<Integer>> finalSets);
	public List<List<Integer>> findAllTrails(Room from, Room to, List<Room> radiantTrail);
	public List<List<Integer>> findAllTrails(Room from, List<Room> tos, List<Room> radiantTrail);
	public String getTrailToDescription(Room R1, List<Room> set, String where, boolean areaNames, boolean confirm, int radius, Set<Room> ignoreRooms, int maxMins);
	
	public static abstract class RFilter
	{
		public abstract boolean isFilteredOut(final Room R, final Exit E, final int dir);
	}

	public static class RFilterNode
	{
		private RFilterNode next=null;
		private final RFilter filter;
		public RFilterNode(RFilter fil){ this.filter=fil;}
		
	}
	public static class RFilters
	{
		private RFilterNode head=null;
		public boolean isFilteredOut(final Room R, final Exit E, final int dir)
		{
			RFilterNode me=head;
			while(me!=null)
			{
				if(me.filter.isFilteredOut(R,E,dir))
					return true;
				me=me.next;
			}
			return false;
		}
		public RFilters plus(RFilter filter) 
		{ 
			RFilterNode me=head;
			if(me==null)
				head=new RFilterNode(filter);
			else
			{
				while(me.next!=null)
					me=me.next;
				me.next=new RFilterNode(filter);
			}
			return this;
		}
	}
	
	public static enum TrackingFlag 
	{
		NOHOMES(new RFilter(){ public boolean isFilteredOut(final Room R, final Exit E, final int dir){ 
			return CMLib.law().getLandTitle(R)!=null; 
		}}),
		OPENONLY(new RFilter(){ public boolean isFilteredOut(final Room R, final Exit E, final int dir){ 
			return !E.isOpen();
		}}),
		UNLOCKEDONLY(new RFilter(){ public boolean isFilteredOut(final Room R, final Exit E, final int dir){ 
			return !E.hasALock();
		}}),
		AREAONLY(new RFilter(){ public boolean isFilteredOut(final Room R, final Exit E, final int dir){ 
			return CMLib.law().getLandTitle(R)!=null; 
		}}),
		NOEMPTYGRIDS(new RFilter(){ public boolean isFilteredOut(final Room R, final Exit E, final int dir){ 
			return (R.getGridParent()!=null)&&(R.getGridParent().roomID().length()==0); 
		}}),
		NOAIR(new RFilter(){ public boolean isFilteredOut(final Room R, final Exit E, final int dir){ 
			return (R.domainType()==Room.DOMAIN_INDOORS_AIR) ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR); 
		}}),
		NOWATER(new RFilter(){  public boolean isFilteredOut(final Room R, final Exit E, final int dir){ 
			return (R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
				   ||(R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
				   ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE); 
		}});
		public RFilter myFilter;
		private TrackingFlag(RFilter filter)
		{
			this.myFilter=filter;
		}
	}
	
	public static class TrackingFlags extends HashSet<TrackingFlag> 
	{
        private static final long serialVersionUID = -6914706649617909073L;
		private int hashCode=(int)serialVersionUID;
		public TrackingFlags plus(TrackingFlag flag) 
		{ 
			add(flag); 
			hashCode^=flag.hashCode();
			return this;
		}
		@Override
		public int hashCode()
		{
			return hashCode;
		}
	}
}
