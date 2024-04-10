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
					System.out.println("Exit     : shutdown");
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
									CMStrings.padRight(peer.getMud().mud_name, 20)+" "+
									CMStrings.padRight(peer.getMud().address+":"+peer.getMud().player_port, 20)+" "+
									CMStrings.padRight(peer.getMud().mud_type+"", 12)+" "+
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
