package com.planet_ink.coffee_mud.Abilities.interfaces;
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
   Copyright 2000-2006 Bo Zimmerman

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
 * The basic interface for all skills, spells, chants, prayers, 
 * and properties. Abilities are listed in a MOBs abilities 
 * list, and may be listed on *any* Environmental objects effects
 * list.  Often the same class files act in both capacities.
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#addEffect(Ability)
 */
public interface Ability extends Environmental
{
	/**
	 * Returns a bitmap describing the general 
	 * classification of the skill (spell, skill, 
	 * chant, or property, etc) and its domain.  It
	 * is made up of the ACODE_* constants the Ability
	 * interface, and optionally the DOMAIN_* constants
	 * also defined in this interface.
	 * @see Ability
	 * @return ACODE_* constant classification
	 */
	public int classificationCode();

	/**
	 * Returns a constant value notifying the system of
	 * the general quality of the skill, whether it is
	 * malicious, beneficial, indifferent, or qualifiably
	 * beneficial (ok), as well as whether it is intended
	 * to target the invoker (self) or others. These
	 * constants are defined in the Ability interface and
	 * called QUALITY_*
	 * @see Ability
	 * @return QUALITY_* constant classification
	 */
	public int abstractQuality();
	
	/**
	 * Identical to abstractQuality() method, but returns
	 * a quality based on how the skill or spell behaves
	 * as a spell effect on a weapon or on armor.  This
	 * helps decide whether this skill is appropriate in
	 * those roles.  These constants are defined in the 
	 * Ability interface and called QUALITY_*
	 * @see Ability#abstractQuality()
	 * @see Ability
	 * @return QUALITY_* constant classification
	 */
	public int enchantQuality();
	
	/**
	 * Serves a purpose similar to that of the Ability
	 * abstractQuality() method, but it determines a more
	 * exact quality based on a given invoker, target, and
	 * assumes an imminent use of the skill.  For this 
	 * reason, the QUALITY_OK_* constants should be avoided
	 * in favor of the other Ability.QUALITY_* constants.
	 * @see Ability
	 * @see Ability#abstractQuality()
	 * @param mob the person to use the skill
	 * @param target the potential target -- may be invoker
	 * @return QUALITY_* constant classification
	 */
	public int castingQuality(MOB mob, Environmental target);

	/**
	 * Returns a bitmap giving some specific information about
	 * the scope and purpose of the skill.  The bitmap is
	 * composed of FLAG_* constants from the Ability interface.
	 * @see Ability
	 * @return FLAG_* constant based bitmap
	 */
	public long flags();
	
	/**
	 * Always returns the mob that invoked or initiated the
	 * skill or effect.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @see Ability#setInvoker(MOB)
	 * @return the invoker mob
	 */
	public MOB invoker();
	
	/**
	 * Sets or changes the mob that invoked or initated the
	 * skill or effect.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @see Ability#invoker()
	 * @param mob the invoker mob
	 */
	public void setInvoker(MOB mob);

	/**
	 * Returns a reference to the room, item, mob, or exit
	 * that is currently being affected by this skill. Does
	 * not apply to non-affecting skills like kick, but more
	 * to auto-invoking skills and spells that affect people
	 * like sleep.
	 * @see Ability#setAffectedOne(Environmental)
	 * @return the room, mob, or item being affected
	 */
	public Environmental affecting();
	
	/**
	 * Sets a reference to the room, item, mob, or exit
	 * that is currently being affected by this skill. Does
	 * not apply to non-affecting skills like kick, but more
	 * to auto-invoking skills and spells that affect people
	 * like sleep, and especially to properties.
	 * @see Ability#affecting()
	 * @param being the room, mob, or item being affected
	 */
	public void setAffectedOne(Environmental being);

	/**
	 * Returns whether this skills triggerStrings should be
	 * places in the master list of commands, unlike 
	 * properties or other hidden skills.
	 * @see Ability#triggerStrings()
	 * @return whether to add the triggerString to the commands list
	 */
	public boolean putInCommandlist();

	/**
	 * A set of the command strings the user types to access
	 * this command when it is listed as a skill ability.
	 * @see Ability#putInCommandlist()
	 * @return an array of command invoking string words
	 */
	public String[] triggerStrings();

	/**
	 * Returns the number of actions required to completely
	 * activate this skill. A value of 0.0 means invoke 
	 * instantly.  This method only applies when the invoker
	 * is not in combat.
	 * @see Ability#combatCastingTime()
	 * @return the number of player free actions required to do this
	 */
	public double castingTime();
	
	/**
	 * Returns the number of actions required to completely
	 * activate this skill. A value of 0.0 means invoke 
	 * instantly.  This method only applies when the invoker
	 * is in combat.
	 * @see Ability#castingTime()
	 * @return the number of player free actions required to do this
	 */
	public double combatCastingTime();

