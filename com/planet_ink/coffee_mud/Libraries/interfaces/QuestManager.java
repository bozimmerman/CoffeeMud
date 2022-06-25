package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.Libraries.Quests;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
 * Manager for all game Quests and Holidays.
 * Quests are scripts that make temporary modifications to the
 * world, are trackable on players, and can be competitive or
 * not.  They can be created by writing a custom script, or using
 * the QuestMaker Wizard based on templates.
 * Holidays are a special time-based Quest and are managed in the
 * same way as a normal Quest, while Archons see them as a
 * separate system.
 *
 * @author Bo Zimmerman
 *
 */
public interface QuestManager extends CMLibrary
{
	/**
	 * Returns the number of Quest objects in the
	 * cache.
	 *
	 * @see QuestManager#fetchQuest(int)
	 *
	 * @return the number of Quests
	 */
	public int numQuests();

	/**
	 * Given a 0-based index into the Quest
	 * object cache, this will return the
	 * corresponding Quest.
	 *
	 * @see QuestManager#numQuests()
	 *
	 * @param i the index
	 * @return null, or the Quest
	 */
	public Quest fetchQuest(int i);

	/**
	 * Returns the cached Quest with the given
	 * unique name.
	 *
	 * @see QuestManager#findQuest(String)
	 * @see QuestManager#fetchQuest(int)
	 * @see QuestManager#enumQuests()
	 *
	 * @param qname the quest name
	 * @return null, or the Quest object
	 */
	public Quest fetchQuest(String qname);

	/**
	 * Given a unique quest name, or partial name,
	 * this will return the closest matching Quest
	 * from the internal cache.
	 *
	 * @see QuestManager#fetchQuest(String)
	 * @see QuestManager#fetchQuest(int)
	 * @see QuestManager#enumQuests()
	 *
	 * @param qname the name of a quest
	 * @return null, or the Quest object
	 */
	public Quest findQuest(String qname);

	/**
	 * Returns an enumeration of all cached
	 * Quest objects.
	 *
	 * @see QuestManager#delQuest(Quest)
	 * @see QuestManager#addQuest(Quest)
	 * @see QuestManager#fetchQuest(String)
	 *
	 * @return all the quests
	 */
	public Enumeration<Quest> enumQuests();

	/**
	 * Given a Quest object, this will add the
	 * object to the cache, auto-starting it
	 * if necessary.
	 *
	 * @see QuestManager#delQuest(Quest)
	 * @see QuestManager#enumQuests()
	 * @see QuestManager#fetchQuest(String)
	 *
	 * @param Q the Quest to delete
	 */
	public void addQuest(Quest Q);

	/**
	 * Given an existing Quest in the cache, this
	 * will stop it if necesssary, and remove it.
	 *
	 * @see QuestManager#addQuest(Quest)
	 * @see QuestManager#enumQuests()
	 * @see QuestManager#fetchQuest(String)
	 *
	 * @param Q the Quest to delete
	 */
	public void delQuest(Quest Q);

	/**
	 * Returns a nice displayable formatted list of
	 * all holidays and their associated areas.
	 *
	 * @param areaName null or all, or area name
	 * @return the nice list of holidays
	 */
	public String listHolidays(String areaName);

	/**
	 * Given an index, this deletes the associated holiday
	 * from the official holiday file completely.
	 *
	 * @see QuestManager#getHolidayIndex(String)
	 *
	 * @param holidayNumber the 0-based index
	 * @return the result message (error, or happy)
	 */
	public String deleteHoliday(int holidayNumber);

	/**
	 * Given an index, this returns the unique name of the
	 * holiday at that index.
	 *
	 * @see QuestManager#getHolidayIndex(String)
	 * @see QuestManager#listHolidays(String)
	 *
	 * @param index the 0-based index
	 * @return "" or the name of the indexed holiday
	 */
	public String getHolidayName(int index);

	/**
	 * Given a unique holiday name, this will return
	 * its index.
	 *
	 * @see QuestManager#modifyHoliday(MOB, int)
	 * @see QuestManager#getHolidayName(int)
	 * @see QuestManager#deleteHoliday(int)
	 * @see QuestManager#listHolidays(String)
	 *
	 * @param named the unique name of the holiday
	 * @return -1, or the 0 based holiday index
	 */
	public int getHolidayIndex(String named);

	/**
	 * Given a player mob and a holiday number, this allows
	 * the given user to modify the holiday using the standard
	 * menu system.
	 *
	 * @see QuestManager#getHolidayIndex(String)
	 * @see QuestManager#alterHoliday(String, HolidayData)
	 * @see QuestManager#deleteHoliday(int)
	 *
	 * @param mob the player mob to do the editing
	 * @param holidayNumber the holiday number (0 based)
	 */
	public void modifyHoliday(MOB mob, int holidayNumber);

