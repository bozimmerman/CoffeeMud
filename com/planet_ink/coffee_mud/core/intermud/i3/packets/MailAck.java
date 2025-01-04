package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Client;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.server.I3Server;
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

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

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
public class MailAck extends OOBPacket
{
	public Hashtable<String,Vector<String>> ack = new Hashtable<String,Vector<String>>();

	public MailAck()
	{
		super();
		type = Packet.PacketType.MAIL_ACK;
	}

	@SuppressWarnings({ "unchecked" })
	public MailAck(final Vector<?> v)
	{
		super();
		if((v.size()>1) && (v.get(1) instanceof Map))
			ack.putAll((Map<String,Vector<String>>)v.get(1));
		type = Packet.PacketType.MAIL_ACK;
	}

	@Override
	public void send() throws InvalidPacketException
	{
		super.send();
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder("");
		str.append("({\"mail-ack\",");
		str.append("([");
		for(final String key : ack.keySet())
		{
			str.append("\"").append(key).append("\"").append(":({");
			final Vector<String> v = ack.get(key);
			for(final String s : v)
				str.append("\"").append(s).append("\",");
			str.append("}),");
		}
		str.append("]),");
		str.append("})");
		return str.toString();
	}
}
