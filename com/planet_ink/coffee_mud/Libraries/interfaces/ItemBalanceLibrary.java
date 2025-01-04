package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
/*
   Copyright 2008-2025 Bo Zimmerman

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
 * The setting of combat values on armor and weapons to the end
 * of more balanced combat is managed here.  Items can be increased
 * or decreased in power level, altered to fit a level, etc.
 *
 * @author Bo Zimmerman
 *
 */
public interface ItemBalanceLibrary extends CMLibrary
{
	/**
	 * Returns the full power level of the given armor,
	 * or weapon.  Will also inspect its affects.
	 *
	 * @param I the item to get the power level of
	 * @return the power level
	 */
	public int timsLevelCalculator(Item I);

	/**
	 * Returns the full power level of the given armor,
	 * or weapon.
	 *
	 * @param I the item to get the power level of
	 * @param props any power affecting effects
	 * @return the power level
	 */
	public int timsLevelCalculator(Item I, List<Ability> props);

	/**
	 * Lowers the base attack and damage of the give weapon
	 * based on its level and other characteristics.
	 *
	 * @param W the weapon to adjust
	 * @param adjA any property that also impacts attack/dmg
	 */
	public void toneDownWeapon(Weapon W, Ability adjA);

	/**
	 * Lowers the base armor of the give armor item
	 * based on its level and other characteristics.
	 *
	 * @param A the armor to adjust
	 * @param adjA any property that also impacts armor
	 */
	public void toneDownArmor(Armor A, Ability adjA);

	/**
	 * Lowers the base gold value of the given armor
	 * or weapon item based on its level and other
	 * characteristics.  A balancing method.
	 *
	 * @param I the armor or weapon
	 * @return true if a change was made, false otherwise
	 */
	public boolean toneDownValue(Item I);

	/**
	 * Returns the apparent power level of the item
	 * based entirely on basic mundane stats, even if
	 * some stats come from a property.
	 *
	 * @param I the weapon or armor to inspect
	 * @return the mundane power level
	 */
	public int timsBaseLevel(Item I);

	/**
	 * Adjusts the mundane stats of the given armor or
	 * weapon item to reflect its present level.
	 *
	 * @param I the item to adjust for balance
	 */
	public void balanceItemByLevel(Item I);

	/**
	 * Calculates the base value of this armor or weapon.
	 * @param I the armor or weapon
	 * @return the base value.
	 */
	public int calculateBaseValue(final Item I);

	/**
	 * Given an item and a new level or 0, this will adjust the stats of the given item
	 * to reflect the desired level or its existing one.  It will return a description
	 * of the changes if you send a stringbuffer.
	 * Obviously, this only works on things like wands, ammunition, weapons, and armor.
	 *
	 * @param I the item to potentially modify
	 * @param lvlOr0 0 to use the existing level, or a level to adjust the item to
	 * @param preferMagic true to prefer adding magical bonuses, false to stay mundane
	 * @param changes null, or a strinbuffer to put changes list into
	 * @return true if a change was made, false otherwise
	 */
	public boolean itemFix(Item I, int lvlOr0, boolean preferMagic, StringBuffer changes);

	/**
	 * Returns a list of all the effects on the given item that
	 * contribute to its power level.
	 *
	 * @param I the item to inspect
	 * @return the effects that make it great
	 */
	public List<Ability> getTimsAdjResCast(Item I);

	/**
	 * Given a percent change, this method MIGHT add an enchantment
	 * or cuse of various levels to the give armor, ammunition,
	 * or weapon piece.
	 *
	 * @param I the item to possibly enchant
	 * @param pct the % chance to enchant
	 * @return the item again
	 */
	public Item enchant(Item I, int pct);

	/**
	 * Returns a map of key value pairs showing what various combat stats
	 * on the given item SHOULD be given the level and other particulars.
	 * Weapon key values: MINRANGE, MAXRANGE, DAMAGE, ATTACK, VALUE
	 * Armor key values: ARMOR, VALUE, WEIGHT
	 *
	 * @param I the item type to inspect
	 * @param level the level you want the item to be
	 * @param material the material of the item
	 * @param hands the number of hands for the item (weapon)
	 * @param wclass the weapon class
	 * @param reach the weapon max range
	 * @param worndata the proper worn location bitmap
	 * @return the map of fields
	 */
	public Map<String, String> timsItemAdjustments(Item I, int level, int material, int hands, int wclass, int reach, long worndata);
}
