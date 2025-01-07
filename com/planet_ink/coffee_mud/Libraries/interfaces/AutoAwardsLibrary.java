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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2008-2024 Bo Zimmerman

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
 * The library for managing the various automatic player awards, such
 * as auto-titles, which are player titles that can, according to a mask,
 * be automatically added and removed from players as they meet, and stop
 * meeting, various criterium. Similar are the AutoProperties entries, which
 * award properties based on player and date-based masks.
 *
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary#reloadAutoTitles()
 */
public interface AutoAwardsLibrary extends CMLibrary
{
	/**
	 * Returns an enumerator of the auto-title strings themselves.
	 * The strings will substitute a * character for the players
	 * name when building the final title.
	 * @return an enumerator of the auto-title strings themselves
	 */
	public Enumeration<String> autoTitles();

	/**
	 * Returns the full auto-title definition for the given title/id.
	 *
	 * @param title the title itself
	 * @return the AutoTitle object that defines it further
	 */
	public AutoTitle getAutoTitle(String title);

	/**
	 * Returns the string mask attributed to a particular
	 * title string.  The mask is as described by the masking
	 * library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param title the title itself
	 * @return the zapper mask to determine who should get this title
	 */
	public String getAutoTitleMask(String title);

	/**
	 * Returns whether the given string matches one of the defined
	 * player titles.
	 * @param title the strong to match
	 * @return true if a title of that string exists, false otherwise
	 */
	public boolean isExistingAutoTitle(String title);

	/**
	 * Scans an admin-given auto-title definition string to see
	 * if it is properly formatted for adding to the list of
	 * auto-titles.
	 * @param row the admin-entered command string
	 * @param addIfPossible true to add it to the list, false to scan-only
	 * @return true if the title meets the criterium, false if it is rejected
	 */
	public String evaluateAutoTitle(String row, boolean addIfPossible);

	/**
	 * Scans all existing titles to see if any should be added to the
	 * given mob.  If any match, the title is added to the mobs list
	 * of choices, after being customized.
	 * @param mob the mob to check for new titles for
	 * @return true if any titles were added, false otherwise
	 */
	public boolean evaluateAutoTitles(MOB mob);

	/**
	 * Forces this library to re-load its list of titles from
	 * the resource file titles.ini.
	 */
	public void reloadAutoTitles();

	/**
	 * Appends to the list of titles from the given text and
	 * refreshes the cache.  This will also save the file.
	 * @param text the text of the new title definition
	 */
	public void appendAutoTitle(String text);

	/**
	 * Removes the given title from all affected players,
	 * removes the given title from the titles properties file,
	 * and refreshes the titles cache.
	 * @param title the title to remove
	 * @return an error message, or null for success
	 */
	public String deleteTitleAndResave(String title);

	/**
	 * Returns the filename of the auto-title recipe
	 * file.
	 *
	 * @return the auto-files filename
	 */
	public String getAutoTitleFilename();

	/**
	 * Returns the filename of the auto-properties
	 * recipe file.
	 *
	 * @return the auto-properties filename
	 */
	public String getAutoPropsFilename();

	/**
	 * Reads the auto-award recipe file given and
	 * returns the instructions from it (basically,
	 * the comments)
	 *
	 * @param filename the file to read
	 * @return the instructions
	 */
	public String getAutoAwardInstructions(String filename);

	/**
	 * Returns an enumeration of all defined auto-properties
	 *
	 * @return the auto-properties
	 */
	public Enumeration<AutoProperties> getAutoProperties();

	/**
	 * Returns a hash of the auto-properties, allowing quick
	 * checks for changes.
	 *
	 * @return the auto-properties hash
	 */
	public int getAutoPropertiesHash();

	/**
	 * Allows the given mob to start participating in
	 * auto-properties.
	 *
	 * @param mob the mob who wants auto properties
	 * @param reset recalculate any existing auto properties
	 */
	public void giveAutoProperties(final MOB mob, boolean reset);

	/**
	 * In auto properties, this will allow it to be modified on a line-by-line
	 * basis automatically, either adding, replacing or deleting a line numbered 1..n.
	 * Comments beginning with "#" or lines with no content are skipped.
	 * Send Integer.MAX_VALUE to append.
	 *
	 * @param lineNum the line to edit
	 * @param newLine null to delete the line, or the new line
	 * @return true if the line was found, false otherwise
	 */
	public boolean modifyAutoProperty(int lineNum, final String newLine);

	/**
	 * Class to store the definitional information
	 * about a single AutoTitle
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public interface AutoTitle
	{
		/**
		 * Returns the ID/Title of the title.
		 * @return the ID/Title of the title.
		 */
		public String getTitle();

		/**
		 * Returns the uncompiled ZapperMask string
		 * @return the uncompiled ZapperMask string
		 */
		public String getMaskStr();

		/**
		 * Returns the compiled ZapperMask
		 * @return the compiled ZapperMask
		 */
		public CompiledZMask getMask();

		/**
		 * Returns the max number of players who
		 * can have this title.
		 * @return the max number
		 */
		public int getMax();

		/**
		 * Change and read the counter for number
		 * of players who presently have this title.
		 *
		 * @param amt the amount to change this number by
		 * @return the current amount
		 */
		public int bumpCounter(int amt);
	}

	/**
	 * Class to store the definitional information
	 * about a single set of AutoProperties
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public interface AutoProperties
	{
		/**
		 * Returns the general player
		 * zappermask.
		 *
		 * @return player zappermask
		 */
		public String getPlayerMask();

		/**
		 * Returns the date-base zappermask.
		 *
		 * @return the date-base zappermask.
		 */
		public String getDateMask();

		/**
		 * Returns the general player compiled
		 * zappermask.
		 *
		 * @return player compiled zappermask
		 */
		public CompiledZMask getPlayerCMask();

		/**
		 * Returns the date-based compiled zappermask.
		 * @return the date-based compiled zappermask.
		 */
		public CompiledZMask getDateCMask();

		/**
		 * Returns the pair of ability/behavior id,
		 * and parms/args to give to those who match
		 * this mask.
		 *
		 * @return the awards
		 */
		public Pair<String, String>[] getProps();

		/**
		 * A quick reference for the date-based mask to
		 * tell how often to check for changes.
		 *
		 * @return the time period of the date mask
		 */
		public TimePeriod getPeriod();
	}
}
