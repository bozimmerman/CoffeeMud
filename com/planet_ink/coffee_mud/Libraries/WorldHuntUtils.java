package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.collections.MultiEnumeration.MultiEnumeratorBuilder;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.util.*;

import org.mozilla.javascript.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class WorldHuntUtils extends StdLibrary implements WorldHuntLibrary
{
	@Override
	public String ID()
	{
		return "WorldHuntUtils";
	}

	private static final long EXPIRE_1MIN	= 1*60*1000;
	private static final long EXPIRE_5MINS	= 5*60*1000;
	private static final long EXPIRE_10MINS	= 10*60*1000;
	private static final long EXPIRE_20MINS	= 20*60*1000;
	private static final long EXPIRE_30MINS	= 30*60*1000;
	private static final long EXPIRE_1HOUR	= 60*60*1000;

	@Override
	public List<Room> findRooms(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean displayOnly, final int timePct)
	{
		final Vector<Room> roomsV=new Vector<Room>();
		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,CMLib.map().getRoom(mob.location().getArea().Name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,CMLib.map().getRoom(srchStr));
		addWorldRoomsLiberally(roomsV,findRooms(rooms,mob,srchStr,displayOnly,false,timePct));
		return roomsV;
	}

	@Override
	public Room findFirstRoom(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean displayOnly, final int timePct)
	{
		final Vector<Room> roomsV=new Vector<Room>();
		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,CMLib.map().getRoom(mob.location().getArea().Name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,CMLib.map().getRoom(srchStr));
		if(roomsV.size()>0)
			return roomsV.firstElement();
		addWorldRoomsLiberally(roomsV,findRooms(rooms,mob,srchStr,displayOnly,true,timePct));
		if(roomsV.size()>0)
			return roomsV.firstElement();
		return null;
	}

	public List<Room> findRooms(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean displayOnly, final boolean returnFirst, final int timePct)
	{
		final List<Room> foundRooms=new Vector<Room>();
		Vector<Room> completeRooms=null;
		try
		{
			completeRooms=new XVector<Room>(rooms);
		}
		catch(final Exception nse)
		{
			Log.errOut("CMMap",nse);
			completeRooms=new Vector<Room>();
		}
		final long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);

		Enumeration<Room> enumSet;
		enumSet=completeRooms.elements();
		while(enumSet.hasMoreElements())
		{
			findRoomsByDisplay(mob,enumSet,foundRooms,srchStr,returnFirst,delay);
			if((returnFirst)&&(foundRooms.size()>0))
				return foundRooms;
			if(enumSet.hasMoreElements()) CMLib.s_sleep(1000 - delay);
		}
		if(!displayOnly)
		{
			enumSet=completeRooms.elements();
			while(enumSet.hasMoreElements())
			{
				findRoomsByDesc(mob,enumSet,foundRooms,srchStr,returnFirst,delay);
				if((returnFirst)&&(foundRooms.size()>0))
					return foundRooms;
				if(enumSet.hasMoreElements()) CMLib.s_sleep(1000 - delay);
			}
		}
		return foundRooms;
	}

	protected void findRoomsByDisplay(final MOB mob, final Enumeration<Room> rooms, final List<Room> foundRooms, String srchStr, final boolean returnFirst, final long maxTime)
	{
		final long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			final boolean useTimer=maxTime>1;
			Room room;
			for(;rooms.hasMoreElements();)
			{
				room=rooms.nextElement();
				if((CMLib.english().containsString(CMStrings.removeColors(room.displayText(mob)),srchStr))
				&&((mob==null)||CMLib.flags().canAccess(mob,room)))
					foundRooms.add(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
		}
		catch (final NoSuchElementException nse)
		{
		}
	}

	protected void findRoomsByDesc(final MOB mob, final Enumeration<Room> rooms, final List<Room> foundRooms, String srchStr, final boolean returnFirst, final long maxTime)
	{
		final long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			final boolean useTimer=maxTime>1;
			for(;rooms.hasMoreElements();)
			{
				final Room room=rooms.nextElement();
				if((CMLib.english().containsString(CMStrings.removeColors(room.description()),srchStr))
				&&((mob==null)||CMLib.flags().canAccess(mob,room)))
					foundRooms.add(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
		}
		catch (final NoSuchElementException nse)
		{
		}
	}

	@Override
	public List<MOB> findInhabitants(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		return findInhabitants(rooms,mob,srchStr,false,timePct);
	}

	@Override
	public MOB findFirstInhabitant(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		final List<MOB> found=findInhabitants(rooms,mob,srchStr,true,timePct);
		if(found.size()>0)
			return found.get(0);
		return null;
	}

	public List<MOB> findInhabitants(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean returnFirst, final int timePct)
	{
		final Vector<MOB> found=new Vector<MOB>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000)
			delay=1000;
		final boolean useTimer = delay>1;
		final boolean allRoomsAllowed=(mob==null);
		long startTime=System.currentTimeMillis();
		Room room;
		for(;rooms.hasMoreElements();)
		{
			room=rooms.nextElement();
			if((room != null) && (allRoomsAllowed || CMLib.flags().canAccess(mob,room)))
			{
				found.addAll(room.fetchInhabitants(srchStr));
				if((returnFirst)&&(found.size()>0))
					return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay))
			{
				CMLib.s_sleep(1000 - delay);
				startTime=System.currentTimeMillis();
			}
		}
		return found;
	}

	@Override
	public List<MOB> findInhabitantsFavorExact(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean returnFirst, final int timePct)
	{
		final Vector<MOB> found=new Vector<MOB>();
		final Vector<MOB> exact=new Vector<MOB>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000)
			delay=1000;
		final boolean useTimer = delay>1;
		final boolean allRoomsAllowed=(mob==null);
		long startTime=System.currentTimeMillis();
		Room room;
		for(;rooms.hasMoreElements();)
		{
			room=rooms.nextElement();
			if((room != null) && (allRoomsAllowed || CMLib.flags().canAccess(mob,room)))
			{
				final MOB M=room.fetchInhabitantExact(srchStr);
				if(M!=null)
				{
					exact.add(M);
					if((returnFirst)&&(exact.size()>0))
						return exact;
				}
				found.addAll(room.fetchInhabitants(srchStr));
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay))
			{
				CMLib.s_sleep(1000 - delay);
				startTime=System.currentTimeMillis();
			}
		}
		if(exact.size()>0)
			return exact;
		if((returnFirst)&&(found.size()>0))
		{
			exact.add(found.get(0));
			return exact;
		}
		return found;
	}

	@Override
	public List<Item> findInventory(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		return findInventory(rooms,mob,srchStr,false,timePct);
	}

	@Override
	public Item findFirstInventory(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		final List<Item> found=findInventory(rooms,mob,srchStr,true,timePct);
		if(found.size()>0)
			return found.get(0);
		return null;
	}

	public List<Item> findInventory(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean returnFirst, final int timePct)
	{
		final List<Item> found=new Vector<Item>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000)
			delay=1000;
		final boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		MOB M;
		Room room;
		if(rooms==null)
		{
			for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
			{
				M=e.nextElement();
				if(M!=null)
					found.addAll(M.findItems(srchStr));
				if((returnFirst)&&(found.size()>0))
					return found;
			}
		}
		else
		for(;rooms.hasMoreElements();)
		{
			room=rooms.nextElement();
			if((room != null) && ((mob==null)||CMLib.flags().canAccess(mob,room)))
			{
				for(int m=0;m<room.numInhabitants();m++)
				{
					M=room.fetchInhabitant(m);
					if(M!=null)
						found.addAll(M.findItems(srchStr));
				}
				if((returnFirst)&&(found.size()>0))
					return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay))
			{
				CMLib.s_sleep(1000 - delay);
				startTime=System.currentTimeMillis();
			}
		}
		return found;
	}

	@Override
	public List<Environmental> findShopStock(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		return findShopStock(rooms,mob,srchStr,false,false,timePct);
	}

	@Override
	public Environmental findFirstShopStock(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		final List<Environmental> found=findShopStock(rooms,mob,srchStr,true,false,timePct);
		if(found.size()>0)
			return found.get(0);
		return null;
	}

	@Override
	public List<Environmental> findShopStockers(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		return findShopStock(rooms,mob,srchStr,false,true,timePct);
	}

	@Override
	public Environmental findFirstShopStocker(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final int timePct)
	{
		final List<Environmental> found=findShopStock(rooms,mob,srchStr,true,true,timePct);
		if(found.size()>0)
			return found.get(0);
		return null;
	}

	public List<Environmental> findShopStock(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean returnFirst, final boolean returnStockers, final int timePct)
	{
		final XVector<Environmental> found=new XVector<Environmental>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000)
			delay=1000;
		final boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		MOB M=null;
		Item I=null;
		final HashSet<ShopKeeper> stocks=new HashSet<ShopKeeper>(1);
		final HashSet<Area> areas=new HashSet<Area>();
		ShopKeeper SK=null;
		final boolean allRoomsAllowed=(mob==null);
		if(rooms==null)
		{
			for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
			{
				M=e.nextElement();
				if(M!=null)
				{
					SK=CMLib.coffeeShops().getShopKeeper(M);
					if((SK!=null)&&(!stocks.contains(SK)))
					{
						stocks.add(SK);
						final Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
						if(ei.hasNext())
						{
							if(returnFirst)
								return (returnStockers)?new XVector<Environmental>(M):new XVector<Environmental>(ei);
							if(returnStockers)
								found.add(M);
							else
								found.addAll(ei);
						}
					}
					for(int i=0;i<M.numItems();i++)
					{
						I=M.getItem(i);
						if(I!=null)
						{
							SK=CMLib.coffeeShops().getShopKeeper(I);
							if((SK!=null)&&(!stocks.contains(SK)))
							{
								stocks.add(SK);
								final Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
								if(ei.hasNext())
								{
									if(returnFirst)
										return (returnStockers)?new XVector<Environmental>(I):new XVector<Environmental>(ei);
									if(returnStockers)
										found.add(I);
									else
										found.addAll(ei);
								}
							}
						}
					}
				}
				if((useTimer)&&((System.currentTimeMillis()-startTime)>delay))
				{
					try
					{
						Thread.sleep(1000 - delay);
						startTime = System.currentTimeMillis();
					}
					catch (final Exception ex)
					{
					}
				}
			}
		}
		else
		for(;rooms.hasMoreElements();)
		{
			final Room room=rooms.nextElement();
			if((room != null) && (allRoomsAllowed||CMLib.flags().canAccess(mob,room)))
			{
				if(!areas.contains(room.getArea()))
					areas.add(room.getArea());
				SK=CMLib.coffeeShops().getShopKeeper(room);
				if((SK!=null)&&(!stocks.contains(SK)))
				{
					stocks.add(SK);
					final Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
					if(ei.hasNext())
					{
						if(returnFirst)
							return (returnStockers)?new XVector<Environmental>(room):new XVector<Environmental>(ei);
						if(returnStockers)
							found.add(room);
						else
							found.addAll(ei);
					}
				}
				for(int m=0;m<room.numInhabitants();m++)
				{
					M=room.fetchInhabitant(m);
					if(M!=null)
					{
						SK=CMLib.coffeeShops().getShopKeeper(M);
						if((SK!=null)&&(!stocks.contains(SK)))
						{
							stocks.add(SK);
							final Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
							if(ei.hasNext())
							{
								if(returnFirst)
									return (returnStockers)?new XVector<Environmental>(M):new XVector<Environmental>(ei);
								if(returnStockers)
									found.add(M);
								else
									found.addAll(ei);
							}
						}
					}
				}
				for(int i=0;i<room.numItems();i++)
				{
					I=room.getItem(i);
					if(I!=null)
					{
						SK=CMLib.coffeeShops().getShopKeeper(I);
						if((SK!=null)&&(!stocks.contains(SK)))
						{
							stocks.add(SK);
							final Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
							if(ei.hasNext())
							{
								if(returnFirst)
									return (returnStockers)?new XVector<Environmental>(I):new XVector<Environmental>(ei);
								if(returnStockers)
									found.add(I);
								else
									found.addAll(ei);
							}
						}
					}
				}
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay))
			{
				CMLib.s_sleep(1000 - delay);
				startTime=System.currentTimeMillis();
			}
		}
		for (final Area A : areas)
		{
			SK=CMLib.coffeeShops().getShopKeeper(A);
			if((SK!=null)&&(!stocks.contains(SK)))
			{
				stocks.add(SK);
				final Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
				if(ei.hasNext())
				{
					if(returnFirst)
						return (returnStockers)?new XVector<Environmental>(A):new XVector<Environmental>(ei);
					if(returnStockers)
						found.add(A);
					else
						found.addAll(ei);
				}
			}
		}
		return found;
	}

	@Override
	public List<Item> findRoomItems(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean anyItems, final int timePct)
	{
		return findRoomItems(rooms,mob,srchStr,anyItems,false,timePct);
	}

	@Override
	public Item findFirstRoomItem(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean anyItems, final int timePct)
	{
		final List<Item> found=findRoomItems(rooms,mob,srchStr,anyItems,true,timePct);
		if(found.size()>0)
			return found.get(0);
		return null;
	}

	protected List<Item> findRoomItems(final Enumeration<Room> rooms, final MOB mob, final String srchStr, final boolean anyItems, final boolean returnFirst, final int timePct)
	{
		final Vector<Item> found=new Vector<Item>(); // ultimate return value
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000)
			delay=1000;
		final boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		final boolean allRoomsAllowed=(mob==null);
		Room room;
		for(;rooms.hasMoreElements();)
		{
			room=rooms.nextElement();
			if((room != null) && (allRoomsAllowed||CMLib.flags().canAccess(mob,room)))
			{
				found.addAll(anyItems?room.findItems(srchStr):room.findItems(null,srchStr));
				if((returnFirst)&&(found.size()>0))
					return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay))
			{
				CMLib.s_sleep(1000 - delay);
				startTime=System.currentTimeMillis();
			}
		}
		return found;
	}

	@Override
	public Room findWorldRoomLiberally(final MOB mob, final String cmd, final String srchWhatAERIPMVK, final int timePct, final long maxMillis)
	{
		final List<Room> rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,true,timePct, maxMillis);
		if((rooms!=null)&&(rooms.size()!=0))
			return rooms.get(0);
		return null;
	}

	@Override
	public List<Room> findWorldRoomsLiberally(final MOB mob, final String cmd, final String srchWhatAERIPMVK, final int timePct, final long maxMillis)
	{
		return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,false,timePct,maxMillis);
	}

	@Override
	public Room findAreaRoomLiberally(final MOB mob, final Area A,final String cmd, final String srchWhatAERIPMVK, final int timePct)
	{
		final List<Room> rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,true,timePct,120);
		if((rooms!=null)&&(rooms.size()!=0))
			return rooms.get(0);
		return null;
	}

	@Override
	public List<Room> findAreaRoomsLiberally(final MOB mob, final Area A,final String cmd, final String srchWhatAERIPMVK, final int timePct)
	{
		return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,false,timePct,120);
	}

	protected Room addWorldRoomsLiberally(final List<Room> rooms, final List<? extends Environmental> choicesV)
	{
		if(choicesV==null)
			return null;
		if(rooms!=null)
		{
			for(final Environmental E : choicesV)
				addWorldRoomsLiberally(rooms,CMLib.map().roomLocation(E));
			return null;
		}
		else
		{
			Room room=null;
			int tries=0;
			while(((room==null)||(room.roomID().length()==0))&&((++tries)<200))
				room=CMLib.map().roomLocation(choicesV.get(CMLib.dice().roll(1,choicesV.size(),-1)));
			return room;
		}
	}

	protected Room addWorldRoomsLiberally(final List<Room> rooms, final Room room)
	{
		if(room==null)
			return null;
		if(rooms!=null)
		{
			if(!rooms.contains(room))
				rooms.add(room);
			return null;
		}
		return room;
	}

	protected Room addWorldRoomsLiberally(final List<Room>rooms, final Area area)
	{
		if((area==null)||(area.isProperlyEmpty()))
			return null;
		return addWorldRoomsLiberally(rooms,area.getRandomProperRoom());
	}

	protected List<Room> returnResponse(final List<Room> rooms, final Room room)
	{
		if(rooms!=null)
			return rooms;
		if(room==null)
			return new Vector<Room>(1);
		return new XVector<Room>(room);
	}

	protected boolean enforceTimeLimit(final long startTime,  final long maxMillis)
	{
		if(maxMillis<=0)
			return false;
		return ((System.currentTimeMillis() - startTime)) > maxMillis;
	}

	protected List<MOB> checkMOBCachedList(final List<MOB> list)
	{
		if (list != null)
		{
			for(final Environmental E : list)
				if(E.amDestroyed())
					return null;
		}
		return list;
	}

	protected List<Item> checkInvCachedList(final List<Item> list)
	{
		if (list != null)
		{
			for(final Item E : list)
				if((E.amDestroyed())||(!(E.owner() instanceof MOB)))
					return null;
		}
		return list;
	}

	protected List<Item> checkRoomItemCachedList(final List<Item> list)
	{
		if (list != null)
		{
			for(final Item E : list)
				if((E.amDestroyed())||(!(E.owner() instanceof Room)))
					return null;
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public Map<String,List<MOB>> getMOBFinder()
	{
		Map<String,List<MOB>> finder=(Map<String,List<MOB>>)Resources.getResource("SYSTEM_MOB_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<MOB>>(10,EXPIRE_5MINS,EXPIRE_10MINS,100);
			Resources.submitResource("SYSTEM_MOB_FINDER_CACHE",finder);
		}
		return finder;
	}
	@SuppressWarnings("unchecked")
	public Map<String,Area> getAreaFinder()
	{
		Map<String,Area> finder=(Map<String,Area>)Resources.getResource("SYSTEM_AREA_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,Area>(50,EXPIRE_30MINS,EXPIRE_1HOUR,100);
			Resources.submitResource("SYSTEM_AREA_FINDER_CACHE",finder);
		}
		return finder;
	}

	@SuppressWarnings("unchecked")
	public Map<String,List<Item>> getRoomItemFinder()
	{
		Map<String,List<Item>> finder=(Map<String,List<Item>>)Resources.getResource("SYSTEM_RITEM_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Item>>(10,EXPIRE_5MINS,EXPIRE_10MINS,100);
			Resources.submitResource("SYSTEM_RITEM_FINDER_CACHE",finder);
		}
		return finder;
	}

	@SuppressWarnings("unchecked")
	public Map<String,List<Item>> getInvItemFinder()
	{
		Map<String,List<Item>> finder=(Map<String,List<Item>>)Resources.getResource("SYSTEM_IITEM_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Item>>(10,EXPIRE_1MIN,EXPIRE_10MINS,100);
			Resources.submitResource("SYSTEM_IITEM_FINDER_CACHE",finder);
		}
		return finder;
	}

	@SuppressWarnings("unchecked")
	public Map<String,List<Environmental>> getStockFinder()
	{
		Map<String,List<Environmental>> finder=(Map<String,List<Environmental>>)Resources.getResource("SYSTEM_STOCK_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Environmental>>(10,EXPIRE_10MINS,EXPIRE_1HOUR,100);
			Resources.submitResource("SYSTEM_STOCK_FINDER_CACHE",finder);
		}
		return finder;
	}

	@SuppressWarnings("unchecked")
	public Map<String,List<Room>> getRoomFinder()
	{
		Map<String,List<Room>> finder=(Map<String,List<Room>>)Resources.getResource("SYSTEM_ROOM_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Room>>(20,EXPIRE_20MINS,EXPIRE_1HOUR,100);
			Resources.submitResource("SYSTEM_ROOM_FINDER_CACHE",finder);
		}
		return finder;
	}

	protected List<Room> findWorldRoomsLiberally(final MOB mob,
												 final String cmd,
												 final String srchWhatAERIPMVK,
												 Area area,
												 final boolean returnFirst,
												 final int timePct,
												 final long maxMillis)
	{
		Room room=null;
		// wish this stuff could be cached, even temporarily, however,
		// far too much of the world is dynamic, and far too many searches
		// are looking for dynamic things.  the cached results would be useless
		// as soon as they are put away -- that's why the limited caches time them out!
		final boolean disableCaching= CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE);

		final Vector<Room> rooms=(returnFirst)?null:new Vector<Room>();
		final Room curRoom=(mob!=null)?mob.location():null;

		boolean searchWeakAreas=false;
		boolean searchStrictAreas=false;
		boolean searchRooms=false;
		boolean searchPlayers=false;
		boolean searchItems=false;
		boolean searchInhabs=false;
		boolean searchInventories=false;
		boolean searchStocks=false;
		final char[] flags = srchWhatAERIPMVK.toUpperCase().toCharArray();
		for (final char flag : flags)
		{
			switch(flag)
			{
			case 'E':
				searchWeakAreas = true;
				break;
			case 'A':
				searchStrictAreas = true;
				break;
			case 'R':
				searchRooms = true;
				break;
			case 'P':
				searchPlayers = true;
				break;
			case 'I':
				searchItems = true;
				break;
			case 'M':
				searchInhabs = true;
				break;
			case 'V':
				searchInventories = true;
				break;
			case 'K':
				searchStocks = true;
				break;
			}
		}
		final long startTime = System.currentTimeMillis();
		if(searchRooms)
		{
			final int dirCode=CMLib.directions().getGoodDirectionCode(cmd);
			if((dirCode>=0)&&(curRoom!=null))
				room=addWorldRoomsLiberally(rooms,curRoom.rawDoors()[dirCode]);
			if(room==null)
				room=addWorldRoomsLiberally(rooms,CMLib.map().getRoom(cmd));
			if((room == null) && (curRoom != null) && (curRoom.getArea()!=null))
				room=addWorldRoomsLiberally(rooms,curRoom.getArea().getRoom(cmd));
		}

		if(room==null)
		{
			// first get room ids
			if((cmd.charAt(0)=='#')&&(curRoom!=null)&&(searchRooms))
			{
				room=addWorldRoomsLiberally(rooms,CMLib.map().getRoom(curRoom.getArea().Name()+cmd));
				if(room == null)
					room=addWorldRoomsLiberally(rooms,curRoom.getArea().getRoom(curRoom.getArea().Name()+cmd));
			}
			else
			{
				final String srchStr=cmd;

				if(searchPlayers)
				{
					// then look for players
					final MOB M=CMLib.sessions().findCharacterOnline(srchStr,false);
					if(M!=null)
						room=addWorldRoomsLiberally(rooms,M.location());
				}
				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				// search areas strictly
				if(searchStrictAreas && (room==null) && (area==null))
				{
					area=CMLib.map().getArea(srchStr);
					if((area!=null) &&(area.properSize()>0) &&(area.getProperRoomnumbers().roomCountAllAreas()>0))
						room=addWorldRoomsLiberally(rooms,area);
					area=null;
				}
				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				final Area A=area;
				final MultiEnumeratorBuilder<Room> roomer = new MultiEnumeratorBuilder<Room>()
				{
					@Override
					public MultiEnumeration<Room> getList()
					{
						if(A==null)
							return new MultiEnumeration<Room>(CMLib.map().roomsFilled());
						else
							return new MultiEnumeration<Room>()
									.addEnumeration(A.getProperMap())
									.addEnumeration(CMLib.map().shipsRoomEnumerator(A));
					}
				};

				// no good, so look for room inhabitants
				if(searchInhabs && room==null)
				{
					final Map<String,List<MOB>> finder=getMOBFinder();
					List<MOB> candidates=null;

					if((mob==null)||(mob.isMonster()))
					{
						candidates=checkMOBCachedList(finder.get(srchStr.toLowerCase()));
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<MOB>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findInhabitants(roomer.getList(), mob, srchStr,returnFirst, timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);

					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				// now check room text
				if(searchRooms && room==null)
				{
					final Map<String,List<Room>> finder=getRoomFinder();
					List<Room> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=finder.get(srchStr.toLowerCase());
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Room>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findRooms(roomer.getList(), mob, srchStr, false,returnFirst, timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				// check floor items
				if(searchItems && room==null)
				{
					final Map<String,List<Item>> finder=getRoomItemFinder();
					List<Item> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=checkRoomItemCachedList(finder.get(srchStr.toLowerCase()));
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Item>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findRoomItems(roomer.getList(), mob, srchStr, false,returnFirst,timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				// check inventories
				if(searchInventories && room==null)
				{
					final Map<String,List<Item>> finder=getInvItemFinder();
					List<Item> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=checkInvCachedList(finder.get(srchStr.toLowerCase()));
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Item>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findInventory(roomer.getList(), mob, srchStr, returnFirst,timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				// check stocks
				if(searchStocks && room==null)
				{
					final Map<String,List<Environmental>> finder=getStockFinder();
					List<Environmental> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=finder.get(srchStr.toLowerCase());
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Environmental>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findShopStock(roomer.getList(), mob, srchStr, returnFirst,false,timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis))
					return returnResponse(rooms,room);

				// search areas weakly
				if(searchWeakAreas && (room==null) && (A==null))
				{
					final Area A2=CMLib.map().findArea(srchStr);
					if((A2!=null) &&(A2.properSize()>0) &&(A2.getProperRoomnumbers().roomCountAllAreas()>0))
						room=addWorldRoomsLiberally(rooms,A2);
				}
			}
		}
		final List<Room> responseSet = returnResponse(rooms,room);
		return responseSet;
	}

	protected boolean isOwnerHere(final CMObject ownerR, final Room here)
	{
		if(ownerR instanceof Room)
		{
			if(ownerR == here)
				return true;
			final Area A = ((Room)ownerR).getArea();
			if(A instanceof Boardable)
			{
				final Boardable B = (Boardable)A;
				final Item I = B.getBoardableItem();
				if(I!=null)
					return (I.owner()==here);
			}
		}
		return false;
	}

	@Override
	public boolean isHere(final CMObject E2, final Room here)
	{
		if(E2==null)
			return false;
		else
		if(E2==here)
			return true;
		else
		if((E2 instanceof MOB)
		&&(isOwnerHere(((MOB)E2).location(),here)))
			return true;
		else
		if((E2 instanceof Item)
		&&(isOwnerHere(((Item)E2).owner(),here)))
			return true;
		else
		if((E2 instanceof Item)
		&&(((Item)E2).owner() instanceof MOB)
		&&(isOwnerHere(((MOB)((Item)E2).owner()).location(),here)))
			return true;
		else
		if(E2 instanceof Exit)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(here.getRawExit(d)==E2)
					return true;
			}
		}
		else
		{
		}
		return false;
	}

	@Override
	public boolean isHere(final CMObject E2, final Area here)
	{
		if(E2==null)
			return false;
		else
		if(E2==here)
			return true;
		else
		if(E2 instanceof Room)
			return ((Room)E2).getArea()==here;
		else
		if(E2 instanceof MOB)
			return isHere(((MOB)E2).location(),here);
		else
		if(E2 instanceof Item)
			return isHere(((Item)E2).owner(),here);
		return false;
	}

	@Override
	public boolean isAnAdminHere(final Room R, final boolean sysMsgsOnly)
	{
		final Set<MOB> mobsThere=CMLib.players().getPlayersHere(R);
		if(mobsThere.size()>0)
		{
			try
			{
				for(final MOB inhab : mobsThere)
				{
					if((inhab.session()!=null)
					&&(CMSecurity.isAllowed(inhab,R,CMSecurity.SecFlag.CMDMOBS)||CMSecurity.isAllowed(inhab,R,CMSecurity.SecFlag.CMDROOMS))
					&&(CMLib.flags().isInTheGame(inhab, true))
					&&((!sysMsgsOnly) || inhab.isAttributeSet(MOB.Attrib.SYSOPMSGS)))
						return true;
				}
			}
			catch(final java.util.ConcurrentModificationException e)
			{
				return isAnAdminHere(R,sysMsgsOnly);
			}
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Set<Physical> getAllGroupRiders(final Physical P, final Set<Physical> set)
	{
		if((P==null)||(set.contains(P)))
			return set;
		set.add(P);
		if(P instanceof Followable)
		{
			getAllGroupRiders(((Followable)P).amFollowing(), set);
			for(final Enumeration<Pair<MOB,Short>> f=((Followable)P).followers();f.hasMoreElements();)
				getAllGroupRiders(f.nextElement().first, set);
		}
		if(P instanceof Rider)
			getAllGroupRiders(((Rider)P).riding(), set);
		if(P instanceof Rideable)
		{
			for(final Enumeration<Rider> r=((Rideable)P).riders();r.hasMoreElements();)
				getAllGroupRiders(r.nextElement(), set);
		}
		return set;
	}

	@Override
	public Set<Physical> getAllGroupRiders(final Physical P, final Room hereOnlyR)
	{
		final Set<Physical> set=getAllGroupRiders(P, new HashSet<Physical>());
		if(hereOnlyR == null)
			return set;
		final WorldMap map=CMLib.map();
		for(final Iterator<Physical> p=set.iterator();p.hasNext();)
		{
			final Room R=map.roomLocation(p.next());
			if(R!=hereOnlyR)
				p.remove();
		}
		return set;
	}

}
