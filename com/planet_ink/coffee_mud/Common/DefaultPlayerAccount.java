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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.nio.ByteBuffer;
import java.util.*;

/*
   Copyright 2010-2024 Bo Zimmerman

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
public class DefaultPlayerAccount extends DefaultPrideStats implements PlayerAccount
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
	protected SHashSet<String>	subscriptions		= new SHashSet<String>();
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

	protected SVector<PlayerLibrary.ThinPlayer> thinPlayers 	= new SVector<PlayerLibrary.ThinPlayer>();
	protected Map<String,Tracker>				achievementers	= new STreeMap<String,Tracker>();

	protected final static Filterer<Tattoo> expiringTattooFilter = new Filterer<Tattoo>()
	{
		@Override
		public boolean passesFilter(final Tattoo obj)
		{
			return obj.expirationDate()==0 || (System.currentTimeMillis() < obj.expirationDate());
		}
	};

	protected CMUniqNameSortListWrapper<Tattoo>	tattoos;

	public DefaultPlayerAccount()
	{
		super();
		xtraValues=CMProps.getExtraStatCodesHolder(this);
		final List<Tattoo> tattooPackage = new SVector<Tattoo>(1);
		final FilteredListWrapper<Tattoo> tattWrapper=new FilteredListWrapper<Tattoo>(tattooPackage, expiringTattooFilter);
		tattoos = new CMUniqNameSortListWrapper<Tattoo>(tattWrapper);
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
			final DefaultPlayerAccount O=(DefaultPlayerAccount)this.clone();
			O.friends=friends.copyOf();
			O.subscriptions=subscriptions.copyOf();
			O.ignored=ignored.copyOf();
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			O.thinPlayers = thinPlayers.copyOf();
			O.achievementers = new STreeMap<String,Tracker>();
			for(final String key : achievementers.keySet())
				O.achievementers.put(key, achievementers.get(key).copyOf());
			final List<Tattoo> newTattooPackage = new SVector<Tattoo>(tattoos);
			final FilteredListWrapper<Tattoo> newTattWrapper=new FilteredListWrapper<Tattoo>(newTattooPackage, expiringTattooFilter);
			O.tattoos = new CMUniqNameSortListWrapper<Tattoo>(newTattWrapper);
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
	public void copyInto(final PlayerAccount otherAccount)
	{
		for(final String stat : this.getStatCodes())
			otherAccount.setStat(stat, this.getStat(stat));
		if(otherAccount instanceof DefaultPlayerAccount)
		{
			final DefaultPlayerAccount O = (DefaultPlayerAccount)otherAccount;
			O.friends=friends.copyOf();
			O.subscriptions=subscriptions.copyOf();
			O.ignored=ignored.copyOf();
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			O.thinPlayers = thinPlayers.copyOf();
			O.achievementers = new STreeMap<String,Tracker>(achievementers);
			final List<Tattoo> newTattooPackage = new SVector<Tattoo>(tattoos);
			final FilteredListWrapper<Tattoo> newTattWrapper=new FilteredListWrapper<Tattoo>(newTattooPackage, expiringTattooFilter);
			O.tattoos = new CMUniqNameSortListWrapper<Tattoo>(newTattWrapper);
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
	public void setLastIP(final String ip)
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
	public void setEmail(final String newAdd)
	{
		email=newAdd;
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
	}

	@Override
	public long getLastDateTime()
	{
		return lastDateTime;
	}

	@Override
	public void setLastDateTime(final long C)
	{
		lastDateTime=C;
	}

	@Override
	public String getPasswordStr()
	{
		return password;
	}

	@Override
	public void setPassword(final String newPassword)
	{
		password = CMLib.encoder().makeFinalPasswordString(newPassword);
	}

	@Override
	public boolean matchesPassword(final String checkPass)
	{
		return CMLib.encoder().passwordCheck(checkPass, password);
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

	protected SHashSet<String> getHashFrom(String str, final boolean trim)
	{
		final SHashSet<String> h=new SHashSet<String>();
		int x=str.indexOf(';');
		while(x>=0)
		{
			final String fi=trim?str.substring(0,x).trim():str.substring(0,x);
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
	public Set<String> getSubscriptions()
	{
		return subscriptions;
	}

	@Override
	public boolean isIgnored(MOB mob)
	{
		if(mob==null)
			return false;
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
		return (ignored.contains(name) || ignored.contains(name+"*"));
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
		rest.append(super.getXML());
		rest.append("<ACHIEVEMENTS");
		for(final Iterator<Tracker> i=achievementers.values().iterator();i.hasNext();)
		{
			final Tracker T = i.next();
			if(T.getAchievement().isSavableTracker() && (T.getCount(null) != 0))
				rest.append(" ").append(T.getAchievement().getTattoo()).append("=").append(T.getCountParms(null));
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
	public void setXML(final String str)
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
		super.setXML(xmlLib, xml);
		final XMLTag achievePiece = xmlLib.getPieceFromPieces(xml, "ACHIEVEMENTS");
		achievementers.clear();
		for(final Enumeration<Achievement> a=CMLib.achievements().achievements(Agent.ACCOUNT);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if((achievePiece != null) && achievePiece.parms().containsKey(A.getTattoo()))
				achievementers.put(A.getTattoo(), A.getTracker(achievePiece.parms().get(A.getTattoo()).trim()));
			else
				achievementers.put(A.getTattoo(), A.getTracker("0"));
		}
		final String[] allTattoos=xmlLib.getValFromPieces(xml, "TATTOOS").split(",");
		this.tattoos.clear();
		for(final String tattoo : allTattoos)
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
	public void setAccountExpiration(final long newVal)
	{
		accountExpiration=newVal;
	}

	@Override
	public String getAccountName()
	{
		return accountName;
	}

	@Override
	public void setAccountName(final String name)
	{
		accountName = name;
	}

	@Override
	public void addNewPlayer(final MOB mob)
	{
		if(players.contains(mob.Name()))
			return;
		if(mob==fakePlayerM)
			return;
		try
		{
			for(final String name : players)
			{
				if(name.equalsIgnoreCase(mob.Name()))
					return;
			}
		}
		catch(final Exception e)
		{
		}
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CHARACTERS, 1);
		players.add(mob.Name());
		thinPlayers.clear();
	}

	@Override
	public String findPlayer(final String name)
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
	public void delPlayer(final String name)
	{
		players.remove(name);
		try
		{
			for(final String name1 : players)
			{
				if(name1.equalsIgnoreCase(name))
					players.remove(name1);
			}
			CMLib.achievements().possiblyBumpAchievement(null, AchievementLibrary.Event.CHARACTERS, -1);
		}
		catch(final Exception e)
		{
		}
		thinPlayers.clear();
	}

	@Override
	public void delPlayer(final MOB mob)
	{
		if(mob==fakePlayerM)
			return;
		players.remove(mob.Name());
		try
		{
			for(final String name : players)
			{
				if(name.equalsIgnoreCase(mob.Name()))
					players.remove(name);
			}
		}
		catch(final Exception e)
		{
		}
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CHARACTERS, -1);
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
				for(final Enumeration<String> e=getPlayers();e.hasMoreElements();)
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
								return "";
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

							@Override
							public String liege()
							{
								return "";
							}

							@Override
							public String worship()
							{
								return "";
							}

							@Override
							public String gender()
							{
								return "neuter";
							}

							@Override
							public Enumeration<String> clans()
							{
								return new EmptyEnumeration<String>();
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
	public void setPlayerNames(final List<String> names)
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
	public boolean isSet(final AccountFlag flag)
	{
		return acctFlags.contains(flag);
	}

	@Override
	public void setFlag(final AccountFlag flag, final boolean setOrUnset)
	{
		if(setOrUnset)
			acctFlags.add(flag);
		else
			acctFlags.remove(flag);
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
			T=A.getTracker("0");
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
				achievementers.put(A.getTattoo(), A.getTracker(achievementers.get(A.getTattoo()).getCountParms(mob)));
			else
				achievementers.put(A.getTattoo(), A.getTracker("0"));
		}
		else
			achievementers.remove(achievementTattoo);
	}

	/** Manipulation of the tatoo list */
	@Override
	public void addTattoo(final String of)
	{
		final Tattoo T=(Tattoo)CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of));
	}

	@Override
	public void addTattoo(final String of, final int tickDown)
	{
		final Tattoo T=(Tattoo)CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of,tickDown));
	}

	@Override
	public boolean delTattoo(final String of)
	{
		final Tattoo T=findTattoo(of);
		if(T!=null)
			 return tattoos.remove(T);
		return false;
	}

	@Override
	public void addTattoo(final Tattoo of)
	{
		if ((of == null) || (of.getTattooName() == null) || (of.getTattooName().length() == 0) || findTattoo(of.getTattooName()) != null)
			return;
		tattoos.add(of);
	}

	@Override
	public void delTattoo(final Tattoo of)
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
		return new IteratorEnumeration<Tattoo>(tattoos.iterator());
	}

	@Override
	public Tattoo findTattoo(final String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		return tattoos.find(of.trim());
	}

	@Override
	public Tattoo findTattooStartsWith(final String of)
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
	public int getBonusCharsOnlineLimit()
	{
		return bonusCharOnlineLimit;
	}

	@Override
	public void setBonusCharsOnlineLimit(final int bonus)
	{
		bonusCharOnlineLimit = bonus;
	}

	@Override
	public int getBonusCharsLimit()
	{
		return bonusCharLimit;
	}

	@Override
	public void setBonusCharsLimit(final int bonus)
	{
		bonusCharLimit = bonus;
	}

	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","LASTIP","LASTDATETIME",
									 "NOTES","ACCTEXPIRATION","FLAGS","EMAIL",
									 "BONUSCOMMON", "BONUSCRAFT","BONUSNONCRAFT","BONUSLANGS",
									 "BONUSCHARSTATS", "BONUSCHARLIMIT", "BONUSCHARONLINE",
									 "SUBSCRIPTIONS"};

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
		case 16:
			return getPrivateList(getSubscriptions());
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
			for(final String s : CMParms.parseCommas(val.toUpperCase(),true))
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
		case 16:
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
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(final String code)
	{
		for (int i = 0; i < CODES.length; i++)
		{
			if (code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	public boolean sameAs(final PlayerAccount E)
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
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

}
