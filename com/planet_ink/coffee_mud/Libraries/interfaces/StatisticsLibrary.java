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
import com.planet_ink.coffee_mud.Libraries.CoffeeTables;
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/*
   Copyright 2005-2023 Bo Zimmerman

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
 * Library for updating player statistics and tables.
 * This really needs reading and management utilities added.
 *
 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow
 * @author Bo Zimmerman
 *
 */
public interface StatisticsLibrary extends CMLibrary
{
	/**
	 * Flush any cached statistics to the database.
	 */
	public void update();

	/**
	 * Add a new statistic by bumping its total.
	 * See all the STAT_ constants in CoffeeTableRow
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CoffeeTableRow#STAT_ARRESTS
	 *
	 * @param E the object that is having its stat bumped
	 * @param type the type of stat to bump
	 */
	public void bump(CMObject E, int type);

	/**
	 * Returns all of the statistics rows between the given dates, with
	 * the end date usually being 0 for some reason.
	 *
	 * @param startDate the start date in milliseconds
	 * @param endDate 0 for 'up to the present', or an end date in milliseconds
	 * @return a list of all relevant rows
	 */
	public List<CoffeeTableRow> readRawStats(long startDate, long endDate);
}
