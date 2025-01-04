package com.planet_ink.coffee_mud.Libraries.interfaces;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

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
/*
   Copyright 2005-2025 Bo Zimmerman

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
 * The Faction Manager is a storage class for all game Factions,
 * handling lookups, storage, retrieval, and relations between them.
 * There are also lookups for various faction parts, such as faction
 * range codes, descriptions, and names.
 *
 * It also includes references to specific factions, such as
 * alignment or inclination.
 *
 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction
 *
 * @author Bo Zimmerman
 *
 */
public interface FactionManager extends CMLibrary, Tickable
{
	/**
	 * Adds a new faction to the memory cache, or modify one
	 * with the same id.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction
	 * @see FactionManager#removeFaction(String)
	 * @see FactionManager#resaveFaction(Faction)
	 *
	 * @param F the faction to add
	 * @return true if the faction is disabled, false otherwise
	 */
	public boolean addFaction(Faction F);

	/**
	 * Deletes the faction from the memory cache with the
	 * given faction id, or all of the factions if the faction
	 * id is null.  So be careful.
	 *
	 * @see FactionManager#addFaction(Faction)
	 * @see FactionManager#resaveFaction(Faction)
	 *
	 * @param factionID the id to remove, or null for all
	 * @return true if a faction was removed, or false otherwise
	 */
	public boolean removeFaction(String factionID);

	/**
	 * Re-saves the given faction back to the cmfs.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction
	 * @see FactionManager#addFaction(Faction)
	 * @see FactionManager#removeFaction(String)
	 *
	 * @param F the faction to save
	 * @return "" if all is well, or an error message
	 */
	public String resaveFaction(Faction F);

	/**
	 * Returns the enumeration of all the factions
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction
	 * @see FactionManager#numFactions()
	 * @see FactionManager#getFaction(String)
	 * @see FactionManager#getFactionByNumber(int)
	 *
	 * @return the enumeration of all the factions
	 */
	public Enumeration<Faction> factions();

	/**
	 * Return the total number of factions
	 *
	 * @see FactionManager#factions()
	 * @see FactionManager#getFaction(String)
	 * @see FactionManager#getFactionByNumber(int)
	 *
	 * @return the total number of factions
	 */
	public int numFactions();

	/**
	 * Returns the faction with the given faction id.
	 * If the faction exists, but is not yet cached,
	 * this function will cause the faction to be
	 * brought into memory.
	 *
	 * @see FactionManager#factions()
	 * @see FactionManager#numFactions()
	 * @see FactionManager#getFactionByNumber(int)
	 *
	 * @param factionID the faction id to look for
	 * @return the faction found, or null
	 */
	public Faction getFaction(String factionID);

	/**
	 * Returns the faction with the given enum index
	 * number.
	 *
	 * @see FactionManager#factions()
	 * @see FactionManager#numFactions()
	 * @see FactionManager#getFaction(String)
	 *
	 * @param index the index number
	 * @return the faction found, or null
	 */
	public Faction getFactionByNumber(int index);

	/**
	 * Returns a friendly column based list of
	 * all factions, formatted for 80 col screen.
	 *
	 * @return the viewable list of factions
	 */
	public String listFactions();

	/**
	 * Given a semi-colon delimited list of faction
	 * ids, this method will cause them all to be
	 * cached into memory, if they exist.
	 *
	 * @param factionList the list of factions
	 */
	public void reloadFactions(String factionList);

	/**
	 * Clears out all cached factions, which
	 * is part of the shutdown process
	 */
	public void clearFactions();

	/**
	 * Returns whether the given string is, in fact, a range
	 * code for any existing faction.  Range codes are like
	 * PUREGOOD, PUREEVIL, etc..
	 * @see Faction.FRange
	 *
	 * @param key the string to check
	 * @return true if the key is a range code
	 */
	public boolean isRangeCodeName(String key);

	/**
	 * Returns a friendly list of the ranges and the faction
	 * name whose code matches a range in the given faction.
	 * If multiples are found, they may be combined with
	 * the word AND or OR, for example.  This method does
	 * a full scan of all ranges in one faction.
	 *
	 * @param FR the faction range to match
	 * @param andOr the word to use when combining multiples
	 * @return the list of factions and their ranges that match
	 */
	public String rangeDescription(Faction.FRange FR, String andOr);

	/**
	 * Returns the Faction that contains the given range code name.
	 * @see FactionManager#getFactionRangeByCodeName(String)
	 * @see FactionManager#getRange(String, int)
	 * @see FactionManager#getRanges(String)
	 *
	 * @param rangeCodeName the range code name to search for
	 * @return the faction, or null
	 */
	public Faction getFactionByRangeCodeName(String rangeCodeName);

