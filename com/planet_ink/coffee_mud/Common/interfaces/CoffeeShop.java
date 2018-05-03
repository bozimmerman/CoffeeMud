package com.planet_ink.coffee_mud.Common.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Iterator;
import java.util.List;

/*
   Copyright 2005-2018 Bo Zimmerman

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
 * A CoffeeShop is an object for storing the inventory of a shopkeeper, banker,
 * auctionhouse, merchant, or other object that implements the ShopKeeper interface
 * for the purpose of selling goods and services.
 *
 * ShopKeepers maintain two types of inventory, the base inventory, and the stock
 * inventory. The stock or store inventory is the list of items the shopkeeper
 * currently has for sale, the amounts, base prices, etc.
 * The base inventory is used only for shopkeepers who only buy things like
 * they originally had in stock, and so the base inventory is always populated with
 * a single copy of the original store inventory, to be used as a base of comparison
 * for situations where the stock is empty, but someone is wanting to sell.
 *
 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper
 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#isSold(int)
 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_INVENTORYONLY
 */
public interface CoffeeShop extends CMCommon
{
	/**
	 * Returns whether an item sufficiently like the given item originally
	 * existed in this shops inventory when it was created.  Applies only
	 * to shops where their whatIsSold method returns ONLY_INVENTORY
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#isSold(int)
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_INVENTORYONLY
	 * @param thisThang the thing to compare against the base inventory
	 * @return whether the item, or one just like it, is in the base inventory
	 */
	public boolean inEnumerableInventory(Environmental thisThang);

	/**
	 * Adds a new item to the store inventory.  Use this method when an item is sold
	 * to the store, as pricing and other information will have to be derived.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#addStoreInventory(Environmental, int, int)
	 * @param thisThang the thing to sell
	 * @return the core store inventory item added
	 */
	public Environmental addStoreInventory(Environmental thisThang);

	/**
	 * Returns the number of items in the stores base inventory.  Only really useful
	 * for historical reasons, or if the shop sells inventory only.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#isSold(int)
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_INVENTORYONLY
	 * @return the number of items in the base inventory
	 */
	public int enumerableStockSize();

	/**
	 * Returns the number of items this shop currently has for sale.  Does not
	 * take number of duplicates into account.  For that call totalStockSizeIncludingDuplicates
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#totalStockSizeIncludingDuplicates()
	 * @return the number of items for sale.
	 */
	public int totalStockSize();

	/**
	 * Destroys all the items in this shop.
	 */
	public void destroyStoreInventory();

	/**
	 * Remove the specific give shelf product from the official stores.
	 * @param P the shelf product to remove permanently
	 */
	public void deleteShelfProduct(final ShelfProduct P);

	/**
	 * Returns a iterator of all the Environmental objects this shop has for sale.
	 * Will only return one of each item, even if multiple are available.
	 * @return a iterator of objects for sale.
	 */
	public Iterator<Environmental> getStoreInventory();

	/**
	 * Returns a iterator of all the ShelfProduct objects this shop has for sale.
	 * @return a iterator of all the shelves at the shop.
	 */
	public Iterator<ShelfProduct> getStoreShelves();

	/**
	 * Returns a iterator of all the Environmental objects this shop has for sale
	 * which match the given search string.
	 * Will only return one of each item, even if multiple are available.
	 * @param srchStr the item to hunt for.
	 * @return a iterator of objects for sale.
	 */
	public Iterator<Environmental> getStoreInventory(String srchStr);

	/**
	 * Returns a iterator of all the Environmental objects this shop has in its base
	 * inventory.  Only useful for historical reasons, or if the shop sells inventory
	 * only.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#isSold(int)
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_INVENTORYONLY
	 * @return a iterator of objects in base inventory
	 */
	public Iterator<Environmental> getEnumerableInventory();

	/**
	 * Clears both the base and stock/store inventories.
	 */
	public void emptyAllShelves();

	/**
	 * Adds a new item to the store inventory so the shopkeeper can sell it.  All items
	 * added go cumulatively into the store inventory, and one copy is kept in the
	 * base inventory for historical reasons.  The method is called when multiple items
	 * need to be added, or if the price is available.  This method is usually used to
	 * build an original shop inventory.
	 * @param thisThang the item/mob/ability to sell
	 * @param number the number of items to sell
	 * @param price the price of the item (in base currency) or -1 to have it determined
	 * @return the actual object stored in the inventory
	 */
	public Environmental addStoreInventory(Environmental thisThang, int number, int price);

	/**
	 * Total weight, in pounds, of all items in the store inventory, taking number in
	 * stock into account.
	 * @return the total weight in pounds
	 */
	public int totalStockWeight();

	/**
	 * The number of items in the store inventory, taking number in stock into account.
	 * Call this method to see how crowded the shop really is, as opposed to totalStockSize.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#totalStockSize()
	 * @return the total number of all items in stock
	 */
	public int totalStockSizeIncludingDuplicates();

