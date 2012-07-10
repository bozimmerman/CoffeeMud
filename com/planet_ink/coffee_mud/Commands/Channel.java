package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2012 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Channel extends StdCommand
{
	public Channel(){}
	public static String[] access=null;
	public String[] getAccessWords()
	{
		if(access!=null) return access;
		access=CMLib.channels().getChannelNames();
		if(access!=null)
		{
			for(int i=0;i<access.length;i++)
				if(access[i].equalsIgnoreCase("AUCTION"))
					access[i]="";
		}
		return access;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>2)&&(commands.firstElement() instanceof Boolean))
		{
			boolean systemMsg=((Boolean)commands.firstElement()).booleanValue();
			String channelName=(String)commands.elementAt(1);
			String message=(String)commands.elementAt(2);
            CMLib.channels().reallyChannel(mob,channelName,message,systemMsg);
			return true;
		}
		return channel(mob, commands, false);
	}

	public boolean channel(MOB mob, Vector commands, boolean systemMsg)
	{
		PlayerStats pstats=mob.playerStats();
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		commands.removeElementAt(0);
		int channelInt=CMLib.channels().getChannelIndex(channelName);
		int channelNum=CMLib.channels().getChannelCodeNumber(channelName);

		if((pstats!=null)&&(CMath.isSet(pstats.getChannelMask(),channelInt)))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(channelName+" has been turned on.  Use `NO"+channelName.toUpperCase()+"` to turn it off again.");
			return false;
		}
		
		if(CMath.bset(mob.getBitmap(),MOB.ATT_QUIET))
		{
			mob.tell("You have QUIET mode on.  You must turn it off first.");
			return false;
		}

		if(commands.size()==0)
		{
			int size = CMLib.channels().getChannelQue(channelInt).size(); 
			if(size>0)
			{
				if(size>5) size=5;
				mob.tell(channelName+" what?  Here's the last "+size+" message(s):\n\r");
				commands.add("LAST");
				commands.add(Integer.toString(size));
			}
			else
			{
				mob.tell(channelName+" what?");
				return false;
			}
		}

		for(int i=0;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(' ')>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		if(!CMLib.masking().maskCheck(CMLib.channels().getChannelMask(channelInt),mob,true))
		{
			mob.tell("This channel is not available to you.");
			return false;
		}
        
		Set<ChannelsLibrary.ChannelFlag> flags=CMLib.channels().getChannelFlags(channelInt);
		if((mob.getClanID().equalsIgnoreCase("")||(!CMLib.clans().authCheck(mob.getClanID(), mob.getClanRole(), Clan.Function.CHANNEL)))
        &&(flags.contains(ChannelsLibrary.ChannelFlag.CLANONLY)||flags.contains(ChannelsLibrary.ChannelFlag.CLANALLYONLY)))
		{
            mob.tell("You can't talk to your clan - you don't have one.");
            return false;
		}
        
		if((commands.size()==2)
		&&(mob.session()!=null)
		&&(((String)commands.firstElement()).equalsIgnoreCase("last"))
		&&(CMath.isNumber((String)commands.lastElement())))
		{
			int num=CMath.s_int((String)commands.lastElement());
			List<ChannelsLibrary.ChannelMsg> que=CMLib.channels().getChannelQue(channelInt);
			boolean showedAny=false;
			if(que.size()>0)
			{
				if(num>que.size()) num=que.size();
				boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
				long elapsedTime=0;
				long now=System.currentTimeMillis();
				for(int i=que.size()-num;i<que.size();i++)
				{
					ChannelsLibrary.ChannelMsg msg=(ChannelsLibrary.ChannelMsg)que.get(i);
					CMMsg modMsg = (CMMsg)msg.msg.copyOf();
					elapsedTime=now-msg.ts;
					elapsedTime=Math.round(elapsedTime/1000L)*1000L;
					if(elapsedTime<0)
					{
						Log.errOut("Channel","Wierd elapsed time: now="+now+", then="+msg.ts);
						elapsedTime=0;
					}
					final String timeAgo = "^.^N ("+CMLib.time().date2SmartEllapsedTime(elapsedTime,false)+" ago)";
					if((modMsg.sourceMessage()!=null)&&(modMsg.sourceMessage().length()>0))
						modMsg.setSourceMessage(modMsg.sourceMessage()+timeAgo);
					if((modMsg.targetMessage()!=null)&&(modMsg.targetMessage().length()>0))
						modMsg.setTargetMessage(modMsg.targetMessage()+timeAgo);
					if((modMsg.othersMessage()!=null)&&(modMsg.othersMessage().length()>0))
						modMsg.setOthersMessage(modMsg.othersMessage()+timeAgo);
					showedAny=CMLib.channels().channelTo(mob.session(),areareq,channelInt,modMsg,modMsg.source())||showedAny;
				}
			}
			if(!showedAny)
			{
				mob.tell("There are no previous entries on this channel.");
				return false;
			}
		}
		else
        if(flags.contains(ChannelsLibrary.ChannelFlag.READONLY))
        {
            mob.tell("This channel is read-only.");
            return false;
        }
        else
        if(flags.contains(ChannelsLibrary.ChannelFlag.PLAYERREADONLY)&&(!mob.isMonster()))
        {
            mob.tell("This channel is read-only.");
            return false;
        }
        else
            CMLib.channels().reallyChannel(mob,channelName,CMParms.combine(commands,0),systemMsg);
		return false;
	}

	
	public boolean canBeOrdered(){return true;}
    public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
}
