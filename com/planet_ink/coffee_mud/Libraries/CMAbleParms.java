
package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;

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

import java.net.Socket;
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
public class CMAbleParms extends StdLibrary implements AbilityParameters
{
    public String ID(){return "CMAbleParms";}
    
    protected Hashtable DEFAULT_EDITORS = null; 
    
    public CMAbleParms()
    {
        super();
    }
    
    public Vector getCodedSpells(String spells)
    {
        Vector spellsV=new Vector(); 
        if(spells.length()==0) return spellsV;
        if(spells.startsWith("*"))
        {
            spells=spells.substring(1);
            int x=spells.indexOf(";");
            if(x<0) x=spells.length();
            Ability A=CMClass.getAbility(spells.substring(0,x));
            if(A!=null)
            {
                if(x<spells.length())
                    A.setMiscText(spells.substring(x+1));
                spellsV.addElement(A);
                return spellsV;
            }
        }
        Vector V=CMParms.parseSemicolons(spells,true);
        Ability lastSpell=null;
        Ability A=null;
        for(int v=0;v<V.size();v++)
        {
            spells=(String)V.elementAt(v); 
            A=CMClass.getAbility(spells);
            if(A==null)
            {
                if(lastSpell!=null)
                    lastSpell.setMiscText(spells);
            }
            else
            {
                lastSpell=A;
                spellsV.addElement(A);
            }
        }
        return spellsV;
    }
    
    protected String parseLayers(short[] layerAtt, short[] clothingLayers, String misctype)
    {
        int colon=misctype.indexOf(":");
        if(colon>=0)
        {
            String layers=misctype.substring(0,colon).toUpperCase().trim();
            misctype=misctype.substring(colon+1).trim();
            if((layers.startsWith("MS"))
                    ||(layers.startsWith("SM")))
            { layers=layers.substring(2); layerAtt[0]=Armor.LAYERMASK_MULTIWEAR|Armor.LAYERMASK_SEETHROUGH;}
            else
            if(layers.startsWith("M"))
            { layers=layers.substring(1); layerAtt[0]=Armor.LAYERMASK_MULTIWEAR;}
            else
            if(layers.startsWith("S"))
            { layers=layers.substring(1); layerAtt[0]=Armor.LAYERMASK_SEETHROUGH;}
            clothingLayers[0]=CMath.s_short(layers);
        }
        return misctype;
    }
    
    public void parseWearLocation(short[] layerAtt, short[] layers, long[] wornLoc, boolean[] logicalAnd, double[] hardBonus, String wearLocation)
    {
        if(layers != null)
        {
            layerAtt[0] = 0;
            layers[0] = 0;
            wearLocation=parseLayers(layerAtt,layers,wearLocation);
        }
        
        double hardnessMultiplier = hardBonus[0];
        wornLoc[0] = 0;
        hardBonus[0]=0.0;
        Wearable.CODES codes = Wearable.CODES.instance();
        for(int wo=1;wo<codes.total();wo++)
        {
            String WO=codes.name(wo).toUpperCase();
            if(wearLocation.equalsIgnoreCase(WO))
            {
                hardBonus[0]+=codes.location_strength_points()[wo];
                wornLoc[0]=CMath.pow(2,wo-1);
                logicalAnd[0]=false;
            }
            else
                if((wearLocation.toUpperCase().indexOf(WO+"||")>=0)
                        ||(wearLocation.toUpperCase().endsWith("||"+WO)))
                {
                    if(hardBonus[0]==0.0)
                        hardBonus[0]+=codes.location_strength_points()[wo];
                    wornLoc[0]=wornLoc[0]|CMath.pow(2,wo-1);
                    logicalAnd[0]=false;
                }
                else
                    if((wearLocation.toUpperCase().indexOf(WO+"&&")>=0)
                            ||(wearLocation.toUpperCase().endsWith("&&"+WO)))
                    {
                        hardBonus[0]+=codes.location_strength_points()[wo];
                        wornLoc[0]=wornLoc[0]|CMath.pow(2,wo-1);
                        logicalAnd[0]=true;
                    }
        }
        hardBonus[0]=(int)Math.round(hardBonus[0] * hardnessMultiplier);
    }
    
    public Vector parseRecipeFormatColumns(String recipeFormat) {
        char C = '\0';
        StringBuffer currentColumn = new StringBuffer("");
        Vector currentColumns = null;
        char[] recipeFmtC = recipeFormat.toCharArray();
        Vector columnsV = new Vector();
        for(int c=0;c<=recipeFmtC.length;c++) {
            if(c==recipeFmtC.length) {
                
                break;
            }
            C = recipeFmtC[c];
            if((C=='|')
            &&(c<(recipeFmtC.length-1))
            &&(recipeFmtC[c+1]=='|')
            &&(currentColumn.length()>0))
            {
                if(currentColumn.length()>0) {
                    if(currentColumns == null) {
                        currentColumns = new Vector();
                        columnsV.addElement(currentColumns);
                    }
                    currentColumns.addElement(currentColumn.toString());
                    currentColumn.setLength(0);
                }
                c++;
            }
            else
                if(Character.isLetter(C)||Character.isDigit(C)||(C=='_'))
                    currentColumn.append(C);
                else
                {
                    if(currentColumn.length()>0) {
                        if(currentColumns == null) {
                            currentColumns = new Vector();
                            columnsV.addElement(currentColumns);
                        }
                        currentColumns.addElement(currentColumn.toString());
                        currentColumn.setLength(0);
                    }
                    currentColumns = null;
                    if((C=='.')
                    &&(c<(recipeFmtC.length-2))
                    &&(recipeFmtC[c+1]=='.')
                    &&(recipeFmtC[c+2]=='.'))
                    {
                        c+=2;
                        columnsV.addElement("...");
                    }
                    else
                        if(columnsV.lastElement() instanceof String)
                            columnsV.setElementAt(((String)columnsV.lastElement())+C,columnsV.size()-1);
                        else
                            columnsV.addElement(""+C);
                }
        }
        if(currentColumn.length()>0)
        {
            if(currentColumns == null) {
                currentColumns = new Vector();
                columnsV.addElement(currentColumns);
            }
            currentColumns.addElement(currentColumn.toString());
        }
        if((currentColumns != null) && (currentColumns.size()==0))
            columnsV.remove(currentColumns);
        return columnsV;
    }
    
	protected static int getClassFieldIndex(DVector dataRow) {
        for(int d=0;d<dataRow.size();d++)
            if(dataRow.elementAt(d,1) instanceof Vector) 
            {
                Vector V=(Vector)dataRow.elementAt(d,1);
                if(V.contains("ITEM_CLASS_ID")||V.contains("FOOD_DRINK"))
                    return d;
            }
            else
                if(dataRow.elementAt(d,1) instanceof String)
                {
                    String s=(String)dataRow.elementAt(d,1);
                    if(s.equalsIgnoreCase("ITEM_CLASS_ID")||s.equalsIgnoreCase("FOOD_DRINK"))
                        return d;
                }
        return -1;
    }
    
    protected Item getSampleItem(DVector dataRow)
    {
        boolean classIDRequired = false;
        String classID = null;
        int fieldIndex = getClassFieldIndex(dataRow);
        for(int d=0;d<dataRow.size();d++)
            if((dataRow.elementAt(d,1) instanceof Vector) 
            &&(!classIDRequired)
            &&(((Vector)dataRow.elementAt(d,1)).size()>1))
                classIDRequired=true;
        if(fieldIndex >=0)
            classID=(String)dataRow.elementAt(fieldIndex,2);
        if((classID!=null)&&(classID.length()>0))
            if(classID.equalsIgnoreCase("FOOD"))
                return CMClass.sampleItem("GenFood");
            else
                if(classID.equalsIgnoreCase("SOAP"))
                    return CMClass.sampleItem("GenItem");
                else
                    if(classID.equalsIgnoreCase("DRINK"))
                        return CMClass.sampleItem("GenDrink");
                    else
                        return CMClass.sampleItem(classID);
        if(classIDRequired)
            return null;
        return CMClass.sampleItem("StdItem");
    }
    
    protected String stripData(StringBuffer str, String div)
    {
        StringBuffer data = new StringBuffer("");
        while(str.length()>0)
        {
            if(str.length() < div.length())
                return null;
            for(int d=0;d<=div.length();d++)
            {
                if(d==div.length())
                {
                    str.delete(0,div.length());
                    return data.toString();
                } 
                else
                    if(str.charAt(d)!=div.charAt(d))
                        break;
            }
            if(str.charAt(0)=='\n')
            {
                if(data.length()>0)
                    return data.toString();
                return null;
            }
            data.append(str.charAt(0));
            str.delete(0,1);
        }
        return null;
    }
    
