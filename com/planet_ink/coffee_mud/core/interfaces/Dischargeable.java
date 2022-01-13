package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;

/*
   Copyright 2012-2022 Bo Zimmerman

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
* Something that can be discharged.
* @author Bo Zimmerman
*
*/
public interface Dischargeable extends Environmental
{
	/**
	 * Returns the number charges remaining
	 * @return the number charges remaining
	 */
	public int getCharges();

	/**
	 * Sets the number of charges remaining
	 * @param num the number of charges remaining
	 */
	public void setCharges(int num);


	/**
	 * Gets the max number of times this can be invoked before
	 * being drained and empty useless.
	 * @see Dischargeable#setMaxCharges(int)
	 * @return the number of times this wand can be invoked
	 */
	public int getMaxCharges();

	/**
	 * Sets the max number of times this wand can be invoked before
	 * being drained and empty useless stick.
	 * @see Dischargeable#getMaxCharges()
	 * @param num the number of times this can be invoked
	 */
	public void setMaxCharges(int num);
}
