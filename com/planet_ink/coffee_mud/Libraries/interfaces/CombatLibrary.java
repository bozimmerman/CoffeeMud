package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import java.util.*;
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
*/
@SuppressWarnings("unchecked")
public interface CombatLibrary extends CMLibrary
{
    public static final int COMBAT_DEFAULT=0;
    public static final int COMBAT_QUEUE=1;
    public static final int COMBAT_MANUAL=2;
    
    public HashSet allPossibleCombatants(MOB mob, boolean beRuthless);
    public HashSet properTargets(Ability A, MOB caster, boolean beRuthless);
	public int adjustedArmor(MOB mob);
	public int adjustedAttackBonus(MOB mob, MOB target);
	public int adjustedDamage(MOB mob, Weapon weapon, MOB target);
    public boolean rollToHit(MOB attacker, MOB defender);
    public boolean rollToHit(int attack, int defence, int adjustment);
    public HashSet allCombatants(MOB mob);
    public void makePeaceInGroup(MOB mob);
    public void postPanic(MOB mob, CMMsg addHere);
    public void postDeath(MOB killerM, MOB deadM, CMMsg addHere);
    public boolean postAttack(MOB attacker, MOB target, Item weapon);
    public boolean postHealing(MOB healer, MOB target, Environmental tool, int messageCode, int healing, String allDisplayMessage);
    public String replaceDamageTag(String str, int damage, int damageType);
    public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage);
    public void postWeaponDamage(MOB source, MOB target, Item item, boolean success);
    public void processFormation(Vector[] done, MOB leader, int level);
    public MOB getFollowedLeader(MOB mob);
    public Vector[] getFormation(MOB mob);
    public Vector getFormationFollowed(MOB mob);
    public int getFormationAbsOrder(MOB mob);
    public CharClass getCombatDominantClass(MOB killer, MOB killed);
    public HashSet getCombatDividers(MOB killer, MOB killed, CharClass combatCharClass);
    public HashSet getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass);
    public DeadBody justDie(MOB source, MOB target);
    public String armorStr(MOB mob);
    public String standardHitWord(int type, int damage);
    public String fightingProwessStr(MOB mob);
    public String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString);
    public String standardHitString(int weaponClass, int damageAmount,  String weaponName);
    public String standardMobCondition(MOB viewer, MOB mob);
    public void resistanceMsgs(CMMsg msg, MOB source, MOB target);
    public void establishRange(MOB source, MOB target, Environmental tool);
    public void makeFollowersFight(MOB observer, MOB target, MOB source);
    public void handleBeingHealed(CMMsg msg);
    public void handleBeingDamaged(CMMsg msg);
    public void handleBeingAssaulted(CMMsg msg);
    public void handleDeath(CMMsg msg);
    public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg);
    public boolean isKnockedOutUponDeath(MOB mob, MOB fighting);
    public boolean handleConsequences(MOB mob, MOB fighting, String[] commands, int[] lostExperience, String message);
    public void tickCombat(MOB fighter);
    
    /**
     * For a valid set of killers who are benefitting from having killed the given killed mob,
     * this method will make repeated postExperience calls after having calculated their
     * exp bounty for the kill. 
     * @see ExpLevelLibrary#postExperience(MOB, MOB, String, int, boolean)
     * @param killers a set of mobs to benefit from the kill
     * @param dividers a set of mobs who must divide the xp.. usually subset of killers
     * @param killed the mob killed 
     */
	public void dispenseExperience(HashSet killers, HashSet dividers, MOB killed);
}
