package com.planet_ink.coffee_mud.Locales.interfaces;
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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/*
   Copyright 2001-2015 Bo Zimmerman

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
/**
 * Interface for a Room that virtually contains other rooms.  This is
 * for producing substantially identical rooms in grid formations,
 * or mazes, or other groupings where unique control of exits and
 * content is not desired.
 * @author Bo Zimmerman
 */
public interface GridLocale extends Room, GridZones
{
	public String getGridChildLocaleID();
	public Room prepareGridLocale(Room fromRoom, Room toRoom, int direction);
	public void buildGrid();
	public void clearGrid(Room bringBackHere);
	public List<Room> getAllRooms();
	public Iterator<Room> getExistingRooms();
	public Iterator<WorldMap.CrossExit> outerExits();
	public void addOuterExit(WorldMap.CrossExit x);
	public void delOuterExit(WorldMap.CrossExit x);
	public static class ThinGridEntry
	{
		public Room room;
		public XYVector xy;
		public ThinGridEntry(Room R, int x, int y)
		{ 
			room=R; 
			xy=new XYVector(x,y);
		}
	}
	public static class ThinGridEntryConverter implements Converter<ThinGridEntry,Room>
	{
		public static ThinGridEntryConverter INSTANCE = new ThinGridEntryConverter();
		@Override public Room convert(ThinGridEntry obj) { return obj.room;}
	}
}
