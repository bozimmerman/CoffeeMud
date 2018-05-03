package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
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
   Copyright 2016-2018 Bo Zimmerman

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

public class Prop_RoomPlusForSale extends Prop_RoomForSale implements LandTitle
{
	@Override
	public String ID()
	{
		return "Prop_RoomPlusForSale";
	}

	@Override
	public String name()
	{
		return "Putting an expandable room up for sale";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	protected String	uniqueLotID	= null;

	@Override
	public String accountForYourself()
	{
		return "For Sale";
	}

	@Override
	public boolean allowsExpansionConstruction()
	{
		return true;
	}

	@Override
	public String getTitleID()
	{
		return super.getUniqueLotID();
	}
	
	@Override
	public LandTitle generateNextRoomTitle()
	{
		final LandTitle newTitle=(LandTitle)this.copyOf();
		newTitle.setBackTaxes(0);
		return newTitle;
	}

	@Override
	public String getUniqueLotID()
	{
		if(uniqueLotID==null)
			getConnectedPropertyRooms();
		return uniqueLotID;
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
}
