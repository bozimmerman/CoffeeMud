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

import java.net.Socket;
import java.util.*;

/*
   Copyright 2008-2024 Bo Zimmerman

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



	@Override
	public String getGenericClassID(final Ability A)
	{
		if(A==null)
			return null;
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
					fakeSession.getPreviousCMD().clear();
					fakeSession.getPreviousCMD().addAll(new XVector<String>(A.fakeUserInput(oldVal)));
					final String newVal = A.commandLinePrompt(mob,oldVal,showNumber,showFlag);
					editRow.get(a).second=newVal;
				}
				catch (final Exception e)
				{
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
			final String prompt="Save to V)FS, F)ilesystem, or C)ancel (" + (recipe.wasVFS()?"V/f/c":"v/F/c")+"): ";
			final String choice=mob.session().choose(prompt,L("VFC"),recipe.wasVFS()?L("V"):L("F"));
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

	protected static void addExtraMaterial(final Map<Integer,int[]> extraMatsM, final Item I, final Object A, double weight)
	{
		int times = 1;
		if(weight >= 1.0)
		{
			times = (int)Math.round(weight / 1.0);
			weight=.99;
		}
		final MaterialLibrary mats=CMLib.materials();
		final int myBucket = getAppropriateResourceBucket(I,A);
		if(myBucket != RawMaterial.RESOURCE_NOTHING)
		{
			final PairList<Integer,Double> bucket = RawMaterial.CODES.instance().getValueSortedResources(myBucket);
			Integer resourceCode = (bucket.size()==0)
					? Integer.valueOf(CMLib.dice().pick( RawMaterial.CODES.ALL(), I.material() ))
					: bucket.get( (weight>=.99) ? bucket.size()-1 : 0 ).first;
			for (final Pair<Integer, Double> p : bucket)
			{
				if((weight <= p.second.doubleValue())&&(mats.isResourceCodeRoomMapped(p.first.intValue())))
				{
					resourceCode = p.first;
					break;
				}
			}
			int tries=100;
			while((--tries>0)&&(!mats.isResourceCodeRoomMapped(resourceCode.intValue())))
				resourceCode=bucket.get(CMLib.dice().roll(1, bucket.size(), -1)).first;
			resourceCode = Integer.valueOf( resourceCode.intValue() );
			for(int x=0;x<times;x++)
			{
				final int[] amt = extraMatsM.get( resourceCode );
				if(amt == null)
					extraMatsM.put( resourceCode, new int[]{1} );
				else
					amt[0]++;
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected static Pair<String[],String[]> getBuildingCodesNFlags()
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

	protected static void addExtraAbilityMaterial(final Map<Integer,int[]> extraMatsM, final Item I, final Ability A)
	{
		double level = CMLib.ableMapper().lowestQualifyingLevel( A.ID() );
		if( level <= 0.0 )
		{
			level = I.basePhyStats().level();
			if( level <= 0.0 )
				level = 1.0;
			addExtraMaterial(extraMatsM, I, A, CMath.div( level, CMProps.getIntVar( CMProps.Int.LASTPLAYERLEVEL ) ));
		}
		else
		{
			final double levelCap = CMLib.ableMapper().getCalculatedMedianLowestQualifyingLevel();
			addExtraMaterial(extraMatsM, I, A, CMath.div(level , ( levelCap * 2.0)));
		}
	}

	public static Map<Integer,int[]> extraMaterial(final Item I)
	{
		final Map<Integer,int[]> extraMatsM=new TreeMap<Integer,int[]>();
		/*
		 * behaviors/properties of the item
		 */
		for(final Enumeration<Ability> a=I.effects(); a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A.isSavable())
			{
				if((A.abilityCode() & Ability.ALL_ACODES) == Ability.ACODE_PROPERTY)
				{
					if(A instanceof AbilityContainer)
					{
						for(final Enumeration<Ability> a1=((AbilityContainer)A).allAbilities(); a1.hasMoreElements(); )
						{
							addExtraAbilityMaterial(extraMatsM,I,a1.nextElement());
						}
					}
					if(A instanceof TriggeredAffect)
					{
						if((A.flags() & Ability.FLAG_ADJUSTER) != 0)
						{
							int count = CMStrings.countSubstrings( new String[]{A.text()}, ADJUSTER_TOKENS );
							if(count == 0)
								count = 1;
							for(int i=0;i<count;i++)
								addExtraAbilityMaterial(extraMatsM,I,A);
						}
						else
						if((A.flags() & (Ability.FLAG_RESISTER | Ability.FLAG_IMMUNER)) != 0)
						{
							int count = CMStrings.countSubstrings( new String[]{A.text()}, RESISTER_IMMUNER_TOKENS );
							if(count == 0)
								count = 1;
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
		for(final Enumeration<Behavior> b=I.behaviors(); b.hasMoreElements();)
		{
			final Behavior B=b.nextElement();
			if(B.isSavable())
			{
				addExtraMaterial(extraMatsM, I, B, CMath.div( CMProps.getIntVar( CMProps.Int.LASTPLAYERLEVEL ), I.basePhyStats().level() ));
			}
		}
		return extraMatsM;
	}

	@Override
	public synchronized Map<String,AbilityParmEditor> getEditors()
	{
		if(DEFAULT_EDITORS != null)
			return DEFAULT_EDITORS;

		final Vector<AbilityParmEditorImpl> V=new XVector<AbilityParmEditorImpl>(new AbilityParmEditorImpl[]
		{
			new AbilityParmEditorImpl("SPELL_ID","The Spell ID",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(CMClass.abilities());
				}

				@Override
				public String defaultValue()
				{
					return "Spell_ID";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Potion)
						return ((Potion)I).getSpellList();
					else
					if((I instanceof Scroll)
					&&(I instanceof MiscMagic))
						return ((Scroll)I).getSpellList();
					return "";
				}
			},
			new AbilityParmEditorImpl("RESOURCE_NAME","Resource",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(RawMaterial.CODES.NAMES());
				}

				@Override
				public String defaultValue()
				{
					return "IRON";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return RawMaterial.CODES.NAME(I.material());
				}
			},
			new AbilityParmEditorImpl("ITEM_NAME","Item Final Name",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "Item Name";
				}

				@Override
				public int minColWidth()
				{
					return 10;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					final String oldName=CMStrings.removeColors(I.Name());
					if(I.material()==RawMaterial.RESOURCE_GLASS)
						return CMLib.english().removeArticleLead(oldName);

					String newName=oldName;
					final List<String> V=CMParms.parseSpaces(oldName,true);
					for(int i=0;i<V.size();i++)
					{
						final String s=V.get(i);
						final int code=RawMaterial.CODES.FIND_IgnoreCase(s);
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
							final String s=V.get(i);
							final int code=RawMaterial.CODES.FIND_IgnoreCase(s);
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
						{
							if(CMLib.english().isAnArticle( V.get( i ) ))
							{
								if(i==0)
									V.set( i, "%" );
								else
									V.add(i+1, "%");
								break;
							}
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
			new AbilityParmEditorImpl("COLOR_TERM","Color term",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "color name";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public int minColWidth()
				{
					return 5;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("ACTIVE_VERB","Active Verb",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "verbing";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public int minColWidth()
				{
					return 3;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("DISPLAY_MASK","Display Mask",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "@x1 is here";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public int minColWidth()
				{
					return 3;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return CMStrings.replaceAll(I.displayText(), I.Name(), "@x1");
				}
			},
			new AbilityParmEditorImpl("COLOR_MASK","Color mask",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.length()>0)
					{
						for(int i=0;i<oldVal.length();i++)
							if("0123cCwWeEFm-*|".indexOf(oldVal.charAt(i))<0)
								return false;
					}
					return true;
				}

				@Override
				public int minColWidth()
				{
					return 5;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("EXPERTISENUM","Expertise#",ParmType.NUMBERORNULL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public int minColWidth()
				{
					return 5;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("EXPERTISE","Expertise",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(CMLib.expertises().definitions());
					super.choices.add(0,new Pair<String,String>("","NA"));
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if((oldVal==null)||(oldVal.trim().length()==0))
						return true;
					return CMLib.expertises().findDefinition(oldVal, true) != null;
				}

				@Override
				public int minColWidth()
				{
					return 15;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("STAIRS_DESC","Exit Desc",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equals("STAIRS"))
							return 1;
					}
					return -1;
				}

				@Override
				public String defaultValue()
				{
					return "@x1stairs to the @x2 floor";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("ZAPPERMASK","Zappermask",ParmType.STRINGORNULL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return 1;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("BUILDING_NOUN","Building noun",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "thing";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("BUILDER_MASK","Builder Mask",ParmType.STRINGORNULL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("BUILDER_DESC","Info Description",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public int maxColWidth()
				{
					return 20;
				}

			},
			new AbilityParmEditorImpl("RES_SUBTYPE","Sub-Type",ParmType.STRINGORNULL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof RawMaterial)
						return 5;
					return -1;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof RawMaterial)
						return ((RawMaterial)I).getSubType();
					return "";
				}
				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag)
				throws java.io.IOException
				{
					return super.commandLinePrompt(mob, oldVal, showNumber, showFlag).toUpperCase().trim();
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					return super.webValue(httpReq,parms,oldVal,fieldName).toUpperCase().trim();
				}
			},
			new AbilityParmEditorImpl("ITEM_LEVEL","Lvl",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if((I instanceof Weapon)||(I instanceof Armor))
					{
						final int timsLevel = CMLib.itemBuilder().timsLevelCalculator(I);
						if((timsLevel > I.basePhyStats().level() ) && (timsLevel < CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)))
							return ""+timsLevel;
					}
					return ""+I.basePhyStats().level();
				}
			},
			new AbilityParmEditorImpl("BUILDING_GRID_SIZE","Grid Size",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equals("ROOM"))
							return 1;
					}
					return -1;
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "1";
				}
			},
			new AbilityParmEditorImpl("BUILD_TIME_TICKS","Time",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "20";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return ""+(10 + (I.basePhyStats().level()/2));
				}
			},
			new AbilityParmEditorImpl("XLEVEL","Expertise Level",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "0";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "0";
				}
			},
			new AbilityParmEditorImpl("FUTURE_USE","Future Use",ParmType.STRINGORNULL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "1";
				}
			},
			new AbilityParmEditorImpl("AMOUNT_MATERIAL_REQUIRED","Amt",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public String defaultValue()
				{
					return "10";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return ""+Math.round(CMath.mul(I.basePhyStats().weight(),(A!=null)?A.getItemWeightMultiplier(false):1.0));
				}
			},
			new AbilityParmEditorImpl("MATERIALS_REQUIRED","Amount/Cmp",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					if(httpReq.isUrlParameter(fieldName+"_WHICH"))
					{
						final String which=httpReq.getUrlParameter(fieldName+"_WHICH");
						if((which.trim().length()==0)||(which.trim().equalsIgnoreCase("AMOUNT")))
							return httpReq.getUrlParameter(fieldName+"_AMOUNT");
						if(which.trim().equalsIgnoreCase("COMPONENT"))
							return httpReq.getUrlParameter(fieldName+"_COMPONENT");
						int x=1;
						final List<AbilityComponent> comps=new Vector<AbilityComponent>();
						while(httpReq.isUrlParameter(fieldName+"_CUST_TYPE_"+x))
						{
							String connector=httpReq.getUrlParameter(fieldName+"_CUST_CONN_"+x);
							final String amt=httpReq.getUrlParameter(fieldName+"_CUST_AMT_"+x);
							final String strVal=httpReq.getUrlParameter(fieldName+"_CUST_STR_"+x);
							final String loc=httpReq.getUrlParameter(fieldName+"_CUST_LOC_"+x);
							final String def=httpReq.getUrlParameter(fieldName+"_CUST_DEF_"+x);
							final String typ=httpReq.getUrlParameter(fieldName+"_CUST_TYPE_"+x);
							final String styp=httpReq.getUrlParameter(fieldName+"_CUST_STYPE_"+x);
							final String con=httpReq.getUrlParameter(fieldName+"_CUST_CON_"+x);
							if(connector==null)
								connector="AND";
							if(connector.equalsIgnoreCase("DEL")||(connector.length()==0))
							{
								x++;
								continue;
							}
							try
							{
								final AbilityComponent able=CMLib.ableComponents().createBlankAbilityComponent("");
								able.setConnector(AbilityComponent.CompConnector.valueOf(connector));
								if(able.getConnector()==CompConnector.MESSAGE)
									able.setMask((def==null)?"":def);
								else
								{
									able.setAmount(CMath.s_int(amt));
									able.setMask((def==null)?"":def);
									able.setTriggererDef("");
									able.setConsumed((con!=null) && con.equalsIgnoreCase("on"));
									able.setLocation(AbilityComponent.CompLocation.valueOf(loc));
									if(CMath.s_valueOf(CompType.class, typ)!=null)
										able.setType(CompType.valueOf(typ), strVal, styp);
								}
								comps.add(able);
							}
							catch(final Exception e)
							{
							}
							x++;
						}
						if(comps.size()>0)
							return CMLib.ableComponents().getAbilityComponentCodedString(comps);
					}
					return oldVal;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					int amt=(int)Math.round(CMath.mul(I.basePhyStats().weight()-1,(A!=null)?A.getItemWeightMultiplier(false):1.0));
					if(amt<1)
						amt=1;
					final Map<Integer,int[]> extraMatsM = CMAbleParms.extraMaterial( I );
					if((extraMatsM == null) || (extraMatsM.size()==0))
					{
						return ""+amt;
					}
					final String subType = (I instanceof RawMaterial)?((RawMaterial)I).getSubType():"";
					final List<AbilityComponent> comps=new Vector<AbilityComponent>();
					AbilityComponent able=CMLib.ableComponents().createBlankAbilityComponent("");
					able.setConnector(CompConnector.AND);
					able.setAmount(amt);
					able.setMask("");
					able.setTriggererDef("");
					able.setConsumed(true);
					able.setLocation(CompLocation.ONGROUND);
					able.setType(CompType.MATERIAL, Integer.valueOf(I.material() & RawMaterial.MATERIAL_MASK), subType);
					comps.add(able);
					for(final Integer resourceCode : extraMatsM.keySet())
					{
						able=CMLib.ableComponents().createBlankAbilityComponent("");
						able.setConnector(CompConnector.AND);
						able.setAmount(extraMatsM.get(resourceCode)[0]);
						able.setMask("");
						able.setTriggererDef("");
						able.setConsumed(true);
						able.setLocation(CompLocation.ONGROUND);
						able.setType(CompType.RESOURCE, resourceCode, "");
						comps.add(able);
					}
					return CMLib.ableComponents().getAbilityComponentCodedString(comps);
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					String value=webValue(httpReq,parms,oldVal,fieldName);
					if(value.endsWith("$"))
						value = value.substring(0,oldVal.length()-1);
					value = value.trim();
					final String curWhich=httpReq.getUrlParameter(fieldName+"_WHICH");
					int type=0;
					if("COMPONENT".equalsIgnoreCase(curWhich))
						type=1;
					else
					if("EMBEDDED".equalsIgnoreCase(curWhich))
						type=2;
					else
					if("AMOUNT".equalsIgnoreCase(curWhich))
						type=0;
					else
					if(CMLib.ableComponents().getAbilityComponentMap().containsKey(value.toUpperCase().trim()))
						type=1;
					else
					if(value.startsWith("("))
						type=2;
					else
						type=0;

					List<AbilityComponent> comps=null;
					if(type==2)
					{
						final Hashtable<String,List<AbilityComponent>> H=new Hashtable<String,List<AbilityComponent>>();
						final String s="ID="+value;
						CMLib.ableComponents().addAbilityComponent(s, H);
						comps=H.get("ID");
					}
					if(comps==null)
						comps=new ArrayList<AbilityComponent>(1);

					final StringBuffer str = new StringBuffer("<FONT SIZE=-1>");
					str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==0?"CHECKED ":"")+"VALUE=\"AMOUNT\">");
					str.append("\n\rAmount: <INPUT TYPE=TEXT SIZE=3 NAME="+fieldName+"_AMOUNT VALUE=\""+(type!=0?"":value)+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[0].checked=true;\">");
					str.append("\n\r<BR>");
					str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==1?"CHECKED ":"")+"VALUE=\"COMPONENT\">");
					str.append(L("\n\rSkill Components:"));
					str.append("\n\r<SELECT NAME="+fieldName+"_COMPONENT ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[1].checked=true;\">");
					str.append("<OPTION VALUE=\"0\"");
					if((type!=1)||(value.length()==0)||(value.equalsIgnoreCase("0")))
						str.append(" SELECTED");
					str.append(">&nbsp;");
					for(final String S : CMLib.ableComponents().getAbilityComponentMap().keySet())
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
							for(final CompConnector conector : CompConnector.values())
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
						final CompType compType=(comp!=null)?comp.getType():CompType.STRING;
						final String subType=(comp != null)?comp.getSubType():"";
						for(final CompType conn : CompType.values())
						{
							str.append("<OPTION VALUE=\""+conn.toString()+"\" ");
							if(conn==compType)
								str.append("SELECTED ");
							str.append(">"+CMStrings.capitalizeAndLower(conn.toString()));
						}
						str.append("</SELECT>");
						if(compType==CompType.STRING)
							str.append("\n\r<INPUT TYPE=TEXT SIZE=10 NAME="+fieldName+"_CUST_STR_"+(i+1)+" VALUE=\""+(((type!=2)||(comp==null))?"":comp.getStringType())+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
						else
						{
							str.append("\n\r<SELECT NAME="+fieldName+"_CUST_STR_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
							if(compType==CompType.MATERIAL)
							{
								final RawMaterial.Material[] M=RawMaterial.Material.values();
								Arrays.sort(M,new Comparator<RawMaterial.Material>()
								{
									@Override
									public int compare(final Material o1, final Material o2)
									{
										return o1.name().compareToIgnoreCase(o2.name());
									}
								});
								for(final RawMaterial.Material m : M)
								{
									str.append("<OPTION VALUE="+m.mask());
									if((type==2)&&(comp!=null)&&(m.mask()==comp.getLongType()))
										str.append(" SELECTED");
									str.append(">"+m.noun());
								}
							}
							else
							if(compType==CompType.RESOURCE)
							{
								final List<Pair<String,Integer>> L=new Vector<Pair<String,Integer>>();
								for(int x=0;x<RawMaterial.CODES.TOTAL();x++)
									L.add(new Pair<String,Integer>(RawMaterial.CODES.NAME(x),Integer.valueOf(RawMaterial.CODES.GET(x))));
								Collections.sort(L,new Comparator<Pair<String,Integer>>()
								{
									@Override
									public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2)
									{
										return o1.first.compareToIgnoreCase(o2.first);
									}
								});
								for(final Pair<String,Integer> p : L)
								{
									str.append("<OPTION VALUE="+p.second);
									if((type==2)&&(comp!=null)&&(p.second.longValue()==comp.getLongType()))
										str.append(" SELECTED");
									str.append(">"+p.first);
								}
							}
							str.append("</SELECT>");
							str.append(" <INPUT TYPE=TEXT SIZE=2 NAME="+fieldName+"_CUST_STYPE_"+(i+1)+" VALUE=\""+subType+"\">");
						}
						str.append("\n\r<SELECT NAME="+fieldName+"_CUST_LOC_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
						for(final CompLocation conn : CompLocation.values())
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

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					return new String[] { oldVal };
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					++showNumber[0];
					String str = oldVal;
					while(!mob.session().isStopped())
					{
						final String help="<AMOUNT>"
							+"\n\rSkill Component: "+CMParms.toListString(CMLib.ableComponents().getAbilityComponentMap().keySet())
							+"\n\rCustom Component: ([DISPOSITION]:[FATE]:[AMOUNT]:[COMPONENT ID]:[MASK]) && ...";
						str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,help).trim();
						if(str.equals(oldVal))
							return oldVal;
						if(CMath.isInteger(str))
							return Integer.toString(CMath.s_int(str));
						if(CMLib.ableComponents().getAbilityComponentMap().containsKey(str.toUpperCase().trim()))
							return str.toUpperCase().trim();
						String error=null;
						if(str.trim().startsWith("("))
						{
							error=CMLib.ableComponents().addAbilityComponent("ID="+str, new Hashtable<String,List<AbilityComponent>>());
							if(error==null)
								return str;
						}
						mob.session().println(L("'@x1' is not an amount of material, a component key, or custom component list@x2.  Please use ? for help.",str,(error==null?"":"("+error+")")));
					}
					return str;
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}
			},
			new AbilityParmEditorImpl("OPTIONAL_AMOUNT_REQUIRED","Amt",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("ITEM_BASE_VALUE","Value",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "5";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return ""+I.baseGoldValue();
				}
			},
			new AbilityParmEditorImpl("FREQUENCY","Frequency",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "1";
				}
			},
			new AbilityParmEditorImpl("ROOM_CLASS_ID","Class ID",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equalsIgnoreCase("ROOM")
						||chk.equalsIgnoreCase("EXCAVATE"))
							return 1;
					}
					return -1;
				}

				@Override
				public void createChoices()
				{
					final Vector<Environmental> V  = new Vector<Environmental>();
					V.addAll(new XVector<Room>(CMClass.locales()));
					createChoices(V);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "Plains";
				}
			},
			new AbilityParmEditorImpl("ALLITEM_CLASS_ID","Class ID",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equalsIgnoreCase("ITEM"))
							return 1;
					}
					return -1;
				}

				@Override
				public void createChoices()
				{
					final XVector<Environmental> V  = new XVector<Environmental>();
					V.addAll(CMClass.basicItems());
					V.addAll(CMClass.weapons());
					V.addAll(CMClass.tech());
					V.addAll(CMClass.armor());
					V.addAll(CMClass.miscMagic());
					V.addAll(CMClass.clanItems());
					for(int i=V.size()-1;i<=0;i--)
					{
						if(V.get(i) instanceof CMObjectWrapper)
							V.remove(i);
					}
					createChoices(V);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "StdItem";
				}
			},
			new AbilityParmEditorImpl("ROOM_CLASS_ID_OR_NONE","Class ID",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equalsIgnoreCase("DEMOLISH")
						||chk.equalsIgnoreCase("STAIRS"))
							return 1;
					}
					return -1;
				}

				@Override
				public void createChoices()
				{
					final Vector<Object> V  = new Vector<Object>();
					V.add("");
					V.addAll(new XVector<Room>(CMClass.locales()));
					createChoices(V);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("EXIT_CLASS_ID","Class ID",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equalsIgnoreCase("DOOR"))
							return 1;
					}
					return -1;
				}

				@Override
				public void createChoices()
				{
					final Vector<Environmental> V  = new Vector<Environmental>();
					V.addAll(new XVector<Exit>(CMClass.exits()));
					final Vector<CMObject> V2=new Vector<CMObject>();
					Environmental I;
					for(final Enumeration<Environmental> e=V.elements();e.hasMoreElements();)
					{
						I=e.nextElement();
						if(I.isGeneric())
							V2.addElement(I);
					}
					createChoices(V2);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "GenExit";
				}
			},
			new AbilityParmEditorImpl("ITEM_CLASS_ID","Class ID",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					final List<Item> V  = new ArrayList<Item>();
					V.addAll(new XVector<ClanItem>(CMClass.clanItems()));
					V.addAll(new XVector<Armor>(CMClass.armor()));
					V.addAll(new XVector<Item>(CMClass.basicItems()));
					V.addAll(new XVector<MiscMagic>(CMClass.miscMagic()));
					V.addAll(new XVector<Technical>(CMClass.tech()));
					V.addAll(new XVector<Weapon>(CMClass.weapons()));
					final List<Item> V2=new ArrayList<Item>();
					Item I;
					for(final Iterator<Item> e=V.iterator();e.hasNext();)
					{
						I=e.next();
						if(I.isGeneric() || I.ID().equalsIgnoreCase("StdDeckOfCards"))
							V2.add(I);
					}
					for(int i=V.size()-1;i<=0;i--)
					{
						if(V.get(i) instanceof CMObjectWrapper)
							V.remove(i);
					}
					createChoices(V2);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I.isGeneric())
						return I.ID();
					if(I instanceof Weapon)
						return "GenWeapon";
					if(I instanceof Armor)
					{
						if(I instanceof Container)
							return "GenArmor";
						else
							return "GenWearable";
					}
					if(I instanceof Rideable)
						return "GenRideable";
					return "GenItem";
				}

				@Override
				public String defaultValue()
				{
					return "GenItem";
				}
			},
			new AbilityParmEditorImpl("CODED_WEAR_LOCATION","Wear Locs",ParmType.SPECIAL)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof FalseLimb)
						return -1;
					return ((o instanceof Armor) || (o instanceof MusicalInstrument)) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return oldVal.trim().length() > 0;
				}

				@Override
				public String defaultValue()
				{
					return "NECK";
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final short[] layerAtt = new short[1];
					final short[] layers = new short[1];
					final long[] wornLoc = new long[1];
					final boolean[] logicalAnd = new boolean[1];
					final double[] hardBonus=new double[1];
					CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
					if(httpReq.isUrlParameter(fieldName+"_WORNDATA"))
					{
						wornLoc[0]=CMath.s_long(httpReq.getUrlParameter(fieldName+"_WORNDATA"));
						for(int i=1;;i++)
							if(httpReq.isUrlParameter(fieldName+"_WORNDATA"+(Integer.toString(i))))
								wornLoc[0]=wornLoc[0]|CMath.s_long(httpReq.getUrlParameter(fieldName+"_WORNDATA"+(Integer.toString(i))));
							else
								break;
						logicalAnd[0] = httpReq.getUrlParameter(fieldName+"_ISTWOHANDED").equalsIgnoreCase("on");
						layers[0] = CMath.s_short(httpReq.getUrlParameter(fieldName+"_LAYER"));
						layerAtt[0] = 0;
						if((httpReq.isUrlParameter(fieldName+"_SEETHRU"))
						&&(httpReq.getUrlParameter(fieldName+"_SEETHRU").equalsIgnoreCase("on")))
							layerAtt[0] |= Armor.LAYERMASK_SEETHROUGH;
						if((httpReq.isUrlParameter(fieldName+"_MULTIWEAR"))
						&&(httpReq.getUrlParameter(fieldName+"_MULTIWEAR").equalsIgnoreCase("on")))
							layerAtt[0] |= Armor.LAYERMASK_MULTIWEAR;
					}
					return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String value = webValue(httpReq,parms,oldVal,fieldName);
					final short[] layerAtt = new short[1];
					final short[] layers = new short[1];
					final long[] wornLoc = new long[1];
					final boolean[] logicalAnd = new boolean[1];
					final double[] hardBonus=new double[1];
					CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,value);
					final StringBuffer str = new StringBuffer("");
					str.append("\n\r<SELECT NAME="+fieldName+"_WORNDATA MULTIPLE>");
					final Wearable.CODES codes = Wearable.CODES.instance();
					for(int i=1;i<codes.total();i++)
					{
						final String climstr=codes.name(i);
						final int mask=(int)CMath.pow(2,i-1);
						str.append("<OPTION VALUE="+mask);
						if((wornLoc[0]&mask)>0)
							str.append(" SELECTED");
						str.append(">"+climstr);
					}
					str.append("</SELECT>");
					str.append("<BR>\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_ISTWOHANDED value=\"on\" "+(logicalAnd[0]?"CHECKED":"")+">Is worn on All above Locations.");
					str.append("<BR>\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_ISTWOHANDED value=\"\" "+(logicalAnd[0]?"":"CHECKED")+">Is worn on ANY of the above Locations.");
					str.append("<BR>\n\rLayer: <INPUT TYPE=TEXT NAME="+fieldName+"_LAYER SIZE=5 VALUE=\""+layers[0]+"\">");
					final boolean seeThru = CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH);
					final boolean multiWear = CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR);
					str.append("&nbsp;&nbsp;\n\r<INPUT TYPE=CHECKBOX NAME="+fieldName+"_SEETHRU value=\"on\" "+(seeThru?"CHECKED":"")+">Is see-through.");
					str.append("&nbsp;&nbsp;\n\r<INPUT TYPE=CHECKBOX NAME="+fieldName+"_MULTIWEAR value=\"on\" "+(multiWear?"CHECKED":"")+">Is multi-wear.");
					return str.toString();
				}

				public String reconvert(final short[] layerAtt, final short[] layers, final long[] wornLoc, final boolean[] logicalAnd, final double[] hardBonus)
				{
					final StringBuffer newVal = new StringBuffer("");
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
					final Wearable.CODES codes = Wearable.CODES.instance();
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

				@Override
				public String convertFromItem(final ItemCraftor C, final Item I)
				{
					if(!(I instanceof Armor))
						return "HELD";
					final Armor A=(Armor)I;
					final boolean[] logicalAnd=new boolean[]{I.rawLogicalAnd()};
					final long[] wornLoc=new long[]{I.rawProperLocationBitmap()};
					final double[] hardBonus=new double[]{0.0};
					final short[] layerAtt=new short[]{A.getLayerAttributes()};
					final short[] layers=new short[]{A.getClothingLayer()};
					return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					final ArrayList<String> V = new ArrayList<String>();
					final short[] layerAtt = new short[1];
					final short[] layers = new short[1];
					final long[] wornLoc = new long[1];
					final boolean[] logicalAnd = new boolean[1];
					final double[] hardBonus=new double[1];
					CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
					V.add(""+layers[0]);
					if(CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH))
						V.add("Y");
					else
						V.add("N");
					if(CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR))
						V.add("Y");
					else
						V.add("N");
					V.add("1");
					V.add("1");
					final Wearable.CODES codes = Wearable.CODES.instance();
					for(int i=0;i<codes.total();i++)
					{
						if(CMath.bset(wornLoc[0],codes.get(i)))
						{
							V.add(""+(i+2));
							V.add(""+(i+2));
						}
					}
					V.add("0");
					return CMParms.toStringArray(V);
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					final short[] layerAtt = new short[1];
					final short[] layers = new short[1];
					final long[] wornLoc = new long[1];
					final boolean[] logicalAnd = new boolean[1];
					final double[] hardBonus=new double[1];
					CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
					CMLib.genEd().wornLayer(mob,layerAtt,layers,++showNumber[0],showFlag);
					CMLib.genEd().wornLocation(mob,wornLoc,logicalAnd,++showNumber[0],showFlag);
					return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
				}
			},
			new AbilityParmEditorImpl("PAGES_CHARS","Max Pgs/Chrs.",ParmType.SPECIAL)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Book) ? 1 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "1/0";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Book)
						return ""+((Book)I).getMaxPages()+"/"+((Book)I).getMaxCharsPerPage();
					return "1/0";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return oldVal.trim().length() > 0;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					int maxPages=1;
					int maxCharsPage=0;
					if(oldVal.length()>0)
					{
						final int x=oldVal.indexOf('/');
						if(x>0)
						{
							maxPages=CMath.s_int(oldVal.substring(0,x));
							maxCharsPage=CMath.s_int(oldVal.substring(x+1));
						}
					}
					if(httpReq.isUrlParameter(fieldName+"_MAXPAGES"))
						maxPages = CMath.s_int(httpReq.getUrlParameter(fieldName+"_MAXPAGES"));
					if(httpReq.isUrlParameter(fieldName+"_MAXCHARSPAGE"))
						maxCharsPage = CMath.s_int(httpReq.getUrlParameter(fieldName+"_MAXCHARSPAGE"));
					return ""+maxPages+"/"+maxCharsPage;
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String value = webValue(httpReq, parms, oldVal, fieldName);
					final StringBuffer str = new StringBuffer("");
					str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0><TR>");
					final String[] vals = this.fakeUserInput(value);
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Max Pages")+"</FONT></TD>");
					str.append("<TD WIDTH=25%><INPUT TYPE=TEXT SIZE=5 NAME="+fieldName+"_MAXPAGES VALUE=\""+vals[0]+"\">");
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Max Chars Page")+"</FONT></TD>");
					str.append("<TD WIDTH=25%><INPUT TYPE=TEXT SIZE=5 NAME="+fieldName+"_MAXCHARSPAGE VALUE=\""+vals[1]+"\">");
					str.append("</TD>");
					str.append("</TR></TABLE>");
					return str.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					final ArrayList<String> V = new ArrayList<String>();
					int maxPages=1;
					int maxCharsPage=0;
					if(oldVal.length()>0)
					{
						final int x=oldVal.indexOf('/');
						if(x>0)
						{
							maxPages=CMath.s_int(oldVal.substring(0,x));
							maxCharsPage=CMath.s_int(oldVal.substring(x+1));
						}
					}
					V.add(""+maxPages);
					V.add(""+maxCharsPage);
					return CMParms.toStringArray(V);
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					final String[] input=this.fakeUserInput(oldVal);
					int maxPages=CMath.s_int(input[0]);
					int maxCharsPage=CMath.s_int(input[1]);
					maxPages = CMLib.genEd().prompt(mob, maxPages, ++showNumber[0], showFlag, L("Max Pages"), null);
					maxCharsPage = CMLib.genEd().prompt(mob, maxCharsPage, ++showNumber[0], showFlag, L("Max Chars/Page"), null);
					return maxPages+"/"+maxCharsPage;
				}
			},
			new AbilityParmEditorImpl("RIDE_OVERRIDE_STRS","Ride Strings",ParmType.SPECIAL)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Rideable) ? 1 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Rideable)
					{
						final Rideable R=(Rideable)I;
						final StringBuilder str=new StringBuilder("");
						//STATESTR,STATESUBJSTR,RIDERSTR,MOUNTSTR,DISMOUNTSTR,PUTSTR
						str.append(R.getStateString().replace(';', ',')).append(';');
						str.append(R.getStateStringSubject().replace(';', ',')).append(';');
						str.append(R.getRideString().replace(';', ',')).append(';');
						str.append(R.getMountString().replace(';', ',')).append(';');
						str.append(R.getDismountString().replace(';', ',')).append(';');
						str.append(R.getPutString().replace(';', ','));
						if(str.length()==5)
							return "";
						return str.toString();
					}
					return "";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String[] finput = this.fakeUserInput(oldVal);
					String stateStr=finput[0];
					String stateSubjectStr=finput[1];
					String riderStr=finput[2];
					String mountStr=finput[3];
					String dismountStr=finput[4];
					String putStr=finput[5];
					if(httpReq.isUrlParameter(fieldName+"_RSTATESTR"))
						stateStr = httpReq.getUrlParameter(fieldName+"_RSTATESTR");
					if(httpReq.isUrlParameter(fieldName+"_RSTATESUBJSTR"))
						stateSubjectStr = httpReq.getUrlParameter(fieldName+"_RSTATESUBJSTR");
					if(httpReq.isUrlParameter(fieldName+"_RRIDERSTR"))
						riderStr = httpReq.getUrlParameter(fieldName+"_RRIDERSTR");
					if(httpReq.isUrlParameter(fieldName+"_RMOUNTSTR"))
						mountStr = httpReq.getUrlParameter(fieldName+"_RMOUNTSTR");
					if(httpReq.isUrlParameter(fieldName+"_RDISMOUNTSTR"))
						dismountStr = httpReq.getUrlParameter(fieldName+"_RDISMOUNTSTR");
					if(httpReq.isUrlParameter(fieldName+"_RPUTSTR"))
						putStr = httpReq.getUrlParameter(fieldName+"_RPUTSTR");
					final StringBuilder str=new StringBuilder("");
					str.append(stateStr.replace(';', ',')).append(';');
					str.append(stateSubjectStr.replace(';', ',')).append(';');
					str.append(riderStr.replace(';', ',')).append(';');
					str.append(mountStr.replace(';', ',')).append(';');
					str.append(dismountStr.replace(';', ',')).append(';');
					str.append(putStr.replace(';', ','));
					if(str.length()==5)
						return "";
					return str.toString();
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String value = webValue(httpReq, parms, oldVal, fieldName);
					final StringBuffer str = new StringBuffer("");
					str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
					final String[] vals = this.fakeUserInput(value);
					str.append("<TR>");
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("State")+"</FONT></TD>");
					str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RSTATESTR VALUE=\""+vals[0]+"\">");
					str.append("</TR>");
					str.append("<TR>");
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("State Subj.")+"</FONT></TD>");
					str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RSTATESUBJSTR VALUE=\""+vals[1]+"\">");
					str.append("</TR>");
					str.append("<TR>");
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Rider")+"</FONT></TD>");
					str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RRIDERSTR VALUE=\""+vals[2]+"\">");
					str.append("</TR>");
					str.append("<TR>");
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Mount")+"</FONT></TD>");
					str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RMOUNTSTR VALUE=\""+vals[3]+"\">");
					str.append("</TR>");
					str.append("<TR>");
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Dismount")+"</FONT></TD>");
					str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RDISMOUNTSTR VALUE=\""+vals[4]+"\">");
					str.append("</TR>");
					str.append("<TR>");
					str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Put")+"</FONT></TD>");
					str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RPUTSTR VALUE=\""+vals[5]+"\">");
					str.append("</TR>");
					str.append("</TABLE>");
					return str.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					final ArrayList<String> V = new ArrayList<String>();
					String stateStr="";
					String stateSubjectStr="";
					String riderStr="";
					String mountStr="";
					String dismountStr="";
					String putStr="";
					if(oldVal.length()>0)
					{
						final List<String> lst=CMParms.parseSemicolons(oldVal.trim(),false);
						if(lst.size()>0)
							stateStr=lst.get(0).replace(';',',');
						if(lst.size()>1)
							stateSubjectStr=lst.get(1).replace(';',',');
						if(lst.size()>2)
							riderStr=lst.get(2).replace(';',',');
						if(lst.size()>3)
							mountStr=lst.get(3).replace(';',',');
						if(lst.size()>4)
							dismountStr=lst.get(4).replace(';',',');
						if(lst.size()>5)
							putStr=lst.get(5).replace(';',',');
					}
					V.add(stateStr);
					V.add(stateSubjectStr);
					V.add(riderStr);
					V.add(mountStr);
					V.add(dismountStr);
					V.add(putStr);
					return CMParms.toStringArray(V);
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					final String[] finput = this.fakeUserInput(oldVal);
					String stateStr=finput[0];
					String stateSubjectStr=finput[1];
					String riderStr=finput[2];
					String mountStr=finput[3];
					String dismountStr=finput[4];
					String putStr=finput[5];
					stateStr = CMLib.genEd().prompt(mob, stateStr, ++showNumber[0], showFlag, L("State Str"), true);
					stateSubjectStr = CMLib.genEd().prompt(mob, stateSubjectStr, ++showNumber[0], showFlag, L("State Subject"), true);
					riderStr = CMLib.genEd().prompt(mob, riderStr, ++showNumber[0], showFlag, L("Ride Str"), true);
					mountStr = CMLib.genEd().prompt(mob, mountStr, ++showNumber[0], showFlag, L("Mount Str"), true);
					dismountStr = CMLib.genEd().prompt(mob, dismountStr, ++showNumber[0], showFlag, L("Dismount Str"), true);
					putStr = CMLib.genEd().prompt(mob, putStr, ++showNumber[0], showFlag, L("Put Str"), true);
					final StringBuilder str=new StringBuilder("");
					str.append(stateStr.replace(';', ',')).append(';');
					str.append(stateSubjectStr.replace(';', ',')).append(';');
					str.append(riderStr.replace(';', ',')).append(';');
					str.append(mountStr.replace(';', ',')).append(';');
					str.append(dismountStr.replace(';', ',')).append(';');
					str.append(putStr.replace(';', ','));
					if(str.length()==5)
						return "";
					return str.toString();
				}
			},
			new AbilityParmEditorImpl("CONTAINER_CAPACITY","Cap.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Container) ? 1 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "20";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Container)
						return ""+((Container)I).capacity();
					return "0";
				}
			},
			new AbilityParmEditorImpl("BASE_ARMOR_AMOUNT","Arm.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Armor) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return ""+I.basePhyStats().armor();
				}
			},
			new AbilityParmEditorImpl("BOARDABLE_POP","Pop.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Boardable) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "100";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Boardable)
						return ""+((Boardable)I).getArea().numberOfProperIDedRooms()*10;
					return "100";
				}
			},
			new AbilityParmEditorImpl("CONTAINER_TYPE","Con.",ParmType.MULTICHOICES)
			{
				@Override
				public void createChoices()
				{
					createBinaryChoices(Container.CONTAIN_DESCS);
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Container) ? 1 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "0";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof Container))
						return "";
					final Container C=(Container)I;
					final StringBuilder str=new StringBuilder("");
					for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
					{
						if(CMath.isSet(C.containTypes(), i-1))
						{
							if(str.length()>0)
								str.append("|");
							str.append(Container.CONTAIN_DESCS[i]);
						}
					}
					return str.toString();
				}
			},
			new AbilityParmEditorImpl("CONTAINER_TYPE_OR_LIDLOCK","Con.",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
					super.choices = new PairVector<String,String>();
					for(final String s : Container.CONTAIN_DESCS)
						choices().add(s.toUpperCase().trim(),s);
					choices().add("LID","Lid");
					choices().add("LOCK","Lid+Lock");
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Container) ? 1 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					final StringBuilder str=new StringBuilder("");
					if(I instanceof Container)
					{
						final Container C=(Container)I;
						if(C.hasALock())
							str.append("LOCK");
						if(str.length()>0)
							str.append("|");
						if(C.hasADoor())
							str.append("LID");
						if(str.length()>0)
							str.append("|");
						for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
						{
							if(CMath.isSet(C.containTypes(), i-1))
							{
								if(str.length()>0)
									str.append("|");
								str.append(Container.CONTAIN_DESCS[i]);
							}
						}
					}
					return str.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return new String[]{"NULL"};
					return CMParms.parseAny(oldVal,'|',true).toArray(new String[0]);
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String webValue = httpReq.getUrlParameter(fieldName);
					if(webValue == null)
						return oldVal;
					String id="";
					int index=0;
					final StringBuilder str=new StringBuilder("");
					for(;httpReq.isUrlParameter(fieldName+id);id=""+(++index))
					{
						final String newVal = httpReq.getUrlParameter(fieldName+id);
						if((newVal!=null)&&(newVal.length()>0)&&(choices().containsFirst(newVal)))
							str.append(newVal).append("|");
					}
					return str.toString();
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag)
				throws java.io.IOException
				{
					return CMLib.genEd().promptMultiSelectList(mob,oldVal,"|",++showNumber[0],showFlag,prompt(),choices(),false);
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					final List<String> webVals=CMParms.parseAny(oldVal.toUpperCase().trim(), "|", true);
					for(final String s : webVals)
					{
						if(!choices().containsFirst(s))
							return false;
					}
					return true;
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String webValue = webValue(httpReq,parms,oldVal,fieldName);
					final List<String> webVals=CMParms.parseAny(webValue.toUpperCase().trim(), "|", true);
					String onChange = null;
					onChange = " MULTIPLE ";
					if(!parms.containsKey("NOSELECT"))
						onChange+= "ONCHANGE=\"MultiSelect(this);\"";
					final StringBuilder str=new StringBuilder("");
					str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
					for(int i=0;i<choices().size();i++)
					{
						final String option = (choices().get(i).first);
						str.append("<OPTION VALUE=\""+option+"\" ");
						if(webVals.contains(option))
							str.append("SELECTED");
						str.append(">"+(choices().get(i).second));
					}
					return str.toString()+"</SELECT>";
				}
			},
			new AbilityParmEditorImpl("CONTAINER_TYPE_OR_LIDLOCK_OR_RIDEBASIS","Con./Ride",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
					super.choices = new PairVector<String,String>();
					for(final String s : Container.CONTAIN_DESCS)
						choices().add(s.toUpperCase().trim(),s);
					choices().add("LID","Lid");
					choices().add("LOCK","Lid+Lock");
					for(final String s : new String[] { "", "CHAIR", "TABLE", "LADDER", "ENTER", "BED" })
						choices().add(s, CMStrings.capitalizeAndLower(s));
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return ((o instanceof Container) || (o instanceof Rideable)) ? 1 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					final StringBuilder str=new StringBuilder("");
					if(I instanceof Container)
					{
						final Container C=(Container)I;
						if(C.hasALock())
							str.append("LOCK|");
						if(C.hasADoor())
							str.append("LID|");
						for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
						{
							if(CMath.isSet(C.containTypes(), i-1))
								str.append(Container.CONTAIN_DESCS[i]).append("|");
						}
					}
					if(I instanceof Rideable)
					{
						switch(((Rideable)I).rideBasis())
						{
						case FURNITURE_SIT:
							str.append("SIT|");
							break;
						case FURNITURE_TABLE:
							str.append("TABLE|");
							break;
						case LADDER:
							str.append("LADDER|");
							break;
						case ENTER_IN:
							str.append("ENTER|");
							break;
						case FURNITURE_SLEEP:
							str.append("BED|");
							break;
						case FURNITURE_HOOK:
							str.append("HOOK|");
							break;
						case AIR_FLYING:
						case LAND_BASED:
						case WAGON:
						case WATER_BASED:
							// not supported
							break;
						}
					}
					return str.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return new String[]{""};
					return CMParms.parseAny(oldVal,'|',true).toArray(new String[0]);
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String webValue = httpReq.getUrlParameter(fieldName);
					if(webValue == null)
						return oldVal;
					String id="";
					int index=0;
					final StringBuilder str=new StringBuilder("");
					for(;httpReq.isUrlParameter(fieldName+id);id=""+(++index))
					{
						final String newVal = httpReq.getUrlParameter(fieldName+id);
						if((newVal!=null)&&(newVal.length()>0)&&(choices().containsFirst(newVal)))
							str.append(newVal).append("|");
					}
					return str.toString();
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag)
				throws java.io.IOException
				{
					return CMLib.genEd().promptMultiSelectList(mob,oldVal,"|",++showNumber[0],showFlag,prompt(),choices(),false);
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					final List<String> webVals=CMParms.parseAny(oldVal.toUpperCase().trim(), "|", true);
					for(final String s : webVals)
					{
						if(!choices().containsFirst(s))
							return false;
					}
					return true;
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String webValue = webValue(httpReq,parms,oldVal,fieldName);
					final List<String> webVals=CMParms.parseAny(webValue.toUpperCase().trim(), "|", true);
					String onChange = null;
					onChange = " MULTIPLE ";
					if(!parms.containsKey("NOSELECT"))
						onChange+= "ONCHANGE=\"MultiSelect(this);\"";
					final StringBuilder str=new StringBuilder("");
					str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
					for(int i=0;i<choices().size();i++)
					{
						final String option = (choices().get(i).first);
						str.append("<OPTION VALUE=\""+option+"\" ");
						if(webVals.contains(option))
							str.append("SELECTED");
						str.append(">"+(choices().get(i).second));
					}
					return str.toString()+"</SELECT>";
				}
			},
			new AbilityParmEditorImpl("CODED_SPELL_LIST","Spell Affects",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int maxColWidth()
				{
					return 20;
				}

				@Override
				public boolean confirmValue(String oldVal)
				{
					if(oldVal.length()==0)
						return true;
					if(oldVal.charAt(0)=='*')
						oldVal = oldVal.substring(1);
					final int x=oldVal.indexOf('(');
					int y=oldVal.indexOf(';');
					if((x<y)&&(x>0))
						y=x;
					if(y<0)
						return CMClass.getAbility(oldVal)!=null;
					return CMClass.getAbility(oldVal.substring(0,y))!=null;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return CMLib.coffeeMaker().getCodedSpellsOrBehaviors(I);
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				public String rebuild(final List<CMObject> spells) throws CMException
				{
					final StringBuffer newVal = new StringBuffer("");
					if(spells.size()==1)
					{
						newVal.append("*" + spells.get(0).ID() + ";");
						if(spells.get(0) instanceof Ability)
							newVal.append(((Ability)spells.get(0)).text());
						else
						if(spells.get(0) instanceof Behavior)
							newVal.append(((Behavior)spells.get(0)).getParms());
					}
					else
					{
						if(spells.size()>1)
						{
							for(int s=0;s<spells.size();s++)
							{
								final String txt;
								if(spells.get(s) instanceof Ability)
									txt=((Ability)spells.get(s)).text();
								else
								if(spells.get(s) instanceof Behavior)
									txt=((Behavior)spells.get(s)).getParms();
								else
									txt="";
								if(txt.length()>0)
								{
									if((txt.indexOf(';')>=0)
									||(CMClass.getAbility(txt.trim())!=null)
									||(CMClass.getBehavior(txt.trim())!=null))
										throw new CMException("You may not have more than one spell when one of the spells parameters is a spell id or a ; character.");
								}
								newVal.append(spells.get(s).ID());
								if(txt.length()>0)
									newVal.append(";" + txt);
								if(s<(spells.size()-1))
									newVal.append(";");
							}
						}
					}
					return newVal.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					final Vector<String> V = new Vector<String>();
					final Vector<String> V2 = new Vector<String>();
					final List<CMObject> spells=CMLib.coffeeMaker().getCodedSpellsOrBehaviors(oldVal);
					for(int s=0;s<spells.size();s++)
					{
						final CMObject O = spells.get(s);
						V.addElement(O.ID());
						V2.addElement(O.ID());
						if(O instanceof Ability)
							V2.addElement(((Ability)O).text());
						else
						if(O instanceof Behavior)
							V2.addElement(((Behavior)O).getParms());
						else
							V2.add("");
					}
					V.addAll(V2);
					V.addElement("");
					return CMParms.toStringArray(V);
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					List<CMObject> spells=null;
					if(httpReq.isUrlParameter(fieldName+"_AFFECT1"))
					{
						spells = new Vector<CMObject>();
						int num=1;
						String behav=httpReq.getUrlParameter(fieldName+"_AFFECT"+num);
						String theparm=httpReq.getUrlParameter(fieldName+"_ADATA"+num);
						while((behav!=null)&&(theparm!=null))
						{
							if(behav.length()>0)
							{
								final Ability A=CMClass.getAbility(behav);
								if(A!=null)
								{
									if(theparm.trim().length()>0)
										A.setMiscText(theparm);
									spells.add(A);
								}
								else
								{
									final Behavior B=CMClass.getBehavior(behav);
									if(B!=null)
									{
										if(theparm.trim().length()>0)
											B.setParms(theparm);
										spells.add(B);
									}
								}
							}
							num++;
							behav=httpReq.getUrlParameter(fieldName+"_AFFECT"+num);
							theparm=httpReq.getUrlParameter(fieldName+"_ADATA"+num);
						}
					}
					else
						spells = CMLib.coffeeMaker().getCodedSpellsOrBehaviors(oldVal);
					try
					{
						return rebuild(spells);
					}
					catch(final Exception e)
					{
						return oldVal;
					}
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final List<CMObject> spells=CMLib.coffeeMaker().getCodedSpellsOrBehaviors(webValue(httpReq,parms,oldVal,fieldName));
					final StringBuffer str = new StringBuffer("");
					str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
					for(int i=0;i<spells.size();i++)
					{
						final CMObject A=spells.get(i);
						str.append("<TR><TD WIDTH=50%>");
						str.append("\n\r<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+fieldName+"_AFFECT"+(i+1)+">");
						str.append("<OPTION VALUE=\"\">Delete!");
						str.append("<OPTION VALUE=\""+A.ID()+"\" SELECTED>"+A.ID());
						str.append("</SELECT>");
						str.append("</TD><TD WIDTH=50%>");
						final String parmstr=(A instanceof Ability)?((Ability)A).text():((Behavior)A).getParms();
						final String theparm=CMStrings.replaceAll(parmstr,"\"","&quot;");
						str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
						str.append("</TD></TR>");
					}
					str.append("<TR><TD WIDTH=50%>");
					str.append("\n\r<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+fieldName+"_AFFECT"+(spells.size()+1)+">");
					str.append("<OPTION SELECTED VALUE=\"\">Select an Effect/Behav");
					for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
							continue;
						final String cnam=A.ID();
						str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
					}
					for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
					{
						final Behavior B=b.nextElement();
						final String cnam=B.ID();
						str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
					}
					str.append("</SELECT>");
					str.append("</TD><TD WIDTH=50%>");
					str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(spells.size()+1)+" VALUE=\"\">");
					str.append("</TD></TR>");
					str.append("</TABLE>");
					return str.toString();
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					final List<CMObject> spells=CMLib.coffeeMaker().getCodedSpellsOrBehaviors(oldVal);
					final StringBuffer rawCheck = new StringBuffer("");
					for(int s=0;s<spells.size();s++)
					{
						rawCheck.append(spells.get(s).ID()).append(";");
						if(spells.get(s) instanceof Ability)
							rawCheck.append(((Ability)spells.get(s)).text()).append(";");
						else
						if(spells.get(s) instanceof Behavior)
							rawCheck.append(((Behavior)spells.get(s)).getParms()).append(";");
					}
					boolean okToProceed = true;
					++showNumber[0];
					String newVal = null;
					while(okToProceed)
					{
						okToProceed = false;
						CMLib.genEd().spellsOrBehavs(mob,spells,showNumber[0],showFlag,true);
						final StringBuffer sameCheck = new StringBuffer("");
						for(int s=0;s<spells.size();s++)
						{
							sameCheck.append(spells.get(s).ID()).append(';');
							if(spells.get(s) instanceof Ability)
								sameCheck.append(((Ability)spells.get(s)).text()).append(";");
							else
							if(spells.get(s) instanceof Behavior)
								sameCheck.append(((Behavior)spells.get(s)).getParms()).append(";");
						}
						if(sameCheck.toString().equals(rawCheck.toString()))
							return oldVal;
						try
						{
							newVal = rebuild(spells);
						}
						catch(final CMException e)
						{
							mob.tell(e.getMessage());
							okToProceed = true;
							break;
						}
					}
					return (newVal==null)?oldVal:newVal.toString();
				}
			},
			new AbilityParmEditorImpl("BUILDING_FLAGS","Flags",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					final Pair<String[],String[]> codesFlags = CMAbleParms.getBuildingCodesNFlags();
					final String[] names = CMParms.parseSpaces(oldVal, true).toArray(new String[0]);
					for(final String name : names)
					{
						if(!CMParms.containsIgnoreCase(codesFlags.second, name))
							return false;
					}
					return true;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					return CMParms.parseSpaces(oldVal, true).toArray(new String[0]);
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String webValue = httpReq.getUrlParameter(fieldName);
					if(webValue == null)
						return oldVal;
					final StringBuilder s=new StringBuilder("");
					String id="";
					int index=0;
					final Pair<String[],String[]> codesFlags = CMAbleParms.getBuildingCodesNFlags();
					for(;httpReq.isUrlParameter(fieldName+id);id=""+(++index))
					{
						final String newVal = httpReq.getUrlParameter(fieldName+id);
						if(CMParms.containsIgnoreCase(codesFlags.second, newVal.toUpperCase().trim()))
							s.append(" ").append(newVal.toUpperCase().trim());
					}
					return s.toString().trim();
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final StringBuffer str = new StringBuffer("");
					final String webValue = webValue(httpReq,parms,oldVal,fieldName);
					String onChange = null;
					onChange = " MULTIPLE ";
					if(!parms.containsKey("NOSELECT"))
						onChange+= "ONCHANGE=\"MultiSelect(this);\"";
					final Pair<String[],String[]> codesFlags = CMAbleParms.getBuildingCodesNFlags();
					final String[] fakeValues = this.fakeUserInput(webValue);
					str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
					for(int i=0;i<codesFlags.second.length;i++)
					{
						final String option = (codesFlags.second[i]);
						str.append("<OPTION VALUE=\""+option+"\" ");
						if(CMParms.containsIgnoreCase(fakeValues, option))
							str.append("SELECTED");
						str.append(">"+option);
					}
					return str.toString()+"</SELECT>";
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					final Pair<String[],String[]> codesFlags = CMAbleParms.getBuildingCodesNFlags();
					final String help=CMParms.combineWith(Arrays.asList(codesFlags.second), ',');
					final String newVal = CMLib.genEd().prompt(mob, oldVal, ++showNumber[0], showFlag, L("Flags"), true, help);
					String[] newVals;
					if(newVal.indexOf(',')>0)
						newVals = CMParms.parseCommas(newVal.toUpperCase().trim(), true).toArray(new String[0]);
					else
					if(newVal.indexOf(';')>0)
						newVals = CMParms.parseSemicolons(newVal.toUpperCase().trim(), true).toArray(new String[0]);
					else
						newVals = CMParms.parse(newVal.toUpperCase().trim()).toArray(new String[0]);
					final StringBuilder finalVal = new StringBuilder("");
					for(int i=0;i<newVals.length;i++)
					{
						if(CMParms.containsIgnoreCase(codesFlags.second, newVals[i]))
							finalVal.append(" ").append(newVals[i]);
					}
					return finalVal.toString().toUpperCase().trim();
				}
			},
			new AbilityParmEditorImpl("EXIT_NAMES","Exit Words",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equalsIgnoreCase("DOOR"))
							return 1;
					}
					return -1;
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					final String[] names = CMParms.parseAny(oldVal.trim(), '|', true).toArray(new String[0]);
					if(names.length > 6)
						return false;
					return true;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "door|open|close|A closed door.|An open doorway.|";
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{

					final Vector<String> V = new Vector<String>();
					V.addAll(CMParms.parseAny(oldVal.trim(), '|', true));
					while(V.size()<6)
						V.add("");
					return CMParms.toStringArray(V);
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					if(httpReq.isUrlParameter(fieldName+"_W1"))
					{
						final StringBuilder str=new StringBuilder("");
						str.append(httpReq.getUrlParameter(fieldName+"_W1")).append("|");
						str.append(httpReq.getUrlParameter(fieldName+"_W2")).append("|");
						str.append(httpReq.getUrlParameter(fieldName+"_W3")).append("|");
						str.append(httpReq.getUrlParameter(fieldName+"_W4")).append("|");
						str.append(httpReq.getUrlParameter(fieldName+"_W5")).append("|");
						str.append(httpReq.getUrlParameter(fieldName+"_W6"));
						String s=str.toString();
						while(s.endsWith("|"))
							s=s.substring(0,s.length()-1);
						return s;
					}
					else
					{
						return oldVal;
					}
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final StringBuffer str = new StringBuffer("");
					str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
					final String[] vals = this.fakeUserInput(oldVal);
					final String[] keys = new String[]{"Noun","Open","Close","Closed Display","Open Display","Description"};
					for(int i=0;i<keys.length;i++)
					{
						str.append("<TR><TD WIDTH=30%><FONT COLOR=WHITE>"+L(keys[i])+"</FONT></TD>");
						str.append("<TD><INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_W"+(i+1)+" VALUE=\""+vals[i]+"\">");
						str.append("</TD></TR>");
					}
					str.append("</TABLE>");
					return str.toString();
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					final String[] vals = this.fakeUserInput(oldVal);
					final StringBuilder newVal = new StringBuilder("");
					newVal.append(CMLib.genEd().prompt(mob, vals[0], ++showNumber[0], showFlag, L("Exit Noun"), true)).append("|");
					newVal.append(CMLib.genEd().prompt(mob, vals[1], ++showNumber[0], showFlag, L("Open Verb"), true)).append("|");
					newVal.append(CMLib.genEd().prompt(mob, vals[2], ++showNumber[0], showFlag, L("Close Verb"), true)).append("|");
					newVal.append(CMLib.genEd().prompt(mob, vals[3], ++showNumber[0], showFlag, L("Opened Text"), true)).append("|");
					newVal.append(CMLib.genEd().prompt(mob, vals[4], ++showNumber[0], showFlag, L("Closed Text"), true)).append("|");
					newVal.append(CMLib.genEd().prompt(mob, vals[5], ++showNumber[0], showFlag, L("Description"), true));
					String s=newVal.toString();
					while(s.endsWith("|"))
						s=s.substring(0,s.length()-1);
					return s;
				}
			},
			new AbilityParmEditorImpl("PCODED_SPELL_LIST","Spell Affects",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int maxColWidth()
				{
					return 20;
				}

				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof String)
					{
						final String chk=((String)o).toUpperCase();
						if(chk.equalsIgnoreCase("WALL")
						||chk.equalsIgnoreCase("DEMOLISH")
						||chk.equalsIgnoreCase("TITLE")
						||chk.equalsIgnoreCase("DESC"))
							return -1;
						final Pair<String[],String[]> codeFlags = CMAbleParms.getBuildingCodesNFlags();
						if(CMParms.contains(codeFlags.first, chk))
							return 1;
					}
					return -1;
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					final String[] spells = CMParms.parseAny(oldVal.trim(), ')', true).toArray(new String[0]);
					for(String spell : spells)
					{
						final int x=spell.indexOf('(');
						if(x>0)
							spell=spell.substring(0,x);
						if(spell.trim().length()==0)
							continue;
						if((CMClass.getAbility(spell)==null)&&(CMClass.getBehavior(spell)==null))
							return false;
					}
					return true;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				public String rebuild(final List<CMObject> spells) throws CMException
				{
					final StringBuffer newVal = new StringBuffer("");
					for(int s=0;s<spells.size();s++)
					{
						final String txt;
						if(spells.get(s) instanceof Ability)
							txt = ((Ability)spells.get(s)).text().trim();
						else
						if(spells.get(s) instanceof Behavior)
							txt = ((Behavior)spells.get(s)).getParms().trim();
						else
							continue;
						newVal.append(spells.get(s).ID()).append("(").append(txt).append(")");
					}
					return newVal.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					final Vector<String> V = new Vector<String>();
					final String[] spells = CMParms.parseAny(oldVal.trim(), ')', true).toArray(new String[0]);
					for(String spell : spells)
					{
						final int x=spell.indexOf('(');
						String parms="";
						if(x>0)
						{
							parms=spell.substring(x+1).trim();
							spell=spell.substring(0,x);
						}
						if(spell.trim().length()==0)
							continue;
						if((CMClass.getAbility(spell)!=null)
						||(CMClass.getBehavior(spell)!=null))
						{
							V.add(spell);
							V.add(parms);
						}
					}
					return CMParms.toStringArray(V);
				}

				public List<CMObject> getCodedSpells(final String oldVal)
				{
					final String[] spellStrs = this.fakeUserInput(oldVal);
					final List<CMObject> spells=new ArrayList<CMObject>(spellStrs.length/2);
					for(int s=0;s<spellStrs.length;s+=2)
					{
						final Ability A=CMClass.getAbility(spellStrs[s]);
						if(A!=null)
						{
							if(spellStrs[s+1].length()>0)
								A.setMiscText(spellStrs[s+1]);
							spells.add(A);
						}
						else
						{
							final Behavior B=CMClass.getBehavior(spellStrs[s]);
							if(spellStrs[s+1].length()>0)
								B.setParms(spellStrs[s+1]);
							spells.add(B);
						}
					}
					return spells;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					List<CMObject> spells=null;
					if(httpReq.isUrlParameter(fieldName+"_AFFECT1"))
					{
						spells = new Vector<CMObject>();
						int num=1;
						String behav=httpReq.getUrlParameter(fieldName+"_AFFECT"+num);
						String theparm=httpReq.getUrlParameter(fieldName+"_ADATA"+num);
						while((behav!=null)&&(theparm!=null))
						{
							if(behav.length()>0)
							{
								final Ability A=CMClass.getAbility(behav);
								if(A!=null)
								{
									if(theparm.trim().length()>0)
										A.setMiscText(theparm);
									spells.add(A);
								}
								else
								{
									final Behavior B=CMClass.getBehavior(behav);
									if(theparm.trim().length()>0)
										B.setParms(theparm);
									spells.add(B);
								}
							}
							num++;
							behav=httpReq.getUrlParameter(fieldName+"_AFFECT"+num);
							theparm=httpReq.getUrlParameter(fieldName+"_ADATA"+num);
						}
					}
					else
					{
						spells = getCodedSpells(oldVal);
					}
					try
					{
						return rebuild(spells);
					}
					catch(final Exception e)
					{
						return oldVal;
					}
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final List<CMObject> spells=getCodedSpells(webValue(httpReq,parms,oldVal,fieldName));
					final StringBuffer str = new StringBuffer("");
					str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
					for(int i=0;i<spells.size();i++)
					{
						final CMObject A=spells.get(i);
						str.append("<TR><TD WIDTH=50%>");
						str.append("\n\r<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+fieldName+"_AFFECT"+(i+1)+">");
						str.append("<OPTION VALUE=\"\">Delete!");
						str.append("<OPTION VALUE=\""+A.ID()+"\" SELECTED>"+A.ID());
						str.append("</SELECT>");
						str.append("</TD><TD WIDTH=50%>");
						final String theparm;
						if(A instanceof Ability)
							theparm=CMStrings.replaceAll(((Ability)A).text(),"\"","&quot;");
						else
						if(A instanceof Behavior)
							theparm=CMStrings.replaceAll(((Behavior)A).getParms(),"\"","&quot;");
						else
							continue;
						str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
						str.append("</TD></TR>");
					}
					str.append("<TR><TD WIDTH=50%>");
					str.append("\n\r<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+fieldName+"_AFFECT"+(spells.size()+1)+">");
					str.append("<OPTION SELECTED VALUE=\"\">Select Effect/Behavior");
					for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
							continue;
						final String cnam=A.ID();
						str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
					}
					for(final Enumeration<Behavior> a=CMClass.behaviors();a.hasMoreElements();)
					{
						final Behavior A=a.nextElement();
						final String cnam=A.ID();
						str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
					}
					str.append("</SELECT>");
					str.append("</TD><TD WIDTH=50%>");
					str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(spells.size()+1)+" VALUE=\"\">");
					str.append("</TD></TR>");
					str.append("</TABLE>");
					return str.toString();
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					final List<CMObject> spells=getCodedSpells(oldVal);
					final StringBuffer rawCheck = new StringBuffer("");
					for(int s=0;s<spells.size();s++)
					{
						rawCheck.append(spells.get(s).ID()).append(";");
						if(spells.get(s) instanceof Ability)
							rawCheck.append(((Ability)spells.get(s)).text()).append(";");
						else
						if(spells.get(s) instanceof Behavior)
							rawCheck.append(((Behavior)spells.get(s)).getParms()).append(";");
						else
							rawCheck.append(";");
					}
					boolean okToProceed = true;
					++showNumber[0];
					String newVal = null;
					while(okToProceed)
					{
						okToProceed = false;
						CMLib.genEd().spellsOrBehaviors(mob,spells,showNumber[0],showFlag,true);
						final StringBuffer sameCheck = new StringBuffer("");
						for(int s=0;s<spells.size();s++)
						{
							if(spells.get(s) instanceof Ability)
								rawCheck.append(((Ability)spells.get(s)).text()).append(";");
							else
							if(spells.get(s) instanceof Behavior)
								rawCheck.append(((Behavior)spells.get(s)).getParms()).append(";");
							else
								rawCheck.append(";");
						}
						if(sameCheck.toString().equals(rawCheck.toString()))
							return oldVal;
						try
						{
							newVal = rebuild(spells);
						}
						catch(final CMException e)
						{
							mob.tell(e.getMessage());
							okToProceed = true;
							break;
						}
					}
					return (newVal==null)?oldVal:newVal.toString();
				}
			},
			new AbilityParmEditorImpl("ABILITY_ID","Ability ID",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int maxColWidth()
				{
					return 20;
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return 1;
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return false;
					final Ability A=CMClass.getAbility(oldVal);
					return (A instanceof Trap);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					if(confirmValue(oldVal))
						return new String[] {oldVal};
					return new String[] {defaultValue()};
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					if(confirmValue(httpReq.getUrlParameter(fieldName)))
						return httpReq.getUrlParameter(fieldName);
					return oldVal;
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final StringBuffer str = new StringBuffer("");
					final String val = webValue(httpReq,parms,oldVal,fieldName);
					str.append("\n\r<SELECT NAME="+fieldName+">");
					str.append("<OPTION SELECTED VALUE=\"\">Select Effect/Behavior");
					for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_TRAP)
						&&(A instanceof Trap)
						&&(((Trap)A).maySetTrap(null, Integer.MAX_VALUE)))
						{
							str.append("<OPTION VALUE=\""+A.ID()+"\"");
							if(A.ID().equalsIgnoreCase(val))
								str.append(" SELECTED");
							str.append(">"+A.ID());
						}
					}
					str.append("</SELECT>");
					return str.toString();
				}

				@Override
				public String commandLinePrompt(final MOB mob, String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					++showNumber[0];
					if((showFlag>0)&&(showFlag!=showNumber[0]))
						return oldVal;
					String behave="NO";
					while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
					{
						mob.tell(L("@x1. Trap ID: '@x2'.",""+showNumber,oldVal));
						if((showFlag!=showNumber[0])&&(showFlag>-999))
							return oldVal;
						behave=mob.session().prompt(L("Enter trap ability ID (?):"),"");
						if(behave.length()>0)
						{
							if(behave.equalsIgnoreCase("?"))
							{
								mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(new Filterer<Ability>() {
									@Override
									public boolean passesFilter(final Ability obj)
									{
										if(((obj.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_TRAP)
										&&(obj instanceof Trap)
										&&(((Trap)obj).maySetTrap(null, Integer.MAX_VALUE)))
											return true;
										return false;
									}
								})).toString());
							}
							else
							{
								Ability chosenOne=CMClass.getAbility(behave);
								if((chosenOne instanceof Ability)
								&&((chosenOne.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
								&&(!CMSecurity.isASysOp(mob)))
									chosenOne=null;
								if((chosenOne!=null)
								&&((chosenOne.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_TRAP)
								&&(chosenOne instanceof Trap))
								{
									oldVal = chosenOne.ID();
									behave="";
								}
								else
									mob.tell(L("'@x1' is not a recognized trap.  Try '?'.",behave));
							}
						}
						else
							mob.tell(L("(no change)"));
					}
					return oldVal;
				}
			},
			new AbilityParmEditorImpl("TRIGGER_MSG","Trigger Msg",ParmType.STRINGORNULL)
			{

				@Override
				public String defaultValue()
				{
					return "";
				}
				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
				@Override
				public void createChoices()
				{
				}
			},
			new AbilityParmEditorImpl("DAMAGE_MSG","Damage Msg",ParmType.STRINGORNULL)
			{
				@Override
				public String defaultValue()
				{
					return "";
				}
				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
				@Override
				public void createChoices()
				{
				}
			},
			new AbilityParmEditorImpl("AVOID_MSG","Avoid Msg",ParmType.STRINGORNULL)
			{
				@Override
				public String defaultValue()
				{
					return "";
				}
				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
				@Override
				public void createChoices()
				{
				}
			},
			new AbilityParmEditorImpl("ABILITY_ID","Ability ID",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(CMClass.abilities(new Filterer<Ability>() {

						@Override
						public boolean passesFilter(final Ability obj)
						{
							return ((obj.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON);
						}
					}));
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public int maxColWidth()
				{
					return 20;
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return 1;
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					final Ability A=CMClass.getAbility(oldVal);
					if(A==null)
						return false;
					return ((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON);
				}
			},
			new AbilityParmEditorImpl("TRAP_ID","Trap ID",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(CMClass.abilities(new Filterer<Ability>() {

						@Override
						public boolean passesFilter(final Ability obj)
						{
							return (obj instanceof Trap)
								&&((obj.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_TRAP)
								&&(((Trap)obj).maySetTrap(null, -1));
						}
					}));
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public int maxColWidth()
				{
					return 20;
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return 1;
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return false;
					final Ability A=CMClass.getAbility(oldVal);
					return (A instanceof Trap);
				}
			},
			new AbilityParmEditorImpl("BASE_DAMAGE","Dmg.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Weapon) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Weapon)
						return ""+((Weapon)I).basePhyStats().damage();
					return "0";
				}
			},
			new AbilityParmEditorImpl("DECORATION_FLAG","Flag",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return 1;
				}

				@Override
				public void createChoices()
				{
					createChoices(new String[] { "", "BLENDED", "HIPS", "BLENDED+HIPS" });
				}

				@Override
				public String defaultValue()
				{
					return "BLENDED";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("LID_LOCK","Lid.",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Container) ? 1 : -1;
				}

				@Override
				public void createChoices()
				{
					super.choices = new PairVector<String,String>();
					choices().add("","");
					choices().add("LID","Lid");
					choices().add("LOCK","Lid+Lock");
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof Container))
						return "";
					final Container C=(Container)I;
					if(C.hasALock())
						return "LOCK";
					if(C.hasADoor())
						return "LID";
					return "";
				}
			},
			new AbilityParmEditorImpl("BUILDING_CODE","Code",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return 1;
				}

				@Override
				public void createChoices()
				{
					createChoices(getBuildingCodesNFlags().first);
				}

				@Override
				public String defaultValue()
				{
					return "TITLE";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "TITLE";
				}
			},
			new AbilityParmEditorImpl("STATUE","Statue",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return ((!(o instanceof Armor)) && (!(o instanceof Container)) && (!(o instanceof Drink))) ? 1 : -1;
				}

				@Override
				public void createChoices()
				{
					createChoices(new String[] { "", "STATUE" });
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Weapon)
						return "";
					if(I instanceof Armor)
						return "";
					if(I instanceof Ammunition)
						return "";
					final int x=I.Name().lastIndexOf(" of ");
					if(x<0)
						return "";
					final String ender=I.Name();
					if(!I.displayText().endsWith(ender+" is here"))
						return "";
					if(!I.description().startsWith(ender+". "))
						return "";
					return "STATUE";
				}
			},
			new AbilityParmEditorImpl("RIDE_BASIS","Ride",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Rideable) ? 3 : -1;
				}

				@Override
				public void createChoices()
				{
					createChoices(new String[] { "", "CHAIR", "TABLE", "LADDER", "ENTER", "BED" });
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof Rideable))
						return "";
					switch(((Rideable)I).rideBasis())
					{
					case FURNITURE_SIT:
						return "SIT";
					case FURNITURE_TABLE:
						return "TABLE";
					case FURNITURE_HOOK:
						return "HOOK";
					case LADDER:
						return "LADDER";
					case ENTER_IN:
						return "ENTER";
					case FURNITURE_SLEEP:
						return "BED";
					default:
						return "";
					}
				}
			},
			new AbilityParmEditorImpl("LIQUID_CAPACITY","Liq.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Drink) ? 4 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "25";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof Drink))
						return "";
					return ""+((Drink)I).liquidHeld();
				}
			},
			new AbilityParmEditorImpl("MAX_WAND_USES","Max.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Wand) ? 5 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "25";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof Wand))
						return "";
					return ""+((Wand)I).getMaxCharges();
				}
			},
			new AbilityParmEditorImpl("WAND_TYPE","MagicT",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Wand) ? 2 : -1;
				}

				@Override
				public int minColWidth()
				{
					return 3;
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal==null)
						return false;
					if(oldVal.length()==0)
						return true;
					return super.confirmValue(oldVal);
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					if((oldVal==null) || (oldVal.length()==0))
						return new String[] { "ANY" };
					return new String[] { oldVal };
				}

				@Override
				public void createChoices()
				{
					choices = new PairVector<String,String>();
					choices.add("ANY", "Any");
					for(final String[] set : Wand.WandUsage.WAND_OPTIONS)
						choices.add(set[0], CMStrings.capitalizeAllFirstLettersAndLower(set[1]));
				}

				@Override
				public String defaultValue()
				{
					return "ANY";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Wand)
					{
						final int ofType=((Wand)I).getEnchantType();
						if((ofType<0)||(ofType>Ability.ACODE.DESCS_.size()))
							return "ANY";
						return Ability.ACODE.DESCS_.get(ofType);
					}
					return "ANY";
				}
			},
			new AbilityParmEditorImpl("DICE_SIDES","Dice.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof CMObject)
					{
						if(((CMObject)o).ID().endsWith("Dice"))
							return 1;
					}
					return -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "6";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return ""+I.basePhyStats().ability();
				}
			},
			new AbilityParmEditorImpl("WEAPON_CLASS","WClas",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Weapon) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
					createChoices(Weapon.CLASS_DESCS);
				}

				@Override
				public String defaultValue()
				{
					return "BLUNT";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Weapon)
						return Weapon.CLASS_DESCS[((Weapon)I).weaponClassification()];
					return "0";
				}
			},
			new AbilityParmEditorImpl("SMOKE_FLAG","Smoke",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Light) ? 5 : -1;
				}

				@Override
				public void createChoices()
				{
					createChoices(new String[] { "", "SMOKE" });
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof Light))
						return "";
					if((I instanceof Container)
					&&(((Light)I).getDuration() > 199)
					&&(((Container)I).capacity()==0))
						return "SMOKE";
					return "";
				}
			},
			new AbilityParmEditorImpl("WEAPON_HANDS_REQUIRED","Hand",ParmType.NUMBERORNULL)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Weapon) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Weapon)
						return ((Weapon)I).rawLogicalAnd()?"2":"1";
					return "";
				}
			},
			new AbilityParmEditorImpl("KEY_VALUE_PARMS","Keys",ParmType.STRINGORNULL)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return 1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					// how would I even start....
					return "";
				}
			},
			new AbilityParmEditorImpl("LIGHT_DURATION","Dur.",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Light) ? 5 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "10";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Light)
						return ""+((Light)I).getDuration();
					return "";
				}
			},
			new AbilityParmEditorImpl("CLAN_ITEM_CODENUMBER","Typ.",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof ClanItem) ? 10 : -1;
				}

				@Override
				public void createChoices()
				{
					createNumberedChoices(ClanItem.ClanItemType.ALL);
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof ClanItem)
						return ""+((ClanItem)I).getClanItemType().ordinal();
					return "";
				}
			},
			new AbilityParmEditorImpl("CLAN_EXPERIENCE_COST_AMOUNT","Exp",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "100";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof ClanItem))
						return "100";
					if(I.getClass().getName().toString().indexOf("Flag")>0)
						return "2500";
					if(I.getClass().getName().toString().indexOf("ClanItem")>0)
						return "1000";
					if(I.getClass().getName().toString().indexOf("GenClanSpecialItem")>0)
						return "500";
					return "100";
				}
			},
			new AbilityParmEditorImpl("CLAN_AREA_FLAG","Area",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return o.getClass().getName().toString().indexOf("LawBook") > 0 ? 5 : -1;
				}

				@Override
				public void createChoices()
				{
					createChoices(new String[] { "", "AREA" });
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return (I.getClass().getName().toString().indexOf("LawBook")>0)?"AREA":"";
				}
			},
			new AbilityParmEditorImpl("READABLE_TEXT","Read",ParmType.STRINGORNULL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(CMLib.flags().isReadable(I))
						return I.readableText();
					return "";
				}
			},
			new AbilityParmEditorImpl("REQUIRED_COMMON_SKILL_ID","Common Skill",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof ClanItem) ? 5 : -1;
				}

				@Override
				public void createChoices()
				{
					final Vector<Object> V  = new Vector<Object>();
					Ability A = null;
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						A=e.nextElement();
						if((A.classificationCode() & Ability.ALL_ACODES) == Ability.ACODE_COMMON_SKILL)
							V.addElement(A);
					}
					V.addElement("");
					createChoices(V);
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I.getClass().getName().toString().indexOf("LawBook")>0)
						return "";
					if(I instanceof ClanItem)
						return ((ClanItem)I).readableText();
					return "";
				}
			},
			new AbilityParmEditorImpl("FOOD_DRINK","ETyp",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(new String[]
					{
						"", "FOOD", "DRINK", "SOAP",
						"GenPerfume", "GenPowder", "GenCigar",
						"GenFoodResource", "GenLiquidResource", "GenResource"
					});
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					final String str=(I.name()+" "+I.displayText()+" "+I.description()).toUpperCase();
					if(str.startsWith("SOAP ") || str.endsWith(" SOAP") || (str.indexOf("SOAP")>0))
						return "SOAP";
					if(I instanceof Perfume)
						return "GenPerfume";
					if(I instanceof MagicDust)
						return "GenPowder";
					if((I instanceof Container)&&(I instanceof Light))
						return "GenCigar";
					if((I instanceof Food)&&(I instanceof RawMaterial))
						return "GenFoodResource";
					if(I instanceof Food)
						return "FOOD";
					if((I instanceof Drink)&&(I instanceof RawMaterial))
						return "GenLiquidResource";
					if(I instanceof Drink)
						return "DRINK";
					if(I instanceof RawMaterial)
						return "GenResource";
					return "";
				}
			},
			new AbilityParmEditorImpl("PAINTING_TYPE","PTyp",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(new String[]
					{
						"PAINTING"
					});
				}

				@Override
				public String defaultValue()
				{
					return "PAINTING";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "PAINTING";
				}
			},
			new AbilityParmEditorImpl("POTION_POWDER","P-type",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(new String[] { "", "POTION", "POWDER" });
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Potion)
						return "POTION";
					if(I instanceof MagicDust)
						return "POWDER";
					return "";
				}
			},
			new AbilityParmEditorImpl("SMELL_LIST","Smells",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Perfume) ? 5 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Perfume)
						return ((Perfume)I).getSmellList();
					return "";
				}
			},
			new AbilityParmEditorImpl("RESOURCE_OR_KEYWORD","Resc/Itm",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return true;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					if(httpReq.isUrlParameter(fieldName+"_WHICH"))
					{
						final String which=httpReq.getUrlParameter(fieldName+"_WHICH");
						if(which.trim().length()>0)
							return httpReq.getUrlParameter(fieldName+"_RESOURCE");
						return httpReq.getUrlParameter(fieldName+"_WORD");
					}
					return oldVal;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					String value=webValue(httpReq,parms,oldVal,fieldName);
					if(value.endsWith("$"))
						value = value.substring(0,oldVal.length()-1);
					value = value.trim();
					final StringBuffer str = new StringBuffer("");
					str.append("\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH ");
					final boolean rsc=(value.trim().length()==0)||(RawMaterial.CODES.FIND_IgnoreCase(value)>=0);
					if(rsc)
						str.append("CHECKED ");
					str.append("VALUE=\"RESOURCE\">");
					str.append("\n\r<SELECT NAME="+fieldName+"_RESOURCE>");
					final String[] Ss=RawMaterial.CODES.NAMES().clone();
					Arrays.sort(Ss);
					for(final String S : Ss)
					{
						final String VALUE = S.equals("NOTHING")?"":S;
						str.append("<OPTION VALUE=\""+VALUE+"\"");
						if(rsc&&(value.equalsIgnoreCase(VALUE)))
							str.append(" SELECTED");
						str.append(">"+CMStrings.capitalizeAndLower(S));
					}
					str.append("</SELECT>");
					str.append("<BR>");
					str.append("\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH ");
					if(!rsc)
						str.append("CHECKED ");
					str.append("VALUE=\"\">");
					str.append("\n\r<INPUT TYPE=TEXT NAME="+fieldName+"_WORD VALUE=\""+(rsc?"":value)+"\">");
					return str.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					return new String[] { oldVal };
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					++showNumber[0];
					boolean proceed = true;
					String str = oldVal;
					while(proceed&&(!mob.session().isStopped()))
					{
						proceed = false;
						str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,CMParms.toListString(RawMaterial.CODES.NAMES())).trim();
						if(str.equals(oldVal))
							return oldVal;
						final int r=RawMaterial.CODES.FIND_IgnoreCase(str);
						if(r==0)
							str="";
						else
						if(r>0)
							str=RawMaterial.CODES.NAME(r);
						if(str.equals(oldVal))
							return oldVal;
						if(str.length()==0)
							return "";
						final boolean isResource = CMParms.contains(RawMaterial.CODES.NAMES(),str);
						if((!isResource)&&(mob.session()!=null)&&(!mob.session().isStopped()))
							if(!mob.session().confirm(L("You`ve entered a non-resource item keyword '@x1', ok (Y/n)?",str),"Y"))
								proceed = true;
					}
					return str;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("RESOURCE_NAME_OR_HERB_NAME","Resrc/Herb",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					if(!oldVal.endsWith("$"))
					{
						return CMParms.contains(RawMaterial.CODES.NAMES(),oldVal);
					}
					return true;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					if(oldVal.endsWith("$"))
						return new String[]{oldVal.substring(0,oldVal.length()-1)};
					return new String[]{oldVal};
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, String oldVal, final String fieldName)
				{
					final AbilityParmEditor A = CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
					if(oldVal.endsWith("$"))
						oldVal = oldVal.substring(0,oldVal.length()-1);
					final String value = A.webValue(httpReq,parms,oldVal,fieldName);
					final int r=RawMaterial.CODES.FIND_IgnoreCase(value);
					if(r>=0)
						return RawMaterial.CODES.NAME(r);
					return (value.trim().length()==0)?"":(value+"$");
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final AbilityParmEditor A = CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
					return A.webField(httpReq,parms,oldVal,fieldName);
				}

				@Override
				public String webTableField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal)
				{
					if(oldVal.endsWith("$"))
						return oldVal.substring(0,oldVal.length()-1);
					return oldVal;
				}

				@Override
				public String commandLinePrompt(final MOB mob, String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					++showNumber[0];
					boolean proceed = true;
					String str = oldVal;
					final String orig = oldVal;
					while(proceed&&(!mob.session().isStopped()))
					{
						proceed = false;
						if(oldVal.trim().endsWith("$"))
							oldVal=oldVal.trim().substring(0,oldVal.trim().length()-1);
						str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,CMParms.toListString(RawMaterial.CODES.NAMES())).trim();
						if(str.equals(orig))
							return orig;
						final int r=RawMaterial.CODES.FIND_IgnoreCase(str);
						if(r==0)
							str="";
						else
						if(r>0)
							str=RawMaterial.CODES.NAME(r);
						if(str.equals(orig))
							return orig;
						if(str.length()==0)
							return "";
						final boolean isResource = CMParms.contains(RawMaterial.CODES.NAMES(),str);
						if((!isResource)&&(mob.session()!=null)&&(!mob.session().isStopped()))
						{
							if(!mob.session().confirm(L("You`ve entered a non-resource item keyword '@x1', ok (Y/n)?",str),"Y"))
								proceed = true;
							else
								str=str+"$";
						}
					}
					return str;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("AMMO_TYPE","Ammo",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return ((o instanceof AmmunitionWeapon) || (o instanceof Ammunition)) ? 2 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "arrows";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Ammunition)
						return ""+((Ammunition)I).ammunitionType();
					if((I instanceof AmmunitionWeapon)&&(((AmmunitionWeapon)I).requiresAmmunition()))
						return ""+((AmmunitionWeapon)I).ammunitionType();
					return "";
				}
			},
			new AbilityParmEditorImpl("AMMO_CAPACITY","Ammo#",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return ((o instanceof AmmunitionWeapon) || (o instanceof Ammunition)) ? 2 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "1";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Ammunition)
						return ""+((Ammunition)I).ammunitionRemaining();
					if((I instanceof AmmunitionWeapon)&&(((AmmunitionWeapon)I).requiresAmmunition()))
						return ""+((AmmunitionWeapon)I).rawAmmunitionCapacity();
					return "";
				}
			},
			new AbilityParmEditorImpl("MIN_MAX_RANGE","Rng",ParmType.NUMBER_PAIR)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return ((o instanceof Weapon) && (!(o instanceof Ammunition))) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "5";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Weapon)
						return ""+((Weapon)I).getRanges()[0]+","+((Weapon)I).getRanges()[1];
					else
					if(I instanceof Ammunition)
						return ""+I.minRange()+","+I.maxRange();
					return "";
				}
			},
			new AbilityParmEditorImpl("RESOURCE_OR_MATERIAL","Rsc/ Mat",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					final XVector<String> V=new XVector<String>(RawMaterial.CODES.NAMES());
					Collections.sort(V);
					final XVector<String> V2=new XVector<String>(RawMaterial.Material.names());
					Collections.sort(V2);
					V.addAll(V2);
					createChoices(V);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(CMStrings.containsWordIgnoreCase(I.Name(),"rice"))
						return "RICE";
					if(I.material() == RawMaterial.RESOURCE_PAPER)
						return "WOOD";
					return RawMaterial.CODES.NAME(I.material());
				}

				@Override
				public String defaultValue()
				{
					return "IRON";
				}
			},
			new AbilityParmEditorImpl("REQ_RESOURCE_OR_MATERIAL","Rsc/ Mat",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof RawMaterial)
						return 1;
					return -1;
				}

				@Override
				public void createChoices()
				{
					final XVector<String> V=new XVector<String>(RawMaterial.CODES.NAMES());
					Collections.sort(V);
					final XVector<String> V2=new XVector<String>(RawMaterial.Material.names());
					Collections.sort(V2);
					V.addAll(V2);
					createChoices(V);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(CMStrings.containsWordIgnoreCase(I.Name(),"rice"))
						return "RICE";
					if(I.material() == RawMaterial.RESOURCE_PAPER)
						return "WOOD";
					return RawMaterial.CODES.NAME(I.material());
				}

				@Override
				public String defaultValue()
				{
					return "IRON";
				}
			},
			new AbilityParmEditorImpl("OPTIONAL_RESOURCE_OR_MATERIAL","Rsc/ Mat",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					final XVector<String> V=new XVector<String>(RawMaterial.CODES.NAMES());
					Collections.sort(V);
					final XVector<String> V2=new XVector<String>(RawMaterial.Material.names());
					Collections.sort(V2);
					V.addAll(V2);
					V.addElement("");
					createChoices(V);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("OPTIONAL_RESOURCE_OR_MATERIAL_AMT","Rsc Amt",ParmType.NUMBER)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					if(o instanceof Wand)
						return 1;
					return ((o instanceof Weapon) && (!(o instanceof Ammunition))) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					return CMath.isInteger(oldVal);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I==null)
						return "";
					final List<String> words=CMParms.parse(I.name());
					for(int i=words.size()-1;i>=0;i--)
					{
						final String s=words.get(i);
						final int y=s.indexOf('-');
						if(y>=0)
						{
							words.add(s.substring(0, y));
							words.add(s.substring(0, y+1));
						}
					}
					for(final String word : words)
					{
						if(word.length()>0)
						{
							final int rsc=RawMaterial.CODES.FIND_IgnoreCase(word);
							if((rsc > 0)&&(rsc != I.material()))
							{
								if(I.basePhyStats().level()>80)
									return ""+4;

								if(I.basePhyStats().level()>40)
									return ""+2;

								return ""+1;
							}
						}
					}
					return "";
				}
			},
			new AbilityParmEditorImpl("OPTIONAL_BUILDING_RESOURCE_OR_MATERIAL","Rsc/ Mat",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					final XVector<String> V=new XVector<String>(RawMaterial.CODES.NAMES());
					Collections.sort(V);
					final XVector<String> V2=new XVector<String>(RawMaterial.Material.names());
					Collections.sort(V2);
					V.addAll(V2);
					V.addElement("VALUE");
					V.addElement("MONEY");
					V.addElement("");
					createChoices(V);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					final List<String> words=CMParms.parse(I.name());
					for(int i=words.size()-1;i>=0;i--)
					{
						final String s=words.get(i);
						final int y=s.indexOf('-');
						if(y>=0)
						{
							words.add(s.substring(0, y));
							words.add(s.substring(0, y+1));
						}
					}
					for(final String word : words)
					{
						if(word.length()>0)
						{
							final int rsc=RawMaterial.CODES.FIND_IgnoreCase(word);
							if((rsc > 0)&&(rsc != I.material()))
								return RawMaterial.CODES.NAME(rsc);
						}
					}
					return "";
				}

				@Override
				public String defaultValue()
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("HERB_NAME","Herb Final Name",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "Herb Name";
				}

				@Override
				public int minColWidth()
				{
					return 10;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I.material()==RawMaterial.RESOURCE_HERBS)
						return CMStrings.lastWordIn(I.Name());
					return "";
				}
			},
			new AbilityParmEditorImpl("MUSHROOM_NAME","Mushroom Final Name",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "Mushroom Name";
				}

				@Override
				public int minColWidth()
				{
					return 10;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I.material()==RawMaterial.RESOURCE_MUSHROOMS)
						return CMStrings.lastWordIn(I.Name());
					return "";
				}
			},
			new AbilityParmEditorImpl("FLOWER_NAME","Flower Final Name",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "Flower Name";
				}

				@Override
				public int minColWidth()
				{
					return 10;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I.material()==RawMaterial.RESOURCE_FLOWERS)
						return CMStrings.lastWordIn(I.Name());
					return "";
				}
			},
			new AbilityParmEditorImpl("RIDE_CAPACITY","Ridrs",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Rideable) ? 3 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "2";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof Rideable)
						return ""+((Rideable)I).riderCapacity();
					return "0";
				}
			},
			new AbilityParmEditorImpl("METAL_OR_WOOD","Metal",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(new String[] { "METAL", "WOOD" });
				}

				@Override
				public String defaultValue()
				{
					return "METAL";
				}

				@Override
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
			new AbilityParmEditorImpl("OPTIONAL_RACE_ID","Race",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
					createChoices(CMClass.races());
					choices().add("","");
					for(int x=0;x<choices().size();x++)
						choices().get(x).first = choices().get(x).first.toUpperCase();
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return ""; // absolutely no way to determine
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					final Vector<String> parsedVals = CMParms.parse(oldVal.toUpperCase());
					for(int v=0;v<parsedVals.size();v++)
					{
						if(CMClass.getRace(parsedVals.elementAt(v))==null)
							return false;
					}
					return true;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					Vector<String> raceIDs=null;
					if(httpReq.isUrlParameter(fieldName+"_RACE"))
					{
						String id="";
						raceIDs=new Vector<String>();
						for(int i=0;httpReq.isUrlParameter(fieldName+"_RACE"+id);id=""+(++i))
							raceIDs.addElement(httpReq.getUrlParameter(fieldName+"_RACE"+id).toUpperCase().trim());
					}
					else
						raceIDs = CMParms.parse(oldVal.toUpperCase().trim());
					return CMParms.combine(raceIDs,0);
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final Vector<String> raceIDs=CMParms.parse(webValue(httpReq,parms,oldVal,fieldName).toUpperCase());
					final StringBuffer str = new StringBuffer("");
					str.append("\n\r<SELECT NAME="+fieldName+"_RACE MULTIPLE>");
					str.append("<OPTION VALUE=\"\" "+((raceIDs.size()==0)?"SELECTED":"")+">");
					for(final Enumeration<Race> e=CMClass.races();e.hasMoreElements();)
					{
						final Race R=e.nextElement();
						str.append("<OPTION VALUE=\""+R.ID()+"\" "+((raceIDs.contains(R.ID().toUpperCase()))?"SELECTED":"")+">"+R.name());
					}
					str.append("</SELECT>");
					return str.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					final Vector<String> parsedVals = CMParms.parse(oldVal.toUpperCase());
					if(parsedVals.size()==0)
						return new String[]{""};
					final Vector<String> races = new Vector<String>();
					for(int p=0;p<parsedVals.size();p++)
					{
						final Race R=CMClass.getRace(parsedVals.elementAt(p));
						races.addElement(R.name());
					}
					for(int p=0;p<parsedVals.size();p++)
					{
						final Race R=CMClass.getRace(parsedVals.elementAt(p));
						races.addElement(R.name());
					}
					races.addElement("");
					return CMParms.toStringArray(races);
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					if((showFlag>0)&&(showFlag!=showNumber[0]))
						return oldVal;
					String behave="NO";
					String newVal = oldVal;
					while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
					{
						mob.tell(showNumber+". "+prompt()+": '"+newVal+"'.");
						if((showFlag!=showNumber[0])&&(showFlag>-999))
							return newVal;
						final Vector<String> parsedVals = CMParms.parse(newVal.toUpperCase());
						behave=mob.session().prompt(L("Enter a race to add/remove (?)\n\r:"),"");
						if(behave.length()>0)
						{
							if(behave.equalsIgnoreCase("?"))
								mob.tell(CMLib.lister().build3ColTable(mob,CMClass.races()).toString());
							else
							{
								final Race R=CMClass.getRace(behave);
								if(R!=null)
								{
									if(parsedVals.contains(R.ID().toUpperCase()))
									{
										mob.tell(L("'@x1' removed.",behave));
										parsedVals.remove(R.ID().toUpperCase().trim());
										newVal = CMParms.combine(parsedVals,0);
									}
									else
									{
										mob.tell(L("@x1 added.",R.ID()));
										parsedVals.addElement(R.ID().toUpperCase());
										newVal = CMParms.combine(parsedVals,0);
									}
								}
								else
								{
									mob.tell(L("'@x1' is not a recognized race.  Try '?'.",behave));
								}
							}
						}
						else
						{
							if(oldVal.equalsIgnoreCase(newVal))
								mob.tell(L("(no change)"));
						}
					}
					return newVal;
				}
			},
			new AbilityParmEditorImpl("INSTRUMENT_TYPE","Instrmnt",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(MusicalInstrument.InstrumentType.valueNames());
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof MusicalInstrument) ? 5 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "DRUMS";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I instanceof MusicalInstrument)
						return ((MusicalInstrument)I).getInstrumentTypeName();
					return "0";
				}
			},
			new AbilityParmEditorImpl("STONE_FLAG","Stone",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(new String[] { "", "STONE" });
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof RawMaterial) ? 5 : -1;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I.material()==RawMaterial.RESOURCE_STONE)
						return "STONE";
					return "";
				}
			},
			new AbilityParmEditorImpl("POSE_NAME","Pose Word",ParmType.ONEWORD)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "New Post";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("POSE_DESCRIPTION","Pose Description",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String defaultValue()
				{
					return "<S-NAME> is standing here.";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(!(I instanceof DeadBody))
						return "";
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
			new AbilityParmEditorImpl("WOOD_METAL_CLOTH","",ParmType.CHOICES)
			{
				@Override
				public void createChoices()
				{
					createChoices(new String[] { "WOOD", "METAL", "CLOTH" });
				}

				@Override
				public String defaultValue()
				{
					return "WOOD";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					switch(I.material()&RawMaterial.MATERIAL_MASK)
					{
					case RawMaterial.MATERIAL_CLOTH:
						return "CLOTH";
					case RawMaterial.MATERIAL_METAL:
						return "METAL";
					case RawMaterial.MATERIAL_MITHRIL:
						return "METAL";
					case RawMaterial.MATERIAL_WOODEN:
						return "WOOD";
					default:
						return "";
					}
				}
			},
			new AbilityParmEditorImpl("WEAPON_TYPE","W.Type",ParmType.CHOICES)
			{
				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Weapon) ? 2 : -1;
				}

				@Override
				public void createChoices()
				{
					createChoices(Weapon.TYPE_DESCS);
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return (I instanceof Weapon) ? Weapon.TYPE_DESCS[((Weapon) I).weaponDamageType()] : "";
				}

				@Override
				public String defaultValue()
				{
					return "BASHING";
				}
			},
			new AbilityParmEditorImpl("ATTACK_MODIFICATION","Att.",ParmType.NUMBER)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return (o instanceof Weapon) ? 2 : -1;
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "" + ((I instanceof Weapon) ? ((Weapon) I).basePhyStats().attackAdjustment() : 0);
				}

				@Override
				public String defaultValue()
				{
					return "0";
				}
			},
			new AbilityParmEditorImpl("N_A","N/A",ParmType.STRING)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return -1;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					return "";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return oldVal.trim().length() == 0 || oldVal.equals("0") || oldVal.equals("NA") || oldVal.equals("-");
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					return "";
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String, String> parms, final String oldVal, final String fieldName)
				{
					return "";
				}
			},
			new AbilityParmEditorImpl("RESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED","Resrc/Amt",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
					createChoices(RawMaterial.CODES.NAMES());
					choices().add("","");
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					int amt=(int)Math.round(CMath.mul(I.basePhyStats().weight()-1,(A!=null)?A.getItemWeightMultiplier(false):1.0));
					if(amt<1)
						amt=1;
					return RawMaterial.CODES.NAME(I.material())+"/"+amt;
				}

				@Override
				public String defaultValue()
				{
					return "";
				}

				@Override
				public int appliesToClass(final Object o)
				{
					return 0;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					if(httpReq.isUrlParameter(fieldName+"_RESOURCE"))
					{
						final String rsc=httpReq.getUrlParameter(fieldName+"_RESOURCE");
						final String amt=httpReq.getUrlParameter(fieldName+"_AMOUNT");
						if((rsc.trim().length()==0)||(rsc.equalsIgnoreCase("NOTHING"))||(CMath.s_int(amt)<=0))
							return "";
						return rsc+"/"+amt;
					}
					return oldVal;
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					final String value=webValue(httpReq,parms,oldVal,fieldName);
					String rsc = "";
					int amt = 0;
					final int x=value.indexOf('/');
					if(x>0)
					{
						rsc = value.substring(0,x);
						amt = CMath.s_int(value.substring(x+1));
					}
					final StringBuffer str=new StringBuffer("");
					str.append("\n\r<SELECT NAME="+fieldName+"_RESOURCE MULTIPLE>");
					final String[] Ss=RawMaterial.CODES.NAMES().clone();
					Arrays.sort(Ss);
					for(final String S : Ss)
					{
						str.append("<OPTION VALUE=\""+S+"\" "
								+((S.equalsIgnoreCase(rsc))?"SELECTED":"")+">"
								+CMStrings.capitalizeAndLower(S));
					}
					str.append("</SELECT>");
					str.append("&nbsp;&nbsp;Amount: ");
					str.append("<INPUT TYPE=TEXT NAME="+fieldName+"_AMOUNT VALUE="+amt+">");
					return str.toString();
				}

				@Override
				public boolean confirmValue(String oldVal)
				{
					if(oldVal.trim().length()==0)
						return true;
					oldVal=oldVal.trim();
					final int x=oldVal.indexOf('/');
					if(x<0)
						return false;
					if(!CMStrings.contains(choices().toArrayFirst(new String[0]),oldVal.substring(0,x)))
						return false;
					if(!CMath.isInteger(oldVal.substring(x+1)))
						return false;
					return true;
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					final int x=oldVal.indexOf('/');
					if(x<=0) return new String[]{""};
					return new String[]{oldVal.substring(0,x),oldVal.substring(x+1)};
				}

				@Override
				public String commandLinePrompt(final MOB mob, String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					oldVal=oldVal.trim();
					final int x=oldVal.indexOf('/');
					String oldRsc = "";
					int oldAmt = 0;
					if(x>0)
					{
						oldRsc = oldVal.substring(0,x);
						oldAmt = CMath.s_int(oldVal.substring(x));
					}
					oldRsc = CMLib.genEd().promptChoice(mob,oldRsc,++showNumber[0],showFlag,prompt(),choices());
					if(oldRsc.length()>0)
						return oldRsc+"/"+CMLib.genEd().prompt(mob,oldAmt,++showNumber[0],showFlag,prompt());
					return "";
				}
			},
			new AbilityParmEditorImpl("ITEM_CMARE","Items",ParmType.SPECIAL)
			{
				@Override
				public void createChoices()
				{
				}

				@Override
				public String commandLineValue(final String oldVal)
				{
					if(oldVal.trim().startsWith("<"))
					{
						final List<XMLLibrary.XMLTag> xml = CMLib.xml().parseAllXML(oldVal);
						if(xml.size()>0)
						{
							final String nameXML=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ITEXT");
							final List<XMLLibrary.XMLTag> nameTags=CMLib.xml().parseAllXML(CMLib.xml().restoreAngleBrackets(nameXML));
							final String name=CMLib.xml().getValFromPieces(nameTags, "NAME");
							final String classID=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ICLAS");
							return name+" ("+classID+")";
						}
					}
					return oldVal;
				}

				@Override
				public String defaultValue()
				{
					return "<ITEM></ITEM>";
				}

				@Override
				public String convertFromItem(final ItemCraftor A, final Item I)
				{
					if(I==null)
						return defaultValue();
					return CMLib.coffeeMaker().getItemXML(I);
				}

				@Override
				public String prompt()
				{
					return "Item";
				}

				@Override
				public boolean confirmValue(final String oldVal)
				{
					return ((oldVal != null)
							&&(oldVal.length()>0)
							&&(oldVal.trim().startsWith("<")));
				}

				@Override
				public String webTableField(final HTTPRequest httpReq, final java.util.Map<String, String> parms, final String oldVal)
				{
					final String val=oldVal;

					if((val != null)
					&&(val.length()>0)
					&&(val.trim().startsWith("<")))
					{
						final List<XMLLibrary.XMLTag> xml = CMLib.xml().parseAllXML(oldVal);
						if(xml.size()>0)
						{
							final String nameXML=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ITEXT");
							final List<XMLLibrary.XMLTag> nameTags=CMLib.xml().parseAllXML(CMLib.xml().restoreAngleBrackets(nameXML));
							final String name=CMLib.xml().getValFromPieces(nameTags, "NAME");
							final String classID=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ICLAS");
							return name+" ("+classID+")";
						}
					}
					return oldVal;
				}

				@Override
				public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					if(fieldName.equalsIgnoreCase("NEWCLASSFIELD"))
					{
						return "";
					}
					else
					if(fieldName.equalsIgnoreCase("CLASSFIELD"))
					{
						if(oldVal.trim().startsWith("<"))
						{
							final List<XMLLibrary.XMLTag> pieces=CMLib.xml().parseAllXML(oldVal);
							if(pieces.size()>0)
							{
								final XMLLibrary.XMLTag tag = CMLib.xml().getPieceFromPieces(pieces, "ITEM");
								if(tag != null)
								{
									String realClassID=CMLib.xml().getValFromPieces(tag.contents(), "ICLAS");
									if(realClassID == null)
										realClassID=CMLib.xml().getValFromPieces(tag.contents(), "CLASSID");
									if((realClassID!=null)
									&&(realClassID.length()>0))
										return realClassID;
								}
							}
						}
						return oldVal;
					}
					else
					if(fieldName.startsWith("DATA_"))
					{
						if(httpReq.isUrlParameter("ITEM"))
						{
							final String itemID = httpReq.getUrlParameter("ITEM");
							final Item I=CMLib.webMacroFilter().getItemFromWebCache(itemID);
							if(I!=null)
								return CMLib.coffeeMaker().getItemXML(I);
						}
						String rowNum=fieldName.substring(5);
						final int x=rowNum.indexOf('_');
						if(x>0)
							rowNum=rowNum.substring(0,x);
						Item I=null;
						if(oldVal.trim().startsWith("<"))
						{
							final List<Item> madeItem=new ArrayList<Item>(1);
							final String error=CMLib.coffeeMaker().addItemsFromXML("<ITEMS>"+oldVal+"</ITEMS>", madeItem, null);
							if(((error == null)||(error.length()==0))
							&&(madeItem.size()>0))
							{
								I=madeItem.get(0);
								CMLib.threads().unTickAll(I);
							}
						}
						else
							I=CMClass.getItem(oldVal);
						if(I!=null)
						{
							CMLib.webMacroFilter().contributeItemsToWebCache(new XVector<Item>(I));
							final String cachedID = CMLib.webMacroFilter().findItemWebCacheCode(I);
							final StringBuilder data = new StringBuilder("");
							data.append("<FONT COLOR=WHITE>"+I.Name()+" ("+I.ID()+")</FONT>&nbsp;&nbsp;");
							data.append("<INPUT TYPE=HIDDEN NAME=\""+fieldName+"\" VALUE=\""+cachedID+"\">");
							httpReq.addFakeUrlParameter("ONMODIFY", "SwitchToItemEditor('"+cachedID+"','"+rowNum+"')");
							httpReq.addFakeUrlParameter("HIDESAVEOPTION", "true");
							//data.append("<a href=\"javascript:SwitchToItemEditor('"+cachedID+"','"+rowNum+"');\">Edit Item</a>\n\r ");
							I.destroy();
							return data.toString();
						}
					}

					Log.debugOut("AbilityEditor:ITEM_CMARE:webValue: "+fieldName+": "+oldVal);
					return oldVal;
				}

				@Override
				public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
				{
					String value=webValue(httpReq,parms,oldVal,fieldName);
					if(value.endsWith("$"))
						value = value.substring(0,oldVal.length()-1);
					value = value.trim();
					if(fieldName.equalsIgnoreCase("NEWCLASSFIELD"))
					{
						final StringBuilder html = new StringBuilder("");
						final List<String> sortMe=new Vector<String>();
						CMClass.addAllItemClassNames(sortMe,true,false,true,Area.THEME_ALLTHEMES);
						Collections.sort(sortMe);
						html.append("<SELECT NAME="+fieldName+" ID="+fieldName+">");
						for (final Object element : sortMe)
							html.append("<OPTION VALUE=\""+(String)element+"\">"+(String)element);
						html.append("</SELECT>");
						return html.toString();
					}
					else
					if(fieldName.equalsIgnoreCase("CLASSFIELD"))
					{
						return "*dunno*";
					}
					Log.debugOut("AbilityEditor:ITEM_CMARE:webField: "+fieldName+": "+oldVal);
					return value.toString();
				}

				@Override
				public String[] fakeUserInput(final String oldVal)
				{
					return new String[] { oldVal };
				}

				@Override
				public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
				{
					++showNumber[0];
					String str = oldVal;
					Item I = null;
					if(oldVal.trim().startsWith("<"))
					{
						final List<Item> madeItem=new ArrayList<Item>(1);
						final String error=CMLib.coffeeMaker().addItemsFromXML("<ITEMS>"+oldVal+"</ITEMS>", madeItem, null);
						if(((error == null)||(error.length()==0))
						&&(madeItem.size()>0))
						{
							I=madeItem.get(0);
							CMLib.threads().unTickAll(I);
						}
					}
					else
						I=CMClass.getItem(oldVal);
					while(!mob.session().isStopped())
					{
						final String showVal = (I==null)?"None?!":(I.Name()+" ("+I.ID()+")");
						str=CMLib.genEd().prompt(mob,showVal,showNumber[0],showFlag,prompt(),true,"").trim();
						if(str.equals(oldVal)||(str.equals(showVal))||(str.trim().length()==0))
							return oldVal;
						final Item newI = mob.location().findItem(str);
						if(newI == null)
							mob.tell(L("No item '"+str+"' found in "+mob.location().displayText(mob)));
						else
							return CMLib.coffeeMaker().getItemXML(newI);
					}
					if(I!=null)
						I.destroy();
					return str;
				}
			},
		});
		DEFAULT_EDITORS = new Hashtable<String,AbilityParmEditor>();
		for(int v=0;v<V.size();v++)
		{
			final AbilityParmEditor A = V.elementAt(v);
			DEFAULT_EDITORS.put(A.ID(),A);
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
				while(proceed&&(!mob.session().isStopped()))
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
