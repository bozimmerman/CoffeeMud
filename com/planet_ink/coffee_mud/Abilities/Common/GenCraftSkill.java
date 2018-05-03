package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Pattern;

/*
   Copyright 2011-2018 Bo Zimmerman

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
public class GenCraftSkill extends EnhancedCraftingSkill implements ItemCraftor
{
	public String	ID	= "GenCraftSkill";

	@Override
	public String ID()
	{
		return ID;
	}

	@Override
	public String Name()
	{
		return name();
	}

	@Override
	public String name()
	{
		return (String) V(ID, V_NAME);
	}

	private static final Map<String,Object[]> vars=new Hashtable<String,Object[]>();
	private static final int	V_NAME			= 0;	// S
	private static final int	V_TRIG			= 1;	// S[]
	private static final int	V_HELP			= 2;	// S
	private static final int	V_FNAM			= 3;	// S
	private static final int	V_RSCS			= 4;	// S
	private static final int	V_VERB			= 5;	// S
	private static final int	V_CNMN			= 6;	// B
	private static final int	V_CNRF			= 7;	// B
	private static final int	V_CNBN			= 8;	// B
	private static final int	V_SOND			= 9;	// S
	private static final int	V_CNST			= 10;	// B
	private static final int	NUM_VS			= 11;	// S

	// 

	@Override
	public String parametersFormat()
	{ 
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\t"
		+"ITEM_BASE_VALUE\tITEM_CLASS_ID\t"
		+"LID_LOCK||STATUE||RIDE_BASIS||WEAPON_CLASS||CODED_WEAR_LOCATION||SMOKE_FLAG\t"
		+"CONTAINER_CAPACITY||WEAPON_HANDS_REQUIRED||LIQUID_CAPACITY||LIGHT_DURATION||MAX_WAND_USES\t"
		+"BASE_ARMOR_AMOUNT||BASE_DAMAGE\tCONTAINER_TYPE||ATTACK_MODIFICATION\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_AMOUNTMATS	= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MISCTYPE	= 6;
	protected static final int	RCP_CAPACITY	= 7;
	protected static final int	RCP_ARMORDMG	= 8;
	protected static final int	RCP_CONTAINMASK	= 9;
	protected static final int	RCP_SPELL		= 10;

	protected DoorKey key=null;

	@Override
	public boolean supportsDeconstruction()
	{
		return false;
	}

	private static final Object[] makeEmpty()
	{
		final Object[] O=new Object[NUM_VS];
		O[V_NAME]="Crafting Skill";
		O[V_TRIG]=new String[]{"CRAFT"};
		O[V_HELP]="<ABILITY>This skill is not yet documented.";
		O[V_FNAM]="";
		O[V_RSCS]="WOODEN";
		O[V_VERB]="crafting";
		O[V_CNMN]=Boolean.valueOf(true);
		O[V_CNRF]=Boolean.valueOf(true);
		O[V_CNBN]=Boolean.valueOf(true);
		O[V_SOND]="sawing.wav";
		O[V_CNST]=Boolean.valueOf(false);
		return O;
	}

	@Override
	public String[] triggerStrings()
	{
		return (String[]) V(ID, V_TRIG);
	}

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return ((Boolean) V(ID, V_CNST)).booleanValue();
	}

	@Override
	public String parametersFile()
	{
		return (String) V(ID, V_FNAM);
	}

	@Override
	public String supportedResourceString()
	{
		return (String) V(ID, V_RSCS);
	}

	private static final Object V(String ID, int varNum)
	{
		if(vars.containsKey(ID))
			return vars.get(ID)[varNum];
		final Object[] O=makeEmpty();
		vars.put(ID,O);
		return O[varNum];
	}

	private static final void SV(String ID,int varNum,Object O)
	{
		if(vars.containsKey(ID))
			vars.get(ID)[varNum]=O;
		else
		{
			final Object[] O2=makeEmpty();
			vars.put(ID,O2);
			O2[varNum]=O;
		}
	}

	public GenCraftSkill()
	{
		super();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			final GenCraftSkill A=this.getClass().newInstance();
			A.ID=ID;
			return A;
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenCraftSkill();
	}

	@Override
	protected void cloneFix(Ability E)
	{
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	// lots of work to be done here
	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[] CODES={"CLASS",//0
										 "TEXT",//1
										 "NAME",//2S
										 "HELP",//27I
										 "TRIGSTR",//4S[]
										 "FILENAME",//2S
										 "MATLIST",//2S
										 "VERB",//2S
										 "CANMEND",//2S
										 "CANREFIT",//2S
										 "CANBUNDLE",//2S
										 "SOUND",//2S
										 "CANSIT",//2S
										};

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
	{
		/*
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		*/
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return text();
		case 2:
			return (String) V(ID, V_NAME);
		case 3:
			return (String) V(ID, V_HELP);
		case 4:
			return CMParms.toListString((String[]) V(ID, V_TRIG));
		case 5:
			return (String) V(ID, V_FNAM);
		case 6:
			return ((String) V(ID, V_RSCS)).replace('|', ',');
		case 7:
			return (String) V(ID, V_VERB);
		case 8:
			return Boolean.toString(((Boolean) V(ID, V_CNMN)).booleanValue());
		case 9:
			return Boolean.toString(((Boolean) V(ID, V_CNRF)).booleanValue());
		case 10:
			return Boolean.toString(((Boolean) V(ID, V_CNBN)).booleanValue());
		case 11:
			return (String) V(ID, V_SOND);
		case 12:
			return Boolean.toString(((Boolean) V(ID, V_CNST)).booleanValue());
		default:
			if(code.equalsIgnoreCase("allxml"))
				return getAllXML();
			break;
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		switch(getCodeNum(code))
		{
		case 0:
			if(val.trim().length()>0)
			{
				V(ID,V_NAME); // force creation, if necc
				final Object[] O=vars.get(ID);
				vars.remove(ID);
				vars.put(val,O);
				if(num!=9)
					CMClass.delClass(CMObjectType.ABILITY,this);
				ID=val;
				if(num!=9)
					CMClass.addClass(CMObjectType.ABILITY,this);
			}
			break;
		case 1:
			setMiscText(val);
			break;
		case 2:
			SV(ID, V_NAME, val);
			if (ID.equalsIgnoreCase("GenCraftSkill"))
				break;
			break;
		case 3:
			SV(ID, V_HELP, val);
			break;
		case 4:
			SV(ID, V_TRIG, CMParms.parseCommas(val, true).toArray(new String[0]));
			break;
		case 5:
			SV(ID, V_FNAM, val);
			break;
		case 6:
			SV(ID, V_RSCS, val.toUpperCase().replace(',', '|'));
			break;
		case 7:
			SV(ID, V_VERB, val);
			break;
		case 8:
			SV(ID, V_CNMN, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 9:
			SV(ID, V_CNRF, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 10:
			SV(ID, V_CNBN, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 11:
			SV(ID, V_SOND, val);
			break;
		case 12:
			SV(ID, V_CNST, Boolean.valueOf(CMath.s_bool(val)));
			break;
		default:
			if(code.equalsIgnoreCase("allxml")&&ID.equalsIgnoreCase("GenCraftSkill"))
				parseAllXML(val);
			break;
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenCraftSkill))
			return false;
		if(!((GenCraftSkill)E).ID().equals(ID))
			return false;
		if(!((GenCraftSkill)E).text().equals(text()))
			return false;
		return true;
	}

	private void parseAllXML(String xml)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(xml);
		if((V==null)||(V.size()==0))
			return;
		for(int c=0;c<getStatCodes().length;c++)
		{
			if(getStatCodes()[c].equals("CLASS"))
				ID=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c]));
			else
			if(!getStatCodes()[c].equals("TEXT"))
				setStat(getStatCodes()[c],CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c])));
		}
	}

	private String getAllXML()
	{
		final StringBuffer str=new StringBuffer("");
		for(int c=0;c<getStatCodes().length;c++)
		{
			if(!getStatCodes()[c].equals("TEXT"))
			{
				str.append("<"+getStatCodes()[c]+">"
						+CMLib.xml().parseOutAngleBrackets(getStat(getStatCodes()[c]))
						+"</"+getStatCodes()[c]+">");
			}
		}
		return str.toString();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(buildingI==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		if(parametersFile().length()==0)
			return new Vector<List<String>>();
		return super.loadRecipes(parametersFile());
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,L("<S-NAME> mess(es) up refitting @x1.",buildingI.name()));
						else
							commonEmote(mob,L("<S-NAME> mess(es) up @x1 @x2.",((String)V(ID,V_VERB)),buildingI.name()));
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
						{
							buildingI.setUsesRemaining(100);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.MENDER, 1, this);
						}
						else
						if(activity == CraftingActivity.REFITTING)
						{
							buildingI.basePhyStats().setHeight(0);
							buildingI.recoverPhyStats();
						}
						else
						{
							dropAWinner(mob,buildingI);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this);
							if(key!=null)
							{
								dropAWinner(mob,key);
								if(buildingI instanceof Container)
									key.setContainer((Container)buildingI);
							}
						}
					}
				}
				buildingI=null;
				key=null;
				activity = CraftingActivity.CRAFTING;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		return mayICraft(null,I);
	}

	@Override
	public boolean mayICraft(final MOB mob, final Item I)
	{
		if(I==null)
			return false;
		if(!super.isMadeOfSupportedResource(I))
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		for(final List<String> recipe : recipes)
		{
			String finalName=recipe.get(RCP_FINALNAME);
			finalName=CMStrings.replaceAll(finalName, "%", ".*").toLowerCase();
			if(Pattern.matches(finalName, I.Name().toLowerCase()))
				return true;
		}
		return false;
	}

	public boolean supportsMending(Physical I)
	{
		return canMend(null, I, true);
	}

	@Override
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		final Boolean canMendB=(Boolean)V(ID,V_CNMN);
		if(!canMendB.booleanValue())
			return false;
		if(!super.canMend(mob,E,quiet))
			return false;
		final Item IE=(Item)E;
		if(mayICraft(mob, IE))
			return true;
		if(!super.isMadeOfSupportedResource(IE))
		{
			if(!quiet)
			{
				commonTell(mob,L("That can't be mended with this skill."));
			}
			return false;
		}
		return true;
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_AMOUNTMATS );
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(0));
	}
	
	@Override
	protected boolean autoGenInvoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto, 
								 final int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;
		
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		final String noun=CMStrings.capitalizeAndLower(triggerStrings()[0]);
		final String verbing=V(ID,V_VERB).toString();
		final Boolean canMendB=(Boolean)V(ID,V_CNMN);
		final Boolean canRefitB=(Boolean)V(ID,V_CNRF);
		final Boolean canBundleB=(Boolean)V(ID,V_CNBN);
		if(commands.size()==0)
		{
			final StringBuilder features=new StringBuilder(noun+" what? Enter \""+noun.toLowerCase()+" list\" for a list");
			features.append(", \""+noun.toLowerCase()+" info\" to details");
			if(canMendB.booleanValue())
				features.append(", \""+noun.toLowerCase()+" mend <item>\" to mend broken items, \""+noun.toLowerCase()+" scan\" to scan for mendable items");
			if(canRefitB.booleanValue())
				features.append(", \""+noun.toLowerCase()+" refit <item>\" to resize wearables");
			if(canBundleB.booleanValue())
				features.append(", \""+noun.toLowerCase()+" bundle\" to make bundles");
			features.append(", or \""+noun.toLowerCase()+" stop\" to cancel.");
			commonTell(mob,features.toString());
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle"))
		&&(canBundleB.booleanValue()))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=commands.get(0);
		String startStr=null;
		int duration=4;
		bundling=false;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(L("Item <S-NAME> <S-IS-ARE> skilled at @x1:\n\r",verbing));
			int toggler=1;
			final int toggleTop=2;
			final int[] cols={
				CMLib.lister().fixColWidth(29,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session()),
				CMLib.lister().fixColWidth(4,mob.session())
			};
			for(int r=0;r<toggleTop;r++)
				buf.append((r>0?" ":"")+CMStrings.padRight(L("Item"),cols[0])+" "+CMStrings.padRight(L("Lvl"),cols[1])+" "+CMStrings.padRight(L("Mats"),cols[2]));
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String mats=getComponentDescription(mob,V,RCP_AMOUNTMATS);
					if(mats.length()>5)
					{
						if(toggler>1)
							buf.append("\n\r");
						toggler=toggleTop;
					}
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRightPreserve(""+mats,cols[2])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			if(toggler!=1)
				buf.append("\n\r");
			commonTell(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		if(str.equalsIgnoreCase("scan") && canMendB.booleanValue())
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend") && canMendB.booleanValue())
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob, buildingI,false))
				return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr=L("<S-NAME> start(s) mending @x1.",buildingI.name());
			displayText=L("You are mending @x1",buildingI.name());
			verb=L("mending @x1",buildingI.name());
		}
		else
		if(str.equalsIgnoreCase("refit") && canRefitB.booleanValue())
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(buildingI==null)
				return false;
			if((!this.mayICraft(mob, buildingI))&&(!super.isMadeOfSupportedResource(buildingI)))
			{
				commonTell(mob,L("That's can't be refitted with this skill."));
				return false;
			}
			if(!(buildingI instanceof Armor))
			{
				commonTell(mob,L("You don't know how to refit that sort of thing."));
				return false;
			}
			if(buildingI.phyStats().height()==0)
			{
				commonTell(mob,L("@x1 is already the right size.",buildingI.name(mob)));
				return false;
			}
			activity = CraftingActivity.REFITTING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr=L("<S-NAME> start(s) refitting @x1.",buildingI.name());
			displayText=L("You are refitting @x1",buildingI.name());
			verb=L("refitting @x1",buildingI.name());
		}
		else
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			key=null;
			messedUp=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
			{
				amount=CMath.s_int(commands.get(commands.size()-1));
				commands.remove(commands.size()-1);
			}
			final String recipeName=CMParms.combine(commands,0);
			List<String> foundRecipe=null;
			final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
			for(int r=0;r<matches.size();r++)
			{
				final List<String> V=matches.get(r);
				if(V.size()>0)
				{
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					if((autoGenerate>0)||(level<=xlevel(mob)))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,L("You don't know how to @x1 a '@x2'.  Try \"@x3 list\" for a list.",noun.toLowerCase(),recipeName,noun.toLowerCase()));
				return false;
			}

			final String requiredMats = foundRecipe.get(RCP_AMOUNTMATS);
			final int[] compData = new int[CF_TOTAL];
			final List<Object> componentsFoundList=getAbilityComponents(mob, requiredMats, "make "+CMLib.english().startWithAorAn(recipeName),autoGenerate,compData);
			if(componentsFoundList==null)
				return false;
			int numRequired=CMath.isInteger(requiredMats)?CMath.s_int(requiredMats):0;
			numRequired=adjustWoodRequired(numRequired,mob);

			if(amount>numRequired)
				numRequired=amount;
			final String misctype=foundRecipe.get(RCP_MISCTYPE);
			final Integer[] ipm=super.supportedResourcesMap();
			final int[] pm=new int[ipm.length];
			for(int i=0;i<ipm.length;i++) pm[i]=ipm[i].intValue();
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			final int[][] data=fetchFoundResourceData(mob,
													numRequired,"material",pm,
													0,null,null,
													bundling,
													autoGenerate,
													enhancedTypes);
			if(data==null)
				return false;
			fixDataForComponents(data,requiredMats,autoGenerate>0,componentsFoundList);
			numRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			final int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResourcesValue(mob.location(),numRequired,data[0][FOUND_CODE],0,null)
				+CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			if(buildingI==null)
			{
				commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			buildingI.setMaterial(getBuildingMaterial(numRequired,data,compData));
			String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(buildingI.material())).toLowerCase();
			if(bundling)
				itemName="a "+numRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) @x1 @x2.",verbing,buildingI.name());
			displayText=L("You are @x1 @x2",verbing,buildingI.name());
			playSound=(String)V(ID,V_SOND);
			verb=verbing+" "+buildingI.name();
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(itemName+". ");
			buildingI.basePhyStats().setWeight(getStandardWeight(numRequired, bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
			final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-3;
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL))+(hardness));
			if(buildingI.basePhyStats().level()<1)
				buildingI.basePhyStats().setLevel(1);
			setBrand(mob, buildingI);
			final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
			final long canContain=getContainerType(foundRecipe.get(RCP_CONTAINMASK));
			final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			if(bundling)
				buildingI.setBaseValue(lostValue);
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			addSpells(buildingI,spell);
			key=null;
			if((buildingI instanceof Container)
			&&(!(buildingI instanceof Armor)))
			{
				if(capacity>0)
				{
					((Container)buildingI).setCapacity(capacity+numRequired);
					((Container)buildingI).setContainTypes(canContain);
				}
				if(misctype.equalsIgnoreCase("LID"))
					((Container)buildingI).setDoorsNLocks(true,false,true,false,false,false);
				else
				if(misctype.equalsIgnoreCase("LOCK"))
				{
					((Container)buildingI).setDoorsNLocks(true,false,true,true,false,true);
					((Container)buildingI).setKeyName(Double.toString(Math.random()));
					key=(DoorKey)CMClass.getItem("GenKey");
					key.setKey(((Container)buildingI).keyName());
					key.setName(L("a key"));
					key.setDisplayText(L("a small key sits here"));
					key.setDescription(L("looks like a key to @x1",buildingI.name()));
					key.recoverPhyStats();
					key.text();
				}
			}
			if(buildingI instanceof Drink)
			{
				if(CMLib.flags().isGettable(buildingI))
				{
					((Drink)buildingI).setLiquidHeld(capacity*50);
					((Drink)buildingI).setThirstQuenched(250);
					if((capacity*50)<250)
						((Drink)buildingI).setThirstQuenched(capacity*50);
					((Drink)buildingI).setLiquidRemaining(0);
				}
			}
			if(buildingI instanceof Rideable)
			{
				setRideBasis((Rideable)buildingI,misctype);
			}
			if(buildingI instanceof Wand)
			{
				if(foundRecipe.get(RCP_CAPACITY).trim().length()>0)
					((Wand)buildingI).setMaxUses(capacity);
			}
			else
			if(buildingI instanceof Weapon)
			{
				((Weapon)buildingI).setRawLogicalAnd((capacity>1));
			}
			if(buildingI instanceof Weapon)
			{
				((Weapon)buildingI).setWeaponClassification(Weapon.CLASS_BLUNT);
				setWeaponTypeClass((Weapon)buildingI,misctype,Weapon.TYPE_SLASHING);
				buildingI.basePhyStats().setAttackAdjustment((baseYield()+abilityCode()+(hardness*5)-1));
				buildingI.basePhyStats().setDamage(armordmg+hardness);
				((Weapon)buildingI).setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
				if(!(buildingI instanceof Container))
					buildingI.basePhyStats().setAttackAdjustment(buildingI.basePhyStats().attackAdjustment()+(int)canContain);
			}
			if((buildingI instanceof Armor)&&(!(buildingI instanceof FalseLimb)))
			{
				((Armor)buildingI).basePhyStats().setArmor(0);
				if(armordmg!=0)
					((Armor)buildingI).basePhyStats().setArmor(armordmg+(baseYield()+abilityCode()-1));
				setWearLocation(buildingI,misctype,hardness);
			}
			if(buildingI instanceof Light)
			{
				((Light)buildingI).setDuration(capacity);
				if((buildingI instanceof Container)
				&&(!misctype.equals("SMOKE")))
				{
					((Light)buildingI).setDuration(200);
					((Container)buildingI).setCapacity(0);
				}
			}
			buildingI.recoverPhyStats();
			buildingI.text();
			buildingI.recoverPhyStats();
		}

		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb=L("bundling @x1",RawMaterial.CODES.NAME(buildingI.material()).toLowerCase());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}

		if(autoGenerate>0)
		{
			if(key!=null)
				crafted.add(key);
			crafted.add(buildingI);
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,buildingI,enhancedTypes);
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
