package com.planet_ink.coffee_mud.core.intermud;
import com.planet_ink.coffee_mud.core.intermud.packets.*;
import com.planet_ink.coffee_mud.core.intermud.persist.*;
import com.planet_ink.coffee_mud.core.intermud.server.*;
import com.planet_ink.coffee_mud.core.intermud.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.Serializable;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class IMudInterface implements ImudServices, Serializable
{
	public static final long serialVersionUID=0;
	public String version="CoffeeMud vX.X";
	public String name="CoffeeMud";
	public String i3state="Development";
	public int port=5555;
	public String[][] channels={{"diku_chat","CHAT",""},
								{"diku_immortals","GOSSIP",""},
								{"diku_code","GREET",""}};

	String[][] i3ansi_conversion=
	{
		/*
		 * Conversion Format Below:
		 *
		 * { "<MUD TRANSLATION>", "PINKFISH", "ANSI TRANSLATION" }
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
		{ "^.", "%^RESET%^",     "\033[0m" }, // Reset Text
		{ "^.", "%^RESET%^",     "\033[0m" }, // Reset Text
		{ "^H", "%^BOLD%^",      "\033[1m" }, // Bolden Text(Brightens it)
		{ "^.", "%^EBOLD%^",	 "\033[0m" }, // Assumed to be a reset tag to stop bold
		{ "^_", "%^UNDERLINE%^", "\033[4m" }, // Underline Text
		{ "^*", "%^FLASH%^",     "\033[5m" }, // Blink Text
		{ "^/", "%^ITALIC%^",    "\033[6m" }, // Italic Text
		{ "", "%^REVERSE%^",   "\033[7m" }, // Reverse Background and Foreground Colors

		/* Foreground extended colors */
		{ "^k", "%^BLACK%^%^BOLD%^",   "\033[0;1;30m" }, // Dark Grey
		{ "^r", "%^RED%^%^BOLD%^",     "\033[0;1;31m" }, // Red
		{ "^g", "%^GREEN%^%^BOLD%^",   "\033[0;1;32m" }, // Green
		{ "^y", "%^YELLOW%^",          "\033[0;1;33m" }, // Yellow
		{ "^b", "%^BLUE%^%^BOLD%^",    "\033[0;1;34m" }, // Blue
		{ "^p", "%^MAGENTA%^%^BOLD%^", "\033[0;1;35m" }, // Pink
		{ "^c", "%^CYAN%^%^BOLD%^",    "\033[0;1;36m" }, // Light Blue
		{ "^w", "%^WHITE%^%^BOLD%^",   "\033[0;1;37m" }, // White

		/* Blinking foreground standard color */
		{ "^K^*", "%^BLACK%^%^FLASH%^",           "\033[0;5;30m" }, // Black
		{ "^R^*", "%^RED%^%^FLASH%^",             "\033[0;5;31m" }, // Dark Red
		{ "^G^*", "%^GREEN%^%^FLASH%^",           "\033[0;5;32m" }, // Dark Green
		{ "^Y^*", "%^ORANGE%^%^FLASH%^",          "\033[0;5;33m" }, // Orange/Brown
		{ "^B^*", "%^BLUE%^%^FLASH%^",            "\033[0;5;34m" }, // Dark Blue
		{ "^P^*", "%^MAGENTA%^%^FLASH%^",         "\033[0;5;35m" }, // Magenta/Purple
		{ "^C^*", "%^CYAN%^%^FLASH%^",            "\033[0;5;36m" }, // Cyan
		{ "^W^*", "%^WHITE%^%^FLASH%^",           "\033[0;5;37m" }, // Grey
		{ "^k^*", "%^BLACK%^%^BOLD%^%^FLASH%^",   "\033[1;5;30m" }, // Dark Grey
		{ "^r^*", "%^RED%^%^BOLD%^%^FLASH%^",     "\033[1;5;31m" }, // Red
		{ "^g^*", "%^GREEN%^%^BOLD%^%^FLASH%^",   "\033[1;5;32m" }, // Green
		{ "^y^*", "%^YELLOW%^%^FLASH%^",          "\033[1;5;33m" }, // Yellow
		{ "^b^*", "%^BLUE%^%^BOLD%^%^FLASH%^",    "\033[1;5;34m" }, // Blue
		{ "^p^*", "%^MAGENTA%^%^BOLD%^%^FLASH%^", "\033[1;5;35m" }, // Pink
		{ "^c^*", "%^CYAN%^%^BOLD%^%^FLASH%^",    "\033[1;5;36m" }, // Light Blue
		{ "^w^*", "%^WHITE%^%^BOLD%^%^FLASH%^",   "\033[1;5;37m" }  // White
	};



	public IMudInterface (String Name, String Version, int Port, String i3status, String[][] Channels)
	{
		if(Name!=null) name=Name;
		if(i3status!=null) i3state=i3status;
		if(Version!=null) version=Version;
		if(Channels!=null) channels=Channels;
		port=Port;
	}

	protected MOB findSessMob(String mobName)
	{
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session ses=CMLib.sessions().elementAt(s);
			if((!ses.killFlag())&&(ses.mob()!=null)
			&&(!ses.mob().amDead())
			&&(ses.mob().Name().equalsIgnoreCase(mobName))
			&&(ses.mob().location()!=null))
				return ses.mob();
		}
		return null;
	}

	public String fixColors(String str)
	{
		StringBuffer buf=new StringBuffer(str);
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
					String code=buf.substring(startedAt,i+2);
					for(int x=0;x<i3ansi_conversion.length;x++)
					{
						if(code.equals(i3ansi_conversion[x][1]))
						{found=i3ansi_conversion[x][0]; break;}
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
		if(str.equals("$")) return "";
		return str.trim();
	}

    public void destroymob(MOB mob)
    {
        if(mob==null) return;
        Room R=mob.location();
        mob.destroy();
        if(R!=null) R.destroy();
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
	public void receive(Packet packet)
	{
		switch(packet.type)
		{
		case Packet.CHAN_EMOTE:
		case Packet.CHAN_MESSAGE:
		case Packet.CHAN_TARGET:
			{
				ChannelPacket ck=(ChannelPacket)packet;
				String channelName=ck.channel;
				CMMsg msg=null;

				if((ck.sender_mud!=null)&&(ck.sender_mud.equalsIgnoreCase(getMudName())))
				   return;
				if((ck.channel==null)||(ck.channel.length()==0))
					return;
				int channelInt=CMLib.channels().getChannelIndex(channelName);
				if(channelInt<0) return;
				ck.message=fixColors(CMProps.applyINIFilter(ck.message,CMProps.SYSTEM_CHANNELFILTER));
				if(ck.message_target!=null)
				    ck.message_target=fixColors(CMProps.applyINIFilter(ck.message_target,CMProps.SYSTEM_CHANNELFILTER));
                MOB mob=CMClass.getMOB("StdMOB");
                mob.setName(ck.sender_name+"@"+ck.sender_mud);
                mob.setLocation(CMClass.getLocale("StdRoom"));
                MOB targetMOB=null;
                boolean killtargetmob=false;
				if(ck.type==Packet.CHAN_TARGET)
				{
				    if((ck.target_mud!=null)&&(ck.target_mud.equalsIgnoreCase(getMudName())))
				        targetMOB=CMLib.players().getLoadPlayer(ck.target_name);
				    if((ck.target_visible_name!=null)&&(ck.target_mud!=null)&&(targetMOB==null))
				    {
                        killtargetmob=true;
						targetMOB=CMClass.getMOB("StdMOB");
						targetMOB.setName(ck.target_visible_name+"@"+ck.target_mud);
						targetMOB.setLocation(CMClass.getLocale("StdRoom"));
				    }
					String msgs=socialFixIn(ck.message);
					msgs=CMProps.applyINIFilter(msgs,CMProps.SYSTEM_EMOTEFILTER);
					String targmsgs=socialFixIn(ck.message_target);
					targmsgs=CMProps.applyINIFilter(targmsgs,CMProps.SYSTEM_EMOTEFILTER);
					String str="^Q^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+msgs+"^</CHANNEL^>^N^.";
					String str2="^Q^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+targmsgs+"^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,targetMOB,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),str2,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),str);
				}
				else
				if(ck.type==Packet.CHAN_EMOTE)
				{
					String msgs=socialFixIn(ck.message);
					msgs=CMProps.applyINIFilter(msgs,CMProps.SYSTEM_EMOTEFILTER);
					String str="^Q^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+msgs+"^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),str);
				}
				else
				{
					String str="^Q^<CHANNEL \""+channelName+"\"^>"+mob.name()+" "+channelName+"(S) '"+ck.message+"'^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),str);
				}

				CMLib.channels().channelQueUp(channelInt,msg);
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session ses=CMLib.sessions().elementAt(s);
					if((CMLib.channels().mayReadThisChannel(mob,false,ses,channelInt))
                    &&(ses.mob().location()!=null)
					&&(ses.mob().location().okMessage(ses.mob(),msg)))
						ses.mob().executeMsg(ses.mob(),msg);
				}
                destroymob(mob);
                if((targetMOB!=null)&&(killtargetmob)) destroymob(targetMOB);
			}
			break;
		case Packet.LOCATE_QUERY:
			{
				LocateQueryPacket lk=(LocateQueryPacket)packet;
				String stat="online";
				String name=CMStrings.capitalizeAndLower(lk.user_name);
				MOB smob=findSessMob(lk.user_name);
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
					LocateReplyPacket lpk=new LocateReplyPacket(lk.sender_name,lk.sender_mud,name,0,stat);
					try{
					lpk.send();
					}catch(Exception e){Log.errOut("IMudClient",e);}
				}
			}
			break;
		case Packet.LOCATE_REPLY:
			{
				LocateReplyPacket lk=(LocateReplyPacket)packet;
				MOB smob=findSessMob(lk.target_name);
				if(smob!=null)
					smob.tell(fixColors(lk.located_visible_name)+"@"+fixColors(lk.located_mud_name)+" ("+lk.idle_time+"): "+fixColors(lk.status));
			}
			break;
		case Packet.WHO_REPLY:
			{
				WhoPacket wk=(WhoPacket)packet;
				MOB smob=findSessMob(wk.target_name);
				if(smob!=null)
				{
					StringBuffer buf=new StringBuffer("\n\rwhois@"+fixColors(wk.sender_mud)+":\n\r");
					Vector V=wk.who;
					if(V.size()==0)
						buf.append("Nobody!");
					else
					for(int v=0;v<V.size();v++)
					{
						Vector V2=(Vector)V.elementAt(v);
						String nom = fixColors((String)V2.elementAt(0));
						int idle=0;
						if(V2.elementAt(1) instanceof Integer)
							idle = ((Integer)V2.elementAt(1)).intValue();
						String xtra = fixColors((String)V2.elementAt(2));
						buf.append("["+CMStrings.padRight(nom,20)+"] "+xtra+" ("+idle+")\n\r");
					}
					smob.session().wraplessPrintln(buf.toString());
					break;
				}
			}
			break;
		case Packet.CHAN_WHO_REP:
			{
				ChannelWhoReply wk=(ChannelWhoReply)packet;
				MOB smob=findSessMob(wk.target_name);
				if(smob!=null)
				{
					StringBuffer buf=new StringBuffer("\n\rListening on "+wk.channel+"@"+fixColors(wk.sender_mud)+":\n\r");
					Vector V=wk.who;
					if(V.size()==0)
						buf.append("Nobody!");
					else
					for(int v=0;v<V.size();v++)
					{
						String nom = fixColors((String)V.elementAt(v));
						buf.append("["+CMStrings.padRight(nom,20)+"]\n\r");
					}
					smob.session().wraplessPrintln(buf.toString());
					smob.session().setPromptFlag(true);
					break;
				}
			}
			break;
		case Packet.CHAN_WHO_REQ:
			{
				ChannelWhoRequest wk=(ChannelWhoRequest)packet;
				ChannelWhoReply wkr=new ChannelWhoReply();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				wkr.channel=wk.channel;
				int channelInt=CMLib.channels().getChannelIndex(wk.channel);
				Vector whoV=new Vector();
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session ses=CMLib.sessions().elementAt(s);
					if((CMLib.channels().mayReadThisChannel(ses.mob(),false,ses,channelInt))
                    &&((ses.mob()==null)||(!CMLib.flags().isCloaked(ses.mob()))))
						whoV.addElement(ses.mob().name());
				}
				wkr.who=whoV;
				try{
				wkr.send();
				}catch(Exception e){Log.errOut("IMudClient",e);}
			}
			break;
		case Packet.CHAN_USER_REQ:
			{
				ChannelUserRequest wk=(ChannelUserRequest)packet;
				ChannelUserReply wkr=new ChannelUserReply();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				wkr.userRequested=wk.userToRequest;
				MOB M=CMLib.players().getLoadPlayer(wk.userToRequest);
				if(M!=null) {
					wkr.userVisibleName = M.name();
					wkr.gender=(char)M.charStats().getStat(CharStats.STAT_GENDER);
					try{
						wkr.send();
					}catch(Exception e){Log.errOut("IMudClient",e);}
				}
			}
			break;
		case Packet.WHO_REQUEST:
			{
				WhoPacket wk=(WhoPacket)packet;
				WhoPacket wkr=new WhoPacket();
				wkr.type=Packet.WHO_REPLY;
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				Vector whoV=new Vector();
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session ses=CMLib.sessions().elementAt(s);
					MOB smob=ses.mob();
					if((smob!=null)&&(smob.soulMate()!=null))
						smob=smob.soulMate();
					if((!ses.killFlag())&&(smob!=null)
					&&(!smob.amDead())
					&&(CMLib.flags().isInTheGame(smob,true))
					&&(!CMLib.flags().isCloaked(smob)))
					{
						Vector whoV2=new Vector();
						whoV2.addElement(smob.name());
						whoV2.addElement(Integer.valueOf((int)(ses.getIdleMillis()/1000)));
						whoV2.addElement(smob.charStats().displayClassLevel(smob,true));
						whoV.addElement(whoV2);
					}
				}
				wkr.who=whoV;
				try{
				wkr.send();
				}catch(Exception e){Log.errOut("IMudClient",e);}
			}
			break;
		case Packet.TELL:
			{
				TellPacket tk=(TellPacket)packet;
				MOB mob=CMClass.getMOB("StdMOB");
				mob.setName(tk.sender_name+"@"+tk.sender_mud);
				mob.setLocation(CMClass.getLocale("StdRoom"));
				MOB smob=findSessMob(tk.target_name);
				if(smob!=null)
				{
					tk.message=fixColors(CMProps.applyINIFilter(tk.message,CMProps.SYSTEM_SAYFILTER));
					CMLib.commands().postSay(mob,smob,tk.message,true,true);
				}
                destroymob(mob);
			}
			break;
		default:
			Log.errOut("IMudInterface","Unknown type: "+packet.type);
			break;
		}
	}

    /**
     * @return an enumeration of channels this mud subscribes to
     */
	public java.util.Enumeration getChannels()
	{
		Vector V=new Vector();
		for(int i=0;i<channels.length;i++)
			V.addElement(channels[i][0]);
		return V.elements();
	}

    /**
     * Given a I3 channel name, this method should provide
     * the local name for that channel.
     * Example:
     * <PRE>
     * if( str.equals("imud_code") ) return "intercre";
     * </PRE>
     * @param str the remote name of the desired channel
     * @return the local channel name for a remote channel
     * @see #getRemoteChannel
     */
    public String getLocalChannel(String str){
		for(int i=0;i<channels.length;i++)
			if(channels[i][0].equalsIgnoreCase(str))
				return channels[i][1];
		return "";
	}

    /**
     * Given a I3 channel name, this method should provide
     * the local level for that channel.
     * Example:
     * <PRE>
     * if( str.equals("imud_code") ) return "intercre";
     * </PRE>
     * @param str the remote name of the desired channel
     * @return the local channel name for a remote channel
     * @see #getRemoteChannel
     */
    public String getLocalMask(String str){
		for(int i=0;i<channels.length;i++)
			if(channels[i][1].equalsIgnoreCase(str))
				return channels[i][2];
		return "";
	}

    /**
     * @return the name of this mud
     */
    public String getMudName(){
		return name;
	}

    /**
     * @return the software name and version
     */
    public String getMudVersion()
	{
		return version;
	}

    /**
     * @return the software name and version
     */
    public String getMudState()
	{
		return i3state;
	}

    /**
     * @return the player port for this mud
     */
    public int getMudPort(){
		return port;
	}


    /**
     * Given a local channel name, returns the level
     * required.
     * Example:
     * <PRE>
     * if( str.equals("intercre") ) return "";
     * </PRE>
     * @param str the local name of the desired channel
     * @return the remote name of the specified local channel
     */
	public String getChannelMask(String str){
		for(int i=0;i<channels.length;i++)
			if(channels[i][1].equalsIgnoreCase(str))
				return channels[i][2];
		return "";
	}

    /**
     * Given a local channel name, returns the remote
     * channel name.
     * Example:
     * <PRE>
     * if( str.equals("intercre") ) return "imud_code";
     * </PRE>
     * @param str the local name of the desired channel
     * @return the remote name of the specified local channel
     */
    public String getRemoteChannel(String str){
		for(int i=0;i<channels.length;i++)
			if(channels[i][1].equalsIgnoreCase(str))
				return channels[i][0];
		return "";
	}
}
