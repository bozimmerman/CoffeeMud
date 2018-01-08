package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.*;

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
 * An object to access and change fields representing the varias aspects of a MOB
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
 * @author Bo Zimmerman
 *
 */
public interface CharStats extends CMCommon, Modifiable
{
	/** stat constant for strength */
	public static final int VALUE_ALLSTATS_DEFAULT=10;

	/** stat constant for strength */
	public static final int STAT_STRENGTH=0;
	/** stat constant for intelligence */
	public static final int STAT_INTELLIGENCE=1;
	/** stat constant for dexterity */
	public static final int STAT_DEXTERITY=2;
	/** stat constant for constitution */
	public static final int STAT_CONSTITUTION=3;
	/** stat constant for  constitution*/
	public static final int STAT_CHARISMA=4;
	/** stat constant for  charisma*/
	public static final int STAT_WISDOM=5;
	/** stat constant for  gender*/
	public static final int STAT_GENDER=6;
	/** stat constant for  save vs paralysis*/
	public static final int STAT_SAVE_PARALYSIS=7;
	/** stat constant for  save vs fire*/
	public static final int STAT_SAVE_FIRE=8;
	/** stat constant for  save vs cold*/
	public static final int STAT_SAVE_COLD=9;
	/** stat constant for  save vs water*/
	public static final int STAT_SAVE_WATER=10;
	/** stat constant for  save vs gas*/
	public static final int STAT_SAVE_GAS=11;
	/** stat constant for  save vs mind attacks/illusion*/
	public static final int STAT_SAVE_MIND=12;
	/** stat constant for  save vs somethingelse*/
	public static final int STAT_SAVE_GENERAL=13;
	/** stat constant for  save vs humiliation*/
	public static final int STAT_SAVE_JUSTICE=14;
	/** stat constant for  save vs acid*/
	public static final int STAT_SAVE_ACID=15;
	/** stat constant for  save vs electricity*/
	public static final int STAT_SAVE_ELECTRIC=16;
	/** stat constant for  save vs poison*/
	public static final int STAT_SAVE_POISON=17;
	/** stat constant for  save vs undead attacks*/
	public static final int STAT_SAVE_UNDEAD=18;
	/** stat constant for  save vs magic*/
	public static final int STAT_SAVE_MAGIC=19;
	/** stat constant for  save vs disease*/
	public static final int STAT_SAVE_DISEASE=20;
	/** stat constant for  save vs traps*/
	public static final int STAT_SAVE_TRAPS=21;
	/** stat constant for  max strength adjustment*/
	public static final int STAT_MAX_STRENGTH_ADJ=22;
	/** stat constant for  max intelligence adjustment*/
	public static final int STAT_MAX_INTELLIGENCE_ADJ=23;
	/** stat constant for  max dextity adjustment*/
	public static final int STAT_MAX_DEXTERITY_ADJ=24;
	/** stat constant for  nax constitution adjustment*/
	public static final int STAT_MAX_CONSTITUTION_ADJ=25;
	/** stat constant for  mac charisma adjustment*/
	public static final int STAT_MAX_CHARISMA_ADJ=26;
	/** stat constant for  max wisdom adjustment*/
	public static final int STAT_MAX_WISDOM_ADJ=27;
	/** stat constant for age*/
	public static final int STAT_AGE=28;
	/** stat constant for save vs detection when hiding */
	public static final int STAT_SAVE_DETECTION=29;
	/** stat constant for save vs overlooking hidden things */
	public static final int STAT_SAVE_OVERLOOKING=30;
	/** stat constant for amount of faith? */
	public static final int STAT_FAITH=31;
	/** stat constant for additions/subtractions from base weight */
	public static final int STAT_WEIGHTADJ=32;
	/** stat constant for  save vs bludgeoning*/
	public static final int STAT_SAVE_BLUNT=33;
	/** stat constant for  save vs piercing*/
	public static final int STAT_SAVE_PIERCE=34;
	/** stat constant for  save vs slashing*/
	public static final int STAT_SAVE_SLASH=35;
	/** stat constant for  save vs spells*/
	public static final int STAT_SAVE_SPELLS=36;
	/** stat constant for  save vs prayers*/
	public static final int STAT_SAVE_PRAYERS=37;
	/** stat constant for  save vs songs*/
	public static final int STAT_SAVE_SONGS=38;
	/** stat constant for  save vs chants*/
	public static final int STAT_SAVE_CHANTS=39;
	/** stat constant for  bonus chance % to critical hits with a weapon */
	public static final int STAT_CRIT_CHANCE_PCT_WEAPON=40;
	/** stat constant for  bonus chance % to critical hits with magic */
	public static final int STAT_CRIT_CHANCE_PCT_MAGIC=41;
	/** stat constant for  bonus damage % to critical hits with a weapon */
	public static final int STAT_CRIT_DAMAGE_PCT_WEAPON=42;
	/** stat constant for  bonus damage % to critical hits with magic */
	public static final int STAT_CRIT_DAMAGE_PCT_MAGIC=43;
	/** constant for total number of stat codes */
	public final static int DEFAULT_NUM_STATS=42;

	/**
	 * Copies the internal data of this object into another of kind.
	 * @param intoStats another CharStats object.
	 */
	public void copyInto(CharStats intoStats);

   /**
	 * Get the value of one of the STAT_ constants, representing attributes,
	 * saving throws, and max attributes, from the CharStats interface.
	 * @see CharStats
	 * @param statNum which STAT_ constant to get a value for
	 * @return the value of the given STAT
	 */
	public int getStat(int statNum);

	/**
	 * A method that simply calculates the nomimal max of the given
	 * ordinary stat code from available data.
	 * @param abilityCode (str, int, etc)
	 * @return the max stat.
	 */
	public int getMaxStat(int abilityCode);

	/**
	 * Set the value of one of the STAT_ constants, representing attributes,
	 * saving throws, and max attributes, from the CharStats interface.
	 * @see CharStats
	 * @param statNum which STAT_ constant to get a value for
	 * @param value the value of the given STAT
	 */
	public void setStat(int statNum, int value);

	/**
	 * Set one of the basic attributes to a given value.  The basic attributes
	 * are defined as the first 6 STAT_ constants from the CharStats interface.
	 * Using this method will also update the STAT_MAX_* attributes to make
	 * sure that they are not able to be modifed upwards.
	 * @see CharStats
	 * @param statNum which STAT_ constant to get a value for
	 * @param value the value of the given STAT
	 */
	public void setPermanentStat(int statNum, int value);

