package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2008-2022 Bo Zimmerman

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
 * The command line user interface for almost all On Line Editor functions
 * goes through this library.  Functions are available for Modifiable
 * interface edits, general menu edits, as well as abstract object
 * editor menu interfaces.
 *
 * @author Bo Zimmerman
 *
 */
public interface GenericEditor extends CMLibrary
{
	/**
	 * An interface for implementing a custom evaluator
	 * for prompts requiring lists of things.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface CMEval
	{
		/**
		 * Evaluate the given value entry against a list
		 * of choices, throwing an exception on problems.
		 *
		 * @param val the user entry
		 * @param choices the choices
		 * @param emptyOK true if "" is an ok value
		 * @return the adjusted value
		 * @throws CMException any errors
		 */
		public Object eval(Object val, Object[] choices, boolean emptyOK)
				throws CMException;
	}

	public void modifyArea(MOB mob, Area myArea, Set<Area> alsoUpdateAreas, int showFlag) throws IOException;
	public Room modifyRoom(MOB mob, Room R, int showFlag) throws IOException;
	public void modifyAccount(MOB mob, PlayerAccount A, int showFlag) throws IOException;
	public void modifyStdItem(MOB mob, Item thang, int showFlag) throws IOException;
	public void modifyStdMob(MOB mob, MOB thang, int showFlag) throws IOException;
	public void modifyComponents(MOB mob, String componentID, int showFlag) throws IOException;
	public void modifyClan(MOB mob, Clan C, int showFlag) throws IOException;
	public void modifyGenAbility(MOB mob, Ability me, int showFlag) throws IOException;
	public void modifyGenLanguage(MOB mob, Language me, int showFlag) throws IOException;
	public void modifyManufacturer(MOB mob, Manufacturer me, int showFlag) throws IOException;
	public void modifyGenCraftSkill(MOB mob, Ability me, int showFlag) throws IOException;
	public void modifyGenGatheringSkill(final MOB mob, final Ability me, int showFlag) throws IOException;
	public void modifyGenClass(MOB mob, CharClass me, int showFlag) throws IOException;
	public void modifyGenExit(MOB mob, Exit me, int showFlag) throws IOException;
	public void modifyGenRace(MOB mob, Race me, int showFlag) throws IOException;
	public void modifyPlayer(MOB mob, MOB me, int showFlag) throws IOException;
	public void modifyGovernment(MOB mob, ClanGovernment me, int showFlag) throws IOException;
	public AbilityMapper.AbilityMapping modifyAllQualifyEntry(MOB mob, String eachOrAll, Ability me, int showFlag) throws IOException;
	public String modifyPlane(final MOB mob, final String planeName, final Map<String,String> planeSet, int showFlag) throws IOException;

	public Room changeRoomType(Room R, Room newRoom);
	public void spellsOrBehavs(MOB mob, List<CMObject> V, int showNumber, int showFlag, boolean inParms) throws IOException;
	public void spellsOrBehaviors(MOB mob, List<CMObject> V, int showNumber, int showFlag, boolean inParms) throws IOException;
	public void wornLocation(MOB mob, long[] oldWornLocation, boolean[] logicalAnd, int showNumber, int showFlag) throws IOException;
	public void wornLayer(MOB mob, short[] layerAtt, short[] clothingLayer, int showNumber, int showFlag) throws IOException;
	public void genAbility(MOB mob, Physical P, int showNumber, int showFlag) throws IOException;
	public void genAffects(MOB mob, Physical P, int showNumber, int showFlag) throws IOException;
	public void genBehaviors(MOB mob, PhysicalAgent P, int showNumber, int showFlag) throws IOException;
	public void genDescription(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;
	public void genDisplayText(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;
	public void genMiscSet(MOB mob, Environmental E, int showFlag) throws IOException;
	public void genName(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;
	public void genMiscText(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;

	public boolean promptToggle(MOB mob, int showNumber, int showFlag, String fieldDisp) throws IOException;
	public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;
	public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;
	public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;
	public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;
	public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;
	public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;
	public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;
	public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;
	public int promptMulti(MOB mob, int oldVal, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices) throws IOException;
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices) throws IOException;
	public String promptMultiSelectList(MOB mob, String oldVal, String delimiter, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices, boolean nullOK) throws IOException;
	public String promptMultiOrExtra(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices) throws IOException;
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK) throws IOException;
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK, boolean rawPrint) throws IOException;
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK, String help) throws IOException;
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK, boolean rawPrint, String help) throws IOException;
	public String prompt(MOB mob,
						String oldVal,
						int showNumber,
						int showFlag,
						String fieldDisp,
						boolean emptyOK,
						boolean rawPrint,
						String help,
						CMEval eval,
						Object[] choices) throws IOException;
	public String prompt(MOB mob,
						String oldVal,
						int showNumber,
						int showFlag,
						String fieldDisp,
						boolean emptyOK,
						boolean rawPrint,
						int maxChars,
						String help,
						CMEval eval,
						Object[] choices) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface String stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatStr(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface String stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatStr(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField, boolean emptyOK) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Integer stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatInt(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Integer stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatInt(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Boolean stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatBool(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Boolean stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatBool(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Double stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatDouble(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Double stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatDouble(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface stat value chosen from
	 * a set of choices.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @param choices the set of valid choices, any object that has toString()
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatChoices(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField, Object[] choices) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface stat value that consists of a
	 * comma delimited list of values from a set of choices.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @param choices the set of valid choices, any object that has toString()
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatCommaChoices(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField, Object[] choices) throws IOException;

	/**
	 * Prompts the given mob for one or more choices from a given enum values
	 * array.  Returns all the choices in a collection.  Uses the standard
	 * menu interface.
	 *
	 * @param mob the player doing the editing
	 * @param flags the current existing set of selected enum values
	 * @param values the full set of all enum values
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisplayStr the prompt display string
	 * @return the new user value
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public Collection<? extends Object> promptEnumChoices(final MOB mob, final Collection<? extends Object> flags, final Object[] values, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException;
}
