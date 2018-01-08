package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.reflect.Method;
import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
	
	public final static List<ChannelMsg> emptyQueue=new ReadOnlyList<ChannelMsg>(new Vector<ChannelMsg>(1));
	public final static Set<ChannelFlag> emptyFlags=new ReadOnlySet<ChannelFlag>(new HashSet<ChannelFlag>(1));

	@Override
	public int getNumChannels()
	{
		return channelList.size();
	}

	@Override
	public CMChannel getChannel(int channelNumber)
	{
		if((channelNumber>=0)&&(channelNumber<channelList.size()))
			return channelList.get(channelNumber);
		return null;
	}

	public CMChannel createNewChannel(final String name)
	{
		return createNewChannel(name, "", "", "", new HashSet<ChannelFlag>(), "", "");
	}
	
	public CMChannel createNewChannel(final String name, final String mask, final Set<ChannelFlag> flags, 
									  final String colorOverrideANSI, final String colorOverrideWords)
	{
		return createNewChannel(name, "", "", mask, flags, colorOverrideANSI, colorOverrideWords);
	}
	
	@Override
	public CMChannel createNewChannel(final String name, final String i3Name, final String imc2Name, 
									  final String mask, final Set<ChannelFlag> flags, 
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
	public List<ChannelMsg> getChannelQue(int channelNumber, int numNewToSkip, int numToReturn)
	{
		if((channelNumber>=0)&&(channelNumber<channelList.size()))
		{
			final CMChannel channel = channelList.get(channelNumber);
			LinkedList<ChannelMsg> msgs=new LinkedList<ChannelMsg>();
			if(numNewToSkip < channel.queue().size())
			{
				int skipNum=numNewToSkip;
				for(ChannelMsg msg : channel.queue())
				{
					if((--skipNum < 0)&&(msgs.size() < numToReturn))
						msgs.addFirst(msg);
				}
			}
			
			if(msgs.size()>=numToReturn)
				return msgs;
			if(channel.flags().contains(ChannelsLibrary.ChannelFlag.NOBACKLOG))
				return msgs;
			final List<Pair<String,Long>> backLog=CMLib.database().getBackLogEntries(channel.name(), numNewToSkip, numToReturn);
			if(backLog.size()<=msgs.size())
				return msgs;
			final List<ChannelMsg> allMsgs = new XVector<ChannelMsg>();
			for(int x=0;x<backLog.size()-msgs.size();x++)
			{
				final CMMsg msg=CMClass.getMsg();
				msg.parseFlatString(backLog.get(x).first);
				final long time = backLog.get(x).second.longValue(); 
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
				});
			}
			allMsgs.addAll(msgs);
			return allMsgs;
		}
		return emptyQueue;
	}

	@Override
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int channelNumber)
	{ 
		return mayReadThisChannel(sender,areaReq,M,channelNumber,false);
	}
	
	@Override
	public boolean mayReadThisChannel(MOB sender,
									  boolean areaReq,
									  MOB M,
									  int channelNumber,
									  boolean offlineOK)
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

		if((!pstats.getIgnored().contains(sender.Name()))
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
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, Session ses, int channelNumber)
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
		&&(!pstats.getIgnored().contains(senderName))
		&&(CMLib.masking().maskCheck(chan.mask(),M,true))
		&&((!areaReq)
		   ||(sender.location()==null)
		   ||(R.getArea()==sender.location().getArea()))
		&&(!CMath.isSet(pstats.getChannelMask(),channelNumber)))
			return true;
		return false;
	}

	@Override
	public boolean mayReadThisChannel(MOB M, int channelNumber, boolean zapCheckOnly)
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
	public void channelQueUp(final int channelNumber, final CMMsg msg)
	{
		CMLib.map().sendGlobalMessage(msg.source(),CMMsg.TYP_CHANNEL,msg);
		final CMChannel channel=getChannel(channelNumber);
		final SLinkedList<ChannelMsg> q=channel.queue();
		synchronized(q)
		{
			if(q.size()>=QUEUE_SIZE)
				q.removeLast();
			final long now = System.currentTimeMillis();
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
			});
		}
		if((!channel.flags().contains(ChannelsLibrary.ChannelFlag.NOBACKLOG))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CHANNELBACKLOGS))
		&&(!CMProps.getVar(CMProps.Str.CHANNELBACKLOG).equals("0")))
		{
			try
			{
				CMLib.database().addBackLogEntry(getChannel(channelNumber).name(), msg.toFlatString());
			}
			catch(Exception e)
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
	public List<String> getFlaggedChannelNames(ChannelFlag flag)
	{
		final List<String> channels=new Vector<String>();
		for(int c=0;c<channelList.size();c++)
		{
			if(channelList.get(c).flags().contains(flag))
				channels.add(channelList.get(c).name().toUpperCase());
		}
		return channels;
	}

	@Override
	public String getExtraChannelDesc(String channelName)
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
		channelList=new Vector<CMChannel>();
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
	public List<Session> clearInvalidSnoopers(Session mySession, int channelCode)
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
	public void restoreInvalidSnoopers(Session mySession, List<Session> invalid)
	{
		if((mySession==null)||(invalid==null))
			return;
		for(int s=0;s<invalid.size();s++)
			mySession.setBeingSnoopedBy(invalid.get(s), true);
	}

	public String parseOutFlags(String mask, Set<ChannelFlag> flags, String[] colorOverride)
	{
		final List<String> V=CMParms.parseSpaces(mask,true);
		for(int v=V.size()-1;v>=0;v--)
		{
			final String s=V.get(v).toUpperCase();
			if(CMParms.contains(CMParms.toStringArray(ChannelFlag.values()), s))
			{
				V.remove(v);
				flags.add(ChannelFlag.valueOf(s));
			}
			else
			{
				Color C=(Color)CMath.s_valueOf(Color.class, s);
				if(C!=null)
				{
					V.remove(v);
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
				String mask=parseOutFlags(item.substring(x+1).trim(),flags,colorOverride);
				item=item.substring(0,x);
				chan = this.createNewChannel(item.toUpperCase().trim(), mask, flags, colorOverride[0], colorOverride[1]);
			}
			else
				chan = this.createNewChannel(item.toUpperCase().trim());
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
			String nameStr=item.toUpperCase().trim();
			final String[] colorOverride=new String[]{"",""};
			String maskStr=parseOutFlags(lvl,flags,colorOverride);
			String i3nameStr=ichan;
			final CMChannel chan = this.createNewChannel(nameStr, i3nameStr, "", maskStr, flags, colorOverride[0], colorOverride[1]);
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
			String nameStr=item.toUpperCase().trim();
			final String[] colorOverride=new String[]{"",""};
			String maskStr=parseOutFlags(lvl,flags,colorOverride);
			String imc2Name=ichan;
			final CMChannel chan = this.createNewChannel(nameStr, "", imc2Name, maskStr, flags, colorOverride[0], colorOverride[1]);
			channelList.add(chan);
		}
		baseChannelNames=new String[channelList.size()];
		for(int i=0;i<channelList.size();i++)
			baseChannelNames[i]=channelList.get(i).name();
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CHANNELAUCTION))
		{
			channelList.add(this.createNewChannel("AUCTION"));
		}
		return channelList.size();
	}

	@Override
	public boolean sendChannelCMMsgTo(Session ses, boolean areareq, int channelInt, CMMsg msg, MOB sender)
	{
		final MOB M=ses.mob();
		if(M==null)
			return false;
		final Room R=M.location();
		boolean didIt=false;
		if(mayReadThisChannel(sender,areareq,ses,channelInt)
		&&(R!=null)
		&&((sender.location()==R)||(R.okMessage(ses.mob(),msg))))
		{
			if(ses.getClientTelnetMode(Session.TELNET_GMCP))
			{
				ses.sendGMCPEvent("comm.channel", "{\"chan\":\""+getChannel(channelInt).name()+"\",\"msg\":\""+
						MiniJSON.toJSONString(CMLib.coffeeFilter().fullOutFilter(null, M, msg.source(), msg.target(), msg.tool(), CMStrings.removeColors((M==msg.source())?msg.sourceMessage():msg.othersMessage()), false))
						+"\",\"player\":\""+msg.source().name()+"\"}");
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

	@Override
	public void createAndSendChannelMessage(MOB mob, String channelName, String message, boolean systemMsg)
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
		
		CMMsg msg=null;
		if(systemMsg)
		{
			String str="["+channelName+"]"+nameAppendage+" '"+message+"'^</CHANNEL^>^?^.";
			if((!mob.name().startsWith("^"))||(mob.name().length()>2))
				str="<S-NAME>"+nameAppendage+" "+str;
			msg=CMClass.getMsg(mob,null,null,
					CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,channelColor+"^<CHANNEL \""+channelName+"\"^>"+str,
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
			if(S!=null)
				msg=S.makeChannelMsg(mob,channelInt,channelName,V,false);
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
						CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,channelColor+""+srcstr,
						CMMsg.NO_EFFECT,null,
						CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+reststr);
			}
		}
		else
		{
			msg=CMClass.getMsg(mob,null,null,
					CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,channelColor+"^<CHANNEL \""+channelName+"\"^>"+L("You")+nameAppendage+" "+channelName+" '"+message+"'^</CHANNEL^>^N^.",
					CMMsg.NO_EFFECT,null,
					CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+"^<CHANNEL \""+channelName+"\"^><S-NAME>"+nameAppendage+" "+CMLib.english().makePlural(channelName)+" '"+message+"'^</CHANNEL^>^N^.");
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
		
		if(chan.flags().contains(ChannelsLibrary.ChannelFlag.NOLANGUAGE))
			msg.setTool(getCommonLanguage());

		CMLib.commands().monitorGlobalMessage(R, msg);
		if((R!=null)
		&&((!R.isInhabitant(mob))||(R.okMessage(mob,msg))))
		{
			if(chan.flags().contains(ChannelsLibrary.ChannelFlag.TWITTER))
				tweet(message);
			final boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
			channelQueUp(channelInt,msg);
			for(final Session S : CMLib.sessions().localOnlineIterable())
				sendChannelCMMsgTo(S,areareq,channelInt,msg,mob);
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
		if(serviceClient==null)
		{
			name="THChannels"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_UTILTHREAD_SLEEP, 1);
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
			Class<?> cbClass = Class.forName("twitter4j.conf.ConfigurationBuilder");
			Object cbObj = cbClass.newInstance();
			Method cbM1=cbClass.getMethod("setOAuthConsumerKey", String.class);
			cbM1.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHCONSUMERKEY"));
			Method cbM2=cbClass.getMethod("setOAuthConsumerSecret", String.class);
			cbM2.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHCONSUMERSECRET"));
			Method cbM3=cbClass.getMethod("setOAuthAccessToken", String.class);
			cbM3.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHACCESSTOKEN"));
			Method cbM4=cbClass.getMethod("setOAuthAccessTokenSecret", String.class);
			cbM4.invoke(cbObj,CMProps.getProp("TWITTER-OAUTHACCESSTOKENSECRET"));
			Method cbM5=cbClass.getMethod("build");
			Object cbBuildObj = cbM5.invoke(cbObj);
			
			Class<?> cfClass = Class.forName("twitter4j.conf.Configuration");
			Class<?> afClass = Class.forName("twitter4j.auth.AuthorizationFactory");
			Method adM1 = afClass.getMethod("getInstance",cfClass);
			Object auObj = adM1.invoke(null, cbBuildObj);
			
			Class<?> auClass = Class.forName("twitter4j.auth.Authorization");
			Class<?> tfClass = Class.forName("twitter4j.TwitterFactory");
			Object tfObj = tfClass.newInstance();
			Method tfM1 = tfClass.getMethod("getInstance", auClass);
			Object twObj = tfM1.invoke(tfObj, auObj);
			
			Class<?> twClass = Class.forName("twitter4j.Twitter");
			Method twM1 = twClass.getMethod("updateStatus", String.class);
			twM1.invoke(twObj, msg);
		}
		catch (Exception e)
		{
			Log.errOut(e);
		}
	}

	@Override 
	public boolean tick(Tickable ticking, int tickID)
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
							String[] ss=propStr.split(" ");
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
		return true;
	}

}
