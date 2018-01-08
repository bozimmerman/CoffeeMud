package com.planet_ink.coffee_mud.Common.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultFaction;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/*
 * Copyright 2000-2018 Bo Zimmerman Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
/**
 * A Faction is an arbitrary numeric range, where different mobs/players can be
 * within that range, if they have the faction at all. Factions can be
 * programmatically set to change due to events that occur to/around the mob,
 * and adjust themselves relative to other factions. Subsets of the faction can
 * be given readable names for display to the user.
 *
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#fetchFaction(String)
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addFaction(String, int)
 */
public interface Faction extends CMCommon, MsgListener, Contingent
{
	/**
	 * Initializes a new faction with default values
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#initializeFaction(StringBuffer, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
	 * @param aname the factionID (and default name)
	 */
	public void initializeFaction(String aname);

	/**
	 * Initializes a new faction from a faction.ini properties formatted document,
	 * and a given new faction ID
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#initializeFaction(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#getINIDef(String, String)
	 * @param file the ini properties style document
	 * @param fID the new factionID
	 */
	public void initializeFaction(StringBuffer file, String fID);

	/**
	 * Returns the value of a given internal faction variable.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#TAG_NAMES
	 * @param tag the tag to get the value of
	 * @return the value of the given tag
	 */
	public String getTagValue(String tag);

	/**
	 * Retrieves an entry for an ini properties definition document that describes this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#getINIDef(String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#initializeFaction(StringBuffer, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#TAG_NAMES
	 * @param tag the tag to retrieve a properties definition for
	 * @param delimeter if the tag represents a list, this is the delimiter for entries.
	 * @return the ini properties definition entry for the tag
	 */
	public String getINIDef(String tag, String delimeter);

	/**
	 * Returns a FactionData object for the given mob to store his faction
	 * information in.  It will contain all the affects and behaviors,
	 * and other information necessary to maintain a relationship between
	 * the given mob and this faction.
	 * Any parameters should be set on the affects or behaviors before returning them.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FData
	 * @param mob the mob to generate affects and behaviors for
	 * @return a FactionData object with all the appropriate affects and behaviors
	 */
	public FData makeFactionData(MOB mob);

	/**
	 * Updates the given FactionData object that the given mob to store his faction
	 * information in.  It will contain all the affects and behaviors,
	 * and other information necessary to maintain a relationship between
	 * the given mob and this faction.
	 * Any parameters should be set on the affects or behaviors before returning them.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FData
	 * @param mob the mob to generate affects and behaviors for
	 * @param data the old faction data object
	 */
	public void updateFactionData(MOB mob, FData data);

	/**
	 * Checks to see if the given mob has this faction.  Same as checking if
	 * mob.fetchFaction(this.factionID())!=Integer.MAX_VALUE.
	 * @param mob the mob to check
	 * @return true if the mob has this faction, false otherwise
	 */
	public boolean hasFaction(MOB mob);

	/**
	 * Returns the given faction value, as a percent from minimum of the range
	 * of this faction
	 * @param faction the faction value to convert to a percent
	 * @return the percentage value (0-100)
	 */
	public int asPercent(int faction);

	/**
	 * Returns true if this faction is presently specified in the coffeemud ini
	 * file entry "FACTIONS", thereby designating that this faction is loaded at
	 * boot-time, as opposed to run-time.
	 * @return true if its pre-loaded, false otherwise.
	 */
	public boolean isPreLoaded();

	/**
	 * Returns true if this faction is presently specified in the coffeemud ini
	 * file entry "DISABLE", thereby designating that this faction is currently
	 * disabled.  See also {@link #disable(boolean)}
	 * @return true if its disabled, false otherwise.
	 */
	public boolean isDisabled();

	/**
	 * Disables this faction, as if it had been added to the DISABLE= entry in the
	 * coffeemud.ini file.  See also {@link #isDisabled()}
	 * @param truefalse disable true to disable, false otherwise
	 */
	public void disable(boolean truefalse);

	/**
	 * Returns the given value faction value, as a percent from average of the
	 * range values of this faction.
	 * @param faction the faction value to convert to a percent
	 * @return the percentage value (0-100)
	 */
	public int asPercentFromAvg(int faction);

	/**
	 * Returns a random value within the valid range of this faction
	 * @return a random valid value
	 */
	public int randomFaction();

	/**
	 * The official, unique faction id of this faction.  FactionIDs are usually
	 * the CoffeeMud VFS path from the resources directory, of the properties ini
	 * file that defines the faction.  The ID (and therefore the properties file location)
	 * should not be changed once a faction is "deployed".
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setFactionID(String)
	 * @return the unique id of this faction
	 */
	public String factionID();

	/**
	 * Sets the official, unique faction id of this faction.  FactionIDs are usually
	 * the CoffeeMud VFS path from the resources directory, of the properties ini
	 * file that defines the faction.  The ID (and therefore the properties file location)
	 * should not be changed once a faction is "deployed".
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
	 * @param newStr the new unique id of this faction
	 */
	public void setFactionID(String newStr);

	/**
	 * The friendly, displayable name of this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setName(String)
	 * @return the name of this faction
	 */
	@Override
	public String name();

	/**
	 * Sets the friendly, displayable name of this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#name()
	 * @param newStr the new name of this faction
	 */
	public void setName(String newStr);

	/**
	 * Gets the filename of a file, from the resources directory,
	 * that is displayed to users when they are given the choice
	 * of a starting value to this faction.  Requires more than
	 * one choice range be available.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#findChoices(MOB)
	 * @return the filename of the choice description file
	 */
	public String choiceIntro();

	/**
	 * Sets the filename of a file, from the resources directory,
	 * that is displayed to users when they are given the choice
	 * of a starting value to this faction.  Requires more than
	 * one choice range be available.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#findChoices(MOB)
	 * @param newStr the new filename of the choice description file
	 */
	public void setChoiceIntro(String newStr);

	/**
	 * Gets the lowest absolute range value
	 * @return the lowest absolute range value
	 */
	public int minimum();

	/**
	 * Gets the median absolute range value
	 * @return the median absolute range value
	 */
	public int middle();

	/**
	 * Returns the difference between the highest and lowest range value
	 * @return the difference between the highest and lowest range value
	 */
	public int difference();

	/**
	 * Returns the highest absolute range value
	 * @return the highest absolute range value
	 */
	public int maximum();

