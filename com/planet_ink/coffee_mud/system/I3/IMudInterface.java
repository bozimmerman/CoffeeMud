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
														
	
	public IMudInterface (String Name, String Version, int Port, String[][] Channels)
	{
		if(Name!=null) name=Name;
		if(Version!=null) version=Version;
		if(Channels!=null) channels=Channels;
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
					String str=mob.name()+" "+channelName+"(S) '"+ck.message+"'";
					msg=new FullMsg(mob,null,null,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null,Affect.MASK_CHANNEL|channelInt,str);
				}
				else
				{
					String msgs=socialFix(ck.message);
					String str="("+channelName+") "+msgs+"";
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
					smob.tell(lk.located_visible_name+"@"+lk.located_mud_name+" ("+lk.idle_time+"): "+lk.status);
			}
			break;
		case Packet.WHO_REPLY:
			{
				WhoPacket wk=(WhoPacket)packet;
				MOB smob=findSessMob(wk.target_name);
				if(smob!=null)
				{
					StringBuffer buf=new StringBuffer("\n\rwhois@"+wk.sender_mud+":\n\r");
					Vector V=wk.who;
					if(V.size()==0)
						buf.append("Nobody!");
					else
					for(int v=0;v<V.size();v++)
					{
						Vector V2=(Vector)V.elementAt(v);
						String nom = (String)V2.elementAt(0);
						int idle = ((Integer)V2.elementAt(1)).intValue();
						String xtra = (String)V2.elementAt(2);
						buf.append("["+Util.padRight(nom,20)+"] ("+Util.padRightPreserve(""+idle,3)+"): "+xtra+"\n\r");
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
				MOB smob=findSessMob(tk.target_name);
				if(smob!=null)
				{
					ExternalPlay.quickSay(mob,smob,tk.message,true,true);
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
