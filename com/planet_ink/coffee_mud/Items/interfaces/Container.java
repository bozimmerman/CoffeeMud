package com.planet_ink.coffee_mud.Items.interfaces;
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
 * A container is an item that contains other items, including other containers.
 * Next to the Item interface, the Container interface is probably the most basic.
 * @author Bo Zimmerman
 *
 */
public interface Container extends Item, CloseableLockable
{
	/**
	 * Returns all the items in this container, including other
	 * containers and THEIR contents, recursively.
	 * @see Container#getContents()
	 * @return all the items in this container, recursively
	 */
	public ReadOnlyList<Item> getDeepContents();
	
	/**
	 * Returns all the immediate items in this container, including
	 * other containers, but not the contents of those innner containers.
	 * Just the first level contents of this container.
	 * @see Container#getDeepContents()
	 * @return all the immediate items in this container
	 */
	public ReadOnlyList<Item> getContents();
	
	/**
	 * Returns the maximum weight that can fit inside this container.  Weight
	 * is used as a proxy for volume in CoffeeMud.
	 * @see Container#setCapacity(int)
	 * @return the maximum weight that can fit inside this container
	 */
	public int capacity();
	
	/**
	 * Sets the maximum weight that can fit inside this container.  Weight
	 * is used as a proxy for volume in CoffeeMud.
	 * @see Container#capacity()
	 * @param newValue the maximum weight that can fit inside this container
	 */
	public void setCapacity(int newValue);
	
	/**
	 * Returns whether there is anything at all in this container.
	 * @return true if there is anything in this container, false if it is empty
	 */
	public boolean hasContent();
	
	/**
	 * Returns whether this container is allowed to contain the given object.
	 * @see Container#setContainTypes(long)
	 * @see Container#containTypes()
	 * @param I the item to check against the allowed content
	 * @return true if the given item can go in this container, false otherwise
	 */
	public boolean canContain(Item I);
	
	/**
	 * Returns whether the given item is, in fact, inside this container, even
	 * recursively.  So if the given item is in a container, and that container
	 * is in THIS container, it would still be inside.
	 * @param I the item to check against the content
	 * @return true if the item is somewhere inside, false otherwise
	 */
	public boolean isInside(Item I);
	
	/**
	 * Returns a bitmap of the types of things that this container can hold.
	 * @see Container#setContainTypes(long)
	 * @see Container#CONTAIN_ANYTHING
	 * @see Container#CONTAIN_DESCS
	 * @return a bitmap of the types of things that this container can hold
	 */
	public long containTypes();
	
	/**
	 * Sets a bitmap of the types of things that this container can hold.
	 * @see Container#containTypes()
	 * @see Container#CONTAIN_ANYTHING
	 * @see Container#CONTAIN_DESCS
	 * @param containTypes a bitmap of the types of things that this container can hold.
	 */
	public void setContainTypes(long containTypes);
	
	/**
	 * Empties this container into its owner.
	 * @param flatten if true, will also remove all recursive items from their containers
	 */
	public void emptyPlease(boolean flatten);
	
	/**
	 * Container type that overrides all others -- the container can hold anything!
	 * @see Container#setContainTypes(long)
	 * @see Container#containTypes()
	 */
	public static final int CONTAIN_ANYTHING=0;
	/** Container Type flag that means the container can hold liquids */
	public static final int CONTAIN_LIQUID=1;
	/** Container Type flag that means the container can hold coins */
	public static final int CONTAIN_COINS=2;
	/** Container Type flag that means the container can hold swords */
	public static final int CONTAIN_SWORDS=4;
	/** Container Type flag that means the container can hold daggers */
	public static final int CONTAIN_DAGGERS=8;
	/** Container Type flag that means the container can hold other weapons */
	public static final int CONTAIN_OTHERWEAPONS=16;
	/** Container Type flag that means the container can hold one handed weapons */
	public static final int CONTAIN_ONEHANDWEAPONS=32;
	/** Container Type flag that means the container can hold corpses */
	public static final int CONTAIN_BODIES=64;
	/** Container Type flag that means the container can hold books and such */
	public static final int CONTAIN_READABLES=128;
	/** Container Type flag that means the container can hold scrolls */
	public static final int CONTAIN_SCROLLS=256;
	/** Container Type flag that means the container can hold caged animals */
	public static final int CONTAIN_CAGED=512;
	/** Container Type flag that means the container can hold keys */
	public static final int CONTAIN_KEYS=1024;
	/** Container Type flag that means the container can hold drinkables */
	public static final int CONTAIN_DRINKABLES=2048;
	/** Container Type flag that means the container can hold clothing */
	public static final int CONTAIN_CLOTHES=4096;
	/** Container Type flag that means the container can hold smokeables */
	public static final int CONTAIN_SMOKEABLES=8192;
	/** Container Type flag that means the container can hold space ship components */
	public static final int CONTAIN_SSCOMPONENTS=16384;
	/** Container Type flag that means the container can hold shoes */
	public static final int CONTAIN_FOOTWEAR=32768;
	/** Container Type flag that means the container can hold raw resources */
	public static final int CONTAIN_RAWMATERIALS=65536;
	/** Container Type flag that means the container can hold foods */
	public static final int CONTAIN_EATABLES=131072;
	
	/**
	 * Ordinal list of the names of all the container bitmask types.
	 * @see Container#setContainTypes(long)
	 * @see Container#containTypes()
	 */
	public static final String[] CONTAIN_DESCS={"ANYTHING",
												"LIQUID",
												"COINS",
												"SWORDS",
												"DAGGERS",
												"OTHER WEAPONS",
												"ONE-HANDED WEAPONS",
												"BODIES",
												"READABLES",
												"SCROLLS",
												"CAGED ANIMALS",
												"KEYS",
												"DRINKABLES",
												"CLOTHES",
												"SMOKEABLES",
												"SS COMPONENTS",
												"FOOTWEAR",
												"RAWMATERIALS",
												"EATABLES"
												};
}
