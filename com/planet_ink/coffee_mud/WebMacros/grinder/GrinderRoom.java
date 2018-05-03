package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class GrinderRoom
{
	public int		z			= 0;
	public int[]	xy			= null;
	public String	roomID		= "";
	private Room	roomCache	= null;

	public Room room()
	{
		if((roomID.length()>0)
		&&((roomCache==null)||(roomCache.amDestroyed())))
		{
			roomCache=CMLib.map().getRoom(roomID);
			if(roomCache!=null)
				fixExits(roomCache);
		}
		return roomCache;
	}

	public boolean isRoomGood()
	{
		return ((roomCache != null) && (!roomCache.amDestroyed()));
	}

	public GrinderDir[]	doors	= new GrinderDir[Directions.NUM_DIRECTIONS()];

	public GrinderRoom(String newRoomID)
	{
		roomCache=null;
		roomID=newRoomID;
	}

	public void fixExits(Room R)
	{
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final GrinderDir D=new GrinderDir();
			R.clearSky();
			final Room R2=R.rawDoors()[d];
			if(R2!=null)
				D.room=R2.roomID();
			final Exit E2=R.getRawExit(d);
			if(E2!=null)
				D.exit=E2;
			doors[d]=D;
		}
	}

	public GrinderRoom(Room R)
	{
		roomCache=null;
		if(!R.amDestroyed())
		{
			roomCache=R;
			fixExits(R);
		}
		roomID=R.roomID();
	}
}
