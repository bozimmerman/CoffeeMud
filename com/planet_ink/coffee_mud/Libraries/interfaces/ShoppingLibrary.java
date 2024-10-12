package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
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
   Copyright 2005-2024 Bo Zimmerman

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
 * This library handles everything related to shopkeepers
 * and merchants.  This includes all the buying, selling, paying,
 * being paid, viewing, and valueing.
 * Although there are shopkeeper mobs, there are also shopkeeper
 * properties that can appear on anything, and so you'll often see
 * that shopkeeper interface is considered separate from the host
 * object that the player sees as the shop.
 *
 * @author Bo Zimmerman
 *
 */
public interface ShoppingLibrary extends CMLibrary
{
	/**
	 * Given a potential shopkeeper host, this will return
	 * the shopkeeper shop itself.  Sometimes they are the
	 * same, sometimes not.
	 *
	 * @param E the potential shopkeeper host
	 * @return null, or the shopkeeper shop
	 */
	public ShopKeeper getShopKeeper(Environmental E);

	/**
	 * Returns all the shopkeeper hosts accessible by anyone in
	 * the given room.
	 *
	 * @param here the room to find shopkeeper
	 * @param notMOB an exception, or null for no exceptions
	 * @return the list of all shopkeeper hosts
	 */
	public List<Environmental> getAllShopkeepers(Room here, MOB notMOB);

	/**
	 * Returns the official view message for the given item and viewer
	 * mob.
	 *
	 * @see ShopKeeper.ViewType
	 *
	 * @param viewerM the mob viewing the irem
	 * @param E the item being viewed
	 * @param flags the level of information returned
	 * @return the official 'view' of an item
	 */
	public String getViewDescription(MOB viewerM, Environmental E, Set<ViewType> flags);

	/**
	 * Given a BUY message string, will look for the FOR syntax
	 * and return the mob in the same room as the agent buyer
	 * with that name, or null.
	 *
	 * @param buyer the agent buyer in the room
	 * @param message the buy message string
	 * @return the mob in the room the item is really for
	 */
	public MOB parseBuyingFor(MOB buyer, String message);

	/**
	 * Returns whether the given ignorer (usually a shopkeeper mob) is ignoring
	 * the given mob for most purposes.
	 *
	 * @param mob the mob who might be ignored
	 * @param ignoreMask the zappermask determining if the mob is ignored
	 * @param whoIgnores the mob who might be ignoring
	 * @return true if ignored, false othwrwise
	 */
	public boolean ignoreIfNecessary(MOB mob, String ignoreMask, MOB whoIgnores);

	/**
	 * Returns a brief plain ordinary camel-cased description of what kind
	 * of shopkeeper this is: Miscellaneous Magic Items, General Items, etc.
	 *
	 * @param shop coffee shop inventory to check
	 * @param keeper the actual coffee shop
	 * @return the items sold description
	 */
	public String storeKeeperString(CoffeeShop shop, ShopKeeper keeper);

	/**
	 * Returns whether the given shopkeeper shop sells the given thing
	 * normally.  Does not care whether its in stock.
	 *
	 * @param thisThang the thing to see if its ordinary stock
	 * @param shop the actual shop to check for a good match
	 * @return true if the shop would sell it, false otherwise
	 */
	public boolean doISellThis(Environmental thisThang, ShopKeeper shop);

	/**
	 * Gives the price of an item that might be sold to a player/mob by a shopkeeper.
	 *
	 * @see ShoppingLibrary#transactMoneyOnly(MOB, MOB, ShopKeeper, Environmental, BuySellFlag)
	 * @see ShoppingLibrary#sellEvaluation(MOB, MOB, Environmental, ShopKeeper, BuySellFlag)
	 * @see ShoppingLibrary#getSalesTax(Room, MOB)
	 *
	 * @param sellerShopM the shopkeeper mob
	 * @param buyerCustM the player buyer mob
	 * @param product the proposed produce to sell to the shop
	 * @param shopKeeper the shop itself
	 * @param shop the inventory object of the shop
	 * @param includeSalesTax true to include sales tax in the price, false otherwise
	 * @return the selling price that this shopkeeper will put on the item
	 */
	public ShopKeeper.ShopPrice sellingPrice(MOB sellerShopM, MOB buyerCustM, Environmental product,
											 ShopKeeper shopKeeper, CoffeeShop shop, boolean includeSalesTax);

