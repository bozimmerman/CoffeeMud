package com.planet_ink.coffee_mud.MOBS.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.List;
import java.util.Vector;

/*
   Copyright 2003-2018 Bo Zimmerman

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
 * A Banker is a kind of shopkeeper that belongs to a "chain" which
 * shares access to a common store of player/clan accounts to hold money
 * and items.  Players must go to a banker in the same chain to retrieve 
 * money and items deposited with the chain.  Bankers respond to new
 * commands such as DEPOSIT and WITHDRAW that normal shopkeepers do not.
 * Bankers also respect marriage by giving both parters access to each
 * others accounts.
 * 
 * Bankers can serve an entire clan, or a single player.
 * 
 * @author Bo Zimmerman
 */
public interface Banker extends ShopKeeper
{
	/**
	 * The default value by which the value of items is divided to determine minimum money
	 * to continue item storage.  Default is needing 10% of item value in money to store.
	 */
	public final static double MIN_ITEM_BALANCE_DIVISOR=10.0;

	/**
	 * Gets the interest rate paid (or cost) on deposited money.
	 * A positive value is a payment, a negative is a cost.
	 * @see Banker#setCoinInterest(double)
	 * @return the interest rate paid (or cost) on deposited money.
	 */
	public double getCoinInterest();
	
	/**
	 * Sets the interest rate paid (or cost) on deposited money.
	 * A positive value is a payment, a negative is a cost.
	 * @see Banker#getCoinInterest()
	 * @param interest the interest rate paid (or cost) on deposited money.
	 */
	public void setCoinInterest(double interest);
	
	/**
	 * Gets the interest rate paid (or cost) on the value of deposited
	 * items.
	 * A positive value is a payment, a negative is a cost.
	 * @see Banker#setItemInterest(double)
	 * @return the interest rate paid (or cost) on deposited items.
	 */
	public double getItemInterest();
	
	/**
	 * Sets the interest rate paid (or cost) on the value of deposited
	 * items.
	 * A positive value is a payment, a negative is a cost.
	 * @see Banker#getItemInterest()
	 * @param interest the interest rate paid (or cost) on deposited items.
	 */
	public void setItemInterest(double interest);
	
	/**
	 * Gets the interest rate paid (or cost) on loaned out funds as  debt.
	 * A positive value is a cost, a negative is a bonus.
	 * @see Banker#setLoanInterest(double)
	 * @return the interest rate paid (or cost) on loaned money debt.
	 */
	public double getLoanInterest();
	
	/**
	 * Sets the interest rate paid (or cost) on loaned out funds as  debt.
	 * A positive value is a cost, a negative is a bonus.
	 * @see Banker#getLoanInterest()
	 * @param interest the interest rate paid (or cost) on loaned money debt.
	 */
	public void setLoanInterest(double interest);
	
	/**
	 * Gets the name of the bank chain to which this banker belongs.
	 * @see Banker#setBankChain(String)
	 * @return the bank chain name
	 */
	public String bankChain();
	
	/**
	 * Sets the name of the bank chain to which this banker belongs.
	 * @see Banker#bankChain()
	 * @param name the bank chain name
	 */
	public void setBankChain(String name);
	
	/**
	 * Returns all the player and clan names who have open accounts
	 * at this bank.
	 * @return all the player and clan names who have open accounts
	 */
	public List<String> getAccountNames();
	
	/**
	 * When the given mob tries to deposit or withdraw something, this method is 
	 * called to get the proper account name, which is either the mob themselves 
	 * or their clan, if they are (optionally) permitted by their rank.
	 * If checked is true, and the mob does NOT have clan privileges, then an
	 * error message is given to the mob and null is returned.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.Function
	 * @param mob the mob who is trying to deposit or withdraw or list or something
	 * @param func either Clan.Function.WITHDRAW or Clan.FUNCTION.DEPOSIT or LIST
	 * @param checked true if the mob must have clan privileges, false if not.
	 * @return the mobs name, their clan name, or null
	 */
	public String getBankClientName(MOB mob, Clan.Function func, boolean checked);