	/**
	 * Returns the faction range object whose range code matches
	 * the given string.
	 *
	 * @see FactionManager#getFactionByRangeCodeName(String)
	 * @see FactionManager#getRange(String, int)
	 * @see FactionManager#getRanges(String)
	 *
	 * @param rangeCodeName the range code name to search for
	 * @return the faction range object, or null
	 */
	public Faction.FRange getFactionRangeByCodeName(String rangeCodeName);

	/**
	 * Returns the faction range from the faction with the given faction
	 * id whose range covers the given faction value.
	 *
	 * @see FactionManager#getFactionByRangeCodeName(String)
	 * @see FactionManager#getFactionRangeByCodeName(String)
	 * @see FactionManager#getRanges(String)
	 *
	 * @param factionID the faction id of the faction to check
	 * @param faction the amount of faction to find the range for
	 * @return the faction range object, or null
	 */
	public Faction.FRange getRange(String factionID, int faction);

	/**
	 * Returns an enumerator of all faction range objects in the
	 * faction with the given id.
	 *
	 * @see FactionManager#getFactionByRangeCodeName(String)
	 * @see FactionManager#getFactionRangeByCodeName(String)
	 * @see FactionManager#getRange(String, int)
	 *
	 * @param factionID the faction id of the faction to check
	 * @return the enumerator of faction range objects
	 */
	public Enumeration<Faction.FRange> getRanges(String factionID);

	/**
	 * Returns whether the given string is a legitimate,
	 * loadable faction id.  If so, it will load the faction
	 * in the course of checking.
	 * @see FactionManager#isFactionLoaded(String)
	 *
	 * @param key the string to check
	 * @return true if its a legit faction id
	 */
	public boolean isFactionID(String key);

	/**
	 * Returns whether the given string is the faction
	 * id of an already loaded and cached faction.
	 *
	 * @see FactionManager#isFactionID(String)
	 *
	 * @param key the faction id to check
	 * @return true if the faction is already loaded
	 */
	public boolean isFactionLoaded(String key);

	/**
	 * Returns the faction object with the given
	 * exact case-insensitive display name.
	 *
	 * @see FactionManager#getFaction(String)
	 *
	 * @param factionNamed the display name to look for
	 * @return the faction obj or null
	 */
	public Faction getFactionByName(String factionNamed);

	/**
	 * Returns the friendly faction display name
	 * associated with the given faction id
	 *
	 * @param factionID the faction id to look for
	 * @return the friendly name, or empty string ""
	 */
	public String getName(String factionID);

	/**
	 * Returns the absolute minimum possible faction
	 * value that the faction with the given id can
	 * be assigned.
	 *
	 * @see FactionManager#getMaximum(String)
	 *
	 * @param factionID the faction id to look for
	 * @return the minimum possible value or 0
	 */
	public int getMinimum(String factionID);

	/**
	 * Returns the absolute maximum possible faction
	 * value that the faction with the given id can
	 * be assigned.
	 *
	 * @see FactionManager#getMinimum(String)
	 *
	 * @param factionID the faction id to look for
	 * @return the maximum possible value or 0
	 */
	public int getMaximum(String factionID);

	/**
	 * Returns the percentage of the total faction numeric
	 * range (0.00 to 100.00) that the given faction number
	 * represents.  E.g. a faction with -1000 to 1000 would return
	 * 50.00% for a faction value of 0.
	 *
	 * It returns 0 if the faction id is not found.
	 *
	 * @see FactionManager#getPercent(String, int)
	 * @see FactionManager#getPercentFromAvg(String, int)
	 *
	 * @param factionID the faction id to get a range percent for.
	 * @param faction the faction number to find the percent in
	 * @return the percentage in 0-100, with 2 decimal places
	 */
	public double getRangePercent(String factionID, int faction);

	/**
	 * Returns the percentage of the total faction numeric
	 * range (0 to 100) that the given faction number
	 * represents.  E.g. a faction with -1000 to 1000 would return
	 * 50% for a faction value of 0.
	 *
	 * It returns 0 if the faction id is not found.
	 *
	 * @see FactionManager#getRangePercent(String, int)
	 * @see FactionManager#getPercentFromAvg(String, int)
	 *
	 * @param factionID the faction id to get a range percent for.
	 * @param faction the faction number to find the percent in
	 * @return the percentage in 0-100
	 */
	public int getPercent(String factionID, int faction);