	/**
	 * Returns the applicable sales tax percentage for the given seller from the
	 * given room.
	 *
	 * @see ShoppingLibrary#transactMoneyOnly(MOB, MOB, ShopKeeper, Environmental, BuySellFlag)
	 * @see ShoppingLibrary#sellEvaluation(MOB, MOB, Environmental, ShopKeeper, BuySellFlag)
	 * @see ShoppingLibrary#sellingPrice(MOB, MOB, Environmental, ShopKeeper, CoffeeShop, boolean)
	 *
	 * @param homeRoom the room the shopkeeper is originally from
	 * @param seller the seller him or herself
	 * @return a value from 0-1.
	 */
	public double getSalesTax(Room homeRoom, MOB seller);

	/**
	 * Evaluates a potential sale of an item from a shopkeeper to a player/npc.
	 * Returns whether the sale can go forth.  This also handles proposed
	 * VIEW commands.
	 *
	 * @see ShoppingLibrary#transactMoneyOnly(MOB, MOB, ShopKeeper, Environmental, BuySellFlag)
	 * @see ShoppingLibrary#getSalesTax(Room, MOB)
	 * @see ShoppingLibrary#sellingPrice(MOB, MOB, Environmental, ShopKeeper, CoffeeShop, boolean)
	 *
	 * @param sellerShopM the mob doing the selling
	 * @param buyerCustM the player or mob doing the buying
	 * @param product the product to potentiall sell to the buyer
	 * @param shop the actual shop
	 * @param buyFlag the flag to tell how to do the evaluation
	 * @return true if the buy or view should go forward
	 */
	public boolean sellEvaluation(MOB sellerShopM, MOB buyerCustM, Environmental product, ShopKeeper shop, BuySellFlag buyFlag);

	/**
	 * Part of a shopkeeper selling an item to a player/mob is the transaction of the price.
	 * This handles that by taking away the buyers money, qp, xp, or whatever.  The
	 * price comes from sellingPrice above.  Having the money go into the shopkeepers
	 * pocket is optional.
	 *
	 * @see ShoppingLibrary#sellEvaluation(MOB, MOB, Environmental, ShopKeeper, BuySellFlag)
	 * @see ShoppingLibrary#getSalesTax(Room, MOB)
	 * @see ShoppingLibrary#sellingPrice(MOB, MOB, Environmental, ShopKeeper, CoffeeShop, boolean)
	 *
	 * @param sellerM the seller mob who wants money
	 * @param buyerM the buyer mob who is losing money
	 * @param shop the shop itself
	 * @param product the product being sold, whose value constitutes the price
	 * @param flag true to add the money to the shopkeepers
	 */
	public void transactMoneyOnly(MOB sellerM, MOB buyerM, ShopKeeper shop, Environmental product, BuySellFlag flag);


	/**
	 * Adjusts the given inventory of a shopkeepers shop by adding external inventory, which might include
	 * new real estate titles, or existing titles to real estate, ships, and the like.
	 *
	 * @param productsV the existing inventory
	 * @param buyer the buyer who wants to see the inventory
	 * @param shop the shop inventory object
	 * @param myRoom the room where the shopkeeper is
	 * @return the filled inventory
	 */
	public List<Environmental> addRealEstateTitles(List<Environmental> productsV, MOB buyer, CoffeeShop shop, Room myRoom);

