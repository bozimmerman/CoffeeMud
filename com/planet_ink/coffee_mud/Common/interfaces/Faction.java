package com.planet_ink.coffee_mud.Common.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultFaction;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

/*
 * Copyright 2000-2008 Bo Zimmerman Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
/**
 * A Faction is an arbitrary numeric range, where different mobs/players can be
 * within that range, if they have the faction at all. Factions can be
 * programmatically set to change due to events that occur to/around the mob,
 * and adjust themselves relative to other factions. Subsets of the faction can
 * be given readable names for display to the user.
 * 
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#fetchFaction(String)
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addFaction(String, int)
 */
public interface Faction extends CMCommon, MsgListener
{
    /**
     * Initializes a new faction with default values
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#initializeFaction(StringBuffer, String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
     * @param aname the factionID (and default name)
     */
    public void initializeFaction(String aname);

    /**
     * Initializes a new faction from a faction.ini properties formatted document, 
     * and a given new faction ID
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#initializeFaction(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#getINIDef(String, String)
     * @param file the ini properties style document
     * @param fID the new factionID
     */
    public void initializeFaction(StringBuffer file, String fID);

    /**
     * Returns the value of a given internal faction variable.  
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#TAG_NAMES
     * @param tag the tag to get the value of
     * @return the value of the given tag
     */
    public String getTagValue(String tag);

    /**
     * Retreives an entry for an ini properties definition document that describes this faction.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#getINIDef(String, String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#initializeFaction(StringBuffer, String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#TAG_NAMES
     * @param tag the tag to retreive a properties definition for
     * @param delimeter if the tag represents a list, this is the delimiter for entries.
     * @return the ini properties definition entry for the tag
     */
    public String getINIDef(String tag, String delimeter);

    /**
     * Checks to see if the given mob has this faction.  Same as checking if
     * mob.fetchFaction(this.factionID())!=Integer.MAX_VALUE.
     * @param mob the mob to check
     * @return true if the mob has this faction, false otherwise
     */
    public boolean hasFaction(MOB mob);

    /**
     * Returns the given faction value, as a percent from minimum of the range
     * of this faction
     * @param faction the faction value to convert to a percent
     * @return the percentage value (0-100)
     */
    public int asPercent(int faction);

    /**
     * Returns the given value faction value, as a percent from average of the
     * range values of this faction.
     * @param faction the faction value to convert to a percent
     * @return the percentage value (0-100)
     */
    public int asPercentFromAvg(int faction);

    /**
     * Returns a random value within the valid range of this faction
     * @return a random valid value
     */
    public int randomFaction();

    /**
     * The official, unique faction id of this faction.  FactionIDs are usually
     * the CoffeeMud VFS path from the resources directory, of the properties ini
     * file that defines the faction.  The ID (and therefore the properties file location)
     * should not be changed once a faction is "deployed".
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setFactionID(String)
     * @return the unique id of this faction
     */
    public String factionID();

    /**
     * Sets the official, unique faction id of this faction.  FactionIDs are usually
     * the CoffeeMud VFS path from the resources directory, of the properties ini
     * file that defines the faction.  The ID (and therefore the properties file location)
     * should not be changed once a faction is "deployed".
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#factionID()
     * @param newStr the new unique id of this faction
     */
    public void setFactionID(String newStr);

    /**
     * The friendly, displayable name of this faction.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#setName(String)
     * @return the name of this faction
     */
    public String name();

    /**
     * Sets the friendly, displayable name of this faction.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#name()
     * @param newStr the new name of this faction
     */
    public void setName(String newStr);

    /**
     * Gets the filename of a file, from the resources directory,
     * that is displayed to users when they are given the choice
     * of a starting value to this faction.  Requires more than
     * one choice range be available.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#findChoices(MOB)
     * @return the filename of the choice description file
     */
    public String choiceIntro();

    /**
     * Sets the filename of a file, from the resources directory,
     * that is displayed to users when they are given the choice
     * of a starting value to this faction.  Requires more than
     * one choice range be available.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#findChoices(MOB)
     * @param newStr the new filename of the choice description file
     */
    public void setChoiceIntro(String newStr);

    /**
     * Gets the lowest absolute range value 
     * @return the lowest absolute range value
     */
    public int minimum();

    /**
     * Gets the median absolute range value
     * @return the median absolute range value
     */
    public int middle();

    /**
     * Returns the difference between the highest and lowest range value
     * @return the difference between the highest and lowest range value
     */
    public int difference();

    /**
     * Returns the highest absolute range value
     * @return the highest absolute range value
     */
    public int maximum();

