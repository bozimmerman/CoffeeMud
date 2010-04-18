package com.planet_ink.coffee_mud.CharClasses.interfaces;
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
/**
 * This class represents a player or mobs character class.  One of more of these
 * objects are associated with each mob through the mob interfaces charStats() object.
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#charStats()
 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
 * @author Bo Zimmerman
 */
@SuppressWarnings("unchecked")
public interface CharClass extends Tickable, StatsAffecting, MsgListener, CMObject, CMModifiable
{
	/**
	 * Returns the generally displayable name of this class.  Usually deferred to
	 * by name(int), which is more often called.
	 * @see CharClass#name(int)
	 * @see CharClass#nameSet()
	 */
	public String name();

	/**
	 * Returns the displayable name of this class, when the mob is the
	 * given class level.  Usually defers to name()
	 * @see CharClass#name()
	 * @see CharClass#nameSet()
	 */
    public String name(int classLevel);

	/**
	 * Returns all of the displayable names of this class.  Usually defers to name()
	 * @see CharClass#name()
	 * @see CharClass#name(int)
	 */
    public String[] nameSet();

    /**
     * Returns the base-class of this class.  Typically only important in multi-classing
     * systems that restrict class changing to those classes part of the same base class.
     * True multi-classing systems don't need to worry about this value.  Can be the same
     * as the ID() method.
     * @return the base-class of this class
     */
	public String baseClass();

	/**
	 * Returns one or a combination of the Area.THEME_*
	 * constants from the Area interface.  This bitmap
	 * then describes the types of areas, skills, and
	 * classes which can interact.
	 * This bitmap is also used to to tell whether
	 * the class is available for selection by users
	 * at char creation time, whether they can
	 * change to this class via spells, or whether
	 * the class is utterly unavailable to them.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area
	 * @return the availability/theme of this class
	 */
	public int availabilityCode();

	/**
	 * Returns whether this given mob qualifies for this class, and
	 * optionally gives them an error message.
	 * @param mob the mob to evaluate the worthiness of
	 * @param quiet false to give the mob error messages, true for silence
	 * @return whether the given mob is worthy of this class
	 */
	public boolean qualifiesForThisClass(MOB mob, boolean quiet);

	/**
	 * Returns any boot-time parameters that are required to fully define
	 * this instance of a charclass.  Charclasses are shared among mobs,
	 * but that doesn't mean mulitple instances of a single class can't
	 * generate different versions of themselves for the classloader, with
	 * different parameters.  GenCharClass is an example.
	 * @see CharClass#setClassParms(String)
	 * @see CharClass#isGeneric()
	 * @return any parameters used to define this class
	 */
	public String classParms();

	/**
	 * Sets any boot-time parameters that are required to fully define
	 * this instance of a charclass.  Charclasses are shared among mobs,
	 * but that doesn't mean mulitple instances of a single class can't
	 * generate different versions of themselves for the classloader, with
	 * different parameters.  GenCharClass is an example.
	 * @see CharClass#classParms()
	 * @see CharClass#isGeneric()
	 * @param parms any parameters used to define this class
	 */
	public void setClassParms(String parms);

	/**
	 * Returns whether this class is fully defined using the setParms
	 * method, as opposed to being defined by its Java code.
	 * @see CharClass#classParms()
     * @see CharClass#makeGenCharClass()
	 * @see CharClass#setClassParms(String)
	 * @return whether this class is defined fully by parameters
	 */
	public boolean isGeneric();

    /**
     * Converts this class into a generic one, if it is not already.
     * If it is generic, this method returns itself.  Otherwise, the
     * standard char class is converted to a generic one and returned.
     * @see CharClass#isGeneric()
     * @return a generic version of this class.
     */
    public CharClass makeGenCharClass();
	/**
	 * Returns a Vector of security flag strings granted to all mobs/players
	 * who are this class, and the given class level or lower.
	 * @param classLevel the class level of the mob
	 * @return a vector of security flag strings
	 */
    public Vector getSecurityGroups(int classLevel);

