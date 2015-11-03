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
   Copyright 2015-2015 Bo Zimmerman

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
	 * @return null if missing components, or the list of found components
	 */
	public List<Object> componentCheck(MOB mob, List<AbilityComponent> req);
	
	/**
	 * If the ability component recipe used to build the list of found
	 * components needed to use a skill requires that any of the componenets
	 * are destroyed.
	 * @see AbilityComponents#componentCheck(MOB, List)
	 * @param found the components found with componentCheck
	 * @return the value of the components destroyed
	 */
	public int destroyAbilityComponents(List<Object> found);
	
	/**
	 * Returns a friendly readable form of the component requirements
	 * of the given Ability/Skill ID(), or null if that ability has
	 * no requirements.  Since requirements may differ by player
	 * mask, the player mob is also required.
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
	 * @param mob the player mob who wants to know
	 * @param req the coded requirements list
	 * @return a friendly readable form of the component requirements
	 */
	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req);
	public Map<String, List<AbilityComponent>> getAbilityComponentMap();
	public String addAbilityComponent(String s, Map<String, List<AbilityComponent>> H);
	public String getAbilityComponentCodedString(String AID);
	public List<AbilityComponent> getAbilityComponents(String AID);
	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req, int r);
	public void setAbilityComponentCodedFromCodedPairs(PairList<String,String> decodedDV, AbilityComponent comp);
	public PairList<String,String> getAbilityComponentCoded(AbilityComponent comp);
	public String getAbilityComponentCodedString(List<AbilityComponent> comps);
	public AbilityComponent createBlankAbilityComponent();
	public void alterAbilityComponentFile(String compID, boolean delete);
	
	public AbilityLimits getCommonSkillLimit(MOB studentM);
	public AbilityLimits getCommonSkillLimit(MOB studentM, Ability A);
	public AbilityLimits getCommonSkillRemainder(MOB studentM, Ability A);
	public AbilityLimits getCommonSkillRemainders(MOB student);
	
	public static interface AbilityLimits
	{
		public int commonSkills();

		public AbilityLimits commonSkills(int newVal);

		public int craftingSkills();

		public AbilityLimits craftingSkills(int newVal);

		public int nonCraftingSkills();

		public AbilityLimits nonCraftingSkills(int newVal);

		public int specificSkillLimit();

		public AbilityLimits specificSkillLimit(int newVal);
	}
}
