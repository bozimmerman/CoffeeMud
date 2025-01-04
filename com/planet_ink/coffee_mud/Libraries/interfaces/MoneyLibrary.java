package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/*
   Copyright 2005-2025 Bo Zimmerman

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
 * This library manages the official list of all currencies used
 * in the game, including the built-in default currencies:
 * "" (DEFAULT), GOLD, COPPER, CREDIT, DOLLAR, and VICTORY
 *
 * This also manages the creation and distribution of actual
 * money items in the known currencies.
 *
 * @author Bo Zimmerman
 *
 */
public interface MoneyLibrary extends CMLibrary
{
	/**
	 * Unregisters the given currency
	 *
	 * @param currency the currency to kill
	 */
	public void unloadCurrencySet(String currency);

	/**
	 * Creates and registers a new currency definition.
	 * This will be an encoded form like: NAME=DEFINITION
	 * DEFINITION=DENOMINATION,DENOMINATION,etc
	 * DENOMINATION=AMOUNT FULL_NAME (ABBREVIATION)
	 * AMOUNT=amount of base value for the denomination
	 * FULL_NAME=full name of the denomination with (s)
	 * ABBRECIATION=short code char of the denom
	 *
	 * @param currency the encoded currency definition
	 * @return null, or the definition of the currency
	 */
	public MoneyDefinition createCurrencySet(String currency);

	/**
	 * Returns the official MoneyDefinition object for the
	 * given currency.
	 *
	 * @param currency the currency
	 * @return the definition of the currency
	 */
	public MoneyDefinition getCurrencySet(String currency);

	/**
	 * Returns the list of all known currency names.
	 * This could change over time.
	 *
	 * @return the list of all known currencies
	 */
	public List<String> getAllCurrencies();

	/**
	 * Given a mob to get a currency from and a total money value, this
	 * will return final value of the given value after finding a single
	 * denomination of the currency that best matches.
	 *
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String, double)
	 * @see MoneyLibrary#abbreviatedPrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedPrice(String, double)
	 * @see MoneyLibrary#abbreviatedRePrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedRePrice(String, double)
	 *
	 * @param shopkeeper the mob to get a currency from to use
	 * @param absoluteAmount the total amount of value to use
	 * @return the best total value when converting to a single denomination
	 */
	public double abbreviatedRePrice(MOB shopkeeper, double absoluteAmount);

	/**
	 * Given a currency and a total money value, this will return the
	 * final value of the given value after finding a single denomination
	 * of the currency that best matches.
	 *
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String, double)
	 * @see MoneyLibrary#abbreviatedPrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedPrice(String, double)
	 * @see MoneyLibrary#abbreviatedRePrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedRePrice(String, double)
	 *
	 * @param currency the currency to use
	 * @param absoluteAmount the total amount of value to use
	 * @return the best total value when converting to a single denomination
	 */
	public double abbreviatedRePrice(String currency, double absoluteAmount);

	/**
	 * Given a mob to get a currency from and a total money value, this
	 * will return a very brief representation of the money in a single
	 * denomination of the currency that best matches.
	 *
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String, double)
	 * @see MoneyLibrary#abbreviatedPrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedPrice(String, double)
	 * @see MoneyLibrary#abbreviatedRePrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedRePrice(String, double)
	 *
	 * @param shopkeeper the mob to get a currency from to use
	 * @param absoluteAmount the total amount of value to name
	 * @return the best tiny price to show in a single denomination
	 */
	public String abbreviatedPrice(MOB shopkeeper, double absoluteAmount);

	/**
	 * Given a currency and a total money value, this will return a very
	 * brief representation of the money in a single denomination of the
	 * currency that best matches.
	 *
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String, double)
	 * @see MoneyLibrary#abbreviatedPrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedPrice(String, double)
	 * @see MoneyLibrary#abbreviatedRePrice(MOB, double)
	 * @see MoneyLibrary#abbreviatedRePrice(String, double)
	 *
	 * @param currency the currency to use
	 * @param absoluteAmount the total amount of value to name
	 * @return the best tiny price to show in a single denomination
	 */
	public String abbreviatedPrice(String currency, double absoluteAmount);

	/**
	 * Returns the ordinal index of the given denomination value in the
	 * given currency definitions list of denominations.
	 *
	 * @param currency the currency to use
	 * @param value the denomination value
	 * @return -1, or the index of the denom value
	 */
	public int getDenominationIndex(String currency, double value);

	/**
	 * Returns the names of all the denominations in the given currency.
	 *
	 * @param currency the currency to get denominations for
	 * @return the list of all denomination names
	 */
	public List<String> getDenominationNameSet(String currency);

	/**
	 * Returns the value of the lowest denomination in the given
	 * currency.
	 *
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String, double)
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String)
	 *
	 * @param currency the currency to use
	 * @return the lowest denomination value in the currency
	 */
	public double getLowestDenomination(String currency);

	/**
	 * Returns the lowest denomination in the given currency that
	 * has an abbreviation char/code.
	 *
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String, double)
	 * @see MoneyLibrary#getLowestDenomination(String)
	 *
	 * @param currency the currency to find a denomination for
	 * @return the lowest denomination with an abbreviation char
	 */
	public double lowestAbbreviatedDenomination(String currency);

	/**
	 * Returns the denomination of the given currency and given
	 * abbreviation or name.
	 *
	 * @param currency the currency
	 * @param name the abbreviation or name
	 * @return null, or the denomination
	 */
	public MoneyDenomination getDenomination(final String currency, final String name);

	/**
	 * Given a currency type and an absolute value, this will return the lowest
	 * denomination that best divides into the given absolute amount AND which
	 * has a abbreviation char/code.
	 *
	 * @see MoneyLibrary#lowestAbbreviatedDenomination(String)
	 * @see MoneyLibrary#getLowestDenomination(String)
	 * @see MoneyLibrary#getDenominationName(String)
	 *
	 * @param currency the currency type
	 * @param absoluteAmount the total amount to divide into
	 * @return the lowest denomination to use that is abbreviated
	 */
	public double lowestAbbreviatedDenomination(String currency, double absoluteAmount);

