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

/*
   Copyright 2003-2018 Bo Zimmerman

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
 * Represents a MOB being stored as an Item, such as a mob in a cage, or a baby.
 * This interface allows the mob to be converted to the item, and back again.
 * @author Bo Zimmerman
 */
public interface CagedAnimal extends Item
{
	/**
	 * A Cage flag bit mask denoting that this caged animal should
	 * not automatically uncage itself, but may only do so programmatically.
	 * A Baby, who is uncaged only through aging, is the classic example.
	 * @see CagedAnimal#setCageFlagsBitmap(int)
	 * @see CagedAnimal#getCageFlagsBitmap()
	 */
	public static final int CAGEFLAG_TO_MOB_PROGRAMMATICALLY=1;

	/**
	 * Cages the given mob, storing it in this Item object for
	 * uncaging later.  The mob must not be bound to a session.
	 * Does not otherwise affect the mob.
	 * @see CagedAnimal#unCageMe()
	 * @param M the mob to cage
	 * @return true if the caging was successful, false otherwise
	 */
	public boolean cageMe(MOB M);

	/**
	 * Uncages the mob previously stored in this item by returning the mob
	 * stored in this item.  Does not otherwise affect the item.
	 * @see CagedAnimal#cageMe(MOB)
	 * @return the mob stored in this item, or null if a failure
	 */
	public MOB unCageMe();

	/**
	 * Returns the raw xml text representing the mob stored in this item.
	 * @see CagedAnimal#setCageText(String)
	 * @return the raw xml text representing the mob stored in this item.
	 */
	public String cageText();

	/**
	 * Sets the raw xml text representing the mob stored in this item.
	 * @see CagedAnimal#cageText()
	 * @param text the raw xml text representing the mob stored in this item.
	 */
	public void setCageText(String text);

	/**
	 * Returns the flag bitmap describing how this item behaves.
	 * @see CagedAnimal#CAGEFLAG_TO_MOB_PROGRAMMATICALLY
	 * @see CagedAnimal#setCageFlagsBitmap(int)
	 * @return  the flag bitmap describing how this item behaves.
	 */
	public int getCageFlagsBitmap();

	/**
	 * Sets the flag bitmap describing how this item behaves.
	 * @see CagedAnimal#CAGEFLAG_TO_MOB_PROGRAMMATICALLY
	 * @see CagedAnimal#getCageFlagsBitmap()
	 * @param bitmap the flag bitmap describing how this item behaves.
	 */
	public void setCageFlagsBitmap(int bitmap);
}