	/**
	 * Removes all items like the given item from the base and store inventory.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#isSold(int)
	 * @param thisThang the item like which to remove
	 */
	public void delAllStoreInventory(Environmental thisThang);

	/**
	 * Returns whether an item with the given name is presently in this stores
	 * stock inventory, and available for sale.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#isSold(int)
	 * @param name the name of the item to search for
	 * @param mob the mob who is interested (stock can differ depending on customer)
	 * @return whether the item is available
	 */
	public boolean doIHaveThisInStock(String name, MOB mob);

	/**
	 * Returns the base stock price (not the final price by any means) that the shop
	 * will use as a foundation for determining the given items price.  -1 would mean
	 * that the shopkeeper uses the valuation of the item as a basis, whereas another
	 * value is in base gold.  Best to get likeThis item from the getStoreInventory()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#getStoreInventory()
	 * @param likeThis the item like which to compare
	 * @return the stock price of the item given.
	 */
	public int stockPrice(Environmental likeThis);

	/**
	 * Returns the number of items like the one given that the shopkeeper presently
	 * has in stock and available for sale.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#getStoreInventory()
	 * @param likeThis the item like which to compare
	 * @return the number currently in stock.
	 */
	public int numberInStock(Environmental likeThis);

	/**
	 * Searches this shops stock of items for sale for one matching the given name.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#getStoreInventory()
	 * @param name the name of the item to search for
	 * @param mob the mob who is interested (stock can differ depending on customer)
	 * @return the available item, if found
	 */
	public Environmental getStock(String name, MOB mob);

	/**
	 * Searches this shops stock of items for sale for one matching the given name.
	 * If one is found, it copies the item, removes one from the available stock, and
	 * returns the copy.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#getStoreInventory()
	 * @param name the name of the item to search for
	 * @param mob the mob who is interested (stock can differ depending on customer)
	 * @return the available item, if found
	 */
	public Environmental removeStock(String name, MOB mob);

	/**
	 * Searches this shops stock of items for sale for one matching the given name.
	 * If one is found, then one unit is removed from one of the available stock and
	 * true is returned.
	 * @param name the name of the item to lower the stock of
	 * @return true if it was lowered
	 */
	public boolean lowerStock(String name);

	/**
	 * Searches this shops stock of items for sale for one matching the given name.
	 * If one is found, it copies the item, removes one from the available stock, and
	 * prepares it for sale by adding it to a list along with any necessary accessories,
	 * such as necessary keys, or if a container, any contents of the container.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#getStoreInventory()
	 * @param named the name of the item to search for
	 * @param mob the mob who is interested (stock can differ depending on customer)
	 * @return the available items, if found, as a list of Environmental objects
	 */
	public List<Environmental> removeSellableProduct(String named, MOB mob);

	/**
	 * Generates an XML document of all available shop inventory, prices, and availability.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#getStoreInventory()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#buildShopFromXML(String)
	 * @return an XML document.
	 */
	public String makeXML();

	/**
	 * Repopulates this shop inventory from a given xml document, restoring store inventory,
	 * base inventory, prices, and availability.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop#makeXML()
	 * @param text the xml document to restore from
	 */
	public void buildShopFromXML(String text);

	/**
	 * A method for quickly making wholesale changes to a shopkeepers inventory.
	 * getStoreInventory should be called to get the list of items.  The items can
	 * then be modified, and this method called to properly "resubmit" them to
	 * the shopkeeper.
	 * @param shopItems the items for inventory
	 */
	public void resubmitInventory(List<Environmental> shopItems);

	/**
	 * Initializes this shop object with its host ShopKeeper
	 * @param SK the shopkeeper that hosts this object
	 * @return always this
	 */
	public CoffeeShop build(ShopKeeper SK);

	/**
	 * Returns the shopKeeper that is hosting this shop
	 * @return the shopKeeper that is hosting this shop
	 */
	public ShopKeeper shopKeeper();

	/**
	 * Returns a thin copy with independent lists, but the
	 * same items as the original host.
	 * @return the thin copy
	 */
	public CoffeeShop weakCopyOf();

	/**
	 * Returns whether the whatIsSold code applies to the shopkeeper hosting this shop.
	 * @see ShopKeeper#DEAL_DESCS
	 * @param code the whatIsSold code
	 * @return whether the whatIsSold code applies to the shopkeeper hosting this shop.
	 */
	public boolean isSold(int code);

	/**
	 * Class for representing a shelf product, holding
	 * an item prototype, the number in stock, and the
	 * price.  A price of -1 means to use the items
	 * calculated value (common).
	 * @author Bo Zimmermanimmerman
	 */
	public static class ShelfProduct
	{
		public Environmental product;
		public int number;
		public int price;
		public ShelfProduct(Environmental E, int number, int price)
		{
			this.product=E;this.number=number;this.price=price;
		}
		@Override
		public int hashCode()
		{
			return product.hashCode() ^ number ^ price;
		}
	}
	
	/**
	 * Returns a hash of all the items currently in the
	 * shop, for help in determining when things change.
	 * @return the hash of the contents.
	 */
	public long contentHash();
}
