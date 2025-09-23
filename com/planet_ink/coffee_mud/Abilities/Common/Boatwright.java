package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.Rideable.Basis;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2025 Bo Zimmerman

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
public class Boatwright extends CraftingSkill implements ItemCraftor, MendingSkill
{
	@Override
	public String ID()
	{
		return "Boatwright";
	}

	private final static String	localizedName	= CMLib.lang().L("Boatwrighting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BOATWRIGHTING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.LargeConstructions;
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
		+"ITEM_CLASS_ID\tRIDE_BASIS\tRIDE_CAPACITY\tCONTAINER_CAPACITY\t"
		+"RIDE_OVERRIDE_STRS\tCODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MISCTYPE	= 6;
	protected static final int	RCP_CAPACITY	= 7;
	protected static final int	RCP_CONTAINMASK	= 8;
	protected static final int	RCP_STRINGS		= 9;
	protected static final int	RCP_SPELL		= 10;

	protected Item key=null;

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return this.addRecipes(mob, loadRecipes());
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
	public String getRecipeFilename()
	{
		return "boatwright.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(getRecipeFilename());
	}

	protected void buildDoor(Room room, final int dir)
	{
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			//int size = CMath.s_int(recipe[DAT_MISC]);
			String closeWord=null;
			String openWord=null;
			String closedWord=null;
			final String displayText="";
			//if(closeWord == null)
				closeWord="close";
			//if(openWord == null)
				openWord="open";
			//if(closedWord == null)
				closedWord=CMLib.english().startWithAorAn("closed door");
			room=CMLib.map().getRoom(room);
			final Exit X=CMClass.getExit("GenDoor");
			X.setName(CMLib.english().startWithAorAn("a door"));
			X.setDescription("");
			X.setDisplayText(displayText);
			X.setOpenDelayTicks(9999);
			X.setExitParams("door",closeWord,openWord,closedWord);
			if(X.defaultsClosed() && X.hasADoor())
				X.setDoorsNLocks(X.hasADoor(), !X.defaultsClosed(), X.defaultsClosed(), X.hasALock(), X.hasALock(), X.defaultsLocked());
			X.recoverPhyStats();
			X.text();
			room.setRawExit(dir,X);
			if(room.rawDoors()[dir]!=null)
			{
				final Room oroom = room.rawDoors()[dir].prepareRoomInDir(room, dir);
				final Exit X2=(Exit)X.copyOf();
				X2.recoverPhyStats();
				X2.text();
				oroom.setRawExit(Directions.getOpDirectionCode(dir),X2);
				CMLib.database().DBUpdateExits(oroom);
			}
			CMLib.database().DBUpdateExits(room);
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
				if((buildingI!=null)
				&&(!aborted))
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
							commonEmote(mob,L("<S-NAME> mess(es) up carving @x1.",buildingI.name()));
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
							if(key!=null)
							{
								dropAWinner(mob,key);
								if(key instanceof Container)
									key.setContainer((Container)buildingI);
							}
						}
					}
				}
				buildingI=null;
				key=null;
				activity = CraftingActivity.CRAFTING;
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
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I instanceof Rideable)
		{
			final Rideable R=(Rideable)I;
			final Basis rideType=R.rideBasis();
			switch(rideType)
			{
			case WATER_BASED:
				return true;
			default:
				return false;
			}
		}
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
		if((!(E instanceof Item))||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTelL(mob,"That's not a boatwrighting item.");
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

		@SuppressWarnings("unused")
		int recipeLevel=1;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTelL(mob,"Boatwright what? Enter \"boatwright list\" for a list, \"boatwright info <item>\", \"boatwright scan\","
						+ " \"boatwright learn <item>\", \"boatwright mend <item>\","
						+ " or \"boatwright stop\" to cancel.");
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
		if(str.equalsIgnoreCase("list") && (autoGenerate <= 0))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final int[] cols={
				CMLib.lister().fixColWidth(16,mob.session()),
				CMLib.lister().fixColWidth(5,mob.session()),
				CMLib.lister().fixColWidth(8,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("^H@x1 @x2 @x3 Wood required^N\n\r",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Level"),cols[1]),CMStrings.padRight(L("Capacity"),cols[2])));
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipes(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					final int capacity=CMath.s_int(V.get(RCP_CAPACITY));
					if(((level<=xlevel(mob))||allFlag))
						buf.append("^w"+CMStrings.padRight(item,cols[0])+"^N "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRight(""+capacity,cols[2])+" "+wood+"\n\r");
				}
			}
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
			key=null;
			messedUp=false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			final Room R=mob.location();
			if(R.getArea() instanceof Boardable)
			{
				buildingI=getTarget(mob,CMLib.map().roomLocation(((Boardable)R.getArea()).getBoardableItem()),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
				if(buildingI != ((Boardable)R.getArea()).getBoardableItem())
					buildingI=null;
			}
			if(buildingI==null)
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
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
			{
				amount=CMath.s_int(commands.get(commands.size()-1));
				commands.remove(commands.size()-1);
			}
			final int[] pm=checkMaterialFrom(mob,commands,new int[]{RawMaterial.MATERIAL_WOODEN});
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
				commonFaiL(mob,commands,"You don't know how to carve a '@x1'.  Try \"boatwright list\" for a list.",recipeName);
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
			final int[][] data=fetchFoundResourceData(mob,
													woodRequired,"wood",pm,
													0,null,null,
													false,
													autoGenerate,
													null);
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
				commonFaiL(mob,commands,"There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),6);
			buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
			String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			if(misctype.equalsIgnoreCase("BUNDLE"))
				itemName=CMLib.english().startWithAorAn(woodRequired+"# "+itemName);
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) carving @x1.",buildingI.name());
			displayText=L("You are carving @x1",buildingI.name());
			verb=L("carving @x1",buildingI.name());
			playSound="saw.wav";
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(determineDescription(itemName, buildingI.material(), deadMats, deadComps));
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],data[1][FOUND_CODE], bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			setBrand(mob, buildingI);
			final String strstr=foundRecipe.get(RCP_STRINGS);
			if((strstr!=null)&&(strstr.length()>0)&&(buildingI instanceof Rideable))
			{
				final List<String> strstrl=CMParms.parseSemicolons(strstr, false);
				if((strstrl.size()>0)&&(strstrl.get(0).trim().length()>0))
					((Rideable)buildingI).setStateString(strstrl.get(0).trim());
				if((strstrl.size()>1)&&(strstrl.get(1).trim().length()>0))
					((Rideable)buildingI).setStateStringSubject(strstrl.get(1).trim());
				if((strstrl.size()>2)&&(strstrl.get(2).trim().length()>0))
					((Rideable)buildingI).setRideString(strstrl.get(2).trim());
				if((strstrl.size()>3)&&(strstrl.get(3).trim().length()>0))
					((Rideable)buildingI).setMountString(strstrl.get(3).trim());
				if((strstrl.size()>4)&&(strstrl.get(4).trim().length()>0))
					((Rideable)buildingI).setDismountString(strstrl.get(4).trim());
				if((strstrl.size()>5)&&(strstrl.get(5).trim().length()>0))
					((Rideable)buildingI).setPutString(strstrl.get(5).trim());
			}
			final long canContain=getContainerType(foundRecipe.get(RCP_CONTAINMASK));
			final String capacity=foundRecipe.get(RCP_CAPACITY);
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			if(bundling)
				buildingI.setBaseValue(lostValue);
			addSpellsOrBehaviors(buildingI,spell,deadMats.getLostProps(),deadComps.getLostProps());
			key=null;
			if(buildingI instanceof Rideable)
			{
				if(misctype.length()>0)
					setRideBasis((Rideable)buildingI,misctype);
				if(CMath.isInteger(capacity))
					((Rideable)buildingI).setRiderCapacity(CMath.s_int(capacity));
				((Container)buildingI).setContainTypes(canContain);
				((Container)buildingI).setCapacity(buildingI.basePhyStats().weight()+250+(250*CMath.s_int(capacity)));
			}
			else
			if(buildingI instanceof Container)
			{
				((Container)buildingI).setContainTypes(canContain);
				((Container)buildingI).setCapacity(CMath.s_int(capacity));
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
