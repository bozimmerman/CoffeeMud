package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.CompleteRoomEnumerator;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2025 Bo Zimmerman

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
public class StdThinGridArea extends StdGridArea
{
	@Override
	public String ID()
	{
		return "StdThinGridArea";
	}

	@Override
	public long flags()
	{
		return Area.FLAG_THIN;
	}

	public RoomnumberSet myRoomSet=null;

	@Override
	public void addProperRoom(final Room R)
	{
		if(R!=null)
			R.setExpirationDate(System.currentTimeMillis() + WorldMap.ROOM_EXPIRATION_MILLIS);
		super.addProperRoom(R);
	}

	@Override
	public Room getRoom(String roomID)
	{
		if(!isRoom(roomID))
			return null;
		Room R=super.getRoom(roomID);
		if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
		{
			if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
				roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
			R=CMLib.database().DBReadRoomObject(roomID,true, false);
			if(R!=null)
			{
				R.setArea(this);
				addProperRoom(R);
				CMLib.database().DBReadRoomExits(roomID,R,false);
				CMLib.database().DBReadContent(roomID,R,true);
				fillInAreaRoom(R);
				R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
			}
		}
		return R;
	}

	@Override
	public boolean isRoomCached(final String roomID)
	{
		if(!isRoom(roomID))
			return false;
		final Room R=super.getRoom(roomID); // *NOT* this.getRoom
		return (((R!=null)&&(!R.amDestroyed()))&&(roomID!=null));
	}

	public boolean isRoom(final String roomID)
	{
		return getProperRoomnumbers().contains(roomID);
	}

	@Override
	public boolean isRoom(final Room R)
	{
		if(R==null)
			return false;
		if(super.isRoom(R))
			return true;
		if(R.roomID().length()==0)
			return false;
		return isRoom(R.roomID());
	}

	@Override
	public Enumeration<Room> getProperMap()
	{
		return new IteratorEnumeration<Room>(properRooms.values().iterator());
	}

	@Override
	public Enumeration<Room> getMetroMap()
	{
		final int minimum=getProperRoomnumbers().roomCountAllAreas()/10;
		if(getCachedRoomnumbers().roomCountAllAreas()<minimum)
		{
			for(int r=0;r<minimum;r++)
				getRandomProperRoom();
		}
		final MultiEnumeration<Room> multiEnumerator = new MultiEnumeration<Room>(new RoomIDEnumerator(this));
		for(final Iterator<Area> a=getChildrenReverseIterator();a.hasNext();)
			multiEnumerator.addEnumeration(a.next().getMetroMap());
		return new CompleteRoomEnumerator(multiEnumerator);
	}

	@Override
	public Enumeration<Room> getCompleteMap()
	{
		return new CompleteRoomEnumerator(new RoomIDEnumerator(this));
	}
}
