package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
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
public class LocateQueryPacket extends MudPacket
{
	public String user_name;

	public LocateQueryPacket()
	{
		super();
		type = Packet.PacketType.LOCATE_REQ;
	}

	public LocateQueryPacket(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		try
		{
			type = Packet.PacketType.LOCATE_REQ;
			user_name = (String)v.elementAt(6);
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	public LocateQueryPacket(final String nom, final String who)
	{
		super();
		type = Packet.PacketType.LOCATE_REQ;
		sender_name = nom;
		user_name = who;
	}

	@Override
	public void send() throws InvalidPacketException
	{
		if( sender_name == null || user_name == null )
		{
			throw new InvalidPacketException();
		}
		super.send();
	}

	@Override
	public String toString()
	{
		final String cmd="({\"locate-req\",5,\"" + sender_mud +
			   "\",\"" + sender_name + "\",0,0,\"" +
			   user_name + "\",})";
		return cmd;
	}
}
