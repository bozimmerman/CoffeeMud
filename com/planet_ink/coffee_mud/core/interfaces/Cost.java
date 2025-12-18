package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.CostDef.CostType;

/**
 * Interface for the definition of a cost
 */
public interface Cost
{
	/**
	 * Produce a string representation of this cost.
	 * @return a string representation of this cost.
	 */
	public String value();

	/**
	 * Returns the amount as double;
	 * @return the amount as double
	 */
	public double priceD();

	/**
	 * Returns the amount as integer;
	 * @return the amount as integer
	 */
	public int priceI();

	/**
	 * Returns the cost type
	 * @return the cost type
	 */
	public CostType type();

	/**
	 * Returns the currency
	 * @return the currency
	 */
	public String currency();

	/**
	 * Derive cost from a string representation of this cost.
	 * @param str a string representation of this cost.
	 * @return a cost object
	 */
	public Cost valueOf(final String str);

}
