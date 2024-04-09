package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.Intermud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
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
public class ChannelAdd extends MudPacket
{
	public String channel = null;
	public int	channelType = 0;

	public ChannelAdd()
	{
		super();
		type = Packet.PacketType.CHANNEL_ADD;
	}

	public ChannelAdd(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		try
		{
			type = Packet.PacketType.CHANNEL_ADD;
			channel = (String)v.elementAt(6);
			channelType = ((Integer)v.elementAt(7)).intValue();
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	public ChannelAdd(final String chan, final String who, final int chtyp)
	{
		super();
		type = Packet.PacketType.CHANNEL_ADD;
		channel = chan;
		sender_name = who;
		channelType = chtyp;
	}

	@Override
	public void send() throws InvalidPacketException {
		if( channel == null  )
		{
			throw new InvalidPacketException();
		}
		super.send();
	}

	@Override
	public String toString()
	{
		final NameServer n = Intermud.getNameServer();
		final String cmd=
			 "({\"channel-add\",5,\"" + sender_mud + "\",\"" +
			   sender_name + "\",\""+n.name+"\",0,\"" + channel + "\",0,})";
		return cmd;
	}
}