	/**
	 * Set one of the basic attributes to approx the value.  The basic attributes
	 * are defined as the first 6 STAT_ constants from the CharStats interface.
	 * Using this method will also update the STAT_MAX_* attributes to make
	 * sure that they are not able to be trained upwards.
	 * @see CharStats
	 * @param statNum which STAT_ constant to get an approx value for
	 * @param value the value of the max, and approxvalue of the given STAT
	 */
	public void setRacialStat(int statNum, int value);

	/**
	 * Get one of the basic attributes after racial adjustment.
	 * @see CharStats
	 * @param mob the mob to get adjustments for
	 * @param statNum which STAT_ constant to get
	 * @return the value
	 */
	public int getRacialStat(MOB mob, int statNum);

	/**
	 * This method cross-references the given stat name string with the STAT_DESCS
	 * string list in the CharStats interface to return the STAT_ constant which
	 * the given string represents.
	 * @see CharStats
	 * @param abilityName name of which constant to determine the STAT_ constant for
	 * @return the STAT_ constant value from CharStats interface
	 */
	public int getCode(String abilityName);

	/**
	 * Uses the saving throw stats stored here the mob, modified by basic attributes,
	 * to return a final Saving Throw value for this mob.
	 * @see CharStats
	 * @param which which STAT_SAVE_ constant from CharStats interface to use
	 * @return the final saving throw value
	 */
	public int getSave(int which);

	/**
	 * Get saving throw and max stat info as an semicolon string list.
	 * @return semicolon string
	 */
	public String getNonBaseStatsAsString();

	/**
	 * set saving throw and max stat info from a semicolon string list.
	 * @param str semicolon string
	 */
	public void setNonBaseStatsFromString(String str);

	/**
	 * Return the number of a given body part which this mob has.  The
	 * racial part number comes from the Race interface BODY_ constants.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @param racialPartNumber the BODY_ constant from Race interface
	 * @return the number of the given body part this mob has
	 */
	public int getBodyPart(int racialPartNumber);

	/**
	 * Alter the number of a given body part which this mob has.  The
	 * racial part number comes from the Race interface BODY_ constants.
	 * The number is positive or negative reflecting the change.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @param racialPartNumber the BODY_ constant from Race interface
	 * @param number the amount to change by, positive or negative
	 */
	public void alterBodypart(int racialPartNumber, int number);

	/**
	 * Check the difference between the number of a given body part which
	 * this mob has and the number he or she should have.  The
	 * racial part number comes from the Race interface BODY_ constants.
	 * The return value is positive or negative reflecting the change.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @param racialPartNumber the BODY_ constant from Race interface
	 * @return the difference between parts he has and should have
	 */
	public int getBodypartAlteration(int racialPartNumber);

	/**
	 * Returns the entire current body part situation as a string list.
	 * @return the body parts alteration list
	 */
	public String getBodyPartsAsString();

	/**
	 * Sets the body part situation for this mob from a string list.  The
	 * name is a reminder to make sure the mobs Race is established first.
	 * @param str the string list representing the body part situation
	 */
	public void setBodyPartsFromStringAfterRace(String str);

	/**
	 * Returns the number of character classes that this mob has 0 or more
	 * levels in.
	 * @return number of character classes
	 */
	public int numClasses();

	/**
	 * Returns the CharClass object for this mob which corresponds to the
	 * the given index.  The oldest class is always indexed at 0, with next
	 * newest at 1, and the current one last.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @param i the index into the charclass list.
	 * @return the CharClass object
	 */
	public CharClass getMyClass(int i);

	/**
	 * Returns the CharClass object for this mob which corresponds to the
	 * the final index.  The oldest class is always indexed at 0, with next
	 * newest at 1, and the current one last.
	 * @see #numClasses()
	 * @see #getCharClasses()
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @return the 0 index CharClass object
	 */
	public CharClass getCurrentClass();

	/**
	 * Returns all CharClass objects for this mob.
	 * The oldest class is always indexed at 0, with next
	 * newest at 1, and the current one last.
	 * @see #numClasses()
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @return the Collection of CharClass objects
	 */
	public Collection<CharClass> getCharClasses();

	/**
	 * Returns the number of levels that this mob has in the CharClass
	 * object which corresponds to the final index.
	 * The oldest class is always indexed at 0, with next
	 * newest at 1, and the current one last.
	 * @see #numClasses()
	 * @return the number of levels in the 0 index class
	 */
	public int getCurrentClassLevel();

	/**
	 * Creates the enumerated set of character class objects stored here
	 * from a semicolon list of string names
	 * @param classes the semicolon list of character class names
	 */
	public void setMyClasses(String classes);

	/**
	 * Creates the enumerated set of character class levels stored here
	 * from a semicolon list of levels.
	 * @param levels the semicolon list of levels
	 */
	public void setMyLevels(String levels);

	/**
	 * Returns the enumerated set of character class names stored here
	 * as a semicolon list of string names
	 * @return the semicolon list of character class names
	 */
	public String getMyClassesStr();

	/**
	 * Returns the enumerated set of character class levels stored here
	 * as a semicolon list of levels.
	 * @return levels the semicolon list of levels
	 */
	public String getMyLevelsStr();

	/**
	 * Adds the character class to the mob to the given class, automatically
	 * making the class level 0, and making the class current.
	 * @see #getCurrentClass()
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @param aClass The charclass object to set the current class to
	 */
	public void setCurrentClass(CharClass aClass);

	/**
	 * Sets the current class level for the mob to the given level.
	 * @param level The chararacter class level to set the current class to
	 */
	public void setCurrentClassLevel(int level);

	/**
	 * Returns the number of levels this mob has in the given character class.
	 * -1 means the mob has NO levels in that class.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @param aClass the CharClass object to check
	 * @return the number of levels the mob has in the class, or -1
	 */
	public int getClassLevel(CharClass aClass);

	/**
	 * Returns the number of levels this mob has in the given character class by name.
	 * -1 means the mob has NO levels in that class.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @param aClass the name of the Character Class to check
	 * @return the number of levels the mob has in the class, or -1
	 */
	public int getClassLevel(String aClass);

	/**
	 * Returns the combined number of class levels the mob has in all of his classes,
	 * except for the current one.
	 * @return combined levels minus the current one.
	 */
	public int combinedSubLevels();

	/**
	 * Returns true if this user is capped by the given
	 * classes level cap (if one exists)
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass#getLevelCap()
	 * @param C the class to check
	 * @return true if the player is level capped, false otherwise
	 */
	public boolean isLevelCapped(CharClass C);

