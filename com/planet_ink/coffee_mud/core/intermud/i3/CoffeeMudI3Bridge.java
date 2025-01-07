package com.planet_ink.coffee_mud.core.intermud.i3;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.Serializable;

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
public class CoffeeMudI3Bridge implements ImudServices, Serializable
{
	public static final long serialVersionUID=0;

	public String			version		= "CoffeeMud vX.X";
	public String			name		= "CoffeeMud";
	public String			i3state		= "Development";
	public Room				universalR	= null;
	public int				port		= 5555;
	public List<CMChannel>	channels	= new XVector<CMChannel>();
	public Map<String,Long>	inkeys		= new LimitedTreeMap<String,Long>(1200000,1000,true);
	public Map<String,Long>	outkeys		= new LimitedTreeMap<String,Long>(1200000,1000,true);

	private static volatile long lastPacketReceivedTime = System.currentTimeMillis();

	String[][] i3ansi_conversion=
	{
		/*
		 * Conversion Format Below:
		 *
		 * { "&lt;MUD TRANSLATION&gt;", "PINKFISH", "ANSI TRANSLATION" }
		 *
		 * Foreground Standard Colors
		 */
		{ "^K", "%^BLACK%^",   "\033[0;0;30m" }, // Black
		{ "^R", "%^RED%^",     "\033[0;0;31m" }, // Dark Red
		{ "^G", "%^GREEN%^",   "\033[0;0;32m" }, // Dark Green
		{ "^Y", "%^ORANGE%^",  "\033[0;0;33m" }, // Orange/Brown
		{ "^B", "%^BLUE%^",    "\033[0;0;34m" }, // Dark Blue
		{ "^P", "%^MAGENTA%^", "\033[0;0;35m" }, // Purple/Magenta
		{ "^C", "%^CYAN%^",    "\033[0;0;36m" }, // Cyan
		{ "^W", "%^WHITE%^",   "\033[0;0;37m" }, // Grey

		/* Background colors */
		{ "", "%^B_BLACK%^",   "\033[40m" }, // Black
		{ "", "%^B_RED%^",     "\033[41m" }, // Red
		{ "", "%^B_GREEN%^",   "\033[42m" }, // Green
		{ "", "%^B_ORANGE%^",  "\033[43m" }, // Orange
		{ "", "%^B_YELLOW%^",  "\033[43m" }, // Yellow, which may as well be orange since ANSI doesn't do that
		{ "", "%^B_BLUE%^",    "\033[44m" }, // Blue
		{ "", "%^B_MAGENTA%^", "\033[45m" }, // Purple/Magenta
		{ "", "%^B_CYAN%^",    "\033[46m" }, // Cyan
		{ "", "%^B_WHITE%^",   "\033[47m" }, // White

		/* Text Affects */
		{ "^.", "%^RESET%^",	 "\033[0m" }, // Reset Text
		{ "^.", "%^RESET%^",	 "\033[0m" }, // Reset Text
		{ "^H", "%^BOLD%^", 	 "\033[1m" }, // Bolden Text(Brightens it)
		{ "^.", "%^EBOLD%^",	 "\033[0m" }, // Assumed to be a reset tag to stop bold
		{ "^_", "%^UNDERLINE%^", "\033[4m" }, // Underline Text
		{ "^*", "%^FLASH%^",	 "\033[5m" }, // Blink Text
		{ "^/", "%^ITALIC%^",    "\033[6m" }, // Italic Text
		{ "", "%^REVERSE%^",   "\033[7m" }, // Reverse Background and Foreground Colors

		/* Foreground extended colors */
		{ "^k", "%^BLACK%^%^BOLD%^",   "\033[0;1;30m" }, // Dark Grey
		{ "^r", "%^RED%^%^BOLD%^",     "\033[0;1;31m" }, // Red
		{ "^g", "%^GREEN%^%^BOLD%^",   "\033[0;1;32m" }, // Green
		{ "^y", "%^YELLOW%^",   	   "\033[0;1;33m" }, // Yellow
		{ "^b", "%^BLUE%^%^BOLD%^",    "\033[0;1;34m" }, // Blue
		{ "^p", "%^MAGENTA%^%^BOLD%^", "\033[0;1;35m" }, // Pink
		{ "^c", "%^CYAN%^%^BOLD%^",    "\033[0;1;36m" }, // Light Blue
		{ "^w", "%^WHITE%^%^BOLD%^",   "\033[0;1;37m" }, // White

		/* Blinking foreground standard color */
		{ "^K^*", "%^BLACK%^%^FLASH%^", 		  "\033[0;5;30m" }, // Black
		{ "^R^*", "%^RED%^%^FLASH%^",   		  "\033[0;5;31m" }, // Dark Red
		{ "^G^*", "%^GREEN%^%^FLASH%^", 		  "\033[0;5;32m" }, // Dark Green
		{ "^Y^*", "%^ORANGE%^%^FLASH%^",		  "\033[0;5;33m" }, // Orange/Brown
		{ "^B^*", "%^BLUE%^%^FLASH%^",  		  "\033[0;5;34m" }, // Dark Blue
		{ "^P^*", "%^MAGENTA%^%^FLASH%^",   	  "\033[0;5;35m" }, // Magenta/Purple
		{ "^C^*", "%^CYAN%^%^FLASH%^",  		  "\033[0;5;36m" }, // Cyan
		{ "^W^*", "%^WHITE%^%^FLASH%^", 		  "\033[0;5;37m" }, // Grey
		{ "^k^*", "%^BLACK%^%^BOLD%^%^FLASH%^",   "\033[1;5;30m" }, // Dark Grey
		{ "^r^*", "%^RED%^%^BOLD%^%^FLASH%^",     "\033[1;5;31m" }, // Red
		{ "^g^*", "%^GREEN%^%^BOLD%^%^FLASH%^",   "\033[1;5;32m" }, // Green
		{ "^y^*", "%^YELLOW%^%^FLASH%^",		  "\033[1;5;33m" }, // Yellow
		{ "^b^*", "%^BLUE%^%^BOLD%^%^FLASH%^",    "\033[1;5;34m" }, // Blue
		{ "^p^*", "%^MAGENTA%^%^BOLD%^%^FLASH%^", "\033[1;5;35m" }, // Pink
		{ "^c^*", "%^CYAN%^%^BOLD%^%^FLASH%^",    "\033[1;5;36m" }, // Light Blue
		{ "^w^*", "%^WHITE%^%^BOLD%^%^FLASH%^",   "\033[1;5;37m" }  // White
	};

