package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.sysop.CreateEdit;
import com.planet_ink.coffee_mud.commands.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
public class Channels
{
	public static int numChannelsLoaded=0;
	public static Vector channelNames=new Vector();

	public static void channel(MOB mob, Vector commands)
	{
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		commands.removeElementAt(0);
		
		
		long channelNum=0;
		for(int c=0;c<channelNames.size();c++)
		{
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				channelNum=Math.round(Math.pow(2.0,new Integer(c).doubleValue()));
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
		
		for(int s=0;s<MUD.allSessions.size();s++)
		{
			Session ses=(Session)MUD.allSessions.elementAt(s);
			if((!ses.killFlag)&&(ses.mob!=null)&&(!ses.mob.amDead()))
			{
				if(((ses.mob.getChannelMask()&channelNum)==0)&&(ses.mob.location()!=null))
					ses.mob.tell(mob.name()+" "+channelName+"S '"+CommandProcessor.combine(commands,0)+"'");
			}
		}
	}
	public static void nochannel(MOB mob, Vector commands)
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
	
	public static void quiet(MOB mob)
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
