package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2020 Bo Zimmerman

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
public class GlassBlowing extends EnhancedCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "GlassBlowing";
	}

	private final static String	localizedName	= CMLib.lang().L("Glass Blowing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "GLASSBLOW", "GLASSBLOWING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "_GLASS|SAND";
	}

	@Override
	public String parametersFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tCONTAINER_TYPE_OR_LIDLOCK||RIDE_BASIS||STATUE||\t"
		+ "CONTAINER_CAPACITY||LIQUID_CAPACITY||MAX_WAND_USES||LIGHT_DURATION||DICE_SIDES\t"
		+ "CODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MISCTYPE	= 6;
	protected static final int	RCP_CAPACITY	= 7;
	protected static final int	RCP_SPELL		= 8;

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
	public String parametersFile()
	{
		return "glassblowing.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
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
					if(messedUp)
					{
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
						else
							commonTell(mob,L("@x1 explodes!",CMStrings.capitalizeAndLower(buildingI.name(mob))));
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
						CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this);
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
		if(I.material()!=RawMaterial.RESOURCE_GLASS)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I instanceof Rideable)
		{
			final Rideable R=(Rideable)I;
			final int rideType=R.rideBasis();
			switch(rideType)
			{
			case Rideable.RIDEABLE_LADDER:
			case Rideable.RIDEABLE_SLEEP:
			case Rideable.RIDEABLE_SIT:
			case Rideable.RIDEABLE_TABLE:
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
			if((W.weaponClassification()!=Weapon.CLASS_BLUNT)
			||((W instanceof AmmunitionWeapon) && ((AmmunitionWeapon)W).requiresAmmunition()))
				return false;
			return true;
		}
		if(I instanceof Light)
			return true;
		if(I instanceof Armor)
			return false;
		if((I instanceof Drink)&&(!(I instanceof Potion)))
			return true;
		if(I instanceof Potion)
			return false;
		if(I instanceof Container)
			return true;
		if(I instanceof FalseLimb)
			return true;
		if(I instanceof Wand)
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
				commonTell(mob,L("That's not a glassblown item."));
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
		final List<String> originalCommands = new XVector<String>(commands);
		if(super.checkStop(mob, commands))
			return true;
		fireRequired=true;

		if(super.checkInfo(mob, commands))
			return true;

		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);

		int recipeLevel=1;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Make what? Enter \"glassblow list\" for a list, \"glassblow info <item>\", \"glassblow learn <item>\" to gain recipes,"
							+ " or \"glassblow stop\" to cancel."));
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
		String statue=null;
		if((commands.size()>1)&&(commands.get(commands.size()-1)).startsWith("STATUE="))
		{
			statue=((commands.get(commands.size()-1)).substring(7)).trim();
			if(statue.length()==0)
				statue=null;
			else
				commands.remove(commands.size()-1);
		}
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final int[] cols={
				CMLib.lister().fixColWidth(29,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 Sand required\n\r",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Lvl"),cols[1])));
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipeNames(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if((level<=xlevel(mob))||allFlag)
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+wood+"\n\r");
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
		final Item fire=getRequiredFire(mob,autoGenerate);
		if(fire==null)
			return false;
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
					recipeLevel=level;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,L("You don't know how to make a '@x1'.  Try \"glassblow list\" for a list.",recipeName));
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
		final int[] pm={RawMaterial.RESOURCE_SAND,RawMaterial.RESOURCE_CRYSTAL,RawMaterial.RESOURCE_GLASS};
		final int[][] data=fetchFoundResourceData(mob,
												woodRequired,"sand",pm,
												0,null,null,
												bundling,
												autoGenerate,
												enhancedTypes);
		if(data==null)
			return false;
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
		if(data[0][FOUND_CODE]==RawMaterial.RESOURCE_SAND)
			buildingI.setMaterial(RawMaterial.RESOURCE_GLASS);
		else
			buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
		String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
		if(bundling)
			itemName="a "+woodRequired+"# "+itemName;
		else
			itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) blowing @x1.",buildingI.name());
		displayText=L("You are blowing @x1",buildingI.name());
		verb=L("blowing @x1",buildingI.name());
		playSound="fire.wav";
		buildingI.setDisplayText(L("@x1 lies here",itemName));
		buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
		buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],data[1][FOUND_CODE], bundling));
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		setBrand(mob, buildingI);
		final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpellsOrBehaviors(buildingI,spell,deadMats.lostProps,deadComps.lostProps);
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
		if(buildingI instanceof Rideable)
		{
			setRideBasis((Rideable)buildingI,misctype);
			if(capacity==0)
				((Rideable)buildingI).setRiderCapacity(1);
			else
			if(capacity<5)
				((Rideable)buildingI).setRiderCapacity(capacity);
		}
		else
		if(buildingI instanceof Light)
		{
			((Light)buildingI).setDuration(capacity);
			if((buildingI instanceof Container)
			&&(!misctype.equals("SMOKE")))
			{
				((Light)buildingI).setDuration((capacity > 200) ? capacity : 200);
				if((buildingI.fitsOn(Wearable.WORN_MOUTH))
				||(((Container)buildingI).containTypes()==Container.CONTAIN_SMOKEABLES))
					((Container)buildingI).setCapacity(((Container)buildingI).basePhyStats().weight()+1);
				else
					((Container)buildingI).setCapacity(0);
			}
		}
		else
		if(buildingI instanceof Container)
		{
			if(capacity>0)
				((Container)buildingI).setCapacity(capacity+woodRequired);
			if(misctype.indexOf("LID")>=0)
				((Container)buildingI).setDoorsNLocks(true,false,true,false,false,false);
			else
			if(misctype.indexOf("LOCK")>=0)
			{
				((Container)buildingI).setDoorsNLocks(true,false,true,true,false,true);
				((Container)buildingI).setKeyName(Double.toString(Math.random()));
			}
			if(!(buildingI instanceof Armor))
				((Container)buildingI).setContainTypes(getContainerType(misctype));
		}
		if(buildingI instanceof Drink)
		{
			if(CMLib.flags().isGettable(buildingI))
			{
				((Drink)buildingI).setLiquidRemaining(0);
				((Drink)buildingI).setLiquidHeld(capacity*50);
				((Drink)buildingI).setThirstQuenched(250);
				if((capacity*50)<250)
					((Drink)buildingI).setThirstQuenched(capacity*50);
			}
		}
		if((buildingI instanceof Wand)
		&&(foundRecipe.get(RCP_CAPACITY).trim().length()>0))
		{
			((Wand)buildingI).setMaxUses(capacity);
		}
		if(buildingI.ID().endsWith("Dice"))
		{
			buildingI.basePhyStats().setAbility(capacity);
		}
		if(bundling)
			buildingI.setBaseValue(lostValue);
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

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
			this.buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,this.buildingI,recipeLevel,enhancedTypes);
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