	/**
	 * Gives the value of an item that might be sold to a shopkeeper by a player/mob.
	 * Takes current stock into account.
	 *
	 * @see ShoppingLibrary#transactPawn(MOB, MOB, ShopKeeper, Environmental, CoffeeShop, BuySellFlag)
	 * @see ShoppingLibrary#pawnEvaluation(MOB, MOB, Environmental, ShopKeeper, double, double, BuySellFlag)
	 *
	 * @param buyerShopM the shopkeeper mob
	 * @param sellerCustM the player seller mob
	 * @param product the proposed produce to sell to the shop
	 * @param shopKeeper the shop itself
	 * @return the valuation that this shopkeeper will put on the item
	 */
	public ShopKeeper.ShopPrice pawningPrice(MOB buyerShopM, MOB sellerCustM, Environmental product,
											 ShopKeeper shopKeeper);

	/**
	 * Evaluates a proposed sale of an item to a shopkeeper by a player/mob
	 * Returns whether the sale can go forth.  This also handles proposed
	 * VALUE commands.
	 *
	 * @see ShoppingLibrary#transactPawn(MOB, MOB, ShopKeeper, Environmental, CoffeeShop, BuySellFlag)
	 * @see ShoppingLibrary#pawningPrice(MOB, MOB, Environmental, ShopKeeper)
	 *
	 * @param buyerShopM the shopkeeper mob
	 * @param sellerCustM the player seller mob
	 * @param product the proposed produce to sell to the shop
	 * @param shop the shop itself
	 * @param maxToPay money the shopkeeper has remaining
	 * @param maxEverPaid the overall budget of the shopkeeper
	 * @param buyFlag how to do the evaluation
	 * @return true if the sale should go through, false otherwise
	 */
	public boolean pawnEvaluation(MOB buyerShopM, MOB sellerCustM, Environmental product,
								  ShopKeeper shop, double maxToPay, double maxEverPaid, BuySellFlag buyFlag);

	/**
	 * Does the transaction where a player/mob sells an item to a shopkeeper.
	 * It returns the amount given to the player in absolute value.  Adds
	 * the item to the shopkeepers inventory
	 *
	 * @see ShoppingLibrary#pawnEvaluation(MOB, MOB, Environmental, ShopKeeper, double, double, BuySellFlag)
	 * @see ShoppingLibrary#pawningPrice(MOB, MOB, Environmental, ShopKeeper)
	 *
	 * @param shopkeeperM the shopkeeper mob being sold to
	 * @param pawnerM the player/mob selling an item
	 * @param shop the shopkeeper object
	 * @param product the product sold to the shopkeeper
	 * @param shopItems the inventory object for the shopkeeper
	 * @param flag flag on how to do the transaction
	 * @return the value given to the player
	 */
	public double transactPawn(MOB shopkeeperM, MOB pawnerM, ShopKeeper shop, Environmental product, CoffeeShop shopItems, BuySellFlag flag);

	/**
	 * Checks a BUY message for an english embedded 'FOR' message, which
	 * can affect the assignment of purchased private property.
	 *
	 * @see ShoppingLibrary#getListInventory(MOB, MOB, List, int, ShopKeeper, String)
	 * @see ShoppingLibrary#getListForMask(String)
	 *
	 * @param targetMessage the target message string
	 * @return null, or a name that the object is being purchased for
	 */
	public String getListForMask(String targetMessage);

	/**
	 * Formats and returns a displayable inventory for a shopkeeper.
	 *
	 * @see ShoppingLibrary#getListInventory(MOB, MOB, List, int, ShopKeeper, String)
	 * @see ShoppingLibrary#getListForMask(String)
	 *
	 * @param seller the seller mob who speaks
	 * @param buyer the buyer mob who is ... buying
	 * @param inventory the inventory of the seller to show
	 * @param limit 0, or maximum number of rows to display
	 * @param shop the ShopKeeper object
	 * @param mask null, or a name substring mask for items to show
	 * @return the formatted displayable inventory
	 */
	public String getListInventory(MOB seller,  MOB buyer, List<? extends Environmental> inventory,
								   int limit, ShopKeeper shop, String mask);

