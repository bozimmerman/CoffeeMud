package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.ExpertiseAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecGroup;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class DefaultPlayerStats implements PlayerStats
{
	@Override
	public String ID()
	{
		return "DefaultPlayerStats";
	}

	@Override
	public String name()
	{
		return ID();
	}

	protected final static int TELL_STACK_MAX_SIZE=50;
	protected final static int GTELL_STACK_MAX_SIZE=50;

	protected long			 hygiene		= 0;
	protected int			 theme			= Area.THEME_FANTASY;
	protected String[]		 xtraValues		= null;
	protected String		 lastIP			= "";
	protected long  		 lLastDateTime	= System.currentTimeMillis();
	protected long  		 lastUpdated	= 0;
	protected int   		 channelMask;
	protected String		 email			= "";
	protected String		 password		= "";
	protected String		 colorStr		= "";
	protected String		 prompt			= "";
	protected String		 poofin			= "";
	protected String		 poofout		= "";
	protected String		 tranpoofin		= "";
	protected String		 tranpoofout	= "";
	protected String		 announceMsg	= "";
	protected String		 savedPose		= "";
	protected String		 notes			= "";
	protected int   		 wrap			= 78;
	protected int			 bonusCommonSk	= 0;
	protected int			 bonusCraftSk	= 0;
	protected int			 bonusNonCraftSk= 0;
	protected int			 bonusLanguages = 0;
	protected int			 bonusCharStatPt= 0;
	protected int   		 pageBreak		= CMProps.getIntVar(CMProps.Int.PAGEBREAK);
	protected int[] 		 birthday		= null;
	protected MOB   		 replyTo		= null;
	protected int   		 replyType		= 0;
	protected long  		 replyTime		= 0;
	protected Set<String>	 friends		= new SHashSet<String>();
	protected Set<String>	 ignored		= new SHashSet<String>();
	protected List<String>	 tellStack		= new SVector<String>();
	protected List<String>	 gtellStack		= new SVector<String>();
	protected List<String>	 titles			= new SVector<String>();
	protected Set<String>	 autoInvokeSet	= new TreeSet<String>();
	protected PlayerAccount  account		= null;
	protected SecGroup		 securityFlags	= new SecGroup(new CMSecurity.SecFlag[]{});
	protected long			 accountExpires	= 0;
	protected RoomnumberSet  visitedRoomSet	= null;
	protected Set<String>	 introductions	= new SHashSet<String>();
	protected long[]	 	 prideExpireTime= new long[TimeClock.TimePeriod.values().length];
	protected int[][]		 prideStats		= new int[TimeClock.TimePeriod.values().length][AccountStats.PrideStat.values().length];
	protected ItemCollection extItems;

	protected volatile boolean		isSavable		= true;
	protected Map<String,Tracker>	achievementers	= new STreeMap<String,Tracker>();
	protected Map<String,String>	alias			= new STreeMap<String,String>();
	protected Map<String,Integer>	legacy			= new STreeMap<String,Integer>();
	protected Map<String,int[]>		combatSpams		= new STreeMap<String,int[]>();

	protected Map<String, AbilityMapping>		ableMap		= new SHashtable<String, AbilityMapping>();
	protected Map<String, ExpertiseDefinition>	experMap	= new SHashtable<String, ExpertiseDefinition>();
	protected Map<CharClass,Map<String,Object>>	classMap	= new STreeMap<CharClass,Map<String,Object>>();
	
	protected QuadVector<Integer, Long, String, Long>		levelInfo	= new QuadVector<Integer, Long, String, Long>();
	
	public DefaultPlayerStats()
	{
		super();
		xtraValues=CMProps.getExtraStatCodesHolder(this);
		extItems=(ItemCollection)CMClass.getCommon("WeakItemCollection");
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(final Exception e)
		{
			return new DefaultPlayerStats();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultPlayerStats O=(DefaultPlayerStats)this.clone();
			O.levelInfo=new QuadVector<Integer, Long, String, Long>();
			O.levelInfo.addAll(levelInfo);
			if(visitedRoomSet!=null)
				O.visitedRoomSet=(RoomnumberSet)visitedRoomSet.copyOf();
			else
				O.visitedRoomSet=null;
			O.securityFlags=securityFlags.copyOf();
			O.friends=new SHashSet<String>(friends);
			O.ignored=new SHashSet<String>(ignored);
			O.tellStack=new SVector<String>(tellStack);
			O.gtellStack=new SVector<String>(gtellStack);
			O.titles=new SVector<String>(titles);
			O.alias=new SHashtable<String,String>(alias);
			O.legacy=new SHashtable<String,Integer>(legacy);
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			O.extItems=(ItemCollection)extItems.copyOf();
			return O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultPlayerStats();
		}
	}

	@Override
	public String getLastIP()
	{
		return lastIP;
	}

	@Override
	public void setLastIP(String ip)
	{
		lastIP=ip;
		if(account != null)
			account.setLastIP(ip);
	}

	@Override
	public String getEmail()
	{
		if(account != null)
			return account.getEmail();
		if(email==null)
			return "";
		return email;
	}

	@Override
	public void setEmail(String newAdd)
	{
		email=newAdd;
		if(account != null)
			account.setEmail(newAdd);
	}

	@Override
	public int getTheme()
	{
		return theme;
	}

	@Override
	public void setTheme(int theme)
	{
		this.theme=theme;
	}

	@Override
	public long getLastUpdated()
	{
		return lastUpdated;
	}

	@Override
	public void setLastUpdated(long time)
	{
		lastUpdated=time;
		if(account != null)
			account.setLastUpdated(time);
	}

	@Override
	public long getLastDateTime()
	{
		return lLastDateTime;
	}

	@Override
	public void setLastDateTime(long C)
	{
		lLastDateTime=C;
		if(account != null)
			account.setLastDateTime(C);
	}

	@Override
	public String getPasswordStr()
	{
		return (account!=null)?account.getPasswordStr():password;
	}

	@Override
	public void setPassword(String newPassword)
	{
		if(CMProps.getBoolVar(CMProps.Bool.HASHPASSWORDS)
		&&(!CMLib.encoder().isARandomHashString(newPassword)))
			password=CMLib.encoder().makeRandomHashString(newPassword);
		else
			password=newPassword;
		if(account != null)
			account.setPassword(password);
	}

	@Override
	public boolean matchesPassword(String checkPass)
	{
		if(account!=null)
			return account.matchesPassword(checkPass);
		return CMLib.encoder().passwordCheck(checkPass, password);
	}

	@Override
	public int getWrap()
	{
		return wrap;
	}

	@Override
	public void setWrap(int newWrap)
	{
		if(newWrap > CMStrings.SPACES.length())
			newWrap=CMStrings.SPACES.length();
		wrap=newWrap;
	}

	@Override
	public int getPageBreak()
	{
		return pageBreak;
	}

	@Override
	public void setPageBreak(int newBreak)
	{
		pageBreak=newBreak;
	}

	@Override
	public String getNotes()
	{
		return notes;
	}

	@Override
	public void setNotes(String newnotes)
	{
		notes=newnotes;
	}

	@Override
	public void setChannelMask(int newMask)
	{
		channelMask=newMask;
	}

	@Override
	public int getChannelMask()
	{
		return channelMask;
	}

	@Override
	public MOB getReplyToMOB()
	{
		return replyTo;
	}

	@Override
	public int getReplyType()
	{
		return replyType;
	}

	@Override
	public long getReplyToTime()
	{
		return replyTime;
	}

	@Override
	public void setReplyTo(MOB mob, int replyType)
	{
		replyTo=mob;
		this.replyType=replyType;
	}

	@Override
	public void setPrompt(String newPrompt)
	{
		prompt=newPrompt;
	}

	@Override
	public String getColorStr()
	{
		return colorStr;
	}

	@Override
	public void setColorStr(String newColors)
	{
		colorStr=newColors;
	}

	@Override
	public String getAnnounceMessage()
	{
		return announceMsg;
	}

	@Override
	public void setAnnounceMessage(String msg)
	{
		announceMsg=msg;
	}

	@Override
	public String getSavedPose()
	{
		return savedPose;
	}

	@Override
	public void setSavedPose(String msg)
	{
		savedPose=msg;
	}

	@Override
	public String getPrompt()
	{
		if((prompt==null)||(prompt.length()==0))
		{
			prompt=CMProps.getVar(CMProps.Str.DEFAULTPROMPT);
			if((prompt==null)||(prompt.length()==0))
				return "^N%E<^h%hhp ^m%mm ^v%vmv^N>";
		}
		return prompt;
	}

	@Override
	public boolean isIntroducedTo(String name)
	{
		return introductions.contains(name.toUpperCase().trim());
	}

	@Override
	public void introduceTo(String name)
	{
		if((!isIntroducedTo(name))&&(name.trim().length()>0))
			introductions.add(name.toUpperCase().trim());
	}

	public SHashSet<String> getHashFrom(String str)
	{
		final SHashSet<String> h=new SHashSet<String>();
		if((str==null)||(str.length()==0))
			return h;
		str=CMStrings.replaceAll(str,"<FRIENDS>","");
		str=CMStrings.replaceAll(str,"<IGNORED>","");
		str=CMStrings.replaceAll(str,"<INTROS>","");
		str=CMStrings.replaceAll(str,"</INTROS>","");
		str=CMStrings.replaceAll(str,"</FRIENDS>","");
		str=CMStrings.replaceAll(str,"</IGNORED>","");
		int x=str.indexOf(';');
		while(x>=0)
		{
			final String fi=str.substring(0,x).trim();
			if(fi.length()>0)
				h.add(fi);
			str=str.substring(x+1);
			x=str.indexOf(';');
		}
		if(str.trim().length()>0)
			h.add(str.trim());
		return h;
	}

	@Override
	public void addTellStack(String msg)
	{
		if(tellStack.size()>TELL_STACK_MAX_SIZE)
			tellStack.remove(0);
		tellStack.add(msg);
	}

	@Override
	public List<String> getTellStack()
	{
		return new ReadOnlyList<String>(tellStack);
	}

	private RoomnumberSet roomSet()
	{
		if(visitedRoomSet==null)
			visitedRoomSet=((RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet"));
		return visitedRoomSet;
	}

	@Override
	public void addGTellStack(String msg)
	{
		if(gtellStack.size()>GTELL_STACK_MAX_SIZE)
			gtellStack.remove(0);
		gtellStack.add(msg);
	}

	@Override
	public List<String> getGTellStack()
	{
		return new ReadOnlyList<String>(gtellStack);
	}

	@Override
	public Set<String> getFriends()
	{
		if(account != null)
			return account.getFriends();
		return friends;
	}

	@Override
	public Set<String> getIgnored()
	{
		if(account != null)
			return account.getIgnored();
		return ignored;
	}

	@Override
	public Map<String,Object> getClassVariableMap(final CharClass charClass)
	{
		final Map<String,Object> map=this.classMap.get(charClass);
		if(map != null)
			return map;
		final Map<String,Object> newMap = new TreeMap<String,Object>();
		this.classMap.put(charClass, newMap);
		return newMap;
	}

	@Override
	public String[] getAliasNames()
	{
		return alias.keySet().toArray(new String[0]);
	}

	@Override
	public String getAlias(String named)
	{
		if(alias.containsKey(named.toUpperCase().trim()))
			return alias.get(named.toUpperCase().trim());
		return "";
	}

	@Override
	public void addAliasName(String named)
	{
		named=named.toUpperCase().trim();
		if(getAlias(named).length()==0)
			alias.put(named,"");
	}

	@Override
	public void delAliasName(String named)
	{
		alias.remove(named.toUpperCase().trim());
	}

	@Override
	public void setAlias(String named, String value)
	{
		alias.put(named.toUpperCase().trim(),value);
	}

	public String getAliasXML()
	{
		if(alias.size()==0)
			return "";
		final StringBuilder str=new StringBuilder("");
		alias.remove("");
		for(final String key : alias.keySet())
			str.append("<ALIAS CMD=\"").append(key).append("\" VAL=\"").append(CMLib.xml().parseOutAngleBracketsAndQuotes(alias.get(key))).append("\" />");
		return str.toString();
	}

	public String getLegacyXML()
	{
		if(legacy.size()==0)
			return "";
		final StringBuilder str=new StringBuilder("");
		for(final String key : legacy.keySet())
			str.append("<LEGACY CAT=\"").append(key).append("\" LVL=\"").append(legacy.get(key)).append("\" />");
		return str.toString();
	}

	@Override
	public String getActiveTitle()
	{
		if((titles==null)||(titles.size()==0))
			return null;
		final String s=titles.get(0);
		if((s.length()<2)||(s.charAt(0)!='{')||(s.charAt(s.length()-1)!='}'))
			return s;
		return s.substring(1,s.length()-1);
	}

	@Override
	public List<String> getTitles()
	{
		return titles;
	}

	private String getTitleXML()
	{
		if(titles.size()==0)
			return "";
		for(int t=titles.size()-1;t>=0;t--)
		{
			final String s=titles.get(t);
			if(s.length()==0)
				titles.remove(t);
		}
		final StringBuilder str=new StringBuilder("");
		for(int t=0;t<titles.size();t++)
		{
			final String s=titles.get(t);
			str.append("<TITLE>"+CMLib.xml().parseOutAngleBrackets(CMLib.coffeeFilter().safetyFilter(s))+"</TITLE>");
		}
		return str.toString();
	}

	@Override
	public String getPoofIn()
	{
		return poofin;
	}

	@Override
	public String getPoofOut()
	{
		return poofout;
	}

	@Override
	public String getTranPoofIn()
	{
		return tranpoofin;
	}

	@Override
	public String getTranPoofOut()
	{
		return tranpoofout;
	}

	@Override
	public int[] getBirthday()
	{
		return birthday;
	}

	@Override
	public int initializeBirthday(TimeClock clock, int ageHours, Race R)
	{
		if(clock == null)
			clock=CMLib.time().globalClock();
		birthday=new int[4];
		birthday[0]=clock.getDayOfMonth();
		birthday[1]=clock.getMonth();
		birthday[2]=clock.getYear();
		birthday[3]=clock.getYear();
		while(ageHours>15)
		{
			birthday[2]-=1;
			ageHours-=15;
		}
		if(ageHours>0)
		{
			birthday[1]=CMLib.dice().roll(1,clock.getMonthsInYear(),0);
			birthday[0]=CMLib.dice().roll(1,clock.getDaysInMonth(),0);
		}
		final int month=clock.getMonth();
		final int day=clock.getDayOfMonth();
		if((month<birthday[1])||((month==birthday[1])&&(birthday[0]<day)))
			return (R.getAgingChart()[Race.AGE_YOUNGADULT]+clock.getYear()-birthday[2])-1;
		return (R.getAgingChart()[Race.AGE_YOUNGADULT]+clock.getYear()-birthday[2]);
	}

	protected String getPrivateList(final Set<String> h)
	{
		if((h==null)||(h.size()==0))
			return "";
		final StringBuffer list=new StringBuffer("");
		for (final String string : h)
			list.append((string)+";");
		return list.toString();
	}

	@Override
	public void bumpPrideStat(final PrideStat stat, final int amt)
	{
		final long now=System.currentTimeMillis();
		if(stat!=null)
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			if(period==TimeClock.TimePeriod.ALLTIME)
				prideStats[period.ordinal()][stat.ordinal()]+=amt;
			else
			{
				if(now>prideExpireTime[period.ordinal()])
				{
					for(final AccountStats.PrideStat stat2 : AccountStats.PrideStat.values())
						prideStats[period.ordinal()][stat2.ordinal()]=0;
					prideExpireTime[period.ordinal()]=period.nextPeriod();
				}
				prideStats[period.ordinal()][stat.ordinal()]+=amt;
			}
		}
	}

	@Override
	public int getPrideStat(final TimePeriod period, final PrideStat stat)
	{
		if((period==null)||(stat==null))
			return 0;
		return prideStats[period.ordinal()][stat.ordinal()];
	}

	@Override
	public String getXML()
	{
		final String friendsStr=getPrivateList(getFriends());
		final String ignoredStr=getPrivateList(getIgnored());
		final String privateListStr=getPrivateList(introductions);
		final StringBuffer rest=new StringBuffer("");
		final String[] codes=getStatCodes();
		for(int x=getSaveStatIndex();x<codes.length;x++)
		{
			final String code=codes[x].toUpperCase();
			rest.append("<"+code+">"+CMLib.xml().parseOutAngleBrackets(getStat(code))+"</"+code+">");
		}
		rest.append("<NEXTPRIDEPERIODS>").append(CMParms.toTightListString(prideExpireTime)).append("</NEXTPRIDEPERIODS>");
		rest.append("<PRIDESTATS>");
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
			rest.append(CMParms.toTightListString(prideStats[period.ordinal()])).append(";");
		rest.append("</PRIDESTATS>");
		
		rest.append("<ACHIEVEMENTS");
		for(Iterator<Tracker> i=achievementers.values().iterator();i.hasNext();)
		{
			final Tracker T = i.next();
			if(T.getAchievement().isSavableTracker() && (T.getCount(null) != 0))
				rest.append(" ").append(T.getAchievement().getTattoo()).append("=").append(T.getCount(null));
			// getCount(null) should be ok, because it's only the un-savable trackers that need the mob obj
		}
		rest.append(" />");
		rest.append("<PCCSTATS>")
			.append(bonusCommonSk).append(';')
			.append(bonusCraftSk).append(';')
			.append(bonusNonCraftSk).append(';')
			.append(bonusLanguages).append(';')
			.append(bonusCharStatPt).append(';')
			.append("</PCCSTATS>");
		
		return ((friendsStr.length()>0)?"<FRIENDS>"+friendsStr+"</FRIENDS>":"")
			+((ignoredStr.length()>0)?"<IGNORED>"+ignoredStr+"</IGNORED>":"")
			+((privateListStr.length()>0)?"<INTROS>"+privateListStr+"</INTROS>":"")
			+"<WRAP>"+wrap+"</WRAP>"
			+"<THEME>"+theme+"</THEME>"
			+"<PAGEBREAK>"+pageBreak+"</PAGEBREAK>"
			+((account!=null)?("<ACCOUNT>"+account.getAccountName()+"</ACCOUNT>"):"")
			+getTitleXML()
			+getAliasXML()
			+getLegacyXML()
			+"<ACCTEXP>"+accountExpires+"</ACCTEXP>"
			+((birthday!=null)?"<BIRTHDAY>"+CMParms.toListString(birthday)+"</BIRTHDAY>":"")
			+((poofin.length()>0)?"<POOFIN>"+CMLib.xml().parseOutAngleBrackets(poofin)+"</POOFIN>":"")
			+((notes.length()>0)?"<NOTES>"+CMLib.xml().parseOutAngleBrackets(notes)+"</NOTES>":"")
			+((poofout.length()>0)?"<POOFOUT>"+CMLib.xml().parseOutAngleBrackets(poofout)+"</POOFOUT>":"")
			+((announceMsg.length()>0)?"<ANNOUNCE>"+CMLib.xml().parseOutAngleBrackets(announceMsg)+"</ANNOUNCE>":"")
			+((savedPose.length()>0)?"<POSE>"+CMLib.xml().parseOutAngleBrackets(savedPose)+"</POSE>":"")
			+((tranpoofin.length()>0)?"<TRANPOOFIN>"+CMLib.xml().parseOutAngleBrackets(tranpoofin)+"</TRANPOOFIN>":"")
			+((tranpoofout.length()>0)?"<TRANPOOFOUT>"+CMLib.xml().parseOutAngleBrackets(tranpoofout)+"</TRANPOOFOUT>":"")
			+"<DATES>"+CMLib.xml().parseOutAngleBrackets(this.getLevelDateTimesStr())+"</DATES>"
			+"<SECGRPS>"+CMLib.xml().parseOutAngleBrackets(getSetSecurityFlags(null))+"</SECGRPS>"
			+"<AUTOINVSET>"+CMLib.xml().parseOutAngleBrackets(getStat("AUTOINVSET"))+"</AUTOINVSET>"
			+roomSet().xml()
			+rest.toString();
	}

	private void setBirthday(String bday)
	{
		if((bday!=null)&&(bday.length()>0))
		{
			final List<String> V=CMParms.parseCommas(bday,true);
			birthday=new int[4];
			for(int v=0;(v<V.size()) && (v<birthday.length);v++)
				birthday[v]=CMath.s_int(V.get(v));
			if(V.size()<4)
			{
				final TimeClock C=CMLib.time().globalClock();
				birthday[3]=C.getYear();
			}
		}
	}

	private void setAliasXML(List<XMLTag> xml)
	{
		alias.clear();
		for (final XMLTag piece : xml)
		{
			if((piece.tag().equals("ALIAS"))&&(piece.parms()!=null))
			{
				final String command=piece.getParmValue( "CMD");
				final String value=piece.getParmValue( "VAL");
				if((command!=null)&&(value!=null))
					alias.put(command, CMLib.xml().restoreAngleBrackets(value));
			}
		}
		int a=-1;
		while((++a)>=0)
		{
			final String name=CMLib.xml().getValFromPieces(xml,"ALIAS"+a);
			final String value=CMLib.xml().getValFromPieces(xml,"ALIASV"+a);
			if((name.length()==0)||(value.length()==0))
				break;
			alias.put(name.toUpperCase().trim(),CMLib.xml().restoreAngleBrackets(value));
		}
	}

	private void setLegacyXML(List<XMLTag> xml)
	{
		legacy.clear();
		for (final XMLTag piece : xml)
		{
			if((piece.tag().equals("LEGACY"))&&(piece.parms()!=null))
			{
				final String category=piece.getParmValue( "CAT");
				final String levelStr=piece.getParmValue( "LVL");
				if((category!=null)&&(levelStr!=null))
					legacy.put(category, Integer.valueOf(levelStr));
			}
		}
	}

	private void setTitleXML(List<XMLTag> xml)
	{
		titles.clear();
		for (final XMLTag piece : xml)
		{
			if(piece.tag().equals("TITLE"))
				titles.add(CMLib.xml().restoreAngleBrackets(piece.value()));
		}
		int t=-1;
		while((++t)>=0)
		{
			final String title=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xml,"TITLE"+t));
			if(title.length()==0)
				break;
			titles.add(title);
		}

	}

	@Override
	public Map<String, AbilityMapping> getExtraQualifiedSkills()
	{
		return ableMap;
	}
	
	@Override
	public void setXML(String xmlStr)
	{
		account = null;
		if(xmlStr==null)
			return;
		final XMLLibrary xmlLib=CMLib.xml();
		final boolean debug=CMSecurity.isDebugging(CMSecurity.DbgFlag.PLAYERSTATS);
		if(debug)
			Log.debugOut("XML="+xmlStr);
		final List<XMLLibrary.XMLTag> xml = xmlLib.parseAllXML(xmlStr);
		String str=xmlLib.getValFromPieces(xml,"FRIENDS");
		if(debug)
			Log.debugOut("FRIENDS="+str);
		friends.clear();
		friends.addAll(getHashFrom(str));
		str=xmlLib.getValFromPieces(xml,"IGNORED");
		if(debug)
			Log.debugOut("IGNORED="+str);
		ignored.clear();
		ignored.addAll(getHashFrom(str));
		str=xmlLib.getValFromPieces(xml,"INTROS");
		if(debug)
			Log.debugOut("INTROS="+str);
		introductions.clear();
		introductions.addAll(getHashFrom(str));
		str=xmlLib.getValFromPieces(xml, "THEME");
		if(debug)
			Log.debugOut("THEME="+str);
		if(CMath.isInteger(str))
			theme=CMath.s_int(str);
		str = xmlLib.getValFromPieces(xml,"ACCTEXP");
		if(debug)
			Log.debugOut("ACCTEXP="+str);
		if((str!=null)&&(str.length()>0))
			setAccountExpiration(CMath.s_long(str));
		else
		{
			final Calendar C=Calendar.getInstance();
			C.add(Calendar.DATE,CMProps.getIntVar(CMProps.Int.TRIALDAYS));
			setAccountExpiration(C.getTimeInMillis());
		}
		str=xmlLib.getValFromPieces(xml,"WRAP");
		if(debug)
			Log.debugOut("WRAP="+str);
		if(CMath.isInteger(str))
			setWrap(CMath.s_int(str));
		str=xmlLib.getValFromPieces(xml,"PAGEBREAK");
		if(debug)
			Log.debugOut("PAGEBREAK="+str);
		if(CMath.isInteger(str))
			pageBreak=CMath.s_int(str);
		else
			pageBreak=CMProps.getIntVar(CMProps.Int.PAGEBREAK);
		str=xmlLib.restoreAngleBrackets(xmlLib.getValFromPieces(xml,"SECGRPS"));
		if(debug)
			Log.debugOut("SECGRPS="+str);
		getSetSecurityFlags(str);
		setAliasXML(xml);
		setTitleXML(xml);
		setLegacyXML(xml);
		str=xmlLib.getValFromPieces(xml,"BIRTHDAY");
		if(debug)
			Log.debugOut("BIRTHDAY="+str);
		setBirthday(str);

		poofin=xmlLib.getValFromPieces(xml,"POOFIN");
		if(debug)
			Log.debugOut("POOFIN="+poofin);
		if(poofin==null)
			poofin="";
		poofin=xmlLib.restoreAngleBrackets(poofin);

		poofout=xmlLib.getValFromPieces(xml,"POOFOUT");
		if(debug)
			Log.debugOut("POOFOUT="+poofout);
		if(poofout==null)
			poofout="";
		poofout=xmlLib.restoreAngleBrackets(poofout);

		tranpoofin=xmlLib.getValFromPieces(xml,"TRANPOOFIN");
		if(debug)
			Log.debugOut("TRANPOOFIN="+tranpoofin);
		if(tranpoofin==null)
			tranpoofin="";
		tranpoofin=xmlLib.restoreAngleBrackets(tranpoofin);

		tranpoofout=xmlLib.getValFromPieces(xml,"TRANPOOFOUT");
		if(debug)
			Log.debugOut("TRANPOOFOUT="+tranpoofout);
		if(tranpoofout==null)
			tranpoofout="";
		tranpoofout=xmlLib.restoreAngleBrackets(tranpoofout);

		announceMsg=xmlLib.getValFromPieces(xml,"ANNOUNCE");
		if(debug)
			Log.debugOut("ANNOUNCE="+announceMsg);
		if(announceMsg==null)
			announceMsg="";
		announceMsg=xmlLib.restoreAngleBrackets(announceMsg);

		savedPose=xmlLib.getValFromPieces(xml,"POSE");
		if(debug)
			Log.debugOut("POSE="+savedPose);
		if(savedPose==null)
			savedPose="";
		savedPose=xmlLib.restoreAngleBrackets(savedPose);

		notes=xmlLib.getValFromPieces(xml,"NOTES");
		if(debug)
			Log.debugOut("NOTES="+notes);
		if(notes==null)
			notes="";
		notes=xmlLib.restoreAngleBrackets(notes);

		str=xmlLib.restoreAngleBrackets(xmlLib.getValFromPieces(xml,"DATES"));
		if(debug)
			Log.debugOut("DATES="+str);
		if(str==null)
			str="";
		// now parse all the level date/times
		int lastNum=Integer.MIN_VALUE;
		levelInfo.clear();
		if(str.length()>0)
		{
			final List<String> sets=CMParms.parseSemicolons(str,true);
			for(int ss=0;ss<sets.size();ss++)
			{
				final String sStr=sets.get(ss);
				final List<String> twin=CMParms.parseCommas(sStr,true);
				if((twin.size()!=2)&&(twin.size()!=3)&&(twin.size()!=4))
					continue;
				if(CMath.s_int(twin.get(0))>=lastNum)
				{
					lastNum=CMath.s_int(twin.get(0));
					levelInfo.addElement(Integer.valueOf(lastNum),
										 Long.valueOf(CMath.s_long(twin.get(1))),
										 (twin.size()>2)?(String)twin.get(2):"",
										 (twin.size()>3)?Long.valueOf(CMath.s_long(twin.get(3))):Long.valueOf(0));
				}
			}
		}
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"",Long.valueOf(0));
		str = xmlLib.getValFromPieces(xml,"AREAS");
		if(debug)
			Log.debugOut("AREAS="+str);
		if(str!=null)
			roomSet().parseXML("<AREAS>"+str+"</AREAS>");
		else
			roomSet().parseXML("<AREAS />");
		final XMLTag achievePiece = xmlLib.getPieceFromPieces(xml, "ACHIEVEMENTS");
		achievementers.clear();
		for(Enumeration<Achievement> a=CMLib.achievements().achievements(Agent.PLAYER);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if((achievePiece != null) && achievePiece.parms().containsKey(A.getTattoo()))
				achievementers.put(A.getTattoo(), A.getTracker(CMath.s_int(achievePiece.parms().get(A.getTattoo()).trim())));
			else
				achievementers.put(A.getTattoo(), A.getTracker(0));
		}
		
		final String[] codes=getStatCodes();
		for(int i=getSaveStatIndex();i<codes.length;i++)
		{
			str=xmlLib.getValFromPieces(xml,codes[i].toUpperCase());
			if(str==null)
				str="";
			setStat(codes[i].toUpperCase(),xmlLib.restoreAngleBrackets(str));
		}
		final String[] nextPeriods=xmlLib.getValFromPieces(xml, "NEXTPRIDEPERIODS").split(",");
		final String[] prideStats=xmlLib.getValFromPieces(xml, "PRIDESTATS").split(";");
		final Pair<Long,int[]>[] finalPrideStats = CMLib.players().parsePrideStats(nextPeriods, prideStats);
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			if(period.ordinal()<finalPrideStats.length)
			{
				this.prideExpireTime[period.ordinal()]=finalPrideStats[period.ordinal()].first.longValue();
				this.prideStats[period.ordinal()]=finalPrideStats[period.ordinal()].second;
			}
		}

		str = xmlLib.getValFromPieces(xml,"ACCOUNT");
		if(debug)
			Log.debugOut("ACCOUNT="+str);
		if(CMProps.isUsingAccountSystem())
		{
			if((str != null)&&(str.length()>0))
				account = CMLib.players().getLoadAccount(str);
		}
		
		final String[] allAccStats=xmlLib.getValFromPieces(xml, "PCCSTATS").split(";");
		if(allAccStats.length>=5)
		{
			bonusCommonSk=CMath.s_int(allAccStats[0]);
			bonusCraftSk=CMath.s_int(allAccStats[1]);
			bonusNonCraftSk=CMath.s_int(allAccStats[2]);
			bonusLanguages=CMath.s_int(allAccStats[3]);
			bonusCharStatPt=CMath.s_int(allAccStats[4]);
		}
		
		setStat("AUTOINVSET",CMLib.xml().restoreAngleBrackets(xmlLib.getValFromPieces(xml,"AUTOINVSET")));
	}

	private String getLevelDateTimesStr()
	{
		if(levelInfo.size()==0)
			levelInfo.add(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"",Long.valueOf(0));
		final StringBuilder buf=new StringBuilder("");
		for(int ss=0;ss<levelInfo.size();ss++)
		{
			buf.append(levelInfo.elementAtFirst(ss).toString()).append(",");
			buf.append(levelInfo.elementAtSecond(ss).toString()).append(",");
			buf.append(levelInfo.elementAtThird(ss).toString()).append(",");
			buf.append(levelInfo.elementAtFourth(ss).toString()).append(";");
		}
		return buf.toString();
	}

	@Override
	public String getSetSecurityFlags(String newFlags)
	{
		if(newFlags != null)
		{
			securityFlags=CMSecurity.instance().createGroup("", CMParms.parseSemicolons(newFlags,true));
		}
		return securityFlags.toString(';');

	}

	@Override
	public CMSecurity.SecGroup getSecurityFlags()
	{
		return securityFlags;
	}

	@Override
	public void setPoofs(String poofIn, String poofOut, String tranPoofIn, String tranPoofOut)
	{
		poofin=poofIn;
		poofout=poofOut;
		tranpoofin=tranPoofIn;
		tranpoofout=tranPoofOut;
	}

	@Override
	public long getHygiene()
	{
		return hygiene;
	}

	@Override
	public void setHygiene(long newVal)
	{
		hygiene=newVal;
	}

	@Override
	public boolean adjHygiene(long byThisMuch)
	{
		hygiene+=byThisMuch;
		if(hygiene<1)
		{
			hygiene=0;
			return false;
		}
		return true;
	}

	@Override
	public Map<String, int[]> getCombatSpams()
	{
		return this.combatSpams;
	}

	// Acct Expire Code
	@Override
	public long getAccountExpiration()
	{
		return  (account != null) ? account.getAccountExpiration() : accountExpires;
	}

	@Override
	public void setAccountExpiration(long newVal)
	{
		if(account != null)
			account.setAccountExpiration(newVal);
		accountExpires=newVal;
	}

	@Override
	public boolean addRoomVisit(Room R)
	{
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))
		&&(R!=null)
		&&(!CMath.bset(R.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNEXPLORABLE))
		&&(!(R.getArea() instanceof AutoGenArea))
		&&(R.getArea()!=null)
		&&(!hasVisited(R)))
		{
			roomSet().add(CMLib.map().getExtendedRoomID(R));
			return true;
		}
		return false;
	}

	@Override
	public boolean hasVisited(Room R)
	{
		return roomSet().contains(CMLib.map().getExtendedRoomID(R));
	}

	@Override
	public boolean hasVisited(Area A)
	{
		final int numRooms=A.getAreaIStats()[Area.Stats.VISITABLE_ROOMS.ordinal()];
		if(numRooms<=0)
			return true;
		return roomSet().roomCount(A.Name())>0;
	}

	@Override
	public void unVisit(Room R)
	{
		if(R != null)
		{
			if(roomSet().contains(CMLib.map().getExtendedRoomID(R)))
				roomSet().remove(CMLib.map().getExtendedRoomID(R));
		}
	}

	@Override
	public void unVisit(Area A)
	{
		if(A != null)
		{
			Room R;
			for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
			{
				R=r.nextElement();
				if(roomSet().contains(CMLib.map().getExtendedRoomID(R)))
					roomSet().remove(CMLib.map().getExtendedRoomID(R));
			}
		}
	}

	@Override
	public int percentVisited(MOB mob, Area A)
	{
		if(A==null)
		{
			long totalRooms=0;
			long totalVisits=0;
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				A=e.nextElement();
				if((!CMLib.flags().isHidden(A))
				&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
				{
					final int[] stats=A.getAreaIStats();
					if(stats[Area.Stats.VISITABLE_ROOMS.ordinal()]>0)
					{
						totalRooms+=stats[Area.Stats.VISITABLE_ROOMS.ordinal()];
						totalVisits+=roomSet().roomCount(A.Name());
					}
				}
			}
			if(totalRooms==0)
				return 100;
			final double pct=CMath.div(totalVisits,totalRooms);
			return (int)Math.round(100.0*pct);
		}
		final int numRooms=A.getAreaIStats()[Area.Stats.VISITABLE_ROOMS.ordinal()];
		if(numRooms<=0)
			return 100;
		final double pct=CMath.div(roomSet().roomCount(A.Name()),numRooms);
		return (int)Math.round(100.0*pct);
	}

	@Override
	public void killAchievementTracker(final Achievement A, final MOB mob)
	{
		if(achievementers.containsKey(A.getTattoo()))
		{
			achievementers.remove(A.getTattoo());
			if(mob != null)
			{
				mob.delTattoo(A.getTattoo());
			}
		}
	}
	
	@Override
	public Tracker getAchievementTracker(final Achievement A, final MOB mob)
	{
		final Tracker T;
		if(achievementers.containsKey(A.getTattoo()))
		{
			T=achievementers.get(A.getTattoo());
		}
		else
		{
			T=A.getTracker(0);
			achievementers.put(A.getTattoo(), T);
		}
		return T;
	}

	@Override
	public void rebuildAchievementTracker(final MOB mob, String achievementTattoo)
	{
		Achievement A=CMLib.achievements().getAchievement(achievementTattoo);
		if(A!=null)
		{
			if(achievementers.containsKey(A.getTattoo()))
				achievementers.put(A.getTattoo(), A.getTracker(achievementers.get(A.getTattoo()).getCount(mob)));
			else
				achievementers.put(A.getTattoo(), A.getTracker(0));
		}
		else
			achievementers.remove(achievementTattoo);
	}
	
	@Override
	public long leveledDateTime(int level)
	{
		if(levelInfo.size()==0)
			levelInfo.add(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"",Long.valueOf(0));
		long lowest=levelInfo.elementAtSecond(0).longValue();
		for(int l=1;l<levelInfo.size();l++)
		{
			if(level==levelInfo.elementAtFirst(l).intValue())
				return levelInfo.elementAtSecond(l).longValue();
			else
			if(level<levelInfo.elementAtFirst(l).intValue())
				return lowest;
			lowest=levelInfo.elementAtSecond(l).longValue();
		}
		return lowest;
	}

	@Override
	public String leveledRoomID(int level)
	{
		if(levelInfo.size()==0)
			levelInfo.add(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"",Long.valueOf(0));
		for(int l=1;l<levelInfo.size();l++)
		{
			if(level==levelInfo.elementAtFirst(l).intValue())
				return levelInfo.elementAtThird(l);
			else
			if(level<levelInfo.elementAtFirst(l).intValue())
				return levelInfo.elementAtThird(l-1);
		}
		return "";
	}

	@Override
	public long leveledMinutesPlayed(int level)
	{
		if(levelInfo.size()==0)
			levelInfo.add(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"",Long.valueOf(0));
		long age=levelInfo.elementAtFourth(0).longValue();
		for(int l=1;l<levelInfo.size();l++)
		{
			if(level<levelInfo.elementAtFirst(l).intValue())
				return age;
			age=levelInfo.elementAtFourth(l).longValue();
		}
		return age;
	}

	@Override
	public void setLeveledDateTime(int level, long ageHours, Room R)
	{
		if(levelInfo.size()==0)
			levelInfo.add(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"",Long.valueOf(0));
		long lastTime=0;
		for(int l=0;l<levelInfo.size();l++)
		{
			final Quad<Integer, Long, String, Long> quad = levelInfo.elementAt(l);
			if(level==quad.first.intValue())
			{
				quad.second = Long.valueOf(System.currentTimeMillis());
				quad.third = CMLib.map().getExtendedRoomID(R);
				quad.fourth = Long.valueOf(ageHours);
				return;
			}
			else
			if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_SECOND)
				return;
			else
			if(level<quad.first.intValue())
			{
				levelInfo.insertElementAt(new Quad<Integer,Long,String,Long>(
											Integer.valueOf(level),
											Long.valueOf(System.currentTimeMillis()),
											CMLib.map().getExtendedRoomID(R),
											Long.valueOf(ageHours)),l);
				return;
			}
			lastTime=quad.second.longValue();
		}
		if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_SECOND)
			return;
		levelInfo.addElement(Integer.valueOf(level),Long.valueOf(System.currentTimeMillis()),CMLib.map().getExtendedRoomID(R),Long.valueOf(ageHours));
	}

	@Override
	public int getTotalLegacyLevels()
	{
		int total=0;
		for(final Integer value : legacy.values())
			total+=value.intValue();
		return total;
	}

	@Override
	public void addLegacyLevel(String category)
	{
		final Integer level=legacy.get(category);
		if(level != null)
			legacy.put(category, Integer.valueOf(level.intValue()+1));
		else
			legacy.put(category, Integer.valueOf(1));
	}

	@Override
	public int getLegacyLevel(String category)
	{
		final Integer level=legacy.get(category);
		if(level != null)
			return level.intValue();
		return 0;
	}

	@Override
	public Map<String,ExpertiseDefinition> getExtraQualifiedExpertises()
	{
		return experMap;
	}
	
	@Override
	public PlayerAccount getAccount()
	{
		return account;
	}

	@Override
	public void setAccount(PlayerAccount account)
	{
		this.account = account;
	}

	@Override
	public ItemCollection getExtItems()
	{
		return extItems;
	}

	@Override
	public int getBonusCharStatPoints()
	{
		return this.bonusCharStatPt;
	}

	@Override
	public void setBonusCharStatPoints(int bonus)
	{
		this.bonusCharStatPt = bonus;
	}

	@Override
	public int getBonusCommonSkillLimits()
	{
		return this.bonusCommonSk;
	}

	@Override
	public void setBonusCommonSkillLimits(int bonus)
	{
		this.bonusCommonSk = bonus;
	}

	@Override
	public int getBonusCraftingSkillLimits()
	{
		return this.bonusCraftSk;
	}

	@Override
	public void setBonusCraftingSkillLimits(int bonus)
	{
		this.bonusCraftSk = bonus;
	}

	@Override
	public int getBonusNonCraftingSkillLimits()
	{
		return this.bonusNonCraftSk;
	}

	@Override
	public void setBonusNonCraftingSkillLimits(int bonus)
	{
		this.bonusNonCraftSk = bonus;
	}

	@Override
	public int getBonusLanguageLimits()
	{
		return this.bonusLanguages;
	}

	@Override
	public void setBonusLanguageLimits(int bonus)
	{
		this.bonusLanguages = bonus;
	}

	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","TITLES",
									 "ALIAS","LASTIP","LASTDATETIME",
									 "CHANNELMASK",
									 "COLORSTR","PROMPT","POOFIN",
									 "POOFOUT","TRANPOOFIN","TRAINPOOFOUT",
									 "ANNOUNCEMSG","NOTES","WRAP","BIRTHDAY",
									 "ACCTEXPIRATION","INTRODUCTIONS","PAGEBREAK",
									 "SAVEDPOSE","THEME", "LEGLEVELS","BONUSCOMMON",
									 "BONUSCRAFT","BONUSNONCRAFT","BONUSLANGS",
									 "BONUSCHARSTATS","AUTOINVSET"};

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return getPrivateList(getFriends());
		case 2:
			return getPrivateList(getIgnored());
		case 3:
			return getTitleXML();
		case 4:
			return getAliasXML();
		case 5:
			return lastIP;
		case 6:
			return "" + lLastDateTime;
		case 7:
			return "" + channelMask;
		case 8:
			return colorStr;
		case 9:
			return prompt;
		case 10:
			return poofin;
		case 11:
			return poofout;
		case 12:
			return tranpoofin;
		case 13:
			return tranpoofout;
		case 14:
			return announceMsg;
		case 15:
			return notes;
		case 16:
			return "" + wrap;
		case 17:
			return CMParms.toListString(birthday);
		case 18:
			return "" + accountExpires;
		case 19:
			return getPrivateList(introductions);
		case 20:
			return "" + pageBreak;
		case 21:
			return "" + savedPose;
		case 22:
			return "" + theme;
		case 23:
			return "" + getTotalLegacyLevels();
		case 24:
			return "" + bonusCommonSk;
		case 25:
			return "" + bonusCraftSk;
		case 26:
			return "" + bonusNonCraftSk;
		case 27:
			return "" + bonusLanguages;
		case 28:
			return "" + bonusCharStatPt;
		case 29:
			return CMParms.combineWith(autoInvokeSet, ',');
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	
	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			break;
		case 1:
		{
			friends.clear();
			friends.addAll(getHashFrom(val));
			break;
		}
		case 2:
		{
			ignored.clear();
			ignored.addAll(getHashFrom(val));
			break;
		}
		case 3:
			setTitleXML(CMLib.xml().parseAllXML(val));
			break;
		case 4:
			setAliasXML(CMLib.xml().parseAllXML(val));
			break;
		case 5:
			lastIP = val;
			break;
		case 6:
			lLastDateTime = CMath.s_parseLongExpression(val);
			break;
		case 7:
			channelMask = CMath.s_parseIntExpression(val);
			break;
		case 8:
			colorStr = val;
			break;
		case 9:
			prompt = val;
			break;
		case 10:
			poofin = val;
			break;
		case 11:
			poofout = val;
			break;
		case 12:
			tranpoofin = val;
			break;
		case 13:
			tranpoofout = val;
			break;
		case 14:
			announceMsg = val;
			break;
		case 15:
			notes = val;
			break;
		case 16:
			setWrap(CMath.s_parseIntExpression(val));
			break;
		case 17:
			setBirthday(val);
			break;
		case 18:
			accountExpires = CMath.s_parseLongExpression(val);
			break;
		case 19:
		{
			introductions.clear();
			introductions.addAll(getHashFrom(val));
			break;
		}
		case 20:
			pageBreak = CMath.s_parseIntExpression(val);
			break;
		case 21:
			savedPose = val;
			break;
		case 22:
			theme = CMath.s_parseIntExpression(val);
			break;
		case 23:
			break; // legacy levels
		case 24:
			bonusCommonSk = CMath.s_parseIntExpression(val);
			break;
		case 25:
			bonusCraftSk = CMath.s_parseIntExpression(val);
			break;
		case 26:
			bonusNonCraftSk = CMath.s_parseIntExpression(val);
			break;
		case 27:
			bonusLanguages = CMath.s_parseIntExpression(val);
			break;
		case 28:
			bonusCharStatPt = CMath.s_parseIntExpression(val);
			break;
		case 29:
			autoInvokeSet = new XTreeSet<String>(CMParms.parseAny(val,',',true));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	public boolean isOnAutoInvokeList(String abilityID)
	{
		if(abilityID != null)
		{
			if(abilityID.equals("ANYTHING"))
				return this.autoInvokeSet.size()>0;
			return this.autoInvokeSet.contains(abilityID);
		}
		return false;
	}
	
	@Override
	public void addAutoInvokeList(String abilityID)
	{
		this.autoInvokeSet.add(abilityID);
	}
	
	@Override
	public void removeAutoInvokeList(String abilityID)
	{
		this.autoInvokeSet.remove(abilityID);
	}
	
	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes==null)
			codes=CMProps.getStatCodesList(CODES,this);
		return codes;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean sameAs(PlayerStats E)
	{
		if(!(E instanceof DefaultPlayerStats))
			return false;
		for(int i=0;i<getStatCodes().length;i++)
		{
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		}
		return true;
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void destroy()
	{
		friends.clear();
		ignored.clear();
		tellStack.clear();
		gtellStack.clear();
		titles.clear();
		autoInvokeSet.clear();
		account = null;
		visitedRoomSet	= null;
		introductions.clear();
		extItems.delAllItems(true);
		achievementers.clear();
		alias.clear();
		legacy.clear();
		combatSpams.clear();
		ableMap.clear();
		experMap.clear();
		levelInfo.clear();
	}

	@Override
	public boolean isSavable()
	{
		return isSavable;
	}

	@Override
	public boolean amDestroyed()
	{
		return false;
	}

	@Override
	public void setSavable(boolean truefalse)
	{
		synchronized(this)
		{
			isSavable=truefalse;
		}
	}
}
