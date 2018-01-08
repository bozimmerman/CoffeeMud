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
 * Interface for items which are complex wearables.  
 * Since even the most basic of items can be held, most of the methods
 * that deal strictly with "Wearables" have their own interface.
 * @see Wearable
 * This leaves the Armor interface just for the more complex aspects of
 * Wearability.
 *  
 * @author Bo Zimmerman
 *
 */
public interface Armor extends Item
{
	/**
	 * Description string array for the LAYERMASK_ constants, in ordinal order,
	 * as opposed to mask-value indexed.
	 * @see Armor#LAYERMASK_MULTIWEAR 
	 * @see Armor#LAYERMASK_SEETHROUGH 
	 */
	public static final String[] LAYERMASK_DESCS={"SEETHROUGH","MULTIWEAR"};
	
	/**
	 * Mask value for Armor which, when worn on the top layer, can be seen through.
	 * This allows the items worn at lower layers to be seen (unless they too are 
	 * see-through).
	 * @see Armor#LAYERMASK_DESCS
	 * @see Armor#LAYERMASK_MULTIWEAR 
	 */
	public static final short LAYERMASK_SEETHROUGH=(short)1;
	
	/**
	 * Mask value for Armor which can be worn many times at the same layer.
	 * @see Armor#LAYERMASK_DESCS
	 * @see Armor#LAYERMASK_SEETHROUGH 
	 */
	public static final short LAYERMASK_MULTIWEAR=(short)2;
	
	/**
	 * Returns the layer at which this item is worn.  0 is the baseline layer, while
	 * larger than 0 is things like coats and shawls, where less than 0 are things
	 * like underwear and hose.
	 * @see Armor#setClothingLayer(short)
	 * @return the layer at which this item is worn.
	 */
	public short getClothingLayer();
	
	/**
	 * Sets the layer at which this item is worn.  0 is the baseline layer, while
	 * larger than 0 is things like coats and shawls, where less than 0 are things
	 * like underwear and hose.
	 * @see Armor#getClothingLayer()
	 * @param newLayer the layer at which this item is worn.
	 */
	public void setClothingLayer(short newLayer);
	
	/**
	 * Returns the bitmask for the several layer attribute flags.  See the flags
	 * for more information on what they do.
	 * 
	 * @see Armor#setLayerAttributes(short)
	 * @see Armor#LAYERMASK_DESCS
	 * @see Armor#LAYERMASK_MULTIWEAR 
	 * @see Armor#LAYERMASK_SEETHROUGH 
	 * @return the bitmask for the several layer attribute flags
	 */
	public short getLayerAttributes();
	
	/**
	 * Sets the bitmask for the several layer attribute flags.  See the flags
	 * for more information on what they do.
	 * 
	 * @see Armor#getLayerAttributes()
	 * @see Armor#LAYERMASK_DESCS
	 * @see Armor#LAYERMASK_MULTIWEAR 
	 * @see Armor#LAYERMASK_SEETHROUGH 
	 * @param newAttributes the bitmask for the several layer attribute flags
	 */
	public void setLayerAttributes(short newAttributes);
	
	/**
	 * Returns a coded description of how well this item fits on the
	 * given mob.  See the SizeDeviation object for more information
	 * on the responses.
	 * @see SizeDeviation
	 * @param mob the mob to compare the fittability of this armor to
	 * @return a coded description of how well this item fits
	 */
	public SizeDeviation getSizingDeviation(MOB mob);
	
	/**
	 * Different ways in which a piece of armor can fit, or not
	 * fit, on a mob.
	 * @see Armor#getSizingDeviation(MOB)
	 * @author Bo Zimmerman
	 */
	public enum SizeDeviation
	{
		TOO_LARGE,
		TOO_SMALL,
		FITS
	}
}
