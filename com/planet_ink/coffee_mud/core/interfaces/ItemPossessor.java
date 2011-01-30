package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;
import java.util.List;

import com.planet_ink.coffee_mud.Items.interfaces.Item;
/*
Copyright 2000-2010 Bo Zimmerman

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
 * An interface for something capable of possessing Items
 * @author Bo Zimmerman
 *
 */
public interface ItemPossessor extends PhysicalAgent
{
	/**
	 * Adds a new item to its possessor. By default, the item is added
	 * in a default resting state -- no containers, timeouts, or other
	 * modifiers are set.  Duplicates will not be permitted.
	 * @see com.planet_ink.core.interfaces.ItemPossessor#delItem(Item)
	 * @param item the item to add
	 */
	public void addItem(Item item);
	
	
	/**
	 * Adds a new item to its possessor, with an expiration code.
	 * Duplicates will not be permitted.
	 * @see com.planet_ink.coffee_mud.core.Expire.ItemExpiration
	 * @param item the item to add
	 */
	public void addItem(Item item, Expire expire);
	
	/**
	 * Intelligently removes an item from its current location and
	 * moves it to this possessor, managing any container contents,
	 * and possibly followers/riders if the item is a cart.  An
	 * expiration can be set on the move to have the items expire.
	 * Duplicates will not be permitted.
	 * @see com.planet_ink.coffee_mud.core.Expire.ItemExpiration
	 * @param container the item to add
	 * @param expire the expiration code 
	 * @param moveFlags any flags related to the move
	 */
	public void moveItemTo(Item container, Expire expire, Move... moveFlags);
	
	/**
	 * Intelligently removes an item from its current location and
	 * moves it to this possessor, managing any container contents.
	 * Is the same as calling the longer moveItemTo with a Never
	 * expiration, and NO movement flags.
	 * Duplicates will not be permitted.
	 * @see com.planet_ink.coffee_mud.core.Expire.ItemExpiration
	 * @param container the item to add
	 */
	public void moveItemTo(Item container);
	
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

	/** constants for the addItem methods to denote how long the item lives before expiring */
	public enum Expire { Never, Monster_EQ, Player_Drop, Resource, Monster_Body, Player_Body	}

	/** constant for the moveItemTo methods to denote flags are being given -- normal operation */
	public enum Move { Followers}
	
	/** constant for the findItem/findItems method denoting special modifying flags on the search */
	public enum Find { WornOnly, UnwornOnly, AddCoins, RespectLocation} 
}
