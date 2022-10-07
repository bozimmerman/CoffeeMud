package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.CostDef.CostType;
/*
Copyright 2010-2022 Bo Zimmerman

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
 * Class for the definition of a cost of some sort
 *
 * @author Bo Zimmerman
 */
public interface CostDef
{
	/**
	 * Enumeration of the types of costs of gaining this ability
	 */
	public enum CostType
	{
		TRAIN,
		PRACTICE,
		XP,
		GOLD,
		QP;
	}

	/**
	 * Returns the type of resources defining the cost
	 * of a skill.
	 * @see CostType
	 * @return the type of cost
	 */
	public CostType type();

	/**
	 * A math formula definition the amount of the cost
	 * type required, where at-x1 is the qualifying level
	 * and at-x2 is the player level
	 *
	 * @return the amount formula
	 */
	public String costDefinition();
}
