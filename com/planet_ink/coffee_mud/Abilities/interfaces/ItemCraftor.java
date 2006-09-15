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
   Copyright 2000-2006 Bo Zimmerman

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
	 * @return
	 */
	public Vector fetchRecipes();
	/**
	 * @param recipeName
	 * @param beLoose
	 * @return
	 */
	public Vector matchingRecipeNames(String recipeName, boolean beLoose);
	/**
	 * @param material
	 * @return
	 */
	public Vector craftAnyItem(int material);
	/**
	 * @param material
	 * @return
	 */
	public Vector craftAllItemsVectors(int material);
	/**
	 * @return
	 */
	public Vector craftAllItemsVectors();
	/**
	 * @param recipe
	 * @return
	 */
	public Vector craftItem(String recipe);
	/**
	 * @param recipe
	 * @param material
	 * @return
	 */
	public Vector craftItem(String recipe, int material);
	/**
	 * @return
	 */
	public Vector myResources();
}
