package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Exception;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Client;
import com.planet_ink.coffee_mud.core.intermud.i3.LPCData;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3RMud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.interfaces.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
/*
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The mudlib interface to the server.
 */
/**
 * The Server class is the mudlib's interface to the
 * Imaginary Mud Server.  It is responsible with knowing all
 * internal information about the server.
 * @author George Reese, Bo Zimmerman
 * @version 1.0
 */
public class I3Router
{
	static private I3RouterThread routerThread = null;
	static private boolean started = false;

	/**
	 * Creates a server thread if one has not yet been
	 * created.
	 * @throws ServerSecurityException thrown if an attempt to call start()
	 * is made once the server is running.
	 * @param mud the name of the mud being started
	 * @param ip the routers remote ip, because i only know the local ip
	 * @param port the port of the server
	 * @param password the password for this server
	 */
	static public void start(final String mud,
							 final String ip,
							 final int port,
							 final int password) throws ServerSecurityException
	{
		try
		{
			if( started )
			{
				throw new ServerSecurityException("Illegal attempt to start Router.");
			}
			started = true;
			routerThread = new I3RouterThread(mud, ip, port, password);
			Log.sysOut("I3Router", "InterMud3 Core (c)1996 George Reese");
			routerThread.start();
		}
		catch(final Exception e)
		{
			routerThread=null;
			Log.errOut("I3Server",e);
		}
	}

	public static NameServer getNameServer()
	{
		return I3Router.routerThread.me;
	}

	public static I3RouterThread getRouter()
	{
		return I3Router.routerThread;
	}

	/**
	 * Directs the router thread to manage the given object
	 * @throws ObjectLoadException thrown when a problem occurs loading the object
	 * @param the object loaded
	 * @return the same object
	 */
	static public ServerObject addObject(final ServerObject object) throws ObjectLoadException {
		if(object instanceof MudPeer)
			return routerThread.addMudPeer((MudPeer)object);
		if(object instanceof RouterPeer)
			return routerThread.addRouterPeer((RouterPeer)object);
		throw new ObjectLoadException("Object is unmanaged");
	}

	public static void sendMudChange(final I3RMud mud)
	{
		routerThread.sendMudChange(mud);
	}

	public static boolean writePacket(final IrnPacket irnpkt)
	{
		final RouterPeer peer = I3Router.findRouterPeer(irnpkt.target_router);
		if(peer == null)
		{
			Log.errOut("Unknown peer target: "+irnpkt.target_router);
			return false;
		}
		peer.lastPing = System.currentTimeMillis();
		return writePacket(irnpkt, peer);
	}

