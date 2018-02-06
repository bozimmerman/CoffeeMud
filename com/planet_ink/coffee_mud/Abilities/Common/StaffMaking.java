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
   Copyright 2018-2018 Bo Zimmerman

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
public class StaffMaking extends EnhancedCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "StaffMaking";
	}

	private final static String	localizedName	= CMLib.lang().L("Staff Making");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "STAFFMAKE", "STAFFMAKING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN";
	}

	@Override
	public String parametersFormat()
	{ 
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\t"
		+"ITEM_BASE_VALUE\tITEM_CLASS_ID\tMAX_WAND_USES\tBASE_DAMAGE\t"
		+"OPTIONAL_RESOURCE_OR_MATERIAL\tOPTIONAL_RESOURCE_OR_MATERIAL_AMT\tCODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MAXRANGE	= 6;
	protected static final int	RCP_ARMORDMG	= 7;
	protected static final int	RCP_MAXUSES		= 8;
	protected static final int	RCP_EXTRAREQ	= 9;
	protected static final int	RCP_EXTRAREQAMT	= 10;
	protected static final int	RCP_SPELL		= 11;

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
	public String parametersFile()
	{
		return "staffmaking.txt";
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
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,L("<S-NAME> mess(es) up refitting @x1.",buildingI.name()));
						else
							commonEmote(mob,L("<S-NAME> mess(es) up "+this.getActivePresentTenseVerb()+" @x1.",buildingI.name()));
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
						{
							deconstructRecipeInto( buildingI, recipeHolder );
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
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(!CMParms.contains(getBaseMaterialCodes(), (I.material()&RawMaterial.MATERIAL_MASK)))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(!(I instanceof Weapon))
			return (isANativeItem(I.Name()));
		final Weapon W=(Weapon)I;
		if(W.weaponClassification()==Weapon.CLASS_STAFF)
			return true;
		return false;
	}

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
				commonTell(mob,L("That's not a "+name().toLowerCase()+" item."));
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
	
	protected int getOtherRscAmtRequired(final MOB mob, String req)
	{
		if((req == null)||(req.trim().length()==0))
			return 0;
		return CMath.s_int(req);
	}
	
	protected String getOtherRscRequired(String req)
	{
		if((req == null)||(req.trim().length()==0))
			return "";
		return req;
	}
	
	
	protected String getActivePresentTenseVerb()
	{
		return "making";
	}
	
	protected String getActiveVerb()
	{
		return "make";
	}
	
	protected String getTriggerKeyword()
	{
		return this.triggerStrings()[0].toLowerCase();
	}

	protected String getSoundFile()
	{
		return "sawing.wav";
	}
	
	protected String getBaseMaterialType()
	{
		return "Wood";
	}
	
	protected int[] getBaseMaterialCodes()
	{
		return new int[]{RawMaterial.MATERIAL_WOODEN};
	}
	
	@Override
	protected boolean autoGenInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;
		
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		final String keyWord = getTriggerKeyword();
		if(commands.size()==0)
		{
			commonTell(mob,L(CMStrings.capitalizeAndLower(keyWord)+" what? Enter \""+keyWord+" list\" for a list, \""+keyWord+" info <item>\","
							+ " \""+keyWord+" learn <item>\", \""+keyWord+" scan\", \""+keyWord+" mend <item>\", or \""+keyWord+" stop\" to cancel."));
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
		final int[] cols={
			CMLib.lister().fixColWidth(24,mob.session()),
			CMLib.lister().fixColWidth(3,mob.session()),
			CMLib.lister().fixColWidth(5,mob.session())
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
			final StringBuffer buf=new StringBuffer(L("Item <S-NAME> <S-IS-ARE> skilled at "+getActivePresentTenseVerb()+":\n\r"));
			int toggler=1;
			final int toggleTop=2;
			for(int r=0;r<toggleTop;r++)
				buf.append((r>0?" ":"")+CMStrings.padRight(L("Item"),cols[0])+" "+CMStrings.padRight(L("Lvl"),cols[1])+" "+CMStrings.padRight(L(getBaseMaterialType()),cols[2]));
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
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(
							CMStrings.padRight(item,cols[0])+" "+
							CMStrings.padRight(""+level,cols[1])+" "+
							CMStrings.padRightPreserve(""+wood,cols[2])
							+((toggler!=toggleTop)?" ":"\n\r"));
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
			final Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null)
				return false;
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
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
		{
			Item fire=null;
			if(fireRequired)
			{
				fire=getRequiredFire(mob,autoGenerate);
				if(fire==null)
					return false;
			}
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
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
				commonTell(mob,L("You don't know how to "+getActiveVerb()+" a '@x1'.  Try \""+keyWord+" list\" for a list.",recipeName));
				return false;
			}

			final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
			final int[] compData = new int[CF_TOTAL];
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, this.getActiveVerb()+" "+CMLib.english().startWithAorAn(recipeName),autoGenerate,compData);
			if(componentsFoundList==null)
				return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);

			if((amount>woodRequired)&&(woodRequired>0))
				woodRequired=amount;
			final String otherRequired=getOtherRscRequired(foundRecipe.get(RCP_EXTRAREQ));
			final int otherAmtRequired=getOtherRscAmtRequired(mob,foundRecipe.get(RCP_EXTRAREQAMT));
			final int[] pm=this.getBaseMaterialCodes();
			final int[][] data=fetchFoundResourceData(mob,
													  woodRequired,this.getBaseMaterialType().toLowerCase(),pm,
													  otherAmtRequired,otherRequired,null,
													  false,
													  autoGenerate,
													  enhancedTypes);
			if(data==null)
				return false;
			fixDataForComponents(data,woodRequiredStr,(autoGenerate>0) && (woodRequired==0),componentsFoundList);
			woodRequired=data[0][FOUND_AMT];
			if(((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			||((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
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
					commonTell(mob,L("You'll need to build a fire first."));
					return false;
				}
			}
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			bundling=spell.equalsIgnoreCase("BUNDLE");
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			final int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null)
				+CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			if(buildingI==null)
			{
				commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			buildingI.setMaterial(getBuildingMaterial(woodRequired,data,compData));
			String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(buildingI.material())).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) "+getActivePresentTenseVerb()+" @x1.",buildingI.name());
			displayText=L("You are "+getActivePresentTenseVerb()+" @x1",buildingI.name());
			playSound=getSoundFile();
			verb=L(""+getActivePresentTenseVerb()+" @x1",buildingI.name());
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(itemName+". ");
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
			final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-3;
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL))+(hardness));
			if(buildingI.basePhyStats().level()<1)
				buildingI.basePhyStats().setLevel(1);
			setBrand(mob, buildingI);
			final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			final int maxuses=CMath.s_int(foundRecipe.get(RCP_MAXUSES));
			if(bundling)
				buildingI.setBaseValue(lostValue);
			addSpells(buildingI,spell);
			if((buildingI instanceof Wand)
			&&(foundRecipe.get(RCP_MAXUSES).trim().length()>0))
				((Wand)buildingI).setMaxUses(maxuses);
			if(buildingI instanceof Weapon)
			{
				buildingI.basePhyStats().setAttackAdjustment((baseYield()+abilityCode()+(hardness*5)-1));
				buildingI.basePhyStats().setDamage(armordmg+hardness);
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
