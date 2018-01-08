package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg.View;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2018 Bo Zimmerman

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
 * Fighting, healing, determining combatants, death, attacking, 
 * damaging, and formations.  These are all the things handled
 * by the combat library.  But I'm betting you already knew that.
 * 
 * @author Bo Zimmerman
 *
 */
public interface CombatLibrary extends CMLibrary
{
	/**
	 * An enumeration of the several combat systems.
	 * These are specified in the coffeemud.ini file.
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public enum CombatSystem
	{
		/**
		 * The default system allows one action point to be used
		 * by each combatant on a skill, per round.  All other action
		 * points are automatically used on basic attacks, every round.
		 */
		DEFAULT,
		
		/**
		 * The queue system allows all action points to be used
		 * by each combatant on skills.  All unused action points are
		 * spent on basic attacks every round.
		 */
		QUEUE,
		
		/**
		 * The manual system allows all action points to be used
		 * by each combatant on skills or on basic attacks, but
		 * all attack must be manually entered.  Any unused points
		 * are lost every round. 
		 */
		MANUAL,
		
		/**
		 * The turn based system gives each combatant an amount of time
		 * to use all their action points on skills or basic attacks,
		 * not permitting any other combatant to act until the previous
		 * one completes.
		 */
		TURNBASED
	}

	/**
	 * Number of normal 4-second ticks per ship combat round
	 */
	public static final int TICKS_PER_SHIP_COMBAT = 4;
	
	/**
	 * Returns all the mobs in the same room as the given mob, which 
	 * that aren't in the mobs group.  It doesn't check
	 * combatability, and as such, is pretty loose.
	 * @see CombatLibrary#allCombatants(MOB)
	 * @see CombatLibrary#properTargets(Ability, MOB, boolean)
	 * @param mob the mob to check
	 * @param includePlayers true to include players, false not
	 * @return the set of mobs who might be a future combatant
	 */
	public Set<MOB> allPossibleCombatants(MOB mob, boolean includePlayers);

	/**
	 * Returns all the potential targets for the given ability, in the same
	 * room as the given mob.  If the skill is non-malicious, it returns 
	 * the mobs groups members, if the mob is in combat, it returns the 
	 * mobs in combat with him/her, and if the skill is malicious, it returns all
	 * potential combatants.
	 * @see CombatLibrary#allCombatants(MOB)
	 * @see CombatLibrary#allPossibleCombatants(MOB, boolean)
	 * @param A the skill you want targets for
	 * @param caster the user of the skill
	 * @param includePlayers true to include players, false otherwise
	 * @return the proper targets for the given skill
	 */
	public Set<MOB> properTargets(Ability A, MOB caster, boolean includePlayers);

	/**
	 * If the given mob is not in combat, this returns null.
	 * Otherwise, it returns all the mobs in the same room
	 * that are fighting the mob, or a follower.
	 * @see CombatLibrary#allPossibleCombatants(MOB, boolean)
	 * @see CombatLibrary#properTargets(Ability, MOB, boolean)
	 * @param mob the fighting mob
	 * @return the set of combatants.
	 */
	public Set<MOB> allCombatants(MOB mob);

	/**
	 * This strange method makes sure that none of the mobs
	 * in the given mobs group are fighting each other.
	 * @param mob the mob whose group needs peace.
	 */
	public void makePeaceInGroup(MOB mob);

	/**
	 * Returns the given mobs armor, after adjusting for
	 * hunger, thirst, fatigue, position, and dexterity.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PhyStats#armor()
	 * @param mob the mob whose armor needs adjusting
	 * @return the given mobs adjusted armor value
	 */
	public int adjustedArmor(MOB mob);

	/**
	 * Returns the given armor value, after adjusting for
	 * hunger, thirst, fatigue, position, and dexterity.
	 * Assumes an average random mob.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PhyStats#armor()
	 * @param armorValue the base armor value to start from
	 * @return the given adjusted armor value
	 */
	public int adjustedArmor(int armorValue);
	
	/**
	 * Returns the given mobs attack bonus, after adjusting for
	 * hunger, thirst, fatigue, position, and strength.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PhyStats#armor()
	 * @param mob the mob whose attack bonus needs adjusting
	 * @param target i guess this is who they are fighting
	 * @return the given mobs adjusted attack bonus
	 */
	public int adjustedAttackBonus(MOB mob, MOB target);

	/**
	 * Returns the given attack bonus, after adjusting for
	 * hunger, thirst, fatigue, position, and strength.
	 * Assumes an average mob otherwise.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PhyStats#armor()
	 * @param baseAttack the base attack bonus to work from.
	 * @return the given adjusted attack bonus
	 */
	public int adjustedAttackBonus(int baseAttack);

	/**
	 * Returns the given mob (or weapons, if provided) damage bonus,
	 * after adjusting for hunger, thirst, fatigue, position, and
	 * strength, from the proper damage formula, which depends on the
	 * weapon and the target.
	 * @param mob the mob who wants adjusted damage
	 * @param weapon the weapon used by the mob against target, or null
	 * @param target the target being hit, or null
	 * @param bonusDamage any bonus to base damage
	 * @param allowCrits true to apply crit if crit damage occurs
	 * @param biasHigh random numbers are always biased high
	 * @return the total adjusted damage
	 */
	public int adjustedDamage(MOB mob, Weapon weapon, MOB target, int bonusDamage, boolean allowCrits, boolean biasHigh);

	/**
	 * Returns the given unarmed damage bonus,
	 * after adjusting for hunger, thirst, fatigue, position, and
	 * strength, from the proper damage formula, which assumes a
	 * standard unarmed mob with average stats.
	 * @param baseDamage base damage
	 * @param level the level of the mob
	 * @param biasHigh random numbers are always biased high
	 * @return the total adjusted damage
	 */
	public int adjustedDamage(int baseDamage, int level, boolean biasHigh);

	/**
	 * Gathers the given attackers adjusted attack and the given defenders adjusted armor,
	 * applies a fudge factor, and makes a to-hit roll.  
	 * @see CombatLibrary#rollToHit(int, int, int)
	 * @param attacker the attacking mob
	 * @param defender the mob being attacked
	 * @return true if the hit roll succeeded, false if it failed
	 */
	public boolean rollToHit(MOB attacker, MOB defender);

	/**
	 * Given the exact attack score, against the given defense score, and 
	 * the given percentage adjustment.
	 * @see CombatLibrary#rollToHit(MOB, MOB)
	 * @param attack the attack score
	 * @param defence the armor score
	 * @param adjustment the percentage adjustment
	 * @return true if the attack roll succeeds, false if it fails.
	 */
	public boolean rollToHit(int attack, int defence, int adjustment);

	/**
	 * Forces all the mobs in the same room to stop
	 * fighting the given mob, with possible exceptions.
	 * @param mob the mob who needs peace
	 * @param exceptionSet null, or a set of mobs not to apply this to.
	 */
	public void forcePeaceAllFightingAgainst(final MOB mob, final Set<MOB> exceptionSet);

	/**
	 * Returns all the mobs in the same room as the given mob
	 * who are attacking the given mob, and puts them in the given set.
	 * @param mob the mob who is in combat
	 * @param set the set to put combatants in, or null to make one
	 * @return the same set sent in
	 */
	public Set<MOB> getAllFightingAgainst(final MOB mob, Set<MOB> set);

	/**
	 * When a mobs hit points fall below the wimp level, they panic,
	 * which often causes the mob to flee.  This method will initiate
	 * that process by causing a combat panic, and either posting
	 * it to the room, or adding it as a trailer to the given msg.
	 * Generates a CMMsg message and sends it to the mobs room.
	 * @see CombatLibrary#postDeath(MOB, MOB, CMMsg)
	 * @param mob the mob who is panicing
	 * @param addHere null, or the message to add the panic to
	 */
	public void postPanic(MOB mob, CMMsg addHere);

	/**
	 * This method will create an official death message for the
	 * given deadM mob, by the given killerM mob.  It will then
	 * either post it, or add it as a trailer to the given msg
	 * Generates a CMMsg message and sends it to the target room.
	 * @see CombatLibrary#postPanic(MOB, CMMsg)
	 * @param killerM the killer mob
	 * @param deadM the dead mob
	 * @param addHere null, or the message to add this one to.
	 */
	public void postDeath(MOB killerM, MOB deadM, CMMsg addHere);

	/**
	 * This method causes a mundane attack to occur by the given
	 * attack to the given target using the given weapon.  If 
	 * no weapon is given, it might attempt a draw first.
	 * Generates a CMMsg message and sends it to the target room.
	 * @param attacker the attacker mob
	 * @param target the target mob
	 * @param weapon the weapon used by the attacker, or null
	 * @return true if the attack succeeded, false if it failed.
	 */
	public boolean postAttack(MOB attacker, MOB target, Item weapon);

	/**
	 * This method causes a mundane attack by the given attacking mob (which
	 * may be a fake factory mob) on the given attacking ship against the
	 * given target ship with the given siege weapon.
	 * @param attacker the attacking agent mob
	 * @param attackingShip the ship the attacker is on
	 * @param target the target ship
	 * @param weapon the siege weapon used
	 * @param wasAHit true to register a hit, false to register an attack
	 * @return true if the attack succeeded, false if it failed
	 */
	public boolean postShipAttack(MOB attacker, PhysicalAgent attackingShip, PhysicalAgent target, Weapon weapon, boolean wasAHit);
	
	/**
	 * Returns whether the given attacking mob, on the given attacker ship, may attack the people and property
	 * of the given defending ship.  
	 * @param mob the agent attacker
	 * @param defender the attacked ship
	 * @return true if an attack is authorized, false otherwise
	 */
	public boolean mayIAttackThisVessel(final MOB mob, final PhysicalAgent defender);
	
	/**
	 * Posts a message of healing from the given healer to the given
	 * target using the given optional Ability tool.  There are no
	 * optional parameters here, except the msg of course
	 * Generates a CMMsg message and sends it to the target room.
	 * @param healer the healer mob
	 * @param target the target mob being healed
	 * @param tool the skill doing the healing, or null
	 * @param healing the amount of healing to do
	 * @param messageCode msg code for the source and others code
	 * @param allDisplayMessage the string to show everyone
	 * @return true if the healing post worked, false otherwise
	 */
	public boolean postHealing(MOB healer, MOB target, Ability tool, int healing, int messageCode, String allDisplayMessage);

	/**
	 * Because damage messages are basically always modified in message preview (okMessage),
	 * there is no point in putting the amount of damage into the message string.  Because of this
	 * the string &lt;DAMAGE&gt; or &lt;DAMAGES&gt; is put as a placeholder, and then this method
	 * is called to replace those tags with actual damage words based on the given final amount 
	 * and the given weapon type, and a clue as to who would see the message 
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg.View
	 * @see CombatLibrary#postDamage(MOB, MOB, Environmental, int, int, int, String)
	 * @param str the original string with the appropriate tag
	 * @param damage the final amount of damage
	 * @param damageType the weapon type code {@link com.planet_ink.coffee_mud.Items.interfaces.Weapon#TYPE_BASHING}
	 * @param sourceTargetSTO the view of the message
	 * @return the final modified string
	 */
	public String replaceDamageTag(String str, int damage, int damageType, View sourceTargetSTO);

	/**
	 * The official way to post damage that is happening.  
	 * It generates a message and sends it to the target room.
	 * Handles MXP tagging, fight coloring, and other details on the
	 * string message.  replaceDataTag is called to ensure a proper
	 * damage word.  
	 * @see CombatLibrary#replaceDamageTag(String, int, int, View)
	 * @param attacker the attacking mob
	 * @param target the target mob being healed
	 * @param weapon the item weapon, ability skill, or null tool used to damage
	 * @param damage the initial amount of damage
	 * @param messageCode msg code for the source and others code
	 * @param damageType the weapon type code {@link com.planet_ink.coffee_mud.Items.interfaces.Weapon#TYPE_BASHING}
	 * @param allDisplayMessage the message to send 
	 */
	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage);

	/**
	 * An alternative to {@link CombatLibrary#postWeaponAttackResult(MOB, MOB, Item, boolean)}.
	 * This method handles only a hit with a weapon, which will post damage.
	 * Generates a CMMsg message and sends it to the SOURCE room.  Call this
	 * instead of postAttackResult when the amount of damage done is custom
	 * instead of random.
	 * @return the message sent to the source room, or null
	 * @see CombatLibrary#replaceDamageTag(String, int, int, View)
	 * @param source the attacker
	 * @param target the target
	 * @param item the weapon used
	 * @param damageInt the amount of damage done by the weapon
	 */
	public CMMsg postWeaponDamage(MOB source, MOB target, Item item, int damageInt);
	
	/**
	 * This method handles both a hit or a miss with a weapon.  The
	 * hit, obviously, posts damage, while the miss, posts a miss.
	 * replaceDataTag is called to ensure a proper damage word.
	 * Generates a CMMsg message and sends it to the SOURCE room.
	 * @return the message sent to the source room, or null
	 * @see CombatLibrary#replaceDamageTag(String, int, int, View)
	 * @param source the attacker
	 * @param target the target
	 * @param item the weapon used
	 * @param success true if it was a hit with damage, false if it was a miss
	 */
	public CMMsg postWeaponAttackResult(MOB source, MOB target, Item item, boolean success);

	/**
	 * This method handles both a hit or a miss with a weapon between two
	 * ships in combat.  The hit, obviously, posts damage, while the miss, 
	 * posts a miss. replaceDataTag is called to ensure a proper damage word.
	 * Generates a CMMsg message and sends it to the common room.
	 * @see CombatLibrary#replaceDamageTag(String, int, int, View)
	 * @param source the agent of the attack
	 * @param attacker the attacker
	 * @param defender the target
	 * @param weapon the weapon used
	 * @param success true if it was a hit with damage, false if it was a miss
	 */
	public void postShipWeaponAttackResult(MOB source, PhysicalAgent attacker, PhysicalAgent defender, Weapon weapon, boolean success);
	
	/**
	 * This method handles an item taking damage.  If the item is subject
	 * to wear and tear, it will take the amount of specific damage
	 * given until it gets below 0, after which it is destroyed.  If the
	 * item is not subject to wear and tear, any positive damage destroys
	 * the item.
	 * @param mob the mob doing damage to an item
	 * @param I the item being damaged
	 * @param tool the weapon or skill used to do the damage
	 * @param damageAmount the amount of damage done (0-100)
	 * @param messageType the CMMsg message code for source and others
	 * @param message the message string
	 */
	public void postItemDamage(MOB mob, Item I, Environmental tool, int damageAmount, int messageType, String message);
	
	/**
	 * Returns the front of the follower line for
	 * this mob.  If this mob is following someone, it returns
	 * the MOB being ultimately followed, otherwise it 
	 * just returns the mob
	 * @param mob the mob who might be following someone
	 * @return the leader mob
	 */
	public MOB getFollowedLeader(MOB mob);
	
	/**
	 * Returns this mobs combat formation an an array
	 * of string lists, where each entry is a "row" in the
	 * formation, and the lists contain the mobs at that
	 * row.  Only the leaders formation settings matter,
	 * so any mob can be sent, because the leader will be
	 * teased out and used.
	 * @see CombatLibrary#getFormationFollowed(MOB)
	 * @see CombatLibrary#getFormationAbsOrder(MOB)
	 * @param mob a member of a group with a formation.
	 * @return the formation.
	 */
	public List<MOB>[] getFormation(MOB mob);
	
	/**
	 * Returns the list of mobs behind the given mob in
	 * their respective formation order.
	 * @see CombatLibrary#getFormation(MOB)
	 * @see CombatLibrary#getFormationAbsOrder(MOB)
	 * @param mob the mob in the formation
	 * @return the list of mobs behind the given one
	 */
	public List<MOB> getFormationFollowed(MOB mob);
	
	/**
	 * Returns the numeric position of the given mob
	 * in his or her combat formation.
	 * @see CombatLibrary#getFormationFollowed(MOB)
	 * @see CombatLibrary#getFormation(MOB)
	 * @param mob the mob in formation
	 * @return the numeric order, with 0 being front.
	 */
	public int getFormationAbsOrder(MOB mob);
	
	/**
	 * Returns the character class of the given killer, 
	 * or their leader if they are following someone
	 * who is not a mob.
	 * @see CombatLibrary#getCombatDividers(MOB, MOB, CharClass)
	 * @see CombatLibrary#getCombatBeneficiaries(MOB, MOB, CharClass)
	 * @param killer the killer
	 * @param killed the killed
	 * @return the leaders char class
	 */
	public CharClass getCombatDominantClass(MOB killer, MOB killed);
	
	/**
	 * Returns all the mobs for whom experience awards must be divided
	 * before awarding.  This does not mean the others do not get 
	 * experience, just that they aren't counted for the purposes of
	 * decreasing the experience each member wil get.  This is usually
	 * used to exclude the mob followers of certain classes.
	 * @see CombatLibrary#getCombatBeneficiaries(MOB, MOB, CharClass)
	 * @see CombatLibrary#getCombatDominantClass(MOB, MOB)
	 * @param killer the killer
	 * @param killed the killed the killer killed
	 * @param combatCharClass the charclass of the leader (usually)
	 * @return the set of mobs who must decrease experience through division.
	 */
	public Set<MOB> getCombatDividers(MOB killer, MOB killed, CharClass combatCharClass);

	/**
	 * Returns all the mobs set to benefit from the death of the given killed
	 * mob by the given killer.  This means going through the followers,
	 * and asking the classes if they are allowed to benefit from xp.\
	 * @see CombatLibrary#getCombatDividers(MOB, MOB, CharClass)
	 * @see CombatLibrary#getCombatDominantClass(MOB, MOB)
	 * @param killer the killer of the killed
	 * @param killed the killed one
	 * @param combatCharClass the charclass of the killer
	 * @return the set of mobs who get xp from the kill
	 */
	public Set<MOB> getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass);

	/**
	 * Returns the friendly armor string for the given mob,
	 * describing how well armored they are.
	 * @see CombatLibrary#fightingProwessStr(MOB)
	 * @param mob the mob who has armor
	 * @return the displayable armor string
	 */
	public String armorStr(MOB mob);

	/**
	 * Returns the friendly attack string for the given mob,
	 * describing how well attacky they are.
	 * @see CombatLibrary#armorStr(MOB)
	 * @param mob the mob who has attack
	 * @return the displayable attack string
	 */
	public String fightingProwessStr(MOB mob);

	/**
	 * Returns the friendly damage prowess string for the given mob,
	 * describing how well damagy they are.
	 * @param mob the mob who damages
	 * @return the displayable damage string
	 */
	public String damageProwessStr(MOB mob);

	/**
	 * Given the weapon type and amount of damage,
	 * this method returns the hit/damage string from
	 * the lists.ini file that matches.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Weapon#TYPE_BASHING
	 * @param type the weapon type
	 * @param damage the amount of damage
	 * @return the hit/damage word
	 */
	public String standardHitWord(int type, int damage);

	/**
	 * Given the weapon type and percent of damage 0 to 1,
	 * this method returns the hit/damage string from
	 * the lists.ini file that matches.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Weapon#TYPE_BASHING
	 * @param type the weapon type
	 * @param pct the percent of damage from 0.0 to 1.0
	 * @return the hit/damage word
	 */
	public String standardHitWord(int type, double pct);

	/**
	 * Given the weapon type and classification and name,
	 * this method returns either the fullly filled out weapon
	 * miss string, or generic non-extended non-weapon miss string
	 * from the lists.ini file that matches.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Weapon#TYPE_DESCS
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Weapon#CLASS_DESCS
	 * @param weaponDamageType the weapon type
	 * @param weaponClassification the weapon classification
	 * @param weaponName the name of the weapon
	 * @param useExtendedMissString true to include the weapon name, false for a shorter message
	 * @return the fully formed swing and miss string
	 */
	public String standardMissString(int weaponDamageType, int weaponClassification, String weaponName, boolean useExtendedMissString);

	/**
	 * Given the weapon type and classification and name,
	 * this method returns either the fullly filled out weapon
	 * hit/damage string from the lists.ini file that matches.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Weapon#TYPE_DESCS
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Weapon#CLASS_DESCS
	 * @param weaponDamageType the weapon type
	 * @param weaponClass the weapon classification
	 * @param damageAmount the amount of damage done
	 * @param weaponName the name of the weapon
	 * @return the fully formed hit and damage string
	 */
	public String standardHitString(int weaponDamageType, int weaponClass, int damageAmount,  String weaponName);

	/**
	 * When a particular race does not provide its own override
	 * health condition message, this method provides the base
	 * message from the list.ini file.  It returns the condition
	 * of the given mob as seen by the given viewer.
	 * @param viewer the viewer of the mob
	 * @param mob the mob who has a health condition
	 * @return the condition of the health of the mob
	 */
	public String standardMobCondition(MOB viewer, MOB mob);

	/**
	 * When the source does something to the target that the 
	 * target resists, and the given message has a targetminor
	 * containing the type of damage that's being resisted,
	 * this message will generate and tack on a new message
	 * with the resistance of the target, and flag the given
	 * message as having been resisted.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#addTrailerMsg(CMMsg)
	 * @param source the attacker
	 * @param target the defender who is resisting
	 * @param msg the message to flag as being resisted and tack on resistance msg
	 */
	public void resistanceMsgs(MOB source, MOB target, CMMsg msg);

	/**
	 * Given an attacking source and a defending target and the sources weapon
	 * or skill, this method will set the distance between the source and target
	 * from each other.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#setRangeToTarget(int)
	 * @param source the attacker
	 * @param target the target
	 * @param tool the sources weapon
	 */
	public void establishRange(MOB source, MOB target, Environmental tool);

	/**
	 * When the given observerM witnesses the given attacker mob attacking
	 * the given defender mob, this method checks to see if the observer has
	 * a dog in the fight, and if they do, causes them to start fighting
	 * either the attacker or the defender.
	 * @param observerM the observer mob who might be a follower
	 * @param defenderM the defender mob who is being attacked
	 * @param attackerM the attacker mob who is attacking the defender
	 */
	public void makeFollowersFight(MOB observerM, MOB defenderM, MOB attackerM);

	/**
	 * When a healing message targeting a given mob is received, 
	 * this method is called to actually do the healing.
	 * @param msg the healing message
	 */
	public void handleBeingHealed(CMMsg msg);

	/**
	 * When a damaging message targeting a given mob is received, 
	 * this method is called to actually do the damaging.
	 * @param msg the damaging message
	 */
	public void handleBeingDamaged(CMMsg msg);

	/**
	 * When an attack message targeting a given mob is received, 
	 * this method is called to react to the attack.  If the 
	 * target is not in combat, range is established and the target
	 * is pissed off (has their victim set).  An attack roll is
	 * then made for the source and the results used to alter the
	 * message. The target will also be made to stand.
	 * The message has a source attacker, target, and the tool is
	 * a weapon.
	 * @param msg the attack message
	 */
	public void handleBeingAssaulted(CMMsg msg);

	/**
	 * When a player has nobattlespam, this method is called when
	 * damage is observed to add to the totals.
	 * @param observerM the observer of the combat
	 * @param target the damaged thing.
	 * @param amount the amount of damage.
	 * @return true if it was counted
	 */
	public boolean handleDamageSpam(MOB observerM, final Physical target, int amount);
	
	/**
	 * When a player has nobattlespam, this method is called when
	 * damage is observed to report the totals from the last  
	 * combat round.
	 * @param mob the no spam observer.
	 */
	public void handleDamageSpamSummary(final MOB mob);
	
	/**
	 * When a death message is received by a mob and the message
	 * has the mob as a source, this method is called to kill
	 * the source of the message off.  The message has a source
	 * as dead person, the target is null, and the tool is the
	 * killer.
	 * @param msg the death message
	 */
	public void handleDeath(CMMsg msg);
	
	/**
	 * When an observer observes a death, this method is called
	 * is called to have the observer react.
	 * @param observer the one observing the death
	 * @param fighting the dead mob
	 * @param msg the death message
	 */
	public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg);

	/**
	 * When a death occurs, there are certain record keeping processes
	 * that need to happen.  This method does nothing but make sure
	 * those records are kept. The message has a source
	 * as dead person, the target is null, and the tool is the
	 * killer.
	 * @param msg the death message
	 */
	public void doDeathPostProcessing(CMMsg msg);

	/**
	 * Returns whether the system properties specify that, when the
	 * given dead mob does by the hand of the given killer mob, that
	 * the dead mob won't actually die, but just be knocked out.
	 * @param deadM the dead mob
	 * @param killerM the killer mob
	 * @return true if there's no death, but only being knocked out
	 */
	public boolean isKnockedOutUponDeath(MOB deadM, MOB killerM);

	/**
	 * When a player dies or flees, the system coffeemud.ini file
	 * defines the consequences of losing the fight. 
	 * @param deadM the mob who died or is fleeing
	 * @param killerM the killer or attacker of the given mob
	 * @param consequences the list of consequence strings from the ini file
	 * @param lostExperience a one dimensional array containing the base experience to lose
	 * @param message the xp loss message, sometimes localized, sometimes not.. you got me!
	 * @return false if the mob is obliterated, true otherwise
	 */
	public boolean handleCombatLossConsequences(MOB deadM, MOB killerM, String[] consequences, int[] lostExperience, String message);

	/**
	 * This is the heart of the main combat engine.  Every tick that a mob
	 * is in combat, and is permitted to use auto attacks, this method is called.
	 * It figures out how many weapon attacks to dish out, and dishes them. 
	 * @param fighter the attacker
	 */
	public void tickCombat(MOB fighter);

	/**
	 * If an NPC attacker comes under the sudden effect of a spell, and 
	 * knows who is responsible, but are not presently in combat, this
	 * method will start combat with them. 
	 * @param attacker the wronged npc party
	 * @param defender the defender who wronged the attacker
	 * @return true if an attack was tried, false if not.
	 */
	public boolean postRevengeAttack(MOB attacker, MOB defender);
	
	/**
	 * Every tick, this method is called.  If the given mob is not
	 * in combat, it will help the mob recover some of their hit points,
	 * mana, movement, etc.
	 * @param mob the mob who is recovering
	 */
	public void recoverTick(MOB mob);

	/**
	 * The heart of the alternative turn-based combat engine, this method is
	 * called every tick to determine if it is the given mobs turn to fight.
	 * If it is not the method returns false, and if it is, true
	 * @param mob the mob who wants to fight
	 * @param R the room the mob is in
	 * @param A the area the room is in
	 * @return true if its time to fight, false otherwise
	 */
	public boolean doTurnBasedCombat(final MOB mob, final Room R, final Area A);

	/**
	 * Every tick, THIS method is called to make the given mob a little more
	 * hungry and thirsty. It might even expend movement if they are walking
	 * around.
	 * @param mob the mob who needs to get hungry
	 * @param expendMovement true to also expend the rooms movement amt, false otherwise
	 */
	public void expendEnergy(final MOB mob, final boolean expendMovement);

	/**
	 * For a valid set of killers who are benefitting from having killed the given killed mob,
	 * this method will make repeated postExperience calls after having calculated their
	 * exp bounty for the kill.
	 * @see ExpLevelLibrary#postExperience(MOB, MOB, String, int, boolean)
	 * @param killers a set of mobs to benefit from the kill
	 * @param dividers a set of mobs who must divide the xp.. usually subset of killers
	 * @param killed the mob killed
	 */
	public void dispenseExperience(Set<MOB> killers, Set<MOB> dividers, MOB killed);
	
	/**
	 * When a mob can't breathe, it actually takes some work to figure out if
	 * anyone is to blame.  This method attempts to place the blame anywhere
	 * but on the victim him or herself.
	 * @param victim the one who can't breathe
	 * @return the mob to blame
	 */
	public MOB getBreatheKiller(MOB victim);
	
	/**
	 * Returns whether the given item is classified as a ammunition
	 * firing siege weapon, as used on a sailing ship.
	 * @param I the item to check
	 * @return true if its a siege weapon, false otherwise
	 */
	public boolean isAShipSiegeWeapon(Item I);
	
	/**
	 * Returns the number of base hull points that the given ship has.
	 * @param ship the ship to get points for
	 * @return the base hull points of the ship
	 */
	public int getShipHullPoints(BoardableShip ship);
	
	/**
	 * Checks to see if the given message gets a saving throw 
	 * for the given mob and, if so, applies it.
	 * @param mob the mob to save
	 * @param msg the message that might apply
	 * @return true if the message na or save only, false to cancel
	 */
	public boolean checkSavingThrows(final MOB mob, final CMMsg msg);
	
	/**
	 * Checks to see if the given message gets a saving throw 
	 * for the given mob damage and, if so, adjusts it
	 * @param mob the mob to save
	 * @param msg the message that might apply
	 * @return true if the message na or save only, false to cancel
	 */
	public boolean checkDamageSaves(final MOB mob, final CMMsg msg);
}
