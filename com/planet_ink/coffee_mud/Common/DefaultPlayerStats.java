package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;
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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class DefaultPlayerStats implements PlayerStats
{
	public String ID(){return "DefaultPlayerStats";}
	public String name() { return ID();}
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
	protected PlayerAccount  account		= null;
	protected SecGroup		 securityFlags	= new SecGroup(new CMSecurity.SecFlag[]{});
	protected long			 accountExpires	= 0;
	protected RoomnumberSet  visitedRoomSet	= null;
	protected DVector   	 levelInfo		= new DVector(3);
	protected Set<String>	 introductions	= new SHashSet<String>();
	protected ItemCollection extItems;
	
	protected Map<String,String>	alias	= new STreeMap<String,String>();
	protected Map<String,Integer>	legacy	= new STreeMap<String,Integer>();

	public DefaultPlayerStats() 
	{
		super();
		xtraValues=CMProps.getExtraStatCodesHolder(this);
		extItems=(ItemCollection)CMClass.getCommon("WeakItemCollection");
	}
	
	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","TITLES",
									 "ALIAS","LASTIP","LASTDATETIME",
									 "CHANNELMASK",
									 "COLORSTR","PROMPT","POOFIN",
									 "POOFOUT","TRANPOOFIN","TRAINPOOFOUT",
									 "ANNOUNCEMSG","NOTES","WRAP","BIRTHDAY",
									 "ACCTEXPIRATION","INTRODUCTIONS","PAGEBREAK",
									 "SAVEDPOSE","THEME", "LEGLEVELS"};
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return getPrivateList(getFriends());
		case 2: return getPrivateList(getIgnored());
		case 3: return getTitleXML();
		case 4: return getAliasXML();
		case 5: return lastIP;
		case 6: return ""+lLastDateTime;
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
		case 18: return ""+accountExpires;
		case 19: return getPrivateList(introductions);
		case 20: return ""+pageBreak;
		case 21: return ""+savedPose;
		case 22: return ""+theme;
		case 23: return ""+getTotalLegacyLevels();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: break;
		case 1: { friends.clear(); friends.addAll(getHashFrom(val)); break; }
		case 2: { ignored.clear(); ignored.addAll(getHashFrom(val)); break; }
		case 3: setTitleXML(CMLib.xml().parseAllXML(val)); break;
		case 4: setAliasXML(CMLib.xml().parseAllXML(val)); break;
		case 5: lastIP=val; break;
		case 6: lLastDateTime=CMath.s_long(val); break;
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
		case 18: accountExpires=CMath.s_long(val); break;
		case 19: { introductions.clear(); introductions.addAll(getHashFrom(val)); break; }
		case 20: pageBreak=CMath.s_int(val); break;
		case 21: savedPose=val; break;
		case 22: theme=CMath.s_int(val); break;
		case 23: break; // legacy levels
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
	public boolean sameAs(PlayerStats E)
	{
		if(!(E instanceof DefaultPlayerStats)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
	
	public CMObject newInstance(){try{return getClass().newInstance();}catch(Exception e){return new DefaultPlayerStats();}}
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
		catch(CloneNotSupportedException e)
		{
			return new DefaultPlayerStats();
		}
	}
	public String lastIP(){return lastIP;}
	public void setLastIP(String ip)
	{
		lastIP=ip;
		if(account != null)
			account.setLastIP(ip);
	}
	public String getEmail()
	{
		if(account != null)
			return account.getEmail();
		if(email==null) 
			return ""; 
		return email;
	}
	public void setEmail(String newAdd)
	{
		email=newAdd;
		if(account != null)
			account.setEmail(newAdd);
	}
	public int getTheme()
	{
		return theme;
	}
	public void setTheme(int theme)
	{
		this.theme=theme;
	}
	public long lastUpdated(){return lastUpdated;}
	public void setLastUpdated(long time)
	{
		lastUpdated=time;
		if(account != null)
			account.setLastUpdated(time);
	}
	public long lastDateTime(){return lLastDateTime;}
	public void setLastDateTime(long C)
	{ 
		lLastDateTime=C;
		if(account != null)
			account.setLastDateTime(C);
	}
	public String getPasswordStr()
	{
		return (account!=null)?account.getPasswordStr():password;
	}
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
	public boolean matchesPassword(String checkPass)
	{
		if(account!=null)
			return account.matchesPassword(checkPass);
		if(CMLib.encoder().isARandomHashString(password))
			return CMLib.encoder().checkAgainstRandomHashString(checkPass, password);
		return checkPass.equalsIgnoreCase(password);
	}
	
	public int getWrap(){return wrap;}
	public void setWrap(int newWrap){wrap=newWrap;}
	public int getPageBreak(){return pageBreak;}
	public void setPageBreak(int newBreak){pageBreak=newBreak;}
	public String notes(){return notes;}
	public void setNotes(String newnotes){notes=newnotes;}
	public void setChannelMask(int newMask){ channelMask=newMask;}
	public int getChannelMask(){ return channelMask;}
	public MOB replyTo(){    return replyTo;	}
	public int replyType(){    return replyType;}
	public long replyTime(){	return replyTime;    }
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
	public String getSavedPose(){return savedPose;}
	public void setSavedPose(String msg){savedPose=msg;}
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

	public boolean isIntroducedTo(String name){return introductions.contains(name.toUpperCase().trim());}
	public void introduceTo(String name){
		if((!isIntroducedTo(name))&&(name.trim().length()>0))
			introductions.add(name.toUpperCase().trim());
	}
	
	public SHashSet<String> getHashFrom(String str)
	{
		SHashSet<String> h=new SHashSet<String>();
		if((str==null)||(str.length()==0)) return h;
		str=CMStrings.replaceAll(str,"<FRIENDS>","");
		str=CMStrings.replaceAll(str,"<IGNORED>","");
		str=CMStrings.replaceAll(str,"<INTROS>","");
		str=CMStrings.replaceAll(str,"</INTROS>","");
		str=CMStrings.replaceAll(str,"</FRIENDS>","");
		str=CMStrings.replaceAll(str,"</IGNORED>","");
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

	public void addTellStack(String msg)
	{
		if(tellStack.size()>TELL_STACK_MAX_SIZE)
			tellStack.remove(0);
		tellStack.add(msg);
	}
	
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
	public void addGTellStack(String msg)
	{
		if(gtellStack.size()>GTELL_STACK_MAX_SIZE)
			gtellStack.remove(0);
		gtellStack.add(msg);
	}
	
	public List<String> getGTellStack()
	{
		return new ReadOnlyList<String>(gtellStack);
	}
	
	public Set<String> getFriends()
	{
		if(account != null)
			return account.getFriends();
		return friends;
	}
	public Set<String> getIgnored()
	{
		if(account != null)
			return account.getIgnored();
		return ignored;
	}
	
	public String[] getAliasNames()
	{
		return alias.keySet().toArray(new String[0]);
	}
	
	public String getAlias(String named)
	{
		if(alias.containsKey(named.toUpperCase().trim()))
			return alias.get(named.toUpperCase().trim());
		return "";
	}
	public void addAliasName(String named)
	{
		named=named.toUpperCase().trim();
		if(getAlias(named).length()==0)
			alias.put(named,"");
	}
	public void delAliasName(String named)
	{
		alias.remove(named.toUpperCase().trim());
	}
	public void setAlias(String named, String value)
	{
		alias.put(named.toUpperCase().trim(),value);
	}
	
	public String getAliasXML()
	{
		if(alias.size()==0) return "";
		StringBuilder str=new StringBuilder("");
		alias.remove("");
		for(String key : alias.keySet())
			str.append("<ALIAS CMD=\"").append(key).append("\" VAL=\"").append(CMLib.xml().parseOutAngleBracketsAndQuotes(alias.get(key))).append("\" />");
		return str.toString();
	}
	
	public String getLegacyXML()
	{
		if(legacy.size()==0) return "";
		StringBuilder str=new StringBuilder("");
		for(String key : legacy.keySet())
			str.append("<LEGACY CAT=\"").append(key).append("\" LVL=\"").append(legacy.get(key)).append("\" />");
		return str.toString();
	}
	
	public String getActiveTitle()
	{
		if((titles==null)||(titles.size()==0)) return null;
		String s=titles.get(0);
		if((s.length()<2)||(s.charAt(0)!='{')||(s.charAt(s.length()-1)!='}'))
			return s;
		return s.substring(1,s.length()-1);
	}
	
	public List<String> getTitles()
	{
		return titles;
	}
	private String getTitleXML()
	{
		if(titles.size()==0) return "";
		for(int t=titles.size()-1;t>=0;t--)
		{
			String s=titles.get(t);
			if(s.length()==0) titles.remove(t);
		}
		StringBuilder str=new StringBuilder("");
		for(int t=0;t<titles.size();t++)
		{
			String s=titles.get(t);
			str.append("<TITLE>"+CMLib.xml().parseOutAngleBrackets(CMLib.coffeeFilter().safetyFilter(s))+"</TITLE>");
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
		birthday=new int[4];
		TimeClock C=CMLib.time().globalClock();
		birthday[0]=C.getDayOfMonth();
		birthday[1]=C.getMonth();
		birthday[2]=C.getYear();
		birthday[3]=C.getYear();
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
	
	protected String getPrivateList(Set<String> h)
	{
		if((h==null)||(h.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator<String> e=h.iterator();e.hasNext();)
			list.append((e.next())+";");
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
			+"<THEME>"+theme+"</THEME>"
			+"<PAGEBREAK>"+pageBreak+"</PAGEBREAK>"
			+((account!=null)?("<ACCOUNT>"+account.accountName()+"</ACCOUNT>"):"")
			+getTitleXML()
			+getAliasXML()
			+getLegacyXML()
			+"<ACCTEXP>"+accountExpires+"</ACCTEXP>"
			+((birthday!=null)?"<BIRTHDAY>"+CMParms.toStringList(birthday)+"</BIRTHDAY>":"")
			+((poofin.length()>0)?"<POOFIN>"+CMLib.xml().parseOutAngleBrackets(poofin)+"</POOFIN>":"")
			+((notes.length()>0)?"<NOTES>"+CMLib.xml().parseOutAngleBrackets(notes)+"</NOTES>":"")
			+((poofout.length()>0)?"<POOFOUT>"+CMLib.xml().parseOutAngleBrackets(poofout)+"</POOFOUT>":"")
			+((announceMsg.length()>0)?"<ANNOUNCE>"+CMLib.xml().parseOutAngleBrackets(announceMsg)+"</ANNOUNCE>":"")
			+((savedPose.length()>0)?"<POSE>"+CMLib.xml().parseOutAngleBrackets(savedPose)+"</POSE>":"")
			+((tranpoofin.length()>0)?"<TRANPOOFIN>"+CMLib.xml().parseOutAngleBrackets(tranpoofin)+"</TRANPOOFIN>":"")
			+((tranpoofout.length()>0)?"<TRANPOOFOUT>"+CMLib.xml().parseOutAngleBrackets(tranpoofout)+"</TRANPOOFOUT>":"")
			+"<DATES>"+this.getLevelDateTimesStr()+"</DATES>"
			+"<SECGRPS>"+CMLib.xml().parseOutAngleBrackets(getSetSecurityFlags(null))+"</SECGRPS>"
			+roomSet().xml()
			+rest.toString();
	}

	private void setBirthday(String bday)
	{
		if((bday!=null)&&(bday.length()>0))
		{
			Vector<String> V=CMParms.parseCommas(bday,true);
			birthday=new int[4];
			for(int v=0;(v<V.size()) && (v<birthday.length);v++)
				birthday[v]=CMath.s_int(V.elementAt(v));
			if(V.size()<4)
			{
				TimeClock C=CMLib.time().globalClock();
				birthday[3]=C.getYear();
			}
		}
	}
	
	private void setAliasXML(List<XMLpiece> xml)
	{
		alias.clear();
		for(Iterator<XMLpiece> p=xml.iterator();p.hasNext();)
		{
			XMLpiece piece=p.next();
			if((piece.tag.equals("ALIAS"))&&(piece.parms!=null))
			{
				String command=CMLib.xml().getParmValue(piece.parms, "CMD");
				String value=CMLib.xml().getParmValue(piece.parms, "VAL");
				if((command!=null)&&(value!=null))
					alias.put(command, CMLib.xml().restoreAngleBrackets(value));
			}
		}
		int a=-1;
		while((++a)>=0)
		{
			String name=CMLib.xml().getValFromPieces(xml,"ALIAS"+a);
			String value=CMLib.xml().getValFromPieces(xml,"ALIASV"+a);
			if((name.length()==0)||(value.length()==0))
				break;
			alias.put(name.toUpperCase().trim(),CMLib.xml().restoreAngleBrackets(value));
		}
	}
	
	private void setLegacyXML(List<XMLpiece> xml)
	{
		legacy.clear();
		for(Iterator<XMLpiece> p=xml.iterator();p.hasNext();)
		{
			XMLpiece piece=p.next();
			if((piece.tag.equals("LEGACY"))&&(piece.parms!=null))
			{
				String category=CMLib.xml().getParmValue(piece.parms, "CAT");
				String levelStr=CMLib.xml().getParmValue(piece.parms, "LVL");
				if((category!=null)&&(levelStr!=null))
					legacy.put(category, Integer.valueOf(levelStr));
			}
		}
	}
	
	private void setTitleXML(List<XMLpiece> xml)
	{
		titles.clear();
		for(Iterator<XMLpiece> p=xml.iterator();p.hasNext();)
		{
			XMLpiece piece=p.next();
			if(piece.tag.equals("TITLE"))
				titles.add(CMLib.xml().restoreAngleBrackets(piece.value));
		}
		int t=-1;
		while((++t)>=0)
		{
			String title=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xml,"TITLE"+t));
			if(title.length()==0)
				break;
			titles.add(title);
		}
		
	}
	
	public void setXML(String xmlStr)
	{
		account = null;
		if(xmlStr==null) 
			return;
		final boolean debug=CMSecurity.isDebugging(CMSecurity.DbgFlag.PLAYERSTATS);
		if(debug) Log.debugOut("XML="+xmlStr);
		List<XMLLibrary.XMLpiece> xml = CMLib.xml().parseAllXML(xmlStr);
		String str=CMLib.xml().getValFromPieces(xml,"FRIENDS");
		if(debug) Log.debugOut("FRIENDS="+str);
		friends.clear();
		friends.addAll(getHashFrom(str));
		str=CMLib.xml().getValFromPieces(xml,"IGNORED");
		if(debug) Log.debugOut("IGNORED="+str);
		ignored.clear();
		ignored.addAll(getHashFrom(str));
		str=CMLib.xml().getValFromPieces(xml,"INTROS");
		if(debug) Log.debugOut("INTROS="+str);
		introductions.clear();
		introductions.addAll(getHashFrom(str));
		str=CMLib.xml().getValFromPieces(xml, "THEME");
		if(debug) Log.debugOut("THEME="+str);
		if(CMath.isInteger(str))
			theme=CMath.s_int(str);
		str = CMLib.xml().getValFromPieces(xml,"ACCTEXP");
		if(debug) Log.debugOut("ACCTEXP="+str);
		if((str!=null)&&(str.length()>0))
			setAccountExpiration(CMath.s_long(str));
		else
		{
			Calendar C=Calendar.getInstance();
			C.add(Calendar.DATE,CMProps.getIntVar(CMProps.Int.TRIALDAYS));
			setAccountExpiration(C.getTimeInMillis());
		}
		str=CMLib.xml().getValFromPieces(xml,"WRAP");
		if(debug) Log.debugOut("WRAP="+str);
		if(CMath.isInteger(str)) 
			wrap=CMath.s_int(str);
		str=CMLib.xml().getValFromPieces(xml,"PAGEBREAK");
		if(debug) Log.debugOut("PAGEBREAK="+str);
		if(CMath.isInteger(str)) 
			pageBreak=CMath.s_int(str);
		else
			pageBreak=CMProps.getIntVar(CMProps.Int.PAGEBREAK);
		str=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xml,"SECGRPS"));
		if(debug) Log.debugOut("SECGRPS="+str);
		getSetSecurityFlags(str);
		setAliasXML(xml);
		setTitleXML(xml);
		setLegacyXML(xml);
		str=CMLib.xml().getValFromPieces(xml,"BIRTHDAY");
		if(debug) Log.debugOut("BIRTHDAY="+str);
		setBirthday(str);
		
		poofin=CMLib.xml().getValFromPieces(xml,"POOFIN");
		if(debug) Log.debugOut("POOFIN="+poofin);
		if(poofin==null) poofin="";
		poofin=CMLib.xml().restoreAngleBrackets(poofin);
		
		poofout=CMLib.xml().getValFromPieces(xml,"POOFOUT");
		if(debug) Log.debugOut("POOFOUT="+poofout);
		if(poofout==null) poofout="";
		poofout=CMLib.xml().restoreAngleBrackets(poofout);
		
		tranpoofin=CMLib.xml().getValFromPieces(xml,"TRANPOOFIN");
		if(debug) Log.debugOut("TRANPOOFIN="+tranpoofin);
		if(tranpoofin==null) tranpoofin="";
		tranpoofin=CMLib.xml().restoreAngleBrackets(tranpoofin);
		
		tranpoofout=CMLib.xml().getValFromPieces(xml,"TRANPOOFOUT");
		if(debug) Log.debugOut("TRANPOOFOUT="+tranpoofout);
		if(tranpoofout==null) tranpoofout="";
		tranpoofout=CMLib.xml().restoreAngleBrackets(tranpoofout);
		
		announceMsg=CMLib.xml().getValFromPieces(xml,"ANNOUNCE");
		if(debug) Log.debugOut("ANNOUNCE="+announceMsg);
		if(announceMsg==null) announceMsg="";
		announceMsg=CMLib.xml().restoreAngleBrackets(announceMsg);
		
		savedPose=CMLib.xml().getValFromPieces(xml,"POSE");
		if(debug) Log.debugOut("POSE="+savedPose);
		if(savedPose==null) savedPose="";
		savedPose=CMLib.xml().restoreAngleBrackets(savedPose);
		
		notes=CMLib.xml().getValFromPieces(xml,"NOTES");
		if(debug) Log.debugOut("NOTES="+notes);
		if(notes==null) notes="";
		notes=CMLib.xml().restoreAngleBrackets(notes);
		
		str=CMLib.xml().getValFromPieces(xml,"DATES");
		if(debug) Log.debugOut("DATES="+str);
		if(str==null) str="";
		// now parse all the level date/times
		int lastNum=Integer.MIN_VALUE;
		levelInfo.clear();
		if(str.length()>0)
		{
			Vector<String> sets=CMParms.parseSemicolons(str,true);
			for(int ss=0;ss<sets.size();ss++)
			{
				String sStr=sets.elementAt(ss);
				Vector<String> twin=CMParms.parseCommas(sStr,true);
				if((twin.size()!=2)&&(twin.size()!=3))  continue;
				if(CMath.s_int(twin.firstElement())>=lastNum)
				{
					levelInfo.addElement(Integer.valueOf(CMath.s_int(twin.firstElement())),
											  Long.valueOf(CMath.s_long(twin.elementAt(1))),
											  (twin.size()>2)?(String)twin.elementAt(2):"");
					lastNum=CMath.s_int(twin.firstElement());
				}
			}
		}
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
		str = CMLib.xml().getValFromPieces(xml,"AREAS");
		if(debug) Log.debugOut("AREAS="+str);
		if(str!=null)
			roomSet().parseXML("<AREAS>"+str+"</AREAS>");
		else
			roomSet().parseXML("<AREAS />");
		String[] codes=getStatCodes();
		for(int i=getSaveStatIndex();i<codes.length;i++)
		{
			str=CMLib.xml().getValFromPieces(xml,codes[i].toUpperCase());
			if(str==null) str="";
			setStat(codes[i].toUpperCase(),CMLib.xml().restoreAngleBrackets(str));
		}
		
		str = CMLib.xml().getValFromPieces(xml,"ACCOUNT");
		if(debug) Log.debugOut("ACCOUNT="+str);
		if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
		{
			if((str != null)&&(str.length()>0))
				account = CMLib.players().getLoadAccount(str);
		}
	}

	private String getLevelDateTimesStr()
	{
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
		StringBuffer buf=new StringBuffer("");
		for(int ss=0;ss<levelInfo.size();ss++)
		{
			buf.append(((Integer)levelInfo.elementAt(ss,1)).intValue()+",");
			buf.append(((Long)levelInfo.elementAt(ss,2)).longValue()+",");
			buf.append((String)levelInfo.elementAt(ss,3)+";");
		}
		return buf.toString();
	}
	public String getSetSecurityFlags(String newFlags)
	{
		if(newFlags != null)
		{
			securityFlags=CMSecurity.instance().createGroup("", CMParms.parseSemicolons(newFlags,true));
		}
		return securityFlags.toString(';');
		
	}
	public CMSecurity.SecGroup getSecurityFlags()
	{ 
		return securityFlags;
	}
	public void setPoofs(String poofIn, String poofOut, String tranPoofIn, String tranPoofOut)
	{
		poofin=poofIn;
		poofout=poofOut;
		tranpoofin=tranPoofIn;
		tranpoofout=tranPoofOut;
	}
	
	public long getHygiene(){return hygiene;}
	public void setHygiene(long newVal){hygiene=newVal;}
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
	

	// Acct Expire Code
	public long getAccountExpiration() {
		return  (account != null) ? account.getAccountExpiration() : accountExpires;
	}
	public void setAccountExpiration(long newVal)
	{
		if(account != null)
			account.setAccountExpiration(newVal);
		accountExpires=newVal;
	}
	
	public void addRoomVisit(Room R)
	{
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))
		&&(R!=null)
		&&(!CMath.bset(R.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNEXPLORABLE))
		&&(!(R.getArea() instanceof AutoGenArea)))
			roomSet().add(CMLib.map().getExtendedRoomID(R));
	}
	public boolean hasVisited(Room R)
	{
		return roomSet().contains(CMLib.map().getExtendedRoomID(R));
	}
	public boolean hasVisited(Area A)
	{
		int numRooms=A.getAreaIStats()[Area.Stats.VISITABLE_ROOMS.ordinal()];
		if(numRooms<=0) return true;
		return roomSet().roomCount(A.Name())>0;
	}
	public void unVisit(Room R)
	{
		if(roomSet().contains(CMLib.map().getExtendedRoomID(R)))
			roomSet().remove(CMLib.map().getExtendedRoomID(R));
	}
	public void unVisit(Area A)
	{
		Room R;
		for(Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if(roomSet().contains(CMLib.map().getExtendedRoomID(R)))
				roomSet().remove(CMLib.map().getExtendedRoomID(R));
		}
	}
	
	public int percentVisited(MOB mob, Area A)
	{
		if(A==null)
		{
			long totalRooms=0;
			long totalVisits=0;
			for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				A=e.nextElement();
				if((CMLib.flags().canAccess(mob,A))
				&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
				{
					int[] stats=A.getAreaIStats();
					if(stats[Area.Stats.VISITABLE_ROOMS.ordinal()]>0)
					{
						totalRooms+=stats[Area.Stats.VISITABLE_ROOMS.ordinal()];
						totalVisits+=roomSet().roomCount(A.Name());
					}
				}
			}
			if(totalRooms==0) return 100;
			double pct=CMath.div(totalVisits,totalRooms);
			return (int)Math.round(100.0*pct);
		}
		int numRooms=A.getAreaIStats()[Area.Stats.VISITABLE_ROOMS.ordinal()];
		if(numRooms<=0) return 100;
		double pct=CMath.div(roomSet().roomCount(A.Name()),numRooms);
		return (int)Math.round(100.0*pct);
	}
	
	public long leveledDateTime(int level)
	{
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
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
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
		long lastTime=0;
		for(int l=0;l<levelInfo.size();l++)
		{
			if(level==((Integer)levelInfo.elementAt(l,1)).intValue())
			{
				levelInfo.setElementAt(l,2,Long.valueOf(System.currentTimeMillis()));
				levelInfo.setElementAt(l,3,CMLib.map().getExtendedRoomID(R));
				return;
			}
			else
			if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_HOUR)
				return;
			else
			if(level<((Integer)levelInfo.elementAt(l,1)).intValue())
			{
				levelInfo.insertElementAt(l,Integer.valueOf(level),Long.valueOf(System.currentTimeMillis()),CMLib.map().getExtendedRoomID(R));
				return;
			}
			lastTime=((Long)levelInfo.elementAt(l,2)).longValue();
		}
		if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_HOUR)
			return;
		levelInfo.addElement(Integer.valueOf(level),Long.valueOf(System.currentTimeMillis()),CMLib.map().getExtendedRoomID(R));
	}
	
	public int getTotalLegacyLevels()
	{
		int total=0;
		for(Integer value : legacy.values())
			total+=value.intValue();
		return total;
	}
	
	public void addLegacyLevel(String category)
	{
		Integer level=legacy.get(category);
		if(level != null) 
			legacy.put(category, Integer.valueOf(level.intValue()+1));
		else
			legacy.put(category, Integer.valueOf(1));
	}
	
	public int getLegacyLevel(String category)
	{
		Integer level=legacy.get(category);
		if(level != null) return level.intValue();
		return 0;
	}
	
	public PlayerAccount getAccount() { return account;}
	public void setAccount(PlayerAccount account) { this.account = account;}
	public ItemCollection getExtItems() { return extItems; }
	
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
