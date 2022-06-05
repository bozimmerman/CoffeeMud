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
public interface QuestManager extends CMLibrary
{
	public int numQuests();
	public Enumeration<Quest> enumQuests();
	public Quest fetchQuest(int i);
	public Quest fetchQuest(String qname);
	public Quest findQuest(String qname);
	public void addQuest(Quest Q);
	public void delQuest(Quest Q);
	
	public int getHolidayIndex(String named);
	public String getHolidayName(int index);
	public String listHolidays(Area A, String otherParms);
	public String deleteHoliday(int holidayNumber);
	public void modifyHoliday(MOB mob, int holidayNumber);
	public String alterHoliday(String oldName, HolidayData newData);
	public String createHoliday(String named, String areaName, boolean save);
	public StringBuffer getDefaultHoliData(String named, String area);
	public Object getHolidayFile();
	public HolidayData getEncodedHolidayData(String dataFromStepsFile);

	public List<List<String>> breakOutMudChatVs(String MUDCHAT, TriadList<String,String,Integer> behaviors);
	public String breakOutMaskString(String s, List<String> p);

	/**
	 * Parses a Quest Template file into a data structure which no
	 * human, not even me, will ever fully unravel again.  I mean,
	 * it is unravelable, but the risk to breaking the codebase is
	 * enormous, so just trust me, leave it alone.
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
	 * @param mob player mob, for file permission reasons
	 * @param fileToGet the template filename to load
	 * @return the evil data structure
	 */
	public DVector getQuestTemplate(MOB mob, String fileToGet);

	public Quest questMaker(MOB mob);
	
	public List<Quest> getPlayerPersistentQuests(MOB player);

	public GenericEditor.CMEval getQuestCommandEval(QMCommand command);

	public Quest objectInUse(Environmental E);
	public void save();
	public List<String> parseQuestSteps(List<String> script, int startLine, boolean rawLineInput);
	public List<List<String>> parseQuestCommandLines(List<?> script, String cmdOnly, int startLine);
	
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