	protected Vector parseDataRows(StringBuffer recipeData, Vector columnsV, int numberOfDataColumns)
    throws CMException
    {
        StringBuffer str = new StringBuffer(recipeData.toString());
        str = cleanDataRowEOLs(str);
        Vector rowsV = new Vector();
        DVector dataRow = new DVector(2);
        Vector currCol = null;
        String lastDiv = null;
        
        int lastLen = str.length();
        while(str.length() > 0)
        {
            lastLen = str.length();
            for(int c = 0; c < columnsV.size(); c++)
            {
                String div = "\n";
                currCol = null;
                if(columnsV.elementAt(c) instanceof String)
                    stripData(str,(String)columnsV.elementAt(c));
                else
                    if(columnsV.elementAt(c) instanceof Vector)
                    {
                        currCol = (Vector)columnsV.elementAt(c);
                        if(c<columnsV.size()-1)
                        {
                            div = (String)columnsV.elementAt(c+1);
                            c++;
                        }
                    }
                if(!div.equals("..."))
                {
                    lastDiv = div;
                    String data = null;
                    data = stripData(str,lastDiv);
                    if(data == null)
                        data = "";
                    dataRow.addElement(currCol,data);
                    currCol = null;
                } 
                else 
                {
                    String data = stripData(str,lastDiv);
                    if(data == null)
                        break;
                    dataRow.addElement(currCol,data);
                }
            }
            if(dataRow.size() != numberOfDataColumns)
                throw new CMException("Row "+(rowsV.size()+1)+" has "+dataRow.size()+"/"+numberOfDataColumns);
            rowsV.addElement(dataRow);
            dataRow = new DVector(2);
            if(str.length()==lastLen)
                throw new CMException("UNCHANGED: Row "+(rowsV.size()+1)+" has "+dataRow.size()+"/"+numberOfDataColumns);
        }
        if(str.length()<2) str.setLength(0);
        return rowsV;
    }
    
	protected boolean fixDataColumn(DVector dataRow, int rowShow) throws CMException
    {
        Item classModelI = getSampleItem(dataRow);
        Hashtable editors = getEditors();
        if(classModelI == null) {
            Log.errOut("CMAbleParms","Data row "+rowShow+" discarded due to null/empty classID");
            return false;
        } 
        for(int d=0;d<dataRow.size();d++) 
        {
            Vector colV=(Vector)dataRow.elementAt(d,1);
            if(colV.size()==1)
            {
                AbilityParmEditor A = (AbilityParmEditor)editors.get((String)colV.firstElement());
                if((A == null)||(A.appliesToClass(classModelI)<0))
                    A = (AbilityParmEditor)editors.get("N_A");
                dataRow.setElementAt(d,1,A.ID());
            }
            else
            {
                AbilityParmEditor applicableA = null;
                for(int c=0;c<colV.size();c++)
                {
                    AbilityParmEditor A = (AbilityParmEditor)editors.get((String)colV.elementAt(c));
                    if(A==null) 
                        throw new CMException("Col name "+((String)colV.elementAt(c))+" is not defined.");
                    if((applicableA==null)
                            ||(A.appliesToClass(classModelI) > applicableA.appliesToClass(classModelI)))
                        applicableA = A;
                }
                if((applicableA == null)||(applicableA.appliesToClass(classModelI)<0))
                    applicableA = (AbilityParmEditor)editors.get("N_A");
                dataRow.setElementAt(d,1,applicableA.ID());
            }
            AbilityParmEditor A = (AbilityParmEditor)editors.get((String)dataRow.elementAt(d,1));
            if(A==null)
            {
                Log.errOut("CMAbleParms","Item id "+classModelI.ID()+" has no editor for "+((String)dataRow.elementAt(d,1)));
                return false;
            }
            else
                if((rowShow>=0)&&(!A.confirmValue((String)dataRow.elementAt(d,2))))
                    Log.errOut("CMAbleParms","Item id "+classModelI.ID()+" has bad data '"+((String)dataRow.elementAt(d,2))+"' for column "+((String)dataRow.elementAt(d,1))+" at row "+rowShow);
        }
        return true;
    }
    
    protected void fixDataColumns(Vector rowsV) throws CMException
    {
        DVector dataRow = new DVector(2);
        for(int r=0;r<rowsV.size();r++) {
            dataRow=(DVector)rowsV.elementAt(r);
            if(!fixDataColumn(dataRow,r))
            {
                rowsV.removeElementAt(r);
                r--;
            }
        }
    }
    
    protected StringBuffer cleanDataRowEOLs(StringBuffer str)
    {
        if(str.indexOf("\n")<0)
            return new StringBuffer(str.toString().replace('\r','\n'));
        for(int i=str.length()-1;i>=0;i--)
            if(str.charAt(i)=='\r')
                str.delete(i,i+1);
        return str;
    }
    
