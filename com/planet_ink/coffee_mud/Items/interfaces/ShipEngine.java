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
 * A ShipEngine is a special TechComponent that is often a fuel consumer, 
 * and which has special tracked attributes related to its ability to 
 * propel a ship and, usually, consume fuel.
 * @author Bo Zimmerman
 *
 */
public interface ShipEngine extends TechComponent
{
	/**
	 * Gets set of available thrust ports on this engine.
	 * @see ShipEngine#setAvailPorts(com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir[])
	 * @return the set of available thrust ports.
	 */
	public TechComponent.ShipDir[] getAvailPorts();

	/**
	 * Sets set of available thrust ports on this engine.
	 * @see ShipEngine#getAvailPorts()
	 * @param ports the set of available thrust ports.
	 */
	public void setAvailPorts(TechComponent.ShipDir[] ports);

	/**
	 * Gets the maximum amount of thrust that this engine can put out.
	 * @see ShipEngine#setMaxThrust(int)
	 * @return the maximum amount of thrust that this engine can put out.
	 */
	public int getMaxThrust();

	/**
	 * Sets the maximum amount of thrust that this engine can put out.
	 * @see ShipEngine#getMaxThrust()
	 * @param max the maximum amount of thrust that this engine can put out.
	 */
	public void setMaxThrust(int max);

	/**
	 * Gets the minimum amount of thrust that this engine can put out.
	 * @see ShipEngine#setMinThrust(int)
	 * @return the minimum amount of thrust that this engine can put out.
	 */
	public int getMinThrust();

	/**
	 * Sets the minimum amount of thrust that this engine can put out.
	 * @see ShipEngine#getMinThrust()
	 * @param min the minimum amount of thrust that this engine can put out.
	 */
	public void setMinThrust(int min);

	/**
	 * Gets whether this engine, once thrust is engaged, will continue
	 * to thrust at that speed, thus accellerating.  True if it does, 
	 * and false if whatever speed you get out of it is all you get.
	 * @see ShipEngine#setConstantThruster(boolean)
	 * @return true for an accellerator, false for one shot
	 */
	public boolean isConstantThruster();

	/**
	 * Sets whether this engine, once thrust is engaged, will continue
	 * to thrust at that speed, thus accellerating.  True if it does, 
	 * and false if whatever speed you get out of it is all you get.
	 * @see ShipEngine#isConstantThruster()
	 * @param isConstant true for an accellerator, false for one shot
	 */
	public void setConstantThruster(boolean isConstant);

	/**
	 * Gets the current amount of thrust being emitted by this ShipEngine,
	 * typically only describing the AFT thrust, since all other thrust
	 * is done in spurts.
	 * @see ShipEngine#setThrust(double)
	 * @return the current amount of aft thrust
	 */
	public double getThrust();

	/**
	 * Gets the current amount of thrust being emitted by this ShipEngine,
	 * typically only describing the AFT thrust, since all other thrust
	 * is done in spurts.
	 * @see ShipEngine#getThrust()
	 * @param aftThrust the current amount of aft thrust
	 */
	public void setThrust(double aftThrust);

	/**
	 * Specific Impulse has to do with the efficiency of conversion of
	 * fuel into thrust.
	 * @see ShipEngine#setSpecificImpulse(long)
	 * @return efficiency of conversion of fuel into thrust
	 */
	public long getSpecificImpulse();

	/**
	 * Specific Impulse has to do with the efficiency of conversion of
	 * fuel into thrust.
	 * @see ShipEngine#getSpecificImpulse()
	 * @param amt efficiency of conversion of fuel into thrust
	 */
	public void setSpecificImpulse(long amt);

	/**
	 * Gets the fuel efficiency pct, denoting how well the engine uses
	 * fuel to product thrust.  Basically, this is what specific impulse
	 * SHOULD be.
	 * @see ShipEngine#setFuelEfficiency(double)
	 * @return the fuel efficiency pct
	 */
	public double getFuelEfficiency();

	/**
	 * Sets the fuel efficiency pct, denoting how well the engine uses
	 * fuel to product thrust.  Basically, this is what specific impulse
	 * SHOULD be.
	 * @see ShipEngine#getFuelEfficiency()
	 * @param amt the fuel efficiency pct
	 */
	public void setFuelEfficiency(double amt);
	
	/**
	 * Forces this engine to consumer some amount of its
	 * fuel, without any other effect. If there was not enough
	 * fuel to be consumed, it might result in de-activation.
	 * @param amount the amount of fuel to consume
	 * @return true if there was NOT enough fuel, false if fuel was consumed OK.
	 */
	public boolean consumeFuel(int amount);
}
