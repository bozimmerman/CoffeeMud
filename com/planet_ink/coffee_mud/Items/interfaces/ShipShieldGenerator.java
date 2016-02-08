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
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2016 Bo Zimmerman

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
 * A shield generator is an installed component that mitigates the
 * damage from ship damage of various sorts.
 * 
 * @author Bo Zimmerman
 *
 */
public interface ShipShieldGenerator extends TechComponent
{
	/**
	 * These are all the ordinary recognized damage message
	 * types that the several shields can even possibly
	 * mitigate.
	 */
	public static final int[] AVAIL_DAMAGE_TYPES = 
	{
		CMMsg.TYP_COLLISION,
		CMMsg.TYP_ELECTRIC,
		CMMsg.TYP_ACID,
		CMMsg.TYP_COLD,
		CMMsg.TYP_FIRE,
		CMMsg.TYP_GAS,
		CMMsg.TYP_LASER,
		CMMsg.TYP_PARALYZE,
		CMMsg.TYP_POISON,
		CMMsg.TYP_SONIC,
		CMMsg.TYP_UNDEAD,
		CMMsg.TYP_WATER
	};
	
	/**
	 * Sets the total set of ship directions that this shield
	 * can ever cover.  Some shields may only be mounted on
	 * the front, rear, or other areas of the ship.  This
	 * tells the system the complete set of coverage by
	 * the shield, even if it is incapable of covering them
	 * all at once.
	 * @see ShipShieldGenerator#getPermittedDirections()
	 * @param newPossDirs the total set of ship directions
	 */
	public void setPermittedDirections(ShipDir[] newPossDirs);
	
	/**
	 * Gets the total set of ship directions that this shield
	 * can ever cover.  Some shields may only be mounted on
	 * the front, rear, or other areas of the ship.  This
	 * tells the system the complete set of coverage by
	 * the shield, even if it is incapable of covering them
	 * all at once.
	 * @see ShipShieldGenerator#setPermittedDirections(ShipDir[])
	 * @return the total set of ship directions
	 */
	public ShipDir[] getPermittedDirections();

	/**
	 * Sets the total number of quarters or sections of the
	 * ship that can be covered by this shield at any one time.
	 * The sections are always contiguous, centered on a 
	 * particular section, and moving outward as per the ShipDir
	 * list order.
	 * @see ShipDir
	 * @see ShipShieldGenerator#getPermittedDirections() 
	 * @see ShipShieldGenerator#setPermittedNumDirections(int) 
	 * @param numDirs the total number of sections covered
	 */
	public void setPermittedNumDirections(int numDirs);

	/**
	 * Gets the total number of quarters or sections of the
	 * ship that can be covered by this shield at any one time.
	 * The sections are always contiguous, centered on a 
	 * particular section, and moving outward as per the ShipDir
	 * list order.
	 * @see ShipDir
	 * @see ShipShieldGenerator#getPermittedDirections() 
	 * @see ShipShieldGenerator#setPermittedNumDirections(int) 
	 * @return the total number of sections covered
	 */
	public int getPermittedNumDirections();

	/**
	 * Sets the set of CMMsg message types that can be blocked
	 * and or managed by these shields.
	 * @see CMMsg#TYP_ACID
	 * @see ShipShieldGenerator#getShieldedMsgTypes()
	 * @param newTypes the set of message types
	 */
	public void setShieldedMsgTypes(int[] newTypes);
	
	/**
	 * Gets the set of CMMsg message types that can be blocked
	 * and or managed by these shields.
	 * @see CMMsg#TYP_ACID
	 * @see ShipShieldGenerator#setShieldedMsgTypes(int[])
	 * @return the set of message types
	 */
	public int[] getShieldedMsgTypes();
}