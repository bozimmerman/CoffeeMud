package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.nio.ByteBuffer;
import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public class DefaultPlayerAccount implements PlayerAccount
{
	@Override
	public String ID()
	{
		return "DefaultPlayerAccount";
	}

	@Override
	public String name()
	{
		return ID();
	}

	protected SHashSet<String>	friends				= new SHashSet<String>();
	protected SHashSet<String>	ignored				= new SHashSet<String>();
	protected SVector<String>	players				= new SVector<String>();
	protected String			accountName 		= "";
	protected String			lastIP				= "";
	protected long				lastDateTime		= System.currentTimeMillis();
	protected long				lastUpdated			= 0;
	protected String			email				= "";
	protected String			password			= "";
	protected String			notes				= "";
	protected long 				accountExpiration	= 0;
	protected int				bonusCommonSk		= 0;
	protected int				bonusCraftSk		= 0;
	protected int				bonusNonCraftSk		= 0;
	protected int				bonusLanguages		= 0;
	protected int				bonusCharStatPt		= 0;
	protected int				bonusCharLimit		= 0;
	protected int				bonusCharOnlineLimit= 0;
	protected String[]			xtraValues			= null;
	protected Set<AccountFlag>	acctFlags			= new SHashSet<AccountFlag>();
	protected volatile MOB 		fakePlayerM			= null;
	protected long[]			prideExpireTime		= new long[TimeClock.TimePeriod.values().length];
	protected int[][]			prideStats			= new int[TimeClock.TimePeriod.values().length][AccountStats.PrideStat.values().length];

	protected SVector<PlayerLibrary.ThinPlayer> thinPlayers 	= new SVector<PlayerLibrary.ThinPlayer>();
	protected Map<String,Tracker>				achievementers	= new STreeMap<String,Tracker>();
	protected CMUniqNameSortSVec<Tattoo>		tattoos			= new CMUniqNameSortSVec<Tattoo>(1);

	public DefaultPlayerAccount()
	{
		super();
		xtraValues=CMProps.getExtraStatCodesHolder(this);
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
			final DefaultPlayerAccount O=(DefaultPlayerAccount)this.clone();
			O.friends=friends.copyOf();
			O.ignored=ignored.copyOf();
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			O.thinPlayers = thinPlayers.copyOf();
			O.achievementers = new STreeMap<String,Tracker>();
			for(final String key : achievementers.keySet())
				O.achievementers.put(key, achievementers.get(key).copyOf());
			O.tattoos = new CMUniqNameSortSVec<Tattoo>(tattoos);
			O.acctFlags = new SHashSet<AccountFlag>(acctFlags);
			O.fakePlayerM = null;
			return O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultPlayerStats();
		}
	}

	@Override
	public void copyInto(PlayerAccount otherAccount)
	{
		for(String stat : this.getStatCodes())
			otherAccount.setStat(stat, this.getStat(stat));
		if(otherAccount instanceof DefaultPlayerAccount)
		{
			DefaultPlayerAccount O = (DefaultPlayerAccount)otherAccount;
			O.friends=friends.copyOf();
			O.ignored=ignored.copyOf();
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			O.thinPlayers = thinPlayers.copyOf();
			O.achievementers = new STreeMap<String,Tracker>(achievementers);
			O.tattoos = new CMUniqNameSortSVec<Tattoo>(tattoos);
			O.acctFlags = new SHashSet<AccountFlag>(acctFlags);
			O.fakePlayerM = null;
			O.accountName = accountName;
			O.lastIP = lastIP;
			O.lastDateTime = lastDateTime;
			O.lastUpdated = lastUpdated;
			O.email = email;
			O.password = password;
			O.notes = notes;
			O.accountExpiration = accountExpiration;
			O.bonusCommonSk = bonusCommonSk;
			O.bonusCraftSk = bonusCraftSk;
			O.bonusNonCraftSk = bonusNonCraftSk;
			O.bonusLanguages = bonusLanguages;
			O.bonusCharStatPt = bonusCharStatPt;
			O.bonusCharLimit = bonusCharLimit;
			O.bonusCharOnlineLimit= bonusCharOnlineLimit;
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
	}

	@Override
	public String getEmail()
	{
		if(email==null)
			return "";
		return email;
	}

	@Override
	public void setEmail(String newAdd)
	{
		email=newAdd;
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
	}

	@Override
	public long getLastDateTime()
	{
		return lastDateTime;
	}

	@Override
	public void setLastDateTime(long C)
	{
		lastDateTime=C;
	}

	@Override
	public String getPasswordStr()
	{
		return password;
	}

	@Override
	public void setPassword(String newPassword)
	{
		if(CMProps.getBoolVar(CMProps.Bool.HASHPASSWORDS)
		&&(!CMLib.encoder().isARandomHashString(newPassword)))
			password=CMLib.encoder().makeRandomHashString(newPassword);
		else
			password=newPassword;
	}

	@Override
	public boolean matchesPassword(String checkPass)
	{
		return CMLib.encoder().passwordCheck(checkPass, password);
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

	protected SHashSet<String> getHashFrom(String str)
	{
		final SHashSet<String> h=new SHashSet<String>();
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
	public Set<String> getFriends()
	{
		return friends;
	}

	@Override
	public Set<String> getIgnored()
	{
		return ignored;
	}

	@Override
	public void bumpPrideStat(PrideStat stat, int amt)
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
	public int getPrideStat(TimePeriod period, PrideStat stat)
	{
		if((period==null)||(stat==null))
			return 0;
		return prideStats[period.ordinal()][stat.ordinal()];
	}

	@Override
	public MOB getAccountMob()
	{
		if(fakePlayerM!=null)
			return fakePlayerM;
		synchronized(this)
		{
			if(fakePlayerM!=null)
				return fakePlayerM;
			fakePlayerM=CMClass.getMOB("StdMOB");
			fakePlayerM.setName(getAccountName());
			fakePlayerM.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
			fakePlayerM.playerStats().setAccount(this);
			fakePlayerM.playerStats().setEmail(getEmail());
			fakePlayerM.basePhyStats().setLevel(1);
			fakePlayerM.recoverPhyStats();
			return fakePlayerM;
		}
	}

	protected String getPrivateList(Set<String> h)
	{
		if((h==null)||(h.size()==0))
			return "";
		final StringBuffer list=new StringBuffer("");
		for (final String string : h)
			list.append((string)+";");
		return list.toString();
	}

	@Override
	public String getXML()
	{
		final StringBuffer rest=new StringBuffer("");
		final String[] codes=getStatCodes();
		final XMLLibrary libXML = CMLib.xml();
		for (final String code2 : codes)
		{
			final String code=code2.toUpperCase();
			final String value = getStat(code);
			if(value.length()==0)
				rest.append("<"+code+" />");
			else
				rest.append("<"+code+">"+libXML.parseOutAngleBrackets(value)+"</"+code+">");
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
		rest.append("<TATTOOS>").append(CMParms.toListString(tattoos)).append("</TATTOOS>");
		rest.append("<ACCSTATS>")
			.append(bonusCommonSk).append(';')
			.append(bonusCraftSk).append(';')
			.append(bonusNonCraftSk).append(';')
			.append(bonusLanguages).append(';')
			.append(bonusCharStatPt).append(';')
			.append(bonusCharLimit).append(';')
			.append(bonusCharOnlineLimit).append(';')
			.append("</ACCSTATS>");
		return rest.toString();
	}

	@Override
	public void setXML(String str)
	{
		final XMLLibrary xmlLib = CMLib.xml();
		final List<XMLLibrary.XMLTag> xml = xmlLib.parseAllXML(str);
		final String[] codes=getStatCodes();
		for (final String code : codes)
		{
			String val=xmlLib.getValFromPieces(xml,code.toUpperCase());
			if(val==null)
				val="";
			setStat(code.toUpperCase(),xmlLib.restoreAngleBrackets(val));
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
		final XMLTag achievePiece = xmlLib.getPieceFromPieces(xml, "ACHIEVEMENTS");
		achievementers.clear();
		for(Enumeration<Achievement> a=CMLib.achievements().achievements(Agent.ACCOUNT);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if((achievePiece != null) && achievePiece.parms().containsKey(A.getTattoo()))
				achievementers.put(A.getTattoo(), A.getTracker(CMath.s_int(achievePiece.parms().get(A.getTattoo()).trim())));
			else
				achievementers.put(A.getTattoo(), A.getTracker(0));
		}
		final String[] allTattoos=xmlLib.getValFromPieces(xml, "TATTOOS").split(",");
		this.tattoos.clear();
		for(String tattoo : allTattoos)
			this.addTattoo(tattoo);
		
		final String[] allAccStats=xmlLib.getValFromPieces(xml, "ACCSTATS").split(";");
		if(allAccStats.length>=7)
		{
			bonusCommonSk=CMath.s_int(allAccStats[0]);
			bonusCraftSk=CMath.s_int(allAccStats[1]);
			bonusNonCraftSk=CMath.s_int(allAccStats[2]);
			bonusLanguages=CMath.s_int(allAccStats[3]);
			bonusCharStatPt=CMath.s_int(allAccStats[4]);
			bonusCharLimit=CMath.s_int(allAccStats[5]);
			bonusCharOnlineLimit=CMath.s_int(allAccStats[6]);
		}
	}

	// Acct Expire Code
	@Override
	public long getAccountExpiration()
	{
		return accountExpiration;
	}

	@Override
	public void setAccountExpiration(long newVal)
	{
		accountExpiration=newVal;
	}

	@Override
	public String getAccountName()
	{
		return accountName;
	}

	@Override
	public void setAccountName(String name)
	{
		accountName = name;
	}

	@Override
	public void addNewPlayer(MOB mob)
	{
		if(players.contains(mob.Name()))
			return;
		if(mob==fakePlayerM)
			return;
		try
		{
			for(final String name : players)
				if(name.equalsIgnoreCase(mob.Name()))
					return;
		}
		catch(final Exception e)
		{
		}
		players.add(mob.Name());
		thinPlayers.clear();
	}

	@Override
	public String findPlayer(String name)
	{
		if(name==null)
			return null;
		for(final String pName : players)
		{
			if(pName.equalsIgnoreCase(name))
				return pName;
		}
		return null;
	}

	@Override
	public void delPlayer(String name)
	{
		players.remove(name);
		try
		{
			for(final String name1 : players)
				if(name1.equalsIgnoreCase(name))
					players.remove(name1);
		}
		catch(final Exception e)
		{
		}
		thinPlayers.clear();
	}

	@Override
	public void delPlayer(MOB mob)
	{
		if(mob==fakePlayerM)
			return;
		players.remove(mob.Name());
		try
		{
			for(final String name : players)
				if(name.equalsIgnoreCase(mob.Name()))
					players.remove(name);
		}
		catch(final Exception e)
		{
		}
		thinPlayers.clear();
	}

	@Override
	public Enumeration<MOB> getLoadPlayers()
	{
		final Vector<MOB> mobs = new Vector<MOB>(players.size());
		for(final Enumeration<String> e=getPlayers();e.hasMoreElements();)
		{
			final MOB M=CMLib.players().getLoadPlayer(e.nextElement());
			if(M!=null)
				mobs.addElement(M);
		}
		return mobs.elements();
	}

	@Override
	public Enumeration<PlayerLibrary.ThinPlayer> getThinPlayers()
	{
		synchronized(thinPlayers)
		{
			if(thinPlayers.size() != players.size())
			{
				for(Enumeration<String> e=getPlayers();e.hasMoreElements();)
				{
					final String name = e.nextElement();
					PlayerLibrary.ThinPlayer tP = CMLib.database().getThinUser(name);
					if (tP == null)
					{
						tP = new PlayerLibrary.ThinPlayer()
						{
							@Override
							public String name()
							{
								return name;
							}

							@Override
							public String charClass()
							{
								return "";
							}

							@Override
							public String race()
							{
								return "";
							}

							@Override
							public int level()
							{
								return 0;
							}

							@Override
							public int age()
							{
								return 0;
							}

							@Override
							public long last()
							{
								return 0;
							}

							@Override
							public String email()
							{
								return null;
							}

							@Override
							public String ip()
							{
								return "";
							}

							@Override
							public int exp()
							{
								return 0;
							}

							@Override
							public int expLvl()
							{
								return 0;
							}
						};
					}
					thinPlayers.add(tP);
				}
			}
		}
		return thinPlayers.elements();
	}

	@Override
	public Enumeration<String> getPlayers()
	{
		return players.elements();
	}

	@Override
	public void setPlayerNames(Vector<String> names)
	{
		if(names != null)
		{
			players = new SVector<String>(names);
			for(final String name : players)
			{
				final MOB M=CMLib.players().getPlayer(name);
				if((M!=null)&&(M.playerStats()!=null)&&(M.playerStats().getAccount()==null))
					M.playerStats().setAccount(this);
			}
		}
	}

	@Override
	public int numPlayers()
	{
		return players.size();
	}

	@Override
	public boolean isSet(AccountFlag flag)
	{
		return acctFlags.contains(flag);
	}

	@Override
	public void setFlag(AccountFlag flag, boolean setOrUnset)
	{
		if(setOrUnset)
			acctFlags.add(flag);
		else
			acctFlags.remove(flag);
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
	
	/** Manipulation of the tatoo list */
	@Override
	public void addTattoo(String of)
	{
		final Tattoo T=(Tattoo)CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of));
	}

	@Override
	public void addTattoo(String of, int tickDown)
	{
		final Tattoo T=(Tattoo)CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of,tickDown));
	}

	@Override
	public void delTattoo(String of)
	{
		final Tattoo T=findTattoo(of);
		if(T!=null)
			tattoos.remove(T);
	}
	
	@Override
	public void addTattoo(Tattoo of)
	{
		if ((of == null) || (of.getTattooName() == null) || (of.getTattooName().length() == 0) || findTattoo(of.getTattooName()) != null)
			return;
		tattoos.addElement(of);
	}

	@Override
	public void delTattoo(Tattoo of)
	{
		if ((of == null) || (of.getTattooName() == null) || (of.getTattooName().length() == 0))
			return;
		final Tattoo tat = findTattoo(of.getTattooName());
		if (tat == null)
			return;
		tattoos.remove(tat);
	}

	@Override
	public Enumeration<Tattoo> tattoos()
	{
		return tattoos.elements();
	}

	@Override
	public Tattoo findTattoo(String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		return tattoos.find(of.trim());
	}

	@Override
	public Tattoo findTattooStartsWith(String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		return tattoos.findStartsWith(of.trim());
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

	@Override
	public int getBonusCharsOnlineLimit()
	{
		return bonusCharOnlineLimit;
	}

	@Override
	public void setBonusCharsOnlineLimit(int bonus)
	{
		bonusCharOnlineLimit = bonus;
	}

	@Override
	public int getBonusCharsLimit()
	{
		return bonusCharLimit;
	}

	@Override
	public void setBonusCharsLimit(int bonus)
	{
		bonusCharLimit = bonus;
	}

	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","LASTIP","LASTDATETIME",
									 "NOTES","ACCTEXPIRATION","FLAGS","EMAIL",
									 "BONUSCOMMON", "BONUSCRAFT","BONUSNONCRAFT","BONUSLANGS",
									 "BONUSCHARSTATS", "BONUSCHARLIMIT", "BONUSCHARONLINE"};

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
			return lastIP;
		case 4:
			return "" + lastDateTime;
		case 5:
			return notes;
		case 6:
			return "" + accountExpiration;
		case 7:
			return CMParms.toListString(acctFlags);
		case 8:
			return email;
		case 9:
			return "" + bonusCommonSk;
		case 10:
			return "" + bonusCraftSk;
		case 11:
			return "" + bonusNonCraftSk;
		case 12:
			return "" + bonusLanguages;
		case 13:
			return "" + bonusCharStatPt;
		case 14:
			return "" + bonusCharLimit;
		case 15:
			return "" + bonusCharOnlineLimit;
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: break;
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
			lastIP = val;
			break;
		case 4:
			lastDateTime = CMath.s_long(val);
			break;
		case 5:
			notes = val;
			break;
		case 6:
			accountExpiration = CMath.s_long(val);
			break;
		case 7:
		{
			acctFlags = new SHashSet<AccountFlag>();
			for(String s : CMParms.parseCommas(val.toUpperCase(),true))
			{
				final AccountFlag flag = (AccountFlag)CMath.s_valueOf(AccountFlag.class, s);
				if(flag != null)
					acctFlags.add(flag);
			}
			break;
		}
		case 8:
			email = val;
			break;
		case 9:
			bonusCommonSk = CMath.s_parseIntExpression(val);
			break;
		case 10:
			bonusCraftSk = CMath.s_parseIntExpression(val);
			break;
		case 11:
			bonusNonCraftSk = CMath.s_parseIntExpression(val);
			break;
		case 12:
			bonusLanguages = CMath.s_parseIntExpression(val);
			break;
		case 13:
			bonusCharStatPt = CMath.s_parseIntExpression(val);
			break;
		case 14:
			bonusCharLimit = CMath.s_parseIntExpression(val);
			break;
		case 15:
			bonusCharOnlineLimit = CMath.s_parseIntExpression(val);
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if (codes == null)
			codes = CMProps.getStatCodesList(CODES, this);
		return codes;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		for (int i = 0; i < CODES.length; i++)
		{
			if (code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	public boolean sameAs(PlayerAccount E)
	{
		if (!(E instanceof DefaultPlayerAccount))
			return false;
		for (int i = 0; i < getStatCodes().length; i++)
		{
			if (!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		}
		return true;
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

}
