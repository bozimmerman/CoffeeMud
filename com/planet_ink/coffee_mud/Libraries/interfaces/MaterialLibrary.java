package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Libraries.Socials;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary.DeadResourceRecord;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2006-2025 Bo Zimmerman

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
 * At the center of CoffeeMuds crafting system are raw resources.  These
 * are represented as item objects of a particular material code or
 * resource code.  Normal non-resource items are also made of the same
 * material/resource codes.   Material/Resource codes are numbers which
 * are composed of a resource type and a material type, with the material
 * type being a high-order-bitmask over the ordinal resource type.
 *
 * This Library handles raw resource item object and more, mostly supporting
 * the gathering and crafting systems.
 *
 * @author Bo Zimmerman
 *
 */
public interface MaterialLibrary extends CMLibrary
{
	/**
	 * Searches for the given material type, and, if
	 * found, returns the ordinal value of the material
	 * code.
	 *
	 * @see MaterialLibrary#findMaterialCode(String, boolean)
	 * @see MaterialLibrary#findResourceCode(String, boolean)
	 *
	 * @param s the possible material type
	 * @return the ordinal value of the material
	 */
	public int findMaterialRelativeInt(String s);

	/**
	 * Searches for the given material type, and, if
	 * found, returns the material code.
	 *
	 * @see MaterialLibrary#findResourceCode(String, boolean)
	 * @see MaterialLibrary#findMaterialRelativeInt(String)
	 *
	 * @param s the possible material type
	 * @param exact true for exact match only, false for startswith
	 * @return the material code
	 */
	public int findMaterialCode(String s, boolean exact);


	/**
	 * Searches for the given resource code by name, and, if
	 * found, returns the resource code.
	 *
	 * @see MaterialLibrary#findMaterialCode(String, boolean)
	 * @see MaterialLibrary#findMaterialRelativeInt(String)
	 *
	 * @param s the possible resource codes name
	 * @param exact true for exact match only, false for startswith
	 * @return the resource code
	 */
	public int findResourceCode(String s, boolean exact);

	/**
	 * Returns the name of the given resource type code
	 *
	 * @see MaterialLibrary#getMaterialDesc(int)
	 *
	 * @param resourceCode the given resource type code
	 * @return the name of the given resource type code
	 */
	public String getResourceDesc(int resourceCode);

	/**
	 * Returns the name of the given material type code
	 *
	 * @see MaterialLibrary#getResourceDesc(int)
	 *
	 * @param materialCode the material type code
	 * @return the name of the material type code
	 */
	public String getMaterialDesc(int materialCode);

	/**
	 * Creates a resource item of one pound.
	 *
	 * @see MaterialLibrary#makeItemResource(int, String)
	 * @see MaterialLibrary#makeResource(int, String, boolean, String, String)
	 *
	 * @param type the resource code
	 * @return the newly created resource item
	 */
	public Item makeItemResource(int type);

	/**
	 * Creates a resource item of one pound.
	 *
	 * @see MaterialLibrary#makeItemResource(int)
	 * @see MaterialLibrary#makeResource(int, String, boolean, String, String)
	 *
	 * @param type the resource code
	 * @param subType "" or the subType of the resource
	 * @return the newly created resource item
	 */
	public Item makeItemResource(int type, String subType);

	/**
	 * Creates a resource item, or possibly a mob that represents the resource.
	 *
	 * @see MaterialLibrary#makeItemResource(int)
	 * @see MaterialLibrary#makeItemResource(int, String)
	 *
	 * @param myResource the full resource code
	 * @param localeCode null, the string representation of the rooms domain type source
	 * @param noAnimals true to only return items, false to possibly return a mob
	 * @param fullName null, or the secret identity name
	 * @param subType null, "", or the resource subType
	 * @return the resource item or mob, created from scratch
	 */
	public PhysicalAgent makeResource(int myResource, String localeCode, boolean noAnimals, String fullName, String subType);

