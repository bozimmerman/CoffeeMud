package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.LeatherWorking.Stage;
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
public class MasterLeatherWorking extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	@Override
	public String ID()
	{
		return "MasterLeatherWorking";
	}

	private final static String	localizedName	= CMLib.lang().L("Master Leather Working");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MASTERLEATHERWORKING", "MLEATHERWORK", "MLEATHERWORKING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.Armor;
	}

	@Override
	public String supportedResourceString()
	{
		return "LEATHER";
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tWEAPON_CLASS||CODED_WEAR_LOCATION\t"
		+"CONTAINER_CAPACITY||LIQUID_CAPACITY||WEAPON_HANDS_REQUIRED||MAX_WAND_USES\tBASE_DAMAGE||BASE_ARMOR_AMOUNT\t"
		+"CONTAINER_TYPE||MIN_MAX_RANGE\tCODED_SPELL_LIST";
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

	@Override
	public String getRecipeFilename()
	{
		return "masterleatherworking.txt";
	}

	public enum Stage
	{
		Designer	 (30, 4,  1.0,  -16, 1.0),
		Cuirboulli    (37, 5,  1.28, -24, 1.15),
		Thick   	 (45, 6,  1.56, -30, 1.3),
		Masterwork   (54, 7,  1.84, -36, 1.45),
		Laminar 	 (63, 8,  2.12, -42, 1.60),
		Battlemoulded(72, 9,  2.40, -48, 1.75),
		Legendary    (80, 10, 2.68, -54, 1.85)
		;
		public final int recipeLevel;
		public final int multiplier;
		public final double damagePct;
		public final int attack;
		public final double armorPct;
		private String term = null;
		private Stage(final int recipeLevel, final int multiplier, final double dmgPct, final int attackAdj, final double armorAdjPct)
		{
			this.recipeLevel=recipeLevel;
			this.multiplier=multiplier;
			this.damagePct=dmgPct;
			this.attack=attackAdj;
			this.armorPct=armorAdjPct;
		}

		public final String term()
		{
			if(term == null)
			{
				switch(this)
				{
				case Designer: term = CMLib.lang().L("Designer"); break;
				case Cuirboulli: term = CMLib.lang().L("Cuirboulli"); break;
				case Thick: term = CMLib.lang().L("Thick"); break;
				case Battlemoulded: term = CMLib.lang().L("Battlemoulded"); break;
				case Laminar: term = CMLib.lang().L("Laminar"); break;
				case Legendary: term = CMLib.lang().L("Legendary"); break;
				case Masterwork: term = CMLib.lang().L("Masterwork"); break;
				default: term = CMLib.lang().L("Normal"); break;
				}
			}
			return term;
		}


		public final static Stage find(final String name)
		{
			final Stage stage = (Stage)CMath.s_valueOf(Stage.class, CMStrings.capitalizeAndLower(name));
			if(stage != null)
				return stage;
			for(final Stage s : Stage.values())
			{
				if(s.term().equalsIgnoreCase(name))
					return s;
			}
			final String uname=name.toUpperCase();
			for(final Stage s : Stage.values())
			{
				final String uterm=s.term().toUpperCase();
				if(uname.startsWith(uterm)
				||(uname.indexOf(" "+uterm+" ")>0))
					return s;
			}
			return null;
		}
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
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I.basePhyStats().level()<31)
			return (isANativeItem(I.Name()));
		if(I instanceof Armor)
		{
			final long noWearLocations=Wearable.WORN_LEFT_FINGER|Wearable.WORN_RIGHT_FINGER|Wearable.WORN_EARS;
			if((I.rawProperLocationBitmap() & noWearLocations)>0)
				return (isANativeItem(I.Name()));
			return true;
		}
		if(I instanceof Rideable)
		{
			final Rideable R=(Rideable)I;
			final Rideable.Basis rideType=R.rideBasis();
			switch(rideType)
			{
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
			if(((W instanceof AmmunitionWeapon)&&((AmmunitionWeapon)W).requiresAmmunition())
			||(W.weaponClassification()==Weapon.CLASS_FLAILED))
				return true;
			return (isANativeItem(I.Name()));
		}
		if(I instanceof Container)
			return true;
		if((I instanceof Drink)&&(!(I instanceof Potion)))
			return true;
		if(I instanceof FalseLimb)
			return true;
		if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
			return true;
		return (isANativeItem(I.Name()));
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
				commonTelL(mob,"That's not a master leatherworked item.");
			return false;
		}
		return true;
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		final String filename=getRecipeFilename();
		@SuppressWarnings("unchecked")
		List<List<String>> recipes=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(recipes==null)
		{
			recipes = new Vector<List<String>>();
			for(final CMFile F : CMFile.getExistingExtendedFiles(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS))
			{
				final StringBuffer str = F.text();
				recipes.addAll(loadList(str));
			}
			if(recipes.size()==0)
				Log.errOut("LeatherWorking","Recipes not found!");
			else
			{
				final List<List<String>> newRecipes=new Vector<List<String>>();
				for(int r=0;r<recipes.size();r++)
				{
					final List<String> V=recipes.get(r);
					if(V.size()>0)
					{
						final String name=V.get(RCP_FINALNAME);
						final int baseLevel=CMath.s_int(V.get(RCP_LEVEL))+2;
						for(final Stage s : Stage.values())
						{
							final List<String> V1=new XVector<String>(V);
							V1.set(RCP_FINALNAME,s.term()+" "+name);
							final int level=baseLevel+s.recipeLevel;
							V1.set(RCP_LEVEL,""+level);
							for(int i=0;i<=newRecipes.size();i++)
							{
								if(newRecipes.size()==i)
								{
									newRecipes.add(V1);
									break;
								}
								else
								if(CMath.s_int(newRecipes.get(i).get(RCP_LEVEL))>level)
								{
									newRecipes.add(i,V1);
									break;
								}
							}
						}
					}
				}
				recipes.clear();
				recipes=newRecipes;
			}
			Collections.sort(recipes,new Comparator<List<String>>()
			{
				@Override
				public int compare(final List<String> o1, final List<String> o2)
				{
					final Integer l1=Integer.valueOf(CMath.s_int(o1.get(RCP_LEVEL)));
					final Integer l2=Integer.valueOf(CMath.s_int(o2.get(RCP_LEVEL)));
					return l1.compareTo(l2);
				}
			});
			Resources.submitResource("PARSED_RECIPE: "+filename,recipes);
		}
		return recipes;
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
		int recipeLevel = 1;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTelL(mob,"Make what? Enter \"mleatherwork list\" for a list, \"mleatherworkd info <item>\", \"mleatherwork refit <item>\" to resize,"
							+ " \"mleatherwork learn <item>\", \"mleatherwork scan\", \"mleatherwork mend <item>\", or \"mleatherwork stop\" to cancel.");
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
		playSound="scissor.wav";
		String startStr=null;
		bundling=false;
		int multiplier=4;
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
			final StringBuffer buf=new StringBuffer("");
			int toggler=1;
			final int toggleTop=2;
			final int[] cols={
				CMLib.lister().fixColWidth(29,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session())
			};
			for(int r=0;r<toggleTop;r++)
				buf.append("^H"+(r>0?" ":"")+CMStrings.padRight(L("Item"),cols[0])+" "+CMStrings.padRight(L("Lvl"),cols[1])+" "+CMStrings.padRight(L("Amt"),cols[2]));
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
						buf.append("^w"+CMStrings.padRight(item,cols[0])+"^N "+CMStrings.padRight(""+(level),cols[1])+" "+CMStrings.padRightPreserve(""+wood,cols[2])+((toggler!=toggleTop)?" ":"\n\r"));
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
			if((buildingI.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)
			{
				commonTelL(mob,"That's not made of leather.  That can't be refitted.");
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
			messedUp=false;
			aborted=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
			{
				amount=CMath.s_int(commands.get(commands.size()-1));
				commands.remove(commands.size()-1);
			}
			final int[] pm=checkMaterialFrom(mob,commands,new int[]{RawMaterial.MATERIAL_LEATHER});
			if(pm==null)
				return false;
			final String recipeName=CMParms.combine(commands,0);
			List<String> foundRecipe=null;
			final List<List<String>> matches=matchingRecipes(recipes,recipeName,false);
			if(matches.size()==0)
				matches.addAll(matchingRecipes(recipes,recipeName,true));
			double bonusDamagePct=1;
			int bonusAttack=0;
			double bonusArmorPct=1;
			Stage foundStage = Stage.Designer;
			for(int r=0;r<matches.size();r++)
			{
				final List<String> V=matches.get(r);
				if(V.size()>0)
				{
					final String name=V.get(RCP_FINALNAME);
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					if(level<=xlevel(mob))
					{
						final Stage stage=Stage.find(name);
						if(stage == null)
							multiplier=1;
						else
						{
							multiplier=stage.multiplier;
							bonusDamagePct=stage.damagePct;
							bonusAttack=stage.attack;
							bonusArmorPct=stage.armorPct;
							foundStage=stage;
						}
						foundRecipe=V;
						recipeLevel=level;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTelL(mob,"You don't know how to make a '@x1'.  Try \"mleatherwork list\" for a list.",recipeName);
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
			final int[] pm1={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			final String misctype=foundRecipe.get(RCP_MISCTYPE);
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			final boolean needsMetal = foundStage==Stage.Battlemoulded;
			final int[][] data=fetchFoundResourceData(mob,
													woodRequired,"leather",pm,
													(needsMetal)?1:0,
													(needsMetal)?"metal":null,
													(needsMetal)?pm1:null,
													bundling,
													autoGenerate,
													enhancedTypes);
			if(data==null)
				return false;
			fixDataForComponents(data,woodRequiredStr,(autoGenerate>0) && (woodRequired==0),componentsFoundList, 1);
			woodRequired=data[0][FOUND_AMT];
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
			duration=getDuration(multiplier*CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,30,4);
			buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
			String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			if(bundling)
				itemName=CMLib.english().startWithAorAn(woodRequired+"# "+itemName);
			else
			if(itemName.endsWith("s"))
				itemName="some "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) making @x1.",buildingI.name());
			displayText=L("You are making @x1",buildingI.name());
			verb=L("making @x1",buildingI.name());
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],data[1][FOUND_CODE], bundling));
			buildingI.setBaseValue((int)Math.round(
					CMath.mul(CMath.s_int(foundRecipe.get(RCP_VALUE)), 1.0+CMath.mul(multiplier-4,.3))));
			setBrand(mob, buildingI);
			final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-2;
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)) + hardness);
			final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
			final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			if(bundling)
				buildingI.setBaseValue(lostValue);
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			addSpellsOrBehaviors(buildingI,spell,deadMats.getLostProps(),deadComps.getLostProps());
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
			if(buildingI instanceof Weapon)
			{
				final String maxRangeStr=foundRecipe.get(RCP_CONTAINMASK);
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
				if(minrange<0)
					((Weapon)buildingI).setRanges(((Weapon)buildingI).minRange(),maxrange);
				else
					((Weapon)buildingI).setRanges(minrange,maxrange);
				((Weapon)buildingI).basePhyStats().setAttackAdjustment(baseYield()+abilityCode()+(hardness*5)+bonusAttack);
				((Weapon)buildingI).setWeaponClassification(Weapon.CLASS_FLAILED);
				setWeaponTypeClass((Weapon)buildingI,misctype,Weapon.TYPE_SLASHING);
				buildingI.basePhyStats().setDamage((int)Math.round(CMath.mul(armordmg, bonusDamagePct))+hardness);
				((Weapon)buildingI).setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
			}
			if((buildingI instanceof Armor)&&(!(buildingI instanceof FalseLimb)))
			{
				final String containStr=foundRecipe.get(RCP_CONTAINMASK);
				final long canContain=getContainerType(containStr);
				if((capacity>0)&&(buildingI instanceof Container))
				{
					((Container)buildingI).setCapacity(capacity+woodRequired + ((multiplier - 1) * 2));
					((Container)buildingI).setContainTypes(canContain);
				}
				((Armor)buildingI).basePhyStats().setArmor(0);
				if(armordmg!=0)
					((Armor)buildingI).basePhyStats().setArmor((int)Math.round(CMath.mul(armordmg, bonusArmorPct))+(baseYield()+abilityCode()-1)+hardness);
				setWearLocation(buildingI,misctype,0);
			}
			if(buildingI instanceof Drink)
			{
				if(CMLib.flags().isGettable(buildingI))
				{
					((Drink)buildingI).setLiquidRemaining(0);
					((Drink)buildingI).setLiquidHeld((capacity + ((multiplier - 1) * 2))*50);
					((Drink)buildingI).setThirstQuenched(250);
					if((capacity*50)<250)
						((Drink)buildingI).setThirstQuenched(capacity*50);
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
