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
public class Fletching extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	@Override
	public String ID()
	{
		return "Fletching";
	}

	private final static String	localizedName	= CMLib.lang().L("Fletching");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "FLETCH", "FLETCHING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.Weapons;
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
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tAMMO_TYPE\tAMMO_CAPACITY\tBASE_DAMAGE\tMIN_MAX_RANGE\t"
		+"OPTIONAL_RESOURCE_OR_MATERIAL\tCODED_SPELL_LIST\tWEAPON_HANDS_REQUIRED";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_AMMOTYPE	= 6;
	protected static final int	RCP_AMOCAPACITY	= 7;
	protected static final int	RCP_ARMORDMG	= 8;
	protected static final int	RCP_MAXRANGE	= 9;
	protected static final int	RCP_EXTRAREQ	= 10;
	protected static final int	RCP_SPELL		= 11;
	protected static final int	RCP_HANDS		= 12;

	@Override
	public String getRecipeFilename()
	{
		return "fletching.txt";
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
				if((buildingI!=null)&&(!aborted)&&(!helping))
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
						{
							commonEmote(mob,L("<S-NAME> mess(es) up making @x1.",buildingI.name()));
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
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			return false;
		if(I instanceof Ammunition)
			return true;
		if(!(I instanceof Weapon))
			return false;
		if((((Weapon)I).weaponClassification()==Weapon.CLASS_RANGED)
		||(((Weapon)I).weaponClassification()==Weapon.CLASS_THROWN))
			return true;
		return false;
	}

	@Override
	public boolean supportsMending(final Physical item)
	{
		return canMend(null, item, true);
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
				commonTelL(mob,"That's not a @x1 item.",name());
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

	protected int getOtherRscAmtRequired(final MOB mob, final String req)
	{
		if((req == null)||(req.trim().length()==0))
			return 0;
		return 1;
	}

	protected String getOtherRscRequired(final String req)
	{
		if((req == null)||(req.trim().length()==0))
			return "";
		return req;
	}

	protected String commandWord()
	{
		return triggerStrings()[0].toLowerCase();
	}

	protected int getNumberOfColumns()
	{
		return 2;
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto,
								 final int asLevel, final int autoGenerate, final boolean forceLevels, final List<CraftedItem> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;

		final String woodName=(supportedResourceString().indexOf("METAL")>=0)?L("Metal"):L("Wood");
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		int recipeLevel = 1;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTelL(mob,"Make what? Enter \"@x1 list\" for a list, \"@x1 scan\", \"@x1 info <item>\", \"@x1 learn <item>\","
							+ " \"@x1 mend <item>\", or \"@x1 stop\" to cancel.",commandWord());
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
		bundling=false;
		int duration=4;
		if(str.equalsIgnoreCase("list") && (autoGenerate <= 0))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final int toggleTop=getNumberOfColumns();
			int toggler=1;
			final StringBuffer buf=new StringBuffer("^H");
			final int[] cols={
				CMLib.lister().fixColWidth(27,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session()),
				CMLib.lister().fixColWidth(5,mob.session())
			};
			for(int r=0;r<toggleTop;r++)
				buf.append((r>0?" ":"")+CMStrings.padRight(L("Item"),cols[0])+" "
									    +CMStrings.padRight(L("Lvl"),cols[1])+" "
									    +CMStrings.padRight(woodName,cols[2]));
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
						buf.append("^w"+CMStrings.padRight(item,cols[0])+"^N "+
									CMStrings.padRight(""+level,cols[1])+" "+
									CMStrings.padRightPreserve(""+wood,cols[2])+
									((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			buf.append(L("\n\rSome items may require additional material."));
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
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTargetItemFavorMOB(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob,buildingI,false))
				return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr=L("<S-NAME> start(s) mending @x1.",buildingI.name());
			displayText=L("You are mending @x1",buildingI.name());
			verb=L("mending @x1",buildingI.name());
		}
		else
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			aborted=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
			{
				amount=CMath.s_int(commands.get(commands.size()-1));
				commands.remove(commands.size()-1);
			}
			final int[] matCat = (this.supportedResourceString().indexOf("METAL")>=0)?
					new int[]{RawMaterial.MATERIAL_METAL}:new int[]{RawMaterial.MATERIAL_WOODEN};
			final int[] pm=checkMaterialFrom(mob,commands,matCat);
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
				commonFaiL(mob,commands,"You don't know how to make a '@x1'.  Try \"@x2 list\" for a list.",recipeName,commandWord());
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

			if((amount>woodRequired)&&(woodRequired>0))
				woodRequired=amount;
			final String otherRequired=getOtherRscRequired(foundRecipe.get(RCP_EXTRAREQ));
			final int otherAmtRequired=getOtherRscAmtRequired(mob,foundRecipe.get(RCP_EXTRAREQ));
			final int[][] data=fetchFoundResourceData(mob,
													  woodRequired,woodName.toLowerCase(),pm,
													  otherAmtRequired,otherRequired,null,
													  false,
													  autoGenerate,
													  enhancedTypes);
			if(data==null)
				return false;
			fixDataForComponents(data,woodRequiredStr,(autoGenerate>0) && (woodRequired==0),componentsFoundList, 1);
			woodRequired=data[0][FOUND_AMT];
			if(((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			||((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
			{
				Item fire=null;
				if(autoGenerate<=0)
				{
					for(int i=0;i<mob.location().numItems();i++)
					{
						final Item I2=mob.location().getItem(i);
						if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
						{
							fire=I2;
							break;
						}
					}
					if((fire==null)||(!mob.location().isContent(fire)))
					{
						commonFaiL(mob,commands,"You'll need to build a fire first.");
						return false;
					}
				}
			}
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			bundling=spell.equalsIgnoreCase("BUNDLE");
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
				commonFaiL(mob,commands,"There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			buildingI.setMaterial(getBuildingMaterial(woodRequired,data,compData));
			final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-3;
			String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) making @x1.",buildingI.name());
			displayText=L("You are making @x1",buildingI.name());
			verb=L("making @x1",buildingI.name());
			playSound="sanding.wav";
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired + compData[CF_AMOUNT],data[1][FOUND_CODE], bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
			final int level=CMath.s_int(foundRecipe.get(RCP_LEVEL));
			buildingI.basePhyStats().setLevel(level+hardness);
			setBrand(mob, buildingI);
			final String ammotype=foundRecipe.get(RCP_AMMOTYPE);
			final int capacity=CMath.s_int(foundRecipe.get(RCP_AMOCAPACITY));
			final String maxRangeStr=foundRecipe.get(RCP_MAXRANGE);
			final int maxrange;
			final int minrange;
			if(maxRangeStr.indexOf(',')>0)
			{
				minrange=CMath.s_int(maxRangeStr.substring(0,maxRangeStr.indexOf(',')).trim());
				maxrange=CMath.s_int(maxRangeStr.substring(maxRangeStr.indexOf(',')+1).trim());
			}
			else
			{
				minrange=-1;
				maxrange=CMath.s_int(maxRangeStr);
			}
			final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			if(bundling)
				buildingI.setBaseValue(lostValue);
			addSpellsOrBehaviors(buildingI,spell,deadMats.getLostProps(),deadComps.getLostProps());
			if(buildingI instanceof Weapon)
			{
				if(buildingI instanceof AmmunitionWeapon)
				{
					if(ammotype.length()>0)
					{
						((AmmunitionWeapon)buildingI).setAmmoCapacity(capacity);
						((AmmunitionWeapon)buildingI).setAmmoRemaining(0);
						if(!ammotype.equalsIgnoreCase("nada"))
							((AmmunitionWeapon)buildingI).setAmmunitionType(ammotype);
					}
				}
				buildingI.basePhyStats().setAttackAdjustment((baseYield()+abilityCode()-1+(hardness*5)));
				buildingI.basePhyStats().setDamage(armordmg+hardness);
				if(minrange<0)
					((Weapon)buildingI).setRanges(((Weapon)buildingI).minRange(),maxrange);
				else
					((Weapon)buildingI).setRanges(minrange,maxrange);
			}
			else
			if((ammotype.length()>0)
			&&(buildingI instanceof Ammunition))
			{
				if(!ammotype.equalsIgnoreCase("nada"))
					((Ammunition)buildingI).setAmmunitionType(ammotype);
				((Ammunition)buildingI).setAmmoRemaining(capacity);
			}
			if(buildingI.subjectToWearAndTear())
				buildingI.setUsesRemaining(100);
			final int hands=foundRecipe.size()>RCP_HANDS?CMath.s_int(foundRecipe.get(RCP_HANDS)):0;
			buildingI.setRawLogicalAnd((hands==1)?false:(hands==2)?true:buildingI.rawLogicalAnd());
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
