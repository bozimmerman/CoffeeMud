package com.planet_ink.coffee_mud.system.I3;

import java.util.*;
import java.net.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.system.I3.packets.*;

public class IMudClient implements I3Interface
{
	public void i3who(MOB mob, String mudName)
	{
		if((mob==null)||(!i3online())) return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell("You must specify a mud name.");
			return;
		}
		if(!Intermud.isUp(mudName))
		{
			mob.tell(mudName+" is not available.");
			return;
		}
		WhoPacket wk=new WhoPacket();
		wk.type=Packet.WHO_REQUEST;
		wk.sender_name=mob.name();
		wk.target_mud=mudName;
		wk.who=new Vector();
		try{
		wk.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}
	
	
	public boolean i3online()
	{
		return Intermud.isConnected();
	}
	
	public void i3tell(MOB mob, String tellName, String mudName, String message)
	{
		if((mob==null)||(!i3online())) return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell("You must specify a mud name.");
			return;
		}
		if((tellName==null)||(tellName.length()<1))
		{
			mob.tell("You must specify someone to talk to.");
			return;
		}
		if((message==null)||(message.length()<1))
		{
			mob.tell("You must enter a message!");
			return;
		}
		if(!Intermud.isUp(Intermud.translateName(mudName)))
		{
			mob.tell(mudName+" is not available.");
			return;
		}
		mob.tell("You tell "+tellName+" '"+message+"'");
		TellPacket tk=new TellPacket();
		tk.sender_name=mob.name();
		tk.sender_visible_name=mob.name();
		tk.target_mud=Intermud.translateName(mudName);
		tk.target_name=tellName;
		tk.message=message;
		try{
		tk.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}
	
	public void i3channel(MOB mob, String channelName, String message)
	{
		if((mob==null)||(!i3online())) return;
		if((channelName==null)||(channelName.length()==0)||(Intermud.getRemoteChannel(channelName).length()==0))
		{
			mob.tell("You must specify a channel name.");
			return;
		}
		if((message==null)||(message.length()<1))
		{
			mob.tell("You must enter a message!");
			return;
		}

		ChannelPacket ck=new ChannelPacket();
		ck.channel=channelName; // ck will translate it for us
		ck.sender_name=mob.name();
		ck.sender_visible_name=mob.name();
		ck.message=message;
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}
	
	public void i3locate(MOB mob, String mobName)
	{
		if((mob==null)||(!i3online())) return;
		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell("You must specify a name.");
			return;
		}

		LocateQueryPacket ck=new LocateQueryPacket();
		ck.sender_name=mob.name();
		ck.user_name=mobName;
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}
	
	public void giveMudList(MOB mob)
	{
		if((mob==null)||(!i3online())) return;
		if(mob.isMonster()) return;
		StringBuffer buf=new StringBuffer("\n\rI3 Mud List:\n\r");
		MudList list=Intermud.getAllMudsList();
		if(list!=null)
		{
			Hashtable l=list.getMuds();
			for(Enumeration e=l.elements();e.hasMoreElements();)
			{
				Mud m=(Mud)e.nextElement();
				if(m.state<0)
					buf.append("["+Util.padRight(m.mud_name,20)+"] "+m.address+" ("+m.player_port+")\n\r");
			}
		}
		mob.session().unfilteredPrintln(buf.toString());
	}
	
	public void giveChannelsList(MOB mob)
	{
		if((mob==null)||(!i3online())) return;
		if(mob.isMonster()) return;
		StringBuffer buf=new StringBuffer("\n\rI3 Channels List:\n\r");
		ChannelList list=Intermud.getAllChannelList();
		if(list!=null)
		{
			Hashtable l=list.getChannels();
			for(Enumeration e=l.elements();e.hasMoreElements();)
			{
				Channel c=(Channel)e.nextElement();
				if(c.type==0)
					buf.append("["+Util.padRight(c.channel,20)+"] "+c.owner+"\n\r");
			}
		}
		mob.session().unfilteredPrintln(buf.toString());
	}
	
	public boolean isI3channel(String channelName)
	{
		if(!i3online()) return false;
		String remote=Intermud.getRemoteChannel(channelName);
		if(remote.length()==0) return false;
		return true;
	}
}
