package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Thief.Thief_Articles;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftedItem;
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
   Copyright 2016-2025 Bo Zimmerman

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
	public void setAbilityCode(final int newCode)
	{
		this.abilityCode = newCode;
	}

	@Override
	public CMObject copyOf()
	{
		final Skill_HireCrewmember A=(Skill_HireCrewmember)super.copyOf();
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
	protected CrewType	type		= null;
	protected Behavior	sailor		= null;

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		final int x=newMiscText.indexOf(';');
		if(x>0)
		{
			shipName=newMiscText.substring(0,x);
			type=(CrewType)CMath.s_valueOf(CrewType.class, newMiscText.substring(x+1));
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(type != null)
		{
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
	}

	public Behavior getSailor()
	{
		if(affected instanceof PhysicalAgent)
		{
			final PhysicalAgent agent=(PhysicalAgent)affected;
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
							final Ability A=CMClass.getAbility("Shipwright");
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
					sailor.setParms("PEACEMOVER=true WANDEROK=true FIGHTMOVER=false FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				case TRAWLER:
					if(agent instanceof MOB)
					{
						final MOB mob=(MOB)agent;
						if(mob.fetchAbility("Trawling")==null)
						{
							final Ability A=CMClass.getAbility("Trawling");
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
		final Physical affected=this.affected;
		if(affected instanceof PhysicalAgent)
		{
			final PhysicalAgent agent=(PhysicalAgent)affected;
			final Behavior B=agent.fetchBehavior("Sailor");
			if(B!=null)
				agent.delBehavior(B);
			final Room R=CMLib.map().roomLocation(agent);
			if(agent instanceof MOB)
			{
				R.show((MOB)agent, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> quit(s)."));
				CMLib.tracking().wanderAway((MOB)agent, false, false);
			}
		}
		super.unInvoke();
		if(affected != null)
			affected.destroy();
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		return true;
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
	}

	protected boolean isCrew(final MOB M, final String shipName)
	{
		final Skill_HireCrewmember articlesA=(Skill_HireCrewmember)M.fetchEffect(ID());
		return ((articlesA!=null)&&(articlesA.shipName.equals(shipName)));
	}

	protected List<Item> craftGear(final MOB mob, final String[] gearNames, final String... skillNames)
	{
		final List<Item> items = new ArrayList<Item>();
		final ItemCraftor[] craftors = new ItemCraftor[skillNames.length];
		final int[] mats = new int[skillNames.length];
		int c=0, m=0;
		for(final String skillName : skillNames)
		{
			final int mat = RawMaterial.CODES.FIND_IgnoreCase(skillName);
			if(mat >0)
				mats[m++]=m;
			else
				craftors[c++]=(ItemCraftor)CMClass.getAbility(skillName);
		}
		for(final String bit : gearNames)
		{
			Item I=null;
			for(final ItemCraftor craftor : craftors)
			{
				final CraftedItem item = craftor.craftItem(bit, -1, true, false);
				if((item != null)
				&&(item.item != null))
				{
					I=item.item;
					break;
				}
			}
			if(I != null)
			{
				I.setBaseValue(1);
				if(I.basePhyStats().level()>mob.phyStats().level())
				{
					I.basePhyStats().setLevel(mob.phyStats().level());
					I.phyStats().setLevel(mob.phyStats().level());
				}
				mob.addItem(I);
				I.wearIfPossible(mob);
				if(!I.amBeingWornProperly())
					I.destroy();
				else
					items.add(I);
			}
		}
		return items;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return false;
		if((type == null)||(this.shipName.length()==0))
		{
			final Physical affected=this.affected;
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Room R=mob.location();
				if((mob.amDead())||(R==null)||(!CMLib.flags().isInTheGame(mob, true)))
				{
					this.unInvoke();
					return false;
				}

				if((R.getArea() instanceof Boardable)
				&&(((Boardable)R.getArea()).getBoardableItem() instanceof NavigableItem)
				&&(((NavigableItem)((Boardable)R.getArea()).getBoardableItem()).navBasis()==Rideable.Basis.WATER_BASED))
				{
					final Area shipArea=R.getArea();
					final NavigableItem ship = (NavigableItem)((Boardable)shipArea).getBoardableItem();
					if(this.type==null)
					{
						int numRooms=0;
						int numCrew=0;
						int numDecks=0;
						final int[] numTypes=new int[CrewType.values().length];
						for(final Enumeration<Room> r=shipArea.getProperMap();r.hasMoreElements();)
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
							for(final Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if((M!=null)
								&&(M.isMonster())
								&&(isCrew(M,ship.Name())))
								{
									numCrew++;
									numTypes[getCrewType(M).ordinal()]++;
								}
							}
						}

						final int bonus= ( adjustedLevel(mob,0) / 10);
						if(bonus > 0)
						{
							final int bonusDecks = bonus / 2;
							numRooms += bonus;
							numDecks += bonusDecks;
						}

						CrewType nextType = null;
						final int maxCaptains = 1;
						final int maxTacticians = 1;
						final int maxDefenders = numDecks;
						final int maxTrawlers = numDecks;
						final int maxRepairers = (int)Math.round(Math.ceil(CMath.div((numRooms-numDecks),2.0)));

						if(numCrew >= (maxRepairers + maxTrawlers + maxDefenders + maxTacticians + maxCaptains))
						{
							CMLib.commands().postSay(mob, L("This ship already has the maximum crew."));
							unInvoke();
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
									if(numTypes[nextType.ordinal()]>=maxTrawlers)
										nextType=null;
									break;
								case DEFENDER:
									if(numTypes[nextType.ordinal()]>=maxDefenders)
										nextType=null;
									break;
								case CAPTAIN:
									if(numTypes[nextType.ordinal()]>=maxCaptains)
										nextType=null;
									break;
								case TACTICIAN:
									if(numTypes[nextType.ordinal()]>=maxTacticians)
										nextType=null;
									break;
								case REPAIRER:
									if(numTypes[nextType.ordinal()]>=maxRepairers)
										nextType=null;
									break;
								}
							}
						}

						if(nextType == null)
						{
							CMLib.commands().postSay(mob, L("This ship already has enough crew."));
							unInvoke();
							return false;
						}

						this.type=nextType;
						mob.recoverPhyStats();
						R.recoverRoomStats();
						mob.recoverPhyStats();
					}
					if(mob.amFollowing()!=null)
					{
						// I'm only done when I'm no longer following.
					}
					else
					{
						this.setMiscText(ship.Name()+";"+type.name());
						this.setAbilityCode(super.getXLEVELLevel(mob));
						this.makeNonUninvokable();
						this.setSavable(true);
						getSailor();
						//metacraft white pants, blue and white-striped shirts and white hats for everyone else.
						if((type != CrewType.DEFENDER)
						&&(type != CrewType.CAPTAIN))
						{
							final String[] gear = new String[] {
								"pants", "shirt", "Sailor`s Cap"
							};
							final List<Item> Is = craftGear(mob, gear, "Tailoring", "COTTON");
							for(final Item I : Is)
							{
								final String on = I.Name();
								final int x=on.indexOf(' ');
								I.setName(on.substring(0,x)+" ^wwhite^N"+on.substring(x));
								I.setDisplayText(CMStrings.replaceAll(I.displayText(), on, I.Name()));
							}
						}
						switch(type)
						{
						case DEFENDER:
						{
							final String[] gear = new String[] {
								"vambrace", "gauntlets", "vest", "skullcap", "leggings", "bullwhip", "small shield"
							};
							craftGear(mob, gear, "LeatherWorking", "LEATHER", "Carpentry", "OAK");
							break;
						}
						case REPAIRER:
							craftGear(mob, new String[] {"craftsmans hammer"}, "Weaponsmithing", "IRON");
							break;
						case CAPTAIN:
						{
							final String[] gear = new String[] {
								"pants", "shirt", "Captain`s Hat"
							};
							final List<Item> Is = craftGear(mob, gear, "Tailoring", "COTTON");
							for(final Item I : Is)
							{
								final String on = I.Name();
								final int x=on.indexOf(' ');
								I.setName(on.substring(0,x)+" ^bblue^N"+on.substring(x));
								I.setDisplayText(CMStrings.replaceAll(I.displayText(), on, I.Name()));
							}
							break;
						}
						case TACTICIAN:
							craftGear(mob, new String[] {"drum"}, "InstrumentMaking", "OAK");
							break;
						case TRAWLER:
							craftGear(mob, new String[] {"net"}, "Weaving", "HEMP");
							break;
						}
						CMLib.commands().postSay(mob, L("I am ready for duty."));
					}
				}
				else
				if(mob.amFollowing()==null)
				{
					CMLib.commands().postSay(mob, L("I guess I'm fired."));
					unInvoke();
					return false;
				}
			}
		}
		else
			getSailor();
		return true;
	}

	protected String getCrewShip(final MOB M)
	{
		final Skill_HireCrewmember articlesA=(Skill_HireCrewmember)M.fetchEffect(ID());
		return (articlesA!=null) ? articlesA.shipName : "";
	}

	protected CrewType getCrewType(final MOB M)
	{
		final Skill_HireCrewmember articlesA=(Skill_HireCrewmember)M.fetchEffect(ID());
		return (articlesA!=null) ? articlesA.type : null;
	}

	public boolean isPub(final MOB mob, final Room room)
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

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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
		final int level = CMLib.dice().roll(1, range, minLevel);
		if(!auto)
		{
			if(!this.isPub(mob, R))
			{
				mob.tell(L("You must in a pub to hire a sailor."));
				return false;
			}
			final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
			flags.plus(TrackingLibrary.TrackingFlag.PASSABLE);
			final int roomRange = baseWaterRange + super.getXLEVELLevel(mob)+super.getXMAXRANGELevel(mob);
			final List<Room> nearby=CMLib.tracking().findTrailToAnyRoom(R, TrackingFlag.WATERSURFACEONLY.myFilter, flags, roomRange);
			if((nearby==null)||(nearby.size()==0))
			{
				mob.tell(L("There's no sea or river nearby, so no one here would be a sailor."));
				return false;
			}

			final int medLevel = minLevel + (int)Math.round(CMath.ceiling(CMath.div(range, 2.0)));
			final double amt = medLevel * 10.0;
			final String currency=R.getArea().getFinalCurrency();
			moneyStr = CMLib.beanCounter().abbreviatedPrice(currency, amt);
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob, currency) < amt)
			{
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

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			if(money > 0.0)
				CMLib.beanCounter().subtractMoney(mob, money);
			final MOB targetM=CMClass.getMOB("GenMob");
			final List<Race> races=CMLib.login().raceQualifies(mob, Area.THEME_FANTASY);
			final Race raceR=races.get(CMLib.dice().roll(1, races.size(), -1));
			final String name=CMLib.login().generateRandomName(1, 5);
			final String raceName=raceR.name();
			switch(CMLib.dice().roll(1, 5, -1))
			{
			case 0:
				targetM.setName(name);
				break;
			case 1:
				targetM.setName(L("sailor @x1",name));
				break;
			case 2:
				targetM.setName(L("a sailor"));
				break;
			case 3:
				targetM.setName(L("an @x1",raceName));
				break;
			case 4:
				targetM.setName(L("a sailor @x1",raceName));
				break;
			}
			targetM.setDisplayText(L("@x1 stands here",targetM.Name()));
			targetM.setDescription("");
			targetM.basePhyStats().setAbility(CMProps.getMobHPBase());
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
			//targetM.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience","0")); -- could be dangerous not having this, but 5-10 levels lower, so...
			targetM.addTattoo("SYSTEM_SUMMONED");
			targetM.addTattoo("SUMMONED_BY:"+mob.name());
			targetM.recoverCharStats();
			targetM.recoverPhyStats();
			targetM.recoverMaxState();
			CMLib.factions().setAlignment(targetM,Faction.Align.NEUTRAL);
			targetM.resetToMaxState();
			targetM.text();
			targetM.bringToLife(R,true);
			CMLib.beanCounter().clearZeroMoney(targetM,null);
			targetM.setMoneyVariation(0);
			//targetM.location().showOthers(targetM,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
			R.recoverRoomStats();
			targetM.setStartRoom(null);

			final String str=auto?"":L("^S<S-NAME> offer(s) <T-NAME> @x1 to hire on as a crewmember..^?",moneyStr);
			final CMMsg msg=CMClass.getMsg(mob,targetM,this,CMMsg.MSG_NOISYMOVEMENT,str,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_NOISYMOVEMENT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				R.show(targetM, null, CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> sign(s) up to be a member of your crew.",targetM.name()));
				type=null;
				final Skill_HireCrewmember A=(Skill_HireCrewmember)this.beneficialAffect(mob, targetM, asLevel, 0);
				A.type=null;
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