	/**
	 * Given a currency and a denomination value, this will return the short
	 * code/char of the denomination.
	 *
	 * @see MoneyLibrary#getDenominationName(String, double)
	 *
	 * @param currency the currency to use
	 * @param denomination the denomination value
	 * @return the name of the denominations short code
	 */
	public String getDenominationShortCode(String currency, double denomination);

	/**
	 * Returns the name of the lowest denomination in the given currency.
	 *
	 * @see MoneyLibrary#getDenominationName(String, double, long)
	 * @see MoneyLibrary#getDenominationName(String, double)
	 * @see MoneyLibrary#getDenominationName(MOB, double)
	 * @see MoneyLibrary#getLowestDenomination(String)
	 *
	 * @param currency the currency to use
	 * @return the name of the lowest denomination in that currency
	 */
	public String getDenominationName(String currency);

	/**
	 * Returns the amount and name of the denomination that matches the given
	 * currency and the given denomination value, in the given amount.
	 * This handles plurals!
	 *
	 * @see MoneyLibrary#getDenominationName(String)
	 * @see MoneyLibrary#getDenominationName(String, double)
	 * @see MoneyLibrary#getDenominationName(MOB, double)
	 *
	 * @param currency the currency type to use
	 * @param denomination the denomination value to match
	 * @param number the amount of the denomination to name
	 * @return the amount and name of the denomination
	 */
	public String getDenominationName(String currency,  double denomination, long number);

	/**
	 * Returns the name of the denomination that matches the given
	 * currency and the given denomination value.
	 *
	 * @see MoneyLibrary#getDenominationName(String)
	 * @see MoneyLibrary#getDenominationName(String, double, long)
	 * @see MoneyLibrary#getDenominationName(MOB, double)
	 * @see MoneyLibrary#getDenominationShortCode(String, double)
	 *
	 * @param currency the currency type to use
	 * @param denomination the denomination value to match
	 * @return the name of the denomination
	 */
	public String getDenominationName(String currency, double denomination);

	/**
	 * Returns the name of the denomination that matches the given mobs
	 * currency, and the given denomination value
	 *
	 * @see MoneyLibrary#getDenominationName(String)
	 * @see MoneyLibrary#getDenominationName(String, double, long)
	 * @see MoneyLibrary#getDenominationName(String, double)
	 *
	 * @param mob the mob to get a currency from
	 * @param denomination the denomination value to match
	 * @return the name of the denomination
	 */
	public String getDenominationName(final MOB mob, double denomination);

	/**
	 * Given a currency and a total money value in that currency, this will return
	 * the denomination which will most evenly divide into the given value.
	 *
	 * @see MoneyLibrary#getBestDenomination(String, int, double)
	 * @see MoneyLibrary#getBestDenominations(String, double)
	 *
	 * @param currency the currency to get denominations from
	 * @param absoluteValue the total value to parse out
	 * @return the best denomination to use to represent the given value
	 */
	public double getBestDenomination(String currency, double absoluteValue);

	/**
	 * Given a currency, a total money value, and a number of currency coins, this
	 * will find the denomination that is closest to the total money value in that
	 * given number of coins
	 *
	 * @see MoneyLibrary#getBestDenomination(String, double)
	 * @see MoneyLibrary#getBestDenominations(String, double)
	 *
	 * @param currency the currency to use
	 * @param numberOfCoins the number of coins that MUST be used
	 * @param absoluteValue the total value to get cloest to
	 * @return the denomination value that matches best
	 */
	public double getBestDenomination(String currency, int numberOfCoins, double absoluteValue);

	/**
	 * Given a currency and a total money value in that currency, this will return
	 * the denominations whose combination will produce the total value evenly.
	 *
	 * @see MoneyLibrary#getBestDenomination(String, double)
	 * @see MoneyLibrary#getBestDenomination(String, int, double)
	 *
	 * @param currency the currency to get denominations from
	 * @param absoluteValue the total value to parse out
	 * @return the set of denominations that will make up the value
	 */
	public double[] getBestDenominations(String currency, double absoluteValue);

	/**
	 * Given a currency, returns the string "Equal to" followed by the conversion
	 * of the given duration into the lowest denomination in the currency.  If the
	 * given denomination is already the lowest, it returns ""
	 *
	 * @param currency the currency to get a conversion for
	 * @param denomination the denomination to get a conversion to lowest for
	 * @return the conversion Equal to string
	 */
	public String getConvertableDescription(String currency, double denomination);

	/**
	 * Given a mob to get a currency, this will determine the denomination in
	 * that currency closest to the given total value, and return a string
	 * denoting that denomination and the amount, in x.xx form, of that
	 * denomination.  This is for display purposes only.
	 *
	 * @see MoneyLibrary#nameCurrencyLong(String, double)
	 * @see MoneyLibrary#nameCurrencyShort(String, double)
	 * @see MoneyLibrary#nameCurrencyShort(MOB, int)
	 *
	 * @param mob the mob from whom to get a currency to use
	 * @param absoluteValue the total value to show
	 * @return the close denomination with of a short form of the value
	 */
	public String nameCurrencyShort(MOB mob, double absoluteValue);

	/**
	 * Given a mob to get a currency, this will determine the denomination in
	 * that currency closest to the given total value, and return a string
	 * denoting that denomination and the amount, in x.xx form, of that
	 * denomination.  This is for display purposes only.
	 *
	 * @see MoneyLibrary#nameCurrencyLong(String, double)
	 * @see MoneyLibrary#nameCurrencyShort(String, double)
	 * @see MoneyLibrary#nameCurrencyShort(MOB, double)
	 *
	 * @param mob the mob from which to get the currency to use
	 * @param absoluteValue the total value to show
	 * @return the close denomination with of a short form of the value
	 */
	public String nameCurrencyShort(MOB mob, int absoluteValue);

