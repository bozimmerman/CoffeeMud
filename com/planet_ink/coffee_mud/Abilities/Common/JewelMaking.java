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
public class JewelMaking extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	public String ID() { return "JewelMaking"; }
	public String name(){ return "Jewel Making";}
	private static final String[] triggerStrings = {"JEWEL","JEWELMAKING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "GLASS|PRECIOUS|SAND";}
	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tSTATUE||CODED_WEAR_LOCATION\tN_A\tBASE_ARMOR_AMOUNT\tOPTIONAL_RESOURCE_OR_MATERIAL\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	//private static final int RCP_CAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_EXTRAREQ=9;
	protected static final int RCP_SPELL=10;

	protected Vector beingDone=null;

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

	public String parametersFile(){ return "jewelmaking.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	protected boolean doLearnRecipe(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
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
					if((beingDone!=null)&&(beingDone.size()>=2))
					{
						if(messedUp)
							commonEmote(mob,"<S-NAME> mess(es) up "+verb+".");
						else
						{
							Item I=(Item)beingDone.elementAt(1);
							building.setBaseValue(building.baseGoldValue()+(I.baseGoldValue()*2));
							building.setDescription(building.description()+" "+(String)beingDone.elementAt(0));
						}
						beingDone=null;
					}
					else
					if(messedUp)
					{
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.LEARNING)
						{
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+building.name()+".");
							building.destroy();
						}
						else
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up "+verb+".");
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
							building.setUsesRemaining(100);
						else
						if(activity==CraftingActivity.LEARNING)
						{
							deconstructRecipeInto( building, recipeHolder );
							building.destroy();
						}
						else
						if(activity == CraftingActivity.REFITTING)
						{
							building.basePhyStats().setHeight(0);
							building.recoverPhyStats();
						}
						else
							dropAWinner(mob,building);
					}
				}
				building=null;
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
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I)) 
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
		{
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
			else
			if(I instanceof Armor)
			{
				if(I.fitsOn(Armor.WORN_EARS)
				||I.fitsOn(Armor.WORN_EYES)
				||I.fitsOn(Armor.WORN_HEAD)
				||I.fitsOn(Armor.WORN_NECK)
				||I.fitsOn(Armor.WORN_FEET)
				||I.fitsOn(Armor.WORN_LEFT_FINGER)
				||I.fitsOn(Armor.WORN_RIGHT_FINGER)
				||I.fitsOn(Armor.WORN_LEFT_WRIST)
				||I.fitsOn(Armor.WORN_RIGHT_WRIST))
					return true;
				return (isANativeItem(I.Name()));
			}
			if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
				return true;
			return true;
		}
		else
		if((I instanceof Armor)
		&&(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)))
		{
			Armor A=(Armor)I;
			if((CMath.bset(A.getLayerAttributes(), Armor.LAYERMASK_SEETHROUGH))
			&&(A.basePhyStats().armor()<3))
				return true;
			if((A.basePhyStats().armor()<2)
			&&(I.fitsOn(Armor.WORN_EARS)
				||I.fitsOn(Armor.WORN_EYES)
				||I.fitsOn(Armor.WORN_HEAD)
				||I.fitsOn(Armor.WORN_NECK)
				||I.fitsOn(Armor.WORN_FEET)
				||I.fitsOn(Armor.WORN_LEFT_FINGER)
				||I.fitsOn(Armor.WORN_RIGHT_FINGER)
				||I.fitsOn(Armor.WORN_LEFT_WRIST)
				||I.fitsOn(Armor.WORN_RIGHT_WRIST)))
					return true;
			return (isANativeItem(I.Name()));
		}
		return (isANativeItem(I.Name()));
	}

	public boolean supportsMending(Physical I){ return canMend(null,I,true);}
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if((!(E instanceof Item))
		||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,"That's not an jewelworked item.");
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
			commonTell(mob,"Make what? Enter \"jewel list\" for a list.  You may also enter jewel encrust <gem name> <item name>, jewel mount <gem name> <item name>, jewel refit <item name>, jewel learn <item>, jewel scan, jewel mend <item name>, or jewel stop to cancel.");
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
		fireRequired=true;
		bundling=false;
		int duration=4;
		String misctype="";
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			int toggler=1;
			int toggleTop=2;
			StringBuffer buf=new StringBuffer("");
			int[] cols={
					ListingLibrary.ColFixer.fixColWidth(27,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(5,mob.session())
				};
			for(int r=0;r<toggleTop;r++)
				buf.append((r>0?" ":"")+CMStrings.padRight("Item",cols[0])+" "+CMStrings.padRight("Lvl",cols[1])+" "+CMStrings.padRight("Metal",cols[2]));
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.get(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5)
					{
						if(toggler>1) buf.append("\n\r");
						toggler=toggleTop;
					}
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRightPreserve(""+wood,cols[2])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
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
		else
		if((str.equalsIgnoreCase("encrust"))||(str.equalsIgnoreCase("mount")))
		{
			String word=str.toLowerCase();
			if(commands.size()<3)
			{
				commonTell(mob,CMStrings.capitalizeAndLower(word)+" what jewel onto what item?");
				return false;
			}
			Item fire=getRequiredFire(mob,autoGenerate);
			building=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			messedUp=false;
			if(fire==null) return false;
			final String jewel=(String)commands.elementAt(1);
			final String rest=CMParms.combine(commands,2);
			final Environmental jewelE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,jewel,Wearable.FILTER_UNWORNONLY);
			final Environmental thangE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,rest,Wearable.FILTER_UNWORNONLY);
			if((jewelE==null)||(!CMLib.flags().canBeSeenBy(jewelE,mob)))
			{ 
				commonTell(mob,"You don't see any '"+jewel+"' here."); 
				return false;
			}
			if((thangE==null)||(!CMLib.flags().canBeSeenBy(thangE,mob)))
			{ 
				commonTell(mob,"You don't see any '"+rest+"' here."); 
				return false;
			}
			if((!(jewelE instanceof RawMaterial))||(!(jewelE instanceof Item))
			   ||(((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
				  &&((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)))
			{ 
				commonTell(mob,"A "+jewelE.name()+" is not suitable to "+word+" on anything."); 
				return false;
			}
			final Item jewelI=(Item)CMLib.materials().unbundle((Item)jewelE,1,null);
			if(jewelI==null)
			{
				commonTell(mob,jewelE.name()+" is not pure enough to be "+word+"ed with.  You will need to use a gathered one.");
				return false;
			}
			if((!(thangE instanceof Item))
			   ||(!thangE.isGeneric())
			   ||(((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_SYNTHETIC)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)))
			{ 
				commonTell(mob,"A "+thangE.name()+" is not suitable to be "+word+"ed on."); 
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			building=(Item)thangE;
			beingDone=new Vector();
			String materialName=RawMaterial.CODES.NAME(jewelI.material()).toLowerCase();
			if(word.equals("encrust"))
			{
				beingDone.addElement(CMStrings.capitalizeAndLower(building.name())+" is encrusted with bits of "+materialName+".");
				startStr="<S-NAME> start(s) encrusting "+building.name()+" with "+materialName+".";
				displayText="You are encrusting "+building.name()+" with "+materialName;
				verb="encrusting "+building.name()+" with bits of "+materialName;
			}
			else
			{
				materialName=CMLib.english().startWithAorAn(materialName).toLowerCase();
				beingDone.addElement(CMStrings.capitalizeAndLower(building.name())+" has "+materialName+" mounted on it.");
				startStr="<S-NAME> start(s) mounting "+materialName+" onto "+building.name()+".";
				displayText="You are mounting "+materialName+" onto "+building.name();
				verb="mounting "+materialName+" onto "+building.name();
			}
			beingDone.addElement(jewelI);
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=10;
			CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr);
			if(mob.location().okMessage(mob,msg))
			{
				jewelI.destroy();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
				return true;
			}
			return false;
		}
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob, building,false)) return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();
		}
		else
		if(str.equalsIgnoreCase("refit"))
		{
			building=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(building==null) return false;
			if(!mayICraft(mob,building))
				return false;
			if(building.phyStats().height()==0)
			{
				commonTell(mob,building.name()+" is already the right size.");
				return false;
			}
			activity = CraftingActivity.REFITTING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) refitting "+building.name()+".";
			displayText="You are refitting "+building.name();
			verb="refitting "+building.name();
		}
		else
		{
			beingDone=null;
			building=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			messedUp=false;
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
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					if((autoGenerate>0)||(level<=xlevel(mob)))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"jewel list\" for a list.");
				return false;
			}
			misctype=(String)foundRecipe.get(RCP_MISCTYPE);
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			if(!bundling)
			{
				Item fire=getRequiredFire(mob,autoGenerate);
				if(fire==null) return false;
			}
			else
				fireRequired=false;
			
			final String woodRequiredStr = (String)foundRecipe.get(RCP_WOOD);
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
			if(componentsFoundList==null) return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);
			
			if(amount>woodRequired) woodRequired=amount;
			String otherRequired=(String)foundRecipe.get(RCP_EXTRAREQ);
			int[] pm={RawMaterial.MATERIAL_MITHRIL,RawMaterial.MATERIAL_METAL};
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"metal",pm,
												otherRequired.length()>0?1:0,otherRequired,null,
												false,
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
			building=CMClass.getItem((String)foundRecipe.get(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			duration=getDuration(CMath.s_int((String)foundRecipe.get(RCP_TICKS)),mob,CMath.s_int((String)foundRecipe.get(RCP_LEVEL)),4);
			String itemName=null;
			if((otherRequired!=null)&&(otherRequired.length()>0)&&(otherRequired.equalsIgnoreCase("PRECIOUS")))
				itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME((data[1][FOUND_CODE]))).toLowerCase();
			else
				itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
			playSound="tinktinktink.wav";
			building.setDisplayText(itemName+" lies here");
			if((data[1][FOUND_CODE]>0)
			&&(((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			   ||((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
			&&(((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)))
				building.setDescription(itemName+" made of "+RawMaterial.CODES.NAME(data[0][FOUND_CODE]).toLowerCase()+".");
			else
				building.setDescription(itemName+". ");
			building.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
			building.setBaseValue(CMath.s_int((String)foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
			building.setSecretIdentity(getBrand(mob));
			if(data[1][FOUND_CODE]==0)
				building.setMaterial(data[0][FOUND_CODE]);
			else
			{
				building.setMaterial(data[1][FOUND_CODE]);
				building.setBaseValue(building.baseGoldValue()+RawMaterial.CODES.VALUE(data[1][FOUND_CODE]));
			}
			building.basePhyStats().setLevel(CMath.s_int((String)foundRecipe.get(RCP_LEVEL)));
			//int capacity=CMath.s_int((String)foundRecipe.get(RCP_CAPACITY));
			int armordmg=CMath.s_int((String)foundRecipe.get(RCP_ARMORDMG));
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.get(RCP_SPELL)).trim():"";
			addSpells(building,spell);
			if((building instanceof Armor)&&(!(building instanceof FalseLimb)))
			{
				((Armor)building).basePhyStats().setArmor(0);
				if(armordmg!=0)
					((Armor)building).basePhyStats().setArmor(armordmg);
				setWearLocation(building,misctype,0);
			}
			if((misctype.equalsIgnoreCase("statue"))&&(!mob.isMonster()))
			{
				String of="";
				try
				{
					of=mob.session().prompt("What is this item a representation of?","");
					if((of.trim().length()==0)||(of.indexOf('<')>=0))
						return false;
				}
				catch(java.io.IOException x)
				{
					return false;
				}
				of=of.trim();
				if(of.startsWith("of "))
					of=of.substring(3).trim();
				building.setName(itemName+" of "+of);
				building.setDisplayText(itemName+" of "+of+" is here");
				building.setDescription(itemName+" of "+of+". ");
			}
			if(bundling) building.setBaseValue(lostValue);
			building.recoverPhyStats();
			building.text();
			building.recoverPhyStats();
		}


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

		CMMsg msg=CMClass.getMsg(mob,building,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,building,enhancedTypes);
			return true;
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return false;
	}
}
