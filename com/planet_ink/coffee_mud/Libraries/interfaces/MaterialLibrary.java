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
   Copyright 2006-2022 Bo Zimmerman

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
public interface MaterialLibrary extends CMLibrary
{
	public int getMaterialRelativeInt(String s);
	public int getMaterialCode(String s, boolean exact);
	public int getResourceCode(String s, boolean exact);
	public String getResourceDesc(int MASK);
	public String getMaterialDesc(int MASK);
	public Item makeItemResource(int type);
	public Item makeItemResource(int type, String subType);
	public PhysicalAgent makeResource(int myResource, String localeCode, boolean noAnimals, String fullName, String subType);
	public int getRandomResourceOfMaterial(int material);
	public DeadResourceRecord destroyResources(final Room R, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int finalOtherHash);
	public int destroyResourcesValue(MOB M, int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash);
	public int destroyResourcesValue(final Room R, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash);
	public int destroyResourcesValue(List<Item> V, int howMuch, int finalMaterial, int otherMaterial, Item never, Container C);
	public int destroyResourcesAmt(MOB E, int howMuch, int finalMaterial, String subType, Container C);
	public int destroyResourcesAmt(Room E, int howMuch, int finalMaterial, String subType, Container C);
	public int destroyResourcesAmt(List<Item> V, int howMuch, int finalMaterial, String subType, Container C);
	public RawMaterial fetchFoundOtherEncoded(Room E, String otherRequired);
	public RawMaterial fetchFoundOtherEncoded(MOB E, String otherRequired);
	public RawMaterial findMostOfMaterial(Room E, int material);
	public RawMaterial findMostOfMaterial(MOB E, int material);
	public int findNumberOfResource(Room E, RawMaterial resource);
	public int findNumberOfResource(MOB E, RawMaterial resource);
	public RawMaterial findMostOfMaterial(Room E, String other);
	public RawMaterial findMostOfMaterial(MOB E, String other);
	public RawMaterial findFirstResource(Room E, int resource);
	public RawMaterial findFirstResource(MOB E, int resource);
	public RawMaterial findFirstResource(Room E, String other);
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
