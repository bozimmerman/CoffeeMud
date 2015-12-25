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
import com.planet_ink.coffee_mud.Libraries.CMMap;
import com.planet_ink.coffee_mud.Libraries.CoffeeUtensils;
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2005-2015 Bo Zimmerman

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
 * The MiscUtils (Miscellaneous Utilities) library should not exist.
 * Its purpose is to expose universal functionality that doesn't
 * seem to fit anywhere else.  Usually, however, it does fit
 * elsewhere just fine, but the elsewhere felt too crowded already
 * to receive it.  I'm sure this library will always exist, but the
 * interface is definitely subject to change.
 * 
 * @author Bo Zimmerman
 */
public interface CMMiscUtils extends CMLibrary
{
	/**
	 * Checks the given player mob for the format of their prompt in
	 * their playerstats and generates a fully formed prompt, complete
	 * with all variables filled in.
	 * @param mob the mob to build a prompt for
	 * @return the fully filled in customized prompt string
	 */
	public String builtPrompt(MOB mob);

	/**
	 * Returns the current mud-month-day-year for the object, depending
	 * on the timeclock where the environmental is located.
	 * @param E the object curious about the date
	 * @return the date string
	 */
	public String getFormattedDate(Environmental E);
	
	/**
	 * Returns a rediculous best guess on the amount of memory used
	 * by the given environmental.  
	 * @param E the object to check for a footprint of
	 * @param number the accuracy -- higher is better
	 * @return the amount of memory used, very approximately
	 */
	public double memoryUse ( Environmental E, int number );
	
	/**
	 * Nice english comma-delimited list, with oxford commas
	 * and trailing "and" or "or" at the end.  If the list is
	 * of environmental, it will use the name, otherwise, it 
	 * casts everything as a string and lists it.
	 * @param V the objects to list
	 * @param andTOrF true for trailing and, false for trailing or
	 * @return the readable comma list.
	 */
	public String niceCommaList(List<?> V, boolean andTOrF);
	
	/**
	 * This strange method takes a list of space-delimited expressions of the
	 * form [CONDITION]number number number number, etc. E.G.: >1 3 2 5 3 2.
	 * Each list must contain the given num of digits. If the condition falls
	 * within the given start of range and end of range, then the condition
	 * range of entries in the returned array is populated with the values on
	 * that row.
	 * @param condV the list of fully expressions
	 * @param numDigits the min number of digits in each expression
	 * @param startOfRange the starting range to return
	 * @param endOfRange the ending range to return > startOfRange
	 * @return the list of number lists
	 */
	public long[][] compileConditionalRange(List<String> condV, int numDigits, final int startOfRange, final int endOfRange);

	/**
	 * Outfits the given mob with the list of given items.  If the mob
	 * does not have an item with the same name as one on the list, it 
	 * is given to them magically.  If they can, they will then wear it.
	 * @param mob the mob to outfit
	 * @param items the items to outfit the mob with
	 */
	public void outfit(MOB mob, List<Item> items);
	
	/**
	 * Returns the language being spoke by the given object (mob, usually).
	 * A null return usually means Common. 
	 * @param P the mob to check
	 * @return the language being spoken.
	 */
	public Language getLanguageSpoken(Physical P);
	
	/**
	 * Returns whether the given Item is reachable by the given mob.
	 * It may not be reachable if the mob is riding something, and the
	 * item is on the ground, etc.  If the object given is not an
	 * item, or null, true is always returned.
	 * @param mob the mob who wants to reach
	 * @param E the Item to reach for
	 * @return true, or false if it's unreachable
	 */
	public boolean reachableItem(MOB mob, Environmental E);
	
	/**
	 * Recursively extinguishes everything from the given target on down.
	 * If a room is given, everything in the room, including the room,
	 * is extinguished.   Ordinary fires and torches also go out.  
	 * The mundane flag is to prevent extinguishing elementals and magic
	 * flame spells.
	 * @param source the mob doing the extinguishing
	 * @param target the thing to extinguish
	 * @param mundane true to skip magic and elemental targets
	 */
	public void extinguish(MOB source, Physical target, boolean mundane);
	
	/**
	 * Given the allowedArmorLevel code and the mob, this method returns
	 * whether the given mob is only wearing permitted items on the
	 * applicable armor slots.  
	 * @see CharClass#ARMOR_DESCS
	 * @param mob the mob to check
	 * @param allowedArmorLevel the allowed armor level
	 * @return true if the mob is good to go, false otherwise
	 */
	public boolean armorCheck(MOB mob, int allowedArmorLevel);
	
	/**
	 * Given the allowedArmorLevel code and the mob, this method returns
	 * whether the given mob is permitted to wear the given item is
	 * applicable armor slots given the armor level code.  
	 * @see CharClass#ARMOR_DESCS
	 * @param mob the mob to check
	 * @param I the item to check
	 * @param allowedArmorLevel the allowed armor level
	 * @return true if the mob is good to go for that item, false otherwise
	 */
	public boolean armorCheck(MOB mob, Item I, int allowedArmorLevel);
	public void recursiveDropMOB(MOB mob, Room room, Item thisContainer, boolean bodyFlag);
	public List<Item> deepCopyOf(Item theContainer);
	public void confirmWearability(MOB mob);
	public int processVariableEquipment(MOB mob);

	public Trap makeADeprecatedTrap(Physical unlockThis);
	public void setTrapped(Physical myThang, boolean isTrapped);
	public void setTrapped(Physical myThang, Trap theTrap, boolean isTrapped);
	public Trap fetchMyTrap(Physical myThang);

	public MOB getMobPossessingAnother(MOB mob);
	public void roomAffectFully(CMMsg msg, Room room, int dirCode);
	public List<DeadBody> getDeadBodies(Environmental container);
	public boolean resurrect(MOB tellMob, Room corpseRoom, DeadBody body, int XPLevel);

	public Item isRuinedLoot(MOB mob, Item I);

	public void swapRaces(Race newR, Race oldR);
	public void reloadCharClasses(CharClass oldC);

	public boolean canBePlayerDestroyed(final MOB mob, final Item I, final boolean ignoreBodies);
	
	public boolean disInvokeEffects(Environmental E);
	public int disenchantItem(Item target);
}
