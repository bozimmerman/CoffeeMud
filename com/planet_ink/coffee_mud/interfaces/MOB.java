package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

/**
 * A MOB is a creature in the system, from a user
 * down to a goblin
 */
public interface MOB
	extends Environmental, Rider
{
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

	public static final long SHEATH_TIME=3*Host.TICK_TIME;
	
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
										   "AUTONOTIFY"};

	public int getBitmap();
	public void setBitmap(int bitmap);

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
	public int baseWeight();
	public String healthText();

	/** Combat and death */
	public boolean amDead();
	public DeadBody killMeDead(boolean createBody);
	public boolean isInCombat();
	public void bringToLife(Room newLocation, boolean resetStats);
	public void removeFromGame();
	public void destroy();
	public MOB getVictim();
	public void setVictim(MOB mob);
	public void makePeace();
	public void setAtRange(int newRange);
	public int rangeToTarget();
	public boolean mayIFight(MOB mob);
	public boolean mayPhysicallyAttack(MOB mob);
	public int adjustedAttackBonus(MOB mob);
	public int adjustedArmor();
	public int adjustedDamage(Weapon weapon, MOB target);
	public long peaceTime();
	

	/** If the MOB is controlled by a USER, this
	 * will point to the controlling session object*/
	public Session session();
	public void tell(MOB source, Environmental target, Environmental tool, String msg);
	public void tell(String msg);
	public void setSession(Session newSession);

	/** Whether a sessiob object is attached to this MOB */
	public boolean isMonster();
	public boolean isASysOp(Room of);
	public MOB soulMate();
	public void setSoulMate(MOB mob);

	// gained attributes
	public long getAgeHours();
	public int getPractices();
	public int getExperience();
	public int getExpNextLevel();
	public int getExpNeededLevel();
	public int getTrains();
	public int getMoney();
	public void setAgeHours(long newVal);
	public void setExperience(int newVal);
	public void setExpNextLevel(int newVal);
	public void setPractices(int newVal);
	public void setTrains(int newVal);
	public void setMoney(int newVal);

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
	public String getLeigeID();
	public Deity getMyDeity();
	public String getClanID();
	public void setClanID(String clan);
	public int getClanRole();
	public void setClanRole(int role);
	public void setLeigeID(String newVal);
	public int getAlignment();
	public int getWimpHitPoint();
	public int getQuestPoint();
	public void setAlignment(int newVal);
	public void setWorshipCharID(String newVal);
	public void setWimpHitPoint(int newVal);
	public void setQuestPoint(int newVal);
	public long lastTickedDateTime();
	public int movesSinceLastTick();
	public long lastMovedDateTime();

	// location!
	public Room getStartRoom();
	public void setStartRoom(Room newVal);
	public Room location();
	public void setLocation(Room newRoom);

	/** Manipulation of inventory, which includes held,
	 * worn, wielded, and contained items */
	public void addInventory(Item item);
	public void delInventory(Item item);
	public int inventorySize();
	public Item fetchInventory(int index);
	public Item fetchInventory(String itemName);
	public Item fetchInventory(Item goodLocation, String itemName);
	public Item fetchCarried(Item goodLocation, String itemName);
	public Item fetchWornItem(String itemName);
	public Item fetchFirstWornItem(long wornCode);
	public Item fetchWieldedItem();
	
	
	public int freeWearPositions(long wornCode);
	public int numWearingHere(long wornCode);
	public boolean isMine(Environmental env);
	public void confirmWearability();
	public void giveItem(Item thisContainer);
	public int getWearPositions(long wornCode);

	/** Manipulation of followers */
	public void addFollower(MOB follower);
	public void delFollower(MOB follower);
	public int numFollowers();
	public MOB fetchFollower(int index);
	public MOB fetchFollower(MOB thisOne);
	public MOB fetchFollower(String ID);
	public MOB amFollowing();
	public boolean willFollowOrdersOf(MOB mob);
	public void setFollowing(MOB mob);
	public Hashtable getGroupMembers(Hashtable list);
	public Hashtable getRideBuddies(Hashtable list);
	public boolean isEligibleMonster();
	public int maxFollowers();
	public int totalFollowers();

	/** Manipulation of ability objects, which includes
	 * spells, traits, skills, etc.*/
	public boolean hasAbilityEvoker(String word);
	public void addAbility(Ability to);
	public void delAbility(Ability to);
	public int numLearnedAbilities();
	public int numAbilities();
	public Ability fetchAbility(int index);
	public Ability fetchAbility(String ID);
}
