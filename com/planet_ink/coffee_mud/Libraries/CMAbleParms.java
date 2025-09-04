package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_web.interfaces.*;
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
   Copyright 2008-2025 Bo Zimmerman

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
				if (str.length() > 0)
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
			if (matcher.find())
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
					if (o instanceof List)
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
				final List<XMLLibrary.XMLTag> pieces=CMLib.xml().parseAllXML(classID);
				if(pieces.size()>0)
				{
					final XMLLibrary.XMLTag tag = CMLib.xml().getPieceFromPieces(pieces, "ITEM");
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
			&&(!A.confirmValue(dataRow.get(d).second)))
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
						if(!A.confirmValue(classFieldData))
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
		private String recipeFilename;
		private String recipeFormat;
		private Vector<Object> columns;
		private Vector<AbilityRecipeRow> dataRows;
		private int numberOfDataColumns;
		public String[] columnHeaders;
		public int[] columnLengths;
		public int classFieldIndex;
		private String parseError = null;
		private boolean wasVFS = false;

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
		public boolean confirmValue(final String oldVal)
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
			case NUMBERORNULL:
			case NUMBER_PAIR:
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
			case NUMBERORNULL:
			case NUMBER_PAIR:
				return "\n\r<INPUT TYPE=TEXT NAME=" + fieldName + " SIZE=10 VALUE=\"" + webValue + "\">";
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
}
