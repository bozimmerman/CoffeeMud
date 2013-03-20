package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
	public String ID(){return "CMChannels";}
	public final int QUEUE_SIZE=100;
	
	public String[] baseChannelNames=new String[0];
	public List<CMChannel> channelList=new Vector<CMChannel>();
	public final static List<ChannelMsg> emptyQueue=new ReadOnlyList<ChannelMsg>(new Vector<ChannelMsg>(1));
	public final static Set<ChannelFlag> emptyFlags=new ReadOnlySet<ChannelFlag>(new HashSet<ChannelFlag>(1));
	
	public int getNumChannels()
	{
		return channelList.size();
	}
	
	public CMChannel getChannel(int i)
	{
		if((i>=0)&&(i<channelList.size()))
			return channelList.get(i);
		return null;
	}

	public List<ChannelMsg> getChannelQue(int i)
	{
		if((i>=0)&&(i<channelList.size()))
			return channelList.get(i).queue;
		return emptyQueue;
	}
	
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i)
	{ return mayReadThisChannel(sender,areaReq,M,i,false);}
	public boolean mayReadThisChannel(MOB sender,
									  boolean areaReq,
									  MOB M, 
									  int i,
									  boolean offlineOK)
	{
		if((sender==null)||(M==null)) return false;
		PlayerStats pstats=M.playerStats();
		if(pstats==null) return false;
		Room R=M.location();
		if(((!offlineOK))
		&&((M.amDead())||(R==null)))
			return false;
		CMChannel chan=getChannel(i);
		if(chan==null) return false;
		if(chan.flags.contains(ChannelFlag.CLANONLY)||chan.flags.contains(ChannelFlag.CLANALLYONLY))
		{
			// only way to fail an all-clan send is to have NO clan.
			if(!CMLib.clans().checkClanPrivilege(M, Clan.Function.CHANNEL))
				return false;

			if((!CMLib.clans().isAnyCommonClan(sender,M))
			&&((!chan.flags.contains(ChannelFlag.CLANALLYONLY))
				||(!CMLib.clans().findAnyClanRelations(M,sender,Clan.REL_ALLY))))
				return false;
		}
		
		if((!pstats.getIgnored().contains(sender.Name()))
		&&(CMLib.masking().maskCheck(chan.mask,M,true))
		&&((!areaReq)
		   ||(sender.location()==null)
		   ||(R.getArea()==sender.location().getArea()))
		&&(!CMath.isSet(pstats.getChannelMask(),i)))
			return true;
		return false;
	}

	public boolean mayReadThisChannel(MOB sender,
									  boolean areaReq,
									  Session ses, 
									  int i)
	{
		if(ses==null) 
			return false;
		MOB M=ses.mob();
		
		if((sender==null)
		||(M==null)
		||(M.amDead())
		||(M.location()==null))
			return false;
		PlayerStats pstats=M.playerStats();
		if(pstats==null) return false;
		String senderName=sender.Name();
		int x=senderName.indexOf('@');
		if(x>0) senderName=senderName.substring(0,x);
		CMChannel chan=getChannel(i);
		if(chan==null) return false;
		if(chan.flags.contains(ChannelFlag.CLANONLY)||chan.flags.contains(ChannelFlag.CLANALLYONLY))
		{
			// only way to fail an all-clan send is to have NO clan.
			if(!CMLib.clans().checkClanPrivilege(M, Clan.Function.CHANNEL))
				return false;
			if((!CMLib.clans().isAnyCommonClan(sender,M))
			&&((!chan.flags.contains(ChannelFlag.CLANALLYONLY))
				||(!CMLib.clans().findAnyClanRelations(M,sender,Clan.REL_ALLY))))
				return false;
		}
		
		Room R=M.location();
		if((!ses.isStopped())
		&&(R!=null)
		&&(!pstats.getIgnored().contains(senderName))
		&&(CMLib.masking().maskCheck(chan.mask,M,true))
		&&((!areaReq)
		   ||(sender.location()==null)
		   ||(R.getArea()==sender.location().getArea()))
		&&(!CMath.isSet(pstats.getChannelMask(),i)))
			return true;
		return false;
	}
	
	public boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly)
	{
		if(M==null) return false;
		
		if(i>=getNumChannels())
			return false;
		
		CMChannel chan=getChannel(i);
		if(chan==null) return false;
		if((chan.flags.contains(ChannelFlag.CLANONLY)||chan.flags.contains(ChannelFlag.CLANALLYONLY))
		&&(!CMLib.clans().checkClanPrivilege(M, Clan.Function.CHANNEL)))
			return false;

		if(((zapCheckOnly)||((!M.amDead())&&(M.location()!=null)))
		&&(CMLib.masking().maskCheck(chan.mask,M,true))
		&&(!CMath.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}

	public void channelQueUp(int i, CMMsg msg)
	{
		CMLib.map().sendGlobalMessage(msg.source(),CMMsg.TYP_CHANNEL,msg);
		List<ChannelMsg> q=getChannelQue(i);
		synchronized(q)
		{
			if(q.size()>=QUEUE_SIZE)
				q.remove(0);
			q.add(new ChannelMsg(msg));
		}
	}
	
	public int getChannelIndex(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(int c=0;c<channelList.size();c++)
			if((channelList.get(c)).name.startsWith(channelName))
				return c;
		return -1;
	}

	public int getChannelCodeNumber(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(int c=0;c<channelList.size();c++)
			if((channelList.get(c)).name.startsWith(channelName))
				return 1<<c;
		return -1;
	}

	public String findChannelName(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(int c=0;c<channelList.size();c++)
			if((channelList.get(c)).name.startsWith(channelName))
				return (channelList.get(c)).name.toUpperCase();
		return "";
	}

	public List<String> getFlaggedChannelNames(ChannelFlag flag)
	{
		List<String> channels=new Vector<String>();
		for(int c=0;c<channelList.size();c++)
			if(channelList.get(c).flags.contains(flag))
				channels.add(channelList.get(c).name.toUpperCase());
		return channels;
	}
	
	public String getExtraChannelDesc(String channelName)
	{
		StringBuilder str=new StringBuilder("");
		int dex = getChannelIndex(channelName);
		if(dex >= 0)
		{
			CMChannel chan=getChannel(dex);
			Set<ChannelFlag> flags = chan.flags;
			String mask = chan.mask;
			if(flags.contains(ChannelFlag.CLANALLYONLY))
				str.append(" This is a channel for clans and their allies.");
			if(flags.contains(ChannelFlag.CLANONLY))
				str.append(" Only members of the same clan can see messages on this channel.");
			if(flags.contains(ChannelFlag.PLAYERREADONLY)||flags.contains(ChannelFlag.READONLY))
				str.append(" This channel is read-only.");
			if(flags.contains(ChannelFlag.SAMEAREA))
				str.append(" Only people in the same area can see messages on this channel.");
			if((mask!=null)&&(mask.trim().length()>0))
				str.append(" The following may read this channel : "+CMLib.masking().maskDesc(mask));
		}
		return str.toString();
	}

	private void clearChannels()
	{
		channelList=new Vector<CMChannel>();
	}
	
	public boolean shutdown()
	{
		clearChannels();
		return true;
	}

	public List<CMChannel> getIMC2ChannelsList()
	{
		List<CMChannel> list=new Vector<CMChannel>();
		for(int i=0;i<channelList.size();i++)
			if((channelList.get(i).imc2Name!=null)
			&&(channelList.get(i).imc2Name.length()>0))
				list.add(channelList.get(i));
		return list;
	}

	public List<CMChannel> getI3ChannelsList()
	{
		List<CMChannel> list=new Vector<CMChannel>();
		for(int i=0;i<channelList.size();i++)
			if((channelList.get(i).i3name!=null)
			&&(channelList.get(i).i3name.length()>0))
				list.add(channelList.get(i));
		return list;
	}
	
	public String[] getChannelNames()
	{
		return baseChannelNames;
	}
	
	public List<Session> clearInvalidSnoopers(Session mySession, int channelCode)
	{
		List<Session> invalid=null;
		if(mySession!=null)
		{
			for(Session S : CMLib.sessions().allIterable())
			{
				if((S!=mySession)
				&&(S.mob()!=null)
				&&(mySession.amBeingSnoopedBy(S))
				&&(!mayReadThisChannel(S.mob(),channelCode,false)))
				{
					if(invalid==null) invalid=new Vector<Session>();
					invalid.add(S);
					mySession.stopBeingSnoopedBy(S);
				}
			}
		}
		return invalid; 	   
	}
	
	public void restoreInvalidSnoopers(Session mySession, List<Session> invalid)
	{
		if((mySession==null)||(invalid==null)) return;
		for(int s=0;s<invalid.size();s++)
			mySession.startBeingSnoopedBy(invalid.get(s));
	}

	public String parseOutFlags(String mask, Set<ChannelFlag> flags, String[] colorOverride)
	{
		Vector<String> V=CMParms.parseSpaces(mask,true);
		for(int v=V.size()-1;v>=0;v--)
		{
			String s=V.elementAt(v).toUpperCase();
			if(CMParms.contains(CMParms.toStringArray(ChannelFlag.values()), s))
			{
				V.removeElementAt(v);
				flags.add(ChannelFlag.valueOf(s));
			}
			else
			{
				int colorNum=CMParms.indexOf(ColorLibrary.COLOR_ALLCOLORNAMES, s);
				if(colorNum>=0)
				{
					V.removeElementAt(v);
					if(s.startsWith("BG"))
						colorOverride[0]=colorOverride[0]+ColorLibrary.COLOR_ALLCOLORS[colorNum];
					else
						colorOverride[0]=ColorLibrary.COLOR_ALLCOLORS[colorNum]+ColorLibrary.COLOR_ALLCOLORS[colorNum]+colorOverride[0];
				}
			}
		}
		final StringBuilder str=new StringBuilder();
		for(final String s : V)
			str.append(s).append(" ");
		return str.toString().trim();
	}
	
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
			CMChannel chan=new CMChannel();
			if(x>0)
			{
				String[] colorOverride=new String[]{""};
				chan.mask=parseOutFlags(item.substring(x+1).trim(),chan.flags,colorOverride);
				chan.colorOverride=colorOverride[0];
				item=item.substring(0,x);
			}
			chan.name=item.toUpperCase().trim();
			channelList.add(chan);
		}
		while(ilist.length()>0)
		{
			int x=ilist.indexOf(',');

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
			int y1=item.indexOf(' ');
			int y2=item.lastIndexOf(' ');
			if((y1<0)||(y2<=y1)) continue;
			CMChannel chan=new CMChannel();
			String lvl=item.substring(y1+1,y2).trim();
			String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			chan.name=item.toUpperCase().trim();
			String[] colorOverride=new String[]{""};
			chan.mask=parseOutFlags(lvl,chan.flags,colorOverride);
			chan.colorOverride=colorOverride[0];
			chan.i3name=ichan;
			channelList.add(chan);
		}
		while(imc2list.length()>0)
		{
			int x=imc2list.indexOf(',');

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
			int y1=item.indexOf(' ');
			int y2=item.lastIndexOf(' ');
			if((y1<0)||(y2<=y1)) continue;
			CMChannel chan=new CMChannel();
			String lvl=item.substring(y1+1,y2).trim();
			String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			chan.name=item.toUpperCase().trim();
			String[] colorOverride=new String[]{""};
			chan.mask=parseOutFlags(lvl,chan.flags,colorOverride);
			chan.colorOverride=colorOverride[0];
			chan.imc2Name=ichan;
			channelList.add(chan);
		}
		baseChannelNames=new String[channelList.size()];
		for(int i=0;i<channelList.size();i++)
			baseChannelNames[i]=channelList.get(i).name;
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CHANNELAUCTION))
		{
			CMChannel chan=new CMChannel();
			chan.name="AUCTION";
			channelList.add(chan);
		}
		return channelList.size();
	}
	
	public boolean channelTo(Session ses, boolean areareq, int channelInt, CMMsg msg, MOB sender)
	{
		MOB M=ses.mob();
		boolean didIt=false;
		if(mayReadThisChannel(sender,areareq,ses,channelInt)
		&&(M.location()!=null)
		&&(M.location().okMessage(ses.mob(),msg)))
		{
			M.executeMsg(M,msg);
			didIt=true;
			if(msg.trailerMsgs()!=null)
			{
				for(CMMsg msg2 : msg.trailerMsgs())
					if((msg!=msg2)&&(M.location()!=null)&&(M.location().okMessage(M,msg2)))
						M.executeMsg(M,msg2);
				msg.trailerMsgs().clear();
			}
		}
		return didIt;
	}
	
	public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg)
	{
		int channelInt=getChannelIndex(channelName);
		if(channelInt<0) return;
		
		final PlayerStats pStats=mob.playerStats();
		
		message=CMProps.applyINIFilter(message,CMProps.SYSTEM_CHANNELFILTER);
		CMChannel chan=getChannel(channelInt);
		Set<ChannelFlag> flags=chan.flags;
		channelName=chan.name;
		String channelColor=chan.colorOverride;
		if(channelColor.length()==0)
			channelColor="^Q";

		CMMsg msg=null;
		if(systemMsg)
		{
			String str="["+channelName+"] '"+message+"'^</CHANNEL^>^?^.";
			if((!mob.name().startsWith("^"))||(mob.name().length()>2))
				str="<S-NAME> "+str;
			msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,channelColor+"^<CHANNEL \""+channelName+"\"^>"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+"^<CHANNEL \""+channelName+"\"^>"+str);
		}
		else
		if(message.startsWith(",")
		||(message.startsWith(":")
			&&(message.length()>1)
			&&(Character.isLetter(message.charAt(1))||message.charAt(1)==' ')))
		{
			String msgstr=message.substring(1);
			Vector<String> V=CMParms.parse(msgstr);
			Social S=CMLib.socials().fetchSocial(V,true,false);
			if(S==null) S=CMLib.socials().fetchSocial(V,false,false);
			if(S!=null)
				msg=S.makeChannelMsg(mob,channelInt,channelName,V,false);
			else
			{
				msgstr=CMProps.applyINIFilter(msgstr,CMProps.SYSTEM_EMOTEFILTER);
				if(msgstr.trim().startsWith("'")||msgstr.trim().startsWith("`"))
					msgstr=msgstr.trim();
				else
					msgstr=" "+msgstr.trim();
				String srcstr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+mob.name()+msgstr+"^</CHANNEL^>^N^.";
				String reststr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] <S-NAME>"+msgstr+"^</CHANNEL^>^N^.";
				msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,channelColor+""+srcstr,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+reststr);
			}
		}
		else
			msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,channelColor+"^<CHANNEL \""+channelName+"\"^>You "+channelName+" '"+message+"'^</CHANNEL^>^N^.",CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),channelColor+"^<CHANNEL \""+channelName+"\"^><S-NAME> "+channelName+"S '"+message+"'^</CHANNEL^>^N^.");
		if((chan.flags.contains(ChannelsLibrary.ChannelFlag.ACCOUNTOOC))
		&&(pStats!=null)
		&&(pStats.getAccount()!=null)
		&&(msg.source()==mob))
		{
			String accountName=pStats.getAccount().accountName();
			if(msg.sourceMessage()!=null)
				msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(), "<S-NAME>", accountName));
			if(msg.targetMessage()!=null)
				msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(), "<S-NAME>", accountName));
			if(msg.othersMessage()!=null)
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<S-NAME>", accountName));
		}
		CMLib.commands().monitorGlobalMessage(mob.location(), msg);
		if((mob.location()!=null)
		&&((!mob.location().isInhabitant(mob))||(mob.location().okMessage(mob,msg))))
		{
			boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
			channelQueUp(channelInt,msg);
			for(Session S : CMLib.sessions().localOnlineIterable())
				channelTo(S,areareq,channelInt,msg,mob);
		}
		if((CMLib.intermud().i3online()&&(CMLib.intermud().isI3channel(channelName)))
		||(CMLib.intermud().imc2online()&&(CMLib.intermud().isIMC2channel(channelName))))
			CMLib.intermud().i3channel(mob,channelName,message);
	}
	
}
