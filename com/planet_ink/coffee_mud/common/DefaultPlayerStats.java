package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.XMLManager;
import com.planet_ink.coffee_mud.utils.Util;
import java.util.*;

public class DefaultPlayerStats implements PlayerStats
{
	private HashSet friends=new HashSet();
	private HashSet ignored=new HashSet();
	private String lastIP="";
	private long LastDateTime=System.currentTimeMillis();
	private long lastUpdated=0;
	private int channelMask;
	private String email="";
	private String Password="";
	private String colorStr="";
	private String prompt="";
	private MOB replyTo=null;
	private Vector securityGroups=null;
	
	public String lastIP(){return lastIP;}
	public void setLastIP(String ip){lastIP=ip;}
	public String getEmail(){if(email==null) return ""; return email;}
	public void setEmail(String newAdd){email=newAdd;}
	public long lastUpdated(){return lastUpdated;}
	public void setUpdated(long time){lastUpdated=time;}
	public long lastDateTime(){return LastDateTime;}
	public void setLastDateTime(long C){ LastDateTime=C;}
	public String password(){return Password;}
	public void setPassword(String newPassword){Password=newPassword;}
	public void setChannelMask(int newMask){ channelMask=newMask;}
	public int getChannelMask(){ return channelMask;}
	public MOB replyTo(){	return replyTo;	}
	public void setReplyTo(MOB mob){	replyTo=mob;	}
	public void setPrompt(String newPrompt){prompt=newPrompt;}
	public String getColorStr(){return colorStr;}
	public void setColorStr(String newColors){colorStr=newColors;}
	public String getPrompt()
	{
		if((prompt==null)||(prompt.length()==0))
			return defaultPrompt;
		else
			return prompt;
	}

	public HashSet getHashFrom(String str)
	{
		HashSet h=new HashSet();
		if((str==null)||(str.length()==0)) return h;
		str=Util.replaceAll(str,"<FRIENDS>","");
		str=Util.replaceAll(str,"<IGNORED>","");
		str=Util.replaceAll(str,"</FRIENDS>","");
		str=Util.replaceAll(str,"</IGNORED>","");
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
	
	private String getPrivateList(HashSet h)
	{
		if((h==null)||(h.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator e=h.iterator();e.hasNext();)
			list.append(((String)e.next())+";");
		return list.toString();
	}
	public String getFriendsIgnoreStr()
	{
		String f=getPrivateList(getFriends());
		String i=getPrivateList(getIgnored());
		return ((f.length()>0)?"<FRIENDS>"+f+"</FRIENDS>":"")
			+((i.length()>0)?"<IGNORED>"+i+"</IGNORED>":"");
	}
	public void setFriendsIgnoreStr(String str)
	{
		friends=getHashFrom(XMLManager.returnXMLValue(str,"FRIENDS"));
		ignored=getHashFrom(XMLManager.returnXMLValue(str,"IGNORED"));
	}
	
	public void setSecurityGroupXml(String grps)
	{
		securityGroups=null;
		if((grps==null)||(grps.trim().length()==0))	
			return;
		setSecurityGroupXml(XMLManager.returnXMLValue(grps,"FRIENDS"));
	}
	public void setSecurityGroupStr(String grps)
	{
		securityGroups=null;
		if((grps==null)||(grps.trim().length()==0))	
			return;
		securityGroups=new Vector();
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
	public String getSecurityGroupStr()
	{
		if((securityGroups==null)||(securityGroups.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator e=securityGroups.iterator();e.hasNext();)
			list.append(((String)e.next())+";");
		return "<SECGRPS>"+list.toString()+"</SECGRPS>";
		
	}
	public Vector getSecurityGroups(){	return securityGroups;}
}
