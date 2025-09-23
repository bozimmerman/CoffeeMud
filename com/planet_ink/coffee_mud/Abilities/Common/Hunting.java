package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2002-2025 Bo Zimmerman

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
public class Hunting extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Hunting";
	}

	public Hunting()
	{
		super();
		displayText=L("You are hunting...");
		verb=L("hunting");
	}

	private final static String localizedName = CMLib.lang().L("Hunting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HUNT","HUNTING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL;
	}

	// common recipe definition indexes
	protected static final int	RCP_RESOURCE= 0;
	protected static final int	RCP_DOMAIN	= 1;
	protected static final int	RCP_FREQ	= 2;
	protected static final int	RCP_MOB		= 3;

	protected MOB		found			= null;
	protected String	foundShortName	= "";

	public Room nearByRoom()
	{
		final List<Integer> possibilities=new ArrayList<Integer>(Directions.NUM_DIRECTIONS());
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d!=Directions.UP)
			{
				final Room room=activityRoom.getRoomInDir(d);
				final Exit exit=activityRoom.getExitInDir(d);
				if((room!=null)&&(exit!=null)&&(exit.isOpen()))
					possibilities.add(Integer.valueOf(d));
			}
		}
		if(possibilities.size()>0)
		{
			final int dir=possibilities.get(CMLib.dice().roll(1,possibilities.size(),-1)).intValue();
			return activityRoom.getRoomInDir(dir);
		}
		return null;
	}

	public void moveFound()
	{
		if(found.location()==null)
			return;

		final List<Integer> possibilities=new ArrayList<Integer>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room room=found.location().getRoomInDir(d);
			if((room == null)
			||(CMLib.flags().isAiryRoom(room)&&(!CMLib.flags().isFlying(found))))
			{
				final Exit exit=found.location().getExitInDir(d);
				if((exit!=null)
				&&(exit.isOpen())
				&&(CMLib.flags().canBreatheHere(found, room)))
					possibilities.add(Integer.valueOf(d));
			}
		}
		if(possibilities.size()>0)
		{
			final int dir=possibilities.get(CMLib.dice().roll(1,possibilities.size(),-1)).intValue();
			CMLib.tracking().walk(found,dir,true,false);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			activityRoom=mob.location();
			if((found!=null)&&(found.amDead()))
			{
				found.setLocation(null);
				found.destroy();
				unInvoke();
			}
			else
			if((found!=null)
			&&(found.location()!=null)
			&&(CMLib.flags().isAliveAwakeMobile(found,true))
			&&(!found.isInCombat()))
			{
				if(found.location()==mob.location())
				{
					if((mob.isMonster())
					&&(CMLib.flags().isAliveAwakeMobile(mob,true))
					&&(CMLib.flags().canBeSeenBy(found,mob))
					&&(!mob.isInCombat()))
						CMLib.combat().postAttack(mob,found,mob.fetchWieldedItem());
					else
						moveFound();
				}
			}

			if(tickUp==0)
			{
				if(found!=null)
				{
					super.adjustYieldBasedOnRoomSpam(1, mob.location());
					if(CMLib.flags().isWateryRoom(mob.location()))
						commonTelL(mob,"You have found some @x1 signs!",foundShortName);
					else
						commonTelL(mob,"You have found some @x1 tracks!",foundShortName);
					commonTelL(mob,"You need to find the @x1 nearby before the trail goes cold!",foundShortName);
					displayText=L("You are hunting for @x1",found.name());
					verb=L("hunting for @x1",found.name());
					found.basePhyStats().setLevel(mob.basePhyStats().level());
					found.recoverPhyStats();
					final Ability A=CMClass.getAbility("Prop_ModExperience");
					A.setMiscText("=20%");
					found.addNonUninvokableEffect(A);
					found.bringToLife(nearByRoom(),true);
					CMLib.beanCounter().clearZeroMoney(found,null);
					found.setMoneyVariation(0);
				}
				else
				{
					final int d=lookingForMat(RawMaterial.MATERIAL_FLESH,mob.location());
					if(d<0)
						commonTelL(mob,"You can't seem to find any game around here.\n\rYou might try elsewhere.");
					else
						commonTelL(mob,"You can't seem to find any game around here.\n\rYou might try @x1.",CMLib.directions().getInDirectionName(d));
					unInvoke();
				}

			}
			else
			if(mob.isMonster()
			&&(CMLib.dice().rollPercentage()>50)
			&&(CMLib.flags().isMobile(mob))
			&&(CMLib.flags().isAliveAwakeMobile(mob,true))
			&&(CMLib.flags().canSenseEnteringLeaving(found,mob)))
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=mob.location().getRoomInDir(d);
					if((R!=null)&&(R==found.location()))
					{
						CMLib.tracking().walk(mob,d,false,false);
						break;
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((found!=null)&&(!found.amDead())&&(found.location()!=null)&&(!found.isInCombat()))
				{
					if(found.location()==mob.location())
						moveFound();
					found.location().delInhabitant(found);
					found.setLocation(null);
					found.destroy();
					mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> <S-HAS-HAVE> lost the trail."));
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("hunting");
		found=null;
		activityRoom=null;
		final Room R=mob.location();
		if(R==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=R.myResource();
		if((resourceType&RawMaterial.RESOURCE_MASK)>=RawMaterial.CODES.NAMES().length)
		{
			resourceType=0;
			Log.errOut("Hunting","Room "+CMLib.map().getExtendedRoomID(R)+" had resource code "+R.myResource());
		}
		final String resourceName = RawMaterial.CODES.NAME(resourceType).toUpperCase();
		final int domainType = R.domainType()&(~Room.INDOORS);
		final String domainName;
		if((R.domainType()&Room.INDOORS)==0)
			domainName = (domainType < Room.DOMAIN_OUTDOOR_DESCS.length) ? Room.DOMAIN_OUTDOOR_DESCS[domainType].toUpperCase() : "";
		else
			domainName = (domainType < Room.DOMAIN_INDOORS_DESCS.length) ? Room.DOMAIN_INDOORS_DESCS[domainType].toUpperCase() : "";

		if((proficiencyCheck(mob,0,auto))
		&&(super.checkIfAnyYield(mob.location()))
		&&(nearByRoom()!=null))
		{
			final List<List<String>> recipes = loadRecipes("hunting.txt");
			if((recipes != null)
			&&(recipes.size()>0))
			{
				int totalWeights = 0;
				final List<List<String>> subset=new ArrayList<List<String>>();
				for(final List<String> subl : recipes)
				{
					if(subl.size()<4)
						continue;
					if(subl.get(RCP_RESOURCE).toUpperCase().equals(resourceName)
					&&((subl.get(RCP_DOMAIN).equals("*"))
						||(subl.get(RCP_DOMAIN).equals(domainName))
						||(subl.get(RCP_DOMAIN).equals(R.ID()))))
					{
						subset.add(subl);
						totalWeights += CMath.s_int(subl.get(RCP_FREQ));
					}
				}
				List<String> winl = null;
				if(totalWeights > 0)
				{
					final int winner=CMLib.dice().roll(1, totalWeights, -1);
					int current = 0;
					for(final List<String> subl : subset)
					{
						current += CMath.s_int(subl.get(RCP_FREQ));
						if(winner < current)
						{
							winl = subl;
							break;
						}
					}
					if(winl == null)
						winl=subset.get(subset.size()-1);
				}
				if(winl != null)
				{
					final String mobID = winl.get(RCP_MOB);
					MOB stdM = CMClass.getMOB(mobID);
					if(stdM == null)
						stdM = CMLib.catalog().getCatalogMob(mobID);
					if(stdM == null)
					{
						final Race raceR=CMClass.getRace(mobID);
						if(raceR!=null)
						{
							if(raceR.useRideClass())
								stdM=CMClass.getMOB("GenRideable");
							else
								stdM=CMClass.getMOB("GenMob");
							stdM.setName(CMLib.english().startWithAorAn(raceR.name()));
							stdM.setDisplayText(L("@x1 is here",stdM.Name()));
							stdM.baseCharStats().setMyRace(raceR);
							stdM.recoverPhyStats();
							stdM.recoverCharStats();
						}
					}
					if(stdM == null)
					{
						commonFaiL(mob,commands,"There are no signs of life here.");
						return false;
					}
					MOB genM=stdM;
					if(!genM.isGeneric())
					{
						if(genM.baseCharStats().getMyRace().useRideClass())
							genM=CMClass.getMOB("GenRideable");
						else
							genM=CMClass.getMOB("GenMob");
						genM.setBaseCharStats((CharStats)stdM.baseCharStats().copyOf());
						genM.setBasePhyStats((PhyStats)stdM.basePhyStats().copyOf());
						genM.setBaseState((CharState)stdM.baseState().copyOf());
						for(final GenericBuilder.GenMOBCode stat : GenericBuilder.GenMOBCode.values())
						{
							if(stat != GenericBuilder.GenMOBCode.ABILITY) // because this screws up gen hit points
								genM.setStat(stat.name(), CMLib.coffeeMaker().getGenMobStat(stdM,stat.name()));
						}
						genM.basePhyStats().setRejuv(0);
						genM.recoverCharStats();
						genM.recoverPhyStats();
						genM.recoverMaxState();
						genM.resetToMaxState();
					}
					final int moblevel = Math.round(CMath.sqrt(mob.phyStats().level())); // pity on artisans
					final int addlevel = (moblevel < CMProps.getIntVar(CMProps.Int.EXPRATE)) ? 0 : (moblevel - CMProps.getIntVar(CMProps.Int.EXPRATE));
					genM.basePhyStats().setLevel(genM.basePhyStats().level() + addlevel);
					genM.basePhyStats().setRejuv(0);
					genM.recoverCharStats();
					genM.recoverPhyStats();
					genM.recoverMaxState();
					genM.resetToMaxState();
					CMLib.leveler().fillOutMOB(genM,genM.basePhyStats().level());
					genM.basePhyStats().setRejuv(0);
					genM.recoverPhyStats();

					found=genM;
					foundShortName=found.name();
					int x=0;
					if((x=foundShortName.lastIndexOf(' '))>=0)
						foundShortName=foundShortName.substring(x).trim().toLowerCase();
					found.setLocation(null);
					if(found instanceof Rideable)
					{
						if((CMLib.flags().canBreatheThis(found, RawMaterial.RESOURCE_FRESHWATER)
							|| CMLib.flags().canBreatheThis(found, RawMaterial.RESOURCE_SALTWATER))
						&&(!CMLib.flags().canBreatheThis(found, RawMaterial.RESOURCE_AIR)))
							((Rideable)found).setRideBasis(Rideable.Basis.WATER_BASED);
						else
							((Rideable)found).setRideBasis(Rideable.Basis.LAND_BASED);
					}
				}
			}
		}
		final int duration=10+mob.phyStats().level()+(super.getXTIMELevel(mob)*2);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) hunting."));
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			found=(MOB)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
