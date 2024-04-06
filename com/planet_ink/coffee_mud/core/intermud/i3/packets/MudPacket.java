package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.Intermud;
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

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Copyright (c) 1996 George Reese, (c) 2024 Bo Zimmerman
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
public class MudPacket extends Packet
{
	public String		sender_mud	= null;
	public String		sender_name	= null;
	public String		target_mud	= null;
	public String		target_name	= null;
	public PacketType	type		= null;

	public MudPacket()
	{
		super();
		sender_mud = I3Server.getMudName();
	}

	public MudPacket(final Vector<?> v)
	{
		super();
		{
			Object ob;

			ob = v.elementAt(2);
			if( ob instanceof String )
			{
				sender_mud = (String)ob;
			}
			ob = v.elementAt(3);
			if( ob instanceof String )
			{
				sender_name = (String)ob;
			}
			ob = v.elementAt(4);
			if( ob instanceof String )
			{
				target_mud = (String)ob;
			}
			ob = v.elementAt(5);
			if( ob instanceof String )
			{
				target_name = (String)ob;
			}
		}
	}

	public PacketType getType()
	{
		return type;
	}

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

	public void send() throws InvalidPacketException {
		if( type == null )
		{
			throw new InvalidPacketException();
		}
		Intermud.sendPacket(this);
	}

}