	/**
	 * Returns debt information for the given depositor to this bank chain.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.DebtItem
	 * @param depositorName the player or clan name that owes the bank money
	 * @return information about the debt
	 */
	public MoneyLibrary.DebtItem getDebtInfo(String depositorName);

	/**
	 * Deposits a new item into the given account.  Coin items are, of course,
	 * money.
	 * @see Banker#delAllDeposits(String)
	 * @see Banker#delDepositInventory(String, Item)
	 * @see Banker#getBalance(String)
	 * @param depositorName the account to deposit into, like mob or clan name 
	 * @param item the item to deposit
	 * @param container the container the item is in, which also needs depositing
	 */
	public void addDepositInventory(String depositorName, Item item, Item container);

	/**
	 * Deletes item into the given account.  Coin items are, of course,
	 * money.  The items are returned in a list to the caller.  If the 
	 * item is a container, all contained items are also returned.
	 * @see Banker#delAllDeposits(String)
	 * @see Banker#addDepositInventory(String, Item, Item)
	 * @see Banker#getBalance(String)
	 * @param depositorName the account to delete from, like mob or clan name 
	 * @param likeItem the likeItem to delete
	 * @return the collection of items deleted from the account
	 */
	public List<Item> delDepositInventory(String depositorName, Item likeItem);
	
	/**
	 * Empties all the items and money from a given depositors box.
	 * @see Banker#delDepositInventory(String, Item)
	 * @see Banker#addDepositInventory(String, Item, Item)
	 * @see Banker#getBalance(String)
	 * @param depositorName the account to empty, like mob or clan name 
	 */
	public void delAllDeposits(String depositorName);
	
	/**
	 * Returns the money balance in the account, in base value
	 * @see Banker#delDepositInventory(String, Item)
	 * @see Banker#addDepositInventory(String, Item, Item)
	 * @see Banker#delAllDeposits(String)
	 * @param depositorName the account to empty, like mob or clan name 
	 * @return the money balance, in base value
	 */
	public double getBalance(String depositorName);
	
	/**
	 * Returns the number of items deposited, including money items
	 * @see Banker#getDepositedItems(String)
	 * @see Banker#findDepositInventory(String, String)
	 * @see Banker#totalItemsWorth(String)
	 * @param depositorName the account to size up, like mob or clan name
	 * @return the number of items in the account
	 */
	public int numberDeposited(String depositorName);
	
	/**
	 * Returns all of the items deposited in the account. Make sure
	 * you destroy these when you are done looking at them!
	 * @see Banker#numberDeposited(String)
	 * @see Banker#findDepositInventory(String, String)
	 * @see Banker#totalItemsWorth(String)
	 * @param depositorName the account to return, like mob or clan name
	 * @return the list of all items in the account
	 */
	public List<Item> getDepositedItems(String depositorName);
	
	/**
	 * Searches the deposit inventory for an item with a substring name
	 * like the one given, returning the first found.  If the search
	 * string resembles a number at all, this will return the Coins
	 * item in the account.
	 * @see Banker#getDepositedItems(String)
	 * @see Banker#numberDeposited(String)
	 * @see Banker#totalItemsWorth(String)
	 * @param mob the player or clan name of the account to search
	 * @param likeThis the search string
	 * @return the item found, or null.
	 */
	public Item findDepositInventory(String mob, String likeThis);
	
	/**
	 * Returns the base money value of all items deposited in the given account.
	 * @see Banker#getDepositedItems(String)
	 * @see Banker#numberDeposited(String)
	 * @see Banker#findDepositInventory(String, String)
	 * @param depositorName the account to account for, like mob or clan name
	 * @return the base money value of all items deposited
	 */
	public double totalItemsWorth(String depositorName);
}