	/**
	 * Changes the number of class levels the mob has in the given character class
	 * to the given level.  If the mob does not have any levels in the given
	 * class, then setCurrentClass will be called first.
	 * @see #setCurrentClass(CharClass)
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @param aClass the character class to set a level for
	 * @param level the level to set for the given character class
	 */
	public void setClassLevel(CharClass aClass, int level);

	/**
	 * Returns the race of the mob.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @return the Race of the mob
	 */
	public Race getMyRace();

	/**
	 * Sets the race of the mob.  Race.startRacing should
	 * be called after this method is.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race#startRacing(MOB, boolean)
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @param newVal the Race of the mob
	 */
	public void setMyRace(Race newVal);

	/**
	 * Returns the displayable name of this mobs current race.  If this method
	 * is called on the mobs charStats() object, as opposed to baseCharStats(), it
	 * may return something different than charStats().getMyRace().name().  For this
	 * reason, you should ONLY use this method when you want to display the mobs
	 * current race.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @see #setRaceName(String)
	 * @return the name of this mobs current race.
	 */
	public String raceName();

	/**
	 * Returns the term seen when a character arrives into a room
	 * By default, these come from the current 
	 * actual race, unless set to something new.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @see #setArriveLeaveStr(String,String)
	 * @return the arrive string
	 */
	public String getArriveStr();
	
	/**
	 * Returns the term seen when a character leaves a room
	 * By default, these come from the current 
	 * actual race, unless set to something new.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @see #setArriveLeaveStr(String,String)
	 * @return the leave string
	 */
	public String getLeaveStr();

	/**
	 * Returns resource codes of what this race can breathe as
	 * an atmosphere.  The list is guaranteed sorted.  If the list
	 * is empty, the race can breathe anything at all.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @see #setBreathables(int[])
	 * @return a list of resource codes that this race can breathe
	 */
	public int[] getBreathables();

	/**
	 * Sets resource codes of what this race can breathe as
	 * an atmosphere.  The list MUST BE sorted.  If the list
	 * is empty, the race can breathe anything at all.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @see #getBreathables()
	 * @param newArray a list of resource codes that this race can breathe
	 */
	public void setBreathables(int[] newArray);

	/**
	 * Changes the apparent race of ths mob by setting a new name.  A value of null will
	 * reset this setting, allowing the mobs TRUE race to be displayed through the
	 * raceName method instead of the string set through this one.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @see #raceName()
	 * @param newRaceName the name of the mobs apparent race
	 */
	public void setRaceName(String newRaceName);

	/**
	 * Changes the terms seen when a character arrives into a room
	 * and leaves it.  By default, these come from the current 
	 * actual race. 
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @see #getArriveStr()
	 * @see #getLeaveStr()
	 * @param arriveStr the arrive string
	 * @param leaveStr the leave string
	 */
	public void setArriveLeaveStr(String arriveStr, String leaveStr);

	/**
	 * Changes the apparent char class of ths mob by setting a new name.  A value of null will
	 * reset this setting, allowing the mobs TRUE current class to be displayed through the
	 * displayClassName method instead of the string set through this one.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @see #displayClassName()
	 * @see #displayClassLevel(MOB, boolean)
	 * @see #displayClassLevelOnly(MOB)
	 * @param newname the name of the mobs apparent current class
	 */
	public void setDisplayClassName(String newname);

	/**
	 * Changes the apparent level of ths mob by setting a new name.  A value of null will
	 * reset this setting, allowing the mobs TRUE level to be displayed through the
	 * displayClassLevel method instead of the string set through this one.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @see #displayClassName()
	 * @see #displayClassLevel(MOB, boolean)
	 * @see #displayClassLevelOnly(MOB)
	 * @see #setDisplayClassName(String)
	 * @param newlevel the name of the mobs apparent level
	 */
	public void setDisplayClassLevel(String newlevel);

	/**
	 * Returns the displayable name of this mobs current class.  If this method
	 * is called on the mobs charStats() object, as opposed to baseCharStats(), it
	 * may return something different than charStats().getCurrentClass().name().  For this
	 * reason, you should ONLY use this method when you want to display the mobs
	 * current class.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @see #setDisplayClassName(String)
	 * @see #displayClassLevel(MOB, boolean)
	 * @see #displayClassLevelOnly(MOB)
	 * @return the name of this mobs current class.
	 */
	public String displayClassName();

	/**
	 * Returns a combination of the displayClassName and displayClassLevel for
	 * the given mob.  If either are null, authentic values will be used.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @see #displayClassName()
	 * @see #setDisplayClassName(String)
	 * @see #displayClassLevelOnly(MOB)
	 * @param mob the mob whose class and overall level to display
	 * @param shortForm if true, display only the class and level, no extra wording
	 * @return the name of this mobs current class.
	 */
	public String displayClassLevel(MOB mob, boolean shortForm);

	/**
	 * Returns either the given mobs authentic classlevel/total level,
	 * or the value set through setDisplayClassLevel method.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats#displayClassName()
	 * @see #setDisplayClassName(String)
	 * @see #displayClassLevel(MOB, boolean)
	 * @param mob the mob whose class and overall level to display
	 * @return the name of this mobs current class.
	 */
	public String displayClassLevelOnly(MOB mob);

	/**
	 * Resets all of the stats in this object, attributes and saves alike, to
	 * the same given value
	 * @param def the value to set all stats to.
	 */
	public void setAllValues(int def);

	/**
	 * Resets all the stats in this object to their factory defaults.
	 */
	public void reset();

	/**
	 * Resets all of the basic 1-6 stats in this object, to the same given value
	 * @param def the value to set all stats to.
	 */
	public void setAllBaseValues(int def);

	/**
	 * Returns the sum of local and race-based unwearability codes.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Item
	 * @return a bitmap of unwearable locations
	 */
	public long getWearableRestrictionsBitmap();

	/**
	 * Sets the sum of local and race-based unwearability codes.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Item
	 * @param bitmap a bitmap of unwearable locations
	 */
	public void setWearableRestrictionsBitmap(long bitmap);

	/**
	 * Sets the apparent gender of the mob. If null is used, this value is reset
	 * and the mobs real gender name will be displayed through the genderName method
	 * instead of the one set here.
	 * @see #genderName()
	 * @param gname the name of the mobs gender to display
	 */
	public void setGenderName(String gname);

