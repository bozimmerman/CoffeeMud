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


import java.util.List;

/*
   Copyright 2021-2024 Bo Zimmerman

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
public interface SiegableItem extends Item, Combatant
{

	public static enum SiegeCommand
	{
		TARGET,
		AIM,
		IMPLODE
		;
	}

	/**
	 * Returns the mapping of this objects siege weapons to the coordinates
	 * they are presently aimed at.  Each coordinate is int[x,y]
	 * @return the pairings of weapons to coordinates aimed at
	 */
	public PairList<Weapon,int[]> getSiegeWeaponAimings();

	/**
	 * A unique display message for siegable objects and their relationship
	 * to this object.  So, this method returns the relationship between this
	 * object and the given viewer object.
	 * @param viewer an object viewing this one
	 * @return a quick display message
	 */
	public String getTacticalView(final SiegableItem viewer);

	/**
	 * Returns the tactical coordinates of this object
	 * @return the tactical coordinates of this object
	 */
	public int[] getTacticalCoords();

	/**
	 * Returns the combat field, which must be public to allow
	 * all participants too coordinate.  If this siegable is not
	 * yet resolved, this may return null.
	 * @return the field of combat or null if not yet set
	 */
	public PairList<Item, int[]> getCombatField();

	/**
	 * Returns whether this is sunk or destroyed in a siege..
	 * @return true if this is sunk or destroyed in a siege
	 */
	@Override
	public boolean amDead();

	/**
	 * Returns the number of base hull points that the given obj has.
	 * @return the base hull points of the obj
	 */
	public int getMaxHullPoints();
}
