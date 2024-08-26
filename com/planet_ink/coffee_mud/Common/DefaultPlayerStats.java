package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.ExpertiseAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
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
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2024 Bo Zimmerman

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

	protected final static int	TELL_STACK_MAX_SIZE		= 50;
	protected final static int	GTELL_STACK_MAX_SIZE	= 50;
	protected final static int	DEFAULT_WORDWRAP		= 78;

	protected long			 hygiene		= 0;
	protected int			 theme			= Area.THEME_FANTASY;
	protected String[]		 xtraValues		= null;
	protected String		 lastIP			= "";
	protected long  		 lLastDateTime	= System.currentTimeMillis();
	protected long			 lastXPDateTime	= 0;
	protected long  		 lastUpdated	= 0;
	protected int			 rolePlayXP		= 0;
	protected int			 maxRolePlayXP	= Integer.MAX_VALUE;
	protected long			 lastRolePlayTm = System.currentTimeMillis();
	protected int			 deferredXP		= 0;
	protected int			 maxDeferredXP	= 0;
	protected int			 deathCounter	= 0;
	protected int   		 channelMask;
	protected String		 email			= "";
	protected String		 password		= "";
	protected String		 colorStr		= "";
	protected String		 prompt			= "";
	protected String		 poofin			= "";
	protected String		 poofout		= "";
	protected String		 tranpoofin		= "";
	protected String		 tranpoofout	= "";
	protected String		 deathPoof		= "";
	protected String		 announceMsg	= "";
	protected String		 savedPose		= "";
	protected boolean		 poseConstant	= true; // makes the location change a bit faster
	protected String		 notes			= "";
	private volatile String  actTitle		= null;
	protected int   		 wrap			= DEFAULT_WORDWRAP;
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
	protected Set<String>	 subscriptions	= new SHashSet<String>();
	protected List<TellMsg>	 tellStack		= new SVector<TellMsg>();
	protected List<TellMsg>	 gtellStack		= new SVector<TellMsg>();
	protected List<Title>	 titles			= new SVector<Title>();
	protected Set<String>	 autoInvokeSet	= new TreeSet<String>();
	protected PlayerAccount  account		= null;
	protected SecGroup		 securityFlags	= new SecGroup(new CMSecurity.SecFlag[]{});
	protected long			 accountExpires	= 0;
	protected RoomnumberSet  visitedRoomSet	= null;
	protected RoomnumberSet  tVisitedRoomSet= null;
	protected Set<String>	 introductions	= new SHashSet<String>();
	protected long[]	 	 prideExpireTime= new long[TimeClock.TimePeriod.values().length];
	protected int[][]		 prideStats		= new int[TimeClock.TimePeriod.values().length][AccountStats.PrideStat.values().length];
	protected long[][]		 combatStats	= new long[0][PlayerCombatStat.values().length];
	protected TimeClock		 birthdayClock	= null;

	protected ItemCollection extItems;

	protected volatile boolean		isSavable		= true;
	protected Map<String,Tracker>	achievementers	= new STreeMap<String,Tracker>();
	protected Map<String,String>	alias			= new STreeMap<String,String>();
	protected Map<String,Integer>	legacy			= new STreeMap<String,Integer>();
	protected Map<String,int[]>		combatSpams		= new STreeMap<String,int[]>();
	protected Set<PlayerFlag>		playFlags		= new SHashSet<PlayerFlag>();
	protected List<LevelInfo>		levelInfo		= new SVector<LevelInfo>();

	protected Map<String, AbilityMapping>		ableMap		= new SHashtable<String, AbilityMapping>();
	protected Map<String, ExpertiseDefinition>	experMap	= new SHashtable<String, ExpertiseDefinition>();
	protected Map<CharClass,Map<String,Object>>	classMap	= new STreeMap<CharClass,Map<String,Object>>();

	private class Title
	{
		public String	s;
		public boolean	r	= false;

		protected Title(final String s)
		{
			this.s=s;
		}
	}

	private static final Converter<Title, String> titleConverter = new Converter<Title, String>()
	{
		@Override
		public String convert(final Title obj)
		{
			return obj.s;
		}

	};

	private class LevelInfo
	{
		public int		level;
		public long		time;
		public String	roomID;
		public long		mins;
		public int[]	costGains	= new int[CostDef.CostType.values().length];
	}

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
			return getClass().getDeclaredConstructor().newInstance();
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
			O.levelInfo=new SVector<LevelInfo>();
			O.levelInfo.addAll(levelInfo);
			if(visitedRoomSet!=null)
				O.visitedRoomSet=(RoomnumberSet)visitedRoomSet.copyOf();
			else
				O.visitedRoomSet=null;
			O.tVisitedRoomSet = null;
			O.securityFlags=securityFlags.copyOf();
			O.friends=new SHashSet<String>(friends);
			O.ignored=new SHashSet<String>(ignored);
			O.subscriptions=new SHashSet<String>(subscriptions);
			O.tellStack=new SVector<TellMsg>(tellStack);
			O.gtellStack=new SVector<TellMsg>(gtellStack);
			O.titles=new SVector<Title>(titles);
			O.alias=new SHashtable<String,String>(alias);
			O.legacy=new SHashtable<String,Integer>(legacy);
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			O.extItems=(ItemCollection)extItems.copyOf();
			O.playFlags = new SHashSet<PlayerFlag>(playFlags);
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
	public void setLastIP(final String ip)
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
	public void setEmail(final String newAdd)
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
	public void setTheme(final int theme)
	{
		this.theme=theme;
	}

	@Override
	public long getLastUpdated()
	{
		return lastUpdated;
	}

	@Override
	public void setLastUpdated(final long time)
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
	public void setLastDateTime(final long C)
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
	public void setPassword(final String newPassword)
	{
		password = CMLib.encoder().makeFinalPasswordString(newPassword);
		if(account != null)
			account.setPassword(password);
	}

	@Override
	public boolean matchesPassword(final String checkPass)
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
	public void setPageBreak(final int newBreak)
	{
		pageBreak=newBreak;
	}

	@Override
	public String getNotes()
	{
		return notes;
	}

	@Override
	public void setNotes(final String newnotes)
	{
		notes=newnotes;
	}

	@Override
	public boolean isSet(final PlayerFlag flag)
	{
		return playFlags.contains(flag);
	}

	@Override
	public void setFlag(final PlayerFlag flag, final boolean setOrUnset)
	{
		if(setOrUnset)
			playFlags.add(flag);
		else
			playFlags.remove(flag);
	}

	@Override
	public void setChannelMask(final int newMask)
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
	public void setReplyTo(final MOB mob, final int replyType)
	{
		replyTo=mob;
		this.replyType=replyType;
	}

	@Override
	public void setPrompt(final String newPrompt)
	{
		prompt=newPrompt;
	}

	@Override
	public String getColorStr()
	{
		return colorStr;
	}

	@Override
	public void setColorStr(final String newColors)
	{
		colorStr=newColors;
	}

	@Override
	public String getAnnounceMessage()
	{
		return announceMsg;
	}

	@Override
	public void setAnnounceMessage(final String msg)
	{
		announceMsg=msg;
	}

	@Override
	public String getSavedPose()
	{
		return savedPose;
	}

	@Override
	public void setSavedPose(final String msg, final boolean constant)
	{
		if(msg == null)
			savedPose="";
		else
			savedPose=msg;
		poseConstant = constant;
	}

	@Override
	public boolean isPoseConstant()
	{
		return poseConstant;
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
	public boolean isIntroducedTo(final String name)
	{
		return introductions.contains(name.toUpperCase().trim());
	}

	@Override
	public void introduceTo(final String name)
	{
		if((!isIntroducedTo(name))&&(name.trim().length()>0))
			introductions.add(name.toUpperCase().trim());
	}

	public SHashSet<String> getHashFrom(String str, final boolean trim)
	{
		final SHashSet<String> h=new SHashSet<String>();
		if((str==null)||(str.length()==0))
			return h;
		str=CMStrings.replaceAll(str,"<FRIENDS>","");
		str=CMStrings.replaceAll(str,"<SUBSCRIPTIONS>","");
		str=CMStrings.replaceAll(str,"<IGNORED>","");
		str=CMStrings.replaceAll(str,"<INTROS>","");
		str=CMStrings.replaceAll(str,"</INTROS>","");
		str=CMStrings.replaceAll(str,"</FRIENDS>","");
		str=CMStrings.replaceAll(str,"</SUBSCRIPTIONS>","");
		str=CMStrings.replaceAll(str,"</IGNORED>","");
		int x=str.indexOf(';');
		while(x>=0)
		{
			final String fi=(trim?str.substring(0,x).trim():str.substring(0,x));
			if(fi.length()>0)
				h.add(fi);
			str=str.substring(x+1);
			x=str.indexOf(';');
		}
		if(str.trim().length()>0)
			h.add(trim?str.trim():str);
		return h;
	}

	@Override
	public void addTellStack(final String from, final String to, final String msg)
	{
		if(tellStack.size()>TELL_STACK_MAX_SIZE)
			tellStack.remove(0);
		tellStack.add(makeTellMsg(from,to,msg));
	}

	@Override
	public List<TellMsg> queryTellStack(final String fromName, final String toName, final Long sinceTime)
	{
		final List<TellMsg> msgs=new Vector<TellMsg>();
		for(final PlayerStats.TellMsg M : getTellStack())
		{
			if(((sinceTime == null)||(M.time()>=sinceTime.longValue()))
			&&((fromName==null)||(M.from().equalsIgnoreCase(fromName)))
			&&((toName==null)||(M.to().equalsIgnoreCase(toName))))
				msgs.add(M);
		}
		return msgs;
	}

	@Override
	public List<TellMsg> getTellStack()
	{
		return new ReadOnlyList<TellMsg>(tellStack);
	}

	private RoomnumberSet roomSet()
	{
		if(visitedRoomSet==null)
			visitedRoomSet=((RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet"));
		return visitedRoomSet;
	}

	private RoomnumberSet tempRoomSet()
	{
		if(tVisitedRoomSet==null)
		{
			tVisitedRoomSet=((RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet"));
			tVisitedRoomSet.setSingleAreaFlag(true);
		}
		return tVisitedRoomSet;
	}

	protected TellMsg makeTellMsg(final String from, final String to, final String msg)
	{
		return new TellMsg()
		{
			private final String fromName = from;
			private final String toName = to;
			private final String msgStr = msg;
			private final long time = System.currentTimeMillis();

			@Override
			public String to()
			{
				return toName;
			}

			@Override
			public String from()
			{
				return fromName;
			}

			@Override
			public long time()
			{
				return time;
			}

			@Override
			public String message()
			{
				return msgStr;
			}

		};
	}

	@Override
	public void addGTellStack(final String from, final String to, final String msg)
	{
		if(gtellStack.size()>GTELL_STACK_MAX_SIZE)
			gtellStack.remove(0);
		gtellStack.add(makeTellMsg(from,to,msg));
	}

	@Override
	public List<TellMsg> queryGTellStack(final String fromName, final String toName, final Long sinceTime)
	{
		final List<TellMsg> msgs = new Vector<TellMsg>(1);
		for(final PlayerStats.TellMsg M : getGTellStack())
		{
			if(((sinceTime == null)||(M.time()>=sinceTime.longValue()))
			&&((fromName==null)||(M.from().equalsIgnoreCase(fromName)))
			&&((toName==null)||(M.to().equalsIgnoreCase(toName))))
				msgs.add(M);
		}
		return msgs;
	}

	@Override
	public List<TellMsg> getGTellStack()
	{
		return new ReadOnlyList<TellMsg>(gtellStack);
	}

	@Override
	public Set<String> getFriends()
	{
		if(account != null)
			return account.getFriends();
		return friends;
	}

	@Override
	public Set<String> getSubscriptions()
	{
		if(account != null)
			return account.getSubscriptions();
		return subscriptions;
	}

	@Override
	public Set<String> getIgnored()
	{
		if(account != null)
			return account.getIgnored();
		return ignored;
	}

	@Override
	public boolean isIgnored(MOB mob)
	{
		if(mob==null)
			return false;
		if(account != null)
			return account.isIgnored(mob);
		if (mob.soulMate() != null)
			mob=mob.soulMate();
		if(ignored.contains(mob.Name()))
			return true;
		final PlayerStats stats=mob.playerStats();
		if(stats ==null)
			return false;
		final PlayerAccount account=stats.getAccount();
		if(account == null)
			return false;
		return ignored.contains(account.getAccountName()+"*");
	}

	@Override
	public boolean isIgnored(final String name)
	{
		if(name==null)
			return false;
		if(account != null)
			return account.isIgnored(name);
		return (ignored.contains(name) || ignored.contains(name+"*"));
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
	public String getAlias(final String named)
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
	public void delAliasName(final String named)
	{
		alias.remove(named.toUpperCase().trim());
	}

	@Override
	public void setAlias(final String named, final String value)
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
	public boolean getTitleRandom(final String title, final Boolean changeTF)
	{
		for(final Iterator<Title> t = titles.iterator();t.hasNext();)
		{
			final Title T = t.next();
			if(T.s.equals(title))
			{
				if(changeTF != null)
					T.r = changeTF.booleanValue();
				return T.r;
			}
		}
		return false;
	}

	@Override
	public String getActiveTitle()
	{
		if(titles.size()==0)
			return "*";
		synchronized(this)
		{
			final String oActiveTitle=actTitle;
			if(oActiveTitle != null)
				return oActiveTitle;
		}

		final String s=titles.get(0).s;
		if((s.length()>2)&&(s.charAt(0)=='{')&&(s.charAt(s.length()-1)=='}'))
			this.actTitle = s.substring(1,s.length()-1);
		else
		if((titles.size()==1)
		||(s.equals("*"))
		||(!s.endsWith("*"))
		||(titles.get(1).s.length()==0)
		||(titles.get(1).s.equals("*"))
		||(!titles.get(1).s.startsWith("*")))
			this.actTitle = s;
		else
			this.actTitle=s.substring(0,s.length()-1)+titles.get(1).s;
		return this.actTitle;
	}

	@Override
	public List<String> getTitles()
	{
		return new ConvertingList<Title,String>(titles,titleConverter);
	}

	@Override
	public boolean delTitle(final String s)
	{
		synchronized(titles)
		{
			if((titles.size()==0)||(s.equals("*")))
				return false;
			for(final Iterator<Title> t = titles.iterator();t.hasNext();)
			{
				final Title T = t.next();
				if(T.s.equals(s))
				{
					this.actTitle = null;
					titles.remove(T);
					return true;
				}
			}
			for(final Iterator<Title> i=titles.iterator();i.hasNext();)
			{
				final Title s1=i.next();
				if(s1.s.equalsIgnoreCase(s))
				{
					this.actTitle = null;
					titles.remove(s1);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void addTitle(final String s)
	{
		synchronized(titles)
		{
			if(titles.size()==0)
			{
				if(!s.equals("*"))
					titles.add(new Title("*"));
				titles.add(new Title(s));
				this.actTitle=null;
			}
			else
			{
				final int oldTitleSize = titles.size();
				for(int i=0;i<titles.size();i++)
				{
					final Title t1 = titles.get(i);
					final String s1 = t1.s;
					if(s1.equalsIgnoreCase(s))
					{
						if(i==0)
							return; // nothing changed!
						this.actTitle = null;
						titles.remove(i);
						if(s.endsWith("*"))
							titles.add(0, t1);
						else
						if(s.startsWith("*")
						&&(s.length()>1)
						&&(titles.get(0).s.endsWith("*"))
						&&(titles.get(0).s.length()>1))
							titles.add(1, t1);
						else
							titles.add(0, t1);
						if(titles.size()!=oldTitleSize)
							Log.errOut("DefaultPlayerStats", titles.size()+"!="+oldTitleSize);
						return;
					}
				}
				titles.add(new Title(s));
				this.actTitle = null;
			}
		}
	}

	private String getTitleXML()
	{
		if(titles.size()==0)
			return "";
		for(int t=titles.size()-1;t>=0;t--)
		{
			final String s=titles.get(t).s;
			if(s.length()==0)
				titles.remove(t);
		}
		final StringBuilder str=new StringBuilder("");
		for(int t=0;t<titles.size();t++)
		{
			final Title T=titles.get(t);
			final String titleXMLStr = CMLib.xml().parseOutAngleBrackets(CMLib.coffeeFilter().safetyInFilter(T.s));
			str.append("<TITLE").append(T.r?" RAND=1":"").append(">").append(titleXMLStr).append("</TITLE>");
		}
		return str.toString();
	}

	@Override
	public String getDeathPoof()
	{
		return deathPoof;
	}

	@Override
	public void setDeathPoof(final String poof)
	{
		if(poof != null)
			this.deathPoof=poof.trim();
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
	public TimeClock getBirthdayClock(final TimeClock clock)
	{
		if(birthdayClock == null)
		{
			birthdayClock = (TimeClock)clock.copyOf();
			birthdayClock.setYear(birthday[2]);
			birthdayClock.setMonth(birthday[1]);
			birthdayClock.setDayOfMonth(birthday[0]);
		}
		return birthdayClock;
	}

	@Override
	public int initializeBirthday(TimeClock clock, int ageHours, final Race R)
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
		final String subscriptionStr=getPrivateList(getSubscriptions());
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
		for(final Iterator<Tracker> i=achievementers.values().iterator();i.hasNext();)
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
			+((subscriptionStr.length()>0)?"<SUBSCRIPTIONS>"+subscriptionStr+"</SUBSCRIPTIONS>":"")
			+"<WRAP>"+wrap+"</WRAP>"
			+"<THEME>"+theme+"</THEME>"
			+"<PAGEBREAK>"+pageBreak+"</PAGEBREAK>"
			+((account!=null)?("<ACCOUNT>"+account.getAccountName()+"</ACCOUNT>"):"")
			+getTitleXML()
			+getAliasXML()
			+getLegacyXML()
			+"<ACCTEXP>"+accountExpires+"</ACCTEXP>"
			+((birthday!=null)?"<BIRTHDAY>"+CMParms.toListString(birthday)+"</BIRTHDAY>":"")
			+((deathPoof.length()>0)?"<DEATHPOOF>"+CMLib.xml().parseOutAngleBrackets(deathPoof)+"</DEATHPOOF>":"")
			+((poofin.length()>0)?"<POOFIN>"+CMLib.xml().parseOutAngleBrackets(poofin)+"</POOFIN>":"")
			+((notes.length()>0)?"<NOTES>"+CMLib.xml().parseOutAngleBrackets(notes)+"</NOTES>":"")
			+((poofout.length()>0)?"<POOFOUT>"+CMLib.xml().parseOutAngleBrackets(poofout)+"</POOFOUT>":"")
			+((announceMsg.length()>0)?"<ANNOUNCE>"+CMLib.xml().parseOutAngleBrackets(announceMsg)+"</ANNOUNCE>":"")
			+((savedPose.length()>0)?"<POSE>"+CMLib.xml().parseOutAngleBrackets(savedPose)+"</POSE>":"")
			+((savedPose.length()>0)?"<POSECONST>"+poseConstant+"</POSECONST>":"")
			+((tranpoofin.length()>0)?"<TRANPOOFIN>"+CMLib.xml().parseOutAngleBrackets(tranpoofin)+"</TRANPOOFIN>":"")
			+((tranpoofout.length()>0)?"<TRANPOOFOUT>"+CMLib.xml().parseOutAngleBrackets(tranpoofout)+"</TRANPOOFOUT>":"")
			+"<DATES>"+CMLib.xml().parseOutAngleBrackets(this.getLevelDateTimesStr())+"</DATES>"
			+"<SECGRPS>"+CMLib.xml().parseOutAngleBrackets(getSetSecurityFlags(null))+"</SECGRPS>"
			+"<AUTOINVSET>"+CMLib.xml().parseOutAngleBrackets(getStat("AUTOINVSET"))+"</AUTOINVSET>"
			+"<XP RP="+this.rolePlayXP+" MAXRP="+this.maxRolePlayXP+" DEF="+this.deferredXP+" MAXDEF="+this.maxDeferredXP+" />"
			+"<LASTXPMILLIS>"+this.lastXPDateTime+"</LASTXPMILLIS>"
			+"<NUMDEATHS>"+this.deathCounter+"</NUMDEATHS>"
			+((playFlags.size()>0)?"<FLAGS>"+this.getStat("FLAGS")+"</FLAGS>":"")
			+roomSet().xml()
			+rest.toString();
	}

	private void setBirthday(final String bday)
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

	private void setAliasXML(final List<XMLTag> xml)
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

	private void setLegacyXML(final List<XMLTag> xml)
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

	private void setTitleXML(final List<XMLTag> xml)
	{
		titles.clear();
		final XMLLibrary xmlLib = CMLib.xml();
		for (final XMLTag piece : xml)
		{
			if(piece.tag().equals("TITLE"))
			{
				final Title T = new Title(xmlLib.restoreAngleBrackets(piece.value()));
				if(piece.parms().containsKey("RAND"))
					T.r=true;
				titles.add(T);
			}
		}
		int t=-1;
		while((++t)>=0)
		{
			final XMLLibrary.XMLTag tag = xmlLib.getPieceFromPieces(xml,"TITLE"+t);
			if(tag == null)
				break;
			final String title=xmlLib.restoreAngleBrackets(tag.value());
			if(title.length()==0)
				break;
			final Title T = new Title(title);
			if(tag.parms().containsKey("RAND"))
				T.r=true;
			titles.add(T);
		}
		this.actTitle = null;
	}

	@Override
	public Map<String, AbilityMapping> getExtraQualifiedSkills()
	{
		return ableMap;
	}

	protected void addBlankLevelInfo()
	{
		final LevelInfo info = new LevelInfo();
		info.level = 0;
		info.time = System.currentTimeMillis();
		info.roomID = "";
		info.mins = 0;
		info.costGains = new int[CostDef.CostType.values().length];
		levelInfo.add(info);
	}

	@Override
	public void setXML(final String xmlStr)
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
		friends.addAll(getHashFrom(str,true));
		str=xmlLib.getValFromPieces(xml,"SUBSCRIPTIONS");
		if(debug)
			Log.debugOut("SUBSCRIPTIONS="+str);
		subscriptions.clear();
		subscriptions.addAll(getHashFrom(str,false));
		final List<String> addThese=new ArrayList<String>();
		for(final String s : subscriptions)
		{
			if((s.length()>3)
			&&(Character.isLetter(s.charAt(0)))
			&&(s.charAt(1)==' ')
			&&(s.charAt(2)==':'))
				addThese.add(s);
		}
		for(final String s : addThese)
		{
			subscriptions.remove(s);
			subscriptions.add(" "+s);
		}
		str=xmlLib.getValFromPieces(xml,"IGNORED");
		if(debug)
			Log.debugOut("IGNORED="+str);
		ignored.clear();
		ignored.addAll(getHashFrom(str,true));
		str=xmlLib.getValFromPieces(xml,"INTROS");
		if(debug)
			Log.debugOut("INTROS="+str);
		introductions.clear();
		introductions.addAll(getHashFrom(str,true));
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
		if(xmlLib.getValFromPieces(xml, "SECGRPS", null) == null)
			Log.debugOut("DefaultPlayerStats","MISSING SECGRPS XML="+xmlStr);
		str=xmlLib.getValFromPieces(xml,"SECGRPS");
		str=xmlLib.restoreAngleBrackets(str);
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

		deathPoof=xmlLib.getValFromPieces(xml,"DEATHPOOF");
		if(debug)
			Log.debugOut("POOFIN="+deathPoof);
		if(deathPoof==null)
			deathPoof="";
		deathPoof=xmlLib.restoreAngleBrackets(deathPoof);

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
		if(savedPose.length()>0)
		{
			final String c = xmlLib.getValFromPieces(xml, "POSECONST");
			poseConstant = (c.length()>0) ? CMath.s_bool(c) : true;
		}

		notes=xmlLib.getValFromPieces(xml,"NOTES");
		if(debug)
			Log.debugOut("NOTES="+notes);
		if(notes==null)
			notes="";
		notes=xmlLib.restoreAngleBrackets(notes);

		this.setStat("FLAGS", xmlLib.getValFromPieces(xml, "FLAGS"));

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
				if(twin.size()<2)
					continue;
				if(CMath.s_int(twin.get(0))>=lastNum)
				{
					lastNum=CMath.s_int(twin.get(0));
					final LevelInfo info = new LevelInfo();
					info.level = lastNum;
					info.time = CMath.s_long(twin.get(1));
					info.roomID = (twin.size()>2)?twin.get(2):"";
					info.mins = (twin.size()>3)?CMath.s_long(twin.get(3)):0;
					info.costGains = new int[CostDef.CostType.values().length];
					for(int i=4;i<4+info.costGains.length;i++)
					{
						if(twin.size()>i)
							info.costGains[i-4]=CMath.s_int(twin.get(i));
					}
					levelInfo.add(info);
				}
			}
		}
		if(levelInfo.size()==0)
			addBlankLevelInfo();
		str = xmlLib.getValFromPieces(xml,"AREAS");
		if(debug)
			Log.debugOut("AREAS="+str);
		if(str!=null)
			roomSet().parseXML("<AREAS>"+str+"</AREAS>");
		else
			roomSet().parseXML("<AREAS />");
		final XMLTag achievePiece = xmlLib.getPieceFromPieces(xml, "ACHIEVEMENTS");
		achievementers.clear();
		for(final Enumeration<Achievement> a=CMLib.achievements().achievements(Agent.PLAYER);a.hasMoreElements();)
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

		final XMLLibrary.XMLTag xpPiece = CMLib.xml().getPieceFromPieces(xml, "XP");
		if(xpPiece != null)
		{
			this.maxRolePlayXP = CMath.s_int(xpPiece.getParmValue("MAXRP"));
			this.rolePlayXP = CMath.s_int(xpPiece.getParmValue("RP"));
			this.maxDeferredXP = CMath.s_int(xpPiece.getParmValue("MAXDEF"));
			this.deferredXP = CMath.s_int(xpPiece.getParmValue("DEF"));
		}
		this.lastXPDateTime=CMath.s_long(xmlLib.getValFromPieces(xml, "LASTXPMILLIS"));
		this.deathCounter=CMath.s_int(xmlLib.getValFromPieces(xml, "NUMDEATHS"));
	}

	private String getLevelDateTimesStr()
	{
		if(levelInfo.size()==0)
			addBlankLevelInfo();
		final StringBuilder buf=new StringBuilder("");
		for(final LevelInfo info : levelInfo)
		{
			buf.append(info.level).append(",")
				.append(info.time).append(",")
				.append(info.roomID).append(",")
				.append(info.mins).append(",")
				.append(CMParms.toTightListString(info.costGains)).append(";");
		}
		return buf.toString();
	}

	@Override
	public String getSetSecurityFlags(final String newFlags)
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
	public void setPoofs(final String poofIn, final String poofOut, final String tranPoofIn, final String tranPoofOut)
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
	public void setHygiene(final long newVal)
	{
		hygiene=newVal;
	}

	@Override
	public boolean adjHygiene(final long byThisMuch)
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
	public void setAccountExpiration(final long newVal)
	{
		if(account != null)
			account.setAccountExpiration(newVal);
		accountExpires=newVal;
	}

	@Override
	public boolean addRoomVisit(final Room R)
	{
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))
		&&(R!=null)
		&&(!CMath.bset(R.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNEXPLORABLE))
		&&(R.getArea()!=null)
		&&(!hasVisited(R)))
		{
			if((R.getArea() instanceof SubArea)||(R.getArea() instanceof AutoGenArea))
				tempRoomSet().add(CMLib.map().getExtendedRoomID(R));
			else
				roomSet().add(CMLib.map().getExtendedRoomID(R));
			return true;
		}
		return false;
	}

	@Override
	public boolean hasVisited(final Room R)
	{
		final String roomID=CMLib.map().getExtendedRoomID(R);
		return roomSet().contains(roomID) || tempRoomSet().contains(roomID);
	}

	@Override
	public boolean hasVisited(final Area A)
	{
		final int numRooms=A.getIStat(Area.Stats.VISITABLE_ROOMS);
		if(numRooms<=0)
			return true;
		return (roomSet().roomCount(A.Name())>0) || (tempRoomSet().roomCount(A.Name())>0);
	}

	@Override
	public void unVisit(final Room R)
	{
		if(R != null)
		{
			final String roomID=CMLib.map().getExtendedRoomID(R);
			if(roomSet().contains(roomID))
				roomSet().remove(roomID);
			if(tempRoomSet().contains(roomID))
				tempRoomSet().remove(roomID);
		}
	}

	@Override
	public void unVisit(final Area A)
	{
		if(A != null)
		{
			for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
				unVisit(r.nextElement());
		}
	}

	@Override
	public int totalVisitedRooms(final MOB mob, Area A)
	{
		if(A==null)
		{
			int totalVisits=0;
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				A=e.nextElement();
				if((!CMLib.flags().isHidden(A))
				&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
				{
					if(A.getIStat(Area.Stats.VISITABLE_ROOMS)>0)
						totalVisits+=roomSet().roomCount(A.Name()) + tempRoomSet().roomCount(A.Name());
				}
			}
			return totalVisits;
		}
		return roomSet().roomCount(A.Name()) + tempRoomSet().roomCount(A.Name());
	}

	@Override
	public int percentVisited(final MOB mob, Area A)
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
					final int visitable = A.getIStat(Area.Stats.VISITABLE_ROOMS);
					if(visitable>0)
					{
						totalRooms+=visitable;
						totalVisits+=roomSet().roomCount(A.Name()) + tempRoomSet().roomCount(A.Name());
					}
				}
			}
			if(totalRooms==0)
				return 100;
			final double pct=CMath.div(totalVisits,totalRooms);
			return (int)Math.round(100.0*pct);
		}
		final int numRooms=A.getIStat(Area.Stats.VISITABLE_ROOMS);
		if(numRooms<=0)
			return 100;
		final double pct=CMath.div(roomSet().roomCount(A.Name()) + tempRoomSet().roomCount(A.Name()),numRooms);
		return (int)Math.round(100.0*pct);
	}

	@Override
	public void killAchievementTracker(final Achievement A, final Tattooable tracked, final MOB mob)
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
	public Tracker getAchievementTracker(final Achievement A, final Tattooable tracked, final MOB mob)
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
	public void rebuildAchievementTracker(final Tattooable tracked, final MOB mob, final String achievementTattoo)
	{
		final Achievement A=CMLib.achievements().getAchievement(achievementTattoo);
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

	protected LevelInfo getLevelInfo(final int level)
	{
		if(levelInfo.size()==0)
			addBlankLevelInfo();
		LevelInfo lowest = levelInfo.get(0);
		for(final LevelInfo info : levelInfo)
		{
			if(info.level==level)
				return info;
			if(level < info.level)
				return lowest;
			lowest = info;
		}
		return lowest;
	}

	@Override
	public long leveledDateTime(final int level)
	{
		return getLevelInfo(level).time;
	}

	@Override
	public String leveledRoomID(final int level)
	{
		return getLevelInfo(level).roomID;
	}

	@Override
	public long leveledMinutesPlayed(final int level)
	{
		return getLevelInfo(level-1).mins;
	}

	@Override
	public int[] leveledCostGains(final int level)
	{
		return getLevelInfo(level).costGains;
	}

	@Override
	public void recordLevelData(final int level, final long ageHours, final Room R, final int[] costGains)
	{
		if(levelInfo.size()==0)
			addBlankLevelInfo();
		long lastTime=0;
		for(int l=0;l<levelInfo.size();l++)
		{
			final LevelInfo info = levelInfo.get(l);
			if(level==info.level)
			{
				info.time = System.currentTimeMillis();
				info.roomID = CMLib.map().getExtendedRoomID(R);
				info.mins = ageHours;
				info.costGains = costGains;
				return;
			}
			else
			if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_SECOND)
				return;
			else
			if(level<info.level)
			{
				final LevelInfo newInfo = new LevelInfo();
				newInfo.level = level;
				newInfo.time = System.currentTimeMillis();
				newInfo.roomID = CMLib.map().getExtendedRoomID(R);
				newInfo.mins = ageHours;
				newInfo.costGains = costGains;
				levelInfo.add(newInfo);
				return;
			}
			lastTime=info.time;
		}
		if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_SECOND)
			return;
		final LevelInfo newInfo = new LevelInfo();
		newInfo.level = level;
		newInfo.time = System.currentTimeMillis();
		newInfo.roomID = CMLib.map().getExtendedRoomID(R);
		newInfo.mins = ageHours;
		newInfo.costGains = costGains;
		levelInfo.add(newInfo);
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
	public void addLegacyLevel(final String category)
	{
		final Integer level=legacy.get(category);
		if(level != null)
			legacy.put(category, Integer.valueOf(level.intValue()+1));
		else
			legacy.put(category, Integer.valueOf(1));
	}

	@Override
	public int getLegacyLevel(final String category)
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
	public void setAccount(final PlayerAccount account)
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
	public void setBonusCharStatPoints(final int bonus)
	{
		this.bonusCharStatPt = bonus;
	}

	@Override
	public int getBonusCommonSkillLimits()
	{
		return this.bonusCommonSk;
	}

	@Override
	public void setBonusCommonSkillLimits(final int bonus)
	{
		this.bonusCommonSk = bonus;
	}

	@Override
	public int getBonusCraftingSkillLimits()
	{
		return this.bonusCraftSk;
	}

	@Override
	public void setBonusCraftingSkillLimits(final int bonus)
	{
		this.bonusCraftSk = bonus;
	}

	@Override
	public int getBonusNonCraftingSkillLimits()
	{
		return this.bonusNonCraftSk;
	}

	@Override
	public void setBonusNonCraftingSkillLimits(final int bonus)
	{
		this.bonusNonCraftSk = bonus;
	}

	@Override
	public int getBonusLanguageLimits()
	{
		return this.bonusLanguages;
	}

	@Override
	public void setBonusLanguageLimits(final int bonus)
	{
		this.bonusLanguages = bonus;
	}

	@Override
	public int getMaxRolePlayXP()
	{
		return this.maxRolePlayXP;
	}

	@Override
	public void setMaxRolePlayXP(final int amt)
	{
		this.maxRolePlayXP = amt;
	}

	@Override
	public int getRolePlayXP()
	{
		return this.rolePlayXP;
	}

	@Override
	public void setRolePlayXP(final int amt)
	{
		if(amt < 0)
			return;
		if(amt > this.getMaxRolePlayXP())
			this.rolePlayXP = this.getMaxRolePlayXP();
		else
			this.rolePlayXP = amt;
	}

	@Override
	public long getLastRolePlayXPTime()
	{
		return this.lastRolePlayTm;
	}

	@Override
	public void setLastRolePlayXPTime(final long tm)
	{
		this.lastRolePlayTm = tm;
	}

	@Override
	public int getMaxDeferredXP()
	{
		return this.maxDeferredXP;
	}

	@Override
	public void setMaxDeferredXP(final int amt)
	{
		this.maxDeferredXP = amt;
	}

	@Override
	public int getDeferredXP()
	{
		return this.deferredXP;
	}

	@Override
	public void setDeferredXP(final int amt)
	{
		if(amt > getMaxDeferredXP())
			this.deferredXP = this.getMaxDeferredXP();
		else
			this.deferredXP = amt;
	}

	@Override
	public long getLastXPAwardMillis()
	{
		return this.lastXPDateTime;
	}

	@Override
	public void setLastXPAwardMillis(final long time)
	{
		this.lastXPDateTime = time;
	}

	@Override
	public synchronized int deathCounter(final int bump)
	{
		deathCounter += bump;
		return deathCounter;
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
									 "BONUSCHARSTATS","AUTOINVSET",
									 "MAXRPXP","CURRRPXP",
									 "MAXDEFXP","CURRDEFXP",
									 "LASTXPAWARD","FLAGS","SUBSCRIPTIONS",
									 "COMBATSTATS","DEATHS"};

	@Override
	public String getStat(final String code)
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
		case 30:
			return ""+this.maxRolePlayXP;
		case 31:
			return ""+this.rolePlayXP;
		case 32:
			return ""+this.maxDeferredXP;
		case 33:
			return ""+this.deferredXP;
		case 34:
			return ""+this.lastXPDateTime;
		case 35:
			return CMParms.toListString(playFlags);
		case 36:
			return getPrivateList(getSubscriptions());
		case 37:
		{
			final StringBuilder str=new StringBuilder("");
			if(combatStats.length>0)
			{
				for(final long[] list : this.combatStats)
				{
					if(list != null)
						str.append(CMParms.toTightListString(list));
					str.append(';');
				}
				while((str.length()>0)
				&&(str.charAt(str.length()-1)==';'))
					str.deleteCharAt(str.length()-1);
			}
			return str.toString();
		}
		case 38:
			return Integer.toString(this.deathCounter);
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			break;
		case 1:
		{
			friends.clear();
			friends.addAll(getHashFrom(val,true));
			break;
		}
		case 2:
		{
			ignored.clear();
			ignored.addAll(getHashFrom(val,true));
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
			introductions.addAll(getHashFrom(val,true));
			break;
		}
		case 20:
			pageBreak = CMath.s_parseIntExpression(val);
			break;
		case 21:
			if(val == null)
				savedPose = "";
			else
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
		case 30:
			this.maxRolePlayXP = CMath.s_parseIntExpression(val);
			break;
		case 31:
			this.setRolePlayXP(CMath.s_parseIntExpression(val));
			break;
		case 32:
			this.maxDeferredXP = CMath.s_parseIntExpression(val);
			break;
		case 33:
			this.setDeferredXP(CMath.s_parseIntExpression(val));
			break;
		case 34:
			this.lastXPDateTime = CMath.s_parseLongExpression(val);
			break;
		case 35:
		{
			playFlags = new SHashSet<PlayerFlag>();
			for(final String s : CMParms.parseCommas(val.toUpperCase(),true))
			{
				final PlayerFlag flag = (PlayerFlag)CMath.s_valueOf(PlayerFlag.class, s);
				if(flag != null)
					playFlags.add(flag);
			}
			break;
		}
		case 36:
		{
			subscriptions.clear();
			subscriptions.addAll(getHashFrom(val,false));
			final List<String> addThese=new ArrayList<String>();
			for(final String s : subscriptions)
			{
				if((s.length()>3)
				&&(Character.isLetter(s.charAt(0)))
				&&(s.charAt(1)==' ')
				&&(s.charAt(2)==':'))
					addThese.add(s);
			}
			for(final String s : addThese)
			{
				subscriptions.remove(s);
				subscriptions.add(" "+s);
			}
			break;
		}
		case 37:
		{
			final List<String> lines = CMParms.parseSemicolons(val, false);
			while((lines.size()>0)
			&& (lines.get(lines.size()-1).trim().length()==0))
				lines.remove(lines.size()-1);
			combatStats=new long[lines.size()][PlayerCombatStat.values().length];
			for(int level = 0; level<combatStats.length; level++)
			{
				final long[] levelLine = CMParms.parseLongList(lines.get(level), ',');
				if(levelLine.length < PlayerCombatStat.values().length)
					combatStats[level] = Arrays.copyOf(levelLine, PlayerCombatStat.values().length);
				else
					combatStats[level] = levelLine;
			}
			break;

		}
		case 38:
			this.deathCounter = CMath.s_parseIntExpression(val);
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	public boolean isOnAutoInvokeList(final String abilityID)
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
	public void addAutoInvokeList(final String abilityID)
	{
		this.autoInvokeSet.add(abilityID);
	}

	@Override
	public void removeAutoInvokeList(final String abilityID)
	{
		this.autoInvokeSet.remove(abilityID);
	}

	@Override
	public long bumpLevelCombatStat(final PlayerCombatStat stat, final int level, final int amt)
	{
		if((stat == null)
		|| (level <= 0)
		|| (CMSecurity.isDisabled(CMSecurity.DisFlag.COMBATSTATS))
		|| (level >= CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)+10))
			return 0;
		if(level > combatStats.length)
			combatStats = Arrays.copyOf(combatStats, level);
		if(combatStats[level-1] == null)
		{
			combatStats[level-1] = new long[PlayerCombatStat.values().length];
			combatStats[level-1][PlayerCombatStat.STATS_LEVEL.ordinal()] = level;
		}
		if((amt > 0)
		&&(amt < Integer.MAX_VALUE/2))
		{
			if(Long.MAX_VALUE-combatStats[level-1][stat.ordinal()] > amt)
				combatStats[level-1][stat.ordinal()] += amt;
		}
		return combatStats[level-1][stat.ordinal()];
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
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean sameAs(final PlayerStats E)
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
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void destroy()
	{
		friends.clear();
		subscriptions.clear();
		ignored.clear();
		tellStack.clear();
		gtellStack.clear();
		titles.clear();
		autoInvokeSet.clear();
		account = null;
		visitedRoomSet	= null;
		tVisitedRoomSet	= null;
		introductions.clear();
		extItems.delAllItems(true);
		achievementers.clear();
		alias.clear();
		legacy.clear();
		combatSpams.clear();
		ableMap.clear();
		experMap.clear();
		levelInfo.clear();
		combatStats=new long[0][PlayerCombatStat.values().length];
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
	public void setSavable(final boolean truefalse)
	{
		synchronized(this)
		{
			isSavable=truefalse;
		}
	}
}
