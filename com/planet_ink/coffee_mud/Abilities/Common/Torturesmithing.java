package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Torturesmithing extends EnhancedCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "Torturesmithing";
	}

	private final static String	localizedName	= CMLib.lang().L("Torturesmithing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TORTURESMITH", "TORTURESMITHING" });

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
		return "METAL|MITHRIL|CLOTH";
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\t"
		+"ITEM_BASE_VALUE\tITEM_CLASS_ID\t"
		+"CONTAINER_TYPE_OR_LIDLOCK_OR_RIDEBASIS||WEAPON_CLASS||CODED_WEAR_LOCATION\t"
		+"CONTAINER_CAPACITY||LIQUID_CAPACITY||MAX_WAND_USES\t"
		+"BASE_ARMOR_AMOUNT\tWOOD_METAL_CLOTH\tCODED_SPELL_LIST";
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
	protected static final int	RCP_MATERIAL	= 9;
	protected static final int	RCP_SPELL		= 10;

	@Override
	public String getRecipeFilename()
	{
		return "torturesmith.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(getRecipeFilename());
	}

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return this.addRecipes(mob, loadRecipes());
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
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
						else
							commonTelL(mob,"You've ruined @x1!",buildingI.name(mob));
						dropALoser(mob,buildingI);
						buildingI.destroy();
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
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return true;
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(!CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I instanceof Ammunition)
			return false;
		if(I instanceof Rideable)
			return true;
		if(I instanceof Shield)
			return false;
		if(I instanceof Weapon)
			return (isANativeItem(I.Name()));
		if(I instanceof Armor)
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
				commonTelL(mob,"That's not a torturesmithing item.");
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
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;

		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTelL(mob,"Make what? Enter \"@x1 list\" for a list, \"@x2 info <item>\", \"@x2 learn <item>\" to gain recipes,"
							+ " or \"@x3 stop\" to cancel.", triggerStrings()[0].toLowerCase(), triggerStrings()[0].toLowerCase(),
							triggerStrings()[0].toLowerCase());
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=commands.get(0);
		String startStr=null;
		bundling=false;
		int duration=4;
		int recipeLevel=1;
		if(str.equalsIgnoreCase("list") && (autoGenerate <= 0))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(L("^H@x1 Lvl Material required^N\n\r",CMStrings.padRight(L("Item"),16)));
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipes(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					String mat=V.get(RCP_MATERIAL);
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5)
						mat="";
					if((level<=xlevel(mob))||allFlag)
						buf.append("^w"+CMStrings.padRight(item,16)+"^N "+CMStrings.padRight(""+level,3)+" "+wood+" "+mat.toLowerCase()+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		else
		if(((commands.get(0))).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}

		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		messedUp=false;
		int amount=-1;
		if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
		{
			amount=CMath.s_int(commands.get(commands.size()-1));
			commands.remove(commands.size()-1);
		}
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
			commonTelL(mob,"You don't know how to make a '@x1'.  Try \"@x2 list\" for a list.",recipeName,triggerStrings[0].toLowerCase());
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
		final String materialtype=foundRecipe.get(RCP_MATERIAL);
		int[] pm=null;
		if(materialtype.equalsIgnoreCase("wood"))
		{
			pm=new int[1];
			pm[0]=RawMaterial.MATERIAL_WOODEN;
		}
		else
		if(materialtype.equalsIgnoreCase("metal"))
		{
			pm=new int[2];
			pm[0]=RawMaterial.MATERIAL_METAL;
			pm[1]=RawMaterial.MATERIAL_MITHRIL;
		}
		else
		if(materialtype.equalsIgnoreCase("cloth"))
		{
			pm=new int[1];
			pm[0]=RawMaterial.MATERIAL_CLOTH;
		}
		bundling=misctype.equalsIgnoreCase("BUNDLE");
		final int[][] data=fetchFoundResourceData(mob,
												woodRequired,"wood or cloth",pm,
												0,null,null,
												bundling,
												autoGenerate,
												enhancedTypes);
		if(data==null)
			return false;
		woodRequired=data[0][FOUND_AMT];
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final MaterialLibrary.DeadResourceRecord deadMats;
		if((componentsFoundList.size() > 0)||(autoGenerate>0))
			deadMats = deadRecord;
		else
		{
			deadMats = CMLib.materials().destroyResources(mob.location(),data[0][FOUND_AMT],
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
		buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
		String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
		if(bundling)
			itemName=CMLib.english().startWithAorAn(woodRequired+"# "+itemName);
		else
			itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) making @x1.",buildingI.name());
		displayText=L("You are making @x1",buildingI.name());
		verb=L("making @x1",buildingI.name());
		playSound="hammer.wav";
		buildingI.setDisplayText(L("@x1 lies here",itemName));
		buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
		buildingI.basePhyStats().setWeight(woodRequired);
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		setBrand(mob, buildingI);
		final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
		final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-3;
		final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpellsOrBehaviors(buildingI,spell,deadMats.getLostProps(),deadComps.getLostProps());
		if(buildingI instanceof Container)
		{
			final String[] allTypes=CMParms.parseAny(misctype, "|", true).toArray(new String[0]);
			((Container)buildingI).setCapacity(capacity+woodRequired);
			if(CMParms.contains(allTypes, "LID"))
				((Container)buildingI).setDoorsNLocks(true,false,true,false,false,false);
			else
			if(CMParms.contains(allTypes, "LOCK"))
			{
				((Container)buildingI).setDoorsNLocks(true,false,true,true,false,true);
				((Container)buildingI).setKeyName(Double.toString(Math.random()));
			}
			else
				((Container)buildingI).setContainTypes(getContainerType(misctype));
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
		if((buildingI instanceof Armor)
		&&(!(buildingI instanceof FalseLimb)))
		{
			((Armor)buildingI).basePhyStats().setArmor(0);
			if(armordmg!=0)
				((Armor)buildingI).basePhyStats().setArmor(armordmg+(baseYield()+abilityCode()-1));
			setWearLocation(buildingI,misctype,hardness);
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
		if((buildingI instanceof Wand)
		&&(foundRecipe.get(RCP_CAPACITY).trim().length()>0))
		{
			((Wand)buildingI).setMaxCharges(capacity);
		}
		if(bundling)
			buildingI.setBaseValue(lostValue);
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);

		if(autoGenerate>0)
		{
			crafted.add(new CraftedItem(buildingI,null,duration));
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			this.buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,this.buildingI,recipeLevel,enhancedTypes);
		}
		return true;
	}
}
