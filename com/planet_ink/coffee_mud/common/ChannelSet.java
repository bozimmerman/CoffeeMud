package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class ChannelSet
{
	protected static final int QUEUE_SIZE=100;
	
	protected static int numChannelsLoaded=0;
	protected static int numIChannelsLoaded=0;
	protected static int numImc2ChannelsLoaded=0;
	protected static Vector channelNames=new Vector();
	protected static Vector channelMasks=new Vector();
	protected static Vector ichannelList=new Vector();
	protected static Vector imc2channelList=new Vector();
	protected static Vector channelQue=new Vector();
	
	public static int getNumChannels()
	{
		return channelNames.size();
	}
	
	public static String getChannelMask(int i)
	{
		if((i>=0)&&(i<channelMasks.size()))
			return (String)channelMasks.elementAt(i);
		return "";
	}

	public static String getChannelName(int i)
	{
		if((i>=0)&&(i<channelNames.size()))
			return (String)channelNames.elementAt(i);
		return "";
	}

	public static Vector getChannelQue(int i)
	{
		if((i>=0)&&(i<channelQue.size()))
			return (Vector)channelQue.elementAt(i);
		return new Vector();
	}
	
	public static boolean mayReadThisChannel(MOB sender,
											 boolean areaReq,
											 Session ses, 
											 int i)
	{
		if(ses==null) return false;
		MOB M=ses.mob();
		if(M==null) return false;
		
		if(getChannelName(i).equalsIgnoreCase("CLANTALK")
		&&(sender!=null)
		&&((!sender.getClanID().equals("ALL"))||(M.getClanID().length()==0))
		&&((!M.getClanID().equalsIgnoreCase(sender.getClanID()))||(M.getClanRole()==Clan.POS_APPLICANT)))
			return false;
		
		if((!ses.killFlag())
		&&(!M.amDead())
		&&(M.location()!=null)
		&&((sender==null)
			||(M.playerStats()==null)
			||(!M.playerStats().getIgnored().contains(sender.Name())))
		&&(MUDZapper.zapperCheck(getChannelMask(i),M))
		&&((sender==null)
		   ||(!areaReq)
		   ||(M.location().getArea()==sender.location().getArea()))
		&&(!Util.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}
	
	public static void channelQueUp(int i, CMMsg msg)
	{
		Vector q=getChannelQue(i);
		synchronized(q)
		{
			if(q.size()>=QUEUE_SIZE)
				q.removeElementAt(0);
			q.addElement(msg);
		}
	}
	
	public static int getChannelInt(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return c;
		return -1;
	}

	public static int getChannelNum(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return 1<<c;
		return -1;
	}

	public static String getChannelName(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return ((String)channelNames.elementAt(c)).toUpperCase();
		return "";
	}

	public static void unloadChannels()
	{
		numChannelsLoaded=0;
		numIChannelsLoaded=0;
		channelNames=new Vector();
		channelMasks=new Vector();
		ichannelList=new Vector();
		imc2channelList=new Vector();
		channelQue=new Vector();
	}

	public static String[][] imc2ChannelsArray()
	{
		String[][] array=new String[numImc2ChannelsLoaded][3];
		int num=0;
		for(int i=0;i<channelNames.size();i++)
		{
			String name=(String)channelNames.elementAt(i);
			String mask=(String)channelMasks.elementAt(i);
			String iname=(String)imc2channelList.elementAt(i);
			if((iname!=null)&&(iname.trim().length()>0))
			{
				array[num][0]=iname.trim();
				array[num][1]=name.trim();
				array[num][2]=mask;
				num++;
			}
		}
		return array;
	}
	public static String[][] iChannelsArray()
	{
		String[][] array=new String[numIChannelsLoaded][3];
		int num=0;
		for(int i=0;i<channelNames.size();i++)
		{
			String name=(String)channelNames.elementAt(i);
			String mask=(String)channelMasks.elementAt(i);
			String iname=(String)ichannelList.elementAt(i);
			if((iname!=null)&&(iname.trim().length()>0))
			{
				array[num][0]=iname.trim();
				array[num][1]=name.trim();
				array[num][2]=mask;
				num++;
			}
		}
		return array;
	}
	public static String[] getChannelNames()
	{
		if(channelNames.size()==0) return null;
		return Util.toStringArray(channelNames);
	}
	
	public static int loadChannels(String list, String ilist, String imc2list)
	{
		while(list.length()>0)
		{
			int x=list.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=list.trim();
				list="";
			}
			else
			{
				item=list.substring(0,x).trim();
				list=list.substring(x+1);
			}
			numChannelsLoaded++;
			x=item.indexOf(" ");
			if(item.indexOf(" ")>=0)
			{
				int i=item.indexOf(" ");
				channelMasks.addElement(item.substring(i+1).trim());
				item=item.substring(0,i);
			}
			else
				channelMasks.addElement("");
			ichannelList.addElement("");
			imc2channelList.addElement("");
			channelNames.addElement(item.toUpperCase().trim());
			channelQue.addElement(new Vector());
		}
		while(ilist.length()>0)
		{
			int x=ilist.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=ilist.trim();
				ilist="";
			}
			else
			{
				item=ilist.substring(0,x).trim();
				ilist=ilist.substring(x+1);
			}
			int y1=item.indexOf(" ");
			int y2=item.lastIndexOf(" ");
			if((y1<0)||(y2<=y1)) continue;
			numChannelsLoaded++;
			numIChannelsLoaded++;
			String lvl=item.substring(y1+1,y2).trim();
			String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			channelNames.addElement(item.toUpperCase().trim());
			channelQue.addElement(new Vector());
			channelMasks.addElement(lvl);
			imc2channelList.addElement("");
			ichannelList.addElement(ichan);
		}
		while(imc2list.length()>0)
		{
			int x=imc2list.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=imc2list.trim();
				imc2list="";
			}
			else
			{
				item=imc2list.substring(0,x).trim();
				imc2list=imc2list.substring(x+1);
			}
			int y1=item.indexOf(" ");
			int y2=item.lastIndexOf(" ");
			if((y1<0)||(y2<=y1)) continue;
			numChannelsLoaded++;
			numImc2ChannelsLoaded++;
			String lvl=item.substring(y1+1,y2).trim();
			String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			channelNames.addElement(item.toUpperCase().trim());
			channelQue.addElement(new Vector());
			channelMasks.addElement(lvl);
			imc2channelList.addElement(ichan);
			ichannelList.addElement("");
		}
		channelNames.addElement(new String("CLANTALK"));
		channelQue.addElement(new Vector());
		channelMasks.addElement("");
		ichannelList.addElement("");
		imc2channelList.addElement("");
		numChannelsLoaded++;

		channelNames.addElement(new String("AUCTION"));
		channelQue.addElement(new Vector());
		channelMasks.addElement("");
		ichannelList.addElement("");
		imc2channelList.addElement("");
		numChannelsLoaded++;

		channelNames.addElement(new String("WIZINFO"));
		channelQue.addElement(new Vector());
		channelMasks.addElement("+SYSOP -NAMES "+CommonStrings.getVar(CommonStrings.SYSTEM_WIZINFONAMES));
		ichannelList.addElement("");
		imc2channelList.addElement("");
		numChannelsLoaded++;
		return numChannelsLoaded;
	}

}
	
