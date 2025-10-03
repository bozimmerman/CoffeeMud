package com.planet_ink.coffee_mud.core.interfaces;
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
   Copyright 2022-2025 Bo Zimmerman

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
 * An object containing its own liquid material type, and liquid capacity management.
 * Not necessarily drinkable though.  Think lantern, or fuel tank.
 * @author Bo Zimmerman
 *
 */
public interface LiquidHolder extends Environmental
{
	/**
	 * The total amount of liquid possible to be contained in this liquid container.
	 * @return total liquid contained herein.
	 */
	public int liquidHeld();

	/**
	 * The amount of liquid remaining in this liquid container.  Will always be less
	 * less than liquidHeld();
	 * @see Drink#liquidHeld()
	 * @return amount of liquid remaining in this liquid container.
	 */
	public int liquidRemaining();

	/**
	 * The material type of the liquid in this container.  Although a class implementing
	 * the LiquidHolder interface can sometimes be a liquid itself (like GenLiquidResource), most
	 * often, a Drink interface implementing class is a mob without a material to draw from
	 * or an Item having its own non-liquid material (like a leather waterskin containing milk).
	 * Either way, this is necessary.  The material types are constants in RawMaterial.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @return the type of liquid contained herein
	 */
	public int liquidType();

	/**
	 * Sets the material type of the liquid in this container.  Although a class implementing
	 * the LiquidHolder interface can sometimes be a liquid itself (like GenLiquidResource), most
	 * often, a Drink interface implementing class is a mob without a material to draw from
	 * or an Item having its own non-liquid material (like a leather waterskin containing milk).
	 * Either way, this is necessary.  The material types are constants in RawMaterial.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @param newLiquidType the type of liquid contained herein
	 */
	public void setLiquidType(int newLiquidType);

	/**
	 * The displayable name of the liquid type in this container.
	 * Usually derived from liquidType();
	 *
	 * @see com.planet_ink.coffee_mud.Items.interfaces.LiquidHolder#liquidType()
	 *
	 * @return the displayable name of the liquid type in this container.
	 */
	public String liquidTypeName();

	/**
	 * Sets the displayable name of the liquid type in this container. Usually
	 * derived from liquidType(), but overrideable here.
	 * This field is NOT persisted.
	 *
	 * @see com.planet_ink.coffee_mud.Items.interfaces.LiquidHolder#liquidType()
	 *
	 * @param name the displayable name of the liquid type in this container.
	 */
	public void setLiquidTypeName(String name);

	/**
	 * Sets the total amount of liquid possible to be contained in this liquid container.
	 * @param amount total liquid contained herein.
	 */
	public void setLiquidHeld(int amount);

	/**
	 * Sets the amount of liquid remaining in this liquid container.  Will always be less
	 * less than liquidHeld();
	 * @see Drink#setLiquidHeld(int)
	 * @param amount amount of liquid remaining in this liquid container.
	 */
	public void setLiquidRemaining(int amount);

	/**
	 * Whether this liquid container still contains any liquid.
	 * @return whether any liquid is left.
	 */
	public boolean containsLiquid();

	/**
	 * Given the liquid source, the amount of liquid which would need to be taken
	 * from the source liquid source to fill up THIS liquid source.
	 * @param theSource the liquid source to fill up from
	 * @return the amount to take from the liquid source
	 */
	public int amountTakenToFillMe(LiquidHolder theSource);
}
