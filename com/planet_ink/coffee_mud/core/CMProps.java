package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.util.*;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

/*
   Copyright 2000-2013 Bo Zimmerman

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
public class CMProps extends Properties
{
	private static final CMProps[] props=new CMProps[256];
	public CMProps()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null) props[c]=this;
	}
	public static final CMProps instance()
	{
		final CMProps p=p();
		if(p==null) return new CMProps();
		return p;
	}
	public static final CMProps instance(char c){ return props[c];}
	private static final CMProps p(){ return props[Thread.currentThread().getThreadGroup().getName().charAt(0)];}

	public static final long serialVersionUID=0;
	public static final int SYSTEM_PKILL=0;
	public static final int SYSTEM_MULTICLASS=1;
	public static final int SYSTEM_PLAYERDEATH=2;
	public static final int SYSTEM_PLAYERFLEE=3;
	public static final int SYSTEM_SHOWDAMAGE=4;
	public static final int SYSTEM_EMAILREQ=5;
	public static final int SYSTEM_ESC0=6;
	public static final int SYSTEM_ESC1=7;
	public static final int SYSTEM_ESC2=8;
	public static final int SYSTEM_ESC3=9;
	public static final int SYSTEM_ESC4=10;
	public static final int SYSTEM_ESC5=11;
	public static final int SYSTEM_ESC6=12;
	public static final int SYSTEM_ESC7=13;
	public static final int SYSTEM_ESC8=14;
	public static final int SYSTEM_ESC9=15;
	public static final int SYSTEM_MSPPATH=16;
	public static final int SYSTEM_BADNAMES=17;
	public static final int SYSTEM_SKILLCOST=18;
	public static final int SYSTEM_COMMONCOST=19;
	public static final int SYSTEM_LANGCOST=20;
	public static final int SYSTEM_AUTOPURGE=21;
	public static final int SYSTEM_MUDNAME=22;
	public static final int SYSTEM_MUDVER=23;
	public static final int SYSTEM_MUDSTATUS=24;
	public static final int SYSTEM_MUDPORTS=25;
	public static final int SYSTEM_CORPSEGUARD=26;
	public static final int SYSTEM_INIPATH=27;
	public static final int SYSTEM_MUDBINDADDRESS=28;
	public static final int SYSTEM_MUDDOMAIN=29;
	public static final int SYSTEM_ADMINEMAIL=30;
	public static final int SYSTEM_PREJUDICE=31;
	public static final int SYSTEM_BUDGET=32;
	public static final int SYSTEM_DEVALUERATE=33;
	public static final int SYSTEM_INVRESETRATE=34;
	public static final int SYSTEM_EMOTEFILTER=35;
	public static final int SYSTEM_SAYFILTER=36;
	public static final int SYSTEM_CHANNELFILTER=37;
	public static final int SYSTEM_CLANTROPPK=38;
	public static final int SYSTEM_MAILBOX=39;
	public static final int SYSTEM_CLANTROPCP=40;
	public static final int SYSTEM_CLANTROPEXP=41;
	public static final int SYSTEM_CLANTROPAREA=42;
	public static final int SYSTEM_COLORSCHEME=43;
	public static final int SYSTEM_SMTPSERVERNAME=44;
	public static final int SYSTEM_EXPCONTACTLINE=45;
	public static final int SYSTEM_AUTOWEATHERPARMS=46;
	public static final int SYSTEM_MXPIMAGEPATH=47;
	public static final int SYSTEM_IGNOREMASK=48;
	public static final int SYSTEM_SIPLET=49;
	public static final int SYSTEM_PREFACTIONS=50;
	public static final int SYSTEM_AUTOAREAPROPS=51;
	public static final int SYSTEM_MOBDEATH=52;
	public static final int SYSTEM_I3ROUTERS=53;
	public static final int SYSTEM_IDLETIMERS=54;
	public static final int SYSTEM_PRICEFACTORS=55;
	public static final int SYSTEM_ITEMLOOTPOLICY=56;
	public static final int SYSTEM_AUCTIONRATES=57;
	public static final int SYSTEM_DEFAULTPROMPT=58;
	public static final int SYSTEM_PRIVATERESOURCES=59;
	public static final int SYSTEM_CHARCREATIONSCRIPTS=60;
	public static final int SYSTEM_CHARSETINPUT=61;
	public static final int SYSTEM_CHARSETOUTPUT=62;
	public static final int SYSTEM_DEFAULTPLAYERFLAGS=63;
	public static final int SYSTEM_FORMULA_ATTACKADJUSTMENT=64;
	public static final int SYSTEM_FORMULA_ARMORADJUSTMENT=65;
	public static final int SYSTEM_FORMULA_ATTACKFUDGEBONUS=66;
	public static final int SYSTEM_FORMULA_CHANCESPELLCRIT=67;
	public static final int SYSTEM_FORMULA_DAMAGESPELLCRIT=68;
	public static final int SYSTEM_FORMULA_DAMAGERANGEDTARGETED=69;
	public static final int SYSTEM_FORMULA_DAMAGERANGEDSTATIC=70;
	public static final int SYSTEM_FORMULA_DAMAGEMELEETARGETED=71;
	public static final int SYSTEM_FORMULA_DAMAGEMELEESTATIC=72;
	public static final int SYSTEM_FORMULA_CHANCEWEAPONCRIT=73;
	public static final int SYSTEM_FORMULA_DAMAGEWEAPONCRIT=74;
	public static final int SYSTEM_FORMULA_NPCHITPOINTS=75;
	public static final int SYSTEM_FORMULA_DAMAGESPELLFUDGE=76;
	public static final int SYSTEM_FORMULA_DAMAGEMELEEFUDGE=77;
	public static final int SYSTEM_FORMULA_DAMAGERANGEDFUDGE=78;
	public static final int SYSTEM_WIZLISTMASK=79;
	public static final int SYSTEM_AUTOREACTION=80;
	public static final int SYSTEM_POSEFILTER=81;
	public static final int SYSTEM_STARTINGITEMS=82;
	public static final int SYSTEM_STATCOSTS=83;
	public static final int SYSTEM_CLANTROPMB=84;
	public static final int SYSTEM_CLANTROPLVL=85;
	public static final int SYSTEM_FORMULA_PVPATTACKFUDGEBONUS=86;
	public static final int SYSTEM_FORMULA_PVPCHANCESPELLCRIT=87;
	public static final int SYSTEM_FORMULA_PVPDAMAGESPELLCRIT=88;
	public static final int SYSTEM_FORMULA_PVPDAMAGERANGEDTARGETED=89;
	public static final int SYSTEM_FORMULA_PVPDAMAGEMELEETARGETED=90;
	public static final int SYSTEM_FORMULA_PVPCHANCEWEAPONCRIT=91;
	public static final int SYSTEM_FORMULA_PVPDAMAGEWEAPONCRIT=92;
	public static final int SYSTEM_FORMULA_PVPDAMAGESPELLFUDGE=93;
	public static final int SYSTEM_FORMULA_PVPDAMAGEMELEEFUDGE=94;
	public static final int SYSTEM_FORMULA_PVPDAMAGERANGEDFUDGE=95;
	public static final int NUM_SYSTEM=96;

	public static final int SYSTEMI_EXPRATE=0;
	public static final int SYSTEMI_SKYSIZE=1;
	public static final int SYSTEMI_MAXSTAT=2;
	public static final int SYSTEMI_EDITORTYPE=3;
	public static final int SYSTEMI_MINCLANMEMBERS=4;
	public static final int SYSTEMI_DAYSCLANDEATH=5;
	public static final int SYSTEMI_MINCLANLEVEL=6;
	public static final int SYSTEMI_MANACOST=7;
	//public static final int SYSTEMI_DBPINGINTMINS=8;
	//public static final int SYSTEMI_LANGTRAINCOST=9;
	//public static final int SYSTEMI_SKILLTRAINCOST=10;
	//public static final int SYSTEMI_COMMONPRACCOST=11;
	//public static final int SYSTEMI_LANGPRACCOST=12;
	//public static final int SYSTEMI_SKILLPRACCOST=13;
	public static final int SYSTEMI_CLANCOST=14;
	public static final int SYSTEMI_PAGEBREAK=15;
	public static final int SYSTEMI_FOLLOWLEVELDIFF=16;
	public static final int SYSTEMI_LASTPLAYERLEVEL=17;
	public static final int SYSTEMI_CLANENCHCOST=18;
	public static final int SYSTEMI_BASEMAXSTAT=19;
	public static final int SYSTEMI_MANAMINCOST=20;
	public static final int SYSTEMI_MAXCLANMEMBERS=21;
	public static final int SYSTEMI_MANACONSUMETIME=22;
	public static final int SYSTEMI_MANACONSUMEAMT=23;
	public static final int SYSTEMI_MUDBACKLOG=24;
	public static final int SYSTEMI_TICKSPERMUDDAY=25;
	public static final int SYSTEMI_COMBATSYSTEM=26;
	public static final int SYSTEMI_JOURNALLIMIT=27;
	public static final int SYSTEMI_TICKSPERMUDMONTH=28;
	public static final int SYSTEMI_MUDTHEME=29;
	public static final int SYSTEMI_INJPCTCHANCE=30;
	public static final int SYSTEMI_INJPCTHP=31;
	public static final int SYSTEMI_INJPCTHPAMP=32;
	public static final int SYSTEMI_INJPCTCHANCEAMP=33;
	public static final int SYSTEMI_INJMULTIPLIER=34;
	public static final int SYSTEMI_STARTHP=35;
	public static final int SYSTEMI_STARTMANA=36;
	public static final int SYSTEMI_STARTMOVE=37;
	public static final int SYSTEMI_TRIALDAYS=38;
	public static final int SYSTEMI_EQVIEW=39;
	public static final int SYSTEMI_MAXCONNSPERIP=40;
	public static final int SYSTEMI_MAXNEWPERIP=41;
	public static final int SYSTEMI_MAXMAILBOX=42;
	public static final int SYSTEMI_JSCRIPTS=43;
	public static final int SYSTEMI_INJMINLEVEL=44;
	public static final int SYSTEMI_DEFCMDTIME=45;
	public static final int SYSTEMI_DEFCOMCMDTIME=46;
	public static final int SYSTEMI_DEFABLETIME=47;
	public static final int SYSTEMI_DEFCOMABLETIME=48;
	public static final int SYSTEMI_INJBLEEDMINLEVEL=49;
	public static final int SYSTEMI_INJBLEEDPCTHP=50;
	public static final int SYSTEMI_INJBLEEDPCTCHANCE=51;
	public static final int SYSTEMI_EXPIRE_MONSTER_EQ=52;
	public static final int SYSTEMI_EXPIRE_PLAYER_DROP=53;
	public static final int SYSTEMI_EXPIRE_RESOURCE=54;
	public static final int SYSTEMI_EXPIRE_MONSTER_BODY=55;
	public static final int SYSTEMI_EXPIRE_PLAYER_BODY=56;
	public static final int SYSTEMI_MAXITEMSHOWN=57;
	public static final int SYSTEMI_STARTSTAT=58;
	public static final int SYSTEMI_RECOVERRATE=59;
	public static final int SYSTEMI_COMMONACCOUNTSYSTEM=60;
	public static final int SYSTEMI_MAXCONNSPERACCOUNT=61;
	public static final int SYSTEMI_EXVIEW=62;
	public static final int SYSTEMI_MUDSTATE=63;
	public static final int SYSTEMI_OBJSPERTHREAD=64;
	public static final int SYSTEMI_MAXCOMMONSKILLS=65;
	public static final int SYSTEMI_MAXCRAFTINGSKILLS=66;
	public static final int SYSTEMI_MAXNONCRAFTINGSKILLS=67;
	public static final int SYSTEMI_MAXLANGUAGES=68;
	public static final int SYSTEMI_WALKCOST=69;
	public static final int SYSTEMI_RUNCOST=70;
	public static final int SYSTEMI_ACCOUNTPURGEDAYS=71;
	public static final int SYSTEMI_AWARERANGE=72;
	public static final int SYSTEMI_MINSESSIONTHREADS=73;
	public static final int SYSTEMI_MAXSESSIONTHREADS=74;
	public static final int SYSTEMI_DUELTICKDOWN=75;
	public static final int SYSTEMI_BASEMINSTAT=76;
	public static final int NUMI_SYSTEM=77;

	public static final int SYSTEMB_MOBCOMPRESS=0;
	public static final int SYSTEMB_ITEMDCOMPRESS=1;
	public static final int SYSTEMB_ROOMDCOMPRESS=2;
	public static final int SYSTEMB_MOBDCOMPRESS=3;
	public static final int SYSTEMB_MUDSTARTED=4;
	public static final int SYSTEMB_EMAILFORWARDING=5;
	public static final int SYSTEMB_MOBNOCACHE=6;
	public static final int SYSTEMB_ROOMDNOCACHE=7;
	public static final int SYSTEMB_MUDSHUTTINGDOWN=8;
	public static final int SYSTEMB_ACCOUNTEXPIRATION=9;
	public static final int SYSTEMB_INTRODUCTIONSYSTEM=10;
	public static final int SYSTEMB_FILERESOURCENOCACHE=11;
	public static final int SYSTEMB_CATALOGNOCACHE=12;
	public static final int SYSTEMB_MAPFINDSNOCACHE=13;
	public static final int SYSTEMB_HASHPASSWORDS=14;
	public static final int NUMB_SYSTEM=15;

	public static final int SYSTEML_SUBSCRIPTION_STRS=0;
	public static final int NUML_SYSTEM=1;

	public static final int SYSTEMLF_DAMAGE_WORDS_THRESHOLDS=0;
	public static final int SYSTEMLF_DAMAGE_WORDS=1;
	public static final int SYSTEMLF_HEALTH_CHART=2;
	public static final int SYSTEMLF_MISS_DESCS=3;
	public static final int SYSTEMLF_WEAPON_MISS_DESCS=4;
	public static final int SYSTEMLF_PROWESS_DESCS_CEILING=5;
	public static final int SYSTEMLF_PROWESS_DESCS=6;
	public static final int SYSTEMLF_ARMOR_DESCS_CEILING=7;
	public static final int SYSTEMLF_ARMOR_DESCS=8;
	public static final int SYSTEMLF_EXP_CHART=9;
	public static final int SYSTEMLF_ARMOR_MISFITS=10;
	public static final int SYSTEMLF_MAGIC_WORDS=11;
	public static final int SYSTEMLF_TODCHANGE_OUTSIDE=12;
	public static final int SYSTEMLF_TODCHANGE_INSIDE=13;
	public static final int SYSTEMLF_WEATHER_ENDS=14;
	public static final int SYSTEMLF_WEAPON_HIT_DESCS=15;
	public static final int SYSTEMLF_WEATHER_CLEAR=16; // try to always and forever keep these at the end...
	public static final int SYSTEMLF_WEATHER_CLOUDY=17;
	public static final int SYSTEMLF_WEATHER_WINDY=18;
	public static final int SYSTEMLF_WEATHER_RAIN=19;
	public static final int SYSTEMLF_WEATHER_THUNDERSTORM=20;
	public static final int SYSTEMLF_WEATHER_SNOW=21;
	public static final int SYSTEMLF_WEATHER_HAIL=22;
	public static final int SYSTEMLF_WEATHER_HEAT=23;
	public static final int SYSTEMLF_WEATHER_SLEET=24;
	public static final int SYSTEMLF_WEATHER_BLIZZARD=25;
	public static final int SYSTEMLF_WEATHER_DUST=26;
	public static final int SYSTEMLF_WEATHER_DROUGHT=27;
	public static final int SYSTEMLF_WEATHER_COLD=28;
	public static final int SYSTEMLF_WEATHER_NONE=29;

	public static final String[] SYSTEMLF_KEYS={
									"DAMAGE_WORDS_THRESHOLDS",
									"DAMAGE_WORDS",
									"HEALTH_CHART",
									"MISS_DESCS",
									"WEAPON_MISS_DESCS",
									"PROWESS_DESCS_CEILING",
									"PROWESS_DESCS",
									"ARMOR_DESCS_CEILING",
									"ARMOR_DESCS",
									"EXP_CHART",
									"ARMOR_MISFITS",
									"MAGIC_WORDS",
									"TOD_CHANGE_OUTSIDE",
									"TOD_CHANGE_INSIDE",
									"WEATHER_ENDS",
									"WEAPON_HIT_DESCS",
									"WEATHER_CLEAR",
									"WEATHER_CLOUDY",
									"WEATHER_WINDY",
									"WEATHER_RAIN",
									"WEATHER_THUNDERSTORM",
									"WEATHER_SNOW",
									"WEATHER_HAIL",
									"WEATHER_HEAT_WAVE",
									"WEATHER_SLEET",
									"WEATHER_BLIZZARD",
									"WEATHER_DUSTSTORM",
									"WEATHER_DROUGHT",
									"WEATHER_WINTER_COLD",
									"WEATHER_NONE"
	};
	public static final int NUMLF_SYSTEM=SYSTEMLF_KEYS.length;

	public static final int SYSTEMWL_CONNS=0;
	public static final int SYSTEMWL_LOGINS=1;
	public static final int SYSTEMWL_NEWPLAYERS=2;
	public static final int NUMWL_SYSTEM=3;
	
	protected final String[]	sysVars=new String[NUM_SYSTEM];
	protected final Integer[]   sysInts=new Integer[NUMI_SYSTEM];
	protected final Boolean[]   sysBools=new Boolean[NUMB_SYSTEM];
	protected final String[][]  sysLists=new String[NUML_SYSTEM][];
	protected final Object[]	sysLstFileLists=new Object[NUMLF_SYSTEM];
	protected final List<String>sayFilter=new Vector<String>();
	protected final List<String>channelFilter=new Vector<String>();
	protected final List<String>emoteFilter=new Vector<String>();
	protected final List<String>poseFilter=new Vector<String>();
	protected final DVector 	newusersByIP=new DVector(2);
	protected String[][]		statCodeExtensions = null;
	protected Pattern[][]		whiteLists = new Pattern[0][];
	protected int   			pkillLevelDiff=26;
	protected boolean   		loaded=false;
	protected long  			TIME_TICK=4000;
	protected long  			MILLIS_PER_MUDHOUR=600000;
	protected long  			TICKS_PER_RLMIN=(int)Math.round(60000.0/TIME_TICK);
	protected long  			TICKS_PER_RLHOUR=TICKS_PER_RLMIN * 60;
	protected long  			TICKS_PER_RLDAY=TICKS_PER_RLHOUR * 24;
	protected double			TIME_TICK_DOUBLE=TIME_TICK;
	protected final Map<String,Integer>		maxClanCatsMap				=new HashMap<String,Integer>();
	protected final Set<String>				publicClanCats				=new HashSet<String>();
	protected final Map<String,Double>		skillMaxManaExceptions		=new HashMap<String,Double>();
	protected final Map<String,Double>		skillMinManaExceptions		=new HashMap<String,Double>();
	protected final Map<String,Double>		skillActionCostExceptions	=new HashMap<String,Double>();
	protected final Map<String,Double>		skillComActionCostExceptions=new HashMap<String,Double>();
	protected final Map<String,Double>		cmdActionCostExceptions		=new HashMap<String,Double>();
	protected final Map<String,Double>		cmdComActionCostExceptions	=new HashMap<String,Double>();
	protected final Map<String,ExpertiseLibrary.SkillCostDefinition> commonCost  =new HashMap<String,ExpertiseLibrary.SkillCostDefinition>();
	protected final Map<String,ExpertiseLibrary.SkillCostDefinition> skillsCost  =new HashMap<String,ExpertiseLibrary.SkillCostDefinition>();
	protected final Map<String,ExpertiseLibrary.SkillCostDefinition> languageCost=new HashMap<String,ExpertiseLibrary.SkillCostDefinition>();

	public CMProps(InputStream in)
	{
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null) props[c]=this;
		try
		{
			this.load(in);
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}
	
	public CMProps(String filename)
	{
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null) props[c]=this;
		try
		{
			final CMFile F=new CMFile(filename,null,false);
			if(F.exists())
			{
				this.load(new ByteArrayInputStream(F.textUnformatted().toString().getBytes()));
				loaded=true;
			}
			else
				loaded=false;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}

	public final boolean load(String filename)
	{
		try
		{
			this.load(new ByteArrayInputStream(new CMFile(filename,null,false).raw()));
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
		return loaded;
	}

	public CMProps(final Properties p, final String filename)
	{
		super(p);
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(props[c]==null) props[c]=this;

		try
		{
			this.load(new ByteArrayInputStream(new CMFile(filename,null,false).raw()));
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}

	public static final CMProps loadPropPage(final String iniFile)
	{
		CMProps page=new CMProps(iniFile);
		if(!page.loaded)
			return null;
		return page;
	}
	
	public boolean isLoaded()
	{
		return loaded;
	}

	/** retrieve a local .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=getPrivateStr("TAG");
	* @param tagToGet   the property tag to retreive.
	* @return String   the value of the .ini file tag
	*/
	public final String getPrivateStr(final String tagToGet)
	{
		final String s=getProperty(tagToGet);
		if(s==null) return "";
		return s;
	}
	
	/** retrieve raw local .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=getRawPrivateStr("TAG");
	* @param tagToGet   the property tag to retreive.
	* @return String   the value of the .ini file tag
	*/
	public final String getRawPrivateStr(final String tagToGet)
	{
		return getProperty(tagToGet);
	}
	
	/** retrieve a particular .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=getStr(p,"TAG");
	* @param tagToGet    the property tag to retreive.
	* @return String	the value of the .ini file tag
	*/
	public final String getStr(final String tagToGet)
	{
		final String thisTag=this.getProperty(tagToGet);
		if((thisTag==null)&&(props[MudHost.MAIN_HOST]!=null)&&(props[MudHost.MAIN_HOST]!=this))
			return props[MudHost.MAIN_HOST].getStr(tagToGet);
		if(thisTag==null) return "";
		return thisTag;
	}

	/** retrieve a particular .ini file entry as a string, or use a default
	*
	* <br><br><b>Usage:</b>  String s=getStr(p,"TAG");
	* @param tagToGet    the property tag to retreive.
	* @return String	the value of the .ini file tag
	*/
	public final String getStr(final String tagToGet, final String defaultVal)
	{
		String thisTag=this.getProperty(tagToGet);
		if((thisTag==null)&&(props[MudHost.MAIN_HOST]!=null)&&(props[MudHost.MAIN_HOST]!=this))
			thisTag=props[MudHost.MAIN_HOST].getStr(tagToGet);
		if((thisTag==null)||(thisTag.length()==0)) return defaultVal;
		return thisTag;
	}
	
	/** retrieve particular .ini file entrys as a string array
	*
	* <br><br><b>Usage:</b>  String s=getStrsStarting(p,"TAG");
	* @param tagStartersToGet    the property tag to retreive.
	* @return String	the value of the .ini file tag
	*/
	public final String[][] getStrsStarting(String tagStartersToGet)
	{
		final DVector strBag = new DVector(2);
		tagStartersToGet = tagStartersToGet.toUpperCase();
		for(Enumeration<?> e=propertyNames(); e.hasMoreElements();)
		{
			final String propName = (String)e.nextElement();
			if(propName.toUpperCase().startsWith(tagStartersToGet))
			{
				String subPropName = propName.substring(tagStartersToGet.length()).toUpperCase();
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
			strArray[s][0] = (String)strBag.elementAt(s,1);
			strArray[s][1] = (String)strBag.elementAt(s,2);
		}
		return strArray;
	}

	/** retrieve a particular .ini file entry as a boolean
	*
	* <br><br><b>Usage:</b>  boolean i=getBoolean("TAG");
	* @param tagToGet   the property tag to retreive.
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
	* <br><br><b>Usage:</b>  double i=getDouble("TAG");
	* @param tagToGet    the property tag to retreive.
	* @return int    the value of the .ini file tag
	*/
	public final double getDouble(final String tagToGet)
	{
		try
		{
			return Double.parseDouble(getStr(tagToGet));
		}
		catch(Exception e)
		{
			return 0.0;
		}
	}

	/** retrieve a particular .ini file entry as an integer
	*
	* <br><br><b>Usage:</b>  int i=getInt("TAG");
	* @param tagToGet    the property tag to retreive.
	* @return int    the value of the .ini file tag
	*/
	public final int getInt(final String tagToGet)
	{
		try
		{
			return Integer.parseInt(getStr(tagToGet));
		}
		catch(Exception t)
		{
			return 0;
		}
	}

	/** retrieve a particular .ini file entry as a long
	*
	* <br><br><b>Usage:</b>  long i=getInt("TAG");
	* @param tagToGet    the property tag to retreive.
	* @return long the value of the .ini file tag
	*/
	public final long getLong(final String tagToGet)
	{
		try
		{
			return Long.parseLong(getStr(tagToGet));
		}
		catch(Exception t)
		{
			return 0;
		}
	}
	
	public static final double getActionCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().cmdActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	public static final double getCombatActionCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().cmdComActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	public static final double getActionCost(final String ID)
	{
		return getActionCost(ID,CMath.div(getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0));
	}

	public static final double getCombatActionCost(final String ID)
	{
		return getCombatActionCost(ID,CMath.div(getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0));
	}

	public static final double getActionSkillCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().skillActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	public static final double getCombatActionSkillCost(final String ID, final double defaultValue)
	{
		final Map<String,Double> overrides=p().skillComActionCostExceptions;
		final String uID=ID.toUpperCase();
		if(overrides.containsKey(uID))
			return overrides.get(uID).doubleValue();
		return defaultValue;
	}

	public static final double getActionSkillCost(final String ID)
	{
		return getActionSkillCost(ID,CMath.div(getIntVar(CMProps.SYSTEMI_DEFABLETIME),100.0));
	}

	public static final double getCombatActionSkillCost(final String ID)
	{
		return getCombatActionSkillCost(ID,CMath.div(getIntVar(CMProps.SYSTEMI_DEFCOMABLETIME),100.0));
	}

	public static final int getPKillLevelDiff(){return p().pkillLevelDiff;}

	public static final String getVar(final int varNum)
	{
		try { return p().sysVars[varNum];} 
		catch(Exception t) { return ""; }
	}

	public static final int getIntVar(final int varNum)
	{
		try { return p().sysInts[varNum].intValue(); } 
		catch(Exception t) { return -1; }
	}

	public static final String[] getListVar(final int varNum)
	{
		try { return p().sysLists[varNum]; } 
		catch(Exception t) { return new String[0]; }
	}

	public static final boolean getBoolVar0(final int varNum)
	{
		if((varNum<0)||(varNum>=NUMB_SYSTEM)) return false;
		if(instance(MudHost.MAIN_HOST).sysBools[varNum]==null) return false;
		return instance(MudHost.MAIN_HOST).sysBools[varNum].booleanValue();
	}

	public static final boolean getBoolVar(final int varNum)
	{
		try { return p().sysBools[varNum].booleanValue(); } 
		catch(Exception t) { return false; }
	}

	public static final void setBoolVar(final int varNum, final boolean val)
	{
		if((varNum<0)||(varNum>=NUMB_SYSTEM)) return ;
		p().sysBools[varNum]=Boolean.valueOf(val);
	}

	public static final void setBoolVar0(final int varNum, final boolean val)
	{
		if((varNum<0)||(varNum>=NUMB_SYSTEM)) return ;
		instance(MudHost.MAIN_HOST).sysBools[varNum]=Boolean.valueOf(val);
	}

	public static final void setIntVar(final int varNum, final int val)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
		p().sysInts[varNum]=Integer.valueOf(val);
	}

	public static final void setIntVar(final int varNum, String val)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
		if(val==null) val="0";
		p().sysInts[varNum]=Integer.valueOf(CMath.s_int(val.trim()));
	}

	public static final void setIntVar(final int varNum, String val, final int defaultValue)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
		if((val==null)||(val.length()==0)) val=""+defaultValue;
		p().sysInts[varNum]=Integer.valueOf(CMath.s_int(val.trim()));
	}

	public static final void setListVar(final int varNum, String[] var)
	{
		if((varNum<0)||(varNum>=NUML_SYSTEM)) return ;
		if(var==null) var=new String[0];
		p().sysLists[varNum]=var;
	}

	public static final void addListVar(final int varNum, String var)
	{
		if((varNum<0)||(varNum>=NUML_SYSTEM)) return ;
		if(var==null) return;
		CMProps prop=p();
		if(prop.sysLists[varNum]==null)
			setListVar(varNum, new String[0]);
		String[] list=prop.sysLists[varNum];
		prop.sysLists[varNum]=Arrays.copyOf(list, list.length+1);
		list[list.length-1]=var;
	}

	public static final void setVar(final int varNum, String val, final boolean upperFy)
	{
		if(val==null) val="";
		setUpLowVar(varNum,upperFy?val.toUpperCase():val);
	}

	public static final void setVar(final int varNum, String val)
	{
		if(val==null) val="";
		setUpLowVar(varNum,val.toUpperCase());
	}
	
	private static final void setUpLowVar(final CMProps props, final int varNum, String val)
	{
		if((varNum<0)||(varNum>=NUM_SYSTEM)) return ;
		if(val==null) val="";
		props.sysVars[varNum]=val;
		switch(varNum)
		{
		case SYSTEM_PKILL:
			{
				final int x=val.indexOf('-');
				if(x>0)
					props.pkillLevelDiff=CMath.s_int(val.substring(x+1));
			}
			break;
		}
	}
	
	public static final void setUpLowVar(final int varNum, final String val)
	{ 
		setUpLowVar(p(),varNum,val); 
	}
	
	public static final void setUpAllLowVar(final int varNum, final String val)
	{ 
		for(int p=0;p<props.length;p++)
			if(props[p]!=null)
			   setUpLowVar(props[p],varNum,val);
	}

	public static final void setWhitelist(final CMProps props, final int listNum, final String list)
	{
		if((listNum<0)||(listNum>=NUMWL_SYSTEM)) return ;
		if(props.whiteLists.length<=listNum)
			props.whiteLists=Arrays.copyOf(props.whiteLists, listNum+1);
		props.whiteLists[listNum]=new Pattern[0];
		if((list==null)||(list.trim().length()==0))
			return;
		final List<String> parts=CMParms.parseCommas(list.trim(),true);
		for(String part : parts)
		{
			if(part.trim().length()==0)
				continue;
			props.whiteLists[listNum]=Arrays.copyOf(props.whiteLists[listNum], props.whiteLists[listNum].length+1);
			props.whiteLists[listNum][props.whiteLists[listNum].length-1]=Pattern.compile(part.trim(),Pattern.CASE_INSENSITIVE|Pattern.DOTALL|Pattern.CANON_EQ);
		}
	}

	public static final void setWhitelist(final int listNum, final String list)
	{
		setWhitelist(p(), listNum, list);
	}

	public static final boolean isOnWhiteList(final CMProps props, final int listNum, final String chk)
	{
		if((listNum<0)||(listNum>=NUMWL_SYSTEM)) 
			return false;
		if(props.whiteLists.length<=listNum)
			return false;
		if((chk==null)||(chk.trim().length()==0))
			return false;
		final Pattern[] patts=props.whiteLists[listNum];
		final String chkTrim=chk.trim();
		final CharSequence seq=chkTrim.subSequence(0, chkTrim.length());
		for(final Pattern p : patts)
		{
			if(p.matcher(seq).matches())
				return true;
		}
		return false;
	}

	public static final boolean isOnWhiteList(final int listNum, final String chk)
	{
		return isOnWhiteList(p(), listNum, chk);
	}

	public static final void setUpCosts(final String fieldName, final Map<String,ExpertiseLibrary.SkillCostDefinition> map, final Vector<String> fields)
	{
		final double[] doubleChecker=new double[10];
		for(String field : fields)
		{
			field=field.trim();
			if(field.length()==0) continue;
			final int typeIndex=field.lastIndexOf(' ');
			if(typeIndex<0)
			{
				Log.errOut("CMProps","Error parsing coffeemud.ini field "+fieldName+", value: "+field);
				continue;
			}
			final String type=field.substring(typeIndex+1).toUpperCase().trim();
			String formula=field.substring(0,typeIndex).trim();
			ExpertiseLibrary.CostType costType=(ExpertiseLibrary.CostType)CMath.s_valueOf(ExpertiseLibrary.CostType.values(), type);
			if(costType==null)
			{
				Log.errOut("CMProps","Error parsing coffeemud.ini field '"+fieldName+"', invalid type: "+type);
				Log.errOut("CMProps","Valid values include "+CMParms.toStringList(ExpertiseLibrary.CostType.values()));
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
			map.put(keyField.toUpperCase(), new ExpertiseLibrary.SkillCostDefinition(costType,formula));
		}
	}

	public static final ExpertiseLibrary.SkillCostDefinition getSkillTrainCostFormula(final String id)
	{ 
		final CMProps p=p();
		ExpertiseLibrary.SkillCostDefinition pair=p.skillsCost.get(id.toUpperCase());
		if(pair==null) pair=p.skillsCost.get("");
		if(pair==null) pair=new ExpertiseLibrary.SkillCostDefinition(ExpertiseLibrary.CostType.TRAIN, "1");
		return pair;
	}
	
	public static final ExpertiseLibrary.SkillCostDefinition getCommonTrainCostFormula(final String id)
	{ 
		final CMProps p=p();
		ExpertiseLibrary.SkillCostDefinition pair=p.commonCost.get(id.toUpperCase());
		if(pair==null) pair=p.commonCost.get("");
		if(pair==null) pair=new ExpertiseLibrary.SkillCostDefinition(ExpertiseLibrary.CostType.TRAIN, "1");
		return pair;
	}
	
	public static final ExpertiseLibrary.SkillCostDefinition getLangTrainCostFormula(final String id)
	{ 
		final CMProps p=p();
		ExpertiseLibrary.SkillCostDefinition pair=p.languageCost.get(id.toUpperCase());
		if(pair==null) pair=p.languageCost.get("");
		if(pair==null) pair=new ExpertiseLibrary.SkillCostDefinition(ExpertiseLibrary.CostType.TRAIN, "1");
		return pair;
	}
	
	public static final int getCountNewUserByIP(final String address)
	{
		int count=0;
		final DVector DV=p().newusersByIP;
		synchronized(DV)
		{
			for(int i=DV.size()-1;i>=0;i--)
				if(((String)DV.elementAt(i,1)).equalsIgnoreCase(address))
				{
					if(System.currentTimeMillis()>(((Long)DV.elementAt(i,2)).longValue()))
						DV.removeElementAt(i);
					else
						count++;
				}
		}
		return count;
	}
	public static final void addNewUserByIP(final String address)
	{
		final DVector DV=p().newusersByIP;
		synchronized(DV)
		{
			DV.addElement(address,Long.valueOf(System.currentTimeMillis()+TimeManager.MILI_DAY));
		}
	}

	public static final int getMinManaException(final String skillID)
	{
		final Map<String,Double> DV=p().skillMinManaExceptions;
		if(DV.containsKey(skillID.toUpperCase()))
			return DV.get(skillID.toUpperCase()).intValue();
		return Integer.MIN_VALUE;
	}
	public static final int getMaxManaException(final String skillID)
	{
		final Map<String,Double> DV=p().skillMaxManaExceptions;
		if(DV.containsKey(skillID.toUpperCase()))
			return DV.get(skillID.toUpperCase()).intValue();
		return Integer.MIN_VALUE;
	}

	private static final double setExceptionCosts(final String val, final Map<String,Double> set)
	{
		if(val==null) return 0;
		set.clear();
		final Vector<String> V=CMParms.parseCommas(val,true);
		String s=null;
		double endVal=0;
		for(int v=0;v<V.size();v++)
		{
			s=V.elementAt(v);
			if(CMath.isNumber(s)){ endVal=CMath.s_double(s); continue;}
			int x=s.indexOf(' ');
			if(CMath.isDouble(s.substring(x+1).trim()))
				set.put(s.substring(0,x).trim().toUpperCase(),Double.valueOf(CMath.s_double(s.substring(x+1).trim())));
		}
		return endVal;
	}

	private static final String getRawListFileEntry(final String key) 
	{
		Properties rawListData=(Properties)Resources.getResource("PARSED_LISTFILE");
		if(rawListData==null)
		{
			synchronized("PARSED_LISTFILE".intern())
			{
				rawListData=(Properties)Resources.getResource("PARSED_LISTFILE");
				if(rawListData==null)
				{
					rawListData=new Properties();
					final String listFileName=CMProps.p().getProperty("LISTFILE");
					final CMFile F=new CMFile(listFileName,null,true);
					if(F.exists())
					{
						try{
							rawListData.load(new InputStreamReader(new ByteArrayInputStream(F.raw()), CMProps.getVar(CMProps.SYSTEM_CHARSETINPUT)));
						} catch(IOException e){}
					}
					Resources.submitResource("PARSED_LISTFILE", rawListData);
					final Object[] set=p().sysLstFileLists; 
					for(int i=0;i<set.length;i++)
						set[i]=null;
				}
			}
		}
		final String val = rawListData.getProperty(key);
		if(val == null) Log.errOut("CMProps","Unable to load required list file entry: "+key);
		return val;
	}

	public static final int getListFileFirstInt(final int var)
	{
		if((var<0)||(var>=NUMLF_SYSTEM)) return -1;
		if(p().sysLstFileLists[var]==null)
			p().sysLstFileLists[var]=new int[]{(CMath.s_int(getRawListFileEntry(SYSTEMLF_KEYS[var])))};
		return ((int[])p().sysLstFileLists[var])[0];
	}

	public static final int[] getListFileIntList(final int var)
	{
		if((var<0)||(var>=NUMLF_SYSTEM)) return new int[0];
		if(p().sysLstFileLists[var]==null)
		{
			final Vector<String> V=CMParms.parseCommas(getRawListFileEntry(SYSTEMLF_KEYS[var]), true);
			final int[] set=new int[V.size()];
			for(int v=0;v<V.size();v++)
				set[v]=CMath.s_int(V.elementAt(v));
			p().sysLstFileLists[var]=set;
		}
		return ((int[])p().sysLstFileLists[var]);
	}

	private static final Object[][] getSLstFileVar(final int var)
	{
		if((var<0)||(var>=NUMLF_SYSTEM)) return new Object[0][];
		if(p().sysLstFileLists[var]==null)
		{
			final String[] baseArray = CMParms.toStringArray(CMParms.parseCommas(getRawListFileEntry(SYSTEMLF_KEYS[var]),false));
			final Object[][] finalArray=new Object[baseArray.length][];
			for(int s=0;s<finalArray.length;s++)
				if((baseArray[s]==null)||(baseArray[s].length()==0))
					finalArray[s]=new Object[]{""};
				else
					finalArray[s]=CMParms.toStringArray(CMParms.parseAny(baseArray[s], '|', false));
			p().sysLstFileLists[var]=finalArray;
		}
		return (Object[][])p().sysLstFileLists[var];
	}

	public static final Object[][][] getListFileGrid(final int var)
	{
		if((var<0)||(var>=NUMLF_SYSTEM)) return new String[0][0][];
		if(p().sysLstFileLists[var]==null)
		{
			Vector<String> V=CMParms.parseSemicolons(getRawListFileEntry(SYSTEMLF_KEYS[var]),true);
			Object[][] subSet=new Object[V.size()][];
			for(int v=0;v<V.size();v++)
				subSet[v]=CMParms.toStringArray(CMParms.parseCommas(V.elementAt(v),false));
			Object[][][] finalSet=new Object[subSet.length][][];
			for(int s=0;s<subSet.length;s++)
			{
				finalSet[s]=new Object[subSet[s].length][];
				for(int s1=0;s1<subSet[s].length;s1++)
					if((subSet[s][s1]==null)||(((String)subSet[s][s1]).length()==0))
						finalSet[s][s1]=new Object[]{""};
					else
						finalSet[s][s1]=CMParms.toStringArray(CMParms.parseAny((String)subSet[s][s1], '|', false));
			}
			p().sysLstFileLists[var]=finalSet;
		}
		return (Object[][][])p().sysLstFileLists[var];
	}

	public static final String getListFileValue(final int varCode, final int listIndex)
	{
		final Object[] set = getSLstFileVar(varCode)[listIndex];
		if(set.length==1) return (String)set[0];
		return (String)CMLib.dice().pick(set);
	}
	
	public static final String getListFileValueByHash(final int varCode, final int hash)
	{
		final Object[][] allVars = getSLstFileVar(varCode);
		final Object[] set = allVars[hash % allVars.length];
		if(set.length==1) return (String)set[0];
		return (String)CMLib.dice().pick(set);
	}
	
	public static final int getListFileSize(final int varCode)
	{
		return getSLstFileVar(varCode).length;
	}
	
	public static final String getAnyListFileValue(final int varCode)
	{
		return (String)CMLib.dice().doublePick(getSLstFileVar(varCode));
	}
	
	public final void resetSystemVars()
	{
		if(CMLib.lang()!=null)
			CMLib.lang().setLocale(getStr("LANGUAGE"),getStr("COUNTRY"));
		
		TIME_TICK=getLong("TICKTIME");
		if(TIME_TICK<500) TIME_TICK=4000;
		TIME_TICK_DOUBLE=TIME_TICK;
		TICKS_PER_RLMIN=(int)Math.round(60000.0/TIME_TICK_DOUBLE);
		TICKS_PER_RLHOUR=TICKS_PER_RLMIN * 60;
		TICKS_PER_RLDAY=TICKS_PER_RLHOUR * 24;
		MILLIS_PER_MUDHOUR=getLong("MILLISPERMUDHOUR");
		if(MILLIS_PER_MUDHOUR < TIME_TICK)
			MILLIS_PER_MUDHOUR = 600000;
		
		setVar(SYSTEM_PRIVATERESOURCES,getStr("PRIVATERESOURCES"));
		setVar(SYSTEM_BADNAMES,getStr("BADNAMES"));
		setVar(SYSTEM_MULTICLASS,getStr("CLASSSYSTEM"));
		setVar(SYSTEM_PKILL,getStr("PLAYERKILL"));
		setVar(SYSTEM_PLAYERDEATH,getStr("PLAYERDEATH"));
		setVar(SYSTEM_ITEMLOOTPOLICY,getStr("ITEMLOOTPOLICY"));
		setVar(SYSTEM_MOBDEATH,getStr("MOBDEATH"));
		setVar(SYSTEM_PLAYERFLEE,getStr("FLEE"));
		setVar(SYSTEM_SHOWDAMAGE,getStr("SHOWDAMAGE"));
		setVar(SYSTEM_EMAILREQ,getStr("EMAILREQ"));
		setVar(SYSTEM_ESC0,getStr("ESCAPE0"));
		setVar(SYSTEM_ESC1,getStr("ESCAPE1"));
		setVar(SYSTEM_ESC2,getStr("ESCAPE2"));
		setVar(SYSTEM_ESC3,getStr("ESCAPE3"));
		setVar(SYSTEM_ESC4,getStr("ESCAPE4"));
		setVar(SYSTEM_ESC5,getStr("ESCAPE5"));
		setVar(SYSTEM_ESC6,getStr("ESCAPE6"));
		setVar(SYSTEM_ESC7,getStr("ESCAPE7"));
		setVar(SYSTEM_ESC8,getStr("ESCAPE8"));
		setVar(SYSTEM_ESC9,getStr("ESCAPE9"));
		setVar(SYSTEM_MSPPATH,getStr("SOUNDPATH"),false);
		setVar(SYSTEM_AUTOPURGE,getStr("AUTOPURGE"));
		setIntVar(SYSTEMI_ACCOUNTPURGEDAYS,getStr("ACCOUNTPURGE"),14);
		setVar(SYSTEM_IDLETIMERS,getStr("IDLETIMERS"));
		setVar(SYSTEM_CORPSEGUARD,getStr("CORPSEGUARD"));
		setUpLowVar(SYSTEM_MUDDOMAIN,getStr("DOMAIN"));
		String adminEmail = getStr("ADMINEMAIL");
		if((adminEmail==null)||(adminEmail.trim().length()==0))
			adminEmail = getStr("I3EMAIL");
		setVar(SYSTEM_ADMINEMAIL,adminEmail);
		setUpLowVar(SYSTEM_I3ROUTERS,getStr("I3ROUTERS"));
		setVar(SYSTEM_AUTOREACTION,getStr("AUTOREACTION"));
		setVar(SYSTEM_WIZLISTMASK,getStr("WIZLISTMASK"));
		setUpLowVar(SYSTEM_STARTINGITEMS,getStr("STARTINGITEMS","1 Waterskin, 1 Ration, 1 Torch"));
		setVar(SYSTEM_PREJUDICE,getStr("PREJUDICE"));
		setUpLowVar(SYSTEM_PRICEFACTORS,getStr("PRICEFACTORS"));
		setVar(SYSTEM_IGNOREMASK,getStr("IGNOREMASK"));
		setVar(SYSTEM_BUDGET,getStr("BUDGET"));
		setVar(SYSTEM_DEVALUERATE,getStr("DEVALUERATE"));
		setVar(SYSTEM_INVRESETRATE,getStr("INVRESETRATE"));
		setVar(SYSTEM_AUCTIONRATES,getStr("AUCTIONRATES","0,10,0.1%,10%,5%,1,168"));
		setUpLowVar(SYSTEM_DEFAULTPROMPT,getStr("DEFAULTPROMPT"));
		for(int i=0;i<NUMLF_SYSTEM;i++)
			sysLstFileLists[i]=null;
		setVar(SYSTEM_EMOTEFILTER,getStr("EMOTEFILTER"));
		p().emoteFilter.clear();
		p().emoteFilter.addAll(CMParms.parse((getStr("EMOTEFILTER")).toUpperCase()));
		setVar(SYSTEM_POSEFILTER,getStr("POSEFILTER"));
		setVar(SYSTEM_STATCOSTS,getStr("STATCOSTS","<18 1, <22 2, <25 3, <99 5"));
		p().poseFilter.clear();
		p().poseFilter.addAll(CMParms.parse((getStr("POSEFILTER")).toUpperCase()));
		setVar(SYSTEM_SAYFILTER,getStr("SAYFILTER"));
		p().sayFilter.clear();
		p().sayFilter.addAll(CMParms.parse((getStr("SAYFILTER")).toUpperCase()));
		setVar(SYSTEM_CHANNELFILTER,getStr("CHANNELFILTER"));
		p().channelFilter.clear();
		p().channelFilter.addAll(CMParms.parse((getStr("CHANNELFILTER")).toUpperCase()));
		setVar(SYSTEM_CLANTROPAREA,getStr("CLANTROPAREA"));
		setVar(SYSTEM_CLANTROPCP,getStr("CLANTROPCP"));
		setVar(SYSTEM_CLANTROPEXP,getStr("CLANTROPEXP"));
		setVar(SYSTEM_CLANTROPPK,getStr("CLANTROPPK"));
		setVar(SYSTEM_CLANTROPMB,getStr("CLANTROPMB"));
		setVar(SYSTEM_CLANTROPLVL,getStr("CLANTROPLVL"));
		setVar(SYSTEM_COLORSCHEME,getStr("COLORSCHEME"));
		setUpLowVar(SYSTEM_SMTPSERVERNAME,getStr("SMTPSERVERNAME"));
		setVar(SYSTEM_EXPCONTACTLINE,getStr("EXPCONTACTLINE"));
		setVar(SYSTEM_AUTOWEATHERPARMS,getStr("AUTOWEATHERPARMS"));
		setVar(SYSTEM_DEFAULTPLAYERFLAGS,getStr("DEFAULTPLAYERFLAGS"));
		setUpLowVar(SYSTEM_AUTOAREAPROPS,getStr("AUTOAREAPROPS"));
		setUpLowVar(SYSTEM_MXPIMAGEPATH,getStr("MXPIMAGEPATH"));
		setBoolVar(SYSTEMB_ACCOUNTEXPIRATION,getStr("ACCOUNTEXPIRATION").equalsIgnoreCase("YES")?true:false);
		setBoolVar(SYSTEMB_INTRODUCTIONSYSTEM,getStr("INTRODUCTIONSYSTEM").equalsIgnoreCase("YES")?true:false);
		setBoolVar(SYSTEMB_HASHPASSWORDS,getStr("HASHPASSWORDS").equalsIgnoreCase("YES")?true:false);
		setUpLowVar(SYSTEM_PREFACTIONS,getStr("FACTIONS"));
		setUpLowVar(SYSTEM_CHARCREATIONSCRIPTS,getStr("CHARCREATIONSCRIPTS"));
		setUpLowVar(SYSTEM_CHARSETINPUT,getStr("CHARSETINPUT","iso-8859-1"));
		setUpLowVar(SYSTEM_CHARSETOUTPUT,getStr("CHARSETOUTPUT","iso-8859-1"));
		setUpCosts("COMMONCOST",commonCost,CMParms.parseCommas(getStr("COMMONCOST","1 TRAIN"),true));
		setUpCosts("SKILLCOST",skillsCost,CMParms.parseCommas(getStr("SKILLCOST","1 TRAIN"),true));
		setUpCosts("LANGCOST",languageCost,CMParms.parseCommas(getStr("LANGCOST","3 PRACTICE"),true));

		setWhitelist(CMProps.SYSTEMWL_CONNS,getStr("WHITELISTIPSCONN"));
		setWhitelist(CMProps.SYSTEMWL_LOGINS,getStr("WHITELISTLOGINS"));
		setWhitelist(CMProps.SYSTEMWL_NEWPLAYERS,getStr("WHITELISTIPSNEWPLAYERS"));
		
		if(CMLib.color()!=null) CMLib.color().clearLookups();
		if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("LEVEL"))
			setIntVar(SYSTEMI_MANACONSUMEAMT,-100);
		else
		if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("SPELLLEVEL"))
			setIntVar(SYSTEMI_MANACONSUMEAMT,-200);
		else
			setIntVar(SYSTEMI_MANACONSUMEAMT,CMath.s_int(getStr("MANACONSUMEAMT").trim()));
		String s=getStr("COMBATSYSTEM");
		if("queue".equalsIgnoreCase(s))
			setIntVar(SYSTEMI_COMBATSYSTEM,CombatLibrary.COMBAT_QUEUE);
		else
		if("manual".equalsIgnoreCase(s))
			setIntVar(SYSTEMI_COMBATSYSTEM,CombatLibrary.COMBAT_MANUAL);
		else
			setIntVar(SYSTEMI_COMBATSYSTEM,CombatLibrary.COMBAT_DEFAULT);
		s=getStr("EQVIEW");
		if("paragraph".equalsIgnoreCase(s))
			setIntVar(SYSTEMI_EQVIEW,2);
		else
		if("mixed".equalsIgnoreCase(s))
			setIntVar(SYSTEMI_EQVIEW,1);
		else
			setIntVar(SYSTEMI_EQVIEW,0);
		s=getStr("EXVIEW");
		if("brief".equalsIgnoreCase(s))
			setIntVar(SYSTEMI_EXVIEW,3);
		else
		if("paragraph".equalsIgnoreCase(s))
			setIntVar(SYSTEMI_EXVIEW,1);
		else
		if("mixed".equalsIgnoreCase(s))
			setIntVar(SYSTEMI_EXVIEW,2);
		else
			setIntVar(SYSTEMI_EXVIEW,0);

		s=getStr("EXPIRATIONS");
		Vector<String> V=CMParms.parseCommas(s,false);
		for(int i=0;i<5;i++)
		{
			if(V.size()>0)
			{
				setIntVar(SYSTEMI_EXPIRE_MONSTER_EQ + i,V.elementAt(0));
				V.removeElementAt(0);
			}
			else
			switch(SYSTEMI_EXPIRE_MONSTER_EQ + i)
			{
			case SYSTEMI_EXPIRE_MONSTER_EQ: setIntVar(SYSTEMI_EXPIRE_MONSTER_EQ,"30"); break;
			case SYSTEMI_EXPIRE_PLAYER_DROP: setIntVar(SYSTEMI_EXPIRE_PLAYER_DROP,"1200"); break;
			case SYSTEMI_EXPIRE_RESOURCE: setIntVar(SYSTEMI_EXPIRE_RESOURCE,"60"); break;
			case SYSTEMI_EXPIRE_MONSTER_BODY: setIntVar(SYSTEMI_EXPIRE_MONSTER_BODY,"30"); break;
			case SYSTEMI_EXPIRE_PLAYER_BODY: setIntVar(SYSTEMI_EXPIRE_PLAYER_BODY,"1330"); break;
			}
		}

		setIntVar(SYSTEMI_MANACONSUMETIME,getStr("MANACONSUMETIME"));
		setIntVar(SYSTEMI_PAGEBREAK,getStr("PAGEBREAK"));
		setIntVar(SYSTEMI_CLANENCHCOST,getStr("CLANENCHCOST"));
		setIntVar(SYSTEMI_FOLLOWLEVELDIFF,getStr("FOLLOWLEVELDIFF"));
		setIntVar(SYSTEMI_EXPRATE,getStr("EXPRATE"));
		setIntVar(SYSTEMI_SKYSIZE,getStr("SKYSIZE"));
		setIntVar(SYSTEMI_MAXSTAT,getStr("MAXSTATS"));
		setIntVar(SYSTEMI_BASEMAXSTAT,getStr("BASEMAXSTAT","18"));
		setIntVar(SYSTEMI_BASEMINSTAT,getStr("BASEMINSTAT","3"));
		setIntVar(SYSTEMI_STARTSTAT,getStr("STARTSTAT"));
		setIntVar(SYSTEMI_DEFCMDTIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFCMDTIME"),p().cmdActionCostExceptions)*100.0));
		setIntVar(SYSTEMI_DEFCOMCMDTIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFCOMCMDTIME"),p().cmdComActionCostExceptions)*100.0));
		setIntVar(SYSTEMI_DEFABLETIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFABLETIME"),p().skillActionCostExceptions)*100.0));
		setIntVar(SYSTEMI_DEFCOMABLETIME,(int)Math.round(CMProps.setExceptionCosts(getStr("DEFCOMABLETIME"),p().skillComActionCostExceptions)*100.0));
		setIntVar(SYSTEMI_MANACOST,(int)CMProps.setExceptionCosts(getStr("MANACOST"),p().skillMaxManaExceptions));
		setIntVar(SYSTEMI_MANAMINCOST,(int)CMProps.setExceptionCosts(getStr("MANAMINCOST"),p().skillMinManaExceptions));
		setIntVar(SYSTEMI_EDITORTYPE,0);
		if(getStr("EDITORTYPE").equalsIgnoreCase("WIZARD")) setIntVar(SYSTEMI_EDITORTYPE,1);
		setIntVar(SYSTEMI_MINCLANMEMBERS,getStr("MINCLANMEMBERS"));
		setIntVar(SYSTEMI_MAXCLANMEMBERS,getStr("MAXCLANMEMBERS"));
		setIntVar(SYSTEMI_CLANCOST,getStr("CLANCOST"));
		setIntVar(SYSTEMI_DAYSCLANDEATH,getStr("DAYSCLANDEATH"));
		setIntVar(SYSTEMI_MINCLANLEVEL,getStr("MINCLANLEVEL"));
		setIntVar(SYSTEMI_LASTPLAYERLEVEL,getStr("LASTPLAYERLEVEL"));
		setIntVar(SYSTEMI_JOURNALLIMIT,getStr("JOURNALLIMIT"));
		setIntVar(SYSTEMI_MUDTHEME,getStr("MUDTHEME"));
		setIntVar(SYSTEMI_TRIALDAYS,getStr("TRIALDAYS"));
		setIntVar(SYSTEMI_MAXCONNSPERIP,getStr("MAXCONNSPERIP"));
		setIntVar(SYSTEMI_MAXCONNSPERACCOUNT,getStr("MAXCONNSPERACCOUNT"));
		setIntVar(SYSTEMI_MAXNEWPERIP,getStr("MAXNEWPERIP"));
		setIntVar(SYSTEMI_JSCRIPTS,getStr("JSCRIPTS"));
		setIntVar(SYSTEMI_RECOVERRATE,getStr("RECOVERRATE"),1);
		setIntVar(SYSTEMI_COMMONACCOUNTSYSTEM,getStr("COMMONACCOUNTSYSTEM"),1);
		setIntVar(SYSTEMI_OBJSPERTHREAD,getStr("OBJSPERTHREAD"));
		setIntVar(SYSTEMI_MAXCOMMONSKILLS,getStr("MAXCOMMONSKILLS"),0);
		setIntVar(SYSTEMI_MAXCRAFTINGSKILLS,getStr("MAXCRAFTINGSKILLS"),2);
		setIntVar(SYSTEMI_MAXNONCRAFTINGSKILLS,getStr("MAXNONCRAFTINGSKILLS"),5);
		setIntVar(SYSTEMI_MAXLANGUAGES,getStr("MAXLANGUAGES"),3);
		setIntVar(SYSTEMI_WALKCOST,getStr("WALKCOST"),1);
		setIntVar(SYSTEMI_RUNCOST,getStr("RUNCOST"),2);
		setIntVar(SYSTEMI_AWARERANGE,getStr("AWARERANGE"),0);
		setIntVar(SYSTEMI_MINSESSIONTHREADS,getStr("MINSESSIONTHREADS"),5);
		setIntVar(SYSTEMI_MAXSESSIONTHREADS,getStr("MAXSESSIONTHREADS"),100);
		setIntVar(SYSTEMI_DUELTICKDOWN,getStr("DUELTICKDOWN"),5);
		V=CMParms.parseCommas(getStr("MAXCLANCATS"), true);
		p().maxClanCatsMap.clear();
		for(String cat : V)
			if(CMath.isInteger(cat.trim()))
				p().maxClanCatsMap.put("", Integer.valueOf(CMath.s_int(cat.trim())));
			else
			{
				int x=cat.lastIndexOf(' ');
				if((x>0)&&CMath.isInteger(cat.substring(x+1).trim()))
					p().maxClanCatsMap.put(cat.substring(0,x).trim().toUpperCase(), Integer.valueOf(CMath.s_int(cat.substring(x+1).trim())));
			}
		if(!p().maxClanCatsMap.containsKey(""))
			p().maxClanCatsMap.put("", Integer.valueOf(1));

		p().publicClanCats.clear();
		V=CMParms.parseCommas(getStr("PUBLICCLANCATS"), true);
		for(String cat : V)
			p().publicClanCats.add(cat.trim().toUpperCase());
		p().publicClanCats.add("");

		V=CMParms.parseCommas(getStr("INJURYSYSTEM"),true);

		if(V.size()>0) setIntVar(SYSTEMI_INJPCTCHANCE,CMath.s_int(V.elementAt(0)));
		else setIntVar(SYSTEMI_INJPCTCHANCE,100);
		if(V.size()>1) setIntVar(SYSTEMI_INJPCTHP,CMath.s_int(V.elementAt(1)));
		else setIntVar(SYSTEMI_INJPCTHP,40);
		if(V.size()>2) setIntVar(SYSTEMI_INJPCTHPAMP,CMath.s_int(V.elementAt(2)));
		else setIntVar(SYSTEMI_INJPCTHPAMP,10);
		if(V.size()>3) setIntVar(SYSTEMI_INJPCTCHANCEAMP,CMath.s_int(V.elementAt(3)));
		else setIntVar(SYSTEMI_INJPCTCHANCEAMP,100);
		if(V.size()>4) setIntVar(SYSTEMI_INJMULTIPLIER,CMath.s_int(V.elementAt(4)));
		else setIntVar(SYSTEMI_INJMULTIPLIER,4);
		if(V.size()>5) setIntVar(SYSTEMI_INJMINLEVEL,CMath.s_int(V.elementAt(5)));
		else setIntVar(SYSTEMI_INJMINLEVEL,10);
		if(V.size()>6) setIntVar(SYSTEMI_INJBLEEDMINLEVEL,CMath.s_int(V.elementAt(6)));
		else setIntVar(SYSTEMI_INJBLEEDMINLEVEL,15);
		if(V.size()>7) setIntVar(SYSTEMI_INJBLEEDPCTHP,CMath.s_int(V.elementAt(7)));
		else setIntVar(SYSTEMI_INJBLEEDPCTHP,20);
		if(V.size()>8) setIntVar(SYSTEMI_INJBLEEDPCTCHANCE,CMath.s_int(V.elementAt(8)));
		else setIntVar(SYSTEMI_INJBLEEDPCTCHANCE,100);

		String stateVar=getStr("STARTHP");
		if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
			setIntVar(SYSTEMI_STARTHP,CMath.s_int(stateVar));
		stateVar=getStr("STARTMANA");
		if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
			setIntVar(SYSTEMI_STARTMANA,CMath.s_int(stateVar));
		stateVar=getStr("STARTMOVE");
		if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
			setIntVar(SYSTEMI_STARTMOVE,CMath.s_int(stateVar));

		setIntVar(SYSTEMI_MAXITEMSHOWN,getStr("MAXITEMSHOWN"));
		setIntVar(SYSTEMI_MUDSTATE,getStr("MUDSTATE"));
		
		setUpLowVar(SYSTEM_FORMULA_ATTACKADJUSTMENT, getStr("FORMULA_ATTACKADJUSTMENT","(50+@x1+(((@x2-9)/5)*((@x3-9)/5)*((@x3-9)/5))+@x4)-(0.15*@xx*@x5)-(0.15*@xx*@x6)-(0.3*@xx*@x7)"));
		setUpLowVar(SYSTEM_FORMULA_ARMORADJUSTMENT, getStr("FORMULA_ARMORADJUSTMENT","(@x1-( (((@x2-9)/5)*((@x3-9)/5)*((@x3-9)/5*@x8)) +(@x4*@x8)-(0.15*@xx>0*@x5)-(0.15*@xx>0*@x6)-(0.3*@xx>0*@x7)*@x9))-100"));
		setUpLowVar(SYSTEM_FORMULA_ATTACKFUDGEBONUS, getStr("FORMULA_ATTACKFUDGEBONUS","@x3 * (@x1 - @x2)* (@x1 - @x2)"));
		setUpLowVar(SYSTEM_FORMULA_PVPATTACKFUDGEBONUS, getStr("FORMULA_PVPATTACKFUDGEBONUS",getVar(SYSTEM_FORMULA_ATTACKFUDGEBONUS)));
		setUpLowVar(SYSTEM_FORMULA_CHANCESPELLCRIT, getStr("FORMULA_CHANCESPELLCRIT","(( ((@x2-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5) ))"));
		setUpLowVar(SYSTEM_FORMULA_PVPCHANCESPELLCRIT, getStr("FORMULA_PVPCHANCESPELLCRIT",getVar(SYSTEM_FORMULA_CHANCESPELLCRIT)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGESPELLCRIT, getStr("FORMULA_DAMAGESPELLCRIT","(@x1*( ((@x2-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5)>0 * ((@x3-10+((@x8-@x9)<10))/5) )/100.0)+(@x4/2)"));
		setUpLowVar(SYSTEM_FORMULA_PVPDAMAGESPELLCRIT, getStr("FORMULA_PVPDAMAGESPELLCRIT",getVar(SYSTEM_FORMULA_DAMAGESPELLCRIT)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGESPELLFUDGE, getStr("FORMULA_DAMAGESPELLFUDGE","(@x1 - ( ((@x9-@x8)>0<1.0) * @x1 * (((@x9-@x8)<12.0)*((@x9-@x8)<12.0))/120.0))>0"));
		setUpLowVar(SYSTEM_FORMULA_PVPDAMAGESPELLFUDGE, getStr("FORMULA_PVPDAMAGESPELLFUDGE",getVar(SYSTEM_FORMULA_DAMAGESPELLFUDGE)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGEMELEEFUDGE, getStr("FORMULA_DAMAGEMELEEFUDGE","(@x1 - ( ((@x9-@x8)>0<1.0) * @x1 * (((@x9-@x8)<12.0)*((@x9-@x8)<12.0))/120.0))>0"));
		setUpLowVar(SYSTEM_FORMULA_PVPDAMAGEMELEEFUDGE, getStr("FORMULA_PVPDAMAGEMELEEFUDGE",getVar(SYSTEM_FORMULA_DAMAGEMELEEFUDGE)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGERANGEDFUDGE, getStr("FORMULA_DAMAGERANGEDFUDGE","(@x1 - ( ((@x9-@x8)>0<1.0) * @x1 * (((@x9-@x8)<12.0)*((@x9-@x8)<12.0))/120.0))>0"));
		setUpLowVar(SYSTEM_FORMULA_PVPDAMAGERANGEDFUDGE, getStr("FORMULA_PVPDAMAGERANGEDFUDGE",getVar(SYSTEM_FORMULA_DAMAGERANGEDFUDGE)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGERANGEDTARGETED, getStr("FORMULA_DAMAGERANGEDTARGETED","((1?@x1)+((@x3-@x4)/2.5)-(0.5*@xx*@x8)+(0.5*@xx*@x9)+(0.2*@xx*@x10)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(SYSTEM_FORMULA_PVPDAMAGERANGEDTARGETED, getStr("FORMULA_PVPDAMAGERANGEDTARGETED",getVar(SYSTEM_FORMULA_DAMAGERANGEDTARGETED)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGERANGEDSTATIC, getStr("FORMULA_DAMAGERANGEDSTATIC","((1?@x1)+((@x3-@x4)/2.5)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(SYSTEM_FORMULA_DAMAGEMELEETARGETED, getStr("FORMULA_DAMAGEMELEETARGETED","((1?@x1)+((@x2-10+@x3-@x4)/5)-(0.5*@xx*@x8)+(0.5*@xx*@x9)+(0.2*@xx*@x10)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(SYSTEM_FORMULA_PVPDAMAGEMELEETARGETED, getStr("FORMULA_PVPDAMAGEMELEETARGETED",getVar(SYSTEM_FORMULA_DAMAGEMELEETARGETED)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGEMELEESTATIC, getStr("FORMULA_DAMAGEMELEESTATIC","((1?@x1)+((@x2-10+@x3-@x4)/5)-(0.2*@xx*@x5)-(0.2*@xx*@x6)-(0.2*@xx*@x7))>1"));
		setUpLowVar(SYSTEM_FORMULA_CHANCEWEAPONCRIT, getStr("FORMULA_CHANCEWEAPONCRIT","((((@x2-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5)))"));
		setUpLowVar(SYSTEM_FORMULA_PVPCHANCEWEAPONCRIT, getStr("FORMULA_PVPCHANCEWEAPONCRIT",getVar(SYSTEM_FORMULA_CHANCEWEAPONCRIT)));
		setUpLowVar(SYSTEM_FORMULA_DAMAGEWEAPONCRIT, getStr("FORMULA_DAMAGEWEAPONCRIT","(@x1 * (((@x2-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5)>0 * ((@x3-10+((@x8-@x9)<10))/2.5))/50.0)+(@x4/2)"));
		setUpLowVar(SYSTEM_FORMULA_PVPDAMAGEWEAPONCRIT, getStr("FORMULA_PVPDAMAGEWEAPONCRIT",getVar(SYSTEM_FORMULA_DAMAGEWEAPONCRIT)));
		setUpLowVar(SYSTEM_FORMULA_NPCHITPOINTS, getStr("FORMULA_NPCHITPOINTS","3 + @x1 + (@x1 * @x2)"));

		Directions.instance().reInitialize(getInt("DIRECTIONS"));
		
		resetSecurityVars();
		statCodeExtensions = getStrsStarting("EXTVAR_");

		// initialized elsewhere
		if(getVar(CMProps.SYSTEM_MAILBOX)==null)
		{
			setVar(SYSTEM_MAILBOX, "");
			setIntVar(SYSTEMI_MAXMAILBOX,0);
			setBoolVar(SYSTEMB_EMAILFORWARDING,false);
		}

		CMLib.propertiesLoaded();
	}
	
	public final void resetSecurityVars() 
	{
		String disable=getStr("DISABLE");
		if(getVar(SYSTEM_MULTICLASS).equalsIgnoreCase("DISABLED"))
			disable+=", CLASSES";
		CMSecurity.setDisableVars(disable);
		CMSecurity.setDebugVars(getStr("DEBUG"));
		CMSecurity.setSaveFlags(getStr("SAVE"));
	}

	public static String applyINIFilter(String msg, int whichFilter)
	{
		List<String> filter=null;
		switch(whichFilter)
		{
		case SYSTEM_EMOTEFILTER: filter=p().emoteFilter; break;
		case SYSTEM_POSEFILTER: filter=p().poseFilter; break;
		case SYSTEM_SAYFILTER: filter=p().sayFilter; break;
		case SYSTEM_CHANNELFILTER: filter=p().channelFilter; break;
		}
		if((filter==null)||(filter.size()==0))
			return msg;

		int fdex=0;
		int len=0;
		StringBuffer newMsg=null;
		String upp=msg.toUpperCase();
		final char[] filterPattern={'%','#','@','*','!','$','&','?'};
		int fpIndex=0;
		for(final String filterStr : filter)
		{
			if(filterStr.length()==0) continue;
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
						if(!Character.isWhitespace(msg.charAt(fdex)))
						{
							if(newMsg==null) newMsg=new StringBuffer(msg);
							newMsg.setCharAt(fdex,filterPattern[fpIndex]);
							if((++fpIndex)>=filterPattern.length) fpIndex=0;
							upp=newMsg.toString().toUpperCase();
						}
						else
							fpIndex=0;
					fdex=upp.indexOf(filterStr);
				}
				else
				if(fdex<(filterStr.length()-1))
					fdex=upp.indexOf(filterStr,fdex+1);
				else
					fdex=-1;
			}
		}
		if(newMsg!=null) return newMsg.toString();
		return msg;
	}


	// this is the sound support method.
	// it builds a valid MSP sound code from built-in web server
	// info, and the info provided.
	public static final String msp(final String soundName, final int volume, final int priority)
	{
		if((soundName==null)||(soundName.length()==0)||CMSecurity.isDisabled(CMSecurity.DisFlag.MSP)) return "";
		if(getVar(SYSTEM_MSPPATH).length()>0)
			return " !!SOUND("+soundName+" V="+volume+" P="+priority+" U="+getVar(SYSTEM_MSPPATH)+") ";
		return " !!SOUND("+soundName+" V="+volume+" P="+priority+") ";
	}

	public static final String[] mxpImagePath(String fileName)
	{
		if((fileName==null)||(fileName.trim().length()==0))
			return new String[]{"",""};
		if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return new String[]{"",""};
		int x=fileName.lastIndexOf('=');
		String preFilename="";
		if(x>=0)
		{
			preFilename=fileName.substring(0,x+1);
			fileName=fileName.substring(x+1);
		}
		x=fileName.lastIndexOf('/');
		if(x>=0)
		{
			preFilename+=fileName.substring(0,x+1);
			fileName=fileName.substring(x+1);
		}
		if(getVar(SYSTEM_MXPIMAGEPATH).endsWith("/"))
			return new String[]{getVar(SYSTEM_MXPIMAGEPATH)+preFilename,fileName};
		return new String[]{getVar(SYSTEM_MXPIMAGEPATH)+"/"+preFilename,fileName};
	}

	public static final String mxpImage(final Environmental E, final String parms)
	{
		if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return "";
		final String image=E.image();
		if(image.length()==0) return "";
		final String[] fixedFilenames=mxpImagePath(image);
		if(fixedFilenames[0].length()==0) return "";
		return "^<IMAGE '"+fixedFilenames[1]+"' URL=\""+fixedFilenames[0]+"\" "+parms+"^>^N";
	}

	public static final String mxpImage(final Environmental E, final String parms, final String pre, final String post)
	{
		if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return "";
		final String image=E.image();
		if(image.length()==0) return "";
		final String[] fixedFilenames=mxpImagePath(image);
		if(fixedFilenames[0].length()==0) return "";
		return pre+"^<IMAGE '"+fixedFilenames[1]+"' URL=\""+fixedFilenames[0]+"\" "+parms+"^>^N"+post;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public static final String getHashedMXPImage(final String key)
	{
		Map<String,String> H=(Map)Resources.getResource("MXP_IMAGES");
		if(H==null) getDefaultMXPImage(null);
		H=(Map)Resources.getResource("MXP_IMAGES");
		if(H==null) return "";
		return getHashedMXPImage(H,key);

	}
	public static final boolean isPublicClanGvtCategory(final String clanCategory)
	{
		if((clanCategory==null)||(clanCategory.trim().length()==0))
			return true;
		String upperClanCategory=clanCategory.toUpperCase().trim();
		return p().publicClanCats.contains(upperClanCategory);
	}
	public static final int getMaxClansThisCategory(final String clanCategory)
	{
		if(clanCategory==null)
			return p().maxClanCatsMap.get("").intValue();
		String upperClanCategory=clanCategory.toUpperCase().trim();
		if(p().maxClanCatsMap.containsKey(upperClanCategory))
			return p().maxClanCatsMap.get(upperClanCategory).intValue();
		return p().maxClanCatsMap.get("").intValue();
	}
	public static final String getHashedMXPImage(final Map<String, String> H, final String key)
	{
		if(H==null) return "";
		final String s=H.get(key);
		if(s==null) return null;
		if(s.trim().length()==0) return null;
		if(s.equalsIgnoreCase("NULL")) return "";
		return s;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public static final String getDefaultMXPImage(final Object O)
	{
		if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return "";
		Map<String,String> H=(Map)Resources.getResource("PARSED: mxp_images.ini");
		if(H==null)
		{
			H=new Hashtable<String,String>();
			List<String> V=Resources.getFileLineVector(new CMFile("resources/mxp_images.ini",null,false).text());
			if((V!=null)&&(V.size()>0))
			{
				String s=null;
				int x=0;
				for(int v=0;v<V.size();v++)
				{
					s=V.get(v).trim();
					if(s.startsWith("//")||s.startsWith(";"))
						continue;
					x=s.indexOf('=');
					if(x<0) continue;
					if(s.substring(x+1).trim().length()>0)
						H.put(s.substring(0,x),s.substring(x+1));
				}
			}
			Resources.submitResource("PARSED: mxp_images.ini",H);
		}
		String image=null;
		if(O instanceof Race)
		{
			image=getHashedMXPImage(H,"RACE_"+((Race)O).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"RACECAT_"+((Race)O).racialCategory().toUpperCase().replace(' ','_'));
			if(image==null) image=getHashedMXPImage(H,"RACE_*");
			if(image==null) image=getHashedMXPImage(H,"RACECAT_*");
		}
		else
		if(O instanceof MOB)
		{
			final String raceName=((MOB)O).charStats().raceName();
			Race R=null;
			for(Enumeration<Race> e=CMClass.races();e.hasMoreElements();)
			{
				R=e.nextElement();
				if(raceName.equalsIgnoreCase(R.name()))
					image=getDefaultMXPImage(R);
			}
			if(image==null)
				image=getDefaultMXPImage(((MOB)O).charStats().getMyRace());
		}
		else
		if(O instanceof Room)
		{
			image=getHashedMXPImage(H,"ROOM_"+((Room)O).ID().toUpperCase());
			if(image==null)
				if(CMath.bset(((Room)O).domainType(),Room.INDOORS))
					image=getHashedMXPImage(H,"LOCALE_INDOOR_"+Room.indoorDomainDescs[((Room)O).domainType()-Room.INDOORS]);
				else
					image=getHashedMXPImage(H,"LOCALE_"+Room.outdoorDomainDescs[((Room)O).domainType()]);
			if(image==null) image=getHashedMXPImage(H,"ROOM_*");
			if(image==null) image=getHashedMXPImage(H,"LOCALE_*");
		}
		else
		if(O instanceof Exit)
		{
			image=getHashedMXPImage(H,"EXIT_"+((Exit)O).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"EXIT_"+((Exit)O).doorName().toUpperCase());
			if(image==null)
				if(((Exit)O).hasADoor())
					image=getHashedMXPImage(H,"EXIT_WITHDOOR");
				else
					image=getHashedMXPImage(H,"EXIT_OPEN");
			if(image==null) image=getHashedMXPImage(H,"EXIT_*");
		}
		else
		if(O instanceof Rideable)
		{
			image=getHashedMXPImage(H,"RIDEABLE_"+Rideable.RIDEABLE_DESCS[((Rideable)O).rideBasis()]);
			if(image==null) image=getHashedMXPImage(H,"RIDEABLE_*");
		}
		else
		if(O instanceof Shield)
		{
			image=getHashedMXPImage(H,"SHIELD_"+RawMaterial.MATERIAL_DESCS[(((Shield)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) image=getHashedMXPImage(H,"SHIELD_*");
		}
		else
		if(O instanceof Coins)
		{
			image=getHashedMXPImage(H,"COINS_"+RawMaterial.CODES.NAME(((Coins)O).material()));
			if(image==null) image=getHashedMXPImage(H,"COINS_*");
		}
		else
		if(O instanceof Ammunition)
		{
			image=getHashedMXPImage(H,"AMMO_"+((Ammunition)O).ammunitionType().toUpperCase().replace(' ','_'));
			if(image==null) image=getHashedMXPImage(H,"AMMO_*");
		}
		else
		if(O instanceof CagedAnimal)
		{
			MOB mob=((CagedAnimal)O).unCageMe();
			return getDefaultMXPImage(mob);
		}
		else
		if(O instanceof ClanItem)
		{
			image=getHashedMXPImage(H,"CLAN_"+((ClanItem)O).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"CLAN_"+ClanItem.CI_DESC[((ClanItem)O).ciType()].toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"CLAN_*");
		}
		else
		if(O instanceof DeadBody)
		{
			Race R=((DeadBody)O).charStats().getMyRace();
			if(R!=null)
			{
				image=getHashedMXPImage(H,"CORPSE_"+R.ID().toUpperCase());
				if(image==null) image=getHashedMXPImage(H,"CORPSECAT_"+R.racialCategory().toUpperCase().replace(' ','_'));
			}
			if(image==null) image=getHashedMXPImage(H,"CORPSE_*");
			if(image==null) image=getHashedMXPImage(H,"CORPSECAT_*");
		}
		else
		if(O instanceof RawMaterial)
		{
			image=getHashedMXPImage(H,"RESOURCE_"+RawMaterial.CODES.NAME(((RawMaterial)O).material()));
			if(image==null)
				image=getHashedMXPImage(H,"RESOURCE_"+RawMaterial.MATERIAL_DESCS[(((RawMaterial)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) 
				image=getHashedMXPImage(H,"RESOURCE_*");
		}
		else
		if(O instanceof DoorKey)
		{
			image=getHashedMXPImage(H,"KEY_"+RawMaterial.CODES.NAME(((DoorKey)O).material()));
			image=getHashedMXPImage(H,"KEY_"+RawMaterial.MATERIAL_DESCS[(((DoorKey)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) image=getHashedMXPImage(H,"KEY_*");
		}
		else
		if(O instanceof LandTitle)
			image=getHashedMXPImage(H,"ITEM_LANDTITLE");
		else
		if(O instanceof MagicDust)
		{
			List<Ability> V=((MagicDust)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"DUST_"+V.get(0).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"DUST_*");
		}
		else
		if(O instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
			image=getHashedMXPImage(H,"ITEM_MAP");
		else
		if(O instanceof MusicalInstrument)
		{
			image=getHashedMXPImage(H,"MUSINSTR_"+MusicalInstrument.TYPE_DESC[((MusicalInstrument)O).instrumentType()]);
			if(image==null) image=getHashedMXPImage(H,"MUSINSTR_*");
		}
		else
		if(O instanceof PackagedItems)
			image=getHashedMXPImage(H,"ITEM_PACKAGED");
		else
		if(O instanceof Perfume)
			image=getHashedMXPImage(H,"ITEM_PERFUME");
		else
		if(O instanceof Pill)
		{
			List<Ability> V=((Pill)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"PILL_"+V.get(0).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"PILL_*");
		}
		else
		if(O instanceof Potion)
		{
			List<Ability> V=((Potion)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"POTION_"+V.get(0).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"POTION_*");
		}
		else
		if(O instanceof Recipe)
			image=getHashedMXPImage(H,"ITEM_RECIPE");
		else
		if(O instanceof Scroll)
		{
			List<Ability> V=((Scroll)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"SCROLL_"+V.get(0).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"SCROLL_*");
		}
		else
		if(O instanceof Electronics.PowerGenerator)
		{
			final String key = "POWERGENERATOR_"+((Electronics)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,"POWERGENERATOR_"+key);
			else
				image=getHashedMXPImage(H,"POWERGENERATOR");
		}
		else
		if(O instanceof Electronics.PowerSource)
		{
			final String key = "POWERSOURCE_"+((Electronics)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,key);
			else
			image=getHashedMXPImage(H,"POWERSOURCE");
		}
		else
		if(O instanceof Electronics.ElecPanel)
		{
			final String key = "ELECPANEL_"+((Electronics)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,key);
			else
			image=getHashedMXPImage(H,"ELECPANEL");
		}
		else
		if(O instanceof ShipComponent)
		{
			final String key = "SHIPCOMP_"+((ShipComponent)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,key);
			else
			if(O instanceof ShipComponent.ShipEngine)
				image=getHashedMXPImage(H,"SHIPCOMP_ENGINE");
			else
			if(O instanceof ShipComponent.ShipEnviroControl)
				image=getHashedMXPImage(H,"SHIPCOMP_ENVIRO");
			else
			if(O instanceof ShipComponent.ShipSensor)
				image=getHashedMXPImage(H,"SHIPCOMP_SENSOR");
			else
			if(O instanceof ShipComponent.ShipWeapon)
				image=getHashedMXPImage(H,"SHIPCOMP_WEAPON");
			if(image==null) image=getHashedMXPImage(H,"SHIPCOMP_*");
		}
		else
		if(O instanceof Software)
			image=getHashedMXPImage(H,"ITEM_SOFTWARE");
		else
		if(O instanceof Armor)
		{
			Armor A=(Armor)O;
			final long[] bits=
			{Wearable.WORN_TORSO, Wearable.WORN_FEET, Wearable.WORN_LEGS, Wearable.WORN_HANDS, Wearable.WORN_ARMS,
			 Wearable.WORN_HEAD, Wearable.WORN_EARS, Wearable.WORN_EYES, Wearable.WORN_MOUTH, Wearable.WORN_NECK,
			 Wearable.WORN_LEFT_FINGER, Wearable.WORN_LEFT_WRIST, Wearable.WORN_BACK, Wearable.WORN_WAIST,
			 Wearable.WORN_ABOUT_BODY, Wearable.WORN_FLOATING_NEARBY, Wearable.WORN_HELD, Wearable.WORN_WIELD};
			final String[] bitdesc=
			{"TORSO","FEET","LEGS","HANDS","ARMS","HEAD","EARS","EYES","MOUTH",
			 "NECK","FINGERS","WRIST","BACK","WAIST","BODY","FLOATER","HELD","WIELDED"};
			for(int i=0;i<bits.length;i++)
				if(CMath.bset(A.rawProperLocationBitmap(),bits[i]))
				{
					image=getHashedMXPImage(H,"ARMOR_"+bitdesc[i]);
					break;
				}
			if(image==null) image=getHashedMXPImage(H,"ARMOR_*");
		}
		else
		if(O instanceof Weapon)
		{
			image=getHashedMXPImage(H,"WEAPON_"+Weapon.CLASS_DESCS[((Weapon)O).weaponClassification()]);
			if(image==null) image=getHashedMXPImage(H,"WEAPON_"+Weapon.TYPE_DESCS[((Weapon)O).weaponType()]);
			if(image==null) image=getHashedMXPImage(H,"WEAPON_"+((Weapon)O).ammunitionType().toUpperCase().replace(' ','_'));
			if(image==null) image=getHashedMXPImage(H,"WEAPON_*");
		}
		else
		if(O instanceof Wand)
		{
			image=getHashedMXPImage(H,"WAND_"+((Wand)O).ID().toUpperCase());
			if(image==null)
			{
				Ability A=((Wand)O).getSpell();
				if(A!=null) image=getHashedMXPImage(H,"WAND_"+A.ID().toUpperCase());
			}
			if(image==null) image=getHashedMXPImage(H,"WAND_*");
		}
		else
		if(O instanceof Food)
		{
			image=getHashedMXPImage(H,"FOOD_"+((Food)O).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"FOOD_"+RawMaterial.CODES.NAME(((Food)O).material()));
			if(image==null) image=getHashedMXPImage(H,"FOOD_"+RawMaterial.MATERIAL_DESCS[(((Food)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) image=getHashedMXPImage(H,"FOOD_*");
		}
		else
		if(O instanceof Drink)
		{
			image=getHashedMXPImage(H,"DRINK_"+((Drink)O).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"DRINK_"+RawMaterial.CODES.NAME(((Item)O).material()));
			if(image==null) image=getHashedMXPImage(H,"DRINK_"+RawMaterial.MATERIAL_DESCS[(((Item)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) image=getHashedMXPImage(H,"DRINK_*");
		}
		else
		if(O instanceof Light)
		{
			image=getHashedMXPImage(H,"LIGHT_"+((Light)O).ID().toUpperCase());
			image=getHashedMXPImage(H,"LIGHT_"+RawMaterial.MATERIAL_DESCS[(((Light)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) image=getHashedMXPImage(H,"LIGHT_*");
		}
		else
		if(O instanceof Container)
		{
			image=getHashedMXPImage(H,"CONTAINER_"+((Container)O).ID().toUpperCase());
			String lid=((Container)O).hasALid()?"LID_":"";
			if(image==null) image=getHashedMXPImage(H,"CONTAINER_"+lid+RawMaterial.MATERIAL_DESCS[(((Container)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) image=getHashedMXPImage(H,"CONTAINER_"+lid+"*");
		}
		else
		if(O instanceof Electronics)
			image=getHashedMXPImage(H,"ITEM_ELECTRONICS");
		else
		if(O instanceof MiscMagic)
			image=getHashedMXPImage(H,"ITEM_MISCMAGIC");
		if((image==null)&&(O instanceof Item))
		{
			image=getHashedMXPImage(H,"ITEM_"+((Item)O).ID().toUpperCase());
			if(image==null) image=getHashedMXPImage(H,"ITEM_"+RawMaterial.CODES.NAME(((Item)O).material()));
			if(image==null) image=getHashedMXPImage(H,"ITEM_"+RawMaterial.MATERIAL_DESCS[(((Item)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
			if(image==null) image=getHashedMXPImage(H,"ITEM_*");
		}
		if(image==null) image=getHashedMXPImage(H,"*");
		if(image==null) return "";
		return image;
	}

	public static final long getTickMillis()
	{
		return p().TIME_TICK;
	}
	
	public static final double getTickMillisD()
	{
		return p().TIME_TICK_DOUBLE;
	}
	
	public static final long getMillisPerMudHour()
	{
		return p().MILLIS_PER_MUDHOUR;
	}
	
	public static final long getTicksPerMinute()
	{
		return p().TICKS_PER_RLMIN;
	}
	
	public static final long getTicksPerHour()
	{
		return p().TICKS_PER_RLHOUR;
	}
	
	public static final long getTicksPerDay()
	{
		return p().TICKS_PER_RLDAY;
	}
	
	public static final String msp(final String soundName, final int priority)
	{ return msp(soundName,50,CMLib.dice().roll(1,50,priority));}

	public static final boolean isTheme(final int i)
	{
		return (getIntVar(SYSTEMI_MUDTHEME)&i)>0;
	}

	public static final List<String> loadEnumerablePage(final String iniFile)
	{
		final StringBuffer str=new CMFile(iniFile,null,true).text();
		if((str==null)||(str.length()==0)) return new Vector<String>();
		final List<String> page=Resources.getFileLineVector(str);
		for(int p=0;p<(page.size()-1);p++)
		{
			String s=page.get(p).trim();
			if(s.startsWith("#")||s.startsWith("!")) continue;
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
	
	public static final String getStatCodeExtensionValue(final String[] codes, final String[] xtraValues, final String code) 
	{
		if(xtraValues!=null)
			for(int x=0;x<xtraValues.length;x++)
				if(codes[codes.length-x-1].equalsIgnoreCase(code))
					return xtraValues[xtraValues.length-x-1]; 
		return "";
	}

	public static void setStatCodeExtensionValue(final String[] codes, final String[] xtraValues, final String code, final String val) 
	{
		if(xtraValues!=null)
			for(int x=0;x<xtraValues.length;x++)
				if(codes[codes.length-x-1].equalsIgnoreCase(code))
					xtraValues[xtraValues.length-x-1]=val; 
	}

	public static final List<String> getStatCodeExtensions(Class<?> C, final String ID)
	{
		final String[][] statCodeExtensions = p().statCodeExtensions;
		if( statCodeExtensions == null) return null;
		final List<String> V=new Vector<String>();
		String myClassName=ID;
		V.add(myClassName.toUpperCase());
		for(;C!=null;C=C.getSuperclass())
		{
			myClassName=C.getName();
			int x=myClassName.lastIndexOf('.');
			if(x>=0)
				V.add(myClassName.substring(x+1).toUpperCase());
			else
				V.add(myClassName.toUpperCase());
		}
		for(final Iterator<String> v=V.iterator();v.hasNext();)
		{
			myClassName = v.next();
			for(int i=0;i<statCodeExtensions.length;i++)
				if(statCodeExtensions[i][0].equals(myClassName))
					return CMParms.parseCommas(statCodeExtensions[i][1],true);
		}
		return null;
	}
	
	public static final List<String> getStatCodeExtentions(final CMObject O)
	{
		String name;
		try {
			name = O.ID();
		}catch (NullPointerException e) {
			name = O.getClass().getSimpleName();
		}
		return getStatCodeExtensions(O.getClass(), name);
	}

	public static final String[] getExtraStatCodesHolder(final CMObject O)
	{
		final List<String> addedStatCodesV = getStatCodeExtentions(O);
		if(addedStatCodesV == null) return null;
		final String[] statHolder= new String[addedStatCodesV.size()];
		for(int s=0;s<statHolder.length;s++)
			statHolder[s]="";
		return statHolder;
	}

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