	/**
	 * Returns the apparent gender of the mob.  If this method
	 * is called on the mobs charStats() object, as opposed to baseCharStats(), it
	 * may return something different than charStats().getStat(GENDER)  For this
	 * reason, you should ONLY use this method when you want to display the mobs
	 * current gender.
	 * @see #setGenderName(String)
	 * @return the apparent gender of the mob
	 */
	public String genderName();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "him", "her", or "it".
	 * @return the gender-correct pronoun for this mob
	 */
	public String himher();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "his", "her", or "its".
	 * @return the gender-correct pronoun for this mob
	 */
	public String hisher();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "he", "she", or "it".
	 * @return the gender-correct pronoun for this mob
	 */
	public String heshe();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "He", "She", or "It".
	 * @return the gender-correct pronoun for this mob
	 */
	public String HeShe();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "sir", "madam", or "sir".
	 * @return the gender-correct title for this mob
	 */
	public String sirmadam();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "Sir", "Madam", or "Sir".
	 * @return the gender-correct title for this mob
	 */
	public String SirMadam();

	/**
	 * Returns the age category for this mob, based on the age stat constant stored here.  The age
	 * categories are defined in the Race interface as AGE_ constants.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race
	 * @return the numeric age category of this mob
	 */
	public int ageCategory();

	/**
	 * Returns the string name of the age category for this mob, based on the age
	 * stat constant stored here.  The age category names are defined in the Race
	 * interface in the AGE_DESCS_ constant.
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race#AGE_DESCS
	 * @return the name of the age category for this mob
	 */
	public String ageName();
	
	/**
	 * Returns the adjustments to mob abilities, whether proficiency or
	 * something else numeric.  
	 * Ability adjustments begin with "prof+" to adjust proficiency.
	 * They begin with "level+" to adjust the adjusted level.
	 * The are followed by the exact ID() of the ability, or * for All, or
	 * the skill classification name in uppercase, or the domain name in
	 * uppercase.
	 * @param ableID the ability ID, with an appropriate prefix
	 * @return the numeric value associated with the adjustment.
	 */
	public int getAbilityAdjustment(String ableID);
	
	/**
	 * Sets the adjustments to mob abilities, whether proficiency or
	 * something else numeric.  
	 * Ability adjustments begin with "prof+" to adjust proficiency.
	 * They begin with "level+" to adjust the adjusted level.
	 * The are followed by the exact ID() of the ability, or * for All, or
	 * the skill classification name in uppercase, or the domain name in
	 * uppercase.
	 * @param ableID the ability ID, with an appropriate prefix
	 * @param newValue the numeric value associated with the adjustment.
	 */
	public void adjustAbilityAdjustment(String ableID, int newValue);

	/**
	 * Returns the list of special weapon or armor proficiencies, 
	 * or special detriments, that this character has.
	 * Each one is a filterer which, if passes, means the character
	 * can always use the armor or weapon, even if class or race
	 * or other considerations might not permit it.
	 * @see #setItemProficiencies(DoubleFilterer[])
	 * @return a list of proficiency or deficiency codes
	 */
	public DoubleFilterer<Item>[] getItemProficiencies();

	/**
	 * Sets the list of special weapon or armor proficiencies, 
	 * or special detriments, that this character has.
	 * Each one is a filterer which, if passes, means the character
	 * can always use the armor or weapon, even if class or race
	 * or other considerations might not permit it.
	 * @see #getItemProficiencies()
	 * @see #addItemDeficiency(String)
	 * @see #addItemProficiency(String)
	 * @param newArray a list of proficiency or deficiency codes
	 */
	public void setItemProficiencies(DoubleFilterer<Item>[] newArray);

	/**
	 * Adds a new weapon or armor proficiency, compiling it into
	 * the proficiency filter.  The argument is a zapper mask describing
	 * the type of item that the player is newly proficient at.
	 * 
	 * @see #getItemProficiencies()
	 * @see #addItemDeficiency(String)
	 * 
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param zapperMask the zapper mask
	 */
	public void addItemProficiency(String zapperMask);

	/**
	 * Adds a new weapon or armor deficiency, compiling it into
	 * the deficiency filter.  The argument is a zapper mask describing
	 * the type of item that the player is newly deficient at.
	 * 
	 * @see #getItemProficiencies()
	 * @see #addItemProficiency(String)
	 * 
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param zapperMask the zapper mask
	 */
	public void addItemDeficiency(String zapperMask);

	/** string array of abbreviations of each stat code, ordered by numeric value */
	public static final String[] DEFAULT_STAT_ABBR=
	{
		"S",
		"I",
		"D",
		"C",
		"CH",
		"W",
		"G",
		"vPY",
		"vF",
		"vC",
		"vW",
		"vG",
		"vMI",
		"V",
		"vJ",
		"vA",
		"vE",
		"vP",
		"vU",
		"vM",
		"vD",
		"vT",
		"mS",
		"mI",
		"mD",
		"mC",
		"mCH",
		"mW",
		"A",
		"vH",
		"vO",
		"vC",
		"Wa",
		"vWB",
		"vWP",
		"vWS",
		"vMS",
		"vMP",
		"vMG",
		"vMC",
		"cRW",
		"cRM",
		"cDW",
		"cDM"
	};

	/** string array of descriptions of each stat code, ordered by numeric value */
	public static final String[] DEFAULT_STAT_DESCS=
	{
		"STRENGTH",
		"INTELLIGENCE",
		"DEXTERITY",
		"CONSTITUTION",
		"CHARISMA",
		"WISDOM",
		"GENDER",
		"PARALYSIS SAVE",
		"SAVE VS FIRE",
		"SAVE VS COLD",
		"SAVE VS WATER",
		"SAVE VS GAS",
		"SAVE VS MIND",
		"GENERAL SAVE",
		"JUSTICE SAVE",
		"SAVE VS ACID",
		"SAVE VS ELECTRICITY",
		"SAVE VS POISON",
		"SAVE VS UNDEAD",
		"SAVE VS MAGIC",
		"SAVE VS DISEASE",
		"SAVE VS TRAPS",
		"MAX STRENGTH ADJ.",
		"MAX INTELLIGENCE ADJ.",
		"MAX DEXTERITY ADJ.",
		"MAX CONSTITUTION ADJ.",
		"MAX CHARISMA ADJ.",
		"MAX WISDOM ADJ.",
		"AGE",
		"SAVE VS DETECTION",
		"SAVE VS OVERLOOKING",
		"SAVE VS CONVERSIONS",
		"WEIGHT ADJUSTMENT",
		"SAVE VS BLUNT",
		"SAVE VS PIERCE",
		"SAVE VS SLASH",
		"SAVE VS SPELLS",
		"SAVE VS PRAYERS",
		"SAVE VS SONGS",
		"SAVE VS CHANTS",
		"CRIT WEAPON CHANCE PCT",
		"CRIT MAGIC CHANCE PCT",
		"CRIT WEAPON DAMAGE PCT",
		"CRIT MAGIC DAMAGE PCT"
	};

