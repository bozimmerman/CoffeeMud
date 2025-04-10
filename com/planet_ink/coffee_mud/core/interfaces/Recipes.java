package com.planet_ink.coffee_mud.core.interfaces;
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
import java.util.Vector;

/*
   Copyright 2005-2025 Bo Zimmerman

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
 * Recipes add to the recipes available to construction-type common skills, such
 * as tailoring, weaponsmithing, etc.
 *
 * @author Bo Zimmerman
 */
public interface Recipes extends Environmental
{
	/**
	 * Gets the Ability ID of the skill that
	 * this Recipe item adds a new recipe to.
	 * @see Recipes#setCommonSkillID(String)
	 * @return the Ability ID this enhances
	 */
	public String getCommonSkillID();

	/**
	 * Sets the Ability ID of the skill that
	 * this Recipe item adds a new recipe to.
	 * @see Recipes#getCommonSkillID()
	 * @param ID the Ability ID this enhances
	 */
	public void setCommonSkillID(String ID);

	/**
	 * Gets all the recipes written on this Recipe item,
	 * one on each line/page, and each line encoded
	 * according to the specific common skill it
	 * applies to.
	 * @see Recipes#setRecipeCodeLines(String[])
	 * @return all the recipes written on this Recipe item
	 */
	public String[] getRecipeCodeLines();

	/**
	 * Sets all the recipes written on this Recipe item,
	 * one on each line/page, and each line encoded
	 * according to the specific common skill it
	 * applies to.
	 * @see Recipes#getRecipeCodeLines()
	 * @param lines all the recipes written on this Recipe item
	 */
	public void setRecipeCodeLines(String[] lines);
}

