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
 * An interface for an Electrical item that also consumes
 * some other sort of fuel other than electrical power, if
 * it consumes any electricity at all.  Because they require
 * fuel, these items are typically containers that can only
 * hold the type of fuel they need to operate.
 * Generators are typical of Fuel Consumers.
 * @author Bo Zimmerman
 *
 */
public interface FuelConsumer extends Electronics, Container
{
	/**
	 * Gets an array of RawMaterial codes representing the
	 * type of fuel that must be put inside this fuel container
	 * for it to consume it.
	 * @see FuelConsumer#setConsumedFuelType(int[])
	 * @see RawMaterial
	 * @return an array of RawMaterial codes
	 */
	public int[] getConsumedFuelTypes();
	
	/**
	 * Sets an array of RawMaterial codes representing the
	 * type of fuel that must be put inside this fuel container
	 * for it to consume it.
	 * @see FuelConsumer#getConsumedFuelTypes()
	 * @see RawMaterial
	 * @param resources an array of RawMaterial codes
	 */
	public void setConsumedFuelType(int[] resources);
	
	/**
	 * Gets the number of ticks between each consumption of fuel.
	 * This determines the rate of fuel consumption, assuming the
	 * amount of fuel itself is fixed, or determined internally.
	 * This is the only way variation in consumption is controlled
	 * from outside.
	 * @see FuelConsumer#setTicksPerFuelConsume(int)
	 * @return the number of ticks between each consumption of fuel.
	 */
	public int getTicksPerFuelConsume();
	
	/**
	 * Sets the number of ticks between each consumption of fuel.
	 * This determines the rate of fuel consumption, assuming the
	 * amount of fuel itself is fixed, or determined internally.
	 * This is the only way variation in consumption is controlled
	 * from outside.
	 * @see FuelConsumer#getTicksPerFuelConsume()
	 * @param tick the number of ticks between each consumption of fuel.
	 */
	public void setTicksPerFuelConsume(int tick);
	
	/**
	 * Returns the amount of fuel remaining in this container.
	 * @see FuelConsumer#getTotalFuelCapacity()
	 * @return the amount of fuel remaining in this container.
	 */
	public int getFuelRemaining();
	
	/**
	 * Forces this fuel consumer to consumer some amount of its
	 * fuel, without any other effect. If there was not enough
	 * fuel to be consumed, it might result in de-activation.
	 * @param amount the amount of fuel to consume
	 * @return true if there was NOT enough fuel, false if fuel was consumed OK.
	 */
	public boolean consumeFuel(int amount);
	
	/**
	 * Returns the amount of total fuel this container can hold.
	 * @see FuelConsumer#getFuelRemaining()
	 * @return the amount of total fuel this container can hold.
	 */
	public int getTotalFuelCapacity();
}