	/** string array of descriptions of each stat code, ordered by numeric value */
	public static final String[] DEFAULT_STAT_NAMES=
	{
		"STRENGTH",
		"INTELLIGENCE",
		"DEXTERITY",
		"CONSTITUTION",
		"CHARISMA",
		"WISDOM",
		"GENDER",
		"PARALYSIS",
		"FIRE",
		"COLD",
		"WATER",
		"GAS",
		"MIND",
		"GENERAL",
		"JUSTICE",
		"ACID",
		"ELECTRICITY",
		"POISON",
		"UNDEAD",
		"MAGIC",
		"DISEASE",
		"TRAPS",
		"MAXSTRENGTH",
		"MAXINTELLIGENCE",
		"MAXDEXTERITY",
		"MAXCONSTITUTION",
		"MAXCHARISMA",
		"MAXWISDOM",
		"AGE",
		"DETECTION",
		"OVERLOOKING",
		"CONVERSION",
		"WEIGHTADJ",
		"SAVEBLUNT",
		"SAVEPIERCE",
		"SAVESLASH",
		"SAVESPELLS",
		"SAVEPRAYERS",
		"SAVESONGS",
		"SAVECHANTS",
		"CRITPCTWEAPONS",
		"CRITPCTMAGIC",
		"CRITDMGWEAPONS",
		"CRITDMGMAGIC"
	};

	/** string array of attributable descriptions of each stat code, ordered by numeric value */
	public static final String[] DEFAULT_STAT_DESC_ATTS=
	{
		"STRONG",
		"INTELLIGENT",
		"DEXTEROUS",
		"HEALTHY",
		"CHARISMATIC",
		"WISE",
		"",
		"RESISTANT TO PARALYSIS",
		"RESISTANT TO FIRE",
		"RESISTANT TO COLD",
		"RESISTANT TO WATER",
		"RESISTANT TO GAS",
		"RESISTANT TO MIND ATTACKS",
		"RESISTANT",
		"RESISTANT TO UNDIGNIFIED ATTACKS",
		"RESISTANT TO ACID",
		"RESISTANT TO ELECTRICITY",
		"RESISTANT TO POISON",
		"RESISTANT TO UNDEAD",
		"RESISTANT TO MAGIC",
		"RESISTANT TO DISEASE",
		"RESISTANT TO TRAPS",
		"POTENTIALLY STRONG",
		"POTENTIALLY INTELLIGENT",
		"POTENTIALLY DEXTROUS",
		"POTENTIALLY HEALTHY",
		"POTENTIALLY CHARISMATIC",
		"POTENTIALLY WISE",
		"OLD",
		"CONCEALED",
		"WATCHFUL",
		"DOUBTFUL",
		"",
		"RESISTANT TO BLUNT",
		"RESISTANT TO PIERCE",
		"RESISTANT TO SLASH",
		"RESISTANT TO SPELLS",
		"RESISTANT TO PRAYERS",
		"RESISTANT TO SONGS",
		"RESISTANT TO CHANTS",
		"BONUS TO WEAPON CRIT CHANCE",
		"BONUS TO MAGIC CRIT CHANCE",
		"BONUS TO WEAPON CRIT DAMAGE",
		"BONUS TO MAGIC CRIT DAMAGE",
	};

	/** an appropriate CMMsg MSG type to correspond to the given saving throw, indexed as STAT_SAVE_ constant */
	public static int[] DEFAULT_STAT_MSG_MAP= 
	{
		-1, // strength
		-1, // intelligence
		-1, // dexterity
		-1, // constitution
		-1, // charisma
		-1, // wisdom
		-1, // gender
		CMMsg.TYP_PARALYZE,
		CMMsg.TYP_FIRE,
		CMMsg.TYP_COLD,
		CMMsg.TYP_WATER,
		CMMsg.TYP_GAS,
		CMMsg.TYP_MIND,
		CMMsg.TYP_GENERAL,
		CMMsg.TYP_JUSTICE,
		CMMsg.TYP_ACID,
		CMMsg.TYP_ELECTRIC,
		CMMsg.TYP_POISON,
		CMMsg.TYP_UNDEAD,
		CMMsg.TYP_CAST_SPELL,
		CMMsg.TYP_DISEASE,
		-1, // traps
		-1, // max str
		-1, // max int
		-1, // max
		-1, // max dex
		-1, // max con
		-1, // max cha
		-1, // max wis
		-1, // age
		-1, // save conceilment
		-1, // save overlooking
		-1, // save conversion
		-1, // weight adjustment
		-1, // save blunt
		-1, // save pierce
		-1, // save slash
		-1, // save spells
		-1, // save prayers
		-1, // save songs
		-1, // save chants
		-1, // bonus weapon crit chance
		-1, // bonus magic crit chance
		-1, // bonus weapon crit damage
		-1, // bonus magic crit damage
	};

	/**
	 * Global character stat code data collector
	 * @author Bo Zimmermanimmerman
	 */
	public class CODES
	{
		private static CODES[] insts=new CODES[256];

		private int[]			baseStatCodes				= new int[0];
		private int[]			maxStatCodes				= new int[0];
		private int[]			MaxBaseCrossCodes			= new int[0];
		private int[]			allStatCodes				= new int[0];
		private int[]			savingThrowCodes			= new int[0];
		private boolean[]		isBaseStatCode				= new boolean[0];
		private String[]		statAbbreviations			= new String[0];
		private String[]		statDescriptions			= new String[0];
		private String[]		statNames					= new String[0];
		private String[]		baseStatNames				= new String[0];
		private String[]		shortNames					= new String[0];
		private String[]		statAttributionDescriptions	= new String[0];
		private int[]			statCMMsgMapping			= new int[0];
		private int				longestBaseCodeName			= -1;
		private final Map<Integer,Integer> rvsStatCMMsgMapping	= new Hashtable<Integer,Integer>();

