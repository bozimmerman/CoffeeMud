package com.planet_ink.coffee_mud.application;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.RNameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.router.I3RouterThread;
import com.planet_ink.coffee_mud.core.intermud.i3.router.MudPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.router.RouterPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.server.ServerSecurityException;

/*
Copyright 2024-2024 Bo Zimmerman

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
public class I3Router
{
	public static String getArgParm(final String[] args, final String parmid)
	{
		for(int i = 0;i<args.length;i++)
		{
			final String arg = args[i];
			if(arg.startsWith("--"))
			{
				if(arg.toLowerCase().startsWith(parmid))
				{
					String parm = arg.substring(parmid.length()).trim();
					if(parm.startsWith("="))
						parm=parm.substring(1).trim();
					if(parm.length()>0)
						return parm;
					if((i<args.length-1)
					&&(!args[i+1].startsWith("--")))
						return args[i+1];
				}
			}
		}
		return null;
	}

	public static void initThreadGroup(final int daemon, final int debug)
	{
		final Log log = Log.newInstance();
		if(daemon == 0)
		{
			for(final Log.Type type : Log.Type.values())
				log.configureLog(type, "ON");
		}
		else
		{
			log.configureLogFile("i3router", 1);
			for(final Log.Type type : Log.Type.values())
				log.configureLog(type, "FILE");
		}
		if(debug > 0)
			CMSecurity.setDebugVar(CMSecurity.DbgFlag.I3);
		try
		{
			CMProps.instance().load(new ByteArrayInputStream(new byte[0]));
			CMLib.initialize();
		}
		catch (final Exception e1)
		{
			e1.printStackTrace();
			System.exit(-1);
		}
	}


	public static void main(final String[] args)
	{
		final String name = getArgParm(args,"--name");
		final String ip = getArgParm(args,"--ip");
		final int debug = CMath.s_int(getArgParm(args,"--debug"));
		final int daemon = CMath.s_int(getArgParm(args,"--daemon"));
		final int port = CMath.s_int(getArgParm(args,"--port"));
		final int password = CMath.s_int(getArgParm(args,"--password"));;
		if((name == null) || (ip == null) || (port < 0))
		{
			System.out.println("I3Router:");
			System.out.println("--name x router name");
			System.out.println("--ip x remote access ip address");
			System.out.println("--port x port to listen on");
			System.out.println("--password x router password (number from 0 - 999999)");
			System.out.println("--debug=1 turn on debug messages");
			System.out.println("--daemon=1 turn off interactive prompt");
			System.exit(-1);
		}
		initThreadGroup(daemon, debug);
		new Thread(new ThreadGroup("0-I3R"), new Runnable() {
			@Override
			public void run()
			{
				try
				{
					initThreadGroup(daemon, debug);
					com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.start(name, ip, port, password);
				}
				catch(final Exception e)
				{
					System.err.println(e.getMessage());
				}
			}
		}).start();
		if(daemon > 0)
		{
			try
			{
				Thread.sleep(1000);
				while(com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getRouter().running)
				{
					Thread.sleep(1000);
				}
			}
			catch(final Exception e)
			{
				System.exit(-1);
			}
			finally
			{
				com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.shutdown();
			}
			System.exit(0);
		}
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("I3 Router "+name+" interactive mode.  Use ? for a menu.\r\n> ");
		try
		{
			String s = br.readLine();
			boolean running=true;
			while(running)
			{
				final I3RouterThread router = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getRouter();
				if(s.equalsIgnoreCase("?"))
				{
					System.out.println("Menu: ");
					System.out.println("info x   : show info on peer, mud, channel x");
					System.out.println("list x   : list peers, muds, channels");
					System.out.println("add x ...: add peer name address port password");
					System.out.println("boot x ..: boot peer name");
					System.out.println("         : boot mud name");
					System.out.println("del x y  : del peer name");
					System.out.println("         : del mud name");
					System.out.println("         : del channel name");
					System.out.println("exit     : shutdown");
					System.out.println(" * names are case-sensitive");
				}
				else
				if(s.toLowerCase().startsWith("add "))
				{
					s=s.substring(4).trim();
					if(s.toLowerCase().startsWith("peer "))
					{
						s=s.substring(5).trim();
						final String[] parts = s.split(" ");
						if(parts.length!=4)
							System.err.println("Try add peer name address port password");
						else
						{
							final RNameServer ns = new RNameServer(parts[1],CMath.s_int(parts[2]),parts[0]);
							ns.password = CMath.s_int(parts[3]);
							final RouterPeer rp = new RouterPeer(ns,null);
							rp.connect();
							if(rp.isConnected())
							{
								com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.addObject(rp);
								com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getRouter().initializePeer(rp);
								System.out.println("Added peer '"+ns.name+"'");
							}
							else
								System.err.println("Could not connect to peer '"+ns.name+"'");
						}
					}
					else
						System.err.println("Try add peer");
				}
				else
				if(s.toLowerCase().startsWith("del "))
				{
					s=s.substring(4).trim();
					if(s.toLowerCase().startsWith("peer "))
					{
						s=s.substring(5).trim();
						final RouterPeer[] peers = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getRouterPeers();
						RouterPeer peer = null;
						for(int i=0;i<peers.length;i++)
							if(peers[i].name.equals(s))
								peer=peers[i];
						if(peer == null)
							System.err.println("Peer '"+s+"' does not exist.");
						else
						{
							com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.destroyObjectForever(peer);
							System.err.println("Peer '"+s+"'has been destroyed.");
						}
					}
					else
					if(s.toLowerCase().startsWith("mud "))
					{
						s=s.substring(4).trim();
						final MudPeer[] muds = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getMudPeers();
						MudPeer mud = null;
						for(int i=0;i<muds.length;i++)
							if(muds[i].mud_name.equals(s))
								mud=muds[i];
						if(mud == null)
							System.err.println("Mud '"+s+"' does not exist.");
						else
						{
							com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.destroyObjectForever(mud);
							System.err.println("Mud '"+s+"'has been destroyed.");
						}
					}
					else
					if(s.toLowerCase().startsWith("channel "))
					{
						s=s.substring(8).trim();
						final Channel chan = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.findChannel(s);
						if(chan == null)
							System.err.println("Chan '"+s+"' does not exist.");
						else
						{
							com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.destroyObjectForever(chan);
							System.err.println("Chan '"+s+"'has been destroyed.");
						}
					}
					else
						System.err.println("Try del peer, mud, or channel");
				}
				else
				if(s.toLowerCase().startsWith("boot "))
				{
					s=s.substring(5).trim();
					if(s.toLowerCase().startsWith("peer "))
					{
						s=s.substring(5).trim();
						final RouterPeer[] peers = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getRouterPeers();
						RouterPeer peer = null;
						for(int i=0;i<peers.length;i++)
							if(peers[i].name.equals(s))
								peer=peers[i];
						if(peer == null)
							System.err.println("Peer '"+s+"' does not exist.");
						else
						if(!peer.isConnected())
							System.err.println("Peer '"+s+"' is not connected.");
						else
						{
							peer.destruct();
							System.err.println("Peer '"+s+"'has been booted.");
						}
					}
					else
					if(s.toLowerCase().startsWith("mud "))
					{
						s=s.substring(4).trim();
						final MudPeer[] muds = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getMudPeers();
						MudPeer mud = null;
						for(int i=0;i<muds.length;i++)
							if(muds[i].mud_name.equals(s))
								mud=muds[i];
						if(mud == null)
							System.err.println("Mud '"+s+"' does not exist.");
						else
						if(!mud.isConnected())
							System.err.println("Mud '"+s+"' is not connected.");
						else
						{
							mud.destruct();
							System.err.println("Mud '"+s+"'has been booted.");
						}
					}
					else
						System.err.println("Try del peer, mud, or channel");
				}
				else
				if(s.toLowerCase().startsWith("list "))
				{
					s=s.substring(5).trim();
					if(s.equalsIgnoreCase("peers"))
					{
						final RouterPeer[] peers = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getRouterPeers();
						for(final RouterPeer peer : peers)
							System.out.println(
									CMStrings.padRight(peer.name, 20)+" "+
									CMStrings.padRight(peer.ip, 20)+" "+
									CMStrings.padRight(peer.port+"", 6)+" "+
									CMStrings.padRight(peer.isConnected()?"up":"down", 5)+" "+
									CMStrings.padRight(peer.muds.getMuds().size()+" muds", 10)+" "+
									CMStrings.padRight(peer.channels.getChannels().size()+" chans", 10)+" "+
							"");
					}
					else
					if(s.equalsIgnoreCase("channels"))
					{
						final Channel[] channels = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getChannels();
						for(final Channel peer : channels)
							System.out.println(
									CMStrings.padRight(peer.channel, 20)+" "+
									CMStrings.padRight(peer.owner, 20)+" "+
									CMStrings.padRight(peer.type+"", 12)+" "+
							"");
					}
					else
					if(s.equalsIgnoreCase("muds"))
					{
						final MudPeer[] muds = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getMudPeers();
						for(final MudPeer peer : muds)
							System.out.println(
									CMStrings.padRight(peer.mud_name, 20)+" "+
									CMStrings.padRight(peer.address+":"+peer.player_port, 20)+" "+
									CMStrings.padRight(peer.mud_type+"", 12)+" "+
									CMStrings.padRight(peer.isConnected()?"up":"down", 5)+" "+
									CMStrings.padRight(peer.listening.size()+" listens", 10)+" "+
							"");
					}
					else
						System.err.println("Try list muds, peers, or channels");
				}
				else
				if(s.toLowerCase().startsWith("info "))
				{
					s=s.substring(5).trim();
					if(s.toLowerCase().startsWith("peer "))
					{
						s=s.substring(5).trim();
						final RouterPeer[] peers = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getRouterPeers();
						RouterPeer peer = null;
						for(int i=0;i<peers.length;i++)
							if(peers[i].name.equals(s))
								peer=peers[i];
						if(peer == null)
							System.err.println("Peer '"+s+"' does not exist.");
						else
						{
							System.out.println("Router  : "+peer.name);
							System.out.println("Connect : "+peer.isConnected());
							System.out.println("IP      : "+peer.ip);
							System.out.println("Port    : "+peer.port);
							System.out.println("Password: "+peer.password);
							System.out.println("Chans   : "+peer.channels.list.size()+" ("+peer.channels.getChannelListId()+")");
							System.out.println("Muds    : "+peer.muds.getMuds().size()+" ("+peer.muds.getMudListId()+")");
							System.out.println("Modified: "+peer.modified);
						}
					}
					else
					if(s.toLowerCase().startsWith("channels"))
					{
						final Channel chan = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.findChannel(s);
						if(chan == null)
							System.err.println("Channel '"+s+"' does not exist.");
						else
						{
							System.out.println("Channel : "+chan.channel);
							System.out.println("Owner   : "+chan.owner);
							System.out.println("Type    : "+chan.type);
							System.out.println("Modified: "+chan.modified);
						}
					}
					else
					if(s.toLowerCase().startsWith("mud "))
					{
						s=s.substring(4).trim();
						final MudPeer[] muds = com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.getMudPeers();
						MudPeer mud = null;
						for(int i=0;i<muds.length;i++)
							if(muds[i].mud_name.equals(s))
								mud=muds[i];
						if(mud == null)
							System.err.println("Mud '"+s+"' does not exist.");
						else
						{
							System.out.println("Router  : "+mud.mud_name);
							System.out.println("Connect : "+mud.isConnected());
							System.out.println("Address : "+mud.address);
							System.out.println("Port    : "+mud.player_port);
							System.out.println("Password: "+mud.password);
							System.out.println("Admin@  : "+mud.admin_email);
							System.out.println("MudType : "+mud.mud_type);
							System.out.println("BMudlib : "+mud.base_mudlib);
							System.out.println("Mudlib  : "+mud.mudlib);
							System.out.println("Driver  : "+mud.driver);
							System.out.println("Router  : "+mud.router);
							System.out.println("State   : "+mud.state);
							System.out.println("Status  : "+mud.status);
							System.out.println("TCP-port: "+mud.tcp_port);
							System.out.println("UDP-port: "+mud.udp_port);
							System.out.println("Version : "+mud.version);
							System.out.println("Chan-id : "+mud.channelListId);
							System.out.println("Mud-id  : "+mud.mudListId);
							System.out.println("Modified: "+mud.modified);
						}
					}
					else
						System.err.println("Try info peer, mud, or channel");
				}
				else
				if(s.equalsIgnoreCase("exit"))
					break;
				System.out.print("> ");
				running = router.running;
				if(running)
					s = br.readLine();
			}
			com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router.shutdown();
			System.out.println("Bye-bye");
			System.exit(0);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
