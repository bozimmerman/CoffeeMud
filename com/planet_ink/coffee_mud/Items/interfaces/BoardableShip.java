package com.planet_ink.coffee_mud.Items.interfaces;
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

/*
   Copyright 2014-2018 Bo Zimmerman

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
 * A Boardable Ship, which is an object that's boardable, rooms are attached
 * to it so you can get in.
 * @author Bo Zimmerman
 *
 */
public interface BoardableShip extends Environmental
{
	/**
	 * Designates that this ship is landed and docked in the given 
	 * planetary room.
	 * @param R the coordinate toom in which the ship is docked.
	 */
	public void dockHere(Room R);

	/**
	 * When a boardable ship area is created, it is bound to an item
	 * that resides in a room when docked, or possibly, always. 
	 * @param dockableItem the item that acts as dockable item 
	 */
	public void setDockableItem(Item dockableItem);
	
	/**
	 * Designates that this ship is no longer docked, and whether it
	 * should also be moved into its na
	 * @param moveToOutside true to put in space/ocean,  or false to leave in limbo
	 * @return one of the internal rooms that was connected to the dock
	 */
	public Room unDock(boolean moveToOutside);

	/**
	 * Returns the Room where this ship is docked, or NULL if in space.
	 * @return the Room where this ship is docked, or NULL if in space.
	 */
	public Room getIsDocked();

	/**
	 * Space ships are unique in having an Item stand-in for dirt-side access,
	 * as well as an Area object.  This method returns the area object that 
	 * represents the contents of the ship.
	 * @return the official area version of this ship
	 */
	public Area getShipArea();

	/**
	 * Space ships are unique in having an Item stand-in for dirt-side access,
	 * as well as an Area object.  This method sets the area object that 
	 * represents the contents of the ship.
	 * @param xml area xml for the ship
	 */
	public void setShipArea(String xml);

	/**
	 * Renames the ship to something else
	 * @param newName the new ship name
	 */
	public void renameShip(String newName);

	/**
	 * Returns the room ID of the ships home port.
	 * @return the ships home port
	 */
	public String getHomePortID();

	/**
	 * Sets the room ID of the ships home port
	 * @param portID the ships home port
	 */
	public void setHomePortID(String portID);

	/**
	 * Ships are unique in having an Item stand-in for port-side access,
	 * as well as an Area object.  This method returns that Item.
	 * @return the official space version of this ship
	 */
	public Item getShipItem();
}