    /**
     * This method should be called whenever a mob has this class added to
     * their charStats list. Its purpose is to outfit the mob with any
     * necessary abilities, or perform other necessary changes to the mob
     * to reflect the class addition or change.  A character class is
     * considered borrowed if its existence is derived from something else,
     * or its skills/abilities should not be saved as a permanent feature
     * of the mob.
     * @param mob the mob being outfitted with this class
     * @param isBorrowedClass whether the charclasses skills are borrowed(true) or permanent
     * @param verifyOnly send true if no skills or changes are to be made
     */
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly);

	/**
	 * Called when a class is no longer the current dominant class of a player or mob,
	 * usually during class training.
	 * @param mob the mob whose career to end or put aside
	 */
	public void endCharacter(MOB mob);


	/**
	 * Returns whether the given mob should share in the experience gained by the killer
	 * for having killed the killed.  Assumes the mob is in the same room, and requires
	 * the followers of the killer be passed in.
	 * @param killer the killer mob
	 * @param killed who the killer mob killed
	 * @param mob the mob whose sharing capacity is being evaluated
	 * @param followers the killers followers
	 * @return whether the mob shares in the exp gains
	 */
    public boolean isValidClassBeneficiary(MOB killer, MOB killed, MOB mob, HashSet followers);

	/**
	 * Returns whether the given mob should count in the division of experience gained by the killer
	 * for having killed the killed.  Assumes the mob is in the same room, and requires
	 * the followers of the killer be passed in.
	 * @param killer the killer mob
	 * @param killed who the killer mob killed
	 * @param mob the mob whose sharing capacity is being evaluated
	 * @param followers the killers followers
	 * @return whether the mob shares in the exp gains
	 */
    public boolean isValidClassDivider(MOB killer, MOB killed, MOB mob, HashSet followers);
    
    /**
     * Typically called when a mob gains a level in this class, to allow the class to 
     * assign any new skills.  Can also be called just to populate a mob with class skills,
     * so it should also confirm any lower level skills also.
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
     * @param mob the mob to give abilities to.
     * @param isBorrowedClass whether the skills are savable (false) or temporary (true)
     */
	public void grantAbilities(MOB mob, boolean isBorrowedClass);
	
	/**
	 * This method is called whenever a player gains a level while a member of this class.  If
	 * there are any special things which need to be done to a player who gains a level, they
	 * can be done in this method.  By default, it doesn't do anything.
	 * @param mob the mob to level up
	 * @param gainedAbilityIDs the set of abilities/skill IDs gained during this leveling process
	 */
	public void level(MOB mob, Vector gainedAbilityIDs);
	
	/**
	 * Whenever a player or mob of this race gains experience, this method gets a chance
	 * to modify the amount before the gain actually occurs.
	 * @param host the player or mob whose class is being queried  
	 * @param mob the player or mob gaining experience
	 * @param victim if applicable, the mob or player who died to give the exp
	 * @param amount the amount of exp on track for gaining
	 * @return the adjusted amount of experience to gain
	 */
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount);
	
	/**
	 * This method is called whenever a player loses a level while a member of this class.  If
	 * there are any special things which need to be done to a player who loses a level, they
	 * can be done in this method.  By default, it doesn't do anything.
	 * @param mob the mob to level down
	 */
	public void unLevel(MOB mob);
	
	/**
	 * Returns a vector of Item objects representing the standard
	 * clothing, weapons, or other objects commonly given to players
	 * of this class just starting out.
	 * @param myChar one who will receive the objects
	 * @return a vector of Item objects
	 */
	public Vector outfit(MOB myChar);

	/**
	 * This method is called whenever a player casts a spell which has a lasting
	 * effect on the target.  This method is called even if the class is not the
	 * players CURRENT class.
	 * @param myChar the caster or skill user
	 * @param skill the skill or spell that was cast.
	 * @param duration the default duration
	 * @return usually, it just returns default again
	 */
	public int classDurationModifier(MOB myChar, Ability skill, int duration);

	/**
	 * Returns the number of bonus practices received by members of
	 * this class when they gain a level.  This is over and above
	 * the normal formula applied during the leveling process.
	 * @return the number of bonus practices to grant
	 */
	public int getBonusPracLevel();
	/**
	 * Returns the number of bonus attack points received by members of
	 * this class when they gain a level.  This is over and above
	 * the normal formula applied during the leveling process.
	 * @return the number of bonus attack points to grant
	 */
	public int getBonusAttackLevel();
	/**
	 * Returns which of the CharStats.STAT_* constants should be
	 * used to calculate the standard attack prowess points given
	 * when a member of this class gains a level.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @return a CharStats.STAT_* attribute constant code
	 */
	public int getAttackAttribute();
	/**
	 * Returns the number of practices received by members of
	 * this class when they are first created.
	 * @return the number of initial practices to grant
	 */
	public int getPracsFirstLevel();
	/**
	 * Returns the number of training points received by members of
	 * this class when they are first created.
	 * @return the number of initial trains to grant
	 */
	public int getTrainsFirstLevel();
	/**
	 * Returns the number of levels which must be gained by a member
	 * of this class before they gain 1 more point of default damage
	 * per hit.
	 * @return the number of levels between damage gains
	 */
	public int getLevelsPerBonusDamage();
	/**
	 * The number multiplied by this classes movement-related character stat
	 * (Strength, Int, etc) in order to determine the BASE movement gained every
	 * level.  This is applied after the stat score has been divided by 18.
	 * @return a multiplier for a players movement related char stat score 
	 */
	public int getMovementMultiplier();
	/**
	 * This number is used to generate the hit point bonus for mobs/players
	 * when they gain levels by dividing their hit point stat (constitution)
	 * by this number.
	 * @see CharClass#getHPDice()
	 * @see CharClass#getHPDie()
	 * @return the hit point char stat divisor
	 */
	public int getHPDivisor();
	/**
	 * Hit points gained upon level is calculated by multiplying this stat by
	 * getHPDie().
	 * @see CharClass#getHPDie()
	 * @return a hit point gain multiplier
	 */
	public int getHPDice();
	/**
	 * Hit points gained upon level is calculated by multiplying this stat by
	 * getHPDice().
	 * @see CharClass#getHPDice()
	 * @return a hit point gain multiplier
	 */
	public int getHPDie();
	/**
	 * This number is used to generate the mana bonus for mobs/players
	 * when they gain levels by dividing their mana stat (intelligence)
	 * by this number.
	 * @see CharClass#getManaDice()
	 * @see CharClass#getManaDie()
	 * @return the mana char stat divisor
	 */
	public int getManaDivisor();
	/**
	 * mana gained upon level is calculated by multiplying this stat by
	 * getManaDie().
	 * @see CharClass#getManaDie()
	 * @return a mana gain multiplier
	 */
	public int getManaDice();
	/**
	 * mana gained upon level is calculated by multiplying this stat by
	 * getManaDice().
	 * @see CharClass#getManaDice()
	 * @return a mana gain multiplier
	 */
	public int getManaDie();
	/**
	 * Returns a text description of any weapon restrictions
	 * imposed by this class upon its members.
	 * @return a text description of weapon retrictions
	 */
	public String getWeaponLimitDesc();
	/**
	 * Returns a text description of any armor restrictions
	 * imposed by this class upon its members.
	 * @return a text description of armor retrictions
	 */
	public String getArmorLimitDesc();
	/**
	 * Returns a text description of any misc restrictions
	 * imposed by this class upon its members.
	 * @return a text description of misc retrictions
	 */
	public String getOtherLimitsDesc();
	/**
	 * Returns a text description of any bonus properties
	 * granted by this class to its members.
	 * @return a text description of bonus properties
	 */
	public String getOtherBonusDesc();
	/**
	 * Returns a text description of the stat qualifications
	 * required to become a member of this character class
	 * @return a txt description of stat qualifications
	 */
	public String getStatQualDesc();
	/**
	 * Returns a text description of the attack bonuses
	 * gained by members of this class.
	 * @return a text description of the attack bonuses
	 */
	public String getAttackDesc();
	/**
	 * Returns the prime statistic of this class
	 * @return the prime statistic of this class
	 */
	public String getPrimeStatDesc();
	/**
	 * Returns a text description of the movement bonuses
	 * gained by members of this class.
	 * @return a text description of the movement bonuses
	 */
	public String getMovementDesc();
	/**
	 * Returns a text description of the mana bonuses
	 * gained by members of this class.
	 * @return a text description of the mana bonuses
	 */
	public String getManaDesc();
	/**
	 * Returns a text description of the hit point bonuses
	 * gained by members of this class.
	 * @return a text description of the hit point bonuses
	 */
	public String getHitPointDesc();
	/**
	 * Returns a text description of the damage bonuses
	 * gained by members of this class.
	 * @return a text description of the damage bonuses
	 */
	public String getDamageDesc();
	/**
	 * Returns a text description of the train bonuses
	 * gained by members of this class.
	 * @return a text description of the train bonuses
	 */
	public String getTrainDesc();
	/**
	 * Returns a text description of the practice bonuses
	 * gained by members of this class.
	 * @return a text description of the practice bonuses
	 */
	public String getPracticeDesc();
	/**
	 * Returns the list of max stats for members of this
	 * class.
	 * @return the max stat values for this class.
	 */
	public String getMaxStatDesc();
	
    /**
     * Returns the highest class level that can be achieved
     * by a player who has this class.  Once this level is
     * reached, the class behaves as it is were levelless.
     * Default is -1, meaning the cap does not exist.
     * @return highest class level for this class;
     */
    public int getLevelCap();
    
	/**
	 * Returns a bonus or negative adjustments to the base
	 * maximum for the CharStats.STAT_* base statistics.
	 * The maximum is the most a player can train up to.
	 * The array only holds enough to the first 6 base stats.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @return a six element array of adjustments to max base stats
	 */
	public int[] maxStatAdjustments();

	/**
	 * Whether this class can be associated with a race.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @return whether this class can have a class
	 */
	public boolean raceless();
	/**
	 * Whether players of this class can be associated with an experience level.
	 * @return whether players of this class can have a level
	 */
	public boolean leveless();
	/**
	 * Whether players of this class can gain or lose experience points.
	 * @return whether players of this class can gain or lose experience points
	 */
	public boolean expless();

	/**
	 * Max number of common gathering/non-crafting skills this class can learn.
	 * 0 means unlimited.  Skills directly qualified for by the class are excepted.
	 * @return Max number of common gathering/non-crafting skills this class can learn.
	 */
	public int maxNonCraftingSkills();

	/**
	 * Max number of common crafting skills this class can learn.
	 * 0 means unlimited.  Skills directly qualified for by the class are excepted.
	 * @return Max number of common crafting skills this class can learn.
	 */
	public int maxCraftingSkills();
	
	/**
	 * Max number of common skills (both crafting and non-crafting) this class can learn.
	 * 0 means unlimited.  Skills directly qualified for by the class are excepted.
	 * @return Max number of common skills this class can learn.
	 */
	public int maxCommonSkills();
	
	/**
	 * Max number of languages this class can learn.
	 * 0 means unlimited.  Languages directly qualified for by the class or race are excepted.
	 * @return Max number of languages this class can learn.
	 */
	public int maxLanguages();
	
	/**
	 * A code designating what kind of armor can be used by this class
	 * without affecting their skills.  The worn locations this coded
	 * type refers to locations defined by ARMOR_WEARMASK
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass#ARMOR_WEARMASK
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass#ARMOR_ANY
	 * @return the encoded allowed armor type
	 */
	public int allowedArmorLevel();
	
	/**
	 * A code designating what kind of weapons can be used by this class
	 * without fumbling their usage.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass#WEAPONS_ANY
	 * @return the encoded allowed weapon type
	 */
	public int allowedWeaponLevel();
	
	/** constant returned by allowedArmorLevel() to designate any allowed armors. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedArmorLevel() */
	public static final int ARMOR_ANY=0;
	/** constant returned by allowedArmorLevel() to designate only cloth armors. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedArmorLevel() */
	public static final int ARMOR_CLOTH=1;
	/** constant returned by allowedArmorLevel() to designate only leather armors. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedArmorLevel() */
	public static final int ARMOR_LEATHER=2;
	/** constant returned by allowedArmorLevel() to designate only nonmetal armors. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedArmorLevel() */
	public static final int ARMOR_NONMETAL=3;
	/** constant returned by allowedArmorLevel() to designate only plant/wood armors. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedArmorLevel() */
	public static final int ARMOR_VEGAN=4;
	/** constant returned by allowedArmorLevel() to designate only metal armors. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedArmorLevel() */
	public static final int ARMOR_METALONLY=5;
	/** constant returned by allowedArmorLevel() to designate only metal/stone armors. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedArmorLevel() */
	public static final int ARMOR_OREONLY=6;
	/** useful constant for calculating the wear locations to which armor restrictions apply */
	public static long ARMOR_WEARMASK=Wearable.WORN_TORSO|Wearable.WORN_LEGS|Wearable.WORN_ARMS|Wearable.WORN_WAIST|Wearable.WORN_HEAD;
	/** list of string descriptions for the CharClass.ARMOR_* constants, ordered by their value.  @see CharClass */
	public static final String[] ARMOR_DESCS={
		"ANY","CLOTH","LEATHER","NONMETAL","VEGAN","METALONLY","OREONLY"
	};
	/** list of long string descriptions for the CharClass.ARMOR_* constants, ordered by their value.  @see CharClass */
	public static final String[] ARMOR_LONGDESC={
		"May wear any armor.",
		"Must wear cloth, vegetation, or paper based armor.",
		"Must wear leather, cloth, or vegetation based armor.",
		"Must wear non-metal armor.",
		"Must wear wood or vegetation based armor.",
		"Must wear metal armor",
		"Must wear stone, crystal, or metal armor."
	};

	/** constant returned by allowedWeaponLevel() to designate any weapons. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_ANY=0;
	/** constant returned by allowedWeaponLevel() to designate daggers only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_DAGGERONLY=1;
	/** constant returned by allowedWeaponLevel() to designate swords/daggers only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_THIEFLIKE=2;
	/** constant returned by allowedWeaponLevel() to designate natural weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_NATURAL=3;
	/** constant returned by allowedWeaponLevel() to designate burglar class weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_BURGLAR=4;
	/** constant returned by allowedWeaponLevel() to designate stone weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_ROCKY=5;
	/** constant returned by allowedWeaponLevel() to designate mage weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_MAGELIKE=6;
	/** constant returned by allowedWeaponLevel() to designate evil cleric weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_EVILCLERIC=7;
	/** constant returned by allowedWeaponLevel() to designate good cleric weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_GOODCLERIC=8;
	/** constant returned by allowedWeaponLevel() to designate neutral cleric weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_NEUTRALCLERIC=9;
	/** constant returned by allowedWeaponLevel() to designate any cleric weapons only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_ALLCLERIC=10;
	/** constant returned by allowedWeaponLevel() to designate flails only. @see com.planet_ink.coffee_mud.CharClass.StdCharClass#allowedWeaponLevel() */
	public static final int WEAPONS_FLAILONLY=11;
	/** constant set of integer arrays defining the Weapon.CLASS_* constants for the CharClass.WEAPONS_* constants, ordered by CharClass.WEAPONS_* values. */
	public static final int[][] WEAPONS_SETS={
/*0*/{Weapon.CLASS_AXE,Weapon.CLASS_BLUNT,Weapon.CLASS_DAGGER,Weapon.CLASS_EDGED,Weapon.CLASS_FLAILED,Weapon.CLASS_HAMMER,Weapon.CLASS_NATURAL,Weapon.CLASS_POLEARM,Weapon.CLASS_RANGED,Weapon.CLASS_STAFF,Weapon.CLASS_SWORD,Weapon.CLASS_THROWN},
/*1*/{Weapon.CLASS_NATURAL,Weapon.CLASS_DAGGER},
/*2*/{Weapon.CLASS_SWORD,Weapon.CLASS_RANGED,Weapon.CLASS_THROWN,Weapon.CLASS_NATURAL,Weapon.CLASS_DAGGER,Weapon.CLASS_EDGED},
/*3*/{RawMaterial.MATERIAL_WOODEN,RawMaterial.MATERIAL_UNKNOWN,RawMaterial.MATERIAL_VEGETATION,RawMaterial.MATERIAL_FLESH,RawMaterial.MATERIAL_LEATHER},
/*4*/{Weapon.CLASS_NATURAL,Weapon.CLASS_SWORD,Weapon.CLASS_FLAILED,Weapon.CLASS_BLUNT,Weapon.CLASS_DAGGER,Weapon.CLASS_EDGED},
/*5*/{RawMaterial.MATERIAL_ROCK,RawMaterial.MATERIAL_UNKNOWN,RawMaterial.MATERIAL_GLASS,RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL,RawMaterial.MATERIAL_PRECIOUS},
/*6*/{Weapon.CLASS_NATURAL,Weapon.CLASS_DAGGER,Weapon.CLASS_STAFF},
/*7*/{Weapon.CLASS_EDGED,Weapon.CLASS_POLEARM,Weapon.CLASS_AXE,Weapon.CLASS_SWORD,Weapon.CLASS_DAGGER},
/*8*/{Weapon.CLASS_BLUNT,Weapon.CLASS_HAMMER,Weapon.CLASS_FLAILED,Weapon.CLASS_NATURAL,Weapon.CLASS_STAFF},
/*9*/{Weapon.CLASS_BLUNT,Weapon.CLASS_RANGED,Weapon.CLASS_THROWN,Weapon.CLASS_STAFF,Weapon.CLASS_NATURAL,Weapon.CLASS_SWORD},
/*10*/{Weapon.CLASS_AXE,Weapon.CLASS_BLUNT,Weapon.CLASS_DAGGER,Weapon.CLASS_EDGED,Weapon.CLASS_FLAILED,Weapon.CLASS_HAMMER,Weapon.CLASS_NATURAL,Weapon.CLASS_POLEARM,Weapon.CLASS_RANGED,Weapon.CLASS_STAFF,Weapon.CLASS_SWORD,Weapon.CLASS_THROWN},
/*11*/{Weapon.CLASS_NATURAL,Weapon.CLASS_FLAILED},
	};
	/** list of string descriptions for the CharClass.WEAPONS_* constants, ordered by their value.  @see CharClass */
	public static final String[] WEAPONS_LONGDESC={
/*0*/"May use any weapons.",
/*1*/"Must use dagger-like or natural weapons.",
/*2*/"Must use swords, daggers, natural, or ranged weapons.",
/*3*/"Must use wooden, plant-based, or leather weapons.",
/*4*/"Must use sword, daggers, flailed, blunt, or natural weapons.",
/*5*/"Must use stone, crystal, metal, or glass weapons.",
/*6*/"Must use daggers, staves, or natural weapons.",
/*7*/"Must use polearms, axes, swords, daggers, or edged weapons.",
/*8*/"Must use hammers, staves, flailed, natural, or blunt weapons.",
/*9*/"Must use swords, staves, natural, ranged, or blunt weapons",
/*10*/"Evil must use polearm, sword, axe, edged, or natural.  Neutral must use blunt, ranged, thrown, staff, natural, or sword.  Good must use blunt, flailed, natural, staff, or hammer.",
/*11*/"Must use flailed weapons."
	};

	/** for character classes that define themselves using getParms, this can designate racelessness bitmaps */
	public final static int GENFLAG_NORACE=1;
	/** for character classes that define themselves using getParms, this can designate levelless bitmaps */
	public final static int GENFLAG_NOLEVELS=2;
	/** for character classes that define themselves using getParms, this can designate expless bitmaps */
	public final static int GENFLAG_NOEXP=4;
    /** constant string list naming each of the GENFLAG_* constants in the order of their value */
    public final static String[] GENFLAG_DESCS={"RACELESS","LEVELLESS","EXPLESS"};
}
