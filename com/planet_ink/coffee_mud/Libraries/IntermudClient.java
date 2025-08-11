package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.IntermudInterface.InterProto;
import com.planet_ink.coffee_mud.Libraries.interfaces.IntermudInterface.RemoteIMud;
import com.planet_ink.coffee_mud.Libraries.intermud.*;
import com.planet_ink.coffee_mud.Libraries.intermud.cm1.CM1Server;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.CoffeeMudI3Bridge;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.I3Client;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.entities.I3Mud;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.net.*;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.server.*;
import com.planet_ink.coffee_mud.Libraries.intermud.imc2.*;
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
import com.planet_ink.coffee_mud.application.MUD;

import java.util.*;
import java.io.IOException;
import java.net.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class IntermudClient implements IntermudInterface
{
	@Override
	public String ID()
	{
		return "IntermudClient";
	}

	@Override
	public String name()
	{
		return ID();
	}

	protected CoffeeMudI3Bridge	i3mud		= null;
	protected IMC2Driver		imc2		= null;
	protected GrapevineClient	grapevine	= null;
	protected DiscordClient		discord		= null;
	protected List<CM1Server>	cm1Servers	= new Vector<CM1Server>();

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
		}
		catch(final Exception e)
		{
			return new IntermudClient();
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
		return CMLib.lang().fullSessionTranslation(getClass(), str, xs);
	}

	@Override
	public String L(final Class<?> clazz, final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(clazz, str, xs);
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public boolean startIntermud(final InterProto proto, final boolean restart)
	{
		if(restart)
			stopIntermud(proto);
		switch(proto)
		{
		case CM1:
			return startCM1();
		case Grapevine:
			return startGrapevine();
		case DISCORD:
			return startDiscord();
		case I3:
			return startI3();
		case IMC2:
			return startIntermud2();
		}
		return false;
	}

	@Override
	public void stopIntermud(final InterProto proto)
	{
		switch(proto)
		{
		case CM1:
			this.shutdownCM1();
			break;
		case Grapevine:
		{
			try
			{
				if(grapevine != null)
					grapevine.close();
			}
			catch (final IOException e)
			{}
			grapevine = null;
			break;
		}
		case DISCORD:
			if(discord != null)
				discord.stopClient();
			discord = null;
			break;
		case I3:
			if(i3mud != null)
				shutdownI3();
			break;
		case IMC2:
			this.shutdownIntermud2();
			break;
		}
	}

	private boolean startI3()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMProps page=CMProps.instance();
		try
		{
			if(page.getBoolean("RUNI3SERVER")
			&&(tCode==MUD.MAIN_HOST)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.I3)))
			{
				if(i3mud != null)
					i3mud.shutdown();
				i3mud = null;
				int i3port=page.getInt("I3PORT");
				if(i3port==0)
					i3port=27766;
				final String routersList = page.getStr("I3ROUTERS");
				final List<String> routersSepV = CMParms.parseCommas(routersList, true);
				if(routersSepV.size()>0)
				{
					final String adminEmail = CMProps.getVar(CMProps.Str.ADMINEMAIL);
					final String[] routersArray = routersSepV.toArray(new String[0]);
					final int smtpPort = CMath.s_int(CMLib.host().executeCommand("GET SMTP PORT"));
					i3mud=new CoffeeMudI3Bridge(CMProps.getVar(CMProps.Str.MUDNAME),
												 "CoffeeMud v"+CMProps.getVar(CMProps.Str.MUDVER),
												 CMLib.mud(0).getPublicPort(),
												 CMLib.channels().getIMudChannelsList(InterProto.I3),
												 i3port,
												 routersArray,
												 adminEmail,
												 smtpPort
												 );
				}
			}
		}
		catch(final Exception e)
		{
			if(i3mud!=null)
				i3mud.shutdown();
			i3mud = null;
		}
		return true;
	}

	private boolean startGrapevine()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMProps page=CMProps.instance();
		try
		{
			final String gvClientId = page.getProperty(CMProps.Str.GVCLIENTID.name());
			final String gvClientSecret = page.getProperty(CMProps.Str.GVCLIENTSECRET.name());
			if(page.getBoolean("RUNGVSERVER")
			&&(gvClientId!=null)
			&&(gvClientId.length()>0)
			&&(gvClientSecret!=null)
			&&(gvClientSecret.length()>0)
			&&(tCode==MUD.MAIN_HOST)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.GRAPEVINE)))
			{
				if(grapevine!=null)
					grapevine.close();
				grapevine = null;
				String playstate=page.getStr("MUDSTATE");
				if((playstate==null) || (playstate.length()==0))
					playstate=page.getStr("I3STATE");
				if((playstate==null) || (!CMath.isInteger(playstate)))
					playstate="Development";
				else
				switch(CMath.s_int(playstate.trim()))
				{
				case 0:
					playstate = "MudLib Development";
					break;
				case 1:
					playstate = "Restricted Access";
					break;
				case 2:
					playstate = "Beta Testing";
					break;
				case 3:
					playstate = "Open to the public";
					break;
				default:
					playstate = "MudLib Development";
					break;
				}
				grapevine=new GrapevineClient(gvClientId,gvClientSecret,CMLib.channels().getIMudChannelsList(InterProto.Grapevine));
				grapevine.start();
			}
		}
		catch(final Exception e)
		{
			if(grapevine!=null)
			try
			{
				grapevine.close();
			}
			catch (final IOException e1)
			{}
			grapevine=null;
			i3mud = null;
		}
		return true;
	}

	private boolean startCM1()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMProps page=CMProps.instance();
		CM1Server cm1server = null;
		try
		{
			final String runcm1=page.getPrivateStr("RUNCM1SERVER");
			if((runcm1!=null)&&(runcm1.equalsIgnoreCase("TRUE")))
			{
				final String iniFile = page.getStr("CM1CONFIG");
				for(final CM1Server s : cm1Servers)
				{
					if(s.getINIFilename().equalsIgnoreCase(iniFile))
					{
						s.shutdown();
						cm1Servers.remove(s);
					}
				}
				cm1server=new CM1Server("CM1Server"+tCode,iniFile);
				cm1server.start();
				cm1Servers.add(cm1server);
			}
			return true;
		}
		catch(final Exception e)
		{
			if(cm1server!=null)
			{
				cm1server.shutdown();
				cm1Servers.remove(cm1server);
			}
			return false;
		}
	}

	protected boolean startDiscord()
	{
		try
		{
			final List<CMChannel> channels = CMLib.channels().getIMudChannelsList(InterProto.DISCORD);
			if((CMProps.getVar(Str.DISCORD_JAR_PATH).length()>0)
			&&(CMProps.getVar(Str.DISCORD_BOT_KEY).length()>0)
			&&(CMProps.getVar(Str.DISCORD_BOT_KEY).length()>0)
			&&(channels.size()>0))
			{
				discord = new DiscordClient(channels);
				discord.startClient();
			}
			return true;
		}
		catch(final Exception e)
		{
			if(discord!=null)
				discord.stopClient();
			discord = null;
			return false;
		}
	}

	protected boolean startIntermud2()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMProps page=CMProps.instance();
		try
		{
			if(page.getBoolean("RUNIMC2CLIENT")&&(tCode==MUD.MAIN_HOST)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.IMC2)))
			{
				imc2=new IMC2Driver();
				if(!imc2.imc_startup(false,
											page.getStr("IMC2LOGIN").trim(),
											CMProps.getVar(CMProps.Str.MUDNAME),
											page.getStr("IMC2MYEMAIL").trim(),
											page.getStr("IMC2MYWEB").trim(),
											page.getStr("IMC2HUBNAME").trim(),
											page.getInt("IMC2HUBPORT"),
											page.getStr("IMC2PASS1").trim(),
											page.getStr("IMC2PASS2").trim(),
											CMLib.channels().getIMudChannelsList(InterProto.IMC2)))
				{
					Log.errOut(Thread.currentThread().getName(),"IMC2 Failed to start!");
					imc2=null;
				}
				else
				{
					imc2.start();
					return true;
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut(e);
		}
		return false;
	}

	@Override
	public boolean activate()
	{
		startCM1();
		startI3();
		startIntermud2();
		startGrapevine();
		startDiscord();
		return true;
	}

	protected void shutdownCM1()
	{
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...CM1Servers");
		for(final CM1Server cm1server : cm1Servers)
		{
			try
			{
				cm1server.shutdown();
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			finally
			{
				//if(S!=null)
				//	S.println(CMLib.lang().L("@x1 stopped",cm1server.getName()));
				Log.sysOut(CMLib.lang().L("@x1 stopped",cm1server.getName()));
				//if(debugMem) shutdownMemReport("CM1Server");
			}
		}
		cm1Servers.clear();
	}

	protected void shutdownI3()
	{
		if(i3mud!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...I3Server");
			try
			{
				i3mud.shutdown();
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			i3mud=null;
			//if(S!=null)
			//	S.println(CMLib.lang().L("I3Server stopped"));
			Log.sysOut(Thread.currentThread().getName(),"I3Server stopped");
			//if(debugMem) shutdownMemReport("I3Server");
		}
	}

	protected void shutdownIntermud2()
	{
		if(imc2!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...IMC2Server");
			try
			{
				imc2.shutdown();
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			imc2=null;
			//if(S!=null)
				//S.println(CMLib.lang().L("IMC2Server stopped"));
			Log.sysOut(Thread.currentThread().getName(),"IMC2Server stopped");
			//if(debugMem) shutdownMemReport("IMC2Server");
		}
	}

	@Override
	public boolean shutdown()
	{
		//final boolean debugMem = CMSecurity.isDebugging(CMSecurity.DbgFlag.SHUTDOWN);
		this.stopIntermud(InterProto.Grapevine);
		this.stopIntermud(InterProto.DISCORD);
		shutdownIntermud2();
		shutdownI3();
		shutdownCM1();
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

	@Override
	public void imudWho(final MOB mob, String mudName)
	{
		if(mob==null)
			return;
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell(L("You must specify a mud name."));
			return;
		}
		boolean triedAny=false;
		if(i3online()&&I3Client.isAPossibleMUDName(mudName))
		{
			mudName=I3Client.translateName(mudName);
			if(!I3Client.isUp(mudName))
				mob.tell(L("@x1 is not available.",mudName));
			else
			{
				triedAny=true;
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
					Log.errOut("IntermudClient", e);
				}
			}
		}

		if(imc2online()&&(imc2.getIMC2Mud(mudName)!=null))
		{
			triedAny=true;
			imc2mudInfo(mob,mudName);
		}

		if(isOnline(InterProto.Grapevine) && grapevine.isAKnownMud(mudName))
		{
			final String fmudName = grapevine.getKnownMud(mudName);
			if(fmudName != null)
			{
				triedAny=true;
				grapevine.playerStatusReq(mob,fmudName);
			}
		}

		if(!triedAny)
		{
			mob.tell(L("'@x1' is not an available mud name.",mudName));
			return;
		}
	}

	private boolean i3online()
	{
		return (i3mud != null) && (!CMSecurity.isDisabled(DisFlag.I3)) && i3mud.isOnline();
	}

	private boolean imc2online()
	{
		if((imc2==null)||(CMSecurity.isDisabled(DisFlag.IMC2)))
			return false;
		return imc2.imc_active==IMC2Driver.IA_UP;
	}

	private void imc2mudInfo(final MOB mob, final String parms)
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
	public void imudTell(final MOB mob, String tellName, String mudName, final String message)
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
		boolean sentAny=false;
		if(i3online()&&I3Client.isAPossibleMUDName(mudName))
		{
			sentAny=true;
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
				Log.errOut("IntermudClient", e);
			}
		}
		if(imc2online()&&(imc2.getIMC2Mud(mudName)!=null))
		{
			sentAny=true;
			tellName=CMStrings.capitalizeAndLower(tellName)+"@"+imc2.getIMC2Mud(mudName).name;
			mob.tell(L("^CYou tell @x1 '@x2'^?",tellName,message));
			if(mob.playerStats()!=null)
				mob.playerStats().addTellStack(mob.Name(), tellName, "You tell "+tellName+" '"+message+"'");
			imc2.imc_send_tell(mob.name(),tellName,message,0,CMLib.flags().isInvisible(mob)?1:0);
		}

		if(isOnline(InterProto.Grapevine)
		&& (grapevine.isAKnownMud(mudName)))
		{
			sentAny = true;
			if(grapevine.isPlayerOnMud(mudName,tellName)==null)
				mob.tell(L("@x1 is not on @x2.",tellName,mudName));
			else
			{
				final String realName = grapevine.getKnownMud(mudName);
				final String fullTellName=CMStrings.capitalizeAndLower(tellName)+"@"+realName;
				mob.tell(L("^CYou tell @x1 '@x2'^?",fullTellName,message));
				if(mob.playerStats()!=null)
					mob.playerStats().addTellStack(mob.Name(), tellName, "You tell "+fullTellName+" '"+message+"'");
				grapevine.sendTell(mob.Name(), tellName, realName, CMStrings.removeColors(message));
			}
		}

		if(!sentAny)
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

	protected void sendIMC2ChannelMsg(final MOB mob, final String channelName, String message)
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


	protected void sendGrapevineChannelMsg(final MOB mob, final String channelName, final String message)
	{
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
					final String mudName=msg.target().name().substring(x+1);
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
					if(!grapevine.isAKnownMud(mudName))
					{
						mob.tell(L("'@x1' is an unknown mud or is not presently available.",mudName));
						return;
					}
					final String finalMsg = socialNormalFixOut(
							mob.Name()+"@"+CMProps.getVar(CMProps.Str.MUDNAME),
							msg.target().name(),CMStrings.removeColors(msg.othersMessage()));
					grapevine.sendMappedChannelMessage(mob.Name(), channelName, finalMsg);
				}
				else
				if(msg.target()!=null)
				{
					final String finalMsg = socialNormalFixOut(
							mob.Name()+"@"+CMProps.getVar(CMProps.Str.MUDNAME),
							msg.target().name(),
							CMStrings.removeColors(msg.othersMessage()));
					grapevine.sendMappedChannelMessage(mob.Name(), channelName, finalMsg);
				}
				else
				{
					final String finalMsg = socialNormalFixOut(
							mob.Name()+"@"+CMProps.getVar(CMProps.Str.MUDNAME),
							msg.target().name(),
							CMStrings.removeColors(msg.othersMessage()));
					grapevine.sendMappedChannelMessage(mob.Name(), channelName, finalMsg);
				}
			}
			else
			{
				if(msgstr.trim().startsWith("'")||msgstr.trim().startsWith("`"))
					msgstr=msgstr.trim();
				else
					msgstr=" "+msgstr.trim();
				grapevine.sendMappedChannelMessage(mob.Name(), channelName, mob.Name()+msgstr);
			}
		}
		else
		{
			grapevine.sendMappedChannelMessage(mob.Name(), channelName, CMStrings.removeColors(message));
		}
	}

	@Override
	public void imudChannel(final MOB mob, final String channelName, final String message)
	{
		if(mob==null)
			return;
		if(!this.isAnyNonCM1Online() || !this.isAnyImudChannel(channelName))
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
		if(imc2online()&&(imc2.getAnIMC2Channel(channelName)!=null))
			this.sendIMC2ChannelMsg(mob, channelName, message);

		if(i3online() && I3Client.getRemoteChannel(channelName).length()>0)
			i3mud.sendI3ChannelMsg(mob, channelName, message);

		if(this.isOnline(InterProto.DISCORD))
		{
			final String message2=CMStrings.replaceAll(message,"<S-NAME>",mob.name());
			discord.sendDiscordMsg(channelName, message2);
		}

		if(this.isOnline(InterProto.Grapevine) && grapevine.isMappedChannel(channelName))
			this.sendGrapevineChannelMsg(mob, channelName, message);
	}

	@Override
	public void imudLocate(final MOB mob, final String mobName)
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
				Log.errOut("IntermudClient", e);
			}
		}
		if(imc2online())
			imc2.imc_send_whois(mob.Name(),mobName,mob.phyStats().level());
		if(isOnline(InterProto.Grapevine))
		{
			final List<String> list = grapevine.getPlayerOnMuds(mobName);
			if(list.size()==0)
				mob.tell(L("No players '@x1' found on Grapevine.",mobName));
			else
				mob.tell(L("A player '@x1' was found on: @x2",mobName,CMParms.toListString(list)));
		}
	}

	public void i3pingRouter(final MOB mob)
	{
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
				Log.errOut("IntermudClient", e);
			}
		}
	}

	@Override
	public void imudFinger(final MOB mob, final String mobName, String mudName)
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

		boolean foundOne=false;
		if(i3online() && I3Client.isAPossibleMUDName(mudName))
		{
			mudName=I3Client.translateName(mudName);
			if(!I3Client.isUp(mudName))
				mob.tell(L("@x1 is not available.",mudName));
			else
			{
				foundOne=true;
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
					Log.errOut("IntermudClient", e);
				}
			}
		}

		if(imc2online())
		{
			foundOne=true;
			imc2.imc_send_whois(mob.Name(),mobName,mob.phyStats().level());
		}

		if(!foundOne)
			mob.tell(L("@x1 is unknown or not available.",mudName));
	}

	public String getI3MudDetails(final I3Mud mudToShow)
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

	public void i3mudInfo(final MOB mob, final String parms)
	{
		if((mob==null)||(!i3online()))
			return;
		if(mob.isMonster())
			return;
		final StringBuffer buf=new StringBuffer("\n\r");
		final List<I3Mud> muds=(i3mud==null)?new ArrayList<I3Mud>():i3mud.i3MudFinder(parms);
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

	protected List<String> getIMC2MudList(final boolean coffeemudOnly)
	{
		final Hashtable<String,REMOTEINFO> l=imc2.query_muds();
		final Vector<String> V=new Vector<String>();
		for(final Enumeration<REMOTEINFO> e=l.elements();e.hasMoreElements();)
		{
			final REMOTEINFO r = e.nextElement();
			if(!coffeemudOnly)
				V.add(r.name);
			else
			if((r.version!=null)&&(r.version.toLowerCase().indexOf("coffeemud")>=0))
				V.add(r.name);
		}
		Collections.sort(V);
		return V;
	}

	public List<RemoteIMud> getIMC2MudInfo(final boolean coffeemudOnly)
	{
		final List<RemoteIMud> list = new Vector<RemoteIMud>();
		if(!imc2online())
			return list;
		for(final Enumeration<REMOTEINFO> n = imc2.query_muds().elements();n.hasMoreElements();)
		{
			final REMOTEINFO m = n.nextElement();
			final RemoteIMud mud = new RemoteIMud();
			mud.name = m.name;
			mud.mudLib = m.version;
			if(coffeemudOnly)
			{
				if((mud.mudLib==null)||(mud.mudLib.toLowerCase().indexOf("coffeemud")<0))
					continue;
			}
			mud.proto = InterProto.IMC2;
			mud.numOnline = -1;
			mud.hostPort = m.url;
			list.add(mud);
		}
		Collections.sort(list,new Comparator<RemoteIMud>(){
			@Override
			public int compare(final RemoteIMud o1, final RemoteIMud o2)
			{
				return o1.name.compareTo(o2.name);
			}
		});
		return list;
	}

	public List<String> getI3MudList(final boolean coffeemudOnly)
	{
		final List<String> list = new Vector<String>();
		if(i3mud == null)
			return list;
		 final List<I3Mud> muds = i3mud.getSortedI3Muds();
		 if(muds == null)
			 return list;
		 for(final I3Mud mud : muds)
		 {
			 if((mud!=null) && ((!coffeemudOnly) || mud.base_mudlib.startsWith("CoffeeMud")))
				 list.add(mud.mud_name);
		 }
		 return list;
	}

	public List<RemoteIMud> getI3MudInfo(final boolean coffeemudOnly)
	{
		final List<RemoteIMud> list = new Vector<RemoteIMud>();
		if(i3mud==null)
			return list;
		for(final I3Mud m : i3mud.getSortedI3Muds())
		{
			final RemoteIMud mud = new RemoteIMud();
			mud.name = m.mud_name;
			mud.mudLib = m.base_mudlib;
			mud.proto = InterProto.I3;
			mud.numOnline = -1;
			mud.hostPort = m.address + ":"+m.player_port;
			list.add(mud);
		}
		return list;
	}

	public List<String> getGVMudList(final boolean coffeemudOnly)
	{
		final List<String> list = new Vector<String>();
		if(!isOnline(InterProto.Grapevine))
			return list;
		 final List<RemoteIMud> muds = getGVMudInfo(coffeemudOnly);
		 for(final RemoteIMud mud : muds)
			 list.add(mud.name);
		 return list;
	}

	public List<RemoteIMud> getGVMudInfo(final boolean coffeemudOnly)
	{
		final List<RemoteIMud> list = new Vector<RemoteIMud>();
		if(!isOnline(InterProto.Grapevine))
			return list;
		for(final MiniJSON.JSONObject j : grapevine.getKnownMuds())
		{
			final RemoteIMud mud = new RemoteIMud();
			final String online = ""+j.get("players_online_count");
			mud.name = (String)j.get("game");
			mud.mudLib = (String)j.get("user_agent");
			mud.proto = InterProto.Grapevine;
			mud.numOnline = CMath.s_int(online);
			final Object[] cconnections = (Object[])j.get("connections");
			if(cconnections != null)
			{
				for(int i=0;i<cconnections.length;i++)
				{
					try
					{
						final MiniJSON.JSONObject cobj = (MiniJSON.JSONObject)cconnections[i];
						if (cobj.getCheckedString("type").equalsIgnoreCase("telnet"))
						{
							mud.hostPort = cobj.getCheckedString("host")+":"+cobj.getCheckedLong("mudPort");
							break;
						}
						else
						if (cobj.getCheckedString("type").equalsIgnoreCase("secure telnet"))
							mud.hostPort = cobj.getCheckedString("host")+":"+cobj.getCheckedLong("mudPort");
					}
					catch(final MiniJSON.MJSONException e)
					{}
				}
			}
			if(mud.hostPort.length()==0)
				mud.hostPort = "Unknown";
			list.add(mud);
		}
		return list;
	}

	public TriadList<String,String,String> giveI3ChannelsList()
	{
		if(!i3online())
			return new TriadVector<String,String,String>();
		final ChannelList list=I3Client.getAllChannelList();
		final TriadList<String,String,String> retList = new TriadVector<String,String,String>();
		if(list!=null)
		{
			final Hashtable<String,Channel> l=list.getChannels();
			final List<String> channelNames = new XArrayList<String>(l.keySet());
			Collections.sort(channelNames);
			for(final String channelName : channelNames)
			{
				final Channel c=l.get(channelName);
				final String app = (c.type == 0)?"":" ^R(private)^?";
				retList.add(c.channel, c.owner, app);
			}
		}
		return retList;
	}

	public TriadList<String,String,String> giveIMC2ChannelsList()
	{
		if(!imc2online())
			return new TriadVector<String,String,String>();
		final TriadList<String,String,String> retList = new TriadVector<String,String,String>();
		final Hashtable<String,IMC_CHANNEL> channels=imc2.query_channels();
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
				retList.add(key,r.owner,policy+"("+r.level+")");
			}
		}
		return retList;
	}

	public boolean isIMC2channel(final String channelName)
	{
		if(!imc2online())
			return false;
		final Object remote=imc2.getAnIMC2Channel(channelName);
		if(remote==null)
			return false;
		return true;
	}

	public String socialNormalFixOut(final String sourceName, final String targetName, String str)
	{
		str=CMStrings.replaceAll(str,"<S-NAME>",sourceName);
		str=CMStrings.replaceAll(str,"<T-NAME>",targetName);
		str=CMStrings.replaceAll(str,"<T-NAMESELF>",targetName);
		str=CMStrings.replaceAll(str,"<S-HIM-HER>",sourceName);
		str=CMStrings.replaceAll(str,"<T-HIM-HER>",targetName);
		str=CMStrings.replaceAll(str,"<S-HIS-HER>",sourceName);
		str=CMStrings.replaceAll(str,"<T-HIS-HER>",targetName);
		str=CMStrings.replaceAll(str,"<S-HE-SHE>",sourceName);
		str=CMStrings.replaceAll(str,"<T-HE-SHE>",targetName);
		str=CMStrings.replaceAll(str,"\'","`");
		return str.trim();
	}

	@Override
	public String queryService(final InterProto proto, final InterQuery query)
	{
		switch(proto)
		{
		case CM1:
			switch(query)
			{
			case NAME:
				if(cm1Servers.size()>0)
					return ""+cm1Servers.get(0).getName();
				break;
			case PORT:
				if(cm1Servers.size()>0)
					return ""+cm1Servers.get(0).getPort();
				break;
			case RUNNING:
				return ""+(this.cm1Servers.size()>0);
			}
			break;
		case Grapevine:
			switch(query)
			{
			case NAME:
				return grapevine.getName();
			case PORT:
				return "-1";
			case RUNNING:
				return ""+isOnline(InterProto.Grapevine);
			}
			break;
		case DISCORD:
			switch(query)
			{
			case NAME:
				return "DiscordClient";
			case PORT:
				return "-1";
			case RUNNING:
				return ""+isOnline(InterProto.DISCORD);
			}
			break;
		case I3:
			switch(query)
			{
			case NAME:
				if(i3online() && (this.i3mud!=null))
					return this.i3mud.name;
				break;
			case PORT:
				if(i3online())
					return ""+I3Server.getPort();
				break;
			case RUNNING:
				return ""+this.i3online();
			}
			break;
		case IMC2:
			switch(query)
			{
			case NAME:
				if(imc2online())
					return this.imc2.getName();
				break;
			case PORT:
				if(imc2online())
					return ""+this.imc2.imc_siteinfo.port;
				break;
			case RUNNING:
				return imc2online()+"";
			}
			break;
		}
		return "";
	}

	@Override
	public void pingRouter(final InterProto proto, final MOB mob)
	{
		if(mob==null)
			return;
		switch(proto)
		{
		case CM1:
			break;
		case Grapevine:
			break;
		case DISCORD:
			break;
		case I3:
			this.i3pingRouter(mob);
			break;
		case IMC2:
			if(imc2online())
				imc2.imc_send_ping("Server01");
			break;
		}
	}

	@Override
	public List<String> getAllMudList(final boolean coffeemudOnly)
	{
		final Set<String> all = new TreeSet<String>();
		for(final InterProto proto : InterProto.values())
			all.addAll(getMudList(proto,coffeemudOnly));
		return new XVector<String>(all);
	}

	@Override
	public List<String> getMudList(final InterProto proto, final boolean coffeemudOnly)
	{
		switch(proto)
		{
		case CM1:
			return new Vector<String>(0);
		case DISCORD:
			return new Vector<String>(0); // doesn't really apply, unexposed info
		case Grapevine:
			return getGVMudList(coffeemudOnly);
		case I3:
			return getI3MudList(coffeemudOnly);
		case IMC2:
			return getIMC2MudList(coffeemudOnly);
		}
		return new Vector<String>(0);
	}

	@Override
	public boolean isImudChannel(final InterProto proto, final String channelName)
	{
		switch(proto)
		{
		case CM1:
			return false;
		case Grapevine:
			if(grapevine != null)
			{
				if(grapevine.getLocalIMudChannelsList().contains(channelName))
					return true;
				return grapevine.getRemoteChannelsList().containsFirst(channelName);
			}
			break;
		case DISCORD:
			if(discord != null)
			{
				if(discord.getLocalIMudChannelsList().contains(channelName))
					return true;
				return discord.getRemoteChannelsList().containsFirst(channelName);
			}
			break;
		case I3:
			if(i3mud != null)
				return i3mud.isLocalI3channel(channelName) || i3mud.isRemoteI3Channel(channelName);
			break;
		case IMC2:
			return isIMC2channel(channelName);
		}
		return false;
	}

	@Override
	public boolean isAnyImudChannel(final String channelName)
	{
		for(final InterProto proto : InterProto.values())
			if(isImudChannel(proto,channelName))
				return true;
		return false;
	}

	@Override
	public boolean isOnline(final InterProto proto)
	{
		switch(proto)
		{
		case CM1:
			return cm1Servers.size()>0;
		case Grapevine:
			return (grapevine != null) && (grapevine.isOnline());
		case DISCORD:
			return (discord != null) && (discord.isActive());
		case I3:
			return this.i3online();
		case IMC2:
			return this.imc2online();
		}
		return false;
	}

	@Override
	public void chanWho(final MOB mob, final String channel, final String mudName)
	{
		if((mudName==null)||(mudName.length()==0))
		{
			mob.tell(L("You must specify a mud name."));
			return;
		}
		if((channel==null)||(channel.length()==0))
		{
			mob.tell(L("You must specify an channel name."));
			return;
		}
		boolean tried = false;
		// something only i3 supports. :(
		for(final InterProto proto : InterProto.values())
		{
			switch(proto)
			{
			case CM1:
				break;
			case Grapevine:
				break;
			case DISCORD:
				break;
			case I3:
				if(i3mud != null)
					tried = tried || i3mud.i3chanwho(mob, channel, mudName);
				break;
			case IMC2:
				break;
			}
		}
		if(!tried)
			mob.tell(L("That may nto have been a valid channel or mud name."));
	}

	@Override
	public void getChannelsList(final MOB mob)
	{
		for(final InterProto proto : InterProto.values())
			getChannelsList(mob, proto);
	}

	@Override
	public void getChannelsList(final MOB mob, final InterProto proto)
	{
		if((mob==null)||(mob.isMonster()))
			return;
		TriadList<String,String,String> list = new TriadVector<String,String,String>(0);
		switch(proto)
		{
		case CM1:
			return;
		case Grapevine:
			if(grapevine != null)
				list = grapevine.getRemoteChannelsList();
			break;
		case DISCORD:
			if(discord != null)
				list = discord.getRemoteChannelsList();
			break;
		case I3:
			list = this.giveI3ChannelsList();
			break;
		case IMC2:
			list = this.giveIMC2ChannelsList();
			break;
		}
		final StringBuffer buf=new StringBuffer("\n\r"+proto.name()+" Channels List:\n\r");
		for(final Triad<String,String,String> chan : list)
		{
			buf.append("["+CMStrings.padRight(chan.first,20)+"] "+chan.second + chan.third);
			buf.append("\n\r");
		}
		mob.session().wraplessPrintln(buf.toString());
	}

	@Override
	public void channelListen(final InterProto proto, final MOB mob, final String channel)
	{
		switch(proto)
		{
		case CM1:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			return;
		case DISCORD:
			if(discord != null)
			{
				final boolean result = discord.addChannel(channel);
				if(mob != null)
				{
					if(result)
						mob.tell(L("Done."));
					else
						mob.tell(L("Fail."));
				}
			}
			break;
		case Grapevine:
			grapevine.subscribeChannel(channel);
			break;
		case I3:
			if(i3mud != null)
				i3mud.i3channelListen(mob, channel);
			break;
		case IMC2:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			break;
		}
	}

	@Override
	public void channelSilence(final InterProto proto, final MOB mob, final String channel)
	{
		switch(proto)
		{
		case CM1:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			return;
		case DISCORD:
			if(discord != null)
			{
				final boolean result = discord.removeChannel(channel);
				if(mob != null)
				{
					if(result)
						mob.tell(L("Done."));
					else
						mob.tell(L("Fail."));
				}
			}
			break;
		case Grapevine:
			if(grapevine != null)
				grapevine.unsubscribeChannel(channel);
			break;
		case I3:
			if(i3mud != null)
				i3mud.i3channelSilence(mob, channel);
			break;
		case IMC2:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			break;
		}
	}

	@Override
	public void channelAdd(final InterProto proto, final MOB mob, final String channel)
	{
		switch(proto)
		{
		case CM1:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			return;
		case DISCORD:
			if(discord != null)
			{
				final boolean result = discord.addChannel(channel);
				if(mob != null)
				{
					if(result)
						mob.tell(L("Done."));
					else
						mob.tell(L("Fail."));
				}
			}
			break;
		case Grapevine:
			if(grapevine != null)
				grapevine.subscribeChannel(channel);
			break;
		case I3:
			if(i3mud != null)
				i3mud.i3channelAdd(mob, channel);
			break;
		case IMC2:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			break;
		}
	}

	@Override
	public void channelRemove(final InterProto proto, final MOB mob, final String channel)
	{
		switch(proto)
		{
		case CM1:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			return;
		case DISCORD:
			if(discord != null)
			{
				final boolean result = discord.removeChannel(channel);
				if(mob != null)
				{
					if(result)
						mob.tell(L("Done."));
					else
						mob.tell(L("Fail."));
				}
			}
			break;
		case Grapevine:
			if(grapevine != null)
				grapevine.unsubscribeChannel(channel);
			break;
		case I3:
			if(i3mud != null)
				i3mud.i3channelRemove(mob, channel);
			break;
		case IMC2:
			if(mob != null)
				mob.tell(L("Not supported for @x1",proto.name()));
			break;
		}
	}

	@Override
	public void mudInfo(final MOB mob, final String parms)
	{
		for(final InterProto proto : InterProto.values())
			mudInfo(proto, mob, parms);
	}

	@Override
	public void mudInfo(final InterProto proto, final MOB mob, final String parms)
	{
		if((mob==null)||(mob.isMonster()))
			return;
		final Session sess = mob.session();
		if(sess == null)
			return;
		final String mudName = parms;
		if(proto == InterProto.I3)
		{
			if(i3online() && I3Client.isAPossibleMUDName(mudName))
			{
				final List<I3Mud> muds=i3mud.i3MudFinder(parms);
				if(muds.size()==1)  // 2 means a non-exact match, and 0 is none
				{
					final StringBuilder buf=new StringBuilder("\n\r^HI3:^?");
					buf.append(this.getI3MudDetails(muds.get(0)));
					sess.print(buf.toString());
				}
			}
		}
		if(proto == InterProto.IMC2)
		{
			if(imc2online() && imc2.getIMC2Mud(mudName)!=null)
			{
				final REMOTEINFO mud = imc2.getIMC2Mud(mudName);
				final StringBuilder buf=new StringBuilder("\n\r^HIMC2:^?");
				buf.append(CMStrings.padRight(L("Name"),10)+": "+mud.name+"\n\r");
				if(mud.network != null)
					buf.append(CMStrings.padRight(L("Network"),10)+": "+mud.network+"\n\r");
				buf.append(CMStrings.padRight(L("Url"),10)+": "+mud.url+"\n\r");
				buf.append(CMStrings.padRight(L("Version"),10)+": "+mud.version+"\n\r");
				if(mud.hub != null)
					buf.append(CMStrings.padRight(L("Hub"),10)+": "+mud.hub+"\n\r");
				sess.print(buf.toString());
			}
		}
		if(proto == InterProto.Grapevine)
		{
			if(isOnline(InterProto.Grapevine) && (grapevine.getMud(mudName)!=null))
			{
				final MiniJSON.JSONObject obj = grapevine.getMud(mudName);
				grapevine.gameStatusReq(mob, (String)obj.get("name"));
			}
		}
	}

	@Override
	public boolean isAnyNonCM1Online()
	{
		for(final InterProto proto : InterProto.values())
			if((proto != InterProto.CM1) && (isOnline(proto)))
				return true;
		return false;
	}

	@Override
	public List<RemoteIMud> getAllMudInfo(final boolean coffeemudOnly)
	{
		final List<RemoteIMud> list = new Vector<RemoteIMud>();
		for(final InterProto proto : InterProto.values())
		{
			for(final RemoteIMud mud : getMudInfo(proto,coffeemudOnly))
			{
				mud.proto = proto;
				list.add(mud);
			}
		}
		return list;
	}

	@Override
	public void registerPlayerOnline(final MOB mob)
	{
		for(final InterProto proto : InterProto.values())
			switch(proto)
			{
			case CM1: break;
			case DISCORD: break;
			case Grapevine:
				if(isOnline(InterProto.Grapevine))
					grapevine.playerLoggedIn(mob.Name());
				break;
			case I3: break;
			case IMC2: break;
			}
	}

	@Override
	public void registerPlayerOffline(final MOB mob)
	{
		for(final InterProto proto : InterProto.values())
			switch(proto)
			{
			case CM1: break;
			case DISCORD: break;
			case Grapevine:
				if(isOnline(InterProto.Grapevine))
					grapevine.playerLoggedOut(mob.Name());
				break;
			case I3: break;
			case IMC2: break;
			}
	}

	@Override
	public List<RemoteIMud> getMudInfo(final InterProto proto, final boolean coffeemudOnly)
	{
		switch(proto)
		{
		case CM1:
			return new Vector<RemoteIMud>();
		case DISCORD:
			return new Vector<RemoteIMud>();
		case Grapevine:
			return this.getGVMudInfo(coffeemudOnly);
		case I3:
			return this.getI3MudInfo(coffeemudOnly);
		case IMC2:
			return this.getIMC2MudInfo(coffeemudOnly);
		}
		return new Vector<RemoteIMud>();
	}
}
