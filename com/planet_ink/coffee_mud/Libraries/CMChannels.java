package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMLib.Library;
import com.planet_ink.coffee_mud.core.CMProps.Bool;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelMsg;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/*
   Copyright 2005-2024 Bo Zimmerman

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
public class CMChannels extends StdLibrary implements ChannelsLibrary
{
	@Override
	public String ID()
	{
		return "CMChannels";
	}

	public final int QUEUE_SIZE=100;

	public String[]			baseChannelNames= new String[0];
	public List<CMChannel>	channelList		= new Vector<CMChannel>();

	protected Language		commonLang		= null;

	protected final static List<ChannelMsg> emptyQueue=new ReadOnlyList<ChannelMsg>(new Vector<ChannelMsg>(1));
	protected final static Set<ChannelFlag> emptyFlags=new ReadOnlySet<ChannelFlag>(new HashSet<ChannelFlag>(1));

	protected final static Map<Object, List<CMChannels>> discordLibMap = new Hashtable<Object,List<CMChannels>>();
	protected static ClassLoader			discordClassLoader	= null;
	protected static Object					discordApi			= null;
	protected DoubleMap<CMChannel, Object>	discordChannels		= new DoubleMap<CMChannel, Object>(SHashtable.class);
	protected LimitedTreeSet<String>		lastDiscordMsgs		= new LimitedTreeSet<String>(10000,100,false,true);

	@Override
	public int getNumChannels()
	{
		return channelList.size();
	}

	protected Enumeration<CMChannel> channels()
	{
		return new IteratorEnumeration<CMChannel>(channelList.iterator());
	}

	@Override
	public CMChannel getChannel(final int channelNumber)
	{
		if((channelNumber>=0)&&(channelNumber<channelList.size()))
			return channelList.get(channelNumber);
		return null;
	}

	@Override
	public CMChannel getChannelFromMsg(final CMMsg msg)
	{
		int channelCode;
		if(CMath.bset(msg.othersMajor(), CMMsg.MASK_CHANNEL))
			channelCode = msg.othersMinor() - CMMsg.TYP_CHANNEL;
		else
		if(CMath.bset(msg.targetMajor(), CMMsg.MASK_CHANNEL))
			channelCode = msg.targetMinor() - CMMsg.TYP_CHANNEL;
		else
		if(CMath.bset(msg.sourceMajor(), CMMsg.MASK_CHANNEL))
			channelCode = msg.sourceMajor() - CMMsg.TYP_CHANNEL;
		else
			return null;
		return getChannel(channelCode);
	}

	@Override
	public CMChannel createNewChannel(final String name, final String i3Name, final String imc2Name,
									  final String mask, final Set<ChannelFlag> flags, final String disName,
									  final String colorOverrideANSI, final String colorOverrideWords)
	{
		final SLinkedList<ChannelMsg> queue = new SLinkedList<ChannelMsg>();
		return new CMChannel()
		{
			@Override
			public String name()
			{
				return name;
			}

			@Override
			public String i3name()
			{
				return i3Name;
			}

			@Override
			public String imc2Name()
			{
				return imc2Name;
			}

			@Override
			public String discordName()
			{
				return disName;
			}

			@Override
			public String mask()
			{
				return mask;
			}

			@Override
			public String colorOverrideANSICodes()
			{
				return colorOverrideANSI;
			}

			@Override
			public String colorOverrideWords()
			{
				return colorOverrideWords;
			}

			@Override
			public Set<ChannelFlag> flags()
			{
				return flags;
			}

			@Override
			public SLinkedList<ChannelMsg> queue()
			{
				return queue;
			}
		};
	}

	@Override
	public int getChannelQuePageEnd(final int channelNumber, final MOB mob)
	{
		if((channelNumber>=0)
		&&(channelNumber<channelList.size()))
		{
			final CMChannel channel = channelList.get(channelNumber);
			final int subNameField = this.getExtraChannelDataField(mob, channel);
			return CMLib.database().getBackLogPageEnd(channel.name(), subNameField);
		}
		return -1;
	}

	@Override
	public int getChannelQueIndex(final int channelNumber, final MOB mob, final long oldestDateMs)
	{
		if((channelNumber>=0)
		&&(channelNumber<channelList.size()))
		{
			final CMChannel channel = channelList.get(channelNumber);
			final int subNameField = this.getExtraChannelDataField(mob, channel);
			return CMLib.database().getLowestBackLogIndex(channel.name(), subNameField, oldestDateMs);
		}
		return -1;
	}

	@Override
	public List<ChannelMsg> searchChannelQue(final MOB mob, final int channelNumber, final String srchTerms, final int maxReturn)
	{
		if((channelNumber>=0)
		&&(channelNumber<channelList.size()))
		{
			final String srch = srchTerms.toLowerCase().trim();
			final CMChannel channel = channelList.get(channelNumber);
			if(channel.flags().contains(ChannelsLibrary.ChannelFlag.NOBACKLOG))
			{
				final LinkedList<ChannelMsg> msgs=new LinkedList<ChannelMsg>();
				for(final ChannelMsg msg : channel.queue())
				{
					if(msg.msg().othersMessage().toLowerCase().indexOf(srch)>=0)
					{
						msgs.add(msg);
						if(msgs.size()>=maxReturn)
							return msgs;
					}
				}
				return msgs;
			}
			final int subNameField = this.getExtraChannelDataField(mob, channel);
			final List<Triad<String,Integer,Long>> backLog=CMLib.database().searchBackLogEntries(channel.name(), subNameField, srch, maxReturn);
			return this.convertBackLogEntries(mob, channel, subNameField, backLog);
		}
		return emptyQueue;
	}

	protected List<ChannelMsg> convertBackLogEntries(final MOB mob, final CMChannel channel, final int subNameField, final List<Triad<String,Integer,Long>> backLog)
	{
		final List<ChannelMsg> allMsgs = new XVector<ChannelMsg>();
		for(int x=0;x<backLog.size();x++)
		{
			final Triad<String,Integer,Long> p = backLog.get(x);
			final CMMsg msg=CMClass.getMsg();
			final String codedMsgStr;
			if(p.first.startsWith("<EXTRA>"))
			{
				final int y=p.first.indexOf("</EXTRA>");
				if(y<0)
					continue;
				codedMsgStr = p.first.substring(y+8);
			}
			else
				codedMsgStr = p.first;
			msg.parseFlatString(codedMsgStr);
			if((subNameField != 0)
			&&(msg.source().isMonster()))
			{
				if((channel.flags().contains(ChannelFlag.CLANALLYONLY)||(channel.flags().contains(ChannelFlag.CLANONLY)))
				&&(msg.source().isMonster())
				&&(!msg.source().clans().iterator().hasNext()))
				{
					for(final Pair<Clan,Integer> c : mob.clans())
						msg.source().setClan(c.first.clanID(), c.second.intValue());
				}
				else
				if(channel.flags().contains(ChannelFlag.SAMEAREA))
					msg.source().setLocation(mob.location());
			}

			final long time = p.third.longValue();
			allMsgs.add(new ChannelMsg()
			{
				@Override
				public CMMsg msg()
				{
					return msg;
				}

				@Override
				public long sentTimeMillis()
				{
					return time;
				}

				@Override
				public int subNameField()
				{
					return subNameField;
				}
			});
		}
		return allMsgs;
	}

	@Override
	public List<ChannelMsg> getChannelQue(final int channelNumber, final int numNewToSkip, final int numToReturn, final MOB mob)
	{
		if((channelNumber>=0)
		&&(channelNumber<channelList.size()))
		{
			final CMChannel channel = channelList.get(channelNumber);
			final LinkedList<ChannelMsg> msgs=new LinkedList<ChannelMsg>();
			if(numNewToSkip < channel.queue().size())
			{
				int skipNum=numNewToSkip;
				for(final ChannelMsg msg : channel.queue())
				{
					if((--skipNum < 0)&&(msgs.size() < numToReturn))
						msgs.addFirst(msg);
				}
			}

			if(msgs.size()>=numToReturn)
				return msgs;
			if(channel.flags().contains(ChannelsLibrary.ChannelFlag.NOBACKLOG))
				return msgs;
			final int subNameField = this.getExtraChannelDataField(mob, channel);
			final List<Triad<String,Integer,Long>> backLog=CMLib.database().getBackLogEntries(channel.name(), subNameField, numNewToSkip, numToReturn);
			if(backLog.size()<=msgs.size())
				return msgs;

			final int numRedundantIndex = msgs.size();
			for(int i=0;i<numRedundantIndex;i++)  // we already have these, so just cut them.
				backLog.remove(backLog.size()-1);

			final List<ChannelMsg> allMsgs = this.convertBackLogEntries(mob, channel, subNameField, backLog);
			allMsgs.addAll(msgs);
			return allMsgs;
		}
		return emptyQueue;
	}

	@Override
	public boolean mayReadThisChannel(final MOB sender, final boolean areaReq, final MOB M, final int channelNumber)
	{
		return mayReadThisChannel(sender,areaReq,M,channelNumber,false);
	}

	@Override
	public boolean mayReadThisChannel(final MOB sender,
									  final boolean areaReq,
									  final MOB M,
									  final int channelNumber,
									  final boolean offlineOK)
	{
		if((sender==null)||(M==null))
			return false;
		final PlayerStats pstats=M.playerStats();
		if(pstats==null)
			return false;
		final Room R=M.location();
		if(((!offlineOK))
		&&((M.amDead())||(R==null)))
			return false;
		final CMChannel chan=getChannel(channelNumber);
		if(chan==null)
			return false;
		if(chan.flags().contains(ChannelFlag.CLANONLY)||chan.flags().contains(ChannelFlag.CLANALLYONLY))
		{
			// only way to fail an all-clan send is to have NO clan.
			if(!CMLib.clans().checkClanPrivilege(M, Clan.Function.CHANNEL))
				return false;

			if((!CMLib.clans().isAnyCommonClan(sender,M))
			&&((!chan.flags().contains(ChannelFlag.CLANALLYONLY))
				||(!CMLib.clans().findAnyClanRelations(M,sender,Clan.REL_ALLY))))
				return false;
		}

		if((!pstats.isIgnored(sender))
		&&(CMLib.masking().maskCheck(chan.mask(),M,true))
		&&((!areaReq)
		   ||(sender.location()==null)
		   ||(R==null)
		   ||(R.getArea()==sender.location().getArea()))
		&&(!CMath.isSet(pstats.getChannelMask(),channelNumber)))
			return true;
		return false;
	}

	@Override
	public boolean mayReadThisChannel(final MOB sender, final boolean areaReq, final Session ses, final int channelNumber)
	{
		if(ses==null)
			return false;
		final MOB M=ses.mob();

		if((sender==null)
		||(M==null)
		||(M.amDead())
		||(M.location()==null))
			return false;
		final PlayerStats pstats=M.playerStats();
		if(pstats==null)
			return false;
		String senderName=sender.Name();
		final int x=senderName.indexOf('@');
		if(x>0)
			senderName=senderName.substring(0,x);
		final CMChannel chan=getChannel(channelNumber);
		if(chan==null)
			return false;
		if(chan.flags().contains(ChannelFlag.CLANONLY)||chan.flags().contains(ChannelFlag.CLANALLYONLY))
		{
			// only way to fail an all-clan send is to have NO clan.
			if(!CMLib.clans().checkClanPrivilege(M, Clan.Function.CHANNEL))
				return false;
			if((!CMLib.clans().isAnyCommonClan(sender,M))
			&&((!chan.flags().contains(ChannelFlag.CLANALLYONLY))
				||(!CMLib.clans().findAnyClanRelations(M,sender,Clan.REL_ALLY))))
				return false;
		}

		final Room R=M.location();
		if((!ses.isStopped())
		&&(R!=null)
		&&(!pstats.isIgnored(sender))
		&&(!pstats.isIgnored(senderName))
		&&(CMLib.masking().maskCheck(chan.mask(),M,true))
		&&((!areaReq)
		   ||(sender.location()==null)
		   ||(R.getArea()==sender.location().getArea()))
		&&(!CMath.isSet(pstats.getChannelMask(),channelNumber)))
			return true;
		return false;
	}

	@Override
	public boolean mayReadThisChannel(final MOB M, final int channelNumber, final boolean zapCheckOnly)
	{
		if(M==null)
			return false;

		if(channelNumber>=getNumChannels())
			return false;

		final CMChannel chan=getChannel(channelNumber);
		if(chan==null)
			return false;
		if((chan.flags().contains(ChannelFlag.CLANONLY)||chan.flags().contains(ChannelFlag.CLANALLYONLY))
		&&(!CMLib.clans().checkClanPrivilege(M, Clan.Function.CHANNEL)))
			return false;

		if(((zapCheckOnly)||((!M.amDead())&&(M.location()!=null)))
		&&(CMLib.masking().maskCheck(chan.mask(),M,true))
		&&(!CMath.isSet(M.playerStats().getChannelMask(),channelNumber)))
			return true;
		return false;
	}

	@Override
	public void channelQueUp(final int channelNumber, final CMMsg msg, final int subNameField)
	{
		CMLib.map().sendGlobalMessage(msg.source(),CMMsg.TYP_CHANNEL,msg);
		final CMChannel channel=getChannel(channelNumber);
		final SLinkedList<ChannelMsg> q=channel.queue();
		final long now;
		synchronized(q)
		{
			if(q.size()>=QUEUE_SIZE)
				q.removeLast();
			now = System.currentTimeMillis();
			q.addFirst(new ChannelMsg()
			{
				@Override
				public CMMsg msg()
				{
					return msg;
				}

				@Override
				public long sentTimeMillis()
				{
					return now;
				}

				@Override
				public int subNameField()
				{
					return subNameField;
				}
			});
		}
		if((!channel.flags().contains(ChannelsLibrary.ChannelFlag.NOBACKLOG))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CHANNELBACKLOGS))
		&&(!CMProps.getVar(CMProps.Str.CHANNELBACKLOG).equals("0")))
		{
			try
			{
				CMLib.database().addBackLogEntry(getChannel(channelNumber).name(), subNameField, now, msg.toFlatString());
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
		}
	}

	@Override
	public int getChannelIndex(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(int c=0;c<channelList.size();c++)
		{
			if((channelList.get(c)).name().equals(channelName))
				return c;
		}
		for(int c=0;c<channelList.size();c++)
		{
			if((channelList.get(c)).name().startsWith(channelName))
				return c;
		}
		return -1;
	}

	@Override
	public int getChannelCodeNumber(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(int c=0;c<channelList.size();c++)
		{
			if((channelList.get(c)).name().equals(channelName))
				return 1<<c;
		}
		for(int c=0;c<channelList.size();c++)
		{
			if((channelList.get(c)).name().startsWith(channelName))
				return 1<<c;
		}
		return -1;
	}

	@Override
	public String findChannelName(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(int c=0;c<channelList.size();c++)
		{
			if((channelList.get(c)).name().equals(channelName))
				return (channelList.get(c)).name().toUpperCase();
		}
		for(int c=0;c<channelList.size();c++)
		{
			if((channelList.get(c)).name().startsWith(channelName))
				return (channelList.get(c)).name().toUpperCase();
		}
		return "";
	}

	@Override
	public CMChannel getChannel(final String channelName)
	{
		for(int c=0;c<channelList.size();c++)
		{
			if((channelList.get(c)).name().equals(channelName))
				return channelList.get(c);
		}
		return null;
	}

	@Override
	public List<String> getFlaggedChannelNames(final ChannelFlag flag, final MOB mob)
	{
		final List<String> channels=new Vector<String>();
		for(int c=0;c<channelList.size();c++)
		{
			final CMChannel chan=channelList.get(c);
			if((chan!=null)
			&&(chan.flags().contains(flag)))
			{
				if((mob==null)
				||(!mob.isAttributeSet(Attrib.PRIVACY)))
					channels.add(chan.name().toUpperCase());
				else // mob is not null, and privacy is turned on, so readability is the key -- what they don't know can't hurt them.
				if(!CMLib.masking().maskCheck(chan.mask(),mob,true))
					channels.add(chan.name().toUpperCase());
			}
		}
		return channels;
	}

	@Override
	public String getExtraChannelDesc(final String channelName)
	{
		final StringBuilder str=new StringBuilder("");
		final int dex = getChannelIndex(channelName);
		if(dex >= 0)
		{
			final CMChannel chan=getChannel(dex);
			final Set<ChannelFlag> flags = chan.flags();
			final String mask = chan.mask();
			if(flags.contains(ChannelFlag.CLANALLYONLY))
				str.append(L(" This is a channel for clans and their allies."));
			if(flags.contains(ChannelFlag.CLANONLY))
				str.append(L(" Only members of the same clan can see messages on this channel."));
			if(flags.contains(ChannelFlag.PLAYERREADONLY)
			||flags.contains(ChannelFlag.READONLY)
			||flags.contains(ChannelFlag.ARCHONREADONLY))
				str.append(L(" This channel is read-only."));
			if(flags.contains(ChannelFlag.SAMEAREA))
				str.append(L(" Only people in the same area can see messages on this channel."));
			if((mask!=null)&&(mask.trim().length()>0))
				str.append(L(" The following may read this channel : @x1",CMLib.masking().maskDesc(mask)));
		}
		return str.toString();
	}

	private void clearChannels()
	{
		channelList = new Vector<CMChannel>();
		discordChannels = new DoubleMap<CMChannel, Object>(SHashtable.class);
		for(final Object key : discordLibMap.keySet())
			discordLibMap.get(key).remove(this);
	}

	@Override
	public List<CMChannel> getIMC2ChannelsList()
	{
		final List<CMChannel> list=new Vector<CMChannel>();
		for(int i=0;i<channelList.size();i++)
		{
			if((channelList.get(i).imc2Name()!=null)
			&&(channelList.get(i).imc2Name().length()>0))
				list.add(channelList.get(i));
		}
		return list;
	}

	@Override
	public List<CMChannel> getI3ChannelsList()
	{
		final List<CMChannel> list=new Vector<CMChannel>();
		for(int i=0;i<channelList.size();i++)
		{
			if((channelList.get(i).i3name()!=null)
			&&(channelList.get(i).i3name().length()>0))
				list.add(channelList.get(i));
		}
		return list;
	}

	@Override
	public String[] getChannelNames()
	{
		return baseChannelNames;
	}

	@Override
	public List<Session> clearInvalidSnoopers(final Session mySession, final int channelCode)
	{
		List<Session> invalid=null;
		if(mySession!=null)
		{
			for(final Session S : CMLib.sessions().allIterable())
			{
				if((S!=mySession)
				&&(S.mob()!=null)
				&&(mySession.isBeingSnoopedBy(S))
				&&(!mayReadThisChannel(S.mob(),channelCode,false)))
				{
					if(invalid==null)
						invalid=new Vector<Session>();
					invalid.add(S);
					mySession.setBeingSnoopedBy(S,false);
				}
			}
		}
		return invalid;
	}

	@Override
	public void restoreInvalidSnoopers(final Session mySession, final List<Session> invalid)
	{
		if((mySession==null)||(invalid==null))
			return;
		for(int s=0;s<invalid.size();s++)
			mySession.setBeingSnoopedBy(invalid.get(s), true);
	}

	protected String parseOutFlags(final String mask, final Set<ChannelFlag> flags, final String[] colorOverride, final String[] discordChannel)
	{
		final List<String> V=CMParms.parseSpaces(mask,true);
		for(int i=V.size()-1;i>=0;i--)
		{
			final String vs=V.get(i);
			final String s;
			final String v;
			final int x = vs.indexOf('=');
			if(x<0)
			{
				s=vs.toUpperCase();
				v="";
			}
			else
			{
				s=vs.substring(0,x).toUpperCase().trim();
				v=vs.substring(x+1);
			}
			if(CMParms.contains(CMParms.toStringArray(ChannelFlag.values()), s))
			{
				V.remove(i);
				final ChannelFlag flag = ChannelFlag.valueOf(s);
				flags.add(flag);
				if((flag==ChannelFlag.DISCORD)&&(v.length()>0))
					discordChannel[0]=v;
			}
			else
			{
				final Color C=(Color)CMath.s_valueOf(Color.class, s);
				if(C!=null)
				{
					V.remove(i);
					if(s.startsWith("BG"))
						colorOverride[0]=colorOverride[0]+C.getANSICode();
					else
						colorOverride[0]=C.getANSICode()+colorOverride[0];
					colorOverride[1]+=" "+s;
				}
			}
		}
		final StringBuilder str=new StringBuilder();
		for(final String s : V)
			str.append(s).append(" ");
		return str.toString().trim();
	}

	@Override
	public int loadChannels(String list, String ilist, String imc2list)
	{
		clearChannels();
		while(list.length()>0)
		{
			int x=list.indexOf(',');

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
			x=item.indexOf(' ');
			final CMChannel chan;
			if(x>0)
			{
				final String[] colorOverride=new String[]{"",""};
				final Set<ChannelFlag> flags = new HashSet<ChannelFlag>();
				final String[] discordChan = new String[] {""};
				final String mask=parseOutFlags(item.substring(x+1).trim(),flags,colorOverride,discordChan);
				item=item.substring(0,x);
				chan = this.createNewChannel(item.toUpperCase().trim(), "", "", mask, flags,
												discordChan[0],  colorOverride[0], colorOverride[1]);
			}
			else
				chan = createNewChannel(item.toUpperCase().trim(), "", "", "", new HashSet<ChannelFlag>(), "", "", "");
			channelList.add(chan);
		}
		while(ilist.length()>0)
		{
			final int x=ilist.indexOf(',');

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
			final int y1=item.indexOf(' ');
			final int y2=item.lastIndexOf(' ');
			if((y1<0)||(y2<=y1))
				continue;
			final String lvl=item.substring(y1+1,y2).trim();
			final String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			final Set<ChannelFlag> flags = new HashSet<ChannelFlag>();
			final String nameStr=item.toUpperCase().trim();
			final String[] colorOverride=new String[]{"",""};
			final String[] discordChan = new String[] {""};
			final String maskStr=parseOutFlags(lvl,flags,colorOverride,discordChan);
			final String i3nameStr=ichan;
			final CMChannel chan = this.createNewChannel(nameStr, i3nameStr, "", maskStr, flags,
														discordChan[0], colorOverride[0], colorOverride[1]);
			channelList.add(chan);
		}
		while(imc2list.length()>0)
		{
			final int x=imc2list.indexOf(',');

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
			final int y1=item.indexOf(' ');
			final int y2=item.lastIndexOf(' ');
			if((y1<0)||(y2<=y1))
				continue;
			final Set<ChannelFlag> flags = new HashSet<ChannelFlag>();
			final String lvl=item.substring(y1+1,y2).trim();
			final String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			final String nameStr=item.toUpperCase().trim();
			final String[] colorOverride=new String[]{"",""};
			final String[] discordChan = new String[] {""};
			final String maskStr=parseOutFlags(lvl,flags,colorOverride,discordChan);
			final String imc2Name=ichan;
			final CMChannel chan = this.createNewChannel(nameStr, "", imc2Name, maskStr, flags,
					discordChan[0], colorOverride[0], colorOverride[1]);
			channelList.add(chan);
		}
		baseChannelNames=new String[channelList.size()];
		for(int i=0;i<channelList.size();i++)
			baseChannelNames[i]=channelList.get(i).name();
		if(channelList.size()>31)
		{
			Log.errOut("CMChannels", "Too many channels defined: "+channelList.size()+".");
		}
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CHANNELAUCTION))
		&&(this.getChannel("AUCTION")==null))
			channelList.add(createNewChannel("AUCTION", "", "", "", new HashSet<ChannelFlag>(), "", "", ""));
		for(final CMChannel chan : channelList)
		{
			if(chan.flags().contains(ChannelFlag.DISCORD)
			&& (chan.discordName().length() > 0))
			{
				initDiscord();
				if(discordApi == null)
					break;
				final Object chanObj = CMChannels.getDiscordChannelObj(chan.discordName());
				if(chanObj != null)
				{
					discordChannels.put(chan, chanObj);
					synchronized(discordLibMap)
					{
						if(!discordLibMap.containsKey(chanObj))
							discordLibMap.put(chanObj, new Vector<CMChannels>());
						discordLibMap.get(chanObj).add(this);
					}
				}
				else
					Log.errOut("Unable to map discord channel '"+chan.discordName()+"'");
			}
		}
		return channelList.size();
	}

	@Override
	public boolean sendChannelCMMsgTo(final Session ses, final boolean areareq, final int channelInt, final CMMsg msg, final MOB sender)
	{
		final MOB M=ses.mob();
		if(M==null)
			return false;
		final ChannelsLibrary libChk=(ChannelsLibrary)CMLib.library((char)ses.getGroupID(),Library.CHANNELS);
		final CMChannel channel=this.getChannel(channelInt);
		if((libChk != null)&&(libChk != this)&&(channel!=null))
		{
			final int newDex=libChk.getChannelIndex(channel.name());
			if(newDex < 0)
				return false;
		}
		final Room R=M.location();
		boolean didIt=false;
		if(mayReadThisChannel(sender,areareq,ses,channelInt)
		&&(R!=null)
		&&((sender.location()==R)||(R.okMessage(ses.mob(),msg))))
		{
			if(ses.getClientTelnetMode(Session.TELNET_GMCP)&&(channel!=null))
			{
				final String player=CMStrings.removeAllButLettersAndDigits(CMStrings.removeColors(msg.source().name()));
				final String chanMsgStr = CMStrings.removeColors((M==msg.source())?msg.sourceMessage():msg.othersMessage()).trim();
				final String filteredMsgStr = CMLib.coffeeFilter().fullOutFilter(null, M, msg.source(), msg.target(), msg.tool(), chanMsgStr, false).trim();
				final String jsonMsgStr = MiniJSON.toJSONString(CMStrings.unWWrap(filteredMsgStr));
				ses.sendGMCPEvent("comm.channel", "{\"chan\":\""+channel.name()+"\",\"msg\":\""+jsonMsgStr+"\",\"player\":\""+player+"\"}");
			}
			M.executeMsg(M,msg);
			didIt=true;
			if(msg.trailerMsgs()!=null)
			{
				for(final CMMsg msg2 : msg.trailerMsgs())
				{
					if((msg!=msg2)&&(R.okMessage(M,msg2)))
						M.executeMsg(M,msg2);
				}
				msg.trailerMsgs().clear();
			}
			if(msg.trailerRunnables()!=null)
			{
				for(final Runnable r : msg.trailerRunnables())
					CMLib.threads().executeRunnable(r);
				msg.trailerRunnables().clear();
			}
		}
		return didIt;
	}

	protected int[] getExtraChannelDataFields(final MOB mob, final CMChannel chan)
	{
		final List<Integer> all=new LinkedList<Integer>();
		if(chan.flags().contains(ChannelsLibrary.ChannelFlag.SAMEAREA))
		{
			final Area A=CMLib.map().areaLocation(mob);
			if(A!=null)
				all.add(Integer.valueOf(A.Name().toUpperCase().hashCode()));
		}
		else
		if((chan.flags().contains(ChannelsLibrary.ChannelFlag.CLANONLY))
		||(chan.flags().contains(ChannelsLibrary.ChannelFlag.CLANALLYONLY)))
		{
			final List<Pair<Clan,Integer>> allClans=new ArrayList<Pair<Clan,Integer>>();
			allClans.addAll(CMLib.clans().findPrivilegedClans(mob, Clan.Function.CHANNEL));
			Collections.sort(allClans,Clan.compareByRole);
			for(final Pair<Clan,Integer> p : allClans)
				all.add(Integer.valueOf(p.first.name().toUpperCase().hashCode()));
		}
		if(all.size()==0)
			return new int[0];
		final int[] f = new int[all.size()];
		for(int i=0;i<f.length;i++)
			f[i]=all.get(i).intValue();
		return f;
	}

	protected int getExtraChannelDataField(final MOB mob, final CMChannel chan)
	{
		final int[] extraFields = getExtraChannelDataFields(mob, chan);
		if(extraFields.length==0)
			return 0;
		return extraFields[0];
	}

	@Override
	public void createAndSendChannelMessage(final MOB mob, final String channelName, final String message, final boolean systemMsg)
	{
		this.createAndSendChannelMessage(mob, channelName, message, systemMsg, false);
	}

	protected void createAndSendChannelMessage(final MOB mob, String channelName, String message, final boolean systemMsg, final boolean noloop)
	{
		if(mob == null)
			return;
		final int channelInt=getChannelIndex(channelName);
		if(channelInt<0)
			return;

		final PlayerStats pStats=mob.playerStats();
		final Room R=mob.location();

		message=CMProps.applyINIFilter(message,CMProps.Str.CHANNELFILTER);
		final CMChannel chan=getChannel(channelInt);
		final Set<ChannelFlag> flags=chan.flags();
		channelName=chan.name();
		final String channelColor="^Q";

		String nameAppendage="";
		if(chan.flags().contains(ChannelsLibrary.ChannelFlag.ADDACCOUNT)
		&&(pStats.getAccount()!=null))
			nameAppendage+=" ("+pStats.getAccount().name()+")";
		if(chan.flags().contains(ChannelsLibrary.ChannelFlag.ADDROOM)
		&&(R!=null))
			nameAppendage+=" in "+CMStrings.replaceAll(R.displayText(mob),"\'","");
		if(chan.flags().contains(ChannelsLibrary.ChannelFlag.ADDAREA)
		&&(R!=null)&&(R.getArea()!=null))
			nameAppendage+=" at "+CMStrings.replaceAll(R.getArea().name(mob),"\'","");

		if((mob!=null)
		&&(mob.isPlayer())
		&&(!systemMsg)
		&&(((CMProps.getIntVar(CMProps.Int.RP_CHANNEL)!=0)||(CMProps.getIntVar(CMProps.Int.RP_CHANNEL_NAMED)!=0))))
		{
			if(System.currentTimeMillis() >= pStats.getLastRolePlayXPTime() + CMProps.getIntVar(CMProps.Int.RP_AWARD_DELAY))
			{
				if(CMProps.isSpecialRPChannel(channelName))
				{
					if(CMProps.getIntVar(CMProps.Int.RP_CHANNEL_NAMED)!=0)
					{
						pStats.setLastRolePlayXPTime(System.currentTimeMillis());
						CMLib.leveler().postRPExperience(mob, "CHANNEL:"+channelName, null, "", CMProps.getIntVar(CMProps.Int.RP_CHANNEL_NAMED), false);
					}
				}
				else
				{
					if(CMProps.getIntVar(CMProps.Int.RP_CHANNEL)!=0)
					{
						pStats.setLastRolePlayXPTime(System.currentTimeMillis());
						CMLib.leveler().postRPExperience(mob, "CHANNEL:"+channelName, null, "", CMProps.getIntVar(CMProps.Int.RP_CHANNEL), false);
					}
				}
			}
		}

		CMMsg msg=null;
		final int srcCode = chan.flags().contains(ChannelFlag.NOMOUTH) ? CMMsg.TYP_OK_ACTION : CMMsg.MSG_SPEAK;
		if(systemMsg)
		{
			String str="["+channelName+"]"+nameAppendage+" '"+message+"'^</CHANNEL^>^?^.";
			if((!mob.name().startsWith("^"))||(mob.name().length()>2))
			{
				str="<S-NAME>"+nameAppendage+" "+str;
				message = "(<S-NAME>"+"@"+CMProps.getVar(CMProps.Str.MUDNAME)+nameAppendage+") "+message;
			}
			msg=CMClass.getMsg(mob,null,null,
					CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|srcCode,channelColor+"^<CHANNEL \""+channelName+"\"^>"+str,
					CMMsg.NO_EFFECT,null,
					CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+"^<CHANNEL \""+channelName+"\"^>"+str);
		}
		else
		if(message.startsWith(",")
		||(message.startsWith(":")
			&&(message.length()>1)
			&&(Character.isLetter(message.charAt(1))||message.charAt(1)==' ')))
		{
			String msgstr=message.substring(1);
			final Vector<String> V=CMParms.parse(msgstr);
			Social S=CMLib.socials().fetchSocial(V,true,false);
			if(S==null)
				S=CMLib.socials().fetchSocial(V,false,false);
			if((S!=null)
			&&(S.meetsCriteriaToUse(mob)))
			{
				msg=S.makeChannelMsg(mob,channelInt,channelName,V,false);
				if(msg.othersMessage()!=null)
				{
					message=CMStrings.removeColors(msg.othersMessage());
					if(msg.target()!=null)
						message=CMStrings.replaceAll(message,"<T-NAME>",msg.target().name());
					message=CMStrings.replaceAll(message,"["+channelName+"]","(@"+CMProps.getVar(Str.MUDNAME)+")");
				}
			}
			else
			{
				msgstr=CMProps.applyINIFilter(msgstr,CMProps.Str.EMOTEFILTER);
				if(msgstr.trim().startsWith("'")||msgstr.trim().startsWith("`"))
					msgstr=msgstr.trim();
				else
					msgstr=" "+msgstr.trim();
				final String srcstr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+mob.name()+nameAppendage+msgstr+"^</CHANNEL^>^N^.";
				final String reststr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] <S-NAME>"+nameAppendage+msgstr+"^</CHANNEL^>^N^.";
				msg=CMClass.getMsg(mob,null,null,
						CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|srcCode,channelColor+""+srcstr,
						CMMsg.NO_EFFECT,null,
						CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+reststr);
				message="<S-NAME>"+"@"+CMProps.getVar(CMProps.Str.MUDNAME)+nameAppendage+msgstr;
			}
		}
		else
		{
			msg=CMClass.getMsg(mob,null,null,
					CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|srcCode,channelColor+"^<CHANNEL \""+channelName+"\"^>"+L("You")+nameAppendage+" "+channelName+" '"+message+"'^</CHANNEL^>^N^.",
					CMMsg.NO_EFFECT,null,
					CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+"^<CHANNEL \""+channelName+"\"^><S-NAME>"+nameAppendage+" "+CMLib.english().makePlural(channelName)+" '"+message+"'^</CHANNEL^>^N^.");
			message="(<S-NAME>@"+CMProps.getVar(CMProps.Str.MUDNAME)+nameAppendage+") "+message;
		}

		if((chan.flags().contains(ChannelsLibrary.ChannelFlag.ACCOUNTOOC)
			||(chan.flags().contains(ChannelsLibrary.ChannelFlag.ACCOUNTOOCNOADMIN) && (!CMSecurity.isStaff(mob))))
		&&(pStats!=null)
		&&(pStats.getAccount()!=null)
		&&(msg.source()==mob))
		{
			final String accountName=pStats.getAccount().getAccountName();
			if(msg.sourceMessage()!=null)
				msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(), "<S-NAME>", accountName));
			if(msg.targetMessage()!=null)
				msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(), "<S-NAME>", accountName));
			if(msg.othersMessage()!=null)
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<S-NAME>", accountName));
		}

		if((chan.flags().contains(ChannelsLibrary.ChannelFlag.REALNAMEOOC)
			||(chan.flags().contains(ChannelsLibrary.ChannelFlag.REALNAMEOOCNOADMIN) && (!CMSecurity.isStaff(mob))))
		&&(pStats!=null)
		&&(pStats.getAccount()!=null)
		&&(msg.source()==mob))
		{
			final String realName=mob.Name();
			if(msg.sourceMessage()!=null)
				msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(), "<S-NAME>", realName));
			if(msg.targetMessage()!=null)
				msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(), "<S-NAME>", realName));
			if(msg.othersMessage()!=null)
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<S-NAME>", realName));
		}

		if(chan.flags().contains(ChannelsLibrary.ChannelFlag.NOLANGUAGE))
			msg.setTool(getCommonLanguage());

		CMLib.commands().monitorGlobalMessage(R, msg);
		if((R!=null)
		&&((!R.isInhabitant(mob))||(R.okMessage(mob,msg))))
		{
			if(chan.flags().contains(ChannelsLibrary.ChannelFlag.TWITTER))
				tweet(message);
			if(chan.flags().contains(ChannelsLibrary.ChannelFlag.DISCORD) && (!noloop))
			{
				message=CMStrings.replaceAll(message,"<S-NAME>",mob.name());
				discordMsg(chan, message);
			}

			final boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
			try
			{
				channelQueUp(channelInt, msg, this.getExtraChannelDataField(mob, chan));
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				final ChannelsLibrary myChanLib=CMLib.get(S)._channels();
				final int chanNum = (myChanLib == this) ? channelInt : myChanLib.getChannelIndex(channelName);
				if(chanNum >= 0)
				{
					msg.setOthersCode(CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+chanNum));
					myChanLib.sendChannelCMMsgTo(S,areareq,chanNum,msg,mob);
				}
			}
		}
		if((CMLib.intermud().i3online()&&(CMLib.intermud().isI3channel(channelName)))
		||(CMLib.intermud().imc2online()&&(CMLib.intermud().isIMC2channel(channelName))))
			CMLib.intermud().i3channel(mob,channelName,message);
	}

	protected Language getCommonLanguage()
	{
		if(commonLang==null)
		{
			commonLang = (Language)CMClass.getAbility("Common");
		}
		return commonLang;

	}

	@Override
	public boolean activate()
	{
		if(!super.activate())
			return false;
		if(serviceClient==null)
		{
			name="THChannels"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_UTILTHREAD_SLEEP, 1);
			CMLib.database().checkUpgradeBacklogTable(this);
		}
		return true;
	}

	/**
	 * Requires including special library and special configuration.
	 * @param msg the message to tweet
	 */
	private void tweet(String msg)
	{
		msg = CMStrings.scrunchWord(CMStrings.removeColors(msg), 280);
		try
		{
			final Class<?> cbClass = Class.forName("twitter4j.conf.ConfigurationBuilder");
			final Object cbObj = cbClass.getDeclaredConstructor().newInstance();
			final Method cbM1=cbClass.getMethod("setOAuthConsumerKey", String.class);
			cbM1.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHCONSUMERKEY"));
			final Method cbM2=cbClass.getMethod("setOAuthConsumerSecret", String.class);
			cbM2.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHCONSUMERSECRET"));
			final Method cbM3=cbClass.getMethod("setOAuthAccessToken", String.class);
			cbM3.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHACCESSTOKEN"));
			final Method cbM4=cbClass.getMethod("setOAuthAccessTokenSecret", String.class);
			cbM4.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHACCESSTOKENSECRET"));
			final Method cbM5=cbClass.getMethod("build");
			final Object cbBuildObj = cbM5.invoke(cbObj);

			final Class<?> cfClass = Class.forName("twitter4j.conf.Configuration");
			final Class<?> afClass = Class.forName("twitter4j.auth.AuthorizationFactory");
			final Method adM1 = afClass.getMethod("getInstance",cfClass);
			final Object auObj = adM1.invoke(null, cbBuildObj);

			final Class<?> auClass = Class.forName("twitter4j.auth.Authorization");
			final Class<?> tfClass = Class.forName("twitter4j.TwitterFactory");
			final Object tfObj = tfClass.getDeclaredConstructor().newInstance();
			final Method tfM1 = tfClass.getMethod("getInstance", auClass);
			final Object twObj = tfM1.invoke(tfObj, auObj);

			final Class<?> twClass = Class.forName("twitter4j.Twitter");
			final Method twM1 = twClass.getMethod("updateStatus", String.class);
			twM1.invoke(twObj, msg);
		}
		catch (final Exception e)
		{
			Log.errOut(e);
		}
	}

	/**
	 * Requires including special library and special configuration.
	 * @param msg the message to send
	 */
	private void discordMsg(final CMChannel chan, final String msg)
	{
		if(discordApi==null)
			return;
		final Object chanObj = this.discordChannels.get(chan);
		if(chanObj != null)
		{
			try
			{
				final Class<?> schannelClass = discordClassLoader.loadClass("org.javacord.core.entity.channel.ServerTextChannelImpl");
				final Method sendM = schannelClass.getMethod("sendMessage",String.class);
				lastDiscordMsgs.add(msg);
				sendM.invoke(chanObj, msg);
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
		}
	}

	protected static class DiscordMsgListener implements InvocationHandler
	{
		private final Class<?> eventClass;
		private final static Map<String,MOB> discordTalkers = Collections.synchronizedMap(new TreeMap<String,MOB>());
		public DiscordMsgListener(final Class<?> eventClass)
		{
			this.eventClass = eventClass;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
		{
			final Object result = null;
			try
			{
				if(method.getName().equals("hashCode"))
					return Integer.valueOf(this.hashCode());
				else
				if(method.getName().equals("equals") && (args!=null) && (args.length>0))
					return Boolean.valueOf(this.equals(args[0]));
				else
				if(method.getName().equals("toString"))
					return "";
				else
				if((method.getName().equals("onMessageCreate"))
				&&(args != null)
				&&(args.length>0)
				&&(CMProps.getBoolVar(Bool.MUDSTARTED)))
				{
					final Method channelM = eventClass.getMethod("getChannel");
					final Object channelObj = channelM.invoke(args[0]);
					if(discordLibMap.containsKey(channelObj))
					{
						for(final CMChannels lib : discordLibMap.get(channelObj))
						{
							final CMChannel chan = lib.discordChannels.getValue(channelObj);
							if(chan == null)
								continue;
							if(chan.flags().contains(ChannelsLibrary.ChannelFlag.READONLY)
							|| chan.flags().contains(ChannelsLibrary.ChannelFlag.PLAYERREADONLY)
							|| chan.flags().contains(ChannelsLibrary.ChannelFlag.ARCHONREADONLY))
								continue;
							final Class<?> authClass = discordClassLoader.loadClass("org.javacord.api.entity.message.MessageAuthor");
							final Method contentM = eventClass.getMethod("getMessageContent");
							final String content = (String)contentM.invoke(args[0]);
							if(!lib.lastDiscordMsgs.contains(content))
							{
								final Method authorM = eventClass.getMethod("getMessageAuthor");
								final Object authorO = authorM.invoke(args[0]);
								final Method nameM = authClass.getMethod("getDisplayName");
								final String name = (String)nameM.invoke(authorO);
								final MOB M;
								// preserve because backlogs
								if(discordTalkers.containsKey(name))
									M = discordTalkers.get(name);
								else
								if(CMLib.players().getPlayer(name)!=null)
								{
									M=CMLib.players().getPlayer(name);
									discordTalkers.put(name,M);
								}
								else
								{
									M=CMClass.getMOB("StdMOB");
									M.setName(name);
									M.setLocation(CMLib.map().getRandomRoom());
									discordTalkers.put(name,M);
								}
								final StringBuilder str = new StringBuilder("");
								for(final char c : content.toCharArray())
								{
									if((c>31)&&(c<128))
										str.append(c);
									else
									switch(c)
									{
									case '\n':case '\r':case '\t':
										str.append(c);
										break;
									}
								}
								lib.createAndSendChannelMessage(M, chan.name(), str.toString(), false, true);
							}
						}
					}
				}
			}
			catch (final Exception e)
			{
				Log.errOut(e);
			}
			return result;
		}
	}

	@SuppressWarnings("rawtypes")
	protected static Object getDiscordChannelObj(final String named)
	{
		if(discordApi == null)
			return null;
		try
		{
			final Class<?> apiClass = discordClassLoader.loadClass("org.javacord.api.DiscordApi");
			final Class<?> serverClass = discordClassLoader.loadClass("org.javacord.api.entity.server.Server");
			final Class<?> schannelClass = discordClassLoader.loadClass("org.javacord.core.entity.channel.ServerTextChannelImpl");
			final Method getServersM = apiClass.getMethod("getServers");
			final Map<String,Object> names = new TreeMap<String,Object>();
			for(final Object svrObj : (Iterable)getServersM.invoke(discordApi))
			{
				final Method getChannels = serverClass.getMethod("getTextChannels");
				for(final Object chanObj : (Iterable)getChannels.invoke(svrObj))
				{
					if(schannelClass.isInstance(chanObj))
					{
						final Method getName = schannelClass.getMethod("getName");
						final String channelName = (String)getName.invoke(chanObj);
						names.put(channelName.toUpperCase(), chanObj);
						if(channelName.equals(named))
							return chanObj;
					}
				}
			}
			final String unamed = named.toUpperCase();
			if(names.containsKey(unamed))
				return names.get(unamed);
			for(final String name : names.keySet())
			{
				if(unamed.startsWith(name))
					return names.get(name);
			}
			for(final String name : names.keySet())
			{
				if(unamed.endsWith(name))
					return names.get(name);
			}
			for(final String name : names.keySet())
			{
				if(name.startsWith(unamed))
					return names.get(name);
			}
			for(final String name : names.keySet())
			{
				if(name.endsWith(unamed))
					return names.get(name);
			}
		}
		catch (final Exception e)
		{
			Log.errOut(e);
		}
		/* channel.sendMessage(""); */
		return null;
	}

	protected static void initDiscord()
	{
		if(discordApi != null)
			return;
		final String jarPath = CMProps.getVar(Str.DISCORD_JAR_PATH);
		if(jarPath.length()==0)
		{
			Log.errOut("DISCORD_JAR_PATH not set in INI file.");
			return;
		}
		final CMFile F = new CMFile(jarPath, null);
		if(!F.exists())
		{
			Log.errOut("DISCORD jar file not found in "+jarPath);
			return;
		}
		URL jarUrl;
		try
		{
			URL.setURLStreamHandlerFactory(protocol -> "vfs".equals(protocol) ? new URLStreamHandler()
			{
				@Override
				protected java.net.URLConnection openConnection(final URL url) throws IOException
				{
					return new java.net.URLConnection(url)
					{
						final CMFile F = new CMFile(url.getPath(),null);
						@Override
						public void connect() throws IOException
						{
							if(!F.exists())
								throw new IOException("File not found: "+F.getAbsolutePath());
						}
						@Override
						public java.io.InputStream getInputStream() throws IOException
						{
							if(!F.exists())
								throw new IOException("File not found: "+F.getAbsolutePath());
							return F.getRawStream();
						}
					};
				}
			} : null);
			jarUrl = new URL("vfs:" + jarPath);
		}
		catch (final MalformedURLException e)
		{
			Log.errOut(e);
			return;
		}
		discordClassLoader=new URLClassLoader(new URL[]{jarUrl});
		final PrintStream originalOut = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(final int b)
			{
			}
		}));
		try
		{
			final Class<?> apiBuilderClass = discordClassLoader.loadClass("org.javacord.api.DiscordApiBuilder");
			final Class<?> apiClass = discordClassLoader.loadClass("org.javacord.api.DiscordApi");
			final Class<?> intentClass = discordClassLoader.loadClass("org.javacord.api.entity.intent.Intent");
			final Class<?> listenClass = discordClassLoader.loadClass("org.javacord.api.listener.message.MessageCreateListener");
			final Class<?> eventInterface = discordClassLoader.loadClass("org.javacord.api.event.message.MessageCreateEvent");
			final Object apiBuilder = apiBuilderClass.getDeclaredConstructor().newInstance();
			final String secretToken = CMProps.getVar(CMProps.Str.DISCORD_BOT_KEY);
			if(secretToken.length()==0)
				return;
			Object api = apiBuilder;
			final Method setTokenM = apiBuilderClass.getMethod("setToken", String.class);
			api = setTokenM.invoke(api, secretToken);
			final Method addIntentsM = apiBuilderClass.getMethod("addIntents", intentClass.getEnumConstants().getClass());
			final Object array = Array.newInstance(intentClass, 1);
			for(final Object o : intentClass.getEnumConstants())
			{
				@SuppressWarnings("rawtypes")
				final Enum e = (Enum)o;
				if(e.name().equals("MESSAGE_CONTENT"))
					Array.set(array, 0, e);
			}
			api = addIntentsM.invoke(api, new Object[] {array});
			final Method loginM = apiBuilderClass.getMethod("login");
			final CompletableFuture<?> future = (CompletableFuture<?>)loginM.invoke(api);
			discordApi = future.join();
			final Method createBotInviteM = apiClass.getMethod("createBotInvite");
			final String url=CMStrings.replaceAll(createBotInviteM.invoke(discordApi).toString(),"permissions=0","permissions=2048");
			Log.infoOut("Discord Bot auth url: "+url);
			final Method listenM = apiClass.getMethod("addMessageCreateListener", listenClass);
			final Class<?>[] classArray = new Class<?>[] { listenClass };
			final Object listener = Proxy.newProxyInstance(discordClassLoader, classArray, new DiscordMsgListener(eventInterface));
			listenM.invoke(discordApi, listener);
		}
		catch (final Exception e)
		{
			Log.errOut(e);
		}
		finally
		{
			System.setOut(originalOut);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		try
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.UTILITHREAD))
			{
				tickStatus=Tickable.STATUS_ALIVE;
				try
				{
					final String propStr=CMProps.getVar(CMProps.Str.CHANNELBACKLOG).toUpperCase().trim();
					if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CHANNELBACKLOGS))
					&&(!propStr.equalsIgnoreCase("INFINITY"))
					&&(!propStr.equalsIgnoreCase("FOREVER")))
					{
						if(CMath.isInteger(propStr))
							CMLib.database().trimBackLogEntries(getChannelNames(), CMath.s_int(propStr), 0);
						else
						{
							final String[] ss=propStr.split(" ");
							if((ss.length!=2)&&(!CMath.isInteger(ss[0])))
								Log.errOut("CMChannels","Malformed CHANNELBACKLOG entry in coffeemud.ini file: "+propStr);
							else
							if(ss[1].equals("DAYS")||ss[1].equals("DAY"))
								CMLib.database().trimBackLogEntries(getChannelNames(), Integer.MAX_VALUE, System.currentTimeMillis() - (CMath.s_int(ss[0]) * TimeManager.MILI_DAY));
							else
							if(ss[1].equals("WEEKS")||ss[1].equals("WEEK"))
								CMLib.database().trimBackLogEntries(getChannelNames(), Integer.MAX_VALUE, System.currentTimeMillis() - (CMath.s_int(ss[0]) * TimeManager.MILI_WEEK));
							else
							if(ss[1].equals("MONTHS")||ss[1].equals("MONTHS"))
								CMLib.database().trimBackLogEntries(getChannelNames(), Integer.MAX_VALUE, System.currentTimeMillis() - (CMath.s_int(ss[0]) * TimeManager.MILI_MONTH));
							else
							if(ss[1].equals("YEARSS")||ss[1].equals("YEAR"))
								CMLib.database().trimBackLogEntries(getChannelNames(), Integer.MAX_VALUE, System.currentTimeMillis() - (CMath.s_int(ss[0]) * TimeManager.MILI_YEAR));
							else
								Log.errOut("CMChannels","Malformed CHANNELBACKLOG entry in coffeemud.ini file: "+propStr);
						}
					}
				}
				finally
				{
				}
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		clearChannels();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		if(discordApi != null)
		{
			try
			{
				final Method disconnM = discordApi.getClass().getMethod("disconnect");
				disconnM.invoke(discordApi);
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
			finally
			{
				discordApi = null;
			}
		}
		return true;
	}

}
