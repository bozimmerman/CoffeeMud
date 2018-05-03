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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2015-2018 Bo Zimmerman

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
 * Library for configureing and managing the rules for what resource
 * components (magic dust, tools, etc) are required every time a 
 * particular skill is used.  Normally this would be part of the 
 * skill itself, but since this feature was added so late, it's 
 * separate.  
 * 
 *  Also here are common skill limit utilities, for determining
 *  how many common skills a player can learn.
 *  
 * @author Bo Zimmerman
 */
public interface AbilityComponents extends CMLibrary
{
	/**
	 * Checks whether the given mob has the given components
	 * required to use a skill available to him/her, and if found,
	 * returns them as a FoundComponents list.
	 * @see AbilityComponents#getAbilityComponents(String)
	 * @param mob the mob whose inventory or room or both to check
	 * @param req the ability components rules definition
	 * @param mithrilOK true to allow mithril as a metal substitute
	 * @return null if missing components, or the list of found components
	 */
	public List<Object> componentCheck(MOB mob, List<AbilityComponent> req, boolean mithrilOK);
	
	/**
	 * Returns a very dirty approximate of a sample of what components appear
	 * to be required.  Named items are not required, but raw material components
	 * are created, and most rules are ignored in order to generate as many as
	 * possible.
	 * @see AbilityComponents#getAbilityComponents(String)
	 * @param req the ability components rules definition
	 * @param mithrilOK true to allow mithril as a metal substitute
	 * @return a list of sample items
	 */
	public List<Item> componentsSample(List<AbilityComponent> req, boolean mithrilOK);
	
	/**
	 * If the ability component recipe used to build the list of found
	 * components needed to use a skill requires that any of the componenets
	 * are destroyed.
	 * @see AbilityComponents#componentCheck(MOB, List, boolean)
	 * @param found the components found with componentCheck
	 * @return the value of the components destroyed
	 */
	public int destroyAbilityComponents(List<Object> found);
	
	/**
	 * Returns a friendly readable form of the component requirements
	 * of the given Ability/Skill ID(), or null if that ability has
	 * no requirements.  Since requirements may differ by player
	 * mask, the player mob is also required.
	 * @see AbilityComponents#getAbilityComponentDesc(MOB, List)
	 * @see AbilityComponents#getAbilityComponentDesc(MOB, AbilityComponent, boolean)
	 * @param mob the player mob who wants to know
	 * @param AID the Ability ID() of the skill whose components to check
	 * @return a friendly readable form of the component requirements
	 */
	public String getAbilityComponentDesc(MOB mob, String AID);
	
