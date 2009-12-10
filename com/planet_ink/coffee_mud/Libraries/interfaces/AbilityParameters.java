package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface AbilityParameters extends CMLibrary
{
    public static final int PARMTYPE_CHOICES=0;
    public static final int PARMTYPE_STRING=1;
    public static final int PARMTYPE_NUMBER=2;
    public static final int PARMTYPE_STRINGORNULL=3;
    public static final int PARMTYPE_ONEWORD=4;
    public static final int PARMTYPE_MULTICHOICES=5;
    public static final int PARMTYPE_SPECIAL=6;
    
    public static interface AbilityParmEditor 
    {
        public String ID();
        public int parmType();
        public DVector createChoices(Enumeration e);
        public DVector createChoices(Vector V);
        public DVector createChoices(String[] S);
        public DVector choices();
        public int appliesToClass(Object o);
        public boolean confirmValue(String oldVal);
        public String[] fakeUserInput(String oldVal);
        public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException;
        public String colHeader();
        public String prompt();
        public String defaultValue();
        public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName);
        public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName);
        public String webTableField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal);
    }
    
    public Vector getCodedSpells(String spells);
    public void parseWearLocation(short[] layerAtt, short[] layers, long[] wornLoc, boolean[] logicalAnd, double[] hardBonus, String wearLocation);
    public void modifyRecipesList(MOB mob, String recipeFilename, String recipeFormat) throws java.io.IOException;
    public void testRecipeParsing(String recipeFilename, String recipeFormat, boolean save);
    public AbilityRecipeData parseRecipe(String recipeFilename, String recipeFormat);
    public Hashtable getEditors();
    public void resaveRecipeFile(MOB mob, String recipeFilename, Vector rowsV, Vector columnsV, boolean saveVFS);
    public StringBuffer getRecipeList(ItemCraftor iA);
    
    public static interface AbilityRecipeData 
    {
        public String recipeFilename();
        public String recipeFormat();
        public Vector dataRows();
        public Vector columns();
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
