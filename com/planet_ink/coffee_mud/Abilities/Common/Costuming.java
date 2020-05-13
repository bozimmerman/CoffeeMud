package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2020 Bo Zimmerman

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
public class Costuming extends CraftingSkill implements ItemCraftor, MendingSkill
{
	@Override
	public String ID()
	{
		return "Costuming";
	}

	private final static String	localizedName	= CMLib.lang().L("Costuming");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "COSTUME", "COSTUMING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "PAPER";
	}

	@Override
	public String parametersFormat()
	{
		return
		  "ITEM_NAME\t"
		+ "ITEM_LEVEL\t"
		+ "BUILD_TIME_TICKS\t"
		+ "MATERIALS_REQUIRED\t"
		+ "ITEM_BASE_VALUE\t"
		+ "ITEM_CLASS_ID\t"
		+ "WEAPON_CLASS||CODED_WEAR_LOCATION||RIDE_BASIS\t"
		+ "CONTAINER_CAPACITY||WEAPON_HANDS_REQUIRED\t"
		+ "BASE_ARMOR_AMOUNT||BASE_DAMAGE\t"
		+ "CONTAINER_TYPE\t"
		+ "CODED_SPELL_LIST";
	}

	// protected static final int RCP_FINALNAME=0;
	// protected static final int RCP_LEVEL=1;
	// protected static final int RCP_TICKS=2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MISCTYPE	= 6;
	protected static final int	RCP_CAPACITY	= 7;
	protected static final int	RCP_ARMORDMG	= 8;
	protected static final int	RCP_CONTAINMASK	= 9;
	protected static final int	RCP_SPELL		= 10;

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return this.addRecipes(mob, loadRecipes());
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(buildingI==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String parametersFile()
	{
		return "costume.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return true;
	}

	@Override
	public double getItemWeightMultiplier(final boolean bundling)
	{
		return bundling ? 1.0 : 0.5;
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
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
						else
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,L("<S-NAME> mess(es) up refitting @x1.",buildingI.name()));
						else
							commonEmote(mob,L("<S-NAME> mess(es) up knitting @x1.",buildingI.name()));
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
						{
							buildingI.setUsesRemaining(100);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.MENDER, 1, this);
						}
						else
						if(activity==CraftingActivity.LEARNING)
							deconstructRecipeInto(mob, buildingI, recipeHolder );
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
	protected boolean deconstructRecipeInto(final MOB mob, final Item I, final Recipe R)
	{

		if((I==null)||(R==null))
			return false;
		if(!(this instanceof ItemCraftor))
			return false;
		final List<String> existingRecipes=new XVector<String>(R.getRecipeCodeLines());
		if(R.getTotalRecipePages() <=existingRecipes.size())
			return false;
		final CMMsg msg=CMClass.getMsg(mob,I,this,CMMsg.TYP_RECIPELEARNED|CMMsg.MASK_ALWAYS,null);
		setMsgXPValue(mob,msg);
		if((mob!=null)
		&&(mob.location()!=null)
		&&(mob.location().okMessage(mob, msg)))
		{
			mob.location().send(mob, msg);
			final StringBuilder str=new StringBuilder("");
			str.append(I.Name()).append("\t");
			str.append(I.basePhyStats().level()).append("\t");
			str.append(5+(I.basePhyStats().level()/5)).append("\t");
			int wood=0;
			for(final long code : Wearable.CODES.ALL())
			{
				if(I.fitsOn(code))
				{
					if (code == Wearable.WORN_ABOUT_BODY)
						wood += 4;
					else
					if (code == Wearable.WORN_TORSO)
						wood += 3;
					else
					if((code == Wearable.WORN_ARMS)
					|| (code == Wearable.WORN_LEGS)
					|| (code == Wearable.WORN_BACK))
						wood += 2;
					else
						wood += 1;
				}
			}
			str.append(wood).append("\t");
			str.append(0).append("\t");
			str.append(I.ID()).append("\t");
			if(I instanceof Armor)
			{
				str.append(((Armor)I).getClothingLayer()+10).append(":");
				final String connector = ((Armor)I).rawLogicalAnd() ? "&&" : "||";
				boolean didAny=false;
				for(final long code : Wearable.CODES.ALL())
				{
					if(I.fitsOn(code))
					{
						str.append(Wearable.CODES.NAMEUP(code)).append(connector);
						didAny=true;
					}
				}
				if(didAny)
					str.setLength(str.length()-2);
			}
			str.append("\t");
			str.append("0\t");
			str.append("0\t");
			str.append("0\t");
			existingRecipes.add(str.toString());
			R.setRecipeCodeLines(existingRecipes.toArray(new String[0]));
			R.setCommonSkillID( ID() );
			return true;
		}
		return false;
	}


	protected boolean masterCraftCheck(final Item I)
	{
		if(I.basePhyStats().level()>31)
			return false;
		return true;
	}

	@Override
	protected boolean mayILearnToCraft(final MOB mob, final Item I)
	{
		return (I instanceof Armor) || ((I!=null) && isANativeItem(I.Name()));
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I.basePhyStats().weight()>50)
			return false;
		if(I instanceof Armor)
		{
			if(I.basePhyStats().armor()>0)
				return false;
			if(CMath.bset(((Armor)I).getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH))
				return false;
			if(((Armor)I).getClothingLayer()<10)
				return false;
			if(!masterCraftCheck(I))
				return false;
			return true;
		}
		return isANativeItem(I.Name());
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
		if((!(E instanceof Item))||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,L("That's not a @x1 item.",CMLib.english().startWithAorAn(Name().toLowerCase())));
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
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(0));
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto,
								 	final int asLevel, final int autoGenerate, final boolean forceLevels, final List<Item> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Costume what? Enter \"costume list\" for a list, \"costume info <item>\", \"costume refit <item>\" to resize,"
					+ " \"costume learn <item>\", \"costume scan\", \"costume mend <item>\", or \"costume stop\" to cancel."));
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
		final int[] cols=
		{
			CMLib.lister().fixColWidth(27,mob.session()),
			CMLib.lister().fixColWidth(3,mob.session()),
			CMLib.lister().fixColWidth(6,mob.session())
		};
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer("");
			int toggler=1;
			final int toggleTop=2;
			for(int r=0;r<toggleTop;r++)
				buf.append((r>0?" ":"")+CMStrings.padRight(L("Item"),cols[0])+" "+CMStrings.padRight(L("Lvl"),cols[1])+" "+CMStrings.padRight(L("Paper Bolts"),cols[2]));
			buf.append("\n\r");
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipeNames(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5)
					{
						if(toggler>1)
							buf.append("\n\r");
						toggler=toggleTop;
					}
					if((level<=xlevel(mob))||allFlag)
					{
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRightPreserve(""+wood,cols[2])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			if(toggler!=1)
				buf.append("\n\r");
			commonTell(mob,buf.toString());
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
		if(str.equalsIgnoreCase("refit"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTargetItemFavorMOB(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(buildingI==null)
				return false;
			if((buildingI.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
			{
				commonTell(mob,L("That's not made of paper.  It can't be refitted."));
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
			messedUp=false;
			aborted=false;
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
				commonTell(mob,L("You don't know how to make a '@x1' costume. Have you LEARNed any recipes yet? Try \"@x2 list\" for a list.",recipeName,triggerStrings()[0].toLowerCase()));
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
			final int[] pm= null;
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			final int[][] data=fetchFoundResourceData(mob,
													  woodRequired,"paper",pm,
													  0,null,null,
													  bundling,
													  autoGenerate,
													  null);
			if(data==null)
				return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			final MaterialLibrary.DeadResourceRecord deadMats;
			if((componentsFoundList.size() > 0)||(autoGenerate>0))
				deadMats = new MaterialLibrary.DeadResourceRecord();
			else
			{
				deadMats = CMLib.materials().destroyResources(mob.location(),woodRequired,
						data[0][FOUND_CODE],data[0][FOUND_SUB],data[1][FOUND_CODE],data[1][FOUND_SUB]);
			}
			final MaterialLibrary.DeadResourceRecord deadComps = CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
			final int lostValue=autoGenerate>0?0:(deadMats.lostValue + deadComps.lostValue);
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			final Item buildingI=this.buildingI;
			if(buildingI==null)
			{
				commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			buildingI.setMaterial(getBuildingMaterial(woodRequired,data,compData));
			String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) making @x1.",buildingI.name());
			displayText=L("You are making @x1",buildingI.name());
			playSound="scissor.wav";
			verb=L("making @x1",buildingI.name());
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],data[1][FOUND_CODE], bundling));
			final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-1;
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+deadMats.lostValue+deadComps.lostValue);
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			setBrand(mob, buildingI);
			final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
			final long canContain=getContainerType(foundRecipe.get(RCP_CONTAINMASK));
			final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			if(bundling)
				buildingI.setBaseValue(lostValue);
			addSpellsOrBehaviors(buildingI,spell,deadMats.lostProps,deadComps.lostProps);
			if(buildingI instanceof Weapon)
			{
				((Weapon)buildingI).setWeaponClassification(Weapon.CLASS_NATURAL);
				setWeaponTypeClass((Weapon)buildingI,misctype);
				buildingI.basePhyStats().setDamage(armordmg);
				((Weapon)buildingI).setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
				((Weapon)buildingI).setRawLogicalAnd((capacity>1));
			}
			if((buildingI instanceof Armor)&&(!(buildingI instanceof FalseLimb)))
			{
				if((capacity>0)&&(buildingI instanceof Container))
				{
					((Container)buildingI).setCapacity(capacity+woodRequired);
					((Container)buildingI).setContainTypes(canContain);
				}
				((Armor)buildingI).basePhyStats().setArmor(0);
				if(armordmg!=0)
					((Armor)buildingI).basePhyStats().setArmor(armordmg+(baseYield()+abilityCode()-1)+hardness);
				setWearLocation(buildingI,misctype,0);
			}
			if(buildingI instanceof Rideable)
			{
				setRideBasis((Rideable)buildingI,misctype);
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
			crafted.add(buildingI);
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
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
