package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2023 Bo Zimmerman

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
public class Prop_LotsForSale extends Prop_RoomForSale
{
	@Override
	public String ID()
	{
		return "Prop_LotsForSale";
	}

	@Override
	public String name()
	{
		return "Putting many rooms up for sale";
	}

	protected String	uniqueLotID	= null;

	protected volatile long			lastRoomsTimestamp	= 0;
	protected volatile Area			lastArea			= null;
	protected final    List<Room>	lastRoomsV			= new ArrayList<Room>();

	@Override
	public boolean allowsExpansionConstruction()
	{
		return true;
	}

	@Override
	public Room getAConnectedPropertyRoom()
	{
		if(affected instanceof Room)
			return (Room)affected;
		return CMLib.map().getRoom(landPropertyID());
	}

	@Override
	public int getNumConnectedPropertyRooms()
	{
		return getConnectedPropertyRooms().size();
	}

	protected void fillLotsCluster(final Room R, final List<Room> roomsV)
	{
		fillCluster(R, roomsV, null, true);
	}

	protected List<Room> getConnectedPropertyRooms()
	{
		final List<Room> roomsV=new ArrayList<Room>();
		Room R=getAConnectedPropertyRoom();
		if(R!=null)
		{
			final Area A=R.getArea();
			if((A==this.lastArea)
			&&(A.getProperRoomnumbers().getLastChangedMs() == this.lastRoomsTimestamp))
				roomsV.addAll(this.lastRoomsV);
			else
			{
				fillLotsCluster(R,roomsV);
				String uniqueID="LOTS_PROPERTY_"+this;
				if(roomsV.size()>0)
					uniqueID="LOTS_PROPERTY_"+CMLib.map().getExtendedRoomID(roomsV.get(0));
				for(final Iterator<Room> r=roomsV.iterator();r.hasNext();)
				{
					Ability bA=null;
					R=r.next();
					if(R!=null)
						bA=R.fetchEffect(ID());
					if(bA instanceof Prop_LotsForSale)
						((Prop_LotsForSale)bA).uniqueLotID=uniqueID;
				}
				this.lastArea = A;
				this.lastRoomsV.clear();
				this.lastRoomsV.addAll(roomsV);
				this.lastRoomsTimestamp = A.getProperRoomnumbers().getLastChangedMs();
			}
		}
		else
			uniqueLotID="";
		return roomsV;

	}

