package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.lang.reflect.Modifier;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;


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
public class CMLib 
{
    public CMLib(){super();}
    static final long serialVersionUID=42;
    public String getClassName(){return "CMLib";}
    private static CMLib inst=new CMLib();
    public static CMLib instance(){return inst;}
    
    public static final int LIBRARY_DATABASE=0;
    public static final int LIBRARY_THREADS=1;
    public static final int LIBRARY_INTERMUD=2;
    public static final int LIBRARY_HTTP=3;
    public static final int LIBRARY_LISTER=4;
    public static final int LIBRARY_MONEY=5;
    public static final int LIBRARY_SHOPS=6;
    public static final int LIBRARY_COMBAT=7;
    public static final int LIBRARY_HELP=8;
    public static final int LIBRARY_TRACKING=9;
    public static final int LIBRARY_MASKING=10;
    public static final int LIBRARY_CHANNELS=11;
    public static final int LIBRARY_COMMANDS=12;
    public static final int LIBRARY_ENGLISH=13;
    public static final int LIBRARY_SLAVERY=14;
    public static final int LIBRARY_JOURNALS=15;
    public static final int LIBRARY_FLAGS=16;
    public static final int LIBRARY_OBJBUILDERS=17;
    public static final int LIBRARY_SESSIONS=18;
    public static final int LIBRARY_TELNET=19;
    public static final int LIBRARY_XML=20;
    public static final int LIBRARY_SOCIALS=21;
    public static final int LIBRARY_UTENSILS=22;
    public static final int LIBRARY_STATS=23;
    public static final int LIBRARY_MAP=24;
    public static final int LIBRARY_QUEST=25;
    public static final int LIBRARY_ABLEMAP=26;
    public static final int LIBRARY_ENCODER=27;
    public static final int LIBRARY_SMTP=28;
    public static final int LIBRARY_DICE=29;
    public static final int LIBRARY_FACTIONS=30;
    public static final int LIBRARY_CLANS=31;
    public static final int LIBRARY_POLLS=32;
    public static final int LIBRARY_TIME=33;
    public static final int LIBRARY_COLOR=34;
    public static final int LIBRARY_TOTAL=35;

    private static final CMObject[] libraries=new CMObject[LIBRARY_TOTAL];
    private static boolean[] registered=new boolean[LIBRARY_TOTAL];
    
    public static CMath math(){return CMath.instance();}
    public static CMParms parms(){return CMParms.instance();}
    public static CMStrings strings(){return CMStrings.instance();}
    public static CMClass classes(){return CMClass.instance();}
    public static CMSecurity security(){return CMSecurity.instance();}
    public static Directions directions(){return Directions.instance();}
    public static Log log(){return Log.instance();}
    public static Resources resources(){return Resources.instance();}
    public static CMProps props(){return CMProps.instance();}
    public static CMLib libraries(){return CMLib.instance();}
    public static CMFile newFile(String currentPath, String filename, boolean pleaseLogErrors)
    { return new CMFile(currentPath,filename,null,pleaseLogErrors,false); }
    
    public static DatabaseEngine database(){return (DatabaseEngine)libraries[LIBRARY_DATABASE];}
    public static ThreadEngine threads(){return (ThreadEngine)libraries[LIBRARY_THREADS];}
    public static I3Interface intermud(){return (I3Interface)libraries[LIBRARY_INTERMUD];}
    public static ExternalHTTPRequests httpUtils(){return (ExternalHTTPRequests)libraries[LIBRARY_HTTP];}
    public static ListingLibrary lister(){return (ListingLibrary)libraries[LIBRARY_LISTER];}
    public static MoneyLibrary beanCounter(){return (MoneyLibrary)libraries[LIBRARY_MONEY];}
    public static ShoppingLibrary coffeeShops(){return (ShoppingLibrary)libraries[LIBRARY_SHOPS];}
    public static CombatLibrary combat(){return (CombatLibrary)libraries[LIBRARY_COMBAT];}
    public static HelpLibrary help(){return (HelpLibrary)libraries[LIBRARY_HELP];}
    public static TrackingLibrary tracking(){return (TrackingLibrary)libraries[LIBRARY_TRACKING];}
    public static MaskingLibrary masking(){return (MaskingLibrary)libraries[LIBRARY_MASKING];}
    public static ChannelsLibrary channels(){return (ChannelsLibrary)libraries[LIBRARY_CHANNELS];}
    public static CommonCommands commands(){return (CommonCommands)libraries[LIBRARY_COMMANDS];}
    public static EnglishParsing english(){return (EnglishParsing)libraries[LIBRARY_ENGLISH];}
    public static SlaveryLibrary slavery(){return (SlaveryLibrary)libraries[LIBRARY_SLAVERY];}
    public static JournalsLibrary journals(){return (JournalsLibrary)libraries[LIBRARY_JOURNALS];}
    public static TelnetFilter coffeeFilter(){return (TelnetFilter)libraries[LIBRARY_TELNET];}
    public static CMObjectBuilder coffeeMaker(){return (CMObjectBuilder)libraries[LIBRARY_OBJBUILDERS];}
    public static SessionsList sessions(){return (SessionsList)libraries[LIBRARY_SESSIONS];}
    public static CMFlagLibrary flags(){return (CMFlagLibrary)libraries[LIBRARY_FLAGS];}
    public static XMLLibrary xml(){return (XMLLibrary)libraries[LIBRARY_XML];}
    public static SocialsList socials(){return (SocialsList)libraries[LIBRARY_SOCIALS];}
    public static CMMiscUtils utensils(){return (CMMiscUtils)libraries[LIBRARY_UTENSILS];}
    public static StatisticsLibrary coffeeTables(){return (StatisticsLibrary)libraries[LIBRARY_STATS];}
    public static WorldMap map(){return (WorldMap)libraries[LIBRARY_MAP];}
    public static QuestManager quests(){return (QuestManager)libraries[LIBRARY_QUEST];}
    public static AbilityMapper ableMapper(){return (AbilityMapper)libraries[LIBRARY_ABLEMAP];}
    public static TextEncoders encoder(){return (TextEncoders)libraries[LIBRARY_ENCODER];}
    public static SMTPLibrary smtp(){return (SMTPLibrary)libraries[LIBRARY_SMTP];}
    public static DiceLibrary dice(){return (DiceLibrary)libraries[LIBRARY_DICE];}
    public static FactionManager factions(){return (FactionManager)libraries[LIBRARY_FACTIONS];}
    public static ClanManager clans(){return (ClanManager)libraries[LIBRARY_CLANS];}
    public static PollManager polls(){return (PollManager)libraries[LIBRARY_POLLS];}
    public static TimeManager time(){return (TimeManager)libraries[LIBRARY_TIME];}
    public static ColorLibrary color(){return (ColorLibrary)libraries[LIBRARY_COLOR];}
    
