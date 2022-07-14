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
   Copyright 2016-2022 Bo Zimmerman

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
public interface CraftorAbility extends Ability, RecipeDriven
{
	/**
	 * A list containing an entry for each craftable recipe
	 * both standard, and extra recipes from given mob
	 * Each craftable recipe is also a list of strings.
	 * @param mob the mob to check for extra recipes
	 * @return a vector of vectors
	 */
	public List<List<String>> fetchMyRecipes(final MOB mob);

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
	 * Class for generating filters of crafted items
	 * @author Bo Zimmerman
	 *
	 */
	public static class CraftorFilter
	{
		public int			minLevel	= 0;
		public int			maxLevel	= Integer.MAX_VALUE;
		public int			reqLevel	= -1;
		public int			minValue	= -1;
		public int			maxValue	= Integer.MAX_VALUE;
		public Class<?>[]	classes		= new Class<?>[0];
		public String		name		= "";
	}
}
