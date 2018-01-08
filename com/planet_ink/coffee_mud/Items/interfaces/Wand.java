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
import java.util.*;

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
 * A wand is an item containing a magic spell that can be invoked
 * on a target, by holding the wand, and then saying the magic word
 * to the target.
 * @author Bo Zimmerman
 *
 */
public interface Wand extends MiscMagic
{
	/**
	 * Sets the spell ability object that this wand can cast.
	 * @see Wand#getSpell()
	 * @param theSpell the spell ability object that this wand can cast.
	 */
	public void setSpell(Ability theSpell);
	
	/**
	 * Sets the spell ability object that this wand can cast.
	 * @see Wand#setSpell(Ability)
	 * @return the spell ability object that this wand can cast.
	 */
	public Ability getSpell();

	/**
	 * Returns whether the given mob can use this wand.  They
	 * must be holding it, and must say the magic word, etc.
	 * @param mob the mob to check for waving ability
	 * @param message what the mob said prior to the check
	 * @return true if the wand can be invoked, false otherwise
	 */
	public boolean checkWave(MOB mob, String message);
	
	/**
	 * Checks to see if the mob can invoke this wand against the
	 * given target, given the message they just said out loud.
	 * @param mob the invoker of the wand
	 * @param afftarget the target of the say message
	 * @param message the thing said by the wand invoker
	 */
	public void waveIfAble(MOB mob, Physical afftarget, String message);
	
	/**
	 * Returns the magic word that invokes this wand
	 * @return the magic word that invokes this wand
	 */
	public String magicWord();
	
	/**
	 * Gets the number of times this wand can be invoked before
	 * being drained and empty useless stick.
	 * @see Wand#setMaxUses(int)
	 * @return the number of times this wand can be invoked
	 */
	public int maxUses();
	
	/**
	 * Sets the number of times this wand can be invoked before
	 * being drained and empty useless stick.
	 * @see Wand#maxUses()
	 * @param maxUses the number of times this wand can be invoked
	 */
	public void setMaxUses(int maxUses);
}
