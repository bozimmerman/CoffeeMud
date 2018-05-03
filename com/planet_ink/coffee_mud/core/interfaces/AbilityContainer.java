package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;

/*
   Copyright 2012-2018 Bo Zimmerman

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
* Something that can know or contain abilities for use.
* @author Bo Zimmerman
*
*/
public interface AbilityContainer
{
	/**
	 * Adds a new ability to this for use.
	 * No ability with the same ID can be contained twice.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param to the Ability to add.
	 */
	public void addAbility(Ability to);

	/**
	 * Removes the exact given ability object from here.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param to the exact Ability to remove
	 */
	public void delAbility(Ability to);

	/**
	 * Returns the number of abilities contained herein this object.
	 * Any extraneous abilities bestowed from other sources will NOT
	 * be returned -- only the exact abilities owned herein.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return the number of owned abilities
	 */
	public int numAbilities();

	/**
	 * Returns the Ability object at that index in this container.
	 * Any extraneous abilities bestowed from other sources MAY
	 * be returned, so long as index &gt; numAbilities.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param index the index of the Ability object to return
	 * @return the Ability object
	 */
	public Ability fetchAbility(int index);

	/**
	 * If contained herein, this will return the ability from this
	 * container of the given ID.
	 * Any extraneous abilities bestowed from other sources MAY
	 * be returned by this method.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param ID the ID of the ability to return.
	 * @return the Ability object
	 */
	public Ability fetchAbility(String ID);

	/**
	 * Returns a random ability from this container.
	 * Any extraneous abilities bestowed from other sources MAY
	 * be returned by this method.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a random Ability
	 */
	public Ability fetchRandomAbility();
	
	/**
	 * Returns an enumerator of the Ability objects in this container.
	 * Any extraneous abilities bestowed from other sources will NOT
	 * be returned -- only the exact abilities owned herein.
	 * @return An enumerator for abilities
	 */
	public Enumeration<Ability> abilities();

	/**
	 * Removes all owned abilities from this container.
	 * Any extraneous abilities bestowed from other sources will NOT
	 * be removed.
	 */
	public void delAllAbilities();

	/**
	 * Returns the number of all abilities in this container.
	 * Any extraneous abilities bestowed from other sources WILL
	 * be counted by this.
	 * @return the number of all abilities in this container
	 */
	public int numAllAbilities();

	/**
	 * Returns an enumerator of the Ability objects in this container.
	 * Any extraneous abilities bestowed from other sources WILL ALSO
	 * be returned.
	 * @return An enumerator for all abilities, both in the container and not
	 */
	public Enumeration<Ability> allAbilities();

}