	/**
	 * Given a particular currency, this will determine the denomination in
	 * that currency closest to the given total value, and return a string
	 * denoting that denomination and the amount, in x.xx form, of that
	 * denomination.  This is for display purposes only.
	 *
	 * @see MoneyLibrary#nameCurrencyLong(String, double)
	 * @see MoneyLibrary#nameCurrencyShort(MOB, double)
	 * @see MoneyLibrary#nameCurrencyShort(MOB, int)
	 *
	 * @param currency the currency to use
	 * @param absoluteValue the total value to show
	 * @return the close denomination with of a short form of the value
	 */
	public String nameCurrencyShort(String currency, double absoluteValue);

	/**
	 * Given a mob whose currency to use, this will split the given
	 * amount of value into denominations of that currency and
	 * return a string with the number and denoms, comma delimited,
	 * necessary to produce the value.
	 *
	 * @see MoneyLibrary#nameCurrencyLong(MOB, int)
	 * @see MoneyLibrary#nameCurrencyLong(String, double)
	 * @see MoneyLibrary#nameCurrencyShort(String, double)
	 *
	 * @param mob the mob whose currency to use
	 * @param absoluteValue the total value of the money
	 * @return the long form of all the denominations to make up the value
	 */
	public String nameCurrencyLong(MOB mob, double absoluteValue);

	/**
	 * Given a mob whose currency to use, this will split the given
	 * amount of value into denominations of that currency and
	 * return a string with the number and denoms, comma delimited,
	 * necessary to produce the value.
	 *
	 * @see MoneyLibrary#nameCurrencyLong(MOB, double)
	 * @see MoneyLibrary#nameCurrencyLong(String, double)
	 * @see MoneyLibrary#nameCurrencyShort(String, double)
	 *
	 * @param mob the mob whose currency to use
	 * @param absoluteValue the total value of the money
	 * @return the long form of all the denominations to make up the value
	 */
	public String nameCurrencyLong(MOB mob, int absoluteValue);

	/**
	 * Given a currency, this will split the given amount of value into
	 * denominations and return a string with the number and denoms,
	 * comma delimited, necessary to produce the value.
	 *
	 * @see MoneyLibrary#nameCurrencyLong(MOB, double)
	 * @see MoneyLibrary#nameCurrencyLong(MOB, int)
	 * @see MoneyLibrary#nameCurrencyShort(String, double)
	 *
	 * @param currency the currency to use
	 * @param absoluteValue the total value of the money
	 * @return the long form of all the denominations to make up the value
	 */
	public String nameCurrencyLong(String currency, double absoluteValue);

	/**
	 * Given a mob to derive a currency from, this will find the denomination
	 * in that currency which is capable to generating a stack of money closest
	 * to the given value. It will then give that money to the given owner
	 * in the given container.
	 *
	 * @see MoneyLibrary#makeBestCurrency(MOB, double)
	 * @see MoneyLibrary#makeBestCurrency(String, double)
	 * @see MoneyLibrary#makeBestCurrency(String, double, ItemCollection, Container)
	 * @see MoneyLibrary#makeAllCurrency(String, double)
	 * @see MoneyLibrary#makeCurrency(String, double, long)
	 *
	 * @param mob the mob from whom to get a currency to use
	 * @param absoluteValue the amount to approximate
	 * @param owner the new owner of the returned item
	 * @param container the container to put the item in
	 * @return the stack of currency whose value is closest to the value
	 */
	public Coins makeBestCurrency(MOB mob,  double absoluteValue, ItemCollection owner, Container container);

	/**
	 * Given a currency type, this will find the denomination in that currency which
	 * is capable to generating a stack of money closest to the given value.
	 * It will then give that money to the given owner in the given container.
	 *
	 * @see MoneyLibrary#makeBestCurrency(MOB, double)
	 * @see MoneyLibrary#makeBestCurrency(String, double)
	 * @see MoneyLibrary#makeBestCurrency(MOB, double, ItemCollection, Container)
	 * @see MoneyLibrary#makeAllCurrency(String, double)
	 * @see MoneyLibrary#makeCurrency(String, double, long)
	 *
	 * @param currency the currency to use
	 * @param absoluteValue the amount to approximate
	 * @param owner the new owner of the returned item
	 * @param container the container to put the item in
	 * @return the stack of currency whose value is closest to the value
	 */
	public Coins makeBestCurrency(String currency,  double absoluteValue, ItemCollection owner, Container container);

	/**
	 * Given a mob to derive a currency from, this will find the denomination
	 * in that currency which is capable to generating a stack of money closest
	 * to the given value.
	 *
	 * @see MoneyLibrary#makeBestCurrency(String, double)
	 * @see MoneyLibrary#makeBestCurrency(MOB, double, ItemCollection, Container)
	 * @see MoneyLibrary#makeBestCurrency(String, double, ItemCollection, Container)
	 * @see MoneyLibrary#makeAllCurrency(String, double)
	 * @see MoneyLibrary#makeCurrency(String, double, long)
	 *
	 * @param mob the mob to get the native currency from
	 * @param absoluteValue the amount to approximate
	 * @return the stack of currency whose value is closest to the value
	 */
	public Coins makeBestCurrency(MOB mob, double absoluteValue);

	/**
	 * Given a currency type, this will find the denomination in that currency which
	 * is capable to generating a stack of money closest to the given value.
	 *
	 * @see MoneyLibrary#makeBestCurrency(MOB, double)
	 * @see MoneyLibrary#makeBestCurrency(MOB, double, ItemCollection, Container)
	 * @see MoneyLibrary#makeBestCurrency(String, double, ItemCollection, Container)
	 * @see MoneyLibrary#makeAllCurrency(String, double)
	 * @see MoneyLibrary#makeCurrency(String, double, long)
	 *
	 * @param currency the currency to use
	 * @param absoluteValue the amount to approximate
	 * @return the stack of currency whose value is closest to the value
	 */
	public Coins makeBestCurrency(String currency, double absoluteValue);

