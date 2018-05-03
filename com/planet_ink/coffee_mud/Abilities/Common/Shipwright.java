package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.BuildingSkill.Flag;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Shipwright extends CraftingSkill implements ItemCraftor, MendingSkill
{
	@Override
	public String ID()
	{
		return "Shipwright";
	}

	private final static String	localizedName	= CMLib.lang().L("Ship Building");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SHIPBUILD", "SHIPBUILDING", "SHIPWRIGHT" });

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
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tRIDE_BASIS\tRIDE_CAPACITY\tCONTAINER_CAPACITY\t"
		+"RIDE_OVERRIDE_STRS\tCODED_SPELL_LIST";
	}

	private int					doorDir			= -1;
	private String				reTitle			= null;
	private String				reDesc			= null;
	
	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
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
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if((buildingI==null)
			&&(activity != CraftingActivity.RETITLING)
			&&(activity != CraftingActivity.DOORING)
			&&(activity != CraftingActivity.DEMOLISH))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String parametersFile()
	{
		return "shipwright.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	protected void buildDoor(Room room, int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			//int size = CMath.s_int(recipe[DAT_MISC]);
			String closeWord=null;
			String openWord=null;
			String closedWord=null;
			String displayText="";
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
				final Exit X2=(Exit)X.copyOf();
				X2.recoverPhyStats();
				X2.text();
				room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),X2);
				CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
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
				if((activity == CraftingActivity.RETITLING)
				&&(!aborted))
				{
					if((messedUp)||(mob.location()!=activityRoom))
						commonEmote(mob,L("<S-NAME> mess(es) up <S-HIS-HER> work on @x1.",activityRoom.displayText()));
					else
					{
						activityRoom.setDisplayText(reTitle);
						activityRoom.setDescription(reDesc);
						reTitle=null;
						reDesc=null;
					}
				}
				else
				if((activity == CraftingActivity.DOORING)
				&&(!aborted))
				{
					if((messedUp)||(mob.location()!=activityRoom))
						commonEmote(mob,L("<S-NAME> mess(es) up <S-HIS-HER> work on the door in @x1.",activityRoom.displayText()));
					else
						buildDoor(activityRoom,doorDir);
				}
				else
				if((activity == CraftingActivity.DEMOLISH)
				&&(!aborted))
				{
					if((messedUp)||(mob.location()!=activityRoom))
						commonEmote(mob,L("<S-NAME> mess(es) up <S-HIS-HER> work on demolishing the door in @x1.",activityRoom.displayText()));
					else
					{
						activityRoom.setRawExit(doorDir,CMClass.getExit("Open"));
						if(activityRoom.rawDoors()[doorDir]!=null)
						{
							activityRoom.rawDoors()[doorDir].setRawExit(Directions.getOpDirectionCode(doorDir),CMClass.getExit("Open"));
							CMLib.database().DBUpdateExits(activityRoom.rawDoors()[doorDir]);
						}
						CMLib.database().DBUpdateExits(activityRoom);
					}
				}
				else
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
							buildingI.destroy();
						}
						else
							commonEmote(mob,L("<S-NAME> mess(es) up carving @x1.",buildingI.name()));
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
						{
							if((buildingI instanceof BoardableShip)
							&&(buildingI.usesRemaining()<95))
								buildingI.setUsesRemaining(buildingI.usesRemaining()+5);
							else
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
						{
							dropAWinner(mob,buildingI);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this);
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
			final int rideType=R.rideBasis();
			switch(rideType)
			{
			case Rideable.RIDEABLE_WATER:
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean supportsMending(Physical item)
	{
		return canMend(null, item, true);
	}

	@Override
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet))
			return false;
		if((E instanceof BoardableShip)
		&&(E instanceof Rideable)
		&&(((Rideable)E).rideBasis()==Rideable.RIDEABLE_ENTERIN))
			return true;
		if((!(E instanceof Item))||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,L("That's not a shipwrighting item."));
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
		
		if(super.checkInfo(mob, commands))
			return true;
		
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Shipwright what? Enter \"shipwright list\" for a list, \"shipwright info <item>\", \"shipwright scan\","
						+ " \"shipwright learn <item>\", \"shipwright mend <item>\", \"shipwright title <text>\", \"shipwright desc <text>\","
						+ " \"shipwright door <dir>\", \"shipwright demolish <dir>\", or \"shipwright stop\" to cancel."));
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
				CMLib.lister().fixColWidth(16,mob.session()),
				CMLib.lister().fixColWidth(5,mob.session()),
				CMLib.lister().fixColWidth(8,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 @x3 Wood required\n\r",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Level"),cols[1]),CMStrings.padRight(L("Capacity"),cols[2])));
			Collections.sort(recipes,new Comparator<List<String>>(){
				@Override
				public int compare(List<String> o1, List<String> o2)
				{
					final int level1=CMath.s_int(o1.get(RCP_LEVEL));
					final int level2=CMath.s_int(o2.get(RCP_LEVEL));
					return (level1>level2)?1:(level1<level2)?-1:0;
				}
			});
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					final int capacity=CMath.s_int(V.get(RCP_CAPACITY));
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRight(""+capacity,cols[2])+" "+wood+"\n\r");
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
			if(R.getArea() instanceof BoardableShip)
			{
				buildingI=getTarget(mob,CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem()),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
				if(buildingI != ((BoardableShip)R.getArea()).getShipItem())
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
		if(str.equalsIgnoreCase("title"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof BoardableShip))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,L("A title must be specified."));
				return false;
			}
			final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
			final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,20);
			for (final Room room2 : checkSet)
			{
				final Room R2=CMLib.map().getRoom(room2);
				if(R2.displayText(mob).equalsIgnoreCase(title))
				{
					commonTell(mob,L("That title has already been taken.  Choose another."));
					return false;
				}
			}
			reTitle=title;
			reDesc=R.description();
			activity = CraftingActivity.RETITLING;
			activityRoom=R;
			duration=getDuration(10,mob,mob.phyStats().level(),3);
		}
		else
		if(str.equalsIgnoreCase("desc"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof BoardableShip))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			if(commands.size()<2)
			{
				commonTell(mob,L("You must specify a description for it."));
				return false;
			}

			final String newDescription=CMParms.combine(commands,1);
			if(newDescription.length()==0)
			{
				commonTell(mob,L("A description must be specified."));
				return false;
			}
			reTitle=R.displayText();
			reDesc=newDescription;
			activity = CraftingActivity.RETITLING;
			activityRoom=R;
			duration=getDuration(40,mob,mob.phyStats().level(),10);
		}
		else
		if(str.equalsIgnoreCase("door"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof BoardableShip))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			final String dirName=commands.get(commands.size()-1);
			int dir=CMLib.directions().getGoodShipDirectionCode(dirName);
			if(dir <0)
			{
				commonTell(mob,L("You must specify a direction in which to build the door."));
				return false;
			}
			if((dir<0)
			||(dir==Directions.UP)
			||(dir==Directions.DOWN))
			{
				commonTell(mob,L("A valid direction in which to build the door must be specified."));
				return false;
			}
			
			if((R.domainType()&Room.INDOORS)==0)
			{
				commonTell(mob,L("You can only build a door below decks."));
				return false;
			}
			
			Room R1=R.getRoomInDir(dir);
			Exit E1=R.getExitInDir(dir);
			if((R1==null)||(E1==null))
			{
				commonTell(mob,L("There is nowhere to build a door that way."));
				return false;
			}
			if(E1.hasADoor())
			{
				commonTell(mob,L("There is already a door that way."));
				return false;
			}
			
			int woodRequired=125 ;
			woodRequired=adjustWoodRequired(woodRequired,mob);
			final int[] pm={RawMaterial.MATERIAL_WOODEN};
			final int[][] data=fetchFoundResourceData(mob,
													woodRequired,"wood",pm,
													0,null,null,
													false,
													autoGenerate,
													null);
			if(data==null)
				return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			final int woodDestroyed=woodRequired;
			if(autoGenerate<=0)
				CMLib.materials().destroyResourcesValue(mob.location(),woodDestroyed,data[0][FOUND_CODE],0,null);
			
			doorDir = dir;
			activity = CraftingActivity.DOORING;
			activityRoom=R;
			duration=getDuration(25,mob,mob.phyStats().level(),10);
		}
		else
		if(str.equalsIgnoreCase("demolish"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof BoardableShip))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			final String dirName=commands.get(commands.size()-1);
			int dir=CMLib.directions().getGoodShipDirectionCode(dirName);
			if(dir <0)
			{
				commonTell(mob,L("You must specify a direction in which to demolish a door."));
				return false;
			}
			if((dir<0)
			||(dir==Directions.UP)
			||(dir==Directions.DOWN))
			{
				commonTell(mob,L("A valid direction in which to demolish a door must be specified."));
				return false;
			}
			
			if((R.domainType()&Room.INDOORS)==0)
			{
				commonTell(mob,L("You can only demolish a door below decks."));
				return false;
			}
			
			Room R1=R.getRoomInDir(dir);
			Exit E1=R.getExitInDir(dir);
			if((R1==null)||(E1==null))
			{
				commonTell(mob,L("There is nowhere to demolish a door that way."));
				return false;
			}
			if(!E1.hasADoor())
			{
				commonTell(mob,L("There is not a door that way to demolish."));
				return false;
			}
			
			doorDir = dir;
			activity = CraftingActivity.DEMOLISH;
			activityRoom=R;
			duration=getDuration(25,mob,mob.phyStats().level(),10);
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
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
				commonTell(mob,L("You don't know how to carve a '@x1'.  Try \"shipwright list\" for a list.",recipeName));
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
			final int[] pm={RawMaterial.MATERIAL_WOODEN};
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
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			final int woodDestroyed=woodRequired;
			final int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResourcesValue(mob.location(),woodDestroyed,data[0][FOUND_CODE],0,null)
				+CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			if(buildingI==null)
			{
				commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),6);
			buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
			String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(buildingI.material())).toLowerCase();
			if(misctype.equalsIgnoreCase("BUNDLE"))
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) carving @x1.",buildingI.name());
			displayText=L("You are carving @x1",buildingI.name());
			verb=L("carving @x1",buildingI.name());
			playSound="saw.wav";
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(itemName+". ");
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			setBrand(mob, buildingI);
			String strstr=foundRecipe.get(RCP_STRINGS);
			if((strstr!=null)&&(strstr.length()>0)&&(buildingI instanceof Rideable))
			{
				List<String> strstrl=CMParms.parseSemicolons(strstr, false);
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
			addSpells(buildingI,spell);
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
		else
		if(activity == CraftingActivity.RETITLING)
		{
			messedUp=false;
			verb=L("working on @x1",mob.location().displayText());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}
		else
		if(activity == CraftingActivity.DOORING)
		{
			messedUp=false;
			verb=L("working on a @x1 door in @x2",CMLib.directions().getShipDirectionName(doorDir),mob.location().displayText());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}
		else
		if(activity == CraftingActivity.DEMOLISH)
		{
			messedUp=false;
			verb=L("working on demolishing the @x1 door in @x2",CMLib.directions().getShipDirectionName(doorDir),mob.location().displayText());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}

		if((autoGenerate>0) 
		&& (activity != CraftingActivity.RETITLING)
		&& (activity != CraftingActivity.DOORING)
		&& (activity != CraftingActivity.DEMOLISH))
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
