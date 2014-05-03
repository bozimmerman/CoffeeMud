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
   Copyright 2000-2014 Bo Zimmerman

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
	@Override public String[] getAccessWords() { return CMLib.channels().getChannelNames(); }

	private final static Class[][] internalParameters=new Class[][]{{Boolean.class,String.class,String.class}};

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
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
		CMLib.channels().reallyChannel(mob,channelName,message,systemMsg);
		return Boolean.TRUE;
	}

	public boolean channel(MOB mob, Vector commands, boolean systemMsg)
	{
		final PlayerStats pstats=mob.playerStats();
		final String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		commands.removeElementAt(0);
		final int channelInt=CMLib.channels().getChannelIndex(channelName);
		final int channelNum=CMLib.channels().getChannelCodeNumber(channelName);

		if((pstats!=null)&&(CMath.isSet(pstats.getChannelMask(),channelInt)))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(channelName+" has been turned on.  Use `NO"+channelName.toUpperCase()+"` to turn it off again.");
			return false;
		}

		if(CMath.bset(mob.getBitmap(),MOB.ATT_QUIET))
		{
			mob.tell(_("You have QUIET mode on.  You must turn it off first."));
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
			final String s=(String)commands.elementAt(i);
			if(s.indexOf(' ')>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		final ChannelsLibrary.CMChannel chan=CMLib.channels().getChannel(channelInt);
		if(!CMLib.masking().maskCheck(chan.mask,mob,true))
		{
			mob.tell(_("This channel is not available to you."));
			return false;
		}

		final Set<ChannelsLibrary.ChannelFlag> flags=chan.flags;
		if((flags.contains(ChannelsLibrary.ChannelFlag.CLANONLY)||flags.contains(ChannelsLibrary.ChannelFlag.CLANALLYONLY)))
		{
			if(!CMLib.clans().checkClanPrivilege(mob, Clan.Function.CHANNEL))
			{
				mob.tell(_("You can't talk to your clan - you don't have one that allows you."));
				return false;
			}
		}

		if((commands.size()==2)
		&&(mob.session()!=null)
		&&(((String)commands.firstElement()).equalsIgnoreCase("last"))
		&&(CMath.isNumber((String)commands.lastElement())))
		{
			int num=CMath.s_int((String)commands.lastElement());
			final List<ChannelsLibrary.ChannelMsg> que=CMLib.channels().getChannelQue(channelInt);
			boolean showedAny=false;
			if(que.size()>0)
			{
				if(num>que.size()) num=que.size();
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
					final CMMsg modMsg = (CMMsg)msg.msg.copyOf();
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
				mob.tell(_("There are no previous entries on this channel."));
				return false;
			}
		}
		else
		if(flags.contains(ChannelsLibrary.ChannelFlag.READONLY))
		{
			mob.tell(_("This channel is read-only."));
			return false;
		}
		else
		if(flags.contains(ChannelsLibrary.ChannelFlag.PLAYERREADONLY)&&(!mob.isMonster()))
		{
			mob.tell(_("This channel is read-only."));
			return false;
		}
		else
			CMLib.channels().reallyChannel(mob,channelName,CMParms.combine(commands,0),systemMsg);
		return false;
	}


	@Override public boolean canBeOrdered(){return true;}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
}
