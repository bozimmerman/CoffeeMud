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
import com.planet_ink.coffee_mud.Common.DefaultCharStats;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Deity.DeityWorshipper;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/*
   Copyright 2005-2026 Bo Zimmerman

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
public interface CharStats extends CMCommon, Modifiable, DeityWorshipper
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
	/** stat constant for amount of doubt? */
	public static final int STAT_SAVE_DOUBT=31;
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
	/** stat constant for amount of faith? */
	public static final int STAT_FAITH=44;
	/** stat constant for recovery rate */
	public static final int STAT_RECOVERRATE5_ADJ=45;
	/** stat constant for xp adjustment % */
	public static final int STAT_XP_ADJ_PCT=46;
	/** stat constant for xp adjustment % */
	public static final int STAT_SAVE_POLYMORPH=47;
	/** constant for total number of stat codes */
	public final static int DEFAULT_NUM_STATS=48;

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
	 * Adjusts the value of one of the STAT_ constants, representing attributes,
	 * saving throws, and max attributes, from the CharStats interface.
	 * Using this method will also update the STAT_MAX_* attributes to make
	 * sure that they are not able to be trained upwards.
	 * @see CharStats
	 * @param statNum which STAT_ constant to adjust
	 * @param value the amount + or -, to adjust by
	 */
	public void adjStat(int statNum, int value);

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
	 * @param statName name of which constant to determine the STAT_ constant for
	 * @return the STAT_ constant value from CharStats interface
	 */
	public int getStatCode(String statName);

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
	public Iterable<CharClass> getCharClasses();

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
	 * Unpacks the set of character class objects stored here
	 * from a semicolon list of string names and level amounts
	 * @param classes the semicolon list of character class names
	 * @param levels the semicolon list of levels
	 */
	public void setAllClassInfo(String classes, String levels);

	/**
	 * Returns the enumerated set of character class names stored here
	 * as a semicolon list of string names and level amounts.
	 * @return the semicolon list of character class names and levels respectively
	 */
	public Pair<String,String> getAllClassInfo();

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
	 * @see #realGenderName()
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
	 * Returns the gender of the mob.
	 * @see #setGenderName(String)
	 * @return the apparent gender of the mob
	 */
	public String realGenderName();

	/**
	 * Based on the gender of the mob, returns M, N, or F, reflective
	 * of reproductive role.
	 *
	 * @return M, N, or F
	 */
	public char reproductiveCode();

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
	 * Based on the apparent gender of the mob, return the appropriate word "himself", "herself", or "itself".
	 * @return the gender-correct pronoun for this mob
	 */
	public String himherself();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "hisself", "herself", or "itself".
	 * @return the gender-correct pronoun for this mob
	 */
	public String hisherself();

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
	 * Based on the apparent gender of the mob, return the appropriate word "Man", "Woman", or "Man".
	 * @return the gender-correct term for this mob
	 */
	public String manwoman();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "Son", "Daughter", or "Child".
	 * @return the gender-correct term for this young mob
	 */
	public String sondaughter();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "Boy", "Girl", or "Child".
	 * @return the gender-correct term for this young mob
	 */
	public String boygirl();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "Mr.", or "Ms.".
	 * @return the gender-correct title for this mob
	 */
	public String MrMs();

	/**
	 * Based on the apparent gender of the mob, return the appropriate word "Mister", or "Madam".
	 * @return the gender-correct title for this mob
	 */
	public String MisterMadam();

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
	 * They begin with "X"+expertise code name + "+" for expertise level
	 * The are followed by the exact ID() of the ability, or * for All, or
	 * the skill classification name in uppercase, or the domain name in
	 * uppercase.
	 *
	 * @see CharStats#isAbilityAdjustment(String)
	 * @see CharStats#adjustAbilityAdjustment(String, int)
	 *
	 * @param ableID the ability ID, with an appropriate prefix
	 * @return the numeric value associated with the adjustment.
	 */
	public int getAbilityAdjustment(String ableID);

	/**
	 * Returns whether the mob has an ability adjustment.
	 *
	 * @see CharStats#getAbilityAdjustment(String)
	 * @see CharStats#adjustAbilityAdjustment(String, int)
	 *
	 * @param prefix the prefix, like PROF
	 * @return true if the adjustment is there.
	 */
	public boolean isAbilityAdjustment(final String prefix);

	/**
	 * Sets the adjustments to mob abilities, whether proficiency or
	 * something else numeric.
	 * Ability adjustments begin with "prof+" to adjust proficiency.
	 * They begin with "level+" to adjust the adjusted level.
	 * They begin with "X"+expertise code name + "+" for expertise level
	 * The are followed by the exact ID() of the ability, or * for All, or
	 * the skill classification name in uppercase, or the domain name in
	 * uppercase.
	 *
	 * @see CharStats#isAbilityAdjustment(String)
	 * @see CharStats#getAbilityAdjustment(String)
	 *
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

	/**
	 * General categories for the default Char Stat codes
	 */
	public static enum CStatType
	{
		/** BASE - meaning a basic physical attribute, like strength */
		BASE,
		/** MAX - a cap/max value on a physical attribute, like strength */
		MAX,
		/** SAVE - a saving throw */
		SAVE,
		/** MISC - doesn't fit in any above */
		MISC
	}

	/**
	 * Basic definitions of all the default char stat codes.
	 * *note Very likely this will be moved into lists.ini at some point, because too many words
	 * See the constructor below.
	 */
	public static enum DefCharStat
	{
		STRENGTH(STAT_STRENGTH,"S","STRENGTH","STRENGTH","STRONG",-1,CStatType.BASE),
		INTELLIGENCE(STAT_INTELLIGENCE,"I","INTELLIGENCE","INTELLIGENCE","INTELLIGENT",-1,CStatType.BASE),
		DEXTERITY(STAT_DEXTERITY,"D","DEXTERITY","DEXTERITY","DEXTEROUS",-1,CStatType.BASE),
		CONSTITUTION(STAT_CONSTITUTION,"C","CONSTITUTION","CONSTITUTION","HEALTHY",-1,CStatType.BASE),
		CHARISMA(STAT_CHARISMA,"CH","CHARISMA","CHARISMA","CHARISMATIC",-1,CStatType.BASE),
		WISDOM(STAT_WISDOM,"W","WISDOM","WISDOM","WISE",-1,CStatType.BASE),
		GENDER(STAT_GENDER,"G","GENDER","GENDER","",-1,CStatType.MISC),
		SAVE_PARALYSIS(STAT_SAVE_PARALYSIS,"vPY","PARALYSIS SAVE","PARALYSIS","RESISTANT TO PARALYSIS",CMMsg.TYP_PARALYZE,CStatType.SAVE),
		SAVE_FIRE(STAT_SAVE_FIRE,"vF","SAVE VS FIRE","FIRE","RESISTANT TO FIRE",CMMsg.TYP_FIRE,CStatType.SAVE),
		SAVE_COLD(STAT_SAVE_COLD,"vC","SAVE VS COLD","COLD","RESISTANT TO COLD",CMMsg.TYP_COLD,CStatType.SAVE),
		SAVE_WATER(STAT_SAVE_WATER,"vW","SAVE VS WATER","WATER","RESISTANT TO WATER",CMMsg.TYP_WATER,CStatType.SAVE),
		SAVE_GAS(STAT_SAVE_GAS,"vG","SAVE VS GAS","GAS","RESISTANT TO GAS",CMMsg.TYP_GAS,CStatType.SAVE),
		SAVE_MIND(STAT_SAVE_MIND,"vMI","SAVE VS MIND","MIND","RESISTANT TO MIND ATTACKS",CMMsg.TYP_MIND,CStatType.SAVE),
		SAVE_GENERAL(STAT_SAVE_GENERAL,"V","GENERAL SAVE","GENERAL","RESISTANT",CMMsg.TYP_GENERAL,CStatType.SAVE),
		SAVE_JUSTICE(STAT_SAVE_JUSTICE,"vJ","JUSTICE SAVE","JUSTICE","RESISTANT TO UNDIGNIFIED ATTACKS",CMMsg.TYP_JUSTICE,CStatType.SAVE),
		SAVE_ACID(STAT_SAVE_ACID,"vA","SAVE VS ACID","ACID","RESISTANT TO ACID",CMMsg.TYP_ACID,CStatType.SAVE),
		SAVE_ELECTRIC(STAT_SAVE_ELECTRIC,"vE","SAVE VS ELECTRICITY","ELECTRICITY","RESISTANT TO ELECTRICITY",CMMsg.TYP_ELECTRIC,CStatType.SAVE),
		SAVE_POISON(STAT_SAVE_POISON,"vP","SAVE VS POISON","POISON","RESISTANT TO POISON",CMMsg.TYP_POISON,CStatType.SAVE),
		SAVE_UNDEAD(STAT_SAVE_UNDEAD,"vU","SAVE VS UNDEAD","UNDEAD","RESISTANT TO UNDEAD",CMMsg.TYP_UNDEAD,CStatType.SAVE),
		SAVE_MAGIC(STAT_SAVE_MAGIC,"vM","SAVE VS MAGIC","MAGIC","RESISTANT TO MAGIC",CMMsg.TYP_CAST_SPELL,CStatType.SAVE),
		SAVE_DISEASE(STAT_SAVE_DISEASE,"vD","SAVE VS DISEASE","DISEASE","RESISTANT TO DISEASE",CMMsg.TYP_DISEASE,CStatType.SAVE),
		SAVE_TRAPS(STAT_SAVE_TRAPS,"vT","SAVE VS TRAPS","TRAPS","RESISTANT TO TRAPS",-1,CStatType.SAVE),
		MAX_STRENGTH_ADJ(STAT_MAX_STRENGTH_ADJ,"mS","MAX STRENGTH ADJ.","MAXSTRENGTH","POTENTIALLY STRONG",-1,CStatType.MAX),
		MAX_INTELLIGENCE_ADJ(STAT_MAX_INTELLIGENCE_ADJ,"mI","MAX INTELLIGENCE ADJ.","MAXINTELLIGENCE","POTENTIALLY INTELLIGENT",-1,CStatType.MAX),
		MAX_DEXTERITY_ADJ(STAT_MAX_DEXTERITY_ADJ,"mD","MAX DEXTERITY ADJ.","MAXDEXTERITY","POTENTIALLY DEXTROUS",-1,CStatType.MAX),
		MAX_CONSTITUTION_ADJ(STAT_MAX_CONSTITUTION_ADJ,"mC","MAX CONSTITUTION ADJ.","MAXCONSTITUTION","POTENTIALLY HEALTHY",-1,CStatType.MAX),
		MAX_CHARISMA_ADJ(STAT_MAX_CHARISMA_ADJ,"mCH","MAX CHARISMA ADJ.","MAXCHARISMA","POTENTIALLY CHARISMATIC",-1,CStatType.MAX),
		MAX_WISDOM_ADJ(STAT_MAX_WISDOM_ADJ,"mW","MAX WISDOM ADJ.","MAXWISDOM","POTENTIALLY WISE",-1,CStatType.MAX),
		AGE(STAT_AGE,"A","AGE","AGE","OLD",-1,CStatType.MISC),
		SAVE_DETECTION(STAT_SAVE_DETECTION,"vH","SAVE VS DETECTION","DETECTION","CONCEALED",-1,CStatType.SAVE),
		SAVE_OVERLOOKING(STAT_SAVE_OVERLOOKING,"vO","SAVE VS OVERLOOKING","OVERLOOKING","WATCHFUL",-1,CStatType.SAVE),
		SAVE_DOUBT(STAT_SAVE_DOUBT,"vB","SAVE VS CONVERSIONS","DOUBT","DOUBTFUL",-1,CStatType.SAVE),
		WEIGHTADJ(STAT_WEIGHTADJ,"Wa","WEIGHT ADJUSTMENT","WEIGHTADJ","",-1,CStatType.MISC),
		SAVE_BLUNT(STAT_SAVE_BLUNT,"vWB","SAVE VS BLUNT","SAVEBLUNT","RESISTANT TO BLUNT",-1,CStatType.SAVE),
		SAVE_PIERCE(STAT_SAVE_PIERCE,"vWP","SAVE VS PIERCE","SAVEPIERCE","RESISTANT TO PIERCE",-1,CStatType.SAVE),
		SAVE_SLASH(STAT_SAVE_SLASH,"vWS","SAVE VS SLASH","SAVESLASH","RESISTANT TO SLASH",-1,CStatType.SAVE),
		SAVE_SPELLS(STAT_SAVE_SPELLS,"vMS","SAVE VS SPELLS","SAVESPELLS","RESISTANT TO SPELLS",-1,CStatType.SAVE),
		SAVE_PRAYERS(STAT_SAVE_PRAYERS,"vMP","SAVE VS PRAYERS","SAVEPRAYERS","RESISTANT TO PRAYERS",-1,CStatType.SAVE),
		SAVE_SONGS(STAT_SAVE_SONGS,"vMG","SAVE VS SONGS","SAVESONGS","RESISTANT TO SONGS",-1,CStatType.SAVE),
		SAVE_CHANTS(STAT_SAVE_CHANTS,"vMC","SAVE VS CHANTS","SAVECHANTS","RESISTANT TO CHANTS",-1,CStatType.SAVE),
		CRIT_CHANCE_PCT_WEAPON(STAT_CRIT_CHANCE_PCT_WEAPON,"cRW","CRIT WEAPON CHANCE PCT","CRITPCTWEAPONS","BONUS TO WEAPON CRIT CHANCE",-1,CStatType.MISC),
		CRIT_CHANCE_PCT_MAGIC(STAT_CRIT_CHANCE_PCT_MAGIC,"cRM","CRIT MAGIC CHANCE PCT","CRITPCTMAGIC","BONUS TO MAGIC CRIT CHANCE",-1,CStatType.MISC),
		CRIT_DAMAGE_PCT_WEAPON(STAT_CRIT_DAMAGE_PCT_WEAPON,"cDW","CRIT WEAPON DAMAGE PCT","CRITDMGWEAPONS","BONUS TO WEAPON CRIT DAMAGE",-1,CStatType.MISC),
		CRIT_DAMAGE_PCT_MAGIC(STAT_CRIT_DAMAGE_PCT_MAGIC,"cDM","CRIT MAGIC DAMAGE PCT","CRITDMGMAGIC","BONUS TO MAGIC CRIT DAMAGE",-1,CStatType.MISC),
		FAITH(STAT_FAITH,"F","FAITH","FAITH","FAITHFUL",-1,CStatType.MISC),
		RECOVERRATE5_ADJ(STAT_RECOVERRATE5_ADJ,"RR","REJUVENATION RATE ADJ","REJUVRATE","REJUVENATINGLY DIFFERENT",-1,CStatType.MISC),
		XP_ADJ_PCT(STAT_XP_ADJ_PCT,"XP","XP ADJUSTMENT PCT","XPADJPCT","EXPERIENTIAL",-1,CStatType.MISC),
		SAVE_POLYMORPH(STAT_SAVE_POLYMORPH,"vY","SAVE VS POLYMORPH/PETRIFY","POLYMORPH","ACTUALIZED",CMMsg.TYP_POLYMORPH,CStatType.SAVE),
		;
		public int statCode;
		public String abbr;
		public String desc;
		public String name;
		public String attDesc;
		public int saveMsgCode;
		public CStatType type;

		/**
		 * Build a default stat code object from default data.
		 *
		 * @param statCode the hard coded stat code value, which could be ordinal I suppose
		 * @param abbr abbreviations of a stat code
		 * @param desc description of a stat code, which MAY have spaces
		 * @param name name of a stat code, which may NOT have spaces
		 * @param attDesc attribute description of someone with this stat, spaces allowed
		 * @param saveMsgCode a CMMsg type code that triggers a saving throw, or -1
		 * @param type one of the CStatType categories this code goes in
		 */
		private DefCharStat(final int statCode, final String abbr, final String desc, final String name,
						 final String attDesc, final int saveMsgCode, final CStatType type)
		{
			this.statCode=statCode;
			this.abbr = abbr;
			this.desc = desc;
			this.name = name;
			this.attDesc=attDesc;
			this.saveMsgCode=saveMsgCode;
			this.type = type;
			DEF_CHAR_STAT_NAMES_MAP.put(name,this);
			DEF_CHAR_STAT_DESCS_MAP.put(desc,this);
		}
	}

	/**
	 * Map of default char stat names to charstat objects, used ONLY during boot, as the ini
	 * files may adjust or add to the ACTUAL stat codes, which won't be reflected here.
	 */
	public static final Map<String,DefCharStat> DEF_CHAR_STAT_NAMES_MAP = new Hashtable<String,DefCharStat>();

	/**
	 * Map of default char stat descriptions to charstat objects, used ONLY during boot, as the ini
	 * files may adjust or add to the ACTUAL stat codes, which won't be reflected here.
	 */
	public static final Map<String,DefCharStat> DEF_CHAR_STAT_DESCS_MAP = new Hashtable<String,DefCharStat>();

	/**
	 * Global character stat code data collector
	 * @author Bo Zimmerman
	 */
	public class CODES
	{
		private static CODES[] insts=new CODES[256];

		private int[]			baseStatCodes				= new int[0];
		private int[]			maxStatCodes				= new int[0];
		private int[]			maxBaseCrossCodes			= new int[0];
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
			for(final DefCharStat stat : DefCharStat.values())
				addAllStat(stat.statCode,stat.abbr,stat.desc,stat.name,stat.attDesc,stat.saveMsgCode,stat.type);
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
					DefCharStat oldStat = CharStats.DEF_CHAR_STAT_NAMES_MAP.get(repStat);
					if(oldStat == null)
						oldStat = CharStats.DEF_CHAR_STAT_DESCS_MAP.get(repStat);
					if(oldStat == null)
					{
						Log.errOut("CharStats","Bad coffeemud.ini charstat row, bad stat name: "+repStat);
						continue;
					}
					oldStatCode=oldStat.statCode;
					type="REPLACE";
				}
				final String abbr=V.get(1);
				final String desc=V.get(2).toUpperCase();
				final String adj=V.get(3).toUpperCase();
				if(type.equalsIgnoreCase("BASE"))
				{
					final int baseStatCode=allStatCodes.length;
					addAllStat(baseStatCode,abbr,desc,stat,adj,-1,CStatType.BASE);
					final int maxStatCode=allStatCodes.length;
					addAllStat(maxStatCode, "m"+abbr, "MAX "+stat+" ADJ.", "MAX"+stat, "POTENTIALLY "+adj, -1,CStatType.MAX);
				}
				else
				if(type.equalsIgnoreCase("SAVE"))
					addAllStat(allStatCodes.length,abbr,desc,stat,adj,-1,CStatType.SAVE);
				else
				if(type.equalsIgnoreCase("OTHER"))
					addAllStat(allStatCodes.length,abbr,desc,stat,adj,-1,CStatType.MISC);
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

		public static CODES c(final byte c)
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
				for(final int code : c.baseStatCodes)
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
		public static boolean isBASE(final int code)
		{
			return c().isBase(code);
		}

		/**
		 * Returns whether the given code is a base stat
		 *
		 * @param code the STAT code to check
		 * @return whether the given code is a base stat
		 */
		public boolean isBase(final int code)
		{
			if(code<isBaseStatCode.length)
				return isBaseStatCode[code];
			return false;
		}

		/**
		 * Returns the code for the base code that matches the given max adj code
		 * Returns the code for the max adj code that matches the given base code
		 * Returns -1 if the code is not a max adj code.
		 * @param max the MAX state adjustment code
		 * @return the translated code
		 */
		public static int toMAXBASE(final int max)
		{
			return c().toMaxBase(max);
		}

		/**
		 * Returns the code for the base code that matches the given max adj code
		 * Returns the code for the max adj code that matches the given base code
		 * Returns -1 if the code is not a max adj code.
		 * @param max the MAX state adjustment code
		 * @return the translated code
		 */
		public int toMaxBase(final int max)
		{
			if(max<maxBaseCrossCodes.length)
				return maxBaseCrossCodes[max];
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
		public static String NAME(final int code)
		{
			return c().name(code);
		}

		/**
		 * Returns the short name of the stat code
		 *
		 * @param code the stat code
		 * @return the short name of the stat code
		 */
		public static String SHORTNAME(final int code)
		{
			return c().shortName(code);
		}

		/**
		 * Returns the name of the stat code
		 *
		 * @param code the stat code
		 * @return the name of the stat code
		 */
		public String name(final int code)
		{
			if(code < statNames.length)
				return statNames[code];
			return "";
		}

		/**
		 * Returns the name of the stat code
		 *
		 * @param code the stat code
		 * @return the name of the stat code
		 */
		public String shortName(final int code)
		{
			if(code < shortNames.length)
				return shortNames[code];
			return "";
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
		public static String DESC(final int code)
		{
			return c().desc(code);
		}

		/**
		 * Returns the description of the stat code
		 *
		 * @param code the stat code
		 * @return the description of the stat code
		 */
		public String desc(final int code)
		{
			if(code < statDescriptions.length)
				return statDescriptions[code];
			return "";
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
		public static String ABBR(final int code)
		{
			return c().abbr(code);
		}

		/**
		 * Returns the abbreviation of the stat code
		 *
		 * @param code the stat code
		 * @return the abbreviation of the stat code
		 */
		public String abbr(final int code)
		{
			if(code < statAbbreviations.length)
				return statAbbreviations[code];
			return "";
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
		public static String ATTDESC(final int code)
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
		public static int CMMSGMAP(final int code)
		{
			return c().statCMMsgMapping[code];
		}

		/**
		 * Returns the Reverse CMMsg mapping of the stat from MsgCode -&gt; Stat Code
		 * @param code the Stat mapping for the given CMMsg code or -1
		 * @return the Stat mapping of the CMMsg
		 */
		public static int RVSCMMSGMAP(final int code)
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
		public int find(final String name, final boolean exactOnly)
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
		public static int findWhole(final String name, final boolean exactOnly)
		{
			return c().find(name, exactOnly);
		}

		/**
		 * Adds a new miscellaneous stat to this object for all mobs and players to share
		 * @param code the official hard coded stat code
		 * @param abbr 1-3 letter short code for this stat
		 * @param desc longer description of this stat
		 * @param name space-free coded name of this stat
		 * @param attDesc description of someone with this stat in abundance
		 * @param cmmsgMap a CMMsg message code that saves with this stat
		 * @param base true if the code is a base stat, false if a save or something else
		 * @param type the CStatType category of this
		 */
		public void addAllStat(final int code, final String abbr, final String desc, final String name, final String attDesc, final int cmmsgMap, final CStatType type)
		{
			switch(type)
			{
			case BASE:
			{
				baseStatCodes=Arrays.copyOf(baseStatCodes, baseStatCodes.length+1);
				baseStatCodes[baseStatCodes.length-1]=code;
				baseStatNames=Arrays.copyOf(baseStatNames, baseStatNames.length+1);
				baseStatNames[baseStatNames.length-1]=name;
				break;
			}
			case SAVE:
			{
				savingThrowCodes=Arrays.copyOf(savingThrowCodes, savingThrowCodes.length+1);
				savingThrowCodes[savingThrowCodes.length-1]=code;
				break;
			}
			case MAX:
			{
				int baseCode = -1;
				try
				{
					for(int x=0;x<baseStatNames.length;x++)
						if(name.toUpperCase().indexOf(baseStatNames[x].toUpperCase())>=0)
							baseCode = baseStatCodes[x];
					if(baseCode <0)
						throw new IllegalArgumentException("Unknown baseStatName "+name);
					maxStatCodes=Arrays.copyOf(maxStatCodes, maxStatCodes.length+1);
					maxStatCodes[maxStatCodes.length-1]=code;
					if(code >= maxBaseCrossCodes.length)
						maxBaseCrossCodes=Arrays.copyOf(maxBaseCrossCodes, code+1);
					maxBaseCrossCodes[code]=baseCode;
					maxBaseCrossCodes[baseCode]=code;
				}
				catch(final Exception e)
				{
				}
				break;
			}
			case MISC:
				break;
			}
			if(code >= allStatCodes.length)
				allStatCodes=Arrays.copyOf(allStatCodes, code+1);
			allStatCodes[code]=allStatCodes.length-1;
			isBaseStatCode=Arrays.copyOf(isBaseStatCode, allStatCodes.length);
			isBaseStatCode[code]=(type == CStatType.BASE);
			statAbbreviations=Arrays.copyOf(statAbbreviations, allStatCodes.length);
			statAbbreviations[code]=abbr;
			statDescriptions=Arrays.copyOf(statDescriptions, allStatCodes.length);
			statDescriptions[code]=desc.toUpperCase().trim();
			statNames=Arrays.copyOf(statNames, allStatCodes.length);
			statNames[code]=name.toUpperCase().trim().replace(' ','_');
			shortNames=Arrays.copyOf(shortNames, allStatCodes.length);
			if(name.length()>3)
				shortNames[code]=CMStrings.capitalizeAndLower(name.trim().replace(' ','_')).substring(0, 3);
			else
				shortNames[code]=CMStrings.capitalizeAndLower(name.trim().replace(' ','_'));
			statAttributionDescriptions=Arrays.copyOf(statAttributionDescriptions, allStatCodes.length);
			statAttributionDescriptions[code]=attDesc.toUpperCase();
			statCMMsgMapping=Arrays.copyOf(statCMMsgMapping, allStatCodes.length);
			statCMMsgMapping[code]=cmmsgMap;
			rvsStatCMMsgMapping.put(Integer.valueOf(cmmsgMap), Integer.valueOf(allStatCodes.length-1));
		}
	}
}
