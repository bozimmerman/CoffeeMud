package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;
import java.util.List;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
/*
   Copyright 2011-2018 Bo Zimmerman

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
import com.planet_ink.coffee_mud.core.CMLib;

/**
 * An abstract interface for something capable of grouping items together
 * @author Bo Zimmerman
 *
 */
public interface ItemCollection extends CMObject
{
	/**
	 * Adds a new item to its possessor. By default, the item is added
	 * in a default resting state -- no containers, timeouts, or other
	 * modifiers are set.  Duplicates will not be permitted.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ItemPossessor#delItem(Item)
	 * @param item the item to add
	 */
	public void addItem(Item item);

	/**
	 * Removes the item from this possessor.
	 * @param item the item to remove
	 */
	public void delItem(Item item);

	/**
	 * Returns the total number of items at this possessor, regardless
	 * of container status.
	 * @return the total number of items
	 */
	public int numItems();

	/**
	 * Returns the item at the given index, regardless of container status,
	 * visibility, or other modifiers.
	 * @param i the index of the item
	 * @return the item at that index, or null if its not found
	 */
	public Item getItem(int i);

	/**
	 * Returns a random item in this collection, or null
	 * @return a random item in this collection, or null
	 */
	public Item getRandomItem();

	/**
	 * An enumeration of all the items at this possessor.
	 * @return enumeration of all the items at this possessor.
	 */
	public Enumeration<Item> items();

	/**
	 * Returns the item in the given container that matches the
	 * given itemID, whether by full name, description, class ID,
	 * or partial name (if no fuller name is found).  Handles indexing
	 * for duplicate-named items.
	 * @param goodLocation the container to look in, or null for none
	 * @param itemID the name or partial name of the item to fetch
	 * @return the item found, or null
	 */
	public Item findItem(Item goodLocation, String itemID);

	/**
	 * Returns the item in this possessor that matches the
	 * given itemID, whether by full name, description, class ID,
	 * or partial name (if no fuller name is found).  Handles indexing
	 * for duplicate-named items.
	 * @param itemID the name or partial name of the item to fetch
	 * @return the item found, or null
	 */
	public Item findItem(String itemID);

	/**
	 * Returns all items in the given container that matches the
	 * given itemID, whether by full name, description, class ID,
	 * or partial name (if no fuller names are found).
	 *
	 * @param goodLocation the container to look in, or null for none
	 * @param itemID the name or partial name of the item to fetch
	 * @return the item found, or null
	 */
	public List<Item> findItems(Item goodLocation, String itemID);

	/**
	 * Returns all items in this possessor that matches the
	 * given itemID, whether by full name, description, class ID,
	 * or partial name (if no fuller names are found).
	 *
	 * @param itemID the name or partial name of the item to fetch
	 * @return the item found, or null
	 */
	public List<Item> findItems(String itemID);

	/**
	 * Returns whether the given item is in this possessors list.
	 * @param item the item to check
	 * @return true if the item was found, and false otherwise
	 */
	public boolean isContent(Item item);
	/**
	 * Removes all items from this collection
	 * @param destroy true to also destroy the items
	 */
	public void delAllItems(boolean destroy);
	/**
	 * Applies the given code to each item in this collection
	 * @param applier code to execute against each object
	 */
	public void eachItem(final EachApplicable<Item> applier);
}