	/**
	 * Returns the string code describing how a faction-holders experience
	 * changes from killing another faction holder affect his own faction value.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_NAMES
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_DESCS
	 * @return the string code for xp changes to faction changes
	 */
	public String experienceFlag();

	/**
	 * Sets the string code describing how a faction-holders experience
	 * changes from killing another faction holder affect his own faction value.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_NAMES
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_DESCS
	 * @param newStr the new string code for xp changes to faction changes
	 */
	public void setExperienceFlag(String newStr);

	/**
	 * Returns whether this faction is displayed in the player Score command.
	 * @return true if displayed in Score, false otherwise
	 */
	public boolean showInScore();

	/**
	 * Sets whether this faction is displayed in the player Score command.
	 * @param truefalse true if displayed in Score, false otherwise
	 */
	public void setShowInScore(boolean truefalse);

	/**
	 * Returns whether this factions value is shown in certain special admins commands.
	 * @return true if displayed in special admin commands, false otherwise
	 */
	public boolean showInSpecialReported();

	/**
	 * Sets whether this factions value is shown in certain special admins commands.
	 * @param truefalse true if displayed in special admin commands, false otherwise
	 */
	public void setShowInSpecialReported(boolean truefalse);

	/**
	 * Returns whether this factions value is shown as a line item in mob editors
	 * @return true if displayed in mob editors, false otherwise
	 */
	public boolean showInEditor();

	/**
	 * Sets whether this factions value is shown as a line item in mob editors
	 * @param truefalse true if displayed in mob editors, false otherwise
	 */
	public void setShowInEditor(boolean truefalse);

	/**
	 * Returns whether this factions value is shown in player Factions command
	 * @return true if displayed in factions command, false otherwise
	 */
	public boolean showInFactionsCommand();

	/**
	 * Sets whether this factions value is shown in player Factions command
	 * @param truefalse true if displayed in factions command, false otherwise
	 */
	public void setShowInFactionsCommand(boolean truefalse);

	/**
	 * Returns the default faction mask/value list, which is applied whenever
	 * a Faction Change Event applies a Faction Add command.
	 * A default faction mask/value is defined as a number, along with an
	 * optional Zapper mask describing to whom the value is applied.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setDefaults(List)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return the default faction mask/value list
	 */
	public Enumeration<String> defaults();

	/**
	 * Returns the default faction value that applies to the given mob.
	 * This method is called when a Faction Change event applies a
	 * Faction Add command. Returns Integer.MAX_VALUE if no default
	 * value applies to this mob.
	 * Each list item is a string.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#defaults()
	 * @param mob the mob to find a default faction value for
	 * @return the faction value that applies, or Integer.MAX_VALUE
	 */
	public int findDefault(MOB mob);

	/**
	 * Sets the default faction mask/value list, which is applied whenever
	 * a Faction Change Event applies a Faction Add command.
	 * A default faction mask/value is defined as a number, along with an
	 * optional Zapper mask describing to whom the value is applied.
	 * Each list item is a string.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#defaults()
	 * @param v the new default faction mask/value list
	 */
	public void setDefaults(List<String> v);

	/**
	 * Returns the automatic default faction mask/value list, which is
	 * possibly applied whenever a mob or player is brought to life for
	 * the first time. An automatic default faction mask/value is defined
	 * as a number, along with an optional Zapper mask describing to whom
	 * the value is applied. Each list item is a string.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setAutoDefaults(List)
	 * @return the automatic default faction mask/value list
	 */
	public Enumeration<String> autoDefaults();

	/**
	 * Returns the automatic default faction value that applies to the
	 * given mob.  This method is called when a mob is brought into the
	 * world.  Returns Integer.MAX_VALUE if no default value applies
	 * to this mob.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#defaults()
	 * @param mob the mob to find a default value of this faction for.
	 * @return the value to give to the given mob, or Integer.MAX_VALUE
	 */
	public int findAutoDefault(MOB mob);

	/**
	 * Sets the automatic default faction mask/value list, which is
	 * possibly applied whenever a mob or player is brought to life for
	 * the first time. An automatic default faction mask/value is defined
	 * as a number, along with an optional Zapper mask describing to whom
	 * the value is applied. Each list item is a string.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#defaults()
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param v the new automatic default faction mask/value list
	 */
	public void setAutoDefaults(List<String> v);

	/**
	 * A modifier of the base amount of faction value change, namely 100.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setRateModifier(double)
	 * @return a modifier of the base amount of faction change
	 */
	public double rateModifier();

	/**
	 * Sets the modifier of the base amount of faction value change, namely 100.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#rateModifier()
	 * @param d the new modifier of the base amount of faction value change
	 */
	public void setRateModifier(double d);

	/**
	 * Returns the player choosable faction mask/value list, which is
	 * possibly presented whenever a player creates a new character.
	 * An faction mask/value is defined as a number, along with an
	 * optional Zapper mask describing to whom the value is applied.
	 * Each list item is a string.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setChoices(List)
	 * @return the choosable faction mask/value list
	 */
	public Enumeration<String> choices();

	/**
	 * Returns a vector of Integer objects representing the choosable
	 * faction values available to the given mob when they create
	 * a new character.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#choices()
	 * @param mob the player mob to evaluate
	 * @return a vector of integer faction values that applies
	 */
	public List<Integer> findChoices(MOB mob);

	/**
	 * Sets the player choosable faction mask/value list, which is
	 * possibly presented whenever a player creates a new character.
	 * An faction mask/value is defined as a number, along with an
	 * optional Zapper mask describing to whom the value is applied.
	 * Each list item is a string.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#choices()
	 * @param v the list of choosable faction mask/values
	 */
	public void setChoices(List<String> v);

	/**
	 * Returns an enumeration of all available Faction.FactionRange objects,
	 * representing the entire score of available values valid for this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addRange(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delRange(com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange
	 * @return an enumeration of all available ranges
	 */
	public Enumeration<Faction.FRange> ranges();

	/**
	 * Returns the Faction.FactionRange object that applies to the given faction
	 * value.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ranges()
	 * @param faction the value to find a matching range object for
	 * @return the range object that matches the given faction value
	 */
	public FRange fetchRange(int faction);

	/**
	 * Returns the name of the Faction.FactionRange object that applies to
	 * the given faction value.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ranges()
	 * @param faction the value to find a matching range object for
	 * @return the name of the given faction object
	 */
	public String fetchRangeName(int faction);

