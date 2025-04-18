package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2025 Bo Zimmerman

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
public class JewelMaking extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	@Override
	public String ID()
	{
		return "JewelMaking";
	}

	private final static String	localizedName	= CMLib.lang().L("Jewel Making");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "JEWEL", "JEWELMAKE", "JEWELMAKING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "GLASS|PRECIOUS|SAND";
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.Armor;
	}

	@Override
	public String getRecipeFormat()
	{
		return
		  "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+ "ITEM_CLASS_ID\t"
		+ "STATUE||CODED_WEAR_LOCATION\t"
		+ "N_A\t"
		+ "BASE_ARMOR_AMOUNT||DICE_SIDES\t"
		+ "OPTIONAL_RESOURCE_OR_MATERIAL\t"
		+ "CODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MISCTYPE	= 6;
	// private static final int RCP_CAPACITY	= 7;
	protected static final int	RCP_ARMORDMG	= 8;
	protected static final int	RCP_EXTRAREQ	= 9;
	protected static final int	RCP_SPELL		= 10;

	protected Pair<Item,String> beingDone=null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((fireRequired)
			&&(mob.location()==activityRoom))
			{
				if((buildingI==null)
				||(getRequiredFire(mob,0)==null))
				{
					messedUp=true;
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String getRecipeFilename()
	{
		return "jewelmaking.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(getRecipeFilename());
	}

	@Override
	protected boolean doLearnRecipe(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
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
					if(beingDone!=null)
					{
						if(messedUp)
							commonEmote(mob,L("<S-NAME> mess(es) up @x1.",verb));
						else
						{
							final Item I=beingDone.first;
							buildingI.setBaseValue(buildingI.baseGoldValue()+(I.baseGoldValue()*2));
							buildingI.setDescription(buildingI.description()+" "+beingDone.second);
						}
						beingDone=null;
					}
					else
					if(messedUp)
					{
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.LEARNING)
						{
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
							dropALoser(mob,buildingI);
							buildingI.destroy();
						}
						else
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,L("<S-NAME> mess(es) up refitting @x1.",buildingI.name()));
						else
						{
							commonEmote(mob,L("<S-NAME> mess(es) up @x1.",verb));
							dropALoser(mob,buildingI);
						}
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
						{
							buildingI.setUsesRemaining(100);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.MENDER, 1, this, buildingI);
						}
						else
						if(activity==CraftingActivity.LEARNING)
						{
							deconstructRecipeInto(mob, buildingI, recipeHolder );
							buildingI.destroy();
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
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this, buildingI);
						}
					}
				}
				buildingI=null;
				activity = CraftingActivity.CRAFTING;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
		{
			if(I instanceof Rideable)
			{
				final Rideable R=(Rideable)I;
				final Rideable.Basis rideType=R.rideBasis();
				switch(rideType)
				{
				case LADDER:
				case FURNITURE_SLEEP:
				case FURNITURE_SIT:
				case FURNITURE_TABLE:
				case FURNITURE_HOOK:
					return true;
				default:
					return false;
				}
			}
			else
			if(I instanceof Armor)
			{
				if(I.fitsOn(Wearable.WORN_EARS)
				||I.fitsOn(Wearable.WORN_EYES)
				||I.fitsOn(Wearable.WORN_HEAD)
				||I.fitsOn(Wearable.WORN_NECK)
				||I.fitsOn(Wearable.WORN_FEET)
				||I.fitsOn(Wearable.WORN_LEFT_FINGER)
				||I.fitsOn(Wearable.WORN_RIGHT_FINGER)
				||I.fitsOn(Wearable.WORN_LEFT_WRIST)
				||I.fitsOn(Wearable.WORN_RIGHT_WRIST))
					return true;
				return (isANativeItem(I.Name()));
			}
			if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
				return true;
			return true;
		}
		else
		if((I instanceof Armor)
		&&(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)))
		{
			final Armor A=(Armor)I;
			if((CMath.bset(A.getLayerAttributes(), Armor.LAYERMASK_SEETHROUGH))
			&&(A.basePhyStats().armor()<3))
				return true;
			if((A.basePhyStats().armor()<2)
			&&(I.fitsOn(Wearable.WORN_EARS)
				||I.fitsOn(Wearable.WORN_EYES)
				||I.fitsOn(Wearable.WORN_HEAD)
				||I.fitsOn(Wearable.WORN_NECK)
				||I.fitsOn(Wearable.WORN_FEET)
				||I.fitsOn(Wearable.WORN_LEFT_FINGER)
				||I.fitsOn(Wearable.WORN_RIGHT_FINGER)
				||I.fitsOn(Wearable.WORN_LEFT_WRIST)
				||I.fitsOn(Wearable.WORN_RIGHT_WRIST)))
					return true;
			return (isANativeItem(I.Name()));
		}
		return (isANativeItem(I.Name()));
	}

	@Override
	public boolean supportsMending(final Physical I)
	{
		return canMend(null, I, true);
	}

	@Override
	protected boolean canMend(final MOB mob, final Environmental E, final boolean quiet)
	{
		if(!super.canMend(mob,E,quiet))
			return false;
		if((!(E instanceof Item))
		||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTelL(mob,"That's not a jewelworked item.");
			return false;
		}
		return true;
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new ArrayList<CraftedItem>(0));
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto,
								 final int asLevel, final int autoGenerate, final boolean forceLevels, final List<CraftedItem> crafted)
	{
		final List<String> originalCommands = new XVector<String>(commands);
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;

		fireRequired=true;

		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		int recipeLevel = 1;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTelL(mob,"Make what? Enter \"jewel list\" for a list.  You may also enter jewel encrust <gem name> <item name>, "
							+ "jewel mount <gem name> <item name>, jewel refit <item name>, jewel info <item>, jewel learn <item>, "
							+ "jewel scan, jewel mend <item name>, or jewel stop to cancel.");
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=commands.get(0);
		String startStr=null;
		fireRequired=true;
		bundling=false;
		int duration=4;
		String misctype="";
		if(str.equalsIgnoreCase("list") && (autoGenerate <= 0))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			int toggler=1;
			final int toggleTop=2;
			final StringBuffer buf=new StringBuffer("");
			final int[] cols={
				CMLib.lister().fixColWidth(27,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session()),
				CMLib.lister().fixColWidth(5,mob.session())
			};
			for(int r=0;r<toggleTop;r++)
				buf.append("^H"+(r>0?" ":"")+CMStrings.padRight(L("Item"),cols[0])+" "+CMStrings.padRight(L("Lvl"),cols[1])+" "+CMStrings.padRight(L("Metal"),cols[2]));
			buf.append("^N\n\r");
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipes(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					final String otherRequired=V.get(RCP_EXTRAREQ);
					if((otherRequired!=null)
					&&(otherRequired.equalsIgnoreCase("PRECIOUS"))
					&&(CMath.s_int(wood)==0))
						wood = "1*";
					if((level<=xlevel(mob))||allFlag)
					{
						if(wood.length()>5)
						{
							if(toggler>1)
								buf.append("\n\r");
							toggler=toggleTop;
						}
						buf.append("^w"+CMStrings.padRight(item,cols[0])+"^N "+
									CMStrings.padRight(""+level,cols[1])+" "+
									CMStrings.padRightPreserve(""+wood,cols[2])
									+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			if(!buf.toString().endsWith("\n\r"))
				buf.append("\n\r");
			buf.append(L("* Instead of metal, these recipes require precious stones.\n\r"));
			commonTell(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		else
		if(((commands.get(0))).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		else
		if((str.equalsIgnoreCase("encrust"))||(str.equalsIgnoreCase("mount")))
		{
			final String word=str.toLowerCase();
			if(commands.size()<3)
			{
				commonTelL(mob,"@x1 what jewel onto what item?",CMStrings.capitalizeAndLower(word));
				return false;
			}
			final Item fire=getRequiredFire(mob,autoGenerate);
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			messedUp=false;
			if(fire==null)
				return false;
			final String jewel=commands.get(1);
			final String rest=CMParms.combine(commands,2);
			final Environmental jewelE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,jewel,Wearable.FILTER_UNWORNONLY);
			final Environmental thangE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,rest,Wearable.FILTER_UNWORNONLY);
			if((jewelE==null)||(!CMLib.flags().canBeSeenBy(jewelE,mob)))
			{
				commonTelL(mob,"You don't see any '@x1' here.",jewel);
				return false;
			}
			if((thangE==null)||(!CMLib.flags().canBeSeenBy(thangE,mob)))
			{
				commonTelL(mob,"You don't see any '@x1' here.",rest);
				return false;
			}
			if((!(jewelE instanceof RawMaterial))||(!(jewelE instanceof Item))
			   ||(((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
				  &&((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)))
			{
				commonTelL(mob,"@x1 is not suitable to @x2 on anything.",jewelE.name(),word);
				return false;
			}
			final Item jewelI=CMLib.materials().unbundle((Item)jewelE,1,null);
			if(jewelI==null)
			{
				commonTelL(mob,"@x1 is not pure enough to be @x2ed with.  You will need to use a gathered one.",jewelE.name(),word);
				return false;
			}
			if((!(thangE instanceof Item))
			   ||(!thangE.isGeneric())
			   ||(((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_SYNTHETIC)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)))
			{
				commonTelL(mob,"@x1 is not suitable to be @x2ed on.",thangE.name(),word);
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			buildingI=(Item)thangE;
			beingDone=new Pair<Item,String>(null,"");
			String materialName=RawMaterial.CODES.NAME(jewelI.material()).toLowerCase();
			if(word.equals("encrust"))
			{
				beingDone.second = (CMStrings.capitalizeAndLower(buildingI.name())+" is encrusted with bits of "+materialName+".");
				startStr=L("<S-NAME> start(s) encrusting @x1 with @x2.",buildingI.name(),materialName);
				displayText=L("You are encrusting @x1 with @x2",buildingI.name(),materialName);
				verb=L("encrusting @x1 with bits of @x2",buildingI.name(),materialName);
			}
			else
			{
				materialName=CMLib.english().startWithAorAn(materialName).toLowerCase();
				beingDone.second = (CMStrings.capitalizeAndLower(buildingI.name())+" has "+materialName+" mounted on it.");
				startStr=L("<S-NAME> start(s) mounting @x1 onto @x2.",materialName,buildingI.name());
				displayText=L("You are mounting @x1 onto @x2",materialName,buildingI.name());
				verb=L("mounting @x1 onto @x2",materialName,buildingI.name());
			}
			beingDone.first = jewelI;
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=10;
			final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr);
			if(mob.location().okMessage(mob,msg))
			{
				jewelI.destroy();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
				return true;
			}
			return false;
		}
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null)
				return false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTargetItemFavorMOB(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
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
		if(str.equalsIgnoreCase("refit"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null)
				return false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTargetItemFavorMOB(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(buildingI==null)
				return false;
			if(!mayICraft(mob,buildingI))
				return false;
			if(buildingI.phyStats().height()==0)
			{
				commonTelL(mob,"@x1 is already the right size.",buildingI.name(mob));
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
			beingDone=null;
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			messedUp=false;
			String statue=null;
			if((commands.size()>1)&&(commands.get(commands.size()-1)).startsWith("STATUE="))
			{
				statue=((commands.get(commands.size()-1)).substring(7)).trim();
				if(statue.length()==0)
					statue=null;
				else
					commands.remove(commands.size()-1);
			}
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
			{
				amount=CMath.s_int(commands.get(commands.size()-1));
				commands.remove(commands.size()-1);
			}
			final int[] pm=checkMaterialFrom(mob,commands,new int[]{RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL});
			if(pm==null)
				return false;
			final String recipeName=CMParms.combine(commands,0);
			List<String> foundRecipe=null;
			final List<List<String>> matches=matchingRecipes(recipes,recipeName,false);
			if(matches.size()==0)
				matches.addAll(matchingRecipes(recipes,recipeName,true));
			for(int r=0;r<matches.size();r++)
			{
				final List<String> V=matches.get(r);
				if(V.size()>0)
				{
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					if((autoGenerate>0)||(level<=xlevel(mob)))
					{
						foundRecipe=V;
						recipeLevel=level;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTelL(mob,"You don't know how to make a '@x1'.  Try \"jewel list\" for a list.",recipeName);
				return false;
			}
			misctype=foundRecipe.get(RCP_MISCTYPE);
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			if(!bundling)
			{
				final Item fire=getRequiredFire(mob,autoGenerate);
				if(fire==null)
					return false;
			}
			else
				fireRequired=false;

			final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
			final int[] compData = new int[CF_TOTAL];
			final String realRecipeName=replacePercent(foundRecipe.get(RCP_FINALNAME),"");
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(realRecipeName),autoGenerate,compData,1);
			if(componentsFoundList==null)
				return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);

			if(amount>woodRequired)
				woodRequired=amount;
			final String otherRequired=foundRecipe.get(RCP_EXTRAREQ);
			final int[][] data=fetchFoundResourceData(mob,
													woodRequired,"metal",pm,
													otherRequired.length()>0?1:0,otherRequired,null,
													false,
													autoGenerate,
													enhancedTypes);
			if(data==null)
				return false;
			fixDataForComponents(data,woodRequiredStr,(autoGenerate>0) && (woodRequired==0),componentsFoundList, 1);
			woodRequired=data[0][FOUND_AMT];

			final Session session=mob.session();
			if((misctype.indexOf("STATUE")>=0)
			&&((statue==null)||(statue.trim().length()==0)))
			{
				final Ability me=this;
				final Physical target=givenTarget;
				if((autoGenerate>0)
				||(session==null))
					statue=mob.Name();
				else
				{
					session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
					{
						@Override
						public void showPrompt()
						{
							session.promptPrint(L("What is this item a representation of?\n\r: "));
						}

						@Override

						public void timedOut()
						{
						}

						@Override
						public void callBack()
						{
							final String of=this.input;
							if((of.trim().length()==0)||(of.indexOf('<')>=0))
								return;
							final Vector<String> newCommands=new XVector<String>(originalCommands);
							newCommands.add("STATUE="+of);
							me.invoke(mob, newCommands, target, auto, asLevel);
						}
					});
					return false;
				}
			}

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;

			final MaterialLibrary.DeadResourceRecord deadMats;
			if((componentsFoundList.size() > 0)||(autoGenerate>0))
				deadMats = deadRecord;
			else
			{
				deadMats = CMLib.materials().destroyResources(mob.location(),woodRequired,
						data[0][FOUND_CODE],data[0][FOUND_SUB],data[1][FOUND_CODE],data[1][FOUND_SUB]);
			}
			final MaterialLibrary.DeadResourceRecord deadComps = CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
			final int lostValue=autoGenerate>0?0:(deadMats.getLostValue() + deadComps.getLostValue());
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			final Item buildingI=this.buildingI;
			if(buildingI==null)
			{
				commonTelL(mob,"There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			String itemName=null;
			buildingI.setMaterial(getBuildingMaterial(woodRequired,data,compData));
			if((otherRequired.length()>0)&&(otherRequired.equalsIgnoreCase("PRECIOUS")))
				itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME((data[1][FOUND_CODE]))).toLowerCase();
			else
				itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			if(bundling)
				itemName=CMLib.english().startWithAorAn(woodRequired+"# "+itemName);
			else
			if(!CMLib.english().startsWithAnArticle(itemName))
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) making @x1.",buildingI.name());
			displayText=L("You are making @x1",buildingI.name());
			verb=L("making @x1",buildingI.name());
			playSound="tinktinktink.wav";
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			if((data[1][FOUND_CODE]>0)
			&&(((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			   ||((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
			&&(((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)))
				buildingI.setDescription(L("@x1 made of @x2.",itemName,RawMaterial.CODES.NAME(data[0][FOUND_CODE]).toLowerCase()));
			else
				buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],data[1][FOUND_CODE], bundling));
			if(buildingI.basePhyStats().weight()<=0)
				buildingI.basePhyStats().setWeight(1);
			final int valueAdjust = (woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE])));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+valueAdjust);
			setBrand(mob, buildingI);
			if((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)>0)
				buildingI.setBaseValue(buildingI.baseGoldValue()+RawMaterial.CODES.VALUE(data[1][FOUND_CODE]));
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			//int capacity=CMath.s_int((String)foundRecipe.get(RCP_CAPACITY));
			final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			addSpellsOrBehaviors(buildingI,spell,deadMats.getLostProps(),deadComps.getLostProps());
			if((buildingI instanceof Armor)&&(!(buildingI instanceof FalseLimb)))
			{
				((Armor)buildingI).basePhyStats().setArmor(0);
				if(armordmg!=0)
					((Armor)buildingI).basePhyStats().setArmor(armordmg);
				setWearLocation(buildingI,misctype,0);
			}
			if(buildingI.ID().endsWith("Dice"))
			{
				buildingI.basePhyStats().setAbility(armordmg);
			}
			if((misctype.equalsIgnoreCase("statue"))
			&&(statue!=null)
			&&(statue.trim().length()>0))
			{
				buildingI.setName(L("@x1 of @x2",itemName,statue.trim()));
				buildingI.setDisplayText(L("@x1 of @x2 is here",itemName,statue.trim()));
				buildingI.setDescription(L("@x1 of @x2. ",itemName,statue.trim()));
			}
			if(bundling)
				buildingI.setBaseValue(lostValue);
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
			crafted.add(new CraftedItem(buildingI,null,duration));
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,buildingI,recipeLevel,enhancedTypes);
			return true;
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return false;
	}
}
