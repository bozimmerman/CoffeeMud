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
   Copyright 2000-2012 Bo Zimmerman

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
	public String ID(){return "DefaultPlayerAccount";}
	public String name() { return ID();}

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
	
	protected SVector<PlayerLibrary.ThinPlayer> thinPlayers = new SVector<PlayerLibrary.ThinPlayer>();

	public DefaultPlayerAccount() {
		super();
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	
	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","LASTIP","LASTDATETIME",
									 "NOTES","ACCTEXPIRATION","FLAGS","EMAIL"};
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
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: break;
		case 1: friends=getHashFrom(val); break;
		case 2: ignored=getHashFrom(val); break;
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
	public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	private static String[] codes=null;
	public String[] getStatCodes(){
		if(codes==null)
			codes=CMProps.getStatCodesList(CODES,this);
		return codes;
	}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(PlayerAccount E)
	{
		if(!(E instanceof DefaultPlayerAccount)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
	
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultPlayerStats();}}
	public void initializeClass(){}
	public CMObject copyOf()
	{
		try
		{
			DefaultPlayerAccount O=(DefaultPlayerAccount)this.clone();
			O.friends=friends.copyOf();
			O.ignored=ignored.copyOf();
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			return O;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultPlayerStats();
		}
	}
	public String lastIP(){return lastIP;}
	public void setLastIP(String ip){lastIP=ip;}
	public String getEmail(){if(email==null) return ""; return email;}
	public void setEmail(String newAdd){email=newAdd;}
	public long lastUpdated(){return lastUpdated;}
	public void setLastUpdated(long time){lastUpdated=time;}
	public long lastDateTime(){return lastDateTime;}
	public void setLastDateTime(long C){ lastDateTime=C;}
	public String getPasswordStr(){return password;}
	public void setPassword(String newPassword)
	{
		if(CMProps.getBoolVar(CMProps.SYSTEMB_HASHPASSWORDS)
		&&(!CMLib.encoder().isARandomHashString(newPassword)))
			password=CMLib.encoder().makeRandomHashString(newPassword);
		else
			password=newPassword;
	}
	public boolean matchesPassword(String checkPass)
	{
		if(CMLib.encoder().isARandomHashString(password))
			return CMLib.encoder().checkAgainstRandomHashString(checkPass, password);
		return checkPass.equalsIgnoreCase(password);
	}
	public String notes(){return notes;}
	public void setNotes(String newnotes){notes=newnotes;}
	public SHashSet<String> getHashFrom(String str)
	{
		SHashSet<String> h=new SHashSet<String>();
		int x=str.indexOf(';');
		while(x>=0)
		{
			String fi=str.substring(0,x).trim();
			if(fi.length()>0) h.add(fi);
			str=str.substring(x+1);
			x=str.indexOf(';');
		}
		if(str.trim().length()>0)
			h.add(str.trim());
		return h;
	}

	public Set<String> getFriends(){return friends;}
	public Set<String> getIgnored(){return ignored;}

	public MOB getAccountMob()
	{
		if(fakePlayerM!=null)
			return fakePlayerM;
		synchronized(this)
		{
			if(fakePlayerM!=null)
				return fakePlayerM;
			fakePlayerM=CMClass.getMOB("StdMOB");
			fakePlayerM.setName(accountName());
			fakePlayerM.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
			fakePlayerM.playerStats().setAccount(this);
			fakePlayerM.basePhyStats().setLevel(1);
			fakePlayerM.recoverPhyStats();
			return fakePlayerM;
		}
	}
	
	protected String getPrivateList(Set<String> h)
	{
		if((h==null)||(h.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator<String> e=h.iterator();e.hasNext();)
			list.append(((String)e.next())+";");
		return list.toString();
	}
	
	public String getXML()
	{
		StringBuffer rest=new StringBuffer("");
		String[] codes=getStatCodes();
		XMLLibrary libXML = CMLib.xml();
		for(int x=0;x<codes.length;x++)
		{
			String code=codes[x].toUpperCase();
			String value = getStat(code);
			if(value.length()==0)
				rest.append("<"+code+" />");
			else
				rest.append("<"+code+">"+libXML.parseOutAngleBrackets(value)+"</"+code+">");
		}
		return rest.toString();
	}

	public void setXML(String str)
	{
		List<XMLLibrary.XMLpiece> xml = CMLib.xml().parseAllXML(str);
		XMLLibrary libXML = CMLib.xml();
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			String val=libXML.getValFromPieces(xml,codes[i].toUpperCase());
			if(val==null) val="";
			setStat(codes[i].toUpperCase(),libXML.restoreAngleBrackets(val));
		}
	}


	// Acct Expire Code
	public long getAccountExpiration() {return accountExpiration;}
	public void setAccountExpiration(long newVal){accountExpiration=newVal;}
	
	public String accountName() { return accountName;}
	public void setAccountName(String name) { accountName = name;}
	
	public void addNewPlayer(MOB mob) 
	{
		if(players.contains(mob.Name()))
			return;
		if(mob==fakePlayerM)
			return;
		try
		{
			for(String name : players)
				if(name.equalsIgnoreCase(mob.Name()))
					return;
		}
		catch(Exception e) {}
		players.add(mob.Name());
		thinPlayers.clear();
	}
	
	public String findPlayer(String name)
	{
		if(name==null) return null;
		for(final String pName : players)
			if(pName.equalsIgnoreCase(name))
				return pName;
		return null;
	}
	
	public void delPlayer(String name) 
	{
		players.remove(name);
		try
		{
			for(String name1 : players)
				if(name1.equalsIgnoreCase(name))
					players.remove(name1);
		}
		catch(Exception e) {}
		thinPlayers.clear();
	}
	
	public void delPlayer(MOB mob) 
	{
		if(mob==fakePlayerM)
			return;
		players.remove(mob.Name());
		try
		{
			for(String name : players)
				if(name.equalsIgnoreCase(mob.Name()))
					players.remove(name);
		}
		catch(Exception e) {}
		thinPlayers.clear();
	}
	public Enumeration<MOB> getLoadPlayers() 
	{
		Vector<MOB> mobs = new Vector<MOB>(players.size());
		for(Enumeration<String> e=getPlayers();e.hasMoreElements();)
		{
			MOB M=CMLib.players().getLoadPlayer(e.nextElement());
			if(M!=null) mobs.addElement(M);
		}
		return mobs.elements();
	}
	public Enumeration<PlayerLibrary.ThinPlayer> getThinPlayers() 
	{
		synchronized(thinPlayers)
		{
			if(thinPlayers.size() != players.size())
				for(Enumeration<String> e=getPlayers();e.hasMoreElements();)
				{
					String name = e.nextElement();
					PlayerLibrary.ThinPlayer tP = CMLib.database().getThinUser(name);
					if(tP==null){ tP=new PlayerLibrary.ThinPlayer(); tP.name = name;}
					thinPlayers.add(tP);
				}
		}
		return thinPlayers.elements();
	}
	public Enumeration<String> getPlayers() {
		return players.elements();
	}
	public void setPlayerNames(Vector<String> names) {
		if(names != null)
			players = new SVector<String>(names);
	}
	public int numPlayers() { return players.size();}
	public boolean isSet(String flagName) { return acctFlags.contains(flagName.toUpperCase());}
	public void setFlag(String flagName, boolean setOrUnset)
	{
		if(setOrUnset)
			acctFlags.add(flagName.toUpperCase());
		else
			acctFlags.remove(flagName.toUpperCase());
	}
	
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