	/**
	 * Returns a random resource code of the given material type
	 *
	 * @param material the material type code
	 * @return a random resource code of that material
	 */
	public int getRandomResourceOfMaterial(int material);

	/**
	 * Searches the given room for an amount of specified qualifying resource items in the
	 * given list and in the given container, destroys that many pounds of the resource,
	 * and then returns all kinds of statistics about what was destroyed in the form of
	 * a DeadResourceRecord
	 *
	 * @see MaterialLibrary.DeadResourceRecord
	 * @see MaterialLibrary#destroyResourcesValue(List, int, int, int, Item, Container)
	 * @see MaterialLibrary#destroyResourcesAmt(List, int, int, String, Container)
	 * @see MaterialLibrary#destroyResources(Room, int, int, int, int, int)
	 *
	 * @param R the possessor of raw material resource items
	 * @param howMuch the amount to destroy, in weight
	 * @param finalMaterial the resource code of the material to destroy
	 * @param finalSubHash 0 or the hash of the subtype
	 * @param otherMaterial -1, 0, or the OTHER resource code of the material to destroy
	 * @param finalOtherHash 0 or the hash of the subtype
	 * @return the dead resource record of statistics
	 */
	public DeadResourceRecord destroyResources(final Room R, final int howMuch, final int finalMaterial,
											   final int finalSubHash, final int otherMaterial, final int finalOtherHash);

	/**
	 * Searches the given mob for an amount of specified qualifying resource items in the
	 * given list and in NO container, destroys that many pounds of the resource,
	 * and then returns the value of what was destroyed.
	 *
	 * @see MaterialLibrary#destroyResourcesValue(Room, int, int, int, int, int)
	 * @see MaterialLibrary#destroyResourcesValue(MOB,  int, int, int, int, int)
	 * @see MaterialLibrary#destroyResourcesValue(List, int, int, int, Item, Container)
	 * @see MaterialLibrary#destroyResourcesAmt(List, int, int, String, Container)
	 * @see MaterialLibrary#destroyResources(Room, int, int, int, int, int)
	 *
	 * @param M the possessor of raw material resource items
	 * @param howMuch the amount to destroy, in weight
	 * @param finalMaterial the resource code of the material to destroy
	 * @param finalSubHash 0 or the hash of the subtype
	 * @param otherMaterial -1, 0, or the OTHER resource code of the material to destroy
	 * @param otherSubHash 0 or the hash of the subtype
	 * @return the value of what was destroyed
	 */
	public int destroyResourcesValue(final MOB M, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash);

	/**
	 * Searches the given room for an amount of specified qualifying resource items in the
	 * given list and in NO container, destroys that many pounds of the resource,
	 * and then returns the value of what was destroyed.
	 *
	 * @see MaterialLibrary#destroyResourcesValue(Room, int, int, int, int, int)
	 * @see MaterialLibrary#destroyResourcesValue(MOB, int, int, int, int, int)
	 * @see MaterialLibrary#destroyResourcesValue(List, int, int, int, Item, Container)
	 * @see MaterialLibrary#destroyResourcesAmt(List, int, int, String, Container)
	 * @see MaterialLibrary#destroyResources(Room, int, int, int, int, int)
	 *
	 * @param R the possessor of raw material resource items
	 * @param howMuch the amount to destroy, in weight
	 * @param finalMaterial the resource code of the material to destroy
	 * @param finalSubHash 0 or the hash of the subtype
	 * @param otherMaterial -1, 0, or the OTHER resource code of the material to destroy
	 * @param otherSubHash 0 or the hash of the subtype
	 * @return the value of what was destroyed
	 */
	public int destroyResourcesValue(final Room R, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash);

