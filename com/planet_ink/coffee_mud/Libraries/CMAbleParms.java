
package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2012 Bo Zimmerman

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
public class CMAbleParms extends StdLibrary implements AbilityParameters
{
	public String ID(){return "CMAbleParms";}

	protected Map<String,AbilityParmEditor> DEFAULT_EDITORS = null;

	protected final static int[] ALL_BUCKET_MATERIAL_CHOICES = new int[]{RawMaterial.MATERIAL_CLOTH, RawMaterial.MATERIAL_METAL, RawMaterial.MATERIAL_LEATHER, 
			RawMaterial.MATERIAL_LIQUID, RawMaterial.MATERIAL_WOODEN, RawMaterial.MATERIAL_PRECIOUS,RawMaterial.MATERIAL_VEGETATION, RawMaterial.MATERIAL_ROCK };
	protected final static int[] ALLOWED_BUCKET_ACODES = new int[]{Ability.ACODE_CHANT,Ability.ACODE_SPELL,Ability.ACODE_PRAYER,Ability.ACODE_SONG,Ability.ACODE_SKILL,
			Ability.ACODE_THIEF_SKILL}; 
	protected final static int[] ALLOWED_BUCKET_QUALITIES = new int[]{Ability.QUALITY_BENEFICIAL_OTHERS,Ability.QUALITY_BENEFICIAL_SELF,Ability.QUALITY_INDIFFERENT,
			Ability.QUALITY_OK_OTHERS,Ability.QUALITY_OK_SELF};
	protected final static String[] ADJUSTER_TOKENS = new String[]{"+","-","="};
	protected final static String[] RESISTER_IMMUNER_TOKENS = new String[]{"%",";"};

	public CMAbleParms()
	{
		super();
	}
	

	public String encodeCodedSpells(Affectable I)
	{
		final StringBuilder str=new StringBuilder("");
		for(Enumeration<Ability> a=I.effects();a.hasMoreElements();)
		{
			Ability A=a.nextElement();
			if(A.text().indexOf(";")>0)
				return A.ID()+";"+A.text();
			if(str.length()>0) str.append(";");
			str.append(A.ID());
			if(A.text().length()>0) 
				str.append(";").append(A.text());
		}
		return str.toString();
	}

