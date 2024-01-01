package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.collections.Triad;
import com.planet_ink.coffee_mud.core.interfaces.CostDef.CostType;
/*
Copyright 2006-2024 Bo Zimmerman

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
 * Class for the cost of a skill, or similar things perhaps
 * @author Bo Zimmerman
 */
public interface CostManager
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
	public void doSpend(final MOB student);
}
