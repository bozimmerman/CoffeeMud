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
   Copyright 2003-2024 Bo Zimmerman

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
public class Channel extends StdCommand
{
	public Channel()
	{
	}
	@Override
	public String[] getAccessWords()
	{
		return CMLib.channels().getChannelNames();
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]
	{
		{Boolean.class,String.class,String.class}
	};

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		return channel(mob, commands, false);
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		final int index = getArgumentSetIndex(internalParameters, args);
		if(index == 0)
		{
			final boolean systemMsg=((Boolean)args[0]).booleanValue();
			final String channelName=(String)args[1];
			final String message=(String)args[2];
			CMLib.channels().createAndSendChannelMessage(mob,channelName,message,systemMsg);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public boolean showBacklogMsg(final MOB mob, final long now, final int channelInt, final boolean areareq, final ChannelsLibrary.ChannelMsg msg)
	{
		final CMMsg modMsg = (CMMsg)msg.msg().copyOf();
		long elapsedTime=now-msg.sentTimeMillis();
		elapsedTime=Math.round(elapsedTime/1000L)*1000L;
		if(elapsedTime<0)
		{
			Log.errOut("Channel","Weird elapsed time: now="+now+", then="+msg.sentTimeMillis());
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
		return CMLib.channels().sendChannelCMMsgTo(mob.session(),areareq,channelInt,modMsg,modMsg.source());
	}

	public boolean channel(final MOB mob, final List<String> commands, final boolean systemMsg)
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
		if((chan == null)||(!CMLib.masking().maskCheck(chan.mask(),mob,true)))
		{
			mob.tell(L("This channel is not available to you."));
			return false;
		}

		if(commands.size()==0)
		{
			int size = CMLib.channels().getChannelQue(channelInt, 0, 5, mob).size();
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

		if((commands.size()==1)
		&&("last".startsWith(commands.get(0))||"list".startsWith(commands.get(0))))
		{
			commands.set(0, "last");
			commands.add("10");
		}

		if((commands.size()==1)
		&&("undo".startsWith(commands.get(0))||"delete".startsWith(commands.get(0))))
		{
			final List<ChannelsLibrary.ChannelMsg> que=CMLib.channels().getChannelQue(channelInt, 0, 1, mob);
			if(que.size()>0)
			{
				final ChannelsLibrary.ChannelMsg chanMsg =que.get(que.size()-1);
				final CMMsg msg=chanMsg.msg();
				if((msg!=null)
				&&(msg.source()!=null)
				&&(msg.source().Name().equals(mob.Name())))
				{
					CMLib.database().delBackLogEntry(channelName, chanMsg.sentTimeMillis());
					mob.tell(L("Previous message deleted."));
					return true;
				}
			}
			if(commands.get(0).length()>=4)
			{
				mob.tell(L("You may not delete the last message."));
				return true;
			}
		}

		final String lfw = (commands.size()>1)?commands.get(0).toLowerCase():"";
		if((commands.size()>1)
		&&(mob.session()!=null)
		&&(lfw.startsWith("search:")))
		{
			final String searchTerm = (lfw.substring(7)+" "+CMParms.combine(commands,1)).trim();
			boolean showedAny=false;
			final int max=CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.JOURNALS)?Integer.MAX_VALUE/2:100;
			final List<ChannelsLibrary.ChannelMsg> q=CMLib.channels().searchChannelQue(mob, channelInt, searchTerm, max);
			final ChannelsLibrary clib=CMLib.channels();
			final boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
			final long now=System.currentTimeMillis();
			for(int i=0;i<q.size();i++)
			{
				final ChannelsLibrary.ChannelMsg m=q.get(i);
				if(clib.mayReadThisChannel(m.msg().source(), areareq, mob, channelInt))
					showedAny=this.showBacklogMsg(mob,now,channelInt,areareq,m)||showedAny;
			}
			if(!showedAny)
			{
				mob.tell(L("No messages matching '@x1' found on this channel.",searchTerm));
				return false;
			}
		}
		else
		if((commands.size()==2)
		&&(mob.session()!=null)
		&&(CMath.isNumber(commands.get(commands.size()-1)))
		&&(lfw.length()>0)
		&&(lfw.equals("last")
			||("last".startsWith(lfw))
			||("last".endsWith(lfw))
			||("previous".startsWith(lfw))
			||(CMStrings.sameLetterCount("last",lfw))>2))
		{
			final long now=System.currentTimeMillis();
			final boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
			int num=CMath.s_int(commands.get(commands.size()-1));
			if((num>100)
			&&(!CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.JOURNALS)))
				num=100;
			final ChannelsLibrary clib=CMLib.channels();
			int count=0;
			int page=-1;
			int lastIndex=-1;
			int lastPage=0;
			boolean skippedSome=true;
			final int pageSize=50;
			final int highestNum=CMLib.channels().getChannelQuePageEnd(channelInt, mob);
			while((count < num)
			&&((skippedSome)||((page*pageSize) < (highestNum+2+pageSize))))
			{
				page++;
				skippedSome=false;
				final List<ChannelsLibrary.ChannelMsg> que=CMLib.channels().getChannelQue(channelInt, (page*pageSize), pageSize, mob);
				lastIndex=0;
				for(int i=que.size()-1;i>=0;i--)
				{
					final ChannelsLibrary.ChannelMsg m = que.get(i);
					if(clib.mayReadThisChannel(m.msg().source(), areareq, mob, channelInt,true))
					{
						lastPage=page;
						count++;
						if(count>=num)
						{
							lastIndex=i;
							break;
						}
					}
					else
						skippedSome=true;
					//CMLib.s_sleep(10); //?!
				}
			}
			page=lastPage;
			boolean showedAny=false;
			List<ChannelsLibrary.ChannelMsg> q=CMLib.channels().getChannelQue(channelInt, (page*pageSize), pageSize, mob);
			if(lastIndex<0)
				lastIndex=0;
			for(int i=lastIndex;i<q.size();i++)
			{
				final ChannelsLibrary.ChannelMsg m=q.get(i);
				if(clib.mayReadThisChannel(m.msg().source(), areareq, mob, channelInt))
					showedAny=this.showBacklogMsg(mob,now,channelInt,areareq,m)||showedAny;
			}
			page--;
			while(page >=0)
			{
				q=CMLib.channels().getChannelQue(channelInt, (page*pageSize), pageSize, mob);
				for(int i=0;i<q.size();i++)
				{
					final ChannelsLibrary.ChannelMsg m=q.get(i);
					if(clib.mayReadThisChannel(m.msg().source(), areareq, mob, channelInt,true))
						showedAny=this.showBacklogMsg(mob,now,channelInt,areareq,m)||showedAny;
				}
				page--;
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