	/**
	 * Searches the given list for an amount of specified qualifying resource items in the
	 * given list and in the given container, destroys that many pounds of the resource,
	 * and then returns the value of what was destroyed.
	 *
	 * @see MaterialLibrary#destroyResourcesValue(Room, int, int, int, int, int)
	 * @see MaterialLibrary#destroyResourcesValue(MOB, int, int, int, int, int)
	 * @see MaterialLibrary#destroyResourcesValue(List, int, int, int, Item, Container)
	 * @see MaterialLibrary#destroyResourcesAmt(List, int, int, String, Container)
	 * @see MaterialLibrary#destroyResources(Room, int, int, int, int, int)
	 *
	 * @param V the list of raw material resource items
	 * @param howMuch the amount to destroy, in weight
	 * @param finalMaterial the resource code of the material to destroy
	 * @param otherMaterial -1, 0, or the OTHER resource code of the material to destroy
	 * @param never null, or a specific item to never destroy no matter what
	 * @param C the container in which the items must be found
	 * @return the value of what was destroyed
	 */
	public int destroyResourcesValue(List<Item> V, int howMuch, int finalMaterial, int otherMaterial, Item never, Container C);

	/**
	 * Searches the given mob for an amount of specified qualifying resource items in the
	 * given list and in the given container, destroys that many pounds of the resource,
	 * and then returns the amount destroyed.
	 *
	 * @see MaterialLibrary#destroyResourcesAmt(List, int, int, String, Container)
	 * @see MaterialLibrary#destroyResourcesAmt(Room, int, int, String, Container)
	 * @see MaterialLibrary#destroyResourcesValue(List, int, int, int, Item, Container)
	 * @see MaterialLibrary#destroyResources(Room, int, int, int, int, int)
	 *
	 * @param E the possessor of raw material resource items
	 * @param howMuch the amount to destroy, in weight
	 * @param resourceCode the resource code of the material to destroy
	 * @param subType null, "", or the sub type of the resource
	 * @param C the container in which the items must be found
	 * @return the amount destroyed
	 */
	public int destroyResourcesAmt(MOB E, int howMuch, int resourceCode, String subType, Container C);

	/**
	 * Searches the given room for an amount of specified qualifying resource items in the
	 * given list and in the given container, destroys that many pounds of the resource,
	 * and then returns the amount destroyed.
	 *
	 * @see MaterialLibrary#destroyResourcesAmt(List, int, int, String, Container)
	 * @see MaterialLibrary#destroyResourcesAmt(MOB, int, int, String, Container)
	 * @see MaterialLibrary#destroyResourcesValue(List, int, int, int, Item, Container)
	 * @see MaterialLibrary#destroyResources(Room, int, int, int, int, int)
	 *
	 * @param E the possessor of raw material resource items
	 * @param howMuch the amount to destroy, in weight
	 * @param resourceCode the resource code of the material to destroy
	 * @param subType null, "", or the sub type of the resource
	 * @param C the container in which the items must be found
	 * @return the amount destroyed
	 */
	public int destroyResourcesAmt(Room E, int howMuch, int resourceCode, String subType, Container C);

	/**
	 * Searches the given mob for an amount of specified qualifying resource items in the
	 * given list and in the given container, destroys that many pounds of the resource,
	 * and then returns the amount destroyed.
	 *
	 * @see MaterialLibrary#destroyResourcesAmt(MOB, int, int, String, Container)
	 * @see MaterialLibrary#destroyResourcesAmt(Room, int, int, String, Container)
	 * @see MaterialLibrary#destroyResourcesValue(List, int, int, int, Item, Container)
	 * @see MaterialLibrary#destroyResources(Room, int, int, int, int, int)
	 *
	 * @param V the list of raw material resource items
	 * @param howMuch the amount to destroy, in weight
	 * @param finalMaterial the resource code of the material to destroy
	 * @param subType null, "", or the sub type of the resource
	 * @param C the container in which the items must be found
	 * @return the amount destroyed
	 */
	public int destroyResourcesAmt(List<Item> V, int howMuch, int finalMaterial, String subType, Container C);

