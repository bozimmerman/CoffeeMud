package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.XMLManager;
import com.planet_ink.coffee_mud.utils.Util;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	private final static int TELL_STACK_MAX_SIZE=50;
	private final static int GTELL_STACK_MAX_SIZE=50;
	
	private HashSet friends=new HashSet();
	private HashSet ignored=new HashSet();
	private Vector tellStack=new Vector();
	private Vector gtellStack=new Vector();
	private String lastIP="";
	private long LastDateTime=System.currentTimeMillis();
	private long lastUpdated=0;
	private int channelMask;
	private String email="";
	private String Password="";
	private String colorStr="";
	private String prompt="";
	private String poofin="";
	private String poofout="";						  
	private String tranpoofin="";
	private String tranpoofout="";						  
	private MOB replyTo=null;
	private Vector securityGroups=new Vector();

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
	
	public String poofIn(){return poofin;}
	public String poofOut(){return poofout;}
	public String tranPoofIn(){return tranpoofin;}
	public String tranPoofOut(){return tranpoofout;}
	
	private String getPrivateList(HashSet h)
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
		return ((f.length()>0)?"<FRIENDS>"+f+"</FRIENDS>":"")
			+((i.length()>0)?"<IGNORED>"+i+"</IGNORED>":"")
			+((poofin.length()>0)?"<POOFIN>"+poofin+"</POOFIN>":"")
			+((poofout.length()>0)?"<POOFOUT>"+poofout+"</POOFOUT>":"")
			+((tranpoofin.length()>0)?"<TRANPOOFIN>"+tranpoofin+"</TRANPOOFIN>":"")
			+((tranpoofout.length()>0)?"<TRANPOOFOUT>"+tranpoofout+"</TRANPOOFOUT>":"")
			+getSecurityGroupStr();
	}
	public void setXML(String str)
	{
		friends=getHashFrom(XMLManager.returnXMLValue(str,"FRIENDS"));
		ignored=getHashFrom(XMLManager.returnXMLValue(str,"IGNORED"));
		setSecurityGroupStr(XMLManager.returnXMLValue(str,"SECGRPS"));
		poofin=XMLManager.returnXMLValue(str,"POOFIN");
		if(poofin==null) poofin="";
		poofout=XMLManager.returnXMLValue(str,"POOFOUT");
		if(poofout==null) poofout="";
		tranpoofin=XMLManager.returnXMLValue(str,"TRANPOOFIN");
		if(tranpoofin==null) tranpoofin="";
		tranpoofout=XMLManager.returnXMLValue(str,"TRANPOOFOUT");
		if(tranpoofout==null) tranpoofout="";
	}
	
	private void setSecurityGroupStr(String grps)
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
	private String getSecurityGroupStr()
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
}
