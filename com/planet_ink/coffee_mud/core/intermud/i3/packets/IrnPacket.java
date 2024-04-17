package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router;
import com.planet_ink.coffee_mud.core.intermud.i3.router.I3RouterThread;
import com.planet_ink.coffee_mud.core.intermud.i3.router.RouterPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
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

import java.util.List;
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
public class IrnPacket extends Packet
{
	public long			ttl				= 0;
	public String		sender_router	= null;
	public String		target_router	= null;
	public PacketType	type			= null;

	public IrnPacket(final String targetRouter)
	{
		super();
		sender_router = I3Router.getRouterName();
		target_router = targetRouter;
	}

	public IrnPacket(final Vector<?> v)
	{
		super();
		{
			Object ob;

			ob = v.elementAt(1);
			if( ob instanceof Integer )
				ttl = ((Integer) ob).intValue();
			else
			if( ob instanceof Long )
				ttl = ((Long) ob).intValue();
			else
			if( ob instanceof Double )
				ttl = ((Double) ob).intValue();

			ob = v.elementAt(2);
			if( ob instanceof String )
			{
				sender_router = (String)ob;
			}
			ob = v.elementAt(4);
			if( ob instanceof String )
			{
				target_router = (String)ob;
			}
			// payload in 6
		}
	}

	@Override
	public PacketType getType()
	{
		return type;
	}

	protected int s_int(final Object o)
	{
		if(o instanceof Integer)
			return ((Integer)o).intValue();
		return -1;
	}

	protected int s_int(final List<?> lst, final int index)
	{
		if((index >=0) && (index < lst.size()))
		{
			final Object o = lst.get(index);
			if(o instanceof Integer)
				return ((Integer)o).intValue();
		}
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

	@Override
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

	@Override
	public void send() throws InvalidPacketException
	{
		if( type == null )
		{
			throw new InvalidPacketException();
		}
		if( target_router == null
		|| sender_router == null)
		{
			throw new InvalidPacketException();
		}
		I3Router.writePacket(this);
	}
}
