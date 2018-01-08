package com.planet_ink.coffee_mud.Abilities.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
 * The basic interface for all skills, spells, chants, prayers,
 * and properties. Abilities are listed in a MOBs abilities
 * list, and may be listed on *any* Environmental objects effects
 * list.  Often the same class files act in both capacities.
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
 * @see com.planet_ink.coffee_mud.core.interfaces.Affectable#addEffect(Ability)
 */

public interface Ability extends Environmental
{
	/** Constant shortcut for setting the ticks remaining on the skill to basically be endless. */
	public static final int TICKS_FOREVER		= Integer.MAX_VALUE-1000;
	/** Constant shortcut for setting the ticks remaining on the skill to basically be almost endless. */
	public static final int TICKS_ALMOST_FOREVER= Integer.MAX_VALUE/2;

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
	 * @return Ability.QUALITY_* constant classification
	 */
	public int abstractQuality();

	/**
	 * Identical to abstractQuality() method, but returns
	 * a quality based on how the skill or spell behaves
	 * as a spell effect on a weapon or on armor.  This
	 * helps decide whether this skill is appropriate in
	 * those roles.  These constants are defined in the
	 * Ability interface and called QUALITY_*
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()
	 * @see Ability
	 * @return Ability.QUALITY_* constant classification
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
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()
	 * @param mob the person to use the skill
	 * @param target the potential target -- may be invoker
	 * @return Ability.QUALITY_* constant classification
	 */
	public int castingQuality(MOB mob, Physical target);

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
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#setInvoker(MOB)
	 * @return the invoker mob
	 */
	public MOB invoker();

	/**
	 * Sets or changes the mob that invoked or initated the
	 * skill or effect.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#invoker()
	 * @param mob the invoker mob
	 */
	public void setInvoker(MOB mob);

	/**
	 * Returns a reference to the room, item, mob, or exit
	 * that is currently being affected by this skill. Does
	 * not apply to non-affecting skills like kick, but more
	 * to auto-invoking skills and spells that affect people
	 * like sleep.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#setAffectedOne(Physical)
	 * @return the room, mob, or item being affected
	 */
	public Physical affecting();

	/**
	 * Sets a reference to the room, item, mob, or exit
	 * that is currently being affected by this skill. Does
	 * not apply to non-affecting skills like kick, but more
	 * to auto-invoking skills and spells that affect people
	 * like sleep, and especially to properties.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#affecting()
	 * @param P the room, mob, or item being affected
	 */
	public void setAffectedOne(Physical P);

	/**
	 * Returns whether this skills triggerStrings should be
	 * places in the master list of commands, unlike
	 * properties or other hidden skills.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#triggerStrings()
	 * @return whether to add the triggerString to the commands list
	 */
	public boolean putInCommandlist();

	/**
	 * Returns cost of training up this skill, for the given user.
	 * @param mob the potential caster
	 * @return a pair, with the number of the cost type, and the cost type
	 */
	public ExpertiseLibrary.SkillCost getTrainingCost(MOB mob);

	/**
	 * A set of the command strings the user types to access
	 * this command when it is listed as a skill ability.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#putInCommandlist()
	 * @return an array of command invoking string words
	 */
	public String[] triggerStrings();

	/**
	 * Returns the number of actions required to completely
	 * activate this skill. A value of 0.0 means invoke
	 * instantly.  This method only applies when the invoker
	 * is not in combat.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#combatCastingTime(MOB, List)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#checkedCastingCost(MOB, List)
	 * @param mob the potential caster
	 * @param commands the potential command set
	 * @return the number of player free actions required to do this
	 */
	public double castingTime(final MOB mob, final List<String> commands);

	/**
	 * Returns the number of actions required to completely
	 * activate this skill. A value of 0.0 means invoke
	 * instantly.  This method only applies when the invoker
	 * is in combat.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#castingTime(MOB, List)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#checkedCastingCost(MOB, List)
	 * @param mob the potential caster
	 * @param commands the potential command set
	 * @return the number of player free actions required to do this
	 */
	public double combatCastingTime(final MOB mob, final List<String> commands);

	/**
	 * Returns the number of actions required to completely
	 * activate this skill. A value of 0.0 means invoke
	 * instantly.  This method should return the correct time depending
	 * on the nature of the skill, and whether the user is in combat.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#castingTime(MOB, List)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#combatCastingTime(MOB, List)
	 * @param mob the potential caster
	 * @param commands the potential command set
	 * @return the number of player free actions required to do this
	 */
	public double checkedCastingCost(final MOB mob, final List<String> commands);