	public CoffeeMudI3Bridge(final String name, final String version, final int port, final String i3status, final List<CMChannel> channels)
	{
		if(name!=null)
			this.name=name;
		if(i3status!=null)
			this.i3state=i3status;
		if(version!=null)
			this.version=version;
		if(channels!=null)
			this.channels=channels;
		else
		if(this.channels.size()==0)
		{
			this.channels.add(CMLib.channels().createNewChannel("I3CHAT", "diku_chat", "", "", new HashSet<ChannelFlag>(),"","",""));
			this.channels.add(CMLib.channels().createNewChannel("I3GOSSIP", "diku_immortals", "", "", new HashSet<ChannelFlag>(),"","",""));
			this.channels.add(CMLib.channels().createNewChannel("GREET", "diku_code", "", "", new HashSet<ChannelFlag>(),"","",""));
		}
		this.port=port;
	}

	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	public void setChannelsMap(final List<CMChannel> channels)
	{
		this.channels=channels;
	}

	protected MOB findSessMob(final String mobName)
	{
		return CMLib.sessions().findCharacterOnline(mobName, true);
	}

	protected Room getUniversalRoom()
	{
		if(universalR==null)
		{
			universalR=CMClass.getLocale("StdRoom");
		}
		return universalR;
	}

	public String fixColors(final String str)
	{
		final StringBuffer buf=new StringBuffer(str);
		int startedAt=-1;
		for(int i=0;i<buf.length();i++)
		{
			if(buf.charAt(i)=='%')
			{
				if(startedAt<0)
					startedAt=i;
				else
				if(((i+1)<buf.length())&&(buf.charAt(i+1)=='^'))
				{
					String found=null;
					final String code=buf.substring(startedAt,i+2);
					for (final String[] element : i3ansi_conversion)
					{
						if(code.equals(element[1]))
						{
							found=element[0];
							break;
						}
					}
					if(found!=null)
					{
						buf.replace(startedAt,i+2,found);
						i=startedAt+1;
					}
					startedAt=-1;
				}
			}
		}
		return buf.toString();
	}

