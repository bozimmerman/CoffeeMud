package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;

public class DefaultPlayerStats implements PlayerStats
{
	private Hashtable friends=new Hashtable();
	private Hashtable ignored=new Hashtable();
	private String lastIP="";
	private long LastDateTime=System.currentTimeMillis();
	private long lastUpdated=0;
	private int channelMask;
	private String email="";
	private String Password="";
	private String colorStr="";
	private String prompt="";
	private MOB replyTo=null;
	
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

	public Hashtable getHashFrom(String str)
	{
		Hashtable h=new Hashtable();
		if((str==null)||(str.length()==0)) return h;
		int x=str.indexOf(";");
		while(x>=0)
		{
			String fi=str.substring(0,x).trim();
			if(fi.length()>0) h.put(fi,fi);
			str=str.substring(x+1);
			x=str.indexOf(";");
		}
		if(str.trim().length()>0)
			h.put(str.trim(),str.trim());
		return h;
	}

	private String getMyMsg(String fil, String subject)
	{
		int x=fil.indexOf("<"+subject+">");
		if(x<0) return "";
		fil=fil.substring(x+2+subject.length());
		x=fil.indexOf("</"+subject+">");
		if(x<0) return "";
		fil=fil.substring(0,x);
		return fil;
	}
	
	public Hashtable getFriends(){return friends;}
	public Hashtable getIgnored(){return ignored;}
	
	private String getPrivateList(Hashtable h)
	{
		if((h==null)||(h.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Enumeration e=h.elements();e.hasMoreElements();)
			list.append(((String)e.nextElement())+";");
		return list.toString();
	}
	/** When the USER last logged off */
	/** User PASSWORD */
	/** update USER information */
	public String getFriendsIgnoreStr(){
		return "<FRIENDS>"+getPrivateList(getFriends())+"</FRIENDS>"
			+"<IGNORED>"+getPrivateList(getIgnored())+"</IGNORED>";
	}
	public void setFriendsIgnoreStr(String str)
	{
		friends=getHashFrom(getMyMsg(str,"FRIENDS"));
		ignored=getHashFrom(getMyMsg(str,"IGNORE"));
	}
}
