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
   Copyright 2013-2018 Bo Zimmerman

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
 * An interface for a weapon which does not work without ammunition, or
 * which can potentially require ammunition.
 * 
 * @author Bo Zimmerman
 */
public interface AmmunitionWeapon extends Weapon
{
	/**
	 * Returns true if the weapon requires ammunition, ever.
	 * Not sure why this would ever return false for an AmmunitionWeapon, 
	 * but it can, so here it is.
	 * @return true if the weapon requires ammunition, false otherwise 
	 */
	public boolean requiresAmmunition();
	
	/**
	 * Sets the type/class of ammunition required by this weapon.  This must match the 
	 * ammunition type of the ammunition item in order to be reloaded.
	 * This can be an arbitrary string.
	 * @see Ammunition#setAmmunitionType(String)
	 * @see #ammunitionType()
	 * @param ammo the ammunition type string
	 */
	public void setAmmunitionType(String ammo);
	
	/**
	 * The type/class of ammunition required by this weapon.  This must match the 
	 * ammunition type of the ammunition item in order to be reloaded.
	 * This can be an arbitrary string.
	 * @see Ammunition#ammunitionType()
	 * @see #setAmmunitionType(String)
	 * @return the ammunition type string
	 */
	public String ammunitionType();

	/**
	 * The amount of Units of ammunition loaded into this weapon.  If this is,
	 * for example, a bow, or a gun, how many are in the clip or ready to shoot.
	 * @see #setAmmoRemaining(int)
	 * @return the number of units of ammunition
	 */
	public int ammunitionRemaining();
	
	/**
	 * Sets the amount of Units of ammunition loaded into this weapon.  If this is,
	 * for example, a bow, or a gun, how many are in the clip or ready to shoot.
	 * @see #ammunitionRemaining()
	 * @param amount the number of units of ammunition
	 */
	public void setAmmoRemaining(int amount);

	/**
	 * The maximum amount of Units of ammunition which can be loaded into this weapon.  
	 * If this is, for example, a bow, or a gun, how many does the clip hold or can be 
	 * ready to shoot.
	 * @see #setAmmoCapacity(int)
	 * @return the max number of units of ammunition that can be loaded
	 */
	public int ammunitionCapacity();

	/**
	 * Sets the maximum amount of Units of ammunition which can be loaded into this weapon.
	 * If this is, for example, a bow, or a gun, how many does the clip hold or can be 
	 * ready to shoot.
	 * @see AmmunitionWeapon#ammunitionCapacity()
	 * @param amount the max number of units of ammunition that can be loaded
	 */
	public void setAmmoCapacity(int amount);
	
	/**
	 * Returns true if the weapon is a free-standing missile weapon, meaning
	 * it can be loaded and fired from the ground, like a siege weapon/catapult.
	 * @return true if the weapon is free standing, false if held only 
	 */
	public boolean isFreeStanding();
}
