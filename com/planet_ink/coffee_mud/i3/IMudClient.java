package com.planet_ink.coffee_mud.i3;

import java.util.*;
import java.net.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.i3.imc2.*;
import com.planet_ink.coffee_mud.i3.packets.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class IMudClient implements I3Interface
{
	public IMC2Driver imc2=null;
	public void registerIMC2(Object O)
	{ 
		if(O instanceof IMC2Driver)
			imc2=(IMC2Driver)O;
	}
	
	public void i3who(MOB mob, String mudName)
	{
		if(mob==null) return;
		if((!i3online())&&(!imc2online())) return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell("You must specify a mud name.");
			return;
		}
		if(i3online()&&Intermud.isUp(Intermud.translateName(mudName)))
		{
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
		else
		if(imc2online()&&(imc2.getIMC2Mud(mudName)!=null))
			imc2.imc_send_who(mob.name(),imc2.getIMC2Mud(mudName).name,"who",mob.envStats().level(),0);
		else
		{
			mob.tell("'"+mudName+" is not a mud name.");
			return;
		}
	}

	public boolean i3online()
	{
		return Intermud.isConnected();
	}
	
	public boolean imc2online()
	{
		if(imc2==null) return false;
		return imc2.imc_active==IMC2Driver.IA_UP;
	}
	public void imc2mudInfo(MOB mob, String parms)
	{
		if((mob==null)||(!imc2online())) return;
		if((parms==null)||(parms.length()==0)||(imc2.getIMC2Mud(parms)==null))
		{
			mob.tell("You must specify a mud name.");
			return;
		}
		imc2.imc_send_who(mob.name(),imc2.getIMC2Mud(parms).name,"info",mob.envStats().level(),0);
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
		if(mob==null) return;
		if((!i3online())&&(!imc2online())) return;
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
		if(i3online()&&Intermud.isUp(Intermud.translateName(mudName)))
		{
			mudName=Intermud.translateName(mudName);
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
		else
		if(imc2online()&&(imc2.getIMC2Mud(mudName)!=null))
		{
			tellName=Util.capitalize(tellName)+"@"+imc2.getIMC2Mud(mudName).name;
			mob.tell("^CYou tell "+tellName+" '"+message+"'^?");
			imc2.imc_send_tell(mob.name(),tellName,message,0,Sense.isInvisible(mob)?1:0);
		}
		else
		{
			mob.tell(mudName+" is not available.");
			return;
		}
	}

	public void i3channel(MOB mob, String channelName, String message)
	{
		if(mob==null) return;
		if((!i3online())&&(!imc2online())) return;
		if((channelName==null)||(channelName.length()==0))
		{
			mob.tell("You must specify a channel name.");
			return;
		}
		if((message==null)||(message.length()<1))
		{
			mob.tell("You must enter a message!");
			return;
		}
		if(i3online()&&Intermud.getRemoteChannel(channelName).length()>0)
		{
			ChannelPacket ck=new ChannelPacket();
			ck.channel=channelName; // ck will translate it for us
			ck.sender_name=mob.Name();
			ck.sender_visible_name=mob.Name();
			if((message.startsWith(":")||message.startsWith(","))&&(message.trim().length()>1))
			{
				String msgstr=message.substring(1);
				Vector V=Util.parse(msgstr);
				Social S=Socials.FetchSocial(V,true);
				if(S==null) S=Socials.FetchSocial(V,false);
				CMMsg msg=null;
				if(S!=null)
				{
					msg=S.makeChannelMsg(mob,0,channelName,V,true);
					if((msg.target()!=null)&&(msg.target().name().indexOf("@")>=0))
					{
						int x=msg.target().name().indexOf("@");
						String mudName=msg.target().name().substring(x+1);
						String tellName=msg.target().name().substring(0,x);
						if((mudName==null)||(mudName.length()==0))
						{
							mob.tell("You must specify a mud name.");
							return;
						}
						if((tellName==null)||(tellName.length()<1))
						{
							mob.tell("You must specify someone to emote to.");
							return;
						}
						mudName=Intermud.translateName(mudName);
						if(!Intermud.isUp(mudName))
						{
							mob.tell(mudName+" is not available.");
							return;
						}
						ck.target_mud=mudName;
						ck.target_name=tellName;
					}
					else
					if(msg.target()!=null)
						ck.target_name=msg.target().name();
					if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
						ck.message=socialFixOut(msg.othersMessage());
					else
						ck.message=socialFixOut(msg.sourceMessage());
				}
				else
					ck.message=msgstr;
				ck.type=Packet.CHAN_EMOTE;
			}
			else
				ck.message=message;
			try{
				ck.send();
			}catch(Exception e){Log.errOut("IMudClient",e);}
		}
		else
		if(imc2online()&&(imc2.getAnIMC2Channel(channelName)!=null))
		{
			int emote=0;
			if((message.startsWith(":")||message.startsWith(","))&&(message.trim().length()>1))
			{
				message=message.substring(1);
				MOB mob2=CMClass.getMOB("StdMOB");
				mob2.setName(mob.Name()+"@"+imc2.imc_name);
				mob2.setLocation(CMClass.getLocale("StdRoom"));
				Vector V=Util.parse(message);
				Social S=Socials.FetchSocial(V,true);
				if(S==null) S=Socials.FetchSocial(V,false);
				CMMsg msg=null;
				if(S!=null)
				{
					msg=S.makeChannelMsg(mob,0,channelName,V,true);
					if((msg.target()!=null)&&(msg.target().name().indexOf("@")>=0))
					{
						int x=msg.target().name().indexOf("@");
						String mudName=msg.target().name().substring(x+1);
						String tellName=msg.target().name().substring(0,x);
						if((mudName==null)||(mudName.length()==0))
						{
							mob.tell("You must specify a mud name.");
							return;
						}
						if((tellName==null)||(tellName.length()<1))
						{
							mob.tell("You must specify someone to emote to.");
							return;
						}
						if(imc2.getIMC2Mud(mudName)==null)
						{
							mob.tell(mudName+" is not available.");
							return;
						}
					}
					
					if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
						message=CoffeeFilter.fullOutFilter(null,CMClass.sampleMOB(),mob2,msg.target(),null,msg.othersMessage(),false);
					else
						message=CoffeeFilter.fullOutFilter(null,CMClass.sampleMOB(),mob2,msg.target(),null,msg.sourceMessage(),false);
					if(message.toUpperCase().startsWith((mob.Name()+"@"+imc2.imc_name).toUpperCase()))
						message=message.substring((mob.Name()+"@"+imc2.imc_name).length()).trim();
					emote=2;
				}
				emote=1;
			}
			IMC_CHANNEL c=imc2.getAnIMC2Channel(channelName);
			imc2.imc_send_chat(mob.name(),c.name,message,c.level,emote);
		}
		else
		{
			mob.tell("You must specify a channel name.");
			return;
		}
	}

	public void i3locate(MOB mob, String mobName)
	{
		if(mob==null) return;
		if((!i3online())&&(!imc2online())) return;
		
		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell("You must specify a name.");
			return;
		}

		if(i3online())
		{
			LocateQueryPacket ck=new LocateQueryPacket();
			ck.sender_name=mob.Name();
			ck.user_name=mobName;
			try{
			ck.send();
			}catch(Exception e){Log.errOut("IMudClient",e);}
		}
		if(imc2online())
			imc2.imc_send_whois(mob.Name(),mobName,mob.envStats().level());
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
				if((m.state<0)&&(EnglishParser.containsString(m.mud_name,parms)))
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
	
	public void giveIMC2MudList(MOB mob)
	{
		if((mob==null)||(!imc2online())) return;
		if(mob.isMonster()) return;
		Hashtable l=imc2.query_muds();
		Vector V=new Vector();
		for(Enumeration e=l.elements();e.hasMoreElements();)
		{
			REMOTEINFO m=(REMOTEINFO)e.nextElement();
			boolean done=false;
			for(int v=0;v<V.size();v++)
			{
				REMOTEINFO m2=(REMOTEINFO)V.elementAt(v);
				if(m2.name.toUpperCase().compareTo(m.name.toUpperCase())>0)
				{
					V.insertElementAt(m,v);
					done=true;
					break;
				}
			}
			if(!done) V.addElement(m);
		}
		StringBuffer buf=new StringBuffer("\n\rIMC2 Mud List:\n\r");
		for(int v=0;v<V.size();v++)
		{
			REMOTEINFO m=(REMOTEINFO)V.elementAt(v);
			buf.append("["+Util.padRight(m.name,20)+"]["+Util.padRight(m.version,35)+"] "+m.network+" ("+m.hub+")\n\r");
		}
		mob.session().unfilteredPrintln(buf.toString());
	}
	public void giveI3MudList(MOB mob)
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

	public void giveI3ChannelsList(MOB mob)
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

	public void giveIMC2ChannelsList(MOB mob)
	{
		if((mob==null)||(!imc2online())) return;
		if(mob.isMonster()) return;
		StringBuffer buf=new StringBuffer("\n\rIMC2 Channels List:\n\r");
        Hashtable channels=imc2.query_channels();
        buf.append(Util.padRight("Name", 22)+Util.padRight("Policy",25)+Util.padRight("Owner",20)+"\n\r");
        Enumeration e = channels.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            IMC_CHANNEL r = (IMC_CHANNEL) channels.get(key);
            if (r != null) {
                String policy = "final public";
                if (r.policy == IMC2Driver.CHAN_PRIVATE)
                    policy = "(private)";
                else
                if (r.policy == IMC2Driver.CHAN_COPEN)
                    policy = "open";
                else
                if (r.policy == IMC2Driver.CHAN_CPRIVATE)
                    policy = "(cprivate)";

                buf.append(Util.padRight(key, 22)+
						   Util.padRight(policy+"("+r.level+")",25)+
						   r.owner+"\n\r");
            }
        }
		mob.session().unfilteredPrintln(buf.toString());
	}

	public boolean isIMC2channel(String channelName)
	{
		if(!imc2online()) return false;
		Object remote=imc2.getAnIMC2Channel(channelName);
		if(remote==null)
			return false;
		return true;
	}

	public boolean isI3channel(String channelName)
	{
		if(!i3online()) return false;
		String remote=Intermud.getRemoteChannel(channelName);
		if(remote.length()==0) return false;
		return true;
	}

	public String socialFixOut(String str)
	{
		str=Util.replaceAll(str,"<S-NAME>","$N");
		str=Util.replaceAll(str,"<S-NAME>","$n");
		str=Util.replaceAll(str,"<T-NAMESELF>","$T");
		str=Util.replaceAll(str,"<T-NAMESELF>","$t");
		str=Util.replaceAll(str,"<S-HIM-HER>","$m");
		str=Util.replaceAll(str,"<T-HIM-HER>","$M");
		str=Util.replaceAll(str,"<S-HIS-HER>","$s");
		str=Util.replaceAll(str,"<T-HIS-HER>","$S");
		str=Util.replaceAll(str,"<S-HE-SHE>","$e");
		str=Util.replaceAll(str,"<T-HE-SHE>","$E");
		str=Util.replaceAll(str,"\'","`");
		if(str.equals("")) return "$";
		return str.trim();
	}

}
