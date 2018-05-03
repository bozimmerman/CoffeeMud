package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class Weaponsmithing extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	@Override
	public String ID()
	{
		return "Weaponsmithing";
	}

	private final static String	localizedName	= CMLib.lang().L("Weaponsmithing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "WEAPONSMITH", "WEAPONSMITHING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "METAL|MITHRIL";
	}

	protected int displayColumns()
	{
		return 2;
	}

	@Override
	public String parametersFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tWEAPON_CLASS\tWEAPON_TYPE\tBASE_DAMAGE\tATTACK_MODIFICATION\t"
		+"WEAPON_HANDS_REQUIRED||MAX_WAND_USES\tMAXIMUM_RANGE\tOPTIONAL_RESOURCE_OR_MATERIAL\tCODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_WEAPONCLASS	= 6;
	protected static final int	RCP_WEAPONTYPE	= 7;
	protected static final int	RCP_ARMORDMG	= 8;
	protected static final int	RCP_ATTACK		= 9;
	protected static final int	RCP_HANDS		= 10;
	protected static final int	RCP_MAXRANGE	= 11;
	protected static final int	RCP_EXTRAREQ	= 12;
	protected static final int	RCP_SPELL		= 13;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((buildingI==null)
			||(getRequiredFire(mob,0)==null))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	protected boolean doLearnRecipe(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
	}

	@Override
	public String parametersFile()
	{
		return "weaponsmith.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
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
						if(activity == CraftingActivity.LEARNING)
						{
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
							buildingI.destroy();
						}
						else
							commonEmote(mob,L("<S-NAME> mess(es) up smithing @x1.",buildingI.name()));
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( buildingI, recipeHolder );
						buildingI.destroy();
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
						{
							buildingI.setUsesRemaining(100);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.MENDER, 1, this);
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

	protected int specClass(String weaponClass)
	{
		for(int i=0;i<Weapon.CLASS_DESCS.length;i++)
		{
			if(Weapon.CLASS_DESCS[i].equalsIgnoreCase(weaponClass))
				return i;
		}
		return -1;
	}

	protected int specType(String weaponDamageType)
	{
		for(int i=0;i<Weapon.TYPE_DESCS.length;i++)
		{
			if(Weapon.TYPE_DESCS[i].equalsIgnoreCase(weaponDamageType))
				return i;
		}
		return -1;
	}

	protected boolean canDo(String weaponClass, MOB mob)
	{
		if((mob.isMonster())&&(!CMLib.flags().isAnimalIntelligence(mob)))
			return true;

		if(mob.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Commoner"))
			return true;
		String specialization="";
		switch(specClass(weaponClass))
		{
		case Weapon.CLASS_AXE:
			specialization = "Specialization_Axe";
			break;
		case Weapon.CLASS_STAFF:
		case Weapon.CLASS_HAMMER:
		case Weapon.CLASS_BLUNT:
			specialization = "Specialization_BluntWeapon";
			break;
		case Weapon.CLASS_DAGGER:
		case Weapon.CLASS_EDGED:
			specialization = "Specialization_EdgedWeapon";
			break;
		case Weapon.CLASS_FLAILED:
			specialization = "Specialization_FlailedWeapon";
			break;
		case Weapon.CLASS_POLEARM:
			specialization = "Specialization_Polearm";
			break;
		case Weapon.CLASS_SWORD:
			specialization = "Specialization_Sword";
			break;
		case Weapon.CLASS_THROWN:
		case Weapon.CLASS_RANGED:
			specialization = "Specialization_Ranged";
			break;
		default:
			return false;
		}
		if(mob.fetchAbility(specialization)==null)
			return false;
		return true;
	}

	protected boolean masterCraftCheck(final Item I)
	{
		if(I.basePhyStats().level()>30)
			return false;
		if(I.name().toUpperCase().startsWith("MASTER")||(I.name().toUpperCase().indexOf(" MASTER ")>0))
			return false;
		return true;
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
		&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(!masterCraftCheck(I))
			return (isANativeItem(I.Name()));
		if(!(I instanceof Weapon))
			return false;
		final Weapon W=(Weapon)I;
		if((W instanceof AmmunitionWeapon)&&((AmmunitionWeapon)W).requiresAmmunition())
			return false;
		return true;
	}

	@Override
	public boolean supportsMending(Physical I)
	{
		return canMend(null, I, true);
	}

	@Override
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet))
			return false;
		if((!(E instanceof Item))
		||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,L("That's not @x1 item.",CMLib.english().startWithAorAn(Name().toLowerCase())));
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
		fireRequired=true;

		if(super.checkInfo(mob, commands))
			return true;
		
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Make what? Enter \"weaponsmith list\" for a list, \"weaponsmith info <item>\", \"weaponsmith scan\","
						+ " \"weaponsmith learn <item>\", \"weaponsmith mend <item>\", or \"weaponsmith stop\" to cancel."));
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
		bundling=false;
		String startStr=null;
		int duration=4;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(L("Weapons <S-NAME> <S-IS-ARE> skilled at making:\n\r"));
			int toggler=1;
			final int toggleTop=displayColumns();
			final int itemWidth=CMLib.lister().fixColWidth((78/toggleTop)-9,mob.session());
			for(int r=0;r<toggleTop;r++)
				buf.append(L("@x1 Lvl @x2@x3",CMStrings.padRight(L("Item"),itemWidth),CMStrings.padRight(L("Amt"),3),((r<(toggleTop-1)?" ":""))));
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
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
					if(((autoGenerate>0)
						||(((level<=xlevel(mob))||(allFlag))&&((canDo(V.get(RCP_WEAPONCLASS),mob)))))
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,itemWidth)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRightPreserve(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			if(toggler!=1)
				buf.append("\n\r");
			commonEmote(mob,buf.toString());
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
			final Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null)
				return false;
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
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
			activity = CraftingActivity.CRAFTING;
			final Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null)
				return false;
			buildingI=null;
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
					if((autoGenerate>0)
					||((level<=mob.phyStats().level())&&(canDo(V.get(RCP_WEAPONCLASS),mob))))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,L("You don't know how to make a '@x1'.  Try 'list' instead.",recipeName));
				return false;
			}

			final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
			final int[] compData = new int[CF_TOTAL];
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),autoGenerate,compData);
			if(componentsFoundList==null)
				return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);

			if(amount>woodRequired)
				woodRequired=amount;
			final String otherRequired=foundRecipe.get(RCP_EXTRAREQ);
			final int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			bundling=spell.equalsIgnoreCase("BUNDLE");
			final int[][] data=fetchFoundResourceData(mob,
													woodRequired,"metal",pm,
													otherRequired.length()>0?1:0,otherRequired,null,
													false,
													autoGenerate,
													enhancedTypes);
			if(data==null)
				return false;
			fixDataForComponents(data,woodRequiredStr,(autoGenerate>0) && (woodRequired==0),componentsFoundList);
			woodRequired=data[0][FOUND_AMT];

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			final int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],data[1][FOUND_CODE],null)
				+CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			if(buildingI==null)
			{
				commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
			String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(buildingI.material())).toLowerCase();
			itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) smithing @x1.",buildingI.name());
			displayText=L("You are smithing @x1",buildingI.name());
			verb=L("smithing @x1",buildingI.name());
			playSound="tinktinktink2.wav";
			final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-6;
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(itemName+". ");
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],bundling));
			buildingI.setBaseValue((CMath.s_int(foundRecipe.get(RCP_VALUE))/4)+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL))+(hardness*3));
			if(buildingI.basePhyStats().level()<1)
				buildingI.basePhyStats().setLevel(1);
			setBrand(mob, buildingI);
			if(bundling)
				buildingI.setBaseValue(lostValue);
			addSpells(buildingI,spell);
			if(buildingI instanceof Weapon)
			{
				final Weapon w=(Weapon)buildingI;
				w.setWeaponClassification(specClass(foundRecipe.get(RCP_WEAPONCLASS)));
				w.setWeaponDamageType(specType(foundRecipe.get(RCP_WEAPONTYPE)));
				w.setRanges(w.minRange(),CMath.s_int(foundRecipe.get(RCP_MAXRANGE)));
			}
			if(buildingI instanceof Wand)
			{
				if(foundRecipe.get(RCP_HANDS).trim().length()>0)
					((Wand)buildingI).setMaxUses(CMath.s_int(foundRecipe.get(RCP_HANDS).trim()));
			}
			else
			if(CMath.s_int(foundRecipe.get(RCP_HANDS))==2)
				buildingI.setRawLogicalAnd(true);
			buildingI.basePhyStats().setAttackAdjustment(CMath.s_int(foundRecipe.get(RCP_ATTACK))+(hardness*5)+(baseYield()+abilityCode()-1));
			buildingI.basePhyStats().setDamage(CMath.s_int(foundRecipe.get(RCP_ARMORDMG))+hardness);

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
