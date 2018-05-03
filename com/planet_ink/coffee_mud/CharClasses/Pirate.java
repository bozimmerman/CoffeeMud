package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.MoonPhase;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TidePhase;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Pirate extends Thief
{
	@Override
	public String ID()
	{
		return "Pirate";
	}

	private final static String localizedStaticName = CMLib.lang().L("Pirate");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	public Pirate()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
	}
	
	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	private final String[] raceRequiredList=new String[]{"All","-Equine"};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String, Integer>[] minimumStatRequirements = new Pair[] 
	{ 
		new Pair<String, Integer>("Dexterity", Integer.valueOf(5)), 
		new Pair<String, Integer>("Charisma", Integer.valueOf(5)) 
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Bonus XP in ship combat, and combat bonus for each fake limb.");
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Get less leniency from the law, no limb recovery after death, and can be paid to leave combat.");
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",0,false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Superstition",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_RopeSwing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_ImprovedBoarding",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_LocateAlcohol",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_HoldYourLiquor",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Belay",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Hide",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_RideTheRigging",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_SeaLegs",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_BuriedTreasure",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Wenching",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_TreasureMap",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Peek",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_WalkThePlank",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_SeaMapping",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Plunder",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Parry",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_SeaCharting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_BackStab",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_DeadReckoning",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Steal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Trip",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_SeaNavigation",false,new XVector<String>("Skill_SeaCharting"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_Listen",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Scuttle",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Bind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_FenceLoot",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_Curse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Song_PirateShanty",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_PiecesOfEight",false,new XVector<String>("Thief_BuriedTreasure"));

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_PirateFamiliar",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_ConcealItem",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_PubContacts",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_Stability",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_PetSpy",false,new XVector<String>("Thief_PirateFamiliar"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_CombatRepairs",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_SilentLoot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_WarningShot",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_FoulWeatherSailing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Distract",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_PayOff",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Salvaging",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_PetSteal",false,new XVector<String>("Thief_PirateFamiliar"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Alertness",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_MerchantFlag",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_Articles",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_RammingSpeed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_SmugglersHold",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Skill_InterceptShip",false,new XVector<String>("Skill_SeaCharting"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_MastShot",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_HideShip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Skill_AttackHalf",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_AwaitShip",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Thief_SilentRunning",true);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if((msg.source() == myHost) && (msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			final Ability A=msg.source().fetchEffect("Amputation");
			if(A!=null)
			{
				msg.addTrailerRunnable(new Runnable()
				{
					private final String amuText = A.text();
					private final MOB mob=msg.source();

					@Override
					public void run()
					{
						if(mob.fetchEffect("Amputation")==null)
						{
							final Ability A=CMClass.getAbility("Amputation");
							mob.addNonUninvokableEffect(A);
							A.setMiscText(amuText);
							mob.recoverCharStats();
							mob.recoverMaxState();
							mob.recoverPhyStats();
						}
					}
				});
			}
		}

		if (msg.amITarget(myHost)
		&& (msg.targetMinor() == CMMsg.TYP_GIVE)
		&& (msg.source() != myHost)
		&& (msg.tool() instanceof Coins)
		&& (myHost instanceof MOB)
		&& (((MOB)myHost).getVictim()==msg.source()))
		{
			final double amt = ((Coins)msg.tool()).getTotalValue();
			final double min = (50.0 * ((MOB)myHost).phyStats().level()); 
			if(amt >= min)
			{
				final MOB pirate = (MOB)myHost;
				msg.addTrailerRunnable(new Runnable()
				{
					@Override
					public void run()
					{
						pirate.makePeace(true);
						Room R=CMLib.map().roomLocation(msg.source());
						Ability A=pirate.fetchEffect("Prop_PiratePaidOff");
						if(A==null)
						{
							pirate.addNonUninvokableEffect(A=new StdAbility()
							{
								private final Map<MOB,Long> timeOuts = new SHashtable<MOB,Long>();

								@Override
								public String ID()
								{
									return "Prop_PiratePaidOff";
								}
								
								@Override
								public void setMiscText(String newMiscText)
								{
									if(newMiscText.startsWith("+"))
									{
										Room R=CMLib.map().roomLocation(affected);
										if(R!=null)
										{
											final MOB M=R.fetchInhabitant(newMiscText.substring(1));
											if(M!=null)
											{
												final Long newTime = Long.valueOf(System.currentTimeMillis() + (10L * 60L * 1000L));
												synchronized(timeOuts)
												{
													timeOuts.put(M,newTime);
												}
											}
										}
									}
									else
										super.setMiscText(newMiscText);
									final long now=System.currentTimeMillis();
									synchronized(timeOuts)
									{
										for(MOB M : timeOuts.keySet())
										{
											final Long L=timeOuts.get(M);
											if((L!=null)&&(L.longValue() < now))
											{
												timeOuts.remove(M);
											}
										}
									}
								}
								
								@Override
								public boolean isSavable()
								{
									return false;
								}

								@Override
								public boolean okMessage(Environmental myHost, CMMsg msg)
								{
									if(!super.okMessage(myHost, msg))
										return false;
									if((msg.target() instanceof MOB)
									&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS)))
									{
										if(msg.source() == pirate)
										{
											final MOB victimM=(MOB)msg.target();
											MOB paidOneM = victimM;
											Long timeOut;
											synchronized(timeOuts)
											{
												timeOut = timeOuts.get(victimM);
											}
											final MOB ultiM=victimM.amUltimatelyFollowing();
											if((timeOut == null)&&(ultiM!=null))
											{
												synchronized(timeOuts)
												{
													timeOut = timeOuts.get(ultiM);
												}
												if(timeOut != null)
													paidOneM= ultiM;
											}
											if(timeOut != null)
											{
												if(System.currentTimeMillis() < timeOut.longValue())
												{
													if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
														msg.source().tell(L("@x1 paid you off, so you can't attack @x2 for awhile.",paidOneM.name(pirate),victimM.charStats().himher()));
													victimM.makePeace(true);
													msg.source().makePeace(true);
													return false;
												}
												else
												{
													timeOuts.remove(paidOneM);
													if(timeOuts.size()==0)
														pirate.delEffect(this);
												}
											}
										}
										else
										if(msg.target() == pirate)
										{
											final MOB attackerM=msg.source();
											MOB paidOneM = attackerM;
											Long timeOut;
											synchronized(timeOuts)
											{
												timeOut = timeOuts.get(attackerM);
											}
											final MOB ultiM=attackerM.amUltimatelyFollowing();
											if((timeOut == null)&&(ultiM!=null))
											{
												synchronized(timeOuts)
												{
													timeOut = timeOuts.get(ultiM);
												}
												if(timeOut != null)
													paidOneM= ultiM;
											}
											if(timeOut != null)
											{
												if(System.currentTimeMillis() < timeOut.longValue())
												{
													if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
														msg.source().tell(paidOneM,null,null,L("<S-NAME> paid off @x1, so you can't attack @x2 for awhile.",pirate.name(attackerM),pirate.charStats().himher()));
													pirate.makePeace(true);
													msg.source().makePeace(true);
													return false;
												}
												else
												{
													timeOuts.remove(paidOneM);
													if(timeOuts.size()==0)
														pirate.delEffect(this);
												}
											}
										}
									}
									return true;
								}
							});
						}
						A.setMiscText("+"+R.getContextName(msg.source()));
					}
				});
			}
			else
			{
				final String minAmt = CMLib.beanCounter().abbreviatedPrice(((Coins)msg.tool()).getCurrency(), min);
				final String himHer = ((MOB)myHost).charStats().himher();
				msg.source().tell(L("You'll need to fork over at lease @x1 to make @x2 go away.",minAmt,himHer));
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.value()>0)
		&&(msg.source().charStats().getCurrentClass() == this)
		&&(CMLib.map().areaLocation(msg.source()) instanceof BoardableShip)
		&&(msg.source() != msg.target()))
		{
			if(msg.target() instanceof MOB)
				msg.setValue(msg.value() * 3);
			else
			if(msg.target() == null)
			{
				BoardableShip shipArea = (BoardableShip)CMLib.map().areaLocation(msg.source());
				Room R=CMLib.map().roomLocation(shipArea.getShipItem());
				if(R!=null)
				{
					for(Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						Item I=i.nextElement();
						if((I instanceof BoardableShip)
						&&(I.fetchEffect("Sinking")!=null)
						&&(I!=shipArea.getShipItem()))
						{
							msg.setValue(msg.value() * 2);
							break;
						}
					}
				}
			}
		}
		if((msg.targetMinor()==CMMsg.MSG_LEGALWARRANT)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).charStats().getCurrentClass()==this)
		&&(((MOB)msg.target()).location()!=null))
		{
			LegalBehavior behav = CMLib.law().getLegalBehavior(((MOB)msg.target()).location());
			Area area = CMLib.law().getLegalObject(((MOB)msg.target()).location());
			List<LegalWarrant> warrants = behav.getWarrantsOf(area, (MOB)msg.target());
			for(LegalWarrant W : warrants)
			{
				if((W.victim()==msg.tool())
				&&(W.crime() == msg.targetMessage()))
					W.setPunishment(W.punishment()+1);
			}
		}
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			int numLimbs = 0;
			for(Enumeration<Item> i=((MOB)affected).items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I instanceof FalseLimb)
				&&(!I.amWearingAt(Item.IN_INVENTORY))
				&&(!I.amWearingAt(Wearable.WORN_HELD)))
				{
					numLimbs++;
				}
			}
			if(numLimbs > 0)
			{
				affectableStats.setDamage(affectableStats.damage() + numLimbs);
				affectableStats.setAttackAdjustment(affectableStats.damage() + (5 * numLimbs));
				affectableStats.setArmor(affectableStats.armor() - (5 * numLimbs));
			}
		}
		
	}
}