	/**
	 * Given the official name of an existing holiday, and a
	 * HolidayData data structure that was possibly altered,
	 * this will modify the existing holiday to reflect the
	 * new data.  It then saves the holiday file.
	 *
	 * @see QuestManager.HolidayData
	 * @see QuestManager#getHolidayFile()
	 * @see QuestManager#modifyHoliday(MOB, int)
	 *
	 * @param oldName the name of the existing holiday to modify
	 * @param newData the modified holiday data
	 * @return "", or any error message from the alteration
	 */
	public String alterHoliday(String oldName, HolidayData newData);

	/**
	 * Attempts to create and possibly save a holiday quest script
	 * where the holiday has the given name, and applies to the given
	 * area (or ANY).
	 *
	 * @see QuestManager#getDefaultHoliData(String, String)
	 *
	 * @param named the name of the holiday, unique plz
	 * @param areaName the name of the area (or ANY) the holiday occurs in
	 * @param save true to append to the official holiday file
	 * @return "", or any error message from the creation
	 */
	public String createHoliday(String named, String areaName, boolean save);

	/**
	 * Generates a default Holiday quest script.
	 *
	 * @see QuestManager#createHoliday(String, String, boolean)
	 *
	 * @param named the name of the holiday, unique plz
	 * @param area the name of the area (or ANY) the holiday occurs in
	 * @return the default holiday quest script
	 */
	public StringBuffer getDefaultHoliData(String named, String area);

	/**
	 * Loads the official Holiday Quest Script, and parses it into
	 * its constituent steps, returning the list of sub-scripts.
	 *
	 * @see QuestManager#getEncodedHolidayData(String)
	 *
	 * @return the steps of the holiday file
	 * @throws CMException a parsing error occurred
	 */
	public List<String> getHolidayFile() throws CMException;

	/**
	 * Given a quest script representing a Holiday, this will parse the script
	 * and generate a HolidayData object, usually for editing.
	 *
	 * @see QuestManager.HolidayData
	 * @see QuestManager#getHolidayFile()
	 * @see QuestManager#alterHoliday(String, HolidayData)
	 *
	 * @param dataFromStepsFile the holiday quest script
	 * @return the HolidayData structure
	 */
	public HolidayData getEncodedHolidayData(String dataFromStepsFile);

	/**
	 * Given a list of variables and their values, and the name of a variable,
	 * this will find the variable value that matches the variable name,
	 * and then return the list of definitions.
	 *
	 * @param MUDCHAT the mudchat variable name, usually "MUDCHAT"
	 * @param behaviors list of behavior variables, name, value, an integer
	 * @return the parsed list of mudchat definitions
	 */
	public List<List<String>> breakOutMudChatVs(String MUDCHAT, TriadList<String,String,Integer> behaviors);

	/**
	 * Parses a Quest Template file into a data structure which no
	 * human, not even me, will ever fully unravel again.  I mean,
	 * it is unravelable, but the risk to breaking the codebase is
	 * enormous, so just trust me, leave it alone.  This is all part
	 * of the QuestMaker system.
	 *
	 * The DVector is 5-part, consisting of:
	 * 1. name
	 * 2. description
	 * 3. filename
	 * 4. DVector list of pages
	 *    -- Page is 4-part list, each representing a variable,
	 *    except the first entry, which is page type, name, description
	 *    Variables are: 1. var type code (Integer),
	 *    				 2. name,
	 *    				 3. default value,
	 *    				 4. final value
	 * 5. Final Quest Script as a StringBuffer
	 *
	 * @see QuestManager#getQuestTemplate(MOB, String)
	 * @see QuestManager#questMakerCommandLine(MOB)
	 * @see QuestManager#getQuestCommandEval(QMCommand)
	 *
	 * @param mob player mob, for file permission reasons
	 * @param fileToGet the template filename to load
	 * @return the evil data structure
	 */
	public DVector getQuestTemplate(MOB mob, String fileToGet);

	/**
	 * Runs the Quest Maker Wizard process command line
	 * version for the given mob, returning the resulting
	 * Quest.
	 *
	 * @see QuestManager#getQuestTemplate(MOB, String)
	 * @see QuestManager#questMakerCommandLine(MOB)
	 * @see QuestManager#getQuestCommandEval(QMCommand)
	 *
	 * @param mob the player who wants to create a Quest
	 *
	 * @return null, or the Quest created
	 */
	public Quest questMakerCommandLine(MOB mob);

	/**
	 * This is part of the QuestMaker.  Given a QMCommand,
	 * which is a QuestMaker script variable type, this
	 * will return an Eval object that can be used to
	 * populate a value for the variable of that type.
	 *
	 * @see QuestManager#getQuestTemplate(MOB, String)
	 * @see QuestManager#questMakerCommandLine(MOB)
	 * @see QuestManager#getQuestCommandEval(QMCommand)
	 * @see QuestManager.QMCommand
	 * @see GenericEditor.CMEval
	 *
	 * @param command the quest maker variable type
	 * @return the CMEval object for that variable type
	 */
	public GenericEditor.CMEval getQuestCommandEval(QMCommand command);

