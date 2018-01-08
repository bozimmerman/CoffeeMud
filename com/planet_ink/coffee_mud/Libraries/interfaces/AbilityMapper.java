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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
 * The Ability Mapper Library handles everything dealing with the relationship
 * between players, races, etc and Abilities (Skills, Spells, Prayers, etc).
 * The principle duty is to map the provider of player Abilities to those
 * Abilities, along with any requirements for being provided the skills,
 * and the condition the skill is in when it is received.
 * 
 * @author Bo Zimmerman
 *
 */
public interface AbilityMapper extends CMLibrary
{
	//TODO: Just use AbilityMapping class for all these methods, replace classes here with interfaces.

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, boolean, List)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, boolean)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, boolean, String)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param preReqSkillsList String list of required Ability IDs with optional min. proff in parenthesis
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain, List<String> preReqSkillsList);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, boolean, List)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, boolean, List, String)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param extraMasks a zappermask for the player with any miscellaneough requirements
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain, String extraMasks);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, boolean, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param preReqSkillsList String list of required Ability IDs with optional min. proff in parenthesis
	 * @param extraMasks a zappermask for the player with any miscellaneough requirements
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain, List<String> preReqSkillsList, String extraMasks);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, boolean, List, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, boolean)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param defaultParam if the Ability allows parameters, these are the parameters this mapping gives
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency, String defaultParam, boolean autoGain);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean, boolean)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency, boolean autoGain);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * 
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean, boolean)
	 * 
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param skillPreReqs String list of required Ability IDs with optional min. proff in parenthesis
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency, boolean autoGain, List<String> skillPreReqs);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, boolean)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean, boolean, List, String)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param defaultParam if the Ability allows parameters, these are the parameters this mapping gives
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param secret true if this mapping is NOT available to Qualify and WillQualify commands, false if it is. 
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
												String defaultParam, boolean autoGain, boolean secret);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean, boolean)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, int, String, boolean, boolean, List, String, Integer[])
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param defaultParam if the Ability allows parameters, these are the parameters this mapping gives
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param secret true if this mapping is NOT available to Qualify and WillQualify commands, false if it is. 
	 * @param preReqSkillsList String list of required Ability IDs with optional min. proff in parenthesis
	 * @param extraMask a zappermask for the player with any miscellaneough requirements
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
												String defaultParam, boolean autoGain, boolean secret,
												List<String> preReqSkillsList, String extraMask);

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean, boolean, List, String)
	 * @see AbilityMapper#addDynaAbilityMapping(String, int, String, int, String, boolean, boolean, String)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param maxProficiency the maximum proficiency that this mapping allows (100 default)
	 * @param defaultParam if the Ability allows parameters, these are the parameters this mapping gives
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param secret true if this mapping is NOT available to Qualify and WillQualify commands, false if it is. 
	 * @param preReqSkillsList String list of required Ability IDs with optional min. proff in parenthesis
	 * @param extraMask a zappermask for the player with any miscellaneough requirements
	 * @param costOverrides overrides of the CMProps-based cost formulas for gaining this skill
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
												int maxProficiency, String defaultParam, boolean autoGain, boolean secret,
												List<String> preReqSkillsList, String extraMask, Integer[] costOverrides);

	/**
	 * Creates a raw Ability Mapping object.
	 * @see AbilityMapping
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#newAbilityMapping()
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param maxProficiency the maximum proficiency that this mapping allows (100 default)
	 * @param defaultParam if the Ability allows parameters, these are the parameters this mapping gives
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param secret true if this mapping is NOT available to Qualify and WillQualify commands, false if it is. 
	 * @param isAllQualified true if all classes qualify for this skill, as from the All Qualifies list.
	 * @param preReqSkillsList String list of required Ability IDs with optional min. proff in parenthesis
	 * @param extraMask a zappermask for the player with any miscellaneough requirements
	 * @param costOverrides overrides of the CMProps-based cost formulas for gaining this skill
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping makeAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency, int maxProficiency, String defaultParam, boolean autoGain,
			 boolean secret, boolean isAllQualified, List<String> preReqSkillsList, String extraMask, Integer[] costOverrides);

	/**
	 * Creates a new, blank ability mapping object
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @return a new, blank ability mapping object
	 */
	public AbilityMapping newAbilityMapping();

	/**
	 * Adds a mapping between a charclass, race, or whatever, and an Ability, by String Ability ID.
	 * Also allows specifying numerous other attributes.
	 * Substantially identical to methods like {@link AbilityMapper#addCharAbilityMapping(String, int, String, int, String, boolean, boolean, List, String)}
	 * except that the Each/global ability mappings are not initialized if this is the first class
	 * mapping, meaning it's best to call this at runtime instead of boot time.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, int, String, boolean, boolean, List, String, Integer[])
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param qualLevel the charclass or race player level at which one qualifies to receive the ability
	 * @param abilityID the Ability ID()
	 * @param defaultProficiency the initial proficiency at which this mapping bestows the ability
	 * @param defaultParam if the Ability allows parameters, these are the parameters this mapping gives
	 * @param autoGain true if the player/race automatically gets the ability, false if they must pay COSTs
	 * @param secret true if this mapping is NOT available to Qualify and WillQualify commands, false if it is. 
	 * @param extraMask a zappermask for the player with any miscellaneough requirements
	 * @return the finished AbilityMapping
	 */
	public AbilityMapping addDynaAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
									  String defaultParam, boolean autoGain, boolean secret, String extraMask);

	/**
	 * Removes the specified mapping between charclass, race, or whatever, and an Ability, by String ability ID.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, int, String, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharMappings(String)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 * @param abilityID the Ability ID()
	 * @return the mapping removed
	 */
	public AbilityMapping delCharAbilityMapping(String ID, String abilityID);

	/**
	 * Removes all ability mappings for the given charclass, race, or whatever.
	 * @see AbilityMapping
	 * @see AbilityMapper#makeAbilityMapping(String, int, String, int, int, String, boolean, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#addCharAbilityMapping(String, int, String, int, int, String, boolean, boolean, List, String, Integer[])
	 * @see AbilityMapper#delCharAbilityMapping(String, String)
	 * @param ID the race ID(), charclass ID(), "All" is also acceptable.
	 */
	public void delCharMappings(String ID);

	/**
	 * Adds to the list of allowed skills, expertises, etc that come from the requirements
	 * listed for the given skill.  Used mostly by the Expertise system, but also used
	 * by the class system to cross-reference skill requirements.
	 * @param ID the Ability ID of the skill gaining pre-requisites
	 * @param preReqSkillsList the formatted list of prereqs, typically Ability IDs.
	 * @param extraMask a zappermask of other requirements that a person must have for the skill
	 */
	public void addPreRequisites(String ID, List<String> preReqSkillsList, String extraMask);

	/**
	 * Returns an enumeration of all the AbilityMapping object for all the abilities the
	 * given class, race, whatever qualifies for ever.  Will include common (all qualified)
	 * abilities if specified.
	 * @see AbilityMapper.AbilityMapping
	 * @param ID the race ID(), charclass ID(), etc
	 * @param addAll true to include all-qualified abilities, false for just class unique
	 * @return an enumeration of AbilityMapping objects
	 */
	public Enumeration<AbilityMapping> getClassAbles(String ID, boolean addAll);

	/**
	 * Returns the lowest class level at which any class qualifies for the
	 * given ability, returning 0 if non found.
	 * @param abilityID the ability ID()
	 * @return the lowest qualifying level
	 */
	public int lowestQualifyingLevel(String abilityID);

	/**
	 * Returns the median lowest qualifying level for all abilities, given you
	 * an idea of the middle-skill-gaining levels, for some reason.
	 * @return the median lowest qualifying level for all abilities
	 */
	public int getCalculatedMedianLowestQualifyingLevel();

	/**
	 * Returns the AbilityIDs of all the skills qualified for by the given
	 * char class or race or whatever, at the given level, and optionally
	 * including the all-qualified skills.
	 * @see AbilityMapper#getUpToLevelListings(String, int, boolean, boolean)
	 * @param ID the charclass ID(), race ID(), or whatever
	 * @param checkAll true to check the All Qualifies list, or false otherwise
	 * @param level the specific level to check.
	 * @return the AbilityIDs of all the skills qualified for at that level
	 */
	public List<String> getLevelListings(String ID, boolean checkAll, int level);

	/**
	 * Returns the full AbilityMappings of all the skills qualified for by the given
	 * char class or race or whatever, at every level up to and including the 
	 * given level, and optionally excluding the all-qualified skills and
	 * non-gained skills.
	 * @see AbilityMapper#getLevelListings(String, boolean, int)
	 * @param ID the charclass ID(), race ID(), or whatever
	 * @param level the specific level to check.
	 * @param ignoreAll true to ignore the All Qualifies list, or false otherwise
	 * @param gainedOnly true to only include the auto-gain-only skills, false otherwise  
	 * @return the AbilityIDs of all the skills qualified for at that level
	 */
	public List<AbilityMapping> getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly);

	/**
	 * Returns the raw Ability mappings for the given class, race, or clan ID.
	 * Does nto include all-qualified.
	 * @param ID the charclass ID(), race ID(), or whatever
	 * @return the map of AbilityIDs to their ability mappings.
	 */
	public Map<String,AbilityMapping> getAbleMapping(String ID);

	/**
	 * Returns the level at which the given class or race qualifies for the given ability ID(),
	 * optionally checking the All-Qualifies list or not.  Returns -1 for no match.
	 * @see AbilityMapper#qualifyingLevel(MOB, Ability)
	 * @see AbilityMapper#qualifyingClassLevel(MOB, Ability)
	 * @see AbilityMapper#lowestQualifyingClassRaceGovt(MOB, Ability)
	 * @param ID the charclass ID(), race ID(), or whatever
	 * @param checkAll true to check the All Qualifies list, false to skip it
	 * @param abilityID the Ability ID() to find a level for
	 * @return the level at which the give class or race qualifies, or -1
	 */
	public int getQualifyingLevel(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns the mapping which the given class or race qualifies for the given ability ID(),
	 * optionally checking the All-Qualifies list or not.  Returns null for no match.
	 * @see AbilityMapper#qualifyingLevel(MOB, Ability)
	 * @see AbilityMapper#qualifyingClassLevel(MOB, Ability)
	 * @see AbilityMapper#lowestQualifyingClassRaceGovt(MOB, Ability)
	 * @param ID the charclass ID(), race ID(), or whatever
	 * @param checkAll true to check the All Qualifies list, false to skip it
	 * @param abilityID the Ability ID() to find a map for
	 * @return the level at which the give class or race qualifies, or null
	 */
	public AbilityMapping getQualifyingMapping(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns the lowest class or player level at which the given mob (by race or 
	 * class) qualified for the given ability if any. Returns -1 for no match.
	 * This method is called when you are trying to guage how powerful the player
	 * is with the skill.
	 * @see AbilityMapper#getQualifyingLevel(String, boolean, String)
	 * @see AbilityMapper#qualifyingClassLevel(MOB, Ability)
	 * @see AbilityMapper#lowestQualifyingClassRaceGovt(MOB, Ability)
	 * @param studentM the mob, whose charclass ID(), race ID() are checked
	 * @param A the Ability to find a level for
	 * @return the best level at which the given mob qualified, or -1
	 */
	public int qualifyingLevel(MOB studentM, Ability A);

	/**
	 * Returns the lowest char class level at which the given mob (by race or 
	 * class) qualified for the given ability if any. Returns -1 for no match.
	 * This method is called when you are trying to guage how powerful the player
	 * is with the skill, and only care about class skills.
	 * @see AbilityMapper#getQualifyingLevel(String, boolean, String)
	 * @see AbilityMapper#qualifyingLevel(MOB, Ability)
	 * @see AbilityMapper#lowestQualifyingClassRaceGovt(MOB, Ability)
	 * @param studentM the mob, whose charclass ID() is checked
	 * @param A the Ability to find a level for
	 * @return the best char class level at which the given mob qualified, or -1
	 */
	public int qualifyingClassLevel(MOB studentM, Ability A);

	/**
	 * Returns the class, race, or clan government object that qualifies
	 * the given mob at the lowest level for the given ability.
	 * @see AbilityMapper#getQualifyingLevel(String, boolean, String)
	 * @see AbilityMapper#qualifyingLevel(MOB, Ability)
	 * @see AbilityMapper#qualifyingClassLevel(MOB, Ability)
	 * @param studentM the mob whose class, race, etc to check
	 * @param A the Ability to check
	 * @return the char class, race, or clan government object.
	 */
	public CMObject lowestQualifyingClassRaceGovt(MOB studentM, Ability A);

	/**
	 * Returns whether the given mob qualifies for the given ability
	 * only because of their clan affiliations.
	 * @see AbilityMapper#qualifiesOnlyByRace(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, String)
	 * @see AbilityMapper#qualifiesOnlyByACharClass(MOB, Ability)
	 * @see AbilityMapper#qualifiesByCurrentClassAndLevel(MOB, Ability)
	 * @param studentM the mob to check
	 * @param A the ability to check
	 * @return true if the student only qualifies because of their clan
	 */
	public boolean qualifiesOnlyByClan(MOB studentM, Ability A);

	/**
	 * Returns whether the given mob qualifies for the given ability
	 * only because of their race.
	 * @see AbilityMapper#qualifiesOnlyByClan(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, String)
	 * @see AbilityMapper#qualifiesOnlyByACharClass(MOB, Ability)
	 * @see AbilityMapper#qualifiesByCurrentClassAndLevel(MOB, Ability)
	 * @param studentM the mob to check
	 * @param A the ability to check
	 * @return true if the student only qualifies because of their race
	 */
	public boolean qualifiesOnlyByRace(MOB studentM, Ability A);

	/**
	 * Returns whether the given mob qualifies for the given ability
	 * only because of their current class at its current level.
	 * @see AbilityMapper#qualifiesOnlyByClan(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, String)
	 * @see AbilityMapper#qualifiesOnlyByACharClass(MOB, Ability)
	 * @see AbilityMapper#qualifiesOnlyByRace(MOB, Ability)
	 * @param studentM the mob to check
	 * @param A the ability to check
	 * @return true if the student only qualifies because of their current char class
	 */
	public boolean qualifiesByCurrentClassAndLevel(MOB studentM, Ability A);

	/**
	 * Returns whether the given mob qualifies for the given ability
	 * only because of their a char class they have, at its current level.
	 * @see AbilityMapper#qualifiesOnlyByClan(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, String)
	 * @see AbilityMapper#qualifiesByCurrentClassAndLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesOnlyByRace(MOB, Ability)
	 * @param studentM the mob to check
	 * @param A the ability to check
	 * @return true if the student only qualifies because of a char class they have
	 */
	public boolean qualifiesOnlyByACharClass(MOB studentM, Ability A);

	/**
	 * Returns whether the given mob qualifies for the given ability
	 * because of any of their char classes at its current level, race, or clan.
	 * @see AbilityMapper#qualifiesOnlyByClan(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, String)
	 * @see AbilityMapper#qualifiesByCurrentClassAndLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesOnlyByRace(MOB, Ability)
	 * @param studentM the mob to check
	 * @param A the ability to check
	 * @return true if the student qualifies because of their classes, race, etc
	 */
	public boolean qualifiesByLevel(MOB studentM, Ability A);

	/**
	 * Returns whether the given mob qualifies for the given ability
	 * because of any of their char classes at its current level, race, or clan.
	 * @see AbilityMapper#qualifiesOnlyByClan(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesByLevel(MOB, String)
	 * @see AbilityMapper#qualifiesByCurrentClassAndLevel(MOB, Ability)
	 * @see AbilityMapper#qualifiesOnlyByRace(MOB, Ability)
	 * @param studentM the mob to check
	 * @param abilityID the Ability ID() to check
	 * @return true if the student qualifies because of their classes, race, etc
	 */
	public boolean qualifiesByLevel(MOB studentM, String abilityID);

	/**
	 * Returns whether the given Ability ID() represents a skill qualified for by
	 * any existing character class, including All-Qualified abilities.
	 * @see AbilityMapper#classOnly(String, String)
	 * @see AbilityMapper#classOnly(MOB, String, String)
	 * @param abilityID the ability ID()
	 * @return true if a class qualifies for it, false otherwise
	 */
	public boolean qualifiesByAnyCharClass(String abilityID);

	/**
	 * Returns whether the given Ability ID() represents a skill qualified for by
	 * any existing character class, or race, including All-Qualified abilities.
	 * @see AbilityMapper#classOnly(String, String)
	 * @see AbilityMapper#qualifiesByAnyCharClass(String)
	 * @param abilityID the ability ID()
	 * @return true if a class or race qualifies for it, false otherwise
	 */
	public boolean qualifiesByAnyCharClassOrRace(String abilityID);

	/**
	 * Returns whether the given class qualifies for the given ability.
	 * Does not check all-qualifies list, so this is only class (or race)
	 * specific qualifications.  Returns true ONLY if the given class
	 * or race is the ONLY one who qualifies.
	 * @see AbilityMapper#classOnly(MOB, String, String)
	 * @see AbilityMapper#qualifiesByAnyCharClass(String)
	 * @param classID the class ID(), race ID() or whatever
	 * @param abilityID the ability ID()
	 * @return true if the class qualifies for this ability
	 */
	public boolean classOnly(String classID, String abilityID);

	/**
	 * Returns whether the given class qualifies for the given ability.
	 * Does not check all-qualifies list, so this is only class (or race)
	 * specific qualifications.  Will also specifically check the given
	 * mobs class object with the given ID, which is strange. Returns true 
	 * ONLY if the given class or race is the ONLY one who qualifies.
	 * @see AbilityMapper#classOnly(String, String)
	 * @see AbilityMapper#qualifiesByAnyCharClass(String)
	 * @param mob the mob whose classes to also check
	 * @param classID the class ID(), race ID() or whatever to specifically check
	 * @param abilityID the ability ID() to use
	 * @return true if the class qualifies for this ability
	 */
	public boolean classOnly(MOB mob, String classID, String abilityID);

	/**
	 * Discovers whether the given ability is qualified for by a class that
	 * is available to the given theme id number.
	 * @see Area#THEME_ALLTHEMES
	 * @param abilityID the Ability ID()
	 * @param theme the theme code
	 * @param publicly true to disqualify skill-only masks, false to allow
	 * @return true if the ability is qualifies for by a char class of the theme
	 */
	public boolean availableToTheme(String abilityID, int theme, boolean publicly);

	/**
	 * Returns the total number of abilities mapped to a class or race or something.
	 * @see AbilityMapper#getAllAbleMap(String)
	 * @see AbilityMapper#getAbleMap(String, String)
	 * @return the total number of abilities mapped to a class or race or something.
	 */
	public int numMappedAbilities();

	/**
	 * Returns the ability mapping that is defined by the All Qualifies list for
	 * the given Ability ID(), or null.
	 * @see AbilityMapper.AbilityMapping
	 * @see AbilityMapper#numMappedAbilities()
	 * @see AbilityMapper#getAbleMap(String, String)
	 * @param abilityID the Ability ID() 
	 * @return the ability mapping that is defined, or null
	 */
	public AbilityMapping getAllAbleMap(String abilityID);

	/**
	 * Returns the ability mapping that is defined by the given char class, race, or 
	 * clan ID for the given Ability ID(), or null.
	 * @see AbilityMapper.AbilityMapping
	 * @see AbilityMapper#numMappedAbilities()
	 * @see AbilityMapper#getAllAbleMap(String)
	 * @param ID the CharClass ID(), Race ID(), or clan ID 
	 * @param abilityID the Ability ID() 
	 * @return the ability mapping that is defined, or null
	 */
	public AbilityMapping getAbleMap(String ID, String abilityID);

	/**
	 * Returns an iterator over the list of expertises and skills which having the given
	 * skill will allow.
	 * @see AbilityMapper#getClassAllowsList(String)
	 * @param ableID the skill to get the allows list for
	 * @return an iterator over the list of expertises and skills
	 */
	public Iterator<String> getAbilityAllowsList(String ableID);

	/**
	 * Returns the list of things allowed by skill which the given class/race/whatever
	 * qualifies over their life, along with qualifying levels.  This is intended entirely for
	 * gathering expertises qualified for by class skills. 
	 * @see AbilityMapper#getAbilityAllowsList(String)
	 * @see AbilityMapper.QualifyingID
	 * @param ID the charclass ID(), race ID(), or whatever
	 * @return the list of skill and skill qualifying information for that class
	 */
	public List<QualifyingID> getClassAllowsList(String ID);

	/**
	 * Returns the zapper mask that applies to the given class or race for the given 
	 * ability ID(), optionally checking the All-Qualifies list or not.  Returns null 
	 * for no match. The mask is checked against a potential learner of this skill.
	 * @see MaskingLibrary
	 * @param ID the charclass ID(), race ID(), or whatever
	 * @param checkAll true to check the All Qualifies list, false to skip it
	 * @param abilityID the Ability ID() to find a level for
	 * @return the zapper mask that applies
	 */
	public String getExtraMask(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns the first applicable zapper mask that applies to the given mob for 
	 * the given ability. Returns null for nothing found. The mask is checked 
	 * against a potential learner of this skill.
	 * @see MaskingLibrary
	 * @param studentM the mob, whose clans, charclass ID(), race ID() are checked
	 * @param A the Ability to find a mask for
	 * @return the zapper mask that applies
	 */
	public String getApplicableMask(MOB studentM, Ability A);

	/**
	 * Returns the general zapper mask that defines the requirements to learn
	 * the given ability, by ID.  The mask is applied to the learner mob.
	 * @see MaskingLibrary
	 * @param A the Ability whose ID is looked up
	 * @return the zapper mask, or ""
	 */
	public String getCommonExtraMask(Ability A);

	/**
	 * Given a mob who wants to learn the given Ability skill, this
	 * method will check the mob (by race/class/whatever) against the
	 * ability they qualify for, determining if there are any pre-requisite
	 * skills needed to learn this skill.  If they are found, it returns
	 * those pre-requisites, specially coded.
	 * @see AbilityMapper#getCommonPreRequisites(MOB, Ability)
	 * @see AbilityMapper#getCommonPreRequisites(Ability)
	 * @see AbilityMapper#formatPreRequisites(DVector)
	 * @see AbilityMapper#getPreReqStrings(String, boolean, String)
	 * @param studentM the mob who wants to learn
	 * @param A the Ability the mob wants to learn
	 * @return any pre-requisites, or null for none.
	 */
	public DVector getUnmetPreRequisites(MOB studentM, Ability A);

	/**
	 * Returns the coded form of the skill prerequisites for the
	 * given ability.  Skill prerequisites are typically other
	 * skills needed to learn this one.  They are called Common
	 * because, for the given ability, they apply to everyone.
	 * @see AbilityMapper#getCommonPreRequisites(MOB, Ability)
	 * @see AbilityMapper#getUnmetPreRequisites(MOB, Ability)
	 * @see AbilityMapper#formatPreRequisites(DVector)
	 * @see AbilityMapper#getPreReqStrings(String, boolean, String)
	 * @param A the ability to look for prerequisites to learn
	 * @return the coded skill prerequisites
	 */
	public DVector getCommonPreRequisites(Ability A);

	/**
	 * Returns the coded form of the skill prerequisites for the
	 * given ability that apply to the given mob by race or class.  
	 * Skill prerequisites are typically other skills needed to 
	 * learn this one.  
	 * @see AbilityMapper#getCommonPreRequisites(Ability)
	 * @see AbilityMapper#getUnmetPreRequisites(MOB, Ability)
	 * @see AbilityMapper#formatPreRequisites(DVector)
	 * @see AbilityMapper#getPreReqStrings(String, boolean, String)
	 * @param mob the potential learner of the ability
	 * @param A the ability to look for prerequisites to learn
	 * @return the coded skill prerequisites
	 */
	public DVector getCommonPreRequisites(MOB mob, Ability A);

	/**
	 * Given a set of common Ability/skill pre-requisites
	 * for learning, coded, this method will return those
	 * pre-requisites formatted in a friendly, readable form.
	 * @see AbilityMapper#getCommonPreRequisites(Ability)
	 * @see AbilityMapper#getUnmetPreRequisites(MOB, Ability)
	 * @see AbilityMapper#getCommonPreRequisites(MOB, Ability)
	 * @see AbilityMapper#getPreReqStrings(String, boolean, String)
	 * @param preReqs the coded pre-requisites for this skill
	 * @return a friendly readable description of those pre-reqs
	 */
	public String formatPreRequisites(DVector preReqs);

	/**
	 * Gets the raw pre-requisites definition for the given mapping by
	 * class, race, clan ID and ability ID(), optionally also checking the
	 * All-Qualifies list.  Returns "" if not found, or there are no 
	 * skill prerequisites.  This is a formatted string where Ability IDs
	 * are generally semicolon delimited, with required proficiency in 
	 * parenthesis.
	 * @see AbilityMapper#getCommonPreRequisites(Ability)
	 * @see AbilityMapper#getUnmetPreRequisites(MOB, Ability)
	 * @see AbilityMapper#getCommonPreRequisites(MOB, Ability)
	 * @see AbilityMapper#formatPreRequisites(DVector)
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return the raw formatted pre-requisite string.
	 */
	public String getPreReqStrings(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns whether the given ability, for the given charclass, race, or clan
	 * government ID, and optionally checking the All Qualifies list, is gained
	 * by default or must be trained.
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return true if its gained automatically, or false if it must be trained
	 */
	public boolean getDefaultGain(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns whether the given ability, for the given charclass, race, or clan
	 * government ID, and optionally checking the All Qualifies list, is part
	 * of the All Qualified list.
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return true if the ability is part of the all qualifies list, false otherwise
	 */
	public boolean getAllQualified(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns whether the given ability, for the given charclass, race, or clan
	 * government ID, and optionally checking the All Qualifies list, is a
	 * secret skill, or whether it can be seen and known about.
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return true if its secret, false otherwise
	 */
	public boolean getSecretSkill(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns whether the given Ability ID() is both mapped on the All Qualifies
	 * list AND is a secret skill.
	 * @param abilityID the ability ID() to check
	 * @return true if its secret, false otherwise
	 */
	public boolean getAllSecretSkill(String abilityID);

	/**
	 * Returns whether the given ability ID() represents a skill that is secret to
	 * the given mob, by whatever class, race, or clan they qualify for it by.
	 * @param mob the mob to check
	 * @param abilityID the ability ID() to check
	 * @return true if the ability is secret for this mob, false otherwise
	 */
	public boolean getSecretSkill(MOB mob, String abilityID);

	/**
	 * Returns whether the given Ability ID() is secret in every mapping (race, class,
	 * govt clan id, all qualifies) or not.
	 * @param abilityID the ability ID() to check
	 * @return true if its secret everywhere, false otherwise
	 */
	public boolean getSecretSkill(String abilityID);

	/**
	 * Returns any mapping-based overrides to the standard system white
	 * standards for casting costs (the amount of mana or moves to use a skill).
	 * The integer array is indexed by the ordinals of the Cost enum.
	 * @see AbilityMapper.Cost
	 * @see AbilityMapper#getAllCostOverrides(String)
	 * @see AbilityMapper#getCostOverrides(String)
	 * @see AbilityMapper#getCostOverrides(MOB, String)
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return an integer array of the mapping-based overrides to usage costs
	 */
	public Integer[] getCostOverrides(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns any All-Qualifies overrides to the standard system white
	 * standards for casting costs (the amount of mana or moves to use a skill).
	 * The integer array is indexed by the ordinals of the Cost enum.
	 * @see AbilityMapper.Cost
	 * @see AbilityMapper#getCostOverrides(String, boolean, String)
	 * @see AbilityMapper#getCostOverrides(String)
	 * @see AbilityMapper#getCostOverrides(MOB, String)
	 * @param abilityID the ability ID() to check
	 * @return an integer array of the mapping-based overrides to usage costs
	 */
	public Integer[] getAllCostOverrides(String abilityID);

	/**
	 * Returns any mapping-based overrides to the standard system white
	 * standards for casting costs (the amount of mana or moves to use a skill)
	 * relevant to the given mob, based on their class, race, etc.
	 * The integer array is indexed by the ordinals of the Cost enum.
	 * @see AbilityMapper.Cost
	 * @see AbilityMapper#getAllCostOverrides(String)
	 * @see AbilityMapper#getCostOverrides(String)
	 * @see AbilityMapper#getCostOverrides(String, boolean, String)
	 * @param mob the mob whose charclass, race, or clan ID() is relevant
	 * @param abilityID the ability ID() to check
	 * @return an integer array of the mapping-based overrides to usage costs
	 */
	public Integer[] getCostOverrides(MOB mob, String abilityID);

	/**
	 * Returns the first mapping-based override to the standard system white
	 * standards for casting costs (the amount of mana or moves to use a skill).
	 * The integer array is indexed by the ordinals of the Cost enum.
	 * @see AbilityMapper.Cost
	 * @see AbilityMapper#getCostOverrides(String, boolean, String)
	 * @see AbilityMapper#getAllCostOverrides(String)
	 * @see AbilityMapper#getCostOverrides(MOB, String)
	 * @param abilityID the ability ID() to check
	 * @return an integer array of the mapping-based overrides to usage costs
	 */
	public Integer[] getCostOverrides(String abilityID);

	/**
	 * Returns the default argument/parameter to add to the given Ability by ID()
	 * when gained by the class, race, clan ID, optionally also checking the
	 * All-Qualifies list.  The argument is sent as the misc-text to the 
	 * Ability when adding it as a skill.
	 * @see Environmental#setMiscText(String)
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return the default misc-text arguments for this mapped Ability
	 */
	public String getDefaultParm(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns the default proficiency to give to the given Ability by ID()
	 * when gained by the class, race, clan ID, optionally also checking the
	 * All-Qualifies list. 
	 * @see Ability#proficiency()
	 * @see AbilityMapper#getMaxProficiency(MOB, boolean, String)
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return the default proficiency argument for this mapped Ability, usually 0
	 */
	public int getDefaultProficiency(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns the max allowed proficiency for those with the given Ability by ID()
	 * when carried by the class, race, clan ID, optionally also checking the
	 * All-Qualifies list. 
	 * @see Ability#proficiency()
	 * @see AbilityMapper#getMaxProficiency(String)
	 * @see AbilityMapper#getMaxProficiency(String, boolean, String)
	 * @param ID the charclass, race, or clan ID()
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return the max proficiency for someone with this this mapped Ability, usually 100
	 */
	public int getMaxProficiency(String ID, boolean checkAll, String abilityID);

	/**
	 * Returns the max allowed proficiency for those with the given Ability by ID()
	 * when carried by any class, race, clan ID, while also checking the
	 * All-Qualifies list. 
	 * @see AbilityMapper#getMaxProficiency(MOB, boolean, String)
	 * @see AbilityMapper#getMaxProficiency(String, boolean, String)
	 * @see Ability#proficiency()
	 * @param abilityID the ability ID() to check
	 * @return the max proficiency for everyone with this this mapped Ability, usually 100
	 */
	public int getMaxProficiency(String abilityID);

	/**
	 * Returns the max allowed proficiency for those with the given Ability by ID()
	 * when carried by the mob by class, race, clan ID, optionally also checking the
	 * All-Qualifies list. 
	 * @see Ability#proficiency()
	 * @see AbilityMapper#getDefaultProficiency(String, boolean, String)
	 * @see AbilityMapper#getMaxProficiency(String)
	 * @see AbilityMapper#getMaxProficiency(String, boolean, String)
	 * @param mob the mob whose charclass, race, or clan ID() applies
	 * @param checkAll true to check the All Qualifies list, or false not to
	 * @param abilityID the ability ID() to check
	 * @return the max proficiency for the mob with this this mapped Ability, usually 100
	 */
	public int getMaxProficiency(MOB mob, boolean checkAll, String abilityID);

	/**
	 * Loads the All-Qualifies list from the filesystem.  This is the list that defines particular
	 * skills that either ALL classes qualify for together.  Things like Skill_Write, or Swim..
	 * The method takes an optional cache to preserve a loaded map over several sessions.  Otherwise,
	 * it is usually only needed once.
	 * @see AbilityMapper#saveAllQualifysFile(Map)
	 * @param cache a cache to store the map in temporarily, or null
	 * @return the All-Qualifies skills in a coded map
	 */
	public Map<String, Map<String,AbilityMapping>> getAllQualifiesMap(final Map<String,Object> cache);

	/**
	 * Returns a converter from an ability id to an ability mapping
	 * @param classID the classid (or 'all') that owns the mapping
	 * @return the converter
	 */
	public Converter<String,AbilityMapping> getMapper(final String classID);

	/**
	 * Saves the All-Qualifies list to the filesystem.  This is the list that defines particular
	 * skills that either ALL classes qualify for together.  Things like Skill_Write, or Swim.
	 * @see AbilityMapper#getAllQualified(String, boolean, String)
	 * @param newMap the All-Qualifies skills in a coded map
	 */
	public void saveAllQualifysFile(Map<String, Map<String,AbilityMapping>> newMap);

	/**
	 * Returns a String list of all the classes and levels that qualify for the given
	 * skill.  If more than the given abbreviateAt skills qualify, then the list
	 * will start abstracting the list by returning abstract terms instead of class ids.
	 * @param A the skill to get a list of qualifiers for
	 * @param abbreviateAt the number of classes beyond which is starts aggregating
	 * @return a list of class ids/aggregate strings and levels
	 */
	public PairList<String,Integer> getAvailabilityList(final Ability A, int abbreviateAt);

	/**
	 * A mapping between an Ability ID and it's qualifying level
	 * @see AbilityMapper#getAbilityAllowsList(String)
	 * @author Bo Zimmerman
	 */
	public static interface QualifyingID
	{
		/**
		 * Returns the Ability ID()
		 * @return the Ability ID()
		 */
		public String ID();

		/**
		 * Gets the level at which this mapping qualifies for the given Ability ID
		 * @return the level at which this mapping qualifies for the given Ability ID
		 */
		public int qualifyingLevel();

		/**
		 * sets the level at which this mapping qualifies for the given Ability ID
		 * @param newLevel the level at which this mapping qualifies for the given Ability ID
		 * @return this
		 */
		public QualifyingID qualifyingLevel(int newLevel);
	}

	/**
	 * An enum usually used to index an array of different kinds
	 * of skill use costs. 
	 * @see AbilityMapper#getCostOverrides(String)
	 * @author Bo Zimmerman
	 */
	public enum Cost
	{
		PRAC,
		TRAIN,
		MANA,
		PRACPRAC
	}

	/**
	 * An official Ability Mapping, used by many of the AbilityMapper
	 * classes.  This class doubles as a builder of itself as well.
	 * @see AbilityMapper
	 * @author Bo Zimmerman
	 *
	 */
	public static interface AbilityMapping extends Cloneable
	{
		/**
		 * Returns the class, race, or clan government ID this
		 * mapping applies to.
		 * @see AbilityMapper.AbilityMapping#ID(String)
		 * @return the class, race, or clan government ID
		 */
		public String				ID();

		/**
		 * Sets the class, race, or clan government ID this
		 * mapping applies to.
		 * @see AbilityMapper.AbilityMapping#ID()
		 * @param newValue ID the class, race, or clan government ID
		 * @return this
		 */
		public AbilityMapping		ID(String newValue);

		/**
		 * Returns the ability ID() this mapping belongs to.
		 * @see AbilityMapper.AbilityMapping#abilityID(String)
		 * @return the ability ID() this mapping belongs to.
		 */
		public String				abilityID();

		/**
		 * Sets the ability ID() this mapping belongs to.
		 * @see AbilityMapper.AbilityMapping#abilityID()
		 * @param newValue the ability ID() this mapping belongs to.
		 * @return this
		 */
		public AbilityMapping		abilityID(String newValue);

		/**
		 * Returns the class/race/clan qualifying level.
		 * @see AbilityMapper.AbilityMapping#qualLevel(int)
		 * @return the class/race/clan qualifying level.
		 */
		public int					qualLevel();

		/**
		 * Sets the class/race/clan qualifying level.
		 * @see AbilityMapper.AbilityMapping#qualLevel()
		 * @param newValue the class/race/clan qualifying level.
		 * @return this
		 */
		public AbilityMapping		qualLevel(int newValue);

		/**
		 * Returns true if the skill is automatically gained, false
		 * if it must be trained.
		 * @see AbilityMapper.AbilityMapping#autoGain(boolean)
		 * @return true if the skill is automatically gained
		 */
		public boolean				autoGain();

		/**
		 * Sets true if the skill is automatically gained, false
		 * if it must be trained.
		 * @see AbilityMapper.AbilityMapping#autoGain()
		 * @param newValue true if the skill is automatically gained
		 * @return this
		 */
		public AbilityMapping		autoGain(boolean newValue);

		/**
		 * Returns the default proficiency of the skill gained under
		 * this mapping.
		 * @see AbilityMapper.AbilityMapping#defaultProficiency(int)
		 * @return the default proficiency of the skill gained
		 */
		public int					defaultProficiency();

		/**
		 * Sets the default proficiency of the skill gained under
		 * this mapping.
		 * @see AbilityMapper.AbilityMapping#defaultProficiency()
		 * @param newValue the default proficiency of the skill gained
		 * @return this
		 */
		public AbilityMapping		defaultProficiency(int newValue);

		/**
		 * Returns the maximum proficiency of the skill gained under
		 * this mapping.
		 * @see AbilityMapper.AbilityMapping#maxProficiency(int)
		 * @return the maximum proficiency of the skill gained
		 */
		public int					maxProficiency();

		/**
		 * Sets the maximum proficiency of the skill gained under
		 * this mapping.
		 * @see AbilityMapper.AbilityMapping#maxProficiency()
		 * @param newValue the maximum proficiency of the skill gained
		 * @return this
		 */
		public AbilityMapping		maxProficiency(int newValue);

		/**
		 * Returns the default argument/parameter applies to skills
		 * gained under this mapping.
		 * @see AbilityMapper.AbilityMapping#defaultParm(String)
		 * @return the default argument/parameter
		 */
		public String				defaultParm();

		/**
		 * Sets the default argument/parameter applies to skills
		 * gained under this mapping.
		 * @see AbilityMapper.AbilityMapping#defaultParm()
		 * @param newValue the default argument/parameter
		 * @return this
		 */
		public AbilityMapping		defaultParm(String newValue);

		/**
		 * Gets whether this skill is secret and unseen even when
		 * qualified for.
		 * @see AbilityMapper.AbilityMapping#isSecret(boolean)
		 * @return whether this skill is secret and unseen
		 */
		public boolean				isSecret();

		/**
		 * Sets whether this skill is secret and unseen even when
		 * qualified for.
		 * @see AbilityMapper.AbilityMapping#isSecret()
		 * @param newValue whether this skill is secret and unseen
		 * @return this
		 */
		public AbilityMapping		isSecret(boolean newValue);

		/**
		 * Gets whether this skill is qualified for identically
		 * by all classes, thus sharing a mapping.
		 * @see AbilityMapper.AbilityMapping#isAllQualified(boolean)
		 * @return whether this skill is qualified for by all classes
		 */
		public boolean				isAllQualified();

		/**
		 * Sets whether this skill is qualified for identically
		 * by all classes, thus sharing a mapping.
		 * @see AbilityMapper.AbilityMapping#isAllQualified()
		 * @param newValue whether this skill is qualified for by all classes
		 * @return this
		 */
		public AbilityMapping		isAllQualified(boolean newValue);

		/**
		 * Gets the coded form of the pre-requisites skills needed
		 * to train or gain this skill.
		 * @see AbilityMapper.AbilityMapping#originalSkillPreReqList()
		 * @see AbilityMapper.AbilityMapping#originalSkillPreReqList(String)
		 * @see AbilityMapper.AbilityMapping#skillPreReqs(DVector)
		 * @return the coded form of the pre-requisites skills needed
		 */
		public DVector				skillPreReqs();

		/**
		 * Sets the coded form of the pre-requisites skills needed
		 * to train or gain this skill.
		 * @see AbilityMapper.AbilityMapping#originalSkillPreReqList()
		 * @see AbilityMapper.AbilityMapping#originalSkillPreReqList(String)
		 * @see AbilityMapper.AbilityMapping#skillPreReqs()
		 * @param newValue the coded form of the pre-requisites skills needed
		 * @return this
		 */
		public AbilityMapping		skillPreReqs(DVector newValue);

		/**
		 * Gets the zapper mask to apply to players to see if they
		 * qualify to learn or gain this skill under this mapping.
		 * @see AbilityMapper.AbilityMapping#extraMask(String)
		 * @return the zapper mask to apply to players
		 */
		public String				extraMask();

		/**
		 * Sets the zapper mask to apply to players to see if they
		 * qualify to learn or gain this skill under this mapping.
		 * @see AbilityMapper.AbilityMapping#extraMask()
		 * @param newValue the zapper mask to apply to players
		 * @return this
		 */
		public AbilityMapping		extraMask(String newValue);

		/**
		 * Gets the uncoded raw string form of the pre-requisite skills
		 * needed to train or gain this skill
		 * @see AbilityMapper.AbilityMapping#skillPreReqs()
		 * @see AbilityMapper.AbilityMapping#skillPreReqs(DVector)
		 * @see AbilityMapper.AbilityMapping#originalSkillPreReqList(String)
		 * @return the uncoded raw string form of the pre-requisite skills
		 */
		public String				originalSkillPreReqList();

		/**
		 * Sets the uncoded raw string form of the pre-requisite skills
		 * needed to train or gain this skill
		 * @see AbilityMapper.AbilityMapping#skillPreReqs()
		 * @see AbilityMapper.AbilityMapping#skillPreReqs(DVector)
		 * @see AbilityMapper.AbilityMapping#originalSkillPreReqList()
		 * @param newValue the uncoded raw string form of the pre-requisite skills
		 * @return this
		 */
		public AbilityMapping		originalSkillPreReqList(String newValue);

		/**
		 * Gets the array of cost overrides, indexed by the Costs enum, or
		 * null if there is no overrides of the basic costs.
		 * @see AbilityMapper.Cost
		 * @see AbilityMapper.AbilityMapping#costOverrides(Integer[])
		 * @return the array of cost overrides, or null
		 */
		public Integer[]			costOverrides();

		/**
		 * Sets the array of cost overrides, indexed by the Costs enum, or
		 * null if there is no overrides of the basic costs.
		 * @see AbilityMapper.Cost
		 * @see AbilityMapper.AbilityMapping#costOverrides()
		 * @param newValue the array of cost overrides, or null
		 * @return this
		 */
		public AbilityMapping		costOverrides(Integer[] newValue);

		/**
		 * Gets whether this skill is qualified for identically
		 * by all classes, thus sharing a mapping.  This is true 
		 * when from the AllQualifies list file, and false when
		 * from a class or coded definition.
		 * @see AbilityMapper.AbilityMapping#allQualifyFlag(boolean)
		 * @return whether this skill is qualified for by all classes
		 */
		public boolean				allQualifyFlag();

		/**
		 * Sets whether this skill is qualified for identically
		 * by all classes, thus sharing a mapping.  This is true 
		 * when from the AllQualifies list file, and false when
		 * from a class or coded definition.
		 * @see AbilityMapper.AbilityMapping#allQualifyFlag()
		 * @param newValue whether this skill is qualified for by all classes
		 * @return this
		 */
		public AbilityMapping		allQualifyFlag(boolean newValue);

		/**
		 * Gets a key/value pair mappings of extraneous information to
		 * store with this ability mapping.
		 * @see AbilityMapper.AbilityMapping#extFields(Map)
		 * @return a key/value pair mappings of extraneous information
		 */
		public Map<String, String>	extFields();

		/**
		 * Sets a key/value pair mappings of extraneous information to
		 * store with this ability mapping.
		 * @see AbilityMapper.AbilityMapping#extFields()
		 * @param newValue a key/value pair mappings of extraneous information
		 * @return this
		 */
		public AbilityMapping		extFields(Map<String, String> newValue);

		/**
		 * Returns a copy of this mapping.  It's a deep copy.
		 * @return a copy of this mapping.  It's a deep copy.
		 */
		public AbilityMapping		copyOf();
	}
}
