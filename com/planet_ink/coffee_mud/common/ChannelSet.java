package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
/**
  * Global utility Vector for holding and creating Clans
  * @author=Jeremy Vyska
  */
public class ChannelSet
{
	protected static int numChannelsLoaded=0;
	protected static int numIChannelsLoaded=0;
	protected static Vector channelNames=new Vector();
	protected static Vector channelMasks=new Vector();
	protected static Vector ichannelList=new Vector();
	
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
	
	public static int loadChannels(String list, String ilist)
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
			channelNames.addElement(item.toUpperCase().trim());
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
			channelMasks.addElement(lvl);
			ichannelList.addElement(ichan);
		}
		channelNames.addElement(new String("CLANTALK"));
		channelMasks.addElement("");
		ichannelList.addElement("");
		numChannelsLoaded++;

		channelNames.addElement(new String("AUCTION"));
		channelMasks.addElement("");
		ichannelList.addElement("");
		numChannelsLoaded++;

		channelNames.addElement(new String("WIZINFO"));
		channelMasks.addElement("+SYSOP -NAMES");
		ichannelList.addElement("");
		numChannelsLoaded++;
		return numChannelsLoaded;
	}

}
	
