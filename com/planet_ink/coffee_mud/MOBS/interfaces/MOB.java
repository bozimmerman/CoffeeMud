package com.planet_ink.coffee_mud.MOBS.interfaces;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/*
   Copyright 2000-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 * A MOB is a creature in the system, from a user
 * down to a goblin
 */
@SuppressWarnings("rawtypes")
public interface MOB extends Rider, DBIdentifiable, PhysicalAgent, ItemPossessor, AbilityContainer
{
	public static long AGE_MILLIS_THRESHOLD = 120000;
	
	public int getBitmap();
	public void setBitmap(int bitmap);
	public String titledName();
	public String genericName();

	/** Some general statistics about MOBs.  See the
	 * CharStats class (in interfaces) for more info. */
	public PlayerStats playerStats();
	public void setPlayerStats(PlayerStats newStats);
	public CharStats baseCharStats();
	public CharStats charStats();
	public void recoverCharStats();
	public void setBaseCharStats(CharStats newBaseCharStats);
	public int maxCarry();
	public int maxItems();
	public int baseWeight();
	public String healthText(MOB viewer);

	/** Combat and death */
	public boolean amDead();
	public DeadBody killMeDead(boolean createBody);
	public boolean isInCombat();
	public void bringToLife(Room newLocation, boolean resetStats);
	public void bringToLife();
	public void removeFromGame(boolean preserveFollowers, boolean killSession);
	public boolean amActive();
	public MOB getVictim();
	public void setVictim(MOB mob);
	public void makePeace();
	public void setAtRange(int newRange);
	public int maxRange(Environmental using);
	public int minRange(Environmental using);
	public int rangeToTarget();
	public boolean mayIFight(MOB mob);
	public boolean mayPhysicallyAttack(MOB mob);
	
	/** Primary mob communication */
	public void tell(MOB source, Environmental target, Environmental tool, String msg);
	public void tell(String msg);
	public void enqueCommand(List<String> commands, int metaFlags, double tickDelay);
	public void prequeCommand(Vector commands, int metaFlags, double tickDelay);
	public boolean dequeCommand();
	public void clearCommandQueue();
	public int commandQueSize();
	public void doCommand(List commands, int metaFlags);
	public double actions();
	public void setActions(double remain);

	/** Whether a sessiob object is attached to this MOB */
	public Session session();
	public void setSession(Session newSession);
	public boolean isMonster();
	public boolean isPlayer();
	public boolean isPossessing();
	public MOB soulMate();
	public void setSoulMate(MOB mob);
	public void dispossess(boolean giveMsg);

	// gained attributes
	public long getAgeMinutes();
	public int getPractices();
	public int getExperience();
	public int getExpNextLevel();
	public int getExpNeededLevel();
	public int getExpNeededDelevel();
	public int getExpPrevLevel();
	public int getTrains();
	public int getMoney();
	public double getMoneyVariation();
	public void setAgeMinutes(long newVal);
	public void setExperience(int newVal);
	public void setExpNextLevel(int newVal);
	public void setPractices(int newVal);
	public void setTrains(int newVal);
	public void setMoney(int newVal);
	public void setMoneyVariation(double newVal);

	// the core state values
	public CharState curState();
	public CharState maxState();
	public void recoverMaxState();
	public CharState baseState();
	public void setBaseState(CharState newState);
	public void resetToMaxState();
	public Weapon myNaturalWeapon();

	// misc characteristics
	public String getLiegeID();
	public void setLiegeID(String newVal);
	public boolean isMarriedToLiege();

	public String getWorshipCharID();
	public void setWorshipCharID(String newVal);
	public Deity getMyDeity();

	public int getWimpHitPoint();
	public void setWimpHitPoint(int newVal);

	public int getQuestPoint();
	public void setQuestPoint(int newVal);
	public long lastTickedDateTime();

	public Iterable<Pair<Clan,Integer>> clans();
	public Pair<Clan,Integer> getClanRole(String clanID);
	public void setClan(String clanID, int role);

	
	// location!
	public Room getStartRoom();
	public void setStartRoom(Room newRoom);
	public Room location();
	public void setLocation(Room newRoom);

