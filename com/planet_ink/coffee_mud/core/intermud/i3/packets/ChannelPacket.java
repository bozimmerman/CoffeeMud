package com.planet_ink.coffee_mud.core.intermud.i3.packets;

import java.util.Vector;

import com.planet_ink.coffee_mud.core.intermud.i3.Intermud;

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
public abstract class ChannelPacket extends MudPacket
{
	public String channel = null;
	public String sender_visible_name = null;
	public String message = null;

	public ChannelPacket()
	{
		super();
	}

	public ChannelPacket(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		try
		{
			channel = (String)v.elementAt(6);
			channel = Intermud.getLocalChannel(channel);
			sender_visible_name = (String)v.elementAt(7);
			message = (String)v.elementAt(8);
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	public ChannelPacket(final Packet.PacketType t, final String chan, final String who, final String vis, final String msg)
	{
		super();
		type = Packet.PacketType.CHANNEL_REMOVE;
		channel = chan;
		sender_visible_name = vis;
		sender_name = who;
		message = msg;
	}

	@Override
	public void send() throws InvalidPacketException {
		if( channel == null || sender_visible_name == null )
		{
			throw new InvalidPacketException();
		}
		final String fixedChannel = Intermud.getRemoteChannel(channel);
		if(((fixedChannel != null)&&(fixedChannel.length()>0))
		||(channel == null))
			channel = fixedChannel;
		message = convertString(message);
		super.send();
	}

}