	/**
	 * Formats and returns a displayable inventory for a shopkeeper.
	 *
	 * @see ShoppingLibrary#getListInventory(MOB, MOB, List, int, ShopKeeper, String)
	 * @see ShoppingLibrary#getListForMask(String)
	 *
	 * @param seller the seller mob who speaks
	 * @param buyer the buyer mob who is ... buying
	 * @param inventory the inventory of the seller to show
	 * @param limit 0, or maximum number of rows to display
	 * @param shop the ShopKeeper object
	 * @param shopItems the ShopKeeper stock list
	 * @param mask null, or a name substring mask for items to show
	 * @return the formatted displayable inventory
	 */
	public String getListInventory(MOB seller,  MOB buyer, List<? extends Environmental> inventory,
								   int limit, ShopKeeper shop, CoffeeShop shopItems, String mask);

	/**
	 * Given an inn key, and a starting room, this will return
	 * the directions to follow to reach the room.
	 *
	 * @param key the key to find the room for
	 * @param addThis a word to prefix the directions with
	 * @param R the room starting from
	 * @return the directions to the room
	 */
	public String findInnRoom(InnKey key, String addThis, Room R);

	/**
	 * Parses a rate of devaluation for each purchase, preceded
	 * optionally by a special rate just for raw resources. Each
	 * numbe is a percentage like 30%.
	 *
	 * @see ShoppingLibrary#parseItemPricingAdjustments(String)
	 * @see ShoppingLibrary#parseBudget(String)
	 *
	 * @param factors the rate of devaluation(s)
	 * @return the raw material followed by normal devalue rate 0-1
	 */
	public double[] parseDevalueRate(String factors);

	/**
	 * Parses the given list of factors into a set of factors.
	 * Each factor is a mask followed by a decimal percentage
	 * to adjust prices by.
	 *
	 * @see ShoppingLibrary#parseDevalueRate(String)
	 * @see ShoppingLibrary#parseBudget(String)
	 *
	 * @param factors the raw factors string
	 * @return the parsed list of factors
	 */
	public String[] parseItemPricingAdjustments(String factors);

	/**
	 * Parses the given budget string into a data structure
	 * showing the amount of base currency, and how often
	 * that amount is reset for the purpose of making
	 * purchases.
	 *
	 * @see ShoppingLibrary#parseDevalueRate(String)
	 * @see ShoppingLibrary#parseItemPricingAdjustments(String)
	 *
	 * @param budget the encoded budget string (amount time period)
	 * @return the parsed budget object
	 */
	public Pair<Long,TimeClock.TimePeriod> parseBudget(String budget);

	/**
	 * Completes the purchase of the given item from the given seller to the given
	 * buyer mob.
	 *
	 * @see ShoppingLibrary#purchaseAbility(Ability, MOB, ShopKeeper, MOB)
	 * @see ShoppingLibrary#purchaseMOB(MOB, MOB, ShopKeeper, MOB)
	 *
	 * @param baseProduct the item being purchased
	 * @param products all items in the item product
	 * @param seller the seller
	 * @param mobFor the buyer
	 * @return true if the purchase went through, false otherwise
	 */
	public boolean purchaseItems(Item baseProduct, List<Environmental> products, MOB seller, MOB mobFor);

	/**
	 * Completes the purchase of the given follower mob from the given seller to the given
	 * buyer mob.
	 *
	 * @see ShoppingLibrary#purchaseAbility(Ability, MOB, ShopKeeper, MOB)
	 * @see ShoppingLibrary#purchaseItems(Item, List, MOB, MOB)
	 *
	 * @param product the follower mob being purchased
	 * @param seller the seller
	 * @param shop the seller's shop
	 * @param mobFor the buyer
	 * @return true if the purchase went through, false otherwise
	 */
	public boolean purchaseMOB(MOB product, MOB seller, ShopKeeper shop, MOB mobFor);