	public static boolean writePacket(final Packet mudpkt, final NetPeer peer)
	{
		if(!peer.isConnected())
			return false;
		final String cmd = mudpkt.toString();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.I3))
			Log.sysOut("I3Router","Sending: "+cmd);
		try
		{
			final byte[] packet = cmd.getBytes("ISO-8859-1");
			peer.getOutputStream().writeInt(packet.length);
			// Remove non-printables, as required by the I3 specification
			// (Contributed by David Green <green@couchpotato.net>)
			for (int i = 0; i < packet.length; i++)
			{
				// 160 is a non-breaking space. We'll consider that "printable".
				if ( (packet[i]&0xFF) < 32 || ((packet[i]&0xFF) >= 127 && (packet[i]&0xFF) <= 159))
				{
					// Java uses it as a replacement character,
					// so it's probably ok for us too.
					packet[i] = '?';
				}
				peer.getOutputStream().write(packet[i]);
			}
		}
		catch( final java.io.IOException e )
		{
			final String errMsg=e.getMessage()==null?e.toString():e.getMessage();
			if(errMsg!=null)
			{
				Log.errOut("557-"+errMsg);
				Log.errOut("Could not send "+mudpkt.getType()+" to "+peer.getName());
			}
			// this means the peer is DEAD!!!
			if(peer instanceof MudPeer)
				((MudPeer)peer).destruct();
			else
			if(peer instanceof RouterPeer)
				((RouterPeer)peer).destruct();
			else
			{
				try {
					peer.close();
				}
				catch (final IOException e1) { }
			}
			return false;
		}
		return true;
	}

	public static boolean writePacket(final MudPacket mudpkt)
	{
		final MudPeer targetMud = I3Router.findMudPeer(mudpkt.target_mud);
		if(targetMud == null)
		{
			Log.errOut("Unknown mud target: "+mudpkt.target_mud);
			return false;
		}
		targetMud.lastPing = System.currentTimeMillis();
		return writePacket(mudpkt, targetMud);
	}

	/**
	 * Returns mud
	 * @param name of the mud being loaded
	 * @return mud identified
	 */
	static public MudPeer findMudPeer(final String name) {
		return routerThread.findMudPeer(name);
	}

	public static boolean isConnected()
	{
		if(routerThread==null)
			return false;
		return routerThread.running;
	}

	/**
	 * Returns router
	 * @param name of the router being loaded
	 * @return router identified
	 */
	static public RouterPeer findRouterPeer(final String name) {
		return routerThread.findRouterPeer(name);
	}

	static public RouterPeer[] getRouterPeers() {
		return routerThread.getPeers();
	}

	static public Channel findChannel(final String str) {
		return routerThread.findChannel(str);
	}

	static public MudPeer[] getMudPeers() {
		return routerThread.getMuds();
	}

	static public Channel[] getChannels() {
		return new XArrayList<Channel>(routerThread.channels.getChannels().values()).toArray(new Channel[0]);
	}

	static public int getMudListId() {
		return routerThread.muds.getMudListId();
	}

	static public int getChannelListId() {
		return routerThread.channels.getChannelListId();
	}

	static public I3RMud[] getMudXPeers() {
		final List<I3RMud> peers = new XArrayList<I3RMud>();
		for(final MudPeer obj : routerThread.getMuds())
		{
			final I3RMud mud = obj;
			if(!obj.isConnected())
				mud.state=0;
			peers.add(mud);
		}
		return peers.toArray(new I3RMud[peers.size()]);
	}

	static public int getRouterPassword()
	{
		if(routerThread != null)
			return routerThread.getRouterPassword();
		return -1;
	}


	static public String getRouterName()
	{
		if(routerThread != null)
			return routerThread.getRouterName();
		if(I3Client.getNameServer() != null)
			return I3Client.getNameServer().name;
		return "";
	}

	static public int getPort()
	{
		if(routerThread != null)
			return routerThread.getPort();
		if(I3Client.getNameServer() != null)
			return I3Client.getNameServer().port;
		return 0;
	}

	static public void shutdown()
	{
		try
		{
			for(final RouterPeer peer : I3Router.getRouterPeers())
			{
				if(!peer.isConnected())
					continue;
				try
				{
					final IrnShutdown shutdown=new IrnShutdown(peer.name);
					shutdown.send();
				}
				catch(final Exception e)
				{
				}
			}
			routerThread.shutdown();
			started=false;
		}
		catch(final Exception e)
		{
		}
	}

	static public void removeObject(final ServerObject ob)
	{
		if( !ob.getDestructed() )
		{
			return;
		}
		if(ob instanceof MudPeer)
			routerThread.removeMudPeer((MudPeer)ob);
		if(ob instanceof RouterPeer)
			routerThread.removeRouterPeer((RouterPeer)ob);
	}

	static public void destroyObjectForever(final Object ob)
	{
		if(ob instanceof MudPeer)
		{
			((MudPeer)ob).destruct();
			routerThread.muds.list.remove(((MudPeer)ob).mud_name);
			routerThread.muds.setMudListId(getMudListId()+1);
		}
		else
		if(ob instanceof RouterPeer)
		{
			((RouterPeer)ob).destruct();
			routerThread.peers.list.remove(((RouterPeer)ob).name);
			routerThread.peers.setRouterListId(routerThread.peers.getRouterListId()+1);
		}
		else
		if(ob instanceof Channel)
		{
			routerThread.channels.list.remove(((Channel)ob).channel);
			routerThread.channels.setChannelListId(getChannelListId()+1);
		}
	}
}
