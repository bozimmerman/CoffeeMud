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
   Copyright 2016-2018 Bo Zimmerman

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
 * skill, but one could imagine an craftor also being a spell that
 * generates its own items.  Also the generation is an incidental
 * and internal aspect of the ability, these methods allow that
 * functionality to be exposed for archon use.
 */
public interface CraftorAbility extends Ability
{
	/**
	 * A list containing an entry for each craftable recipe
	 * Each craftable recipe is also a list of strings.
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
	 * Returns a list of Integer objects where each Integer
	 * is a fully qualified RawMaterial code.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @return a vector of integers
	 */
	public List<Integer> myResources();

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