    /**
     * Returns the string code describing how a faction-holders experience
     * changes from killing another faction holder affect his own faction value.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_NAMES
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_DESCS
     * @return the string code for xp changes->faction changes
     */
    public String experienceFlag();

    /**
     * Sets the string code describing how a faction-holders experience
     * changes from killing another faction holder affect his own faction value.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_NAMES
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#EXPAFFECT_DESCS
     * @param newStr the new string code for xp changes->faction changes
     */
    public void setExperienceFlag(String newStr);

    /**
     * Returns whether this faction is displayed in the player Score command.
     * @return true if displayed in Score, false otherwise
     */
    public boolean showInScore();

    /**
     * Sets whether this faction is displayed in the player Score command.
     * @param truefalse true if displayed in Score, false otherwise
     */
    public void setShowInScore(boolean truefalse);

    /**
     * Returns whether this factions value is shown in certain special admins commands.
     * @return true if displayed in special admin commands, false otherwise
     */
    public boolean showInSpecialReported();

    /**
     * Sets whether this factions value is shown in certain special admins commands.
     * @param truefalse true if displayed in special admin commands, false otherwise
     */
    public void setShowInSpecialReported(boolean truefalse);

    /**
     * Returns whether this factions value is shown as a line item in mob editors
     * @return true if displayed in mob editors, false otherwise
     */
    public boolean showInEditor();

    /**
     * Sets whether this factions value is shown as a line item in mob editors
     * @param truefalse true if displayed in mob editors, false otherwise
     */
    public void setShowInEditor(boolean truefalse);

    /**
     * Returns whether this factions value is shown in player Factions command
     * @return true if displayed in factions command, false otherwise
     */
    public boolean showInFactionsCommand();

    /**
     * Sets whether this factions value is shown in player Factions command
     * @param truefalse true if displayed in factions command, false otherwise
     */
    public void setShowInFactionsCommand(boolean truefalse);

    /**
     * Returns the default faction mask/value list, which is applied whenever
     * a Faction Change Event applies a Faction Add command.
     * A default faction mask/value is defined as a number, along with an
     * optional Zapper mask describing to whom the value is applied.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changes()
     * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
     * @return the default faction mask/value list
     */
    public Enumeration defaults();

    /**
     * Returns the default faction value that applies to the given mob.
     * This method is called when a Faction Change event applies a 
     * Faction Add command. Returns Integer.MAX_VALUE if no default
     * value applies to this mob.
     * @param mob the mob to find a default faction value for
     * @return the faction value that applies, or Integer.MAX_VALUE
     */
    public int findDefault(MOB mob);

    /**
     * Sets the default faction mask/value list, which is applied whenever
     * a Faction Change Event applies a Faction Add command.
     * A default faction mask/value is defined as a number, along with an
     * optional Zapper mask describing to whom the value is applied.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Faction#changes()
     * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
     * @param v the new default faction mask/value list
     */
    public void setDefaults(Vector v);

    /**
     * Returns the automatic default faction mask/value list, which is 
     * possibly applied whenever a mob or player is brought to life for
     * the first time. An automatic default faction mask/value is defined 
     * as a number, along with an optional Zapper mask describing to whom 
     * the value is applied.
     * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
     * @return the automatic default faction mask/value list
     */
    public Enumeration autoDefaults();

    /**
     * Returns the automatic default faction value that applies to the
     * given mob.  This method is called when a mob is brought into the
     * world.  Returns Integer.MAX_VALUE if no default value applies 
     * to this mob.
     * @param mob the mob to find a default value of this faction for.
     * @return the value to give to the given mob, or Integer.MAX_VALUE
     */
    public int findAutoDefault(MOB mob);

    /**
     * Sets the automatic default faction mask/value list, which is 
     * possibly applied whenever a mob or player is brought to life for
     * the first time. An automatic default faction mask/value is defined 
     * as a number, along with an optional Zapper mask describing to whom 
     * the value is applied.
     * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
     * @param v the new automatic default faction mask/value list
     */
    public void setAutoDefaults(Vector v);

    /**
     * A modifier of the base amount of faction value change, namely 100.
     * @return a modifier of the base amount of faction change
     */
    public double rateModifier();

    /**
     * Sets the modifier of the base amount of faction value change, namely 100.
     * @param d the new modifier of the base amount of faction value change
     */
    public void setRateModifier(double d);

    /**
     * 
     * @return
     */
    public Enumeration choices();

    /**
     * 
     * @param mob
     * @return
     */
    public Vector findChoices(MOB mob);

    /**
     * 
     * @param v
     */
    public void setChoices(Vector v);

    /**
     * 
     * @return
     */
    public Enumeration ranges();

