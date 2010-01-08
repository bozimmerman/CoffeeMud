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
public class CMLib
{
    static final long serialVersionUID=42;
    public String getClassName(){return "CMLib";}
    private static final Vector mudThreads=new Vector();
    private static CMLib[] libs=new CMLib[256];
    public CMLib(){
        super();
        char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(libs==null) libs=new CMLib[256];
        if(libs[c]==null) libs[c]=this;
    }
    private static CMLib l(){ return libs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
    public static CMLib l(char c){return libs[c];}
    public static CMLib instance(){return l();}
    private final CMLibrary[] libraries=new CMLibrary[LIBRARY_TOTAL];
    private boolean[] registered=new boolean[LIBRARY_TOTAL];


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
    public static final int LIBRARY_LOGIN=35;
    public static final int LIBRARY_TIMS=36;
    public static final int LIBRARY_LEVELS=37;
    public static final int LIBRARY_EXPERTISES=38;
    public static final int LIBRARY_MATERIALS=39;
    public static final int LIBRARY_LEGAL=40;
    public static final int LIBRARY_LANGUAGE=41;
    public static final int LIBRARY_CATALOG=42;
    public static final int LIBRARY_PLAYERS=43;
    public static final int LIBRARY_TITLES=44;
    public static final int LIBRARY_ABLEPARMS=45;
    public static final int LIBRARY_GENEDITOR=46;
    public static final int LIBRARY_AREAGEN=47;
    public static final int LIBRARY_TOTAL=48;
    public static final String[] LIBRARY_DESCS={
        "DATABASE","THREADS","INTERMUD","HTTP","LISTER","MONEY","SHOPS","COMBAT",
        "HELP","TRACKING","MASKING","CHANNELS","COMMANDS","ENGLISH","SLAVERY","JOURNALS",
        "FLAGS","OBJBUILDERS","SESSIONS","TELNET","XML","SOCIALS","UTENSILS","STATS",
        "MAP","QUEST","ABLEMAP","ENCODER","SMTP","DICE","FACTIONS","CLANS","POLLS",
        "TIME","COLOR","LOGIN","TIMS","LEVELS","EXPERTISES","MATERIALS","LEGAL",
        "LANGUAGE","CATALOG","PLAYERS","TITLES","ABLEPARMS","GENEDITOR","AREAGEN"};

    public static CMath math(){return CMath.instance();}
    public static CMParms parms(){return CMParms.instance();}
    public static CMStrings strings(){return CMStrings.instance();}
    public static CMClass classes(){return CMClass.instance();}
    public static CMSecurity security(){return CMSecurity.instance();}
    public static Directions directions(){return Directions.instance();}
    public static Log log(){return Log.instance();}
    public static Vector hosts(){return mudThreads;}
    public static MudHost mud(int port){
        if(mudThreads.size()==0)
            return null;
        else
        if(port<=0)
            return (MudHost)mudThreads.firstElement();
        else
        for(int i=0;i<mudThreads.size();i++)
            if(((MudHost)mudThreads.elementAt(i)).getPort()==port)
                return (MudHost)mudThreads.elementAt(i);
        return null;
    }
    public static Resources resources(){return Resources.instance();}
    public static CMProps props(){return CMProps.instance();}
    public static Enumeration libraries(){
        Vector V=new Vector();
        for(int l=0;l<CMLib.LIBRARY_TOTAL;l++)
            if(l().libraries[l]!=null)
                V.addElement(l().libraries[l]);
        return V.elements();
    }
    public static CMFile newFile(String currentPath, String filename, boolean pleaseLogErrors)
    { return new CMFile(currentPath,filename,null,pleaseLogErrors,false); }

    public static DatabaseEngine database(){return (DatabaseEngine)l().libraries[LIBRARY_DATABASE];}
    public static ThreadEngine threads(){return (ThreadEngine)l().libraries[LIBRARY_THREADS];}
    public static I3Interface intermud(){return (I3Interface)l().libraries[LIBRARY_INTERMUD];}
    public static ItemBalanceLibrary itemBuilder(){return (ItemBalanceLibrary)l().libraries[LIBRARY_TIMS];}
    public static ExternalHTTPRequests httpUtils(){return (ExternalHTTPRequests)l().libraries[LIBRARY_HTTP];}
    public static ListingLibrary lister(){return (ListingLibrary)l().libraries[LIBRARY_LISTER];}
    public static MoneyLibrary beanCounter(){return (MoneyLibrary)l().libraries[LIBRARY_MONEY];}
    public static ShoppingLibrary coffeeShops(){return (ShoppingLibrary)l().libraries[LIBRARY_SHOPS];}
    public static MaterialLibrary materials(){return (MaterialLibrary)l().libraries[LIBRARY_MATERIALS];}
    public static CombatLibrary combat(){return (CombatLibrary)l().libraries[LIBRARY_COMBAT];}
    public static HelpLibrary help(){return (HelpLibrary)l().libraries[LIBRARY_HELP];}
    public static TrackingLibrary tracking(){return (TrackingLibrary)l().libraries[LIBRARY_TRACKING];}
    public static LegalLibrary law(){return (LegalLibrary)l().libraries[LIBRARY_LEGAL];}
    public static MaskingLibrary masking(){return (MaskingLibrary)l().libraries[LIBRARY_MASKING];}
    public static ChannelsLibrary channels(){return (ChannelsLibrary)l().libraries[LIBRARY_CHANNELS];}
    public static CommonCommands commands(){return (CommonCommands)l().libraries[LIBRARY_COMMANDS];}
    public static EnglishParsing english(){return (EnglishParsing)l().libraries[LIBRARY_ENGLISH];}
    public static SlaveryLibrary slavery(){return (SlaveryLibrary)l().libraries[LIBRARY_SLAVERY];}
    public static JournalsLibrary journals(){return (JournalsLibrary)l().libraries[LIBRARY_JOURNALS];}
    public static TelnetFilter coffeeFilter(){return (TelnetFilter)l().libraries[LIBRARY_TELNET];}
    public static GenericBuilder coffeeMaker(){return (GenericBuilder)l().libraries[LIBRARY_OBJBUILDERS];}
    public static SessionsList sessions(){return (SessionsList)l().libraries[LIBRARY_SESSIONS];}
    public static CMFlagLibrary flags(){return (CMFlagLibrary)l().libraries[LIBRARY_FLAGS];}
    public static XMLLibrary xml(){return (XMLLibrary)l().libraries[LIBRARY_XML];}
    public static SocialsList socials(){return (SocialsList)l().libraries[LIBRARY_SOCIALS];}
    public static CMMiscUtils utensils(){return (CMMiscUtils)l().libraries[LIBRARY_UTENSILS];}
    public static StatisticsLibrary coffeeTables(){return (StatisticsLibrary)l().libraries[LIBRARY_STATS];}
    public static ExpLevelLibrary leveler(){return (ExpLevelLibrary)l().libraries[LIBRARY_LEVELS];}
    public static WorldMap map(){return (WorldMap)l().libraries[LIBRARY_MAP];}
    public static QuestManager quests(){return (QuestManager)l().libraries[LIBRARY_QUEST];}
    public static AreaGenerationLibrary percolator(){return (AreaGenerationLibrary)l().libraries[LIBRARY_AREAGEN];}
    public static AbilityMapper ableMapper(){return (AbilityMapper)l().libraries[LIBRARY_ABLEMAP];}
    public static TextEncoders encoder(){return (TextEncoders)l().libraries[LIBRARY_ENCODER];}
    public static SMTPLibrary smtp(){return (SMTPLibrary)l().libraries[LIBRARY_SMTP];}
    public static LanguageLibrary lang(){return (LanguageLibrary)l().libraries[LIBRARY_LANGUAGE];}
    public static DiceLibrary dice(){return (DiceLibrary)l().libraries[LIBRARY_DICE];}
    public static FactionManager factions(){return (FactionManager)l().libraries[LIBRARY_FACTIONS];}
    public static ClanManager clans(){return (ClanManager)l().libraries[LIBRARY_CLANS];}
    public static PollManager polls(){return (PollManager)l().libraries[LIBRARY_POLLS];}
    public static TimeManager time(){return (TimeManager)l().libraries[LIBRARY_TIME];}
    public static ColorLibrary color(){return (ColorLibrary)l().libraries[LIBRARY_COLOR];}
    public static CharCreationLibrary login(){return (CharCreationLibrary)l().libraries[LIBRARY_LOGIN];}
    public static ExpertiseLibrary expertises(){return (ExpertiseLibrary)l().libraries[LIBRARY_EXPERTISES];}
    public static PlayerLibrary players(){return (PlayerLibrary)l().libraries[LIBRARY_PLAYERS];}
    public static CatalogLibrary catalog(){return (CatalogLibrary)l().libraries[LIBRARY_CATALOG];}
    public static AutoTitlesLibrary titles(){return (AutoTitlesLibrary)l().libraries[LIBRARY_TITLES];}
    public static AbilityParameters ableParms(){return (AbilityParameters)l().libraries[LIBRARY_ABLEPARMS];}
    public static GenericEditor genEd(){return (GenericEditor)l().libraries[LIBRARY_GENEDITOR];}

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
        if(O instanceof ExpLevelLibrary) return LIBRARY_LEVELS;
        if(O instanceof TrackingLibrary) return LIBRARY_TRACKING;
        if(O instanceof LanguageLibrary) return LIBRARY_LANGUAGE;
        if(O instanceof MaskingLibrary) return LIBRARY_MASKING;
        if(O instanceof ChannelsLibrary) return LIBRARY_CHANNELS;
        if(O instanceof CommonCommands) return LIBRARY_COMMANDS;
        if(O instanceof EnglishParsing) return LIBRARY_ENGLISH;
        if(O instanceof SlaveryLibrary) return LIBRARY_SLAVERY;
        if(O instanceof JournalsLibrary) return LIBRARY_JOURNALS;
        if(O instanceof TelnetFilter) return LIBRARY_TELNET;
        if(O instanceof GenericBuilder) return LIBRARY_OBJBUILDERS;
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
        if(O instanceof CharCreationLibrary) return LIBRARY_LOGIN;
        if(O instanceof ItemBalanceLibrary) return LIBRARY_TIMS;
        if(O instanceof ExpertiseLibrary) return LIBRARY_EXPERTISES;
        if(O instanceof MaterialLibrary) return LIBRARY_MATERIALS;
        if(O instanceof LegalLibrary) return LIBRARY_LEGAL;
        if(O instanceof CatalogLibrary) return LIBRARY_CATALOG;
        if(O instanceof PlayerLibrary) return LIBRARY_PLAYERS;
        if(O instanceof AutoTitlesLibrary) return LIBRARY_TITLES;
        if(O instanceof AbilityParameters) return LIBRARY_ABLEPARMS;
        if(O instanceof GenericEditor) return LIBRARY_GENEDITOR;
        if(O instanceof AreaGenerationLibrary) return LIBRARY_AREAGEN;
        return -1;
    }

