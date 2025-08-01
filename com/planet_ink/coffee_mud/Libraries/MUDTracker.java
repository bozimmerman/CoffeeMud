package com.planet_ink.coffee_mud.Libraries;
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

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.RFilter;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.RFilters;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrailFlag;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class MUDTracker extends StdLibrary implements TrackingLibrary
{
	@Override
	public String ID()
	{
		return "MUDTracker";
	}

	protected Map<Integer,List<String>>		directionCommandSets= new Hashtable<Integer,List<String>>();
	protected Map<Integer,List<String>>		openCommandSets		= new Hashtable<Integer,List<String>>();
	protected Map<Integer,List<String>>		closeCommandSets	= new Hashtable<Integer,List<String>>();
	protected Map<TrackingFlags,RFilters>	trackingFilters		= new Hashtable<TrackingFlags,RFilters>();
	protected static final TrackingFlags	EMPTY_FLAGS			= new DefaultTrackingFlags();
	protected static final RFilters			EMPTY_FILTERS		= new DefaultRFilters();

	protected static class RFilterNode
	{
		private RFilterNode		next	= null;
		private final RFilter	filter;

		public RFilterNode(final RFilter fil)
		{
			this.filter = fil;
		}

	}

	protected final RFilters emptyFilter = new DefaultRFilters();
	protected final RFilter validIDFilter = new RFilter()
	{
		@Override
		public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
		{
			if(R==null)
				return true;
			if((R.roomID()!=null)
			&&(R.roomID().length()>0))
				return false;
			if((R.getGridParent()!=null)
			&&(R.getGridParent().roomID()!=null)
			&&(R.getGridParent().roomID().length()>0))
				return false;
			return true;
		}
	};

	protected static class DefaultRFilters implements RFilters
	{
		private RFilterNode head=null;

		@Override
		public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
		{
			RFilterNode me=head;
			while(me!=null)
			{
				if(me.filter.isFilteredOut(hostR,R,E, dir))
					return true;
				me=me.next;
			}
			return false;
		}

		@Override
		public RFilters plus(final RFilter filter)
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

		@Override
		public RFilters minus(final RFilter filter)
		{
			RFilterNode prev=null;
			RFilterNode me=head;
			while((me!=null)&&(me.filter != filter))
			{
				prev=me;
				me=me.next;
			}
			if((me!=null)&&(me.filter==filter))
			{
				if(prev==null)
					head=me.next;
				else
					prev.next=me.next;
			}
			return this;
		}

		@Override
		public RFilters copyOf()
		{
			final DefaultRFilters newFilters = new DefaultRFilters();
			RFilterNode me=head;
			while(me!=null)
			{
				newFilters.plus(me.filter);
				me=me.next;
			}
			return newFilters;
		}
	}

	protected static class DefaultTrackingFlags extends HashSet<TrackingFlag> implements TrackingFlags
	{
		private static final long serialVersionUID = -6914706649617909073L;

		private int hashCode=(int)serialVersionUID;

		@Override
		public boolean add(final TrackingFlag flag)
		{
			if(super.add(flag))
			{
				hashCode^=flag.hashCode();
				return true;
			}
			return false;
		}

		@Override
		public boolean addAll(final Collection<? extends TrackingFlag> flags)
		{
			if(super.addAll(flags))
			{
				for(final TrackingFlag f : flags)
					hashCode^=f.hashCode();
				return true;
			}
			return false;
		}

		@Override
		public TrackingFlags plus(final TrackingFlag flag)
		{
			add(flag);
			return this;
		}

		@Override
		public TrackingFlags plus(final TrackingFlags flags)
		{
			addAll(flags);
			return this;
		}

		@Override
		public TrackingFlags copyOf()
		{
			final DefaultTrackingFlags newFlags = new DefaultTrackingFlags();
			newFlags.addAll(this);
			newFlags.hashCode = hashCode;
			return newFlags;
		}

		@Override
		public TrackingFlags minus(final TrackingFlag flag)
		{
			remove(flag);
			return this;
		}

		@Override
		public boolean remove(final Object flag)
		{
			if(super.remove(flag))
			{
				hashCode=(int)serialVersionUID;
				for(final TrackingFlag f : this)
					hashCode^=f.hashCode();
				return true;
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}
	}

	@Override
	public boolean autoTrack(final MOB mob, final Room destR)
	{
		CMLib.tracking().stopTracking(mob);
		final Ability A=CMClass.getAbility("Skill_Track");
		if(A!=null)
		{
			A.invoke(mob,CMParms.parse("\""+CMLib.map().getExtendedRoomID(destR)+"\" NPC"),destR,true,0);
			return true;
		}
		return false;
	}

	protected List<String> getDirectionCommandSet(final int direction)
	{
		final Integer dir=Integer.valueOf(direction);
		if(!directionCommandSets.containsKey(dir))
		{
			final Vector<String> V=new ReadOnlyVector<String>(CMLib.directions().getDirectionName(direction));
			directionCommandSets.put(dir, V);
		}
		return directionCommandSets.get(dir);
	}

	protected List<String> getOpenCommandSet(final int direction)
	{
		final Integer dir=Integer.valueOf(direction);
		if(!directionCommandSets.containsKey(dir))
		{
			final Vector<String> V=new ReadOnlyVector<String>(CMParms.parse("OPEN "+CMLib.directions().getDirectionName(direction)));
			directionCommandSets.put(dir, V);
		}
		return directionCommandSets.get(dir);
	}

	protected List<String> getCloseCommandSet(final int direction)
	{
		final Integer dir=Integer.valueOf(direction);
		if(!directionCommandSets.containsKey(dir))
		{
			final Vector<String> V=new ReadOnlyVector<String>(CMParms.parse("CLOSE "+CMLib.directions().getDirectionName(direction)));
			directionCommandSets.put(dir, V);
		}
		return directionCommandSets.get(dir);
	}

	@Override
	public List<Room> findTrailToRoom(final Room location, final Room destRoom, final TrackingFlags flags, final int maxRadius)
	{
		return findTrailToRoom(location,destRoom,flags,maxRadius,null);
	}

	@Override
	public List<Room> findTrailToRoom(final Room location, final Room destRoom, final TrackingFlags flags, final int maxRadius, List<Room> radiant)
	{
		if((radiant==null)||(radiant.size()==0))
		{
			radiant=new ArrayList<Room>();
			getRadiantRooms(location,radiant,flags,destRoom,maxRadius,null);
			if(!radiant.contains(location))
				radiant.add(0,location);
		}
		else
		{
			final List<Room> radiant2=new ArrayList<Room>(radiant.size());
			int r=0;
			boolean foundLocation=false;
			Room O;
			for(;r<radiant.size();r++)
			{
				O=radiant.get(r);
				radiant2.add(O);
				if((!foundLocation)&&(O==location))
					foundLocation=true;
				if(O==destRoom)
					break;
			}
			if(!foundLocation)
			{
				radiant.add(0,location);
				radiant2.add(0,location);
			}
			if(r>=radiant.size())
				return null;
			radiant=radiant2;
		}
		if((radiant.size()>0)&&(destRoom==radiant.get(radiant.size()-1)))
		{
			List<Room> thisTrail=new Vector<Room>();
			final HashSet<Room> tried=new HashSet<Room>();
			thisTrail.add(destRoom);
			tried.add(destRoom);
			Room R=null;
			int index=radiant.size()-2;
			if(destRoom!=location)
			{
				while(index>=0)
				{
					int best=-1;
					for(int i=index;i>=0;i--)
					{
						R=radiant.get(i);
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							if((R.getRoomInDir(d)==thisTrail.get(thisTrail.size()-1))
							&&(R.getExitInDir(d)!=null)
							&&(!tried.contains(R)))
								best=i;
						}
					}
					if(best>=0)
					{
						R=radiant.get(best);
						thisTrail.add(R);
						tried.add(R);
						if(R==location)
							break;
						index=best-1;
					}
					else
					{
						thisTrail.clear();
						thisTrail=null;
						break;
					}
				}
			}
			return thisTrail;
		}
		return null;
	}

	@Override
	public void markToWanderHomeLater(final MOB M, final int ticks)
	{
		if((M == null)||(M.getStartRoom()==null))
			return;
		Ability A=M.fetchEffect("WanderHomeLater");
		if(A == null)
		{
			A=CMClass.getAbility("WanderHomeLater");
			A.setMiscText("ONCE=true MINTICKS="+ticks+" MAXTICKS="+ticks);
			M.addEffect(A);
			A.setSavable(false);
			A.makeLongLasting();
		}
		else
			A.setMiscText("ONCE=true MINTICKS="+ticks+" MAXTICKS="+ticks);
	}

	@Override
	public List<Room> findTrailToAnyRoom(final Room location,
										 final List<Room> destRooms,
										 final TrackingFlags flags,
										 final int maxRadius)
	{

		List<Room> finalTrail=null;
		Room destRoom=null;
		int pick=0;
		List<Room> radiant=null;
		if(destRooms.size()>1)
		{
			radiant=new Vector<Room>();
			getRadiantRooms(location,radiant,flags,null,maxRadius,null);
		}
		for(int i=0;(i<5)&&(destRooms.size()>0);i++)
		{
			pick=CMLib.dice().roll(1,destRooms.size(),-1);
			destRoom=destRooms.get(pick);
			destRooms.remove(pick);
			final TrackingFlags finalFlags = flags.copyOf();
			if(destRoom.getArea() == location.getArea())
				finalFlags.add(TrackingFlag.AREAONLY);
			List<Room> thisTrail=findTrailToRoom(location,destRoom,finalFlags,maxRadius,radiant);
			if((destRoom.getArea() == location.getArea())
			&&((thisTrail==null)||(thisTrail.size()==0)))
				thisTrail=findTrailToRoom(location,destRoom,flags,maxRadius,radiant);
			if((thisTrail!=null)
			&&((finalTrail==null)||(thisTrail.size()<finalTrail.size())))
				finalTrail=thisTrail;
		}
		if(finalTrail==null)
		{
			for(int r=0;r<destRooms.size();r++)
			{
				destRoom=destRooms.get(r);
				final List<Room> thisTrail=findTrailToRoom(location,destRoom,flags,maxRadius);
				if((thisTrail!=null)
				&&((finalTrail==null)||(thisTrail.size()<finalTrail.size())))
					finalTrail=thisTrail;
			}
		}
		return finalTrail;
	}

	@Override
	public List<Room> findTrailToAnyRoom(final Room location, final RFilter destFilter, final TrackingFlags flags, final int maxRadius)
	{
		final List<Room> radiant=new ArrayList<Room>();
		if(!getRadiantRoomsToTarget(location, radiant, flags, destFilter, maxRadius))
			return new Vector<Room>(1);
		if(radiant.size()==0)
			return radiant;

		final Room destRoom=radiant.get(radiant.size()-1);
		return findTrailToRoom(location,destRoom,flags,maxRadius);
	}

	@Override
	public int trackNextDirectionFromHere(final List<Room> theTrail,
										  final Room location,
										  final boolean openOnly)
	{
		if((theTrail==null)||(location==null))
			return -1;
		if(location==theTrail.get(0))
			return 999;
		int locationLocation=theTrail.indexOf(location);
		if(locationLocation<0)
			locationLocation=Integer.MAX_VALUE;
		Room R=null;
		Exit E=null;
		int x=0;
		int winningDirection=-1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R=location.getRoomInDir(d);
			E=location.getExitInDir(d);
			if((R!=null)
			&&(E!=null)
			&&((!openOnly)||(E.isOpen())))
			{
				x=theTrail.indexOf(R);
				if((x>=0)&&(x<locationLocation))
				{
					locationLocation=x;
					winningDirection=d;
				}
			}
		}
		return winningDirection;
	}

	@Override
	public int radiatesFromDir(final Room room, final List<Room> rooms)
	{
		final Map<Room,Integer> moveSet = new HashMap<Room,Integer>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room cR = room.getRoomInDir(d);
			if(cR != null)
				moveSet.put(cR, Integer.valueOf(d));
		}
		for(final Room R : rooms)
		{
			if(R==room)
				return -1;
			if(moveSet.containsKey(R))
				return moveSet.get(R).intValue();
		}
		return -1;
	}

	@Override
	public List<Room> getRadiantRooms(final Room room, final TrackingFlags flags, final int maxDepth)
	{
		final List<Room> V=new Vector<Room>();
		getRadiantRooms(room,V,flags,null,maxDepth,null);
		return V;
	}

	@Override
	public List<Room> getRadiantRooms(final Room room, final RFilters filters, final int maxDepth)
	{
		final List<Room> V=new Vector<Room>();
		getRadiantRooms(room,V,filters,(Room)null,maxDepth,null);
		return V;
	}

	@Override
	public List<Area> getRadiantAreas(final Area area, final int maxDepth)
	{
		final List<Area> V=new Vector<Area>();
		getRadiantAreas(area,V,null,maxDepth,null);
		return V;
	}

	@Override
	public void getRadiantRooms(final Room room, final List<Room> rooms, TrackingFlags flags, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms)
	{
		if(flags == null)
			flags = EMPTY_FLAGS;
		RFilters filters=trackingFilters.get(flags);
		if(filters==null)
		{
			if(flags.size()==0)
				filters=EMPTY_FILTERS;
			else
			{
				filters=new DefaultRFilters();
				for(final TrackingFlag flag : flags)
					filters.plus(flag.myFilter);
			}
			trackingFilters.put(flags, filters);
		}
		getRadiantRooms(room, rooms, filters, radiateTo, maxDepth, ignoreRooms);
	}

	@Override
	public TrackingFlags newFlags()
	{
		return new DefaultTrackingFlags();
	}

	@Override
	public void getRadiantRooms(final Room room, final List<Room> rooms, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms)
	{
		int depth=0;
		if(room==null)
			return;
		if(rooms.contains(room))
			return;
		final HashSet<Room> H=new HashSet<Room>(1000);
		rooms.add(room);
		if(rooms instanceof Vector<?>)
			((Vector<Room>)rooms).ensureCapacity(200);
		if(ignoreRooms != null)
			H.addAll(ignoreRooms);
		for(int r=0;r<rooms.size();r++)
			H.add(rooms.get(r));
		int min=0;
		int size=rooms.size();
		Room R1=null;
		Room R=null;
		Exit E=null;

		int r=0;
		int d=0;
		final WorldMap map=CMLib.map();
		while(depth<maxDepth)
		{
			for(r=min;r<size;r++)
			{
				R1=rooms.get(r);
				for(d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					R=R1.getRoomInDir(d);
					E=R1.getExitInDir(d);

					if((R==null)||(E==null))
						continue;
					R=map.getRoom(R);
					if((R==null)
					||(H.contains(R))
					||(filters.isFilteredOut(R1, R, E, d)))
						continue;
					rooms.add(R);
					H.add(R);
					if(R==radiateTo) // R can't be null here, so if they are equal, time to go!
						return;
				}
			}
			min=size;
			size=rooms.size();
			if(min==size)
				return;
			depth++;
		}
	}

	protected void getRadiantAreas(final Area area, final List<Area> areas, final Area radiateTo, final int maxDepth, final Set<Area> ignoreAreas)
	{
		int depth=0;
		if(area==null)
			return;
		if(areas.contains(area))
			return;
		final HashSet<Area> H=new HashSet<Area>();
		areas.add(area);
		if(areas instanceof Vector<?>)
			((Vector<Area>)areas).ensureCapacity(10);
		if(ignoreAreas != null)
			H.addAll(ignoreAreas);
		for(int r=0;r<areas.size();r++)
			H.add(areas.get(r));
		int min=0;
		int size=areas.size();
		Area A1=null;
		Room R1=null;
		Room R=null;
		Exit E=null;

		int a=0;
		int d=0;
		final WorldMap map=CMLib.map();
		while(depth<maxDepth)
		{
			for(a=min;a<size;a++)
			{
				A1=areas.get(a);
				for(final Enumeration<Room> rs=A1.getProperMap();rs.hasMoreElements();)
				{
					R1 = rs.nextElement();
					if(R1 == null)
						continue;
					for(d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						R=R1.getRoomInDir(d);
						E=R1.getExitInDir(d);

						if((R==null)||(E==null))
							continue;
						R=map.getRoom(R);
						if((R==null)
						||(H.contains(R.getArea())))
							continue;
						areas.add(R.getArea());
						H.add(R.getArea());
						if(R.getArea()==radiateTo) // R can't be null here, so if they are equal, time to go!
							return;
					}
				}
			}
			min=size;
			size=areas.size();
			if(min==size)
				return;
			depth++;
		}
	}

	@Override
	public Enumeration<Room> getRadiantRoomsEnum(final Room room, final RFilters filters, final Room radiateTo, final int maxDepth, final Set<Room> ignoreRooms)
	{
		if(room==null)
			return new EmptyEnumeration<Room>();

		final Set<Room> Hs=new HashSet<Room>(1000);
		final LinkedList<Room> Rs = new LinkedList<Room>();
		Rs.add(room);
		if(ignoreRooms != null)
			Hs.addAll(ignoreRooms);
		for(int r=0;r<Rs.size();r++)
			Hs.add(Rs.get(r));
		return new Enumeration<Room>()
		{
			final LinkedList<Room> rooms=Rs;
			final Set<Room> H=Hs;
			final WorldMap map=CMLib.map();
			final boolean noFilter = filters==null;
			final RFilters filter = filters;

			int depth=0;
			Room R1=null;
			Room R=null;
			Exit E=null;
			int min=0;
			int r=0;
			int d=0;
			boolean finished=false;

			@Override
			public boolean hasMoreElements()
			{
				return rooms.size()>0;
			}
			@Override
			public Room nextElement()
			{
				if((!finished)
				&&(rooms.size()>0)
				&&(depth<maxDepth)
				&&(min==0))
				{
					final int newMin=rooms.size();
					for(r=min;r<newMin;r++)
					{
						R1=rooms.get(r);
						for(d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							R=R1.getRoomInDir(d);
							E=R1.getExitInDir(d);
							if((R!=null)&&(E!=null))
							{
								R=map.getRoom(R);
								if((R!=null)
								&&(!H.contains(R))
								&&((noFilter)||(!filter.isFilteredOut(R1, R, E, d))))
								{
									rooms.add(R);
									H.add(R);
									if(R==radiateTo) // R can't be null here, so if they are equal, time to go!
									{
										finished=true;
										break;
									}
								}
							}
						}
					}
					min=newMin;
					if(min==rooms.size())
						finished=true;
					depth++;
				}
				if(!hasMoreElements())
					throw new java.util.NoSuchElementException();
				min--; // super important!
				return rooms.removeFirst();
			}
		};
	}

	@Override
	public boolean getRadiantRoomsToTarget(final Room room, final List<Room> rooms, TrackingFlags flags, final RFilter radiateTo, final int maxDepth)
	{
		if(flags == null)
			flags = EMPTY_FLAGS;
		RFilters filters=trackingFilters.get(flags);
		if(filters==null)
		{
			if(flags.size()==0)
				filters=EMPTY_FILTERS;
			else
			{
				filters=new DefaultRFilters();
				for(final TrackingFlag flag : flags)
					filters.plus(flag.myFilter);
			}
			trackingFilters.put(flags, filters);
		}
		return getRadiantRoomsToTarget(room, rooms, filters, radiateTo, maxDepth);
	}

	protected boolean getRadiantRoomsToTarget(final Room room, final List<Room> rooms, final RFilters filters, final RFilter radiateTo, final int maxDepth)
	{
		int depth=0;
		if(room==null)
			return false;
		if(rooms.contains(room))
			return false;
		final HashSet<Room> H=new HashSet<Room>(1000);
		rooms.add(room);
		if(rooms instanceof Vector<?>)
			((Vector<Room>)rooms).ensureCapacity(200);
		for(int r=0;r<rooms.size();r++)
			H.add(rooms.get(r));
		int min=0;
		int size=rooms.size();
		Room R1=null;
		Room R=null;
		Exit E=null;

		int r=0;
		int d=0;
		while(depth<maxDepth)
		{
			for(r=min;r<size;r++)
			{
				R1=rooms.get(r);
				for(d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					R=R1.getRoomInDir(d);
					E=R1.getExitInDir(d);

					if((R==null)||(E==null)
					||(H.contains(R))
					||(filters.isFilteredOut(R1, R, E, d)))
						continue;
					rooms.add(R);
					H.add(R);
					if(!radiateTo.isFilteredOut(R1,R,E,d)) // R can't be null here, so if they are equal, time to go!
						return true;
				}
			}
			min=size;
			size=rooms.size();
			if(min==size)
				return false;
			depth++;
		}
		return false;
	}

	@Override
	public Room getRadiantRoomTarget(final Room room, final RFilters filters, final RFilter radiateTo)
	{
		if(room==null)
			return null;
		final TreeSet<Room> H=new TreeSet<Room>();
		Room R1=null;
		Room R=null;
		Exit E=null;

		int d=0;
		final LinkedList<Room> roomsToDo=new LinkedList<Room>();
		roomsToDo.add(room);
		while(roomsToDo.size()>0)
		{
			R1=roomsToDo.removeFirst();
			for(d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				R=R1.getRoomInDir(d);
				E=R1.getExitInDir(d);

				if((R==null)
				||(E==null)
				||(H.contains(R))
				||(filters.isFilteredOut(R1, R, E, d)))
					continue;
				roomsToDo.add(R);
				H.add(R);
				if(!radiateTo.isFilteredOut(R1, R, E, d)) // R can't be null here, so if they are equal, time to go!
					return R;
			}
		}
		return null;
	}

	@Override
	public Room getNearestValidIDRoom(final Room R)
	{
		if(R==null)
			return null;
		if(!validIDFilter.isFilteredOut(R, R, null, 0))
			return R;
		return getRadiantRoomTarget(R, emptyFilter, validIDFilter);
	}

	@Override
	public void stopTracking(final MOB mob)
	{
		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(final Ability A : V)
		{
			A.unInvoke();
			mob.delEffect(A);
		}
	}

	@Override
	public boolean beMobile(final MOB mob,
							final boolean dooropen,
							final boolean wander,
							final boolean roomprefer,
							final boolean roomobject,
							final int[] status,
							final Set<Room> rooms)
	{
		return beMobile(mob,dooropen,wander,roomprefer,roomobject,true,status,rooms);

	}

	private boolean beMobile(final MOB mob,
							 boolean dooropen,
							 final boolean wander,
							 final boolean roomprefer,
							 final boolean roomobject,
							 final boolean sneakIfAble,
							 final int[] status,
							 final Set<Room> rooms)
	{
		if(status!=null)
			status[0]=Tickable.STATUS_MISC7+0;

		// ridden and following things aren't mobile!
		if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
		||((mob.amFollowing()!=null)
			&&(CMLib.tracking().areNearEachOther(mob,mob.amFollowing())
				||CMLib.tracking().areNearEachOther(mob,mob.getGroupLeader()))))
		{
			if(status!=null)
				status[0]=Tickable.STATUS_NOT;
			return false;
		}

		Room oldRoom=mob.location();

		if(CMLib.hunt().isAnAdminHere(oldRoom, true))
		{
			if(status!=null)
				status[0]=Tickable.STATUS_NOT;
			return false;
		}

		if(oldRoom instanceof GridLocale)
		{
			final Room R=((GridLocale)oldRoom).getRandomGridChild();
			if(R!=null)
				R.bringMobHere(mob,true);
			oldRoom=mob.location();
		}

		if(oldRoom == null)
		{
			if((!mob.amDead())
			&&(!mob.amDestroyed()))
			{
				if(mob.isPlayer()
				&& (mob.getStartRoom()!=null))
					mob.getStartRoom().bringMobHere(mob, true);
				else
					mob.killMeDead(false);
				Log.errOut("MUDTracker","Inexplicable lost room for '"+mob.Name()+"'.  Killing dead.");
			}
			return false;
		}

		if(status!=null)
			status[0]=Tickable.STATUS_MISC7+3;
		int tries=0;
		int direction=-1;
		final CMFlagLibrary flags=CMLib.flags();
		while(((tries++)<10)&&(direction<0))
		{
			direction=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1);
			final Room nextRoom=oldRoom.getRoomInDir(direction);
			final Exit nextExit=oldRoom.getExitInDir(direction);
			if((nextRoom!=null)&&(nextExit!=null))
			{
				if(CMLib.hunt().isAnAdminHere(nextRoom, true))
				{
					direction=-1;
					continue;
				}

				final Exit opExit=nextRoom.getExitInDir(Directions.getOpDirectionCode(direction));
				if(flags.isTrapped(nextExit)
				||(flags.isHidden(nextExit)
					&&(!flags.canSeeHidden(mob))
					&&(!flags.canSeeHiddenItems(mob)))
				||(flags.isInvisible(nextExit)
					&&(!flags.canSeeInvisible(mob))))
				{
					direction=-1;
				}
				else
				if((opExit!=null)&&(flags.isTrapped(opExit)))
					direction=-1;
				else
				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!flags.isInFlight(mob))
				&&((nextRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
					||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)))
				{
					direction=-1;
				}
				else
				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!flags.isSwimming(mob))
				&&(flags.isUnderWateryRoom(nextRoom)))
					direction=-1;
				else
				if((!wander)
				&&(!oldRoom.getArea().Name().equals(nextRoom.getArea().Name())))
					direction=-1;
				else
				if((roomobject)
				&&(rooms!=null)&&(rooms.contains(nextRoom)))
					direction=-1;
				else
				if((roomprefer)
				&&(rooms!=null)&&(!rooms.contains(nextRoom)))
					direction=-1;
				else
					break;
			}
			else
				direction=-1;
		}

		if(status!=null)
			status[0]=Tickable.STATUS_MISC7+10;

		if(direction<0)
		{
			if(status!=null)
				status[0]=Tickable.STATUS_NOT;
			return false;
		}

		final Room nextRoom=oldRoom.getRoomInDir(direction);
		final Exit nextExit=oldRoom.getExitInDir(direction);
		final int opDirection=oldRoom.getReverseDir(direction);

		if((nextRoom==null)||(nextExit==null))
		{
			if(status!=null)
				status[0]=Tickable.STATUS_NOT;
			return false;
		}

		if(dooropen)
		{
			final LandTitle landTitle=CMLib.law().getLandTitle(nextRoom);
			if((landTitle!=null)&&(landTitle.getOwnerName().length()>0))
				dooropen=false;
		}

		boolean reclose=false;
		boolean relock=false;
		// handle doors!
		if(nextExit.hasADoor()
		&&(!nextExit.isOpen())
		&&(dooropen))
		{
			if((nextExit.hasALock())
			&&(nextExit.isLocked()))
			{
				CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
				if(oldRoom.okMessage(mob,msg))
				{
					relock=true;
					msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,L("<S-NAME> unlock(s) <T-NAMESELF><O-WITHNAME>."));
					if(oldRoom.okMessage(mob,msg))
						CMLib.utensils().roomAffectFully(msg,oldRoom,direction);
				}
			}
			if(!nextExit.isOpen())
			{
				mob.doCommand(getOpenCommandSet(direction),MUDCmdProcessor.METAFLAG_FORCED);
				if(nextExit.isOpen())
					reclose=true;
			}
		}
		if(!nextExit.isOpen())
		{
			if(status!=null)
				status[0]=Tickable.STATUS_NOT;
			return false;
		}

		if(mob.numAllAbilities()==0)
			walk(mob,direction,false,false);
		else
		if(((flags.isWaterySurfaceRoom(nextRoom)))
		   &&(!flags.isWaterWorthy(mob))
		   &&(!flags.isInFlight(mob))
		   &&(mob.fetchAbility("Skill_Swim")!=null))
		{
			final Ability A=mob.fetchAbility("Skill_Swim");
			final List<String> V=getDirectionCommandSet(direction);
			if(A.proficiency()<50)
				A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			final CharState state = mob.curState();
			final int[] oldStateVals = new int[] {state.getMana(), state.getMovement() } ;
			A.invoke(mob,V,null,false,0);
			state.setMana(oldStateVals[0]);
			state.setMovement(oldStateVals[1]);
		}
		else
		if(((nextRoom.ID().indexOf("Surface")>0)
			||(flags.isClimbing(nextExit))
			||(flags.isClimbing(nextRoom)))
		&&(!flags.isClimbing(mob))
		&&(!flags.isInFlight(mob))
		&&((mob.fetchAbility("Skill_Climb")!=null)||(mob.fetchAbility("Power_SuperClimb")!=null)))
		{
			Ability A=mob.fetchAbility("Skill_Climb");
			if(A==null )
				A=mob.fetchAbility("Power_SuperClimb");
			final List<String> V=getDirectionCommandSet(direction);
			if(A.proficiency()<50)
				A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			final CharState state = mob.curState();
			final int[] oldStateVals = new int[] {state.getMana(), state.getMovement() } ;
			A.invoke(mob,V,null,false,0);
			state.setMana(oldStateVals[0]);
			state.setMovement(oldStateVals[1]);
		}
		else
		if((mob.fetchAbility("Thief_Sneak")!=null)
		&&(sneakIfAble))
		{
			final Ability A=mob.fetchAbility("Thief_Sneak");
			final List<String> V=getDirectionCommandSet(direction);
			if(A.proficiency()<50)
			{
				A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
				final Ability A2=mob.fetchAbility("Thief_Hide");
				if(A2!=null)
					A2.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			}
			final CharState state = mob.curState();
			final int[] oldStateVals = new int[] {state.getMana(), state.getMovement() } ;
			A.invoke(mob,V,null,false,0);
			state.setMana(oldStateVals[0]);
			state.setMovement(oldStateVals[1]);
		}
		else
		{
			walk(mob,direction,false,false);
		}
		if(status!=null)
			status[0]=Tickable.STATUS_MISC7+21;

		if((reclose)&&(mob.location()==nextRoom)&&(dooropen))
		{
			final Exit opExit=nextRoom.getExitInDir(opDirection);
			if((opExit!=null)
			&&(opExit.hasADoor())
			&&(opExit.isOpen()))
			{
				mob.doCommand(getCloseCommandSet(opDirection),MUDCmdProcessor.METAFLAG_FORCED);
				if((opExit.hasALock())&&(relock))
				{
					CMMsg msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
					if(nextRoom.okMessage(mob,msg))
					{
						msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,L("<S-NAME> lock(s) <T-NAMESELF><O-WITHNAME>."));
						if(nextRoom.okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,nextRoom,opDirection);
					}
				}
			}
		}
		if(status!=null)
			status[0]=Tickable.STATUS_NOT;
		return mob.location()!=oldRoom;
	}

	@Override
	public void wanderAway(final MOB M, final boolean mindPCs, final boolean andGoHome)
	{
		if(M==null)
			return;
		final Room R=M.location();
		if(R==null)
			return;
		int tries=0;
		while((M.location()==R)
		&&((++tries)<10)
		&&((!mindPCs)||(R.numPCInhabitants()==0)))
		{
			if(((M instanceof Rideable)&&(((Rideable)M).numRiders()>0))
			||((M.amFollowing()!=null)
				&&(CMLib.tracking().areNearEachOther(M,M.amFollowing())
					||CMLib.tracking().areNearEachOther(M,M.getGroupLeader()))))
				return;
			beMobile(M,true,true,false,false,false,null,null);
		}
		if(andGoHome)
		{
			final Room startRoom=M.getStartRoom();
			if(startRoom!=null)
			{
				final CMMsg msg=CMClass.getMsg(M, startRoom, null, CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,L("<S-NAME> enter(s)."));
				startRoom.okMessage(M, msg);
				((Room)msg.target()).showOthers(M,startRoom,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,L("<S-NAME> enter(s)."));
				((Room)msg.target()).bringMobHere(M,true);
			}
		}
	}

	@Override
	public boolean areNearEachOther(final MOB whichM, final MOB nearM)
	{
		if((whichM==null)||(nearM==null))
			return false;
		final Room whichR=whichM.location();
		final Room nearR=nearM.location();
		if((whichR==null)||(nearR==null))
			return false;
		if(whichR==nearR)
			return true;
		if(whichR.isInhabitant(nearM)
		||nearR.isInhabitant(whichM))
			return true;
		for(final int d : Directions.CODES())
		{
			if(nearR.getRoomInDir(d)==whichR)
				return true;
			if(whichR.getRoomInDir(d)==nearR)
				return true;
		}
		return false;
	}

	@Override
	public boolean wanderCheckedAway(final MOB M, final boolean mindPCs, final boolean andGoHome)
	{
		if(M==null)
			return false;
		final Room R=M.location();
		if(R==null)
			return false;
		int tries=0;
		while((M.location()==R)
		&&((++tries)<10)
		&&((!mindPCs)||(R.numPCInhabitants()==0)))
			beMobile(M,true,true,false,false,false,null,null);
		if(M.location()==R)
			return false;
		if(andGoHome)
		{
			final Room startRoom=M.getStartRoom();
			if(startRoom!=null)
				startRoom.bringMobHere(M,true);
			return true;
		}
		return true;
	}

	@Override
	public void wanderFromTo(final MOB M, final Room toHere, final boolean mindPCs)
	{
		if(M==null)
			return;
		final Room oldRoom=M.location();
		if((oldRoom!=null)&&(oldRoom.isInhabitant(M)))
			wanderAway(M,mindPCs,false);
		wanderIn(M,toHere);
	}

	protected int getCheckedDir(final MOB M, final Room toHere)
	{
		int dir=-1;
		int tries=0;
		final CMFlagLibrary flags=CMLib.flags();
		while((dir<0)&&((++tries)<100))
		{
			dir=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1);
			final Room R=toHere.getRoomInDir(dir);
			if(R!=null)
			{
				if(((R.domainType()==Room.DOMAIN_INDOORS_AIR)&&(!flags.isFlying(M)))
				||((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)&&(!flags.isFlying(M)))
				||((flags.isUnderWateryRoom(R))&&(!flags.isSwimming(M)))
				||((flags.isWaterySurfaceRoom(R))&&(!flags.isWaterWorthy(M))))
					dir=-1;
				if(tries < 65)
				{
					final Exit E=toHere.getExitInDir(dir);
					if((E==null)||(E.isLocked()))
						dir=-1;
				}
			}
			else
				dir=-1;
		}
		return dir;
	}

	@Override
	public boolean wanderCheckedFromTo(final MOB M, final Room toHere, final boolean mindPCs)
	{
		if(M==null)
			return false;
		if(toHere==null)
			return false;
		final Room oldRoom=M.location();
		if((oldRoom!=null)&&(oldRoom.isInhabitant(M)))
		{
			if(!wanderCheckedAway(M,mindPCs,false))
				return false;
		}
		final int dir = getCheckedDir(M,toHere);
		final Exit exit = (dir < 0) ? null : toHere.getExitInDir(dir);
		final CMMsg enterMsg=CMClass.getMsg(M,toHere,exit,CMMsg.MSG_ENTER,null);
		if(toHere.okMessage(M, enterMsg))
		{
			if(dir<0)
				((Room)enterMsg.target()).show(M,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wanders in."));
			else
			{
				final String inDir=CMLib.directions().getFromDirectionName(dir, CMLib.flags().getDirType(toHere));
				((Room)enterMsg.target()).show(M,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wanders in from @x1.",inDir));
			}
			((Room)enterMsg.target()).executeMsg(M, enterMsg);
			if(M.location()!=((Room)enterMsg.target()))
				((Room)enterMsg.target()).bringMobHere(M,true);
			return true;
		}
		return false;
	}

	@Override
	public void wanderIn(final MOB M, final Room toHere)
	{
		if(toHere==null)
			return;
		if(M==null)
			return;
		final int dir = getCheckedDir(M,toHere);
		if(dir<0)
			toHere.show(M,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wanders in."));
		else
		{
			final String inDir=((toHere instanceof Boardable)||(toHere.getArea() instanceof Boardable))?
					CMLib.directions().getShipDirectionName(dir):CMLib.directions().getDirectionName(dir);
			toHere.show(M,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wanders in from @x1.",inDir));
		}
		toHere.bringMobHere(M,true);
	}

	protected void forceEntry(final MOB M, final Room toHere, final boolean andFollowers, final boolean forceLook, final String msg)
	{
		if(toHere==null)
			return;
		if(M==null)
			return;
		toHere.show(M,toHere,null,CMMsg.MSG_ENTER|CMMsg.MASK_ALWAYS,msg);
		toHere.bringMobHere(M,andFollowers);
		if(forceLook)
			CMLib.commands().postLook(M, true);
	}

	@Override
	public void walkForced(final MOB M, final Room fromHere, final Room toHere, final boolean andFollowers, final boolean forceLook, final String msgStr)
	{
		if(toHere==null)
			return;
		if(M==null)
			return;
		int dir=this.getRoomDirection(fromHere, toHere, null);
		Exit enterExit=null;
		Exit leaveExit=null;
		if(dir < 0)
		{
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				if((d!=Directions.UP)&&(d!=Directions.DOWN))
				{
					if(fromHere.getExitInDir(d)!=null)
						enterExit = fromHere.getExitInDir(d);
					if(toHere.getExitInDir(d)!=null)
					{
						dir=d;
						leaveExit = toHere.getExitInDir(d);
					}
				}
			}
			if(dir<0)
				dir = CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1);
		}
		final CMMsg enterMsg = CMClass.getMsg(M, toHere, enterExit, CMMsg.MSG_ENTER|CMMsg.MASK_ALWAYS, msgStr);
		final CMMsg leaveMsg = CMClass.getMsg(M, fromHere, leaveExit, CMMsg.MSG_LEAVE|CMMsg.MASK_ALWAYS, null);
		leaveMsg.setValue(dir+1);
		enterMsg.setValue(fromHere.getReverseDir(dir)+1);
		if(enterExit!=null)
			enterExit.executeMsg(M,enterMsg);
		if((M.location()!=null)&&(M.location()!=toHere))
			M.location().delInhabitant(M);
		((Room)leaveMsg.target()).send(M,leaveMsg);
		toHere.bringMobHere(M,andFollowers);
		((Room)enterMsg.target()).send(M,enterMsg);
		if(leaveExit!=null)
			leaveExit.executeMsg(M,leaveMsg);
		if(forceLook)
			CMLib.commands().postLook(M, true);
	}

	private class RideFallChecker implements Runnable
	{

		private final MOB mob;
		private volatile int count=0;

		public RideFallChecker(final MOB mob)
		{
			this.mob=mob;
		}

		@Override
		public void run()
		{
			final MOB M;
			synchronized(mob)
			{
				M=mob;
			}
			final WorldMap map=CMLib.map();
			final Rideable ride=M.riding();
			if(ride!=null)
			{
				final Room R=M.location();
				if(R!=null)
				{
					if(map.roomLocation(ride) != map.roomLocation(M))
					{
						if((map.areaLocation(ride) != map.areaLocation(M))
						&&(count>1))
							M.setRiding(null);
						else
						if(++count>=20)
							M.setRiding(null);
						else
						{
							// keep checking
							CMLib.threads().scheduleRunnable(this, 99);
							return;
						}
					}
				}
			}
		}

		@Override
		public int hashCode()
		{
			return mob.hashCode();
		}
	}

	@Override
	public boolean doFallenOffCheck(final MOB mob)
	{
		if((mob==null)||(mob.riding()==null))
			return false;
		final RideFallChecker checker = new RideFallChecker(mob);
		CMLib.threads().scheduleRunnable(checker, 99);
		return false;
	}

	public void ridersBehind(final List<Rider> riders, final Room sourceRoom, final Room destRoom, final int directionCode, final boolean flee, final boolean running)
	{
		if(riders!=null)
		for(final Rider rider : riders)
		{
			if(rider instanceof MOB)
			{
				final MOB rMOB=(MOB)rider;

				if((rMOB.location()==sourceRoom)||(rMOB.location()==destRoom))
				{
					boolean fallOff=false;
					if(rMOB.location()==sourceRoom)
					{
						if(rMOB.riding()!=null)
						{
							final String inDir=((sourceRoom instanceof Boardable)||(sourceRoom.getArea() instanceof Boardable))?
									CMLib.directions().getShipDirectionName(directionCode):CMLib.directions().getDirectionName(directionCode);
							rMOB.tell(L("You ride @x1 @x2.",rMOB.riding().name(),inDir));
						}
						if(!move(rMOB,directionCode,flee,false,true,false,running))
							fallOff=true;
					}
					if(fallOff)
					{
						if(rMOB.riding()!=null)
							rMOB.tell(L("You fall off @x1!",rMOB.riding().name()));
						rMOB.setRiding(null);
					}
				}
				else
					rMOB.setRiding(null);
			}
			else
			if(rider instanceof Item)
			{
				final Item rItem=(Item)rider;
				if((rItem.owner()==sourceRoom)
				||(rItem.owner()==destRoom))
				{
					if(rider instanceof NavigableItem)
						((NavigableItem)rider).navigate(directionCode);
					else
						destRoom.moveItemTo(rItem);
				}
				else
					rItem.setRiding(null);
			}
		}
	}

	public static List<Rider> addRiders(final Rider theRider, final Rideable riding, final List<Rider> riders)
	{

		if((riding!=null)&&(riding.mobileRideBasis()))
		{
			for(int r=0;r<riding.numRiders();r++)
			{
				final Rider rider=riding.fetchRider(r);
				if((rider!=null)
				&&(rider!=theRider)
				&&(!riders.contains(rider)))
				{
					riders.add(rider);
					if(rider instanceof Rideable)
						addRiders(theRider,(Rideable)rider,riders);
				}
			}
		}
		return riders;
	}

	@Override
	public MOB createNavigationMob(final NavigableItem ship)
	{
		if(ship == null)
			return null;
		final Room thisR = CMLib.map().roomLocation(ship.getBoardableItem());
		final MOB mob = CMClass.getFactoryMOB(ship.name(),ship.phyStats().level(),thisR);
		if(thisR == null)
			return null;
		mob.setRiding(ship);
		if(ship.navBasis() == Rideable.Basis.WATER_BASED)
		{
			mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
			mob.phyStats().setDisposition(mob.basePhyStats().disposition());
		}
		if((ship instanceof PrivateProperty)
		&&(((PrivateProperty)ship).getOwnerName()!=null)
		&&(((PrivateProperty)ship).getOwnerName().length()>0))
		{
			final Clan clan = CMLib.clans().fetchClanAnyHost(((PrivateProperty)ship).getOwnerName());
			if(clan != null)
				mob.setClan(clan.name(), clan.getAutoPosition());
		}
		return mob;
	}

	public List<Rider> ridersAhead(final Rider theRider, final Room sourceRoom, final Room destRoom, final int directionCode, final boolean flee, final boolean running)
	{
		final LinkedList<Rider> riders=new LinkedList<Rider>();
		Rideable riding=theRider.riding();
		final LinkedList<Rideable> rideables=new LinkedList<Rideable>();
		while((riding!=null)&&(riding.mobileRideBasis()))
		{
			rideables.add(riding);
			addRiders(theRider,riding,riders);
			if(((Rider)riding).riding()!=theRider.riding())
				riding=((Rider)riding).riding();
			else
				riding=null;
		}
		if(theRider instanceof Rideable)
			addRiders(theRider,(Rideable)theRider,riders);
		for(final Iterator<Rider> r=riders.descendingIterator(); r.hasNext(); )
		{
			final Rider R=r.next();
			if((R instanceof Rideable)&&(((Rideable)R).numRiders()>0))
			{
				if(!rideables.contains(R))
					rideables.add((Rideable)R);
				r.remove();
			}
		}
		for(final ListIterator<Rideable> r=rideables.listIterator(); r.hasNext();)
		{
			riding=r.next();
			if((riding instanceof Item)
			&&((sourceRoom).isContent((Item)riding)))
			{
				if(riding instanceof NavigableItem)
					((NavigableItem)riding).navigate(directionCode);
				else
					destRoom.moveItemTo((Item)riding);
			}
			else
			if((riding instanceof MOB)
			&&((sourceRoom).isInhabitant((MOB)riding)))
			{
				final String inDir=((sourceRoom instanceof Boardable)||(sourceRoom.getArea() instanceof Boardable))?
						CMLib.directions().getShipDirectionName(directionCode):CMLib.directions().getDirectionName(directionCode);
				((MOB)riding).tell(L("You are ridden @x1.",inDir));
				if(!move(((MOB)riding),directionCode,false,false,true,false,running))
				{
					if(theRider instanceof MOB)
						((MOB)theRider).tell(L("@x1 won't seem to let you go that way.",((MOB)riding).name()));
					for(;r.hasPrevious();)
					{
						riding=r.previous();
						if((riding instanceof Item)
						&&((destRoom).isContent((Item)riding)))
							sourceRoom.moveItemTo((Item)riding);
						else
						if((riding instanceof MOB)
						&&(((MOB)riding).isMonster())
						&&((destRoom).isInhabitant((MOB)riding)))
							sourceRoom.bringMobHere((MOB)riding,false);
					}
					return null;
				}
			}
		}
		return riders;
	}

	@Override
	public boolean walk(final MOB mob, final int directionCode, final boolean flee, final boolean nolook, final boolean noriders)
	{
		return walk(mob,directionCode,flee,nolook,noriders,false);
	}

	@Override
	public boolean run(final MOB mob, final int directionCode, final boolean flee, final boolean nolook, final boolean noriders)
	{
		return run(mob,directionCode,flee,nolook,noriders,false);
	}

	@Override
	public boolean walk(final MOB mob, final int directionCode, final boolean flee, final boolean nolook, final boolean noriders, final boolean always)
	{
		return move(mob,directionCode,flee,nolook,noriders,always,false);
	}

	@Override
	public boolean run(final MOB mob, final int directionCode, final boolean flee, final boolean nolook, final boolean noriders, final boolean always)
	{
		return move(mob,directionCode,flee,nolook,noriders,always,true);
	}

	protected boolean move(final MOB mob, final int directionCode, final boolean flee, final boolean nolook, final boolean noriders, final boolean always, final boolean running)
	{
		final Room thisRoom=mob.location();
		if(thisRoom==null)
			return false;
		final Room destRoom=thisRoom.getRoomInDir(directionCode);
		final Exit exit=thisRoom.getExitInDir(directionCode);
		if(destRoom==null)
		{
			mob.tell(L("You can't go that way."));
			final Session sess=mob.session();
			if((sess!=null)&&(sess.getClientTelnetMode(Session.TELNET_GMCP)))
				sess.sendGMCPEvent("room.wrongdir", "\""+CMLib.directions().getDirectionChar(directionCode)+"\"");
			return false;
		}

		final CMFlagLibrary flags=CMLib.flags();
		final int opDir=thisRoom.getReverseDir(directionCode);
		final Exit opExit=((opDir < 0)||(destRoom==null)) ? null : destRoom.getExitInDir(opDir);
		final Directions.DirType dirType=flags.getDirType(thisRoom);
		final String dirName=CMLib.directions().getDirectionName(directionCode, dirType);
		final String fromDir=CMLib.directions().getFromDirectionName(opDir, dirType);
		final String directionName;
		if((exit instanceof PrepositionExit)&&(((PrepositionExit)exit).getExitPreposition().length()>0))
			directionName=((PrepositionExit)exit).getExitPreposition();
		else
			directionName=(directionCode==Directions.GATE)&&(exit!=null)?L("through @x1",exit.name()):dirName.toLowerCase();
		final String otherDirectionPhrase;
		if((exit instanceof PrepositionExit)&&(((PrepositionExit)exit).getEntryPreposition().length()>0))
			otherDirectionPhrase=((PrepositionExit)exit).getEntryPreposition();
		else
			otherDirectionPhrase=L("from @x1",((opDir==Directions.GATE)&&(exit!=null)?exit.name():fromDir));

		final int generalMask=always?CMMsg.MASK_ALWAYS:0;
		final int leaveCode;
		if(flee)
		{
			if(flags.isSitting(mob)&&flags.isCrawlable(mob.location()))
				leaveCode=generalMask|CMMsg.MSG_CRAWLFLEE;
			else
				leaveCode=generalMask|CMMsg.MSG_FLEE;
		}
		else
			leaveCode=generalMask|CMMsg.MSG_LEAVE;

		final CMMsg enterMsg;
		final CMMsg leaveMsg;
		final Rideable ride=mob.riding();
		if((ride!=null)
		&&(ride.mobileRideBasis()))
		{
			final String enterStr=L("<S-NAME> @x1 @x2 in @x3.",ride.rideString(mob),ride.name(),otherDirectionPhrase);
			enterMsg=CMClass.getMsg(mob,destRoom,exit,generalMask|CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,enterStr);
			if(flee)
				leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,leaveCode,L("You flee @x1.",directionName),leaveCode,null,leaveCode,L("<S-NAME> flee(s) with @x1 @x2.",ride.name(),directionName));
			else
				leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,leaveCode,null,leaveCode,null,leaveCode,L("<S-NAME> @x1 @x2 @x3.",ride.rideString(mob),ride.name(),directionName));
		}
		else
		{
			final String arriveWord=flags.getPresentDispositionVerb(mob,CMFlagLibrary.ComingOrGoing.ARRIVES);
			final String arriveStr=L("<S-NAME> "+arriveWord+" @x1.",otherDirectionPhrase);
			enterMsg=CMClass.getMsg(mob,destRoom,exit,generalMask|CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,arriveStr);
			if(flee)
				leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,leaveCode,L("You flee @x1.",directionName),leaveCode,null,leaveCode,L("<S-NAME> flee(s) @x1.",directionName));
			else
			{
				final String leaveWord=flags.getPresentDispositionVerb(mob,CMFlagLibrary.ComingOrGoing.LEAVES);
				final String leaveStr=L("<S-NAME> "+leaveWord+" @x1.",directionName);
				leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,leaveCode,null,leaveCode,null,leaveCode,leaveStr);
			}
		}
		leaveMsg.setValue(directionCode+1);
		enterMsg.setValue(opDir+1);
		final boolean gotoAllowed=(!mob.isMonster()) && CMSecurity.isAllowed(mob,destRoom,CMSecurity.SecFlag.GOTO);
		if((exit==null)&&(!gotoAllowed))
		{
			mob.tell(L("You can't go that way."));
			return false;
		}
		else
		if(exit==null)
			thisRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The area to the @x1 shimmers and becomes transparent.",directionName));
		else
		if(!mob.okMessage(mob,leaveMsg)&&(!gotoAllowed)) // added for honorary degrees .. what will this break?
			return false;
		else
		if((!exit.okMessage(mob,enterMsg))&&(!gotoAllowed))
			return false;
		else
		if(!leaveMsg.target().okMessage(mob,leaveMsg)&&(!gotoAllowed))
			return false;
		else
		if((opExit!=null)&&(!opExit.okMessage(mob,leaveMsg))&&(!gotoAllowed))
			return false;
		else
		if(!enterMsg.target().okMessage(mob,enterMsg)&&(!gotoAllowed))
			return false;
		else
		if(!mob.okMessage(mob,enterMsg)&&(!gotoAllowed))
			return false;

		if(ride!=null)
		{
			if((!ride.okMessage(mob,enterMsg))&&(!gotoAllowed))
				return false;
		}
		else
		{
			if(!mob.isMonster())
			{
				final int expense = running
									? CMProps.getIntVar(CMProps.Int.RUNCOST)
									: CMProps.getIntVar(CMProps.Int.WALKCOST);
				for(int i=0;i<expense;i++)
					CMLib.combat().expendEnergy(mob,true);
			}
			if((!flee)
			&&(!always)
			&&(mob.curState().getMovement()<=0)
			&&(!gotoAllowed))
			{
				mob.tell(L("You are too tired."));
				return false;
			}
			if((mob.soulMate()==null)
			&&(mob.playerStats()!=null)
			&&(mob.riding()==null)
			&&(mob.location()!=null)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.HYGIENE)))
			{
				mob.playerStats().adjHygiene(mob.location().pointsPerMove());
			}
		}

		List<Rider> riders;
		if(!noriders)
		{
			riders=ridersAhead(mob,(Room)leaveMsg.target(),(Room)enterMsg.target(),directionCode,flee, running);
			if(riders==null)
				return false;
		}
		else
			riders=null;
		List<CMMsg> enterTrailersSoFar;
		List<CMMsg> leaveTrailersSoFar;
		if((leaveMsg.trailerMsgs()!=null)&&(leaveMsg.trailerMsgs().size()>0))
		{
			leaveTrailersSoFar=new LinkedList<CMMsg>();
			leaveTrailersSoFar.addAll(leaveMsg.trailerMsgs());
			leaveMsg.trailerMsgs().clear();
		}
		else
			leaveTrailersSoFar=null;
		if((enterMsg.trailerMsgs()!=null)&&(enterMsg.trailerMsgs().size()>0))
		{
			enterTrailersSoFar=new LinkedList<CMMsg>();
			enterTrailersSoFar.addAll(enterMsg.trailerMsgs());
			enterMsg.trailerMsgs().clear();
		}
		else
			enterTrailersSoFar=null;
		if(exit!=null)
			exit.executeMsg(mob,enterMsg);
		if(mob.location()!=null)
			mob.location().delInhabitant(mob);

		((Room)leaveMsg.target()).send(mob,leaveMsg);

		if(enterMsg.target()==null)
		{
			((Room)leaveMsg.target()).bringMobHere(mob,false);
			mob.tell(L("You can't go that way."));
			return false;
		}
		mob.setLocation((Room)enterMsg.target());
		((Room)enterMsg.target()).addInhabitant(mob);
		((Room)enterMsg.target()).send(mob,enterMsg);

		if(opExit!=null)
			opExit.executeMsg(mob,leaveMsg);

		if(!nolook)
		{
			CMLib.commands().postLook(mob,true);
			if((!mob.isMonster())
			&&(mob.isAttributeSet(MOB.Attrib.AUTOWEATHER))
			&&(((Room)enterMsg.target())!=null)
			&&((thisRoom.domainType()&Room.INDOORS)>0)
			&&((((Room)enterMsg.target()).domainType()&Room.INDOORS)==0)
			&&(((Room)enterMsg.target()).getArea().getClimateObj().weatherType(((Room)enterMsg.target()))!=Climate.WEATHER_CLEAR)
			&&(((Room)enterMsg.target()).isInhabitant(mob)))
				mob.tell("\n\r"+((Room)enterMsg.target()).getArea().getClimateObj().weatherDescription(((Room)enterMsg.target())));
		}

		if(!noriders)
			ridersBehind(riders,(Room)leaveMsg.target(),(Room)enterMsg.target(),directionCode,flee, running);

		if(!flee)
		{
			for(int f=0;f<mob.numFollowers();f++)
			{
				final MOB follower=mob.fetchFollower(f);
				if(follower!=null)
				{
					if((follower.amFollowing()==mob)
					&&((follower.location()==thisRoom)||(follower.location()==destRoom)))
					{
						if((follower.location()==thisRoom)
						&&(flags.isAliveAwakeMobile(follower,true)))
						{
							if(follower.isAttributeSet(MOB.Attrib.AUTOGUARD))
								thisRoom.show(follower,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> remain(s) on guard here."));
							else
							{
								final String inDir=((thisRoom instanceof Boardable)||(thisRoom.getArea() instanceof Boardable))?
										CMLib.directions().getShipDirectionName(directionCode):CMLib.directions().getDirectionName(directionCode);
								follower.tell(L("You follow @x1 @x2.",mob.name(follower),inDir));
								boolean tryStand=false;
								if(flags.isSitting(mob))
								{
									if(flags.isSitting(follower))
										tryStand=true;
									else
									{
										final CMMsg msg=CMClass.getMsg(follower,null,null,CMMsg.MSG_SIT,null);
										if((thisRoom.okMessage(mob,msg))
										&&(!flags.isSitting(follower)))
										{
											thisRoom.send(mob,msg);
											tryStand=true;
										}
									}
								}
								if(move(follower,directionCode,false,false,false,false, running)
								&&(tryStand))
									CMLib.commands().postStand(follower, true, false);
							}
						}
					}
				}
			}
		}
		if((leaveTrailersSoFar!=null)&&(leaveMsg.target() instanceof Room))
		{
			for(final CMMsg msg : leaveTrailersSoFar)
				((Room)leaveMsg.target()).send(mob,msg);
		}
		if((enterTrailersSoFar!=null)&&(enterMsg.target() instanceof Room))
		{
			for(final CMMsg msg : enterTrailersSoFar)
				((Room)enterMsg.target()).send(mob,msg);
		}
		return true;
	}

	@Override
	public boolean walk(final MOB mob, final int directionCode, final boolean flee, final boolean nolook)
	{
		return walk(mob,directionCode,flee,nolook,false);
	}

	@Override
	public boolean walk(final Item I, final int directionCode)
	{
		if(I==null)
			return false;
		final Room thisRoom=CMLib.map().roomLocation(I);
		if(thisRoom==null)
			return false;
		final Room thatRoom = thisRoom.getRoomInDir(directionCode);
		final Exit E=thisRoom.getExitInDir(directionCode);
		if((thatRoom==null)||(E==null))
			return false;
		List<Rider> riders=null;
		if(I instanceof Rideable)
		{
			riders=new XVector<Rider>(((Rideable)I).riders());
			final Exit opExit=thatRoom.getReverseExit(directionCode);
			for(int i=0;i<riders.size();i++)
			{
				final Rider R=riders.get(i);
				if(R instanceof MOB)
				{
					final MOB mob=(MOB)R;
					mob.setRiding((Rideable)I);
					// overboard check
					if(mob.isMonster()
					&& mob.isSavable()
					&& (mob.location() != thisRoom)
					&& CMLib.flags().isInTheGame(mob,true))
						thisRoom.bringMobHere(mob,false);

					final CMMsg enterMsg=CMClass.getMsg(mob,thatRoom,E,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
					final CMMsg leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null);
					leaveMsg.setValue(directionCode+1);
					enterMsg.setValue(thisRoom.getReverseDir(directionCode)+1);
					if(!E.okMessage(mob,enterMsg))
					{
						return false;
					}
					else
					if((opExit!=null)&&(!opExit.okMessage(mob,leaveMsg)))
					{
						return false;
					}
					else
					if(!enterMsg.target().okMessage(mob,enterMsg))
					{
						return false;
					}
					else
					if(!mob.okMessage(mob,enterMsg))
					{
						return false;
					}
				}
			}
		}

		thisRoom.showHappens(CMMsg.MSG_OK_ACTION,I,L("<S-NAME> goes @x1.",CMLib.directions().getDirectionName(directionCode)));
		thatRoom.moveItemTo(I);
		if(I.owner()==thatRoom)
		{
			thatRoom.showHappens(CMMsg.MSG_OK_ACTION,I,L("<S-NAME> arrives from @x1.",CMLib.directions().getFromCompassDirectionName(Directions.getOpDirectionCode(directionCode))));
			if(riders!=null)
			{
				for(int i=0;i<riders.size();i++)
				{
					final Rider R=riders.get(i);
					if(CMLib.map().roomLocation(R)!=thatRoom)
					{
						if((((Rideable)I).rideBasis()!=Rideable.Basis.FURNITURE_SIT)
						&&(((Rideable)I).rideBasis()!=Rideable.Basis.FURNITURE_TABLE)
						&&(((Rideable)I).rideBasis()!=Rideable.Basis.FURNITURE_HOOK)
						&&(((Rideable)I).rideBasis()!=Rideable.Basis.ENTER_IN)
						&&(((Rideable)I).rideBasis()!=Rideable.Basis.FURNITURE_SLEEP)
						&&(((Rideable)I).rideBasis()!=Rideable.Basis.LADDER))
						{
							if((R instanceof MOB)
							&&(CMLib.flags().isInTheGame((MOB)R,true)))
							{
								thatRoom.bringMobHere((MOB)R,true);
								((MOB)R).setRiding((Rideable)I);
								CMLib.commands().postLook((MOB)R,true);
								thatRoom.show((MOB)R,thatRoom,E,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,null);
							}
							else
							if(R instanceof Item)
							{
								thatRoom.moveItemTo((Item)R);
							}
						}
						else
							R.setRiding(null);
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int findExitDir(final MOB mob, final Room R, final String desc)
	{
		int dir=CMLib.directions().getGoodDirectionCode(desc);
		if(dir<0)
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Exit e=R.getExitInDir(d);
			final Room r=R.getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((CMLib.flags().canBeSeenBy(e,mob))
				&&((e.name().equalsIgnoreCase(desc))
				||(e.displayText().equalsIgnoreCase(desc))
				||(r.displayText(mob).equalsIgnoreCase(desc))
				||(e.description().equalsIgnoreCase(desc))))
				{
					dir=d; break;
				}
			}
		}
		if(dir<0)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Exit e=R.getExitInDir(d);
				final Room r=R.getRoomInDir(d);
				if((e!=null)&&(r!=null))
				{
					if((CMLib.flags().canBeSeenBy(e,mob))
					&&(((CMLib.english().containsString(e.name(),desc))
					||(CMLib.english().containsString(e.displayText(),desc))
					||(CMLib.english().containsString(r.displayText(),desc))
					||(CMLib.english().containsString(e.description(),desc)))))
					{
						dir=d; break;
					}
				}
			}
		}
		return dir;
	}

	@Override
	public int findRoomDir(final MOB mob, final Room R)
	{
		if((mob==null)||(R==null))
			return -1;
		final Room R2=mob.location();
		if(R2==null)
			return -1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(R2.getRoomInDir(d)==R)
				return d;
		}
		return -1;
	}

	@Override
	public List<Integer> getShortestTrail(final List<List<Integer>> finalSets)
	{
		if((finalSets==null)||(finalSets.size()==0))
			return null;
		List<Integer> shortest=finalSets.get(0);
		for(int i=1;i<finalSets.size();i++)
		{
			if((finalSets.get(i).size()<shortest.size())
			&&(finalSets.get(i).size()>0))
				shortest=finalSets.get(i);
		}
		return shortest;
	}

	@Override
	public List<List<Integer>> findAllTrails(final Room from, final Room to, final List<Room> radiantTrail)
	{
		final List<List<Integer>> finalSets=new Vector<List<Integer>>();
		if((from==null)||(to==null)||(from==to))
			return finalSets;
		final int index=radiantTrail.indexOf(to);
		if(index<0)
			return finalSets;
		Room R=null;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R=to.getRoomInDir(d);
			if(R!=null)
			{
				if((R==from)&&(from.getRoomInDir(Directions.getOpDirectionCode(d))==to))
				{
					finalSets.add(new XVector<Integer>(Integer.valueOf(Directions.getOpDirectionCode(d))));
					return finalSets;
				}
				final int dex=radiantTrail.indexOf(R);
				if((dex>=0)&&(dex<index)&&(R.getRoomInDir(Directions.getOpDirectionCode(d))==to))
				{
					final List<List<Integer>> allTrailsBack=findAllTrails(from,R,radiantTrail);
					for(int a=0;a<allTrailsBack.size();a++)
					{
						final List<Integer> thisTrail=allTrailsBack.get(a);
						thisTrail.add(Integer.valueOf(Directions.getOpDirectionCode(d)));
						finalSets.add(thisTrail);
					}
				}
			}
		}
		return finalSets;
	}

	@Override
	public List<List<Integer>> findAllTrails(final Room from, final List<Room> tos, final List<Room> radiantTrail)
	{
		final List<List<Integer>> finalSets=new Vector<List<Integer>>();
		if(from==null)
			return finalSets;
		Room to=null;
		for(int t=0;t<tos.size();t++)
		{
			to=tos.get(t);
			finalSets.addAll(findAllTrails(from,to,radiantTrail));
		}
		return finalSets;
	}

	protected int getRoomDirection(final Room R, final Room toRoom, final List<Room> ignore)
	{
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if((R.getRoomInDir(d)==toRoom)
			&&(R!=toRoom)
			&&((ignore==null)||(!ignore.contains(R))))
				return d;
		}
		return -1;
	}

	protected Room getWhere(final String where, final List<Room> set)
	{
		Room R2=CMLib.map().getRoom(where);
		if(R2 == null)
		{
			for(int i=0;i<set.size();i++)
			{
				final Room R=set.get(i);
				if((""+R).equals(where))
				{
					R2=R;
					break;
				}
			}
		}
		if(R2==null)
		{
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if(A.name().equalsIgnoreCase(where))
				{
					if(set.size()==0)
					{
						int lowest=Integer.MAX_VALUE;
						for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
						{
							final Room R=r.nextElement();
							final int x=R.roomID().indexOf('#');
							if((x>=0)&&(CMath.s_int(R.roomID().substring(x+1))<lowest))
								lowest=CMath.s_int(R.roomID().substring(x+1));
						}
						if(lowest<Integer.MAX_VALUE)
							R2=CMLib.map().getRoom(A.name()+"#"+lowest);
					}
					else
					{
						for(int i=0;i<set.size();i++)
						{
							final Room R=set.get(i);
							if(R.getArea()==A)
							{
								R2=R;
								break;
							}
						}
					}
					break;
				}
			}
		}
		return R2;
	}

	protected int getIndexEnsureSet(final Room R1, final Room R2, final List<Room> set, final int radius, final Set<Room> ignoreRooms)
	{
		final TrackingLibrary.TrackingFlags flags = new DefaultTrackingFlags()
		.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
		if(set.size()==0)
			getRadiantRooms(R1,set,flags,R2,radius,ignoreRooms);
		int foundAt=-1;
		for(int i=0;i<set.size();i++)
		{
			final Room R=set.get(i);
			if(R==R2)
			{
				foundAt=i;
				break;
			}
		}
		return foundAt;
	}

	@Override
	public boolean canValidTrail(final Room startR, final List<Room> radiantV, final String where, final int radius,
								 final Set<Room> ignoreRooms, final int maxSecs)
	{
		final Room R2=getWhere(where,radiantV);
		if(R2==null)
			return false;
		int foundAt=getIndexEnsureSet(startR,R2,radiantV,radius,ignoreRooms);
		if(foundAt<0)
			return false;
		Room checkR=R2;
		final List<Room> trailV=new ArrayList<Room>();
		trailV.add(R2);
		final HashSet<Area> areasDone=new HashSet<Area>();
		boolean didSomething=false;
		final long startTime = System.currentTimeMillis();
		while(checkR!=startR)
		{
			final long waitTime = System.currentTimeMillis() - startTime;
			if(waitTime > (1000 * (maxSecs)))
				return false;
			didSomething=false;
			for(int r=foundAt-1;r>=0;r--)
			{
				final Room R=radiantV.get(r);
				if(getRoomDirection(R,checkR,trailV)>=0)
				{
					trailV.add(R);
					if(!areasDone.contains(R.getArea()))
						areasDone.add(R.getArea());
					foundAt=r;
					checkR=R;
					didSomething=true;
					break;
				}
			}
			if(!didSomething)
				return false;
		}
		return true;
	}

	@Override
	public String getTrailToDescription(final Room startR, final List<Room> radiantV, final String where,
										final Set<TrailFlag> trailFlags, final int radius, final Set<Room> ignoreRooms,
										String delimeter, final int maxSecs)
	{
		if(delimeter==null)
			delimeter=" ";
		final Room R2=getWhere(where,radiantV);
		if(R2==null)
			return L("Unable to determine '@x1'.",where);
		int foundAt=getIndexEnsureSet(startR,R2,radiantV,radius,ignoreRooms);
		if(foundAt<0)
			return L("You can't get to '@x1' from here.",R2.roomID());
		final boolean confirm = trailFlags != null && trailFlags.contains(TrailFlag.CONFIRM);
		final boolean areaNames = trailFlags != null && trailFlags.contains(TrailFlag.AREANAMES);
		Room checkR=R2;
		final List<Room> trailV=new ArrayList<Room>();
		trailV.add(R2);
		final HashSet<Area> areasDone=new HashSet<Area>();
		boolean didSomething=false;
		final long startTime = System.currentTimeMillis();
		while(checkR!=startR)
		{
			final long waitTime = System.currentTimeMillis() - startTime;
			if(waitTime > (1000 *(maxSecs)))
				return L("You can't get there from here.");
			didSomething=false;
			for(int r=foundAt-1;r>=0;r--)
			{
				final Room R=radiantV.get(r);
				if(getRoomDirection(R,checkR,trailV)>=0)
				{
					trailV.add(R);
					if(!areasDone.contains(R.getArea()))
						areasDone.add(R.getArea());
					foundAt=r;
					checkR=R;
					didSomething=true;
					break;
				}
			}
			if(!didSomething)
				return L("You can't get there from here.");
		}
		final List<String> theDirTrail=new ArrayList<String>();
		final List<Room> empty=new ReadOnlyVector<Room>();
		for(int s=trailV.size()-1;s>=1;s--)
		{
			final Room R=trailV.get(s);
			final Room RA=trailV.get(s-1);
			theDirTrail.add(CMLib.directions().getDirectionChar(getRoomDirection(R,RA,empty)));
		}
		final StringBuffer theTrail=new StringBuffer("");
		if(confirm)
			theTrail.append("\n\r"+CMStrings.padRight(L("Trail"),30)+": ");
		String lastDir="";
		int lastNum=0;
		while(theDirTrail.size()>0)
		{
			final String s=theDirTrail.get(0);
			if(lastNum==0)
			{
				lastDir=s;
				lastNum=1;
			}
			else
			if(s.equalsIgnoreCase(lastDir))
				lastNum++;
			else
			{
				if(lastNum==1)
					theTrail.append(lastDir+delimeter);
				else
					theTrail.append(Integer.toString(lastNum)+lastDir+delimeter);
				lastDir=s;
				lastNum=1;
			}
			theDirTrail.remove(0);
		}
		if(lastNum==1)
			theTrail.append(lastDir);
		else
		if(lastNum>0)
			theTrail.append(Integer.toString(lastNum)+lastDir);
		if((theTrail.length()>delimeter.length())
		&&(theTrail.substring(theTrail.length()-delimeter.length()).equals(delimeter)))
			theTrail.delete(theTrail.length()-delimeter.length(), theTrail.length());
		if((confirm)&&(trailV.size()>1))
		{
			for(int i=0;i<trailV.size();i++)
			{
				final Room R=trailV.get(i);
				if(R.roomID().length()==0)
				{
					theTrail.append("*");
					break;
				}
			}
			final Room R=trailV.get(1);
			theTrail.append("\n\r"+CMStrings.padRight(L("From"),30)+": "+CMLib.directions().getDirectionName(getRoomDirection(R,R2,empty))+" <- "+R.roomID());
			theTrail.append("\n\r"+CMStrings.padRight(L("Room"),30)+": "+R.displayText()+"/"+R.description());
			theTrail.append("\n\r\n\r");
		}
		if((areaNames)&&(areasDone.size()>0))
		{
			theTrail.append("\n\r"+CMStrings.padLeft(L("Areas crossed"),30)+":");
			for (final Area A : areasDone)
			{
				theTrail.append(" \""+A.name()+"\",");
			}
		}
		if(theTrail.toString().trim().length()==0)
			return L("You can't get there from here.");
		return theTrail.toString();
	}

	@Override
	public Rideable findALadder(final MOB mob, final Room room)
	{
		if(room==null)
			return null;
		if(mob.riding()!=null)
			return null;
		for(int i=0;i<room.numItems();i++)
		{
			final Item I=room.getItem(i);
			if((I!=null)
			&&(I instanceof Rideable)
			&&(CMLib.flags().canBeSeenBy(I,mob))
			&&(((Rideable)I).rideBasis()==Rideable.Basis.LADDER))
				return (Rideable)I;
		}
		return null;
	}

	@Override
	public void postMountLadder(final MOB mob, final Rideable ladder)
	{
		final String mountStr=ladder.mountString(CMMsg.TYP_MOUNT,mob);
		final CMMsg msg=CMClass.getMsg(mob,ladder,null,CMMsg.MSG_MOUNT,L("<S-NAME> @x1 <T-NAMESELF>.",mountStr));
		Room room=(Room)((Item)ladder).owner();
		if(mob.location()==room)
			room=null;
		if((mob.location().okMessage(mob,msg))
		&&((room==null)||(room.okMessage(mob,msg))))
		{
			mob.location().send(mob,msg);
			if(room!=null)
				room.sendOthers(mob,msg);
		}
	}

	@Override
	public boolean makeFall(final Physical P, final Room room, final boolean reverseFall)
	{
		if((P==null)||(room==null))
			return false;

		if((!reverseFall)&&(room.getRoomInDir(Directions.DOWN)==null))
			return false;

		if((reverseFall)&&(room.getRoomInDir(Directions.UP)==null))
			return false;

		if(((P instanceof MOB)&&(!CMLib.flags().isInFlight(P)))
		||((P instanceof Item)
			&&(((Item)P).container()==null)
			&&(!CMLib.flags().isFlying(((Item)P).ultimateContainer(null)))))
		{
			if(!CMLib.flags().isFalling(P))
			{
				final Ability falling=CMClass.getAbility("Falling");
				if(falling!=null)
				{
					falling.setMiscText(reverseFall?"REVERSED":"NORMAL");
					falling.setAffectedOne(room);
					falling.invoke(null,null,P,true,0);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public CheckedMsgResponse isOkWaterSurfaceAffect(final Room room, final CMMsg msg)
	{
		if(CMLib.flags().isSleeping(room))
			return CheckedMsgResponse.CONTINUE;

		if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_FLEE))
		&&(msg.amITarget(room))
		&&(msg.sourceMinor()!=CMMsg.TYP_RECALL)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)
		   ||(!(msg.tool() instanceof Ability))
		   ||(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
		&&(!CMLib.flags().isFalling(msg.source()))
		&&(!CMLib.flags().isInFlight(msg.source()))
		&&(!CMLib.flags().isWaterWorthy(msg.source())))
		{
			final MOB mob=msg.source();
			boolean hasBoat=false;
			if((msg.tool() instanceof Exit)
			&&(msg.targetMinor()==CMMsg.TYP_LEAVE))
			{
				final int dir=CMLib.map().getExitDir(room, (Exit)msg.tool());
				if(dir >=0)
				{
					final Room R=room.getRoomInDir(dir);
					if((R!=null)
					&&(R.getArea() instanceof Boardable))
						hasBoat=true;
				}
			}

			for(int i=0;i<mob.numItems();i++)
			{
				final Item I=mob.getItem(i);
				if((I!=null)&&(I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.Basis.WATER_BASED))
				{
					hasBoat = true;
					break;
				}
			}
			if((!CMLib.flags().isWaterWorthy(mob))
			&&(!hasBoat)
			&&(!CMLib.flags().isInFlight(mob)))
			{
				mob.tell(CMLib.lang().L("You need to swim or ride a boat that way."));
				return CheckedMsgResponse.CANCEL;
			}
			else
			if(CMLib.flags().isSwimming(mob))
			{
				if(mob.phyStats().weight()>Math.round(CMath.mul(mob.maxCarry(),0.50)))
				{
					mob.tell(CMLib.lang().L("You are too encumbered to swim."));
					return CheckedMsgResponse.CANCEL;
				}
			}
		}
		else
		if(((msg.sourceMinor()==CMMsg.TYP_SIT)||(msg.sourceMinor()==CMMsg.TYP_SLEEP))
		&&(!(msg.target() instanceof Exit))
		&&(!CMLib.flags().canBreatheThis(msg.source(), RawMaterial.RESOURCE_SALTWATER))
		&&(!CMLib.flags().canBreatheThis(msg.source(), RawMaterial.RESOURCE_FRESHWATER))
		&&((msg.source().riding()==null)||(!CMLib.flags().isSwimming(msg.source().riding()))))
		{
			msg.source().tell(CMLib.lang().L("You cannot rest here."));
			return CheckedMsgResponse.CANCEL;
		}
		else
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			if(((Drink)room).liquidType()==RawMaterial.RESOURCE_SALTWATER)
			{
				msg.source().tell(CMLib.lang().L("You don't want to be drinking saltwater."));
				return CheckedMsgResponse.CANCEL;
			}
			return CheckedMsgResponse.FORCEDOK;
		}
		return CheckedMsgResponse.CONTINUE;
	}

	@Override
	public void makeSink(final Physical P, final Room room, final boolean reverseSink)
	{
		if((P==null)||(room==null))
			return;

		final Room R=(reverseSink) ?
				room.getRoomInDir(Directions.UP) :
				room.getRoomInDir(Directions.DOWN);
		if((R==null)
		||((R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)))
			return;

		if(((P instanceof MOB)
			&&(!CMLib.flags().isWaterWorthy(P))
			&&(!CMLib.flags().isInFlight(P))
			&&(P.phyStats().weight()>=1))
		||((P instanceof Item)
			&&(!CMLib.flags().isInFlight(((Item)P).ultimateContainer(null)))
			&&(!CMLib.flags().isWaterWorthy(((Item)P).ultimateContainer(null)))))
		{
			if(P.fetchEffect("Sinking")==null)
			{
				final Ability sinking=CMClass.getAbility("Sinking");
				if(sinking!=null)
				{
					sinking.setMiscText(reverseSink?"REVERSED":"NORMAL");
					sinking.setAffectedOne(room);
					sinking.invoke(null,null,P,true,0);
				}
			}
		}
	}

	@Override
	public void forceRecall(final MOB mob, final boolean includeFollowers)
	{
		if(mob == null)
			return;
		final Room currentRoom=mob.location();
		Room recallRoom=CMLib.map().getStartRoom(mob);
		if((recallRoom==null)&&(!mob.isMonster()))
		{
			mob.setStartRoom(CMLib.login().getDefaultStartRoom(mob));
			recallRoom=CMLib.map().getStartRoom(mob);
		}
		if((currentRoom == recallRoom)
		||(recallRoom == null))
			return;
		if(currentRoom != null)
		{
			final LinkedList<MOB> travellers=new LinkedList<MOB>();
			travellers.add(mob);
			if(includeFollowers)
			{
				for(int f=0;f<mob.numFollowers();f++)
				{
					final MOB follower=mob.fetchFollower(f);

					if((follower!=null)
					&&(follower.isMonster())
					&&(!follower.isPossessing())
					&&(CMLib.flags().isInTheGame(follower,true))
					&&(!follower.isAttributeSet(MOB.Attrib.AUTOGUARD))
					&&(follower.location()==currentRoom))
						travellers.add(follower);
				}
			}
			for(final Iterator<MOB> m=travellers.iterator();m.hasNext();)
			{
				final MOB M=m.next();
				if(M.isInCombat())
					CMLib.commands().postFlee(M,("NOWHERE"));
				final CMMsg msg=CMClass.getMsg(M,currentRoom,null,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,L("<S-NAME> disappear(s) into the Java Plane!"));
				currentRoom.okMessage(M, msg);
				currentRoom.send(M,msg);
				final CMMsg msg2=CMClass.getMsg(M,recallRoom,null,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,null);
				recallRoom.okMessage(M, msg2);
				((Room)msg2.target()).send(M,msg2);
				if(((Room)msg2.target()) != currentRoom)
				{
					if(currentRoom.isInhabitant(M))
						currentRoom.delInhabitant(M);
					if(!((Room)msg2.target()).isInhabitant(M))
						((Room)msg2.target()).bringMobHere(M,M.isMonster());
				}
			}
		}
	}

	@Override
	public PairVector<Room,int[]> buildGridList(final Room room, final String ownerName, final int maxDepth)
	{
		int depth=0;
		final PairVector<Room,int[]> rooms = new PairVector<Room,int[]>();
		if(room==null)
			return rooms;
		if(rooms.containsFirst(room))
			return rooms;
		final Set<Room> H=new HashSet<Room>(1000);
		H.add(room);
		rooms.add(new Pair<Room,int[]>(room,new int[]{0,0,0}));
		int min=0;
		int size=rooms.size();
		int[] coords=null;
		Room R1=null;
		Room R=null;
		LandTitle T = null;

		int r=0;
		int d=0;
		while(depth<maxDepth)
		{
			for(r=min;r<size;r++)
			{
				R1=rooms.get(r).first;
				coords=rooms.get(r).second;
				for(d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					R=R1.getRoomInDir(d); // exit doesn't matter because walls
					if(R!=null)
						T=CMLib.law().getLandTitle(R);
					if((R==null)
					||(T==null)
					||((ownerName!=null)&&(!T.getOwnerName().equalsIgnoreCase(ownerName)))
					||(H.contains(R))
					||(R.roomID().length()==0))
						continue;
					rooms.add(new Pair<Room,int[]>(R,Directions.adjustXYZByDirections(coords[0], coords[1], coords[2], d)));
					H.add(R);
				}
			}
			min=size;
			size=rooms.size();
			if(min==size)
				return rooms;
			depth++;
		}
		return rooms;
	}

	@Override
	public Room getCalculatedAdjacentGridRoom(final PairVector<Room,int[]> rooms, final Room R, final int dir)
	{
		final int[] lookForCoords = Directions.adjustXYZByDirections(0, 0, 0, dir);
		for(int i=0;i<rooms.size();i++)
		{
			if((Arrays.equals(lookForCoords, rooms.getSecond(i)))
			&&(rooms.getFirst(i).rawDoors()[Directions.getOpDirectionCode(dir)]==null))
				return rooms.getFirst(i);
		}
		return null;
	}

	@Override
	public void initializeClass()
	{
		for(final TrackingFlag tf : TrackingFlag.values())
		{
			switch(tf)
			{
			case NOHOMES: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return CMLib.law().getLandTitle(R) != null;
					}
				};
				break;
			case OPENONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (!E.isOpen()) || ((E.phyStats().disposition()&PhyStats.IS_UNHELPFUL)>0);
					}
				};
				break;
			case UNLOCKEDONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (E.hasALock() && E.isLocked()) || ((E.phyStats().disposition()&PhyStats.IS_UNHELPFUL)>0);
					}
				};
				break;
			case PASSABLE: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (E.phyStats().disposition()&PhyStats.IS_UNHELPFUL)>0;
					}
				};
				break;
			case AREAONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R!=null)&&(hostR!=null)&&(hostR.getArea()!=R.getArea());
					}
				};
				break;
			case NOTHINAREAS: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R!=null)&&(hostR!=null)&&(R.getArea()!=null)
						&&(CMath.bset(R.getArea().flags(), Area.FLAG_THIN));
					}
				};
				break;
			case NOHIDDENAREAS: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return ((R!=null)&&(CMLib.flags().isHidden(R)))
							|| ((R!=null)&&(R.getArea()!=null)&&(CMLib.flags().isHidden(R.getArea())));
					}
				};
				break;
			case NOEMPTYGRIDS: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R.getGridParent() != null) && (R.getGridParent().roomID().length() == 0);
					}
				};
				break;
			case NOAIR: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R.domainType() == Room.DOMAIN_INDOORS_AIR) || (R.domainType() == Room.DOMAIN_OUTDOORS_AIR);
					}
				};
				break;
			case NOPRIVATEPROPERTY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						final LegalLibrary law=CMLib.law();
						final LandTitle title = law.getLandTitle(R);
						return  (title == null) || (title.getOwnerName() == null) || (title.getOwnerName().length()==0);
					}
				};
				break;
			case NOWATER: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return CMLib.flags().isWateryRoom(R);
					}
				};
				break;
			case WATERSURFACEONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return !CMLib.flags().isWaterySurfaceRoom(R);
					}
				};
				break;
			case DRIVEABLEONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return !CMLib.flags().isDrivableRoom(R);
					}
				};
				break;
			case WATERSURFACEORSHOREONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						if(R==null)
							return true;
						if((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(R.domainType()==Room.DOMAIN_INDOORS_AIR))
							return true;
						if(CMLib.flags().isWaterySurfaceRoom(R)
						|| (R.ID().equals("Shore"))
						|| (R.domainType() == Room.DOMAIN_OUTDOORS_SEAPORT)
						|| (R.domainType() == Room.DOMAIN_INDOORS_SEAPORT)
						|| (R.domainType() == Room.DOMAIN_INDOORS_CAVE_SEAPORT))
							return false;
						boolean foundWater=false;
						for(final int dir2 : Directions.CODES())
						{
							final Room R2=R.getRoomInDir(dir2);
							if((R2!=null)&&(CMLib.flags().isWaterySurfaceRoom(R2)))
								foundWater=true;
						}
						return (!foundWater);
					}
				};
				break;
			case SHOREONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						if(R==null)
							return true;
						if((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						|| (R.domainType()==Room.DOMAIN_INDOORS_AIR)
						|| (CMLib.flags().isWaterySurfaceRoom(R) ))
							return true;
						if((R.ID().equals("Shore"))
						|| (R.domainType() == Room.DOMAIN_OUTDOORS_SEAPORT)
						|| (R.domainType() == Room.DOMAIN_INDOORS_SEAPORT)
						|| (R.domainType() == Room.DOMAIN_INDOORS_CAVE_SEAPORT))
							return false;
						boolean foundWater=false;
						for(final int dir2 : Directions.CODES())
						{
							final Room R2=R.getRoomInDir(dir2);
							if((R2!=null)&&(CMLib.flags().isWaterySurfaceRoom(R2)))
								foundWater=true;
						}
						return (!foundWater);
					}
				};
				break;
			case UNDERWATERONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return !CMLib.flags().isUnderWateryRoom(R);
					}
				};
				break;
			case FLOORSONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R.getRoomInDir(Directions.DOWN)!=null);
					}
				};
				break;
			case CEILINGSSONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R.getRoomInDir(Directions.UP)!=null);
					}
				};
				break;
			case NOCLIMB: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (CMLib.flags().isClimbing(R) || CMLib.flags().isClimbing(E));
					}
				};
				break;
			case NOCRAWL: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (CMLib.flags().isCrawlable(R) || CMLib.flags().isCrawlable(E));
					}
				};
				break;
			case OUTDOORONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R.domainType() & Room.INDOORS) != 0;
					}
				};
				break;
			case INDOORONLY: tf.myFilter=new RFilter()
				{
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						return (R.domainType() & Room.INDOORS) == 0;
					}
				};
				break;
			}
		}
	}
}
