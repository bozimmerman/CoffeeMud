package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FData;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;

/*
   Copyright 2020-2020 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, e\ither express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
//STOP-ECLIPSE-FORMATTING
// @formatter: off
public class StdMobWrapper implements MOB, CMObjectWrapper
{
	@Override
	public String ID()
	{
		return "StdMobWrapper";
	}

	protected CharStats			baseCharStats	= (CharStats) CMClass.getCommon("DefaultCharStats");
	protected PhyStats			basePhyStats	= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected CharState			curState		= (CharState) CMClass.getCommon("DefaultCharState");
	protected volatile Room		location		= null;
	protected volatile Room		lastLocation	= null;
	protected Rideable			riding			= null;
	protected boolean			destroyed		= false;

	protected MOB mob = null;

	@Override
	public void setWrappedObject(final CMObject obj)
	{
		if(obj instanceof MOB)
		{
			this.mob=(MOB)obj;
			this.mob.phyStats().copyInto(basePhyStats);
			this.mob.charStats().copyInto(baseCharStats);
			this.mob.curState().copyInto(curState);
		}
	}

	@Override
	public CMObject getWrappedObject()
	{
		return this.mob;
	}

	@Override
	public void setRiding(final Rideable ride)
	{
		riding=ride;
	}

	@Override
	public Rideable riding()
	{
		return riding;
	}

	@Override
	public String displayText(final MOB viewerMob)
	{
		return (mob == null) ? "" : mob.displayText(viewerMob);
	}

	@Override
	public String name(final MOB viewerMob)
	{
		return (mob == null) ? "" : mob.name(viewerMob);
	}

	@Override
	public String description(final MOB viewerMob)
	{
		return (mob == null) ? "" : mob.description(viewerMob);
	}

	@Override
	public String Name()
	{
		return (mob == null) ? "" : mob.Name();
	}

	@Override
	public void setName(final String newName)
	{
	}

	@Override
	public String displayText()
	{
		return (mob == null) ? "" : mob.displayText();
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
	}

	@Override
	public String description()
	{
		return (mob == null) ? "" : mob.description();
	}

	@Override
	public void setDescription(final String newDescription)
	{
	}

	@Override
	public String image()
	{
		return (mob == null) ? "" : mob.image();
	}

	@Override
	public String rawImage()
	{
		return (mob == null) ? "" : mob.rawImage();
	}

	@Override
	public void setImage(final String newImage)
	{
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
	}

	@Override
	public String text()
	{
		return "";
	}

	@Override
	public String miscTextFormat()
	{
		return (mob == null) ? "" : mob.miscTextFormat();
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		return (E instanceof StdMobWrapper)&&(mob == ((StdMobWrapper)E).mob);
	}

	@Override
	public long expirationDate()
	{
		return (mob == null) ? 0 : mob.expirationDate();
	}

	@Override
	public void setExpirationDate(final long dateTime)
	{
	}

	@Override
	public int maxRange()
	{
		return (mob == null) ? 0 : mob.maxRange();
	}

	@Override
	public int minRange()
	{
		return (mob == null) ? 0 : mob.minRange();
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().L(str, xs);
	}

	@Override
	public String name()
	{
		return (mob == null) ? "" : mob.name();
	}

	@Override
	public int getTickStatus()
	{
		return (mob == null) ? 0 : mob.getTickStatus();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return false;
	}

	@Override
	public CMObject newInstance()
	{
		return new StdMobWrapper();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject)this.clone();
		}
		catch (final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return true;
	}

	@Override
	public void destroy()
	{
		destroyed=true;
	}

	@Override
	public boolean isSavable()
	{
		return false;
	}

	@Override
	public boolean amDestroyed()
	{
		return destroyed;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
	}

	@Override
	public String[] getStatCodes()
	{
		return new String[0];
	}

	@Override
	public int getSaveStatIndex()
	{
		return 0;
	}

	@Override
	public String getStat(final String code)
	{
		return "";
	}

	@Override
	public boolean isStat(final String code)
	{
		return false;
	}

	@Override
	public void setStat(final String code, final String val)
	{
	}

	@Override
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}

	@Override
	public void setBasePhyStats(final PhyStats newStats)
	{
		newStats.copyInto(basePhyStats);
	}

	@Override
	public PhyStats phyStats()
	{
		return basePhyStats;
	}

	@Override
	public void recoverPhyStats()
	{
	}

	@Override
	public void addEffect(final Ability to)
	{
	}

	@Override
	public void addNonUninvokableEffect(final Ability to)
	{
	}

	@Override
	public void delEffect(final Ability to)
	{
	}

	@Override
	public int numEffects()
	{
		return 0;
	}

	@Override
	public Ability fetchEffect(final int index)
	{
		return null;
	}

	@Override
	public Ability fetchEffect(final String ID)
	{
		return null;
	}

	@Override
	public Enumeration<Ability> effects()
	{
		return new EmptyEnumeration<Ability>();
	}

	@Override
	public void delAllEffects(final boolean unInvoke)
	{
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
	}

	@Override
	public void addBehavior(final Behavior to)
	{
	}

	@Override
	public void delBehavior(final Behavior to)
	{
	}

	@Override
	public int numBehaviors()
	{
		return 0;
	}

	@Override
	public Behavior fetchBehavior(final int index)
	{
		return null;
	}

	@Override
	public Behavior fetchBehavior(final String ID)
	{
		return null;
	}

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return new EmptyEnumeration<Behavior>();
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
	}

	@Override
	public void addScript(final ScriptingEngine s)
	{
	}

	@Override
	public void delAllBehaviors()
	{
	}

	@Override
	public void delScript(final ScriptingEngine s)
	{
	}

	@Override
	public void delAllScripts()
	{
	}

	@Override
	public int numScripts()
	{
		return 0;
	}

	@Override
	public ScriptingEngine fetchScript(final int x)
	{
		return null;
	}

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return new EmptyEnumeration<ScriptingEngine>();
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
	}

	@Override
	public String databaseID()
	{
		return (mob == null) ? "" : mob.databaseID();
	}

	@Override
	public void setDatabaseID(final String ID)
	{
	}

	@Override
	public boolean canSaveDatabaseID()
	{
		return false;
	}

	@Override
	public void addItem(final Item item, final Expire expire)
	{
	}

	@Override
	public void moveItemTo(final Item container, final Expire expire, final Move... moveFlags)
	{
	}

	@Override
	public void moveItemTo(final Item container)
	{
	}

	@Override
	public void addItem(final Item item)
	{
	}

	@Override
	public void delItem(final Item item)
	{
	}

	@Override
	public int numItems()
	{
		return 0;
	}

	@Override
	public Item getItem(final int i)
	{
		return null;
	}

	@Override
	public Item getRandomItem()
	{
		return null;
	}

	@Override
	public Enumeration<Item> items()
	{
		return new EmptyEnumeration<Item>();
	}

	@Override
	public Item findItem(final Item goodLocation, final String itemID)
	{
		return null;
	}

	@Override
	public Item findItem(final String itemID)
	{
		return null;
	}

	@Override
	public List<Item> findItems(final Item goodLocation, final String itemID)
	{
		return new ArrayList<Item>(0);
	}

	@Override
	public List<Item> findItems(final String itemID)
	{
		return new ArrayList<Item>(0);
	}

	@Override
	public boolean isContent(final Item item)
	{
		return false;
	}

	@Override
	public void delAllItems(final boolean destroy)
	{
	}

	@Override
	public void eachItem(final EachApplicable<Item> applier)
	{
	}

	@Override
	public void addAbility(final Ability to)
	{
	}

	@Override
	public void delAbility(final Ability to)
	{
	}

	@Override
	public int numAbilities()
	{
		return 0;
	}

	@Override
	public Ability fetchAbility(final int index)
	{
		return null;
	}

	@Override
	public Ability fetchAbility(final String ID)
	{
		return null;
	}

	@Override
	public Ability fetchRandomAbility()
	{
		return null;
	}

	@Override
	public Enumeration<Ability> abilities()
	{
		return new EmptyEnumeration<Ability>();
	}

	@Override
	public void delAllAbilities()
	{
	}

	@Override
	public int numAllAbilities()
	{
		return 0;
	}

	@Override
	public Enumeration<Ability> allAbilities()
	{
		return new EmptyEnumeration<Ability>();
	}

	@Override
	public void addTattoo(final Tattoo of)
	{
	}

	@Override
	public void addTattoo(final String of)
	{
	}

	@Override
	public void addTattoo(final String of, final int tickDown)
	{
	}

	@Override
	public void delTattoo(final Tattoo of)
	{
	}

	@Override
	public void delTattoo(final String of)
	{
	}

	@Override
	public Enumeration<Tattoo> tattoos()
	{
		return new EmptyEnumeration<Tattoo>();
	}

	@Override
	public Tattoo findTattoo(final String of)
	{
		return null;
	}

	@Override
	public Tattoo findTattooStartsWith(final String of)
	{
		return null;
	}

	@Override
	public void addFaction(final String of, final int start)
	{
	}

	@Override
	public void adjustFaction(final String of, final int amount)
	{
	}

	@Override
	public Enumeration<String> factions()
	{
		return new EmptyEnumeration<String>();
	}

	@Override
	public List<String> fetchFactionRanges()
	{
		return new ArrayList<String>(0);
	}

	@Override
	public boolean hasFaction(final String which)
	{
		return false;
	}

	@Override
	public int fetchFaction(final String which)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public FData fetchFactionData(final String which)
	{
		return null;
	}

	@Override
	public String getFactionListing()
	{
		return (mob == null) ? "" : mob.getFactionListing();
	}

	@Override
	public void removeFaction(final String which)
	{
	}

	@Override
	public void copyFactions(final FactionMember source)
	{
	}

	@Override
	public void enqueCommand(final List<String> commands, final int metaFlags, final double actionCost)
	{
	}

	@Override
	public void enqueCommands(final List<List<String>> commands, final int metaFlags)
	{
	}

	@Override
	public void prequeCommand(final List<String> commands, final int metaFlags, final double actionCost)
	{
	}

	@Override
	public void prequeCommands(final List<List<String>> commands, final int metaFlags)
	{
	}

	@Override
	public boolean dequeCommand()
	{
		return false;
	}

	@Override
	public void clearCommandQueue()
	{
	}

	@Override
	public int commandQueSize()
	{
		return 0;
	}

	@Override
	public void doCommand(final List<String> commands, final int metaFlags)
	{
	}

	@Override
	public double actions()
	{
		return 0;
	}

	@Override
	public void setActions(final double remain)
	{
	}

	@Override
	public void addFollower(final MOB follower, final int order)
	{
	}

	@Override
	public void delFollower(final MOB follower)
	{
	}

	@Override
	public int numFollowers()
	{
		return 0;
	}

	@Override
	public int fetchFollowerOrder(final MOB thisOne)
	{
		return 0;
	}

	@Override
	public boolean isFollowedBy(final MOB thisOne)
	{
		return false;
	}

	@Override
	public Enumeration<Pair<MOB, Short>> followers()
	{
		return new EmptyEnumeration<Pair<MOB,Short>>();
	}

	@Override
	public MOB fetchFollower(final int index)
	{
		return null;
	}

	@Override
	public MOB fetchFollower(final String named)
	{
		return null;
	}

	@Override
	public int totalFollowers()
	{
		return 0;
	}

	@Override
	public MOB amFollowing()
	{
		return null;
	}

	@Override
	public MOB amUltimatelyFollowing()
	{
		return null;
	}

	@Override
	public void setFollowing(final MOB mob)
	{
	}

	@Override
	public Set<MOB> getGroupMembers(final Set<MOB> list)
	{
		return list;
	}

	@Override
	public Set<? extends Rider> getGroupMembersAndRideables(final Set<? extends Rider> list)
	{
		return list;
	}

	@Override
	public boolean isInCombat()
	{
		return (mob == null) ? false : mob.isInCombat();
	}

	@Override
	public void setRangeToTarget(final int newRange)
	{
	}

	@Override
	public int rangeToTarget()
	{
		return (mob == null) ? 0 : mob.rangeToTarget();
	}

	@Override
	public int getDirectionToTarget()
	{
		return (mob == null) ? 0 : mob.getDirectionToTarget();
	}

	@Override
	public boolean mayPhysicallyAttack(final PhysicalAgent victim)
	{
		return (mob == null) ? false : mob.mayPhysicallyAttack(victim);
	}

	@Override
	public boolean mayIFight(final PhysicalAgent victim)
	{
		return (mob == null) ? false : mob.mayIFight(victim);
	}

	@Override
	public void makePeace(final boolean includePlayerFollowers)
	{
	}

	@Override
	public PhysicalAgent getCombatant()
	{
		return null;
	}

	@Override
	public void setCombatant(final PhysicalAgent other)
	{
	}

	@Override
	public int getAttributesBitmap()
	{
		return (mob == null) ? 0 : mob.getAttributesBitmap();
	}

	@Override
	public void setAttributesBitmap(final int bitmap)
	{
	}

	@Override
	public void setAttribute(final Attrib attrib, final boolean set)
	{
	}

	@Override
	public boolean isAttributeSet(final Attrib attrib)
	{
		return (mob == null) ? false : mob.isAttributeSet(attrib);
	}

	@Override
	public String titledName()
	{
		return (mob == null) ? "" : mob.titledName();
	}
	
	@Override
	public String titledName(final MOB viewer)
	{
		return (mob == null) ? "" : mob.titledName(viewer);
	}


	@Override
	public String genericName()
	{
		return (mob == null) ? "" : mob.genericName();
	}

	@Override
	public boolean isPlayer()
	{
		return false;
	}

	@Override
	public PlayerStats playerStats()
	{
		return null;
	}

	@Override
	public void setPlayerStats(final PlayerStats newStats)
	{
	}

	@Override
	public CharStats baseCharStats()
	{
		return baseCharStats;
	}

	@Override
	public CharStats charStats()
	{
		return baseCharStats;
	}

	@Override
	public void recoverCharStats()
	{
	}

	@Override
	public void setBaseCharStats(final CharStats newBaseCharStats)
	{
	}

	@Override
	public int maxCarry()
	{
		return (mob == null) ? 0 : mob.maxCarry();
	}

	@Override
	public int maxItems()
	{
		return (mob == null) ? 0 : mob.maxItems();
	}

	@Override
	public int baseWeight()
	{
		return (mob == null) ? 0 : mob.baseWeight();
	}

	@Override
	public String healthText(final MOB viewer)
	{
		return (mob == null) ? "" : mob.healthText(viewer);
	}

	@Override
	public boolean amDead()
	{
		return (mob == null) ? false : mob.amDead();
	}

	@Override
	public DeadBody killMeDead(final boolean createBody)
	{
		return null;
	}

	@Override
	public void bringToLife(final Room newLocation, final boolean resetStats)
	{
	}

	@Override
	public void bringToLife()
	{
	}

	@Override
	public void removeFromGame(final boolean preserveFollowers, final boolean killSession)
	{
	}

	@Override
	public boolean amActive()
	{
		return (mob == null) ? false : mob.amActive();
	}

	@Override
	public MOB getVictim()
	{
		return null;
	}

	@Override
	public void setVictim(final MOB other)
	{
	}

	@Override
	public void tell(final MOB source, final Environmental target, final Environmental tool, final String msg)
	{
	}

	@Override
	public void tell(final String msg)
	{
	}

	@Override
	public Session session()
	{
		return null;
	}

	@Override
	public void setSession(final Session newSession)
	{
	}

	@Override
	public boolean isMonster()
	{
		return true;
	}

	@Override
	public boolean isPossessing()
	{
		return false;
	}

	@Override
	public MOB soulMate()
	{
		return null;
	}

	@Override
	public void setSoulMate(final MOB mob)
	{
	}

	@Override
	public void dispossess(final boolean forceLook)
	{
	}

	@Override
	public int getExperience()
	{
		return (mob == null) ? 0 : mob.getExperience();
	}

	@Override
	public void setExperience(final int newVal)
	{
	}

	@Override
	public long getPeaceTime()
	{
		return (mob == null) ? 0 : mob.getPeaceTime();
	}

	@Override
	public int getExpNextLevel()
	{
		return (mob == null) ? 0 : mob.getExpNextLevel();
	}

	@Override
	public int getExpNeededLevel()
	{
		return (mob == null) ? 0 : mob.getExpNeededLevel();
	}

	@Override
	public int getExpNeededDelevel()
	{
		return (mob == null) ? 0 : mob.getExpNeededDelevel();
	}

	@Override
	public int getExpPrevLevel()
	{
		return (mob == null) ? 0 : mob.getExpPrevLevel();
	}

	@Override
	public long getAgeMinutes()
	{
		return (mob == null) ? 0 : mob.getAgeMinutes();
	}

	@Override
	public void setAgeMinutes(final long newVal)
	{
	}

	@Override
	public int getPractices()
	{
		return (mob == null) ? 0 : mob.getPractices();
	}

	@Override
	public void setPractices(final int newVal)
	{
	}

	@Override
	public int getTrains()
	{
		return (mob == null) ? 0 : mob.getTrains();
	}

	@Override
	public void setTrains(final int newVal)
	{
	}

	@Override
	public int getMoney()
	{
		return (mob == null) ? 0 : mob.getMoney();
	}

	@Override
	public void setMoney(final int newVal)
	{
	}

	@Override
	public double getMoneyVariation()
	{
		return (mob == null) ? 0 : mob.getMoneyVariation();
	}

	@Override
	public void setMoneyVariation(final double newVal)
	{
	}

	@Override
	public CharState baseState()
	{
		return curState;
	}

	@Override
	public void setBaseState(final CharState newState)
	{
		newState.copyInto(curState);

	}

	@Override
	public CharState curState()
	{
		return curState;
	}

	@Override
	public CharState maxState()
	{
		return curState;
	}

	@Override
	public void recoverMaxState()
	{
	}

	@Override
	public void resetToMaxState()
	{
	}

	@Override
	public Weapon getNaturalWeapon()
	{
		return (mob == null) ? null : mob.getNaturalWeapon();
	}

	@Override
	public String getLiegeID()
	{
		return (mob == null) ? "" : mob.getLiegeID();
	}

	@Override
	public void setLiegeID(final String newVal)
	{
	}

	@Override
	public boolean isMarriedToLiege()
	{
		return (mob == null) ? false : mob.isMarriedToLiege();
	}

	@Override
	public String getWorshipCharID()
	{
		return (mob == null) ? "" : mob.getWorshipCharID();
	}

	@Override
	public void setWorshipCharID(final String newVal)
	{
	}

	@Override
	public Deity getMyDeity()
	{
		return (mob == null) ? null : mob.getMyDeity();
	}

	@Override
	public int getWimpHitPoint()
	{
		return (mob == null) ? 0 : mob.getWimpHitPoint();
	}

	@Override
	public void setWimpHitPoint(final int newVal)
	{
	}

	@Override
	public int getQuestPoint()
	{
		return (mob == null) ? 0 : mob.getQuestPoint();
	}

	@Override
	public void setQuestPoint(final int newVal)
	{
	}

	@Override
	public long lastTickedDateTime()
	{
		return (mob == null) ? 0 : mob.lastTickedDateTime();
	}

	@Override
	public Iterable<Pair<Clan, Integer>> clans()
	{
		return new ArrayList<Pair<Clan, Integer>>();
	}

	@Override
	public Pair<Clan, Integer> getClanRole(final String clanID)
	{
		return null;
	}

	@Override
	public void setClan(final String clanID, final int role)
	{
	}

	@Override
	public Room getStartRoom()
	{
		return (mob == null) ? null : mob.getStartRoom();
	}

	@Override
	public void setStartRoom(final Room newRoom)
	{
	}

	@Override
	public Room location()
	{
		return (mob == null) ? null : location;
	}

	@Override
	public void setLocation(final Room newRoom)
	{
		location=newRoom;
	}

	@Override
	public void flagVariableEq()
	{
	}

	@Override
	public Item fetchItem(final Item goodLocation, final Filterer<Environmental> filter, final String itemName)
	{
		return null;
	}

	@Override
	public List<Item> fetchWornItems(final long wornCode, final short aboveOrAroundLayer, final short layerAttributes)
	{
		return new ArrayList<Item>(0);
	}

	@Override
	public Item fetchFirstWornItem(final long wornCode)
	{
		return (mob == null) ? null : mob.fetchFirstWornItem(wornCode);
	}

	@Override
	public Item fetchWieldedItem()
	{
		return (mob == null) ? null : mob.fetchWieldedItem();
	}

	@Override
	public Item fetchHeldItem()
	{
		return (mob == null) ? null : mob.fetchHeldItem();
	}

	@Override
	public boolean hasOnlyGoldInInventory()
	{
		return (mob == null) ? false : mob.hasOnlyGoldInInventory();
	}

	@Override
	public int freeWearPositions(final long wornCode, final short belowLayer, final short layerAttributes)
	{
		return (mob == null) ? 0 : mob.freeWearPositions(wornCode, belowLayer, layerAttributes);
	}

	@Override
	public int getWearPositions(final long wornCode)
	{
		return (mob == null) ? 0 : mob.getWearPositions(wornCode);
	}

	@Override
	public boolean isMine(final Environmental env)
	{
		return (mob == null) ? false : mob.isMine(env);
	}

	@Override
	public int numAllEffects()
	{
		return 0;
	}

	@Override
	public void addPriorityEffect(final Ability to)
	{
	}

	@Override
	public Enumeration<Ability> personalEffects()
	{
		return new EmptyEnumeration<Ability>();
	}

	@Override
	public Set<MOB> getRideBuddies(final Set<MOB> list)
	{
		return null;
	}

	@Override
	public boolean willFollowOrdersOf(final MOB mob)
	{
		return false;
	}

	@Override
	public int maxFollowers()
	{
		return 0;
	}

	@Override
	public Ability findAbility(final String name)
	{
		return null;
	}

	@Override
	public int[][] getAbilityUsageCache(final String abilityID)
	{
		return (mob == null) ? null : mob.getAbilityUsageCache(abilityID);
	}

	@Override
	public boolean isRacialAbility(final String abilityID)
	{
		return false;
	}

	@Override
	public void addExpertise(final String code)
	{
	}

	@Override
	public void delExpertise(final String baseCode)
	{
	}

	@Override
	public Pair<String, Integer> fetchExpertise(final String code)
	{
		return null;
	}

	@Override
	public void delAllExpertises()
	{
	}

	@Override
	public Enumeration<String> expertises()
	{
		return new EmptyEnumeration<String>();
	}


}
