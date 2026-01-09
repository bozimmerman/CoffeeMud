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
   Copyright 2016-2026 Bo Zimmerman

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
	 * @see ShipEngine#setAvailPorts(com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir[])
	 * @return the set of available thrust ports.
	 */
	public ShipDirectional.ShipDir[] getAvailPorts();

	/**
	 * Sets set of available thrust ports on this engine.
	 * @see ShipEngine#getAvailPorts()
	 * @param ports the set of available thrust ports.
	 */
	public void setAvailPorts(ShipDirectional.ShipDir[] ports);

	/**
	 * Gets the maximum amount of thrust that this engine can put out.
	 * @see ShipEngine#setMaxThrust(double)
	 * @return the maximum amount of thrust that this engine can put out.
	 */
	public double getMaxThrust();

	/**
	 * Sets the maximum amount of thrust that this engine can put out.
	 * @see ShipEngine#getMaxThrust()
	 * @param max the maximum amount of thrust that this engine can put out.
	 */
	public void setMaxThrust(double max);

	/**
	 * Gets the minimum amount of thrust that this engine can put out.
	 * @see ShipEngine#setMinThrust(double)
	 * @return the minimum amount of thrust that this engine can put out.
	 */
	public double getMinThrust();

	/**
	 * Sets the minimum amount of thrust that this engine can put out.
	 * @see ShipEngine#getMinThrust()
	 * @param min the minimum amount of thrust that this engine can put out.
	 */
	public void setMinThrust(double min);

	/**
	 * Gets whether this engine, once thrust is engaged, will continue
	 * to thrust at that speed, thus accelerating.  True if it does,
	 * and false if whatever speed you get out of it is all you get.
	 * @see ShipEngine#setReactionEngine(boolean)
	 * @return true for an accelerator, false for one shot
	 */
	public boolean isReactionEngine();

	/**
	 * Sets whether this engine, once thrust is engaged, will continue
	 * to thrust at that speed, thus accelerating.  True if it does,
	 * and false if whatever speed you get out of it is all you get.
	 * @see ShipEngine#isReactionEngine()
	 * @param isConstant true for an accelerator, false for one shot
	 */
	public void setReactionEngine(boolean isConstant);

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
	 * Gets the specific impulse of this engine.
	 * Specific Impulse has to do with the efficiency of conversion of
	 * fuel into thrust.  It essentially determines the fuel consumption
	 * by dividing the thrust / specific impulse * constant.
	 *
	 * @see ShipEngine#setSpecificImpulse(long)
	 * @return efficiency of conversion of fuel into thrust
	 */
	public double getSpecificImpulse();

	/**
	 * Sets the specific impulse of this engine.
	 * Specific Impulse has to do with the efficiency of conversion of
	 * fuel into thrust.  It essentially determines the fuel consumption
	 * by dividing the thrust / specific impulse * constant.
	 *
	 * @see ShipEngine#getSpecificImpulse()
	 * @param amt efficiency of conversion of fuel into thrust
	 */
	public void setSpecificImpulse(double amt);

	/**
	 * Forces this engine to consumer some amount of its
	 * fuel, without any other effect. If there was not enough
	 * fuel to be consumed, it might result in de-activation.
	 *
	 * @param amount the amount of fuel to consume
	 * @return true if there was NOT enough fuel, false if fuel was consumed OK.
	 */
	public boolean consumeFuel(int amount);

	/**
	 * An Accelerator is an object that knows how to make a particular
	 * type of ShipEngine work.  Different engines have different
	 * physics and fuel consumption models, and so each engine
	 * type has its own Accelerator class.
	 * @return the Accelerator object for this engine
	 */
	public static interface ShipAccelerator
	{
		
		/**
		 * Execute the activate command, which is a command
		 * sent to the ship's thruster to cause thrust in
		 * some direction.
		 * @param msg the thruster message
		 * @return true if the command was successfully executed
		 */
		public boolean executeActivateCommand(final CMMsg msg, final String circuitKey);
		
		/**
		 * Execute the deactivate command, which is a command
		 * sent to the ship's thruster to stop thrusting.
		 * @param msg the thruster message
		 * @return true if the command was successfully executed
		 */
		public boolean executeDeactivateCommand(final MOB mob);
		
		/**
		 * Execute the ongoing thrust command, which is a command
		 * sent to the ship's thruster to continue thrusting.
		 * @param mob the mob acting as agent
		 * @param circuitKey the key of the circuit that contains this thruster
		 * @return true if the command was successfully executed
		 */
		public boolean executeOngoingThrustCommand(final MOB mob, final String circuitKey);
		
		/**
		 * Execute a thrust action, which is an action
		 * sent to the ship's thruster to cause thrust in
		 * some direction.
		 * @param mob the mob doing the thrusting
		 * @param controlI the software controlling the engine
		 * @param circuitKey the key of the circuit that contains this thruster
		 * @param portDir the direction of the thrust
		 * @param injection the amount of thrust to inject
		 * @param simulation true if this is just a simulation
		 * @return true if the command was successfully executed
		 */
		public boolean executeThrust(final MOB mob, final Software controlI, final String circuitKey, 
									final ShipDirectional.ShipDir portDir, final double injection, final boolean simulation);
		
		/**
		 * Given an amount of injection 0-1, return the amount of thrust
		 * that will actually be sent into the ship's movement.
		 * This may be less than the requested amount, due to
		 * engine limitations.
		 * @param injection the amount of thrust to inject
		 * @return the amount of thrust that will actually be injected
		 */
		public double getInjectedThrust(final double injection);
		
		/**
		 * Given a direction and an amount of thrust, return the
		 * amount of fuel that will be consumed.
		 * @param portDir the direction of the thrust
		 * @param thrust the amount of thrust to be generated
		 * @return the amount of fuel that will be consumed
		 */
		public int getFuelToConsume(final ShipDir portDir, double thrust);
		
		/**
		 * Returns the fuel factor, which is a multiplier
		 * applied to all fuel consumption calculations.
		 * @return the fuel factor
		 */
		public double getFuelThrustCap();
	}
}