	/**
	 * Completes the purchase of the given ability from the given seller to the given
	 * buyer mob.
	 *
	 * @see ShoppingLibrary#purchaseMOB(MOB, MOB, ShopKeeper, MOB)
	 * @see ShoppingLibrary#purchaseItems(Item, List, MOB, MOB)
	 *
	 * @param A the ability being purchased
	 * @param seller the seller
	 * @param shop the seller's shop
	 * @param mobFor the buyer
	 */
	public void purchaseAbility(Ability A,  MOB seller, ShopKeeper shop, MOB mobFor);

	/**
	 * A manipulator of the given players money.  Either adds or subtracts
	 * money as given, and then re-saves the players inventory to the
	 * database.
	 *
	 * @param to the mob who is getting or losing money
	 * @param currency the currency to add/remove
	 * @param amt the amount to add/remove
	 */
	public void returnMoney(MOB to, String currency, double amt);

	/**
	 * Submits a new bid for an item by the given mob, of the given
	 * amount of the given currency to the given auction for the
	 * given item.  It also returns a message for any public
	 * auction channels to the given array.  It turns a two-
	 * dimensional array.
	 *
	 * @see AuctionData
	 * @see ShoppingLibrary#auctionNotify(MOB, String, String)
	 * @see ShoppingLibrary#saveAuction(AuctionData, String, boolean)
	 * @see ShoppingLibrary#cancelAuction(String, AuctionData)
	 * @see ShoppingLibrary#fetchAuctionByItemName(String, String)
	 * @see ShoppingLibrary#getAuctions(Object, String)
	 * @see ShoppingLibrary#getAuctionInventory(MOB, MOB, Auctioneer, String)
	 *
	 * @param mob the bidder mob
	 * @param bid the bidding amount in base value
	 * @param bidCurrency the bidding currency
	 * @param auctionData the auction data for the auction bid on
	 * @param I the item bid on (why not use from auction data?)
	 * @param auctionAnnounces list to put channel messages into
	 * @return 2-dim array: message to bidder, message to prev high bidder
	 */
	public String[] bid(MOB mob, double bid, String bidCurrency,
						AuctionData auctionData, Item I, List<String> auctionAnnounces);

	/**
	 * Returns the formal auction listing of all items for the given auctioneer's auction house.
	 *
	 * @see AuctionData
	 * @see ShoppingLibrary#auctionNotify(MOB, String, String)
	 * @see ShoppingLibrary#saveAuction(AuctionData, String, boolean)
	 * @see ShoppingLibrary#cancelAuction(String, AuctionData)
	 * @see ShoppingLibrary#fetchAuctionByItemName(String, String)
	 * @see ShoppingLibrary#getAuctions(Object, String)
	 * @see ShoppingLibrary#bid(MOB, double, String, AuctionData, Item, List)
	 *
	 * @param seller the seller mob, which is not necessarily the auction house
	 * @param buyer the buyer doing the listing
	 * @param auction the auction house object itself
	 * @param itemName the null or the name of the item interested in
	 * @return the displayable list of auctioned items
	 */
	public String getAuctionInventory(MOB seller, MOB buyer, Auctioneer auction, String itemName);

	/**
	 * Returns an enumeration of all active auctions in the given auction house,
	 * of the given name
	 *
	 * @see AuctionData
	 * @see ShoppingLibrary#auctionNotify(MOB, String, String)
	 * @see ShoppingLibrary#saveAuction(AuctionData, String, boolean)
	 * @see ShoppingLibrary#cancelAuction(String, AuctionData)
	 * @see ShoppingLibrary#fetchAuctionByItemName(String, String)
	 * @see ShoppingLibrary#getAuctionInventory(MOB, MOB, Auctioneer, String)
	 * @see ShoppingLibrary#bid(MOB, double, String, AuctionData, Item, List)
	 *
	 * @param ofLike null, or a name to match
	 * @param auctionHouse the auction house to return auctions from
	 * @return an enumeration of all auctions
	 */
	public Enumeration<AuctionData> getAuctions(Object ofLike, String auctionHouse);

