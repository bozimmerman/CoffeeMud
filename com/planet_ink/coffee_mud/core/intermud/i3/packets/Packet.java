package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Exception;
import com.planet_ink.coffee_mud.core.intermud.i3.LPCData;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
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
 */
public abstract class Packet
{
	/*
		Transmissions are LPC arrays with a predefined set of six initial elements:
	({ type, ttl, originator mudname, originator username, target mudname, target username, ... }).
	*/
	public static enum PacketType
	{
		CHANNEL_M(ChannelMessage.class),
		CHANNEL_E(ChannelEmote.class),
		CHANNEL_T(ChannelTargetEmote.class),
		WHO_REQ(WhoReqPacket.class),
		WHO_REPLY(WhoReplyPacket.class),
		TELL(TellPacket.class),
		LOCATE_REQ(LocateQueryPacket.class),
		LOCATE_REPLY(LocateReplyPacket.class),
		CHAN_WHO_REQ(ChannelWhoRequest.class),
		CHAN_WHO_REPLY(ChannelWhoReply.class),
		CHANNEL_ADD(ChannelAdd.class),
		CHANNEL_ADMIN(ChannelAdmin.class),
		CHANNEL_REMOVE(ChannelDelete.class),
		CHANNEL_LISTEN(ChannelListen.class),
		CHAN_USER_REQ(ChannelUserRequest.class),
		CHAN_USER_REPLY(ChannelUserReply.class),
		SHUTDOWN(ShutdownPacket.class),
		FINGER_REQ(FingerRequest.class),
		FINGER_REPLY(FingerReply.class),
		PING_REQ(PingPacket.class),
		OOB_REQ(OOBReq.class),
		AUTH_MUD_REQ(MudAuthRequest.class),
		AUTH_MUD_REPLY(MudAuthReply.class),
		UCACHE_UPDATE(UCacheUpdate.class),
		MUDLIST(MudlistPacket.class),
		STARTUP_REPLY(StartupReply.class),
		ERROR(ErrorPacket.class),
		CHANLIST_REPLY(ChanlistReply.class),
		STARTUP_REQ_3(StartupReq3.class),
		IRN_STARTUP_REQ(IrnStartupRequest.class),
		IRN_MUDLIST_REQ(IrnMudlistRequest.class),
		IRN_MUDLIST_DELTA(IrnMudlistDelta.class),
		IRN_MUDLIST_ALTERED(IrnMudlistDelta.class),
		IRN_CHANLIST_REQ(IrnChanlistRequest.class),
		IRN_CHANLIST_DELTA(IrnChanlistDelta.class),
		IRN_CHANLIST_ALTERED(IrnChanlistDelta.class),
		IRN_DATA(IrnData.class),
		IRN_PING(IrnPing.class),
		IRN_SHUTDOWN(IrnShutdown.class),
		OOB_BEGIN(OOBBegin.class),
		OOB_END(OOBEnd.class),
		MAIL(MailPacket.class),
		MAIL_ACK(MailAck.class),
		;
		public Class<? extends Packet> packetClass;
		String key;
		private PacketType(final Class<? extends Packet> pktClass)
		{
			key = this.name().toLowerCase().replace("_", "-");
			if(pktClass != null)
				packetClass = pktClass;
			else
				packetClass = null;
		}
	}

	public Packet()
	{
		super();
	}

	public Packet(final Vector<?> v)
	{
		super();
	}

	public abstract PacketType getType();

	public abstract void send() throws InvalidPacketException;

	public String convertString(final String cmd)
	{
		final StringBuffer b = new StringBuffer(cmd);
		int i = 0;

		while( i < b.length() )
		{
			final char c = b.charAt(i);

			if( c != '\\' && c != '"' )
			{
				i++;
			}
			else
			{
				b.insert(i, '\\');
				i += 2;
			}
		}
		return new String(b);
	}

