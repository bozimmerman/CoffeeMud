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
import java.util.Vector;

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
 * An interface for objects capable of being a shopkeeper, usually a MOB or an Ability.
 * @author Bo Zimmerman
 *
 */
public interface ShopKeeper extends Environmental, Economics
{
	/** shopkeeper type constant, means they buy anything */
	public final static int DEAL_ANYTHING=0;
	/** shopkeeper type constant, means they buy items not covered by other constants */
	public final static int DEAL_GENERAL=1;
	/** shopkeeper type constant, means they buy armor */
	public final static int DEAL_ARMOR=2;
	/** shopkeeper type constant, means they buy magic items*/
	public final static int DEAL_MAGIC=3;
	/** shopkeeper type constant, means they buy weapons*/
	public final static int DEAL_WEAPONS=4;
	/** shopkeeper type constant, means they buy pets*/
	public final static int DEAL_PETS=5;
	/** shopkeeper type constant, means they buy leather stuff*/
	public final static int DEAL_LEATHER=6;
	/** shopkeeper type constant, means they buy only what they are told to sell*/
	public final static int DEAL_INVENTORYONLY=7;
	/** shopkeeper type constant, means they train players in skills they sell*/
	public final static int DEAL_TRAINER=8;
	/** shopkeeper type constant, means they cast spells they sell on players*/
	public final static int DEAL_CASTER=9;
	/** shopkeeper type constant, means they buy jewelry*/
	public final static int DEAL_JEWELLER=10;
	/** shopkeeper type constant, means they buy potions*/
	public final static int DEAL_ALCHEMIST=11;
	/** shopkeeper type constant, means they are a banker for players, and implement the Banker interface*/
	public final static int DEAL_BANKER=12;
	/** shopkeeper type constant, means they buy and sell property in their area to players*/
	public final static int DEAL_LANDSELLER=13;
	/** shopkeeper type constant, means they buy electronics items*/
	public final static int DEAL_ANYTECHNOLOGY=14;
	/** shopkeeper type constant, means they buy and sell property in their area to clans*/
	public final static int DEAL_CLANDSELLER=15;
	/** shopkeeper type constant, means they buy and sell raw foodstuff*/
	public final static int DEAL_FOODSELLER=16;
	/** shopkeeper type constant, means they buy and sell raw meats*/
	public final static int DEAL_BUTCHER=17;
	/** shopkeeper type constant, means they buy and sell raw non-meat foodstuffs*/
	public final static int DEAL_GROWER=18;
	/** shopkeeper type constant, means they buy raw leathers and hides*/
	public final static int DEAL_HIDESELLER=19;
	/** shopkeeper type constant, means they buy raw lumber*/
	public final static int DEAL_LUMBERER=20;
	/** shopkeeper type constant, means they buy raw metals*/
	public final static int DEAL_METALSMITH=21;
	/** shopkeeper type constant, means they buy raw stones*/
	public final static int DEAL_STONEYARDER=22;
	/** shopkeeper type constant, means they are a banker for clans, and implement the Banker interface*/
	public final static int DEAL_CLANBANKER=23;
	/** shopkeeper type constant, means they sell InnKeys*/
	public final static int DEAL_INNKEEPER=24;
	/** shopkeeper type constant, means they buy and sell SpaceShip areas to players*/
	public final static int DEAL_SHIPSELLER=25;
	/** shopkeeper type constant, means they buy and sell SpaceShip areas to clans*/
	public final static int DEAL_CSHIPSELLER=26;
	/** shopkeeper type constant, means they buy and sell intelligent mobs as slaves*/
	public final static int DEAL_SLAVES=27;
	/** shopkeeper type constant, means they handle mail for players, and implement the Postman interface*/
	public final static int DEAL_POSTMAN=28;
	/** shopkeeper type constant, means they handle mail for clans, and implement the Postman interface*/
	public final static int DEAL_CLANPOSTMAN=29;
	/** shopkeeper type constant, means they handle auctions, and implement the Auctioneer interface*/
	public final static int DEAL_AUCTIONEER=30;
	/** shopkeeper type constant, means they buy and sell musical instruments*/
	public final static int DEAL_INSTRUMENTS=31;
	/** shopkeeper type constant, means they buy and sell any books*/
	public final static int DEAL_BOOKS=32;
	/** shopkeeper type constant, means they buy and sell any readables*/
	public final static int DEAL_READABLES=33;

