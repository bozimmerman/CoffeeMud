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
@SuppressWarnings("rawtypes")
public class Prop_LotsForSale extends Prop_RoomForSale
{
	@Override public String ID() { return "Prop_LotsForSale"; }
	@Override public String name(){ return "Putting many rooms up for sale";}
	protected String uniqueLotID=null;

	@Override public boolean allowsExpansionConstruction(){ return true; }

	protected void fillCluster(Room R, List<Room> V)
	{
		V.add(R);
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Room R2=R.getRoomInDir(d);
			if((R2!=null)&&(R2.roomID().length()>0)&&(!V.contains(R2)))
			{
				Ability A=R2.fetchEffect(ID());
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
		List<Room> V=new ArrayList<Room>();
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
			for(Iterator<Room> r=V.iterator();r.hasNext();)
			{
				Ability A=null;
				R=r.next();
				if(R!=null) A=R.fetchEffect(ID());
				if(A!=null) ((Prop_LotsForSale)A).uniqueLotID=uniqueID;
			}
		}
		else
			uniqueLotID="";
		return V;

	}

	protected static boolean isCleanRoom(Room fromRoom, Room theRoom)
	{
		if(theRoom==null) return true;

		if((theRoom.roomID().length()>0)
		&&((CMLib.law().getLandTitle(theRoom)==null)
			||(CMLib.law().getLandTitle(theRoom).getOwnerName().length()>0)))
			return false;

		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Room R=theRoom.rawDoors()[d];
			if((R!=null)
			   &&(R!=fromRoom)
			   &&(R.roomID().length()>0)
			   &&((CMLib.law().getLandTitle(R)==null)||(CMLib.law().getLandTitle(R).getOwnerName().length()>0)))
				return false;
		}
		return true;
	}

	@Override
	public String getTitleID()
	{
		if(affected instanceof Room)
			return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID((Room)affected);
		else
		{
			Room R=CMLib.map().getRoom(landPropertyID());
			if(R!=null)
				return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID(R);
		}
		return "";
	}

	@Override
	public String getUniqueLotID()
	{
		if(uniqueLotID==null) getConnectedPropertyRooms();
		return uniqueLotID;
	}

	@Override
	public void updateLot(List optPlayerList)
	{
		Environmental EV=affected;
		if(!(EV instanceof Room)) return;
		Room R=(Room)EV;
		boolean didAnything=false;
		try
		{
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				lastItemNums=updateLotWithThisData(R,this,true,scheduleReset,optPlayerList,lastItemNums);

				if(getOwnerName().length()==0)
				{
					boolean updateExits=false;
					boolean foundOne=false;
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						if(d==Directions.GATE)
							continue;
						Room R2=R.rawDoors()[d];
						foundOne=foundOne||(R2!=null);
						Exit E=R.getRawExit(d);
						if((R2!=null)&&(isCleanRoom(R,R2)))
						{
							R.rawDoors()[d]=null;
							R.setRawExit(d,null);
							updateExits=true;
							CMLib.map().obliterateRoom(R2);
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
									CMLib.database().DBUpdateExits(R2);
									R2.getArea().fillInAreaRoom(R2);
									didAnything=true;
								}
							}
						}
					}
					if(!foundOne)
					{
						CMLib.map().obliterateRoom(R);
						didAnything=true;
						return;
					}
					if(updateExits)
					{
						CMLib.database().DBUpdateExits(R);
						R.getArea().fillInAreaRoom(R);
						didAnything=true;
					}
				}
				else
				{
					boolean updateExits=false;
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						if((d==Directions.UP)||(d==Directions.DOWN)||(d==Directions.GATE))
							continue;
						Room R2=R.getRoomInDir(d);
						if(R2==null)
						{
							R2=CMClass.getLocale(CMClass.classID(R));
							R2.setRoomID(R.getArea().getNewRoomID(R,d));
							if(R2.roomID().length()==0) continue;
							R2.setArea(R.getArea());
							LandTitle newTitle=CMLib.law().getLandTitle(R);
							if((newTitle!=null)&&(CMLib.law().getLandTitle(R2)==null))
							{
								newTitle=(LandTitle)((Ability)newTitle).copyOf();
								newTitle.setOwnerName("");
								newTitle.setBackTaxes(0);
								R2.addNonUninvokableEffect((Ability)newTitle);
							}
							R.rawDoors()[d]=R2;
							R.setRawExit(d,CMClass.getExit("Open"));
							R2.rawDoors()[Directions.getOpDirectionCode(d)]=R;
							R2.setRawExit(Directions.getOpDirectionCode(d),CMClass.getExit("Open"));
							updateExits=true;
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
								Log.debugOut("Lots4Sale",R2.roomID()+" created and put up for sale.");
							CMLib.database().DBCreateRoom(R2);
							if(newTitle!=null)
								colorForSale(R2,newTitle.rentalProperty(),true);
							R2.getArea().fillInAreaRoom(R2);
							CMLib.database().DBUpdateExits(R2);
							didAnything=true;
						}
					}
					if(updateExits)
					{
						CMLib.database().DBUpdateExits(R);
						R.getArea().fillInAreaRoom(R);
						didAnything=true;
					}
				}
			}
			scheduleReset=false;
		}
		finally
		{
			if(didAnything)
				getConnectedPropertyRooms(); // recalculates the unique id for this lot of rooms
		}
	}
}
