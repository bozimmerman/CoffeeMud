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

	public Set<MOB> allPossibleCombatants(MOB mob, boolean beRuthless);
	public Set<MOB> properTargets(Ability A, MOB caster, boolean beRuthless);
	public int adjustedArmor(MOB mob);
	public int adjustedAttackBonus(MOB mob, MOB target);
	public int adjustedDamage(MOB mob, Weapon weapon, MOB target, int bonusDamage, boolean allowCrits);
	public boolean rollToHit(MOB attacker, MOB defender);
	public boolean rollToHit(int attack, int defence, int adjustment);
	public Set<MOB> allCombatants(MOB mob);
	public void makePeaceInGroup(MOB mob);
	public void forcePeaceAllFightingAgainst(final MOB mob, final Set<MOB> exceptionSet);
	public Set<MOB> getAllFightingAgainst(final MOB mob, Set<MOB> set);
	public void postPanic(MOB mob, CMMsg addHere);
	public void postDeath(MOB killerM, MOB deadM, CMMsg addHere);
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
