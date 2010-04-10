package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class StdThinArea extends StdArea
{
	public String ID(){	return "StdThinArea";}
	public long flags(){return Area.FLAG_THIN;}

    public void addProperRoom(Room R)
    {
    	if(R!=null) R.setExpirationDate(WorldMap.ROOM_EXPIRATION_MILLIS);
    	super.addProperRoom(R);
    }
	public Room getProperRoom(String roomID)
	{
    	if(!isRoom(roomID)) return null;
    	Room R=super.getRoom(roomID);
    	if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
    		return null;
    	return R;
	}
	
    public Room getRoom(String roomID)
    {
    	if(!isRoom(roomID)) return null;
    	Room R=super.getRoom(roomID);
    	if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
    	{
    		if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
	    		roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
			R=CMLib.database().DBReadRoomObject(roomID,false);
			if(R!=null)
			{
				R.setArea(this);
				addProperRoom(R);
				Vector V=CMParms.makeVector(R);
				CMLib.database().DBReadRoomExits(roomID,V,false);
				CMLib.database().DBReadContent(R,V);
				fillInAreaRoom(R);
				R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
			}
    	}
    	return R;
    }
	public Enumeration getProperMap(){return DVector.s_enum(properRooms);}
    public boolean isRoom(String roomID){ return getProperRoomnumbers().contains(roomID); }
    public boolean isRoom(Room R)
    {
    	if(R==null) return false;
    	if(R.roomID().length()==0) return super.isRoom(R);
    	return isRoom(R.roomID());
    }
	public Enumeration getCompleteMap(){return new CompleteRoomEnumerator(this);}
	public Vector getMetroCollection()
	{
		int minimum=(properRoomIDSet==null)?0:(properRoomIDSet.roomCountAllAreas()/10);
		if(getCachedRoomnumbers().roomCountAllAreas()<minimum)
		{
			for(int r=0;r<minimum;r++)
				getRandomProperRoom();
		}
		return super.getMetroCollection();
	}
}
