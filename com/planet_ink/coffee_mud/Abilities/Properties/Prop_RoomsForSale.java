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

public class Prop_RoomsForSale extends Prop_RoomForSale
{
	@Override
	public String ID()
	{
		return "Prop_RoomsForSale";
	}

	@Override
	public String name()
	{
		return "Putting a cluster of rooms up for sale";
	}

	protected String	uniqueLotID	= null;

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
	public List<Room> getAllTitledRooms()
	{
		final List<Room> V=new ArrayList<Room>();
		Room R=null;
		if(affected instanceof Room)
			R=(Room)affected;
		else
			R=CMLib.map().getRoom(landPropertyID());
		if(R!=null)
			fillCluster(R,V);
		return V;
	}

	@Override
	public String getTitleID()
	{
		if(affected instanceof Room)
			return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID((Room)affected);
		else
		{
			final Room R=CMLib.map().getRoom(landPropertyID());
			if(R!=null)
				return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID(R);
		}
		return "";
	}

	// update title, since it may affect room clusters, worries about EVERYONE
	@Override
	public void updateTitle()
	{
		final List<Room> V=getAllTitledRooms();
		final String owner=getOwnerName();
		final int price=getPrice();
		final boolean rental=rentalProperty();
		final boolean gridLayout = gridLayout();
		final int back=backTaxes();
		String uniqueID="ROOMS_PROPERTY_"+this;
		if(V.size()>0)
			uniqueID="ROOMS_PROPERTY_"+CMLib.map().getExtendedRoomID(V.get(0));
		for(int v=0;v<V.size();v++)
		{
			Room R=V.get(v);
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				final LandTitle A=(LandTitle)R.fetchEffect(ID());
				if((A!=null)
				&&((!A.getOwnerName().equals(owner))
				   ||(A.getPrice()!=price)
				   ||(A.backTaxes()!=back)
				   ||(A.rentalProperty()!=rental)))
				{
					A.setOwnerName(owner);
					A.setPrice(price);
					A.setBackTaxes(back);
					A.setRentalProperty(rental);
					A.setGridLayout(gridLayout);
					CMLib.database().DBUpdateRoom(R);
				}
				if(A instanceof Prop_RoomsForSale)
					((Prop_RoomsForSale)A).uniqueLotID=uniqueID;
			}
		}
	}

	@Override
	public String getUniqueLotID()
	{
		if(uniqueLotID==null)
			updateTitle();
		return uniqueLotID;
	}

	// update lot, since its called by the savethread, ONLY worries about itself
	@Override
	public void updateLot(List<String> optPlayerList)
	{
		if(affected instanceof Room)
		{
			lastItemNums=updateLotWithThisData((Room)affected,this,false,scheduleReset,optPlayerList,lastItemNums);
			if((lastDayDone!=((Room)affected).getArea().getTimeObj().getDayOfMonth())
			&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			{
				final Room R=(Room)affected;
				lastDayDone=R.getArea().getTimeObj().getDayOfMonth();
				final List<Room> V=getAllTitledRooms();
				for(int v=0;v<V.size();v++)
				{
					final Room R2=V.get(v);
					final Prop_RoomForSale PRFS=(Prop_RoomForSale)R2.fetchEffect(ID());
					if(PRFS!=null)
						PRFS.lastDayDone=R.getArea().getTimeObj().getDayOfMonth();
				}
				if((getOwnerName().length()>0)&&rentalProperty()&&(R.roomID().length()>0))
				{
					if(doRentalProperty(R.getArea(),R.roomID(),getOwnerName(),getPrice()))
					{
						setOwnerName("");
						updateTitle();
						lastItemNums=updateLotWithThisData((Room)affected,this,false,scheduleReset,optPlayerList,lastItemNums);
					}
				}
			}
			scheduleReset=false;
		}
	}
}
