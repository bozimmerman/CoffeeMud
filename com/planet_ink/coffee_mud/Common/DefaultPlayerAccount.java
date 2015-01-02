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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.nio.ByteBuffer;
import java.util.*;

/*
   Copyright 2010-2015 Bo Zimmerman

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
	@Override public String ID(){return "DefaultPlayerAccount";}
	@Override public String name() { return ID();}

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
	protected String[]			xtraValues			= null;
	protected SHashSet<String>	acctFlags			= new SHashSet<String>();
	protected volatile MOB 		fakePlayerM			= null;
	protected long[]			prideExpireTime		= new long[TimeClock.TimePeriod.values().length];
	protected int[][]			prideStats			= new int[TimeClock.TimePeriod.values().length][AccountStats.PrideStat.values().length];

	protected SVector<PlayerLibrary.ThinPlayer> thinPlayers = new SVector<PlayerLibrary.ThinPlayer>();

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

	@Override public void initializeClass(){}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultPlayerAccount O=(DefaultPlayerAccount)this.clone();
			O.friends=friends.copyOf();
			O.ignored=ignored.copyOf();
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
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
		if(CMLib.encoder().isARandomHashString(password))
			return CMLib.encoder().checkAgainstRandomHashString(checkPass, password);
		return checkPass.equalsIgnoreCase(password);
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
		rest.append("<NEXTPRIDEPERIODS>").append(CMParms.toTightStringList(prideExpireTime)).append("</NEXTPRIDEPERIODS>");
		rest.append("<PRIDESTATS>");
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
			rest.append(CMParms.toTightStringList(prideStats[period.ordinal()])).append(";");
		rest.append("</PRIDESTATS>");
		return rest.toString();
	}

	@Override
	public void setXML(String str)
	{
		final List<XMLLibrary.XMLpiece> xml = CMLib.xml().parseAllXML(str);
		final XMLLibrary libXML = CMLib.xml();
		final String[] codes=getStatCodes();
		for (final String code : codes)
		{
			String val=libXML.getValFromPieces(xml,code.toUpperCase());
			if(val==null)
				val="";
			setStat(code.toUpperCase(),libXML.restoreAngleBrackets(val));
		}
		final String[] nextPeriods=libXML.getValFromPieces(xml, "NEXTPRIDEPERIODS").split(",");
		final String[] prideStats=libXML.getValFromPieces(xml, "PRIDESTATS").split(";");
		final Pair<Long,int[]>[] finalPrideStats = CMLib.players().parsePrideStats(nextPeriods, prideStats);
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
			if(period.ordinal()<finalPrideStats.length)
			{
				this.prideExpireTime[period.ordinal()]=finalPrideStats[period.ordinal()].first.longValue();
				this.prideStats[period.ordinal()]=finalPrideStats[period.ordinal()].second;
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
		catch(final Exception e) {}
		players.add(mob.Name());
		thinPlayers.clear();
	}

	@Override
	public String findPlayer(String name)
	{
		if(name==null)
			return null;
		for(final String pName : players)
			if(pName.equalsIgnoreCase(name))
				return pName;
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
		catch(final Exception e) {}
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
		catch(final Exception e) {}
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
				for(final Enumeration<String> e=getPlayers();e.hasMoreElements();)
				{
					final String name = e.nextElement();
					PlayerLibrary.ThinPlayer tP = CMLib.database().getThinUser(name);
					if(tP==null){ tP=new PlayerLibrary.ThinPlayer(); tP.name = name;}
					thinPlayers.add(tP);
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
	public boolean isSet(String flagName)
	{
		return acctFlags.contains(flagName.toUpperCase());
	}

	@Override
	public void setFlag(String flagName, boolean setOrUnset)
	{
		if(setOrUnset)
			acctFlags.add(flagName.toUpperCase());
		else
			acctFlags.remove(flagName.toUpperCase());
	}

	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","LASTIP","LASTDATETIME","NOTES","ACCTEXPIRATION","FLAGS","EMAIL"};

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return getPrivateList(getFriends());
		case 2: return getPrivateList(getIgnored());
		case 3: return lastIP;
		case 4: return ""+lastDateTime;
		case 5: return notes;
		case 6: return ""+accountExpiration;
		case 7: return CMParms.toStringList(acctFlags);
		case 8: return email;
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
		case 1: { friends.clear(); friends.addAll(getHashFrom(val)); break; }
		case 2: { ignored.clear(); ignored.addAll(getHashFrom(val)); break; }
		case 3: lastIP=val; break;
		case 4: lastDateTime=CMath.s_long(val); break;
		case 5: notes=val; break;
		case 6: accountExpiration=CMath.s_long(val); break;
		case 7: acctFlags = new SHashSet<String>(CMParms.parseCommandFlags(val.toUpperCase(),PlayerAccount.FLAG_DESCS)); break;
		case 8: email=val; break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}
	@Override public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	private static String[] codes=null;
	@Override
	public String[] getStatCodes()
	{
		if(codes==null)
			codes=CMProps.getStatCodesList(CODES,this);
		return codes;
	}
	@Override public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		return -1;
	}
	public boolean sameAs(PlayerAccount E)
	{
		if(!(E instanceof DefaultPlayerAccount))
			return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
