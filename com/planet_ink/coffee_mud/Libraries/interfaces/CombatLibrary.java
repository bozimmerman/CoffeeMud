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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2015 Bo Zimmerman

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
	 * Returns the given mobs attack bonus, after adjusting for
	 * hunger, thirst, fatigue, position, and strength.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PhyStats#armor()
	 * @param mob the mob whose attack bonus needs adjusting
	 * @param target i guess this is who they are fighting
	 * @return the given mobs adjusted attack bonus
	 */
	public int adjustedAttackBonus(MOB mob, MOB target);
	
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
	 * @return the total adjusted damage
	 */
	public int adjustedDamage(MOB mob, Weapon weapon, MOB target, int bonusDamage, boolean allowCrits);

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
	 * @param mob the mob who is panicing
	 * @param addHere null, or the message to add the panic to
	 */
	public void postPanic(MOB mob, CMMsg addHere);
	
	/**
	 * This method will create an official death message for the
	 * given deadM mob, by the given killerM mob.  It will then
	 * either post it, or add it as a trailer to the given msg
	 * @param killerM the killer mob
	 * @param deadM the dead mob
	 * @param addHere null, or the message to add this one to.
	 */
	public void postDeath(MOB killerM, MOB deadM, CMMsg addHere);
	
	/**
	 * This method causes a mundane attack to occur by the given
	 * attack to the given target using the given weapon.  If 
	 * no weapon is given, it might attempt a draw first.
	 * @param attacker the attacker mob
	 * @param target the target mob
	 * @param weapon the weapon used by the attacker, or null
	 * @return true if the attack succeeded, false if it failed.
	 */
	public boolean postAttack(MOB attacker, MOB target, Item weapon);
	public boolean postHealing(MOB healer, MOB target, Environmental tool, int messageCode, int healing, String allDisplayMessage);
	public String replaceDamageTag(String str, int damage, int damageType, char sourceTargetSTO);
	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage);
	public void postWeaponDamage(MOB source, MOB target, Item item, boolean success);
	public void postItemDamage(MOB mob, Item I, Environmental tool, int damageAmount, int messageType, String message);
	public void processFormation(List<MOB>[] done, MOB leader, int level);
	public MOB getFollowedLeader(MOB mob);
	public List<MOB>[] getFormation(MOB mob);
	public List<MOB> getFormationFollowed(MOB mob);
	public int getFormationAbsOrder(MOB mob);
	public CharClass getCombatDominantClass(MOB killer, MOB killed);
	public Set<MOB> getCombatDividers(MOB killer, MOB killed, CharClass combatCharClass);
	public Set<MOB> getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass);
	public DeadBody justDie(MOB source, MOB target);
	public String armorStr(MOB mob);
	public String standardHitWord(int type, int damage);
	public String fightingProwessStr(MOB mob);
	public String standardMissString(int weaponDamageType, int weaponClassification, String weaponName, boolean useExtendedMissString);
	public String standardHitString(int weaponDamageType, int weaponClass, int damageAmount,  String weaponName);
	public String standardMobCondition(MOB viewer, MOB mob);
	public void resistanceMsgs(CMMsg msg, MOB source, MOB target);
	public void establishRange(MOB source, MOB target, Environmental tool);
	public void makeFollowersFight(MOB observer, MOB target, MOB source);
	public void handleBeingHealed(CMMsg msg);
	public void handleBeingDamaged(CMMsg msg);
	public void handleBeingAssaulted(CMMsg msg);
	public void handleDeath(CMMsg msg);
	public void doDeathPostProcessing(CMMsg msg);
	public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg);
	public boolean isKnockedOutUponDeath(MOB mob, MOB fighting);
	public boolean handleConsequences(MOB mob, MOB fighting, String[] commands, int[] lostExperience, String message);
	public void tickCombat(MOB fighter);
	public void recoverTick(MOB mob);
	public boolean doTurnBasedCombat(final MOB mob, final Room R, final Area A);
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
}
