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
   Copyright 2007-2018 Bo Zimmerman

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
 * An Auctioneer is a type of ShopKeeper that wants customers to bid
 * higher prices on items instead of simply buying them.  Other players
 * are also responsible for giving the auctioneer things to auction
 * off.  The winning bidder has the item delivered to them
 * automatically, even if they are offline, and the money is 
 * also exchanged automatically by the Auctioneer.
 *
 * Auctioneers belong to a chain called an auction house, so that
 * players need only visit the auctioneer nearest them to see all the
 * items that are up for auction in a particular auction house.
 * 
 * @see com.planet_ink.coffee_mud.Common.interfaces.AuctionPolicy
 * 
 * @author Bo Zimmerman
 *
 */
public interface Auctioneer extends ShopKeeper, AuctionPolicy
{
	/**
	 * Gets the name of the auction house to which this
	 * auctioneer belongs.
	 * @see Auctioneer#setAuctionHouse(String)
	 * @return name of the auction house
	 */
	public String auctionHouse();
	
	/**
	 * Sets the name of the auction house to which this
	 * auctioneer belongs.
	 * @see Auctioneer#auctionHouse()
	 * @param named name of the auction house
	 */
	public void setAuctionHouse(String named);
}