    public void testRecipeParsing(String recipeFilename, String recipeFormat, boolean save)
    {
        StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+recipeFilename,null,true).text();
        Vector columnsV = parseRecipeFormatColumns(recipeFormat);
        int numberOfDataColumns = 0;
        for(int c = 0; c < columnsV.size(); c++)
            if(columnsV.elementAt(c) instanceof Vector)
                numberOfDataColumns++;
        Vector rowsV = null;
        try {
            rowsV = parseDataRows(str,columnsV,numberOfDataColumns);
            fixDataColumns(rowsV);
        } catch(CMException e) {
            Log.errOut("CMAbleParms","File: "+recipeFilename+": "+e.getMessage());
            return;
        }
        Hashtable editors = getEditors();
        DVector editRow = null;
        int[] showNumber = {0};
        int showFlag =-999;
        MOB mob = CMClass.getMOB("StdMOB");
        Session fakeSession = (Session)CMClass.getCommon("FakeSession");
        mob.setSession(fakeSession);
        fakeSession.setMob(mob);
        for(int r=0;r<rowsV.size();r++)
        {
            editRow = (DVector)rowsV.elementAt(r);
            for(int a=0;a<editRow.size();a++)
            {
                AbilityParmEditor A = (AbilityParmEditor)editors.get((String)editRow.elementAt(a,1));
                try{ 
                    String oldVal = (String)editRow.elementAt(a,2);
                    fakeSession.previousCMD().clear();
                    fakeSession.previousCMD().addAll(CMParms.makeVector(A.fakeUserInput(oldVal)));
                    String newVal = A.commandLinePrompt(mob,oldVal,showNumber,showFlag);
                    editRow.setElementAt(a,2,newVal);
                } catch(Exception e) {}
            }
        }
        fakeSession.setMob(null);
        mob.destroy();
        if(save)
            resaveRecipeFile(mob,recipeFilename,rowsV,columnsV,false);
    }
    
    protected void calculateRecipeCols(int[] lengths, String[] headers, Vector rowsV)
    {
        Hashtable editors = getEditors();
        DVector dataRow = null;
        for(int r=0;r<rowsV.size();r++) {
            dataRow=(DVector)rowsV.elementAt(r);
            for(int c=0;c<dataRow.size();c++)
            {
                AbilityParmEditor A = (AbilityParmEditor)editors.get((String)dataRow.elementAt(c,1));
                if(A==null)
                    Log.errOut("CMAbleParms","Inexplicable lack of a column: "+((String)dataRow.elementAt(c,1)));
                else
                if(headers[c] == null)
                {
                    headers[c] = A.colHeader();
                    lengths[c]=headers[c].length();
                }
                else
                    if((!headers[c].startsWith("#"))
                            &&(!headers[c].equalsIgnoreCase(A.colHeader())))
                    {
                        headers[c]="#"+c;
                        lengths[c]=headers[c].length();
                    }
            }
        }
        int currLenTotal = 0;
        for(int l=0;l<lengths.length;l++)
            currLenTotal+=lengths[l];
        int curCol = 0;
        while((currLenTotal+lengths.length)>72) {
            if(lengths[curCol]>1)
            {
                lengths[curCol]--;
                currLenTotal--;
            }
            curCol++;
            if(curCol >= lengths.length)
                curCol = 0;
        }
        while((currLenTotal+lengths.length)<72) {
            lengths[curCol]++;
            currLenTotal++;
            curCol++;
            if(curCol >= lengths.length)
                curCol = 0;
        }
    }
    
    public AbilityRecipeData parseRecipe(String recipeFilename, String recipeFormat)
    {
        AbilityRecipeDataImpl recipe = new AbilityRecipeDataImpl(recipeFilename, recipeFormat);
        return recipe;
    }

    public StringBuffer getRecipeList(ItemCraftor iA)
    {
        AbilityRecipeData recipe = parseRecipe(iA.parametersFile(),iA.parametersFormat());
        if(recipe.parseError() != null)
            return new StringBuffer("File: "+iA.parametersFile()+": "+recipe.parseError());
        return getRecipeList(recipe);
    }
    
    private StringBuffer getRecipeList(AbilityRecipeData recipe)
    {
        StringBuffer list=new StringBuffer("");
        DVector dataRow = null;
        list.append("### ");
        for(int l=0;l<recipe.columnLengths().length;l++)
            list.append(CMStrings.padRight(recipe.columnHeaders()[l],recipe.columnLengths()[l])+" ");
        list.append("\n\r");
        for(int r=0;r<recipe.dataRows().size();r++) {
            dataRow=(DVector)recipe.dataRows().elementAt(r);
            list.append(CMStrings.padRight(""+(r+1),3)+" ");
            for(int c=0;c<dataRow.size();c++)
                list.append(CMStrings.padRight(CMStrings.limit((String)dataRow.elementAt(c,2),recipe.columnLengths()[c]),recipe.columnLengths()[c])+" ");
            
            list.append("\n\r");
        }
        return list;
    }
    
	public void modifyRecipesList(MOB mob, String recipeFilename, String recipeFormat) throws java.io.IOException
    {
        Hashtable editors = getEditors();
        AbilityRecipeData recipe = parseRecipe(recipeFilename, recipeFormat);
        if(recipe.parseError() != null)
        {
            Log.errOut("CMAbleParms","File: "+recipeFilename+": "+recipe.parseError());
            return;
        }
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            StringBuffer list=getRecipeList(recipe);
            mob.tell(list.toString());
            String lineNum = mob.session().prompt("\n\rEnter a line to edit, A to add, or ENTER to exit: ","");
            if(lineNum.trim().length()==0) break;
            DVector editRow = null;
            if(lineNum.equalsIgnoreCase("A"))
            {
                editRow = recipe.blankRow();
                int keyIndex = getClassFieldIndex(editRow);
                String classFieldData = null;
                if(keyIndex>=0) {
                    AbilityParmEditor A = (AbilityParmEditor)editors.get(((Vector)editRow.elementAt(keyIndex,1)).firstElement());
                    if(A!=null)
                    {
                        classFieldData = A.commandLinePrompt(mob,(String)editRow.elementAt(keyIndex,2),new int[]{0},-999);
                        if(!A.confirmValue(classFieldData))
                        {
                            mob.tell("Invalid value.  Aborted.");
                            continue;
                        }
                    }
                }
                editRow=recipe.newRow(classFieldData);
                if(editRow==null) continue;
                recipe.dataRows().addElement(editRow);
            }
            else
                if(CMath.isInteger(lineNum))
                {
                    int line = CMath.s_int(lineNum);
                    if((line<1)||(line>recipe.dataRows().size()))
                        continue;
                    editRow = (DVector)recipe.dataRows().elementAt(line-1);
                }
                else
                    break;
            if(editRow != null) 
            {
                int showFlag=-1;
                if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
                    showFlag=-999;
                boolean ok=false;
                while(!ok)
                {
                    int[] showNumber = {0};
                    int keyIndex = getClassFieldIndex(editRow);
                    for(int a=0;a<editRow.size();a++)
                        if(a!=keyIndex)
                        {
                            AbilityParmEditor A = (AbilityParmEditor)editors.get((String)editRow.elementAt(a,1));
                            String newVal = A.commandLinePrompt(mob,(String)editRow.elementAt(a,2),showNumber,showFlag);
                            editRow.setElementAt(a,2,newVal);
                        }
                    if(showFlag<-900){ ok=true; break;}
                    if(showFlag>0){ showFlag=-1; continue;}
                    showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
                    if(showFlag<=0)
                    {
                        showFlag=-1;
                        ok=true;
                    }
                }
            }
        }
        if((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String prompt="Save to V)FS, F)ilesystem, or C)ancel (" + (recipe.wasVFS()?"V/f/c":"v/F/c")+"): ";
            String choice=mob.session().choose(prompt,"VFC",recipe.wasVFS()?"V":"F");
            if(choice.equalsIgnoreCase("C"))
                mob.tell("Cancelled.");
            else
            {
                boolean saveToVFS = choice.equalsIgnoreCase("V");
                resaveRecipeFile(mob, recipeFilename,recipe.dataRows(),recipe.columns(),saveToVFS);
            }
        }
    }
    
    public void resaveRecipeFile(MOB mob, String recipeFilename, Vector rowsV, Vector columnsV, boolean saveToVFS)
    {
        StringBuffer saveBuf = new StringBuffer("");
        for(int r=0;r<rowsV.size();r++)
        {
            DVector dataRow = (DVector)rowsV.elementAt(r);
            int dataDex = 0;
            for(int c=0;c<columnsV.size();c++)
            {
                if(columnsV.elementAt(c) instanceof String)
                    saveBuf.append((String)columnsV.elementAt(c));
                else
                    saveBuf.append(dataRow.elementAt(dataDex++,2));
            }
            saveBuf.append("\n");
        }
        CMFile file = new CMFile((saveToVFS?"::":"//")+Resources.buildResourcePath("skills")+recipeFilename,null,true);
        if(!file.canWrite())
            Log.errOut("CMAbleParms","File: "+recipeFilename+" can not be written");
        else
        if((!file.exists())||(!file.text().equals(saveBuf)))
        {
            file.saveText(saveBuf);
            if(!saveToVFS)
            {
                file = new CMFile("::"+Resources.buildResourcePath("skills")+recipeFilename,null,true);
                if((file.exists())&&(file.canWrite()))
                {
                    file.saveText(saveBuf);
                }
            }
            Log.sysOut("CMAbleParms","User: "+mob.Name()+" modified "+(saveToVFS?"VFS":"Local")+" file "+recipeFilename);
            Resources.removeResource("PARSED: "+recipeFilename);
        }
    }
    
    
    public synchronized Hashtable getEditors()
    {
        if(DEFAULT_EDITORS != null)
            return DEFAULT_EDITORS;
        
        Vector V=CMParms.makeVector(new Object[] {
                new AbilityParmEditorImpl("SPELL_ID","The Spell ID",PARMTYPE_CHOICES) {
                    public void createChoices() { createChoices(CMClass.abilities());}
                    public String defaultValue(){ return "Spell_ID";}
                },
                new AbilityParmEditorImpl("RESOURCE_NAME","Resource",PARMTYPE_CHOICES) {
                    public void createChoices() { createChoices(RawMaterial.CODES.NAMES());}
                    public String defaultValue(){ return "IRON";}
                },
                new AbilityParmEditorImpl("ITEM_NAME","Item Final Name",PARMTYPE_STRING){
                    public void createChoices() {}
                    public String defaultValue(){ return "Item Name";}
                }, 
                new AbilityParmEditorImpl("ITEM_LEVEL","Lvl",PARMTYPE_NUMBER){
                    public void createChoices() {}
                    public String defaultValue(){ return "1";}
                },
                new AbilityParmEditorImpl("BUILD_TIME_TICKS","Time",PARMTYPE_NUMBER){
                    public void createChoices() {}
                    public String defaultValue(){ return "20";}
                },
                new AbilityParmEditorImpl("AMOUNT_MATERIAL_REQUIRED","Amt",PARMTYPE_NUMBER){
                    public void createChoices() {}
                    public boolean confirmValue(String oldVal) { return true;}
                    public String defaultValue(){ return "10";}
                },
                new AbilityParmEditorImpl("OPTIONAL_AMOUNT_REQUIRED","Amt",PARMTYPE_NUMBER){
                    public void createChoices() {}
                    public boolean confirmValue(String oldVal) { return true;}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("ITEM_BASE_VALUE","Value",PARMTYPE_NUMBER){
                    public void createChoices() {}
                    public String defaultValue(){ return "5";}
                },
                new AbilityParmEditorImpl("ITEM_CLASS_ID","Class ID",PARMTYPE_CHOICES) {
                    public void createChoices() { 
                        Vector V  = new Vector();
                        V.addAll(CMParms.makeVector(CMClass.clanItems()));
                        V.addAll(CMParms.makeVector(CMClass.armor()));
                        V.addAll(CMParms.makeVector(CMClass.basicItems()));
                        V.addAll(CMParms.makeVector(CMClass.miscMagic()));
                        V.addAll(CMParms.makeVector(CMClass.miscTech()));
                        V.addAll(CMParms.makeVector(CMClass.weapons()));
                        Vector V2=new Vector();
                        Item I;
                        for(Enumeration e=V.elements();e.hasMoreElements();)
                        {
                            I=(Item)e.nextElement();
                            if(I.isGeneric())
                                V2.addElement(I);
                        }
                        createChoices(V2);
                    }
                    public String defaultValue(){ return "GenItem";}
                },
                new AbilityParmEditorImpl("CODED_WEAR_LOCATION","Wear Locs",PARMTYPE_SPECIAL) {
                    public int appliesToClass(Object o) { return ((o instanceof Armor)||(o instanceof MusicalInstrument))?2:-1;}
                    public void createChoices() {}
                    public boolean confirmValue(String oldVal) { return oldVal.trim().length()>0;}
                    public String defaultValue(){ return "NECK";}
                    public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        short[] layerAtt = new short[1];
                        short[] layers = new short[1];
                        long[] wornLoc = new long[1];
                        boolean[] logicalAnd = new boolean[1];
                        double[] hardBonus=new double[1];
                        CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
                        if(httpReq.isRequestParameter(fieldName+"_WORNDATA"))
                        {
                            wornLoc[0]=CMath.s_long(httpReq.getRequestParameter(fieldName+"_WORNDATA"));
                            for(int i=1;;i++)
                                if(httpReq.isRequestParameter(fieldName+"_WORNDATA"+(Integer.toString(i))))
                                    wornLoc[0]=wornLoc[0]|CMath.s_long(httpReq.getRequestParameter(fieldName+"_WORNDATA"+(Integer.toString(i))));
                                else
                                    break;
                            logicalAnd[0] = httpReq.getRequestParameter(fieldName+"_ISTWOHANDED").equalsIgnoreCase("on");
                            layers[0] = CMath.s_short(httpReq.getRequestParameter(fieldName+"_LAYER"));
                            layerAtt[0] = 0;
                            if((httpReq.isRequestParameter(fieldName+"_SEETHRU"))
                            &&(httpReq.getRequestParameter(fieldName+"_SEETHRU").equalsIgnoreCase("on")))
                                layerAtt[0] |= Armor.LAYERMASK_SEETHROUGH;
                            if((httpReq.isRequestParameter(fieldName+"_MULTIWEAR"))
                            &&(httpReq.getRequestParameter(fieldName+"_MULTIWEAR").equalsIgnoreCase("on")))
                                layerAtt[0] |= Armor.LAYERMASK_MULTIWEAR;
                        }
                        return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
                    }
                    public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        String value = webValue(httpReq,parms,oldVal,fieldName);
                        short[] layerAtt = new short[1];
                        short[] layers = new short[1];
                        long[] wornLoc = new long[1];
                        boolean[] logicalAnd = new boolean[1];
                        double[] hardBonus=new double[1];
                        CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,value);
                        StringBuffer str = new StringBuffer("");
                        str.append("\n\r<SELECT NAME="+fieldName+"_WORNDATA MULTIPLE>");
                		Wearable.CODES codes = Wearable.CODES.instance();
                        for(int i=1;i<codes.total();i++)
                        {
                            String climstr=codes.name(i);
                            int mask=(int)CMath.pow(2,i-1);
                            str.append("<OPTION VALUE="+mask);
                            if((wornLoc[0]&mask)>0) str.append(" SELECTED");
                            str.append(">"+climstr);
                        }
                        str.append("</SELECT>");
                        str.append("<BR>\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_ISTWOHANDED value=\"on\" "+(logicalAnd[0]?"CHECKED":"")+">Is worn on All above Locations.");
                        str.append("<BR>\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_ISTWOHANDED value=\"\" "+(logicalAnd[0]?"":"CHECKED")+">Is worn on ANY of the above Locations.");
                        str.append("<BR>\n\rLayer: <INPUT TYPE=TEXT NAME="+fieldName+"_LAYER SIZE=5 VALUE=\""+layers[0]+"\">");
                        boolean seeThru = CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH);
                        boolean multiWear = CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR);
                        str.append("&nbsp;&nbsp;\n\r<INPUT TYPE=CHECKBOX NAME="+fieldName+"_SEETHRU value=\"on\" "+(seeThru?"CHECKED":"")+">Is see-through.");
                        str.append("&nbsp;&nbsp;\n\r<INPUT TYPE=CHECKBOX NAME="+fieldName+"_MULTIWEAR value=\"on\" "+(multiWear?"CHECKED":"")+">Is multi-wear.");
                        return str.toString();
                    }
                    
                    public String reconvert(short[] layerAtt, short[] layers, long[] wornLoc, boolean[] logicalAnd, double[] hardBonus)
                    {
                        StringBuffer newVal = new StringBuffer("");
                        if((layerAtt[0]!=0)||(layers[0]!=0))
                        {
                            if(CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR))
                                newVal.append('M');
                            if(CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH))
                                newVal.append('S');
                            newVal.append(layers[0]);
                            newVal.append(':');
                        }
                        boolean needLink=false;
                		Wearable.CODES codes = Wearable.CODES.instance();
                        for(int wo=1;wo<codes.total();wo++)
                        {
                            if(CMath.bset(wornLoc[0],CMath.pow(2,wo-1)))
                            {
                                if(needLink)
                                    newVal.append(logicalAnd[0]?"&&":"||");
                                needLink = true;
                                newVal.append(codes.name(wo).toUpperCase());
                            }
                        }
                        return newVal.toString();
                        
                    }
                    public String[] fakeUserInput(String oldVal) {
                        Vector V = new Vector();
                        short[] layerAtt = new short[1];
                        short[] layers = new short[1];
                        long[] wornLoc = new long[1];
                        boolean[] logicalAnd = new boolean[1];
                        double[] hardBonus=new double[1];
                        CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
                        V.addElement(""+layers[0]);
                        if(CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH))
                            V.addElement("Y");
                        else
                            V.addElement("N");
                        if(CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR))
                            V.addElement("Y");
                        else
                            V.addElement("N");
                        V.addElement("1");
                        V.addElement("1");
                		Wearable.CODES codes = Wearable.CODES.instance();
                        for(int i=0;i<codes.total();i++)
                            if(CMath.bset(wornLoc[0],codes.get(i)))
                            {
                                V.addElement(""+(i+2));
                                V.addElement(""+(i+2));
                            }
                        V.addElement("0");
                        return CMParms.toStringArray(V); 
                    }
                    public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
                    {
                        short[] layerAtt = new short[1];
                        short[] layers = new short[1];
                        long[] wornLoc = new long[1];
                        boolean[] logicalAnd = new boolean[1];
                        double[] hardBonus=new double[1];
                        CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
                        CMLib.genEd().wornLayer(mob,layerAtt,layers,++showNumber[0],showFlag);
                        CMLib.genEd().wornLocation(mob,wornLoc,logicalAnd,++showNumber[0],showFlag);
                        return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
                    }
                },
                new AbilityParmEditorImpl("CONTAINER_CAPACITY","Cap.",PARMTYPE_NUMBER) {
                    public int appliesToClass(Object o) { return (o instanceof Container)?1:-1;}
                    public void createChoices() {}
                    public String defaultValue(){ return "20";}
                },
                new AbilityParmEditorImpl("BASE_ARMOR_AMOUNT","Arm.",PARMTYPE_NUMBER) {
                    public int appliesToClass(Object o) { return (o instanceof Armor)?2:-1;}
                    public void createChoices() {}
                    public String defaultValue(){ return "1";}
                },
                new AbilityParmEditorImpl("CONTAINER_TYPE","Con.",PARMTYPE_MULTICHOICES) {
                    public void createChoices() { createBinaryChoices(Container.CONTAIN_DESCS);}
                    public int appliesToClass(Object o) { return (o instanceof Container)?1:-1;}
                    public String defaultValue(){ return "0";}
                },
                new AbilityParmEditorImpl("CONTAINER_TYPE_OR_LIDLOCK","Con.",PARMTYPE_MULTICHOICES) {
                    public void createChoices() { 
                        createBinaryChoices(Container.CONTAIN_DESCS);
                        choices().addElement("LID","Lid");
                        choices().addElement("LOCK","Lock");
                        choices().addElement("","");
                    }
                    public int appliesToClass(Object o) { return (o instanceof Container)?1:-1;}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("CODED_SPELL_LIST","Spell Affects",PARMTYPE_SPECIAL) {
                    public void createChoices() {}
                    public boolean confirmValue(String oldVal) {
                        if(oldVal.length()==0) return true;
                        if(oldVal.charAt(0)=='*')
                            oldVal = oldVal.substring(1);
                        int x=oldVal.indexOf('(');
                        int y=oldVal.indexOf(';');
                        if((x<y)&&(x>0)) y=x;
                        if(y<0) 
                            return CMClass.getAbility(oldVal)!=null;
                        return CMClass.getAbility(oldVal.substring(0,y))!=null;
                    }
                    public String defaultValue(){ return "";}
                    public String rebuild(Vector spells) throws CMException
                    {
                        StringBuffer newVal = new StringBuffer("");
                        if(spells.size()==1)
                            newVal.append("*" + ((Ability)spells.firstElement()).ID() + ";" + ((Ability)spells.firstElement()).text());
                        else
                            if(spells.size()>1) {
                                for(int s=0;s<spells.size();s++)
                                {
                                    String txt = ((Ability)spells.elementAt(s)).text().trim();
                                    if((txt.indexOf(';')>=0)||(CMClass.getAbility(txt)!=null))
                                        throw new CMException("You may not have more than one spell when one of the spells parameters is a spell id or a ; character.");
                                    newVal.append(((Ability)spells.elementAt(s)).ID());
                                    if(txt.length()>0)
                                        newVal.append(";" + ((Ability)spells.elementAt(s)).text());
                                    if(s<(spells.size()-1))
                                        newVal.append(";");
                                }
                            }
                        return newVal.toString();
                    }
                    public String[] fakeUserInput(String oldVal) {
                        Vector V = new Vector();
                        Vector V2 = new Vector();
                        Vector spells=CMLib.ableParms().getCodedSpells(oldVal);
                        for(int s=0;s<spells.size();s++) {
                            V.addElement(((Ability)spells.elementAt(s)).ID());
                            V2.addElement(((Ability)spells.elementAt(s)).ID());
                            V2.addElement(((Ability)spells.elementAt(s)).text());
                        }
                        V.addAll(V2);
                        V.addElement("");
                        return CMParms.toStringArray(V);
                    }
                    public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        Vector spells=null;
                        if(httpReq.isRequestParameter(fieldName+"_AFFECT1"))
                        {
                            spells = new Vector();
                            int num=1;
                            String behav=httpReq.getRequestParameter(fieldName+"_AFFECT"+num);
                            String theparm=httpReq.getRequestParameter(fieldName+"_ADATA"+num);
                            while((behav!=null)&&(theparm!=null))
                            {
                                if(behav.length()>0)
                                {
                                    Ability A=CMClass.getAbility(behav);
                                    if(theparm.trim().length()>0)
                                        A.setMiscText(theparm);
                                    spells.addElement(A);
                                }
                                num++;
                                behav=httpReq.getRequestParameter(fieldName+"_AFFECT"+num);
                                theparm=httpReq.getRequestParameter(fieldName+"_ADATA"+num);
                            }
                        }
                        else
                            spells = CMLib.ableParms().getCodedSpells(oldVal);
                        try {
                            return rebuild(spells);
                        } catch(Exception e) {
                            return oldVal;
                        }
                    }
                    public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        Vector spells=CMLib.ableParms().getCodedSpells(webValue(httpReq,parms,oldVal,fieldName));
                        StringBuffer str = new StringBuffer("");
                        str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
                        for(int i=0;i<spells.size();i++)
                        {
                            Ability A=(Ability)spells.elementAt(i);
                            str.append("<TR><TD WIDTH=50%>");
                            str.append("\n\r<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+fieldName+"_AFFECT"+(i+1)+">");
                            str.append("<OPTION VALUE=\"\">Delete!");
                            str.append("<OPTION VALUE=\""+A.ID()+"\" SELECTED>"+A.ID());
                            str.append("</SELECT>");
                            str.append("</TD><TD WIDTH=50%>");
                            String theparm=CMStrings.replaceAll(A.text(),"\"","&quot;");
                            str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
                            str.append("</TD></TR>");
                        }
                        str.append("<TR><TD WIDTH=50%>");
                        str.append("\n\r<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+fieldName+"_AFFECT"+(spells.size()+1)+">");
                        str.append("<OPTION SELECTED VALUE=\"\">Select an Effect");
                        for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
                        {
                            Ability A=(Ability)a.nextElement();
                            String cnam=A.ID();
                            str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
                        }
                        str.append("</SELECT>");
                        str.append("</TD><TD WIDTH=50%>");
                        str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(spells.size()+1)+" VALUE=\"\">");
                        str.append("</TD></TR>");
                        str.append("</TABLE>");
                        return str.toString();
                    }
                    public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
                    {
                        Vector spells=CMLib.ableParms().getCodedSpells(oldVal);
                        StringBuffer rawCheck = new StringBuffer("");
                        for(int s=0;s<spells.size();s++)
                            rawCheck.append(((Ability)spells.elementAt(s)).ID()).append(";").append(((Ability)spells.elementAt(s)).text()).append(";");
                        boolean okToProceed = true;
                        ++showNumber[0];
                        String newVal = null;
                        while(okToProceed) {
                            okToProceed = false;
                            CMLib.genEd().spells(mob,spells,showNumber[0],showFlag,true);
                            StringBuffer sameCheck = new StringBuffer("");
                            for(int s=0;s<spells.size();s++)
                                sameCheck.append(((Ability)spells.elementAt(s)).ID()).append(';').append(((Ability)spells.elementAt(s)).text()).append(';');
                            if(sameCheck.toString().equals(rawCheck.toString())) 
                                return oldVal;
                            try {
                                newVal = rebuild(spells);
                            } catch(CMException e) {
                                mob.tell(e.getMessage());
                                okToProceed = true;
                                break;
                            }
                        }
                        return (newVal==null)?oldVal:newVal.toString();
                    }
                },
                new AbilityParmEditorImpl("BASE_DAMAGE","Dmg.",PARMTYPE_NUMBER) {
                    public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
                    public void createChoices() {}
                    public String defaultValue(){ return "1";}
                },
                new AbilityParmEditorImpl("LID_LOCK","Lid.",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return (o instanceof Container)?1:-1;}
                    public void createChoices() { createChoices(new String[]{"","LID","LOCK"});}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("STATUE","Statue",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return ((!(o instanceof Armor))&&(!(o instanceof Container))&&(!(o instanceof Drink)))?1:-1;}
                    public void createChoices() { createChoices(new String[]{"","STATUE"});}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("RIDE_BASIS","Ride",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return (o instanceof Rideable)?3:-1;}
                    public void createChoices() { createChoices(new String[]{"","CHAIR","TABLE","LADDER","ENTER","BED"});}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("LIQUID_CAPACITY","Liq.",PARMTYPE_NUMBER) {
                    public int appliesToClass(Object o) { return (o instanceof Drink)?4:-1;}
                    public void createChoices() {}
                    public String defaultValue(){ return "25";}
                },
                new AbilityParmEditorImpl("WEAPON_CLASS","WClas",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
                    public void createChoices() { createChoices(Weapon.CLASS_DESCS);}
                    public String defaultValue(){ return "BLUNT";}
                },
                new AbilityParmEditorImpl("SMOKE_FLAG","Smoke",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return (o instanceof Light)?5:-1;}
                    public void createChoices() { createChoices(new String[]{"","SMOKE"});}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("WEAPON_HANDS_REQUIRED","Hand",PARMTYPE_NUMBER) {
                    public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
                    public void createChoices() {}
                    public String defaultValue(){ return "1";}
                },
                new AbilityParmEditorImpl("LIGHT_DURATION","Dur.",PARMTYPE_NUMBER) {
                    public int appliesToClass(Object o) { return (o instanceof Light)?5:-1;}
                    public void createChoices() {}
                    public String defaultValue(){ return "10";}
                },
                new AbilityParmEditorImpl("CLAN_ITEM_CODENUMBER","Typ.",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return (o instanceof ClanItem)?10:-1;}
                    public void createChoices() { createNumberedChoices(ClanItem.CI_DESC);}
                    public String defaultValue(){ return "1";}
                },
                new AbilityParmEditorImpl("CLAN_EXPERIENCE_COST_AMOUNT","Exp",PARMTYPE_NUMBER) {
                    public void createChoices() {}
                    public String defaultValue(){ return "100";}
                },
                new AbilityParmEditorImpl("CLAN_AREA_FLAG","Area",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return o.getClass().getName().toString().indexOf("LawBook")>0?5:-1;}
                    public void createChoices() { createChoices(new String[]{"","AREA"});}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("READABLE_TEXT","Read",PARMTYPE_STRINGORNULL) {
                    public void createChoices() {}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("REQUIRED_COMMON_SKILL_ID","Common Skill",PARMTYPE_CHOICES) {
                    public void createChoices() {
                        Vector V  = new Vector();
                        Ability A = null;
                        for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
                        {
                            A=(Ability)e.nextElement();
                            if((A.classificationCode() & Ability.ALL_ACODES) == Ability.ACODE_COMMON_SKILL)
                                V.addElement(A);
                        }
                        V.addElement("");
                        createChoices(V);
                    }
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("FOOD_DRINK","Type",PARMTYPE_CHOICES) {
                    public void createChoices() { createChoices(new String[]{"","FOOD","DRINK","SOAP","GenPerfume"});}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("SMELL_LIST","Smells",PARMTYPE_STRING) {
                    public void createChoices() {}
                    public int appliesToClass(Object o) { return (o instanceof Perfume)?5:-1;}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("RESOURCE_OR_KEYWORD","Resource",PARMTYPE_SPECIAL) {
                    public void createChoices() {}
                    public boolean confirmValue(String oldVal) { return true;}
                    public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        if(httpReq.isRequestParameter(fieldName+"_WHICH"))
                        {
                            String which=httpReq.getRequestParameter(fieldName+"_WHICH");
                            if(which.trim().length()>0)
                                return httpReq.getRequestParameter(fieldName+"_RESOURCE");
                            return httpReq.getRequestParameter(fieldName+"_WORD");
                        }
                        return oldVal;
                    }
                    public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        String value=webValue(httpReq,parms,oldVal,fieldName);
                        if(value.endsWith("$")) 
                            value = value.substring(0,oldVal.length()-1);
                        value = value.trim();
                        StringBuffer str = new StringBuffer("");
                        str.append("\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH ");
                        boolean rsc=(value.trim().length()==0)||(RawMaterial.CODES.FIND_IgnoreCase(value)>=0);
                        if(rsc) str.append("CHECKED ");
                        str.append("VALUE=\"RESOURCE\">");
                        str.append("\n\r<SELECT NAME="+fieldName+"_RESOURCE>");
                        for(String S : RawMaterial.CODES.NAMES())
                        {
                        	String VALUE = S.equals("NOTHING")?"":S;
                            str.append("<OPTION VALUE=\""+VALUE+"\"");
                            if(rsc&&(value.equalsIgnoreCase(VALUE)))
                                str.append(" SELECTED");
                            str.append(">"+CMStrings.capitalizeAndLower(S));
                        }
                        str.append("</SELECT>");
                        str.append("<BR>");
                        str.append("\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH ");
                        if(!rsc) str.append("CHECKED ");
                        str.append("VALUE=\"\">");
                        str.append("\n\r<INPUT TYPE=TEXT NAME="+fieldName+"_WORD VALUE=\""+(rsc?"":value)+"\">");
                        return str.toString();
                    }
                    public String[] fakeUserInput(String oldVal) { return  new String[]{oldVal}; }
                    public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
                    {
                        ++showNumber[0];
                        boolean proceed = true;
                        String str = oldVal;
                        while(proceed)
                        {
                            proceed = false;
                            str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,CMParms.toStringList(RawMaterial.CODES.NAMES())).trim();
                            if(str.equals(oldVal)) return oldVal;
                            int r=RawMaterial.CODES.FIND_IgnoreCase(str);
                            if(r==0) str="";
                            else if(r>0) str=RawMaterial.CODES.NAME(r);
                            if(str.equals(oldVal)) return oldVal;
                            if(str.length()==0) return "";
                            boolean isResource = CMParms.contains(RawMaterial.CODES.NAMES(),str);
                            if((!isResource)&&(mob.session()!=null)&&(!mob.session().killFlag()))
                                if(!mob.session().confirm("You`ve entered a non-resource item keyword '"+str+"', ok (Y/n)?","Y"))
                                    proceed = true;
                        }
                        return str;
                    }
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("RESOURCE_NAME_OR_HERB_NAME","Resrc/Herb",PARMTYPE_SPECIAL) {
                    public void createChoices() {}
                    public boolean confirmValue(String oldVal) {
                        if(oldVal.trim().length()==0)
                            return true;
                        if(!oldVal.endsWith("$")) {
                            return CMParms.contains(RawMaterial.CODES.NAMES(),oldVal);
                        }
                        return true;
                    }
                    public String[] fakeUserInput(String oldVal) {
                        if(oldVal.endsWith("$"))
                            return new String[]{oldVal.substring(0,oldVal.length()-1)}; 
                        return new String[]{oldVal}; 
                    }
                    public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        AbilityParmEditor A = (AbilityParmEditor)CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
                        if(oldVal.endsWith("$")) oldVal = oldVal.substring(0,oldVal.length()-1);
                        String value = A.webValue(httpReq,parms,oldVal,fieldName);
                        int r=RawMaterial.CODES.FIND_IgnoreCase(value);
                        if(r>=0) return RawMaterial.CODES.NAME(r);
                        return (value.trim().length()==0)?"":(value+"$");
                    }
                    public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        AbilityParmEditor A = (AbilityParmEditor)CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
                        return A.webField(httpReq,parms,oldVal,fieldName);
                    }
                    public String webTableField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal) {
                    	if(oldVal.endsWith("$"))
                    		return oldVal.substring(0,oldVal.length()-1);
                    	return oldVal;
                    }
                    
                    public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
                    {
                        ++showNumber[0];
                        boolean proceed = true;
                        String str = oldVal;
                        String orig = oldVal;
                        while(proceed)
                        {
                            proceed = false;
                            if(oldVal.trim().endsWith("$")) oldVal=oldVal.trim().substring(0,oldVal.trim().length()-1);
                            str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,CMParms.toStringList(RawMaterial.CODES.NAMES())).trim();
                            if(str.equals(orig)) return orig;
                            int r=RawMaterial.CODES.FIND_IgnoreCase(str);
                            if(r==0) str="";
                            else if(r>0) str=RawMaterial.CODES.NAME(r);
                            if(str.equals(orig)) return orig;
                            if(str.length()==0) return "";
                            boolean isResource = CMParms.contains(RawMaterial.CODES.NAMES(),str);
                            if((!isResource)&&(mob.session()!=null)&&(!mob.session().killFlag()))
                            {
                                if(!mob.session().confirm("You`ve entered a non-resource item keyword '"+str+"', ok (Y/n)?","Y"))
                                    proceed = true;
                                else
                                    str=str+"$";
                            }
                        }
                        return str;
                    }
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("AMMO_TYPE","Ammo",PARMTYPE_STRING) {
                    public void createChoices() {}
                    public int appliesToClass(Object o) { return ((o instanceof Weapon)||(o instanceof Ammunition))?2:-1;}
                    public String defaultValue(){ return "arrows";}
                },
                new AbilityParmEditorImpl("AMMO_CAPACITY","Ammo#",PARMTYPE_NUMBER) {
                    public void createChoices() {}
                    public int appliesToClass(Object o) { return ((o instanceof Weapon)||(o instanceof Ammunition))?2:-1;}
                    public String defaultValue(){ return "1";}
                },
                new AbilityParmEditorImpl("MAXIMUM_RANGE","Max",PARMTYPE_NUMBER) 
                { 
                    public int appliesToClass(Object o) { return ((o instanceof Weapon)&&(!(o instanceof Ammunition)))?2:-1;}
                    public void createChoices() {} 
                    public String defaultValue(){ return "5";}
                },
                new AbilityParmEditorImpl("RESOURCE_OR_MATERIAL","Rsc/Mat",PARMTYPE_CHOICES) {
                    public void createChoices() {
                        Vector V=CMParms.makeVector(RawMaterial.CODES.NAMES());
                        V.addAll(CMParms.makeVector(RawMaterial.MATERIAL_DESCS));
                        createChoices(V);
                    }
                    public String defaultValue(){ return "IRON";}
                },
                new AbilityParmEditorImpl("OPTIONAL_RESOURCE_OR_MATERIAL","Rsc/Mat",PARMTYPE_CHOICES) {
                    public void createChoices() {
                        Vector V=CMParms.makeVector(RawMaterial.CODES.NAMES());
                        V.addAll(CMParms.makeVector(RawMaterial.MATERIAL_DESCS));
                        V.addElement("");
                        createChoices(V);
                    }
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("HERB_NAME","Herb Final Name",PARMTYPE_STRING) {
                    public void createChoices() {}
                    public String defaultValue(){ return "Herb Name";}
                },
                new AbilityParmEditorImpl("RIDE_CAPACITY","Ridrs",PARMTYPE_NUMBER) {
                    public void createChoices() {}
                    public int appliesToClass(Object o) { return (o instanceof Rideable)?3:-1;}
                    public String defaultValue(){ return "2";}
                },
                new AbilityParmEditorImpl("METAL_OR_WOOD","Metal",PARMTYPE_CHOICES) {
                    public void createChoices() { createChoices(new String[]{"METAL","WOOD"});}
                    public String defaultValue(){ return "METAL";}
                },
                new AbilityParmEditorImpl("OPTIONAL_RACE_ID","Race",PARMTYPE_SPECIAL) {
                    public void createChoices() { 
                        createChoices(CMClass.races());
                        choices().addElement("","");
                        for(int x=0;x<choices().size();x++)
                            choices().setElementAt(x,1,((String)choices().elementAt(x,1)).toUpperCase());
                    }
                    public String defaultValue(){ return "";}
                    public boolean confirmValue(String oldVal) {
                        if(oldVal.trim().length()==0)
                            return true;
                        Vector parsedVals = CMParms.parse(oldVal.toUpperCase());
                        for(int v=0;v<parsedVals.size();v++)
                            if(CMClass.getRace((String)parsedVals.elementAt(v))==null)
                                return false;
                        return true;
                    }
                    public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        Vector raceIDs=null;
                        if(httpReq.isRequestParameter(fieldName+"_RACE"))
                        {
                            String id="";
                            raceIDs=new Vector();
                            for(int i=0;httpReq.isRequestParameter(fieldName+"_RACE"+id);id=""+(++i))
                            	raceIDs.addElement(httpReq.getRequestParameter(fieldName+"_RACE"+id).toUpperCase().trim());
                        }
                        else
                            raceIDs = CMParms.parse(oldVal.toUpperCase().trim());
                        return CMParms.combine(raceIDs,0);
                    }
                    public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        Vector raceIDs=CMParms.parse(webValue(httpReq,parms,oldVal,fieldName).toUpperCase());
                        StringBuffer str = new StringBuffer("");
                        str.append("\n\r<SELECT NAME="+fieldName+"_RACE MULTIPLE>");
                        str.append("<OPTION VALUE=\"\" "+((raceIDs.size()==0)?"SELECTED":"")+">");
                        for(Enumeration e=CMClass.races();e.hasMoreElements();)
                        {
                            Race R=(Race)e.nextElement();
                            str.append("<OPTION VALUE=\""+R.ID()+"\" "+((raceIDs.contains(R.ID().toUpperCase()))?"SELECTED":"")+">"+R.name());
                        }
                        str.append("</SELECT>");
                        return str.toString();
                    }
                    public String[] fakeUserInput(String oldVal) { 
                        Vector parsedVals = CMParms.parse(oldVal.toUpperCase());
                        if(parsedVals.size()==0)
                            return new String[]{""};
                        Vector races = new Vector();
                        for(int p=0;p<parsedVals.size();p++) {
                            Race R=CMClass.getRace((String)parsedVals.elementAt(p));
                            races.addElement(R.name());
                        }
                        for(int p=0;p<parsedVals.size();p++) {
                            Race R=CMClass.getRace((String)parsedVals.elementAt(p));
                            races.addElement(R.name());
                        }
                        races.addElement("");
                        return CMParms.toStringArray(races);
                    }
                    
                    public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
                    {
                        if((showFlag>0)&&(showFlag!=showNumber[0])) return oldVal;
                        String behave="NO";
                        String newVal = oldVal;
                        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
                        {
                            mob.tell(showNumber+". "+prompt()+": '"+newVal+"'.");
                            if((showFlag!=showNumber[0])&&(showFlag>-999)) return newVal;
                            Vector parsedVals = CMParms.parse(newVal.toUpperCase());
                            behave=mob.session().prompt("Enter a race to add/remove (?)\n\r:","");
                            if(behave.length()>0)
                            {
                                if(behave.equalsIgnoreCase("?"))
                                    mob.tell(CMLib.lister().reallyList(CMClass.races(),-1).toString());
                                else
                                {
                                    Race R=CMClass.getRace(behave);
                                    if(R!=null)
                                    {
                                        if(parsedVals.contains(R.ID().toUpperCase()))
                                        {
                                            mob.tell("'"+behave+"' removed.");
                                            parsedVals.remove(R.ID().toUpperCase().trim());
                                            newVal = CMParms.combine(parsedVals,0);
                                        }
                                        else
                                        {
                                            mob.tell(R.ID()+" added.");
                                            parsedVals.addElement(R.ID().toUpperCase());
                                            newVal = CMParms.combine(parsedVals,0);
                                        }
                                    }
                                    else
                                    {
                                        mob.tell("'"+behave+"' is not a recognized race.  Try '?'.");
                                    }
                                }
                            }
                            else
                                if(oldVal.equalsIgnoreCase(newVal))
                                    mob.tell("(no change)");
                        }
                        return newVal;
                    }
                },
                new AbilityParmEditorImpl("INSTRUMENT_TYPE","Instrmnt",PARMTYPE_CHOICES) {
                    public void createChoices() { createChoices(MusicalInstrument.TYPE_DESC); }
                    public int appliesToClass(Object o) { return (o instanceof MusicalInstrument)?5:-1;}
                    public String defaultValue(){ return "DRUMS";}
                },
                new AbilityParmEditorImpl("STONE_FLAG","Stone",PARMTYPE_CHOICES) {
                    public void createChoices() { createChoices(new String[]{"","STONE"});}
                    public String defaultValue(){ return "";}
                },
                new AbilityParmEditorImpl("POSE_NAME","Pose Word",PARMTYPE_ONEWORD) {
                    public void createChoices() {}
                    public String defaultValue(){ return "New Post";}
                },
                new AbilityParmEditorImpl("POSE_DESCRIPTION","Pose Description",PARMTYPE_STRING) {
                    public void createChoices() {}
                    public String defaultValue(){ return "<S-NAME> is standing here.";}
                },
                new AbilityParmEditorImpl("WOOD_METAL_CLOTH","",PARMTYPE_CHOICES) {
                    public void createChoices() { createChoices(new String[]{"WOOD","METAL","CLOTH"});}
                    public String defaultValue(){ return "WOOD";}
                },
                new AbilityParmEditorImpl("WEAPON_TYPE","W.Type",PARMTYPE_CHOICES) {
                    public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
                    public void createChoices() { createChoices(Weapon.TYPE_DESCS);}
                    public String defaultValue(){ return "BASHING";}
                },
                new AbilityParmEditorImpl("ATTACK_MODIFICATION","Att.",PARMTYPE_NUMBER) {
                    public void createChoices() {}
                    public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
                    public String defaultValue(){ return "0";}
                },
                new AbilityParmEditorImpl("N_A","N/A",PARMTYPE_STRING) {
                    public void createChoices() {}
                    public int appliesToClass(Object o) { return -1;}
                    public String defaultValue(){ return "";}
                    public boolean confirmValue(String oldVal) { return oldVal.trim().length()==0||oldVal.equals("0");}
                    public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
                    { return "";}
                    public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) { return ""; }
                },
                new AbilityParmEditorImpl("RESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED","Resrc/Amt",PARMTYPE_SPECIAL) {
                    public void createChoices() { 
                        createChoices(RawMaterial.CODES.NAMES()); 
                        choices().addElement("","");
                    }
                    public String defaultValue(){ return "";}
                    public int appliesToClass(Object o) { return 0;}
                    public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        if(httpReq.isRequestParameter(fieldName+"_RESOURCE"))
                        {
                            String rsc=httpReq.getRequestParameter(fieldName+"_RESOURCE");
                            String amt=httpReq.getRequestParameter(fieldName+"_AMOUNT");
                            if((rsc.trim().length()==0)||(rsc.equalsIgnoreCase("NOTHING"))||(CMath.s_int(amt)<=0))
                                return "";
                            return rsc+"/"+amt;
                        }
                        return oldVal;
                    }
                    public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
                        String value=webValue(httpReq,parms,oldVal,fieldName);
                        String rsc = "";
                        int amt = 0;
                        int x=value.indexOf('/');
                        if(x>0)
                        {
                            rsc = value.substring(0,x);
                            amt = CMath.s_int(value.substring(x+1));
                        }
                        StringBuffer str=new StringBuffer("");
                        str.append("\n\r<SELECT NAME="+fieldName+"_RESOURCE MULTIPLE>");
                        for(String S : RawMaterial.CODES.NAMES())
                            str.append("<OPTION VALUE=\""+S+"\" "
                                    +((S.equalsIgnoreCase(rsc))?"SELECTED":"")+">"
                                    +CMStrings.capitalizeAndLower(S));
                        str.append("</SELECT>");
                        str.append("&nbsp;&nbsp;Amount: ");
                        str.append("<INPUT TYPE=TEXT NAME="+fieldName+"_AMOUNT VALUE="+amt+">");
                        return str.toString();
                    }
                    public boolean confirmValue(String oldVal) { 
                        if(oldVal.trim().length()==0) return true;
                        oldVal=oldVal.trim();
                        int x=oldVal.indexOf('/');
                        if(x<0) return false;
                        if(!choices().getDimensionVector(1).contains(oldVal.substring(0,x)))
                            return false;
                        if(!CMath.isInteger(oldVal.substring(x+1)))
                            return false;
                        return true;
                    }
                    public String[] fakeUserInput(String oldVal) { 
                        int x=oldVal.indexOf('/');
                        if(x<=0) return new String[]{""};
                        return new String[]{oldVal.substring(0,x),oldVal.substring(x+1)};
                    }
                    public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
                    {
                        oldVal=oldVal.trim();
                        int x=oldVal.indexOf('/');
                        String oldRsc = "";
                        int oldAmt = 0;
                        if(x>0) {
                            oldRsc = oldVal.substring(0,x);
                            oldAmt = CMath.s_int(oldVal.substring(x));
                        }
                        oldRsc = CMLib.genEd().prompt(mob,oldRsc,++showNumber[0],showFlag,prompt(),choices());
                        if(oldRsc.length()>0)
                            return oldRsc+"/"+CMLib.genEd().prompt(mob,oldAmt,++showNumber[0],showFlag,prompt());
                        return "";
                    }
                },
                
        });
        DEFAULT_EDITORS = new Hashtable();
        for(int v=0;v<V.size();v++) {
            AbilityParmEditor A = (AbilityParmEditor)V.elementAt(v);
            DEFAULT_EDITORS.put(A.ID(),A);
        }
        return DEFAULT_EDITORS;
    };
    
    protected class AbilityRecipeDataImpl implements AbilityRecipeData 
    {
        private String recipeFilename;
        private String recipeFormat;
        private Vector columns;
        private Vector dataRows;
        private int numberOfDataColumns;
        public String[] columnHeaders;
        public int[] columnLengths;
        public int classFieldIndex;
        private String parseError = null;
        private boolean wasVFS = false;
        
		public AbilityRecipeDataImpl(String recipeFilename, String recipeFormat)
        {
            this.recipeFilename = recipeFilename;
            this.recipeFormat = recipeFormat;
            CMFile F = new CMFile(Resources.buildResourcePath("skills")+recipeFilename,null,true);
            wasVFS=F.isVFSFile();
            StringBuffer str=F.text();
            columns = parseRecipeFormatColumns(recipeFormat);
            numberOfDataColumns = 0;
            for(int c = 0; c < columns.size(); c++)
                if(columns.elementAt(c) instanceof Vector)
                    numberOfDataColumns++;
            dataRows = null;
            try {
                dataRows = parseDataRows(str,columns,numberOfDataColumns);
                DVector editRow = new DVector(2);
                for(int c=0;c<columns().size();c++)
                    if(columns().elementAt(c) instanceof Vector)
                        editRow.addElement((Vector)columns().elementAt(c),"");
                classFieldIndex = CMAbleParms.getClassFieldIndex(editRow);
                fixDataColumns(dataRows);
            } catch(CMException e) {
                parseError = e.getMessage();
                return;
            }
            columnLengths = new int[numberOfDataColumns];
            columnHeaders = new String[numberOfDataColumns];
            calculateRecipeCols(columnLengths,columnHeaders,dataRows);
        }
        public boolean wasVFS(){ return wasVFS;}
        public DVector newRow(String classFieldData)
        {
            DVector editRow = blankRow();
            int keyIndex =classFieldIndex;
            if((keyIndex>=0)&&(classFieldData!=null)) {
                editRow.setElementAt(keyIndex,2,classFieldData);
            }
            try {
                fixDataColumn(editRow,-1);
            } catch(CMException cme) { return null;}
            for(int i=0;i<editRow.size();i++)
                if(i!=keyIndex)
                {
                    AbilityParmEditor A = (AbilityParmEditor)getEditors().get((String)editRow.elementAt(i,1));
                    editRow.setElementAt(i,2,A.defaultValue());
                }
            return editRow;
        }
		public DVector blankRow() {
            DVector editRow = new DVector(2);
            for(int c=0;c<columns().size();c++)
                if(columns().elementAt(c) instanceof Vector)
                    editRow.addElement((Vector)columns().elementAt(c),"");
            return editRow;
        }
        public int getClassFieldIndex() { return classFieldIndex;}
        public String recipeFilename(){ return recipeFilename;}
        public String recipeFormat(){ return recipeFormat;}
        public Vector dataRows() { return dataRows;}
        public Vector columns() { return columns;}
        public int[] columnLengths() { return columnLengths;}
        public String[] columnHeaders(){ return columnHeaders;}
        public int numberOfDataColumns(){ return numberOfDataColumns;}
        public String parseError(){ return parseError;}
    }
    protected abstract class AbilityParmEditorImpl implements AbilityParmEditor 
    {
        private String ID;
        private DVector choices = null;
        private int fieldType;
        private String prompt = null;
        private String header = null;
        
        public AbilityParmEditorImpl(String fieldName, String shortHeader, int type) {
            ID=fieldName; 
            fieldType = type;
            header = shortHeader;
            prompt = CMStrings.capitalizeAndLower(CMStrings.replaceAll(ID,"_"," "));
            createChoices();
        }
        public String ID(){ return ID;}
        public int parmType(){ return fieldType;}
        public String prompt() { return prompt; }
        public String colHeader() { return header;}
        
        public boolean confirmValue(String oldVal)
        {
            boolean spaceOK = fieldType != PARMTYPE_ONEWORD;
            boolean emptyOK = false;
            switch(fieldType) {
            case PARMTYPE_STRINGORNULL:
                emptyOK = true;
            case PARMTYPE_ONEWORD:
            case PARMTYPE_STRING:
            {
                if((!spaceOK) && (oldVal.indexOf(' ') >= 0))
                    return false;
                return (emptyOK)||(oldVal.trim().length()>0);
            }
            case PARMTYPE_NUMBER:
                return CMath.isInteger(oldVal);
            case PARMTYPE_CHOICES:
                if(!choices.getDimensionVector(1).contains(oldVal))
                    return choices.getDimensionVector(1).contains(oldVal.toUpperCase().trim());
                return true;
            case PARMTYPE_MULTICHOICES:
                return CMath.isInteger(oldVal)||choices().contains(oldVal);
            }
            return false;
        }
		public String[] fakeUserInput(String oldVal) {
            boolean emptyOK = false;
            switch(fieldType) {
            case PARMTYPE_STRINGORNULL:
                emptyOK = true;
            case PARMTYPE_ONEWORD:
            case PARMTYPE_STRING:
            {
                if(emptyOK && (oldVal.trim().length()==0))
                    return new String[]{"NULL"};
                return new String[]{oldVal};
            }
            case PARMTYPE_NUMBER:
                return new String[]{oldVal};
            case PARMTYPE_CHOICES:
            {
                if(oldVal.trim().length()==0) return new String[]{"NULL"};
                Vector V = choices.getDimensionVector(1);
                for(int v=0;v<V.size();v++)
                    if(oldVal.equalsIgnoreCase((String)V.elementAt(v)))
                        return new String[]{(String)choices.elementAt(v,2)};
                return new String[]{oldVal};
            }
            case PARMTYPE_MULTICHOICES:
                if(oldVal.trim().length()==0) return new String[]{"NULL"};
                if(!CMath.isInteger(oldVal))
                {
                    Vector V = (Vector)choices.getDimensionVector(1);
                    for(int v=0;v<V.size();v++)
                        if(oldVal.equalsIgnoreCase((String)V.elementAt(v)))
                            return new String[]{(String)choices.elementAt(v,2),""};
                } else {
                    Vector V = new Vector();
                    for(int c=0;c<choices.size();c++)
                        if(CMath.bset(CMath.s_int(oldVal),((Integer)choices.elementAt(c,1)).intValue()))
                        {
                            V.addElement((String)choices.elementAt(c,2));
                            V.addElement((String)choices.elementAt(c,2));
                        }
                    if(V.size()>0)
                    {
                        V.addElement("");
                        return CMParms.toStringArray(V);
                    }
                }
                return new String[]{"NULL"};
            }
            return new String[]{};
        }
        
        public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag)
        throws java.io.IOException
        {
            String str = null;
            boolean emptyOK = false;
            boolean spaceOK = fieldType != PARMTYPE_ONEWORD;
            switch(fieldType) {
            case PARMTYPE_STRINGORNULL:
                emptyOK = true;
            case PARMTYPE_ONEWORD:
            case PARMTYPE_STRING:
            {
                ++showNumber[0];
                boolean proceed = true;
                while(proceed) {
                    str = CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),emptyOK).trim();
                    if((!spaceOK) && (str.indexOf(' ') >= 0))
                        mob.tell("Spaces are not allowed here.");
                    else
                        proceed=false;
                }
                break;
            }
            case PARMTYPE_NUMBER:
                str = Integer.toString(CMLib.genEd().prompt(mob,Integer.parseInt(oldVal),++showNumber[0],showFlag,prompt()));
                break;
            case PARMTYPE_CHOICES:
                str = CMLib.genEd().promptMultiOrExtra(mob,oldVal,++showNumber[0],showFlag,prompt(),choices);
                break;
            case PARMTYPE_MULTICHOICES:
                str = CMLib.genEd().promptMultiOrExtra(mob,oldVal,++showNumber[0],showFlag,prompt(),choices);
                if(CMath.isInteger(str))
                    str = Integer.toString(CMath.s_int(str));
                break;
            }
            return str;
        }
        
        public String webValue(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
            String webValue = httpReq.getRequestParameter(fieldName);
            switch(fieldType) {
            case PARMTYPE_ONEWORD:
            case PARMTYPE_STRINGORNULL:
            case PARMTYPE_STRING:
            case PARMTYPE_NUMBER:
                return (webValue == null)?oldVal:webValue;
            case PARMTYPE_MULTICHOICES:
            {
                if(webValue == null) return oldVal;
                String id="";
                int num=0;
                for(;httpReq.isRequestParameter(fieldName+id);id=""+(++num))
                {
                    String newVal = httpReq.getRequestParameter(fieldName+id); 
                    if(CMath.s_int(newVal)<=0)
                        return newVal;
                    num |= CMath.s_int(newVal);
                }
                return ""+num;
            }
            case PARMTYPE_CHOICES:
                return (webValue == null)?oldVal:webValue;
            }
            return "";
        }
        
        public String webTableField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal) { return oldVal; }
        
        public String webField(ExternalHTTPRequests httpReq, Hashtable parms, String oldVal, String fieldName) {
            int textSize = 50;
            String webValue = webValue(httpReq,parms,oldVal,fieldName);
            String onChange = null;
            Vector choiceValues = new Vector();
            switch(fieldType) {
            case PARMTYPE_ONEWORD:
                textSize = 10;
            case PARMTYPE_STRINGORNULL:
            case PARMTYPE_STRING:
                return "\n\r<INPUT TYPE=TEXT NAME=" + fieldName + " SIZE=" + textSize + " VALUE=\"" + webValue + "\">";
            case PARMTYPE_NUMBER:
                return "\n\r<INPUT TYPE=TEXT NAME=" + fieldName + " SIZE=10 VALUE=\"" + webValue + "\">";
            case PARMTYPE_MULTICHOICES:
            {
                onChange = " MULTIPLE ";
                if(!parms.containsKey("NOSELECT"))
                    onChange+= "ONCHANGE=\"MultiSelect(this);\"";
                if(CMath.isInteger(webValue))
                {
                    int bits = CMath.s_int(webValue);
                    for(int i=0;i<choices.size();i++)
                    {
                        int bitVal =CMath.s_int((String)choices.elementAt(i,1)); 
                        if((bitVal>0)&&(CMath.bset(bits,bitVal)))
                            choiceValues.addElement((String)choices.elementAt(i,1));
                    }
                }
            }
            case PARMTYPE_CHOICES:
            {
                if(choiceValues.size()==0)
                    choiceValues.addElement(webValue);
                if((onChange == null)&&(!parms.containsKey("NOSELECT")))
                    onChange = " ONCHANGE=\"Select(this);\"";
                else
                if(onChange==null)
                    onChange="";
                StringBuffer str= new StringBuffer("");
                str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
                for(int i=0;i<choices.size();i++)
                {
                    String option = ((String)choices.elementAt(i,1));
                    str.append("<OPTION VALUE=\""+option+"\" ");
                    for(int c=0;c<choiceValues.size();c++)
                        if(option.equalsIgnoreCase((String)choiceValues.elementAt(c)))
                            str.append("SELECTED");
                    str.append(">"+((String)choices.elementAt(i,2)));
                }
                return str.toString()+"</SELECT>";
            }
            }
            return "";
        }
        
        public abstract void createChoices(); 
        public DVector createChoices(Enumeration e) {
            if(choices != null) return choices;
            choices = new DVector(2);
            Object o = null;
            for(;e.hasMoreElements();) {
                o = e.nextElement();
                if(o instanceof String)
                    choices.addElement(o,CMStrings.capitalizeAndLower((String)o));
                else
                    if(o instanceof Ability)
                        choices.addElement(((Ability)o).ID(),((Ability)o).name());
                    else
                        if(o instanceof Race)
                            choices.addElement(((Race)o).ID(),((Race)o).name());
                        else
                            if(o instanceof Environmental)
                                choices.addElement(((Environmental)o).ID(),((Environmental)o).ID());
            }
            return choices;
        }
        public DVector createChoices(Vector V) { return createChoices(V.elements());}
        public DVector createChoices(String[] S) { return createChoices(CMParms.makeVector(S).elements());}
        public DVector createBinaryChoices(String[] S) { 
            if(choices != null) return choices;
            choices = createChoices(CMParms.makeVector(S).elements());
            for(int i=0;i<choices.size();i++)
                if(i==0)
                    choices.setElementAt(i,1,Integer.toString(0));
                else
                    choices.setElementAt(i,1,Integer.toString(1<<(i-1)));
            return choices;
        }
        public DVector createNumberedChoices(String[] S) { 
            if(choices != null) return choices;
            choices = createChoices(CMParms.makeVector(S).elements());
            for(int i=0;i<choices.size();i++)
                choices.setElementAt(i,1,Integer.toString(i));
            return choices;
        }
        public DVector choices() { return choices; } 
        public int appliesToClass(Object o) { return 0;}
    }
    
}