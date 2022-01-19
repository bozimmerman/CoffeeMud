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
   Copyright 2006-2022 Bo Zimmerman

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
	/**
	 * Returns the number of expertise definitions, which
	 * includes all stages for each expertise type.
	 * @see ExpertiseLibrary#delDefinition(String, boolean)
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#numStages(String)
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#definitions()
	 *
	 * @return the number of all expertise stages
	 */
	public int numExpertises();

	/**
	 * Adds a temporary or permanent expertise and all of its stage
	 * definitions from a coded string.  The coded string format is
	 * as defined in the expertises.txt file.
	 * @see ExpertiseLibrary#delDefinition(String, boolean)
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#numStages(String)
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#definitions()
	 * @see ExpertiseLibrary#createNewSkillCost(CostType, Double)
	 * @see ExpertiseLibrary#confirmExpertiseLine(String, String, boolean)
	 *
	 * @param codedLine the coded expertise line
	 * @param andSave true to save, false to leave in ram
	 * @return true if the expertise stages were added
	 */
	public boolean addModifyDefinition(final String codedLine, final boolean andSave);

	/**
	 * Can delete either a single definition stage, or all stages of a expertise,
	 * and optionally save.
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#numStages(String)
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#definitions()
	 *
	 * @param ID either a fully qualified, or base expertise name
	 * @param andSave true to save, false to just change ram
	 * @return true if a delete happened, false otherwise
	 */
	public boolean delDefinition(String ID, final boolean andSave);

	/**
	 * Returns a single expertise stage definition
	 * @see ExpertiseLibrary#delDefinition(String, boolean)
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#numStages(String)
	 * @see ExpertiseLibrary#definitions()
	 *
	 * @param ID a full expertise id, including stage
	 * @return the corresponding expertise definition
	 */
	public ExpertiseDefinition getDefinition(String ID);


	/**
	 * Find an expertise stage definition by name or id
	 * @see ExpertiseLibrary#delDefinition(String, boolean)
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#definitions()
	 *
	 * @param ID search string
	 * @param exactOnly true for only exact matches, false to be looser
	 * @return the found definition, or null
	 */
	public ExpertiseDefinition findDefinition(String ID, boolean exactOnly);

	/**
	 * Returns an enumeration of all expertise stage definitions
	 * @see ExpertiseLibrary#delDefinition(String, boolean)
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#findDefinition(String, boolean)
	 *
	 * @return all the definitions by stage
	 */
	public Enumeration<ExpertiseDefinition> definitions();
	/**
	 * Re-loads all expertise definitions from the file
	 * /resources/expertises.txt (.1, .2, etc)
	 *
	 * @see ExpertiseLibrary#delDefinition(String, boolean)
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#findDefinition(String, boolean)
	 * @see ExpertiseLibrary#definitions()
	 */
	public void recompileExpertises();

	/**
	 * Returns the instructions for the coded line version of
	 * base expertise definitions.
	 *
	 * @return the instructions from expertises.txt
	 */
	public String getExpertiseInstructions();

	/**
	 * Gets the help/info about a specific expertise stage.
	 *
	 * @param ID the expertise stage id, or search string
	 * @param exact true to return exact match, or false for the first
	 * @return help/info text for the stage
	 */
	public String getExpertiseHelp(String ID, boolean exact);

	/**
	 * Returns the number of stages of the given base
	 * expertise ID.
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#getStageCodes(String)
	 * @see ExpertiseLibrary#getDefinition(String)
	 *
	 * @param baseExpertiseCode full base expertise code
	 * @return the number of stages in the expertise
	 */
	public int numStages(String baseExpertiseCode);

	/**
	 * Returns the individual expertise stage ids for
	 * all stages in a given base expertise id.
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#numStages(String)
	 * @see ExpertiseLibrary#getDefinition(String)
	 *
	 * @param baseExpertiseCode the base expertise id code
	 * @return the expertise stage id codes
	 */
	public List<String> getStageCodes(String baseExpertiseCode);

	/**
	 * Returns the other individual expertise stage ids for
	 * all stages in the base expertise as the given full
	 * stage expertise id.
	 * @see ExpertiseLibrary#numExpertises()
	 * @see ExpertiseLibrary#numStages(String)
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#getStageCodes(String)
	 *
	 * @param expertiseCode the full stage expertise id code
	 * @return the other expertise stage id codes
	 */
	public List<String> getPeerStageCodes(final String expertiseCode);

	/**
	 * Validate the syntax of a coded expertise definition line,
	 * returning a readable error if found.
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#createNewSkillCost(CostType, Double)
	 *
	 * @param row the coded definition
	 * @param ID the expertise definition id code
	 * @param addIfPossible true to cache, false to not
	 * @return an error message, or null if all is OK
	 */
	public String confirmExpertiseLine(String row, String ID, boolean addIfPossible);

	/**
	 * Creates a new SkillCost object reflecting the given type and value.
	 * This is part of the expertise defining process.
	 * @see ExpertiseLibrary#addModifyDefinition(String, boolean)
	 * @see ExpertiseLibrary#confirmExpertiseLine(String, String, boolean)
	 *
	 * @param costType the type of code
	 * @param value the amount of the type
	 * @return the SkillCost object
	 */
	public SkillCost createNewSkillCost(CostType costType, Double value);

	/**
	 * Given a mob and expertise id, this method will return the definition for the
	 * expertise that the mob actually has, if it is the same.
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#myListableExpertises(MOB)
	 * @see ExpertiseLibrary#myQualifiedExpertises(MOB)
	 * @see ExpertiseLibrary#getExpertiseLevelCached(MOB, String, XType)
	 * @see ExpertiseLibrary#getExpertiseLevelCalced(MOB, String, XType)
	 *
	 * @param mob the mob/player to check
	 * @param ID the expertise stage id
	 * @return the definition that applies, or null
	 */
	public ExpertiseDefinition getConfirmedDefinition(final MOB mob, final String ID);

	/**
	 * Returns all the expertises that the given mob qualifies for.
	 *
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#myListableExpertises(MOB)
	 * @see ExpertiseLibrary#getConfirmedDefinition(MOB, String)
	 * @see ExpertiseLibrary#getExpertiseLevelCached(MOB, String, XType)
	 * @see ExpertiseLibrary#getExpertiseLevelCalced(MOB, String, XType)
	 * @see ExpertiseLibrary#getHighestListableStageBySkill(MOB, String, XType)
	 *
	 * @param mob the mob to check
	 * @return the definitions qualified for
	 */
	public List<ExpertiseDefinition> myQualifiedExpertises(MOB mob);

	/**
	 * Returns all the expertises that the given mob can list
	 * for possible training.
	 *
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#myQualifiedExpertises(MOB)
	 * @see ExpertiseLibrary#getConfirmedDefinition(MOB, String)
	 * @see ExpertiseLibrary#getExpertiseLevelCached(MOB, String, XType)
	 * @see ExpertiseLibrary#getExpertiseLevelCalced(MOB, String, XType)
	 * @see ExpertiseLibrary#getHighestListableStageBySkill(MOB, String, XType)
	 *
	 * @param mob the mob to check
	 * @return the definitions qualified for
	 */
	public List<ExpertiseDefinition> myListableExpertises(MOB mob);

	/**
	 * If the given mob has an expertise that applies the given
	 * expertise type to the given ability id, then this method will
	 * return how many levels of that expertise the mob has, or 0
	 * if none.
	 * This method employs the mob-specific cache to prevent
	 * constant recalculation.
	 *
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#myListableExpertises(MOB)
	 * @see ExpertiseLibrary#myQualifiedExpertises(MOB)
	 * @see ExpertiseLibrary#getConfirmedDefinition(MOB, String)
	 * @see ExpertiseLibrary#getExpertiseLevelCalced(MOB, String, XType)
	 *
	 * @param mob the player to check
	 * @param abilityID the ability to check
	 * @param code the expertise type to check
	 * @return the level of expertise that applies, or 0
	 */
	public int getExpertiseLevelCached(final MOB mob, final String abilityID, final XType code);


	/**
	 * If the given mob has an expertise that applies the given
	 * expertise type to the given ability id, then this method will
	 * return how many levels of that expertise the mob has, or 0
	 * if none.
	 * This method employs the mob-specific cache to prevent
	 * constant recalculation.
	 *
	 * @see ExpertiseLibrary#getDefinition(String)
	 * @see ExpertiseLibrary#myListableExpertises(MOB)
	 * @see ExpertiseLibrary#myQualifiedExpertises(MOB)
	 * @see ExpertiseLibrary#getConfirmedDefinition(MOB, String)
	 * @see ExpertiseLibrary#getExpertiseLevelCached(MOB, String, XType)
	 *
	 * @param mob the player to check
	 * @param abilityID the ability to check
	 * @param code the expertise type to check
	 * @return the level of expertise that applies, or 0
	 */
	public int getExpertiseLevelCalced(MOB mob, String abilityID, XType code);

	/**
	 * Finds the expertise that applies to the given mob and ability id and expertise
	 * type, and then returns the highest stage that is qualified for by the mob.
	 *
	 * @see ExpertiseLibrary#myListableExpertises(MOB)
	 * @see ExpertiseLibrary#myQualifiedExpertises(MOB)
	 * @see ExpertiseLibrary#getHighestListableStageBySkill(MOB, String, XType)
	 *
	 * @param mob the mob to check
	 * @param ableID the ability id
	 * @param flag the expertise type
	 * @return the highest listable stage or 0
	 */
	public int getHighestListableStageBySkill(final MOB mob, String ableID, ExpertiseLibrary.XType flag);

	/**
	 * Returns the base expertise id that applies to the given ability and expertise type
	 * @see ExpertiseLibrary#getApplicableExpertises(String, XType)
	 *
	 * @param abilityID ability ID
	 * @param code expertise type
	 * @return the expertise base id or null
	 */
	public String getApplicableExpertise(String abilityID, XType code);

	/**
	 * Returns all stage expertise ids that apply to the given ability and
	 * expertise type.
	 *
	 * @see ExpertiseLibrary#getApplicableExpertise(String, XType)
	 *
	 * @param abilityID ability ID
	 * @param code expertise type
	 * @return the expertise stage ids or null
	 */
	public String[] getApplicableExpertises(String abilityID, XType code);

	/**
	 * Handles the teaching of an Ability or ExpertiseDefinition, either given,
	 * or by parsing a coded Learn string from a message.
	 * @see ExpertiseLibrary#handleBeingTaught(MOB, MOB, Environmental, String, int)
	 * @see ExpertiseLibrary#canBeTaught(MOB, MOB, Environmental, String)
	 * @see ExpertiseLibrary#confirmAndTeach(MOB, MOB, CMObject, Runnable)
	 * @see ExpertiseLibrary#postTeach(MOB, MOB, CMObject)
	 *
	 * @param teacher the teacher mob
	 * @param student the student mob
	 * @param item the Ability or ExpertiseDefinition object
	 * @param msg an optional coded learn string
	 * @param add an amount of ability proficiency to add over default
	 */
	public void handleBeingTaught(MOB teacher, MOB student, Environmental item, String msg, int add);

	/**
	 * Given an Ability or ExpertiseDefinition, or msg containing a learn coded string,
	 * this will return whether the given teacher can teach, and the given student can
	 * learn the ability or expertise.
	 * @see ExpertiseLibrary#handleBeingTaught(MOB, MOB, Environmental, String, int)
	 * @see ExpertiseLibrary#confirmAndTeach(MOB, MOB, CMObject, Runnable)
	 * @see ExpertiseLibrary#postTeach(MOB, MOB, CMObject)
	 *
	 * @param teacher the teacher mob
	 * @param student the student mob
	 * @param item the Ability or ExpertiseDefinition object
	 * @param msg an optional coded learn string
	 * @return true if teaching is OK, false otherwise
	 */
	public boolean canBeTaught(MOB teacher, MOB student, Environmental item, String msg);

	/**
	 * Given a teacher, student, and either an Ability or Expertise, this method
	 * will confirm the lesson with the student, and call the Runnable if approval
	 * is given.
	 * @see ExpertiseLibrary#handleBeingTaught(MOB, MOB, Environmental, String, int)
	 * @see ExpertiseLibrary#canBeTaught(MOB, MOB, Environmental, String)
	 * @see ExpertiseLibrary#postTeach(MOB, MOB, CMObject)
	 *
	 * @param teacherM the teacher
	 * @param studentM the student
	 * @param teachableO the ability or expertise
	 * @param callBack the runnable to call on approval
	 * @return true if confirmation is proceeding
	 */
	public boolean confirmAndTeach(final MOB teacherM, final MOB studentM, final CMObject teachableO, final Runnable callBack);

	/**
	 * Posts a teaching message of the given Ability or ExpertiseDefinition
	 * for the given teacher and student.
	 * @see ExpertiseLibrary#handleBeingTaught(MOB, MOB, Environmental, String, int)
	 * @see ExpertiseLibrary#canBeTaught(MOB, MOB, Environmental, String)
	 * @see ExpertiseLibrary#confirmAndTeach(MOB, MOB, CMObject, Runnable)
	 *
	 * @param teacher the teacher mob
	 * @param student the student mob
	 * @param teachObj the ability or expertisedefinition
	 * @return true if the posting went ok, though not necessarily a completed learn
	 */
	public boolean postTeach(MOB teacher, MOB student, CMObject teachObj);

	/**
	 * Given an iterator of expertise stage ids, this will return an iterator
	 * of unique expertise stage ids, filtering out other later stages.
	 * @param i the id list to filter
	 * @return the filtered list
	 */
	public Iterator<String> filterUniqueExpertiseIDList(Iterator<String> i);

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

		/**
		 * Read/Writeable list of XType flags for this
		 * expertise.
		 * @return the xtype flag set
		 */
		public Set<XType> getFlagTypes();

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
