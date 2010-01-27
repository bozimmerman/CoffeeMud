package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class DefaultPlayerAccount implements PlayerAccount
{
    public String ID(){return "DefaultPlayerAccount";}
    protected HashSet friends=new HashSet();
    protected HashSet ignored=new HashSet();
    protected Vector<String> players = new Vector<String>();
    protected String accountName = "";
	protected String lastIP="";
    protected long LastDateTime=System.currentTimeMillis();
    protected long lastUpdated=0;
	protected String email="";
	protected String Password="";
    protected String notes="";
    protected long accountExpiration=0;
    protected String[] xtraValues=null;

    public DefaultPlayerAccount() {
        super();
        xtraValues=CMProps.getExtraStatCodesHolder(this);
    }
    
	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","LASTIP","LASTDATETIME",
									 "NOTES","ACCTEXPIRATION"};
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return "<FRIENDS>"+getPrivateList(getFriends())+"</FRIENDS>";
		case 2: return "<IGNORED>"+getPrivateList(getIgnored())+"</IGNORED>";
		case 3: return lastIP;
		case 4: return ""+LastDateTime;
		case 5: return notes;
		case 6: return ""+accountExpiration;
        default:
            return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: break;
		case 1: friends=getHashFrom(CMLib.xml().returnXMLValue(val,"FRIENDS")); break;
		case 2: ignored=getHashFrom(CMLib.xml().returnXMLValue(val,"IGNORED")); break;
		case 3: lastIP=val; break;
		case 4: LastDateTime=CMath.s_long(val); break;
		case 5: notes=val; break;
		case 6: accountExpiration=CMath.s_long(val); break;
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
            O.friends=(HashSet)friends.clone();
            O.ignored=(HashSet)ignored.clone();
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
	public long lastDateTime(){return LastDateTime;}
	public void setLastDateTime(long C){ LastDateTime=C;}
	public String password(){return Password;}
	public void setPassword(String newPassword){Password=newPassword;}
	public String notes(){return notes;}
	public void setNotes(String newnotes){notes=newnotes;}
	public HashSet getHashFrom(String str)
	{
		HashSet h=new HashSet();
		if((str==null)||(str.length()==0)) return h;
		str=CMStrings.replaceAll(str,"<FRIENDS>","");
		str=CMStrings.replaceAll(str,"<IGNORED>","");
		str=CMStrings.replaceAll(str,"</FRIENDS>","");
		str=CMStrings.replaceAll(str,"</IGNORED>","");
		int x=str.indexOf(";");
		while(x>=0)
		{
			String fi=str.substring(0,x).trim();
			if(fi.length()>0) h.add(fi);
			str=str.substring(x+1);
			x=str.indexOf(";");
		}
		if(str.trim().length()>0)
			h.add(str.trim());
		return h;
	}

	public HashSet getFriends(){return friends;}
	public HashSet getIgnored(){return ignored;}
	
	protected String getPrivateList(HashSet h)
	{
		if((h==null)||(h.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator e=h.iterator();e.hasNext();)
			list.append(((String)e.next())+";");
		return list.toString();
	}
	public String getXML()
	{
		String f=getPrivateList(getFriends());
		String i=getPrivateList(getIgnored());
        StringBuffer rest=new StringBuffer("");
        String[] codes=getStatCodes();
        for(int x=getSaveStatIndex();x<codes.length;x++)
        {
        	String code=codes[x].toUpperCase();
        	rest.append("<"+code+">"+CMLib.xml().parseOutAngleBrackets(getStat(code))+"</"+code+">");
        }
        
		return ((f.length()>0)?"<FRIENDS>"+f+"</FRIENDS>":"")
			+((i.length()>0)?"<IGNORED>"+i+"</IGNORED>":"")
			+"<ACCTEXP>"+accountExpiration+"</ACCTEXP>"
			+((notes.length()>0)?"<NOTES>"+CMLib.xml().parseOutAngleBrackets(notes)+"</NOTES>":"")
            +rest.toString();
	}

	public void setXML(String str)
	{
		friends=getHashFrom(CMLib.xml().returnXMLValue(str,"FRIENDS"));
		ignored=getHashFrom(CMLib.xml().returnXMLValue(str,"IGNORED"));
        if(CMLib.xml().returnXMLValue(str,"ACCTEXP").length()>0)
            setAccountExpiration(CMath.s_long(CMLib.xml().returnXMLValue(str,"ACCTEXP")));
        else
        {
            Calendar C=Calendar.getInstance();
            C.add(Calendar.DATE,15);
            setAccountExpiration(C.getTimeInMillis());
        }
        notes=CMLib.xml().returnXMLValue(str,"NOTES");
        if(notes==null) notes="";
        notes=CMLib.xml().restoreAngleBrackets(notes);
		
        String[] codes=getStatCodes();
        for(int i=getSaveStatIndex();i<codes.length;i++)
        {
        	String val=CMLib.xml().returnXMLValue(str,codes[i].toUpperCase());
        	if(val==null) val="";
        	setStat(codes[i].toUpperCase(),CMLib.xml().restoreAngleBrackets(val));
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
		try
		{
			for(int p=0;p<players.size();p++)
				if(players.elementAt(p).equalsIgnoreCase(mob.Name()))
					return;
		}
		catch(Exception e) {}
		players.add(mob.Name());
	}
	public void delPlayer(MOB mob) 
	{
		players.remove(mob.Name());
		try
		{
			for(int p=players.size()-1;p>=0;p--)
				if(players.elementAt(p).equalsIgnoreCase(mob.Name()))
					players.removeElementAt(p);
		}
		catch(Exception e) {}
	}
	public Enumeration<MOB> getLoadPlayers() {
		Vector<MOB> mobs = new Vector<MOB>(players.size());
		for(Enumeration<String> e=getPlayers();e.hasMoreElements();)
		{
			MOB M=CMLib.players().getLoadPlayer(e.nextElement());
			if(M!=null) mobs.addElement(M);
		}
		return mobs.elements();
	}
	public Enumeration<String> getPlayers() {
		return ((Vector<String>)players.clone()).elements();
	}
	public void setPlayerNames(Vector<String> names) {
		names.trimToSize();
		players = names;
	}
	
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
