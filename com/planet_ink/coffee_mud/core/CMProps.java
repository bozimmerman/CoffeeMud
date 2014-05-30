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
   Copyright 2000-2014 Bo Zimmerman

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
	private static final long serialVersionUID = -6592429720705457521L;
	private static final CMProps[] props	   = new CMProps[256];

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

	public static enum Str {
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
		FORMULA_MOVESRECOVER
	}

	public static enum Int {
		EXPRATE,
		SKYSIZE,
		MAXSTAT,
		EDITORTYPE,
		MINCLANMEMBERS,
		DAYSCLANDEATH,
		MINCLANLEVEL,
		MANACOST,
		//DBPINGINTMINS,
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
	}

	public static enum Bool {
		MOBCOMPRESS,
		ITEMDCOMPRESS,
		ROOMDCOMPRESS,
		MOBDCOMPRESS,
		MUDSTARTED,
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

	public enum StrList
	{
		SUBSCRIPTION_STRS("SUBSCRIPTION_STRS")
		;
		private final String str;
		private StrList(String toStr)
		{
			str=toStr;
		}
		@Override public String toString() { return str; }
	}

	public static enum ListFile {
		DAMAGE_WORDS_THRESHOLDS,
		DAMAGE_WORDS,
		HEALTH_CHART,
		MISS_DESCS,
		WEAPON_MISS_DESCS,
		PROWESS_DESCS_CEILING,
		PROWESS_DESCS,
		ARMOR_DESCS_CEILING,
		ARMOR_DESCS,
		EXP_CHART,
		ARMOR_MISFITS,
		MAGIC_WORDS,
		TOD_CHANGE_OUTSIDE,
		TOD_CHANGE_INSIDE,
		WEATHER_ENDS,
		WEAPON_HIT_DESCS,
		TECH_LEVEL_NAMES,
		TECH_BABBLE_VERBS,
		TECH_BABBLE_ADJ1,
		TECH_BABBLE_ADJ2,
		TECH_BABBLE_NOUN,
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
		public String getKey() { return key; }
	}

	public static final int SYSTEMWL_CONNS=0;
	public static final int SYSTEMWL_LOGINS=1;
	public static final int SYSTEMWL_NEWPLAYERS=2;
	public static final int NUMWL_SYSTEM=3;

	protected final String[]	sysVars=new String[Str.values().length];
	protected final Integer[]   sysInts=new Integer[Int.values().length];
	protected final Boolean[]   sysBools=new Boolean[Bool.values().length];
	protected final String[][]  sysLists=new String[StrList.values().length][];
	protected final Object[]	sysLstFileLists=new Object[ListFile.values().length];
	protected final List<String>sayFilter=new Vector<String>();
	protected final List<String>channelFilter=new Vector<String>();
	protected final List<String>emoteFilter=new Vector<String>();
	protected final List<String>poseFilter=new Vector<String>();
	protected String[][]		statCodeExtensions = null;
	protected Pattern[][]		whiteLists = new Pattern[0][];
	protected int   			pkillLevelDiff=26;
	protected boolean   		loaded=false;
	protected long				lastReset=System.currentTimeMillis();
	protected long  			TIME_TICK=4000;
	protected long  			MILLIS_PER_MUDHOUR=600000;
	protected long  			TICKS_PER_RLMIN=(int)Math.round(60000.0/TIME_TICK);
	protected long  			TICKS_PER_RLHOUR=TICKS_PER_RLMIN * 60;
	protected long  			TICKS_PER_RLDAY=TICKS_PER_RLHOUR * 24;
	protected double			TIME_TICK_DOUBLE=TIME_TICK;
	protected final Map<String,Integer>	maxClanCatsMap				=new HashMap<String,Integer>();
	protected final Set<String>			publicClanCats				=new HashSet<String>();
	protected final Map<String,Double>	skillMaxManaExceptions		=new HashMap<String,Double>();
	protected final Map<String,Double>	skillMinManaExceptions		=new HashMap<String,Double>();
	protected final Map<String,Double>	skillActionCostExceptions	=new HashMap<String,Double>();
	protected final Map<String,Double>	skillComActionCostExceptions=new HashMap<String,Double>();
	protected final Map<String,Double>	cmdActionCostExceptions		=new HashMap<String,Double>();
	protected final Map<String,Double>	cmdComActionCostExceptions	=new HashMap<String,Double>();
	protected final PairVector<String,Long> newusersByIP=new PairVector<String,Long>();
	protected final Map<String,ThreadGroup> privateSet=new HashMap<String,ThreadGroup>();
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
		catch(final IOException e)
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

	public final boolean load(String filename)
	{
		try
		{
			this.load(new ByteArrayInputStream(new CMFile(filename,null).raw()));
			loaded=true;
		}
		catch(final IOException e)
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
			this.load(new ByteArrayInputStream(new CMFile(filename,null).raw()));
			loaded=true;
		}
		catch(final IOException e)
		{
			loaded=false;
		}
	}

	public static final CMProps loadPropPage(final String iniFile)
	{
		final CMProps page=new CMProps(iniFile);
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
		catch(final Exception e)
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
		catch(final Exception t)
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
		catch(final Exception t)
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
		return getActionCost(ID,CMath.div(getIntVar(Int.DEFCMDTIME),100.0));
	}

	public static final double getCombatActionCost(final String ID)
	{
		return getCombatActionCost(ID,CMath.div(getIntVar(Int.DEFCOMCMDTIME),100.0));
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
		return getActionSkillCost(ID,CMath.div(getIntVar(Int.DEFABLETIME),100.0));
	}

	public static final double getCombatActionSkillCost(final String ID)
	{
		return getCombatActionSkillCost(ID,CMath.div(getIntVar(Int.DEFCOMABLETIME),100.0));
	}

	public static final int getPKillLevelDiff(){return p().pkillLevelDiff;}

	public static final String getVar(final Str varNum)
	{
		try { return p().sysVars[varNum.ordinal()];}
		catch(final Exception t) { return ""; }
	}

	public static final int getIntVar(final Int varNum)
	{
		try { return p().sysInts[varNum.ordinal()].intValue(); }
		catch(final Exception t) { return -1; }
	}

	public static final String[] getListVar(final StrList varType)
	{
		try { return p().sysLists[varType.ordinal()]; }
		catch(final Exception t) { return new String[0]; }
	}

	public static final boolean getBoolVar(final Bool varNum)
	{
		try { return p().sysBools[varNum.ordinal()].booleanValue(); }
		catch(final Exception t) { return false; }
	}

	public static final void setBoolVar(final Bool varNum, final boolean val)
	{
		if(varNum==null) return;
		p().sysBools[varNum.ordinal()]=Boolean.valueOf(val);
	}

	public static final void setBoolAllVar(final Bool varNum, final boolean val)
	{
		if(varNum==null) return;
		for(final CMProps p : CMProps.props)
			if(p!=null)
				p.sysBools[varNum.ordinal()]=Boolean.valueOf(val);
	}

	public static final void setIntVar(final Int varNum, final int val)
	{
		if(varNum==null) return ;
		p().sysInts[varNum.ordinal()]=Integer.valueOf(val);
	}

	public static final void setIntVar(final Int varNum, String val)
	{
		if(varNum==null) return ;
		if(val==null) val="0";
		p().sysInts[varNum.ordinal()]=Integer.valueOf(CMath.s_int(val.trim()));
	}

	public static final void setIntVar(final Int varNum, String val, final int defaultValue)
	{
		if(varNum==null) return ;
		if((val==null)||(val.length()==0)) val=""+defaultValue;
		p().sysInts[varNum.ordinal()]=Integer.valueOf(CMath.s_int(val.trim()));
	}

	public static final void setListVar(final StrList varType, String[] var)
	{
		if(varType==null) return ;
		if(var==null) var=new String[0];
		p().sysLists[varType.ordinal()]=var;
	}

	public static final void addListVar(final StrList varType, String var)
	{
		if(varType==null) return ;
		if(var==null) return;
		final CMProps prop=p();
		if(prop.sysLists[varType.ordinal()]==null)
			setListVar(varType, new String[0]);
		final String[] list=prop.sysLists[varType.ordinal()];
		prop.sysLists[varType.ordinal()]=Arrays.copyOf(list, list.length+1);
		list[list.length-1]=var;
	}

	public static final void setVar(final Str varNum, String val, final boolean upperFy)
	{
		if(val==null) val="";
		setUpLowVar(varNum,upperFy?val.toUpperCase():val);
	}

	public static final void setVar(final Str varNum, String val)
	{
		if(val==null) val="";
		setUpLowVar(varNum,val.toUpperCase());
	}

	private static final void setUpLowVar(final CMProps props, final Str varNum, String val)
	{
		if(varNum==null) return ;
		if(val==null) val="";
		props.sysVars[varNum.ordinal()]=val;
		if(varNum==Str.PKILL)
		{
			final int x=val.indexOf('-');
			if(x>0)
				props.pkillLevelDiff=CMath.s_int(val.substring(x+1));
		}
	}

	public static final void setUpLowVar(final Str varNum, final String val)
	{
		setUpLowVar(p(),varNum,val);
	}

	public static final void setUpAllLowVar(final Str varNum, final String val)
	{
		for (final CMProps prop : props)
			if(prop!=null)
			   setUpLowVar(prop,varNum,val);
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
		for(final String part : parts)
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

	public static final void setUpCosts(final String fieldName, final Map<String,ExpertiseLibrary.SkillCostDefinition> map, final List<String> fields)
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
			final ExpertiseLibrary.CostType costType=(ExpertiseLibrary.CostType)CMath.s_valueOf(ExpertiseLibrary.CostType.values(), type);
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
		final PairVector<String,Long> DV=p().newusersByIP;
		synchronized(DV)
		{
			for(int i=DV.size()-1;i>=0;i--)
				if(DV.elementAt(i).first.equalsIgnoreCase(address))
				{
					if(System.currentTimeMillis()>(DV.elementAt(i).second.longValue()))
						DV.removeElementAt(i);
					else
						count++;
				}
		}
		return count;
	}
	public static final void addNewUserByIP(final String address)
	{
		final PairVector<String,Long> DV=p().newusersByIP;
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
		final List<String> V=CMParms.parseCommas(val,true);
		String s=null;
		double endVal=0;
		for(int v=0;v<V.size();v++)
		{
			s=V.get(v);
			if(CMath.isNumber(s)){ endVal=CMath.s_double(s); continue;}
			final int x=s.indexOf(' ');
			if(CMath.isDouble(s.substring(x+1).trim()))
				set.put(s.substring(0,x).trim().toUpperCase(),Double.valueOf(CMath.s_double(s.substring(x+1).trim())));
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
					final String listFileName=CMProps.p().getProperty("LISTFILE");
					final CMFile F=new CMFile(listFileName,null,CMFile.FLAG_LOGERRORS);
					if(F.exists())
					{
						try
						{
							rawListData.load(new InputStreamReader(new ByteArrayInputStream(F.raw()), CMProps.getVar(Str.CHARSETINPUT)));
						}
						catch(final IOException e){}
					}
					Resources.submitResource(rscKey, rawListData);
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

	public static final int getListFileFirstInt(final ListFile var)
	{
		if(var==null) return -1;
		if(p().sysLstFileLists[var.ordinal()]==null)
			p().sysLstFileLists[var.ordinal()]=new int[]{(CMath.s_int(getRawListFileEntry(var.getKey())))};
		return ((int[])p().sysLstFileLists[var.ordinal()])[0];
	}

	public static final String[] getListFileStringList(final ListFile var)
	{
		if(var==null) return new String[0];
		if(p().sysLstFileLists[var.ordinal()]==null)
			p().sysLstFileLists[var.ordinal()]=CMParms.toStringArray(CMParms.parseCommas(getRawListFileEntry(var.getKey()),true));
		return ((String[])p().sysLstFileLists[var.ordinal()]);
	}

	public static final int[] getListFileIntList(final ListFile var)
	{
		if(var==null) return new int[0];
		if(p().sysLstFileLists[var.ordinal()]==null)
		{
			final List<String> V=CMParms.parseCommas(getRawListFileEntry(var.getKey()), true);
			final int[] set=new int[V.size()];
			for(int v=0;v<V.size();v++)
				set[v]=CMath.s_int(V.get(v));
			p().sysLstFileLists[var.ordinal()]=set;
		}
		return ((int[])p().sysLstFileLists[var.ordinal()]);
	}

	private static final Object[][] getSLstFileVar(final ListFile var)
	{
		if(var==null) return new Object[0][];
		if(p().sysLstFileLists[var.ordinal()]==null)
		{
			final String[] baseArray = CMParms.toStringArray(CMParms.parseCommas(getRawListFileEntry(var.getKey()),false));
			final Object[][] finalArray=new Object[baseArray.length][];
			for(int s=0;s<finalArray.length;s++)
				if((baseArray[s]==null)||(baseArray[s].length()==0))
					finalArray[s]=new Object[]{""};
				else
					finalArray[s]=CMParms.toStringArray(CMParms.parseAny(baseArray[s], '|', false));
			p().sysLstFileLists[var.ordinal()]=finalArray;
		}
		return (Object[][])p().sysLstFileLists[var.ordinal()];
	}

	public static final Object[][][] getListFileGrid(final ListFile var)
	{
		if(var==null) return new String[0][0][];
		if(p().sysLstFileLists[var.ordinal()]==null)
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
					if((subSet[s][s1]==null)||(((String)subSet[s][s1]).length()==0))
						finalSet[s][s1]=new Object[]{""};
					else
						finalSet[s][s1]=CMParms.toStringArray(CMParms.parseAny((String)subSet[s][s1], '|', false));
			}
			p().sysLstFileLists[var.ordinal()]=finalSet;
		}
		return (Object[][][])p().sysLstFileLists[var.ordinal()];
	}

	public static final String getListFileValue(final ListFile varCode, final int listIndex)
	{
		final Object[] set = getSLstFileVar(varCode)[listIndex];
		if(set.length==1) return (String)set[0];
		return (String)CMLib.dice().pick(set);
	}

	public static final String getListFileValueByHash(final ListFile varCode, final int hash)
	{
		final Object[][] allVars = getSLstFileVar(varCode);
		final Object[] set = allVars[hash % allVars.length];
		if(set.length==1) return (String)set[0];
		return (String)CMLib.dice().pick(set);
	}

	public static final int getListFileSize(final ListFile varCode)
	{
		return getSLstFileVar(varCode).length;
	}

	public static final String getAnyListFileValue(final ListFile varCode)
	{
		return (String)CMLib.dice().doublePick(getSLstFileVar(varCode));
	}

	public static boolean isPrivateToMe(final String s)
	{
		return p().privateSet.containsKey(s.toUpperCase().trim());
	}

	public static Set<String> getPrivateSubSet(final String mask)
	{
		final Set<String> newSet=new HashSet<String>();
		for(final String s : p().privateSet.keySet())
			if(Pattern.matches(mask, s))
				newSet.add(s);
		return newSet;
	}

	public static ThreadGroup getPrivateOwner(final String s)
	{
		final String tag=s.toUpperCase().trim();
		for(final CMProps p : CMProps.props)
			if((p!=null)&&p.privateSet.containsKey(tag))
				return p.privateSet.get(tag);
		return null;
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
		for(final ListFile lfVar : ListFile.values())
			sysLstFileLists[lfVar.ordinal()]=null;
		setVar(Str.EMOTEFILTER,getStr("EMOTEFILTER"));
		p().emoteFilter.clear();
		p().emoteFilter.addAll(CMParms.parse((getStr("EMOTEFILTER")).toUpperCase()));
		setVar(Str.POSEFILTER,getStr("POSEFILTER"));
		setVar(Str.STATCOSTS,getStr("STATCOSTS","<18 1, <22 2, <25 3, <99 5"));
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

		setWhitelist(CMProps.SYSTEMWL_CONNS,getStr("WHITELISTIPSCONN"));
		setWhitelist(CMProps.SYSTEMWL_LOGINS,getStr("WHITELISTLOGINS"));
		setWhitelist(CMProps.SYSTEMWL_NEWPLAYERS,getStr("WHITELISTIPSNEWPLAYERS"));

		for(final StrList strListVar : StrList.values())
		{
			final String list=getStr(strListVar.toString().toUpperCase().trim());
			if((list!=null)&&(list.trim().length()>0))
				setListVar(strListVar, CMParms.parseCommas(list,false).toArray(new String[0]));
		}

		if(CMLib.color()!=null) CMLib.color().clearLookups();
		if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("LEVEL"))
			setIntVar(Int.MANACONSUMEAMT,-100);
		else
		if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("SPELLLEVEL"))
			setIntVar(Int.MANACONSUMEAMT,-200);
		else
			setIntVar(Int.MANACONSUMEAMT,CMath.s_int(getStr("MANACONSUMEAMT").trim()));
		String s=getStr("COMBATSYSTEM");
		if("queue".equalsIgnoreCase(s))
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.COMBAT_QUEUE);
		else
		if("manual".equalsIgnoreCase(s))
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.COMBAT_MANUAL);
		else
		if("turnbased".equalsIgnoreCase(s))
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.COMBAT_TURNBASED);
		else
			setIntVar(Int.COMBATSYSTEM,CombatLibrary.COMBAT_DEFAULT);
		s=getStr("EQVIEW");
		if("paragraph".equalsIgnoreCase(s))
			setIntVar(Int.EQVIEW,2);
		else
		if("mixed".equalsIgnoreCase(s))
			setIntVar(Int.EQVIEW,1);
		else
			setIntVar(Int.EQVIEW,0);
		s=getStr("EXVIEW");
		if("brief".equalsIgnoreCase(s))
			setIntVar(Int.EXVIEW,3);
		else
		if("paragraph".equalsIgnoreCase(s))
			setIntVar(Int.EXVIEW,1);
		else
		if("mixed".equalsIgnoreCase(s))
			setIntVar(Int.EXVIEW,2);
		else
			setIntVar(Int.EXVIEW,0);

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
		setIntVar(Int.MANACOST,(int)CMProps.setExceptionCosts(getStr("MANACOST"),p().skillMaxManaExceptions));
		setIntVar(Int.MANAMINCOST,(int)CMProps.setExceptionCosts(getStr("MANAMINCOST"),p().skillMinManaExceptions));
		setIntVar(Int.EDITORTYPE,0);
		if(getStr("EDITORTYPE").equalsIgnoreCase("WIZARD")) setIntVar(Int.EDITORTYPE,1);
		setIntVar(Int.MINCLANMEMBERS,getStr("MINCLANMEMBERS"));
		setIntVar(Int.MAXCLANMEMBERS,getStr("MAXCLANMEMBERS"));
		setIntVar(Int.CLANCOST,getStr("CLANCOST"));
		setIntVar(Int.DAYSCLANDEATH,getStr("DAYSCLANDEATH"));
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
		setIntVar(Int.WALKCOST,getStr("WALKCOST"),1);
		setIntVar(Int.RUNCOST,getStr("RUNCOST"),2);
		setIntVar(Int.AWARERANGE,getStr("AWARERANGE"),0);
		setIntVar(Int.MINWORKERTHREADS,getStr("MINWORKERTHREADS"),1);
		setIntVar(Int.MAXWORKERTHREADS,getStr("MAXWORKERTHREADS"),100);
		setIntVar(Int.DUELTICKDOWN,getStr("DUELTICKDOWN"),5);
		V=CMParms.parseCommas(getStr("MAXCLANCATS"), true);
		p().maxClanCatsMap.clear();
		for(final String cat : V)
			if(CMath.isInteger(cat.trim()))
				p().maxClanCatsMap.put("", Integer.valueOf(CMath.s_int(cat.trim())));
			else
			{
				final int x=cat.lastIndexOf(' ');
				if((x>0)&&CMath.isInteger(cat.substring(x+1).trim()))
					p().maxClanCatsMap.put(cat.substring(0,x).trim().toUpperCase(), Integer.valueOf(CMath.s_int(cat.substring(x+1).trim())));
			}
		if(!p().maxClanCatsMap.containsKey(""))
			p().maxClanCatsMap.put("", Integer.valueOf(1));

		p().publicClanCats.clear();
		V=CMParms.parseCommas(getStr("PUBLICCLANCATS"), true);
		for(final String cat : V)
			p().publicClanCats.add(cat.trim().toUpperCase());
		p().publicClanCats.add("");

		V=CMParms.parseCommas(getStr("INJURYSYSTEM"),true);

		if(V.size()>0) setIntVar(Int.INJPCTCHANCE,CMath.s_int(V.get(0)));
		else setIntVar(Int.INJPCTCHANCE,100);
		if(V.size()>1) setIntVar(Int.INJPCTHP,CMath.s_int(V.get(1)));
		else setIntVar(Int.INJPCTHP,40);
		if(V.size()>2) setIntVar(Int.INJPCTHPAMP,CMath.s_int(V.get(2)));
		else setIntVar(Int.INJPCTHPAMP,10);
		if(V.size()>3) setIntVar(Int.INJPCTCHANCEAMP,CMath.s_int(V.get(3)));
		else setIntVar(Int.INJPCTCHANCEAMP,100);
		if(V.size()>4) setIntVar(Int.INJMULTIPLIER,CMath.s_int(V.get(4)));
		else setIntVar(Int.INJMULTIPLIER,4);
		if(V.size()>5) setIntVar(Int.INJMINLEVEL,CMath.s_int(V.get(5)));
		else setIntVar(Int.INJMINLEVEL,10);
		if(V.size()>6) setIntVar(Int.INJBLEEDMINLEVEL,CMath.s_int(V.get(6)));
		else setIntVar(Int.INJBLEEDMINLEVEL,15);
		if(V.size()>7) setIntVar(Int.INJBLEEDPCTHP,CMath.s_int(V.get(7)));
		else setIntVar(Int.INJBLEEDPCTHP,20);
		if(V.size()>8) setIntVar(Int.INJBLEEDPCTCHANCE,CMath.s_int(V.get(8)));
		else setIntVar(Int.INJBLEEDPCTCHANCE,100);

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

		Directions.instance().reInitialize(getInt("DIRECTIONS"));

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

	public static long getLastResetTime()
	{
		return p().lastReset;
	}

	public final void resetSecurityVars()
	{
		String disable=getStr("DISABLE");
		if(getVar(Str.MULTICLASS).equalsIgnoreCase("DISABLED"))
			disable+=", CLASSES";
		CMSecurity.setDisableVars(disable);
		CMSecurity.setDebugVars(getStr("DEBUG"));
		CMSecurity.setSaveFlags(getStr("SAVE"));
	}

	public static String applyINIFilter(String msg, Str whichFilter)
	{
		List<String> filter=null;
		switch(whichFilter)
		{
		case EMOTEFILTER: filter=p().emoteFilter; break;
		case POSEFILTER: filter=p().poseFilter; break;
		case SAYFILTER: filter=p().sayFilter; break;
		case CHANNELFILTER: filter=p().channelFilter; break;
		default:
			return msg;
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

	public static final boolean isPublicClanGvtCategory(final String clanCategory)
	{
		if((clanCategory==null)||(clanCategory.trim().length()==0))
			return true;
		final String upperClanCategory=clanCategory.toUpperCase().trim();
		return p().publicClanCats.contains(upperClanCategory);
	}
	public static final int getMaxClansThisCategory(final String clanCategory)
	{
		if(clanCategory==null)
			return p().maxClanCatsMap.get("").intValue();
		final String upperClanCategory=clanCategory.toUpperCase().trim();
		if(p().maxClanCatsMap.containsKey(upperClanCategory))
			return p().maxClanCatsMap.get(upperClanCategory).intValue();
		return p().maxClanCatsMap.get("").intValue();
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

	public static final long getTicksPerMudHour()
	{
		return p().MILLIS_PER_MUDHOUR / p().TIME_TICK;
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

	public static final boolean isTheme(final int i)
	{
		return (getIntVar(Int.MUDTHEME)&i)>0;
	}

	public static final List<String> loadEnumerablePage(final String iniFile)
	{
		final StringBuffer str=new CMFile(iniFile,null,CMFile.FLAG_LOGERRORS).text();
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
				if(statCodeExtension[0].equals(myClassName))
					return CMParms.parseCommas(statCodeExtension[1],true);
		}
		return null;
	}

	public static final List<String> getStatCodeExtentions(final CMObject O)
	{
		String name;
		try
		{
			name = O.ID();
		}catch (final NullPointerException e)
		{
			name = O.getClass().getSimpleName();
		}
		return getStatCodeExtensions(O.getClass(), name);
	}

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