	/**
	 * Generates an individual currency item of the given type, denomination, and
	 * number, as an item stack of currency item.
	 *
	 * @see MoneyLibrary#makeAllCurrency(String, double)
	 * @see MoneyLibrary#makeBestCurrency(String, double)
	 *
	 * @param currency the currency of the money
	 * @param denomination the denomination of that money
	 * @param numberOfCoins the number of coins of that denomination in the stack
	 * @return the currency item representing the currency stack
	 */
	public Coins makeCurrency(String currency, double denomination, long numberOfCoins);

	/**
	 * This great workhorse generates the individual denomination coins necessary
	 * to properly represent the given absolute value in the given currency.
	 *
	 * @see MoneyLibrary#makeCurrency(String, double, long)
	 * @see MoneyLibrary#makeBestCurrency(String, double)
	 *
	 * @param currency the currency to make the money in
	 * @param absoluteValue the absolute value of all the money to make combined
	 * @return individual denomination coin items that add up to the value
	 */
	public List<Coins> makeAllCurrency(String currency, double absoluteValue);

	/**
	 * Adds the given amount of money to the given holders items in their native currency.
	 *
	 * @see MoneyLibrary#addMoney(ItemCollection, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, int)
	 *
	 * @param IP the holder to have more money
	 * @param deltaValue the amount of total value to add
	 */
	public void addMoney(ItemCollection IP, int deltaValue);

	/**
	 * Adds the given amount of money to the given holders items in their native currency.
	 *
	 * @see MoneyLibrary#addMoney(ItemCollection, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, int)
	 *
	 * @param IP the mob to have more money
	 * @param deltaValue the amount of total value to add
	 */
	public void addMoney(ItemCollection IP, double deltaValue);

	/**
	 * Adds the given amount of money, in the given currency, to the given holders items.
	 *
	 * @see MoneyLibrary#addMoney(ItemCollection, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, int)
	 *
	 * @param IP the mob to have more money
	 * @param currency the currency of the money to make
	 * @param deltaValue the amount of total value to add
	 */
	public void addMoney(ItemCollection IP, String currency, int deltaValue);

	/**
	 * Adds the given amount of money, in the given currency, to the given holders items.
	 *
	 * @see MoneyLibrary#addMoney(ItemCollection, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, int)
	 *
	 * @param IP the mob to have more money
	 * @param currency the currency of the money to make
	 * @param deltaValue the amount of total value to add
	 */
	public void addMoney(ItemCollection IP, String currency, double deltaValue);

	/**
	 * Adds the given amount of money, in the given currency, to the given holders items,
	 * in the given container.
	 *
	 * @see MoneyLibrary#addMoney(ItemCollection, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, double)
	 *
	 * @param IP the mob to have more money
	 * @param container null, or the container to put the money in
	 * @param currency the currency of the money to make
	 * @param deltaValue the amount of total value to add
	 */
	public void addMoney(ItemCollection IP, Container container, String currency, int deltaValue);

	/**
	 * Adds the given amount of money, in the given currency, to the given holders items,
	 * in the given container.
	 *
	 * @see MoneyLibrary#addMoney(ItemCollection, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#addMoney(ItemCollection, String, int)
	 * @see MoneyLibrary#addMoney(ItemCollection, Container, String, int)
	 *
	 * @param IP the mob to have more money
	 * @param container null, or the container to put the money in
	 * @param currency the currency of the money to make
	 * @param deltaValue the amount of total value to add
	 */
	public void addMoney(ItemCollection IP, Container container, String currency, double deltaValue);

	/**
	 * Generates a visible message of the given recipient receiving the given amount
	 * of money in their native currency from themselves.  Yea, it's weird.
	 *
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, MOB, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, String, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, MOB, String, double)
	 *
	 * @param recipient the recipient of the money
	 * @param absoluteValue the absolute amount of the money to give
	 */
	public void giveSomeoneMoney(MOB recipient, double absoluteValue);

	/**
	 * Generates a visible message of the given recipient receiving the given amount
	 * of money in the given currency from themselves.  Yea, it's weird.
	 *
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, MOB, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, MOB, String, double)
	 *
	 * @param recipient the recipient of the money
	 * @param currency the currency the money is in
	 * @param absoluteValue the absolute amount of the money to give
	 */
	public void giveSomeoneMoney(MOB recipient, String currency, double absoluteValue);

	/**
	 * Generates a visible message of the given recipient receiving the given amount
	 * of money in the banker's currency from the given banker/giver.  The money is created,
	 * given to the banker, who gives it to the recipient.
	 *
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, String, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, MOB, String, double)
	 *
	 * @param banker the giver of the money
	 * @param customer the recipient of the money
	 * @param absoluteValue the absolute amount of the money to give
	 */
	public void giveSomeoneMoney(MOB banker, MOB customer, double absoluteValue);

	/**
	 * Generates a visible message of the given recipient receiving the given amount
	 * of money in the given currency from the given banker/giver.  The money is created,
	 * given to the banker, who gives it to the recipient.
	 *
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, MOB, double)
	 * @see MoneyLibrary#giveSomeoneMoney(MOB, String, double)
	 *
	 * @param banker the giver of the money
	 * @param customer the recipient of the money
	 * @param currency the currency the money is in
	 * @param absoluteValue the absolute amount of the money to give
	 */
	public void giveSomeoneMoney(MOB banker, MOB customer, String currency, double absoluteValue);

	/**
	 * Adds a new record to the bank account ledger, which is an accounting of deposits
	 * and withdrawls.
	 *
	 * @see MoneyLibrary#getBankAccountChains(String)
	 * @see MoneyLibrary#getBankBalance(String, String, String)
	 * @see MoneyLibrary#modifyBankGold(String, String, String, String, double)
	 * @see MoneyLibrary#modifyThisAreaBankGold(Area, Set, String, String, double)
	 * @see MoneyLibrary#modifyLocalBankGold(Area, String, String, double)
	 * @see MoneyLibrary#getBankChainCurrency(String)
	 *
	 * @param bankName the bank chain name
	 * @param owner the account owner name (a player usually)
	 * @param explanation a brief explanation of what happened
	 */
	public void addToBankLedger(String bankName, String owner, String explanation);

