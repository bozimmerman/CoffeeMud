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
public class MailPacket extends OOBPacket
{
	public int id = 0;
	public String sending_user = "";
	public Hashtable<String,Vector<String>> to = new Hashtable<String,Vector<String>>();
	public Hashtable<String,Vector<String>> cc = new Hashtable<String,Vector<String>>();
	public Vector<String> bcc = new Vector<String>();
	public int send_time = 0;
	public String subject = "";
	public String message = "";

	public MailPacket()
	{
		super();
		type = Packet.PacketType.MAIL;
	}

	@SuppressWarnings({ "unchecked" })
	public MailPacket(final Vector<?> v)
	{
		super();
		id = s_int(v,1);
		sending_user = s_str(v,2);
		if((v.size()>3) && (v.get(3) instanceof Map))
			to.putAll((Map<String,Vector<String>>)v.get(3));
		if((v.size()>4) && (v.get(4) instanceof Map))
			cc.putAll((Map<String,Vector<String>>)v.get(4));
		if((v.size()>5) && (v.get(5) instanceof Vector))
			bcc.addAll((Vector<String>)v.get(5));
		send_time = s_int(v,6);
		subject = s_str(v,7);
		message = s_str(v,8);
		type = Packet.PacketType.MAIL;
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
		str.append("({\"mail\","+id+",");
		str.append("\"").append(sending_user).append("\",");
		str.append("([");
		for(final String key : to.keySet())
		{
			str.append("\"").append(key).append("\"").append(":({");
			final Vector<String> v = to.get(key);
			for(final String s : v)
				str.append("\"").append(s).append("\",");
			str.append("}),");
		}
		str.append("]),");
		str.append("([");
		for(final String key : cc.keySet())
		{
			str.append("\"").append(key).append("\"").append(":({");
			final Vector<String> v = cc.get(key);
			for(final String s : v)
				str.append("\"").append(s).append("\",");
			str.append("}),");
		}
		str.append("]),({");
		for(final String s : bcc)
			str.append("\"").append(s).append("\",");
		str.append("}),");
		str.append(send_time).append(",");
		str.append("\"").append(subject).append("\",");
		str.append("\"").append(message).append("\",");
		str.append("})");
		return str.toString();
	}
}
