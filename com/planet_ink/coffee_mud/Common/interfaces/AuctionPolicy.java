package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.List;
/*
   Copyright 2015-2018 Bo Zimmerman

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

import com.planet_ink.coffee_mud.MOBS.interfaces.Auctioneer;

/**
 * Class for storing basic Auction House policies.  
 * @author Bo Zimmerman
 */
public interface AuctionPolicy extends CMCommon
{
	/**
	 * Gets the flat fee in base currency to list an 
	 * item of any sort at the auction house for a timed
	 * auction.
	 * @see AuctionPolicy#setTimedListingPrice(double)
	 * @return the flat fee in base currency to list
	 */
	public double timedListingPrice();

	/**
	 * Sets the flat fee in base currency to list an 
	 * item of any sort at the auction house for a timed
	 * auction.
	 * @see AuctionPolicy#timedListingPrice()
	 * @param d the flat fee in base currency to list
	 */
	public void setTimedListingPrice(double d);

	/**
	 * Gets the percent of an items value, per day,
	 * to charge to list an item. 0.0-1.0 for a timed
	 * auction.
	 * @see AuctionPolicy#setTimedListingPct(double)
	 * @return  the percent of an items value, per day
	 */
	public double timedListingPct();
	
	/**
	 * Sets the percent of an items value, per day,
	 * to charge to list an item. 0.0-1.0for a timed
	 * auction.
	 * @see AuctionPolicy#timedListingPct()
	 * @param d the percent of an items value, per day
	 */
	public void setTimedListingPct(double d);

	/**
	 * Gets the percent of an items final value
	 * to take off the winning bid for the house
	 * for a timed auction. 0.0-1.0
	 * @see AuctionPolicy#setTimedFinalCutPct(double) 
	 * @return the percent of an items final value
	 */
	public double timedFinalCutPct();
	
	/**
	 * Gets the percent of an items final value
	 * to take off the winning bid for the house.
	 * for a timed auction. 0.0-1.0
	 * @see AuctionPolicy#timedFinalCutPct() 
	 * @param d the percent of an items final value
	 */
	public void setTimedFinalCutPct(double d);

	/**
	 * Gets the maximum number of game-days that 
	 * an auction can continue.
	 * @see AuctionPolicy#setMaxTimedAuctionDays(int)
	 * @return the maximum number of game-days
	 */
	public int maxTimedAuctionDays();
	
	/**
	 * Sets the maximum number of game-days that 
	 * an auction can continue.
	 * @see AuctionPolicy#maxTimedAuctionDays()
	 * @param d the maximum number of game-days
	 */
	public void setMaxTimedAuctionDays(int d);

	/**
	 * Gets the minimum number of game-days that 
	 * an auction can continue.
	 * @see AuctionPolicy#setMinTimedAuctionDays(int)
	 * @return the minimum number of game-days
	 */
	public int minTimedAuctionDays();
	
	/**
	 * Sets the minimum number of game-days that 
	 * an auction can continue.
	 * @see AuctionPolicy#minTimedAuctionDays()
	 * @param d the minimum number of game-days
	 */
	public void setMinTimedAuctionDays(int d);

	/**
	 * Gets the flat fee in base currency to list an 
	 * item of any sort at the auction house for a live
	 * auction.
	 * @see AuctionPolicy#setLiveListingPrice(double)
	 * @return the flat fee in base currency to list
	 */
	public double liveListingPrice();

	/**
	 * Sets the flat fee in base currency to list an 
	 * item of any sort at the auction house for a live
	 * auction.
	 * @see AuctionPolicy#liveListingPrice()
	 * @param d the flat fee in base currency to list
	 */
	public void setLiveListingPrice(double d);

	/**
	 * Gets the percent of an items final value
	 * to take off the winning bid for the house
	 * for a live auction. 0.0-1.0
	 * @see AuctionPolicy#setLiveFinalCutPct(double) 
	 * @return the percent of an items final value
	 */
	public double liveFinalCutPct();
	
	/**
	 * Gets the percent of an items final value
	 * to take off the winning bid for the house.
	 * for a live auction. 0.0-1.0
	 * @see AuctionPolicy#liveFinalCutPct() 
	 * @param d the percent of an items final value
	 */
	public void setLiveFinalCutPct(double d);
	
	/**
	 * Alters this policy to take account of the policies
	 * of the policies of a full timed auctioneer.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.Auctioneer 
	 * @param auction the auctioneer
	 */
	public void mergeAuctioneerPolicy(Auctioneer auction);
}
