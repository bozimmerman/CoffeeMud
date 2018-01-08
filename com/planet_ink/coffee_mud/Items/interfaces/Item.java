package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.Readable;
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
 * The interface for all common items, and as a base for RawMaterial, armor, weapons, etc.
 *
 * @author Bo Zimmerman
 *
 */
public interface Item extends Rider, DBIdentifiable, Wearable, PhysicalAgent, Readable
{
	/**
	 * Where the item is located.  Either null for
	 * plain site (or contained on person), or will
	 * point to the container object
	 * @return The item in which  it is contained, or null.
	 */
	public Container container();
	/**
	 * Change  the container where the item is located.  Either null for
	 * plain site (or contained on person), or will
	 * point to the Container item.
	 * @see Container
	 * @param newLocation Container item in which this item is contained.
	 */
	public void setContainer(Container newLocation);
	/**
	 * If an item is in a container, which is in a container, etc, this will
	 * return the "highest" or ultimate container in which this item is located.
	 * If an item is in a container which is in plain view, it will simply
	 * return container().  If the item is not in a container, it will return itself.
	 * @param stopAtC if contained in this object, it will return this object.. default null.
	 * @see Container
	 * @return the highest level container in which  this item is found, or itself
	 */
	public Item ultimateContainer(Physical stopAtC);
	/**
	 * This method basically calls setContainer(null), and then removes this item
	 * from its owner().  It effectively removes the item from the map.  This is
	 * generally assumed to be a temporary condition.  To really destroy the item
	 * permanently, the destroy() method is used.  The unWear() method is also called.
	 * @see Container
	 */
	public void removeFromOwnerContainer();

	/**
	 * How many items this Item object represents.  When an item is Packaged, this
	 * method will return a number greater than 1, otherwise it always returns 1.
	 * @return the number of items represented by this object.
	 */
	public int numberOfItems();

	/**
	 * This method returns the calculated and expanded description of the properties
	 * of the item as would be discovered through the Identify spell.  It starts with
	 * its rawSecretIdentity() and adds to it any strings which the Ability objects
	 * contained in the Items effects list would generate.  An empty string means
	 * the item has no secret properties per se.
	 * @return a displayable string describing the secret properties of the item.
	 */
	public String secretIdentity();
	/**
	 * This method returns those secret properties of the item which are entered directly
	 * by the builder when the item is designed.  It is the string saved to the database,
	 * and is used by the secretIdentity() method to construct a full secret description
	 * of the Item.
	 * @return the string entered by the builder as the item secret properties or name.
	 */
	public String rawSecretIdentity();
	/**
	 * This method is used to change the string returned by rawSecretIdentity.  This string
	 * is saved to the database as the items secret properties desctiption.  The secretIdentity
	 * method uses this string to construct its full description.
	 * @param newIdentity the secret properties of this item.  Empty string means it has none.
	 */
	public void setSecretIdentity(String newIdentity);

	/**
	 * Whether the usesRemaining() number above is used to determine the percentage health of
	 * the item.  If this method returns false, then health or condition is irrelevant to this
	 * Item. If true is returned, then usesRemaining is a number from 100 to 0,  where 100 means
	 * perfect condition, and 0 means imminent disintegration.
	 * @return whether this item has a valid condition
	 */
	public boolean subjectToWearAndTear();
	/**
	 * Uses remaining is a general use numeric value whose meaning differs for different Item
	 * types.  For instance, Wands use it to represent charges, Weapons and Armor use it to
	 * represent Condition, Ammunition uses it to represent quantity, etc.
	 * @return the general numeric value of this field.
	 */
	public int usesRemaining();
	/**
	 * Sets the uses remaining field, which is a general numeric value whose meaning differs
	 * for different Item types.  See usesRemaining() method above for more information.
	 * @param newUses a new  general numeric value for this field.
	 */
	public void setUsesRemaining(int newUses);

	/**
	 * If this Item is current Ticking due to its having Behaviors or other properties which
	 * might grant it the ability to Tick, this method will cause that ticking to cease and
	 * desist.  This means that it will lose its periodic thread calls to its tick() method.
	 * This method also makes the item appear destroyed without actually being destroyed.
	 * The whole idea is to make the item 'dormant' for storage.
	 */
	public void stopTicking();
	/**
	 * The default value of the item, represented in the base CoffeeMud currency.  This
	 * method starts with baseGoldValue, which is a user-entered value, and adjusts
	 * according to magical enhancements and the condition of the  item.
	 * @return the adjusted value of the item in the base currency.
	 */
	public int value();
	/**
	 * The user/builder-entered value of the item, represented in base CoffeeMud currency.
	 * It is used as a basis for the value returned by the value() method.
	 * @return the raw user-entered value of item.
	 */
	public int baseGoldValue();
	/**
	 * Changes the base value of the item, represented in base CoffeeMud currency.
	 * The value is saved to the database, and is used by the value() method as a basis.
	 * @param newValue the new raw value of the item
	 */
	public void setBaseValue(int newValue);

	/**
	 * The resource code representing the material out of which this item is principally made.
	 * The resource codes are composed of an integer where the highest order bits represent
	 * the basic material type, and the lower order bits represent the specific material type.
	 * These codes are defined in RawMaterial.
	 * @see RawMaterial
	 * @return the RawMaterial code describing what this item is made of.
	 */
	public int material();
	/**
	 * Sets the resource code representing the material out of which this item is principally made.
	 * The resource codes are composed of an integer where the highest order bits represent
	 * the basic material type, and the lower order bits represent the specific material type.
	 * These codes are defined in RawMaterial interface.
	 * @param newValue the resource code
	 * @see RawMaterial
	 */
	public void setMaterial(int newValue);

	/**
	 * For a normal item, this method returns the same as phyStats().weight().  For
	 * a Container, it returns the weight of the container plus the recursive weight
	 * of all items in the container.
	 * @see Container
	 * @return the total weight of the item and any possible contents.
	 */
	public int recursiveWeight();
	/**
	 * The Room or MOB representing where this item is located.  Containers are handled
	 * by another pointer, container(), so those two methods be used together to determine
	 * where a given item is.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Item
	 * @return the mob or room where the item is located
	 */
	public ItemPossessor owner();
	/**
	 * Sets the Room or MOB representing where this item is located.  Containers are handled
	 * by another pointer, container(), so those two methods be used together to determine
	 * where a given item is.  This method is called by the addItem method on mobs
	 * and the addItem interface on Rooms.  Alone, this method is insufficient to properly
	 * determine an items location, so one of the two above should be called instead.
	 * @see com.planet_ink.coffee_mud.core.interfaces.ItemPossessor#addItem(Item)
	 * @param E the mob or room where the item is located
	 */
	public void setOwner(ItemPossessor E);
}
