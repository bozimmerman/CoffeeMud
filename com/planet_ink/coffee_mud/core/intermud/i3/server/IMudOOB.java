package com.planet_ink.coffee_mud.core.intermud.i3.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Client;
/**
 * Copyright (c) 2024-2024 Bo Zimmerman
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
import com.planet_ink.coffee_mud.core.intermud.i3.net.NetPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelAdd;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelAdmin;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelDelete;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelListen;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.InvalidPacketException;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.LocateQueryPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.MudPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.OOBBegin;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.OOBPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.PingPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.StartupReq3;
import com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router;

public class IMudOOB implements NetPeer, ServerObject
{
	public String			id				= "";
	public Socket			sock;
	public DataInputStream	in;
	public DataOutputStream	out;
	public final long		connectTime		= System.currentTimeMillis();
	public long				lastPong		= System.currentTimeMillis();
	final long[]			timeoutCtr		= new long[] { 0 };
	public boolean			isDestructed	= false;
	public OOBState			state			= OOBState.CONNECTED;

	private static enum OOBState
	{
		CONNECTED,
		BEGUN,
		ENDED
	}

	public IMudOOB()
	{
		sock = null;
		in = null;
		out = null;
		isDestructed = false;
	}

	public IMudOOB(final Socket sock)
	{
		this.sock = sock;
		if(sock != null)
		{
			try
			{
				this.in = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
				this.out = new DataOutputStream(sock.getOutputStream());
			}
			catch (final IOException e)
			{
			}
		}
	}

	public IMudOOB(final NetPeer other)
	{
		super();
		this.sock = other.getSocket();
		this.in = other.getInputStream();
		this.out = other.getOutputStream();
		other.clearSocket();
	}

	@Override
	public void destruct()
	{
		try {
			close();
		}
		catch (final IOException e) { }
		isDestructed = true;
	}

	@Override
	public void processEvent()
	{
		final DataInputStream istream = getInputStream();
		if((!isConnected())||(istream == null))
		{
			destruct();
			return;
		}
		try
		{
			final Packet pkt;
			if((pkt = Packet.readPacket(this))==null)
			{
				final long now = System.currentTimeMillis();
				if((now - this.lastPong) > 600000)
				{
					destruct();
				}
				return;
			}
			if(!(pkt instanceof OOBPacket))
			{
				//sendError("not-allowed", "Not allowed to send this packet.", pkt);
				Log.errOut("Unwanted message type: "+pkt.getType().name() + " from "+getObjectId());
				return;
			}
			lastPong = System.currentTimeMillis();
			final OOBPacket mudpkt = (OOBPacket)pkt;
			switch(mudpkt.getType())
			{
			case OOB_BEGIN:
			{
				final OOBBegin ob = (OOBBegin)mudpkt;
				final Long key = I3Client.getIncomingKeys().get(ob.sender_mud);
				if((key == null)
				||((ob.authToken != key.longValue()) && (key.longValue()>=0)))
				{
					//TODO: post an error
				}
				else
					state = OOBState.BEGUN;
				break;
			}
			case OOB_END:
				if(this.state == OOBState.BEGUN)
					this.state = OOBState.ENDED;
				break;
			default:

			}
		}
		catch (final IOException e)
		{
			destruct();
			Log.errOut(getObjectId(),e);
		}
	}

	@Override
	public boolean getDestructed()
	{
		return isDestructed;
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

	@Override
	public Socket getSocket()
	{
		return sock;
	}

	@Override
	public boolean isConnected()
	{
		return (sock != null) && (sock.isConnected());
	}

	@Override
	public DataInputStream getInputStream()
	{
		return (isConnected()) ? in : null;
	}

	@Override
	public DataOutputStream getOutputStream()
	{
		return (isConnected()) ? out : null;
	}

	@Override
	public void clearSocket()
	{
		sock = null;
		in = null;
		out = null;
	}

	@Override
	public void close() throws IOException
	{
		if((sock != null)
		&&(isConnected()))
		{
			in.close();
			out.flush();
			out.close();
			sock = null;
			in = null;
			out = null;
		}

	}

	/**
	 * Return the identifying name
	 * @return the name
	 */
	@Override
	public String getName()
	{
		if(sock != null)
		{
			final SocketAddress addr = sock.getRemoteSocketAddress();
			if(addr != null)
				return addr.toString();
		}
		return this+"";
	}

	@Override
	public long getConnectTime()
	{
		return this.connectTime;
	}

	@Override
	public long[] getSockTimeout()
	{
		synchronized(this)
		{
			return timeoutCtr;
		}
	}

}
