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
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelMsg;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Channel extends StdCommand
{
	public Channel(){}
	@Override
	public String[] getAccessWords()
	{
		return CMLib.channels().getChannelNames();
	}

	private final static Class[][] internalParameters=new Class[][]{{Boolean.class,String.class,String.class}};

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		return channel(mob, commands, false);
	}

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		final boolean systemMsg=((Boolean)args[0]).booleanValue();
		final String channelName=(String)args[1];
		final String message=(String)args[2];
		CMLib.channels().createAndSendChannelMessage(mob,channelName,message,systemMsg);
		return Boolean.TRUE;
	}

	public boolean channel(MOB mob, List<String> commands, boolean systemMsg)
	{
		final PlayerStats pstats=mob.playerStats();
		final String channelName=commands.get(0).toUpperCase().trim();
		commands.remove(0);
		final int channelInt=CMLib.channels().getChannelIndex(channelName);
		final int channelNum=CMLib.channels().getChannelCodeNumber(channelName);

		if((pstats!=null)&&(CMath.isSet(pstats.getChannelMask(),channelInt)))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(L("@x1 has been turned on.  Use `NO@x2` to turn it off again.",channelName,channelName.toUpperCase()));
			return false;
		}

		if(mob.isAttributeSet(MOB.Attrib.QUIET))
		{
			mob.tell(L("You have QUIET mode on.  You must turn it off first."));
			return false;
		}

		final ChannelsLibrary.CMChannel chan=CMLib.channels().getChannel(channelInt);
		if(!CMLib.masking().maskCheck(chan.mask(),mob,true))
		{
			mob.tell(L("This channel is not available to you."));
			return false;
		}
		
		if(commands.size()==0)
		{
			int size = CMLib.channels().getChannelQue(channelInt, 0, 5).size();
			if(size>0)
			{
				if(size>5)
					size=5;
				mob.tell(L("@x1 what?  Here's the last @x2 message(s):\n\r",channelName,""+size));
				commands.add("LAST");
				commands.add(Integer.toString(size));
			}
			else
			{
				mob.tell(L("@x1 what?",channelName));
				return false;
			}
		}

		for(int i=0;i<commands.size();i++)
		{
			final String s=commands.get(i);
			if(s.indexOf(' ')>=0)
				commands.set(i,"\""+s+"\"");
		}

		final Set<ChannelsLibrary.ChannelFlag> flags=chan.flags();
		if((flags.contains(ChannelsLibrary.ChannelFlag.CLANONLY)||flags.contains(ChannelsLibrary.ChannelFlag.CLANALLYONLY)))
		{
			if(!CMLib.clans().checkClanPrivilege(mob, Clan.Function.CHANNEL))
			{
				mob.tell(L("You can't talk to your clan - you don't have one that allows you."));
				return false;
			}
		}

		if((commands.size()==2)
		&&(mob.session()!=null)
		&&(commands.get(0).equalsIgnoreCase("last"))
		&&(CMath.isNumber(commands.get(commands.size()-1))))
		{
			int num=CMath.s_int(commands.get(commands.size()-1));
			final List<ChannelsLibrary.ChannelMsg> que=CMLib.channels().getChannelQue(channelInt, 0, num);
			boolean showedAny=false;
			if(que.size()>0)
			{
				if(num>que.size())
					num=que.size();
				final boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
				long elapsedTime=0;
				final long now=System.currentTimeMillis();
				final LinkedList<ChannelsLibrary.ChannelMsg> showThese=new LinkedList<ChannelsLibrary.ChannelMsg>();
				for (final ChannelMsg channelMsg : que)
				{
					showThese.add(channelMsg);
					if(showThese.size()>num)
						showThese.removeFirst();
				}
				for(final ChannelsLibrary.ChannelMsg msg : showThese)
				{
					final CMMsg modMsg = (CMMsg)msg.msg().copyOf();
					elapsedTime=now-msg.sentTimeMillis();
					elapsedTime=Math.round(elapsedTime/1000L)*1000L;
					if(elapsedTime<0)
					{
						Log.errOut("Channel","Wierd elapsed time: now="+now+", then="+msg.sentTimeMillis());
						elapsedTime=0;
					}

					final String timeAgo = "^.^N ("+CMLib.time().date2SmartEllapsedTime(elapsedTime,false)+" ago)";
					if((modMsg.sourceMessage()!=null)&&(modMsg.sourceMessage().length()>0))
						modMsg.setSourceMessage(modMsg.sourceMessage()+timeAgo);
					if((modMsg.targetMessage()!=null)&&(modMsg.targetMessage().length()>0))
						modMsg.setTargetMessage(modMsg.targetMessage()+timeAgo);
					if((modMsg.othersMessage()!=null)&&(modMsg.othersMessage().length()>0))
						modMsg.setOthersMessage(modMsg.othersMessage()+timeAgo);
					if(CMath.bset(modMsg.sourceCode(),CMMsg.MASK_CHANNEL))
						modMsg.setSourceCode(CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt));
					if(CMath.bset(modMsg.targetCode(),CMMsg.MASK_CHANNEL))
						modMsg.setTargetCode(CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt));
					if(CMath.bset(modMsg.othersCode(),CMMsg.MASK_CHANNEL))
						modMsg.setOthersCode(CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt));
					showedAny=CMLib.channels().sendChannelCMMsgTo(mob.session(),areareq,channelInt,modMsg,modMsg.source())||showedAny;
				}
			}
			if(!showedAny)
			{
				mob.tell(L("There are no previous entries on this channel."));
				return false;
			}
		}
		else
		if(flags.contains(ChannelsLibrary.ChannelFlag.READONLY))
		{
			mob.tell(L("This channel is read-only."));
			return false;
		}
		else
		if(flags.contains(ChannelsLibrary.ChannelFlag.PLAYERREADONLY)&&(!mob.isMonster()))
		{
			mob.tell(L("This channel is read-only."));
			return false;
		}
		else
		if(flags.contains(ChannelsLibrary.ChannelFlag.ARCHONREADONLY)&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(L("This channel is read-only."));
			return false;
		}
		
		else
			CMLib.channels().createAndSendChannelMessage(mob,channelName,CMParms.combine(commands,0),systemMsg);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}
}
