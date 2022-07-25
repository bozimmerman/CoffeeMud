package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2022-2022 Bo Zimmerman

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
public class AnimalTrapping extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "AnimalTrapping";
	}

	public AnimalTrapping()
	{
		super();
		displayText=L("You are trapping animals...");
		verb=L("trapping animals");
	}

	private final static String localizedName = CMLib.lang().L("Animal Trapping");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"ATRAP","ANIMALTRAPPING"});
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

	protected CagedAnimal	found			= null;
	protected String		foundShortName	= "";
	protected Container		cage			= null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if((found != null)
		&&(cage != null)
		&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			R.showHappens(CMMsg.MSG_NOISE, L("@x1 makes a clicking sound.",cage.name()));
			R.addItem(found,Expire.Resource);
			found.setContainer(cage);
			cage.setDoorsNLocks(cage.hasADoor(),false,cage.defaultsClosed(),cage.hasALock(),cage.hasALock(),cage.defaultsLocked());
			found=null;
			cage=null;
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("trapping animals");
		found=null;
		activityRoom=null;
		final Room R=mob.location();
		if(R==null)
			return false;

		if(R.fetchEffect(ID())!=null)
		{
			mob.tell(L("Animal traps have already been set here."));
			return false;
		}

		final Item targetI=super.getTarget(mob, R, givenTarget, commands, Wearable.FILTER_UNWORNONLY);
		if(targetI==null)
			return false;
		final CagedAnimal cI=(CagedAnimal)CMClass.getItem("GenCaged");
		if((!(targetI instanceof Container))
		||(((Container)targetI).containTypes() != (Container.CONTAIN_BODIES|Container.CONTAIN_CAGED))
		||(!(((Container)targetI).canContain(cI)))
		||(!((Container)targetI).hasADoor()))
		{
			mob.tell(L("@x1 is not a proper animal cage!",targetI.name(mob)));
			return false;
		}
		boolean empty=true;
		for(final Item I : ((Container)targetI).getContents())
		{
			if(I instanceof CagedAnimal)
				empty=false;
		}
		if(!empty)
		{
			mob.tell(L("@x1 is not a empty!",targetI.name(mob)));
			return false;
		}
		if(targetI.owner()==mob)
			CMLib.commands().postDrop(mob, targetI, true, false, false);
		if(targetI.owner() != R)
		{
			mob.tell(L("@x1 is not a working!",targetI.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=R.myResource();
		if((resourceType&RawMaterial.RESOURCE_MASK)>=RawMaterial.CODES.NAMES().length)
		{
			resourceType=0;
			Log.errOut("AnimalTrapping","Room "+CMLib.map().getExtendedRoomID(R)+" had resource code "+R.myResource());
		}
		final String resourceName = RawMaterial.CODES.NAME(resourceType).toUpperCase();
		final int domainType = R.domainType()&(~Room.INDOORS);
		final String domainName;
		if((R.domainType()&Room.INDOORS)==0)
			domainName = (domainType < Room.DOMAIN_OUTDOOR_DESCS.length) ? Room.DOMAIN_OUTDOOR_DESCS[domainType].toUpperCase() : "";
		else
			domainName = (domainType < Room.DOMAIN_INDOORS_DESCS.length) ? Room.DOMAIN_INDOORS_DESCS[domainType].toUpperCase() : "";
		found=null;
		cage=null;
		if(proficiencyCheck(mob,0,auto))
		{
			final List<List<String>> recipes = loadRecipes("atrapping.txt");
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
							stdM.setDisplayText(stdM.Name()+" is here");
							stdM.baseCharStats().setMyRace(raceR);
							stdM.recoverPhyStats();
							stdM.recoverCharStats();
						}
					}
					if(stdM != null)
					{
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

						foundShortName=genM.name();
						cage = (Container)targetI;
						int x=0;
						if((x=foundShortName.lastIndexOf(' '))>=0)
							foundShortName=foundShortName.substring(x).trim().toLowerCase();
						genM.setLocation(null);
						if(genM instanceof Rideable)
						{
							if((CMLib.flags().canBreatheThis(genM, RawMaterial.RESOURCE_FRESHWATER)
								|| CMLib.flags().canBreatheThis(genM, RawMaterial.RESOURCE_SALTWATER))
							&&(!CMLib.flags().canBreatheThis(genM, RawMaterial.RESOURCE_AIR)))
								((Rideable)genM).setRideBasis(Rideable.Basis.WATER_BASED);
							else
								((Rideable)genM).setRideBasis(Rideable.Basis.LAND_BASED);
						}
						cI.cageMe(genM);
						cI.recoverPhyStats();
						genM.destroy();
						final Container C=(Container)targetI;
						if(C.capacity() - C.phyStats().weight() >= cI.phyStats().weight())
							found=cI;
						else
							cI.destroy();
					}
				}
			}
		}
		final int duration=10+mob.phyStats().level()+(super.getXTIMELevel(mob)*2);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> set(s) an animal trap."));
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			beneficialAffect(mob,R,asLevel,duration);
		}
		else
			found=null;
		return true;
	}
}