	/**
	 * Returns the set of all bank chains that have accounts for the
	 * given bank account owner.
	 *
	 * @see MoneyLibrary#addToBankLedger(String, String, String)
	 * @see MoneyLibrary#getBankBalance(String, String, String)
	 * @see MoneyLibrary#modifyBankGold(String, String, String, String, double)
	 * @see MoneyLibrary#modifyThisAreaBankGold(Area, Set, String, String, double)
	 * @see MoneyLibrary#modifyLocalBankGold(Area, String, String, double)
	 * @see MoneyLibrary#getBankChainCurrency(String)
	 *
	 * @param owner the bank account owner, typically a player
	 * @return a set of all bank chains found, could be empty
	 */
	public Set<String> getBankAccountChains(final String owner);

	/**
	 * For the given bank chain and given bank account owner name, and optionally a
	 * currency, this will return the found currency and bank balance.
	 *
	 * @see MoneyLibrary#addToBankLedger(String, String, String)
	 * @see MoneyLibrary#getBankAccountChains(String)
	 * @see MoneyLibrary#modifyBankGold(String, String, String, String, double)
	 * @see MoneyLibrary#modifyThisAreaBankGold(Area, Set, String, String, double)
	 * @see MoneyLibrary#modifyLocalBankGold(Area, String, String, double)
	 * @see MoneyLibrary#getBankChainCurrency(String)
	 *
	 * @param bankName the bank chain name
	 * @param owner the account owner name (usually a player)
	 * @param optionalCurrency null, or a currency to ensure is returned
	 * @return NULL, or the currency and total amount of the bank balance
	 */
	public Pair<String,Double> getBankBalance(final String bankName, final String owner, final String optionalCurrency);

	/**
	 * Modifies the amount of money in the bank account of the given
	 * account name owner and the given bank name.
	 *
	 * @see MoneyLibrary#addToBankLedger(String, String, String)
	 * @see MoneyLibrary#getBankAccountChains(String)
	 * @see MoneyLibrary#getBankBalance(String, String, String)
	 * @see MoneyLibrary#modifyThisAreaBankGold(Area, Set, String, String, double)
	 * @see MoneyLibrary#modifyLocalBankGold(Area, String, String, double)
	 * @see MoneyLibrary#getBankChainCurrency(String)
	 *
	 * @param bankName the name of the bank chain
	 * @param owner the account name (player) to alter money in
	 * @param explanation the reason for the change, for the ledger
	 * @param currency the currency to use
	 * @param deltaAmount an amount to change local money by
	 * @return true if money was successfully changed at a chain here
	 */
	public boolean modifyBankGold(String bankName,  String owner, String explanation, String currency, double deltaAmount);

	/**
	 * Loops through all bank chains in the given area and attempts to
	 * find an account for the given account name owner and modify the
	 * amount of money in the bank account.
	 *
	 * @see MoneyLibrary#addToBankLedger(String, String, String)
	 * @see MoneyLibrary#getBankAccountChains(String)
	 * @see MoneyLibrary#getBankBalance(String, String, String)
	 * @see MoneyLibrary#modifyBankGold(String, String, String, String, double)
	 * @see MoneyLibrary#modifyLocalBankGold(Area, String, String, double)
	 * @see MoneyLibrary#getBankChainCurrency(String)
	 *
	 * @param A null or the area to find chains in
	 * @param triedBanks set of bank chains already tried (don't try again)
	 * @param owner the account name (player) to alter money in
	 * @param explanation the reason for the change, for the ledger
	 * @param deltaAmount an amount to change local money by
	 * @return true if money was successfully changed at a chain here
	 */
	public boolean modifyThisAreaBankGold(Area A,  Set<String> triedBanks, String owner, String explanation, double deltaAmount);

	/**
	 * Starting with the given area, and proceeding to parent areas,
	 * this will attempt to alter the amount of money in any bank chains
	 * account associated with the given account name owner.  If no chains
	 * associated with the area are found, it will end up just taking any
	 * chain it can find.
	 *
	 * @see MoneyLibrary#addToBankLedger(String, String, String)
	 * @see MoneyLibrary#getBankAccountChains(String)
	 * @see MoneyLibrary#getBankBalance(String, String, String)
	 * @see MoneyLibrary#modifyBankGold(String, String, String, String, double)
	 * @see MoneyLibrary#modifyThisAreaBankGold(Area, Set, String, String, double)
	 * @see MoneyLibrary#getBankChainCurrency(String)
	 *
	 * @param A the area to start finding chains in
	 * @param owner the account name (player) to alter money in
	 * @param explanation the reason for the change, for the ledger
	 * @param deltaAmount an amount to change local money by
	 * @return true if money was successfully changed somewhere
	 */
	public boolean modifyLocalBankGold(Area A, String owner, String explanation, double deltaAmount);

	/**
	 * Given a bank chain, which may deal in many currencies due to having
	 * branches in many areas, this method will return the chains most
	 * popular currency.
	 *
	 * @see MoneyLibrary#addToBankLedger(String, String, String)
	 * @see MoneyLibrary#getBankAccountChains(String)
	 * @see MoneyLibrary#getBankBalance(String, String, String)
	 * @see MoneyLibrary#modifyBankGold(String, String, String, String, double)
	 * @see MoneyLibrary#modifyThisAreaBankGold(Area, Set, String, String, double)
	 * @see MoneyLibrary#modifyLocalBankGold(Area, String, String, double)
	 *
	 * @param bankChain the bank chain name
	 * @return null, or a currency
	 */
	public String getBankChainCurrency(final String bankChain);

