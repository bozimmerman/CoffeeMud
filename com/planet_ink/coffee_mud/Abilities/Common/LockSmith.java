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

public class LockSmith extends CraftingSkill
{
	@Override
	public String ID()
	{
		return "LockSmith";
	}

	private final static String localizedName = CMLib.lang().L("Locksmithing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"LOCKSMITH","LOCKSMITHING"});
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

	private String keyCode="";
	protected Physical workingOn=null;
	protected boolean boltlock=false;
	private boolean delock=false;

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
						commonTell(mob,L("You've ruined @x1!",buildingI.name(mob)));
					else
					if(!delock)
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

	public Item getBuilding(Environmental target)
	{
		final Item newbuilding=CMClass.getItem("GenKey");
		if((workingOn instanceof Exit)
		&&((Exit)workingOn).hasALock()
		&&(((Exit)workingOn).keyName().length()>0))
			keyCode=((Exit)workingOn).keyName();
		if((workingOn instanceof Container)
		&&(((Container)workingOn).hasALock())
		&&(((Container)workingOn).keyName().length()>0))
			keyCode=((Container)workingOn).keyName();
		((DoorKey)newbuilding).setKey(keyCode);
		return newbuilding;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_BUILDINGSKILL;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((workingOn instanceof Container) && (mob.location()!=CMLib.map().roomLocation(workingOn)))
			{
				commonTell(mob,L("You've stopped @x1.",verb));
				buildingI=null;
				unInvoke();
				return super.tick(ticking, tickID);
			}
			if(tickDown<=1)
			{
				if(buildingI==null)
					buildingI=getBuilding(workingOn);
				if((workingOn!=null)&&(mob.location()!=null)&&(!aborted))
				{
					if(workingOn instanceof Exit)
					{
						if((delock)||(!((Exit)workingOn).hasALock()))
						{
							int dir=-1;
							for(final int d : Directions.CODES())
							{
								if(mob.location().getExitInDir(d)==workingOn)
								{
									dir=d;
									break;
								}
							}
							if((messedUp)||(dir<0))
							{
								if(delock)
									commonTell(mob,L("You've failed to remove the lock."));
								else
									commonTell(mob,L("You've ruined the lock."));
								buildingI=null;
								unInvoke();
							}
							else
							{
								final Exit exit2=mob.location().getPairedExit(dir);
								final Room room2=mob.location().getRoomInDir(dir);
								((Exit)workingOn).basePhyStats().setLevel(xlevel(mob));
								((Exit)workingOn).recoverPhyStats();
								((Exit)workingOn).setDoorsNLocks(true,false,true,!delock,!delock,!delock);
								if(buildingI instanceof DoorKey)
								{
									if(((DoorKey)buildingI).getKey().length()==0)
										((DoorKey)buildingI).setKey(keyCode);
									((Exit)workingOn).setKeyName(((DoorKey)buildingI).getKey());
								}
								if(CMLib.map().getRoom(mob.location().roomID())==mob.location()) // ensures not an instance or ship
									CMLib.database().DBUpdateExits(mob.location());
								if((exit2!=null)
								&&(!boltlock)
								&&(exit2.hasADoor())
								&&(exit2.isGeneric())
								&&(room2!=null))
								{
									exit2.basePhyStats().setLevel(xlevel(mob));
									exit2.setDoorsNLocks(true,false,true,!delock,!delock,!delock);
									if(buildingI instanceof DoorKey)
										exit2.setKeyName(((DoorKey)buildingI).getKey());
									if(CMLib.map().getRoom(room2.roomID())==room2) // ensures not an instance or ship
										CMLib.database().DBUpdateExits(room2);
								}
							}
						}
					}
					else
					if(workingOn instanceof Container)
					{
						if(delock||(!((Container)workingOn).hasALock()))
						{
							if(messedUp)
							{
								if(delock)
									commonTell(mob,L("You've failed to remove the lock."));
								else
									commonTell(mob,L("You've ruined the lock."));
								buildingI=null;
								unInvoke();
							}
							else
							{
								((Container)workingOn).setDoorsNLocks(true,false,((Container)workingOn).defaultsClosed(),!delock,!delock,!delock);
								if(buildingI instanceof DoorKey)
								{
									if(((DoorKey)buildingI).getKey().length()==0)
										((DoorKey)buildingI).setKey(keyCode);
									((Container)workingOn).setKeyName(((DoorKey)buildingI).getKey());
								}
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if((commands.size()==0)
		||(CMParms.combine(commands,0).equalsIgnoreCase("list")))
		{
			commonTell(mob,L("Locksmith what or where? Enter the name of a container or door direction. Put the word \"boltlock\" in front of the door direction to make a one-way lock.  Put the word \"delock\" in front of the door direction to remove the locks."));
			return false;
		}
		keyCode=""+Math.random();
		String startStr=null;
		int duration=8;
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		boolean keyFlag=false;
		workingOn=null;
		messedUp=false;
		int woodRequired=1;
		boolean lboltlock=false;
		if((commands.size()>0)&&("BOLTLOCK".startsWith((commands.get(0)).toUpperCase())))
		{
			lboltlock=true;
			commands.remove(0);
		}
		boolean ldelock=false;
		String label = "";
		if((commands.size()>0)&&("DELOCK".startsWith((commands.get(0)).toUpperCase())))
		{
			ldelock=true;
			commands.remove(0);
		}
		else
		if((commands.size()>2)&&("LABEL".equalsIgnoreCase((commands.get(0)).toUpperCase())))
		{
			commands.remove(0);
			label = commands.remove(0);
			if((label.length()>7) || (label.indexOf(' ')>=0))
			{
				commonTell(mob,L("That can't be etched on a key."));
				return false;
			}
		}
		
		final String recipeName=CMParms.combine(commands,0);
		final int dir=CMLib.directions().getGoodDirectionCode(recipeName);
		if(dir<0)
			workingOn=mob.location().fetchFromMOBRoomFavorsItems(mob,null,recipeName,Wearable.FILTER_UNWORNONLY);
		else
			workingOn=mob.location().getExitInDir(dir);

		if((workingOn==null)||(!CMLib.flags().canBeSeenBy(workingOn,mob)))
		{
			commonTell(mob,L("You don't see a '@x1' here.",recipeName));
			return false;
		}
		if(workingOn instanceof Exit)
		{
			if(!((Exit)workingOn).hasADoor())
			{
				commonTell(mob,L("There is no door in that direction."));
				return false;
			}
			if(!workingOn.isGeneric())
			{
				commonTell(mob,L("That door isn't built right -- it can't be modified."));
				return false;
			}
			if(!ldelock)
			{
				if(((Exit)workingOn).hasALock())
					keyFlag=true;
				else
					woodRequired=5;
			}

			final Room otherRoom=(dir>=0)?mob.location().getRoomInDir(dir):null;
			if((!CMLib.law().doesOwnThisProperty(mob,mob.location()))
			&&((otherRoom==null)
				||(!CMLib.law().doesOwnThisProperty(mob,otherRoom))))
			{
				commonTell(mob,L("You'll need the permission of the owner to do that."));
				return false;
			}
		}
		else
		if(workingOn instanceof Container)
		{
			if(!((Container)workingOn).hasADoor())
			{
				commonTell(mob,L("That doesn't have a lid."));
				return false;
			}
			if(!workingOn.isGeneric())
			{
				commonTell(mob,L("That just isn't built right -- it can't be modified."));
				return false;
			}
			if(!ldelock)
			{
				if(((Container)workingOn).hasALock())
					keyFlag=true;
				else
					woodRequired=3;
			}
			if((((Container)workingOn).owner() instanceof Room)
			&&(!CMLib.flags().isGettable((Container)workingOn))
			&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
			{
				commonTell(mob,L("You'll need the permission of the owner of this place to do that."));
				return false;
			}
		}
		else
		{
			commonTell(mob,L("You can't put a lock on that."));
			return false;
		}

		String itemName=null;
		int makeResource=-1;
		if(ldelock)
		{
			itemName="a broken lock";
			keyFlag=false;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
		}
		else
		{
			final int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			final int[][] data=fetchFoundResourceData(mob,
												woodRequired,"metal",pm,
												0,null,null,
												false,
												0,
												null);
			if(data==null)
				return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
			makeResource=data[0][FOUND_CODE];
			String prefix = (label.length()>0) ? label : RawMaterial.CODES.NAME(makeResource);
			itemName=(prefix+" key").toLowerCase();
			itemName=CMLib.english().startWithAorAn(itemName);
		}
		buildingI=getBuilding(workingOn);
		if(buildingI==null)
		{
			commonTell(mob,L("There's no such thing as a GenKey!!!"));
			return false;
		}
		if((makeResource>=0)&&(buildingI!=null))
			buildingI.setMaterial(makeResource);
		duration=getDuration(25,mob,workingOn.phyStats().level(),8);
		if(keyFlag)
			duration=duration/2;
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) working on @x1@x2.",(keyFlag?"a key for ":""),workingOn.name());
		displayText=L("You are working on @x1@x2",(keyFlag?"a key for ":""),workingOn.name());
		verb=L("working on @x1@x2",(keyFlag?"a key for ":""),workingOn.name());
		playSound="drill.wav";
		buildingI.setDisplayText(L("@x1 lies here",itemName));
		buildingI.setDescription(itemName+". ");
		buildingI.basePhyStats().setWeight(woodRequired);
		buildingI.setBaseValue(1);
		buildingI.basePhyStats().setLevel(1);
		setBrand(mob, buildingI);
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		int proficiencyAddition=0;
		if(workingOn.phyStats().level()>xlevel(mob))
			proficiencyAddition=workingOn.phyStats().level()-xlevel(mob);
		messedUp=!proficiencyCheck(mob,proficiencyAddition*5,auto);

		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			boltlock=lboltlock;
			delock=ldelock;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