	/**
	 * Searches the given room for specified qualifying resource items.
	 * The given string is either a full resource name, or a ! followed
	 * by a material type name, with optional subtype in parenthesis.
	 * If a resource name is given, this will return the first matching
	 * resource.  If a material type is given, this will return the
	 * largest example of that material of a particular resource.
	 *
	 * @see MaterialLibrary#fetchFoundOtherEncoded(MOB, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param otherRequired the resource name, or !material type
	 * @return the found raw material item, or null
	 */
	public RawMaterial fetchFoundOtherEncoded(Room E, String otherRequired);

	/**
	 * Searches the given mob for specified qualifying resource items.
	 * The given string is either a full resource name, or a ! followed
	 * by a material type name, with optional subtype in parenthesis.
	 * If a resource name is given, this will return the first matching
	 * resource.  If a material type is given, this will return the
	 * largest example of that material of a particular resource.
	 *
	 * @see MaterialLibrary#fetchFoundOtherEncoded(Room, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param otherRequired the resource name, or !material type
	 * @return the found raw material item, or null
	 */
	public RawMaterial fetchFoundOtherEncoded(MOB E, String otherRequired);

	/**
	 * Searches the given room for all raw material resource items that
	 * have the same material type and subtype as the given raw
	 * material resource item, and returns the total amount of similar
	 * resources.  The items must not be in a container.
	 *
	 * @see MaterialLibrary#findNumberOfResourceLike(MOB, RawMaterial)
	 *
	 * @param E the possessor of possible raw material items
	 * @param resource the resource item to match others against
	 * @return the total amount of resources like the given one found
	 */
	public int findNumberOfResourceLike(Room E, RawMaterial resource);

	/**
	 * Searches the given mob for all raw material resource items that
	 * have the same material type and subtype as the given raw
	 * material resource item, and returns the total amount of similar
	 * resources.  The items must not be in a container.
	 *
	 * @see MaterialLibrary#findNumberOfResourceLike(Room, RawMaterial)
	 *
	 * @param E the possessor of possible raw material items
	 * @param resource the resource item to match others against
	 * @return the total amount of resources like the given one found
	 */
	public int findNumberOfResourceLike(MOB E, RawMaterial resource);

	/**
	 * Searches the given Room for the largest pile of qualifying raw
	 * material resource item that matches the given material type
	 * code number.  The items must not be in a container.
	 *
	 *  @see MaterialLibrary#findMostOfMaterial(MOB, int)
	 *  @see MaterialLibrary#findMostOfMaterial(MOB, String)
	 *  @see MaterialLibrary#findMostOfMaterial(Room, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param material the material type code number
	 * @return null, or the largest matching raw material item
	 */
	public RawMaterial findMostOfMaterial(Room E, int material);

	/**
	 * Searches the given MOB for the largest pile of qualifying raw
	 * material resource item that matches the given material type
	 * code number.  The items must not be in a container.
	 *
	 *  @see MaterialLibrary#findMostOfMaterial(MOB, String)
	 *  @see MaterialLibrary#findMostOfMaterial(Room, int)
	 *  @see MaterialLibrary#findMostOfMaterial(Room, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param material the material type code number
	 * @return null, or the largest matching raw material item
	 */
	public RawMaterial findMostOfMaterial(MOB E, int material);

	/**
	 * Searches the given Room for the largest pile of qualifying raw
	 * material resource item that matches the given material type
	 * name, with optional subtype in parenthesis.
	 * The items must not be in a container.
	 *
	 *  @see MaterialLibrary#findMostOfMaterial(MOB, int)
	 *  @see MaterialLibrary#findMostOfMaterial(MOB, String)
	 *  @see MaterialLibrary#findMostOfMaterial(Room, int)
	 *
	 * @param E the possessor of possible raw material items
	 * @param other the material type name, with optional subtype in parenthesis
	 * @return null, or the largest matching raw material item
	 */
	public RawMaterial findMostOfMaterial(Room E, String other);

