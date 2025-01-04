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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2007-2025 Bo Zimmerman

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
 * An CraftBroker is a type of ShopKeeper that lists item requests
 * with prices instead of items themselves.  Customers who have
 * the listed item can then sell it to the broker for the listed price,
 * after which the actual item will be available for the original
 * requester to buy.  The CraftBroker, of course, also takes his cut.
 *
 * CraftBrokers belong to a broker chain, so that players need only
 * visit the broker nearest them to see all the requests.
 *
 * @author Bo Zimmerman
 *
 */
public interface CraftBroker extends ShopKeeper
{
	/**
	 * Gets the name of the broker chain to which this
	 * broker belongs.
	 * @see CraftBroker#setBrokerChain(String)
	 * @return name of the chain
	 */
	public String brokerChain();

	/**
	 * Sets the name of the broker chain to which this
	 * broker belongs.
	 * @see CraftBroker#brokerChain()
	 * @param named name of the chain
	 */
	public void setBrokerChain(String named);

	/**
	 * Gets the maximum number of game-days that
	 * an listing can continue.
	 * @see CraftBroker#setMaxTimedListingDays(int)
	 * @return the maximum number of game-days
	 */
	public int maxTimedListingDays();

	/**
	 * Sets the maximum number of game-days that
	 * an listing can continue.
	 * @see CraftBroker#maxTimedListingDays()
	 * @param d the maximum number of game-days
	 */
	public void setMaxTimedListingDays(int d);

	/**
	 * Gets the maximum number of listings that
	 * a player can have.
	 * @see CraftBroker#setMaxListings(int)
	 * @return the maximum number of listings
	 */
	public int maxListings();

	/**
	 * Sets the maximum number of listings that
	 * a player can have.
	 * @see CraftBroker#maxListings()
	 * @param d the maximum number of listings
	 */
	public void setMaxListings(int d);

	/**
	 * Gets the commission percentage.
	 * @see CraftBroker#setCommissionPct(int)
	 * @return the commission percentage
	 */
	public double commissionPct();

	/**
	 * Sets the commission percentage.
	 * @see CraftBroker#commissionPct()
	 * @param d the commission percentage
	 */
	public void setCommissionPct(double d);
}
