package com.planet_ink.coffee_mud.core.interfaces;

import java.util.List;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.core.collections.Pair;
/*
   Copyright 2022-2023 Bo Zimmerman

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
 * A CMObject whose behavior is driven by 'recipes', or some
 * data file formatted in a standard way.
 *
 * @see com.planet_ink.coffee_mud.core.CMClass
 * @author Bo Zimmerman
 *
 */
public interface RecipeDriven extends CMObject
{
	/**
	 * Standard for all data drivens, where first field is the name
	 */
	public static final int	RCP_FINALNAME	= 0;

	/**
	 * Standard for all data drivens, where second field is the level, if applicable
	 */
	public static final int	RCP_LEVEL		= 1;

	/**
	 * A list containing an entry for each recipe
	 * Each recipe is also a list of strings.
	 * @return a vector of vectors
	 */
	public List<List<String>> fetchRecipes();

	/**
	 * A String containing the format of each entry in the parameter file
	 * in a recipe.
	 * @return a String showing the format of each entry in the parameter file
	 */
	public String getRecipeFormat();

	/**
	 * A String naming the file where the recipes are found
	 * @return a String naming the file where the recipes are found
	 */
	public String getRecipeFilename();

	/**
	 * Returns a vector containing an entry for each recipe
	 * whose name matches the given name.  Each entry is also a vector.
	 * @param recipeName the name of the recipe
	 * @param beLoose whether to be specific or "loose" with name matching
	 * @return a vector of vectors
	 */
	public List<String> matchingRecipeNames(String recipeName, boolean beLoose);

	/**
	 * Given a raw recipe, returns the raw name and level of the ting built therefrom.
	 * @param recipe the raw recipe description
	 * @return a descriptive pair
	 */
	public Pair<String,Integer> getDecodedItemNameAndLevel(final List<String> recipe);
}
