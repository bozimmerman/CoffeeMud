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

import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.MOBS.interfaces.Auctioneer;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/**
 * Class for storing basic data about a specific auction in progress.
 * Handles both Live and Timed auction data.
 * 
 * @author Bo Zimmerman
 */
public interface AuctionData extends CMCommon
{
	/**
	 * Returns the number of days remaining in this
	 * auction.  The mobs are only for determining
	 * which calendar to use.  The first mobs'
	 * local calendar gets priority.
	 * @param mob the mob whose calendar to use
	 * @param mob2 the second mob whose calendar to use
	 * @return the number of days remaining in the auction
	 */
	public int daysRemaining(MOB mob, MOB mob2);

	/**
	 * Returns the number of days ellapsed in this
	 * auction.  The mobs are only for determining
	 * which calendar to use.  The first mobs'
	 * local calendar gets priority.
	 * @param mob the mob whose calendar to use
	 * @param mob2 the second mob whose calendar to use
	 * @return the number of days ellapsed in the auction
	 */
	public int daysEllapsed(MOB mob, MOB mob2);

	/**
	 * Get the item being auctioned.
	 * @see AuctionData#setAuctionedItem(Item)
	 * @return the item being auctioned.
	 */
	public Item getAuctionedItem();

	/**
	 * Set the item being auctioned.
	 * @see AuctionData#getAuctionedItem()
	 * @param auctionedItem the item being auctioned.
	 */
	public void setAuctionedItem(Item auctionedItem);

	/**
	 * Get the mob auctioning the item
	 * @see AuctionData#setAuctioningMob(MOB)
	 * @return the mob auctioning the item
	 */
	public MOB getAuctioningMob();

	/**
	 * Set the mob auctioning the item
	 * @see AuctionData#getAuctioningMob()
	 * @param auctioningM the mob auctioning the item
	 */
	public void setAuctioningMob(MOB auctioningM);

	/**
	 * Get the mob who is the high bidder in the auction.
	 * @see AuctionData#setHighBidderMob(MOB)
	 * @return the mob who is the high bidder in the auction.
	 */
	public MOB getHighBidderMob();

	/**
	 * Set the mob who is the high bidder in the auction.
	 * @see AuctionData#getHighBidderMob()
	 * @param highBidderM the mob who is the high bidder in the auction.
	 */
	public void setHighBidderMob(MOB highBidderM);

	/**
	 * Get the currency the auction is being held in.
	 * @see AuctionData#setCurrency(String)
	 * @return the currency the auction is being held in.
	 */
	public String getCurrency();

	/**
	 * Set the currency the auction is being held in.
	 * @see AuctionData#getCurrency()
	 * @param currency the currency the auction is being held in.
	 */
	public void setCurrency(String currency);

	/**
	 * Get the current high bid in the auction
	 * @see AuctionData#setHighBid(double)
	 * @return the current high bid in the auction
	 */
	public double getHighBid();
	
	/**
	 * Set the current high bid in the auction
	 * @see AuctionData#getHighBid()
	 * @param highBid the current high bid in the auction
	 */
	public void setHighBid(double highBid);

	/**
	 * Get the current bid in the auction
	 * @see AuctionData#setBid(double)
	 * @return the current bid in the auction
	 */
	public double getBid();

	/**
	 * Set the current bid in the auction
	 * @see AuctionData#getBid()
	 * @param bid the current bid in the auction
	 */
	public void setBid(double bid);

	/**
	 * Get the current buy-out price for this auction. 
	 * @see AuctionData#setBuyOutPrice(double)
	 * @return the current buy-out price for this auction.
	 */
	public double getBuyOutPrice();

	/**
	 * Set the current buy-out price for this auction. 
	 * @see AuctionData#getBuyOutPrice()
	 * @param buyOutPrice the current buy-out price for this auction.
	 */
	public void setBuyOutPrice(double buyOutPrice);

	/**
	 * Get the current auction State
	 * @see AuctionData#setAuctionState(int)
	 * @return the current auction State
	 */
	public int getAuctionState();

	/**
	 * Set the current auction State
	 * @see AuctionData#getAuctionState()
	 * @param state the current auction State
	 */
	public void setAuctionState(int state);

	/**
	 * Get the tick down timer to check stuff
	 * @see AuctionData#setAuctionTickDown(long)
	 * @return the tick down timer to check stuff
	 */
	public long getAuctionTickDown();

	/**
	 * Set the tick down timer to check stuff
	 * @see AuctionData#getAuctionTickDown()
	 * @param tickDown the tick down timer to check stuff
	 */
	public void setAuctionTickDown(long tickDown);

	/**
	 * Get the start time of this auction
	 * @see AuctionData#setStartTime(long)
	 * @return the start time of this auction
	 */
	public long getStartTime();

	/**
	 * Set the start time of this auction
	 * @see AuctionData#getStartTime()
	 * @param start the start time of this auction
	 */
	public void setStartTime(long start);

	/**
	 * Get the auction database key for this auction
	 * @see AuctionData#setAuctionDBKey(String)
	 * @return the auction database key for this auction
	 */
	public String getAuctionDBKey();

	/**
	 * Set the auction database key for this auction
	 * @see AuctionData#getAuctionDBKey()
	 * @param auctionDBKey the auction database key for this auction
	 */
	public void setAuctionDBKey(String auctionDBKey);
}