	/**
	 * Adds a new Faction.FactionRange object to this faction using an encoded key.
	 * The key is encoded as semicolon separated values of low, high, name, code name,
	 * and alignment flag.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ranges()
	 * @param key the encoded values for the new faction range
	 * @return the faction range object created and added.
	 */
	public FRange addRange(String key);

	/**
	 * Removes the given FactionRange object from the faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ranges()
	 * @param FR the faction range object to remove
	 * @return whether a removal was necessary
	 */
	public boolean delRange(FRange FR);

	/**
	 * Returns the Faction.FactionRange object that applies to
	 * the given faction range code name.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ranges()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange#codeName()
	 * @param codeName the code name to find a matching range object for
	 * @return the correct faction range object, or null
	 */
	public Faction.FRange fetchRange(String codeName);

	/**
	 * Returns an enumeration of change event keys, which are the code names of
	 * the triggers that cause faction values to change automatically.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#createChangeEvent(String eventID)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delChangeEvent(Faction.FactionChangeEvent event)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#executeChange(MOB, MOB, com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent)
	 * @return an enumeration of the event keys (triggers)
	 */
	public Enumeration<String> changeEventKeys();

	/**
	 * Returns a FactionChangeEvent that applies when the given Ability is used
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#executeChange(MOB, MOB, com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 * @param key the Ability to find a change event for.
	 * @return the FactionChangeEvent that applies, or null.
	 */
	public FactionChangeEvent[] findAbilityChangeEvents(Ability key);

	/**
	 * Returns a FactionChangeEvent that applies when the given event name (a trigger
	 * code) occurs in the game.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#MISC_TRIGGERS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#executeChange(MOB, MOB, com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 * @param key the code name of the event that occurred
	 * @return the FactionChangeEvent triggered by that event
	 */
	public FactionChangeEvent[] getChangeEvents(String key);

	/**
	 * Adds a new FactionChangeEvent object to this faction using the given event code
	 * name, or fully encoded event string.  The key must be either a single event
	 * trigger code (an ability name, event code name), or a fully encoded string
	 * which is a semicolon delimited field consisting of event (trigger) id, direction
	 * code, and amount
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#MISC_TRIGGERS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 * @param key the field used to create the new FactionChangeEvent
	 * @return the FactionChangeEvent object created and added to this faction, or null
	 */
	public FactionChangeEvent createChangeEvent(String key);

	/**
	 * Removes a FactionChangeEvent of the given event (trigger) id.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#MISC_TRIGGERS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 * @param event the event object to remove from the list of change events
	 * @return whether the event id was found to remove
	 */
	public boolean delChangeEvent(Faction.FactionChangeEvent event);

	/**
	 * Removes all FactionChangeEvents
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#MISC_TRIGGERS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 */
	public void clearChangeEvents();

	/**
	 * Executes a Faction change event for the given event source and target, and the
	 * applicable FactionChangeEvent event object for this faction
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 * @param source the source of the event
	 * @param target the target of the event
	 * @param event the applicable event object for this faction
	 */
	public void executeChange(MOB source, MOB target, FactionChangeEvent event);

	/**
	 * Computed completed at runtime, this method returns all possible valid FactionChangeEvent
	 * event ids that can be used to define triggers.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changeEventKeys()
	 * @return a list of all valid event trigger ids.
	 */
	public String ALL_CHANGE_EVENT_TYPES();

	/**
	 * Returns an enumeration of Object arrays referring to the a factor to multiply
	 * times the base amount (100) of faction change (up or down) for particular
	 * mobs who match a given Zapper mask.  Each Object array consists of a factor
	 * to apply on faction gains, a factor to apply on factor drops, and the zapper
	 * mask to decide which mobs it applies to (or mob states).
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addFactor(double, double, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delFactor(Faction.FZapFactor)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#findFactor(MOB, boolean)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return the enumeration of change factor object arrays
	 */
	public Enumeration<Faction.FZapFactor> factors();

	/**
	 * Removes the given change factor from this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factors()
	 * @param f the factor to remove
	 * @return whether the given factor was found to remove
	 */
	public boolean delFactor(Faction.FZapFactor f);

	/**
	 * Returns the given enumerated change factor
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factors()
	 * @param x which factor (0-number) to return
	 * @return the given factor, or null.
	 */
	public Faction.FZapFactor getFactor(int x);

	/**
	 * Adds a new change factor to this Faction.  A change factor is a state
	 * dependent multiplier by a change in faction.  It consists of a Zapper
	 * mask to determine whether the factor applies to the given mob/player
	 * state, and a factor to apply on gains in faction or losses in faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factors()
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param gain the factor to apply on gains in faction
	 * @param loss the factor to apply on losses of faction
	 * @param mask the zapper mask to use to determine if this factor applies to a mob
	 * @return the newly created factor Object[] array
	 */
	public Faction.FZapFactor addFactor(double gain, double loss, String mask);

	/**
	 * Returns the applicable change factor for the given mob, and the
	 * whether the faction change was a gain or loss (not a gain).
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factors()
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param mob the mob to compare against the zapper masks of the various factors
	 * @param gain return the gain factor if true, or the loss factor if false
	 * @return the factor value that applies, or 1.0 (meaning no change).
	 */
	public double findFactor(MOB mob, boolean gain);

	/**
	 * Returns an enumeration of faction ids (of other factions) that are
	 * automatically changed, up or down, when this faction changes. A relation
	 * factor is a number multiplied by the change in this faction to determine
	 * the amount that another faction on the same mob is changed by.  The factor
	 * can be positive or negative to cause the other faction to rise or fall.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addRelation(String, double)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delRelation(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#getRelation(String)
	 * @return an enumeration of faction ids
	 */
	public Enumeration<String> relationFactions();

	/**
	 * Removes the give faction relation from this faction.  Requires a faction id
	 * of another faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#relationFactions()
	 * @param factionID the faction id to remove
	 * @return whether the faction id was found and removed
	 */
	public boolean delRelation(String factionID);

	/**
	 * Adds a new faction relation factor to this faction.  The faction id is the id
	 * of another complementary or rival faction, and the relation is a number multiplied
	 * by thge change in this faction to determine the amount the given faction id
	 * faction is changed by. The relation factor can be positive or negative to cause
	 * the faction id faction to rise or fall.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#relationFactions()
	 * @param factionID the faction id of the other faction
	 * @param relation the relation factor to use as a multiplier
	 * @return whether the new faction id was successfully added
	 */
	public boolean addRelation(String factionID, double relation);

