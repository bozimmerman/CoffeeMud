package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.List;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2005-2024 Bo Zimmerman

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
 * A PrideStats object manages the Pride Stats / TOP player, account,
 * etc data for the system.
 *
 * These objects are managed by the PlayerManager Library
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary
 */
public interface PrideStats extends CMCommon, XMLConfigureable
{

	/**
	 * The recorded player and account statistics.
	 * @author Bo Zimmerman
	 *
	 */
	public enum PrideStat
	{
		PVPKILLS,
		AREAS_EXPLORED,
		ROOMS_EXPLORED,
		EXPERIENCE_GAINED,
		MINUTES_ON,
		QUESTS_COMPLETED,
		QUESTPOINTS_EARNED
	}

	/**
	 * Add to one of the pride stats for this player or account
	 * @see PrideStats.PrideStat
	 * @param stat which pride stat to add to
	 * @param amt the amount to add
	 */
	public void bumpPrideStat(PrideStats.PrideStat stat, int amt);

	/**
	 * Get one of the pride stats for this player or account
	 * @see PrideStats.PrideStat
	 * @param period the time period to get the number for
	 * @param stat which pride stat to get
	 * @return the pride stat value/count/whatever
	 */
	public int getPrideStat(TimeClock.TimePeriod period, PrideStats.PrideStat stat);
}