	/**
	 * Returns the percentage of the total faction numeric
	 * range (0 to 100) that the given faction number
	 * represents.  E.g. a faction with -1000 to 1000 would return
	 * 50% for a faction value of 0.
	 *
	 * It returns 0 if the faction id is not found.
	 *
	 * @see FactionManager#getRangePercent(String, int)
	 * @see FactionManager#getPercent(String, int)
	 * @see FactionManager#getPercentFromAvg(String, int)
	 *
	 * @param factionID the faction id to get a range percent for.
	 * @param faction the faction number to find the percent in
	 * @return the percentage in 0-100
	 */
	public int getPercentFromAvg(String factionID, int faction);

	/**
	 * Gets the absolute number of points in this faction,
	 * essentially the max - min.
	 *
	 * @param factionID the faction id to check
	 * @return the max - min, or 0
	 */
	public int getTotal(String factionID);

	/**
	 * Returns a random faction number in the range
	 * from its minimum to maximum, given a faction
	 * id.
	 *
	 * @param factionID the faction id to check
	 * @return a random faction number
	 */
	public int getRandom(String factionID);

	/**
	 * Returns which faction tag (FacTag) enum
	 * is represented by the given string, which is
	 * typically a parameter from a faction definition/ini
	 * file.
	 * @see Faction.FacTag
	 *
	 * @param tag the string to check
	 * @return the FacTag object, or null
	 */
	public Faction.FacTag getFactionTag(String tag);

	/**
	 * Creates a reaction faction based from a template, to represent a specific
	 * entity in the reaction category.  Categories are things like areas, races,
	 * planes, etc.
	 *
	 * e.g. makeReactionFaction("AREA_",A.ID(),A.Name(),areaCode,"examples/areareaction.ini")
	 *
	 * @param prefix prefix to add to the faction id to identify the reaction category
	 * @param classID the literal entity class/ID() - for use in masks
	 * @param Name the friendly name of the entity, prob also for masks or comm.
	 * @param code the unique identifier for the entity, w/o spaces
	 * @param baseTemplateFilename the cmfs path of the template to base the reaction on
	 * @return the fully formed and cached reaction faction, or null
	 */
	public Faction makeReactionFaction(final String prefix, final String classID, final String Name, final String code, final String baseTemplateFilename);

	/**
	 * If area reactions are enabled, and the given area has one of them, then this
	 * will return the areas faction.
	 *
	 * @see FactionManager#getSpecialFactions(MOB, Room)
	 *
	 * @param A the Area to check
	 * @return a Faction object, or null
	 */
	public Faction getSpecialAreaFaction(final Area A);

	/**
	 * If reaction factions are enabled, and the given location has one of them,
	 * then this will return the applicable faction.
	 *
	 * @see FactionManager#getSpecialAreaFaction(Area)
	 *
	 * @param mob the mob involved
	 * @param R the mobs location
	 * @return a Faction object, or null
	 */
	public Faction[] getSpecialFactions(final MOB mob, final Room R);

	/**
	 * Returns what type of ability mask is represented by the string,
	 * as one of the FAbilityMaskType enums, which includes things like
	 * the Ability Code, Domain, Ability Flag (usually prefixed with "!"),
	 * or an Ability ID
	 * @see FAbilityMaskType
	 *
	 * @param strflag the string to find a flag type rep of
	 * @return the ability mask type
	 */
	public FAbilityMaskType getAbilityFlagType(String strflag);

	/**
	 * Returns a cmfs path, relative to /resources (so, not including
	 * that prefix) that the given faction id should be saved into.
	 * It will prefer /resources/factions, of course.
	 *
	 * @param factionID the faction id to build a filename for
	 * @return the full faction id filename
	 */
	public String makeFactionFilename(String factionID);

	/**
	 * Returns whether the given mob has the faction represented
	 * by the range, and whether their faction value falls within
	 * that range.
	 *
	 * @param mob the mob to check
	 * @param rangeCode the faction range object to check the mob for
	 * @return true if the mob is in that range, false otherwise
	 */
	public boolean isFactionedThisWay(MOB mob, Faction.FRange rangeCode);

	/**
	 * This function makes sure that any publicly available factions
	 * which the given mob in the given location are applied to the
	 * given mob, if they can be.  This is a typical login/come to life
	 * method.
	 *
	 * Normally a faction can be set to not apply auto-values, but
	 * that behavior can be over-ridden with forceAutoCheck.  There is
	 * really no good reason to ever do this, but it's there
	 *
	 * @param mob the player/npc mob to confirm against existing factions
	 * @param R the room the mob is in
	 * @param forceAutoCheck just set this to false
	 */
	public void updatePlayerFactions(MOB mob, Room R, boolean forceAutoCheck);