    /**
     * 
     * @param faction
     * @return
     */
    public FactionRange fetchRange(int faction);

    /**
     * 
     * @param faction
     * @return
     */
    public String fetchRangeName(int faction);

    /**
     * 
     * @param key
     * @return
     */
    public FactionRange addRange(String key);

    /**
     * 
     * @param FR
     * @return
     */
    public boolean delRange(FactionRange FR);
    
    /**
     * 
     * @return
     */
    public Enumeration changeEventKeys();

    /**
     * 
     * @param key
     * @return
     */
    public FactionChangeEvent findChangeEvent(Ability key);

    /**
     * 
     * @param key
     * @return
     */
    public FactionChangeEvent getChangeEvent(String key);

    /**
     * 
     * @param key
     * @return
     */
    public FactionChangeEvent addChangeEvent(String key);

    /**
     * 
     * @param eventKey
     * @return
     */
    public boolean delChangeEvent(String eventKey);

    /**
     * 
     * @return
     */
    public Enumeration factors();
    
    /**
     * 
     * @param o
     */
    public boolean delFactor(Object[] o);

    /**
     * 
     * @param x
     */
    public Object[] getFactor(int x);
    
    /**
     * 
     * @param o
     */
    public Object[] addFactor(double gain, double loss, String mask);
    
    /**
     * 
     * @param mob
     * @param gain
     * @return
     */
    public double findFactor(MOB mob, boolean gain);

    /**
     * 
     * @return
     */
    public Enumeration relationFactions();

    /**
     * 
     * @param factionID
     * @return
     */
    public boolean delRelation(String factionID);
    
    /**
     * 
     * @param factionID
     * @param relation
     * @return
     */
    public boolean addRelation(String factionID, double relation);
    
    /**
     * 
     * @param factionID
     * @return
     */
    public double getRelation(String factionID); 
    
    /**
     * 
     * @return
     */
    public Enumeration abilityUsages();

    /**
     * 
     * @param A
     * @return
     */
    public String usageFactors(Ability A);

    /**
     * 
     * @param A
     * @return
     */
    public boolean hasUsage(Ability A);

    /**
     * 
     * @param mob
     * @param A
     * @return
     */
    public boolean canUse(MOB mob, Ability A);

    /**
     * 
     * @param key
     * @return
     */
    public FactionAbilityUsage addAbilityUsage(String key);

    /**
     * 
     * @param x
     * @return
     */
    public FactionAbilityUsage getAbilityUsage(int x);
    
    /**
     * 
     * @param usage
     * @return
     */
    public boolean delAbilityUsage(FactionAbilityUsage usage);

    /**
     * 
     * @param source
     * @param target
     * @param event
     */
    public void executeChange(MOB source, MOB target, FactionChangeEvent event);

    /**
     * 
     * @return
     */
    public String ALL_CHANGE_EVENT_TYPES();
    
    /**
     * 
     * @author Bo Zimmerman
     *
     */
    public static interface FactionChangeEvent
    {
        /**
         * 
         * @return
         */
        public String eventID();

        /**
         * 
         * @return
         */
        public String flagCache();

        /**
         * 
         * @return
         */
        public int IDclassFilter();

        /**
         * 
         * @return
         */
        public int IDflagFilter();

        /**
         * 
         * @return
         */
        public int IDdomainFilter();

        /**
         * 
         * @return
         */
        public int direction();

        /**
         * 
         * @return
         */
        public double factor();

        /**
         * 
         * @return
         */
        public String zapper();

        /**
         * 
         * @return
         */
        public boolean outsiderTargetOK();

        /**
         * 
         * @return
         */
        public boolean selfTargetOK();

        /**
         * 
         * @return
         */
        public boolean just100();

        /**
         * 
         * @param newVal
         */
        public void setDirection(int newVal);

        /**
         * 
         * @param newVal
         */
        public void setFactor(double newVal);

        /**
         * 
         * @param newVal
         */
        public void setZapper(String newVal);


        /**
         * 
         * @return
         */
        public String toString();

        /**
         * 
         * @param newID
         * @return
         */
        public boolean setEventID(String newID);

        /**
         * 
         * @param newFlagCache
         */
        public void setFlags(String newFlagCache);

        /**
         * 
         * @param mob
         * @return
         */
        public boolean applies(MOB mob);
        