	/** Manipulation of inventory, which includes held,
	 * worn, wielded, and contained items */
	public void flagVariableEq();
	public Item fetchItem(Item goodLocation, Filterer<Environmental> filter, String itemName);
	public List<Item> fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes);
	public Item fetchFirstWornItem(long wornCode);
	public Item fetchWieldedItem();
	public Item fetchHeldItem();
	public boolean hasOnlyGoldInInventory();

	public int freeWearPositions(long wornCode, short belowLayer, short layerAttributes);
	public boolean isMine(Environmental env);
	public int getWearPositions(long wornCode);

	public int numAllEffects();
	public void addPriorityEffect(Ability to);
	public Enumeration<Ability> personalEffects();

	/** Manipulation of followers */
	public void addFollower(MOB follower, int order);
	public void delFollower(MOB follower);
	public int numFollowers();
	public int fetchFollowerOrder(MOB thisOne);
	public boolean isFollowedBy(MOB thisOne);
	public Enumeration<Follower> followers();
	public MOB fetchFollower(int index);
	public MOB fetchFollower(String named);
	public MOB amFollowing();
	public MOB amUltimatelyFollowing();
	public boolean willFollowOrdersOf(MOB mob);
	public void setFollowing(MOB mob);
	public Set<MOB> getGroupMembers(Set<MOB> list);
	public Set<MOB> getRideBuddies(Set<MOB> list);
	public int maxFollowers();
	public int totalFollowers();

	/** Extra functions on ability objects, which includes
	 * spells, traits, skills, etc.*/
	public Ability findAbility(String name);
	public int[][] getAbilityUsageCache(final String abilityID);

	/**
	 * Adds a new expertise, or updates an existing one.
	 * Requires a coded expertise name (string followed by
	 * roman or decimal number)
	 * @param code the expertise to add or update
	 */
	public void addExpertise(String code);

	/**
	 * Deletes an expertise.
	 * 
	 * @param baseCode the expertise code
	 */
	public void delExpertise(String baseCode);

	/**
	 * Returns the expertise and number for the given code
	 * @param code the expertise code
	 * @return the entry with the string and number
	 */
	public Pair<String, Integer> fetchExpertise(String code);

	/**
	 * Deletes all expertises from the collection
	 */
	public void delAllExpertises();

	/**
	 * Returns an enumerator of all the expertise names
	 * with their numbers if any .
	 * @return an enumerator
	 */
	public Enumeration<String> expertises();
	
	/** Manipulation of the tatoo list */
	public void addTattoo(Tattoo of);
	public void delTattoo(Tattoo of);
	public Enumeration<Tattoo> tattoos();
	public Tattoo findTattoo(String of);

	/** Manipulation of the factions list */
	public void addFaction(String of, int start);
	public void adjustFaction(String of, int amount);
	public Enumeration<String> fetchFactions();
	public List<String> fetchFactionRanges();
	public boolean hasFaction(String which);
	public int fetchFaction(String which);
	public String getFactionListing();
	public void removeFaction(String which);
	public void copyFactions(MOB source);

	public static class Follower
	{
		public MOB follower;
		public int marchingOrder;
		public Follower(MOB M, int order){follower=M; marchingOrder=order;}
		public static final Converter<Follower,MOB> converter = new Converter<Follower,MOB>(){
			public MOB convert(Follower obj) { return obj.follower;}
		};
	}
	
	public static class Tattoo implements Cloneable, CMObject
	{
		public int tickDown=0;
		public String tattooName;
		public Tattoo(String name) { tattooName = name.toUpperCase().trim(); }
		public Tattoo(String name, int down) { tattooName = name.toUpperCase().trim(); tickDown=down;}
		public String toString() { return ((tickDown>0)?(tickDown+" "):"")+tattooName; }
		public Tattoo copyOf(){ try{ return (Tattoo)this.clone(); } catch(Exception e){ return this; }}
		public int compareTo(CMObject o) 
		{ 
			if(o==null) return 1;
			return (this==o)?0:this.ID().compareTo(o.ID());
		}
		public String ID() { return tattooName; }
		public String name() { return ID();}
		public CMObject newInstance() { return new Tattoo(tattooName); }
		public void initializeClass() {}
	}
	
	public static class QMCommand
	{
		public Object   	commandObj = null;
		public double   	actionDelay = 0.0;
		public long 		execTime = 0;
		public long 		nextCheck=System.currentTimeMillis()-1;
		public int  		seconds=-1;
		public int  		metaFlags=0;
		public List<String>	commandVector = null;
	}

	public static final int ATT_AUTOGOLD=1;
	public static final int ATT_AUTOLOOT=2;
	public static final int ATT_AUTOEXITS=4;
	public static final int ATT_AUTOASSIST=8;
	public static final int ATT_ANSI=16;
	public static final int ATT_SYSOPMSGS=32;
	public static final int ATT_AUTOMELEE=64;
	public static final int ATT_PLAYERKILL=128;
	public static final int ATT_BRIEF=256;
	public static final int ATT_NOFOLLOW=512;
	public static final int ATT_AUTOWEATHER=1024;
	public static final int ATT_AUTODRAW=2048;
	public static final int ATT_AUTOGUARD=4096;
	public static final int ATT_SOUND=8192;
	public static final int ATT_AUTOIMPROVE=16384;
	public static final int ATT_NOTEACH=32768;
	public static final int ATT_AUTONOTIFY=65536;
	public static final int ATT_AUTOFORWARD=131072;
	public static final int ATT_DAILYMESSAGE=262144;
	public static final int ATT_QUIET=524288;
	public static final int ATT_MXP=1048576;
	public static final int ATT_COMPRESS=2097152;
	public static final int ATT_AUTORUN=4194304;
	public static final int ATT_AUTOMAP=8388608;
	// maybe 9 more?


	public static final long START_SHEATH_TIME=3*CMProps.getTickMillis();
	public static final long END_SHEATH_TIME=6*CMProps.getTickMillis();

	public static final boolean[] AUTOREV={false,
										   false,
										   false,
										   true,
										   false,
										   false,
										   true,
										   false,
										   false,
										   false,
										   false,
										   false,
										   false,
										   false,
										   false,
										   false,
										   false,
										   true,
										   true,
										   false,
										   false,
										   false,
										   false,
										   true};
	public static final String[] AUTODESC={"AUTOGOLD",
										   "AUTOLOOT",
										   "AUTOEXITS",
										   "AUTOASSIST",
										   "ANSI COLOR",
										   "SYSMSGS",
										   "AUTOMELEE",
										   "PLAYERKILL",
										   "BRIEF",
										   "NOFOLLOW",
										   "AUTOWEATHER",
										   "AUTODRAW",
										   "AUTOGUARD",
										   "SOUNDS",
										   "AUTOIMPROVEMENT",
										   "NOTEACH",
										   "AUTONOTIFY",
										   "AUTOFORWARD",
										   "MOTD",
										   "QUIET",
										   "MXP",
										   "COMPRESSED",
										   "AUTORUN",
										   "AUTOMAP"};

}
