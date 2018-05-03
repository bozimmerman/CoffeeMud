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
   Copyright 2003-2018 Bo Zimmerman

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

	@Override
	public boolean allowsExpansionConstruction()
	{
		return true;
	}

	protected void fillCluster(Room R, List<Room> V)
	{
		V.add(R);
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room R2=R.getRoomInDir(d);
			if((R2!=null)&&(R2.roomID().length()>0)&&(!V.contains(R2)))
			{
				final Ability A=R2.fetchEffect(ID());
				if((R2.getArea()==R.getArea())&&(A!=null))
					fillCluster(R2,V);
				else
				{
					V.remove(R); // purpose here is to put the "front" door up front.
					V.add(0,R);
				}
			}
		}
	}

	@Override
	public List<Room> getConnectedPropertyRooms()
	{
		final List<Room> V=new ArrayList<Room>();
		Room R=null;
		if(affected instanceof Room)
			R=(Room)affected;
		else
			R=CMLib.map().getRoom(landPropertyID());
		if(R!=null)
		{
			fillCluster(R,V);
			String uniqueID="LOTS_PROPERTY_"+this;
			if(V.size()>0)
				uniqueID="LOTS_PROPERTY_"+CMLib.map().getExtendedRoomID(V.get(0));
			for(final Iterator<Room> r=V.iterator();r.hasNext();)
			{
				Ability A=null;
				R=r.next();
				if(R!=null)
					A=R.fetchEffect(ID());
				if(A instanceof Prop_LotsForSale)
					((Prop_LotsForSale)A).uniqueLotID=uniqueID;
			}
		}
		else
			uniqueLotID="";
		return V;

	}

	protected boolean isRetractableLink(Map<Room,Boolean> recurseChkRooms, Room fromRoom, Room theRoom)
	{
		if(theRoom==null)
			return true;

		if((theRoom.roomID().length()>0)
		&&((CMLib.law().getLandTitle(theRoom)==null)
			||(CMLib.law().getLandTitle(theRoom).getOwnerName().length()>0)))
		{
			if(recurseChkRooms != null)
				recurseChkRooms.put(theRoom, Boolean.valueOf(false));
			return false;
		}

		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room R=theRoom.rawDoors()[d];
			if(R!=null)
			{
				if((recurseChkRooms != null)
				&&(recurseChkRooms.containsKey(R)))
					return recurseChkRooms.get(theRoom).booleanValue();
				if((R!=fromRoom)
				&&(R.roomID().length()>0))
				{
					if((CMLib.law().getLandTitle(R)==null)||(CMLib.law().getLandTitle(R).getOwnerName().length()>0))
					{
						if(recurseChkRooms != null)
							recurseChkRooms.put(theRoom, Boolean.valueOf(false));
						return false;
					}
					if((recurseChkRooms != null)
					&&(!isRetractableLink(recurseChkRooms,theRoom,R)))
					{
						recurseChkRooms.put(theRoom, Boolean.valueOf(false));
						return false;
					}
				}
			}
		}
		if(recurseChkRooms != null)
			recurseChkRooms.put(theRoom, Boolean.valueOf(true));
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
		return newTitle;
	}
	
	protected boolean canGenerateAdjacentRooms(Room R)
	{
		return getOwnerName().length()>0;
	}

	protected boolean retractRooms(final Room R, List<Runnable> postWork)
	{
		boolean updateExits=false;
		boolean foundOne=false;
		boolean didAnything = false;
		final Map<Room,Boolean> checkedRetractRooms;
		if(super.gridLayout())
			checkedRetractRooms = new Hashtable<Room,Boolean>();
		else
			checkedRetractRooms = null;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if(d==Directions.GATE)
				continue;
			final Room R2=R.rawDoors()[d];
			if((R2!=null)&&((!R2.isSavable())||(R2.roomID().length()==0)))
				continue;
			Exit E=R.getRawExit(d);
			if(checkedRetractRooms != null)
				checkedRetractRooms.clear();
			if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(d)]==R))
				foundOne=true;
			else
			if((R2!=null)
			&&(isRetractableLink(checkedRetractRooms,R,R2)))
			{
				R.rawDoors()[d]=null;
				R.setRawExit(d,null);
				updateExits=true;
				postWork.add(new Runnable()
				{
					final Room room=R2;

					@Override
					public void run()
					{
						CMLib.map().obliterateRoom(room);
					}
				});
				didAnything=true;
			}
			else
			if((E!=null)&&(E.hasALock())&&(E.isGeneric()))
			{
				E.setKeyName("");
				E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
				updateExits=true;
				if(R2!=null)
				{
					E=R2.getRawExit(Directions.getOpDirectionCode(d));
					if((E!=null)&&(E.hasALock())&&(E.isGeneric()))
					{
						E.setKeyName("");
						E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
						postWork.add(new Runnable(){
							final Room room=R2;

							@Override
							public void run()
							{
								CMLib.database().DBUpdateExits(room);
								R2.getArea().fillInAreaRoom(room);
							}
						});
						didAnything=true;
					}
				}
			}
		}
		if(checkedRetractRooms != null)
			checkedRetractRooms.clear();
		if(!foundOne)
		{
			CMLib.map().obliterateRoom(R);
			didAnything=true;
		}
		else
		if(updateExits)
		{
			CMLib.database().DBUpdateExits(R);
			R.getArea().fillInAreaRoom(R);
			didAnything=true;
		}
		return didAnything;
	}
	
	public boolean expandRooms(final Room R, final List<Runnable> postWork)
	{
		int numberOfPeers = -1;//getConnectedPropertyRooms().size();
		final boolean doGrid=super.gridLayout();
		long roomLimit = Long.MAX_VALUE;
		final Set<Room> updateExits=new HashSet<Room>();
		Prop_ReqCapacity cap = null;
		boolean didAnything = false;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if((d==Directions.UP)||(d==Directions.DOWN)||(d==Directions.GATE))
				continue;
			final Room chkR=R.getRoomInDir(d);
			if((chkR==null)&&(numberOfPeers < 0))
			{
				final List<Room> allRooms = getConnectedPropertyRooms();
				if(allRooms.size()>0)
				{
					cap = (Prop_ReqCapacity)allRooms.get(0).fetchEffect("Prop_ReqCapacity");
					if(cap != null)
					{
						roomLimit = cap.roomLimit;
					}
				}
				numberOfPeers = allRooms.size();
			}
			if((chkR==null)&&(numberOfPeers < roomLimit))
			{
				numberOfPeers++;
				final Room R2=CMClass.getLocale(CMClass.classID(R));
				R2.setRoomID(R.getArea().getNewRoomID(R,d));
				if(R2.roomID().length()==0)
					continue;
				R2.setArea(R.getArea());
				LandTitle oldTitle=CMLib.law().getLandTitle(R);
				final LandTitle newTitle;
				if((oldTitle!=null)&&(CMLib.law().getLandTitle(R2)==null))
				{
					newTitle = oldTitle.generateNextRoomTitle();
					R2.addNonUninvokableEffect((Ability)newTitle);
				}
				else
					newTitle=null;
				R.rawDoors()[d]=R2;
				R.setRawExit(d,CMClass.getExit("Open"));
				R2.rawDoors()[Directions.getOpDirectionCode(d)]=R;
				R2.setRawExit(Directions.getOpDirectionCode(d),CMClass.getExit("Open"));
				updateExits.add(R);
				if(doGrid)
				{
					final PairVector<Room,int[]> rooms=CMLib.tracking().buildGridList(R2, this.getOwnerName(), 100);
					for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++)
					{
						if(dir==Directions.GATE)
							continue;
						Room R3=R2.getRoomInDir(dir);
						if(R3 == null)
						{
							R3=CMLib.tracking().getCalculatedAdjacentRoom(rooms, R3, dir);
							if(R3!=null)
							{
								R2.rawDoors()[dir]=R3;
								R3.rawDoors()[Directions.getOpDirectionCode(dir)]=R2;
								updateExits.add(R3);
							}
						}
					}
				}
				updateExits.add(R2);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
					Log.debugOut("Lots4Sale",R2.roomID()+" created and put up for sale.");
				if(cap != null)
					R2.addNonUninvokableEffect((Ability)cap.copyOf());
				postWork.add(new Runnable()
				{
					final Room room = R2;
					final LandTitle title=newTitle;

					@Override
					public void run()
					{
						CMLib.database().DBCreateRoom(room);
						if(title!=null)
							CMLib.law().colorRoomForSale(room,title,true);
						room.getArea().fillInAreaRoom(room);
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
					for(Room xR : updateExits2)
					{
						CMLib.database().DBUpdateExits(xR);
					}
				}
			});
		}
		return didAnything;
	}
	
	@Override
	public void updateLot(List<String> optPlayerList)
	{
		final Environmental EV=affected;
		if(!(EV instanceof Room))
			return;
		Room R=(Room)EV;
		boolean didAnything=false;
		try
		{
			List<Runnable> postWork=new ArrayList<Runnable>();
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				lastItemNums=updateLotWithThisData(R,this,true,scheduleReset,optPlayerList,lastItemNums);

				if(getOwnerName().length()==0)
				{
					didAnything = retractRooms(R,postWork) || didAnything;
				}
				else
				if(canGenerateAdjacentRooms(R))
				{
					didAnything = expandRooms(R,postWork) || didAnything;
				}
			}
			for(Runnable run : postWork)
				run.run();
			scheduleReset=false;
		}
		finally
		{
			if(didAnything)
				getConnectedPropertyRooms(); // recalculates the unique id for this lot of rooms
		}
	}
}
