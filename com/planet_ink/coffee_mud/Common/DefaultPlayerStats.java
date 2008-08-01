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
   Copyright 2000-2008 Bo Zimmerman

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
    public String ID(){return "DefaultPlayerStats";}
    protected final static int TELL_STACK_MAX_SIZE=50;
    protected final static int GTELL_STACK_MAX_SIZE=50;
    protected long Hygiene=0; 
	
    protected HashSet friends=new HashSet();
    protected HashSet ignored=new HashSet();
	protected Vector tellStack=new Vector();
	protected Vector gtellStack=new Vector();
	protected Vector titles=new Vector();
    protected DVector alias=new DVector(2);
    protected String[] xtraValues=null;
	protected String lastIP="";
    protected long LastDateTime=System.currentTimeMillis();
    protected long lastUpdated=0;
	protected int channelMask;
	protected String email="";
	protected String Password="";
	protected String colorStr="";
	protected String prompt="";
	protected String poofin="";
	protected String poofout="";						  
	protected String tranpoofin="";
	protected String tranpoofout="";
    protected String announceMsg="";
    protected String notes="";
	protected int wrap=78;
    protected int[] birthday=null;
	protected MOB replyTo=null;
	protected int replyType=0;
	protected long replyTime=0;
	
	protected Vector securityGroups=new Vector();
    protected long accountExpiration=0;
    protected RoomnumberSet visitedRoomSet=null;
    protected DVector levelInfo=new DVector(3);
    protected HashSet introductions=new HashSet();

    public DefaultPlayerStats() {
        super();
        xtraValues=CMProps.getExtraStatCodesHolder(ID());
    }
    
	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","TITLES",
									 "ALIAS","LASTIP","LASTDATETIME",
									 "CHANNELMASK",
									 "COLORSTR","PROMPT","POOFIN",
									 "POOFOUT","TRANPOOFIN","TRAINPOOFOUT",
									 "ANNOUNCEMSG","NOTES","WRAP","BIRTHDAY",
									 "ACCTEXPIRATION","INTRODUCTIONS"};
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return "<FRIENDS>"+getPrivateList(getFriends())+"</FRIENDS>";
		case 2: return "<IGNORED>"+getPrivateList(getIgnored())+"</IGNORED>";
		case 3: return getTitleXML();
		case 4: return getAliasXML();
		case 5: return lastIP;
		case 6: return ""+LastDateTime;
		case 7: return ""+channelMask;
		case 8: return colorStr;
		case 9: return prompt;
		case 10: return poofin;
		case 11: return poofout;
		case 12: return tranpoofin;
		case 13: return tranpoofout;
		case 14: return announceMsg;
		case 15: return notes;
		case 16: return ""+wrap;
		case 17: return CMParms.toStringList(birthday);
		case 18: return ""+accountExpiration;
		case 19: return "<INTROS>"+getPrivateList(introductions)+"</INTROS>";
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
		case 3: setTitleXML(val); break;
		case 4: setAliasXML(val); break;
		case 5: lastIP=val; break;
		case 6: LastDateTime=CMath.s_long(val); break;
		case 7: channelMask=CMath.s_int(val); break;
		case 8: colorStr=val; break;
		case 9: prompt=val; break;
		case 10: poofin=val; break;
		case 11: poofout=val; break;
		case 12: tranpoofin=val; break;
		case 13: tranpoofout=val; break;
		case 14: announceMsg=val; break;
		case 15: notes=val; break;
		case 16: wrap=CMath.s_int(val); break;
		case 17: setBirthday(val); break;
		case 18: accountExpiration=CMath.s_long(val); break;
		case 19: introductions=getHashFrom(CMLib.xml().returnXMLValue(val,"INTROS")); break;
		default:
            CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
            break;
		}
	}
	public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	private static String[] codes=null;
	public String[] getStatCodes(){
	    if(codes==null)
	        codes=CMProps.getStatCodesList(CODES,ID());
	    return codes;
	}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(PlayerStats E)
	{
		if(!(E instanceof DefaultPlayerStats)) return false;
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
            DefaultPlayerStats O=(DefaultPlayerStats)this.clone();
            O.levelInfo=levelInfo.copyOf();
            if(visitedRoomSet!=null)
                O.visitedRoomSet=(RoomnumberSet)visitedRoomSet.copyOf();
            else
                O.visitedRoomSet=null;
            O.securityGroups=(Vector)securityGroups.clone();
            O.friends=(HashSet)friends.clone();
            O.ignored=(HashSet)ignored.clone();
            O.tellStack=(Vector)tellStack.clone();
            O.gtellStack=(Vector)gtellStack.clone();
            O.titles=(Vector)titles.clone();
            O.alias=alias.copyOf();
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
	public int getWrap(){return wrap;}
	public void setWrap(int newWrap){wrap=newWrap;}
	public String password(){return Password;}
	public void setPassword(String newPassword){Password=newPassword;}
	public String notes(){return notes;}
	public void setNotes(String newnotes){notes=newnotes;}
	public void setChannelMask(int newMask){ channelMask=newMask;}
	public int getChannelMask(){ return channelMask;}
	public MOB replyTo(){	return replyTo;	}
	public int replyType(){	return replyType;}
	public long replyTime(){	return replyTime;	}
	public void setReplyTo(MOB mob, int replyType)
	{	
		replyTo=mob;
		this.replyType=replyType;
	}
	public void setPrompt(String newPrompt){prompt=newPrompt;}
	public String getColorStr(){return colorStr;}
	public void setColorStr(String newColors){colorStr=newColors;}
    public String announceMessage(){return announceMsg;}
    public void setAnnounceMessage(String msg){announceMsg=msg;}
	public String getPrompt()
	{
		if((prompt==null)||(prompt.length()==0))
        {
            prompt=CMProps.getVar(CMProps.SYSTEM_DEFAULTPROMPT);
            if((prompt==null)||(prompt.length()==0))
                return "^N%E<^h%hhp ^m%mm ^v%vmv^N>";
        }
		return prompt;
	}

    public boolean isIntroducedTo(String name){return introductions.contains(name.toUpperCase().trim());}
    public void introduceTo(String name){
        if((!isIntroducedTo(name))&&(name.trim().length()>0))
            introductions.add(name.toUpperCase().trim());
    }
    
	public HashSet getHashFrom(String str)
	{
		HashSet h=new HashSet();
		if((str==null)||(str.length()==0)) return h;
		str=CMStrings.replaceAll(str,"<FRIENDS>","");
		str=CMStrings.replaceAll(str,"<IGNORED>","");
        str=CMStrings.replaceAll(str,"<INTROS>","");
        str=CMStrings.replaceAll(str,"</INTROS>","");
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

	public void addTellStack(String msg)
	{
		if(tellStack.size()>TELL_STACK_MAX_SIZE)
			tellStack.removeElementAt(0);
		tellStack.addElement(msg);
	}
	
	public Vector getTellStack()
	{
		return (Vector)tellStack.clone();
	}
    public RoomnumberSet roomSet()
    {
        if(visitedRoomSet==null)
            visitedRoomSet=((RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet"));
        return visitedRoomSet;
    }
	public void addGTellStack(String msg)
	{
		if(gtellStack.size()>GTELL_STACK_MAX_SIZE)
			gtellStack.removeElementAt(0);
		gtellStack.addElement(msg);
	}
	
	public Vector getGTellStack()
	{
		return (Vector)gtellStack.clone();
	}
	
	public HashSet getFriends(){return friends;}
	public HashSet getIgnored(){return ignored;}
	
    public String[] getAliasNames()
    {
        String[] aliasNames=new String[alias.size()];
        for(int s=0;s<alias.size();s++)
            aliasNames[s]=(String)alias.elementAt(s,1);
        return aliasNames;
    }
    
    public String getAlias(String named)
    {
        int x=alias.indexOf(named.toUpperCase().trim());
        if(x<0) return "";
        return (String)alias.elementAt(x,2);
    }
    public void addAliasName(String named)
    {
        named=named.toUpperCase().trim();
        if(getAlias(named).length()==0)
            alias.addElement(named,"");
    }
    public void delAliasName(String named)
    {
        int x=alias.indexOf(named.toUpperCase().trim());
        if(x>=0) alias.removeElementAt(x);
    }
    public void setAlias(String named, String value)
    {
        int x=alias.indexOf(named.toUpperCase().trim());
        if(x>=0) alias.setElementAt(x,2,value);
    }
    
    public String getAliasXML()
    {
        if(alias.size()==0) return "";
        StringBuffer str=new StringBuffer("");
        for(int t=alias.size()-1;t>=0;t--)
        {
            String s=(String)alias.elementAt(t,1);
            if(s.length()==0) alias.removeElementAt(t);
        }
        for(int t=0;t<alias.size();t++)
        {
            String s=(String)alias.elementAt(t,1);
            String v=(String)alias.elementAt(t,2);
            str.append("<ALIAS"+t+">"+s+"</ALIAS"+t+">");
            str.append("<ALIASV"+t+">"+v+"</ALIASV"+t+">");
        }
        return str.toString();
    }
    
    public String getActiveTitle()
    {
        if((titles==null)||(titles.size()==0)) return null;
        String s=(String)titles.firstElement();
        if((s.length()<2)||(s.charAt(0)!='{')||(s.charAt(s.length()-1)!='}'))
            return s;
        return s.substring(1,s.length()-1);
    }
    
	public Vector getTitles()
	{
	    return titles;
	}
	public String getTitleXML()
	{
	    if(titles.size()==0) return "";
	    StringBuffer str=new StringBuffer("");
	    for(int t=titles.size()-1;t>=0;t--)
	    {
	        String s=(String)titles.elementAt(t);
	        if(s.length()==0) titles.removeElementAt(t);
	    }
	    for(int t=0;t<titles.size();t++)
	    {
	        String s=(String)titles.elementAt(t);
	        str.append("<TITLE"+t+">"+s+"</TITLE"+t+">");
	    }
	    return str.toString();
	}
	
	public String poofIn(){return poofin;}
	public String poofOut(){return poofout;}
	public String tranPoofIn(){return tranpoofin;}
	public String tranPoofOut(){return tranpoofout;}
	public int[] getBirthday(){return birthday;}
	public int initializeBirthday(int ageHours, Race R)
	{
	    birthday=new int[3];
	    TimeClock C=CMClass.globalClock();
	    birthday[0]=C.getDayOfMonth();
	    birthday[1]=C.getMonth();
	    birthday[2]=C.getYear();
	    while(ageHours>15)
	    {
	        birthday[2]-=1;
	        ageHours-=15;
	    }
	    if(ageHours>0)
	    {
	        birthday[1]=CMLib.dice().roll(1,C.getMonthsInYear(),0);
	        birthday[0]=CMLib.dice().roll(1,C.getDaysInMonth(),0);
	    }
        int month=C.getMonth();
        int day=C.getDayOfMonth();
        if((month<birthday[1])||((month==birthday[1])&&(birthday[0]<day)))
		    return (R.getAgingChart()[Race.AGE_YOUNGADULT]+C.getYear()-birthday[2])-1;
	    return (R.getAgingChart()[Race.AGE_YOUNGADULT]+C.getYear()-birthday[2]);
	}
	
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
        String t=getPrivateList(introductions);
        StringBuffer rest=new StringBuffer("");
        String[] codes=getStatCodes();
        for(int x=getSaveStatIndex();x<codes.length;x++)
        {
        	String code=codes[x].toUpperCase();
        	rest.append("<"+code+">"+CMLib.xml().parseOutAngleBrackets(getStat(code))+"</"+code+">");
        }
        
		return ((f.length()>0)?"<FRIENDS>"+f+"</FRIENDS>":"")
			+((i.length()>0)?"<IGNORED>"+i+"</IGNORED>":"")
            +((t.length()>0)?"<INTROS>"+t+"</INTROS>":"")
			+"<WRAP>"+wrap+"</WRAP>"
			+getTitleXML()
            +getAliasXML()
			+"<ACCTEXP>"+accountExpiration+"</ACCTEXP>"
			+((birthday!=null)?"<BIRTHDAY>"+CMParms.toStringList(birthday)+"</BIRTHDAY>":"")
			+((poofin.length()>0)?"<POOFIN>"+CMLib.xml().parseOutAngleBrackets(poofin)+"</POOFIN>":"")
			+((notes.length()>0)?"<NOTES>"+CMLib.xml().parseOutAngleBrackets(notes)+"</NOTES>":"")
			+((poofout.length()>0)?"<POOFOUT>"+CMLib.xml().parseOutAngleBrackets(poofout)+"</POOFOUT>":"")
            +((announceMsg.length()>0)?"<ANNOUNCE>"+CMLib.xml().parseOutAngleBrackets(announceMsg)+"</ANNOUNCE>":"")
			+((tranpoofin.length()>0)?"<TRANPOOFIN>"+CMLib.xml().parseOutAngleBrackets(tranpoofin)+"</TRANPOOFIN>":"")
			+((tranpoofout.length()>0)?"<TRANPOOFOUT>"+CMLib.xml().parseOutAngleBrackets(tranpoofout)+"</TRANPOOFOUT>":"")
            +"<DATES>"+this.getLevelDateTimesStr()+"</DATES>"
			+getSecurityGroupStr()
            +roomSet().xml()
            +rest.toString();
	}

	private void setBirthday(String bday)
	{
		if((bday!=null)&&(bday.length()>0))
		{
		    Vector V=CMParms.parseCommas(bday,true);
		    birthday=new int[3];
		    for(int v=0;v<V.size();v++)
		        birthday[v]=CMath.s_int((String)V.elementAt(v));
		}
	}
	
	private void setAliasXML(String str)
	{
        alias.clear();
        int a=-1;
        while((++a)>=0)
        {
            String name=CMLib.xml().returnXMLValue(str,"ALIAS"+a);
            String value=CMLib.xml().returnXMLValue(str,"ALIASV"+a);
            if((name.length()==0)||(value.length()==0))
                break;
            alias.addElement(name,value);
        }
	}
	
	private void setTitleXML(String str)
	{
		titles.clear();
		int t=-1;
		while((++t)>=0)
		{
			String title=CMLib.xml().returnXMLValue(str,"TITLE"+t);
			if(title.length()==0)
			    break;
		    titles.addElement(title);
		}
		
	}
	
	public void setXML(String str)
	{
		friends=getHashFrom(CMLib.xml().returnXMLValue(str,"FRIENDS"));
		ignored=getHashFrom(CMLib.xml().returnXMLValue(str,"IGNORED"));
        introductions=getHashFrom(CMLib.xml().returnXMLValue(str,"INTROS"));
        if(CMLib.xml().returnXMLValue(str,"ACCTEXP").length()>0)
            setAccountExpiration(CMath.s_long(CMLib.xml().returnXMLValue(str,"ACCTEXP")));
        else
        {
            Calendar C=Calendar.getInstance();
            C.add(Calendar.DATE,15);
            setAccountExpiration(C.getTimeInMillis());
        }
		String oldWrap=CMLib.xml().returnXMLValue(str,"WRAP");
		if(CMath.isInteger(oldWrap)) wrap=CMath.s_int(oldWrap);
		setSecurityGroupStr(CMLib.xml().returnXMLValue(str,"SECGRPS"));
		setAliasXML(str);
		setTitleXML(str);
		String bday=CMLib.xml().returnXMLValue(str,"BIRTHDAY");
		setBirthday(bday);
		
		poofin=CMLib.xml().returnXMLValue(str,"POOFIN");
		if(poofin==null) poofin="";
		poofin=CMLib.xml().restoreAngleBrackets(poofin);
		
		poofout=CMLib.xml().returnXMLValue(str,"POOFOUT");
		if(poofout==null) poofout="";
		poofout=CMLib.xml().restoreAngleBrackets(poofout);
		
		tranpoofin=CMLib.xml().returnXMLValue(str,"TRANPOOFIN");
		if(tranpoofin==null) tranpoofin="";
		tranpoofin=CMLib.xml().restoreAngleBrackets(tranpoofin);
		
		tranpoofout=CMLib.xml().returnXMLValue(str,"TRANPOOFOUT");
		if(tranpoofout==null) tranpoofout="";
		tranpoofout=CMLib.xml().restoreAngleBrackets(tranpoofout);
		
        announceMsg=CMLib.xml().returnXMLValue(str,"ANNOUNCE");
        if(announceMsg==null) announceMsg="";
        announceMsg=CMLib.xml().restoreAngleBrackets(announceMsg);
        
        notes=CMLib.xml().returnXMLValue(str,"NOTES");
        if(notes==null) notes="";
        notes=CMLib.xml().restoreAngleBrackets(notes);
		
        String dates=CMLib.xml().returnXMLValue(str,"DATES");
        if(dates==null) dates="";
        // now parse all the level date/times
        int lastNum=Integer.MIN_VALUE;
        levelInfo.clear();
        if(dates.length()>0)
        {
            Vector sets=CMParms.parseSemicolons(dates,true);
            for(int ss=0;ss<sets.size();ss++)
            {
                String sStr=(String)sets.elementAt(ss);
                Vector twin=CMParms.parseCommas(sStr,true);
                if((twin.size()!=2)&&(twin.size()!=3))  continue;
                if(CMath.s_int((String)twin.firstElement())>=lastNum)
                {
                	levelInfo.addElement(new Integer(CMath.s_int((String)twin.firstElement())),
                                              new Long(CMath.s_long((String)twin.elementAt(1))),
                                              (twin.size()>2)?(String)twin.elementAt(2):"");
                    lastNum=CMath.s_int((String)twin.firstElement());
                }
            }
        }
        if(levelInfo.size()==0)
        	levelInfo.addElement(new Integer(0),new Long(System.currentTimeMillis()),"");
        roomSet().parseXML(str);
        String[] codes=getStatCodes();
        for(int i=getSaveStatIndex();i<getStatCodes().length;i++)
        {
        	String val=CMLib.xml().returnXMLValue(str,codes[i].toUpperCase());
        	if(val==null) val="";
        	setStat(codes[i].toUpperCase(),CMLib.xml().restoreAngleBrackets(val));
        }
	}

    private String getLevelDateTimesStr()
    {
        if(levelInfo.size()==0)
        	levelInfo.addElement(new Integer(0),new Long(System.currentTimeMillis()),"");
        StringBuffer buf=new StringBuffer("");
        for(int ss=0;ss<levelInfo.size();ss++)
        {
            buf.append(((Integer)levelInfo.elementAt(ss,1)).intValue()+",");
            buf.append(((Long)levelInfo.elementAt(ss,2)).longValue()+",");
            buf.append((String)levelInfo.elementAt(ss,3)+";");
        }
        return buf.toString();
    }
    
    protected void setSecurityGroupStr(String grps)
	{
		securityGroups=new Vector();
		if((grps==null)||(grps.trim().length()==0))	
			return;
		int x=grps.indexOf(";");
		while(x>=0)
		{
			String fi=grps.substring(0,x).trim();
			if(fi.length()>0) securityGroups.addElement(fi.toUpperCase());
			grps=grps.substring(x+1);
			x=grps.indexOf(";");
		}
		if(grps.trim().length()>0)
			securityGroups.addElement(grps.trim().toUpperCase());
	}
	protected String getSecurityGroupStr()
	{
		if(securityGroups.size()==0) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator e=securityGroups.iterator();e.hasNext();)
			list.append(((String)e.next())+";");
		return "<SECGRPS>"+list.toString()+"</SECGRPS>";
		
	}
	public Vector getSecurityGroups(){	return securityGroups;}
	public void setPoofs(String poofIn, String poofOut, String tranPoofIn, String tranPoofOut)
	{
		poofin=poofIn;
		poofout=poofOut;
		tranpoofin=tranPoofIn;
		tranpoofout=tranPoofOut;
	}
	
	public long getHygiene(){return Hygiene;}
	public void setHygiene(long newVal){Hygiene=newVal;}
	public boolean adjHygiene(long byThisMuch)
	{
	    Hygiene+=byThisMuch;
		if(Hygiene<1)
		{
		    Hygiene=0;
			return false;
		}
		return true;
	}
	

    // Acct Expire Code
    public long getAccountExpiration() {return accountExpiration;}
    public void setAccountExpiration(long newVal){accountExpiration=newVal;}
    
    public void addRoomVisit(Room R)
    {
        if((!CMSecurity.isDisabled("ROOMVISITS"))
        &&(R!=null)
        &&(!CMath.bset(R.envStats().sensesMask(),EnvStats.SENSE_ROOMUNEXPLORABLE)))
            roomSet().add(CMLib.map().getExtendedRoomID(R));
    }
    public boolean hasVisited(Room R)
    {
        return roomSet().contains(CMLib.map().getExtendedRoomID(R));
    }
    public boolean hasVisited(Area A)
    {
        int numRooms=A.getAreaIStats()[Area.AREASTAT_VISITABLEROOMS];
        if(numRooms<=0) return true;
        return roomSet().roomCount(A.Name())>0;
    }

    
    public int percentVisited(MOB mob, Area A)
    {
        if(A==null)
        {
            long totalRooms=0;
            long totalVisits=0;
            for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
            {
                A=(Area)e.nextElement();
                if(CMLib.flags().canAccess(mob,A))
                {
                	int[] stats=A.getAreaIStats();
                    if(stats[Area.AREASTAT_VISITABLEROOMS]>0)
                    {
                        totalRooms+=stats[Area.AREASTAT_VISITABLEROOMS];
                        totalVisits+=roomSet().roomCount(A.Name());
                    }
                }
            }
            if(totalRooms==0) return 100;
            double pct=CMath.div(totalVisits,totalRooms);
            return (int)Math.round(100.0*pct);
        }
        int numRooms=A.getAreaIStats()[Area.AREASTAT_VISITABLEROOMS];
        if(numRooms<=0) return 100;
        double pct=CMath.div(roomSet().roomCount(A.Name()),numRooms);
        return (int)Math.round(100.0*pct);
    }
    
    public long leveledDateTime(int level)
    {
        if(levelInfo.size()==0)
        	levelInfo.addElement(new Integer(0),new Long(System.currentTimeMillis()),"");
        long lowest=((Long)levelInfo.elementAt(0,2)).longValue();
        for(int l=1;l<levelInfo.size();l++)
        {
            if(level<((Integer)levelInfo.elementAt(l,1)).intValue())
                return lowest;
            lowest=((Long)levelInfo.elementAt(l,2)).longValue();
        }
        return lowest;
    }
    
    public void setLeveledDateTime(int level, Room R)
    {
        if(levelInfo.size()==0)
        	levelInfo.addElement(new Integer(0),new Long(System.currentTimeMillis()),"");
        long lastTime=0;
        for(int l=0;l<levelInfo.size();l++)
        {
            if(level==((Integer)levelInfo.elementAt(l,1)).intValue())
            {
            	levelInfo.setElementAt(l,2,new Long(System.currentTimeMillis()));
            	levelInfo.setElementAt(l,3,CMLib.map().getExtendedRoomID(R));
                return;
            }
            else
            if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_HOUR)
                return;
            else
            if(level<((Integer)levelInfo.elementAt(l,1)).intValue())
            {
            	levelInfo.insertElementAt(l,new Integer(level),new Long(System.currentTimeMillis()),CMLib.map().getExtendedRoomID(R));
                return;
            }
            lastTime=((Long)levelInfo.elementAt(l,2)).longValue();
        }
        if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_HOUR)
            return;
        levelInfo.addElement(new Integer(level),new Long(System.currentTimeMillis()),CMLib.map().getExtendedRoomID(R));
    }
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