	/**
	 * This strange method takes away all the money from the given mob, of the bankers
	 * native currency, and then has the given banker hand money back to the mob equal
	 * to the total money minus the given amount.
	 *
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, int)
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, double)
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, String, double)
	 *
	 * @param banker the banker who gives change
	 * @param mob the mob losing their money, but maybe getting some back
	 * @param positiveDeltaAmount the amount to NOT give back
	 */
	public void subtractMoneyGiveChange(MOB banker, MOB mob, int positiveDeltaAmount);

	/**
	 * This strange method takes away all the money from the given mob, of the bankers
	 * native currency, and then has the given banker hand money back to the mob equal
	 * to the total money minus the given amount.
	 *
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, int)
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, double)
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, String, double)
	 *
	 * @param banker the banker who gives change
	 * @param mob the mob losing their money, but maybe getting some back
	 * @param positiveDeltaAmount the amount to NOT give back
	 */
	public void subtractMoneyGiveChange(MOB banker, MOB mob, double positiveDeltaAmount);

	/**
	 * This strange method takes away all the money from the given mob, of the given
	 * currency, and then has the given banker hand money back to the mob equal to the
	 * total money minus the given amount.
	 *
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, int)
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, double)
	 * @see MoneyLibrary#subtractMoneyGiveChange(MOB, MOB, String, double)
	 *
	 * @param banker the banker who gives change
	 * @param mob the mob losing their money, but maybe getting some back
	 * @param currency the currency of the money to lose
	 * @param positiveDeltaAmount the amount to NOT give back
	 */
	public void subtractMoneyGiveChange(MOB banker, MOB mob, String currency, double positiveDeltaAmount);

	/**
	 * Removes the given total amount of money from the given mob/room.
	 * This deals in currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, Container, String, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double, double)
	 *
	 * @param IP the mob/room/item holder losing money
	 * @param positiveDeltaAmount the total value to remove
	 */
	public void subtractMoney(ItemCollection IP, double positiveDeltaAmount);

	/**
	 * Removes the given total amount of money from the given IP, in the given
	 * currency.  This deals in currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, Container, String, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double, double)
	 *
	 * @param IP the mob/room/item holder losing money
	 * @param currency the type of currency to remove
	 * @param positiveDeltaAmount the total value to remove
	 */
	public void subtractMoney(ItemCollection IP, String currency, double positiveDeltaAmount);

	/**
	 * Removes the given total amount of money from the given IP, in the given
	 * currency and the given container of that currency.  This deals in
	 * currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double, double)
	 *
	 * @param IP the mob/room/item holder losing money
	 * @param container null, or the container with the money in it
	 * @param currency the type of currency to remove
	 * @param positiveDeltaAmount the total value to remove
	 */
	public void subtractMoney(ItemCollection IP, Container container, String currency, double positiveDeltaAmount);

	/**
	 * Removes the given total amount of money from the given IP, in their native
	 * currency and the given denomination of that currency.  This deals in
	 * currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, Container, String, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double, double)
	 *
	 * @param IP the mob/room/item holder losing money
	 * @param denomination the denomination of the currency to remove
	 * @param positiveDeltaAmount the total value to remove
	 */
	public void subtractMoney(ItemCollection IP, double denomination, double positiveDeltaAmount);

	/**
	 * Removes the given total amount of money from the given IP, in the given
	 * currency and the given denomination of that currency.
	 * This deals in currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, double, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, String, double)
	 * @see MoneyLibrary#subtractMoney(ItemCollection, Container, String, double)
	 *
	 * @param IP the mob/room/item holder losing money
	 * @param currency the type of currency to remove
	 * @param denomination the denomination of the currency to remove
	 * @param positiveDeltaAmount the total value to remove
	 */
	public void subtractMoney(ItemCollection IP, String currency, double denomination, double positiveDeltaAmount);

	/**
	 * When a Coins item is dropped into a new collection,
	 * this method will seek out a similar denominated pile
	 * and update it, thus destroying the given coins, and
	 * return true, otherwise it does nothing and returns false.
	 *
	 * @param C the coins already dropped
	 * @param coll the collection that might have a dup
	 * @return true if the coins were destroyed, false otherwise
	 */
	public boolean putCoinsBack(Coins C, ItemCollection coll);


	/**
	 * If the given mob/room is an npc with native parameter-value
	 * money set, this will return that value.  Otherwise, the total
	 * absolute value of all currency items on the given mob are
	 * counted up, rounded to an int, and returned.
	 *
	 * @param mob the mob whose money to get
	 * @return the amount of money in absolute value
	 */
	public int getMoney(MOB mob);

	/**
	 * Sets the total amount of money the given mob has in their native
	 * currency to the given absolute value.
	 *
	 * @see MoneyLibrary#setMoney(MOB, int)
	 * @see MoneyLibrary#setMoney(MOB, String, double)
	 *
	 * @param mob the mob to have their money set
	 * @param absoluteAmount the amount for the mob to have
	 */
	public void setMoney(MOB mob, double absoluteAmount);

	/**
	 * Sets the total amount of money the given mob has in the given
	 * currency to the given absolute value.
	 *
	 * @see MoneyLibrary#setMoney(MOB, double)
	 * @see MoneyLibrary#setMoney(MOB, int)
	 *
	 * @param mob the mob to have their money set
	 * @param currency the required currency to set
	 * @param absoluteAmount the amount for the mob to have
	 */
	public void setMoney(MOB mob, String currency, double absoluteAmount);

	/**
	 * Called only on npcs saved to the database, and primarily for legacy
	 * use, this method destroys any money items and sets the npc money
	 * property to the given absolute value.  The currency is determined
	 * by the start room of the npc.
	 *
	 * @see MoneyLibrary#setMoney(MOB, double)
	 * @see MoneyLibrary#setMoney(MOB, String, double)
	 *
	 * @param mob the npc to have their money set
	 * @param amount the amount of their currency to set it to
	 */
	public void setMoney(MOB mob, int amount);

