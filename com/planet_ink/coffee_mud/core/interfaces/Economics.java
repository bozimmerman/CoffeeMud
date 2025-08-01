package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Vector;

/*
   Copyright 2006-2025 Bo Zimmerman

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
 * An interface for objects capable of modifying economic conditions
 * @author Bo Zimmerman
 *
 */
public interface Economics extends Environmental
{
	/**
	 * A string describing how pricing for this ShopKeeper will differ based on customer attributes
	 * such as race.
	 * @return the string describing price prejudicing
	 */
	public String getRawPrejudiceFactors();

	/**
	 * A string describing how pricing for this ShopKeeper will differ based on customer attributes
	 * such as race.
	 *
	 * @return the string describing price prejudicing
	 */
	public String getFinalPrejudiceFactors();

	/**
	 * Sets the string describing how pricing for this ShopKeeper will differ based on customer attributes
	 * such as race.
	 * @param factors the string describing price prejudicing
	 */
	public void setPrejudiceFactors(String factors);

	/**
	 * A string set describing how pricing for this ShopKeeper will differ based on item masks
	 * The format for each string is a floating point number followers by a space and a zapper mask
	 * @return an array of the strings describing price adjustments
	 */
	public String[] getFinalItemPricingAdjustments();

	/**
	 * A string set describing how pricing for this ShopKeeper will differ based on item masks
	 * The format for each string is a floating point number followers by a space and a zapper mask
	 * @return an array of the strings describing price adjustments
	 */
	public String[] getRawItemPricingAdjustments();

	/**
	 * Sets the string set describing how pricing for this ShopKeeper will differ based on item masks
	 * The format for each string is a floating point number followers by a space and a zapper mask
	 * @param factors the string describing price prejudicing
	 */
	public void setItemPricingAdjustments(String[] factors);

	/**
	 * Returns the mask used to determine if a customer is ignored by the ShopKeeper.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return the mask used
	 */
	public String getFinalIgnoreMask();

	/**
	 * Returns the mask used to determine if a customer is ignored by the ShopKeeper.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return the mask used
	 */
	public String getRawIgnoreMask();

	/**
	 * Sets the mask used to determine if a customer is ignored by the ShopKeeper.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param factors the mask to use
	 */
	public void setIgnoreMask(String factors);

	/**
	 * Returns a description of the buying budget of the shopkeeper.  Format is
	 * an amount of base currency followed by HOUR,WEEK,DAY,MONTH or YEAR.
	 * @return the string for the shopkeepers buying budget
	 */
	public String getRawBbudget();

	/**
	 * Returns a description of the buying budget of the shopkeeper.  Format is
	 * an amount of base currency followed by HOUR,WEEK,DAY,MONTH or YEAR.
	 * @return the pair for the shopkeepers buying budget
	 */
	public Pair<Long, TimePeriod> getFinalBudget();

	/**
	 * Sets a description of the buying budget of the shopkeeper.  Format is
	 * an amount of base currency followed by HOUR,WEEK,DAY,MONTH or YEAR.
	 * @param factors the string for the shopkeepers buying budget
	 */
	public void setBudget(String factors);

	/**
	 * Returns a double array describing the percentage in the drop of the price at
	 * which this ShopKeeper will buy back items based on the number already
	 * in his inventory.  The format is a number representing the percentage
	 * price drop per normal item followed by a space, followed by a number
	 * representing the percentage price drop per raw resource item. A value
	 * of null or [0,0] would mean no drop in price for either,  ever.
	 * @return null, or the price dropping percentage rule for this shopkeeper
	 */
	public double[] getFinalDevalueRate();

	/**
	 * Returns a string describing the percentage in the drop of the price at
	 * which this ShopKeeper will buy back items based on the number already
	 * in his inventory.  The format is a number representing the percentage
	 * price drop per normal item followed by a space, followed by a number
	 * representing the percentage price drop per raw resource item. A value
	 * of "0 0" would mean no drop in price for either,  ever.
	 * @return the price dropping percentage rule for this shopkeeper
	 */
	public String getRawDevalueRate();

	/**
	 * Sets a string describing the percentage in the drop of the price at
	 * which this ShopKeeper will buy back items based on the number already
	 * in his inventory.  The format is a number representing the percentage
	 * price drop per normal item followed by a space, followed by a number
	 * representing the percentage price drop per raw resource item. A value
	 * of "0 0" would mean no drop in price for either,  ever.
	 * @param factors the price dropping percentage rule for this shopkeeper
	 */
	public void setDevalueRate(String factors);

	/**
	 * Returns the number of ticks between totally resetting this ShopKeepers
	 * inventory back to what it was.
	 *
	 * @return the number of ticks between total resets of inventory
	 */
	public int getFinalInvResetRate();

	/**
	 * Returns the number of ticks between totally resetting this ShopKeepers
	 * inventory back to what it was.
	 *
	 * @return the number of ticks between total resets of inventory
	 */
	public int getRawInvResetRate();

	/**
	 * Sets the number of ticks between totally resetting this ShopKeepers
	 * inventory back to what it was.
	 *
	 * @param ticks the number of ticks between total resets of inventory
	 */
	public void setInvResetRate(int ticks);

	/**
	 * Gets the derived currency for this object, which will be referenced by
	 * shopkeepers, bankers, and other mobs.  See Archon's Guide for the proper
	 * format for this string, as it can be anything from a full current
	 * definition, to a reference to an existing one.
	 * @return a currency name/definition
	 */
	public String getFinalCurrency();

	/**
	 * Gets the default currency for this object, which will be referenced by
	 * shopkeepers, bankers, and other mobs.  See Archon's Guide for the proper
	 * format for this string, as it can be anything from a full current
	 * definition, to a reference to an existing one.
	 * @return a currency name/definition or NULL to derive
	 */
	public String getRawCurrency();

	/**
	 * Returns the default currency for this object, which will be referenced by
	 * shopkeepers, bankers, and other mobs.  See Archon's Guide for the proper
	 * format for this string, as it can be anything from a full current
	 * definition, to a reference to an existing one.
	 * @param currency  a currency name/definition
	 */
	public void setCurrency(String currency);

	/**
	 * The interface for a single economic prejudice factor.
	 * Each entry is a group name, representing a group
	 * to have unique factors for, followed by the factor
	 * to multiply the prices by.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface Prejudice
	{
		/**
		 * The prejudice factor group, typically
		 * the name of a race, class, race category,
		 * gender, etc.
		 * @return the factor group
		 */
		public String group();

		/**
		 * Number to multiply prices by when
		 * buying from players.
		 * @return buy multiplier
		 */
		public double buyFactor();

		/**
		 * Number to multiply prices by when
		 * selling to players.
		 * @return sell multiplier
		 */
		public double sellFactor();
	}

	/**
	 * The interface for economic Prejudice factors,
	 * as well as various miscellaneous fields for
	 * shopkeepers.
	 *
	 * @author BZ
	 */
	public static interface Prejudices
	{
		/**
		 * The set of prejudice factors for various
		 * groups.
		 * @return the prejudice group factors
		 */
		public Prejudice[]  factors();

		/**
		 * The maximum number of rows of produces to display
		 * at a time when a player LISTs inventory. 0 disables.
		 *
		 * @return 0, or maximum rows to display.
		 */
		public int displayLimit();

		/**
		 * The level range that the shopkeeper will buy.  If
		 * this is between 0-1, it is a percentage of the areas
		 * median level.  Otherwise, it is the number of levels
		 * above or below the median that the shopkeeper will buy.
		 *
		 * @return the level range factor
		 */
		public double levelRange();
	}
}