    public static void registerLibrary(CMLibrary O)
    {
        int code=convertToLibraryCode(O);
        if(code>=0)
        {
            if(l()==null) new CMLib();
            Vector privacyV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(), true);
            if((!privacyV.contains(LIBRARY_DESCS[code])
            &&(libs[MudHost.MAIN_HOST]!=l())))
            {
                if(libs[MudHost.MAIN_HOST].libraries[code]==null)
                    libs[MudHost.MAIN_HOST].libraries[code]=O;
                else
                    l().libraries[code]=libs[MudHost.MAIN_HOST].libraries[code];
            }
            else
                l().libraries[code]=O;
            l().registered[code]=true;
        }
    }
    @SuppressWarnings("deprecation")
	public static void killThread(Thread t, long sleepTime, int attempts)
    {
		try{
			if(t==null) return;
			t.interrupt();
			try{Thread.sleep(sleepTime);}catch(Exception e){}
			int att=0;
			while((att<attempts)&&t.isAlive())try{att++;Thread.sleep(sleepTime);}catch(Exception e){}
			try {if(t.isAlive()) { try { t.stop();}catch(Throwable tx){} } }catch(java.lang.ThreadDeath td) {}
		}
		catch(Throwable th){}

    }
    
    public static boolean s_sleep(long millis) {
    	try{ Thread.sleep(millis); } catch(java.lang.InterruptedException ex) { return false;}
    	return true;
    }

    public static void propertiesLoaded() {
        CMLib lib=l();
        Vector privacyV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(), true);
        for(int l=0;l<lib.libraries.length;l++)
            if((!privacyV.contains(LIBRARY_DESCS[l])&&(libs[MudHost.MAIN_HOST]!=lib)))
            {}
            else
            if(lib.libraries[l]==null)
            {}
            else
                lib.libraries[l].propertiesLoaded();
        CharStats.CODES.reset();
        RawMaterial.CODES.reset();
        Wearable.CODES.reset();
    }
    
    public static void activateLibraries() {
        CMLib lib=l();
        Vector privacyV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(), true);
        for(int l=0;l<lib.libraries.length;l++)
            if((!privacyV.contains(LIBRARY_DESCS[l])&&(libs[MudHost.MAIN_HOST]!=lib)))
            {
                Log.debugOut("CMLib","HOST"+Thread.currentThread().getThreadGroup().getName().charAt(0)+" sharing library "+CMLib.LIBRARY_DESCS[l]);
                lib.libraries[l]=libs[MudHost.MAIN_HOST].libraries[l];
            }
            else
            if(lib.libraries[l]==null)
                Log.errOut("CMLib","Unable to find library "+CMLib.LIBRARY_DESCS[l]);
            else
                lib.libraries[l].activate();
    }
    
    public static CMLibrary library(char tcode, int lcode) {
        if(libs[tcode]!=null)
            return libs[tcode].libraries[lcode];
        return null;
    }
    
    public static Enumeration libraries(int code) {
        Vector V=new Vector();
        for(int l=0;l<libs.length;l++)
            if((libs[l]!=null)
            &&(libs[l].libraries[code]!=null)
            &&(!V.contains(libs[l].libraries[code])))
                V.addElement(libs[l].libraries[code]);
        return V.elements();
    }
    
    public static void registerLibraries(Enumeration e)
    {
        for(;e.hasMoreElements();)
            registerLibrary((CMLibrary)e.nextElement());
    }
    
    public static int countRegistered()
    {
        int x=0;
        for(int i=0;i<l().registered.length;i++)
            if(l().registered[i]) x++;
        return x;
    }
    public static String unregistered()
    {
        StringBuffer str=new StringBuffer("");
        for(int i=0;i<l().registered.length;i++)
            if(!l().registered[i]) str.append(""+i+", ");
        return str.toString();
    }
}