	/**
	 * Searches the given MOB for the largest pile of qualifying raw
	 * material resource item that matches the given material type
	 * name, with optional subtype in parenthesis.
	 * The items must not be in a container.
	 *
	 *  @see MaterialLibrary#findMostOfMaterial(MOB, int)
	 *  @see MaterialLibrary#findMostOfMaterial(Room, int)
	 *  @see MaterialLibrary#findMostOfMaterial(Room, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param other the material type name, with optional subtype in parenthesis
	 * @return null, or the largest matching raw material item
	 */
	public RawMaterial findMostOfMaterial(MOB E, String other);

	/**
	 * Searches the given Room for the first qualifying raw
	 * material resource item that matches the given resource
	 * code number.
	 *
	 * @see MaterialLibrary#findFirstResource(MOB, int)
	 * @see MaterialLibrary#findFirstResource(MOB, String)
	 * @see MaterialLibrary#findFirstResource(Room, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param resource the resource code number
	 * @return null, or the first matching raw material item
	 */
	public RawMaterial findFirstResource(Room E, int resource);

	/**
	 * Searches the given MOB for the first qualifying raw
	 * material resource item that matches the given resource
	 * code number.
	 *
	 * @see MaterialLibrary#findFirstResource(MOB, String)
	 * @see MaterialLibrary#findFirstResource(Room, int)
	 * @see MaterialLibrary#findFirstResource(Room, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param resource the resource code number
	 * @return null, or the first matching raw material item
	 */
	public RawMaterial findFirstResource(MOB E, int resource);

	/**
	 * Searches the given Room for the first qualifying raw
	 * material resource item that matches the given resource
	 * name, with optional subtype in parenthesis.
	 *
	 * @see MaterialLibrary#findFirstResource(MOB, int)
	 * @see MaterialLibrary#findFirstResource(MOB, String)
	 * @see MaterialLibrary#findFirstResource(Room, int)
	 *
	 * @param E the possessor of possible raw material items
	 * @param other the resource name, with optional subtype in parenthesis
	 * @return null, or the first matching raw material item
	 */
	public RawMaterial findFirstResource(Room E, String other);

	/**
	 * Searches the given MOB for the first qualifying raw
	 * material resource item that matches the given resource
	 * name, with optional subtype in parenthesis.
	 *
	 * @see MaterialLibrary#findFirstResource(MOB, int)
	 * @see MaterialLibrary#findFirstResource(Room, int)
	 * @see MaterialLibrary#findFirstResource(Room, String)
	 *
	 * @param E the possessor of possible raw material items
	 * @param other the resource name, with optional subtype in parenthesis
	 * @return null, or the first matching raw material item
	 */
	public RawMaterial findFirstResource(MOB E, String other);


	/**
	 * Given a bundle (more than weight 1) raw resource item, this
	 * method will split the item into the given number of parts,
	 * each being 1 pound.  One of those is then returned.
	 * The container of the item is also required, for some reason.
	 *
	 * @see MaterialLibrary#splitBundle(Item, int, Container)
	 *
	 * @param I the resource bundle to split up
	 * @param number the number of 1 pounders to remove
	 * @param C the container of the bundle and the 1 pounders
	 * @return the first one pounder
	 */
	public Item unbundle(Item I, int number, Container C);

	/**
	 * Given a bundle (more than weight 1) raw resource item, this
	 * method will split the item into two parts, one part of the given
	 * size, which is returned, and the other being what remains of I.
	 * The container of the item is also required, for some reason.
	 *
	 * @see MaterialLibrary#unbundle(Item, int, Container)
	 *
	 * @param I the bundled item
	 * @param size the amount of the bundle to split out
	 * @param C the container I is in, and which the split goes into
	 * @return null, or the split out resource
	 */
	public Item splitBundle(Item I, int size, Container C);

	/**
	 * This great workhorse takes a given raw resource item and
	 * finds any pairings in its new possessor, and, if found,
	 * re-bundles with the existing item, and renames it.  The
	 * passed item is then destroyed.
	 *
	 * @see MaterialLibrary#unbundle(Item, int, Container)
	 * @see MaterialLibrary#rebundle(Item)
	 * @see MaterialLibrary#splitBundle(Item, int, Container)
	 *
	 * @param I the raw material item to rebundle
	 * @return true if the rebundle happened, false otherwise
	 */
	public boolean rebundle(Item I);

