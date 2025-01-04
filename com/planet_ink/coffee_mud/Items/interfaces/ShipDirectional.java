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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2020-2025 Bo Zimmerman

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
 * A ship dir item is any ship or space object that can be directed
 * in a particular ship direction, such as a beam, launcher, sensor,
 * or whatever.
 *
 * @author Bo Zimmerman
 *
 */
public interface ShipDirectional extends Item
{
	/**
	/**
	 * The ShipDir enum is for the different ports, denoting
	 * the port, by its direction location.
	 * @author Bo Zimmerman
	 */
	public enum ShipDir
	{
		AFT,
		PORT,
		VENTRAL,
		DORSEL,
		STARBOARD,
		FORWARD
		;
		public final ShipDir opposite()
		{
			switch(this)
			{
			case AFT:
				return FORWARD;
			case PORT:
				return STARBOARD;
			case VENTRAL:
				return DORSEL;
			case DORSEL:
				return VENTRAL;
			case STARBOARD:
				return PORT;
			case FORWARD:
				return AFT;
			}
			return this;
		}
	}

	/**
	 * Sets the total set of ship directions that this object
	 * can ever cover or weapons fire at.  Some shields or guns
	 * may only be mounted on* the front, rear, or other areas
	 * of the ship.  This tells the system the complete set of
	 * coverage by the shield or weapon, even if it is
	 * incapable of covering them all at once.
	 * @see ShipDirectional#getPermittedDirections()
	 * @param newPossDirs the total set of ship directions
	 */
	public void setPermittedDirections(ShipDir[] newPossDirs);

	/**
	 * Gets the total set of ship directions that this shield
	 * can ever cover or weapons fire at.  Some shields or guns
	 * may only be mounted on* the front, rear, or other areas
	 * of the ship.  This tells the system the complete set of
	 * coverage by the shield or weapon, even if it is
	 * incapable of covering them all at once.
	 * see also #setPermittedDirections(ShipDir[])
	 * @return the total set of ship directions
	 */
	public ShipDir[] getPermittedDirections();

	/**
	 * Sets the total number of quarters or sections of the
	 * ship that can be covered by this shield or shot by
	 * a gun at any one time. The sections are always contiguous,
	 * centered on a particular section, and moving outward
	 * as per the ShipDir list order.
	 * @see ShipDir
	 * @see ShipDirectional#getPermittedDirections()
	 * @see #setPermittedNumDirections(int)
	 * @param numDirs the total number of sections covered
	 */
	public void setPermittedNumDirections(int numDirs);

	/**
	 * Gets the total number of quarters or sections of the
	 * ship that can be covered by this shield or shot by
	 * a gun at any one time. The sections are always contiguous,
	 * centered on a particular section, and moving outward
	 * as per the ShipDir list order.
	 * @see ShipDir
	 * @see ShipDirectional#getPermittedDirections()
	 * @see ShipDirectional#setPermittedNumDirections(int)
	 * @return the total number of sections covered
	 */
	public int getPermittedNumDirections();
}
