package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;

import java.util.List;

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
/**
 * A Sailing Ship, which is an object that's boardable, rooms are attached
 * to it so you can get in, and its an item that can appear in rooms, have
 * speed and direction, be in combat, etc.
 * @author Bo Zimmerman
 *
 */
public interface SailingShip extends BoardableShip, Item, Combatant, Rideable
{
	public static final int COURSE_STEER_MASK = 256;
	
	/**
	 * Returns which direction the ship is currently facing.
	 * @return the direction the ship is facing. 
	 */
	public int getDirectionFacing();
	
	/**
	 * Returns whether the anchor is down, thus holding the ship in place.
	 * @return true if the anchor is down, holding the ship in place.
	 */
	public boolean isAnchorDown();
	
	/**
	 * Returns this ships max speed, typically &gt;= 1
	 * @return this ships max speed, typically &gt;= 1
	 */
	public int getShipSpeed();

	/**
	 * Sets whether the anchor is down, thus holding the ship in place.
	 * @param truefalse true if the anchor is down, false if the anchor is up
	 */
	public void setAnchorDown(boolean truefalse);
	
	/**
	 * Returns the mapping of this ships siege weapons to the coordinates
	 * they are presently aimed at.  Each coordinate is int[x,y]
	 * @return the pairings of weapons to coordinates aimed at
	 */
	public PairList<Weapon,int[]> getSiegeWeaponAimings();
	
	/**
	 * Returns the future course of this ship.  A stop-course direction is
	 * always -1, so it is typically the last entry.  Otherwise, each entry
	 * is a compass direction, possibly masked by COURSE_STEER_MASK in order
	 * to specify that it is a TURN ONLY.  Directions not marked as turns
	 * are automatic movements in that direction.
	 * @see SailingShip#setCurrentCourse(List)
	 * @return the future course of this ship.
	 */
	public List<Integer> getCurrentCourse();

	/**
	 * Sets the future course of this ship.  A stop-course direction is
	 * always -1, so it is typically the last entry.  Otherwise, each entry
	 * is a compass direction, possibly masked by COURSE_STEER_MASK in order
	 * to specify that it is a TURN ONLY.  Directions not marked as turns
	 * are automatic movements in that direction.
	 * @see SailingShip#getCurrentCourse()
	 * @param course the new course to set.
	 */
	public void setCurrentCourse(List<Integer> course);
}