	/**
	 * Given a raw material resource, this method will query for the
	 * natural effects/properties of that resource type, and add
	 * them to the given item.
	 *
	 * @param I the raw material item
	 */
	public void addEffectsToResource(Item I);

	/**
	 * Removes the given item from its possessor, and then calls
	 * destroy on the item.  By removing it first, it shortcuts
	 * some of the more thorough processes that standard item
	 * destroy goes through, including recovery of stats.
	 *
	 * @param I null, or an item to destroy
	 * @return true if it was destroyed, or false if null
	 */
	public boolean quickDestroy(Item I);

	/**
	 * Given a raw material resource item with particular resource and
	 * subType and weight/amount, this method will set its name and
	 * display text as appropriate.
	 *
	 * @see MaterialLibrary#makeResourceBetterName(int, String, boolean)
	 * @see MaterialLibrary#makeResourceSimpleName(int, String)
	 *
	 * @param I the raw material item to name
	 */
	public void adjustResourceName(Item I);

	/**
	 * Returns a simplistic name for a raw material item of the given
	 * resource and subtype.  The name is always broad, like "iron ore".
	 *
	 * @see MaterialLibrary#makeResourceBetterName(int, String, boolean)
	 * @see MaterialLibrary#adjustResourceName(Item)
	 *
	 * @param rscCode the resource code
	 * @param subType "", null, or a subType
	 * @return a broad name for the resource
	 */
	public String makeResourceSimpleName(final int rscCode, String subType);

	/**
	 * Returns an appropriate name for an raw material item of the given
	 * resource and subtype. Differentiates between ores and cloth bolts and
	 * liquid pools, which is why it is a "better" name.
	 *
	 * @see MaterialLibrary#makeResourceSimpleName(int, String)
	 * @see MaterialLibrary#adjustResourceName(Item)
	 *
	 * @param rscCode the resource code
	 * @param subType null, "" or the subType
	 * @param plural true to make the name plural (more than 1), or false singular
	 * @return the better resource item name
	 */
	public String makeResourceBetterName(final int rscCode, String subType, final boolean plural);

	/**
	 * Returns a plural for the type of item given.  For raw resources, it
	 * returns the material name.
	 *
	 * @param I the item to get the general type of
	 * @return the general type of the item
	 */
	public String getGeneralItemType(Item I);

	/**
	 * Searches through all the locale/room classes to discover whether the given
	 * resource code appears in any of them as a possible resource.
	 *
	 * @param resourceCode the resource code
	 * @return true if its used somewhere.
	 */
	public boolean isResourceCodeRoomMapped(final int resourceCode);

	/**
	 * Returns the number of ticks that the given item, whatever
	 * will burn, or 0 if it won't burn.
	 *
	 * @param E the item to check
	 * @return the number of ticks to burn, or 0
	 */
	public int getBurnDuration(Environmental E);

	/**
	 * A record detailing information about
	 * destoryed resources, used mostly for common
	 * skills that consume them.
	 *
	 * @author Bo Zimmerman
	 */
	public static interface DeadResourceRecord
	{
		/**
		 * Returns the amount of base gold value consumed.
		 *
		 * @return the amount of base gold value consumed.
		 */
		public int getLostValue();

		/**
		 * Returns the pounds of material consumed
		 * @return the pounds of material consumed
		 */
		public int getLostAmt();

		/**
		 * Returns the Resource code of the consumed material
		 * @return the Resource code of the consumed material
		 */
		public int getResCode();

		/**
		 * Returns the subType, if any, of the consumed resource
		 * @return the subType, if any, of the consumed resource
		 */
		public String getSubType();

		/**
		 * Returns any property effects on consumed resource
		 * @return any property effects on consumed resource
		 */
		public List<CMObject> getLostProps();
	}
}
