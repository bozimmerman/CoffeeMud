package com.planet_ink.coffee_mud.MOBS.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import java.util.HashSet;
import java.util.Vector;


/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface MOB extends Environmental, Rider, DBIdentifiable
{
	public int getBitmap();
	public void setBitmap(int bitmap);
	public String titledName();
    public String displayName(MOB mob);
    public String genericName();

	/** Some general statistics about MOBs.  See the
	 * CharStats class (in interfaces) for more info. */
	public PlayerStats playerStats();
	public void setPlayerStats(PlayerStats newStats);
	public CharStats baseCharStats();
	public CharStats charStats();
	public void recoverCharStats();
	public void setBaseCharStats(CharStats newBaseCharStats);
	public String displayText(MOB viewer);
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
	public long peaceTime();
	
	public void resetVectors();

	/** Primary mob communication */
	public void tell(MOB source, Environmental target, Environmental tool, String msg);
	public void tell(String msg);
	public void enqueCommand(Vector commands, int metaFlags, double tickDelay);
    public void prequeCommand(Vector commands, int metaFlags, double tickDelay);
	public boolean dequeCommand();
    public int commandQueSize();
	public void doCommand(Vector commands, int metaFlags);
    public double actions();
    public void setActions(double remain);

	/** Whether a sessiob object is attached to this MOB */
	public Session session();
	public void setSession(Session newSession);
	public boolean isMonster();
	public boolean isPossessing();
	public MOB soulMate();
	public void setSoulMate(MOB mob);
    public void dispossess(boolean giveMsg);

	// gained attributes
	public long getAgeHours();
	public int getPractices();
	public int getExperience();
	public int getExpNextLevel();
	public int getExpNeededLevel();
	public int getExpNeededDelevel();
	public int getExpPrevLevel();
	public int getTrains();
	public int getMoney();
	public double getMoneyVariation();
	public void setAgeHours(long newVal);
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
	public String getWorshipCharID();
	public String getLiegeID();
	public boolean isMarriedToLiege();
	public Deity getMyDeity();
	public String getClanID();
	public void setClanID(String clan);
	public int getClanRole();
	public void setClanRole(int role);
	public void setLiegeID(String newVal);
	public int getWimpHitPoint();
	public int getQuestPoint();
	public void setWorshipCharID(String newVal);
	public void setWimpHitPoint(int newVal);
	public void setQuestPoint(int newVal);
	public long lastTickedDateTime();

	// location!
	public Room getStartRoom();
	public void setStartRoom(Room newRoom);
	public Room location();
	public void setLocation(Room newRoom);

	/** Manipulation of inventory, which includes held,
	 * worn, wielded, and contained items */
	public void flagVariableEq();
	public void addInventory(Item item);
	public void delInventory(Item item);
	public int inventorySize();
	public Item fetchInventory(int index);
	public Item fetchFromInventory(Item goodLocation, String itemName, int wornFilter, boolean allowCoins, boolean respectLocationAndWornCode);
	public Item fetchInventory(String itemName);
	public Vector fetchInventories(String itemName);
	public Item fetchInventory(Item goodLocation, String itemName);
	public Item fetchCarried(Item goodLocation, String itemName);
	public Item fetchWornItem(String itemName);
	public Vector fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes);
	public Item fetchFirstWornItem(long wornCode);
	public Item fetchWieldedItem();
    public boolean hasOnlyGoldInInventory();


	public int freeWearPositions(long wornCode, short belowLayer, short layerAttributes);
	public boolean isMine(Environmental env);
	public void giveItem(Item thisContainer);
	public int getWearPositions(long wornCode);

	public int numAllEffects();
	public void addPriorityEffect(Ability to);

	/** Manipulation of followers */
	public void addFollower(MOB follower, int order);
	public void delFollower(MOB follower);
	public int numFollowers();
	public int fetchFollowerOrder(MOB thisOne);
	public boolean isFollowedBy(MOB thisOne);
	public MOB fetchFollower(int index);
	public MOB fetchFollower(String named);
	public MOB amFollowing();
    public MOB amUltimatelyFollowing();
	public boolean willFollowOrdersOf(MOB mob);
	public void setFollowing(MOB mob);
	public HashSet getGroupMembers(HashSet list);
	public HashSet getRideBuddies(HashSet list);
	public int maxFollowers();
	public int totalFollowers();

	/** Manipulation of ability objects, which includes
	 * spells, traits, skills, etc.*/
	public void addAbility(Ability to);
	public void delAbility(Ability to);
	public int numLearnedAbilities();
	public int numAbilities();
	public Ability fetchAbility(int index);
	public Ability findAbility(String name);
	public Ability fetchAbility(String ID);

	/** Manipulation of the expertise list */
	public void addExpertise(String of);
	public void delExpertise(String of);
	public int numExpertises();
	public Enumeration uniqueExpertises();
	public String fetchExpertise(int x);
	public String fetchExpertise(String of);
    
	/** Manipulation of the tatoo list */
	public void addTattoo(String of);
	public void delTattoo(String of);
	public int numTattoos();
	public String fetchTattoo(int x);
	public String fetchTattoo(String of);

    /** Manipulation of the factions list */
    public void addFaction(String of, int start);
    public void adjustFaction(String of, int amount);
    public Enumeration<String> fetchFactions();
    public Vector fetchFactionRanges();
    public boolean hasFaction(String which);
    public int fetchFaction(String which);
    public String getFactionListing();
    public void removeFaction(String which);
    public void copyFactions(MOB source);
    
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
	// maybe 10 more?

	public static final long SHEATH_TIME=3*Tickable.TIME_TICK;

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
										   false};
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
                                           "AUTORUN"};

}
