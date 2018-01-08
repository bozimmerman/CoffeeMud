package com.planet_ink.coffee_mud.Common.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

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
 * A class for holding information about the manufacturer of electronics.
 */
public interface Manufacturer extends CMCommon
{

	/**
	 * Returns whether the given item will be manufacturered by this manufacturer.
	 * @param T the item type
	 * @return true if this manufacturer will do it
	 */
	public boolean isManufactureredType(Technical T);

	/**
	 * Returns a comma-delimited list of the types of things this manufacturer will make.
	 * @return a comma-delimited list of the types of things this manufacturer will make.
	 */
	public String getManufactureredTypesList();

	/**
	 * Sets the comma-delimited list of the types of things this manufacturer will make.
	 * @param list the comma-delimited list of the types of things this manufacturer will make.
	 */
	public void setManufactureredTypesList(String list);

	/**
	 * Returns a positive difference from Max Tech Level-10.  This is
	 * the maximum tech level this manufacturer can manage.
	 * @return a number from 0-10
	 */
	public byte getMaxTechLevelDiff();

	/**
	 * Sets a positive difference from Max Tech Level-10.  This is
	 * the maximum tech level this manufacturer can manage.
	 * @param max a number from 0-10
	 */
	public void setMaxTechLevelDiff(byte max);

	/**
	 * Returns a positive difference from Max Tech Level-10.  This is
	 * the minimum tech level this manufacturer can manage.
	 * @return a number from 0-10
	 */
	public byte getMinTechLevelDiff();

	/**
	 * Sets a positive difference from Max Tech Level-10.  This is
	 * the minimum tech level this manufacturer will make.
	 * @param min a number from 0-10
	 */
	public void setMinTechLevelDiff(byte min);

	/**
	 * Sets the name of the manufacturer
	 * @param name of the manufacturer
	 */
	public void setName(String name);

	/**
	 * Returns a pct, from 0-2, to multiply by the power requirements
	 * of electronic items for certain purposes.  A 1.0 means perfectly
	 * standard.  A 2.0 means very inefficient.  A 0.5 means super efficient.
	 * @return a pct, from 0-2
	 */
	public double getEfficiencyPct();

	/**
	 * Sets a pct, from 0-2, to multiply by the power requirements
	 * of electronic items for certain purposes.  A 1.0 means perfectly
	 * standard.  A 2.0 means very inefficient.  A 0.5 means super efficient.
	 * @param pct from 0-2
	 */
	public void setEfficiencyPct(double pct);

	/**
	 * Returns a pct, from 0-2, to multiply by the amt of damage taken,
	 * and the chance of failure, esp. when damaged.  A 1.0 means perfectly
	 * standard.  A 2.0 means super-standard.  A 0.5 means sub standard.
	 * @return a pct, from 0-2
	 */
	public double getReliabilityPct();

	/**
	 * Sets a pct, from 0-2, to multiply by the amt of damage taken,
	 * and the chance of failure, esp. when damaged.  A 1.0 means perfectly
	 * standard.  A 2.0 means super-standard.  A 0.5 means sub standard.
	 * @param pct from 0-2
	 */
	public void setReliabilityPct(double pct);

	/**
	 * Sets the item mask that describes what kind of items this
	 * manufacturer makes.
	 * @see MaskingLibrary
	 * @param newMask the zapperMask
	 */
	public void setItemMask(String newMask);

	/**
	 * Returns the item mask that describes what kind of items this
	 * manufacturer makes.
	 * @see MaskingLibrary
	 * @return the zapperMask, compiled
	 */
	public MaskingLibrary.CompiledZMask getItemMask();

	/**
	 * Returns the item mask that describes what kind of items this
	 * manufacturer makes.
	 * @see MaskingLibrary
	 * @return the zapperMask, not compiled
	 */
	public String getItemMaskStr();

	/**
	 * Returns an Xml document representing this manufacturer.
	 * @return an Xml document representing this manufacturer.
	 */
	public String getXml();

	/**
	 * Sets an Xml document representing this manufacturer.
	 * This will "build out" the manufacturer object.
	 * @param xml Xml document representing this manufacturer.
	 */
	public void setXml(String xml);
}
