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

import java.util.Map;
import java.util.Vector;

/*
   Copyright 2005-2018 Bo Zimmerman

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
 * A PostOffice is a kind of shopkeeper that belongs to a "chain" which
 * shares access to a common store of post office boxes to hold items
 * for players.  Post officers simulate delivery by transferring the
 * items given to them into the appropriate boxes.  Players must then
 * go to a post office in the same chain to retrieve items delivered
 * to them.  
 * Post offices can serve an entire clan, or a single player.
 * They can handle several different types of charges, including COD.
 * 
 * @author Bo Zimmerman
 */
public interface PostOffice extends ShopKeeper
{
	/**
	 * When the given mob tries to send or withdraw a package, this method is 
	 * called to get the proper FROM address, which is either the mob themselves 
	 * or their clan, if they are (optionally) permitted by their rank.
	 * If checked is true, and the mob does NOT have clan privileges, then an
	 * error message is given to the mob and null is returned.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.Function
	 * @param mob the mob who is trying to send the package
	 * @param func either Clan.Function.WITHDRAW or Clan.FUNCTION.DEPOSIT
	 * @param checked true if the mob must have clan privileges, false if not.
	 * @return the mobs name, their clan name, or null
	 */
	public String getSenderName(MOB mob, Clan.Function func, boolean checked);

	/**
	 * Low level method to add an item directly to a postal box.
	 * @see PostOffice#delFromBox(String, Item)
	 * @see PostOffice#emptyBox(String)
	 * @param boxName the box name, such as a player or clan name
	 * @param thisThang the item to add to the postal box
	 * @param from player or clan the item is from
	 * @param to player or clan the item is to
	 * @param holdTime when the package was sent, real time, in milliseconds
	 * @param COD the amount of money which must be paid to withdraw the item
	 */
	public void addToBox(String boxName, Item thisThang, String from, String to, long holdTime, double COD);

	/**
	 * Low level method to remove an item from a postal box
	 * @see PostOffice#addToBox(String, Item, String, String, long, double)
	 * @see PostOffice#emptyBox(String)
	 * @param boxName the box name, such as a player or clan name
	 * @param thisThang the item to remove from the postal box
	 * @return true if the item was removed, or false otherwise
	 */
	public boolean delFromBox(String boxName, Item thisThang);

	/**
	 * Removes all items from the given post office box
	 * @see PostOffice#addToBox(String, Item, String, String, long, double)
	 * @see PostOffice#delFromBox(String, Item)
	 * @param boxName the box name, such as a player or clan name
	 */
	public void emptyBox(String boxName);

	/**
	 * Searches the given postal box for an item with the given
	 * name, or one with the given string as a substring.
	 * @param boxName the player or clan name of the postal box
	 * @param likeThis the search string for the item name
	 * @return the item found, or null
	 */
	public Item findBoxContents(String boxName, String likeThis);
	
	/**
	 * Returns a mapping of postal branches in this chain to forwarding 
	 * addresses, for the given postal box.
	 * @see PostOffice#deleteBoxHere(String)
	 * @see PostOffice#createBoxHere(String, String)
	 * @param boxName the player or clan name of the box
	 * @return a mapping of postal branches to forwarding addresses.
	 */
	public Map<String, String> getOurOpenBoxes(String boxName);

	/**
	 * Creates a new post office box for the given player or clan
	 * name, with the given forwarding address.
	 * @see PostOffice#deleteBoxHere(String)
	 * @see PostOffice#getOurOpenBoxes(String)
	 * @param boxName the player or clan name of the new box
	 * @param forward the player or clan name of the forwarding
	 */
	public void createBoxHere(String boxName, String forward);

	/**
	 * Removes the given post office box for the given player or
	 * clan name.
	 * @see PostOffice#createBoxHere(String, String)
	 * @see PostOffice#getOurOpenBoxes(String)
	 * @param boxName the player or clan name of the new box
	 */
	public void deleteBoxHere(String boxName);

	/**
	 * Parses an postal package entry, which is formatted
	 * as item xml preceded by various semicolon-delimited
	 * numbers and data. 
	 * @see PostOffice.MailPiece
	 * @param data the postal package formatted string
	 * @return the MailPiece object
	 */
	public MailPiece parsePostalItemData(String data);