    public static int convertToLibraryCode(Object O)
    {
        if(O instanceof DatabaseEngine) return LIBRARY_DATABASE;
        if(O instanceof ThreadEngine) return LIBRARY_THREADS;
        if(O instanceof I3Interface) return LIBRARY_INTERMUD;
        if(O instanceof ExternalHTTPRequests) return LIBRARY_HTTP;
        if(O instanceof ListingLibrary) return LIBRARY_LISTER;
        if(O instanceof MoneyLibrary) return LIBRARY_MONEY;
        if(O instanceof ShoppingLibrary) return LIBRARY_SHOPS;
        if(O instanceof CombatLibrary) return LIBRARY_COMBAT;
        if(O instanceof HelpLibrary) return LIBRARY_HELP;
        if(O instanceof TrackingLibrary) return LIBRARY_TRACKING;
        if(O instanceof MaskingLibrary) return LIBRARY_MASKING;
        if(O instanceof ChannelsLibrary) return LIBRARY_CHANNELS;
        if(O instanceof CommonCommands) return LIBRARY_COMMANDS;
        if(O instanceof EnglishParsing) return LIBRARY_ENGLISH;
        if(O instanceof SlaveryLibrary) return LIBRARY_SLAVERY;
        if(O instanceof JournalsLibrary) return LIBRARY_JOURNALS;
        if(O instanceof TelnetFilter) return LIBRARY_TELNET;
        if(O instanceof CMObjectBuilder) return LIBRARY_OBJBUILDERS;
        if(O instanceof SessionsList) return LIBRARY_SESSIONS;
        if(O instanceof CMFlagLibrary) return LIBRARY_FLAGS;
        if(O instanceof XMLLibrary) return LIBRARY_XML;
        if(O instanceof SocialsList) return LIBRARY_SOCIALS;
        if(O instanceof CMMiscUtils) return LIBRARY_UTENSILS;
        if(O instanceof StatisticsLibrary) return LIBRARY_STATS;
        if(O instanceof WorldMap) return LIBRARY_MAP;
        if(O instanceof QuestManager) return LIBRARY_QUEST;
        if(O instanceof AbilityMapper) return LIBRARY_ABLEMAP;
        if(O instanceof TextEncoders) return LIBRARY_ENCODER;
        if(O instanceof SMTPLibrary) return LIBRARY_SMTP;
        if(O instanceof DiceLibrary) return LIBRARY_DICE;
        if(O instanceof FactionManager) return LIBRARY_FACTIONS;
        if(O instanceof ClanManager) return LIBRARY_CLANS;
        if(O instanceof PollManager) return LIBRARY_POLLS;
        if(O instanceof TimeManager) return LIBRARY_TIME;
        if(O instanceof ColorLibrary) return LIBRARY_COLOR;
        return -1;
    }
    
    public static void registerLibrary(CMObject O)
    {
        int code=convertToLibraryCode(O);
        if(code>=0)
        { 
            libraries[code]=O; 
            registered[code]=true;
        }
    }
    
    public static void registerLibraries(Enumeration e)
    {
        for(;e.hasMoreElements();)
            registerLibrary((CMObject)e.nextElement());
    }
    public static int countRegistered()
    {
        int x=0;
        for(int i=0;i<registered.length;i++)
            if(registered[i]) x++;
        return x;
    }
    public static String unregistered()
    {
        StringBuffer str=new StringBuffer("");
        for(int i=0;i<registered.length;i++)
            if(!registered[i]) str.append(""+i+", ");
        return str.toString();
    }
}