	public String socialFixIn(String str)
	{

		str=CMStrings.replaceAll(str,"$N","<S-NAME>");
		str=CMStrings.replaceAll(str,"$n","<S-NAME>");
		str=CMStrings.replaceAll(str,"$T","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$t","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$O","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$o","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$m","<S-HIM-HER>");
		str=CMStrings.replaceAll(str,"$M","<T-HIM-HER>");
		str=CMStrings.replaceAll(str,"$s","<S-HIS-HER>");
		str=CMStrings.replaceAll(str,"$S","<T-HIS-HER>");
		str=CMStrings.replaceAll(str,"$e","<S-HE-SHE>");
		str=CMStrings.replaceAll(str,"$E","<T-HE-SHE>");
		str=CMStrings.replaceAll(str,"`","\'");
		if(str.equals("$"))
			return "";
		return str.trim();
	}

	@Override
	public long getLastPacketReceivedTime()
	{
		return lastPacketReceivedTime;
	}

	@Override
	public void resetLastPacketReceivedTime()
	{
		lastPacketReceivedTime=System.currentTimeMillis();
	}

	@Override
	public Map<String,Long> getIncomingKeys()
	{
		return inkeys;
	}

	@Override
	public Map<String,Long> getOutgoingKeys()
	{
		return outkeys;
	}

