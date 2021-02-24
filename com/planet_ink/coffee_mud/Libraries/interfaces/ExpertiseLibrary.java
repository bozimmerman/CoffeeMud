package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2006-2021 Bo Zimmerman

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
 * The ExpertiseLibrary manages a list of optional, purchaseable player attributes, called expertises.
 * Each expertise applies to a certain group of skills, and will modify the behavior or cost
 * of those skills by the expertise type.  Types of expertise include those affecting the level of
 * the skill, its mana cost, its duration or time, and other miscellaneous scorings.
 *
 * @author Bo Zimmerman
 *
 */
public interface ExpertiseLibrary extends CMLibrary
{
	public ExpertiseDefinition addDefinition(String ID, String name, String baseName, String listMask, String finalMask, String[] costs, String[] data);
	public void delDefinition(String ID);
	public ExpertiseDefinition getDefinition(String ID);
	public ExpertiseDefinition findDefinition(String ID, boolean exactOnly);
	public Enumeration<ExpertiseDefinition> definitions();
	public List<ExpertiseDefinition> myQualifiedExpertises(MOB mob);
	public List<ExpertiseDefinition> myListableExpertises(MOB mob);
	public ExpertiseDefinition getConfirmedDefinition(final MOB mob, final String ID);
	public int numExpertises();
	public SkillCost createNewSkillCost(CostType costType, Double value);
	public void recompileExpertises();
	public int getExpertiseLevel(final MOB mob, final String abilityID, final ExpertiseLibrary.XType code);
	public String getExpertiseHelp(String ID, boolean exact);
	public String getApplicableExpertise(String ID, XType code);
	public String[] getApplicableExpertises(String ID, XType code);
	public int getApplicableExpertiseLevel(String ID, XType code, MOB mob);
	public int getStages(String baseExpertiseCode);
	public List<String> getStageCodes(String baseExpertiseCode);
	public String confirmExpertiseLine(String row, String ID, boolean addIfPossible);
	public List<String> getPeerStageCodes(final String expertiseCode);
	public String getGuessedBaseExpertiseName(final String expertiseCode);
	public void handleBeingTaught(MOB teacher, MOB student, Environmental item, String msg, int add);
	public boolean canBeTaught(MOB teacher, MOB student, Environmental item, String msg);
	public boolean confirmAndTeach(final MOB teacherM, final MOB studentM, final CMObject teachableO, final Runnable callBack);
	public boolean postTeach(MOB teacher, MOB student, CMObject teachObj);
	public Iterator<String> filterUniqueExpertiseIDList(Iterator<String> i);
	public int getHighestListableStageBySkill(final MOB mob, String ableID, ExpertiseLibrary.XType flag);

	/**
	 * The Expertise Type num, which describes what skill aspect that this expertise
	 * will modify
	 * @author Bo Zimmerman
	 *
	 */
	public enum XType
	{
		X1,
		X2,
		X3,
		X4,
		X5,
		LEVEL,
		TIME,
		MAXRANGE,
		LOWCOST,
		XPCOST,
		LOWFREECOST
	}

	/**
	 * Class definiing a single level of a particular expertise, along
	 * with all requirements to gain it.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public interface ExpertiseDefinition extends CMObject
	{
		/**
		 * Gets the base (non-leveled) name of this expertise, for
		 * association with its other-leveled brethren.
		 * @see ExpertiseLibrary.ExpertiseDefinition#setBaseName(String)
		 * @return the base name of this expertise type
		 */
		public String getBaseName();

		/**
		 * Sets the base (non-leveled) name of this expertise, for
		 * association with its other-leveled brethren.
		 * @see ExpertiseLibrary.ExpertiseDefinition#getBaseName()
		 * @param baseName the base name of this expertise type
		 */
		public void setBaseName(String baseName);

		/**
		 * Sets the friendly name of this expertise, including its
		 * stage/level number.  e.g. EXPERTISE8
		 * @param name the friendly name
		 */
		public void setName(String name);

		/**
		 * Sets the code name of this expertise, including its
		 * stage/level number.  e.g. EXPERTISE8
		 * @param ID the code name of this expertise
		 */
		public void setID(String ID);

		/**
		 * If this expertise supports naming _DATA for each stage/level, this
		 * will set an array of the names of each stage/level for easy
		 * lookup.
		 * @see ExpertiseDefinition#getStageNames()
		 * @param data an array of the names
		 */
		public void setStageNames(String[] data);