	protected boolean isRetractableGridLink(final Map<Room,boolean[]> recurseChkRooms, final Room fromRoom, final Room theRoom)
	{
		// the only potentially retractable rooms are those that ARE for sale, and NOT owned
		if(theRoom==null)
			return true;

		if(recurseChkRooms.containsKey(theRoom))
		{
			final boolean[] B=recurseChkRooms.get(theRoom);
			if(B.length>0)
				return B[0];
		}

		// never retract across areas!
		if((fromRoom != null)
		&&(theRoom.getArea()!=fromRoom.getArea()))
			return false;

		final LegalLibrary theLaw=CMLib.law();

		// if its a legit room and either not for sale, or owned already, then it is NOT retractable.
		if(theRoom.roomID().length()>0)
		{
			final LandTitle theTitle=theLaw.getLandTitle(theRoom);
			if((theTitle==null)||(theTitle.getOwnerName().length()>0))
			{
				recurseChkRooms.put(theRoom, new boolean[]{false});
				return false;
			}
		}

		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room R=theRoom.getRawDoor(d);
			if((R!=null)
			&&(R!=fromRoom)
			&&(R.roomID().length()>0))
			{
				if(!R.isSavable())
					return false; // if its not cached, it can't be retracted
				if(recurseChkRooms.containsKey(R))
				{
					final boolean[] B=recurseChkRooms.get(R);
					if(B.length>0)
						return B[0];
					continue;
				}
				recurseChkRooms.put(R, new boolean[0]);
				if(!isRetractableGridLink(recurseChkRooms,theRoom,R))
				{
					recurseChkRooms.put(theRoom, new boolean[]{false});
					return false;
				}
			}
		}
		recurseChkRooms.put(theRoom, new boolean[]{true});
		return true;
	}

	@Override
	public String getUniqueLotID()
	{
		if(uniqueLotID==null)
			getConnectedPropertyRooms();
		return uniqueLotID;
	}

	@Override
	public LandTitle generateNextRoomTitle()
	{
		final LandTitle newTitle=(LandTitle)this.copyOf();
		newTitle.setOwnerName("");
		newTitle.setBackTaxes(0);
		this.lastRoomsTimestamp=0;
		return newTitle;
	}

	protected boolean canGenerateAdjacentRooms(final Room R)
	{
		return getOwnerName().length()>0;
	}

	/**
	 * Given a room that is 1. this land title, and 2. has no owner (or otherwise can't expand),
	 * this will see if the room qualifies for deletion, and do so if it can.
	 * @param R the room to work on
	 * @param postWork a set of runnables that do the actual deleting.
	 * @return true if the room is schedules for deletion.
	 */
	protected boolean retractRooms(final Room R, final List<Runnable> postWork)
	{
		final int numKillableLinks = this.gridLayout()?2:1;
		int linksToOtherLotForSaleRoomsThatAreForSale = 0;
		int numDoors = 0;
		final PairList<Room,Integer> linkBack = new PairArrayList<Room,Integer>(4);
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if(d==Directions.GATE)
				continue;
			final Room R2=R.getRawDoor(d);
			if(R2==null)
				continue;
			if(R2.roomID().length()==0) // skys and underwater don't count
				continue;
			numDoors++;
			// check for exceptions that protect Room R FOREVER!
			if(R2.getArea() != R.getArea())
				return false;
			// check the loop back, and whether this is an actual peer
			final Room RR2 = R2.getRawDoor(Directions.getOpDirectionCode(d));
			if((RR2 != null)
			&&((RR2!=R)
			&&(!RR2.roomID().equalsIgnoreCase(R2.roomID()))))
				return false; // if links to a room that doesn't link back, protect it!
			if(canGenerateAdjacentRooms(R2))
				return false;
			// loop back passed; area check passed; so now check the title
			final LandTitle lotA;
			if((!R2.isSavable())&&(R2.numEffects()==0)) // might be thin
			{
				final Room realR2=CMLib.database().DBReadRoomObject(R2.roomID(), false);
				lotA = (realR2 != null) ? (LandTitle)realR2.fetchEffect(ID()) : null;
			}
			else
				lotA = (LandTitle)R2.fetchEffect(ID());
			if(lotA != null)
			{
				if((lotA.getOwnerName()!=null)
				&&(lotA.getOwnerName().trim().length()==0))
				{
					linksToOtherLotForSaleRoomsThatAreForSale++;
					linkBack.add(R2,Integer.valueOf(Directions.getOpDirectionCode(d)));
				}
				else
					return false; // linked to an owned room means R is safe
			}
			else
			if(CMLib.law().getLandTitle(R2)!=null)
				return false; // linked to another titled room means R is safe
		}
		if((linksToOtherLotForSaleRoomsThatAreForSale == 1)
		||(linksToOtherLotForSaleRoomsThatAreForSale == numKillableLinks)
		||(numDoors == 0))
		{
			for(final Pair<Room,Integer> p : linkBack)
			{
				final Room lbR=p.first;
				final int linkBackDir = p.second.intValue();
				if((lbR!=null)
				&&(lbR.getRawDoor(linkBackDir)==R))
				{
					// this might help obliterate,
					// and with future work on the same thread
					lbR.setRawExit(linkBackDir, null);
					lbR.setRawDoor(linkBackDir,null);
				}
				postWork.add(new Runnable()
				{
					final Room room=R;
					final Room lbroom=lbR;
					@Override
					public void run()
					{
						final Room obliteR = CMLib.map().getRoom(room);
						if(obliteR != null)
							CMLib.map().obliterateMapRoom(obliteR);
						final Room updateR = CMLib.map().getRoom(lbroom);
						if((updateR != null)
						&&(updateR.getArea()!=null))
						{
							CMLib.database().DBUpdateExits(updateR);
							updateR.getArea().fillInAreaRoom(updateR);
						}
					}
				});
			}
		}
		return false;
	}

	public boolean expandRooms(final Room R, final List<Runnable> postWork)
	{
		int numberOfPeers = -1;//was: get Connected Property Rooms.size();
		final boolean doGrid=super.gridLayout();
		long roomLimit = 300; // default limit
		final Set<Room> updateExits=new HashSet<Room>();
		Prop_ReqCapacity capA = null;
		boolean didAnything = false;
		List<Room> allRooms = null;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if((d==Directions.UP)||(d==Directions.DOWN)||(d==Directions.GATE))
				continue;
			final Room chkR;
			synchronized(R)
			{
				synchronized(R.rawDoors())
				{
					chkR=R.getRawDoor(d);
				}
			}
			if((chkR==null)&&(numberOfPeers < 0))
			{
				if(allRooms == null) // this is now only mildly inefficient for thin areas
					allRooms = getConnectedPropertyRooms();
				if(allRooms.size()>0)
				{
					final Room capRoom = this.getAConnectedPropertyRoom();
					capA = (Prop_ReqCapacity)((capRoom!=null)?capRoom.fetchEffect("Prop_ReqCapacity"):null);
					if(capA != null)
					{
						roomLimit = capA.roomLimit;
					}
				}
				numberOfPeers = allRooms.size();
			}
			if((chkR==null)&&(numberOfPeers < roomLimit))
			{
				numberOfPeers++;
				if(doGrid)
				{
					final PairVector<Room,int[]> rooms=CMLib.tracking().buildGridList(R, null, 100);
					Room R3=R.getRoomInDir(d);
					if(R3 == null)
					{
						R3=CMLib.tracking().getCalculatedAdjacentRoom(rooms, R, d);
						if(R3!=null)
						{
							final int opd = Directions.getOpDirectionCode(d);
							R.setRawDoor(d,R3);
							R3.setRawDoor(opd,R);
							Exit E = R.getRawExit(d);
							if(E == null)
								E = R3.getRawExit(opd);
							if(E==null)
								E=CMClass.getExit("Open");
							if(R.getRawExit(d)==null)
								R.setRawExit(d, E);
							if(R3.getRawExit(opd)==null)
								R3.setRawExit(opd, E);
							updateExits.add(R);
							updateExits.add(R3);
							continue;
						}
					}
				}
				final LandTitle newTitle;
				final Room R2=CMClass.getLocale(CMClass.classID(R));
				R2.setRoomID(R.getArea().getNewRoomID(R,d));
				if(R2.roomID().length()==0)
					continue;
				R2.setArea(R.getArea());
				final LandTitle oldTitle=CMLib.law().getLandTitle(R);
				if((oldTitle!=null)&&(CMLib.law().getLandTitle(R2)==null))
				{
					newTitle = oldTitle.generateNextRoomTitle();
					R2.addNonUninvokableEffect((Ability)newTitle);
				}
				else
					newTitle=null;
				R.setRawDoor(d,R2);
				R.setRawExit(d,CMClass.getExit("Open"));
				R2.setRawDoor(Directions.getOpDirectionCode(d),R);
				R2.setRawExit(Directions.getOpDirectionCode(d),CMClass.getExit("Open"));
				updateExits.add(R);
				updateExits.add(R2);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
					Log.debugOut("Lots4Sale",R2.roomID()+" created and put up for sale.");
				if(capA != null)
					R2.addNonUninvokableEffect((Ability)capA.copyOf());
				final Room postR = R2;
				postWork.add(new Runnable()
				{
					final Room room = postR;
					final LandTitle title=newTitle;

					@Override
					public void run()
					{
						CMLib.database().DBCreateRoom(room);
						if(title!=null)
							CMLib.law().colorRoomForSale(room,title,true);
						room.getArea().fillInAreaRoom(room);
						final MOB mob=CMClass.getFactoryMOB("the wind",1,room);
						try
						{
							room.executeMsg(mob, CMClass.getMsg(mob, room, CMMsg.MSG_NEWROOM, null));
						}
						finally
						{
							mob.destroy();
						}
					}
				});
				didAnything=true;
			}
		}
		if(updateExits.size()>0)
		{
			didAnything=true;
			R.getArea().fillInAreaRoom(R);
			postWork.add(new Runnable()
			{
				final Set<Room> updateExits2=new SHashSet<Room>(updateExits);

				@Override
				public void run()
				{
					for(final Room xR : updateExits2)
					{
						CMLib.database().DBUpdateExits(xR);
					}
				}
			});
		}
		return didAnything;
	}

	@Override
	public void updateLot(final Set<String> optPlayerList)
	{
		final Environmental EV=affected;
		if((!(EV instanceof Room))
		||(!((Room)EV).isSavable())) // not thin!
			return;
		Room R=(Room)EV;
		boolean didAnything=false;
		try
		{
			final List<Runnable> postWork=new ArrayList<Runnable>();
			synchronized(CMClass.getSync("SYNC"+R.roomID()))
			{
				R=CMLib.map().getRoom(R);
				if((R!=null)
				&&(R.isSavable())) // not thin!
				{
					final int[] data=updateLotWithThisData(R,this,true,scheduleReset,optPlayerList,lastItemNums,daysWithNoChange);
					if(data != null)
					{
						lastItemNums=data[0];
						daysWithNoChange=data[1];
						if(canGenerateAdjacentRooms(R))
							didAnything = expandRooms(R,postWork) || didAnything;
						else
							didAnything = retractRooms(R,postWork) || didAnything;
					}
				}
			}
			for(final Runnable run : postWork)
				run.run();
			scheduleReset=false;
		}
		finally
		{
			if(didAnything)
			{
				this.lastArea=null;
				this.lastRoomsTimestamp=0;
				this.lastRoomsV.clear();
				getConnectedPropertyRooms(); // recalculates the unique id for this lot of rooms
			}
		}
	}
}
