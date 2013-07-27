package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
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
   Copyright 2000-2013 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class LeatherWorking extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	public String ID() { return "LeatherWorking"; }
	public String name(){ return "Leather Working";}
	private static final String[] triggerStrings = {"LEATHERWORK","LEATHERWORKING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "LEATHER";}
	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tWEAPON_CLASS||CODED_WEAR_LOCATION\t"
		+"CONTAINER_CAPACITY||LIQUID_CAPACITY||WEAPON_HANDS_REQUIRED\tBASE_DAMAGE||BASE_ARMOR_AMOUNT\t"
		+"CONTAINER_TYPE\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_CONTAINMASK=9;
	protected static final int RCP_SPELL=10;

	public String parametersFile(){ return "leatherworking.txt";}
	protected List<List<String>> loadRecipes()
	{
		String filename=parametersFile();
		List<List<String>> recipes=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(recipes==null)
		{
			StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS).text();
			recipes=loadList(str);
			if(recipes.size()==0)
				Log.errOut("LeatherWorking","Recipes not found!");
			else
			{
				List<List<String>> pleaseAdd1=new Vector();
				List<List<String>> pleaseAdd2=new Vector();
				for(int r=0;r<recipes.size();r++)
				{
					List<String> V=recipes.get(r);
					if(V.size()>0)
					{
						List<String> V1=new XVector<String>(V);
						List<String> V2=new XVector<String>(V);
						String name=V.get(RCP_FINALNAME);
						V1.set(RCP_FINALNAME,"Hard "+name);
						V1.set(RCP_LEVEL,""+(CMath.s_int(V.get(RCP_LEVEL))+9));
						V2.set(RCP_FINALNAME,"Studded "+name);
						V2.set(RCP_LEVEL,""+(CMath.s_int(V.get(RCP_LEVEL))+18));
						pleaseAdd1.add(V1);
						pleaseAdd2.add(V2);
					}
				}
				for(int i=0;i<pleaseAdd1.size();i++)
					recipes.add(pleaseAdd1.get(i));
				for(int i=0;i<pleaseAdd2.size();i++)
					recipes.add(pleaseAdd2.get(i));
			}
			Resources.submitResource("PARSED_RECIPE: "+filename,recipes);
		}
		return recipes;
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.LEARNING)
						{
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+buildingI.name()+".");
							buildingI.destroy();
						}
						else
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+buildingI.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up making "+buildingI.name()+".");
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
							buildingI.setUsesRemaining(100);
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
							dropAWinner(mob,buildingI);
					}
				}
				buildingI=null;
				activity = CraftingActivity.CRAFTING;
			}
		}
		super.unInvoke();
	}

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I)) 
			return false;
		if(I.basePhyStats().level()>30)
			return (isANativeItem(I.Name()));
		if(I.name().toUpperCase().startsWith("DESIGNER")||(I.name().toUpperCase().indexOf(" DESIGNER ")>0))
			return (isANativeItem(I.Name()));
		if(I instanceof Armor)
		{
			final long noWearLocations=Wearable.WORN_LEFT_FINGER|Wearable.WORN_RIGHT_FINGER|Wearable.WORN_EARS;
			if((I.rawProperLocationBitmap() & noWearLocations)>0)
				return false;
			return true;
		}
		if(I instanceof Rideable)
		{
			Rideable R=(Rideable)I;
			int rideType=R.rideBasis();
			switch(rideType)
			{
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
			Weapon W=(Weapon)I;
			if((W.requiresAmmunition())||(W.weaponClassification()==Weapon.CLASS_FLAILED))
				return true;
			return false;
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

	public boolean supportsMending(Physical item){ return canMend(null,item,true);}
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if((!(E instanceof Item))||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,"That's not a simple leatherworked item.");
			return false;
		}
		return true;
	}

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		int autoGenerate=0;
		if((auto)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		DVector enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"leatherwork list\" for a list, \"leatherwork refit <item>\" to resize, \"leatherwork learn <item>\", \"leatherwork scan\", \"leatherwork mend <item>\", or \"leatherwork stop\" to cancel.");
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
		bundling=false;
		int multiplier=1;
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
			StringBuffer buf=new StringBuffer("");
			int toggler=1;
			int toggleTop=2;
			int[] cols={
					ListingLibrary.ColFixer.fixColWidth(29,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session())
				};
			for(int r=0;r<toggleTop;r++)
				buf.append((r>0?" ":"")+CMStrings.padRight("Item",cols[0])+" "+CMStrings.padRight("Lvl",cols[1])+" "+CMStrings.padRight("Amt",cols[2]));
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent(V.get(RCP_FINALNAME),"");
					int level=CMath.s_int(V.get(RCP_LEVEL));
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5)
					{
						if(toggler>1) buf.append("\n\r");
						toggler=toggleTop;
					}
					if(((level<=xlevel(mob))||allFlag)
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRightPreserve(""+wood,cols[2])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonTell(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		else
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
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
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob,buildingI,false)) return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) mending "+buildingI.name()+".";
			displayText="You are mending "+buildingI.name();
			verb="mending "+buildingI.name();
		}
		else
		if(str.equalsIgnoreCase("refit"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(buildingI==null) return false;
			if((buildingI.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)
			{
				commonTell(mob,"That's not made of leather.  That can't be refitted.");
				return false;
			}
			if(!(buildingI instanceof Armor))
			{
				commonTell(mob,"You don't know how to refit that sort of thing.");
				return false;
			}
			if(buildingI.phyStats().height()==0)
			{
				commonTell(mob,buildingI.name(mob)+" is already the right size.");
				return false;
			}
			activity = CraftingActivity.REFITTING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) refitting "+buildingI.name()+".";
			displayText="You are refitting "+buildingI.name();
			verb="refitting "+buildingI.name();
		}
		else
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			aborted=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber((String)commands.lastElement())))
			{
				amount=CMath.s_int((String)commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			String recipeName=CMParms.combine(commands,0);
			List<String> foundRecipe=null;
			List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
			for(int r=0;r<matches.size();r++)
			{
				List<String> V=matches.get(r);
				if(V.size()>0)
				{
					int level=CMath.s_int(V.get(RCP_LEVEL));
					if(level<=xlevel(mob))
					{
						String name=V.get(RCP_FINALNAME);
						if(name.toUpperCase().startsWith("STUDDED "))
							multiplier=3;
						else
						if(name.toUpperCase().startsWith("HARD "))
							multiplier=2;
						else
							multiplier=1;
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"leatherwork list\" for a list.");
				return false;
			}
			
			final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
			if(componentsFoundList==null) return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);
			
			if(amount>woodRequired) woodRequired=amount;
			int[] pm={RawMaterial.MATERIAL_LEATHER};
			int[] pm1={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			String misctype=foundRecipe.get(RCP_MISCTYPE);
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"leather",pm,
												(multiplier==3)?1:0,
												(multiplier==3)?"metal":null,
												(multiplier==3)?pm1:null,
												bundling,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			fixDataForComponents(data,componentsFoundList);
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],data[1][FOUND_CODE],null)
				+CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			if(buildingI==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			duration=getDuration(multiplier*CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			String itemName=(replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE]))).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
			if(itemName.endsWith("s"))
				itemName="some "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr="<S-NAME> start(s) making "+buildingI.name()+".";
			displayText="You are making "+buildingI.name();
			verb="making "+buildingI.name();
			playSound="scissor.wav";
			buildingI.setDisplayText(itemName+" lies here");
			buildingI.setDescription(itemName+". ");
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))*multiplier);
			buildingI.setMaterial(data[0][FOUND_CODE]);
			buildingI.setSecretIdentity(getBrand(mob));
			int hardness=RawMaterial.CODES.HARDNESS(data[0][FOUND_CODE])-2;
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL))+hardness+((multiplier-1)*9));
			int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
			long canContain=getContainerType(foundRecipe.get(RCP_CONTAINMASK));
			int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			if(armordmg!=0) armordmg=armordmg+(multiplier-1);
			if(bundling) buildingI.setBaseValue(lostValue);
			String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			addSpells(buildingI,spell);
			if(buildingI instanceof Weapon)
			{
				((Weapon)buildingI).basePhyStats().setAttackAdjustment(abilityCode()+(hardness*5)+(abilityCode()-1)-1);
				((Weapon)buildingI).setWeaponClassification(Weapon.CLASS_FLAILED);
				setWeaponTypeClass((Weapon)buildingI,misctype,Weapon.TYPE_SLASHING);
				buildingI.basePhyStats().setDamage(armordmg+hardness);
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
					((Armor)buildingI).basePhyStats().setArmor(armordmg+(abilityCode()-1)+hardness);
				setWearLocation(buildingI,misctype,0);
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
			buildingI.recoverPhyStats();
			buildingI.text();
			buildingI.recoverPhyStats();
		}

		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb="bundling "+RawMaterial.CODES.NAME(buildingI.material()).toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		if(autoGenerate>0)
		{
			commands.addElement(buildingI);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
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
