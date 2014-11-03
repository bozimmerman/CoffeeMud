package com.planet_ink.coffee_mud.Abilities.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
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
   Copyright 2008-2014 Bo Zimmerman

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
 * This interface denotes an ability that also incidentally is capable
 * of generating objects, usually items.  This is probably a common
 * skill, but one could imagine an itemcraftor also being a spell that
 * generates its own items.  Also the item generation is an incidental
 * and internal aspect of the ability, these methods allow that
 * functionality to be exposed for archon use.
 */
public interface ItemCraftor extends Ability
{
	/**
	 * A Vector containing an entry for each craftable recipe
	 * Each craftable recipe is also a vector of strings.
	 * @return a vector of vectors
	 */
	public List<List<String>> fetchRecipes();

	/**
	 * A String containing the format of each entry in the parameter file
	 * in a recipe.
	 * @return a String showing the format of each entry in the parameter file
	 */
	public String parametersFormat();

	/**
	 * A String naming the file where the recipes are found
	 * @return a String naming the file where the recipes are found
	 */
	public String parametersFile();

	/**
	 * Returns a vector containing an entry for each craftable recipe
	 * whose name matches the given name.  Each entry is also a vector.
	 * @param recipeName the name of the recipe to craft
	 * @param beLoose whether to be specific or "loose" with name matching
	 * @return a vector of vectors
	 */
	public List<List<String>> matchingRecipeNames(String recipeName, boolean beLoose);

	/**
	 * Crafts a random item of a type supported by this class of
	 * the given resource code.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param material the rawmaterial code to make the item out of
	 * @return a vector of Item(s)
	 */
	public ItemKeyPair craftAnyItem(int material);

	/**
	 * Crafts every item of a type supported by this class of
	 * the given resource code.  Each finished item is represented
	 * as a vector in the returned vector. The item vector usually
	 * only contains the finished item, but a second element will
	 * occur when a key is required and also generated.
	 * @param material the rawmaterial code to make the item out of
	 * @param forceLevels forces crafted item to have a level if it otherwise doesn't
	 * @return a vector of vectors of item(s)
	 */
	public List<ItemKeyPair> craftAllItemSets(int material, boolean forceLevels);

	/**
	 * Crafts every item of a type supported by this class of
	 * every supported material.  Each finished item is represented
	 * as a vector in the returned vector. The item vector usually
	 * only contains the finished item, but a second element will
	 * occur when a key is required and also generated.
	 * @param forceLevels forces crafted item to have a level if it otherwise doesn't
	 * @return a vector of vectors of item vector(s)
	 */
	public List<ItemKeyPair> craftAllItemSets(boolean forceLevels);

	/**
	 * Crafts the item specified by the recipe name, of a supported
	 * material type which this class can produce.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param recipeName the name of the item to make
	 * @return a vector of Item(s)
	 */
	public ItemKeyPair craftItem(String recipeName);

	/**
	 * Crafts the item specified by the recipe name, of the specified
	 * material type which this class can produce or -1 for any material.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param recipeName the name of the item to make
	 * @param material the rawmaterial code to make the item out of, or -1
	 * @param forceLevels forces crafted item to have a level if it otherwise doesn't
	 * @return a vector of Item(s)
	 */
	public ItemKeyPair craftItem(String recipeName, int material, boolean forceLevels);

	/**
	 * Returns a Vector of Integer objects where each Integer
	 * is a fully qualified RawMaterial code.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @return a vector of integers
	 */
	public List<Integer> myResources();

	/**
	 * For auto-crafting, this object represents an item,
	 * and (optionally) a key to go with it.
	 * @author bzimmerman
	 */
	public class ItemKeyPair
	{
		public Item item;
		public DoorKey key;
		public ItemKeyPair(Item item, DoorKey key) { this.item=item; this.key=key;}
		public List<Item> asList()
		{
			final List<Item> list = new LinkedList<Item>();
			if(item!=null) list.add(item);
			if(key != null) list.add(key);
			return list;
		}

	}

	/**
	 * Returns whether the given item could have been crafted by this skill.
	 * @param I the item to examine
	 * @return true if the item is consistent with this crafting, or false otherwise
	 */
	public boolean mayICraft(final Item I);

	/**
	 * Returns true if mundane items can be demonstructed into recipes with this skill.
	 * @return true if mundane items can be demonstructed into recipes with this skill.
	 */
	public boolean supportsDeconstruction();

	/**
	 * Returns the ratio of the weight of material used to make an item with this
	 * skill versus the item weight when finished
	 * @param bundling true if the item being created is just a raw resource bundle
	 * @return the ratio of the weight of material used to make an item with this
	 */
	public double getItemWeightMultiplier(boolean bundling);

	/**
	 * Given a raw recipe, returns a description of the required components to build it.
	 * @param mob the potential builder
	 * @param recipe the raw recipe description
	 * @return a descriptive string
	 */
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe);

	/**
	 * Given a raw recipe, returns the raw name and level of the item built therefrom.
	 * @param recipe the raw recipe description
	 * @return a descriptive pair
	 */
	public Pair<String,Integer> getDecodedItemNameAndLevel(final List<String> recipe);
}
