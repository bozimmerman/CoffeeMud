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
   Copyright 2000-2005 Bo Zimmerman

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
public interface CombatLibrary extends CMObject
{
    public static final int ARMOR_CEILING=500;
    public static final int PROWESS_CEILING=1000;
    
    public static final int COMBAT_DEFAULT=0;
    public static final int COMBAT_QUEUE=1;
    
    public HashSet allPossibleCombatants(MOB mob, boolean beRuthless);
    public HashSet properTargets(Ability A, MOB caster, boolean beRuthless);
    public boolean rollToHit(MOB attacker, MOB defender);
    public boolean rollToHit(int attack, int defense);
    public HashSet allCombatants(MOB mob);
    public void makePeaceInGroup(MOB mob);
    public void postPanic(MOB mob, CMMsg addHere);
    public void postDeath(MOB killerM, MOB deadM, CMMsg addHere);
    public boolean postAttack(MOB attacker, MOB target, Item weapon);
    public boolean postHealing(MOB healer, MOB target, Environmental tool, int messageCode, int healing, String allDisplayMessage);
    public String replaceDamageTag(String str, int damage, int damageType);
    public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage);
    public boolean postExperience(MOB mob, MOB victim, String homage, int amount, boolean quiet);
    public boolean changeFactions(MOB mob, MOB victim, int amount, boolean quiet);
    public void postWeaponDamage(MOB source, MOB target, Item item, boolean success);
    public void processFormation(Vector[] done, MOB leader, int level);
    public MOB getFollowedLeader(MOB mob);
    public Vector[] getFormation(MOB mob);
    public Vector getFormationFollowed(MOB mob);
    public int getFormationAbsOrder(MOB mob);
    public CharClass getCombatDominantClass(MOB killer, MOB killed);
    public HashSet getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass);
    public DeadBody justDie(MOB source, MOB target);
    public int[] damageThresholds();
    public String[][] hitWords();
    public String[] armorDescs();
    public String[] prowessDescs();
    public String[] missWeaponDescs();
    public String[] missDescs();
    public String[] healthDescs();
    public String armorStr(int armor);
    public String standardHitWord(int type, int damage);
    public String fightingProwessStr(int prowess);
    public String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString);
    public String standardHitString(int weaponClass, int damageAmount,  String weaponName);
    public String standardMobCondition(MOB mob);
    public void resistanceMsgs(CMMsg msg, MOB source, MOB target);
    
    public static final String[] DEFAULT_ARMOR_DESCS={
        "vulnerable",
        "slightly covered",
        "somewhat covered",
        "covered",
        "well covered",
        "very covered",
        "slightly protected",
        "somewhat protected",
        "protected",
        "well protected",
        "very protected",
        "heavily protected",
        "slightly armored",
        "somewhat armored",
        "armored",
        "armored",
        "well armored",
        "very armored",
        "heavily armored",
        "completely armored",
        "totally armored",
        "divinely armored",
        "slightly unhittable",
        "somewhat unhittable",
        "practically unhittable",
        "unhittable",
        "unhittable",
        "totally unhittable",
        "totally unhittable",
        "slightly impenetrable",
        "slightly impenetrable",
        "somewhat impenetrable",
        "somewhat impenetrable",
        "almost impenetrable",
        "almost impenetrable",
        "impenetrable",
        "impenetrable",
        "slightly invincible",
        "slightly invincible",
        "slightly invincible",
        "somewhat invincible",
        "somewhat invincible",
        "somewhat invincible",
        "somewhat invincible",
        "almost invincible",
        "almost invincible",
        "almost invincible",
        "almost invincible",
        "almost invincible",
        "invincible!",
    };

    public static final String[] DEFAULT_PROWESS_DESCS={
        "none",
        "novice",
        "initiate",
        "trainee",
        "barely skilled",
        "a little skilled",
        "slightly skilled",
        "somewhat skilled",
        "almost skilled",
        "mostly skilled",
        "simply skilled",
        "skilled",
        "really skilled",
        "obviously skilled",
        "very skilled",
        "extremely skilled",
        "terribly skilled",
        "masterfully skilled",
        "a little dangerous",
        "barely dangerous",
        "slightly dangerous",
        "somewhat dangerous",
        "almost dangerous",
        "mostly dangerous",
        "simply dangerous",
        "dangerous",
        "really dangerous",
        "obviously dangerous",
        "very dangerous",
        "extremely dangerous",
        "terribly dangerous",
        "horribly dangerous",
        "fearfully dangerous",
        "frighteningly dangerous",
        "totally dangerous",
        "entirely dangerous",
        "a novice master I",
        "a novice master II",
        "a novice master III",
        "a master initiate I",
        "a master initiate II",
        "a master initiate III",
        "an apprentice master I",
        "an apprentice master II",
        "an apprentice master III",
        "a master I",
        "a master I",
        "a master II",
        "a master II",
        "a master III",
        "a master III",
        "a master IV",
        "a master IV",
        "a master V",
        "a master V",
        "a master VI",
        "a master VI",
        "a master VII",
        "a master VII",
        "a master VIII",
        "a master VIII",
        "a master IX",
        "a master IX",
        "a master X",
        "a master X",
        "an initiate of death I",
        "an initiate of death II",
        "an initiate of death III",
        "an apprentice of death I",
        "an apprentice of death II",
        "an apprentice of death III",
        "a servant of death I",
        "a servant of death II",
        "a servant of death III",
        "a bringer of death I",
        "a bringer of death II",
        "a bringer of death III",
        "a bringer of death IV",
        "a giver of death I",
        "a giver of death II",
        "a giver of death III",
        "a giver of death V",
        "a giver of death VI",
        "a giver of death VIII",
        "a giver of death X",
        "a dealer of death I",
        "a dealer of death II",
        "a dealer of death III",
        "a dealer of death IV",
        "a dealer of death V",
        "a dealer of death VI",
        "a dealer of death VIII",
        "a dealer of death X",
        "a master of death I",
        "a master of death II",
        "a master of death III",
        "a master of death IV",
        "a master of death V",
        "a master of death VII",
        "a master of death VIII",
        "a master of death IX",
        "a master of death X",
        "a lord of death I",
        "a lord of death II",
        "a lord of death III",
        "a lord of death IV",
        "a lord of death V",
        "a lord of death VI",
        "a lord of death VII",
        "a lord of death VIII",
        "a lord of death IX",
        "a lord of death X",
        "death incarnate!"
    };
    
    public static final String[] DEFAULT_WEAPON_MISS_DESCS={
        "<S-NAME> fire(s) at <T-NAMESELF> with <TOOLNAME> and miss(es).", // 0
        "<S-NAME> throw(s) <TOOLNAME> at <T-NAMESELF> and miss(es).", // 1
        "<S-NAME> swing(s) at <T-NAMESELF> with <TOOLNAME> and miss(es).", //2
        "<S-NAME> attack(s) <T-NAMESELF> with <TOOLNAME> and miss(es).", //3
        "<S-NAME> lunge(s) at <T-NAMESELF> with <TOOLNAME> and miss(es)." //4
    };
    public static final String[] DEFAULT_MISS_DESCS={
        "<S-NAME> fire(s) at <T-NAMESELF> and miss(es).", //0
        "<S-NAME> throw(s) at <T-NAMESELF> and miss(es).", //1
        "<S-NAME> swing(s) at <T-NAMESELF> and miss(es).", //2
        "<S-NAME> attack(s) <T-NAMESELF> and miss(es).",  //3
        "<S-NAME> lunge(s) at <T-NAMESELF> and miss(es)." //4
    };
    
    public static final String[][] DEFAULT_DAMAGE_WORDS={
    {"ALL","annoy(s)", "scratch(es)","graze(s)","wound(s)","cut(s)","damage(es)","decimate(s)","murder(s)",
        "massacre(s)", "MASSACRE(S)", "destroy(s)", "DESTROY(S)", "obliterate(s)", "OBLITERATE(S)", "**OBLITERATE(S)**", "--==::OBLITERATE(S)::==--"},
    {""+Weapon.TYPE_NATURAL, "annoy(s)", "scratch(es)","graze(s)","hit(s)","cut(s)","hurt(s)","rip(s)","crunch(es)"},
    {""+Weapon.TYPE_SLASHING, "annoy(s)", "scratch(es)","graze(s)","wound(s)","cut(s)","slice(s)","gut(s)","murder(s)"},
    {""+Weapon.TYPE_PIERCING, "annoy(s)", "scratch(es)","graze(s)","prick(s)","cut(s)","stab(s)","pierce(s)","murder(s)"},
    {""+Weapon.TYPE_BASHING, "annoy(s)", "scratch(es)","graze(s)","hit(s)","smash(es)","bash(es)","crush(es)","crunch(es)"},
    {""+Weapon.TYPE_BURNING, "annoy(s)", "warm(s)","heat(s)","singe(s)","burn(s)","flame(s)","scorch(es)","incinerate(s)"},
    {""+Weapon.TYPE_SHOOT, "annoy(s)", "scratch(es)","graze(s)","hit(s)","pierce(s)","pierce(s)","decimate(s)","murder(s)"},
    {""+Weapon.TYPE_MELTING, "annoy(s)", "sting(s)","sizzle(s)","burn(s)","scorch(es)","dissolve(s)","melt(s)","melt(s)"},
    {""+Weapon.TYPE_STRIKING, "annoy(s)", "sting(s)","charge(s)","singe(s)","burn(s)","scorch(es)","blast(s)","incinerate(s)"},
    {""+Weapon.TYPE_BURSTING, "annoy(s)", "scratch(es)","graze(s)","wound(s)","cut(s)","damage(es)","decimate(s)","murder(s)"},
    };
    
    public static final int[] DEFAULT_DAMAGE_THRESHHOLDS={0,3,6,10,15,25,35,50,70,100,130,165,215,295,395};
    
    public static final String[] DEFAULT_HEALTH_CHART={
        "^r<MOB>^r is hovering on deaths door!^N",
        "^r<MOB>^r is covered in blood.^N",
        "^r<MOB>^r is bleeding badly from lots of wounds.^N",
        "^y<MOB>^y has numerous bloody wounds and gashes.^N",
        "^y<MOB>^y has some bloody wounds and gashes.^N",
        "^p<MOB>^p has a few bloody wounds.^N",
        "^p<MOB>^p is cut and bruised.^N",
        "^g<MOB>^g has some minor cuts and bruises.^N",
        "^g<MOB>^g has a few bruises and scratches.^N",
        "^g<MOB>^g has a few small bruises.^N",
        "^c<MOB>^c is in perfect health.^N"
    };
}
