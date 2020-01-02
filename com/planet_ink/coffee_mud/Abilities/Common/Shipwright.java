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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2020 Bo Zimmerman

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
		return "";
		//"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		//+"ITEM_CLASS_ID\tRIDE_BASIS\tRIDE_CAPACITY\tCONTAINER_CAPACITY\t"
		//+"RIDE_OVERRIDE_STRS\tCODED_SPELL_LIST";
	}

	private int					doorDir			= -1;
	private String				reTitle			= null;
	private String				reDesc			= null;

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_SHIPINDEX	= 5;

	protected Item key=null;

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
			if((buildingI==null)
			&&(activity != CraftingActivity.RETITLING)
			&&(activity != CraftingActivity.DOORING)
			&&(activity != CraftingActivity.DEMOLISH))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	protected List<Item> getShips()
	{
		final String allItemID = "SHIPWRIGHT_PARSED";
		@SuppressWarnings("unchecked")
		List<Item> shipPrototypes = (List<Item>)Resources.getResource(allItemID);
		if(shipPrototypes == null)
		{
			final CMFile F=new CMFile(Resources.makeFileResourceName("skills/shipbuilding.cmare"),null);
			if(F.exists())
			{
				shipPrototypes=new Vector<Item>();
				CMLib.coffeeMaker().addItemsFromXML(F.textUnformatted().toString(), shipPrototypes, null);
				for(final Item I : shipPrototypes)
					CMLib.threads().deleteAllTicks(I);
				if(shipPrototypes.size()>0)
					Resources.submitResource(allItemID, shipPrototypes);
			}
		}
		return shipPrototypes;
	}

	@Override
	public String parametersFile()
	{
		final CMFile F=new CMFile(Resources.makeFileResourceName("::skills/shipwright.txt"),null);
		if(F.exists())
			return "shipwright.txt";
		final List<Item> ships = getShips();
		if(ships == null)
			return "";
		final StringBuilder recipes = new StringBuilder("");
		int x=0;
		for(final Item I : getShips())
		{
			recipes.append(I.Name()).append("\t")
					.append(""+I.basePhyStats().level()).append("\t")
					.append(""+I.basePhyStats().weight()/10).append("\t")
					.append(""+I.basePhyStats().weight()).append("\t")
					.append(""+I.baseGoldValue()).append("\t")
					.append(""+(x++)).append("\r\n");
		}
		F.saveText(recipes.toString());
		return "shipwright.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	protected void buildDoor(Room room, final int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
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
				final Exit X2=(Exit)X.copyOf();
				X2.recoverPhyStats();
				X2.text();
				room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),X2);
				CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
			}
			CMLib.database().DBUpdateExits(room);
		}
	}

	protected void doShipTransfer(final BoardableShip buildingI, final MOB buyer)
	{
		final MOB shopKeeper = CMClass.getMOB("StdShopkeeper");
		try
		{
			((ShopKeeper)shopKeeper).setWhatIsSoldMask(ShopKeeper.DEAL_SHIPSELLER);
			final CMMsg msg=CMClass.getMsg(buyer,buildingI,shopKeeper,CMMsg.MSG_GET,null);
			buildingI.executeMsg(buyer, msg);
		}
		finally
		{
			shopKeeper.destroy();
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
						{
							commonEmote(mob,L("<S-NAME> mess(es) up building @x1.",buildingI.name()));
							buildingI.destroy();
						}
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
							deconstructRecipeInto(mob, buildingI, recipeHolder );
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
							if(buildingI instanceof BoardableShip)
							{
								final BoardableShip ship=(BoardableShip)buildingI;
								MOB buyer = mob;
								if(buyer.isMonster() && (buyer.amFollowing()!=null))
									buyer = buyer.amUltimatelyFollowing();
								if(buyer.isMonster())
									((BoardableShip)buildingI).renameShip(""+CMLib.dice().roll(1, 999, 0));
								else
									doShipTransfer(ship, buyer);
								if(ship instanceof PrivateProperty)
								{
									final PrivateProperty shipP=(PrivateProperty)ship;
									if(shipP.getOwnerName().length()>0)
									{
										final LandTitle titleI=(LandTitle)CMClass.getItem("GenTitle");
										titleI.setLandPropertyID(ship.Name());
										titleI.text(); // everything else is derived from the ship itself
										((Item)titleI).recoverPhyStats();
										mob.addItem((Item)titleI);
									}
								}
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
		if(I instanceof SailingShip)
			return true;
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

	protected String getIdentifierCommandWord()
	{
		return "shipwright";
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

		@SuppressWarnings("unused")
		int recipeLevel=1;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("@x1 what? Enter \"@x2 list\" for a list, \"@x2 info <item>\", \"@x2 scan\","
						+ " \"@x2 learn <item>\", \"@x2 mend <item>\", \"@x2 title <text>\", \"@x2 desc <text>\","
						+ " \"@x2 door <dir>\", \"@x2 demolish <dir>\", or \"@x2 stop\" to cancel.",
							CMStrings.capitalizeFirstLetter(getIdentifierCommandWord()),getIdentifierCommandWord()));
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
		helpingAbility=null;
		helping=false;
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
				CMLib.lister().fixColWidth(40,mob.session()),
				CMLib.lister().fixColWidth(10,mob.session()),
				CMLib.lister().fixColWidth(10,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 Wood required\n\r",
					CMStrings.padRight(L("Item"),cols[0]),
					CMStrings.padRight(L("Level"),cols[1])));
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
					{
						buf.append(CMStrings.padRight(item,cols[0])
								+" "+CMStrings.padRight(""+level,cols[1])
								+" "+wood+"\n\r");
					}
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
		if(str.equalsIgnoreCase("help"))
		{
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=25;
			commands.remove(0);
			final MOB targetMOB=getTarget(mob,commands,givenTarget,false,true);
			if(targetMOB==null)
				return false;
			if(targetMOB==mob)
			{
				commonTell(mob,L("You can not do that."));
				return false;
			}
			helpingAbility=targetMOB.fetchEffect(ID());
			if(helpingAbility==null)
			{
				commonTell(mob,L("@x1 is not wrighting anything.",targetMOB.Name()));
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			{
				helpingAbility=null;
				return false;
			}
			helping=true;
			verb=L("helping @x1 with @x2",targetMOB.name(),helpingAbility.name());
			startStr=L("<S-NAME> start(s) @x1",verb);
			final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
			}
			return true;
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
			if(title.length()>250)
			{
				commonTell(mob,L("That title is too long."));
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
			final int dir=CMLib.directions().getGoodShipDirectionCode(dirName);
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

			final Room R1=R.getRoomInDir(dir);
			final Exit E1=R.getExitInDir(dir);
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
			if(autoGenerate<=0)
			{
				CMLib.materials().destroyResources(mob.location(),woodRequired,
					data[0][FOUND_CODE],data[0][FOUND_SUB],data[1][FOUND_CODE],data[1][FOUND_SUB]);
			}

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
			final int dir=CMLib.directions().getGoodShipDirectionCode(dirName);
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

			final Room R1=R.getRoomInDir(dir);
			final Exit E1=R.getExitInDir(dir);
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
						recipeLevel=level;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,L("You don't know how to make a '@x1'.  Try \"@x2 list\" for a list.",recipeName,getIdentifierCommandWord()));
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
			final String shipIndexStr = foundRecipe.get(RCP_SHIPINDEX);
			final List<Item> shipPrototypes = getShips();
			if(shipPrototypes != null)
			{
				if(CMath.isInteger(shipIndexStr))
				{
					final int dex=CMath.s_int(shipIndexStr);
					if((dex>=0)&&(dex<shipPrototypes.size()))
						buildingI=shipPrototypes.get(dex);
				}
				else
				{
					for(final Item I : shipPrototypes)
					{
						if(CMLib.english().containsString(I.Name(), super.replacePercent(foundRecipe.get(RCP_FINALNAME), "")))
						{
							buildingI=I;
							break;
						}
					}
				}
				//buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			}
			if(buildingI==null)
			{
				commonTell(mob,L("There's no such thing as a @x1!!!","("+shipIndexStr)+")");
				return false;
			}
			buildingI=(Item)buildingI.copyOf();
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),6);
			buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
			//String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			//itemName=CMLib.english().startWithAorAn(itemName);
			//buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) building @x1.",buildingI.name());
			displayText=L("You are building @x1",buildingI.name());
			verb=L("building @x1",buildingI.name());
			playSound="saw.wav";
			//buildingI.setDisplayText(L("@x1 lies here",itemName));
			//buildingI.setDescription(itemName+". ");
			//buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)+lostValue));
			//buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			setBrand(mob, buildingI);
			key=null;
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
			buildingI=null;
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