		public CODES()
		{
			super();
			final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
			if(insts==null)
				insts=new CODES[256];
			if(insts[c]==null)
				insts[c]=this;

			final String[][] addExtra = CMProps.instance().getStrsStarting("ADDCHARSTAT_");
			final String[][] repExtra = CMProps.instance().getStrsStarting("REPLACECHARSTAT_");
			for(int i=STAT_STRENGTH;i<=STAT_WISDOM;i++)
				addBaseStat(DEFAULT_STAT_ABBR[i],DEFAULT_STAT_DESCS[i],DEFAULT_STAT_NAMES[i],DEFAULT_STAT_DESC_ATTS[i],DEFAULT_STAT_MSG_MAP[i]);
			addAllStat(DEFAULT_STAT_ABBR[STAT_GENDER],DEFAULT_STAT_DESCS[STAT_GENDER],DEFAULT_STAT_NAMES[STAT_GENDER],
						DEFAULT_STAT_DESC_ATTS[STAT_GENDER],DEFAULT_STAT_MSG_MAP[STAT_GENDER],false);
			for(int i=STAT_SAVE_PARALYSIS;i<=STAT_SAVE_TRAPS;i++)
				addSavingThrow(DEFAULT_STAT_ABBR[i],DEFAULT_STAT_DESCS[i],DEFAULT_STAT_NAMES[i],DEFAULT_STAT_DESC_ATTS[i],DEFAULT_STAT_MSG_MAP[i]);
			int baseCtr=0;
			for(int i=STAT_MAX_STRENGTH_ADJ;i<=STAT_MAX_WISDOM_ADJ;i++)
				addMaxStat(baseStatCodes[baseCtr++],DEFAULT_STAT_ABBR[i],DEFAULT_STAT_DESCS[i],DEFAULT_STAT_NAMES[i],DEFAULT_STAT_DESC_ATTS[i],DEFAULT_STAT_MSG_MAP[i]);
			addAllStat(DEFAULT_STAT_ABBR[STAT_AGE],DEFAULT_STAT_DESCS[STAT_AGE],DEFAULT_STAT_NAMES[STAT_AGE],
						DEFAULT_STAT_DESC_ATTS[STAT_AGE],DEFAULT_STAT_MSG_MAP[STAT_AGE],false);
			for(int i=STAT_SAVE_DETECTION;i<=STAT_FAITH;i++)
				addSavingThrow(DEFAULT_STAT_ABBR[i],DEFAULT_STAT_DESCS[i],DEFAULT_STAT_NAMES[i],DEFAULT_STAT_DESC_ATTS[i],DEFAULT_STAT_MSG_MAP[i]);
			for(int i=STAT_FAITH;i<DEFAULT_NUM_STATS;i++)
				addAllStat(DEFAULT_STAT_ABBR[i],DEFAULT_STAT_DESCS[i],DEFAULT_STAT_NAMES[i],DEFAULT_STAT_DESC_ATTS[i],DEFAULT_STAT_MSG_MAP[i],false);
			for(int i=0;i<addExtra.length+repExtra.length;i++)
			{
				final String[] array = (i>=addExtra.length)?repExtra[i-addExtra.length]:addExtra[i];
				final boolean replace = i>=addExtra.length;
				String stat = array[0].toUpperCase().trim();
				final String p=array[1];
				final List<String> V=CMParms.parseCommas(p, false);
				if(V.size()!=4)
				{
					Log.errOut("CharStats","Bad coffeemud.ini charstat row, requires 4 ; separated entries: "+p);
					continue;
				}
				String type=V.get(0).toUpperCase().trim();
				int oldStatCode=-1;
				if(replace)
				{
					final String repStat=stat;
					stat=type;
					oldStatCode=CMParms.indexOf(DEFAULT_STAT_NAMES, repStat);
					if(oldStatCode<0)
						oldStatCode=CMParms.indexOf(DEFAULT_STAT_DESCS, repStat);
					if(oldStatCode>=0)
						type="REPLACE";
					else
					{
						Log.errOut("CharStats","Bad coffeemud.ini charstat row, bad stat name: "+repStat);
						continue;
					}
				}
				final String abbr=V.get(1);
				final String desc=V.get(2).toUpperCase();
				final String adj=V.get(3).toUpperCase();
				if(type.equalsIgnoreCase("BASE"))
				{
					addBaseStat(abbr, desc, stat, adj, -1);
					final int baseStatCode=allStatCodes.length-1;
					addMaxStat(baseStatCode, "m"+abbr, "MAX "+stat+" ADJ.", "MAX"+stat, "POTENTIALLY "+adj, -1);
				}
				else
				if(type.equalsIgnoreCase("SAVE"))
					addSavingThrow(abbr,desc,stat,adj,-1);
				else
				if(type.equalsIgnoreCase("OTHER"))
					addAllStat(abbr,desc,stat,adj,-1,false);
				else
				if(replace&&(oldStatCode>=0))
				{
					statAbbreviations[oldStatCode]=abbr;
					statDescriptions[oldStatCode]=desc;
					statNames[oldStatCode]=stat;
					if(stat.length()>3)
						shortNames[oldStatCode]=CMStrings.capitalizeAndLower(stat).substring(0,3);
					else
						shortNames[oldStatCode]=CMStrings.capitalizeAndLower(stat);
					statAttributionDescriptions[oldStatCode]=adj;
					statCMMsgMapping[oldStatCode]=-1;
				}
				else
					Log.errOut("CharStats","Bad coffeemud.ini charstat row, bad type: "+type);
			}
		}
		
		private static CODES c()
		{ 
			return insts[Thread.currentThread().getThreadGroup().getName().charAt(0)];
		}
		
		public static CODES c(byte c)
		{
			return insts[c];
		}
		
		public static CODES instance()
		{
			CODES c=insts[Thread.currentThread().getThreadGroup().getName().charAt(0)];
			if(c==null)
				c=new CODES();
			return c;
		}
		
		public static void reset() 
		{
			insts[Thread.currentThread().getThreadGroup().getName().charAt(0)]=null;
			instance();
		}

		/**
		 * Returns an array of the numeric codes for all base stats
		 * @return an array of the numeric codes for all base stats
		 */
		public static int[] BASECODES()
		{
			return c().baseStatCodes;
		}

		/**
		 * Returns the longest base code name
		 * @return the longest base code name
		 */
		public static int LONNGEST_BASECODE_NAME()
		{
			final CODES c=c();
			if(c.longestBaseCodeName<0)
			{
				for(int code : c.baseStatCodes)
				{
					if(c.statNames[code].length()>c.longestBaseCodeName)
						c.longestBaseCodeName=c.statNames[code].length();
				}
			}
			return c.longestBaseCodeName;
		}

