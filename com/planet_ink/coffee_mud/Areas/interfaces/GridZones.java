package com.planet_ink.coffee_mud.Areas.interfaces;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.interfaces.*;
/*
   Copyright 2006-2018 Bo Zimmerman

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
 * GridZones is a cross-object interface that applies both to Areas, and Locales.
 * It represents an area (or room) organized like a Grid.
 * @author Bo Zimmerman
 */
public interface GridZones extends Environmental
{
	/**
	 * Returns whether the given Room is a child of this
	 * particular Grid instance.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param loc a Room object
	 * @return whether the room is a child
	 */
	public boolean isMyGridChild(Room loc);
	/**
	 * Returns the fully-qualified Room ID of this room
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.GridZones#getGridChild(String)
	 * @param loc a Room object
	 * @return a fully qualified room ID
	 */
	public String getGridChildCode(Room loc);
	/**
	 * Returns the fully-qualified Room ID of this room
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.GridZones#getGridChildCode(Room)
	 * @param childCode a fully-qualified Room ID for this Room
	 * @return a Room object
	 */
	public Room getGridChild(String childCode);
	/**
	 * Returns a random Room object that is a child of this one.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return a Room object
	 */
	public Room getRandomGridChild();
	/**
	 * Returns the X coordinate of the given Room object
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param loc the Room object
	 * @return the x coordinate of the room
	 */
	public int getGridChildX(Room loc);
	/**
	 * Returns the Y coordinate of the given Room object
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param loc the Room object
	 * @return the y coordinate of the room
	 */
	public int getGridChildY(Room loc);

	/**
	 * Returns the XY coordinates of the Room with the given roomID
	 * in XYVector format.
	 * @see XYVector
	 * @param roomID the roomID of the room to get coordinates for
	 * @return coordinates in XYVector format.
	 */
	public XYVector getRoomXY(String roomID);

	/**
	 * Returns the XY coordinates of the Room, if a child of
	 * this gridzone, in XYVector format.
	 * @see XYVector
	 * @param room the room to get coordinates for
	 * @return coordinates in XYVector format.
	 */
	public XYVector getRoomXY(Room room);

	/**
	 * Returns the total width of this grid.
	 * @return the width
	 */
	public int xGridSize();
	/**
	 * Returns the total height of this grid
	 * @return the height
	 */
	public int yGridSize();
	/**
	 * Sets the total width of this grid.
	 * @param x the width
	 */
	public void setXGridSize(int x);
	/**
	 * Sets the total height of this grid.
	 * @param y the height
	 */
	public void setYGridSize(int y);
	/**
	 * Returns the Room object at the given coordinates.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the Room object at those coordinates
	 */
	public Room getGridChild(int x, int y);

	/**
	 * Returns the Room object at the given coordinates.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @see XYVector
	 * @param xy the x and y coordinate
	 * @return the Room object at those coordinates
	 */
	public Room getGridChild(XYVector xy);

	/**
	 * A class for holding x/y coordinates. Used by GridZones
	 * as a way to hold such coordinates in a single place,
	 * and easily compare them to each other.
	 * @author Bo Zimmermanimmerman
	 */
	public static class XYVector
	{
		public int x;
		public int y;
		public XYVector(int x, int y)
		{
			this.x=x;
			this.y=y;
		}
	}
}