	public static Packet readPacket(final NetPeer peer) throws IOException
	{
		final DataInputStream istream = peer.getInputStream();
		if(istream.available() >= 4)
		{
			if(istream.markSupported())
				istream.mark(65536);
			final int len = istream.readInt();
			if(len > 65536)
			{
				if(istream.markSupported())
					istream.reset();
				istream.skip(istream.available());
				return null;
			}
			final long[] timeout = peer.getSockTimeout();
			if(istream.available() >= len)
			{
				synchronized(timeout)
				{
					timeout[0] = 0;
				}
				final byte[] tmp = new byte[len];
				istream.readFully(tmp);
				final String cmd=new String(tmp);
				Object o;
				try
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.I3))
						Log.sysOut("Receiving: "+cmd);
					o = LPCData.getLPCData(cmd);
					if((!(o instanceof Vector))
					||(((Vector<?>)o).size()<4))
					{
						Log.errOut("I3R: 390-"+o);
						if(istream.markSupported())
							istream.reset();
						istream.skip(istream.available());
						return null;
					}
					final Vector<?> data=(Vector<?>)o;
					final String typeStr = ((String)data.elementAt(0)).trim().replace("-", "_");
					final PacketType type = (PacketType)CMath.s_valueOf(PacketType.class,typeStr.toUpperCase());
					if(type == null)
					{
						Log.errOut("I3R: Unknown packet type: " + typeStr);
						return null;
					}
					final Class<? extends Packet> pktClass = type.packetClass;
					if(pktClass == null)
						Log.errOut("I3R: Other packet type: " + typeStr);
					else
					{
						try
						{
							final Constructor<? extends Packet> con = pktClass.getConstructor(Vector.class);
							return con.newInstance(data);
						}
						catch( final Exception  e )
						{
							Log.errOut("I3R: "+type+"-"+e.getMessage());
						}
					}
				}
				catch (final I3Exception e)
				{
					Log.errOut("I3R: 390-"+e.getMessage());
					if(istream.markSupported())
						istream.reset();
					istream.skip(istream.available());
					return null;
				}
			}
			else
			{
				if(istream.markSupported())
					istream.reset();
				final long currto;
				synchronized(timeout)
				{
					currto = timeout[0];
				}
				if(currto > 0)
				{
					if((System.currentTimeMillis() - currto)>5000)
					{
						Log.errOut("I3R: 390-Eating"+istream.available());
						istream.skipBytes(istream.available());
						timeout[0] = 0;
					}
				}
				else
					timeout[0] = System.currentTimeMillis();
			}
		}
		return null;
	}

	protected int s_int(final Object o)
	{
		if(o instanceof Integer)
			return ((Integer)o).intValue();
		if(o instanceof Long)
			return ((Long)o).intValue();
		if(o instanceof String)
		{
			try
			{
				return Integer.valueOf((String)o).intValue();
			}
			catch(final Exception e) { }
		}
		return -1;
	}

	protected int s_int(final List<?> lst, final int index)
	{
		if((index >=0) && (index < lst.size()))
		{
			return s_int(lst.get(index));
		}
		return -1;
	}

	protected long s_long(final Object o)
	{
		if(o instanceof Integer)
			return ((Integer)o).longValue();
		if(o instanceof Long)
			return ((Long)o).longValue();
		if(o instanceof String)
		{
			try {
				return Long.valueOf((String)o).longValue();
			}
			catch(final Exception e) { }
		}
		return -1;
	}

	protected long s_long(final List<?> lst, final int index)
	{
		if((index >=0) && (index < lst.size()))
			return s_long(lst.get(index));
		return -1;
	}

	protected String s_str(final Object o)
	{
		if(o instanceof String)
			return (String)o;
		return "";
	}

	protected String s_str(final Object o, final String def)
	{
		if(o instanceof String)
			return (String)o;
		return def;
	}

	protected String s_str(final List<?> lst, final int index)
	{
		if((index >=0) && (index < lst.size()))
		{
			final Object o = lst.get(index);
			if(o instanceof String)
				return (String)o;
		}
		return "";
	}
}
