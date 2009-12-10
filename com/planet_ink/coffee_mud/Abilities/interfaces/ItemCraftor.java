package com.planet_ink.coffee_mud.Abilities.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface ItemCraftor extends Ability
{
	/**
	 * A Vector containing an entry for each craftable recipe
	 * Each craftable recipe is also a vector of strings.
	 * @return a vector of vectors
	 */
	public Vector fetchRecipes();
	
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
	public Vector matchingRecipeNames(String recipeName, boolean beLoose);
	
	/**
	 * Crafts a random item of a type supported by this class of 
	 * the given resource code.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param material the rawmaterial code to make the item out of
	 * @return a vector of Item(s)
	 */
	public Vector craftAnyItem(int material);
	
	/**
	 * Crafts every item of a type supported by this class of 
	 * the given resource code.  Each finished item is represented
	 * as a vector in the returned vector. The item vector usually
	 * only contains the finished item, but a second element will
	 * occur when a key is required and also generated.
	 * @param material the rawmaterial code to make the item out of
	 * @return a vector of vectors of item(s)
	 */
	public Vector craftAllItemsVectors(int material);
	
	/**
	 * Crafts every item of a type supported by this class of 
	 * every supported material.  Each finished item is represented
	 * as a vector in the returned vector. The item vector usually
	 * only contains the finished item, but a second element will
	 * occur when a key is required and also generated.
	 * @return a vector of vectors of item vector(s)
	 */
	public Vector craftAllItemsVectors();
	
	/**
	 * Crafts the item specified by the recipe name, of a supported
	 * material type which this class can produce.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param recipe the name of the item to make
	 * @return a vector of Item(s)
	 */
	public Vector craftItem(String recipe);
	
	/**
	 * Crafts the item specified by the recipe name, of the specified
	 * material type which this class can produce.
	 * Returns a vector containing the finished Item.  A second element is
	 * rare, but will occur when a key is required and also generated.
	 * @param recipe the name of the item to make
	 * @param material the rawmaterial code to make the item out of
	 * @return a vector of Item(s)
	 */
	public Vector craftItem(String recipe, int material);
	
	/**
	 * Returns a Vector of Integer objects where each Integer
	 * is a fully qualified RawMaterial code.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @return a vector of integers
	 */
	public Vector myResources();
}