	/**
	 * Returns a friendly readable form of the component requirements
	 * of the given Ability/Skill Component List, or null if it has
	 * no requirements.  Since requirements may differ by player
	 * mask, the player mob is also required.
	 * @see AbilityComponents#getAbilityComponentDesc(MOB, AbilityComponent, boolean)
	 * @see AbilityComponents#getAbilityComponentDesc(MOB, String)
	 * @param mob the player mob who wants to know
	 * @param req the coded requirements list
	 * @return a friendly readable form of the component requirements
	 */
	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req);
	
	/**
	 * Returns a friendly readable description of a specific component in 
	 * the given decoded ability components definition list.  If the 
	 * component does not refer to the given mob, "" is returned.  
	 * @see AbilityComponents#getAbilityComponentDesc(MOB, List)
	 * @see AbilityComponents#getAbilityComponentDesc(MOB, String)
	 * @param mob the mob to check this components applicability to.
	 * @param comp the complete ability component decoded
	 * @param useConnector true to use a connector AND/OR, false otherwise
	 * @return a friendly readable description of a specific component
	 */
	public String getAbilityComponentDesc(MOB mob, AbilityComponent comp, boolean useConnector);
	
	/**
	 * Returns the master ability component map, keyed by the Ability ID.
	 * @return the master ability component map, keyed by the Ability ID.
	 */
	public Map<String, List<AbilityComponent>> getAbilityComponentMap();
	
	/**
	 * Adds a new coded ability component to the given component map.
	 * The component is coded as found in components.txt, with ID=parms.
	 * @param s the new coded ability component string 
	 * @param H the map to add the new component to
	 * @return an error string, or null if everything went well.
	 */
	public String addAbilityComponent(String s, Map<String, List<AbilityComponent>> H);
	
	/**
	 * Gets the decoded ability component definition for a given Ability ID().
	 * This is then used by other methods to determine whether a user has the
	 * necessary components, or for manipulation of the definition.
	 * @param AID the Ability ID()
	 * @return the decoded ability component definition for a given Ability ID()
	 */
	public List<AbilityComponent> getAbilityComponents(String AID);
	
	/**
	 * Breaks an ability component decoded objects into a series of key/value pairs,
	 * where the first is always the connector, and the keys are as follows:
	 * ANDOR, DISPOSITION, FATE, AMOUNT, COMPONENTID, MASK. In that order.
	 * This is primarily for simplifying editors.
	 * @see AbilityComponents#setAbilityComponentCodedFromCodedPairs(PairList, AbilityComponent)
	 * @param comp the decoded ability component to produce fields from
	 * @return the key/value pairs of the ability component values.
	 */
	public PairList<String, String> getAbilityComponentCoded(AbilityComponent comp);
	
	/**
	 * Copies the key/value pairs from a PairList of specific abilitycomponent fields
	 * into the given AbilityComponent object.	 The first pairlist entry is always the 
	 * connector, and the keys are as follows: ANDOR, DISPOSITION, FATE, AMOUNT, 
	 * COMPONENTID, MASK. In that order.
	 * This is primarily for simplifying editors.
	 * @see AbilityComponents#getAbilityComponentCoded(AbilityComponent)
	 * @param decodedDV  the key/value pairs of the ability component values.
	 * @param comp the decoded ability component to copy field data into 
	 */
	public void setAbilityComponentCodedFromCodedPairs(PairList<String, String> decodedDV, AbilityComponent comp);
	
	/**
	 * Reconstructs the coded ability component definition string (ID=parms)
	 * from the internal cached structures, given a particular Ability ID.
	 * @see AbilityComponents#getAbilityComponentCodedString(List)
	 * @param AID the Ability ID()
	 * @return the coded ability component definition string (ID=parms)
	 */
	public String getAbilityComponentCodedString(String AID);
	
	/**
	 * Reconstructs the coded ability component definition string (ID=parms)
	 * from the given cached decoded structures list.
	 * @see AbilityComponents#getAbilityComponentCodedString(String)
	 * @param comps the decoded ability components definition list
	 * @return the coded ability component definition string (ID=parms)
	 */
	public String getAbilityComponentCodedString(List<AbilityComponent> comps);
	
	/**
	 * Creates a new blank ability component object
	 * @return a new blank ability component object
	 */
	public AbilityComponent createBlankAbilityComponent();
	
	/**
	 * Alters and saved the ability components definition to on the
	 * filesystem (components.txt).  
	 * @param compID the ID of the component being altered
	 * @param delete true to delete, false to add or modify
	 */
	public void alterAbilityComponentFile(String compID, boolean delete);
	
	/**
	 * Returns the character-class based common skill ability limits
	 * object applicable to the given mob, or zeroes if there's
	 * a problem.
	 * @see AbilityComponents#getSpecialSkillLimit(MOB, Ability)
	 * @see AbilityComponents#getSpecialSkillRemainder(MOB, Ability)
	 * @see AbilityComponents#getSpecialSkillRemainders(MOB)
	 * @see AbilityLimits
	 * @param studentM the mob to find limits for
	 * @return the character-class based common skill ability limits
	 */
	public AbilityLimits getSpecialSkillLimit(MOB studentM);
	
	/**
	 * Returns the character-class based common skill ability limits
	 * object applicable to the given mob and the given ability.
	 * @see AbilityComponents#getSpecialSkillLimit(MOB)
	 * @see AbilityComponents#getSpecialSkillRemainder(MOB, Ability)
	 * @see AbilityComponents#getSpecialSkillRemainders(MOB)
	 * @see AbilityLimits
	 * @see AbilityLimits#specificSkillLimit()
	 * @param studentM the mob to find limits for
	 * @param A the ability object to find limits for
	 * @return the character-class based common skill ability limits
	 */
	public AbilityLimits getSpecialSkillLimit(MOB studentM, Ability A);
	
	/**
	 * Returns the character-class based common skill ability limits
	 * object applicable to the given mob and the given ability, and
	 * then subtracts the number of each common skill already learned
	 * to derive a remaining number of each type.
	 * @see AbilityComponents#getSpecialSkillLimit(MOB, Ability)
	 * @see AbilityComponents#getSpecialSkillLimit(MOB)
	 * @see AbilityComponents#getSpecialSkillRemainders(MOB)
	 * @see AbilityLimits
	 * @see AbilityLimits#specificSkillLimit()
	 * @param studentM the mob to find limits for
	 * @param A the ability object to find limits for
	 * @return the character-class based common skill ability remainders
	 */
	public AbilityLimits getSpecialSkillRemainder(MOB studentM, Ability A);
	
	/**
	 * Returns the character-class based common skill ability limits
	 * object applicable to the given mob, and
	 * then subtracts the number of each common skill already learned
	 * to derive a remaining number of each type.
	 * @see AbilityComponents#getSpecialSkillLimit(MOB, Ability)
	 * @see AbilityComponents#getSpecialSkillLimit(MOB)
	 * @see AbilityComponents#getSpecialSkillRemainder(MOB, Ability)
	 * @see AbilityLimits
	 * @param studentM the mob to find limits for
	 * @return the character-class based common skill ability limits
	 */
	public AbilityLimits getSpecialSkillRemainders(MOB studentM);
	
	/**
	 * Ability Limits object, denoting how many of different types
	 * of common skills and langs that a player can learn, including an
	 * entry for a specific skill.
	 * @author Bo Zimmerman
	 */
	public static interface AbilityLimits
	{
		/**
		 * Returns number of common skills
		 * @return number of common skills
		 */
		public int commonSkills();

		/**
		 * Returns max number of common skills
		 * @return max number of common skills
		 */
		public int maxCommonSkills();
		
		/**
		 * Sets number of common skills
		 * @param newVal number of common skills
		 * @return this
		 */
		public AbilityLimits commonSkills(int newVal);

		/**
		 * Returns number of crafting skills
		 * @return number of crafting skills
		 */
		public int craftingSkills();

		/**
		 * Returns max number of crafting skills
		 * @return max number of crafting skills
		 */
		public int maxCraftingSkills();

		/**
		 * Sets number of crafting skills
		 * @param newVal number of crafting skills
		 * @return this
		 */
		public AbilityLimits craftingSkills(int newVal);

		/**
		 * Returns number of non-crafting skills
		 * @return number of non-crafting skills
		 */
		public int nonCraftingSkills();

		/**
		 * Returns max number of non-crafting skills
		 * @return max number of non-crafting skills
		 */
		public int maxNonCraftingSkills();

		/**
		 * Sets number of non-crafting skills
		 * @param newVal number of non-crafting skills
		 * @return this
		 */
		public AbilityLimits nonCraftingSkills(int newVal);

		/**
		 * Returns max number of language skills
		 * @return max number of language skills
		 */
		public int maxLanguageSkills();

		/**
		 * Sets number of language skills
		 * @param newVal number of language skills
		 * @return this
		 */
		public AbilityLimits languageSkills(int newVal);

		/**
		 * Returns number of language skills
		 * @return number of language skills
		 */
		public int languageSkills();

		/**
		 * Returns number of given specific ability type 
		 * limit.
		 * @return i don't know how to say it
		 */
		public int specificSkillLimit();

		/**
		 * Sets number of given specific ability type 
		 * limit.
		 * @param newVal a new number
		 * @return this
		 */
		public AbilityLimits specificSkillLimit(int newVal);
	}
}
