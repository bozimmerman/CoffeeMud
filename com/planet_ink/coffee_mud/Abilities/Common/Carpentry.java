package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftedItem;
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
   Copyright 2002-2024 Bo Zimmerman

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
public class Carpentry extends EnhancedCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "Carpentry";
	}

	private final static String	localizedName	= CMLib.lang().L("Carpentry");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CARVE", "CARPENTRY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.General;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN";
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\t"
		+"ITEM_BASE_VALUE\tITEM_CLASS_ID\t"
		+"LID_LOCK||RIDE_BASIS||WEAPON_CLASS||CODED_WEAR_LOCATION||SMOKE_FLAG||STATUE\t"
		+"CONTAINER_CAPACITY||WEAPON_HANDS_REQUIRED||LIQUID_CAPACITY||LIGHT_DURATION||MAX_WAND_USES||DICE_SIDES\t"
		+"BASE_ARMOR_AMOUNT||BASE_DAMAGE||REQ_RESOURCE_OR_MATERIAL\tCONTAINER_TYPE||ATTACK_MODIFICATION||RES_SUBTYPE\tCODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MISCTYPE	= 6;
	protected static final int	RCP_CAPACITY	= 7;
	protected static final int	RCP_ARMORDMG	= 8;
	protected static final int	RCP_CONTAINMASK	= 9;
	protected static final int	RCP_SPELL		= 10;

	protected DoorKey key=null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(buildingI==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String getRecipeFilename()
	{
		return "carpentry.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(getRecipeFilename());
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
							commonEmote(mob,L("<S-NAME> mess(es) up carving @x1.",buildingI.name()));
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
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I instanceof MusicalInstrument)
			return false;
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
		if(I instanceof Shield)
			return true;
		if(I instanceof Weapon)
		{
			final Weapon W=(Weapon)I;
			if(((W.weaponClassification()!=Weapon.CLASS_BLUNT)&&(W.weaponClassification()!=Weapon.CLASS_STAFF))
			||((W instanceof AmmunitionWeapon) && ((AmmunitionWeapon)W).requiresAmmunition()))
				return false;
			return true;
		}
		if(I instanceof Light)
			return true;
		if(I instanceof Ammunition)
			return false;
		if(I instanceof Armor)
			return (isANativeItem(I.Name()));
		if(I instanceof Container)
		{
			final Container C=(Container)I;
			if((C.containTypes()==Container.CONTAIN_CAGED)
			||(C.containTypes()==(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED)))
				return false;
			return true;
		}
		if((I instanceof Drink)&&(!(I instanceof Potion)))
			return true;
		if(I instanceof FalseLimb)
			return true;
		if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
			return true;
		return (isANativeItem(I.Name()));
	}

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
				commonTelL(mob,"That's not a carpentry item.");
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

		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		int recipeLevel = 1;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTelL(mob,"Carve what? Enter \"carve list\" for a list, \"carve info <item>\", \"carve refit <item>\" to resize shoes or armor,"
							+ " \"carve learn <item>\", \"carve scan\", \"carve mend <item>\", or \"carve stop\" to cancel.");
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
		int duration=4;
		bundling=false;
		String statue=null;
		if((commands.size()>1)&&(commands.get(commands.size()-1)).startsWith("STATUE="))
		{
			statue=((commands.get(commands.size()-1)).substring(7)).trim();
			if(statue.length()==0)
				statue=null;
			else
				commands.remove(commands.size()-1);
		}
		final int[] cols={
			CMLib.lister().fixColWidth(28,mob.session()),
			CMLib.lister().fixColWidth(3,mob.session()),
			CMLib.lister().fixColWidth(4,mob.session())
		};
		if(str.equalsIgnoreCase("list") && (autoGenerate <= 0))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(L("Item <S-NAME> <S-IS-ARE> skilled at carving:\n\r"));
			int toggler=1;
			final int toggleTop=2;
			for(int r=0;r<toggleTop;r++)
				buf.append("^H"+(r>0?" ":"")+CMStrings.padRight(L("Item"),cols[0])+" "+CMStrings.padRight(L("Lvl"),cols[1])+" "+CMStrings.padRight(L("Wood"),cols[2]));
			buf.append("^N\n\r");
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipes(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if((level<=xlevel(mob))||allFlag)
					{
						if(wood.length()>5)
						{
							if(toggler>1)
								buf.append("\n\r");
							toggler=toggleTop;
						}
						buf.append("^w"+CMStrings.padRight(item,cols[0])+" ^N"+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRightPreserve(""+wood,cols[2])+((toggler!=toggleTop)?" ":"\n\r"));
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
		else
		if(commands.get(0).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		else
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
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
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTargetItemFavorMOB(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(buildingI==null)
				return false;
			if((buildingI.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			{
				commonTelL(mob,"That's not made of wood.  That can't be refitted.");
				return false;
			}
			if(!(buildingI instanceof Armor))
			{
				commonTelL(mob,"You don't know how to refit that sort of thing.");
				return false;
			}
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
			final int[] pm=checkMaterialFrom(mob,commands,new int[]{RawMaterial.MATERIAL_WOODEN});
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
				commonTelL(mob,"You don't know how to carve a '@x1'.  Try \"carve list\" for a list.",recipeName);
				return false;
			}

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
			final String misctype=foundRecipe.get(RCP_MISCTYPE);
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			final int[][] data=fetchFoundResourceData(mob,
													woodRequired,"wood",pm,
													0,null,null,
													bundling,
													autoGenerate,
													enhancedTypes);
			if(data==null)
				return false;
			fixDataForComponents(data,woodRequiredStr,(autoGenerate>0) && (woodRequired==0),componentsFoundList, 1);
			woodRequired=data[0][FOUND_AMT];
			final Session session=mob.session();
			if((misctype.equalsIgnoreCase("statue"))
			&&(session!=null)
			&&((statue==null)||(statue.trim().length()==0)))
			{
				final Ability me=this;
				final Physical target=givenTarget;
				if(autoGenerate>0)
					statue=mob.Name();
				else
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override
					public void showPrompt()
					{
						session.promptPrint(L("What is this of?\n\r: "));
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
			buildingI.setMaterial(getBuildingMaterial(woodRequired,data,compData));
			String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			if(bundling)
				itemName=CMLib.english().startWithAorAn(woodRequired+"# "+itemName);
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) carving @x1.",buildingI.name());
			displayText=L("You are carving @x1",buildingI.name());
			playSound="sawing.wav";
			verb=L("carving @x1",buildingI.name());
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],data[1][FOUND_CODE], bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
			final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-3;
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL))+(hardness));
			if(buildingI.basePhyStats().level()<1)
				buildingI.basePhyStats().setLevel(1);
			setBrand(mob, buildingI);
			final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
			final String containMask = foundRecipe.get(RCP_CONTAINMASK);
			final long canContain=(containMask.length()>0) ? getContainerType(containMask) : 0;
			final String armorDmgStr = foundRecipe.get(RCP_ARMORDMG);
			final int resourceType=(containMask.length()>0) ? RawMaterial.CODES.FIND_IgnoreCase(armorDmgStr) : -1;
			final int armordmg=CMath.s_int(armorDmgStr);
			if(bundling)
				buildingI.setBaseValue(lostValue);
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			addSpellsOrBehaviors(buildingI,spell,deadMats.getLostProps(),deadComps.getLostProps());
			key=null;
			if((misctype.equalsIgnoreCase("statue"))
			&&(statue!=null)
			&&(statue.trim().length()>0))
			{
				if(buildingI.Name().indexOf('%')>0)
				{
					buildingI.setName(CMStrings.replaceAll(buildingI.Name(), "%", statue.trim()));
					buildingI.setDisplayText(CMStrings.replaceAll(buildingI.displayText(), "%", statue.trim()));
					buildingI.setDescription(CMStrings.replaceAll(buildingI.description(), "%", statue.trim()));
				}
				else
				{
					buildingI.setName(L("@x1 of @x2",itemName,statue.trim()));
					buildingI.setDisplayText(L("@x1 of @x2 is here",itemName,statue.trim()));
					buildingI.setDescription(L("@x1 of @x2. ",itemName,statue.trim()));
				}
				verb=L("making @x1",buildingI.name());
			}
			else
			if((buildingI instanceof Container)
			&&(!(buildingI instanceof Armor)))
			{
				final String[] allTypes=CMParms.parseAny(misctype, "|", true).toArray(new String[0]);
				if(capacity>0)
				{
					((Container)buildingI).setCapacity(capacity+woodRequired);
					((Container)buildingI).setContainTypes(canContain);
				}
				if(CMParms.contains(allTypes, "LID"))
					((Container)buildingI).setDoorsNLocks(true,false,true,false,false,false);
				else
				if(CMParms.contains(allTypes, "LOCK"))
				{
					((Container)buildingI).setDoorsNLocks(true,false,true,true,false,true);
					((Container)buildingI).setKeyName(Double.toString(Math.random()));
					key=(DoorKey)CMClass.getItem("GenKey");
					key.setKey(((Container)buildingI).keyName());
					key.setName(L("a key"));
					key.setDisplayText(L("a small key sits here"));
					key.setDescription(L("looks like a key to @x1",buildingI.name()));
					key.recoverPhyStats();
					setBrand(mob, key);
					key.text();
				}
			}
			if(buildingI.ID().endsWith("Dice"))
			{
				buildingI.basePhyStats().setAbility(capacity);
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
				if(capacity==0)
					((Rideable)buildingI).setRiderCapacity(1);
				else
				if(capacity<5)
					((Rideable)buildingI).setRiderCapacity(capacity);
			}
			if(buildingI instanceof Wand)
			{
				if(foundRecipe.get(RCP_CAPACITY).trim().length()>0)
					((Wand)buildingI).setMaxCharges(capacity);
			}
			else
			if(buildingI instanceof Weapon)
			{
				((Weapon)buildingI).setRawLogicalAnd((capacity>1));
			}
			else
			if((!(buildingI instanceof Container))
			&&(resourceType > 0))
			{
				buildingI.setMaterial(resourceType);
				if(buildingI instanceof RawMaterial)
					((RawMaterial)buildingI).setSubType(containMask);
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
			else
			if(buildingI instanceof Light)
			{
				if((buildingI instanceof Container)
				&&(!misctype.equals("SMOKE")))
				{
					((Light)buildingI).setDuration(200);
					if((buildingI.fitsOn(Wearable.WORN_MOUTH))
					||(((Container)buildingI).containTypes()==Container.CONTAIN_SMOKEABLES))
						((Container)buildingI).setCapacity(((Container)buildingI).basePhyStats().weight()+1);
					else
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
			crafted.add(new CraftedItem(buildingI,key,duration));
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,buildingI,recipeLevel,enhancedTypes);
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