	/**
	 * Returns auction data for the auction for an item of the given name
	 * in the given auction house.
	 *
	 * @see AuctionData
	 * @see ShoppingLibrary#auctionNotify(MOB, String, String)
	 * @see ShoppingLibrary#saveAuction(AuctionData, String, boolean)
	 * @see ShoppingLibrary#cancelAuction(String, AuctionData)
	 * @see ShoppingLibrary#getAuctions(Object, String)
	 * @see ShoppingLibrary#getAuctionInventory(MOB, MOB, Auctioneer, String)
	 * @see ShoppingLibrary#bid(MOB, double, String, AuctionData, Item, List)
	 *
	 * @param named the item name, or partial match
	 * @param auctionHouse the auction house to search in
	 * @return null, or the auctio data
	 */
	public AuctionData fetchAuctionByItemName(String named, String auctionHouse);

	/**
	 * Sends a notification to the given stakeholder in an auction for the given
	 * item.  It does a tell for online players, and an email for those
	 * not online.
	 *
	 * @see AuctionData
	 * @see ShoppingLibrary#saveAuction(AuctionData, String, boolean)
	 * @see ShoppingLibrary#cancelAuction(String, AuctionData)
	 * @see ShoppingLibrary#fetchAuctionByItemName(String, String)
	 * @see ShoppingLibrary#getAuctions(Object, String)
	 * @see ShoppingLibrary#getAuctionInventory(MOB, MOB, Auctioneer, String)
	 * @see ShoppingLibrary#bid(MOB, double, String, AuctionData, Item, List)
	 * @see ShoppingLibrary#auctionNotify(MOB, String, String)
	 *
	 * @param M the recipient of the message
	 * @param resp the message
	 * @param regardingItem the item up for auction
	 */
	public void auctionNotify(MOB M, String resp, String regardingItem);

	/**
	 * Cancels the given auction.
	 *
	 * @see AuctionData
	 * @see ShoppingLibrary#saveAuction(AuctionData, String, boolean)
	 * @see ShoppingLibrary#fetchAuctionByItemName(String, String)
	 * @see ShoppingLibrary#getAuctions(Object, String)
	 * @see ShoppingLibrary#getAuctionInventory(MOB, MOB, Auctioneer, String)
	 * @see ShoppingLibrary#bid(MOB, double, String, AuctionData, Item, List)
	 * @see ShoppingLibrary#auctionNotify(MOB, String, String)
	 *
	 * @param auctionHouse the auction house the data belongs to
	 * @param data the AuctionData to delete
	 */
	public void cancelAuction(String auctionHouse, AuctionData data);

	/**
	 * Update the given AuctionData (an auction) for the given auctionHouse.
	 * Called usually when the auction is created, or max big is raised.
	 *
	 * @see AuctionData
	 * @see ShoppingLibrary#auctionNotify(MOB, String, String)
	 * @see ShoppingLibrary#cancelAuction(String, AuctionData)
	 * @see ShoppingLibrary#fetchAuctionByItemName(String, String)
	 * @see ShoppingLibrary#getAuctions(Object, String)
	 * @see ShoppingLibrary#getAuctionInventory(MOB, MOB, Auctioneer, String)
	 * @see ShoppingLibrary#bid(MOB, double, String, AuctionData, Item, List)
	 *
	 * @param data the AuctionData to update
	 * @param auctionHouse the auction house the data belongs to
	 * @param updateOnly true for update, false when creating
	 */
	public void saveAuction(AuctionData data, String auctionHouse, boolean updateOnly);

	/**
	 * Flag to tell the system how to handle a transaction.
	 *
	 * @author BZ
	 */
	public enum BuySellFlag
	{
		INFO,
		RETAIL,
		WHOLESALE
	}
}