	public List<Ability> getCodedSpells(String spells)
	{
		Vector<Ability> spellsV=new Vector<Ability>(); 
		if(spells.length()==0) return spellsV;
		if(spells.startsWith("*"))
		{
			spells=spells.substring(1);
			int x=spells.indexOf(';');
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
		Vector<String> V=CMParms.parseSemicolons(spells,true);
		Ability lastSpell=null;
		Ability A=null;
		for(int v=0;v<V.size();v++)
		{
			spells=V.elementAt(v); 
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
		int colon=misctype.indexOf(':');
		if(colon>=0)
		{
			String layers=misctype.substring(0,colon).toUpperCase().trim();
			misctype=misctype.substring(colon+1).trim();
			if((layers.startsWith("MS"))||(layers.startsWith("SM")))
			{ 
				layers=layers.substring(2); 
				layerAtt[0]=Armor.LAYERMASK_MULTIWEAR|Armor.LAYERMASK_SEETHROUGH;
			}
			else
			if(layers.startsWith("M"))
			{ 
				layers=layers.substring(1); 
				layerAtt[0]=Armor.LAYERMASK_MULTIWEAR;
			}
			else
			if(layers.startsWith("S"))
			{ 
				layers=layers.substring(1); 
				layerAtt[0]=Armor.LAYERMASK_SEETHROUGH;
			}
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

	public Vector<Object> parseRecipeFormatColumns(String recipeFormat) 
	{
		char C = '\0';
		StringBuffer currentColumn = new StringBuffer("");
		Vector<String> currentColumns = null;
		char[] recipeFmtC = recipeFormat.toCharArray();
		Vector<Object> columnsV = new Vector<Object>();
		for(int c=0;c<=recipeFmtC.length;c++) 
		{
			if(c==recipeFmtC.length) 
			{
				break;
			}
			C = recipeFmtC[c];
			if((C=='|')
			&&(c<(recipeFmtC.length-1))
			&&(recipeFmtC[c+1]=='|')
			&&(currentColumn.length()>0))
			{
				if(currentColumn.length()>0) 
				{
					if(currentColumns == null) 
					{
						currentColumns = new Vector<String>();
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
				if(currentColumn.length()>0)
				{
					if(currentColumns == null)
					{
						currentColumns = new Vector<String>();
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
			if(currentColumns == null) 
			{
				currentColumns = new Vector<String>();
				columnsV.addElement(currentColumns);
			}
			currentColumns.addElement(currentColumn.toString());
		}
		if((currentColumns != null) && (currentColumns.size()==0))
			columnsV.remove(currentColumns);
		return columnsV;
	}

	@SuppressWarnings("unchecked")
    public String makeRecipeFromItem(final ItemCraftor C, final Item I) throws CMException
	{
		Vector<Object> columns = parseRecipeFormatColumns(C.parametersFormat());
		Map<String,AbilityParmEditor> editors = this.getEditors();
		StringBuilder recipe = new StringBuilder("");
		for(int d=0;d<columns.size();d++) 
		{
			if(columns.get(d) instanceof String)
			{
				String name = (String)columns.get( d );
				AbilityParmEditor A = (AbilityParmEditor)editors.get(columns.get(d));
				if((A==null)||(name.length()<3))
				{
					recipe.append("\t");
					continue;
				}
				if(A.appliesToClass(I)<0)
					A = (AbilityParmEditor)editors.get("N_A");
				columns.set(d,A.ID());
			}
			else
			if(columns.get(d) instanceof List)
			{
				AbilityParmEditor applicableA = null;
				List<AbilityParmEditor> colV=(List<AbilityParmEditor>)columns.get(d);
				for(int c=0;c<colV.size();c++)
				{
					AbilityParmEditor A = (AbilityParmEditor)editors.get(colV.get(c));
					if(A==null) 
						throw new CMException("Column name "+(colV.get(c))+" is not found.");
					if((applicableA==null)
							||(A.appliesToClass(I) > applicableA.appliesToClass(I)))
						applicableA = A;
				}
				if((applicableA == null)||(applicableA.appliesToClass(I)<0))
					applicableA = (AbilityParmEditor)editors.get("N_A");
				columns.set(d,applicableA.ID());
			}
			else
				throw new CMException("Col name "+(columns.get(d))+" is not a String or List.");
			AbilityParmEditor A = (AbilityParmEditor)editors.get((String)columns.get(d));
			if(A==null)
				throw new CMException("Editor name "+(columns.get(d))+" is not defined.");
			recipe.append(A.convertFromItem(C, I));
		}
		return recipe.toString();
	}
	
	@SuppressWarnings("unchecked")
    protected static int getClassFieldIndex(DVector dataRow)
	{
		for(int d=0;d<dataRow.size();d++)
			if(dataRow.elementAt(d,1) instanceof List) 
			{
				List<String> V=(List<String>)dataRow.elementAt(d,1);
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

	@SuppressWarnings("unchecked")
    protected Item getSampleItem(DVector dataRow)
	{
		boolean classIDRequired = false;
		String classID = null;
		int fieldIndex = getClassFieldIndex(dataRow);
		for(int d=0;d<dataRow.size();d++)
			if((dataRow.elementAt(d,1) instanceof List) 
			&&(!classIDRequired)
			&&(((List<String>)dataRow.elementAt(d,1)).size()>1))
				classIDRequired=true;
		if(fieldIndex >=0)
			classID=(String)dataRow.elementAt(fieldIndex,2);
		if((classID!=null)&&(classID.length()>0))
		{
			if(classID.equalsIgnoreCase("FOOD"))
				return CMClass.getItemPrototype("GenFood");
			else
			if(classID.equalsIgnoreCase("SOAP"))
				return CMClass.getItemPrototype("GenItem");
			else
			if(classID.equalsIgnoreCase("DRINK"))
				return CMClass.getItemPrototype("GenDrink");
			else
				return CMClass.getItemPrototype(classID);
		}
		if(classIDRequired)
			return null;
		return CMClass.getItemPrototype("StdItem");
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

	@SuppressWarnings("unchecked")
    protected Vector<DVector> parseDataRows(StringBuffer recipeData, Vector<? extends Object> columnsV, int numberOfDataColumns)
		throws CMException
	{
		StringBuffer str = new StringBuffer(recipeData.toString());
		str = cleanDataRowEOLs(str);
		Vector<DVector> rowsV = new Vector<DVector>();
		DVector dataRow = new DVector(2);
		List<String> currCol = null;
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
				if(columnsV.elementAt(c) instanceof List)
				{
					currCol = (List<String>)columnsV.elementAt(c);
					if(c<columnsV.size()-1)
					{
						div = (String)columnsV.elementAt(c+1);
						c++;
					}
				}
				if((str.length()==0)&&(c<columnsV.size()-1))
					break;
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
		return fixDataColumn(dataRow,rowShow,classModelI);
	}
	
	@SuppressWarnings("unchecked")
    protected boolean fixDataColumn(DVector dataRow, int rowShow, final Item classModelI) throws CMException
	{
		Map<String,AbilityParmEditor> editors = getEditors();
		if(classModelI == null) {
			Log.errOut("CMAbleParms","Data row "+rowShow+" discarded due to null/empty classID");
			return false;
		} 
		for(int d=0;d<dataRow.size();d++) 
		{
			List<String> colV=(List<String>)dataRow.elementAt(d,1);
			if(colV.size()==1)
			{
				AbilityParmEditor A = (AbilityParmEditor)editors.get(colV.get(0));
				if((A == null)||(A.appliesToClass(classModelI)<0))
					A = (AbilityParmEditor)editors.get("N_A");
				dataRow.setElementAt(d,1,A.ID());
			}
			else
			{
				AbilityParmEditor applicableA = null;
				for(int c=0;c<colV.size();c++)
				{
					AbilityParmEditor A = (AbilityParmEditor)editors.get(colV.get(c));
					if(A==null) 
						throw new CMException("Col name "+(colV.get(c))+" is not defined.");
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

	protected void fixDataColumns(Vector<DVector> rowsV) throws CMException
	{
		DVector dataRow = new DVector(2);
		for(int r=0;r<rowsV.size();r++) 
		{
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

	public void testRecipeParsing(StringBuffer recipesString, String recipeFormat) throws CMException
	{
		testRecipeParsing(recipesString,recipeFormat,null);
	}
	
	public void testRecipeParsing(String recipeFilename, String recipeFormat, boolean save)
	{
		StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+recipeFilename,null,true).text();
		try
		{
			testRecipeParsing(str,recipeFormat,save?recipeFilename:null);
		} catch(CMException e) {
			
			Log.errOut("CMAbleParms","File: "+recipeFilename+": "+e.getMessage());
			return;
		}
	}
	
	@SuppressWarnings("unchecked")
    public void testRecipeParsing(StringBuffer str, String recipeFormat, String saveRecipeFilename) throws CMException
	{
		Vector<? extends Object> columnsV = parseRecipeFormatColumns(recipeFormat);
		int numberOfDataColumns = 0;
		for(int c = 0; c < columnsV.size(); c++)
			if(columnsV.elementAt(c) instanceof List)
				numberOfDataColumns++;
		Vector<DVector> rowsV = parseDataRows(str,columnsV,numberOfDataColumns);
		Vector<String> convertedColumnsV=(Vector<String>)columnsV;
		fixDataColumns(rowsV);
		Map<String,AbilityParmEditor> editors = getEditors();
		DVector editRow = null;
		int[] showNumber = {0};
		int showFlag =-999;
		MOB mob=CMClass.getFactoryMOB();
		Session fakeSession = (Session)CMClass.getCommon("FakeSession");
		mob.setSession(fakeSession);
		fakeSession.setMob(mob);
		for(int r=0;r<rowsV.size();r++)
		{
			editRow = (DVector)rowsV.elementAt(r);
			for(int a=0;a<editRow.size();a++)
			{
				AbilityParmEditor A = (AbilityParmEditor)editors.get((String)editRow.elementAt(a,1));
				try
				{ 
					String oldVal = (String)editRow.elementAt(a,2);
					fakeSession.previousCMD().clear();
					fakeSession.previousCMD().addAll(new XVector<String>(A.fakeUserInput(oldVal)));
					String newVal = A.commandLinePrompt(mob,oldVal,showNumber,showFlag);
					editRow.setElementAt(a,2,newVal);
				} catch(Exception e) {}
			}
		}
		fakeSession.setMob(null);
		mob.destroy();
		if(saveRecipeFilename!=null)
			resaveRecipeFile(mob,saveRecipeFilename,rowsV,convertedColumnsV,false);
	}

	protected void calculateRecipeCols(int[] lengths, String[] headers, Vector<DVector> rowsV)
	{
		Map<String,AbilityParmEditor> editors = getEditors();
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
				if((!headers[c].startsWith("#"))&&(!headers[c].equalsIgnoreCase(A.colHeader())))
				{
					headers[c]="#"+c;
					lengths[c]=headers[c].length();
				}
			}
		}
		for(int i=0;i<headers.length;i++)
			if(headers[i]==null)
				headers[i]="*Add*";
		int currLenTotal = 0;
		for(int l=0;l<lengths.length;l++)
			currLenTotal+=lengths[l];
		int curCol = 0;
		while((currLenTotal+lengths.length)>72) 
		{
			if(lengths[curCol]>1)
			{
				lengths[curCol]--;
				currLenTotal--;
			}
			curCol++;
			if(curCol >= lengths.length)
				curCol = 0;
		}
		while((currLenTotal+lengths.length)<72) 
		{
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
		for(int r=0;r<recipe.dataRows().size();r++) 
		{
			dataRow=(DVector)recipe.dataRows().elementAt(r);
			list.append(CMStrings.padRight(""+(r+1),3)+" ");
			for(int c=0;c<dataRow.size();c++)
				list.append(CMStrings.padRight(CMStrings.limit((String)dataRow.elementAt(c,2),recipe.columnLengths()[c]),recipe.columnLengths()[c])+" ");
			list.append("\n\r");
		}
		return list;
	}

	@SuppressWarnings("unchecked")
    public void modifyRecipesList(MOB mob, String recipeFilename, String recipeFormat) throws java.io.IOException
	{
		Map<String,AbilityParmEditor> editors = getEditors();
		AbilityRecipeData recipe = parseRecipe(recipeFilename, recipeFormat);
		if(recipe.parseError() != null)
		{
			Log.errOut("CMAbleParms","File: "+recipeFilename+": "+recipe.parseError());
			return;
		}
		while((mob.session()!=null)&&(!mob.session().isStopped()))
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
					AbilityParmEditor A = (AbilityParmEditor)editors.get(((List<String>)editRow.elementAt(keyIndex,1)).get(0));
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
		if((mob.session()!=null)&&(!mob.session().isStopped()))
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

	public void resaveRecipeFile(MOB mob, String recipeFilename, Vector<DVector> rowsV, Vector<String> columnsV, boolean saveToVFS)
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
			Resources.removeResource("PARSED_RECIPE: "+recipeFilename);
			Resources.removeMultiLists(recipeFilename);
		}
	}
	
	protected static int getAppropriateResourceBucket(final Item I, final Object A)
	{
		final int myMaterial = ((I.material() & RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL) ? RawMaterial.MATERIAL_METAL : (I.material() & RawMaterial.MATERIAL_MASK);
		if(A instanceof Behavior)
			return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_LEATHER, RawMaterial.MATERIAL_VEGETATION}, myMaterial );
		if(A instanceof Ability)
		{
			switch(((Ability)A).abilityCode() & Ability.ALL_ACODES)
			{
				case Ability.ACODE_CHANT: 
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_VEGETATION, RawMaterial.MATERIAL_ROCK}, myMaterial );
				case Ability.ACODE_SPELL: 
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_WOODEN, RawMaterial.MATERIAL_PRECIOUS}, myMaterial );
				case Ability.ACODE_PRAYER: 
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_METAL, RawMaterial.MATERIAL_ROCK}, myMaterial );
				case Ability.ACODE_SONG: 
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_LIQUID, RawMaterial.MATERIAL_WOODEN}, myMaterial );
				case Ability.ACODE_THIEF_SKILL:
				case Ability.ACODE_SKILL: 
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_CLOTH, RawMaterial.MATERIAL_METAL}, myMaterial );
				case Ability.ACODE_PROPERTY:
					if(A instanceof TriggeredAffect)
						return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_PRECIOUS, RawMaterial.MATERIAL_METAL}, myMaterial );
					break;
				default:
					break;
			}
		}
		return CMLib.dice().pick( ALL_BUCKET_MATERIAL_CHOICES, myMaterial );
	}

	protected static boolean isResourceCodeRoomMapped(final int resourceCode)
	{
		final Integer I=Integer.valueOf(resourceCode);
		for(Enumeration<Room> e=CMClass.locales();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(!(R instanceof GridLocale))
				if((R.resourceChoices()!=null)&&(R.resourceChoices().contains(I)))
					return true;
		}
		return false;
	}
	
	protected static void addExtraMaterial(final Map<Integer,int[]> extraMatsM, final Item I, final Object A, double weight)
	{
		int times = 1;
		if(weight >= 1.0)
		{
			times = (int)Math.round(weight / 1.0);
			weight=.99;
		}
		final int myBucket = getAppropriateResourceBucket(I,A);
		if(myBucket != RawMaterial.RESOURCE_NOTHING)
		{
			final SPairList<Integer,Double> bucket = RawMaterial.CODES.instance().getValueSortedBucket(myBucket);
			Integer resourceCode = (bucket.size()==0) 
					? Integer.valueOf(CMLib.dice().pick( RawMaterial.CODES.ALL(), I.material() )) 
					: bucket.get( (weight>=.99) ? bucket.size()-1 : 0 ).first;
			for(Iterator<Pair<Integer,Double>> b = bucket.iterator(); b.hasNext();)
			{
				final Pair<Integer,Double> p = b.next();
				if((weight <= p.second.doubleValue())&&(isResourceCodeRoomMapped(p.first.intValue())))
				{
					resourceCode = p.first;
					break;
				}
			}
			int tries=100;
			while((--tries>0)&&(!isResourceCodeRoomMapped(resourceCode.intValue())))
				resourceCode=bucket.get(CMLib.dice().roll(1, bucket.size(), -1)).first;
			resourceCode = Integer.valueOf( resourceCode.intValue() );
			for(int x=0;x<times;x++)
			{
				int[] amt = extraMatsM.get( resourceCode );
				if(amt == null)
					extraMatsM.put( resourceCode, new int[]{1} );
				else
					amt[0]++;
			}
		}
	}
	
	protected static void addExtraAbilityMaterial(final Map<Integer,int[]> extraMatsM, final Item I, final Ability A)
	{
		double level = (double)CMLib.ableMapper().lowestQualifyingLevel( A.ID() );
		if( level <= 0.0 )
		{
			level = (double)I.basePhyStats().level();
			if( level <= 0.0 ) level = 1.0;
			addExtraMaterial(extraMatsM, I, A, CMath.div( level, CMProps.getIntVar( CMProps.SYSTEMI_LASTPLAYERLEVEL ) ));
		}
		else
		{
			double levelCap = (double)CMLib.ableMapper().getCalculatedMedianLowestQualifyingLevel();
			addExtraMaterial(extraMatsM, I, A, CMath.div(level , ( levelCap * 2.0)));
		}
	}

	public static Map<Integer,int[]> extraMaterial(final Item I)
	{
		Map<Integer,int[]> extraMatsM=new TreeMap<Integer,int[]>();
		/*
		 * behaviors/properties of the item
		 */
		for(Enumeration<Ability> a=I.effects(); a.hasMoreElements();)
		{
			Ability A=a.nextElement();
			if(A.isSavable())
			{
				if((A.abilityCode() & Ability.ALL_ACODES) == Ability.ACODE_PROPERTY)
				{
					if(A instanceof AbilityContainer)
					{
						for(Enumeration<Ability> a1=((AbilityContainer)A).abilities(); a1.hasMoreElements(); )
						{
							addExtraAbilityMaterial(extraMatsM,I,a1.nextElement());
						}
					}
					if(A instanceof TriggeredAffect)
					{
						if((A.flags() & Ability.FLAG_ADJUSTER) != 0)
						{
							int count = CMStrings.countSubstrings( new String[]{A.text()}, ADJUSTER_TOKENS );
							if(count == 0) count = 1;
							for(int i=0;i<count;i++)
								addExtraAbilityMaterial(extraMatsM,I,A);
						}
						else
						if((A.flags() & (Ability.FLAG_RESISTER | Ability.FLAG_IMMUNER)) != 0)
						{
							int count = CMStrings.countSubstrings( new String[]{A.text()}, RESISTER_IMMUNER_TOKENS );
							if(count == 0) count = 1;
							for(int i=0;i<count;i++)
								addExtraAbilityMaterial(extraMatsM,I,A);
						}
					}
				}
				else
				if((CMParms.indexOf(ALLOWED_BUCKET_ACODES, A.abilityCode() & Ability.ALL_ACODES ) >=0)
				&&(CMParms.indexOf( ALLOWED_BUCKET_QUALITIES, A.abstractQuality()) >=0 ))
				{
					addExtraAbilityMaterial(extraMatsM,I,A);
				}
			}
		}
		for(Enumeration<Behavior> b=I.behaviors(); b.hasMoreElements();)
		{
			Behavior B=b.nextElement();
			if(B.isSavable())
			{
				addExtraMaterial(extraMatsM, I, B, CMath.div( CMProps.getIntVar( CMProps.SYSTEMI_LASTPLAYERLEVEL ), I.basePhyStats().level() ));
			}
		}
		return extraMatsM;
	}
	
	public synchronized Map<String,AbilityParmEditor> getEditors()
	{
		if(DEFAULT_EDITORS != null)
			return DEFAULT_EDITORS;
		
		Vector<AbilityParmEditorImpl> V=new XVector<AbilityParmEditorImpl>(new AbilityParmEditorImpl[] {
				new AbilityParmEditorImpl("SPELL_ID","The Spell ID",PARMTYPE_CHOICES) {
					public void createChoices() { createChoices(CMClass.abilities());}
					public String defaultValue(){ return "Spell_ID";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I instanceof Potion)
							return ((Potion)I).getSpellList();
						else
						if(I instanceof Scroll)
							return ((Scroll)I).getSpellList();
						return "";
					}
				},
				new AbilityParmEditorImpl("RESOURCE_NAME","Resource",PARMTYPE_CHOICES) {
					public void createChoices() { createChoices(RawMaterial.CODES.NAMES());}
					public String defaultValue(){ return "IRON";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{ 
						return RawMaterial.CODES.NAME(I.material());
					}
				},
				new AbilityParmEditorImpl("ITEM_NAME","Item Final Name",PARMTYPE_STRING){
					public void createChoices() {}
					public String defaultValue(){ return "Item Name";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						String oldName=I.Name();
						if(I.material()==RawMaterial.RESOURCE_GLASS)
							return CMLib.english().cleanArticles(oldName);
						
						String newName=oldName;
						List<String> V=CMParms.parseSpaces( oldName,true);
						for(int i=0;i<V.size();i++)
						{
							String s=V.get(i);
							int code=RawMaterial.CODES.FIND_IgnoreCase(s);
							if((code>0)&&(code==I.material()))
							{
								V.set(i, "%");
								if((i>0)&&(CMLib.english().isAnArticle(V.get(i-1))))
									V.remove(i-1);
								newName=CMParms.combine(V);
								break;
							}
						}
						if(oldName.equals(newName))
						{
							for(int i=0;i<V.size();i++)
							{
								String s=V.get(i);
								int code=RawMaterial.CODES.FIND_IgnoreCase(s);
								if(code>0)
								{
									V.set(i, "%");
									if((i>0)&&(CMLib.english().isAnArticle(V.get(i-1))))
										V.remove(i-1);
									newName=CMParms.combine(V);
									break;
								}
							}
						}
						if(newName.indexOf( '%' )<0)
						{
							for(int i=0;i<V.size()-1;i++)
								if(CMLib.english().isAnArticle( V.get( i ) ))
								{
									if(i==0)
										V.set( i, "%" );
									else
										V.add(i+1, "%");
									break;
								}
							newName=CMParms.combine( V );
						}
						if(newName.indexOf( '%' )<0)
						{
							newName="% "+newName;
						}
						return newName;
					}
				}, 
				new AbilityParmEditorImpl("ITEM_LEVEL","Lvl",PARMTYPE_NUMBER){
					public void createChoices() {}
					public String defaultValue(){ return "1";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if((I instanceof Weapon)||(I instanceof Armor))
						{
							int timsLevel = CMLib.itemBuilder().timsLevelCalculator(I);
							if((timsLevel > I.basePhyStats().level() ) && (timsLevel < CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)))
								return ""+timsLevel;
						}
						return ""+I.basePhyStats().level();
					}
				},
				new AbilityParmEditorImpl("BUILD_TIME_TICKS","Time",PARMTYPE_NUMBER){
					public void createChoices() {}
					public String defaultValue(){ return "20";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{ 
						return ""+(10 + (I.basePhyStats().level()/2));
					}
				},
				new AbilityParmEditorImpl("AMOUNT_MATERIAL_REQUIRED","Amt",PARMTYPE_NUMBER){
					public void createChoices() {}
					public boolean confirmValue(String oldVal) { return true;}
					public String defaultValue(){ return "10";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{ 
						return ""+Math.round(CMath.mul(I.basePhyStats().weight(),(A!=null)?A.getItemWeightMultiplier(false):1.0));
					}
				},
				new AbilityParmEditorImpl("MATERIALS_REQUIRED","Amount/Cmp",PARMTYPE_SPECIAL){
					public void createChoices() {}
					public boolean confirmValue(String oldVal) { return true;}
					public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						if(httpReq.isRequestParameter(fieldName+"_WHICH"))
						{
							String which=httpReq.getRequestParameter(fieldName+"_WHICH");
							if((which.trim().length()==0)||(which.trim().equalsIgnoreCase("AMOUNT")))
								return httpReq.getRequestParameter(fieldName+"_AMOUNT");
							if(which.trim().equalsIgnoreCase("COMPONENT"))
								return httpReq.getRequestParameter(fieldName+"_COMPONENT");
							int x=1;
							List<AbilityComponent> comps=new Vector<AbilityComponent>();
							while(httpReq.isRequestParameter(fieldName+"_CUST_TYPE_"+x))
							{
								String connector=httpReq.getRequestParameter(fieldName+"_CUST_CONN_"+x);
								String amt=httpReq.getRequestParameter(fieldName+"_CUST_AMT_"+x);
								String strVal=httpReq.getRequestParameter(fieldName+"_CUST_STR_"+x);
								String loc=httpReq.getRequestParameter(fieldName+"_CUST_LOC_"+x);
								String typ=httpReq.getRequestParameter(fieldName+"_CUST_TYPE_"+x);
								String con=httpReq.getRequestParameter(fieldName+"_CUST_CON_"+x);
								if(connector==null) connector="AND";
								if(connector.equalsIgnoreCase("DEL")||(connector.length()==0)){x++; continue;}
								try
								{
									AbilityComponent able=CMLib.ableMapper().createBlankAbilityComponent();
									able.setConnector(AbilityComponent.CompConnector.valueOf(connector));
									able.setAmount(CMath.s_int(amt));
									able.setMask("");
									able.setConsumed((con!=null) && con.equalsIgnoreCase("on"));
									able.setLocation(AbilityComponent.CompLocation.valueOf(loc));
									able.setType(AbilityComponent.CompType.valueOf(typ), strVal);
									comps.add(able);
								}
								catch(Exception e){}
								x++;
							}
							if(comps.size()>0)
								return CMLib.ableMapper().getAbilityComponentCodedString(comps);
						}
						return oldVal;
					}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{ 
						int amt=(int)Math.round(CMath.mul(I.basePhyStats().weight()-1,(A!=null)?A.getItemWeightMultiplier(false):1.0));
						if(amt<1) amt=1;
						Map<Integer,int[]> extraMatsM = CMAbleParms.extraMaterial( I );
						if((extraMatsM == null) || (extraMatsM.size()==0))
						{
							return ""+amt;
						}
						List<AbilityComponent> comps=new Vector<AbilityComponent>();
						AbilityComponent able=CMLib.ableMapper().createBlankAbilityComponent();
						able.setConnector(AbilityComponent.CompConnector.AND);
						able.setAmount(amt);
						able.setMask("");
						able.setConsumed(true);
						able.setLocation(AbilityComponent.CompLocation.ONGROUND);
						able.setType(AbilityComponent.CompType.MATERIAL, Integer.valueOf(I.material() & RawMaterial.MATERIAL_MASK));
						comps.add(able);
						for(Integer resourceCode : extraMatsM.keySet())
						{
							able=CMLib.ableMapper().createBlankAbilityComponent();
							able.setConnector(AbilityComponent.CompConnector.AND);
							able.setAmount(extraMatsM.get(resourceCode)[0]);
							able.setMask("");
							able.setConsumed(true);
							able.setLocation(AbilityComponent.CompLocation.ONGROUND);
							able.setType(AbilityComponent.CompType.RESOURCE, resourceCode);
							comps.add(able);
						}
						return CMLib.ableMapper().getAbilityComponentCodedString(comps);
					}
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						String value=webValue(httpReq,parms,oldVal,fieldName);
						if(value.endsWith("$")) 
							value = value.substring(0,oldVal.length()-1);
						value = value.trim();
						String curWhich=httpReq.getRequestParameter(fieldName+"_WHICH");
						int type=0;
						if("COMPONENT".equalsIgnoreCase(curWhich)) type=1;
						else if("EMBEDDED".equalsIgnoreCase(curWhich)) type=2;
						else if("AMOUNT".equalsIgnoreCase(curWhich)) type=0;
						else if(CMLib.ableMapper().getAbilityComponentMap().containsKey(value.toUpperCase().trim())) type=1;
						else if(value.startsWith("(")) type=2;
						else type=0;
						
						List<AbilityComponent> comps=null;
						if(type==2)
						{
							Hashtable<String,List<AbilityComponent>> H=new Hashtable<String,List<AbilityComponent>>();
							String s="ID="+value;
							CMLib.ableMapper().addAbilityComponent(s, H);
							comps=H.get("ID");
						}
						if(comps==null) comps=new ArrayList<AbilityComponent>(1);
							
						StringBuffer str = new StringBuffer("<FONT SIZE=-1>");
						str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==0?"CHECKED ":"")+"VALUE=\"AMOUNT\">");
						str.append("\n\rAmount: <INPUT TYPE=TEXT SIZE=3 NAME="+fieldName+"_AMOUNT VALUE=\""+(type!=0?"":value)+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[0].checked=true;\">");
						str.append("\n\r<BR>");
						str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==1?"CHECKED ":"")+"VALUE=\"COMPONENT\">");
						str.append("\n\rSkill Components:");
						str.append("\n\r<SELECT NAME="+fieldName+"_COMPONENT ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[1].checked=true;\">");
						str.append("<OPTION VALUE=\"0\"");
						if((type!=1)||(value.length()==0)||(value.equalsIgnoreCase("0")))
							str.append(" SELECTED");
						str.append(">&nbsp;");
						for(String S : CMLib.ableMapper().getAbilityComponentMap().keySet())
						{
							str.append("<OPTION VALUE=\""+S+"\"");
							if((type==1)&&(value.equalsIgnoreCase(S)))
								str.append(" SELECTED");
							str.append(">"+S);
						}
						str.append("</SELECT>");
						str.append("\n\r<BR>");
						str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==2?"CHECKED ":"")+"VALUE=\"EMBEDDED\">");
						str.append("\n\rCustom:");
						str.append("\n\r<BR>");
						AbilityComponent comp;
						for(int i=0;i<=comps.size();i++)
						{
							comp=(i<comps.size())?comps.get(i):null;
							if(i>0)
							{
								str.append("\n\r<SELECT NAME="+fieldName+"_CUST_CONN_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
								if(comp!=null)
									str.append("<OPTION VALUE=\"DEL\">DEL");
								else
								if(type==2)
									str.append("<OPTION VALUE=\"\" SELECTED>");
								for(AbilityComponent.CompConnector conector : AbilityComponent.CompConnector.values())
								{
									str.append("<OPTION VALUE=\""+conector.toString()+"\" ");
									if((type==2)&&(comp!=null)&&(conector==comp.getConnector()))
										str.append("SELECTED ");
									str.append(">"+CMStrings.capitalizeAndLower(conector.toString()));
								}
								str.append("</SELECT>");
							}
							str.append("\n\rAmt: <INPUT TYPE=TEXT SIZE=2 NAME="+fieldName+"_CUST_AMT_"+(i+1)+" VALUE=\""+(((type!=2)||(comp==null))?"":Integer.toString(comp.getAmount()))+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
							str.append("\n\r<SELECT NAME="+fieldName+"_CUST_TYPE_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true; ReShow();\">");
							AbilityComponent.CompType compType=(comp!=null)?comp.getType():AbilityComponent.CompType.STRING;
							for(AbilityComponent.CompType conn : AbilityComponent.CompType.values())
							{
								str.append("<OPTION VALUE=\""+conn.toString()+"\" ");
								if(conn==compType) str.append("SELECTED ");
								str.append(">"+CMStrings.capitalizeAndLower(conn.toString()));
							}
							str.append("</SELECT>");
							if(compType==AbilityComponent.CompType.STRING)
								str.append("\n\r<INPUT TYPE=TEXT SIZE=10 NAME="+fieldName+"_CUST_STR_"+(i+1)+" VALUE=\""+(((type!=2)||(comp==null))?"":comp.getStringType())+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
							else
							{
								str.append("\n\r<SELECT NAME="+fieldName+"_CUST_STR_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
								if(compType==AbilityComponent.CompType.MATERIAL)
									for(int x=0;x<RawMaterial.MATERIAL_CODES.length;x++)
									{
										str.append("<OPTION VALUE="+RawMaterial.MATERIAL_CODES[x]);
										if((type==2)&&(comp!=null)&&(RawMaterial.MATERIAL_CODES[x]==comp.getLongType())) 
											str.append(" SELECTED");
										str.append(">"+RawMaterial.MATERIAL_DESCS[x]);
									}
								else
								if(compType==AbilityComponent.CompType.RESOURCE)
									for(int x=0;x<RawMaterial.CODES.TOTAL();x++)
									{
										str.append("<OPTION VALUE="+RawMaterial.CODES.GET(x));
										if((type==2)&&(comp!=null)&&(RawMaterial.CODES.GET(x)==comp.getLongType())) 
											str.append(" SELECTED");
										str.append(">"+RawMaterial.CODES.NAME(x));
									}
								str.append("</SELECT>");
							}
							str.append("\n\r<SELECT NAME="+fieldName+"_CUST_LOC_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
							for(AbilityComponent.CompLocation conn : AbilityComponent.CompLocation.values())
							{
								str.append("<OPTION VALUE=\""+conn.toString()+"\" ");
								if((type==2)&&(comp!=null)&&(conn==comp.getLocation())) 
									str.append("SELECTED ");
								str.append(">"+CMStrings.capitalizeAndLower(conn.toString()));
							}
							str.append("</SELECT>");
							str.append("\n\rConsumed:<INPUT TYPE=CHECKBOX NAME="+fieldName+"_CUST_CON_"+(i+1)+" "+((type!=2)||(comp==null)||(!comp.isConsumed())?"":"CHECKED")+"  ONCLICK=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
							if(i<comps.size()) 
								str.append("\n\r<BR>\n\r");
							else
								str.append("\n\r<a href=\"javascript:ReShow();\">&lt;*&gt;</a>\n\r");
						}
						str.append("<BR>");
						str.append("</FONT>");
						return str.toString();
					}
					public String[] fakeUserInput(String oldVal) { return  new String[]{oldVal}; }
					public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
					{
						++showNumber[0];
						String str = oldVal;
						while(!mob.session().isStopped())
						{
							String help="<AMOUNT>"
								+"\n\rSkill Component: "+CMParms.toStringList(CMLib.ableMapper().getAbilityComponentMap().keySet())
								+"\n\rCustom Component: ([DISPOSITION]:[FATE]:[AMOUNT]:[COMPONENT ID]:[MASK]) && ...";
							str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,help).trim();
							if(str.equals(oldVal)) return oldVal;
							if(CMath.isInteger(str)) 
								return Integer.toString(CMath.s_int(str));
							if(CMLib.ableMapper().getAbilityComponentMap().containsKey(str.toUpperCase().trim()))
								return str.toUpperCase().trim();
							String error=null;
							if(str.trim().startsWith("("))
							{
								error=CMLib.ableMapper().addAbilityComponent("ID="+str, new Hashtable<String,List<AbilityComponent>>());
								if(error==null) return str;
							}
							mob.session().println("'"+str+"' is not an amount of material, a component key, or custom component list"+(error==null?"":"("+error+")")+".  Please use ? for help.");
						}
						return str;
					}
					public String defaultValue(){ return "1";}
				},
				new AbilityParmEditorImpl("OPTIONAL_AMOUNT_REQUIRED","Amt",PARMTYPE_NUMBER){
					public void createChoices() {}
					public boolean confirmValue(String oldVal) { return true;}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{ 
						return "";
					}
				},
				new AbilityParmEditorImpl("ITEM_BASE_VALUE","Value",PARMTYPE_NUMBER){
					public void createChoices() {}
					public String defaultValue(){ return "5";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						return ""+I.baseGoldValue();
					}
				},
				new AbilityParmEditorImpl("ITEM_CLASS_ID","Class ID",PARMTYPE_CHOICES) {
					public void createChoices() 
					{ 
						Vector<Item> V  = new Vector<Item>();
						V.addAll(new XVector<ClanItem>(CMClass.clanItems()));
						V.addAll(new XVector<Armor>(CMClass.armor()));
						V.addAll(new XVector<Item>(CMClass.basicItems()));
						V.addAll(new XVector<MiscMagic>(CMClass.miscMagic()));
						V.addAll(new XVector<Electronics>(CMClass.miscTech()));
						V.addAll(new XVector<Weapon>(CMClass.weapons()));
						Vector<Item> V2=new Vector<Item>();
						Item I;
						for(Enumeration<Item> e=V.elements();e.hasMoreElements();)
						{
							I=(Item)e.nextElement();
							if(I.isGeneric())
								V2.addElement(I);
						}
						createChoices(V2);
					}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I.isGeneric()) return I.ID();
						if(I instanceof Weapon) return "GenWeapon";
						if(I instanceof Armor) return "GenArmor";
						if(I instanceof Rideable) return "GenRideable";
						return "GenItem";
					}
					public String defaultValue(){ return "GenItem";}
				},
				new AbilityParmEditorImpl("CODED_WEAR_LOCATION","Wear Locs",PARMTYPE_SPECIAL) {
					public int appliesToClass(Object o) { return ((o instanceof Armor)||(o instanceof MusicalInstrument))?2:-1;}
					public void createChoices() {}
					public boolean confirmValue(String oldVal) { return oldVal.trim().length()>0;}
					public String defaultValue(){ return "NECK";}
					public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
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
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
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
					public String convertFromItem(final ItemCraftor C, final Item I) 
					{ 
						if(!(I instanceof Armor)) return "HELD";
						Armor A=(Armor)I;
						final boolean[] logicalAnd=new boolean[]{I.rawLogicalAnd()};
						final long[] wornLoc=new long[]{I.rawProperLocationBitmap()};
						final double[] hardBonus=new double[]{0.0};
						final short[] layerAtt=new short[]{A.getLayerAttributes()};
						final short[] layers=new short[]{A.getClothingLayer()};
						return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
					}
					public String[] fakeUserInput(String oldVal) {
						Vector<String> V = new Vector<String>();
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
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I instanceof Container)
							return ""+((Container)I).capacity();
						return "0";
					}
				},
				new AbilityParmEditorImpl("BASE_ARMOR_AMOUNT","Arm.",PARMTYPE_NUMBER) {
					public int appliesToClass(Object o) { return (o instanceof Armor)?2:-1;}
					public void createChoices() {}
					public String defaultValue(){ return "1";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						return ""+I.basePhyStats().armor();
					}
				},
				new AbilityParmEditorImpl("CONTAINER_TYPE","Con.",PARMTYPE_MULTICHOICES) {
					public void createChoices() { createBinaryChoices(Container.CONTAIN_DESCS);}
					public int appliesToClass(Object o) { return (o instanceof Container)?1:-1;}
					public String defaultValue(){ return "0";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(!(I instanceof Container))
							return "";
						Container C=(Container)I;
						StringBuilder str=new StringBuilder("");
						for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
							if(CMath.isSet(C.containTypes(), i-1))
							{
								if(str.length()>0) str.append("|");
								str.append(Container.CONTAIN_DESCS[i]);
							}
						return str.toString();
					}
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
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(!(I instanceof Container))
							return "";
						Container C=(Container)I;
						if(C.hasALock()) return "LOCK";
						if(C.hasALid()) return "LID";
						StringBuilder str=new StringBuilder("");
						for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
							if(CMath.isSet(C.containTypes(), i-1))
							{
								if(str.length()>0) str.append("|");
								str.append(Container.CONTAIN_DESCS[i]);
							}
						return str.toString();
					}
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
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						return CMLib.ableParms().encodeCodedSpells(I);
					}
					public String defaultValue(){ return "";}
					public String rebuild(List<Ability> spells) throws CMException
					{
						StringBuffer newVal = new StringBuffer("");
						if(spells.size()==1)
							newVal.append("*" + ((Ability)spells.get(0)).ID() + ";" + ((Ability)spells.get(0)).text());
						else
							if(spells.size()>1) {
								for(int s=0;s<spells.size();s++)
								{
									String txt = ((Ability)spells.get(s)).text().trim();
									if((txt.indexOf(';')>=0)||(CMClass.getAbility(txt)!=null))
										throw new CMException("You may not have more than one spell when one of the spells parameters is a spell id or a ; character.");
									newVal.append(((Ability)spells.get(s)).ID());
									if(txt.length()>0)
										newVal.append(";" + ((Ability)spells.get(s)).text());
									if(s<(spells.size()-1))
										newVal.append(";");
								}
							}
						return newVal.toString();
					}
					public String[] fakeUserInput(String oldVal) 
					{
						Vector<String> V = new Vector<String>();
						Vector<String> V2 = new Vector<String>();
						List<Ability> spells=CMLib.ableParms().getCodedSpells(oldVal);
						for(int s=0;s<spells.size();s++) {
							V.addElement(((Ability)spells.get(s)).ID());
							V2.addElement(((Ability)spells.get(s)).ID());
							V2.addElement(((Ability)spells.get(s)).text());
						}
						V.addAll(V2);
						V.addElement("");
						return CMParms.toStringArray(V);
					}
					public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) 
					{
						List<Ability> spells=null;
						if(httpReq.isRequestParameter(fieldName+"_AFFECT1"))
						{
							spells = new Vector<Ability>();
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
									spells.add(A);
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
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						List<Ability> spells=CMLib.ableParms().getCodedSpells(webValue(httpReq,parms,oldVal,fieldName));
						StringBuffer str = new StringBuffer("");
						str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
						for(int i=0;i<spells.size();i++)
						{
							Ability A=(Ability)spells.get(i);
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
						for(Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
						{
							Ability A=(Ability)a.nextElement();
							if((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
								continue;
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
						List<Ability> spells=CMLib.ableParms().getCodedSpells(oldVal);
						StringBuffer rawCheck = new StringBuffer("");
						for(int s=0;s<spells.size();s++)
							rawCheck.append(((Ability)spells.get(s)).ID()).append(";").append(((Ability)spells.get(s)).text()).append(";");
						boolean okToProceed = true;
						++showNumber[0];
						String newVal = null;
						while(okToProceed) {
							okToProceed = false;
							CMLib.genEd().spells(mob,spells,showNumber[0],showFlag,true);
							StringBuffer sameCheck = new StringBuffer("");
							for(int s=0;s<spells.size();s++)
								sameCheck.append(((Ability)spells.get(s)).ID()).append(';').append(((Ability)spells.get(s)).text()).append(';');
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
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I instanceof Weapon)
							return ""+((Weapon)I).basePhyStats().damage();
						return "0";
					}
				},
				new AbilityParmEditorImpl("LID_LOCK","Lid.",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return (o instanceof Container)?1:-1;}
					public void createChoices() { createChoices(new String[]{"","LID","LOCK"});}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(!(I instanceof Container))
							return "";
						Container C=(Container)I;
						if(C.hasALock()) return "LOCK";
						if(C.hasALid()) return "LID";
						return "";
					}
				},
				new AbilityParmEditorImpl("STATUE","Statue",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return ((!(o instanceof Armor))&&(!(o instanceof Container))&&(!(o instanceof Drink)))?1:-1;}
					public void createChoices() { createChoices(new String[]{"","STATUE"});}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I instanceof Weapon) return "";
						if(I instanceof Armor) return "";
						if(I instanceof Ammunition) return "";
						int x=I.Name().lastIndexOf(" of ");
						if(x<0) return "";
						String ender=I.Name();
						if(!I.displayText().endsWith(ender+" is here")) return "";
						if(!I.description().startsWith(ender+". ")) return "";
						return "STATUE";
					}
				},
				new AbilityParmEditorImpl("RIDE_BASIS","Ride",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return (o instanceof Rideable)?3:-1;}
					public void createChoices() { createChoices(new String[]{"","CHAIR","TABLE","LADDER","ENTER","BED"});}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(!(I instanceof Rideable)) return "";
						switch(((Rideable)I).rideBasis())
						{
						case Rideable.RIDEABLE_SIT: return "SIT";
						case Rideable.RIDEABLE_TABLE:  return "TABLE";
						case Rideable.RIDEABLE_LADDER: return "LADDER";
						case Rideable.RIDEABLE_ENTERIN: return "ENTER";
						case Rideable.RIDEABLE_SLEEP: return "BED";
						default: return "";
						}
					}
				},
				new AbilityParmEditorImpl("LIQUID_CAPACITY","Liq.",PARMTYPE_NUMBER) {
					public int appliesToClass(Object o) { return (o instanceof Drink)?4:-1;}
					public void createChoices() {}
					public String defaultValue(){ return "25";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(!(I instanceof Drink)) return "";
						return ""+((Drink)I).liquidHeld();
					}
				},
				new AbilityParmEditorImpl("WEAPON_CLASS","WClas",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
					public void createChoices() { createChoices(Weapon.CLASS_DESCS);}
					public String defaultValue(){ return "BLUNT";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I instanceof Weapon)
							return Weapon.CLASS_DESCS[((Weapon)I).weaponClassification()];
						return "0";
					}
				},
				new AbilityParmEditorImpl("SMOKE_FLAG","Smoke",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return (o instanceof Light)?5:-1;}
					public void createChoices() { createChoices(new String[]{"","SMOKE"});}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(!(I instanceof Light)) return "";
						if((I instanceof Container)
						&&(((Light)I).getDuration() > 199)
						&&(((Container)I).capacity()==0))
							return "SMOKE";
						return "";
					}
				},
				new AbilityParmEditorImpl("WEAPON_HANDS_REQUIRED","Hand",PARMTYPE_NUMBER) {
					public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
					public void createChoices() {}
					public String defaultValue(){ return "1";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I instanceof Weapon)
							return ((Weapon)I).rawLogicalAnd()?"2":"1";
						return "";
					}
				},
				new AbilityParmEditorImpl("LIGHT_DURATION","Dur.",PARMTYPE_NUMBER) {
					public int appliesToClass(Object o) { return (o instanceof Light)?5:-1;}
					public void createChoices() {}
					public String defaultValue(){ return "10";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(I instanceof Light)
							return ""+((Light)I).getDuration();
						return "";
					}
				},
				new AbilityParmEditorImpl("CLAN_ITEM_CODENUMBER","Typ.",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return (o instanceof ClanItem)?10:-1;}
					public void createChoices() { createNumberedChoices(ClanItem.CI_DESC);}
					public String defaultValue(){ return "1";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(I instanceof ClanItem)
							return ""+((ClanItem)I).ciType();
						return "";
					}
				},
				new AbilityParmEditorImpl("CLAN_EXPERIENCE_COST_AMOUNT","Exp",PARMTYPE_NUMBER) {
					public void createChoices() {}
					public String defaultValue(){ return "100";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if(!(I instanceof ClanItem)) return "100";
						if(I.getClass().getName().toString().indexOf("Flag")>0)
							return "2500";
						if(I.getClass().getName().toString().indexOf("ClanItem")>0)
							return "1000";
						if(I.getClass().getName().toString().indexOf("GenClanSpecialItem")>0)
							return "500";
						return "100";
					}
				},
				new AbilityParmEditorImpl("CLAN_AREA_FLAG","Area",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return o.getClass().getName().toString().indexOf("LawBook")>0?5:-1;}
					public void createChoices() { createChoices(new String[]{"","AREA"});}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						return (I.getClass().getName().toString().indexOf("LawBook")>0)?"AREA":"";
					}
				},
				new AbilityParmEditorImpl("READABLE_TEXT","Read",PARMTYPE_STRINGORNULL) {
					public void createChoices() {}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(CMLib.flags().isReadable(I))
							return I.readableText();
						return "";
					}
				},
				new AbilityParmEditorImpl("REQUIRED_COMMON_SKILL_ID","Common Skill",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return (o instanceof ClanItem)?5:-1;}
					public void createChoices() {
						Vector<Object> V  = new Vector<Object>();
						Ability A = null;
						for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
						{
							A=(Ability)e.nextElement();
							if((A.classificationCode() & Ability.ALL_ACODES) == Ability.ACODE_COMMON_SKILL)
								V.addElement(A);
						}
						V.addElement("");
						createChoices(V);
					}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if(I.getClass().getName().toString().indexOf("LawBook")>0)
							return "";
						if(I instanceof ClanItem)
							return ((ClanItem)I).readableText();
						return "";
					}
				},
				new AbilityParmEditorImpl("FOOD_DRINK","ETyp",PARMTYPE_CHOICES) {
					public void createChoices() { createChoices(new String[]{"","FOOD","DRINK","SOAP","GenPerfume"});}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						final String str=(I.name()+" "+I.displayText()+" "+I.description()).toUpperCase();
						if(str.startsWith("SOAP ") || str.endsWith(" SOAP") || (str.indexOf("SOAP")>0))
							return "SOAP";
						if(I instanceof Perfume)
							return "PERFUME";
						if(I instanceof Food)
							return "FOOD";
						if(I instanceof Drink)
							return "DRINK";
						return "";
					}
				},
				new AbilityParmEditorImpl("SMELL_LIST","Smells",PARMTYPE_STRING) {
					public void createChoices() {}
					public int appliesToClass(Object o) { return (o instanceof Perfume)?5:-1;}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if(I instanceof Perfume)
							return ((Perfume)I).getSmellList();
						return "";
					}
				},
				new AbilityParmEditorImpl("RESOURCE_OR_KEYWORD","Resc/Itm",PARMTYPE_SPECIAL) {
					public void createChoices() {}
					public boolean confirmValue(String oldVal) { return true;}
					public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						if(httpReq.isRequestParameter(fieldName+"_WHICH"))
						{
							String which=httpReq.getRequestParameter(fieldName+"_WHICH");
							if(which.trim().length()>0)
								return httpReq.getRequestParameter(fieldName+"_RESOURCE");
							return httpReq.getRequestParameter(fieldName+"_WORD");
						}
						return oldVal;
					}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						return "";
					}
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
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
						while(proceed&&(!mob.session().isStopped()))
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
							if((!isResource)&&(mob.session()!=null)&&(!mob.session().isStopped()))
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
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						return "";
					}
					public String[] fakeUserInput(String oldVal) {
						if(oldVal.endsWith("$"))
							return new String[]{oldVal.substring(0,oldVal.length()-1)}; 
						return new String[]{oldVal}; 
					}
					public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						AbilityParmEditor A = (AbilityParmEditor)CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
						if(oldVal.endsWith("$")) oldVal = oldVal.substring(0,oldVal.length()-1);
						String value = A.webValue(httpReq,parms,oldVal,fieldName);
						int r=RawMaterial.CODES.FIND_IgnoreCase(value);
						if(r>=0) return RawMaterial.CODES.NAME(r);
						return (value.trim().length()==0)?"":(value+"$");
					}
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						AbilityParmEditor A = (AbilityParmEditor)CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
						return A.webField(httpReq,parms,oldVal,fieldName);
					}
					public String webTableField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal) {
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
						while(proceed&&(!mob.session().isStopped()))
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
							if((!isResource)&&(mob.session()!=null)&&(!mob.session().isStopped()))
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
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if(I instanceof Ammunition)
							return ""+((Ammunition)I).ammunitionType();
						return "";
					}
				},
				new AbilityParmEditorImpl("AMMO_CAPACITY","Ammo#",PARMTYPE_NUMBER) {
					public void createChoices() {}
					public int appliesToClass(Object o) { return ((o instanceof Weapon)||(o instanceof Ammunition))?2:-1;}
					public String defaultValue(){ return "1";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if(I instanceof Ammunition)
							return ""+((Ammunition)I).usesRemaining();
						if((I instanceof Weapon)&&(((Weapon)I).requiresAmmunition()))
							return ""+((Weapon)I).ammunitionCapacity();
						return "";
					}
				},
				new AbilityParmEditorImpl("MAXIMUM_RANGE","Max",PARMTYPE_NUMBER) 
				{ 
					public int appliesToClass(Object o) { return ((o instanceof Weapon)&&(!(o instanceof Ammunition)))?2:-1;}
					public void createChoices() {} 
					public String defaultValue(){ return "5";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if((I instanceof Ammunition)||(I instanceof Weapon))
							return ""+I.maxRange();
						return "";
					}
				},
				new AbilityParmEditorImpl("RESOURCE_OR_MATERIAL","Rsc/Mat",PARMTYPE_CHOICES) {
                    public void createChoices() {
						Vector<String> V=new XVector<String>(RawMaterial.CODES.NAMES());
						V.addAll(new XVector<String>(RawMaterial.MATERIAL_DESCS));
						createChoices(V);
					}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if(CMStrings.containsWordIgnoreCase(I.Name(),"rice"))
							return "RICE";
						if(I.material() == RawMaterial.RESOURCE_PAPER)
							return "WOOD";
						return RawMaterial.CODES.NAME(I.material());
					}
					public String defaultValue(){ return "IRON";}
				},
				new AbilityParmEditorImpl("OPTIONAL_RESOURCE_OR_MATERIAL","Rsc/Mat",PARMTYPE_CHOICES) {
					public void createChoices() {
						Vector<String> V=new XVector<String>(RawMaterial.CODES.NAMES());
						V.addAll(new XVector<String>(RawMaterial.MATERIAL_DESCS));
						V.addElement("");
						createChoices(V);
					}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{ 
						return "";
					}
					public String defaultValue(){ return "";}
				},
				new AbilityParmEditorImpl("HERB_NAME","Herb Final Name",PARMTYPE_STRING) {
					public void createChoices() {}
					public String defaultValue(){ return "Herb Name";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I.material()==RawMaterial.RESOURCE_HERBS)
							return CMStrings.lastWordIn(I.Name());
						return "";
					}
				},
				new AbilityParmEditorImpl("RIDE_CAPACITY","Ridrs",PARMTYPE_NUMBER) {
					public void createChoices() {}
					public int appliesToClass(Object o) { return (o instanceof Rideable)?3:-1;}
					public String defaultValue(){ return "2";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{ 
						if(I instanceof Rideable)
							return ""+((Rideable)I).riderCapacity();
						return "0";
					}
				},
				new AbilityParmEditorImpl("METAL_OR_WOOD","Metal",PARMTYPE_CHOICES) {
					public void createChoices() { createChoices(new String[]{"METAL","WOOD"});}
					public String defaultValue(){ return "METAL";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						switch(I.material()&RawMaterial.MATERIAL_MASK)
						{
						case RawMaterial.MATERIAL_METAL:
						case RawMaterial.MATERIAL_MITHRIL:
							return "METAL";
						case RawMaterial.MATERIAL_WOODEN:
							return "WOOD";
						}
						return ""; // absolutely no way to determine
					}
				},
				new AbilityParmEditorImpl("OPTIONAL_RACE_ID","Race",PARMTYPE_SPECIAL) {
					public void createChoices() { 
						createChoices(CMClass.races());
						choices().addElement("","");
						for(int x=0;x<choices().size();x++)
							choices().setElementAt(x,1,((String)choices().elementAt(x,1)).toUpperCase());
					}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						return ""; // absolutely no way to determine
					}
					public String defaultValue(){ return "";}
					public boolean confirmValue(String oldVal) {
						if(oldVal.trim().length()==0)
							return true;
						Vector<String> parsedVals = CMParms.parse(oldVal.toUpperCase());
						for(int v=0;v<parsedVals.size();v++)
							if(CMClass.getRace((String)parsedVals.elementAt(v))==null)
								return false;
						return true;
					}
					public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						Vector<String> raceIDs=null;
						if(httpReq.isRequestParameter(fieldName+"_RACE"))
						{
							String id="";
							raceIDs=new Vector<String>();
							for(int i=0;httpReq.isRequestParameter(fieldName+"_RACE"+id);id=""+(++i))
								raceIDs.addElement(httpReq.getRequestParameter(fieldName+"_RACE"+id).toUpperCase().trim());
						}
						else
							raceIDs = CMParms.parse(oldVal.toUpperCase().trim());
						return CMParms.combine(raceIDs,0);
					}
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
						Vector<String> raceIDs=CMParms.parse(webValue(httpReq,parms,oldVal,fieldName).toUpperCase());
						StringBuffer str = new StringBuffer("");
						str.append("\n\r<SELECT NAME="+fieldName+"_RACE MULTIPLE>");
						str.append("<OPTION VALUE=\"\" "+((raceIDs.size()==0)?"SELECTED":"")+">");
						for(Enumeration<Race> e=CMClass.races();e.hasMoreElements();)
						{
							Race R=e.nextElement();
							str.append("<OPTION VALUE=\""+R.ID()+"\" "+((raceIDs.contains(R.ID().toUpperCase()))?"SELECTED":"")+">"+R.name());
						}
						str.append("</SELECT>");
						return str.toString();
					}
					public String[] fakeUserInput(String oldVal) { 
						Vector<String> parsedVals = CMParms.parse(oldVal.toUpperCase());
						if(parsedVals.size()==0)
							return new String[]{""};
						Vector<String> races = new Vector<String>();
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
						while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
						{
							mob.tell(showNumber+". "+prompt()+": '"+newVal+"'.");
							if((showFlag!=showNumber[0])&&(showFlag>-999)) return newVal;
							Vector<String> parsedVals = CMParms.parse(newVal.toUpperCase());
							behave=mob.session().prompt("Enter a race to add/remove (?)\n\r:","");
							if(behave.length()>0)
							{
								if(behave.equalsIgnoreCase("?"))
									mob.tell(CMLib.lister().reallyList(mob,CMClass.races(),-1).toString());
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
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(I instanceof MusicalInstrument)
							return MusicalInstrument.TYPE_DESC[((MusicalInstrument)I).instrumentType()];
						return "0";
					}
				},
				new AbilityParmEditorImpl("STONE_FLAG","Stone",PARMTYPE_CHOICES) {
					public void createChoices() { createChoices(new String[]{"","STONE"});}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{
						if(I.material()==RawMaterial.RESOURCE_STONE)
							return "STONE";
						return "";
					}
				},
				new AbilityParmEditorImpl("POSE_NAME","Pose Word",PARMTYPE_ONEWORD) {
					public void createChoices() {}
					public String defaultValue(){ return "New Post";}
					public String convertFromItem(final ItemCraftor A, final Item I) { return ""; }
				},
				new AbilityParmEditorImpl("POSE_DESCRIPTION","Pose Description",PARMTYPE_STRING) {
					public void createChoices() {}
					public String defaultValue(){ return "<S-NAME> is standing here.";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						if(!(I instanceof DeadBody)) return "";
						String pose=I.displayText();
						pose=CMStrings.replaceAll(pose,I.name(),"<S-NAME>");
						pose=CMStrings.replaceWord(pose,"himself"," <S-HIM-HERSELF>");
						pose=CMStrings.replaceWord(pose,"herself"," <S-HIM-HERSELF>");
						pose=CMStrings.replaceWord(pose,"his"," <S-HIS-HER>");
						pose=CMStrings.replaceWord(pose,"her"," <S-HIS-HER>");
						pose=CMStrings.replaceWord(pose,"him"," <S-HIM-HER>");
						pose=CMStrings.replaceWord(pose,"her"," <S-HIM-HER>");
						return pose;
					}
				},
				new AbilityParmEditorImpl("WOOD_METAL_CLOTH","",PARMTYPE_CHOICES) {
					public void createChoices() { createChoices(new String[]{"WOOD","METAL","CLOTH"});}
					public String defaultValue(){ return "WOOD";}
					public String convertFromItem(final ItemCraftor A, final Item I) 
					{
						switch(I.material()&RawMaterial.MATERIAL_MASK)
						{
						case RawMaterial.MATERIAL_CLOTH: return "CLOTH";
						case RawMaterial.MATERIAL_METAL: return "METAL";
						case RawMaterial.MATERIAL_MITHRIL: return "METAL";
						case RawMaterial.MATERIAL_WOODEN: return "WOOD";
						default: return "";
						}
					}
				},
				new AbilityParmEditorImpl("WEAPON_TYPE","W.Type",PARMTYPE_CHOICES) {
					public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
					public void createChoices() { createChoices(Weapon.TYPE_DESCS);}
					public String convertFromItem(final ItemCraftor A, final Item I){ return (I instanceof Weapon) ? Weapon.TYPE_DESCS[((Weapon)I).weaponType()] : ""; }
					public String defaultValue(){ return "BASHING";}
				},
				new AbilityParmEditorImpl("ATTACK_MODIFICATION","Att.",PARMTYPE_NUMBER) {
					public void createChoices() {}
					public int appliesToClass(Object o) { return (o instanceof Weapon)?2:-1;}
					public String convertFromItem(final ItemCraftor A, final Item I){ return ""+((I instanceof Weapon)?((Weapon)I).basePhyStats().attackAdjustment():0); }
					public String defaultValue(){ return "0";}
				},
				new AbilityParmEditorImpl("N_A","N/A",PARMTYPE_STRING) {
					public void createChoices() {}
					public int appliesToClass(Object o) { return -1;}
					public String defaultValue(){ return "";}
					public String convertFromItem(final ItemCraftor A, final Item I){ return ""; }
					public boolean confirmValue(String oldVal) { return oldVal.trim().length()==0||oldVal.equals("0");}
					public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag) throws java.io.IOException
					{ return "";}
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) { return ""; }
				},
				new AbilityParmEditorImpl("RESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED","Resrc/Amt",PARMTYPE_SPECIAL) {
					public void createChoices() { 
						createChoices(RawMaterial.CODES.NAMES()); 
						choices().addElement("","");
					}
					public String convertFromItem(final ItemCraftor A, final Item I)
					{ 
						int amt=(int)Math.round(CMath.mul(I.basePhyStats().weight()-1,(A!=null)?A.getItemWeightMultiplier(false):1.0));
						if(amt<1) amt=1;
						return RawMaterial.CODES.NAME(I.material())+"/"+amt;
					}
					public String defaultValue(){ return "";}
					public int appliesToClass(Object o) { return 0;}
					public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
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
					public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
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
		DEFAULT_EDITORS = new Hashtable<String,AbilityParmEditor>();
		for(int v=0;v<V.size();v++) {
			AbilityParmEditor A = (AbilityParmEditor)V.elementAt(v);
			DEFAULT_EDITORS.put(A.ID(),A);
		}
		return DEFAULT_EDITORS;
	}
	
	protected class AbilityRecipeDataImpl implements AbilityRecipeData 
	{
		private String recipeFilename;
		private String recipeFormat;
		private Vector<Object> columns;
		private Vector<DVector> dataRows;
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
			if(recipeFilename.trim().length()==0)
			{
				parseError = "No file";
				return;
			}
			CMFile F = new CMFile(Resources.buildResourcePath("skills")+recipeFilename,null,true);
			wasVFS=F.isVFSFile();
			StringBuffer str=F.text();
			columns = parseRecipeFormatColumns(recipeFormat);
			numberOfDataColumns = 0;
			for(int c = 0; c < columns.size(); c++)
				if(columns.elementAt(c) instanceof List)
					numberOfDataColumns++;
			dataRows = null;
			try {
				dataRows = parseDataRows(str,columns,numberOfDataColumns);
				DVector editRow = new DVector(2);
				for(int c=0;c<columns().size();c++)
					if(columns().elementAt(c) instanceof List)
						editRow.addElement(columns().elementAt(c),"");
				if(editRow.size()==0)
				{
					//classFieldIndex = CMAbleParms.getClassFieldIndex(dataRow);
				}
				else
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
				if(columns().elementAt(c) instanceof List)
					editRow.addElement(columns().elementAt(c),"");
			return editRow;
		}
		public int getClassFieldIndex() { return classFieldIndex;}
		public String recipeFilename(){ return recipeFilename;}
		public String recipeFormat(){ return recipeFormat;}
		public Vector<DVector> dataRows() { return dataRows;}
		public Vector<? extends Object> columns() { return columns;}
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
			//$FALL-THROUGH$
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
		@SuppressWarnings("unchecked")
        public String[] fakeUserInput(String oldVal) {
			boolean emptyOK = false;
			switch(fieldType) {
			case PARMTYPE_STRINGORNULL:
				emptyOK = true;
			//$FALL-THROUGH$
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
				Vector<String> V = choices.getDimensionVector(1);
				for(int v=0;v<V.size();v++)
					if(oldVal.equalsIgnoreCase((String)V.elementAt(v)))
						return new String[]{(String)choices.elementAt(v,2)};
				return new String[]{oldVal};
			}
			case PARMTYPE_MULTICHOICES:
				if(oldVal.trim().length()==0) return new String[]{"NULL"};
				if(!CMath.isInteger(oldVal))
				{
					Vector<String> V = (Vector<String>)choices.getDimensionVector(1);
					for(int v=0;v<V.size();v++)
						if(oldVal.equalsIgnoreCase((String)V.elementAt(v)))
							return new String[]{(String)choices.elementAt(v,2),""};
				} else {
					Vector<String> V = new Vector<String>();
					for(int c=0;c<choices.size();c++)
						if(CMath.bset(CMath.s_int(oldVal),CMath.s_int((String)choices.elementAt(c,1))))
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
			//$FALL-THROUGH$
			case PARMTYPE_ONEWORD:
			case PARMTYPE_STRING:
			{
				++showNumber[0];
				boolean proceed = true;
				while(proceed&&(!mob.session().isStopped())) {
					str = CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),emptyOK).trim();
					if((!spaceOK) && (str.indexOf(' ') >= 0))
						mob.tell("Spaces are not allowed here.");
					else
						proceed=false;
				}
				break;
			}
			case PARMTYPE_NUMBER:
			{
				String newStr=CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),true);
				if(newStr.trim().length()==0)
					str="";
				else
					str = Integer.toString(CMath.s_int(newStr));
				break;
			}
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
		
		public String webValue(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
			String webValue = httpReq.getRequestParameter(fieldName);
			switch(fieldType) {
			case PARMTYPE_ONEWORD:
			case PARMTYPE_STRINGORNULL:
			case PARMTYPE_STRING:
			case PARMTYPE_NUMBER:
				return (webValue == null)?oldVal:webValue;
			case PARMTYPE_MULTICHOICES:
			{
				if(webValue == null) 
					return oldVal;
				String id="";
				long num=0;
				int index=0;
				for(;httpReq.isRequestParameter(fieldName+id);id=""+(++index))
				{
					String newVal = httpReq.getRequestParameter(fieldName+id); 
					if(CMath.s_long(newVal)<=0)
						return newVal;
					num |= CMath.s_long(newVal);
				}
				return ""+num;
			}
			case PARMTYPE_CHOICES:
				return (webValue == null)?oldVal:webValue;
			}
			return "";
		}
		
		public String webTableField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal) { return oldVal; }
		
		public String webField(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, String oldVal, String fieldName) {
			int textSize = 50;
			String webValue = webValue(httpReq,parms,oldVal,fieldName);
			String onChange = null;
			Vector<String> choiceValues = new Vector<String>();
			switch(fieldType) {
			case PARMTYPE_ONEWORD:
				textSize = 10;
			//$FALL-THROUGH$
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
			//$FALL-THROUGH$
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
		public DVector createChoices(Enumeration<? extends Object> e) 
		{
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
		public DVector createChoices(Vector<? extends Object> V) { return createChoices(V.elements());}
		public DVector createChoices(String[] S) { return createChoices(new XVector<String>(S).elements());}
		public DVector createBinaryChoices(String[] S) { 
			if(choices != null) return choices;
			choices = createChoices(new XVector<String>(S).elements());
			for(int i=0;i<choices.size();i++)
			{
				if(i==0)
					choices.setElementAt(i,1,Integer.toString(0));
				else
					choices.setElementAt(i,1,Integer.toString(1<<(i-1)));
			}
			return choices;
		}
		public DVector createNumberedChoices(String[] S) { 
			if(choices != null) return choices;
			choices = createChoices(new XVector<String>(S).elements());
			for(int i=0;i<choices.size();i++)
				choices.setElementAt(i,1,Integer.toString(i));
			return choices;
		}
		public DVector choices() { return choices; } 
		public int appliesToClass(Object o) { return 0;}
	}
	
}
