package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.sysop.CreateEdit;
import com.planet_ink.coffee_mud.commands.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
public class Channels
{
	private int numChannelsLoaded=0;
	private Vector channelNames=new Vector();

	public void listChannels(MOB mob)
	{
		StringBuffer buf=new StringBuffer("Available channels: \n\r");
		int col=0;
		for(int x=0;x<channelNames.size();x++)
		{
			if((++col)>3)
			{
				buf.append("\n\r");
				col=1;
			}
			String channelName=(String)channelNames.elementAt(x);
			String onoff="";
			if(Util.bset((int)mob.getChannelMask(),x+1))
				onoff=" (OFF)";
			buf.append(Util.padRight(channelName+onoff,24));
		}
		if(channelNames.size()==0) buf.append("None!");
		mob.tell(buf.toString());
	}
	
	public int loadChannels(String list, CommandSet cmdSet)
	{
		while(list.length()>0)
		{
			int x=list.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=list;
				list="";
			}
			else
			{
				item=list.substring(0,x).trim();
				list=list.substring(x+1);
			}
			numChannelsLoaded++;
			channelNames.addElement(item.toUpperCase().trim());
			//extraCMDs.addElement(item.toUpperCase().trim());
			cmdSet.put(item.toUpperCase().trim(),new Integer(CommandSet.CHANNEL));
			//extraCMDs.addElement("NO"+item.toUpperCase().trim());
			cmdSet.put("NO"+item.toUpperCase().trim(),new Integer(CommandSet.NOCHANNEL));
		}
		return numChannelsLoaded;
	}

	public void channel(MOB mob, Vector commands)
	{
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		commands.removeElementAt(0);


		long channelNum=0;
		int channelInt=0;
		for(int c=0;c<channelNames.size();c++)
		{
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
			{
				channelNum=Math.round(Util.pow(2,c));
				channelInt=c+1;
				break;
			}
		}

		if((mob.getChannelMask()&channelNum)==channelNum)
		{
			mob.setChannelMask(mob.getChannelMask()&(mob.getChannelMask()-channelNum));
			mob.tell(channelName+" has been turned back on.");
			return;
		}

		if(commands.size()==0)
		{
			mob.tell(channelName+" what?");
			return;
		}

		String str=" "+channelName+"S '"+Util.combine(commands,0)+"'";
		FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_OK_ACTION,"You"+str,Affect.NO_EFFECT,null,channelInt,mob.name()+str);
		if(mob.location().okAffect(msg))
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session ses=(Session)Sessions.elementAt(s);
				if((!ses.killFlag())&&(ses.mob()!=null)
				&&(!ses.mob().amDead())
				&&(ses.mob().location()!=null)
				&&(ses.mob().okAffect(msg)))
					ses.mob().affect(msg);
			}
		}
	}
	public void nochannel(MOB mob, Vector commands)
	{
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim().substring(2);
		commands.removeElementAt(0);


		long channelNum=0;
		for(int c=0;c<channelNames.size();c++)
		{
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				channelNum=Math.round(Math.pow(2.0,new Integer(c).doubleValue()));
		}

		if((mob.getChannelMask()&channelNum)==0)
			mob.setChannelMask(mob.getChannelMask()|channelNum);
		mob.tell("The "+channelName+" channel has been turned off.");
	}

	public void quiet(MOB mob)
	{
		long channelNum=0;
		boolean turnedoff=false;
		for(int c=0;c<channelNames.size();c++)
		{
			channelNum=Math.round(Math.pow(2.0,new Integer(c).doubleValue()));
			if((mob.getChannelMask()&channelNum)==0)
			{
				mob.setChannelMask(mob.getChannelMask()|channelNum);
				turnedoff=true;
			}
		}
		if(turnedoff)
			mob.tell("All channels have been turned off.");
		else
		{
			for(int c=0;c<channelNames.size();c++)
			{
				channelNum=Math.round(Math.pow(2.0,new Integer(c).doubleValue()));
				String channelName=(String)channelNames.elementAt(c);
				if((mob.getChannelMask()&channelNum)==channelNum)
				{
					mob.setChannelMask(mob.getChannelMask()&(mob.getChannelMask()-channelNum));
					mob.tell(channelName+" has been turned back on.");
				}
			}
		}
	}
}