	/**
	 * Handles an incoming I3 packet asynchronously.
	 * An implementation should make sure that asynchronously
	 * processing the incoming packet will not have any
	 * impact, otherwise you could end up with bizarre
	 * behaviour like an intermud chat line appearing
	 * in the middle of a room description.  If your
	 * mudlib is not prepared to handle multiple threads,
	 * just stack up incoming packets and pull them off
	 * the stack during your main thread of execution.
	 * @param packet the incoming packet
	 */
	@Override
	public void receive(final Packet packet)
	{
		switch(packet.getType())
		{
		case CHANNEL_E:
		case CHANNEL_M:
		case CHANNEL_T:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelPacket ck=(ChannelPacket)packet;
				String channelName=ck.channel;
				channelName = I3Client.getLocalChannel(channelName);
				CMMsg msg=null;

				if((ck.sender_mud!=null)&&(ck.sender_mud.equalsIgnoreCase(getMudName())))
					return;
				if((ck.channel==null)||(ck.channel.length()==0))
					return;
				final String channelColor="^Q";
				final ChannelsLibrary channels = CMLib.channels();
				final int channelInt=channels.getChannelIndex(channelName);
				int channelCode=channelInt;
				if(channelInt < 0)
				{
					channelCode=47;
				}
				ck.message=fixColors(CMProps.applyINIFilter(ck.message,CMProps.Str.CHANNELFILTER));
				final MOB mob=CMClass.getFactoryMOB();
				mob.setName(ck.sender_name+"@"+ck.sender_mud);
				mob.setLocation(getUniversalRoom());
				MOB targetMOB=null;
				boolean killtargetmob=false;
				if(ck.type==Packet.PacketType.CHANNEL_T)
				{
					final ChannelTargetEmote ct = (ChannelTargetEmote)ck;
					if(ct.message_target != null)
						ct.message_target = fixColors(CMProps.applyINIFilter(ct.message_target,CMProps.Str.CHANNELFILTER));
					if((ct.target_mud!=null)
					&&(ct.target_mud.equals(getMudName()))
					&&(CMLib.players().isLoadedPlayer(ct.target_name)))
						targetMOB=CMLib.players().getPlayer(ct.target_name);
					if((ct.target_visible_name!=null)&&(targetMOB==null))
					{
						killtargetmob=true;
						targetMOB=CMClass.getFactoryMOB();
						targetMOB.setName(ct.target_visible_name+"@"+ck.target_mud);
						targetMOB.setLocation(getUniversalRoom());
					}
					String msgs=socialFixIn(ck.message);
					msgs=CMProps.applyINIFilter(msgs,CMProps.Str.EMOTEFILTER);
					String targmsgs=socialFixIn(ct.message_target);
					targmsgs=CMProps.applyINIFilter(targmsgs,CMProps.Str.EMOTEFILTER);
					final String str=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+msgs+"^</CHANNEL^>^N^.";
					final String str2=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+targmsgs+"^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,targetMOB,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str2,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str);
				}
				else
				if(ck.type==Packet.PacketType.CHANNEL_E)
				{
					String msgs=socialFixIn(ck.message);
					msgs=CMProps.applyINIFilter(msgs,CMProps.Str.EMOTEFILTER);
					final String str=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+msgs+"^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str);
				}
				else
				{
					final String str=channelColor+"^<CHANNEL \""+channelName+"\"^>"+mob.name()+" "+channelName+"(S) '"+ck.message+"'^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str);
				}
				CMLib.commands().monitorGlobalMessage(mob.location(), msg);
				try
				{
					if(channelInt>=0)
						channels.channelQueUp(channelInt, msg, 0);
				}
				catch(final Exception e)
				{
					Log.errOut(e);
				}
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					final MOB M=S.mob();
					final ChannelsLibrary myChanLib=CMLib.get(S)._channels();
					final int chanNum = (myChanLib == channels) ? channelInt : myChanLib.getChannelIndex(channelName);
					if((chanNum >= 0)
					&&(myChanLib.mayReadThisChannel(mob,false,S,chanNum))
					&&(M.location()!=null))
					{
						msg.setOthersCode(CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+chanNum));
						if(M.location().okMessage(M,msg))
							M.executeMsg(M,msg);
					}
				}
				mob.destroy();
				if((targetMOB!=null)&&(killtargetmob))
					targetMOB.destroy();
			}
			break;
		case LOCATE_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final LocateQueryPacket lk=(LocateQueryPacket)packet;
				String stat="online";
				String name=CMStrings.capitalizeAndLower(lk.user_name);
				final MOB smob=findSessMob(lk.user_name);
				if(smob!=null)
				{
					if(CMLib.flags().isCloaked(smob))
						stat="exists, but not logged in";
				}
				else
				if(CMLib.players().getPlayer(lk.user_name)!=null)
					stat="exists, but not logged in";
				else
				if(CMLib.players().playerExists(lk.user_name))
					stat="exists, but is not online";
				else
					name=null;
				if(name!=null)
				{
					final LocateReplyPacket lpk=new LocateReplyPacket(lk.sender_name,lk.sender_mud,name,0,stat);
					try
					{
						lpk.send();
					}
					catch (final Exception e)
					{
						Log.errOut("IMudClient", e);
					}
				}
			}
			break;
		case LOCATE_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final LocateReplyPacket lk=(LocateReplyPacket)packet;
				final MOB smob=findSessMob(lk.target_name);
				if(smob!=null)
					smob.tell(fixColors(lk.located_visible_name)+"@"+fixColors(lk.located_mud_name)+" ("+lk.idle_time+"): "+fixColors(lk.status));
			}
			break;
		case FINGER_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final FingerRequest lk=(FingerRequest)packet;
				Packet pkt;
				final MOB M=CMLib.players().getLoadPlayer(lk.target_name);
				if(M==null)
					pkt=new ErrorPacket(lk.sender_name,lk.sender_mud,"unk-user","User "+lk.target_name+" is not known here.","0");
				else
				{
					final FingerReply fpkt = new FingerReply(lk.sender_name,lk.sender_mud);
					pkt=fpkt;
					fpkt.e_mail="0";
					final Session sess=M.session();
					if((sess==null)||(!sess.isAfk()))
						fpkt.idle_time="-1";
					else
						fpkt.idle_time=Long.toString(sess.getIdleMillis()/1000);
					fpkt.ip_time="0"; // what IS this?
					fpkt.loginout_time=CMLib.time().date2String(M.playerStats().getLastDateTime());
					fpkt.real_name="0"; // don't even know this
					if(M.titledName().equals(M.name()))
						fpkt.title="An ordinary "+M.charStats().displayClassName();
					else
						fpkt.title=M.titledName();
					fpkt.visible_name=M.name();
					fpkt.extra=M.name()+" is a "+M.charStats().raceName()+" "+M.charStats().displayClassName();
				}
				try
				{
					pkt.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IMudClient", e);
				}
			}
			break;
		case FINGER_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final FingerReply lk=(FingerReply)packet;
				final MOB smob=findSessMob(lk.target_name);
				if(smob!=null)
				{
					final StringBuilder response=new StringBuilder("");
					if((lk.visible_name.length()>0)&&(!lk.visible_name.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Name"),10)).append(": ^N").append(lk.visible_name).append("\n\r");
					if((lk.title.length()>0)&&(!lk.title.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Title"),10)).append(": ^N").append(lk.title).append("\n\r");
					if((lk.real_name.length()>0)&&(!lk.real_name.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Real Name"),10)).append(": ^N").append(lk.real_name).append("\n\r");
					if((lk.e_mail.length()>0)&&(!lk.e_mail.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Email"),10)).append(": ^N").append(lk.e_mail).append("\n\r");
					if((lk.loginout_time.length()>0)&&(!lk.loginout_time.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Logged"),10)).append(": ^N").append(lk.loginout_time).append("\n\r");
					if((lk.ip_time.length()>0)&&(!lk.ip_time.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("IP Time"),10)).append(": ^N").append(lk.ip_time).append("\n\r");
					if((lk.extra.length()>0)&&(!lk.extra.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Extra"),10)).append(": ^N").append(lk.extra).append("\n\r");
					smob.tell(response.toString());
				}
			}
			break;
		case AUTH_MUD_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final MudAuthRequest lk=(MudAuthRequest)packet;
				if(lk.sender_mud.equalsIgnoreCase(I3Server.getMudName()))
				{
					if(CMSecurity.isDebugging(DbgFlag.I3))
						Log.debugOut("I3","Received my own mud-auth.");
				}
				else
					Log.sysOut("I3","MUD "+lk.sender_mud+" wants to mud-auth.");
				inkeys.put(lk.sender_mud, Long.valueOf(new Random(System.currentTimeMillis()).nextInt(999999)));
				final MudAuthReply pkt = new MudAuthReply(lk.sender_mud, inkeys.get(lk.sender_mud).intValue());
				try
				{
					pkt.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IMudClient", e);
				}
			}
			break;
		case OOB_REQ:
			{
				final OOBReq lk=(OOBReq)packet;
				if(lk.target_mud.equalsIgnoreCase(I3Server.getMudName()))
					inkeys.put(lk.sender_mud, Long.valueOf(-1));
				break;
			}
		case AUTH_MUD_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final MudAuthReply lk=(MudAuthReply)packet;
				if(lk.sender_mud.equalsIgnoreCase(I3Server.getMudName()))
				{
					if(CMSecurity.isDebugging(DbgFlag.I3))
						Log.debugOut("I3","I replied to my mud-auth.");
					lastPacketReceivedTime=System.currentTimeMillis();
				}
				else
				{
					outkeys.put(lk.sender_mud, Long.valueOf(lk.key));
					//TODO: if we have stuff to deliver, connect, and deliver it now
					Log.sysOut("I3","MUD "+lk.sender_mud+" replied to my mud-auth with key "+lk.key+".");
				}
			}
			break;
		case WHO_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final WhoReplyPacket wk=(WhoReplyPacket)packet;
				final MOB smob=findSessMob(wk.target_name);
				if(smob!=null)
				{
					final StringBuffer buf=new StringBuffer("\n\rwhois@"+fixColors(wk.sender_mud)+":\n\r");
					final Vector<?> V=wk.who;
					if(V.size()==0)
						buf.append("Nobody!");
					else
					for(int v=0;v<V.size();v++)
					{
						final Vector<?> V2=(Vector<?>)V.elementAt(v);
						final String nom = fixColors((String)V2.elementAt(0));
						final String idle=V2.elementAt(1).toString();
						final String xtra = fixColors(V2.elementAt(2).toString());
						buf.append("["+CMStrings.padRight(nom,20)+"] "+xtra+" ("+idle+")\n\r");
					}
					smob.session().wraplessPrintln(buf.toString());
					break;
				}
			}
			break;
		case CHAN_WHO_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelWhoReply wk=(ChannelWhoReply)packet;
				final MOB smob=findSessMob(wk.target_name);
				if(smob!=null)
				{
					final StringBuffer buf=new StringBuffer("\n\rListening on "+wk.channel+"@"+fixColors(wk.sender_mud)+":\n\r");
					final Vector<?> V=wk.who;
					if(V.size()==0)
						buf.append("Nobody!");
					else
					for(int v=0;v<V.size();v++)
					{
						final String nom = fixColors((String)V.elementAt(v));
						buf.append("["+CMStrings.padRight(nom,20)+"]\n\r");
					}
					smob.session().wraplessPrintln(buf.toString());
					smob.session().setPromptFlag(true);
					break;
				}
			}
			break;
		case CHAN_WHO_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelWhoRequest wk=(ChannelWhoRequest)packet;
				final ChannelWhoReply wkr=new ChannelWhoReply();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				wkr.channel=wk.channel;
				final String locChannel = I3Client.getRemoteChannel(wk.channel);
				final int channelInt=CMLib.channels().getChannelIndex(locChannel);
				final Vector<String> whoV=new Vector<String>();
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					final MOB M=S.mob();
					if((CMLib.channels().mayReadThisChannel(M,false,S,channelInt))
					&&(M!=null)
					&&(!CMLib.flags().isCloaked(M)))
						whoV.addElement(M.name());
				}
				wkr.who=whoV;
				try
				{
					wkr.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IMudClient", e);
				}
			}
			break;
		case CHAN_USER_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelUserRequest wk=(ChannelUserRequest)packet;
				final ChannelUserReply wkr=new ChannelUserReply();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				wkr.userRequested=wk.userToRequest;
				final MOB M=CMLib.players().getLoadPlayer(wk.userToRequest);
				if(M!=null)
				{
					wkr.userVisibleName = M.name();
					wkr.gender=(char)M.charStats().getStat(CharStats.STAT_GENDER);
					try
					{
						wkr.send();
					}
					catch (final Exception e)
					{
						Log.errOut("IMudClient", e);
					}
				}
			}
			break;
		case WHO_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final WhoReqPacket wk=(WhoReqPacket)packet;
				final WhoReplyPacket wkr=new WhoReplyPacket();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				final Vector<Vector<Object>> whoV=new Vector<Vector<Object>>();
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					MOB smob=S.mob();
					if((smob!=null)&&(smob.soulMate()!=null))
						smob=smob.soulMate();
					if((!S.isStopped())&&(smob!=null)
					&&(!smob.amDead())
					&&(CMLib.flags().isInTheGame(smob,true))
					&&(!CMLib.flags().isCloaked(smob)))
					{
						final Vector<Object> whoV2=new Vector<Object>();
						whoV2.addElement(smob.name());
						whoV2.addElement(Integer.valueOf((int)(S.getIdleMillis()/1000)));
						whoV2.addElement(smob.charStats().displayClassLevel(smob,true));
						whoV.addElement(whoV2);
					}
				}
				wkr.who=whoV;
				try
				{
					wkr.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IMudClient", e);
				}
			}
			break;
		case TELL:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final TellPacket tk=(TellPacket)packet;
				final MOB smob=findSessMob(tk.target_name);
				if(smob!=null)
				{
					MOB mob=null;
					final PlayerStats pstats=smob.playerStats();
					if(pstats!=null)
					{
						if((pstats.getReplyToMOB()!=null)&&(pstats.getReplyToMOB().Name().indexOf('@')>=0))
							mob=pstats.getReplyToMOB();
						else
							mob=CMClass.getFactoryMOB();
						pstats.setReplyTo(mob, PlayerStats.REPLY_TELL);
					}
					else
						mob=CMClass.getFactoryMOB();
					mob.setName(tk.sender_name+"@"+tk.sender_mud);
					mob.setLocation(getUniversalRoom());
					tk.message=fixColors(CMProps.applyINIFilter(tk.message,CMProps.Str.SAYFILTER));
					CMLib.commands().postSay(mob,smob,tk.message,true,true);
				}
			}
			break;
		default:
			Log.errOut("IMudInterface","Unknown type: "+packet.getType());
			break;
		}
	}

	/**
	 * @return an enumeration of channels this mud subscribes to
	 */
	@Override
	public java.util.Enumeration<String> getChannels()
	{
		final Vector<String> V=new Vector<String>();
		for(final CMChannel chan : channels)
			V.addElement(chan.i3name());
		return V.elements();
	}

	/**
	 * Register a fake channel
	 * @param chan the remote channel name
	 * @return the local channel name for the specified new local channel name
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices#getLocalChannel
	 */
	@Override
	public boolean addChannel(final CMChannel chan)
	{
		if((getLocalChannel(chan.i3name()).length()==0)
		&&(getRemoteChannel(chan.name()).length()==0))
		{
			channels.add(chan);
			return true;
		}
		return false;
	}

	/**
	 * Remote a channel
	 * @param remoteChannelName the remote name
	 * @return true if remove succeeds
	 */
	@Override
	public boolean delChannel(final String remoteChannelName)
	{
		for(int i=0;i<channels.size();i++)
		{
			if(channels.get(i).i3name().equalsIgnoreCase(remoteChannelName))
			{
				channels.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Given a I3 channel name, this method should provide
	 * the local name for that channel.
	 * Example:
	 *
	 * if( str.equals("imud_code") ) return "intercre";
	 *
	 * @param str the remote name of the desired channel
	 * @return the local channel name for a remote channel
	 * @see #getRemoteChannel
	 */
	@Override
	public String getLocalChannel(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.i3name().equalsIgnoreCase(str))
				return chan.name();
		}
		return "";
	}

	/**
	 * Given a local channel name, this method should provide
	 * the local mask for that channel.
	 * Example:
	 *
	 * if( str.equals("ICODE") ) return "";
	 *
	 * @param str the local name of the desired channel
	 * @return the local channel mask for a remote channel
	 * @see #getLocalMask
	 */
	public String getLocalMask(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.name().equalsIgnoreCase(str))
				return chan.mask();
		}
		return "";
	}

	/**
	 * @return the name of this mud
	 */
	@Override
	public String getMudName()
	{
		return name;
	}

	/**
	 * @return the software name and version
	 */
	@Override
	public String getMudVersion()
	{
		return version;
	}

	/**
	 * @return the software name and version
	 */
	@Override
	public String getMudState()
	{
		return i3state;
	}

	/**
	 * @return the player port for this mud
	 */
	@Override
	public int getMudPort()
	{
		return port;
	}

	/**
	 * Given a remote channel name, returns the mask
	 * required.
	 * Example:
	 *
	 * if( str.equals("intercre") ) return "";
	 *
	 * @param str the remote name of the desired channel
	 * @return the remote mask of the specified local channel
	 */
	@Override
	public String getRemoteMask(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.i3name().equalsIgnoreCase(str))
				return chan.mask();
		}
		return "";
	}

	/**
	 * Given a local channel name, returns the remote
	 * channel name.
	 * Example:
	 *
	 * if( str.equals("intercre") ) return "imud_code";
	 *
	 * @param str the local name of the desired channel
	 * @return the remote name of the specified local channel
	 */
	@Override
	public String getRemoteChannel(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.name().equalsIgnoreCase(str))
				return chan.i3name();
		}
		return "";
	}
}