        public static final int FACTION_UP=0;
        public static final int FACTION_DOWN=1;
        public static final int FACTION_OPPOSITE=2;
        public static final int FACTION_MINIMUM=3;
        public static final int FACTION_MAXIMUM=4;
        public static final int FACTION_REMOVE=5;
        public static final int FACTION_ADD=6;
        public static final int FACTION_AWAY=7;
        public static final int FACTION_TOWARD=8;
        public static final String[] FACTION_DIRECTIONS={"UP","DOWN","OPPOSITE","MINIMUM","MAXIMUM","REMOVE","ADD","AWAY","TOWARD"};
        public static final String[] VALID_FLAGS={"OUTSIDER","SELFOK","JUST100"};
        public static final String[] MISC_TRIGGERS={"MURDER","TIME","ADDOUTSIDER"};
    }
    

    /**
     * 
     * @author Bo Zimmerman
     *
     */    
    public static interface FactionRange
    {
        /**
         * 
         * @return
         */
        public String rangeID();

        /**
         * 
         * @return
         */
        public int low();

        /**
         * 
         * @return
         */
        public int high();

        /**
         * 
         * @return
         */
        public String name();

        /**
         * 
         * @return
         */
        public String codeName();

        /**
         * 
         * @return
         */
        public int alignEquiv();

        /**
         * 
         * @return
         */
        public Faction myFaction();

        /**
         * 
         * @param newVal
         */
        public void setLow(int newVal);

        /**
         * 
         * @param newVal
         */
        public void setHigh(int newVal);

        /**
         * 
         * @param newVal
         */
        public void setName(String newVal);

        /**
         * 
         * @param newVal
         */
        public void setCodeName(String newVal);

        /**
         * 
         * @param newVal
         */
        public void setAlignEquiv(int newVal);

        /**
         * 
         * @return
         */
        public String toString();

        /**
         * 
         * @return
         */
        public int random();
    }
    
    /**
     * 
     * @author Bo Zimmerman
     *
     */
    public static interface FactionAbilityUsage
    {
        /**
         * 
         * @return
         */
        public String usageID();

        /**
         * 
         * @return
         */
        public boolean possibleAbilityID();

        /**
         * 
         * @return
         */
        public int type();

        /**
         * 
         * @return
         */
        public int domain();

        /**
         * 
         * @return
         */        
        public int flag();

        /**
         * 
         * @return
         */
        public int low();

        /**
         * 
         * @return
         */
        public int high();

        /**
         * 
         * @return
         */
        public int notflag();

        /**
         * 
         * @param newVal
         */
        public void setLow(int newVal);

        /**
         * 
         * @param newVal
         */
        public void setHigh(int newVal);
        
        /**
         * 
         * @param str
         * @return
         */
        public Vector setAbilityFlag(String str);
        /**
         * 
         * @return
         */
        public String toString();
    }
    
    public final static int ALIGN_INDIFF=0;
    public final static int ALIGN_EVIL=1;
    public final static int ALIGN_NEUTRAL=2;
    public final static int ALIGN_GOOD=3;
    
    public final static String[] ALIGN_NAMES={"","EVIL","NEUTRAL","GOOD"};
    
    public final static String[] EXPAFFECT_NAMES={"NONE","EXTREME","HIGHER","LOWER","FOLLOWHIGHER","FOLLOWLOWER"};
    public final static String[] EXPAFFECT_DESCS={"None","Proportional (Extreme)","Higher (mine)","Lower (mine)","Higher (other)","Lower (other)"};
    public final static int TAG_NAME=0;
    public final static int TAG_MINIMUM=1;
    public final static int TAG_MAXIMUM=2;
    public final static int TAG_SCOREDISPLAY=3;
    public final static int TAG_SPECIALREPORTED=4;
    public final static int TAG_EDITALONE=5;
    public final static int TAG_DEFAULT=6;
    public final static int TAG_AUTODEFAULTS=7;
    public final static int TAG_AUTOCHOICES=8;
    public final static int TAG_CHOICEINTRO=9;
    public final static int TAG_RATEMODIFIER=10;
    public final static int TAG_EXPERIENCE=11;
    public final static int TAG_RANGE_=12;
    public final static int TAG_CHANGE_=13;
    public final static int TAG_ABILITY_=14;
    public final static int TAG_FACTOR_=15;
    public final static int TAG_RELATION_=16;
    public final static int TAG_SHOWINFACTIONSCMD=17;
    public final static String[] TAG_NAMES={"NAME","MINIMUM","MAXIMUM","SCOREDISPLAY","SPECIALREPORTED","EDITALONE","DEFAULT","AUTODEFAULTS",
            "AUTOCHOICES","CHOICEINTRO","RATEMODIFIER","EXPERIENCE","RANGE*","CHANGE*","ABILITY*","FACTOR*","RELATION*","SHOWINFACTIONSCMD"};
}
