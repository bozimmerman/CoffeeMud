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

import java.util.List;
import java.util.Vector;

/**
 * Copyright (c)2024-2024 Bo Zimmerman
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
public class ChannelAdmin extends MudPacket
{
	public String channel = null;
	public List<String> addlist = new Vector<String>();
	public List<String> removelist = new Vector<String>();

	public ChannelAdmin()
	{
		super();
		type = Packet.PacketType.CHANNEL_ADMIN;
	}

	public ChannelAdmin(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		try
		{
			type = Packet.PacketType.CHANNEL_ADMIN;
			channel = (String)v.elementAt(6);
			if((v.size()>7) && (v.get(7) instanceof List<?>))
			{
				final List<?> ml = (List<?>)v.get(7);
				for(final Object mlo : ml)
					addlist.add(mlo.toString());
			}
			if((v.size()>8) && (v.get(8) instanceof List<?>))
			{
				final List<?> ml = (List<?>)v.get(8);
				for(final Object mlo : ml)
					removelist.add(mlo.toString());
			}
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	public ChannelAdmin(final String chan, final String who)
	{
		super();
		type = Packet.PacketType.CHANNEL_ADMIN;
		channel = chan;
		sender_name = who;
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
		final StringBuilder str = new StringBuilder("");
		str.append("({\"channel-admin\",5,\"" + sender_mud + "\",\"" +
			   sender_name + "\",\""+target_mud+"\",0,\"" + channel + "\",");
		{
			str.append("({");
			for(final String ml : this.addlist)
				str.append("\"").append(ml).append("\"").append(",");
			str.append("}),");
		}
		{
			str.append("({");
			for(final String ml : this.removelist)
				str.append("\"").append(ml).append("\"").append(",");
			str.append("}),");
		}
		str.append("})");
		return str.toString();
	}
}
