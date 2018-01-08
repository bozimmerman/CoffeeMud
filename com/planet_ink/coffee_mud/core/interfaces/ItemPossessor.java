package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;
import java.util.List;

import com.planet_ink.coffee_mud.Items.interfaces.Item;
/*
   Copyright 2010-2018 Bo Zimmerman

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
public interface ItemPossessor extends PhysicalAgent, ItemCollection
{

	/**
	 * Adds a new item to its possessor, with an expiration code.
	 * Duplicates will not be permitted.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire
	 * @param item the item to add
	 * @param expire the expiration argument to decide when it will get cleaned up
	 */
	public void addItem(Item item, Expire expire);

	/**
	 * Intelligently removes an item from its current location and
	 * moves it to this possessor, managing any container contents,
	 * and possibly followers/riders if the item is a cart.  An
	 * expiration can be set on the move to have the items expire.
	 * Duplicates will not be permitted.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire
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
	 * @see com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire
	 * @param container the item to add
	 */
	public void moveItemTo(Item container);

	/** constants for the addItem methods to denote how long the item lives before expiring */
	public enum Expire { Never, Monster_EQ, Player_Drop, Resource, Monster_Body, Player_Body	}

	/** constant for the moveItemTo methods to denote flags are being given -- normal operation */
	public enum Move { Followers}

	/** constant for the findItem/findItems method denoting special modifying flags on the search */
	public enum Find { WornOnly, UnwornOnly, AddCoins, RespectLocation}
}