		/**
		 * Returns the name of all base stats
		 * @return names of all base stats
		 */
		public static String[] BASENAMES()
		{
			return c().baseStatNames;
		}

		/**
		 * Returns an array of the numeric codes for all base stats
		 * 
		 * @return an array of the numeric codes for all base stats
		 */
		public int[] base()
		{
			return baseStatCodes;
		}

		/**
		 * Returns whether the given code is a base stat
		 * 
		 * @param code the STAT code to check
		 * @return whether the given code is a base stat
		 */
		public static boolean isBASE(int code)
		{
			return c().isBaseStatCode[code];
		}

		/**
		 * Returns whether the given code is a base stat
		 * 
		 * @param code the STAT code to check
		 * @return whether the given code is a base stat
		 */
		public boolean isBase(int code)
		{
			return isBaseStatCode[code];
		}

		/**
		 * Returns the code for the base code that matches the given max adj code
		 * Returns the code for the max adj code that matches the given base code
		 * Returns -1 if the code is not a max adj code.
		 * @param max the MAX state adjustment code
		 * @return the translated code
		 */
		public static int toMAXBASE(int max)
		{
			final CODES c = c();
			if(max<c.MaxBaseCrossCodes.length)
				return c.MaxBaseCrossCodes[max];
			return -1;
		}

		/**
		 * Returns the code for the base code that matches the given max adj code
		 * Returns the code for the max adj code that matches the given base code
		 * Returns -1 if the code is not a max adj code.
		 * @param max the MAX state adjustment code
		 * @return the translated code
		 */
		public int toMaxBase(int max)
		{
			if(max<MaxBaseCrossCodes.length)
				return MaxBaseCrossCodes[max];
			return -1;
		}
		/**
		 * Returns an array of the numeric codes for all max stats
		 * @return an array of the numeric codes for all max stats
		 */
		public static int[] MAXCODES()
		{
			return c().maxStatCodes;
		}

		/**
		 * Returns an array of the numeric codes for all max stats
		 * 
		 * @return an array of the numeric codes for all max stats
		 */
		public int[] max()
		{
			return maxStatCodes;
		}

		/**
		 * Returns total number of stat codes 0 - this-1
		 * 
		 * @return total number of stat codes 0 - this-1
		 */
		public static int TOTAL()
		{
			return c().allStatCodes.length;
		}

		/**
		 * Returns total number of stat codes 0 - this-1
		 * 
		 * @return total number of stat codes 0 - this-1
		 */
		public int total()
		{
			return allStatCodes.length;
		}

		/**
		 * Returns an array of the numeric codes for all stats
		 * 
		 * @return an array of the numeric codes for all stats
		 */
		public static int[] ALLCODES()
		{
			return c().allStatCodes;
		}

		/**
		 * Returns an array of the numeric codes for all stats
		 * 
		 * @return an array of the numeric codes for all stats
		 */
		public int[] all()
		{
			return allStatCodes;
		}

		/**
		 * Returns an array of the numeric codes for all save stats
		 * 
		 * @return an array of the numeric codes for all save stats
		 */
		public static int[] SAVING_THROWS()
		{
			return c().savingThrowCodes;
		}

		/**
		 * Returns an array of the numeric codes for all save stats
		 * 
		 * @return an array of the numeric codes for all save stats
		 */
		public int[] saving_throws()
		{
			return savingThrowCodes;
		}

		/**
		 * Returns the names of the various stats
		 * 
		 * @return the names of the various stats
		 */
		public static String[] NAMES()
		{
			return c().statNames;
		}

		/**
		 * Returns the name of the stat code
		 * 
		 * @param code the stat code
		 * @return the name of the stat code
		 */
		public static String NAME(int code)
		{
			return c().statNames[code];
		}

		/**
		 * Returns the short name of the stat code
		 * 
		 * @param code the stat code
		 * @return the short name of the stat code
		 */
		public static String SHORTNAME(int code)
		{
			return c().shortNames[code];
		}

		/**
		 * Returns the name of the stat code
		 * 
		 * @param code the stat code
		 * @return the name of the stat code
		 */
		public String name(int code)
		{
			return statNames[code];
		}

		/**
		 * Returns the name of the stat code
		 * 
		 * @param code the stat code
		 * @return the name of the stat code
		 */
		public String shortName(int code)
		{
			return shortNames[code];
		}

		/**
		 * Returns the descriptions of the various stats
		 * 
		 * @return the descriptions of the various stats
		 */
		public static String[] DESCS()
		{
			return c().statDescriptions;
		}

		/**
		 * Returns the description of the stat code
		 * 
		 * @param code the stat code
		 * @return the description of the stat code
		 */
		public static String DESC(int code)
		{
			return c().statDescriptions[code];
		}

		/**
		 * Returns the description of the stat code
		 * 
		 * @param code the stat code
		 * @return the description of the stat code
		 */
		public String desc(int code)
		{
			return statDescriptions[code];
		}

		/**
		 * Returns the abbreviations of the various stats
		 * 
		 * @return the abbreviations of the various stats
		 */
		public static String[] ABBRS()
		{
			return c().statAbbreviations;
		}

		/**
		 * Returns the abbreviation of the stat code
		 * 
		 * @param code the stat code
		 * @return the abbreviation of the stat code
		 */
		public static String ABBR(int code)
		{
			return c().statAbbreviations[code];
		}

		/**
		 * Returns the abbreviation of the stat code
		 * 
		 * @param code the stat code
		 * @return the abbreviation of the stat code
		 */
		public String abbr(int code)
		{
			return statAbbreviations[code];
		}

		/**
		 * Returns the adjective descriptions of the various stats
		 * 
		 * @return the adjective descriptions of the various stats
		 */
		public static String[] ATTDESCS()
		{
			return c().statAttributionDescriptions;
		}

		/**
		 * Returns the adjective description of the stat code
		 * 
		 * @param code the stat code
		 * @return the adjective description of the stat code
		 */
		public static String ATTDESC(int code)
		{
			return c().statAttributionDescriptions[code];
		}

		/**
		 * Returns the CMMsg mappings of the various stats
		 * 
		 * @return the CMMsg mappings of the various stats
		 */
		public static int[] CMMSGMAP()
		{
			return c().statCMMsgMapping;
		}

		/**
		 * Returns the CMMsg mapping of the stat
		 * 
		 * @param code the CMMsg mapping for the given stat code
		 * @return the CMMsg mapping of the stat
		 */
		public static int CMMSGMAP(int code)
		{
			return c().statCMMsgMapping[code];
		}