		/**
		 * If this is a later stage expertise definition, this
		 * method returns the parent/base definition.
		 * @return the parent/base definition.
		 */
		public ExpertiseDefinition getParent();

		/**
		 * Returns the minimum player level required to learn this
		 * expertise.
		 * @return the minimum player level required
		 */
		public int getMinimumLevel();

		/**
		 * If this expertise supports naming _DATA for each stage/level, this
		 * will return an array of the names of each stage/level for easy
		 * lookup.
		 * @see ExpertiseDefinition#setStageNames(String[])
		 * @return the stage names
		 */
		public String[] getStageNames();

		/**
		 * Gets a ZapperMask describing who may know about the availability of this
		 * expertise.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return a ZapperMask describing who may know about the availability of this
		 */
		public MaskingLibrary.CompiledZMask compiledListMask();

		/**
		 * Gets a ZapperMask describing who may acquire this
		 * expertise through training.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return a ZapperMask describing who acquire this
		 */
		public MaskingLibrary.CompiledZMask compiledFinalMask();

		/**
		 * Gets a ZapperMask describing who may know about the availability of this
		 * expertise AND acquire it.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return a ZapperMask describing access to this expertise
		 */
		public String allRequirements();

		/**
		 * Gets a ZapperMask describing who may know about the availability of this
		 * expertise.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return a ZapperMask describing who may know about the availability of this
		 */
		public String listRequirements();

		/**
		 * Gets a ZapperMask describing who may acquire this
		 * expertise through training.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @return a ZapperMask describing who may acquire this
		 */
		public String finalRequirements();

		/**
		 * Adds a ZapperMask describing who may know about the availability of this
		 * expertise.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @param mask a ZapperMask describing who may know about the availability of this
		 */
		public void addListMask(String mask);

		/**
		 * Adds a ZapperMask describing who may acquire this
		 * expertise through training.
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
		 * @param mask a ZapperMask describing who may acquire this
		 */
		public void addFinalMask(String mask);

		/**
		 * Adds another resource cost to acquire this expertise.
		 * @param type the type of cost
		 * @param value the amount of the resource required
		 */
		public void addCost(CostType type, Double value);

		/**
		 * Returns a friendly description of this expertise cost
		 * @return a friendly description of this expertise cost
		 */
		public String costDescription();

		/**
		 * Returns whether the given mob has enough resources
		 * to cover the costs described by this expertise.
		 * @param mob the mob to lose resources
		 * @return true if the mob can meet the costs
		 */
		public boolean meetsCostRequirements(MOB mob);

		/**
		 * Subtracts the costs described by this class from
		 * the given mob
		 * @param mob the mob to lose resources
		 */
		public void spendCostRequirements(MOB mob);

	}

	/** Enumeration of the types of costs of gaining this ability */
	public enum CostType
	{
		TRAIN,
		PRACTICE,
		XP,
		GOLD,
		QP;
	}

	/**
	 * Class for the definition of the cost of a skill
	 * @author Bo Zimmerman
	 */
	public interface SkillCostDefinition
	{
		/**
		 * Returns the type of resources defining the cost
		 * of a skill.
		 * @see ExpertiseLibrary.CostType
		 * @return the type of cost
		 */
		public CostType type();

		/**
		 * A math formula definition the amount of the cost
		 * type required, where at-x1 is the qualifying level
		 * and at-x2 is the player level
		 *
		 * @return the amount formula
		 */
		public String costDefinition();
	}

	/**
	 * Class for the cost of a skill, or similar things perhaps
	 * @author Bo Zimmerman
	 */
	public interface SkillCost
	{
		/**
		 * Returns a simple description of the Type of
		 * this cost.  A MOB and sample value is required for
		 * money currencies.
		 * @param mob MOB, for GOLD type currency eval
		 * @return the type of currency
		 */
		public String costType(final MOB mob);

		/**
		 * Returns a simple description of the amount
		 * and type of cost required for the skill.
		 * @param mob the creature, for evaluating currency
		 * @return a description of the required amount
		 */
		public String requirements(final MOB mob);

		/**
		 * Returns whether the given mob meets the given cost requirements.
		 * @param student the student to check
		 * @return true if it meets, false otherwise
		 */
		public boolean doesMeetCostRequirements(final MOB student);

		/**
		 * Expends the given cost upon the given student
		 * @param student the student to check
		 */
		public void spendSkillCost(final MOB student);
	}
}
