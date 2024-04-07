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
		final String cmd = toString();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.I3))
			Log.sysOut("I3Router","Sending: "+cmd);
		try
		{
			final byte[] packet = cmd.getBytes("ISO-8859-1");
			final RouterPeer peer = I3Router.getRouter().getPeer(this.target_router);
			if(peer != null)
			{
				peer.getOutputStream().writeInt(packet.length);
				// Remove non-printables, as required by the I3 specification
				// (Contributed by David Green <green@couchpotato.net>)
				for (int i = 0; i < packet.length; i++)
				{
					// 160 is a non-breaking space. We'll consider that "printable".
					if ( (packet[i]&0xFF) < 32 || ((packet[i]&0xFF) >= 127 && (packet[i]&0xFF) <= 159))
					{
						// Java uses it as a replacement character,
						// so it's probably ok for us too.
						packet[i] = '?';
					}
					peer.getOutputStream().write(packet[i]);
				}
			}
		}
		catch( final java.io.IOException e )
		{
			final String errMsg=e.getMessage()==null?e.toString():e.getMessage();
			if(errMsg!=null)
			{
				Log.errOut("I3Router","557-"+errMsg);
			}
		}
	}
}
