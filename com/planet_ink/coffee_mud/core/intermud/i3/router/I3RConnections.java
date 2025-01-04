package com.planet_ink.coffee_mud.core.intermud.i3.router;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.collections.XVector;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3RMud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.RNameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.ListenThread;
import com.planet_ink.coffee_mud.core.intermud.i3.net.NetPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.UnknownNetPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnMudlistDelta;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnStartupRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.StartupReply;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.StartupReq3;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent;
import com.planet_ink.coffee_mud.core.intermud.i3.server.ServerObject;

/**
 * Copyright (c) 2024-2025 Bo Zimmerman
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
 */
public class I3RConnections implements ServerObject
{
	public final static int peerTimeout = 10000;

	boolean isShutdown = false;
	String id;

	public I3RConnections()
	{
		this.id = "I3RConn"+Thread.currentThread().getThreadGroup().getName().charAt(0);
	}

	@Override
	public void destruct()
	{
		isShutdown = true;
	}

	@Override
	public void processEvent()
	{
		final ListenThread listen_thread;
		final I3RouterThread routerBase = I3Router.getRouter();
		synchronized(this)
		{
			listen_thread = routerBase.listen_thread;
		}
		if(listen_thread != null)
		{
			// Get new tentative connections
			final Map<String, NetPeer> socks;
			synchronized( routerBase )
			{
				socks = routerBase.socks;
			}
			Socket sock = listen_thread.nextSocket();
			while(sock != null)
			{
				final UnknownNetPeer newPeer = new UnknownNetPeer(sock);
				synchronized( socks )
				{
					socks.put(newPeer.toString(), newPeer);
				}
				sock = listen_thread.nextSocket();
			}
			Packet pkt;
			for(final Iterator<String> i = socks.keySet().iterator();i.hasNext();)
			{
				final String key = i.next();
				final NetPeer peer = socks.get(key);
				final DataInputStream istream = peer.getInputStream();
				try
				{
					if(istream == null)
					{
						peer.close();
						i.remove();
					}
					else
					if((pkt = Packet.readPacket(peer)) != null)
					{
						if(pkt.getType() == Packet.PacketType.IRN_STARTUP_REQ)
						{
							final IrnStartupRequest ipkt = (IrnStartupRequest)pkt;
							if(((ipkt.target_password == I3Router.getRouterPassword())
								|| (I3Router.getRouterPassword() < 0))
							&&(ipkt.target_router.equalsIgnoreCase(I3Router.getRouterName())))
							{
								String remoteAddr = peer.getSocket().getRemoteSocketAddress().toString();
								int port = -1;
								final int x = remoteAddr.indexOf(':');
								if(x>0)
								{
									port = CMath.s_int(remoteAddr.substring(x+1));
									remoteAddr = remoteAddr.substring(0,x);
								}
								Log.sysOut("Accepting peer "+ipkt.sender_router);
								final RNameServer ns = new RNameServer(remoteAddr,port,ipkt.sender_router);
								ns.password = ipkt.sender_password;
								final RouterPeer opeer = I3Router.getRouter().peers.getRouters().get(ipkt.sender_router);
								if(opeer != null)
								{
									opeer.modified = Persistent.MODIFIED;
									opeer.password = ipkt.sender_password;
									opeer.setSocket(peer.getSocket());
									peer.clearSocket();
								}
								else
								{
									final RouterPeer rpeer = new RouterPeer(ns, peer);
									I3Router.addObject(rpeer);
								}
								// do not close peer, as we want to keep the socks open
							}
							else
								peer.close();
							i.remove();
						}
						else
						if(pkt.getType() == Packet.PacketType.STARTUP_REQ_3)
						{
							final StartupReq3 mpkt = (StartupReq3)pkt;
							if( true
							//&&((mpkt.password == I3Router.getRouterPassword()) // let all clients enter
							//	|| (I3Router.getRouterPassword() < 0))
							&&(mpkt.target_router.equalsIgnoreCase(I3Router.getRouterName())))
							{
								final I3RMud mudx = mpkt.makeMud(peer);
								Log.sysOut("Accepting mud "+mpkt.sender_router);
								final MudPeer newMud = new MudPeer(mudx, peer);
								newMud.state = -1; // mark online
								I3Router.addObject(newMud);
							}
							else
								peer.close();
							i.remove();
						}
						else
							Log.sysOut(getObjectId(), "Rejecting new peer packet type: "+pkt.getType());
					}
					else
					if((System.currentTimeMillis() - peer.getConnectTime()) > peerTimeout)
					{
						peer.close();
						i.remove();
					}
				}
				catch (final Exception e)
				{
					try
					{
						peer.close();
					}
					catch (final IOException e1)
					{
					}
					Log.errOut(this.id, e);
					i.remove();
				}
			}
		}
	}

	@Override
	public boolean getDestructed()
	{
		return isShutdown;
	}

	@Override
	public String getObjectId()
	{
		return id;
	}

	@Override
	public void setObjectId(final String id)
	{
		this.id = id;
	}

}
