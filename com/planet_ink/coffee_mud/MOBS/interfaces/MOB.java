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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/*
   Copyright 2001-2018 Bo Zimmerman

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
/**
 *  A MOB is a creature in the system, from a user down to a goblin.
 *
 * @author Bo Zimmerman
 *
 */
public interface MOB extends Rider, DBIdentifiable, PhysicalAgent, ItemPossessor, AbilityContainer, 
							 Tattooable, FactionMember, MUDCmdProcessor, Followable<MOB>, Combatant
{
	public static long AGE_MILLIS_THRESHOLD = 120000;

	/**
	 * Returns the raw numeric attributes bitmap
	 * @see MOB.Attrib
	 * @see MOB#setAttributesBitmap(int)
	 * @return the raw numeric attributes bitmap
	 */
	public int getAttributesBitmap();
	
	/**
	 * Sets the raw numeric attributes bitmap
	 * @see MOB.Attrib
	 * @see MOB#getAttributesBitmap()
	 * @param bitmap the raw numeric attributes bitmap
	 */
	public void setAttributesBitmap(int bitmap);
	
	/**
	 * Changes the value of a specific attribute
	 * @see MOB#isAttributeSet(Attrib)
	 * @param attrib the attribute to set or clear
	 * @param set true to set it, false to clear it
	 */
	public void setAttribute(MOB.Attrib attrib, boolean set);
	
	/**
	 * Returns whether the given attribute is set.
	 * @see MOB#setAttribute(Attrib, boolean)
	 * @param attrib the attribute to check
	 * @return true if it is set, false otherwise
	 */
	public boolean isAttributeSet(MOB.Attrib attrib);
	
	/**
	 * If this player is using a title, this method returns
	 * the players Name() with the title.  If not using a
	 * title, this method returns name().
	 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#Name()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Tickable#name()
	 * @return the name with a title, or not
	 */
	public String titledName();
	
	/**
	 * Returns the age-range and race of this mob, as if it were someone
	 * spotted on the street that you didn't know.
	 * @return the age-range and race of this mob
	 */
	public String genericName();

	/* Some general statistics about MOBs.  See the CharStats class (in interfaces) for more info. */
	
	/**
	 * Returns whether this mob represents a player.  It basically checks for a PlayerStats object.
	 * @see MOB#isMonster()
	 * @see MOB#playerStats()
	 * @return whether this mob represents a player
	 */
	public boolean isPlayer();
	
	/**
	 * Returns the PlayerStats object for this mob.  A null response indicated definitively
	 * that this is a Player and not an NPC, even if there is no session attached.
	 * @see MOB#setPlayerStats(PlayerStats)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats
	 * @return the PlayerStats object or NULL for an NPC
	 */
	public PlayerStats playerStats();
	
	/**
	 * Sets the PlayerStats object for this mob.  A null value indicated definitively
	 * that this is a Player and not an NPC, even if there is no session attached.
	 * @see MOB#playerStats()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerStats
	 * @param newStats the PlayerStats object or null for an NPC
	 */
	public void setPlayerStats(PlayerStats newStats);
	
	/**
	 * Gets the Base CharStats object for this mob, which are the stats like saves and strength.
	 * The Base CharStats are those stats before modification by equipment or spell effects.
	 * @see MOB#charStats()
	 * @see MOB#recoverCharStats()
	 * @see MOB#setBaseCharStats(CharStats)
	 * @return the CharStats object for this mob
	 */
	public CharStats baseCharStats();
	
	/**
	 * Gets the Current CharStats object for this mob, which are the stats like saves and strength.
	 * The Current CharStats are the Base stats after modification by equipment or spell effects.
	 * @see MOB#baseCharStats()
	 * @see MOB#recoverCharStats()
	 * @see MOB#setBaseCharStats(CharStats)
	 * @return the CharStats object for this mob
	 */
	public CharStats charStats();
	
	/**
	 * Causes this mob to recalculate its current char stats by copying the base stats
	 * over and then calling all equipment and spell effects to modify them.
	 * @see MOB#baseCharStats()
	 * @see MOB#charStats()
	 * @see MOB#setBaseCharStats(CharStats)
	 */
	public void recoverCharStats();
	
	/**
	 * Sets the Base CharStats object for this mob, which are the stats like saves and strength.
	 * The Base CharStats are those stats before modification by equipment or spell effects.
	 * @see MOB#charStats()
	 * @see MOB#baseCharStats()
	 * @see MOB#setBaseCharStats(CharStats)
	 * @param newBaseCharStats the CharStats object for this mob
	 */
	public void setBaseCharStats(CharStats newBaseCharStats);
	
	/**
	 * Returns the maximum total weight in pounds that this mob can carry.
	 * @see MOB#maxItems()
	 * @return the maximum total weight in pounds that this mob can carry.
	 */
	public int maxCarry();
	
	/**
	 * Returns the maximum total number of items that this mob can carry.
	 * @see MOB#maxCarry()
	 * @return the maximum total number of items that this mob can carry.
	 */
	public int maxItems();
	
	/**
	 * Returns the base weight of this mob, which includes any char stat
	 * adjustments, and adjustments from race.
	 * @return the base weight of this mob
	 */
	public int baseWeight();
	
	/**
	 * Returns the friendly viewable description of this mobs health status,
	 * from the given viewer mobs point of view.
	 * @param viewer the mob viewing this mob
	 * @return the friendly viewable health status string
	 */
	public String healthText(MOB viewer);

	/* Combat and death */
	/**
	 * Returns whether this mob is dead and presumably waiting for rejuv.
	 * @see MOB#killMeDead(boolean)
	 * @see MOB#bringToLife(Room, boolean)
	 * @see MOB#removeFromGame(boolean, boolean)
	 * @return true if this mob is dead, false otherwise
	 */
	public boolean amDead();
	
	/**
	 * Puts this mob in a dead state, removes all temporary effects,
	 * creates a corpse, ends combat, and sends players to their graveyard.
	 * @see MOB#amDead()
	 * @see MOB#bringToLife(Room, boolean)
	 * @see MOB#removeFromGame(boolean, boolean)
	 * @param createBody true to create a corpse, false otherwise
	 * @return the corpse, if one was created
	 */
	public DeadBody killMeDead(boolean createBody);

	/**
	 * Brings this mob to life, or back to life, and puts the mob
	 * into the given room, or their start room if none given.
	 * This also calls bringToLife to start ticking.
	 * @see MOB#bringToLife()
	 * @see MOB#killMeDead(boolean)
	 * @see MOB#removeFromGame(boolean, boolean)
	 * @param newLocation the room to bring the mob to life in
	 * @param resetStats true to bring all char state stats to max
	 */
	public void bringToLife(Room newLocation, boolean resetStats);
	
	/**
	 * Flags this mob as being alive, and restarts the mob tick.
	 * @see MOB#bringToLife(Room, boolean)
	 * @see MOB#killMeDead(boolean)
	 * @see MOB#removeFromGame(boolean, boolean)
	 */
	public void bringToLife();
	
	/**
	 * Removes this mob from the game.  Principally used for player mobs,
	 * this method optionally copies the follower mobs, stops the session
	 * if any, and removes the mob from the room they are in.
	 * @see MOB#bringToLife(Room, boolean)
	 * @see MOB#killMeDead(boolean)
	 * @see MOB#amActive()
	 * @param preserveFollowers true to copy the followers over
	 * @param killSession true to end the session connected to this mob, if any
	 */
	public void removeFromGame(boolean preserveFollowers, boolean killSession);
	
	/**
	 * Returns whether this mob has been removed from the game.  It only
	 * checks the flag set by removeFromGame.
	 * @see MOB#bringToLife(Room, boolean)
	 * @see MOB#killMeDead(boolean)
	 * @see MOB#removeFromGame(boolean, boolean)
	 * @return true if the removeFromGame flag is set, false otherwise
	 */
	public boolean amActive();
	
	/**
	 * If this mob is in combat, this returns the mob that this mob is
	 * targeting. If this method returns null, the mob is not in combat.
	 * @see Combatant#isInCombat()
	 * @see MOB#setVictim(MOB)
	 * @see Combatant#makePeace(boolean)
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @return the combat target, or null for a peace state
	 */
	public MOB getVictim();
	
	/**
	 * Sets the mob that this mob is targeting for combat, which
	 * either puts them into, or clears their combat state. 
	 * If a null value, the mob is no longer fighting.
	 * @see Combatant#isInCombat()
	 * @see MOB#getVictim()
	 * @see Combatant#makePeace(boolean)
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @param other the combat target, or null for a peace state
	 */
	public void setVictim(MOB other);

	/* Primary mob communication */
	
	/**
	 * Basic communication to a session attached to this mob, if any.
	 * This version allows the basic naming tags to be used by providing
	 * the objects to supply the names.
	 * @see MOB#tell(String)
	 * @param source the &lt;S-NAME&gt; source/agent of the message or null
	 * @param target the &lt;T-NAME&gt; target of the message or null
	 * @param tool the &lt;O-NAME&gt; tool of the message or null
	 * @param msg the string message with naming tags
	 */
	public void tell(MOB source, Environmental target, Environmental tool, String msg);
	
	/**
	 * Basic communication to a session attached to this mob, if any.
	 * @see MOB#tell(MOB, Environmental, Environmental, String)
	 * @param msg the string message to send to the session.
	 */
	public void tell(String msg);
	
	/* Session related stuff */
	
	/**
	 * Returns any Telnet Session object attached to this mob.  Without one, this
	 * mob has nowhere to send messages, or receive input from a keyboard
	 * @see MOB#setSession(Session)
	 * @see MOB#isMonster()
	 * @see MOB#isPossessing()
	 * @see MOB#soulMate()
	 * @return the Telnet Session attached to this mob
	 */
	public Session session();
	
	/**
	 * Sets any Telnet Session object attached to this mob.  Without one, this
	 * mob has nowhere to send messages, or receive input from a keyboard
	 * @see MOB#session()
	 * @see MOB#isMonster()
	 * @see MOB#isPossessing()
	 * @see MOB#soulMate()
	 * @param newSession the Telnet Session attached to this mob
	 */
	public void setSession(Session newSession);
	
	/**
	 * Returns whether this mob has a real telnet session attached to it.
	 * @see MOB#session()
	 * @see MOB#setSession(Session)
	 * @see MOB#isPossessing()
	 * @see MOB#soulMate()
	 * @return true if there is a session attached, false otherwise
	 */
	public boolean isMonster();

	/**
	 * Returns whether this mob base session is possessing some other
	 * mob.  It says more about the session than the mob.
	 * @see MOB#session()
	 * @see MOB#setSession(Session)
	 * @see MOB#soulMate()
	 * @see MOB#dispossess(boolean)
	 * @return true if this mobs session is possessing another mob
	 */
	public boolean isPossessing();
	
	/**
	 * Returns the player mob that is possessing this mob by loaning
	 * it its session.
	 * @see MOB#session()
	 * @see MOB#isPossessing()
	 * @see MOB#setSoulMate(MOB)
	 * @see MOB#dispossess(boolean)
	 * @return the mob that is possessing this mob
	 */
	public MOB soulMate();
	
	/**
	 * Sets the player mob that is possessing this mob by loaning
	 * it its session.
	 * @see MOB#session()
	 * @see MOB#isPossessing()
	 * @see MOB#soulMate()
	 * @see MOB#dispossess(boolean)
	 * @param mob the mob that is possessing this mob
	 */
	public void setSoulMate(MOB mob);
	
	/**
	 * If this mob is being possessed by a player, this method can be
	 * called to return this mobs borrowed session to the player,
	 * thus ending the possession.
	 * @see MOB#session()
	 * @see MOB#isPossessing()
	 * @see MOB#soulMate()
	 * @see MOB#setSoulMate(MOB)
	 * @param forceLook true to force the player to Look afterwards
	 */
	public void dispossess(boolean forceLook);

	/**
	 * Returns the total number of experience points earned by this mob.
	 * @see MOB#setExperience(int)
	 * @see MOB#getExpNextLevel()
	 * @see MOB#getExpNeededDelevel()
	 * @see MOB#getExpNeededLevel()
	 * @see MOB#getExpPrevLevel()
	 * @return the total number of experience points earned by this mob.
	 */
	public int getExperience();
	
	/**
	 * Sets the total number of experience points earned by this mob.
	 * @see MOB#getExperience()
	 * @see MOB#getExpNextLevel()
	 * @see MOB#getExpNeededDelevel()
	 * @see MOB#getExpNeededLevel()
	 * @see MOB#getExpPrevLevel()
	 * @param newVal the total number of experience points earned by this mob.
	 */
	public void setExperience(int newVal);
	
	/**
	 * Returns the number of ms (in tick increments)
	 * that this mob has NOT been in combat. 
	 * @return the ms of peace enjoyed by this mob.
	 */
	public long getPeaceTime();
	
	/**
	 * Gets the total number of experience points this mob needs to earn
	 * their next level.
	 * @see MOB#getExperience()
	 * @see MOB#setExperience(int)
	 * @see MOB#getExpNeededDelevel()
	 * @see MOB#getExpNeededLevel()
	 * @see MOB#getExpPrevLevel()
	 * @return total xp to next level
	 */
	public int getExpNextLevel();

	/**
	 * Returns the number of additional experience points needed for
	 * this mob to gain their next level.  It returns Integer.MAX_VALUE
	 * if this does not apply.
	 * @see MOB#getExperience()
	 * @see MOB#getExpNextLevel()
	 * @see MOB#getExpNeededDelevel()
	 * @see MOB#setExperience(int)
	 * @see MOB#getExpPrevLevel()
	 * @return how much more xp needed
	 */
	public int getExpNeededLevel();
	
	/**
	 * Returns the number of experience points gained since the player
	 * got their current level, thus also telling you how much xp can
	 * be lost before de-leveling.  It returns 0 at first level, since
	 * a first level character cannot de-level.
	 * @see MOB#getExperience()
	 * @see MOB#getExpNextLevel()
	 * @see MOB#setExperience(int)
	 * @see MOB#getExpNeededLevel()
	 * @see MOB#getExpPrevLevel()
	 * @return how much more xp gained
	 */
	public int getExpNeededDelevel();
	
	/**
	 * Gets the total number of experience points the mob acquired to
	 * reach their current level, making it the baseline for this
	 * levels experience. 
	 * @return the experience point level baseline
	 */
	public int getExpPrevLevel();
	
	/* gained attributes */
	
	/**
	 * Returns the total number of rl minutes this player has ever played.
	 * @see MOB#setAgeMinutes(long)
	 * @return the total number of rl minutes this player has ever played.
	 */
	public long getAgeMinutes();
	
	/**
	 * Returns the total number of rl minutes this player has ever played.
	 * @see MOB#getAgeMinutes()
	 * @param newVal the total number of rl minutes this player has ever played.
	 */
	public void setAgeMinutes(long newVal);
	
	/**
	 * Returns the number of practice points this mob has
	 * @see MOB#setPractices(int)
	 * @return the number of practice points this mob has
	 */
	public int getPractices();
	
	/**
	 * Sets the number of practice points this mob has
	 * @see MOB#getPractices()
	 * @param newVal the number of practice points this mob has
	 */
	public void setPractices(int newVal);
	
	/**
	 * Returns the number of training points this mob has
	 * @see MOB#setTrains(int)
	 * @return the number of training points this mob has
	 */
	public int getTrains();
	
	/**
	 * Sets the number of training points this mob has
	 * @see MOB#getTrains()
	 * @param newVal the number of training points this mob has
	 */
	public void setTrains(int newVal);
	
	/**
	 * Only somewhat deprecated, this method returns the internal
	 * money counter.  Technically money is supposed to be stored
	 * as Coin items, but these methods are still used as shortcuts
	 * to give NPCs their initial money.  This number is also a 
	 * baseline value that can be modified with the money variation
	 * methods.
	 * @see MOB#setMoney(int)
	 * @see MOB#getMoneyVariation()
	 * @see MOB#setMoneyVariation(double)
	 * @return the mob npc money
	 */
	public int getMoney();
	
	/**
	 * Only somewhat deprecated, this method sets the internal
	 * money counter.  Technically money is supposed to be stored
	 * as Coin items, but these methods are still used as shortcuts
	 * to give NPCs their initial money.  This number is also a 
	 * baseline value that can be modified with the money variation
	 * methods.
	 * @see MOB#getMoney()
	 * @see MOB#getMoneyVariation()
	 * @see MOB#setMoneyVariation(double)
	 * @param newVal the mob npc money
	 */
	public void setMoney(int newVal);
	
	/**
	 * Returns a positive or negative range from 0-&gt;this number
	 * that represents the amount of money added or removed from
	 * this mob when the mob-as-NPC does and is ready to be looted. 
	 * @see MOB#getMoney()
	 * @see MOB#setMoney(int)
	 * @see MOB#setMoneyVariation(double)
	 * @return the amount of money to vary for looting money
	 */
	public double getMoneyVariation();
	
	/**
	 * Sets a positive or negative range from 0-&gt;this number
	 * that represents the amount of money added or removed from
	 * this mob when the mob-as-NPC does and is ready to be looted. 
	 * @see MOB#getMoney()
	 * @see MOB#getMoneyVariation()
	 * @see MOB#setMoneyVariation(double)
	 * @param newVal the amount of money to vary for looting money
	 */
	public void setMoneyVariation(double newVal);

	/* the core state values */
	
	/**
	 * Gets the Base CharState object for this mob, which are the stats like health and mana.
	 * The Base CharState are those stats before modification by equipment or spell effects.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharState
	 * @see MOB#setBaseState(CharState)
	 * @see MOB#curState()
	 * @see MOB#maxState()
	 * @see MOB#recoverMaxState()
	 * @see MOB#resetToMaxState()
	 * @return the base CharState object for this mob
	 */
	public CharState baseState();
	
	/**
	 * Sets the Base CharState object for this mob, which are the stats like health and mana.
	 * The Base CharState are those stats before modification by equipment or spell effects.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharState
	 * @see MOB#baseState()
	 * @see MOB#curState()
	 * @see MOB#maxState()
	 * @see MOB#recoverMaxState()
	 * @see MOB#resetToMaxState()
	 * @param newState the base CharState object for this mob
	 */
	public void setBaseState(CharState newState);
	
	/**
	 * Gets the Current CharState object for this mob, which are the temp stats like health and mana.
	 * The Current CharState are the max state after modification by casting, damage, and running around.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharState
	 * @see MOB#setBaseState(CharState)
	 * @see MOB#baseState()
	 * @see MOB#maxState()
	 * @see MOB#recoverMaxState()
	 * @see MOB#resetToMaxState()
	 * @return the Current charState object for this mob
	 */
	public CharState curState();
	
	/**
	 * Gets the Max CharState object for this mob, which are the stats like health and mana.
	 * The Max CharState are those stats after modification by equipment or spell effects, but
	 * before taking damage or using by resources with movement and casting
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharState
	 * @see MOB#setBaseState(CharState)
	 * @see MOB#curState()
	 * @see MOB#baseState()
	 * @see MOB#recoverMaxState()
	 * @see MOB#resetToMaxState()
	 * @return the base CharState object for this mob
	 */
	public CharState maxState();
	
	/**
	 * Causes this mob to recalculate its max char state by copying the base state
	 * over and then calling all equipment and spell effects to modify them.
	 * @see MOB#setBaseState(CharState)
	 * @see MOB#curState()
	 * @see MOB#baseState()
	 * @see MOB#maxState()
	 * @see MOB#resetToMaxState()
	 */
	public void recoverMaxState();
	
	/**
	 * Causes this mob to copy the max state object to the current state object,
	 * effectively healing and rejuvinating the mob.
	 * @see MOB#setBaseState(CharState)
	 * @see MOB#curState()
	 * @see MOB#baseState()
	 * @see MOB#maxState()
	 * @see MOB#recoverMaxState()
	 */
	public void resetToMaxState();
	
	/**
	 * Returns the Weapon object that this mob attacks with when attacking
	 * otherwise unarmed.  This is like claws and teeth for animals and
	 * so forth.  It is typically determined by the mobs race.
	 * @return the unarmed weapon
	 */
	public Weapon getNaturalWeapon();

	/* misc characteristics */
	
	/**
	 * Returns the liege to which this mob owes loyalty.  Because this
	 * field doubles for spouse, a player can have a liege or a spouse,
	 * but not both. Empty string means no liege or spouse.
	 * @see MOB#setLiegeID(String)
	 * @see MOB#isMarriedToLiege()
	 * @return the name of the player to which this mob owes loyalty
	 */
	public String getLiegeID();
	
	/**
	 * Sets the liege to which this mob owes loyalty.  Because this
	 * field doubles for spouse, a player can have a liege or a spouse,
	 * but not both. Empty string means no liege or spouse.
	 * @see MOB#getLiegeID()
	 * @see MOB#isMarriedToLiege()
	 * @param newVal the name of the player to which this mob owes loyalty
	 */
	public void setLiegeID(String newVal);
	
	/**
	 * Returns whether this mob/player is married to their liege, or whether
	 * they are a simple liege.
	 * @see MOB#getLiegeID()
	 * @see MOB#setLiegeID(String)
	 * @return true if they are married to liege, false if just a leige
	 */
	public boolean isMarriedToLiege();

	/**
	 * Returns the name of the Deity mob that this player/mob worships.
	 * Empty string means they are an atheist. :) The name here should
	 * always be the same as a Deity type mob in the game in order for
	 * the religion system to work correctly.  For Clerics, this field
	 * has particular importance.
	 * @see MOB#setWorshipCharID(String)
	 * @see MOB#getMyDeity()
	 * @return the name of the Deity mob that this player/mob worships.
	 */
	public String getWorshipCharID();
	
	/**
	 * Sets the name of the Deity mob that this player/mob worships.
	 * Empty string means they are an atheist. :) The name here should
	 * always be the same as a Deity type mob in the game in order for
	 * the religion system to work correctly.  For Clerics, this field
	 * has particular importance.
	 * @see MOB#setWorshipCharID(String)
	 * @see MOB#getMyDeity()
	 * @param newVal the name of the Deity mob that this player/mob worships.
	 */
	public void setWorshipCharID(String newVal);
	
	/**
	 * Returns the Deity object of the mob that this player/mob worships.
	 * A null return means they are an atheist.  Very important for Clerics. 
	 * @see MOB#getWorshipCharID()
	 * @see MOB#setWorshipCharID(String)
	 * @return the Deity object of the mob that this player/mob worships
	 */
	public Deity getMyDeity();

	/**
	 * Returns the number of hit points below which this mob will
	 * automatically flee combat.
	 * @see MOB#setWimpHitPoint(int)
	 * @return the wimpy hit point number
	 */
	public int getWimpHitPoint();
	
	/**
	 * Sets the number of hit points below which this mob will
	 * automatically flee combat.
	 * @see MOB#getWimpHitPoint()
	 * @param newVal the wimpy hit point number
	 */
	public void setWimpHitPoint(int newVal);

	/**
	 * Returns the number of quest points that this mob has earned.
	 * @see MOB#setQuestPoint(int)
	 * @return the number of quest points that this mob has earned.
	 */
	public int getQuestPoint();
	
	/**
	 * Sets the number of quest points that this mob has earned.
	 * @see MOB#getQuestPoint()
	 * @param newVal the number of quest points that this mob has earned.
	 */
	public void setQuestPoint(int newVal);

	/**
	 * Returns the precise time, in milliseconds, that this mob last
	 * "Ticked".
	 * @return the precise time, in milliseconds
	 */
	public long lastTickedDateTime();

	/**
	 * Returns an iterable set of the Clans that this mob/player belongs
	 * to, along with the Rank code in that clan that this mob has in it.
	 * @see MOB#setClan(String, int)
	 * @see MOB#getClanRole(String)
	 * @return an iterable set of the Clans that this mob/player belongs
	 */
	public Iterable<Pair<Clan,Integer>> clans();
	
	/**
	 * Given a precise clanID (name), this method returns the Clan object and
	 * this players rank in the clan, if they belong.  Otherwise it
	 * returns null
	 * @see MOB#clans()
	 * @see MOB#setClan(String, int)
	 * @param clanID the clan name/id
	 * @return the Clan object + Rank, or null
	 */
	public Pair<Clan,Integer> getClanRole(String clanID);
	
	/**
	 * Adds or alters the rank of this player/mob in the given clan.
	 * @see MOB#clans()
	 * @see MOB#getClanRole(String)
	 * @param clanID the clanID/name
	 * @param role the rank/role of this player in the clan
	 */
	public void setClan(String clanID, int role);

	/* location! */
	
	/**
	 * Gets the stored Start Room for this mob.  This is where the mob/player
	 * goes when they recall.  Can also return null, of course, which for a mob
	 * typically means they are temporary.
	 * @see MOB#setStartRoom(Room)
	 * @return  the stored Start Room for this mob
	 */
	public Room getStartRoom();
	
	/**
	 * Sets the stored Start Room for this mob.  This is where the mob/player
	 * goes when they recall.  Can also set to null, of course, which for a mob
	 * typically means they are temporary.
	 * @see MOB#getStartRoom()
	 * @param newRoom  the stored Start Room for this mob
	 */
	public void setStartRoom(Room newRoom);
	
	/**
	 * Returns the room in which this mob/player is currently standing. It can
	 * also refer to the room in which this mob/player WOULD be standing if they
	 * were still in the game.
	 * @see MOB#setLocation(Room)
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room#isInhabitant(MOB)
	 * @return the room in which this mob/player is currently standing
	 */
	public Room location();
	
	/**
	 * Sets the room in which this mob/player is currently standing. It can
	 * also refer to the room in which this mob/player WOULD be standing if they
	 * were still in the game.
	 * @see MOB#location()
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room#isInhabitant(MOB)
	 * @param newRoom the room in which this mob/player is currently standing
	 */
	public void setLocation(Room newRoom);

	/* Manipulation of inventory, which includes held, worn, wielded, and contained items */
	/**
	 * Flags this mob as having their base inventory / shop inventory reconstructed from
	 * the database.  This allows the mob to eventually handle any variable or optional
	 * equipment changes, or for shopkeepers to fill out tech/electronics variables.
	 */
	public void flagVariableEq();
	
	/**
	 * Returns a best match for the given itemName in this mob/players base inventory.
	 * The filter must be non-null, but can be Wearable.FILTER_ANY or one of the other
	 * Wearable filters.  Also allows specific container checks.  This method also
	 * respects context numbers, such as .1, .2 for grabbing a specific duplicate
	 * item.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Wearable#FILTER_WORNONLY
	 * @see MOB#fetchWornItems(long, short, short)
	 * @see MOB#fetchWieldedItem()
	 * @param goodLocation the container to look in, or null for uncontained
	 * @param filter the wearable filter to use
	 * @param itemName the name, id, or display text, or keyboard for the item to find.
	 * @return a best match for the given itemName
	 */
	public Item fetchItem(Item goodLocation, Filterer<Environmental> filter, String itemName);
	
	/**
	 * Returns the collection of items worn by this mob/player at the given specific worn
	 * code, at or above the given Layer code, and having the given layerAttributes.
	 * @see Wearable#WORN_HEAD
	 * @see Armor#LAYERMASK_MULTIWEAR
	 * @see MOB#fetchItem(Item, Filterer, String)
	 * @see MOB#fetchWieldedItem()
	 * @see MOB#fetchFirstWornItem(long)
	 * @param wornCode the specific worn code to loook for worn items
	 * @param aboveOrAroundLayer -2048 will grab everything, and &gt; 0 are higher layers
	 * @param layerAttributes 0 will grab everything, or one of the layer attributes
	 * @return the list of items worn at the given worn code
	 */
	public List<Item> fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes);
	
	/**
	 * Returns the first item encountered on this player/mob at the given worn code, or
	 * null if nothing found.
	 * @see Wearable#WORN_HEAD
	 * @see MOB#fetchItem(Item, Filterer, String)
	 * @see MOB#fetchFirstWornItem(long)
	 * @see MOB#fetchWieldedItem()
	 * @param wornCode the worn_ code
	 * @return the first item encountered at that worn code.
	 */
	public Item fetchFirstWornItem(long wornCode);
	
	/**
	 * Returns the item being wielded in the WIELD position by this player/mob.  Its a really
	 * quick way to get the mobs main weapon.
	 * @see MOB#fetchFirstWornItem(long)
	 * @see MOB#fetchWornItems(long, short, short)
	 * @see MOB#fetchHeldItem()
	 * @return the item being wielded in the WIELD position by this player/mob
	 */
	public Item fetchWieldedItem();
	
	/**
	 * Returns the item being wielded in the HELD position by this player/mob.  Its a really
	 * quick way to get the mobs secondary weapon or shield.
	 * @see MOB#fetchFirstWornItem(long)
	 * @see MOB#fetchWornItems(long, short, short)
	 * @see MOB#fetchWieldedItem()
	 * @return the item being wielded in the HELD position by this player/mob
	 */
	public Item fetchHeldItem();
	
	/**
	 * Returns whether this mob is only carrying money, meaning their main inventory
	 * is essentially empty.  Also returns true if they are broke.
	 * @return true if only money is being carried, or nothing.
	 */
	public boolean hasOnlyGoldInInventory();

	/**
	 * Returns the number of free spaces the player/mob has at the given worn location,
	 * below the given layer, without the given attributes.
	 * @see Wearable#WORN_HEAD
	 * @see Armor#LAYERMASK_MULTIWEAR
	 * @see MOB#getWearPositions(long)
	 * @param wornCode the worn location to look for a free spot in
	 * @param belowLayer the layer at or below which you need a spot -- 0 works
	 * @param layerAttributes the layer attributes to check, again, 0 works
	 * @return the number of free spaces the player/mob has at the given worn location
	 */
	public int freeWearPositions(long wornCode, short belowLayer, short layerAttributes);
	
	/**
	 * Returns the total number of worn locations this mob/player has at the given
	 * worn code location.
	 * @see Wearable#WORN_HEAD
	 * @see MOB#freeWearPositions(long, short, short)
	 * @param wornCode the worn location to look in
	 * @return the total number of worn locations this mob/player has
	 */
	public int getWearPositions(long wornCode);

	/**
	 * Returns whether the given environmental is possessed by this mob.
	 * Whether the object is an exact Ability or effect Ability, a 
	 * Follower MOB, or an inventory Item.
	 * @param env the mob, item, or Ability to look for
	 * @return true if this is presently mine
	 */
	public boolean isMine(Environmental env);
	
	/**
	 * Returns the total number of effects this mob/player is under, including
	 * Racial and Clan effects.  This is as opposed to the normal numEffects,
	 * which only returns the effects properly owned by this mob.
	 * @see MOB#addPriorityEffect(Ability)
	 * @see MOB#personalEffects()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Physical#numEffects()
	 * @return the total number of effects this mob/player is under
	 */
	public int numAllEffects();
	
	/**
	 * Adds the given Ability as a new effect, also putting it on the top of
	 * the list to ensure that it is processed first for messaging and 
	 * stat effect purposes.
	 * @see MOB#numAllEffects()
	 * @see MOB#personalEffects()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.core.interfaces.Physical#addEffect(Ability)
	 * @param to the Ability to add as an effect
	 */
	public void addPriorityEffect(Ability to);
	
	/**
	 * Returns an enumeration only of the effects that are personally owned
	 * by this mob, which means it will skip any Clan or Racial effects.
	 * @see MOB#numAllEffects()
	 * @see MOB#addPriorityEffect(Ability)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.core.interfaces.Physical#effects()
	 * @return the enumeration of personal effect objects
	 */
	public Enumeration<Ability> personalEffects();

	/* Manipulation of followers */
	
	/**
	 * This method recursively returns whoever this mob is riding, and
	 * if they are a rideable, who all is riding with him.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Rider
	 * @see com.planet_ink.coffee_mud.core.interfaces.Rideable
	 * @param list the required list to populate with riders and rideable
	 * @return the same list sent in
	 */
	public Set<MOB> getRideBuddies(Set<MOB> list);
	
	/**
	 * Returns whether the given mob has the authority to give Orders
	 * to this mob, whether from security status, or clan rank.
	 * @param mob the mob to check to see if he's the leader
	 * @return true if you'll follow the mobs orders, false otherwise
	 */
	public boolean willFollowOrdersOf(MOB mob);
	
	/**
	 * Returns the maximum number of followers that this Followable can
	 * have.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Followable
	 * @return the maximum number of followers
	 */
	public int maxFollowers();
	
	/* Extra functions on ability objects, which includes spells, traits, skills, etc.*/
	
	/**
	 * Returns the best match ability/skill/spell of this mob to the given search
	 * name string.  This also searches racial and clan abilities.
	 * @param name the search string for the ability id, name, or whatever
	 * @return the best match ability/skill to the search string
	 */
	public Ability findAbility(String name);
	
	/**
	 * Because of certain variables, mobs are required to cache the calculation of the 
	 * costs of using their Abilities.  The array's first dimension is the type of
	 * cost, indexed by the CACHEINDEX constants.  The second dimension is the 
	 * resource type, which is indexed by the USAGEINDEX constants.  Therefore
	 * by default, the array is CACHEINDEX_TOTAL x 3.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#CACHEINDEX_TOTAL
	 * @param abilityID the specific Ability ID() to get the cache for
	 * @return the ability usage cache array
	 */
	public int[][] getAbilityUsageCache(final String abilityID);

	/**
	 * Returns whether the given ability is actually a racial
	 * ability instead of a learned, clan, or some other kind
	 * of ability.
	 * @param abilityID the ability object to check for
	 * @return true if that object is racial, false otherwise
	 */
	public boolean isRacialAbility(String abilityID);

	/**
	 * Adds a new expertise, or updates an existing one.
	 * Requires a coded expertise name (string followed by
	 * roman or decimal number)
	 * @see MOB#fetchExpertise(String)
	 * @see MOB#expertises()
	 * @see MOB#delAllExpertises()
	 * @see MOB#delExpertise(String)
	 * @param code the expertise to add or update
	 */
	public void addExpertise(String code);

	/**
	 * Deletes an expertise.
	 *
	 * @see MOB#fetchExpertise(String)
	 * @see MOB#expertises()
	 * @see MOB#delAllExpertises()
	 * @see MOB#addExpertise(String)
	 * @param baseCode the expertise code
	 */
	public void delExpertise(String baseCode);

	/**
	 * Returns the expertise and number for the given code. The
	 * code is a full expertise code, including number.  This
	 * will only return a value if the mob has one at or below
	 * the given level, returning null if the mob does not have
	 * a sufficient expertise as the one given.
	 * @see MOB#delAllExpertises()
	 * @see MOB#expertises()
	 * @see MOB#delExpertise(String)
	 * @see MOB#addExpertise(String)
	 * @param code the expertise code
	 * @return the entry with the string and number
	 */
	public Pair<String, Integer> fetchExpertise(String code);

	/**
	 * Deletes all expertises from the collection
	 * @see MOB#fetchExpertise(String)
	 * @see MOB#expertises()
	 * @see MOB#delExpertise(String)
	 * @see MOB#addExpertise(String)
	 */
	public void delAllExpertises();

	/**
	 * Returns an enumerator of all the expertise names
	 * with their numbers if any .
	 * @see MOB#delAllExpertises()
	 * @see MOB#fetchExpertise(String)
	 * @see MOB#delExpertise(String)
	 * @see MOB#addExpertise(String)
	 * @return an enumerator
	 */
	public Enumeration<String> expertises();

	/**
	 * Enum for the MOB Attributes, which will also return
	 * whether the attribute is intuitively reversed (so
	 * Setting turns it off instead of on) and a longer
	 * description string.  It also pre-calculates the
	 * bitmap value for storage.
	 * @see MOB#setAttribute(Attrib, boolean)
	 * @see MOB#getAttributesBitmap()
	 * @author Bo Zimmerman
	 *
	 */
	public static enum Attrib
	{
		AUTOGOLD(false), //1
		AUTOLOOT(false), //2
		AUTOEXITS(false), //3
		AUTOASSIST(true), //4
		ANSI(false,"ANSI COLOR"), //5
		SYSOPMSGS(false,"SYSMSGS"), //6
		AUTOMELEE(true), //7
		PLAYERKILL(false), //8
		BRIEF(false),//9
		NOFOLLOW(false),//10
		AUTOWEATHER(false),//11
		AUTODRAW(false),//12
		AUTOGUARD(false),//13
		SOUND(false,"SOUNDS"),//14
		AUTOIMPROVE(false,"AUTOIMPROVEMENT"),//15
		NOTEACH(false),//16
		AUTONOTIFY(false),//17
		AUTOFORWARD(true),//18
		DAILYMESSAGE(true,"MOTD"),//19
		QUIET(false),//20
		MXP(false),//21
		COMPRESS(false,"COMPRESSED"),//22
		AUTORUN(false),//23
		AUTOMAP(true),//24
		NOBATTLESPAM(false),//25
		TELNET_GA(false,"TELNET-GA"), // 26
		// .. up to /31
		;
		private final int bitCode;
		private final boolean autoReverse;
		private final String desc;
		
		private Attrib(boolean reversed, String desc)
		{
			this.autoReverse=reversed;
			this.desc=desc;
			this.bitCode=(int)Math.round(Math.pow(2,this.ordinal()));
		}

		private Attrib(boolean reversed)
		{
			this.autoReverse=reversed;
			this.desc=this.name();
			this.bitCode=(int)Math.round(Math.pow(2,this.ordinal()));
		}
		
		/**
		 * Returns the bitmap mask of this attribute 
		 * @return the bitmap mask of this attribute
		 */
		public int getBitCode()
		{
			return bitCode;
		}
		
		/**
		 * Returns true if the attribute is intuitively reversed, meaning
		 * that setting it turns it off, while clearing it turns it on.
		 * @return true if it is reversed, false for normal
		 */
		public boolean isAutoReversed()
		{
			return autoReverse;
		}
		
		/**
		 * The more official description code name of this attribute.
		 * @return more official description code name of this attribute.
		 */
		public String getName()
		{
			return desc;
		}
	}

	/**
	 * The number of ticks out of combat this mob should be before trying to sheath their weapon, assuming 
	 * the appropriate attribute is set.
	 */
	public static final long START_SHEATH_TIME=3*CMProps.getTickMillis();
	
	/**
	 * The number of ticks out of combat this mob will try to sheath their weapon, assuming 
	 * the appropriate attribute is set.
	 */
	public static final long END_SHEATH_TIME=6*CMProps.getTickMillis();
}
