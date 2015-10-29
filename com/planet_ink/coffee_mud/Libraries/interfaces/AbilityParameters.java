package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2008-2015 Bo Zimmerman

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
 * Abilities, like common skills and others, have parameter definition strings that tell the
 * system about any parameters that modify how the ability works.  In the case of common
 * skills, these parameters come from files that give them "recipes" for crafting things.
 * Well, this Library provides methods for accessing and using the web and command line
 * editors that are appropriate to a particular ability's parameters.  It also provides
 * accessors for common skills recipes and their data.
 * 
 * @author Bo Zimmerman
 *
 */
public interface AbilityParameters extends CMLibrary
{
	/**
	 * The column or single parameter types
	 * @author Bo Zimmerman
	 *
	 */
	public enum ParmType
	{
		CHOICES,
		STRING,
		NUMBER,
		STRINGORNULL,
		ONEWORD,
		MULTICHOICES,
		SPECIAL
	}

	/**
	 * The main interface for the individual column editors.  It's methods reflect
	 * both the different ways the parameters can be edited, and the different
	 * kinds of editors necessary to make it work.
	 * @author Bo Zimmerman
	 *
	 */
	public static interface AbilityParmEditor
	{
		public String ID();
		public ParmType parmType();
		public PairList<String,String> createChoices(Enumeration<? extends Object> e);
		public PairList<String,String> createChoices(Vector<? extends Object> V);
		public PairList<String,String> createChoices(String[] S);
		public PairList<String,String> choices();
		public int appliesToClass(Object o);
		public boolean confirmValue(String oldVal);
		public String[] fakeUserInput(String oldVal);
		public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException;
		public String colHeader();
		public String prompt();
		public String defaultValue();
		public String webValue(HTTPRequest httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName);
		public String webField(HTTPRequest httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName);
		public String webTableField(HTTPRequest httpReq, java.util.Map<String,String> parms, String oldVal);
		public String convertFromItem(final ItemCraftor A, final Item I);
	}

	/**
	 * Returns all of the given effect Abilities on the given Affectable as a semicolon delimited 
	 * string of Ability IDs.  If any of the abilities contain parameters, they come after the 
	 * ability and another semicolon.  This method can't really capture all permutations and
	 * combinations, but, well, it seemed like a good idea at the time.
	 * @see Affectable#effects()
	 * @see AbilityParameters#getCodedSpells(String)
	 * @param I the Affectable one to look at the effects of
	 * @return the coded string of those effects
	 */
	public String encodeCodedSpells(Affectable I);
	
	/**
	 * Parses the coded effects available from an ability parameter column and generates
	 * the Ability objects with any parameters of their own.
	 * @param spells the coded ability parameter affectable effects string
	 * @see Affectable#effects()
	 * @see AbilityParameters#encodeCodedSpells(Affectable)
	 * @return the list of ability which are the effects
	 */
	public List<Ability> getCodedSpells(String spells);
	
	/**
	 * Parses a coded wear location, for armor-type items that have particular
	 * wear locations, and fills in the given arrays with the information
	 * contained therein.
	 * @param layerAtt one dimensional array with the layer attributes
	 * @param layers one dimensional array with the layer level
	 * @param wornLoc one dimensional array with the wear location bitmap
	 * @param logicalAnd one dimensional array with the boolean for whether the location bitmap is AND or OR
	 * @param hardBonus one dimensional array with the hardness bonus (an armor bonus basically)
	 * @param wearLocation The coded wear location string
	 */
	public void parseWearLocation(short[] layerAtt, short[] layers, long[] wornLoc, boolean[] logicalAnd, double[] hardBonus, String wearLocation);
	
	/**
	 * Main method for altering a particular recipe list from any of the crafting common
	 * skills, from the command line, for the given mob.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFormat()
	 * @param mob the mob who is editing this recipe file
	 * @param recipeFilename the unpathed regular filename of the recipe file to edit
	 * @param recipeFormat the recipe format from the crafting skill recipe format string
	 * @throws java.io.IOException
	 */
	public void modifyRecipesList(MOB mob, String recipeFilename, String recipeFormat) throws java.io.IOException;
	
	/**
	 * Test method for the crafting common skill recipe parsers.  Basically it loads a recipe
	 * file, parses it into the editors, re-generates the recipe file data from the
	 * editors, and then optionally re-saves.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFormat()
	 * @param recipeFilename the unpathed regular filename of the recipe data to start with
	 * @param recipeFormat the recipe format coded string from
	 * @param save true to re-save the recipes file, false not to
	 */
	public void testRecipeParsing(String recipeFilename, String recipeFormat, boolean save);
	/**
	 * Test method for the crafting common skill recipe parsers.  Basically it takes loaded
	 * recipe file data, parses it into the editors, re-generates the recipe file data from the
	 * editors, and then either returns, or throws an exception if there were parsing errors
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFormat()
	 * @param recipesString the raw loaded recipe data
	 * @param recipeFormat the recipe format coded string from
	 * @throws CMException a parse error, if any
	 */
	public void testRecipeParsing(StringBuffer recipesString, String recipeFormat) throws CMException;
	
	/**
	 * Mian parser for the crafting common skill recipe parsers.  It loads a recipe
	 * file, parses it into the editors, and then returns the AbilityRecipeData.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor#parametersFormat()
	 * @see AbilityParameters.AbilityRecipeData
	 * @param recipeFilename the unpathed regular filename of the recipe data to start with
	 * @param recipeFormat the recipe format coded string from
	 * @return the parsed AbilityRecipeData
	 */
	public AbilityRecipeData parseRecipe(String recipeFilename, String recipeFormat);
	
	/**
	 * Map of all the Ability Parameter editor objects, keyed by their parameter
	 * column ID.
	 * @return map of all the Ability Parameter editor objects
	 */
	public Map<String,AbilityParmEditor> getEditors();
	
	/**
	 * Resaves the given recipe file given the editor and data information, already parsed for easy
	 * manipulation.
	 * @see AbilityRecipeData
	 * See also dev notes below
	 * @param mob the mob doing the save, used only for logging
	 * @param recipeFilename the plain unpathed 
	 * @param rowsV the altered data rows
	 * @param columnsV the recipe column information
	 * @param saveVFS true to save to vfs, false for local hard drive
	 */
	public void resaveRecipeFile(MOB mob, String recipeFilename, List<DVector> rowsV, List<? extends Object> columnsV, boolean saveVFS);
	public StringBuffer getRecipeList(ItemCraftor iA);
	public String makeRecipeFromItem(final ItemCraftor C, final Item I) throws CMException;

	/**
	 * 
	 * DEV NOTES: Data rows are a DVector (editor ID, data).  However, it starts off as
	 * (List of possible editor IDs, data), until the correct ID is determined.
	 * The columns are always either a string, or a list of strings, for multi-use columns.
	 * For data rows, the offender is blankRow() below, which returns the confused set. 
	 * @author Bo Zimmerman
	 *
	 */
	public static interface AbilityRecipeData
	{
		public String recipeFilename();
		public String recipeFormat();
		public List<DVector> dataRows();
		public List<? extends Object> columns();
		public int[] columnLengths();
		public String[] columnHeaders();
		public int numberOfDataColumns();
		public String parseError();
		public int getClassFieldIndex();
		public DVector newRow(String classFieldData);
		public DVector blankRow();
		public boolean wasVFS();
	}

}
