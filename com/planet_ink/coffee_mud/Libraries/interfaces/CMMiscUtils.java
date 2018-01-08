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
   Copyright 2005-2018 Bo Zimmerman

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
	 * form [CONDITION]number number number number, etc. E.G.: &gt;1 3 2 5 3 2.
	 * Each list must contain the given num of digits. If the condition falls
	 * within the given start of range and end of range, then the condition
	 * range of entries in the returned array is populated with the values on
	 * that row.
	 * @param condV the list of fully expressions
	 * @param numDigits the min number of digits in each expression
	 * @param startOfRange the starting range to return
	 * @param endOfRange the ending range to return &gt; startOfRange
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

	/**
	 * Drops all items from the given mob into the given room which are inside the given
	 * container (or are the given item).  The bodyFlag ensures that the contents are
	 * not marked for cleanup.  This method does the deed, but does not generate any
	 * new messages.  It also does not recover the state of the mob.
	 * @param mob the mob who is dropping
	 * @param room the room where it's being dropped
	 * @param thisContainer the item or container where the items must be
	 * @param bodyFlag true if the container is a body, false otherwise
	 */
	public void recursiveDropMOB(MOB mob, Room room, Item thisContainer, boolean bodyFlag);

	/**
	 * Returns a copy of the given item/container and a copy of every item in that
	 * container, recursively.
	 * @param theContainer the container or item to copy
	 * @return a list of copies of all the contents
	 */
	public List<Item> deepCopyOf(Item theContainer);

	/**
	 * This method removes all equipment from the mob and quickly attempt to re-wear/hold/wield
	 * it all where it originally was by issueing wear messages which are previewed and executed.
	 * In the end, the mob will be wearing everything that the system will let them wear.
	 * Moreover, this is all done silently.
	 * @param mob the mob to confirm the equipment of
	 */
	public void confirmWearability(MOB mob);

	/**
	 * Assumes that every inventory item, equipped item, and store inventory
	 * are for an NPC mob, where "rejuv" doesn't really matter.  In those cases,
	 * the "rejuv" stat is, for non-rivalrous items, a pct chance of it being
	 * retained on this mob.  For rivalrous items (two wielded swords, for example),
	 * it is a weighted chance of being selected.  Electronics items also have their
	 * random stats determined at this time. 
	 * @param mob the npc mob to process variable equipment on
	 * @return 0 for success, -1 if an admin is in the room, so nothing could be done.
	 */
	public int processVariableEquipment(MOB mob);

	/**
	 * Creates one of the deprecated traps depending on what sort of object is passed
	 * in, whether it has a lid or a lock, etc.
	 * @param unlockThis the exit, container, room, whatever
	 * @return the trap to add to the physical thing, or null
	 */
	public Trap makeADeprecatedTrap(Physical unlockThis);
	
	/**
	 * Creates and sets a deprecated trap on the given exit, room,
	 * container, or whatever.
	 * @see CMMiscUtils#makeADeprecatedTrap(Physical)
	 * @see CMMiscUtils#setTrapped(Physical, Trap)
	 * @see CMMiscUtils#fetchMyTrap(Physical)
	 * @param myThang the thing to set the trap on.
	 */
	public void setTrapped(Physical myThang);
	
	/**
	 * Sets the given deprecated trap on the given exit, room,
	 * container, or whatever.
	 * @see CMMiscUtils#makeADeprecatedTrap(Physical)
	 * @see CMMiscUtils#setTrapped(Physical)
	 * @param myThang the thing to set the trap on.
	 * @param theTrap the deprecated trap to set on it
	 */
	public void setTrapped(Physical myThang, Trap theTrap);
	
	/**
	 * Returns any trap found on the given thing, or null.
	 * @see CMMiscUtils#makeADeprecatedTrap(Physical)
	 * @param myThang the thing to check for a trap
	 * @return the trap found, or null
	 */
	public Trap fetchMyTrap(Physical myThang);

	/**
	 * If any mob (probably a player) is possessing the
	 * given mob, this will return that mob, or null
	 * @param mob the mob to check for possession
	 * @return the mob possessing the given mob
	 */
	public MOB getMobPossessingAnother(MOB mob);

	/**
	 * Normally just sends the message to the room by calling
	 * Room.send.  However, if the target of the message is 
	 * an exit, then the several exits involved would also
	 * informed by having executeMsg called on them.
	 * @param msg the message to send
	 * @param room the room to send the message to
	 * @param dirCode if known, the direction of the target exit
	 */
	public void roomAffectFully(CMMsg msg, Room room, int dirCode);

	/**
	 * Returns any corpses found in the given container, recursively.
	 * @param container the container that possibly has corpses
	 * @return a list of any corpses found, or an empty list
	 */
	public List<DeadBody> getDeadBodies(Environmental container);

	/**
	 * Resurrects the given body according to all system rules.
	 * 
	 * @param tellMob if the corpse could not be resurrected, tell this mob.
	 * @param corpseRoom room to bring the mob to after resurrection, probably same as body's location
	 * @param body the corpse to resurrect
	 * @param XPLevel if &gt; 0, and rules allow, bonus xp restored
	 * @return true if the resurrection happened, false otherwise
	 */
	public boolean resurrect(MOB tellMob, Room corpseRoom, DeadBody body, int XPLevel);

	/**
	 * This method parses the item ruinning rules and possibly ruins the given item
	 * by returning the ruined version.  Or it might do nothing
	 * and just return the item.
	 * @param mob the mob to get ruin policies for
	 * @param I the item to potentially ruin
	 * @return the ruined item, or the original item, depending
	 */
	public Item isRuinedLoot(MOB mob, Item I);

	/**
	 * Always converts the given item into the Ruined version
	 * @see CMMiscUtils#isRuinedLoot(MOB, Item)
	 * @param I the item to ruin
	 * @return the new, ruined version
	 */
	public Item ruinItem(Item I);

	/**
	 * Iterates through every mob and player in the game, replacing the old race
	 * object with the new one.
	 * @see CMMiscUtils#reloadCharClasses(CharClass)
	 * @param newR the new race object
	 * @param oldR the old race object
	 */
	public void swapRaces(Race newR, Race oldR);

	/**
	 * Iterates through every mob and player in the game, replacing the old char class
	 * object given with the new one of the same ID from CMClass.
	 * @see CMMiscUtils#swapRaces(Race, Race)
	 * @param oldC the old charclass object
	 */
	public void reloadCharClasses(CharClass oldC);

	/**
	 * Returns whether the given item can be destroyed by the given mob, probably magically.
	 * This is a recursive check if the item is a container.
	 * @param mob the mob who wants to destroy the item
	 * @param I the item to destroy
	 * @param ignoreBodies true to ignore corpse checks, false otherwise
	 * @return true if the item can be destroyed, false otherwise
	 */
	public boolean canBePlayerDestroyed(final MOB mob, final Item I, final boolean ignoreBodies);

	/**
	 * Calls unInvoke on all effects on the given environmental.  
	 * This may not cause the effects to disappear, depending on the
	 * behavior of each effect.
	 * @param E the object to diseffect
	 * @return true if the item still exists, false if it was destroyed
	 */
	public boolean disInvokeEffects(Environmental E);

	/**
	 * Removes magical effects from wands, and other spell holders,
	 * deletes any effects after attempting to uninvoke.  This method
	 * probably needs more thought, since not all effects are
	 * magic (though they usually are)
	 * 
	 * The return value is a very strange number: 0 if the item is 
	 * destroyed, -999 if nothing done, or the item level minus the 
	 * magic value of what was done to it.
	 * @param target the object to disenchant.
	 * @return a bizarre number 
	 */
	public int disenchantItem(Item target);
	
	/**
	 * Absolutely returns the correct race when mixing races of the
	 * two given IDs.  Applies system rules to the generation.
	 * 
	 * @param race1 the mother race
	 * @param race2 the father race
	 * @param ignoreRules TODO
	 * @return the mixed race
	 */
	public Race getMixedRace(String race1, String race2, boolean ignoreRules);
	
	/**
	 * Breaks apart a given generic mixed race ID to figure
	 * out which races were combined to make it up.
	 * @param raceID the raceID to break apart
	 * @return a list of constituant races, or empty.
	 */
	public List<Race> getConstituantRaces(final String raceID);
}
