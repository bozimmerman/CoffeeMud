package com.planet_ink.coffee_mud.Abilities.interfaces;
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
 * The interface for affects which cause an Item
 * to rejuvenate after a particular amount of time.  This interface
 * also allows the system to "source" an item back to its originating
 * room.
 * Items do not usually have tick services, so this affect ticks in
 * an items stead to allow it to rejuvenate.
 */
public interface ItemTicker extends Ability
{
	/**
	 * Registers the given item as being from the given room.  It will
	 * read the items phyStats().rejuv() value and use it as an interval
	 * for checking to see if this item is no longer in its originating
	 * room.  If so, it will create a copy of it in the originating room.
	 * @param item the item to rejuvenate
	 * @param room the room which the item is from
	 */
	public void loadMeUp(Item item, Room room);

	/**
	 * Removes the rejuvenating ticker from an item.  This
	 * is done when a room is resetting its content, and this
	 * item is no longer to be used as a source for rejuvenation.
	 * @param item the item to remove from tracking
	 */
	public void unloadIfNecessary(Item item);

	/**
	 * Returns the room where this item belongs
	 * @return a Room object
	 */
	public Room properLocation();

	/**
	 * Sets the room where this item belongs
	 * @param room a room object
	 */
	public void setProperLocation(Room room);

	/**
	 * Returns whether the given item is an official item
	 * being managed as a rejuveing item
	 * @param item the item to check for
	 * @return true if it belongs, false otherwise
	 */
	public boolean isVerifiedContents(Item item);
}