	/**
	 * Returns the relation factor of the given faction id.  See addRelation for
	 * more information.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#relationFactions()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addRelation(String, double)
	 * @param factionID the other factions faction id
	 * @return the factor to multiply a change in the other faction by
	 */
	public double getRelation(String factionID);

	/**
	 * Returns an enumeration of Abilities or Behavior IDs that are
	 * automatically but conditionally added to mobs (not players) with this faction.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addAffectBehav(String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delAffectBehav(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#getAffectBehav(String)
	 * @return an enumeration of Abilities or Behavior ID
	 */
	public Enumeration<String> affectsBehavs();

	/**
	 * Removes the given ability or behavior from this Faction.  It will require the
	 * mob be reset or rejuved in order for this to take affect.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#affectsBehavs()
	 * @param ID the Abilities or Behavior ID to remove
	 * @return whether the Abilities or Behavior ID was found and removed
	 */
	public boolean delAffectBehav(String ID);

	/**
	 * Adds a new Ability or Behavior to this Faction.  The ID must match a
	 * Behavior or, if one is not found, an Ability.  The parms are any
	 * parameters required by the Behavior or Ability.  The gainMask is
	 * a simple mask to further narrow what kind of mobs receive the
	 * given Ability or Behavior when first receiving this Faction.  It
	 * will require the mob be reset or rejuved in order for this to take affect.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#affectsBehavs()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delAffectBehav(String)
	 * @param ID the Abilities or Behavior ID to add
	 * @param parms the parameters for the new affect or behavior
	 * @param gainMask the zapper mask to check to see who qualifies
	 * @return whether the new Abilities or Behavior ID was successfully added
	 */
	public boolean addAffectBehav(String ID, String parms, String gainMask);

	/**
	 * Returns a string array containing the parms at index 0, and the gainMask at 1.
	 * See addAffectBehav for more information.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#affectsBehavs()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addAffectBehav(String, String, String)
	 * @param ID the Abilities or Behavior ID
	 * @return a string array containing the parms at index 0, and the gainMask at 1
	 */
	public String[] getAffectBehav(String ID);

	/**
	 * Returns an enumeration of Faction.FactionReaction items associated
	 * with this faction.  These are automatically added to mobs in the presence
	 * of one with this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addReaction(String, String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delReaction(Faction.FReactionItem)
	 * @return an enumeration of Faction.FactionReaction items
	 */
	public Enumeration<Faction.FReactionItem> reactions();

	/**
	 * Returns an enumeration of Faction.FactionReaction items associated
	 * with this faction and the given range code.  These are automatically
	 * added to mobs in the presence of one with this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addReaction(String, String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delReaction(Faction.FReactionItem)
	 * @param rangeCode the range code to filter by
	 * @return an enumeration of Faction.FactionReaction items
	 */
	public Enumeration<Faction.FReactionItem> reactions(String rangeCode);

	/**
	 * Removes the given reaction from this Faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addReaction(String, String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#reactions()
	 * @param item the faction reaction item to remove
	 * @return whether the reaction was found and removed
	 */
	public boolean delReaction(Faction.FReactionItem item);

	/**
	 * Adds a new reaction to this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delReaction(Faction.FReactionItem)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#reactions()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.Commands.interfaces.Command
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @param range the faction range to use as a determinate
	 * @param abilityID the ability/Behavior/or command ID
	 * @param parms the parameters for the new affect or behavior or command
	 * @param mask the zapper mask to check to see which mob qualifies
	 * @return whether the new reaction was successfully added
	 */
	public boolean addReaction(String range, String mask, String abilityID, String parms);

	/**
	 * Set this faction to use the light-reaction system, which is easier on resources, but
	 * not as powerful.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#useLightReactions()
	 * @param truefalse true to use the light reaction system, false otherwise
	 */
	public void setLightReactions(boolean truefalse);

	/**
	 * Return whether this faction uses the light-reaction system, which is easier on resources, but
	 * not as powerful.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setLightReactions(boolean)
	 * @return true if this faction to use the light-reaction system
	 */
	public boolean useLightReactions();

	/**
	 * Returns an enumeration of Faction.FactionAbilityUsage objects for this Faction.
	 * A FactionAbilityUsage object defines restrictions on the use of a mob or players
	 * abilities based on values in this faction and other variables.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addAbilityUsage(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delAbilityUsage(com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#usageFactorRangeDescription(Ability)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#hasUsage(Ability)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#canUse(MOB, Ability)
	 * @return an enumeration of Faction.FactionAbilityUsage objects for this Faction
	 */
	public Enumeration<Faction.FAbilityUsage> abilityUsages();

	/**
	 * Returns the list of faction ranges that apply based on Faction.FactionAbilityUsage
	 * usage factor that apply to the given ability.  An empty string means it does not
	 * apply.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#abilityUsages()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange
	 * @param A the ability to find a usage factor for, and then use to find applicable ranges
	 * @return the list of faction range names that apply to this ability from usage factors
	 */
	public String usageFactorRangeDescription(Ability A);

	/**
	 * Returns whether any of the Faction.FactionAbilityUsage objects for this Faction
	 * apply to the given ability.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#abilityUsages()
	 * @param A the ability to find a usage criterium for
	 * @return true if a criterium exists, false otherwise.
	 */
	public boolean hasUsage(Ability A);

	/**
	 * Returns whether the given player/mob is prevented from using the given Ability
	 * based on any of the Faction.FactionAbilityUsage (faction ability usage)
	 * criterium defined for this Faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#abilityUsages()
	 * @param mob the mob/player to evaluate
	 * @param A the ability to evaluate
	 * @return true if the player can use the ability, false otherwise
	 */
	public boolean canUse(MOB mob, Ability A);

	/**
	 * Adds a new Faction.FactionAbilityUsage object to this Faction based on the
	 * given definitional key.  The key is NULL to create an empty usage, or
	 * a definitional string that consists of one or more ability names, domains,
	 * flags, etc followed by a semicolon and a minimum faction value, and another
	 * semicolon and a maximum faction value.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#abilityUsages()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#delAbilityUsage(com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage)
	 * @param key the definitional key, or null
	 * @return the new Faction.FactionAbilityUsage added
	 */
	public FAbilityUsage addAbilityUsage(String key);

