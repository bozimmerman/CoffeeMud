package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Channel extends BaseChanneler
{
	public Channel(){}
	public static String[] access=null;
	public String[] getAccessWords()
	{
		if(access!=null) return access;
		access=ChannelSet.getChannelNames();
		if(access!=null)
		{
			for(int i=0;i<access.length;i++)
				if(access[i].equalsIgnoreCase("AUCTION"))
					access[i]="";
		}
		return access;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()>2)&&(commands.firstElement() instanceof Boolean))
		{
			boolean systemMsg=((Boolean)commands.firstElement()).booleanValue();
			String channelName=(String)commands.elementAt(1);
			String message=(String)commands.elementAt(2);
			reallyChannel(mob,channelName,message,systemMsg);
			return true;
		}
		else
			return channel(mob, commands, false);
	}

	public boolean channel(MOB mob, Vector commands, boolean systemMsg)
	{
		PlayerStats pstats=mob.playerStats();
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		commands.removeElementAt(0);
		int channelInt=ChannelSet.getChannelInt(channelName);
		int channelNum=ChannelSet.getChannelNum(channelName);

		if((pstats!=null)&&(Util.isSet(pstats.getChannelMask(),channelInt)))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(channelName+" has been turned on.  Use `NO"+channelName.toUpperCase()+"` to turn it off again.");
			return false;
		}

		if(commands.size()==0)
		{
			mob.tell(channelName+" what?");
			return false;
		}

		for(int i=0;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		if(!MUDZapper.zapperCheck(ChannelSet.getChannelMask(channelInt),mob))
		{
			mob.tell("This channel is not available to you.");
			return false;
		}

		if((mob.getClanID().equalsIgnoreCase(""))&&(channelName.equalsIgnoreCase("CLANTALK")))
		{
		  mob.tell("You can't talk to your clan - you don't have one.");
		  return false;
		}
		if((commands.size()==2)
		&&(mob.session()!=null)
		&&(((String)commands.firstElement()).equalsIgnoreCase("last"))
		&&(Util.isNumber((String)commands.lastElement())))
		{
			int num=Util.s_int((String)commands.lastElement());
			Vector que=ChannelSet.getChannelQue(channelInt);
			if(que.size()==0)
			{
				mob.tell("There are no previous entries on this channel.");
				return false;
			}
			if(num>que.size()) num=que.size();
			boolean areareq=ChannelSet.getChannelMask(channelInt).toUpperCase().indexOf("SAMEAREA")>=0;
			for(int i=que.size()-num;i<que.size();i++)
			{
				CMMsg msg=(CMMsg)que.elementAt(i);
				channelTo(mob.session(),areareq,channelInt,msg,msg.source());
			}
		}
		else
			reallyChannel(mob,channelName,Util.combine(commands,0),systemMsg);
		return false;
	}

	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
