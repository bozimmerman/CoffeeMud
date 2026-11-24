package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.*;
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
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompType;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.Material;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2026 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CMAbleParms";
	}

	protected Map<String,AbilityParmEditor> DEFAULT_EDITORS = null;

	public CMAbleParms()
	{
		super();
	}

	@Override
	public String getGenericClassID(final Ability A)
	{
		if(A==null)
			return null;
		if(A.getStat("JAVACLASS").toLowerCase().indexOf("tweak")>=0)
			return "GenTweakAbility";
		else
		if(A instanceof Trap)
			return "GenTrap";
		else
		if(A instanceof ItemCraftor)
		{
			if((((ItemCraftor)A).getRecipeFilename()!=null)
			&&(((ItemCraftor)A).getRecipeFilename().toLowerCase().endsWith(".cmare")))
				return "GenWrightSkill";
			else
				return "GenCraftSkill";
		}
		else
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
			return "GenGatheringSkill";
		else
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)
			return "GenPoison";
		else
		if((A instanceof Language)
		&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
			return "GenLanguage";
		else
			return "GenAbility";
	}

	@Override
	public Ability convertAbilityToGeneric(final Ability A)
	{
		if(A==null)
			return null;
		if(A.isGeneric())
			return A;
		final Ability CR;
		if(A instanceof Trap)
		{
			CR=(Ability)CMClass.getAbility("GenTrap").copyOf();
			CR.setStat("PERMRESET", ""+((Trap)A).getReset());
		}
		else
		if(A instanceof ItemCraftor)
		{
			if((((ItemCraftor)A).getRecipeFilename()!=null)
			&&(((ItemCraftor)A).getRecipeFilename().toLowerCase().endsWith(".cmare")))
				CR=(Ability)CMClass.getAbility("GenWrightSkill").copyOf();
			else
				CR=(Ability)CMClass.getAbility("GenCraftSkill").copyOf();
			CR.setStat("FILENAME", ((ItemCraftor)A).getRecipeFilename());
		}
		else
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
			CR=(Ability)CMClass.getAbility("GenGatheringSkill").copyOf();
		else
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)
			CR=(Ability)CMClass.getAbility("GenPoison").copyOf();
		else
		if((A instanceof Language)
		&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
		{
			CR=(Ability)CMClass.getAbility("GenLanguage").copyOf();
			final List<String[]> lists=((Language)A).translationLists(A.ID());
			final StringBuilder str = new StringBuilder("");
			for (final String[] wset : lists)
			{
				if(str.length() > 0)
					str.append("/");
				str.append(CMParms.toListString(wset));
			}
			CR.setStat("WORDS", str.toString());
			CR.setStat("HASHEDWORDS", CMParms.toKeyValueSlashListString(((Language)A).translationHash(A.ID())));
			CR.setStat("NATURALLANG", ""+((Language)A).isANaturalLanguage());
			CR.setStat("INTERPRETS", CMParms.combineWith(((Language)A).languagesSupported(),','));
			CR.setStat("VERB", ((Language)A).getVerb());
			CR.setStat("TRANSVERB", ((Language)A).getTranslationVerb());
		}
		else
			CR=(Ability)CMClass.getAbility("GenAbility").copyOf();
		CR.setStat("CLASS",A.ID());
		CR.setStat("LEVEL","1");
		CR.setStat("NAME", A.Name());
		CR.setStat("CLASSIFICATION", ""+A.classificationCode());
		CR.setStat("DISPLAY", A.displayText());
		CR.setStat("TRIGSTR", CMParms.toListString(A.triggerStrings()));
		CR.setStat("MAXRANGE", ""+A.maxRange());
		CR.setStat("MINRANGE", ""+A.minRange());
		CR.setStat("FLAGS", ""+A.flags());
		CR.setStat("AUTOINVOKE", ""+A.isAutoInvoked());
		//CR.setStat("OVERRIDEMANA", ""+A.);
		CR.setStat("USAGEMASK", ""+A.usageType());
		int canAffect=0;
		for(int i=0;i<Ability.CAN_DESCS.length;i++)
		{
			if(A.canAffect(Math.round(CMath.pow(2, i))))
				canAffect |= Math.round(CMath.pow(2, i));
		}
		CR.setStat("CANAFFECTMASK", ""+canAffect);
		int canTarget=0;
		for(int i=0;i<Ability.CAN_DESCS.length;i++)
		{
			if(A.canTarget(Math.round(CMath.pow(2, i))))
				canTarget |= Math.round(CMath.pow(2, i));
		}
		CR.setStat("CANTARGETMASK", ""+canTarget);
		CR.setStat("USAGEMASK", ""+A.usageType());
		CR.setStat("QUALITY", ""+A.abstractQuality());
		if(canAffect!=0)
		{
			CR.setStat("MOCKABILITY", ""+A.ID());
			CR.setStat("MOCKABLETEXT", ""+A.text());
		}
		CR.setStat("POSTCASTABILITY", ""+A.ID());
		CR.setStat("HELP", CMLib.help().getHelpText(A.ID(), null, false, true));
		return CR;
	}

	@Override
	public String getCraftingBrand(final Item buildingI)
	{
		if((buildingI != null) && (buildingI.rawSecretIdentity().length()>0))
		{
			final Ability A=buildingI.fetchEffect("Copyright");
			if((A!=null)&&(A.text().length()>0))
				return A.text();
			Pattern P = (Pattern)Resources.getResource("SYSTEM_CRAFTING_BRAND_STR");
			if(P == null)
			{
				final String CRAFTING_BRAND_STR=CMLib.lang().L("This is the work of @x1.");
				String mask = CRAFTING_BRAND_STR;
				int x = mask.indexOf("@x1");
				if(x<0)
					mask = mask+" @x1.";
				x = mask.indexOf("@x1");
				final String prefix = mask.substring(0,x);
				final String suffix = mask.substring(x+3);
				mask = ".*" + Pattern.quote(prefix) + "([^.]+)" + Pattern.quote(suffix) + ".*";
				P = Pattern.compile(mask);
				Resources.submitResource("SYSTEM_CRAFTING_BRAND_STR",P);
			}
			final Matcher matcher = P.matcher(buildingI.rawSecretIdentity());
			if(matcher.find())
				return matcher.group(1).trim();
		}
		return "";
	}

	@Override
	public String createCraftingBrand(final MOB mob)
	{

		final String CRAFTING_BRAND_ANON=CMLib.lang().L("an anonymous craftsman");
		final String CRAFTING_BRAND_STR=CMLib.lang().L("This is the work of @x1.");
		if(mob==null)
			return CMStrings.replaceVariables(CRAFTING_BRAND_STR,CRAFTING_BRAND_ANON);
		else
			return CMStrings.replaceVariables(CRAFTING_BRAND_STR,mob.Name());
	}

	protected String parseLayers(final short[] layerAtt, final short[] clothingLayers, String misctype)
	{
		final int colon=misctype.indexOf(':');
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

	@Override
	public void parseWearLocation(final short[] layerAtt, final short[] layers, final long[] wornLoc, final boolean[] logicalAnd, final double[] hardBonus, String wearLocation)
	{
		if(layers != null)
		{
			layerAtt[0] = 0;
			layers[0] = 0;
			wearLocation=parseLayers(layerAtt,layers,wearLocation);
		}

		final double hardnessMultiplier = hardBonus[0];
		wornLoc[0] = 0;
		hardBonus[0]=0.0;
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int wo=1;wo<codes.total();wo++)
		{
			final String WO=codes.name(wo).toUpperCase();
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

	public Vector<Object> parseRecipeFormatColumns(final String recipeFormat)
	{
		char C = '\0';
		final StringBuffer currentColumn = new StringBuffer("");
		Vector<String> currentColumns = null;
		final char[] recipeFmtC = recipeFormat.toCharArray();
		final Vector<Object> columnsV = new Vector<Object>();
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

	@Override
	public String makeRecipeFromItem(final ItemCraftor C, final Item I) throws CMException
	{
		final Vector<Object> columns = parseRecipeFormatColumns(C.getRecipeFormat());
		final Map<String,AbilityParmEditor> editors = this.getEditors();
		final StringBuilder recipe = new StringBuilder("");
		for(int d=0;d<columns.size();d++)
		{
			if(columns.get(d) instanceof String)
			{
				final String name = (String)columns.get( d );
				AbilityParmEditor A = editors.get(columns.get(d));
				if((A==null)||(name.length()<3))
				{
					recipe.append("\t");
					continue;
				}
				if(A.appliesToClass(I)<0)
					A = editors.get("N_A");
				if(A!=null)
					columns.set(d,A.ID());
			}
			else
			if(columns.get(d) instanceof List)
			{
				AbilityParmEditor applicableA = null;
				final List<?> colV=(List<?>)columns.get(d);
				for(int c=0;c<colV.size();c++)
				{
					final Object o = colV.get(c);
					if(o instanceof List)
						continue;
					final String ID = (o instanceof String) ? (String)o : ((AbilityParmEditor)o).ID();
					final AbilityParmEditor A = editors.get(ID);
					if(A==null)
						throw new CMException("Column name "+ID+" is not found.");
					if((applicableA==null)
					||(A.appliesToClass(I) > applicableA.appliesToClass(I)))
						applicableA = A;
				}
				if((applicableA == null)||(applicableA.appliesToClass(I)<0))
					applicableA = editors.get("N_A");
				columns.set(d,applicableA.ID());
			}
			else
				throw new CMException("Col name "+(columns.get(d))+" is not a String or List.");
			final AbilityParmEditor A = editors.get(columns.get(d));
			if(A==null)
				throw new CMException("Editor name "+(columns.get(d))+" is not defined.");
			recipe.append(A.convertFromItem(C, I));
		}
		return recipe.toString();
	}

	@SuppressWarnings("unchecked")
	protected static int getClassFieldIndex(final AbilityRecipeRow dataRow)
	{
		for(int d=0;d<dataRow.size();d++)
		{
			if(dataRow.get(d).first instanceof List)
			{
				final List<String> V=(List<String>)dataRow.get(d).first;
				if(V.contains("ITEM_CLASS_ID")
				||V.contains("FOOD_DRINK")
				||V.contains("COLOR_TERM")
				||V.contains("ITEM_CMARE")
				||V.contains("BUILDING_CODE"))
					return d;
			}
			else
			if(dataRow.get(d).first instanceof String)
			{
				final String s=(String)dataRow.get(d).first;
				if(s.equalsIgnoreCase("ITEM_CLASS_ID")
				||s.equalsIgnoreCase("FOOD_DRINK")
				||s.equalsIgnoreCase("ITEM_CMARE")
				||s.equalsIgnoreCase("COLOR_TERM")
				||s.equalsIgnoreCase("BUILDING_CODE"))
					return d;
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	protected Object getSampleObject(final AbilityRecipeRow dataRow)
	{
		boolean classIDRequired = false;
		String classID = null;
		final int fieldIndex = getClassFieldIndex(dataRow);
		for(int d=0;d<dataRow.size();d++)
		{
			if((dataRow.get(d).first instanceof List)
			&&(!classIDRequired)
			&&(((List<String>)dataRow.get(d).first).size()>1))
				classIDRequired=true;
		}
		if(fieldIndex >=0)
			classID=dataRow.get(fieldIndex).second;
		if((classID!=null)&&(classID.length()>0))
		{
			final String uClassID=classID.toUpperCase();
			if(uClassID.equalsIgnoreCase("FOOD"))
				return CMClass.getItemPrototype("GenFood");
			else
			if(uClassID.equalsIgnoreCase("SOAP"))
				return CMClass.getItemPrototype("GenItem");
			else
			if(uClassID.equalsIgnoreCase("DRINK"))
				return CMClass.getItemPrototype("GenDrink");
			else
			if(uClassID.startsWith("<ITEM>")
			||uClassID.startsWith("<MOB>")
			||uClassID.startsWith("<AREA>")
			||uClassID.startsWith("<ROOM>"))
			{
				final List<XMLTag> pieces=CMLib.xml().parseAllXML(classID);
				if(pieces.size()>0)
				{
					final XMLTag tag = CMLib.xml().getPieceFromPieces(pieces, "ITEM");
					if(tag != null)
					{
						final String realClassID=CMLib.xml().getValFromPieces(tag.contents(), "CLASSID");
						if((realClassID!=null)
						&&(realClassID.length()>0))
						{
							if(uClassID.startsWith("<ITEM>"))
								return CMClass.getItemPrototype(realClassID);
							if(uClassID.startsWith("<MOB>"))
								return CMClass.getMOBPrototype(realClassID);
							if(uClassID.startsWith("<AREA>"))
								return CMClass.getAreaType(realClassID);
							if(uClassID.startsWith("<ROOM>"))
								return CMClass.getLocalePrototype(realClassID);
						}
					}
				}
				return CMClass.getItemPrototype("GenDrink");
			}
			else
			{
				final PhysicalAgent I=CMClass.getItemPrototype(classID);
				if(I==null)
				{
					final Pair<String[],String[]> codeFlags = getBuildingCodesNFlags();
					if(CMParms.containsIgnoreCase(codeFlags.first, classID))
						return classID.toUpperCase().trim();
				}
				return I;
			}
		}
		if(classIDRequired)
			return null;
		return CMClass.getItemPrototype("StdItem");
	}

	@SuppressWarnings("unchecked")
	protected Pair<String[],String[]> getBuildingCodesNFlags()
	{
		Pair<String[],String[]> codesFlags = (Pair<String[],String[]>)Resources.getResource("BUILDING_SKILL_CODES_FLAGS");
		if(codesFlags == null)
		{
			RecipeDriven A=(RecipeDriven)CMClass.getAbility("Masonry");
			if(A==null)
				A=(RecipeDriven)CMClass.getAbility("Construction");
			if(A==null)
				A=(RecipeDriven)CMClass.getAbility("Excavation");
			if(A!=null)
				A.getRecipeFormat();
			codesFlags = (Pair<String[],String[]>)Resources.getResource("BUILDING_SKILL_CODES_FLAGS");
		}
		return codesFlags;
	}

	protected String stripData(final StringBuffer str, final String div)
	{
		final StringBuffer data = new StringBuffer("");
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
		if((div.charAt(0)=='\n') && (data.length()>0))
			return data.toString();
		return null;
	}

	@SuppressWarnings("unchecked")
	protected Vector<AbilityRecipeRow> parseDataRows(final StringBuffer recipeData, final List<? extends Object> columnsV, final int numberOfDataColumns)
		throws CMException
	{
		StringBuffer str = new StringBuffer(recipeData.toString());
		str = cleanDataRowEOLs(str);
		final Vector<AbilityRecipeRow> rowsV = new Vector<AbilityRecipeRow>();
		AbilityRecipeRow dataRow = new AbilityRecipeRow();
		List<String> currCol = null;
		String lastDiv = null;

		// detect item xml, and to special line parse
		{
			final int openTagDex=str.indexOf("<");
			if((openTagDex>=0)
			&&(str.substring(0,openTagDex).trim().length()==0)
			&&(columnsV.size()==1))
			{
				String xml = str.toString().trim();
				String uxml = xml.toUpperCase();
				if(uxml.startsWith("<ITEMS>")&&(uxml.endsWith("</ITEMS>")))
				{
					currCol=(List<String>)columnsV.get(0);
					str.setLength(0);
					xml = xml.substring(7,xml.length()-8).trim();
					uxml = uxml.substring(7,uxml.length()-8).trim();

					while(uxml.startsWith("<ITEM>"))
					{
						final int closeTagDex=uxml.indexOf("</ITEM>");
						final String itemRow = xml.substring(0,closeTagDex+7);
						uxml=uxml.substring(closeTagDex+7).trim();
						xml=xml.substring(closeTagDex+7).trim();
						dataRow.add(currCol,itemRow);
						rowsV.addElement(dataRow);
						dataRow = new AbilityRecipeRow();
					}
					str.setLength(0);
					str.append(xml);
				}
			}
		}

		int lastLen = str.length();
		while(str.length() > 0)
		{
			if(str.charAt(0)=='#')
			{
				final int x=str.indexOf("\n");
				if(x>0)
				{
					str.delete(0, x+1);
					while((str.length()>0)
					&&((str.charAt(0)=='\r')||(str.charAt(0)=='\n')))
						str.delete(0, 1);
					continue;
				}
			}
			lastLen = str.length();
			for(int c = 0; c < columnsV.size(); c++)
			{
				String div = "\n";
				currCol = null;
				if(columnsV.get(c) instanceof String)
					stripData(str,(String)columnsV.get(c));
				else
				if(columnsV.get(c) instanceof List)
				{
					currCol = (List<String>)columnsV.get(c);
					if(c<columnsV.size()-1)
					{
						div = (String)columnsV.get(c+1);
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
					dataRow.add(currCol,data);
					currCol = null;
				}
				else
				{
					final String data = stripData(str,lastDiv);
					if(data == null)
						break;
					dataRow.add(currCol,data);
				}
			}
			if(dataRow.size() != numberOfDataColumns)
				throw new CMException("Row "+(rowsV.size()+1)+" has "+dataRow.size()+"/"+numberOfDataColumns);
			rowsV.addElement(dataRow);
			dataRow = new AbilityRecipeRow();
			if(str.length()==lastLen)
				throw new CMException("UNCHANGED: Row "+(rowsV.size()+1)+" has "+dataRow.size()+"/"+numberOfDataColumns);
		}
		if(str.length()<2)
			str.setLength(0);
		return rowsV;
	}

	protected boolean fixDataColumn(final AbilityRecipeRow dataRow, final int rowShow) throws CMException
	{
		final Object classModelI = getSampleObject(dataRow);
		return fixDataColumn(dataRow,rowShow,classModelI);
	}

	@SuppressWarnings("unchecked")
	protected boolean fixDataColumn(final AbilityRecipeRow dataRow, final int rowShow, final Object classModelI) throws CMException
	{
		final Map<String,AbilityParmEditor> editors = getEditors();
		for(int d=0;d<dataRow.size();d++)
		{
			if(!(dataRow.get(d).first instanceof List))
				throw new CMException(L("Data row @x1 discarded due to non-List at @x2",""+rowShow,""+d));

			final List<String> colV=(List<String>)dataRow.get(d).first;
			if(colV.size()==1)
			{
				AbilityParmEditor A = editors.get(colV.get(0));
				if((A == null)||(A.appliesToClass(classModelI)<0))
					A = editors.get("N_A");
				dataRow.get(d).first=A.ID();
			}
			else
			{
				if(classModelI == null)
				{
					//Log.errOut("CMAbleParms","Data row "+rowShow+" discarded due to null/empty classID");
					throw new CMException(L("Data row @x1 discarded due to null/empty classID",""+rowShow));
				}
				AbilityParmEditor applicableA = null;
				for(int c=0;c<colV.size();c++)
				{
					final AbilityParmEditor A = editors.get(colV.get(c));
					if(A==null)
						throw new CMException(L("Col name @x1 is not defined.",""+(colV.get(c))));
					if((applicableA==null)
					||(A.appliesToClass(classModelI) > applicableA.appliesToClass(classModelI)))
						applicableA = A;
				}
				if((applicableA == null)||(applicableA.appliesToClass(classModelI)<0))
					applicableA = editors.get("N_A");
				dataRow.get(d).first=applicableA.ID();
			}
			final AbilityParmEditor A = editors.get(dataRow.get(d).first);
			if(A==null)
			{
				if(classModelI instanceof CMObject)
					throw new CMException(L("Item id @x1 has no editor for @x2",((CMObject)classModelI).ID(),((String)dataRow.get(d).first)));
				else
					throw new CMException(L("Item id @x1 has no editor for @x2",classModelI+"",((String)dataRow.get(d).first)));
				//Log.errOut("CMAbleParms","Item id "+classModelI.ID()+" has no editor for "+((String)dataRow.elementAt(d,1)));
				//return false;
			}
			else
			if((rowShow>=0)
			&&(!A.confirmValue(dataRow.get(d).second, null)))
			{
				final String data = dataRow.get(d).second.replace('@', ' ');
				if(classModelI instanceof CMObject)
					throw new CMException(L("Item id @x1 has bad data '@x2' for column @x3 at row @x4",((CMObject)classModelI).ID(),data,((String)dataRow.get(d).first),""+rowShow));
				else
					throw new CMException(L("Item id @x1 has bad data '@x2' for column @x3 at row @x4",""+classModelI,data,((String)dataRow.get(d).first),""+rowShow));
				//Log.errOut("CMAbleParms","Item id "+classModelI.ID()+" has bad data '"+((String)dataRow.elementAt(d,2))+"' for column "+((String)dataRow.elementAt(d,1))+" at row "+rowShow);
			}
		}
		return true;
	}

	protected void fixDataColumns(final Vector<AbilityRecipeRow> rowsV) throws CMException
	{
		AbilityRecipeRow dataRow;
		for(int r=0;r<rowsV.size();r++)
		{
			dataRow=rowsV.elementAt(r);
			if(!fixDataColumn(dataRow,r))
				throw new CMException(L("Unknown error in row @x1",""+r));
			/*
			catch(CMException e)
			{
				rowsV.removeElementAt(r);
				r--;
			}
			*/
		}
	}

	protected StringBuffer cleanDataRowEOLs(final StringBuffer str)
	{
		if(str.indexOf("\n")<0)
			return new StringBuffer(str.toString().replace('\r','\n'));
		for(int i=str.length()-1;i>=0;i--)
		{
			if(str.charAt(i)=='\r')
				str.delete(i,i+1);
		}
		return str;
	}

	@Override
	public void testRecipeParsing(final StringBuffer recipesString, final String recipeFormat) throws CMException
	{
		testRecipeParsing(recipesString,recipeFormat,null);
	}

	@Override
	public void testRecipeParsing(final String recipeFilename, final String recipeFormat, final boolean save) throws CMException
	{
		final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+recipeFilename,null,CMFile.FLAG_LOGERRORS).text();
		testRecipeParsing(str,recipeFormat,save?recipeFilename:null);
	}

	@SuppressWarnings("unchecked")
	public void testRecipeParsing(final StringBuffer str, final String recipeFormat, final String saveRecipeFilename) throws CMException
	{
		final Vector<? extends Object> columnsV = parseRecipeFormatColumns(recipeFormat);
		int numberOfDataColumns = 0;
		for(int c = 0; c < columnsV.size(); c++)
		{
			if(columnsV.elementAt(c) instanceof List)
				numberOfDataColumns++;
		}
		final Vector<AbilityRecipeRow> rowsV = parseDataRows(str,columnsV,numberOfDataColumns);
		final Vector<String> convertedColumnsV=(Vector<String>)columnsV;
		fixDataColumns(rowsV);
		final Map<String,AbilityParmEditor> editors = getEditors();
		AbilityRecipeRow editRow = null;
		final int[] showNumber = {0};
		final int showFlag =-999;
		final MOB mob=CMClass.getFactoryMOB();
		final Session fakeSession = (Session)CMClass.getCommon("FakeSession");
		mob.setSession(fakeSession);
		fakeSession.setMob(mob);
		for(int r=0;r<rowsV.size();r++)
		{
			editRow = rowsV.elementAt(r);
			for(int a=0;a<editRow.size();a++)
			{
				final AbilityParmEditor A = editors.get(editRow.get(a).first);
				try
				{
					final String oldVal = editRow.get(a).second;
					if(fakeSession.getHistory().size()>0)
						fakeSession.getHistory().getLast().clear();
					fakeSession.getHistory().getLast().addAll(new XVector<String>(A.fakeUserInput(oldVal)));
					final String newVal = A.commandLinePrompt(mob,oldVal,showNumber,showFlag);
					editRow.get(a).second=newVal;
				}
				catch (final IOException e)
				{
				}
				catch (final Exception e)
				{
					Log.errOut(e);
				}
			}
		}
		fakeSession.setMob(null);
		mob.destroy();
		if(saveRecipeFilename!=null)
			resaveRecipeFile(mob,saveRecipeFilename,rowsV,convertedColumnsV,false);
	}

	protected void calculateRecipeCols(final int[] lengths, final String[] headers, final List<AbilityRecipeRow> rowsV)
	{
		final Map<String,AbilityParmEditor> editors = getEditors();
		AbilityRecipeRow dataRow = null;
		final int numRows[]=new int[headers.length];
		for(int r=0;r<rowsV.size();r++)
		{
			dataRow=rowsV.get(r);
			for(int c=0;c<dataRow.size();c++)
			{
				final AbilityParmEditor A = editors.get(dataRow.get(c).first);
				try
				{
					int dataLen=dataRow.get(c).second.length();
					if(dataLen > A.maxColWidth())
						dataLen = A.maxColWidth();
					if(dataLen < A.minColWidth())
						dataLen = A.minColWidth();
					lengths[c]+=dataLen;
					numRows[c]++;
				}
				catch(final Exception e)
				{
				}
				if(A==null)
					Log.errOut("CMAbleParms","Inexplicable lack of a column: "+((String)dataRow.get(c).first));
				else
				if(headers[c] == null)
				{
					headers[c] = A.colHeader();
				}
				else
				if((!headers[c].startsWith("#"))&&(!headers[c].equalsIgnoreCase(A.colHeader())))
				{
					headers[c]="#"+c;
				}
			}
		}
		for(int i=0;i<headers.length;i++)
		{
			if(numRows[i]>0)
			{
				lengths[i] /= numRows[i];
				if(lengths[i]+headers.length>50)
					lengths[i]=50-headers.length;
			}
			if(headers[i]==null)
				headers[i]="*Add*";
		}
		int currLenTotal = 0;
		for (final int length : lengths)
			currLenTotal+=length;
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

	@Override
	public AbilityRecipeData parseRecipe(final String recipeFilename, final String recipeFormat)
	{
		final AbilityRecipeDataImpl recipe = new AbilityRecipeDataImpl(recipeFilename, recipeFormat);
		return recipe;
	}

	@Override
	public StringBuffer getRecipeList(final RecipeDriven iA)
	{
		final AbilityRecipeData recipe = parseRecipe(iA.getRecipeFilename(),iA.getRecipeFormat());
		if(recipe.parseError() != null)
			return new StringBuffer("File: "+iA.getRecipeFilename()+": "+recipe.parseError());
		return getRecipeList(recipe);
	}

	private StringBuffer getRecipeList(final AbilityRecipeData recipe)
	{
		final Map<String,AbilityParmEditor> editors = getEditors();
		final StringBuffer list=new StringBuffer("");
		AbilityRecipeRow dataRow = null;
		list.append("### ");
		for(int l=0;l<recipe.columnLengths().length;l++)
			list.append(CMStrings.padRight(recipe.columnHeaders()[l],recipe.columnLengths()[l])+" ");
		list.append("\n\r");
		for(int r=0;r<recipe.dataRows().size();r++)
		{
			dataRow=recipe.dataRows().get(r);
			list.append(CMStrings.padRight(""+(r+1),3)+" ");
			for(int c=0;c<dataRow.size();c++)
			{
				final Object fieldType = dataRow.get(c).first;
				final AbilityParmEditor A = editors.get(fieldType);
				String value=dataRow.get(c).second;
				if(A!=null)
					value=A.commandLineValue(value);
				final String colVal = CMStrings.limit(value,recipe.columnLengths()[c]);
				list.append(CMStrings.padRight(colVal,recipe.columnLengths()[c])+" ");
			}
			list.append("\n\r");
		}
		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void modifyRecipesList(final MOB mob, final String recipeFilename, final String recipeFormat) throws java.io.IOException
	{
		final Map<String,AbilityParmEditor> editors = getEditors();
		final AbilityRecipeData recipe = parseRecipe(recipeFilename, recipeFormat);
		if(recipe.parseError() != null)
		{
			Log.errOut("CMAbleParms","File: "+recipeFilename+": "+recipe.parseError());
			return;
		}
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer list=getRecipeList(recipe);
			mob.tell(list.toString());
			final String lineNum = mob.session().prompt(L("\n\rEnter a line to edit, A to add, or ENTER to exit: "),"");
			if(lineNum.trim().length()==0)
				break;
			AbilityRecipeRow editRow = null;
			if(lineNum.equalsIgnoreCase("A"))
			{
				editRow = recipe.blankRow();
				final int keyIndex = getClassFieldIndex(editRow);
				String classFieldData = null;
				if(keyIndex>=0)
				{
					final AbilityParmEditor A = editors.get(((List<String>)editRow.get(keyIndex).first).get(0));
					if(A!=null)
					{
						classFieldData = A.commandLinePrompt(mob,editRow.get(keyIndex).second,new int[]{0},-999);
						if(!A.confirmValue(classFieldData, null))
						{
							mob.tell(L("Invalid value.  Aborted."));
							continue;
						}
					}
				}
				editRow=recipe.newRow(classFieldData);
				if(editRow==null)
					continue;
				recipe.dataRows().add(editRow);
			}
			else
			if(CMath.isInteger(lineNum))
			{
				final int line = CMath.s_int(lineNum);
				if((line<1)||(line>recipe.dataRows().size()))
					continue;
				editRow = recipe.dataRows().get(line-1);
			}
			else
				break;
			if(editRow != null)
			{
				int showFlag=-1;
				if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
					showFlag=-999;
				boolean ok=false;
				while(!ok)
				{
					final int[] showNumber = {0};
					final int keyIndex = getClassFieldIndex(editRow);
					for(int a=0;a<editRow.size();a++)
					{
						final Object editorName = editRow.get(a).first;
						if((a!=keyIndex)
						||(editors.containsKey(editorName)))
						{
							final AbilityParmEditor A = editors.get(editorName);
							final String newVal = A.commandLinePrompt(mob,editRow.get(a).second,showNumber,showFlag);
							editRow.get(a).second=newVal;
						}
					}
					if(showFlag<-900)
					{
						ok=true;
						break;
					}
					if(showFlag>0)
					{
						showFlag=-1;
						continue;
					}
					showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
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
			final String prompt;
			if(recipe.wasVFS())
				prompt="Save to V)FS, F)ilesystem, or C)ancel (V/f/c): ";
			else
				prompt="Save to V)FS, F)ilesystem, or C)ancel (v/F/c): ";
			final String choice=mob.session().choose(prompt,("VFC"),recipe.wasVFS()?("V"):("F"));
			if(choice.equalsIgnoreCase("C"))
				mob.tell(L("Cancelled."));
			else
			{
				final boolean saveToVFS = choice.equalsIgnoreCase("V");
				resaveRecipeFile(mob, recipeFilename,recipe.dataRows(),recipe.columns(),saveToVFS);
			}
		}
	}

	@Override
	public void resaveRecipeFile(final MOB mob, final String recipeFilename, final List<AbilityRecipeRow> rowsV, final List<? extends Object> columnsV, final boolean saveToVFS)
	{
		final StringBuffer saveBuf = new StringBuffer("");
		for(int r=0;r<rowsV.size();r++)
		{
			final AbilityRecipeRow dataRow = rowsV.get(r);
			int dataDex = 0;
			for(int c=0;c<columnsV.size();c++)
			{
				final String newRowVal;
				if(columnsV.get(c) instanceof String)
					newRowVal = (String)columnsV.get(c);
				else
					newRowVal = dataRow.get(dataDex++).second.toString();
				saveBuf.append(newRowVal);
			}
			saveBuf.append("\n");
		}
		if((saveBuf.length()>10)
		&&(saveBuf.substring(0,8).trim().toUpperCase().startsWith("<ITEM>")))
		{
			saveBuf.insert(0, "<ITEMS>");
			saveBuf.append("</ITEMS>");
		}
		CMFile file = new CMFile((saveToVFS?"::":"//")+Resources.buildResourcePath("skills")+recipeFilename,null,CMFile.FLAG_LOGERRORS);
		if(!file.canWrite())
			Log.errOut("CMAbleParms","File: "+recipeFilename+" can not be written");
		else
		if((!file.exists())||(!file.text().equals(saveBuf)))
		{
			file.saveText(saveBuf);
			if(!saveToVFS)
			{
				file = new CMFile("::"+Resources.buildResourcePath("skills")+recipeFilename,null,CMFile.FLAG_LOGERRORS);
				if((file.exists())&&(file.canWrite()))
				{
					file.saveText(saveBuf);
				}
			}
			if(mob != null)
				Log.sysOut("CMAbleParms","User: "+mob.Name()+" modified "+(saveToVFS?"VFS":"Local")+" file "+recipeFilename);
			Resources.removeResource("PARSED_RECIPE: "+recipeFilename);
			Resources.removeMultiLists(recipeFilename);
		}
	}

	@Override
	public synchronized Map<String,AbilityParmEditor> getEditors()
	{
		if(DEFAULT_EDITORS != null)
			return DEFAULT_EDITORS;

		DEFAULT_EDITORS = new Hashtable<String,AbilityParmEditor>();
		final String filePath="com/planet_ink/coffee_mud/Libraries/editors";
		final CMProps page = CMProps.instance();
		final List<Object> editors=CMClass.loadClassList(filePath,page.getStr("LIBRARY"),"/editors",AbilityParmEditor.class,true);
		for(int f=0;f<editors.size();f++)
		{
			final AbilityParmEditor editor= (AbilityParmEditor)editors.get(f);
			if(!Modifier.isAbstract(editor.getClass().getModifiers()))
				DEFAULT_EDITORS.put(editor.ID().toUpperCase().trim(),editor);
		}
		return DEFAULT_EDITORS;
	}

	protected class AbilityRecipeDataImpl implements AbilityRecipeData
	{
		private String						recipeFilename;
		private String						recipeFormat;
		private Vector<Object>				columns;
		private Vector<AbilityRecipeRow>	dataRows;
		private int							numberOfDataColumns;
		public String[]						columnHeaders;
		public int[]						columnLengths;
		public int							classFieldIndex;
		private String						parseError	= null;
		private boolean						wasVFS		= false;

		public AbilityRecipeDataImpl(final String recipeFilename, final String recipeFormat)
		{
			this.recipeFilename = recipeFilename;
			this.recipeFormat = recipeFormat;
			if(recipeFilename.trim().length()==0)
			{
				parseError = "No file";
				return;
			}
			final CMFile F = new CMFile(Resources.buildResourcePath("skills")+recipeFilename,null,CMFile.FLAG_LOGERRORS);
			wasVFS=F.isVFSFile();
			final StringBuffer str=F.text();
			columns = parseRecipeFormatColumns(recipeFormat);
			numberOfDataColumns = 0;
			for(int c = 0; c < columns.size(); c++)
			{
				if(columns.elementAt(c) instanceof List)
					numberOfDataColumns++;
			}
			dataRows = null;
			try
			{
				dataRows = parseDataRows(str,columns,numberOfDataColumns);
				final AbilityRecipeRow editRow = new AbilityRecipeRow();
				for(int c=0;c<columns().size();c++)
				{
					if(columns().elementAt(c) instanceof List)
						editRow.add(columns().elementAt(c),"");
				}
				if(editRow.size()==0)
				{
					//classFieldIndex = CMAbleParms.getClassFieldIndex(dataRow);
				}
				else
					classFieldIndex = CMAbleParms.getClassFieldIndex(editRow);
				fixDataColumns(dataRows);
			}
			catch(final CMException e)
			{
				parseError = e.getMessage();
				return;
			}
			columnLengths = new int[numberOfDataColumns];
			columnHeaders = new String[numberOfDataColumns];
			calculateRecipeCols(columnLengths,columnHeaders,dataRows);
		}

		@Override
		public boolean wasVFS()
		{
			return wasVFS;
		}

		@Override
		public AbilityRecipeRow newRow(final String classFieldData)
		{
			final AbilityRecipeRow editRow = blankRow();
			final int keyIndex =classFieldIndex;
			if((keyIndex>=0)&&(classFieldData!=null))
			{
				editRow.get(keyIndex).second=classFieldData;
			}
			try
			{
				fixDataColumn(editRow,-1);
			}
			catch (final CMException cme)
			{
				return null;
			}
			for(int i=0;i<editRow.size();i++)
			{
				if(i!=keyIndex)
				{
					final AbilityParmEditor A = getEditors().get(editRow.get(i).first);
					editRow.get(i).second=A.defaultValue();
				}
			}
			return editRow;
		}

		@Override
		public AbilityRecipeRow blankRow()
		{
			final AbilityRecipeRow editRow = new AbilityRecipeRow();
			for(int c=0;c<columns().size();c++)
			{
				if(columns().elementAt(c) instanceof List)
					editRow.add(columns().elementAt(c),"");
			}
			return editRow;
		}

		@Override
		public int getClassFieldIndex()
		{
			return classFieldIndex;
		}

		@Override
		public String recipeFilename()
		{
			return recipeFilename;
		}

		@Override
		public String recipeFormat()
		{
			return recipeFormat;
		}

		@Override
		public Vector<AbilityRecipeRow> dataRows()
		{
			return dataRows;
		}

		@Override
		public Vector<? extends Object> columns()
		{
			return columns;
		}

		@Override
		public int[] columnLengths()
		{
			return columnLengths;
		}

		@Override
		public String[] columnHeaders()
		{
			return columnHeaders;
		}

		@Override
		public int numberOfDataColumns()
		{
			return numberOfDataColumns;
		}

		@Override
		public String parseError()
		{
			return parseError;
		}
	}

	protected abstract class AbilityParmEditorImpl implements AbilityParmEditor
	{
		private final String	ID;
		private final ParmType	fieldType;
		private String			prompt	= null;
		private String			header	= null;

		protected PairList<String, String>	choices	= null;

		public AbilityParmEditorImpl(final String fieldName, final String shortHeader, final ParmType type)
		{
			ID=fieldName;
			fieldType = type;
			header = shortHeader;
			prompt = CMStrings.capitalizeAndLower(CMStrings.replaceAll(ID,"_"," "));
			createChoices();
		}

		@Override
		public String ID()
		{
			return ID;
		}

		@Override
		public ParmType parmType()
		{
			return fieldType;
		}

		@Override
		public String prompt()
		{
			return prompt;
		}

		@Override
		public String colHeader()
		{
			return header;
		}

		@Override
		public int maxColWidth()
		{
			return Integer.MAX_VALUE;
		}

		@Override
		public int minColWidth()
		{
			return 0;
		}

		@Override
		public boolean confirmValue(final String oldVal, Map<String, String> args)
		{
			final boolean spaceOK = fieldType != ParmType.ONEWORD;
			boolean emptyOK = false;
			switch(fieldType)
			{
			case STRINGORNULL:
				emptyOK = true;
			//$FALL-THROUGH$
			case ONEWORD:
			case STRING:
			{
				if((!spaceOK) && (oldVal.indexOf(' ') >= 0))
					return false;
				return (emptyOK)||(oldVal.trim().length()>0);
			}
			case NUMBERORNULL:
				if(oldVal.length()==0)
					return true;
				//$FALL-THROUGH$
			case NUMBER:
				return CMath.isInteger(oldVal);
			case DECIMAL:
				return CMath.isInteger(oldVal)||CMath.isDouble(oldVal);
			case NUMBER_PAIR:
			{
				if(oldVal==null)
					return emptyOK;
				final int x=oldVal.indexOf(',');
				return CMath.isInteger(oldVal)
					||((x>0)&&(CMath.isInteger(oldVal.substring(0,x).trim()))&&(CMath.isInteger(oldVal.substring(x+1).trim())));
			}
			case CHOICES:
				if(!CMStrings.contains(choices.toArrayFirst(new String[0]),oldVal))
					return CMStrings.contains(choices.toArrayFirst(new String[0]),oldVal.toUpperCase().trim());
				return true;
			case MULTICHOICES:
				return CMath.isInteger(oldVal)||choices().containsFirst(oldVal);
			case CONFIRM:
			case CHECK:
				return CMath.isBool(oldVal);
			case SPECIAL:
				break;
			}
			return false;
		}

		@Override
		public String[] fakeUserInput(final String oldVal)
		{
			boolean emptyOK = false;
			switch(fieldType)
			{
			case STRINGORNULL:
				emptyOK = true;
			//$FALL-THROUGH$
			case ONEWORD:
			case STRING:
			{
				if(emptyOK && (oldVal.trim().length()==0))
					return new String[]{"NULL"};
				return new String[]{oldVal};
			}
			case NUMBERORNULL:
				if(oldVal.trim().length()==0)
					return new String[]{"NULL"};
				//$FALL-THROUGH$
			case NUMBER:
			case DECIMAL:
			case NUMBER_PAIR:
				return new String[]{oldVal};
			case CHOICES:
			{
				if(oldVal.trim().length()==0) return new String[]{"NULL"};
				final Vector<String> V = new XVector<String>(choices.toArrayFirst(new String[0]));
				for(int v=0;v<V.size();v++)
				{
					if(oldVal.equalsIgnoreCase(V.elementAt(v)))
						return new String[]{choices.get(v).second};
				}
				return new String[]{oldVal};
			}
			case MULTICHOICES:
				if(oldVal.trim().length()==0)
					return new String[]{"NULL"};
				if(!CMath.isInteger(oldVal))
				{
					final Vector<String> V = new XVector<String>(choices.toArrayFirst(new String[0]));
					for(int v=0;v<V.size();v++)
					{
						if(oldVal.equalsIgnoreCase(V.elementAt(v)))
							return new String[]{choices.get(v).second,""};
					}
				}
				else
				{
					final Vector<String> V = new Vector<String>();
					for(int c=0;c<choices.size();c++)
					{
						if(CMath.bset(CMath.s_int(oldVal),CMath.s_int(choices.get(c).first)))
						{
							V.addElement(choices.get(c).second);
							V.addElement(choices.get(c).second);
						}
					}
					if(V.size()>0)
					{
						V.addElement("");
						return CMParms.toStringArray(V);
					}
				}
				return new String[]{"NULL"};
			case CONFIRM:
			case CHECK:
				return new String[]{CMath.s_bool(oldVal)?"true":"false"};
			case SPECIAL:
				break;
			}
			return new String[]{};
		}

		@Override
		public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag)
		throws java.io.IOException
		{
			String str = null;
			boolean emptyOK = false;
			final boolean spaceOK = fieldType != ParmType.ONEWORD;
			switch(fieldType)
			{
			case STRINGORNULL:
				emptyOK = true;
			//$FALL-THROUGH$
			case ONEWORD:
			case STRING:
			{
				++showNumber[0];
				boolean proceed = true;
				while(proceed&&(mob.session()!=null)&&(!mob.session().isStopped()))
				{
					str = CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),emptyOK).trim();
					if((!spaceOK) && (str.indexOf(' ') >= 0))
						mob.tell(L("Spaces are not allowed here."));
					else
						proceed=false;
				}
				break;
			}
			case NUMBERORNULL:
			case NUMBER:
			{
				final String newStr=CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),true);
				if(newStr.trim().length()==0)
					str="";
				else
					str = Integer.toString(CMath.s_int(newStr));
				break;
			}
			case DECIMAL:
			{
				final String newStr=CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),true);
				if(newStr.trim().length()==0)
					str="";
				else
					str = newStr.trim();
				break;
			}
			case NUMBER_PAIR:
			{
				final String newStr=CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),true);
				if(newStr.trim().length()==0)
					str="";
				else
				if(CMath.isInteger(newStr))
					str = Integer.toString(CMath.s_int(newStr));
				else
				{
					final int x=newStr.indexOf(',');
					if(x>0)
					{
						str = Integer.toString(CMath.s_int(newStr.substring(0,x).trim()))
							+ Integer.toString(CMath.s_int(newStr.substring(x+1).trim()));
					}
				}
				break;
			}
			case CHOICES:
				str = CMLib.genEd().promptMultiOrExtra(mob,oldVal,++showNumber[0],showFlag,prompt(),choices);
				break;
			case MULTICHOICES:
				str = CMLib.genEd().promptMultiOrExtra(mob,oldVal,++showNumber[0],showFlag,prompt(),choices);
				if(CMath.isInteger(str))
					str = Integer.toString(CMath.s_int(str));
				break;
			case CONFIRM:
			case CHECK:
			{
				final String newStr=CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),true);
				if(newStr.trim().length()==0)
					str="";
				else
					str = CMath.s_bool(newStr)?"true":"false";
				break;
			}
			case SPECIAL:
				break;
			}
			return str;
		}

		@Override
		public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
		{
			final String webValue = httpReq.getUrlParameter(fieldName);
			switch(fieldType)
			{
			case ONEWORD:
			case STRINGORNULL:
			case STRING:
			case NUMBER:
			case DECIMAL:
			case NUMBERORNULL:
			case NUMBER_PAIR:
			case CONFIRM:
			case CHECK:
				return (webValue == null)?oldVal:webValue;
			case MULTICHOICES:
			{
				if(webValue == null)
					return oldVal;
				String id="";
				long num=0;
				int index=0;
				for(;httpReq.isUrlParameter(fieldName+id);id=""+(++index))
				{
					final String newVal = httpReq.getUrlParameter(fieldName+id);
					if(CMath.s_long(newVal)<=0)
						return newVal;
					num |= CMath.s_long(newVal);
				}
				return ""+num;
			}
			case CHOICES:
				return (webValue == null)?oldVal:webValue;
			case SPECIAL:
				break;
			}
			return "";
		}

		@Override
		public String webTableField(final HTTPRequest httpReq, final java.util.Map<String, String> parms, final String oldVal)
		{
			return oldVal;
		}

		@Override
		public String commandLineValue(final String oldVal)
		{
			return oldVal;
		}

		@Override
		public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
		{
			int textSize = 50;
			final String webValue = webValue(httpReq,parms,oldVal,fieldName);
			String onChange = null;
			final Vector<String> choiceValues = new Vector<String>();
			switch(fieldType)
			{
			case ONEWORD:
				textSize = 10;
			//$FALL-THROUGH$
			case STRINGORNULL:
			case STRING:
				return "\n\r<INPUT TYPE=TEXT NAME=" + fieldName + " SIZE=" + textSize + " VALUE=\"" + webValue + "\">";
			case NUMBER:
			case DECIMAL:
			case NUMBERORNULL:
			case NUMBER_PAIR:
				return "\n\r<INPUT TYPE=TEXT NAME=" + fieldName + " SIZE=10 VALUE=\"" + webValue + "\">";
			case CONFIRM:
			{
				final boolean b = CMath.s_bool(webValue);
				final StringBuffer str=new StringBuffer("");
				str.append("\n\r<SELECT NAME="+fieldName+">");
				str.append("<OPTION VALUE=\"true\""+(b?" SELECTED":"")+">True</OPTION>");
				str.append("<OPTION VALUE=\"false\""+(!b?" SELECTED":"")+">False</OPTION>");
				return str.toString()+"</SELECT>";
			}
			case CHECK:
			{
				final boolean b = CMath.s_bool(webValue);
				return "\n\r<INPUT TYPE=CHECKBOX NAME="+fieldName+" VALUE=\"true\""+(b?" CHECKED":"")+">";
			}
			case MULTICHOICES:
			{
				onChange = " MULTIPLE ";
				if(!parms.containsKey("NOSELECT"))
					onChange+= "ONCHANGE=\"MultiSelect(this);\"";
				if(CMath.isInteger(webValue))
				{
					final int bits = CMath.s_int(webValue);
					for(int i=0;i<choices.size();i++)
					{
						final int bitVal =CMath.s_int(choices.get(i).first);
						if((bitVal>0)&&(CMath.bset(bits,bitVal)))
							choiceValues.addElement(choices.get(i).first);
					}
				}
			}
			//$FALL-THROUGH$
			case CHOICES:
			{
				if(choiceValues.size()==0)
					choiceValues.addElement(webValue);
				if((onChange == null)&&(!parms.containsKey("NOSELECT")))
					onChange = " ONCHANGE=\"Select(this);\"";
				else
				if(onChange==null)
					onChange="";
				final StringBuffer str= new StringBuffer("");
				str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
				for(int i=0;i<choices.size();i++)
				{
					final String option = (choices.get(i).first);
					str.append("<OPTION VALUE=\""+option+"\" ");
					for(int c=0;c<choiceValues.size();c++)
					{
						if(option.equalsIgnoreCase(choiceValues.elementAt(c)))
							str.append("SELECTED");
					}
					str.append(">"+(choices.get(i).second));
				}
				return str.toString()+"</SELECT>";
			}
			case SPECIAL:
				break;
			}
			return "";
		}

		public abstract void createChoices();

		@Override
		public PairList<String,String> createChoices(final Enumeration<? extends Object> e)
		{
			if(choices != null)
				return choices;
			choices = new PairVector<String,String>();
			Object o = null;
			for(;e.hasMoreElements();)
			{
				o = e.nextElement();
				if(o instanceof String)
					choices.add((String)o,CMStrings.capitalizeAndLower((String)o));
				else
				if(o instanceof Ability)
					choices.add(((Ability)o).ID(),((Ability)o).name());
				else
				if(o instanceof Behavior)
					choices.add(((Behavior)o).ID(),((Behavior)o).name());
				else
				if(o instanceof Race)
					choices.add(((Race)o).ID(),((Race)o).name());
				else
				if(o instanceof Environmental)
					choices.add(((Environmental)o).ID(),((Environmental)o).ID());
			}
			return choices;
		}

		@SuppressWarnings("unchecked")
		@Override
		public PairList<String,String> createChoices(final List<? extends Object> V)
		{
			return createChoices(new IteratorEnumeration<Object>((Iterator<Object>)V.iterator()));
		}

		@Override
		public PairList<String,String> createChoices(final String[] S)
		{
			final XVector<String> X=new XVector<String>(S);
			Collections.sort(X);
			return createChoices(X.elements());
		}

		public PairList<String,String> createBinaryChoices(final String[] S)
		{
			if(choices != null)
				return choices;
			choices = createChoices(new XVector<String>(S).elements());
			for(int i=0;i<choices.size();i++)
			{
				if(i==0)
					choices.get(i).first =Integer.toString(0);
				else
					choices.get(i).first = Integer.toString(1<<(i-1));
			}
			return choices;
		}

		public PairList<String,String> createNumberedChoices(final String[] S)
		{
			if(choices != null)
				return choices;
			choices = createChoices(new XVector<String>(S).elements());
			for(int i=0;i<choices.size();i++)
				choices.get(i).first = Integer.toString(i);
			return choices;
		}

		@Override
		public PairList<String, String> choices()
		{
			return choices;
		}

		@Override
		public int appliesToClass(final Object o)
		{
			return 0;
		}
	}

	@Override
	public String testParmsFormat(final String format, final String proposition)
	{
		final String text = (proposition == null) ? "" : proposition;
		final MTFail fail = new MTFail();
		try
		{
			if((format == null)||(format.trim().length()==0)||(format.trim().equals(CMParms.FORMAT_UNDEFINED)))
				return null; // empty format doesn't care
			final MTToken root = parseMTSequence(format, 0, format.length());
			final MTParse parse = new MTParse(text, fail, null);
			if(!parse.match(root, 0, null, ""))
				return fail.describe(text);
			if(parse.outIndex[0] < text.length())
			{
				int restIndex = parse.outIndex[0];
				while((restIndex < text.length())&&(Character.isWhitespace(text.charAt(restIndex))))
					restIndex++;
				if(restIndex < text.length())
				{
					if(fail.getBestIndex() > restIndex)
						return fail.describe(text);
					return L("unexpected text '@x1' at position @x2",snippetValue(text.substring(restIndex)),""+(restIndex+1));
				}
			}
			return null;
		}
		catch(final CMException e)
		{
			return L("malformed format: @x1",e.getMessage());
		}
	}

	@Override
	public String parmsEditorHtml(final HTTPRequest httpReq, final ParmEdit edit, final String format, final String parms) throws CMException
	{
		MTToken tree;
		if((format == null)||(format.trim().length() == 0)||(format.trim().equals(CMParms.FORMAT_UNDEFINED)))
			tree = null;
		else
			tree = this.parseMTSequence(format, 0, format.length());
		if(tree == null)
			return "<INPUT TYPE=TEXT SIZE=60 NAME=PEDITORFREE VALUE=\"" + escapeHtml(parms) + "\" ONKEYUP=\"ParmsEditorChanged();\" ONCHANGE=\"ParmsEditorChanged();\">";
		final StringBuilder str = new StringBuilder("");
		final Map<String, String> seeded = new HashMap<String, String>();
		final String text = (parms == null) ? "" : parms;
		final MTParse parse = new MTParse(text, new MTFail(), seeded);
		parse.match(tree, 0, null, "");
		final Map<String, String> values;
		if(this.parmRemovingPath(edit) != null)
			values = this.parmRemovedReps(edit, seeded);
		else
		{
			this.parmValuesWithFields(tree, "", edit, seeded);
			values = seeded;
		}
		this.parmsEditors(tree, "", 0, edit, httpReq, values, str, false);
		return str.toString();
	}

	@Override
	public String parmsEditorText(final ParmEdit edit, final String format, final String parms) throws CMException
	{
		final String text = (parms == null) ? "" : parms;
		if(edit.field("PEDITORSTATE") == null)
			return text;
		final String free = edit.field("PEDITORFREE");
		MTToken tree;
		if((format == null)||(format.trim().length() == 0)||(format.trim().equals(CMParms.FORMAT_UNDEFINED)))
			tree = null;
		else
			tree = this.parseMTSequence(format, 0, format.length());
		if(tree == null)
			return (free == null) ? text : free.trim();
		final StringBuilder str = new StringBuilder("");
		final Map<String, String> seeded = new HashMap<String, String>();
		final MTParse parse = new MTParse(text, new MTFail(), seeded);
		parse.match(tree, 0, null, "");
		final Map<String, String> folded;
		if(this.parmRemovingPath(edit) != null)
			folded = this.parmRemovedReps(edit, seeded);
		else
		{
			folded = new HashMap<String, String>(seeded);
			this.parmValuesWithFields(tree, "", edit, folded);
		}
		final String adding = edit.field("PADD");
		final boolean grew = (adding != null)&&(adding.length() > 0);
		if((!grew)&&(edit.field("PREMOVE") == null)&&(folded.equals(seeded)))
			return text;
		final Map<String, String> values = folded;
		this.parmsText(tree, "", edit, values, str);
		return str.toString();
	}

	private void parmValuesWithFields(final MTToken node, final String path, final ParmEdit edit, final Map<String, String> values)
	{
		if(node.children == null)
		{
			if(!this.isParmPlaceholder(node))
				return;
			final String field = edit.field("PV" + path);
			if((field != null)&&(field.length() > 0))
				values.put(path, field);
			return;
		}
		final MTToken[] children = node.children;
		if(node.kind == MTKind.ALTERNATION)
		{
			final int occurrences = node.repeats ? this.parmRepCount(edit, path, values) : 1;
			for(int c = 0; c < occurrences; c++)
			{
				final String choicePath = path + ((c > 0) ? "~" + c : "");
				final String field = edit.field("PA" + choicePath);
				if(field != null)
					values.put(choicePath, field);
				final int chosen = Math.max(0, Math.min(children.length - 1, CMath.s_int(values.get(choicePath))));
				this.parmValuesWithFields(children[chosen], choicePath + "_" + chosen, edit, values);
			}
			return;
		}
		final String on = edit.field("PG" + path);
		if(on != null)
			values.put(path, on);
		final int count = node.repeats ? this.parmRepCount(edit, path, values) : 1;
		for(int c = 0; c < count; c++)
		{
			for(int i = 0; i < children.length; i++)
				this.parmValuesWithFields(children[i], path + "_" + i + ((c > 0) ? "~" + c : ""), edit, values);
		}
	}

	/**
	 * The part whose rep the page asked to drop.
	 *
	 * @param edit what the page posted back
	 * @return the part, or null when no rep is being removed
	 */
	private String parmRemovingPath(final ParmEdit edit)
	{
		if(edit.op != ParmOp.REMOVE)
			return null;
		final String removingPath = edit.opPath;
		if((removingPath == null)||(removingPath.length() == 0))
			return null;
		// a rep's path ends in its own number, so a path without one names no rep
		final int tilde = removingPath.lastIndexOf('~');
		if((tilde < 0)||(tilde == (removingPath.length() - 1)))
			return null;
		for(int i = tilde + 1; i < removingPath.length(); i++)
		{
			if(!Character.isDigit(removingPath.charAt(i)))
				return null;
		}
		return removingPath;
	}
	
	/**
	 * Drops the rep named by the request from the values, moving every rep after
	 * it down one, so the fields on the page match the parms that are left.
	 *
	 * @param httpReq the request naming the rep to drop
	 * @param values the values read from the parms
	 * @return the values without that rep
	 */
	private Map<String, String> parmRemovedReps(final ParmEdit edit, final Map<String, String> values)
	{
		final String removingPath = this.parmRemovingPath(edit);
		if((removingPath == null)||(values == null))
			return values;
		final int tildeAt = removingPath.lastIndexOf('~');
		final int gone = CMath.s_int(removingPath.substring(tildeAt + 1));
		final String base = (tildeAt < 0) ? null : removingPath.substring(0, tildeAt);
		if((gone < 0)||(base == null))
			return values;
		final Map<String, String> moved = new HashMap<String, String>();
		for(final String key : values.keySet())
		{
			if((!key.equals(base))
			&&(!key.startsWith(base + "_"))
			&&(!key.startsWith(base + "~")))
			{
				moved.put(key, values.get(key));
				continue;
			}
			final String tail = key.substring(base.length());
			final int tilde = tail.indexOf('~');
			if(tilde < 0)
			{
				// a first rep carries no number of its own, so only a later one can be gone
				if(gone > 0)
					moved.put(key, values.get(key));
				continue;
			}
			int digits = tilde + 1;
			while((digits < tail.length())&&(Character.isDigit(tail.charAt(digits))))
				digits++;
			if(digits == (tilde + 1))
			{
				// the count the matcher recorded for this part includes the rep being dropped
				if((tilde == 0)
				&&(tail.substring(tilde + 1).equals("REPS"))
				&&(CMath.s_int(values.get(key)) > 1))
					moved.put(key, "" + (CMath.s_int(values.get(key)) - 1));
				else
					moved.put(key, values.get(key));
				continue;
			}
			final int rep = CMath.s_int(tail.substring(tilde + 1, digits));
			if(rep == gone)
				continue;
			final int now = (rep > gone) ? (rep - 1) : rep;
			moved.put(base + tail.substring(0, tilde) + ((now > 0) ? "~" + now : "") + tail.substring(digits), values.get(key));
		}
		return moved;
	}

	private boolean isParmPlaceholder(final MTToken node)
	{
		if(!(node.children == null))
			return false;
		final String name = node.literal.toUpperCase(Locale.ROOT).trim();
		if(name.length() == 0)
			return false;
		if(node.parameterArgs != null)
			return true;
		if(CMath.s_valueOf(ParmType.class,name) != null)
			return true;
		return getEditors().containsKey(name);
	}

	private int parmValueReps(final String path, final Map<String, String> values)
	{
		if(values != null)
		{
			// the matcher counted this part's reps while it read the parms
			final String counted = values.get(path + "~REPS");
			if((counted != null)&&(CMath.isInteger(counted)))
				return Math.max(1, CMath.s_int(counted));
		}
		int reps = 1;
		if(values == null)
			return reps;
		for(final String key : values.keySet())
		{
			if(!key.startsWith(path))
				continue;
			if((key.length() <= path.length())||(key.charAt(path.length()) != '~'))
				continue;
			final int tilde = path.length();
			final int end = key.indexOf('_', tilde + 1);
			final String num = (end < 0) ? key.substring(tilde + 1) : key.substring(tilde + 1, end);
			if((end < 0)||(key.endsWith("~GAP"))||(key.indexOf("~WS") >= 0))
				continue;
			final String held = values.get(key);
			if((CMath.isInteger(num))&&(held != null)&&(held.trim().length() > 0))
				reps = Math.max(reps, CMath.s_int(num) + 1);
		}
		return reps;
	}

	private int parmRepCount(final ParmEdit edit, final String path, final Map<String, String> values)
	{
		final int held = Math.max(1, this.parmValueReps(path, values));
		int reps = held;
		final String adding = edit.field("PADD");
		final boolean addingHere = (adding != null)&&(adding.length() > 0);
		final String drawn = (this.parmRemovingPath(edit) == null) ? edit.field("PREPS") : null;
		if((drawn != null)&&(drawn.trim().length() > 0))
		{
			for(final String rep : CMParms.parseCommas(drawn, true))
			{
				if(rep.startsWith(path + "~"))
				{
					final String num = rep.substring(path.length() + 1);
					if(CMath.isInteger(num))
						reps = Math.max(reps, CMath.s_int(num) + 1);
				}
			}
		}
		if(addingHere)
		{
			final String addingPath = edit.field("PADDPATH");
			if(((addingPath == null) ? (path.length() == 0) : addingPath.equalsIgnoreCase(path)))
				reps++;
		}
		return Math.max(1, reps);
	}

	private int parmChoice(final String repPath, final Map<String, String> values, final int branches)
	{
		if(branches <= 0)
			return 0;
		int chosen = 0;
		final String stored = values.get(repPath);
		if((stored != null)
		&&(stored.length() > 0)
		&&CMath.isInteger(stored))
			chosen = CMath.s_int(stored);
		if((chosen >= 0)&&(chosen < branches))
			return chosen;
		chosen = 0;
		for(int b = 0; b < branches; b++)
		{
			final String prefix = repPath + "_" + b;
			for(final String key : values.keySet())
			{
				if(!key.startsWith(prefix))
					continue;
				if(key.endsWith("~GAP"))
					continue;
				final String held = values.get(key);
				if((held == null)||(held.trim().length() == 0))
					continue;
				return b;
			}
		}
		return chosen;
	}

	private int parmChoiceAt(final ParmEdit edit, final String repPath, final Map<String, String> values, final int branches)
	{
		final String picked = (this.parmRemovingPath(edit) == null) ? edit.field("PA" + repPath) : null;
		if((picked != null)&&CMath.isInteger(picked.trim()))
		{
			final int choice = CMath.s_int(picked.trim());
			if((choice >= 0)&&(choice < branches))
				return choice;
		}
		return this.parmChoice(repPath, values, branches);
	}

	private boolean parmPartWritten(final String path, final Map<String, String> values)
	{
		if(values == null)
			return false;
		for(final String key : values.keySet())
		{
			if((key.equals(path))
			||(key.startsWith(path + "_"))
			||(key.startsWith(path + "~")))
				return true;
		}
		return false;
	}

	private boolean parmGroupOn(final String path, final Map<String, String> values)
	{
		if((values == null)||(values.size() == 0))
			return true;
		for(final String key : values.keySet())
		{
			if((key.equals(path))
			||(key.startsWith(path + "_"))
			||(key.startsWith(path + "~")))
				return true;
		}
		return false;
	}

	/**
	 * Makes the markup for a piece of literal text safe to sit in a page.
	 *
	 * @param text the text to write
	 * @return the text with the characters a page reads specially escaped
	 */
	private static String escapeHtml(final String text)
	{
		if((text == null)||(text.length() == 0))
			return "";
		final StringBuilder str = new StringBuilder();
		for(int i = 0; i < text.length(); i++)
		{
			final char c = text.charAt(i);
			switch(c)
			{
			case '&':
				str.append("&amp;");
				break;
			case '<':
				str.append("&lt;");
				break;
			case '>':
				str.append("&gt;");
				break;
			case '"':
				str.append("&quot;");
				break;
			default:
				str.append(c);
				break;
			}
		}
		return str.toString();
	}

	private boolean parmIsQuoteMarker(final MTToken node)
	{
		if(node.kind == MTKind.QUOTE_MARKER)
			return true;
		if(node.children == null)
			return node.literal.equals("\"");
		final MTToken[] children = node.children;
		return (children.length == 1)&&(children[0].children == null)&&(children[0].literal.equals("\""));
	}

	private boolean parmNeedsQuotes(final String value)
	{
		if(value == null)
			return false;
		for(int i = 0; i < value.length(); i++)
		{
			if(Character.isWhitespace(value.charAt(i)))
				return true;
		}
		return false;
	}

	private String parmCheckValue(final MTToken node)
	{
		if((!this.isParmPlaceholder(node))||(node.parameterArgs == null))
			return null;
		final ParmType type = (ParmType)CMath.s_valueOf(ParmType.class, node.literal.toUpperCase(Locale.ROOT));
		if(type != ParmType.CHECK)
			return null;
		return this.parmArg(node, "name");
	}

	private boolean parmQuoted(final MTToken node)
	{
		if(node.stop == MTStop.QUOTE)
			return true;
		if((node.stop == MTStop.CHAR) && (node.stopChar() == '"'))
			return true;
		if(node.stop == MTStop.TOKENS)
		{
			for(final String term : node.stopTokens())
			{
				if((term != null)&&(term.equals("\"")))
					return true;
			}
		}
		return false;
	}

	private String parmArg(final MTToken node, final String argName)
	{
		if(node.parameterArgs == null)
			return null;
		final String found = node.parameterArgs.get(argName.toUpperCase(Locale.ROOT));
		if(found != null)
			return found;
		for(final String key : node.parameterArgs.keySet())
		{
			if(key.equalsIgnoreCase(argName))
				return node.parameterArgs.get(key);
		}
		return null;
	}

	private String parmBranchText(final MTToken branch)
	{
		if((branch.children == null)&&(branch.parameterArgs != null))
		{
			final String prompt = this.parmArg(branch, "prompt");
			if((prompt != null)&&(prompt.trim().length() > 0))
				return prompt.trim();
		}
		final StringBuilder str = new StringBuilder();
		this.parmLabelText(branch, str);
		if(str.length() == 0)
		{
			if(branch.children == null)
				return branch.literal;
			return "?";
		}
		return str.toString();
	}

	private void parmLabelText(final MTToken node, final StringBuilder str)
	{
		if(node.children == null)
		{
			final String text = node.literal;
			if((text.length() == 1)&&("\"()[]".indexOf(text.charAt(0)) >= 0))
				return;
			// a placeholder is named by what it asks for, when it says so
			if((node.parameterArgs != null)&&(this.isParmPlaceholder(node)))
			{
				final String prompt = this.parmArg(node, "prompt");
				if((prompt != null)&&(prompt.trim().length() > 0))
				{
					str.append(prompt.trim());
					return;
				}
			}
			str.append(text);
			return;
		}
		final MTToken[] children = node.children;
		for(int i = 0; i < children.length; i++)
		{
			if((i > 0)&&(node.kind == MTKind.ALTERNATION))
				str.append("/");
			this.parmLabelText(children[i], str);
		}
	}

	private boolean parmHasField(final MTToken node)
	{
		if(node.children == null)
			return this.isParmPlaceholder(node);
		for(final MTToken child : node.children)
		{
			if(this.parmHasField(child))
				return true;
		}
		return false;
	}

	private String parmPartPrompt(final MTToken node)
	{
		if(node.children == null)
		{
			if(!this.isParmPlaceholder(node))
			{
				// a part that is only punctuation is named by the punctuation itself
				return node.literal.trim();
			}
			final String prompt = this.parmArg(node, "prompt");
			return (prompt == null) ? "" : prompt.trim();
		}
		if(node.children != null)
		{
			final MTToken[] children = node.children;
			if(children.length != 1)
				return "";
			return this.parmPartPrompt(children[0]);
		}
		return "";
	}

	private void parmPrompt(final MTToken node, final StringBuilder str)
	{
		if(node.parameterArgs == null)
			return;
		final String prompt = this.parmArg(node, "prompt");
		if((prompt == null)||(prompt.trim().length() == 0))
			return;
		str.append("<FONT COLOR=#C0C0C0>").append(escapeHtml(prompt.trim())).append("&nbsp;</FONT>");
	}

	private String parmEditorField(final MTToken node, final String path, final ParmEdit edit, final HTTPRequest httpReq, final Map<String, String> values)
	{
		// a check the format names has nothing to type, since the name is the value
		if(this.parmCheckValue(node) != null)
			return "";
		final String name = node.literal.toUpperCase(Locale.ROOT).trim();
		final String fieldName = "PV" + path;
		String value = values.get(path);
		// a true/false value is a box to tick, and the box says which it is
		if((httpReq != null)&&(edit.field("PC" + path) != null)&&(this.parmRemovingPath(edit) == null))
			value = (edit.field("PV" + path) != null) ? "true" : "false";
		if(value == null)
			value = "";
		// a true/false value is a box to tick, not something to type
		final ParmType parmType = (ParmType)CMath.s_valueOf(ParmType.class, name);
		if((parmType == ParmType.CONFIRM)||(parmType == ParmType.CHECK))
		{
			final StringBuilder str = new StringBuilder();
			str.append("<INPUT TYPE=CHECKBOX NAME=").append(escapeHtml(fieldName)).append(" VALUE=TRUE");
			if(CMath.s_bool(value))
				str.append(" CHECKED");
			str.append(" ONCLICK=\"ParmsEditorChanged();\">");
			str.append("<INPUT TYPE=HIDDEN NAME=PC").append(escapeHtml(path)).append(" VALUE=1>");
			return str.toString();
		}
		final AbilityParmEditor editor = getEditors().get(name);
		if(editor != null)
		{
			final String entry = editor.webField(httpReq, httpReq.getUrlParametersCopy(), value, fieldName);
			if((entry != null)&&(entry.trim().length() > 0)
				&&((entry.indexOf("<OPTION") >= 0)||(entry.indexOf("<SELECT") < 0)))
				return entry;
		}
		final String choices = this.parmArg(node, "choices");
		if((choices != null)&&(choices.length() > 0)&&(choices.indexOf(',') > 0))
		{
			final StringBuilder str = new StringBuilder();
			str.append("<SELECT NAME=").append(escapeHtml(fieldName)).append(" ONCHANGE=\"ParmsEditorChanged();\">");
			str.append("<OPTION VALUE=\"\"></OPTION>");
			for(final String choice : CMParms.parseCommas(choices, true))
			{
				final String unquoted = choice.trim();
				str.append("<OPTION VALUE=\"").append(escapeHtml(unquoted)).append("\"");
				if(unquoted.equalsIgnoreCase(value))
					str.append(" SELECTED");
				final String shown = ((unquoted == null)||(unquoted.length() <= 60)) ? unquoted : unquoted.substring(0, 60).trim();
				str.append(">").append(escapeHtml(shown)).append("</OPTION>");
			}
			str.append("</SELECT>");
			return str.toString();
		}
		return "<INPUT TYPE=TEXT SIZE=20 NAME=" + escapeHtml(fieldName) + " VALUE=\"" + escapeHtml(value)
			+ "\" ONKEYUP=\"ParmsEditorChanged();\" ONCHANGE=\"ParmsEditorChanged();\">";
	}


	private void parmsEditors(final MTToken node, final String path, final int depth, final ParmEdit edit, final HTTPRequest httpReq, final Map<String, String> values, final StringBuilder str, final boolean chosenBranch)
	{
		if(this.parmIsQuoteMarker(node))
			return;
		final String groupOn = edit.field("PG" + path);
		final boolean present = ((!node.optional)||(path.length() == 0)||(chosenBranch)
				||((groupOn != null)&&(groupOn.length() > 0))||(this.parmGroupOn(path, values)));
		final boolean boxed = (node.optional)&&(path.length() > 0)&&(!chosenBranch);
		if(boxed)
		{
			final String label = this.parmPartPrompt(node);
			if(label.length() > 0)
				str.append("<FONT COLOR=#C0C0C0>").append(escapeHtml(label)).append("&nbsp;</FONT>");
			str.append("<INPUT TYPE=CHECKBOX NAME=PG").append(escapeHtml(path)).append(" VALUE=1");
			if(present)
				str.append(" CHECKED");
			str.append(" ONCLICK=\"ParmsEditorRebuild();\">");
			if(!present)
				return;
		}
		if(node.children == null)
		{
			if(!this.isParmPlaceholder(node))
			{
				final String text = node.literal;
				if((text.length() == 1)&&("\"".indexOf(text.charAt(0)) >= 0))
					return;
				str.append(escapeHtml(text));
				return;
			}
			// the box next to this field already carries its name
			if(!boxed)
				this.parmPrompt(node, str);
			str.append(this.parmEditorField(node, path, edit, httpReq, values));
			return;
		}
		final MTToken[] children = node.children;
		if(node.kind == MTKind.ALTERNATION)
		{
			final int reps = node.repeats ? this.parmRepCount(edit, path, values) : 1;
			for(int c = 0; c < reps; c++)
			{
				final String repPath = (c > 0) ? (path + "~" + c) : path;
				str.append("<INPUT TYPE=HIDDEN NAME=PX").append(escapeHtml(repPath)).append(" VALUE=1>");
				str.append("<BR>");
				for(int i = 0; i < depth; i++)
					str.append("&nbsp;&nbsp;");
				if(children.length == 1)
					this.parmsEditors(children[0], repPath + "_0", depth, edit, httpReq, values, str, false);
				else
				{
					final int chosen = this.parmChoiceAt(edit, repPath, values, children.length);
					str.append("<SELECT NAME=PA").append(escapeHtml(repPath)).append(" ONCHANGE=\"ParmsEditorRebuild();\">");
					for(int b = 0; b < children.length; b++)
					{
						str.append("<OPTION VALUE=").append(b);
						if(b == chosen)
							str.append(" SELECTED");
						final String shown = this.parmBranchText(children[b]);
						str.append(">").append(escapeHtml((shown == null)||(shown.length() <= 60) ? shown : shown.substring(0, 60).trim())).append("</OPTION>");
					}
					str.append("</SELECT>");
					if(this.parmHasField(children[chosen]))
					{
						str.append("&nbsp;");
						this.parmsEditors(children[chosen], repPath + "_" + chosen, depth, edit, httpReq, values, str, true);
					}
				}
				if((node.repeats)&&(reps > 1))
				{
					str.append("&nbsp;<A HREF=\"javascript:ParmsEditorRemoveRep('").append(escapeHtml(path + "~" + c))
						.append("')\" TITLE=\"remove this rep\">-</A>");
				}
			}
			if(node.repeats)
			{
				str.append("<BR><A HREF=\"javascript:ParmsEditorAddOne('").append(escapeHtml(path))
					.append("')\" TITLE=\"add another\">+</A><BR>");
			}
			return;
		}
		final int count = node.repeats ? this.parmRepCount(edit, path, values) : 1;
		for(int c = 0; c < count; c++)
		{
			final String repPath = (c > 0) ? (path + "~" + c) : path;
			if(c > 0)
			{
				str.append("<INPUT TYPE=HIDDEN NAME=PX").append(escapeHtml(repPath)).append(" VALUE=1>");
				str.append("<BR>");
				for(int i = 0; i < depth; i++)
					str.append("&nbsp;&nbsp;");
			}
			for(int i = 0; i < children.length; i++)
				this.parmsEditors(children[i], path + "_" + i + ((c > 0) ? "~" + c : ""), node.repeats ? (depth + 1) : depth, edit, httpReq, values, str, false);
			if((node.repeats)&&(count > 1))
			{
				str.append("&nbsp;<A HREF=\"javascript:ParmsEditorRemoveRep('").append(escapeHtml(path + "~" + c))
					.append("')\" TITLE=\"remove this rep\">-</A>");
			}
		}
		if(node.repeats)
		{
			str.append("<BR><A HREF=\"javascript:ParmsEditorAddOne('").append(escapeHtml(path))
				.append("')\" TITLE=\"add another\">+</A><BR>");
		}
	}

	private boolean parmHoldsCheck(final MTToken node)
	{
		if(node == null)
			return false;
		if(this.parmCheckValue(node) != null)
			return true;
		if(node.children != null)
		{
			for(final MTToken child : node.children)
			{
				if(this.parmHoldsCheck(child))
					return true;
			}
		}
		return false;
	}

	private boolean parmBranchFilled(final String branchPath, final Map<String, String> values)
	{
		for(final String key : values.keySet())
		{
			if(key.startsWith(branchPath))
			{
				final String held = values.get(key);
				if((held != null)&&(held.length() > 0))
					return true;
			}
		}
		return false;
	}

	private static boolean parmNeedsGap(final StringBuilder parms, final String piece)
	{
		if((parms.length() == 0)||(piece.length() == 0))
			return false;
		final char before = parms.charAt(parms.length() - 1);
		final char first = piece.charAt(0);
		if(!Character.isLetterOrDigit(before))
			return false;
		if(Character.isLetterOrDigit(first))
			return true;
		if((first == '+')||(first == '-')||(first == '"'))
			return true;
		return false;
	}

	private void parmsText(final MTToken node, final String path, final ParmEdit edit, final Map<String, String> values, final StringBuilder parms)
	{
		if(this.parmIsQuoteMarker(node))
			return;
		if(node.children == null)
		{
			final String piece;
			if(this.isParmPlaceholder(node))
			{
				final String checkValue = this.parmCheckValue(node);
				final String held;
				if(checkValue != null)
					held = checkValue;
				else
				if((edit != null)
				&&(edit.field("PC" + path) != null)
				&&(this.parmRemovingPath(edit) == null))
					held = (edit.field("PV" + path) != null) ? "true" : "false";
				else
					held = (values.get(path) == null) ? "" : values.get(path);
				if((this.parmQuoted(node))&&(this.parmNeedsQuotes(held)))
					piece = "\"" + held + "\"";
				else
					piece = held;
			}
			else
			{
				final String held = values.get(path);
				piece = (held == null) ? node.literal : held;
			}
			final String gap = values.get(path + "~GAP");
			if((gap != null)&&(gap.length() > 0)&&(parms.length() > 0)&&(piece.length() > 0))
				parms.append(gap);
			else
			if(parmNeedsGap(parms, piece))
				parms.append(' ');
			parms.append(piece);
			final String trailing = values.get(path + "~WS");
			if(trailing != null)
				parms.append(trailing);
			return;
		}
		final MTToken[] children = node.children;
		if(node.kind == MTKind.ALTERNATION)
		{
			final int reps = node.repeats ? this.parmRepCount(edit, path, values) : 1;
			for(int c = 0; c < reps; c++)
			{
				final String repPath = (c > 0) ? (path + "~" + c) : path;
				final int chosen = this.parmChoiceAt(edit, repPath, values, children.length);
				final String branchPath = repPath + "_" + chosen;
				// a rep the user added but has not written into yet is not written
				if((!this.parmBranchFilled(branchPath, values))&&(!this.parmHoldsCheck(children[chosen])))
					continue;
				this.parmsText(children[chosen], branchPath, edit, values, parms);
			}
			return;
		}
		if((node.optional)&&(path.length() > 0)&&(!this.parmPartWritten(path, values)))
			return;
		final int count = node.repeats ? this.parmRepCount(edit, path, values) : 1;
		for(int c = 0; c < count; c++)
		{
			for(int i = 0; i < children.length; i++)
				this.parmsText(children[i], path + "_" + i + ((c > 0) ? "~" + c : ""), edit, values, parms);
		}
	}

	/**
	 * Remembers the furthest point at which a proposition failed to match its
	 * format, and what was expected there.
	 */
	class MTFail
	{
		private static final int	MAX_EXPECTATIONS	= 4;

		private int					bestIndex			= -1;
		private final List<String>	whats				= new ArrayList<String>();

		public void record(final int index, final String what)
		{
			if(index < bestIndex)
				return;
			if(index > bestIndex)
			{
				bestIndex = index;
				whats.clear();
			}
			if((whats.size() < MAX_EXPECTATIONS)&&(!whats.contains(what)))
				whats.add(what);
		}

		public int getBestIndex()
		{
			return bestIndex;
		}

		public String describe(final String proposition)
		{
			if(bestIndex < 0)
				return L("invalid parameters at position 1");
			if(bestIndex >= proposition.length())
			{
				if(whats.size() == 0)
					return L("invalid parameters at end of input");
				return L("expected @x1 at end of input",describeExpectations());
			}
			if(whats.size() == 0)
				return L("invalid parameters at position @x1",""+(bestIndex + 1));
			return L("expected @x1 at position @x2",describeExpectations(),""+(bestIndex + 1));
		}

		private String describeExpectations()
		{
			if(whats.size() == 1)
				return whats.get(0);
			final StringBuilder str = new StringBuilder();
			for(int i = 0; i < whats.size(); i++)
			{
				if(i > 0)
					str.append(((i == whats.size()-1) ? L(" or ") : ", "));
				str.append(whats.get(i));
			}
			return L("one of @x1",str.toString());
		}
	}

	private static String snippetValue(final String value)
	{
		final String trimmed = (value == null) ? "" : value.trim();
		final StringBuilder str = new StringBuilder();
		int i = 0;
		while((i < trimmed.length()) && (str.length() < 20))
		{
			final char c = trimmed.charAt(i);
			str.append(Character.isWhitespace(c) ? ' ' : c);
			i++;
		}
		if(i < trimmed.length())
			str.append("...");
		return str.toString();
	}

	/**
	 * One pass of the parms matcher. Everything that stays the same for a whole
	 * match lives here, so a call only carries the token, the position, the
	 * terminators and the field name it is matching.
	 */
	class MTParse
	{
		final String proposition;
		final MTFail fail;
		final Map<String,String> sink;
		final int[] outIndex = new int[1];
		final boolean[] quoteMode = new boolean[1];

		MTParse(final String proposition, final MTFail fail, final Map<String,String> sink)
		{
			this.proposition = proposition;
			this.fail = fail;
			this.sink = sink;
		}

		/**
		 * Matches a token, recording what it found into this pass's sink.
		 *
		 * @param token the format token being matched
		 * @param index where in the parms to start
		 * @param terminators the characters that end a value here, or null for none
		 * @param path the name of this token's fields in the sink
		 * @return true if the token matched
		 */
		boolean match(final MTToken token, final int index, final Set<Character> terminators, final String path)
		{
			return this.match(token, index, terminators, path, this.sink);
		}

		/**
		 * Matches a token, recording what it found into the given map, which is often
		 * a local one so an attempt that fails leaves no trace.
		 *
		 * @param token the format token being matched
		 * @param index where in the parms to start
		 * @param terminators the characters that end a value here, or null for none
		 * @param path the name of this token's fields in the sink
		 * @param into where the matched values are recorded, or null to only match
		 * @return true if the token matched
		 */
		boolean match(final MTToken token, final int index, final Set<Character> terminators, final String path, final Map<String,String> into)
		{
			final int saveIndex = index;
			final boolean saveQuoteMode = this.quoteMode[0];
			if(this.matchInternal(token, index, terminators, path, into))
				return true;
			if(token.optional)
			{
				this.outIndex[0] = saveIndex;
				this.quoteMode[0] = saveQuoteMode;
				return true;
			}
			return false;
		}

		/**
		 * The part of a match that {@link #match} can still undo when the token is
		 * optional.
		 *
		 * @param token the format token being matched
		 * @param index where in the parms to start
		 * @param terminators the characters that end a value here, or null for none
		 * @param path the name of this token's fields in the sink
		 * @param into where the matched values are recorded, or null to only match
		 * @return true if the token matched
		 */
		private boolean matchInternal(final MTToken token, final int index, final Set<Character> terminators, final String path, final Map<String,String> into)
		{
			final Map<String,String> mine = (into == null) ? null : new HashMap<String,String>();
			this.outIndex[0] = index;

			if(token.children == null)
			{
				if((!this.matchLiteral(token, index, terminators))&&(!token.optional))
					return false;
				if((mine != null)&&(this.outIndex[0] > index))
					this.record(token, mine, path, index, this.outIndex[0]);
				if(token.repeats)
				{
					int rep = 0;
					while(true)
					{
						final int saveIndex = this.outIndex[0];
						if(!this.matchLiteral(token, this.outIndex[0], terminators))
						{
							this.outIndex[0] = saveIndex;
							break;
						}
						if((mine != null)&&(this.outIndex[0] > saveIndex))
							this.record(token, mine, path + "~" + (++rep), saveIndex, this.outIndex[0]);
					}
					if((mine != null)&&(rep > 0))
						mine.put(path + "~REPS", Integer.toString(rep + 1));
				}
				if((into != null)&&(mine != null))
					into.putAll(mine);
				return true;
			}
			else
			if(token.children != null)
			{
				final MTToken[] children = token.children;
				if(token.kind == MTKind.ALTERNATION)
				{
					boolean emptyMatch = false;
					int altIndex = 0;
					int rep = 0;
					for(final MTToken alt : children)
					{
						final String repPath = path + ((rep > 0) ? "~" + rep : "");
						final int thisAlt = altIndex++;
						final int saveIndex = index;
						final boolean saveQuoteMode = this.quoteMode[0];
						if(this.match(alt, saveIndex, terminators, repPath + "_" + thisAlt, mine))
						{
							if(this.outIndex[0] > index)
							{
								if(mine != null)
									mine.put(repPath, Integer.toString(thisAlt));
								if(token.repeats)
								{
									while(true)
									{
										final int saveRepeatIndex = this.outIndex[0];
										final boolean saveRepeatQuoteMode = this.quoteMode[0];
										boolean matched = false;
										int innerIndex = 0;
										for(final MTToken alt2 : children)
										{
											final int thisAlt2 = innerIndex++;
											if(this.match(alt2, saveRepeatIndex, terminators, path + "~" + (rep + 1) + "_" + thisAlt2, mine))
											{
												if(this.outIndex[0] > saveRepeatIndex)
												{
													matched = true;
													if(mine != null)
														mine.put(path + "~" + (rep + 1), Integer.toString(thisAlt2));
													break;
												}
											}
											this.outIndex[0] = saveRepeatIndex;
											this.quoteMode[0] = saveRepeatQuoteMode;
										}
										if(!matched)
										{
											this.quoteMode[0] = saveRepeatQuoteMode;
											this.outIndex[0] = saveRepeatIndex;
											break;
										}
										rep++;
									}
									if(mine != null)
										mine.put(path + "~REPS", Integer.toString(rep + 1));
								}
								if((into != null)&&(mine != null))
									into.putAll(mine);
								return true;
							}
							if(!token.mustConsume)
								emptyMatch = true;
						}
						this.quoteMode[0] = saveQuoteMode;
						this.outIndex[0] = saveIndex;
					}
					return emptyMatch;
				}
				else
				{
					int currentIndex = index;
					final boolean saveQuoteMode = this.quoteMode[0];
					for(int c = 0; c < children.length; c++)
					{
						final MTToken child = children[c];
						final Set<Character> childTerminators = terminatorsAfter(children, c, token.repeats, terminators);
						if(!this.match(child, currentIndex, childTerminators, path + "_" + c, mine))
						{
							if(token.optional)
							{
								this.outIndex[0] = index;
								this.quoteMode[0] = saveQuoteMode;
								return true;
							}
							return false;
						}
						currentIndex = this.outIndex[0];
					}

					if(token.repeats)
					{
						int rep = 0;
						while(true)
						{
							final int saveIndex = this.outIndex[0];
							final boolean saveRepeatQuoteMode = this.quoteMode[0];
							boolean matched = true;
							int tempIndex = saveIndex;
							for(int c = 0; c < children.length; c++)
							{
								final Set<Character> repTers = terminatorsAfter(children, c, token.repeats, terminators);
								if(!this.match(children[c], tempIndex, repTers, path + "_" + c + "~" + (rep + 1), mine))
								{
									matched = false;
									break;
								}
								tempIndex = this.outIndex[0];
							}
							if((!matched)||(this.outIndex[0] == saveIndex))
							{
								this.quoteMode[0] = saveRepeatQuoteMode;
								this.outIndex[0] = saveIndex;
								break;
							}
							rep++;
						}
						if((mine != null)&&(rep > 0))
							mine.put(path + "~REPS", Integer.toString(rep + 1));
					}
					if((into != null)&&(mine != null))
						into.putAll(mine);
					return true;
				}
			}
			return false;
		}

		/**
		 * Matches one literal, placeholder or quote marker at the given position.
		 *
		 * @param token the format token being matched
		 * @param index where in the parms to start
		 * @param terminators the characters that end a value here, or null for none
		 * @return true if the token matched
		 */
		boolean matchLiteral(final MTToken token, int index, final Set<Character> terminators)
		{
			while(index < this.proposition.length() && Character.isWhitespace(this.proposition.charAt(index)))
				index++;
			if(index >= this.proposition.length())
			{
				this.outIndex[0] = index;
				if(token.stop == MTStop.REST)
					return true;
				this.fail.record(index, token.describeExpectation());
				return false;
			}
			final String literal = token.text();
			final String lowerProposition = this.proposition.toLowerCase();
			if(token.kind == MTKind.QUOTE_MARKER)
			{
				if(this.proposition.charAt(index) == '\"')
				{
					this.quoteMode[0] = !this.quoteMode[0];
					this.outIndex[0] = index + 1;
				}
				return true;
			}
			else
			if((token.stop == MTStop.QUOTE) && this.quoteMode[0])
			{
				final int end = this.proposition.length();
				int i = index;
				while(i < end)
				{
					final char c = this.proposition.charAt(i);
					if(c == '\\')
					{
						if(i + 1 >= end)
						{
							this.fail.record(i, "'\"'");
							return false;
						}
						i += 2;
					}
					else
					if(c == '\"')
						break;
					else
						i++;
				}
				if(i >= end)
				{
					this.fail.record(i, "'\"'");
					return false;
				}
				if(!confirmParmValue(literal, CMStrings.deEscape(this.proposition.substring(index, i)), token.parameterArgs))
				{
					this.fail.record(index, token.describeRejected(CMStrings.deEscape(this.proposition.substring(index, i))));
					return false;
				}
				this.quoteMode[0] = false;
				this.outIndex[0] = i + 1;
				return true;
			}
			if((token.stop == MTStop.CHAR) || ((token.stop == MTStop.QUOTE) && (token.stopAt != null)))
			{
				// Greedy up to the delimiter character, without consuming it.
				final int end = this.proposition.length();
				int i = index;
				int depth = 0;
				while(i < end)
				{
					final char c = this.proposition.charAt(i);
					if((token.stopChar() == ')') && (c == '('))
						depth++;
					else
					if((token.stopChar() == ')') && (c == ')') && (depth > 0))
						depth--;
					else
					if(c == token.stopChar())
						break;
					i++;
				}
				if(!confirmParmValue(literal, this.proposition.substring(index, i).trim(), token.parameterArgs))
				{
					this.fail.record(index, token.describeRejected(this.proposition.substring(index, i)));
					return false;
				}
				this.outIndex[0] = i;
				return true;
			}
			if(token.stop == MTStop.TOKENS)
			{
				// Greedy up to any of the delimiter strings, without consuming them.
				final int end = this.proposition.length();
				int i = index;
				while(i < end)
				{
					boolean delimiterFound = false;
					for(final String delimiter : token.stopTokens())
					{
						if((delimiter.length() > 0)
						&&(this.proposition.regionMatches(true, i, delimiter, 0, delimiter.length())))
						{
							delimiterFound = true;
							break;
						}
					}
					if(delimiterFound)
						break;
					i++;
				}
				if(!confirmParmValue(literal, this.proposition.substring(index, i).trim(), token.parameterArgs))
				{
					this.fail.record(index, token.describeRejected(this.proposition.substring(index, i)));
					return false;
				}
				this.outIndex[0] = i;
				return true;
			}
			if(token.stop == MTStop.LITERAL)
			{
				final int end = this.proposition.length();
				int i = index + 1;
				while(i < end)
				{
					if(lowerProposition.startsWith(token.stopLiteral().toLowerCase(), i))
					{
						if(!confirmParmValue(literal, this.proposition.substring(index, i).trim(), token.parameterArgs))
						{
							this.fail.record(index, token.describeRejected(this.proposition.substring(index, i)));
							return false;
						}
						this.outIndex[0] = i;
						return true;
					}
					i++;
				}
				this.fail.record(index, "'" + token.stopLiteral() + "'");
				return false;
			}
			else
			if(token.stop == MTStop.END) // greedy placeholder at end of format, matches to end
			{
				if(!confirmParmValue(literal, this.proposition.substring(index).trim(), token.parameterArgs))
				{
					this.fail.record(index, token.describeRejected(this.proposition.substring(index)));
					return false;
				}
				this.outIndex[0] = this.proposition.length();
				return true;
			}
			if(token.stop == MTStop.REST) // [X].. rest-or-empty placeholder
			{
				final String val = this.proposition.substring(index).trim();
				if(val.length()==0)
				{
					this.outIndex[0] = index;
					return true;
				}
				if(!confirmParmValue(literal, val, token.parameterArgs))
				{
					this.fail.record(index, token.describeRejected(val));
					return false;
				}
				this.outIndex[0] = this.proposition.length();
				return true;
			}
			if(token.parameterArgs != null) // is a placeholder
			{
				final int start = index;
				if(this.proposition.charAt(index) == '\"')
				{
					// a quoted value counts as one value, the way CMParms.parse sees it
					int i = index + 1;
					while((i < this.proposition.length())
					&&(this.proposition.charAt(i) != '\"')
					&&(this.proposition.charAt(i) != '\\'))
						i++;
					if(i < this.proposition.length())
						i++;
					index = i;
				}
				else
				{
					while((index < this.proposition.length())
					&&(!Character.isWhitespace(this.proposition.charAt(index)))
					&&((terminators == null)
						|| (!terminators.contains(Character.valueOf(Character.toLowerCase(this.proposition.charAt(index)))))))
							index++;
				}
				if(index == start)
				{
					this.fail.record(index, token.describeExpectation());
					return false; // a placeholder must capture at least one character
				}
				if(!confirmParmValue(literal, this.proposition.substring(start,index), token.parameterArgs))
				{
					this.fail.record(start, token.describeRejected(this.proposition.substring(start,index)));
					return false;
				}
				this.outIndex[0] = index;
				return true;
			}
			else
			{
				// Match literal string, ignoring case
				if(lowerProposition.startsWith(literal.toLowerCase(), index))
				{
					this.outIndex[0] = index + literal.length();
					return true;
				}
				this.fail.record(index, "'" + literal + "'");
				return false;
			}
		}

		/**
		 * Records what a token consumed, including the whitespace around it, so the
		 * value and its gaps can be put back exactly as they were.
		 *
		 * @param token the format token that was matched
		 * @param into where the values are recorded
		 * @param path the name of this token's fields in the sink
		 * @param start where the token began in the parms
		 * @param end where the token ended in the parms
		 */
		private void record(final MTToken token, final Map<String,String> into, final String path, final int start, final int end)
		{
			int rawEnd = end;
			// a quote-terminated value has its closing quote counted as part of the token
			if((token.stop == MTStop.QUOTE)
			&&(rawEnd > start)
			&&(this.proposition.charAt(rawEnd - 1) == '"'))
				rawEnd--;
			final String raw = this.proposition.substring(start, rawEnd);
			final String held = raw.trim();
			into.put(path, held);
			final int lead = (held.length() == 0) ? 0 : raw.indexOf(held);
			if(lead > 0)
				into.put(path + "~GAP", raw.substring(0, lead));
			if(held.length() > 0)
			{
				final String trail = raw.substring(lead + held.length());
				if(trail.length() > 0)
					into.put(path + "~WS", trail);
			}
		}
	}
	private boolean confirmParmValue(final String parmName, final String value, final Map<String,String> parameterArgs)
	{
		final String confirmVal;
		if((value.length() > 1)
		&& (value.charAt(0) == '"')
		&& (value.charAt(value.length() - 1) == '"'))
			confirmVal = value.substring(1, value.length() - 1);
		else
			confirmVal = value;
		final String key = parmName.toUpperCase().trim();
		final Map<String, AbilityParmEditor> editors = getEditors();
		if(editors.containsKey(key))
			return editors.get(key).confirmValue(confirmVal, parameterArgs);
		else
		{
			final ParmType parmType = (ParmType)CMath.s_valueOf(ParmType.class,key);
			if(parmType != null)
			{
				switch(parmType)
				{
				case STRINGORNULL:
					return true;
				case ONEWORD:
					return (confirmVal.trim().length() > 0) && (confirmVal.indexOf(' ') < 0);
				case STRING:
					return confirmVal.trim().length() > 0;
				case NUMBERORNULL:
					if(confirmVal.length() == 0)
						return true;
					//$FALL-THROUGH$
				case NUMBER:
					return CMath.isInteger(confirmVal);
				case DECIMAL:
					return CMath.isInteger(confirmVal)||CMath.isDouble(confirmVal);
				case NUMBER_PAIR:
				{
					final int x = confirmVal.indexOf(',');
					return CMath.isInteger(confirmVal)
						||((x > 0)
							&&(CMath.isInteger(confirmVal.substring(0,x).trim()))
							&&(CMath.isInteger(confirmVal.substring(x+1).trim())));
				}
				case CHOICES:
				case MULTICHOICES:
				{
					if((parmType == ParmType.MULTICHOICES)&&(CMath.isInteger(confirmVal)))
						return true;
					final String choicesValue = (parameterArgs == null)?null:parameterArgs.get("CHOICES");
					if(choicesValue == null)
						return false;
					for(final String choice : CMParms.parseCommas(choicesValue,true))
					{
						if(choice.equalsIgnoreCase(confirmVal.trim()))
							return true;
					}
					return false;
				}
				case SPECIAL:
					return false;
				case CONFIRM:
					return CMath.isBool(confirmVal);
				case CHECK:
				{
					if(confirmVal.trim().length()==0)
						return true;
					final String checkName = (parameterArgs == null)?null:parameterArgs.get("NAME");
					return (checkName != null) && checkName.equalsIgnoreCase(confirmVal.trim());
				}
				}
			}
			else
			{
				Log.debugOut("CMAbleParms", "Invalid/unknown miscText parm editor or type '"+parmName+"' for value '"+confirmVal+"'");
			}
		}
		return true;
	}

	private static boolean isNullable(final MTToken token)
	{
		if(token == null)
			return true;
		if(token.children == null)
		{
			if(token.kind == MTKind.QUOTE_MARKER || token.optional)
				return true; // (") matches empty with no quote; an optional literal may match empty
			return false; // literals and placeholders otherwise require >=1 char
		}
		final MTToken[] children = token.children;
		if(token.optional)
			return true; // groups are optional
		if(token.kind == MTKind.ALTERNATION)
		{
			if(token.mustConsume)
				return false;
			for(final MTToken child : children)
			{
				if(isNullable(child))
					return true;
			}
			return false;
		}
		for(final MTToken child : children)
			if(!isNullable(child))
				return false;
		return true;
	}

	private static void collectFirstChars(final MTToken token, final Set<Character> out)
	{
		if(token == null)
			return;
		if(token.children == null)
		{
			if(token.parameterArgs == null)
			{
				final String lit = token.text();
				if((lit != null) && (lit.length() > 0))
				{
					final char ch = Character.toLowerCase(lit.charAt(0));
					if(!Character.isLetterOrDigit(ch) && (ch != '_'))
						out.add(Character.valueOf(ch));
				}
			}
		}
		else
		if(token.children != null)
		{
			final MTToken[] children = token.children;
			if(token.kind == MTKind.ALTERNATION)
			{
				for(final MTToken child : children)
					collectFirstChars(child, out);
			}
			else
			if(children.length > 0)
				collectFirstChars(children[0], out);
		}
	}

	private static Set<Character> terminatorsAfter(final MTToken[] children, final int c, final boolean repeats, final Set<Character> inherited)
	{
		final Set<Character> first = new HashSet<Character>();
		for(int i = c + 1; i < children.length; i++)
		{
			collectFirstChars(children[i], first);
			if(!isNullable(children[i]))
				return first; // a required sibling is a hard stop
		}
		if(repeats)
		{
			for(int i = 0; i < children.length; i++)
			{
				collectFirstChars(children[i], first);
				if(!isNullable(children[i]))
				{
					if(inherited != null)
						first.addAll(inherited); // values inside a repeated group can also end at terminators in the enclosing context
					return first;
				}
			}
		}
		if(inherited != null)
			first.addAll(inherited); // every following sibling is nullable
		return first;
	}


	private boolean endsWithOpenGreedy(final MTToken token)
	{
		if(token == null)
			return false;
		if(token.children == null)
			return token.stop == MTStop.END;
		if(token.children != null)
		{
			final MTToken[] children = token.children;
			if(children.length == 0)
				return false;
			if(token.kind == MTKind.ALTERNATION)
			{
				for(final MTToken child : children)
				{
					if(endsWithOpenGreedy(child))
						return true;
				}
				return false;
			}
			return endsWithOpenGreedy(children[children.length - 1]);
		}
		return false;
	}

	private boolean hasTopLevelSlash(final String s)
	{
		int depth = 0;
		for(int i = 0; i < s.length(); i++)
		{
			final char c = s.charAt(i);
			if(c == '\\')
			{
				i++;
				continue;
			}
			if(c == '(')
				depth++;
			else
			if(c == ')')
				depth--;
			else
			if((c == '/') && (depth == 0))
				return true;
		}
		return false;
	}

	private static final String ESCAPED_SLASH = "\\/";

	enum MTKind
	{
		LITERAL,		// text the parms must contain
		PLACEHOLDER,	// a declared parameter the parms must supply
		QUOTE_MARKER,	// the optional ("), which surrounds a quoted value
		SEQUENCE,		// children that must all appear, in order
		ALTERNATION		// children of which one must appear
	}

	enum MTStop
	{
		NONE,			// not greedy: the value ends where its own type says
		END,			// takes the rest of the parms
		CHAR,			// up to a delimiter character, which it does not take
		TOKENS,			// up to any of several delimiter strings, which it does not take
		LITERAL,		// up to a following literal, which it does not take
		QUOTE,			// up to the quote that ends the quoted value
		REST			// an [X].. placeholder: what is left, or nothing
	}

	class MTToken
	{
		public MTKind				kind;
		public String				literal			= null;		// LITERAL, PLACEHOLDER and QUOTE_MARKER text
		public MTToken[]			children		= null;		// SEQUENCE and ALTERNATION contents
		public boolean				optional		= false;
		public boolean				repeats			= false;
		public boolean				mustConsume		= false;	// ALTERNATION: some branch has to take at least one character
		public MTStop				stop			= MTStop.NONE;
		public Object				stopAt			= null;		// Character, List<String> or String, per stop
		public Map<String, String>	parameterArgs	= null;		// declared prompt=/choices= arguments; null when this is not a placeholder

		public MTToken(final String text, final boolean optional)
		{
			this.kind = MTKind.LITERAL;
			this.literal = text;
			this.optional = optional;
		}

		public MTToken(final MTKind kind, final String text, final boolean optional)
		{
			this.kind = kind;
			this.literal = text;
			this.optional = optional;
		}

		public MTToken(final MTToken[] children, final boolean optional)
		{
			this.kind = MTKind.SEQUENCE;
			this.children = children;
			this.optional = optional;
		}

		public MTToken(final MTToken other)
		{
			this.kind = other.kind;
			this.literal = other.literal;
			this.children = other.children;
			this.optional = other.optional;
			this.repeats = other.repeats;
			this.mustConsume = other.mustConsume;
			this.stop = other.stop;
			this.stopAt = other.stopAt;
			this.parameterArgs = other.parameterArgs;
		}

		public boolean isPlaceholder()
		{
			return this.kind == MTKind.PLACEHOLDER;
		}

		/**
		 * The text of a literal, giving an escaped slash back as a slash.
		 *
		 * @return the text this token stands for
		 */
		public String text()
		{
			return ESCAPED_SLASH.equals(this.literal) ? "/" : this.literal;
		}

		public char stopChar()
		{
			if(this.stop == MTStop.QUOTE)
				return '"';
			return (this.stopAt instanceof Character) ? ((Character)this.stopAt).charValue() : '\0';
		}

		public String stopLiteral()
		{
			return (this.stopAt instanceof String) ? (String)this.stopAt : null;
		}

		@SuppressWarnings("unchecked")
		public List<String> stopTokens()
		{
			return (this.stopAt instanceof List) ? (List<String>)this.stopAt : null;
		}

		public String describePlaceholder()
		{
			final String name = this.literal.toUpperCase(Locale.ROOT).trim();
			final ParmType parmType = (ParmType)CMath.s_valueOf(ParmType.class, name);
			if(parmType != null)
			{
				switch(parmType)
				{
				case NUMBER:
				case DECIMAL:
					return L("a number");
				case NUMBER_PAIR:
					return L("two comma-separated numbers");
				case ONEWORD:
					return L("a single word");
				case CHOICES:
					return L("a valid choice");
				case MULTICHOICES:
					return L("a choice or a count");
				case CONFIRM:
				case CHECK:
					return L("a true/false value");
				default:
					return L("a value");
				}
			}
			if(getEditors().containsKey(name))
				return L("a valid @x1 value",name);
			return L("a value for @x1",name);
		}

		public String describeExpectation()
		{
			if(this.kind == MTKind.PLACEHOLDER)
				return this.describePlaceholder();
			return L("'@x1'",this.text());
		}

		public String describeRejected(final String value)
		{
			final String noun = this.describePlaceholder();
			final String snippet = snippetValue(value);
			if(snippet.length() == 0)
				return noun;
			return L("@x1, got '@x2'",noun,snippet);
		}
	}

	private MTToken parseMTAlternationBlock(final String format, final int start, final int end) throws CMException
	{
		final List<MTToken> alternatives = new ArrayList<MTToken>();
		int depth = 0;
		int branchStart = start;
		for(int i = start; i < end; i++)
		{
			final char c = format.charAt(i);
			if(c == '\\')
			{
				i++;
				continue;
			}
			if(c == '(')
				depth++;
			else
			if(c == ')')
				depth--;
			else
			if((c == '/') && (depth == 0))
			{
				if(i == branchStart)
					throw new CMException(L("Empty alternative in [...] at @x1",""+i));
				alternatives.add(parseMTSequence(format, branchStart, i));
				branchStart = i + 1;
			}
		}
		if(branchStart >= end)
			throw new CMException(L("Empty alternative in [...] at @x1",""+end));
		alternatives.add(parseMTSequence(format, branchStart, end));
		final MTToken altToken = new MTToken(alternatives.toArray(new MTToken[0]), false);
		altToken.kind = MTKind.ALTERNATION;
		altToken.mustConsume = true;
		return altToken;
	}

	private static boolean isDelimiterChar(final char c)
	{
		switch(c)
		{
		case '/':
		case '(':
		case '[':
		case ')':
		case '\\':
		case '.':
			return false;
		default:
			return !Character.isWhitespace(c);
		}
	}

	/**
	 * Parses a misc text format.  The format supports literals, [PLACEHOLDER]
	 * (optionally with (args)), (optional) groups, '/' alternatives, '...'
	 * repetition, and '\' escapes.  An escaped syntax character never acts as
	 * syntax, even when it forms a token alone: a \/ between placeholders is
	 * a literal slash, not an alternative separator.  A placeholder whose name ends with "..."
	 * (for example [NAME...]) is greedy.  A placeholder whose name ends with
	 * '..' plus a delimiter character (for example [NAME..;]), or '..' plus an
	 * escaped character (for example [NAME..\/]), is greedy up to
	 * that delimiter without consuming it.  A placeholder whose name ends with
	 * '..' plus a parenthesized, '/'-separated set of delimiters (for example
	 * [NAME..(</=/>/!)], or [NAME..(;|MASK=)] for multi-character delimiters)
	 * is greedy up to any of those delimiters, without consuming them.  A placeholder followed by '.. '
	 * (dot-dot-space, for example ([NAME].. )) is greedy to end-of-string,
	 * or empty when nothing remains, and may appear inside groups.  The special (") marker denotes an
	 * optional quotation mark.  (")[NAME...](") accepts a quoted-or-bare
	 * value.  A '..' followed by a single delimiter character
	 * (for example '..,' or '..;') repeats the preceding token with that literal
	 * delimiter between repetitions, equivalent to writing [X](,[X])... by hand.
	 * A bracketed block containing an unescaped '/' outside of any
	 * parentheses, for example [A/B] or [mana+10/hits+10], is a required
	 * alternation: exactly one of its '/'-separated branches must match, and
	 * the block may not match empty.
	 *
	 * A bracketed block that names neither a registered placeholder nor a
	 * ParmType (for example [;] or [;/-]) is a literal: [;] matches a
	 * semicolon and nothing else, and [;/-] matches a semicolon or a dash.
	 * Such a block may not carry arguments or a greedy marker, and an
	 * unknown placeholder name with arguments is an error, not a catch-all.
	 */
	private MTToken parseMTSequence(final String format, final int start, final int end) throws CMException
	{
		final List<MTToken> tokens = new ArrayList<>();
		int index = start;

		while(index < end)
		{
			while((index < end)
			&& Character.isWhitespace(format.charAt(index)))
				index++;
			if(index >= end)
				break;

			char c = format.charAt(index);
			if(c == ')')
				break;
			if(c == '/')
			{
				tokens.add(new MTToken("/", false));
				index++;
			}
			else
			if(c == '(')
			{
				if((index + 2 < end)
				&&(format.charAt(index + 1) == '\"')
				&&(format.charAt(index + 2) == ')'))
				{
					final MTToken quoteToken = new MTToken("\"", false);
					quoteToken.kind = MTKind.QUOTE_MARKER;
					quoteToken.optional = true;
					tokens.add(quoteToken);
					index += 3;
					continue;
				}
				int depth = 1;
				int closeIndex = index + 1;
				while(closeIndex < end && depth > 0)
				{
					if(format.charAt(closeIndex) == '\\')
					{
						closeIndex += 2;
						continue;
					}
					if(format.charAt(closeIndex) == '(')
						depth++;
					else
					if(format.charAt(closeIndex) == ')')
						depth--;
					closeIndex++;
				}
				if(depth != 0)
					throw new CMException(L("Unclosed ( at @x1",""+index));

				final MTToken inner = parseMTSequence(format, index + 1, closeIndex - 1);
				inner.optional = true;
				tokens.add(inner);
				index = closeIndex;
				if(endsWithOpenGreedy(inner))
				{
					int la = closeIndex;
					while((la < end) && Character.isWhitespace(format.charAt(la)))
						la++;
					if(la < end)
						throw new CMException(L("Greedy placeholder at end of group requires end-of-string at @x1",""+index));
				}
			}
			else
			if(c == '[')
			{
				int closeIndex = index + 1;
				int bracketDepth = 0;
				while(closeIndex < end)
				{
					final char bc = format.charAt(closeIndex);
					if(bc == '\\')
					{
						closeIndex += 2;
						continue;
					}
					if(bc == '[')
						bracketDepth++;
					else
					if(bc == ']')
					{
						if(bracketDepth == 0)
							break;
						bracketDepth--;
					}
					closeIndex++;
				}
				if(closeIndex >= end)
					throw new CMException(L("Unclosed [ at @x1",""+index));
				String placeholder = format.substring(index + 1, closeIndex);
				if(hasTopLevelSlash(placeholder))
				{
					tokens.add(parseMTAlternationBlock(format, index + 1, closeIndex));
					index = closeIndex + 1;
					continue;
				}
				List<String> greedyToTokens = null;
				final int setSuffix = placeholder.lastIndexOf("..(");
				if((setSuffix >= 0) && placeholder.endsWith(")"))
				{
					final String setStr = placeholder.substring(setSuffix + 3, placeholder.length() - 1);
					placeholder = placeholder.substring(0, setSuffix).trim();
					greedyToTokens = new ArrayList<String>();
					for(final String setTok : CMParms.parseAny(setStr, '/', false, false))
					{
						if(setTok.length() > 0)
							greedyToTokens.add(setTok);
					}
				}
				final boolean greedy = (greedyToTokens == null) && placeholder.endsWith("...");
				char greedyTo = '\0';
				boolean greedyRest = false;
				boolean greedyQuote = false;
				String placeholderToken = placeholder;
				if(greedy)
					placeholderToken = placeholderToken.substring(0, placeholderToken.length() - 3).trim();
				else
				if((placeholder.length() > 3)
				&&(placeholder.charAt(placeholder.length() - 2) == '\\')
				&&(placeholder.charAt(placeholder.length() - 3) == '.')
				&&(placeholder.charAt(placeholder.length() - 4) == '.'))
				{
					// an escaped character is always a valid greedy-to delimiter
					greedyTo = placeholder.charAt(placeholder.length() - 1);
					placeholderToken = placeholder.substring(0, placeholder.length() - 4).trim();
				}
				else
				if((placeholder.length() > 2)
				&&(placeholder.charAt(placeholder.length() - 2) == '.')
				&&(placeholder.charAt(placeholder.length() - 3) == '.'))
				{
					if(isDelimiterChar(placeholder.charAt(placeholder.length() - 1)))
					{
						greedyTo = placeholder.charAt(placeholder.length() - 1);
						placeholderToken = placeholder.substring(0, placeholder.length() - 3).trim();
					}
					else
					if(placeholder.charAt(placeholder.length() - 1)==' ')
					{
						if((tokens.size()>0) 
						&&"\"".equals(tokens.get(tokens.size()-1).literal)
						&&tokens.get(tokens.size()-1).optional)
						{
							greedyQuote = true;
							greedyTo='\"';
						}
						else
							greedyRest = true;
						placeholderToken = placeholder.substring(0, placeholder.length() - 3).trim();
					}
				}
				if(placeholderToken.length() == 0)
					throw new CMException(L("Empty placeholder name at @x1",""+index));
				String[] placeholderArgs = new String[0];
				final Map<String,String> parameterArgs = new HashMap<String,String>();
				final int x = placeholderToken.indexOf('(');
				final int firstCloseParen = placeholderToken.indexOf(')');
				if((x >= 0)||(firstCloseParen >= 0))
				{
					if((x <= 0)||(firstCloseParen >= 0 && firstCloseParen < x))
						throw new CMException(L("Malformed placeholder arguments at @x1",""+index));
					int depth = 0;
					int closeParen = -1;
					for(int i=x;i<placeholderToken.length();i++)
					{
						if(placeholderToken.charAt(i) == '\\')
						{
							i++;
							continue;
						}
						if(placeholderToken.charAt(i) == '(')
							depth++;
						else
						if(placeholderToken.charAt(i) == ')')
						{
							depth--;
							if(depth == 0)
							{
								closeParen = i;
								break;
							}
						}
					}
					if((depth != 0)||(closeParen != placeholderToken.length()-1))
						throw new CMException(L("Malformed placeholder arguments at @x1",""+index));
					final String argGroup = placeholderToken.substring(x+1,closeParen);
					placeholderArgs = CMParms.parseAny(argGroup, '/', false, true).toArray(new String[0]);
					for(final String arg : placeholderArgs)
					{
						final int equals = arg.indexOf('=');
						if(equals <= 0)
							throw new CMException(L("Malformed placeholder argument at @x1",""+index));
						final String argName = arg.substring(0,equals).trim();
						final String argValue = arg.substring(equals+1).trim();
						if((argName.length() == 0)||(argValue.length() == 0))
							throw new CMException(L("Malformed placeholder argument at @x1",""+index));
						final String argKey = argName.toUpperCase(Locale.ROOT);
						if(parameterArgs.containsKey(argKey))
							throw new CMException(L("Duplicate placeholder argument @x1 at @x2",argName,""+index));
						parameterArgs.put(argKey,argValue);
					}
					placeholderToken = placeholderToken.substring(0,x).trim();
				}
				final String bareName = placeholderToken.toUpperCase(Locale.ROOT).trim();
				if((!getEditors().containsKey(bareName))
				&&(CMath.s_valueOf(ParmType.class,bareName)==null))
				{
					if((placeholderArgs.length > 0)
					||greedy
					||(greedyTo != '\0')
					||greedyRest
					||(greedyToTokens != null))
						throw new CMException(L("Unknown placeholder '@x1' at @x2",placeholderToken,""+index));
					tokens.add(parseMTAlternationBlock(format, index + 1, closeIndex));
					index = closeIndex + 1;
					continue;
				}
				final MTToken token = new MTToken(placeholderToken, false);
				tokens.add(token);
				token.kind = MTKind.PLACEHOLDER;
				token.parameterArgs=parameterArgs;
				if(greedyTo != '\0')
				{
					token.stop = MTStop.CHAR;
					token.stopAt = Character.valueOf(greedyTo);
				}
				else
				if(greedyToTokens != null)
				{
					token.stop = MTStop.TOKENS;
					token.stopAt = greedyToTokens;
				}
				if(greedyRest)
					token.stop = MTStop.REST;
				if(greedyQuote)
				{
					token.stop = MTStop.QUOTE;
					token.stopAt = Character.valueOf('"');
				}
				index = closeIndex + 1;
				if(greedy)
				{
					if(index >= end)
						token.stop = MTStop.END;
					else
					if(Character.isWhitespace(format.charAt(index)))
					{}
					else
					if((index + 2 < end)
					&&(format.charAt(index) == '(')
					&&(format.charAt(index + 1) == '\"')
					&&(format.charAt(index + 2) == ')'))
					{
						token.stop = MTStop.QUOTE;
						index += 3;
					}
					else
					{
						final char nc = format.charAt(index);
						if((nc == '[') 
						|| (nc == '(') 
						|| (nc == '/') 
						|| (nc == ']') 
						|| (nc == '.'))
							throw new CMException(L("Greedy placeholder requires an immediately following literal or end-of-string at @x1",""+index));
						final StringBuilder lb = new StringBuilder();
						int la = index;
						while(la < end)
						{
							final char lc = format.charAt(la);
							if(Character.isWhitespace(lc)
							|| (lc == '/') 
							|| (lc == '(') 
							|| (lc == '[') 
							|| (lc == '.') 
							|| (lc == ')'))
								break;
							if(lc == '\\')
							{
								if(la + 1 >= end)
									throw new CMException(L("Trailing backslash at @x1",""+la));
								lb.append(format.charAt(la + 1));
								la += 2;
							}
							else
							{
								lb.append(lc);
								la++;
							}
						}
						if(lb.length() == 0)
							throw new CMException(L("Greedy placeholder requires an immediately following literal or end-of-string at @x1",""+index));
						token.stop = MTStop.END;
						token.stop = MTStop.LITERAL;
						token.stopAt = lb.toString();
					}
				}
			}
			else
			if(c == '.')
			{
				if((index + 2 < end) && (format.charAt(index + 1) == '.'))
				{
					if(tokens.isEmpty() || "/".equals(tokens.get(tokens.size() - 1).literal))
						throw new CMException(L("Nothing to repeat at @x1",""+index));
					final char d = format.charAt(index + 2);
					if(d == '.')
					{
						tokens.get(tokens.size() - 1).repeats = true;
						index += 3;
					}
					else
					if(d == ' ')
					{
						final MTToken x = tokens.get(tokens.size() - 1);
						if(x.parameterArgs == null)
							throw new CMException(L("'.. ' requires a preceding placeholder at @x1",""+index));
						if(x.stop == MTStop.QUOTE)
						{
							x.stop = MTStop.CHAR;
							x.stopAt = Character.valueOf('\"');
						}
						else
							x.stop = MTStop.REST;
						index += 3;
					}
					else
					if(isDelimiterChar(d))
					{
						final MTToken x = tokens.get(tokens.size() - 1);
						final MTToken xReq = new MTToken(x);
						xReq.optional = false;
						xReq.repeats = false;
						final MTToken delim = new MTToken(String.valueOf(d), false);
						final MTToken group = new MTToken(new MTToken[]{delim, xReq}, false);
						group.optional = true;
						group.repeats = true;
						tokens.add(group);
						index += 3;
					}
					else
						throw new CMException(L("Invalid delimiter after '..' at @x1",""+index));
				}
				else
				{
					tokens.add(new MTToken(".", false));
					index++;
				}
			}
			else
			{
				// Collect consecutive literal characters
				final StringBuilder literal = new StringBuilder();
				boolean hadEscape = false;
				while(index < end)
				{
					c = format.charAt(index);
					if(c == '\\')
					{
						if(index + 1 >= end)
							throw new CMException(L("Trailing backslash at @x1",""+index));
						literal.append(format.charAt(index + 1));
						hadEscape = true;
						index += 2;
					}
					else
					if(Character.isWhitespace(c) 
					|| c == '/' 
					|| c == '(' 
					|| c == '[' 
					|| c == '.' 
					|| c == ')')
						break;
					else
					{
						literal.append(c);
						index++;
					}
				}
				if(literal.length() > 0)
				{
					final MTToken litToken = new MTToken(literal.toString(), false);
					if(hadEscape && "/".equals(literal.toString()))
						litToken.literal = ESCAPED_SLASH;
					tokens.add(litToken);
				}
			}
		}
		return collapseMTAlternatives(tokens);
	}

	private MTToken collapseMTAlternatives(final List<MTToken> tokens) throws CMException
	{
		if(tokens.isEmpty())
			throw new CMException(L("Empty sequence"));

		if((tokens.size() > 1) 
		&&("/".equals(tokens.get(1).literal)))
			return collapseFullAlternation(tokens);

		final List<MTToken> result = new ArrayList<>();
		int i = 0;

		while(i < tokens.size())
		{
			if((i+2<tokens.size())
			&&("/".equals(tokens.get(i+1).literal)))
			{
				// Found a slash - collect all alternatives
				final List<MTToken> alternatives = new ArrayList<>();
				alternatives.add(tokens.get(i));
				i += 2; // skip the slash
				alternatives.add(tokens.get(i));
				while((i + 2 < tokens.size())
				&&("/".equals(tokens.get(i+1).literal)))
				{
					i += 2;
					alternatives.add(tokens.get(i));
				}

				final MTToken altToken = new MTToken(alternatives.toArray(new MTToken[0]), false);
				altToken.kind = MTKind.ALTERNATION;
				result.add(altToken);
			}
			else
				result.add(tokens.get(i));
			i++;
		}

		if (result.size() == 1)
			return result.get(0);

		final MTToken seq = new MTToken(result.toArray(new MTToken[0]), false);
		seq.kind = MTKind.SEQUENCE;
		return seq;
	}

	private MTToken collapseFullAlternation(final List<MTToken> tokens) throws CMException
	{
		final List<MTToken> alternatives = new ArrayList<>();
		final List<MTToken> current = new ArrayList<>();
		for(final MTToken t : tokens)
		{
			if("/".equals(t.literal))
			{
				if(current.isEmpty())
					throw new CMException(L("Empty alternative in alternation"));
				alternatives.add(asSequence(current));
				current.clear();
			}
			else
				current.add(t);
		}
		if(current.isEmpty())
			throw new CMException(L("Empty alternative in alternation"));
		alternatives.add(asSequence(current));

		final MTToken altToken = new MTToken(alternatives.toArray(new MTToken[0]), false);
		altToken.kind = MTKind.ALTERNATION;
		return altToken;
	}

	private MTToken asSequence(final List<MTToken> tokens)
	{
		if(tokens.size() == 1)
			return tokens.get(0);
		final MTToken seq = new MTToken(tokens.toArray(new MTToken[0]), false);
		seq.kind = MTKind.SEQUENCE;
		return seq;
	}
}
