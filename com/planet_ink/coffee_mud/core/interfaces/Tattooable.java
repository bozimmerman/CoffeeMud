package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;
import com.planet_ink.coffee_mud.Common.interfaces.Tattoo;

/*
   Copyright 2015-2016 Bo Zimmerman

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
*
* Something that can be tattooed with a named marker.
* @see com.planet_ink.coffee_mud.Common.interfaces.Tattoo
* @author Bo Zimmerman
*
*/
public interface Tattooable extends CMObject
{
	/**
	 * Add a new tattoo to this object
	 * @param of the tattoo object to add
	 */
	public void addTattoo(Tattoo of);
	
	/**
	 * Add a new tattoo to this object
	 * @param of the permanent tattoo name to add
	 */
	public void addTattoo(String of);
	/**
	 * Add a new tattoo to this object
	 * @param of the permanent tattoo name to add
	 * @param tickDown the ticks of life for this tattoo
	 */
	public void addTattoo(String of, int tickDown);
	
	/**
	 * Remove a specific tattoo from this object
	 * @param of the tattoo object to remove
	 */
	public void delTattoo(Tattoo of);
	
	/**
	 * Remove a specific tattoo from this object
	 * @param of the tattoo name of the tattoo to remove
	 */
	public void delTattoo(String of);

	/**
	 * Returns an enumeration of all the tattoos on this object.
	 * @return an enumeration of all the tattoos on this object.
	 */
	public Enumeration<Tattoo> tattoos();
	
	/**
	 * Returns the tattoo of the given marker name
	 * @param of the marker name to return a tattoo for
	 * @return the tattoo of the given marker name
	 */
	public Tattoo findTattoo(String of);
}