	/**
	 * This method is only called when the mob invoking this skill
	 * does not have enough actions to complete it immediately.  The
	 * method is called when the command is entered, and every second
	 * afterwards until the invoker has enough actions to complete it.
	 * At completion time, invoke is called.
	 * @see Ability#invoke(MOB, Vector, Environmental, boolean, int)
	 * @param mob the player or mob invoking the skill
	 * @param commands the parameters entered for the skill (minus trigger word)
	 * @param givenTarget null, unless being auto-invoked. Represents an override target.
	 * @param auto false if player enters command, true if system invokes the command
	 * @param asLevel -1, unless being auto-invoked, when it is the level to invoke it at.
	 * @param secondsElapsed 0 at first, and increments every second
	 * @param actionsRemaining number of free actions the player is defficient.
	 * @return whether the skill should be allowed to invoke.  false cancels altogether.
	 */
    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining);
    
    /**
     * This method is called when a player or the system invokes this skill,  
     * casts this spell, etc.
     * Calls the more complete invoke method without an empty command strings vector
     * unless target is non-null, in which case the vector will contain the name
     * of the target. 
	 * @see Ability#invoke(MOB, Vector, Environmental, boolean, int)
	 * @param mob the player or mob invoking the skill
	 * @param target null, unless being auto-invoked. Represents an override target.
	 * @param auto false if player enters command, true if system invokes the command
	 * @param asLevel -1, unless being auto-invoked, when it is the level to invoke it at.
     * @return whether the skill successfully invoked.
     */
	public boolean invoke(MOB mob, Environmental target, boolean auto, int asLevel);
	
    /**
     * This method is called when a player or the system invokes this skill,  
     * casts this spell, etc.
     * Calls the more complete invoke method without an empty command strings vector
     * unless target is non-null, in which case the vector will contain the name
     * of the target. 
	 * @param mob the player or mob invoking the skill
	 * @param commands the parameters entered for the skill (minus trigger word)
	 * @param target null, unless being auto-invoked. Represents an override target.
	 * @param auto false if player enters command, true if system invokes the command
	 * @param asLevel -1, unless being auto-invoked, when it is the level to invoke it at.
     * @return whether the skill successfully invoked.
     */
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel);

	/**
	 * If this skill is uninvokable, this method will uninvoke it, remove it
	 * as an effect on the target, and shut it down.
	 * @see Ability#canBeUninvoked()
	 */
	public void unInvoke();
	
	/**
	 * This method is used to modify the behavior of the affectEnvStats, 
	 * affectCharStats, and affectCharState methods.  If this returns true,
	 * then those methods will be called only on the owner/mob of the item
	 * currently being affected by this skill.  If false, those methods
	 * behave normally.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Item#owner()
	 * @return whether this skill affects a mob owner of an item
	 */
	public boolean bubbleAffect();
	
	/**
	 * Whether this skill is allowed to be uninvoked.  If it returns false,
	 * then it behaves as a permanent property of the object it is affecting.
	 * Not death, dispelling, or other negations will remove it.
	 * @see Ability#makeNonUninvokable()
	 * @return whether this skill can be uninvoked.
	 */
	public boolean canBeUninvoked();
	
	/**
	 * Designates that this skill should never be uninvoked.  Designates that
	 * this skill is a permanent property of the object it is affecting.
	 * Is normally called by addNonUninvokableAffect
	 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#addNonUninvokableEffect(Ability)
	 * @see Ability#canBeUninvoked()
	 */
	public void makeNonUninvokable();

	/**
	 * If this ability has been added as as temporary effect, like
	 * sleep, or trip, or other typical spells, calling this
	 * method will make it so thatit will not do so on its own for a 
	 * long long time.  This method does not make it UNinvokable, but
	 * only makes it last a long time.
	 */
	public void makeLongLasting();

	/**
	 * An autoinvocating ability is an ability which affects the
	 * mob just by having learned the ability.  Dodge is an example of this.
	 * This method is called to initiate this, and is called whenever
	 * a mob gains this skill as a new Ability.  This method will 
	 * add the ability to the mob as an effect.
	 * @see Ability#isAutoInvoked()
	 * @see Ability#isNowAnAutoEffect()
	 * @param mob the player or npc mob who has this ability
	 * @return whether the ability autoinvoked correctly
	 */
	public boolean autoInvocation(MOB mob);
	
	/**
	 * An autoinvocating ability is an ability which affects the
	 * mob just by having learned the ability.  Dodge is an example of this.
	 * This method returns whether this skill is classified as one of
	 * those kinds of abilities.
	 * @return whether this is an autoinvoking skill
	 */
	public boolean isAutoInvoked();
	
	/**
	 * An autoinvocating ability is an ability which affects the
	 * mob just by having learned the ability.  Dodge is an example of this.
	 * Returns whether this instance was added to a mobs affects for
	 * this reason.
	 * @return whether this skill is currently an autoinvoked effect
	 */
	public boolean isNowAnAutoEffect();
	
	/**
	 * Returns an integer array telling the system how much mana, 
	 * movement, or hit points are required to invoke this skill.
	 * Use the Ability.USAGEINDEX_* constants to index the array.
	 * A value of 0-1000 is an absolute cost.  A value of 
	 * Integer.MAX_VALUE means to use all of the mana, movement, 
	 * hit points.  A value of Integer.MAXVALUE-100 and up 
	 * represents a percentage of the cost. 
	 * The values in this method MUST be accompanied by properly
	 * set usageType() bitmap.
	 * @see Ability#usageType()
	 * @see Ability
	 * @param mob the invoker of the skill
	 * @return an array of costs, indexed by Ability.USAGEINDEX_*
	 */
	public int[] usageCost(MOB mob);
	
	/**
	 * Returns a bitmap made up of constants defined by
	 * Ability.USAGE_*.  It lets the system know which of the
	 * values in the usageCost(MOB) method are relevant.  It
	 * determines whether this skill requires mana, movement,
	 * hit points, or some combination of the three.
	 * @see Ability
	 * @return a bitmap defined by Ability.USAGE_*
	 */
	public int usageType();

	/**
	 * Returns whether this skill may be saved to the database,
	 * or whether it is strictly a run-time thing that should
	 * not  be saved.
	 * @param truefalse whether this skill may be saved to the database
	 */
	public void setSavable(boolean truefalse);

	/**
	 * Returns an optional numeric value whose purpose is
	 * entirely contextual.  For many skills, this reflects
	 * some sort of enhancement.  Default is 0.
	 * @return the current optional numeric value or enhancement
	 */
	public int abilityCode();
	
	/**
	 * Sets an optional numeric value whose purpose is
	 * entirely contextual.  For many skills, this reflects
	 * some sort of enhancement.  Default is 0.
	 * @param newCode the optional numeric value or enhancement
	 */
	public void setAbilityCode(int newCode);
	
	/**
	 * Any external files which may be required to make this ability work
	 * files returned by this method should not be base distrib files!
	 * @return a Vector of local path names
	 */
	public Vector externalFiles();

	/**
	 * Returns whether the given teacher mob is able and allowed to teach
	 * this skill to the given student.  Error messages should be directed
	 * to both.  This method should focus exclusively on the qualifications
	 * of the teacher.
	 * @see Ability#canBeLearnedBy(MOB, MOB)
	 * @see Ability#teach(MOB, MOB)
	 * @param teacher the potential teacher of this skill
	 * @param student the potential learner of this skill
	 * @return whether the teacher can teach the skill to the student
	 */
	
	public boolean canBeTaughtBy(MOB teacher, MOB student);
	/**
	 * Returns whether the given teacher mob is able and allowed to practice
	 * this skill with the given student *and* whether the student is able
	 * and allowed to practice this skill.  Error messages should be directed
	 * to both.
	 * @see Ability#practice(MOB, MOB)
	 * @param teacher the potential practice teacher of this skill
	 * @param student the potential practicer of this skil
	 * @return whether the teacher and student are capable of practicing together
	 */
	public boolean canBePracticedBy(MOB teacher, MOB student);
	
	/**
	 * Returns whether the given student mob is able and allowed to learn
	 * this skill from the given teacher.  Error messages should be directed
	 * to both.  This method should focus exclusively on the qualifications
	 * of the student.
	 * @see Ability#canBeTaughtBy(MOB, MOB)
	 * @see Ability#teach(MOB, MOB)
	 * @param teacher the potential teacher of this skill
	 * @param student the potential learner of this skill
	 * @return whether the student can learn the skill from the teacher
	 */
	public boolean canBeLearnedBy(MOB teacher, MOB student);
	
	/**
	 * Teaches this skill to the student mob, presumably from the teacher mob.
	 * This method assumes that both the teacher and student are authorized.
	 * Authorization comes from canBeTaughtBy and canBeLearnedBy
	 * @see Ability#canBeTaughtBy(MOB, MOB)
	 * @see Ability#canBeLearnedBy(MOB, MOB)
	 * @param teacher the teacher of this skill
	 * @param student the learner of this skill
	 */
	public void teach(MOB teacher, MOB student);
	
	/**
	 * Causes the student to practice this skill, presumably with the teacher mob.
	 * This method assumes that both the teacher and student are authorized.
	 * Authorization comes from canBePracticedBy
	 * @see Ability#canBePracticedBy(MOB, MOB)
	 * @param teacher the teacher of this skill
	 * @param student the practicer of this skill
	 */
	public void practice(MOB teacher, MOB student);
	
	/**
	 * Returns a string describing the requirements and qualifications that
	 * are required to learn this skill.
	 * @return a description of the learning requirements of this skill.
	 */
	public String requirements();

	/**
	 * Returns whether, when used as a skill, this ability can target itself
	 * at the given object.  This method derives its answer from the protected
	 * integer method canTargetCode()
	 * @see com.planet_ink.coffee_mud.Abilities.StdAbility#canTargetCode()
	 * @param E the potential target of this skill
	 * @return whether E is a valid target
	 */
	public boolean canTarget(Environmental E);
	/**
	 * Returns whether, when used as a property/effect, this ability can affect
	 * the given object.  This method derives its answer from the protected
	 * integer method canAffectCode()
	 * @see com.planet_ink.coffee_mud.Abilities.StdAbility#canAffectCode()
	 * @param E the potential object to have this as a property/effect
	 * @return whether E is a valid object to have this as a property/effect
	 */
	public boolean canAffect(Environmental E);
	
	/**
	 * Returns whether, when used as a skill, this ability can target itself
	 * at an object of the given type.  This method derives its answer from the protected
	 * integer method canTargetCode(), and requires one of the Ability.CAN_* 
	 * constants.
	 * @see com.planet_ink.coffee_mud.Abilities.StdAbility#canTargetCode()
	 * @param can_code a bitmap of one or more Ability.CAN_* constants
	 * @return whether the object type is a valid target
	 */
	public boolean canTarget(int can_code);
	
	/**
	 * Returns whether, when used as a property/effect, this ability can affect
	 * the given type of object.  This method derives its answer from the protected
	 * integer method canAffectCode(), and requires one of the Ability.CAN_* 
	 * constants.
	 * @see Ability
	 * @see com.planet_ink.coffee_mud.Abilities.StdAbility#canAffectCode()
	 * @param can_code a bitmap of one or more Ability.CAN_* constants
	 * @return whether the object object type may have this as a property/effect
	 */
	public boolean canAffect(int can_code);

	/**
	 * For use by the identify spell, this should return a
	 * nice description of any properties incorporated
	 * by this effect.
	 * @return a description of properties incorporated
	 */
	public String accountForYourself();

	/**
	 * Returns whether there are any Faction reasons why the given
	 * mob should not use this skill.  Used primarily for alignment
	 * checks of Prayers.  
	 * @see com.planet_ink.coffee_mud.Abilities.Prayers.Prayer
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Faction
	 * @param mob the mob whose factions to check
	 * @return whether there are any faction reasons not to cast this.
	 */
    public boolean appropriateToMyFactions(MOB mob);
    
    /**
     * This method calculates the strength level of this skill for the
     * given mob, and taking into account the given override-level (asLevel).
     * The level returned should be based on a minimum baseline level (usually
     * the class level of the class that qualifies for this skill at the
     * lowest level), and should take into account the number of levels the
     * mob has over and above that in appropriate classes
     * @param mob the mob to evaluate the skill level of
     * @param asLevel if greater than 0, this method always returns it
     * @return the level of power the given mob has in this skill.
     */
	public int adjustedLevel(MOB mob, int asLevel);

	/**
	 * Adds this Ability object as an effect on the given affected target, and
	 * sets it to unInvoke after the given number of ticks.  This method 
	 * intelligently determines whether the target is already going to provide
	 * tick events to the Ability (like a mob) or whether the system should
	 * create a tick event for this ability (like on rooms or items).
	 * @param invoker the invoker of the ability
	 * @param affected the object to be affected by this ability
	 * @param tickTime the number of ticks to keep the ability ticking.
	 */
	public void startTickDown(MOB invoker, Environmental affected, int tickTime);

	/**
	 * Returns a number from 0-100 representing the percent of proficiency
	 * the mob or player who has this instance in their Abilities list has in
	 * this skill.  Is also used for other miscellaneous purposes by other non-skill
	 * Abilities, such as Drowning.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
	 * @see Ability#setProficiency(int)
	 * @return the proficiency from 0-100
	 */
	public int proficiency();
	
	/**
	 * Sets a number from 0-100 representing the percent of proficiency
	 * the mob or player who has this instance in their Abilities list has in
	 * this skill.  Is also used for other miscellaneous purposes by other non-skill
	 * Abilities, such as Drowning.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
	 * @see Ability#proficiency()
	 * @param newProficiency the proficiency from 0-100
	 */
	public void setProficiency(int newProficiency);
	
	/**
	 * Returns whether the given mob passes their proficiency check in this skill
	 * at this time.  Will accept a numeric adjustment, positive or negative, to
	 * their base proficiency.  Will also accept an auto parameter, which forces
	 * this method to always return true.
	 * @see Ability#proficiency()
	 * @param mob the mob whose proficiency to check
	 * @param adjustment a positive or negative adjustment to the mobs base proficiency
	 * @param auto if true, this method returns true always
	 * @return whether the mob passes their proficiency check
	 */
	public boolean proficiencyCheck(MOB mob, int adjustment, boolean auto);
	
	/**
	 * This method should, occassionally, add to the proficiency the given mob
	 * has in this skill.  There are no guarentees about how often this method
	 * is called, but presumably it is called when the mob is doing something
	 * that exercises this skill in some way.  How often proficiency is effected
	 * by this method is up to the method, but is typically time-based, and based
	 * on intelligence and other factors.
	 * @see Ability#proficiency()
	 * @param mob the mob whose proficiency in this skill to possibly advance
	 */
	public void helpProficiency(MOB mob);
	
	/** usageType() constant meaning that the skill is free @see Ability#usageType()*/
	public final static int USAGE_NADA=0;
	/** usageType() constant meaning that the skill costs mana @see Ability#usageType()*/
	public final static int USAGE_MANA=1;
	/** usageType() constant meaning that the skill costs movement @see Ability#usageType()*/
	public final static int USAGE_MOVEMENT=2;
	/** usageType() constant meaning that the skill costs hp @see Ability#usageType()*/
	public final static int USAGE_HITPOINTS=4;
	
	/** index into usageCost(MOB) array for the amount of mana this skill costs @see Ability#usageCost(MOB) */
	public final static int USAGEINDEX_MANA=0;
	/** index into usageCost(MOB) array for the amount of moves this skill costs @see Ability#usageCost(MOB) */
	public final static int USAGEINDEX_MOVEMENT=1;
	/** index into usageCost(MOB) array for the amount of hp this skill costs @see Ability#usageCost(MOB) */
	public final static int USAGEINDEX_HITPOINTS=2;

	/** constant returned by classificationCode() designating this ability as a Skill @see Ability#classificationCode() */
	public static final int ACODE_SKILL=0;
	/** constant returned by classificationCode() designating this ability as a Spell @see Ability#classificationCode() */
	public static final int ACODE_SPELL=1;
	/** constant returned by classificationCode() designating this ability as a Prayer @see Ability#classificationCode() */
	public static final int ACODE_PRAYER=2;
	/** constant returned by classificationCode() designating this ability as a Song @see Ability#classificationCode() */
	public static final int ACODE_SONG=3;
	/** constant returned by classificationCode() designating this ability as a Trap @see Ability#classificationCode() */
	public static final int ACODE_TRAP=4;
	/** constant returned by classificationCode() designating this ability as a Property @see Ability#classificationCode() */
	public static final int ACODE_PROPERTY=5;
	/** constant returned by classificationCode() designating this ability as a Thief Skill @see Ability#classificationCode() */
	public static final int ACODE_THIEF_SKILL=6;
	/** constant returned by classificationCode() designating this ability as a Language @see Ability#classificationCode() */
	public static final int ACODE_LANGUAGE=7;
	/** constant returned by classificationCode() designating this ability as a Chant @see Ability#classificationCode() */
	public static final int ACODE_CHANT=8;
	/** constant returned by classificationCode() designating this ability as a Common Skill @see Ability#classificationCode() */
	public static final int ACODE_COMMON_SKILL=9;
	/** constant returned by classificationCode() designating this ability as a Disease @see Ability#classificationCode() */
	public static final int ACODE_DISEASE=10;
	/** constant returned by classificationCode() designating this ability as a Poison @see Ability#classificationCode() */
	public static final int ACODE_POISON=11;
	/** constant returned by classificationCode() designating this ability as a Super Power @see Ability#classificationCode() */
	public static final int ACODE_SUPERPOWER=12;
	/** constant returned by classificationCode() designating this ability as a Archon ONly Skill @see Ability#classificationCode() */
	public static final int ALL_ACODES=31;
	/** array of string describtions for the ACODE_* constants, indexed by their values */
	public static final String[] ACODE_DESCS={
		"SKILL","SPELL","PRAYER","SONG","TRAP","PROPERTY",
		"THIEF SKILL","LANGUAGE","CHANT","COMMON SKILL",
		"DISEASE","POISON","SUPERPOWER","ARCHON SKILL"
	};

	/** constant mask returned by classificationCode() designating this ability as being Divination @see Ability#classificationCode() */
	public static final int DOMAIN_DIVINATION=1<<5;
	/** constant mask returned by classificationCode() designating this ability as being Abjuration @see Ability#classificationCode() */
	public static final int DOMAIN_ABJURATION=2<<5;
	/** constant mask returned by classificationCode() designating this ability as being Illusion @see Ability#classificationCode() */
	public static final int DOMAIN_ILLUSION=3<<5;
	/** constant mask returned by classificationCode() designating this ability as being Evocation @see Ability#classificationCode() */
	public static final int DOMAIN_EVOCATION=4<<5;
	/** constant mask returned by classificationCode() designating this ability as being Alteration @see Ability#classificationCode() */
	public static final int DOMAIN_ALTERATION=5<<5;
	/** constant mask returned by classificationCode() designating this ability as being Transmutation @see Ability#classificationCode() */
	public static final int DOMAIN_TRANSMUTATION=6<<5;
	/** constant mask returned by classificationCode() designating this ability as being Enchantment @see Ability#classificationCode() */
	public static final int DOMAIN_ENCHANTMENT=7<<5;
	/** constant mask returned by classificationCode() designating this ability as being Conjuration @see Ability#classificationCode() */
	public static final int DOMAIN_CONJURATION=8<<5;
	/** constant mask returned by classificationCode() designating this ability as being Archon @see Ability#classificationCode() */
	public static final int DOMAIN_ARCHON=9<<5;
	/** constant mask returned by classificationCode() designating this ability as being Singing @see Ability#classificationCode() */
	public static final int DOMAIN_SINGING=10<<5;
	/** constant mask returned by classificationCode() designating this ability as being Dancing @see Ability#classificationCode() */
	public static final int DOMAIN_DANCING=11<<5;
	/** constant mask returned by classificationCode() designating this ability as being Playing @see Ability#classificationCode() */
	public static final int DOMAIN_PLAYING=12<<5;
    /** constant mask returned by classificationCode() designating this ability as being Deceptive @see Ability#classificationCode() */
    public static final int DOMAIN_DECEPTIVE=13<<5;
    /** constant mask returned by classificationCode() designating this ability as being Detrapping @see Ability#classificationCode() */
    public static final int DOMAIN_DETRAP=14<<5;
    /** constant mask returned by classificationCode() designating this ability as being RopeUsing @see Ability#classificationCode() */
    public static final int DOMAIN_ROPEUSE=15<<5;
    /** constant mask returned by classificationCode() designating this ability as being Stealing @see Ability#classificationCode() */
    public static final int DOMAIN_STEALING=16<<5;
    /** constant mask returned by classificationCode() designating this ability as being Stealthy @see Ability#classificationCode() */
    public static final int DOMAIN_STEALTHY=17<<5;
    /** constant mask returned by classificationCode() designating this ability as being Trapping @see Ability#classificationCode() */
    public static final int DOMAIN_TRAPPING=18<<5;
    /** constant mask returned by classificationCode() designating this ability as being Alert @see Ability#classificationCode() */
    public static final int DOMAIN_ALERT=19<<5;
    /** constant mask returned by classificationCode() designating this ability as being holy protection @see Ability#classificationCode() */
    public static final int DOMAIN_HOLYPROTECTION=20<<5;
    /** constant mask returned by classificationCode() designating this ability as being healing @see Ability#classificationCode() */
    public static final int DOMAIN_HEALING=21<<5;
    /** constant mask returned by classificationCode() designating this ability as being vexing @see Ability#classificationCode() */
    public static final int DOMAIN_VEXING=22<<5;
    /** constant mask returned by classificationCode() designating this ability as being blessing @see Ability#classificationCode() */
    public static final int DOMAIN_BLESSING=23<<5;
    /** constant mask returned by classificationCode() designating this ability as being cursing @see Ability#classificationCode() */
    public static final int DOMAIN_CURSING=24<<5;
    /** constant mask returned by classificationCode() designating this ability as being evangelistic @see Ability#classificationCode() */
    public static final int DOMAIN_EVANGELISM=25<<5;
    /** constant mask returned by classificationCode() designating this ability as being moon summoning @see Ability#classificationCode() */
    public static final int DOMAIN_MOONSUMMONING=26<<5;
    /** constant mask returned by classificationCode() designating this ability as being moon altering @see Ability#classificationCode() */
    public static final int DOMAIN_MOONALTERING=27<<5;
    /** constant mask returned by classificationCode() designating this ability as being gathering skill @see Ability#classificationCode() */
    public static final int DOMAIN_GATHERINGSKILL=28<<5;
    /** constant mask returned by classificationCode() designating this ability as being crafting skill @see Ability#classificationCode() */
    public static final int DOMAIN_CRAFTINGSKILL=29<<5;
	/** constant used to mask classificationCode() designating this ability as being plant growth skil. @see Ability#classificationCode() */
    public static final int DOMAIN_PLANTGROWTH=30<<5;
	/** constant used to mask classificationCode() designating this ability as being shape shifting skil. @see Ability#classificationCode() */
    public static final int DOMAIN_SHAPE_SHIFTING=31<<5;
	/** constant used to mask classificationCode() designating this ability as being foolish skil. @see Ability#classificationCode() */
    public static final int DOMAIN_FOOLISHNESS=32<<5;
	/** constant used to mask classificationCode() designating this ability as being room ward skil. @see Ability#classificationCode() */
    public static final int DOMAIN_WARDING=33<<5;
	/** constant used to mask classificationCode() designating this ability as being death lore skil. @see Ability#classificationCode() */
    public static final int DOMAIN_DEATHLORE=34<<5;
	/** constant used to mask classificationCode() designating this ability as being weather skil. @see Ability#classificationCode() */
    public static final int DOMAIN_WEATHER=35<<5;
	/** constant used to mask classificationCode() designating this ability as being corrupting skil. @see Ability#classificationCode() */
    public static final int DOMAIN_CORRUPTION=36<<5;
	/** constant used to mask classificationCode() designating this ability as being restoring skil. @see Ability#classificationCode() */
    public static final int DOMAIN_RESTORATION=37<<5;
	/** constant used to mask classificationCode() designating this ability as being neutralizing skil. @see Ability#classificationCode() */
    public static final int DOMAIN_NEUTRALIZATION=38<<5;
	/** constant used to mask classificationCode() designating this ability as being neutralizing skil. @see Ability#classificationCode() */
    public static final int DOMAIN_CREATION=39<<5;
	/** constant used to mask classificationCode() designating this ability as being communing skil. @see Ability#classificationCode() */
    public static final int DOMAIN_COMMUNING=40<<5;
    /** constant used to mask classificationCode() designating this ability as being preserving skil. @see Ability#classificationCode() */
    public static final int DOMAIN_PRESERVING=41<<5;
    /** constant used to mask classificationCode() designating this ability as being enduring skil. @see Ability#classificationCode() */
    public static final int DOMAIN_ENDURING=42<<5;
    /** constant used to mask classificationCode() designating this ability as being plant control skil. @see Ability#classificationCode() */
    public static final int DOMAIN_PLANTCONTROL=43<<5;
    /** constant used to mask classificationCode() designating this ability as being animal affinity skil. @see Ability#classificationCode() */
    public static final int DOMAIN_ANIMALAFFINITY=44<<5;
    /** constant used to mask classificationCode() designating this ability as being deep magic skil. @see Ability#classificationCode() */
    public static final int DOMAIN_DEEPMAGIC=45<<5;
    /** constant used to mask classificationCode() designating this ability as being breeding skill. @see Ability#classificationCode() */
    public static final int DOMAIN_BREEDING=46<<5;
    /** constant used to mask classificationCode() designating this ability as being weapon use skil. @see Ability#classificationCode() */
    public static final int DOMAIN_WEAPON_USE=47<<5;
    /** constant used to mask classificationCode() designating this ability as being breeding skill. @see Ability#classificationCode() */
    public static final int DOMAIN_ROCKCONTROL=48<<5;
    /** constant used to mask classificationCode() to return only the higher order DOMAIN_* constant. @see Ability#classificationCode() */
	public static final int ALL_DOMAINS=(255<<5);
	/** array of string describtions for the DOMAIN_* constants, indexed by their values */
	public static final String[] DOMAIN_DESCS={
		"NOTHING","DIVINATION","ABJURATION","ILLUSION",
		"INVOCATION/EVOCATION","ALTERATION","TRANSMUTATION","ENCHANTMENT/CHARM",
		"CONJURATION", "ARCHON","SINGING","DANCING",
		"PLAYING","DECEPTIVE","DETRAPPING","ROPEUSING",
        "STEALING","STEALTHY","TRAPPING","ALERT",
        "HOLY_PROTECTION","HEALING","VEXING","BLESSING",
        "CURSING","EVANGELISM","MOON_SUMMONING","MOON_ALTERING",
        "GATHERING","CRAFTING","PLANT_GROWTH","SHAPE_SHIFTING",
        "FOOLISHNESS","WARDING","DEATH_LORE","WEATHER",
        "CORRUPTION","RESTORATION","NEUTRALIZATION","CREATION",
        "COMMUNING","PRESERVING","ENDURING","PLANT_CONTROL",
        "ANIMAL_AFFINITY","DEEP_MAGIC","BREEDING","WEAPON_USE",
        "ROCK_CONTROL"
	};
	/** array of string verbs for the DOMAIN_* constants, indexed by their values */
	public static final String[] DOMAIN_VERBS={
		"","Divining","Abjuring","Illusing",
		"In/Evoking","Altering","Transmuting","Enchanting",
		"Conjuring", "ArChreating","Singing", "Dancing",
		"Playing","Deceptive","DeTrapping","Rope Using",
        "Stealing","Stealthing","Trapping","Watching",
        "Divinely Protecting","Healing","Vexing","Blessing",
        "Cursing","Evangelising","Moon Summoning","Moon Altering",
        "Gathering","Crafting","Plant Growing","Shape Shifting",
        "Fool-Making","Warding","Death Animating","Weather Mastering",
        "Corrupting","Restoring","Neutralizing","Creating",
        "Communing","Preserving","Enduring","Plant Controling",
        "Animal Befriending","Deep Enchanting","Breeding","Weapon Using",
        "Rock Controling"
	};

	/** constant mask for the flags() method designating that this ability is a binding effect @see Ability#flags() */
	public static final int FLAG_BINDING=1;
	/** constant mask for the flags() method designating that this ability is a room-moving skill @see Ability#flags() */
	public static final int FLAG_MOVING=2;
	/** constant mask for the flags() method designating that this ability is a transporting skill @see Ability#flags() */
	public static final int FLAG_TRANSPORTING=4;
	/** constant mask for the flags() method designating that this ability is a weather-affecting skill @see Ability#flags() */
	public static final int FLAG_WEATHERAFFECTING=8;
	/** constant mask for the flags() method designating that this ability is a summoning skill @see Ability#flags() */
	public static final int FLAG_SUMMONING=16;
	/** constant mask for the flags() method designating that this ability is a charming effect @see Ability#flags() */
	public static final int FLAG_CHARMING=32;
	/** constant mask for the flags() method designating that this ability is a tracking-causing effect @see Ability#flags() */
	public static final int FLAG_TRACKING=64;
	/** constant mask for the flags() method designating that this ability is a heating effect @see Ability#flags() */
	public static final int FLAG_HEATING=128;
	/** constant mask for the flags() method designating that this ability is a burning effect @see Ability#flags() */
	public static final int FLAG_FIREBASED=256;
	/** constant mask for the flags() method designating that this ability is a holy or neutral effect @see Ability#flags() */
	public static final int FLAG_HOLY=512;
	/** constant mask for the flags() method designating that this ability is a unholy or neutral effect @see Ability#flags() */
	public static final int FLAG_UNHOLY=1024;
	/** constant mask for the flags() method designating that this ability is a paralyzing effect @see Ability#flags() */
	public static final int FLAG_PARALYZING=2048;
	/** constant mask for the flags() method designating that this ability may not be ordered @see Ability#flags() */
	public static final int FLAG_NOORDERING=65536;
    /** constant mask for the flags() method designating that this ability is a clan magic @see Ability#flags() */
    public static final int FLAG_CLANMAGIC=131072;
    /** constant mask for the flags() method designating that this ability is healing magic @see Ability#flags() */
    public static final int FLAG_HEALINGMAGIC=131072*2;
    /** constant mask for the flags() method designating that this ability is a freezing effect @see Ability#flags() */
    public static final int FLAG_WATERBASED=131072*4;
    /** constant mask for the flags() method designating that this ability is a washing effect @see Ability#flags() */
    public static final int FLAG_AIRBASED=131072*8;
    /** constant mask for the flags() method designating that this ability is a grounded effect @see Ability#flags() */
    public static final int FLAG_EARTHBASED=131072*16;
	

	/** array of string describtions for the FLAG_* constants, indexed by their values */
	public static final String[] FLAG_DESCS={
	    "BINDING",
	    "MOVING",
	    "TRANSPORTING",
	    "WEATHERAFFECTING",
	    "SUMMONING",
	    "CHARMING",
	    "TRACKING",
	    "HEATING",
	    "BURNING",
	    "HOLY",
	    "UNHOLY",
	    "PARALYZING",
	    "NOORDERING",
        "CLANMAGIC",
	    "HEALING",
        "FREEZING",
        "ELECTROCUTING",
        "ACIDIZING"
	};

	/* constant for the abstractQuality and other methods.  Means that this skill would not make the target happy. @see Ability#abstractQuality()*/
	public static final int QUALITY_MALICIOUS=0;
	/* constant for the abstractQuality and other methods.  Means that this skill would not make the target either happy or unhappy. @see Ability#abstractQuality()*/
	public static final int QUALITY_INDIFFERENT=1;
	/* constant for the abstractQuality and other methods.  Means that this skill targets the invoker, and is harmless, but only useful in qualified situations. @see Ability#abstractQuality()*/
	public static final int QUALITY_OK_SELF=2;
	/* constant for the abstractQuality and other methods.  Means that this skill targets the invoker or others, and is harmless, but only useful in qualified situations. @see Ability#abstractQuality()*/
	public static final int QUALITY_OK_OTHERS=3;
	/* constant for the abstractQuality and other methods.  Means that this skill targets the invoker, and is always beneficial. @see Ability#abstractQuality()*/
	public static final int QUALITY_BENEFICIAL_SELF=4;
	/* constant for the abstractQuality and other methods.  Means that this skill targets the invoker or others, and is always beneficial. @see Ability#abstractQuality()*/
	public static final int QUALITY_BENEFICIAL_OTHERS=5;
	/* descriptive list of the QUALITY_ flags. @see Ability#abstractQuality() */
	public static final String[] QUALITY_DESCS={"MALICIOUS","INDIFFERENT","OK_SELF","OK_OTHERS","BENEFICIAL_SELF","BENEFICIAL_OTHERS"};
	
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect mobs @see Ability#canAffectCode() */
	public static final int CAN_MOBS=1;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect items @see Ability#canAffectCode() */
	public static final int CAN_ITEMS=2;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect areas @see Ability#canAffectCode() */
	public static final int CAN_AREAS=4;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect rooms @see Ability#canAffectCode() */
	public static final int CAN_ROOMS=8;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect exits @see Ability#canAffectCode() */
	public static final int CAN_EXITS=16;
}
