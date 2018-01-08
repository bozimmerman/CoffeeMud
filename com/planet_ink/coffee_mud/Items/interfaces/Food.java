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
 * The food interface is for items for items that can be targeted with the
 * EAT command, and which provide nourishment that reduces hunger. 
 * @author Bo Zimmerman
 */
public interface Food extends Item, Decayable
{
	/**
	 * Gets the total amount of nourishment contained in this food item.
	 * These are divided by the eat command into bites.
	 * @see Food#bite()
	 * @see Food#setNourishment(int)
	 * @return total amount of nourishment contained in this food item.
	 */
	public int nourishment();
	
	/**
	 * Sets the total amount of nourishment contained in this food item.
	 * These are divided by the eat command into bites.
	 * @see Food#bite()
	 * @see Food#setNourishment(int)
	 * @param amount total amount of nourishment contained in this food item.
	 */
	public void setNourishment(int amount);
	
	/**
	 * Gets the size of an individual bite, which is basically how my nourishment
	 * you get from each eat command.  A food item generally disappears after
	 * its nourishment is used by by bites.
	 * @see Food#nourishment()
	 * @return the size of an individual bite
	 */
	public int bite();
	
	/**
	 * Sets the size of an individual bite, which is basically how my nourishment
	 * you get from each eat command.  A food item generally disappears after
	 * its nourishment is used by by bites.
	 * @see Food#nourishment()
	 * @param amount the size of an individual bite
	 */
	public void setBite(int amount);
}
