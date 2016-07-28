package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.StdBehavior;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2016-2016 Bo Zimmerman

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

public class Skill_HireCrewmember extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_HireCrewmember";
	}

	private final static String	localizedName	= CMLib.lang().L("Hire Crewmember");

	protected final static int baseWaterRange = 8;

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "HIRECREWMEMBER","HIRECREW"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	protected int abilityCode = 0;
	
	@Override
	public int abilityCode()
	{
		return abilityCode;
	}
	
	@Override
	public void setAbilityCode(int newCode)
	{
		this.abilityCode = newCode;
	}
	
	@Override
	public CMObject copyOf()
	{
		Skill_HireCrewmember A=(Skill_HireCrewmember)super.copyOf();
		A.sailor=null;
		return A;
	}
	
	private enum CrewType
	{
		REPAIRER,
		TACTICIAN,
		CAPTAIN,
		DEFENDER,
		TRAWLER
	}
	
	protected String	shipName	= "";
	protected CrewType	type		= CrewType.DEFENDER;
	protected Behavior	sailor		= null;
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		int x=newMiscText.indexOf(';');
		if(x>0)
		{
			shipName=newMiscText.substring(0,x);
			type=(CrewType)CMath.s_valueOf(CrewType.class, newMiscText.substring(x+1));
		}
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		switch(type)
		{
		case REPAIRER:
			affectableStats.addAmbiance(L("Repairer"));
			break;
		case DEFENDER:
			affectableStats.addAmbiance(L("Defender"));
			break;
		case TACTICIAN:
			affectableStats.addAmbiance(L("Tactician"));
			break;
		case CAPTAIN:
			affectableStats.addAmbiance(L("Captain"));
			break;
		case TRAWLER:
			affectableStats.addAmbiance(L("Trawler"));
			break;
		}
	}
	
	public Behavior getSailor()
	{
		if(affected instanceof PhysicalAgent)
		{
			PhysicalAgent agent=(PhysicalAgent)affected;
			if((sailor == null)||(agent.fetchBehavior("Sailor")!=sailor))
			{
				final Behavior B=agent.fetchBehavior("Sailor");
				if(B!=null)
					agent.delBehavior(B);
				sailor = CMClass.getBehavior("Sailor");
				switch(type)
				{
				case REPAIRER:
					if(agent instanceof MOB)
					{
						final MOB mob=(MOB)agent;
						if(mob.fetchAbility("Shipwright")==null)
						{
							Ability A=CMClass.getAbility("Shipwright");
							int prof=(mob.phyStats().level()+(5*super.getXLEVELLevel(invoker())))*2;
							if(prof>=100)
								prof=99;
							A.setProficiency(prof);
							A.setSavable(true);
							mob.addAbility(A);
						}
					}
					sailor.setParms("FIGHTMOVER=false FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				case DEFENDER:
					sailor.setParms("DEFENDER=true FIGHTMOVER=false FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				case TACTICIAN:
					sailor.setParms("FIGHTMOVER=true FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				case CAPTAIN:
					sailor.setParms("PEACEMOVER=true FIGHTMOVER=false FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				case TRAWLER:
					if(agent instanceof MOB)
					{
						final MOB mob=(MOB)agent;
						if(mob.fetchAbility("Trawling")==null)
						{
							Ability A=CMClass.getAbility("Trawling");
							int prof=(mob.phyStats().level()+(5*super.getXLEVELLevel(invoker())))*2;
							if(prof>=100)
								prof=99;
							A.setProficiency(prof);
							A.setSavable(true);
							mob.addAbility(A);
						}
					}
					sailor.setParms("FIGHTMOVER=false FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				}
				sailor.setSavable(false);
				((PhysicalAgent)affected).addBehavior(sailor);
			}
		}
		return sailor;
	}
	
	@Override
	public void unInvoke()
	{
		if(affected instanceof PhysicalAgent)
		{
			PhysicalAgent agent=(PhysicalAgent)affected;
			final Behavior B=agent.fetchBehavior("Sailor");
			if(B!=null)
				agent.delBehavior(B);
		}
		super.unInvoke();
	}
	
	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting, msg))
			return false;
		return true;
	}
	
	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		getSailor();
		return true;
	}
	
	protected String getCrewShip(final MOB M)
	{
		Skill_HireCrewmember articlesA=(Skill_HireCrewmember)M.fetchEffect(ID());
		return (articlesA!=null) ? articlesA.shipName : "";
	}
	
	protected CrewType getCrewType(final MOB M)
	{
		Skill_HireCrewmember articlesA=(Skill_HireCrewmember)M.fetchEffect(ID());
		return (articlesA!=null) ? articlesA.type : null;
	}
	
	public boolean isPub(MOB mob, Room room)
	{
		for(int m=0;m<room.numInhabitants();m++)
		{
			final MOB M=room.fetchInhabitant(m);
			if((M!=null)&&(M!=mob))
			{
				if(CMLib.flags().canBeSeenBy(M,mob))
				{
					final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
					if(SK!=null)
					{
						for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
						{
							final Environmental E=i.next();
							if((E instanceof Item)&&(CMLib.flags().isAlcoholic((Item)E)))
							{
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}


	/*
	 * (non-Javadoc)
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		
		if((!target.isMonster())
		||(CMLib.flags().isAnimalIntelligence(target))
		||((target.getStartRoom()==null)&&(target.fetchEffect(ID())==null)))
		{
			mob.tell(L("You can't hire @x1 as a crewmember.",target.name(mob)));
			return false;
		}
		
		boolean allowedToOfferToThem=false;
		if(CMLib.flags().isBoundOrHeld(target))
		{
			allowedToOfferToThem=true;
		}

		if(mob.getGroupMembers(new HashSet<MOB>()).contains(target))
		{
			allowedToOfferToThem=true;
		}

		if(!allowedToOfferToThem)
		{
			mob.tell(L("You can't hire @x1 as crew.",target.name()));
			return false;
		}
		
		int numRooms=0;
		int numCrew=0;
		int numDecks=0;
		int[] numTypes=new int[CrewType.values().length];
		for(Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
		{
			final Room R2=r.nextElement();
			switch(R2.domainType())
			{
			case Room.DOMAIN_INDOORS_AIR:
			case Room.DOMAIN_OUTDOORS_AIR:
				break;
			default:
				if(((R2.domainType()&Room.INDOORS)!=0))
					numDecks++;
				numRooms++;
				break;
			}
			for(Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)
				&&(M.isMonster())
				&&(isCrew(M,myShipItem.Name())))
				{
					numCrew++;
					numTypes[getCrewType(M).ordinal()]++;
				}
			}
		}
		
		int bonus= ( adjustedLevel(mob,asLevel) / 10);
		if(bonus > 0)
		{
			int bonusDecks = bonus / 2;
			numRooms += bonus;
			numDecks += bonusDecks;
		}
		
		CrewType nextType = null;
		final int maxGunners = numDecks;
		int maxBoarders = (numRooms-numDecks)/2;
		if(maxBoarders<1)
			maxBoarders=1;
		int maxDefenders = (numRooms-numDecks - maxBoarders);
		if(maxDefenders<1)
			maxDefenders=1;
		
		if(numCrew >= (maxGunners + maxBoarders + maxDefenders))
		{
			mob.tell(L("This ship already has the maximum crew."));
			return false;
		}
		
		int attempts=10000;
		while((nextType == null)&&(--attempts>0))
		{
			nextType = CrewType.values()[CMLib.dice().roll(1, CrewType.values().length, -1)];
			if(numTypes[nextType.ordinal()]>0)
			{
				switch(nextType)
				{
				case TRAWLER:
					if(numTypes[nextType.ordinal()]>=numDecks)
						nextType=null;
					break;
				case DEFENDER:
					if(numTypes[nextType.ordinal()]>=maxDefenders)
						nextType=null;
					break;
				case CAPTAIN:
					if(numTypes[nextType.ordinal()]>=maxBoarders)
						nextType=null;
					break;
				case TACTICIAN:
					if(numTypes[nextType.ordinal()]>=maxBoarders)
						nextType=null;
					break;
				case REPAIRER:
					if(numTypes[nextType.ordinal()]>=maxBoarders)
						nextType=null;
					break;
				}
			}
		}
		
		if(nextType == null)
		{
			mob.tell(L("This ship already has enough crew."));
			return false;
		}
		
					Ability A=target.fetchEffect(ID());
					if(A!=null)
						target.delEffect(A);
					A=(Ability)copyOf();
					A.setMiscText(myShipItem.Name()+";"+nextType.name());
					A.setAbilityCode(super.getXLEVELLevel(mob));
					target.addNonUninvokableEffect(A);
		
	 * @see com.planet_ink.coffee_mud.Abilities.StdAbility#invoke(com.planet_ink.coffee_mud.MOBS.interfaces.MOB, java.util.List, com.planet_ink.coffee_mud.core.interfaces.Physical, boolean, int)
	 */
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		double money=0.0;
		String moneyStr="money";
		int minLevel = mob.phyStats().level()-10;
		if(minLevel < 1)
			minLevel=1;
		int range=5;
		if(minLevel + range > mob.phyStats().level())
			range = 1;
		int level = CMLib.dice().roll(1, range, minLevel);
		if(!auto)
		{
			if(!this.isPub(mob, R))
			{
				mob.tell(L("You must in a pub to hire a sailor."));
				return false;
			}
			TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
			int roomRange = baseWaterRange + super.getXLEVELLevel(mob)+super.getXMAXRANGELevel(mob);
			List<Room> nearby=CMLib.tracking().findTrailToAnyRoom(R, TrackingFlag.WATERSURFACEONLY.myFilter, flags, roomRange);
			if((nearby==null)||(nearby.size()==0))
			{
				mob.tell(L("There's no sea or river nearby, so no one here would be a sailor."));
				return false;
			}
			
			int medLevel = minLevel + (int)Math.round(CMath.ceiling(CMath.div(range, 2.0)));
			double amt = medLevel * 10.0;
			String currency=R.getArea().getCurrency();
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob, currency) < amt)
			{
				moneyStr = CMLib.beanCounter().abbreviatedPrice(currency, amt);
				mob.tell(L("You need at least @x1 to hire a decent sailor here.",moneyStr));
				return false;
			}
			money=amt;
		}
		
		if(mob.numFollowers() >= mob.maxFollowers())
		{
			mob.tell(L("You have too many followers to gather up another one right now."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			if(money > 0.0)
				CMLib.beanCounter().subtractMoney(mob, money);
			final MOB targetM=CMClass.getMOB("GenMob");
			final List<Race> races=CMLib.login().raceQualifies(CMProps.getIntVar(CMProps.Int.MUDTHEME));
			final Race raceR=races.get(CMLib.dice().roll(1, races.size(), -1));
			String name=CMLib.login().generateRandomName(1, 5);
			String fullName=name;
			//TODO: some funny stuff with the name, display text, desc
			targetM.setName(name);
			targetM.setDisplayText(L("a warhorse with broad powerful wings stands here"));
			targetM.setDescription(L("A ferocious, fleet of foot, flying friend."));
			targetM.basePhyStats().setAbility(11);
			targetM.basePhyStats().setDisposition(targetM.basePhyStats().disposition()|PhyStats.IS_FLYING);
			targetM.basePhyStats().setLevel(level);
			targetM.basePhyStats().setRejuv(PhyStats.NO_REJUV);
			targetM.recoverPhyStats();
			targetM.baseCharStats().setMyRace(raceR);
			targetM.baseCharStats().setStat(CharStats.STAT_GENDER,(CMLib.dice().rollPercentage()>50)?'M':'F');
			targetM.baseCharStats().getMyRace().startRacing(targetM,false);
			targetM.recoverPhyStats();
			targetM.recoverCharStats();
			targetM.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(targetM));
			targetM.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(targetM));
			targetM.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(targetM));
			targetM.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(targetM));
			//targetM.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience")); -- could be dangerous not having this, but 5-10 levels lower, so...
			targetM.recoverCharStats();
			targetM.recoverPhyStats();
			targetM.recoverMaxState();
			CMLib.factions().setAlignment(targetM,Faction.Align.NEUTRAL);
			targetM.resetToMaxState();
			targetM.text();
			targetM.bringToLife(R,true);
			CMLib.beanCounter().clearZeroMoney(targetM,null);
			//targetM.location().showOthers(targetM,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
			R.recoverRoomStats();
			targetM.setStartRoom(null);
			
			String str=auto?"":L("^S<S-NAME> offer(s) <T-NAME> @x1 to hire on as a crewmember..^?",moneyStr);
			final CMMsg msg=CMClass.getMsg(mob,targetM,this,CMMsg.MSG_NOISYMOVEMENT,str,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_NOISYMOVEMENT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				R.show(targetM, null, CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> sign(s) up to be a member of the crew of @x1.",targetM.name()));
				CMLib.commands().postFollow(targetM, mob, false);
			}
			else
				targetM.destroy();
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> offer(s) @x1 to potential sailors, but no one is interested.",moneyStr));


		// return whether it worked
		return success;
	}
}
