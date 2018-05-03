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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2006-2018 Bo Zimmerman

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
public interface ExpertiseLibrary extends CMLibrary
{
	public enum Flag
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
	
	public interface ExpertiseDefinition extends CMObject
	{
		public String getBaseName();
		
		public void setBaseName(String baseName);
		
		public void setName(String name);
		
		public void setID(String ID);
		
		public void setData(String[] data);
		
		public ExpertiseDefinition getParent();
		
		public int getMinimumLevel();
		
		public String[] getData();
		
		public MaskingLibrary.CompiledZMask compiledListMask();
		
		public MaskingLibrary.CompiledZMask compiledFinalMask();
		
		public String allRequirements();
		
		public String listRequirements();

		public String finalRequirements();

		public void addListMask(String mask);

		public void addFinalMask(String mask);

		public void addCost(CostType type, Double value);
		
		public String costDescription();

		public boolean meetsCostRequirements(MOB mob);

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
		public CostType type();
		
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

	public ExpertiseDefinition addDefinition(String ID, String name, String baseName, String listMask, String finalMask, String[] costs, String[] data);
	public void delDefinition(String ID);
	public ExpertiseDefinition getDefinition(String ID);
	public ExpertiseDefinition findDefinition(String ID, boolean exactOnly);
	public Enumeration<ExpertiseDefinition> definitions();
	public List<ExpertiseDefinition> myQualifiedExpertises(MOB mob);
	public List<ExpertiseDefinition> myListableExpertises(MOB mob);
	public int numExpertises();
	public SkillCost createNewSkillCost(CostType costType, Double value);
	public void recompileExpertises();
	public int getExpertiseLevel(final MOB mob, final String abilityID, final ExpertiseLibrary.Flag code);
	public String getExpertiseHelp(String ID, boolean exact);
	public String getApplicableExpertise(String ID, Flag code);
	public int getApplicableExpertiseLevel(String ID, Flag code, MOB mob);
	public int getStages(String baseExpertiseCode);
	public List<String> getStageCodes(String baseExpertiseCode);
	public String confirmExpertiseLine(String row, String ID, boolean addIfPossible);
	public List<String> getPeerStageCodes(final String expertiseCode);
	public String getGuessedBaseExpertiseName(final String expertiseCode);
	public void handleBeingTaught(MOB teacher, MOB student, Environmental item, String msg);
	public boolean canBeTaught(MOB teacher, MOB student, Environmental item, String msg);
	public boolean postTeach(MOB teacher, MOB student, CMObject teachObj);
	public Iterator<String> filterUniqueExpertiseIDList(Iterator<String> i);
	public int getHighestListableStageBySkill(final MOB mob, String ableID, ExpertiseLibrary.Flag flag);
}
