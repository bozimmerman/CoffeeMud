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
   Copyright 2001-2018 Bo Zimmerman

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
 * Coins are an item that represent a single coin, or a stack of dollar bills.
 * It is a collection of currency, of a total value that can be represented
 * in the Base Value system.
 * @author Bo Zimmerman
 */
public interface Coins extends Item
{
	/**
	 * Returns the number of units of currency in the stack
	 * @see Coins#setNumberOfCoins(long)
	 * @return the number of units of currency in the stack
	 */
	public long getNumberOfCoins();
	
	/**
	 * Sets the number of units of currency in the stack
	 * @see Coins#getNumberOfCoins()
	 * @param number the number of units of currency in the stack
	 */
	public void setNumberOfCoins(long number);
	
	/**
	 * This method will inspect the current owner of this item for other
	 * items of identical currency and denomination.  If any are found, it
	 * will add its numberOfCoins to THAT stack, and destroy itself.
	 * Otherwise it will do nothing 
	 * @see Coins#getNumberOfCoins()
	 * @return true if this item was destroyed due to a combining, false otherwise
	 */
	public boolean putCoinsBack();
	
	/**
	 * Returns the denomination value of this currency, in base values.  A penny,
	 * for example, would be a denomination of 0.01, when the dollar is "base".
	 * That said, 1.0 and higher is a more common response.
	 * @see Coins#setDenomination(double)
	 * @return the denomination value of this currency
	 */
	public double getDenomination();
	
	/**
	 * Sets the denomination value of this currency, in base values.  A penny,
	 * for example, would be a denomination of 0.01, when the dollar is "base".
	 * That said, 1.0 and higher is a more common response.
	 * @see Coins#getDenomination()
	 * @param valuePerCoin the denomination value of this currency
	 */
	public void setDenomination(double valuePerCoin);
	
	/**
	 * Returns the total value of this stack of currency in base value.  
	 * It is the same as getNumberOfCoins() * getDenomination()
	 * @see Coins#getNumberOfCoins()
	 * @see Coins#getDenomination()
	 * @return the total value of this stack of currency
	 */
	public double getTotalValue();
	
	/**
	 * Returns the currency that this stack of money belongs to.  It's a coded ID that
	 * defines a set of valid denominations, abbreviations, and so forth.  Values are
	 * things like GOLDSTANDARD, DOLLARS, etc..
	 * @return the currency that this stack of money belongs to
	 */
	public String getCurrency();
	
	/**
	 * Sets the currency that this stack of money belongs to.  It's a coded ID that
	 * defines a set of valid denominations, abbreviations, and so forth.  Values are
	 * things like GOLDSTANDARD, DOLLARS, etc..
	 * @param named the currency that this stack of money belongs to
	 */
	public void setCurrency(String named);
}
