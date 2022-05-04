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
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
/*
   Copyright 2005-2022 Bo Zimmerman

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
 * Help File system for online users, providing not only lookup for
 * named entries, but derived help data for various map and game
 * objects.
 *
 * @author Bo Zimmerman
 */
public interface HelpLibrary extends CMLibrary
{
	/**
	 * Returns a list of all help keys.
	 *
	 * @param archonHelp true to include archon keys, false otherwise
	 * @param standardHelp true to include normal keys, false otherwise
	 * @return the list of help keys
	 */
	public List<String> getTopics(boolean archonHelp, boolean standardHelp);

	/**
	 * Do post processing on the given help text, resolving tags and
	 * other derived data that needs insertion.
	 *
	 * @param tag the key
	 * @param str the help text to post-process
	 * @param forM the mob for whom the help is intended
	 * @return the final help text
	 */
	public String fixHelp(String tag, String str, MOB forM);

	/**
	 * Returns a viewable help entry for the given key.
	 *
	 * @see HelpLibrary#getArcHelpFile()
	 * @see HelpLibrary#getHelpFile()
	 *
	 * @param helpStr the key
	 * @param forM the mob to get info for
	 * @param favorAHelp true to search archon help first
	 * @return the help entry text
	 */
	public String getHelpText(String helpStr, MOB forM, boolean favorAHelp);

	/**
	 * Returns a viewable help entry for the given key.
	 *
	 * @see HelpLibrary#getArcHelpFile()
	 * @see HelpLibrary#getHelpFile()
	 *
	 * @param helpStr the key
	 * @param forM the mob to get info for
	 * @param favorAHelp true to search archon help first
	 * @param noFix true to skip post-processing, false otherwise
	 * @return the help entry text
	 */
	public String getHelpText(String helpStr, MOB forM, boolean favorAHelp, boolean noFix);

	/**
	 * Returns a viewable help entry for the given key, checking the given
	 * help file.
	 *
	 * @see HelpLibrary#getArcHelpFile()
	 * @see HelpLibrary#getHelpFile()
	 *
	 * @param helpStr the key
	 * @param rHelpFile the help file cache to use
	 * @param forM the mob to get info for
	 * @return the help entry text
	 */
	public String getHelpText(String helpStr, Properties rHelpFile, MOB forM);

	/**
	 * Returns a list of help entries containing the given string, as an aid
	 * in search.
	 *
	 * @see HelpLibrary#getArcHelpFile()
	 * @see HelpLibrary#getHelpFile()
	 *
	 * @param helpStr the string to search for
	 * @param rHelpFile1 the first help cache to prioritize
	 * @param rHelpFile2 null, or the second help cache to prioritize
	 * @param forM the mob for whom help entries are searched
	 * @return a friendly list of help entries in columns suitable for the given mob
	 */
	public String getHelpList(String helpStr,  Properties rHelpFile1, Properties rHelpFile2, MOB forM);


	/**
	 * Returns the cache for the archon help entries.
	 *
	 * @return the cache for the archon help entries.
	 */
	public Properties getArcHelpFile();

	/**
	 * Returns the cache for the main help entries.
	 *
	 * @return the cache for the main help entries.
	 */
	public Properties getHelpFile();


	/**
	 * Unloads the help file cache, forcing the cache to be repopulated from
	 * the filesystem on next access.
	 *
	 * @param mob null, or a mob to notify after the unload is completed.
	 */
	public void unloadHelpFile(MOB mob);

	/**
	 * Returns the official description of a skill proficiency value.
	 *
	 * @param proficiency the proficiency value 0-100
	 * @return the description for that proficiency
	 */
	public String getRPProficiencyStr(final int proficiency);

	/**
	 * Returns whether the given string resembles a key for
	 * a player qualifying skill, or if the entry has common
	 * skill help markers.
	 *
	 * @param helpStr the possible skill key
	 * @return true if it looks like a player skill, false otherwise
	 */
	public boolean isPlayerSkill(String helpStr);

	/**
	 * Adds a new entry to the cached help entries.  Saves nothing.
	 *
	 * @param ID the key of the help entry
	 * @param text the full text of the help entry
	 * @param archon true to add to archon ahelp entries, false for normal
	 */
	public void addHelpEntry(String ID, String text, boolean archon);

	/**
	 * Returns a description of the amount of a particular cost is required
	 * by the given mob to use the given ability.
	 *
	 * @see Ability#USAGE_DESCS
	 *
	 * @param A the Ability to find usage for
	 * @param whichUsageCode the Ability.USAGE_ code.
	 * @param forM null, or a mob to make the report more specific
	 * @return either the word "all" or a number in string form
	 */
	public String getActualAbilityUsageDesc(Ability A, int whichUsageCode, MOB forM);

	/**
	 * Returns a description of the valid targets of the given skill.
	 *
	 * @param A the Ability in question
	 * @return the quality description
	 */
	public String getAbilityTargetDesc(Ability A);

	/**
	 * Returns a description of the quality of the
	 * given skill.
	 *
	 * @param A the Ability in question
	 * @return the quality description
	 */
	public String getAbilityQualityDesc(Ability A);

	/**
	 * Returns a description of the range of the given skill.
	 *
	 * @param A the Ability in question
	 * @return the range description
	 */
	public String getAbilityRangeDesc(Ability A);

	/**
	 * Returns a description of the amount of trains/practices
	 * required by the give mob to gain the given ability.
	 *
	 * @param A the Ability in question
	 * @param forM the mob to get info for
	 * @return the cost description
	 */
	public String getAbilityCostDesc(Ability A, final MOB forM);
}
