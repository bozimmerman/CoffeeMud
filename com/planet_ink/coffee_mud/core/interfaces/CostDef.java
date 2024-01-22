package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.core.collections.Triad;
import com.planet_ink.coffee_mud.core.interfaces.CostDef.CostType;
/*
Copyright 2006-2024 Bo Zimmerman

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
		MANA,
		MOVEMENT,
		HITPOINT,
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
	 * Currency, if applicable, to the cost.
	 *
	 * @return null, or "", or a currency
	 */
	public String typeCurrency();

	/**
	 * A math formula definition the amount of the cost
	 * type required, where at-x1 is the qualifying level
	 * and at-x2 is the player level
	 *
	 * @return the amount formula
	 */
	public String costDefinition();

	/**
	 * A final cost is an integer, and the type, and possibly
	 * a currency.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static class Cost extends Triad<Double,CostType, String>
	{
		private static final long serialVersionUID = 1386265685565326643L;

		/**
		 * Construct a Cost object
		 * @param amt the amount
		 * @param type the cost type
		 * @param currency the currency type
		 */
		public Cost(final double amt, final CostType type, final String currency)
		{
			super(Double.valueOf(amt), type, currency);
		}

		/**
		 * Construct a Cost object
		 * @param amt the amount
		 * @param type the cost type
		 */
		public Cost(final double amt, final CostType type)
		{
			super(Double.valueOf(amt), type, "");
		}
	}
}
