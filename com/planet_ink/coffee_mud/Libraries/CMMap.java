package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMLib.Library;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.collections.MultiEnumeration.MultiEnumeratorBuilder;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.interfaces.TickableGroup.LocalType;
import com.planet_ink.coffee_mud.core.interfaces.LandTitle;
import com.planet_ink.coffee_mud.core.interfaces.MsgListener;
import com.planet_ink.coffee_mud.core.interfaces.PrivateProperty;
import com.planet_ink.coffee_mud.core.interfaces.SpaceObject;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.GridLocale.CrossExit;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;
/*
   Copyright 2001-2024 Bo Zimmerman

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
public class CMMap extends StdLibrary implements WorldMap
{
	@Override
	public String ID()
	{
		return "CMMap";
	}

	protected static MOB				deityStandIn			= null;
	protected long						lastVReset				= 0;
	protected CMNSortSVec<Area>			areasList				= new CMNSortSVec<Area>();
	protected CMNSortSVec<Deity>		deitiesList				= new CMNSortSVec<Deity>();
	protected List<Boardable>			shipList				= new SVector<Boardable>();
	protected Map<String, Object>		SCRIPT_HOST_SEMAPHORES	= new Hashtable<String, Object>();
	protected Map<String, TimeClock>	clockCache 				= new Hashtable<String, TimeClock>();

	private static final long EXPIRE_30MINS	= 30*60*1000;
	private static final long EXPIRE_1HOUR	= 60*60*1000;

	protected static final Comparator<Area>	areaComparator = new Comparator<Area>()
	{
		@Override
		public int compare(final Area o1, final Area o2)
		{
			if(o1==null)
				return (o2==null)?0:-1;
			return o1.Name().compareToIgnoreCase(o2.Name());
		}
	};

	public Map<Integer,List<WeakReference<MsgListener>>>
								globalHandlers   		= new SHashtable<Integer,List<WeakReference<MsgListener>>>();
	public Map<String,SLinkedList<LocatedPair>>
								scriptHostMap			= new STreeMap<String,SLinkedList<LocatedPair>>();

	private static class LocatedPairImpl implements LocatedPair
	{
		final WeakReference<Room> roomW;
		final WeakReference<PhysicalAgent> objW;

		private LocatedPairImpl(final Room room, final PhysicalAgent host)
		{
			roomW = new WeakReference<Room>(room);
			objW = new WeakReference<PhysicalAgent>(host);
		}

		@Override
		public Room room()
		{
			return roomW.get();
		}

		@Override
		public PhysicalAgent obj()
		{
			return objW.get();
		}
	}

	private static Filterer<Area> nonSpaceAreaFilter=new Filterer<Area>()
	{
		@Override
		public boolean passesFilter(final Area obj)
		{
			return !(obj instanceof SpaceObject);
		}
	};

	private static Filterer<Area> topLevelAreaFilter=new Filterer<Area>()
	{
		@Override
		public boolean passesFilter(final Area obj)
		{
			return ! obj.getParents().hasMoreElements();
		}
	};

	protected int getGlobalIndex(final List<Environmental> list, final String name)
	{
		if(list.size()==0)
			return -1;
		int start=0;
		int end=list.size()-1;
		while(start<=end)
		{
			final int mid=(end+start)/2;
			try
			{
				final int comp=list.get(mid).Name().compareToIgnoreCase(name);
				if(comp==0)
					return mid;
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
			}
			catch(final java.lang.ArrayIndexOutOfBoundsException e)
			{
				start=0;
				end=list.size()-1;
			}
		}
		return -1;
	}

	@Override
	public void renamedArea(final Area theA)
	{
		areasList.reSort(theA);
		final Map<String,Area> finder=getAreaFinder();
		finder.clear();
	}

	// areas
	@Override
	public int numAreas()
	{
		return areasList.size();
	}

	@Override
	public void addArea(final Area newOne)
	{
		areasList.add(newOne);
		if((newOne instanceof SpaceObject)&&(!CMLib.space().isObjectInSpace((SpaceObject)newOne)))
			CMLib.space().addObjectToSpace((SpaceObject)newOne);
	}

	@Override
	public void delArea(final Area oneToDel)
	{
		areasList.remove(oneToDel);
		if((oneToDel instanceof SpaceObject)&&(CMLib.space().isObjectInSpace((SpaceObject)oneToDel)))
			CMLib.space().delObjectInSpace((SpaceObject)oneToDel);
		Resources.removeResource("SYSTEM_AREA_FINDER_CACHE");
	}

	@Override
	public Map<String,TimeClock> getClockCache()
	{
		return this.clockCache;
	}

	@Override
	public Area getModelArea(final Area A)
	{
		if(A!=null)
		{
			if(CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD))
			{
				final int x=A.Name().indexOf('_');
				if((x>0)
				&&(CMath.isInteger(A.Name().substring(0,x))))
				{
					final Area A2=getArea(A.Name().substring(x+1));
					if(A2!=null)
						return A2;
				}
			}
		}
		return A;
	}

	@SuppressWarnings("unchecked")
	protected Map<String,Area> getAreaFinder()
	{
		Map<String,Area> finder=(Map<String,Area>)Resources.getResource("SYSTEM_AREA_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,Area>(50,EXPIRE_30MINS,EXPIRE_1HOUR,100);
			Resources.submitResource("SYSTEM_AREA_FINDER_CACHE",finder);
		}
		return finder;
	}

	@Override
	public Area getArea(final String calledThis)
	{
		final Map<String,Area> finder=getAreaFinder();
		Area A=finder.get(calledThis.toLowerCase());
		if((A!=null)&&(!A.amDestroyed()))
			return A;
		final SearchIDList<Area> list;
		synchronized(areasList)
		{
			list = areasList;
		}
		A=list.find(calledThis);
		if((A!=null)&&(!A.amDestroyed()))
		{
			if(!CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE))
				finder.put(calledThis.toLowerCase(), A);
			return A;
		}
		return null;
	}

	@Override
	public Area findAreaStartsWith(final String calledThis)
	{
		final boolean disableCaching=CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE);
		Area A=getArea(calledThis);
		if(A!=null)
			return A;
		final Map<String,Area> finder=getAreaFinder();
		A=finder.get(calledThis.toLowerCase());
		if((A!=null)&&(!A.amDestroyed()))
			return A;
		for(final Enumeration<Area> a=areas();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A.Name().toUpperCase().startsWith(calledThis))
			{
				if(!disableCaching)
					finder.put(calledThis.toLowerCase(), A);
				return A;
			}
		}
		return null;
	}

	@Override
	public Area findArea(final String calledThis)
	{
		final boolean disableCaching=CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE);
		Area A=findAreaStartsWith(calledThis);
		if(A!=null)
			return A;
		final Map<String,Area> finder=getAreaFinder();
		A=finder.get(calledThis.toLowerCase());
		if((A!=null)&&(!A.amDestroyed()))
			return A;
		for(final Enumeration<Area> a=areas();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(CMLib.english().containsString(A.Name(),calledThis))
			{
				if(!disableCaching)
					finder.put(calledThis.toLowerCase(), A);
				return A;
			}
		}
		return null;
	}

	@Override
	public Enumeration<Area> areas()
	{
		return new IteratorEnumeration<Area>(areasList.iterator());
	}

	@Override
	public Enumeration<Area> areasPlusShips()
	{
		final MultiEnumeration<Area> m=new MultiEnumeration<Area>(new IteratorEnumeration<Area>(areasList.iterator()));
		m.addEnumeration(shipAreaEnumerator(null));
		return m;
	}

	@Override
	public Enumeration<Area> mundaneAreas()
	{
		return new FilteredEnumeration<Area>(areas(),nonSpaceAreaFilter);
	}

	@Override
	public Enumeration<Area> topAreas()
	{
		return new FilteredEnumeration<Area>(areas(),topLevelAreaFilter);
	}

	@Override
	public Enumeration<String> roomIDs()
	{
		return new Enumeration<String>()
		{
			private volatile Enumeration<String> roomIDEnumerator=null;
			private volatile Enumeration<Area> areaEnumerator=areasPlusShips();

			@Override
			public boolean hasMoreElements()
			{
				boolean hasMore = (roomIDEnumerator != null) && roomIDEnumerator.hasMoreElements();
				while(!hasMore)
				{
					if((areaEnumerator == null)||(!areaEnumerator.hasMoreElements()))
					{
						roomIDEnumerator=null;
						areaEnumerator = null;
						return false;
					}
					final Area A=areaEnumerator.nextElement();
					roomIDEnumerator=A.getProperRoomnumbers().getRoomIDs();
					hasMore = (roomIDEnumerator != null) && roomIDEnumerator.hasMoreElements();
				}
				return hasMore;
			}

			@Override
			public String nextElement()
			{
				return hasMoreElements() ? (String) roomIDEnumerator.nextElement() : null;
			}
		};
	}

	@Override
	public Area getFirstArea()
	{
		if (areas().hasMoreElements())
			return areas().nextElement();
		return null;
	}

	@Override
	public Area getDefaultParentArea()
	{
		final String defaultParentAreaName=CMProps.getVar(CMProps.Str.DEFAULTPARENTAREA);
		if((defaultParentAreaName!=null)&&(defaultParentAreaName.trim().length()>0))
			return getArea(defaultParentAreaName.trim());
		return null;
	}

	@Override
	public Area getRandomArea()
	{
		Area A=null;
		while((numAreas()>0)&&(A==null))
		{
			try
			{
				A=areasList.get(CMLib.dice().roll(1,numAreas(),-1));
			}
			catch(final ArrayIndexOutOfBoundsException e)
			{
			}
		}
		return A;
	}

	@Override
	public void addGlobalHandler(final MsgListener E, final int category)
	{
		if(E==null)
			return;
		List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
		if(V==null)
		{
			V=new SLinkedList<WeakReference<MsgListener>>();
			globalHandlers.put(Integer.valueOf(category),V);
		}
		synchronized(V)
		{
			for (final WeakReference<MsgListener> W : V)
			{
				if(W.get()==E)
					return;
			}
			V.add(new WeakReference<MsgListener>(E));
		}
	}

	@Override
	public void delGlobalHandler(final MsgListener E, final int category)
	{
		final List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
		if((E==null)||(V==null))
			return;
		synchronized(V)
		{
			for (final WeakReference<MsgListener> W : V)
			{
				if(W.get()==E)
					V.remove(W);
			}
		}
	}

	@Override
	public MOB deity()
	{
		if(deities().hasMoreElements())
			return deities().nextElement();
		if((deityStandIn==null)
		||(deityStandIn.amDestroyed())
		||(deityStandIn.amDead())
		||(deityStandIn.location()==null)
		||(deityStandIn.location().isInhabitant(deityStandIn)))
		{
			if(deityStandIn!=null)
				deityStandIn.destroy();
			final MOB everywhereMOB=CMClass.getMOB("StdMOB");
			everywhereMOB.setName(L("god"));
			everywhereMOB.setLocation(this.getRandomRoom());
			deityStandIn=everywhereMOB;
		}
		return deityStandIn;
	}

	@Override
	public MOB getFactoryMOBInAnyRoom()
	{
		return getFactoryMOB(this.getRandomRoom());
	}

	@Override
	public MOB getFactoryMOB(final Room R)
	{
		final MOB everywhereMOB=CMClass.getFactoryMOB();
		everywhereMOB.setName(L("somebody"));
		everywhereMOB.setLocation(R);
		return everywhereMOB;
	}

	@Override
	public String createNewExit(Room from, Room room, final int direction)
	{
		if(direction >= from.rawDoors().length)
			return "Bad direction";
		if(direction >= room.rawDoors().length)
			return "Bad direction";
		Room opRoom=from.rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==from))
			return "Opposite room already exists and heads this way.";

		Exit thisExit=null;
		synchronized(CMClass.getSync("SYNC"+from.roomID()))
		{
			from=getRoom(from);
			if(opRoom!=null)
				from.rawDoors()[direction]=null;

			from.rawDoors()[direction]=room;
			thisExit=from.getRawExit(direction);
			if(thisExit==null)
			{
				thisExit=CMClass.getExit("StdOpenDoorway");
				from.setRawExit(direction,thisExit);
			}
			CMLib.database().DBUpdateExits(from);
		}
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			room=getRoom(room);
			if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
			{
				room.rawDoors()[Directions.getOpDirectionCode(direction)]=from;
				room.setRawExit(Directions.getOpDirectionCode(direction),thisExit);
				CMLib.database().DBUpdateExits(room);
			}
		}
		return "";
	}

	@Override
	public int numRooms()
	{
		int total=0;
		for(final Enumeration<Area> e=areas();e.hasMoreElements();)
			total+=e.nextElement().properSize();
		return total;
	}

	@Override
	public boolean sendGlobalMessage(final MOB host, final int category, final CMMsg msg)
	{
		final List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
		if(V==null)
			return true;
		synchronized(V)
		{
			try
			{
				MsgListener O=null;
				Environmental E=null;
				for(final WeakReference<MsgListener> W : V)
				{
					O=W.get();
					if(O instanceof Environmental)
					{
						E=(Environmental)O;
						if(!CMLib.flags().isInTheGame(E,true))
						{
							if(!CMLib.flags().isInTheGame(E,false))
								delGlobalHandler(E,category);
						}
						else
						if(!E.okMessage(host,msg))
							return false;
					}
					else
					if(O!=null)
					{
						if(!O.okMessage(host, msg))
							return false;
					}
					else
						V.remove(W);
				}
				for(final WeakReference<MsgListener> W : V)
				{
					O=W.get();
					if(O !=null)
						O.executeMsg(host,msg);
				}
			}
			catch(final java.lang.ArrayIndexOutOfBoundsException xx)
			{
			}
			catch (final Exception x)
			{
				Log.errOut("CMMap", x);
			}
		}
		return true;
	}

	@Override
	public String getExtendedRoomID(final Room R)
	{
		if(R==null)
			return "";
		if(R.roomID().length()>0)
			return R.roomID();
		final Area A=R.getArea();
		if(A==null)
			return "";
		final GridLocale GR=R.getGridParent();
		if((GR!=null)&&(GR.roomID().length()>0))
			return GR.getGridChildCode(R);
		return R.roomID();
	}

	@Override
	public String getDescriptiveExtendedRoomID(final Room room)
	{
		if(room==null)
			return "";
		final String roomID = getExtendedRoomID(room);
		if(roomID.length()>0)
			return roomID;
		final GridLocale gridParentRoom=room.getGridParent();
		if((gridParentRoom!=null)&&(gridParentRoom.roomID().length()==0))
		{
			for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++)
			{
				final Room attachedRoom = gridParentRoom.rawDoors()[dir];
				if(attachedRoom != null)
				{
					final String attachedRoomID = getExtendedRoomID(attachedRoom);
					if(attachedRoomID.length()>0)
						return CMLib.directions().getFromCompassDirectionName(Directions.getOpDirectionCode(dir))+" "+attachedRoomID;
				}
			}
		}
		Area area=room.getArea();
		if((area==null)&&(gridParentRoom!=null))
			area=gridParentRoom.getArea();
		if(area == null)
			return "";
		return area.Name()+"#?";
	}

	@Override
	public String getApproximateExtendedRoomID(final Room room)
	{
		if(room==null)
			return "";
		final String extendedID = getExtendedRoomID(room);
		if(extendedID.length()>0)
			return extendedID;
		Room validRoom = CMLib.tracking().getNearestValidIDRoom(room);
		if(validRoom != null)
		{
			if((validRoom instanceof GridLocale)
			&&(validRoom.roomID()!=null)
			&&(validRoom.roomID().length()>0))
				validRoom=((GridLocale)validRoom).getRandomGridChild();
			return getExtendedRoomID(validRoom);
		}
		if(room.getArea()!=null)
			return room.getArea().Name()+"#?";
		return "";
	}

	@Override
	public String getExtendedTwinRoomIDs(final Room R1,final Room R2)
	{
		final String R1s=getExtendedRoomID(R1);
		final String R2s=getExtendedRoomID(R2);
		if(R1s.compareTo(R2s)>0)
			return R1s+"_"+R2s;
		else
			return R2s+"_"+R1s;
	}

	@Override
	public Area findRoomIDArea(final String roomID)
	{
		final int grid = roomID.lastIndexOf("#(");
		if(grid > 0)
			return findRoomIDArea(roomID.substring(0,grid));
		final int x=roomID.indexOf('#');
		if(x>=0)
		{
			final Area A=getArea(roomID.substring(0,x));
			if((A!=null)
			&&(A.getProperRoomnumbers().contains(roomID)))
				return A;
		}
		for(final Enumeration<Area> e=this.areas();e.hasMoreElements();)
		{
			final Area A = e.nextElement();
			if((A!=null)
			&&(A.getProperRoomnumbers().contains(roomID)))
				return A;
		}
		for(final Enumeration<Area> e=shipAreaEnumerator(null);e.hasMoreElements();)
		{
			final Area A = e.nextElement();
			if((A!=null)
			&&(A.getProperRoomnumbers().contains(roomID)))
				return A;
		}
		return null;
	}

	protected Room getRoom(final Enumeration<Room> roomSet, final String roomID, final boolean cachedOnly)
	{
		try
		{
			if(roomID==null)
				return null;
			if(roomID.length()==0)
				return null;
			if(roomID.endsWith(")"))
			{
				final int child=roomID.lastIndexOf("#(");
				if(child>1)
				{
					Room R=getRoom(roomSet,roomID.substring(0,child));
					if(R instanceof GridLocale)
					{
						R=((GridLocale)R).getGridChild(roomID);
						if(R!=null)
							return R;
					}
				}
			}
			Room R=null;
			if(roomSet==null)
			{
				final int x=roomID.indexOf('#');
				if(x>=0)
				{
					final Area A=getArea(roomID.substring(0,x));
					if(A!=null)
					{
						if(cachedOnly)
						{
							if(A.getProperRoomnumbers().contains(roomID))
							{
								if(A.isRoomCached(roomID))
									return A.getRoom(roomID);
								return null;
							}
						}
						else
						{
							R = A.getRoom(roomID);
							if(R != null)
								return R;
						}
					}
				}
				for(final Enumeration<Area> e=areas();e.hasMoreElements();)
				{
					final Area A = e.nextElement();
					if(A!=null)
					{
						if(cachedOnly)
						{
							if(A.getProperRoomnumbers().contains(roomID))
							{
								if(A.isRoomCached(roomID))
									return A.getRoom(roomID);
								return null;
							}
						}
						else
						{
							R = A.getRoom(roomID);
							if(R != null)
								return R;
						}
					}
				}
				for(final Enumeration<Area> e=shipAreaEnumerator(null);e.hasMoreElements();)
				{
					final Area A = e.nextElement();
					if(A!=null)
					{
						if(cachedOnly)
						{
							if(A.getProperRoomnumbers().contains(roomID))
							{
								if(A.isRoomCached(roomID))
									return A.getRoom(roomID);
								return null;
							}
						}
						else
						{
							R = A.getRoom(roomID);
							if(R != null)
								return R;
						}
					}
				}
			}
			else
			{
				for(final Enumeration<Room> e=roomSet;e.hasMoreElements();)
				{
					R=e.nextElement();
					if(R.roomID().equalsIgnoreCase(roomID))
						return R;
				}
			}
		}
		catch (final java.util.NoSuchElementException x)
		{
		}
		return null;
	}

	@Override
	public Room getRoom(final Enumeration<Room> roomSet, final String roomID)
	{
		return getRoom(roomSet, roomID, false);
	}


	@Override
	public Room getRoom(final Room room)
	{
		if(room==null)
			return null;
		if(room.amDestroyed())
			return getRoom(getExtendedRoomID(room));
		return room;
	}

	@Override
	public Room getRoom(final String roomID)
	{
		return getRoom(null,roomID);
	}

	@Override
	public Room getCachedRoom(final String roomID)
	{
		return getRoom(null,roomID,true);
	}

	@Override
	public Room getRoomAllHosts(final String roomID)
	{
		final Room R = getRoom(null,roomID);
		if(R!=null)
			return R;
		for(final Enumeration<CMLibrary> pl=CMLib.libraries(CMLib.Library.MAP); pl.hasMoreElements(); )
		{
			final WorldMap mLib = (WorldMap)pl.nextElement();
			if(mLib != this)
			{
				final Room R2 = mLib.getRoom(roomID);
				if(R2 != null)
					return R2;
			}
		}
		return null;
	}

	@Override
	public Enumeration<Room> rooms()
	{
		return new AreasRoomsEnumerator(areasPlusShips(), false);
	}

	@Override
	public Enumeration<Room> roomsFilled()
	{
		return new AreasRoomsEnumerator(areasPlusShips(), true);
	}

	@Override
	public Enumeration<MOB> worldMobs()
	{
		return new RoomMobsEnumerator(roomsFilled());
	}

	@Override
	public Enumeration<Item> worldRoomItems()
	{
		return new RoomItemsEnumerator(roomsFilled(), false);
	}

	@Override
	public Enumeration<Item> worldEveryItems()
	{
		return new RoomItemsEnumerator(roomsFilled(),true);
	}

	@Override
	public Room getRandomRoom()
	{
		Room R=null;
		int numRooms=-1;
		for(int i=0;i<1000 && ((R==null)&&((numRooms=numRooms())>0));i++)
		{
			try
			{
				final int which=CMLib.dice().roll(1,numRooms,-1);
				int total=0;
				for(final Enumeration<Area> e=areas();e.hasMoreElements();)
				{
					final Area A=e.nextElement();
					if(which<(total+A.properSize()))
					{
						R = A.getRandomProperRoom();
						break;
					}
					total+=A.properSize();
				}
			}
			catch (final NoSuchElementException e)
			{
				if(i > 998)
					Log.errOut(e);
			}
		}
		return R;
	}

	public int numDeities()
	{
		return deitiesList.size();
	}

	protected void addDeity(final Deity newOne)
	{
		if (!deitiesList.contains(newOne))
			deitiesList.add(newOne);
	}

	protected void delDeity(final Deity oneToDel)
	{
		if (deitiesList.contains(oneToDel))
		{
			//final boolean deitiesRemain = deitiesList.size()>0;
			deitiesList.remove(oneToDel);
			//if(deitiesRemain && ((deitiesList.size()==0)))
			//	Log.debugOut("**DEITIES",new Exception());
		}
	}

	@Override
	public Deity getDeity(final String calledThis)
	{
		if((calledThis==null)||(calledThis.length()==0))
			return null;
		return deitiesList.find(calledThis);
	}

	@Override
	public Enumeration<Deity> deities()
	{
		return new IteratorEnumeration<Deity>(deitiesList.iterator());
	}

	@Override
	public int numShips()
	{
		return shipList.size();
	}

	protected void addShip(final Boardable newOne)
	{
		if (!shipList.contains(newOne))
		{
			shipList.add(newOne);
			final Area area=newOne.getArea();
			if((area!=null)&&(area.getAreaState()==Area.State.ACTIVE))
				area.setAreaState(Area.State.ACTIVE);
		}
	}

	protected void delShip(final Boardable oneToDel)
	{
		if(oneToDel!=null)
		{
			shipList.remove(oneToDel);
			final Item shipI = oneToDel.getBoardableItem();
			if(shipI instanceof Boardable)
			{
				final Boardable boardableShipI = (Boardable)shipI;
				shipList.remove(boardableShipI);
			}
			final Area area=oneToDel.getArea();
			if(area!=null)
			{
				if(area instanceof Boardable)
				{
					final Boardable boardableShipA = (Boardable)area;
					shipList.remove(boardableShipA);
				}
				area.setAreaState(Area.State.STOPPED);
			}
		}
	}

	@Override
	public Boardable getShip(final String calledThis)
	{
		for (final Boardable S : shipList)
		{
			if (S.Name().equalsIgnoreCase(calledThis))
				return S;
		}
		return null;
	}

	@Override
	public Boardable findShip(final String s, final boolean exactOnly)
	{
		return (Boardable)CMLib.english().fetchEnvironmental(shipList, s, exactOnly);
	}

	@Override
	public Enumeration<Boardable> ships()
	{
		return new IteratorEnumeration<Boardable>(shipList.iterator());
	}

	@Override
	public Enumeration<Room> shipsRoomEnumerator(final Area inA)
	{
		return new Enumeration<Room>()
		{
			private Enumeration<Room> cur = null;
			private Enumeration<Area> cA = shipAreaEnumerator(inA);

			@Override
			public boolean hasMoreElements()
			{
				boolean hasMore = (cur != null) && cur.hasMoreElements();
				while(!hasMore)
				{
					if((cA == null)||(!cA.hasMoreElements()))
					{
						cur=null;
						cA = null;
						return false;
					}
					cur = cA.nextElement().getProperMap();
					hasMore = (cur != null) && cur.hasMoreElements();
				}
				return hasMore;
			}

			@Override
			public Room nextElement()
			{
				if(!hasMoreElements())
					throw new NoSuchElementException();
				return cur.nextElement();
			}
		};
	}

	public Enumeration<Area> shipAreaEnumerator(final Area inA)
	{
		return new Enumeration<Area>()
		{
			private volatile Area nextArea=null;
			private volatile Enumeration<Boardable> shipsEnum=ships();

			@Override
			public boolean hasMoreElements()
			{
				while(nextArea == null)
				{
					if((shipsEnum==null)||(!shipsEnum.hasMoreElements()))
					{
						shipsEnum=null;
						return false;
					}
					final Boardable ship=shipsEnum.nextElement();
					if((ship!=null)&&(ship.getArea()!=null))
					{
						if((inA==null)||(areaLocation(ship)==inA))
							nextArea=ship.getArea();
					}
				}
				return (nextArea != null);
			}

			@Override
			public Area nextElement()
			{
				if(!hasMoreElements())
					throw new NoSuchElementException();
				final Area A=nextArea;
				this.nextArea=null;
				return A;
			}
		};
	}

	@Override
	public void renameRooms(final Area A, final String oldName, List<Room> allMyDamnRooms)
	{
		final List<Room> onesToRenumber=new Vector<Room>();
		if(allMyDamnRooms == null)
			allMyDamnRooms = new XArrayList<Room>(A.getCompleteMap());
		for(Room R : allMyDamnRooms)
		{
			synchronized(CMClass.getSync("SYNC"+R.roomID()))
			{
				R=getRoom(R);
				R.setArea(A);
				if(oldName!=null)
				{
					if(R.roomID().toUpperCase().startsWith(oldName.toUpperCase()+"#"))
					{
						Room R2=A.getRoom(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
						if(R2 == null)
							R2=getRoom(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
						if((R2==null)||(!R2.roomID().startsWith(A.Name()+"#")))
						{
							final String oldID=R.roomID();
							R.setRoomID(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
							CMLib.database().DBReCreate(R,oldID);
						}
						else
							onesToRenumber.add(R);
					}
					else
						CMLib.database().DBUpdateRoom(R);
				}
			}
		}
		if(oldName!=null)
		{
			for(final Room R: onesToRenumber)
			{
				final String oldID=R.roomID();
				R.setRoomID(A.getNewRoomID(R,-1));
				CMLib.database().DBReCreate(R,oldID);
			}
		}
	}

	@Override
	public int getRoomDir(final Room from, final Room to)
	{
		if((from==null)||(to==null))
			return -1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(from.getRoomInDir(d)==to)
				return d;
		}
		return -1;
	}

	@Override
	public Area getTargetArea(final Room from, final Exit to)
	{
		final Room R=getTargetRoom(from, to);
		if(R==null)
			return null;
		return R.getArea();
	}

	@Override
	public Room getTargetRoom(final Room from, final Exit to)
	{
		if((from==null)||(to==null))
			return null;
		final int d=getExitDir(from, to);
		if(d<0)
			return null;
		return from.getRoomInDir(d);
	}

	@Override
	public int getExitDir(final Room from, final Exit to)
	{
		if((from==null)||(to==null))
			return -1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(from.getExitInDir(d)==to)
				return d;
			if(from.getRawExit(d)==to)
				return d;
			if(from.getReverseExit(d)==to)
				return d;
		}
		return -1;
	}

	@Override
	public Room findConnectingRoom(final Room room)
	{
		if(room==null)
			return null;
		Room R=null;
		final Vector<Room> otherChoices=new Vector<Room>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R=room.getRoomInDir(d);
			if(R!=null)
			{
				for(int d1=Directions.NUM_DIRECTIONS()-1;d1>=0;d1--)
				{
					if(R.getRoomInDir(d1)==room)
					{
						if(R.getArea()==room.getArea())
							return R;
						otherChoices.addElement(R);
					}
				}
			}
		}
		for(final Enumeration<Room> e=rooms();e.hasMoreElements();)
		{
			R=e.nextElement();
			if(R==room)
				continue;
			for(int d1=Directions.NUM_DIRECTIONS()-1;d1>=0;d1--)
			{
				if(R.getRoomInDir(d1)==room)
				{
					if(R.getArea()==room.getArea())
						return R;
					otherChoices.addElement(R);
				}
			}
		}
		if(otherChoices.size()>0)
			return otherChoices.firstElement();
		return null;
	}

	@Override
	public boolean isClearableRoom(final Room R)
	{
		if((R==null)||(R.amDestroyed()))
			return true;
		MOB M=null;
		Room sR=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			M=R.fetchInhabitant(i);
			if(M==null)
				continue;
			sR=M.getStartRoom();
			if((sR!=null)
			&&(sR != R)
			&&(!sR.roomID().equals(R.roomID()))
			&&(!sR.amDestroyed()))
			{
				CMLib.tracking().wanderAway(M, false, true);
				if(M.location()==R)
					return false;
			}
			if(M.session()!=null)
				return false;
		}
		Item I=null;
		for(int i=0;i<R.numItems();i++)
		{
			I=R.getItem(i);
			if((I!=null)
			&&((I.expirationDate()!=0)
				||((I instanceof DeadBody)&&(((DeadBody)I).isPlayerCorpse()))
				||((I instanceof PrivateProperty)&&(((PrivateProperty)I).getOwnerName().length()>0))))
					return false;
		}
		for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(!A.isSavable()))
				return false;
		}
		return true;
	}

	public class AreasRoomsEnumerator implements Enumeration<Room>
	{
		private final Enumeration<Area>		curAreaEnumeration;
		private final boolean				addSkys;
		private volatile Enumeration<Room>	curRoomEnumeration	= null;

		public AreasRoomsEnumerator(final Enumeration<Area> curAreaEnumeration, final boolean includeSkys)
		{
			addSkys = includeSkys;
			this.curAreaEnumeration=curAreaEnumeration;
		}

		@Override
		public boolean hasMoreElements()
		{
			boolean hasMore = (curRoomEnumeration!=null)&&(curRoomEnumeration.hasMoreElements());
			while(!hasMore)
			{
				if((curAreaEnumeration == null)||(!curAreaEnumeration.hasMoreElements()))
				{
					curRoomEnumeration = null;
					return false;
				}
				if(addSkys)
					curRoomEnumeration=curAreaEnumeration.nextElement().getFilledProperMap();
				else
					curRoomEnumeration=curAreaEnumeration.nextElement().getProperMap();
				hasMore = (curRoomEnumeration!=null)&&(curRoomEnumeration.hasMoreElements());
			}
			return hasMore;
		}

		@Override
		public Room nextElement()
		{
			if(!hasMoreElements())
				throw new NoSuchElementException();
			return curRoomEnumeration.nextElement();
		}
	}

	public class RoomMobsEnumerator implements Enumeration<MOB>
	{
		private final Enumeration<Room>		curRoomEnumeration;
		private volatile Enumeration<MOB>	curMobEnumeration	= null;

		public RoomMobsEnumerator(final Enumeration<Room> curRoomEnumeration)
		{
			this.curRoomEnumeration=curRoomEnumeration;
		}

		@Override
		public boolean hasMoreElements()
		{
			boolean hasMore = (curMobEnumeration!=null)&&(curMobEnumeration.hasMoreElements());
			while(!hasMore)
			{
				if((curRoomEnumeration == null)||(!curRoomEnumeration.hasMoreElements()))
				{
					curMobEnumeration = null;
					return false;
				}
				curMobEnumeration=curRoomEnumeration.nextElement().inhabitants();
				hasMore = (curMobEnumeration!=null)&&(curMobEnumeration.hasMoreElements());
			}
			return hasMore;
		}

		@Override
		public MOB nextElement()
		{
			if(!hasMoreElements())
				throw new NoSuchElementException();
			return curMobEnumeration.nextElement();
		}
	}

	public class RoomItemsEnumerator implements Enumeration<Item>
	{
		private final Enumeration<Room>		curRoomEnumeration;
		private final boolean				includeMobItems;
		private volatile Enumeration<Item>	curItemEnumeration	= null;

		public RoomItemsEnumerator(final Enumeration<Room> curRoomEnumeration, final boolean includeMobItems)
		{
			this.curRoomEnumeration=curRoomEnumeration;
			this.includeMobItems=includeMobItems;
		}

		@Override
		public boolean hasMoreElements()
		{
			boolean hasMore = (curItemEnumeration!=null)&&(curItemEnumeration.hasMoreElements());
			while(!hasMore)
			{
				if((curRoomEnumeration == null)||(!curRoomEnumeration.hasMoreElements()))
				{
					curItemEnumeration = null;
					return false;
				}
				if(includeMobItems)
					curItemEnumeration=curRoomEnumeration.nextElement().itemsRecursive();
				else
					curItemEnumeration=curRoomEnumeration.nextElement().items();
				hasMore = (curItemEnumeration!=null)&&(curItemEnumeration.hasMoreElements());
			}
			return hasMore;
		}

		@Override
		public Item nextElement()
		{
			if(!hasMoreElements())
				throw new NoSuchElementException();
			return curItemEnumeration.nextElement();
		}
	}

	@Override
	public void obliterateMapRoom(final Room deadRoom)
	{
		obliterateRoom(deadRoom,null,true);
	}

	@Override
	public void destroyRoomObject(final Room deadRoom)
	{
		obliterateRoom(deadRoom,null,false);
	}

	protected void obliterateRoom(final Room deadRoom, final List<Room> linkInRooms, final boolean includeDB)
	{
		if(deadRoom == null)
			return;
		final Area A = deadRoom.getArea();
		for(final Enumeration<Ability> a=deadRoom.effects();a.hasMoreElements();)
		{
			final Ability effA=a.nextElement();
			if(A!=null)
			{
				effA.unInvoke();
				deadRoom.delEffect(effA);
			}
		}
		try
		{
			final Map<Room,Set<Integer>> roomsToDo=new HashMap<Room,Set<Integer>>();
			if(deadRoom.roomID().length()>0)
			{
				final Map<Integer,Pair<String,String>> exitIntoMap = CMLib.database().DBReadIncomingRoomExitIDsMap(deadRoom.roomID());
				for(final Integer key : exitIntoMap.keySet())
				{
					final Pair<String,String> p = exitIntoMap.get(key);
					if(p.first.trim().length()>0)
					{
						final Room R = this.getCachedRoom(p.first);
						if(R != null)
						{
							if(!roomsToDo.containsKey(R))
								roomsToDo.put(R, new TreeSet<Integer>());
							roomsToDo.get(R).add(key);
						}
					}
				}
			}
			final Enumeration<Room> r;
			if(linkInRooms != null)
				r=new IteratorEnumeration<Room>(linkInRooms.iterator());
			else
				r=rooms();
			for(;r.hasMoreElements();)
			{
				final Room R=getRoom(r.nextElement());
				if(R!=null)
				{
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Room linkR=R.rawDoors()[d];
						if(linkR != null)
						{
							if((linkR==deadRoom)
							||(linkR.roomID().equalsIgnoreCase(deadRoom.roomID())&&(linkR.roomID().length()>0)))
							{
								if(R.roomID().trim().length()==0)
									R.rawDoors()[d]=null;
								else
								{
									final Set<Integer> dirs;
									if(roomsToDo.containsKey(R))
										dirs = roomsToDo.get(R);
									else
									{
										dirs=new TreeSet<Integer>();
										roomsToDo.put(R, dirs);
									}
									dirs.add(Integer.valueOf(d));
								}
							}
						}
					}
				}
			}
			for(final Room R : roomsToDo.keySet())
			{
				final Set<Integer> dirOs = roomsToDo.get(R);
				synchronized(CMClass.getSync("SYNC"+R.roomID()))
				{
					for(final Integer dirO : dirOs)
					{
						final int d = dirO.intValue();
						if(d<Directions.NUM_DIRECTIONS())
						{
							R.rawDoors()[d]=null;
							if((R.getRawExit(d)!=null)
							&&(R.getRawExit(d).isGeneric()))
							{
								final Exit GE=R.getRawExit(d);
								GE.setTemporaryDoorLink(deadRoom.roomID());
							}
						}
						else
						if(R instanceof GridLocale)
						{
							final GridLocale rG = (GridLocale)R;
							for(final Iterator<CrossExit> i = rG.outerExits();i.hasNext();)
							{
								final CrossExit cE = i.next();
								if(cE.destRoomID.equalsIgnoreCase(deadRoom.roomID())
								&&(deadRoom.roomID().length()>0))
									i.remove();
							}
						}
					}
					if(includeDB)
						CMLib.database().DBUpdateExits(R);
				}
			}
		}
		catch (final NoSuchElementException e)
		{
		}
		emptyRoom(deadRoom,null,true);
		deadRoom.destroy();
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid(null);
		if(includeDB)
		{
			CMLib.database().DBDeleteRoom(deadRoom);
			if(A != null)
			{
				Resources.removeResource("HELP_" + A.Name().toUpperCase());
				Resources.removeResource("STATS_" + A.Name().toUpperCase());
			}
		}
	}

	@Override
	public void emptyAreaAndDestroyRooms(final Area area)
	{
		for(final Enumeration<Ability> a=area.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
			{
				A.unInvoke();
				area.delEffect(A);
			}
		}
		for(final Enumeration<Room> e=area.getProperMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			emptyRoom(R,null,true);
			R.destroy();
		}
	}

	@Override
	public Room roomLocation(final Environmental E)
	{
		if(E==null)
			return null;
		if((E instanceof Area)&&(!((Area)E).isProperlyEmpty()))
			return ((Area)E).getRandomProperRoom();
		else
		if(E instanceof Room)
			return (Room)E;
		else
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
			return ((MOB)((Item)E).owner()).location();
		else
		if(E instanceof Ability)
			return roomLocation(((Ability)E).affecting());
		else
		if(E instanceof Exit)
			return roomLocation(((Exit)E).lastRoomUsedFrom(null));
		return null;
	}

	@Override
	public Area getStartArea(final Environmental E)
	{
		if(E instanceof Area)
			return (Area)E;
		final Room R=getStartRoom(E);
		if(R==null)
			return null;
		return R.getArea();
	}

	@Override
	public Room getStartRoom(final Environmental E)
	{
		if(E ==null)
			return null;
		if(E instanceof MOB)
			return ((MOB)E).getStartRoom();
		if(E instanceof Item)
		{
			if(((Item)E).owner() instanceof MOB)
				return getStartRoom(((Item)E).owner());
			if(CMLib.flags().isGettable((Item)E))
				return null;
		}
		if(E instanceof Ability)
			return getStartRoom(((Ability)E).affecting());
		if((E instanceof Area)&&(!((Area)E).isProperlyEmpty()))
			return ((Area)E).getRandomProperRoom();
		if(E instanceof Room)
			return (Room)E;
		return roomLocation(E);
	}

	@Override
	public Area areaLocation(final CMObject E)
	{
		if(E==null)
			return null;
		if(E instanceof Area)
			return (Area)E;
		else
		if(E instanceof Room)
			return ((Room)E).getArea();
		else
		if(E instanceof MOB)
			return areaLocation(((MOB)E).location());
		else
		if(E instanceof Item)
			return areaLocation(((Item) E).owner());
		else
		if((E instanceof Ability)&&(((Ability)E).affecting()!=null))
			return areaLocation(((Ability)E).affecting());
		else
		if(E instanceof Exit)
			return areaLocation(((Exit)E).lastRoomUsedFrom(null));
		return null;
	}

	@Override
	public Room getSafeRoomToMovePropertyTo(final Room room, final PrivateProperty I)
	{
		if(I instanceof Boardable)
		{
			final Room R=getRoom(((Boardable)I).getHomePortID());
			if((R!=null)&&(R!=room)&&(!R.amDestroyed()))
				return R;
		}
		if(room != null)
		{
			Room R=null;
			if(room.getGridParent()!=null)
			{
				R=getRoom(room.getGridParent());
				if((R!=null)&&(R!=room)&&(!R.amDestroyed())&&(R.roomID().length()>0))
					return R;
			}
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				R=getRoom(room.getRoomInDir(d));
				if((R!=null)&&(R!=room)&&(!R.amDestroyed())&&(R.roomID().length()>0))
					return R;
			}
			final Room parentR = room.getGridParent();
			if(parentR!=null)
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					R=getRoom(parentR.getRoomInDir(d));
					if((R!=null)&&(R!=room)&&(!R.amDestroyed())&&(R.roomID().length()>0))
						return R;
				}
			}
			final Area A=room.getArea();
			if(A!=null)
			{
				for(int i=0;i<A.numberOfProperIDedRooms();i++)
				{
					R=getRoom(A.getRandomProperRoom());
					if((R!=null)&&(R!=room)&&(!R.amDestroyed())&&(R.roomID().length()>0))
						return R;
				}
			}
		}
		for(int i=0;i<100;i++)
		{
			final Room R=getRoom(this.getRandomRoom());
			if((R!=null)&&(R!=room)&&(!R.amDestroyed())&&(R.roomID().length()>0))
				return R;
		}
		return null;
	}

	@Override
	public void emptyRoom(final Room room, final Room toRoom, final boolean clearPlayers)
	{
		if(room==null)
			return;
		// this will empty grid rooms so that
		// the code below can delete them or whatever.
		if(room instanceof GridLocale)
		{
			for(final Iterator<Room> r=((GridLocale)room).getExistingRooms();r.hasNext();)
				emptyRoom(r.next(), toRoom, clearPlayers);
		}
		// this will empty skys and underwater of mobs so that
		// the code below can delete them or whatever.
		room.clearSky();
		if(toRoom != null)
		{
			for(final Enumeration<MOB> i=room.inhabitants();i.hasMoreElements();)
			{
				final MOB M=i.nextElement();
				if(M!=null)
					toRoom.bringMobHere(M,false);
			}
		}
		else
		if(clearPlayers)
		{
			for(final Enumeration<MOB> i=room.inhabitants();i.hasMoreElements();)
			{
				final MOB M=i.nextElement();
				if((M!=null) && (M.isPlayer()))
				{
					Room sR=M.getStartRoom();
					int attempts=1000;
					while(((sR == room)||(sR==null))
					&&(--attempts>0))
						sR=getRandomRoom();
					if((sR!=null)&&(sR!=room))
						sR.bringMobHere(M,true);
					else
						room.delInhabitant(M);
				}
			}
		}
		for(final Enumeration<MOB> i=room.inhabitants();i.hasMoreElements();)
		{
			final MOB M=i.nextElement();
			if((M!=null)
			&&(!M.isPlayer())
			&&(M.isSavable()) // this is almost certainly to protect Quest mobs, which are just about the only unsavable things.
			&&((M.amFollowing()==null)||(!M.amFollowing().isPlayer())))
			{
				final Room startRoom = M.getStartRoom();
				final Area startArea = (startRoom == null) ? null : startRoom.getArea();
				if((startRoom==null)
				||(startRoom==room)
				||(startRoom.amDestroyed())
				||(startArea==null)
				||(startArea.amDestroyed())
				||(startRoom.ID().length()==0))
					M.destroy();
				else
					M.getStartRoom().bringMobHere(M,false);
			}
		}

		Item I=null;
		if(toRoom != null)
		{
			for(final Enumeration<Item> i=room.items();i.hasMoreElements();)
			{
				I=i.nextElement();
				if(I!=null)
					toRoom.moveItemTo(I,ItemPossessor.Expire.Player_Drop);
			}
		}
		else
		{
			for(final Enumeration<Item> i=room.items();i.hasMoreElements();)
			{
				I=i.nextElement();
				if(I != null)
				{
					if((I instanceof PrivateProperty)
					&&((((PrivateProperty)I).getOwnerName().length()>0)))
					{
						final Room R=getSafeRoomToMovePropertyTo(room, (PrivateProperty)I);
						if((R!=null)
						&&(R!=room))
							R.moveItemTo(I,ItemPossessor.Expire.Player_Drop);
					}
					else
						I.destroy();
				}
			}
		}
		room.clearSky();
		 // clear debri only clears things by their start rooms, not location, so only roomid matters.
		if(room.roomID().length()>0)
			CMLib.threads().clearDebri(room,LocalType.MOBS_OR_ITEMS);
		if(room instanceof GridLocale)
		{
			for(final Iterator<Room> r=((GridLocale)room).getExistingRooms();r.hasNext();)
				emptyRoom(r.next(), toRoom, clearPlayers);
		}
	}

	@Override
	public void obliterateMapArea(final Area A)
	{
		obliterateArea(A,true);
		for(final Enumeration<Area> a=areas();a.hasMoreElements();)
		{
			final Area A2=a.nextElement();
			if((A2!=null)
			&&(A2.isSavable())
			&&(A2.isParent(A)||A2.isChild(A)))
			{
				A2.removeParent(A);
				A2.removeChild(A);
				CMLib.database().DBUpdateArea(A2.Name(), A2);
			}
		}
	}

	@Override
	public void destroyAreaObject(final Area A)
	{
		obliterateArea(A,false);
	}

	protected void obliterateArea(final Area A, final boolean includeDB)
	{
		if(A==null)
			return;
		A.setAreaState(Area.State.STOPPED);
		if(A instanceof SpaceShip)
			CMLib.tech().unregisterAllElectronics(CMLib.tech().getElectronicsKey(A));
		final List<Room> allRooms=new LinkedList<Room>();
		for(int i=0;i<2;i++)
		{
			for(final Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
			{
				final Room R=e.nextElement();
				if(R!=null)
				{
					allRooms.add(R);
					emptyRoom(R,null,false);
					R.clearSky();
				}
			}
		}
		if(includeDB)
			CMLib.database().DBDeleteAreaAndRooms(A);
		final List<Room> linkInRooms = new LinkedList<Room>();
		for(final Enumeration<Room> r=rooms();r.hasMoreElements();)
		{
			final Room R=getRoom(r.nextElement());
			if(R!=null)
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room thatRoom=R.rawDoors()[d];
					if((thatRoom!=null)
					&&(thatRoom.getArea()==A))
					{
						linkInRooms.add(R);
						break;
					}
				}
			}
		}

		for(final Room R : allRooms)
			obliterateRoom(R,linkInRooms,includeDB);
		delArea(A);
		Resources.removeResource("HELP_" + A.Name().toUpperCase());
		Resources.removeResource("STATS_" + A.Name().toUpperCase());
		A.destroy(); // why not?
	}

	public CMMsg resetMsg=null;

	@Override
	public void resetRoom(final Room room)
	{
		resetRoom(room,false);
	}

	@Override
	public void resetRoom(Room room, final boolean rebuildGrids)
	{
		if(room==null)
			return;
		if(room.roomID().length()==0)
			return;
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			room=getRoom(room);
			if((rebuildGrids)&&(room instanceof GridLocale))
				((GridLocale)room).clearGrid(null);
			final boolean mobile=room.getMobility();
			try
			{
				room.toggleMobility(false);
				if(resetMsg==null)
					resetMsg=CMClass.getMsg(CMClass.sampleMOB(),room,CMMsg.MSG_ROOMRESET,null);
				resetMsg.setTarget(room);
				room.executeMsg(room,resetMsg);
				if(room.isSavable())
					emptyRoom(room,null,false);
				for(final Enumeration<Ability> a=room.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)&&(A.canBeUninvoked()))
						A.unInvoke();
				}
				if(room.isSavable())
				{
					CMLib.database().DBReReadRoomData(room);
					CMLib.database().DBReadContent(room.roomID(),room,true);
				}
				room.startItemRejuv();
				room.setResource(-1);
			}
			finally
			{
				room.toggleMobility(mobile);
			}
		}
	}


	protected PairVector<MOB,String> getAllPlayersHere(final Area area, final boolean includeLocalFollowers)
	{
		final PairVector<MOB,String> playersHere=new PairVector<MOB,String>();
		MOB M=null;
		Room R=null;
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			M=S.mob();
			R=(M!=null)?M.location():null;
			if((R!=null)&&(R.getArea()==area)&&(M!=null))
			{
				playersHere.addElement(M,getExtendedRoomID(R));
				if(includeLocalFollowers)
				{
					MOB M2=null;
					final Set<MOB> H=M.getGroupMembers(new HashSet<MOB>());
					for(final Iterator<MOB> i=H.iterator();i.hasNext();)
					{
						M2=i.next();
						if((M2!=M)&&(M2.location()==R))
							playersHere.addElement(M2,getExtendedRoomID(R));
					}
				}
			}
		}
		return playersHere;
	}

	@Override
	public void resetArea(final Area area)
	{
		final Area.State oldFlag=area.getAreaState();
		area.setAreaState(Area.State.FROZEN);
		if(area instanceof AutoGenArea)
		{
			Room returnR = null;
			for(final Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
			{
				final Room R = r.nextElement();
				if((R!=null)
				&&(returnR == null))
				{
					for(final Room nR : R.rawDoors())
					{
						if((nR!=null)&&(nR.getArea()!=area))
							returnR = nR;
					}
				}
			}
			if(returnR == null)
				returnR = getRandomRoom();
			((AutoGenArea)area).resetInstance(returnR);
			area.setAreaState(oldFlag);
			return;
		}

		final PairVector<MOB,String> playersHere=getAllPlayersHere(area,true);
		final PairVector<PrivateProperty, String> propertyHere=new PairVector<PrivateProperty, String>();
		for(int p=0;p<playersHere.size();p++)
		{
			final MOB M=playersHere.elementAt(p).first;
			final Room R=M.location();
			R.delInhabitant(M);
		}
		for(final Enumeration<Boardable> b=ships();b.hasMoreElements();)
		{
			final Boardable ship=b.nextElement();
			final Room R=roomLocation(ship);
			if((R!=null)
			&&(R.getArea()==area)
			&&(ship instanceof PrivateProperty)
			&&(((PrivateProperty)ship).getOwnerName().length()>0)
			&&(ship instanceof Item))
			{
				R.delItem((Item)ship);
				propertyHere.add((PrivateProperty)ship,getExtendedRoomID(R));
			}
		}
		for(final Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
			resetRoom(r.nextElement());
		area.fillInAreaRooms();
		for(int p=0;p<playersHere.size();p++)
		{
			final MOB M=playersHere.elementAt(p).first;
			Room R=getRoom(playersHere.elementAt(p).second);
			if(R==null)
				R=M.getStartRoom();
			if(R==null)
				R=getStartRoom(M);
			if(R!=null)
				R.bringMobHere(M,false);
		}
		for(int p=0;p<propertyHere.size();p++)
		{
			final PrivateProperty P=propertyHere.elementAt(p).first;
			Room R=getRoom(propertyHere.elementAt(p).second);
			if((R==null)||(R.amDestroyed()))
				R=getSafeRoomToMovePropertyTo((R==null)?area.getRandomProperRoom():R,P);
			if(R!=null)
				R.moveItemTo((Item)P);
		}
		CMLib.database().DBReadAreaData(area);
		area.setAreaState(oldFlag);
	}

	@Override
	public boolean hasASky(final Room room)
	{
		if((room==null)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||((room.domainType()&Room.INDOORS)>0))
			return false;
		return true;
	}

	@Override
	public void registerWorldObjectDestroyed(Area area, final Room room, final CMObject o)
	{
		if(o instanceof Deity)
			delDeity((Deity)o);

		if((o instanceof Boardable)&&(!(o instanceof Area)))
			delShip((Boardable)o);

		if(o instanceof PostOffice)
			CMLib.city().delPostOffice((PostOffice)o);

		if(o instanceof Librarian)
			CMLib.city().delLibrary((Librarian)o);

		if(o instanceof Banker)
			CMLib.city().delBank((Banker)o);

		if(o instanceof Auctioneer)
			CMLib.city().delAuctionHouse((Auctioneer)o);

		if(o instanceof PhysicalAgent)
		{
			final PhysicalAgent AE=(PhysicalAgent)o;
			if((area == null) && (room!=null))
				area = room.getArea();
			if(area == null)
				area =getStartArea(AE);
			delScriptHost(area, AE);
		}
	}

	@Override
	public void registerWorldObjectLoaded(Area area, Room room, final CMObject o)
	{
		if(o instanceof Deity)
			addDeity((Deity)o);

		if(o instanceof Boardable)
			addShip((Boardable)o);

		if(o instanceof PostOffice)
			CMLib.city().addPostOffice((PostOffice)o);

		if(o instanceof Banker)
			CMLib.city().addBank((Banker)o);

		if(o instanceof Librarian)
			CMLib.city().addLibrary((Librarian)o);

		if(o instanceof Auctioneer)
			CMLib.city().addAuctionHouse((Auctioneer)o);

		if(o instanceof PhysicalAgent)
		{
			final PhysicalAgent AE=(PhysicalAgent)o;
			if(room == null)
				room = getStartRoom(AE);
			if((area == null) && (room!=null))
				area = room.getArea();
			if(area == null)
				area = getStartArea(AE);
			addScriptHost(area, room, AE);
			if(o instanceof MOB)
			{
				for(final Enumeration<Item> i=((MOB)o).items();i.hasMoreElements();)
					addScriptHost(area, room, i.nextElement());
			}
		}
	}

	protected void cleanScriptHosts(final SLinkedList<LocatedPair> hosts, final PhysicalAgent oneToDel, final boolean fullCleaning)
	{
		PhysicalAgent PA;
		for (final LocatedPair W : hosts)
		{
			if(W==null)
				hosts.remove(W);
			else
			{
				PA=W.obj();
				if((PA==null)
				||(PA==oneToDel)
				||(PA.amDestroyed())
				||((fullCleaning)&&(!isAQualifyingScriptHost(PA))))
					hosts.remove(W);
			}
		}
	}

	protected boolean isAQualifyingScriptHost(final PhysicalAgent host)
	{
		if(host==null)
			return false;
		for(final Enumeration<Behavior> e = host.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if((B!=null) && B.isSavable() && (B instanceof ScriptingEngine))
				return true;
		}
		for(final Enumeration<ScriptingEngine> e = host.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			if((SE!=null) && SE.isSavable())
				return true;
		}
		return false;
	}

	protected boolean isAScriptHost(final Area area, final PhysicalAgent host)
	{
		if(area == null)
			return false;
		return isAScriptHost(scriptHostMap.get(area.Name()), host);
	}

	protected boolean isAScriptHost(final SLinkedList<LocatedPair> hosts, final PhysicalAgent host)
	{
		if((hosts==null)||(host==null)||(hosts.size()==0))
			return false;
		for (final LocatedPair W : hosts)
		{
			if(W.obj()==host)
				return true;
		}
		return false;
	}

	protected final Object getScriptHostSemaphore(final Area area)
	{
		final Object semaphore;
		if(SCRIPT_HOST_SEMAPHORES.containsKey(area.Name()))
			semaphore=SCRIPT_HOST_SEMAPHORES.get(area.Name());
		else
		{
			synchronized(SCRIPT_HOST_SEMAPHORES)
			{
				semaphore=new Object();
				SCRIPT_HOST_SEMAPHORES.put(area.Name(), semaphore);
			}
		}
		return semaphore;
	}

	protected void addScriptHost(final Area area, final Room room, final PhysicalAgent host)
	{
		if((area==null) || (host == null))
			return;
		if(!isAQualifyingScriptHost(host))
			return;
		synchronized(getScriptHostSemaphore(area))
		{
			SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name());
			if(hosts == null)
			{
				hosts=new SLinkedList<LocatedPair>();
				scriptHostMap.put(area.Name(), hosts);
			}
			else
			{
				cleanScriptHosts(hosts, null, false);
				if(isAScriptHost(hosts,host))
					return;
			}
			hosts.add(new LocatedPairImpl(room, host));
		}
	}

	protected void delScriptHost(Area area, final PhysicalAgent oneToDel)
	{
		if(oneToDel == null)
			return;
		if(area == null)
		{
			for(final Area A : areasList)
			{
				if(isAScriptHost(A,oneToDel))
				{
					area = A;
					break;
				}
			}
		}
		if(area == null)
			return;
		synchronized(getScriptHostSemaphore(area))
		{
			final SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name());
			if(hosts==null)
				return;
			cleanScriptHosts(hosts, oneToDel, false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<LocatedPair> scriptHosts(final Area area)
	{
		final LinkedList<List<LocatedPair>> V = new LinkedList<List<LocatedPair>>();
		if(area == null)
		{
			for(final String areaKey : scriptHostMap.keySet())
				V.add(scriptHostMap.get(areaKey));
		}
		else
		{
			final SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name());
			if(hosts==null)
				return EmptyEnumeration.INSTANCE;
			V.add(hosts);
		}
		if(V.size()==0)
			return EmptyEnumeration.INSTANCE;
		final MultiListEnumeration<LocatedPair> me=new MultiListEnumeration<LocatedPair>(V,true);
		return new Enumeration<LocatedPair>()
		{
			@Override
			public boolean hasMoreElements()
			{
				return me.hasMoreElements();
			}

			@Override
			public LocatedPair nextElement()
			{
				final LocatedPair W = me.nextElement();
				final PhysicalAgent E = W.obj();
				if(((E==null) || (E.amDestroyed())) && hasMoreElements())
					return nextElement();
				return W;
			}
		};
	}

	@Override
	public boolean activate()
	{
		if(serviceClient==null)
		{
			name="THMap"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAPTHREAD))
		&&(tickStatus == Tickable.STATUS_NOT))
		{
			try
			{
				tickStatus=Tickable.STATUS_ALIVE;
				isDebugging=CMSecurity.isDebugging(DbgFlag.MAPTHREAD);
				if(checkDatabase())
					roomMaintSweep();
				setThreadStatus(serviceClient,"saving props");
				Resources.savePropResources();
			}
			finally
			{
				tickStatus=Tickable.STATUS_NOT;
				setThreadStatus(serviceClient,"sleeping");
			}
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		final boolean debugMem = CMSecurity.isDebugging(CMSecurity.DbgFlag.SHUTDOWN);
		for(final Enumeration<Area> a=areasList.elements();a.hasMoreElements();)
		{
			try
			{
				final Area A = a.nextElement();
				if(A!=null)
				{
					CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down Map area '"+A.Name()+"'...");
					final LinkedList<Room> rooms=new LinkedList<Room>();
					for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
					{
						try
						{
							final Room R=r.nextElement();
							if(R!=null)
								rooms.add(R);
						}
						catch(final Exception e)
						{
						}
					}
					for(final Iterator<Room> r=rooms.iterator();r.hasNext();)
					{
						try
						{
							final Room R=r.next();
							A.delProperRoom(R);
							R.destroy();
						}
						catch(final Exception e)
						{
						}
					}
				}
				if(debugMem)
				{
					try
					{
						Object obj = new Object();
						final WeakReference<Object> ref = new WeakReference<Object>(obj);
						obj = null;
						System.gc();
						System.runFinalization();
						while(ref.get() != null)
						{
							System.gc();
						}
						Thread.sleep(3000);
					}
					catch (final Exception e)
					{
					}
					final long free=Runtime.getRuntime().freeMemory()/1024;
					final long total=Runtime.getRuntime().totalMemory()/1024;
					if(A!=null)
						Log.debugOut("Memory: CMMap: "+A.Name()+": "+(total-free)+"/"+total);
				}
			}
			catch (final Exception e)
			{
			}
		}
		areasList.clear();
		deitiesList.clear();
		shipList.clear();
		globalHandlers.clear();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	public void roomMaintSweep()
	{
		final boolean corpsesOnly=CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMITEMS);
		final boolean noMobs=CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMMOBS);
		setThreadStatus(serviceClient,"expiration sweep");
		final long currentTime=System.currentTimeMillis();
		final boolean debug=CMSecurity.isDebugging(CMSecurity.DbgFlag.VACUUM);
		final MOB expireM=getFactoryMOB(null);
		try
		{
			final List<Environmental> stuffToGo=new LinkedList<Environmental>();
			final List<Room> roomsToGo=new LinkedList<Room>();
			final CMMsg expireMsg=CMClass.getMsg(expireM,null,null,CMMsg.MSG_EXPIRE,null);
			for(final Enumeration<Room> r=roomsFilled();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				expireM.setLocation(R);
				expireMsg.setTarget(R);
				if((R.expirationDate()!=0)
				&&(currentTime>R.expirationDate())
				&&(R.okMessage(R,expireMsg)))
					roomsToGo.add(R);
				else
				if(!R.amDestroyed())
				{
					stuffToGo.clear();
					for(int i=0;i<R.numItems();i++)
					{
						final Item I=R.getItem(i);
						if((I!=null)
						&&((!corpsesOnly)||(I instanceof DeadBody))
						&&(I.expirationDate()!=0)
						&&(I.owner()==R)
						&&(currentTime>I.expirationDate()))
							stuffToGo.add(I);
					}
					if(!noMobs)
					{
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.expirationDate()!=0)
							&&(currentTime>M.expirationDate()))
								stuffToGo.add(M);
						}
					}
				}
				if(stuffToGo.size()>0)
				{
					boolean success=true;
					for(final Environmental E : stuffToGo)
					{
						//setThreadStatus(serviceClient,"expiring "+E.Name()); // just too much -- ms count here
						expireMsg.setTarget(E);
						if(R.okMessage(expireM,expireMsg))
							R.sendOthers(expireM,expireMsg);
						else
							success=false;
						if(debug)
							Log.sysOut("UTILITHREAD","Expired "+E.Name()+" in "+getExtendedRoomID(R)+": "+success);
					}
					stuffToGo.clear();
				}
			}
			for(final Room R : roomsToGo)
			{
				expireM.setLocation(R);
				expireMsg.setTarget(R);
				setThreadStatus(serviceClient,"expirating room "+getExtendedRoomID(R));
				if(debug)
				{
					String roomID=getExtendedRoomID(R);
					if(roomID.length()==0)
						roomID="(unassigned grid room, probably in the air)";
					if(debug)
						Log.sysOut("UTILITHREAD","Expired "+roomID+".");
				}
				R.sendOthers(expireM,expireMsg);
			}
		}
		catch(final java.util.NoSuchElementException e)
		{
		}
		setThreadStatus(serviceClient,"title sweeping");
		final LegalLibrary law=CMLib.law();
		final Set<String> playerList=new TreeSet<String>();
		try
		{
			final Set<LandTitle> titlesDone = new HashSet<LandTitle>();
			for(final Enumeration<Area> a=areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if(A.numEffects()>0)
				{
					final LandTitle T=law.getLandTitle(A);
					if((T!=null)
					&&(!titlesDone.contains(T)))
					{
						T.updateLot(playerList);
						titlesDone.add(T);
					}
				}
			}
			for(final Enumeration<Room> r=rooms();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				// roomid > 0? these are unfilled...
				if(R.numEffects()>0)
				{
					for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(A instanceof LandTitle)
						{
							final LandTitle T=(LandTitle)A;
							if(!titlesDone.contains(T))
							{
								T.updateLot(playerList);
								titlesDone.add(T);
							}
						}
					}
				}
			}
		}
		catch(final NoSuchElementException nse)
		{
		}

		setThreadStatus(serviceClient,"cleaning scripts");
		for(final String areaKey : scriptHostMap.keySet())
			cleanScriptHosts(scriptHostMap.get(areaKey), null, true);

		final long lastDateTime=System.currentTimeMillis()-(5*TimeManager.MILI_MINUTE);
		setThreadStatus(serviceClient,"checking");
		try
		{
			for(final Enumeration<Room> r=roomsFilled();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				for(int m=0;m<R.numInhabitants();m++)
				{
					final MOB mob=R.fetchInhabitant(m);
					if(mob == null)
						continue;
					if(mob.amDestroyed())
					{
						R.delInhabitant(mob);
						continue;
					}
					if((mob.lastTickedDateTime()>0)
					&&(mob.lastTickedDateTime()<lastDateTime))
					{
						final boolean ticked=CMLib.threads().isTicking(mob,Tickable.TICKID_MOB);
						final boolean isDead=mob.amDead();
						final Room startR=mob.getStartRoom();
						final String wasFrom=(startR!=null)?startR.roomID():"NULL";
						if(!ticked)
						{
							if(!mob.isPlayer())
							{
								if(ticked)
								{
									// we have a dead group.. let the group handler deal with it.
									Log.errOut(serviceClient.getName(),mob.name()+" in room "+getDescriptiveExtendedRoomID(R)
											+" unticked in dead group (Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+".");
									continue;
								}
								else
								if(mob.amFollowing()!=null)
								{
									if(mob.location()!=R)
										Log.errOut(serviceClient.getName(),"Follower "+mob.name()+" in room "+getDescriptiveExtendedRoomID(R)
										+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob being removed from here."));
									else
										Log.errOut(serviceClient.getName(),"Follower "+mob.name()+" in room "+getDescriptiveExtendedRoomID(R)
										+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob is being ignored."));
								}
								else
								{
									Log.errOut(serviceClient.getName(),mob.name()+" in room "+getDescriptiveExtendedRoomID(R)
									+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been destroyed. May he rest in peace."));
									mob.destroy();
								}
							}
							else
							{
								Log.errOut(serviceClient.getName(),"Player "+mob.name()+" in room "+getDescriptiveExtendedRoomID(R)
										+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been put aside."));
							}
							R.delInhabitant(mob);//keeps it from happening again.
						}
					}
				}
			}
		}
		catch(final java.util.NoSuchElementException e)
		{
		}
		finally
		{
			if(expireM!=null)
				expireM.destroy();
		}
	}

	protected final static char[] cmfsFilenameifyChars=new char[]{'/','\\',' '};

	protected String cmfsFilenameify(final String str)
	{
		return CMStrings.replaceAllofAny(str, cmfsFilenameifyChars, '_').toLowerCase().trim();
	}

	// this is a beautiful idea, but im scared of the memory of all the final refs
	protected void addMapStatFiles(final List<CMFile.CMVFSFile> rootFiles, final Room R, final Environmental E, final CMFile.CMVFSDir root)
	{
		rootFiles.add(new CMFile.CMVFSDir(root,root.getPath()+"stats/")
		{
			@Override
			protected CMFile.CMVFSFile[] getFiles()
			{
				final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
				final String[] stats=E.getStatCodes();
				final String oldName=E.Name();
				for (final String statName : stats)
				{
					final String statValue=E.getStat(statName);
					myFiles.add(new CMFile.CMVFSFile(this.getPath()+statName,256,System.currentTimeMillis(),"SYS")
					{
						@Override
						public int getMaskBits(final MOB accessor)
						{
							if(accessor==null)
								return this.mask;
							if((E instanceof Area)&&(CMSecurity.isAllowed(accessor,((Area)E).getRandomProperRoom(),CMSecurity.SecFlag.CMDAREAS)))
								return this.mask;
							else
							if(CMSecurity.isAllowed(accessor,R,CMSecurity.SecFlag.CMDROOMS))
								return this.mask;
							else
							if((E instanceof MOB) && CMSecurity.isAllowed(accessor,R,CMSecurity.SecFlag.CMDMOBS))
								return this.mask;
							else
							if((E instanceof Item) && CMSecurity.isAllowed(accessor,R,CMSecurity.SecFlag.CMDITEMS))
								return this.mask;
							return this.mask|48;
						}

						@Override
						public Object readData()
						{
							return statValue;
						}

						@Override
						public void saveData(final String filename, final int vfsBits, final String author, final Object O)
						{
							E.setStat(statName, O.toString());
							if(E instanceof Area)
								CMLib.database().DBUpdateArea(oldName, (Area)E);
							else
							if(E instanceof Room)
								CMLib.database().DBUpdateRoom((Room)E);
							else
							if(E instanceof MOB)
								CMLib.database().DBUpdateMOB(R.roomID(), (MOB)E);
							else
							if(E instanceof Item)
								CMLib.database().DBUpdateItem(R.roomID(), (Item)E);
						}
					});
				}
				Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
				return myFiles.toArray(new CMFile.CMVFSFile[0]);
			}
		});
	}

	@Override
	public CMFile.CMVFSDir getMapRoot(final CMFile.CMVFSDir root)
	{
		return new CMFile.CMVFSDir(root,root.getPath()+"map/")
		{
			@Override
			protected CMFile.CMVFSFile[] getFiles()
			{
				final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>(numAreas());
				for(final Enumeration<Area> a=areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					myFiles.add(new CMFile.CMVFSFile(this.getPath()+cmfsFilenameify(A.Name())+".cmare",48,System.currentTimeMillis(),"SYS")
					{
						@Override
						public Object readData()
						{
							return CMLib.coffeeMaker().getAreaXML(A, null, null, null, true);
						}
					});
					myFiles.add(new CMFile.CMVFSDir(this,this.getPath()+cmfsFilenameify(A.Name())+"/")
					{
						@Override
						protected CMFile.CMVFSFile[] getFiles()
						{
							final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
							for(final Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
							{
								final Room R=r.nextElement();
								if(R.roomID().length()>0)
								{
									String roomID=R.roomID();
									if(roomID.startsWith(A.Name()+"#"))
										roomID=roomID.substring(A.Name().length()+1);
									myFiles.add(new CMFile.CMVFSFile(this.getPath()+cmfsFilenameify(R.roomID())+".cmare",48,System.currentTimeMillis(),"SYS")
									{
										@Override
										public Object readData()
										{
											return CMLib.coffeeMaker().getRoomXML(R, null, null, true);
										}
									});
									myFiles.add(new CMFile.CMVFSDir(this,this.getPath()+cmfsFilenameify(roomID).toLowerCase()+"/")
									{
										@Override
										protected CMFile.CMVFSFile[] getFiles()
										{
											final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
											myFiles.add(new CMFile.CMVFSFile(this.getPath()+"items.cmare",48,System.currentTimeMillis(),"SYS")
											{
												@Override
												public Object readData()
												{
													return CMLib.coffeeMaker().getRoomItems(R, new TreeMap<String,List<Item>>(), null, null);
												}
											});
											myFiles.add(new CMFile.CMVFSFile(this.path+"mobs.cmare",48,System.currentTimeMillis(),"SYS")
											{
												@Override
												public Object readData()
												{
													return CMLib.coffeeMaker().getRoomMobs(R, null, null, new TreeMap<String,List<MOB>>());
												}
											});
											myFiles.add(new CMFile.CMVFSDir(this,this.path+"mobs/")
											{
												@Override
												protected CMFile.CMVFSFile[] getFiles()
												{
													final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
													final Room R2=CMLib.coffeeMaker().makeNewRoomContent(R, false);
													if(R2!=null)
													{
														for(int i=0;i<R2.numInhabitants();i++)
														{
															final MOB M=R2.fetchInhabitant(i);
															myFiles.add(new CMFile.CMVFSFile(this.path+cmfsFilenameify(R2.getContextName(M))+".cmare",48,System.currentTimeMillis(),"SYS")
															{
																@Override
																public Object readData()
																{
																	return CMLib.coffeeMaker().getMobXML(M);
																}
															});
															myFiles.add(new CMFile.CMVFSDir(this,this.path+cmfsFilenameify(R2.getContextName(M))+"/")
															{
																@Override
																protected CMFile.CMVFSFile[] getFiles()
																{
																	final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
																	addMapStatFiles(myFiles,R,M,this);
																	Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
																	return myFiles.toArray(new CMFile.CMVFSFile[0]);
																}
															});
														}
														Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
													}
													return myFiles.toArray(new CMFile.CMVFSFile[0]);
												}
											});
											myFiles.add(new CMFile.CMVFSDir(this,this.path+"items/")
											{
												@Override
												protected CMFile.CMVFSFile[] getFiles()
												{
													final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
													final Room R2=CMLib.coffeeMaker().makeNewRoomContent(R, false);
													if(R2 != null)
													{
														for(int i=0;i<R2.numItems();i++)
														{
															final Item I=R2.getItem(i);
															myFiles.add(new CMFile.CMVFSFile(this.path+cmfsFilenameify(R2.getContextName(I))+".cmare",48,System.currentTimeMillis(),"SYS")
															{
																@Override
																public Object readData()
																{
																	return CMLib.coffeeMaker().getItemXML(I);
																}
															});
															myFiles.add(new CMFile.CMVFSDir(this,this.path+cmfsFilenameify(R2.getContextName(I))+"/")
															{
																@Override
																protected CMFile.CMVFSFile[] getFiles()
																{
																	final List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
																	addMapStatFiles(myFiles,R,I,this);
																	Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
																	return myFiles.toArray(new CMFile.CMVFSFile[0]);
																}
															});
														}
														Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
														return myFiles.toArray(new CMFile.CMVFSFile[0]);
													}
													return new CMFile.CMVFSFile[0];
												}
											});
											addMapStatFiles(myFiles,R,R,this);
											Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
											return myFiles.toArray(new CMFile.CMVFSFile[0]);
										}
									});
								}
							}
							addMapStatFiles(myFiles,null,A,this);
							Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
							return myFiles.toArray(new CMFile.CMVFSFile[0]);
						}
					});
				}
				Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
				return myFiles.toArray(new CMFile.CMVFSFile[0]);
			}
		};
	}

}