	/**
	 * Returns the enumerated Faction.FactionAbilityUsage object at the given index.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#abilityUsages()
	 * @param x the index of the Faction.FactionAbilityUsage object to return
	 * @return the Faction.FactionAbilityUsage object at that index
	 */
	public FAbilityUsage getAbilityUsage(int x);

	/**
	 * Removes the given Faction.FactionAbilityUsage object from this faction
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#abilityUsages()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addAbilityUsage(String)
	 * @param usage the Faction.FactionAbilityUsage object to remove
	 * @return true if the object was found and removed
	 */
	public boolean delAbilityUsage(FAbilityUsage usage);

	/**
	 * Return the bitmap of internal-use flags for this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setInternalFlags(long)
	 * @return the bitmap of internal-use flags for this faction.
	 */
	public long getInternalFlags();

	/**
	 * Set the bitmap of internal-use flags for this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#getInternalFlags()
	 * @param bitmap the bitmap of internal-use flags for this faction.
	 */
	public void setInternalFlags(long bitmap);

	/**
	 * A Faction Change Event is an event that triggers an automatic change in
	 * a mob or players faction value.  Triggers can be the use of abilities,
	 * or certain specific coded events (such as killing another mob).
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#executeChange(MOB, MOB, com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent)
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface FactionChangeEvent
	{
		/**
		 * Returns the event trigger id
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setEventID(String)
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDclassFilter()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDdomainFilter()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDflagFilter()
		 * @return the event trigger id
		 */
		public String eventID();

		/**
		 * Sets the event trigger id
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ALL_CHANGE_EVENT_TYPES()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#eventID()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDclassFilter()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDdomainFilter()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDflagFilter()
		 * @param newID the new event trigger id
		 * @return true if the event id is valid
		 */
		public boolean setEventID(String newID);

		/**
		 * A derivative of the event id, this will return a value of 0 or above
		 * if the event id was of a particular Ability ACODE_.  Returns -1 if
		 * this value does not apply, or an index into ACODE_DESCS.
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_DESCS
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#eventID()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDdomainFilter()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDflagFilter()
		 * @return -1, or an index into an Ability ACODE
		 */
		public int IDclassFilter();

		/**
		 * A derivative of the event id, this will return a value of 0 or above
		 * if the event id was of a particular Ability FLAG_.  Returns -1 if
		 * this value does not apply, or an index into FLAG_DESCS.
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#FLAG_DESCS
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#eventID()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDclassFilter()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDdomainFilter()
		 * @return -1, or an index into an Ability FLAG
		 */
		public int IDflagFilter();

		/**
		 * A derivative of the event id, this will return a value of 0 or above
		 * if the event id was of a particular Ability DOMAIN_.  Returns -1 if
		 * this value does not apply, or an index into DOMAIN_DESCS.
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#DOMAIN_DESCS
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#eventID()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDclassFilter()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#IDflagFilter()
		 * @return -1, or an index into an Ability ACODE
		 */
		public int IDdomainFilter();

		/**
		 * Returns the list of flags that apply to this event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#FLAG_DESCS
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setFlags(String)
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#outsiderTargetOK()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#selfTargetOK()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#just100()
		 * @return the list of applicable flags
		 */
		public String flagCache();

		/**
		 * Sets the list of flags that apply to this event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#FLAG_DESCS
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#flagCache()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#outsiderTargetOK()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#selfTargetOK()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#just100()
		 * @param newFlagCache the new list of applicable flags
		 */
		public void setFlags(String newFlagCache);

		/**
		 * A derivative of the flag cache, this method returns whether the flag was set that
		 * allows this event to trigger when the target of the event does not have any value
		 * with this faction
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#flagCache()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#selfTargetOK()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#just100()
		 * @return true if the target does not have to have this faction, false otherwise
		 */
		public boolean outsiderTargetOK();

		/**
		 * A derivative of the flag cache, this method returns whether the flag was set that
		 * allows this event to trigger when the target and source of the event are the same.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#flagCache()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#outsiderTargetOK()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#just100()
		 * @return true if src and target are the same, false otherwise
		 */
		public boolean selfTargetOK();

		/**
		 * A derivative of the flag cache, this method returns whether the flag was set that
		 * causes the determination of the amount of faction move to apply to NOT take the
		 * difference between the source and targets levels into account.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#flagCache()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#outsiderTargetOK()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#selfTargetOK()
		 * @return true to NOT take level into account when determining amount of faction change
		 */
		public boolean just100();

		/**
		 * Returns a code for a description of how an event, if applicable, will affect this
		 * factions value.  The direction is an index into CHANGE_DIRECTION_DESCS, or one of the
		 * CHANGE_DIRECTION_ constants.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#CHANGE_DIRECTION_DESCS
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setDirection(int)
		 * @return a FactionChangeEvent#CHANGE_DIRECTION_ constant
		 */
		public int direction();

		/**
		 * Sets a code for a description of how an event, if applicable, will affect this
		 * factions value.  The direction is an index into CHANGE_DIRECTION_DESCS, or one of the
		 * CHANGE_DIRECTION_ constants.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#CHANGE_DIRECTION_DESCS
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#direction()
		 * @param newVal a new FactionChangeEvent#CHANGE_DIRECTION_ constant
		 */
		public void setDirection(int newVal);

		/**
		 * Returns the factor to multiply the base faction change amount (100) by, to determine
		 * the amount of this faction changed by this event, in accordance with the given direction.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#direction()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setFactor(double)
		 * @return the factor to multiply the base amount of the faction by
		 */
		public double factor();

		/**
		 * Sets the factor to multiply the base faction change amount (100) by, to determine
		 * the amount of this faction changed by this event, in accordance with the given direction.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#direction()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#factor()
		 * @param newVal the new faction change factor amount
		 */
		public void setFactor(double newVal);

		/**
		 * Returns the zapper mask that is used to see if the target of the event qualifies in
		 * order to trigger a faction change by this defined event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setTargetZapper(String)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return the zapper mask string
		 */
		public String targetZapper();

		/**
		 * Returns the zapper mask that is used to see if the target of the event qualifies in
		 * order to trigger a faction change by this defined event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setTargetZapper(String)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return the zapper mask compiled
		 */
		public MaskingLibrary.CompiledZMask compiledTargetZapper();

		/**
		 * Returns the zapper mask that is used to see if the source of the event qualifies in
		 * order to trigger a faction change by this defined event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setTriggerParameters(String)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return the zapper mask compiled
		 */
		public MaskingLibrary.CompiledZMask compiledSourceZapper();

