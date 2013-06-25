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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
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
public class Blacksmithing extends EnhancedCraftingSkill implements ItemCraftor
{
	public String ID() { return "Blacksmithing"; }
	public String name(){ return "Blacksmithing";}
	private static final String[] triggerStrings = {"BLACKSMITH","BLACKSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "METAL|MITHRIL";}
	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\t"
	   +"ITEM_BASE_VALUE\tITEM_CLASS_ID\tSTATUE||RIDE_BASIS||CONTAINER_TYPE_OR_LIDLOCK\t"
	   +"CONTAINER_CAPACITY||LIQUID_CAPACITY\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_SPELL=8;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if(fireRequired)
			{
				if((building==null)
				||(getRequiredFire(mob,0)==null))
				{
					messedUp=true;
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public String parametersFile(){ return "blacksmith.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public boolean supportsDeconstruction() { return true; }

	protected boolean doLearnRecipe(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
	}

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
		&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I)) 
			return false;
		if(isANativeItem(I.Name()) && (!(I instanceof Armor)) && (!(I instanceof Weapon)))
			return true;
		if(I instanceof Rideable)
		{
			Rideable R=(Rideable)I;
			int rideType=R.rideBasis();
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
		if(I instanceof DoorKey)
			return true;
		if(I instanceof Shield)
			return false;
		if(I instanceof Weapon)
			return false;
		if(I instanceof Light)
			return true;
		if(I instanceof Armor)
			return false;
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

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+building.name()+".");
						else
							commonTell(mob,"You've ruined "+building.name()+"!");
						building.destroy();
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( building, recipeHolder );
						building.destroy();
					}
					else
						dropAWinner(mob,building);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	public boolean invoke(final MOB mob, Vector commands, Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Vector originalCommands=(Vector)commands.clone();
		if(super.checkStop(mob, commands))
			return true;
		int autoGenerate=0;
		fireRequired=true;
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
			commonTell(mob,"Make what? Enter \""+triggerStrings()[0].toLowerCase()+" list\" for a list, \""+triggerStrings()[0].toLowerCase()+" learn <item>\" to gain recipes, or \""+triggerStrings()[0].toLowerCase()+" stop\" to cancel.");
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
		int duration=4;
		int[] cols={
				ListingLibrary.ColFixer.fixColWidth(16,mob.session()),
				ListingLibrary.ColFixer.fixColWidth(3,mob.session())
			};
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer(CMStrings.padRight("Item",cols[0])+" "+CMStrings.padRight("Lvl",cols[1])+" Metals required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent(V.get(RCP_FINALNAME),"");
					int level=CMath.s_int(V.get(RCP_LEVEL));
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		else
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}

		activity = CraftingActivity.CRAFTING;
		building=null;
		messedUp=false;
		String statue=null;
		if((commands.size()>1)&&((String)commands.lastElement()).startsWith("STATUE="))
		{
			statue=(((String)commands.lastElement()).substring(7)).trim();
			if(statue.length()==0)
				statue=null;
			else
				commands.removeElementAt(commands.size()-1);
		}
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
				if((autoGenerate>0)||(level<=xlevel(mob)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \""+triggerStrings[0].toLowerCase()+" list\" for a list.");
			return false;
		}
		
		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
		if(componentsFoundList==null) return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);
		
		if(amount>woodRequired) woodRequired=amount;
		String misctype=foundRecipe.get(RCP_MISCTYPE);
		int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
		bundling=misctype.equalsIgnoreCase("BUNDLE");
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"metal",pm,
											0,null,null,
											bundling,
											autoGenerate,
											enhancedTypes);
		if(data==null) return false;
		fixDataForComponents(data,componentsFoundList);
		woodRequired=data[0][FOUND_AMT];
		if(!bundling)
		{
			fireRequired=true;
			Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
		}
		else
			fireRequired=false;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int lostValue=autoGenerate>0?0:
			CMLib.materials().destroyResources(mob.location(),data[0][FOUND_AMT],data[0][FOUND_CODE],0,null)
			+CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
		building=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
		if(bundling)
			itemName="a "+woodRequired+"# "+itemName;
		else
			itemName=CMLib.english().startWithAorAn(itemName);
		building.setName(itemName);
		startStr="<S-NAME> start(s) smithing "+building.name()+".";
		displayText="You are smithing "+building.name();
		verb="smithing "+building.name();
		playSound="tinktinktink2.wav";
		building.setDisplayText(itemName+" lies here");
		building.setDescription(itemName+". ");
		building.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
		building.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
		building.setMaterial(data[0][FOUND_CODE]);
		building.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		building.setSecretIdentity(getBrand(mob));
		int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpells(building,spell);
		final Session session=mob.session();
		if((misctype.equalsIgnoreCase("statue"))
		&&((session!=null)||((statue!=null)&&(statue.trim().length()>0))))
		{
			if((statue==null)||(statue.trim().length()==0))
			{
				final Ability me=this;
				final Physical target=givenTarget;
				if(session!=null)
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0){
					@Override public void showPrompt() {session.print("What is this a statue of?\n\r: ");}
					@Override public void timedOut() {}
					@Override public void callBack() {
						String of=this.input;
						if((of.trim().length()==0)||(of.indexOf('<')>=0))
							return;
						Vector newCommands=(Vector)originalCommands.clone();
						newCommands.add("STATUE="+of);
						me.invoke(mob, newCommands, target, auto, asLevel);
					}
				});
				return false;
			}
			else
			{
				building.setName(itemName+" of "+statue.trim());
				building.setDisplayText(itemName+" of "+statue.trim()+" is here");
				building.setDescription(itemName+" of "+statue.trim()+". ");
			}
		}
		else
		if(building instanceof Rideable)
		{
			setRideBasis((Rideable)building,misctype);
			if(capacity==0)
				((Rideable)building).setRiderCapacity(1);
			else
			if(capacity<5)
				((Rideable)building).setRiderCapacity(capacity);
		}
		else
		if(building instanceof Container)
		{
			((Container)building).setCapacity(capacity+woodRequired);
			if(misctype.equalsIgnoreCase("LID"))
				((Container)building).setLidsNLocks(true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)building).setLidsNLocks(true,false,true,false);
				((Container)building).setKeyName(Double.toString(Math.random()));
			}
			else
				((Container)building).setContainTypes(getContainerType(misctype));
		}
		if(building instanceof Drink)
		{
			if(CMLib.flags().isGettable(building))
			{
				((Drink)building).setLiquidHeld(capacity*50);
				((Drink)building).setThirstQuenched(250);
				if((capacity*50)<250)
					((Drink)building).setThirstQuenched(capacity*50);
				((Drink)building).setLiquidRemaining(0);
			}
		}
		if(bundling) building.setBaseValue(lostValue);
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();


		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb="bundling "+RawMaterial.CODES.NAME(building.material()).toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,building,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,building,enhancedTypes);
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
