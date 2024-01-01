package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;
import java.util.List;

import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
/*
   Copyright 2010-2024 Bo Zimmerman

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

	/**
	 * Generates a specific search-string name for the given
	 * object in this possessor.  Since items or mobs with the exact
	 * same name can be in a given room, a context-number-suffix
	 * (.1, .2, etc..) is used to specify which of the identical
	 * objects to return in the list.  This method wil, given
	 * an item or mob, will generate that search string
	 * by returning the name plus the optional context suffix.
	 * @param E the mob or item to return a search string for
	 * @return the specific search string that returns the given object
	 */
	public String getContextName(Environmental E);

	/**
	 * Constants for the addItem methods to denote how long the item
	 * lives before expiring.  Includes method to return that time
	 * in milliseconds.
	 * */
	public enum Expire
	{
		Never(null),
		Monster_EQ(CMProps.Int.EXPIRE_MONSTER_EQ),
		Player_Drop(CMProps.Int.EXPIRE_PLAYER_DROP),
		Resource(CMProps.Int.EXPIRE_RESOURCE),
		Monster_Body(CMProps.Int.EXPIRE_MONSTER_BODY),
		Player_Body(CMProps.Int.EXPIRE_PLAYER_BODY),
		Inheret(null);
		private final CMProps.Int propCode;
		private Expire(final CMProps.Int propCode)
		{
			this.propCode = propCode;
		}
		/**
		 * Return the expiration time of an item of this
		 * expire code in RL milliseconds.
		 * @return the number of milliseconds
		 */
		public long getExpirationMilliseconds()
		{
			if(propCode == null)
				return 0;
			return CMProps.getIntVar(this.propCode) * TimeManager.MILI_MINUTE;
		}
	}

	/** constant for the moveItemTo methods to denote flags are being given -- normal operation */
	public enum Move { Followers, Optimize}

	/** constant for the findItem/findItems method denoting special modifying flags on the search */
	public enum Find { WornOnly, UnwornOnly, AddCoins, RespectLocation}
}
