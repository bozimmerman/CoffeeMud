package com.planet_ink.coffee_mud.core;

import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Libraries.interfaces.CombatLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.CostType;
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.MudHost;

import java.util.*;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/*
   Copyright 2005-2018 Bo Zimmerman

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
//<!-- Comments Not Complete -->
/**
 * The source class for thread-group-based and global mud properties.
 * Acts as a singleton for the purposes of access to properties, parsed
 * into Str(ing), Int(eger), and Bool(ean).  The singleton maintains 
 * instances of itself for thread-group access, and fills in any gaps
 * with the base-thread-group values.
 * @author Bo Zimmerman
 */
public class CMProps extends Properties
{
	private static final long serialVersionUID = -6592429720705457521L;
	private static final CMProps[] props	   = new CMProps[256];
	private static final CMProps p(){ return props[Thread.currentThread().getThreadGroup().getName().charAt(0)];}

	protected static final String FILTER_PATTERN="%#@*!$&?";
	protected static final char[] FILTER_CHARS=FILTER_PATTERN.toCharArray();

	/**
	 * Constructor for a property object that applies only to this thread group.
	 */
	public CMProps()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null)
			props[c]=this;
	}
	
	/**
	 * Returns the property object that applies to the callers thread group.
	 * @return the property object that applies to the callers thread group.
	 */
	public static final CMProps instance()
	{
		final CMProps p=p();
		if(p==null)
			return new CMProps();
		return p;
	}
	
	/**
	 * Returns the property object that applies to the given thread group.
	 * @param c thread group code to return properties for. Base = '0'.
	 * @return the property object that applies to the given thread group.
	 */
	public static final CMProps instance(char c)
	{
		return props[c];
	}
	
	/**
	 * Enums for String entries in the coffeemud.ini file
	 * @author Bo Zimmerman
	 */
	public static enum Str 
	{
		/** BLAH BLAH BLAH */
		PKILL, 
		MULTICLASS,
		PLAYERDEATH,
		PLAYERFLEE,
		SHOWDAMAGE,
		EMAILREQ,
		ESC0,
		ESC1,
		ESC2,
		ESC3,
		ESC4,
		ESC5,
		ESC6,
		ESC7,
		ESC8,
		ESC9,
		MSPPATH,
		BADNAMES,
		SKILLCOST,
		COMMONCOST,
		LANGCOST,
		AUTOPURGE,
		MUDNAME,
		MUDVER,
		MUDSTATUS,
		MUDPORTS,
		CORPSEGUARD,
		INIPATH,
		MUDBINDADDRESS,
		MUDDOMAIN,
		ADMINEMAIL,
		PREJUDICE,
		BUDGET,
		DEVALUERATE,
		INVRESETRATE,
		EMOTEFILTER,
		SAYFILTER,
		CHANNELFILTER,
		CLANTROPPK,
		MAILBOX,
		CLANTROPCP,
		CLANTROPEXP,
		CLANTROPAREA,
		COLORSCHEME,
		SMTPSERVERNAME,
		EXPCONTACTLINE,
		AUTOWEATHERPARMS,
		MXPIMAGEPATH,
		IGNOREMASK,
		SIPLET,
		PREFACTIONS,
		AUTOAREAPROPS,
		MOBDEATH,
		I3ROUTERS,
		IDLETIMERS,
		PRICEFACTORS,
		ITEMLOOTPOLICY,
		AUCTIONRATES,
		DEFAULTPROMPT,
		CHARCREATIONSCRIPTS,
		CHARSETINPUT,
		CHARSETOUTPUT,
		DEFAULTPLAYERFLAGS,
		FORMULA_ATTACKADJUSTMENT,
		FORMULA_ARMORADJUSTMENT,
		FORMULA_ATTACKFUDGEBONUS,
		FORMULA_CHANCESPELLCRIT,
		FORMULA_DAMAGESPELLCRIT,
		FORMULA_DAMAGERANGEDTARGETED,
		FORMULA_DAMAGERANGEDSTATIC,
		FORMULA_DAMAGEMELEETARGETED,
		FORMULA_DAMAGEMELEESTATIC,
		FORMULA_CHANCEWEAPONCRIT,
		FORMULA_DAMAGEWEAPONCRIT,
		FORMULA_NPCHITPOINTS,
		FORMULA_DAMAGESPELLFUDGE,
		FORMULA_DAMAGEMELEEFUDGE,
		FORMULA_DAMAGERANGEDFUDGE,
		WIZLISTMASK,
		AUTOREACTION,
		POSEFILTER,
		STARTINGITEMS,
		STATCOSTS,
		CLANTROPMB,
		CLANTROPLVL,
		FORMULA_PVPATTACKFUDGEBONUS,
		FORMULA_PVPCHANCESPELLCRIT,
		FORMULA_PVPDAMAGESPELLCRIT,
		FORMULA_PVPDAMAGERANGEDTARGETED,
		FORMULA_PVPDAMAGEMELEETARGETED,
		FORMULA_PVPCHANCEWEAPONCRIT,
		FORMULA_PVPDAMAGEWEAPONCRIT,
		FORMULA_PVPDAMAGESPELLFUDGE,
		FORMULA_PVPDAMAGEMELEEFUDGE,
		FORMULA_PVPDAMAGERANGEDFUDGE,
		DEFAULTPARENTAREA,
		CLANWEBSITES,
		CLANFORUMDATA,
		FORMULA_HITPOINTRECOVER,
		FORMULA_MANARECOVER,
		FORMULA_MOVESRECOVER,
		CHANNELBACKLOG,
		BLACKLISTFILE,
		REMORTMASK,
		REMORTRETAIN,
		RACEMIXING,
		FORMULA_TOTALCOMBATXP,
		FORMULA_INDCOMBATXP,
	}

	/**
	 * Enums for Integer entries in the coffeemud.ini file
	 * @author Bo Zimmerman
	 *
	 */
	public static enum Int 
	{
		EXPRATE,
		SKYSIZE,
		MAXSTAT,
		EDITORTYPE,
		MINCLANMEMBERS,
		DAYSCLANDEATH,
		MINCLANLEVEL,
		MANACOST,
		DAYSCLANOVERTHROW,
		//LANGTRAINCOST,
		//SKILLTRAINCOST,
		//COMMONPRACCOST,
		//LANGPRACCOST,
		//SKILLPRACCOST,
		CLANCOST,
		PAGEBREAK,
		FOLLOWLEVELDIFF,
		LASTPLAYERLEVEL,
		CLANENCHCOST,
		BASEMAXSTAT,
		MANAMINCOST,
		MAXCLANMEMBERS,
		MANACONSUMETIME,
		MANACONSUMEAMT,
		MUDBACKLOG,
		TICKSPERMUDDAY,
		COMBATSYSTEM,
		JOURNALLIMIT,
		TICKSPERMUDMONTH,
		MUDTHEME,
		INJPCTCHANCE,
		INJPCTHP,
		INJPCTHPAMP,
		INJPCTCHANCEAMP,
		INJMULTIPLIER,
		STARTHP,
		STARTMANA,
		STARTMOVE,
		TRIALDAYS,
		EQVIEW,
		MAXCONNSPERIP,
		MAXNEWPERIP,
		MAXMAILBOX,
		JSCRIPTS,
		INJMINLEVEL,
		DEFCMDTIME,
		DEFCOMCMDTIME,
		DEFABLETIME,
		DEFCOMABLETIME,
		INJBLEEDMINLEVEL,
		INJBLEEDPCTHP,
		INJBLEEDPCTCHANCE,
		EXPIRE_MONSTER_EQ,
		EXPIRE_PLAYER_DROP,
		EXPIRE_RESOURCE,
		EXPIRE_MONSTER_BODY,
		EXPIRE_PLAYER_BODY,
		MAXITEMSHOWN,
		STARTSTAT,
		RECOVERRATE,
		COMMONACCOUNTSYSTEM,
		MAXCONNSPERACCOUNT,
		EXVIEW,
		MUDSTATE,
		OBJSPERTHREAD,
		MAXCOMMONSKILLS,
		MAXCRAFTINGSKILLS,
		MAXNONCRAFTINGSKILLS,
		MAXLANGUAGES,
		WALKCOST,
		RUNCOST,
		ACCOUNTPURGEDAYS,
		AWARERANGE,
		MINWORKERTHREADS,
		MAXWORKERTHREADS,
		DUELTICKDOWN,
		BASEMINSTAT,
		DEFSOCTIME,
		DEFCOMSOCTIME,
		RACEEXPIRATIONDAYS,
		COMBATPROWESS,
		HUNGER_FULL,
		HUNGER_GAIN_PCT,
		HUNGER_LOSS_PCT,
		THIRST_FULL,
		THIRST_GAIN_PCT,
		THIRST_LOSS_PCT,
		MOB_HP_BASE
		;
		
		public static final int	EXVIEW_DEFAULT		= 0;
		public static final int	EXVIEW_PARAGRAPH	= 1;
		public static final int	EXVIEW_MIXED		= 2;
		public static final int	EXVIEW_BRIEF		= 3;
		
		public static final int	EQVIEW_DEFAULT		= 0;
		public static final int	EQVIEW_MIXED		= 1;
		public static final int	EQVIEW_PARAGRAPH	= 2;
		
		public static final int ANY_ARMOR_PROWESS =	Prowesses.ARMOR_ADV.value 
													| Prowesses.ARMOR_ADJ.value 
													| Prowesses.ARMOR_ABSOLUTE.value 
													| Prowesses.ARMOR_NUMBER.value;  
		public static final int ANY_COMBAT_PROWESS =Prowesses.COMBAT_ABSOLUTE.value 
													| Prowesses.COMBAT_NUMBER.value 
													| Prowesses.COMBAT_ADV.value 
													| Prowesses.COMBAT_ADJ.value
													| Prowesses.COMBAT_NOUN.value;
		public static final int ANY_DAMAGE_PROWESS =Prowesses.DAMAGE_ABSOLUTE.value 
													| Prowesses.DAMAGE_ADV.value 
													| Prowesses.DAMAGE_ADJ.value 
													| Prowesses.DAMAGE_NUMBER.value;
		public enum Prowesses
		{
			NONE(0),
			ARMOR_ABSOLUTE(1),
			ARMOR_NUMBER(2),
			ARMOR_ADV(4),
			ARMOR_ADJ(8),
			COMBAT_ABSOLUTE(16),
			COMBAT_NUMBER(32),
			COMBAT_ADV(64),
			COMBAT_ADJ(128),
			COMBAT_NOUN(256),
			DAMAGE_ABSOLUTE(512),
			DAMAGE_ADV(1024),
			DAMAGE_ADJ(2048),
			DAMAGE_NUMBER(4096)
			;
			public int value;
			private Prowesses(int val)
			{
				value=val;
			}

			public boolean is(int val)
			{
				if(value == 0)
					return val==0;
				else
					return (val & value) == value;
			}
		}
	}

	/**
	 * Enums for Boolean entries in the coffeemud.ini file
	 * @author Bo Zimmerman
	 *
	 */
	public static enum Bool 
	{
		MOBCOMPRESS,
		ITEMDCOMPRESS,
		ROOMDCOMPRESS,
		MOBDCOMPRESS,
		MUDSTARTED,
		POPULATIONSTARTED,
		EMAILFORWARDING,
		MOBNOCACHE,
		ROOMDNOCACHE,
		MUDSHUTTINGDOWN,
		ACCOUNTEXPIRATION,
		INTRODUCTIONSYSTEM,
		FILERESOURCENOCACHE,
		CATALOGNOCACHE,
		MAPFINDSNOCACHE,
		HASHPASSWORDS,
		ACCOUNTSNOCACHE,
		PLAYERSNOCACHE
	}

	/**
	 * Enums for String list entries
	 * @author Bo Zimmerman
	 */
	public enum StrList
	{
		SUBSCRIPTION_STRS("SUBSCRIPTION_STRS")
		;
		private final String str;
		private StrList(String toStr)
		{
			str=toStr;
		}
		
		@Override 
		public String toString() 
		{
			return str; 
		}
	}

	/**
	 * Enums for localizeable string list entries in lists.ini
	 * @author Bo Zimmerman
	 */
	public static enum ListFile 
	{
		DAMAGE_WORDS_THRESHOLDS,
		DAMAGE_WORDS,
		HEALTH_CHART,
		MISS_DESCS,
		WEAPON_MISS_DESCS,
		PROWESS_DESCS_CEILING,
		PROWESS_DESCS,
		ARMOR_DESCS_CEILING,
		ARMOR_DESCS,
		ARMOR_ADJS,
		DAMAGE_DESCS_CEILING,
		DAMAGE_DESCS,
		DAMAGE_ADJS,
		EXTREME_ADVS,
		COMBAT_ADJS,
		COMBAT_NOUNS,
		EXP_CHART,
		ARMOR_MISFITS,
		MAGIC_WORDS,
		GAIT_LIST,
		TOD_CHANGE_OUTSIDE,
		TOD_CHANGE_INSIDE,
		WEATHER_ENDS,
		WEAPON_HIT_DESCS,
		RACIAL_CATEGORY_IS_UNDEAD,
		RACIAL_CATEGORY_IS_OUTSIDER,
		RACIAL_CATEGORY_IS_INSECT,
		RACIAL_CATEGORY_IS_VERMIN,
		RACIAL_CATEGORY_IS_VEGETATION,
		RACIAL_CATEGORY_IS_FISH,
		RACIAL_CATEGORY_IS_MARINE,
		WIZ_NOAUTOINVOKE,
		TECH_LEVEL_NAMES,
		TECH_BABBLE_VERBS,
		TECH_BABBLE_ADJ1,
		TECH_BABBLE_ADJ2,
		TECH_BABBLE_NOUN,
		TECH_SECTOR_X_NAMES,
		TECH_SECTOR_Y_NAMES,
		TECH_SECTOR_Z_NAMES,
		WEATHER_CLEAR, // try to always and forever keep these at the end...
		WEATHER_CLOUDY, // try to always and forever keep these at the end...
		WEATHER_WINDY, // try to always and forever keep these at the end...
		WEATHER_RAIN, // try to always and forever keep these at the end...
		WEATHER_THUNDERSTORM, // try to always and forever keep these at the end...
		WEATHER_SNOW, // try to always and forever keep these at the end...
		WEATHER_HAIL, // try to always and forever keep these at the end...
		WEATHER_HEAT_WAVE, // try to always and forever keep these at the end...
		WEATHER_SLEET, // try to always and forever keep these at the end...
		WEATHER_BLIZZARD, // try to always and forever keep these at the end...
		WEATHER_DUSTSTORM, // try to always and forever keep these at the end...
		WEATHER_DROUGHT, // try to always and forever keep these at the end...
		WEATHER_WINTER_COLD, // try to always and forever keep these at the end...
		WEATHER_NONE // try to always and forever keep these at the end...
		;
		private String key;
		
		private ListFile(String key)
		{
			this.key=key;
		}
		
		private ListFile()
		{
			this.key=this.toString();
		}
		
		public String getKey() 
		{ 
			return key; 
		}
	}

	/**
	 * Enum for white lists for various purposes
	 * @author Bo Zimmerman
	 */
	public static enum WhiteList 
	{
		CONNS,
		LOGINS,
		NEWPLAYERS
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Class<? extends Enum>[] PROP_CLASSES = new Class[]  
	{
		CMProps.Bool.class,
		CMProps.Str.class,
		CMProps.Int.class,
		CMProps.ListFile.class,
		CMProps.StrList.class,
		CMProps.WhiteList.class,
	};
	
	@SuppressWarnings("unchecked")
	protected final Set<String>[]sysLstFileSet		= new Set[ListFile.values().length];
	protected final String[]	 sysVars			= new String[Str.values().length];
	protected final Integer[]	 sysInts			= new Integer[Int.values().length];
	protected final Double[]	 sysIntsAsFloat		= new Double[Int.values().length];
	protected final Boolean[]	 sysBools			= new Boolean[Bool.values().length];
	protected final String[][]	 sysLists			= new String[StrList.values().length][];
	protected final Object[]	 sysLstFileLists	= new Object[ListFile.values().length];
	protected final List<String> sayFilter			= new Vector<String>();
	protected final List<String> channelFilter		= new Vector<String>();
	protected final List<String> emoteFilter		= new Vector<String>();
	protected final List<String> poseFilter			= new Vector<String>();
	protected String[][]		 statCodeExtensions = null;
	protected int				 pkillLevelDiff		= 26;
	protected boolean			 loaded				= false;
	protected byte[]			 promptSuffix		= new byte[0];
	protected long				 lastReset			= System.currentTimeMillis();
	protected long  			 TIME_TICK			= 4000;
	protected long  			 MILLIS_PER_MUDHOUR	= 600000;
	protected long  			 TICKS_PER_RLMIN	= (int)Math.round(60000.0/TIME_TICK);
	protected long  			 TICKS_PER_RLHOUR	= TICKS_PER_RLMIN * 60;
	protected long  			 TICKS_PER_RLDAY	= TICKS_PER_RLHOUR * 24;
	protected double			 TIME_TICK_DOUBLE	= TIME_TICK;
	protected final Map<String,Integer>	maxClanCatsMap				= new HashMap<String,Integer>();
	protected final Set<String>			publicClanCats				= new HashSet<String>();
	protected final Map<String,Double>	skillMaxManaExceptions		= new HashMap<String,Double>();
	protected final Map<String,Double>	skillMinManaExceptions		= new HashMap<String,Double>();
	protected final Map<String,Double>	skillActionCostExceptions	= new HashMap<String,Double>();
	protected final Map<String,Double>	skillComActionCostExceptions= new HashMap<String,Double>();
	protected final Map<String,Double>	cmdActionCostExceptions		= new HashMap<String,Double>();
	protected final Map<String,Double>	cmdComActionCostExceptions	= new HashMap<String,Double>();
	protected final Map<String,Double>	socActionCostExceptions		= new HashMap<String,Double>();
	protected final Map<String,Double>	socComActionCostExceptions	= new HashMap<String,Double>();
	protected final Map<WhiteList,Pattern[]>whiteLists				= new HashMap<WhiteList,Pattern[]>();
	protected final PairVector<String,Long> newusersByIP			= new PairVector<String,Long>();
	protected final Map<String,ThreadGroup> privateSet				= new HashMap<String,ThreadGroup>();
	protected final Map<String,ExpertiseLibrary.SkillCostDefinition> commonCost  =new HashMap<String,ExpertiseLibrary.SkillCostDefinition>();
	protected final Map<String,ExpertiseLibrary.SkillCostDefinition> skillsCost  =new HashMap<String,ExpertiseLibrary.SkillCostDefinition>();
	protected final Map<String,ExpertiseLibrary.SkillCostDefinition> languageCost=new HashMap<String,ExpertiseLibrary.SkillCostDefinition>();

	/**
	 * Creates a properties object for the callers thread group using the given input stream 
	 * as input for the properties.
	 * @param in a stream from which to draw the properties.
	 */
	public CMProps(InputStream in)
	{
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null)
			props[c]=this;
		try
		{
			this.load(in);
			loaded=true;
		}
		catch(final IOException e)
		{
			loaded=false;
		}
	}

	/**
	 * Creates a properties object for the callers thread group using the given file path 
	 * as input for the properties.
	 * @param filename a file from which to draw the properties.
	 */
	public CMProps(String filename)
	{
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null)
			props[c]=this;
		try
		{
			final CMFile F=new CMFile(filename,null);
			if(F.exists())
			{
				this.load(new ByteArrayInputStream(F.textUnformatted().toString().getBytes()));
				loaded=true;
			}
			else
				loaded=false;
		}
		catch(final IOException e)
		{
			loaded=false;
		}
	}

	/**
	 * Creates a properties object for the callers thread group using the given file path 
	 * as input for the properties and the given properties as a baseline.
	 * @param p loads these properties into this object first
	 * @param filename a file from which to draw the rest of the properties.
	 */
	public CMProps(final Properties p, final String filename)
	{
		super(p);
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null)
			props[c]=this;

		try
		{
			this.load(new ByteArrayInputStream(new CMFile(filename,null).raw()));
			loaded=true;
		}
		catch(final IOException e)
		{
			loaded=false;
		}
	}

	/**
	 * Creates a new properties object for the callers thread group
	 * and loads the given ini file.
	 * @param iniFile the path and name of the ini file to load
	 * @return the new properties object
	 */
	public static final CMProps loadPropPage(final String iniFile)
	{
		final CMProps page=new CMProps(iniFile);
		if(!page.loaded)
			return null;
		return page;
	}

	/**
	 * Returns true if this properties object has been loaded from
	 * a stream or ini file, and false otherwise.
	 * @return true or false, depending
	 */
	public boolean isLoaded()
	{
		return loaded;
	}

	/**
	 * Returns the dirty word filter pattern
	 * @return the dirty word filter pattern
	 */
	public static String getFilterPattern()
	{
		return FILTER_PATTERN;
	}

	/** retrieve a local .ini file entry as a string
	*
	* Usage:  String s=getPrivateStr("TAG");
	* @param tagToGet   the property tag to retrieve.
	* @return String   the value of the .ini file tag
	*/
	public final String getPrivateStr(final String tagToGet)
	{
		final String s=getProperty(tagToGet);
		if(s==null)
			return "";
		return s;
	}

	/** retrieve raw local .ini file entry as a string
	*
	* Usage:  String s=getRawPrivateStr("TAG");
	* @param tagToGet   the property tag to retrieve.
	* @return String   the value of the .ini file tag
	*/
	public final String getRawPrivateStr(final String tagToGet)
	{
		return getProperty(tagToGet);
	}

	/** retrieve a particular .ini file entry as a string
	*
	* Usage:  String s=getStr(p,"TAG");
	* @param tagToGet    the property tag to retrieve.
	* @return String	the value of the .ini file tag
	*/
	public final String getStr(final String tagToGet)
	{
		final String thisTag=this.getProperty(tagToGet);
		if((thisTag==null)&&(props[MudHost.MAIN_HOST]!=null)&&(props[MudHost.MAIN_HOST]!=this))
			return props[MudHost.MAIN_HOST].getStr(tagToGet);
		if(thisTag==null)
			return "";
		return thisTag;
	}

	/** retrieve a particular .ini file entry as a string, or use a default
	*
	* Usage:  String s=getStr(p,"TAG");
	* @param tagToGet    the property tag to retrieve.
	* @param defaultVal the value to return if the property does not exist
	* @return String	the value of the .ini file tag
	*/
	public final String getStr(final String tagToGet, final String defaultVal)
	{
		String thisTag=this.getProperty(tagToGet);
		if((thisTag==null)&&(props[MudHost.MAIN_HOST]!=null)&&(props[MudHost.MAIN_HOST]!=this))
			thisTag=props[MudHost.MAIN_HOST].getStr(tagToGet);
		if((thisTag==null)||(thisTag.length()==0))
			return defaultVal;
		return thisTag;
	}

	/** retrieve particular .ini file entrys as a string array
	*
	* Usage:  String s=getStrsStarting(p,"TAG");
	* @param tagStartersToGet    the property tag to retrieve.
	* @return String	the value of the .ini file tag
	*/
	public final String[][] getStrsStarting(String tagStartersToGet)
	{
		final PairVector<String,String> strBag = new PairVector<String,String>();
		tagStartersToGet = tagStartersToGet.toUpperCase();
		for(final Enumeration<?> e=propertyNames(); e.hasMoreElements();)
		{
			final String propName = (String)e.nextElement();
			if(propName.toUpperCase().startsWith(tagStartersToGet))
			{
				final String subPropName = propName.substring(tagStartersToGet.length()).toUpperCase();
				String thisTag=this.getProperty(propName);
				if((thisTag==null)&&(props[MudHost.MAIN_HOST]!=null)&&(props[MudHost.MAIN_HOST]!=this))
					thisTag = props[MudHost.MAIN_HOST].getStr(propName);
				if(thisTag!=null)
					strBag.addElement(subPropName,thisTag);
			}
		}
		final String[][] strArray = new String[strBag.size()][2];
		for(int s = 0; s < strBag.size(); s++)
		{
			strArray[s][0] = strBag.elementAt(s).first;
			strArray[s][1] = strBag.elementAt(s).second;
		}
		return strArray;
	}

	/** retrieve a particular .ini file entry as a boolean
	*
	* Usage:  boolean i=getBoolean("TAG");
	* @param tagToGet   the property tag to retrieve.
	* @return int   the value of the .ini file tag
	*/
	public final boolean getBoolean(final String tagToGet)
	{
		final String thisVal=getStr(tagToGet);
		if(thisVal.toUpperCase().startsWith("T"))
			return true;
		return false;
	}

	/** retrieve a particular .ini file entry as a double
	*
	* Usage:  double i=getDouble("TAG");
	* @param tagToGet    the property tag to retrieve.
	* @return int    the value of the .ini file tag
	*/
	public final double getDouble(final String tagToGet)
	{
		try
		{
			return Double.parseDouble(getStr(tagToGet));
		}
		catch(final Exception e)
		{
			return 0.0;
		}
	}

	/** retrieve a particular .ini file entry as an integer
	*
	* Usage:  int i=getInt("TAG");
	* @param tagToGet    the property tag to retrieve.
	* @return int    the value of the .ini file tag
	*/
	public final int getInt(final String tagToGet)
	{
		try
		{
			return Integer.parseInt(getStr(tagToGet));
		}
		catch(final Exception t)
		{
			return 0;
		}
	}

	/** retrieve a particular .ini file entry as a long
	*
	* Usage:  long i=getInt("TAG");
	* @param tagToGet    the property tag to retrieve.
	* @return long the value of the .ini file tag
	*/
	public final long getLong(final String tagToGet)
	{
		try
		{
			return Long.parseLong(getStr(tagToGet));
		}
		catch(final Exception t)
		{
			return 0;
		}
	}
	
	public static final boolean isUsingAccountSystem()
	{
		return getIntVar(Int.COMMONACCOUNTSYSTEM) > 1;
	}

	/**
	 * Return the action cost associated with a specific Command ID(), or the default value if
	 * no exception is found for that command.
	 * @param ID the commands ID()
	 * @param defaultValue the value to return if the override does not exist
	 * @return the action cost
	 */
	public static final double getCommandActionCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().cmdActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	/**
	 * Return the action cost associated with a specific Command ID() during combat, or 
	 * the default value if no exception is found for that command.
	 * @param ID the commands ID()
	 * @param defaultValue the default action cost to use
	 * @return the action cost
	 */
	public static final double getCommandCombatActionCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().cmdComActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	/**
	 * Return the action cost associated with a specific Command ID(), or the default value if
	 * no exception is found for that command.
	 * @param ID the commands ID()
	 * @return the action cost
	 */
	public static final double getCommandActionCost(final String ID)
	{
		return getCommandActionCost(ID,CMath.div(getIntVar(Int.DEFCMDTIME),100.0));
	}

	/**
	 * Return the action cost associated with a specific Command ID() during combat, or 
	 * the default value if no exception is found for that command.
	 * @param ID the commands ID()
	 * @return the action cost
	 */
	public static final double getCommandCombatActionCost(final String ID)
	{
		return getCommandCombatActionCost(ID,CMath.div(getIntVar(Int.DEFCOMCMDTIME),100.0));
	}

	/**
	 * Return the action cost associated with a specific Ability ID(), or the default value if
	 * no exception is found for that Ability.
	 * @param ID the Ability ID()
	 * @param defaultValue the default action cost to use
	 * @return the action cost
	 */
	public static final double getSkillActionCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().skillActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	/**
	 * Return the action cost associated with a specific Ability ID() during combat, or 
	 * the default value if no exception is found for that Ability.
	 * @param ID the Ability ID()
	 * @param defaultValue the default action cost to use
	 * @return the action cost
	 */
	public static final double getSkillCombatActionCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().skillComActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	/**
	 * Return the action cost associated with a specific Ability ID(), or the default value if
	 * no exception is found for that Ability.
	 * @param ID the Ability ID()
	 * @return the action cost
	 */
	public static final double getSkillActionCost(final String ID)
	{
		return getSkillActionCost(ID,CMath.div(getIntVar(Int.DEFABLETIME),100.0));
	}

	/**
	 * Return the action cost associated with a specific Ability ID() during combat, or 
	 * the default value if no exception is found for that Ability.
	 * @param ID the Ability ID()
	 * @return the action cost
	 */
	public static final double getSkillCombatActionCost(final String ID)
	{
		return getSkillCombatActionCost(ID,CMath.div(getIntVar(Int.DEFCOMABLETIME),100.0));
	}

	/**
	 * Return the action cost associated with a specific Social base name, or 
	 * the default value if no exception is found for that Social.
	 * @param baseName the Social Base Name
	 * @param defaultValue the default action cost to use
	 * @return the action cost
	 */
	private static final double getSocialActionCost(final String baseName, final double defaultValue)
	{
		final Map<String,Double> overrides=p().socActionCostExceptions;
		final String uID=baseName.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	/**
	 * Return the action cost associated with a specific Social base name during combat, or 
	 * the default value if no exception is found for that Social.
	 * @param baseName the Social Base Name
	 * @param defaultValue the default action cost to use
	 * @return the action cost
	 */
	private static final double getSocialCombatActionCost(final String baseName, final double defaultValue)
	{
		final Map<String,Double> overrides=p().socComActionCostExceptions;
		final String uID=baseName.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	/**
	 * Return the action cost associated with a specific Social base name, or 
	 * the default value if no exception is found for that Social.
	 * @param baseName the Social Base Name
	 * @return the action cost
	 */
	public static final double getSocialActionCost(final String baseName)
	{
		return getSocialActionCost(baseName,CMath.div(getIntVar(Int.DEFSOCTIME),100.0));
	}

	/**
	 * Return the action cost associated with a specific Social base name during combat, or 
	 * the default value if no exception is found for that Social.
	 * @param baseName the Social Base Name
	 * @return the action cost
	 */
	public static final double getSocialCombatActionCost(final String baseName)
	{
		return getSocialCombatActionCost(baseName,CMath.div(getIntVar(Int.DEFCOMSOCTIME),100.0));
	}

	/**
	 * Returns the maximum level difference between players who want to PVP each other.
	 * @return the maximum level difference between players who want to PVP each other.
	 */
	public static final int getPKillLevelDiff()
	{
		return p().pkillLevelDiff;
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * the callers thread group.
	 * @param varNum the Str enum of the entry to get
	 * @return the value of the property.
	 */
	public static final String getVar(final Str varNum)
	{
		try 
		{ 
			return p().sysVars[varNum.ordinal()];
		}
		catch(final Exception t) 
		{
			return "";
		}
	}

	/**
	 * Returns whether the given string is a valid property name,
	 * referring to the names of the various enums in the static
	 * CMProps class.
	 * @param varName the possible name of a property
	 * @return true if it is some sort of prop enum, false otherwise
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isPropName(String varName)
	{
		if(varName == null)
			return false;
		varName = varName.toUpperCase().trim();
		for(Class<? extends Enum> c : CMProps.PROP_CLASSES)
		{
			if(CMath.s_valueOf(c, varName) != null)
				return true;
		}
		return false;
	}

	/**
	 * Returns a string representation of the given property, as best it can.
	 * referring to the names of the various enums in the static
	 * CMProps class.
	 * If the given property name does not exist, a "" is returned.
	 * @param varName the name of a property
	 * @return the string value of that property
	 */
	@SuppressWarnings("rawtypes")
	public static String getProp(String varName)
	{
		if(varName == null)
			return "";
		varName = varName.toUpperCase().trim();
		for(Class<? extends Enum> c : CMProps.PROP_CLASSES)
		{
			if(CMath.s_valueOf(c, varName) != null)
			{
				if(c == Str.class)
					return p().getStr((Str)CMath.s_valueOf(c, varName));
				else
				if(c == Int.class)
					return ""+p().getInt((Int)CMath.s_valueOf(c, varName));
				else
				if(c == Bool.class)
					return ""+p().getBool((Bool)CMath.s_valueOf(c, varName));
				else
				if(c == ListFile.class)
					return ""+CMParms.toListString(CMProps.getListFileStringList((ListFile)CMath.s_valueOf(c, varName)));
				else
				if(c == StrList.class)
					return ""+CMParms.toListString(CMProps.getListVar((StrList)CMath.s_valueOf(c, varName)));
				else
				if(c == WhiteList.class)
					return ""+CMParms.toListString(p().whiteLists.get(CMath.s_valueOf(c, varName)));
			}
		}
		return p().getStr(varName,"");
	}
	
	/**
	 * Sets a property from a string representation of the given property, as best it can.
	 * referring to the names of the various enums in the static
	 * CMProps class.
	 * If the given property name does not exist, false is returned.
	 * @param varName the name of a property
	 * @param value the string value of that property
	 * @return true if it tried to set the property, false if it failed
	 */
	@SuppressWarnings("rawtypes")
	public static boolean setProp(String varName, String value)
	{
		if(varName == null)
			return false;
		varName = varName.toUpperCase().trim();
		for(Class<? extends Enum> c : CMProps.PROP_CLASSES)
		{
			if(CMath.s_valueOf(c, varName) != null)
			{
				if(c == Str.class)
				{
					p().sysVars[((Str)CMath.s_valueOf(c, varName)).ordinal()] = value;
					return true;
				}
				else
				if(c == Int.class)
				{
					p().sysInts[((Str)CMath.s_valueOf(c, varName)).ordinal()] = Integer.valueOf(CMath.s_int(value));
					p().sysIntsAsFloat[((Str)CMath.s_valueOf(c, varName)).ordinal()] = null;
					return true;
				}
				else
				if(c == Bool.class)
				{
					p().sysBools[((Str)CMath.s_valueOf(c, varName)).ordinal()] = Boolean.valueOf(CMath.s_bool(value));
					return true;
				}
				else
				if(c == ListFile.class)
					throw new java.lang.UnsupportedOperationException();
				else
				if(c == StrList.class)
				{
					p().sysLists[((StrList)CMath.s_valueOf(c, varName)).ordinal()] = CMParms.parseCommas(value,true).toArray(new String[0]);
					return true;
				}
				else
				if(c == WhiteList.class)
					throw new java.lang.UnsupportedOperationException();
			}
		}
		return false;
	}
	
	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * this prop object
	 * @param varNum the Str enum of the entry to get
	 * @return the value of the property.
	 */
	public final String getStr(final Str varNum)
	{
		try 
		{ 
			return sysVars[varNum.ordinal()];
		}
		catch(final Exception t) 
		{
			return "";
		}
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * the callers thread group.
	 * @param varNum the Int enum of the entry to get
	 * @return the value of the property.
	 */
	public static final int getIntVar(final Int varNum)
	{
		try 
		{ 
			return p().sysInts[varNum.ordinal()].intValue(); 
		}
		catch(final Exception t) 
		{
			return -1;
		}
	}

	/**
	 * Retrieve the base MOB hit point base
	 * @return the value of the base.
	 */
	public static final int getMobHPBase()
	{
		try 
		{ 
			final int x=p().sysInts[Int.MOB_HP_BASE.ordinal()].intValue();
			return (x<=0)?11:x;
		}
		catch(final Exception t) 
		{
			return 11;
		}
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * the callers thread group. These are in variables permanently
	 * translated to floats for easy access
	 * @param varNum the Int enum of the entry to get
	 * @return the value of the property as a double.
	 */
	public static final double getIntVarAsDouble(final Int varNum)
	{
		try 
		{ 
			Double d=p().sysIntsAsFloat[varNum.ordinal()];
			if(d==null)
			{
				d=Double.valueOf(p().sysInts[varNum.ordinal()].doubleValue());
				p().sysIntsAsFloat[varNum.ordinal()]=d;
			}
			return d.doubleValue();
		}
		catch(final Exception t) 
		{
			return -1;
		}
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * the callers thread group. These are in variables permanently
	 * translated from 0-100 to pct (0-1) for easy access
	 * @param varNum the Int enum of the entry to get
	 * @return the value of the property as a pct double.
	 */
	public static final double getIntVarAsPct(final Int varNum)
	{
		try 
		{ 
			Double d=p().sysIntsAsFloat[varNum.ordinal()];
			if(d==null)
			{
				d=Double.valueOf(p().sysInts[varNum.ordinal()].doubleValue()/100.0);
				p().sysIntsAsFloat[varNum.ordinal()]=d;
			}
			return d.doubleValue();
		}
		catch(final Exception t) 
		{
			return -1;
		}
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * theis props object.
	 * @param varNum the Int enum of the entry to get
	 * @return the value of the property.
	 */
	public final int getInt(final Int varNum)
	{
		try 
		{ 
			return sysInts[varNum.ordinal()].intValue(); 
		}
		catch(final Exception t) 
		{
			return -1;
		}
	}
	
	/**
	 * Retrieve one of the pre-processed coffeemud.ini lists for
	 * the callers thread group.
	 * @param varType the StrList enum of the list to get
	 * @return the list from the properties.
	 */
	public static final String[] getListVar(final StrList varType)
	{
		try 
		{
			return p().sysLists[varType.ordinal()]; 
		}
		catch(final Exception t) 
		{
			return new String[0];
		}
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini lists for
	 * the callers thread group, as a hashed set of values.
	 * @param varType the StrList enum of the set list to get
	 * @return the list set from the properties.
	 */
	public static final Set<String> getListFileVarSet(final ListFile varType)
	{
		try 
		{
			final CMProps p=p();
			if(p.sysLstFileSet[varType.ordinal()] == null)
			{
				synchronized(p.sysLstFileSet)
				{
					if(p.sysLstFileSet[varType.ordinal()] == null)
					{
						final String[] list =  CMProps.getListFileStringList(varType);
						p.sysLstFileSet[varType.ordinal()] = Collections.synchronizedSet(new HashSet<String>(Arrays.asList(list)));
					}
				}
			}
			return p.sysLstFileSet[varType.ordinal()];
		}
		catch(final Exception t) 
		{
			return new HashSet<String>();
		}
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * the callers thread group.
	 * @param varNum the Bool enum of the entry to get
	 * @return the value of the property.
	 */
	public static final boolean getBoolVar(final Bool varNum)
	{
		try 
		{ 
			return p().sysBools[varNum.ordinal()].booleanValue(); 
		}
		catch(final Exception t) 
		{
			return false;
		}
	}

	/**
	 * Retrieve one of the pre-processed coffeemud.ini entries for
	 * this prop object.
	 * @param varNum the Bool enum of the entry to get
	 * @return the value of the property.
	 */
	public final boolean getBool(final Bool varNum)
	{
		try 
		{ 
			return sysBools[varNum.ordinal()].booleanValue(); 
		}
		catch(final Exception t) 
		{
			return false;
		}
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the callers thread group.
	 * @param varNum the Bool enum of the entry to set
	 * @param val the new value of the entry
	 */
	public static final void setBoolVar(final Bool varNum, final boolean val)
	{
		if(varNum==null)
			return;
		p().sysBools[varNum.ordinal()]=Boolean.valueOf(val);
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * all thread groups.
	 * @param varNum the Bool enum of the entries to set
	 * @param val the new value of the entries
	 */
	public static final void setBoolAllVar(final Bool varNum, final boolean val)
	{
		if(varNum==null)
			return;
		for(final CMProps p : CMProps.props)
		{
			if(p!=null)
				p.sysBools[varNum.ordinal()]=Boolean.valueOf(val);
		}
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the callers thread group.
	 * @param varNum the Int enum of the entry to set
	 * @param val the new value of the entry
	 */
	public static final void setIntVar(final Int varNum, final int val)
	{
		if(varNum==null)
			return ;
		p().sysInts[varNum.ordinal()]=Integer.valueOf(val);
		p().sysIntsAsFloat[varNum.ordinal()] = null;
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the callers thread group, only in this case, the entry
	 * needs to be converted to an int first.
	 * @param varNum the Int enum of the entry to set
	 * @param val the new value of the entry, as a string.
	 */
	public static final void setIntVar(final Int varNum, String val)
	{
		if(varNum==null)
			return ;
		if(val==null)
			val="0";
		p().sysInts[varNum.ordinal()]=Integer.valueOf(CMath.s_int(val.trim()));
		p().sysIntsAsFloat[varNum.ordinal()] = null;
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the callers thread group, only in this case, the entry
	 * needs to be converted to an int first, if it can.  If it
	 * cannot, the defaultValue is used.
	 * @param varNum the Int enum of the entry to set
	 * @param val the new value of the entry, as a string.
	 * @param defaultValue the default value to use when the property doesn't exist
	 */
	public static final void setIntVar(final Int varNum, String val, final int defaultValue)
	{
		if(varNum==null)
			return ;
		if((val==null)||(val.length()==0))
			val=""+defaultValue;
		p().sysInts[varNum.ordinal()]=Integer.valueOf(CMath.s_int(val.trim()));
		p().sysIntsAsFloat[varNum.ordinal()] = null;
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini list entries for
	 * the callers thread group.
	 * @param varType the StrList enum of the entry to set
	 * @param var the new value of the entry list
	 */
	public static final void setListVar(final StrList varType, String[] var)
	{
		if(varType==null)
			return ;
		if(var==null)
			var=new String[0];
		p().sysLists[varType.ordinal()]=var;
	}

	/**
	 * Add to the end of one of the pre-processed coffeemud.ini list entries for
	 * the callers thread group.
	 * @param varType the StrList enum of the entry to add to
	 * @param var the value to add to the entry list
	 */
	public static final void addListVar(final StrList varType, String var)
	{
		if(varType==null)
			return ;
		if(var==null)
			return;
		final CMProps prop=p();
		if(prop.sysLists[varType.ordinal()]==null)
			setListVar(varType, new String[0]);
		final String[] list=prop.sysLists[varType.ordinal()];
		prop.sysLists[varType.ordinal()]=Arrays.copyOf(list, list.length+1);
		list[list.length-1]=var;
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the callers thread group.
	 * @param varNum the Str enum of the entry to set
	 * @param val the new value of the entry
	 * @param upperFy true to make the value uppercase first, false otherwise
	 */
	public static final void setVar(final Str varNum, String val, final boolean upperFy)
	{
		if(val==null)
			val="";
		setUpLowVar(varNum,upperFy?val.toUpperCase():val);
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the callers thread group, forcing the value to uppercase.
	 * @param varNum the Str enum of the entry to set
	 * @param val the new value of the entry
	 */
	public static final void setVar(final Str varNum, String val)
	{
		if(val==null)
			val="";
		setUpLowVar(varNum,val.toUpperCase());
	}
	
	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the given properties object.
	 * @param props the properties object to set the value on
	 * @param varNum the Str enum of the entry to set
	 * @param val the new value of the entry
	 */
	private static final void setUpLowVar(final CMProps props, final Str varNum, String val)
	{
		if(varNum==null)
			return ;
		if(val==null)
			val="";
		props.sysVars[varNum.ordinal()]=val;
		if(varNum==Str.PKILL)
		{
			final int x=val.indexOf('-');
			if(x>0)
				props.pkillLevelDiff=CMath.s_int(val.substring(x+1));
		}
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * the callers thread group, without making the value uppercase.
	 * @param varNum the Str enum of the entry to set
	 * @param val the new value of the entry
	 */
	public static final void setUpLowVar(final Str varNum, final String val)
	{
		setUpLowVar(p(),varNum,val);
	}

	/**
	 * Sets one of the pre-processed coffeemud.ini entries for
	 * all thread groups.
	 * @param varNum the Str enum of the entries to set
	 * @param val the new value of the entries
	 */
	public static final void setUpAllLowVar(final Str varNum, final String val)
	{
		for (final CMProps prop : props)
		{
			if(prop!=null)
				setUpLowVar(prop,varNum,val);
		}
	}

	/**
	 * Parses and sets one of the properties whitelist entries by parsing the given string by commands,
	 * and compiling them into Patterns.
	 * @param props the properties object to set the whitelist on
	 * @param listType the WhiteList type to set
	 * @param list the unparsed whitelist entries 
	 */
	public static final void setWhitelist(final CMProps props, final WhiteList listType, final String list)
	{
		if(listType == null)
			return ;
		if((list==null)||(list.trim().length()==0))
			return;
		final List<String> parts=CMParms.parseCommas(list.trim(),true);
		final List<Pattern> partsCompiled = new ArrayList<Pattern>();
		for(final String part : parts)
		{
			if(part.trim().length()==0)
				continue;
			partsCompiled.add(Pattern.compile(part.trim(),Pattern.CASE_INSENSITIVE|Pattern.DOTALL|Pattern.CANON_EQ));
		}
		props.whiteLists.put(listType, partsCompiled.toArray(new Pattern[0]));
	}

	/**
	 * Parses and sets one of the properties whitelist entries by parsing the given string by commands,
	 * and compiling them into Patterns, all for the callers thread group.
	 * @param listType the WhiteList type to set
	 * @param list the unparsed whitelist entries 
	 */
	public static final void setWhitelist(final WhiteList listType, final String list)
	{
		setWhitelist(p(), listType, list);
	}

	/**
	 * Retrurns true if the given chk string matches one of the entries in the given WhiteList type for the
	 * given properties object.
	 * @param props the properties to check the whitelist on
	 * @param listType the WhiteList type
	 * @param chk the string to search for
	 * @return true if the chk string is found, false otherwise
	 */
	public static final boolean isOnWhiteList(final CMProps props, final WhiteList listType, final String chk)
	{
		if(listType == null)
			return false;
		if((chk==null)||(chk.trim().length()==0))
			return false;
		final Pattern[] patts=props.whiteLists.get(listType);
		if(patts == null)
			return false;
		final String chkTrim=chk.trim();
		final CharSequence seq=chkTrim.subSequence(0, chkTrim.length());
		for(final Pattern p : patts)
		{
			if(p.matcher(seq).matches())
				return true;
		}
		return false;
	}

	/**
	 * Retrurns true if the given chk string matches one of the entries in the given WhiteList type for the
	 * callers thread group.
	 * @param listType the WhiteList type
	 * @param chk the string to search for
	 * @return true if the chk string is found, false otherwise
	 */
	public static final boolean isOnWhiteList(final WhiteList listType, final String chk)
	{
		return isOnWhiteList(p(), listType, chk);
	}

	/**
	 * Given the list of skill cost definitions, this method will parse 
	 * out the ID and the data, build a cost definition object
	 * and store the cost definition in the given map.
	 * @param fieldName the name of the field being parsed, for error messages
	 * @param map the map to store the cost definitions in
	 * @param fields the pre-separated list of cost definitions to finish parsing
	 */
	public static final void setUpCosts(final String fieldName, final Map<String,ExpertiseLibrary.SkillCostDefinition> map, final List<String> fields)
	{
		final double[] doubleChecker=new double[10];
		for(String field : fields)
		{
			field=field.trim();
			if(field.length()==0)
				continue;
			final int typeIndex=field.lastIndexOf(' ');
			if(typeIndex<0)
			{
				Log.errOut("CMProps","Error parsing coffeemud.ini field "+fieldName+", value: "+field);
				continue;
			}
			final String type=field.substring(typeIndex+1).toUpperCase().trim();
			String formula=field.substring(0,typeIndex).trim();
			final ExpertiseLibrary.CostType costType=(ExpertiseLibrary.CostType)CMath.s_valueOf(ExpertiseLibrary.CostType.values(), type);
			if(costType==null)
			{
				Log.errOut("CMProps","Error parsing coffeemud.ini field '"+fieldName+"', invalid type: "+type);
				Log.errOut("CMProps","Valid values include "+CMParms.toListString(ExpertiseLibrary.CostType.values()));
				continue;
			}
			String keyField="";
			if(!CMath.isMathExpression(formula, doubleChecker))
			{
				final int skillIndex=formula.indexOf(' ');
				if(skillIndex<0)
				{
					Log.errOut("CMProps","Error parsing coffeemud.ini field "+fieldName+", invalid formula: "+formula);
					continue;
				}
				keyField=formula.substring(0,skillIndex).toUpperCase().trim();
				formula=formula.substring(skillIndex+1).trim();
				if(!CMath.isMathExpression(formula, doubleChecker))
				{
					Log.errOut("CMProps","Error parsing coffeemud.ini field "+fieldName+", invalid formula: "+formula);
					continue;
				}
			}
			if(map.containsKey(keyField))
			{
				Log.errOut("CMProps","Error parsing coffeemud.ini '"+fieldName+"' has duplicate key:"+((keyField.length()==0)?"<EMPTY>":keyField));
				continue;
			}
			map.put(keyField.toUpperCase(), makeCostDefinition(costType,formula));
		}
	}

	private static final ExpertiseLibrary.SkillCostDefinition makeCostDefinition(final CostType costType, final String costDefinition)
	{
		return new ExpertiseLibrary.SkillCostDefinition()
		{
			@Override
			public CostType type()
			{
				return costType;
			}

			@Override
			public String costDefinition()
			{
				return costDefinition;
			}
		};
	}
	
	/**
	 * Returns the cost of gaining the given skill, by Ability id, for the callers
	 * thread group.
	 * @param id the Ability id to find a cost for
	 * @return the cost definition object for the given Ability.
	 */
	public static final ExpertiseLibrary.SkillCostDefinition getNormalSkillGainCost(final String id)
	{
		final CMProps p=p();
		ExpertiseLibrary.SkillCostDefinition pair=p.skillsCost.get(id.toUpperCase());
		if(pair==null)
			pair=p.skillsCost.get("");
		if(pair==null)
			pair=makeCostDefinition(ExpertiseLibrary.CostType.TRAIN, "1");
		return pair;
	}

	/**
	 * Returns the cost of gaining the given common skill, by Ability id, for the callers
	 * thread group.
	 * @param id the common skill Ability id to find a cost for
	 * @return the cost definition object for the given common skill Ability.
	 */
	public static final ExpertiseLibrary.SkillCostDefinition getCommonSkillGainCost(final String id)
	{
		final CMProps p=p();
		ExpertiseLibrary.SkillCostDefinition pair=p.commonCost.get(id.toUpperCase());
		if(pair==null)
			pair=p.commonCost.get("");
		if(pair==null)
			pair=makeCostDefinition(ExpertiseLibrary.CostType.TRAIN, "1");
		return pair;
	}

	/**
	 * Returns the cost of gaining the given language skill, by Ability id, for the callers
	 * thread group.
	 * @param id the language skill Ability id to find a cost for
	 * @return the cost definition object for the given language skill Ability.
	 */
	public static final ExpertiseLibrary.SkillCostDefinition getLangSkillGainCost(final String id)
	{
		final CMProps p=p();
		ExpertiseLibrary.SkillCostDefinition pair=p.languageCost.get(id.toUpperCase());
		if(pair==null)
			pair=p.languageCost.get("");
		if(pair==null)
			pair=makeCostDefinition(ExpertiseLibrary.CostType.TRAIN, "1");
		return pair;
	}

	/**
	 * Returns the number of times the given ip address has created a new user in the callers
	 * thread group properties, within a given amount of time.
	 * @param address the address to look for.
	 * @return the number of new users created.
	 */
	public static final int getCountNewUserByIP(final String address)
	{
		int count=0;
		final PairVector<String,Long> DV=p().newusersByIP;
		synchronized(DV)
		{
			for(int i=DV.size()-1;i>=0;i--)
			{
				if(DV.elementAt(i).first.equalsIgnoreCase(address))
				{
					if(System.currentTimeMillis()>(DV.elementAt(i).second.longValue()))
						DV.removeElementAt(i);
					else
						count++;
				}
			}
		}
		return count;
	}

	/**
	 * Adds a new new user entry for the callers thread group, and the given address.
	 * These are tracked to make sure a given address doesn't create too many new users.
	 * @param address the address to register a new user for
	 */
	public static final void addNewUserByIP(final String address)
	{
		final PairVector<String,Long> DV=p().newusersByIP;
		synchronized(DV)
		{
			DV.addElement(address,Long.valueOf(System.currentTimeMillis()+TimeManager.MILI_DAY));
		}
	}

	/**
	 * Returns the minimum amount of usage cost (mana) for the given ability ID().
	 * All for the callers thread group.  If no cost is found, returns MIN_VALUE.
	 * @param skillID the Ability ID to find a minimum cost for.
	 * @return the minimim cost of usage (mana) for the ability.
	 */
	public static final int getMinManaException(final String skillID)
	{
		final Map<String,Double> DV=p().skillMinManaExceptions;
		if(DV.containsKey(skillID.toUpperCase()))
			return DV.get(skillID.toUpperCase()).intValue();
		return Integer.MIN_VALUE;
	}

	/**
	 * Returns the maximum amount of usage cost (mana) for the given ability ID().
	 * All for the callers thread group.  If no cost is found, returns MIN_VALUE.
	 * @param skillID the Ability ID to find a maximum cost for.
	 * @return the maximum cost of usage (mana) for the ability.
	 */
	public static final int getMaxManaException(final String skillID)
	{
		final Map<String,Double> DV=p().skillMaxManaExceptions;
		if(DV.containsKey(skillID.toUpperCase()))
			return DV.get(skillID.toUpperCase()).intValue();
		return Integer.MIN_VALUE;
	}

	private static final double setExceptionCosts(final String val, final Map<String,Double> set)
	{
		if(val==null)
			return 0;
		set.clear();
		final List<String> V=CMParms.parseCommas(val,true);
		double endVal=0;
		for(String s : V)
		{
			if(CMath.isNumber(s))
			{ 
				endVal=CMath.s_double(s); 
			}
			else
			{
				final int x=s.indexOf(' ');
				if(CMath.isDouble(s.substring(x+1).trim()))
					set.put(s.substring(0,x).trim().toUpperCase(),Double.valueOf(CMath.s_double(s.substring(x+1).trim())));
			}
		}
		return endVal;
	}

	private static final String getRawListFileEntry(final String key)
	{
		final String rscKey="PARSED_LISTFILE".intern();
		Properties rawListData=(Properties)Resources.getResource(rscKey);
		if(rawListData==null)
		{
			synchronized(rscKey)
			{
				rawListData=(Properties)Resources.getResource(rscKey);
				if(rawListData==null)
				{
					rawListData=new Properties();
					CMProps p=p();
					String listFileName;
					if(p.containsKey("LISTFILE"))
						listFileName=p.getProperty("LISTFILE");
					else
						listFileName = props['0'].getProperty("LISTFILE");
					final CMFile F=new CMFile(listFileName,null,CMFile.FLAG_LOGERRORS);
					if(F.exists())
					{
						try
						{
							rawListData.load(new InputStreamReader(new ByteArrayInputStream(F.raw()), CMProps.getVar(Str.CHARSETINPUT)));
						}
						catch (final IOException e)
						{
						}
					}
					Resources.submitResource(rscKey, rawListData);
					for(final ListFile lfVar : ListFile.values())
					{
						p.sysLstFileLists[lfVar.ordinal()]=null;
						p.sysLstFileSet[lfVar.ordinal()]=null;
					}
				}
			}
		}
		final String val = rawListData.getProperty(key);
		if(val == null)
			Log.errOut("CMProps","Unable to load required list file entry: "+key);
		return val;
	}

	/**
	 * Returns the first integer in the integer array from the lists.ini file of
	 * the given ListFile entry, for the callers thread group.
	 * @param var the ListFile entry to return the first integer from
	 * @return the first int in the given entry
	 */
	public static final int getListFileFirstInt(final ListFile var)
	{
		if(var==null)
			return -1;
		return getListFileIntList(var)[0];
	}

	/**
	 * Returns the entire string list from the lists.ini file of the
	 * given ListFile entry, for the callers thread group.
	 * @param var the ListFile entry to return the string list from
	 * @return the string list from the lists.ini file
	 */
	public static final String[] getListFileStringList(final ListFile var)
	{
		if(var==null)
			return new String[0];
		final CMProps p=p();
		final Object[] objs=p.sysLstFileLists;
		if(objs[var.ordinal()]==null)
		{
			objs[var.ordinal()]=CMParms.toStringArray(CMParms.parseCommas(getRawListFileEntry(var.getKey()),true));
			p.sysLstFileSet[var.ordinal()]=null;
		}
		return ((String[])objs[var.ordinal()]);
	}

	/**
	 * Returns the entire int list from the lists.ini file of the
	 * given ListFile entry, for the callers thread group.
	 * @param var the ListFile entry to return the int list from
	 * @return the int list from the lists.ini file
	 */
	public static final int[] getListFileIntList(final ListFile var)
	{
		if(var==null)
			return new int[0];
		final CMProps p=p();
		final Object[] objs=p.sysLstFileLists;
		if(objs[var.ordinal()]==null)
		{
			if(objs[var.ordinal()]==null)
			{
				objs[var.ordinal()]=CMParms.toIntArray(CMParms.parseCommas(getRawListFileEntry(var.getKey()),true));
				p.sysLstFileSet[var.ordinal()]=null;
			}
		}
		return ((int[])objs[var.ordinal()]);
	}

	/**
	 * Returns a two dimensional list of string terms from the list file
	 * @param var the list entry to return
	 * @return the two-dimensional list.
	 */
	private static final Object[][] getListFileStringChoices(final ListFile var)
	{
		if(var==null)
			return new Object[0][];
		final CMProps p=p();
		final Object[] objs=p.sysLstFileLists;
		if(objs[var.ordinal()]==null)
		{
			final String[] baseArray = CMParms.toStringArray(CMParms.parseCommas(getRawListFileEntry(var.getKey()),false));
			final Object[][] finalArray=new Object[baseArray.length][];
			for(int s=0;s<finalArray.length;s++)
			{
				if((baseArray[s]==null)||(baseArray[s].length()==0))
					finalArray[s]=new Object[]{""};
				else
					finalArray[s]=CMParms.toStringArray(CMParms.parseAny(baseArray[s], '|', false));
			}
			objs[var.ordinal()]=finalArray;
			p.sysLstFileSet[var.ordinal()]=null;
		}
		return (Object[][])objs[var.ordinal()];
	}

	/**
	 * Returns the entire object grid from the lists.ini file of the
	 * given ListFile entry, for the callers thread group.
	 * @param var the ListFile entry to return the object grid from
	 * @return the object grid from the lists.ini file
	 */
	public static final Object[][][] getListFileGrid(final ListFile var)
	{
		if(var==null)
			return new String[0][0][];
		final CMProps p=p();
		final Object[] objs=p.sysLstFileLists;
		if(objs[var.ordinal()]==null)
		{
			final List<String> V=CMParms.parseSemicolons(getRawListFileEntry(var.getKey()),true);
			final Object[][] subSet=new Object[V.size()][];
			for(int v=0;v<V.size();v++)
				subSet[v]=CMParms.toStringArray(CMParms.parseCommas(V.get(v),false));
			final Object[][][] finalSet=new Object[subSet.length][][];
			for(int s=0;s<subSet.length;s++)
			{
				finalSet[s]=new Object[subSet[s].length][];
				for(int s1=0;s1<subSet[s].length;s1++)
				{
					if((subSet[s][s1]==null)||(((String)subSet[s][s1]).length()==0))
						finalSet[s][s1]=new Object[]{""};
					else
						finalSet[s][s1]=CMParms.toStringArray(CMParms.parseAny((String)subSet[s][s1], '|', false));
				}
			}
			objs[var.ordinal()]=finalSet;
			p.sysLstFileSet[var.ordinal()]=null;
		}
		return (Object[][][])objs[var.ordinal()];
	}

	/**
	 * Given the specific lists.ini entry, grabs the indexed string list, and then returns a random string from
	 * that choices available at the given index in the string list.
	 * @param varCode the lists.ini string list entry
	 * @param listIndex the index into the string list, to determine which choices to use
	 * @return a random string from the indexed string list
	 */
	public static final String getListFileChoiceFromIndexedList(final ListFile varCode, final int listIndex)
	{
		final Object[] set = getListFileStringChoices(varCode)[listIndex];
		if(set.length==1)
			return (String)set[0];
		return (String)CMLib.dice().pick(set);
	}

	/**
	 * Given the specific lists.ini entry, grabs the indexed string list, and then returns a random string from
	 * that choices available at the hash index in the string list.
	 * @param varCode the lists.ini string list entry
	 * @param hash the hash to use to determine the index in the list to use.
	 * @return a random string from the indexed string list
	 */
	public static final String getListFileChoiceFromIndexedListByHash(final ListFile varCode, final int hash)
	{
		final Object[][] allVars = getListFileStringChoices(varCode);
		final Object[] set = allVars[hash % allVars.length];
		if(set.length==1)
			return (String)set[0];
		return (String)CMLib.dice().pick(set);
	}

	/**
	 * Given the specific lists.ini entry, grabs the indexed string list, and it's size.
	 * @param varCode the lists.ini string list entry
	 * @return size of the indexed string list
	 */
	public static final int getListFileIndexedListSize(final ListFile varCode)
	{
		return getListFileStringChoices(varCode).length;
	}

	/**
	 * Given the specific lists.ini entry, grabs the indexed string list, returns a random 
	 * string choice from a random index in the list.
	 * @param varCode the lists.ini string list entry
	 * @return the completely random choice
	 */
	public static final String getAnyListFileValue(final ListFile varCode)
	{
		return (String)CMLib.dice().doublePick(getListFileStringChoices(varCode));
	}

	/**
	 * Returns true if the given property name is private to the callers thread group,
	 * and false if it is shared with the base thread group.  If the caller is the base
	 * thread group, it always returns false.
	 * @param s the name of the coffeemud.ini file property
	 * @return true of false
	 */
	public static boolean isPrivateToMe(final String s)
	{
		return p().privateSet.containsKey(s.toUpperCase().trim());
	}

	/**
	 * Returns the set of properties that are private to the callers thread group.  The base
	 * thread group always returns all of them.
	 * @param mask a regular expression mask to limit the set
	 * @return the set of private local properties
	 */
	public static Set<String> getPrivateSubSet(final String mask)
	{
		final Set<String> newSet=new HashSet<String>();
		for(final String s : p().privateSet.keySet())
		{
			if(Pattern.matches(mask, s))
				newSet.add(s);
		}
		return newSet;
	}

	/**
	 * Returns the first thread group available that privately owns the given property,
	 * excepting the base group.
	 * @param s the name of the property to look for
	 * @return the first thread group that privately owns the property
	 */
	public static ThreadGroup getPrivateOwner(final String s)
	{
		final String tag=s.toUpperCase().trim();
		for(final CMProps p : CMProps.props)
		{
			if((p!=null)&&p.privateSet.containsKey(tag))
				return p.privateSet.get(tag);
		}
		return null;
	}

	/**
	 * Reads this properties objects and sets ALL internal variables.  Can be re-called if
	 * any properties are changed.
	 */
	public final void resetSystemVars()
	{
		if(CMLib.lang()!=null)
			CMLib.lang().setLocale(getStr("LANGUAGE"),getStr("COUNTRY"));

		TIME_TICK=getLong("TICKTIME");
		if(TIME_TICK<500)
			TIME_TICK=4000;
		TIME_TICK_DOUBLE=TIME_TICK;
		TICKS_PER_RLMIN=(int)Math.round(60000.0/TIME_TICK_DOUBLE);
		TICKS_PER_RLHOUR=TICKS_PER_RLMIN * 60;
		TICKS_PER_RLDAY=TICKS_PER_RLHOUR * 24;
		MILLIS_PER_MUDHOUR=getLong("MILLISPERMUDHOUR");
		if(MILLIS_PER_MUDHOUR < TIME_TICK)
			MILLIS_PER_MUDHOUR = 600000;

		final List<String> privateList=CMParms.parseCommas(getStr("PRIVATERESOURCES").toUpperCase(),true);
		privateSet.clear();
		for(final String s : privateList)
			privateSet.put(s.trim(),Thread.currentThread().getThreadGroup());

		setVar(Str.BADNAMES,getStr("BADNAMES"));
		setVar(Str.MULTICLASS,getStr("CLASSSYSTEM"));
		setVar(Str.PKILL,getStr("PLAYERKILL"));
		setVar(Str.PLAYERDEATH,getStr("PLAYERDEATH"));
		setVar(Str.ITEMLOOTPOLICY,getStr("ITEMLOOTPOLICY"));
		setVar(Str.MOBDEATH,getStr("MOBDEATH"));
		setVar(Str.PLAYERFLEE,getStr("FLEE"));
		setVar(Str.SHOWDAMAGE,getStr("SHOWDAMAGE"));
		setVar(Str.EMAILREQ,getStr("EMAILREQ"));
		setVar(Str.ESC0,getStr("ESCAPE0"));
		setVar(Str.ESC1,getStr("ESCAPE1"));
		setVar(Str.ESC2,getStr("ESCAPE2"));
		setVar(Str.ESC3,getStr("ESCAPE3"));
		setVar(Str.ESC4,getStr("ESCAPE4"));
		setVar(Str.ESC5,getStr("ESCAPE5"));
		setVar(Str.ESC6,getStr("ESCAPE6"));
		setVar(Str.ESC7,getStr("ESCAPE7"));
		setVar(Str.ESC8,getStr("ESCAPE8"));
		setVar(Str.ESC9,getStr("ESCAPE9"));
		setVar(Str.MSPPATH,getStr("SOUNDPATH"),false);
		setVar(Str.AUTOPURGE,getStr("AUTOPURGE"));
		setIntVar(Int.ACCOUNTPURGEDAYS,getStr("ACCOUNTPURGE"),14);
		setVar(Str.IDLETIMERS,getStr("IDLETIMERS"));
		setVar(Str.CORPSEGUARD,getStr("CORPSEGUARD"));
		setUpLowVar(Str.MUDDOMAIN,getStr("DOMAIN"));
		String adminEmail = getStr("ADMINEMAIL");
		if((adminEmail==null)||(adminEmail.trim().length()==0))
			adminEmail = getStr("I3EMAIL");
		setVar(Str.ADMINEMAIL,adminEmail);
		setUpLowVar(Str.I3ROUTERS,getStr("I3ROUTERS"));
		setVar(Str.AUTOREACTION,getStr("AUTOREACTION"));
		setVar(Str.WIZLISTMASK,getStr("WIZLISTMASK"));
		setUpLowVar(Str.DEFAULTPARENTAREA,getStr("DEFAULTPARENTAREA"));
		setUpLowVar(Str.CLANWEBSITES,getStr("CLANWEBSITES"));
		setVar(Str.CHANNELBACKLOG,getStr("CHANNELBACKLOG"));
		setUpLowVar(Str.CLANFORUMDATA,getStr("CLANFORUMDATA"));
		setUpLowVar(Str.STARTINGITEMS,getStr("STARTINGITEMS","1 Waterskin, 1 Ration, 1 Torch"));
		setVar(Str.PREJUDICE,getStr("PREJUDICE"));
		setUpLowVar(Str.PRICEFACTORS,getStr("PRICEFACTORS"));
		setVar(Str.IGNOREMASK,getStr("IGNOREMASK"));
		setVar(Str.BUDGET,getStr("BUDGET"));
		setVar(Str.DEVALUERATE,getStr("DEVALUERATE"));
		setVar(Str.INVRESETRATE,getStr("INVRESETRATE"));
		setVar(Str.AUCTIONRATES,getStr("AUCTIONRATES","0,10,0.1%,10%,5%,1,168"));
		setUpLowVar(Str.DEFAULTPROMPT,getStr("DEFAULTPROMPT"));
		String promptBehavior = getStr("PROMPTBEHAVIOR","NORMAL");
		promptSuffix = new byte[0];
		if(!promptBehavior.equalsIgnoreCase("NORMAL") && promptBehavior.length()>0)
		{
			for(int i=0;i<promptBehavior.length()-1;i+=2)
			{
				final String cStr = promptBehavior.substring(i,i+2);
				if(cStr.equalsIgnoreCase("CR"))
				{
					promptSuffix = Arrays.copyOf(promptSuffix, promptSuffix.length+1);
					promptSuffix[promptSuffix.length-1] = (byte)'\n';
				}
				else
				if(cStr.equalsIgnoreCase("LF"))
				{
					promptSuffix = Arrays.copyOf(promptSuffix, promptSuffix.length+1);
					promptSuffix[promptSuffix.length-1] = (byte)'\r';
				}
				else
				if(cStr.equalsIgnoreCase("GA"))
				{
					final int pos = promptSuffix.length;
					promptSuffix = Arrays.copyOf(promptSuffix, promptSuffix.length + Session.TELNETGABYTES.length);
					System.arraycopy(Session.TELNETGABYTES, 0, promptSuffix, pos, Session.TELNETGABYTES.length);
				}
				else
				{
					final int pos = promptSuffix.length;
					byte[] bytes;
					try
					{
						bytes = cStr.trim().getBytes(getVar(Str.CHARSETINPUT));
						promptSuffix = Arrays.copyOf(promptSuffix, promptSuffix.length + bytes.length);
						System.arraycopy(bytes, 0, promptSuffix, pos, bytes.length);
					}
					catch (UnsupportedEncodingException e)
					{
					}
				}
			}
		}
		for(final ListFile lfVar : ListFile.values())
		{
			sysLstFileLists[lfVar.ordinal()]=null;
			sysLstFileSet[lfVar.ordinal()]=null;
		}
		setVar(Str.EMOTEFILTER,getStr("EMOTEFILTER"));
		p().emoteFilter.clear();
		p().emoteFilter.addAll(CMParms.parse((getStr("EMOTEFILTER")).toUpperCase()));
		setVar(Str.POSEFILTER,getStr("POSEFILTER"));
		setVar(Str.STATCOSTS,getStr("STATCOSTS","<18 1, <22 2, <25 3, <99 5"));
		setVar(Str.REMORTMASK,getStr("REMORTMASK","-LEVEL +>60"));
		setVar(Str.REMORTRETAIN,getStr("REMORTRETAIN","SKILLSAT100"));
		
		p().poseFilter.clear();
		p().poseFilter.addAll(CMParms.parse((getStr("POSEFILTER")).toUpperCase()));
		setVar(Str.SAYFILTER,getStr("SAYFILTER"));
		p().sayFilter.clear();
		p().sayFilter.addAll(CMParms.parse((getStr("SAYFILTER")).toUpperCase()));
		setVar(Str.CHANNELFILTER,getStr("CHANNELFILTER"));
		p().channelFilter.clear();
		p().channelFilter.addAll(CMParms.parse((getStr("CHANNELFILTER")).toUpperCase()));
		setVar(Str.CLANTROPAREA,getStr("CLANTROPAREA"));
		setVar(Str.CLANTROPCP,getStr("CLANTROPCP"));
		setVar(Str.CLANTROPEXP,getStr("CLANTROPEXP"));
		setVar(Str.CLANTROPPK,getStr("CLANTROPPK"));
		setVar(Str.CLANTROPMB,getStr("CLANTROPMB"));
		setVar(Str.CLANTROPLVL,getStr("CLANTROPLVL"));
		setVar(Str.COLORSCHEME,getStr("COLORSCHEME"));
		setUpLowVar(Str.SMTPSERVERNAME,getStr("SMTPSERVERNAME"));
		setVar(Str.EXPCONTACTLINE,getStr("EXPCONTACTLINE"));
		setVar(Str.AUTOWEATHERPARMS,getStr("AUTOWEATHERPARMS"));
		setVar(Str.DEFAULTPLAYERFLAGS,getStr("DEFAULTPLAYERFLAGS"));
		setUpLowVar(Str.AUTOAREAPROPS,getStr("AUTOAREAPROPS"));
		setUpLowVar(Str.MXPIMAGEPATH,getStr("MXPIMAGEPATH"));
		setBoolVar(Bool.ACCOUNTEXPIRATION,getStr("ACCOUNTEXPIRATION").equalsIgnoreCase("YES")?true:false);
		setBoolVar(Bool.INTRODUCTIONSYSTEM,getStr("INTRODUCTIONSYSTEM").equalsIgnoreCase("YES")?true:false);
		setBoolVar(Bool.HASHPASSWORDS,getStr("HASHPASSWORDS").equalsIgnoreCase("YES")?true:false);
		setUpLowVar(Str.PREFACTIONS,getStr("FACTIONS"));
		setUpLowVar(Str.CHARCREATIONSCRIPTS,getStr("CHARCREATIONSCRIPTS"));
		setUpLowVar(Str.CHARSETINPUT,getStr("CHARSETINPUT","iso-8859-1"));
		setUpLowVar(Str.CHARSETOUTPUT,getStr("CHARSETOUTPUT","iso-8859-1"));
		setUpCosts("COMMONCOST",commonCost,CMParms.parseCommas(getStr("COMMONCOST","1 TRAIN"),true));
		setUpCosts("SKILLCOST",skillsCost,CMParms.parseCommas(getStr("SKILLCOST","1 TRAIN"),true));
		setUpCosts("LANGCOST",languageCost,CMParms.parseCommas(getStr("LANGCOST","3 PRACTICE"),true));
		setVar(Str.RACEMIXING,getStr("RACEMIXING"));
		String[] hungerCodes=CMParms.parseCommas(getStr("HUNGER","500,100,100"),true).toArray(new String[3]);
		setIntVar(Int.HUNGER_FULL,hungerCodes.length>0?CMath.s_int(hungerCodes[0]):500);
		setIntVar(Int.HUNGER_GAIN_PCT,hungerCodes.length>1?CMath.s_int(CMStrings.deleteAllofChar(hungerCodes[1], '%')):100);
		setIntVar(Int.HUNGER_LOSS_PCT,hungerCodes.length>2?CMath.s_int(CMStrings.deleteAllofChar(hungerCodes[2], '%')):100);
		String[] thirstCodes=CMParms.parseCommas(getStr("THIRST","1000,100,100"),true).toArray(new String[3]);
		setIntVar(Int.THIRST_FULL,thirstCodes.length>0?CMath.s_int(thirstCodes[0]):500);
		setIntVar(Int.THIRST_GAIN_PCT,thirstCodes.length>1?CMath.s_int(CMStrings.deleteAllofChar(thirstCodes[1], '%')):100);
		setIntVar(Int.THIRST_LOSS_PCT,thirstCodes.length>2?CMath.s_int(CMStrings.deleteAllofChar(thirstCodes[2], '%')):100);
		setIntVar(Int.MOB_HP_BASE,CMath.s_int(getStr("MOB_HP_BASE","11")));

		setUpLowVar(Str.BLACKLISTFILE,getStr("BLACKLISTFILE","/resources/ipblock.ini"));
		setWhitelist(CMProps.WhiteList.CONNS,getStr("WHITELISTIPSCONN"));
		setWhitelist(CMProps.WhiteList.LOGINS,getStr("WHITELISTLOGINS"));
		setWhitelist(CMProps.WhiteList.NEWPLAYERS,getStr("WHITELISTIPSNEWPLAYERS"));

		if(p().sysBools[Bool.MUDSHUTTINGDOWN.ordinal()]==null)
			p().sysBools[Bool.MUDSHUTTINGDOWN.ordinal()]=Boolean.FALSE;
		
		for(final StrList strListVar : StrList.values())
		{
			final String list=getStr(strListVar.toString().toUpperCase().trim());
			if((list!=null)&&(list.trim().length()>0))
				setListVar(strListVar, CMParms.parseCommas(list,false).toArray(new String[0]));
		}

		if(CMLib.color()!=null)
			CMLib.color().clearLookups();
		if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("LEVEL"))
			setIntVar(Int.MANACONSUMEAMT,-100);
		else
		if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("SPELLLEVEL"))
			setIntVar(Int.MANACONSUMEAMT,-200);
		else
			setIntVar(Int.MANACONSUMEAMT,CMath.s_int(getStr("MANACONSUMEAMT").trim()));
		String s=getStr("COMBATSYSTEM");
		if("queue".equalsIgnoreCase(s))
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.CombatSystem.QUEUE.ordinal());
		else
		if("manual".equalsIgnoreCase(s))
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.CombatSystem.MANUAL.ordinal());
		else
		if("turnbased".equalsIgnoreCase(s))
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.CombatSystem.TURNBASED.ordinal());
		else
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.CombatSystem.DEFAULT.ordinal());
		s=getStr("EQVIEW");
		if("paragraph".equalsIgnoreCase(s))
			setIntVar(Int.EQVIEW,CMProps.Int.EQVIEW_PARAGRAPH);
		else
		if("mixed".equalsIgnoreCase(s))
			setIntVar(Int.EQVIEW,CMProps.Int.EQVIEW_MIXED);
		else
			setIntVar(Int.EQVIEW,CMProps.Int.EQVIEW_DEFAULT);
		s=getStr("EXVIEW");
		if("brief".equalsIgnoreCase(s))
			setIntVar(Int.EXVIEW,CMProps.Int.EXVIEW_BRIEF);
		else
		if("paragraph".equalsIgnoreCase(s))
			setIntVar(Int.EXVIEW,CMProps.Int.EXVIEW_PARAGRAPH);
		else
		if("mixed".equalsIgnoreCase(s))
			setIntVar(Int.EXVIEW,CMProps.Int.EXVIEW_MIXED);
		else
			setIntVar(Int.EXVIEW,CMProps.Int.EXVIEW_DEFAULT);

		s=getStr("EXPIRATIONS");
		List<String> V=CMParms.parseCommas(s,false);
		for(int i=0;i<5;i++)
		{
			final int ordNum =Int.EXPIRE_MONSTER_EQ.ordinal()+i;
			final Int expEnum=Int.values()[ordNum];
			if(V.size()>0)
			{
				setIntVar(expEnum,V.get(0));
				V.remove(0);
			}
			else
			switch(expEnum)
			{
			case EXPIRE_MONSTER_EQ: setIntVar(Int.EXPIRE_MONSTER_EQ,"30"); break;
			case EXPIRE_PLAYER_DROP: setIntVar(Int.EXPIRE_PLAYER_DROP,"1200"); break;
			case EXPIRE_RESOURCE: setIntVar(Int.EXPIRE_RESOURCE,"60"); break;
			case EXPIRE_MONSTER_BODY: setIntVar(Int.EXPIRE_MONSTER_BODY,"30"); break;
			case EXPIRE_PLAYER_BODY: setIntVar(Int.EXPIRE_PLAYER_BODY,"1330"); break;
			default: break;
			}
		}

		setIntVar(Int.MANACONSUMETIME,getStr("MANACONSUMETIME"));
		setIntVar(Int.PAGEBREAK,getStr("PAGEBREAK"));
		setIntVar(Int.CLANENCHCOST,getStr("CLANENCHCOST"));
		setIntVar(Int.FOLLOWLEVELDIFF,getStr("FOLLOWLEVELDIFF"));
		setIntVar(Int.EXPRATE,getStr("EXPRATE"));
		setIntVar(Int.SKYSIZE,getStr("SKYSIZE"));
		setIntVar(Int.MAXSTAT,getStr("MAXSTATS"));
		setIntVar(Int.BASEMAXSTAT,getStr("BASEMAXSTAT","18"));
		setIntVar(Int.BASEMINSTAT,getStr("BASEMINSTAT","3"));
		setIntVar(Int.STARTSTAT,getStr("STARTSTAT"));
		setIntVar(Int.DEFCMDTIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFCMDTIME"),p().cmdActionCostExceptions)*100.0));
		setIntVar(Int.DEFCOMCMDTIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFCOMCMDTIME"),p().cmdComActionCostExceptions)*100.0));
		setIntVar(Int.DEFABLETIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFABLETIME"),p().skillActionCostExceptions)*100.0));
		setIntVar(Int.DEFCOMABLETIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFCOMABLETIME"),p().skillComActionCostExceptions)*100.0));
		setIntVar(Int.DEFSOCTIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFSOCTIME"),p().socActionCostExceptions)*100.0));
		setIntVar(Int.DEFCOMSOCTIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFCOMSOCTIME"),p().socComActionCostExceptions)*100.0));
		setIntVar(Int.MANACOST,(int)CMProps.setExceptionCosts(getStr("MANACOST"),p().skillMaxManaExceptions));
		setIntVar(Int.MANAMINCOST,(int)CMProps.setExceptionCosts(getStr("MANAMINCOST"),p().skillMinManaExceptions));
		setIntVar(Int.EDITORTYPE,(getStr("EDITORTYPE").equalsIgnoreCase("WIZARD")) ? 1 : 0);
		setIntVar(Int.MINCLANMEMBERS,getStr("MINCLANMEMBERS"));
		setIntVar(Int.MAXCLANMEMBERS,getStr("MAXCLANMEMBERS"));
		setIntVar(Int.CLANCOST,getStr("CLANCOST"));
		setIntVar(Int.DAYSCLANDEATH,getStr("DAYSCLANDEATH"));
		setIntVar(Int.DAYSCLANOVERTHROW,getStr("DAYSCLANOVERTHROW","952"));
		setIntVar(Int.MINCLANLEVEL,getStr("MINCLANLEVEL"));
		setIntVar(Int.LASTPLAYERLEVEL,getStr("LASTPLAYERLEVEL"));
		setIntVar(Int.JOURNALLIMIT,getStr("JOURNALLIMIT"));
		setIntVar(Int.MUDTHEME,getStr("MUDTHEME"));
		setIntVar(Int.TRIALDAYS,getStr("TRIALDAYS"));
		setIntVar(Int.MAXCONNSPERIP,getStr("MAXCONNSPERIP"));
		setIntVar(Int.MAXCONNSPERACCOUNT,getStr("MAXCONNSPERACCOUNT"));
		setIntVar(Int.MAXNEWPERIP,getStr("MAXNEWPERIP"));
		setIntVar(Int.JSCRIPTS,getStr("JSCRIPTS"));
		setIntVar(Int.RECOVERRATE,getStr("RECOVERRATE"),1);
		setIntVar(Int.COMMONACCOUNTSYSTEM,getStr("COMMONACCOUNTSYSTEM"),1);
		setIntVar(Int.OBJSPERTHREAD,getStr("OBJSPERTHREAD"));
		setIntVar(Int.MAXCOMMONSKILLS,getStr("MAXCOMMONSKILLS"),0);
		setIntVar(Int.MAXCRAFTINGSKILLS,getStr("MAXCRAFTINGSKILLS"),2);
		setIntVar(Int.MAXNONCRAFTINGSKILLS,getStr("MAXNONCRAFTINGSKILLS"),5);
		setIntVar(Int.MAXLANGUAGES,getStr("MAXLANGUAGES"),3);
		setIntVar(Int.RACEEXPIRATIONDAYS,getStr("RACEEXPIRATIONDAYS"),365);
		setIntVar(Int.WALKCOST,getStr("WALKCOST"),1);
		setIntVar(Int.RUNCOST,getStr("RUNCOST"),2);
		setIntVar(Int.AWARERANGE,getStr("AWARERANGE"),0);
		setIntVar(Int.MINWORKERTHREADS,getStr("MINWORKERTHREADS"),1);
		setIntVar(Int.MAXWORKERTHREADS,getStr("MAXWORKERTHREADS"),100);
		setIntVar(Int.DUELTICKDOWN,getStr("DUELTICKDOWN"),5);
		V=CMParms.parseCommas(getStr("MAXCLANCATS"), true);
		p().maxClanCatsMap.clear();
		for(final String cat : V)
		{
			if(CMath.isInteger(cat.trim()))
				p().maxClanCatsMap.put("", Integer.valueOf(CMath.s_int(cat.trim())));
			else
			{
				final int x=cat.lastIndexOf(' ');
				if((x>0)&&CMath.isInteger(cat.substring(x+1).trim()))
					p().maxClanCatsMap.put(cat.substring(0,x).trim().toUpperCase(), Integer.valueOf(CMath.s_int(cat.substring(x+1).trim())));
			}
		}
		if(!p().maxClanCatsMap.containsKey(""))
			p().maxClanCatsMap.put("", Integer.valueOf(1));

		p().publicClanCats.clear();
		V=CMParms.parseCommas(getStr("PUBLICCLANCATS"), true);
		for(final String cat : V)
			p().publicClanCats.add(cat.trim().toUpperCase());
		p().publicClanCats.add("");

		V=CMParms.parseCommas(getStr("INJURYSYSTEM"),true);
		setIntVar(Int.INJPCTCHANCE,		(V.size()>0) ? CMath.s_int(V.get(0)) : 100);
		setIntVar(Int.INJPCTHP,			(V.size()>1) ? CMath.s_int(V.get(1)) : 40);
		setIntVar(Int.INJPCTHPAMP,		(V.size()>2) ? CMath.s_int(V.get(2)) : 10);
		setIntVar(Int.INJPCTCHANCEAMP,	(V.size()>3) ? CMath.s_int(V.get(3)) : 100);
		setIntVar(Int.INJMULTIPLIER,	(V.size()>4) ? CMath.s_int(V.get(4)) : 4);
		setIntVar(Int.INJMINLEVEL,		(V.size()>5) ? CMath.s_int(V.get(5)) : 10);
		setIntVar(Int.INJBLEEDMINLEVEL,	(V.size()>6) ? CMath.s_int(V.get(6)) : 15);
		setIntVar(Int.INJBLEEDPCTHP,	(V.size()>7) ? CMath.s_int(V.get(7)) : 20);
		setIntVar(Int.INJBLEEDPCTCHANCE,(V.size()>8) ? CMath.s_int(V.get(8)) : 100);

		List<String> prowesses = CMParms.parseCommas(getStr("PROWESSOPTIONS"), true);
		int prowValue = Int.Prowesses.ARMOR_ABSOLUTE.value|
						Int.Prowesses.ARMOR_NUMBER.value|
						Int.Prowesses.COMBAT_ABSOLUTE.value|
						Int.Prowesses.COMBAT_NUMBER.value;
		if(prowesses.size() > 0)
		{
			prowValue = 0;
			for(String prow : prowesses)
			{
				final Int.Prowesses P = (Int.Prowesses)CMath.s_valueOf(Int.Prowesses.class, prow.toUpperCase().replace('-','_').trim());
				if(P == null)
					Log.errOut("CMProps","Invalid PROWESSOPTIONS value: "+prow);
				else
				if(P.value == 0)
					prowValue = 0;
				else
					prowValue |= P.value;
			}
		}
		setIntVar(Int.COMBATPROWESS, prowValue);
		
		String stateVar=getStr("STARTHP");
		if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
			setIntVar(Int.STARTHP,CMath.s_int(stateVar));
		stateVar=getStr("STARTMANA");
		if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
			setIntVar(Int.STARTMANA,CMath.s_int(stateVar));
		stateVar=getStr("STARTMOVE");
		if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
			setIntVar(Int.STARTMOVE,CMath.s_int(stateVar));
		
		setIntVar(Int.MAXITEMSHOWN,getStr("MAXITEMSHOWN"));
		setIntVar(Int.MUDSTATE,getStr("MUDSTATE"));

		setUpLowVar(Str.FORMULA_ATTACKADJUSTMENT, getStr("FORMULA_ATTACKADJUSTMENT","(50+@x1+(((@x2-9)/5)*((@x3-9)/5)*((@x3-9)/5))+@x4)-(0.15*@xx*@x5)-(0.15*@xx*@x6)-(0.3*@xx*@x7)"));
		setUpLowVar(Str.FORMULA_ARMORADJUSTMENT, getStr("FORMULA_ARMORADJUSTMENT","(@x1-( (((@x2-9)/5)*((@x3-9)/5)*((@x3-9)/5*@x8)) +(@x4*@x8)-(0.15*@xx>0*@x5)-(0.15*@xx>0*@x6)-(0.3*@xx>0*@x7)*@x9))-100"));
		setUpLowVar(Str.FORMULA_ATTACKFUDGEBONUS, getStr("FORMULA_ATTACKFUDGEBONUS","@x3 * (@x1 - @x2)* (@x1 - @x2)"));
		setUpLowVar(Str.FORMULA_PVPATTACKFUDGEBONUS, getStr("FORMULA_PVPATTACKFUDGEBONUS",getVar(Str.FORMULA_ATTACKFUDGEBONUS)));
		setUpLowVar(Str.FORMULA_CHANCESPELLCRIT, getStr("FORMULA_CHANCESPELLCRIT","(( ((@x2-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5) ))"));
		setUpLowVar(Str.FORMULA_PVPCHANCESPELLCRIT, getStr("FORMULA_PVPCHANCESPELLCRIT",getVar(Str.FORMULA_CHANCESPELLCRIT)));
		setUpLowVar(Str.FORMULA_DAMAGESPELLCRIT, getStr("FORMULA_DAMAGESPELLCRIT","(@x1*( ((@x2-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5) )/100.0)+(@x4/2)"));
		setUpLowVar(Str.FORMULA_PVPDAMAGESPELLCRIT, getStr("FORMULA_PVPDAMAGESPELLCRIT",getVar(Str.FORMULA_DAMAGESPELLCRIT)));
		setUpLowVar(Str.FORMULA_DAMAGESPELLFUDGE, getStr("FORMULA_DAMAGESPELLFUDGE","(@x1 - ( ((@x9-@x8)>0<1.0) * @x1 * (((@x9-@x8)<12.0)*((@x9-@x8)<12.0))/120.0))>0"));
		setUpLowVar(Str.FORMULA_PVPDAMAGESPELLFUDGE, getStr("FORMULA_PVPDAMAGESPELLFUDGE",getVar(Str.FORMULA_DAMAGESPELLFUDGE)));
		setUpLowVar(Str.FORMULA_DAMAGEMELEEFUDGE, getStr("FORMULA_DAMAGEMELEEFUDGE","(@x1 - ( ((@x9-@x8)>0<1.0) * @x1 * (((@x9-@x8)<12.0)*((@x9-@x8)<12.0))/120.0))>0"));
		setUpLowVar(Str.FORMULA_PVPDAMAGEMELEEFUDGE, getStr("FORMULA_PVPDAMAGEMELEEFUDGE",getVar(Str.FORMULA_DAMAGEMELEEFUDGE)));
		setUpLowVar(Str.FORMULA_DAMAGERANGEDFUDGE, getStr("FORMULA_DAMAGERANGEDFUDGE","(@x1 - ( ((@x9-@x8)>0<1.0) * @x1 * (((@x9-@x8)<12.0)*((@x9-@x8)<12.0))/120.0))>0"));
		setUpLowVar(Str.FORMULA_PVPDAMAGERANGEDFUDGE, getStr("FORMULA_PVPDAMAGERANGEDFUDGE",getVar(Str.FORMULA_DAMAGERANGEDFUDGE)));
		setUpLowVar(Str.FORMULA_DAMAGERANGEDTARGETED, getStr("FORMULA_DAMAGERANGEDTARGETED","((1?@x1)+((@x3-@x4)/2.5)-(0.5*@xx*@x8)+(0.5*@xx*@x9)+(0.2*@xx*@x10)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(Str.FORMULA_PVPDAMAGERANGEDTARGETED, getStr("FORMULA_PVPDAMAGERANGEDTARGETED",getVar(Str.FORMULA_DAMAGERANGEDTARGETED)));
		setUpLowVar(Str.FORMULA_DAMAGERANGEDSTATIC, getStr("FORMULA_DAMAGERANGEDSTATIC","((1?@x1)+((@x3-@x4)/2.5)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(Str.FORMULA_DAMAGEMELEETARGETED, getStr("FORMULA_DAMAGEMELEETARGETED","((1?@x1)+((@x2-10+@x3-@x4)/5)-(0.5*@xx*@x8)+(0.5*@xx*@x9)+(0.2*@xx*@x10)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(Str.FORMULA_PVPDAMAGEMELEETARGETED, getStr("FORMULA_PVPDAMAGEMELEETARGETED",getVar(Str.FORMULA_DAMAGEMELEETARGETED)));
		setUpLowVar(Str.FORMULA_DAMAGEMELEESTATIC, getStr("FORMULA_DAMAGEMELEESTATIC","((1?@x1)+((@x2-10+@x3-@x4)/5)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(Str.FORMULA_CHANCEWEAPONCRIT, getStr("FORMULA_CHANCEWEAPONCRIT","((((@x2-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5)))"));
		setUpLowVar(Str.FORMULA_PVPCHANCEWEAPONCRIT, getStr("FORMULA_PVPCHANCEWEAPONCRIT",getVar(Str.FORMULA_CHANCEWEAPONCRIT)));
		setUpLowVar(Str.FORMULA_DAMAGEWEAPONCRIT, getStr("FORMULA_DAMAGEWEAPONCRIT","(@x1 * (((@x2-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5))/50.0)+(@x4/2)"));
		setUpLowVar(Str.FORMULA_PVPDAMAGEWEAPONCRIT, getStr("FORMULA_PVPDAMAGEWEAPONCRIT",getVar(Str.FORMULA_DAMAGEWEAPONCRIT)));
		setUpLowVar(Str.FORMULA_NPCHITPOINTS, getStr("FORMULA_NPCHITPOINTS","3 + @x1 + (@x1 * @x2)"));
		setUpLowVar(Str.FORMULA_HITPOINTRECOVER, getStr("FORMULA_HITPOINTRECOVER","5+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0))*@x2/9.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) - (@xx/2.0*@x9))"));
		setUpLowVar(Str.FORMULA_MANARECOVER, getStr("FORMULA_MANARECOVER","25+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0) - (@xx*@x5/2.0))*@x2/50.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) - (@xx/2.0*@x9))"));
		setUpLowVar(Str.FORMULA_MOVESRECOVER, getStr("FORMULA_MOVESRECOVER","25+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0) - (@xx*@x5/2.0))*@x2/10.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) + (@xx/4.0*@x8) - (@xx/2.0*@x9))"));
		setUpLowVar(Str.FORMULA_TOTALCOMBATXP, getStr("FORMULA_TOTALCOMBATXP","100 + ((25*@x1) - (@x1*((25*@x1)^.5)))"));
		setUpLowVar(Str.FORMULA_INDCOMBATXP, getStr("FORMULA_INDCOMBATXP","(@x1 * (@x2 / @x3)) < 100"));

		final LanguageLibrary lang = CMLib.lang();
		Directions.instance().reInitialize(getInt("DIRECTIONS"), new Directions.DirectionWordTranslator()
		{
			@Override
			public String translate(String string)
			{
				return lang.L(string);
			}
		});

		resetSecurityVars();
		statCodeExtensions = getStrsStarting("EXTVAR_");

		// initialized elsewhere
		if(getVar(Str.MAILBOX)==null)
		{
			setVar(Str.MAILBOX, "");
			setIntVar(Int.MAXMAILBOX,0);
			setBoolVar(Bool.EMAILFORWARDING,false);
		}

		CMLib.propertiesLoaded();
		this.lastReset=System.currentTimeMillis();
	}

	/**
	 * Returns the array of bytes that must be sent after every prompt.
	 * Defined, in part, by PROMPTBEHAVIOR in coffeemud.ini file
	 * @return the array of bytes that must be sent after every prompt.
	 */
	public static byte[] getPromptSuffix()
	{
		return p().promptSuffix;
	}
	
	/**
	 * Returns the last time the properties for the callers thread group has
	 * been loaded.
	 * @return the time in ms when the callers properties were last parsed.
	 */
	public static long getLastResetTime()
	{
		return p().lastReset;
	}

	/**
	 * Reads this properties objects and sets security variables.  Can be re-called if
	 * any properties are changed.
	 */
	public final void resetSecurityVars()
	{
		String disable=getStr("DISABLE");
		if(getVar(Str.MULTICLASS).equalsIgnoreCase("DISABLED"))
			disable+=", CLASSES";
		CMSecurity.setAnyDisableVars(disable);
		CMSecurity.setAnyEnableVars(getStr("ENABLE"));
		CMSecurity.setDebugVars(getStr("DEBUG"));
		CMSecurity.setSaveFlags(getStr("SAVE"));
	}

	/**
	 * Returns true if the given msg contains words which would be filtered out by any of the filters, for
	 * the callers thread group.  Applicable filters are EMOTEFILTER, POSEFILTER, SAYFILTER, or CHANNELFILTER.
	 * @param msg the message to apply the filter to
	 * @return true if any filter would alter the string in any way
	 */
	public static boolean isAnyINIFiltered(String msg)
	{
		final Str[] filters = new Str[] {Str.EMOTEFILTER,Str.POSEFILTER,Str.SAYFILTER,Str.CHANNELFILTER};
		for(Str filter : filters)
		{
			if(isINIFiltered(msg,filter))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the given msg contains words which would be filtered out by the given filter for
	 * the callers thread group.
	 * @param msg the message to apply the filter to
	 * @param whichFilter the filter to apply, such as EMOTEFILTER, POSEFILTER, SAYFILTER, or CHANNELFILTER
	 * @return true if the filter would alter the string in any way
	 */
	public static boolean isINIFiltered(String msg, Str whichFilter)
	{
		List<String> filter=null;
		switch(whichFilter)
		{
		case EMOTEFILTER:
			filter = p().emoteFilter;
			break;
		case POSEFILTER:
			filter = p().poseFilter;
			break;
		case SAYFILTER:
			filter = p().sayFilter;
			break;
		case CHANNELFILTER:
			filter = p().channelFilter;
			break;
		default:
			return false;
		}
		if((filter==null)||(filter.size()==0)||(msg==null))
			return false;

		int fdex=0;
		int len=0;
		String upp=msg.toUpperCase();
		for(final String filterStr : filter)
		{
			if(filterStr.length()==0)
				continue;
			fdex=upp.indexOf(filterStr);
			int ctr=0;
			while((fdex>=0)&&((++ctr)<999))
			{
				len=fdex+filterStr.length();
				if(((fdex==0)
					||(Character.isWhitespace(upp.charAt(fdex-1)))
					||((fdex>1)&&(upp.charAt(fdex-2)=='^')))
				&&((len==upp.length())
					||(!Character.isLetter(upp.charAt(len)))))
				{
					for(;fdex<len;fdex++)
					{
						if(!Character.isWhitespace(msg.charAt(fdex)))
							return true;
					}
					fdex=upp.indexOf(filterStr);
				}
				else
				if(fdex<(filterStr.length()-1))
					fdex=upp.indexOf(filterStr,fdex+1);
				else
					fdex=-1;
			}
		}
		return false;
	}
	
	/**
	 * Alters the given message according to the given filter, by replacing any words
	 * found in the given filter with garbage characters.  
	 * @param msg the message to apply the filter to
	 * @param whichFilter the filter to apply, such as EMOTEFILTER, POSEFILTER, SAYFILTER, or CHANNELFILTER
	 * @return the altered msg
	 */
	public static String applyINIFilter(final String msg, final Str whichFilter)
	{
		final List<String> filter;
		switch(whichFilter)
		{
		case EMOTEFILTER:
			filter = p().emoteFilter;
			break;
		case POSEFILTER:
			filter = p().poseFilter;
			break;
		case SAYFILTER:
			filter = p().sayFilter;
			break;
		case CHANNELFILTER:
			filter = p().channelFilter;
			break;
		default:
			return msg;
		}
		if((filter==null)||(filter.size()==0))
			return msg;

		int fdex=0;
		int len=0;
		StringBuffer newMsg=null;
		String upp=msg.toUpperCase();
		for(final String filterStr : filter)
		{
			if(filterStr.length()==0)
				continue;
			fdex=upp.indexOf(filterStr);
			int ctr=0;
			while((fdex>=0)&&((++ctr)<999))
			{
				len=fdex+filterStr.length();
				if(((fdex==0)
					||(Character.isWhitespace(upp.charAt(fdex-1)))
					||((fdex>1)&&(upp.charAt(fdex-2)=='^')))
				&&((len==upp.length())
					||(!Character.isLetter(upp.charAt(len)))))
				{
					for(;fdex<len;fdex++)
					{
						if(!Character.isWhitespace(msg.charAt(fdex)))
						{
							if(newMsg==null)
								newMsg=new StringBuffer(msg);
							newMsg.setCharAt(fdex,FILTER_CHARS[fdex % FILTER_CHARS.length]);
							upp=newMsg.toString().toUpperCase();
						}
					}
					fdex=upp.indexOf(filterStr);
				}
				else
				if(fdex<(filterStr.length()-1))
					fdex=upp.indexOf(filterStr,fdex+1);
				else
					fdex=-1;
			}
		}
		if(newMsg!=null) 
			return newMsg.toString();
		return msg;
	}

	/**
	 * Returns true if, according to the callers properties, the given clan category
	 * makes it classified as a "Public Clan".
	 * @param clanCategory the category to check data for
	 * @return true if the category makes it a public category, false otherwise
	 */
	public static final boolean isPublicClanGvtCategory(final String clanCategory)
	{
		if((clanCategory==null)||(clanCategory.trim().length()==0))
			return true;
		final String upperClanCategory=clanCategory.toUpperCase().trim();
		return p().publicClanCats.contains(upperClanCategory);
	}

	/**
	 * Returns maximum  the maximum number of clans of this category a player 
	 * can belong to according to the callers properties.
	 * @param clanCategory the category to check data for
	 * @return the maximum number of clans of this category a player can belong to
	 */
	public static final int getMaxClansThisCategory(final String clanCategory)
	{
		if(clanCategory==null)
			return p().maxClanCatsMap.get("").intValue();
		final String upperClanCategory=clanCategory.toUpperCase().trim();
		if(p().maxClanCatsMap.containsKey(upperClanCategory))
			return p().maxClanCatsMap.get(upperClanCategory).intValue();
		return p().maxClanCatsMap.get("").intValue();
	}

	/**
	 * Returns the amount of milliseconds per mud tick.
	 * @return the amount of milliseconds per mud tick.
	 */
	public static final long getTickMillis()
	{
		return p().TIME_TICK;
	}

	/**
	 * Returns the amount of milliseconds per mud tick.
	 * @return the amount of milliseconds per mud tick.
	 */
	public final long tickMillis()
	{
		return TIME_TICK;
	}
	
	/**
	 * Returns the amount of milliseconds per mud tick, as a double.
	 * @return the amount of milliseconds per mud tick, as a double.
	 */
	public static final double getTickMillisD()
	{
		return p().TIME_TICK_DOUBLE;
	}

	/**
	 * Returns the number of real milliseconds that occur every in-game "hour"
	 * @return the number of real milliseconds that occur every in-game "hour"
	 */
	public static final long getMillisPerMudHour()
	{
		return p().MILLIS_PER_MUDHOUR;
	}

	/**
	 * Returns the number of game ticks that occur every in-game "hour"
	 * @return the number of game ticks that occur every in-game "hour"
	 */
	public static final long getTicksPerMudHour()
	{
		return p().MILLIS_PER_MUDHOUR / p().TIME_TICK;
	}

	/**
	 * Returns the number of game ticks that occur every real life minute
	 * @return the number of game ticks that occur every real life minute
	 */
	public static final long getTicksPerMinute()
	{
		return p().TICKS_PER_RLMIN;
	}

	/**
	 * Returns the number of game ticks that occur every real life hour (60 min)
	 * @return the number of game ticks that occur every real life hour (60 min)
	 */
	public static final long getTicksPerHour()
	{
		return p().TICKS_PER_RLHOUR;
	}

	/**
	 * Returns the number of game ticks that occur every real life day
	 * @return the number of game ticks that occur every real life day
	 */
	public static final long getTicksPerDay()
	{
		return p().TICKS_PER_RLDAY;
	}

	/**
	 * Returns true if the global mud theme includes the them given
	 * by the themeMask passed in.
	 * @param themeMask the theme mask to check for
	 * @return true if the given mask matches is included in muds preference, false otherwise.
	 */
	public static final boolean isTheme(final int themeMask)
	{
		return (getIntVar(Int.MUDTHEME)&themeMask)>0;
	}

	/**
	 * Loads the given iniFile by mud path, combines any multi-line entries, and returns all the 
	 * lines in the file in a list.
	 * @param iniFile the file to load
	 * @return the list of useful entries
	 */
	public static final List<String> loadEnumerablePage(final String iniFile)
	{
		final StringBuffer str=new CMFile(iniFile,null,CMFile.FLAG_LOGERRORS).text();
		if((str==null)||(str.length()==0)) 
			return new Vector<String>();
		final List<String> page=Resources.getFileLineVector(str);
		for(int p=0;p<(page.size()-1);p++)
		{
			String s=page.get(p).trim();
			if(s.startsWith("#")||s.startsWith("!"))
				continue;
			if((s.endsWith("\\"))&&(!s.endsWith("\\\\")))
			{
				s=s.substring(0,s.length()-1)+page.get(p+1).trim();
				page.remove(p+1);
				page.set(p,s);
				p=p-1;
			}
		}
		return page;
	}

	/**
	 * This method searches the given codes array for an entry matching the given code, and returns the value
	 * in xtraValues, or "" if not found.
	 * @param codes the code names to search
	 * @param xtraValues the values matching the codes, or null if unsupported
	 * @param code the code to search for
	 * @return the value from xtraValues, or ""
	 */
	public static final String getStatCodeExtensionValue(final String[] codes, final String[] xtraValues, final String code)
	{
		if(xtraValues!=null)
		{
			for(int x=0;x<xtraValues.length;x++)
			{
				if(codes[codes.length-x-1].equalsIgnoreCase(code))
				{
					return xtraValues[xtraValues.length-x-1];
				}
			}
		}
		return "";
	}

	/**
	 * This method searches the given codes array for an entry matching the given code, and set the value
	 * in xtraValues when found.
	 * @param codes the code names to search
	 * @param xtraValues the values matching the codes, or null if unsupported
	 * @param code the code to search for
	 * @param val the value to set the stat to
	 */
	public static void setStatCodeExtensionValue(final String[] codes, final String[] xtraValues, final String code, final String val)
	{
		if(xtraValues!=null)
		{
			for(int x=0;x<xtraValues.length;x++)
			{
				if(codes[codes.length-x-1].equalsIgnoreCase(code))
				{
					xtraValues[xtraValues.length-x-1]=val;
				}
			}
		}
	}

	private static final List<String> getStatCodeExtensions(Class<?> C, final String ID)
	{
		final String[][] statCodeExtensions = p().statCodeExtensions;
		if( statCodeExtensions == null) 
			return null;
		final List<String> V=new Vector<String>();
		String myClassName=ID;
		V.add(myClassName.toUpperCase());
		for(;C!=null;C=C.getSuperclass())
		{
			myClassName=C.getName();
			final int x=myClassName.lastIndexOf('.');
			if(x>=0)
				V.add(myClassName.substring(x+1).toUpperCase());
			else
				V.add(myClassName.toUpperCase());
		}
		if(V.size()==0)
			return null;
		for(final Iterator<String> v=V.iterator();v.hasNext();)
		{
			myClassName = v.next();
			for (final String[] statCodeExtension : statCodeExtensions)
			{
				if(statCodeExtension[0].equals(myClassName))
				{
					return CMParms.parseCommas(statCodeExtension[1],true);
				}
			}
		}
		return null;
	}

	private static final List<String> getStatCodeExtentions(final CMObject O)
	{
		String name;
		try
		{
			name = O.ID();
		}
		catch (final NullPointerException e)
		{
			name = O.getClass().getSimpleName();
		}
		return getStatCodeExtensions(O.getClass(), name);
	}

	/**
	 * Checks the properties for any "extra" properties attached to an object of
	 * the given object type, and if found, constructs a string array to hold
	 * all of the extra values to go with the extra properties, and returns it.
	 * The object should save this array for its use.
	 * @param O the object to find extra properties for
	 * @return a string array to hold any extra values, or null if not applicable
	 */
	public static final String[] getExtraStatCodesHolder(final CMObject O)
	{
		final List<String> addedStatCodesV = getStatCodeExtentions(O);
		if((addedStatCodesV == null)||(addedStatCodesV.size()==0))
			return null;
		final String[] statHolder= new String[addedStatCodesV.size()];
		for(int s=0;s<statHolder.length;s++)
			statHolder[s]="";
		return statHolder;
	}

	/**
	 * Checks the properties for any "extra" properties attached to an object of
	 * the given object type, and if found, constructs a string array to hold
	 * all of the object "base" stat codes, and the extra stat codes, and returns it.
	 * The object should save this array for its use.
	 * @param baseStatCodes the base set of stat codes that apply to this object
	 * @param O the object to find extra properties for
	 * @return a string array to hold all applicable stat codes, or just the base ones given if N/A
	 */
	public static final String[] getStatCodesList(final String[] baseStatCodes, final CMObject O)
	{
		final List<String> addedStatCodesV = getStatCodeExtentions(O);
		if(addedStatCodesV == null)
			return baseStatCodes;

		final String[] newStatCodes = new String[baseStatCodes.length + addedStatCodesV.size()];
		for(int x=0;x<baseStatCodes.length;x++)
			newStatCodes[x] = baseStatCodes[x];
		for(int x=0;x<addedStatCodesV.size();x++)
			newStatCodes[x+baseStatCodes.length] = addedStatCodesV.get(x);
		return newStatCodes;
	}
}