		/**
		 * Sets the zapper mask that is used to see if the target of the event qualifies in
		 * order to trigger a faction change by this defined event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#targetZapper()
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @param newVal the new zapper mask string
		 */
		public void setTargetZapper(String newVal);

		/**
		 * Returns any trigger parameters defined that modify the way the trigger behaves.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setTriggerParameters(String)
		 * @return the trigger parameters
		 */
		public String triggerParameters();

		/**
		 * Returns the named trigger parameters defined
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setTriggerParameters(String)
		 * @param parmName the name of the trigger parm to look for
		 * @return the specific named trigger parameter
		 */
		public String getTriggerParm(String parmName);

		/**
		 * Sets any trigger parameters defined that modify the way the trigger behaves.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#triggerParameters()
		 * @param newVal the trigger parameters
		 */
		public void setTriggerParameters(String newVal);

		/**
		 * Returns the internal state variable stored for this change event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#setStateVariable(int,Object)
		 * @param x which internal state variable to get
		 * @return the state variable
		 */
		public Object stateVariable(int x);

		/**
		 * Sets an internal state variable stored for this change event.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent#stateVariable(int)
		 * @param x which internal state variable to set
		 * @param newVal the state variable
		 */
		public void setStateVariable(int x, Object newVal);

		/**
		 * Returns a semicolon delimited list of all the settings in this change event
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#createChangeEvent(String)
		 * @return a semicolon delimited list of all the settings in this change event
		 */
		@Override
		public String toString();

		/**
		 * Returns whether the given mob is a valid source and target of this event.
		 * @param source the source to evaluate
		 * @param target the target to evaluate
		 * @return true if this event applies to the mobs, false otherwise
		 */
		public boolean applies(MOB source, MOB target);

		/**
		 * Return the parent faction for which this data stands.
		 * @return this data objects parent faction.
		 */
		public Faction getFaction();

