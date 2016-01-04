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
   Copyright 2004-2016 Bo Zimmerman

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
 * A ShipComponent is a type of electronics item that must be installed into
 * a space ship into a ship panel that can accept it.
 * 
 * @author Bo Zimmerman
 *
 */
public interface ShipComponent extends Electronics
{
	/**
	 * The installed factor is how well the equipment was installed. 0-1.0
	 * @see ShipComponent#setInstalledFactor(float)
	 * @return installed factor is how well the equipment was installed. 0-1.0
	 */
	public float getInstalledFactor();

	/**
	 * The installed factor is how well the equipment was installed. 0-1.0
	 * @see ShipComponent#getInstalledFactor()
	 * @param pct installed factor is how well the equipment was installed. 0-1.0
	 */
	public void setInstalledFactor(float pct);

	/**
	 * A ShipEngine is a special ShipComponent that is also a fuel consumer, 
	 * and which has special tracked attributes related to its ability to 
	 * propel a ship and consume fuel.
	 * @author Bo Zimmerman
	 *
	 */
	public interface ShipEngine extends ShipComponent, Electronics.FuelConsumer
	{
		/**
		 * The ThrustPort enum is for the different thrust ports, denoting
		 * the port, by its direction location.
		 * @author Bo Zimmerman
		 */
		public enum ThrustPort
		{
			AFT,
			PORT,
			VENTRAL,
			DORSEL,
			STARBOARD,
			FORWARD
		}

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
		 * Gets the current amount of thrust being emitted by this ShipEngine,
		 * typically only describing the AFT thrust, since all other thrust
		 * is done in spurts.
		 * @see ShipEngine#setThrust(int)
		 * @return the current amount of aft thrust
		 */
		public int getThrust();

		/**
		 * Gets the current amount of thrust being emitted by this ShipEngine,
		 * typically only describing the AFT thrust, since all other thrust
		 * is done in spurts.
		 * @see ShipEngine#getThrust()
		 * @param aftThrust the current amount of aft thrust
		 */
		public void setThrust(int aftThrust);

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
	}
}
