package com.planet_ink.coffee_mud.i3;

import java.util.*;
import java.net.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.i3.packets.*;

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
		mudName=Intermud.translateName(mudName);
		if(!Intermud.isUp(mudName))
		{
			mob.tell(mudName+" is not available.");
			return;
		}
		WhoPacket wk=new WhoPacket();
		wk.type=Packet.WHO_REQUEST;
		wk.sender_name=mob.Name();
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

	public void i3chanwho(MOB mob, String channel, String mudName)
	{
		if((mob==null)||(!i3online())) return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell("You must specify a mud name.");
			return;
		}
		if((channel==null)||(channel.length()==0)||(Intermud.getRemoteChannel(channel).length()==0))
		{
			mob.tell("You must specify an InterMud 3 channel name.");
			return;
		}
		mudName=Intermud.translateName(mudName);
		if(!Intermud.isUp(mudName))
		{
			mob.tell(mudName+" is not available.");
			return;
		}
		ChannelWhoRequest ck=new ChannelWhoRequest();
		ck.sender_name=mob.Name();
		ck.target_mud=mudName;
		ck.channel=channel;
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}

	public void i3channelAdd(MOB mob, String channel)
	{
		if((mob==null)||(!i3online())) return;
		if((channel==null)
		   ||(channel.length()==0)
		   ||(Intermud.getLocalChannel(channel).length()==0))
		{
			mob.tell("You must specify a channel name listed in your INI file.");
			return;
		}
		ChannelAdd ck=new ChannelAdd();
		ck.sender_name=mob.Name();
		ck.channel=channel;
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}

	public void i3channelListen(MOB mob, String channel)
	{
		if((mob==null)||(!i3online())) return;
		if((channel==null)
		   ||(channel.length()==0)
		   ||(Intermud.getLocalChannel(channel).length()==0))
		{
			mob.tell("You must specify a channel name listed in your INI file.");
			return;
		}
		ChannelListen ck=new ChannelListen();
		ck.sender_name=mob.Name();
		ck.channel=channel;
		ck.onoff="1";
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}

	public void i3channelSilence(MOB mob, String channel)
	{
		if((mob==null)||(!i3online())) return;
		if((channel==null)
		   ||(channel.length()==0)
		   ||(Intermud.getLocalChannel(channel).length()==0))
		{
			mob.tell("You must specify a channel name listed in your INI file.");
			return;
		}
		ChannelListen ck=new ChannelListen();
		ck.sender_name=mob.Name();
		ck.channel=channel;
		ck.onoff="0";
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}

	public void i3channelRemove(MOB mob, String channel)
	{
		if((mob==null)||(!i3online())) return;
		if((channel==null)||(channel.length()==0)||(Intermud.getRemoteChannel(channel).length()==0))
		{
			mob.tell("You must specify a valid InterMud 3 channel name.");
			return;
		}
		ChannelDelete ck=new ChannelDelete();
		ck.sender_name=mob.Name();
		ck.channel=channel;
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
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
		mudName=Intermud.translateName(mudName);
		if(!Intermud.isUp(mudName))
		{
			mob.tell(mudName+" is not available.");
			return;
		}
		mob.tell("You tell "+tellName+" '"+message+"'");
		TellPacket tk=new TellPacket();
		tk.sender_name=mob.Name();
		tk.sender_visible_name=mob.Name();
		tk.target_mud=mudName;
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
		ck.sender_name=mob.Name();
		ck.sender_visible_name=mob.Name();
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
		ck.sender_name=mob.Name();
		ck.user_name=mobName;
		try{
		ck.send();
		}catch(Exception e){Log.errOut("IMudClient",e);}
	}

	public void i3mudInfo(MOB mob, String parms)
	{
		if((mob==null)||(!i3online())) return;
		if(mob.isMonster()) return;
		MudList list=Intermud.getAllMudsList();
		StringBuffer buf=new StringBuffer("\n\r");
		if(list!=null)
		{
			Hashtable l=list.getMuds();
			for(Enumeration e=l.elements();e.hasMoreElements();)
			{
				Mud m=(Mud)e.nextElement();
				if((m.state<0)&&(CoffeeUtensils.containsString(m.mud_name,parms)))
				{
					buf.append(Util.padRight("Name",10)+": "+m.mud_name+"\n\r");
					buf.append(Util.padRight("Address",10)+": "+m.address+"\n\r");
					buf.append(Util.padRight("Port",10)+": "+m.player_port+"\n\r");
					buf.append(Util.padRight("Admin@",10)+": "+m.admin_email+"\n\r");
					buf.append(Util.padRight("Base",10)+": "+m.base_mudlib+"\n\r");
					buf.append(Util.padRight("MudLib",10)+": "+m.mudlib+"\n\r");
					buf.append(Util.padRight("Type",10)+": "+m.mud_type+"\n\r");
					buf.append(Util.padRight("Driver",10)+": "+m.driver+"\n\r");
					buf.append(Util.padRight("Status",10)+": "+m.status+"\n\r");
					break;
				}
			}
		}
		if(buf.length()<10) buf.append("Not found!");
		mob.session().unfilteredPrintln(buf.toString());
	}
	public void giveMudList(MOB mob)
	{
		if((mob==null)||(!i3online())) return;
		if(mob.isMonster()) return;
		StringBuffer buf=new StringBuffer("\n\rI3 Mud List:\n\r");
		MudList list=Intermud.getAllMudsList();
		Vector V=new Vector();
		if(list!=null)
		{
			Hashtable l=list.getMuds();
			for(Enumeration e=l.elements();e.hasMoreElements();)
			{
				Mud m=(Mud)e.nextElement();
				if(m.state<0)
				{
					boolean done=false;
					for(int v=0;v<V.size();v++)
					{
						Mud m2=(Mud)V.elementAt(v);
						if(m2.mud_name.toUpperCase().compareTo(m.mud_name.toUpperCase())>0)
						{
							V.insertElementAt(m,v);
							done=true;
							break;
						}
					}
					if(!done) V.addElement(m);
				}
			}
			for(int v=0;v<V.size();v++)
			{
				Mud m=(Mud)V.elementAt(v);
				buf.append("["+Util.padRight(m.mud_name,20)+"]["+Util.padRight(m.base_mudlib,20)+"] "+m.address+" ("+m.player_port+")\n\r");
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
