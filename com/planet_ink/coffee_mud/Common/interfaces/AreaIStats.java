package com.planet_ink.coffee_mud.Common.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2025 Bo Zimmerman

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
 * An interface for a area statistics, and being able to build
 * them in different circumstances.
 */
public interface AreaIStats extends CMCommon
{
	/**
	 * Build the stats
	 * @param A the area to make stats for
	 * @return the stat object
	 */
	public AreaIStats build(Area A);

	/**
	 * Returns whether these stats are done being calculated
	 * @return true if they are ready to read.
	 */
	public boolean isFinished();

	/**
	 * Return the finished stat data
	 * @param stat which stat to return
	 * @return the finished stats
	 */
	public int getStat(Area.Stats stat);


	/**
	 * Sets a finished stat value
	 * @param stat which stat to set
	 * @param val the stat value
	 */
	public void setStat(Area.Stats stat, int val);

	/**
	 * Returns the most common race in the area.
	 * This might be null if one could not be determined.
	 *
	 * @return the most common race in the area.
	 */
	public Race getCommonRace();

}
