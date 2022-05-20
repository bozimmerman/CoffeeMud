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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/*
   Copyright 2005-2022 Bo Zimmerman

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
public interface MoneyLibrary extends CMLibrary
{
	public void unloadCurrencySet(String currency);
	public MoneyDefinition createCurrencySet(String currency);
	public MoneyDefinition getCurrencySet(String currency);
	public List<String> getAllCurrencies();

	public double abbreviatedRePrice(MOB shopkeeper, double absoluteAmount);
	public double abbreviatedRePrice(String currency, double absoluteAmount);
	public String abbreviatedPrice(MOB shopkeeper, double absoluteAmount);
	public String abbreviatedPrice(String currency, double absoluteAmount);

	public int getDenominationIndex(String currency, double value);
	public List<String> getDenominationNameSet(String currency);
	public double getLowestDenomination(String currency);

	public double lowestAbbreviatedDenomination(String currency);
	public double lowestAbbreviatedDenomination(String currency, double absoluteAmount);

	public String getDenominationShortCode(String currency, double denomination);

	public String getDenominationName(String currency);
	public String getDenominationName(String currency,  double denomination, long number);
	public String getDenominationName(String currency, double denomination);
	public String getDenominationName(final MOB mob, double denomination);

	public double getBestDenomination(String currency, double absoluteValue);
	public double getBestDenomination(String currency, int numberOfCoins, double absoluteValue);
	public double[] getBestDenominations(String currency, double absoluteValue);

	public String getConvertableDescription(String currency, double denomination);
	public String nameCurrencyShort(MOB mob, double absoluteValue);
	public String nameCurrencyShort(MOB mob, int absoluteValue);
	public String nameCurrencyShort(String currency, double absoluteValue);
	public String nameCurrencyLong(MOB mob, double absoluteValue);
	public String nameCurrencyLong(MOB mob, int absoluteValue);
	public String nameCurrencyLong(String currency, double absoluteValue);

	public Coins makeBestCurrency(MOB mob,  double absoluteValue, Environmental owner, Container container);
	public Coins makeBestCurrency(String currency,  double absoluteValue, Environmental owner, Container container);
	public Coins makeBestCurrency(MOB mob, double absoluteValue);
	public Coins makeCurrency(String currency, double denomination, long numberOfCoins);
	public Coins makeBestCurrency(String currency, double absoluteValue);
	public List<Coins> makeAllCurrency(String currency, double absoluteValue);

	public void addMoney(MOB customer, int absoluteValue);
	public void addMoney(MOB customer, double absoluteValue);
	public void addMoney(MOB customer, String currency, int absoluteValue);
	public void addMoney(MOB mob, String currency, double absoluteValue);
	public void addMoney(MOB customer, Container container, String currency, int absoluteValue);
	public void addMoney(MOB mob, Container container, String currency, double absoluteValue);

	public void giveSomeoneMoney(MOB recipient, double absoluteValue);
	public void giveSomeoneMoney(MOB recipient, String currency, double absoluteValue);
	public void giveSomeoneMoney(MOB banker, MOB customer, double absoluteValue);
	public void giveSomeoneMoney(MOB banker, MOB customer, String currency, double absoluteValue);

	public void addToBankLedger(String bankName, String owner, String explanation);
	public Set<String> getBankAccountChains(final String owner);
	public Pair<String,Double> getBankBalance(final String bankName, final String owner, final String optionalCurrency);
	public boolean modifyBankGold(String bankName,  String owner, String explanation, String currency, double absoluteAmount);
	public boolean modifyThisAreaBankGold(Area A,  Set<String> triedBanks, String owner, String explanation, double absoluteAmount);
	public boolean modifyLocalBankGold(Area A, String owner, String explanation, double absoluteAmount);
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
	 * @param absoluteAmount the amount to NOT give back
	 */
	public void subtractMoneyGiveChange(MOB banker, MOB mob, int absoluteAmount);

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
	 * @param absoluteAmount the amount to NOT give back
	 */
	public void subtractMoneyGiveChange(MOB banker, MOB mob, double absoluteAmount);

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
	 * @param absoluteAmount the amount to NOT give back
	 */
	public void subtractMoneyGiveChange(MOB banker, MOB mob, String currency, double absoluteAmount);

	/**
	 * Removes the given total amount of money from the given mob.
	 * This deals in currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(MOB, double, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double)
	 * @see MoneyLibrary#subtractMoney(MOB, Container, String, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double, double)
	 *
	 * @param mob the mob losing money
	 * @param absoluteAmount the total value to remove
	 */
	public void subtractMoney(MOB mob, double absoluteAmount);

	/**
	 * Removes the given total amount of money from the given mob, in the given
	 * currency.  This deals in currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(MOB, double)
	 * @see MoneyLibrary#subtractMoney(MOB, double, double)
	 * @see MoneyLibrary#subtractMoney(MOB, Container, String, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double, double)
	 *
	 * @param mob the mob losing money
	 * @param currency the type of currency to remove
	 * @param absoluteAmount the total value to remove
	 */
	public void subtractMoney(MOB mob, String currency, double absoluteAmount);

	/**
	 * Removes the given total amount of money from the given mob, in the given
	 * currency and the given container of that currency.  This deals in
	 * currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(MOB, double)
	 * @see MoneyLibrary#subtractMoney(MOB, double, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double, double)
	 *
	 * @param mob the mob losing money
	 * @param container null, or the container with the money in it
	 * @param currency the type of currency to remove
	 * @param absoluteAmount the total value to remove
	 */
	public void subtractMoney(MOB mob, Container container, String currency, double absoluteAmount);

	/**
	 * Removes the given total amount of money from the given mob, in their native
	 * currency and the given denomination of that currency.  This deals in
	 * currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(MOB, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double)
	 * @see MoneyLibrary#subtractMoney(MOB, Container, String, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double, double)
	 *
	 * @param mob the mob losing money
	 * @param denomination the denomination of the currency to remove
	 * @param absoluteAmount the total value to remove
	 */
	public void subtractMoney(MOB mob, double denomination, double absoluteAmount);

	/**
	 * Removes the given total amount of money from the given mob, in the given currency and the given
	 * denomination of that currency.  This deals in currency items.
	 *
	 * @see MoneyLibrary#subtractMoney(MOB, double)
	 * @see MoneyLibrary#subtractMoney(MOB, double, double)
	 * @see MoneyLibrary#subtractMoney(MOB, String, double)
	 * @see MoneyLibrary#subtractMoney(MOB, Container, String, double)
	 *
	 * @param mob the mob losing money
	 * @param currency the type of currency to remove
	 * @param denomination the denomination of the currency to remove
	 * @param absoluteAmount the total value to remove
	 */
	public void subtractMoney(MOB mob, String currency, double denomination, double absoluteAmount);

	/**
	 * If the given mob is an npc with native parameter-value
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
	 * @see MoneyLibrary#getMoneyItems(Room, Item, String)
	 * @see MoneyLibrary#getMoneyItems(MOB, Item, String)
	 *
	 * @param mob the mob to scan
	 * @param currency null, or the currency the money must be in
	 * @return a list of the coin items found
	 */
	public List<Coins> getMoneyItems(MOB mob, String currency);

	/**
	 * Scans the given mob and returns any money items in the given
	 * currency and given container.
	 *
	 * @see MoneyLibrary#getMoney(MOB)
	 * @see MoneyLibrary#getMoneyItems(MOB, String)
	 * @see MoneyLibrary#getMoneyItems(Room, Item, String)
	 *
	 * @param mob the mob to scan
	 * @param container null, or the container the money must be in
	 * @param currency null, or the currency the money must be in
	 * @return a list of the coin items found
	 */
	public List<Coins> getMoneyItems(MOB mob, Item container, String currency);

	/**
	 * Scans the given room and returns any money items in the given
	 * currency and given container.
	 *
	 * @see MoneyLibrary#getMoney(MOB)
	 * @see MoneyLibrary#getMoneyItems(MOB, String)
	 * @see MoneyLibrary#getMoneyItems(MOB, Item, String)
	 *
	 * @param R the room to scan
	 * @param container null, or the container the money must be in
	 * @param currency null, or the currency the money must be in
	 * @return a list of the coin items found
	 */
	public List<Coins> getMoneyItems(Room R, Item container, String currency);

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
	public String getCurrency(Environmental E);

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
	 * currency, in the given container, in the given room.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(MOB)
	 *
	 * @param R  the room to count the money in
	 * @param container null, or the container that the currency must be in
	 * @param currency the current type to filter the money through
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteValue(Room R, Item container, String currency);

	/**
	 * Returns the accumulated total value of the money of the given
	 * currency, in the given container, on the given mob.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(Room, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(MOB)
	 *
	 * @param mob the mob to count the money of
	 * @param container null, or the container that the currency must be in
	 * @param currency null for all, or the current type to filter the money through
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteValue(MOB mob, Item container, String currency);

	/**
	 * Returns the accumulated total value of the money of the given
	 * currency, on the given mob.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(Room, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(MOB)
	 *
	 * @param mob the mob to count the money of
	 * @param currency null for all, or the current type to filter the money through
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteValue(MOB mob, String currency);

	/**
	 * Returns the accumulated total value of the money on the given
	 * mob, in that mobs native currency.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(Room, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(MOB)
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
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(Room, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValueAllCurrencies(MOB)
	 *
	 * @param mob the mob to count the money of
	 * @param shopkeeper the shopkeeper to get the currency type from
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteShopKeepersValue(MOB mob, MOB shopkeeper);

	/**
	 * Returns the accumulated total value of the money on the given
	 * mob, counting all currencies.
	 *
	 * @see MoneyLibrary#getTotalAbsoluteNativeValue(MOB)
	 * @see MoneyLibrary#getTotalAbsoluteShopKeepersValue(MOB, MOB)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(MOB, Item, String)
	 * @see MoneyLibrary#getTotalAbsoluteValue(Room, Item, String)
	 *
	 * @param mob the mob to count the money of
	 * @return the absolute money value
	 */
	public double getTotalAbsoluteValueAllCurrencies(MOB mob);

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