	/**
	 * Posts an official faction change event to the system
	 * without actually designating a faction that changed.  This is
	 * weird and makes no sense and will prob never be used omg why is
	 * this even in here?!
	 *
	 * @param mob the mob whose factions have changed
	 * @param victim the victim causing the change, if any
	 * @param amount the amount of factions plus or minus
	 * @param quiet true to be silent about the change, or false otherwise
	 * @return true if the message went through w/o problems
	 */
	public boolean postChangeAllFactions(MOB mob, MOB victim, int amount, boolean quiet);

	/**
	 * Posts an official faction change even to the system
	 * that is related to a skill that changes faction.
	 *
	 * @param mob the mob whose faction has changed
	 * @param skillA the skill that caused the faction change
	 * @param factionID the faction id of the faction to change
	 * @param amount the amount, plus or minus, to change
	 * @return true if the faction changes goes through ok
	 */
	public boolean postSkillFactionChange(MOB mob, Ability skillA, String factionID, int amount);

	/**
	 * This is an archon/system editor function to modify the specific
	 * attributes and parameters of the given faction.  It requires a
	 * non-monster user who can edit the faction.
	 *
	 * @param mob the player doing the editing, with a session
	 * @param meF the faction object being edited
	 * @throws IOException session i/o errors
	 */
	public void modifyFaction(MOB mob, Faction meF) throws IOException;

	/**
	 * Returns the faction ID assigned to 'alignment' (good, evil, neutral)
	 * @return the faction ID assigned to 'alignment' (good, evil, neutral)
	 */
	public String getAlignmentID();

	/**
	 * Changes the alignment of the given mob to the value of the
	 * Align enum given.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
	 *
	 * @param mob the mob to change
	 * @param newAlignment the new alignment by enum
	 */
	public void setAlignment(MOB mob, Faction.Align newAlignment);

	/**
	 * A legacy method for changing alignment according to
	 * the legacy values where 1000 is pure good, and 0 is evil.
	 *
	 * @param mob the mob to change
	 * @param oldRange the 0-1000 value
	 */
	public void setAlignmentOldRange(MOB mob, int oldRange);

	/**
	 * Returns the percentage 0-100 of distance that the given
	 * faction value is from the given alignment enum.  This is
	 * used when figuring out the chance of successfully using
	 * an evil skill when one is only partially or not really evil.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
	 * @see FactionManager#isAlignmentLoaded(com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignMedianFacValue(com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignEnum(String)
	 *
	 * @param faction the faction value
	 * @param eq the alignment enum
	 * @return the percentage of distance, where 0 is close, and 100 is far.
	 */
	public int getAlignPurity(int faction, Faction.Align eq);

	/**
	 * Returns the purist alignment/inclination faction value associated with the given
	 * Align enum.  Also works for inclinatino.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
	 * @see FactionManager#isAlignmentLoaded(com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignPurity(int, com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignEnum(String)
	 *
	 * @param eq the align fac value to return a pure value for
	 * @return the pure value
	 */
	public int getAlignMedianFacValue(Faction.Align eq);

	/**
	 * Returns the Align enum that matches the given string,
	 * or INDIFF if not found.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
	 * @see FactionManager#isAlignmentLoaded(com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignMedianFacValue(com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignPurity(int, com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 *
	 * @param str the Align enum string
	 * @return the Align enum object or INDIFF
	 */
	public Faction.Align getAlignEnum(String str);

	/**
	 * Returns whether any factions are loaded that grant alignment.
	 * This includes the Alignment faction of course, but allows a bit
	 * of flexibility.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
	 * @see FactionManager#getAlignPurity(int, com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignMedianFacValue(com.planet_ink.coffee_mud.Common.interfaces.Faction.Align)
	 * @see FactionManager#getAlignEnum(String)
	 *
	 * @param align the align enum to check
	 * @return true if any faction is loaded that uses it
	 */
	public boolean isAlignmentLoaded(final Faction.Align align);

	/**
	 * Returns the faction ID assigned to 'inclination' (lawful, mod, chaotic)
	 * @return the faction ID assigned to 'inclination' (lawful, mod, chaotic)
	 */
	public String getInclinationID();

	/**
	 * Returns the percentage 0-100 of distance that the given
	 * faction value is from the given alignment enum.  This is
	 * used when figuring out the chance of successfully using
	 * a chaotic skill when one is only partially or not really chaotic.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
	 *
	 * @param faction the faction value
	 * @param eq the alignment enum
	 * @return the percentage of distance, where 0 is close, and 100 is far.
	 */
	public int getInclinationPurity(final int faction, final Faction.Align eq);

	/**
	 * This enum identifies the different types of ability masks
	 * and flags that are found in faciton definitions.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum FAbilityMaskType
	{
		ID,
		ACODE,
		DOMAIN,
		FLAG  // usually denoted with a ! to avoid conflict with prior
	}

}
