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
import java.util.Vector;

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
 * A potion is an item that bestows some magic upong the drinker.
 * 
 * @author Bo Zimmerman
 *
 */
public interface Potion extends Drink, MiscMagic, SpellHolder
{
	/**
	 * Gets whether the potion has been drunk.
	 * @see Potion#setDrunk(boolean)
	 * @return true if the potion has been drunk, false otherwise.
	 */
	public boolean isDrunk();
	
	/**
	 * Sets whether the potion has been drunk.
	 * @see Potion#isDrunk()
	 * @param isTrue true if the potion has been drunk, false otherwise.
	 */
	public void setDrunk(boolean isTrue);

	/**
	 * Causes the potion to betow its effects upon the "drinkerTarget"
	 * at the behest of the owner mob.  The two are different because one
	 * person can make another person drink, if they must.
	 * @param owner the holder of the potion
	 * @param drinkerTarget the one drinking the potion
	 */
	public void drinkIfAble(MOB owner, Physical drinkerTarget);
}
