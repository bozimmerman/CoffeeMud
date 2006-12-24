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
public interface Faction extends CMCommon, MsgListener
{
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
    public final static String[] ALL_TAGS={"NAME","MINIMUM","MAXIMUM","SCOREDISPLAY","SPECIALREPORTED","EDITALONE","DEFAULT",
        "AUTODEFAULTS","AUTOCHOICES","CHOICEINTRO","RATEMODIFIER","EXPERIENCE","RANGE*","CHANGE*","ABILITY*","FACTOR*","RELATION*",
        "SHOWINFACTIONSCMD"};

    public void initializeFaction(String aname);
    public void initializeFaction(StringBuffer file, String fID);
    public void setFactionID(String newStr);
    public void setName(String newStr);
    public void setChoiceIntro(String newStr);
    public void setMinimum(int newVal);
    public void setMiddle(int newVal);
    public void setDifference(int newVal);
    public void setMaximum(int newVal);
    public void setHighest(int newVal);
    public void setLowest(int newVal);
    public String factionID();
    public String name();
    public String choiceIntro();
    public int minimum();
    public int middle();
    public int difference();
    public int maximum();
    public int highest();
    public int lowest();
    public String experienceFlag();
    public boolean showinscore();
    public boolean showinspecialreported();
    public boolean showineditor();
    public boolean showinfactionscommand();
    public Vector ranges();
    public Vector defaults();
    public Vector autoDefaults();
    public double rateModifier();
    public Hashtable Changes();
    public Vector factors();
    public Hashtable relations();
    public Vector abilityUsages();
    public Vector choices();
    public void setExperienceFlag(String newStr);
    public void setShowinscore(boolean truefalse);
    public void setShowinspecialreported(boolean truefalse);
    public void setShowineditor(boolean truefalse);
    public void setShowinfactionscommand(boolean truefalse);
    public void setChoices(Vector v);
    public void setAutoDefaults(Vector v);
    public void setDefaults(Vector v);
    public void setRateModifier(double d);
    
    
    public String getTagValue(String tag);
    public String getINIDef(String tag, String delimeter);
    public FactionChangeEvent findChangeEvent(Ability key); 
    public FactionChangeEvent findChangeEvent(String key);
    public FactionRange fetchRange(int faction);
    public String fetchRangeName(int faction);
    public int asPercent(int faction);
    public int asPercentFromAvg(int faction);
    public int randomFaction();
    public int findDefault(MOB mob);
    public int findAutoDefault(MOB mob);
    public boolean hasFaction(MOB mob);
    public boolean hasUsage(Ability A);
    public boolean canUse(MOB mob, Ability A);
    public double findFactor(MOB mob, boolean gain);
    public Vector findChoices(MOB mob);
    public void executeChange(MOB source, MOB target, FactionChangeEvent event);
    public String usageFactors(Ability A); 
    public String ALL_CHANGE_EVENT_TYPES();
    
    public FactionChangeEvent newChangeEvent(String key);
    public FactionChangeEvent newChangeEvent();
    public static interface FactionChangeEvent 
    {
        public String eventID();
        public String flagCache();
        public int IDclassFilter();
        public int IDflagFilter();
        public int IDdomainFilter();
        public int direction();
        public double factor();
        public String zapper();
        public boolean outsiderTargetOK();
        public boolean selfTargetOK();
        public boolean just100();
        public void setEventID(String newVal);
        public void setFlagCache(String newVal);
        public void setIDclassFilter(int newVal);
        public void setIDflagFilter(int newVal);
        public void setIDdomainFilter(int newVal);
        public void setDirection(int newVal);
        public void setFactor(double newVal);
        public void setZapper(String newVal);
        public void setOutsiderTargetOK(boolean newVal);
        public void setSelfTargetOK(boolean newVal);
        public void setJust100(boolean newVal);
        public String toString();
        public boolean setFilterID(String newID);
        public boolean setDirection(String d);
        public void setFlags(String newFlagCache);
        public boolean applies(MOB mob);

        public static final int FACTION_UP = 0;
        public static final int FACTION_DOWN = 1;
        public static final int FACTION_OPPOSITE = 2;
        public static final int FACTION_MINIMUM = 3;
        public static final int FACTION_MAXIMUM = 4;
        public static final int FACTION_REMOVE = 5;
        public static final int FACTION_ADD = 6;
        public static final int FACTION_AWAY = 7;
        public static final int FACTION_TOWARD= 8;
        public static final String[] FACTION_DIRECTIONS={
            "UP",
            "DOWN",
            "OPPOSITE",
            "MINIMUM",
            "MAXIMUM",
            "REMOVE",
            "ADD",
            "AWAY",
            "TOWARD"
        };
        public static final String[] VALID_FLAGS={
            "OUTSIDER","SELFOK","JUST100"
        };
        public static final String[] MISC_TRIGGERS={
            "MURDER","TIME","ADDOUTSIDER"
        };
    }
    
    public FactionRange newRange(String key);
    public static interface FactionRange
    {
        public String rangeID();
        public int low();
        public int high();
        public String name();
        public String codeName();
        public int alignEquiv();
        public Faction myFaction();
        public void setRangeID(String newVal);
        public void setLow(int newVal);
        public void setHigh(int newVal);
        public void setName(String newVal);
        public void setCodeName(String newVal);
        public void setAlignEquiv(int newVal);

        public String toString();
        public int random();
    }
    
    public FactionAbilityUsage newAbilityUsage(String key);
    public FactionAbilityUsage newAbilityUsage();
    public static interface FactionAbilityUsage
    {
        public String usageID();
        public boolean possibleAbilityID();
        public int type();
        public int domain();
        public int flag();
        public int low();
        public int high();
        public int notflag();
        
        public void setUsageID(String newVal);
        public void setPossibleAbilityID(boolean truefalse);
        public void setType(int newVal);
        public void setDomain(int newVal);
        public void setFlag(int newVal);
        public void setLow(int newVal);
        public void setHigh(int newVal);
        public void setNotflag(int newVal);
        
        public String toString();
        public Vector setAbilityFlag(String str);
    }
}
