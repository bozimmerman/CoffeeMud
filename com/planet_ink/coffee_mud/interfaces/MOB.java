package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

/**
 * A MOB is a creature in the system, from a user
 * down to a goblin
 */
public interface MOB 
	extends Environmental 
{
	
	/** When the USER last logged on */
	public Calendar lastDateTime();
	/** User PASSWORD */
	public String password();
	/** update USER information */
	public void setUserInfo(String newUsername, String newPassword, Calendar newCalendar);
	public void setChannelMask(int newMask);
	public int getChannelMask();
	public int getBitmap();
	public void setBitmap(int bitmap);
	
	public static final int ATT_AUTOGOLD=1;
	public static final int ATT_AUTOLOOT=2;
	public static final int ATT_AUTOEXITS=4;
	public static final int ATT_AUTOASSIST=8;
	public static final int ATT_ANSI=16;
	public static final int ATT_SYSOPMSGS=32;
	
	/** Some general statistics about MOBs.  See the
	 * CharStats class (in interfaces) for more info. */
	public CharStats baseCharStats();
	public CharStats charStats();
	public void recoverCharStats();
	public void setBaseCharStats(CharStats newBaseCharStats);
	public String rawDisplayText();
	public int maxCarry();
	public String healthText();

	/** Whether this MOB is dead.*/
	public boolean amDead();
	public void raiseFromDead();
	public void kill();
	public boolean isInCombat();
	public MOB getVictim();
	public void setVictim(MOB mob);
	public void makePeace();
	public void bringToLife(Room newLocation);
	public void destroy();
	
	/** Where this MOB is currently*/
	public Room location();
	public void setLocation(Room newRoom);
	
	/** If the MOB is controlled by a USER, this
	 * will point to the controlling session object*/
	public Session session();
	public void tell(MOB source, Environmental target, String msg);
	public void tell(String msg);
	public void setSession(Session newSession);
	public void setReplyTo(MOB mob);
	public MOB replyTo();
	
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
	
	// mental characteristics
	public String getWorshipCharID();
	public int getAlignment();
	public int getWimpHitPoint();
	public int getQuestPoint();
	public void setAlignment(int newVal);
	public void setWorshipCharID(String newVal);
	public void setWimpHitPoint(int newVal);
	public void setQuestPoint(int newVal);
	
	// location!
	public Room getStartRoom();
	public void setStartRoom(Room newVal);
	public Calendar lastTickedDateTime();
	
	/** Manipulation of inventory, which includes held,
	 * worn, wielded, and contained items */
	public void addInventory(Item item);
	public void delInventory(Item item);
	public int inventorySize();
	public Item fetchInventory(int index);
	public Item fetchInventory(String itemName);
	public Item fetchCarried(Item goodLocation, String itemName);
	public Item fetchWornItem(String itemName);
	public Item fetchWornItem(long wornCode);
	public Item fetchWieldedItem();
	public boolean amWearingSomethingHere(long wornCode);
	public boolean isMine(Environmental env);
	
	/** Manipulation of followers.*/
	public void addFollower(MOB follower);
	public void delFollower(MOB follower);
	public int numFollowers();
	public MOB fetchFollower(int index);
	public MOB fetchFollower(MOB thisOne);
	public MOB fetchFollower(String ID);
	public MOB amFollowing();
	public void setFollowing(MOB mob);
	
	/** Manipulation of ability objects, which includes
	 * spells, traits, skills, etc.*/
	public void addAbility(Ability to);
	public void delAbility(Ability to);
	public int numAbilities();
	public Ability fetchAbility(int index);
	public Ability fetchAbility(String ID);
}