	/** shopkeeper integer sets denoting the DEAL_* constants which conflict with each other */
	public final static int[][] DEAL_CONFLICTS={
		{DEAL_POSTMAN,DEAL_CLANPOSTMAN},
		{DEAL_CLANBANKER,DEAL_BANKER},
		{DEAL_SHIPSELLER,DEAL_CSHIPSELLER,DEAL_LANDSELLER,DEAL_CLANDSELLER},
		{DEAL_TRAINER,DEAL_CASTER},
	};

	/** A list of strings describing the DEAL_* constants, in their numeric value order. */
	public final static String[] DEAL_DESCS=
	{
		"ANYTHING","GENERAL","ARMOR","MAGIC","WEAPONS",
		"PETS","LEATHER","INVENTORY ONLY","TRAINER",
		"CASTER","JEWELRY","POTIONS","BANKER","LAND",
		"ANY TECHNOLOGY","CLAN LAND","FOODS","MEATS",
		"VEGETABLES","HIDES","LUMBER","METALS","ROCKS",
		"CLAN BANKER", "INN KEEPER", "SHIP SELLER",
		"CLAN SHIP SELLER", "SLAVES", "POSTMAN", "CLAN POSTMAN",
		"AUCTIONEER","INSTRUMENTS","BOOKS","READABLES"
	};

	/**
	 * This class represents a given price for a given item in the shopkeepers inventory. It is usually
	 * calculated for a given buyer.
	 */
	public static class ShopPrice
	{
		/** the price of the item in base currency gold */
		public double absoluteGoldPrice=0.0;
		/** the number of experience points required to purchase the item */
		public int experiencePrice=0;
		/** the number of quest points required to purchase the item */
		public int questPointPrice=0;
	}

	/**
	 * the CoffeeShop method to access the shopkeepers store of goods
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop
	 * @return the CoffeeShop object
	 */
	public CoffeeShop getShop();

	/**
	 * Returns the ShopKeeper DEAL_* mask describing what is sold or bought by this ShopKeeper
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_DESCS
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#setWhatIsSoldMask(long)
	 * @return the dealer type constants to the 2nd power, shifted 8 bits left
	 */
	public long getWhatIsSoldMask();
	/**
	 * Returns whether the given type of good is sold by this shopkeeper.
	 * @param deal  the ShopKeeper DEAL_* constant describing what is sold or bought by this ShopKeeper
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_DESCS
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#getWhatIsSoldMask()
	 * @return true if the shopkeeper will make such a deal
	 */
	public boolean isSold(int deal);
	/**
	 * Sets the encoded ShopKeeper DEAL_* constants describing what is sold or bought by this ShopKeeper
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_DESCS
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#addSoldType(int)
	 * @param newSellCode the dealer type constants to the 2nd power, shifted 8 bits left
	 */
	public void setWhatIsSoldMask(long newSellCode);

	/**
	* Adds the ShopKeeper DEAL_* constants describing what is sold or bought by this ShopKeeper
	* to the existing shopkeeper mask.  A value of 0 will clear the whole mask.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#DEAL_DESCS
	 * @see com.planet_ink.coffee_mud.core.interfaces.ShopKeeper#isSold(int)
	* @param dealType the ShopKeeper DEAL_* constants describing what is sold or bought by this ShopKeeper
	*/
	public void addSoldType(int dealType);

	/**
	 * Based on the value of this ShopKeepers whatIsSold() method, this will return a displayable string
	 * describing that type.
	 * @see ShopKeeper#isSold(int)
	 * @return a description of the whatIsSold() code
	 */
	public String storeKeeperString();
	/**
	 * Returns whether this ShopKeeper deals in the type of item passed in.  The determination is based
	 * on the whatIsSold() code.
	 * @see ShopKeeper#isSold(int)
	 * @param thisThang the item to determine if the shopkeeper deals  in
	 * @return whether the shopkeeper deals in the type of item passed in
	 */
	public boolean doISellThis(Environmental thisThang);
	
	/**
	 * Sets the zapper mask which applies to items to determine whether they are bought and solid
	 * by this shopkeeper.
	 * @see ShopKeeper#isSold(int)
	 * @see ShopKeeper#getWhatIsSoldZappermask()
	 * @see MaskingLibrary
	 * @param newSellMask the item zappermask
	 */
	public void setWhatIsSoldZappermask(String newSellMask);

	/**
	 * Returns the zapper mask which applies to items to determine whether they are bought and solid
	 * by this shopkeeper.
	 * @see ShopKeeper#isSold(int)
	 * @see ShopKeeper#setWhatIsSoldZappermask(String)
	 * @see MaskingLibrary
	 * @return the item zappermask
	 */
	public String getWhatIsSoldZappermask();
}