	/**
	 * Returns the postal chain, a string shared by all post 
	 * offices that deal with the same postal boxes and
	 * customers, able to send mail to each other.
	 * @see PostOffice#setPostalChain(String)
	 * @return the name of the postal chain
	 */
	public String postalChain();

	/**
	 * Sets the postal chain, a string shared by all post 
	 * offices that deal with the same postal boxes and
	 * customers, able to send mail to each other.
	 * @see PostOffice#postalChain()
	 * @param name  the name of the postal chain
	 */
	public void setPostalChain(String name);

	/**
	 * Returns the branch of the postal chain that this specific
	 * postoffice employee belongs to.  Typically the extended
	 * room id, this is a unique identifier for this branch of
	 * the postal chain.  Each PostOffice should be unique!
	 * @see PostOffice#findProperBranch(String)
	 * @return the branch of the postal chain
	 */
	public String postalBranch(); // based on individual shopkeeper

	/**
	 * Returns the proper branch of this postal chain to which to
	 * deliver packages belonging to the given mob or clan name.
	 * @see PostOffice#postalBranch()
	 * @param name the player or clan name receiving the package
	 * @return the best branch in this postal chain to deliver to
	 */
	public String findProperBranch(String name);

	/**
	 * Gets the minimum postage to send a package from this branch.
	 * @see PostOffice#setMinimumPostage(double)
	 * @return the minimum postage to send a package from this branch.
	 */
	public double minimumPostage();

	/**
	 * Sets the minimum postage to send a package from this branch.
	 * @see PostOffice#minimumPostage()
	 * @param d the minimum postage to send a package from this branch.
	 */
	public void setMinimumPostage(double d);

	/**
	 * Gets the postage charged per pound after the first, to send
	 * packages from this branch.
	 * @see PostOffice#setPostagePerPound(double)
	 * @return the postage charged per pound after the first
	 */
	public double postagePerPound();

	/**
	 * Sets the postage charged per pound after the first, to send
	 * packages from this branch.
	 * @see PostOffice#postagePerPound()
	 * @param d the postage charged per pound after the first
	 */
	public void setPostagePerPound(double d);

	/**
	 * Gets the fee charged per pound per mud month to hold a 
	 * package in a postal box at this branch.
	 * @see PostOffice#setHoldFeePerPound(double)
	 * @return the fee charged per pound per mud month
	 */
	public double holdFeePerPound();

	/**
	 * Sets the fee charged per pound per mud month to hold a 
	 * package in a postal box at this branch.
	 * @see PostOffice#holdFeePerPound()
	 * @param d the fee charged per pound per mud month
	 */
	public void setHoldFeePerPound(double d);

	/**
	 * Gets the fee to open a new postal box at this branch.
	 * @see PostOffice#setFeeForNewBox(double)
	 * @return the fee to open a new postal box at this branch.
	 */
	public double feeForNewBox();

	/**
	 * Sets the fee to open a new postal box at this branch.
	 * @see PostOffice#feeForNewBox()
	 * @param d the fee to open a new postal box at this branch.
	 */
	public void setFeeForNewBox(double d);

	/**
	 * Returns the maximum number of mud-months that a package
	 * will be held before it gets put on sale at this branch.
	 * @see PostOffice#setMaxMudMonthsHeld(int)
	 * @return the maximum number of mud-months to hold
	 */
	public int maxMudMonthsHeld();

	/**
	 * Sets the maximum number of mud-months that a package
	 * will be held before it gets put on sale at this branch.
	 * @see PostOffice#setMaxMudMonthsHeld(int)
	 * @param months the maximum number of mud-months to hold
	 */
	public void setMaxMudMonthsHeld(int months);

	/**
	 * Represents a complete mail package/letter, postal
	 * object. Holding all relevant info about a package
	 * from one person to another.
	 * @author Bo Zimmerman
	 *
	 */
	public static class MailPiece
	{
		public String	from	= "";
		public String	to		= "";
		public String	time	= "";
		public String	cod		= "";
		public String	classID	= "";
		public String	xml		= "";
	}

}
