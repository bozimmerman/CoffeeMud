package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Electronics.ElecPanel;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2013 Bo Zimmerman

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
public interface TechLibrary extends CMLibrary
{
	/**
	 * Unregisters an electronic component that belonged
	 * in a complex circuitry, like a panel or a 
	 * generator.
	 * @param E the electronic component to unregister
	 * @param oldKey the last key registered to this device
	 */
	public void unregisterElectronics(Electronics E, String oldKey);
	
	/**
	 * Unregisters all electronic components that belonged
	 * in a complex circuitry, like a panel or a 
	 * generator, of the given key.
	 * @param oldKey the last key registered to this device
	 */
	public void unregisterAllElectronics(final String oldKey);
	
	/**
	 * Registers an electronic component that belongs
	 * in a complex circuitry, like a panel or a 
	 * generator
	 * @param E the electronic component to register
	 * @param oldKey the last key registered to this device
	 * @return the new key assigned to this item (or old key)
	 */
	public String registerElectrics(Electronics E, String oldKey);
	
	/**
	 * Returns a new exclusive list of all the registered electronics
	 * keys being processed.
	 * @return the list of keys
	 */
	public List<String> getMakeRegisteredKeys();
	
	/**
	 * For the given key, return an eclusive list of all the electronics
	 * that belong to that key.
	 * @param key the key to return electronics for
	 * @return
	 */
	public List<Electronics> getMakeRegisteredElectronics(String key);
	
	/**
	 * Certain Key Systems may automatically force batteries in their circuit
	 * to activate in order to provide that system with power. This will make
	 * that attempt.
	 * @param E the key device to seek power
	 * @param key this devices key
	 * @return true if an attempt to give power was made, false otherwise.
	 */
	public boolean seekBatteryPower(final ElecPanel E, final String key);

	/**
	 * Retreives the default manufacturer for new products
	 * @return the default manufacturer for new products
	 */
	public Manufacturer getDefaultManufacturer();
	
	/**
	 * Adds and saves a new manufacturer to the list.
	 * @param manufacturer the one to add
	 */
	public void addManufacturer(Manufacturer manufacturer);
	
	/**
	 * Removes a new manufacturer from the list.
	 * @param manufacturer the one to remove
	 */
	public void delManufacturer(Manufacturer manufacturer);
	
	/**
	 * Updates a new manufacturer in the list.
	 * @param manufacturer the one to update
	 */
	public void updateManufacturer(Manufacturer manufacturer);
	
	/**
	 * Retrieves the manufacturer of the given name, or null
	 * if it is not found.
	 * @param name the manufacturer to fetch
	 * @return the manufacturer found, or null
	 */
	public Manufacturer getManufacturer(String name);
	
	/**
	 * Returns an iterator of manufacturers
	 * @return the set to return
	 */
	public Iterator<Manufacturer> manufacterers();
}
