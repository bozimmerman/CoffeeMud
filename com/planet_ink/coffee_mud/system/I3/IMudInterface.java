package com.planet_ink.coffee_mud.system.I3;

import java.util.*;
import java.io.Serializable;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.system.I3.packets.ImudServices;
import com.planet_ink.coffee_mud.system.I3.packets.*;

public class IMudInterface implements ImudServices, Serializable
{
	public String version="CoffeeMud 3.0";
	public String name="CoffeeMud";
	public int port=4444;
	public String[][] channels={{"diku_chat","CHAT","0"},
								{"diku_immortals","GOSSIP","32"},
								{"diku_code","GREET","0"}};
	
	private final static int I3MAX_ANSI=49;

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
		{ "^^", "%^RESET%^",     "\033[0m" }, // Reset Text
		{ "^^", "%^RESET%^",     "\033[0m" }, // Reset Text
		{ "^H", "%^BOLD%^",      "\033[1m" }, // Bolden Text(Brightens it)
		{ "^^", "%^EBOLD%^",	 "\033[0m" }, // Assumed to be a reset tag to stop bold
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

														
	
	public IMudInterface (String Name, String Version, int Port, String[][] Channels)
	{
		if(Name!=null) name=Name;
		if(Version!=null) version=Version;
		if(Channels!=null) channels=Channels;
		port=Port;
	}
	
	private MOB findSessMob(String mobName)
	{
		for(int s=0;s<Sessions.size();s++)
		{
			Session ses=(Session)Sessions.elementAt(s);
			if((!ses.killFlag())&&(ses.mob()!=null)
			&&(!ses.mob().amDead())
			&&(ses.mob().name().equalsIgnoreCase(mobName))
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
	
	
	public String replaceAll(String str, String thisStr, String withThisStr)
	{
		for(int i=str.length()-1;i>=0;i--)
		{
			if(str.charAt(i)==thisStr.charAt(0))
				if(str.substring(i).startsWith(thisStr))
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
		}
		return str;
	}
	
	public String socialFix(String str)
	{
		
		str=replaceAll(str,"$N","<S-NAME>");
		str=replaceAll(str,"$n","<S-NAME>");
		str=replaceAll(str,"$T","<T-NAMESELF>");
		str=replaceAll(str,"$t","<T-NAMESELF>");
		str=replaceAll(str,"$m","<S-HIM-HER>");
		str=replaceAll(str,"$M","<T-HIM-HER>");
		str=replaceAll(str,"$s","<S-HIS-HER>");
		str=replaceAll(str,"$S","<T-HIS-HER>");
		str=replaceAll(str,"$e","<S-HE-SHE>");
		str=replaceAll(str,"$E","<T-HE-SHE>");
		str=replaceAll(str,"`","\'");
		if(str.equals("$")) return "";
		return str.trim();
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
				MOB mob=CMClass.getMOB("StdMOB");
				mob.setName(ck.sender_name+"@"+ck.sender_mud);
				mob.setLocation(CMClass.getLocale("StdRoom"));
				String channelName=ck.channel;
				FullMsg msg=null;
				
				if((ck.sender_mud!=null)&&(ck.sender_mud.equalsIgnoreCase(getMudName())))
				   return;
				if((ck.channel==null)||(ck.channel.length()==0))
					return;
				int channelInt=ExternalPlay.channelInt(channelName);
				if(channelInt<0) return;
				int lvl=getLocalLevel(channelName);
				if(ck.type==Packet.CHAN_MESSAGE)
				{
					String str="^Q"+mob.name()+" "+channelName+"(S) '"+fixColors(ck.message)+"'^?^^";
					msg=new FullMsg(mob,null,null,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null,Affect.MASK_CHANNEL|channelInt,str);
				}
				else
				{
					String msgs=socialFix(fixColors(ck.message));
					String str="^Q("+channelName+") "+msgs+"^?";
					msg=new FullMsg(mob,null,null,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null,Affect.MASK_CHANNEL|channelInt,str);
				}
				
				for(int s=0;s<Sessions.size();s++)
				{
					Session ses=(Session)Sessions.elementAt(s);
					if((!ses.killFlag())&&(ses.mob()!=null)
					&&(!ses.mob().amDead())
					&&(ses.mob().location()!=null)
					&&(ses.mob().envStats().level()>=lvl)
					&&(ses.mob().okAffect(msg)))
						ses.mob().affect(msg);
				}
			}
			break;
		case Packet.LOCATE_QUERY:
			{
				LocateQueryPacket lk=(LocateQueryPacket)packet;
				MOB smob=findSessMob(lk.user_name);
				if(smob!=null)
				{
					String stat="online";
					if(Sense.isInvisible(smob)) stat="invisible";
					if(Sense.isHidden(smob)) stat="hidden";
					if(!Sense.isSeen(smob)) stat="wizinv";
					LocateReplyPacket lpk=new LocateReplyPacket(lk.sender_name,lk.sender_mud,smob.name(),0,stat);
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
						int idle = ((Integer)V2.elementAt(1)).intValue();
						String xtra = fixColors((String)V2.elementAt(2));
						buf.append("["+Util.padRight(nom,20)+"] "+xtra+"("+idle+")\n\r");
					}
					smob.session().unfilteredPrintln(buf.toString());
					break;
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
				for(int s=0;s<Sessions.size();s++)
				{
					Session ses=(Session)Sessions.elementAt(s);
					if((!ses.killFlag())&&(ses.mob()!=null)
					&&(!ses.mob().amDead())
					&&(ses.mob().location()!=null)
					&&(Sense.isSeen(ses.mob())))
					{
						Vector whoV2=new Vector();
						whoV2.addElement(ses.mob().name());
						whoV2.addElement(new Integer(0));
						whoV2.addElement(ses.mob().charStats().getMyClass().name()+" "+ses.mob().envStats().level());
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
					ExternalPlay.quickSay(mob,smob,fixColors(tk.message),true,true);
					break;
				}
			}
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
    public int getLocalLevel(String str){
		for(int i=0;i<channels.length;i++)
			if(channels[i][1].equalsIgnoreCase(str))
				return Util.s_int(channels[i][2]);
		return 0;
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
     * @return the player port for this mud
     */
    public int getMudPort(){
		return port;
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
