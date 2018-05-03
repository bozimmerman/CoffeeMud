package com.planet_ink.coffee_mud.Abilities.interfaces;
import java.lang.reflect.Method;

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
   Copyright 2003-2018 Bo Zimmerman

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
 * DiseaseAffect is an ability interface to denote those properties,
 * affects, or Disease objects that act as curable physical diseases.
 */
public interface DiseaseAffect extends HealthCondition
{
	/** denotes a diseases spread by sex with someone who is infected. @see Ability#abilityCode() */
	public final static int SPREAD_STD=1;
	/** denotes a diseases spread by touching or making contact with someone who is infected. @see Ability#abilityCode() */
	public final static int SPREAD_CONTACT=2;
	/** denotes a diseases spread by being in the same room as someone who is infected. @see Ability#abilityCode() */
	public final static int SPREAD_PROXIMITY=4;
	/** denotes a diseases spread by eating the remains of someone who is infected. @see Ability#abilityCode() */
	public final static int SPREAD_CONSUMPTION=8;
	/** denotes a diseases spread by taking physical damage from someone who is infected. @see Ability#abilityCode() */
	public final static int SPREAD_DAMAGE=16;

	/**
	 * Descriptions of the SPREAD_ constants
	 */
	public final static String[] SPREAD_DESCS = {
		"sexual contact", "direct contact", "proximity", "ingestion", "blood contact"
	};

	/**
	 * This method returns the level from 0-9 of how difficult it
	 * is to cure this disease through mundane or magical means.
	 * 9 is considered more difficult.
	 *
	 * @return the curing difficulty level 0-9
	 */
	public int difficultyLevel();

	/**
	 * This method returns a bitmap constant denoting how the
	 * disease is spread.
	 * @see DiseaseAffect#SPREAD_CONSUMPTION
	 * @return the bitmap denoting how spread
	 */
	public int spreadBitmap();
	
	/**
	 * This method returns whether this disease, specifically it's spreading,
	 * is a malicious act.  Usually that's a NO, but sometimes...
	 * @return true if its malicious, false otherwise
	 */
	public boolean isMalicious();
}
