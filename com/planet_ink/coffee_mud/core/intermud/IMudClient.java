package com.planet_ink.coffee_mud.core.intermud;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Client;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3Mud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.net.*;

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
public class IMudClient implements I3Interface
{
	@Override
	public String ID()
	{
		return "IMudClient";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
		}
		catch(final Exception e)
		{
			return new IMudClient();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject)this.clone();
		}
		catch(final Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public boolean activate()
	{
		return true;
	}

	@Override
	public boolean shutdown()
	{
		return true;
	}

	@Override
	public void propertiesLoaded()
	{
	}

	@Override
	public TickClient getServiceClient()
	{
		return null;
	}

	public IMC2Driver imc2=null;

	@Override
	public void registerIMC2(final Object O)
	{
		if(O instanceof IMC2Driver)
			imc2=(IMC2Driver)O;
	}

	@Override
	public void i3who(final MOB mob, String mudName)
	{
		if(mob==null)
			return;
		if((!i3online())&&(!imc2online()))
			return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell(L("You must specify a mud name."));
			return;
		}
		if(i3online()&&I3Client.isAPossibleMUDName(mudName))
		{
			mudName=I3Client.translateName(mudName);
			if(!I3Client.isUp(mudName))
			{
				mob.tell(L("@x1 is not available.",mudName));
				return;
			}
			final WhoReqPacket wk=new WhoReqPacket();
			wk.type=Packet.PacketType.WHO_REQ;
			wk.sender_name=mob.Name();
			wk.target_mud=mudName;
			wk.who=new Vector<String>();
			try
			{
				wk.send();
			}
			catch (final Exception e)
			{
				Log.errOut("IMudClient", e);
			}
		}
		else
		if(imc2online()&&(imc2.getIMC2Mud(mudName)!=null))
			imc2.imc_send_who(mob.name(),imc2.getIMC2Mud(mudName).name,"who",mob.phyStats().level(),0);
		else
		{
			mob.tell(L("'@x1' is not a mud name.",mudName));
			return;
		}
	}

	@Override
	public boolean i3online()
	{
		return I3Client.isConnected() && (!CMSecurity.isDisabled(DisFlag.I3));
	}

	@Override
	public boolean imc2online()
	{
		if((imc2==null)||(CMSecurity.isDisabled(DisFlag.IMC2)))
			return false;
		return imc2.imc_active==IMC2Driver.IA_UP;
	}

	@Override
	public void imc2mudInfo(final MOB mob, final String parms)
	{
		if((mob==null)||(!imc2online()))
			return;
		if((parms==null)||(parms.length()==0)||(imc2.getIMC2Mud(parms)==null))
		{
			mob.tell(L("You must specify a mud name."));
			return;
		}
		imc2.imc_send_who(mob.name(),imc2.getIMC2Mud(parms).name,"info",mob.phyStats().level(),0);
	}

	@Override
	public void i3chanwho(final MOB mob, final String channel, String mudName)
	{
		if((mob==null)||(!i3online()))
			return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell(L("You must specify a mud name."));
			return;
		}
		if((channel==null)||(channel.length()==0)||(I3Client.getRemoteChannel(channel).length()==0))
		{
			mob.tell(L("You must specify an InterMud 3 channel name."));
			return;
		}
		if(!I3Client.isAPossibleMUDName(mudName))
		{
			mob.tell(L("'@x1' is an unknown mud.",mudName));
			return;
		}
		mudName=I3Client.translateName(mudName);
		if(!I3Client.isUp(mudName))
		{
			mob.tell(L("@x1 is not available.",mudName));
			return;
		}
		final ChannelWhoRequest ck=new ChannelWhoRequest();
		ck.sender_name=mob.Name();
		ck.target_mud=mudName;
		ck.channel= I3Client.getRemoteChannel(channel);
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IMudClient", e);
		}
	}

	@Override
	public void i3channelAdd(final MOB mob, final String channel)
	{
		if((mob==null)||(!i3online()))
			return;
		if((channel==null)||(channel.length()==0)||(I3Client.getLocalChannel(channel).length()==0))
		{
			mob.tell(L("You must specify an existing channel to add it to the i3 network."));
			return;
		}

		final ChannelAdd ck=new ChannelAdd();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel=channel;
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IMudClient", e);
		}
	}

	@Override
	public void i3channelListen(final MOB mob, final String channel)
	{
		if((mob==null)||(!i3online()))
			return;
		if((channel==null)||(channel.length()==0))
		{
			mob.tell(L("You must specify a channel name listed in your INI file."));
			return;
		}
		if(I3Client.getLocalChannel(channel).length()==0)
		{
			if(I3Client.registerFakeChannel(channel).length()>0)
				mob.tell(L("Channel was not officially registered."));
			else
				mob.tell(L("Channel listen failed."));
		}
		final ChannelListen ck=new ChannelListen();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel= I3Client.getRemoteChannel(channel);
		ck.onoff=1;
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IMudClient", e);
		}
	}

	@Override
	public void i3channelSilence(final MOB mob, final String channel)
	{
		if((mob==null)||(!i3online()))
			return;
		if((channel==null)
		   ||(channel.length()==0)
		   ||(I3Client.getLocalChannel(channel).length()==0))
		{
			mob.tell(L("You must specify an actual channel name."));
			return;
		}
		if(I3Client.removeFakeChannel(channel).length()>0)
			mob.tell(L("Unofficial channel closed."));

		final ChannelListen ck=new ChannelListen();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel=channel;
		ck.onoff=0;
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IMudClient", e);
		}
	}

	@Override
	public void i3channelRemove(final MOB mob, final String channel)
	{
		if((mob==null)||(!i3online()))
			return;
		if((channel==null)||(channel.length()==0)||(I3Client.getRemoteChannel(channel).length()==0))
		{
			mob.tell(L("You must specify a valid InterMud 3 channel name."));
			return;
		}
		final ChannelDelete ck=new ChannelDelete();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel=channel;
		ck.channel = I3Client.getRemoteChannel(ck.channel);
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IMudClient", e);
		}
	}

	@Override
	public void i3tell(final MOB mob, String tellName, String mudName, final String message)
	{
		if(mob==null)
			return;
		if((!i3online())&&(!imc2online()))
			return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell(L("You must specify a mud name."));
			return;
		}
		if((tellName==null)||(tellName.length()<1))
		{
			mob.tell(L("You must specify someone to talk to."));
			return;
		}
		if((message==null)||(message.length()<1))
		{
			mob.tell(L("You must enter a message!"));
			return;
		}
		if(i3online()&&I3Client.isAPossibleMUDName(mudName))
		{
			mudName=I3Client.translateName(mudName);
			if(!I3Client.isUp(mudName))
			{
				mob.tell(L("@x1 is not available.",mudName));
				return;
			}
			mob.tell(L("You tell @x1 '@x2'",tellName,message));
			final TellPacket tk=new TellPacket();
			tk.sender_name=mob.Name();
			tk.sender_visible_name=mob.Name();
			tk.target_mud=mudName;
			tk.target_name=tellName;
			tk.message=message;
			if(mob.playerStats()!=null)
				mob.playerStats().addTellStack(mob.name(), tellName+"@"+mudName, "You tell "+tellName+" '"+message+"'");
			try
			{
				tk.send();
			}
			catch (final Exception e)
			{
				Log.errOut("IMudClient", e);
			}
		}
		else
		if(imc2online()&&(imc2.getIMC2Mud(mudName)!=null))
		{
			tellName=CMStrings.capitalizeAndLower(tellName)+"@"+imc2.getIMC2Mud(mudName).name;
			mob.tell(L("^CYou tell @x1 '@x2'^?",tellName,message));
			if(mob.playerStats()!=null)
				mob.playerStats().addTellStack(mob.Name(), tellName, "You tell "+tellName+" '"+message+"'");
			imc2.imc_send_tell(mob.name(),tellName,message,0,CMLib.flags().isInvisible(mob)?1:0);
		}
		else
		{
			mob.tell(L("@x1 is an unknown mud.",mudName));
			return;
		}
	}

	public void destroymob(final MOB mob)
	{
		if(mob==null)
			return;
		final Room R=mob.location();
		mob.destroy();
		if(R!=null)
			R.destroy();
	}

	public String getI3ChannelName(final String localChannelName)
	{
		final String fixedChannel = I3Client.getRemoteChannel(localChannelName);
		if(((fixedChannel != null)&&(fixedChannel.length()>0)))
			return fixedChannel;
		else
			return localChannelName;
	}

	@Override
	public void i3channel(final MOB mob, final String channelName, String message)
	{
		if(mob==null)
			return;
		if((!i3online())&&(!imc2online()))
			return;
		if((channelName==null)||(channelName.length()==0))
		{
			mob.tell(L("You must specify a channel name."));
			return;
		}
		if((message==null)||(message.length()<1))
		{
			mob.tell(L("You must enter a message!"));
			return;
		}
		if(i3online()
		&&I3Client.getRemoteChannel(channelName).length()>0)
		{
			final ChannelPacket ck;
			if((message.startsWith(":")||message.startsWith(","))
			&&(message.trim().length()>1))
			{
				String msgstr=message.substring(1);
				final Vector<String> V=CMParms.parse(msgstr);
				Social socialS=CMLib.socials().fetchSocial(V,true,false);
				if(socialS==null)
					socialS=CMLib.socials().fetchSocial(V,false,false);
				CMMsg msg=null;
				if((socialS!=null)
				&&(socialS.meetsCriteriaToUse(mob)))
				{
					msg=socialS.makeChannelMsg(mob,0,channelName,V,true);
					final int atDex = (msg.target()!=null) ? msg.target().name().indexOf('@') : -1;
					if(atDex>=0)
					{
						final int x=atDex;
						String mudName=msg.target().name().substring(x+1);
						final String tellName=msg.target().name().substring(0,x);
						if((mudName==null)||(mudName.length()==0))
						{
							mob.tell(L("You must specify a mud name."));
							return;
						}
						if((tellName==null)||(tellName.length()<1))
						{
							mob.tell(L("You must specify someone to emote to."));
							return;
						}
						if(!I3Client.isAPossibleMUDName(mudName))
						{
							mob.tell(L("'@x1' is an unknown mud.",mudName));
							return;
						}
						mudName=I3Client.translateName(mudName);
						if(!I3Client.isUp(mudName))
						{
							mob.tell(L("@x1 is not available.",mudName));
							return;
						}
						ck = new ChannelTargetEmote();
						ck.channel = this.getI3ChannelName(channelName);
						ck.sender_name=mob.Name();
						ck.sender_visible_name=mob.Name();
						ck.target_mud=mudName;
						ck.target_name=tellName;
						((ChannelTargetEmote)ck).target_visible_name=tellName;
						if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0))
							((ChannelTargetEmote)ck).message_target=socialFixOut(CMStrings.removeColors(msg.targetMessage()));
						if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
							((ChannelTargetEmote)ck).message=socialFixOut(CMStrings.removeColors(msg.othersMessage()));
					}
					else
					if(msg.target()!=null)
					{
						ck = new ChannelTargetEmote();
						ck.target_name=msg.target().name();
						ck.channel = this.getI3ChannelName(channelName);
						ck.sender_name=mob.Name();
						ck.sender_visible_name=mob.Name();
						((ChannelTargetEmote)ck).target_visible_name=msg.target().name();
						if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0))
							((ChannelTargetEmote)ck).message_target=socialFixOut(CMStrings.removeColors(msg.targetMessage()));
					}
					else
					{
						ck = new ChannelEmote();
						ck.channel = this.getI3ChannelName(channelName);
						ck.sender_name=mob.Name();
						ck.sender_visible_name=mob.Name();
					}
					if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
						ck.message=socialFixOut(CMStrings.removeColors(msg.othersMessage()));
					else
						ck.message=socialFixOut(CMStrings.removeColors(msg.sourceMessage()));
					ck.message = ck.convertString(ck.message);
				}
				else
				{
					ck = new ChannelEmote();
					ck.channel = this.getI3ChannelName(channelName);
					ck.sender_name=mob.Name();
					ck.sender_visible_name=mob.Name();
					if(msgstr.trim().startsWith("'")||msgstr.trim().startsWith("`"))
						msgstr=msgstr.trim();
					else
						msgstr=" "+msgstr.trim();
					ck.message=socialFixOut("<S-NAME>"+msgstr);
				}
			}
			else
			{
				ck = new ChannelMessage();
				ck.channel = this.getI3ChannelName(channelName);
				ck.sender_name=mob.Name();
				ck.sender_visible_name=mob.Name();
				ck.message=message;
			}
			try
			{
				ck.send();
			}
			catch (final Exception e)
			{
				Log.errOut("IMudClient", e);
			}
		}
		else
		if(imc2online()&&(imc2.getAnIMC2Channel(channelName)!=null))
		{
			int emote=0;
			if((message.startsWith(":")||message.startsWith(","))&&(message.trim().length()>1))
			{
				message=message.substring(1);
				final MOB mob2=CMClass.getFactoryMOB();
				mob2.setName(mob.Name()+"@"+imc2.imc_name);
				mob2.setLocation(CMClass.getLocale("StdRoom"));
				final Vector<String> V=CMParms.parse(message);
				Social S=CMLib.socials().fetchSocial(V,true,false);
				if(S==null)
					S=CMLib.socials().fetchSocial(V,false,false);
				CMMsg msg=null;
				if(S!=null)
				{
					msg=S.makeChannelMsg(mob,0,channelName,V,true);
					if((msg.target()!=null)&&(msg.target().name().indexOf('@')>=0))
					{
						final int x=msg.target().name().indexOf('@');
						final String mudName=msg.target().name().substring(x+1);
						final String tellName=msg.target().name().substring(0,x);
						if((mudName==null)||(mudName.length()==0))
						{
							mob.tell(L("You must specify a mud name."));
							destroymob(mob2);
							return;
						}
						if((tellName==null)||(tellName.length()<1))
						{
							mob.tell(L("You must specify someone to emote to."));
							destroymob(mob2);
							return;
						}
						if(imc2.getIMC2Mud(mudName)==null)
						{
							mob.tell(L("@x1 is not available.",mudName));
							destroymob(mob2);
							return;
						}
					}

					if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
						message=CMLib.coffeeFilter().fullOutFilter(null,CMClass.sampleMOB(),mob2,msg.target(),null,CMStrings.removeColors(msg.othersMessage()),false);
					else
						message=CMLib.coffeeFilter().fullOutFilter(null,CMClass.sampleMOB(),mob2,msg.target(),null,CMStrings.removeColors(msg.sourceMessage()),false);
					if(message.toUpperCase().startsWith((mob.Name()+"@"+imc2.imc_name).toUpperCase()))
						message=message.substring((mob.Name()+"@"+imc2.imc_name).length()).trim();
					emote=2;
				}
				emote=1;
				destroymob(mob2);
			}
			final IMC_CHANNEL c=imc2.getAnIMC2Channel(channelName);
			imc2.imc_send_chat(mob.name(),c.name,message,c.level,emote);
		}
		else
		{
			mob.tell(L("You must specify a channel name."));
			return;
		}
	}

	@Override
	public void i3locate(final MOB mob, final String mobName)
	{
		if(mob==null)
			return;
		if((!i3online())&&(!imc2online()))
			return;

		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell(L("You must specify a name."));
			return;
		}

		if(i3online())
		{
			final LocateQueryPacket ck=new LocateQueryPacket();
			ck.sender_name=mob.Name();
			ck.target_mud=I3Client.getNameServer().name;
			ck.user_name=mobName;
			try
			{
				ck.send();
			}
			catch (final Exception e)
			{
				Log.errOut("IMudClient", e);
			}
		}
		if(imc2online())
			imc2.imc_send_whois(mob.Name(),mobName,mob.phyStats().level());
	}

	@Override
	public void i3pingRouter(final MOB mob)
	{
		if(mob==null)
			return;
		if((!i3online())&&(!imc2online()))
			return;
		if(i3online())
		{
			final PingPacket ck=new PingPacket(I3Server.getMudName());
			try
			{
				ck.send();
			}
			catch (final Exception e)
			{
				Log.errOut("IMudClient", e);
			}
		}
	}

	@Override
	public void i3finger(final MOB mob, final String mobName, final String mudName)
	{
		if(mob==null)
			return;
		if((!i3online())&&(!imc2online()))
			return;

		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell(L("You must specify a name."));
			return;
		}

		if(i3online())
		{
			final FingerRequest ck=new FingerRequest();
			ck.sender_name=mob.Name();
			ck.target_name=mobName;
			ck.target_mud=mudName;
			try
			{
				ck.send();
			}
			catch (final Exception e)
			{
				Log.errOut("IMudClient", e);
			}
		}
		if(imc2online())
			imc2.imc_send_whois(mob.Name(),mobName,mob.phyStats().level());
	}

	public String getMudInfo(final I3Mud mudToShow)
	{
		final StringBuilder buf=new StringBuilder("");
		buf.append(CMStrings.padRight(L("Name"),10)+": "+mudToShow.mud_name+"\n\r");
		buf.append(CMStrings.padRight(L("Address"),10)+": "+mudToShow.address+"\n\r");
		buf.append(CMStrings.padRight(L("Port"),10)+": "+mudToShow.player_port+"\n\r");
		buf.append(CMStrings.padRight(L("Admin@"),10)+": "+mudToShow.admin_email+"\n\r");
		buf.append(CMStrings.padRight(L("Base"),10)+": "+mudToShow.base_mudlib+"\n\r");
		buf.append(CMStrings.padRight(L("MudLib"),10)+": "+mudToShow.mudlib+"\n\r");
		buf.append(CMStrings.padRight(L("Type"),10)+": "+mudToShow.mud_type+"\n\r");
		buf.append(CMStrings.padRight(L("Driver"),10)+": "+mudToShow.driver+"\n\r");
		buf.append(CMStrings.padRight(L("Status"),10)+": "+mudToShow.status+"\n\r");
		return buf.toString();
	}

	public List<I3Mud> mudFinder(final String parms)
	{
		final MudList list=I3Client.getAllMudsList();
		if(list==null)
			return null;
		final Map<String,I3Mud> l=list.getMuds();
		for(final I3Mud m : l.values())
		{
			if(m.mud_name.equals(parms))
				return new XVector<I3Mud>(m);
		}
		for(final I3Mud m : l.values())
		{
			if(m.mud_name.equalsIgnoreCase(parms))
				return new XVector<I3Mud>(m);
		}
		if(parms.startsWith("*")&&(!parms.endsWith("*")))
		{
			final List<I3Mud> muds=new XVector<I3Mud>();
			for(final I3Mud m : l.values())
			{
				if(m.mud_name.toLowerCase().endsWith(parms.toLowerCase()))
					muds.add(m);
			}
			return muds;
		}
		if(parms.endsWith("*")&&(!parms.startsWith("*")))
		{
			final List<I3Mud> muds=new XVector<I3Mud>();
			for(final I3Mud m : l.values())
			{
				if(m.mud_name.toLowerCase().startsWith(parms.toLowerCase()))
					muds.add(m);
			}
			return muds;
		}
		if(parms.endsWith("*")&&(parms.startsWith("*")))
		{
			final List<I3Mud> muds=new XVector<I3Mud>();
			for(final I3Mud m : l.values())
			{
				if(m.mud_name.toLowerCase().indexOf(parms.toLowerCase())>=0)
					muds.add(m);
			}
			return muds;
		}
		final List<I3Mud> muds=new XVector<I3Mud>();
		for(final I3Mud m : l.values())
		{
			if((m.state<0)&&(CMLib.english().containsString(m.mud_name,parms)))
				muds.add(m);
		}
		return muds;
	}

	@Override
	public void i3mudInfo(final MOB mob, final String parms)
	{
		if((mob==null)||(!i3online()))
			return;
		if(mob.isMonster())
			return;
		final StringBuffer buf=new StringBuffer("\n\r");
		final List<I3Mud> muds=this.mudFinder(parms);
		if(muds.size()==0)
			buf.append("Not found!");
		else
		for(final I3Mud mudToShow : muds)
		{
			buf.append(CMStrings.padRight(L("Name"),10)+": "+mudToShow.mud_name+"\n\r");
			buf.append(CMStrings.padRight(L("Address"),10)+": "+mudToShow.address+"\n\r");
			buf.append(CMStrings.padRight(L("Port"),10)+": "+mudToShow.player_port+"\n\r");
			buf.append(CMStrings.padRight(L("Admin@"),10)+": "+mudToShow.admin_email+"\n\r");
			buf.append(CMStrings.padRight(L("Base"),10)+": "+mudToShow.base_mudlib+"\n\r");
			buf.append(CMStrings.padRight(L("MudLib"),10)+": "+mudToShow.mudlib+"\n\r");
			buf.append(CMStrings.padRight(L("Type"),10)+": "+mudToShow.mud_type+"\n\r");
			buf.append(CMStrings.padRight(L("Driver"),10)+": "+mudToShow.driver+"\n\r");
			buf.append(CMStrings.padRight(L("Status"),10)+": "+mudToShow.status+"\n\r");
		}
		mob.session().wraplessPrintln(buf.toString());
	}

	@Override
	public void giveIMC2MudList(final MOB mob)
	{
		if((mob==null)||(!imc2online()))
			return;
		if(mob.isMonster())
			return;
		final Hashtable<String,REMOTEINFO> l=imc2.query_muds();
		final Vector<REMOTEINFO> V=new Vector<REMOTEINFO>();
		for(final Enumeration<REMOTEINFO> e=l.elements();e.hasMoreElements();)
		{
			final REMOTEINFO m=e.nextElement();
			boolean done=false;
			for(int v=0;v<V.size();v++)
			{
				final REMOTEINFO m2=V.elementAt(v);
				if(m2.name.toUpperCase().compareTo(m.name.toUpperCase())>0)
				{
					V.insertElementAt(m,v);
					done=true;
					break;
				}
			}
			if(!done)
				V.addElement(m);
		}
		final StringBuffer buf=new StringBuffer("\n\rIMC2 Mud List:\n\r");
		for(int v=0;v<V.size();v++)
		{
			final REMOTEINFO m=V.elementAt(v);
			buf.append("["+CMStrings.padRight(m.name,15)+"]["+CMStrings.padRight(m.version,30)+"] "+CMStrings.padRight(m.network,13)+" ("+CMStrings.padRight(m.hub,10)+")\n\r");
		}
		mob.session().wraplessPrintln(buf.toString());
	}

	protected List<I3Mud> getSortedI3Muds()
	{
		final Vector<I3Mud> list = new Vector<I3Mud>();
		if(!i3online())
			return list;
		final MudList mudList=I3Client.getAllMudsList();
		if(mudList!=null)
		{
			for(final I3Mud m : mudList.getMuds().values())
			{
				if(m.state<0)
				{
					boolean done=false;
					for(int v=0;v<list.size();v++)
					{
						final I3Mud m2=list.elementAt(v);
						if(m2.mud_name.toUpperCase().compareTo(m.mud_name.toUpperCase())>0)
						{
							list.insertElementAt(m,v);
							done=true;
							break;
						}
					}
					if(!done)
						list.addElement(m);
				}
			}
		}
		return list;
	}

	@Override
	public List<String> getI3MudList(final boolean coffeemudOnly)
	{
		final List<String> list = new Vector<String>();
		if(!i3online())
			return list;
		 final List<I3Mud> muds = getSortedI3Muds();
		 for(final I3Mud mud : muds)
		 {
			 if((mud!=null) && ((!coffeemudOnly) || mud.base_mudlib.startsWith("CoffeeMud")))
				 list.add(mud.mud_name);
		 }
		 return list;
	}

	@Override
	public void giveI3MudList(final MOB mob)
	{
		if((mob==null)||(!i3online()))
			return;
		if(mob.isMonster())
			return;
		final StringBuffer buf=new StringBuffer("\n\rI3 Mud List:\n\r");
		final int col1Width=CMLib.lister().fixColWidth(25, mob);
		final int col2Width=CMLib.lister().fixColWidth(25, mob);
		 final List<I3Mud> muds = getSortedI3Muds();
		for(final I3Mud m : muds)
		{
			if((m!=null)&&(m.base_mudlib!=null))
			{
				final String mudlib = m.base_mudlib.startsWith("CoffeeMud") ? "^H"+m.base_mudlib+"^?" : m.base_mudlib;
				buf.append("["+CMStrings.padRight(m.mud_name,col1Width)+"]["+CMStrings.padRight(mudlib,col2Width)+"] "+m.address+" ("+m.player_port+")\n\r");
			}
		}
		mob.session().wraplessPrintln(buf.toString());
	}

	@Override
	public void giveI3ChannelsList(final MOB mob)
	{
		if((mob==null)||(!i3online()))
			return;
		if(mob.isMonster())
			return;
		final StringBuffer buf=new StringBuffer("\n\rI3 Channels List:\n\r");
		final ChannelList list=I3Client.getAllChannelList();
		if(list!=null)
		{
			final Hashtable<String,Channel> l=list.getChannels();
			final List<String> channelNames = new XArrayList<String>(l.keySet());
			Collections.sort(channelNames);
			for(final String channelName : channelNames)
			{
				final Channel c=l.get(channelName);
				buf.append("["+CMStrings.padRight(c.channel,20)+"] "+c.owner);
				if(c.type != 0)
					buf.append(" ^R(private)^?");
				buf.append("\n\r");
			}
		}
		mob.session().wraplessPrintln(buf.toString());
	}

	@Override
	public void giveIMC2ChannelsList(final MOB mob)
	{
		if((mob==null)||(!imc2online()))
			return;
		if(mob.isMonster())
			return;
		final StringBuffer buf=new StringBuffer("\n\rIMC2 Channels List:\n\r");
		final Hashtable<String,IMC_CHANNEL> channels=imc2.query_channels();
		buf.append(CMStrings.padRight(L("Name"), 22)+CMStrings.padRight(L("Policy"),25)+CMStrings.padRight(L("Owner"),20)+"\n\r");
		final Enumeration<String> e = channels.keys();
		while (e.hasMoreElements())
		{
			final String key = e.nextElement();
			final IMC_CHANNEL r = channels.get(key);
			if (r != null)
			{
				String policy = "final public";
				if (r.policy == IMC2Driver.CHAN_PRIVATE)
					policy = "(private)";
				else
				if (r.policy == IMC2Driver.CHAN_COPEN)
					policy = "open";
				else
				if (r.policy == IMC2Driver.CHAN_CPRIVATE)
					policy = "(cprivate)";

				buf.append(CMStrings.padRight(key, 22)+
						   CMStrings.padRight(policy+"("+r.level+")",25)+
						   r.owner+"\n\r");
			}
		}
		mob.session().wraplessPrintln(buf.toString());
	}

	@Override
	public boolean isIMC2channel(final String channelName)
	{
		if(!imc2online())
			return false;
		final Object remote=imc2.getAnIMC2Channel(channelName);
		if(remote==null)
			return false;
		return true;
	}

	@Override
	public boolean isI3channel(final String channelName)
	{
		if(!i3online())
			return false;
		final String remote=I3Client.getRemoteChannel(channelName);
		if(remote.length()==0)
			return false;
		return true;
	}

	public String socialFixOut(String str)
	{
		str=CMStrings.replaceAll(str,"<S-NAME>","$N");
		str=CMStrings.replaceAll(str,"<T-NAME>","$O");
		str=CMStrings.replaceAll(str,"<T-NAMESELF>","$O");
		str=CMStrings.replaceAll(str,"<S-HIM-HER>","$m");
		str=CMStrings.replaceAll(str,"<T-HIM-HER>","$M");
		str=CMStrings.replaceAll(str,"<S-HIS-HER>","$s");
		str=CMStrings.replaceAll(str,"<T-HIS-HER>","$S");
		str=CMStrings.replaceAll(str,"<S-HE-SHE>","$e");
		str=CMStrings.replaceAll(str,"<T-HE-SHE>","$E");
		str=CMStrings.replaceAll(str,"\'","`");
		if(str.equals(""))
			return "$";
		return str.trim();
	}

}
