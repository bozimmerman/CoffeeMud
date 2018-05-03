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
   Copyright 2008-2018 Bo Zimmerman

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
		/**
		 * The code-word/identifier for this editor.  Corresponds directly to the
		 * bits in the coded parameter string.
		 * @return code-word/identifier for this editor
		 */
		public String ID();
		
		/**
		 * The general type of data being manipulated by the editor.
		 * @see ParmType
		 * @return general type of data being manipulated by the editor
		 */
		public ParmType parmType();
		
		/**
		 * Creates key/display pairs from an enumeration of objects whose identity
		 * depends entirely on this editor.  It returns a pair list where the first
		 * entry is the actual returnable value, and the second is the user-enterable
		 * selectable display key.
		 * @param e the enumeration of objects to create choices from
		 * @return key/display pairs
		 */
		public PairList<String,String> createChoices(Enumeration<? extends Object> e);
		
		/**
		 * Creates key/display pairs from a list of objects whose identity
		 * depends entirely on this editor.  It returns a pair list where the first
		 * entry is the actual returnable value, and the second is the user-enterable
		 * selectable display key.
		 * @param V the list of objects to create choices from
		 * @return key/display pairs
		 */
		public PairList<String,String> createChoices(Vector<? extends Object> V);
		
		/**
		 * Creates key/display pairs from a list of strings whose identity
		 * depends entirely on this editor.  It returns a pair list where the first
		 * entry is the actual returnable value, and the second is the user-enterable
		 * selectable display key.  In this case, they would both be the same.
		 * @param S the array of strings to create choices from
		 * @return key/display pairs
		 */
		public PairList<String,String> createChoices(String[] S);
		
		/**
		 * Creates key/display pairs whose identity depends entirely on this editor.  
		 * It returns a pair list where the first entry is the actual returnable 
		 * value, and the second is the user-enterable selectable display key.  
		 * @return key/display pairs
		 */
		public PairList<String,String> choices();
		
		/**
		 * Returns how much this editor applies as the appropriate editor to the
		 * given object.  A number less than 0 means it definitely doesn't apply.
		 * The higher the number, the more likely it is to be the correct edior.
		 * @param o the object to check and see if this is an editor of
		 * @return a number denoting how likely it is that this is the editor.
		 */
		public int appliesToClass(Object o);
		
		/**
		 * Returns whether the given value constitutes a valid value for this editor.
		 * @param oldVal the data to check
		 * @return true if the value is acceptable, false otherwise
		 */
		public boolean confirmValue(String oldVal);
		
		/**
		 * Presents fake user input for testing.  The String array is the pre-parsed
		 * commands that a user would have entered to satisfy the prompt or prompts
		 * represented by this editor.
		 * @param oldVal the current value
		 * @return  fake user input for testing
		 */
		public String[] fakeUserInput(String oldVal);
		
		/**
		 * Presents the given mob player the official command line prompt for this editor and
		 * lets them enter a value or values before returning the final value as a result.
		 * @param mob the player who is being prompted
		 * @param oldVal the old/previous value for this field
		 * @param showNumber the arbitrary number of this field 1, 2, 3.. 
		 * @param showFlag same as shownumber to edit, -1 to display, -999 to always edit
		 * @return the value entered by the user
		 * @throws java.io.IOException typically means a dropped carrier
		 */
		public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException;
		
		/**
		 * The displayable name of this column.
		 * @return displayable name of this column.
		 */
		public String colHeader();
		
		/**
		 * The display prompt used for command line editors.
		 * @return display prompt used for command line editors.
		 */
		public String prompt();
		
		/**
		 * The default value to use when no previous value is available.
		 * @return default value to use when no previous value is available
		 */
		public String defaultValue();

		/**
		 * The current web value of this field, sufficient to be put into the VALUE field
		 * of a text or hidden tag.
		 * @param httpReq the request objects, containing access to url parameters
		 * @param parms the tag url parameters map
		 * @param oldVal the original previous value of this field
		 * @param fieldName the name of the field
		 * @return current web value of this field
		 */
		public String webValue(HTTPRequest httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName);
		
		/**
		 * Returns the html tag field, complete with current value, for this editor
		 * @param httpReq the request objects, containing access to url parameters
		 * @param parms the tag url parameters map
		 * @param oldVal the original previous value of this field
		 * @param fieldName the name of the field
		 * @return the html tag field, complete with current value, for this editor
		 */
		public String webField(HTTPRequest httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName);
		
		/**
		 * The current web value of this field, sufficient to be put into the overview table
		 * showing the value of all the fields.
		 * @param httpReq the request objects, containing access to url parameters
		 * @param parms the tag url parameters map
		 * @param oldVal the original previous value of this field
		 * @return current web value of this field for an overview table
		 */
		public String webTableField(HTTPRequest httpReq, java.util.Map<String,String> parms, String oldVal);
		
		/**
		 * Createa a new value field from an item, given the specific craftor to which this editor was made.
		 * @param A the item craftor that this editor belongs to
		 * @param I the item to grab a field from
		 * @return the value from the item
		 */
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
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFormat()
	 * @param mob the mob who is editing this recipe file
	 * @param recipeFilename the unpathed regular filename of the recipe file to edit
	 * @param recipeFormat the recipe format from the crafting skill recipe format string
	 * @throws java.io.IOException an i/o error in session communication
	 */
	public void modifyRecipesList(MOB mob, String recipeFilename, String recipeFormat) throws java.io.IOException;
	
	/**
	 * Test method for the crafting common skill recipe parsers.  Basically it loads a recipe
	 * file, parses it into the editors, re-generates the recipe file data from the
	 * editors, and then optionally re-saves.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFormat()
	 * @param recipeFilename the unpathed regular filename of the recipe data to start with
	 * @param recipeFormat the recipe format coded string from
	 * @param save true to re-save the recipes file, false not to
	 * @throws CMException a parse error, if any
	 */
	public void testRecipeParsing(String recipeFilename, String recipeFormat, boolean save) throws CMException;
	/**
	 * Test method for the crafting common skill recipe parsers.  Basically it takes loaded
	 * recipe file data, parses it into the editors, re-generates the recipe file data from the
	 * editors, and then either returns, or throws an exception if there were parsing errors
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFormat()
	 * @param recipesString the raw loaded recipe data
	 * @param recipeFormat the recipe format coded string from
	 * @throws CMException a parse error, if any
	 */
	public void testRecipeParsing(StringBuffer recipesString, String recipeFormat) throws CMException;
	
	/**
	 * Mian parser for the crafting common skill recipe parsers.  It loads a recipe
	 * file, parses it into the editors, and then returns the AbilityRecipeData.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFile()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility#parametersFormat()
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
	
	/**
	 * Given an CraftorAbility object (usually a common skill), this method will load the raw
	 * recipe file and return it as a stringbuffer.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.CraftorAbility
	 * @param iA the CraftorAbility skill
	 * @return the recipes for that CraftorAbility, as a stringbuffer
	 */
	public StringBuffer getRecipeList(CraftorAbility iA);
	
	/**
	 * Given an ItemCraftor object (usually a common skill), and an item which the ItemCraftor
	 * might have crafted, this method will construct a single Recipe text line coded for use
	 * by a Recipe object.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Recipe
	 * @param C the ItemCraftor skill
	 * @param I the Item to return a recipe for
	 * @return the recipe line for that ItemCraftor Item
	 * @throws CMException a recipe syntax error in generating the recipe
	 */
	public String makeRecipeFromItem(final ItemCraftor C, final Item I) throws CMException;

	/**
	 * An AbilityParameters interface for passing around a completely decoded CraftorAbility
	 * (Common Skill) recipe list, ready for manipulation by users.
	 * 
	 * DEV NOTES: Data rows are a DVector (editor ID, data).  However, it starts off as
	 * (List of possible editor IDs, data), until the correct ID is determined.
	 * For data rows, the offender is blankRow() below, which returns the confused set. 
	 * The columns are always either a string, or a list of strings, for multi-use columns.
	 * @author Bo Zimmerman
	 *
	 */
	public static interface AbilityRecipeData
	{
		/**
		 * Returns the VFS filename of the recipe file
		 * @return the VFS filename of the recipe file
		 */
		public String recipeFilename();
		
		/**
		 * Returns the coded format of the recipe list, including optional data
		 * @return the coded format of the recipe list, including optional data
		 */
		public String recipeFormat();
		
		/**
		 * The rows of data, representing the rows of recipes.  One row per List item.
		 * @return rows of data, representing the rows of recipes.  One row per List item.
		 */
		public List<DVector> dataRows();
		
		/**
		 * The columns of the recipe table, including multi-use and optional column data
		 * @return columns of the recipe table, including multi-use and optional column data
		 */
		public List<? extends Object> columns();
		
		/**
		 * Returns the display length of each column, for display purposes
		 * @return the display length of each column, for display purposes
		 */
		public int[] columnLengths();
		
		/**
		 * Returns the display name of each column, for display purposes.
		 * @return the display name of each column, for display purposes.
		 */
		public String[] columnHeaders();
		
		/**
		 * Returns the number of columns that can contain recipe data
		 * @return the number of columns that can contain recipe data
		 */
		public int numberOfDataColumns();
		
		/**
		 * Returns the last parse error when trying to parse a recipe file.
		 * null means no error.
		 * @return the last parse error when trying to parse a recipe file.
		 */
		public String parseError();
		
		/**
		 * Returns the column number index that represents the Class of the objects
		 * created by this recipe.
		 * @return the column number index that represents the Class of the objects
		 */
		public int getClassFieldIndex();
		
		/**
		 * Creates a new recipe row from the given Class information
		 * @param classFieldData the class info for the object in the recipe
		 * @return the new coded row.
		 */
		public DVector newRow(String classFieldData);
		
		/**
		 * Creates a new blank recipe row for alteration.
		 * @return a new blank recipe row for alteration.
		 */
		public DVector blankRow();
		
		/**
		 * Returns true if the recipe file, when loaded, was saved in the vfs
		 * @return true if the recipe is in the vfs, false for local fs
		 */
		public boolean wasVFS();
	}

}
