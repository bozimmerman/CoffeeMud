package com.planet_ink.coffee_mud.core.interfaces;
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

import java.util.List;

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
 * Interface for objects which represents real estate purchasable by players.  May
 * be found implemented by Abilities which are placed as effects on the room  objects
 * for sale, or implemented as Items representing the sellable title.
 * @author Bo Zimmerman
 */
public interface LandTitle extends PrivateProperty
{
	/**
	 * Get the roomID or the Area name of the property for sale
	 * @return the roomID or the Area  name of the property for sale
	 */
	public String landPropertyID();
	
	/**
	 * Set the roomID or the Area name of the property for sale
	 * @param landID the roomID or the Area  name of the property for sale
	 */
	public void setLandPropertyID(String landID);

	/**
	 * Checks for changes in the content or condition of the rooms represented
	 * by this title and saves the changes to the database, if necessary.
	 * @param optPlayerList - null, or a vector of player names for quick confirms
	 * @see LandTitle#updateTitle()
	 */
	public void updateLot(List<String> optPlayerList);
	
	/**
	 * Simply resaves the rooms represented by this title to reflect change
	 * in ownership or price.  The state of the rooms is not inspected or
	 * updated as in updateLot.
	 * @see LandTitle#updateLot(List)
	 */
	public void updateTitle();

	/**
	 * The complete set of room objects represented by this title
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return a list of the complete set of Room objects represented by this title
	 */
	public List<Room> getAllTitledRooms();
	
	/**
	 * The complete set of room objects that are tied together by one or more titles.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return a list of the complete set of Room objects represented by property
	 */
	public List<Room> getConnectedPropertyRooms();

	/**
	 * Returns a unique identifier corresponding to getConnectedPropertyRooms.
	 * An identifier that uniquely identifies all the connected lots of this property,
	 * even if they are unowned, or owned by different people.  Think of it as a "subdivision"
	 * when lots are variously owned, or a "mansion id" when lots are owned by one person.
	 * @return unique identifier
	 */
	public String getUniqueLotID();

	/**
	 * Whether this property is a rental.
	 * @return true if the property is rental, false if ownable outright
	 */
	public boolean rentalProperty();

	/**
	 * Sets whether this property is a rental.
	 * @param truefalse true if the property is rental, false if ownable outright
	 */
	public void setRentalProperty(boolean truefalse);
	
	/**
	 * If back taxes is owned on this property, this is how the value is set.  The
	 * value should be in base coffeemud currency
	 * @param amount the back  taxes owed
	 */
	public void setBackTaxes(int amount);

	/**
	 * If back taxes is owned on this property, this is how the value is retrieved.  The
	 * value should be in base coffeemud currency
	 * @return amount the back  taxes owed
	 */
	public int backTaxes();

	/**
	 * If this title allows expansion construction, this this will return the new
	 * title to assign to the newly constructed rooms.
	 * @see LandTitle#allowsExpansionConstruction()
	 * @return the new title to assigne to a new room.
	 */
	public LandTitle generateNextRoomTitle();
	
	/**
	 * Returns whether this title allows property to be expanded through masonry or construction.
	 * @see LandTitle#generateNextRoomTitle()
	 * @return true if expansion is OK, false otherwise
	 */
	public boolean allowsExpansionConstruction();

	/**
	 * Returns whether this title creates connected walls between new rooms as if the rooms
	 * were laid out in a grid.
	 * @return true if the rooms can lay out as a grid, false otherwise
	 */
	public boolean gridLayout();

	/**
	 * Sets whether this title creates connected walls between new rooms as if the rooms
	 * were laid out in a grid.
	 * @param gridLayout true if the rooms can lay out as a grid, false otherwise
	 */
	public void setGridLayout(boolean gridLayout);

}