	/**
	 * This method is only called when the mob invoking this skill
	 * does not have enough actions to complete it immediately.  The
	 * method is called when the command is entered, and every second
	 * afterwards until the invoker has enough actions to complete it.
	 * At completion time, invoke is called.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#invoke(MOB, List, Physical, boolean, int)
	 * @param mob the player or mob invoking the skill
	 * @param commands the parameters entered for the skill (minus trigger word)
	 * @param givenTarget null, unless being auto-invoked. Represents an override target.
	 * @param auto false if player enters command, true if system invokes the command
	 * @param asLevel -1, unless being auto-invoked, when it is the level to invoke it at.
	 * @param secondsElapsed 0 at first, and increments every second
	 * @param actionsRemaining number of free actions the player is defficient.
	 * @return whether the skill should be allowed to invoke.  false cancels altogether.
	 */
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining);

	/**
	 * This method is called when a player or the system invokes this skill,
	 * casts this spell, etc.
	 * Calls the more complete invoke method without an empty command strings vector
	 * unless target is non-null, in which case the vector will contain the name
	 * of the target.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#invoke(MOB, List, Physical, boolean, int)
	 * @param mob the player or mob invoking the skill
	 * @param target null, unless being auto-invoked. Represents an override target.
	 * @param auto false if player enters command, true if system invokes the command
	 * @param asLevel -1, unless being auto-invoked, when it is the level to invoke it at.
	 * @return whether the skill successfully invoked.
	 */
	public boolean invoke(MOB mob, Physical target, boolean auto, int asLevel);

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
	public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel);

	/**
	 * If this skill is uninvokable, this method will uninvoke it, remove it
	 * as an effect on the target, and shut it down.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canBeUninvoked()
	 */
	public void unInvoke();

	/**
	 * This method is used to modify the behavior of the affectPhyStats,
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
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#makeNonUninvokable()
	 * @return whether this skill can be uninvoked.
	 */
	public boolean canBeUninvoked();

	/**
	 * Designates that this skill should never be uninvoked.  Designates that
	 * this skill is a permanent property of the object it is affecting.
	 * Is normally called by addNonUninvokableAffect
	 * @see com.planet_ink.coffee_mud.core.interfaces.Affectable#addNonUninvokableEffect(Ability)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canBeUninvoked()
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
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#isAutoInvoked()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#isNowAnAutoEffect()
	 * @param mob the player or npc mob who has this ability
	 * @param force if the skill has default-off settings, this overrides to ON
	 * @return whether the ability autoinvoked correctly
	 */
	public boolean autoInvocation(MOB mob, boolean force);

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
	 * Ability.COST_ALL means to use all of the mana, movement,
	 * hit points.  A value of Ability.COST_PCT and up
	 * represents a percentage of the cost.
	 * The values in this method MUST be accompanied by properly
	 * set usageType() bitmap.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageType()
	 * @see Ability
	 * @param mob the invoker of the skill
	 * @param ignoreClassOverride whether to ignore Class Overrides
	 * @return an array of costs, indexed by Ability.USAGEINDEX_*
	 */
	public int[] usageCost(MOB mob, boolean ignoreClassOverride);

	/**
	 * Returns a bitmap made up of constants defined by
	 * Ability.USAGE_*.  It lets the system know which of the
	 * values in the usageCost(MOB,boolean) method are relevant.  It
	 * determines whether this skill requires mana, movement,
	 * hit points, or some combination of the three.
	 * @see Ability
	 * @return a bitmap defined by Ability.USAGE_*
	 */
	public int usageType();

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
	 * @return a list of local path names
	 */
	public List<String> externalFiles();

	/**
	 * Returns whether the given teacher mob is able and allowed to teach
	 * this skill to the given student.  Error messages should be directed
	 * to both.  This method should focus exclusively on the qualifications
	 * of the teacher.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canBeLearnedBy(MOB, MOB)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#teach(MOB, MOB)
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
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#practice(MOB, MOB)
	 * @param teacher the potential practice teacher of this skill
	 * @param student the potential practicer of this skil
	 * @return whether the teacher and student are capable of practicing together
	 */
	public boolean canBePracticedBy(MOB teacher, MOB student);

	/**
	 * Returns whether the given student mob is able and allowed to learn
	 * this skill from the given teacher.  Error messages should be directed
	 * to both.  This method should focus exclusively on the qualifications
	 * of the student, and the teacher is optional.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canBeTaughtBy(MOB, MOB)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#teach(MOB, MOB)
	 * @param teacher the potential teacher of this skill, may be null
	 * @param student the potential learner of this skill, may NOT be null
	 * @return whether the student can learn the skill from the teacher
	 */
	public boolean canBeLearnedBy(MOB teacher, MOB student);

	/**
	 * Teaches this skill to the student mob, presumably from the teacher mob.
	 * This method assumes that both the teacher and student are authorized.
	 * Authorization comes from canBeTaughtBy and canBeLearnedBy
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canBeTaughtBy(MOB, MOB)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canBeLearnedBy(MOB, MOB)
	 * @param teacher the teacher of this skill
	 * @param student the learner of this skill
	 */
	public void teach(MOB teacher, MOB student);

	/**
	 * Causes the student to practice this skill, presumably with the teacher mob.
	 * This method assumes that both the teacher and student are authorized.
	 * Authorization comes from canBePracticedBy
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canBePracticedBy(MOB, MOB)
	 * @param teacher the teacher of this skill
	 * @param student the practicer of this skill
	 */
	public void practice(MOB teacher, MOB student);

	/**
	 * Returns a string describing the requirements and qualifications that
	 * are required to learn this skill.
	 * @param mob the mob whose requirements must be tested
	 * @return a description of the learning requirements of this skill.
	 */
	public String requirements(MOB mob);

	/**
	 * Returns whether, when used as a skill, this ability can target itself
	 * at the given object.  This method derives its answer from the protected
	 * integer method canTargetCode()
	 * @see com.planet_ink.coffee_mud.Abilities.StdAbility#canTargetCode()
	 * @param P the potential target of this skill
	 * @return whether E is a valid target
	 */
	public boolean canTarget(Physical P);
	/**
	 * Returns whether, when used as a property/effect, this ability can affect
	 * the given object.  This method derives its answer from the protected
	 * integer method canAffectCode()
	 * @see com.planet_ink.coffee_mud.Abilities.StdAbility#canAffectCode()
	 * @param P the potential object to have this as a property/effect
	 * @return whether E is a valid object to have this as a property/effect
	 */
	public boolean canAffect(Physical P);

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
	 * Adds THIS Ability object as an effect on the given affected target, and
	 * sets it to unInvoke after the given number of ticks.  This method
	 * intelligently determines whether the target is already going to provide
	 * tick events to the Ability (like a mob) or whether the system should
	 * create a tick event for this ability (like on rooms or items).
	 * @param invoker the invoker of the ability
	 * @param affected the object to be affected by this ability
	 * @param tickTime the number of ticks to keep the ability ticking.
	 */
	public void startTickDown(MOB invoker, Physical affected, int tickTime);

	/**
	 * Returns a number from 0-100 representing the percent of proficiency
	 * the mob or player who has this instance in their Abilities list has in
	 * this skill.  Is also used for other miscellaneous purposes by other non-skill
	 * Abilities, such as Drowning.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#setProficiency(int)
	 * @return the proficiency from 0-100
	 */
	public int proficiency();

	/**
	 * Sets a number from 0-100 representing the percent of proficiency
	 * the mob or player who has this instance in their Abilities list has in
	 * this skill.  Is also used for other miscellaneous purposes by other non-skill
	 * Abilities, such as Drowning.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#proficiency()
	 * @param newProficiency the proficiency from 0-100
	 */
	public void setProficiency(int newProficiency);

	/**
	 * Returns whether the given mob passes their proficiency check in this skill
	 * at this time.  Will accept a numeric adjustment, positive or negative, to
	 * their base proficiency.  Will also accept an auto parameter, which forces
	 * this method to always return true.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#proficiency()
	 * @param mob the mob whose proficiency to check
	 * @param adjustment a positive or negative adjustment to the mobs base proficiency
	 * @param auto if true, this method returns true always
	 * @return whether the mob passes their proficiency check
	 */
	public boolean proficiencyCheck(MOB mob, int adjustment, boolean auto);

	/**
	 * This method should, occasionally, add to the proficiency the given mob
	 * has in this skill.  There are no guarantees about how often this method
	 * is called, but presumably it is called when the mob is doing something
	 * that exercises this skill in some way.  How often proficiency is effected
	 * by this method is up to the method, but is typically time-based, and based
	 * on intelligence and other factors.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#proficiency()
	 * @param mob the mob whose proficiency in this skill to possibly advance
	 * @param adjustment up or down to the chance of becoming more proficient
	 */
	public void helpProficiency(MOB mob, int adjustment);

	/** usageType() constant meaning that the skill is free @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageType()*/
	public final static int USAGE_NADA=0;
	/** usageType() constant meaning that the skill costs mana @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageType()*/
	public final static int USAGE_MANA=1;
	/** usageType() constant meaning that the skill costs movement @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageType()*/
	public final static int USAGE_MOVEMENT=2;
	/** usageType() constant meaning that the skill costs hp @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageType()*/
	public final static int USAGE_HITPOINTS=4;
	/** constant descriptions for the USAGE_* values @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageType() */
	public final static String[] USAGE_DESCS={"MANA","MOVEMENT","HITPOINTS"};

	/** index into internal cache used by usageCost(MOB,boolean) @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int CACHEINDEX_NORMAL=0;
	/** index into internal cache used by usageCost(MOB,boolean) @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int CACHEINDEX_CLASSLESS=1;
	/** index into internal cache used by usageCost(MOB,boolean) @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int CACHEINDEX_EXPERTISE=2;
	/** index into internal cache used by usageCost(MOB,boolean) @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int CACHEINDEX_TOTAL=3;

	/** Constant for overrideMana to denote that the skill uses all of a players mana @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int COST_ALL=Integer.MAX_VALUE;
	/** Constant for overrideMana to denote that the skill uses a % of a players mana @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int COST_PCT=Integer.MAX_VALUE-100;
	/** Constant for overrideMana to denote that the skill uses base mana formula @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int COST_NORMAL=-1;

	/** index into usageCost(MOB,boolean) array for the amount of mana this skill costs @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int USAGEINDEX_MANA=0;
	/** index into usageCost(MOB,boolean) array for the amount of moves this skill costs @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int USAGEINDEX_MOVEMENT=1;
	/** index into usageCost(MOB,boolean) array for the amount of hp this skill costs @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#usageCost(MOB,boolean) */
	public final static int USAGEINDEX_HITPOINTS=2;

	/** constant returned by classificationCode() designating this ability as a Skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_SKILL=0;
	/** constant returned by classificationCode() designating this ability as a Spell @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_SPELL=1;
	/** constant returned by classificationCode() designating this ability as a Prayer @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_PRAYER=2;
	/** constant returned by classificationCode() designating this ability as a Song @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_SONG=3;
	/** constant returned by classificationCode() designating this ability as a Trap @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_TRAP=4;
	/** constant returned by classificationCode() designating this ability as a Property @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_PROPERTY=5;
	/** constant returned by classificationCode() designating this ability as a Thief Skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_THIEF_SKILL=6;
	/** constant returned by classificationCode() designating this ability as a Language @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_LANGUAGE=7;
	/** constant returned by classificationCode() designating this ability as a Chant @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_CHANT=8;
	/** constant returned by classificationCode() designating this ability as a Common Skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_COMMON_SKILL=9;
	/** constant returned by classificationCode() designating this ability as a Disease @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_DISEASE=10;
	/** constant returned by classificationCode() designating this ability as a Poison @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_POISON=11;
	/** constant returned by classificationCode() designating this ability as a Super Power @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_SUPERPOWER=12;
	/** constant returned by classificationCode() designating this ability as a Archon ONly Skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ACODE_TECH=13;
	/** constant returned by classificationCode() designating this ability as a Tech-Only Skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ALL_ACODES=31;
	/** array of string describtions for the ACODE_* constants, indexed by their values */
	public static final String[] ACODE_DESCS=
	{
		"SKILL","SPELL","PRAYER","SONG","TRAP","PROPERTY",
		"THIEF SKILL","LANGUAGE","CHANT","COMMON SKILL",
		"DISEASE","POISON","SUPERPOWER","ARCHON SKILL",
		"TECH SKILL"
	};
	/** array of string describtions for the ACODE_* constants, indexed by their values, with _ where spaces would be */
	public static final String[] ACODE_DESCS_= CMStrings.replaceInAll(ACODE_DESCS.clone()," ","_");

	/** constant mask returned by classificationCode() designating this ability as being Divination @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_DIVINATION=1<<5;
	/** constant mask returned by classificationCode() designating this ability as being Abjuration @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ABJURATION=2<<5;
	/** constant mask returned by classificationCode() designating this ability as being Illusion @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ILLUSION=3<<5;
	/** constant mask returned by classificationCode() designating this ability as being Evocation @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_EVOCATION=4<<5;
	/** constant mask returned by classificationCode() designating this ability as being Alteration @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ALTERATION=5<<5;
	/** constant mask returned by classificationCode() designating this ability as being Transmutation @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_TRANSMUTATION=6<<5;
	/** constant mask returned by classificationCode() designating this ability as being Enchantment @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ENCHANTMENT=7<<5;
	/** constant mask returned by classificationCode() designating this ability as being Conjuration @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_CONJURATION=8<<5;
	/** constant mask returned by classificationCode() designating this ability as being Archon @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ARCHON=9<<5;
	/** constant mask returned by classificationCode() designating this ability as being Singing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_SINGING=10<<5;
	/** constant mask returned by classificationCode() designating this ability as being Dancing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_DANCING=11<<5;
	/** constant mask returned by classificationCode() designating this ability as being Playing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_PLAYING=12<<5;
	/** constant mask returned by classificationCode() designating this ability as being Deceptive @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_DECEPTIVE=13<<5;
	/** constant mask returned by classificationCode() designating this ability as being Detrapping @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_DETRAP=14<<5;
	/** constant mask returned by classificationCode() designating this ability as being RopeUsing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_BINDING=15<<5;
	/** constant mask returned by classificationCode() designating this ability as being Stealing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_STEALING=16<<5;
	/** constant mask returned by classificationCode() designating this ability as being Stealthy @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_STEALTHY=17<<5;
	/** constant mask returned by classificationCode() designating this ability as being Trapping @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_TRAPPING=18<<5;
	/** constant mask returned by classificationCode() designating this ability as being Alert @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ALERT=19<<5;
	/** constant mask returned by classificationCode() designating this ability as being holy protection @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_HOLYPROTECTION=20<<5;
	/** constant mask returned by classificationCode() designating this ability as being healing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_HEALING=21<<5;
	/** constant mask returned by classificationCode() designating this ability as being vexing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_VEXING=22<<5;
	/** constant mask returned by classificationCode() designating this ability as being blessing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_BLESSING=23<<5;
	/** constant mask returned by classificationCode() designating this ability as being cursing @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_CURSING=24<<5;
	/** constant mask returned by classificationCode() designating this ability as being evangelistic @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_EVANGELISM=25<<5;
	/** constant mask returned by classificationCode() designating this ability as being moon summoning @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_MOONSUMMONING=26<<5;
	/** constant mask returned by classificationCode() designating this ability as being moon altering @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_MOONALTERING=27<<5;
	/** constant mask returned by classificationCode() designating this ability as being gathering skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_GATHERINGSKILL=28<<5;
	/** constant mask returned by classificationCode() designating this ability as being crafting skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_CRAFTINGSKILL=29<<5;
	/** constant used to mask classificationCode() designating this ability as being plant growth skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_PLANTGROWTH=30<<5;
	/** constant used to mask classificationCode() designating this ability as being shape shifting skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_SHAPE_SHIFTING=31<<5;
	/** constant used to mask classificationCode() designating this ability as being foolish skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_FOOLISHNESS=32<<5;
	/** constant used to mask classificationCode() designating this ability as being room ward skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_WARDING=33<<5;
	/** constant used to mask classificationCode() designating this ability as being death lore skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_DEATHLORE=34<<5;
	/** constant used to mask classificationCode() designating this ability as being weather skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_WEATHER_MASTERY=35<<5;
	/** constant used to mask classificationCode() designating this ability as being corrupting skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_CORRUPTION=36<<5;
	/** constant used to mask classificationCode() designating this ability as being restoring skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_RESTORATION=37<<5;
	/** constant used to mask classificationCode() designating this ability as being neutralizing skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_NEUTRALIZATION=38<<5;
	/** constant used to mask classificationCode() designating this ability as being neutralizing skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_CREATION=39<<5;
	/** constant used to mask classificationCode() designating this ability as being communing skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_COMMUNING=40<<5;
	/** constant used to mask classificationCode() designating this ability as being preserving skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_PRESERVING=41<<5;
	/** constant used to mask classificationCode() designating this ability as being enduring skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ENDURING=42<<5;
	/** constant used to mask classificationCode() designating this ability as being plant control skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_PLANTCONTROL=43<<5;
	/** constant used to mask classificationCode() designating this ability as being animal affinity skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ANIMALAFFINITY=44<<5;
	/** constant used to mask classificationCode() designating this ability as being deep magic skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_DEEPMAGIC=45<<5;
	/** constant used to mask classificationCode() designating this ability as being breeding skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_BREEDING=46<<5;
	/** constant used to mask classificationCode() designating this ability as being weapon use skil. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_WEAPON_USE=47<<5;
	/** constant used to mask classificationCode() designating this ability as being breeding skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ROCKCONTROL=48<<5;
	/** constant used to mask classificationCode() designating this ability as being kicking skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_KICKING=49<<5;
	/** constant used to mask classificationCode() designating this ability as being punching skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_PUNCHING=50<<5;
	/** constant used to mask classificationCode() designating this ability as being grappling skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_GRAPPLING=51<<5;
	/** constant used to mask classificationCode() designating this ability as being calligraphy skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_CALLIGRAPHY=52<<5;
	/** constant used to mask classificationCode() designating this ability as being poisoning skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_POISONING=53<<5;
	/** constant used to mask classificationCode() designating this ability as being arcane lore skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ARCANELORE=54<<5;
	/** constant used to mask classificationCode() designating this ability as being acrobatic skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ACROBATIC=55<<5;
	/** constant used to mask classificationCode() designating this ability as being amorous skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_INFLUENTIAL=56<<5;
	/** constant used to mask classificationCode() designating this ability as being street smarts skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_STREETSMARTS=57<<5;
	/** constant used to mask classificationCode() designating this ability as being nature lore skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_NATURELORE=58<<5;
	/** constant used to mask classificationCode() designating this ability as being dirty fighting skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_DIRTYFIGHTING=59<<5;
	/** constant used to mask classificationCode() designating this ability as being combat lore skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_COMBATLORE=60<<5;
	/** constant used to mask classificationCode() designating this ability as being combat fluidity skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_COMBATFLUIDITY=61<<5;
	/** constant used to mask classificationCode() designating this ability as being evasive skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_EVASIVE=62<<5;
	/** constant used to mask classificationCode() designating this ability as being martial lore skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_MARTIALLORE=63<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_RACIALABILITY=64<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ARTISTIC=65<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ANATOMY=66<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_ARMORUSE=67<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_SHIELDUSE=68<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_CRIMINAL=69<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_LEGAL=70<<5;
	/** constant used to mask classificationCode() designating this ability as being racial ability skill. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_FITNESS=71<<5;
	/** constant used to mask classificationCode() to return only the higher order DOMAIN_* constant. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_THEATRE=72<<5;
	/** constant mask returned by classificationCode() designating this ability as being a building skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_BUILDINGSKILL=73<<5;
	/** constant mask returned by classificationCode() designating this ability as being a watery chant @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_WATERLORE=74<<5;
	/** constant mask returned by classificationCode() designating this ability as being a watery chant @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_WATERCONTROL=75<<5;
	/** constant mask returned by classificationCode() designating this ability as being a sea travel skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_SEATRAVEL=76<<5;
	/** constant mask returned by classificationCode() designating this ability as being a studying skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_EDUCATIONLORE=77<<5;
	/** constant mask returned by classificationCode() designating this ability as being a studying skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int DOMAIN_EPICUREAN=78<<5;
	/** constant used to mask classificationCode() to return only the higher order DOMAIN_* constant. @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#classificationCode() */
	public static final int ALL_DOMAINS=(255<<5);
	/** array of string describtions for the DOMAIN_* constants, indexed by their values */
	public static final String[] DOMAIN_DESCS={
		"NOTHING","DIVINATION","ABJURATION","ILLUSION",
		"INVOCATION/EVOCATION","ALTERATION","TRANSMUTATION","ENCHANTMENT/CHARM",
		"CONJURATION", "ARCHON","SINGING","DANCING",
		"PLAYING","DECEPTIVE","FIND/REMOVE_TRAPS","BINDING",
		"STEALING","STEALTHY","TRAPPING","ALERT",
		"HOLY_PROTECTION","HEALING","VEXING","BLESSING",
		"CURSING","EVANGELISM","MOON_SUMMONING","MOON_ALTERING",
		"GATHERING","CRAFTING","PLANT_GROWTH","SHAPE_SHIFTING",
		"FOOLISHNESS","WARDING","DEATH_LORE","WEATHER_MASTERY",
		"CORRUPTION","RESTORATION","NEUTRALIZATION","CREATION",
		"COMMUNING","PRESERVING","ENDURING","PLANT_CONTROL",
		"ANIMAL_AFFINITY","DEEP_MAGIC","BREEDING","WEAPON_USE",
		"ROCK_CONTROL","KICKING","PUNCHING","GRAPPLING",
		"CALLIGRAPHY","POISONING","ARCANE_LORE","ACROBATIC",
		"INFLUENTIAL","STREET_SMARTS","NATURE_LORE","DIRTY_FIGHTING",
		"COMBAT_LORE","COMBAT_FLUIDITY","EVASIVE","MARTIAL_LORE",
		"RACIAL_ABILITY","ARTISTIC","ANATOMY","ARMOR_USE",
		"SHIELD_USE","CRIMINAL","LEGAL","FITNESS","THEATRE",
		"BUILDING","WATER_LORE","WATER_CONTROL","SEA_TRAVEL",
		"EDUCATION_LORE","EPICUREAN"
	};
	/** array of string verbs for the DOMAIN_* constants, indexed by their values */
	public static final String[] DOMAIN_VERBS={
		"","Divining","Abjuring","Illusing",
		"In/Evoking","Altering","Transmuting","Enchanting",
		"Conjuring", "ArChreating","Singing", "Dancing",
		"Playing","Deceptive","DeTrapping","Binding",
		"Stealing","Stealthing","Trapping","Watching",
		"Divinely Protecting","Healing","Vexing","Blessing",
		"Cursing","Evangelising","Moon Summoning","Moon Altering",
		"Gathering","Crafting","Plant Growing","Shape Shifting",
		"Fool-Making","Warding","Death Animating","Weather Mastering",
		"Corrupting","Restoring","Neutralizing","Creating",
		"Communing","Preserving","Enduring","Plant Controling",
		"Animal Befriending","Deep Enchanting","Breeding","Weapon Using",
		"Rock Controling","Kicking","Punching","Grappling",
		"Caligraphing","Poisoning","Arcane Loreing","Acrobatisizing",
		"Influencing","Street Knowing","Nature Loring","Dirty Fighting",
		"Combat Loring","Combat Fluidisizing","Evading","Matrial Loring",
		"Racial Knowing","Artmaking","Anatomy","Armor Using",
		"Shield Using","Crimemaking","Legalizing","Fitness","Acting",
		"Building","Water Lore","Water Control","Sea Travel","Indulging"
	};

	/** constant descriptions for the minRange()/maxRange() values @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#minRange() */
	public static final String[] RANGE_CHOICES={"MELEE","CLOSE","SHORT","MEDIUM","LONG","LONGER","LONGERSTILL","VERYLONG","EXTREMELYLONG","INFINITE"};

	/** constant mask for the flags() method designating that this ability is a binding effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_BINDING=1;
	/** constant mask for the flags() method designating that this ability is a room-moving skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_MOVING=2;
	/** constant mask for the flags() method designating that this ability is a transporting skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_TRANSPORTING=4;
	/** constant mask for the flags() method designating that this ability is a weather-affecting skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_WEATHERAFFECTING=8;
	/** constant mask for the flags() method designating that this ability is a summoning skill @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_SUMMONING=16;
	/** constant mask for the flags() method designating that this ability is a charming effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_CHARMING=32;
	/** constant mask for the flags() method designating that this ability is a tracking-causing effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_TRACKING=64;
	/** constant mask for the flags() method designating that this ability is a heating effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_HEATING=128;
	/** constant mask for the flags() method designating that this ability is a burning effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_FIREBASED=256;
	/** constant mask for the flags() method designating that this ability is a holy effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_HOLY=512;
	/** constant mask for the flags() method designating that this ability is a unholy  effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_UNHOLY=1024;
	/** constant mask for the flags() method designating that this ability is a neutral effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_NEUTRAL=1024|512;
	/** constant mask for the flags() method designating that this ability is a paralyzing effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_PARALYZING=2048;
	/** constant mask for the flags() method designating that this ability may not be ordered @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_NOORDERING=4096;
	/** constant mask for the flags() method designating that this ability is a clan magic @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_CLANMAGIC=8192;
	/** constant mask for the flags() method designating that this ability is healing magic @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_HEALINGMAGIC=16384;
	/** constant mask for the flags() method designating that this ability is a freezing effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_WATERBASED=32768;
	/** constant mask for the flags() method designating that this ability is a washing effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_AIRBASED=65536;
	/** constant mask for the flags() method designating that this ability is a grounded effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_EARTHBASED=131072;
	/** constant mask for the flags() method designating that this ability is an intoxicating effect @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_INTOXICATING=262144;
	/** constant mask for the flags() method designating that this ability adjusts char/phy/base stats @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_ADJUSTER=524288;
	/** constant mask for the flags() method designating that this ability resists/saves @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_RESISTER=1048576;
	/** constant mask for the flags() method designating that this ability blocks certain negative affects @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_IMMUNER=2097152;
	/** constant mask for the flags() method designating that this ability blocks getting and other mundane actions @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_ZAPPER=4194304;
	/** constant mask for the flags() method designating that this ability casts some other spell/ability @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_CASTER=8388608;
	/** constant mask for the flags() method designating that this ability grants another spell/ability @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_ENABLER=16777216;
	/** constant mask for the flags() method designating that the thing with this ability cant be learned as a recipe @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_UNCRAFTABLE=33554432L;
	/** constant mask for the flags() method designating that the thing with this ability might kill you @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_POTENTIALLY_DEADLY=67108864L;
	/** constant mask for the flags() method designating that the thing with this ability might kill you @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_AGGROFYING=134217728L;
	/** constant mask for the flags() method designating that the thing with this ability alters the tides @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags() */
	public static final long FLAG_TIDEALTERING=268435456L;

	/** array of string describtions for the FLAG_* constants, indexed by their values */
	public static final String[] FLAG_DESCS=
	{
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
		"ACIDIZING",
		"INTOXICATING",
		"ADJUSTER",
		"RESISTER",
		"IMMUNER",
		"ZAPPER",
		"CASTER",
		"ENABLER",
		"UNCRAFTABLE",
		"DEADLY",
		"AGGROING",
		"TIDEALTERING"
	};

	/** constant for the abstractQuality and other methods.  Means that this skill would not make the target happy. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()}*/
	public static final int QUALITY_MALICIOUS=0;
	/** constant for the abstractQuality and other methods.  Means that this skill would not make the target either happy or unhappy. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()}*/
	public static final int QUALITY_INDIFFERENT=1;
	/** constant for the abstractQuality and other methods.  Means that this skill targets the invoker, and is harmless, but only useful in qualified situations. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()}*/
	public static final int QUALITY_OK_SELF=2;
	/** constant for the abstractQuality and other methods.  Means that this skill targets the invoker or others, and is harmless, but only useful in qualified situations. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()}*/
	public static final int QUALITY_OK_OTHERS=3;
	/** constant for the abstractQuality and other methods.  Means that this skill targets the invoker, and is always beneficial. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()}*/
	public static final int QUALITY_BENEFICIAL_SELF=4;
	/** constant for the abstractQuality and other methods.  Means that this skill targets the invoker or others, and is always beneficial. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()}*/
	public static final int QUALITY_BENEFICIAL_OTHERS=5;
	/** descriptive list of the QUALITY_ flags. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abstractQuality()}*/
	public static final String[] QUALITY_DESCS={"MALICIOUS","INDIFFERENT","OK_SELF","OK_OTHERS","BENEFICIAL_SELF","BENEFICIAL_OTHERS"};

	/** descriptive list of the CAN_ flags. see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canAffect(int)} */
	public static final String[] CAN_DESCS={"MOBS","ITEMS","AREAS","ROOMS","EXITS"};
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect mobs see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canAffect(int)} */
	public static final int CAN_MOBS=1;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect items see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canAffect(int)} */
	public static final int CAN_ITEMS=2;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect areas see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canAffect(int)} */
	public static final int CAN_AREAS=4;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect rooms see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canAffect(int)} */
	public static final int CAN_ROOMS=8;
	/** constant mask for the canTargetCode() and canAffectCode() methods.  Means it can target/affect exits see {@link com.planet_ink.coffee_mud.Abilities.interfaces.Ability#canAffect(int)} */
	public static final int CAN_EXITS=16;
}