	/**
	 * Removes all money from the given mob, item and
	 * npc numeric of the given currency.
	 *
	 * @see MoneyLibrary#clearInventoryMoney(MOB, String)
	 *
	 * @param mob the mob to make poor
	 * @param currency null, or the currency items to kill
	 */
	public void clearZeroMoney(MOB mob, String currency);

	/**
	 * Removes all item money from the given mob, of
	 * the given currency.
	 *
	 * @see MoneyLibrary#clearZeroMoney(MOB, String)
	 *
	 * @param mob the mob to make poor
	 * @param currency null, or the currency to kill
	 */
	public void clearInventoryMoney(MOB mob, String currency);

	/**
	 * Adds the amount of money items to the given room, in the given container, of the
	 * given currency, and the given total absolute value.
	 *
	 * @see MoneyLibrary#removeMoney(Room, Container, String, double)
	 *
	 * @param R the room to add value to
	 * @param container null, or the container the money must be put in
	 * @param currency the currency of the value to add
	 * @param absoluteValue the total value of the currency items to add
	 */
	public void dropMoney(Room R, Container container, String currency, double absoluteValue);

	/**
	 * Removes the amount of money items from the given room, in the given container, of the
	 * given currency, and the given total absolute value.  It does this by just deleting
	 * all the relevant currency and re-adding what wasn't removed.
	 *
	 * @see MoneyLibrary#dropMoney(Room, Container, String, double)
	 *
	 * @param R the room to remove value from
	 * @param container null, or the container the money must be in
	 * @param currency null, or the currency of the value to remove
	 * @param absoluteValue the total value of the currency items to remove
	 */
	public void removeMoney(Room R, Container container, String currency, double absoluteValue);

	/**
	/**
	 * Scans the given mob and returns any money items in the given
	 * currency and NOT in a container.
	 *
	 * @see MoneyLibrary#getMoney(MOB)
	 * @see MoneyLibrary#getMoneyItems(ItemCollection, Item, String)
	 *
	 * @param IP the mob/room to scan
	 * @param currency null, or the currency the money must be in
	 * @return a list of the coin items found
	 */
	public List<Coins> getMoneyItems(ItemCollection IP, String currency);

	/**
	 * Scans the given mob/room and returns any money items in the given
	 * currency and given container.
	 *
	 * @see MoneyLibrary#getMoney(MOB)
	 * @see MoneyLibrary#getMoneyItems(ItemCollection, String)
	 *
	 * @param IP the mob/room to scan
	 * @param container null, or the container the money must be in
	 * @param currency null, or the currency the money must be in
	 * @return a list of the coin items found
	 */
	public List<Coins> getMoneyItems(ItemCollection IP, Item container, String currency);

	/**
	 * Because a currency code could include an entire definition, or maybe
	 * just the name/code itself, this method exists to resolve those
	 * differences and compare just the codes
	 *
	 * @param curr1 the first currency code or definition
	 * @param curr2 the second currency code or definition
	 * @return true if they are the same, false otherwise
	 */
	public boolean isCurrencyMatch(final String curr1, final String curr2);

	/**
	 * Returns the currency code/name most applicable to the given object.
	 * This could be a mob, shopkeeper, room, or area.
	 *
	 * @param E the object to find a currency for
	 * @return the currency code/name
	 */
	public String getCurrency(CMObject E);

	/**
	 * Returns the number of coins that the given mob has in the given currency
	 * and the given denomination.
	 *
	 * @param mob the mob to count the money of
	 * @param currency null for all, or the currency of the money to count
	 * @param denomination the denomination of the money to count
	 * @return the number of coins found
	 */
	public long getNumberOfCoins(MOB mob, String currency, double denomination);

	/**
	 * Returns the accumulated total value of the money of the given
	 * currency, in the given container, on the given mob/room.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(ItemCollection)
	 *
	 * @param IP the money holder to count the money of
	 * @param container null, or the container that the currency must be in
	 * @param currency null for all, or the current type to filter the money through
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteValue(ItemCollection IP, Item container, String currency);

	/**
	 * Returns the accumulated total value of the money of the given
	 * currency, on the given mob/room.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(ItemCollection)
	 *
	 * @param IP the money holder to count the money of
	 * @param currency null for all, or the current type to filter the money through
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteValue(ItemCollection IP, String currency);

	/**
	 * Returns the accumulated total value of the money on the given
	 * mob, in that mobs native currency.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(ItemCollection)
	 *
	 * @param mob the mob to count the money of
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteNativeValue(MOB mob);

	/**
	 * Returns the accumulated total value of the money on the given
	 * mob, in the given shopkeepers native currency.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(ItemCollection)
	 *
	 * @param mob the mob to count the money of
	 * @param shopkeeper the shopkeeper to get the currency type from
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteShopKeepersValue(MOB mob, MOB shopkeeper);

	/**
	 * Returns the accumulated total value of the money on the given
	 * holder, counting all currencies.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(ItemCollection, Item, String)
	 *
	 * @param IP the holder to count the money of
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteValueAllCurrencies(ItemCollection IP);

	/**
	 * Returns all debt records owed by the given debtor.
	 *
	 * @see MoneyLibrary.DebtItem
	 * @see MoneyLibrary#getDebt(String, String)
	 * @see MoneyLibrary#getDebtOwed(String)
	 * @see MoneyLibrary#getDebtOwed(String, String)
	 * @see MoneyLibrary#adjustDebt(String, String, double, String, double, long)
	 * @see MoneyLibrary#delAllDebt(String, String)
	 *
	 * @param name the debtor, usually a player name
	 * @return all debt records
	 */
	public List<DebtItem> getDebt(String name);

	/**
	 * Returns all debt records between the given debtor and
	 * the given lender.  There could be multiple because
	 * they could have different due dates.
	 *
	 * @see MoneyLibrary.DebtItem
	 * @see MoneyLibrary#getDebt(String)
	 * @see MoneyLibrary#getDebtOwed(String, String)
	 * @see MoneyLibrary#getDebtOwed(String)
	 * @see MoneyLibrary#adjustDebt(String, String, double, String, double, long)
	 * @see MoneyLibrary#delAllDebt(String, String)
	 *
	 * @param name the debtor, usually a player name
	 * @param owedTo the money lender, usually a bank chain
	 * @return all debt records
	 */
	public List<DebtItem> getDebt(String name, String owedTo);