		/**
		 * Returns the Reverse CMMsg mapping of the stat from MsgCode -&gt; Stat Code
		 * @param code the Stat mapping for the given CMMsg code or -1
		 * @return the Stat mapping of the CMMsg
		 */
		public static int RVSCMMSGMAP(int code) 
		{
			final Integer statCode = c().rvsStatCMMsgMapping.get(Integer.valueOf(code));
			if(statCode != null)
				return statCode.intValue();
			return -1;
		}

		/**
		 * Returns the code for the given stat name
		 * @param name the case insensitive name
		 * @param exactOnly true to only return exact matches, false to do otherwise
		 * @return the stat code
		 */
		public int find(String name, boolean exactOnly)
		{
			for(int i=0;i<total();i++)
			{
				if((name.equalsIgnoreCase(name(i)))
				||(name.equalsIgnoreCase(desc(i)))
				||(name.equalsIgnoreCase(abbr(i))))
					return i;
			}
			if(exactOnly)
				return -1;
			for(int i=0;i<total();i++)
			{
				if((name(i).toLowerCase().startsWith(name))||(desc(i).toLowerCase().startsWith(name)))
					return i;
			}
			return -1;
		}

		/**
		 * Returns the code for the given stat name
		 * @param name the case insensitive name
		 * @param exactOnly true to only return exact matches, false to do otherwise
		 * @return the stat code
		 */
		public static int findWhole(String name, boolean exactOnly) { return c().find(name, exactOnly); }

		/**
		 * Adds a new base stat to this object for all mobs and players to share
		 * @param abbr 1-3 letter short code for this stat
		 * @param desc longer description of this stat
		 * @param name space-free coded name of this stat
		 * @param attDesc description of someone with this stat in abundance
		 * @param cmmsgMap a CMMsg messf code that saves with this stat
		 */
		public void addBaseStat(String abbr, String desc, String name, String attDesc, int cmmsgMap)
		{
			baseStatCodes=Arrays.copyOf(baseStatCodes, baseStatCodes.length+1);
			baseStatCodes[baseStatCodes.length-1]=allStatCodes.length;
			baseStatNames=Arrays.copyOf(baseStatNames, baseStatNames.length+1);
			baseStatNames[baseStatNames.length-1]=name;
			addAllStat(abbr,desc,name,attDesc,cmmsgMap,true);
		}

		/**
		 * Adds a new max stat to this object for all mobs and players to share
		 * @param baseCode corresponding base stat code
		 * @param abbr 1-3 letter short code for this stat
		 * @param desc longer description of this stat
		 * @param name space-free coded name of this stat
		 * @param attDesc description of someone with this stat in abundance
		 * @param cmmsgMap a CMMsg message code that saves with this stat
		 */
		public void addMaxStat(int baseCode, String abbr, String desc, String name, String attDesc, int cmmsgMap)
		{
			maxStatCodes=Arrays.copyOf(maxStatCodes, maxStatCodes.length+1);
			final int maxCode = allStatCodes.length;
			maxStatCodes[maxStatCodes.length-1]=maxCode;

			addAllStat(abbr,desc,name,attDesc,cmmsgMap,false);

			MaxBaseCrossCodes=Arrays.copyOf(MaxBaseCrossCodes, allStatCodes.length);
			MaxBaseCrossCodes[maxCode]=baseCode;
			MaxBaseCrossCodes[baseCode]=maxCode;
		}

		/**
		 * Adds a new saving throw stat to this object for all mobs and players to share
		 * @param abbr 1-3 letter short code for this stat
		 * @param desc longer description of this stat
		 * @param name space-free coded name of this stat
		 * @param attDesc description of someone with this stat in abundance
		 * @param cmmsgMap a CMMsg message code that saves with this stat
		 */
		public void addSavingThrow(String abbr, String desc, String name, String attDesc, int cmmsgMap)
		{
			savingThrowCodes=Arrays.copyOf(savingThrowCodes, savingThrowCodes.length+1);
			savingThrowCodes[savingThrowCodes.length-1]=allStatCodes.length;
			addAllStat(abbr,desc,name,attDesc,cmmsgMap,false);
		}

		/**
		 * Adds a new miscellaneous stat to this object for all mobs and players to share
		 * @param abbr 1-3 letter short code for this stat
		 * @param desc longer description of this stat
		 * @param name space-free coded name of this stat
		 * @param attDesc description of someone with this stat in abundance
		 * @param cmmsgMap a CMMsg message code that saves with this stat
		 * @param base true if the code is a base stat, false if a save or something else
		 */
		public void addAllStat(String abbr, String desc, String name, String attDesc, int cmmsgMap, boolean base)
		{
			allStatCodes=Arrays.copyOf(allStatCodes, allStatCodes.length+1);
			allStatCodes[allStatCodes.length-1]=allStatCodes.length-1;
			isBaseStatCode=Arrays.copyOf(isBaseStatCode, allStatCodes.length);
			isBaseStatCode[allStatCodes.length-1]=base;
			statAbbreviations=Arrays.copyOf(statAbbreviations, allStatCodes.length);
			statAbbreviations[allStatCodes.length-1]=abbr;
			statDescriptions=Arrays.copyOf(statDescriptions, allStatCodes.length);
			statDescriptions[allStatCodes.length-1]=desc.toUpperCase().trim();
			statNames=Arrays.copyOf(statNames, allStatCodes.length);
			statNames[allStatCodes.length-1]=name.toUpperCase().trim().replace(' ','_');
			shortNames=Arrays.copyOf(shortNames, allStatCodes.length);
			if(name.length()>3)
				shortNames[allStatCodes.length-1]=CMStrings.capitalizeAndLower(name.trim().replace(' ','_')).substring(0, 3);
			else
				shortNames[allStatCodes.length-1]=CMStrings.capitalizeAndLower(name.trim().replace(' ','_'));
			statAttributionDescriptions=Arrays.copyOf(statAttributionDescriptions, allStatCodes.length);
			statAttributionDescriptions[allStatCodes.length-1]=attDesc.toUpperCase();
			statCMMsgMapping=Arrays.copyOf(statCMMsgMapping, allStatCodes.length);
			statCMMsgMapping[allStatCodes.length-1]=cmmsgMap;
			rvsStatCMMsgMapping.put(Integer.valueOf(cmmsgMap), Integer.valueOf(allStatCodes.length-1));
		}
	}
}