		/** a direction constant meaning this event changes the factions value upward */
		public static final int CHANGE_DIRECTION_UP=0;
		/** a direction constant meaning this event changes the factions value downward */
		public static final int CHANGE_DIRECTION_DOWN=1;
		/** a direction constant meaning this event changes the factions value opposite of targets faction leanings */
		public static final int CHANGE_DIRECTION_OPPOSITE=2;
		/** a direction constant meaning this event changes the factions value directly to lowest value */
		public static final int CHANGE_DIRECTION_MINIMUM=3;
		/** a direction constant meaning this event changes the factions value directly to highest value */
		public static final int CHANGE_DIRECTION_MAXIMUM=4;
		/** a direction constant meaning this event removes the faction altogether */
		public static final int CHANGE_DIRECTION_REMOVE=5;
		/** a direction constant meaning this event adds the faction with a default value */
		public static final int CHANGE_DIRECTION_ADD=6;
		/** a direction constant meaning this event changes the factions value away from targets value */
		public static final int CHANGE_DIRECTION_AWAY=7;
		/** a direction constant meaning this event changes the factions value towards the targets value */
		public static final int CHANGE_DIRECTION_TOWARD=8;
		/** the code words for the various direction flags that describe the direction and amount of faction change */
		public static final String[] CHANGE_DIRECTION_DESCS={"UP","DOWN","OPPOSITE","MINIMUM","MAXIMUM","REMOVE","ADD","AWAY","TOWARD"};
		/** the code words for the various evaluation flags to decide if this event applies and other things */
		public static final String[] FLAG_DESCS={"OUTSIDER","SELFOK","JUST100"};
		/** some non-ability-related event trigger ids */
		public static final String[] MISC_TRIGGERS={"MURDER","TIME","ADDOUTSIDER","KILL","BRIBE","TALK","MUDCHAT","ARRESTED"};
	}

	/**
	 * The foundation of any Faction, the Faction Range represents a range of values that constitutes
	 * a single named group of numeric values for the faction.  A factions total range is determined
	 * by the high value of the highest range and the low value of the lowest range.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addRange(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#ranges()
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface FRange
	{
		/**
		 * Returns the unique code name that describes this range of faction values
		 * @return the unique code name that describes this range of faction values
		 */
		public String codeName();

		/**
		 * Returns the numerically low value of this faction range
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange#setLow(int)
		 * @return the numerically low value of this faction range
		 */
		public int low();

		/**
		 * Sets the numerically low value of this faction range
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange#low()
		 * @param newVal the numerically low value of this faction range
		 */
		public void setLow(int newVal);

		/**
		 * Returns the numerically high value of this faction range
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange#setHigh(int)
		 * @return the numerically high value of this faction range
		 */
		public int high();

		/**
		 * Sets the numerically high value of this faction range
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange#high()
		 * @param newVal the numerically high value of this faction range
		 */
		public void setHigh(int newVal);

		/**
		 * Returns the nice friendly displayable name of this faction range,
		 * which need not be unique.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange#setName(String)
		 * @return the name of this range of values
		 */
		public String name();

		/**
		 * Sets the nice friendly displayable name of this faction range,
		 * which need not be unique.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange#name()
		 * @param newVal the name of this range of values
		 */
		public void setName(String newVal);

		/**
		 * Returns a constant reflecting whether this range of faction value is
		 * equivalent to one of the legacy alignment constant values.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
		 * @return an alignment constant
		 */
		public Align alignEquiv();

		/**
		 * Sets a constant reflecting whether this range of faction value is
		 * equivalent to one of the legacy alignment constant values.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.Align
		 * @param newVal a new alignment constant
		 */
		public void setAlignEquiv(Align newVal);

		/**
		 * Returns a semicolon-delimited representation of this faction range, which
		 * can be used to create a new one later.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addRange(String)
		 * @return a semicolon-delimited range
		 */
		@Override
		public String toString();

		/**
		 * Returns a random numeric value within this faction range
		 * @return a random numeric value within this faction range
		 */
		public int random();

		/**
		 * Returns the faction of which this is a range
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addRange(String)
		 * @return the faction of which this is a range
		 */
		public Faction getFaction();
	}

	/**
	 * A FactionData object is stored inside other objects that keep track
	 * of their own faction.  The object stores the faction value, any
	 * event listeners or tickers, and a method to determine when it is
	 * time to refresh the object.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#makeFactionData(MOB)
	 * @author Bo Zimmermanimmerman
	 */
	public static interface FData extends MsgListener, StatsAffecting
	{
		/**
		 * Cleans out the internal data structures of this faction
		 * to denote that a new context is entered.  Is called by the constructor.
		 * @param F the faction to which this data belongs.
		 */
		public void resetFactionData(Faction F);
		/**
		 * Returns true if this object requires updating by the parent
		 * faction for some reason.
		 * @return true if an update is necessary, false otherwise.
		 */
		public boolean requiresUpdating();

		/**
		 * A mirror implementation of Tickable
		 * @see com.planet_ink.coffee_mud.core.interfaces.Tickable
		 * @param ticking the ticking object
		 * @param tickID the id code of the tick being done
		 * @return true to keep ticking, false to stop ticking
		 */
		public boolean tick(Tickable ticking, int tickID);

		/**
		 * Return the parent faction for which this data stands.
		 * @return this data objects parent faction.
		 */
		public Faction getFaction();

		/**
		 * Returns the actual value that the holding object has in this faction.
		 * @return the faction value
		 */
		public int value();

		/**
		 * Sets the actual value that the holding object has in this faction.
		 * @param newValue the faction value
		 */
		public void setValue(int newValue);

		/**
		 * Clears and re-adds all the necessary message listeners and tickers
		 * and stat affecting objects for this faction data reference.
		 * @param abilities a vector of abilities
		 * @param behaviors a vector of behaviors
		 */
		public void addHandlers(List<Ability> abilities, List<Behavior> behaviors);
	}

	/**
	 * A Faction Ability Usage object represents a set of criterium that can be used
	 * to determine whether this faction allows a mob or player to use a particular
	 * ability, or class of abilities.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addAbilityUsage(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#abilityUsages()
	 * @author Bo Zimmerman
	 *
	 */
	public static interface FAbilityUsage
	{
		/**
		 * The unconverted ability mask, denoting ability ids, domains, flags, etc.
		 * Is parsed for benefit of other methods below
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#setAbilityFlag(String)
		 * @return the unconverted ability mask
		 */
		public String abilityFlags();

		/**
		 * Sets the ability usage masks and methods from an ability id, domain, flags, etc.
		 * Parses the string sent to set many of the methods below.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#notflag()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#possibleAbilityID()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#type()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#domain()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#flag()
		 * @param str the ability usage mask
		 * @return A vector of words inside the given string that are not valid or were not understood.
		 */
		public List<String> setAbilityFlag(String str);

		/**
		 * A bitmask of ability flags that must NOT be set for this usage to apply to an ability
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#flag()
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#FLAG_DESCS
		 * @return a bitmask of Ability flags that must not be set by the ability
		 */
		public int notflag();

		/**
		 * A bitmask of ability flags that MUST be set for this usage to apply to an ability
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#notflag()
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#FLAG_DESCS
		 * @return a bitmask of Ability flags that must be set by the ability
		 */
		public int flag();

		/**
		 * Whether the abilityFlags() method is possibly a specific Ability ID
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ID()
		 * @return true if the abilityFlags() string is an Ability ID()
		 */
		public boolean possibleAbilityID();

		/**
		 * An ability code that an ability must be in order for this usage to apply, or -1
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_DESCS
		 * @return an ability code that an ability must be in order for this usage to apply, or -1
		 */
		public int type();

		/**
		 * An ability domain that an ability must be in order for this usage to apply, or -1
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#DOMAIN_DESCS
		 * @return an ability domain that an ability must be in order for this usage to apply, or -1
		 */
		public int domain();

		/**
		 * The minimum value that a player must have in the faction to be able to use the selected
		 * ability referred to by the ability flags of this usage criterium.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#setLow(int)
		 * @return a minimum faction value
		 */
		public int low();

		/**
		 * Sets the minimum value that a player must have in the faction to be able to use the selected
		 * ability referred to by the ability flags of this usage criterium.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#low()
		 * @param newVal a new minimum faction value
		 */
		public void setLow(int newVal);

		/**
		 * Returns the maximum value that a player must have in the faction to be able to use the selected
		 * ability referred to by the ability flags of this usage criterium.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#setHigh(int)
		 * @return a maximum faction value
		 */
		public int high();

		/**
		 * Sets the maximum value that a player must have in the faction to be able to use the selected
		 * ability referred to by the ability flags of this usage criterium.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#abilityFlags()
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FAbilityUsage#high()
		 * @param newVal a new maximum faction value
		 */
		public void setHigh(int newVal);

		/**
		 * Returns a semicolon-delimited string of the values of this ability usage, suitable for
		 * using to create a new one later.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addAbilityUsage(String)
		 * @return a semicolon-delimited string of the values of this ability usage
		 */
		@Override
		public String toString();
	}

	/**
	 * A factor defines how modifications of faction value, up or down, are modified on a
	 * mob by mob basis.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addFactor(double, double, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factors()
	 * @author Bo Zimmerman
	 *
	 */
	public static interface FZapFactor
	{
		/**
		 * Get the gain factor (0-1)
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FZapFactor#setGainFactor(double)
		 * @return the gain factor (0-1)
		 */
		public double gainFactor();

		/**
		 * Set the gain factor (0-1)
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FZapFactor#gainFactor()
		 * @param val the gain factor (0-1)
		 */
		public void setGainFactor(double val);

		/**
		 * Set the loss factor (0-1)
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FZapFactor#setGainFactor(double)
		 * @return the loss factor (0-1)
		 */
		public double lossFactor();

		/**
		 * Set the loss factor (0-1)
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FZapFactor#gainFactor()
		 * @param val the loss factor (0-1)
		 */
		public void setLossFactor(double val);

		/**
		 * The mask to tell which mobs to apply this factor to
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FZapFactor#setMOBMask(String)
		 * @return mask to tell which mobs to apply this reaction to
		 */
		public String MOBMask();

		/**
		 * The compiled mask to tell which mobs to apply this factor to
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FZapFactor#setMOBMask(String)
		 * @return the compiled mask to tell which mobs to apply this reaction to
		 */
		public MaskingLibrary.CompiledZMask compiledMOBMask();

		/**
		 * Set the mask to determine which mobs in the players presence will be affected.  This is a zappermask.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FZapFactor#MOBMask()
		 * @param str the mask to determine which mobs in the players presence will be affected
		 */
		public void setMOBMask(String str);

		/**
		 * Returns a semicolon-delimited string of the values of this factpr, suitable for
		 * using to create a new one later.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addFactor(double,double,String)
		 * @return a string of the values of this factor
		 */
		@Override
		public String toString();
	}

	/**
	 * Adds very temporary affects and behaviors to mobs who match the reaction zapper
	 * mask, and who are in the same room as someone with standing in this faction.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addReaction(String, String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#reactions()
	 * @author Bo Zimmerman
	 *
	 */
	public static interface FReactionItem
	{
		/**
		 * The ability/behavior/command id.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#setReactionObjectID(String)
		 * @return the ability/behavior/command id
		 */
		public String reactionObjectID();

		/**
		 * Set the ability/behavior/command id.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#reactionObjectID()
		 * @param str the ability/behavior/command id
		 */
		public void setReactionObjectID(String str);

		/**
		 * The mask to tell which mobs to apply this reaction to
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#setPresentMOBMask(String)
		 * @return mask to tell which mobs to apply this reaction to
		 */
		public String presentMOBMask();

		/**
		 * The compiled mask to tell which mobs to apply this reaction to
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#setPresentMOBMask(String)
		 * @return the compiled mask to tell which mobs to apply this reaction to
		 */
		public MaskingLibrary.CompiledZMask compiledPresentMOBMask();

		/**
		 * Set the mask to determine which mobs in the players presence will be affected.  This is a zappermask.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#presentMOBMask()
		 * @param str the mask to determine which mobs in the players presence will be affected
		 */
		public void setPresentMOBMask(String str);

		/**
		 * The code name of the range which determines which folks with this faction get a reaction
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#setRangeName(String)
		 * @return the range which determines which folks with this faction get a reaction
		 */
		public String rangeCodeName();

		/**
		 * Set the code name of the range which determines which folks with this faction get a reaction
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#rangeCodeName()
		 * @param str the range which determines which folks with this faction get a reaction
		 */
		public void setRangeName(String str);

		/**
		 * The parameters for the ability/behavior/command above.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#setRangeName(String)
		 * @return the parameters for the ability/behavior/command above
		 */
		public String parameters();
		
		/**
		 * The parameters for the ability/behavior/command above returned in
		 * runtime form so that parameters are adjusted with the given name
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#setRangeName(String)
		 * @param Name the name to replace the &lt;TARGET&gt; moniker with
		 * @return the parameters for the ability/behavior/command above
		 */
		public String parameters(String Name);
		
		/**
		 * Set the parameters for the ability/behavior/command above.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction.FReactionItem#rangeCodeName()
		 * @param str the parameters for the ability/behavior/command above
		 */
		public void setParameters(String str);

		/**
		 * Returns a semicolon-delimited string of the values of this reaction, suitable for
		 * using to create a new one later.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#addReaction(String, String, String, String)
		 * @return a semicolon-delimited string of the values of this reaction
		 */
		@Override
		public String toString();
	}

	/** internal flag masks meaning to skip standard auto system, retrieved by {@link Faction#getInternalFlags()} */
	public static final long IFLAG_IGNOREAUTO=1;
	/** internal flag masks meaning to never save to a file, retrieved by {@link Faction#getInternalFlags()} */
	public static final long IFLAG_NEVERSAVE=2;
	/** internal flag masks meaning to skip normal ticking, retrieved by {@link Faction#getInternalFlags()} */
	public static final long IFLAG_CUSTOMTICK=4;

	/** legacy enumerator constant for {@link FRange#alignEquiv()} denoting that the range does not reflect alignment */
	public enum Align {
		INDIFF, EVIL, NEUTRAL, GOOD
	}

	/** String list for the valid {@link Faction#experienceFlag()} constants */
	public final static String[] EXPAFFECT_NAMES={"NONE","EXTREME","HIGHER","LOWER","FOLLOWHIGHER","FOLLOWLOWER"};
	/** String descriptions for the valid {@link Faction#experienceFlag()} constants */
	public final static String[] EXPAFFECT_DESCS={"None","Proportional (Extreme)","Higher (mine)","Lower (mine)","Higher (other)","Lower (other)"};

	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the NAME tag */
	public final static int TAG_NAME=0;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the MINIMUM tag */
	public final static int TAG_MINIMUM=1;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the MAXIMUM tag */
	public final static int TAG_MAXIMUM=2;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the SCOREDISPLAY tag */
	public final static int TAG_SCOREDISPLAY=3;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the SPECIALREPORTED tag */
	public final static int TAG_SPECIALREPORTED=4;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the EDITALONE tag */
	public final static int TAG_EDITALONE=5;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the DEFAULT tag */
	public final static int TAG_DEFAULT=6;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the AUTODEFAULTS tag */
	public final static int TAG_AUTODEFAULTS=7;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the AUTOCHOICES tag */
	public final static int TAG_AUTOCHOICES=8;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the CHOICEINTRO tag */
	public final static int TAG_CHOICEINTRO=9;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the RATEMODIFIER tag */
	public final static int TAG_RATEMODIFIER=10;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the EXPERIENCE tag */
	public final static int TAG_EXPERIENCE=11;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the RANGE tag */
	public final static int TAG_RANGE_=12;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the CHANGE tag */
	public final static int TAG_CHANGE_=13;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the ABILITY tag */
	public final static int TAG_ABILITY_=14;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the FACTOR tag */
	public final static int TAG_FACTOR_=15;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the RELATION tag */
	public final static int TAG_RELATION_=16;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the SHOWINFACTIONSCMD tag */
	public final static int TAG_SHOWINFACTIONSCMD=17;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the AFFBEHAV tag */
	public final static int TAG_AFFBEHAV_=18;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the RELATION tag */
	public final static int TAG_REACTION_=19;
	/** index constant for tag names in {@link Faction#TAG_NAMES} denoting the SCOREDISPLAY tag */
	public final static int TAG_USELIGHTREACTIONS=20;
	/** list of valid tag names for internal faction data, retrieved by {@link Faction#getTagValue(String)} */
	public final static String[] TAG_NAMES={"NAME","MINIMUM","MAXIMUM","SCOREDISPLAY",
											"SPECIALREPORTED","EDITALONE","DEFAULT","AUTODEFAULTS",
											"AUTOCHOICES","CHOICEINTRO","RATEMODIFIER","EXPERIENCE",
											"RANGE*","CHANGE*","ABILITY*","FACTOR*","RELATION*",
											"SHOWINFACTIONSCMD","AFFBEHAV*","REACTION*","USELIGHTREACTIONS"};
}