	/**
	 * Returns all debt records owed to the given lender.
	 *
	 * @see MoneyLibrary.DebtItem
	 * @see MoneyLibrary#getDebt(String)
	 * @see MoneyLibrary#getDebt(String, String)
	 * @see MoneyLibrary#getDebtOwed(String, String)
	 * @see MoneyLibrary#adjustDebt(String, String, double, String, double, long)
	 * @see MoneyLibrary#delAllDebt(String, String)
	 *
	 * @param owedTo the money lender, usually a bank chain
	 * @return all debt records
	 */
	public List<DebtItem> getDebtOwed(String owedTo);

	/**
	 * Returns total debt amount owed between the given debtor and
	 * the given lender.  There could be multiple because
	 * they could have different due dates.
	 *
	 * @see MoneyLibrary#getDebt(String)
	 * @see MoneyLibrary#getDebt(String, String)
	 * @see MoneyLibrary#getDebtOwed(String)
	 * @see MoneyLibrary#adjustDebt(String, String, double, String, double, long)
	 * @see MoneyLibrary#delAllDebt(String, String)
	 *
	 * @param name the debtor, usually a player name
	 * @param owedTo the money lender, usually a bank chain
	 * @return total amount owed, in base currency
	 */
	public double getDebtOwed(String name, String owedTo);

	/**
	 * Adds or alters a debt record between the given debtor,
	 * and lender.
	 *
	 * @see MoneyLibrary#getDebt(String)
	 * @see MoneyLibrary#getDebt(String, String)
	 * @see MoneyLibrary#getDebtOwed(String)
	 * @see MoneyLibrary#getDebtOwed(String, String)
	 * @see MoneyLibrary#delAllDebt(String, String)
	 *
	 * @param name the debtor, usually a player name
	 * @param owedTo the money lender, usually a bank chain
	 * @param adjustAmt the amount to add/remove to the debt
	 * @param reason the short debt description, like "Bank Loan"
	 * @param interest the interest rate to charge on new loans/mudmonth
	 * @param due the real life due datestamp in milliseconds
	 */
	public void adjustDebt(String name, String owedTo, double adjustAmt, String reason, double interest, long due);

	/**
	 * Deletes a particular debt record.
	 *
	 * @see MoneyLibrary#getDebt(String)
	 * @see MoneyLibrary#getDebt(String, String)
	 * @see MoneyLibrary#getDebtOwed(String)
	 * @see MoneyLibrary#getDebtOwed(String, String)
	 * @see MoneyLibrary#adjustDebt(String, String, double, String, double, long)
	 *
	 * @param name the debtor, usually a player name
	 * @param owedTo the money lender, usually a bank chain
	 */
	public void delAllDebt(String name, String owedTo);

	/**
	 * Interface for an object referencing an entire
	 * currency system, including all of its denominations.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface MoneyDefinition
	{
		/**
		 * The unique code ID for this currency.
		 *
		 * @return the unique code ID for this currency.
		 */
		public String ID();

		/**
		 * Whether this is a tradable currency.
		 * Non-tradable currencies are like victory points,
		 * which are tracked, but not exchangable with
		 * money changers.
		 *
		 * @return whether this is a tradable currency
		 */
		public boolean canTrade();

		/**
		 * Array of all denominations that apply to this
		 * currency.
		 *
		 * @return all denominations
		 */
		public MoneyDenomination[] denominations();
	}

	/**
	 * Interface for an object referencing a single
	 * denomination of a specific currency.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface MoneyDenomination
	{
		/**
		 * The value of this denomination in absolute
		 * relative multiplier.
		 *
		 * @return the value of this denomination
		 */
		public double value();

		/**
		 * Normal name of the denomination when displayed as
		 * a sentence string to the user.  This should still be
		 * short, but can be words like "dollar" or "gold coin"
		 *
		 * @return normal name of the denomination
		 */
		public String name();

		/**
		 * Short display abbreviation of the denomination when
		 * shown in shopkeeper lists.  Should be no more
		 * than one or two characters.
		 *
		 * @return short display abbreviation of the denomination
		 */
		public String abbr();
	}

	/**
	 * Interface for an object referencing a debt
	 * from one to another, denominated in base
	 * currency, and carrying a due date and interest rate.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface DebtItem
	{
		/**
		 * Gets the name of the player that owes the money
		 * Some day this should be expanded to support the
		 * names of clans.
		 *
		 * @return the name of the player that owes the money
		 */
		public String debtor();

		/**
		 * Gets who the money is owed to, typically a bank chain
		 * name.
		 *
		 * @return who the money is owed to
		 */
		public String owedTo();

		/**
		 * The amount, in base currency value, that is currently
		 * owed.  All interest is included.  The currency type
		 * comes from who it is owed to.
		 *
		 * @return The amount, in base currency value, owed
		 */
		public double amt();

		/**
		 * Alters the amount, in base currency value, that is currently
		 * owed.  All interest is included.  The currency type
		 * comes from who it is owed to.
		 *
		 * @param amt The amount, in base currency value, owed
		 */
		public void setAmt(double amt);

		/**
		 * The real-life due date of the loan, after which the
		 * collateral (property) is declared unowned.
		 *
		 * @return the real-life due date of the loan
		 */
		public long due();

		/**
		 * Gets the percentage of the current amount due that is
		 * added back into the total every mud-month.  The value
		 * is from 0-1.
		 *
		 * @return the percentage of the current amount due from 0-1
		 */
		public double interest();

		/**
		 * Gets a short friendly description of the nature of the debt,
		 * usually something like "bank loan".
		 *
		 * @return a short friendly description of the nature of the debt
		 */
		public String reason();
	}
}