	/**
	 * Given a player mob, this will return the list of quests
	 * that the player has accepted and not yet completed.
	 * These are the quests that the player is currently "on".
	 *
	 * @param player the player mob
	 * @return the list of quests the player is on
	 */
	public List<Quest> getPlayerPersistentQuests(MOB player);

	/**
	 * Scans every Quest and returns the first one that is using
	 * the given MOB, Item, Area, Room, whatever.
	 *
	 * @param E the object to find the Quest user of
	 * @return null, or the first Quest using
	 */
	public Quest objectInUse(Environmental E);

	/**
	 * Re-saves every quest in this manager to the database
	 */
	public void save();

	/**
	 * Given a final quest script parsed into lines, and a starting line, this will return
	 * a list of mini-scripts broken by STEPs.
	 *
	 * @param script the final lines of the entire quest script
	 * @param startLine the line to start breaking at
	 * @param rawLineInput false to manage ; line endings, true to leave raw
	 * @return the mini-scripts, cr delimited, broken into steps
	 */
	public List<String> parseQuestSteps(List<String> script, int startLine, boolean rawLineInput);

	/**
	 * Given a semi-parsed quest script and a starting line, this will return all lines, minus any
	 * JavaScript, between the startLine and the next STEP command, parsed into bits.
	 * If a cmdOnly is given it will filter only by those commands.
	 *
	 * @param script the complete quest script parsed
	 * @param cmdOnly null, or a command to filter the script by
	 * @param startLine 0 - the line in the given script to start with
	 * @return the lines of the script, parsed into bits the normal way
	 */
	public List<List<String>> getNextQuestScriptCommands(List<?> script, String cmdOnly, int startLine);

	/**
	 * Interface for the raw definition data for a Holiday
	 *
	 * @author Bo Zimmerman
	 */
	public interface HolidayData
	{
		/**
		 * Returns the list of basic settings, stuff like
		 * NAME, DURATION, MUDDAY, WAIT, etc.  The first
		 * entry is the variable name, the second is the
		 * value, and the third is -1 (maybe Int value
		 * expansion?)
		 *
		 * @return the list of basic settings
		 */
		public TriadList<String,String,Integer> settings();

		/**
		 * Returns the list of behaviors to give mobs
		 * during a holiday.  The first entry is the ID()
		 * of the behavior, the second the parms, and the
		 * third - I don't know.
		 *
		 * @return the list of behaviors
		 */
		public TriadList<String,String,Integer> behaviors();

		/**
		 * Returns the list of properties to give mobs
		 * during a holiday.  The first entry is the ID()
		 * of the property, the second the parms, and the
		 * third, I still don't know.
		 *
		 * @return the list of properties
		 */
		public TriadList<String,String,Integer> properties();

		/**
		 * Stat changes to apply to mobs during a holiday.
		 * The first entry is the stat name, the second is
		 * the value, and the third, might be used?
		 *
		 * @see QuestManager.HolidayData#pricingMobIndex()
		 * @see QuestManager.HolidayData#stepV()
		 *
		 * @return stat changes to apply to mobs
		 */
		public TriadList<String,String,Integer> stats();

		/**
		 * This is a listing of a cache of the actual Quest
		 * Script built from the Holiday.  This is then
		 * modified when the various variables in this Holiday
		 * are.
		 *
		 * @see QuestManager.HolidayData#pricingMobIndex()
		 * @see QuestManager.HolidayData#stats()
		 *
		 * @return the Quest Script
		 */
		public List<String> stepV();

		/**
		 * Returns an integer index into the stepV list,
		 * and is related to the PRICEMASKS quest script
		 * command from the stats data.
		 *
		 * @see QuestManager.HolidayData#stepV()
		 * @see QuestManager.HolidayData#stats()
		 *
		 * @return index into the stepV list
		 */
		public Integer pricingMobIndex();
	}

	/**
	 * MASK for QuestMaker QM COMMAND ORDINAL
	 */
	public final static int	QM_COMMAND_MASK		= 127;

	/**
	 * MASK for QuestMaker QM COMMAND to mark it Optional
	 */
	public final static int	QM_COMMAND_OPTIONAL	= 128;

	/**
	 * Enum of official data types for the QuestMaker Wizard
	 * templates.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum QMCommand
	{
		$TITLE,
		$LABEL,
		$EXPRESSION,
		$UNIQUE_QUEST_NAME,
		$CHOOSE,
		$ITEMXML,
		$STRING,
		$ROOMID,
		$AREA,
		$MOBXML,
		$NAME,
		$LONG_STRING,
		$MOBXML_ONEORMORE,
		$ITEMXML_ONEORMORE,
		$ITEMXML_ZEROORMORE,
		$ZAPPERMASK,
		$ABILITY,
		$MEFFECT,
		$EXISTING_QUEST_NAME,
		$HIDDEN,
		$FACTION,
		$TIMEEXPRESSION
	}
}
