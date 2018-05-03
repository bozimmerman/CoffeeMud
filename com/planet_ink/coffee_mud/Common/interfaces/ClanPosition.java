package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.*;

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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/*
   Copyright 2011-2018 Bo Zimmerman

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
 * A class for the characteristics of a position within a
 * clan government.
 *
 * @author Bo Zimmermanimmerman
 */
public interface ClanPosition extends Modifiable, CMCommon
{

	/**
	 * Gets the iD.
	 *
	 * @see ClanPosition#setID(String)
	 * @return the iD
	 */
	public String getID();

	/**
	 * Sets the iD.
	 *
	 * @see ClanPosition#getID()
	 * @param iD the new iD
	 */
	public void setID(String iD);

	/**
	 * Gets the role id.
	 *
	 * @see ClanPosition#setRoleID(int)
	 * @return the role id
	 */
	public int getRoleID();

	/**
	 * Sets the role id.
	 *
	 * @see ClanPosition#getRoleID()
	 * @param roleID the new role id
	 */
	public void setRoleID(int roleID);

	/**
	 * Gets the rank.
	 *
	 * @see ClanPosition#setRank(int)
	 * @return the rank
	 */
	public int getRank();

	/**
	 * Sets the rank.
	 *
	 * @see ClanPosition#getRank()
	 * @param rank the new rank
	 */
	public void setRank(int rank);

	/**
	 * Gets the name.
	 *
	 * @see ClanPosition#setName(String)
	 * @return the name
	 */
	public String getName();

	/**
	 * Sets the name.
	 *
	 * @see ClanPosition#getName()
	 * @param name the new name
	 */
	public void setName(String name);

	/**
	 * Gets the plural name.
	 *
	 * @see ClanPosition#setPluralName(String)
	 * @return the plural name
	 */
	public String getPluralName();

	/**
	 * Sets the plural name.
	 *
	 * @see ClanPosition#getPluralName()
	 * @param pluralName the new plural name
	 */
	public void setPluralName(String pluralName);

	/**
	 * Gets the max.
	 *
	 * @see ClanPosition#setMax(int)
	 * @return the max
	 */
	public int getMax();

	/**
	 * Sets the max.
	 *
	 * @see ClanPosition#getMax()
	 * @param max the new max
	 */
	public void setMax(int max);

	/**
	 * Gets the inner mask str.
	 *
	 * @see ClanPosition#setInnerMaskStr(String)
	 * @return the inner mask str
	 */
	public String getInnerMaskStr();

	/**
	 * Sets the inner mask str.
	 *
	 * @see ClanPosition#getInnerMaskStr()
	 * @param innerMaskStr the new inner mask str
	 */
	public void setInnerMaskStr(String innerMaskStr);

	/**
	 * Checks if is public.
	 *
	 * @see ClanPosition#setPublic(boolean)
	 * @return true, if is public
	 */
	public boolean isPublic();

	/**
	 * Sets the public.
	 *
	 * @see ClanPosition#isPublic()
	 * @param isPublic the new public
	 */
	public void setPublic(boolean isPublic);

	/**
	 * Gets the function chart.
	 *
	 * @see ClanPosition#setFunctionChart(com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority[])
	 * @return the function chart
	 */
	public Clan.Authority[] getFunctionChart();

	/**
	 * Sets the function chart.
	 *
	 * @see ClanPosition#getFunctionChart()
	 * @param functionChart the new function chart
	 */
	public void setFunctionChart(Clan.Authority[] functionChart);
}

