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
   Copyright 2001-2018 Bo Zimmerman

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
 * Interface for a Room that virtually contains other rooms, which are 
 * called grid-child rooms. This is for producing substantially identical 
 * rooms in grid formations, or mazes, or other groupings where unique 
 * control of exits and content is not desired.
 *
 * Grids have the following general features: They are rectangular with a 
 * defineable width and length.  All rooms in the grid are of the same
 * locale type.  Grid children can only have exits to each other, or to
 * the GridLocale's exiting rooms, unless "outer exits" are defined.
 * @author Bo Zimmerman
 */
public interface GridLocale extends Room, GridZones 
{
	/**
	 * Gets the Room ID() for the type of Java room class used to
	 * populate the grid.  For example: StdRoom.
	 * @return the Room ID() for the type of Java room class
	 */
	public String getGridChildLocaleID();

	/**
	 * Returns the room found in the given direction from the from-room, 
	 * where the stand-in room is the to-room.
	 * 
	 * Grids are not required to actually fill themselves in at boot
	 * time, nor are they required to remain filled in.  Therefore,
	 * rooms can be constructed in real time.
	 * 
	 * This method is called whenever a player wants to move into another
	 * room from a room that is part of a grid to some other room.
	 * @param fromRoom the room moving from, also the grid child
	 * @param toRoom the room ostensibly being moved into
	 * @param direction the direction from the from room moving
	 * @return the new to-room, the actual room to move into
	 */
	public Room prepareGridLocale(Room fromRoom, Room toRoom, int direction);

	/**
	 * Called whenever structural changes are made to either the grid
	 * room, or one of the adjacent connecting rooms, this method will
	 * "rebuild" the grids internal structure as necessary.
	 */
	public void buildGrid();

	/**
	 * Empties the grid-children rooms of all contents, mob and item,
	 * and then destroys the internal grid-children rooms themselves.
	 * This puts the grid back into a default state.
	 * @param bringBackHere a room to teleport *ALL* mobs and items to
	 */
	public void clearGrid(Room bringBackHere);

	/**
	 * Returns a read-only list of grid-child rooms.  You are guaranteed
	 * at least one room, even from grids that create them as-needed.
	 * @return a read-only list of grid-child rooms.
	 */
	public List<Room> getAllRooms();

	/**
	 * Returns a read-only list of grid-child rooms, and any skys or seas
	 * attached to them.
	 * @return a read-only list of all rooms around here.
	 */
	public List<Room> getAllRoomsFilled();
	
	/**
	 * Returns a read-only iterator over the existing grid-child rooms.
	 * This iterator may be empty if the grid creates children as-needed.
	 * @return a read-only iterator over the existing grid-child rooms.
	 */
	public Iterator<Room> getExistingRooms();

	/**
	 * Normally the grid-child rooms can only exit to each other, or to
	 * the same places as the gridlocale host.  Outer Exits are a way around
	 * this by pre-defining exits from grid children to elsewhere on the 
	 * map, including into other gridlocales.
	 * @see GridLocale.CrossExit
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.GridLocale#addOuterExit(GridLocale.CrossExit)
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.GridLocale#delOuterExit(GridLocale.CrossExit)
	 * @return an iterator of cross ("outer") exits.
	 */
	public Iterator<CrossExit> outerExits();

	/**
	 * Normally the grid-child rooms can only exit to each other, or to
	 * the same places as the gridlocale host.  Outer Exits are a way around
	 * this by pre-defining exits from grid children to elsewhere on the 
	 * map, including into other gridlocales.
	 * This method will add a new one.
	 * @see GridLocale.CrossExit
	 * @see GridLocale#outerExits()
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.GridLocale#delOuterExit(GridLocale.CrossExit)
	 * @param x the new cross ("outer") exit
	 */
	public void addOuterExit(CrossExit x);

	/**
	 * Normally the grid-child rooms can only exit to each other, or to
	 * the same places as the gridlocale host.  Outer Exits are a way around
	 * this by pre-defining exits from grid children to elsewhere on the 
	 * map, including into other gridlocales.
	 * This method will remove an existing one
	 * @see GridLocale.CrossExit
	 * @see GridLocale#outerExits()
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.GridLocale#addOuterExit(GridLocale.CrossExit)
	 * @param x the existing cross ("outer") exit to delete
	 */
	public void delOuterExit(CrossExit x);
	
	/**
	 * class definition for an exit that goes from inside a grid locale child to a place 
	 * outside the parent gridlocale room
	 * @author Bo Zimmerman
	 */
	public static class CrossExit
	{
		public int x;
		public int y;
		public int dir;
		public String destRoomID="";
		public boolean out=false;
	
		public static CrossExit make(int xx, int xy, int xdir, String xdestRoomID, boolean xout)
		{
			final CrossExit EX = new CrossExit();
			EX.x = xx;
			EX.y = xy;
			EX.dir = xdir;
			EX.destRoomID = xdestRoomID;
			EX.out = xout;
			return EX;
		}
	}
}
